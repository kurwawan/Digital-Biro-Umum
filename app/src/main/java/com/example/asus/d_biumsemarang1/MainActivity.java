package com.example.asus.d_biumsemarang1;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.asus.d_biumsemarang1.fcm.MyFirebaseInstanceIdService;
import com.example.asus.d_biumsemarang1.fcm.MyFirebaseMessageService;
import com.example.asus.d_biumsemarang1.fragment.GedungFragment;
import com.example.asus.d_biumsemarang1.fragment.HistoryFragment;
import com.example.asus.d_biumsemarang1.fragment.VenueFragment;
import com.example.asus.d_biumsemarang1.listener.AppBarListener;
import com.example.asus.d_biumsemarang1.listener.HistoryFragmentListener;
import com.example.asus.d_biumsemarang1.listener.MainActivityListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity implements  AppBarListener,HistoryFragmentListener {
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String CLICK_POSITION_FRAGMENT = "click_position_fragment";
    public static final String IDENTITY = "identity";
    public static final String KODE_GEDUNG = "gedung";
    public static final String KODE_VENUE = "venue";
    public static final String KODE_RIWAYAT = "riwayat";
    public static final String FROM_LOGIN = "from_login";
    public static final float MILLISECONDS_PER_INCH = 40f;



    private BottomNavigationView mNavigation;
    private CollapsingToolbarLayout mCollapse;
    private AppBarLayout mAppBar;
    private Toolbar mToolbar;
    private NotificationManager mNotificationManager;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private DatabaseReference mTokenReference;
    private DatabaseReference mMaintenanceRef;
    private ValueEventListener mValueListener;
    private ValueEventListener mMaintenanceListener;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private SharedPreferences mPreferences;
    private boolean firstTimeFragment[] = null;
    private Fragment mCurrentFragment;
    private FragmentManager mFragmentManager;
    private boolean isExpanded = true;
    private int mCurrentItem = 0;
    private int mHistoryFragmentPosition = 0;
    private boolean changeLoginTime = false;
    private boolean isFromCheck = false;
    private boolean isFirstTime = true;
    private boolean alreadyCalled = true;
    private boolean hasFinished = false;
    private String mUid;
    private boolean mNeedReload;
    private String mKode;
    private Intent mIntent;
    private boolean isProgramatically = false;
    private boolean checkIntent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivityListener.getInstance().setCreated(true);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            isFromCheck = true;
            backToLoginScreen(2, 0);
            return;
        }
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mUid = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference("users").child(mUid);
        mTokenReference = mReference.child("token");
        mMaintenanceRef = mDatabase.getReference("maintenance");
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        initBoolean();
        checkLogin();
        sendToken();
        handleVerificationIntent();
        initView();
        setUpView();
        initListener();
        mFragmentManager = getSupportFragmentManager();
        positionateFragmentfromIntent();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.KODE_VENUE);
        filter.addAction(MainActivity.KODE_GEDUNG);

    }


    private void checkLogin() {
        mIntent = getIntent();
        changeLoginTime = mIntent.getBooleanExtra(LoginScreenActivity.EXTRA_LOGIN, false);
        attachFirebaseDatabaseListener();

    }

    private void initView() {
        mToolbar = findViewById(R.id.mToolbar);
        mNavigation = findViewById(R.id.mNavigation);
        mCollapse = findViewById(R.id.mCollapse);
        mAppBar = findViewById(R.id.appBar);

    }

    private void setUpView() {
        setSupportActionBar(mToolbar);
        mCollapse.setTitleEnabled(false);
        mToolbar.setTitle(getString(R.string.app_name));
    }

    private void initListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null && !isFromCheck) {
                    mNotificationManager.cancelAll();
                    Intent intent = new Intent(MainActivity.this, LoginScreenActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        };
        mNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (!isProgramatically) {
                    int id = item.getItemId();
                    int pos = 0;
                    switch (id) {
                        case R.id.gedung:
                            pos = 0;
                            break;

                        case R.id.venue:
                            pos = 1;
                            break;

                        case R.id.riwayat:
                            pos = 2;
                            break;
                    }
                    changeFragment(pos);
                } else {
                    isProgramatically = false;
                }
                return true;
            }
        });
        mNavigation.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                Log.v(TAG, "RESELECT");
                if (!isProgramatically) {
                    Log.v(TAG, "PROG");
                    if (mCurrentFragment != null) {
                        if (mCurrentFragment instanceof GedungFragment) {
                            ((GedungFragment) mCurrentFragment).smoothScrool();
                        } else if (mCurrentFragment instanceof VenueFragment) {
                            ((VenueFragment) mCurrentFragment).smoothScrool();
                        }
                    }
                    Log.v(TAG, "Called");
                } else {
                    Log.v(TAG, "CALLS");
                    isProgramatically = false;
                }

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
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthListener);
        if (!alreadyCalled) {
            sendToken();
        }
        if (checkIntent) {
            checkIntent = false;
            checkIntent();
        }
        Log.v(TAG,"onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAuth.removeAuthStateListener(mAuthListener);
        Log.v(TAG,"onPause");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG,"onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG,"onStop");
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem itemSearch = menu.findItem(R.id.search);
        switch (mCurrentItem) {
            case 0:
                itemSearch.setTitle("Cari Gedung");
                break;
            case 1:
                itemSearch.setTitle("Cari Venue");
                break;
            default:
                itemSearch.setTitle("Cari Riwayat");
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.profile:
                intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                return true;
            case R.id.search:
                intent = new Intent(this, SearchActivity.class);
                intent.putExtra(SearchActivity.SEARCH_TYPE, mKode);
                if(mKode.equals(KODE_RIWAYAT)){
                    intent.putExtra(SearchActivity.RIWAYAT_POSITION, mHistoryFragmentPosition);
                }
                startActivity(intent);
                return true;
            case R.id.pay:
                intent = new Intent(this, PayActivity.class);
                startActivity(intent);
                return true;
            case R.id.contact:
                intent = new Intent(this, KontakActivity.class);
                startActivity(intent);
                return  true;
            case R.id.about:
                intent = new Intent(this, AplikasiActivity.class);
                startActivity(intent);
                return  true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        detachFirebaseDatabaseListener();
        MainActivityListener.getInstance().setCreated(false);
        Log.v(TAG,"onDestroy");

    }

    private void changeFragment(int pos) {
        switch (pos) {
            case 0:
                mKode = KODE_GEDUNG;
                mCurrentFragment = new GedungFragment();
                break;
            case 1:
                mKode = KODE_VENUE;
                mCurrentFragment = new VenueFragment();
                break;
            default:
                mKode = KODE_RIWAYAT;
                mCurrentFragment = new HistoryFragment();

                break;
        }
        mCurrentItem = pos;
        Bundle bundle = new Bundle();
        bundle.putBoolean(FROM_LOGIN, firstTimeFragment[mCurrentItem]);
        mCurrentFragment.setArguments(bundle);
        firstTimeFragment[mCurrentItem] = false;
        mFragmentManager.beginTransaction().replace(R.id.mContainer, mCurrentFragment, mKode).commit();
        if(isFirstTime){
            isFirstTime = false;
            return;
        }
        invalidateOptionsMenu();
        mAppBar.setExpanded(true);
    }

    @Override
    public void enableScroll() {
        final AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams)
                mCollapse.getLayoutParams();
        params.setScrollFlags(
                AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                        | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP

        );
        mCollapse.setLayoutParams(params);
    }

    @Override
    public void disableScroll() {
        final AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams)
                mCollapse.getLayoutParams();
        params.setScrollFlags(0);
        mCollapse.setLayoutParams(params);
    }

    @Override
    public void expandAppBar() {
        mAppBar.setExpanded(true, true);
    }

    @Override
    public boolean isExpanded() {
        return isExpanded;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        Log.v(TAG, "Called");
        setIntent(intent);
        mIntent = getIntent();
        checkIntent = true;
        if(mIntent.getBooleanExtra(MyFirebaseMessageService.ACTION_VERIFIKASI, false)){
            handleVerificationIntent();
        }


    }

    private void attachFirebaseDatabaseListener() {
        mValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild("login") || !dataSnapshot.hasChild("email")) {
                    Log.v(TAG, "fromLOGINEMAIL");
                    isFromCheck = true;
                    backToLoginScreen(1, 0);
                } else {
                    checkIdentity((int) dataSnapshot.getChildrenCount());
                    Log.v(TAG, "Change " + changeLoginTime);
                    if (changeLoginTime) {
                        changeLoginTime = false;
                        long currentTime = System.currentTimeMillis();
                        changeLoginTime(currentTime, false);
                    } else {
                        long time = dataSnapshot.child("login").getValue(Long.class);
                        checkLoginTime(time);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mReference.addValueEventListener(mValueListener);
        mMaintenanceListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long time = dataSnapshot.child("lama").getValue(Long.class);
                    boolean value = dataSnapshot.child("status").getValue(Boolean.class);
                    checkMaintenance(value, time);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mMaintenanceRef.addValueEventListener(mMaintenanceListener);

    }

    private void detachFirebaseDatabaseListener() {
        if (mValueListener != null) {
            mReference.removeEventListener(mValueListener);
            mValueListener = null;
        }
        if (mMaintenanceRef != null) {
            mMaintenanceRef.removeEventListener(mMaintenanceListener);
            mMaintenanceListener = null;
        }
    }

    private synchronized void backToLoginScreen(int value, int time) {
        if (!hasFinished) {
            Log.v(TAG, "Called");
            hasFinished = true;
            mDatabase.purgeOutstandingWrites();
            mAuth.signOut();
            mNotificationManager.cancelAll();
            Intent intent = new Intent(this, LoginScreenActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(LoginScreenActivity.SHOW_DIALOG, value);
            if (value == 0) {
                intent.putExtra(LoginScreenActivity.TIME, time);
            }
            startActivity(intent);
        }

    }

    private void checkIdentity(int childCount) {
        SharedPreferences.Editor editor = mPreferences.edit();
        if (childCount >= 7) {
            editor.putBoolean(IDENTITY, true);
        } else {
            editor.putBoolean(IDENTITY, false);
        }
        editor.apply();
    }

    private void changeLoginTime(final long currentTime, boolean repeat) {
        if (!repeat) {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putLong(LoginScreenActivity.LAST_LOGIN, currentTime);
            editor.apply();
        }
        mReference.child("login").setValue(currentTime, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    if (databaseError.getCode() == DatabaseError.WRITE_CANCELED) {
                        changeLoginTime(currentTime, true);
                    } else {
                        isFromCheck = true;
                        Log.v(TAG, "from" + databaseError.getMessage());
                        backToLoginScreen(2, 0);
                    }
                }
            }
        });
    }

    private void checkLoginTime(long time) {
        long lastTimeLogin = mPreferences.getLong(LoginScreenActivity.LAST_LOGIN, 0);
        if (time != lastTimeLogin) {
            Log.v(TAG, "fromLOGIN");
            isFromCheck = true;
            backToLoginScreen(3, 0);
        }
    }

    private void checkMaintenance(boolean value, long time) {
        if (value) {
            isFromCheck = true;
            Log.v(TAG, "fromMT");
            backToLoginScreen(0, (int) time);
        }
    }

    private void sendToken() {
        if (mAuth.getCurrentUser() != null && mPreferences.getBoolean(MyFirebaseInstanceIdService.TOKEN_CHANGED, true)) {
            mTokenReference.setValue(FirebaseInstanceId.getInstance().getToken(), new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        mPreferences.edit().putBoolean(MyFirebaseInstanceIdService.TOKEN_CHANGED, false).apply();
                    } else if (databaseError.getCode() == DatabaseError.WRITE_CANCELED) {
                        sendToken();
                        alreadyCalled = false;
                    }
                }
            });
        }
    }

    private void positionateFragmentfromIntent() {
        if(mIntent.getAction() == null){
            changeFragment(mCurrentItem);
        }
        else{
           changeToHistoryFragment(false);
            mCurrentItem = 2;
            mFragmentManager.beginTransaction().replace(R.id.mContainer, mCurrentFragment, mKode).commit();
            firstTimeFragment[mCurrentItem] = false;
        }

        changePositionNavigation();


    }

    private void changeToHistoryFragment(boolean needReload) {
        Bundle bundle = new Bundle();
        if (mIntent.getAction().equals(KODE_GEDUNG)) {
            bundle.putInt(HistoryFragment.KODE_POSITION, 0);
        } else if (mIntent.getAction().equals(KODE_VENUE)) {
            bundle.putInt(HistoryFragment.KODE_POSITION, 1);
        }
        bundle.putBoolean(HistoryFragment.NEED_RELOAD, needReload);
        bundle.putBoolean(FROM_LOGIN, firstTimeFragment[mCurrentItem]);
        mKode = KODE_RIWAYAT;
        mCurrentFragment = new HistoryFragment();
        mCurrentFragment.setArguments(bundle);

    }

    private void changePositionNavigation() {
        int id = 0;
        switch (mCurrentItem) {
            case 0:
                id = R.id.gedung;
                break;
            case 1:
                id = R.id.venue;
                break;
            case 2:
                id = R.id.riwayat;
                break;
        }
        isProgramatically = true;
        mNavigation.setSelectedItemId(id);
    }

    private void checkIntent() {
        if (mIntent.getAction() != null) {
            Log.v(TAG, "ACTION " + mIntent.getAction());
            if (mIntent.getAction().equals(MainActivity.KODE_GEDUNG) || mIntent.getAction().equals(MainActivity.KODE_VENUE)) {
                proceedCheckIntent(true);
                mAppBar.setExpanded(true);
            }
        }

    }

    private void initBoolean() {
        firstTimeFragment = new boolean[3];
        firstTimeFragment[0] = true;
        firstTimeFragment[1] = true;
        firstTimeFragment[2] = true;
    }

    private void proceedCheckIntent(boolean needReload){
        if (mCurrentItem != 2) {
            mCurrentItem = 2;
            changeToHistoryFragment(needReload);
            mFragmentManager.beginTransaction().replace(R.id.mContainer, mCurrentFragment, mKode).commit();
            changePositionNavigation();
            firstTimeFragment[mCurrentItem] = false;
        } else {
            Intent intent1 = new Intent(MainActivity.KODE_RIWAYAT);
            if (mIntent.getAction().equals(KODE_GEDUNG)) {
                intent1.putExtra(HistoryFragment.KODE_POSITION, 0);
            } else if (mIntent.getAction().equals(KODE_VENUE)) {
                intent1.putExtra(HistoryFragment.KODE_POSITION, 1);
            }
            intent1.putExtra(HistoryFragment.NEED_RELOAD, needReload);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent1);
            Log.v(TAG, "Sended");
        }
    }

    private void handleVerificationIntent(){
        if(mIntent.getBooleanExtra(MyFirebaseMessageService.ACTION_VERIFIKASI, false)){
            Intent intent = new Intent(MainActivity.this, DetailSewaActivity.class);
            intent.setAction(mIntent.getAction());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
           intent.putExtra(DetailActivity.DATA_DATABASE,
                    mIntent.getStringExtra(DetailActivity.DATA_DATABASE));
           startActivity(intent);
        }
    }


    @Override
    public void setHistoryFragmentPosition(int position) {
        mHistoryFragmentPosition = position;
    }
}
