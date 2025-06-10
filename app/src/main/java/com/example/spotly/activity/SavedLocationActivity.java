package com.example.spotly.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotly.AppExecutors;
import com.example.spotly.DatabaseHelper;
import com.example.spotly.R;
import com.example.spotly.adapter.SavedLocationAdapter;

import java.util.List;

/**
 * Activity sekarang mengimplementasikan listener dari adapternya
 */
public class SavedLocationActivity extends AppCompatActivity implements SavedLocationAdapter.SavedLocationAdapterListener {
    private RecyclerView recyclerView;
    private SavedLocationAdapter adapter;
    private DatabaseHelper databaseHelper;
    private int folderId;
    private ImageView kembali_simpan;
    private View emptyView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_location);

        recyclerView = findViewById(R.id.recyclerViewSavedLocation);
        emptyView = findViewById(R.id.emptyViewSavedLocation);
        progressBar = findViewById(R.id.progressBarSavedLocation);
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
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);

        AppExecutors.getInstance().diskIO().execute(() -> {
            List<DatabaseHelper.SavedLocation> savedLocationList = databaseHelper.getLocationsByFolderId(folderId);
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (savedLocationList.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);

                    // --- PERBAIKAN DI SINI: Panggilan konstruktor disesuaikan ---
                    adapter = new SavedLocationAdapter(savedLocationList, this, databaseHelper, this);
                    recyclerView.setAdapter(adapter);
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedLocations();
    }

    /**
     * Implementasi metode dari interface.
     * Akan dipanggil oleh adapter setelah lokasi dihapus.
     */
    @Override
    public void onLocationDeleted() {
        // Cek apakah daftar sekarang kosong
        if (adapter != null && adapter.getItemCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }
}