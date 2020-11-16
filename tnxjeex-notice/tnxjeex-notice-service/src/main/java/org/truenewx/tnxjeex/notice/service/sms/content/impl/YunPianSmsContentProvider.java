package org.truenewx.tnxjeex.notice.service.sms.content.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.truenewx.tnxjee.core.util.LogUtil;
import org.truenewx.tnxjeex.notice.model.sms.SmsModel;
import org.truenewx.tnxjeex.notice.model.sms.SmsSendResult;
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
    public SmsSendResult send(String signName, String content, int maxCount, String... mobilePhones) {
        SmsModel sms = new SmsModel();
        sms.setMobilePhones(mobilePhones);
        sms.setSendTime(new Date());
        SmsSendResult result = new SmsSendResult(sms);
        YunpianClient client = new YunpianClient(this.apiKey).init();
        Map<String, String> params = client.newParam(2);
        StringBuffer msg = new StringBuffer("【");
        msg.append(signName);
        msg.append("】").append(content);
        List<String> failures = new ArrayList<>();
        try {
            for (String phone : mobilePhones) {
                params.put(YunpianClient.MOBILE, phone);
                params.put(YunpianClient.TEXT, msg.toString());
                Result<SmsSingleSend> sendResult = client.sms().single_send(params);
                if (sendResult.getCode() != 0) {
                    failures.add(phone);
                }
            }
            if (CollectionUtils.isNotEmpty(failures)) {
                String failPhones[] = failures.toArray(new String[]{});
                result.addFailures(failPhones);
            }
        } catch (Exception e) {
            LogUtil.error(getClass(), e);
        } finally {
            client.close();
        }
        return result;
    }

}
