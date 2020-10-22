package org.truenewx.tnxjeex.openapi.client.web.security.authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.truenewx.tnxjee.webmvc.security.web.authentication.LoginProcessingFilterSupport;

/**
 * 微信登录进程过滤器
 */
public class WechatLoginProcessingFilter extends LoginProcessingFilterSupport {

    public WechatLoginProcessingFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        return null;
    }

}
