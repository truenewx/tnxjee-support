package org.truenewx.tnxjeex.notice.service.sms.content.impl;

import java.util.Date;
import java.util.Map;

import org.truenewx.tnxjee.core.util.LogUtil;
import org.truenewx.tnxjeex.notice.model.sms.SmsModel;
import org.truenewx.tnxjeex.notice.model.sms.SmsNotifyResult;
import org.truenewx.tnxjeex.notice.service.sms.content.AbstractSmsContentSender;

import com.yunpian.sdk.YunpianClient;
import com.yunpian.sdk.model.Result;
import com.yunpian.sdk.model.SmsSingleSend;

/**
 * 云片短信内容发送器
 *
 * @author jianglei
 */
public class YunPianSmsContentProvider extends AbstractSmsContentSender {

    private String apiKey;

    @Override
    public SmsNotifyResult send(String signName, String content, int maxCount, String... mobilePhones) {
        SmsModel sms = new SmsModel();
        sms.setMobilePhones(mobilePhones);
        sms.setSendTime(new Date());
        SmsNotifyResult result = new SmsNotifyResult(sms);
        YunpianClient client = new YunpianClient(this.apiKey).init();
        Map<String, String> params = client.newParam(2);
        StringBuffer msg = new StringBuffer("【");
        msg.append(signName);
        msg.append("】").append(content);
        for (String mobilePhone : mobilePhones) {
            params.put(YunpianClient.MOBILE, mobilePhone);
            params.put(YunpianClient.TEXT, msg.toString());
            try {
                Result<SmsSingleSend> sendResult = client.sms().single_send(params);
                if (sendResult.getCode() != 0) {
                    result.getFailures().put(sendResult.getMsg(), mobilePhone);
                }
            } catch (Exception e) {
                LogUtil.error(getClass(), e);
                result.getFailures().put(e.getMessage(), mobilePhone);
            }
        }
        client.close();
        return result;
    }

}
