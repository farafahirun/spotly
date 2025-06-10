package com.example.spotly.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotly.AppExecutors;
import com.example.spotly.DatabaseHelper;
import com.example.spotly.R;
import com.example.spotly.activity.SavedLocationActivity;

import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    /**
     * Interface untuk komunikasi dari Adapter ke Fragment.
     */
    public interface FolderAdapterListener {
        void onFolderDeleted();
    }

    private List<DatabaseHelper.Folder> folderList;
    private Context context;
    private DatabaseHelper databaseHelper;
    private FolderAdapterListener listener; // Variabel untuk listener

    /**
     * Konstruktor diubah untuk menerima listener dari Fragment.
     * @param folderList List data folder.
     * @param context Context dari activity/fragment.
     * @param listener Implementasi listener (biasanya fragment itu sendiri).
     */
    public FolderAdapter(List<DatabaseHelper.Folder> folderList, Context context, FolderAdapterListener listener) {
        this.folderList = folderList;
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
        this.listener = listener;
    }

    /**
     * Metode untuk memperbarui data dari fragment.
     * @param newFolderList Daftar folder yang baru.
     */
    public void setData(List<DatabaseHelper.Folder> newFolderList) {
        this.folderList = newFolderList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_folder, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        DatabaseHelper.Folder folder = folderList.get(position);
        holder.txtFolderName.setText(folder.nama_folder);

        // Mengambil jumlah file juga operasi I/O, jalankan di background
        AppExecutors.getInstance().diskIO().execute(() -> {
            int fileCount = databaseHelper.getFileCountInFolder(folder.getId_folder());
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> {
                    if (fileCount == 0) {
                        holder.jumlah_file.setText("Kosong");
                    } else {
                        holder.jumlah_file.setText(fileCount + " file");
                    }
                });
            }
        });

        // Klik item untuk membuka isi folder
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SavedLocationActivity.class);
            intent.putExtra("folder_id", folder.getId_folder());
            context.startActivity(intent);
        });

        holder.btnEdit.setOnClickListener(v -> showEditDialog(folder, position));
        holder.btnDelete.setOnClickListener(v -> showDeleteDialog(folder, position));
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    private void showEditDialog(DatabaseHelper.Folder folder, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Nama Folder");
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(folder.nama_folder);
        builder.setView(input);
        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                AppExecutors.getInstance().diskIO().execute(() -> {
                    databaseHelper.updateFolder(folder.getId_folder(), newName);
                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(() -> {
                            folder.nama_folder = newName;
                            notifyItemChanged(position);
                        });
                    }
                });
            }
        });
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showDeleteDialog(final DatabaseHelper.Folder folder, final int position) {
        new AlertDialog.Builder(context)
                .setTitle("Hapus Folder")
                .setMessage("Apakah Anda yakin ingin menghapus folder '" + folder.getNama_folder() + "'? Semua lokasi di dalamnya juga akan terhapus.")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    AppExecutors.getInstance().diskIO().execute(() -> {
                        databaseHelper.deleteFolder(folder.getId_folder());
                        if (context instanceof Activity) {
                            ((Activity) context).runOnUiThread(() -> {
                                folderList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, folderList.size());

                                // Panggil listener untuk memberitahu fragment bahwa item telah dihapus
                                if (listener != null) {
                                    listener.onFolderDeleted();
                                }
                            });
                        }
                    });
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    public class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView txtFolderName, jumlah_file;
        ImageView btnEdit, btnDelete;
        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtFolderName = itemView.findViewById(R.id.txtFolderName);
            btnEdit = itemView.findViewById(R.id.edit_folder);
            jumlah_file = itemView.findViewById(R.id.jumlah_file);
            btnDelete = itemView.findViewById(R.id.hapus_folder);
        }
    }
}