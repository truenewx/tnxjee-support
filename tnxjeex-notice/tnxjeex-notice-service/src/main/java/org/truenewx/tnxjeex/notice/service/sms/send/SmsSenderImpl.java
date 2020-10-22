package org.truenewx.tnxjeex.notice.service.sms.send;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.truenewx.notice.model.sms.SmsSendResult;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjeex.notice.service.sms.SmsContentProvider;

/**
 * 短信发送器实现
 *
 * @author jianglei
 */
public class SmsSenderImpl implements SmsSender {
    private Map<String, SmsContentSender> contentSenders = new HashMap<>();
    private Map<String, SmsContentProvider> contentProviders = new HashMap<>();
    private boolean disabled;

    public void setContentSenders(Map<String, SmsContentSender> contentSenders) {
        this.contentSenders = contentSenders;
    }

    public void setContentProviders(List<SmsContentProvider> contentProviders) {
        for (SmsContentProvider contentProvider : contentProviders) {
            this.contentProviders.put(contentProvider.getType(), contentProvider);
        }
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    private SmsContentProvider getContentProvider(String type) {
        if (this.disabled) { // 禁用时始终返回空的内容提供者，以实际控制不发送短信
            return null;
        }
        SmsContentProvider contentSender = this.contentProviders.get(type);
        if (contentSender == null) {
            contentSender = this.contentProviders.get(Strings.ASTERISK); // 默认内容提供者
        }
        return contentSender;
    }

    private SmsContentSender getContentSender(String type) {
        SmsContentSender contentSender = this.contentSenders.get(type);
        if (contentSender == null) {
            contentSender = this.contentSenders.get(Strings.ASTERISK); // 默认内容发送器
        }
        return contentSender;
    }

    @Override
    public SmsSendResult send(String type, Map<String, Object> params, Locale locale, String... mobilePhones) {
        SmsContentProvider contentProvider = getContentProvider(type);
        if (contentProvider != null) {
            String content = contentProvider.getContent(params, locale);
            if (content != null) {
                SmsContentSender contentSender = getContentSender(type);
                if (contentSender != null) {
                    return contentSender.send(content, contentProvider.getMaxCount(), locale, mobilePhones);
                }
            }
        }
        return null;
    }

    @Override
    public void send(String type, Map<String, Object> params, Locale locale, String[] mobilePhones,
            SmsSendCallback callback) {
        SmsContentProvider contentProvider = getContentProvider(type);
        if (contentProvider != null) {
            String content = contentProvider.getContent(params, locale);
            if (content != null) {
                SmsContentSender contentSender = getContentSender(type);
                if (contentSender != null) {
                    contentSender.send(content, contentProvider.getMaxCount(), locale, mobilePhones, callback);
                }
            }
        }
    }
}
