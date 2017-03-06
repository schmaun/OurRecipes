package de.schmaun.ourrecipes.Main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import de.schmaun.ourrecipes.Main.RecipeLabelFragment.OnListFragmentInteractionListener;
import de.schmaun.ourrecipes.Model.Label;
import de.schmaun.ourrecipes.R;

import java.util.List;
import java.util.Locale;

public class RecipeLabelRecyclerViewAdapter extends RecyclerView.Adapter<RecipeLabelRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private final List<Label> labels;
    private final OnListFragmentInteractionListener interactionListener;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final ImageView imageView;
        public final TextView nameView;
        //public final TextView countRecipesView;
        public Label item;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            imageView = (ImageView) view.findViewById(R.id.card_view_image);
            nameView = (TextView) view.findViewById(R.id.card_view_label_name);
            //countRecipesView = (TextView) view.findViewById(R.id.card_view_recipe_count);
        }
    }

    public RecipeLabelRecyclerViewAdapter(Context context, List<Label> recipeLabels, OnListFragmentInteractionListener listener) {
        this.context = context;
        labels = recipeLabels;
        interactionListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_main_label_row_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.item = labels.get(position);

        Glide.with(context).load(labels.get(position).getImageLocation()).centerCrop().into(holder.imageView);

        holder.nameView.setText(labels.get(position).getName());
        //holder.countRecipesView.setText(String.format(Locale.getDefault(), "%,d", labels.get(position).getCountRecipes()));

        //float heightFactor = (labels.get(position).getCountRecipes() / 1);
        double heightFactor = Math.log((double)labels.get(position).getCountRecipes());
        if (heightFactor > 1) {
            ViewGroup.LayoutParams layoutParams = holder.imageView.getLayoutParams();
            //layoutParams.height = Math.round(layoutParams.height * (float) heightFactor);
        }

        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.view.getLayoutParams();

            layoutParams.setFullSpan(holder.item.isFullSpan());

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != interactionListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    //interactionListener.onRecipeLabelClick(holder.item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return labels.size();
    }
}
