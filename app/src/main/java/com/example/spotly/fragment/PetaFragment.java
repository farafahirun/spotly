package com.example.spotly.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.spotly.R;
import com.example.spotly.ThemeHelper;
import com.example.spotly.databinding.FragmentPetaBinding;

public class PetaFragment extends Fragment {

    private FragmentPetaBinding binding;

    public PetaFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ThemeHelper.applyTheme(requireContext());
        binding = FragmentPetaBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Set gambar sesuai tema saat ini
        updateImageTheme();

        // Aksi klik ImageView
        binding.imgToggleTheme.setOnClickListener(v -> {
            String currentTheme = ThemeHelper.getCurrentTheme(requireContext());

            if ("dark".equals(currentTheme)) {
                ThemeHelper.setTheme(requireContext(), "light");
            } else {
                ThemeHelper.setTheme(requireContext(), "dark");
            }

            requireActivity().recreate(); // reload Activity + Fragment
        });

        return view;
    }

    private void updateImageTheme() {
        String currentTheme = ThemeHelper.getCurrentTheme(requireContext());

        if ("dark".equals(currentTheme)) {
            binding.imgToggleTheme.setImageResource(R.drawable.light_icon);
        } else {
            binding.imgToggleTheme.setImageResource(R.drawable.dark_icon);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
