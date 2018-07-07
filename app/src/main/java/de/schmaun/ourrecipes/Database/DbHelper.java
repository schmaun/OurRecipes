package de.schmaun.ourrecipes.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 10;
    public static final String DATABASE_NAME = "RecipeBox.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public void onCreate(SQLiteDatabase db) {
    	RecipeRepository.onCreate(db);
        RecipeImageRepository.onCreate(db);
        CategoriesRepository.onCreate(db);
        KeyValueRepository.onCreate(db);
        LabelsRepository.onCreate(db);
    }
    
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	RecipeRepository.onUpgrade(db, oldVersion, newVersion);
        LabelsRepository.onUpgrade(db, oldVersion, newVersion);
        RecipeImageRepository.onUpgrade(db, oldVersion, newVersion);

        /*
        CategoriesRepository.onUpgrade(db);

        //KeyValueRepository.onUpgrade(db);
        LabelsRepository.onUpgrade(db);
        */
    }
}