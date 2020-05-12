package org.truenewx.tnxjeex.cas.server.config;

import javax.servlet.http.HttpSessionListener;

import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.truenewx.tnxjee.web.view.config.WebViewMvcConfigurationSupport;
import org.truenewx.tnxjeex.cas.server.ticket.TicketManagerImpl;

public class CasServerMvcConfigSupport extends WebViewMvcConfigurationSupport {

    @Override
    protected void buildSiteMeshFilter(SiteMeshFilterBuilder builder) {
        builder.addExcludedPath("/serviceValidate");
    }

    @Bean
    public ServletListenerRegistrationBean<HttpSessionListener> ticketManagerSessionListener(
            TicketManagerImpl ticketManager) {
        ServletListenerRegistrationBean<HttpSessionListener> register = new ServletListenerRegistrationBean<>();
        register.setListener(ticketManager);
        return register;
    }

}
