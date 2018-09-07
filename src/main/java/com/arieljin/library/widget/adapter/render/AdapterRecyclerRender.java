package com.arieljin.library.widget.adapter.render;

import android.view.ViewGroup;

import java.io.Serializable;

/**
 * @time 2018/9/7.
 * @email ariel.jin@tom.com
 */
public abstract class AdapterRecyclerRender<T extends Serializable> extends AbsRender<AbsRecyclerAdapterVH> implements AbsAdapterRenderListener<AbsRecyclerAdapterVH,T> {

    @Override
    public AbsRecyclerAdapterVH getReusableComponent(ViewGroup parent) {
        vh = new AbsRecyclerAdapterVH(getViewHolder(parent));
        return vh;
    }
}
