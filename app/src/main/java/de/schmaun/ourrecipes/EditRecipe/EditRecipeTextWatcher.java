package de.schmaun.ourrecipes.EditRecipe;

import android.text.Editable;
import android.text.TextWatcher;

public class EditRecipeTextWatcher implements TextWatcher {
    private EditRecipeFragment fragment;

    public EditRecipeTextWatcher(EditRecipeFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        fragment.setUnsavedChanges(true);
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}
