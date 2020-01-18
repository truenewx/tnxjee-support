package org.truenewx.tnxjeex.fss.service;

/**
 * 文件存储异常代码类
 *
 * @author jianglei
 */
public class FssExceptionCodes {

    private FssExceptionCodes() {
    }

    /**
     * 授权类型无对应的方针
     */
    public static final String NO_POLICY_FOR_AUTHORIZE_TYPE = "error.fss.no_policy_for_authorize_type";

    /**
     * 没有写权限
     */
    public static final String NO_WRITE_PERMISSION = "error.fss.no_write_permission";

    /**
     * 没有读权限
     */
    public static final String NO_READ_PERMISSION = "error.fss.no_read_permission";

    /**
     * 仅支持指定扩展名
     */
    public static final String ONLY_SUPPORTED_EXTENSION = "error.fss.upload.only_supported_extension";

    /**
     * 不支持指定扩展名
     */
    public static final String UNSUPPORTED_EXTENSION = "error.fss.upload.unsupported_extension";

}
