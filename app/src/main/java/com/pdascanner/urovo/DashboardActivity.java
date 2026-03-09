package com.pdascanner.urovo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pdascanner.urovo.adapter.ScanAdapter;
import com.pdascanner.urovo.scanner.ScannerFactory;
import com.pdascanner.urovo.scanner.ScannerProvider;

/**
 * DashboardActivity — Main scan session screen.
 *
 * Scanner integration uses a pluggable {@link ScannerProvider} abstraction.
 * At launch, {@link ScannerFactory#detect(Context)} auto-detects the device
 * brand and returns the correct provider (Urovo, Honeywell, Zebra, Newland,
 * SUNMI, etc.). This lets the same APK work across all supported PDA brands.
 */
public class DashboardActivity extends AppCompatActivity {

    private ScanSession  scanSession;
    private ScanAdapter  adapter;
    private TextView     textScanCount;
    private TextView     textEmpty;
    private Button       btnExport;
    private RecyclerView recyclerScans;

    /** Detected scanner — null on unsupported devices (emulators, phones). */
    private ScannerProvider scanner;

    // ── BroadcastReceiver ─────────────────────────────────────────────────────

    private final BroadcastReceiver scanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (scanner == null) return;

            String barcode = scanner.extractBarcode(intent);

            if (barcode == null || barcode.trim().isEmpty()) {
                Toast.makeText(DashboardActivity.this,
                        getString(R.string.msg_invalid_scan), Toast.LENGTH_SHORT).show();
                return;
            }

            boolean added = scanSession.addScan(barcode);
            if (added) {
                adapter.notifyItemInserted(0);
                recyclerScans.scrollToPosition(0);
                updateUiState();
            } else {
                Toast.makeText(DashboardActivity.this,
                        getString(R.string.msg_duplicate_scan), Toast.LENGTH_SHORT).show();
            }
        }
    };

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initViews();
        initScanner();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (scanner != null) {
            IntentFilter filter = new IntentFilter(scanner.getScanAction());
            registerReceiver(scanReceiver, filter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        safeUnregisterReceiver();
        if (scanner != null) {
            scanner.stopDecode();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scanner != null) {
            scanner.close();
            scanner = null;
        }
    }

    // ── Initialisation ────────────────────────────────────────────────────────

    private void initViews() {
        scanSession    = ScanSession.getInstance();
        textScanCount  = findViewById(R.id.textScanCount);
        textEmpty      = findViewById(R.id.textEmpty);
        btnExport      = findViewById(R.id.btnExport);
        Button btnScan    = findViewById(R.id.btnScan);
        Button btnLogout  = findViewById(R.id.btnLogout);
        recyclerScans  = findViewById(R.id.recyclerScans);

        recyclerScans.setLayoutManager(new LinearLayoutManager(this));

        // Pass live list reference — adapter stays in sync via notifyItemInserted
        adapter = new ScanAdapter(scanSession.getItems());
        recyclerScans.setAdapter(adapter);

        btnScan.setOnClickListener(v -> triggerScan());
        btnExport.setOnClickListener(v -> exportToExcel());
        btnLogout.setOnClickListener(v -> confirmLogout());

        updateUiState();
    }

    private void initScanner() {
        scanner = ScannerFactory.detect(this);
        if (scanner != null) {
            scanner.open(this);
            Toast.makeText(this,
                    "Scanner: " + scanner.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    // ── Scanner control ────────────────────────────────────────────────────────

    private void triggerScan() {
        if (scanner == null) {
            showScannerError("Scanner not available on this device.\n\n"
                    + "Supported: Urovo, Honeywell, Zebra, Newland, SUNMI.");
            return;
        }
        scanner.startDecode();
    }

    private void safeUnregisterReceiver() {
        try {
            unregisterReceiver(scanReceiver);
        } catch (IllegalArgumentException ignored) {
            // Already unregistered
        }
    }

    // ── UI helpers ─────────────────────────────────────────────────────────────

    private void updateUiState() {
        int count = scanSession.getCount();
        textScanCount.setText(count + (count == 1 ? " scan" : " scans"));

        boolean hasScans = count > 0;
        textEmpty.setVisibility(hasScans ? View.GONE : View.VISIBLE);
        recyclerScans.setVisibility(hasScans ? View.VISIBLE : View.GONE);
        btnExport.setEnabled(hasScans);
        btnExport.setAlpha(hasScans ? 1.0f : 0.4f);
    }

    private void showScannerError(String message) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_scanner_error_title))
                .setMessage(message)
                .setPositiveButton(getString(R.string.btn_ok), null)
                .show();
    }

    // ── Logout ─────────────────────────────────────────────────────────────────

    private void confirmLogout() {
        int count = scanSession.getCount();
        String message = getString(R.string.dialog_logout_message, count);
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_logout_title))
                .setMessage(message)
                .setPositiveButton(getString(R.string.btn_yes_logout), (d, w) -> performLogout())
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    private void performLogout() {
        scanSession.clearAll(); // wipe in-memory data BEFORE navigating
        Intent intent = new Intent(this, LoginActivity.class);
        // Clear entire back stack — pressing back after logout does nothing
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // ── Export ─────────────────────────────────────────────────────────────────

    private void exportToExcel() {
        if (scanSession.getCount() == 0) {
            Toast.makeText(this, getString(R.string.msg_export_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        ExportManager.exportToXlsx(this, scanSession.getItems(), new ExportManager.Callback() {
            @Override
            public void onSuccess(String filePath) {
                new AlertDialog.Builder(DashboardActivity.this)
                        .setTitle("Export Successful")
                        .setMessage(getString(R.string.msg_export_success, filePath))
                        .setPositiveButton(getString(R.string.btn_ok), null)
                        .show();
            }

            @Override
            public void onError(String error) {
                new AlertDialog.Builder(DashboardActivity.this)
                        .setTitle("Export Failed")
                        .setMessage(getString(R.string.msg_export_error, error))
                        .setPositiveButton(getString(R.string.btn_ok), null)
                        .show();
            }
        });
    }
}
