package com.example.spotly.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotly.AppExecutors;
import com.example.spotly.DatabaseHelper;
import com.example.spotly.R;
import com.example.spotly.adapter.FolderAdapter;

import java.util.ArrayList;
import java.util.List;

public class SimpanFragment extends Fragment {

    private RecyclerView recyclerView;
    private FolderAdapter folderAdapter;
    private DatabaseHelper databaseHelper;
    private ImageView btnAddFolder;
    private TextView emptyView; // Tambahkan ini
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_simpan, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewFolder);
        btnAddFolder = view.findViewById(R.id.btnAddFolder);
        progressBar = view.findViewById(R.id.progressBarSimpan);
        emptyView = view.findViewById(R.id.emptyViewSimpan); // Pastikan ID ini ada di layout
        databaseHelper = new DatabaseHelper(getContext());

        setupRecyclerView();
        loadFolders();

        btnAddFolder.setOnClickListener(v -> showAddFolderDialog());

        return view;
    }

    private void setupRecyclerView() {
        // Inisialisasi adapter dengan list kosong dulu
        folderAdapter = new FolderAdapter(new ArrayList<>(), getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(folderAdapter);
    }

    private void loadFolders() {
        // 1. Tampilkan loading, sembunyikan yang lain
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);

        // 2. Jalankan operasi database di background thread
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<DatabaseHelper.Folder> folderList = databaseHelper.getAllFolders();

            // 3. Update UI kembali di main thread setelah data didapat
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Sembunyikan loading
                    progressBar.setVisibility(View.GONE);

                    if (folderList.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        // Perbarui data di adapter
                        folderAdapter.setData(folderList);
                    }
                });
            }
        });
    }

    private void showAddFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Tambah Folder Baru");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Tambah", (dialog, which) -> {
            String folderName = input.getText().toString().trim();
            if (!folderName.isEmpty()) {
                // Jalankan operasi insert di background thread
                AppExecutors.getInstance().diskIO().execute(() -> {
                    databaseHelper.insertFolder(folderName);
                    // Muat ulang data folder untuk me-refresh tampilan (akan otomatis menampilkan loading)
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(this::loadFolders);
                    }
                });
            } else {
                Toast.makeText(getContext(), "Nama folder tidak boleh kosong", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}