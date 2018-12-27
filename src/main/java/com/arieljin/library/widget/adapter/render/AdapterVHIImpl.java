package com.arieljin.library.widget.adapter.render;

import android.view.View;

import java.util.WeakHashMap;

/**
 * @time 2018/9/7.
 * @email ariel.jin@tom.com
 */
class AdapterVHIImpl implements AdapterVHI {

    //    private SparseArray<View> holder = null;
    private WeakHashMap<Integer, View> myWeakHashMap = null;
    private View itemView;

    public AdapterVHIImpl(View itemView) {
        this.itemView = itemView;
    }

    @Override
    public <V extends View> V obtainView(int id) {
        if (myWeakHashMap == null) {
            myWeakHashMap = new WeakHashMap<>();
        }

        View view = myWeakHashMap.get(id);

        if (view != null) {
            return (V) view;
        } else {
            view = this.itemView.findViewById(id);
            if (view == null) {
                return null;
            } else {
                myWeakHashMap.put(id, view);
                return (V) view;
            }
        }


//        if (null == this.holder) {
//            this.holder = new SparseArray();
//        }
//
//        View view = this.holder.get(id);
//        if (view != null) {
//            return (V) view;
//        } else {
//            view = this.itemView.findViewById(id);
//            if (view == null) {
//                return null;
//            } else {
//                this.holder.put(id, view);
//                return (V) view;
//            }
//        }
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

    public void clear() {
        if (myWeakHashMap != null)
            myWeakHashMap.clear();
    }
}
