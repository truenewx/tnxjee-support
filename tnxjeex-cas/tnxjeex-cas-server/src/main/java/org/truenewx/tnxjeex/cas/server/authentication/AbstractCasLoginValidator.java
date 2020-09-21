package org.truenewx.tnxjeex.cas.server.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.truenewx.tnxjee.model.spec.user.security.UserSpecificDetails;
import org.truenewx.tnxjee.service.exception.BusinessException;
import org.truenewx.tnxjeex.cas.server.service.CasServerExceptionCodes;
import org.truenewx.tnxjeex.cas.server.service.CasServiceManager;

/**
 * 抽象的CAS登录校验器
 */
public abstract class AbstractCasLoginValidator implements CasLoginValidator {

    @Autowired
    private CasServiceManager serviceManager;

    @Override
    public UserSpecificDetails<?> validateLogin(String service, String scope,
            String username, String password) {
        String userType = this.serviceManager.getUserType(service);
        UserSpecificDetails<?> details = validateLogin(userType, username, password);
        if (details == null) {
            throw new BusinessException(CasServerExceptionCodes.UNSUPPORTED_USER_TYPE, userType);
        }
        return details;
    }

    protected abstract UserSpecificDetails<?> validateLogin(String userType, String username, String password);

}
