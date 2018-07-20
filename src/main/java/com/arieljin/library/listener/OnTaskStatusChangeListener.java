package com.arieljin.library.listener;

public interface OnTaskStatusChangeListener {

    void onTaskStart();

    boolean onTaskSending();

    void onTaskComplete();

    void onTaskLoadMoreComplete();

    void onTaskFailed();

    void onTaskCancel();
}
