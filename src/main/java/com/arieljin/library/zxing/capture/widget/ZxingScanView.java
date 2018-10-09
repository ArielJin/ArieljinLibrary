package com.arieljin.library.zxing.capture.widget;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.SurfaceView;

import com.arieljin.library.R;

/**
 * @time 2018/10/8.
 * @email ariel.jin@tom.com
 */
public class ZxingScanView extends ConstraintLayout {


    public ZxingScanView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZxingScanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);

    }

    private void init(AttributeSet attrs) {

        SurfaceView surfaceView = new SurfaceView(getContext());
        surfaceView.setId(R.id.zxing_preview_view);
        surfaceView.setLayoutParams(new LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));

        ViewfinderView viewfinderView = new ViewfinderView(getContext(), attrs);
        viewfinderView.setId(R.id.zxing_viewfinder);
        viewfinderView.setLayoutParams(new LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));

        addView(surfaceView);
        addView(viewfinderView);

    }

    public SurfaceView getZxingPreviewView(){
        return findViewById(R.id.zxing_preview_view);
    }

    public ViewfinderView getZxingViewfinder() {
        return findViewById(R.id.zxing_viewfinder);
    }



}
