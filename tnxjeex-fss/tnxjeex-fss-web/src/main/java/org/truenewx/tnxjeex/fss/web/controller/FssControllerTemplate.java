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
import org.truenewx.tnxjee.web.bind.annotation.ResponseStream;
import org.truenewx.tnxjee.web.context.SpringWebContext;
import org.truenewx.tnxjee.web.security.config.annotation.ConfigAnonymous;
import org.truenewx.tnxjee.web.security.config.annotation.ConfigAuthority;
import org.truenewx.tnxjee.web.security.util.SecurityUtil;
import org.truenewx.tnxjee.web.util.WebUtil;
import org.truenewx.tnxjeex.fss.service.FssServiceTemplate;
import org.truenewx.tnxjeex.fss.service.model.FssFileMeta;
import org.truenewx.tnxjeex.fss.service.model.FssUploadLimit;
import org.truenewx.tnxjeex.fss.web.model.FssUploadedFileMeta;
import org.truenewx.tnxjeex.fss.web.resolver.FssReadUrlResolver;

import com.aliyun.oss.internal.Mimetypes;

/**
 * 文件存储控制器模板
 *
 * @author jianglei
 */
public abstract class FssControllerTemplate<I extends UserIdentity<?>>
        implements FssReadUrlResolver {

    @Autowired(required = false)
    private FssServiceTemplate<I> service;

    /**
     * 获取指定用户上传指定业务类型的文件上传限制条件
     *
     * @param type 业务类型
     * @return 指定用户上传指定业务类型的文件上传限制条件
     */
    @GetMapping("/upload-limit/{type}")
    @ResponseBody
    @ConfigAuthority // 登录用户才可上传文件，访问策略可能还有更多限定
    public FssUploadLimit getUploadLimit(@PathVariable("type") String type) {
        return this.service.getUploadLimit(type, getUserIdentity());
    }

    @PostMapping("/upload/{type}")
    @ResponseBody
    @ConfigAuthority // 登录用户才可上传文件，访问策略可能还有更多限定
    public List<FssUploadedFileMeta> upload(@PathVariable("type") String type,
            MultipartHttpServletRequest request) {
        return upload(type, null, request);
    }

    @PostMapping("/upload/{type}/{modelIdentity}")
    @ResponseBody
    @ConfigAuthority // 登录用户才可上传文件，访问策略可能还有更多限定
    public List<FssUploadedFileMeta> upload(@PathVariable("type") String type,
            @PathVariable("modelIdentity") String modelIdentity,
            MultipartHttpServletRequest request) {
        List<FssUploadedFileMeta> results = new ArrayList<>();
        String[] fileIds = request.getParameterValues("fileIds");
        Collection<MultipartFile> files = request.getFiles("files");
        int index = 0;
        for (MultipartFile file : files) {
            String fileId = fileIds[index++];
            try {
                String filename = file.getOriginalFilename();
                InputStream in = file.getInputStream();

                // 注意：此处获得的输入流大小与原始文件的大小可能不相同，可能变大或变小
                I userIdentity = getUserIdentity();
                String storageUrl = this.service.write(type, modelIdentity, userIdentity, filename, in);
                in.close();

                FssUploadedFileMeta result;
                boolean onlyStorage = Boolean.parseBoolean(request.getParameter("onlyStorage"));
                if (onlyStorage) { // 只需要存储地址
                    result = new FssUploadedFileMeta(fileId, null, storageUrl, null, null);
                } else {
                    String readUrl = this.service.getReadUrl(userIdentity, storageUrl, false);
                    readUrl = getFullReadUrl(readUrl);
                    // 缩略读取地址附加的缩略参数对最终URL可能产生影响，故需要重新生成，而不能在读取URL上简单附加缩略参数
                    String thumbnailReadUrl = this.service.getReadUrl(userIdentity, storageUrl,
                            true);
                    thumbnailReadUrl = getFullReadUrl(thumbnailReadUrl);
                    result = new FssUploadedFileMeta(fileId, filename, storageUrl, readUrl, thumbnailReadUrl);
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
            String host = WebUtil.getHost(SpringWebContext.getRequest(), true);
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
     * 当前用户获取指定存储URL集对应的文件元数据集
     *
     * @param storageUrls 存储URL集
     * @return 文件元数据集
     */
    @GetMapping("/metas")
    @ResponseBody
    @ConfigAnonymous // 匿名用户即可读取，具体权限由访问策略决定
    public FssFileMeta[] metas(@RequestParam("storageUrls") String[] storageUrls) {
        FssFileMeta[] metas = new FssFileMeta[storageUrls.length];
        for (int i = 0; i < storageUrls.length; i++) {
            if (StringUtils.isNotBlank(storageUrls[i])) {
                FssFileMeta meta = this.service.getMeta(getUserIdentity(), storageUrls[i]);
                if (meta != null) {
                    meta.setReadUrl(getFullReadUrl(meta.getReadUrl()));
                    meta.setThumbnailReadUrl(getFullReadUrl(meta.getThumbnailReadUrl()));
                    metas[i] = meta;
                }
            }
        }
        return metas;
    }

    @GetMapping("/dl/**")
    @ResponseStream
    @ConfigAnonymous // 匿名用户即可读取，具体权限由访问策略决定
    public String download(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        I userIdentity = getUserIdentity();
        String path = getDownloadPath(request);
        long modifiedTime = this.service.getLastModifiedTime(userIdentity, path);
        response.setDateHeader(HttpHeaders.LAST_MODIFIED, modifiedTime);
        response.setContentType(Mimetypes.getInstance().getMimetype(path));
        long modifiedSince = request.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE);
        if (modifiedSince == modifiedTime) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED); // 如果相等则返回表示未修改的状态码
        } else {
            ServletOutputStream out = response.getOutputStream();
            this.service.read(userIdentity, path, out);
            out.close();
        }
        return null;
    }

    protected String getDownloadPath(HttpServletRequest request) {
        String url = WebUtil.getRelativeRequestUrl(request);
        url = URLDecoder.decode(url, StandardCharsets.UTF_8);
        String downloadUrlPrefix = getDownloadUrlPrefix();
        int index = url.indexOf(downloadUrlPrefix + Strings.SLASH);
        return url.substring(index + downloadUrlPrefix.length()); // 通配符部分
    }

    protected I getUserIdentity() {
        return SecurityUtil.getAuthorizedUserIdentity();
    }

}
