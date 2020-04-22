package org.truenewx.tnxjeex.fss.service.model;

import org.springframework.util.Assert;
import org.truenewx.tnxjee.model.spec.DimensionSize;

import com.aliyun.oss.internal.Mimetypes;

/**
 * 文件存储上传限制
 *
 * @author jianglei
 */
public class FssUploadLimit {

    private int number;
    private long capacity;
    private boolean extensionsRejected;
    private String[] extensions;
    private String[] mimeTypes;
    private boolean imageable;
    private Boolean croppable;
    private DimensionSize[] thumbnailSizes;

    public FssUploadLimit(int number, long capacity, boolean extensionsRejected,
            String... extensions) {
        Assert.isTrue(number >= 0, "number must be not less than 0");
        this.number = number;
        Assert.isTrue(capacity >= 0, "capacity must be not less than 0");
        this.capacity = capacity;
        this.extensionsRejected = extensionsRejected;
        this.extensions = extensions;
        this.mimeTypes = new String[extensions.length];
        Mimetypes mimetypes = Mimetypes.getInstance();
        for (int i = 0; i < extensions.length; i++) {
            String extension = "temp." + extensions[i];
            this.mimeTypes[i] = mimetypes.getMimetype(extension);
        }
    }

    public FssUploadLimit(int number, long capacity, String... extensions) {
        this(number, capacity, false, extensions);
    }

    public int getNumber() {
        return this.number;
    }

    public long getCapacity() {
        return this.capacity;
    }

    public boolean isExtensionsRejected() {
        return this.extensionsRejected;
    }

    public String[] getExtensions() {
        return this.extensions;
    }

    public String[] getMimeTypes() {
        return this.mimeTypes;
    }

    public boolean isImageable() {
        return this.imageable;
    }

    public Boolean getCroppable() {
        return this.croppable;
    }

    public DimensionSize[] getThumbnailSizes() {
        return this.thumbnailSizes;
    }

    public void enableImage(boolean croppable, DimensionSize... thumbnailSizes) {
        this.imageable = true;
        this.croppable = croppable;
        this.thumbnailSizes = thumbnailSizes;
    }

}
