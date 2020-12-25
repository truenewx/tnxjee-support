package org.truenewx.tnxjeex.cas.server.controller;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.validation.Assertion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.truenewx.tnxjee.web.util.WebUtil;
import org.truenewx.tnxjee.webmvc.security.config.annotation.ConfigAnonymous;
import org.truenewx.tnxjeex.cas.server.service.CasServiceManager;
import org.truenewx.tnxjeex.cas.server.ticket.TicketLogoutHandler;
import org.truenewx.tnxjeex.cas.server.ticket.TicketManager;
import org.truenewx.tnxjeex.cas.server.util.CasServerConstants;

/**
 * Cas服务端服务控制器
 *
 * @author jianglei
 */
@Controller
public class CasServerServiceController {

    @Autowired
    private CasServiceManager serviceManager;
    @Autowired
    private TicketManager ticketManager;

    @GetMapping("/serviceValidate")
    @ConfigAnonymous
    @ResponseBody
    public Assertion serviceValidate(@RequestParam("service") String service,
            @RequestParam("ticket") String ticket) {
        return this.ticketManager.validateServiceTicket(service, ticket);
    }

    @GetMapping("/serviceLogoutUrls")
    @ConfigAnonymous
    @ResponseBody
    public Map<String, String> serviceLogoutUrls(HttpServletRequest request) {
        String serviceString = WebUtil.getCookieValue(request, CasServerConstants.COOKIE_LOGOUT_SERVICES);
        if (StringUtils.isNotBlank(serviceString)) {
            String regex = "\\" + TicketLogoutHandler.LOGOUT_SERVICES_COOKIE_VALUE_SEPARATOR;
            String[] services = serviceString.split(regex);
            return this.serviceManager.getLogoutProcessUrls(request, services);
        }
        return Collections.emptyMap();
    }

}
