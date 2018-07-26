package com.arieljin.library.abs;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.arieljin.library.interfaces.ActivityInterface;
import com.arieljin.library.utils.DestoryUtil;


public class AbsActivity extends AppCompatActivity implements ActivityInterface {

    protected boolean hasFinishAnimation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindows();
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

    private void initWindows(){

        if (setWindowsFullScreen() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

//                     | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                getWindow().setNavigationBarColor(Color.TRANSPARENT);
            getWindow().setStatusBarColor(setStatusBarColor());

        }

    }

    protected boolean setWindowsFullScreen(){
        return true;
    }

    protected int setStatusBarColor() {
        if (!setWindowsFullScreen())
            throw new IllegalStateException("Method setWindowsFullScreen() must be return true");
        return Color.TRANSPARENT;
    }

}
