package org.truenewx.tnxjeex.cas.server.security.authentication;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.truenewx.tnxjee.web.util.WebUtil;
import org.truenewx.tnxjeex.cas.server.util.CasServerConstants;
import org.truenewx.tnxjeex.cas.server.util.CasServerUtil;

/**
 * CasServiceAuthenticationDetails源
 */
public class CasServiceAuthenticationDetailsSource implements
        AuthenticationDetailsSource<HttpServletRequest, CasServiceAuthenticationDetails> {

    @Override
    public CasServiceAuthenticationDetails buildDetails(HttpServletRequest request) {
        String service = CasServerUtil.getService(request);
        String scope = WebUtil.getParameterOrAttribute(request, CasServerConstants.PARAMETER_SCOPE);
        String ip = WebUtil.getRemoteAddress(request);
        return new CasServiceAuthenticationDetails(service, scope, ip);
    }

}
