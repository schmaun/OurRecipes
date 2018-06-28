package de.schmaun.ourrecipes.sync;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;

import de.schmaun.ourrecipes.Database.DbHelper;
import de.schmaun.ourrecipes.Database.LabelsRepository;
import de.schmaun.ourrecipes.Database.RecipeImageRepository;
import de.schmaun.ourrecipes.Database.RecipeRepository;
import de.schmaun.ourrecipes.Database.Repository;

public class Provider extends ContentProvider {
    public static final String TAG = "Provider";

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final String BASE_URI = "de.schmaun.ourrecipes";
    public static final String PATH_RECIPES = "recipes";
    public static final String PATH_IMAGES = "images";
    public static final String PATH_LABELS = "labels";

    public static final int CODE_RECIPES = 101;
    public static final int CODE_RECIPE_ID = 102;
    public static final int CODE_IMAGES = 201;
    public static final int CODE_IMAGE_ID = 202;
    public static final int CODE_LABELS = 301;
    public static final int CODE_LABEL_ID = 302;

    private static final String CONTENT_TYPE_PROVIDER_RECIPES = "vnd." + BASE_URI + ".recipes";
    private static final String CONTENT_TYPE_PROVIDER_IMAGES = "vnd." + BASE_URI + ".images";
    private static final String CONTENT_TYPE_PROVIDER_LABELS = "vnd." + BASE_URI + ".labels";

    public static final String CONTENT_TYPE_RECIPES = "vnd.android.cursor.dir/" + CONTENT_TYPE_PROVIDER_RECIPES;
    public static final String CONTENT_TYPE_RECIPE = "vnd.android.cursor.item/" + CONTENT_TYPE_PROVIDER_RECIPES;
    public static final String CONTENT_TYPE_IMAGES = "vnd.android.cursor.dir/" + CONTENT_TYPE_PROVIDER_IMAGES;
    public static final String CONTENT_TYPE_IMAGE = "vnd.android.cursor.item/" + CONTENT_TYPE_PROVIDER_IMAGES;
    public static final String CONTENT_TYPE_LABELS = "vnd.android.cursor.dir/" + CONTENT_TYPE_PROVIDER_LABELS;
    public static final String CONTENT_TYPE_LABEL = "vnd.android.cursor.item/" + CONTENT_TYPE_PROVIDER_LABELS;

    private static final SparseArray<String> tables = new SparseArray<>();

    static {
        uriMatcher.addURI(BASE_URI, PATH_RECIPES, CODE_RECIPES);
        uriMatcher.addURI(BASE_URI, PATH_RECIPES + "/#", CODE_RECIPE_ID);

        uriMatcher.addURI(BASE_URI, PATH_IMAGES, CODE_IMAGES);
        uriMatcher.addURI(BASE_URI, PATH_IMAGES + "/#", CODE_IMAGE_ID);

        uriMatcher.addURI(BASE_URI, PATH_LABELS, CODE_LABELS);
        uriMatcher.addURI(BASE_URI, PATH_LABELS + "/#", CODE_LABEL_ID);

        tables.put(CODE_RECIPES, RecipeRepository.TABLE_NAME);
        tables.put(CODE_RECIPE_ID, RecipeRepository.TABLE_NAME);
        tables.put(CODE_IMAGES, RecipeImageRepository.TABLE_NAME);
        tables.put(CODE_IMAGE_ID, RecipeImageRepository.TABLE_NAME);
        tables.put(CODE_LABELS, LabelsRepository.TABLE_NAME);
        tables.put(CODE_LABEL_ID, LabelsRepository.TABLE_NAME);
    }

    private DbHelper dbHelper;

    public Provider() {
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete: " + uri);
        int rowCount;
        int code = uriMatcher.match(uri);
        Repository repository = new Repository(dbHelper);

        switch (code) {
            case CODE_RECIPES:
            case CODE_IMAGES:
            case CODE_LABELS:
                rowCount = repository.delete(tables.get(code), selection, selectionArgs);
                break;
            case CODE_RECIPE_ID:
            case CODE_IMAGE_ID:
            case CODE_LABEL_ID:
                rowCount = repository.delete(tables.get(code), Long.parseLong(uri.getLastPathSegment()));
                break;
            default:
                throw new IllegalArgumentException("Invalid Uri: " + uri);
        }

        return rowCount;
    }

    @Override
    public String getType(Uri uri) {
        Log.d(TAG, "getType: " + uri);

        switch (uriMatcher.match(uri)) {
            case CODE_RECIPES:
                return CONTENT_TYPE_RECIPES;
            case CODE_RECIPE_ID:
                return CONTENT_TYPE_RECIPE;
            case CODE_IMAGES:
                return CONTENT_TYPE_IMAGES;
            case CODE_IMAGE_ID:
                return CONTENT_TYPE_IMAGE;
            case CODE_LABELS:
                return CONTENT_TYPE_LABELS;
            case CODE_LABEL_ID:
                return CONTENT_TYPE_LABEL;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "insert: " + uri);

        long id;
        int code = uriMatcher.match(uri);
        Repository repository = new Repository(dbHelper);

        switch (code) {
            case CODE_RECIPES:
            case CODE_IMAGES:
            case CODE_LABELS:
                id = repository.insert(tables.get(code), values);
                break;
            default:
                throw new IllegalArgumentException("Invalid Uri: " + uri);
        }

        Uri newUri = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(newUri, null);

        return newUri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "query: " + uri);

        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Log.d(TAG, "update: " + uri);

        int rowCount;
        int code = uriMatcher.match(uri);
        Repository repository = new Repository(dbHelper);

        switch (code) {
            case CODE_RECIPES:
            case CODE_IMAGES:
            case CODE_LABELS:
                rowCount = repository.update(tables.get(code), values, selection, selectionArgs);
                break;
            case CODE_RECIPE_ID:
            case CODE_IMAGE_ID:
            case CODE_LABEL_ID:
                rowCount = repository.update(tables.get(code), Long.parseLong(uri.getLastPathSegment()), values);
                break;
            default:
                throw new IllegalArgumentException("Invalid Uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return rowCount;
    }
}
