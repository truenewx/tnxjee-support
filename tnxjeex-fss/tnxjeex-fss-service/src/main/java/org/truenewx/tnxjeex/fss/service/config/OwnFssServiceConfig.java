package org.truenewx.tnxjeex.fss.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.truenewx.tnxjeex.fss.service.own.OwnFssAccessor;
import org.truenewx.tnxjeex.fss.service.own.OwnFssAuthorizer;

/**
 * 自有文件存储服务配置
 */
@Configuration
public class OwnFssServiceConfig {

    @Bean
    public OwnFssAccessor ownFssAccessor(FssLocalAccessorProperties properties) {
        return new OwnFssAccessor(properties.getRoot(), getLocalAccessorSalt());
    }

    protected Byte getLocalAccessorSalt() {
        return null;
    }

    @Bean
    public OwnFssAuthorizer ownFssAuthorizer() {
        return new OwnFssAuthorizer();
    }

}
