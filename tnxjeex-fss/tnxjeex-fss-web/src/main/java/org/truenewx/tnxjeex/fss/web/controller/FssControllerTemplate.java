package org.truenewx.tnxjeex.fss.web.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.*;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.model.spec.user.UserIdentity;
import org.truenewx.tnxjee.web.context.SpringWebContext;
import org.truenewx.tnxjee.web.security.config.annotation.ConfigAnonymous;
import org.truenewx.tnxjee.web.util.WebUtil;
import org.truenewx.tnxjeex.fss.service.FssServiceTemplate;
import org.truenewx.tnxjeex.fss.service.model.FssReadMetadata;
import org.truenewx.tnxjeex.fss.service.model.FssUploadLimit;
import org.truenewx.tnxjeex.fss.web.config.FssWebProperties;
import org.truenewx.tnxjeex.fss.web.model.UploadResult;
import org.truenewx.tnxjeex.fss.web.resolver.FssReadUrlResolver;

import com.aliyun.oss.internal.Mimetypes;

/**
 * 文件存储控制器模板
 *
 * @author jianglei
 */
@EnableConfigurationProperties(FssWebProperties.class)
public abstract class FssControllerTemplate<T extends Enum<T>, I extends UserIdentity>
        implements FssReadUrlResolver {

    @Autowired(required = false)
    private FssServiceTemplate<T, I> service;
    @Autowired(required = false)
    private FssWebProperties webProperties;

    /**
     * 获取指定用户上传指定业务类型的文件上传限制条件
     *
     * @param type 业务类型
     * @return 指定用户上传指定业务类型的文件上传限制条件
     */
    @GetMapping("/upload-limit/{type}")
    @ResponseBody
    public FssUploadLimit getUploadLimit(T type) {
        return this.service.getUploadLimit(type, getUserIdentity());
    }

    // 跨域上传支持
    @RequestMapping(value = "/upload/{type}", method = RequestMethod.OPTIONS)
    public String upload(@PathVariable("type") T type, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", Strings.ASTERISK);
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "x-requested-with,content-type");
        response.setHeader("Access-Control-Max-Age", "30");
        return null;
    }

    @PostMapping("/upload/{type}")
    @ResponseBody
    public List<UploadResult> upload(@PathVariable("type") T type, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        return upload(type, null, request, response);
    }

    @PostMapping("/upload/{type}/{resource}")
    @ResponseBody
    public List<UploadResult> upload(@PathVariable("type") T type,
            @PathVariable("resource") String resource, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        List<UploadResult> results = new ArrayList<>();
        FileItemFactory fileItemFactory = new DiskFileItemFactory();
        ServletFileUpload servletFileUpload = new ServletFileUpload(fileItemFactory);
        servletFileUpload.setHeaderEncoding(Strings.ENCODING_UTF8);
        List<FileItem> fileItems = servletFileUpload.parseRequest(request);
        for (FileItem fileItem : fileItems) {
            if (!fileItem.isFormField()) {
                String filename = fileItem.getName();
                InputStream in = fileItem.getInputStream();
                // 注意：此处获得的输入流大小与原始文件的大小可能不相同，可能变大或变小
                I user = getUserIdentity();
                String storageUrl = this.service.write(type, resource, user, filename, in);
                in.close();

                UploadResult result;
                boolean noReadUrl = Boolean.parseBoolean(request.getParameter("noReadUrl"));
                if (!noReadUrl) { // 指定不需要返回读取地址，则不需要生成读取地址
                    String readUrl = this.service.getReadUrl(user, storageUrl, false);
                    readUrl = getFullReadUrl(readUrl);
                    // 缩略读取地址附加的缩略参数对最终URL可能产生影响，故需要重新生成，而不能在读取URL上简单附加缩略参数
                    String thumbnailReadUrl = this.service.getReadUrl(user, storageUrl, true);
                    thumbnailReadUrl = getFullReadUrl(thumbnailReadUrl);
                    result = new UploadResult(filename, storageUrl, readUrl, thumbnailReadUrl);
                } else {
                    result = new UploadResult(filename, storageUrl, null, null);
                }
                results.add(result);
            }
        }
        // 跨域上传支持
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Origin", Strings.ASTERISK);
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "x-requested-with,content-type");
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
        // 读取地址以/开头但不以//开头，则视为相对地址，相对地址需考虑添加主机地址和上下文根
        if (readUrl != null && readUrl.length() > 1 && readUrl.startsWith(Strings.SLASH) && readUrl.charAt(1) != '/') {
            // 先加上上下文根路径
            String contextPath = SpringWebContext.getRequest().getContextPath();
            if (!contextPath.equals(Strings.SLASH)) {
                readUrl = contextPath + readUrl;
            }
            // 再加上主机地址
            String host = this.webProperties.getHost();
            if (host != null) {
                String requestUrl = SpringWebContext.getRequest().getRequestURL().toString();
                // 如果配置的主机地址以//开头，说明允许各种访问协议，此时需去掉请求地址中的协议部分再进行比较
                if (host.startsWith("//")) {
                    int index = requestUrl.indexOf("://");
                    requestUrl = requestUrl.substring(index + 1); // 让请求地址也以//开头
                }
                // 当前请求地址与非结构化存储的外部读取主机地址不一致，则需要将主机地址加入读取地址中
                if (!requestUrl.startsWith(host)) {
                    readUrl = host + readUrl;
                }
            }
        }
        return readUrl;
    }

    /**
     * 当前用户获取指定内部存储URL集对应的资源读取元信息集<br/>
     *
     * @param storageUrls 内部存储URL集
     * @return 资源读取元信息集
     */
    @GetMapping("/read-metadatas")
    @ConfigAnonymous // 默认匿名可获取，用户读取权限控制由各方针决定
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
    @ConfigAnonymous // 默认匿名可下载，用户读取权限控制由各策略决定
    public String download(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String url = getBucketAndPathFragmentUrl(request);
        int index = url.indexOf(Strings.SLASH);
        String bucket = url.substring(0, index);
        String path = url.substring(index + 1);

        long modifiedSince = request.getDateHeader("If-Modified-Since");
        I user = getUserIdentity();
        long modifiedTime = this.service.getLastModifiedTime(user, bucket, path);
        response.setDateHeader("Last-Modified", modifiedTime);
        response.setContentType(Mimetypes.getInstance().getMimetype(path));
        if (modifiedSince == modifiedTime) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED); // 如果相等则返回表示未修改的状态码
        } else {
            ServletOutputStream out = response.getOutputStream();
            this.service.read(user, bucket, path, out);
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
