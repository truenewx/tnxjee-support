package org.truenewx.tnxjeex.cas.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.service.exception.BusinessException;
import org.truenewx.tnxjeex.cas.server.config.CasServerProperties;

/**
 * 服务管理器实现
 *
 * @author jianglei
 */
@Component
@EnableConfigurationProperties(CasServerProperties.class)
public class CasServiceManagerImpl implements CasServiceManager {

    @Autowired(required = false)
    private CasServerProperties serverProperties;

    @Override
    public String resolveUserType(String service) {
        return getProperties(service).getUserType();
    }

    private CasService getProperties(String service) {
        CasService casService = this.serverProperties.getServices().get(service);
        if (casService == null) {
            throw new BusinessException(CasServerExceptionCodes.INVALID_SERVICE);
        }
        return casService;
    }

    @Override
    public String getAuthenticatedTargetUrl(String service) {
        return getProperties(service).getTargetUrl();
    }

}
