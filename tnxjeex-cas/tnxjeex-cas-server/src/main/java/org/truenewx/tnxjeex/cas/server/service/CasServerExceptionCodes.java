package org.truenewx.tnxjeex.cas.server.service;

/**
 * CAS服务器异常错误码集
 */
public class CasServerExceptionCodes {

    private CasServerExceptionCodes() {
    }

    /**
     * 无效的service参数
     */
    public static final String INVALID_SERVICE = "error.cas.server.invalid_service";

    /**
     * 不支持的用户类型
     */
    public static final String UNSUPPORTED_USER_TYPE = "error.cas.server.unsupported_user_type";

}
