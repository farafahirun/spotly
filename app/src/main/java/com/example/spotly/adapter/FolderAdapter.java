package com.example.spotly.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotly.DatabaseHelper;
import com.example.spotly.R;
import com.example.spotly.activity.SavedLocationActivity;

import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    private List<DatabaseHelper.Folder> folderList;
    private Context context;
    private DatabaseHelper databaseHelper;

    public FolderAdapter(List<DatabaseHelper.Folder> folderList, Context context) {
        this.folderList = folderList;
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
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

        int fileCount = databaseHelper.getFileCountInFolder(folder.getId_folder());
        if (fileCount == 0) {
            holder.jumlah_file.setText("Kosong");
        } else if (fileCount == 1) {
            holder.jumlah_file.setText("1 file");
        } else {
            holder.jumlah_file.setText(fileCount + " file");
        }
        holder.jumlah_file.setText(fileCount + " file");

        // Klik item → buka SavedLocationActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SavedLocationActivity.class);
            intent.putExtra("folder_id", folder.getId_folder());
            context.startActivity(intent);
        });

        // Klik Edit → show dialog edit
        holder.btnEdit.setOnClickListener(v -> showEditDialog(folder, position));

        // Klik Delete → show confirm dialog
        holder.btnDelete.setOnClickListener(v -> showDeleteDialog(folder, position));
    }

    @Override
    public int getItemCount() {
        return folderList.size();
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
                // Update di database
                databaseHelper.updateFolder(folder.getId_folder(), newName);
                // Update di list
                folder.nama_folder = newName;
                notifyItemChanged(position);
            }
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showDeleteDialog(DatabaseHelper.Folder folder, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Hapus Folder");
        builder.setMessage("Apakah Anda yakin ingin menghapus folder ini?");

        builder.setPositiveButton("Hapus", (dialog, which) -> {
            // Hapus di database
            databaseHelper.deleteFolder(folder.getId_folder());
            // Hapus dari list
            folderList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, folderList.size());
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}