package org.truenewx.tnxjeex.notice.service.sms;

import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.truenewx.tnxjee.core.parser.SimpleElTemplateParser;
import org.truenewx.tnxjee.core.parser.TemplateParser;

/**
 * 基于模版的短信提供者
 *
 * @author jianglei
 */
public class TemplateSmsProvider extends AbstractSmsProvider implements MessageSourceAware {
    private String code;
    private MessageSource messageSource;
    private TemplateParser parser = new SimpleElTemplateParser();

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void setParser(TemplateParser parser) {
        this.parser = parser;
    }

    @Override
    public String getContent(Map<String, Object> params, Locale locale) {
        String templateContent = this.messageSource.getMessage(this.code, null, locale);
        return this.parser.parse(templateContent, params, locale);
    }

}
