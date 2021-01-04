package org.truenewx.tnxjeex.cas.server.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.core.config.AppConfiguration;
import org.truenewx.tnxjee.core.config.CommonProperties;
import org.truenewx.tnxjee.service.exception.BusinessException;
import org.truenewx.tnxjeex.cas.server.ticket.CasTicketManager;

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

    private String artifactParameter = "ticket";

    public void setArtifactParameter(String artifactParameter) {
        this.artifactParameter = artifactParameter;
    }

    @Override
    public String getUserType(String service) {
        return loadServiceConfiguration(service).getUserType();
    }

    @Override
    public String getUri(HttpServletRequest request, String service) {
        return loadServiceConfiguration(service).getDirectUri();
    }

    private AppConfiguration loadServiceConfiguration(String service) {
        AppConfiguration msc = this.commonProperties.findAppByContextUri(service, false);
        if (msc == null) {
            throw new BusinessException(CasServerExceptionCodes.INVALID_SERVICE);
        }
        return msc;
    }

    @Override
    public String getLoginProcessUrl(HttpServletRequest request, String service, String scope) {
        String loginUrl = loadServiceConfiguration(service).getLoginProcessUrl();
        int index = loginUrl.indexOf(Strings.QUESTION);
        if (index < 0) {
            loginUrl += Strings.QUESTION;
        } else {
            loginUrl += Strings.AND;
        }
        loginUrl += this.artifactParameter + "=" + this.ticketManager.getServiceTicket(request, service, scope);
        return loginUrl;
    }

    @Override
    public String getLogoutProcessUrl(String service) {
        return loadServiceConfiguration(service).getLogoutProcessUrl();
    }

}
