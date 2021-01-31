package org.truenewx.tnxjeex.office.excel;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;

/**
 * Excel行
 *
 * @author jianglei
 * @since JDK 1.8
 */
public class ExcelRow {

    private HSSFRow origin;
    private ExcelSheet sheet;

    public ExcelRow(ExcelSheet sheet, HSSFRow origin) {
        this.origin = origin;
        this.sheet = sheet;
    }

    public HSSFRow getOrigin() {
        return this.origin;
    }

    public ExcelSheet getSheet() {
        return this.sheet;
    }

    public ExcelCell createCell(int columnIndex) {
        HSSFCell cell = this.origin.createCell(columnIndex);
        return new ExcelCell(this, cell);
    }

    public ExcelCell getCell(int columnIndex, boolean createIfNull) {
        HSSFCell cell = this.origin.getCell(columnIndex);
        if (cell == null && createIfNull) {
            return createCell(columnIndex);
        }
        return cell == null ? null : new ExcelCell(this, cell);
    }

    public ExcelCell getCell(int columnIndex) {
        return getCell(columnIndex, false);
    }

    public int getRowIndex() {
        return this.origin.getRowNum();
    }

    public ExcelCell setCellValue(int columnIndex, Object value) {
        ExcelCell cell = getCell(columnIndex);
        if (cell != null) {
            cell.setCellValue(value);
        }
        return cell;
    }

    public void setHeightInPoints(Number height) {
        this.origin.setHeightInPoints(height.floatValue());
    }

}
