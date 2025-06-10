package com.example.spotly.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotly.AppExecutors;
import com.example.spotly.DatabaseHelper;
import com.example.spotly.R;
import com.example.spotly.activity.SavedLocationActivity;

import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    public interface FolderAdapterListener {
        void onFolderDeleted();
    }

    private List<DatabaseHelper.Folder> folderList;
    private Context context;
    private DatabaseHelper databaseHelper;
    private FolderAdapterListener listener;

    public FolderAdapter(List<DatabaseHelper.Folder> folderList, Context context, FolderAdapterListener listener) {
        this.folderList = folderList;
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
        this.listener = listener;
    }

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
        AppExecutors.getInstance().diskIO().execute(() -> {
            int fileCount = databaseHelper.getFileCountInFolder(folder.getId_folder());
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> {
                    if (fileCount == 0) holder.jumlah_file.setText("Kosong");
                    else holder.jumlah_file.setText(fileCount + " file");
                });
            }
        });
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
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_folder_input, null);
        builder.setView(dialogView);

        final TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        final EditText input = dialogView.findViewById(R.id.input_folder_name);
        final Button btnBatal = dialogView.findViewById(R.id.btn_batal);
        final Button btnSimpan = dialogView.findViewById(R.id.btn_simpan);

        dialogTitle.setText("Edit Nama Folder");
        input.setText(folder.getNama_folder());
        btnSimpan.setText("Update");

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnSimpan.setOnClickListener(v -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(context, "Nama folder tidak boleh kosong.", Toast.LENGTH_SHORT).show();
                return;
            }
            AppExecutors.getInstance().diskIO().execute(() -> {
                boolean exists = databaseHelper.folderExists(newName, folder.getId_folder());
                if (exists) {
                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(() -> Toast.makeText(context, "Nama folder '" + newName + "' sudah ada.", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    databaseHelper.updateFolder(folder.getId_folder(), newName);
                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(() -> {
                            folder.nama_folder = newName;
                            notifyItemChanged(position);
                            dialog.dismiss();
                        });
                    }
                }
            });
        });
        btnBatal.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
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
                                if (listener != null) listener.onFolderDeleted();
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