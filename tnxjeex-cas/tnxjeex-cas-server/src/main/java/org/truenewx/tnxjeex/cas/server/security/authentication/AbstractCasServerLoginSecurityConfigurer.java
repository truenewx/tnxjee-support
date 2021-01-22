package org.truenewx.tnxjeex.cas.server.security.authentication;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.truenewx.tnxjee.core.config.AppConfiguration;
import org.truenewx.tnxjee.web.util.WebUtil;
import org.truenewx.tnxjee.webmvc.security.config.annotation.LoginSecurityConfigurerSupport;
import org.truenewx.tnxjee.webmvc.security.web.authentication.LoginAuthenticationFilter;
import org.truenewx.tnxjee.webmvc.security.web.authentication.ResolvableExceptionAuthenticationFailureHandler;
import org.truenewx.tnxjeex.cas.core.validation.constant.CasParameterNames;
import org.truenewx.tnxjeex.cas.server.service.CasServiceManager;

/**
 * 抽象的CAS服务端登录安全配置器
 *
 * @param <AP> 认证提供器实现类型
 */
public abstract class AbstractCasServerLoginSecurityConfigurer<PF extends AbstractAuthenticationProcessingFilter, AP extends AuthenticationProvider>
        extends LoginSecurityConfigurerSupport<PF, AP> {

    @Autowired
    private CasAuthenticationSuccessHandler authenticationSuccessHandler;
    @Autowired
    private CasServiceManager serviceManager;

    @Override
    protected void configure(HttpSecurity http, PF filter) {
        if (filter instanceof LoginAuthenticationFilter) {
            LoginAuthenticationFilter loginFilter = (LoginAuthenticationFilter) filter;
            loginFilter.setAuthenticationDetailsSource(authenticationDetailsSource());
            loginFilter.setAuthenticationSuccessHandler(this.authenticationSuccessHandler); // 指定登录成功时的处理器
            AuthenticationFailureHandler failureHandler = loginFilter.getFailureHandler();
            if (failureHandler instanceof ResolvableExceptionAuthenticationFailureHandler) {
                ((ResolvableExceptionAuthenticationFailureHandler) failureHandler).setTargetUrlFunction(request -> {
                    String service = WebUtil.getParameterOrAttribute(request, CasParameterNames.SERVICE);
                    String userType = this.serviceManager.getUserType(service);
                    if (AppConfiguration.USER_TYPE_ALL.equals(userType)) { // 不限定用户类型的服务，只能在用户已登录后进行自动登录
                        return null;
                    }
                    String result = "/login";
                    if (StringUtils.isNotBlank(userType)) {
                        result += "/" + userType.toLowerCase();
                    }
                    return result;
                });
            }
        }
        super.configure(http, filter);
    }

    protected AuthenticationDetailsSource<HttpServletRequest, CasServiceAuthenticationDetails> authenticationDetailsSource() {
        return new CasServiceAuthenticationDetailsSource();
    }

}
