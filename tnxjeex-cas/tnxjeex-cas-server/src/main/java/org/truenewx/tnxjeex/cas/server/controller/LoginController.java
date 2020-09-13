package org.truenewx.tnxjeex.cas.server.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.truenewx.tnxjee.web.util.WebConstants;
import org.truenewx.tnxjeex.cas.server.service.CasServiceManager;
import org.truenewx.tnxjeex.cas.server.ticket.TicketManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录控制器
 */
@Controller
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private CasServiceManager serviceManager;
    @Autowired
    private TicketManager ticketManager;
    @Autowired
    private RedirectStrategy redirectStrategy;

    @GetMapping("/form")
    public ModelAndView form(@RequestParam("service") String service,
                             HttpServletRequest request, HttpServletResponse response) {
        if (this.ticketManager.validateTicketGrantingTicket(request, service)) {
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

    @GetMapping("/ajax")
    @ResponseBody
    public String ajax(@RequestParam("service") String service,
                       HttpServletRequest request, HttpServletResponse response) throws IOException {
        String originalRequest = request.getHeader(WebConstants.HEADER_ORIGINAL_REQUEST);
        if (originalRequest != null) {
            response.setHeader(WebConstants.HEADER_ORIGINAL_REQUEST, originalRequest);
        }
        if (this.ticketManager.validateTicketGrantingTicket(request, service)) {
            String targetUrl = this.serviceManager.getLoginUrl(request, service);
            this.redirectStrategy.sendRedirect(request, response, targetUrl);
        } else { // AJAX登录只能进行自动登录，否则报401
            String userType = this.serviceManager.getUserType(service);
            if (StringUtils.isNotBlank(userType)) { // 指定了用户类型的服务，才能取得对应的登录表单地址
                String url = request.getRequestURL().toString();
                url = url.replaceFirst("/login/ajax", "/login/form");
                url += "?service=" + service;
                response.setHeader(WebConstants.HEADER_LOGIN_URL, url);
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return null;
    }


}
