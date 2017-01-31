package de.schmaun.ourrecipes;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class RecipeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView recipeName;
    public TextView recipeDescription;
    public long id;
    private ViewHolderClicks onClickListener;

    public RecipeViewHolder(View itemView, ViewHolderClicks onClickListener) {
        super(itemView);
        itemView.setOnClickListener(this);
        this.onClickListener = onClickListener;
        recipeName = (TextView) itemView.findViewById(R.id.recipeName);
        recipeName.setOnClickListener(this);
        //recipeDescription = (TextView)itemView.findViewById(R.id.recipeDescription);
    }

    @Override
    public void onClick(View v) {
        onClickListener.showRecipe(v, this.id);
    }

    public interface ViewHolderClicks {
        void showRecipe(View caller, long recipeId);
    }
}
