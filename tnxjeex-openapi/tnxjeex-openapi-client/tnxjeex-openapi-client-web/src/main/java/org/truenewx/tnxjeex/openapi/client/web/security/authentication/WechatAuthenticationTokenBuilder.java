package org.truenewx.tnxjeex.openapi.client.web.security.authentication;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.webmvc.security.core.BusinessAuthenticationException;
import org.truenewx.tnxjee.webmvc.security.web.authentication.AbstractAuthenticationTokenBuilder;
import org.truenewx.tnxjeex.openapi.client.model.wechat.WechatUser;
import org.truenewx.tnxjeex.openapi.client.model.wechat.WechatUserDetail;
import org.truenewx.tnxjeex.openapi.client.service.wechat.WechatUserDetailRequiredPredicate;
import org.truenewx.tnxjeex.openapi.client.service.wechat.WechatWebAccessor;

/**
 * 微信登录认证令牌构建器
 */
public class WechatAuthenticationTokenBuilder extends
        AbstractAuthenticationTokenBuilder<WechatAuthenticationToken> {

    @Autowired
    private WechatWebAccessor webAccessor;
    @Autowired
    private WechatUserDetailRequiredPredicate userDetailRequiredPredicate;

    public WechatAuthenticationTokenBuilder(String loginMode) {
        super(loginMode);
    }

    public Map<String, String> parseState(HttpServletRequest request) {
        Map<String, String> states = new HashMap<>();
        String state = request.getParameter("state");
        if (StringUtils.isNotBlank(state) && !"undefined".equals(state)) {
            String[] params = state.split(Strings.SEMICOLON);
            for (String param : params) {
                String[] array = param.split(Strings.COLON);
                if (array.length == 2) {
                    String name = array[0];
                    String value = array[1];
                    states.put(name, value);
                }
            }
        }
        return states;
    }

    @Override
    public WechatAuthenticationToken buildAuthenticationToken(HttpServletRequest request) {
        // 从state参数中解析service和scope放入请求属性中，以便于后续处理使用
        parseState(request).forEach(request::setAttribute);

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
        return new WechatAuthenticationToken(user, loginCode);
    }

}
