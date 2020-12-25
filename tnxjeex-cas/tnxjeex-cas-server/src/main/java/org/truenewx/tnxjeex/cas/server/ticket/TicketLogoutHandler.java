package org.truenewx.tnxjeex.cas.server.ticket;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.web.util.WebUtil;
import org.truenewx.tnxjeex.cas.server.entity.ServiceTicket;
import org.truenewx.tnxjeex.cas.server.util.CasServerConstants;

/**
 * 票据登出处理器
 */
@Component
public class TicketLogoutHandler implements LogoutHandler {

    /**
     * 登出服务Cookie值的分隔符
     */
    public static final String LOGOUT_SERVICES_COOKIE_VALUE_SEPARATOR = "|";

    @Autowired
    private TicketManager ticketManager;
    @Value("${spring.application.name}")
    private String currentService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) {
        Collection<ServiceTicket> serviceTickets = this.ticketManager.deleteTicketGrantingTicket(request, response);
        if (serviceTickets.size() > 0) {
            String logoutService = request.getParameter(CasServerConstants.PARAMETER_SERVICE);
            if (logoutService == null) {
                logoutService = this.currentService;
            }
            StringBuilder services = new StringBuilder();
            for (ServiceTicket ticket : serviceTickets) {
                String service = ticket.getService();
                // 尽量排除当前登出服务，以尽量缩短Cookie值长度
                if (logoutService == null || !logoutService.equals(service)) {
                    services.append(LOGOUT_SERVICES_COOKIE_VALUE_SEPARATOR)
                            .append(service); // Cookie中不允许使用[ ] ( ) = , " / ? @ : ;
                }
            }
            if (services.length() > 0) {
                services.deleteCharAt(0);
                WebUtil.addCookie(request, response, CasServerConstants.COOKIE_LOGOUT_SERVICES, services.toString(),
                        10);
            }
        }
    }
}
