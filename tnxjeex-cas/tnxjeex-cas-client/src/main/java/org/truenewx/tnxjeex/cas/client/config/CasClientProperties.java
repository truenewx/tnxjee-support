package org.truenewx.tnxjeex.cas.client.config;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.cas.ServiceProperties;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.core.config.AppConfiguration;
import org.truenewx.tnxjee.core.config.AppConstants;
import org.truenewx.tnxjee.core.config.CommonProperties;

/**
 * CAS客户端配置属性
 *
 * @author jianglei
 */
@Configuration
@ConfigurationProperties("tnxjeex.cas")
public class CasClientProperties extends ServiceProperties {

    private String serverAppName = "cas";
    @Value(AppConstants.EL_SPRING_APP_NAME)
    private String appName;
    @Autowired
    private CommonProperties commonProperties;

    public String getServerAppName() {
        return this.serverAppName;
    }

    /**
     * @param serverAppName CAS服务器应用名称，默认为cas
     */
    public void setServerAppName(String serverAppName) {
        this.serverAppName = serverAppName;
    }

    @Override
    public void afterPropertiesSet() {
        if (StringUtils.isBlank(getService())) {
            AppConfiguration app = this.commonProperties.getApp(this.appName);
            String contextUri = app.getContextUri(false);
            setService(URLEncoder.encode(contextUri, StandardCharsets.UTF_8));
        }
        super.afterPropertiesSet();
    }

    public String getServerContextUri(boolean direct) {
        AppConfiguration app = this.commonProperties.getApp(getServerAppName());
        return app == null ? null : app.getContextUri(direct);
    }

    public String getLoginFormUrl() {
        String url = getServerContextUri(false);
        if (!url.endsWith(Strings.SLASH)) {
            url += Strings.SLASH;
        }
        return url + "login?" + getServiceParameter() + "=" + getService();
    }

    public String getLogoutProcessUrl() {
        String url = getServerContextUri(false);
        if (!url.endsWith(Strings.SLASH)) {
            url += Strings.SLASH;
        }
        return url + "logout?" + getServiceParameter() + "=" + getService();
    }

}
