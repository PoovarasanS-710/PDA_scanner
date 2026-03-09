package com.pdascanner.urovo.scanner;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Scanner provider for Newland devices (MT90, NLS-MT65, NLS-N7, etc.).
 *
 * Newland PDA firmware broadcasts scan results on the action
 * {@code nlscan.action.SCANNER_RESULT} with barcode data in the
 * {@code SCAN_BARCODE1} extra. Software trigger uses
 * {@code nlscan.action.SCANNER_TRIG}.
 */
public class NewlandScanner implements ScannerProvider {

    private static final String ACTION_RESULT   = "nlscan.action.SCANNER_RESULT";
    private static final String ACTION_TRIGGER   = "nlscan.action.SCANNER_TRIG";
    private static final String ACTION_STOP      = "nlscan.action.STOP_SCAN";
    private static final String EXTRA_BARCODE    = "SCAN_BARCODE1";
    private static final String EXTRA_SCAN_STATE = "SCAN_STATE";

    private Context appContext;

    @Override
    public String getName() {
        return "Newland";
    }

    @Override
    public boolean isAvailable(Context context) {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        return manufacturer.contains("newland") || manufacturer.contains("nls");
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
            Intent trigger = new Intent(ACTION_TRIGGER);
            appContext.sendBroadcast(trigger);
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
        return ACTION_RESULT;
    }

    @Override
    public String extractBarcode(Intent intent) {
        // Only accept successful scans
        String state = intent.getStringExtra(EXTRA_SCAN_STATE);
        if (state != null && !"ok".equalsIgnoreCase(state)) {
            return null;
        }
        return intent.getStringExtra(EXTRA_BARCODE);
    }
}
