package com.pdascanner.urovo.scanner;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.lang.reflect.Method;

/**
 * Scanner provider for Urovo devices (CT58, DT50s, i50, RT40, etc.).
 *
 * Uses reflection on {@code android.device.ScanManager} (or legacy
 * {@code device.scanner.ScanManager}) so the APK compiles without
 * the Urovo SDK JAR. At runtime on Urovo firmware the system class
 * is resolved automatically.
 */
public class UrovoScanner implements ScannerProvider {

    private static final String ACTION_DECODE = "android.intent.ACTION_DECODE_DATA";
    private static final String EXTRA_BARCODE = "barcode_string";

    private Object scanManagerInstance;

    @Override
    public String getName() {
        return "Urovo";
    }

    @Override
    public boolean isAvailable(Context context) {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        if (!manufacturer.contains("urovo") && !manufacturer.contains("ubx")) {
            return false;
        }
        // Verify SDK class is present on this firmware
        try {
            Class.forName("android.device.ScanManager");
            return true;
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("device.scanner.ScanManager");
                return true;
            } catch (ClassNotFoundException e2) {
                return false;
            }
        }
    }

    @Override
    public void open(Context context) {
        try {
            Class<?> smClass;
            try {
                smClass = Class.forName("android.device.ScanManager");
            } catch (ClassNotFoundException e) {
                smClass = Class.forName("device.scanner.ScanManager");
            }
            scanManagerInstance = smClass.getDeclaredConstructor().newInstance();
            smClass.getMethod("openScanner").invoke(scanManagerInstance);
            // 0 = Intent/broadcast mode
            smClass.getMethod("switchOutputMode", int.class)
                   .invoke(scanManagerInstance, 0);
        } catch (Exception e) {
            scanManagerInstance = null;
        }
    }

    @Override
    public void close() {
        if (scanManagerInstance == null) return;
        try {
            scanManagerInstance.getClass()
                    .getMethod("closeScanner")
                    .invoke(scanManagerInstance);
        } catch (Exception ignored) {}
        scanManagerInstance = null;
    }

    @Override
    public void startDecode() {
        if (scanManagerInstance == null) return;
        try {
            Method m = scanManagerInstance.getClass().getMethod("startDecode");
            m.invoke(scanManagerInstance);
        } catch (Exception ignored) {}
    }

    @Override
    public void stopDecode() {
        if (scanManagerInstance == null) return;
        try {
            scanManagerInstance.getClass()
                    .getMethod("stopDecode")
                    .invoke(scanManagerInstance);
        } catch (Exception ignored) {}
    }

    @Override
    public String getScanAction() {
        return ACTION_DECODE;
    }

    @Override
    public String extractBarcode(Intent intent) {
        return intent.getStringExtra(EXTRA_BARCODE);
    }
}
