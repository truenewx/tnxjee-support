package org.truenewx.tnxjeex.cas.client.userdetails;

import org.jasig.cas.client.validation.Assertion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.cas.userdetails.AbstractCasAssertionUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.webmvc.security.core.BusinessAuthenticationException;

/**
 * 默认的根据CasAssertion获取用户细节的服务
 */
@Component
public class DefaultCasAssertionUserDetailsService extends AbstractCasAssertionUserDetailsService {

    private UserDetailsResolver userDetailsResolver = new DefaultUserDetailsResolver();

    @Autowired(required = false)
    public void setUserDetailsResolver(UserDetailsResolver userDetailsResolver) {
        this.userDetailsResolver = userDetailsResolver;
    }

    @Override
    protected UserDetails loadUserDetails(Assertion assertion) {
        if (assertion != null && assertion.isValid()) {
            return this.userDetailsResolver.resolveUserDetails(assertion.getPrincipal());
        }
        throw new BusinessAuthenticationException("error.service.security.authentication_failure");
    }

}
