package com.arieljin.library.widget.adapter.render;

import android.view.View;

/**
 * @time 2018/9/7.
 * @email ariel.jin@tom.com
 */
public class AbsAdapterVH implements AdapterVHI {

    private AdapterVHI adapterVHI;

    public AbsAdapterVH(View itemView) {
        adapterVHI = new AdapterVHIImpl(itemView);
    }

    @Override
    public <V extends View> V obtainView(int id) {
        return adapterVHI.obtainView(id);
    }

    @Override
    public <V> V obtainView(int id, Class<V> viewClazz) {
        return adapterVHI.obtainView(id, viewClazz);
    }

    @Override
    public View getItemView() {
        return adapterVHI.getItemView();
    }
}
