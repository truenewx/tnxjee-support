package org.truenewx.tnxjeex.fss.service;

import org.truenewx.tnxjeex.fss.service.model.FssProvider;

/**
 * 文件存储授权器
 *
 * @author jianglei
 */
public interface FssAuthorizer {

    /**
     * 获取当前授权器的服务提供商
     *
     * @return 服务提供商
     */
    FssProvider getProvider();

    /**
     * 授权指定资源为公开可读
     *
     * @param bucket 存储桶名称
     * @param path   资源路径
     */
    void authorizePublicRead(String bucket, String path);

    /**
     * 获取指定资源读取URL
     *
     * @param userKey 用户唯一标识
     * @param bucket  存储桶名称
     * @param path    资源路径
     * @return 资源读取URL
     */
    String getReadUrl(String userKey, String bucket, String path);

}
