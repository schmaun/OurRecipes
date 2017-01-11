package de.schmaun.ourrecipes;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import de.schmaun.ourrecipes.Adapter.RecipeImageAdapter;
import de.schmaun.ourrecipes.Database.DbHelper;
import de.schmaun.ourrecipes.Database.LabelsRepository;
import de.schmaun.ourrecipes.Database.RecipeImageRepository;
import de.schmaun.ourrecipes.Database.RecipeRepository;
import de.schmaun.ourrecipes.Model.Recipe;
import de.schmaun.ourrecipes.Model.RecipeImage;

public class EditRecipeActivity extends AppCompatActivity implements RecipeChangedListener, RecipeProviderInterface {
    public static final String BUNDLE_KEY_RECIPE_ID = "recipeId";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private Recipe recipe;

    private EditRecipeMainFragment mainFragment;
    private EditRecipeImagesFragment imagesFragment;
    private EditRecipeMetaFragment metaFragment;

    private static final String TAG_LIFECYCLE = "ERA:lifecycle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        Bundle bundle = getIntent().getExtras();
        long recipeId = 0;
        if (bundle != null) {
            recipeId = bundle.getLong(BUNDLE_KEY_RECIPE_ID);
            Log.d(TAG_LIFECYCLE, String.format("started with %s=%s", BUNDLE_KEY_RECIPE_ID, Long.toString(recipeId)));
        }

        recipe = new Recipe();
        if (savedInstanceState == null && recipeId > 0) {
            DbHelper dbHelper = new DbHelper(this);
            RecipeRepository repository = RecipeRepository.getInstance(dbHelper);
            recipe = repository.load(recipeId);

            RecipeImageRepository imageRepository = RecipeImageRepository.getInstance(dbHelper);
            recipe.setImages(imageRepository.load(recipeId));

            LabelsRepository labelsRepository = LabelsRepository.getInstance(dbHelper);
            recipe.setLabels(labelsRepository.loadLabels(recipeId));

            Log.d(TAG_LIFECYCLE, "recipe loaded");
        }
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

    @Override
    public void onNameChange(String name) {
        recipe.setName(name);
    }

    @Override
    public void onDescriptionChange(String description) {
        recipe.setDescription(description);
    }

    @Override
    public void onIngredientsChange(String ingredients) {
        recipe.setIngredients(ingredients);
    }

    @Override
    public void onPreparationChange(String preparation) {
        recipe.setPreparation(preparation);
    }

    @Override
    public void onImageAdded(RecipeImage image) {
        recipe.addImage(image);
    }

    @Override
    public void onImageDeleted(RecipeImage image) {

    }

    @Override
    public void onImageMoved(ArrayList<RecipeImage> recipeImages) {
        recipe.setImages(recipeImages);
    }

    @Override
    public void onLabelChanged() {

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
        }

