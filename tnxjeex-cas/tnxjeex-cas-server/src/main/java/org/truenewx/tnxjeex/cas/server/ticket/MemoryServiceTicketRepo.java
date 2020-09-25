package org.truenewx.tnxjeex.cas.server.ticket;

import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 内存中的服务票据仓库
 */
public class MemoryServiceTicketRepo implements ServiceTicketRepo {

    private final Map<String, ServiceTicket> ticketMapping = new Hashtable<>();

    @Override
    public void save(ServiceTicket ticket) {
        this.ticketMapping.put(ticket.getId(), ticket);
    }

    @Override
    public ServiceTicket findById(String id) {
        return this.ticketMapping.get(id);
    }

    @Override
    public long countByTicketGrantingTicketAndEarliestExpiredTime(String ticketGrantingTicket,
            Date earliestExpiredTime) {
        long count = 0;
        for (ServiceTicket ticket : this.ticketMapping.values()) {
            if (ticket.getTicketGrantingTicket().equals(ticketGrantingTicket) && ticket.getExpiredTime()
                    .after(earliestExpiredTime)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Collection<ServiceTicket> findByTicketGrantingTicket(String ticketGrantingTicket) {
        return this.ticketMapping.values().stream().filter(ticket -> {
            return ticket.getTicketGrantingTicket().equals(ticketGrantingTicket);
        }).collect(Collectors.toList());
    }

    @Override
    public ServiceTicket findByTicketGrantingTicketAndService(String ticketGrantingTicket,
            String service) {
        for (ServiceTicket ticket : this.ticketMapping.values()) {
            if (ticket.getTicketGrantingTicket().equals(ticketGrantingTicket)
                    && ticket.getService().equals(service)) {
                return ticket;
            }
        }
        return null;
    }

    @Override
    public void deleteById(String id) {
        this.ticketMapping.remove(id);
    }

    @Override
    public Collection<ServiceTicket> deleteByTicketGrantingTicket(String ticketGrantingTicket) {
        Collection<ServiceTicket> tickets = findByTicketGrantingTicket(ticketGrantingTicket);
        tickets.forEach(ticket -> {
            this.ticketMapping.remove(ticket.getId());
        });
        return tickets;
    }

}
