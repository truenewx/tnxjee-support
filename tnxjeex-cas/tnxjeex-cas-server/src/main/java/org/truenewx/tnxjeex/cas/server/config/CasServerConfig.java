package org.truenewx.tnxjeex.cas.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.truenewx.tnxjeex.cas.server.authentication.CasAuthenticationSuccessHandler;

@Configuration
public class CasServerConfig {

    @Bean
    public CasAuthenticationSuccessHandler authenticationSuccessHandler() {
        return new CasAuthenticationSuccessHandler();
    }

}
