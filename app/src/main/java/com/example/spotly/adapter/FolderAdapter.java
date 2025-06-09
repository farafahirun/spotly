package com.example.spotly.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotly.DatabaseHelper;
import com.example.spotly.R;
import com.example.spotly.activity.SavedLocationActivity;

import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    private List<DatabaseHelper.Folder> folderList;
    private Context context;

    public FolderAdapter(List<DatabaseHelper.Folder> folderList, Context context) {
        this.folderList = folderList;
        this.context = context;
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

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SavedLocationActivity.class);
            intent.putExtra("folder_id", folder.getId_folder());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    public class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView txtFolderName;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtFolderName = itemView.findViewById(R.id.txtFolderName);
        }
    }
}