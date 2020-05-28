package org.truenewx.tnxjeex.cas.server.service;

import org.apache.commons.lang3.StringUtils;

/**
 * CAS服务
 *
 * @author jianglei
 */
public class CasService {

    private String userType;
    private String host;
    private String loginUrl = "/login/cas";
    private String logoutUrl = "/logout";

    public String getUserType() {
        return this.userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getLoginUrl() {
        return this.loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getLogoutUrl() {
        return this.logoutUrl;
    }

    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }

    public String getFullLoginUrl() {
        return StringUtils.isBlank(this.host) ? this.loginUrl : (this.host + this.loginUrl);
    }

    public String getFullLogoutUrl() {
        return StringUtils.isBlank(this.host) ? this.logoutUrl : (this.host + this.logoutUrl);
    }

}
