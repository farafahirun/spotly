package com.example.spotly.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CariTempatFragment extends Fragment implements PencarianAdapter.OnItemClickListener {
    private SearchView searchView;
    private RecyclerView recyclerView;
    private PencarianAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvInfo;
    private LinearLayout infoLayout;
    private ImageView btnRefresh;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private String lastFailedQuery = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cari_tempat, container, false);

        searchView = view.findViewById(R.id.searchViewCari);
        recyclerView = view.findViewById(R.id.recyclerViewCari);
        progressBar = view.findViewById(R.id.progressBarCari);
        tvInfo = view.findViewById(R.id.tvInfoCari);
        infoLayout = view.findViewById(R.id.infoLayout);
        btnRefresh = view.findViewById(R.id.btnRefreshCari);

        setupRecyclerView();
        setupSearchView();
        setupNetworkCallback();

        btnRefresh.setOnClickListener(v -> {
            if (lastFailedQuery != null && !lastFailedQuery.isEmpty()) {
                performSearch(lastFailedQuery);
            }
        });

        return view;
    }

    private void setupNetworkCallback() {
        connectivityManager = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                if (lastFailedQuery != null) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Jaringan kembali terhubung, mencoba mencari ulang...", Toast.LENGTH_SHORT).show();
                            performSearch(lastFailedQuery);
                            lastFailedQuery = null;
                        });
                    }
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        if (connectivityManager != null) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (connectivityManager != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    private void performSearch(String query) {
        showLoading(true);

        ApiService apiService = NominatimApiClient.getClient().create(ApiService.class);
        Call<List<PlaceResult>> call = apiService.searchPlaces(query, "jsonv2", 1, 1);

        call.enqueue(new Callback<List<PlaceResult>>() {
            @Override
            public void onResponse(@NonNull Call<List<PlaceResult>> call, @NonNull Response<List<PlaceResult>> response) {
                showLoading(false);
                lastFailedQuery = null;
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isEmpty()) {
                        showInfo("Tidak ada hasil ditemukan untuk '" + query + "'.", false);
                    } else {
                        infoLayout.setVisibility(View.GONE);
                        adapter.setData(response.body());
                    }
                } else {
                    showInfo("Gagal memuat data. Error: " + response.code(), true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PlaceResult>> call, @NonNull Throwable t) {
                showLoading(false);
                lastFailedQuery = query;
                showInfo("Gagal terhubung. Periksa koneksi internet Anda.", true);
            }
        });
    }

    private void showInfo(String message, boolean showRefreshButton) {
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        infoLayout.setVisibility(View.VISIBLE);
        tvInfo.setText(message);
        btnRefresh.setVisibility(showRefreshButton ? View.VISIBLE : View.GONE);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PencarianAdapter(getContext());
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearchView() {
        showInfo("Ketik nama tempat untuk memulai pencarian.", false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                handler.removeCallbacks(searchRunnable);
                if (!query.trim().isEmpty()) {
                    performSearch(query.trim());
                }
                hideKeyboard();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                handler.removeCallbacks(searchRunnable);
                String query = newText.trim();
                if (query.length() < 3) {
                    adapter.clearData();
                    showInfo("Ketik minimal 3 huruf untuk memulai pencarian.", false);
                    return true;
                }
                searchRunnable = () -> performSearch(query);
                handler.postDelayed(searchRunnable, 500);
                return true;
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            infoLayout.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void hideKeyboard() {
        if (getActivity() != null && getActivity().getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onItemClick(PlaceResult placeResult) {
        hideKeyboard();
        try {
            double lat = Double.parseDouble(placeResult.getLatitude());
            double lon = Double.parseDouble(placeResult.getLongitude());
            Intent intent = new Intent(getContext(), FormCeritaActivity.class);
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lon);
            intent.putExtra("alamat", placeResult.getDisplayName());
            intent.putExtra("show_map", true);
            startActivity(intent);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Data lokasi dari API tidak valid.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}