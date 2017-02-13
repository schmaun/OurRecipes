package de.schmaun.ourrecipes;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
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
import com.github.iojjj.rcbs.RoundedCornersBackgroundSpan;

import de.schmaun.ourrecipes.Adapter.SimpleRecipeImageAdapter;
import de.schmaun.ourrecipes.Model.Label;
import de.schmaun.ourrecipes.Model.RecipeImage;

import static android.support.design.R.attr.layoutManager;

public class ViewRecipeActivity extends RecipeActivity {

    private TextView labelsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recipe);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView coverImageView = (ImageView) findViewById(R.id.view_recipe_cover_image);
        CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.view_recipe_toolbar_layout);

        labelsView = (TextView) findViewById(R.id.view_recipe_labels);
        TextView ingredientsView = (TextView) findViewById(R.id.view_recipe_ingredients);
        TextView preparationView = (TextView) findViewById(R.id.view_recipe_preparation);
        TextView notesView = (TextView) findViewById(R.id.view_recipe_notes);
        RecyclerView imagesView = (RecyclerView) findViewById(R.id.view_recipe_images);


        loadRecipe(savedInstanceState, true);

        TextView ingredientsHeadlineView = (TextView) findViewById(R.id.view_recipe_ingredients_headline);
        TextView preparationHeadlineView = (TextView) findViewById(R.id.view_recipe_preparation_headline);
        TextView notesHeadlineView = (TextView) findViewById(R.id.view_recipe_notes_headline);

        if (recipe.getIngredients().length() == 0) {
            ingredientsHeadlineView.setVisibility(View.GONE);
        }
        if (recipe.getPreparation().length() == 0) {
            preparationHeadlineView.setVisibility(View.GONE);
        }
        if (recipe.getNotes().length() == 0) {
            notesHeadlineView.setVisibility(View.GONE);
        }

        RecipeImage coverImage = recipe.getCoverImage();
        if (coverImage != null) {
            Glide.with(this).load(coverImage.getLocation()).centerCrop().into(coverImageView);
        }

        toolbarLayout.setTitle(recipe.getName());
        ingredientsView.setText(recipe.getIngredients());
        preparationView.setText(recipe.getPreparation());
        notesView.setText(recipe.getNotes());
        labelsView.setText(getFormattedLabels());

        SimpleRecipeImageAdapter imageAdapter = new SimpleRecipeImageAdapter(this, recipe.getImages());

        imagesView.setHasFixedSize(true);
        imagesView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imagesView.setAdapter(imageAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(imagesView.getContext(), LinearLayoutManager.HORIZONTAL);
        imagesView.addItemDecoration(dividerItemDecoration);



        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        */
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

        Spannable firstText = textPartsBuilder.build();

        return firstText;
    }

    @NonNull
    private SpannableStringBuilder getFormattedLabels1() {

        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        /*
        stringBuilder.append("");
        for (Label label : recipe.getLabels()) {
            String thisTag = "  " + label.getName() + "  ";
            stringBuilder.append(thisTag);
            stringBuilder.setSpan(new RecipeLabelSpan(this),
                    stringBuilder.length() - thisTag.length(),
                    stringBuilder.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            stringBuilder.append("  ");
        }
        return stringBuilder;
        */
        //badgeBuilder.appendTag(tag.getName(), tag.getColorHex());

        final String nbspSpacing = "\u202F\u202F"; // none-breaking spaces
        labelsView.measure(0, 0);

        for (Label label : recipe.getLabels()) {
            String shortenedLabel = label.getName();


            Paint paint = new Paint();
            paint.setTypeface(labelsView.getTypeface());
            paint.setTextSize(labelsView.getTextSize());
            int maxCharsCount = paint.breakText(shortenedLabel, true, labelsView.getWidth(), null);
            maxCharsCount = 20;
            if (shortenedLabel.length() > maxCharsCount) {
                shortenedLabel = shortenedLabel.substring(0, maxCharsCount - (nbspSpacing.length() * 3)) + "\u2026";
            }

            String badgeText = nbspSpacing + shortenedLabel + nbspSpacing;
            stringBuilder.append(badgeText);
            stringBuilder.setSpan(
                    //new RecipeLabelSpan(lineHeight, Color.parseColor(textColor), Color.parseColor(badgeColor)),
                    //new RecipeLabelSpan(this),
                    //new PaddingBackgroundColorSpan(getResources().getColor(R.color.recipeLabelBackground), 2),
                    new RecipeLabelSpan(15,
                            getResources().getColor(R.color.recipeLabelText),
                            getResources().getColor(R.color.recipeLabelBackground)),
                    stringBuilder.length() - badgeText.length(),
                    stringBuilder.length() - badgeText.length() + badgeText.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            stringBuilder.append(" ");
        }

        return stringBuilder;
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
        }

        return super.onOptionsItemSelected(item);
    }
}
