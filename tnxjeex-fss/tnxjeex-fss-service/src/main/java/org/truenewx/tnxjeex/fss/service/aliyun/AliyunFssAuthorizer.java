package org.truenewx.tnxjeex.fss.service.aliyun;

import java.util.Date;

import org.slf4j.LoggerFactory;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.core.util.DateUtil;
import org.truenewx.tnxjeex.fss.service.FssAuthorizer;
import org.truenewx.tnxjeex.fss.service.model.FssProvider;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.ObjectAcl;
import com.aliyun.oss.model.ObjectPermission;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse.Credentials;

/**
 * 阿里云的文件存储授权器
 *
 * @author jianglei
 */
public class AliyunFssAuthorizer implements FssAuthorizer {

    private int tempReadExpiredSeconds = 60; // 临时读取时限默认60秒
    private AliyunAccount account;
    private AliyunPolicyBuilder policyBuilder;
    private AliyunStsRoleAssumer readStsRoleAssumer;

    public AliyunFssAuthorizer(AliyunAccount account) {
        this.account = account;
        this.policyBuilder = new AliyunPolicyBuilder(account);
    }

    /**
     * @param tempReadExpiredSeconds 临时读取权限过期秒数
     */
    public void setTempReadExpiredSeconds(int tempReadExpiredSeconds) {
        this.tempReadExpiredSeconds = tempReadExpiredSeconds;
    }

    /**
     * @param readStsRoleName 读权限的STS临时角色名
     */
    public void setReadStsRoleName(String readStsRoleName) {
        this.readStsRoleAssumer = new AliyunStsRoleAssumer(this.account, readStsRoleName);
    }

    @Override
    public FssProvider getProvider() {
        return FssProvider.ALIYUN;
    }

    @Override
    public void authorizePublicRead(String bucket, String path) {
        // TODO 避免同样的路径反复多次申请公开读
        this.account.getOssClient().setObjectAcl(bucket, path, CannedAccessControlList.PublicRead);
    }

    private boolean isPublicRead(String bucket, String path) {
        ObjectAcl acl = this.account.getOssClient().getObjectAcl(bucket, path);
        ObjectPermission permission = acl.getPermission();
        return permission == ObjectPermission.PublicRead
                || permission == ObjectPermission.PublicReadWrite;
    }

    protected String getReadHost(String bucket) {
        return bucket + Strings.DOT + this.account.getOssEndpoint();
    }

    @Override
    public String getReadUrl(String userKey, String bucket, String path) {
        // 拆分请求参数，确保路径不带参数
        int index = path.indexOf(Strings.QUESTION);
        String parameterString = Strings.EMPTY;
        if (index >= 0) {
            parameterString = path.substring(index + 1);
            path = path.substring(0, index);
        }
        try {
            if (isPublicRead(bucket, path)) {
                // 以双斜杠开头，表示采用当前上下文的相同协议
                StringBuffer url = new StringBuffer("//").append(getReadHost(bucket))
                        .append(Strings.SLASH).append(path);
                if (parameterString.length() > 0) {
                    url.append(Strings.QUESTION).append(parameterString);
                }
                return url.toString();
            } else if (this.readStsRoleAssumer != null) { // 非公开可读的，授予临时读取权限
                String policyDocument = this.policyBuilder.buildReadDocument(bucket, path);
                Credentials credentials = this.readStsRoleAssumer.assumeRole(userKey,
                        policyDocument);
                if (credentials != null) {
                    OSS oss = AliyunOssUtil.buildOss(this.account.getOssEndpoint(),
                            credentials.getAccessKeyId(), credentials.getAccessKeySecret(),
                            credentials.getSecurityToken());
                    GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket,
                            path);
                    Date expiration = DateUtil.addSeconds(new Date(), this.tempReadExpiredSeconds);
                    request.setExpiration(expiration);
                    if (parameterString.length() > 0) {
                        String[] params = parameterString.split(Strings.AND);
                        for (String param : params) {
                            String[] array = param.split(Strings.EQUAL);
                            if (array.length > 1) {
                                request.addQueryParameter(array[0], array[1]);
                            }
                        }
                    }
                    String url = oss.generatePresignedUrl(request).toString();
                    url = replaceHost(url, getReadHost(bucket));
                    return url;
                }
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
        }
        return null;
    }

    private String replaceHost(String url, String host) {
        int index = url.indexOf("://");
        String protocol = url.substring(0, index);
        url = url.substring(url.indexOf(Strings.SLASH, index + 3));
        return protocol + "://" + host + url;
    }

}
