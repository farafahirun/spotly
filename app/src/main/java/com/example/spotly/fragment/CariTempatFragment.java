package com.example.spotly.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotly.R;
import com.example.spotly.activity.FormCeritaActivity;
import com.example.spotly.adapter.PencarianAdapter;
import com.example.spotly.model.PlaceResult;
import com.example.spotly.network.ApiService;
import com.example.spotly.network.NominatimApiClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CariTempatFragment extends Fragment implements PencarianAdapter.OnItemClickListener {

    public static final String REQUEST_KEY = "search_result_request";
    public static final String KEY_LAT = "result_latitude";
    public static final String KEY_LON = "result_longitude";
    public static final String KEY_NAME = "result_display_name";

    private SearchView searchView;
    private RecyclerView recyclerView;
    private PencarianAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvInfo;

    // Variabel untuk Debouncing
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cari_tempat, container, false);

        searchView = view.findViewById(R.id.searchViewCari);
        recyclerView = view.findViewById(R.id.recyclerViewCari);
        progressBar = view.findViewById(R.id.progressBarCari);
        tvInfo = view.findViewById(R.id.tvInfoCari);

        setupRecyclerView();
        setupSearchView();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PencarianAdapter(new ArrayList<>(), getContext(), this);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearchView() {
        showInfo("Ketik nama tempat untuk memulai pencarian.");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Jika pengguna menekan enter, langsung cari
                handler.removeCallbacks(searchRunnable);
                if (!query.trim().isEmpty()) {
                    performSearch(query.trim());
                }
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Hapus pencarian sebelumnya yang mungkin sedang menunggu
                handler.removeCallbacks(searchRunnable);

                String query = newText.trim();

                // Hanya cari jika query lebih dari 2 huruf untuk efisiensi
                if (query.length() < 3) {
                    adapter.clearData(); // Kosongkan hasil jika query pendek
                    showInfo("Ketik minimal 3 huruf untuk memulai pencarian.");
                    return true;
                }

                // Buat tugas pencarian baru
                searchRunnable = () -> performSearch(query);

                // Jalankan tugas setelah jeda 500 milidetik (setengah detik)
                handler.postDelayed(searchRunnable, 500);

                return true;
            }
        });
    }

    private void performSearch(String query) {
        showLoading(true);

        ApiService apiService = NominatimApiClient.getClient().create(ApiService.class);
        Call<List<PlaceResult>> call = apiService.searchPlaces(query, "jsonv2", 1, 1);

        call.enqueue(new Callback<List<PlaceResult>>() {
            @Override
            public void onResponse(@NonNull Call<List<PlaceResult>> call, @NonNull Response<List<PlaceResult>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isEmpty()) {
                        showInfo("Tidak ada hasil ditemukan untuk '" + query + "'.");
                    } else {
                        tvInfo.setVisibility(View.GONE);
                        adapter.setData(response.body());
                    }
                } else {
                    showInfo("Gagal memuat data. Error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PlaceResult>> call, @NonNull Throwable t) {
                showLoading(false);
                showInfo("Gagal terhubung. Periksa koneksi internet Anda.");
            }
        });
    }

    @Override
    public void onItemClick(PlaceResult placeResult) {
        try {
            double lat = Double.parseDouble(placeResult.getLatitude());
            double lon = Double.parseDouble(placeResult.getLongitude());

            Intent intent = new Intent(getContext(), FormCeritaActivity.class);
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lon);
            intent.putExtra("alamat", placeResult.getDisplayName());
            intent.putExtra("show_map", false); // <-- KIRIM FLAG INI
            startActivity(intent);

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Data lokasi tidak valid", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            tvInfo.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showInfo(String message) {
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        tvInfo.setVisibility(View.VISIBLE);
        tvInfo.setText(message);
    }
}