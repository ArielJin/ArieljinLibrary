package com.arieljin.library.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import static android.support.v4.view.ViewPager.SCROLL_STATE_DRAGGING;
import static android.support.v4.view.ViewPager.SCROLL_STATE_IDLE;
import static android.support.v4.view.ViewPager.SCROLL_STATE_SETTLING;

/**
 * @time 2018/8/10.
 * @email ariel.jin@tom.com
 */
public class ColorSizeTrackTabLayout extends TabLayout {

    private int mTabTextSize;
    private int mTabSelectedTextColor;
    private int mTabTextColor;
    private static final int INVALID_TAB_POS = -1;
    /*
     * 最后选中的postition
     */
    private int mLastSelectedTabPosition = INVALID_TAB_POS;

    private ColorSizeTrackTabLayoutOnPageChangeListener mPageChangeListenter;
    private ViewPager mViewPager;


    public ColorSizeTrackTabLayout(Context context) {
        this(context, null);
    }

    public ColorSizeTrackTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorSizeTrackTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, android.support.design.R.styleable.TabLayout,
                    defStyleAttr, android.support.design.R.style.Widget_Design_TabLayout);
            try {
                int tabTextAppearance = a.getResourceId(android.support.design.R.styleable.TabLayout_tabTextAppearance,
                        android.support.design.R.style.TextAppearance_Design_Tab);

                // Text colors/sizes come from the text appearance first
                final TypedArray ta = context.obtainStyledAttributes(tabTextAppearance,
                        android.support.v7.appcompat.R.styleable.TextAppearance);
                try {
                    //Tab字体大小
                    mTabTextSize = ta.getDimensionPixelSize(
                            android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize, 0);
                    //Tab文字颜色
                    mTabTextColor = ta.getColor(
                            android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor, 0);
                } finally {
                    ta.recycle();
                }

                //Tab文字选中颜色
                mTabSelectedTextColor = a.getColor(android.support.design.R.styleable.TabLayout_tabSelectedTextColor, Color.BLACK);

            } finally {
                a.recycle();
            }
        }
    }

    @Override
    public void addTab(@NonNull Tab tab, int position, boolean setSelected) {
        ColorSizeTrackView colorTrackView = new ColorSizeTrackView(getContext());
//        colorTrackView.setProgress(setSelected ? 1 : 0);
        colorTrackView.setText(tab.getText() + "");
        colorTrackView.setTextSize(mTabTextSize);
        colorTrackView.setProgress(setSelected ? 1 : 0);
        colorTrackView.setTag(position);
        colorTrackView.setTextChangeColor(mTabSelectedTextColor);
        colorTrackView.setTextOriginColor(mTabTextColor);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        colorTrackView.setLayoutParams(layoutParams);
//        colorTrackView.setPadding(getResources().getDimensionPixelSize(R.dimen.ariel_dp_8),0,0,0);
        tab.setCustomView(colorTrackView);

        super.addTab(tab, position, setSelected);
        int selectedTabPosition = getSelectedTabPosition();
        if ((selectedTabPosition == INVALID_TAB_POS && position == 0) || (selectedTabPosition == position)) {
            setSelectedView(position);
        }


        setTabWidth(position, colorTrackView);
    }

    private void setTabWidth(int position, ColorSizeTrackView colorTrackView) {
        ViewGroup slidingTabStrip = (ViewGroup) getChildAt(0);
        ViewGroup tabView = (ViewGroup) slidingTabStrip.getChildAt(position);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;

        int w = MeasureSpec.makeMeasureSpec(0,
                MeasureSpec.UNSPECIFIED);
        int h = MeasureSpec.makeMeasureSpec(0,
                MeasureSpec.UNSPECIFIED);
        //手动测量一下
        colorTrackView.measure(w, h);
        params.width = colorTrackView.getMeasuredWidth() + tabView.getPaddingLeft() + tabView.getPaddingRight();
        //设置tabView的宽度
        tabView.setLayoutParams(params);
    }

    /**
     * 设置每个Tab的左内边距和右内边距
     *
     * @param left
     * @param right
     */
    public void setTabPaddingLeftAndRight(int left, int right) {
        try {
            Field mTabPaddingStartField = TabLayout.class.getDeclaredField("mTabPaddingStart");
            Field mTabPaddingEndField = TabLayout.class.getDeclaredField("mTabPaddingEnd");

            mTabPaddingStartField.setAccessible(true);
            mTabPaddingEndField.setAccessible(true);

            mTabPaddingStartField.set(this, left);
            mTabPaddingEndField.set(this, right);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void setupWithViewPager(@Nullable ViewPager viewPager, boolean autoRefresh) {
        super.setupWithViewPager(viewPager, autoRefresh);
        try {
            if (viewPager != null)
                mViewPager = viewPager;
            //通过反射找到mPageChangeListener
            Field field = TabLayout.class.getDeclaredField("mPageChangeListener");
            field.setAccessible(true);
            TabLayoutOnPageChangeListener listener = (TabLayoutOnPageChangeListener) field.get(this);
            if (listener != null) {
                //删除自带监听
                viewPager.removeOnPageChangeListener(listener);
                mPageChangeListenter = new ColorSizeTrackTabLayoutOnPageChangeListener(this);
                mPageChangeListenter.reset();
                viewPager.addOnPageChangeListener(mPageChangeListenter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void tabScrolled(int position, float positionOffset) {

        if (positionOffset == 0.0F) {
            return;
        }
        ColorSizeTrackView currentTrackView = getColorSizeTrackView(position);
        ColorSizeTrackView nextTrackView = getColorSizeTrackView(position + 1);
        currentTrackView.setDirection(1);
        currentTrackView.setProgress(1.0F - positionOffset);
//        nextTrackView.setTextSizeAndProgress((int) ((1.0F - positionOffset) * getResources().getDimensionPixelSize(R.dimen.cardview_default_radius) *2 + mTabTextSize),1.0F - positionOffset);
        nextTrackView.setDirection(0);
//        currentTrackView.setTextSizeAndProgress((int) (positionOffset * getResources().getDimensionPixelSize(R.dimen.cardview_default_radius) *2 + mTabTextSize),1.0F - positionOffset);
        nextTrackView.setProgress(positionOffset);
    }

    public ColorSizeTrackView getColorSizeTrackView(int position) {
        return (ColorSizeTrackView) getTabAt(position).getCustomView();
    }

    public static class ColorSizeTrackTabLayoutOnPageChangeListener extends TabLayoutOnPageChangeListener {

        private final WeakReference<ColorSizeTrackTabLayout> mTabLayoutRef;
        private int mPreviousScrollState;
        private int mScrollState;

        public ColorSizeTrackTabLayoutOnPageChangeListener(TabLayout tabLayout) {
            super(tabLayout);
            mTabLayoutRef = new WeakReference<>((ColorSizeTrackTabLayout) tabLayout);
        }

        @Override
        public void onPageScrollStateChanged(final int state) {
            mPreviousScrollState = mScrollState;
            mScrollState = state;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            ColorSizeTrackTabLayout tabLayout = mTabLayoutRef.get();
            if (tabLayout == null) return;
            final boolean updateText = mScrollState != SCROLL_STATE_SETTLING ||
                    mPreviousScrollState == SCROLL_STATE_DRAGGING;
            if (updateText) {
                tabLayout.tabScrolled(position, positionOffset);
            }
        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            ColorSizeTrackTabLayout tabLayout = mTabLayoutRef.get();
            mPreviousScrollState = SCROLL_STATE_SETTLING;
            tabLayout.setSelectedView(position);
        }

        void reset() {
            mPreviousScrollState = mScrollState = SCROLL_STATE_IDLE;
        }

    }


    protected void setSelectedView(int position) {
        final int tabCount = getTabCount();
        if (position < tabCount) {
            for (int i = 0; i < tabCount; i++) {
                getColorSizeTrackView(i).setProgress(i == position ? 1 : 0);
            }
        }
    }

    @Override
    public void removeAllTabs() {
        // Retain last selected position before removing all tabs
        mLastSelectedTabPosition = getSelectedTabPosition();
        super.removeAllTabs();
    }

    @Override
    public int getSelectedTabPosition() {
        // Override selected tab position to return your last selected tab position
        final int selectedTabPositionAtParent = super.getSelectedTabPosition();
        return selectedTabPositionAtParent == INVALID_TAB_POS ?
                mLastSelectedTabPosition : selectedTabPositionAtParent;
    }

    public void setLastSelectedTabPosition(int lastSelectedTabPosition) {
        mLastSelectedTabPosition = lastSelectedTabPosition;
    }

    public void setCurrentItem(int position) {
        if (mViewPager != null)
            mViewPager.setCurrentItem(position);
    }


//    public static void reflexIndicatorWidth(final TabLayout tabLayout, final int left, final int right) {
//        //了解源码得知 线的宽度是根据 tabView的宽度来设置的
//        tabLayout.post(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    //拿到tabLayout的mTabStrip属性
//                    LinearLayout mTabStrip = (LinearLayout) tabLayout.getChildAt(0);
//
////                    int dp10 = dip2px(tabLayout.getContext(), 10);
//
//                    for (int i = 0; i < mTabStrip.getChildCount(); i++) {
//                        View tabView = mTabStrip.getChildAt(i);
//
//                        //拿到tabView的mTextView属性  tab的字数不固定一定用反射取mTextView
//                        Field mTextViewField = tabView.getClass().getDeclaredField("mTextView");
//                        mTextViewField.setAccessible(true);
//
//                        TextView mTextView = (TextView) mTextViewField.get(tabView);
//
//                        tabView.setPadding(0, 0, 0, 0);
//
//                        //因为我想要的效果是   字多宽线就多宽，所以测量mTextView的宽度
//                        int width = 0;
//                        width = mTextView.getWidth();
//                        if (width == 0) {
//                            mTextView.measure(0, 0);
//                            width = mTextView.getMeasuredWidth();
//                        }
//
//                        //设置tab左右间距为10dp  注意这里不能使用Padding 因为源码中线的宽度是根据 tabView的宽度来设置的
//                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tabView.getLayoutParams();
//                        params.width = width;
//                        params.leftMargin = left;
//                        params.rightMargin = right;
//                        tabView.setLayoutParams(params);
//
//                        tabView.invalidate();
//                    }
//
//                } catch (NoSuchFieldException e) {
//                    e.printStackTrace();
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//    }

}
