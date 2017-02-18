package de.schmaun.ourrecipes;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import de.schmaun.ourrecipes.Adapter.RecipeImagesPagerAdapter;
import de.schmaun.ourrecipes.Model.Recipe;
import de.schmaun.ourrecipes.Model.RecipeImage;

public class ImageViewDialogFragment extends DialogFragment {

    private  RecipeProviderInterface recipeProvider;
    private  int startImage;

    public static ImageViewDialogFragment newInstance(RecipeProviderInterface recipeProvider, int startImage) {
        ImageViewDialogFragment f = new ImageViewDialogFragment();
        f.recipeProvider = recipeProvider;
        f.startImage = startImage;

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.imageViewDialog);
    }


    @Override
    public void onStart() {
        super.onStart();
/*
        Dialog d = getDialog();
        if (d != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            d.getWindow().setLayout(width, height);
        }
        */

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_view_image, container, false);

        Recipe recipe = recipeProvider.getRecipe();
        ArrayList<RecipeImage> images = new ArrayList<RecipeImage>();
        if (recipe != null) {
            images = recipe.getImages();
        }

        RecipeImagesPagerAdapter adapter = new RecipeImagesPagerAdapter(getContext(), images);
        ViewPager viewPager = (ViewPager) v.findViewById(R.id.view_image_dialog_pager);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(startImage);

        return v;
    }
}