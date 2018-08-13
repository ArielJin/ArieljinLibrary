package com.arieljin.library.widget.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.arieljin.library.R;

import java.io.Serializable;
import java.util.List;

public class AbsRecyclerAdapter<T extends Serializable> extends RecyclerView.Adapter<AbsRecyclerAdapter.AbsRecyclerVH> {

    private List<T> list;
    private AdapterRender render;

    public AbsRecyclerAdapter(List<T> list, AdapterRender<T> render) {
        this.list = list;
        this.render = render;
    }

    @Override
    public AbsRecyclerVH onCreateViewHolder(ViewGroup parent, int viewType) {

        AbsRecyclerVH vh = render.getReusableComponent(parent);
        vh.itemView.setTag(R.id.ariel_recycler_render_item, render);
        return vh;
    }

    @Override
    public void onBindViewHolder(AbsRecyclerVH holder, int position) {
        AbsAdapterRender render = (AbsAdapterRender) holder.itemView.getTag(R.id.ariel_recycler_render_item);
        T t = list.get(position);
        if (render != null) {
            render.fitDatas(t);
            render.fitEvents(t);
        }

    }

    @Override
    public int getItemCount() {
        return list != null && !list.isEmpty() ? list.size() : 0;
    }

    public static class AbsRecyclerVH extends RecyclerView.ViewHolder {

        private SparseArray<View> holder = null;

        public AbsRecyclerVH(View itemView) {
            super(itemView);
        }


        public <V extends View> V obtainView(int id) {
            if (null == this.holder) {
                this.holder = new SparseArray();
            }

            View view = (View) this.holder.get(id);
            if (view != null) {
                return (V) view;
            } else {
                view = this.itemView.findViewById(id);
                if (view == null) {
                    return null;
                } else {
                    this.holder.put(id, view);
                    return (V) view;
                }
            }
        }

        public <V> V obtainView(int id, Class<V> viewClazz) {
            View view = this.obtainView(id);
            return null == view ? null : (V) view;
        }
    }


    public static abstract class AdapterRender<T extends Serializable> implements AbsAdapterRender<AbsRecyclerVH, T> {

        protected AbsRecyclerVH vh;


        @Override
        public AbsRecyclerVH getReusableComponent(ViewGroup parent) {
            vh = new AbsRecyclerVH(getViewHolder(parent));
            return vh;
        }


        protected abstract View getViewHolder(ViewGroup parent);

    }

    interface AbsAdapterRender<V, T> {

        V getReusableComponent(ViewGroup parent);

        void fitEvents(T t);

        void fitDatas(T t);
    }


}









