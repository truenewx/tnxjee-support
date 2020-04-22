package org.truenewx.tnxjeex.fss.service.own;

import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.model.spec.user.UserIdentity;
import org.truenewx.tnxjeex.fss.service.FssAuthorizer;
import org.truenewx.tnxjeex.fss.service.model.FssProvider;

/**
 * 文件存储自有授权器
 *
 * @author jianglei
 */
public class OwnFssAuthorizer implements FssAuthorizer {

    @Override
    public FssProvider getProvider() {
        return FssProvider.OWN;
    }

    @Override
    public void authorizePublicRead(String bucket, String path) {
        // 本地资源本身没有权限限制，权限由访问策略进行限制和判断
    }

    @Override
    public String getReadUrl(UserIdentity userIdentity, String bucket, String path) {
        // 形如：/${bucket}/${path}，本地地址交由Controller完成上下文根路径的拼装
        return Strings.SLASH + bucket + Strings.SLASH + path;
    }

}
