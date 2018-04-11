package com.example.asus.d_biumsemarang1.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.asus.d_biumsemarang1.MainActivity;
import com.example.asus.d_biumsemarang1.R;
import com.example.asus.d_biumsemarang1.listener.AppBarListener;
import com.example.asus.d_biumsemarang1.listener.HistoryFragmentListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
public class HistoryFragment extends Fragment {

    public static final String KODE_POSITION = "kode_position";
    public static final String NEED_RELOAD = "need_reload";

    private Context mContext;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private HistoryPagerAdapter mAdapater;
    private AppBarListener mAppBarListener;
    private BroadcastReceiver mLocalReceiver;
    private HistoryFragmentListener mHistoryFragmentListener;

    private static int mLastPosition = 0;

    private int positionForReload = 0;
    private boolean needReload = false;
    private boolean needClearStatic = false;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            needClearStatic = getArguments().getBoolean(MainActivity.FROM_LOGIN, false);
            if (needClearStatic) {
                mLastPosition = 0;
            }
            positionForReload = getArguments().getInt(KODE_POSITION, mLastPosition);
            mLastPosition = positionForReload;
            needReload = getArguments().getBoolean(NEED_RELOAD, false);
        }

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View content = inflater.inflate(R.layout.fragment_history, container, false);
        initView(content);
        setUpViewPager();
        initListener();
        mAppBarListener.enableScroll();
        mLocalReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                positionForReload = intent.getIntExtra(KODE_POSITION, 0);
                needReload = intent.getBooleanExtra(NEED_RELOAD, false);
                if (mLastPosition == positionForReload && needReload) {
                    if (mLastPosition == 0) {
                        mAdapater.reloadCurrentFragment(0);
                    } else {
                        mAdapater.reloadCurrentFragment(1);
                    }
                } else if(mLastPosition != positionForReload) {
                    mViewPager.setCurrentItem(positionForReload);
                }
                Log.v("HistoryFragment", "" + mLastPosition);

            }
        };
        IntentFilter filter = new IntentFilter(MainActivity.KODE_RIWAYAT);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mLocalReceiver, filter);

        return content;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mAppBarListener = (AppBarListener) context;
        mHistoryFragmentListener = (HistoryFragmentListener) context;
        mContext = context;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void initView(View content) {
        mTabLayout = content.findViewById(R.id.mTabLayout);
        mViewPager = content.findViewById(R.id.mViewPager);

    }

    private void setUpViewPager() {
        mAdapater = new HistoryPagerAdapter(getChildFragmentManager(), needClearStatic);
        mViewPager.setAdapter(mAdapater);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.setCurrentItem(mLastPosition);
    }

    private void initListener() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mLastPosition = position;
                mHistoryFragmentListener.setHistoryFragmentPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                   Fragment fragment = mAdapater.getFragmentByPosition(tab.getPosition());
                   if(fragment != null) {
                       if (fragment instanceof GedungHistoryFragment) {
                           ((GedungHistoryFragment) fragment).smoothScrool();
                       } else {
                           ((VenueHistoryFragment) fragment).smoothScrool();
                       }
                   }
            }
        });
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mAppBarListener = null;
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mLocalReceiver);
    }

    public boolean isNeedReload(int pos) {
        if (needReload && positionForReload == pos) {
            needReload = false;
            return true;
        }
        return false;
    }
}
