package org.truenewx.tnxjeex.notice.service.sms;

import org.truenewx.tnxjeex.notice.model.sms.SmsSendResult;

/**
 * 短信发送回调
 *
 * @author jianglei
 */
public interface SmsSendCallback {
    /**
     * 短信发送完成后被调用，通知发送结果
     *
     * @param result 短信发送结果
     */
    void onSmsSent(SmsSendResult result);
}
