package de.schmaun.ourrecipes.EditRecipe;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
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
import de.schmaun.ourrecipes.Configuration;
import de.schmaun.ourrecipes.DownloadImageTask;
import de.schmaun.ourrecipes.Model.RecipeImage;
import de.schmaun.ourrecipes.PhotoDialogFragment;
import de.schmaun.ourrecipes.R;

import static android.app.Activity.RESULT_OK;

abstract public class EditRecipeWithImageList extends EditRecipeFragment implements PhotoDialogFragment.PictureIntentHandler, DownloadImageTask.DownloadImageHandler {
    protected int layout;
    protected RecyclerView imageListView;
    protected RecipeImageAdapter imageAdapter;
    protected ArrayList<RecipeImage> recipeImages;

    protected static final String STATE_IMAGES = "images";
    protected static final String STATE_DELETED_ITEMS = "deletedImages";
    protected static final String TAG = "EditRecipeImagesF";
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_SELECT_PHOTO = 2;
    protected Uri newPhotoURI;
    private RecipeImageAdapter.ImageListManager imageListManager;

    abstract int getParentImageType();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");

        try {
            imageListManager = (RecipeImageAdapter.ImageListManager) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement RecipeImageAdapter.ImageListManager");
        }
    }

    protected void createView(View rootView, final EditRecipeWithImageList fragment) {
        imageListView = (RecyclerView) rootView.findViewById(R.id.edit_recipe_image_list);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 1);

        imageListView.setLayoutManager(layoutManager);
        imageListView.setItemAnimator(new DefaultItemAnimator());
        imageListView.setHasFixedSize(true);

        Button addImageButton = (Button) rootView.findViewById(R.id.edit_recipe_add_image);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhotoDialogFragment newFragment = new PhotoDialogFragment();
                newFragment.setPictureIntentHandler(fragment);
                newFragment.show(getActivity().getSupportFragmentManager(), "add_photo");
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<RecipeImage> deletedImages = new ArrayList<>();
        if (savedInstanceState != null) {
            Log.d(TAG, "onActivityCreated: savedInstanceState != null");

            recipeImages = Parcels.unwrap(savedInstanceState.getParcelable(STATE_IMAGES));
            deletedImages = Parcels.unwrap(savedInstanceState.getParcelable(STATE_DELETED_ITEMS));
        } else {
            Log.d(TAG, "onActivityCreated: savedInstanceState == null");

            recipeImages = new ArrayList<>();
            if (recipeProvider.getRecipe().getId() != 0) {
                recipeImages = recipeProvider.getRecipe().getImages(getParentImageType());
            }
        }

        imageAdapter = new RecipeImageAdapter(getContext(), recipeImages, deletedImages, imageListView);
        imageAdapter.registerAdapterDataObserver(new EditRecipeImagesObserver(this));
        imageAdapter.registerImageListsManager(imageListManager);
        imageListView.setAdapter(imageAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ImageCardTouchHelperCallback(imageAdapter));
        itemTouchHelper.attachToRecyclerView(imageListView);
    }

    public void resetCoverImageStatus() {
        imageAdapter.resetCoverImageStatus();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_IMAGES, Parcels.wrap(recipeImages));
        outState.putParcelable(STATE_DELETED_ITEMS, Parcels.wrap(imageAdapter.getDeletedImages()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, String.format("onActivityResult requestCode: %d; resultCode:%d", requestCode, resultCode));

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            RecipeImage image = new RecipeImage();
            image.setParentType(getParentImageType());
            image.setFileName(newPhotoURI.getLastPathSegment());
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
                newPhotoURI = FileProvider.getUriForFile(getActivity(), Configuration.FILE_AUTHORITY_IMAGES, photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, newPhotoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        } else {
            Toast.makeText(getContext(), R.string.no_camera_app_available, Toast.LENGTH_LONG).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File storageDir = getActivity().getExternalFilesDir(Configuration.IMAGE_PATH);

        return File.createTempFile(timeStamp, Configuration.IMAGE_ENDING, storageDir);
    }

    public void dispatchSelectPictureFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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

    protected void removeDeletedImageFiles() {
        for (RecipeImage recipeImage : imageAdapter.getDeletedImages()) {
            File file = new File(recipeImage.getLocation(getActivity()));
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
        image.setFileName(file.getName());
        imageAdapter.addImage(image);
    }



    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        Log.d(TAG, String.format("%s: %s", this.getClass(), getMethodName()));
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);

        Log.d(TAG, String.format("%s: %s", this.getClass(), getMethodName()));
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);

        Log.d(TAG, String.format("%s: %s", this.getClass(), getMethodName()));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, String.format("%s: %s", this.getClass(), getMethodName()));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, String.format("%s: %s", this.getClass(), getMethodName()));
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        Log.d(TAG, String.format("%s: %s", this.getClass(), getMethodName()));
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d(TAG, String.format("%s: %s", this.getClass(), getMethodName()));
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, String.format("%s: %s", this.getClass(), getMethodName()));
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, String.format("%s: %s", this.getClass(), getMethodName()));
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d(TAG, String.format("%s: %s", this.getClass(), getMethodName()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Log.d(TAG, String.format("%s: %s", this.getClass(), getMethodName()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, String.format("%s: %s", this.getClass(), getMethodName()));
    }

    @Override
    public void onDetach() {
        super.onDetach();

        Log.d(TAG, String.format("%s: %s", this.getClass(), getMethodName()));
    }

    public static String getMethodName() {
        return Thread.currentThread().getStackTrace()[4].getMethodName();
    }
}
