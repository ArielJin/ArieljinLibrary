package com.arieljin.library.widget.adapter.render;

import android.view.ViewGroup;

/**
 * @time 2018/9/7.
 * @email ariel.jin@tom.com
 */
interface AbsAdapterRenderListener<V, T> {


    V getReusableComponent(ViewGroup parent);

    void fitDatas(V v,T t, int position);

    void fitEvents(V v,T t, int position);

}
