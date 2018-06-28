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
    private RecipeLabelRecyclerViewAdapter labelAdapter;

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

        loadLabels();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        float scale = getResources().getDisplayMetrics().density;

        RecyclerView view = (RecyclerView)inflater.inflate(R.layout.fragment_main_label_list, container, false);

        LayoutManager layoutManager = new LinearLayoutManager(getContext());
        view.setLayoutManager(layoutManager);

        labelAdapter = new RecipeLabelRecyclerViewAdapter(getContext(), labels, interactionListener);
        view.setAdapter(labelAdapter);
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

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.app_name);

        loadLabels();
        labelAdapter.setLabels(labels);
        labelAdapter.notifyDataSetChanged();
    }

    private void loadLabels() {
        DbHelper db = new DbHelper(getContext());

        StopWatch stopWatch = StopWatch.createAndStart();
        labels = LabelsRepository.getInstance(db).getLabelsForMain();

        Label label = new Label();
        label.setId(-1);
        label.setName(getString(R.string.not_labeled));
        labels.add(label);

        Log.d(TAG, "getLabelsForMain duration: " + Long.toString(stopWatch.stop()));
    }

    public interface LabelListInteractionListener {
        void onLabelsListLabelClick(Label label);
    }
}
