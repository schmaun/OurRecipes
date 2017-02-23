package de.schmaun.ourrecipes;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import de.schmaun.ourrecipes.Adapter.RecipeImagesPagerAdapter;
import de.schmaun.ourrecipes.Model.Recipe;
import de.schmaun.ourrecipes.Model.RecipeImage;

public class ImageViewDialogFragment extends DialogFragment {

    private RecipeProviderInterface recipeProvider;
    private int startImage;
    private RecipeImage currentImage;

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
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_view_image, container, false);

        Recipe recipe = recipeProvider.getRecipe();
        ArrayList<RecipeImage> images = new ArrayList<RecipeImage>();
        if (recipe != null) {
            images = recipe.getImages();
        }
        currentImage = images.get(startImage);

        Toolbar toolbar = (Toolbar) v.findViewById(R.id.dialog_view_image_action_bar);
 /*       toolbar.inflateMenu(R.menu.menu_view_image);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent editIntent = new Intent(Intent.ACTION_EDIT);
                //Uri uri = FileProvider.getUriForFile(getActivity(), "de.schmaun.fileprovider", photoFile);
                Uri uri = Uri.parse(currentImage.getLocation());

                editIntent.setDataAndType(uri, "image*//*");
                startActivity(Intent.createChooser(editIntent, null));
                return true;
            }
        });*/
        toolbar.setTitle(getString(R.string.dialog_view_image_title, startImage + 1, images.size()));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        RecipeImagesPagerAdapter adapter = new RecipeImagesPagerAdapter(getContext(), images);
        ViewPager viewPager = (ViewPager) v.findViewById(R.id.view_image_dialog_pager);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(startImage);
        viewPager.addOnPageChangeListener(new OnPageChangeListener(toolbar, images));

        return v;
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

    private class OnPageChangeListener implements ViewPager.OnPageChangeListener {
        private final Toolbar toolbar;
        private final ArrayList<RecipeImage> images;

        public OnPageChangeListener(Toolbar toolbar, ArrayList<RecipeImage> images) {
            this.toolbar = toolbar;
            this.images = images;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            this.toolbar.setTitle(getString(R.string.dialog_view_image_title, position + 1, images.size()));
            currentImage = images.get(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }
}