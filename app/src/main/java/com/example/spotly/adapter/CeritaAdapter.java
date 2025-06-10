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
import com.example.spotly.activity.FormCeritaActivity;

import java.util.List;

public class CeritaAdapter extends RecyclerView.Adapter<CeritaAdapter.CeritaViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(DatabaseHelper.Cerita cerita);
    }

    private List<DatabaseHelper.Cerita> ceritaList;
    private OnItemClickListener listener;
    private Context context;
    private DatabaseHelper dbHelper;

    public CeritaAdapter(List<DatabaseHelper.Cerita> ceritaList, Context context, OnItemClickListener listener) {
        this.ceritaList = ceritaList;
        this.context = context;
        this.listener = listener;
        this.dbHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public CeritaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cerita, parent, false);
        return new CeritaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CeritaViewHolder holder, int position) {
        DatabaseHelper.Cerita cerita = ceritaList.get(position);
        holder.tvJudul.setText(cerita.getJudul());
        holder.tvKategori.setText(cerita.getKategori());
        holder.tvLokasi.setText(cerita.getAlamat());
        holder.tvIcon.setText(cerita.getIcon_perasaan().split(" ")[0]); // Ambil emoji saja

        holder.itemView.setOnClickListener(v -> listener.onItemClick(cerita));

        // Tombol Edit
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, FormCeritaActivity.class);
            intent.putExtra("cerita_id_to_update", cerita.getId_cerita());
            context.startActivity(intent);
        });

        // Tombol Delete
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Hapus Cerita")
                    .setMessage("Yakin ingin menghapus cerita '" + cerita.getJudul() + "'?")
                    .setPositiveButton("Hapus", (dialog, which) -> {
                        dbHelper.deleteCerita(cerita.getId_cerita());
                        ceritaList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, ceritaList.size());
                        Toast.makeText(context, "Cerita dihapus", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return ceritaList.size();
    }

    // Metode untuk memperbarui daftar saat filtering/searching
    public void updateList(List<DatabaseHelper.Cerita> newList) {
        ceritaList.clear();
        ceritaList.addAll(newList);
        notifyDataSetChanged();
    }

    static class CeritaViewHolder extends RecyclerView.ViewHolder {
        TextView tvJudul, tvKategori, tvLokasi, tvIcon;
        ImageView btnEdit, btnDelete;

        public CeritaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJudul = itemView.findViewById(R.id.tvJudul);
            tvKategori = itemView.findViewById(R.id.tvKategori);
            tvLokasi = itemView.findViewById(R.id.tvLokasi);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            btnEdit = itemView.findViewById(R.id.buttonEditCerita); // ID dari XML
            btnDelete = itemView.findViewById(R.id.buttonDeleteCerita); // ID dari XML
        }
    }
}