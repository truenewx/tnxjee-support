package org.truenewx.tnxjeex.notice.service.sms;

import java.util.Locale;
import java.util.Map;

import org.truenewx.tnxjeex.notice.model.sms.SmsSendResult;

/**
 * 短信发送器
 *
 * @author jianglei
 */
public interface SmsSender {
    /**
     * 同步发送指定类型的短信
     *
     * @param type         业务类型
     * @param params       参数映射集
     * @param locale       区域
     * @param mobilePhones 手机号码清单
     * @return 发送结果
     */
    SmsSendResult send(String type, Map<String, Object> params, Locale locale, String... mobilePhones);

    /**
     * 异步发送指定类型的短信
     *
     * @param type         业务类型
     * @param params       参数映射集
     * @param locale       区域
     * @param mobilePhones 手机号码清单
     * @param callback     短信发送回调
     */
    void send(String type, Map<String, Object> params, Locale locale, String[] mobilePhones, SmsSendCallback callback);

    /**
     * 获取指定业务类型下，对指定手机号码再次发送短信的剩余时间秒数
     *
     * @param type        业务类型
     * @param mobilePhone 手机号码
     * @return 剩余时间秒数
     */
    int getRemainingSeconds(String type, String mobilePhone);

}
