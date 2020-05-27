package org.truenewx.tnxjeex.cas.server.authentication.logout;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.core.util.NetUtil;
import org.truenewx.tnxjeex.cas.server.service.CasServiceResolver;

/**
 * 服务登出处理器
 */
@Component
public class ServiceLogoutHandler implements LogoutHandler {

    private String serviceParameter = "service";
    @Autowired
    private CasServiceResolver serviceManager;

    public void setServiceParameter(String serviceParameter) {
        this.serviceParameter = serviceParameter;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) {
        String service = request.getParameter(this.serviceParameter);
        List<String> logoutUrls = this.serviceManager.resolveLogoutUrls(request, service);
        logoutUrls.forEach(this::sendLogoutMessage);
    }

    private void sendLogoutMessage(String logoutUrl) {
        NetUtil.requestByGet(logoutUrl, null, null);
    }

}
