---
phase: 1
plan: 3
wave: 2
depends_on: ["01", "02"]
files_modified:
  - app/src/main/java/com/pdascanner/urovo/DashboardActivity.java
  - app/src/main/res/layout/activity_dashboard.xml
  - app/src/main/res/layout/item_scan.xml
  - app/src/main/java/com/pdascanner/urovo/adapter/ScanAdapter.java
autonomous: true
user_setup:
  - item: "Physical Urovo DT50s device required for scanner hardware test"
    why: "The ScanManager and ACTION_DECODE_DATA broadcast are system APIs only present on Urovo hardware — emulator cannot simulate them."
    action: "Connect DT50s via USB with Android Debug Bridge (adb) enabled."

must_haves:
  truths:
    - "DashboardActivity shows a scrollable RecyclerView of all ScanItems, newest first"
    - "Tapping SCAN button calls scanManager.startDecode() — hardware scanner activates on device"
    - "When ACTION_DECODE_DATA broadcast arrives, barcode is passed to ScanSession.addScan()"
    - "If addScan() returns true, RecyclerView updates instantly (notifyItemInserted(0))"
    - "If addScan() returns false (duplicate/debounce), a Toast 'Duplicate scan ignored' appears"
    - "Each list item shows: sequence number, barcode string, formatted timestamp (HH:mm:ss dd/MM/yyyy)"
    - "Scanner hardware error triggers an AlertDialog with the error message"
    - "Scanner is opened in onResume() and closed in onPause() — no scanner leak"
  artifacts:
    - "app/src/main/java/com/pdascanner/urovo/DashboardActivity.java exists"
    - "app/src/main/java/com/pdascanner/urovo/adapter/ScanAdapter.java exists"
    - "app/src/main/res/layout/item_scan.xml exists"
---

# Plan 1.3: DashboardActivity + Urovo Scanner Integration

<objective>
Build the main dashboard screen with live scanner integration and real-time list updates.

Purpose: This is the core operational loop — scan → list → confirm. Delivers REQ-04 through REQ-11.
Output: DashboardActivity with RecyclerView, ScanAdapter, SCAN button wired to Urovo ScanManager via broadcast receiver.
</objective>

<context>
Load for context:
- .gsd/SPEC.md (REQ-04 to REQ-11)
- .gsd/phases/1/RESEARCH.md (Urovo SDK integration section)
- app/src/main/java/com/pdascanner/urovo/ScanSession.java
- app/src/main/java/com/pdascanner/urovo/model/ScanItem.java
</context>

<tasks>

