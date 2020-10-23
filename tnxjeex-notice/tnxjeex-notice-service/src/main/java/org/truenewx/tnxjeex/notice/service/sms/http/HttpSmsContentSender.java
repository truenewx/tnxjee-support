package org.truenewx.tnxjeex.notice.service.sms.http;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.LoggerFactory;
import org.truenewx.tnxjeex.notice.model.sms.SmsModel;
import org.truenewx.tnxjeex.notice.model.sms.SmsSendResult;
import org.truenewx.tnxjee.core.spec.HttpRequestMethod;
import org.truenewx.tnxjee.core.util.HttpClientUtil;
import org.truenewx.tnxjee.core.util.tuple.Binate;
import org.truenewx.tnxjeex.notice.service.sms.send.AbstractSmsContentSender;

/**
 * HTTP方式的短信内容发送器
 *
 * @author jianglei
 */
public class HttpSmsContentSender extends AbstractSmsContentSender {

    private HttpSmsSendStrategy strategy;

    /**
     * @param strategy 短信发送策略
     */
    public void setStrategy(HttpSmsSendStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    protected SmsSendResult send(List<String> contents, Locale locale, String... mobilePhones) {
        SmsModel sms = new SmsModel();
        sms.setContents(contents);
        sms.setMobilePhones(mobilePhones);
        sms.setSendTime(new Date());
        SmsSendResult result = new SmsSendResult(sms);
        try {
            if (this.strategy.isBatchable()) { // 支持批量
                Set<String> failures = send(contents, -1, mobilePhones);
                if (failures != null && !failures.isEmpty()) {
                    result.getFailures().addAll(failures);
                }
            } else {
                for (int i = 0; i < contents.size(); i++) {
                    Set<String> failures = send(contents, i, mobilePhones);
                    if (failures != null && !failures.isEmpty()) {
                        result.getFailures().addAll(failures);
                        // 一次发送失败的手机号码不再发送
                        String[] failureArray = failures.toArray(new String[failures.size()]);
                        mobilePhones = ArrayUtils.removeElements(mobilePhones, failureArray);
                    }
                }
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
            result.addFailures(mobilePhones);
        }
        return result;
    }

    private Set<String> send(List<String> contents, int index, String... mobilePhones)
            throws UnsupportedEncodingException, Exception, URISyntaxException {
        Set<String> mobilePhoneSet = new HashSet<>();
        for (String mobilePhone : mobilePhones) {
            if (this.strategy.isValid(mobilePhone)) {
                mobilePhoneSet.add(mobilePhone);
            }
        }
        Map<String, Object> params = this.strategy.getParams(contents, index, mobilePhoneSet);
        HttpRequestMethod method = EnumUtils.getEnum(HttpRequestMethod.class, this.strategy.getRequestMethod());
        if (method == null) {
            method = HttpRequestMethod.GET;
        }
        Binate<Integer, String> binate = HttpClientUtil.request(this.strategy.getUrl(), params, method,
                this.strategy.getEncoding());
        return this.strategy.getFailures(binate.getLeft(), binate.getRight());
    }

}
