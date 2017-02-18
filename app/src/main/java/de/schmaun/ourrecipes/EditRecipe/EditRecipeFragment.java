package de.schmaun.ourrecipes.EditRecipe;

import android.content.Context;
import android.support.v4.app.Fragment;

import de.schmaun.ourrecipes.RecipeFormInterface;
import de.schmaun.ourrecipes.RecipeProviderInterface;

abstract class EditRecipeFragment extends Fragment implements RecipeFormInterface {
    protected RecipeProviderInterface recipeProvider;
    protected boolean unsavedChanges;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            recipeProvider = (RecipeProviderInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement RecipeProviderInterface");
        }
    }

    public boolean hasUnsavedChanges() {
        return unsavedChanges;
    }

    public void setUnsavedChanges(boolean unsavedChanges) {
        this.unsavedChanges = unsavedChanges;
    }
}