package org.truenewx.tnxjeex.cas.server.authentication;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.truenewx.tnxjee.webmvc.security.config.LoginSecurityConfigurerSupport;

/**
 * 抽象的CAS服务端登录安全配置器
 *
 * @param <AP> 认证提供器实现类型
 */
public abstract class AbstractCasServerLoginSecurityConfigurer<AP extends AuthenticationProvider>
        extends LoginSecurityConfigurerSupport<AP> {

    @Autowired
    private CasServiceAuthenticationDetailsSource authenticationDetailsSource;
    @Autowired
    private CasAuthenticationSuccessHandler authenticationSuccessHandler;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        AbstractAuthenticationProcessingFilter filter = getProcessingFilter();
        filter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class)); // 固定必须
        filter.setAuthenticationDetailsSource(this.authenticationDetailsSource);
        filter.setAuthenticationSuccessHandler(this.authenticationSuccessHandler); // 指定登录成功时的处理器
        http.addFilterAt(filter, getAtFilterClass(filter));
    }

    protected abstract AbstractAuthenticationProcessingFilter getProcessingFilter();

    protected Class<? extends Filter> getAtFilterClass(AbstractAuthenticationProcessingFilter filter) {
        if (filter instanceof UsernamePasswordAuthenticationFilter) {
            return UsernamePasswordAuthenticationFilter.class;
        }
        return filter.getClass();
    }

}
