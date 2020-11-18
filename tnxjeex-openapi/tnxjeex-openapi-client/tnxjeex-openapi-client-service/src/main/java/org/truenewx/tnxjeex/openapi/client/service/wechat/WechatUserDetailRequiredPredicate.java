package org.truenewx.tnxjeex.openapi.client.service.wechat;

import org.truenewx.tnxjeex.openapi.client.model.wechat.WechatUser;

/**
 * 是否需要获取微信用户细节的断言
 */
public interface WechatUserDetailRequiredPredicate {

    boolean requiresDetail(WechatUser user);

}
