package org.truenewx.tnxjeex.cas.server.authentication;

import javax.servlet.Filter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.truenewx.tnxjee.webmvc.security.config.LoginSecurityConfigurerSupport;
import org.truenewx.tnxjee.webmvc.security.web.authentication.LoginProcessingFilter;
import org.truenewx.tnxjee.webmvc.security.web.authentication.ResolvableExceptionAuthenticationFailureHandler;
import org.truenewx.tnxjeex.cas.server.service.CasServiceManager;
import org.truenewx.tnxjeex.cas.server.util.CasServerConstants;

/**
 * 抽象的CAS服务端登录安全配置器
 *
 * @param <AP> 认证提供器实现类型
 */
public abstract class AbstractCasServerLoginSecurityConfigurer<AP extends AuthenticationProvider>
        extends LoginSecurityConfigurerSupport<AP> {

    /**
     * 配置属性前缀：登录处理url
     */
    private static final String PROPERTY_PREFIX_LOGIN_PROCESSES_URL = "tnxjeex.cas.server.login-processes-url.";

    @Autowired
    private CasServiceAuthenticationDetailsSource authenticationDetailsSource;
    @Autowired
    private CasAuthenticationSuccessHandler authenticationSuccessHandler;
    @Autowired
    private CasServiceManager serviceManager;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        String filterProcessesUrl = getFilterProcessesUrl();
        if (filterProcessesUrl != null) { // 提供了过滤器处理url才添加对应的过滤器
            AbstractAuthenticationProcessingFilter filter = getProcessingFilter();
            if (StringUtils.isNotBlank(filterProcessesUrl)) { // 过滤器处理url不为空才修改
                filter.setFilterProcessesUrl(filterProcessesUrl);
            }
            filter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class)); // 固定必须
            filter.setAuthenticationDetailsSource(this.authenticationDetailsSource);
            filter.setAuthenticationSuccessHandler(this.authenticationSuccessHandler); // 指定登录成功时的处理器
            if (filter instanceof LoginProcessingFilter) {
                LoginProcessingFilter loginFilter = (LoginProcessingFilter) filter;
                AuthenticationFailureHandler failureHandler = loginFilter.getFailureHandler();
                if (failureHandler instanceof ResolvableExceptionAuthenticationFailureHandler) {
                    ((ResolvableExceptionAuthenticationFailureHandler) failureHandler).setTargetUrlFunction(request -> {
                        String service = request.getParameter(CasServerConstants.PARAMETER_SERVICE);
                        String userType = this.serviceManager.getUserType(service);
                        if (CasServerConstants.SERVICE_USER_TYPE_ALL.equals(userType)) { // 不限定用户类型的服务，只能在用户已登录后进行自动登录
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
            http.addFilterAt(filter, getAtFilterClass(filter));
        }
    }

    protected String getFilterProcessesUrl() {
        String filterKey = getFilterKey();
        if (StringUtils.isBlank(filterKey)) {
            return null;
        }
        return getApplicationContext().getEnvironment().getProperty(PROPERTY_PREFIX_LOGIN_PROCESSES_URL + filterKey);
    }

    /**
     * @return 过滤器标识，用于从配置文件中获取对应的登录处理url值
     */
    protected abstract String getFilterKey();

    protected abstract AbstractAuthenticationProcessingFilter getProcessingFilter();


    protected Class<? extends Filter> getAtFilterClass(AbstractAuthenticationProcessingFilter filter) {
        if (filter instanceof UsernamePasswordAuthenticationFilter) { // 用户名密码鉴权过滤器的子类，占用其过滤器位置
            return UsernamePasswordAuthenticationFilter.class;
        }
        return filter.getClass();
    }

}
