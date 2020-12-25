package org.truenewx.tnxjeex.cas.server.repo;

import java.util.*;

import org.truenewx.tnxjeex.cas.server.entity.ServiceTicket;

/**
 * 内存中的服务票据仓库
 */
public class MemoryServiceTicketRepo implements ServiceTicketRepo {

    private final Map<String, ServiceTicket> dataMapping = new Hashtable<>(); // serviceTicketId - serviceTicket
    private final Map<String, Set<String>> ticketIdMapping = new Hashtable<>(); // ticketGrantingTicketId - serviceTicketIds

    @Override
    public void save(ServiceTicket unity) {
        if (unity != null) {
            synchronized (this.ticketIdMapping) {
                String id = unity.getId();
                this.dataMapping.put(id, unity);
                String ticketGrantingTicketId = unity.getTicketGrantingTicket().getId();
                Set<String> ids = this.ticketIdMapping.computeIfAbsent(ticketGrantingTicketId, key -> new HashSet<>());
                ids.add(id);
            }
        }
    }

    @Override
    public Optional<ServiceTicket> findById(String id) {
        return Optional.ofNullable(this.dataMapping.get(id));
    }

    @Override
    public ServiceTicket findFirstByTicketGrantingTicketIdAndService(String ticketGrantingTicketId, String service) {
        Set<String> ids = this.ticketIdMapping.get(ticketGrantingTicketId);
        if (ids != null) {
            for (String id : ids) {
                ServiceTicket serviceTicket = this.dataMapping.get(id);
                if (serviceTicket != null && serviceTicket.getService().equals(service)) {
                    return serviceTicket;
                }
            }
        }
        return null;
    }

    @Override
    public Collection<ServiceTicket> deleteByTicketGrantingTicketId(String ticketGrantingTicketId) {
        Collection<ServiceTicket> result = new ArrayList<>();
        synchronized (this.ticketIdMapping) {
            Set<String> ids = this.ticketIdMapping.remove(ticketGrantingTicketId);
            if (ids != null) {
                for (String id : ids) {
                    ServiceTicket serviceTicket = this.dataMapping.remove(id);
                    if (serviceTicket != null) {
                        result.add(serviceTicket);
                    }
                }
            }
        }
        return result;
    }

}
