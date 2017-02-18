package de.schmaun.ourrecipes;

import de.schmaun.ourrecipes.Model.Recipe;

public interface RecipeProviderInterface {
    Recipe getRecipe();

    void setHasUnsavedChanges(boolean unsavedChanges);
}
