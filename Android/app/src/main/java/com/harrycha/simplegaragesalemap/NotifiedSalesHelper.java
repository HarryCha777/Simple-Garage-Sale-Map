package com.harrycha.simplegaragesalemap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class NotifiedSalesHelper extends SQLiteOpenHelper {

    private static final String TABLE_NAME = "NotifiedSales";
    private static final String idCol = "ID";
    private static final String postedDateTimeCol = "PostedDateTime";

    public NotifiedSalesHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" + idCol + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                postedDateTimeCol + " LONG)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void add(String postedDateTime) {
        String query = "INSERT INTO " + TABLE_NAME + " (" + postedDateTimeCol + ") VALUES (?)";

        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement(query);
        stmt.bindString(1, postedDateTime);
        stmt.executeInsert();
    }

    public boolean isNotified(String postedDateTime) {
        String query = "SELECT " + idCol + " FROM " + TABLE_NAME + " WHERE " +
                postedDateTimeCol + " = ?";

        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement(query);
        stmt.bindString(1, postedDateTime);
        try {
            stmt.simpleQueryForLong(); // if exception does not occur, postedDateTime already exists.
            return true;
        } catch (Exception e) { // simpleQueryForLong throws exception if no row is returned.
            return false;
        }
    }
}

