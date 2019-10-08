package org.truenewx.tnxjeex.unstructured.service.model;

import com.aliyun.oss.internal.Mimetypes;

/**
 * 非结构化存储的资源存储元信息
 *
 * @author jianglei
 * @since JDK 1.8
 */
public class UnstructuredStorageMetadata {
    private String filename;
    private long size;
    private long lastModifiedTime;

    public UnstructuredStorageMetadata(final String filename, final long size,
            final long lastModifiedTime) {
        this.filename = filename;
        this.size = size;
        this.lastModifiedTime = lastModifiedTime;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(final String filename) {
        this.filename = filename;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public long getLastModifiedTime() {
        return this.lastModifiedTime;
    }

    public void setLastModifiedTime(final long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public String getMimeType() {
        if (this.filename == null) {
            return null;
        }
        return Mimetypes.getInstance().getMimetype(this.filename);
    }

}
