package org.truenewx.tnxjeex.fss.service.model;

import com.aliyun.oss.internal.Mimetypes;

/**
 * 文件存储服务的文件存储元数据
 *
 * @author jianglei
 */
public class FssFileStorageMeta {

    private String name;
    private long size;
    private long lastModifiedTime;

    public FssFileStorageMeta(String name, long size, long lastModifiedTime) {
        this.name = name;
        this.size = size;
        this.lastModifiedTime = lastModifiedTime;
    }

    public String getName() {
        return this.name;
    }

    public long getSize() {
        return this.size;
    }

    public long getLastModifiedTime() {
        return this.lastModifiedTime;
    }

    public String getMimeType() {
        if (this.name == null) {
            return null;
        }
        return Mimetypes.getInstance().getMimetype(this.name);
    }

}
