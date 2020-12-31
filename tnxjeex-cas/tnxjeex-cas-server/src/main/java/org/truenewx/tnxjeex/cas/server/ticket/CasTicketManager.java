package org.truenewx.tnxjeex.cas.server.ticket;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.validation.Assertion;
import org.truenewx.tnxjee.service.Service;
import org.truenewx.tnxjeex.cas.server.entity.ServiceTicket;

/**
 * CAS票据管理器
 */
public interface CasTicketManager extends Service {

    String TGT_NAME = "CASTGC";
    String TICKET_GRANTING_TICKET_PREFIX = "TGT-";
    String SERVICE_TICKET_PREFIX = "ST-";

    void createTicketGrantingTicket(HttpServletRequest request, HttpServletResponse response);

    boolean checkTicketGrantingTicket(HttpServletRequest request);

    String getServiceTicket(HttpServletRequest request, String service, String scope);

    Collection<ServiceTicket> deleteTicketGrantingTicket(HttpServletRequest request,
            HttpServletResponse response);

    Assertion validateServiceTicket(String service, String serviceTicketId);
}
