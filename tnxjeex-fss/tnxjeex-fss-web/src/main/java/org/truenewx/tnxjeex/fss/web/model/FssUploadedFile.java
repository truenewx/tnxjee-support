package org.truenewx.tnxjeex.fss.web.model;

/**
 * 已上传的文件
 *
 * @author jianglei
 */
public class FssUploadedFile {

    private String id;
    private String name;
    private String storageUrl;
    private String readUrl;
    private String thumbnailReadUrl;

    public FssUploadedFile(String id, String name, String storageUrl, String readUrl,
            String thumbnailReadUrl) {
        this.id = id;
        this.name = name;
        this.storageUrl = storageUrl;
        this.readUrl = readUrl;
        this.thumbnailReadUrl = thumbnailReadUrl;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getStorageUrl() {
        return this.storageUrl;
    }

    public String getReadUrl() {
        return this.readUrl;
    }

    public String getThumbnailReadUrl() {
        return this.thumbnailReadUrl;
    }
}
