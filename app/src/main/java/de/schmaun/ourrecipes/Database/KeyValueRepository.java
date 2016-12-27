package de.schmaun.ourrecipes.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class KeyValueRepository {

    private static KeyValueRepository mInstance;

    private DbHelper dbHelper;

    public static final String TABLE_NAME = "keyValue";

    public static final String COLUMN_NAME_ID = "_id";
    public static final String COLUMN_NAME_KEY = "key";
    public static final String COLUMN_NAME_VALUE = "value";

    public KeyValueRepository(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public static KeyValueRepository getInstance(DbHelper dbHelper) {
        if (mInstance == null) {
            mInstance = new KeyValueRepository(dbHelper);
        }

        return mInstance;
    }

    public String load(String key)
    {
        String value = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_NAME_KEY + " = ?", new String[]{key}, null, null, null, "1");

        if (cursor.moveToFirst()) {
            value = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_VALUE));
        }
        cursor.close();

        return value;
    }

    public void save(String key, String value)
    {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_KEY, key);
        values.put(COLUMN_NAME_VALUE, value);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void delete(String key) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_NAME_KEY + " = ?", new String[]{ key });
    }

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME_KEY + " TEXT UNIQUE,"
                + COLUMN_NAME_VALUE + " TEXT"
                + ")");
    }

    public static void onUpgrade(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
