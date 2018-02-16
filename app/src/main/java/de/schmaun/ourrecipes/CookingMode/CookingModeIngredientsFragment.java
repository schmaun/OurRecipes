package de.schmaun.ourrecipes.CookingMode;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.schmaun.ourrecipes.Model.Recipe;
import de.schmaun.ourrecipes.R;
import de.schmaun.ourrecipes.RecipeProviderInterface;

public class CookingModeIngredientsFragment extends Fragment {
    protected RecipeProviderInterface recipeProvider;

    public CookingModeIngredientsFragment() {
    }

    public static CookingModeIngredientsFragment newInstance() {
        return new CookingModeIngredientsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            recipeProvider = (RecipeProviderInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement RecipeProviderInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
