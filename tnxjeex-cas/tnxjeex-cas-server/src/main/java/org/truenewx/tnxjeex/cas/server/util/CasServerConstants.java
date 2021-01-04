package org.truenewx.tnxjeex.cas.server.util;

import org.truenewx.tnxjee.core.Strings;

/**
 * CAS服务端常量类
 */
public class CasServerConstants {

    private CasServerConstants() {
    }

    /**
     * 服务的用户类型：全部。表示服务不限定用户类型，支持所有可能的用户类型
     */
    public static final String SERVICE_USER_TYPE_ALL = Strings.ASTERISK;

    /**
     * 参数：服务
     */
    public static final String PARAMETER_SERVICE = "service";

    /**
     * 参数：范围
     */
    public static final String PARAMETER_SCOPE = "scope";

    /**
     * 参数：制品
     */
    public static final String PARAMETER_ARTIFACT = "ticket";

}
