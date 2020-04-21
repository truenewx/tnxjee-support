package org.truenewx.tnxjeex.fss.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件存储Web配置属性集
 */
@ConfigurationProperties("tnxjeex.fss.web")
public class FssWebProperties {

    private String host;

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

}
