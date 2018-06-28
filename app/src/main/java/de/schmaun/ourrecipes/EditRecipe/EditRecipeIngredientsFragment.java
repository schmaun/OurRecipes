package de.schmaun.ourrecipes.EditRecipe;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.schmaun.ourrecipes.Model.Recipe;
import de.schmaun.ourrecipes.Model.RecipeImage;
import de.schmaun.ourrecipes.R;

public class EditRecipeIngredientsFragment extends EditRecipeWithImageList {
    private TextView ingredientsView;

    public EditRecipeIngredientsFragment() {
    }

    public static EditRecipeIngredientsFragment newInstance() {
        return new EditRecipeIngredientsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_recipe_ingredients, container, false);

        createView(rootView, this);

        ingredientsView = (TextView) rootView.findViewById(R.id.edit_recipe_ingredients);
        Recipe recipe = recipeProvider.getRecipe();
        if (savedInstanceState == null) {
            ingredientsView.setText(recipe.getIngredients());
        }
        ingredientsView.addTextChangedListener(new EditRecipeTextWatcher(this));

        return rootView;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Recipe getRecipe() {
        Recipe recipe = new Recipe();
        recipe.setIngredients(ingredientsView.getText().toString());
        recipe.setImages(recipeImages);
        recipe.setImagesToDelete(imageAdapter.getDeletedImages());

        return recipe;
    }

    @Override
    public void onSaved() {
        removeDeletedImageFiles();
    }

    @Override
    int getParentImageType() {
        return RecipeImage.PARENT_TYPE_INGREDIENTS;
    }
}