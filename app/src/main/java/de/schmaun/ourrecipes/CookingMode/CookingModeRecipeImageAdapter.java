package de.schmaun.ourrecipes.CookingMode;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.schmaun.ourrecipes.ImageViewDialogFragment;
import de.schmaun.ourrecipes.Model.Recipe;
import de.schmaun.ourrecipes.Model.RecipeImage;
import de.schmaun.ourrecipes.R;
import de.schmaun.ourrecipes.RecipeProviderInterface;

public class CookingModeRecipeImageAdapter extends RecyclerView.Adapter<CookingModeRecipeImageAdapter.ImageHolder> {

    private Context context;
    private List<RecipeImage> images;

    static class ImageHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageHolder(View v) {
            super(v);
            imageView = (ImageView) v.findViewById(R.id.recipeImage);
        }
    }

    public CookingModeRecipeImageAdapter(Context context, List<RecipeImage> images) {
        this.context = context;
        this.images = images;
    }

    @Override
    public ImageHolder onCreateViewHolder(final ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cooking_mode_recipe_image_row, viewGroup, false);

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
                ImageViewDialogFragment imageViewDialog = ImageViewDialogFragment.newInstance((RecipeProviderInterface) context, currentImagePosition, image.getParentType(), ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                imageViewDialog.show(transaction, "imageViewDialog");
            }
        });

        Glide.with(context).load(image.getLocation()).fitCenter().into(imageHolder.imageView);
    }

    @Override
    public int getItemCount() {
        return images == null ? 0 : images.size();
    }
}

