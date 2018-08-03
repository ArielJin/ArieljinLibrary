package com.arieljin.library.utils;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * @time 2018/7/25.
 * @email ariel.jin@tom.com
 */
public final class ViewUtil {

    public static void measureViewHeight(View view) {
        ViewGroup.LayoutParams p = view.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = View.MeasureSpec.makeMeasureSpec(lpHeight, View.MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        }
        view.measure(childWidthSpec, childHeightSpec);
    }

    public static void viewShow(View view) {
        if (view != null && (view.getVisibility() == View.GONE || view.getVisibility() == View.INVISIBLE)) {
            view.setVisibility(View.VISIBLE);
        }
    }

    public static void viewHide(View view) {
        if (view != null && view.getVisibility() == View.VISIBLE) {
            view.setVisibility(View.GONE);
        }
    }

    public static void viewHideInVisible(View view) {
        if (view != null && view.getVisibility() == View.VISIBLE) {
            view.setVisibility(View.INVISIBLE);
        }
    }

    public static Bitmap getTransparentBitmap(Bitmap sourceImg, int number) {

        int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];

        sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0, sourceImg

                .getWidth(), sourceImg.getHeight());// 获得图片的ARGB值

        number = number * 255 / 100;

        for (int i = 0; i < argb.length; i++) {


            if (argb[i] != 0)
                argb[i] = (number << 24) | (argb[i] & 0x00FFFFFF);

        }

        sourceImg = Bitmap.createBitmap(argb, sourceImg.getWidth(), sourceImg

                .getHeight(), Bitmap.Config.ARGB_8888);

        return sourceImg;
    }

    public static Drawable getPressedDrawable(Context context, int resId, int number) {

        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{-android.R.attr.state_pressed}, context.getResources().getDrawable(resId));
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, new BitmapDrawable(getTransparentBitmap(BitmapFactory.decodeResource(context.getResources(), resId), number)));

        return stateListDrawable;

    }

    public static Drawable getPressedDrawable(Context context, Bitmap bitmap, int number) {

        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{-android.R.attr.state_pressed}, new BitmapDrawable(context.getResources(), bitmap));
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, new BitmapDrawable(context.getResources(), ViewUtil.getTransparentBitmap(bitmap, number)));

        return stateListDrawable;

    }

    public static Drawable getWhiteLeftBackDrawable(Context context, int number) {
        return getPressedDrawable(context, context.getResources().getIdentifier("ic_arrow_left_white_kaqu", "drawable", context.getPackageName()), number);
    }

    public static Drawable getBlackLeftBackDrawable(Context context, int number) {
        return getPressedDrawable(context, context.getResources().getIdentifier("ic_arrow_left_black_kaqu", "drawable", context.getPackageName()), number);
    }

    public static Drawable getBtnBackgroundDrawable(int unPressedColor, int pressedColor, int cornerRadius) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(new ColorStateList(new int[][]{{-android.R.attr.state_pressed}, {android.R.attr.state_pressed}}, new int[]{unPressedColor, pressedColor}));
            drawable.setCornerRadius(cornerRadius);
            return drawable;

        } else {
            GradientDrawable unPDrawable = new GradientDrawable();
            unPDrawable.setColor(unPressedColor);
            unPDrawable.setCornerRadius(cornerRadius);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(pressedColor);
            drawable.setCornerRadius(cornerRadius);

            StateListDrawable listDrawable = new StateListDrawable();
            listDrawable.addState(new int[]{-android.R.attr.state_pressed}, unPDrawable);
            listDrawable.addState(new int[]{android.R.attr.state_pressed}, drawable);
            return listDrawable;

        }


    }

    public static int makeViewHeight(View view) {
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
        return view.getMeasuredHeight();
    }

    public static int makeViewWidth(View view) {
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
        return view.getMeasuredWidth();
    }

    public static void setLayoutTransition(LinearLayout linearLayout) {
        if (linearLayout == null)
            return;
        if (linearLayout.getContext() == null)
            return;
        LayoutTransition transition = new LayoutTransition();
//        当一个View在ViewGroup中出现时，对此View对其他View位置造成影响，对其他View设置的动画
        transition.setAnimator(LayoutTransition.CHANGE_APPEARING,
                transition.getAnimator(LayoutTransition.CHANGE_APPEARING));
//        当一个View在ViewGroup中出现时，对此View设置的动画
        transition.setAnimator(LayoutTransition.APPEARING,
                ObjectAnimator.ofFloat(linearLayout.getContext(), "scaleY", 0, 1));
//        当一个View在ViewGroup中消失时，对此View设置的动画
        transition.setAnimator(LayoutTransition.DISAPPEARING,
                ObjectAnimator.ofFloat(linearLayout.getContext(), "scaleY", 1, 0));
//        当一个View在ViewGroup中消失时，对此View对其他View位置造成影响，对其他View设置的动画
        transition.setAnimator(LayoutTransition.CHANGE_DISAPPEARING,
                transition.getAnimator(LayoutTransition.CHANGE_DISAPPEARING));
        linearLayout.setLayoutTransition(transition);

    }

    public static void setImageResAnimator(final ImageView imageView, final Bitmap bitmap, final int number) {

        ObjectAnimator animator;
        animator = ObjectAnimator.ofFloat(imageView, "alpha", 1, 0);
        animator.setDuration(200);
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                imageView.setImageDrawable(ViewUtil.getPressedDrawable(imageView.getContext(), bitmap, number));
                ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "alpha", 0, 1);
                animator.setDuration(200);
                animator.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
        animator.start();
    }

    private static int getStatusBarHeight(Context context) {
        int statusBarHeight = -1;

        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    public static void setViewToStatusBar(View statusBarView, int statusBarcolor) {
        statusBarView.setBackgroundColor(statusBarcolor);
        int statusBarHeight = ViewUtil.getStatusBarHeight(statusBarView.getContext());
        if (statusBarHeight != -1){

            ViewGroup.LayoutParams statusBarViewLp = statusBarView.getLayoutParams();
            if (statusBarViewLp != null) {
                statusBarViewLp.height = statusBarHeight;
                statusBarView.setLayoutParams(statusBarViewLp);
            }

        }

    }
}
