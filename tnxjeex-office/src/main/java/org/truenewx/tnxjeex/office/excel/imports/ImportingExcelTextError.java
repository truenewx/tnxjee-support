package org.truenewx.tnxjeex.office.excel.imports;

import org.truenewx.tnxjee.service.exception.model.CodedError;

/**
 * 导入时的Excel文本错误
 */
public class ImportingExcelTextError extends CodedError {

    private String originalText;

    public ImportingExcelTextError() {
    }

    public ImportingExcelTextError(String code, String message, String originalText) {
        super(code, message);
        this.originalText = originalText;
    }

    public String getOriginalText() {
        return this.originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

}
