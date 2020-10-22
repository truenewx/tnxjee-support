package org.truenewx.tnxjeex.notice.web.sms.security.authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.truenewx.tnxjee.webmvc.security.web.authentication.LoginProcessingFilterSupport;

/**
 * 短信登录进程过滤器
 */
public class SmsLoginProcessingFilter extends LoginProcessingFilterSupport {

    public SmsLoginProcessingFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        return null;
    }

}
