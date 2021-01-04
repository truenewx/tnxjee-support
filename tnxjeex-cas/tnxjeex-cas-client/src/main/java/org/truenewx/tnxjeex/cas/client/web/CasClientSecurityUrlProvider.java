package org.truenewx.tnxjeex.cas.client.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.truenewx.tnxjee.core.util.NetUtil;
import org.truenewx.tnxjee.webmvc.security.web.SecurityUrlProvider;
import org.truenewx.tnxjeex.cas.client.config.CasClientProperties;

/**
 * Cas客户端的安全地址提供者
 */
public class CasClientSecurityUrlProvider implements SecurityUrlProvider {

    @Autowired
    private CasClientProperties casClientProperties;

    @Override
    public String getDefaultLoginFormUrl() {
        return this.casClientProperties.getLoginFormUrl();
    }

    @Override
    public String getLoginFormUrl(HttpServletRequest request) {
        String loginFormUrl = getDefaultLoginFormUrl();
        String url = request.getRequestURL().toString();
        return NetUtil.mergeParam(loginFormUrl, "service", url);
    }

    @Override
    public String getLogoutSuccessUrl() {
        return this.casClientProperties.getLogoutSuccessUrl();
    }

}
