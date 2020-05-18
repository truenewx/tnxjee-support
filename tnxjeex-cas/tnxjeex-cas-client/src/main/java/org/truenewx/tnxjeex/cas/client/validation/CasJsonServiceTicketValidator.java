package org.truenewx.tnxjeex.cas.client.validation;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.validation.AbstractCasProtocolUrlBasedTicketValidator;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;
import org.truenewx.tnxjee.core.tools.ClassGenerator;
import org.truenewx.tnxjee.core.tools.ClassGeneratorImpl;
import org.truenewx.tnxjee.core.util.JsonUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

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
        SimpleAssertion assertion = null;
        if (StringUtils.isNotBlank(response)) {
            assertion = JsonUtil.json2Bean(response, SimpleAssertion.class);
        }
        if (assertion == null) {
            throw new TicketValidationException("The service ticket is invalid");
        }
        if (!assertion.isValid()) {
            throw new TicketValidationException("The service ticket is expired");
        }
        return assertion;
    }

    public static void main(String[] args) {
        ClassGenerator classGenerator = new ClassGeneratorImpl();
        Class<? extends Assertion> assertionClass = classGenerator.generateSimple(Assertion.class);
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"validFromDate\":1589763028807,\"validUntilDate\":1589763928807,\"authenticationDate\":1589763028807,\"attributes\":{},\"principal\":{\"name\":\"manager:1\",\"attributes\":{}},\"valid\":true}";
        try {
            mapper.readValue(json, Assertion.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
