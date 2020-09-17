package org.truenewx.tnxjeex.cas.server.authentication;

import java.io.Serializable;

public class CasServiceAuthenticationDetails implements Serializable {

    private static final long serialVersionUID = -8966249391535990582L;

    private String service;
    private String scope;

    public CasServiceAuthenticationDetails(String service, String scope) {
        this.service = service;
        this.scope = scope;
    }

    public String getService() {
        return this.service;
    }

    public String getScope() {
        return this.scope;
    }

}
