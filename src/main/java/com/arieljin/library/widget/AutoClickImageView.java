package com.arieljin.library.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @time 2018/10/23.
 * @email ariel.jin@tom.com
 */
@SuppressLint("AppCompatCustomView")
public class AutoClickImageView extends ImageView {

    public AutoClickImageView(Context context) {
        super(context);
        init();

    }

    public AutoClickImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoClickImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
//        setOnTouchListener(onTouchListener);
        setClickable(true);


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isPressed())
            canvas.drawColor(0x33000000);
    }

    @Override
    protected void dispatchSetPressed(boolean pressed) {
        super.dispatchSetPressed(pressed);
        invalidate();
    }

    //    private OnTouchListener onTouchListener=new OnTouchListener() {
//        @Override
//        public boolean onTouch(View v, MotionEvent event) {
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_UP:
//                    setColorFilter(null);
//                    break;
//                case MotionEvent.ACTION_DOWN:
//                    changeLight();
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    break;
//                case MotionEvent.ACTION_CANCEL:
//                    setColorFilter(null);
//                    break;
//                default:
//                    break;
//            }
//            return AutoClickImageView.super.onTouchEvent(event);
//        }
//    };
//    private void changeLight() {
////        int brightness=-80;
//        ColorMatrix matrix = new ColorMatrix();
////        matrix.set(new float[] {
////                1, 0, 0, 0, brightness,
////                0, 1, 0, 0, brightness,
////                0, 0, 1, 0, brightness,
////                0, 0, 0, 1, 0 });
//        matrix.set(new float[] {
//                0.33F, 0.59F, 0.11F, 0, 0,
//                0.33F, 0.59F, 0.11F, 0, 0,
//                0.33F, 0.59F, 0.11F, 0, 0,
//                0, 0, 0, 1, 0 });
//        setColorFilter(new ColorMatrixColorFilter(matrix));
//
//    }


}
