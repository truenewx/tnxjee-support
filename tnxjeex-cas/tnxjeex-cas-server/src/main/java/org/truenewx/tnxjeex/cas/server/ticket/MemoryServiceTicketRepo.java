package org.truenewx.tnxjeex.cas.server.ticket;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * 内存中的服务票据仓库
 */
public class MemoryServiceTicketRepo implements ServiceTicketRepo {

    private final Map<String, ServiceTicket> tickets = new Hashtable<>();

    @Override
    public void save(ServiceTicket ticket) {
        this.tickets.put(ticket.getId(), ticket);
    }

    @Override
    public ServiceTicket findById(String id) {
        return this.tickets.get(id);
    }

    @Override
    public ServiceTicket findByTicketGrantingTicketAndService(String ticketGrantingTicket,
            String service) {
        for (ServiceTicket ticket : this.tickets.values()) {
            if (ticket.getTicketGrantingTicket().equals(ticketGrantingTicket)
                    && ticket.getService().equals(service)) {
                return ticket;
            }
        }
        return null;
    }

    @Override
    public void deleteById(String id) {
        this.tickets.remove(id);
    }

    @Override
    public List<String> deleteByTicketGrantingTicket(String ticketGrantingTicket) {
        List<String> ids = new ArrayList<>();
        this.tickets.values().stream()
                .filter(ticket -> ticket.getTicketGrantingTicket().equals(ticketGrantingTicket))
                .forEach(ticket -> {
                    String id = ticket.getId();
                    this.tickets.remove(id);
                    ids.add(id);
                });
        return ids;
    }

}
