package com.arieljin.library.interfaces;

import com.arieljin.library.listener.OnTaskStatusChangeListener;
import com.arieljin.library.task.RefreshBaseTask;

import java.io.Serializable;

/**
 * @time 2018/7/26.
 * @email ariel.jin@tom.com
 */
public interface RefreshBaseInterface extends OnTaskStatusChangeListener {

    void setSwipeRefreshLayoutEnabled(boolean enabled);

    <T extends Serializable>  void setTask(RefreshBaseTask<T> task);

    void startTask();

    void setOnRefreshListener();


}
