package org.truenewx.tnxjeex.cas.server.service;

import javax.servlet.http.HttpServletRequest;

/**
 * CAS服务解决器
 *
 * @author jianglei
 */
public interface CasServiceResolver {

    String resolveUserType(String service);

    String resolveLoginUrl(HttpServletRequest request, String service);

}
