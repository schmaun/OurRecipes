package de.schmaun.ourrecipes.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.schmaun.ourrecipes.Model.Recipe;

public class RecipeRepository {
    private final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss.sss";
    private final String TAG = "RecipeRepository";

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
    public static final String COLUMN_NAME_FAVORITE = "favourite";

    public static final String COLUMN_NAME_CREATED_AT = "createdAt";
    public static final String COLUMN_NAME_LAST_EDIT_AT = "lastEditAt";

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

    public List<Recipe> getRecipesForLabel(long labelId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        List<Recipe> recipes = new ArrayList<Recipe>();

        String sql = "SELECT recipe.* FROM " + TABLE_NAME + " AS recipe " +
                "JOIN " + LabelsRepository.REL_TABLE_NAME + " AS rel ON rel." + LabelsRepository.REL_COLUMN_RECIPE_ID + "=recipe." + COLUMN_NAME_ID + " " +
                "WHERE rel." + LabelsRepository.REL_COLUMN_LABEL_ID + "=" + Long.toString(labelId) + " " +
                "ORDER BY recipe." + COLUMN_NAME_NAME;
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                Recipe recipe = getRecipeFromCursor(cursor);
                loadChildren(recipe);
                recipes.add(recipe);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return recipes;
    }

    public List<Recipe> getRecipesWithoutLabel(long labelId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        List<Recipe> recipes = new ArrayList<Recipe>();

        String sql = "SELECT recipe.* FROM " + TABLE_NAME + " AS recipe " +
                "LEFT JOIN " + LabelsRepository.REL_TABLE_NAME + " AS rel ON rel." + LabelsRepository.REL_COLUMN_RECIPE_ID + "=recipe." + COLUMN_NAME_ID + " " +
                "WHERE rel." + LabelsRepository.REL_COLUMN_LABEL_ID + " IS NULL " +
                "ORDER BY recipe." + COLUMN_NAME_NAME;
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                Recipe recipe = getRecipeFromCursor(cursor);
                loadChildren(recipe);
                recipes.add(recipe);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return recipes;
    }

    public Recipe loadWithChildren(long recipeId) {
        Recipe recipe = load(recipeId);
        loadChildren(recipe);

        return recipe;
    }

    private Recipe loadChildren(Recipe recipe) {
        RecipeImageRepository imageRepository = RecipeImageRepository.getInstance(dbHelper);
        recipe.setImages(imageRepository.load(recipe.getId()));

        LabelsRepository labelsRepository = LabelsRepository.getInstance(dbHelper);
        recipe.setLabels(labelsRepository.loadLabels(recipe.getId()));

        return recipe;
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
        recipe.setCreatedAt(new Date());
        ContentValues contentValues = createContentValues(recipe);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert(TABLE_NAME, null, contentValues);
        db.close();

        recipe.setId(id);
        return id;
    }

    public void update(Recipe recipe)
    {
        recipe.setLastEditAt(new Date());
        ContentValues contentValues = createContentValues(recipe);
        contentValues.put(COLUMN_NAME_ID, recipe.getId());

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.update(TABLE_NAME, contentValues, COLUMN_NAME_ID + " = ?", new String[]{Long.toString(recipe.getId())});
        db.close();
    }

    public void updateFavoriteStatus(Recipe recipe, int favStatus) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_FAVORITE, favStatus);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.update(TABLE_NAME, values, COLUMN_NAME_ID + " = ?", new String[]{Long.toString(recipe.getId())});
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

        values.put(COLUMN_NAME_RATING, recipe.getRating());
        values.put(COLUMN_NAME_FAVORITE, recipe.getFavorite());

        values.put(COLUMN_NAME_CREATED_AT, formatDate(recipe.getCreatedAt()));
        values.put(COLUMN_NAME_LAST_EDIT_AT, formatDate(recipe.getLastEditAt()));

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
        recipe.setNotes(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_NOTES)));

        recipe.setRating(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_RATING)));
        recipe.setFavorite(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_FAVORITE)));

        recipe.setCreatedAt(createDate(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_CREATED_AT))));
        recipe.setLastEditAt(createDate(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_LAST_EDIT_AT))));

        return recipe;
    }

    private Date createDate(String date) {
        if (date == null) {
            return null;
        }

        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.US);

        try {
            return format.parse(date);
        } catch (ParseException e) {
            Log.d(TAG, "invalid date: " + date);
            return null;
        }
    }

    private String formatDate(Date date)
    {
        if (date == null) {
            return null
                    ;
        }

        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.US);

        return format.format(date);
    }

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME_CATEGORY_ID + " INTEGER,"

                + COLUMN_NAME_NAME + " TEXT,"
                + COLUMN_NAME_DESCRIPTION + " TEXT,"
                + COLUMN_NAME_INGREDIENTS + " TEXT,"
                + COLUMN_NAME_PREPARATION + " TEXT,"
                + COLUMN_NAME_NOTES + " TEXT,"

                + COLUMN_NAME_RATING + " INTEGER,"
                + COLUMN_NAME_FAVORITE + " INTEGER,"

                + COLUMN_NAME_CREATED_AT + " TEXT,"
                + COLUMN_NAME_LAST_EDIT_AT + " TEXT"
                + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS " + COLUMN_NAME_FAVORITE + " ON " + TABLE_NAME + "(" + COLUMN_NAME_FAVORITE + ");");
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i = oldVersion + 1; i <= newVersion; i++) {
            doUpgrade(db, i);
        }
    }

    private static void doUpgrade(SQLiteDatabase db, int newVersion) {
        switch (newVersion) {
            case 2:
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " + COLUMN_NAME_NOTES + " TEXT");
                break;
            case 6:
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " + COLUMN_NAME_FAVORITE + " INTEGER");
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " + COLUMN_NAME_CREATED_AT + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " + COLUMN_NAME_LAST_EDIT_AT + " TEXT");
                db.execSQL("CREATE INDEX IF NOT EXISTS " + COLUMN_NAME_FAVORITE + " ON " + TABLE_NAME + "(" + COLUMN_NAME_FAVORITE + ");");
                break;
        }
    }

}
