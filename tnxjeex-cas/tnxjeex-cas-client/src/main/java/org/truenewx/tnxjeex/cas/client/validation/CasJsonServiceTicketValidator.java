package org.truenewx.tnxjeex.cas.client.validation;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.validation.AbstractCasProtocolUrlBasedTicketValidator;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;
import org.truenewx.tnxjee.core.util.JsonUtil;
import org.truenewx.tnxjee.core.util.LogUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 基于JSON数据格式的CAS服务票据校验器
 */
public class CasJsonServiceTicketValidator extends AbstractCasProtocolUrlBasedTicketValidator {

    private ObjectMapper objectMapper = JsonUtil.copyNonConcreteAndCollectionMapper();

    public CasJsonServiceTicketValidator(String casServerUrlPrefix) {
        super(casServerUrlPrefix);
    }

    @Override
    protected String getUrlSuffix() {
        return "serviceValidate";
    }

    @Override
    protected Assertion parseResponseFromServer(String response) throws TicketValidationException {
        Assertion assertion = null;
        if (StringUtils.isNotBlank(response)) {
            try {
                assertion = this.objectMapper.readValue(response, SimpleAssertion.class);
            } catch (IOException e) {
                LogUtil.error(getClass(), e);
            }
        }
        if (assertion == null) {
            throw new TicketValidationException("The service ticket is invalid");
        }
        if (!assertion.isValid()) {
            throw new TicketValidationException("The service ticket is expired");
        }
        return assertion;
    }

}
