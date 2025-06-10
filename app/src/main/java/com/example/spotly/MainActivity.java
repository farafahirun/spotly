package com.example.spotly;


import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.spotly.databinding.ActivityMainBinding;
import com.example.spotly.fragment.CariTempatFragment; // <-- IMPORT FRAGMENT BARU
import com.example.spotly.fragment.CeritaFragment;
import com.example.spotly.fragment.PetaFragment;
import com.example.spotly.fragment.SimpanFragment;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PetaFragment()) // Pastikan ID container benar
                    .commit();
        }

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int id = item.getItemId();
            if (id == R.id.peta) {
                selectedFragment = new PetaFragment();
            } else if (id == R.id.simpan) {
                selectedFragment = new SimpanFragment();
            } else if (id == R.id.cari) { // <-- TAMBAHKAN BLOK INI
                selectedFragment = new CariTempatFragment();
            }
            else if (id == R.id.cerita) {
                selectedFragment = new CeritaFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment) // Pastikan ID container Anda benar
                        .commit();
            }

            return true;
        });
    }

    public void hideBottomNav() {
        // Pastikan ID bottom_navigation sudah benar sesuai layout Anda
        findViewById(R.id.bottom_navigation).setVisibility(View.GONE);
    }

    public void showBottomNav() {
        findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
    }
}