package com.arieljin.library.impl;

import android.support.v4.widget.SwipeRefreshLayout;

import com.arieljin.library.interfaces.RefreshBaseInterface;
import com.arieljin.library.task.RefreshBaseTask;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @time 2018/7/26.
 * @email ariel.jin@tom.com
 */
public class RefreshBaseImpl implements RefreshBaseInterface {

    private SwipeRefreshLayout swipeRefreshLayout;
    private Set<RefreshBaseTask> taskSet;
    private Iterator<RefreshBaseTask> iterator = null;

    private RefreshBaseImpl() {
    }

    public RefreshBaseImpl(SwipeRefreshLayout swipeRefreshLayout) {
        this.swipeRefreshLayout = swipeRefreshLayout;
        init();
    }


    private void init() {
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
        if (iterator != null && iterator.hasNext())
            iterator.next().start();
        else
            swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onTaskLoadMoreComplete() {
        swipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void onTaskFailed() {
        if (iterator != null && iterator.hasNext())
            iterator.next().start();
        else
            swipeRefreshLayout.setRefreshing(false);


    }

    @Override
    public void onTaskCancel() {
        if (iterator != null && iterator.hasNext())
            iterator.next().start();
        else
            swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public RefreshBaseInterface getRefreshInterface() {
        return this;
    }

    @Override
    public void setSwipeRefreshLayoutEnabled(boolean enabled) {
        swipeRefreshLayout.setEnabled(enabled);
    }

    @Override
    public <T extends Serializable> void addTask(RefreshBaseTask<T> task) {
        if (task == null)
            return;
        if (taskSet == null)
            taskSet = new HashSet<>();
        taskSet.add(task);
    }

    @Override
    public void startTasks() {

        if (taskSet != null && !taskSet.isEmpty()) {
            iterator = taskSet.iterator();
            if (iterator.hasNext())
                iterator.next().start();
        } else {
            if (iterator != null)
                iterator = null;
            if (swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);
        }

    }

    @Override
    public void setOnRefreshListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startTasks();
            }
        });
    }

}
