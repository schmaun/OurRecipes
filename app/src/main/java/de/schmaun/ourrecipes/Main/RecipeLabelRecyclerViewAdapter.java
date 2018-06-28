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
import com.bumptech.glide.request.RequestOptions;

import de.schmaun.ourrecipes.Main.LabelsListFragment.LabelListInteractionListener;
import de.schmaun.ourrecipes.Model.Label;
import de.schmaun.ourrecipes.R;

import java.util.List;

public class RecipeLabelRecyclerViewAdapter extends RecyclerView.Adapter<RecipeLabelRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<Label> labels;
    private final LabelsListFragment.LabelListInteractionListener interactionListener;

    public void setLabels(List<Label> labels) {
        this.labels = labels;
    }

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

    public RecipeLabelRecyclerViewAdapter(Context context, List<Label> recipeLabels, LabelListInteractionListener listener) {
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

        holder.nameView.setText(labels.get(position).getName());

        String imageLocation = labels.get(position).getImageLocation();
        if (imageLocation != null) {
            Glide.with(context).load(imageLocation).apply(new RequestOptions().centerCrop()).into(holder.imageView);
        } else {
            holder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.no_image));
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER);
        }

        //holder.countRecipesView.setText(String.format(Locale.getDefault(), "%,d", labels.get(position).getCountRecipes()));

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != interactionListener) {
                    interactionListener.onLabelsListLabelClick(holder.item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return labels.size();
    }
}
