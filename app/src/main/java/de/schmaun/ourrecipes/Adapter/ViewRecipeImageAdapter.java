package de.schmaun.ourrecipes.Adapter;

import android.content.Context;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import de.schmaun.ourrecipes.ImageViewDialogFragment;
import de.schmaun.ourrecipes.Model.Recipe;
import de.schmaun.ourrecipes.Model.RecipeImage;
import de.schmaun.ourrecipes.R;
import de.schmaun.ourrecipes.RecipeProviderInterface;

public class ViewRecipeImageAdapter extends RecyclerView.Adapter<ViewRecipeImageAdapter.ImageHolder> {

    private Context context;
    private List<RecipeImage> images;

    static class ImageHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageHolder(View v) {
            super(v);
            imageView = (ImageView) v.findViewById(R.id.recipeImage);
        }
    }

    public ViewRecipeImageAdapter(Context context, Recipe recipe) {
        this.context = context;
        this.images = recipe.getImagesGroupedByParentType();
    }

    @Override
    public ImageHolder onCreateViewHolder(final ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recipe_image_row, viewGroup, false);

        return new ImageHolder(v);
    }

    @Override
    public void onBindViewHolder(final ImageHolder imageHolder, int i) {
        final RecipeImage image = images.get(i);
        final int currentImagePosition = i;

        imageHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                ImageViewDialogFragment imageViewDialog = ImageViewDialogFragment.newInstance((RecipeProviderInterface)context, currentImagePosition);
                imageViewDialog.show(transaction, "imageViewDialog");
            }
        });

        Glide.with(context).load(image.getLocation(context)).apply(new RequestOptions().centerCrop()).into(imageHolder.imageView);
    }

    @Override
    public int getItemCount() {
        return images == null ? 0 : images.size();
    }
}

