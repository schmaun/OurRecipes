package de.schmaun.ourrecipes.EditRecipe;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialMultiAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

import de.schmaun.ourrecipes.Database.DbHelper;
import de.schmaun.ourrecipes.Database.LabelsRepository;
import de.schmaun.ourrecipes.Model.Label;
import de.schmaun.ourrecipes.Model.Recipe;
import de.schmaun.ourrecipes.R;
import de.schmaun.ourrecipes.RecipeFormInterface;

public class EditRecipeMetaFragment extends EditRecipeFragment implements RecipeFormInterface {
    private TextView notesView;
    private MaterialMultiAutoCompleteTextView labelsView;

    public static EditRecipeMetaFragment newInstance() {
        return new EditRecipeMetaFragment();
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

        ArrayAdapter<Label> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, labels);

        labelsView.setAdapter(adapter);
        labelsView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        labelsView.addTextChangedListener(new EditRecipeTextWatcher(this));
        notesView.addTextChangedListener(new EditRecipeTextWatcher(this));

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
