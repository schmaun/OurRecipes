package de.schmaun.ourrecipes.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.schmaun.ourrecipes.Model.RecipeImage;

public class RecipeImageRepository {

    private static RecipeImageRepository mInstance;

    private DbHelper dbHelper;

    public static final String TABLE_NAME = "recipe_images";

    public static final String COLUMN_NAME_ID = "_id";
    public static final String COLUMN_NAME_RECIPE_ID = "recipeId";

    public static final String COLUMN_NAME_NAME = "name";
    public static final String COLUMN_NAME_DESCRIPTION = "description";
    public static final String COLUMN_NAME_LOCATION = "location";
    public static final String COLUMN_NAME_POSITION = "position";
    public static final String COLUMN_NAME_IS_COVER_IMAGE = "isCoverImage";

    public RecipeImageRepository(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public static RecipeImageRepository getInstance(DbHelper dbHelper) {
        if (mInstance == null) {
            mInstance = new RecipeImageRepository(dbHelper);
        }

        return mInstance;
    }

    public ArrayList<RecipeImage> load(long recipeId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_NAME_RECIPE_ID + " = ?", new String[]{Long.toString(recipeId)}, null, null, COLUMN_NAME_POSITION + " ASC", null);

        ArrayList<RecipeImage> images = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                RecipeImage image = getFromCursor(cursor);
                images.add(image);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return images;
    }

    public void save(long recipeId, ArrayList<RecipeImage> recipeImages) {
        SparseIntArray positions = new SparseIntArray();
        int position;

        for (RecipeImage recipeImage : recipeImages) {
            position = positions.get(recipeImage.getParentType());

            recipeImage.setRecipeId(recipeId);
            recipeImage.setPosition(position);

            positions.put(recipeImage.getParentType(), position + 1);

            if (recipeImage.getId() != 0) {
                this.update(recipeImage);
            } else {
                this.insert(recipeImage);
            }
        }
    }

    public long insert(RecipeImage image) {
        ContentValues contentValues = createContentValues(image);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert(TABLE_NAME, null, contentValues);
        db.close();

        image.setId(id);
        return id;
    }

    public void update(RecipeImage image)
    {
        ContentValues contentValues = createContentValues(image);
        contentValues.put(COLUMN_NAME_ID, image.getId());

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.update(TABLE_NAME, contentValues, COLUMN_NAME_ID + " = ?", new String[]{Long.toString(image.getId())});
        db.close();
    }

    public void deleteByRecipeId(long recipeId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_NAME_RECIPE_ID + " = ?", new String[]{Long.toString(recipeId)});
        db.close();
    }

    public void delete(ArrayList<RecipeImage> imagesToDelete) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        for (RecipeImage image : imagesToDelete) {
            if(image.getId() > 0) {
                db.delete(TABLE_NAME, COLUMN_NAME_ID + " = ?", new String[]{Long.toString(image.getId())});
            }
        }
        db.close();
    }

    private RecipeImage getFromCursor(Cursor cursor) {
        RecipeImage image = new RecipeImage();

        image.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_ID)));
        image.setRecipeId(cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_RECIPE_ID)));

        image.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_NAME)));
        image.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_DESCRIPTION)));
        image.setLocation(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_LOCATION)));
        image.setPosition(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_POSITION)));
        image.setCoverImage(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_IS_COVER_IMAGE)));

        return image;
    }

    private ContentValues createContentValues(RecipeImage recipeImage)
    {
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME_RECIPE_ID, recipeImage.getRecipeId());

        values.put(COLUMN_NAME_NAME, recipeImage.getName());
        values.put(COLUMN_NAME_DESCRIPTION, recipeImage.getDescription());
        values.put(COLUMN_NAME_LOCATION, recipeImage.getLocation());
        values.put(COLUMN_NAME_POSITION, recipeImage.getPosition());
        values.put(COLUMN_NAME_IS_COVER_IMAGE, recipeImage.getCoverImage());

        return values;
    }

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME_RECIPE_ID + " INTEGER,"

                + COLUMN_NAME_NAME + " TEXT,"
                + COLUMN_NAME_DESCRIPTION + " TEXT,"
                + COLUMN_NAME_LOCATION + " TEXT,"
                + COLUMN_NAME_POSITION + " INTEGER,"
                + COLUMN_NAME_IS_COVER_IMAGE + " INTEGER"

                + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS recipeId_position ON " + TABLE_NAME + "(" + COLUMN_NAME_RECIPE_ID + ", " +  COLUMN_NAME_POSITION + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS recipeId_cover ON " + TABLE_NAME + "(" + COLUMN_NAME_RECIPE_ID + ", " +  COLUMN_NAME_IS_COVER_IMAGE + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS " + COLUMN_NAME_IS_COVER_IMAGE + " ON " + TABLE_NAME + "(" + COLUMN_NAME_IS_COVER_IMAGE + ");");
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i = oldVersion + 1; i <= newVersion; i++) {
            doUpgrade(db, i);
        }
    }

    private static void doUpgrade(SQLiteDatabase db, int newVersion) {
        switch (newVersion) {
            case 6:
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " + COLUMN_NAME_IS_COVER_IMAGE + " INTEGER");
                db.execSQL("CREATE INDEX IF NOT EXISTS recipeId_cover ON " + TABLE_NAME + "(" + COLUMN_NAME_RECIPE_ID + ", " +  COLUMN_NAME_IS_COVER_IMAGE + ");");
                db.execSQL("CREATE INDEX IF NOT EXISTS " + COLUMN_NAME_IS_COVER_IMAGE + " ON " + TABLE_NAME + "(" + COLUMN_NAME_IS_COVER_IMAGE + ");");
                break;
        }
    }


}
