package com.example.spotly.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.example.spotly.DatabaseHelper;
import com.example.spotly.MainActivity;
import com.example.spotly.R;

import android.Manifest;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.graphics.Color;
import android.widget.Toast;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.material.card.MaterialCardView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PetaFragment extends Fragment implements OnMapReadyCallback {
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FragmentPetaBinding binding;
    private GoogleMap mMap;
    private LatLng lastKnownUserLatLng = null;
    private boolean isFirstLoad = true;
    private LatLng originLatLng = null;
    private LatLng destinationLatLng = null;
    private Marker originMarker = null;
    private Marker destinationMarker = null;
    private Polyline currentPolyline;
    private DatabaseHelper databaseHelper;

    public PetaFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ThemeHelper.applyTheme(requireContext());
        binding = FragmentPetaBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        databaseHelper = new DatabaseHelper(requireContext());
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        updateIconMode();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        binding.modeIcon.setOnClickListener(v -> {
            String currentTheme = ThemeHelper.getCurrentTheme(requireContext());
            if ("dark".equals(currentTheme)) {
                ThemeHelper.setTheme(requireContext(), "light");
            } else {
                ThemeHelper.setTheme(requireContext(), "dark");
            }
            updateIconMode();
            applyMapStyle();
        });

        binding.search.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.trim().isEmpty()) {
                    searchLocation(query.trim());
                }
                binding.search.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        binding.tipeMap.setOnClickListener(v -> {
            View popupView = LayoutInflater.from(requireContext()).inflate(R.layout.tipe_map, null);
            final PopupWindow popupWindow = new PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
            );

            ImageView normalMap = popupView.findViewById(R.id.normalMap);
            ImageView sateliteMap = popupView.findViewById(R.id.sateliteMap);
            ImageView terrainMap = popupView.findViewById(R.id.terrainMap);
            ImageView hybridMap = popupView.findViewById(R.id.hybridMap);

            normalMap.setOnClickListener(v1 -> {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                popupWindow.dismiss();
            });

            sateliteMap.setOnClickListener(v1 -> {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                popupWindow.dismiss();
            });

            terrainMap.setOnClickListener(v1 -> {
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                popupWindow.dismiss();
            });

            hybridMap.setOnClickListener(v1 -> {
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                popupWindow.dismiss();
            });

            popupWindow.showAsDropDown(binding.tipeMap, 0, 0);
        });
        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        applyMapStyle();

        mMap.setOnMapLongClickListener(latLng -> {
            ((MainActivity) requireActivity()).hideBottomNav();
            binding.fokusUser.setVisibility(View.GONE);
            destinationLatLng = latLng;

            if (destinationMarker != null) destinationMarker.remove();

            destinationMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Destination"));

            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String namaTempat = address.getAdminArea();
                    if (namaTempat == null || namaTempat.isEmpty()) {
                        namaTempat = address.getLocality();
                    } else {
                        namaTempat = address.getSubLocality();
                    }

                    String alamat = address.getAddressLine(0);

                    binding.alamatSingkat.setText(namaTempat);
                    binding.markerAddress.setText(alamat);
                    binding.markerInfoPanel.setVisibility(View.VISIBLE);
                } else {
                    binding.markerAddress.setText("Alamat tidak ditemukan");
                    binding.markerInfoPanel.setVisibility(View.VISIBLE);
                }
            } catch (IOException e) {
                e.printStackTrace();
                binding.markerAddress.setText("Gagal mengambil alamat");
                binding.markerInfoPanel.setVisibility(View.VISIBLE);
            }
        });

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
            startActivity(intent);
        });

        binding.fokusUser.setOnClickListener(v -> {
            if (lastKnownUserLatLng != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastKnownUserLatLng, 15f));
            } else {
                moveCameraToUserLocation();
            }
        });

        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                mMap.setMyLocationEnabled(true);
                if (lastKnownUserLatLng != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownUserLatLng, 15f));
                    isFirstLoad = false;
                } else {
                    moveCameraToUserLocation();
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void moveCameraToUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        lastKnownUserLatLng = userLatLng;
                        originLatLng = userLatLng;

                        if (isFirstLoad) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f));
                            isFirstLoad = false;
                        } else {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    try {
                        mMap.setMyLocationEnabled(true);
                        moveCameraToUserLocation();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void updateIconMode() {
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
        if (mMap == null) return;

        String currentTheme = ThemeHelper.getCurrentTheme(requireContext());

        int styleResId = "dark".equals(currentTheme)
                ? R.raw.map_dark
                : R.raw.map_light;

        try {
            boolean success = mMap.setMapStyle(
                    com.google.android.gms.maps.model.MapStyleOptions.loadRawResourceStyle(requireContext(), styleResId)
            );

            if (!success) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSaveLocationDialog(List<DatabaseHelper.Folder> folders) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_save_location, null);
        builder.setView(dialogView);

        android.widget.Spinner folderSpinner = dialogView.findViewById(R.id.folderSpinner);
        android.widget.EditText titleInput = dialogView.findViewById(R.id.titleInput);

        List<String> folderNames = new ArrayList<>();
        for (DatabaseHelper.Folder folder : folders) {
            folderNames.add(folder.getNama_folder());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, folderNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        folderSpinner.setAdapter(adapter);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        ImageView closePanel = dialogView.findViewById(R.id.close_panel);
        closePanel.setOnClickListener(v -> dialog.dismiss());

        MaterialCardView buttonSave = dialogView.findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(v -> {
            int selectedFolderPosition = folderSpinner.getSelectedItemPosition();
            DatabaseHelper.Folder selectedFolder = folders.get(selectedFolderPosition);

            String title = titleInput.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Judul wajib diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseHelper.SavedLocation savedLocation = new DatabaseHelper.SavedLocation();
            savedLocation.setId_folder(selectedFolder.getId_folder());
            savedLocation.setJudul(title);
            savedLocation.setLat(destinationLatLng.latitude);
            savedLocation.setLng(destinationLatLng.longitude);
            savedLocation.setAlamat(binding.markerAddress.getText().toString());

            String tanggalSekarang = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            savedLocation.setTanggal(tanggalSekarang);

            long result = databaseHelper.insertSavedLocation(savedLocation);
            if (result != -1) {
                Toast.makeText(requireContext(), "Lokasi berhasil disimpan!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(requireContext(), "Gagal menyimpan lokasi.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchLocation(String locationName) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                if (destinationMarker != null) destinationMarker.remove();
                destinationLatLng = latLng;
                destinationMarker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Hasil Pencarian")
                        .snippet(address.getAddressLine(0)));

                String namaTempat = address.getAdminArea();
                if (namaTempat == null || namaTempat.isEmpty()) {
                    namaTempat = address.getLocality();
                } else {
                    namaTempat = address.getSubLocality();
                }

                binding.alamatSingkat.setText(namaTempat);
                binding.markerAddress.setText(address.getAddressLine(0));
                binding.markerInfoPanel.setVisibility(View.VISIBLE);
                binding.fokusUser.setVisibility(View.GONE);
                ((MainActivity) requireActivity()).hideBottomNav();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}