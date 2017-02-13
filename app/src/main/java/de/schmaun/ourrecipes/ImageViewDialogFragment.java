package de.schmaun.ourrecipes;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import de.schmaun.ourrecipes.Model.RecipeImage;

public class ImageViewDialogFragment extends DialogFragment {

    private static final String KEY_IMAGE = "image";

    public static ImageViewDialogFragment newInstance(RecipeImage image) {
        ImageViewDialogFragment f = new ImageViewDialogFragment();
        Bundle args = new Bundle();

        args.putString(KEY_IMAGE, image.getLocation());
        f.setArguments(args);

        return (f);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.imageViewDialog);
    }


    @Override
    public void onStart() {
        super.onStart();

        Dialog d = getDialog();
        if (d != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            d.getWindow().setLayout(width, height);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_view_image, container, false);
        ImageView imageView = (ImageView) v.findViewById(R.id.view_image_dialog_image);

        String imageLocation = getArguments().getString(KEY_IMAGE);
        if (imageLocation != null) {
            Glide.with(getContext()).load(imageLocation).fitCenter().into(imageView);
        }

        return v;
    }
}