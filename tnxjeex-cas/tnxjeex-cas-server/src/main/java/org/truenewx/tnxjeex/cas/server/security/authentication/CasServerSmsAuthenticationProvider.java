package org.truenewx.tnxjeex.cas.server.security.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.model.spec.user.security.UserSpecificDetails;
import org.truenewx.tnxjee.service.spec.user.UserSmsLoginValidator;
import org.truenewx.tnxjee.webmvc.security.authentication.SmsVerifyCodeAuthenticationToken;

/**
 * Cas服务端短信登录授权提供者
 */
@Component
public class CasServerSmsAuthenticationProvider
        extends AbstractCasServerAuthenticationProvider<SmsVerifyCodeAuthenticationToken> {

    @Autowired(required = false)
    private UserSmsLoginValidator loginValidator;

    @Override
    protected UserSpecificDetails<?> validateLogin(String userType, String scope,
            SmsVerifyCodeAuthenticationToken token) {
        if (this.loginValidator != null) {
            String mobilePhone = token.getMobilePhone();
            String verifyCode = token.getVerifyCode();
            return this.loginValidator.validateSmsLogin(userType, scope, mobilePhone, verifyCode);
        }
        return null;
    }

}
