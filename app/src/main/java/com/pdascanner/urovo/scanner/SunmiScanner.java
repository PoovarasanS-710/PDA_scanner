package com.pdascanner.urovo.scanner;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Scanner provider for SUNMI devices (L2s, L2K, L2H, etc.).
 *
 * SUNMI PDAs broadcast decoded barcodes via
 * {@code com.sunmi.scanner.ACTION_DATA_CODE_RECEIVED} with the
 * barcode string in the {@code data} extra.
 */
public class SunmiScanner implements ScannerProvider {

    private static final String ACTION_DATA =
            "com.sunmi.scanner.ACTION_DATA_CODE_RECEIVED";
    private static final String EXTRA_BARCODE = "data";

    // Software trigger
    private static final String ACTION_START =
            "com.sunmi.scanner.ACTION_START_SCAN";
    private static final String ACTION_STOP  =
            "com.sunmi.scanner.ACTION_STOP_SCAN";

    private Context appContext;

    @Override
    public String getName() {
        return "SUNMI";
    }

    @Override
    public boolean isAvailable(Context context) {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        return manufacturer.contains("sunmi");
    }

    @Override
    public void open(Context context) {
        appContext = context.getApplicationContext();
    }

    @Override
    public void close() {
        appContext = null;
    }

    @Override
    public void startDecode() {
        if (appContext == null) return;
        try {
            Intent start = new Intent(ACTION_START);
            appContext.sendBroadcast(start);
        } catch (Exception ignored) {}
    }

    @Override
    public void stopDecode() {
        if (appContext == null) return;
        try {
            Intent stop = new Intent(ACTION_STOP);
            appContext.sendBroadcast(stop);
        } catch (Exception ignored) {}
    }

    @Override
    public String getScanAction() {
        return ACTION_DATA;
    }

    @Override
    public String extractBarcode(Intent intent) {
        return intent.getStringExtra(EXTRA_BARCODE);
    }
}
