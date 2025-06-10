package com.example.spotly.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotly.DatabaseHelper;
import com.example.spotly.R;
import com.example.spotly.adapter.SavedLocationAdapter;
import com.example.spotly.DatabaseHelper.SavedLocation;

import java.util.List;

public class SavedLocationActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SavedLocationAdapter adapter;
    private List<SavedLocation> savedLocationList;
    private DatabaseHelper databaseHelper;
    private int folderId;
    private ImageView kembali_simpan;
    private TextView emptyView; // Tampilan saat data kosong

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_location);

        recyclerView = findViewById(R.id.recyclerViewSavedLocation);
        emptyView = findViewById(R.id.emptyViewSavedLocation); // ID ini harus ada di XML
        kembali_simpan = findViewById(R.id.kembali_simpan);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        databaseHelper = new DatabaseHelper(this);

        folderId = getIntent().getIntExtra("folder_id", -1);
        if (folderId == -1) {
            Toast.makeText(this, "Folder tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        kembali_simpan.setOnClickListener(v -> onBackPressed());
    }

    private void loadSavedLocations() {
        savedLocationList = databaseHelper.getLocationsByFolderId(folderId);

        if (savedLocationList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            adapter = new SavedLocationAdapter(savedLocationList, this, databaseHelper);
            recyclerView.setAdapter(adapter);
        }
    }

    // Gunakan onResume untuk me-refresh data setiap kali activity ini ditampilkan
    @Override
    protected void onResume() {
        super.onResume();
        loadSavedLocations();
    }
}