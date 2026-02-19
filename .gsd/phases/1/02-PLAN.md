---
phase: 1
plan: 2
wave: 1
depends_on: []
files_modified:
  - app/src/main/java/com/pdascanner/urovo/model/ScanItem.java
  - app/src/main/java/com/pdascanner/urovo/ScanSession.java
autonomous: true
user_setup: []

must_haves:
  truths:
    - "ScanSession is a singleton holding an in-memory ArrayList of ScanItems"
    - "Adding the same barcode string twice returns false (session-wide dedup)"
    - "Adding a barcode within 500ms of the last scan of the same string is rejected (debounce)"
    - "clearAll() empties the list completely"
    - "No SharedPreferences, database, or file I/O is used — memory only"
  artifacts:
    - "app/src/main/java/com/pdascanner/urovo/model/ScanItem.java exists"
    - "app/src/main/java/com/pdascanner/urovo/ScanSession.java exists"
---

# Plan 1.2: ScanSession Singleton + ScanItem Model

<objective>
Create the in-memory data layer: the ScanItem model and ScanSession singleton.

Purpose: DashboardActivity and ExportManager both depend on ScanSession. Building it independently in Wave 1 ensures no file conflicts with Plan 1.1.
Output: ScanItem model + ScanSession singleton with add/dedup/debounce/clear logic. Zero disk I/O.
</objective>

<context>
Load for context:
- .gsd/SPEC.md (REQ-05, REQ-07, REQ-15, REQ-16, REQ-17, REQ-21)
- .gsd/phases/1/RESEARCH.md
</context>

<tasks>

<task type="auto">
  <name>Create ScanItem model class</name>
  <files>app/src/main/java/com/pdascanner/urovo/model/ScanItem.java</files>
  <action>
    Create a simple POJO:
    ```java
    package com.pdascanner.urovo.model;

    import java.util.Date;

    public class ScanItem {
        private final String barcodeString;
        private final Date timestamp;
        private final int sequenceNumber;  // 1-based, for display

        public ScanItem(String barcodeString, Date timestamp, int sequenceNumber) {
            this.barcodeString = barcodeString;
            this.timestamp = timestamp;
            this.sequenceNumber = sequenceNumber;
        }

        public String getBarcodeString() { return barcodeString; }
        public Date getTimestamp() { return timestamp; }
        public int getSequenceNumber() { return sequenceNumber; }
    }
    ```

    AVOID: Do NOT use Parcelable or Serializable — ScanItem never crosses process boundaries.
    AVOID: Do NOT add a database ID or Room annotation.
  </action>
  <verify>File exists at the specified path. No compile errors (checked by next plan's build).</verify>
  <done>ScanItem.java created with 3 final fields and 3 getters.</done>
</task>

<task type="auto">
  <name>Create ScanSession singleton with add/dedup/debounce/clear logic</name>
  <files>app/src/main/java/com/pdascanner/urovo/ScanSession.java</files>
  <action>
    ```java
    package com.pdascanner.urovo;

    import com.pdascanner.urovo.model.ScanItem;
    import java.util.ArrayList;
    import java.util.Collections;
    import java.util.Date;
    import java.util.HashSet;
    import java.util.List;
    import java.util.Set;

    public class ScanSession {
        private static ScanSession instance;
        private final List<ScanItem> items = new ArrayList<>();
        private final Set<String> seenBarcodes = new HashSet<>();  // session-wide dedup
        private String lastBarcodeString = null;
        private long lastScanTimeMs = 0;
        private static final long DEBOUNCE_MS = 500;

        private ScanSession() {}

        public static synchronized ScanSession getInstance() {
            if (instance == null) instance = new ScanSession();
            return instance;
        }

        /**
         * Attempt to add a scan.
         * Returns true if added, false if rejected (duplicate or debounce).
         */
        public synchronized boolean addScan(String barcodeString) {
            if (barcodeString == null || barcodeString.trim().isEmpty()) return false;
            barcodeString = barcodeString.trim();

            long now = System.currentTimeMillis();

            // Debounce: same string within DEBOUNCE_MS window
            if (barcodeString.equals(lastBarcodeString) && (now - lastScanTimeMs) < DEBOUNCE_MS) {
                return false;
            }

            // Session-wide duplicate check
            if (seenBarcodes.contains(barcodeString)) return false;

            // Accept
            seenBarcodes.add(barcodeString);
            lastBarcodeString = barcodeString;
            lastScanTimeMs = now;
            ScanItem item = new ScanItem(barcodeString, new Date(now), items.size() + 1);
            items.add(0, item);  // newest first
            return true;
        }

        public synchronized List<ScanItem> getItems() {
            return Collections.unmodifiableList(items);
        }

        public synchronized int getCount() {
            return items.size();
        }

        public synchronized void clearAll() {
            items.clear();
            seenBarcodes.clear();
            lastBarcodeString = null;
            lastScanTimeMs = 0;
        }
    }
    ```

    AVOID: Do NOT write any scan data to SharedPreferences, SQLite, or files — all state must be in-memory only.
    AVOID: Do NOT use a non-synchronized singleton pattern — scanner events arrive from broadcast callbacks off the main thread.
  </action>
  <verify>
    File exists. No compile errors when ./gradlew assembleDebug runs (after Plan 1.1 scaffold exists).
    Manual logic trace:
    - addScan("ABC") → true (item count = 1)
    - addScan("ABC") immediately → false (session dedup)
    - addScan("XYZ") → true (item count = 2)
    - clearAll() → getCount() returns 0
  </verify>
  <done>
    - ScanSession.getInstance() returns same instance each call.
    - Duplicate barcodes return false from addScan().
    - Same barcode within 500ms returns false (debounce).
    - clearAll() resets items, seenBarcodes, and debounce state.
    - Zero file I/O anywhere in the class.
  </done>
</task>

</tasks>

<verification>
After all tasks:
- [ ] ScanItem.java and ScanSession.java created at correct package paths
- [ ] No disk I/O in ScanSession (grep for "SharedPreferences\|SQLite\|FileOutputStream" returns nothing in ScanSession.java)
- [ ] addScan() returns false for: null, empty string, duplicate, debounce window
- [ ] clearAll() resets all state
</verification>

<success_criteria>
- [ ] All tasks verified per <verify> steps
- [ ] Must-haves confirmed above
</success_criteria>
