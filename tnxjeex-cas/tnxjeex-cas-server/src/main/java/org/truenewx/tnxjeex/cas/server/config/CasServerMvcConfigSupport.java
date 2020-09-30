package org.truenewx.tnxjeex.cas.server.config;

import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.truenewx.tnxjee.webmvc.view.config.WebViewMvcConfigurerSupport;

public class CasServerMvcConfigSupport extends WebViewMvcConfigurerSupport {

    @Override
    protected void buildSiteMeshFilter(SiteMeshFilterBuilder builder) {
        builder.addExcludedPath("/serviceValidate");
        builder.addExcludedPath("/serviceLogoutUrls");
    }

}
