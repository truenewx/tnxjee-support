package org.truenewx.tnxjeex.notice.service.sms.send;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

import org.truenewx.tnxjeex.notice.model.sms.SmsSendResult;
import org.truenewx.tnxjee.core.util.concurrent.DefaultThreadPoolExecutor;

/**
 * 抽象的短信内容发送器
 *
 * @author jianglei
 */
public abstract class AbstractSmsContentSender implements SmsContentSender {

    private SmsContentSpliter contentSpliter;
    private Executor executor = new DefaultThreadPoolExecutor(4, 8);

    /**
     * @param contentSpliter 短信内容分割器
     */
    public void setContentSpliter(SmsContentSpliter contentSpliter) {
        this.contentSpliter = contentSpliter;
    }

    /**
     * @param executor 线程执行器
     */
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    @Override
    public SmsSendResult send(String content, int maxCount, Locale locale, String... mobilePhones) {
        List<String> contents = this.contentSpliter.split(content, maxCount);
        return send(contents, locale, mobilePhones);
    }

    @Override
    public void send(String content, int maxCount, Locale locale, String[] mobilePhones, SmsSendCallback callback) {
        List<String> contents = this.contentSpliter.split(content, maxCount);
        this.executor.execute(new SendCommand(contents, locale, mobilePhones, callback));
    }

    /**
     * 分成指定条数的内容发送短信
     *
     * @param contents     内容清单，每一个内容为一条短信
     * @param locale       语言区域
     * @param mobilePhones 手机号码清单
     * @return 发送结果
     */
    protected abstract SmsSendResult send(List<String> contents, Locale locale, String... mobilePhones);

    protected class SendCommand implements Runnable {
        private List<String> contents;
        private Locale locale;
        private String[] mobilePhones;
        private SmsSendCallback callback;

        public SendCommand(List<String> contents, Locale locale, String[] mobilePhones, SmsSendCallback callback) {
            this.contents = contents;
            this.locale = locale;
            this.mobilePhones = mobilePhones;
            this.callback = callback;
        }

        @Override
        public void run() {
            SmsSendResult result = send(this.contents, this.locale, this.mobilePhones);
            this.callback.onSmsSent(result);
        }

    }

}
