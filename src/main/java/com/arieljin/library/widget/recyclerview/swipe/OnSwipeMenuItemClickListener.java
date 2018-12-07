package com.arieljin.library.widget.recyclerview.swipe;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.io.Serializable;

/**
 * @time 2018/8/14.
 * @email ariel.jin@tom.com
 */
public interface OnSwipeMenuItemClickListener<T extends Serializable> {

    void onMenuItemClick(View view,int position, T t);

    RecyclerView getRecyclerView();
}
