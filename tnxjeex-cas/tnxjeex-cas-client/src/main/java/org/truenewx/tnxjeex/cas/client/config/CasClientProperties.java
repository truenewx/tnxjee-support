package org.truenewx.tnxjeex.cas.client.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.cas.ServiceProperties;
import org.truenewx.tnxjee.core.Strings;

/**
 * CAS客户端配置属性
 *
 * @author jianglei
 */
@Configuration
@ConfigurationProperties("tnxjeex.cas")
public class CasClientProperties extends ServiceProperties {

    private String serverUrlPrefix;
    @Autowired
    private Environment environment;

    public String getServerUrlPrefix() {
        return this.serverUrlPrefix;
    }

    public void setServerUrlPrefix(String serverUrlPrefix) {
        this.serverUrlPrefix = serverUrlPrefix;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (StringUtils.isBlank(getService())) {
            setService(this.environment.getProperty("spring.application.name"));
        }
        super.afterPropertiesSet();
    }

    public String getLoginFormUrl() {
        return getLoginUrl("form");
    }

    private String getLoginUrl(String type) {
        String url = this.serverUrlPrefix;
        if (!url.endsWith(Strings.SLASH)) {
            url += Strings.SLASH;
        }
        url += "login/" + type + "?service=" + getService();
        return url;
    }

    public String getLoginAjaxUrl() {
        return getLoginUrl("ajax");
    }

}
