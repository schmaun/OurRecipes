package de.schmaun.ourrecipes;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class RecipeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView recipeName;
    public TextView recipeDescription;
    public ImageView image;
    public long id;
    private ViewHolderClicks onClickListener;
    public ImageView favImage;

    public RecipeViewHolder(View itemView, ViewHolderClicks onClickListener) {
        super(itemView);
        itemView.setOnClickListener(this);
        this.onClickListener = onClickListener;
        recipeName = (TextView) itemView.findViewById(R.id.recipeName);
        recipeName.setOnClickListener(this);
        //recipeDescription = (TextView)itemView.findViewById(R.id.recipeDescription);
        image = (ImageView) itemView.findViewById(R.id.recipeImage);
        favImage = (ImageView) itemView.findViewById(R.id.view_recipe_like_button);
    }

    @Override
    public void onClick(View v) {
        onClickListener.showRecipe(v, this.id);
    }

    public interface ViewHolderClicks {
        void showRecipe(View caller, long recipeId);
    }
}
