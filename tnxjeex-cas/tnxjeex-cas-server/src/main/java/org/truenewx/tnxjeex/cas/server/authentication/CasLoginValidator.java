package org.truenewx.tnxjeex.cas.server.authentication;

import org.truenewx.tnxjee.model.spec.user.UserIdentity;

/**
 * CAS登录校验器
 */
public interface CasLoginValidator {

    UserIdentity<?> validateLogin(String userType, String username, String password);

}
