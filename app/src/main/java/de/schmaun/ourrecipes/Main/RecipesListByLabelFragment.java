package de.schmaun.ourrecipes.Main;

import android.os.Bundle;
import android.util.Log;
import de.schmaun.ourrecipes.Database.DbHelper;
import de.schmaun.ourrecipes.Database.RecipeRepository;
import de.schmaun.ourrecipes.Model.Label;
import de.schmaun.ourrecipes.Utils.StopWatch;

public class RecipesListByLabelFragment extends RecipeListBaseFragment {

    private static final String ARG_LABEL_ID = "label-id";
    public static final String TAG = "RecipesListByLabelFt";
    private long labelId;

    public RecipesListByLabelFragment() {
    }

    public static RecipesListByLabelFragment newInstance(Label label) {
        RecipesListByLabelFragment fragment = new RecipesListByLabelFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_LABEL_ID, label.getId());
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            labelId = getArguments().getLong(ARG_LABEL_ID);
        }
    }

    protected void loadRecipes() {
        DbHelper db = new DbHelper(getContext());
        StopWatch stopWatch = StopWatch.createAndStart();

        if (labelId > 0) {
            recipes = RecipeRepository.getInstance(db).getRecipesForLabel(labelId);
        } else {
            recipes = RecipeRepository.getInstance(db).getRecipesWithoutLabel(labelId);
        }

        Log.d(TAG, "getRecipesForLabel duration: " + Long.toString(stopWatch.stop()));
    }
}
