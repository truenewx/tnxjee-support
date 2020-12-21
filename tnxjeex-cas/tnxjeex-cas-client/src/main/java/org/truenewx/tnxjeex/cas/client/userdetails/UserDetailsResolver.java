package org.truenewx.tnxjeex.cas.client.userdetails;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 用户细节解决器
 */
public interface UserDetailsResolver {

    UserDetails resolveUserDetails(AttributePrincipal principal);

}
