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

    public static EditRecipeMainFragment newInstance() {
        return new EditRecipeMainFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_recipe_main, container, false);



        return rootView;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Recipe getRecipe() {
        Recipe recipe = new Recipe();

        return recipe;
    }

    @Override
    public void onSaved() {

    }
}
