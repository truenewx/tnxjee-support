package org.truenewx.tnxjeex.openapi.client.service;

import org.truenewx.tnxjeex.openapi.client.model.WechatMessage;

/**
 * 微信开放接口消息侦听器
 *
 * @author jianglei
 */
public interface WechatMessageListener {

    WechatMessage onReceived(WechatMessage message) throws NoSuchMessageHandlerException;

}
