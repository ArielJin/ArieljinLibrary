package com.arieljin.library.widget.recyclerview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class AdRecyclerView extends RecyclerView {

    protected AdapterWrapper mAdapterWrapper;

    protected List<View> mHeaderViewList = new ArrayList<>();
    protected List<View> mFooterViewList = new ArrayList<>();

    public AdRecyclerView(Context context) {
        super(context);
    }

    public AdRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AdRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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
        AdapterWrapper(RecyclerView.Adapter adapter) {
            mAdapter = adapter;
        }

        public RecyclerView.Adapter getOriginAdapter() {
            return mAdapter;
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
            RecyclerView.ViewHolder viewHolder = mAdapter.onCreateViewHolder(parent, viewType);

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
