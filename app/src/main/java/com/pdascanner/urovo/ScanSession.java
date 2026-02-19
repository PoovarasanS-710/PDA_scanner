package com.pdascanner.urovo;

import com.pdascanner.urovo.model.ScanItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * In-memory session data store — singleton scoped to app process lifetime.
 *
 * Design guarantees:
 * - Zero disk I/O: no SharedPreferences, SQLite, or file writes.
 * - Data is automatically cleared when the process dies (force-stop, reboot).
 * - Thread-safe: all public methods are synchronized (BroadcastReceiver runs off main thread).
 *
 * Duplicate rejection rules:
 * 1. Session-wide: same barcode string is accepted only once per session.
 * 2. Debounce: same string arriving within DEBOUNCE_MS is rejected (hardware double-fire).
 */
public class ScanSession {

    private static volatile ScanSession instance;

    private static final long DEBOUNCE_MS = 500L;

    private final List<ScanItem> items       = new ArrayList<>();
    private final Set<String>    seenBarcodes = new HashSet<>();

    private String lastBarcodeString = null;
    private long   lastScanTimeMs    = 0L;
    private int    totalAccepted     = 0; // ever-incrementing sequence counter

    // ── Singleton ────────────────────────────────────────────────────────────

    private ScanSession() {}

    public static ScanSession getInstance() {
        if (instance == null) {
            synchronized (ScanSession.class) {
                if (instance == null) {
                    instance = new ScanSession();
                }
            }
        }
        return instance;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Attempts to add a barcode scan to the session.
     *
     * @param raw raw string from scanner (may be null or empty)
     * @return true if accepted and added; false if rejected
     */
    public synchronized boolean addScan(String raw) {
        if (raw == null || raw.trim().isEmpty()) return false;

        String barcode = raw.trim();
        long now = System.currentTimeMillis();

        // Rule 1 — debounce: same barcode within the debounce window
        if (barcode.equals(lastBarcodeString) && (now - lastScanTimeMs) < DEBOUNCE_MS) {
            return false;
        }

        // Rule 2 — session-wide dedup
        if (seenBarcodes.contains(barcode)) return false;

        // Accept
        seenBarcodes.add(barcode);
        lastBarcodeString = barcode;
        lastScanTimeMs    = now;
        totalAccepted++;

        ScanItem item = new ScanItem(barcode, new Date(now), totalAccepted);
        items.add(0, item); // newest first
        return true;
    }

    /**
     * Returns an unmodifiable view of the current scan list (newest first).
     */
    public synchronized List<ScanItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Returns the number of accepted scans in this session.
     */
    public synchronized int getCount() {
        return items.size();
    }

    /**
     * Clears all session data. Called on logout.
     */
    public synchronized void clearAll() {
        items.clear();
        seenBarcodes.clear();
        lastBarcodeString = null;
        lastScanTimeMs    = 0L;
        totalAccepted     = 0;
    }
}
