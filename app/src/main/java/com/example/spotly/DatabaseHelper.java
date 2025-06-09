package com.example.spotly;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "spotly.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createFolderTable = "CREATE TABLE Folder (" +
                "id_folder INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nama_folder TEXT NOT NULL" +
                ")";

        String createSavedLocationTable = "CREATE TABLE SavedLocation (" +
                "id_location INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_folder INTEGER, " +
                "judul TEXT NOT NULL, " +
                "tanggal TEXT NOT NULL, " +
                "lat REAL NOT NULL, " +
                "lng REAL NOT NULL, " +
                "alamat TEXT NOT NULL, " +
                "note TEXT, " +
                "FOREIGN KEY(id_folder) REFERENCES Folder(id_folder) ON DELETE CASCADE" +
                ")";

        db.execSQL(createFolderTable);
        db.execSQL(createSavedLocationTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS SavedLocation");
        db.execSQL("DROP TABLE IF EXISTS Folder");
        onCreate(db);
    }

    //    Folder operations
    public long insertFolder(String nama_folder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nama_folder", nama_folder);
        long result = db.insert("Folder", null, values);
        db.close();
        return result;
    }

    public List<Folder> getAllFolders() {
        List<Folder> folderList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Folder", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_folder"));
                String nama = cursor.getString(cursor.getColumnIndexOrThrow("nama_folder"));
                folderList.add(new Folder(id, nama));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return folderList;
    }

    public boolean updateFolder(int id, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nama_folder", newName);
        int rows = db.update("Folder", values, "id_folder = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }

    public void deleteFolder(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Folder", "id_folder = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public int getFileCountInFolder(int folderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM SavedLocation WHERE id_folder = ?", new String[]{String.valueOf(folderId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    public long insertSavedLocation(int id_folder, String judul, String tanggal, double lat, double lng, String alamat, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_folder", id_folder);
        values.put("judul", judul);
        values.put("tanggal", tanggal);
        values.put("lat", lat);
        values.put("lng", lng);
        values.put("alamat", alamat);
        values.put("note", note);

        long result = db.insert("SavedLocation", null, values);
        db.close();
        return result;
    }

    public long insertSavedLocation(SavedLocation savedLocation) {
        return insertSavedLocation(
                savedLocation.getId_folder(),
                savedLocation.getJudul(),
                savedLocation.getTanggal(),
                savedLocation.getLat(),
                savedLocation.getLng(),
                savedLocation.getAlamat(),
                savedLocation.getNote()
        );
    }

    public List<SavedLocation> getLocationsByFolderId(int id_folder) {
        List<SavedLocation> locationList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM SavedLocation WHERE id_folder = ?", new String[]{String.valueOf(id_folder)});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_location"));
                String judul = cursor.getString(cursor.getColumnIndexOrThrow("judul"));
                String tanggal = cursor.getString(cursor.getColumnIndexOrThrow("tanggal"));
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow("lat"));
                double lng = cursor.getDouble(cursor.getColumnIndexOrThrow("lng"));
                String alamat = cursor.getString(cursor.getColumnIndexOrThrow("alamat"));
                String note = cursor.getString(cursor.getColumnIndexOrThrow("note"));

                locationList.add(new SavedLocation(id, id_folder, judul, tanggal, lat, lng, alamat, note));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return locationList;
    }

    public SavedLocation getLocationById(int id_location) {
        SavedLocation location = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM SavedLocation WHERE id_location = ?", new String[]{String.valueOf(id_location)});

        if (cursor.moveToFirst()) {
            int id_folder = cursor.getInt(cursor.getColumnIndexOrThrow("id_folder"));
            String judul = cursor.getString(cursor.getColumnIndexOrThrow("judul"));
            String tanggal = cursor.getString(cursor.getColumnIndexOrThrow("tanggal"));
            double lat = cursor.getDouble(cursor.getColumnIndexOrThrow("lat"));
            double lng = cursor.getDouble(cursor.getColumnIndexOrThrow("lng"));
            String alamat = cursor.getString(cursor.getColumnIndexOrThrow("alamat"));
            String note = cursor.getString(cursor.getColumnIndexOrThrow("note"));

            location = new SavedLocation(id_location, id_folder, judul, tanggal, lat, lng, alamat, note);
        }
        cursor.close();
        db.close();
        return location;
    }

    public boolean updateSavedLocation(int id_location, String judul, String tanggal, double lat, double lng, String alamat, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("judul", judul);
        values.put("tanggal", tanggal);
        values.put("lat", lat);
        values.put("lng", lng);
        values.put("alamat", alamat);
        values.put("note", note);

        int result = db.update("SavedLocation", values, "id_location = ?", new String[]{String.valueOf(id_location)});
        db.close();
        return result > 0;
    }

    public boolean deleteSavedLocation(int id_location) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("SavedLocation", "id_location = ?", new String[]{String.valueOf(id_location)});
        db.close();
        return result > 0;
    }

    //    Data model classes
    public static class Folder {
        public int id_folder;
        public String nama_folder;

        public Folder(int id_folder, String nama_folder) {
            this.id_folder = id_folder;
            this.nama_folder = nama_folder;
        }

        public int getId_folder() {
            return id_folder;
        }

        public String getNama_folder() {
            return nama_folder;
        }
    }

    public static class SavedLocation {
        public int id_location;
        public int id_folder;
        public String judul;
        public String tanggal;
        public double lat;
        public double lng;
        public String alamat;
        public String note;

        public SavedLocation() {
        }

        public SavedLocation(int id_location, int id_folder, String judul, String tanggal, double lat, double lng, String alamat, String note) {
            this.id_location = id_location;
            this.id_folder = id_folder;
            this.judul = judul;
            this.tanggal = tanggal;
            this.lat = lat;
            this.lng = lng;
            this.alamat = alamat;
            this.note = note;
        }

        public int getId_location() {
            return id_location;
        }

        public void setId_location(int id_location) {
            this.id_location = id_location;
        }

        public int getId_folder() {
            return id_folder;
        }

        public void setId_folder(int id_folder) {
            this.id_folder = id_folder;
        }

        public String getJudul() {
            return judul;
        }

        public void setJudul(String judul) {
            this.judul = judul;
        }

        public String getTanggal() {
            return tanggal;
        }

        public void setTanggal(String tanggal) {
            this.tanggal = tanggal;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLng() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }

        public String getAlamat() {
            return alamat;
        }

        public void setAlamat(String alamat) {
            this.alamat = alamat;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }
}