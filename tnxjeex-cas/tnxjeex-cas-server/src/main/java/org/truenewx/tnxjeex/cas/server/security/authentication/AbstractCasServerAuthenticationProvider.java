package org.truenewx.tnxjeex.cas.server.security.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.truenewx.tnxjee.core.util.ClassUtil;
import org.truenewx.tnxjee.model.spec.user.security.UserSpecificDetails;
import org.truenewx.tnxjee.service.exception.BusinessException;
import org.truenewx.tnxjeex.cas.server.service.CasServerExceptionCodes;
import org.truenewx.tnxjeex.cas.server.service.CasServiceManager;

/**
 * 抽象的Cas服务端认证提供者
 *
 * @param <T> 认证类型
 */
public abstract class AbstractCasServerAuthenticationProvider<T extends Authentication>
        implements AuthenticationProvider {

    @Autowired
    private CasServiceManager serviceManager;

    @Override
    public boolean supports(Class<?> authentication) {
        Class<?> genericType = ClassUtil.getActualGenericType(getClass(), 0);
        return genericType != null && genericType.isAssignableFrom(authentication);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        @SuppressWarnings("unchecked")
        T token = (T) authentication;
        Object details = token.getDetails();
        if (details instanceof CasServiceAuthenticationDetails) {
            CasServiceAuthenticationDetails authenticationDetails = (CasServiceAuthenticationDetails) details;
            String service = authenticationDetails.getService();
            String userType = this.serviceManager.getUserType(service);
            String scope = authenticationDetails.getScope();
            try {
                UserSpecificDetails<?> userDetails = validateLogin(userType, scope, token);
                if (userDetails == null) {
                    throw new BusinessException(CasServerExceptionCodes.UNSUPPORTED_USER_TYPE, userType);
                }
                return new CasUserSpecificDetailsAuthenticationToken(service, userDetails);
            } catch (BusinessException e) {
                throw new BadCredentialsException(e.getLocalizedMessage(), e);
            }
        }
        return null;
    }

    protected abstract UserSpecificDetails<?> validateLogin(String userType, String scope, T token);
}
