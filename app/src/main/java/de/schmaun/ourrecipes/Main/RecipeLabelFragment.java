package de.schmaun.ourrecipes.Main;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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

public class RecipeLabelFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    public static final String TAG = "RecipeLabelFragment";
    private int columnCount = 2;
    private OnListFragmentInteractionListener interactionListener;
    private List<Label> labels;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RecipeLabelFragment() {
    }

    public static RecipeLabelFragment newInstance(int columnCount) {
        RecipeLabelFragment fragment = new RecipeLabelFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            columnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        DbHelper db = new DbHelper(getContext());

        long start = System.currentTimeMillis();
        Log.d(TAG, "getLabelsForMain start: " + Long.toString(start));

        labels = LabelsRepository.getInstance(db).getLabelsForMain();

        long end = System.currentTimeMillis();
        Log.d(TAG, "getLabelsForMain end: " + Long.toString(end));
        Log.d(TAG, "getLabelsForMain duration: " + Long.toString(end - start));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_label_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.LayoutManager layoutManager;

            layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(new RecipeLabelRecyclerViewAdapter(getContext(), labels, interactionListener));

            recyclerView.addItemDecoration(new SpacesItemDecoration(8, 2));
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            interactionListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        interactionListener = null;
    }

    public interface OnListFragmentInteractionListener {
        void onRecipeLabelClick(Label label);
    }
}
