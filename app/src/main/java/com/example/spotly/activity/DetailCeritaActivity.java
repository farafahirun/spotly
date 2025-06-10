package com.example.spotly.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.spotly.DatabaseHelper;
import com.example.spotly.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class DetailCeritaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView tvJudul, tvKategori, tvIcon, tvLokasi, tvIsi, tvTanggal;
    private MapView mapView;
    private GoogleMap googleMap;
    private DatabaseHelper dbHelper;
    private double lat, lng;
    private String judulCerita;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_cerita);

        tvJudul = findViewById(R.id.tvJudulDetail);
        tvKategori = findViewById(R.id.tvKategoriDetail);
        tvIcon = findViewById(R.id.tvIconDetail);
        tvLokasi = findViewById(R.id.tvLokasiDetail);
        tvIsi = findViewById(R.id.tvIsiDetail);
        tvTanggal = findViewById(R.id.tvTanggalDetail);
        mapView = findViewById(R.id.mapViewDetail);

        dbHelper = new DatabaseHelper(this);

        // Inisialisasi MapView
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Ambil ID dari intent
        int ceritaId = getIntent().getIntExtra("cerita_id", -1);
        if (ceritaId != -1) {
            loadCeritaDetails(ceritaId);
        } else {
            Toast.makeText(this, "Gagal memuat detail cerita.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadCeritaDetails(int id) {
        DatabaseHelper.Cerita cerita = dbHelper.getCeritaById(id);
        if(cerita != null) {
            tvJudul.setText(cerita.getJudul());
            tvKategori.setText(cerita.getKategori());
            tvIcon.setText(cerita.getIcon_perasaan());
            tvLokasi.setText(cerita.getAlamat());
            tvIsi.setText(cerita.getIsi());
            tvTanggal.setText(cerita.getTanggal());

            this.lat = cerita.getLat();
            this.lng = cerita.getLng();
            this.judulCerita = cerita.getJudul();

            // Jika peta sudah siap, langsung update
            if (googleMap != null) {
                updateMapLocation();
            }
        } else {
            Toast.makeText(this, "Cerita tidak ditemukan.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        if (lat != 0.0 && lng != 0.0) {
            updateMapLocation();
        }
    }

    private void updateMapLocation() {
        LatLng location = new LatLng(lat, lng);
        googleMap.clear(); // Hapus marker sebelumnya jika ada
        googleMap.addMarker(new MarkerOptions().position(location).title(judulCerita));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
    }

    // Override lifecycle methods untuk MapView
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}