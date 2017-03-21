package de.schmaun.ourrecipes.Main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.schmaun.ourrecipes.Adapter.RecipeAdapter;
import de.schmaun.ourrecipes.Model.Recipe;
import de.schmaun.ourrecipes.MultipleColumnItemDecoration;
import de.schmaun.ourrecipes.R;

public class RecipeListBaseFragment extends Fragment {
    protected List<Recipe> recipes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        float scale = getResources().getDisplayMetrics().density;
        RecyclerView view = (RecyclerView)inflater.inflate(R.layout.fragment_main_label_list, container, false);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        view.setLayoutManager(layoutManager);

        view.setAdapter(new RecipeAdapter(recipes, R.layout.recipe_row, getContext()));
        view.addItemDecoration(new MultipleColumnItemDecoration((int) (scale*4f), 2));

        return view;
    }
}
