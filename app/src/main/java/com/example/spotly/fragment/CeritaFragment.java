package com.example.spotly.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotly.AppExecutors;
import com.example.spotly.DatabaseHelper;
import com.example.spotly.R;
import com.example.spotly.activity.DetailCeritaActivity;
import com.example.spotly.adapter.CeritaAdapter;

import java.util.ArrayList;
import java.util.List;

public class CeritaFragment extends Fragment {

    private RecyclerView recyclerView;
    private CeritaAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<DatabaseHelper.Cerita> allCeritaList;
    private TextView emptyView;
    private SearchView searchView;

    // Komponen UI baru
    private LinearLayout emojiContainer;
    private ProgressBar progressBar;

    // State untuk filter
    private String selectedEmojiFilter = "Semua";
    private List<Button> emojiButtons = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cerita, container, false);

        // Inisialisasi View
        recyclerView = view.findViewById(R.id.recyclerViewCerita);
        emptyView = view.findViewById(R.id.emptyViewCerita);
        searchView = view.findViewById(R.id.searchViewCerita);
        emojiContainer = view.findViewById(R.id.emoji_filter_container);
        progressBar = view.findViewById(R.id.progressBarCerita);

        dbHelper = new DatabaseHelper(getContext());
        allCeritaList = new ArrayList<>();

        setupRecyclerView();
        setupEmojiFilterButtons();
        setupSearchView();

        return view;
    }

    // Panggil loadCerita setiap kali fragment ditampilkan kembali
    @Override
    public void onResume() {
        super.onResume();
        loadCerita();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Inisialisasi adapter dengan list kosong, listener di-pass ke adapter
        adapter = new CeritaAdapter(new ArrayList<>(), getContext(), cerita -> {
            Intent intent = new Intent(getContext(), DetailCeritaActivity.class);
            intent.putExtra("cerita_id", cerita.getId_cerita());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupEmojiFilterButtons() {
        // Daftar emoji yang akan menjadi tombol filter
        String[] emojis = {"Semua", "😊", "😢", "😠", "😲", "❤️", "🤔"};
        emojiContainer.removeAllViews();
        emojiButtons.clear();

        for (String emoji : emojis) {
            Button button = new Button(getContext(), null, android.R.attr.buttonStyleSmall);
            button.setText(emoji);
            button.setBackgroundResource(R.drawable.emoji_button_background);
            button.setTextColor(ContextCompat.getColorStateList(getContext(), R.color.emoji_button_text_color));
            button.setPadding(40, 0, 40, 0); // Padding horizontal
            button.setAllCaps(false);

            button.setOnClickListener(v -> {
                selectedEmojiFilter = emoji;
                updateButtonSelection();
                filterCerita();
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMarginEnd(16); // Jarak antar tombol
            button.setLayoutParams(params);

            emojiButtons.add(button);
            emojiContainer.addView(button);
        }
        updateButtonSelection(); // Set tombol "Semua" sebagai default terpilih
    }

    private void updateButtonSelection() {
        for (Button btn : emojiButtons) {
            // Set status 'selected' berdasarkan filter yang aktif
            btn.setSelected(btn.getText().toString().equals(selectedEmojiFilter));
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

    private void loadCerita() {
        // Tampilkan ProgressBar saat data dimuat
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);

        // Ambil data dari database di background thread
        AppExecutors.getInstance().diskIO().execute(() -> {
            allCeritaList = dbHelper.getAllCerita();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Sembunyikan ProgressBar setelah selesai
                    progressBar.setVisibility(View.GONE);
                    if (allCeritaList.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        // Terapkan filter ke data yang baru dimuat
                        filterCerita();
                    }
                });
            }
        });
    }

    private void filterCerita() {
        if (allCeritaList == null || adapter == null) return;

        String query = searchView.getQuery().toString().toLowerCase().trim();
        // Ambil emoji pertama dari filter (misal: "😊" dari "😊 Senang")
        String selectedFeeling = selectedEmojiFilter;

        List<DatabaseHelper.Cerita> filteredList = new ArrayList<>();
        for (DatabaseHelper.Cerita cerita : allCeritaList) {
            boolean matchesQuery = query.isEmpty() ||
                    cerita.getJudul().toLowerCase().contains(query) ||
                    cerita.getKategori().toLowerCase().contains(query);

            boolean matchesIcon = selectedFeeling.equals("Semua") ||
                    cerita.getIcon_perasaan().startsWith(selectedFeeling);

            if (matchesQuery && matchesIcon) {
                filteredList.add(cerita);
            }
        }

        // Cek kembali setelah filter, mungkin hasilnya jadi kosong
        if(filteredList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText("Tidak ada cerita yang cocok.");
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        adapter.updateList(filteredList);
    }
}