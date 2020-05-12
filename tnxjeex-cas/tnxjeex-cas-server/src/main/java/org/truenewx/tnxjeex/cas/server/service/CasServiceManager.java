package org.truenewx.tnxjeex.cas.server.service;

import javax.servlet.http.HttpServletRequest;

/**
 * CAS服务管理器
 *
 * @author jianglei
 */
public interface CasServiceManager {

    String resolveUserType(String service);

    String getAuthenticatedTargetUrl(HttpServletRequest request, String service);

}
