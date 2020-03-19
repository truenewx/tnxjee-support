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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.core.beans.ContextInitializedBean;
import org.truenewx.tnxjee.core.util.EncryptUtil;
import org.truenewx.tnxjee.model.spec.user.UserIdentity;
import org.truenewx.tnxjee.model.spec.user.UserSpecific;
import org.truenewx.tnxjee.service.exception.BusinessException;
import org.truenewx.tnxjeex.fss.service.model.FssProvider;
import org.truenewx.tnxjeex.fss.service.model.FssReadMetadata;
import org.truenewx.tnxjeex.fss.service.model.FssStorageMetadata;
import org.truenewx.tnxjeex.fss.service.model.FssStorageUrl;
import org.truenewx.tnxjeex.fss.service.model.FssUploadLimit;

/**
 * 文件存储服务模版实现
 *
 * @author jianglei
 *
 */
public class FssServiceTemplateImpl<T extends Enum<T>, U>
        implements FssServiceTemplate<T, U>, ContextInitializedBean {

    private Map<T, FssAuthorizePolicy<T, U>> policies = new HashMap<>();
    private Map<FssProvider, FssAuthorizer> authorizers = new HashMap<>();
    private Map<FssProvider, FssProviderAccessor> accessors = new HashMap<>();
    private FssLocalAccessor localAccessor;

    @Autowired
    public void setLocalAccessor(FssLocalAccessor localAccessor) {
        this.localAccessor = localAccessor;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void afterInitialized(ApplicationContext context) throws Exception {
        Map<String, FssAuthorizePolicy> policies = context
                .getBeansOfType(FssAuthorizePolicy.class);
        for (FssAuthorizePolicy<T, U> policy : policies.values()) {
            this.policies.put(policy.getType(), policy);
        }

        Map<String, FssAuthorizer> authorizers = context
                .getBeansOfType(FssAuthorizer.class);
        for (FssAuthorizer authorizer : authorizers.values()) {
            this.authorizers.put(authorizer.getProvider(), authorizer);
        }

        Map<String, FssProviderAccessor> accessors = context
                .getBeansOfType(FssProviderAccessor.class);
        for (FssProviderAccessor accessor : accessors.values()) {
            this.accessors.put(accessor.getProvider(), accessor);
        }
    }

    @Override
    public FssUploadLimit getUploadLimit(T authorizeType, U user) {
        return getPolicy(authorizeType).getUploadLimit(user);
    }

    private FssAuthorizePolicy<T, U> getPolicy(T authorizeType) {
        FssAuthorizePolicy<T, U> policy = this.policies.get(authorizeType);
        if (policy == null) {
            throw new BusinessException(FssExceptionCodes.NO_POLICY_FOR_AUTHORIZE_TYPE,
                    authorizeType.name());
        }
        return policy;
    }

    @Override
    public String write(T authorizeType, String token, U user, String filename, InputStream in)
            throws IOException {
        FssAuthorizePolicy<T, U> policy = getPolicy(authorizeType);
        String extension = validateExtension(policy, user, filename);
        FssProvider provider = policy.getProvider();
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
            throw new BusinessException(FssExceptionCodes.NO_WRITE_PERMISSION);
        }
        path = standardizePath(path);
        if (!policy.isWritable(user, path)) {
            throw new BusinessException(FssExceptionCodes.NO_WRITE_PERMISSION);
        }

        String bucket = policy.getBucket();
        // 如果方针指定需要本地存储，则进行本地存储；
        // 但如果此时服务提供商是自有，则为了避免重复存储，跳过本地存储
        if (policy.isStoreLocally() && provider != FssProvider.OWN) {
            this.localAccessor.write(bucket, path, filename, in);
        }
        FssProviderAccessor providerAccessor = this.accessors.get(provider);
        if (providerAccessor != null) {
            in.reset(); // 读取输入流之前先重置，以重新读取
            providerAccessor.write(bucket, path, filename, in);
        }
        if (policy.isPublicReadable()) {
            FssAuthorizer authorizer = this.authorizers.get(provider);
            authorizer.authorizePublicRead(bucket, path);
        }
        return getStorageUrl(provider, bucket, path);
    }

    private String validateExtension(FssAuthorizePolicy<T, U> policy, U user,
            String filename) {
        String extension = FilenameUtils.getExtension(filename);
        FssUploadLimit uploadLimit = policy.getUploadLimit(user);
        String[] extensions = uploadLimit.getExtensions();
        if (ArrayUtils.isNotEmpty(extensions)) { // 上传限制中没有设置扩展名，则不限定扩展名
            if (uploadLimit.isRejectedExtension()) { // 拒绝扩展名模式
                if (ArrayUtils.contains(extensions, extension)) {
                    throw new BusinessException(FssExceptionCodes.UNSUPPORTED_EXTENSION,
                            StringUtils.join(extensions, Strings.COMMA), filename);
                }
            } else { // 允许扩展名模式
                if (!ArrayUtils.contains(extensions, extension)) {
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

    protected String getStorageUrl(FssProvider provider, String bucket, String path) {
        return new FssStorageUrl(provider, bucket, path).toString();
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
        return getReadUrl(user, new FssStorageUrl(storageUrl), thumbnail);
    }

    private String getReadUrl(U user, FssStorageUrl url, boolean thumbnail) {
        if (url.isValid()) {
            String bucket = url.getBucket();
            String path = standardizePath(url.getPath());
            FssAuthorizePolicy<T, U> policy = validateUserRead(user, bucket, path);
            // 如果方针要求读取地址为本地地址，则使用自有提供商
            FssProvider provider = policy.isReadLocally() ? FssProvider.OWN
                    : url.getProvider(); // 使用内部协议确定的提供商而不是方针下现有的提供商，以免方针的历史提供商有变化
            FssAuthorizer authorizer = this.authorizers.get(provider);
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

    private String appendThumbnailParameters(FssAuthorizePolicy<T, U> policy,
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

    private FssAuthorizePolicy<T, U> validateUserRead(U user, String bucket, String path) {
        // 存储桶相同，且用户对指定路径具有读权限，则匹配
        // 这要求方针具有唯一的存储桶，或者与其它方针的存储桶相同时，下级存放路径不同
        FssAuthorizePolicy<T, U> policy = this.policies.values().stream()
                .filter(p -> p.getBucket().equals(bucket) && p.isReadable(user, path)).findFirst()
                .orElse(null);
        if (policy == null) {
            // 如果没有找到匹配的方针，则说明没有读权限
            String url = Strings.SLASH + bucket + path;
            throw new BusinessException(FssExceptionCodes.NO_READ_PERMISSION, url);
        }
        return policy;
    }

    @Override
    public FssReadMetadata getReadMetadata(U user, String storageUrl) {
        FssStorageUrl url = new FssStorageUrl(storageUrl);
        String readUrl = getReadUrl(user, url, false);
        if (readUrl != null) { // 不为null，则说明存储url有效且用户权限校验通过
            // 先尝试从本地获取
            FssStorageMetadata storageMetadata = this.localAccessor
                    .getStorageMetadata(url.getBucket(), url.getPath());
            if (storageMetadata == null) {
                // 本地无法获取才尝试从服务提供商处获取
                FssProvider provider = url.getProvider();
                FssProviderAccessor providerAccessor = this.accessors.get(provider);
                if (providerAccessor != null) {
                    storageMetadata = providerAccessor.getStorageMetadata(url.getBucket(),
                            url.getPath());
                }
            }
            if (storageMetadata != null) {
                String thumbnailReadUrl = getReadUrl(user, url, true);
                return new FssReadMetadata(readUrl, thumbnailReadUrl, storageMetadata);
            }
        }
        return null;
    }

    @Override
    public long getLastModifiedTime(U user, String bucket, String path) {
        path = standardizePath(path);
        FssAuthorizePolicy<T, U> policy = validateUserRead(user, bucket, path);
        long lastModifiedTime = this.localAccessor.getLastModifiedTime(bucket, path);
        if (lastModifiedTime <= 0) {
            FssProviderAccessor providerAccessor = this.accessors
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
        FssAuthorizePolicy<T, U> policy = validateUserRead(user, bucket, path); // 校验读取权限
        if (!this.localAccessor.read(bucket, path, out)) {
            FssProviderAccessor providerAccessor = this.accessors
                    .get(policy.getProvider());
            if (providerAccessor != null) {
                providerAccessor.read(bucket, path, out);
            }
        }
    }

}
