package com.arieljin.library.widget.recyclerview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arieljin.library.utils.DipUtil;

public class LoadMoreRecyclerView extends AdRecyclerView {

    private LoadMoreView mLoadMoreView;
    private LoadMoreListener mLoadMoreListener;

    private int mScrollState = -1;

    private boolean isLoadError = false;

    private boolean isAutoLoadMore = true;

    private boolean isLoadMore = false;

    private boolean mDataEmpty = true;

    private boolean mHasMore = false;


    public LoadMoreRecyclerView(Context context) {
        super(context);
    }

    public LoadMoreRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadMoreRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void useDefaultLoadMore() {
        DefaultLoadMoreView defaultLoadMoreView = new DefaultLoadMoreView(getContext());
        addFooterView(defaultLoadMoreView);
        setLoadMoreView(defaultLoadMoreView);
    }

    public void setLoadMoreView(LoadMoreView loadMoreView) {
        mLoadMoreView = loadMoreView;
    }

    public void setLoadMoreListener(LoadMoreListener loadMoreListener) {
        mLoadMoreListener = loadMoreListener;
    }

    @Override
    public void onScrollStateChanged(int state) {
        this.mScrollState = state;
    }

    @Override
    public void onScrolled(int dx, int dy) {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager != null && layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;

            int itemCount = layoutManager.getItemCount();
            int lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();

            if (itemCount > 0 && itemCount == lastVisiblePosition + 1 &&
                    (mScrollState == SCROLL_STATE_DRAGGING || mScrollState == SCROLL_STATE_SETTLING)) {
                dispatchLoadMore();
            }
        }
    }

    private void dispatchLoadMore() {
        if (isLoadError) return;

        if (!isAutoLoadMore) {
            if (mLoadMoreView != null)
                mLoadMoreView.onWaitToLoadMore(mLoadMoreListener);
        } else {
            if (isLoadMore || mDataEmpty || !mHasMore) return;

            isLoadMore = true;

            if (mLoadMoreView != null)
                mLoadMoreView.onLoading();

            if (mLoadMoreListener != null)
                mLoadMoreListener.onLoadMore();
        }
    }

    public final void loadMoreFinish(boolean dataEmpty, boolean hasMore) {
        isLoadMore = false;
        isLoadError = false;

        mDataEmpty = dataEmpty;
        mHasMore = hasMore;

        if (mLoadMoreView != null) {
            mLoadMoreView.onLoadFinish(dataEmpty, hasMore);
        }
    }

    public void loadMoreError(int errorCode, String errorMessage) {
        isLoadMore = false;
        isLoadError = true;

        if (mLoadMoreView != null) {
            mLoadMoreView.onLoadError(errorCode, errorMessage);
        }
    }

    public void setAutoLoadMore(boolean autoLoadMore) {
        isAutoLoadMore = autoLoadMore;
    }

    public interface LoadMoreView {

        /**
         * Show progress.
         */
        void onLoading();

        /**
         * Load finish, handle result.
         */
        void onLoadFinish(boolean dataEmpty, boolean hasMore);

        /**
         * Non-auto-loading mode, you can to click on the item to load.
         */
        void onWaitToLoadMore(LoadMoreListener loadMoreListener);

        /**
         * Load error.
         */
        void onLoadError(int errorCode, String errorMessage);
    }

    public interface LoadMoreListener {

        /**
         * More data should be requested.
         */
        void onLoadMore();
    }



    class DefaultLoadMoreView extends LinearLayout implements LoadMoreView, View.OnClickListener {

        private LoadingView mLoadingView;
        private TextView mTvMessage;

        private LoadMoreListener mLoadMoreListener;

        public DefaultLoadMoreView(Context context) {
            this(context,null);
        }

        public DefaultLoadMoreView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            setGravity(Gravity.CENTER);
            setVisibility(GONE);

            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

            int minHeight = (int) (displayMetrics.density * 30 + 0.5);
            setMinimumHeight(minHeight);


            mLoadingView = new LoadingView(context);
            mLoadingView.setVisibility(View.GONE);
            LayoutParams mLoadingViewLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            mLoadingView.setLayoutParams(mLoadingViewLp);


            mTvMessage = new TextView(context);
            mTvMessage.setGravity(Gravity.CENTER_VERTICAL);
            mTvMessage.setTextColor(Color.parseColor("#FF777777"));
            mTvMessage.setVisibility(View.GONE);
            LayoutParams mTvMessageLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            mTvMessage.setLayoutParams(mTvMessageLp);

            addView(mLoadingView);
            addView(mTvMessage);


            int color1 = Color.parseColor("#55777777");
            int color2 = Color.parseColor("#B1777777");
            int color3 = Color.parseColor("#FF777777");

            mLoadingView.setCircleColors(color1, color2, color3);

            setOnClickListener(this);
        }

        @Override
        public void onLoading() {
            setVisibility(VISIBLE);
            mLoadingView.setVisibility(VISIBLE);
            mTvMessage.setVisibility(VISIBLE);
            mTvMessage.setText("正在加载更多数据，请稍后");
        }

        @Override
        public void onLoadFinish(boolean dataEmpty, boolean hasMore) {
            if (!hasMore) {
                setVisibility(VISIBLE);

                if (dataEmpty) {
                    mLoadingView.setVisibility(GONE);
                    mTvMessage.setVisibility(VISIBLE);
                    mTvMessage.setText("暂时没有数据");
                } else {
                    mLoadingView.setVisibility(GONE);
                    mTvMessage.setVisibility(VISIBLE);
                    mTvMessage.setText("没有更多数据啦");
                }
            } else {
                setVisibility(INVISIBLE);
            }
        }

        @Override
        public void onWaitToLoadMore(LoadMoreListener loadMoreListener) {
            this.mLoadMoreListener = loadMoreListener;

            setVisibility(VISIBLE);
            mLoadingView.setVisibility(GONE);
            mTvMessage.setVisibility(VISIBLE);
            mTvMessage.setText("点击加载更多");
        }

        @Override
        public void onLoadError(int errorCode, String errorMessage) {
            setVisibility(VISIBLE);
            mLoadingView.setVisibility(GONE);
            mTvMessage.setVisibility(VISIBLE);
            mTvMessage.setText(TextUtils.isEmpty(errorMessage) ? "加载出错啦，请稍后重试" : errorMessage);
        }

        @Override
        public void onClick(View v) {
            if (mLoadMoreListener != null) mLoadMoreListener.onLoadMore();
        }


    }


    @SuppressLint("AppCompatCustomView")
    class LoadingView extends ImageView {

        private LoadingDrawable mLoadingDrawable;
        private LevelLoadingRenderer mLoadingRenderer;

        public LoadingView(Context context) {
            this(context,null);
        }

        public LoadingView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            mLoadingRenderer = new LevelLoadingRenderer(context);
            mLoadingDrawable = new LoadingDrawable(mLoadingRenderer);
            setImageDrawable(mLoadingDrawable);
        }

        /**
         * Set several colors of the circle.
         */
        public void setCircleColors(int r1, int r2, int r3) {
            mLoadingRenderer.setCircleColors(r1, r2, r3);
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            startAnimation();
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            stopAnimation();
        }

        @Override
        protected void onVisibilityChanged(View changedView, int visibility) {
            super.onVisibilityChanged(changedView, visibility);
            if (visibility == View.VISIBLE) {
                startAnimation();
            } else {
                stopAnimation();
            }
        }

        private void startAnimation() {
            if (mLoadingDrawable != null) {
                mLoadingDrawable.start();
            }
        }

        private void stopAnimation() {
            if (mLoadingDrawable != null) {
                mLoadingDrawable.stop();
            }
        }


    }

    class LoadingDrawable extends Drawable implements Animatable {

        private final LoadingRenderer mLoadingRender;

        private final Callback mCallback = new Callback() {
            @Override
            public void invalidateDrawable(Drawable d) {
                invalidateSelf();
            }

            @Override
            public void scheduleDrawable(Drawable d, Runnable what, long when) {
                scheduleSelf(what, when);
            }

            @Override
            public void unscheduleDrawable(Drawable d, Runnable what) {
                unscheduleSelf(what);
            }
        };

        public LoadingDrawable(LoadingRenderer loadingRender) {
            this.mLoadingRender = loadingRender;
            this.mLoadingRender.setCallback(mCallback);
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            this.mLoadingRender.setBounds(bounds);
        }

        @Override
        public void draw(Canvas canvas) {
            if (!getBounds().isEmpty()) {
                this.mLoadingRender.draw(canvas);
            }
        }

        @Override
        public void setAlpha(int alpha) {
            this.mLoadingRender.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            this.mLoadingRender.setColorFilter(cf);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        @Override
        public void start() {
            this.mLoadingRender.start();
        }

        @Override
        public void stop() {
            this.mLoadingRender.stop();
        }

        @Override
        public boolean isRunning() {
            return this.mLoadingRender.isRunning();
        }

        @Override
        public int getIntrinsicHeight() {
            return (int) this.mLoadingRender.mHeight;
        }

        @Override
        public int getIntrinsicWidth() {
            return (int) this.mLoadingRender.mWidth;
        }

    }


    abstract static class LoadingRenderer {
        private static final long ANIMATION_DURATION = 1333;

        private final ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                computeRender((float) animation.getAnimatedValue());
                invalidateSelf();
            }
        };

        /**
         * Whenever {@link LoadingDrawable} boundary changes mBounds will be updated.
         * More details you can see {@link LoadingDrawable#onBoundsChange(Rect)}
         */
        protected final Rect mBounds = new Rect();

        private Drawable.Callback mCallback;
        private ValueAnimator mRenderAnimator;

        protected long mDuration;
        protected float mWidth;
        protected float mHeight;

        public LoadingRenderer(Context context) {
            mWidth = mHeight = DipUtil.dip2px(context, 30F);//56
            mDuration = ANIMATION_DURATION;
            setupAnimators();
        }

        @Deprecated
        protected void draw(Canvas canvas, Rect bounds) {
        }

        protected void draw(Canvas canvas) {
            draw(canvas, mBounds);
        }

        protected abstract void computeRender(float renderProgress);

        protected abstract void setAlpha(int alpha);

        protected abstract void setColorFilter(ColorFilter cf);

        protected abstract void reset();

        protected void addRenderListener(Animator.AnimatorListener animatorListener) {
            mRenderAnimator.addListener(animatorListener);
        }

        void start() {
            reset();
            mRenderAnimator.addUpdateListener(mAnimatorUpdateListener);

            mRenderAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mRenderAnimator.setDuration(mDuration);
            mRenderAnimator.start();
        }

        void stop() {
            mRenderAnimator.removeUpdateListener(mAnimatorUpdateListener);
            mRenderAnimator.setRepeatCount(0);
            mRenderAnimator.setDuration(0);
            mRenderAnimator.end();
        }

        boolean isRunning() {
            return mRenderAnimator.isRunning();
        }

        void setCallback(Drawable.Callback callback) {
            this.mCallback = callback;
        }

        void setBounds(Rect bounds) {
            mBounds.set(bounds);
        }

        private void setupAnimators() {
            mRenderAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            mRenderAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mRenderAnimator.setRepeatMode(ValueAnimator.RESTART);
            mRenderAnimator.setDuration(mDuration);
            mRenderAnimator.setInterpolator(new LinearInterpolator());
            mRenderAnimator.addUpdateListener(mAnimatorUpdateListener);
        }

        private void invalidateSelf() {
            mCallback.invalidateDrawable(null);
        }

    }

    public static class LevelLoadingRenderer extends LoadingRenderer {
        private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
        private static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();
        private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
        private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

        private static final int NUM_POINTS = 5;
        private static final int DEGREE_360 = 360;

        private static final float MAX_SWIPE_DEGREES = 0.8f * DEGREE_360;
        private static final float FULL_GROUP_ROTATION = 3.0f * DEGREE_360;

        private static final float[] LEVEL_SWEEP_ANGLE_OFFSETS = new float[]{1.0f, 7.0f / 8.0f, 5.0f / 8.0f};

        private static final float START_TRIM_DURATION_OFFSET = 0.5f;
        private static final float END_TRIM_DURATION_OFFSET = 1.0f;

        private static final float DEFAULT_CENTER_RADIUS = 5f;//12.5f
        private static final float DEFAULT_STROKE_WIDTH = 1.5f;//2.5

        private static final int[] DEFAULT_LEVEL_COLORS = new int[]{
                Color.parseColor("#55ffffff"),
                Color.parseColor("#b1ffffff"),
                Color.parseColor("#ffffffff")
        };

        private final Paint mPaint = new Paint();
        private final RectF mTempBounds = new RectF();

        private final Animator.AnimatorListener mAnimatorListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animator) {
                super.onAnimationRepeat(animator);
                storeOriginals();

                mStartDegrees = mEndDegrees;
                mRotationCount = (mRotationCount + 1) % (NUM_POINTS);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mRotationCount = 0;
            }
        };

        // size 3.
        private int[] mLevelColors;
        // size 3.
        private float[] mLevelSwipeDegrees;

        private float mStrokeInset;

        private float mRotationCount;
        private float mGroupRotation;

        private float mEndDegrees;
        private float mStartDegrees;
        private float mOriginEndDegrees;
        private float mOriginStartDegrees;

        private float mStrokeWidth;
        private float mCenterRadius;

        public LevelLoadingRenderer(Context context) {
            super(context);
            init(context);
            setupPaint();
            addRenderListener(mAnimatorListener);
        }

        private void init(Context context) {
            mStrokeWidth = DipUtil.dip2px(context, DEFAULT_STROKE_WIDTH);
            mCenterRadius = DipUtil.dip2px(context, DEFAULT_CENTER_RADIUS);

            mLevelSwipeDegrees = new float[3];
            mLevelColors = DEFAULT_LEVEL_COLORS;
        }

        private void setupPaint() {
            mPaint.setAntiAlias(true);
            mPaint.setStrokeWidth(mStrokeWidth);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeCap(Paint.Cap.ROUND);

            initStrokeInset((int) mWidth, (int) mHeight);
        }

        /**
         * Set several colors of the circle.
         */
        public void setCircleColors(int r1, int r2, int r3) {
            mLevelColors = new int[]{r1, r2, r3};
        }

        @Override
        protected void draw(Canvas canvas) {
            int saveCount = canvas.save();

            mTempBounds.set(mBounds);
            mTempBounds.inset(mStrokeInset, mStrokeInset);
            canvas.rotate(mGroupRotation, mTempBounds.centerX(), mTempBounds.centerY());

            for (int i = 0; i < 3; i++) {
                if (mLevelSwipeDegrees[i] != 0) {
                    mPaint.setColor(mLevelColors[i]);
                    canvas.drawArc(mTempBounds, mEndDegrees, mLevelSwipeDegrees[i], false, mPaint);
                }
            }

            canvas.restoreToCount(saveCount);
        }

        @Override
        protected void computeRender(float renderProgress) {
            // Moving the start trim only occurs in the first 50% of a single ring animation
            if (renderProgress <= START_TRIM_DURATION_OFFSET) {
                float startTrimProgress = (renderProgress) / START_TRIM_DURATION_OFFSET;
                mStartDegrees = mOriginStartDegrees + MAX_SWIPE_DEGREES * MATERIAL_INTERPOLATOR.getInterpolation(startTrimProgress);

                float mSwipeDegrees = mEndDegrees - mStartDegrees;
                float levelSwipeDegreesProgress = Math.abs(mSwipeDegrees) / MAX_SWIPE_DEGREES;

                float level1Increment = DECELERATE_INTERPOLATOR.getInterpolation(levelSwipeDegreesProgress)
                        - LINEAR_INTERPOLATOR.getInterpolation(levelSwipeDegreesProgress);
                float level3Increment = ACCELERATE_INTERPOLATOR.getInterpolation(levelSwipeDegreesProgress)
                        - LINEAR_INTERPOLATOR.getInterpolation(levelSwipeDegreesProgress);

                mLevelSwipeDegrees[0] = -mSwipeDegrees * LEVEL_SWEEP_ANGLE_OFFSETS[0] * (1.0f + level1Increment);
                mLevelSwipeDegrees[1] = -mSwipeDegrees * LEVEL_SWEEP_ANGLE_OFFSETS[1] * 1.0f;
                mLevelSwipeDegrees[2] = -mSwipeDegrees * LEVEL_SWEEP_ANGLE_OFFSETS[2] * (1.0f + level3Increment);
            }

            // Moving the end trim starts after 50% of a single ring animation
            if (renderProgress > START_TRIM_DURATION_OFFSET) {
                float endTrimProgress = (renderProgress - START_TRIM_DURATION_OFFSET) / (END_TRIM_DURATION_OFFSET -
                        START_TRIM_DURATION_OFFSET);
                mEndDegrees = mOriginEndDegrees + MAX_SWIPE_DEGREES * MATERIAL_INTERPOLATOR.getInterpolation(endTrimProgress);

                float mSwipeDegrees = mEndDegrees - mStartDegrees;
                float levelSwipeDegreesProgress = Math.abs(mSwipeDegrees) / MAX_SWIPE_DEGREES;

                if (levelSwipeDegreesProgress > LEVEL_SWEEP_ANGLE_OFFSETS[1]) {
                    mLevelSwipeDegrees[0] = -mSwipeDegrees;
                    mLevelSwipeDegrees[1] = MAX_SWIPE_DEGREES * LEVEL_SWEEP_ANGLE_OFFSETS[1];
                    mLevelSwipeDegrees[2] = MAX_SWIPE_DEGREES * LEVEL_SWEEP_ANGLE_OFFSETS[2];
                } else if (levelSwipeDegreesProgress > LEVEL_SWEEP_ANGLE_OFFSETS[2]) {
                    mLevelSwipeDegrees[0] = 0;
                    mLevelSwipeDegrees[1] = -mSwipeDegrees;
                    mLevelSwipeDegrees[2] = MAX_SWIPE_DEGREES * LEVEL_SWEEP_ANGLE_OFFSETS[2];
                } else {
                    mLevelSwipeDegrees[0] = 0;
                    mLevelSwipeDegrees[1] = 0;
                    mLevelSwipeDegrees[2] = -mSwipeDegrees;
                }
            }

            mGroupRotation = ((FULL_GROUP_ROTATION / NUM_POINTS) * renderProgress) + (FULL_GROUP_ROTATION * (mRotationCount /
                    NUM_POINTS));
        }

        @Override
        protected void setAlpha(int alpha) {
            mPaint.setAlpha(alpha);
        }

        @Override
        protected void setColorFilter(ColorFilter cf) {
            mPaint.setColorFilter(cf);
        }

        @Override
        protected void reset() {
            resetOriginals();
        }

        private void initStrokeInset(float width, float height) {
            float minSize = Math.min(width, height);
            float strokeInset = minSize / 2.0f - mCenterRadius;
            float minStrokeInset = (float) Math.ceil(mStrokeWidth / 2.0f);
            mStrokeInset = strokeInset < minStrokeInset ? minStrokeInset : strokeInset;
        }

        private void storeOriginals() {
            mOriginEndDegrees = mEndDegrees;
            mOriginStartDegrees = mEndDegrees;
        }

        private void resetOriginals() {
            mOriginEndDegrees = 0;
            mOriginStartDegrees = 0;

            mEndDegrees = 0;
            mStartDegrees = 0;

            mLevelSwipeDegrees[0] = 0;
            mLevelSwipeDegrees[1] = 0;
            mLevelSwipeDegrees[2] = 0;
        }

        private void apply(Builder builder) {
            this.mWidth = builder.mWidth > 0 ? builder.mWidth : this.mWidth;
            this.mHeight = builder.mHeight > 0 ? builder.mHeight : this.mHeight;
            this.mStrokeWidth = builder.mStrokeWidth > 0 ? builder.mStrokeWidth : this.mStrokeWidth;
            this.mCenterRadius = builder.mCenterRadius > 0 ? builder.mCenterRadius : this.mCenterRadius;

            this.mDuration = builder.mDuration > 0 ? builder.mDuration : this.mDuration;

            this.mLevelColors = builder.mLevelColors != null ? builder.mLevelColors : this.mLevelColors;

            setupPaint();
            initStrokeInset(this.mWidth, this.mHeight);
        }

        public static class Builder {
            private Context mContext;

            private int mWidth;
            private int mHeight;
            private int mStrokeWidth;
            private int mCenterRadius;

            private int mDuration;

            private int[] mLevelColors;

            public Builder(Context mContext) {
                this.mContext = mContext;
            }

            public Builder setWidth(int width) {
                this.mWidth = width;
                return this;
            }

            public Builder setHeight(int height) {
                this.mHeight = height;
                return this;
            }

            public Builder setStrokeWidth(int strokeWidth) {
                this.mStrokeWidth = strokeWidth;
                return this;
            }

            public Builder setCenterRadius(int centerRadius) {
                this.mCenterRadius = centerRadius;
                return this;
            }

            public Builder setDuration(int duration) {
                this.mDuration = duration;
                return this;
            }

            // size 3.
            public Builder setLevelColors(int[] colors) {
                this.mLevelColors = colors;
                return this;
            }

            public Builder setLevelColor(int color) {
                return setLevelColors(new int[]{oneThirdAlphaColor(color), twoThirdAlphaColor(color), color});
            }

            public LevelLoadingRenderer build() {
                LevelLoadingRenderer loadingRenderer = new LevelLoadingRenderer(mContext);
                loadingRenderer.apply(this);
                return loadingRenderer;
            }

            private int oneThirdAlphaColor(int colorValue) {
                int startA = (colorValue >> 24) & 0xff;
                int startR = (colorValue >> 16) & 0xff;
                int startG = (colorValue >> 8) & 0xff;
                int startB = colorValue & 0xff;

                return (startA / 3 << 24) | (startR << 16) | (startG << 8) | startB;
            }

            private int twoThirdAlphaColor(int colorValue) {
                int startA = (colorValue >> 24) & 0xff;
                int startR = (colorValue >> 16) & 0xff;
                int startG = (colorValue >> 8) & 0xff;
                int startB = colorValue & 0xff;

                return (startA * 2 / 3 << 24) | (startR << 16) | (startG << 8) | startB;
            }
        }

    }


}

