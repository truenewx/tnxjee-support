package org.truenewx.tnxjeex.office.excel;

import java.time.temporal.Temporal;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.truenewx.tnxjee.core.Strings;
import org.truenewx.tnxjee.core.util.TemporalUtil;

/**
 * Excel单元格
 *
 * @author jianglei
 */
public class ExcelCell {

    private Cell origin;
    private ExcelRow row;

    public ExcelCell(ExcelRow row, Cell origin) {
        this.origin = origin;
        this.row = row;
    }

    public Cell getOrigin() {
        return this.origin;
    }

    public ExcelRow getRow() {
        return this.row;
    }

    public int getColumnIndex() {
        return this.origin.getColumnIndex();
    }

    public void setCellValue(Object value) {
        if (value instanceof Number) {
            this.origin.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Temporal) {
            this.origin.setCellValue(TemporalUtil.format((Temporal) value));
        } else {
            if (value == null) {
                value = Strings.EMPTY;
            }
            this.origin.setCellValue(value.toString());
        }
    }

    public String getStringCellValue() {
        return this.origin.getStringCellValue();
    }

    public void setCellStyle(HSSFCellStyle style) {
        this.origin.setCellStyle(style);
    }

    public CellStyle getCellStyle() {
        return this.origin.getCellStyle();
    }

    public void formatStringValue(Object... args) {
        String value = getStringCellValue();
        setCellValue(String.format(value, args));
    }

}
