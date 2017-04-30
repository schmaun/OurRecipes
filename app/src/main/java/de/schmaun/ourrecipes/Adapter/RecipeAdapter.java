package de.schmaun.ourrecipes.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.schmaun.ourrecipes.Model.Recipe;
import de.schmaun.ourrecipes.Model.RecipeImage;
import de.schmaun.ourrecipes.R;
import de.schmaun.ourrecipes.RecipeViewHolder;
import de.schmaun.ourrecipes.ViewRecipeActivity;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeViewHolder>{

    private List<Recipe> recipes;
    private int rowLayout;
    private Context context;

    public RecipeAdapter(List<Recipe> recipes, int rowLayout, Context context) {
        this.recipes = recipes;
        this.rowLayout = rowLayout;
        this.context = context;
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(final ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);

        return new RecipeViewHolder(v, new RecipeViewHolder.ViewHolderClicks() {
            public void showRecipe(View caller, long recipeId) {
                Intent intent = new Intent(context, ViewRecipeActivity.class);
                intent.putExtra(ViewRecipeActivity.BUNDLE_KEY_RECIPE_ID, recipeId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder recipeViewHolder, int i) {
        Recipe recipe = recipes.get(i);
        recipeViewHolder.recipeName.setText(recipe.getName());
        recipeViewHolder.id = recipe.getId();

        RecipeImage coverImage = recipe.getCoverImage();
        if (coverImage == null && recipe.getImages().size() > 0) {
            coverImage = recipe.getImages().get(0);
        }

        if (coverImage != null) {
            Glide.with(context).load(coverImage.getLocation()).centerCrop().into(recipeViewHolder.image);
        } else {
            recipeViewHolder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.no_image));
            recipeViewHolder.image.setScaleType(ImageView.ScaleType.CENTER);
        }

        if (recipe.isFavorite()) {
            recipeViewHolder.favImage.setVisibility(View.VISIBLE);
        } else {
            recipeViewHolder.favImage.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return recipes == null ? 0 : recipes.size();
    }
}

