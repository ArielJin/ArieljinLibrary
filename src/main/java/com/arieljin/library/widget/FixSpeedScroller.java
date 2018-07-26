package com.arieljin.library.widget;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * @time 2018/7/24.
 * @email ariel.jin@tom.com
 */
public class FixSpeedScroller extends Scroller {

    public int mDuration = 400;

    public FixSpeedScroller(Context context) {
        super(context);
    }

    public FixSpeedScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, mDuration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        super.startScroll(startX, startY, dx, dy, mDuration);
    }
}
