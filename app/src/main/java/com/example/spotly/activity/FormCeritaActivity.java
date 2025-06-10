package com.example.spotly.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.spotly.DatabaseHelper;
import com.example.spotly.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormCeritaActivity extends AppCompatActivity {

    private EditText etKategori, etJudul, etIsi;
    private Spinner spinnerIconPerasaan;
    private Button btnSimpan;
    private String tanggalSekarang;
    private DatabaseHelper dbHelper;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_cerita);

        dbHelper = new DatabaseHelper(this);

        etKategori = findViewById(R.id.etKategori);
        etJudul = findViewById(R.id.etJudul);
        etIsi = findViewById(R.id.etIsi);
        spinnerIconPerasaan = findViewById(R.id.spinnerIconPerasaan);
        btnSimpan = findViewById(R.id.btnSimpan);

        // Isi spinner icon perasaan (contoh)
        String[] icons = {"😊 Senang", "😢 Sedih", "😠 Marah", "😲 Terkejut"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, icons);
        spinnerIconPerasaan.setAdapter(adapter);

        // Ambil data dari intent
        double lat = getIntent().getDoubleExtra("lat", 0.0);
        double lng = getIntent().getDoubleExtra("lng", 0.0);
        String alamat = getIntent().getStringExtra("alamat");

        // Tanggal otomatis
        tanggalSekarang = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        btnSimpan.setOnClickListener(v -> {
            if (validateInput()) {
                simpanCerita(lat, lng, alamat);
            }
        });
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            selectedImageUri = data.getData();
//            ivFotoPreview.setImageURI(selectedImageUri);
//        }
//    }

    private boolean validateInput() {
        if (etKategori.getText().toString().trim().isEmpty()) {
            etKategori.setError("Kategori wajib diisi");
            return false;
        }
        if (etJudul.getText().toString().trim().isEmpty()) {
            etJudul.setError("Judul wajib diisi");
            return false;
        }
        if (etIsi.getText().toString().trim().isEmpty()) {
            etIsi.setError("Isi cerita wajib diisi");
            return false;
        }
        return true;
    }

    private void simpanCerita(double lat, double lng, String alamat) {
        String kategori = etKategori.getText().toString().trim();
        String iconPerasaan = spinnerIconPerasaan.getSelectedItem().toString();
        String judul = etJudul.getText().toString().trim();
        String isi = etIsi.getText().toString().trim();

        long result = dbHelper.insertCerita(kategori, iconPerasaan, judul, lat, lng, alamat, isi, tanggalSekarang);

        if (result > 0) {
            Toast.makeText(this, "Cerita berhasil disimpan", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Gagal menyimpan cerita", Toast.LENGTH_SHORT).show();
        }
    }
}