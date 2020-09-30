package org.truenewx.tnxjeex.cas.server.entity;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;

import org.truenewx.tnxjee.model.entity.unity.Unity;

/**
 * 票据授权票据
 */
public class TicketGrantingTicket implements Unity<String> {

    private String id;
    private Collection<ServiceTicket> serviceTickets = new LinkedHashSet<>();
    private Date createTime;
    private Date expiredTime;

    public TicketGrantingTicket() {
    }

    public TicketGrantingTicket(String id) {
        setId(id);
    }

    @Override
    public String getId() {
        return this.id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    public Collection<ServiceTicket> getServiceTickets() {
        return this.serviceTickets;
    }

    protected void setServiceTickets(Collection<ServiceTicket> serviceTickets) {
        this.serviceTickets = serviceTickets;
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

    public ServiceTicket getServiceTicketByService(String service) {
        for (ServiceTicket serviceTicket : this.serviceTickets) {
            if (service.equals(serviceTicket.getService())) {
                return serviceTicket;
            }
        }
        return null;
    }

}
