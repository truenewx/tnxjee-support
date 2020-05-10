package org.truenewx.tnxjeex.cas.client.authentication;

import java.util.Collections;
import java.util.Map;

import org.jasig.cas.client.authentication.AttributePrincipal;

public class SimpleAttributePrincipal implements AttributePrincipal {

    private static final long serialVersionUID = 950876983210601636L;

    private String name;

    @Override
    public String getProxyTicketFor(String service) {
        return null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Collections.emptyMap();
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
