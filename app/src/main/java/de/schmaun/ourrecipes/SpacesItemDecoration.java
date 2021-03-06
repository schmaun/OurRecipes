package de.schmaun.ourrecipes;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private final int space;
    private int spanCount;
    private int childAdapterPosition;

    public SpacesItemDecoration(int space, int spanCount) {
        this.space = space;
        this.spanCount = spanCount;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        childAdapterPosition = parent.getChildAdapterPosition(view);
        outRect.bottom = space;

        // Add top margin only for the first item to avoid double space between items
        if (childAdapterPosition < spanCount) {
            outRect.top = space;
        }
    }
}
