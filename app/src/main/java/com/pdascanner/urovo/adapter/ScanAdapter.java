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

/**
 * RecyclerView adapter for the scan session list.
 *
 * IMPORTANT: receives the live list reference from ScanSession — do NOT copy it.
 * Call notifyItemInserted(0) after each addScan() to keep the UI in sync efficiently.
 */
public class ScanAdapter extends RecyclerView.Adapter<ScanAdapter.ViewHolder> {

    private static final SimpleDateFormat SDF =
            new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault());

    private final List<ScanItem> items;

    public ScanAdapter(List<ScanItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScanItem item = items.get(position);
        holder.textSeqNumber.setText(String.valueOf(item.getSequenceNumber()));
        holder.textBarcode.setText(item.getBarcodeString());
        holder.textTimestamp.setText(SDF.format(item.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textSeqNumber;
        final TextView textBarcode;
        final TextView textTimestamp;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textSeqNumber = itemView.findViewById(R.id.textSeqNumber);
            textBarcode   = itemView.findViewById(R.id.textBarcode);
            textTimestamp = itemView.findViewById(R.id.textTimestamp);
        }
    }
}
