package com.example.spotly.activity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
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
    private TextView tvJudulForm;
    private TextView tvAlamat;
    private DatabaseHelper dbHelper;

    private double lat, lng;
    private String alamat;
    private int ceritaIdToUpdate = -1;
    private int savedLocationIdToDelete = -1;
    private boolean shouldShowMap; // Flag untuk menentukan apakah peta akan ditampilkan

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_cerita);

        dbHelper = new DatabaseHelper(this);

        // Inisialisasi semua view
        etKategori = findViewById(R.id.etKategori);
        etJudul = findViewById(R.id.etJudul);
        etIsi = findViewById(R.id.etIsi);
        spinnerIconPerasaan = findViewById(R.id.spinnerIconPerasaan);
        btnSimpan = findViewById(R.id.btnSimpan);
        tvJudulForm = findViewById(R.id.tvJudulForm);
        tvAlamat = findViewById(R.id.tvAlamatCerita);

        // Ambil flag dari Intent, defaultnya true (cerita dari peta)
        shouldShowMap = getIntent().getBooleanExtra("show_map", true);

        // Setup Spinner
        String[] icons = {"😊 Senang", "😢 Sedih", "😠 Marah", "😲 Terkejut", "❤️ Suka", "🤔 Bingung"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, icons);
        spinnerIconPerasaan.setAdapter(adapter);

        // Cek intent untuk berbagai mode
        ceritaIdToUpdate = getIntent().getIntExtra("cerita_id_to_update", -1);
        savedLocationIdToDelete = getIntent().getIntExtra("saved_location_id_to_delete", -1);

        if (ceritaIdToUpdate != -1) {
            // Mode EDIT
            tvJudulForm.setText("Edit Cerita");
            btnSimpan.setText("Update");
            loadCeritaData(ceritaIdToUpdate);
        } else if (savedLocationIdToDelete != -1) {
            // Mode PINDAH dari Lokasi Tersimpan
            tvJudulForm.setText("Buat Cerita dari Lokasi");
            prefillFromSavedLocation();
        } else {
            // Mode BUAT BARU (baik dari PetaFragment atau CariTempatFragment)
            tvJudulForm.setText("Buat Cerita Baru");
            lat = getIntent().getDoubleExtra("lat", 0.0);
            lng = getIntent().getDoubleExtra("lng", 0.0);
            alamat = getIntent().getStringExtra("alamat");

            if (alamat != null && !alamat.isEmpty()) {
                tvAlamat.setText(alamat);
            } else {
                tvAlamat.setText("Alamat tidak tersedia");
            }
        }

        btnSimpan.setOnClickListener(v -> {
            if (validateInput()) {
                simpanAtauUpdateCerita();
            }
        });
    }

    private void loadCeritaData(int id) {
        DatabaseHelper.Cerita cerita = dbHelper.getCeritaById(id);
        if (cerita != null) {
            etKategori.setText(cerita.getKategori());
            etJudul.setText(cerita.getJudul());
            etIsi.setText(cerita.getIsi());
            tvAlamat.setText(cerita.getAlamat());

            this.lat = cerita.getLat();
            this.lng = cerita.getLng();
            this.alamat = cerita.getAlamat();
            this.shouldShowMap = cerita.hasMapView(); // Ambil status peta dari data yang ada

            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerIconPerasaan.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).equals(cerita.getIcon_perasaan())) {
                    spinnerIconPerasaan.setSelection(i);
                    break;
                }
            }
        }
    }

    private void prefillFromSavedLocation() {
        etJudul.setText(getIntent().getStringExtra("prefill_judul"));
        this.lat = getIntent().getDoubleExtra("prefill_lat", 0.0);
        this.lng = getIntent().getDoubleExtra("prefill_lng", 0.0);
        this.alamat = getIntent().getStringExtra("prefill_alamat");
        tvAlamat.setText(this.alamat);
    }

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

    private void simpanAtauUpdateCerita() {
        String kategori = etKategori.getText().toString().trim();
        String iconPerasaan = spinnerIconPerasaan.getSelectedItem().toString();
        String judul = etJudul.getText().toString().trim();
        String isi = etIsi.getText().toString().trim();

        if (ceritaIdToUpdate != -1) {
            // Proses UPDATE (tidak mengubah flag 'has_map_view')
            boolean success = dbHelper.updateCerita(ceritaIdToUpdate, kategori, iconPerasaan, judul, isi);
            if (success) {
                Toast.makeText(this, "Cerita berhasil diperbarui", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Gagal memperbarui cerita", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Proses INSERT BARU
            String tanggalSekarang = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            // Buat objek Cerita dengan menyertakan flag 'shouldShowMap'
            DatabaseHelper.Cerita cerita = new DatabaseHelper.Cerita(0, kategori, iconPerasaan, judul, lat, lng, alamat, isi, tanggalSekarang, shouldShowMap);

            long result = dbHelper.insertCerita(cerita);

            if (result > 0) {
                if (savedLocationIdToDelete != -1) {
                    dbHelper.deleteSavedLocation(savedLocationIdToDelete);
                    Toast.makeText(this, "Cerita dibuat & lokasi dipindahkan", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Cerita berhasil disimpan", Toast.LENGTH_SHORT).show();
                }
                finish();
            } else {
                Toast.makeText(this, "Gagal menyimpan cerita", Toast.LENGTH_SHORT).show();
            }
        }
    }
}