package com.arieljin.library.task;

import android.content.Context;

import com.arieljin.library.abs.AbsRequest;
import com.arieljin.library.listener.OnTaskCompleteListener;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @time 2018/7/20.
 * @email ariel.jin@tom.com
 */
public abstract class LoadMoreRecyclerTask<T extends Serializable> extends RefreshBaseTask<ArrayList<T>> {


    public LoadMoreRecyclerTask(Context context, AbsRequest request) {
        super(context, request);
    }

    public LoadMoreRecyclerTask(Context context, AbsRequest request, OnTaskCompleteListener<ArrayList<T>> completeListener) {
        super(context, request, completeListener);
    }

    public LoadMoreRecyclerTask(Context context, AbsRequest request, boolean needToast, OnTaskCompleteListener<ArrayList<T>> completeListener) {
        super(context, request, needToast, completeListener);
    }
}
