package de.schmaun.ourrecipes.EditRecipe;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.schmaun.ourrecipes.Model.Recipe;
import de.schmaun.ourrecipes.R;
import de.schmaun.ourrecipes.RecipeFormInterface;

public class EditRecipeMainFragment extends EditRecipeFragment implements RecipeFormInterface {
    private TextView nameView;
    private TextView ingredientsView;
    private TextView preparationView;

    public static EditRecipeMainFragment newInstance() {
        return new EditRecipeMainFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_recipe_main, container, false);

        nameView = (TextView) rootView.findViewById(R.id.edit_recipe_name);
        ingredientsView = (TextView) rootView.findViewById(R.id.edit_recipe_ingredients);
        preparationView = (TextView) rootView.findViewById(R.id.edit_recipe_preparation);

        Recipe recipe = recipeProvider.getRecipe();
        if (savedInstanceState == null) {
            nameView.setText(recipe.getName());
            ingredientsView.setText(recipe.getIngredients());
            preparationView.setText(recipe.getPreparation());
        }

        nameView.addTextChangedListener(new EditRecipeTextWatcher(this));
        ingredientsView.addTextChangedListener(new EditRecipeTextWatcher(this));
        preparationView.addTextChangedListener(new EditRecipeTextWatcher(this));

        return rootView;
    }

    @Override
    public boolean isValid() {
        if (nameView.length() == 0) {
            nameView.setError(getString(R.string.edit_recipe_name_error_empty));
            return false;
        }

        return true;
    }

    @Override
    public Recipe getRecipe() {
        Recipe recipe = new Recipe();
        recipe.setName(nameView.getText().toString());
        recipe.setIngredients(ingredientsView.getText().toString());
        recipe.setPreparation(preparationView.getText().toString());

        return recipe;
    }

    @Override
    public void onSaved() {

    }
}
