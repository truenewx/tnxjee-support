package org.truenewx.tnxjeex.fss.api.model;

/**
 * Fss转储命令
 */
public class FssTransferCommand {

    private String type;
    private String url;
    private String extension;
    private String modelIdentity;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getExtension() {
        return this.extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getModelIdentity() {
        return this.modelIdentity;
    }

    public void setModelIdentity(String modelIdentity) {
        this.modelIdentity = modelIdentity;
    }

}
