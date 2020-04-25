package org.truenewx.tnxjeex.fss.service.aliyun;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.truenewx.tnxjeex.fss.service.FssProviderAccessor;
import org.truenewx.tnxjeex.fss.service.model.FssProvider;
import org.truenewx.tnxjeex.fss.service.model.FssStorageMetadata;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.model.ObjectMetadata;

/**
 * 阿里云的文件存储访问器
 *
 * @author jianglei
 */
public class AliyunFssAccessor implements FssProviderAccessor {

    private AliyunAccount account;

    public AliyunFssAccessor(AliyunAccount account) {
        this.account = account;
    }

    @Override
    public FssProvider getProvider() {
        return FssProvider.ALIYUN;
    }

    @Override
    public void write(String bucket, String path, String filename, InputStream in)
            throws IOException {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.getUserMetadata().put("filename", filename);
        this.account.getOssClient().putObject(bucket, path, in, objectMetadata);
    }

    @Override
    public FssStorageMetadata getStorageMetadata(String bucket, String path) {
        try {
            ObjectMetadata objectMetadata = this.account.getOssClient().getObjectMetadata(bucket,
                    path);
            String filename = objectMetadata.getUserMetadata().get("filename");
            return new FssStorageMetadata(filename, objectMetadata.getContentLength(),
                    objectMetadata.getLastModified().getTime());
        } catch (Exception e) {
            // 忽略所有异常
            return null;
        }
    }

    @Override
    public long getLastModifiedTime(String bucket, String path) {
        try {
            return this.account.getOssClient().getObjectMetadata(bucket, path).getLastModified()
                    .getTime();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public boolean read(String bucket, String path, OutputStream out) throws IOException {
        try {
            InputStream in = this.account.getOssClient().getObject(bucket, path).getObjectContent();
            IOUtils.copy(in, out);
            in.close();
            return true;
        } catch (ClientException e) {
            return false;
        }
    }

}
