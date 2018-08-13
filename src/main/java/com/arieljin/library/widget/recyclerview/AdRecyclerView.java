package com.arieljin.library.widget.recyclerview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.arieljin.library.R;
import com.arieljin.library.widget.recyclerview.swipe.SwipeMenu;
import com.arieljin.library.widget.recyclerview.swipe.SwipeMenuCreator;
import com.arieljin.library.widget.recyclerview.swipe.SwipeMenuDirection;
import com.arieljin.library.widget.recyclerview.swipe.SwipeMenuLayout;
import com.arieljin.library.widget.recyclerview.swipe.SwipeMenuView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class AdRecyclerView extends RecyclerView {

    protected AdapterWrapper mAdapterWrapper;

    private SwipeMenuCreator mSwipeMenuCreator;

    protected SwipeMenuLayout mOldSwipedLayout;
    private int mDownX;
    private int mDownY;
    protected int mOldTouchedPosition = INVALID_POSITION;
    private static final int INVALID_POSITION = -1;
    protected int mScaleTouchSlop;

    private boolean allowSwipeDelete = false;

    protected List<View> mHeaderViewList = new ArrayList<>();
    protected List<View> mFooterViewList = new ArrayList<>();

    public AdRecyclerView(Context context) {
        this(context, null);
    }

    public AdRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public AdRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScaleTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public void addHeaderView(View view) {
        if (mAdapterWrapper != null)
            throw new IllegalStateException("Cannot add header view, setAdapter has already been called.");
        mHeaderViewList.add(view);
    }

    public void addFooterView(View view) {
        if (mAdapterWrapper != null)
            throw new IllegalStateException("Cannot add footer view, setAdapter has already been called.");
        mFooterViewList.add(view);
    }

    private SwipeMenuCreator mDefaultMenuCreator = new SwipeMenuCreator() {
        @Override
        public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
            if (mSwipeMenuCreator != null) {
                mSwipeMenuCreator.onCreateMenu(swipeLeftMenu, swipeRightMenu, viewType);
            }
        }
    };

    @Override
    public void setAdapter(Adapter adapter) {
        if (mAdapterWrapper != null) {
            if (mAdapterWrapper.getOriginAdapter() == adapter) {
                adapter.notifyDataSetChanged();
                return;
            }

            mAdapterWrapper.getOriginAdapter().unregisterAdapterDataObserver(mAdapterDataObserver);
        }

        adapter.registerAdapterDataObserver(mAdapterDataObserver);

        mAdapterWrapper = new AdapterWrapper(adapter);
        mAdapterWrapper.setSwipeMenuCreator(mDefaultMenuCreator);
        super.setAdapter(mAdapterWrapper);

        if (mHeaderViewList.size() > 0)
            for (View view : mHeaderViewList) {
                mAdapterWrapper.addHeaderView(view);
            }
        if (mFooterViewList.size() > 0)
            for (View view : mFooterViewList) {
                mAdapterWrapper.addFooterView(view);
            }
    }

    public void setSwipeMenuCreator(SwipeMenuCreator mSwipeMenuCreator) {
        this.mSwipeMenuCreator = mSwipeMenuCreator;
    }


    private View getSwipeMenuView(View itemView) {
        if (itemView instanceof SwipeMenuLayout) return itemView;
        List<View> unvisited = new ArrayList<>();
        unvisited.add(itemView);
        while (!unvisited.isEmpty()) {
            View child = unvisited.remove(0);
            if (!(child instanceof ViewGroup)) { // view
                continue;
            }
            if (child instanceof SwipeMenuLayout) return child;
            ViewGroup group = (ViewGroup) child;
            final int childCount = group.getChildCount();
            for (int i = 0; i < childCount; i++) unvisited.add(group.getChildAt(i));
        }
        return itemView;
    }

    private boolean handleUnDown(int x, int y, boolean defaultValue) {
        int disX = mDownX - x;
        int disY = mDownY - y;

        // swipe
        if (Math.abs(disX) > mScaleTouchSlop && Math.abs(disX) > Math.abs(disY))
            return false;
        // click
        if (Math.abs(disY) < mScaleTouchSlop && Math.abs(disX) < mScaleTouchSlop)
            return false;
        return defaultValue;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {


        boolean isIntercepted = super.onInterceptTouchEvent(e);
        if (allowSwipeDelete)  // swipe and menu conflict.
            return isIntercepted;
        else {
            if (e.getPointerCount() > 1) return true;
            int action = e.getAction();
            int x = (int) e.getX();
            int y = (int) e.getY();
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    mDownX = x;
                    mDownY = y;
                    isIntercepted = false;

                    int touchingPosition = getChildAdapterPosition(findChildViewUnder(x, y));
                    if (touchingPosition != mOldTouchedPosition && mOldSwipedLayout != null && mOldSwipedLayout.isMenuOpen()) {
                        mOldSwipedLayout.smoothCloseMenu();
                        isIntercepted = true;
                    }

                    if (isIntercepted) {
                        mOldSwipedLayout = null;
                        mOldTouchedPosition = INVALID_POSITION;
                    } else {
                        ViewHolder vh = findViewHolderForAdapterPosition(touchingPosition);
                        if (vh != null) {
                            View itemView = getSwipeMenuView(vh.itemView);
                            if (itemView instanceof SwipeMenuLayout) {
                                mOldSwipedLayout = (SwipeMenuLayout) itemView;
                                mOldTouchedPosition = touchingPosition;
                            }
                        }
                    }
                    break;
                }
                // They are sensitive to retain sliding and inertia.
                case MotionEvent.ACTION_MOVE: {
                    isIntercepted = handleUnDown(x, y, isIntercepted);
                    if (mOldSwipedLayout == null) break;
                    ViewParent viewParent = getParent();
                    if (viewParent == null) break;

                    int disX = mDownX - x;
                    // 向左滑，显示右侧菜单，或者关闭左侧菜单。
                    boolean showRightCloseLeft = disX > 0 && (mOldSwipedLayout.hasRightMenu() || mOldSwipedLayout.isLeftCompleteOpen());
                    // 向右滑，显示左侧菜单，或者关闭右侧菜单。
                    boolean showLeftCloseRight = disX < 0 && (mOldSwipedLayout.hasLeftMenu() || mOldSwipedLayout.isRightCompleteOpen());
                    viewParent.requestDisallowInterceptTouchEvent(showRightCloseLeft || showLeftCloseRight);
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    isIntercepted = handleUnDown(x, y, isIntercepted);
                    break;
                }
            }


        }
        return isIntercepted;

    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int action = e.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                if (mOldSwipedLayout != null && mOldSwipedLayout.isMenuOpen()) {
                    mOldSwipedLayout.smoothCloseMenu();
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return super.onTouchEvent(e);
    }

    private AdapterDataObserver mAdapterDataObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            mAdapterWrapper.notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            positionStart += getHeaderItemCount();
            mAdapterWrapper.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            positionStart += getHeaderItemCount();
            mAdapterWrapper.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            positionStart += getHeaderItemCount();
            mAdapterWrapper.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            positionStart += getHeaderItemCount();
            mAdapterWrapper.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            fromPosition += getHeaderItemCount();
            toPosition += getHeaderItemCount();
            mAdapterWrapper.notifyItemMoved(fromPosition, toPosition);
        }
    };

    public int getHeaderItemCount() {
        if (mAdapterWrapper == null) return 0;
        return mAdapterWrapper.getHeaderItemCount();
    }

    public class AdapterWrapper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int BASE_ITEM_TYPE_HEADER = 100000;
        private static final int BASE_ITEM_TYPE_FOOTER = 200000;

        private SparseArrayCompat<View> mHeaderViews = new SparseArrayCompat<>();
        private SparseArrayCompat<View> mFootViews = new SparseArrayCompat<>();

        private RecyclerView.Adapter mAdapter;


        private SwipeMenuCreator mSwipeMenuCreator;

        AdapterWrapper(RecyclerView.Adapter adapter) {
            mAdapter = adapter;
        }

        public RecyclerView.Adapter getOriginAdapter() {
            return mAdapter;
        }

        public void setSwipeMenuCreator(SwipeMenuCreator mSwipeMenuCreator) {
            this.mSwipeMenuCreator = mSwipeMenuCreator;
        }

        @Override
        public int getItemCount() {
            return getHeaderItemCount() + getContentItemCount() + getFooterItemCount();
        }

        private int getContentItemCount() {
            return mAdapter.getItemCount();
        }

        @Override
        public int getItemViewType(int position) {
            if (isHeaderView(position)) {
                return mHeaderViews.keyAt(position);
            } else if (isFooterView(position)) {
                return mFootViews.keyAt(position - getHeaderItemCount() - getContentItemCount());
            }
            return mAdapter.getItemViewType(position - getHeaderItemCount());
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (mHeaderViews.get(viewType) != null) {
                return new ViewHolder(mHeaderViews.get(viewType));
            } else if (mFootViews.get(viewType) != null) {
                return new ViewHolder(mFootViews.get(viewType));
            }
            final RecyclerView.ViewHolder viewHolder = mAdapter.onCreateViewHolder(parent, viewType);

            SwipeMenuLayout swipeMenuLayout = (SwipeMenuLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycler_swipe_view_item, parent, false);

            SwipeMenu swipeLeftMenu = new SwipeMenu(swipeMenuLayout, viewType);
            SwipeMenu swipeRightMenu = new SwipeMenu(swipeMenuLayout, viewType);

            mSwipeMenuCreator.onCreateMenu(swipeLeftMenu, swipeRightMenu, viewType);

            int leftMenuCount = swipeLeftMenu.getMenuItems().size();
            if (leftMenuCount > 0) {
                SwipeMenuView swipeLeftMenuView = (SwipeMenuView) swipeMenuLayout.findViewById(R.id.swipe_left);
                // noinspection WrongConstant
                swipeLeftMenuView.setOrientation(swipeLeftMenu.getOrientation());
                swipeLeftMenuView.createMenu(swipeLeftMenu, swipeMenuLayout,/* mSwipeMenuItemClickListener, */SwipeMenuDirection.LEFT_DIRECTION);
            }

            int rightMenuCount = swipeRightMenu.getMenuItems().size();
            if (rightMenuCount > 0) {
                SwipeMenuView swipeRightMenuView = (SwipeMenuView) swipeMenuLayout.findViewById(R.id.swipe_right);
                // noinspection WrongConstant
                swipeRightMenuView.setOrientation(swipeRightMenu.getOrientation());
                swipeRightMenuView.createMenu(swipeRightMenu, swipeMenuLayout, /*mSwipeMenuItemClickListener,*/ SwipeMenuDirection.RIGHT_DIRECTION);
            }

//            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mSwipeItemClickListener.onItemClick(v, viewHolder.getAdapterPosition());
//                }
//            });

            if (leftMenuCount > 0 || rightMenuCount > 0) {
                ViewGroup viewGroup = (ViewGroup) swipeMenuLayout.findViewById(R.id.swipe_content);
                viewGroup.addView(viewHolder.itemView);

                try {
                    Field itemView = getSupperClass(viewHolder.getClass()).getDeclaredField("itemView");
                    if (!itemView.isAccessible()) itemView.setAccessible(true);
                    itemView.set(viewHolder, swipeMenuLayout);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
            if (isHeaderView(position) || isFooterView(position)) {
                return;
            }

            View itemView = holder.itemView;
            if (itemView instanceof SwipeMenuLayout) {
                SwipeMenuLayout swipeMenuLayout = (SwipeMenuLayout) itemView;
                int childCount = swipeMenuLayout.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View childView = swipeMenuLayout.getChildAt(i);
                    if (childView instanceof SwipeMenuView) {
                        ((SwipeMenuView) childView).bindViewHolder(holder);
                    }
                }
            }

            mAdapter.onBindViewHolder(holder, position - getHeaderItemCount(), payloads);
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            mAdapter.onAttachedToRecyclerView(recyclerView);

            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {
                final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
                final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();

                gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        int viewType = getItemViewType(position);
                        if (mHeaderViews.get(viewType) != null) {
                            return gridLayoutManager.getSpanCount();
                        } else if (mFootViews.get(viewType) != null) {
                            return gridLayoutManager.getSpanCount();
                        }
                        if (spanSizeLookup != null)
                            return spanSizeLookup.getSpanSize(position);
                        return 1;
                    }
                });
                gridLayoutManager.setSpanCount(gridLayoutManager.getSpanCount());
            }
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            //noinspection unchecked
            int position = holder.getLayoutPosition();

            if (isHeaderView(position) || isFooterView(position)) {
                ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
                if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                    StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                    p.setFullSpan(true);
                }
            } else {
                mAdapter.onViewAttachedToWindow(holder);
            }
        }

        private Class<?> getSupperClass(Class<?> aClass) {
            Class<?> supperClass = aClass.getSuperclass();
            if (supperClass != null && !supperClass.equals(Object.class)) {
                return getSupperClass(supperClass);
            }
            return aClass;
        }

        private boolean isHeaderView(int position) {
            return position < getHeaderItemCount();
        }

        private boolean isFooterView(int position) {
            return position >= getHeaderItemCount() + getContentItemCount();
        }

        public void addHeaderView(View view) {
            mHeaderViews.put(mHeaderViews.size() + BASE_ITEM_TYPE_HEADER, view);
        }

        public void addFooterView(View view) {
            mFootViews.put(mFootViews.size() + BASE_ITEM_TYPE_FOOTER, view);
        }

        public int getHeaderItemCount() {
            return mHeaderViews.size();
        }

        public int getFooterItemCount() {
            return mFootViews.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(View itemView) {
                super(itemView);
            }
        }

        @Override
        public void setHasStableIds(boolean hasStableIds) {
            super.setHasStableIds(hasStableIds);
            mAdapter.setHasStableIds(hasStableIds);
        }

        @Override
        public long getItemId(int position) {
            if (!isHeaderView(position) && !isFooterView(position)) {
                return mAdapter.getItemId(position);
            }
            return super.getItemId(position);
        }

        @Override
        public void onViewRecycled(RecyclerView.ViewHolder holder) {
            int position = holder.getAdapterPosition();

            if (!isHeaderView(position) && !isFooterView(position))
                mAdapter.onViewRecycled(holder);
        }

        @Override
        public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
            int position = holder.getAdapterPosition();

            if (!isHeaderView(position) && !isFooterView(position))
                return mAdapter.onFailedToRecycleView(holder);
            return false;
        }

        @Override
        public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
            int position = holder.getAdapterPosition();

            if (!isHeaderView(position) && !isFooterView(position))
                mAdapter.onViewDetachedFromWindow(holder);
        }

        @Override
        public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
            super.registerAdapterDataObserver(observer);
        }

        @Override
        public void unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
            super.unregisterAdapterDataObserver(observer);
        }

        @Override
        public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
            mAdapter.onDetachedFromRecyclerView(recyclerView);
        }
    }
}
