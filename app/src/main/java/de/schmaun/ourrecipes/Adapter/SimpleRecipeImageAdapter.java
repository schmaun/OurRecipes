package de.schmaun.ourrecipes.Adapter;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.schmaun.ourrecipes.ImageViewDialogFragment;
import de.schmaun.ourrecipes.Model.RecipeImage;
import de.schmaun.ourrecipes.R;

public class SimpleRecipeImageAdapter extends RecyclerView.Adapter<SimpleRecipeImageAdapter.ImageHolder> {

    private Context context;
    private List<RecipeImage> images;

    static class ImageHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageHolder(View v) {
            super(v);
            imageView = (ImageView) v.findViewById(R.id.recipeImage);
        }
    }

    public SimpleRecipeImageAdapter(Context context, List<RecipeImage> images) {
        this.context = context;
        this.images = images;
    }

    @Override
    public ImageHolder onCreateViewHolder(final ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recipe_image_row, viewGroup, false);

        return new ImageHolder(v);
    }

    @Override
    public void onBindViewHolder(final ImageHolder imageHolder, int i) {
        final RecipeImage image = images.get(i);

        imageHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                ImageViewDialogFragment frag = ImageViewDialogFragment.newInstance(image);

                frag.show(ft, "txn_tag");

                /*
                ImageViewDialogFragment newFragment = new ImageViewDialogFragment();
                newFragment.setImage(image);
                newFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "add_photo");
                */
            }
        });

        Glide.with(context).load(image.getLocation()).centerCrop().into(imageHolder.imageView);
    }

    @Override
    public int getItemCount() {
        return images == null ? 0 : images.size();
    }
}

