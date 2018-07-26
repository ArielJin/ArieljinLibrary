package com.arieljin.library.fragment;

import android.os.Bundle;

import com.arieljin.library.abs.AbsFragment;

/**
 * @time 2018/7/26.
 * @email ariel.jin@tom.com
 */
public class PlaceholderFragment extends AbsFragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    public PlaceholderFragment() {
    }


    public static PlaceholderFragment newInstance(int sectionNumber) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }


}
