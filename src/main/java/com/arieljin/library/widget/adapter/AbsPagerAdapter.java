package com.arieljin.library.widget.adapter;

import android.database.DataSetObserver;
import android.support.v4.view.PagerAdapter;
import android.view.View;

import java.util.HashSet;
import java.util.Set;

/**
 * @time 2018/7/24.
 * @email ariel.jin@tom.com
 */
public abstract class AbsPagerAdapter extends PagerAdapter {

    private Set<DataSetObserver> observers;

    @Override
    public boolean isViewFromObject(View view, Object obj) {
        return view == obj;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (observers != null) {
            for (DataSetObserver observer : observers) {
                observer.onChanged();
            }
        }
    }

    public void unregisterObserver(DataSetObserver observer) {
        if (observers != null) {
            observers.remove(observer);
        }
    }

    public void registerObserver(DataSetObserver observer) {
        if (observers == null) {
            observers = new HashSet<DataSetObserver>();
        }
        observers.add(observer);
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
