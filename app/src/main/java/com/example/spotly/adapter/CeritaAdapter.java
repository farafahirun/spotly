package com.example.spotly.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotly.DatabaseHelper;
import com.example.spotly.R;

import java.util.List;

public class CeritaAdapter extends RecyclerView.Adapter<CeritaAdapter.CeritaViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(DatabaseHelper.Cerita cerita);
    }

    private List<DatabaseHelper.Cerita> ceritaList;
    private OnItemClickListener listener;

    public CeritaAdapter(List<DatabaseHelper.Cerita> ceritaList, OnItemClickListener listener) {
        this.ceritaList = ceritaList;
        this.listener = listener;
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
        holder.tvIcon.setText(cerita.getIcon_perasaan());

        holder.itemView.setOnClickListener(v -> listener.onItemClick(cerita));
    }

    @Override
    public int getItemCount() {
        return ceritaList.size();
    }

    static class CeritaViewHolder extends RecyclerView.ViewHolder {
        TextView tvJudul, tvKategori, tvLokasi, tvIcon;

        public CeritaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJudul = itemView.findViewById(R.id.tvJudul);
            tvKategori = itemView.findViewById(R.id.tvKategori);
            tvLokasi = itemView.findViewById(R.id.tvLokasi);
            tvIcon = itemView.findViewById(R.id.tvIcon);
        }
    }
}