package org.truenewx.tnxjeex.cas.server.ticket;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.webmvc.util.WebMvcUtil;
import org.truenewx.tnxjeex.cas.server.util.CasServerConstants;

/**
 * 票据登出处理器
 */
@Component
public class TicketLogoutHandler implements LogoutHandler {

    @Autowired
    private TicketManager ticketManager;
    @Value("${spring.application.name}")
    private String currentService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) {
        Collection<ServiceTicket> serviceTickets = this.ticketManager.findServiceTickets(request);
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
                    services.append(Strings.SPACE).append(service); // Cookie不支持逗号，用空格作为分隔符
                }
            }
            if (services.length() > 0) {
                services.deleteCharAt(0);
                WebMvcUtil.addCookie(request, response, CasServerConstants.COOKIE_LOGOUT_SERVICES,
                        services.toString(), 30);
            }
        }
    }
}
