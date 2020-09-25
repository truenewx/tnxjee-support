package org.truenewx.tnxjeex.cas.server.ticket;

import java.util.Collection;
import java.util.Date;

/**
 * 服务票据仓库
 */
public interface ServiceTicketRepo {

    void save(ServiceTicket ticket);

    ServiceTicket findById(String id);

    long countByTicketGrantingTicketAndEarliestExpiredTime(String ticketGrantingTicket,
            Date earliestExpiredTime);

    Collection<ServiceTicket> findByTicketGrantingTicket(String ticketGrantingTicket);

    ServiceTicket findByTicketGrantingTicketAndService(String ticketGrantingTicket, String service);

    void deleteById(String id);

    /**
     * @return 被删除的服务票据id集合
     */
    Collection<ServiceTicket> deleteByTicketGrantingTicket(String ticketGrantingTicket);

}
