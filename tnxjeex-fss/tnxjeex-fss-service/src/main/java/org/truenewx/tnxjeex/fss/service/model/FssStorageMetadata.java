package org.truenewx.tnxjeex.fss.service.model;

import com.aliyun.oss.internal.Mimetypes;

/**
 * 文件存储的资源存储元信息
 *
 * @author jianglei
 * @since JDK 1.8
 */
public class FssStorageMetadata {
    private String filename;
    private long size;
    private long lastModifiedTime;

    public FssStorageMetadata(String filename, long size, long lastModifiedTime) {
        this.filename = filename;
        this.size = size;
        this.lastModifiedTime = lastModifiedTime;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getLastModifiedTime() {
        return this.lastModifiedTime;
    }

    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public String getMimeType() {
        if (this.filename == null) {
            return null;
        }
        return Mimetypes.getInstance().getMimetype(this.filename);
    }

}
