package org.truenewx.tnxjeex.cas.server.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.core.util.NetUtil;
import org.truenewx.tnxjee.web.util.WebConstants;
import org.truenewx.tnxjee.web.util.WebUtil;
import org.truenewx.tnxjee.webmvc.api.meta.model.ApiMetaProperties;
import org.truenewx.tnxjee.webmvc.security.web.authentication.ResolvableExceptionAuthenticationFailureHandler;
import org.truenewx.tnxjeex.cas.server.service.CasServiceManager;
import org.truenewx.tnxjeex.cas.server.ticket.TicketManager;
import org.truenewx.tnxjeex.cas.server.util.CasServerConstants;

/**
 * 登录控制器
 */
@RequestMapping("/login")
public abstract class CasLoginControllerSupport {

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
    public ModelAndView get(@RequestParam(value = "service", required = false) String service,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (StringUtils.isBlank(service)) {
            service = getDefaultService();
            if (StringUtils.isBlank(service)) {
                return toBadServiceView(response);
            }
            request.setAttribute(CasServerConstants.PARAMETER_SERVICE, service);
        }
        String redirectParameter = this.apiMetaProperties.getLoginSuccessRedirectParameter();
        if (WebUtil.isAjaxRequest(request)) {
            String originalRequest = request.getHeader(WebConstants.HEADER_ORIGINAL_REQUEST);
            if (originalRequest != null) {
                response.setHeader(WebConstants.HEADER_ORIGINAL_REQUEST, originalRequest);
            }
            if (this.ticketManager.checkTicketGrantingTicket(request)) {
                String targetUrl = this.serviceManager.getLoginProcessUrl(request, service);
                if (originalRequest != null) {
                    String originalUrl = originalRequest.substring(originalRequest.indexOf(Strings.SPACE) + 1);
                    targetUrl = NetUtil.mergeParam(targetUrl, redirectParameter, originalUrl);
                }
                this.redirectStrategy.sendRedirect(request, response, targetUrl);
            } else { // AJAX登录只能进行自动登录，否则报401
                String url = request.getRequestURL().toString();
                url += "?service=" + service;
                response.setHeader(WebConstants.HEADER_LOGIN_URL, url);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            return null;
        } else {
            if (this.ticketManager.checkTicketGrantingTicket(request)) {
                String targetUrl = this.serviceManager.getLoginProcessUrl(request, service);
                String redirectUrl = request.getParameter(redirectParameter);
                if (StringUtils.isNotBlank(redirectUrl)) {
                    targetUrl = NetUtil.mergeParam(targetUrl, redirectParameter, redirectUrl);
                }
                this.redirectStrategy.sendRedirect(request, response, targetUrl);
                return null;
            }
            String result = this.authenticationFailureHandler.getTargetUrlFunction().apply(request);
            if (result == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return null;
            }
            return new ModelAndView(result);
        }
    }

    protected ModelAndView toBadServiceView(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required String parameter 'service' is not present.");
        return null;
    }

    protected String getDefaultService() {
        return null;
    }

}
