package com.arieljin.library.widget.adapter;

import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * @time 2018/8/22.
 * @email ariel.jin@tom.com
 */
public class LoopPagerAdapterWrapper extends BasePagerAdapter {

    private BasePagerAdapter mAdapter;

    private SparseArray<ToDestroy> mToDestroy = new SparseArray<>();

    private static final boolean DEFAULT_BOUNDARY_CASHING = true;
    private static final boolean DEFAULT_BOUNDARY_LOOPING = true;

    private boolean mBoundaryCaching = DEFAULT_BOUNDARY_CASHING;
    private boolean mBoundaryLooping = DEFAULT_BOUNDARY_LOOPING;

    public void setBoundaryCaching(boolean flag) {
        mBoundaryCaching = flag;
    }

    public void setBoundaryLooping(boolean flag) {
        mBoundaryLooping = flag;
    }

    public LoopPagerAdapterWrapper(BasePagerAdapter adapter) {
        super(adapter.list, adapter.render);
        this.mAdapter = adapter;
    }

    @Override
    public void notifyDataSetChanged() {
        mToDestroy = new SparseArray<>();
        super.notifyDataSetChanged();
    }

    public int toRealPosition(int position) {
        int realPosition = position;
        int realCount = getRealCount();
        if (realCount == 0)
            return 0;
        if (mBoundaryLooping) {
            realPosition = (position - 1) % realCount;
            if (realPosition < 0)
                realPosition += realCount;
        }

        return realPosition;
    }

    public int toInnerPosition(int realPosition) {
        int position = (realPosition + 1);
        return mBoundaryLooping ? position : realPosition;
    }

    private int getRealFirstPosition() {
        return mBoundaryLooping ? 1 : 0;
    }

    private int getRealLastPosition() {
        return getRealFirstPosition() + getRealCount() - 1;
    }

    @Override
    public int getCount() {
        int count = getRealCount();
        return mBoundaryLooping ? count + 2 : count;
    }

    public int getRealCount() {
        return mAdapter != null ? mAdapter.getCount() : 0;
    }

    public BasePagerAdapter getRealAdapter() {
        return mAdapter;
    }

    public void setAdapterList(List list) {

        if (mAdapter != null)
            mAdapter.setList(list);
        notifyDataSetChanged();

    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int realPosition = toRealPosition(position);

        if (mBoundaryCaching) {
            ToDestroy toDestroy = mToDestroy.get(position);
            if (toDestroy != null) {
                mToDestroy.remove(position);
                return toDestroy.object;
            }
        }
        return mAdapter != null ? mAdapter.instantiateItem(container, realPosition) : null;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        int realFirst = getRealFirstPosition();
        int realLast = getRealLastPosition();
        int realPosition = toRealPosition(position);

        if (mBoundaryCaching && (position == realFirst || position == realLast)) {
            mToDestroy.put(position, new ToDestroy(container, realPosition, object));
        } else {
            if (mAdapter != null)
                mAdapter.destroyItem(container, realPosition, object);
        }
    }

    /*
     * Delegate rest of methods directly to the inner adapter.
     */

    @Override
    public void finishUpdate(ViewGroup container) {
        if (mAdapter != null)
            mAdapter.finishUpdate(container);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        if (mAdapter != null)
            return mAdapter.isViewFromObject(view, object);
        return false;
    }

    @Override
    public void restoreState(Parcelable bundle, ClassLoader classLoader) {
        if (mAdapter != null)
            mAdapter.restoreState(bundle, classLoader);
    }

    @Override
    public Parcelable saveState() {
        if (mAdapter != null)
            return mAdapter.saveState();
        return null;
    }

    @Override
    public void startUpdate(ViewGroup container) {
        if (mAdapter != null)
            mAdapter.startUpdate(container);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (mAdapter != null)
            mAdapter.setPrimaryItem(container, position, object);
    }

    /*
     * End delegation
     */

    /**
     * Container class for caching the boundary views
     */
    static class ToDestroy {
        ViewGroup container;
        int position;
        Object object;

        public ToDestroy(ViewGroup container, int position, Object object) {
            this.container = container;
            this.position = position;
            this.object = object;
        }
    }

}
