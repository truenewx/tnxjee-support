package org.truenewx.tnxjeex.cas.server.security.authentication;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.webmvc.security.web.authentication.SmsLoginProcessingFilter;

/**
 * Cas服务端短信登录配置器
 */
@Component
public class CasServerSmsLoginSecurityConfigurer extends
        AbstractCasServerLoginSecurityConfigurer<CasServerSmsAuthenticationProvider> {

    @Override
    protected String getFilterKey() {
        return "sms";
    }

    @Override
    protected AbstractAuthenticationProcessingFilter getProcessingFilter(String filterProcessesUrl) {
        if (StringUtils.isBlank(filterProcessesUrl)) {
            return null;
        }
        return new SmsLoginProcessingFilter(filterProcessesUrl, getApplicationContext());
    }

}
