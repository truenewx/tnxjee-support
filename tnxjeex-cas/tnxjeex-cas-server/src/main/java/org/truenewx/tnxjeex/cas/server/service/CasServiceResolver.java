package org.truenewx.tnxjeex.cas.server.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * CAS服务解决器
 *
 * @author jianglei
 */
public interface CasServiceResolver {

    String resolveUserType(String service);

    String resolveLoginUrl(HttpServletRequest request, String service);

    List<String> resolveLogoutUrls(HttpServletRequest request, String excludedService);

}
