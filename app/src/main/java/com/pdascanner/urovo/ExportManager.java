package com.pdascanner.urovo;

import android.content.Context;
import android.os.Environment;

import com.pdascanner.urovo.model.ScanItem;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ExportManager — generates .xlsx file from the current ScanSession list.
 *
 * Uses Apache POI XSSFWorkbook on a background thread to avoid blocking the UI.
 * Result delivered on main thread via Callback.
 *
 * File saved to: Downloads/ScanSession_yyyyMMdd_HHmmss.xlsx
 * On API ≤ 28: uses Environment.DIRECTORY_DOWNLOADS (requires WRITE_EXTERNAL_STORAGE).
 * On API 29+: uses app-scoped Downloads dir (no permission needed).
 */
public class ExportManager {

    private static final SimpleDateFormat FILENAME_SDF =
            new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
    private static final SimpleDateFormat CELL_SDF =
            new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault());

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface Callback {
        void onSuccess(String filePath);
        void onError(String error);
    }

    public static void exportToXlsx(Context context, List<ScanItem> items, Callback callback) {
        // Snapshot the list on the calling (main) thread before handing off
        final List<ScanItem> snapshot = java.util.Collections.unmodifiableList(
                new java.util.ArrayList<>(items));

        executor.execute(() -> {
            try {
                String filePath = buildXlsxFile(context, snapshot);
                android.os.Handler mainHandler =
                        new android.os.Handler(context.getMainLooper());
                mainHandler.post(() -> callback.onSuccess(filePath));
            } catch (Exception e) {
                android.os.Handler mainHandler =
                        new android.os.Handler(context.getMainLooper());
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    private static String buildXlsxFile(Context context, List<ScanItem> items) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(context.getString(R.string.sheet_name));

        // ── Header row ────────────────────────────────────────────────────────
        CellStyle headerStyle = createHeaderStyle(workbook);
        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, context.getString(R.string.col_number),    headerStyle);
        createCell(headerRow, 1, context.getString(R.string.col_barcode),   headerStyle);
        createCell(headerRow, 2, context.getString(R.string.col_timestamp), headerStyle);

        // ── Data rows ─────────────────────────────────────────────────────────
        CellStyle dataStyle = createDataStyle(workbook);
        for (int i = 0; i < items.size(); i++) {
            ScanItem item = items.get(i);
            Row row = sheet.createRow(i + 1);
            createCell(row, 0, String.valueOf(item.getSequenceNumber()), dataStyle);
            createCell(row, 1, item.getBarcodeString(),                   dataStyle);
            createCell(row, 2, CELL_SDF.format(item.getTimestamp()),      dataStyle);
        }

        // Auto-size columns
        sheet.setColumnWidth(0, 4 * 256);   // # column — narrow
        sheet.setColumnWidth(1, 30 * 256);  // Barcode — wide
        sheet.setColumnWidth(2, 22 * 256);  // Timestamp

        // ── Write to file ─────────────────────────────────────────────────────
        String filename = context.getString(R.string.export_filename_prefix)
                + FILENAME_SDF.format(new Date()) + ".xlsx";

        String savedPath;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // API 29+: use MediaStore to save to public Downloads folder
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
            values.put(MediaStore.Downloads.MIME_TYPE,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            values.put(MediaStore.Downloads.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS);

            Uri uri = context.getContentResolver().insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) throw new Exception("Failed to create file in Downloads");

            try (OutputStream os = context.getContentResolver().openOutputStream(uri)) {
                if (os == null) throw new Exception("Failed to open output stream");
                workbook.write(os);
            }
            savedPath = Environment.DIRECTORY_DOWNLOADS + "/" + filename;
        } else {
            // API < 29: use public Downloads folder (requires WRITE_EXTERNAL_STORAGE)
            File downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            if (downloadsDir != null && !downloadsDir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                downloadsDir.mkdirs();
            }
            File outputFile = new File(downloadsDir, filename);
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                workbook.write(fos);
            }
            savedPath = outputFile.getAbsolutePath();
        }

        workbook.close();
        return savedPath;
    }

    // ── Style helpers ─────────────────────────────────────────────────────────

    private static CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.index);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.index);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createDataStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
