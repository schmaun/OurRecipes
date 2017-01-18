package de.schmaun.ourrecipes.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

import de.schmaun.ourrecipes.Model.Recipe;

public class RecipeRepository {

    private static RecipeRepository mInstance;

    private DbHelper dbHelper;

    public static final String TABLE_NAME = "recipes";

    public static final String COLUMN_NAME_ID = "_id";
    public static final String COLUMN_NAME_CATEGORY_ID = "categoryId";

    public static final String COLUMN_NAME_NAME = "name";
    public static final String COLUMN_NAME_DESCRIPTION = "description";
    public static final String COLUMN_NAME_INGREDIENTS = "ingredients";
    public static final String COLUMN_NAME_PREPARATION = "preparation";
    public static final String COLUMN_NAME_NOTES = "notes";

    public static final String COLUMN_NAME_RATING = "rating";

    public RecipeRepository(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public static RecipeRepository getInstance(DbHelper dbHelper) {
        if (mInstance == null) {
            mInstance = new RecipeRepository(dbHelper);
        }

        return mInstance;
    }

    public List<Recipe> getRecipes()
    {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        List<Recipe> recipes = new ArrayList<Recipe>();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_NAME_ID, null);

        if (cursor.moveToFirst()) {
            do {
                Recipe recipe = getRecipeFromCursor(cursor);
                recipes.add(recipe);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return recipes;
    }

    public Recipe load(long recipeId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_NAME_ID + " = ?", new String[]{Long.toString(recipeId)}, null, null, null, "1");

        Recipe recipe = null;
        if (cursor.moveToFirst()) {
            recipe = getRecipeFromCursor(cursor);
        }

        cursor.close();

        return recipe;
    }

    public long save(Recipe recipe)
    {
        if (recipe.getId() != 0) {
            update(recipe);
            return recipe.getId();
        } else {
            return insert(recipe);
        }
    }

    public long insert(Recipe recipe) {
        ContentValues contentValues = createContentValues(recipe);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert(TABLE_NAME, null, contentValues);
        db.close();

        recipe.setId(id);
        return id;
    }

    public void update(Recipe recipe)
    {
        ContentValues contentValues = createContentValues(recipe);
        contentValues.put(COLUMN_NAME_ID, recipe.getId());

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.update(TABLE_NAME, contentValues, COLUMN_NAME_ID + " = ?", new String[]{Long.toString(recipe.getId())});
        db.close();
    }

    public void delete(long recipeId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_NAME_ID + " = ?", new String[]{Long.toString(recipeId)});
    }

    private ContentValues createContentValues(Recipe recipe)
    {
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME_CATEGORY_ID, recipe.getCategoryId());

        values.put(COLUMN_NAME_NAME, recipe.getName());
        values.put(COLUMN_NAME_DESCRIPTION, recipe.getDescription());
        values.put(COLUMN_NAME_INGREDIENTS, recipe.getIngredients());
        values.put(COLUMN_NAME_PREPARATION, recipe.getPreparation());
        values.put(COLUMN_NAME_NOTES, recipe.getNotes());

        return values;
    }

    private Recipe getRecipeFromCursor(Cursor cursor) {
        Recipe recipe = new Recipe();

        recipe.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_ID)));
        recipe.setCategoryId(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_CATEGORY_ID)));

        recipe.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_NAME)));
        recipe.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_DESCRIPTION)));
        recipe.setIngredients(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_INGREDIENTS)));
        recipe.setPreparation(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PREPARATION)));

        return recipe;
    }

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME_CATEGORY_ID + " INTEGER,"

                + COLUMN_NAME_NAME + " TEXT,"
                + COLUMN_NAME_DESCRIPTION + " TEXT,"
                + COLUMN_NAME_INGREDIENTS + " TEXT,"
                + COLUMN_NAME_PREPARATION + " TEXT,"

                + COLUMN_NAME_RATING + " INTEGER"
                + ")");
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i = oldVersion + 1; i <= newVersion; i++) {
            doUpgrade(db, i);
        }
    }

    private static void doUpgrade(SQLiteDatabase db, int newVersion) {
        switch (newVersion) {            case 2:
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " + COLUMN_NAME_NOTES + " TEXT");
                break;
        }
    }
}
