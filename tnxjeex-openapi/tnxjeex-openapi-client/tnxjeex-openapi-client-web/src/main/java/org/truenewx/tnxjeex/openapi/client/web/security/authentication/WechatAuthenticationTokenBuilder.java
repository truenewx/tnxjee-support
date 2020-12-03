package org.truenewx.tnxjeex.openapi.client.web.security.authentication;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.truenewx.tnxjee.core.util.EncryptUtil;
import org.truenewx.tnxjee.core.util.JsonUtil;
import org.truenewx.tnxjee.service.exception.BusinessException;
import org.truenewx.tnxjee.webmvc.security.web.authentication.AbstractAuthenticationTokenBuilder;
import org.truenewx.tnxjeex.openapi.client.model.wechat.WechatUser;
import org.truenewx.tnxjeex.openapi.client.service.wechat.WechatWebAccessor;

/**
 * 微信登录认证令牌构建器
 */
public class WechatAuthenticationTokenBuilder extends
        AbstractAuthenticationTokenBuilder<WechatAuthenticationToken> {

    @Autowired
    private WechatWebAccessor webAccessor;

    public WechatAuthenticationTokenBuilder(String loginMode) {
        super(loginMode);
    }

    public Map<String, Object> resolveState(HttpServletRequest request) {
        String state = request.getParameter("state");
        if (StringUtils.isNotBlank(state) && !"undefined".equals(state)) {
            state = EncryptUtil.decryptByBase64(state);
            return JsonUtil.json2Map(state);
        }
        return Collections.emptyMap();
    }

    public WechatUser resolveUser(HttpServletRequest request) {
        String loginCode = request.getParameter("code");
        WechatUser user = this.webAccessor.getUser(loginCode);
        if (user == null) { // 无效的微信登录编码
            throw new BusinessException("error.openapi.client.invalid_login_code");
        }
        return user;
    }

    @Override
    public WechatAuthenticationToken buildAuthenticationToken(HttpServletRequest request) {
        // 从state参数中解析service和scope放入请求属性中，以便于后续处理使用
        resolveState(request).forEach(request::setAttribute);
        WechatUser user = resolveUser(request);
        return new WechatAuthenticationToken(user);
    }

}
