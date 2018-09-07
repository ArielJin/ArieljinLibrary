package com.arieljin.library.widget.adapter.render;

import android.view.View;

/**
 * @time 2018/9/7.
 * @email ariel.jin@tom.com
 */
interface AdapterVHI {

    <V extends View> V obtainView(int id);

    <V> V obtainView(int id, Class<V> viewClazz);

    View getItemView();
}
