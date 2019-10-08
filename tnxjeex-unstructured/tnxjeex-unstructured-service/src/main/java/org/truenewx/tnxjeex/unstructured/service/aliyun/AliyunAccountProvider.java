package org.truenewx.tnxjeex.unstructured.service.aliyun;

import org.apache.commons.lang3.StringUtils;

import com.aliyun.oss.OSS;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;

/**
 * 阿里云账户信息提供者
 *
 * @author jianglei
 */
public class AliyunAccountProvider implements AliyunAccount {

    private String accountId;
    private String ossRegion;
    private String ossEndpoint;
    private String ramRegion = "cn-hangzhou";
    private String adminAccessKeyId;
    private String adminAccessKeySecret;
    private OSS oss;
    private IAcsClient acsClient;

    /**
     * @param accountId 阿里云账户编号
     */
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    /**
     * @param ossRegion OSS区域
     */
    public void setOssRegion(String ossRegion) {
        this.ossRegion = ossRegion;
        if (StringUtils.isNotBlank(this.ossRegion)) {
            this.ossEndpoint = "oss-" + this.ossRegion + ".aliyuncs.com";
        } else {
            this.ossEndpoint = null;
        }
    }

    /**
     * @param ramRegion RAM区域
     */
    public void setRamRegion(String ramRegion) {
        this.ramRegion = ramRegion;
    }

    /**
     * @param adminAccessKeyId 管理账号访问id
     */
    public void setAdminAccessKeyId(String adminAccessKeyId) {
        this.adminAccessKeyId = adminAccessKeyId;
    }

    /**
     * @param adminAccessKeySecret 管理账号访问密钥
     */
    public void setAdminAccessKeySecret(String adminAccessKeySecret) {
        this.adminAccessKeySecret = adminAccessKeySecret;
    }

    @Override
    public String getAccountId() {
        return this.accountId;
    }

    @Override
    public String getOssRegion() {
        return this.ossRegion;
    }

    @Override
    public String getOssEndpoint() {
        return this.ossEndpoint;
    }

    @Override
    public OSS getOssClient() {
        if (this.oss == null) {
            this.oss = AliyunOssUtil.buildOss(this.ossEndpoint, this.adminAccessKeyId,
                    this.adminAccessKeySecret);
        }
        return this.oss;
    }

    @Override
    public IAcsClient getAcsClient() {
        if (this.acsClient == null) {
            IClientProfile profile = DefaultProfile.getProfile(this.ramRegion,
                    this.adminAccessKeyId, this.adminAccessKeySecret);
            this.acsClient = new DefaultAcsClient(profile);
        }
        return this.acsClient;
    }

    @Override
    protected void finalize() throws Throwable {
        if (this.oss != null) {
            this.oss.shutdown();
        }
    }

}
