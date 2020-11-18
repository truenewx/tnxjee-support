package org.truenewx.tnxjeex.openapi.client.web.security.authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.core.util.SpringUtil;
import org.truenewx.tnxjee.webmvc.security.authentication.OAuth2ClientAuthenticationToken;
import org.truenewx.tnxjee.webmvc.security.core.BusinessAuthenticationException;
import org.truenewx.tnxjee.webmvc.security.web.authentication.AbstractLoginProcessingFilter;
import org.truenewx.tnxjeex.openapi.client.model.wechat.WechatUser;
import org.truenewx.tnxjeex.openapi.client.model.wechat.WechatUserDetail;
import org.truenewx.tnxjeex.openapi.client.service.wechat.WechatUserDetailRequiredPredicate;
import org.truenewx.tnxjeex.openapi.client.service.wechat.WechatWebAccessor;

/**
 * 微信登录进程过滤器
 */
public class WechatLoginProcessingFilter extends AbstractLoginProcessingFilter {

    private WechatWebAccessor webAccessor;
    private WechatUserDetailRequiredPredicate userDetailRequiredPredicate;

    public WechatLoginProcessingFilter(String defaultFilterProcessesUrl, ApplicationContext context) {
        super(defaultFilterProcessesUrl, context);
        this.webAccessor = context.getBean(WechatWebAccessor.class);
        this.userDetailRequiredPredicate = SpringUtil
                .getFirstBeanByClass(context, WechatUserDetailRequiredPredicate.class);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        // 从state参数中解析service和scope放入请求属性中，以便于后续处理使用
        String state = request.getParameter("state");
        if (StringUtils.isNotBlank(state) && !"undefined".equals(state)) {
            String[] params = state.split(Strings.SEMICOLON);
            for (String param : params) {
                String[] array = param.split(Strings.COLON);
                if (array.length == 2) {
                    String name = array[0];
                    String value = array[1];
                    request.setAttribute(name, value);
                }
            }
        }
        String loginCode = request.getParameter("code");
        WechatUser user = this.webAccessor.getUser(loginCode);
        if (user == null) { // 无效的微信登录编码
            throw new BusinessAuthenticationException("error.openapi.client.invalid_login_code");
        }
        // 需要更多用户细节，则从微信服务器获取用户细节
        if (this.userDetailRequiredPredicate != null && this.userDetailRequiredPredicate.requiresDetail(user)) {
            WechatUserDetail userDetail = this.webAccessor.getUserDetail(user.getOpenId(), user.getAccessToken());
            if (userDetail != null) {
                user = userDetail;
            }
        }
        OAuth2ClientAuthenticationToken authRequest = new OAuth2ClientAuthenticationToken(user, loginCode);
        setDetails(request, authRequest);
        return getAuthenticationManager().authenticate(authRequest);
    }

}
