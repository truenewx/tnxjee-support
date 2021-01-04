package org.truenewx.tnxjeex.cas.server.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import org.truenewx.tnxjee.web.util.WebUtil;

/**
 * Cas服务端工具类
 */
public class CasServerUtil {

    private CasServerUtil() {
    }

    public static String getService(HttpServletRequest request) {
        String service = WebUtil.getParameterOrAttribute(request, CasServerConstants.PARAMETER_SERVICE);
        return service == null ? null : URLDecoder.decode(service, StandardCharsets.UTF_8);
    }

}
