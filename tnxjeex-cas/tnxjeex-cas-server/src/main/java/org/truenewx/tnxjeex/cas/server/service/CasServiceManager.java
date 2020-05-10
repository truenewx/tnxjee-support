package org.truenewx.tnxjeex.cas.server.service;

/**
 * CAS服务管理器
 *
 * @author jianglei
 */
public interface CasServiceManager {

    String resolveUserType(String service);

    String getAuthenticatedTargetUrl(String service);

}
