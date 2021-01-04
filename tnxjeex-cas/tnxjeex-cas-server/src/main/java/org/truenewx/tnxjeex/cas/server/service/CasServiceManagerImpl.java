package org.truenewx.tnxjeex.cas.server.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.core.config.AppConfiguration;
import org.truenewx.tnxjee.core.config.CommonProperties;
import org.truenewx.tnxjee.service.exception.BusinessException;
import org.truenewx.tnxjeex.cas.server.ticket.CasTicketManager;
import org.truenewx.tnxjeex.cas.server.util.CasServerConstants;

/**
 * CAS服务管理器实现
 *
 * @author jianglei
 */
@Component
public class CasServiceManagerImpl implements CasServiceManager {

    @Autowired
    private CommonProperties commonProperties;
    @Autowired
    private CasTicketManager ticketManager;

    private String artifactParameter = CasServerConstants.PARAMETER_ARTIFACT;

    public void setArtifactParameter(String artifactParameter) {
        this.artifactParameter = artifactParameter;
    }

    @Override
    public String getAppName(String service) {
        return this.commonProperties.findAppName(service, false);
    }

    @Override
    public String getService(String appName) {
        AppConfiguration app = this.commonProperties.getApp(appName);
        return app == null ? null : app.getContextUri(false);
    }

    private AppConfiguration loadAppConfigurationByService(String service) {
        String appName = getAppName(service);
        return loadAppConfigurationByName(appName);
    }

    private AppConfiguration loadAppConfigurationByName(String appName) {
        AppConfiguration appConfiguration = this.commonProperties.getApp(appName);
        if (appConfiguration == null) {
            throw new BusinessException(CasServerExceptionCodes.INVALID_SERVICE);
        }
        return appConfiguration;
    }

    @Override
    public String getUserType(String service) {
        return loadAppConfigurationByService(service).getUserType();
    }

    @Override
    public String getUri(HttpServletRequest request, String service) {
        return loadAppConfigurationByService(service).getDirectUri();
    }

    @Override
    public String getLoginProcessUrl(HttpServletRequest request, String service, String scope) {
        String appName = getAppName(service);
        String loginUrl = loadAppConfigurationByName(appName).getLoginProcessUrl();
        int index = loginUrl.indexOf(Strings.QUESTION);
        if (index < 0) {
            loginUrl += Strings.QUESTION;
        } else {
            loginUrl += Strings.AND;
        }
        loginUrl += this.artifactParameter + "=" + this.ticketManager.getAppTicketId(request, appName, scope);
        return loginUrl;
    }

    @Override
    public String getLogoutProcessUrl(String service) {
        return loadAppConfigurationByService(service).getLogoutProcessUrl();
    }

}
