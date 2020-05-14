package org.truenewx.tnxjeex.cas.client.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.web.api.meta.model.ApiMetaProperties;
import org.truenewx.tnxjee.web.security.config.SecurityLoginConfigurerSupport;
import org.truenewx.tnxjee.web.security.web.authentication.ResolvableExceptionAuthenticationFailureHandler;
import org.truenewx.tnxjeex.cas.client.filter.CasClientAuthenticationFilter;

/**
 * CAS客户端登录配置器
 */
@Component
public class CasClientLoginConfigurer
        extends SecurityLoginConfigurerSupport<CasAuthenticationProvider> {

    @Autowired(required = false)
    private ApiMetaProperties apiMetaProperties;
    @Autowired
    private RedirectStrategy redirectStrategy;
    @Autowired
    private ResolvableExceptionAuthenticationFailureHandler authenticationFailureHandler;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        CasClientAuthenticationFilter filter = new CasClientAuthenticationFilter();
        filter.setRedirectStrategy(this.redirectStrategy);
        filter.setSuccessTargetUrlParameter(this.apiMetaProperties.getLoginSuccessRedirectParameter());
        filter.setAuthenticationFailureHandler(this.authenticationFailureHandler);
        filter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class)); // 固定必须
        http.addFilterAt(filter, CasAuthenticationFilter.class);
    }

}
