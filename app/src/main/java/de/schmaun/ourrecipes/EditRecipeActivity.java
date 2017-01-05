package de.schmaun.ourrecipes;

import android.Manifest;
import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
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

import java.util.ArrayList;
import java.util.List;

import de.schmaun.ourrecipes.Adapter.RecipeAdapter;
import de.schmaun.ourrecipes.Adapter.RecipeImageAdapter;
import de.schmaun.ourrecipes.Database.DbHelper;
import de.schmaun.ourrecipes.Database.RecipeRepository;
import de.schmaun.ourrecipes.Model.Recipe;
import de.schmaun.ourrecipes.Model.RecipeImage;

public class EditRecipeActivity extends AppCompatActivity implements RecipeChangedListener {

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

    public Recipe recipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
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
        if (id == R.id.action_settings) {
            return true;
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



    public static class EditRecipeMainFragment extends Fragment {
        private RecipeChangedListener recipeChangedListener;

        public EditRecipeMainFragment() {
        }

        public static EditRecipeMainFragment newInstance() {
            EditRecipeMainFragment fragment = new EditRecipeMainFragment();

            return fragment;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);

            recipeChangedListener = (RecipeChangedListener) context;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_edit_recipe_main, container, false);

            TextView name = (TextView) rootView.findViewById(R.id.edit_recipe_name);

            return rootView;
        }
    }

    public static class EditRecipeImagesFragment extends Fragment {
        private RecyclerView imageList;
        ArrayList<RecipeImage> recipeImages = new ArrayList<>();
        private RecipeChangedListener recipeChangedListener;

        public EditRecipeImagesFragment() {
        }

        public static EditRecipeImagesFragment newInstance() {
            EditRecipeImagesFragment fragment = new EditRecipeImagesFragment();

            return fragment;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);

            recipeChangedListener = (RecipeChangedListener) context;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_edit_recipe_images, container, false);



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



            DbHelper db = new DbHelper(getContext());

            imageList = (RecyclerView) rootView.findViewById(R.id.edit_recipe_image_list);
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 1);
            RecipeImageAdapter imageAdapter = new RecipeImageAdapter(getContext(), recipeImages);


            imageList.setLayoutManager(layoutManager);
            imageList.setAdapter(imageAdapter);
            imageList.setItemAnimator(new DefaultItemAnimator());
            imageList.setHasFixedSize(true);

            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SimpleItemTouchHelperCallback(imageAdapter));
            itemTouchHelper.attachToRecyclerView(imageList);

            return rootView;
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

    public static class EditRecipeMetaFragment extends Fragment {
        private RecipeChangedListener recipeChangedListener;

        public EditRecipeMetaFragment() {
        }

        public static EditRecipeMetaFragment newInstance() {
            EditRecipeMetaFragment fragment = new EditRecipeMetaFragment();

            return fragment;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);

            recipeChangedListener = (RecipeChangedListener) context;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_edit_recipe_meta, container, false);

            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private Context context;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
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
    }
}
