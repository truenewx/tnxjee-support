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
import org.truenewx.tnxjee.service.exception.BusinessException;
import org.truenewx.tnxjee.service.exception.message.CodedErrorResolver;
import org.truenewx.tnxjee.service.exception.model.CodedError;
import org.truenewx.tnxjeex.office.excel.ExcelExceptionCodes;
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
        rowModel.addFieldError(fieldName, fieldValue == null ? null : fieldValue.toString(), error);
    }

    public void addCellError(ImportingExcelRowModel rowModel, String fieldName, int index, Object fieldValue,
            String errorCode, Locale locale, Object... args) {
        CodedError error = this.codedErrorResolver.resolveError(errorCode, locale, args);
        rowModel.addFieldError(fieldName, index, fieldValue == null ? null : fieldValue.toString(), error);
    }

    public void addCellError(ImportingExcelRowModel rowModel, String fieldName, Object fieldValue, BusinessException be,
            Locale locale) {
        addCellError(rowModel, fieldName, fieldValue, be.getCode(), locale, be.getArgs());
    }

    public void addCellRequiredError(ImportingExcelRowModel rowModel, String fieldName, Locale locale) {
        addCellError(rowModel, fieldName, null, ExcelExceptionCodes.IMPORT_CELL_REQUIRED, locale);
    }

    public <E extends Enum<E>> E getEnumConstant(ExcelRow row, int columnIndex, Class<E> enumClass, Locale locale) {
        String caption = row.getStringCellValue(columnIndex);
        if (StringUtils.isNotBlank(caption)) {
            return this.enumDictResolver.getEnumConstantByCaption(enumClass, caption, locale);
        }
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void applyRequiredValue(ImportingExcelRowModel rowModel, ExcelRow row, int columnIndex, String fieldName,
            Locale locale) {
        Object value;
        Field field = ClassUtil.findField(rowModel.getClass(), fieldName);
        Class<?> fieldType = field.getType();
        if (fieldType == String.class) {
            value = row.getStringCellValue(columnIndex);
        } else if (fieldType.isEnum()) {
            value = getEnumConstant(row, columnIndex, (Class<Enum>) fieldType, locale);
        } else if (fieldType == LocalDate.class) {
            value = row.getLocalDateCellValue(columnIndex);
        } else {
            BigDecimal decimal = row.getNumericCellValue(columnIndex);
            value = MathUtil.toValue(decimal, fieldType);
        }
        applyRequiredValue(rowModel, fieldName, value, locale);
    }

    public void applyRequiredValue(ImportingExcelRowModel rowModel, String fieldName, Object fieldValue,
            Locale locale) {
        if (fieldValue == null || (fieldValue instanceof String && StringUtils.isBlank((String) fieldValue))) {
            addCellRequiredError(rowModel, fieldName, locale);
        } else {
            BeanUtil.setPropertyValue(rowModel, fieldName, fieldValue);
        }
    }


}
