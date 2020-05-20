package org.truenewx.tnxjeex.cas.client.config;

import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.truenewx.tnxjee.web.security.util.SecurityUtil;
import org.truenewx.tnxjeex.cas.client.userdetails.DefaultCasAssertionUserDetailsService;
import org.truenewx.tnxjeex.cas.client.validation.CasJsonServiceTicketValidator;

@Configuration
@EnableConfigurationProperties(CasClientProperties.class)
public class CasClientConfig {

    static {
        SecurityUtil.DETAIL_FUNCTION = authentication -> {
            if (authentication instanceof CasAuthenticationToken) {
                return ((CasAuthenticationToken) authentication).getUserDetails();
            }
            return authentication.getDetails();
        };
    }

    @Autowired
    private CasClientProperties properties;

    @Bean
    public TicketValidator ticketValidator() {
        return new CasJsonServiceTicketValidator(this.properties.getServerUrlPrefix());
    }

    @Bean
    public CasAuthenticationProvider authenticationProvider(
            DefaultCasAssertionUserDetailsService authenticationUserDetailsService) {
        CasAuthenticationProvider provider = new CasAuthenticationProvider();
        provider.setAuthenticationUserDetailsService(authenticationUserDetailsService);
        provider.setTicketValidator(ticketValidator());
        provider.setKey(this.properties.getService());
        provider.setServiceProperties(this.properties);
        return provider;
    }

}
