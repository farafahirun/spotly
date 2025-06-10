package com.example.spotly;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "spotly.db";
    // Versi database dinaikkan untuk menerapkan perubahan skema (tambah kolom)
    private static final int DATABASE_VERSION = 6;

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
                "tanggal_create TEXT NOT NULL, " +
                "tanggal_update TEXT, " +
                "lat REAL NOT NULL, " +
                "lng REAL NOT NULL, " +
                "alamat TEXT NOT NULL, " +
                "FOREIGN KEY(id_folder) REFERENCES Folder(id_folder) ON DELETE CASCADE" +
                ")";

        String createCeritaTable = "CREATE TABLE Cerita (" +
                "id_cerita INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "kategori TEXT NOT NULL, " +
                "icon_perasaan TEXT NOT NULL, " +
                "judul TEXT NOT NULL, " +
                "alamat TEXT NOT NULL, " +
                "lat REAL, " +
                "lng REAL, " +
                "isi TEXT NOT NULL, " +
                "tanggal TEXT NOT NULL, " +
                "has_map_view INTEGER DEFAULT 1 NOT NULL" + // Kolom baru untuk penanda peta
                ")";

        db.execSQL(createFolderTable);
        db.execSQL(createSavedLocationTable);
        db.execSQL(createCeritaTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Untuk development, cara termudah adalah drop dan create ulang.
        // Data lama akan hilang.
        db.execSQL("DROP TABLE IF EXISTS SavedLocation");
        db.execSQL("DROP TABLE IF EXISTS Folder");
        db.execSQL("DROP TABLE IF EXISTS Cerita");
        onCreate(db);
    }

    // ================== FOLDER OPERATIONS ==================
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
        Cursor cursor = db.rawQuery("SELECT * FROM Folder ORDER BY nama_folder ASC", null);
        if (cursor.moveToFirst()) {
            do {
                folderList.add(new Folder(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_folder")),
                        cursor.getString(cursor.getColumnIndexOrThrow("nama_folder"))
                ));
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


    // ================== SAVED LOCATION OPERATIONS ==================
    public long insertSavedLocation(SavedLocation savedLocation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_folder", savedLocation.getId_folder());
        values.put("judul", savedLocation.getJudul());
        values.put("tanggal_create", savedLocation.getTanggalCreate());
        values.put("lat", savedLocation.getLat());
        values.put("lng", savedLocation.getLng());
        values.put("alamat", savedLocation.getAlamat());
        long result = db.insert("SavedLocation", null, values);
        db.close();
        return result;
    }

    public List<SavedLocation> getLocationsByFolderId(int id_folder) {
        List<SavedLocation> locationList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM SavedLocation WHERE id_folder = ? ORDER BY id_location DESC", new String[]{String.valueOf(id_folder)});
        if (cursor.moveToFirst()) {
            do {
                locationList.add(mapCursorToSavedLocation(cursor));
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
            location = mapCursorToSavedLocation(cursor);
        }
        cursor.close();
        db.close();
        return location;
    }

    public boolean updateSavedLocation(int id_location, String judul, double lat, double lng, String alamat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("judul", judul);
        values.put("lat", lat);
        values.put("lng", lng);
        values.put("alamat", alamat);
        values.put("tanggal_update", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
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

    private SavedLocation mapCursorToSavedLocation(Cursor cursor) {
        return new SavedLocation(
                cursor.getInt(cursor.getColumnIndexOrThrow("id_location")),
                cursor.getInt(cursor.getColumnIndexOrThrow("id_folder")),
                cursor.getString(cursor.getColumnIndexOrThrow("judul")),
                cursor.getString(cursor.getColumnIndexOrThrow("tanggal_create")),
                cursor.getString(cursor.getColumnIndexOrThrow("tanggal_update")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("lat")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("lng")),
                cursor.getString(cursor.getColumnIndexOrThrow("alamat"))
        );
    }

    // ================== CERITA OPERATIONS ==================
    public long insertCerita(Cerita cerita) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("kategori", cerita.getKategori());
        values.put("icon_perasaan", cerita.getIcon_perasaan());
        values.put("judul", cerita.getJudul());
        values.put("lat", cerita.getLat());
        values.put("lng", cerita.getLng());
        values.put("alamat", cerita.getAlamat());
        values.put("isi", cerita.getIsi());
        values.put("tanggal", cerita.getTanggal());
        values.put("has_map_view", cerita.hasMapView() ? 1 : 0); // Simpan flag sebagai integer (1=true, 0=false)
        long result = db.insert("Cerita", null, values);
        db.close();
        return result;
    }

    public boolean updateCerita(int id_cerita, String kategori, String iconPerasaan, String judul, String isi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("kategori", kategori);
        values.put("icon_perasaan", iconPerasaan);
        values.put("judul", judul);
        values.put("isi", isi);
        int result = db.update("Cerita", values, "id_cerita = ?", new String[]{String.valueOf(id_cerita)});
        db.close();
        return result > 0;
    }

    public List<Cerita> getAllCerita() {
        List<Cerita> ceritaList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Cerita ORDER BY id_cerita DESC", null);
        if (cursor.moveToFirst()) {
            do {
                ceritaList.add(mapCursorToCerita(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return ceritaList;
    }

    public Cerita getCeritaById(int id_cerita) {
        Cerita cerita = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Cerita WHERE id_cerita = ?", new String[]{String.valueOf(id_cerita)});
        if (cursor.moveToFirst()) {
            cerita = mapCursorToCerita(cursor);
        }
        cursor.close();
        db.close();
        return cerita;
    }

    public boolean deleteCerita(int id_cerita) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("Cerita", "id_cerita = ?", new String[]{String.valueOf(id_cerita)});
        db.close();
        return result > 0;
    }

    private Cerita mapCursorToCerita(Cursor cursor) {
        return new Cerita(
                cursor.getInt(cursor.getColumnIndexOrThrow("id_cerita")),
                cursor.getString(cursor.getColumnIndexOrThrow("kategori")),
                cursor.getString(cursor.getColumnIndexOrThrow("icon_perasaan")),
                cursor.getString(cursor.getColumnIndexOrThrow("judul")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("lat")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("lng")),
                cursor.getString(cursor.getColumnIndexOrThrow("alamat")),
                cursor.getString(cursor.getColumnIndexOrThrow("isi")),
                cursor.getString(cursor.getColumnIndexOrThrow("tanggal")),
                cursor.getInt(cursor.getColumnIndexOrThrow("has_map_view")) == 1 // Konversi integer ke boolean
        );
    }

    // ================== DATA MODEL CLASSES ==================
    public static class Folder {
        public int id_folder;
        public String nama_folder;
        public Folder(int id_folder, String nama_folder) {
            this.id_folder = id_folder;
            this.nama_folder = nama_folder;
        }
        public int getId_folder() { return id_folder; }
        public String getNama_folder() { return nama_folder; }
    }

    public static class SavedLocation {
        private int id_location;
        private int id_folder;
        private String judul;
        private String tanggalCreate;
        private String tanggalUpdate;
        private double lat;
        private double lng;
        private String alamat;

        public SavedLocation() {}
        public SavedLocation(int id_location, int id_folder, String judul, String tanggalCreate, String tanggalUpdate, double lat, double lng, String alamat) {
            this.id_location = id_location;
            this.id_folder = id_folder;
            this.judul = judul;
            this.tanggalCreate = tanggalCreate;
            this.tanggalUpdate = tanggalUpdate;
            this.lat = lat;
            this.lng = lng;
            this.alamat = alamat;
        }
        public int getId_location() { return id_location; }
        public void setId_location(int id_location) { this.id_location = id_location; }
        public int getId_folder() { return id_folder; }
        public void setId_folder(int id_folder) { this.id_folder = id_folder; }
        public String getJudul() { return judul; }
        public void setJudul(String judul) { this.judul = judul; }
        public String getTanggalCreate() { return tanggalCreate; }
        public void setTanggalCreate(String tanggal) { this.tanggalCreate = tanggal; }
        public String getTanggalUpdate() { return tanggalUpdate; }
        public void setTanggalUpdate(String tanggalUpdate) { this.tanggalUpdate = tanggalUpdate; }
        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }
        public double getLng() { return lng; }
        public void setLng(double lng) { this.lng = lng; }
        public String getAlamat() { return alamat; }
        public void setAlamat(String alamat) { this.alamat = alamat; }
    }

    public static class Cerita {
        private int id_cerita;
        private String kategori;
        private String icon_perasaan;
        private String judul;
        private double lat;
        private double lng;
        private String alamat;
        private String isi;
        private String tanggal;
        private boolean hasMapView; // Field baru

        public Cerita(int id_cerita, String kategori, String icon_perasaan, String judul, double lat, double lng, String alamat, String isi, String tanggal, boolean hasMapView) {
            this.id_cerita = id_cerita;
            this.kategori = kategori;
            this.icon_perasaan = icon_perasaan;
            this.judul = judul;
            this.lat = lat;
            this.lng = lng;
            this.alamat = alamat;
            this.isi = isi;
            this.tanggal = tanggal;
            this.hasMapView = hasMapView;
        }
        public int getId_cerita() { return id_cerita; }
        public String getKategori() { return kategori; }
        public String getIcon_perasaan() { return icon_perasaan; }
        public String getJudul() { return judul; }
        public double getLat() { return lat; }
        public double getLng() { return lng; }
        public String getAlamat() { return alamat; }
        public String getIsi() { return isi; }
        public String getTanggal() { return tanggal; }
        public boolean hasMapView() { return hasMapView; } // Getter untuk field baru
    }
}