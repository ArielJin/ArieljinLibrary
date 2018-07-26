package com.arieljin.library.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.EdgeEffectCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;

import com.arieljin.library.utils.DipUtil;
import com.arieljin.library.widget.adapter.AbsPagerAdapter;

import java.lang.reflect.Field;

/**
 * @time 2018/7/24.
 * @email ariel.jin@tom.com
 */
public final class OverScrollViewPager extends ViewPager implements GestureDetector.OnGestureListener, ViewTreeObserver.OnPreDrawListener {


    private GestureDetector gestureDetector;
    public FixSpeedScroller mScroller;

    private static final short SPEED = 3;

    private float scrollX, item_width;
    private int width, currentViewLeft;

    private Integer maxLeft, maxRight;

    private OnPagerItemClickListener onItemClickListener;
    private OnPagerOverScrollListener onPagerOverScrollListener;

    private EdgeEffectCompat leftEdge, rightEdge;

    private OnPageChangeListener onPageChangeListener;
    private PageTransformer pageTransformer;

    private int scroll_state, last_position;

    public OverScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OverScrollViewPager(Context context) {
        super(context);
        init();
    }

    private void init() {
        width = getResources().getDisplayMetrics().widthPixels;
        gestureDetector = new GestureDetector(getContext(), this);

        getViewTreeObserver().addOnPreDrawListener(this);

        super.setOnPageChangeListener(interalOnPageChangeListener);

        initScroller();
        try {
            Field leftEdgeField = ViewPager.class.getDeclaredField("mLeftEdge");
            Field rightEdgeField = ViewPager.class.getDeclaredField("mRightEdge");
            if (leftEdgeField != null && rightEdgeField != null) {
                leftEdgeField.setAccessible(true);
                rightEdgeField.setAccessible(true);
                leftEdge = (EdgeEffectCompat) leftEdgeField.get(this);
                rightEdge = (EdgeEffectCompat) rightEdgeField.get(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initScroller() {
        if (mScroller == null) {
            mScroller = new FixSpeedScroller(getContext());
            try {
                Field field = ViewPager.class.getDeclaredField("mScroller");
                field.setAccessible(true);
                field.set(this, mScroller);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private DataSetObserver observer = new DataSetObserver() {

        @Override
        public void onChanged() {
            maxLeft = maxRight = null;
            item_width = (DipUtil.getScreenWidth() - getPaddingLeft() - getPaddingRight()) * getAdapter().getPageWidth(0);
        }
    };

    @Override
    public void setAdapter(PagerAdapter adapter) {
        super.setAdapter(adapter);
        if (adapter != null && adapter instanceof AbsPagerAdapter) {
            observer.onChanged();
            ((AbsPagerAdapter) adapter).registerObserver(observer);
        }
    }

    private boolean isAtLeft() {
        return !canScrollHorizontally(-1);
    }

    private boolean isAtRight() {
        return !canScrollHorizontally(1);
    }

    private int getMaxLeft() {
        if (maxLeft == null) {
            if (isAtLeft()) {
                maxLeft = 0;
                for (int i = 0, size = getChildCount(); i < size; i++) {
                    int left = getChildAt(i).getLeft();
                    if (left < maxLeft) {
                        maxLeft = left;
                    }
                }
            }
        }
        return maxLeft;
    }

    private int getMaxRight() {
        if (maxRight == null) {
            if (isAtRight()) {
                maxRight = 0;
                for (int i = 0, size = getChildCount(); i < size; i++) {
                    int right = getChildAt(i).getLeft();
                    if (right > maxRight) {
                        maxRight = right;
                    }
                }
            }
        }
        return maxRight;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }

        if (!mScroller.isFinished() && ((isAtLeft() && scrollX < getMaxLeft()) || (isAtRight() && scrollX > getMaxRight()))) {
            return true;
        }

        boolean b = super.onInterceptTouchEvent(event);

        if (!b) {
            gestureDetector.onTouchEvent(event);
        }

        return b;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            scrollTo((int) scrollX, 0);

            int positionOffsetPixels = getScrollX() - currentViewLeft;

            int page = getCurrentItem();
            float positionOffset = positionOffsetPixels / item_width;

            if (positionOffsetPixels < 0) {
                page--;
                positionOffset += 1;
            }

            onPageScrolled(page, positionOffset, positionOffsetPixels);

            if (scroll_state != SCROLL_STATE_DRAGGING) {
                interalOnPageChangeListener.onPageScrollStateChanged(SCROLL_STATE_DRAGGING);
            }
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (!onUp()) {
                    try {
                        super.onTouchEvent(event);
                    } catch (Exception e) {
                    }
                }
                break;
        }

        return true;
    }

    public boolean onUp() {
        scrollX = getScrollX();

        if (isAtLeft()) {
            int left = getMaxLeft();

            if (getScrollX() < left) {
                mScroller.startScroll((int) scrollX, 0, left - (int) scrollX, 0);
                invalidate();

                if (onPagerOverScrollListener != null && getScrollX() < 100) {
                    onPagerOverScrollListener.onLeft();
                }
                return true;
            }
        } else if (isAtRight()) {
            int right = getMaxRight();

            if (getScrollX() > right) {
                mScroller.startScroll((int) scrollX, 0, (int) -scrollX + right, 0);
                invalidate();

                if (onPagerOverScrollListener != null && getScrollX() - right > 100) {
                    onPagerOverScrollListener.onRight();
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (!mScroller.isFinished() && mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            scrollX = mScroller.getCurrX();
            postInvalidate();
        } else if (scrollX == mScroller.getFinalX() && scroll_state != SCROLL_STATE_SETTLING) {
            interalOnPageChangeListener.onPageScrollStateChanged(SCROLL_STATE_SETTLING);
        }
    }

    public boolean isFling() {
        int offset = getScrollX() % width;
        return offset > 3 && offset < width - 3;
    }

    public void setOnPagerItemClickListener(OnPagerItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnPagerOverScrollListener(OnPagerOverScrollListener onPagerOverScrollListener) {
        this.onPagerOverScrollListener = onPagerOverScrollListener;
    }

    public interface OnPagerOverScrollListener {
        void onRight();

        void onLeft();
    }

    public void setDuration(int duration) {
        mScroller.mDuration = duration;
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item);
        currentViewLeft = getScrollX();
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, smoothScroll);
        if (!smoothScroll) {
            currentViewLeft = getScrollX();
        }
    }

    @Override
    public boolean onPreDraw() {
        getViewTreeObserver().removeOnPreDrawListener(this);
        currentViewLeft = getScrollX();
        return false;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (scroll_state == SCROLL_STATE_SETTLING && getChildCount() > 0 && onItemClickListener != null) {
            onItemClickListener.onItemClick(this, getCurrentItem());
        }
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (getChildCount() == 0) {
            return false;
        }

        boolean result = false;

        if (isAtLeft()) {
            int left = getMaxLeft();

            if (scrollX < left || (scrollX == left && distanceX < 0)) {
                scrollX += distanceX / SPEED;

                if (scrollX > left) {
                    scrollX = left;
                }

                result = true;
            }
        } else if (isAtRight()) {
            int right = getMaxRight();

            if (scrollX > right || (scrollX == right && distanceX > 0)) {
                scrollX += distanceX / SPEED;

                if (scrollX < right) {
                    scrollX = right;
                }

                result = true;
            }
        }

        if (!result) {
            scrollX += distanceX;
        }

        if (getCurrentItem() > 0 && scrollX < currentViewLeft - item_width) {
            setCurrentItem(getCurrentItem() - 1, false);
            scrollX = getScrollX();
            return true;
        } else if (getAdapter() != null && getCurrentItem() < getAdapter().getCount() - 1 && scrollX > currentViewLeft + item_width) {
            setCurrentItem(getCurrentItem() + 1, false);
            scrollX = getScrollX();
            return true;
        }

        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if (pageTransformer != null) {
            super.setPageTransformer(true, pageTransformer);
            pageTransformer = null;
        }

        mScroller.abortAnimation();
        scrollX = getScrollX();
        return false;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        super.onPageScrolled(position, positionOffset, positionOffsetPixels);
        if (leftEdge != null && !leftEdge.isFinished()) {
            leftEdge.finish();
            leftEdge.setSize(0, 0);
        }

        if (rightEdge != null && !rightEdge.isFinished()) {
            rightEdge.finish();
            rightEdge.setSize(0, 0);
        }
    }

    private OnPageChangeListener interalOnPageChangeListener = new OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (onPageChangeListener != null) {
                onPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            scroll_state = state;
            if (onPageChangeListener != null) {
                onPageChangeListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if (onPageChangeListener != null) {
                onPageChangeListener.onPageSelected(position);
            }

            positionChanged(position);
        }
    };

    public void setPageTransformer(boolean arg0, PageTransformer pageTransformer) {
        this.pageTransformer = pageTransformer;
    }

    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.onPageChangeListener = listener;
    }

    public void positionChanged(int position) {
        if (position != last_position) {
            currentViewLeft += ((position - last_position) * item_width);
            last_position = position;
        }
    }

    public interface OnPagerItemClickListener {
        void onItemClick(ViewPager parent, int position);
    }


}
