package com.example.asus.d_biumsemarang1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.d_biumsemarang1.adapter.GedungVenueAdapter;
import com.example.asus.d_biumsemarang1.adapter.ImagesPagerAdapter;
import com.example.asus.d_biumsemarang1.data.GedungVenue;
import com.example.asus.d_biumsemarang1.data.SukaGedungVenue;
import com.example.asus.d_biumsemarang1.listener.AppBarListener;
import com.example.asus.d_biumsemarang1.listener.DetailToFragmentModel;
import com.example.asus.d_biumsemarang1.utils.ConnectionUtils;
import com.example.asus.d_biumsemarang1.utils.ExtendsSwipeRefresh;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;


public class DetailActivity extends AppCompatActivity implements View.OnClickListener,AppBarListener {
    public static final String TAG = DetailActivity.class.getSimpleName();
    public static final String DATA_DATABASE = "data_database";
    public static final String KEY_DATABASE = "key_database";
    public static final String NAMA_DATABASE = "nama_database";
    public static final String ALAMAT_DATABASE = "alamat_database";
    public static final String BIAYA_DATABASE = "biaya_database";
    public static final String KODE_INF = "kode_inf";


    private BroadcastReceiver mBroadcastReceiver;
    private ExtendsSwipeRefresh mRefresh;
    private Toolbar mToolbar;
    private AppBarLayout mAppBar;
    private ViewPager mViewpager;
    private LinearLayout mConnectionLinear;
    private ImageView mImage;
    private TextView mKeterangan;
    private TextView mContact;
    private LinearLayout mContent;
    private ImageView mIndicator1, mIndicator2, mIndicator3;
    private TextView mTextSewa, mTextJadwal, mTextMap, mTextSuka;
    private TextView nama, alamat, biaya, suka, informasi, info1, info2, info3;
    private ImagesPagerAdapter mAdapter;
    private Toast mToast;



    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference mReference;
    private DatabaseReference mSelfLikeReference;
    private DatabaseReference mLikeReference;
    private ValueEventListener mEventListener;
    private ValueEventListener mSelfLikeListener;
    private ValueEventListener mLikeListener;


