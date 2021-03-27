package org.truenewx.tnxjeex.office.excel.imports;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.truenewx.tnxjee.core.enums.EnumDictResolver;
import org.truenewx.tnxjee.core.util.BeanUtil;
import org.truenewx.tnxjee.core.util.ClassUtil;
import org.truenewx.tnxjee.core.util.MathUtil;
import org.truenewx.tnxjee.service.exception.message.CodedErrorResolver;
import org.truenewx.tnxjee.service.exception.model.CodedError;
import org.truenewx.tnxjeex.office.excel.ExcelRow;

/**
 * Excel导入协助者
 *
 * @author jianglei
 */
@Component
public class ExcelImportHelper {

    @Autowired
    private CodedErrorResolver codedErrorResolver;
    @Autowired
    private EnumDictResolver enumDictResolver;

    public void addSheetError(ImportingExcelSheetModel<?> sheetModel, String code, Locale locale, Object... args) {
        CodedError error = this.codedErrorResolver.resolveError(code, locale, args);
        sheetModel.getErrors().add(error);
    }

    public void addRowError(ImportingExcelRowModel rowModel, String code, Locale locale, Object... args) {
        CodedError error = this.codedErrorResolver.resolveError(code, locale, args);
        rowModel.getRowErrors().add(error);
    }

    public void addCellError(ImportingExcelRowModel rowModel, String fieldName, Object fieldValue, String errorCode,
            Locale locale, Object... args) {
        CodedError error = this.codedErrorResolver.resolveError(errorCode, locale, args);
        rowModel.addCellError(fieldName, error, fieldValue == null ? null : fieldValue.toString());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void applyRequiredValue(ImportingExcelRowModel rowModel, ExcelRow row, int columnIndex, String fieldName,
            Locale locale) {
        Object value = null;
        Field field = ClassUtil.findField(rowModel.getClass(), fieldName);
        Class<?> fieldType = field.getType();
        if (fieldType == String.class) {
            value = row.getStringCellValue(columnIndex);
        } else if (fieldType.isEnum()) {
            String caption = row.getStringCellValue(columnIndex);
            value = this.enumDictResolver.getEnumConstantByCaption((Class<Enum>) fieldType, caption, locale);
        } else if (fieldType == LocalDate.class) {
            value = row.getLocalDateCellValue(columnIndex);
        } else {
            BigDecimal decimal = row.getNumericCellValue(columnIndex);
            value = MathUtil.toValue(decimal, fieldType);
        }
        if (value == null || (value instanceof String && StringUtils.isBlank((String) value))) {
            String originalText = row.getStringCellValue(columnIndex);
            if (StringUtils.isBlank(originalText)) {
                addCellError(rowModel, fieldName, originalText, ExcelImportExceptionCodes.CELL_REQUIRED, locale);
            }
        } else {
            BeanUtil.setPropertyValue(rowModel, fieldName, value);
        }
    }

}
