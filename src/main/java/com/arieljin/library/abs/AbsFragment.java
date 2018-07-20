package com.arieljin.library.abs;

import android.support.v4.app.Fragment;

import com.arieljin.library.utils.DestoryUtil;

public class AbsFragment extends Fragment {


    @Override
    public void onDestroy() {
        super.onDestroy();
        DestoryUtil.onDestory(this);
    }
}
