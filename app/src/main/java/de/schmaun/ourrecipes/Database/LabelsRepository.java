package de.schmaun.ourrecipes.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.schmaun.ourrecipes.Model.Label;
import de.schmaun.ourrecipes.Model.Recipe;

public class LabelsRepository {

    public static final String TAG = "LabelsRepository";
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

    public List<Label> getLabelsForMain() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Label> labels = new ArrayList<>();

        String sql = "SELECT " +
                "label." + COLUMN_NAME_ID + " AS id, " +
                "label." + COLUMN_NAME_NAME + " AS name, " +
                "recipeImage." + RecipeImageRepository.COLUMN_NAME_LOCATION + " AS imageLocation, " +
                "COUNT(DISTINCT recipe." + RecipeRepository.COLUMN_NAME_ID + ") AS countRecipes " +
                "FROM " + TABLE_NAME + " AS label " +
                "JOIN " + REL_TABLE_NAME + " AS rel ON rel." + REL_COLUMN_LABEL_ID + "=label." + COLUMN_NAME_ID + " " +
                "JOIN " + RecipeRepository.TABLE_NAME + " AS recipe ON recipe." + RecipeRepository.COLUMN_NAME_ID + "=rel." + REL_COLUMN_RECIPE_ID + " " +
                "JOIN " + RecipeImageRepository.TABLE_NAME + " AS recipeImage ON recipeImage." + RecipeImageRepository.COLUMN_NAME_RECIPE_ID + "=recipe." + RecipeRepository.COLUMN_NAME_ID + " " +
                "GROUP BY label." + COLUMN_NAME_ID + " " +
                "ORDER BY label." + COLUMN_NAME_NAME + " ASC";

        Log.d(TAG, sql);
        Cursor cursor = db.rawQuery(sql, null);

        int i = 0;
        if (cursor.moveToFirst()) {
            do {
                Label label = new Label();
                label.setId(cursor.getLong(cursor.getColumnIndex("id")));
                label.setName(cursor.getString(cursor.getColumnIndex("name")));
                label.setImageLocation(cursor.getString(cursor.getColumnIndex("imageLocation")));
                label.setCountRecipes(cursor.getInt(cursor.getColumnIndex("countRecipes")));

                Label labelBefore = null;
                if (i >= 2) {
                    labelBefore = labels.get(i - 2);
                }

                if (Math.random() < 0.4f && labelBefore != null && !labelBefore.isFullSpan()) {
                    label.setFullSpan(true);
                }

                i++;
                labels.add(label);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return labels;
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
