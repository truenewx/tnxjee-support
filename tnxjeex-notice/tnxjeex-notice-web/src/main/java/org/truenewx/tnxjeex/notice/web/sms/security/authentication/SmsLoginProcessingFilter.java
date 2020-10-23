package org.truenewx.tnxjeex.notice.web.sms.security.authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.truenewx.tnxjee.webmvc.security.web.authentication.AbstractLoginProcessingFilter;

/**
 * 短信登录进程过滤器
 */
public class SmsLoginProcessingFilter extends AbstractLoginProcessingFilter {

    public SmsLoginProcessingFilter(String defaultFilterProcessesUrl, ApplicationContext context) {
        super(defaultFilterProcessesUrl, context);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        return null;
    }

}
