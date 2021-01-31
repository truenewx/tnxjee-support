package org.truenewx.tnxjeex.office.excel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Excel文档
 *
 * @author jianglei
 */
public class ExcelDoc {

    private HSSFWorkbook origin;
    private Map<String, HSSFCellStyle> styles = new HashMap<>();
    private Map<String, HSSFFont> fonts = new HashMap<>();

    public ExcelDoc() {
        this.origin = new HSSFWorkbook();
    }

    public ExcelDoc(InputStream in) throws IOException {
        this.origin = new HSSFWorkbook(in);
    }

    public HSSFWorkbook getOrigin() {
        return this.origin;
    }

    public ExcelSheet cloneSheet(int sourceSheetIndex, String sheetName) {
        HSSFSheet sheet = this.origin.cloneSheet(sourceSheetIndex);
        int newSheetIndex = this.origin.getSheetIndex(sheet);
        this.origin.setSheetName(newSheetIndex, sheetName);
        return new ExcelSheet(this, sheet);
    }

    public ExcelSheet getSheetAt(int sheetIndex) {
        HSSFSheet sheet = this.origin.getSheetAt(sheetIndex);
        return sheet == null ? null : new ExcelSheet(this, sheet);
    }

    public void setActiveSheet(int index) {
        this.origin.setActiveSheet(index);
    }

    public void removeSheetAt(int index) {
        this.origin.removeSheetAt(index);
    }

    public void close() throws IOException {
        this.origin.close();
    }

    public void write() throws IOException {
        this.origin.write();
    }

    public void write(OutputStream stream) throws IOException {
        this.origin.write(stream);
    }

    public HSSFCellStyle createCellStyle(String name) {
        HSSFCellStyle style = this.origin.createCellStyle();
        if (name != null) {
            setCellStyleName(style, name);
        }
        return style;
    }

    public HSSFCellStyle createCellStyle(String name, HSSFCellStyle baseStyle, Consumer<HSSFCellStyle> consumer) {
        HSSFCellStyle style = createCellStyle(name);
        if (baseStyle != null) {
            style.cloneStyleFrom(baseStyle);
        }
        consumer.accept(style);
        return style;
    }

    public void setCellStyleName(HSSFCellStyle style, String name) {
        this.styles.put(name, style);
    }

    public HSSFCellStyle getCellStyle(String name) {
        return this.styles.get(name);
    }

    public HSSFCellStyle getCellStyle(int sheetIndex, int rowIndex, int columnIndex) {
        HSSFSheet sheet = this.origin.getSheetAt(sheetIndex);
        if (sheet != null) {
            HSSFRow row = sheet.getRow(rowIndex);
            if (row != null) {
                HSSFCell cell = row.getCell(columnIndex);
                if (cell != null) {
                    return cell.getCellStyle();
                }
            }
        }
        return null;
    }

    public HSSFFont createFont(String name) {
        HSSFFont font = this.origin.createFont();
        if (name != null) {
            setFontName(font, name);
        }
        return font;
    }

    public HSSFFont createFont(String name, HSSFFont baseFont, Consumer<HSSFFont> consumer) {
        HSSFFont font = createFont(name);
        if (baseFont != null) {
            ExcelUtil.cloneFont(baseFont, font);
        }
        consumer.accept(font);
        return font;
    }

    public void setFontName(HSSFFont font, String name) {
        this.fonts.put(name, font);
    }

    public HSSFFont getFont(String name) {
        return this.fonts.get(name);
    }

    public HSSFFont getFont(int sheetIndex, int rowIndex, int columnIndex) {
        HSSFCellStyle style = getCellStyle(sheetIndex, rowIndex, columnIndex);
        if (style != null) {
            return style.getFont(this.origin);
        }
        return null;
    }

}
