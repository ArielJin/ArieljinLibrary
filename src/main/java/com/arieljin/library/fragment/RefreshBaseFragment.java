package com.arieljin.library.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arieljin.library.R;
import com.arieljin.library.abs.AbsFragment;
import com.arieljin.library.impl.RefreshBaseImpl;
import com.arieljin.library.interfaces.RefreshBaseInterface;
import com.arieljin.library.interfaces.RefreshBaseTaskInterface;
import com.arieljin.library.task.RefreshBaseTask;

import java.io.Serializable;

/**
 * @time 2018/7/26.
 * @email ariel.jin@tom.com
 */
public abstract class RefreshBaseFragment extends AbsFragment implements RefreshBaseTaskInterface {

    private RefreshBaseInterface refreshBaseInterface;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_refresh_base, container, false);
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.ariel_base_srl);
        swipeRefreshLayout.addView(onCreateContentView(inflater, container, savedInstanceState));
        refreshBaseInterface = new RefreshBaseImpl(swipeRefreshLayout);
        init();
        return view;
    }

    protected void init(){

    }

    @Override
    public RefreshBaseInterface getRefreshInterface() {
        return refreshBaseInterface;
    }

    @Override
    public void setSwipeRefreshLayoutEnabled(boolean enabled) {
        refreshBaseInterface.setSwipeRefreshLayoutEnabled(enabled);

    }

    @Override
    public <T extends Serializable> void setTask(RefreshBaseTask<T> task) {
        refreshBaseInterface.setTask(task);

    }

    @Override
    public void startTask() {
        refreshBaseInterface.startTask();

    }

    @Override
    public void setOnRefreshListener() {
        refreshBaseInterface.setOnRefreshListener();
    }

    public abstract View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

}
