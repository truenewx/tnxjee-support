package org.truenewx.tnxjeex.cas.server.ticket;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.validation.Assertion;
import org.truenewx.tnxjee.service.Service;

/**
 * 票据管理器
 */
public interface TicketManager extends Service {

    String TICKET_GRANTING_TICKET_PREFIX = "TGT-";

    String SERVICE_TICKET_PREFIX = "ST-";

    void createTicketGrantingTicket(HttpServletRequest request, HttpServletResponse response);

    boolean validateTicketGrantingTicket(HttpServletRequest request);

    Map<String, String> deleteServiceTickets(HttpServletRequest request);

    String getServiceTicket(HttpServletRequest request, String service, boolean create);

    Assertion validateServiceTicket(String service, String ticketId);

}
