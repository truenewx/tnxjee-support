package org.truenewx.tnxjeex.notice.service.sms.content;

import org.truenewx.tnxjeex.notice.model.sms.SmsSendResult;

/**
 * 短信内容发送器<br/>
 * 仅负责发送提供的短信内容，不管内容如何生成
 *
 * @author jianglei
 */
public interface SmsContentSender {

    /**
     * @return 支持的业务类型集合
     */
    String[] getTypes();

    /**
     * 发送短信
     *
     * @param signName     签名
     * @param content      短信内容
     * @param maxCount     内容拆分的最大条数
     * @param mobilePhones 手机号码清单
     * @return 短信发送结果
     */
    SmsSendResult send(String signName, String content, int maxCount, String... mobilePhones);

}
