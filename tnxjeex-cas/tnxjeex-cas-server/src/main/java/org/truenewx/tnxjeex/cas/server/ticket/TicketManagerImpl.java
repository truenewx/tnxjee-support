package org.truenewx.tnxjeex.cas.server.ticket;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.*;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Service;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.core.util.BeanUtil;
import org.truenewx.tnxjee.core.util.EncryptUtil;
import org.truenewx.tnxjee.model.spec.user.security.UserSpecificDetails;
import org.truenewx.tnxjee.service.transaction.annotation.WriteTransactional;
import org.truenewx.tnxjee.web.security.util.SecurityUtil;
import org.truenewx.tnxjee.web.util.WebUtil;

/**
 * 票据管理器实现
 */
@Service
public class TicketManagerImpl implements TicketManager, HttpSessionListener {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ServerProperties serverProperties;
    private ServiceTicketRepo serviceTicketRepo = new MemoryServiceTicketRepo();

    @Autowired(required = false)
    public void setServiceTicketRepo(ServiceTicketRepo serviceTicketRepo) {
        this.serviceTicketRepo = serviceTicketRepo;
    }

    @Override
    public void createTicketGrantingTicket(HttpServletRequest request,
            HttpServletResponse response) {
        HttpSession session = request.getSession();
        String ticketGrantingTicket = generateTicketGrantingTicket(session.getId());
        session.setAttribute(TGT_NAME, ticketGrantingTicket);
        // 按照CAS规范将TGT写入Cookie，实际上并不会使用Cookie中的值
        int maxAge = (int) this.serverProperties.getServlet().getSession().getTimeout().toSeconds();
        WebUtil.addCookie(request, response, TGT_NAME, ticketGrantingTicket, maxAge);
    }

    private String generateTicketGrantingTicket(String sessionId) {
        return TICKET_GRANTING_TICKET_PREFIX + EncryptUtil.encryptByMd5(sessionId + System.currentTimeMillis());
    }

    private String getTicketGrantingTicket(HttpServletRequest request) {
        return getTicketGrantingTicket(request.getSession());
    }

    private String getTicketGrantingTicket(HttpSession session) {
        return (String) session.getAttribute(TGT_NAME);
    }

    @Override
    public boolean validateTicketGrantingTicket(HttpServletRequest request) {
        String ticketGrantingTicket = getTicketGrantingTicket(request);
        return ticketGrantingTicket != null && this.serviceTicketRepo
                .countByTicketGrantingTicketAndEarliestExpiredTime(ticketGrantingTicket, new Date()) > 0;
    }

    // 用户登录或登出CAS服务器成功后调用，以获取目标服务的票据
    @Override
    @WriteTransactional
    public String getServiceTicket(HttpServletRequest request, String service) {
        String ticketGrantingTicket = getTicketGrantingTicket(request);
        if (ticketGrantingTicket != null) {
            ServiceTicket ticket = this.serviceTicketRepo
                    .findByTicketGrantingTicketAndService(ticketGrantingTicket, service);
            if (ticket != null && ticket.getExpiredTime().before(new Date())) { // 已过期的先删除，再视为null
                this.serviceTicketRepo.deleteById(ticket.getId());
                ticket = null;
            }
            if (ticket == null) { // 不存在则创建新的
                Date now = new Date();
                String text = ticketGrantingTicket + Strings.MINUS + service + Strings.MINUS + now.getTime();
                String ticketId = SERVICE_TICKET_PREFIX + EncryptUtil.encryptByMd5_16(text);
                long timeout = this.serverProperties.getServlet().getSession().getTimeout().toMillis();
                Date expiredTime = new Date(now.getTime() + timeout);
                ticket = new ServiceTicket(ticketId);
                ticket.setTicketGrantingTicket(ticketGrantingTicket);
                ticket.setService(service);
                ticket.setUserDetails(SecurityUtil.getAuthorizedUserDetails());
                ticket.setCreateTime(now);
                ticket.setExpiredTime(expiredTime);
                this.serviceTicketRepo.save(ticket);
            }
            return ticket.getId();
        }
        return null;
    }

    @Override
    public Collection<ServiceTicket> findServiceTickets(HttpServletRequest request) {
        String ticketGrantingTicket = getTicketGrantingTicket(request);
        if (ticketGrantingTicket != null) {
            return this.serviceTicketRepo.findByTicketGrantingTicket(ticketGrantingTicket);
        }
        return Collections.emptyList();
    }

    // 用户访问业务服务，由业务服务校验票据有效性时调用
    @Override
    public Assertion validateServiceTicket(String service, String ticketId) {
        ServiceTicket ticket = this.serviceTicketRepo.findById(ticketId);
        if (ticket == null || !ticket.getService().equals(service)) {
            return null;
        }
        UserSpecificDetails<?> userDetails = ticket.getUserDetails();
        String name = userDetails.getIdentity().toString();
        Map<String, Object> attributes = BeanUtil.toMap(userDetails, "identity", "password", "enabled",
                "accountNonExpired", "accountNonLocked", "credentialsNonExpired");
        AttributePrincipal principal = new AttributePrincipalImpl(name, attributes);
        return new AssertionImpl(principal, ticket.getCreateTime(), ticket.getExpiredTime(), ticket.getCreateTime(),
                Collections.emptyMap());
    }

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        String sessionId = event.getSession().getId();
        this.logger.info("The session({}) has been created.", sessionId);
    }

    @Override
    @WriteTransactional
    public void sessionDestroyed(HttpSessionEvent event) {
        // 此时sessionId已变化，根据sessionId获取TGT将无法与原始TGT匹配，只能从session中获取TGT属性值进行处理
        HttpSession session = event.getSession();
        String ticketGrantingTicket = getTicketGrantingTicket(session);
        if (ticketGrantingTicket != null) {
            session.removeAttribute(TGT_NAME);
            Collection<ServiceTicket> tickets = this.serviceTicketRepo
                    .deleteByTicketGrantingTicket(ticketGrantingTicket);
            tickets.forEach(ticket -> {
                this.logger.info("The service ticketId({}) has been deleted because session destroyed.",
                        ticket.getId());
            });
        }
    }

}
