package com.arieljin.library.abs;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.arieljin.library.interfaces.ActivityInterface;
import com.arieljin.library.utils.DestoryUtil;
import com.arieljin.library.utils.StatusBarUtil;


public class AbsActivity extends AppCompatActivity implements ActivityInterface {

    protected boolean hasFinishAnimation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AbsActivityManager.onCreate(this);
        initWindowsStatusBar(setStatusBarColor());
        new ActivityInterface.MyThread(this, true).start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        AbsActivityManager.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AbsActivityManager.onPause(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        new ActivityInterface.MyThread(this, false).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AbsActivityManager.onDestroy(this);
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

    private void initWindowsStatusBar(int statusBarColor) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (statusBarColor == Color.TRANSPARENT) {
                View decorView = getWindow().getDecorView();

                decorView.setSystemUiVisibility(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR :
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//                     | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                getWindow().setNavigationBarColor(Color.TRANSPARENT);
            }

            getWindow().setStatusBarColor(statusBarColor);

            StatusBarUtil.StatusBarLightMode(this);


        }

    }

//    public void setWindowsStatusBar(int statusBarColor) {
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            if (getWindow().getStatusBarColor() == Color.TRANSPARENT) {
//                if (statusBarColor != Color.TRANSPARENT) {
//
//                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
//                    getWindow().setStatusBarColor(statusBarColor);
//                    getWindow().getDecorView().invalidate();
//
//                }
//            } else {
//                if (statusBarColor == Color.TRANSPARENT)
//                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//                getWindow().setStatusBarColor(statusBarColor);
//                getWindow().getDecorView().invalidate();
//
//            }
//        }
//
//    }

    protected void initView(){

    }

    protected void initData(){

    }


    protected int setStatusBarColor() {
        return Color.WHITE;
    }


}
