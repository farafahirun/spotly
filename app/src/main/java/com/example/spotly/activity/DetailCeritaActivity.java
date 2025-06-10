package com.example.spotly.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.example.spotly.AppExecutors;
import com.example.spotly.DatabaseHelper;
import com.example.spotly.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class DetailCeritaActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Variabel UI disesuaikan dengan layout baru
    private TextView tvJudul, tvKategori, tvIcon, tvLokasi, tvIsi, tvTanggal;
    private ImageView btnBack;
    private ProgressBar mapProgressBar;
    private View mapHeaderContainer; // Container untuk peta

    private MapView mapView;
    private GoogleMap googleMap;
    private DatabaseHelper dbHelper;

    private int ceritaId;
    private double lat, lng;
    private String judulCerita;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_cerita);

        initializeViews();

        dbHelper = new DatabaseHelper(this);

        // Ambil ID dari intent
        ceritaId = getIntent().getIntExtra("cerita_id", -1);
        if (ceritaId != -1) {
            loadCeritaDetails();
        } else {
            Toast.makeText(this, "Gagal memuat detail cerita.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set listener untuk tombol kembali
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void initializeViews() {
        tvJudul = findViewById(R.id.tvJudulDetail);
        tvKategori = findViewById(R.id.tvKategoriDetail);
        tvIcon = findViewById(R.id.tvIconDetail);
        tvLokasi = findViewById(R.id.tvLokasiDetail);
        tvIsi = findViewById(R.id.tvIsiDetail);
        tvTanggal = findViewById(R.id.tvTanggalDetail);
        mapView = findViewById(R.id.mapViewDetail);
        btnBack = findViewById(R.id.kembali_simpan);
        mapProgressBar = findViewById(R.id.map_progress_bar);
        mapHeaderContainer = findViewById(R.id.map_header_container);
    }

    private void loadCeritaDetails() {
        // Gunakan background thread untuk mengambil data dari database
        AppExecutors.getInstance().diskIO().execute(() -> {
            final DatabaseHelper.Cerita cerita = dbHelper.getCeritaById(ceritaId);

            // Kembali ke UI thread untuk menampilkan data
            runOnUiThread(() -> {
                if (cerita != null) {
                    tvJudul.setText(cerita.getJudul());
                    tvKategori.setText(cerita.getKategori());
                    tvLokasi.setText(cerita.getAlamat());
                    tvIsi.setText(cerita.getIsi());
                    tvTanggal.setText(cerita.getTanggal());

                    // Ambil bagian pertama dari teks (emoji) dan tampilkan
                    String iconFullText = cerita.getIcon_perasaan();
                    if (iconFullText != null && !iconFullText.isEmpty()) {
                        tvIcon.setText(iconFullText.split(" ")[0]);
                    }

                    this.lat = cerita.getLat();
                    this.lng = cerita.getLng();
                    this.judulCerita = cerita.getJudul();

                    // Logika untuk menampilkan atau menyembunyikan peta
                    if (cerita.hasMapView()) {
                        mapHeaderContainer.setVisibility(View.VISIBLE);
                        // Tampilkan loading saat peta akan diinisialisasi
                        mapProgressBar.setVisibility(View.VISIBLE);
                        // Inisialisasi MapView
                        mapView.onCreate(null);
                        mapView.getMapAsync(this);
                        mapView.onResume();
                    } else {
                        // Jika tidak ada peta, sembunyikan container dan progress bar
                        mapHeaderContainer.setVisibility(View.GONE);
                        mapProgressBar.setVisibility(View.GONE);
                    }

                } else {
                    Toast.makeText(this, "Cerita tidak ditemukan.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        // Nonaktifkan semua interaksi sentuhan pada peta agar berfungsi sebagai gambar
        googleMap.getUiSettings().setAllGesturesEnabled(false);

        // Jika data koordinat sudah ada, tampilkan di peta
        if (lat != 0.0 || lng != 0.0) {
            updateMapLocation();
        } else {
            // Jika tidak ada koordinat, tetap sembunyikan loading bar
            mapProgressBar.setVisibility(View.GONE);
        }
    }

    private void updateMapLocation() {
        LatLng location = new LatLng(lat, lng);
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(location).title(judulCerita));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));

        // PENTING: Sembunyikan ProgressBar setelah peta selesai dimuat dan ditampilkan
        mapProgressBar.setVisibility(View.GONE);
    }

    // Override lifecycle methods untuk MapView sangat penting
    // Kita perlu memastikan metode ini hanya dipanggil jika mapView terlihat
    @Override
    protected void onResume() {
        super.onResume();
        if (mapHeaderContainer != null && mapHeaderContainer.getVisibility() == View.VISIBLE) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapHeaderContainer != null && mapHeaderContainer.getVisibility() == View.VISIBLE) {
            mapView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapHeaderContainer != null && mapHeaderContainer.getVisibility() == View.VISIBLE) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapHeaderContainer != null && mapHeaderContainer.getVisibility() == View.VISIBLE) {
            mapView.onLowMemory();
        }
    }
}