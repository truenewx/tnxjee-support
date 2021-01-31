package org.truenewx.tnxjeex.office.excel;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

/**
 * Excel工作表
 *
 * @author jianglei
 */
public class ExcelSheet {

    private HSSFSheet origin;
    private ExcelDoc doc;

    public ExcelSheet(ExcelDoc doc, HSSFSheet origin) {
        this.doc = doc;
        this.origin = origin;
    }

    public HSSFSheet getOrigin() {
        return this.origin;
    }

    public ExcelDoc getDoc() {
        return this.doc;
    }

    public ExcelRow createRow(int rowIndex) {
        HSSFRow row = this.origin.createRow(rowIndex);
        return new ExcelRow(this, row);
    }

    public ExcelRow getRow(int rowIndex, boolean createIfNull) {
        HSSFRow row = this.origin.getRow(rowIndex);
        if (row == null && createIfNull) {
            return createRow(rowIndex);
        }
        return row == null ? null : new ExcelRow(this, row);
    }

    public ExcelRow getRow(int rowIndex) {
        return getRow(rowIndex, false);
    }

    public void mergeCells(int firstRowIndex, int firstColumnIndex, int lastRowIndex, int lastColumnIndex) {
        ExcelUtil.mergeCells(this.origin, firstRowIndex, firstColumnIndex, lastRowIndex, lastColumnIndex);
    }

}
