package de.schmaun.ourrecipes;

import java.util.ArrayList;

import de.schmaun.ourrecipes.Model.RecipeImage;

interface RecipeChangedListener {
    void onNameChange(String name);
    void onDescriptionChange(String description);
    void onIngredientsChange(String ingredients);
    void onPreparationChange(String preparation);
    void onImageAdded(RecipeImage image);
    void onImageDeleted(RecipeImage image);
    void onImageMoved(ArrayList<RecipeImage> recipeImages);
    void onLabelChanged();
}