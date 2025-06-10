package com.example.spotly.activity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.spotly.DatabaseHelper;
import com.example.spotly.R;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FormCeritaActivity extends AppCompatActivity {

    private EditText etKategori, etJudul, etIsi, tvAlamat;
    private MaterialCardView cardViewSimpan;
    private TextView tvButtonSimpan, tvJudulForm;
    private ImageView kembaliSimpan;
    private DatabaseHelper dbHelper;
    private LinearLayout emojiInputContainer;
    private List<Button> emojiButtons = new ArrayList<>();
    private String selectedEmoji = "";
    private double lat, lng;
    private String alamat;
    private int ceritaIdToUpdate = -1;
    private int savedLocationIdToDelete = -1;
    private boolean shouldShowMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_cerita);

        dbHelper = new DatabaseHelper(this);
        initializeViews();
        setupEmojiInputButtons();

        shouldShowMap = getIntent().getBooleanExtra("show_map", true);
        ceritaIdToUpdate = getIntent().getIntExtra("cerita_id_to_update", -1);
        savedLocationIdToDelete = getIntent().getIntExtra("saved_location_id_to_delete", -1);

        if (ceritaIdToUpdate != -1) {
            tvJudulForm.setText("Edit Cerita");
            tvButtonSimpan.setText("Update Cerita");
            loadCeritaData(ceritaIdToUpdate);
        } else if (savedLocationIdToDelete != -1) {
            tvJudulForm.setText("Buat Cerita dari Lokasi");
            prefillFromSavedLocation();
        }
        else {
            tvJudulForm.setText("Tambah Cerita");
            lat = getIntent().getDoubleExtra("lat", 0.0);
            lng = getIntent().getDoubleExtra("lng", 0.0);
            alamat = getIntent().getStringExtra("alamat");
            if (alamat != null && !alamat.isEmpty()) {
                tvAlamat.setText(alamat);
            } else {
                tvAlamat.setText("Alamat tidak tersedia");
            }
        }

        cardViewSimpan.setOnClickListener(v -> {
            if (validateInput()) {
                simpanAtauUpdateCerita();
            }
        });

        kembaliSimpan.setOnClickListener(v -> onBackPressed());
    }

    private void initializeViews() {
        etKategori = findViewById(R.id.etKategori);
        etJudul = findViewById(R.id.etJudul);
        etIsi = findViewById(R.id.etIsi);
        tvJudulForm = findViewById(R.id.tvJudulForm);
        tvAlamat = findViewById(R.id.tvAlamatCerita);
        emojiInputContainer = findViewById(R.id.emoji_input_container);
        kembaliSimpan = findViewById(R.id.kembali_simpan);
        cardViewSimpan = findViewById(R.id.Simpan);
        tvButtonSimpan = findViewById(R.id.btnSimpan);

        tvAlamat.setFocusable(false);
        tvAlamat.setClickable(false);
    }

    private void prefillFromSavedLocation() {
        String prefillJudul = getIntent().getStringExtra("prefill_judul");
        String prefillAlamat = getIntent().getStringExtra("prefill_alamat");
        etJudul.setText(prefillJudul);
        tvAlamat.setText(prefillAlamat);
        this.lat = getIntent().getDoubleExtra("prefill_lat", 0.0);
        this.lng = getIntent().getDoubleExtra("prefill_lng", 0.0);
        this.alamat = prefillAlamat;
        this.shouldShowMap = true;
    }

    private void simpanAtauUpdateCerita() {
        // =======================================================
        //      TAMBAHKAN PENGECEKAN KONEKSI DI SINI
        // =======================================================
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Tidak ada koneksi internet. Gagal menyimpan cerita.", Toast.LENGTH_LONG).show();
            return; // Hentikan proses jika offline
        }
        // =======================================================

        // Proses penyimpanan akan lanjut jika online
        String kategori = etKategori.getText().toString().trim();
        String judul = etJudul.getText().toString().trim();
        String isi = etIsi.getText().toString().trim();
        String iconPerasaan = this.selectedEmoji;
        String alamatFinal = tvAlamat.getText().toString();

        if (ceritaIdToUpdate != -1) {
            boolean success = dbHelper.updateCerita(ceritaIdToUpdate, kategori, iconPerasaan, judul, isi);
            if (success) {
                Toast.makeText(this, "Cerita berhasil diperbarui", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Gagal memperbarui cerita", Toast.LENGTH_SHORT).show();
            }
        } else {
            String tanggalSekarang = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            DatabaseHelper.Cerita cerita = new DatabaseHelper.Cerita(0, kategori, iconPerasaan, judul, lat, lng, alamatFinal, isi, tanggalSekarang, shouldShowMap);
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

    private void setupEmojiInputButtons() {
        String[] icons = {"😊", "😢", "😠", "😲", "❤️", "🤔"};
        emojiInputContainer.removeAllViews();
        emojiButtons.clear();

        for (String iconText : icons) {
            Button button = new Button(this, null, android.R.attr.buttonStyleSmall);
            button.setText(iconText);
            button.setBackgroundResource(R.drawable.emoji_button_background);
            button.setTextColor(ContextCompat.getColorStateList(this, R.color.emoji_button_text_color));
            button.setPadding(40, 0, 40, 0);
            button.setAllCaps(false);

            button.setOnClickListener(v -> {
                selectedEmoji = iconText;
                updateButtonSelection();
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMarginEnd(16);
            button.setLayoutParams(params);

            emojiButtons.add(button);
            emojiInputContainer.addView(button);
        }
    }

    private void updateButtonSelection() {
        for (Button btn : emojiButtons) {
            btn.setSelected(btn.getText().toString().equals(selectedEmoji));
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private void loadCeritaData(int id) {
        DatabaseHelper.Cerita cerita = dbHelper.getCeritaById(id);
        if (cerita != null) {
            etKategori.setText(cerita.getKategori());
            etJudul.setText(cerita.getJudul());
            etIsi.setText(cerita.getIsi());
            tvAlamat.setText(cerita.getAlamat());

            selectedEmoji = cerita.getIcon_perasaan();
            updateButtonSelection();

            this.lat = cerita.getLat();
            this.lng = cerita.getLng();
            this.alamat = cerita.getAlamat();
            this.shouldShowMap = cerita.hasMapView();
        }
    }

    private boolean validateInput() {
        if (etJudul.getText().toString().trim().isEmpty()) {
            etJudul.setError("Judul wajib diisi");
            return false;
        }
        if (etKategori.getText().toString().trim().isEmpty()) {
            etKategori.setError("Kategori wajib diisi");
            return false;
        }
        if (selectedEmoji.isEmpty()) {
            Toast.makeText(this, "Pilih perasaanmu dulu ya!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etIsi.getText().toString().trim().isEmpty()) {
            etIsi.setError("Isi cerita wajib diisi");
            return false;
        }
        return true;
    }
}