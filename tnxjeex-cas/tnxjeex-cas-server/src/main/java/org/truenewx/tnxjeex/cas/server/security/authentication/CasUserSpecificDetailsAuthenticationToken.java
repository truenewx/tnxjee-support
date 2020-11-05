package org.truenewx.tnxjeex.cas.server.security.authentication;

import org.truenewx.tnxjee.model.spec.user.security.UserSpecificDetails;
import org.truenewx.tnxjee.webmvc.security.authentication.UserSpecificDetailsAuthenticationToken;

/**
 * CAS用户特性细节鉴权令牌
 */
public class CasUserSpecificDetailsAuthenticationToken extends UserSpecificDetailsAuthenticationToken {

    private static final long serialVersionUID = -2997803056699252908L;

    private final String service;

    public CasUserSpecificDetailsAuthenticationToken(String service, UserSpecificDetails<?> details, String ip) {
        super(details);
        this.service = service;
        setIp(ip);
    }

    public String getService() {
        return this.service;
    }

}
