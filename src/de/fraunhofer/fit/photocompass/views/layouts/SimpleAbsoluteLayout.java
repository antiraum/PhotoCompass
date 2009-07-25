package de.fraunhofer.fit.photocompass.views.layouts;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews.RemoteView;

/**
 * A layout with exact locations (x/y coordinates) for its children.
 */
@RemoteView
public class SimpleAbsoluteLayout extends ViewGroup {
    
    public SimpleAbsoluteLayout(final Context context) {

        super(context);
    }
    

    @Override
    protected final void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {

        // find out how big everyone wants to be
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        
        // find rightmost and bottom-most child
        int maxHeight = 0;
        int maxWidth = 0;
        final int count = getChildCount();
        View child;
        LayoutParams lp;
        for (int i = 0; i < count; i++) {
            child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            
            lp = (LayoutParams) child.getLayoutParams();
            
            maxWidth = Math.max(maxWidth, lp.x + child.getMeasuredWidth());
            maxHeight = Math.max(maxHeight, lp.y + child.getMeasuredHeight());
        }
        
        // check against minimum height and width
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
        
        setMeasuredDimension(resolveSize(maxWidth, widthMeasureSpec), resolveSize(maxHeight, heightMeasureSpec));
    }
    

    @Override
    protected final ViewGroup.LayoutParams generateDefaultLayoutParams() {

        return new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0);
    }
    

    @Override
    protected final void onLayout(final boolean changed, final int left, final int top, final int right,
                                  final int bottom) {

        // we just place every child where it wants to be
        final int count = getChildCount();
        View child;
        LayoutParams lp;
        for (int i = 0; i < count; i++) {
            child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            
            lp = (LayoutParams) child.getLayoutParams();
            
            child.layout(lp.x, lp.y, lp.x + child.getMeasuredWidth(), lp.y + child.getMeasuredHeight());
        }
    }
    
    /**
     * Per-child layout information associated with SimpleAbsoluteLayout.
     */
    public static final class LayoutParams extends ViewGroup.LayoutParams {
        
        /**
         * The horizontal, or X, location of the child within the view group.
         */
        public int x;
        
        /**
         * The vertical, or Y, location of the child within the view group.
         */
        public int y;
        
        
        /**
         * Creates a new set of layout parameters with the specified width, height and location.
         * 
         * @param width The width, either {@link #FILL_PARENT}, {@link #WRAP_CONTENT} or a fixed size in pixels.
         * @param height The height, either {@link #FILL_PARENT}, {@link #WRAP_CONTENT} or a fixed size in pixels.
         * @param x The X location of the child.
         * @param y The Y location of the child.
         */
        public LayoutParams(final int width, final int height, final int x, final int y) {

            super(width, height);
            this.x = x;
            this.y = y;
        }
    }
}
