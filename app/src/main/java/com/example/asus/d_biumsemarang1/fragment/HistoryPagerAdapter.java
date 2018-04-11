package com.example.asus.d_biumsemarang1.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.asus.d_biumsemarang1.MainActivity;

/**
 * Created by ASUS on 11/18/2017.
 */

public class HistoryPagerAdapter extends FragmentStatePagerAdapter {

    private GedungHistoryFragment fragment1;
    private VenueHistoryFragment fragment2;
    private boolean needClearStatic[] = new boolean[2];

    public HistoryPagerAdapter(FragmentManager fm, boolean needClearStatic) {
        super(fm);
        this.needClearStatic[0] = needClearStatic;
        this.needClearStatic[1] = needClearStatic;

    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(MainActivity.FROM_LOGIN, needClearStatic[position]);
        needClearStatic[position] = false;
        if (position == 0) {
            fragment1 = new GedungHistoryFragment();
            fragment1.setArguments(bundle);
            return fragment1;
        } else {
            fragment2 = new VenueHistoryFragment();
            fragment2.setArguments(bundle);
            return fragment2;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String title;
        if (position == 0) {
            title = "gedung";
        } else {
            title = "venue";
        }
        return title;
    }

    public void reloadCurrentFragment(int position) {
        if (position == 0) {
            fragment1.checkForReload();
        } else {
            fragment2.checkForReload();
        }
    }

    public Fragment getFragmentByPosition(int position){
        if(position == 0){
            return fragment1;
        }
        else {
            return  fragment2;
        }
    }
}
