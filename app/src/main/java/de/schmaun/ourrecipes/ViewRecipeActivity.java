package de.schmaun.ourrecipes;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.iojjj.rcbs.RoundedCornersBackgroundSpan;
import com.like.LikeButton;

import de.schmaun.ourrecipes.Adapter.SimpleRecipeImageAdapter;
import de.schmaun.ourrecipes.Model.Label;
import de.schmaun.ourrecipes.Model.RecipeImage;

public class ViewRecipeActivity extends RecipeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recipe);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadRecipeOnCreate(savedInstanceState, true);

        fillView();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), CookingModeActivity.class);
                intent.putExtra(CookingModeActivity.BUNDLE_KEY_RECIPE_ID, recipe.getId());
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        loadRecipe(recipeId);
        fillView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_recipe, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_edit:
                Intent intent = new Intent(this, EditRecipeActivity.class);
                intent.putExtra(EditRecipeActivity.BUNDLE_KEY_RECIPE_ID, recipe.getId());
                startActivity(intent);
                break;
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fillView() {
        ImageView coverImageView = (ImageView) findViewById(R.id.view_recipe_cover_image);
        CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.view_recipe_toolbar_layout);

        TextView labelsView = (TextView) findViewById(R.id.view_recipe_labels);
        TextView ingredientsView = (TextView) findViewById(R.id.view_recipe_ingredients);
        TextView preparationView = (TextView) findViewById(R.id.view_recipe_preparation);
        TextView notesView = (TextView) findViewById(R.id.view_recipe_notes);
        RecyclerView imagesView = (RecyclerView) findViewById(R.id.view_recipe_images);

        TextView ingredientsHeadlineView = (TextView) findViewById(R.id.view_recipe_ingredients_headline);
        TextView preparationHeadlineView = (TextView) findViewById(R.id.view_recipe_preparation_headline);
        TextView notesHeadlineView = (TextView) findViewById(R.id.view_recipe_notes_headline);

        if (recipe.getIngredients().length() == 0) {
            ingredientsHeadlineView.setVisibility(View.GONE);
            ingredientsView.setVisibility(View.GONE);
        }
        if (recipe.getPreparation().length() == 0) {
            preparationHeadlineView.setVisibility(View.GONE);
            preparationView.setVisibility(View.GONE);
        }
        if (recipe.getNotes().length() == 0) {
            notesHeadlineView.setVisibility(View.GONE);
            notesView.setVisibility(View.GONE);
        }
        if (recipe.getLabels().isEmpty()) {
            labelsView.setVisibility(View.GONE);
        } else {
            labelsView.setText(getFormattedLabels());
        }

        RecipeImage coverImage = recipe.getCoverImage();
        if (coverImage != null) {
            Glide.with(this).load(coverImage.getLocation()).apply(new RequestOptions().centerCrop()).into(coverImageView);
        } else {
            coverImageView.setImageDrawable(getResources().getDrawable(R.drawable.no_image));
            coverImageView.setPadding(0, 0, 0, 200);
        }

        toolbarLayout.setTitle(recipe.getName());
        ingredientsView.setText(recipe.getIngredients());
        preparationView.setText(recipe.getPreparation());
        notesView.setText(recipe.getNotes());
        SimpleRecipeImageAdapter imageAdapter = new SimpleRecipeImageAdapter(this, recipe);

        imagesView.setHasFixedSize(true);
        imagesView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imagesView.setAdapter(imageAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(imagesView.getContext(), LinearLayoutManager.HORIZONTAL);
        imagesView.addItemDecoration(dividerItemDecoration);

        LikeButton likeButton = (LikeButton) findViewById(R.id.view_recipe_like_button);
        likeButton.setOnLikeListener(new LikeButtonOnClickListener(this));
        likeButton.setLiked(recipe.isFavorite());
    }

    private static float convertDpToPx(Context context, float dp) {
        return context.getResources().getDisplayMetrics().density * dp;
    }

    private Spannable getFormattedLabels() {
        RoundedCornersBackgroundSpan.TextPartsBuilder textPartsBuilder = new RoundedCornersBackgroundSpan.TextPartsBuilder(this)
                .setTextPadding(convertDpToPx(this, 4))
                .setCornersRadius(convertDpToPx(this, 4))
                .setSeparator(RoundedCornersBackgroundSpan.DEFAULT_SEPARATOR);

        for (Label label : recipe.getLabels()) {
            String shortenedLabel = label.getName();
            final SpannableString string = new SpannableString(shortenedLabel);
            final ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.WHITE);
            string.setSpan(colorSpan, 0, string.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textPartsBuilder.addTextPart(string, getResources().getColor(R.color.recipeLabelBackground));
        }

        return textPartsBuilder.build();
    }
}
