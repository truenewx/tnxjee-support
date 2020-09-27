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
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.webmvc.security.config.annotation.ConfigAnonymous;
import org.truenewx.tnxjee.webmvc.util.WebMvcUtil;
import org.truenewx.tnxjeex.cas.server.service.CasServiceManager;
import org.truenewx.tnxjeex.cas.server.ticket.TicketManager;
import org.truenewx.tnxjeex.cas.server.util.CasServerConstants;

/**
 * 服务控制器
 *
 * @author jianglei
 */
@Controller
public class ServiceController {

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
        String serviceString = WebMvcUtil.getCookieValue(request,
                CasServerConstants.COOKIE_LOGOUT_SERVICES);
        if (StringUtils.isNotBlank(serviceString)) {
            String[] services = serviceString.split(Strings.COMMA);
            return this.serviceManager.getLogoutUrls(services);
        }
        return Collections.emptyMap();
    }

}
