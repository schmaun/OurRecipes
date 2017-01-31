package de.schmaun.ourrecipes.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.schmaun.ourrecipes.EditRecipeActivity;
import de.schmaun.ourrecipes.Model.Recipe;
import de.schmaun.ourrecipes.RecipeViewHolder;
import de.schmaun.ourrecipes.ViewRecipeActivity;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeViewHolder>{

    private List<Recipe> recipes;
    private int rowLayout;
    private Context mContext;

    public RecipeAdapter(List<Recipe> recipes, int rowLayout, Context context) {
        this.recipes = recipes;
        this.rowLayout = rowLayout;
        this.mContext = context;
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(final ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);

        return new RecipeViewHolder(v, new RecipeViewHolder.ViewHolderClicks() {
            public void showRecipe(View caller, long recipeId) {
                Intent intent = new Intent(viewGroup.getContext(), ViewRecipeActivity.class);
                intent.putExtra(ViewRecipeActivity.BUNDLE_KEY_RECIPE_ID, recipeId);
                viewGroup.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder recipeViewHolder, int i) {
        Recipe recipe = recipes.get(i);
        recipeViewHolder.recipeName.setText(recipe.getName());
        recipeViewHolder.id = recipe.getId();
    }

    @Override
    public int getItemCount() {
        return recipes == null ? 0 : recipes.size();
    }
}

