package org.truenewx.tnxjeex.cas.server.security.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.model.spec.user.security.UserSpecificDetails;
import org.truenewx.tnxjee.service.spec.user.UserPasswordLoginValidator;

/**
 * Cas服务端密码登录授权提供者
 */
@Component
public class CasServerPasswordAuthenticationProvider
        extends AbstractCasServerAuthenticationProvider<UsernamePasswordAuthenticationToken> {

    @Autowired(required = false)
    private UserPasswordLoginValidator loginValidator;

    @Override
    protected UserSpecificDetails<?> validateLogin(String userType, String scope,
            UsernamePasswordAuthenticationToken token) {
        if (this.loginValidator != null) {
            String username = (String) token.getPrincipal();
            String password = (String) token.getCredentials();
            return this.loginValidator.validatePasswordLogin(userType, scope, username, password);
        }
        return null;
    }

}
