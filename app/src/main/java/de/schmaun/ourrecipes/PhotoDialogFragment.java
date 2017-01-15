package de.schmaun.ourrecipes;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ViewDebug;
import android.widget.ListAdapter;

import de.schmaun.ourrecipes.Adapter.ArrayWithIconAdapter;

public class PhotoDialogFragment extends DialogFragment {

    private String TAG = "PhotoDialogFragment";
    private PictureIntentHandler pictureIntentHandler;

    interface PictureIntentHandler {
        void dispatchTakePicture();
        void dispatchSelectPictureFromGallery();
        void dispatchSelectPictureFromStorageAccessFramework();
    }

    public void setPictureIntentHandler(PictureIntentHandler pictureIntentHandler) {
        this.pictureIntentHandler = pictureIntentHandler;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Resources resources = getResources();
        String[] items = resources.getStringArray(R.array.edit_recipe_add_image_items);
        Integer[] icons = new Integer[] {R.drawable.ic_image_photo_camera, R.drawable.ic_editor_insert_photo, R.drawable.ic_editor_insert_photo};
        ListAdapter adapter = new ArrayWithIconAdapter(getActivity(), items, icons);

        builder.setTitle(R.string.edit_recipe_add_image)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, Integer.toString(which) + " clicked");
                        switch (which) {
                            case 0:
                                pictureIntentHandler.dispatchTakePicture();
                                break;
                            case 1:
                                pictureIntentHandler.dispatchSelectPictureFromGallery();
                                break;
                            case 2:
                                pictureIntentHandler.dispatchSelectPictureFromStorageAccessFramework();
                                break;
                        }
                    }
                });

        return builder.create();
    }
}