package de.schmaun.ourrecipes.CookingMode;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.schmaun.ourrecipes.Model.Recipe;
import de.schmaun.ourrecipes.Model.RecipeImage;
import de.schmaun.ourrecipes.R;

public class CookingModeIngredientsFragment extends CookingModeWithImages {

    public CookingModeIngredientsFragment() {
    }

    public static CookingModeIngredientsFragment newInstance() {
        return new CookingModeIngredientsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cooking_mode_ingredients, container, false);

        TextView titleView = (TextView) rootView.findViewById(R.id.cooking_mode_recipe_title);
        TextView ingredientsView = (TextView) rootView.findViewById(R.id.cooking_mode_ingredients);

        Recipe recipe = recipeProvider.getRecipe();

        ingredientsView.setText(recipe.getIngredients());
        titleView.setText(recipe.getName());

        createView(rootView);

        return rootView;
    }

    @Override
    int getParentImageType() {
        return RecipeImage.PARENT_TYPE_INGREDIENTS;
    }
}
