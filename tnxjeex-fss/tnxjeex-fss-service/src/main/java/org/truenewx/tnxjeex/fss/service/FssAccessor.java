package org.truenewx.tnxjeex.fss.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.truenewx.tnxjeex.fss.service.model.FssFileStorageMeta;

/**
 * 非结构化数据访问器
 *
 * @author jianglei
 */
public interface FssAccessor {

    void write(String bucket, String path, String filename, InputStream in) throws IOException;

    /**
     * 获取指定文件的文件名
     *
     * @param bucket 存储桶名
     * @param path   存储路径
     * @return 文件名
     */
    String getFilename(String bucket, String path);

    /**
     * 获取指定文件的最后修改时间
     *
     * @param bucket 存储桶名
     * @param path   存储路径
     * @return 最后修改时间毫秒数，指定文件不存在时返回0
     */
    long getLastModifiedTime(String bucket, String path);

    /**
     * 获取指定文件的存储元信息
     *
     * @param bucket 存储桶名
     * @param path   存储路径
     * @return 文件元信息
     */
    FssFileStorageMeta getStorageMeta(String bucket, String path);

    boolean read(String bucket, String path, OutputStream out) throws IOException;

}
