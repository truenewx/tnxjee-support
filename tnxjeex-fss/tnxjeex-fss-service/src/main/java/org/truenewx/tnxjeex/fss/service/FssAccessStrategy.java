package org.truenewx.tnxjeex.fss.service;

import java.util.Map;

import org.truenewx.tnxjee.model.spec.user.UserIdentity;
import org.truenewx.tnxjeex.fss.service.model.FssProvider;
import org.truenewx.tnxjeex.fss.service.model.FssUploadLimit;

/**
 * 文件存储服务的访问策略
 *
 * @author jianglei
 */
public interface FssAccessStrategy<T extends Enum<T>, I extends UserIdentity<?>> {

    T getType();

    FssProvider getProvider();

    /**
     * 指定是否需要本地存储，默认为true
     *
     * @return 是否本地存储
     */
    default boolean isStoreLocally() {
        return true;
    }

    /**
     * 指定读取地址是否为本地地址，当需要由策略严格控制读取权限时应该返回true<br>
     * 默认返回false，此时读取地址由提供商提供
     *
     * @return 读取地址是否为本地地址
     */
    default boolean isReadLocally() {
        return false;
    }

    /**
     * 获取在当前策略下，指定用户上传文件的限制条件
     *
     * @param userIdentity 用户标识
     * @return 指定用户上传文件的限制条件
     */
    FssUploadLimit getUploadLimit(I userIdentity);

    /**
     * 获取存储桶名，存储桶名要求全系统唯一，或者与其它策略的存储桶相同时，下级存放路径不同
     *
     * @return 存储桶名
     */
    String getBucket();

    /**
     * 是否将上传文件的MD5码作为文件名
     *
     * @return 是否将上传文件的MD5码作为文件名
     */
    default boolean isMd5AsFilename() {
        return false;
    }

    /**
     * 获取指定资源的存储路径（含扩展名）
     *
     * @param token        业务标识
     * @param userIdentity 当前登录用户
     * @param filename     原始文件名，含扩展名
     * @return 存储路径，已预见的业务场景中不会出现无写权限时，直接返回null表示没有写权限
     */
    String getPath(String token, I userIdentity, String filename);

    default boolean isPublicReadable() {
        return false;
    }

    boolean isReadable(I userIdentity, String path);

    boolean isWritable(I userIdentity, String path);

    /**
     * 获取缩略图读取参数集，仅在文件为图片时有效，返回空时表示不支持缩略图
     *
     * @return 缩略图读取参数集
     */
    default Map<String, String> getThumbnailParameters() {
        return null;
    }

}
