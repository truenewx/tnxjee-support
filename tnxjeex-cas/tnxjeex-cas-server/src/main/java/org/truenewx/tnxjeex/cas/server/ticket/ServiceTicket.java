package org.truenewx.tnxjeex.cas.server.ticket;

import java.util.Date;

import org.truenewx.tnxjee.model.spec.user.UserIdentity;

/**
 * 服务票据
 */
public class ServiceTicket {

    private String sessionId;
    private UserIdentity<?> userIdentity;
    private String service;
    private String id;
    private Date createTime;
    private Date expiredTime;

    public ServiceTicket(String sessionId, UserIdentity<?> userIdentity, String service, String id,
            Date createTime, Date expiredTime) {
        this.sessionId = sessionId;
        this.userIdentity = userIdentity;
        this.service = service;
        this.id = id;
        this.createTime = createTime;
        this.expiredTime = expiredTime;
    }


    public String getSessionId() {
        return this.sessionId;
    }

    public UserIdentity<?> getUserIdentity() {
        return this.userIdentity;
    }

    public String getService() {
        return this.service;
    }

    public String getId() {
        return this.id;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public Date getExpiredTime() {
        return this.expiredTime;
    }
}
