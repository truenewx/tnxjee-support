package org.truenewx.tnxjeex.cas.client.userdetails;

import java.util.Collection;
import java.util.Map;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.truenewx.tnxjee.model.spec.user.DefaultUserIdentity;
import org.truenewx.tnxjee.model.spec.user.security.DefaultUserSpecificDetails;
import org.truenewx.tnxjee.model.spec.user.security.UserSpecificDetails;

/**
 * 默认的根据CasAssertion获取用户细节的服务
 */
public class DefaultCasAssertionUserDetailsService extends CasAssertionUserDetailsService {

    @Override
    @SuppressWarnings("unchecked")
    protected UserSpecificDetails<?> getUserSpecificDetails(AttributePrincipal principal,
            Map<String, Object> attributes) {
        DefaultUserIdentity userIdentity = DefaultUserIdentity.of(principal.getName());
        DefaultUserSpecificDetails userDetails = new DefaultUserSpecificDetails();
        userDetails.setIdentity(userIdentity);
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
