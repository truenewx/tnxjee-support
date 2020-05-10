package org.truenewx.tnxjeex.cas.server.authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.web.util.WebUtil;
import org.truenewx.tnxjee.web.view.util.WebViewUtil;
import org.truenewx.tnxjeex.cas.server.service.CasServiceManager;
import org.truenewx.tnxjeex.cas.server.ticket.TicketManager;

/**
 * CAS鉴权成功处理器
 */
public class CasAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private ServerProperties serverProperties;
    @Autowired
    private CasServiceManager serviceManager;
    @Autowired
    private TicketManager ticketManager;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        String ticketGrantingTicket = this.ticketManager.getTicketGrantingTicket(request);
        int maxAge = (int) this.serverProperties.getServlet().getSession().getTimeout().toSeconds();
        WebUtil.addCookie(request, response, "CASTGC", ticketGrantingTicket, maxAge);
        CasUserIdentityAuthenticationToken token = (CasUserIdentityAuthenticationToken) authentication;
        String service = token.getService();
        String targetUrl = this.serviceManager.getAuthenticatedTargetUrl(service);
        int index = targetUrl.indexOf(Strings.QUESTION);
        if (index < 0) {
            targetUrl += Strings.QUESTION;
        } else {
            targetUrl += Strings.AND;
        }
        targetUrl += "ticket=" + this.ticketManager.getServiceTicket(request, service);
        WebViewUtil.redirect(request, response, targetUrl);
    }

}
