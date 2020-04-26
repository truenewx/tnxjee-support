package org.truenewx.tnxjeex.fss.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.truenewx.tnxjee.model.spec.user.UserIdentity;
import org.truenewx.tnxjee.service.Service;
import org.truenewx.tnxjeex.fss.service.model.FssFileMeta;
import org.truenewx.tnxjeex.fss.service.model.FssUploadLimit;

/**
 * 文件存储服务模版
 *
 * @param <T> 授权类型
 * @param <I> 用户标识类型
 * @author jianglei
 */
public interface FssServiceTemplate<T extends Enum<T>, I extends UserIdentity> extends Service {

    /**
     * 获取指定用户上传指定业务类型的文件上传限制条件
     *
     * @param type         业务类型
     * @param userIdentity 用户标识
     * @return 指定用户上传指定业务类型的文件上传限制条件
     */
    FssUploadLimit getUploadLimit(T type, I userIdentity);

    /**
     * 指定用户在业务授权类型下写文件
     *
     * @param type         业务类型
     * @param resource     业务资源
     * @param userIdentity 用户标识
     * @param filename     文件名
     * @param in           输入流
     * @return 写好的文件的内部存储URL
     * @throws IOException 如果写的过程中出现错误
     */
    String write(T type, String resource, I userIdentity, String filename, InputStream in)
            throws IOException;

    /**
     * 指定用户获取指定内部存储URL对应的外部读取URL
     *
     * @param userIdentity 用户标识
     * @param storageUrl   内部存储URL
     * @param thumbnail    是否缩略图
     * @return 外部读取URL
     */
    String getReadUrl(I userIdentity, String storageUrl, boolean thumbnail);

    /**
     * 获取指定资源的读取元信息
     *
     * @param userIdentity 用户标识
     * @param storageUrl   资源的存储路径
     * @return 指定资源的读取元信息
     */
    FssFileMeta getMeta(I userIdentity, String storageUrl);

    /**
     * 获取指定资源的最后修改时间
     *
     * @param userIdentity 用户标识
     * @param bucket       存储桶名
     * @param path         存储路径
     * @return 最后修改时间毫秒数，指定资源不存在时返回0
     */
    long getLastModifiedTime(I userIdentity, String bucket, String path);

    /**
     * 指定用户读取指定路径的文件内容到指定输出流中
     *
     * @param userIdentity 用户标识
     * @param bucket       存储桶名
     * @param path         存储路径
     * @param out          输出流
     * @throws IOException 如果读的过程中出现错误
     */
    void read(I userIdentity, String bucket, String path, OutputStream out) throws IOException;

}
