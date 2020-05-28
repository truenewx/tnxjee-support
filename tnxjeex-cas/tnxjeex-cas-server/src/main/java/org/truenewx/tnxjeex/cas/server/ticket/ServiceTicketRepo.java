package org.truenewx.tnxjeex.cas.server.ticket;

import java.util.List;

/**
 * 服务票据仓库
 */
public interface ServiceTicketRepo {

    void save(ServiceTicket ticket);

    ServiceTicket findById(String id);

    ServiceTicket findByTicketGrantingTicketAndService(String ticketGrantingTicket, String service);

    void deleteById(String id);

    /**
     * @return 被删除的服务票据id集合
     */
    List<String> deleteByTicketGrantingTicket(String ticketGrantingTicket);

}
