package com.example.spotly.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.spotly.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class DetailCeritaActivity extends AppCompatActivity implements OnMapReadyCallback {

    TextView tvJudul, tvKategori, tvIcon, tvLokasi, tvIsi, tvTanggal;
    MapView mapView;
    private double lat, lng;

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

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Get data from intent
        String judul = getIntent().getStringExtra("judul");
        String kategori = getIntent().getStringExtra("kategori");
        String icon = getIntent().getStringExtra("icon_perasaan");
        String lokasi = getIntent().getStringExtra("alamat");
        String isi = getIntent().getStringExtra("isi");
        String tanggal = getIntent().getStringExtra("tanggal");
        lat = getIntent().getDoubleExtra("lat", 0.0);
        lng = getIntent().getDoubleExtra("lng", 0.0);

        tvJudul.setText(judul);
        tvKategori.setText(kategori);
        tvIcon.setText(icon);
        tvLokasi.setText(lokasi);
        tvIsi.setText(isi);
        tvTanggal.setText(tanggal);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng location = new LatLng(lat, lng);
        googleMap.addMarker(new MarkerOptions().position(location).title("Lokasi Cerita"));
        googleMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(location, 15f));
    }

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