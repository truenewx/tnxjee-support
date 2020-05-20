package org.truenewx.tnxjeex.cas.server.authentication;

import java.io.Serializable;

public class CasServiceAuthenticationDetails implements Serializable {

    private static final long serialVersionUID = -8966249391535990582L;
    
    private String service;

    public CasServiceAuthenticationDetails(String service) {
        this.service = service;
    }

    public String getService() {
        return this.service;
    }

}
