package com.example.spotly.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.spotly.R;
import com.example.spotly.adapter.SavedLocationAdapter;
import com.example.spotly.DatabaseHelper;
import com.example.spotly.DatabaseHelper.SavedLocation;

import java.util.List;

public class SavedLocationActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SavedLocationAdapter adapter;
    private List<SavedLocation> savedLocationList;
    private DatabaseHelper databaseHelper;
    private static final int REQUEST_CODE_PICK_LOCATION = 1001;
    private double selectedLat = 0.0;
    private double selectedLng = 0.0;
    private boolean isLocationSelected = false;
    private String selectedAlamat = "";
    private int folderId;
    private ImageView fabAddLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_location);

        recyclerView = findViewById(R.id.recyclerViewSavedLocation);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fabAddLocation = findViewById(R.id.btnAddLocation);
        databaseHelper = new DatabaseHelper(this);

        folderId = getIntent().getIntExtra("folder_id", -1);
        if (folderId == -1) {
            Toast.makeText(this, "Folder tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fabAddLocation.setOnClickListener(v -> {
            Intent intent = new Intent(SavedLocationActivity.this, MapsPickLocationActivity.class);
            startActivityForResult(intent, 1001);
        });
        loadSavedLocations();
    }

    private void showAddLocationDialog() {
        if (!isLocationSelected) {
            Toast.makeText(this, "Pilih lokasi terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tambah Lokasi");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_save_location, null);
        builder.setView(dialogView);

        EditText titleInput = dialogView.findViewById(R.id.titleInput);
        EditText noteInput = dialogView.findViewById(R.id.noteInput);

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String title = titleInput.getText().toString().trim();
            String note = noteInput.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(this, "Judul wajib diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            String tanggal = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

            SavedLocation savedLocation = new SavedLocation();
            savedLocation.setId_folder(folderId);
            savedLocation.setJudul(title);
            savedLocation.setNote(note);
            savedLocation.setLat(selectedLat);
            savedLocation.setLng(selectedLng);
            savedLocation.setAlamat(selectedAlamat);
            savedLocation.setTanggal(tanggal);

            long result = databaseHelper.insertSavedLocation(savedLocation);
            if (result != -1) {
                Toast.makeText(this, "Lokasi berhasil disimpan!", Toast.LENGTH_SHORT).show();
                loadSavedLocations();
                isLocationSelected = false;
                selectedLat = 0.0;
                selectedLng = 0.0;
                selectedAlamat = "";
            } else {
                Toast.makeText(this, "Gagal menyimpan lokasi.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void loadSavedLocations() {
        savedLocationList = databaseHelper.getLocationsByFolderId(folderId);
        adapter = new SavedLocationAdapter(savedLocationList, this, databaseHelper);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            selectedLat = data.getDoubleExtra("lat", 0.0);
            selectedLng = data.getDoubleExtra("lng", 0.0);
            selectedAlamat = data.getStringExtra("alamat");
            isLocationSelected = true;
            showAddLocationDialog();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedLocations();
    }
}
