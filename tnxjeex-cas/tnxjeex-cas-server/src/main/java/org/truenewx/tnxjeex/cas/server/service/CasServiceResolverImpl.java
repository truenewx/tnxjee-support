package org.truenewx.tnxjeex.cas.server.service;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.service.exception.BusinessException;
import org.truenewx.tnxjeex.cas.server.config.CasServerProperties;
import org.truenewx.tnxjeex.cas.server.ticket.TicketManager;

/**
 * CAS服务解决器实现
 *
 * @author jianglei
 */
@Component
public class CasServiceResolverImpl implements CasServiceResolver {

    @Autowired
    private CasServerProperties serverProperties;
    @Autowired
    private TicketManager ticketManager;

    @Override
    public String resolveUserType(String service) {
        return getProperties(service).getUserType();
    }

    private CasService getProperties(String service) {
        CasService casService = this.serverProperties.getServices().get(service);
        if (casService == null) {
            throw new BusinessException(CasServerExceptionCodes.INVALID_SERVICE);
        }
        return casService;
    }

    @Override
    public String resolveLoginUrl(HttpServletRequest request, String service) {
        String loginUrl = getProperties(service).getFullLoginUrl();
        loginUrl = prepareParameter(loginUrl);
        loginUrl += "ticket=" + this.ticketManager.getServiceTicket(request, service, true);
        return loginUrl;
    }

    private String prepareParameter(String url) {
        int index = url.indexOf(Strings.QUESTION);
        if (index < 0) {
            url += Strings.QUESTION;
        } else {
            url += Strings.AND;
        }
        return url;
    }

    @Override
    public List<String> resolveLogoutUrls(HttpServletRequest request, String excludedService) {
        List<String> urls = new ArrayList<>();
        this.ticketManager.deleteServiceTickets(request).forEach((service, serviceTicket) -> {
            if (!service.equals(excludedService)) {
                String logoutUrl = getProperties(service).getFullLogoutUrl();
                logoutUrl = prepareParameter(logoutUrl);
                logoutUrl += "ticket=" + serviceTicket;
                urls.add(logoutUrl);
            }
        });
        return urls;
    }
}
