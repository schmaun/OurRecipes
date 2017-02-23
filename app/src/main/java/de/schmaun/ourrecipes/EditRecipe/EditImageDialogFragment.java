package de.schmaun.ourrecipes.EditRecipe;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;

import java.lang.reflect.Array;
import java.util.ArrayList;

import de.schmaun.ourrecipes.Adapter.RecipeImagesPagerAdapter;
import de.schmaun.ourrecipes.Model.Recipe;
import de.schmaun.ourrecipes.Model.RecipeImage;
import de.schmaun.ourrecipes.R;
import de.schmaun.ourrecipes.RecipeProviderInterface;

public class EditImageDialogFragment extends DialogFragment {

    private int startImage;
    private RecipeImage currentImage;
    private RecipeImageProvider imageProvider;

    public interface RecipeImageProvider {
        public void onImageDescriptionChange(RecipeImage image);
        public ArrayList<RecipeImage> getImages();
    }

    public static EditImageDialogFragment newInstance(RecipeImageProvider imageProvider, int startImage) {
        EditImageDialogFragment f = new EditImageDialogFragment();
        f.imageProvider = imageProvider;
        f.startImage = startImage;

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ArrayList<RecipeImage> images = imageProvider.getImages();
        currentImage = images.get(startImage);

        return new MaterialDialog.Builder(getContext())
                .title("Image description")
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                .input(null, currentImage.getDescription(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        currentImage.setDescription(input.toString());
                        imageProvider.onImageDescriptionChange(currentImage);
                    }
                }).show();
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        // handles https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }
}