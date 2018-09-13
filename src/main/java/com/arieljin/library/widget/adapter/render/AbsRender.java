package com.arieljin.library.widget.adapter.render;

import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;

/**
 * @time 2018/9/7.
 * @email ariel.jin@tom.com
 */
abstract class AbsRender<V> {

    protected abstract View getViewHolder(ViewGroup parent);

    public abstract <T extends Serializable> AbsRender<T> getVhTag(V vh);
}
