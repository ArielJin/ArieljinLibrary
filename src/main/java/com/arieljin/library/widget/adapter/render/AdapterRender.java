package com.arieljin.library.widget.adapter.render;

import android.view.ViewGroup;

import java.io.Serializable;

/**
 * @time 2018/9/7.
 * @email ariel.jin@tom.com
 */
public abstract class AdapterRender<T extends Serializable> extends AbsRender<AbsAdapterVH>implements AbsAdapterRenderListener<AbsAdapterVH, T> {


    @Override
    public AbsAdapterVH getReusableComponent(ViewGroup parent) {
        vh = new AbsAdapterVH(getViewHolder(parent));
        return vh;
    }

}
