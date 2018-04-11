package com.example.asus.d_biumsemarang1;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.d_biumsemarang1.adapter.PayAdapter;
import com.example.asus.d_biumsemarang1.data.Bank;
import com.example.asus.d_biumsemarang1.listener.PayHolderListener;
import com.example.asus.d_biumsemarang1.utils.ConnectionUtils;
import com.example.asus.d_biumsemarang1.utils.DialogUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class PayActivity extends AppCompatActivity implements PayHolderListener, View.OnClickListener {
    public static final String TAG = PayActivity.class.getSimpleName();

    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecycleView;
    private ProgressBar mProgress1;
    private LinearLayout mConnectionLinear1;
    private ImageView mImage1;
    private TextView mKeterangan1;
    private Button mBtnCoba1;
    private PayAdapter mAdapter;
    private BroadcastReceiver mReceiver;
    private Toolbar mToolbar;
    private Toast mToast;
    private ImageView mImgTop;
    private AlertDialog mDialog;
    private ValueAnimator mAnimArrow;


    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private ValueEventListener mValueListener;

    private boolean isDestroyed = false;
    private boolean isConnected = false;
    private boolean isFinished = true;
    private boolean isVisible = false;
    private int mBottomMargin;
    private List<Bank> mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                isConnected = ConnectionUtils.isOnline(PayActivity.this);
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
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);
        initFirebase();
        initView();
        initListener();
        mRecycleView.setVisibility(View.GONE);
        isConnected = ConnectionUtils.isOnline(this);
        showProgress1(true);
        if (isConnected) {
            showInfoResult(false, true);
            attachListener();
        } else {
            showProgress1(false);
            showInfoResult(true, true);
        }
    }

    private void initView() {
        mToolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRecycleView = findViewById(R.id.mRecycleView);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) {
            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
                LinearSmoothScroller smoothScroller = new LinearSmoothScroller(PayActivity.this) {
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
        mImgTop = findViewById(R.id.mImgTop);
        mImgTop.setVisibility(View.INVISIBLE);
        mBtnCoba1 = findViewById(R.id.mBtnCoba1);
        mImage1 = findViewById(R.id.mImage1);
        mKeterangan1 = findViewById(R.id.mKeterangan1);
        mConnectionLinear1 = findViewById(R.id.mConnectionLinear1);
        mProgress1 = findViewById(R.id.mProgress1);
        //maybeInitAnimation();
        mProgress1.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, R.color.colorProgress), PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        isDestroyed = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.info:
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                mDialog = DialogUtils.buildMessageDialog(this, R.string.information, R.string.call_admin);
                mDialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initFirebase() {
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference().child("pembayaran");
    }

    private void maybeInitAnimation() {
        if (mBottomMargin == 0) {
            mBottomMargin = ((CoordinatorLayout.LayoutParams)mImgTop.getLayoutParams()).bottomMargin;
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

    private void showInfoResult(boolean value, boolean connection) {
        if (value) {
            mConnectionLinear1.setVisibility(View.VISIBLE);
            if (connection) {
                mImage1.setImageResource(R.drawable.no_signal);
                mKeterangan1.setText(getString(R.string.check_connection));
            } else {
                mKeterangan1.setText(getString(R.string.no_bank));
                mImage1.setImageResource(R.drawable.card_pay);
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
                    String message = getString(R.string.fail_fetchData);
                    Toasty.error(PayActivity.this, message, Toast.LENGTH_SHORT).show();
                }

            }
        };
        mReference.orderByChild("nama").addListenerForSingleValueEvent(mValueListener);


    }

    private void detachListener() {
        if (mValueListener != null) {
            mReference.removeEventListener(mValueListener);
            mValueListener = null;
        }
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
        mAdapter = new PayAdapter(mData, this);
        mRecycleView.setAdapter(mAdapter);
    }

    private void saveDatafromDatabase(DataSnapshot dataSnapshot) {
        if (mData == null) {
            mData = new ArrayList<>();
        }
        if (dataSnapshot != null) {
            for (DataSnapshot data : dataSnapshot.getChildren()) {
                Bank value = data.getValue(Bank.class);
                mData.add(value);
            }
        }
        setUpRecycleView();
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

    @Override
    public void onClickDetail(int position) {
        if(mDialog != null){
            mDialog.dismiss();
        }
        mDialog = DialogUtils.buildMessageDialog(this, mData.get(position).getNama() ,
                getString(R.string.dialog_bank, mData.get(position).getNo_rekening(),
                        mData.get(position).getAtas_nama()));
        mDialog.show();
    }

    @Override
    public void onClickLink(int position) {
        if (mData.get(position).getLink() == null) {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toasty.warning(this, getString(R.string.no_link), Toast.LENGTH_SHORT);
            mToast.show();
            return;
        }
        Uri webpage = Uri.parse(mData.get(position).getLink());

        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toasty.error(this, getString(R.string.no_browser), Toast.LENGTH_SHORT);
            mToast.show();
        }
    }
}
