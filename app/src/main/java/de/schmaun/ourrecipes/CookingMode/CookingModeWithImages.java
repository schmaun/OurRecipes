package de.schmaun.ourrecipes.CookingMode;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import de.schmaun.ourrecipes.Model.Recipe;
import de.schmaun.ourrecipes.Model.RecipeImage;
import de.schmaun.ourrecipes.R;
import de.schmaun.ourrecipes.RecipeProviderInterface;

public abstract class CookingModeWithImages extends Fragment {
    protected RecipeProviderInterface recipeProvider;
    protected RecyclerView imageListView;
    protected CookingModeRecipeImageAdapter imageAdapter;
    protected Recipe recipe;

    abstract int getParentImageType();

    protected void createView(View rootView) {
        imageListView = (RecyclerView) rootView.findViewById(R.id.cooking_mode_recipe_image_list);
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 1);

        imageListView.setLayoutManager(layoutManager);
        //imageListView.setHasFixedSize(true);

        imageAdapter = new CookingModeRecipeImageAdapter(getContext(), recipeProvider.getRecipe().getImages(getParentImageType()));
        imageListView.setAdapter(imageAdapter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
