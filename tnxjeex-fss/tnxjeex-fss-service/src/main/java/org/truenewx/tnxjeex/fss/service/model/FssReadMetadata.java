package org.truenewx.tnxjeex.fss.service.model;

/**
 * 文件存储的资源读取元信息
 *
 * @author jianglei
 */
public class FssReadMetadata {

    private String readUrl;
    private String thumbnailReadUrl;
    private FssStorageMetadata storageMetadata;

    public FssReadMetadata(String readUrl, String thumbnailReadUrl,
            FssStorageMetadata storageMetadata) {
        this.readUrl = readUrl;
        this.thumbnailReadUrl = thumbnailReadUrl;
        this.storageMetadata = storageMetadata;
    }

    public void setReadUrl(String readUrl) {
        this.readUrl = readUrl;
    }

    public String getReadUrl() {
        return this.readUrl;
    }

    public String getThumbnailReadUrl() {
        return this.thumbnailReadUrl;
    }

    public void setThumbnailReadUrl(String thumbnailReadUrl) {
        this.thumbnailReadUrl = thumbnailReadUrl;
    }

    public String getFilename() {
        return this.storageMetadata == null ? null : this.storageMetadata.getFilename();
    }

    public long getSize() {
        return this.storageMetadata == null ? 0 : this.storageMetadata.getSize();
    }

    public long getLastModifiedTime() {
        return this.storageMetadata == null ? 0 : this.storageMetadata.getLastModifiedTime();
    }

    public String getMimeType() {
        return this.storageMetadata == null ? null : this.storageMetadata.getMimeType();
    }

}
