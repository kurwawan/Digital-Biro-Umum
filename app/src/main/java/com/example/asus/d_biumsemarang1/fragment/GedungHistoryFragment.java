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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.NestedScrollView;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.d_biumsemarang1.DetailActivity;
import com.example.asus.d_biumsemarang1.DetailSewaActivity;
import com.example.asus.d_biumsemarang1.MainActivity;
import com.example.asus.d_biumsemarang1.R;
import com.example.asus.d_biumsemarang1.adapter.HistoryAdapter;
import com.example.asus.d_biumsemarang1.data.Sewa;
import com.example.asus.d_biumsemarang1.listener.AppBarListener;
import com.example.asus.d_biumsemarang1.listener.DetailSewaToFragmentModel;
import com.example.asus.d_biumsemarang1.listener.ViewHolderListener;
import com.example.asus.d_biumsemarang1.utils.ConnectionUtils;
import com.example.asus.d_biumsemarang1.utils.ExtendsSwipeRefresh;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.dmoral.toasty.Toasty;

/**
 * Created by ASUS on 11/18/2017.
 */

public class GedungHistoryFragment extends Fragment implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener, ViewHolderListener {
    public static final String TAG = GedungHistoryFragment.class.getSimpleName();

    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecycleView;
    private ProgressBar mProgress1;
    private NestedScrollView mConnectionNested1;
    private ExtendsSwipeRefresh mRefresh;
    private ImageView mImage1;
    private TextView mKeterangan1;
    private Button mBtnCoba1;
    private Context mContext;
    private HistoryAdapter mAdapter;
    private BroadcastReceiver mReceiver;
    private BroadcastReceiver mUpdate;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private ValueEventListener mEventListener;

