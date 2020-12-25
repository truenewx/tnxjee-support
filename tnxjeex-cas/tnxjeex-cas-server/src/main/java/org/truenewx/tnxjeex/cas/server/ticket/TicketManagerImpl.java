package org.truenewx.tnxjeex.cas.server.ticket;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Service;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.core.util.BeanUtil;
import org.truenewx.tnxjee.core.util.EncryptUtil;
import org.truenewx.tnxjee.model.spec.user.security.UserSpecificDetails;
import org.truenewx.tnxjee.service.transaction.annotation.WriteTransactional;
import org.truenewx.tnxjee.web.util.WebUtil;
import org.truenewx.tnxjee.webmvc.security.util.SecurityUtil;
import org.truenewx.tnxjeex.cas.server.entity.ServiceTicket;
import org.truenewx.tnxjeex.cas.server.entity.TicketGrantingTicket;
import org.truenewx.tnxjeex.cas.server.repo.MemoryServiceTicketRepo;
import org.truenewx.tnxjeex.cas.server.repo.MemoryTicketGrantingTicketRepo;
import org.truenewx.tnxjeex.cas.server.repo.ServiceTicketRepo;
import org.truenewx.tnxjeex.cas.server.repo.TicketGrantingTicketRepo;

/**
 * 票据管理器实现
 */
@Service
public class TicketManagerImpl implements TicketManager {
    @Autowired
    private ServerProperties serverProperties;
    private TicketGrantingTicketRepo ticketGrantingTicketRepo = new MemoryTicketGrantingTicketRepo();
    private ServiceTicketRepo serviceTicketRepo = new MemoryServiceTicketRepo();

    @Autowired(required = false)
    public void setTicketGrantingTicketRepo(TicketGrantingTicketRepo ticketGrantingTicketRepo) {
        this.ticketGrantingTicketRepo = ticketGrantingTicketRepo;
    }

    @Autowired(required = false)
    public void setServiceTicketRepo(ServiceTicketRepo serviceTicketRepo) {
        this.serviceTicketRepo = serviceTicketRepo;
    }

    @Override
    @WriteTransactional
    public void createTicketGrantingTicket(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        String ticketGrantingTicketId = TICKET_GRANTING_TICKET_PREFIX
                + EncryptUtil.encryptByMd5(session.getId() + System.currentTimeMillis());
        TicketGrantingTicket ticketGrantingTicket = new TicketGrantingTicket(ticketGrantingTicketId);
        ticketGrantingTicket.setUserDetails(SecurityUtil.getAuthorizedUserDetails());
        Date createTime = new Date();
        ticketGrantingTicket.setCreateTime(createTime);
        Duration timeout = this.serverProperties.getServlet().getSession().getTimeout();
        Date expiredTime = new Date(createTime.getTime() + timeout.toMillis());
        ticketGrantingTicket.setExpiredTime(expiredTime);
        this.ticketGrantingTicketRepo.save(ticketGrantingTicket);

        // 按照CAS规范将TGT写入Cookie
        int cookieMaxAge = (int) timeout.toSeconds();
        WebUtil.addCookie(request, response, TGT_NAME, ticketGrantingTicketId, cookieMaxAge);

        // Cookie中的TGT需要到下一个请求时才能获取，缓存TGT到当前会话，以便当前请求的后续处理获取TGT
        session.setAttribute(TGT_NAME, ticketGrantingTicketId);
    }

    /**
     * 读取已有的票据授权票据id，如果没有则返回null
     *
     * @param request 请求
     * @return 票据授权票据id
     */
    private String readTicketGrantingTicketId(HttpServletRequest request) {
        // 优先从当前会话缓存中获取TGT
        String ticketGrantingTicketId = (String) request.getSession().getAttribute(TGT_NAME);
        if (ticketGrantingTicketId == null) {
            ticketGrantingTicketId = WebUtil.getCookieValue(request, TGT_NAME);
        }
        return ticketGrantingTicketId;
    }

