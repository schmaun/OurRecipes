package de.schmaun.ourrecipes.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import de.schmaun.ourrecipes.Model.Label;
import de.schmaun.ourrecipes.Model.Recipe;

public class LabelsRepository {

    private static LabelsRepository mInstance;

    private DbHelper dbHelper;

    public static final String TABLE_NAME = "labels";
    public static final String REL_TABLE_NAME = "recipe_labels";

    public static final String COLUMN_NAME_ID = "_id";
    public static final String COLUMN_NAME_NAME = "name";

    public static final String REL_COLUMN_ID = "_id";
    public static final String REL_COLUMN_LABEL_ID = "labelId";
    public static final String REL_COLUMN_RECIPE_ID = "recipeId";

    public LabelsRepository(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public static LabelsRepository getInstance(DbHelper dbHelper) {
        if (mInstance == null) {
            mInstance = new LabelsRepository(dbHelper);
        }

        return mInstance;
    }

    public void delete(Label label) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_NAME_ID + " = ?", new String[]{Long.toString(label.getId())});
        db.close();
    }

    public void saveLabels(Recipe recipe) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        for (Label label : recipe.getLabels()) {
            replace(label, db);
        }

        deleteRelations(recipe, db);
        saveRelations(recipe, db);

        db.close();
    }

    private Label replace(Label label, SQLiteDatabase db) {
        ContentValues contentValues = createContentValues(label);

        try {
            long id = db.insertOrThrow(TABLE_NAME, null, contentValues);
            label.setId(id);
        } catch (SQLiteConstraintException e) {
            label = load(label, db);
        }

        return label;
    }

    private Label load(Label label, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_NAME_NAME + " = ?", new String[]{ label.getName() }, null, null, null, "1");
        if (cursor.moveToFirst()) {
            label = getFromCursor(cursor, label);
        }
        cursor.close();

        return label;
    }

    private void saveRelations(Recipe recipe, SQLiteDatabase db) {
        for (Label label : recipe.getLabels()) {
            ContentValues values = new ContentValues();
            values.put(REL_COLUMN_LABEL_ID, label.getId());
            values.put(REL_COLUMN_RECIPE_ID, recipe.getId());

            db.insert(REL_TABLE_NAME, null, values);
        }
    }

    private void deleteRelations(Recipe recipe, SQLiteDatabase db) {
        db.delete(REL_TABLE_NAME, REL_COLUMN_RECIPE_ID + " = ?", new String[]{Long.toString(recipe.getId())});
    }

    public ArrayList<Label> loadLabels(long recipeId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " +
                        "l." + COLUMN_NAME_ID + ", " +
                        "l." + COLUMN_NAME_NAME + " " +
                        "FROM " + TABLE_NAME + " AS l " +
                        "JOIN " + REL_TABLE_NAME + " ON " + REL_COLUMN_LABEL_ID + "=l." + COLUMN_NAME_ID + " " +
                        "WHERE " + REL_COLUMN_RECIPE_ID + "=? " +
                        "ORDER BY l." + COLUMN_NAME_NAME + " ASC"
                , new String[]{Long.toString(recipeId)}
        );


        ArrayList<Label> labels = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Label label = getFromCursor(cursor);
                labels.add(label);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return labels;
    }

    public ArrayList<Label> loadLabels() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, COLUMN_NAME_NAME +  " ASC");

        ArrayList<Label> labels = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Label label = getFromCursor(cursor);
                labels.add(label);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return labels;
    }

    private Label getFromCursor(Cursor cursor) {
        Label label = new Label();

        return getFromCursor(cursor, label);
    }

    private Label getFromCursor(Cursor cursor, Label label) {
        label.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_ID)));
        label.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_NAME)));

        return label;
    }

    private ContentValues createContentValues(Label label) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME_NAME, label.getName());

        return values;
    }

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME_NAME + " TEXT"
                + ")");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + REL_TABLE_NAME + " ("
                + REL_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + REL_COLUMN_LABEL_ID + " INTEGER,"
                + REL_COLUMN_RECIPE_ID + " INTEGER"
                + ")");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS labelId_recipeId ON " + REL_TABLE_NAME + "(" + REL_COLUMN_LABEL_ID + ", " + REL_COLUMN_RECIPE_ID + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS recipeId ON " + REL_TABLE_NAME + "(" + REL_COLUMN_RECIPE_ID + ");");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS " + COLUMN_NAME_NAME + " ON " + TABLE_NAME + "(" + COLUMN_NAME_NAME + ");");
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + REL_TABLE_NAME);
        onCreate(db);
    }
}
