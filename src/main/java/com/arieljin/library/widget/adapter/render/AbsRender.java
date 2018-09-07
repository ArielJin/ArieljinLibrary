package com.arieljin.library.widget.adapter.render;

import android.view.View;
import android.view.ViewGroup;

/**
 * @time 2018/9/7.
 * @email ariel.jin@tom.com
 */
abstract class AbsRender<V> {

    protected V vh;

    protected abstract View getViewHolder(ViewGroup parent);
}
