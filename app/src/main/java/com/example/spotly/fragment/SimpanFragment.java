package com.example.spotly.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    @Override
    public void onResume() {
        super.onResume();
        loadFolders();
    }

    private void setupRecyclerView() {
        folderAdapter = new FolderAdapter(new ArrayList<>(), getContext(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(folderAdapter);
    }

    private void loadFolders() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyViewContainer.setVisibility(View.GONE);
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
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_folder_input, null);
        builder.setView(dialogView);

        final TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        final EditText input = dialogView.findViewById(R.id.input_folder_name);
        final Button btnBatal = dialogView.findViewById(R.id.btn_batal);
        final Button btnSimpan = dialogView.findViewById(R.id.btn_simpan);

        dialogTitle.setText("Tambah Folder Baru");
        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnSimpan.setOnClickListener(v -> {
            String folderName = input.getText().toString().trim();
            if (folderName.isEmpty()) {
                Toast.makeText(getContext(), "Nama folder tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }
            AppExecutors.getInstance().diskIO().execute(() -> {
                boolean exists = databaseHelper.folderExists(folderName);
                if (exists) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Nama folder '" + folderName + "' sudah ada.", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    databaseHelper.insertFolder(folderName);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            loadFolders();
                            dialog.dismiss();
                        });
                    }
                }
            });
        });
        btnBatal.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void onFolderDeleted() {
        if (folderAdapter.getItemCount() == 0) {
            emptyViewContainer.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }
}