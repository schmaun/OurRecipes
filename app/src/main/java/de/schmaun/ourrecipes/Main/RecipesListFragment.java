package de.schmaun.ourrecipes.Main;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import java.util.List;

import de.schmaun.ourrecipes.Adapter.RecipeAdapter;
import de.schmaun.ourrecipes.Database.DbHelper;
import de.schmaun.ourrecipes.Database.RecipeRepository;
import de.schmaun.ourrecipes.Model.Label;
import de.schmaun.ourrecipes.Model.Recipe;
import de.schmaun.ourrecipes.MultipleColumnItemDecoration;
import de.schmaun.ourrecipes.R;
import de.schmaun.ourrecipes.SpacesItemDecoration;
import de.schmaun.ourrecipes.Utils.StopWatch;

public class RecipesListFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_LABEL_ID = "label-id";
    public static final String TAG = "RecipesListFragment";
    private int columnCount = 1;
    private long labelId;
    private RecipeListInteractionListener interactionListener;
    private List<Recipe> recipes;

    public RecipesListFragment() {
    }

    public static RecipesListFragment newInstance(int columnCount, Label label) {
        RecipesListFragment fragment = new RecipesListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putLong(ARG_LABEL_ID, label.getId());
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            columnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            labelId = getArguments().getLong(ARG_LABEL_ID);
        }

        DbHelper db = new DbHelper(getContext());

        StopWatch stopWatch = StopWatch.createAndStart();

        if (labelId > 0) {
            recipes = RecipeRepository.getInstance(db).getRecipesForLabel(labelId);
        } else {
            recipes = RecipeRepository.getInstance(db).getRecipesWithoutLabel(labelId);
        }
        Log.d(TAG, "getRecipesForLabel duration: " + Long.toString(stopWatch.stop()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        float scale = getResources().getDisplayMetrics().density;
        RecyclerView view = (RecyclerView)inflater.inflate(R.layout.fragment_main_label_list, container, false);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), columnCount);
        view.setLayoutManager(layoutManager);

        view.setAdapter(new RecipeAdapter(recipes, R.layout.recipe_row, getContext()));
        view.addItemDecoration(new MultipleColumnItemDecoration((int) (scale*4f), 2));


        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RecipeListInteractionListener) {
            interactionListener = (RecipeListInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement RecipeListInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        interactionListener = null;
    }

    public interface RecipeListInteractionListener {
        void onRecipesListLabelClick(Label label);
    }
}
