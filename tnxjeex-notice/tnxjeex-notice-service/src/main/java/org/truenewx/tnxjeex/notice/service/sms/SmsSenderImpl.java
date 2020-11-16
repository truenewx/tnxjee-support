package org.truenewx.tnxjeex.notice.service.sms;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.core.beans.ContextInitializedBean;
import org.truenewx.tnxjeex.notice.model.sms.SmsSendResult;
import org.truenewx.tnxjeex.notice.service.sms.content.SmsContentProvider;
import org.truenewx.tnxjeex.notice.service.sms.content.SmsContentSender;

/**
 * 短信发送器实现
 *
 * @author jianglei
 */
@Component
public class SmsSenderImpl implements SmsSender, ContextInitializedBean {

    private Map<String, SmsContentProvider> contentProviders = new HashMap<>();
    private Map<String, SmsContentSender> contentSenders = new HashMap<>();
    @Autowired
    private Executor executor;
    @Value("${tnxjeex.notice.sms.disabled}")
    private boolean disabled;

    @Override
    public void afterInitialized(ApplicationContext context) throws Exception {
        context.getBeansOfType(SmsContentProvider.class).forEach((id, provider) -> {
            String[] types = provider.getTypes();
            for (String type : types) {
                Assert.isNull(this.contentProviders.put(type, provider), () -> {
                    return "There is more than one SmsContentProvider for type '" + type + "'";
                });
            }
        });

        context.getBeansOfType(SmsContentSender.class).forEach((id, sender) -> {
            String[] types = sender.getTypes();
            for (String type : types) {
                Assert.isNull(this.contentSenders.put(type, sender), () -> {
                    return "There is more than one SmsContentSender for type '" + type + "'";
                });
            }
        });
    }

    private SmsContentProvider getContentProvider(String type) {
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
        if (!this.disabled) {
            SmsContentProvider contentProvider = getContentProvider(type);
            if (contentProvider != null) {
                String content = contentProvider.getContent(params, locale);
                if (content != null) {
                    SmsContentSender contentSender = getContentSender(type);
                    if (contentSender != null) {
                        String signName = contentProvider.getSignName(locale);
                        int maxCount = contentProvider.getMaxCount();
                        return contentSender.send(signName, content, maxCount, mobilePhones);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void send(String type, Map<String, Object> params, Locale locale, String[] mobilePhones,
            SmsSendCallback callback) {
        this.executor.execute(() -> {
            SmsSendResult result = send(type, params, locale, mobilePhones);
            callback.onSmsSent(result);
        });
    }

}
