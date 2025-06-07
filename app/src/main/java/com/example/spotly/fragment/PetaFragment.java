package com.example.spotly.fragment;

import android.content.pm.PackageManager;
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
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.example.spotly.R;

import android.Manifest;

import com.example.spotly.ThemeHelper;
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class PetaFragment extends Fragment implements OnMapReadyCallback {
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FragmentPetaBinding binding;
    private GoogleMap mMap;
    private ImageView imgToggleTheme;

    public PetaFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ThemeHelper.applyTheme(requireContext());
        binding = FragmentPetaBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        imgToggleTheme = binding.getRoot().findViewById(R.id.imgToggleTheme);
        updateImageTheme();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        imgToggleTheme.setOnClickListener(v -> {
            String currentTheme = ThemeHelper.getCurrentTheme(requireContext());
            if ("dark".equals(currentTheme)) {
                ThemeHelper.setTheme(requireContext(), "light");
            } else {
                ThemeHelper.setTheme(requireContext(), "dark");
            }
            requireActivity().recreate();
        });

        binding.btnMapType.setOnClickListener(v -> {
            View popupView = LayoutInflater.from(requireContext()).inflate(R.layout.tipe_map, null);
            final PopupWindow popupWindow = new PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
            );

            ImageView btnNormal = popupView.findViewById(R.id.btnMapNormal);
            ImageView btnSatellite = popupView.findViewById(R.id.btnMapSatellite);
            ImageView btnTerrain = popupView.findViewById(R.id.btnMapTerrain);
            ImageView btnHybrid = popupView.findViewById(R.id.btnMapHybrid);

            btnNormal.setOnClickListener(v1 -> {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                popupWindow.dismiss();
            });

            btnSatellite.setOnClickListener(v1 -> {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                popupWindow.dismiss();
            });

            btnTerrain.setOnClickListener(v1 -> {
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                popupWindow.dismiss();
            });

            btnHybrid.setOnClickListener(v1 -> {
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                popupWindow.dismiss();
            });

            popupWindow.showAsDropDown(binding.btnMapType, 0, 0);
        });
        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

//        LatLng jakarta = new LatLng(-6.200000, 106.816666);
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(jakarta, 10f));

        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Lokasi"));

            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String alamat = address.getAddressLine(0);
                    marker.setSnippet(alamat);
                    marker.showInfoWindow();
                } else {
                    marker.setSnippet("Alamat tidak ditemukan");
                    marker.showInfoWindow();
                }
            } catch (IOException e) {
                e.printStackTrace();
                marker.setSnippet("Gagal mengambil alamat");
                marker.showInfoWindow();
            }
        });

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                mMap.setMyLocationEnabled(true);
                moveCameraToUserLocation();
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
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f));
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

    private void updateImageTheme() {
        String currentTheme = ThemeHelper.getCurrentTheme(requireContext());

        if ("dark".equals(currentTheme)) {
            imgToggleTheme.setImageResource(R.drawable.light_icon);
        } else {
            imgToggleTheme.setImageResource(R.drawable.dark_icon);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}