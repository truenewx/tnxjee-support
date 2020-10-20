package org.truenewx.tnxjeex.openapi.client.service;

import org.truenewx.tnxjee.service.impl.ServiceSupport;
import org.truenewx.tnxjee.service.transaction.annotation.WriteTransactional;
import org.truenewx.tnxjeex.openapi.client.model.WechatEventMessage;
import org.truenewx.tnxjeex.openapi.client.model.WechatEventType;
import org.truenewx.tnxjeex.openapi.client.model.WechatMessage;
import org.truenewx.tnxjeex.openapi.client.model.WechatMessageType;

/**
 * 微信开放接口事件消息同步处理器
 *
 * @author jianglei
 */
public abstract class WechatEventMessageSyncHandler extends ServiceSupport
        implements WechatMessageSyncHandler {

    @Override
    public final WechatMessageType getMessageType() {
        return WechatMessageType.EVENT;
    }

    @WriteTransactional
    public WechatMessage handleMessage(WechatMessage message) {
        WechatEventMessage eventMessage = (WechatEventMessage) message;
        if (eventMessage.getEventType() == getEventType()) {
            return doHandleMessage(eventMessage);
        }
        return null;
    }

    protected abstract WechatEventType getEventType();

    protected abstract WechatMessage doHandleMessage(WechatEventMessage message);

}
