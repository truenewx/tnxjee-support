package org.truenewx.tnxjeex.unstructured.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.core.beans.ContextInitializedBean;
import org.truenewx.tnxjee.core.util.EncryptUtil;
import org.truenewx.tnxjee.model.spec.user.UserIdentity;
import org.truenewx.tnxjee.model.spec.user.UserSpecific;
import org.truenewx.tnxjee.service.api.exception.BusinessException;
import org.truenewx.tnxjeex.unstructured.service.model.UnstructuredProvider;
import org.truenewx.tnxjeex.unstructured.service.model.UnstructuredReadMetadata;
import org.truenewx.tnxjeex.unstructured.service.model.UnstructuredStorageMetadata;
import org.truenewx.tnxjeex.unstructured.service.model.UnstructuredStorageUrl;
import org.truenewx.tnxjeex.unstructured.service.model.UnstructuredUploadLimit;

/**
 * 非结构化存储服务模版实现
 *
 * @author jianglei
 *
 */
public class UnstructuredServiceTemplateImpl<T extends Enum<T>, U>
        implements UnstructuredServiceTemplate<T, U>, ContextInitializedBean {

    private Map<T, UnstructuredAuthorizePolicy<T, U>> policies = new HashMap<>();
    private Map<UnstructuredProvider, UnstructuredAuthorizer> authorizers = new HashMap<>();
    private Map<UnstructuredProvider, UnstructuredProviderAccessor> accessors = new HashMap<>();
    private UnstructuredLocalAccessor localAccessor;

    @Autowired
    public void setLocalAccessor(UnstructuredLocalAccessor localAccessor) {
        this.localAccessor = localAccessor;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void afterInitialized(ApplicationContext context) throws Exception {
        Map<String, UnstructuredAuthorizePolicy> policies = context
                .getBeansOfType(UnstructuredAuthorizePolicy.class);
        for (UnstructuredAuthorizePolicy<T, U> policy : policies.values()) {
            this.policies.put(policy.getType(), policy);
        }

        Map<String, UnstructuredAuthorizer> authorizers = context
                .getBeansOfType(UnstructuredAuthorizer.class);
        for (UnstructuredAuthorizer authorizer : authorizers.values()) {
            this.authorizers.put(authorizer.getProvider(), authorizer);
        }

        Map<String, UnstructuredProviderAccessor> accessors = context
                .getBeansOfType(UnstructuredProviderAccessor.class);
        for (UnstructuredProviderAccessor accessor : accessors.values()) {
            this.accessors.put(accessor.getProvider(), accessor);
        }
    }

    @Override
    public UnstructuredUploadLimit getUploadLimit(T authorizeType, U user) {
        return getPolicy(authorizeType).getUploadLimit(user);
    }

    private UnstructuredAuthorizePolicy<T, U> getPolicy(T authorizeType) {
        UnstructuredAuthorizePolicy<T, U> policy = this.policies.get(authorizeType);
        if (policy == null) {
            throw new BusinessException(UnstructuredExceptionCodes.NO_POLICY_FOR_AUTHORIZE_TYPE,
                    authorizeType.name());
        }
        return policy;
    }

    @Override
    public String write(T authorizeType, String token, U user, String filename, InputStream in)
            throws IOException {
        UnstructuredAuthorizePolicy<T, U> policy = getPolicy(authorizeType);
        String extension = validateExtension(policy, user, filename);
        UnstructuredProvider provider = policy.getProvider();
        // 用BufferedInputStream装载以确保输入流可以标记和重置位置
        in = new BufferedInputStream(in);
        in.mark(Integer.MAX_VALUE);
        String path;
        if (policy.isMd5AsFilename()) {
            String md5Code = EncryptUtil.encryptByMd5(in);
            in.reset();
            path = policy.getPath(token, user, md5Code + extension);
        } else {
            path = policy.getPath(token, user, filename);
        }
        if (path == null) {
            throw new BusinessException(UnstructuredExceptionCodes.NO_WRITE_PERMISSION);
        }
        path = standardizePath(path);
        if (!policy.isWritable(user, path)) {
            throw new BusinessException(UnstructuredExceptionCodes.NO_WRITE_PERMISSION);
        }

        String bucket = policy.getBucket();
        // 如果方针指定需要本地存储，则进行本地存储；
        // 但如果此时服务提供商是自有，则为了避免重复存储，跳过本地存储
        if (policy.isStoreLocally() && provider != UnstructuredProvider.OWN) {
            this.localAccessor.write(bucket, path, filename, in);
        }
        UnstructuredProviderAccessor providerAccessor = this.accessors.get(provider);
        if (providerAccessor != null) {
            in.reset(); // 读取输入流之前先重置，以重新读取
            providerAccessor.write(bucket, path, filename, in);
        }
        if (policy.isPublicReadable()) {
            UnstructuredAuthorizer authorizer = this.authorizers.get(provider);
            authorizer.authorizePublicRead(bucket, path);
        }
        return getStorageUrl(provider, bucket, path);
    }

    private String validateExtension(UnstructuredAuthorizePolicy<T, U> policy, U user,
            String filename) {
        String extension = FilenameUtils.getExtension(filename);
        UnstructuredUploadLimit uploadLimit = policy.getUploadLimit(user);
        String[] extensions = uploadLimit.getExtensions();
        if (ArrayUtils.isNotEmpty(extensions)) { // 上传限制中没有设置扩展名，则不限定扩展名
            if (uploadLimit.isRejectedExtension()) { // 拒绝扩展名模式
                if (ArrayUtils.contains(extensions, extension)) {
                    throw new BusinessException(UnstructuredExceptionCodes.UNSUPPORTED_EXTENSION,
                            StringUtils.join(extensions, Strings.COMMA), filename);
                }
            } else { // 允许扩展名模式
                if (!ArrayUtils.contains(extensions, extension)) {
                    throw new BusinessException(UnstructuredExceptionCodes.ONLY_SUPPORTED_EXTENSION,
                            StringUtils.join(extensions, Strings.COMMA), filename);
                }
            }
        }
        if (extension.length() > 0) {
            extension = Strings.DOT + extension;
        }
        return extension;
    }

    protected String getStorageUrl(UnstructuredProvider provider, String bucket, String path) {
        return new UnstructuredStorageUrl(provider, bucket, path).toString();
    }

    /**
     * 使路径格式标准化，不以斜杠开头，也不以斜杠结尾<br/>
     * 所有存储服务提供商均接收该标准的路径，如服务提供商对路径的要求与此不同，则服务提供商的实现类中再做转换
     *
     * @param path 标准化前的路径
     * @return 标准化后的路径
     */
    private String standardizePath(String path) {
        if (path.startsWith(Strings.SLASH)) { // 不能以斜杠开头
            return path.substring(1);
        }
        if (path.endsWith(Strings.SLASH)) { // 不能以斜杠结尾
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    @Override
    public String getReadUrl(U user, String storageUrl, boolean thumbnail) {
        return getReadUrl(user, new UnstructuredStorageUrl(storageUrl), thumbnail);
    }

    private String getReadUrl(U user, UnstructuredStorageUrl url, boolean thumbnail) {
        if (url.isValid()) {
            String bucket = url.getBucket();
            String path = standardizePath(url.getPath());
            UnstructuredAuthorizePolicy<T, U> policy = validateUserRead(user, bucket, path);
            // 如果方针要求读取地址为本地地址，则使用自有提供商
            UnstructuredProvider provider = policy.isReadLocally() ? UnstructuredProvider.OWN
                    : url.getProvider(); // 使用内部协议确定的提供商而不是方针下现有的提供商，以免方针的历史提供商有变化
            UnstructuredAuthorizer authorizer = this.authorizers.get(provider);
            String userKey = getUserKey(user);
            if (thumbnail) {
                path = appendThumbnailParameters(policy, path);
            }
            return authorizer.getReadUrl(userKey, bucket, path);
        }
        return null;
    }

    protected String getUserKey(U user) {
        if (user != null) {
            if (user instanceof UserSpecific) {
                UserIdentity identity = ((UserSpecific<?>) user).getIdentity();
                return identity == null ? null : identity.toString();
            }
            return user.toString();
        }
        return null;
    }

    private String appendThumbnailParameters(UnstructuredAuthorizePolicy<T, U> policy,
            String path) {
        if (policy != null) {
            Map<String, String> thumbnailParameters = policy.getThumbnailParameters();
            if (thumbnailParameters != null && thumbnailParameters.size() > 0) {
                String parameterString = Strings.EMPTY;
                for (Entry<String, String> entry : thumbnailParameters.entrySet()) {
                    parameterString += Strings.AND + entry.getKey() + Strings.EQUAL
                            + entry.getValue();
                }
                if (parameterString.length() > 0) {
                    parameterString = parameterString.substring(1);
                }
                int index = path.indexOf(Strings.QUESTION);
                // 确保缩略参数作为优先参数
                if (index > 0) {
                    path = path.substring(0, index + 1) + parameterString + Strings.AND
                            + path.substring(index + 1);
                } else {
                    path += Strings.QUESTION + parameterString;
                }
            }
        }
        return path;
    }

    private UnstructuredAuthorizePolicy<T, U> validateUserRead(U user, String bucket, String path) {
        // 存储桶相同，且用户对指定路径具有读权限，则匹配
        // 这要求方针具有唯一的存储桶，或者与其它方针的存储桶相同时，下级存放路径不同
        UnstructuredAuthorizePolicy<T, U> policy = this.policies.values().stream()
                .filter(p -> p.getBucket().equals(bucket) && p.isReadable(user, path)).findFirst()
                .orElse(null);
        if (policy == null) {
            // 如果没有找到匹配的方针，则说明没有读权限
            String url = Strings.SLASH + bucket + path;
            throw new BusinessException(UnstructuredExceptionCodes.NO_READ_PERMISSION, url);
        }
        return policy;
    }

    @Override
    public UnstructuredReadMetadata getReadMetadata(U user, String storageUrl) {
        UnstructuredStorageUrl url = new UnstructuredStorageUrl(storageUrl);
        String readUrl = getReadUrl(user, url, false);
        if (readUrl != null) { // 不为null，则说明存储url有效且用户权限校验通过
            // 先尝试从本地获取
            UnstructuredStorageMetadata storageMetadata = this.localAccessor
                    .getStorageMetadata(url.getBucket(), url.getPath());
            if (storageMetadata == null) {
                // 本地无法获取才尝试从服务提供商处获取
                UnstructuredProvider provider = url.getProvider();
                UnstructuredProviderAccessor providerAccessor = this.accessors.get(provider);
                if (providerAccessor != null) {
                    storageMetadata = providerAccessor.getStorageMetadata(url.getBucket(),
                            url.getPath());
                }
            }
            if (storageMetadata != null) {
                String thumbnailReadUrl = getReadUrl(user, url, true);
                return new UnstructuredReadMetadata(readUrl, thumbnailReadUrl, storageMetadata);
            }
        }
        return null;
    }

    @Override
    public long getLastModifiedTime(U user, String bucket, String path) {
        path = standardizePath(path);
        UnstructuredAuthorizePolicy<T, U> policy = validateUserRead(user, bucket, path);
        long lastModifiedTime = this.localAccessor.getLastModifiedTime(bucket, path);
        if (lastModifiedTime <= 0) {
            UnstructuredProviderAccessor providerAccessor = this.accessors
                    .get(policy.getProvider());
            if (providerAccessor != null) {
                lastModifiedTime = providerAccessor.getLastModifiedTime(bucket, path);
            }
        }
        return lastModifiedTime;
    }

    @Override
    public void read(U user, String bucket, String path, OutputStream out) throws IOException {
        path = standardizePath(path);
        UnstructuredAuthorizePolicy<T, U> policy = validateUserRead(user, bucket, path); // 校验读取权限
        if (!this.localAccessor.read(bucket, path, out)) {
            UnstructuredProviderAccessor providerAccessor = this.accessors
                    .get(policy.getProvider());
            if (providerAccessor != null) {
                providerAccessor.read(bucket, path, out);
            }
        }
    }

}
