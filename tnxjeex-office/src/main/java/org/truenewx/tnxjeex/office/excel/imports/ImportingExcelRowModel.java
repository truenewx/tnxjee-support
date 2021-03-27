package org.truenewx.tnxjeex.office.excel.imports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.truenewx.tnxjee.service.exception.model.CodedError;

/**
 * Excel导入时的行数据模型
 *
 * @author jianglei
 */
public abstract class ImportingExcelRowModel {

    private List<CodedError> rowErrors = new ArrayList<>();
    private Map<String, ImportingExcelTextError> cellErrors = new HashMap<>();

    public List<CodedError> getRowErrors() {
        return this.rowErrors;
    }

    public Map<String, ImportingExcelTextError> getCellErrors() {
        return this.cellErrors;
    }

    public void addCellError(String fieldName, CodedError error, String originalText) {
        this.cellErrors.put(fieldName, new ImportingExcelTextError(error.getCode(), error.getMessage(), originalText));
    }

}
