package com.example.spotly.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.spotly.AppExecutors;
import com.example.spotly.DatabaseHelper;
import com.example.spotly.R;
import com.google.android.material.card.MaterialCardView;

public class EditSavedLocationActivity extends AppCompatActivity {
    private EditText editTitle;
    private TextView textAddress;
    private MaterialCardView buttonPickLocation, buttonSave;
    private DatabaseHelper databaseHelper;
    private int savedLocationId;
    private int folderId; // Simpan ID folder untuk validasi
    private double selectedLat, selectedLng;
    private String selectedAddress;
    private ImageView kembali_simpan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_saved_location);

        editTitle = findViewById(R.id.editTitle);
        textAddress = findViewById(R.id.textAddress);
        buttonPickLocation = findViewById(R.id.buttonPickLocation);
        buttonSave = findViewById(R.id.buttonSave);
        kembali_simpan = findViewById(R.id.kembali_simpan);

        kembali_simpan.setOnClickListener(v -> onBackPressed());

        databaseHelper = new DatabaseHelper(this);
        savedLocationId = getIntent().getIntExtra("saved_location_id", -1);

        if (savedLocationId != -1) {
            loadSavedLocation(savedLocationId);
        } else {
            Toast.makeText(this, "Gagal memuat lokasi", Toast.LENGTH_SHORT).show();
            finish();
        }

        buttonPickLocation.setOnClickListener(v -> {
            Intent intent = new Intent(EditSavedLocationActivity.this, MapsPickLocationActivity.class);
            pickLocationLauncher.launch(intent);
        });

        buttonSave.setOnClickListener(v -> saveChanges());
    }

    private void loadSavedLocation(int id) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            DatabaseHelper.SavedLocation location = databaseHelper.getLocationById(id);
            runOnUiThread(() -> {
                if (location != null) {
                    editTitle.setText(location.getJudul());
                    folderId = location.getId_folder(); // Simpan folder ID
                    selectedLat = location.getLat();
                    selectedLng = location.getLng();
                    selectedAddress = location.getAlamat();
                    textAddress.setText(selectedAddress);
                }
            });
        });
    }

    private void saveChanges() {
        String judul = editTitle.getText().toString().trim();
        if (judul.isEmpty()) {
            editTitle.setError("Judul tidak boleh kosong");
            return;
        }

        AppExecutors.getInstance().diskIO().execute(() -> {
            boolean exists = databaseHelper.savedLocationTitleExists(judul, folderId, savedLocationId);

            if (exists) {
                runOnUiThread(() -> Toast.makeText(this, "Judul '" + judul + "' sudah ada di folder ini.", Toast.LENGTH_SHORT).show());
            } else {
                boolean updated = databaseHelper.updateSavedLocation(
                        savedLocationId,
                        judul,
                        selectedLat,
                        selectedLng,
                        selectedAddress
                );

                runOnUiThread(() -> {
                    if (updated) {
                        Toast.makeText(this, "Lokasi berhasil diperbarui", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Gagal memperbarui lokasi", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private final ActivityResultLauncher<Intent> pickLocationLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedLat = result.getData().getDoubleExtra("lat", 0);
                    selectedLng = result.getData().getDoubleExtra("lng", 0);
                    selectedAddress = result.getData().getStringExtra("alamat");
                    textAddress.setText(selectedAddress);
                    Toast.makeText(this, "Lokasi diubah", Toast.LENGTH_SHORT).show();
                }
            }
    );
}