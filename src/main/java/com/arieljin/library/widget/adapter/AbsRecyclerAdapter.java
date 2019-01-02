package com.arieljin.library.widget.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.arieljin.library.widget.adapter.render.AbsRecyclerAdapterVH;
import com.arieljin.library.widget.adapter.render.AdapterRecyclerRender;

import java.io.Serializable;
import java.util.List;

public class AbsRecyclerAdapter<T extends Serializable> extends RecyclerView.Adapter<AbsRecyclerAdapterVH> {

    private List<T> list;
    private AdapterRecyclerRender render;
    private AbsRecyclerAdapterVH vh;

    public AbsRecyclerAdapter(List<T> list, AdapterRecyclerRender<T> render) {
        this.list = list;
        this.render = render;
    }

    public void setList(List<T> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void addList(List<T> list) {

        if (list != null && !list.isEmpty()) {
            if (this.list != null) {
                this.list.addAll(list);
            } else {
                this.list = list;
            }
            notifyDataSetChanged();
        }
    }

    public List<T> getList() {
        return list;
    }

    public void clean() {
        if (list != null && !list.isEmpty())
            list.clear();
        notifyDataSetChanged();
    }

    @Override
    public AbsRecyclerAdapterVH onCreateViewHolder(ViewGroup parent, int viewType) {

        vh = render.getReusableComponent(parent);
//        AbsRecyclerAdapterVH vh = render.getReusableComponent(parent);
//        vh.itemView.setTag(R.id.ariel_recycler_render_item, render);
//        vh.setRender(this.render);
        return vh;
    }

    @Override
    public void onBindViewHolder(AbsRecyclerAdapterVH holder, int position) {
//        AdapterRecyclerRender<T> render = holder.getRender();
        AdapterRecyclerRender<T> vhRender = render.getVhTag(holder);
        T t = list.get(position);
        if (vhRender != null) {
            try {

                vhRender.fitDatas(holder, t, position);
                vhRender.fitEvents(holder, t, position);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void onViewRecycled(@NonNull AbsRecyclerAdapterVH holder) {
        super.onViewRecycled(holder);
        AdapterRecyclerRender<T> vhRender = render.getVhTag(holder);
        if (vhRender != null) {
            vhRender.onViewRecycled(holder);
            holder.clear();
        }

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return list != null && !list.isEmpty() ? list.size() : 0;
    }


}









