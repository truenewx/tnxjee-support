package org.truenewx.tnxjeex.notice.service.sms.content;

/**
 * 抽象的短信内容发送器
 */
public abstract class AbstractSmsContentSender implements SmsContentSender {

    private String[] types;

    @Override
    public String[] getTypes() {
        return this.types;
    }

    public void setTypes(String... types) {
        this.types = types;
    }

}
