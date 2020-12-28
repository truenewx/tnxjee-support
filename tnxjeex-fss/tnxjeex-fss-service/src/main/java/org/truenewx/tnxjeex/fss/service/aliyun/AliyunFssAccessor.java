package org.truenewx.tnxjeex.fss.service.aliyun;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.truenewx.tnxjeex.fss.service.FssAccessor;
import org.truenewx.tnxjeex.fss.service.model.FssProvider;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.model.ObjectMetadata;

/**
 * 阿里云的文件存储访问器
 *
 * @author jianglei
 */
public class AliyunFssAccessor implements FssAccessor {

    private AliyunAccount account;
    private String bucket;

    public AliyunFssAccessor(AliyunAccount account, String bucket) {
        this.account = account;
        this.bucket = bucket;
    }

    @Override
    public FssProvider getProvider() {
        return FssProvider.ALIYUN;
    }

    @Override
    public void write(InputStream in, String path, String filename)
            throws IOException {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.getUserMetadata().put("filename", filename);
        this.account.getOssClient().putObject(this.bucket, path, in, objectMetadata);
    }

    @Override
    public String getOriginalFilename(String path) {
        try {
            ObjectMetadata meta = this.account.getOssClient().getObjectMetadata(this.bucket, path);
            return meta.getUserMetadata().get("filename");
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Long getLastModifiedTime(String path) {
        try {
            ObjectMetadata meta = this.account.getOssClient().getObjectMetadata(this.bucket, path);
            return meta.getLastModified().getTime();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean read(String path, OutputStream out) throws IOException {
        try {
            InputStream in = this.account.getOssClient().getObject(this.bucket, path).getObjectContent();
            IOUtils.copy(in, out);
            in.close();
            return true;
        } catch (ClientException e) {
            return false;
        }
    }

}
