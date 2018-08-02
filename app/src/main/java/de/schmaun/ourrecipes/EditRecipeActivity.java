package de.schmaun.ourrecipes;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.ArrayList;

import de.schmaun.ourrecipes.Adapter.RecipeImageAdapter;
import de.schmaun.ourrecipes.Database.DbHelper;
import de.schmaun.ourrecipes.Database.LabelsRepository;
import de.schmaun.ourrecipes.Database.RecipeImageRepository;
import de.schmaun.ourrecipes.Database.RecipeRepository;
import de.schmaun.ourrecipes.EditRecipe.EditRecipeIngredientsFragment;
import de.schmaun.ourrecipes.EditRecipe.EditRecipeMetaFragment;
import de.schmaun.ourrecipes.EditRecipe.EditRecipePreparationFragment;
import de.schmaun.ourrecipes.Model.RecipeImage;

public class EditRecipeActivity extends RecipeActivity implements RecipeImageAdapter.ImageListManager {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private PagerAdapter sectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private EditRecipeMetaFragment metaFragment;
    private EditRecipeIngredientsFragment ingredientsFragment;
    private EditRecipePreparationFragment preparationFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sectionsPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(sectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mViewPager.getWindowToken(), 0);
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        loadRecipeOnCreate(savedInstanceState, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_recipe, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showExitDialog();
                return true;
            case R.id.action_save:
                saveAndFinish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveAndFinish() {
        if(validate()) {
            collectDataFromFragments();
            saveRecipe();
            onSavedRecipe();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    private void showExitDialog() {
        if (!hasUnsavedChanges()) {
            finish();
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle(R.string.edit_leave_confirm_headline)
            .setMessage(R.string.edit_leave_confirm_text)
            .setPositiveButton(R.string.edit_leave_confirm_button_yes, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveAndFinish();
                }

            })
            .setNeutralButton(R.string.edit_leave_confirm_button_no, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }

            })
            .setNegativeButton(R.string.edit_leave_confirm_button_stay, null)
            .show();
    }

    private boolean validate()
    {
        if (metaFragment != null) {
            boolean valid = metaFragment.isValid();
            if (!valid) {
                mViewPager.setCurrentItem(0);
            }

            return valid;
        }

        return false;
    }

    private boolean hasUnsavedChanges()
    {
        if (metaFragment != null && metaFragment.hasUnsavedChanges()) {
            return true;
        }
        if (ingredientsFragment != null && ingredientsFragment.hasUnsavedChanges()) {
            return true;
        }
        if (preparationFragment != null && preparationFragment.hasUnsavedChanges()) {
            return true;
        }

        return false;
    }

    private void collectDataFromFragments()
    {
        ArrayList<RecipeImage> mergedImages = new ArrayList<>();
        ArrayList<RecipeImage> mergedImagesToDelete = new ArrayList<>();

        if (metaFragment != null) {
            this.recipe.setName(metaFragment.getRecipe().getName());
            this.recipe.setLabels(metaFragment.getRecipe().getLabels());
            this.recipe.setNotes(metaFragment.getRecipe().getNotes());
            mergedImages.addAll(metaFragment.getRecipe().getImages());
            mergedImagesToDelete.addAll(metaFragment.getRecipe().getImagesToDelete());
        }
        if (ingredientsFragment != null) {
            this.recipe.setIngredients(ingredientsFragment.getRecipe().getIngredients());
            mergedImages.addAll(ingredientsFragment.getRecipe().getImages());
            mergedImagesToDelete.addAll(ingredientsFragment.getRecipe().getImagesToDelete());

        }
        if (preparationFragment != null) {
            this.recipe.setPreparation(preparationFragment.getRecipe().getPreparation());
            mergedImages.addAll(preparationFragment.getRecipe().getImages());
            mergedImagesToDelete.addAll(preparationFragment.getRecipe().getImagesToDelete());
        }

        this.recipe.setImages(mergedImages);
        this.recipe.setImagesToDelete(mergedImagesToDelete);
    }

    private void saveRecipe()
    {
        DbHelper dbHelper = new DbHelper(this);

        RecipeRepository repository = RecipeRepository.getInstance(dbHelper);
        repository.save(recipe);

        RecipeImageRepository recipeImageRepository = RecipeImageRepository.getInstance(dbHelper);
        recipeImageRepository.save(recipe.getId(), recipe.getImages());
        recipeImageRepository.delete(recipe.getImagesToDelete());

        LabelsRepository labelsRepository = LabelsRepository.getInstance(dbHelper);
        labelsRepository.saveLabels(recipe);

        Toast.makeText(this, getString(R.string.recipe_saved), Toast.LENGTH_LONG).show();
    }

    private void onSavedRecipe()
    {
        if (metaFragment != null) {
            metaFragment.onSaved();
        }

        if (ingredientsFragment != null) {
            ingredientsFragment.onSaved();
        }

        if (preparationFragment != null) {
            preparationFragment.onSaved();
        }
    }

    @Override
    public void resetCoverImageStatus() {
        recipe.resetCoverImage();

        if (metaFragment != null) {
            metaFragment.resetCoverImageStatus();
        }

        if (ingredientsFragment != null) {
            ingredientsFragment.resetCoverImageStatus();
        }

        if (preparationFragment != null) {
            preparationFragment.resetCoverImageStatus();
        }
    }

    @Override
    public int getImageCount() {
        return recipe.getImages().size();
    }

    private class PagerAdapter extends FragmentPagerAdapter {

        private final String TAG = "PagerAdapter";

        PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, String.format("getItem called: %d", position));

            switch (position) {
                case 1:
                    return EditRecipeIngredientsFragment.newInstance();
                case 2:
                    return EditRecipePreparationFragment.newInstance();
                default:
                case 0:
                    return EditRecipeMetaFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.edit_recipe_page_title_meta);
                case 1:
                    return getString(R.string.edit_recipe_page_title_ingredients);
                case 2:
                    return getString(R.string.edit_recipe_page_title_preparation);
            }
            return null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Log.d(TAG, String.format("instantiateItem called: %d", position));

            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            switch (position) {
                case 0:
                    metaFragment = (EditRecipeMetaFragment) fragment;
                    break;
                case 1:
                    ingredientsFragment = (EditRecipeIngredientsFragment) fragment;
                    break;
                case 2:
                    preparationFragment = (EditRecipePreparationFragment) fragment;
                    break;
            }

            return fragment;
        }
    }
}
