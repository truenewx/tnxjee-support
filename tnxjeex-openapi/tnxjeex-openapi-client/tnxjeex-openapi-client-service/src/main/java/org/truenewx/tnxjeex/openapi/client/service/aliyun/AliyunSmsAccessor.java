package org.truenewx.tnxjeex.openapi.client.service.aliyun;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.core.util.LogUtil;

import com.aliyuncs.exceptions.ClientException;

/**
 * 阿里云短信接口访问器
 */
public abstract class AliyunSmsAccessor extends AliyunAcsAccessSupport {

    /**
     * 发送短信
     *
     * @param signName     签名。必须是已经在阿里云通过审核的签名
     * @param templateCode 模版代号。必须是已经在阿里云通过审核的模版
     * @param content      内容
     * @param mobilePhones 手机号码清单
     * @return 发送失败的手机号码-错误消息的映射集
     */
    public final Map<String, String> send(String signName, String templateCode, String content,
            String... mobilePhones) {
        if (mobilePhones.length == 0) {
            return null;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("RegionId", getRegionId());
        params.put("SignName", signName);
        params.put("TemplateCode", templateCode);
        params.put("TemplateParam", content);
        params.put("PhoneNumbers", StringUtils.join(mobilePhones, Strings.COMMA));
        try {
            Map<String, Object> data = post("SendSms", params);
            String responseCode = (String) data.get("Code");
            if ("OK".equalsIgnoreCase(responseCode)) {
                return null;
            } else {
                String errorMessage = (String) data.get("Message");
                return buildFailures(errorMessage);
            }
        } catch (ClientException e) {
            LogUtil.error(getClass(), e);
            return buildFailures(e.getErrMsg(), mobilePhones);
        }
    }

    private Map<String, String> buildFailures(String errorMessage, String... mobilePhones) {
        Map<String, String> failures = new HashMap<>();
        for (String mobilePhone : mobilePhones) {
            failures.put(errorMessage, mobilePhone);
        }
        return failures;
    }

}
