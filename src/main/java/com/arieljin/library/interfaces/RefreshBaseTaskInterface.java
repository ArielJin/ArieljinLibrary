package com.arieljin.library.interfaces;

import android.support.v4.widget.SwipeRefreshLayout;

import com.arieljin.library.task.RefreshBaseTask;

import java.io.Serializable;

/**
 * @time 2018/7/26.
 * @email ariel.jin@tom.com
 */
public interface RefreshBaseTaskInterface {

    RefreshBaseInterface getRefreshInterface();

    void setSwipeRefreshLayoutEnabled(boolean enabled);

    <T extends Serializable>  void addTask(RefreshBaseTask<T> task);

    void startTasks();

    void registerRefreshListener();

    void registerRefreshListener(SwipeRefreshLayout.OnRefreshListener onRefreshListener);

    void unRegisterRefreshListener();

    void setRefreshing(boolean refreshing);

}
