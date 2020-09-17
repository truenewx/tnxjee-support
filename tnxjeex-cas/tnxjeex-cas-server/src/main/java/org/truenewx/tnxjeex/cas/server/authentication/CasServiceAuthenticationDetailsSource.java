package org.truenewx.tnxjeex.cas.server.authentication;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.stereotype.Component;

@Component
public class CasServiceAuthenticationDetailsSource implements
        AuthenticationDetailsSource<HttpServletRequest, CasServiceAuthenticationDetails> {

    @Override
    public CasServiceAuthenticationDetails buildDetails(HttpServletRequest context) {
        String service = context.getParameter("service");
        String scope = context.getParameter("scope");
        return new CasServiceAuthenticationDetails(service, scope);
    }

}
