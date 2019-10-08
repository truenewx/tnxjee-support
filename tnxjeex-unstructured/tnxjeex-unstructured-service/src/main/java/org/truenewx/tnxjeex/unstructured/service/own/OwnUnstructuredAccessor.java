package org.truenewx.tnxjeex.unstructured.service.own;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.truenewx.tnxjeex.unstructured.service.UnstructuredLocalAccessor;
import org.truenewx.tnxjeex.unstructured.service.UnstructuredProviderAccessor;
import org.truenewx.tnxjeex.unstructured.service.model.UnstructuredProvider;
import org.truenewx.tnxjeex.unstructured.service.model.UnstructuredStorageMetadata;

/**
 * 非结构化存储自有访问器
 *
 * @author jianglei
 * @since JDK 1.8
 */
public class OwnUnstructuredAccessor implements UnstructuredProviderAccessor {

    private UnstructuredLocalAccessor localAccessor;

    @Autowired
    public void setLocalAccessor(UnstructuredLocalAccessor localAccessor) {
        this.localAccessor = localAccessor;
    }

    @Override
    public UnstructuredProvider getProvider() {
        return UnstructuredProvider.OWN;
    }

    @Override
    public void write(String bucket, String path, String filename, InputStream in)
            throws IOException {
        this.localAccessor.write(bucket, path, filename, in);
    }

    @Override
    public UnstructuredStorageMetadata getStorageMetadata(String bucket, String path) {
        return this.localAccessor.getStorageMetadata(bucket, path);
    }

    @Override
    public long getLastModifiedTime(String bucket, String path) {
        return this.localAccessor.getLastModifiedTime(bucket, path);
    }

    @Override
    public boolean read(String bucket, String path, OutputStream out) throws IOException {
        return this.localAccessor.read(bucket, path, out);
    }

}