<task type="auto">
  <name>Create ScanAdapter (RecyclerView) and item_scan.xml layout</name>
  <files>
    app/src/main/java/com/pdascanner/urovo/adapter/ScanAdapter.java,
    app/src/main/res/layout/item_scan.xml
  </files>
  <action>
    item_scan.xml:
    - Root: CardView, margin 4dp, elevation 2dp, background = colorSurface (#1A1A2E)
    - Row layout: sequence number (TextView, 40dp wide, amber, bold) | barcode string (TextView, flex, white, 16sp) | timestamp (TextView, wrap_content, gray #B0BEC5, 12sp)
    - Min height: 56dp (glove-friendly touch area)

    ScanAdapter.java:
    ```java
    package com.pdascanner.urovo.adapter;

    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.TextView;
    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.RecyclerView;
    import com.pdascanner.urovo.R;
    import com.pdascanner.urovo.model.ScanItem;
    import java.text.SimpleDateFormat;
    import java.util.List;
    import java.util.Locale;

    public class ScanAdapter extends RecyclerView.Adapter<ScanAdapter.ViewHolder> {
        private final List<ScanItem> items;
        private static final SimpleDateFormat SDF =
            new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault());

        public ScanAdapter(List<ScanItem> items) { this.items = items; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scan, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int position) {
            ScanItem item = items.get(position);
            h.seqNumber.setText(String.valueOf(item.getSequenceNumber()));
            h.barcodeText.setText(item.getBarcodeString());
            h.timestampText.setText(SDF.format(item.getTimestamp()));
        }

        @Override public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView seqNumber, barcodeText, timestampText;
            ViewHolder(View v) {
                super(v);
                seqNumber = v.findViewById(R.id.textSeqNumber);
                barcodeText = v.findViewById(R.id.textBarcode);
                timestampText = v.findViewById(R.id.textTimestamp);
            }
        }
    }
    ```

    AVOID: Do NOT copy the ScanItem list — pass the live list reference from ScanSession so notifyItemInserted() reflects reality without a full rebind.
  </action>
  <verify>./gradlew assembleDebug — BUILD SUCCESSFUL. item_scan.xml inflates without error.</verify>
  <done>ScanAdapter.java and item_scan.xml created. Adapter displays sequence number, barcode, and formatted timestamp per row.</done>
</task>

<task type="auto">
  <name>Build DashboardActivity with Urovo ScanManager + BroadcastReceiver integration</name>
  <files>
    app/src/main/java/com/pdascanner/urovo/DashboardActivity.java,
    app/src/main/res/layout/activity_dashboard.xml
  </files>
  <action>
    activity_dashboard.xml:
    - Root: ConstraintLayout, background = colorBackground
    - Top bar: "PDA Scanner" title (left) + scan count TextView "0 scans" (right), amber text
    - RecyclerView (id=recyclerScans): fills remaining space, vertically scrollable
    - Bottom button row (horizontal LinearLayout, 8dp padding):
      - SCAN button (weight=2, height=56dp, amber background, dark text, bold "SCAN")
      - EXPORT button (weight=1, height=56dp, gray/disabled-looking initially)
      - LOGOUT button (weight=1, height=56dp, red tint #FF4444)
    - All buttons ≥ 48dp touch targets.

    DashboardActivity.java — full implementation:
    ```java
    package com.pdascanner.urovo;

    // Urovo ScanManager — system API, no import needed if using reflection or broadcast-only approach
    // Use broadcast-only: register receiver for ACTION_DECODE_DATA
    import android.content.BroadcastReceiver;
    import android.content.Context;
    import android.content.Intent;
    import android.content.IntentFilter;
    import android.os.Bundle;
    import android.widget.Button;
    import android.widget.TextView;
    import android.widget.Toast;
    import androidx.appcompat.app.AlertDialog;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;
    import com.pdascanner.urovo.adapter.ScanAdapter;

    public class DashboardActivity extends AppCompatActivity {

        private ScanSession scanSession;
        private ScanAdapter adapter;
        private TextView scanCountText;
        private Button btnExport;

        private device.scanner.ScanManager scanManager;  // Urovo system class
        // NOTE: If ScanManager class is not available at compile time (no SDK jar),
        // use reflection: Class.forName("device.scanner.ScanManager")
        // For broadcast-only mode, ScanManager is only needed to open/close scanner

        private final BroadcastReceiver scanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String barcodeString = intent.getStringExtra("barcode_string");
                if (barcodeString == null || barcodeString.trim().isEmpty()) {
                    Toast.makeText(DashboardActivity.this,
                        "Invalid scan — no data received", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean added = scanSession.addScan(barcodeString);
                if (added) {
                    adapter.notifyItemInserted(0);
                    recyclerView.scrollToPosition(0);
                    updateCountAndExportState();
                } else {
                    Toast.makeText(DashboardActivity.this,
                        "Duplicate scan ignored", Toast.LENGTH_SHORT).show();
                }
            }
        };

        private RecyclerView recyclerView;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_dashboard);

            scanSession = ScanSession.getInstance();
            scanCountText = findViewById(R.id.textScanCount);
            btnExport = findViewById(R.id.btnExport);
            Button btnScan = findViewById(R.id.btnScan);
            Button btnLogout = findViewById(R.id.btnLogout);

            recyclerView = findViewById(R.id.recyclerScans);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new ScanAdapter(scanSession.getItems());
            recyclerView.setAdapter(adapter);

            btnScan.setOnClickListener(v -> triggerScan());
            btnExport.setOnClickListener(v -> {
                // ExportManager called in Plan 2.1
                Toast.makeText(this, "Export coming in Phase 2", Toast.LENGTH_SHORT).show();
            });
            btnLogout.setOnClickListener(v -> confirmLogout());

            updateCountAndExportState();

            // Initialize Urovo scanner
            try {
                scanManager = new device.scanner.ScanManager();
                scanManager.openScanner();
                scanManager.switchOutputMode(1); // Intent mode
            } catch (Exception e) {
                showScannerError("Scanner initialization failed: " + e.getMessage());
            }
        }

        @Override
        protected void onResume() {
            super.onResume();
            IntentFilter filter = new IntentFilter("android.intent.ACTION_DECODE_DATA");
            registerReceiver(scanReceiver, filter);
        }

        @Override
        protected void onPause() {
            super.onPause();
            try { unregisterReceiver(scanReceiver); } catch (Exception ignored) {}
            try { if (scanManager != null) scanManager.stopDecode(); } catch (Exception ignored) {}
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            try { if (scanManager != null) scanManager.closeScanner(); } catch (Exception ignored) {}
        }

        private void triggerScan() {
            try {
                scanManager.startDecode();
            } catch (Exception e) {
                showScannerError("Failed to activate scanner: " + e.getMessage());
            }
        }

        private void updateCountAndExportState() {
            int count = scanSession.getCount();
            scanCountText.setText(count + (count == 1 ? " scan" : " scans"));
            btnExport.setEnabled(count > 0);
            btnExport.setAlpha(count > 0 ? 1.0f : 0.4f);
        }

        private void confirmLogout() {
            new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("You have " + scanSession.getCount() +
                    " scan(s). Logging out will clear all data. Continue?")
                .setPositiveButton("Logout", (d, w) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
        }

        private void performLogout() {
            scanSession.clearAll();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        private void showScannerError(String message) {
            new AlertDialog.Builder(this)
                .setTitle("Scanner Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
        }
    }
    ```

    IMPORTANT — ScanManager compile-time resolution:
    If `device.scanner.ScanManager` is NOT available as a compile-time dependency (no SDK jar provided),
    replace direct instantiation with reflection:
    ```java
    Class<?> smClass = Class.forName("device.scanner.ScanManager");
    Object scanManager = smClass.getDeclaredConstructor().newInstance();
    smClass.getMethod("openScanner").invoke(scanManager);
    smClass.getMethod("switchOutputMode", int.class).invoke(scanManager, 1);
    // Store as Object, invoke methods via reflection throughout
    ```
    This approach compiles on any machine but only works at runtime on Urovo hardware.

    AVOID: Do NOT hardcode specific barcode symbologies — accept all types that ACTION_DECODE_DATA delivers.
    AVOID: Do NOT call registerReceiver() in onCreate() — use onResume()/onPause() lifecycle pair to prevent double-registration.
  </action>
  <verify>
    1. ./gradlew assembleDebug — BUILD SUCCESSFUL
    2. Install on Urovo DT50s: adb install app/build/outputs/apk/debug/app-debug.apk
    3. Login → Dashboard appears with 0 scans count.
    4. Tap SCAN → hardware scanner LED/beeper activates.
    5. Scan any barcode → item appears at top of list with barcode text and timestamp.
    6. EXPORT button is enabled (count > 0). EXPORT button disabled when list is empty.
    7. Scan same barcode again → Toast "Duplicate scan ignored" appears, count unchanged.
    8. Kill and restart app → list is EMPTY (session data not persisted).
  </verify>
  <done>
    - Dashboard shows live scan list, newest first.
    - SCAN button activates Urovo hardware scanner.
    - Valid scans appear in list within 500ms.
    - Duplicate scans show Toast, are not added.
    - Scanner lifecycle managed correctly (no leak on pause/destroy).
    - EXPORT disabled when list empty.
  </done>
</task>

</tasks>

<verification>
After all tasks:
- [ ] ./gradlew assembleDebug succeeds
- [ ] Dashboard shows scan list (RecyclerView with ScanAdapter)
- [ ] SCAN button triggers Urovo hardware scanner (verified on physical device)
- [ ] Successful scan adds item at top of list within 500ms
- [ ] Duplicate scan shows Toast, not added
- [ ] EXPORT button disabled when 0 scans; enabled when ≥ 1
- [ ] App killed and relaunched → list is empty (REQ-17)
</verification>

<success_criteria>
- [ ] All tasks verified per <verify> steps
- [ ] Must-haves confirmed above
- [ ] checkpoint:human-verify on physical Urovo DT50s hardware
</success_criteria>
