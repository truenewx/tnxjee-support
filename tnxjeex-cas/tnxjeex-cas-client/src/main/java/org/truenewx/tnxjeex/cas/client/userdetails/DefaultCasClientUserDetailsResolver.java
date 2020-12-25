package org.truenewx.tnxjeex.cas.client.userdetails;

import java.util.Collection;
import java.util.Map;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.truenewx.tnxjee.model.spec.user.DefaultUserIdentity;
import org.truenewx.tnxjee.model.spec.user.security.DefaultUserSpecificDetails;

/**
 * 默认的Cas客户端用户细节解决器
 */
public class DefaultCasClientUserDetailsResolver implements CasClientUserDetailsResolver {

    @Override
    @SuppressWarnings("unchecked")
    public UserDetails resolveUserDetails(AttributePrincipal principal) {
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
    }

}
