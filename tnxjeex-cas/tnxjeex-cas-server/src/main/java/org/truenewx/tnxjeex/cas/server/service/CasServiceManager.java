package org.truenewx.tnxjeex.cas.server.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * CAS服务管理器
 *
 * @author jianglei
 */
public interface CasServiceManager {

    String getUserType(String service);

    String getHost(String service);

    String getLoginUrl(HttpServletRequest request, String service);

    Map<String, String> getLogoutUrls(String[] services);
}
