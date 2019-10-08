package org.truenewx.tnxjeex.unstructured.service.own;

import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjeex.unstructured.service.UnstructuredAuthorizer;
import org.truenewx.tnxjeex.unstructured.service.model.UnstructuredProvider;

/**
 * 非结构化存储自有授权器
 *
 * @author jianglei
 *
 */
public class OwnUnstructuredAuthorizer implements UnstructuredAuthorizer {

    private String downloadPathPrefix = "/unstructured/dl";

    /**
     * @param downloadPathPrefix 资源下载路径前缀
     */
    public void setDownloadPathPrefix(String downloadPathPrefix) {
        this.downloadPathPrefix = downloadPathPrefix;
    }

    @Override
    public UnstructuredProvider getProvider() {
        return UnstructuredProvider.OWN;
    }

    @Override
    public void authorizePublicRead(String bucket, String path) {
        // 本地资源本身没有权限限制，权限由Policy进行限制和判断
    }

    @Override
    public String getReadUrl(String userKey, String bucket, String path) {
        // 形如：/${downloadPathPrefix}/${bucket}/${path}，本地地址交由Controller完成上下文根路径的拼装
        StringBuffer url = new StringBuffer(this.downloadPathPrefix).append(Strings.SLASH)
                .append(bucket).append(Strings.SLASH).append(path);
        return url.toString();
    }

}
