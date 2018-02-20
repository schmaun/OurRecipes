package de.schmaun.ourrecipes.CookingMode;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.schmaun.ourrecipes.Model.Recipe;
import de.schmaun.ourrecipes.Model.RecipeImage;
import de.schmaun.ourrecipes.R;

public class CookingModePreparationFragment extends CookingModeWithImages {

    public CookingModePreparationFragment() {
    }

    public static CookingModePreparationFragment newInstance() {
        return new CookingModePreparationFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cooking_mode_preparation, container, false);

        TextView titleView = (TextView) rootView.findViewById(R.id.cooking_mode_recipe_title);
        TextView preparationStepsView = (TextView) rootView.findViewById(R.id.cooking_mode_preparation);

        Recipe recipe = recipeProvider.getRecipe();

        preparationStepsView.setText(recipe.getPreparation());
        titleView.setText(recipe.getName());

        createView(rootView);

        return rootView;
    }

    @Override
    int getParentImageType() {
        return RecipeImage.PARENT_TYPE_PREPARATION;
    }
}
