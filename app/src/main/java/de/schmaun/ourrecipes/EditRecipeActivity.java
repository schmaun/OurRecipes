package de.schmaun.ourrecipes;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
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
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.rengwuxian.materialedittext.MaterialMultiAutoCompleteTextView;

import org.parceler.Parcels;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.schmaun.ourrecipes.Adapter.RecipeImageAdapter;
import de.schmaun.ourrecipes.Database.DbHelper;
import de.schmaun.ourrecipes.Database.LabelsRepository;
import de.schmaun.ourrecipes.Database.RecipeImageRepository;
import de.schmaun.ourrecipes.Database.RecipeRepository;
import de.schmaun.ourrecipes.Model.Label;
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

        recipe = new Recipe();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            recipe.setId(bundle.getLong(BUNDLE_KEY_RECIPE_ID));
            Log.d(TAG_LIFECYCLE, String.format("started with %s=%s", BUNDLE_KEY_RECIPE_ID, Long.toString(recipe.getId())));
        }

        if (savedInstanceState == null && recipe.getId() > 0) {
            DbHelper dbHelper = new DbHelper(this);
            RecipeRepository repository = RecipeRepository.getInstance(dbHelper);
            recipe = repository.load(recipe.getId());

            RecipeImageRepository imageRepository = RecipeImageRepository.getInstance(dbHelper);
            recipe.setImages(imageRepository.load(recipe.getId()));

            LabelsRepository labelsRepository = LabelsRepository.getInstance(dbHelper);
            recipe.setLabels(labelsRepository.loadLabels(recipe.getId()));

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

    public static class EditRecipeImagesFragment extends EditRecipeFragment implements RecipeFormInterface, PhotoDialogFragment.PictureIntentHandler, DownloadImageTask.DownloadImageHandler {
        private RecyclerView imageListView;
        private RecipeImageAdapter imageAdapter;
        private ArrayList<RecipeImage> recipeImages;

        private static final String STATE_ITEMS = "items";
        private String TAG = "EditRecipeImagesF";
        static final int REQUEST_TAKE_PHOTO = 1;
        static final int REQUEST_SELECT_PHOTO = 2;
        private Uri newPhotoURI;

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

            imageListView = (RecyclerView) rootView.findViewById(R.id.edit_recipe_image_list);
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 1);

            imageListView.setLayoutManager(layoutManager);
            imageListView.setItemAnimator(new DefaultItemAnimator());
            imageListView.setHasFixedSize(true);

            final EditRecipeImagesFragment fragment = this;
            Button addImageButton = (Button) rootView.findViewById(R.id.edit_recipe_add_image);
            addImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PhotoDialogFragment newFragment = new PhotoDialogFragment();
                    newFragment.setPictureIntentHandler(fragment);
                    newFragment.show(getActivity().getSupportFragmentManager(), "add_photo");
                }
            });

            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if (savedInstanceState != null) {
                recipeImages = Parcels.unwrap(savedInstanceState.getParcelable(STATE_ITEMS));
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

            imageAdapter = new RecipeImageAdapter(getContext(), recipeImages, imageListView);
            imageListView.setAdapter(imageAdapter);

            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SimpleItemTouchHelperCallback(imageAdapter));
            itemTouchHelper.attachToRecyclerView(imageListView);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putParcelable(STATE_ITEMS, Parcels.wrap(recipeImages));
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            Log.d(TAG, String.format("onActivityResult requestCode: %d; resultCode:%d", requestCode, resultCode));

            if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
                RecipeImage image = new RecipeImage();
                image.setLocation(newPhotoURI.toString());
                imageAdapter.addImage(image);
            }

            if (requestCode == REQUEST_SELECT_PHOTO && resultCode == RESULT_OK) {
                Uri uri = data.getData();
                Log.i(TAG, "Uri: " + uri.toString());

                try {
                    File file = createImageFile();
                    new DownloadImageTask(getContext(), this, file).execute(uri);
                } catch (IOException e) {

                }
            }
        }

        @Override
        public void dispatchTakePicture() {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Toast.makeText(getContext(), R.string.error_taking_picture, Toast.LENGTH_LONG).show();
                }

                if (photoFile != null) {
                    newPhotoURI = FileProvider.getUriForFile(getActivity(), "de.schmaun.fileprovider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, newPhotoURI);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            } else {
                Toast.makeText(getContext(), R.string.no_camera_app_available, Toast.LENGTH_LONG).show();
            }
        }

        public void dispatchSelectPictureFromGallery()
        {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startSelectPictureIntent(photoPickerIntent);
        }

        public void dispatchSelectPictureFromStorageAccessFramework()
        {
            Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            startSelectPictureIntent(photoPickerIntent);
        }

        private void startSelectPictureIntent(Intent intent) {
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_SELECT_PHOTO);
            } else {
                Toast.makeText(getContext(), R.string.no_gallery_app_available, Toast.LENGTH_LONG).show();
            }
        }

        private File createImageFile() throws IOException {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File storageDir = getActivity().getExternalFilesDir("images");

            return File.createTempFile(timeStamp, ".jpg", storageDir);
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public Recipe getRecipe() {
            Recipe recipe = new Recipe();
            recipe.setImages(recipeImages);
            recipe.setImagesToDelete(imageAdapter.getDeletedImages());

            return recipe;
        }

        @Override
        public void onSaved() {
            removeDeletedImageFiles();
        }

        protected void removeDeletedImageFiles() {
            for (RecipeImage recipeImage : imageAdapter.getDeletedImages()) {
                File file = new File(recipeImage.getLocation());
                boolean deleted = file.delete();

                Log.d("deleteImageFile", Boolean.toString(deleted));
            }
        }

        @Override
        public void onError(Exception error) {
            Toast.makeText(getContext(), R.string.error_saving_image, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onSuccess(File file) {
            RecipeImage image = new RecipeImage();
            image.setLocation(file.getAbsolutePath());
            imageAdapter.addImage(image);
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

    public static class EditRecipeMetaFragment extends EditRecipeFragment implements RecipeFormInterface {
        private TextView notesView;
        private MaterialMultiAutoCompleteTextView labelsView;

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

            labelsView = (MaterialMultiAutoCompleteTextView) rootView.findViewById(R.id.edit_recipe_labels);
            notesView = (TextView) rootView.findViewById(R.id.edit_recipe_notes);

            Recipe recipe = recipeProvider.getRecipe();
            if (savedInstanceState == null) {
                labelsView.setText(parseLabelsToText(recipe.getLabels()));
                notesView.setText(recipe.getNotes());
            }

            DbHelper dbHelper = new DbHelper(getContext());
            LabelsRepository labelsRepository = LabelsRepository.getInstance(dbHelper);
            List<Label> labels = labelsRepository.loadLabels();

            ArrayAdapter<Label> adapter = new ArrayAdapter<Label>(getContext(), android.R.layout.simple_dropdown_item_1line, labels);

            labelsView.setAdapter(adapter);
            labelsView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

            return rootView;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public Recipe getRecipe() {
            Recipe recipe = new Recipe();
            recipe.setNotes(notesView.getText().toString());
            recipe.setLabels(parseLabels());

            return recipe;
        }

        private ArrayList<Label> parseLabels() {
            ArrayList<Label> recipeLabels = new ArrayList<>();
            String labels[] = labelsView.getText().toString().split(",");
            for (String label: labels) {
                label = label.trim();
                if (label.length() > 0) {
                    recipeLabels.add(new Label(label));
                }
            }

            return recipeLabels;
        }

        private String parseLabelsToText(ArrayList<Label> recipeLabels)
        {
            if (recipeLabels == null) {
                return "";
            }

            StringBuilder stringBuilder = new StringBuilder();
            for (Label label: recipeLabels) {
                stringBuilder.append(label.getName());
                stringBuilder.append(", ");
            }

            return stringBuilder.toString();
        }

        @Override
        public void onSaved() {
        }
    }

    public static class DownloadImageTask extends AsyncTask<Uri, Void, File> {
        private Exception error;
        private Context context;
        private DownloadImageHandler imageHandler;
        private File target;

        interface DownloadImageHandler {
            void onError(Exception error);
            void onSuccess(File file);
        }

        public DownloadImageTask(Context context, DownloadImageHandler imageHandler, File target)
        {
            this.context = context;
            this.imageHandler = imageHandler;
            this.target = target;
        }

        protected File doInBackground(Uri... uris) {
            try {
                downloadImage(uris[0]);
            } catch (IOException e) {
                error = e;
                Log.e("DownloadImageTask", e.getMessage(), e);
            }
            return target;
        }

        protected void onPostExecute(File file) {
            if (error != null) {
                imageHandler.onError(error);
            } else {
                imageHandler.onSuccess(file);
            }
        }

        private void downloadImage(Uri uri) throws IOException {
            InputStream in = context.getContentResolver().openInputStream(uri);
            OutputStream out = new FileOutputStream(this.target);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            in.close();
            out.close();
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
