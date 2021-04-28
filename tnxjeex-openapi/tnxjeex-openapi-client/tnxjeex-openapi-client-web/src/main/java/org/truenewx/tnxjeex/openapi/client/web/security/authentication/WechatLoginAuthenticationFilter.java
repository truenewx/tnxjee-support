package org.truenewx.tnxjeex.openapi.client.web.security.authentication;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.service.exception.BusinessException;
import org.truenewx.tnxjee.web.util.WebUtil;
import org.truenewx.tnxjee.webmvc.security.core.BusinessAuthenticationException;

/**
 * 微信登录认证过滤器
 *
 * @author jianglei
 */
public class WechatLoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String REQUEST_URL_PREFIX = "/login/";
    public static final RequestMatcher REQUEST_MATCHER = new AntPathRequestMatcher(
            REQUEST_URL_PREFIX + Strings.ASTERISK, HttpMethod.POST.name());

    private Map<String, WechatAuthenticationTokenResolver> tokenResolverMapping = new HashMap<>();

    public WechatLoginAuthenticationFilter(ApplicationContext context) {
        // 处理所有/login/*请求
        super(REQUEST_MATCHER);

        context.getBeansOfType(WechatAuthenticationTokenResolver.class).forEach((id, resolver) -> {
            String loginMode = resolver.getLoginMode();
            if (StringUtils.isNotBlank(loginMode)) {
                this.tokenResolverMapping.put(loginMode, resolver);
            }
        });
    }

    private String getLoginMode(HttpServletRequest request) {
        String action = WebUtil.getRelativeRequestAction(request);
        if (action.startsWith(REQUEST_URL_PREFIX)) {
            String loginMode = action.substring(REQUEST_URL_PREFIX.length());
            int index = loginMode.indexOf(Strings.SLASH);
            if (index > 0) {
                loginMode = loginMode.substring(0, index);
            }
            return loginMode;
        }
        return null;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        String loginMode = getLoginMode(request);
        WechatAuthenticationTokenResolver tokenResolver = this.tokenResolverMapping.get(loginMode);
        if (tokenResolver == null) {
            throw new AuthenticationServiceException("The " + WechatAuthenticationTokenResolver.class.getSimpleName() +
                    " for loginMode('" + loginMode + "') does not exist.");
        }
        try {
            WechatAuthenticationToken authRequest = tokenResolver.resolveAuthenticationToken(request);
            setDetails(request, authRequest);
            return getAuthenticationManager().authenticate(authRequest);
        } catch (BusinessException e) {
            throw new BusinessAuthenticationException(e);
        }
    }

    protected void setDetails(HttpServletRequest request, WechatAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }

}
