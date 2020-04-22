package org.truenewx.tnxjeex.fss.web.model;

import org.truenewx.tnxjee.core.util.StringUtil;

/**
 * 上传结果
 *
 * @author jianglei
 */
public class FssUploadResult {

    private String id;
    private String filename;
    private String storageUrl;
    private String readUrl;
    private String thumbnailReadUrl;

    public FssUploadResult(String filename, String storageUrl, String readUrl,
            String thumbnailReadUrl) {
        this.id = StringUtil.uuid32();
        this.filename = filename;
        this.storageUrl = storageUrl;
        this.readUrl = readUrl;
        this.thumbnailReadUrl = thumbnailReadUrl;
    }

    public String getId() {
        return this.id;
    }

    public String getFilename() {
        return this.filename;
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
