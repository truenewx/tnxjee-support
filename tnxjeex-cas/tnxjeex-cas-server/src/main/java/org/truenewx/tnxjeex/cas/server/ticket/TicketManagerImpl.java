package org.truenewx.tnxjeex.cas.server.ticket;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Service;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.core.util.EncryptUtil;
import org.truenewx.tnxjee.core.util.LogUtil;
import org.truenewx.tnxjee.model.spec.user.security.UserSpecificDetails;
import org.truenewx.tnxjee.service.transaction.annotation.WriteTransactional;
import org.truenewx.tnxjee.web.security.util.SecurityUtil;
import org.truenewx.tnxjee.web.util.WebUtil;

/**
 * 票据管理器实现
 */
@Service
public class TicketManagerImpl implements TicketManager, HttpSessionListener {

    private static final String COOKIE_TGT = "CASTGC";

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
        String sessionId = request.getSession().getId();
        String ticketGrantingTicket = getTicketGrantingTicket(sessionId);
        int maxAge = (int) this.serverProperties.getServlet().getSession().getTimeout().toSeconds();
        WebUtil.addCookie(request, response, COOKIE_TGT, ticketGrantingTicket, maxAge);
    }

    private String getTicketGrantingTicket(String sessionId) {
        return TICKET_GRANTING_TICKET_PREFIX + EncryptUtil.encryptByMd5_16(sessionId);
    }

    @Override
    public boolean validateTicketGrantingTicket(HttpServletRequest request) {
        String ticketGrantingTicket = WebUtil.getCookieValue(request, COOKIE_TGT);
        String sessionId = request.getSession().getId();
        return StringUtils.isNotBlank(ticketGrantingTicket)
                && ticketGrantingTicket.equals(getTicketGrantingTicket(sessionId));
    }

    @Override
    public Map<String, String> deleteServiceTickets(HttpServletRequest request) {
        Map<String, String> mapping = new HashMap<>();
        String sessionId = request.getSession().getId();
        String ticketGrantingTicket = getTicketGrantingTicket(sessionId);
        List<ServiceTicket> tickets = this.serviceTicketRepo.deleteByTicketGrantingTicket(ticketGrantingTicket);
        tickets.forEach(ticket -> {
            mapping.put(ticket.getService(), ticket.getId());
        });
        return mapping;
    }

    // 用户登录或登出CAS服务器成功后调用，以获取目标服务的票据
    @Override
    @WriteTransactional
    public String getServiceTicket(HttpServletRequest request, String service, boolean create) {
        String sessionId = request.getSession().getId();
        String ticketGrantingTicket = getTicketGrantingTicket(sessionId);
        ServiceTicket ticket = this.serviceTicketRepo.findByTicketGrantingTicketAndService(ticketGrantingTicket, service);
        if (ticket != null && ticket.getExpiredTime().before(new Date())) { // 已过期的先删除，再视为null
            this.serviceTicketRepo.deleteById(ticket.getId());
            ticket = null;
        }
        if (ticket == null && create) { // 不存在且需要创建则创建新的
            Date now = new Date();
            String text = sessionId + Strings.MINUS + service + Strings.MINUS + now.getTime();
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
        return ticket == null ? null : ticket.getId();
    }

    // 用户访问业务服务，由业务服务校验票据有效性时调用
    @Override
    public Assertion validateServiceTicket(String service, String ticketId) {
        ServiceTicket ticket = this.serviceTicketRepo.findById(ticketId);
        if (ticket == null || !ticket.getService().equals(service)) {
            return null;
        }
        UserSpecificDetails<?> userDetails = ticket.getUserDetails();
        AttributePrincipal principal = new AttributePrincipalImpl(userDetails.getIdentity().toString());
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("username", userDetails.getUsername());
        attributes.put("caption", userDetails.getCaption());
        attributes.put("authorities", userDetails.getAuthorities());
        return new AssertionImpl(principal, ticket.getCreateTime(), ticket.getExpiredTime(),
                ticket.getCreateTime(), attributes);
    }

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        String sessionId = event.getSession().getId();
        LogUtil.info(getClass(), "The session({}) has been created.", sessionId);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        String sessionId = event.getSession().getId();
        String ticketGrantingTicket = getTicketGrantingTicket(sessionId);
        List<ServiceTicket> tickets = this.serviceTicketRepo.deleteByTicketGrantingTicket(ticketGrantingTicket);
        tickets.forEach(ticket -> {
            LogUtil.info(getClass(),
                    "The service ticket({}) has been deleted because session({}) destroyed.",
                    ticket.getId(), sessionId);
        });
    }

}
