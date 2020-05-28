package org.truenewx.tnxjeex.cas.server.authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.truenewx.tnxjee.web.view.util.WebViewUtil;
import org.truenewx.tnxjeex.cas.server.service.CasServiceManager;
import org.truenewx.tnxjeex.cas.server.ticket.TicketManager;

/**
 * CAS鉴权成功处理器
 */
public class CasAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private CasServiceManager serviceManager;
    @Autowired
    private TicketManager ticketManager;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        this.ticketManager.createTicketGrantingTicket(request, response);
        CasUserSpecificDetailsAuthenticationToken token = (CasUserSpecificDetailsAuthenticationToken) authentication;
        String service = token.getService();
        String targetUrl = this.serviceManager.getLoginUrl(request, service);
        // 此处一定是表单提交鉴权成功，无需AjaxRedirectStrategy
        WebViewUtil.redirect(request, response, targetUrl);
    }

}
