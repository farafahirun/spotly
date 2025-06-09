package com.example.spotly.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.spotly.DatabaseHelper;
import com.example.spotly.R;
import com.example.spotly.DatabaseHelper.SavedLocation;
import com.google.android.material.card.MaterialCardView;

public class EditSavedLocationActivity extends AppCompatActivity {

    private EditText editTitle, editNote;
    private TextView textAddress;
    private MaterialCardView buttonPickLocation, buttonSave;
    private DatabaseHelper databaseHelper;
    private int savedLocationId;
    private double selectedLat, selectedLng;
    private String selectedAddress;
    private ImageView kembali_simpan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_saved_location);

        editTitle = findViewById(R.id.editTitle);
        editNote = findViewById(R.id.editNote);
        textAddress = findViewById(R.id.textAddress);
        buttonPickLocation = findViewById(R.id.buttonPickLocation);
        buttonSave = findViewById(R.id.buttonSave);
        kembali_simpan = findViewById(R.id.kembali_simpan);

        kembali_simpan.setOnClickListener(v -> {
            onBackPressed();
        });

        databaseHelper = new DatabaseHelper(this);

        savedLocationId = getIntent().getIntExtra("saved_location_id", -1);

        if (savedLocationId != -1) {
            loadSavedLocation(savedLocationId);
        } else {
            Toast.makeText(this, "Gagal memuat lokasi", Toast.LENGTH_SHORT).show();
            finish();
        }

        buttonPickLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditSavedLocationActivity.this, MapsPickLocationActivity.class);
                pickLocationLauncher.launch(intent);
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges();
            }
        });
    }

    private void loadSavedLocation(int id) {
        SavedLocation location = databaseHelper.getLocationById(id);
        if (location != null) {
            editTitle.setText(location.getJudul());
            editNote.setText(location.getNote());
            selectedLat = location.getLat();
            selectedLng = location.getLng();
            selectedAddress = location.getAlamat();
            textAddress.setText(selectedAddress);
        }
    }

    private void saveChanges() {
        String judul = editTitle.getText().toString().trim();
        String tanggal = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        String note = editNote.getText().toString().trim();

        boolean updated = databaseHelper.updateSavedLocation(
                savedLocationId,
                judul,
                tanggal,
                selectedLat,
                selectedLng,
                selectedAddress,
                note
        );

        if (updated) {
            Toast.makeText(this, "Lokasi berhasil diperbarui", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Gagal memperbarui lokasi", Toast.LENGTH_SHORT).show();
        }
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