package org.truenewx.tnxjeex.fss.service.own;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.truenewx.tnxjeex.fss.service.FssLocalAccessor;
import org.truenewx.tnxjeex.fss.service.FssProviderAccessor;
import org.truenewx.tnxjeex.fss.service.model.FssProvider;

/**
 * 文件存储自有访问器
 *
 * @author jianglei
 * @since JDK 1.8
 */
public class OwnFssAccessor implements FssProviderAccessor {

    private FssLocalAccessor localAccessor;

    @Autowired
    public void setLocalAccessor(FssLocalAccessor localAccessor) {
        this.localAccessor = localAccessor;
    }

    @Override
    public FssProvider getProvider() {
        return FssProvider.OWN;
    }

    @Override
    public void write(String bucket, String path, String filename, InputStream in)
            throws IOException {
        this.localAccessor.write(bucket, path, filename, in);
    }

    @Override
    public String getFilename(String bucket, String path) {
        return this.localAccessor.getFilename(bucket, path);
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
