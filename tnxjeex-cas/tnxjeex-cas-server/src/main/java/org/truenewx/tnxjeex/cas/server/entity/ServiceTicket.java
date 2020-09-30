package org.truenewx.tnxjeex.cas.server.entity;

import java.util.Date;

import org.truenewx.tnxjee.model.entity.unity.Unity;
import org.truenewx.tnxjee.model.spec.user.security.UserSpecificDetails;

/**
 * 服务票据
 */
public class ServiceTicket implements Unity<String> {

    private String id;
    private String service;
    private UserSpecificDetails<?> userDetails;
    private Date createTime;
    private Date expiredTime;

    public ServiceTicket() {
    }

    public ServiceTicket(String id) {
        setId(id);
    }

    @Override
    public String getId() {
        return this.id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    public String getService() {
        return this.service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public UserSpecificDetails<?> getUserDetails() {
        return this.userDetails;
    }

    public void setUserDetails(UserSpecificDetails<?> userDetails) {
        this.userDetails = userDetails;
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