        if (metaFragment != null) {
            this.recipe.setLabels(metaFragment.getRecipe().getLabels());
        }
    }

    private void saveRecipe()
    {
        DbHelper dbHelper = new DbHelper(this);

        RecipeRepository repository = RecipeRepository.getInstance(dbHelper);
        repository.save(recipe);

        RecipeImageRepository recipeImageRepository = RecipeImageRepository.getInstance(dbHelper);
        recipeImageRepository.save(recipe.getId(), recipe.getImages());

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

    @Override
    public Recipe getRecipe() {
        return recipe;
    }

    abstract static class EditRecipeFragment extends Fragment {
        protected RecipeProviderInterface recipeProvider;

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);

            try {
                recipeProvider = (RecipeProviderInterface) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString() + " must implement RecipeProviderInterface");
            }
        }
    }

    public static class EditRecipeMainFragment extends EditRecipeFragment implements RecipeFormInterface {
        private TextView nameView;
        private TextView ingredientsView;
        private TextView preparationView;

        public EditRecipeMainFragment() {
        }

        public static EditRecipeMainFragment newInstance() {
            EditRecipeMainFragment fragment = new EditRecipeMainFragment();

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_edit_recipe_main, container, false);

            nameView = (TextView) rootView.findViewById(R.id.edit_recipe_name);
            ingredientsView = (TextView) rootView.findViewById(R.id.edit_recipe_ingredients);
            preparationView = (TextView) rootView.findViewById(R.id.edit_recipe_preparation);

            Recipe recipe = recipeProvider.getRecipe();
            if (savedInstanceState == null) {
                nameView.setText(recipe.getName());
                ingredientsView.setText(recipe.getIngredients());
                preparationView.setText(recipe.getPreparation());
            }

            return rootView;
        }

        @Override
        public boolean isValid() {
            if (nameView.length() == 0) {
                nameView.setError(getString(R.string.edit_recipe_name_error_empty));
                return false;
            }

            return true;
        }

        @Override
        public Recipe getRecipe() {
            Recipe recipe = new Recipe();
            recipe.setName(nameView.getText().toString());
            recipe.setIngredients(ingredientsView.getText().toString());
            recipe.setPreparation(preparationView.getText().toString());

            return recipe;
        }

        @Override
        public void onSaved() {

        }
    }

    public static class EditRecipeImagesFragment extends EditRecipeFragment implements RecipeFormInterface {
        private RecyclerView imageListView;
        ArrayList<RecipeImage> recipeImages;
        private static final String STATE_ITEMS = "items";
        public ArrayList<RecipeImage> deletedImage = new ArrayList<RecipeImage>();

        public EditRecipeImagesFragment() {
        }

        public static EditRecipeImagesFragment newInstance() {
            EditRecipeImagesFragment fragment = new EditRecipeImagesFragment();

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_edit_recipe_images, container, false);

            DbHelper db = new DbHelper(getContext());

            imageListView = (RecyclerView) rootView.findViewById(R.id.edit_recipe_image_list);
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 1);

            imageListView.setLayoutManager(layoutManager);
            imageListView.setItemAnimator(new DefaultItemAnimator());
            imageListView.setHasFixedSize(true);

            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if (savedInstanceState != null) {
                recipeImages = (ArrayList<RecipeImage>) savedInstanceState.getSerializable(STATE_ITEMS);
            } else {
                recipeImages = new ArrayList<>();

                RecipeImage image2 = new RecipeImage("http://api.learn2crack.com/android/images/froyo.png");
                image2.setDescription("awefojefwajo  ef wa ef efwafea wa efwafwe ");
                recipeImages.add(new RecipeImage("http://api.learn2crack.com/android/images/eclair.png"));
                recipeImages.add(image2);
                recipeImages.add(new RecipeImage("http://api.learn2crack.com/android/images/ginger.png"));
                recipeImages.add(new RecipeImage("http://api.learn2crack.com/android/images/honey.png"));
                RecipeImage imageIC = new RecipeImage("http://api.learn2crack.com/android/images/icecream.png");
                imageIC.setDescription("lecker eis");
                recipeImages.add(imageIC);
                RecipeImage imageJB = new RecipeImage("http://api.learn2crack.com/android/images/jellybean.png");
                imageJB.setDescription("bohnen und speck oder so");
                recipeImages.add(imageJB);
                recipeImages.add(new RecipeImage("http://api.learn2crack.com/android/images/kitkat.png"));
                recipeImages.add(new RecipeImage("http://api.learn2crack.com/android/images/lollipop.png"));
                recipeImages.add(new RecipeImage("http://api.learn2crack.com/android/images/marshmallow.png"));

                if (recipeProvider.getRecipe().getId() != 0) {
                    recipeImages = recipeProvider.getRecipe().getImages();
                }
            }

            RecipeImageAdapter imageAdapter = new RecipeImageAdapter(getContext(), recipeImages);
            imageListView.setAdapter(imageAdapter);

            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SimpleItemTouchHelperCallback(imageAdapter));
            itemTouchHelper.attachToRecyclerView(imageListView);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putSerializable(STATE_ITEMS, recipeImages);
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public Recipe getRecipe() {
            Recipe recipe = new Recipe();
            recipe.setImages(recipeImages);

            return recipe;
        }

        @Override
        public void onSaved() {
            removeDeletedImages();
        }

        protected void removeDeletedImages() {
            for (RecipeImage recipeImage: deletedImage) {
                File file = new File(recipeImage.getLocation());
                boolean deleted = file.delete();

                Log.d("deleteImageFile", Boolean.toString(deleted));
            }
        }

        public static class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

            public static final String TAG = "SimpleItemTouchHelper";

            public int counter = 0;

            public interface ItemTouchHelperAdapter {
                void onItemMove(int fromPosition, int toPosition);
                void onItemDismiss(int position);
            }

            /**
             * Notifies a View Holder of relevant callbacks from
             * {@link ItemTouchHelper.Callback}.
             */
            public interface ItemTouchHelperViewHolder {

                /**
                 * Called when the {@link ItemTouchHelper} first registers an
                 * item as being moved or swiped.
                 * Implementations should update the item view to indicate
                 * it's active state.
                 */
                void onItemSelected();

                /**
                 * Called when the {@link ItemTouchHelper} has completed the
                 * move or swipe, and the active item state should be cleared.
                 */
                void onItemClear();
            }

            private final ItemTouchHelperAdapter mAdapter;

            public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
                mAdapter = adapter;
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;

                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {

                mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder,
                                          int actionState) {

                // We only want the active item
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    if (viewHolder instanceof ItemTouchHelperViewHolder) {
                        ItemTouchHelperViewHolder itemViewHolder =
                                (ItemTouchHelperViewHolder) viewHolder;
                        itemViewHolder.onItemSelected();
                    }
                }

                super.onSelectedChanged(viewHolder, actionState);
            }

            @Override
            public void clearView(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);

                if (viewHolder instanceof ItemTouchHelperViewHolder) {
                    ItemTouchHelperViewHolder itemViewHolder =
                            (ItemTouchHelperViewHolder) viewHolder;
                    itemViewHolder.onItemClear();
                }
            }
        }
    }

    public static class EditRecipeMetaFragment extends Fragment implements RecipeFormInterface {

        public EditRecipeMetaFragment() {
        }

        public static EditRecipeMetaFragment newInstance() {
            EditRecipeMetaFragment fragment = new EditRecipeMetaFragment();

            return fragment;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_edit_recipe_meta, container, false);

            return rootView;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public Recipe getRecipe() {
            return new Recipe();
        }

        @Override
        public void onSaved() {

        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private Context context;
        private final String TAG = "SectionsPagerAdapter";
        private FragmentManager fm;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fm = fm;
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
