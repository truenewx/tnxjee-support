package org.truenewx.tnxjeex.cas.server.ticket;

import java.util.Collections;
import java.util.Date;
import java.util.List;

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
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.core.util.EncryptUtil;
import org.truenewx.tnxjee.core.util.LogUtil;
import org.truenewx.tnxjee.model.spec.user.UserIdentity;
import org.truenewx.tnxjee.web.security.util.SecurityUtil;
import org.truenewx.tnxjee.web.util.WebUtil;

/**
 * 票据管理器实现
 */
@Component
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

    // 用户从浏览器登录CAS服务器成功后调用，以获取目标服务的票据
    @Override
    public String getServiceTicket(HttpServletRequest request, String service) {
        String sessionId = request.getSession().getId();
        String ticketGrantingTicket = getTicketGrantingTicket(sessionId);
        ServiceTicket ticket = this.serviceTicketRepo.findByTicketGrantingTicketAndService(ticketGrantingTicket, service);
        Date now = new Date();
        if (ticket != null && ticket.getExpiredTime().before(now)) { // 已过期的先删除，再视为null
            this.serviceTicketRepo.deleteById(ticket.getId());
            ticket = null;
        }
        if (ticket == null) { // 不存在则创建新的
            String text = sessionId + Strings.MINUS + service + Strings.MINUS + now.getTime();
            String ticketId = SERVICE_TICKET_PREFIX + EncryptUtil.encryptByMd5_16(text);
            UserIdentity<?> userIdentity = SecurityUtil.getAuthorizedUserIdentity();
            long timeout = this.serverProperties.getServlet().getSession().getTimeout().toMillis();
            Date expiredTime = new Date(now.getTime() + timeout);
            ticket = new ServiceTicket(ticketId);
            ticket.setTicketGrantingTicket(ticketGrantingTicket);
            ticket.setService(service);
            ticket.setUserIdentity(userIdentity);
            ticket.setCreateTime(now);
            ticket.setExpiredTime(expiredTime);
            this.serviceTicketRepo.save(ticket);
        }
        return ticket.getId();
    }

    // 用户访问业务服务，由业务服务校验票据有效性时调用
    @Override
    public Assertion validateServiceTicket(String service, String ticketId) {
        ServiceTicket ticket = this.serviceTicketRepo.findById(ticketId);
        if (ticket == null || !ticket.getService().equals(service)) {
            return null;
        }
        String userIdentity = ticket.getUserIdentity().toString();
        AttributePrincipal principal = new AttributePrincipalImpl(userIdentity);
        return new AssertionImpl(principal, ticket.getCreateTime(), ticket.getExpiredTime(),
                ticket.getCreateTime(), Collections.emptyMap());
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
        List<String> ticketIds = this.serviceTicketRepo.deleteByTicketGrantingTicket(ticketGrantingTicket);
        ticketIds.forEach(ticketId -> {
            LogUtil.info(getClass(),
                    "The service ticket({}) has been deleted because session({}) destroyed.",
                    ticketId, sessionId);
        });
    }

}
