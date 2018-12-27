package com.arieljin.library.widget.adapter.render;

import android.view.ViewGroup;

import com.arieljin.library.R;

import java.io.Serializable;

/**
 * @time 2018/9/7.
 * @email ariel.jin@tom.com
 */
public abstract class AdapterRecyclerRender<T extends Serializable> extends AbsRender<AbsRecyclerAdapterVH> implements AbsAdapterRenderListener<AbsRecyclerAdapterVH, T> {

    private AbsRecyclerAdapterVH vh;

    @Override
    public AbsRecyclerAdapterVH getReusableComponent(ViewGroup parent) {
        vh = new AbsRecyclerAdapterVH(getViewHolder(parent));
        vh.itemView.setTag(R.id.ariel_recycler_render_item, this);

        return vh;
    }

    @Override
    public AdapterRecyclerRender<T> getVhTag(AbsRecyclerAdapterVH vh) {
        return (AdapterRecyclerRender<T>) vh.itemView.getTag(R.id.ariel_recycler_render_item);
    }

    /**
     * 在RecyclerView onViewRecycled时进行内存回收操作 防止OOM
     *
     * @param vh
     */
    public abstract void onViewRecycled(AbsRecyclerAdapterVH vh);
}
