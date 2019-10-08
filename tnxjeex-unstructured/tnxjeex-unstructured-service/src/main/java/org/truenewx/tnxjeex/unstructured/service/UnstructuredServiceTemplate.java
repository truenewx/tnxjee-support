package org.truenewx.tnxjeex.unstructured.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.truenewx.tnxjee.service.api.Service;
import org.truenewx.tnxjeex.unstructured.service.model.UnstructuredReadMetadata;
import org.truenewx.tnxjeex.unstructured.service.model.UnstructuredUploadLimit;

/**
 * 非结构化存储服务模版
 *
 * @author jianglei
 * @param <T> 授权类型
 * @param <U> 用户标识类型
 */
public interface UnstructuredServiceTemplate<T extends Enum<T>, U> extends Service {

    /**
     * 获取在当前方针下，指定用户上传指定授权类型文件的限制条件
     *
     * @param authorizeType 授权类型
     * @param user          用户标识
     * @return 指定用户上传指定授权类型文件的限制条件
     */
    UnstructuredUploadLimit getUploadLimit(T authorizeType, U user);

    /**
     * 指定用户在指定授权类型下写文件
     *
     * @param authorizeType 授权类型
     * @param token         业务标识
     * @param user          用户标识
     * @param filename      文件名
     * @param in            输入流
     * @return 写好的文件的内部存储URL
     * @throws IOException 如果写的过程中出现错误
     */
    String write(T authorizeType, String token, U user, String filename, InputStream in)
            throws IOException;

    /**
     * 指定用户获取指定内部存储URL对应的外部读取URL
     *
     * @param user       用户标识
     * @param storageUrl 内部存储URL
     * @param thumbnail  是否缩略图
     * @return 外部读取URL
     */
    String getReadUrl(U user, String storageUrl, boolean thumbnail);

    /**
     * 获取指定资源的读取元信息
     *
     * @param user       用户标识
     * @param storageUrl 资源的存储路径
     * @return 指定资源的读取元信息
     */
    UnstructuredReadMetadata getReadMetadata(U user, String storageUrl);

    /**
     * 获取指定资源的最后修改时间
     *
     * @param user   用户标识
     * @param bucket 存储桶名
     * @param path   存储路径
     * @return 最后修改时间毫秒数，指定资源不存在时返回0
     */
    long getLastModifiedTime(U user, String bucket, String path);

    /**
     * 指定用户读取指定路径的文件内容到指定输出流中
     *
     * @param user   用户标识
     * @param bucket 存储桶名
     * @param path   存储路径
     * @param out    输出流
     * @throws IOException 如果读的过程中出现错误
     */
    void read(U user, String bucket, String path, OutputStream out) throws IOException;

}
