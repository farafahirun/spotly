package com.example.spotly.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.spotly.AppExecutors;
import com.example.spotly.DatabaseHelper;
import com.example.spotly.MainActivity;
import com.example.spotly.R;
import com.example.spotly.ThemeHelper;
import com.example.spotly.activity.FormCeritaActivity;
import com.example.spotly.databinding.FragmentPetaBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.card.MaterialCardView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PetaFragment extends Fragment implements OnMapReadyCallback {
    private FragmentPetaBinding binding;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseHelper databaseHelper;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LatLng destinationLatLng = null;
    private Marker destinationMarker = null;
    private boolean isFirstLoad = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ThemeHelper.applyTheme(requireContext());
        binding = FragmentPetaBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        databaseHelper = new DatabaseHelper(getContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        updateIconMode();
        setupListeners();
        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        applyMapStyle();
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setOnMapLongClickListener(latLng -> {
            handleSelectedLocation(latLng, "Lokasi Dipilih");
        });
        checkLocationPermission();
    }

    private void handleSelectedLocation(LatLng location, String title) {
        if (mMap == null || getActivity() == null) return;
        ((MainActivity) requireActivity()).hideBottomNav();
        binding.fokusUser.setVisibility(View.GONE);
        destinationLatLng = location;
        if (destinationMarker != null) destinationMarker.remove();
        destinationMarker = mMap.addMarker(new MarkerOptions().position(location).title(title));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
        getAddressFromApi(location);
    }

    private void getAddressFromApi(LatLng latLng) {
        binding.markerInfoPanel.setVisibility(View.VISIBLE);
        binding.alamatSingkat.setText("Mencari alamat...");
        binding.markerAddress.setVisibility(View.GONE);
        binding.buttonRefreshAlamat.setVisibility(View.GONE);
        binding.closePanel.setVisibility(View.VISIBLE);

        if (!isNetworkAvailable()) {
            binding.alamatSingkat.setText("Gagal Terhubung");
            binding.markerAddress.setVisibility(View.GONE);
            binding.buttonRefreshAlamat.setVisibility(View.VISIBLE);
            return;
        }

        new Thread(() -> {
            try {
                if (getContext() == null) return;
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                final List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

                if(getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (addresses != null && !addresses.isEmpty()) {
                            String fullAddress = addresses.get(0).getAddressLine(0);
                            binding.alamatSingkat.setText(extractSimpleAddress(fullAddress));
                            binding.markerAddress.setText(fullAddress);
                            binding.markerAddress.setVisibility(View.VISIBLE);
                            binding.buttonRefreshAlamat.setVisibility(View.GONE);
                        } else {
                            binding.alamatSingkat.setText("Alamat Tidak Ditemukan");
                            binding.markerAddress.setVisibility(View.GONE);
                            binding.buttonRefreshAlamat.setVisibility(View.VISIBLE);
                        }
                    });
                }
            } catch (IOException e) {
                if(getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        binding.alamatSingkat.setText("Gagal Terhubung");
                        binding.markerAddress.setVisibility(View.GONE);
                        binding.buttonRefreshAlamat.setVisibility(View.VISIBLE);
                    });
                }
            }
        }).start();
    }

    private void setupListeners() {
        binding.modeIcon.setOnClickListener(v -> {
            String currentTheme = ThemeHelper.getCurrentTheme(requireContext());
            ThemeHelper.setTheme(requireContext(), "dark".equals(currentTheme) ? "light" : "dark");
            updateIconMode();
            applyMapStyle();
        });
        binding.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.trim().isEmpty()) searchLocation(query.trim());
                binding.search.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        binding.tipeMap.setOnClickListener(this::showMapTypePopup);
        binding.fokusUser.setOnClickListener(v -> focusToUserLocation());
        binding.closePanel.setOnClickListener(v -> {
            binding.markerInfoPanel.setVisibility(View.GONE);
            binding.fokusUser.setVisibility(View.VISIBLE);
            ((MainActivity) requireActivity()).showBottomNav();
        });
        binding.buttonSimpan.setOnClickListener(v -> {
            if (destinationLatLng == null) {
                Toast.makeText(requireContext(), "Tentukan lokasi dulu!", Toast.LENGTH_SHORT).show();
                return;
            }
            List<DatabaseHelper.Folder> folders = databaseHelper.getAllFolders();
            if (folders.isEmpty()) {
                Toast.makeText(requireContext(), "Buat folder dulu di menu Simpan!", Toast.LENGTH_SHORT).show();
                return;
            }
            showSaveLocationDialog(folders);
        });
        binding.buttonCerita.setOnClickListener(v -> {
            if (destinationLatLng == null) {
                Toast.makeText(requireContext(), "Tentukan lokasi terlebih dahulu!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getContext(), FormCeritaActivity.class);
            intent.putExtra("lat", destinationLatLng.latitude);
            intent.putExtra("lng", destinationLatLng.longitude);
            intent.putExtra("alamat", binding.markerAddress.getText().toString());
            intent.putExtra("show_map", true);
            startActivity(intent);
        });

        binding.buttonRefreshAlamat.setOnClickListener(v -> {
            if (destinationLatLng != null) getAddressFromApi(destinationLatLng);
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                mMap.setMyLocationEnabled(true);
                moveCameraToUserLocation();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void moveCameraToUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                LatLng lastKnownUserLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (isFirstLoad) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownUserLatLng, 15f));
                    isFirstLoad = false;
                }
            }
        });
    }

    private void focusToUserLocation() {
        if (mMap == null || getContext() == null) return;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f));
                    Toast.makeText(getContext(), "Fokus ke lokasi Anda", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Tidak dapat menemukan lokasi. Pastikan GPS aktif.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            checkLocationPermission();
        }
    }

    private void searchLocation(String locationName) {
        if (getContext() == null) return;
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                        getAddressFromApi(latLng);
                        destinationLatLng = latLng;
                        if (destinationMarker != null) destinationMarker.remove();
                        destinationMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(address.getAddressLine(0)));
                        ((MainActivity) requireActivity()).hideBottomNav();
                        binding.fokusUser.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(getContext(), "Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getContext(), "Gagal mencari lokasi", Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        }).start();
    }

    private void showSaveLocationDialog(List<DatabaseHelper.Folder> folders) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_save_location, null);
        builder.setView(dialogView);

        AutoCompleteTextView autoCompleteFolder = dialogView.findViewById(R.id.autoCompleteFolder);
        android.widget.EditText titleInput = dialogView.findViewById(R.id.titleInput);
        ImageView closePanel = dialogView.findViewById(R.id.close_panel);
        MaterialCardView buttonSave = dialogView.findViewById(R.id.buttonSave);

        List<String> folderNames = new ArrayList<>();
        for (DatabaseHelper.Folder folder : folders) {
            folderNames.add(folder.getNama_folder());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_menu_item, folderNames);
        autoCompleteFolder.setAdapter(adapter);

        final DatabaseHelper.Folder[] selectedFolder = new DatabaseHelper.Folder[1];
        autoCompleteFolder.setOnItemClickListener((parent, view, position, id) -> {
            selectedFolder[0] = folders.get(position);
        });

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();

        closePanel.setOnClickListener(v -> dialog.dismiss());

        buttonSave.setOnClickListener(v -> {
            if (!isNetworkAvailable()) {
                Toast.makeText(getContext(), "Tidak ada koneksi internet. Gagal menyimpan lokasi.", Toast.LENGTH_LONG).show();
                return;
            }

            String title = titleInput.getText().toString().trim();
            if (title.isEmpty()) {
                titleInput.setError("Judul wajib diisi");
                return;
            }
            if (selectedFolder[0] == null) {
                Toast.makeText(requireContext(), "Pilih folder dulu!", Toast.LENGTH_SHORT).show();
                return;
            }

            AppExecutors.getInstance().diskIO().execute(() -> {
                boolean exists = databaseHelper.savedLocationTitleExists(title, selectedFolder[0].getId_folder());
                if (exists) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Judul '" + title + "' sudah ada di folder ini.", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    DatabaseHelper.SavedLocation savedLocation = new DatabaseHelper.SavedLocation();
                    savedLocation.setId_folder(selectedFolder[0].getId_folder());
                    savedLocation.setJudul(title);
                    savedLocation.setLat(destinationLatLng.latitude);
                    savedLocation.setLng(destinationLatLng.longitude);
                    savedLocation.setAlamat(binding.markerAddress.getText().toString());
                    String tanggalSekarang = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    savedLocation.setTanggalCreate(tanggalSekarang);

                    long result = databaseHelper.insertSavedLocation(savedLocation);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (result != -1) {
                                Toast.makeText(requireContext(), "Lokasi berhasil disimpan!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(requireContext(), "Gagal menyimpan lokasi.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        });
    }

    private boolean isNetworkAvailable() {
        if (getContext() == null) {
            return false;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private void showMapTypePopup(View v) {
        View popupView = LayoutInflater.from(requireContext()).inflate(R.layout.tipe_map, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupView.findViewById(R.id.normalMap).setOnClickListener(v1 -> {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.sateliteMap).setOnClickListener(v1 -> {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.terrainMap).setOnClickListener(v1 -> {
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.hybridMap).setOnClickListener(v1 -> {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            popupWindow.dismiss();
        });
        popupWindow.showAsDropDown(binding.tipeMap, 0, 0);
    }

    private String extractSimpleAddress(String fullAddress) {
        String[] parts = fullAddress.split(",");
        return parts.length > 0 ? parts[0].trim() : fullAddress;
    }

    private void updateIconMode() {
        if (getContext() == null) return;
        String currentTheme = ThemeHelper.getCurrentTheme(requireContext());
        if ("dark".equals(currentTheme)) {
            binding.modeIcon.setImageResource(R.drawable.light_icon);
            binding.markerAddress.setTextColor(Color.WHITE);
            binding.alamatSingkat.setTextColor(Color.WHITE);
        } else {
            binding.modeIcon.setImageResource(R.drawable.dark_icon);
            binding.markerAddress.setTextColor(Color.BLACK);
            binding.alamatSingkat.setTextColor(Color.BLACK);
        }
    }

    private void applyMapStyle() {
        if (mMap == null || getContext() == null) return;
        String currentTheme = ThemeHelper.getCurrentTheme(requireContext());
        int styleResId = "dark".equals(currentTheme) ? R.raw.map_dark : R.raw.map_light;
        try {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), styleResId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission();
            } else {
                Toast.makeText(getContext(), "Izin lokasi ditolak", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}