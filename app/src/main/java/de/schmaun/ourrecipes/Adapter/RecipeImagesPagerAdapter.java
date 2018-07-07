package de.schmaun.ourrecipes.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import de.schmaun.ourrecipes.Model.RecipeImage;
import de.schmaun.ourrecipes.R;
import uk.co.senab.photoview.PhotoViewAttacher;

public class RecipeImagesPagerAdapter extends PagerAdapter {
    public static final String TAG = "RecipeImagesPA";

    private Context context;
    private ArrayList<RecipeImage> recipeImages;
    private LayoutInflater layoutInflater;

    public RecipeImagesPagerAdapter(Context context, ArrayList<RecipeImage> recipeImages) {
        this.context = context;
        this.recipeImages = recipeImages;
        layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return recipeImages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = layoutInflater.inflate(R.layout.dialog_view_images_image, container, false);
        ImageView imageView = (ImageView) itemView.findViewById(R.id.view_image_dialog_image);
        TextView descriptionView = (TextView) itemView.findViewById(R.id.view_image_dialog_description);
        RecipeImage image = recipeImages.get(position);

        try {
            Bitmap bm = MediaStore.Images.Media.getBitmap(this.context.getContentResolver(), Uri.parse(image.getLocation(this.context)));
            imageView.setImageBitmap(bm);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        descriptionView.setText(image.getDescription());

        PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(imageView);
        photoViewAttacher.update();

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }
}