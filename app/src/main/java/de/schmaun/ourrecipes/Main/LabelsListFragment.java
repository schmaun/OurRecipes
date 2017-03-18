package de.schmaun.ourrecipes.Main;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.schmaun.ourrecipes.Database.DbHelper;
import de.schmaun.ourrecipes.Database.LabelsRepository;
import de.schmaun.ourrecipes.Model.Label;
import de.schmaun.ourrecipes.R;
import de.schmaun.ourrecipes.SpacesItemDecoration;
import de.schmaun.ourrecipes.Utils.StopWatch;

public class LabelsListFragment extends Fragment {
    public static final String TAG = "LabelsListFragment";
    private LabelListInteractionListener interactionListener;
    private List<Label> labels;

    public LabelsListFragment() {
    }

    public static LabelsListFragment newInstance() {
        LabelsListFragment fragment = new LabelsListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DbHelper db = new DbHelper(getContext());

        StopWatch stopWatch = StopWatch.createAndStart();
        labels = LabelsRepository.getInstance(db).getLabelsForMain();

        Label label = new Label();
        label.setId(-1);
        label.setName(getString(R.string.not_labeled));
        labels.add(label);

        Log.d(TAG, "getLabelsForMain duration: " + Long.toString(stopWatch.stop()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        float scale = getResources().getDisplayMetrics().density;

        RecyclerView view = (RecyclerView)inflater.inflate(R.layout.fragment_main_label_list, container, false);

        LayoutManager layoutManager = new LinearLayoutManager(getContext());
        view.setLayoutManager(layoutManager);
        view.setAdapter(new RecipeLabelRecyclerViewAdapter(getContext(), labels, interactionListener));
        view.addItemDecoration(new SpacesItemDecoration((int) (scale*4f), 1));

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LabelListInteractionListener) {
            interactionListener = (LabelListInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement RecipeListInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        interactionListener = null;
    }

    public interface LabelListInteractionListener {
        void onLabelsListLabelClick(Label label);
    }
}
