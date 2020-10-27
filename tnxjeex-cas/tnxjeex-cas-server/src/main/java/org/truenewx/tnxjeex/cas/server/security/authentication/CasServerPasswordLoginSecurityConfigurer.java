package org.truenewx.tnxjeex.cas.server.security.authentication;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.webmvc.security.web.authentication.PasswordLoginProcessingFilter;

/**
 * Cas服务端密码登录配置器
 */
@Component
public class CasServerPasswordLoginSecurityConfigurer
        extends AbstractCasServerLoginSecurityConfigurer<CasServerPasswordAuthenticationProvider> {

    @Override
    protected String getFilterKey() {
        return "password";
    }

    @Override
    protected AbstractAuthenticationProcessingFilter getProcessingFilter(String filterProcessesUrl) {
        if (filterProcessesUrl == null) {
            return null;
        }
        PasswordLoginProcessingFilter filter = new PasswordLoginProcessingFilter(getApplicationContext());
        if (StringUtils.isNotBlank(filterProcessesUrl)) {
            filter.setFilterProcessesUrl(filterProcessesUrl);
        }
        return filter;
    }

}
