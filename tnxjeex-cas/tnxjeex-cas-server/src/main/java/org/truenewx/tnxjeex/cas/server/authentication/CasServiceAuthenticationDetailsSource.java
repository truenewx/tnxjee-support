package org.truenewx.tnxjeex.cas.server.authentication;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjeex.cas.server.util.CasServerConstants;

@Component
public class CasServiceAuthenticationDetailsSource implements
        AuthenticationDetailsSource<HttpServletRequest, CasServiceAuthenticationDetails> {

    @Override
    public CasServiceAuthenticationDetails buildDetails(HttpServletRequest context) {
        String service = context.getParameter(CasServerConstants.PARAMETER_SERVICE);
        String scope = context.getParameter(CasServerConstants.PARAMETER_SCOPE);
        return new CasServiceAuthenticationDetails(service, scope);
    }

}
