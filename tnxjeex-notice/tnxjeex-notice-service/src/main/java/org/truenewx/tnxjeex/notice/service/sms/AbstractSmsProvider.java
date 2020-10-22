package org.truenewx.tnxjeex.notice.service.sms;

/**
 * 抽象的短信提供者
 *
 * @author jianglei
 */
public abstract class AbstractSmsProvider implements SmsContentProvider {

    private String type;
    private int maxCount;

    @Override
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int getMaxCount() {
        return this.maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

}
