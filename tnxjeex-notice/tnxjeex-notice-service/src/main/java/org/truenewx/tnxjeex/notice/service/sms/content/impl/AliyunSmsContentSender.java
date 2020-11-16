package org.truenewx.tnxjeex.notice.service.sms.content.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.truenewx.tnxjeex.notice.model.sms.SmsModel;
import org.truenewx.tnxjeex.notice.model.sms.SmsSendResult;
import org.truenewx.tnxjeex.notice.service.sms.content.AbstractSmsContentSender;
import org.truenewx.tnxjeex.openapi.client.service.aliyun.AliyunSmsAccessor;

/**
 * 阿里云的短信内容发送器
 *
 * @author jianglei
 */
public class AliyunSmsContentSender extends AbstractSmsContentSender {

    private AliyunSmsAccessor smsAccessor;
    private String templateCode;

    @Autowired
    public void setSmsAccessor(AliyunSmsAccessor smsAccessor) {
        this.smsAccessor = smsAccessor;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    @Override
    public SmsSendResult send(String signName, String content, int maxCount, String... mobilePhones) {
        SmsModel sms = new SmsModel();
        sms.setMobilePhones(mobilePhones);
        sms.setSendTime(new Date());
        SmsSendResult result = new SmsSendResult(sms);
        if (this.smsAccessor.send(signName, this.templateCode, content, mobilePhones)) {
            return result;
        }
        result.addFailures(mobilePhones);
        return result;
    }


}