    private boolean isConnected;
    private boolean isExpanded;
    private boolean isCalled = true;
    private boolean isDestroyed = false;
    private boolean isFirstFetch = true;
    private boolean isFinished = true;
    private boolean mCheckLike = false;
    private boolean canCheckLike = false;
    private boolean isLiked = false;
    private String mKode;
    private String mKey;
    private String mUid;
    private boolean fromLikey = false;
    private GedungVenue mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                isConnected = ConnectionUtils.isOnline(DetailActivity.this);
                if (!isConnected) {
                    mRefresh.setRefreshing(false);
                }
                else if(!isCalled){
                    mRefresh.setRefreshing(true);
                }
            }
        };
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mBroadcastReceiver, filter);
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        initView();
        initListener();
        handleIntent();



    }

    private void initView() {
        mRefresh = findViewById(R.id.mRefresh);
        mRefresh.setType(1);
        mRefresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorProgress),
                ContextCompat.getColor(this, R.color.colorPrimary), ContextCompat.getColor(this, R.color.colorPrimaryDark));
        mToolbar = findViewById(R.id.mToolbar);
        mViewpager = findViewById(R.id.mViewPager);
        mViewpager.getLayoutParams().height = getWidth() * 2 / 3;
        mViewpager.setOffscreenPageLimit(2);
        mAppBar = findViewById(R.id.appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        marqueeToolbar();
        mConnectionLinear = findViewById(R.id.mConnectionLinear);
        mImage = findViewById(R.id.mImage);
        mKeterangan = findViewById(R.id.mKeterangan);
        mContact = findViewById(R.id.mContact);
        mContent = findViewById(R.id.mContent);
        mIndicator1 = findViewById(R.id.mIndicator1);
        mIndicator2 = findViewById(R.id.mIndicator2);
        mIndicator3 = findViewById(R.id.mIndicator3);
        mTextSewa = findViewById(R.id.mTextSewa);
        mTextJadwal = findViewById(R.id.mTextJadwal);
        mTextMap = findViewById(R.id.mTextMap);
        mTextSuka = findViewById(R.id.mTextSuka);
        nama = findViewById(R.id.nama);
        alamat = findViewById(R.id.alamat);
        biaya = findViewById(R.id.biaya);
        suka = findViewById(R.id.suka);
        suka.setText("-");
        informasi = findViewById(R.id.informasi);
        info1 = findViewById(R.id.info1);
        info2 = findViewById(R.id.info2);
        info3 = findViewById(R.id.info3);
    }

    private void initListener() {
        mIndicator1.setOnClickListener(this);
        mIndicator2.setOnClickListener(this);
        mIndicator3.setOnClickListener(this);
        mTextSewa.setOnClickListener(this);
        mTextJadwal.setOnClickListener(this);
        mTextMap.setOnClickListener(this);
        mTextSuka.setOnClickListener(this);
        info1.setOnClickListener(this);
        info2.setOnClickListener(this);
        info3.setOnClickListener(this);
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isConnected && isFinished) {
                    fetchData();
                } else {
                    mRefresh.setRefreshing(false);
                }
            }
        });
        mViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                changeCirclePosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                mRefresh.setState(state);
            }
        });
        mAppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                isExpanded = verticalOffset == 0;

            }
        });
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
    protected void onStop() {
        super.onStop();
        Log.v(TAG,"onStop");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG,"onStart");

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void marqueeToolbar() {
        TextView titleTextView;
        try {
            Field f = mToolbar.getClass().getDeclaredField("mTitleTextView");
            f.setAccessible(true);
            titleTextView = (TextView) f.get(mToolbar);
            titleTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            titleTextView.setSingleLine(true);
            titleTextView.setSelected(true);
            titleTextView.setMarqueeRepeatLimit(-1);

        } catch (NoSuchFieldException e) {
            Log.v(TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.v(TAG, e.getMessage());
        }
    }

    private int getWidth() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    private void handleIntent() {
        Intent intent = getIntent();
        Gson gson = new Gson();
        String data = intent.getStringExtra(DATA_DATABASE);
        mData = gson.fromJson(data, new TypeToken<GedungVenue>() {
        }.getType());
        mKey = mData.getKey();
        mKode = intent.getAction();
        String tipe;
        if (mKode.equals(MainActivity.KODE_GEDUNG)) {
            tipe = "gedung";
        } else {
            tipe = "venue";
        }
        mReference = mDatabase.getReference(tipe).child(mData.getKey());
        if (mAuth.getCurrentUser() != null) {
            mUid = mAuth.getCurrentUser().getUid();
            mLikeReference = mDatabase.getReference("suka/" + tipe).child(mKey);
            mSelfLikeReference = mDatabase.getReference("suka/" + tipe).child(mKey + "/users").child(mUid);
            canCheckLike = true;
        }
        if (canCheckLike) {
            checkLike();
        }
        attackLikeListener();
        processData();
    }

    private void fetchData() {
        isFinished = false;
        isCalled = false;
        mEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                isCalled = true;
                mData = dataSnapshot.getValue(GedungVenue.class);

                    sendLocalBroadcast();

                processData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mReference.addListenerForSingleValueEvent(mEventListener);
        attackLikeListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG,"onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        if (mSelfLikeReference != null) {
            mSelfLikeReference.removeEventListener(mSelfLikeListener);
            mSelfLikeReference = null;
        }
        detachListener();
        isDestroyed = true;
        Log.v(TAG,"onDestroy");
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        int index;
        switch (id) {
            case R.id.mIndicator1:
                if (mViewpager.getCurrentItem() != 0) {
                    mViewpager.setCurrentItem(0, true);
                }
                break;
            case R.id.mIndicator2:
                if (mViewpager.getCurrentItem() != 1) {
                    mViewpager.setCurrentItem(1, true);
                }
                break;
            case R.id.mIndicator3:
                if (mViewpager.getCurrentItem() != 2) {
                    mViewpager.setCurrentItem(2, true);
                }
                break;
            case R.id.info1:
                index = info1.getText().toString().indexOf(" ");
                callIntent(info1.getText().toString().substring(0, index));
                break;
            case R.id.info2:
                index = info2.getText().toString().indexOf(" ");
                callIntent(info2.getText().toString().substring(0, index));
                break;
            case R.id.info3:
                index = info3.getText().toString().indexOf(" ");
                callIntent(info3.getText().toString().substring(0, index));
                break;
            case R.id.mTextSewa:
                rentIntent();
                break;
            case R.id.mTextJadwal:
                scheduleIntent();
                break;
            case R.id.mTextMap:
                Intent intent = new Intent(this, MapActivity.class);
                intent.setAction(mKode);
                intent.putExtra(MapActivity.NAMA_MAP, mData.getNama());
                intent.putExtra(MapActivity.ALAMAT_MAP, mData.getAlamat());
                intent.putExtra(MapActivity.FOTO_MAP, mData.getGambar() != null ? mData.getGambar().get("P0").toString() : "");
                startActivity(intent);
                break;
            case R.id.mTextSuka:
                likey();
                break;
            default:
                break;
        }
    }

    private void showNoResult(boolean value) {
        if (value) {
            mConnectionLinear.setVisibility(View.VISIBLE);
            if (mKode.equals(MainActivity.KODE_GEDUNG)) {
                mImage.setImageResource(R.drawable.gedung);
                mKeterangan.setText(getString(R.string.not_found, getString(R.string.gedung)));
            } else {
                mImage.setImageResource(R.drawable.venue);
                mKeterangan.setText(getString(R.string.not_found, getString(R.string.venue)));
            }
        } else {
            mConnectionLinear.setVisibility(View.GONE);
        }
    }

    private void showResult(boolean value) {
        if (value) {
            mContent.setVisibility(View.VISIBLE);
        } else {
            mContent.setVisibility(View.GONE);
        }
    }

    private void checkLike() {
        mSelfLikeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mCheckLike = true;
                isLiked = dataSnapshot.exists();
                changeLike(dataSnapshot.exists());
                if(fromLikey){
                    checkToast();
                    successLike(isLiked);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v(TAG, databaseError.getMessage());
            }
        };
        mSelfLikeReference.addValueEventListener(mSelfLikeListener);
    }

    private void changeLike(boolean value) {
        if (value) {
            isLiked = true;
            mTextSuka.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.favoritefull_v2, 0, 0);
        } else {
            isLiked = false;
            mTextSuka.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.favorite_v2, 0, 0);
        }

    }

    private void processData() {
        mRefresh.setRefreshing(false);
        if (mData != null) {
            showNoResult(false);
            showResult(true);
            getSupportActionBar().setTitle(mData.getNama());
            nama.setText(mData.getNama());
            alamat.setText(mData.getAlamat());
            if (mData.getBiaya() == 0) {
                biaya.setText(getString(R.string.free));
            } else {
                biaya.setText(getString(R.string.hour,
                        NumberFormat.getInstance().format(mData.getBiaya())));
            }
            if (mData.getInformasi() == null) {
                informasi.setText(getString(R.string.no_information));
            } else if (mData.getInformasi().trim().isEmpty()) {
                informasi.setText(getString(R.string.no_information));
            } else {
                informasi.setText(mData.getInformasi());
            }
            List<String> gambar = null;
            if (mData.getGambar() != null) {
                gambar = new ArrayList<>();
                gambar.add((String) mData.getGambar().get("P0"));
                gambar.add((String) mData.getGambar().get("P1"));
                gambar.add((String) mData.getGambar().get("P2"));
            }
            mAdapter = new ImagesPagerAdapter(getSupportFragmentManager(), gambar);
            if (isFirstFetch) {
                mViewpager.setAdapter(mAdapter);
                isFirstFetch = false;
            }
            mViewpager.setCurrentItem(0);
            int value = 0;
            if (mData.getNoHp() != null) {
                Map<String, Object> noHp = mData.getNoHp();
                for (String key : noHp.keySet()) {
                    value++;
                    String info = getString(R.string.no_hp, noHp.get(key), key);
                    SpannableString content = new SpannableString(info);
                    int index = info.indexOf(" ");
                    content.setSpan(new UnderlineSpan(), 0, index, 0);
                    content.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.color_telephone)),
                            0, index, 0);
                    if (value == 1) {
                        info1.setText(content);
                    } else if (value == 2) {
                        info2.setText(content);
                    } else {
                        info3.setText(content);
                    }
                }
            }
            showContact(value);

        } else {
            getSupportActionBar().setTitle("");
            showResult(false);
            showNoResult(true);
            mRefresh.setEnabled(false);
        }
        isFinished = true;

    }

    private void detachListener() {
        if (mEventListener != null) {
            mReference.removeEventListener(mEventListener);
            mEventListener = null;
        }
        if (mLikeListener != null) {
            mLikeReference.child("jumlah").removeEventListener(mLikeListener);
            mLikeListener = null;
        }
    }

    private void showContact(int value) {
        switch (value) {
            case 0:
                info1.setVisibility(View.GONE);
                info2.setVisibility(View.GONE);
                info3.setVisibility(View.GONE);
                break;
            case 1:
                info1.setVisibility(View.VISIBLE);
                info2.setVisibility(View.GONE);
                info3.setVisibility(View.GONE);
                break;
            case 2:
                info1.setVisibility(View.VISIBLE);
                info2.setVisibility(View.VISIBLE);
                info3.setVisibility(View.GONE);
                break;
            default:
                info1.setVisibility(View.VISIBLE);
                info2.setVisibility(View.VISIBLE);
                info3.setVisibility(View.VISIBLE);
                break;
        }
        if (value != 0) {
            mContact.setVisibility(View.GONE);
        } else {
            mContact.setVisibility(View.VISIBLE);
        }
    }

    private void changeCirclePosition(int position) {
        switch (position) {
            case 0:
                mIndicator1.setImageResource(R.drawable.dot_white);
                mIndicator2.setImageResource(R.drawable.dot);
                mIndicator3.setImageResource(R.drawable.dot);
                break;
            case 1:
                mIndicator1.setImageResource(R.drawable.dot);
                mIndicator2.setImageResource(R.drawable.dot_white);
                mIndicator3.setImageResource(R.drawable.dot);
                break;
            default:
                mIndicator1.setImageResource(R.drawable.dot);
                mIndicator2.setImageResource(R.drawable.dot);
                mIndicator3.setImageResource(R.drawable.dot_white);
                break;
        }
    }

    private void callIntent(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void likey() {
        checkToast();
        if (isConnected && mCheckLike) {
            fromLikey = true;
            runTransactionLike();


        } else {
            failToLike();
        }
    }

    private void checkToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }

    private void failToLike() {
        mToast = Toasty.error(this, getString(R.string.check_connection), Toast.LENGTH_SHORT);
        mToast.show();
    }

    private void successLike(boolean like) {
        String message;
        String tipe;
        if (mKode.equals(MainActivity.KODE_GEDUNG)) {
            tipe = getString(R.string.gedung);
        } else {
            tipe = getString(R.string.venue);
        }
        if (like) {
            message = getString(R.string.like_this, tipe);
        } else {
            message = getString(R.string.dislike_this, tipe);
        }
        mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        mToast.show();

    }

    private void sendLocalBroadcast() {
        boolean isSetupFinished = DetailToFragmentModel.getInstance().getSetupFinished();
        if(isSetupFinished){
            if (mData != null) {
                mData.setKey(mKey);
            }
            List<GedungVenue> temp = DetailToFragmentModel.getInstance().getmData();
            if(temp != null){
                if(temp.size() != 0){
                    int pos = -1;
                    for(GedungVenue gedungVenue : temp){
                        pos++;
                        if(gedungVenue.getKey().equals(mKey)){
                            break;
                        }

                    }
                    DetailToFragmentModel.getInstance().setClickPosition(pos);
                    Log.v(TAG,"" + pos);
                    if(!isDestroyed && pos != -1){
                        Gson gson = new Gson();
                        String data = gson.toJson(mData);
                        Intent intent = new Intent(GedungVenueAdapter.NOTIFIY_ADAPTER);
                        intent.putExtra(KODE_INF, mKode);
                        intent.putExtra(DetailActivity.DATA_DATABASE, data);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    }
                }
            }
        }


    }

    private void rentIntent() {
        Intent intent = new Intent(this, RentActivity.class);
        intent.setAction(mKode);
        intent.putExtra(KEY_DATABASE, mData.getKey());
        intent.putExtra(NAMA_DATABASE, mData.getNama());
        intent.putExtra(ALAMAT_DATABASE, mData.getAlamat());
        intent.putExtra(BIAYA_DATABASE, mData.getBiaya());
        startActivity(intent);
    }

    private void scheduleIntent() {
        Intent intent = new Intent(this, ScheduleActivity.class);
        intent.setAction(mKode);
        intent.putExtra(KEY_DATABASE, mData.getKey());
        startActivity(intent);
    }

    private void attackLikeListener() {
        mLikeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long jumlahSuka;
                if (dataSnapshot.exists()) {
                    jumlahSuka = dataSnapshot.getValue(Long.class);

                } else {
                    jumlahSuka = 0;
                }
                suka.setText(NumberFormat.getInstance().format(jumlahSuka));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mLikeReference.child("jumlah").addListenerForSingleValueEvent(mLikeListener);
    }

    private void runTransactionLike() {
        mLikeReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                SukaGedungVenue data = mutableData.getValue(SukaGedungVenue.class);
                if (data == null) {
                    SukaGedungVenue first = new SukaGedungVenue();
                    first.jumlah = first.jumlah + 1;
                    first.users = new HashMap<>();
                    first.users.put(mUid, true);
                    mutableData.setValue(first);
                    return Transaction.success(mutableData);
                }
                if (data.users != null) {
                    if(data.users.containsKey(mUid)){
                        data.jumlah = data.jumlah - 1;
                        data.users.remove(mUid);
                    }
                    else {
                        data.jumlah = data.jumlah + 1;
                        data.users.put(mUid, true);
                    }

                } else {
                    data.jumlah = data.jumlah + 1;
                    data.users = new HashMap<>();
                    data.users.put(mUid, true);
                }
                mutableData.setValue(data);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    Log.v(TAG, databaseError.getMessage());
                }
            }
        });
    }

    @Override
    public void enableScroll() {

    }

    @Override
    public void disableScroll() {

    }

    @Override
    public void expandAppBar() {

    }

    @Override
    public boolean isExpanded() {
        return isExpanded;
    }


}
