package com.harrycha.simplegaragesalemap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class SalesHelper extends SQLiteOpenHelper {

    private static final String TABLE_NAME = "Sales";
    private static final String idCol = "ID";
    private static final String deviceIDCol = "DeviceID";
    private static final String latitudeCol = "Latitude";
    private static final String longitudeCol = "Longitude";
    private static final String postedDateTimeCol = "PostedDateTime";
    private static final String endDateTimeCol = "EndDateTime";
    private static final String titleCol = "Title";
    private static final String addressCol = "Address";

    public SalesHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" + idCol + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                deviceIDCol + " TEXT, " + latitudeCol + " DOUBLE, " + longitudeCol + " DOUBLE, " +
                postedDateTimeCol + " LONG, " + endDateTimeCol + " LONG, " + titleCol + " TEXT, " + addressCol + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void clear() {
        String query = "DELETE FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
    }

    public void add(String deviceID, double latitude, double longitude, String postedDateTime, String endDateTime, String title, String address) {
        String query = "INSERT INTO " + TABLE_NAME + " (" + deviceIDCol + ", " + latitudeCol + ", " + longitudeCol + ", " +
                postedDateTimeCol + ", " + endDateTimeCol + ", " + titleCol + ", " + addressCol + ") VALUES (?, ?, ?, ?, ?, ?, ?)";

        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement(query);
        stmt.bindString(1, deviceID);
        stmt.bindDouble(2, latitude);
        stmt.bindDouble(3, longitude);
        stmt.bindString(4, postedDateTime);
        stmt.bindString(5, endDateTime);
        stmt.bindString(6, title);
        stmt.bindString(7, address);
        stmt.executeInsert();
    }

    public void edit(String postedDateTime, String endDateTime, String title) {
        String query = "UPDATE " + TABLE_NAME + " SET " + endDateTimeCol + " = ?, " + titleCol + " = ? WHERE " +
                postedDateTimeCol + " = ?";

        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement(query);
        stmt.bindString(1, endDateTime);
        stmt.bindString(2, title);
        stmt.bindString(3, postedDateTime);
        stmt.executeUpdateDelete();
    }

    public Cursor get() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public void delete(String postDateTime) {
        String query = "DELETE FROM " + TABLE_NAME + " WHERE " + postedDateTimeCol + " = ?";

        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement(query);
        stmt.bindString(1, postDateTime);
        stmt.executeUpdateDelete();
    }
}

