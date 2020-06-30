package org.truenewx.tnxjeex.cas.server.authentication.logout;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.truenewx.tnxjee.core.util.NetUtil;

/**
 * CAS服务端登出成功处理器
 */
public class CasServerLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    private String serviceParameter = "service";

    public void setServiceParameter(String serviceParameter) {
        this.serviceParameter = serviceParameter;
    }

    @Override
    @Autowired // 覆写以自动注入
    public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
        super.setRedirectStrategy(redirectStrategy);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        String targetUrl = super.determineTargetUrl(request, response);
        String service = request.getParameter(this.serviceParameter);
        targetUrl = NetUtil.mergeParam(targetUrl, this.serviceParameter, service);
        return targetUrl;
    }

}
