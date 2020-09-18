package org.truenewx.tnxjeex.cas.client.userdetails;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.springframework.security.cas.userdetails.AbstractCasAssertionUserDetailsService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.model.spec.user.DefaultUserIdentity;
import org.truenewx.tnxjee.model.spec.user.security.DefaultUserSpecificDetails;
import org.truenewx.tnxjee.model.spec.user.security.UserSpecificDetails;
import org.truenewx.tnxjee.web.security.core.AuthenticationFailureException;

/**
 * 默认的根据CasAssertion获取用户细节的服务
 */
@Component
public class DefaultCasAssertionUserDetailsService extends AbstractCasAssertionUserDetailsService {

    @SuppressWarnings("unchecked")
    private Function<AttributePrincipal, UserSpecificDetails<?>> userSpecificDetailsFunction = principal -> {
        DefaultUserIdentity userIdentity = DefaultUserIdentity.of(principal.getName());
        DefaultUserSpecificDetails details = new DefaultUserSpecificDetails();
        details.setIdentity(userIdentity);
        Map<String, Object> attributes = principal.getAttributes();
        details.setUsername((String) attributes.get("username"));
        details.setCaption((String) attributes.get("caption"));
        details.setAuthorities((Collection<? extends GrantedAuthority>) attributes.get("authorities"));
        details.setEnabled(true);
        details.setAccountNonExpired(true);
        details.setAccountNonLocked(true);
        details.setCredentialsNonExpired(true);
        return details;
    };

    public void setUserSpecificDetailsFunction(
            Function<AttributePrincipal, UserSpecificDetails<?>> userSpecificDetailsFunction) {
        this.userSpecificDetailsFunction = userSpecificDetailsFunction;
    }

    @Override
    protected UserDetails loadUserDetails(Assertion assertion) {
        if (assertion != null && assertion.isValid()) {
            return this.userSpecificDetailsFunction.apply(assertion.getPrincipal());
        }
        throw new AuthenticationFailureException();
    }

}
