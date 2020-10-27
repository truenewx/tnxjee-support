package org.truenewx.tnxjeex.cas.server.security.authentication;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.truenewx.tnxjee.web.util.WebUtil;
import org.truenewx.tnxjeex.cas.server.util.CasServerConstants;

/**
 * CasServiceAuthenticationDetails源
 */
public class CasServiceAuthenticationDetailsSource implements
        AuthenticationDetailsSource<HttpServletRequest, CasServiceAuthenticationDetails> {

    @Override
    public CasServiceAuthenticationDetails buildDetails(HttpServletRequest request) {
        String service = WebUtil.getParameterOrAttribute(request, CasServerConstants.PARAMETER_SERVICE);
        String scope = WebUtil.getParameterOrAttribute(request, CasServerConstants.PARAMETER_SCOPE);
        return new CasServiceAuthenticationDetails(service, scope);
    }

}
