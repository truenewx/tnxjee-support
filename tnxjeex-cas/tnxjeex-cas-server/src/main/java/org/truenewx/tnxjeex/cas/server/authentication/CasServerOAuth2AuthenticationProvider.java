package org.truenewx.tnxjeex.cas.server.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.model.spec.user.security.UserSpecificDetails;
import org.truenewx.tnxjee.service.spec.user.UserOAuth2LoginValidator;
import org.truenewx.tnxjee.webmvc.security.authentication.OAuth2ClientAuthenticationToken;

/**
 * Cas服务端第三方OAuth2登录授权提供者
 */
@Component
public class CasServerOAuth2AuthenticationProvider
        extends AbstractCasServerAuthenticationProvider<OAuth2ClientAuthenticationToken> {

    @Autowired(required = false)
    private UserOAuth2LoginValidator loginValidator;

    @Override
    protected UserSpecificDetails<?> validateLogin(String userType, String scope,
            OAuth2ClientAuthenticationToken token) {
        if (this.loginValidator != null) {
            Object userModel = token.getPrincipal();
            return this.loginValidator.validateOAuth2Login(userType, scope, userModel);
        }
        return null;
    }

}
