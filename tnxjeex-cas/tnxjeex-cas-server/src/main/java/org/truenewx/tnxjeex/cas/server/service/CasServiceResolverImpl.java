package org.truenewx.tnxjeex.cas.server.service;

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
        int index = loginUrl.indexOf(Strings.QUESTION);
        if (index < 0) {
            loginUrl += Strings.QUESTION;
        } else {
            loginUrl += Strings.AND;
        }
        loginUrl += "ticket=" + this.ticketManager.getServiceTicket(request, service);
        return loginUrl;
    }

}
