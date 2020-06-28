package org.truenewx.tnxjeex.cas.server.config;

import java.util.Collection;

import javax.servlet.http.HttpSessionListener;

import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.truenewx.tnxjee.web.view.config.WebViewMvcConfigurerSupport;
import org.truenewx.tnxjeex.cas.server.ticket.TicketManagerImpl;
import org.truenewx.tnxjeex.cas.server.util.CasServerConstants;

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

    @Override
    protected void addExposedHeaders(Collection<String> exposedHeaders) {
        super.addExposedHeaders(exposedHeaders);
        exposedHeaders.add(CasServerConstants.HEADER_LOGIN_FORM_URL);
    }
}
