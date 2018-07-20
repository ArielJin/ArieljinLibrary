package com.arieljin.library.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;

import com.arieljin.library.R;
import com.arieljin.library.abs.AbsActivity;
import com.arieljin.library.listener.OnTaskStatusChangeListener;

public class RefreshBaseActivity extends AbsActivity implements OnTaskStatusChangeListener{

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(R.layout.activity_refresh_base);
        swipeRefreshLayout = this.findViewById(R.id.ariel_base_srl);
        swipeRefreshLayout.addView(view);

    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.activity_refresh_base);
        swipeRefreshLayout = this.findViewById(R.id.ariel_base_srl);
        swipeRefreshLayout.addView(LayoutInflater.from(this).inflate(layoutResID, null));
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
    public void onTaskStart() {
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public boolean onTaskSending() {
        return swipeRefreshLayout.isRefreshing();
    }

    protected void setSwipeRefreshLayoutEnabled(boolean enabled){
        swipeRefreshLayout.setEnabled(enabled);
    }

}
