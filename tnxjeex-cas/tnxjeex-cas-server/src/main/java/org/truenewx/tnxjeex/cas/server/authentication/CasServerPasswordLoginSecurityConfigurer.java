package org.truenewx.tnxjeex.cas.server.authentication;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.webmvc.security.web.authentication.PasswordLoginProcessingFilter;
import org.truenewx.tnxjeex.cas.server.service.CasServiceManager;
import org.truenewx.tnxjeex.cas.server.util.CasServerConstants;

/**
 * CAS服务端密码登录配置器
 */
@Component
// 指定AuthenticationProvider实现类
public class CasServerPasswordLoginSecurityConfigurer
        extends AbstractCasServerLoginSecurityConfigurer<CasUsernamePasswordAuthenticationProvider> {

    @Autowired
    protected CasServiceManager serviceManager;

    @Override
    protected AbstractAuthenticationProcessingFilter getProcessingFilter() {
        PasswordLoginProcessingFilter filter = new PasswordLoginProcessingFilter(getApplicationContext());
        filter.acceptFailureHandler(handler -> {
            handler.setTargetUrlFunction(request -> {
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
        });
        return filter;
    }

}
