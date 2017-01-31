package de.schmaun.ourrecipes.EditRecipe;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.schmaun.ourrecipes.Adapter.RecipeImageAdapter;
import de.schmaun.ourrecipes.DownloadImageTask;
import de.schmaun.ourrecipes.Model.Recipe;
import de.schmaun.ourrecipes.Model.RecipeImage;
import de.schmaun.ourrecipes.PhotoDialogFragment;
import de.schmaun.ourrecipes.R;
import de.schmaun.ourrecipes.RecipeFormInterface;

import static android.app.Activity.RESULT_OK;

public class EditRecipeImagesFragment extends EditRecipeFragment implements RecipeFormInterface, PhotoDialogFragment.PictureIntentHandler, DownloadImageTask.DownloadImageHandler {
    private RecyclerView imageListView;
    private RecipeImageAdapter imageAdapter;
    private ArrayList<RecipeImage> recipeImages;

    private static final String STATE_ITEMS = "items";
    private static final String TAG = "EditRecipeImagesF";
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_SELECT_PHOTO = 2;
    private Uri newPhotoURI;

    public EditRecipeImagesFragment() {
    }

    public static EditRecipeImagesFragment newInstance() {
        return new EditRecipeImagesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_edit_recipe_images, container, false);

        imageListView = (RecyclerView) rootView.findViewById(R.id.edit_recipe_image_list);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 1);

        imageListView.setLayoutManager(layoutManager);
        imageListView.setItemAnimator(new DefaultItemAnimator());
        imageListView.setHasFixedSize(true);

        final EditRecipeImagesFragment fragment = this;
        Button addImageButton = (Button) rootView.findViewById(R.id.edit_recipe_add_image);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhotoDialogFragment newFragment = new PhotoDialogFragment();
                newFragment.setPictureIntentHandler(fragment);
                newFragment.show(getActivity().getSupportFragmentManager(), "add_photo");
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            recipeImages = Parcels.unwrap(savedInstanceState.getParcelable(STATE_ITEMS));
        } else {
            recipeImages = new ArrayList<>();
            if (recipeProvider.getRecipe().getId() != 0) {
                recipeImages = recipeProvider.getRecipe().getImages();
            }
        }

        imageAdapter = new RecipeImageAdapter(getContext(), recipeImages, imageListView);
        imageListView.setAdapter(imageAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SimpleItemTouchHelperCallback(imageAdapter));
        itemTouchHelper.attachToRecyclerView(imageListView);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_ITEMS, Parcels.wrap(recipeImages));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, String.format("onActivityResult requestCode: %d; resultCode:%d", requestCode, resultCode));

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            RecipeImage image = new RecipeImage();
            image.setLocation(newPhotoURI.toString());
            imageAdapter.addImage(image);
        }

        if (requestCode == REQUEST_SELECT_PHOTO && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            Log.i(TAG, "Uri: " + uri.toString());

            try {
                File file = createImageFile();
                new DownloadImageTask(getContext(), this, file).execute(uri);
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void dispatchTakePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(getContext(), R.string.error_taking_picture, Toast.LENGTH_LONG).show();
            }

            if (photoFile != null) {
                newPhotoURI = FileProvider.getUriForFile(getActivity(), "de.schmaun.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, newPhotoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        } else {
            Toast.makeText(getContext(), R.string.no_camera_app_available, Toast.LENGTH_LONG).show();
        }
    }

    public void dispatchSelectPictureFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startSelectPictureIntent(photoPickerIntent);
    }

    public void dispatchSelectPictureFromStorageAccessFramework() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        startSelectPictureIntent(photoPickerIntent);
    }

    private void startSelectPictureIntent(Intent intent) {
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_SELECT_PHOTO);
        } else {
            Toast.makeText(getContext(), R.string.no_gallery_app_available, Toast.LENGTH_LONG).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File storageDir = getActivity().getExternalFilesDir("images");

        return File.createTempFile(timeStamp, ".jpg", storageDir);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Recipe getRecipe() {
        Recipe recipe = new Recipe();
        recipe.setImages(recipeImages);
        recipe.setImagesToDelete(imageAdapter.getDeletedImages());

        return recipe;
    }

    @Override
    public void onSaved() {
        removeDeletedImageFiles();
    }

    protected void removeDeletedImageFiles() {
        for (RecipeImage recipeImage : imageAdapter.getDeletedImages()) {
            File file = new File(recipeImage.getLocation());
            boolean deleted = file.delete();

            Log.d("deleteImageFile", Boolean.toString(deleted));
        }
    }

    @Override
    public void onError(Exception error) {
        Toast.makeText(getContext(), R.string.error_saving_image, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSuccess(File file) {
        RecipeImage image = new RecipeImage();
        image.setLocation(file.getAbsolutePath());
        imageAdapter.addImage(image);
    }

    public static class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {
        public interface ItemTouchHelperAdapter {
            void onItemMove(int fromPosition, int toPosition);

            void onItemDismiss(int position);
        }

        /**
         * Notifies a View Holder of relevant callbacks from
         * {@link ItemTouchHelper.Callback}.
         */
        public interface ItemTouchHelperViewHolder {

            /**
             * Called when the {@link ItemTouchHelper} first registers an
             * item as being moved or swiped.
             * Implementations should update the item view to indicate
             * it's active state.
             */
            void onItemSelected();

            /**
             * Called when the {@link ItemTouchHelper} has completed the
             * move or swipe, and the active item state should be cleared.
             */
            void onItemClear();
        }

        private final ItemTouchHelperAdapter mAdapter;

        SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;

            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                if (viewHolder instanceof ItemTouchHelperViewHolder) {
                    ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
                    itemViewHolder.onItemSelected();
                }
            }

            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);

            if (viewHolder instanceof ItemTouchHelperViewHolder) {
                ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
                itemViewHolder.onItemClear();
            }
        }
    }
}