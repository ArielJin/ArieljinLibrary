package com.arieljin.library.widget.recyclerview.decoration;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @time 2018/9/3.
 * @email ariel.jin@tom.com
 */
public class SpacesItemDecoration extends RecyclerView.ItemDecoration {

    private Builder builder;

    private SpacesItemDecoration() {
    }

    private SpacesItemDecoration(@NonNull Builder builder) {
        this.builder = builder;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {


        if (builder.spaces.top > 0)
            outRect.top = builder.spaces.top;
        if (builder.spaces.left > 0)
            outRect.left = builder.spaces.left;
        if (builder.spaces.right > 0)
            outRect.right = builder.spaces.right;
        if (builder.spaces.bottom > 0)
            outRect.bottom = builder.spaces.bottom;
        if (builder.spaces.recyclerTop > 0 && parent.getChildAdapterPosition(view) == 0) {

            outRect.top = builder.spaces.recyclerTop;
        }

    }

    public static class Builder {

        private Spaces spaces;

        public Builder() {
            this.spaces = new Spaces();
        }

        public SpacesItemDecoration.Builder setTop(int top){

            spaces.top = top;
            return this;
        }

        public SpacesItemDecoration.Builder setBottom(int bottom){

            spaces.bottom = bottom;
            return this;
        }

        public SpacesItemDecoration.Builder setLeft(int left){

            spaces.left = left;
            return this;
        }

        public SpacesItemDecoration.Builder setRight(int right){

            spaces.right = right;
            return this;
        }

        public SpacesItemDecoration.Builder setRecyclerTop(int recyclerTop){

            spaces.recyclerTop = recyclerTop;
            return this;
        }


        public SpacesItemDecoration create(){

            return new  SpacesItemDecoration(this);

        }


    }

    private static class Spaces {

        public int top;
        public int bottom;
        public int left;
        public int right;
        public int recyclerTop;

        private Spaces() {
        }
    }




}
