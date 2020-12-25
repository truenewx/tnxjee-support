package org.truenewx.tnxjeex.cas.server.repo;

import java.util.Collection;
import java.util.Optional;

import org.truenewx.tnxjeex.cas.server.entity.ServiceTicket;

/**
 * 服务票据仓库
 */
public interface ServiceTicketRepo {

    void save(ServiceTicket unity);

    Optional<ServiceTicket> findById(String id);

    ServiceTicket findFirstByTicketGrantingTicketIdAndService(String ticketGrantingTicketId, String service);

    Collection<ServiceTicket> deleteByTicketGrantingTicketId(String ticketGrantingTicketId);

}
