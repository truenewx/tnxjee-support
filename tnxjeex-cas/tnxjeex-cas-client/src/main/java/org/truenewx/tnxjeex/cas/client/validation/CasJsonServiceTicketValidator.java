package org.truenewx.tnxjeex.cas.client.validation;

import org.jasig.cas.client.validation.AbstractCasProtocolUrlBasedTicketValidator;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;
import org.truenewx.tnxjee.core.util.JsonUtil;

public class CasJsonServiceTicketValidator extends AbstractCasProtocolUrlBasedTicketValidator {

    public CasJsonServiceTicketValidator(String casServerUrlPrefix) {
        super(casServerUrlPrefix);
    }

    @Override
    protected String getUrlSuffix() {
        return "serviceValidate";
    }

    @Override
    protected Assertion parseResponseFromServer(String response) throws TicketValidationException {
        try {
            return JsonUtil.json2Bean(response, SimpleAssertion.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
