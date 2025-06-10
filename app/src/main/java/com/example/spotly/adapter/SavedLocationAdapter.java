package com.example.spotly.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotly.AppExecutors;
import com.example.spotly.DatabaseHelper;
import com.example.spotly.R;
import com.example.spotly.activity.EditSavedLocationActivity;
import com.example.spotly.activity.FormCeritaActivity;
import com.example.spotly.activity.MapViewActivity;

import java.util.List;

public class SavedLocationAdapter extends RecyclerView.Adapter<SavedLocationAdapter.ViewHolder> {

    public interface SavedLocationAdapterListener {
        void onLocationDeleted();
    }

    private List<DatabaseHelper.SavedLocation> savedLocationList;
    private Context context;
    private DatabaseHelper databaseHelper;
    private SavedLocationAdapterListener listener;

    public SavedLocationAdapter(List<DatabaseHelper.SavedLocation> savedLocationList, Context context, DatabaseHelper databaseHelper, SavedLocationAdapterListener listener) {
        this.savedLocationList = savedLocationList;
        this.context = context;
        this.databaseHelper = databaseHelper;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_saved_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DatabaseHelper.SavedLocation location = savedLocationList.get(position);

        holder.titleTextView.setText(location.getJudul());
        holder.addressTextView.setText(location.getAlamat());

        if (location.getTanggalUpdate() != null && !location.getTanggalUpdate().isEmpty()) {
            holder.dateTextView.setText("Diperbarui: " + location.getTanggalUpdate());
        } else {
            holder.dateTextView.setText("Dibuat: " + location.getTanggalCreate());
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MapViewActivity.class);
            intent.putExtra("latitude", location.getLat());
            intent.putExtra("longitude", location.getLng());
            intent.putExtra("title", location.getJudul());
            context.startActivity(intent);
        });

        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Hapus Lokasi")
                    .setMessage("Apakah Anda yakin ingin menghapus lokasi '" + location.getJudul() + "'?")
                    .setPositiveButton("Hapus", (dialog, which) -> {
                        AppExecutors.getInstance().diskIO().execute(() -> {
                            databaseHelper.deleteSavedLocation(location.getId_location());
                            if (context instanceof Activity) {
                                ((Activity) context).runOnUiThread(() -> {
                                    savedLocationList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, savedLocationList.size());
                                    Toast.makeText(context, "Lokasi dihapus", Toast.LENGTH_SHORT).show();
                                    if (listener != null) {
                                        listener.onLocationDeleted();
                                    }
                                });
                            }
                        });
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditSavedLocationActivity.class);
            intent.putExtra("saved_location_id", location.getId_location());
            context.startActivity(intent);
        });

        holder.buatCeritaButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, FormCeritaActivity.class);
            intent.putExtra("prefill_judul", location.getJudul());
            intent.putExtra("prefill_alamat", location.getAlamat());
            intent.putExtra("prefill_lat", location.getLat());
            intent.putExtra("prefill_lng", location.getLng());
            intent.putExtra("saved_location_id_to_delete", location.getId_location());
            intent.putExtra("show_map", true);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return savedLocationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, addressTextView, dateTextView;
        ImageView deleteButton, editButton, buatCeritaButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textTitle);
            addressTextView = itemView.findViewById(R.id.textAddress);
            dateTextView = itemView.findViewById(R.id.textDate);
            deleteButton = itemView.findViewById(R.id.buttonDelete);
            editButton = itemView.findViewById(R.id.buttonEdit);
            buatCeritaButton = itemView.findViewById(R.id.buttonBuatCerita);
        }
    }
}