    private String mUid;
    private String mLastClickKey;
    private SharedPreferences mPreference;
    private boolean isVisible = false;
    private boolean isFirstTime = true;
    private boolean needNotify = false;
    private boolean isConnected;
    private boolean needRestore = true;
    private static boolean isFinished = true;
    private boolean isDestroyed = true;
    private static boolean isRefresh = true;
    private static boolean needLoading = false;
    private static Parcelable mParcel;
    private boolean needReload = false;
    private int clickPosition;
    private static boolean isSetupFinished = true;
    private static boolean isListenerAttached = false;
    private static String mKeyLastItem;
    private static List<Sewa> mData;
    private boolean needClearStatic = false;
    private AppBarListener mAppBarListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        isDestroyed = false;
        mContext = context;
        if (isVisible && mReceiver == null) {
            registerReceiver();
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            mUid = mAuth.getCurrentUser().getUid();
        }
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference("riwayat_gedung");
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.fragment_gedungvenuehistory, container, false);
        initView(content);
        initListener();
        showInfoResult(false, false);
        showProgress1(false);
        if (isFirstTime && isVisible) {
            isFirstTime = false;
            proceedForFirstTime();
        }
        return content;
    }


    private void initView(View view) {
        mRefresh = view.findViewById(R.id.mRefresh);
        mRefresh.setColorSchemeColors(ContextCompat.getColor(mContext, R.color.colorProgress),
                ContextCompat.getColor(mContext, R.color.colorPrimary), ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
        mRefresh.setEnabled(false);
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
        mRecycleView.setVisibility(View.GONE);
        mRecycleView.setHasFixedSize(true);
        mBtnCoba1 = view.findViewById(R.id.mBtnCoba1);
        mImage1 = view.findViewById(R.id.mImage1);
        mKeterangan1 = view.findViewById(R.id.mKeterangan1);
        mConnectionNested1 = view.findViewById(R.id.mConnectionNested1);
        mProgress1 = view.findViewById(R.id.mProgress1);
        mProgress1.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(mContext, R.color.colorProgress), PorterDuff.Mode.SRC_ATOP);


    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.v(TAG, "isVisible : " + isVisibleToUser);
        isVisible = isVisibleToUser;
        if (isVisible) {
            if (getView() != null) {
                if (isFirstTime) {
                    registerReceiver();
                    isFirstTime = false;
                    proceedForFirstTime();
                }
                checkForReload();

            }
        }
        else {
            DetailSewaToFragmentModel.getInstance().clearmData();
        }


    }

    private void tryAgain() {
        showProgress1(true);
        if (isConnected && isSetupFinished) {
            isRefresh = true;
            showInfoResult(false, true);
            attachListener();
        } else if (isConnected && !isFinished) {
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


        /*mRefresh.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                refresh();
            }
        });*/
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        mBtnCoba1.setOnClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
        isDestroyed = true;
        DetailSewaToFragmentModel.getInstance().clearmData();
        //LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mUpdate);
        mPreference.unregisterOnSharedPreferenceChangeListener(this);
    }

    private void showInfoResult(boolean value, boolean connection) {
        if (value) {
            mConnectionNested1.setVisibility(View.VISIBLE);
            if (connection) {
                mImage1.setImageResource(R.drawable.no_signal);
                mKeterangan1.setText(mContext.getString(R.string.check_connection));
            } else {
                String message = mContext.getString(R.string.no_history, mContext.getString(R.string.gedung));
                mKeterangan1.setText(message);
                mImage1.setImageResource(R.drawable.history);
            }
        } else {
            mConnectionNested1.setVisibility(View.GONE);
        }
    }

    private void showProgress1(boolean value) {
        if (value) {
            mProgress1.setVisibility(View.VISIBLE);
        } else {
            mProgress1.setVisibility(View.GONE);
        }
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
                mRecycleView.setVisibility(View.GONE);
            }
        }
        if (isVisible) {
            checkForReload();

        }


    }


    public void checkForReload() {
        needReload = ((HistoryFragment) getParentFragment()).isNeedReload(0);
        Log.v(TAG,"TES " + needReload);
        if (needReload) {
            needReload = false;
            isConnected = ConnectionUtils.isOnline(mContext);
            if (mData == null) {
                tryAgain();
            } else if (mData.size() == 0) {
                tryAgain();
            } else {
                mRefresh.setRefreshing(true);
                refresh();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        mParcel = mLayoutManager.onSaveInstanceState();
    }

    private void registerReceiver() {
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
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(mReceiver, filter);
        mAppBarListener = (AppBarListener) getContext();
        mUpdate = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getStringExtra(DetailActivity.KODE_INF).equals(MainActivity.KODE_GEDUNG) && isSetupFinished) {
                    clickPosition = DetailSewaToFragmentModel.getInstance().getClickPosition();
                    Log.v(TAG,"" + clickPosition);
                    if (mData != null) {
                        if (clickPosition <= mData.size() - 1) {
                            mLastClickKey = mPreference.getString(MainActivity.CLICK_POSITION_FRAGMENT, "");
                            if (mData.get(clickPosition).getKey().equals(mLastClickKey)) {
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
        IntentFilter filter1 = new IntentFilter(HistoryAdapter.NOTIFIY_ADAPTER);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mUpdate, filter1);

    }

    private void unregisterReceiver() {
        try {
            if (mReceiver != null) {
                mContext.unregisterReceiver(mReceiver);
                mReceiver = null;
            }
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "mReceiver is already unregistered");
            mReceiver = null;
        }
        mAppBarListener = null;
        try {
            if (mUpdate != null) {
                LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mUpdate);
                mUpdate = null;
            }
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "mUpdate is already unregistered");
            mUpdate = null;
        }
    }

    private void proceedForFirstTime() {
        showProgress1(true);
        mPreference.registerOnSharedPreferenceChangeListener(this);
        isConnected = ConnectionUtils.isOnline(mContext);
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

    }

    private void attachListener() {
        isSetupFinished = false;
        isFinished = false;
        isListenerAttached = true;
        mEventListener = new ValueEventListener() {
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
            mReference.orderByChild("uid").equalTo(mUid).limitToLast(8).addListenerForSingleValueEvent(mEventListener);
        } else {
            mReference.orderByChild("uid").startAt(mUid).endAt(mUid, mKeyLastItem).limitToLast(9).addListenerForSingleValueEvent(mEventListener);
        }


    }

    private void detachListener() {
        if (mEventListener != null) {
            mReference.removeEventListener(mEventListener);
            mEventListener = null;
        }
    }

    @Override
    public void onClick(View view) {
        tryAgain();
    }

    private void processingDatabase(DataSnapshot dataSnapshot) {
        isFinished = true;
        detachListener();
        saveDatafromDatabase(dataSnapshot);
        isSetupFinished = true;
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
            List<Sewa> temp = new ArrayList<>();
            for (DataSnapshot data : dataSnapshot.getChildren()) {
                Sewa value = data.getValue(Sewa.class);
                value.setKey(data.getKey());
                temp.add(value);
            }
            Collections.reverse(temp);
            mData.addAll(temp);
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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(HistoryAdapter.NOTIFIY_ADAPTER) &&
                mPreference.getString(key, "").equals(MainActivity.KODE_GEDUNG)) {
            setUpRecycleView();
        }
    }

    private void setUpRecycleView() {
        showProgress1(false);
        mRefresh.setRefreshing(false);
        int size = mData.size();
        if (size == 0) {
            mRefresh.setEnabled(false);
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
            showInfoResult(true, false);
            mRecycleView.setVisibility(View.GONE);
        } else {
            mRefresh.setEnabled(true);
            showInfoResult(false, false);
            mRecycleView.setVisibility(View.VISIBLE);
        }
        if (mAdapter == null) {
            mAdapter = new HistoryAdapter(mData, this);
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
        DetailSewaToFragmentModel.getInstance().setmData(mData);
        DetailSewaToFragmentModel.getInstance().setSetupFinished(isSetupFinished);
        Gson gson = new Gson();
        String data = gson.toJson(mData.get(position));
        Intent intent = new Intent(mContext, DetailSewaActivity.class);
        intent.setAction(MainActivity.KODE_GEDUNG);
        intent.putExtra(DetailActivity.DATA_DATABASE, data);
        startActivity(intent);
    }

    private void refresh() {
        if (isConnected && isSetupFinished) {
            isRefresh = true;
            attachListener();
        } else if(!isConnected || !isRefresh){
            mRefresh.setRefreshing(false);
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
    public void smoothScrool() {
        if (mAppBarListener != null) {
            if ((mAdapter != null &&  mRecycleView.canScrollVertically(-1)) || !mAppBarListener.isExpanded()) {
                mRecycleView.smoothScrollToPosition(mLayoutManager.findLastVisibleItemPosition() + 5);
                if (mAppBarListener != null) {
                    mAppBarListener.expandAppBar();
                }

            }
        }
    }

    private void proceedToFinal(){
        if (!isDestroyed) {
            setUpRecycleView();
            if(isVisible){
                DetailSewaToFragmentModel.getInstance().setmData(mData);
                DetailSewaToFragmentModel.getInstance().setSetupFinished(true);
            }

        } else {
            mPreference.edit().putString(HistoryAdapter.NOTIFIY_ADAPTER, "").apply();
            mPreference.edit().putString(HistoryAdapter.NOTIFIY_ADAPTER, MainActivity.KODE_GEDUNG).apply();
        }
    }
}
