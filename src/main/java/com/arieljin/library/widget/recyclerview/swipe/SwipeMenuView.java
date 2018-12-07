package com.arieljin.library.widget.recyclerview.swipe;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.arieljin.library.widget.adapter.AbsRecyclerAdapter;
import com.arieljin.library.widget.recyclerview.AdRecyclerView;

import java.io.Serializable;
import java.util.List;

/**
 * @time 2018/8/13.
 * @email ariel.jin@tom.com
 */
public class SwipeMenuView extends LinearLayout {


    private SwipeSwitch mSwipeSwitch;
//    private OnSwipeMenuItemClickListener mItemClickListener;
    //    private int mDirection;
//    private RecyclerView.ViewHolder mAdapterViewHolder;

    private int position;

    public SwipeMenuView(Context context) {
        this(context, null);
    }

    public SwipeMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void createMenu(SwipeMenu swipeMenu, SwipeSwitch swipeSwitch/*, OnSwipeMenuItemClickListener swipeMenuItemClickListener, int direction*/) {
        removeAllViews();

        this.mSwipeSwitch = swipeSwitch;
//        this.mItemClickListener = swipeMenuItemClickListener;
//        this.mDirection = direction;

//        List<SwipeMenuItem> items = swipeMenu.getMenuItems();
//        for (int i = 0; i < items.size(); i++) {
//            addItem(items.get(i), i);
//        }
        addItem(swipeMenu.getSwipeMenuItem());
    }

    public void bindViewPosition(/*RecyclerView.ViewHolder adapterViewHolder, */int position) {
//        this.mAdapterViewHolder = adapterViewHolder;
        this.position = position;
    }

    private void addItem(SwipeMenuItem item/*, final int index*/) {
//        LayoutParams params = new LayoutParams(item.getWidth(), item.getHeight());
//        params.weight = item.getWeight();
//        LinearLayout parent = new LinearLayout(getContext());
//        parent.setId(index);
//        parent.setGravity(Gravity.CENTER);
//        parent.setOrientation(VERTICAL);
//        parent.setLayoutParams(params);
//        ViewCompat.setBackground(parent, item.getBackground());
//        parent.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (mItemClickListener != null && mSwipeSwitch.isMenuOpen()) {
////            SwipeMenuBridge menuBridge = (SwipeMenuBridge) v.getTag();
////            menuBridge.mAdapterPosition = mAdapterViewHolder.getAdapterPosition();
//                    mItemClickListener.onItemClick(v,mAdapterViewHolder.getAdapterPosition(), index);
//                }
//
//            }
//        });
//        addView(parent);
        setGravity(Gravity.CENTER);
        setOrientation(VERTICAL);

        addView(item.getMenuView());
        final OnSwipeMenuItemClickListener onSwipeMenuItemClickListener = item.getOnSwipeMenuItemClickListener();

        int count = getChildCount();

        if (count > 0) {

            OnClickListener onClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (onSwipeMenuItemClickListener != null && mSwipeSwitch.isMenuOpen() && position >= 0) {

                        RecyclerView recyclerView = onSwipeMenuItemClickListener.getRecyclerView();
                        if (recyclerView != null) {
                            RecyclerView.Adapter adapter = recyclerView.getAdapter();

                            List<? extends Serializable> list = null;

                            if (adapter != null) {

                                if (adapter instanceof AdRecyclerView.AdapterWrapper) {

                                    RecyclerView.Adapter originAdapter = ((AdRecyclerView.AdapterWrapper) adapter).getOriginAdapter();

                                    if (originAdapter != null && originAdapter instanceof AbsRecyclerAdapter)
                                        list = ((AbsRecyclerAdapter) originAdapter).getList();

                                } else if (adapter instanceof AbsRecyclerAdapter) {

                                    list = ((AbsRecyclerAdapter) adapter).getList();

                                }

                            }

                            if (list != null && list.size() > position) {

                                onSwipeMenuItemClickListener.onMenuItemClick(v, position, list.get(position));

                            } else {
                                Toast.makeText(getContext(), "数据存在异常， 或者adapter非AbsRecyclerAdapter子类", Toast.LENGTH_LONG).show();
                            }
                        } else {

                            Toast.makeText(getContext(), "请返回正确的RecyclerView!", Toast.LENGTH_LONG).show();
                        }

                    }

                }
            };

            for (int i = 0; i < count; i++) {

                View view = getChildAt(i);

                if (view instanceof ViewGroup && ((ViewGroup) view).getChildCount() > 1) {


                    for (int j = 0; j < ((ViewGroup) view).getChildCount(); j++) {

                        ((ViewGroup) view).getChildAt(j).setOnClickListener(onClickListener);


                    }


                } else {

                    getChildAt(i).setOnClickListener(onClickListener);


                }


            }

        }

//        SwipeMenuBridge menuBridge = new SwipeMenuBridge(mDirection, index, mSwipeSwitch, parent);
//        parent.setTag(menuBridge);

//        if (item.getImage() != null) {
//            ImageView iv = createIcon(item);
////            menuBridge.mImageView = iv;
//            parent.addView(iv);
//        }

//        if (!TextUtils.isEmpty(item.getText())) {
//            TextView tv = createTitle(item);
////            menuBridge.mTextView = tv;
//            parent.addView(tv);
//        }
    }

//    private ImageView createIcon(SwipeMenuItem item) {
//        ImageView imageView = new ImageView(getContext());
//        imageView.setImageDrawable(item.getImage());
//        return imageView;
//    }

//    private TextView createTitle(SwipeMenuItem item) {
//        TextView textView = new TextView(getContext());
//        textView.setText(item.getText());
//        textView.setGravity(Gravity.CENTER);
//        int textSize = item.getTextSize();
//        if (textSize > 0)
//            textView.setTextSize(textSize);
//        ColorStateList textColor = item.getTitleColor();
//        if (textColor != null)
//            textView.setTextColor(textColor);
//        int textAppearance = item.getTextAppearance();
//        if (textAppearance != 0)
//            TextViewCompat.setTextAppearance(textView, textAppearance);
//        Typeface typeface = item.getTextTypeface();
//        if (typeface != null)
//            textView.setTypeface(typeface);
//        return textView;
//    }

}
