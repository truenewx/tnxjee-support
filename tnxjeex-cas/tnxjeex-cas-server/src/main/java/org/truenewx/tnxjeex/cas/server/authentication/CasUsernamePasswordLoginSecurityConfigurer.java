package org.truenewx.tnxjeex.cas.server.authentication;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.web.security.config.LoginSecurityConfigurerSupport;
import org.truenewx.tnxjee.web.security.web.authentication.WebUsernamePasswordAuthenticationFilter;
import org.truenewx.tnxjeex.cas.server.service.CasServiceManager;
import org.truenewx.tnxjeex.cas.server.util.CasServerConstants;

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
    private CasServiceManager serviceManager;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        WebUsernamePasswordAuthenticationFilter filter = new WebUsernamePasswordAuthenticationFilter(
                getApplicationContext());
        filter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class)); // 固定必须
        filter.setAuthenticationDetailsSource(this.authenticationDetailsSource);
        filter.setAuthenticationSuccessHandler(this.authenticationSuccessHandler); // 指定登录成功时的处理器
        filter.setFailureTargetUrlFunction(request -> {
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
        http.addFilterAt(filter, UsernamePasswordAuthenticationFilter.class);
    }

}
