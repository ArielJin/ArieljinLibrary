package com.arieljin.library.widget.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.List;

/**
 * @time 2018/7/26.
 * @email ariel.jin@tom.com
 */
public class AbsFragmentPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragments;
    private FragmentManager fm;


    public AbsFragmentPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fm = fm;
        this.fragments = fragments;
    }

    public List<Fragment> getFragments() {
        return fragments;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        fragment = fragments.get(position);
//        Bundle bundle = new Bundle();
//        bundle.putString("id", "" + position);
//        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public int getCount() {
        return fragments == null ? 0 : fragments.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container,
                position);
        fm.beginTransaction().show(fragment).commit();
        return fragment;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        super.destroyItem(container, position, object);
        Fragment fragment = fragments.get(position);
        fm.beginTransaction().hide(fragment).commit();

    }
}
