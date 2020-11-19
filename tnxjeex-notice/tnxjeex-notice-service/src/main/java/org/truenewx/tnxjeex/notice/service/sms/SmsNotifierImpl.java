package org.truenewx.tnxjeex.notice.service.sms;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.core.beans.ContextInitializedBean;
import org.truenewx.tnxjee.core.util.StringUtil;
import org.truenewx.tnxjeex.notice.model.sms.SmsNotifyResult;
import org.truenewx.tnxjeex.notice.service.sms.content.SmsContentProvider;
import org.truenewx.tnxjeex.notice.service.sms.content.SmsContentSender;

/**
 * 短信发送器实现
 *
 * @author jianglei
 */
@Component
public class SmsNotifierImpl implements SmsNotifier, ContextInitializedBean {

    private Map<String, SmsContentProvider> contentProviders = new HashMap<>();
    private Map<String, SmsContentSender> contentSenders = new HashMap<>();
    private Map<String, Instant> sendableInstants = new HashMap<>(); // 可发送的时刻映射集
    @Autowired
    private MessageSource messageSource;
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
    public SmsNotifyResult notify(String type, Map<String, Object> params, Locale locale, String... mobilePhones) {
        SmsContentProvider contentProvider = getContentProvider(type);
        if (contentProvider != null) {
            String content = contentProvider.getContent(params, locale);
            if (content != null) {
                SmsContentSender contentSender = getContentSender(type);
                if (contentSender != null) {
                    // 检查获取因时限不可发送的手机号码
                    List<String> notMobilePhones = new ArrayList<>();
                    List<String> unsendableMobilePhones = new ArrayList<>();
                    if (mobilePhones.length == 1) { // 只有一个手机号码的，快速处理
                        String mobilePhone = mobilePhones[0];
                        if (!StringUtil.isMobilePhone(mobilePhone)) {
                            notMobilePhones.add(mobilePhone);
                            mobilePhones = new String[0];
                        } else if (getRemainingSeconds(contentSender, mobilePhone) > 0) {
                            unsendableMobilePhones.add(mobilePhone);
                            mobilePhones = new String[0];
                        }
                    } else { // 多个手机号码的，循环处理
                        List<String> sendableMobilePhones = new ArrayList<>();
                        for (String mobilePhone : mobilePhones) {
                            if (!StringUtil.isMobilePhone(mobilePhone)) {
                                notMobilePhones.add(mobilePhone);
                            } else if (getRemainingSeconds(contentSender, mobilePhone) > 0) {
                                unsendableMobilePhones.add(mobilePhone);
                            } else {
                                sendableMobilePhones.add(mobilePhone);
                            }
                        }
                        if (notMobilePhones.size() + unsendableMobilePhones.size() > 0) {
                            mobilePhones = sendableMobilePhones.toArray(new String[0]);
                        }
                    }

                    String signName = contentProvider.getSignName(locale);
                    int maxCount = contentProvider.getMaxCount();
                    SmsNotifyResult result = contentSender
                            .send(signName, content, maxCount, this.disabled ? new String[0] : mobilePhones);
                    putSendableInstants(contentSender, mobilePhones);
                    // 添加不是手机号码的错误
                    notMobilePhones.forEach(mobilePhone -> {
                        Object[] args = { mobilePhone };
                        String errorMessage = this.messageSource
                                .getMessage("error.notice.sms.invalid_mobile_phone", args, locale);
                        result.addFailures(errorMessage, mobilePhone);
                    });
                    // 添加因时限不能发送的错误
                    unsendableMobilePhones.forEach(mobilePhone -> {
                        Object[] args = { getRemainingSeconds(contentSender, mobilePhone) };
                        String errorMessage = this.messageSource
                                .getMessage("error.notice.sms.interval_limited", args, locale);
                        result.addFailures(errorMessage, mobilePhone);
                    });
                    return result;
                }
            }
        }
        return null;
    }

    private int getRemainingSeconds(SmsContentSender contentSender, String mobilePhone) {
        String key = contentSender.toString() + Strings.MINUS + mobilePhone;
        Instant instant = this.sendableInstants.get(key);
        if (instant == null) { // 没有约束，剩余时间为0
            return 0;
        }
        // 计算限定时间与当前时间的秒数差
        long millis = instant.toEpochMilli() - System.currentTimeMillis();
        if (millis <= 0) { // 约束时间已过，则从缓存移除
            this.sendableInstants.remove(key);
            return 0;
        }
        return (int) (millis / 1000 + (millis % 1000 == 0 ? 0 : 1));
    }

    private void putSendableInstants(SmsContentSender contentSender, String... mobilePhones) {
        String prefix = contentSender.toString() + Strings.MINUS;
        Instant instant = Instant.now().plusSeconds(contentSender.getIntervalSeconds());
        for (String mobilePhone : mobilePhones) {
            String key = prefix + mobilePhone;
            this.sendableInstants.put(key, instant);
        }
    }

    @Override
    public void notify(String type, Map<String, Object> params, Locale locale, String[] mobilePhones,
            Consumer<SmsNotifyResult> callback) {
        this.executor.execute(() -> {
            SmsNotifyResult result = notify(type, params, locale, mobilePhones);
            callback.accept(result);
        });
    }

    @Override
    public int getRemainingSeconds(String type, String mobilePhone) {
        SmsContentSender contentSender = getContentSender(type);
        if (contentSender != null) {
            return getRemainingSeconds(contentSender, mobilePhone);
        }
        return -1;
    }

}
