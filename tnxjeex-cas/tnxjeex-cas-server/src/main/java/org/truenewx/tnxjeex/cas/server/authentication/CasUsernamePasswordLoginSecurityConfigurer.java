package org.truenewx.tnxjeex.cas.server.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.web.security.config.LoginSecurityConfigurerSupport;
import org.truenewx.tnxjee.web.security.web.authentication.ResolvableExceptionAuthenticationFailureHandler;
import org.truenewx.tnxjeex.cas.server.service.CasServiceManager;

/**
 * CAS用户名密码登录配置器
 */
@Component
// 指定AuthenticationProvider实现类
public class CasUsernamePasswordLoginSecurityConfigurer
        extends LoginSecurityConfigurerSupport<CasUsernamePasswordAuthenticationProvider> {

    @Autowired
    private CasServiceAuthenticationDetailsSource authenticationDetailsSource;
    @Autowired
    private CasAuthenticationSuccessHandler authenticationSuccessHandler;
    @Autowired
    private ResolvableExceptionAuthenticationFailureHandler authenticationFailureHandler;
    @Autowired
    private CasServiceManager serviceManager;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        UsernamePasswordAuthenticationFilter filter = new UsernamePasswordAuthenticationFilter();
        filter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class)); // 固定必须
        filter.setAuthenticationDetailsSource(this.authenticationDetailsSource);
        filter.setAuthenticationSuccessHandler(this.authenticationSuccessHandler); // 指定登录成功时的处理器
        this.authenticationFailureHandler.setTargetUrlFunction(request -> {
            String service = request.getParameter("service");
            request.setAttribute("service", service);
            String userType = this.serviceManager.getUserType(service);
            return "/login/" + userType.toLowerCase();
        });
        filter.setAuthenticationFailureHandler(this.authenticationFailureHandler); // 指定登录失败时的处理器
        http.addFilterAt(filter, UsernamePasswordAuthenticationFilter.class);
    }

}
