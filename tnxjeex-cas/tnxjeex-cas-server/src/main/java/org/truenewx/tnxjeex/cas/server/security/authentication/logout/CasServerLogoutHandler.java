package org.truenewx.tnxjeex.cas.server.security.authentication.logout;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.core.util.NetUtil;
import org.truenewx.tnxjeex.cas.server.entity.ServiceTicket;
import org.truenewx.tnxjeex.cas.server.service.CasServiceManager;
import org.truenewx.tnxjeex.cas.server.ticket.CasTicketManager;
import org.truenewx.tnxjeex.cas.server.util.CasServerConstants;

/**
 * CAS服务端登出处理器
 */
@Component
public class CasServerLogoutHandler implements LogoutHandler {

    @Autowired
    private CasTicketManager ticketManager;
    @Autowired
    private CasServiceManager serviceManager;
    @Value("${spring.application.name}")
    private String currentService;
    @Autowired
    private Executor executor;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Collection<ServiceTicket> serviceTickets = this.ticketManager.deleteTicketGrantingTicket(request, response);
        if (serviceTickets.size() > 0) {
            String logoutService = request.getParameter(CasServerConstants.PARAMETER_SERVICE);
            if (logoutService == null) {
                logoutService = this.currentService;
            }
            for (ServiceTicket ticket : serviceTickets) {
                String service = ticket.getService();
                if (logoutService == null || !logoutService.equals(service)) {
                    String logoutProcessUrl = this.serviceManager.getLogoutProcessUrl(service);
                    if (logoutProcessUrl != null) {
                        this.executor.execute(() -> {
                            Map<String, Object> params = new HashMap<>();
                            params.put("logoutRequest", "<SessionIndex>" + ticket.getId() + "</SessionIndex>");
                            NetUtil.requestByGet(logoutProcessUrl, params, StandardCharsets.UTF_8.name());
                        });
                    }
                }
            }
        }
    }
}
