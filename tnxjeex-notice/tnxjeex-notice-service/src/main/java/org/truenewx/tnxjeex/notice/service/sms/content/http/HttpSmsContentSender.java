package org.truenewx.tnxjeex.notice.service.sms.content.http;

import java.util.*;

import org.apache.commons.lang3.ArrayUtils;
import org.truenewx.tnxjee.core.spec.HttpRequestMethod;
import org.truenewx.tnxjee.core.util.HttpClientUtil;
import org.truenewx.tnxjee.core.util.LogUtil;
import org.truenewx.tnxjee.core.util.tuple.Binate;
import org.truenewx.tnxjeex.notice.model.sms.SmsModel;
import org.truenewx.tnxjeex.notice.model.sms.SmsNotifyResult;
import org.truenewx.tnxjeex.notice.service.sms.content.SplitableSmsContentSender;

/**
 * HTTP方式的短信内容发送器
 *
 * @author jianglei
 */
public class HttpSmsContentSender extends SplitableSmsContentSender {

    private HttpSmsContentSendStrategy strategy;

    /**
     * @param strategy 短信内容发送策略
     */
    public void setStrategy(HttpSmsContentSendStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    protected SmsNotifyResult send(String signName, List<String> contents, String... mobilePhones) {
        SmsModel sms = new SmsModel();
        sms.setContents(contents);
        sms.setMobilePhones(mobilePhones);
        sms.setSendTime(new Date());
        SmsNotifyResult result = new SmsNotifyResult(sms);
        try {
            if (this.strategy.isBatchable()) { // 支持批量
                Map<String, String> failures = send(contents, -1, mobilePhones);
                if (failures != null && !failures.isEmpty()) {
                    result.getFailures().putAll(failures);
                }
            } else {
                for (int i = 0; i < contents.size(); i++) {
                    Map<String, String> failures = send(contents, i, mobilePhones);
                    if (failures != null && !failures.isEmpty()) {
                        result.getFailures().putAll(failures);
                        // 一次发送失败的手机号码不再发送
                        String[] failureArray = failures.keySet().toArray(new String[0]);
                        mobilePhones = ArrayUtils.removeElements(mobilePhones, failureArray);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(getClass(), e);
            result.addFailures(e.getMessage(), mobilePhones);
        }
        return result;
    }

    private Map<String, String> send(List<String> contents, int index, String... mobilePhones) throws Exception {
        Set<String> mobilePhoneSet = new HashSet<>();
        for (String mobilePhone : mobilePhones) {
            if (this.strategy.isValid(mobilePhone)) {
                mobilePhoneSet.add(mobilePhone);
            }
        }
        Map<String, Object> params = this.strategy.getParams(contents, index, mobilePhoneSet);
        HttpRequestMethod method = this.strategy.getRequestMethod();
        if (method == null) {
            method = HttpRequestMethod.POST;
        }
        Binate<Integer, String> binate = HttpClientUtil.request(this.strategy.getUrl(), params, method,
                this.strategy.getEncoding());
        if (binate != null) {
            return this.strategy.getFailures(binate.getLeft(), binate.getRight());
        }
        return null;
    }

}