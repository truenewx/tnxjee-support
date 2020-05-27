package org.truenewx.tnxjeex.cas.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.truenewx.tnxjee.web.view.security.config.WebViewSecurityConfigurerSupport;
import org.truenewx.tnxjeex.cas.server.authentication.CasAuthenticationSuccessHandler;
import org.truenewx.tnxjeex.cas.server.authentication.logout.CasServerLogoutSuccessHandler;

/**
 * CAS服务端安全配置器支持
 */
public class CasServerSecurityConfigurerSupport extends WebViewSecurityConfigurerSupport {

    @Bean
    public CasAuthenticationSuccessHandler authenticationSuccessHandler() {
        return new CasAuthenticationSuccessHandler();
    }

    @Bean
    @Override
    public LogoutSuccessHandler logoutSuccessHandler() {
        CasServerLogoutSuccessHandler handler = new CasServerLogoutSuccessHandler();
        handler.setDefaultTargetUrl(getLogoutSuccessUrl());
        return handler;
    }

    @Override
    protected String getLoginFormUrl() {
        return "/login/form";
    }

    @Override
    protected String getLoginAjaxUrl() {
        return "/login/ajax";
    }

}
