# RESEARCH.md — Phase 1: Foundation

> **Phase**: 1 — Auth, Scanner & Dashboard
> **Date**: 2026-02-19
> **Discovery Level**: 2 (Standard — new SDK integration + architecture decisions)

---

## 1. Urovo DT50s Scanner SDK Integration

### Integration Method: ScanManager + Broadcast Intent

The Urovo DT50s ships with a proprietary `ScanManager` class delivered as a system API (no JAR needed for broadcast mode — it's part of the device system image).

**Recommended approach: Broadcast Intent (Option A)**

Reason: The DT50s scanner broadcasts `android.intent.ACTION_DECODE_DATA` after every successful decode. This is the most documented, firmware-stable approach. Does NOT require the SDK `.jar` at compile time — the app just registers a `BroadcastReceiver`.

**Integration steps:**
```java
// 1. Open scanner on Activity start
ScanManager scanManager = new ScanManager();
scanManager.openScanner();
scanManager.switchOutputMode(1); // 1 = Intent mode

// 2. Register receiver
IntentFilter filter = new IntentFilter();
filter.addAction("android.intent.ACTION_DECODE_DATA");
registerReceiver(scanReceiver, filter);

// 3. Extract data from broadcast
String barcodeData = intent.getStringExtra("barcode_string");   // BARCODE_STRING_TAG
byte[] barcodeBytes = intent.getByteArrayExtra("decode_data");  // DECODE_DATA_TAG
int barcodeLen = intent.getIntExtra("barcode_length", 0);       // BARCODE_LENGTH_TAG

// 4. Trigger scan programmatically
scanManager.startDecode();  // or hardware trigger button

// 5. Cleanup
scanManager.stopDecode();
unregisterReceiver(scanReceiver);
scanManager.closeScanner();
```

**Key intent extra keys:**
| Key | Type | Content |
|-----|------|---------|
| `barcode_string` | String | Human-readable decoded value ✅ |
| `decode_data` | byte[] | Raw bytes |
| `barcode_length` | int | Length of decoded data |
| `barcode_type` | byte | Symbology type |

**Lifecycle binding:**
- `openScanner()` / `registerReceiver()` → `onResume()`
- `stopDecode()` / `unregisterReceiver()` / `closeScanner()` → `onPause()`

**Debounce strategy:** Track `lastScanTimestamp`; reject any duplicate barcode arriving within 500 ms window to handle hardware double-fire events.

---

## 2. Android Project Structure

**Language decision: Java** (broader Urovo SDK sample availability; industrial PDA codebase convention)

**Target API:**
- `minSdk 26` (Android 8.0 — safe lower bound for DT50s units)
- `targetSdk 29` (Android 10 — DT50s default OS)
- `compileSdk 34`

**Package**: `com.pdascanner.urovo`

**Key classes:**
| Class | Role |
|-------|------|
| `LoginActivity` | Dummy auth screen |
| `DashboardActivity` | Main scan list + buttons |
| `ScanItem` | Data model: `{id, barcodeString, timestamp}` |
| `ScanSession` | Singleton — in-memory list, duplicate/debounce logic |
| `ScanBroadcastReceiver` | Inner class receiving `ACTION_DECODE_DATA` |

---

## 3. Excel Export — Apache POI

**Library**: Apache POI `poi-ooxml` — standard for `.xlsx` on Android.

**Gradle dependency** (app-level `build.gradle`):
```groovy
implementation('org.apache.poi:poi-ooxml:5.2.3') {
    // Exclude stax duplicates that conflict with Android
    exclude group: 'org.apache.xmlbeans', module: 'xmlbeans'
}
implementation 'org.apache.xmlbeans:xmlbeans:5.1.1'
implementation 'com.fasterxml.jackson.core:jackson-core:2.14.2'
```

**Packaging fix** (required in `build.gradle`):
```groovy
android {
    packagingOptions {
        exclude 'META-INF/DEPENDENCIES'
        exclude 'META-INF/LICENSE'
        exclude 'META-INF/NOTICE'
        exclude 'META-INF/LICENSE.txt'
    }
}
```

**File save location**: `Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)` — user-accessible. Requires `WRITE_EXTERNAL_STORAGE` permission for API < 29; on API 29+ use `MediaStore` or app-scoped storage.

**Export columns**: `#` | `Barcode` | `Timestamp`

---

## 4. Decisions Made

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Language | Java | Urovo SDK samples are Java; industrial PDA convention |
| Scanner integration | Broadcast Intent (`ACTION_DECODE_DATA`) | No SDK JAR required at compile time; firmware-stable |
| Output mode | Intent mode (`switchOutputMode(1)`) | Lets app intercept data; prevents it going to focused text box |
| Duplicate rejection | Session-wide dedup (hash set) + 500ms debounce | Both protects against re-scans and hardware double-fire |
| Scan list actions | Read-only (no delete) | PRD specifies no editing; keeps UI minimal |
| Logout | Confirm dialog before clearing | Prevents accidental data loss mid-shift |
| Storage | In-memory `ArrayList<ScanItem>` in singleton | Zero persistence; purged on process death |
| Excel library | Apache POI `poi-ooxml:5.2.3` | Proven, widely used on Android for xlsx |
| Export location | Downloads folder (API 29 compatible) | User-accessible without root or file manager |
