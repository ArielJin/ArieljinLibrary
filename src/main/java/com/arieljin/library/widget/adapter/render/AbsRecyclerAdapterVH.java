package com.arieljin.library.widget.adapter.render;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @time 2018/9/7.
 * @email ariel.jin@tom.com
 */
public class AbsRecyclerAdapterVH extends RecyclerView.ViewHolder implements AdapterVHI{

    private AdapterVHI adapterVHI;
    private AdapterRecyclerRender render;


    public AbsRecyclerAdapterVH(View itemView) {
        super(itemView);
        this.adapterVHI = new AdapterVHIImpl(itemView);
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

    public void setRender(AdapterRecyclerRender render) {
        this.render = render;
    }

    public AdapterRecyclerRender getRender() {
        return render;
    }
}