    /**
     * 查找有效的票据授权票据实体，如果没有或已过期则返回null
     *
     * @param request 请求
     * @return 票据授权票据实体
     */
    private TicketGrantingTicket findValidTicketGrantingTicket(HttpServletRequest request) {
        String ticketGrantingTicketId = readTicketGrantingTicketId(request);
        if (ticketGrantingTicketId != null) {
            TicketGrantingTicket ticketGrantingTicket = this.ticketGrantingTicketRepo.findById(ticketGrantingTicketId)
                    .orElse(null);
            if (ticketGrantingTicket != null) {
                if (ticketGrantingTicket.getExpiredTime().getTime() > System.currentTimeMillis()) {
                    return ticketGrantingTicket;
                } else { // 如果已过期则删除，以尽量减少垃圾数据
                    this.ticketGrantingTicketRepo.delete(ticketGrantingTicket);
                }
            }
        }
        return null;
    }

    @Override
    @WriteTransactional
    public boolean checkTicketGrantingTicket(HttpServletRequest request) {
        return findValidTicketGrantingTicket(request) != null;
    }

    // 用户登录或登出CAS服务器成功后调用，以获取目标服务的票据
    @Override
    @WriteTransactional
    public String getServiceTicket(HttpServletRequest request, String service) {
        TicketGrantingTicket ticketGrantingTicket = findValidTicketGrantingTicket(request);
        if (ticketGrantingTicket != null) {
            String ticketGrantingTicketId = ticketGrantingTicket.getId();
            ServiceTicket serviceTicket = this.serviceTicketRepo
                    .findFirstByTicketGrantingTicketIdAndService(ticketGrantingTicketId, service);
            if (serviceTicket == null) { // 不存在则创建新的
                Date now = new Date();
                String text = ticketGrantingTicketId + Strings.MINUS + service + Strings.MINUS + now.getTime();
                String serviceTicketId = SERVICE_TICKET_PREFIX + EncryptUtil.encryptByMd5(text);
                serviceTicket = new ServiceTicket(serviceTicketId);
                serviceTicket.setTicketGrantingTicket(ticketGrantingTicket);
                serviceTicket.setService(service);
                serviceTicket.setCreateTime(now);
                // 所属票据授权票据的过期时间即为服务票据的过期时间
                serviceTicket.setExpiredTime(ticketGrantingTicket.getExpiredTime());
                this.serviceTicketRepo.save(serviceTicket);
            }
            return serviceTicket.getId();
        }
        return null;
    }

    @Override
    public Collection<ServiceTicket> deleteTicketGrantingTicket(HttpServletRequest request,
            HttpServletResponse response) {
        TicketGrantingTicket ticketGrantingTicket = findValidTicketGrantingTicket(request);
        if (ticketGrantingTicket != null) {
            Collection<ServiceTicket> serviceTickets = this.serviceTicketRepo
                    .deleteByTicketGrantingTicketId(ticketGrantingTicket.getId());
            this.ticketGrantingTicketRepo.delete(ticketGrantingTicket);
            // 按照CAS规范将TGT从Cookie移除
            WebUtil.removeCookie(request, response, TGT_NAME);
            return serviceTickets;
        }
        return Collections.emptyList();
    }

    // 用户访问业务服务，由业务服务校验票据有效性时调用
    @Override
    public Assertion validateServiceTicket(String service, String serviceTicketId) {
        ServiceTicket serviceTicket = this.serviceTicketRepo.findById(serviceTicketId).orElse(null);
        if (serviceTicket == null || !serviceTicket.getService().equals(service)) {
            return null;
        }
        UserSpecificDetails<?> userDetails = serviceTicket.getTicketGrantingTicket().getUserDetails();
        String name = userDetails.getIdentity().toString();
        Map<String, Object> attributes = BeanUtil.toMap(userDetails, "identity", "password",
                "enabled", "accountNonExpired", "accountNonLocked", "credentialsNonExpired");
        AttributePrincipal principal = new AttributePrincipalImpl(name, attributes);
        return new AssertionImpl(principal, serviceTicket.getCreateTime(), serviceTicket.getExpiredTime(),
                serviceTicket.getCreateTime(), Collections.emptyMap());
    }

}
