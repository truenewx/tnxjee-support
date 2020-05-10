package org.truenewx.tnxjeex.cas.server.ticket;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.client.validation.Assertion;

/**
 * 票据管理器
 */
public interface TicketManager {

    String TICKET_GRANTING_TICKET_PREFIX = "TGT-";

    String SERVICE_TICKET_PREFIX = "ST-";

    String getTicketGrantingTicket(HttpServletRequest request);

    String getServiceTicket(HttpServletRequest request, String service);

    Assertion validateServiceTicket(String service, String ticketId);

}
