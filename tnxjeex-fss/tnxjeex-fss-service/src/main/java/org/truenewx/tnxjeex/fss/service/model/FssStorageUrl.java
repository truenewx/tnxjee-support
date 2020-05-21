package org.truenewx.tnxjeex.fss.service.model;

import org.truenewx.tnxjee.core.Strings;

/**
 * 非结构化内部存储URL，包含了内部存储URL的转换逻辑
 *
 * @author jianglei
 */
public class FssStorageUrl {

    private FssProvider provider;
    private String contextPath;
    private String relativePath;

    public FssStorageUrl(FssProvider provider, String contextPath, String relativePath) {
        this.provider = provider;
        this.contextPath = contextPath;
        this.relativePath = relativePath;
    }

    public FssProvider getProvider() {
        return this.provider;
    }

    public String getContextPath() {
        return this.contextPath;
    }

    public String getRelativePath() {
        return this.relativePath;
    }

    public String getPath() {
        return getContextPath() + getRelativePath();
    }

    public boolean isValid() {
        return this.provider != null && this.contextPath != null && this.relativePath != null;
    }

    @Override
    public String toString() {
        if (!isValid()) { // 无效则返回null
            return null;
        }
        // 形如：${proivder}://${bucket}/${path}
        StringBuffer url = new StringBuffer(this.provider.name().toLowerCase()).append("://")
                .append(this.contextPath);
        if (!this.relativePath.startsWith(Strings.SLASH)) {
            url.append(Strings.SLASH);
        }
        url.append(this.relativePath);
        return url.toString();
    }

}
