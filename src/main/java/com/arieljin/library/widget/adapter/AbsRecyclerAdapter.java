package com.arieljin.library.widget.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.arieljin.library.widget.adapter.render.AbsRecyclerAdapterVH;
import com.arieljin.library.widget.adapter.render.AdapterRecyclerRender;

import java.io.Serializable;
import java.util.List;

public class AbsRecyclerAdapter<T extends Serializable> extends RecyclerView.Adapter<AbsRecyclerAdapterVH> {

    private List<T> list;
    private AdapterRecyclerRender render;

    public AbsRecyclerAdapter(List<T> list, AdapterRecyclerRender<T> render) {
        this.list = list;
        this.render = render;
    }

    public void setList(List<T> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public AbsRecyclerAdapterVH onCreateViewHolder(ViewGroup parent, int viewType) {

        AbsRecyclerAdapterVH vh = render.getReusableComponent(parent);
//        vh.itemView.setTag(R.id.ariel_recycler_render_item, render);
        vh.setRender(this.render);
        return vh;
    }

    @Override
    public void onBindViewHolder(AbsRecyclerAdapterVH holder, int position) {
        AdapterRecyclerRender render = holder.getRender();
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


}









