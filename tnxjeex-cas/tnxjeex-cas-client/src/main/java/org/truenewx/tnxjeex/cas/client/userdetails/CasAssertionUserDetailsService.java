package org.truenewx.tnxjeex.cas.client.userdetails;

import java.util.Map;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.springframework.security.cas.userdetails.AbstractCasAssertionUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.truenewx.tnxjee.model.spec.user.security.UserSpecificDetails;
import org.truenewx.tnxjee.web.security.core.AuthenticationFailureException;

/**
 * 根据CasAssertion获取用户细节的抽象服务
 */
public abstract class CasAssertionUserDetailsService extends AbstractCasAssertionUserDetailsService {

    @Override
    protected UserDetails loadUserDetails(Assertion assertion) {
        if (assertion != null && assertion.isValid()) {
            AttributePrincipal principal = assertion.getPrincipal();
            Map<String, Object> attributes = assertion.getAttributes();
            return getUserSpecificDetails(principal, attributes);
        }
        throw new AuthenticationFailureException();
    }

    protected abstract UserSpecificDetails<?> getUserSpecificDetails(AttributePrincipal principal,
            Map<String, Object> attributes);

}
