package org.truenewx.tnxjeex.notice.service.sms.send;

import java.util.Locale;

import org.truenewx.tnxjeex.notice.model.sms.SmsSendResult;

/**
 * 短信内容发送器<br/>
 * 仅负责发送提供的短信内容，不管内容如何生成
 *
 * @author jianglei
 */
public interface SmsContentSender {
    /**
     * 同步发送短信
     *
     * @param content      短信内容
     * @param maxCount     内容拆分的最大条数
     * @param locale       语言区域
     * @param mobilePhones 手机号码清单
     * @return 短信发送结果
     */
    SmsSendResult send(String content, int maxCount, Locale locale, String... mobilePhones);

    /**
     * 异步发送短信
     *
     * @param content      短信内容
     * @param maxCount     内容拆分的最大条数
     * @param locale       语言区域
     * @param mobilePhones 手机号码清单
     * @param callback     短信发送回调
     */
    void send(String content, int maxCount, Locale locale, String[] mobilePhones, SmsSendCallback callback);
}
