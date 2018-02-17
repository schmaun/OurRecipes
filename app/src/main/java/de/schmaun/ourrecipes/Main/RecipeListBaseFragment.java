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

abstract public class RecipeListBaseFragment extends Fragment {
    protected List<Recipe> recipes;
    protected RecipeAdapter recipesListAdapter;

    abstract protected void loadRecipes();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        float scale = getResources().getDisplayMetrics().density;
        RecyclerView view = (RecyclerView)inflater.inflate(R.layout.fragment_main_label_list, container, false);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        view.setLayoutManager(layoutManager);

        recipesListAdapter = new RecipeAdapter(recipes, R.layout.recipe_row, getContext());
        view.setAdapter(recipesListAdapter);
        view.addItemDecoration(new MultipleColumnItemDecoration((int) (scale*4f), 2));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecipes();
        recipesListAdapter.setRecipes(recipes);
        recipesListAdapter.notifyDataSetChanged();
    }
}
