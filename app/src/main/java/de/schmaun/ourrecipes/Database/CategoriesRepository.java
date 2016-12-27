package de.schmaun.ourrecipes.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import de.schmaun.ourrecipes.Model.Category;

public class CategoriesRepository {

    public static final String TABLE_NAME = "categories";
    public static final String COLUMN_NAME_ID = "_id";
    public static final String COLUMN_NAME__ID = "id";
    public static final String COLUMN_NAME_TITLE = "title";
    public static final String COLUMN_NAME_PARENT_ID = "parentId";

	public List<Category> getAllLabels(int parentId, SQLiteDatabase db, int padding)
	{
        List<Category> categories = new ArrayList<Category>();
        List<Category> childrenCategories = new ArrayList<Category>();
              
        Cursor cursor = db.rawQuery(getSqlForGetAllLabels(parentId), null);
        
        if (cursor.moveToFirst()) {
            do {
                Category category = getCategoryFromCursor(cursor);

            	if (padding > 0)
            	{
                    category.setTitle(padCategoryTitle(padding, category.getTitle()));
            	}

                categories.add(category);

                childrenCategories = getAllLabels(category.getId(), db, padding+1);
                if (childrenCategories.size() > 0) {
                    categories.addAll(childrenCategories);
                }
            } while (cursor.moveToNext());
        }
         
        cursor.close();
         
        return categories;
	}

    private String getSqlForGetAllLabels(int parentId) {
        return "SELECT * " +
        	"FROM " + TABLE_NAME + " " +
        	"WHERE " + COLUMN_NAME_PARENT_ID + "=" + parentId + " " +
            "ORDER BY " + COLUMN_NAME_TITLE + " ASC";
    }

    public ArrayList<Category> getCategories(int parentId, SQLiteDatabase db) {
        ArrayList<Category> categories = new ArrayList<Category>();

        Cursor cursor = db.rawQuery(getSqlForGetAllLabels(parentId), null);

        if (cursor.moveToFirst()) {
            do {
                Category category = getCategoryFromCursor(cursor);
                category.setChildCategories(getCategories(category.getId(), db));
                categories.add(category);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return categories;
    }

    private String padCategoryTitle(int padding, String title) {
        int titleLength = ((padding * 4) + title.length());
        return String.format(
                "%" + titleLength + "s",
                title
        );
    }

    private Category getCategoryFromCursor(Cursor cursor) {
        Category category = new Category();
        category.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME__ID)));
        category.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_TITLE)));
        category.setParentId(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_PARENT_ID)));
        return category;
    }

    public void insert(SQLiteDatabase db, Category category)
	{
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME_PARENT_ID, category.getParentId());
		values.put(COLUMN_NAME_TITLE, category.getTitle());
		values.put(COLUMN_NAME__ID, category.getId());
		db.insert(TABLE_NAME, null, values);
	}

    public Category getCategory(int categoryId, SQLiteDatabase db)
    {
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_NAME__ID + " = ?", new String[] { Integer.toString(categoryId) }, null, null, null, "1");
        Category category = null;
        if (cursor.moveToFirst()) {
            category = getCategoryFromCursor(cursor);
        }
        cursor.close();
        return category;
    }

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME__ID + " INTEGER,"
                + COLUMN_NAME_TITLE + " TEXT,"
                + COLUMN_NAME_PARENT_ID + " INTEGER"
                + ")");
    }

    public static void onUpgrade(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
