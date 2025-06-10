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
    private SearchView searchView;
    private ProgressBar progressBar;
    private View emptyViewContainer;
    private TextView emptyViewTitle, emptyViewSubtitle;
    private LinearLayout emojiContainer;
    private String selectedEmojiFilter = "Semua";
    private List<Button> emojiButtons = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cerita, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewCerita);
        searchView = view.findViewById(R.id.searchViewCerita);
        emojiContainer = view.findViewById(R.id.emoji_filter_container);
        progressBar = view.findViewById(R.id.progressBarCerita);
        emptyViewContainer = view.findViewById(R.id.emptyViewCerita);
        emptyViewTitle = view.findViewById(R.id.emptyViewCerita_title);
        emptyViewSubtitle = view.findViewById(R.id.emptyViewCerita_subtitle);
        dbHelper = new DatabaseHelper(getContext());
        allCeritaList = new ArrayList<>();

        setupRecyclerView();
        setupEmojiFilterButtons();
        setupSearchView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCerita();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CeritaAdapter(new ArrayList<>(), getContext(), cerita -> {
            Intent intent = new Intent(getContext(), DetailCeritaActivity.class);
            intent.putExtra("cerita_id", cerita.getId_cerita());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadCerita() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyViewContainer.setVisibility(View.GONE);

        AppExecutors.getInstance().diskIO().execute(() -> {
            allCeritaList = dbHelper.getAllCerita();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    filterCerita();
                });
            }
        });
    }

    private void filterCerita() {
        if (allCeritaList == null || adapter == null) return;

        String query = searchView.getQuery().toString().toLowerCase().trim();
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

        if (filteredList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyViewContainer.setVisibility(View.VISIBLE);

            if (allCeritaList.isEmpty()) {
                emptyViewTitle.setText("Buat Cerita Pertamamu");
                emptyViewSubtitle.setText("Setiap tempat punya kenangan. Tulis ceritamu sekarang!");
            } else {
                emptyViewTitle.setText("Tidak Ada Hasil");
                emptyViewSubtitle.setText("Coba kata kunci atau filter perasaan yang lain.");
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyViewContainer.setVisibility(View.GONE);
        }

        adapter.updateList(filteredList);
    }

    private void setupEmojiFilterButtons() {
        String[] emojis = {"Semua", "😊", "😢", "😠", "😲", "❤️", "🤔"};
        emojiContainer.removeAllViews();
        emojiButtons.clear();

        for (String emoji : emojis) {
            Button button = new Button(getContext(), null, android.R.attr.buttonStyleSmall);
            button.setText(emoji);
            button.setBackgroundResource(R.drawable.emoji_button_background);
            button.setTextColor(ContextCompat.getColorStateList(getContext(), R.color.emoji_button_text_color));
            button.setPadding(40, 0, 40, 0);
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
            params.setMarginEnd(16);
            button.setLayoutParams(params);

            emojiButtons.add(button);
            emojiContainer.addView(button);
        }
        updateButtonSelection();
    }

    private void updateButtonSelection() {
        for (Button btn : emojiButtons) {
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
}