package com.arieljin.library.task;

import android.content.Context;

import com.arieljin.library.abs.AbsRequest;
import com.arieljin.library.listener.OnTaskCompleteListener;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @time 2018/7/23.
 * @email ariel.jin@tom.com
 */
public abstract class RefreshListTask<T extends Serializable> extends RefreshBaseTask<ArrayList<T>> implements OnTaskCompleteListener<ArrayList<T>> {

    @Override
    protected ArrayList<T> parseJson(JSONObject json) throws Throwable {
        return null;
    }

    public RefreshListTask(Context context, AbsRequest request) {
        super(context, request);
        addListener(this);

    }

    public RefreshListTask(Context context, AbsRequest request, OnTaskCompleteListener<ArrayList<T>> completeListener) {
        super(context, request, completeListener);
    }

    public RefreshListTask(Context context, AbsRequest request, boolean needToast, OnTaskCompleteListener<ArrayList<T>> completeListener) {
        super(context, request, needToast, completeListener);
    }


    @Override
    protected HashMap<String, String> addHeaders() {
        return null;
    }


    @Override
    public void onTaskComplete(ArrayList<T> result) {

    }

    @Override
    public void onTaskLoadMoreComplete(ArrayList<T> result) {

    }

    @Override
    public void onTaskFailed(String error) {

    }

    @Override
    protected String getApiMethodName() {
        return null;
    }
}
