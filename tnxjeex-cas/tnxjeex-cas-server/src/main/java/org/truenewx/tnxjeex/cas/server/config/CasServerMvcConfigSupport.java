package org.truenewx.tnxjeex.cas.server.config;

import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.truenewx.tnxjee.webmvc.view.config.WebViewMvcConfigurerSupport;
import org.truenewx.tnxjeex.cas.server.ticket.TicketManagerImpl;

import javax.servlet.http.HttpSessionListener;

public class CasServerMvcConfigSupport extends WebViewMvcConfigurerSupport {

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
