package com.example.spotly.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.spotly.DatabaseHelper;
import com.example.spotly.R;

public class EditSavedLocationActivity extends AppCompatActivity {
    private EditText editTitle, editAddress, editNote;
    private Button buttonSave;
    private DatabaseHelper databaseHelper;
    private int savedLocationId;
    private DatabaseHelper.SavedLocation savedLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_saved_location);

        editTitle = findViewById(R.id.editTitle);
        editAddress = findViewById(R.id.editAddress);
        editNote = findViewById(R.id.editNote);
        buttonSave = findViewById(R.id.buttonSave);

        databaseHelper = new DatabaseHelper(this);
        savedLocationId = getIntent().getIntExtra("saved_location_id", -1);
        if (savedLocationId == -1) {
            Toast.makeText(this, "Data lokasi tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        savedLocation = databaseHelper.getLocationById(savedLocationId);
        if (savedLocation != null) {
            editTitle.setText(savedLocation.judul);
            editAddress.setText(savedLocation.alamat);
            editNote.setText(savedLocation.note);
        }

        buttonSave.setOnClickListener(v -> {
            String newTitle = editTitle.getText().toString().trim();
            String newAddress = editAddress.getText().toString().trim();
            String newNote = editNote.getText().toString().trim();

            editTitle.setText(savedLocation.judul);
            editAddress.setText(savedLocation.alamat);
            editNote.setText(savedLocation.note);

            databaseHelper.updateSavedLocation(
                    savedLocation.id_location,
                    savedLocation.judul,
                    savedLocation.tanggal,
                    savedLocation.lat,
                    savedLocation.lng,
                    savedLocation.alamat,
                    savedLocation.note
            );
        });
    }
}