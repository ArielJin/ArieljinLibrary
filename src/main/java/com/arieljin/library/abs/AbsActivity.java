package com.arieljin.library.abs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.arieljin.library.interfaces.ActivityInterface;
import com.arieljin.library.utils.DestoryUtil;


public class AbsActivity extends AppCompatActivity implements ActivityInterface {

    protected boolean hasFinishAnimation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ActivityInterface.MyThread(this, true).start();
    }


    @Override
    protected void onDestroy() {
        AbsActivityManager.onDestroy(this);
        super.onDestroy();
        DestoryUtil.onDestory(this);
        System.gc();
    }

    @Override
    public void setHasFinishAnimation(boolean hasFinishAnimation) {

        this.hasFinishAnimation = hasFinishAnimation;

    }

    @Override
    public void onFinishAnimation(boolean isCreate) {

    }

}
