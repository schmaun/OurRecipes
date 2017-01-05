package de.schmaun.ourrecipes.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Collections;
import java.util.List;

import de.schmaun.ourrecipes.EditRecipeActivity;
import de.schmaun.ourrecipes.Model.RecipeImage;
import de.schmaun.ourrecipes.R;

public class RecipeImageAdapter extends RecyclerView.Adapter<RecipeImageAdapter.ImageHolder> implements EditRecipeActivity.EditRecipeImagesFragment.SimpleItemTouchHelperCallback.ItemTouchHelperAdapter {

    private Context context;
    private List<RecipeImage> images;

    public static class ImageHolder extends RecyclerView.ViewHolder implements EditRecipeActivity.EditRecipeImagesFragment.SimpleItemTouchHelperCallback.ItemTouchHelperViewHolder {
        public ImageView deleteButton;
        public TextView imageTextView;
        public ImageView imageView;

        public ImageHolder(View v) {
            super(v);
            imageView = (ImageView) v.findViewById(R.id.recipeImage);
            imageTextView = (TextView) v.findViewById(R.id.recipeImageText);
            deleteButton = (ImageView) v.findViewById(R.id.recipeImageDelete);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
            itemView.setAlpha(0.5f);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
            itemView.setAlpha(1);
        }
    }

    public RecipeImageAdapter(Context context, List<RecipeImage> images) {
        this.context = context;
        this.images = images;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(images, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(images, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        images.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public ImageHolder onCreateViewHolder(final ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recipe_image_row_card, viewGroup, false);

        return new ImageHolder(v);
    }

    @Override
    public void onBindViewHolder(final ImageHolder imageHolder, int i) {
        RecipeImage image = images.get(i);
        Glide.with(context).load(image.getLocation()).centerCrop().into(imageHolder.imageView);

        imageHolder.imageTextView.setText(R.string.edit_recipe_image_description);
        imageHolder.imageTextView.setTypeface(null, Typeface.ITALIC);

        if (image.getDescription() != null && image.getDescription().length() > 0) {
            imageHolder.imageTextView.setText(image.getDescription());
            imageHolder.imageTextView.setTypeface(null, Typeface.NORMAL);
        }

        imageHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemDismiss(imageHolder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return images == null ? 0 : images.size();
    }
}

