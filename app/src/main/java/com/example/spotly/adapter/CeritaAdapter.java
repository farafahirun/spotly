package com.example.spotly.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotly.AppExecutors;
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
        holder.tvIcon.setText(cerita.getIcon_perasaan().split(" ")[0]);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(cerita));

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, FormCeritaActivity.class);
            intent.putExtra("cerita_id_to_update", cerita.getId_cerita());
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setTitle("Hapus Cerita")
                    .setMessage("Yakin ingin menghapus cerita '" + cerita.getJudul() + "'?")
                    .setNegativeButton("Batal", null)
                    .setPositiveButton("Hapus", (dialog, which) -> {
                        if (context instanceof Activity) {
                            ProgressBar progressBar = ((Activity) context).findViewById(R.id.progressBarCerita);
                            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
                        }

                        AppExecutors.getInstance().diskIO().execute(() -> {
                            dbHelper.deleteCerita(cerita.getId_cerita());
                            if (context instanceof Activity) {
                                ((Activity) context).runOnUiThread(() -> {
                                    ceritaList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, ceritaList.size());
                                    Toast.makeText(context, "Cerita dihapus", Toast.LENGTH_SHORT).show();

                                    ProgressBar progressBar = ((Activity) context).findViewById(R.id.progressBarCerita);
                                    if (progressBar != null) progressBar.setVisibility(View.GONE);

                                    if (ceritaList.isEmpty()) {
                                        View emptyViewContainer = ((Activity) context).findViewById(R.id.emptyViewCerita);
                                        if (emptyViewContainer != null) {
                                            emptyViewContainer.setVisibility(View.VISIBLE);
                                            TextView emptyTitle = emptyViewContainer.findViewById(R.id.emptyViewCerita_title);
                                            TextView emptySubtitle = emptyViewContainer.findViewById(R.id.emptyViewCerita_subtitle);
                                            if (emptyTitle != null)
                                                emptyTitle.setText("Buat Cerita Pertamamu");
                                            if (emptySubtitle != null)
                                                emptySubtitle.setText("Setiap tempat punya kenangan. Tulis ceritamu sekarang!");
                                        }
                                    }
                                });
                            }
                        });
                    });
            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(dialogInterface -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.red_destructive));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.utama));
            });
            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return ceritaList.size();
    }

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
            btnEdit = itemView.findViewById(R.id.buttonEditCerita);
            btnDelete = itemView.findViewById(R.id.buttonDeleteCerita);
        }
    }
}