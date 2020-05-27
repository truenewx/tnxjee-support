package org.truenewx.tnxjeex.cas.server.config;

import org.springframework.context.annotation.Bean;
import org.truenewx.tnxjee.web.view.security.config.WebViewSecurityConfigurerSupport;
import org.truenewx.tnxjeex.cas.server.authentication.CasAuthenticationSuccessHandler;

/**
 * CAS服务端安全配置器支持
 */
public class CasServerSecurityConfigurerSupport extends WebViewSecurityConfigurerSupport {

    @Bean
    public CasAuthenticationSuccessHandler authenticationSuccessHandler() {
        return new CasAuthenticationSuccessHandler();
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
