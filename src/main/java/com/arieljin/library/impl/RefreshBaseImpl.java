package com.arieljin.library.impl;

import android.support.v4.widget.SwipeRefreshLayout;

import com.arieljin.library.interfaces.RefreshBaseInterface;
import com.arieljin.library.task.RefreshBaseTask;

import java.io.Serializable;

/**
 * @time 2018/7/26.
 * @email ariel.jin@tom.com
 */
public class RefreshBaseImpl implements RefreshBaseInterface {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RefreshBaseTask task;

    private RefreshBaseImpl() {
    }

    public RefreshBaseImpl(SwipeRefreshLayout swipeRefreshLayout) {
        this.swipeRefreshLayout = swipeRefreshLayout;
        init();
    }


    private void init(){
    }

    @Override
    public void onTaskStart() {

        swipeRefreshLayout.setRefreshing(true);

    }

    @Override
    public boolean onTaskSending() {
        return swipeRefreshLayout.isRefreshing();
    }

    @Override
    public void onTaskComplete() {
        swipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void onTaskLoadMoreComplete() {
        swipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void onTaskFailed() {
        swipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void onTaskCancel() {
        swipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void setSwipeRefreshLayoutEnabled(boolean enabled) {
        swipeRefreshLayout.setEnabled(enabled);
    }

    @Override
    public <T extends Serializable> void setTask(RefreshBaseTask<T> task) {
        this.task = task;
    }

    @Override
    public void startTask() {

        if (task != null) {
            task.start();
        } else {
            if (swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);
        }

    }

    @Override
    public void setOnRefreshListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startTask();
            }
        });
    }

}
