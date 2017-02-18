package de.schmaun.ourrecipes;

import de.schmaun.ourrecipes.Model.Recipe;

public interface RecipeFormInterface {
    boolean isValid();
    Recipe getRecipe();
    void onSaved();
    boolean hasUnsavedChanges();
}
