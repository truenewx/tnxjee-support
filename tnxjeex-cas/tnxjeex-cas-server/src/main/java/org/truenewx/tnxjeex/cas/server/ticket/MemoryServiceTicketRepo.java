package org.truenewx.tnxjeex.cas.server.ticket;

import java.util.*;
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
    public List<String> deleteByTicketGrantingTicket(String ticketGrantingTicket) {
        List<String> ids = new ArrayList<>();
        findByTicketGrantingTicket(ticketGrantingTicket).forEach(ticket -> {
            String ticketId = ticket.getId();
            this.ticketMapping.remove(ticketId);
            ids.add(ticketId);
        });
        return ids;
    }

}
