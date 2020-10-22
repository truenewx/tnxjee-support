package org.truenewx.tnxjeex.notice.service.sms.aliyun;

import java.util.Locale;
import java.util.Map;

import org.truenewx.tnxjee.core.util.JsonUtil;
import org.truenewx.tnxjeex.notice.service.sms.SmsContentProvider;

/**
 * 阿里云短信内容提供者
 *
 * @author jianglei
 */
public class AliyunSmsContentProvider implements SmsContentProvider {

    private String type;

    public AliyunSmsContentProvider(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public int getMaxCount() {
        return 0;
    }

    @Override
    public String getContent(Map<String, Object> params, Locale locale) {
        return JsonUtil.toJson(params);
    }

}
