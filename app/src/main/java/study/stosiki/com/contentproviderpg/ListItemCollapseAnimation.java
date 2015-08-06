package study.stosiki.com.contentproviderpg;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by mike on 8/6/2015.
 *
 * Factored in its own subclass because of the strange and so far inexplicable
 * behaviour of ListView when LayoutParams of one of its's child views are altered
 *
 * The current idea of what's going on is that once child's LayoutParams is affected in some way,
 * the child in question becomes orphaned and indexing is screwed
 */
public class ListItemCollapseAnimation extends Animation {
    private View listItemView;
    private View parentListView;

    public ListItemCollapseAnimation(View listItemView, View parentListView) {
        super();
        this.listItemView = listItemView;
        this.parentListView = parentListView;
    }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            final int initialHeight = listItemView.getHeight();
            if (interpolatedTime >= 1.0f) {
    //                  v.setVisibility(View.INVISIBLE);
            }
            else if (interpolatedTime >= 0.5f){

//                listItemView.getLayoutParams().height =
//                        initialHeight - (int)(initialHeight * interpolatedTime);

                listItemView.invalidate();
                listItemView.requestLayout();
                parentListView.invalidate();
                parentListView.requestLayout();

            }
    //                listView.invalidate();
            parentListView.requestLayout();
        }

        @Override
    public boolean willChangeBounds() {
        return true;
    }
}
