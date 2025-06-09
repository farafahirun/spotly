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

import com.example.spotly.MainActivity;
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

import android.graphics.Color;
import android.widget.Toast;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;
import java.util.ArrayList;
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
            binding.route.setVisibility(View.GONE);
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
            binding.route.setVisibility(View.VISIBLE);
            ((MainActivity) requireActivity()).showBottomNav();
        });

        binding.buttonSimpan.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Disimpan ke daftar Simpan!", Toast.LENGTH_SHORT).show();
        });

        binding.buttonCerita.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Ditambahkan ke Cerita!", Toast.LENGTH_SHORT).show();
        });

        binding.buttonRute.setOnClickListener(v -> {
            if (originLatLng != null && destinationLatLng != null) {
                getRoute(originLatLng, destinationLatLng);
            } else {
                Toast.makeText(requireContext(), "Pilih tujuan terlebih dahulu.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.route.setOnClickListener(v -> {
            if (originLatLng != null && destinationLatLng != null) {
                getRoute(originLatLng, destinationLatLng);
            }
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

    private void updateIconMode() {
        String currentTheme = ThemeHelper.getCurrentTheme(requireContext());
        if ("dark".equals(currentTheme)) {
            binding.modeIcon.setImageResource(R.drawable.light_icon);
            binding.markerAddress.setTextColor(Color.WHITE);
            binding.alamatSingkat.setTextColor(Color.WHITE);
//            binding.layerIcon.setImageResource(R.drawable.layer_icon);
        } else {
            binding.modeIcon.setImageResource(R.drawable.dark_icon);
            binding.markerAddress.setTextColor(Color.BLACK);
            binding.alamatSingkat.setTextColor(Color.BLACK);
//            binding.layerIcon.setImageResource(R.drawable.layerr_icon);
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

    private void getRoute(LatLng origin, LatLng destination) {
        String originParam = origin.latitude + "," + origin.longitude;
        String destinationParam = destination.latitude + "," + destination.longitude;

        String apiKey = getString(R.string.api_key); // Ambil dari string.xml
        String urlStr = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + originParam +
                "&destination=" + destinationParam +
                "&key=" + apiKey;

        new Thread(() -> {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "HTTP ERROR: " + responseCode, Toast.LENGTH_SHORT).show());
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                android.util.Log.d("DirectionsAPI", jsonResponse.toString(2));
                String status = jsonResponse.getString("status");

                if (!"OK".equals(status)) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Directions API Error: " + status, Toast.LENGTH_LONG).show());
                    return;
                }

                JSONArray routes = jsonResponse.getJSONArray("routes");

                if (routes.length() == 0) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Tidak ada route ditemukan.", Toast.LENGTH_LONG).show());
                    return;
                }

                JSONObject route = routes.getJSONObject(0);
                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                String points = overviewPolyline.getString("points");

                List<LatLng> decodedPath = decodePoly(points);

                requireActivity().runOnUiThread(() -> {
                    if (currentPolyline != null) currentPolyline.remove();

                    currentPolyline = mMap.addPolyline(new PolylineOptions()
                            .addAll(decodedPath)
                            .width(12f)
                            .color(Color.BLUE)
                            .geodesic(true));

                    if (originMarker != null) originMarker.remove();
                    originMarker = mMap.addMarker(new MarkerOptions()
                            .position(originLatLng)
                            .title("Your Location (Origin)"));

                    Toast.makeText(requireContext(), "Route berhasil digambar!", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Exception: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng(((double) lat / 1E5), ((double) lng / 1E5));
            poly.add(p);
        }
        return poly;
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
                binding.route.setVisibility(View.GONE);
                ((MainActivity) requireActivity()).hideBottomNav();
                binding.route.setEnabled(true);
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