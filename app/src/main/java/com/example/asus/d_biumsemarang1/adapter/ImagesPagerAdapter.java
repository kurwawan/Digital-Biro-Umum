package com.example.asus.d_biumsemarang1.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.asus.d_biumsemarang1.fragment.ImageFragment;

import java.util.List;

/**
 * Created by ASUS on 11/7/2017.
 */

public class ImagesPagerAdapter extends FragmentPagerAdapter {

    private List<String> mGambar;

    public ImagesPagerAdapter(FragmentManager fm, List<String> gambar) {
        super(fm);
        mGambar = gambar;
    }

    @Override
    public Fragment getItem(int position) {
        if (mGambar == null) {
            return ImageFragment.newInstance(null);
        } else {
            return ImageFragment.newInstance(mGambar.get(position));
        }

    }

    @Override
    public int getCount() {
        return 3;
    }
}
