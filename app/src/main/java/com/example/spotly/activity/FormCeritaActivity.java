package com.example.spotly.activity;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.spotly.DatabaseHelper;
import com.example.spotly.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FormCeritaActivity extends AppCompatActivity {

    private EditText etKategori, etJudul, etIsi;
    private Button btnSimpan;
    private TextView tvJudulForm, tvAlamat;
    private DatabaseHelper dbHelper;

    // Komponen UI baru untuk input emoji
    private LinearLayout emojiInputContainer;
    private List<Button> emojiButtons = new ArrayList<>();
    private String selectedEmoji = ""; // Menyimpan perasaan yang dipilih

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

        // Inisialisasi View
        etKategori = findViewById(R.id.etKategori);
        etJudul = findViewById(R.id.etJudul);
        etIsi = findViewById(R.id.etIsi);
        btnSimpan = findViewById(R.id.btnSimpan);
        tvJudulForm = findViewById(R.id.tvJudulForm);
        tvAlamat = findViewById(R.id.tvAlamatCerita);
        emojiInputContainer = findViewById(R.id.emoji_input_container);

        // Panggil metode untuk membuat tombol-tombol emoji
        setupEmojiInputButtons();

        // Ambil data dari Intent
        shouldShowMap = getIntent().getBooleanExtra("show_map", true);
        ceritaIdToUpdate = getIntent().getIntExtra("cerita_id_to_update", -1);
        savedLocationIdToDelete = getIntent().getIntExtra("saved_location_id_to_delete", -1);

        // Logika untuk mode Tambah atau Edit
        if (ceritaIdToUpdate != -1) {
            tvJudulForm.setText("Edit Cerita");
            btnSimpan.setText("Update");
            loadCeritaData(ceritaIdToUpdate);
        } else {
            // Mode Buat Baru
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

    private void setupEmojiInputButtons() {
        // Daftar emoji TANPA "Semua"
        String[] icons = {"😊 Senang", "😢 Sedih", "😠 Marah", "😲 Terkejut", "❤️ Suka", "🤔 Bingung"};
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
                selectedEmoji = iconText; // Simpan teks emoji yang dipilih
                updateButtonSelection(); // Perbarui tampilan tombol
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

    private void loadCeritaData(int id) {
        DatabaseHelper.Cerita cerita = dbHelper.getCeritaById(id);
        if (cerita != null) {
            etKategori.setText(cerita.getKategori());
            etJudul.setText(cerita.getJudul());
            etIsi.setText(cerita.getIsi());
            tvAlamat.setText(cerita.getAlamat());

            // Set emoji yang sudah tersimpan
            selectedEmoji = cerita.getIcon_perasaan();
            updateButtonSelection(); // Update tampilan tombol sesuai data

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
        // Validasi baru: pastikan pengguna sudah memilih perasaan
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

    private void simpanAtauUpdateCerita() {
        String kategori = etKategori.getText().toString().trim();
        String judul = etJudul.getText().toString().trim();
        String isi = etIsi.getText().toString().trim();
        // Ambil perasaan dari variabel yang sudah kita simpan
        String iconPerasaan = this.selectedEmoji;

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
            DatabaseHelper.Cerita cerita = new DatabaseHelper.Cerita(0, kategori, iconPerasaan, judul, lat, lng, alamat, isi, tanggalSekarang, shouldShowMap);
            long result = dbHelper.insertCerita(cerita);
            if (result > 0) {
                Toast.makeText(this, "Cerita berhasil disimpan", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Gagal menyimpan cerita", Toast.LENGTH_SHORT).show();
            }
        }
    }
}