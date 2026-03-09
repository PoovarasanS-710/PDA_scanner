package com.pdascanner.urovo.scanner;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

/**
 * Scanner provider for Zebra devices (TC52, TC72, MC3300, EC50, etc.)
 * using the DataWedge intent API.
 *
 * DataWedge is Zebra's built-in service that manages the barcode scanner.
 * This provider creates/configures a DataWedge profile at runtime so no
 * manual device setup is required.
 *
 * The profile is configured to output decoded barcodes as broadcast intents
 * with a custom action unique to this app.
 */
public class ZebraScanner implements ScannerProvider {

    /**
     * Custom action for DataWedge to broadcast scanned barcodes.
     * Must match the value configured in the DataWedge profile.
     */
    private static final String SCAN_ACTION =
            "com.pdascanner.urovo.ZEBRA_SCAN";

    private static final String EXTRA_BARCODE =
            "com.symbol.datawedge.data_string";

    // DataWedge API actions and extras
    private static final String DW_API_ACTION =
            "com.symbol.datawedge.api.ACTION";
    private static final String DW_EXTRA_CREATE_PROFILE =
            "com.symbol.datawedge.api.CREATE_PROFILE";
    private static final String DW_EXTRA_SET_CONFIG =
            "com.symbol.datawedge.api.SET_CONFIG";
    private static final String DW_EXTRA_SOFT_SCAN =
            "com.symbol.datawedge.api.SOFT_SCAN_TRIGGER";

    private static final String PROFILE_NAME = "PDAScanner";

    private Context appContext;

    @Override
    public String getName() {
        return "Zebra DataWedge";
    }

    @Override
    public boolean isAvailable(Context context) {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        return manufacturer.contains("zebra")
            || manufacturer.contains("symbol")
            || manufacturer.contains("motorola solutions");
    }

    @Override
    public void open(Context context) {
        appContext = context.getApplicationContext();
        createDataWedgeProfile();
    }

    @Override
    public void close() {
        appContext = null;
    }

    @Override
    public void startDecode() {
        if (appContext == null) return;
        try {
            Intent i = new Intent();
            i.setAction(DW_API_ACTION);
            i.putExtra(DW_EXTRA_SOFT_SCAN, "START_SCANNING");
            appContext.sendBroadcast(i);
        } catch (Exception ignored) {}
    }

    @Override
    public void stopDecode() {
        if (appContext == null) return;
        try {
            Intent i = new Intent();
            i.setAction(DW_API_ACTION);
            i.putExtra(DW_EXTRA_SOFT_SCAN, "STOP_SCANNING");
            appContext.sendBroadcast(i);
        } catch (Exception ignored) {}
    }

    @Override
    public String getScanAction() {
        return SCAN_ACTION;
    }

    @Override
    public String extractBarcode(Intent intent) {
        return intent.getStringExtra(EXTRA_BARCODE);
    }

    // ── DataWedge profile setup ──────────────────────────────────────────────

    private void createDataWedgeProfile() {
        if (appContext == null) return;
        try {
            // Step 1: Create the profile (no-op if it already exists)
            Intent create = new Intent();
            create.setAction(DW_API_ACTION);
            create.putExtra(DW_EXTRA_CREATE_PROFILE, PROFILE_NAME);
            appContext.sendBroadcast(create);

            // Step 2: Configure the profile
            Intent config = new Intent();
            config.setAction(DW_API_ACTION);

            Bundle profileConfig = new Bundle();
            profileConfig.putString("PROFILE_NAME", PROFILE_NAME);
            profileConfig.putString("PROFILE_ENABLED", "true");
            profileConfig.putString("CONFIG_MODE", "UPDATE");

            // Associate with this app's package
            Bundle appConfig = new Bundle();
            appConfig.putString("PACKAGE_NAME", appContext.getPackageName());
            appConfig.putStringArray("ACTIVITY_LIST", new String[]{"*"});
            profileConfig.putParcelableArray("APP_LIST",
                    new Bundle[]{appConfig});

            // Configure intent output plugin
            Bundle intentPlugin = new Bundle();
            intentPlugin.putString("PLUGIN_NAME", "INTENT");
            intentPlugin.putString("RESET_CONFIG", "true");

            Bundle intentProps = new Bundle();
            intentProps.putString("intent_output_enabled", "true");
            intentProps.putString("intent_action", SCAN_ACTION);
            intentProps.putString("intent_delivery", "2"); // 2 = broadcast
            intentPlugin.putBundle("PARAM_LIST", intentProps);

            profileConfig.putBundle("PLUGIN_CONFIG", intentPlugin);

            config.putExtra(DW_EXTRA_SET_CONFIG, profileConfig);
            appContext.sendBroadcast(config);
        } catch (Exception ignored) {}
    }
}
