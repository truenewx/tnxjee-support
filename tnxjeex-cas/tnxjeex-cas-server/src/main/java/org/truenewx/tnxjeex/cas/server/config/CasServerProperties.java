package org.truenewx.tnxjeex.cas.server.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.truenewx.tnxjeex.cas.server.service.CasService;

/**
 * CAS服务端配置属性
 *
 * @author jianglei
 */
@ConfigurationProperties("tnxjeex.cas")
public class CasServerProperties {

    private Map<String, CasService> services = new HashMap<>();

    public Map<String, CasService> getServices() {
        return this.services;
    }

    public void setServices(Map<String, CasService> services) {
        this.services = services;
    }

}
