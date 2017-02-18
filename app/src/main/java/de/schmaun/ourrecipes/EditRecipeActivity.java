package de.schmaun.ourrecipes;

import android.content.Context;
import android.support.design.widget.TabLayout;
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

import de.schmaun.ourrecipes.Database.DbHelper;
import de.schmaun.ourrecipes.Database.LabelsRepository;
import de.schmaun.ourrecipes.Database.RecipeImageRepository;
import de.schmaun.ourrecipes.Database.RecipeRepository;
import de.schmaun.ourrecipes.EditRecipe.EditRecipeImagesFragment;
import de.schmaun.ourrecipes.EditRecipe.EditRecipeMainFragment;
import de.schmaun.ourrecipes.EditRecipe.EditRecipeMetaFragment;
import de.schmaun.ourrecipes.Model.Recipe;

public class EditRecipeActivity extends RecipeActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private PagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private EditRecipeMainFragment mainFragment;
    private EditRecipeImagesFragment imagesFragment;
    private EditRecipeMetaFragment metaFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSectionsPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

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

        loadRecipe(savedInstanceState, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_recipe, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            if(validate()) {
                collectDataFromFragments();
                saveRecipe();
                onSavedRecipe();
                finish();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean validate()
    {
        if (mainFragment != null) {
            boolean valid = mainFragment.isValid();
            if (!valid) {
                mViewPager.setCurrentItem(0);
            }

            return valid;
        }

        return false;
    }

    private void collectDataFromFragments()
    {
        if (mainFragment != null) {
            Recipe recipe = mainFragment.getRecipe();

            this.recipe.setName(recipe.getName());
            this.recipe.setIngredients(recipe.getIngredients());
            this.recipe.setPreparation(recipe.getPreparation());
        }

        if (imagesFragment != null) {
            this.recipe.setImages(imagesFragment.getRecipe().getImages());
            this.recipe.setImagesToDelete(imagesFragment.getRecipe().getImagesToDelete());
        }

        if (metaFragment != null) {
            this.recipe.setLabels(metaFragment.getRecipe().getLabels());
            this.recipe.setNotes(metaFragment.getRecipe().getNotes());
        }
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
        if (mainFragment != null) {
            mainFragment.onSaved();
        }

        if (imagesFragment != null) {
            imagesFragment.onSaved();
        }

        if (metaFragment != null) {
            metaFragment.onSaved();
        }
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
                    return EditRecipeImagesFragment.newInstance();
                case 2:
                    return EditRecipeMetaFragment.newInstance();
                default:
                case 0:
                    return EditRecipeMainFragment.newInstance();
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
                    return getString(R.string.edit_recipe_page_title_main);
                case 1:
                    return getString(R.string.edit_recipe_page_title_images);
                case 2:
                    return getString(R.string.edit_recipe_page_title_meta);
            }
            return null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            switch (position) {
                case 0:
                    mainFragment = (EditRecipeMainFragment) fragment;
                    break;
                case 1:
                    imagesFragment = (EditRecipeImagesFragment) fragment;
                    break;
                case 2:
                    metaFragment = (EditRecipeMetaFragment) fragment;
                    break;
            }

            return fragment;
        }
    }
}
