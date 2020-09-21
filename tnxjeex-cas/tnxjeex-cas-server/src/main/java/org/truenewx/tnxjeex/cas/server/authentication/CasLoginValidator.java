package org.truenewx.tnxjeex.cas.server.authentication;

import org.truenewx.tnxjee.model.spec.user.security.UserSpecificDetails;

/**
 * CAS登录校验器
 */
public interface CasLoginValidator {

    UserSpecificDetails<?> validateLogin(String service, String scope, String username,
            String password);

}
