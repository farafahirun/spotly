package com.example.spotly.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.spotly.DatabaseHelper;
import com.example.spotly.R;
import com.example.spotly.activity.DetailCeritaActivity;
import com.example.spotly.adapter.CeritaAdapter;

import java.util.List;

public class CeritaFragment extends Fragment {

    private RecyclerView recyclerView;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cerita, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewCerita);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        dbHelper = new DatabaseHelper(getContext());
        loadCerita();

        return view;
    }

    private void loadCerita() {
        List<DatabaseHelper.Cerita> ceritaList = dbHelper.getAllCerita();

        CeritaAdapter adapter = new CeritaAdapter(ceritaList, cerita -> {
            Intent intent = new Intent(getContext(), DetailCeritaActivity.class);
            intent.putExtra("judul", cerita.getJudul());
            intent.putExtra("kategori", cerita.getKategori());
            intent.putExtra("icon_perasaan", cerita.getIcon_perasaan());
            intent.putExtra("lokasi", cerita.getAlamat());
            intent.putExtra("isi", cerita.getIsi());
            intent.putExtra("tanggal", cerita.getTanggal());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }
}