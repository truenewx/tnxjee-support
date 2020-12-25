package org.truenewx.tnxjeex.cas.client.userdetails;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Cas客户端用户细节解决器
 */
public interface CasClientUserDetailsResolver {

    UserDetails resolveUserDetails(AttributePrincipal principal);

}
