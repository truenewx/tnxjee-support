package org.truenewx.tnxjeex.cas.client.userdetails;

import java.util.Collection;
import java.util.Map;

import org.jasig.cas.client.validation.Assertion;
import org.springframework.security.cas.userdetails.AbstractCasAssertionUserDetailsService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.model.spec.user.DefaultUserIdentity;
import org.truenewx.tnxjee.model.spec.user.security.DefaultUserSpecificDetails;
import org.truenewx.tnxjee.web.security.core.AuthenticationFailureException;

@Component
public class DefaultCasAssertionUserDetailsService extends AbstractCasAssertionUserDetailsService {

    @Override
    @SuppressWarnings("unchecked")
    protected UserDetails loadUserDetails(Assertion assertion) {
        if (assertion != null && assertion.isValid()) {
            String principal = assertion.getPrincipal().getName();
            DefaultUserIdentity userIdentity = DefaultUserIdentity.of(principal);
            if (userIdentity != null) {
                DefaultUserSpecificDetails userDetails = new DefaultUserSpecificDetails();
                userDetails.setIdentity(userIdentity);
                Map<String, Object> attributes = assertion.getAttributes();
                userDetails.setUsername((String) attributes.get("username"));
                userDetails.setCaption((String) attributes.get("caption"));
                userDetails.setAuthorities((Collection<? extends GrantedAuthority>) attributes.get("authorities"));
                userDetails.setEnabled(true);
                userDetails.setAccountNonExpired(true);
                userDetails.setAccountNonLocked(true);
                userDetails.setCredentialsNonExpired(true);
                return userDetails;
            }
        }
        throw new AuthenticationFailureException();
    }

}
