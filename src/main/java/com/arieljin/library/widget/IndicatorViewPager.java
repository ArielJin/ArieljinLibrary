package com.arieljin.library.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.arieljin.library.utils.DipUtil;

/**
 * @time 2018/7/30.
 * @email ariel.jin@tom.com
 */
public class IndicatorViewPager extends ConstraintLayout {

    private ViewPager viewPager;
    private RadioGroup indicatorGroup;


    public IndicatorViewPager(Context context) {
        super(context);
        init();
    }

    public IndicatorViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IndicatorViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        viewPager = new ViewPager(getContext());
        LayoutParams viewPagerLP = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        viewPager.setLayoutParams(viewPagerLP);
        viewPager.setBackgroundColor(Color.GREEN);


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

    public void setAdapter(PagerAdapter adapter) {

        if (adapter != null && adapter.getCount() > 0) {

            if (adapter.getCount() == 1) {
                indicatorGroup.removeAllViews();
                indicatorGroup.setVisibility(GONE);
            } else {

                for (int i = 0; i < adapter.getCount(); i++) {
                    IndicatorView indicatorView = new IndicatorView(getContext());
                    if (i == 0)
                        indicatorView.setChecked(true);
                    indicatorGroup.addView(indicatorView);
                }
                indicatorGroup.setVisibility(VISIBLE);

            }

            viewPager.setAdapter(adapter);

            setVisibility(VISIBLE);

        } else {

            viewPager.setAdapter(null);
            indicatorGroup.removeAllViews();
            setVisibility(GONE);

        }


    }


//    public void setList(List list) {
//
//        if (viewPager.getAdapter() == null)
//            throw new NullPointerException("adapter not null!");
//
//
//    }


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
            RadioGroup.LayoutParams layoutParams = (RadioGroup.LayoutParams) getLayoutParams();
            if (layoutParams != null)
                layoutParams.width = DipUtil.dip2px(getContext(), checked ? 8 : 4);
        }
    }
}
