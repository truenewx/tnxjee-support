package org.truenewx.tnxjeex.cas.server.controller;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.validation.Assertion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.truenewx.tnxjee.web.security.config.annotation.ConfigAnonymous;
import org.truenewx.tnxjee.web.util.WebConstants;
import org.truenewx.tnxjeex.cas.server.service.CasServiceManager;
import org.truenewx.tnxjeex.cas.server.ticket.TicketManager;
import org.truenewx.tnxjeex.cas.server.util.CasServerConstants;

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
            HttpServletRequest request, HttpServletResponse response) {
        if (this.ticketManager.validateTicketGrantingTicket(request)) {
            String targetUrl = this.serviceManager.getLoginUrl(request, service);
            return new ModelAndView("redirect:" + targetUrl);
        }
        String userType = this.serviceManager.getUserType(service);
        if (StringUtils.isBlank(userType)) { // 未指定用户类型的服务，只能在用户已登录后进行自动登录，否则报401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        return new ModelAndView("/login/" + userType.toLowerCase());
    }

    @GetMapping("/login/ajax")
    @ResponseBody
    public String loginAjax(@RequestParam("service") String service,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        String originalRequest = request.getHeader(WebConstants.HEADER_ORIGINAL_REQUEST);
        if (originalRequest != null) {
            response.setHeader(WebConstants.HEADER_ORIGINAL_REQUEST, originalRequest);
        }
        if (this.ticketManager.validateTicketGrantingTicket(request)) {
            String targetUrl = this.serviceManager.getLoginUrl(request, service);
            this.redirectStrategy.sendRedirect(request, response, targetUrl);
        } else { // AJAX登录只能进行自动登录，否则报401
            String url = request.getRequestURL().toString();
            url = url.replaceFirst("/login/ajax", "/login/form");
            url += "?service=" + service;
            response.setHeader(CasServerConstants.HEADER_LOGIN_FORM_URL, url);
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

    @GetMapping("/serviceLogoutUrls")
    @ConfigAnonymous
    @ResponseBody
    public Map<String, String> serviceLogoutUrls(@RequestParam("service") String service) {
        return this.serviceManager.getLogoutUrls(service);
    }

}
