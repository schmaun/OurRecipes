package de.schmaun.ourrecipes.EditRecipe;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.schmaun.ourrecipes.Model.Recipe;
import de.schmaun.ourrecipes.Model.RecipeImage;
import de.schmaun.ourrecipes.R;

public class EditRecipePreparationFragment extends EditRecipeWithImageList {
    private TextView preparationView;

    public EditRecipePreparationFragment() {
    }

    public static EditRecipePreparationFragment newInstance() {
        return new EditRecipePreparationFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_recipe_preparation, container, false);

        createView(rootView, this);

        preparationView = (TextView) rootView.findViewById(R.id.edit_recipe_preparation);
        Recipe recipe = recipeProvider.getRecipe();
        if (savedInstanceState == null) {
            preparationView.setText(recipe.getPreparation());
        }
        preparationView.addTextChangedListener(new EditRecipeTextWatcher(this));

        return rootView;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Recipe getRecipe() {
        Recipe recipe = new Recipe();
        recipe.setPreparation(preparationView.getText().toString());
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
        return RecipeImage.PARENT_TYPE_PREPARATION;
    }
}