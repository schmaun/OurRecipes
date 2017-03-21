package de.schmaun.ourrecipes;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.like.LikeButton;
import com.like.OnLikeListener;

import de.schmaun.ourrecipes.Database.DbHelper;
import de.schmaun.ourrecipes.Database.RecipeRepository;
import de.schmaun.ourrecipes.Model.Recipe;

class RecipeActivity extends AppCompatActivity implements RecipeProviderInterface {
    public static final String BUNDLE_KEY_RECIPE_ID = "recipeId";
    private static final String TAG_LIFECYCLE = "RA:lifecycle";

    protected boolean hasUnsavedChanges = false;
    public Recipe recipe;
    public long recipeId;

    void loadRecipeOnCreate(Bundle savedInstanceState, boolean forceLoading)
    {
        recipe = new Recipe();
        recipeId = 0;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            recipeId = bundle.getLong(BUNDLE_KEY_RECIPE_ID);
            Log.d(TAG_LIFECYCLE, String.format("started with %s=%s", BUNDLE_KEY_RECIPE_ID, Long.toString(recipe.getId())));
        }

        if ((savedInstanceState == null || forceLoading) && recipeId > 0) {
            loadRecipe(recipeId);
            Log.d(TAG_LIFECYCLE, "recipe loaded");
        }
    }

    void loadRecipe(long recipeId) {
        DbHelper dbHelper = new DbHelper(this);
        RecipeRepository repository = RecipeRepository.getInstance(dbHelper);
        recipe = repository.loadWithChildren(recipeId);
    }

    @Override
    public Recipe getRecipe() {
        return recipe;
    }

    @Override
    public void setHasUnsavedChanges(boolean unsavedChanges) {
        hasUnsavedChanges = unsavedChanges;
    }

    protected class LikeButtonOnClickListener implements OnLikeListener {
        private Context context;

        public LikeButtonOnClickListener(Context context) {
            this.context = context;
        }

        @Override
        public void liked(LikeButton likeButton) {
            recipe.setFavorite(true);
            DbHelper dbHelper = new DbHelper(context);
            RecipeRepository.getInstance(dbHelper).updateFavoriteStatus(recipe, 1);
        }

        @Override
        public void unLiked(LikeButton likeButton) {
            recipe.setFavorite(false);
            DbHelper dbHelper = new DbHelper(context);
            RecipeRepository.getInstance(dbHelper).updateFavoriteStatus(recipe, 0);
        }
    }
}
