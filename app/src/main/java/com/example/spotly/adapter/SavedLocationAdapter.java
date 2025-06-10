package com.example.spotly.adapter;

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

import com.example.spotly.DatabaseHelper;
import com.example.spotly.R;
import com.example.spotly.activity.EditSavedLocationActivity;
import com.example.spotly.activity.FormCeritaActivity;
import com.example.spotly.activity.MapViewActivity;
import com.example.spotly.DatabaseHelper.SavedLocation;

import java.util.List;

public class SavedLocationAdapter extends RecyclerView.Adapter<SavedLocationAdapter.ViewHolder> {
    private List<SavedLocation> savedLocationList;
    private Context context;
    private DatabaseHelper databaseHelper;

    public SavedLocationAdapter(List<SavedLocation> savedLocationList, Context context, DatabaseHelper databaseHelper) {
        this.savedLocationList = savedLocationList;
        this.context = context;
        this.databaseHelper = databaseHelper;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_saved_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavedLocation location = savedLocationList.get(position);

        holder.titleTextView.setText(location.getJudul());
        holder.addressTextView.setText(location.getAlamat());

        // Menampilkan tanggal dibuat atau diperbarui
        if (location.getTanggalUpdate() != null && !location.getTanggalUpdate().isEmpty()) {
            holder.dateTextView.setText("Diperbarui: " + location.getTanggalUpdate());
        } else {
            holder.dateTextView.setText("Dibuat: " + location.getTanggalCreate());
        }

        // Klik item untuk melihat peta
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MapViewActivity.class);
            intent.putExtra("latitude", location.getLat());
            intent.putExtra("longitude", location.getLng());
            intent.putExtra("title", location.getJudul());
            context.startActivity(intent);
        });

        // Tombol Hapus dengan konfirmasi
        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Hapus Lokasi")
                    .setMessage("Apakah Anda yakin ingin menghapus lokasi '" + location.getJudul() + "'?")
                    .setPositiveButton("Hapus", (dialog, which) -> {
                        databaseHelper.deleteSavedLocation(location.getId_location());
                        savedLocationList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, savedLocationList.size());
                        Toast.makeText(context, "Lokasi dihapus", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        // Tombol Edit
        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditSavedLocationActivity.class);
            intent.putExtra("saved_location_id", location.getId_location());
            context.startActivity(intent);
        });

        // Tombol baru: Pindahkan ke Cerita
        holder.buatCeritaButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, FormCeritaActivity.class);
            // Kirim data lokasi untuk diisi otomatis di form cerita
            intent.putExtra("prefill_judul", location.getJudul());
            intent.putExtra("prefill_alamat", location.getAlamat());
            intent.putExtra("prefill_lat", location.getLat());
            intent.putExtra("prefill_lng", location.getLng());
            // Kirim ID lokasi yang akan dihapus setelah cerita dibuat
            intent.putExtra("saved_location_id_to_delete", location.getId_location());
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
            buatCeritaButton = itemView.findViewById(R.id.buttonBuatCerita); // Pastikan ID ini ada di XML
        }
    }
}