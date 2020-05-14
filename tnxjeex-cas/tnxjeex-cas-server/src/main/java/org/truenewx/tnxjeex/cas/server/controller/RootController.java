package org.truenewx.tnxjeex.cas.server.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.validation.Assertion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.truenewx.tnxjee.web.security.config.annotation.ConfigAnonymous;
import org.truenewx.tnxjeex.cas.server.service.CasServiceManager;
import org.truenewx.tnxjeex.cas.server.ticket.TicketManager;

/**
 * 根控制器
 *
 * @author jianglei
 */
@Controller
public class RootController {

    @Autowired
    private CasServiceManager serviceManager;
    @Autowired
    private TicketManager ticketManager;
    @Autowired
    private RedirectStrategy redirectStrategy;

    @GetMapping("/login/form")
    public ModelAndView loginForm(@RequestParam("service") String service,
            HttpServletRequest request) {
        if (this.ticketManager.validateTicketGrantingTicket(request)) {
            String targetUrl = this.serviceManager.getAuthenticatedTargetUrl(request, service);
            return new ModelAndView("redirect:" + targetUrl);
        }
        String userType = this.serviceManager.resolveUserType(service);
        ModelAndView mav = new ModelAndView("/login/" + userType.toLowerCase());
        mav.addObject("service", service);
        return mav;
    }

    @GetMapping("/login/ajax")
    @ResponseBody
    public String loginAjax(@RequestParam("service") String service,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (this.ticketManager.validateTicketGrantingTicket(request)) {
            String targetUrl = this.serviceManager.getAuthenticatedTargetUrl(request, service);
            this.redirectStrategy.sendRedirect(request, response, targetUrl);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return null;
    }

    @GetMapping("/serviceValidate")
    @ConfigAnonymous
    @ResponseBody
    public Assertion serviceValidate(@RequestParam("service") String service,
            @RequestParam("ticket") String ticket) {
        return this.ticketManager.validateServiceTicket(service, ticket);
    }

}
