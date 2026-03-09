package com.pdascanner.urovo.scanner;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Scanner provider for Honeywell devices (CT40, CT60, EDA51, EDA52, etc.).
 *
 * Honeywell PDAs broadcast scan results via the built-in Scan Wedge / Data
 * Collection intent. The hardware trigger fires the scan automatically;
 * software trigger is done by broadcasting the claim/release intents.
 *
 * Requires the Honeywell "Data Intent" option to be enabled in device
 * Scanner Settings (usually enabled by default on industrial PDAs).
 */
public class HoneywellScanner implements ScannerProvider {

    private static final String ACTION_BARCODE_READ =
            "com.honeywell.intent.action.BARCODE_READ_SUCCESS";
    private static final String EXTRA_BARCODE = "data";

    // Software trigger intents
    private static final String ACTION_CLAIM  =
            "com.honeywell.intent.action.CLAIM_SCANNER";
    private static final String ACTION_RELEASE =
            "com.honeywell.intent.action.RELEASE_SCANNER";
    private static final String ACTION_TRIGGER =
            "com.honeywell.intent.action.SCAN";

    private Context appContext;

    @Override
    public String getName() {
        return "Honeywell";
    }

    @Override
    public boolean isAvailable(Context context) {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        return manufacturer.contains("honeywell") || manufacturer.contains("hon");
    }

    @Override
    public void open(Context context) {
        appContext = context.getApplicationContext();
        // Claim the scanner so our app receives broadcasts
        try {
            Intent claim = new Intent(ACTION_CLAIM);
            claim.setPackage("com.honeywell.decode.DecodeService");
            appContext.sendBroadcast(claim);
        } catch (Exception ignored) {}
    }

    @Override
    public void close() {
        if (appContext == null) return;
        try {
            Intent release = new Intent(ACTION_RELEASE);
            release.setPackage("com.honeywell.decode.DecodeService");
            appContext.sendBroadcast(release);
        } catch (Exception ignored) {}
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
        // Honeywell stops automatically after decode; no explicit stop needed
    }

    @Override
    public String getScanAction() {
        return ACTION_BARCODE_READ;
    }

    @Override
    public String extractBarcode(Intent intent) {
        return intent.getStringExtra(EXTRA_BARCODE);
    }
}
