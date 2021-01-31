package org.truenewx.tnxjeex.office.excel;

import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * Excel文档工具类
 *
 * @author jianglei
 * @since JDK 1.8
 */
public class ExcelUtil {

    public static void cloneRows(HSSFSheet sheet, int sourceStartRowIndex, int rowsNum, int targetStartRowIndex) {
        for (int i = 0; i < rowsNum; i++) {
            int sourceRowIndex = sourceStartRowIndex + i;
            HSSFRow sourceRow = sheet.getRow(sourceRowIndex);
            if (sourceRow != null) {
                int targetRowIndex = targetStartRowIndex + i;
                HSSFRow targetRow = sheet.getRow(targetRowIndex);
                if (targetRow == null) {
                    targetRow = sheet.createRow(targetRowIndex);
                }
                cloneRow(sourceRow, targetRow);
            }
        }
    }

    public static void cloneRow(HSSFRow source, HSSFRow target) {
        target.setHeight(source.getHeight());
        HSSFSheet sheet = source.getSheet();
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress address = sheet.getMergedRegion(i);
            if (address.getFirstRow() == source.getRowNum()) {
                CellRangeAddress newAddress = new CellRangeAddress(target.getRowNum(),
                        target.getRowNum() + address.getLastRow() - address.getFirstRow(), address.getFirstColumn(),
                        address.getLastColumn());
                sheet.addMergedRegion(newAddress);
            }
        }
        for (Iterator<Cell> iterator = source.cellIterator(); iterator.hasNext();) {
            HSSFCell sourceCell = (HSSFCell) iterator.next();
            HSSFCell newCell = target.createCell(sourceCell.getColumnIndex());
            cloneCell(sourceCell, newCell);
        }
    }

    public static void cloneCell(HSSFCell source, HSSFCell target) {
        // 一个文档中的个性化样式数量有4000的限制，克隆单元格时直接引用样式，而不是深度克隆一个全新的样式
        target.setCellStyle(source.getCellStyle());
        if (source.getCellComment() != null) {
            target.setCellComment(source.getCellComment());
        }
        // 不同数据类型处理
        CellType type = source.getCellType();
        if (type != CellType.FORMULA) {
            target.setCellType(type);
        }
        switch (type) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(source)) {
                    target.setCellValue(source.getDateCellValue());
                } else {
                    target.setCellValue(source.getNumericCellValue());
                }
                break;
            case STRING:
                target.setCellValue(source.getRichStringCellValue());
                break;
            case BOOLEAN:
                target.setCellValue(source.getBooleanCellValue());
                break;
            case ERROR:
                target.setCellErrorValue(FormulaError.forInt(source.getErrorCellValue()));
                break;
            case FORMULA:
                target.setCellFormula(source.getCellFormula());
                break;
            default:
                break;
        }
    }

    public static void cloneFont(HSSFFont source, HSSFFont target) {
        target.setBold(source.getBold());
        target.setCharSet(source.getCharSet());
        target.setColor(source.getColor());
        target.setFontHeight(source.getFontHeight());
        target.setFontName(source.getFontName());
        target.setItalic(source.getItalic());
        target.setStrikeout(source.getStrikeout());
        target.setTypeOffset(source.getTypeOffset());
        target.setUnderline(source.getUnderline());
    }

    public static void mergeCells(HSSFSheet sheet, int firstRowIndex, int firstColumnIndex, int lastRowIndex,
            int lastColumnIndex) {
        sheet.addMergedRegion(new CellRangeAddress(firstRowIndex, lastRowIndex, firstColumnIndex, lastColumnIndex));
    }

    public static void setBackgroundColor(HSSFCellStyle style, HSSFColorPredefined color) {
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }

    public static void setCellHyperLink(HSSFCell cell, String caption, String url) {
        cell.setCellFormula("HYPERLINK(\"" + url + "\",\"" + caption + "\")");
    }

}
