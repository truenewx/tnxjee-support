package org.truenewx.tnxjeex.fss.web.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.core.util.LogUtil;
import org.truenewx.tnxjee.model.spec.user.UserIdentity;
import org.truenewx.tnxjee.web.context.SpringWebContext;
import org.truenewx.tnxjee.web.security.config.annotation.ConfigAnonymous;
import org.truenewx.tnxjee.web.util.WebUtil;
import org.truenewx.tnxjeex.fss.service.FssServiceTemplate;
import org.truenewx.tnxjeex.fss.service.model.FssReadMetadata;
import org.truenewx.tnxjeex.fss.service.model.FssUploadLimit;
import org.truenewx.tnxjeex.fss.web.model.FssUploadedFile;
import org.truenewx.tnxjeex.fss.web.resolver.FssReadUrlResolver;

import com.aliyun.oss.internal.Mimetypes;

/**
 * 文件存储控制器模板
 *
 * @author jianglei
 */
@ConfigAnonymous // 匿名即可访问，具体的权限控制由各访问策略决定
public abstract class FssControllerTemplate<T extends Enum<T>, I extends UserIdentity>
        implements FssReadUrlResolver {

    @Autowired(required = false)
    private FssServiceTemplate<T, I> service;

    /**
     * 获取指定用户上传指定业务类型的文件上传限制条件
     *
     * @param type 业务类型
     * @return 指定用户上传指定业务类型的文件上传限制条件
     */
    @GetMapping("/upload-limit/{type}")
    @ResponseBody
    public FssUploadLimit getUploadLimit(@PathVariable("type") T type) {
        return this.service.getUploadLimit(type, getUserIdentity());
    }

    @PostMapping("/upload/{type}")
    @ResponseBody
    public List<FssUploadedFile> upload(@PathVariable("type") T type,
            MultipartHttpServletRequest request) {
        return upload(type, null, request);
    }

    @PostMapping("/upload/{type}/{resource}")
    @ResponseBody
    public List<FssUploadedFile> upload(@PathVariable("type") T type,
            @PathVariable("resource") String resource, MultipartHttpServletRequest request) {
        List<FssUploadedFile> results = new ArrayList<>();
        String[] fileIds = request.getParameterValues("fileIds");
        Collection<MultipartFile> files = request.getFileMap().values();
        int index = 0;
        for (MultipartFile file : files) {
            String fileId = fileIds[index++];
            try {
                String filename = file.getOriginalFilename();
                InputStream in = file.getInputStream();

                // 注意：此处获得的输入流大小与原始文件的大小可能不相同，可能变大或变小
                I userIdentity = getUserIdentity();
                String storageUrl = this.service.write(type, resource, userIdentity, filename, in);
                in.close();

                FssUploadedFile result;
                boolean onlyStorage = Boolean.parseBoolean(request.getParameter("onlyStorage"));
                if (onlyStorage) { // 只需要存储地址
                    result = new FssUploadedFile(fileId, null, storageUrl, null, null);
                } else {
                    String readUrl = this.service.getReadUrl(userIdentity, storageUrl, false);
                    readUrl = getFullReadUrl(readUrl);
                    // 缩略读取地址附加的缩略参数对最终URL可能产生影响，故需要重新生成，而不能在读取URL上简单附加缩略参数
                    String thumbnailReadUrl = this.service.getReadUrl(userIdentity, storageUrl,
                            true);
                    thumbnailReadUrl = getFullReadUrl(thumbnailReadUrl);
                    result = new FssUploadedFile(fileId, filename, storageUrl, readUrl, thumbnailReadUrl);
                }
                results.add(result);
            } catch (IOException e) {
                LogUtil.error(getClass(), e);
            }
        }
        return results;
    }

    @Override
    public String getReadUrl(String storageUrl, boolean thumbnail) {
        if (StringUtils.isNotBlank(storageUrl)) {
            String readUrl = this.service.getReadUrl(getUserIdentity(), storageUrl, thumbnail);
            return getFullReadUrl(readUrl);
        }
        return null;
    }

    private String getFullReadUrl(String readUrl) {
        // 读取地址以/开头但不以//开头，则视为相对地址，相对地址需考虑添加下载路径前缀、上下文根和主机地址
        if (readUrl != null && readUrl.startsWith(Strings.SLASH) && !readUrl.startsWith("//")) {
            // 加上下载路径前缀
            readUrl = getDownloadUrlPrefix() + readUrl;
            // 加上上下文根路径
            String contextPath = SpringWebContext.getRequest().getContextPath();
            if (!contextPath.equals(Strings.SLASH)) {
                readUrl = contextPath + readUrl;
            }
            // 加上主机地址
            String host = WebUtil.getHost(SpringWebContext.getRequest());
            readUrl = "//" + host + readUrl;
        }
        return readUrl;
    }

    /**
     * 获取下载路径前缀<br/>
     * 子类如果覆写，必须与download()方法的路径前缀相同
     *
     * @return 下载路径前缀
     */
    protected String getDownloadUrlPrefix() {
        return "/dl";
    }

    /**
     * 当前用户获取指定内部存储URL集对应的资源读取元信息集<br/>
     *
     * @param storageUrls 内部存储URL集
     * @return 资源读取元信息集
     */
    @GetMapping("/read-metadatas")
    @ResponseBody
    public FssReadMetadata[] getReadMetadatas(@RequestParam("storageUrls") String[] storageUrls) {
        FssReadMetadata[] metadatas = new FssReadMetadata[storageUrls.length];
        for (int i = 0; i < storageUrls.length; i++) {
            metadatas[i] = this.service.getReadMetadata(getUserIdentity(), storageUrls[i]);
            if (metadatas[i] != null) {
                String readUrl = metadatas[i].getReadUrl();
                readUrl = getFullReadUrl(readUrl);
                metadatas[i].setReadUrl(readUrl);
            }
        }
        return metadatas;
    }

    @GetMapping("/dl/**")
    public String download(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String url = getBucketAndPathFragmentUrl(request);
        int index = url.indexOf(Strings.SLASH);
        String bucket = url.substring(0, index);
        String path = url.substring(index + 1);

        long modifiedSince = request.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE);
        I userIdentity = getUserIdentity();
        long modifiedTime = this.service.getLastModifiedTime(userIdentity, bucket, path);
        response.setDateHeader(HttpHeaders.LAST_MODIFIED, modifiedTime);
        response.setContentType(Mimetypes.getInstance().getMimetype(path));
        if (modifiedSince == modifiedTime) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED); // 如果相等则返回表示未修改的状态码
        } else {
            ServletOutputStream out = response.getOutputStream();
            this.service.read(userIdentity, bucket, path, out);
            out.close();
        }
        return null;
    }

    /**
     * 获取存储桶和路径所在的URL片段，子类可覆写实现自定义的路径格式
     *
     * @param request HTTP请求
     * @return 存储桶和路径所在的URL片段
     */
    protected String getBucketAndPathFragmentUrl(HttpServletRequest request) {
        String url = WebUtil.getRelativeRequestUrl(request);
        url = URLDecoder.decode(url, StandardCharsets.UTF_8);
        int index = url.indexOf("/dl/");
        return url.substring(index + 4); // 通配符部分
    }

    protected abstract I getUserIdentity();

}
