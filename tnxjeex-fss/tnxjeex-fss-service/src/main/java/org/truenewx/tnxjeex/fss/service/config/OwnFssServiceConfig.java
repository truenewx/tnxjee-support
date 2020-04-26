package org.truenewx.tnxjeex.fss.service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.truenewx.tnxjeex.fss.service.FssLocalAccessor;
import org.truenewx.tnxjeex.fss.service.own.OwnFssAccessor;
import org.truenewx.tnxjeex.fss.service.own.OwnFssAuthorizer;

/**
 * 自有文件存储服务配置
 */
@Configuration
@EnableConfigurationProperties(FssLocalAccessorProperties.class)
public class OwnFssServiceConfig {

    @Bean
    public FssLocalAccessor fssLocalAccessor(
            FssLocalAccessorProperties fssLocalAccessorProperties) {
        String root = fssLocalAccessorProperties.getRoot();
        return new FssLocalAccessor(root, getLocalAccessorSalt());
    }

    protected Byte getLocalAccessorSalt() {
        return null;
    }

    @Bean
    public OwnFssAccessor ownFssAccessor(FssLocalAccessor localAccessor) {
        OwnFssAccessor accessor = new OwnFssAccessor();
        accessor.setLocalAccessor(localAccessor);
        return accessor;
    }

    @Bean
    public OwnFssAuthorizer ownFssAuthorizer() {
        return new OwnFssAuthorizer();
    }
}
