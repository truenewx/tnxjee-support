package org.truenewx.tnxjeex.fss.web.tag;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.truenewx.tnxjee.core.util.SpringUtil;
import org.truenewx.tnxjee.service.exception.BusinessException;
import org.truenewx.tnxjee.webmvc.util.SpringWebmvcUtil;
import org.truenewx.tnxjeex.fss.api.FssReadUrlResolver;

/**
 * 格式化日期输出标签
 *
 * @author jianglei
 */
public class FssReadUrlTag extends SimpleTagSupport {

    private String value;
    private boolean thumbnail;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void setValue(String value) {
        this.value = value;
    }

    public void setThumbnail(boolean thumbnail) {
        this.thumbnail = thumbnail;
    }

    private FssReadUrlResolver getReadUrlResolver() {
        PageContext pageContext = (PageContext) getJspContext();
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        ApplicationContext context = SpringWebmvcUtil.getApplicationContext(request);
        if (context != null) {
            return SpringUtil.getFirstBeanByClass(context, FssReadUrlResolver.class);
        }
        return null;
    }

    @Override
    public void doTag() throws JspException, IOException {
        FssReadUrlResolver readUrlResolver = getReadUrlResolver();
        if (readUrlResolver != null) {
            try {
                String readUrl = readUrlResolver.resolveReadUrl(this.value, this.thumbnail);
                if (readUrl != null) {
                    JspWriter out = getJspContext().getOut();
                    out.print(readUrl);
                }
            } catch (BusinessException e) {
                this.logger.error(e.getMessage(), e);
            }
        }
    }

}
