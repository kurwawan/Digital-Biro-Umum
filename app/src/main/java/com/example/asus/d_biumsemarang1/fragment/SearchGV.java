package com.example.asus.d_biumsemarang1.fragment;


import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.d_biumsemarang1.DetailActivity;
import com.example.asus.d_biumsemarang1.MainActivity;
import com.example.asus.d_biumsemarang1.R;
import com.example.asus.d_biumsemarang1.SearchActivity;
import com.example.asus.d_biumsemarang1.adapter.GedungVenueAdapter;
import com.example.asus.d_biumsemarang1.data.GedungVenue;
import com.example.asus.d_biumsemarang1.listener.ViewHolderListener;
import com.example.asus.d_biumsemarang1.utils.ConnectionUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

/**
 * Created by ASUS on 12/2/2017.
 */

public class SearchGV extends Fragment implements ViewHolderListener,View.OnClickListener {
    public static final String TAG = SearchGV.class.getSimpleName();

    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecycleView;
    private ProgressBar mProgress1;
    private LinearLayout mConnectionLinear1;
    private ImageView mImage1;
    private ImageView mImgTop;
    private TextView mKeterangan1;
    private Button mBtnCoba1;
    private GedungVenueAdapter mAdapter;
    private BroadcastReceiver mReceiver;
    private ValueAnimator mAnimArrow;


    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private ValueEventListener mValueListener;

    private boolean isConnected = false;
    private boolean isVisible = false;
    private boolean isFinished = true;
    private boolean isDestroyed = false;
    private List<GedungVenue> mData;
    private Context mContext;
    private String mQuery;
    private String mTitle;
    private String mDatabaseQuery;
    private int mBottomMargin;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
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
                    showInfoResult(true, true);
                    detachListener();
                } else if (isConnected && !isFinished) {
                    showProgress1(true);
                    showInfoResult(false, false);
                }
            }
        };
        if (getArguments() != null) {
            if (getArguments().getString(SearchActivity.SEARCH_TYPE, MainActivity.KODE_GEDUNG).equals(MainActivity.KODE_GEDUNG)) {
                mTitle = getString(R.string.gedung);
                mDatabaseQuery = "gedung";
            } else {
                mTitle = getString(R.string.venue);
                mDatabaseQuery = "venue";
            }
            mQuery = getArguments().getString(SearchActivity.SEARCH_QUERY, "");
        }
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference().child(mDatabaseQuery);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.fragment_searchgv, container, false);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(mReceiver, filter);
        initView(content);
        initListener();
        mRecycleView.setVisibility(View.GONE);
        isConnected = ConnectionUtils.isOnline(mContext);
        showProgress1(true);
        if (isConnected && mQuery.trim().length() > 0) {
            showInfoResult(false, true);
            attachListener();
        } else if (isConnected) {
            showProgress1(false);
            showNoQuery();
        } else {
            showProgress1(false);
            showInfoResult(true, true);
        }
        return content;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.v(TAG, "onStop");


    }


    private void attachListener() {
        isFinished = false;
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
        mReference.orderByChild("nama").startAt(mQuery)
                .endAt(mQuery + "\uf8ff").limitToFirst(50).addListenerForSingleValueEvent(mValueListener);




    }

    private void detachListener() {
        if (mValueListener != null) {
            mReference.removeEventListener(mValueListener);
            mValueListener = null;
        }
    }

    private void initView(View view) {
        mRecycleView = view.findViewById(R.id.mRecycleView);
        mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false) {
            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
                LinearSmoothScroller smoothScroller = new LinearSmoothScroller(mContext) {
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
        mImgTop = view.findViewById(R.id.mImgTop);
        mImgTop.setVisibility(View.INVISIBLE);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext.unregisterReceiver(mReceiver);
        isDestroyed = true;
        Log.v(TAG, "onDestroy");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v(TAG, "onStart");

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
                mKeterangan1.setText(mContext.getString(R.string.not_found, mTitle));
                if(mTitle.equals("Gedung")){
                    mImage1.setImageResource(R.drawable.gedung);

                }else {
                    mImage1.setImageResource(R.drawable.venue);
                }
                mBtnCoba1.setVisibility(View.GONE);
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
        if (isConnected && isFinished) {
            showInfoResult(false, true);
            attachListener();
        } else if (isConnected) {
            showInfoResult(false, true);
        } else {
            showProgress1(false);
        }


    }

    private void maybeInitAnimation() {
        if (mBottomMargin == 0) {
            mBottomMargin = ((FrameLayout.LayoutParams)mImgTop.getLayoutParams()).bottomMargin;
            mImgTop.setTranslationY(mImgTop.getHeight() + mBottomMargin);
            mImgTop.setVisibility(View.VISIBLE);
            mAnimArrow = ObjectAnimator.ofFloat(mImgTop, "translationY", mImgTop.getHeight() + mBottomMargin, 0);
            mAnimArrow.setDuration(100);
        }
    }
    private void initListener() {
        mBtnCoba1.setOnClickListener(this);
        mImgTop.setOnClickListener(this);
        mRecycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                Log.v(TAG, "CALLED");
                Log.v(TAG, "POS " + mLayoutManager.findFirstVisibleItemPosition());
                if(dy > 0 && !isVisible){
                    isVisible = true;
                    maybeInitAnimation();
                    mAnimArrow.start();
                }
                else{
                    if(isVisible && !mRecycleView.canScrollVertically( -1)){
                        isVisible = false;
                        mAnimArrow.reverse();
                    }
                }

            }

        });
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    private void setUpRecycleView() {
        showProgress1(false);
        int size = mData.size();
        if (size == 0) {
            showInfoResult(true, false);
        } else {
            showInfoResult(false, false);
            mRecycleView.setVisibility(View.VISIBLE);
        }
        mAdapter = new GedungVenueAdapter(mData, this);
        mRecycleView.setAdapter(mAdapter);
    }

    private void saveDatafromDatabase(DataSnapshot dataSnapshot) {
        if (mData == null) {
            mData = new ArrayList<>();
        }
        if (dataSnapshot != null) {
            for (DataSnapshot data : dataSnapshot.getChildren()) {
                GedungVenue value = data.getValue(GedungVenue.class);
                value.setKey(data.getKey());
                mData.add(value);
            }
        }
        setUpRecycleView();
    }

    @Override
    public void fetchMoreData() {

    }

    @Override
    public void onClick(int position) {
        PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                .putString(MainActivity.CLICK_POSITION_FRAGMENT, mData.get(position).getKey()).apply();
        Gson gson = new Gson();
        String data = gson.toJson(mData.get(position));
        Intent intent = new Intent(mContext, DetailActivity.class);
        intent.putExtra(DetailActivity.DATA_DATABASE, data);
        if(mTitle.equals("Gedung")){
            intent.setAction(MainActivity.KODE_GEDUNG);
        }
        else{
            intent.setAction(MainActivity.KODE_VENUE);
        }

        startActivity(intent);

    }


    private void processingDatabase(DataSnapshot dataSnapshot) {
        isFinished = true;
        detachListener();
        saveDatafromDatabase(dataSnapshot);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.mBtnCoba1) {
            tryAgain();
        } else {
            mRecycleView.smoothScrollToPosition(mLayoutManager.findLastVisibleItemPosition() + 5);
        }

    }

    private void showNoQuery() {
        mConnectionLinear1.setVisibility(View.VISIBLE);
        mKeterangan1.setText(mContext.getString(R.string.search_infMessage, mTitle));
        mImage1.setImageResource(R.drawable.search_v2);
        mBtnCoba1.setVisibility(View.GONE);
    }


}

