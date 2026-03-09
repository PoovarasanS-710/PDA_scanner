package com.pdascanner.urovo.scanner;

import android.content.Context;
import android.os.Build;
import android.util.Log;

/**
 * Factory that auto-detects the device manufacturer at runtime and returns
 * the correct {@link ScannerProvider} implementation.
 *
 * Detection order:
 * 1. Match {@code Build.MANUFACTURER} against known brands.
 * 2. Verify via {@code provider.isAvailable(context)}.
 * 3. If no match, return {@code null} (non-PDA / unsupported device).
 */
public final class ScannerFactory {

    private static final String TAG = "ScannerFactory";

    private ScannerFactory() {} // no instances

    /**
     * All known scanner providers, ordered by popularity / likelihood.
     * Add new device brands here.
     */
    private static ScannerProvider[] allProviders() {
        return new ScannerProvider[]{
                new UrovoScanner(),
                new HoneywellScanner(),
                new ZebraScanner(),
                new NewlandScanner(),
                new SunmiScanner(),
        };
    }

    /**
     * Detect and return the appropriate scanner provider for this device.
     *
     * @param context application or activity context
     * @return a ready-to-use ScannerProvider, or null if unsupported device
     */
    public static ScannerProvider detect(Context context) {
        String manufacturer = Build.MANUFACTURER;
        Log.d(TAG, "Device manufacturer: " + manufacturer
                + ", model: " + Build.MODEL);

        for (ScannerProvider provider : allProviders()) {
            if (provider.isAvailable(context)) {
                Log.i(TAG, "Detected scanner: " + provider.getName());
                return provider;
            }
        }

        Log.w(TAG, "No compatible scanner found for manufacturer: "
                + manufacturer);
        return null;
    }
}
