package com.example.asus.d_biumsemarang1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.d_biumsemarang1.adapter.HistoryAdapter;
import com.example.asus.d_biumsemarang1.data.Sewa;
import com.example.asus.d_biumsemarang1.fcm.MyFirebaseMessageService;
import com.example.asus.d_biumsemarang1.listener.DetailSewaToFragmentModel;
import com.example.asus.d_biumsemarang1.utils.ConnectionUtils;
import com.example.asus.d_biumsemarang1.utils.DialogUtils;
import com.example.asus.d_biumsemarang1.utils.TimeUtils;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;


public class DetailSewaActivity extends AppCompatActivity implements AlertDialog.OnClickListener {
    public static final String TAG = DetailSewaActivity.class.getSimpleName();

    private View mImageStatus;
    private ImageView mImgInf;
    private ImageView mImgBarcode;
    private Toolbar mToolbar;
    private TextView mStatus;
    private TextView mNama;
    private TextView nama, alamat;
    private TextView mWaktuPinjam, mWaktuKembali;
    private TextView biaya, duration, total, keterangan;
    private AlertDialog mDialog;
    private BroadcastReceiver mReceiver;
    private Toast mToast;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;

    private String mKode;
    private boolean isConnected;
    private boolean isFromNotification = false;
    private boolean isDestroyed;
    private boolean hasFinished = true;
    private boolean isInterrupted = true;
    private String mKey;
    private String status = null;
    private Sewa mData;
    private  String mTitle;
    private String mJenis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_sewa);
        isDestroyed = false;
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                isConnected = ConnectionUtils.isOnline(DetailSewaActivity.this);
                if (!isConnected && !isInterrupted) {
                    isInterrupted = true;
                    showToastError(R.string.check_connection);
                    if(mDialog != null){
                        mDialog.dismiss();
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);
        initView();
        handleIntent();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void initView() {
        mImgInf = findViewById(R.id.mImageInf);
        mImageStatus = findViewById(R.id.mImgStatus);
        mNama = findViewById(R.id.mNama);
        mToolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(mToolbar);
        mStatus = findViewById(R.id.mStatus);
        mImgBarcode = findViewById(R.id.mImgBarcode);
        nama = findViewById(R.id.nama);
        alamat = findViewById(R.id.alamat);
        mWaktuPinjam = findViewById(R.id.mWaktuPinjam);
        mWaktuKembali = findViewById(R.id.mWaktuKembali);
        biaya = findViewById(R.id.biaya);
        duration = findViewById(R.id.duration);
        total = findViewById(R.id.total);
        keterangan = findViewById(R.id.keterangan);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_delete, menu);
        MenuItem menuItem = menu.findItem(R.id.delete);
        if(mData.getStatus() != HistoryAdapter.NOT_YET){
            menuItem.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                backToMainAcitivty();
                return true;
            case R.id.delete:
                if(mDialog != null){
                    mDialog.dismiss();
                }
                mDialog = DialogUtils.buildShowDialog(this, R.string.cancel_rent,
                        getString(R.string.sure_cancel, mData.getStatus_inf().substring(2)), this);
                mDialog.show();
                return true;
            case R.id.info:
                if(mDialog != null){
                    mDialog.dismiss();
                }
                mDialog = DialogUtils.buildMessageDialog(this, R.string.information, R.string.info_barcode);
                mDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        isFromNotification = intent.getBooleanExtra(MyFirebaseMessageService.ACTION_VERIFIKASI, false);
        Log.v(TAG, "isFROMNOTIF " + isFromNotification);
        mKode = intent.getAction();
        int drawable;
        String tipe;
        if (mKode.equals(MainActivity.KODE_GEDUNG)) {
            mJenis = getString(R.string.gedung);
            tipe = "riwayat_gedung";
            drawable = R.drawable.gedung;
        } else {
            mJenis = getString(R.string.venue);
            tipe = "riwayat_venue";
            drawable = R.drawable.venue;
        }
        mImgInf.setImageResource(drawable);
        mTitle = getString(R.string.detail_rent, mJenis);
        getSupportActionBar().setTitle(mTitle);
        mNama.setText(getString(R.string.name, mJenis));
        String data = intent.getStringExtra(DetailActivity.DATA_DATABASE);
        Gson gson = new Gson();
        mData = gson.fromJson(data, new TypeToken<Sewa>() {
        }.getType());
        processData();
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference(tipe).child(mKey);
    }

    private void createBarcode() {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            int pixel = getResources().getDimensionPixelSize(R.dimen.barcode_size);
            BitMatrix bitMatrix = multiFormatWriter.encode(mKey, BarcodeFormat.QR_CODE, pixel, pixel);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            mImgBarcode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void processData() {
        mKey = mData.getKey();
        int color = 0;

        switch ((int) mData.getStatus()) {
            case HistoryAdapter.NOT_YET:
                color = ContextCompat.getColor(this, R.color.color_notYet);
                status = "Belum diverfikasi";
                break;
            case HistoryAdapter.VERIFIED:
                color = ContextCompat.getColor(this, R.color.color_verified);
                status = "Terverifikasi";
                createBarcode();
                break;
            case HistoryAdapter.REJECTED:
                color = ContextCompat.getColor(this, R.color.color_rejected);
                status = "Ditolak";
                break;
        }
        mStatus.setText(status != null ? status.toUpperCase() : "-");
        mImageStatus.setBackgroundColor(color);
        nama.setText(mData.getStatus_inf().substring(2));
        alamat.setText(mData.getAlamat());
        long wktpinjam = mData.getWaktu_pinjam();
        long wktkembali = mData.getWaktu_kembali();
        long hourpinjam = TimeUnit.MILLISECONDS.toHours(wktpinjam);
        long hourkembali = TimeUnit.MILLISECONDS.toHours(wktkembali);
        StringBuilder pinjam = new StringBuilder();
        pinjam.append(TimeUtils.getStringDate(wktpinjam)).append("\n")
                .append(TimeUtils.getStringJam((int) hourpinjam % 24));
        mWaktuPinjam.setText(pinjam.toString());
        StringBuilder kembali = new StringBuilder();
        kembali.append(TimeUtils.getStringDate(wktkembali)).append("\n")
                .append(TimeUtils.getStringJam((int) hourkembali % 24));
        mWaktuKembali.setText(kembali.toString());
        if (mData.getBiaya() == 0) {
            biaya.setText(getString(R.string.free));
        } else {
            biaya.setText(getString(R.string.hour,
                    NumberFormat.getInstance().format(mData.getBiaya())));
        }
        long lamapinjam = mData.getJam();
        long totals = mData.getTotal();
        long hari = lamapinjam / 24;
        long hour = lamapinjam % 24;
        duration.setText(getString(R.string.rent_range, hari, hour));
        if (totals == 0) {
            total.setText(getString(R.string.free));
        } else {
            total.setText(NumberFormat.getInstance().format(totals));
        }
        if (mData.getKeterangan() != null) {
            if (!mData.getKeterangan().isEmpty()) {
                if (mData.getKeterangan().trim().length() > 0) {
                    keterangan.setText(mData.getKeterangan());
                }
            }
        }

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
        isInterrupted = true;
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int id) {
        switch (id) {
            case DialogInterface.BUTTON_POSITIVE:
                proceedDelete();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                mDialog.dismiss();
                break;
            default:
                break;
        }
    }

    private void proceedDelete() {
        if (isConnected) {
            if (!hasFinished) {
                showToastError(R.string.fail_process);
            } else {
                isInterrupted = false;
                hasFinished = false;
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
                mReference.setValue(null, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (!isDestroyed) {
                            if (databaseError != null && !isInterrupted) {
                                showToastError(getString(R.string.fail_processV2, mJenis));
                                mDialog.dismiss();
                            } else if (databaseError == null) {
                                sendLocalBroadcast();
                                mDialog.dismiss();
                                buildFinishDialog();
                            }
                        }
                        isInterrupted = true;
                        hasFinished = true;
                    }
                });
            }
        } else {
            showToastError(R.string.check_connection);
        }
    }

    private void showToastError(int message) {
        if (!isDestroyed) {
            checkToast();
            mToast = Toasty.error(this, getString(message), Toast.LENGTH_SHORT);
            mToast.show();
        }
    }
    private void showToastError(String message) {
        if (!isDestroyed) {
            checkToast();
            mToast = Toasty.error(this, message, Toast.LENGTH_SHORT);
            mToast.show();
        }
    }

    private void checkToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }

    private void sendLocalBroadcast() {
        boolean isSetupFinished = DetailSewaToFragmentModel.getInstance().getSetupFinished();
        if (isSetupFinished) {
            List<Sewa> temp = DetailSewaToFragmentModel.getInstance().getmData();
            if(temp != null){
                if (temp.size() != 0) {
                    int pos = -1;
                    for (Sewa sewa : temp) {
                        pos++;
                        if (sewa.getKey().equals(mKey)) {
                            break;
                        }

                    }
                    DetailSewaToFragmentModel.getInstance().setClickPosition(pos);
                    Log.v(TAG, "" + pos);
                    if (!isDestroyed && pos != -1) {
                        Gson gson = new Gson();
                        String data = gson.toJson(mData);
                        Intent intent = new Intent(HistoryAdapter.NOTIFIY_ADAPTER);
                        intent.putExtra(DetailActivity.KODE_INF, mKode);
                        intent.putExtra(DetailActivity.DATA_DATABASE, data);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    }
                }
            }
        }
    }

    private void buildFinishDialog(){
        mDialog = new AlertDialog.Builder(this, R.style.AlertDialogCustom).setTitle(R.string.success)
                .setMessage( getString(R.string.success_cancel, mData.getStatus_inf().substring(2))).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        backToMainAcitivty();
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                       backToMainAcitivty();
                    }
                }).create();
        mDialog.show();
    }

    @Override
    public void onBackPressed() {
       backToMainAcitivty();

    }

    private void backToMainAcitivty(){
        if(isFromNotification){
            Log.v(TAG, "thisClass");
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setAction(getIntent().getAction());
            startActivity(intent);
        }
        else{
            NavUtils.navigateUpFromSameTask(DetailSewaActivity.this);
        }
    }



}
