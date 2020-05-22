package org.truenewx.tnxjeex.fss.service.model;

import org.apache.commons.lang3.StringUtils;
import org.truenewx.tnxjee.core.Strings;

/**
 * 文件存储服务的存储路径分析
 *
 * @author jianglei
 */
public class FssStoragePathAnalysis {

    private String type;
    private String relativePath;

    public FssStoragePathAnalysis(String type, String relativePath) {
        this.type = type;
        this.relativePath = relativePath;
    }

    public static FssStoragePathAnalysis of(String s) {
        if (s.startsWith("fss://")) {
            s = s.substring(5);
        } else if (s.startsWith("//")) {
            s = s.substring(1);
        }
        int index = s.indexOf(Strings.SLASH, 1); // 第二个斜杠的位置
        if (index > 0) {
            String type = s.substring(1, index);
            String relativePath = s.substring(index);
            FssStoragePathAnalysis instance = new FssStoragePathAnalysis(type, relativePath);
            if (instance.isValid()) {
                return instance;
            }
        }
        return null;
    }

    public String getType() {
        return this.type;
    }

    public String getRelativePath() {
        return this.relativePath;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(this.type) && StringUtils.isNotBlank(this.relativePath);
    }

    public String getUrl() {
        return isValid() ? "fss:/" + toString() : null;
    }

    @Override
    public String toString() {
        return Strings.SLASH + this.type + this.relativePath;
    }

}
