package org.truenewx.tnxjeex.cas.server.authentication;

import org.truenewx.tnxjee.model.spec.user.security.UserSpecificDetails;
import org.truenewx.tnxjee.web.security.authentication.UserSpecificDetailsAuthenticationToken;

/**
 * CAS用户特性细节鉴权令牌
 */
public class CasUserSpecificDetailsAuthenticationToken
        extends UserSpecificDetailsAuthenticationToken {

    private static final long serialVersionUID = -2997803056699252908L;

    private final String service;

    public CasUserSpecificDetailsAuthenticationToken(String service,
            UserSpecificDetails<?> details) {
        super(details);
        this.service = service;
    }

    public String getService() {
        return this.service;
    }

}
