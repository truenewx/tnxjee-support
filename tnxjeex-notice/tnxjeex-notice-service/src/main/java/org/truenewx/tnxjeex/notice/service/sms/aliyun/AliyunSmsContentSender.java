package org.truenewx.tnxjeex.notice.service.sms.aliyun;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.truenewx.notice.model.sms.SmsModel;
import org.truenewx.notice.model.sms.SmsSendResult;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjeex.notice.service.sms.send.SmsContentSender;
import org.truenewx.tnxjeex.notice.service.sms.send.SmsSendCallback;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.sms.model.v20160927.SingleSendSmsRequest;

/**
 * 阿里云的短信内容发送器
 *
 * @author jianglei
 */
public class AliyunSmsContentSender implements SmsContentSender {

    private Executor executor;
    private String regionId;
    private String accessKey;
    private String accessSecret;
    private String freeSignName;
    private String templateCode;

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    /**
     * @param accessKey 访问Key
     */
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    /**
     * @param accessSecret 访问Secret
     */
    public void setAccessSecret(String accessSecret) {
        this.accessSecret = accessSecret;
    }

    /**
     * @param freeSignName 短信签名
     */
    public void setFreeSignName(String freeSignName) {
        this.freeSignName = freeSignName;
    }

    /**
     * @param templateCode 模板编号
     */
    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    /**
     * @param regionId 地区编号
     */
    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    @Override
    public SmsSendResult send(String content, int maxCount, Locale locale, String... mobilePhones) {
        return send(content, locale, mobilePhones);
    }

    @Override
    public void send(String content, int maxCount, Locale locale, String[] mobilePhones, SmsSendCallback callback) {
        this.executor.execute(new SendCommand(content, locale, mobilePhones, callback));
    }

    private SmsSendResult send(String content, Locale locale, String... mobilePhones) {
        SmsModel sms = new SmsModel();
        sms.setMobilePhones(mobilePhones);
        sms.setSendTime(new Date());
        SmsSendResult result = new SmsSendResult(sms);
        try {
            IClientProfile profile = DefaultProfile.getProfile(this.regionId, this.accessKey, this.accessSecret);
            DefaultProfile.addEndpoint(this.regionId, this.regionId, "Sms", "sms.aliyuncs.com");
            IAcsClient client = new DefaultAcsClient(profile);
            SingleSendSmsRequest request = new SingleSendSmsRequest();
            request.setSignName(this.freeSignName);
            request.setTemplateCode(this.templateCode);
            request.setParamString(content);
            request.setRecNum(StringUtils.join(mobilePhones, Strings.COMMA));
            client.getAcsResponse(request);
        } catch (ClientException e) {
            result.addFailures(mobilePhones);
            LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
        }
        return result;
    }

    private class SendCommand implements Runnable {
        private String content;
        private Locale locale;
        private String[] mobilePhones;
        private SmsSendCallback callback;

        public SendCommand(String content, Locale locale, String[] mobilePhones, SmsSendCallback callback) {
            this.content = content;
            this.locale = locale;
            this.mobilePhones = mobilePhones;
            this.callback = callback;
        }

        @Override
        public void run() {
            SmsSendResult result = send(this.content, this.locale, this.mobilePhones);
            this.callback.onSmsSent(result);
        }
    }

}
