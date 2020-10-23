package org.truenewx.tnxjeex.notice.service.sms.yunpian;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.truenewx.tnxjeex.notice.model.sms.SmsModel;
import org.truenewx.tnxjeex.notice.model.sms.SmsSendResult;
import org.truenewx.tnxjee.core.util.concurrent.DefaultThreadPoolExecutor;
import org.truenewx.tnxjeex.notice.service.sms.send.SmsContentSender;
import org.truenewx.tnxjeex.notice.service.sms.send.SmsSendCallback;

import com.yunpian.sdk.YunpianClient;
import com.yunpian.sdk.model.Result;
import com.yunpian.sdk.model.SmsSingleSend;

/**
 * 云片短信内容发送器
 *
 * @author jianglei
 */
public class YunPianSmsContentProvider implements SmsContentSender {

    private Executor executor = new DefaultThreadPoolExecutor(4, 8);
    private Map<String, String> signName;
    private String apiKey;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public SmsSendResult send(String content, int maxCount, Locale locale, String... mobilePhones) {
        return this.send(content, locale, mobilePhones);
    }

    private SmsSendResult send(String content, Locale locale, String... mobilePhones) {
        SmsModel sms = new SmsModel();
        sms.setMobilePhones(mobilePhones);
        sms.setSendTime(new Date());
        SmsSendResult result = new SmsSendResult(sms);
        YunpianClient clnt = new YunpianClient(this.apiKey).init();
        Map<String, String> param = clnt.newParam(2);
        StringBuffer msg = new StringBuffer("【");
        msg.append(this.signName.get(locale.toString()));
        msg.append("】").append(content);
        List<String> failures = new ArrayList<>();
        try {
            for (String phone : mobilePhones) {
                param.put(YunpianClient.MOBILE, phone);
                param.put(YunpianClient.TEXT, msg.toString());
                Result<SmsSingleSend> clntResult = clnt.sms().single_send(param);
                if (clntResult.getCode() != 0) {
                    failures.add(phone);
                }
            }
            if (CollectionUtils.isNotEmpty(failures)) {
                String failPhones[] = failures.toArray(new String[] {});
                result.addFailures(failPhones);
            }
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
        } finally {
            clnt.close();
        }
        return result;
    }

    @Override
    public void send(String content, int maxCount, Locale locale, String[] mobilePhones, SmsSendCallback callback) {
        this.executor.execute(new SendCommand(content, locale, mobilePhones, callback));
    }

    public String getApiKey() {
        return this.apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
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
            SmsSendResult result = YunPianSmsContentProvider.this.send(this.content, this.locale, this.mobilePhones);
            this.callback.onSmsSent(result);
        }
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public Map<String, String> getSignName() {
        return this.signName;
    }

    public void setSignName(Map<String, String> signName) {
        this.signName = signName;
    }

}
