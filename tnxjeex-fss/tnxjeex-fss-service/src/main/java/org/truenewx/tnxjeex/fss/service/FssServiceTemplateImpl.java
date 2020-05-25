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
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.core.beans.ContextInitializedBean;
import org.truenewx.tnxjee.core.util.ArrayUtil;
import org.truenewx.tnxjee.core.util.EncryptUtil;
import org.truenewx.tnxjee.core.util.NetUtil;
import org.truenewx.tnxjee.model.spec.user.UserIdentity;
import org.truenewx.tnxjee.service.exception.BusinessException;
import org.truenewx.tnxjeex.fss.service.model.FssFileMeta;
import org.truenewx.tnxjeex.fss.service.model.FssProvider;
import org.truenewx.tnxjeex.fss.service.model.FssStoragePathAnalysis;
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
        String relativePath;
        if (strategy.isMd5AsFilename()) {
            String md5Code = EncryptUtil.encryptByMd5(in);
            in.reset();
            relativePath = strategy.getRelativePath(modelIdentity, userIdentity,
                    md5Code + extension);
        } else {
            relativePath = strategy.getRelativePath(modelIdentity, userIdentity, filename);
        }
        if (relativePath == null) {
            throw new BusinessException(FssExceptionCodes.NO_WRITE_AUTHORITY);
        }
        relativePath = NetUtil.standardizeUrl(relativePath);

        String contextPath = NetUtil.standardizeUrl(strategy.getContextPath());
        String path = contextPath + relativePath;
        FssAccessor accessor = this.accessors.get(provider);
        accessor.write(in, path, filename);
        // 写好文件之后，如果访问策略是公开匿名可读，则还需要进行相应授权，不过本地自有提供商无需进行授权
        if (strategy.isPublicReadable() && provider != FssProvider.OWN) {
            FssAuthorizer authorizer = this.authorizers.get(provider);
            authorizer.authorizePublicRead(path);
        }
        return new FssStoragePathAnalysis(type, relativePath).getUrl();
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

    @Override
    public String getReadUrl(I userIdentity, String storageUrl, boolean thumbnail) {
        FssStoragePathAnalysis spa = FssStoragePathAnalysis.of(storageUrl);
        return getReadUrl(userIdentity, spa, thumbnail);
    }

    private String getReadUrl(I userIdentity, FssStoragePathAnalysis spa, boolean thumbnail) {
        if (spa != null && spa.isValid()) {
            FssAccessStrategy<I> strategy = validateUserRead(userIdentity, spa);
            FssProvider provider = strategy.getProvider();
            if (provider == FssProvider.OWN) {
                // 本地自有提供商的读取URL与存储URL保持一致，以便于读取时判断所属访问策略
                return spa.toString();
            } else {
                FssAuthorizer authorizer = this.authorizers.get(provider);
                String path = strategy.getContextPath() + spa.getRelativePath();
                if (thumbnail) {
                    path = appendThumbnailParameters(strategy, path);
                }
                return authorizer.getReadUrl(userIdentity, path);
            }
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

    private FssAccessStrategy<I> validateUserRead(I userIdentity, FssStoragePathAnalysis spa) {
        if (spa != null && spa.isValid()) {
            FssAccessStrategy<I> strategy = this.strategies.get(spa.getType());
            if (strategy != null && strategy.isReadable(userIdentity, spa.getRelativePath())) {
                return strategy;
            }
        }
        throw new BusinessException(FssExceptionCodes.NO_READ_AUTHORITY, spa.getUrl());
    }

    @Override
    public FssFileMeta getMeta(I userIdentity, String storageUrl) {
        FssStoragePathAnalysis spa = FssStoragePathAnalysis.of(storageUrl);
        FssAccessStrategy<I> strategy = validateUserRead(userIdentity, spa);
        FssAccessor accessor = this.accessors.get(strategy.getProvider());
        String path = strategy.getContextPath() + spa.getRelativePath();
        String filename = accessor.getFilename(path);
        if (filename != null) {
            String thumbnailReadUrl = getReadUrl(userIdentity, spa, true);
            String readUrl = getReadUrl(userIdentity, spa, false);
            return new FssFileMeta(filename, storageUrl, readUrl, thumbnailReadUrl);
        }
        return null;
    }

    @Override
    public Long getLastModifiedTime(I userIdentity, String path) {
        path = NetUtil.standardizeUrl(path);
        FssStoragePathAnalysis spa = FssStoragePathAnalysis.of(path);
        FssAccessStrategy<I> strategy = validateUserRead(userIdentity, spa);
        FssAccessor accessor = this.accessors.get(strategy.getProvider());
        path = strategy.getContextPath() + spa.getRelativePath();
        return accessor.getLastModifiedTime(path);
    }

    @Override
    public void read(I userIdentity, String path, OutputStream out) throws IOException {
        path = NetUtil.standardizeUrl(path);
        FssStoragePathAnalysis spa = FssStoragePathAnalysis.of(path);
        FssAccessStrategy<I> strategy = validateUserRead(userIdentity, spa);
        FssAccessor accessor = this.accessors.get(strategy.getProvider());
        path = strategy.getContextPath() + spa.getRelativePath();
        accessor.read(path, out);
    }

}
