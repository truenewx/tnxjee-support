package org.truenewx.tnxjeex.fss.service.aliyun;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;

/**
 * 阿里云OSS工具类
 *
 * @author jianglei
 */
public class AliyunOssUtil {

    private AliyunOssUtil() {
    }

    public static OSS buildOss(String endpoint, String accessKeyId, String accessKeySecret,
            String securityToken) {
        CredentialsProvider credsProvider = new DefaultCredentialProvider(accessKeyId,
                accessKeySecret, securityToken);
        ClientConfiguration config = new ClientConfiguration();
        config.setRequestTimeoutEnabled(true);
        config.setRequestTimeout(10 * 1000); // 10秒超时
        return new OSSClient(endpoint, credsProvider, config);
    }

    public static OSS buildOss(String endpoint, String accessKeyId, String accessKeySecret) {
        return buildOss(endpoint, accessKeyId, accessKeySecret, null);
    }

}
