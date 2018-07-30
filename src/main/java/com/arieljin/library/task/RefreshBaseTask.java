package com.arieljin.library.task;

import android.content.Context;

import com.arieljin.library.abs.AbsRequest;
import com.arieljin.library.abs.AbsTask;
import com.arieljin.library.activity.RefreshBaseActivity;
import com.arieljin.library.interfaces.RefreshBaseInterface;
import com.arieljin.library.interfaces.RefreshBaseTaskInterface;
import com.arieljin.library.listener.OnTaskCompleteListener;
import com.arieljin.library.listener.OnTaskStatusChangeListener;
import com.arieljin.library.utils.ToastUtil;

import java.io.Serializable;

public abstract class RefreshBaseTask<T extends Serializable> extends AbsTask<T> implements OnTaskStatusChangeListener {

    public RefreshBaseTask(Context context, AbsRequest request) {
        super(context, request);
    }

    public RefreshBaseTask(Context context, AbsRequest request, OnTaskCompleteListener<T> completeListener) {
        super(context, request, completeListener);
    }

    public RefreshBaseTask(Context context, AbsRequest request, boolean needToast, OnTaskCompleteListener<T> completeListener) {
        super(context, request, needToast, completeListener);
    }

    @Override
    public void start() {
        if (canStart())
            super.start();
    }

    @Override
    public void start(boolean isLoadMore) {
        if (canStart())
            super.start(isLoadMore);
    }

    @Override
    public void start(boolean isLoadMore, boolean isRestart) {
        if (canStart())
            super.start(isLoadMore, isRestart);
    }


    private boolean canStart() {
        if (weakReference != null && weakReference.get() != null && weakReference.get() instanceof RefreshBaseTaskInterface && ((RefreshBaseTaskInterface) weakReference.get()).getRefreshInterface() != null)
            return true;
        ToastUtil.showErrorToast("current activity must be implements RefreshBaseTaskInterface!");
        return false;
    }

    @Override
    public void onTaskStart() {
        ((RefreshBaseTaskInterface) weakReference.get()).getRefreshInterface().onTaskStart();
    }

    @Override
    public boolean onTaskSending() {
        return ((RefreshBaseTaskInterface) weakReference.get()).getRefreshInterface().onTaskSending();
    }

    @Override
    public void onTaskComplete() {
        ((RefreshBaseTaskInterface) weakReference.get()).getRefreshInterface().onTaskComplete();

    }

    @Override
    public void onTaskLoadMoreComplete() {

        ((RefreshBaseTaskInterface) weakReference.get()).getRefreshInterface().onTaskLoadMoreComplete();

    }

    @Override
    public void onTaskFailed() {

        ((RefreshBaseTaskInterface) weakReference.get()).getRefreshInterface().onTaskFailed();

    }

    @Override
    public void onTaskCancel() {

        ((RefreshBaseTaskInterface) weakReference.get()).getRefreshInterface().onTaskCancel();

    }

}
