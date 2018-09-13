package com.arieljin.library.widget.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.arieljin.library.widget.adapter.render.AbsAdapterVH;
import com.arieljin.library.widget.adapter.render.AdapterRender;

import java.io.Serializable;
import java.util.List;

/**
 * @time 2018/9/7.
 * @email ariel.jin@tom.com
 */
public class AbsBaseAdapter<T extends Serializable> extends BaseAdapter {

    private List<T> list;
    private AdapterRender render;

    public AbsBaseAdapter(List<T> list,AdapterRender render) {
        this.list = list;
        this.render = render;
    }

    public void setList(List<T> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list != null? list.size():0;
    }

    @Override
    public T getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
//        ViewHoder viewHoder;
        AbsAdapterVH vh;
        if (convertView == null) {
            vh = render.getReusableComponent(viewGroup);
            convertView = vh.getItemView();
//            convertView = render.getReusableComponent(viewGroup).getItemView();
//            viewHoder = new ViewHoder(render);
            convertView.setTag(vh);
        } else {
//            viewHoder = (ViewHoder) convertView.getTag();
            vh = (AbsAdapterVH) convertView.getTag();
        }

        AdapterRender vhRend = render.getVhTag(vh);
        vhRend.fitDatas(vh, getItem(position), position);
        vhRend.fitEvents(vh, getItem(position), position);

        return convertView;
    }

//    static class ViewHoder {
//
//        private AdapterRender render;
//
//        public ViewHoder(AdapterRender render) {
//            this.render = render;
//        }
//    }


}
