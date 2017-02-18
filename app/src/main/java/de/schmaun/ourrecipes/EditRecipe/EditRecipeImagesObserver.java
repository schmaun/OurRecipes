package de.schmaun.ourrecipes.EditRecipe;

import android.support.v7.widget.RecyclerView;

public class EditRecipeImagesObserver extends RecyclerView.AdapterDataObserver {
    private EditRecipeImagesFragment fragment;

    public EditRecipeImagesObserver(EditRecipeImagesFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onChanged() {
        fragment.setUnsavedChanges(true);
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
        fragment.setUnsavedChanges(true);
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
        fragment.setUnsavedChanges(true);
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        fragment.setUnsavedChanges(true);
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
        fragment.setUnsavedChanges(true);
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        fragment.setUnsavedChanges(true);
    }
}
