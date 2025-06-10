package com.example.spotly.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotly.DatabaseHelper;
import com.example.spotly.R;
import com.example.spotly.activity.DetailCeritaActivity;
import com.example.spotly.adapter.CeritaAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CeritaFragment extends Fragment {

    private RecyclerView recyclerView;
    private CeritaAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<DatabaseHelper.Cerita> allCeritaList;
    private TextView emptyView;
    private SearchView searchView;
    private Spinner filterSpinner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cerita, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewCerita);
        emptyView = view.findViewById(R.id.emptyViewCerita);
        searchView = view.findViewById(R.id.searchViewCerita);
        filterSpinner = view.findViewById(R.id.filterSpinnerCerita);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dbHelper = new DatabaseHelper(getContext());
        allCeritaList = new ArrayList<>();

        setupFilterSpinner();
        setupSearchView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCerita();
    }

    private void loadCerita() {
        allCeritaList = dbHelper.getAllCerita();

        if (allCeritaList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);

            // Inisialisasi adapter dengan semua data
            adapter = new CeritaAdapter(new ArrayList<>(allCeritaList), getContext(), cerita -> {
                Intent intent = new Intent(getContext(), DetailCeritaActivity.class);
                intent.putExtra("cerita_id", cerita.getId_cerita()); // Kirim ID untuk detail
                startActivity(intent);
            });
            recyclerView.setAdapter(adapter);

            // Terapkan filter yang mungkin sudah ada
            filterCerita();
        }
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterCerita();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                filterCerita();
                return true;
            }
        });
    }

    private void setupFilterSpinner() {
        // Daftar filter termasuk "Semua"
        String[] icons = {"Semua Ikon", "😊 Senang", "😢 Sedih", "😠 Marah", "😲 Terkejut", "❤️ Suka", "🤔 Bingung"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, icons);
        filterSpinner.setAdapter(spinnerAdapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterCerita();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void filterCerita() {
        if (allCeritaList == null || adapter == null) return;

        String query = searchView.getQuery().toString().toLowerCase().trim();
        String selectedIcon = filterSpinner.getSelectedItem().toString();

        List<DatabaseHelper.Cerita> filteredList = new ArrayList<>();
        for(DatabaseHelper.Cerita cerita : allCeritaList) {
            boolean matchesQuery = query.isEmpty() ||
                    cerita.getJudul().toLowerCase().contains(query) ||
                    cerita.getKategori().toLowerCase().contains(query);

            boolean matchesIcon = "Semua Ikon".equals(selectedIcon) ||
                    cerita.getIcon_perasaan().equals(selectedIcon);

            if(matchesQuery && matchesIcon) {
                filteredList.add(cerita);
            }
        }

        adapter.updateList(filteredList);
    }
}