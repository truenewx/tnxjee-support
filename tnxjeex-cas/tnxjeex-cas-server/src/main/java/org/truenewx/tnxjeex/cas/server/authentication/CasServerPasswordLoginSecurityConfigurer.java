package org.truenewx.tnxjeex.cas.server.authentication;

import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.webmvc.security.web.authentication.PasswordLoginProcessingFilter;

/**
 * CAS服务端密码登录配置器
 */
@Component
// 指定AuthenticationProvider实现类
public class CasServerPasswordLoginSecurityConfigurer
        extends AbstractCasServerLoginSecurityConfigurer<CasUsernamePasswordAuthenticationProvider> {

    @Override
    protected String getFilterKey() {
        return "password";
    }

    @Override
    protected AbstractAuthenticationProcessingFilter getProcessingFilter() {
        return new PasswordLoginProcessingFilter(getApplicationContext());
    }

}
