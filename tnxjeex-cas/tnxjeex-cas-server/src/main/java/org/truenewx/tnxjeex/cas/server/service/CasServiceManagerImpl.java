package org.truenewx.tnxjeex.cas.server.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.service.exception.BusinessException;
import org.truenewx.tnxjeex.cas.server.config.CasServerProperties;
import org.truenewx.tnxjeex.cas.server.ticket.TicketManager;

/**
 * 服务管理器实现
 *
 * @author jianglei
 */
@Component
public class CasServiceManagerImpl implements CasServiceManager {

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
    public String getAuthenticatedTargetUrl(HttpServletRequest request, String service) {
        String targetUrl = getProperties(service).getTargetUrl();
        int index = targetUrl.indexOf(Strings.QUESTION);
        if (index < 0) {
            targetUrl += Strings.QUESTION;
        } else {
            targetUrl += Strings.AND;
        }
        targetUrl += "ticket=" + this.ticketManager.getServiceTicket(request, service);
        return targetUrl;
    }

}
