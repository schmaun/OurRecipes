package de.schmaun.ourrecipes;

import de.schmaun.ourrecipes.Model.Recipe;

public interface RecipeFormInterface {
    public boolean isValid();
    public Recipe getRecipe();
    public void onSaved();
}
