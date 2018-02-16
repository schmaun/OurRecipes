package de.schmaun.ourrecipes.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.schmaun.ourrecipes.EditRecipe.EditImageDialogFragment;
import de.schmaun.ourrecipes.EditRecipe.ImageCardTouchHelperCallback;
import de.schmaun.ourrecipes.ImageViewDialogFragment;
import de.schmaun.ourrecipes.Model.RecipeImage;
import de.schmaun.ourrecipes.R;
import de.schmaun.ourrecipes.RecipeProviderInterface;

public class RecipeImageAdapter extends RecyclerView.Adapter<RecipeImageAdapter.ImageHolder> implements ImageCardTouchHelperCallback.ItemTouchHelperAdapter, EditImageDialogFragment.RecipeImageProvider {

    private Context context;
    private ArrayList<RecipeImage> images;
    private View rootView;

    private ArrayList<RecipeImage> deletedImages = new ArrayList<>();
    private RecipeImage deletedRecipeImage;
    private int deletedRecipeImagePosition;
    private ImageListManager imageListManager;

    public void registerImageListsManager(ImageListManager imageListManager) {
        this.imageListManager = imageListManager;
    }

    public interface ImageListManager {
        void resetCoverImageStatus();
    }

    static class ImageHolder extends RecyclerView.ViewHolder implements ImageCardTouchHelperCallback.ItemTouchHelperViewHolder {
        Button deleteButton;
        Button coverButton;
        TextView imageTextView;
        ImageView imageView;

        ImageHolder(View v) {
            super(v);
            imageView = (ImageView) v.findViewById(R.id.recipeImage);
            imageTextView = (TextView) v.findViewById(R.id.recipeImageText);
            deleteButton = (Button) v.findViewById(R.id.recipeImageDelete);
            coverButton = (Button) v.findViewById(R.id.recipeImageSetAsCover);
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

    public RecipeImageAdapter(Context context, ArrayList<RecipeImage> images, ArrayList<RecipeImage> deletedImages, View rootView) {
        this.context = context;
        this.images = images;
        this.deletedImages = deletedImages;
        this.rootView = rootView;
    }

    public void addImage(RecipeImage image) {
        this.images.add(image);
        if (this.images.size() == 1) {
            onSetAsCoverImage(image);
        }

        notifyDataSetChanged();
    }

    @Override
    public void onImageDescriptionChange(RecipeImage image) {
        notifyDataSetChanged();
    }

    @Override
    public ArrayList<RecipeImage> getImages() {
        return images;
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
        deletedRecipeImage = images.get(position);
        deletedRecipeImagePosition = position;

        deletedImages.add(deletedRecipeImage);
        images.remove(position);
        notifyItemRemoved(position);

        if (images.size() == 1) {
            onSetAsCoverImage(0);
        }

        Snackbar snackbar = Snackbar.make(this.rootView, R.string.edit_recipe_image_deleted, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.edit_recipe_image_deleted_undo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                images.add(deletedRecipeImagePosition, deletedRecipeImage);
                deletedImages.remove(deletedRecipeImage);

                if (deletedRecipeImage.isCoverImage()) {
                    onSetAsCoverImage(deletedRecipeImage);
                }

                notifyItemInserted(deletedRecipeImagePosition);
            }
        });
        snackbar.show();
    }

    private void onSetAsCoverImage(int position) {
        onSetAsCoverImage(images.get(position));
    }

    private void onSetAsCoverImage(RecipeImage image) {
        imageListManager.resetCoverImageStatus();

        image.setCoverImage(true);
        notifyDataSetChanged();
    }

    public void resetCoverImageStatus() {
        for(RecipeImage notCoverImage: images) {
            notCoverImage.setCoverImage(false);
        }
        notifyDataSetChanged();
    }

    @Override
    public ImageHolder onCreateViewHolder(final ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recipe_image_row_card, viewGroup, false);

        return new ImageHolder(v);
    }

    @Override
    public void onBindViewHolder(final ImageHolder imageHolder, int i) {
        final int position = i;
        final RecipeImageAdapter that = this;

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

        imageHolder.coverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSetAsCoverImage(imageHolder.getAdapterPosition());
            }
        });

        imageHolder.coverButton.setEnabled(true);
        if(image.isCoverImage()) {
            imageHolder.coverButton.setEnabled(false);
        }

        imageHolder.imageTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                EditImageDialogFragment editImageDialog = EditImageDialogFragment.newInstance(that, position);
                editImageDialog.show(transaction, "editImageDialog");
            }
        });
    }

    @Override
    public int getItemCount() {
        return images == null ? 0 : images.size();
    }

    public ArrayList<RecipeImage> getDeletedImages() {
        return deletedImages;
    }

    public void setDeletedImages(ArrayList<RecipeImage> deletedImages) {
        this.deletedImages = deletedImages;
    }
}

