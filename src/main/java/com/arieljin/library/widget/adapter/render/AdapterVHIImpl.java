package com.arieljin.library.widget.adapter.render;

import android.util.SparseArray;
import android.view.View;

/**
 * @time 2018/9/7.
 * @email ariel.jin@tom.com
 */
class AdapterVHIImpl implements AdapterVHI {

    private SparseArray<View> holder = null;
    private View itemView;

    public AdapterVHIImpl(View itemView) {
        this.itemView = itemView;
    }

    @Override
    public <V extends View> V obtainView(int id) {
        if (null == this.holder) {
            this.holder = new SparseArray();
        }

        View view = (View) this.holder.get(id);
        if (view != null) {
            return (V) view;
        } else {
            view = this.itemView.findViewById(id);
            if (view == null) {
                return null;
            } else {
                this.holder.put(id, view);
                return (V) view;
            }
        }
    }

    @Override
    public <V> V obtainView(int id, Class<V> viewClazz) {
        View view = this.obtainView(id);
        return null == view ? null : (V) view;
    }

    @Override
    public View getItemView() {
        return itemView;
    }
}
