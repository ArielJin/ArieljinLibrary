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
//    private View contentView, errorView;
//    private boolean hasComplete = false, hasErrorPage = false;

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

        setRefreshing(true);

    }

    @Override
    public boolean onTaskSending() {
        return swipeRefreshLayout.isRefreshing();
    }

    @Override
    public void onTaskComplete() {
//        hasComplete = true;
//        if (hasErrorPage) {
//            // TODO: 2018/12/12 刷新成功  加载正常界面
//            hasErrorPage = false;
//            swipeRefreshLayout.removeView(errorView);
//            swipeRefreshLayout.addView(contentView);
//        }
        if (iterator != null && iterator.hasNext())
            iterator.next().start();
        else
            setRefreshing(false);
    }

    @Override
    public void onTaskLoadMoreComplete() {
        setRefreshing(false);

    }

    @Override
    public void onTaskFailed() {
        if (iterator != null && iterator.hasNext())
            iterator.next().start();
        else {
//            if (!hasComplete && !hasErrorPage) {
//                // TODO: 2018/12/12 加载错误的界面
//                hasErrorPage = true;
//
//                swipeRefreshLayout.removeView(contentView);
//                if (errorView == null) {
//                    errorView = LayoutInflater.from(swipeRefreshLayout.getContext()).inflate(R.layout.activity_refresh_error, null);
//                    errorView.findViewById(R.id.tv_ariel_refresh).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            startTasks();
//                        }
//                    });
//                }
//
//
//                swipeRefreshLayout.addView(errorView);
//
//
//            }

            setRefreshing(false);

        }

    }

    @Override
    public void onTaskCancel() {
        if (iterator != null && iterator.hasNext())
            iterator.next().start();
        else
            setRefreshing(false);
    }

    @Override
    public RefreshBaseInterface getRefreshInterface() {
        return this;
    }

    @Override
    public void setSwipeRefreshLayoutEnabled(boolean enabled) {
        if (enabled)
            registerRefreshListener();
        else
            unRegisterRefreshListener();
        swipeRefreshLayout.setEnabled(enabled);
    }

//    @Override
//    public void setContentView(View view) {
//        this.contentView = view;
//
//    }

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
                setRefreshing(false);
        }

    }

    @Override
    public void registerRefreshListener() {

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startTasks();
            }
        });

    }

    @Override
    public void registerRefreshListener(SwipeRefreshLayout.OnRefreshListener onRefreshListener) {
        unRegisterRefreshListener();
        swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
    }

    @Override
    public void unRegisterRefreshListener() {
        swipeRefreshLayout.setOnRefreshListener(null);
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        swipeRefreshLayout.setRefreshing(refreshing);
    }


}
