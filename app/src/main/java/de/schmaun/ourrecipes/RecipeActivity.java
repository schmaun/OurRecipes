package de.schmaun.ourrecipes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import de.schmaun.ourrecipes.Database.DbHelper;
import de.schmaun.ourrecipes.Database.RecipeRepository;
import de.schmaun.ourrecipes.Model.Recipe;

class RecipeActivity extends AppCompatActivity implements RecipeProviderInterface {
    public static final String BUNDLE_KEY_RECIPE_ID = "recipeId";
    private static final String TAG_LIFECYCLE = "RA:lifecycle";

    public Recipe recipe;

    void loadRecipe(Bundle savedInstanceState, boolean forceLoading)
    {
        recipe = new Recipe();
        long recipeId = 0;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            recipeId = bundle.getLong(BUNDLE_KEY_RECIPE_ID);
            Log.d(TAG_LIFECYCLE, String.format("started with %s=%s", BUNDLE_KEY_RECIPE_ID, Long.toString(recipe.getId())));
        }

        if ((savedInstanceState == null || forceLoading == true) && recipeId > 0) {
            DbHelper dbHelper = new DbHelper(this);
            RecipeRepository repository = RecipeRepository.getInstance(dbHelper);
            recipe = repository.loadWithChildren(recipeId);

            Log.d(TAG_LIFECYCLE, "recipe loaded");
        }
    }

    @Override
    public Recipe getRecipe() {
        return recipe;
    }
}
