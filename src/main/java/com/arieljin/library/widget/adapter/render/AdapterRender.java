package com.arieljin.library.widget.adapter.render;

import android.view.ViewGroup;

import com.arieljin.library.R;

import java.io.Serializable;

/**
 * @time 2018/9/7.
 * @email ariel.jin@tom.com
 */
public abstract class AdapterRender<T extends Serializable> extends AbsRender<AbsAdapterVH>implements AbsAdapterRenderListener<AbsAdapterVH, T> {


    @Override
    public AbsAdapterVH getReusableComponent(ViewGroup parent) {
        AbsAdapterVH vh = new AbsAdapterVH(getViewHolder(parent));
        vh.getItemView().setTag(R.id.ariel_recycler_render_item, this);
        return vh;
    }

    @Override
    public AdapterRender<T> getVhTag(AbsAdapterVH vh){
        return (AdapterRender<T>) vh.getItemView().getTag(R.id.ariel_recycler_render_item);
    }

}
