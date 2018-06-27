package de.schmaun.ourrecipes.Database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class Repository {
    protected DbHelper dbHelper;
    public static final String COLUMN_NAME_ID = "_id";

    public Repository(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public int delete(String table, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        return db.delete(table, selection, selectionArgs);
    }

    public int delete(String table, long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        return db.delete(table, COLUMN_NAME_ID + " = ?", new String[]{Long.toString(id)});
    }

    public long insert(String table, ContentValues contentValues) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        return db.insertOrThrow(table, null, contentValues);
    }

    public int update(String table, ContentValues contentValues, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        return db.update(table, contentValues, selection, selectionArgs);
    }

    public int update(String table, long id, ContentValues contentValues) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        return db.update(table, contentValues, COLUMN_NAME_ID + " = ?", new String[]{Long.toString(id)});
    }
}
