package com.arieljin.library.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;

import com.arieljin.library.R;
import com.arieljin.library.abs.AbsActivity;
import com.arieljin.library.impl.RefreshBaseImpl;
import com.arieljin.library.interfaces.RefreshBaseInterface;
import com.arieljin.library.interfaces.RefreshBaseTaskInterface;
import com.arieljin.library.task.RefreshBaseTask;

import java.io.Serializable;

public class RefreshBaseActivity extends AbsActivity implements RefreshBaseTaskInterface {

    private RefreshBaseInterface refreshBaseInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(R.layout.activity_refresh_base);
        SwipeRefreshLayout swipeRefreshLayout = this.findViewById(R.id.ariel_base_srl);
        swipeRefreshLayout.addView(view);
        refreshBaseInterface = new RefreshBaseImpl(swipeRefreshLayout);
//        refreshBaseInterface.setContentView(view);
        registerRefreshListener();

    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.activity_refresh_base);
        SwipeRefreshLayout swipeRefreshLayout = this.findViewById(R.id.ariel_base_srl);
        View contentView = LayoutInflater.from(this).inflate(layoutResID, null);
        swipeRefreshLayout.addView(contentView);
        refreshBaseInterface = new RefreshBaseImpl(swipeRefreshLayout);
//        refreshBaseInterface.setContentView(contentView);
        registerRefreshListener();
    }

    @Override
    public RefreshBaseInterface getRefreshInterface() {
        return refreshBaseInterface.getRefreshInterface();
    }

    @Override
    public void setSwipeRefreshLayoutEnabled(boolean enabled) {
        refreshBaseInterface.setSwipeRefreshLayoutEnabled(enabled);
    }

    @Override
    public <T extends Serializable> void addTask(RefreshBaseTask<T> task) {
        refreshBaseInterface.addTask(task);

    }

    @Override
    public void startTasks() {
        refreshBaseInterface.startTasks();

    }

    @Override
    public void registerRefreshListener() {

        refreshBaseInterface.registerRefreshListener();


    }

    @Override
    public void registerRefreshListener(SwipeRefreshLayout.OnRefreshListener onRefreshListener) {
        refreshBaseInterface.registerRefreshListener(onRefreshListener);
    }

    @Override
    public void unRegisterRefreshListener() {

        refreshBaseInterface.unRegisterRefreshListener();

    }

    @Override
    public void setRefreshing(boolean refreshing) {
        refreshBaseInterface.setRefreshing(refreshing);
    }

}
