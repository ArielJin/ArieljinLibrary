package com.arieljin.library.task;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.arieljin.library.abs.AbsRequest;
import com.arieljin.library.abs.AbsTask;
import com.arieljin.library.activity.RefreshBaseActivity;
import com.arieljin.library.fragment.RefreshBaseFragment;
import com.arieljin.library.interfaces.RefreshBaseTaskInterface;
import com.arieljin.library.listener.OnTaskCompleteListener;
import com.arieljin.library.listener.OnTaskStatusChangeListener;

import java.io.Serializable;
import java.lang.ref.WeakReference;

public abstract class RefreshBaseTask<T extends Serializable> extends AbsTask<T> implements OnTaskStatusChangeListener {

    private WeakReference referenceContext;

    public RefreshBaseTask(@NonNull Fragment fragment, AbsRequest request) {
        this(fragment.getActivity(), request);
        referenceContext = new WeakReference(fragment);
    }

    public RefreshBaseTask(@NonNull Fragment fragment, AbsRequest request, OnTaskCompleteListener<T> completeListener) {
        this(fragment.getActivity(), request, completeListener);
        referenceContext = new WeakReference(fragment);
    }

    public RefreshBaseTask(@NonNull Fragment fragment, AbsRequest request, boolean needToast, OnTaskCompleteListener<T> completeListener) {
        this(fragment.getActivity(), request, needToast, completeListener);
        referenceContext = new WeakReference(fragment);
    }

    public RefreshBaseTask(Context context, AbsRequest request) {
        super(context, request);
        referenceContext = weakReference;
    }

    public RefreshBaseTask(Context context, AbsRequest request, OnTaskCompleteListener<T> completeListener) {
        super(context, request, completeListener);
        referenceContext = weakReference;
    }

    public RefreshBaseTask(Context context, AbsRequest request, boolean needToast, OnTaskCompleteListener<T> completeListener) {
        super(context, request, needToast, completeListener);
        referenceContext = weakReference;
    }

//    @Override
//    public void start() {
////        if (canStart())
//        super.start();
//    }
//
//    @Override
//    public void start(boolean isLoadMore) {
////        if (canStart())
//        super.start(isLoadMore);
//    }
//
//    @Override
//    public void start(boolean isLoadMore, boolean isRestart) {
////        if (canStart())
//        super.start(isLoadMore, isRestart);
//    }


    private boolean isRefreshBaseTaskInterface() {
        return referenceContext != null && referenceContext.get() != null && referenceContext.get() instanceof RefreshBaseTaskInterface && ((RefreshBaseTaskInterface) referenceContext.get()).getRefreshInterface() != null && (referenceContext.get() instanceof RefreshBaseActivity || (referenceContext.get() instanceof RefreshBaseFragment && ((Fragment) referenceContext.get()).isVisible()));
//            return true;
//        ToastUtil.showErrorToast("current activity must be implements RefreshBaseTaskInterface!");
//        return false;
    }

    @Override
    protected boolean isCanCallbackToUi() {
        return referenceContext != null && referenceContext.get() != null && ((referenceContext.get() instanceof Activity && !((Activity) referenceContext.get()).isFinishing()) || (referenceContext.get() instanceof RefreshBaseFragment && ((RefreshBaseFragment) referenceContext.get()).isVisible()));
    }

    @Override
    public void onTaskStart() {
        if (isRefreshBaseTaskInterface())
            ((RefreshBaseTaskInterface) referenceContext.get()).getRefreshInterface().onTaskStart();
    }

    @Override
    public boolean onTaskSending() {
        return isRefreshBaseTaskInterface() ? ((RefreshBaseTaskInterface) referenceContext.get()).getRefreshInterface().onTaskSending() : isSending;
    }

    @Override
    public void onTaskComplete() {
        if (isRefreshBaseTaskInterface())
            ((RefreshBaseTaskInterface) referenceContext.get()).getRefreshInterface().onTaskComplete();

    }

    @Override
    public void onTaskLoadMoreComplete() {
        if (isRefreshBaseTaskInterface())
            ((RefreshBaseTaskInterface) referenceContext.get()).getRefreshInterface().onTaskLoadMoreComplete();

    }

    @Override
    public void onTaskFailed() {
        if (isRefreshBaseTaskInterface())
            ((RefreshBaseTaskInterface) referenceContext.get()).getRefreshInterface().onTaskFailed();

    }

    @Override
    public void onTaskCancel() {
        if (isRefreshBaseTaskInterface())
            ((RefreshBaseTaskInterface) referenceContext.get()).getRefreshInterface().onTaskCancel();

    }

}
