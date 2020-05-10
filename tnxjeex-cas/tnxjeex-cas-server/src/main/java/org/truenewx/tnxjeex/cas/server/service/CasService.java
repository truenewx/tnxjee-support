package org.truenewx.tnxjeex.cas.server.service;

/**
 * CAS服务
 *
 * @author jianglei
 */
public class CasService {

    private String userType;
    private String targetUrl;

    public String getUserType() {
        return this.userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getTargetUrl() {
        return this.targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

}
