package org.truenewx.tnxjeex.cas.server.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.core.util.NetUtil;
import org.truenewx.tnxjee.webmvc.api.meta.model.ApiMetaProperties;
import org.truenewx.tnxjee.webmvc.security.web.authentication.ResolvableExceptionAuthenticationFailureHandler;
import org.truenewx.tnxjee.webmvc.util.WebMvcConstants;
import org.truenewx.tnxjee.webmvc.util.WebMvcUtil;
import org.truenewx.tnxjeex.cas.server.service.CasServiceManager;
import org.truenewx.tnxjeex.cas.server.ticket.TicketManager;

/**
 * 登录控制器
 */
@Controller
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private ResolvableExceptionAuthenticationFailureHandler authenticationFailureHandler;
    @Autowired
    private CasServiceManager serviceManager;
    @Autowired
    private TicketManager ticketManager;
    @Autowired
    private RedirectStrategy redirectStrategy;
    @Autowired
    private ApiMetaProperties apiMetaProperties;

    @GetMapping
    public ModelAndView form(@RequestParam("service") String service, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        if (WebMvcUtil.isAjaxRequest(request)) {
            String originalRequest = request.getHeader(WebMvcConstants.HEADER_ORIGINAL_REQUEST);
            if (originalRequest != null) {
                response.setHeader(WebMvcConstants.HEADER_ORIGINAL_REQUEST, originalRequest);
            }
            if (this.ticketManager.validateTicketGrantingTicket(request)) {
                String targetUrl = this.serviceManager.getLoginUrl(request, service);
                if (originalRequest != null) {
                    String originalUrl = originalRequest
                            .substring(originalRequest.indexOf(Strings.SPACE) + 1);
                    targetUrl = NetUtil.mergeParam(targetUrl,
                            this.apiMetaProperties.getLoginSuccessRedirectParameter(), originalUrl);
                }
                this.redirectStrategy.sendRedirect(request, response, targetUrl);
            } else { // AJAX登录只能进行自动登录，否则报401
                String url = request.getRequestURL().toString();
                url += "?service=" + service;
                response.setHeader(WebMvcConstants.HEADER_LOGIN_URL, url);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            return null;
        } else {
            if (this.ticketManager.validateTicketGrantingTicket(request)) {
                String targetUrl = this.serviceManager.getLoginUrl(request, service);
                return new ModelAndView("redirect:" + targetUrl);
            }
            String result = this.authenticationFailureHandler.getTargetUrlFunction().apply(request);
            if (result == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return null;
            }
            return new ModelAndView(result);
        }
    }

}
