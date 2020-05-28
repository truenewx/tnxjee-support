package org.truenewx.tnxjeex.cas.client.validation;

import java.util.Date;
import java.util.Map;

import org.jasig.cas.client.validation.Assertion;
import org.truenewx.tnxjeex.cas.client.authentication.SimpleAttributePrincipal;

/**
 * 便于序列化和反序列化的简单Assertion实现
 */
public class SimpleAssertion implements Assertion {

    private static final long serialVersionUID = -5843298720172335725L;

    private Date validFromDate;
    private Date validUntilDate;
    private Date authenticationDate;
    private Map<String, Object> attributes;
    private SimpleAttributePrincipal principal;
    private boolean valid;

    @Override
    public Date getValidFromDate() {
        return this.validFromDate;
    }

    public void setValidFromDate(Date validFromDate) {
        this.validFromDate = validFromDate;
    }

    @Override
    public Date getValidUntilDate() {
        return this.validUntilDate;
    }

    public void setValidUntilDate(Date validUntilDate) {
        this.validUntilDate = validUntilDate;
    }

    @Override
    public Date getAuthenticationDate() {
        return this.authenticationDate;
    }

    public void setAuthenticationDate(Date authenticationDate) {
        this.authenticationDate = authenticationDate;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public SimpleAttributePrincipal getPrincipal() {
        return this.principal;
    }

    public void setPrincipal(SimpleAttributePrincipal principal) {
        this.principal = principal;
    }

    @Override
    public boolean isValid() {
        return this.valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
