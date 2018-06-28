package de.schmaun.ourrecipes.Main;

import android.os.Bundle;
import android.util.Log;

import de.schmaun.ourrecipes.Database.DbHelper;
import de.schmaun.ourrecipes.Database.RecipeRepository;
import de.schmaun.ourrecipes.Utils.StopWatch;

public class RecipesListFavoritesFragment extends RecipeListBaseFragment {

    public static final String TAG = "RecipesListFavoritesFt";

    public RecipesListFavoritesFragment() {
    }

    public static RecipesListFavoritesFragment newInstance() {
        return new RecipesListFavoritesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void loadRecipes() {
        StopWatch stopWatch = StopWatch.createAndStart();
        recipes = RecipeRepository.getInstance(new DbHelper(getContext())).getFavoriteRecipes();
        Log.d(TAG, "getRecipesForLabel duration: " + Long.toString(stopWatch.stop()));
    }
}
