package org.truenewx.tnxjeex.fss.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.core.beans.ContextInitializedBean;
import org.truenewx.tnxjee.core.util.ArrayUtil;
import org.truenewx.tnxjee.core.util.EncryptUtil;
import org.truenewx.tnxjee.model.spec.user.UserIdentity;
import org.truenewx.tnxjee.service.exception.BusinessException;
import org.truenewx.tnxjeex.fss.service.model.FssFileMeta;
import org.truenewx.tnxjeex.fss.service.model.FssProvider;
import org.truenewx.tnxjeex.fss.service.model.FssStorageUrl;
import org.truenewx.tnxjeex.fss.service.model.FssUploadLimit;

/**
 * 文件存储服务模版实现
 *
 * @author jianglei
 */
public class FssServiceTemplateImpl<I extends UserIdentity<?>>
        implements FssServiceTemplate<I>, ContextInitializedBean {

    private final Map<String, FssAccessStrategy<I>> strategies = new HashMap<>();
    private final Map<FssProvider, FssAuthorizer> authorizers = new HashMap<>();
    private final Map<FssProvider, FssAccessor> accessors = new HashMap<>();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void afterInitialized(ApplicationContext context) throws Exception {
        Map<String, FssAccessStrategy> strategies = context.getBeansOfType(FssAccessStrategy.class);
        for (FssAccessStrategy<I> strategy : strategies.values()) {
            this.strategies.put(strategy.getType(), strategy);
        }

        Map<String, FssAuthorizer> authorizers = context.getBeansOfType(FssAuthorizer.class);
        for (FssAuthorizer authorizer : authorizers.values()) {
            this.authorizers.put(authorizer.getProvider(), authorizer);
        }

        Map<String, FssAccessor> accessors = context.getBeansOfType(FssAccessor.class);
        for (FssAccessor accessor : accessors.values()) {
            this.accessors.put(accessor.getProvider(), accessor);
        }
    }

    @Override
    public FssUploadLimit getUploadLimit(String type, I userIdentity) {
        return getStrategy(type).getUploadLimit(userIdentity);
    }

    private FssAccessStrategy<I> getStrategy(String type) {
        FssAccessStrategy<I> strategy = this.strategies.get(type);
        if (strategy == null) {
            throw new BusinessException(FssExceptionCodes.NO_ACCESS_STRATEGY_FOR_TYPE, type);
        }
        return strategy;
    }

    @Override
    public String write(String type, String modelIdentity, I userIdentity, String filename,
            InputStream in) throws IOException {
        FssAccessStrategy<I> strategy = getStrategy(type);
        String extension = validateExtension(strategy, userIdentity, filename);
        FssProvider provider = strategy.getProvider();
        // 用BufferedInputStream装载以确保输入流可以标记和重置位置
        in = new BufferedInputStream(in);
        in.mark(Integer.MAX_VALUE);
        String path;
        if (strategy.isMd5AsFilename()) {
            String md5Code = EncryptUtil.encryptByMd5(in);
            in.reset();
            path = strategy.getRelativePath(modelIdentity, userIdentity, md5Code + extension);
        } else {
            path = strategy.getRelativePath(modelIdentity, userIdentity, filename);
        }
        if (path == null) {
            throw new BusinessException(FssExceptionCodes.NO_WRITE_AUTHORITY);
        }
        path = standardizePath(path);
        if (!strategy.isWritable(userIdentity, path)) {
            throw new BusinessException(FssExceptionCodes.NO_WRITE_AUTHORITY);
        }

        FssAccessor accessor = this.accessors.get(provider);
        if (accessor != null) {
            accessor.write(in, path, filename);
        }
        String contextPath = strategy.getContextPath();
        if (strategy.isPublicReadable()) {
            FssAuthorizer authorizer = this.authorizers.get(provider);
            authorizer.authorizePublicRead(contextPath, path);
        }
        return getStorageUrl(provider, contextPath, path);
    }

    private String validateExtension(FssAccessStrategy<I> strategy, I user, String filename) {
        String extension = FilenameUtils.getExtension(filename);
        FssUploadLimit uploadLimit = strategy.getUploadLimit(user);
        String[] extensions = uploadLimit.getExtensions();
        if (ArrayUtils.isNotEmpty(extensions)) { // 上传限制中没有设置扩展名，则不限定扩展名
            if (uploadLimit.isExtensionsRejected()) { // 拒绝扩展名模式
                if (ArrayUtil.containsIgnoreCase(extensions, extension)) {
                    throw new BusinessException(FssExceptionCodes.UNSUPPORTED_EXTENSION,
                            StringUtils.join(extensions, Strings.COMMA), filename);
                }
            } else { // 允许扩展名模式
                if (!ArrayUtil.containsIgnoreCase(extensions, extension)) {
                    throw new BusinessException(FssExceptionCodes.ONLY_SUPPORTED_EXTENSION,
                            StringUtils.join(extensions, Strings.COMMA), filename);
                }
            }
        }
        if (extension.length() > 0) {
            extension = Strings.DOT + extension;
        }
        return extension;
    }

    protected String getStorageUrl(FssProvider provider, String contextPath, String relativePath) {
        return new FssStorageUrl(provider, contextPath, relativePath).toString();
    }

    /**
     * 使路径格式标准化，以斜杠开头，不以斜杠结尾<br/>
     * 所有存储服务提供商均接收该标准的路径，如服务提供商对路径的要求与此不同，则服务提供商的实现类中再做转换
     *
     * @param path 标准化前的路径
     * @return 标准化后的路径
     */
    private String standardizePath(String path) {
        if (!path.startsWith(Strings.SLASH)) { // 以斜杠开头
            path = Strings.SLASH + path;
        }
        if (path.endsWith(Strings.SLASH)) { // 不能以斜杠结尾
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    @Override
    public String getReadUrl(I userIdentity, String storageUrl, boolean thumbnail) {
        FssStorageUrl url = buildStorageUrl(userIdentity, storageUrl);
        return getReadUrl(userIdentity, url, thumbnail);
    }

    private String getReadUrl(I userIdentity, FssStorageUrl url, boolean thumbnail) {
        if (url.isValid()) {
            String contextPath = standardizePath(url.getContextPath());
            String path = contextPath + standardizePath(url.getRelativePath());
            FssAccessStrategy<I> strategy = validateUserRead(userIdentity, path);
            // 使用内部协议确定的提供商而不是访问策略下现有的提供商，以免访问策略的历史提供商有变化
            FssProvider provider = url.getProvider();
            FssAuthorizer authorizer = this.authorizers.get(provider);
            if (thumbnail) {
                path = appendThumbnailParameters(strategy, path);
            }
            return authorizer.getReadUrl(userIdentity, contextPath, path);
        }
        return null;
    }

    private String appendThumbnailParameters(FssAccessStrategy<I> strategy, String path) {
        if (strategy != null) {
            Map<String, String> thumbnailParameters = strategy.getThumbnailParameters();
            if (thumbnailParameters != null && thumbnailParameters.size() > 0) {
                StringBuilder params = new StringBuilder();
                for (Entry<String, String> entry : thumbnailParameters.entrySet()) {
                    params.append(Strings.AND).append(entry.getKey()).append(Strings.EQUAL)
                            .append(entry.getValue());
                }
                if (params.length() > 0) {
                    params.deleteCharAt(0);
                }
                int index = path.indexOf(Strings.QUESTION);
                // 确保缩略参数作为优先参数
                if (index > 0) {
                    path = path.substring(0, index + 1) + params + Strings.AND
                            + path.substring(index + 1);
                } else {
                    path += Strings.QUESTION + params;
                }
            }
        }
        return path;
    }

    private FssAccessStrategy<I> validateUserRead(I userIdentity, String path) {
        // 根据上下文根路径判断所属访问策略，这要求所有的访问策略拥有各自唯一的上下文根路径
        return this.strategies.values().stream()
                .filter(s -> path.startsWith(s.getContextPath()) && s.isReadable(userIdentity, path)).findFirst()
                .orElseThrow(() -> {
                    // 如果没有找到匹配的访问策略，则说明没有读权限
                    return new BusinessException(FssExceptionCodes.NO_READ_AUTHORITY, path);
                });
    }

    private FssStorageUrl buildStorageUrl(I userIdentity, String storageUrl) {
        int index = storageUrl.indexOf("://");
        if (index > 0) {
            String protocol = storageUrl.substring(0, index);
            FssProvider provider = EnumUtils.getEnum(FssProvider.class, protocol.toUpperCase());
            if (provider != null) {
                String path = storageUrl.substring(index + 3);
                FssAccessStrategy<I> strategy = validateUserRead(userIdentity, path);
                if (strategy != null) {
                    String contextPath = strategy.getContextPath();
                    String relativePath = path.substring(contextPath.length());
                    return new FssStorageUrl(provider, contextPath, relativePath);
                }
            }
        }
        return null;
    }

    @Override
    public FssFileMeta getMeta(I userIdentity, String storageUrl) {
        FssStorageUrl url = buildStorageUrl(userIdentity, storageUrl);
        if (url != null) {
            FssAccessor accessor = this.accessors.get(url.getProvider());
            if (accessor != null) {
                String filename = accessor.getFilename(url.getPath());
                if (filename != null) {
                    String thumbnailReadUrl = getReadUrl(userIdentity, url, true);
                    String readUrl = getReadUrl(userIdentity, url, false);
                    return new FssFileMeta(filename, storageUrl, readUrl, thumbnailReadUrl);
                }
            }
        }
        return null;
    }

    @Override
    public Long getLastModifiedTime(I userIdentity, String path) {
        path = standardizePath(path);
        FssAccessStrategy<I> strategy = validateUserRead(userIdentity, path);
        FssAccessor accessor = this.accessors.get(strategy.getProvider());
        if (accessor != null) {
            return accessor.getLastModifiedTime(path);
        }
        return null;
    }

    @Override
    public void read(I userIdentity, String path, OutputStream out)
            throws IOException {
        path = standardizePath(path);
        FssAccessStrategy<I> strategy = validateUserRead(userIdentity, path); // 校验读取权限
        FssAccessor accessor = this.accessors.get(strategy.getProvider());
        if (accessor != null) {
            accessor.read(path, out);
        }
    }

}
