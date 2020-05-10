package org.truenewx.tnxjeex.cas.server.authentication;

import org.truenewx.tnxjee.model.spec.user.UserIdentity;
import org.truenewx.tnxjee.web.security.authentication.UserIdentityAuthenticationToken;

/**
 * CAS用户标识鉴权令牌
 */
public class CasUserIdentityAuthenticationToken extends UserIdentityAuthenticationToken {

    private static final long serialVersionUID = -2997803056699252908L;

    private String service;

    public CasUserIdentityAuthenticationToken(String service, UserIdentity<?> userIdentity) {
        super(userIdentity);
        this.service = service;
    }

    public String getService() {
        return this.service;
    }

}
