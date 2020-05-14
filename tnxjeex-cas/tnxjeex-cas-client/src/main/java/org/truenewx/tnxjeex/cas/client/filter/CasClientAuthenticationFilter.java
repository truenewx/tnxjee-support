package org.truenewx.tnxjeex.cas.client.filter;

import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

/**
 * CAS客户端鉴权过滤器
 */
public class CasClientAuthenticationFilter extends CasAuthenticationFilter {

    public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
        if (redirectStrategy != null) {
            AuthenticationSuccessHandler successHandler = getSuccessHandler();
            if (successHandler instanceof AbstractAuthenticationTargetUrlRequestHandler) {
                ((AbstractAuthenticationTargetUrlRequestHandler) successHandler).setRedirectStrategy(redirectStrategy);
            }
            AuthenticationFailureHandler failureHandler = getFailureHandler();
            if (failureHandler instanceof SimpleUrlAuthenticationFailureHandler) {
                ((SimpleUrlAuthenticationFailureHandler) failureHandler).setRedirectStrategy(redirectStrategy);
            }
        }
    }

    public void setSuccessTargetUrlParameter(String targetUrlParameter) {
        AuthenticationSuccessHandler successHandler = getSuccessHandler();
        if (successHandler instanceof AbstractAuthenticationTargetUrlRequestHandler) {
            ((AbstractAuthenticationTargetUrlRequestHandler) successHandler).setTargetUrlParameter(targetUrlParameter);
        }
    }

}
