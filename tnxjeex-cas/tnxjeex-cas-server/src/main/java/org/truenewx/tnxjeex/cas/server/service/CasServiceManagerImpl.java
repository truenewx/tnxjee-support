package org.truenewx.tnxjeex.cas.server.service;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.core.config.AppConfiguration;
import org.truenewx.tnxjee.core.config.CommonProperties;
import org.truenewx.tnxjee.service.exception.BusinessException;
import org.truenewx.tnxjeex.cas.server.ticket.TicketManager;

/**
 * CAS服务管理器实现
 *
 * @author jianglei
 */
@Component
public class CasServiceManagerImpl implements CasServiceManager {

    @Autowired
    private CommonProperties microServiceProperties;
    @Autowired
    private TicketManager ticketManager;

    @Override
    public String getUserType(String service) {
        return obtainServiceConfiguration(service).getUserType();
    }

    @Override
    public String getHost(String service) {
        return obtainServiceConfiguration(service).getHost();
    }

    private AppConfiguration obtainServiceConfiguration(String service) {
        AppConfiguration msc = this.microServiceProperties.getApps().get(service);
        if (msc == null) {
            throw new BusinessException(CasServerExceptionCodes.INVALID_SERVICE);
        }
        return msc;
    }

    @Override
    public String getLoginUrl(HttpServletRequest request, String service) {
        String loginUrl = obtainServiceConfiguration(service).getLoginUrl();
        int index = loginUrl.indexOf(Strings.QUESTION);
        if (index < 0) {
            loginUrl += Strings.QUESTION;
        } else {
            loginUrl += Strings.AND;
        }
        loginUrl += "ticket=" + this.ticketManager.getServiceTicket(request, service);
        return loginUrl;
    }

    @Override
    public Map<String, String> getLogoutUrls(String[] services) {
        Map<String, String> logoutUrls = new HashMap<>();
        Map<String, AppConfiguration> serviceConfigurations = this.microServiceProperties.getApps();
        for (String service : services) {
            AppConfiguration serviceConfiguration = serviceConfigurations.get(service);
            if (serviceConfiguration != null) {
                logoutUrls.put(service, serviceConfiguration.getLogoutUrl());
            }
        }
        return logoutUrls;
    }

}
