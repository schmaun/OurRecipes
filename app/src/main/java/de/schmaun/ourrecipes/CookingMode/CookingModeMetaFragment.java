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

public class CookingModeMetaFragment extends Fragment {
    protected RecipeProviderInterface recipeProvider;

    public CookingModeMetaFragment() {
    }

    public static CookingModeMetaFragment newInstance() {
        return new CookingModeMetaFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cooking_mode_meta, container, false);

        TextView titleView = (TextView) rootView.findViewById(R.id.cooking_mode_recipe_title);
        TextView notesView = (TextView) rootView.findViewById(R.id.cooking_mode_notes);

        Recipe recipe = recipeProvider.getRecipe();

        notesView.setText(recipe.getNotes());
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
