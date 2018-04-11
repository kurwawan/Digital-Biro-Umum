package com.example.asus.d_biumsemarang1.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.d_biumsemarang1.DetailActivity;
import com.example.asus.d_biumsemarang1.MainActivity;
import com.example.asus.d_biumsemarang1.R;
import com.example.asus.d_biumsemarang1.adapter.GedungVenueAdapter;
import com.example.asus.d_biumsemarang1.data.GedungVenue;
import com.example.asus.d_biumsemarang1.listener.AppBarListener;
import com.example.asus.d_biumsemarang1.listener.DetailToFragmentModel;
import com.example.asus.d_biumsemarang1.listener.ViewHolderListener;
import com.example.asus.d_biumsemarang1.utils.ConnectionUtils;
import com.example.asus.d_biumsemarang1.utils.ExtendsSwipeRefresh;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;


/**
 * Created by ASUS on 11/2/2017.
 */

public class GedungFragment extends Fragment implements ViewHolderListener, View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = GedungFragment.class.getSimpleName();

    private RecyclerView mRecycleView;
    private Button mBtnCoba1;
    private ImageView mImage1;
    private TextView mKeterangan1;
    private LinearLayout mConnectionLinear1;
    private ProgressBar mProgress1;
    private LinearLayoutManager mLayoutManager;
    private ExtendsSwipeRefresh mRefresh;
    private GedungVenueAdapter mAdapter;
    private BroadcastReceiver mReceiver;
    private BroadcastReceiver mUpdate;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private ValueEventListener mValueListener;

    private boolean isConnected = false;
    private boolean isDestroyed = true;
    private static boolean isFinished = true;
    private static boolean isRefresh = true;
    private boolean needRestore = true;
    private static boolean isSetupFinished = true;
    private static boolean needLoading = false;
    private static boolean isListenerAttached = false;
    private static String mKeyLastItem;
    private static List<GedungVenue> mData;
    private static Parcelable mParcel;
    private Context mContext;
    private AppBarListener mAppBarListener;
    private SharedPreferences mPreference;
    private boolean needClearStatic = false;
    private int clickPosition;
    private String mLastClickKey;
    private boolean needNotify = false;



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        isDestroyed = false;
        mContext = context;
        mAppBarListener = (AppBarListener) getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                isConnected = ConnectionUtils.isOnline(mContext);
                if (!isFinished && !isConnected) {
                    showProgress1(false);
                    mRefresh.setRefreshing(false);
                    if (!isRefresh && mData.get(mData.size() - 1) == null) {
                        mAdapter.setNeedLoading(false);
                        mAdapter.notifyItemChanged(mData.size() - 1);
                    } else if (mData == null) {
                        showInfoResult(true, true);
                    } else if (mData.size() == 0) {
                        showInfoResult(true, false);
                    }
                    detachListener();
                } else if (isConnected) {
                    mDatabase.purgeOutstandingWrites();
                    if (isRefresh && !isFinished) {
                        if (mData == null) {
                            showProgress1(true);
                            showInfoResult(false, false);
                        } else if (mData.size() == 0) {
                            showProgress1(true);
                            showInfoResult(false, false);
                        } else {
                            mRefresh.setRefreshing(true);
                        }

                    } else if (!isFinished) {
                        mAdapter.setNeedLoading(true);
                        mAdapter.notifyItemChanged(mData.size() - 1);

                    }
                }
            }
        };
        mUpdate = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getStringExtra(DetailActivity.KODE_INF).equals(MainActivity.KODE_GEDUNG) && isSetupFinished) {
                    Gson gson = new Gson();
                    String data = intent.getStringExtra(DetailActivity.DATA_DATABASE);
                    GedungVenue newData = gson.fromJson(data, new TypeToken<GedungVenue>() {
                    }.getType());
                    clickPosition = DetailToFragmentModel.getInstance().getClickPosition();
                    Log.v(TAG,"" + clickPosition);
                    if(clickPosition <= mData.size() -1){
                        mLastClickKey = mPreference.getString(MainActivity.CLICK_POSITION_FRAGMENT, "");
                        if(mData.get(clickPosition).getKey().equals(mLastClickKey)){
                            if(newData != null){
                                mData.set(clickPosition, newData);
                                mAdapter.notifyItemChanged(clickPosition);
                            }
                            else {
                                mData.remove(clickPosition);
                                if(isResumed()){
                                    mAdapter.notifyItemRemoved(clickPosition);
                                }
                                else {
                                    needNotify = true;
                                }
                            }
                        }
                    }
                    Log.v(TAG,"RECEIVED");
                }
            }
        };
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference().child("gedung");
        mPreference = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (getArguments() != null) {
            needClearStatic = getArguments().getBoolean(MainActivity.FROM_LOGIN);
        }
        if (needClearStatic) {
            clearStaticVariable();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mPreference.registerOnSharedPreferenceChangeListener(this);
        View content = inflater.inflate(R.layout.fragment_gedung_venue, container, false);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(mReceiver, filter);
        IntentFilter filter1 = new IntentFilter(GedungVenueAdapter.NOTIFIY_ADAPTER);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mUpdate, filter1);
        initView(content);
        initListener();
        mAppBarListener.disableScroll();
        mRecycleView.setVisibility(View.GONE);
        isConnected = ConnectionUtils.isOnline(mContext);
        showProgress1(true);
        mPreference.registerOnSharedPreferenceChangeListener(this);
        if (mData == null && isConnected) {
            showInfoResult(false, true);
            if (!isListenerAttached) {
                attachListener();
            }
        } else if (mData == null) {
            showProgress1(false);
            showInfoResult(true, true);
        } else {
            setUpRecycleView();
        }
        if (mParcel != null) {
            mLayoutManager.onRestoreInstanceState(mParcel);
        }

        return content;
    }


    @Override
    public void onResume() {
        super.onResume();
        if(needNotify){
            needNotify = false;
            mAdapter.notifyItemRemoved(clickPosition);
            if(mData.size() == 0){
                showInfoResult(true, false);
                mRefresh.setEnabled(false);
                if(mAppBarListener != null){
                    mAppBarListener.disableScroll();
                    mAppBarListener.expandAppBar();
                }
            }
        }
        Log.v(TAG,"onResume");
    }

    @Override
    public void onStop() {
        super.onStop();
        mParcel = mLayoutManager.onSaveInstanceState();
        Log.v(TAG,"onStop");


    }

    private void attachListener() {
        isSetupFinished = false;
        isFinished = false;
        isListenerAttached = true;
        mValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                processingDatabase(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                processingDatabase(null);
                if (!isDestroyed) {
                    String message = mContext.getString(R.string.fail_fetchData);
                    Toasty.error(mContext, message, Toast.LENGTH_SHORT).show();
                }

            }
        };

        if (isRefresh || mKeyLastItem == null) {
            mReference.orderByKey().limitToFirst(8).addListenerForSingleValueEvent(mValueListener);
        } else {
            mReference.orderByKey().startAt(mKeyLastItem).limitToFirst(9).addListenerForSingleValueEvent(mValueListener);
        }


    }

    private void detachListener() {
        if (mValueListener != null) {
            mReference.removeEventListener(mValueListener);
            mValueListener = null;
        }
    }

    private void initView(View view) {
        mRecycleView = view.findViewById(R.id.mRecycleView);
        mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false){
            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
                LinearSmoothScroller smoothScroller = new LinearSmoothScroller(mContext){
                    @Nullable
                    @Override
                    public PointF computeScrollVectorForPosition(int targetPosition) {
                        return new PointF(0, -1);
                    }
                    @Override
                    protected float calculateSpeedPerPixel
                            (DisplayMetrics displayMetrics) {
                        return MainActivity.MILLISECONDS_PER_INCH/displayMetrics.densityDpi;
                    }
                };
                smoothScroller.setTargetPosition(position);
                startSmoothScroll(smoothScroller);
            }
        };
        mRecycleView.setLayoutManager(mLayoutManager);
        mRecycleView.setHasFixedSize(true);

        mBtnCoba1 = view.findViewById(R.id.mBtnCoba1);
        mImage1 = view.findViewById(R.id.mImage1);
        mKeterangan1 = view.findViewById(R.id.mKeterangan1);
        mConnectionLinear1 = view.findViewById(R.id.mConnectionLinear1);
        mProgress1 = view.findViewById(R.id.mProgress1);
        mProgress1.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(mContext, R.color.colorProgress), PorterDuff.Mode.SRC_ATOP);
        mRefresh = view.findViewById(R.id.mRefresh);
        mRefresh.setEnabled(false);
        mRefresh.setColorSchemeColors(ContextCompat.getColor(mContext, R.color.colorProgress),
                ContextCompat.getColor(mContext, R.color.colorPrimary), ContextCompat.getColor(mContext, R.color.colorPrimaryDark));

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext.unregisterReceiver(mReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mUpdate);
        isDestroyed = true;
        mAppBarListener = null;
        DetailToFragmentModel.getInstance().clearmData();
        mPreference.unregisterOnSharedPreferenceChangeListener(this);
        Log.v(TAG,"onDestroy");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v(TAG,"onStart");

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void showInfoResult(boolean value, boolean connection) {
        if (value) {
            mConnectionLinear1.setVisibility(View.VISIBLE);
            if (connection) {
                mImage1.setImageResource(R.drawable.no_signal);
                mKeterangan1.setText(mContext.getString(R.string.check_connection));
            } else {
                String gedung = mContext.getString(R.string.gedung);
                mKeterangan1.setText(mContext.getString(R.string.no_result, gedung));
                mImage1.setImageResource(R.drawable.gedung);
            }
        } else {
            mConnectionLinear1.setVisibility(View.GONE);
        }
    }

    private void showProgress1(boolean value) {
        if (value) {
            mProgress1.setVisibility(View.VISIBLE);
        } else {
            mProgress1.setVisibility(View.GONE);
        }
    }

    private void tryAgain() {
        showProgress1(true);
        if (isConnected && isSetupFinished) {
            isRefresh = true;
            showInfoResult(false, true);
            attachListener();
        } else if (isConnected) {
            showInfoResult(false, true);
        } else {
            showProgress1(false);
        }


    }

    private void initListener() {
        mRecycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) //check for scroll down
                {
                    int itemCount = mLayoutManager.getItemCount();
                    int lastpos = mLayoutManager.findLastVisibleItemPosition();
                    if (needLoading && itemCount <= lastpos + 5) {
                        if (isSetupFinished) {
                            needLoading = false;
                            mData.add(null);
                            mAdapter.notifyItemInserted(mData.size() - 1);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    fetchMoreData();
                                }
                            }, 100);
                        }
                    }
                }
            }
        });
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isConnected && isSetupFinished) {
                    isRefresh = true;
                    attachListener();
                } else {
                    mRefresh.setRefreshing(false);
                }
            }
        });
        mBtnCoba1.setOnClickListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    private void setUpRecycleView() {
        showProgress1(false);
        mRefresh.setRefreshing(false);
        int size = mData.size();
        if (size == 0) {
            mRefresh.setEnabled(false);
            if(mAppBarListener != null){
                mAppBarListener.disableScroll();
                mAppBarListener.expandAppBar();
            }
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
            showInfoResult(true, false);
        } else {
            mRefresh.setEnabled(true);
            if(mAppBarListener != null){
                mAppBarListener.enableScroll();
            }
            showInfoResult(false, false);
            mRecycleView.setVisibility(View.VISIBLE);
        }
        if (mAdapter == null) {
            mAdapter = new GedungVenueAdapter(mData, this);
            mAdapter.setNeedLoading(needLoading);
            mRecycleView.setAdapter(mAdapter);
        } else if (size != 0) {
            mAdapter.setNeedLoading(needLoading);
            mAdapter.notifyDataSetChanged();
        }
        if (isRefresh && mData.size() != 0) {
            mRecycleView.scrollToPosition(0);
        }
    }

    private void saveDatafromDatabase(DataSnapshot dataSnapshot) {
        if (mData == null) {
            mData = new ArrayList<>();
        }
        if (isRefresh) {
            mData.clear();
            mKeyLastItem = null;
        }
        needLoading = false;
        if (dataSnapshot != null) {
            for (DataSnapshot data : dataSnapshot.getChildren()) {
                GedungVenue value = data.getValue(GedungVenue.class);
                value.setKey(data.getKey());
                mData.add(value);
            }
            int count = (int) dataSnapshot.getChildrenCount();
            if (!isRefresh) {
                if (count == 9) {
                    needLoading = true;
                }
                int pos = mData.size() - count - 1;
                if (mData.get(pos) == null) {
                    mData.remove(pos);
                }
                if (count != 0) {
                    mData.remove(pos);
                }
            } else if (count == 8 && mKeyLastItem == null) {
                needLoading = true;
            }
            if (count != 0) {
                mKeyLastItem = mData.get(mData.size() - 1).getKey();
            }
            proceedToFinal();
        } else {
            if (!isRefresh) {
                int pos = mData.size() - 1;
                if (mData.get(pos) == null) {
                    mData.remove(pos);
                }
            }
            proceedToFinal();

        }
    }

    @Override
    public void fetchMoreData() {
        if (isConnected && isSetupFinished) {
            isRefresh = false;
            attachListener();
        } else if (!isConnected || isRefresh) {
            mAdapter.setNeedLoading(false);
            mAdapter.notifyItemChanged(mData.size() - 1);
        }
    }

    @Override
    public void onClick(int position) {
        mPreference.edit().putString(MainActivity.CLICK_POSITION_FRAGMENT, mData.get(position).getKey()).apply();
        DetailToFragmentModel.getInstance().setSetupFinished(isSetupFinished);
        DetailToFragmentModel.getInstance().setmData(mData);
        Gson gson = new Gson();
        String data = gson.toJson(mData.get(position));
        Intent intent = new Intent(mContext, DetailActivity.class);
        intent.putExtra(DetailActivity.DATA_DATABASE, data);
        intent.setAction(MainActivity.KODE_GEDUNG);
        startActivity(intent);

    }


    private void processingDatabase(DataSnapshot dataSnapshot) {
        isFinished = true;
        detachListener();
        saveDatafromDatabase(dataSnapshot);
        isSetupFinished = true;
    }

    @Override
    public void onClick(View view) {
        tryAgain();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(GedungVenueAdapter.NOTIFIY_ADAPTER) &&
                mPreference.getString(key, "").equals(MainActivity.KODE_GEDUNG)) {
            setUpRecycleView();
        }

    }

    public void smoothScrool() {
        if (mAppBarListener != null) {
            if ((mAdapter != null && mRecycleView.canScrollVertically(-1)) || !mAppBarListener.isExpanded()) {
                mRecycleView.smoothScrollToPosition(mLayoutManager.findLastVisibleItemPosition() + 5 );
                if (mAppBarListener != null) {
                    mAppBarListener.expandAppBar();
                }

            }
        }


    }

    private void clearStaticVariable() {
        isFinished = true;
        isRefresh = true;
        needLoading = false;
        mParcel = null;
        isSetupFinished = true;
        isListenerAttached = false;
        mKeyLastItem = null;
        mData = null;
    }

    private void proceedToFinal(){
        if (!isDestroyed) {
            setUpRecycleView();
            DetailToFragmentModel.getInstance().setmData(mData);
            DetailToFragmentModel.getInstance().setSetupFinished(true);
        } else {
            mPreference.edit().putString(GedungVenueAdapter.NOTIFIY_ADAPTER, "").apply();
            mPreference.edit().putString(GedungVenueAdapter.NOTIFIY_ADAPTER, MainActivity.KODE_GEDUNG).apply();
        }
    }


}
