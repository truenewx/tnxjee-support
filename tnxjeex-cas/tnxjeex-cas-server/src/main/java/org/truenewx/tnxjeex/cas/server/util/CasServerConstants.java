package org.truenewx.tnxjeex.cas.server.util;

/**
 * CAS服务端常量类
 */
public class CasServerConstants {

    private CasServerConstants() {
    }

    /**
     * 参数：服务
     */
    public static final String PARAMETER_SERVICE = "service";
    /**
     * 头信息：登录地址
     */
    public static final String HEADER_LOGIN_URL = "Login-Url";
    /**
     * 头信息：原始请求
     */
    public static final String HEADER_ORIGINAL_REQUEST = "Original-Request";
    /**
     * Cookie：登出服务集
     */
    public static final String COOKIE_LOGOUT_SERVICES = "LogoutServices";
}
