package org.truenewx.tnxjeex.fss.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.truenewx.tnxjeex.fss.model.FssFileMeta;

/**
 * 文件存储服务的元数据解决器
 */
public interface FssMetaResolver {

    @GetMapping("/metas")
    FssFileMeta[] resolveMetas(@RequestParam("storageUrls") String[] storageUrls);

}
