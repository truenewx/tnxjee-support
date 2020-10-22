package org.truenewx.notice.model.sms;

import java.util.HashSet;
import java.util.Set;

/**
 * 短信发送结果
 *
 * @author jianglei
 */
public class SmsSendResult {
    private Sms sms;
    private Set<String> failures;

    public SmsSendResult(Sms sms) {
        this.sms = sms;
    }

    public Sms getSms() {
        return this.sms;
    }

    /**
     * @return 发送失败的手机号码清单
     */
    public Set<String> getFailures() {
        if (this.failures == null) {
            this.failures = new HashSet<>();
        }
        return this.failures;
    }

    public void addFailures(String... failures) {
        getFailures(); // 确保非空
        for (String failure : failures) {
            this.failures.add(failure);
        }
    }

    /**
     * @return 是否全部发送成功
     */
    public boolean isAllSuccessful() {
        return this.failures == null || this.failures.isEmpty();
    }
}
