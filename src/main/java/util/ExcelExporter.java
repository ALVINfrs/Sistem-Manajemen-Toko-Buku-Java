package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelExporter {

    private static final NumberFormat FMT_RP = NumberFormat.getInstance(new Locale("id", "ID"));

    public static void exportJTable(JTable table, String title, String periode, File outFile) throws IOException {
        TableModel model = table.getModel();
        int rowCount = model.getRowCount();
        int colCount = model.getColumnCount();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            String sheetName = sanitizeSheetName(title);
            XSSFSheet sheet = workbook.createSheet(sheetName);
            sheet.setDisplayGridlines(true);

            // 1. Palette Colors (Navy Header: RGB 30, 41, 59)
            byte[] navyRgb = new byte[]{(byte) 30, (byte) 41, (byte) 59};
            XSSFColor colorNavy = new XSSFColor(navyRgb, null);

            // 2. Fonts
            XSSFFont fontTitle = workbook.createFont();
            fontTitle.setFontName("Segoe UI");
            fontTitle.setFontHeightInPoints((short) 14);
            fontTitle.setBold(true);

            XSSFFont fontSub = workbook.createFont();
            fontSub.setFontName("Segoe UI");
            fontSub.setFontHeightInPoints((short) 10);
            fontSub.setColor(IndexedColors.GREY_50_PERCENT.getIndex());

            XSSFFont fontHeader = workbook.createFont();
            fontHeader.setFontName("Segoe UI");
            fontHeader.setFontHeightInPoints((short) 11);
            fontHeader.setBold(true);
            fontHeader.setColor(IndexedColors.WHITE.getIndex());

            XSSFFont fontData = workbook.createFont();
            fontData.setFontName("Segoe UI");
            fontData.setFontHeightInPoints((short) 10);

            // 3. Styles
            XSSFCellStyle styleTitle = workbook.createCellStyle();
            styleTitle.setFont(fontTitle);

            XSSFCellStyle styleSub = workbook.createCellStyle();
            styleSub.setFont(fontSub);

            XSSFCellStyle styleHeader = workbook.createCellStyle();
            styleHeader.setFont(fontHeader);
            styleHeader.setFillForegroundColor(colorNavy);
            styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styleHeader.setAlignment(HorizontalAlignment.CENTER);
            styleHeader.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorders(styleHeader);

            XSSFCellStyle styleText = workbook.createCellStyle();
            styleText.setFont(fontData);
            styleText.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorders(styleText);

            XSSFCellStyle styleNumber = workbook.createCellStyle();
            styleNumber.setFont(fontData);
            styleNumber.setAlignment(HorizontalAlignment.RIGHT);
            styleNumber.setVerticalAlignment(VerticalAlignment.CENTER);
            styleNumber.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
            setBorders(styleNumber);

            XSSFCellStyle styleCurrency = workbook.createCellStyle();
            styleCurrency.setFont(fontData);
            styleCurrency.setAlignment(HorizontalAlignment.RIGHT);
            styleCurrency.setVerticalAlignment(VerticalAlignment.CENTER);
            styleCurrency.setDataFormat(workbook.createDataFormat().getFormat("\"Rp \"#,##0"));
            setBorders(styleCurrency);

            // 4. Header Titles (Rows 0, 1, 2)
            XSSFRow rowApp = sheet.createRow(0);
            XSSFCell cellApp = rowApp.createCell(0);
            cellApp.setCellValue("TOKO BUKU ALMIRA");
            cellApp.setCellStyle(styleTitle);

            XSSFRow rowReport = sheet.createRow(1);
            XSSFCell cellReport = rowReport.createCell(0);
            cellReport.setCellValue(title != null ? title.toUpperCase() : "LAPORAN");
            cellReport.setCellStyle(styleTitle);

            XSSFRow rowPeriode = sheet.createRow(2);
            XSSFCell cellPeriode = rowPeriode.createCell(0);
            cellPeriode.setCellValue("Periode: " + (periode != null ? periode : "Semua Data"));
            cellPeriode.setCellStyle(styleSub);

            // Row 3 is blank spacing
            int startRow = 4;

            // 5. Table Headers (Row 4)
            XSSFRow rowHeader = sheet.createRow(startRow);
            rowHeader.setHeightInPoints(24);
            for (int c = 0; c < colCount; c++) {
                XSSFCell cell = rowHeader.createCell(c);
                cell.setCellValue(model.getColumnName(c));
                cell.setCellStyle(styleHeader);
            }

            // 6. Data Rows (Row 5+)
            for (int r = 0; r < rowCount; r++) {
                XSSFRow row = sheet.createRow(startRow + 1 + r);
                row.setHeightInPoints(20);
                for (int c = 0; c < colCount; c++) {
                    Object val = model.getValueAt(r, c);
                    XSSFCell cell = row.createCell(c);

                    if (val == null) {
                        cell.setCellValue("-");
                        cell.setCellStyle(styleText);
                        continue;
                    }

                    String str = val.toString().trim();

                    // Cek jika bernilai Rupiah (cth: "Rp 65.000" atau "Rp. 65.000")
                    if (str.startsWith("Rp") || str.startsWith("RP") || str.startsWith("rp")) {
                        try {
                            String clean = str.replaceAll("[^0-9\\-]", "");
                            if (!clean.isEmpty()) {
                                double dbl = Double.parseDouble(clean);
                                cell.setCellValue(dbl);
                                cell.setCellStyle(styleCurrency);
                                continue;
                            }
                        } catch (Exception ignored) {}
                    }

                    // Cek jika angka bulat murni
                    if (str.matches("^-?\\d+$")) {
                        try {
                            long num = Long.parseLong(str);
                            cell.setCellValue(num);
                            cell.setCellStyle(styleNumber);
                            continue;
                        } catch (Exception ignored) {}
                    }

                    // Cek jika format kuantitas (cth: "15 pcs")
                    if (str.toLowerCase().endsWith(" pcs")) {
                        try {
                            String numStr = str.substring(0, str.length() - 4).trim().replace(".", "");
                            long num = Long.parseLong(numStr);
                            cell.setCellValue(num);
                            cell.setCellStyle(styleNumber);
                            continue;
                        } catch (Exception ignored) {}
                    }

                    // Default sebagai teks
                    cell.setCellValue(str);
                    cell.setCellStyle(styleText);
                }
            }

            // 7. Freeze Panes (Header tetap terlihat saat scroll)
            sheet.createFreezePane(0, startRow + 1);

            // 8. Auto-fit column widths
            for (int c = 0; c < colCount; c++) {
                sheet.autoSizeColumn(c);
                int currentWidth = sheet.getColumnWidth(c);
                // Tambahkan padding agar tidak terpotong
                int paddedWidth = currentWidth + 1200;
                if (paddedWidth < 3500) paddedWidth = 3500;
                if (paddedWidth > 15000) paddedWidth = 15000;
                sheet.setColumnWidth(c, paddedWidth);
            }

            // 9. Write to Output File
            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                workbook.write(fos);
            }
        }
    }

    private static void setBorders(XSSFCellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
    }

    private static String sanitizeSheetName(String name) {
        if (name == null || name.isBlank()) {
            return "Laporan";
        }
        String s = name.replaceAll("[\\\\/*?\\[\\]:]", " ").trim();
        if (s.length() > 30) {
            s = s.substring(0, 30);
        }
        return s.isEmpty() ? "Laporan" : s;
    }
}
