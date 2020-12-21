package org.truenewx.tnxjeex.fss.service;

import java.util.Map;

import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.model.spec.user.UserIdentity;
import org.truenewx.tnxjeex.fss.service.model.FssProvider;
import org.truenewx.tnxjeex.fss.service.model.FssUploadLimit;

/**
 * 文件存储服务的访问策略
 *
 * @author jianglei
 */
public interface FssAccessStrategy<I extends UserIdentity<?>> {

    /**
     * 获取业务类型，要求在同一个系统中唯一
     *
     * @return 业务类型
     */
    String getType();

    FssProvider getProvider();

    /**
     * 获取在当前策略下，指定用户上传文件的限制条件
     *
     * @param userIdentity 用户标识
     * @return 指定用户上传文件的限制条件
     */
    FssUploadLimit getUploadLimit(I userIdentity);

    /**
     * 是否将上传文件的MD5码作为文件名
     *
     * @return 是否将上传文件的MD5码作为文件名
     */
    default boolean isMd5AsFilename() {
        return false;
    }

    /**
     * 获取存储路径上下文根，要求在同一个系统中唯一
     *
     * @return 存储路径上下文根
     */
    default String getContextPath() {
        return Strings.SLASH + getType();
    }

    /**
     * 获取指定资源的相对于上下文根的存储路径（含扩展名）
     *
     * @param modelIdentity 业务模型标识
     * @param userIdentity  用户标识
     * @param filename      原始文件名，含扩展名
     * @return 相对于上下文根的存储路径，返回null表示没有写权限
     */
    String getRelativePath(String modelIdentity, I userIdentity, String filename);

    /**
     * @return 是否公开匿名可读
     */
    default boolean isPublicReadable() {
        return false;
    }

    /**
     * 判断指定用户对指定相对路径是否可读
     *
     * @param userIdentity 用户标识
     * @param relativePath 相对路径
     * @return 指定用户对指定相对路径是否可读
     */
    default boolean isReadable(I userIdentity, String relativePath) {
        return isPublicReadable();
    }

    /**
     * 获取缩略图读取参数集，仅在文件为图片时有效，返回空时表示不支持缩略图
     *
     * @return 缩略图读取参数集
     */
    default Map<String, String> getThumbnailParameters() {
        return null;
    }

}
