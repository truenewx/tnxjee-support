package org.truenewx.tnxjeex.fss.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件存储访问器配置属性集
 */
@ConfigurationProperties("tnxjeex.fss.accessor")
public class FssAccessorProperties {

    /**
     * 本地访问器根目录
     */
    private String localRoot;

    public String getLocalRoot() {
        return this.localRoot;
    }

    public void setLocalRoot(String localRoot) {
        this.localRoot = localRoot;
    }
}
