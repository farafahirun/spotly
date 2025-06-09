package com.example.spotly.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.spotly.R;
import com.example.spotly.adapter.FolderAdapter;
import com.example.spotly.DatabaseHelper;
import com.example.spotly.DatabaseHelper.Folder;

import java.util.List;

public class SimpanFragment extends Fragment {

    private RecyclerView recyclerView;
    private FolderAdapter folderAdapter;
    private DatabaseHelper databaseHelper;
    private ImageView btnAddFolder;

    public SimpanFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_simpan, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewFolder);
        btnAddFolder = view.findViewById(R.id.btnAddFolder);
        databaseHelper = new DatabaseHelper(getContext());

        loadFolders();

        btnAddFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddFolderDialog();
            }
        });

        return view;
    }

    private void loadFolders() {
        List<Folder> folderList = databaseHelper.getAllFolders();
        folderAdapter = new FolderAdapter(folderList, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(folderAdapter);
    }

    private void showAddFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Tambah Folder");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Tambah", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String folderName = input.getText().toString().trim();
                if (!folderName.isEmpty()) {
                    databaseHelper.insertFolder(folderName);
                    loadFolders();
                }
            }
        });

        builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}