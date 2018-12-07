package com.arieljin.library.widget.recyclerview.swipe;

import android.content.Context;
import android.support.annotation.IntDef;
import android.widget.LinearLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @time 2018/8/13.
 * @email ariel.jin@tom.com
 */
public class SwipeMenu {


    @IntDef({HORIZONTAL, VERTICAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface OrientationMode {
    }

    public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
    public static final int VERTICAL = LinearLayout.VERTICAL;

    private SwipeMenuLayout mSwipeMenuLayout;

    private int mViewType;

    private int orientation = HORIZONTAL;

//    private List<SwipeMenuItem> mSwipeMenuItems = new ArrayList<>(2);
    private SwipeMenuItem swipeMenuItem = null;

    public SwipeMenu(SwipeMenuLayout swipeMenuLayout, int viewType) {
        this.mSwipeMenuLayout = swipeMenuLayout;
        this.mViewType = viewType;
    }

    /**
     * Set a percentage.
     *
     * @param openPercent such as 0.5F.
     */
    public void setOpenPercent(float openPercent) {
        if (openPercent != mSwipeMenuLayout.getOpenPercent()) {
            openPercent = openPercent > 1 ? 1 : (openPercent < 0 ? 0 : openPercent);
            mSwipeMenuLayout.setOpenPercent(openPercent);
        }
    }

    /**
     * The duration of the set.
     *
     * @param scrollerDuration such 500.
     */
    public void setScrollerDuration(int scrollerDuration) {
        this.mSwipeMenuLayout.setScrollerDuration(scrollerDuration);
    }

    /**
     * Set the menu orientation.
     *
     * @param orientation use {@link SwipeMenu#HORIZONTAL} or {@link SwipeMenu#VERTICAL}.
     * @see SwipeMenu#HORIZONTAL
     * @see SwipeMenu#VERTICAL
     */
    public void setOrientation(@OrientationMode int orientation) {
        if (orientation != HORIZONTAL && orientation != VERTICAL)
            throw new IllegalArgumentException("Use SwipeMenu#HORIZONTAL or SwipeMenu#VERTICAL.");
        this.orientation = orientation;
    }

    /**
     * Get the menu orientation.
     *
     * @return {@link SwipeMenu#HORIZONTAL} or {@link SwipeMenu#VERTICAL}.
     */
    @OrientationMode
    public int getOrientation() {
        return orientation;
    }

//    public void addMenuItem(SwipeMenuItem item) {
//        mSwipeMenuItems.add(item);
//    }
//
//    public void removeMenuItem(SwipeMenuItem item) {
//        mSwipeMenuItems.remove(item);
//    }


    public void setSwipeMenuItem(SwipeMenuItem swipeMenuItem) {
        this.swipeMenuItem = swipeMenuItem;
    }

//    public List<SwipeMenuItem> getMenuItems() {
//        return mSwipeMenuItems;
//    }


    public SwipeMenuItem getSwipeMenuItem() {
        return swipeMenuItem;
    }

//    public SwipeMenuItem getMenuItem(int index) {
//        return mSwipeMenuItems.get(index);
//    }

    public void smoothCloseRightMenu(){
        mSwipeMenuLayout.smoothCloseRightMenu();
    }

    public Context getContext() {
        return mSwipeMenuLayout.getContext();
    }

    public int getViewType() {
        return mViewType;
    }

}
