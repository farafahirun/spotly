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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotly.AppExecutors;
import com.example.spotly.DatabaseHelper;
import com.example.spotly.R;
import com.example.spotly.adapter.FolderAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * SimpanFragment sekarang mengimplementasikan FolderAdapterListener
 * untuk menerima event dari adapter.
 */
public class SimpanFragment extends Fragment implements FolderAdapter.FolderAdapterListener {

    private RecyclerView recyclerView;
    private FolderAdapter folderAdapter;
    private DatabaseHelper databaseHelper;
    private ImageView btnAddFolder;
    private View emptyViewContainer;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_simpan, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewFolder);
        btnAddFolder = view.findViewById(R.id.btnAddFolder);
        progressBar = view.findViewById(R.id.progressBarSimpan);
        emptyViewContainer = view.findViewById(R.id.emptyViewSimpan);
        databaseHelper = new DatabaseHelper(getContext());

        setupRecyclerView();

        btnAddFolder.setOnClickListener(v -> showAddFolderDialog());

        return view;
    }

    /**
     * onResume akan selalu dipanggil saat fragment kembali ditampilkan,
     * memastikan data selalu yang terbaru.
     */
    @Override
    public void onResume() {
        super.onResume();
        loadFolders();
    }

    private void setupRecyclerView() {
        // Saat membuat adapter, kita passing 'this' sebagai listener.
        // Ini menghubungkan fragment ini dengan adapter.
        folderAdapter = new FolderAdapter(new ArrayList<>(), getContext(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(folderAdapter);
    }

    private void loadFolders() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyViewContainer.setVisibility(View.GONE);

        // Jalankan operasi database di background thread
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<DatabaseHelper.Folder> folderList = databaseHelper.getAllFolders();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (folderList.isEmpty()) {
                        emptyViewContainer.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyViewContainer.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
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
                AppExecutors.getInstance().diskIO().execute(() -> {
                    databaseHelper.insertFolder(folderName);
                    if (getActivity() != null) {
                        // Memanggil loadFolders akan otomatis merefresh list dan menampilkan loading
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

    /**
     * Ini adalah implementasi metode dari interface FolderAdapterListener.
     * Metode ini akan dipanggil oleh adapter setelah sebuah folder berhasil dihapus.
     */
    @Override
    public void onFolderDeleted() {
        // Cek apakah adapter sekarang kosong setelah item dihapus
        if (folderAdapter.getItemCount() == 0) {
            emptyViewContainer.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }
}