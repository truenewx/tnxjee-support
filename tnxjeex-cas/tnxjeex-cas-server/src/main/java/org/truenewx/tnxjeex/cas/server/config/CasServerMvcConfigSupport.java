package org.truenewx.tnxjeex.cas.server.config;

import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.truenewx.tnxjee.web.view.config.WebViewMvcConfigurationSupport;

public class CasServerMvcConfigSupport extends WebViewMvcConfigurationSupport {

    @Override
    protected void buildSiteMeshFilter(SiteMeshFilterBuilder builder) {
        builder.addExcludedPath("/serviceValidate");
    }

}
