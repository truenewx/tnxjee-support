package org.truenewx.tnxjeex.openapi.client.service;

import java.util.function.Predicate;

import org.truenewx.tnxjeex.openapi.client.model.WechatUser;

/**
 * 是否需要获取微信用户细节的断言
 */
public interface WechatUserDetailRequiredPredicate extends Predicate<WechatUser> {
}
