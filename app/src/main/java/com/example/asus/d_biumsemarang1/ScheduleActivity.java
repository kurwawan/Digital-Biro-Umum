package com.example.asus.d_biumsemarang1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.d_biumsemarang1.adapter.ScheduleAdapter;
import com.example.asus.d_biumsemarang1.data.JadwalSewa;
import com.example.asus.d_biumsemarang1.utils.ConnectionUtils;
import com.example.asus.d_biumsemarang1.utils.TimeUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class ScheduleActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView mRecycleView;
    private Toolbar mToolbar;
    private ProgressBar mProgress;
    private LinearLayout mConnectionLinear;
    private ImageView mImage;
    private TextView mKeterangan;
    private Button mBtnCoba;
    private BroadcastReceiver mReceiver;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private ValueEventListener mJadwalListener;

    private ScheduleAdapter mAdapter;
    private boolean isConnected;
    private boolean isCalled = true;
    private boolean isFinised = true;
    private String mType;
    private String mKey;
    private String mKode;
    private List<JadwalSewa> mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                isConnected = ConnectionUtils.isOnline(ScheduleActivity.this);
                if(!isFinised && !isConnected){
                    isFinised = true;
                    detachListener();
                    showProgress(false);
                    showInfoResult(true, true);
                }
            }
        };
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);
        initView();
        initListener();
        handleIntent();
        initFirebaseDatabase();
        showProgress(true);
        showInfoResult(false, false);
        isConnected = ConnectionUtils.isOnline(this);
        if(isConnected && isCalled){
            isFinised = false;
            isCalled = false;
            attachListener();
        }
        else {
            showProgress(false);
            showInfoResult(true, true);
        }
    }

    private void initView() {
        mRecycleView = findViewById(R.id.mRecycleView);
        mRecycleView.setVisibility(View.GONE);
        mToolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mProgress = findViewById(R.id.mProgress);
        mProgress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, R.color.colorProgress), PorterDuff.Mode.SRC_ATOP);
        mConnectionLinear = findViewById(R.id.mConnectionLinear);
        mImage = findViewById(R.id.mImage);
        mKeterangan = findViewById(R.id.mKeterangan);
        mBtnCoba = findViewById(R.id.mBtnCoba);
    }

    private void initListener() {
        mBtnCoba.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        showProgress(true);
        if(isConnected && isCalled){
            isFinised = false;
            isCalled = false;
            showInfoResult(false, false);
            attachListener();

        }
        else if(isConnected) {
            isFinised = false;
            showInfoResult(false, true);
        }
        else {
            showProgress(false);
        }

    }
    private void initFirebaseDatabase(){
        mDatabase = FirebaseDatabase.getInstance();
        String tipe;
        if(mKode.equals(MainActivity.KODE_GEDUNG)){
            tipe = "gedung";
        }
        else {
            tipe = "venue";
        }
        mReference = mDatabase.getReference("waktu/" + tipe).child(mKey);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private void handleIntent(){
        Intent intent = getIntent();
        mKode = intent.getAction();
        if(mKode.equals(MainActivity.KODE_GEDUNG)){
            mType = getString(R.string.gedung);
        }
        else {
            mType = getString(R.string.venue);
        }
        mKey = intent.getStringExtra(DetailActivity.KEY_DATABASE);
        getSupportActionBar().setTitle(getString(R.string.schedule_tipe, mType));
    }

    private void showProgress(boolean value){
        if(value){
            mProgress.setVisibility(View.VISIBLE);
        }else{
            mProgress.setVisibility(View.GONE);
        }
    }
    private void showInfoResult(boolean value, boolean connection){
        if(value){
            mConnectionLinear.setVisibility(View.VISIBLE);
            if(connection){
                mImage.setImageResource(R.drawable.no_signal);
                mKeterangan.setText(R.string.check_connection);
                mBtnCoba.setVisibility(View.VISIBLE);
            }else {
                mImage.setImageResource(R.drawable.schedule);
                mKeterangan.setText(getString(R.string.no_schedule, mType));
                mBtnCoba.setVisibility(View.GONE);
            }
        }
        else {
            mConnectionLinear.setVisibility(View.GONE);
        }
    }
    private void detachListener(){
        if(mJadwalListener != null){
            mReference.removeEventListener(mJadwalListener);
            mJadwalListener = null;
        }
    }

    private void attachListener(){
        mJadwalListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                isFinised = true;
                processingDatabase(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                processingDatabase(null);
                String message = ScheduleActivity.this.getString(R.string.fail_fetchData);
                Toasty.error(ScheduleActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        };
        long currentTime = TimeUtils.ubahKeFormatWIB(System.currentTimeMillis());
        mReference.orderByChild("waktu_kembali").startAt((double) currentTime).addListenerForSingleValueEvent(mJadwalListener);
    }

    private void processingDatabase(DataSnapshot snapshot){
        mData = new ArrayList<>();
        if(snapshot != null){
            for (DataSnapshot data : snapshot.getChildren()){
                long waktu_pinjam = data.child("waktu_pinjam").getValue(Long.class);
                long waktu_kembali = data.child("waktu_kembali").getValue(Long.class);
                mData.add(new JadwalSewa(waktu_pinjam,waktu_kembali));
            }
        }
        setUpRecycleView();
    }

    private void setUpRecycleView(){
        showProgress(false);
        if(mData.size() == 0){
            showInfoResult(true, false);
        }
        else {
            LinearLayoutManager lm = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false);
            mRecycleView.setLayoutManager(lm);
            mRecycleView.setHasFixedSize(true);
            mAdapter = new ScheduleAdapter(mData);
            mRecycleView.setAdapter(mAdapter);
            mRecycleView.setVisibility(View.VISIBLE);
        }
    }

}
