package com.arieljin.library.widget.adapter;

import android.view.View;
import android.view.ViewGroup;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @time 2018/7/31.
 * @email ariel.jin@tom.com
 */
public class BasePagerAdapter<T> extends AbsPagerAdapter {


    protected List<T> list;
    private Set<View> views = new HashSet<>();
    protected AdapterRender<T> render;


    public void setList(List<T> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public BasePagerAdapter(List<T> list, AdapterRender<T> render) {
        this.list = list;
        this.render = render;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = (View) object;
        container.removeView(view);
        views.add(view);
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = null;
        if (views.isEmpty()) {
            view = render.getItemView(container);
        } else {
            view = views.iterator().next();
            views.remove(view);
        }

        render.initDate(view, getItem(position));

        container.addView(view);
        return view;
    }

    @Override
    public int getCount() {
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    public T getItem(int position) {
        if (list != null) {
            return list.get(position);
        }
        return null;
    }


    public static abstract class AdapterRender<T> {

        public AdapterRender() {
        }

        public abstract View getItemView(ViewGroup container);

        public abstract void initDate(View itemView, T t);
    }


}
