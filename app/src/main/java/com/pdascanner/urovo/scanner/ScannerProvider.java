package com.pdascanner.urovo.scanner;

import android.content.Context;
import android.content.Intent;

/**
 * Abstraction for hardware barcode scanners across PDA brands.
 *
 * Each implementation handles a specific manufacturer's scanner API
 * (broadcast intents, SDK reflection, or service bindings).
 *
 * Lifecycle: detect → open → startDecode/stopDecode → close
 */
public interface ScannerProvider {

    /**
     * Human-readable name of this scanner provider (e.g. "Urovo", "Zebra DataWedge").
     */
    String getName();

    /**
     * Returns true if this provider can work on the current device.
     * Typically checks Build.MANUFACTURER and/or tries to load SDK classes.
     */
    boolean isAvailable(Context context);

    /**
     * Initialise and open the scanner hardware. Called once from onCreate.
     */
    void open(Context context);

    /**
     * Release scanner resources. Called from onDestroy.
     */
    void close();

    /**
     * Trigger a single scan (equivalent to pressing the hardware button).
     */
    void startDecode();

    /**
     * Cancel an in-progress scan.
     */
    void stopDecode();

    /**
     * The broadcast intent action this scanner sends when a barcode is decoded.
     * The Activity will register a BroadcastReceiver for this action.
     */
    String getScanAction();

    /**
     * Extract the barcode string from the received broadcast intent.
     *
     * @param intent the broadcast intent received by the BroadcastReceiver
     * @return the decoded barcode string, or null if extraction failed
     */
    String extractBarcode(Intent intent);
}
