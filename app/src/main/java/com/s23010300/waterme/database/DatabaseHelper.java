package com.s23010300.waterme.database;

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
    private static final String DATABASE_NAME = "WaterMe.db";
    private static final int DATABASE_VERSION = 1;

    // Plants table
    private static final String TABLE_PLANTS = "plants";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_IMAGE = "image";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_FREQUENCY = "frequency";
    private static final String COL_LAST_WATERED = "last_watered";
    private static final String COL_MISSED = "missed_last";

    // Watering history table
    private static final String TABLE_HISTORY = "watering_history";
    private static final String COL_HISTORY_ID = "history_id";
    private static final String COL_PLANT_ID = "plant_id";
    private static final String COL_WATERED_DATE = "watered_date";
    private static final String COL_SUCCESS = "success";

    // Settings table
    private static final String TABLE_SETTINGS = "settings";
    private static final String COL_SETTING_ID = "setting_id";
    private static final String COL_LATITUDE = "latitude";
    private static final String COL_LONGITUDE = "longitude";
    private static final String COL_EMAIL = "email";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create plants table
        String createPlantsTable = "CREATE TABLE " + TABLE_PLANTS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT, " +
                COL_IMAGE + " TEXT, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_FREQUENCY + " INTEGER, " +
                COL_LAST_WATERED + " TEXT, " +
                COL_MISSED + " INTEGER DEFAULT 0)";
        db.execSQL(createPlantsTable);

        // Create watering history table
        String createHistoryTable = "CREATE TABLE " + TABLE_HISTORY + " (" +
                COL_HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PLANT_ID + " INTEGER, " +
                COL_WATERED_DATE + " TEXT, " +
                COL_SUCCESS + " INTEGER, " +
                "FOREIGN KEY(" + COL_PLANT_ID + ") REFERENCES " + TABLE_PLANTS + "(" + COL_ID + "))";
        db.execSQL(createHistoryTable);

        // Create settings table
        String createSettingsTable = "CREATE TABLE " + TABLE_SETTINGS + " (" +
                COL_SETTING_ID + " INTEGER PRIMARY KEY, " +
                COL_LATITUDE + " REAL DEFAULT 0.0, " +
                COL_LONGITUDE + " REAL DEFAULT 0.0, " +
                COL_EMAIL + " TEXT DEFAULT '')";
        db.execSQL(createSettingsTable);

        // Insert default settings
        ContentValues cv = new ContentValues();
        cv.put(COL_SETTING_ID, 1);
        cv.put(COL_LATITUDE, 0.0);
        cv.put(COL_LONGITUDE, 0.0);
        cv.put(COL_EMAIL, "");
        db.insert(TABLE_SETTINGS, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLANTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
        onCreate(db);
    }

    // Add new plant
    public long addPlant(Plant plant) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, plant.getName());
        cv.put(COL_IMAGE, plant.getImageName());
        cv.put(COL_DESCRIPTION, plant.getDescription());
        cv.put(COL_FREQUENCY, plant.getWateringFrequency());
        cv.put(COL_LAST_WATERED, getCurrentDate());
        cv.put(COL_MISSED, 0);

        long id = db.insert(TABLE_PLANTS, null, cv);

        // Add initial watering entry (plant was just added and watered)
        if (id != -1) {
            addWateringHistory((int) id, true);
        }

        return id;
    }

    // Get all plants
    public List<Plant> getAllPlants() {
        List<Plant> plants = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PLANTS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Plant plant = new Plant();
                plant.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
                plant.setName(cursor.getString(cursor.getColumnIndex(COL_NAME)));
                plant.setImageName(cursor.getString(cursor.getColumnIndex(COL_IMAGE)));
                plant.setDescription(cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION)));
                plant.setWateringFrequency(cursor.getInt(cursor.getColumnIndex(COL_FREQUENCY)));
                plant.setLastWatered(cursor.getString(cursor.getColumnIndex(COL_LAST_WATERED)));
                plant.setMissedLastWatering(cursor.getInt(cursor.getColumnIndex(COL_MISSED)) == 1);
                plants.add(plant);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return plants;
    }

    // Get plant by ID
    public Plant getPlant(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PLANTS, null, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        Plant plant = null;
        if (cursor.moveToFirst()) {
            plant = new Plant();
            plant.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
            plant.setName(cursor.getString(cursor.getColumnIndex(COL_NAME)));
            plant.setImageName(cursor.getString(cursor.getColumnIndex(COL_IMAGE)));
            plant.setDescription(cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION)));
            plant.setWateringFrequency(cursor.getInt(cursor.getColumnIndex(COL_FREQUENCY)));
            plant.setLastWatered(cursor.getString(cursor.getColumnIndex(COL_LAST_WATERED)));
            plant.setMissedLastWatering(cursor.getInt(cursor.getColumnIndex(COL_MISSED)) == 1);
        }
        cursor.close();
        return plant;
    }

    // Water plant
    public void waterPlant(int plantId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Update last watered date
        ContentValues cv = new ContentValues();
        cv.put(COL_LAST_WATERED, getCurrentDate());
        cv.put(COL_MISSED, 0);
        db.update(TABLE_PLANTS, cv, COL_ID + "=?", new String[]{String.valueOf(plantId)});

        // Add to history
        addWateringHistory(plantId, true);
    }

    // Add watering history
    public void addWateringHistory(int plantId, boolean success) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PLANT_ID, plantId);
        cv.put(COL_WATERED_DATE, getCurrentDate());
        cv.put(COL_SUCCESS, success ? 1 : 0);
        db.insert(TABLE_HISTORY, null, cv);
    }

    // Get watering history for a plant
    public List<String[]> getWateringHistory(int plantId) {
        List<String[]> history = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HISTORY, null, COL_PLANT_ID + "=?",
                new String[]{String.valueOf(plantId)}, null, null, COL_WATERED_DATE + " DESC");

        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndex(COL_WATERED_DATE));
                boolean success = cursor.getInt(cursor.getColumnIndex(COL_SUCCESS)) == 1;
                history.add(new String[]{date, String.valueOf(success)});
            } while (cursor.moveToNext());
        }
        cursor.close();
        return history;
    }

    // Get watering statistics
    public int[] getWateringStats() {
        SQLiteDatabase db = this.getReadableDatabase();
        int success = 0, missed = 0;

        Cursor cursor = db.query(TABLE_HISTORY, new String[]{"COUNT(*)"},
                COL_SUCCESS + "=1", null, null, null, null);
        if (cursor.moveToFirst()) {
            success = cursor.getInt(0);
        }
        cursor.close();

        cursor = db.query(TABLE_HISTORY, new String[]{"COUNT(*)"},
                COL_SUCCESS + "=0", null, null, null, null);
        if (cursor.moveToFirst()) {
            missed = cursor.getInt(0);
        }
        cursor.close();

        return new int[]{success, missed};
    }

    // Settings methods
    public void updateSettings(double latitude, double longitude, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_LATITUDE, latitude);
        cv.put(COL_LONGITUDE, longitude);
        cv.put(COL_EMAIL, email);
        db.update(TABLE_SETTINGS, cv, COL_SETTING_ID + "=1", null);
    }

    public double[] getLocation() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SETTINGS, new String[]{COL_LATITUDE, COL_LONGITUDE},
                COL_SETTING_ID + "=1", null, null, null, null);

        double[] location = new double[]{0.0, 0.0};
        if (cursor.moveToFirst()) {
            location[0] = cursor.getDouble(0);
            location[1] = cursor.getDouble(1);
        }
        cursor.close();
        return location;
    }

    public String getEmail() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SETTINGS, new String[]{COL_EMAIL},
                COL_SETTING_ID + "=1", null, null, null, null);

        String email = "";
        if (cursor.moveToFirst()) {
            email = cursor.getString(0);
        }
        cursor.close();
        return email;
    }

    // Delete plant
    public boolean deletePlant(int plantId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Delete watering history first (foreign key constraint)
        db.delete(TABLE_HISTORY, COL_PLANT_ID + "=?", new String[]{String.valueOf(plantId)});

        // Delete plant
        int result = db.delete(TABLE_PLANTS, COL_ID + "=?", new String[]{String.valueOf(plantId)});
        return result > 0;
    }

    // Helper methods
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }
}