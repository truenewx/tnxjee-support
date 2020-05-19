package org.truenewx.tnxjeex.fss.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.truenewx.tnxjee.web.security.config.WebSecurityConfigurerSupport;
import org.truenewx.tnxjeex.cas.client.config.CasClientProperties;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerSupport {

    @Autowired
    private CasClientProperties casClientProperties;

    @Override
    protected String getLoginFormUrl() {
        return this.casClientProperties.getLoginFormUrl();
    }

    @Override
    protected String getLoginAjaxUrl() {
        return this.casClientProperties.getLoginAjaxUrl();
    }

}
