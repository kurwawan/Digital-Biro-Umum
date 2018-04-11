package com.example.asus.d_biumsemarang1;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.d_biumsemarang1.data.Sewa;
import com.example.asus.d_biumsemarang1.utils.ConnectionUtils;
import com.example.asus.d_biumsemarang1.utils.DialogUtils;
import com.example.asus.d_biumsemarang1.utils.TimeUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.wdullaer.materialdatetimepicker.time.Timepoint;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;

public class RentActivity extends AppCompatActivity implements View.OnClickListener, Dialog.OnClickListener {
    public static final String TAG = RentActivity.class.getSimpleName();


    private TextView mNama, nama;
    private TextView mJudul;
    private TextView alamat;
    private TextView biaya;
    private TextView tgl_pinjam, tgl_kembali, jam_pinjam, jam_kembali;
    private TextView pickDate1, pickTime1, pickDate2, pickTime2;
    private TextView duration, total;
    private Button mBtnCount, mBtnRent;
    private BroadcastReceiver mReceiver;
    private Toast mToast;
    private AlertDialog mDialog;
    private Toolbar mToolbar;

    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference mWaktuRef;
    private DatabaseReference mLastRef;
    private ValueEventListener mLastListener;
    private ValueEventListener mWaktuListener;


    private String mKode;
    private String mKey;
    private String mType;
    private long mBiaya;
    private long mTanggalPinjam;
    private long mTanggalKembali;
    private long mJamPinjam;
    private long mJamKembali;
    private long mWaktuPinjam;
    private long mWaktuKembali;
    private long mTotalBiaya;
    private long mLamaPinjam;
    private long mLastClickTime;
    private long mLastRent;
    private int invalidType;
    private int idLastButton;
    private boolean isInterrupted = true;
    private boolean isConnected = false;
    private boolean isPinjam;
    private boolean isDestroyed;
    private boolean isChanged = true;
    private boolean isValid;
    private boolean isCalled = false;
    private boolean isChecked = true;
    private boolean hasFinished = true;
    private boolean isDateAlreadySet = false;
    private boolean isTimeAlreadySet = false;
    private Sewa mSewa;
    private Timepoint mTimepoint[] = new Timepoint[17];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent);
        isDestroyed = false;
        int hour = 7;
        for (int i = 0; i < 17; i++) {
            mTimepoint[i] = new Timepoint(hour);
            hour++;
        }
        //cancel sewa ketika lost connection
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                isConnected = ConnectionUtils.isOnline(RentActivity.this);
                if (!isConnected && !isInterrupted) {
                    isInterrupted = true;
                    showToastError(R.string.check_connection);
                    if(mDialog != null){
                        mDialog.dismiss();
                    }
                    detachListener();
                }
            }
        };
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);
        mAuth = FirebaseAuth.getInstance();
        initView();
        initListener();
        handleIntent();
        initFirebaseDatabase();
        attachListenerLastRent();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initView() {
        mNama = findViewById(R.id.mNama);
        mJudul = findViewById(R.id.mJudul);
        nama = findViewById(R.id.nama);
        alamat = findViewById(R.id.alamat);
        biaya = findViewById(R.id.biaya);
        tgl_pinjam = findViewById(R.id.tgl_pinjam);
        jam_pinjam = findViewById(R.id.jam_pinjam);
        tgl_kembali = findViewById(R.id.tgl_kembali);
        jam_kembali = findViewById(R.id.jam_kembali);
        pickDate1 = findViewById(R.id.pickDate1);
        pickDate2 = findViewById(R.id.pickDate2);
        pickTime1 = findViewById(R.id.pickTime1);
        pickTime2 = findViewById(R.id.pickTime2);
        duration = findViewById(R.id.duration);
        total = findViewById(R.id.total);
        mBtnCount = findViewById(R.id.mBtnCount);
        mBtnRent = findViewById(R.id.mBtnRent);
        mToolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(mToolbar);
    }

    private void initListener() {
        pickDate1.setOnClickListener(this);
        pickDate2.setOnClickListener(this);
        pickTime1.setOnClickListener(this);
        pickTime2.setOnClickListener(this);
        mBtnCount.setOnClickListener(this);
        mBtnRent.setOnClickListener(this);
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

    private void handleIntent() {
        Intent intent = getIntent();
        mKode = intent.getAction();
        int btnIcon;
        if (mKode.equals(MainActivity.KODE_GEDUNG)) {
            mType = getString(R.string.gedung);
            btnIcon = R.drawable.gedung_v2;
        } else {
            mType = getString(R.string.venue);
            btnIcon = R.drawable.venue_v2;
        }
        getSupportActionBar().setTitle(getString(R.string.rent_v2, mType));
        mBtnRent.setCompoundDrawablesWithIntrinsicBounds(btnIcon, 0, 0, 0);
        mKey = intent.getStringExtra(DetailActivity.KEY_DATABASE);
        mJudul.setText(getString(R.string.info, mType));
        mNama.setText(getString(R.string.name, mType));
        nama.setText(intent.getStringExtra(DetailActivity.NAMA_DATABASE));
        alamat.setText(intent.getStringExtra(DetailActivity.ALAMAT_DATABASE));
        mBiaya = intent.getLongExtra(DetailActivity.BIAYA_DATABASE, 0);
        changePrice();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500 && idLastButton == id) {
            return;
        }
        idLastButton = id;
        mLastClickTime = SystemClock.elapsedRealtime();
        switch (id) {
            case R.id.pickDate1:
                isPinjam = true;
                buildDatePicker(R.id.pickDate1);
                break;
            case R.id.pickTime1:
                isPinjam = true;
                buildTimePicker(R.id.pickTime1);
                break;
            case R.id.pickDate2:
                isPinjam = false;
                buildDatePicker(R.id.pickDate2);
                break;
            case R.id.pickTime2:
                isPinjam = false;
                buildTimePicker(R.id.pickTime2);
                break;
            case R.id.mBtnCount:
                calculatePrice();
                break;
            case R.id.mBtnRent:
                calculatePrice();
                if (isValid) {
                    String message = getString(R.string.sure_rent, nama.getText().toString()
                            , duration.getText().toString(), total.getText().toString());
                    mDialog = DialogUtils.buildShowDialog(this, R.string.rent, message, this);
                    mDialog.show();
                }
                break;
            default:
                break;
        }
    }

    private void initFirebaseDatabase() {
        mDatabase = FirebaseDatabase.getInstance();
        String tipe;
        if (mKode.equals(MainActivity.KODE_GEDUNG)) {
            tipe = "gedung";
        } else {
            tipe = "venue";
        }
        mWaktuRef = mDatabase.getReference("waktu").child(tipe).child(mKey);
        if (mAuth.getCurrentUser() != null) {
            mLastRef = mDatabase.getReference().child("sewa/" + tipe).child(mKey).child(mAuth.getCurrentUser().getUid());
        }
    }

    private void detachListener() {
        if (mWaktuListener != null) {
            mWaktuRef.removeEventListener(mWaktuListener);
            mWaktuListener = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        isDestroyed = true;
        isInterrupted = true;
        detachListener();
        if (mLastListener != null) {
            mLastRef.removeEventListener(mLastListener);
            mLastListener = null;
        }
    }

    private void changePrice() {
        if (mBiaya == 0) {
            biaya.setText(getString(R.string.free));
        } else {
            biaya.setText(getString(R.string.hour,
                    NumberFormat.getInstance().format(mBiaya)));
        }
    }

    private void buildDatePicker(int tipe) {
        long minTanggal = TimeUtils.getMinTanggal();
        long maxTanggal;
        Calendar calendarMin = Calendar.getInstance();
        Calendar calendarMax = Calendar.getInstance();
        calendarMin.setTimeInMillis(minTanggal);
        if (tipe == R.id.pickDate1) {
            maxTanggal = TimeUtils.getMaxTanggalPinjam();

        } else {
            maxTanggal = TimeUtils.getMaxTanggalKembali();
        }
        int year, month, dayOfMonth;
        Calendar calendar = Calendar.getInstance();
        if (isDateAlreadySet) {
            if (isPinjam) {
                calendar.setTimeInMillis(mTanggalPinjam);
            } else {
                calendar.setTimeInMillis(mTanggalKembali);
            }
        } else {
            calendar.setTimeInMillis(minTanggal);
        }
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                isDateAlreadySet = true;
                isChanged = true;
                if (isPinjam) {
                    mTanggalPinjam = TimeUtils.getDateToMillisecond(year, monthOfYear, dayOfMonth);
                    tgl_pinjam.setTextColor(ContextCompat.getColor(RentActivity.this, android.R.color.black));
                    tgl_pinjam.setText(TimeUtils.getStringDate(mTanggalPinjam));
                } else {
                    mTanggalKembali = TimeUtils.getDateToMillisecond(year, monthOfYear, dayOfMonth);
                    tgl_kembali.setTextColor(ContextCompat.getColor(RentActivity.this, android.R.color.black));
                    tgl_kembali.setText(TimeUtils.getStringDate(mTanggalKembali));
                }
                resetTextPrice();
            }
        }, year, month, dayOfMonth);
        dpd.setCancelText(R.string.cancel);
        dpd.setMinDate(calendarMin);
        calendarMax.setTimeInMillis(maxTanggal);
        dpd.setMaxDate(calendarMax);
        dpd.setTitle(getString(R.string.pick_date));
        dpd.setAccentColor(ContextCompat.getColor(this, R.color.colorProgress));
        dpd.vibrate(false);
        dpd.show(getFragmentManager(), "DATEPICKERDIALOG");
    }

    private void buildTimePicker(int tipe) {
        int hour;
        if (isTimeAlreadySet) {
            if (isPinjam) {
                hour = (int) TimeUnit.MILLISECONDS.toHours(mJamPinjam);
            } else {
                hour = (int) TimeUnit.MILLISECONDS.toHours(mJamKembali);
            }

        } else {
            hour = 7;
        }
        TimePickerDialog tpd = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
                isTimeAlreadySet = true;
                isChanged = true;
                if (isPinjam) {
                    mJamPinjam = TimeUnit.HOURS.toMillis(hourOfDay);
                    jam_pinjam.setTextColor(ContextCompat.getColor(RentActivity.this, android.R.color.black));
                    jam_pinjam.setText(TimeUtils.getStringJam((int) TimeUnit.MILLISECONDS.toHours(mJamPinjam)));
                } else {
                    mJamKembali = TimeUnit.HOURS.toMillis(hourOfDay);
                    jam_kembali.setTextColor(ContextCompat.getColor(RentActivity.this, android.R.color.black));
                    jam_kembali.setText(TimeUtils.getStringJam((int) TimeUnit.MILLISECONDS.toHours(mJamKembali)));
                }
                resetTextPrice();
            }
        }, hour, 0, true);
        tpd.setCancelText(R.string.cancel);
        tpd.setMinTime(7, 0, 0);
        if (isPinjam) {
            tpd.setMaxTime(22, 0, 0);
        } else {
            tpd.setMaxTime(23, 0, 0);
        }
        tpd.setSelectableTimes(mTimepoint);
        tpd.setTitle(getString(R.string.pick_time));
        tpd.vibrate(false);
        tpd.setAccentColor(ContextCompat.getColor(this, R.color.colorProgress));
        tpd.show(getFragmentManager(), "TIMEPICKERDIALOG");
    }

    private void calculatePrice() {
        long limitTime = TimeUtils.getMinTanggal() + TimeUnit.HOURS.toMillis(7);
        if (isChanged) {
            isChanged = false;
            if (mTanggalPinjam != 0 && mTanggalKembali != 0 && mJamPinjam != 0 && mJamKembali != 0) {
                mWaktuPinjam = mTanggalPinjam + mJamPinjam;
                mWaktuKembali = mTanggalKembali + mJamKembali;
                if (mWaktuPinjam >= mWaktuKembali || mWaktuPinjam < limitTime) {
                    isValid = false;
                    invalidType = R.string.time_invalid;
                } else {
                    long deviation = mWaktuKembali - mWaktuPinjam;
                    mLamaPinjam = TimeUnit.MILLISECONDS.toHours(deviation);
                    isValid = true;
                    mTotalBiaya = mLamaPinjam * mBiaya;
                    long hari = mLamaPinjam / 24;
                    long hour = mLamaPinjam % 24;
                    duration.setText(getString(R.string.rent_range, hari, hour));
                    if (mTotalBiaya == 0) {
                        total.setText(getString(R.string.free));
                    } else {
                        total.setText(NumberFormat.getInstance().format(mTotalBiaya));
                    }
                }
            } else {
                invalidType = R.string.time_notComplete;
                isValid = false;
            }
            if (isValid) {
                total.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                duration.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            } else {
                invalidInput(invalidType);
                resetTextPrice();
            }
        } else {
            if (!isValid) {
                invalidInput(invalidType);
            } else if (mWaktuPinjam < limitTime) {
                isValid = false;
                invalidType = R.string.time_invalid;
                invalidInput(invalidType);
            }
        }

    }

    private void checkToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }

    private void invalidInput(int message) {
        checkToast();
        mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int id) {
        switch (id) {
            case DialogInterface.BUTTON_POSITIVE:
                processingRent();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                dialog.dismiss();
                break;
            default:
                break;
        }
    }

    private void processingRent() {
        detachListener();
        long currentTime = TimeUtils.ubahKeFormatWIB(System.currentTimeMillis());
        if(!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(MainActivity.IDENTITY, false)){
            mDialog = DialogUtils.buildMessageDialog(this, R.string.information , getString(R.string.fill_profile, nama.getText().toString()));
            mDialog.show();
            return;
        }
        /*else if(!mAuth.getCurrentUser().isEmailVerified()){
            mDialog = DialogUtils.buildMessageDialog(this, R.string.information , getString(R.string.send_emailVerif, mType));
            mDialog.show();
        }
        else*/
        if (isConnected && isCalled) {
            if (mLastRent > currentTime) {
                mDialog = DialogUtils.buildMessageDialog(this, R.string.information,
                        getString(R.string.has_alreadyRent, mType));
                mDialog.show();
            } else if (hasFinished) {
                hasFinished = false;
                isInterrupted = false;
                mDialog = DialogUtils.buildShowDialogProgress(this);
                mDialog.setCancelable(true);
                mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        isInterrupted = true;
                        mDatabase.purgeOutstandingWrites();
                    }
                });
                mDialog.show();
                attachListenerWaktu();
            } else {
                showToastError(R.string.fail_process);
            }
        } else if (isConnected) {
            showToastError(R.string.fail_process);

        } else {
            showToastError(R.string.check_connection);
        }
    }

    private void attachListenerLastRent() {
        mLastListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mLastRent = dataSnapshot.getValue(Long.class);
                } else {
                    mLastRent = 0;
                }
                isCalled = true;
                Log.v(TAG, "Called");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mLastRef.addValueEventListener(mLastListener);
    }

    private void attachListenerWaktu() {
        mWaktuListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!isInterrupted) {
                    validateWaktuRent(dataSnapshot);
                } else {
                    hasFinished = true;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mWaktuRef.orderByChild("waktu_pinjam").endAt((double) mWaktuKembali).addListenerForSingleValueEvent(mWaktuListener);

    }

    private void showToastError(int message) {
        if (!isDestroyed) {
            checkToast();
            mToast = Toasty.error(this, getString(message), Toast.LENGTH_SHORT);
            mToast.show();
        }
    }

    private void validateWaktuRent(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() != 0) {
            for (DataSnapshot data : dataSnapshot.getChildren()) {
                checkTime(data.child("waktu_pinjam").getValue(Long.class), data.child("waktu_kembali").getValue(Long.class));
                if (!isChecked && !isInterrupted) {
                    mDialog.dismiss();
                    mDialog = DialogUtils.buildMessageDialog(this, R.string.information
                            , getString(R.string.collison, mType));
                    mDialog.show();
                    hasFinished = true;
                    return;
                } else if (isInterrupted) {
                    hasFinished = true;
                    return;
                }
            }
            addSewaInfrastruktur();
        } else {
            addSewaInfrastruktur();
        }
    }

    private void checkTime(long pinjam, long kembali) {
        if (kembali < mWaktuPinjam || pinjam > mWaktuKembali) {
            if ((kembali + TimeUnit.HOURS.toMillis(2) <= mWaktuPinjam) ||
                    (pinjam - TimeUnit.HOURS.toMillis(2) >= mWaktuKembali)) {
                isChecked = true;
            } else {
                isChecked = false;
            }
        } else {
            isChecked = false;
        }
    }

    private Sewa dataSewa() {
        String email = mAuth.getCurrentUser().getEmail();
        Sewa sewa = new Sewa();
        sewa.setAlamat(alamat.getText().toString());
        sewa.setBiaya(mBiaya);
        sewa.setEmail_inf(email + "_" + nama.getText().toString());
        sewa.setJam(mLamaPinjam);
        sewa.setStatus(0);
        sewa.setStatus_email(0 + "_" + email);
        sewa.setId_inf(mKey);
        sewa.setUid(mAuth.getCurrentUser().getUid());
        sewa.setTotal(mTotalBiaya);
        sewa.setWaktu_pinjam(mWaktuPinjam);
        sewa.setWaktu_kembali(mWaktuKembali);
        sewa.setStatus_inf(0 + "_" + nama.getText().toString());
        sewa.setKeterangan("");
        return sewa;
    }

    private void addSewaInfrastruktur() {
        if (!isInterrupted) {
            mSewa = dataSewa();
            String push = mWaktuRef.push().getKey();
            String tipe;
            Map<String, Object> map = new HashMap<>();
            if (mKode.equals(MainActivity.KODE_GEDUNG)) {
                tipe = "gedung";
            } else {
                tipe = "venue";
            }
            map.put("waktu/" + tipe + "/" + mKey + "/" + push, mSewa);
            map.put("sewa/" + tipe + "/" + mKey + "/" + mAuth.getCurrentUser().getUid(), mWaktuPinjam);
            mDatabase.getReference().updateChildren(map, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (!isInterrupted) {
                        if (databaseError == null) {
                            resetAll();
                        } else {
                            showToastError(R.string.fail_process);
                        }
                    }
                    mDialog.dismiss();
                    isInterrupted = true;
                    hasFinished = true;
                }
            });
        } else {
            hasFinished = true;
        }

    }


    private void resetTextPrice() {
        duration.setText("misal : 5 jam");
        total.setText("misal : 3,000,000");
        total.setTextColor(ContextCompat.getColor(this, R.color.colorHint));
        duration.setTextColor(ContextCompat.getColor(this, R.color.colorHint));
    }

    private void resetAll() {
        tgl_pinjam.setText("misal : Selasa 11 Juli 2017");
        tgl_pinjam.setTextColor(ContextCompat.getColor(this, R.color.colorHint));
        tgl_kembali.setText("misal : Selasa 11 Juli 2017");
        tgl_kembali.setTextColor(ContextCompat.getColor(this, R.color.colorHint));
        jam_pinjam.setText("misal : 07.00 WIB");
        jam_pinjam.setTextColor(ContextCompat.getColor(this, R.color.colorHint));
        jam_kembali.setText("misal : 12.00 WIB");
        jam_kembali.setTextColor(ContextCompat.getColor(this, R.color.colorHint));
        resetTextPrice();
        mTanggalPinjam = 0;
        mTanggalKembali = 0;
        mJamPinjam = 0;
        mJamKembali = 0;
        mWaktuPinjam = 0;
        mWaktuKembali = 0;
        mTotalBiaya = 0;
        mLamaPinjam = 0;
        isDateAlreadySet = false;
        isTimeAlreadySet = false;
        isChanged = true;

    }


}
