package org.truenewx.tnxjeex.cas.server.repo;

import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;

import org.truenewx.tnxjeex.cas.server.entity.ServiceTicket;

/**
 * 内存中的服务票据仓库
 */
public class MemoryServiceTicketRepo implements ServiceTicketRepo {

    private Map<String, ServiceTicket> dataMapping = new Hashtable<>();

    @Override
    public void save(ServiceTicket unity) {
        if (unity != null) {
            this.dataMapping.put(unity.getId(), unity);
        }
    }

    @Override
    public Optional<ServiceTicket> findById(String id) {
        return Optional.ofNullable(this.dataMapping.get(id));
    }

    @Override
    public void delete(ServiceTicket unity) {
        if (unity != null) {
            this.dataMapping.remove(unity.getId());
        }
    }

}
