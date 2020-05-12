package org.truenewx.tnxjeex.cas.server.ticket;

import java.util.Date;

import org.truenewx.tnxjee.model.entity.unity.AbstractUnity;
import org.truenewx.tnxjee.model.spec.user.UserIdentity;

/**
 * 服务票据
 */
public class ServiceTicket extends AbstractUnity<String> {

    private String ticketGrantingTicket;
    private UserIdentity<?> userIdentity;
    private String service;
    private Date createTime;
    private Date expiredTime;

    public ServiceTicket() {
    }

    public ServiceTicket(String id) {
        setId(id);
    }

    public String getTicketGrantingTicket() {
        return this.ticketGrantingTicket;
    }

    public void setTicketGrantingTicket(String ticketGrantingTicket) {
        this.ticketGrantingTicket = ticketGrantingTicket;
    }

    public UserIdentity<?> getUserIdentity() {
        return this.userIdentity;
    }

    public void setUserIdentity(UserIdentity<?> userIdentity) {
        this.userIdentity = userIdentity;
    }

    public String getService() {
        return this.service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getExpiredTime() {
        return this.expiredTime;
    }

    public void setExpiredTime(Date expiredTime) {
        this.expiredTime = expiredTime;
    }
}
