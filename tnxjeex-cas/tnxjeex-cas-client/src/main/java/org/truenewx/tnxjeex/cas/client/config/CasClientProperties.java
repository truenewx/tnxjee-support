package org.truenewx.tnxjeex.cas.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.cas.ServiceProperties;
import org.truenewx.tnxjee.core.Strings;

/**
 * CAS客户端配置属性
 *
 * @author jianglei
 */
@ConfigurationProperties("tnxjeex.cas")
public class CasClientProperties extends ServiceProperties {

    private String serverUrlPrefix;

    public String getServerUrlPrefix() {
        return this.serverUrlPrefix;
    }

    public void setServerUrlPrefix(String serverUrlPrefix) {
        this.serverUrlPrefix = serverUrlPrefix;
    }

    public String getLoginFormUrl() {
        String url = this.serverUrlPrefix;
        if (!url.endsWith(Strings.SLASH)) {
            url += Strings.SLASH;
        }
        url += "login?service=" + getService();
        return url;
    }

}
