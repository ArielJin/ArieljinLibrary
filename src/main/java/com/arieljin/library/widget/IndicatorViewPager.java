package com.arieljin.library.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.arieljin.library.R;
import com.arieljin.library.utils.DipUtil;
import com.arieljin.library.widget.adapter.BasePagerAdapter;

import java.util.List;

/**
 * @time 2018/7/30.
 * @email ariel.jin@tom.com
 */
public class IndicatorViewPager extends ConstraintLayout implements ViewPager.OnPageChangeListener {

    private ViewPager viewPager;
    private RadioGroup indicatorGroup;
    private boolean isLoop = false;

    private boolean autoPlay = false;

    private int time = 0;

    private @DrawableRes
    int bg_placeholder = 0;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            play();

        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (isLoop)
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
            else
                viewPager.setCurrentItem(viewPager.getCurrentItem() == viewPager.getAdapter().getCount() - 1 ? 0 : viewPager.getCurrentItem() + 1, true);

        }
    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        play();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancel();
    }

    public void play() {

        if (viewPager.getAdapter() == null)
            return;
        if (autoPlay) {
            handler.postDelayed(runnable, time);
        } else {
            handler.removeCallbacks(runnable);
        }


    }

    public void cancel() {
        handler.removeCallbacks(runnable);
    }

    public void setAutoPlay(int seconds) {
        this.time = seconds * 1000;
        this.autoPlay = this.time > 0;
        if (!autoPlay) {
            handler.removeCallbacks(runnable);
        }
    }

    public IndicatorViewPager(Context context) {
        this(context, null);
    }

    public IndicatorViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndicatorViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Ariel_IndicatorViewPager, defStyleAttr, 0);
        if (a.hasValue(R.styleable.Ariel_IndicatorViewPager_is_loop))
            isLoop = a.getBoolean(R.styleable.Ariel_IndicatorViewPager_is_loop, false);
        if (a.hasValue(R.styleable.Ariel_IndicatorViewPager_play_time_seconds)) {
            time = a.getInteger(R.styleable.Ariel_IndicatorViewPager_play_time_seconds, 0) * 1000;
            autoPlay = time > 0;
        }
        if (a.hasValue(R.styleable.Ariel_IndicatorViewPager_bg_placeholder)) {
            bg_placeholder = a.getResourceId(R.styleable.Ariel_IndicatorViewPager_bg_placeholder, bg_placeholder);
        }
        init();
    }

    private void init() {

        viewPager = isLoop ? new LoopViewPager(getContext()) : new ViewPager(getContext());
        LayoutParams viewPagerLP = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        viewPager.setLayoutParams(viewPagerLP);
        viewPager.setOnPageChangeListener(this);


        indicatorGroup = new RadioGroup(getContext());
        indicatorGroup.setOrientation(LinearLayout.HORIZONTAL);
        LayoutParams indicatorGroupLP = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        indicatorGroupLP.bottomToBottom = LayoutParams.PARENT_ID;
        indicatorGroupLP.startToStart = LayoutParams.PARENT_ID;
        indicatorGroupLP.endToEnd = LayoutParams.PARENT_ID;
        indicatorGroupLP.bottomMargin = DipUtil.dip2px(getContext(), 12);
        indicatorGroup.setLayoutParams(indicatorGroupLP);
//        indicatorGroup.setBackgroundColor(Color.GREEN);

        addView(viewPager);
        addView(indicatorGroup);
    }

    public void setAdapter(@NonNull BasePagerAdapter adapter) {

        if (isLoop)
            ((LoopViewPager) viewPager).setBoundaryLooping(adapter.getCount() > 1);
        viewPager.setAdapter(adapter);
        invalidateViews(adapter.getCount());
        if (adapter.getCount() > 1)
            play();

    }


    public void setList(List list) {
        cancel();
        if (viewPager.getAdapter() != null) {
            PagerAdapter adapter = viewPager.getAdapter();
            if (adapter instanceof BasePagerAdapter) {
                if (isLoop) {
                    ((LoopViewPager) viewPager).setBoundaryLooping(list != null && list.size() > 1);
                    ((LoopViewPager) viewPager).setList(list);
                } else {
                    ((BasePagerAdapter) adapter).setList(list);
                }
            }

        }

        invalidateViews(list != null && !list.isEmpty() ? list.size() : 0);
        if (list != null && list.size() > 1) {
            play();
        }
    }

    private void invalidateViews(int itemCount) {

        if (itemCount > 0) {

            indicatorGroup.removeAllViews();
            if (itemCount == 1) {
                indicatorGroup.setVisibility(GONE);
            } else {

                for (int i = 0; i < itemCount; i++) {
                    IndicatorView indicatorView = new IndicatorView(getContext());
                    indicatorGroup.addView(indicatorView);
                }
                indicatorGroup.setVisibility(VISIBLE);

                ((IndicatorView) indicatorGroup.getChildAt(0)).setChecked(true);

            }


        } else {

            indicatorGroup.removeAllViews();
            indicatorGroup.setVisibility(GONE);
            if (bg_placeholder != 0)
                viewPager.setBackgroundResource(bg_placeholder);

        }

    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (indicatorGroup != null && indicatorGroup.getChildCount() > 1)
            ((IndicatorView) indicatorGroup.getChildAt(position)).setChecked(true);

    }

    @Override
    public void onPageScrollStateChanged(int state) {

        if (state == ViewPager.SCROLL_STATE_IDLE) {
            play();
        } else if (state == ViewPager.SCROLL_STATE_DRAGGING) {
            cancel();
        }

    }


    @SuppressLint("AppCompatCustomView")
    class IndicatorView extends RadioButton {

        public IndicatorView(Context context) {
            super(context);
            init();
        }

        public IndicatorView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public IndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        private void init() {

            setButtonDrawable(null);
            setButtonDrawable(0);


            int cornerRadius = DipUtil.dip2px(getContext(), 2);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                GradientDrawable drawable = new GradientDrawable();
                drawable.setColor(new ColorStateList(new int[][]{{-android.R.attr.state_checked}, {android.R.attr.state_checked}}, new int[]{Color.WHITE, Color.BLACK}));
                drawable.setCornerRadius(cornerRadius);
                setBackground(drawable);

            } else {
                GradientDrawable unPDrawable = new GradientDrawable();
                unPDrawable.setColor(Color.WHITE);
                unPDrawable.setCornerRadius(cornerRadius);


                GradientDrawable drawable = new GradientDrawable();
                drawable.setColor(Color.BLACK);
                drawable.setCornerRadius(cornerRadius);

                StateListDrawable listDrawable = new StateListDrawable();
                listDrawable.addState(new int[]{-android.R.attr.state_checked}, unPDrawable);
                listDrawable.addState(new int[]{android.R.attr.state_checked}, drawable);

                setBackgroundDrawable(listDrawable);

            }

            RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(DipUtil.dip2px(getContext(), 4), DipUtil.dip2px(getContext(), 2));
            layoutParams.setMargins(DipUtil.dip2px(getContext(), 2), 0, DipUtil.dip2px(getContext(), 2), 0);
            setLayoutParams(layoutParams);


        }

        @Override
        public void setChecked(boolean checked) {
            super.setChecked(checked);
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            if (layoutParams != null && layoutParams instanceof RadioGroup.LayoutParams) {

                layoutParams.width = DipUtil.dip2px(getContext(), checked ? 8 : 4);
                setLayoutParams(layoutParams);


            }

        }
    }

}
