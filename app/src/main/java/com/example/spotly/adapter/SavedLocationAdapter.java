package com.example.spotly.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotly.DatabaseHelper;
import com.example.spotly.activity.EditSavedLocationActivity;
import com.example.spotly.activity.MapViewActivity;
import com.example.spotly.R;
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
        holder.dateTextView.setText(location.getTanggal());
        holder.noteTextView.setText(location.getNote());
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MapViewActivity.class);
            intent.putExtra("latitude", location.getLat());
            intent.putExtra("longitude", location.getLng());
            intent.putExtra("title", location.getJudul());
            context.startActivity(intent);
        });

        holder.deleteButton.setOnClickListener(v -> {
            databaseHelper.deleteSavedLocation(location.getId_location());
            savedLocationList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, savedLocationList.size());
            Toast.makeText(context, "Lokasi dihapus", Toast.LENGTH_SHORT).show();
        });

        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditSavedLocationActivity.class);
            intent.putExtra("saved_location_id", location.getId_location());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return savedLocationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, addressTextView, dateTextView, noteTextView;
        ImageButton deleteButton, editButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textTitle);
            addressTextView = itemView.findViewById(R.id.textAddress);
            dateTextView = itemView.findViewById(R.id.textDate);
            noteTextView = itemView.findViewById(R.id.textNote);
            deleteButton = itemView.findViewById(R.id.buttonDelete);
            editButton = itemView.findViewById(R.id.buttonEdit);
        }
    }
}