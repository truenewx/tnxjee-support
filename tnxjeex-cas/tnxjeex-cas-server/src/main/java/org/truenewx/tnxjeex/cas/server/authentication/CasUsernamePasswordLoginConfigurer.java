package org.truenewx.tnxjeex.cas.server.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.web.security.config.SecurityLoginConfigurerSupport;
import org.truenewx.tnxjee.web.security.web.authentication.BusinessExceptionAuthenticationFailureHandler;

/**
 * CAS用户名密码登录配置器
 */
@Component
// 指定AuthenticationProvider实现类
public class CasUsernamePasswordLoginConfigurer
        extends SecurityLoginConfigurerSupport<CasUsernamePasswordAuthenticationProvider> {

    @Autowired
    private CasServiceAuthenticationDetailsSource authenticationDetailsSource;
    @Autowired
    private CasAuthenticationSuccessHandler authenticationSuccessHandler;
    @Autowired
    private BusinessExceptionAuthenticationFailureHandler authenticationFailureHandler;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        UsernamePasswordAuthenticationFilter filter = new UsernamePasswordAuthenticationFilter();
        filter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class)); // 固定必须
        filter.setAuthenticationDetailsSource(this.authenticationDetailsSource);
        filter.setAuthenticationSuccessHandler(this.authenticationSuccessHandler); // 指定登录成功时的处理器
        filter.setAuthenticationFailureHandler(this.authenticationFailureHandler); // 指定登录失败时的处理器
        http.addFilterAt(filter, UsernamePasswordAuthenticationFilter.class);
    }

}
