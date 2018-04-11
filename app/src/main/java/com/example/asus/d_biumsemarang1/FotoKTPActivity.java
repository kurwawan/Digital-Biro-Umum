package com.example.asus.d_biumsemarang1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.asus.d_biumsemarang1.utils.ConnectionUtils;
import com.example.asus.d_biumsemarang1.utils.DialogUtils;
import com.example.asus.d_biumsemarang1.utils.GlideApp;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import es.dmoral.toasty.Toasty;


public class FotoKTPActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = FotoKTPActivity.class.getSimpleName();
    private static final int RC_PHOTO_PICKER = 0;
    public static final String KTP = "ktp";
    public static final String KTP_DIRI = "ktp_diri";

    private Toolbar mToolbar;
    private ImageView mImgKtp;
    private TextView mNoPhoto;
    private ImageView mImgKtpUpload;
    private Button mBtnUpload;
    private Button mBtnPilih;
    private Toast mToast;
    private AlertDialog mDialog;
    private TextView mJudulKTP;
    private ProgressBar mProgress;

    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private ValueEventListener mListener;
    private UploadTask mTask;

    private String mUid;
    private String mType;
    private String mEmail;
    private String mTitle;
    private int mPrevId;
    private boolean isConnected;
    private boolean isInterrupted = true;
    private boolean hasCalled = true;
    private long mlLastClickTime;
    private boolean isDestroyed = false;
    private Uri mSelectedImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foto_ktp);
        initFirebase();
        initView();
        initListener();
    }

    private void initView(){
        mToolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(mToolbar);
        String judul;
        if(mType.equals(KTP)){
            mTitle = getString(R.string.KTP);
            judul = getString(R.string.ktp_only, mTitle);
        }
        else{
            mTitle = getString(R.string.KTP_self);
            judul = getString(R.string.ktp_self);
        }
        getSupportActionBar().setTitle(getString(R.string.change_photo, mTitle));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mJudulKTP = findViewById(R.id.mJudulKTP);
        mJudulKTP.setText(judul);
        mImgKtp = findViewById(R.id.mImgKtp);
        mImgKtpUpload = findViewById(R.id.mImgKtpUpload);
        mBtnUpload = findViewById(R.id.mBtnUpload);
        mBtnPilih = findViewById(R.id.mBtnPilih);
        mProgress = findViewById(R.id.mProgress);
        mNoPhoto = findViewById(R.id.mNoPhoto);
        mNoPhoto.setText(getString(R.string.no_photo, mTitle));
        mNoPhoto.setVisibility(View.GONE);
        changeSize();




    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    private void initListener(){
        mBtnPilih.setOnClickListener(this);
        mBtnUpload.setOnClickListener(this);

    }
    private int getWidth() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    private void changeSize(){
        int dimen = getResources().getDimensionPixelSize(R.dimen.padding_foto);
        LinearLayout.LayoutParams paramv = (LinearLayout.LayoutParams) mImgKtpUpload.getLayoutParams();
        paramv.height = getWidth() / 2;
        paramv.width = getWidth() / 2;
        FrameLayout.LayoutParams param = (FrameLayout.LayoutParams) mImgKtp.getLayoutParams();
        param.height = getWidth() - dimen;

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(mPrevId == id && SystemClock.elapsedRealtime() - mlLastClickTime < 500){
            return;
        }
        mPrevId = id;
        mlLastClickTime = SystemClock.elapsedRealtime();
        switch (id){
            case R.id.mBtnPilih:
                choosePhoto();
                break;
            case R.id.mBtnUpload:
                if(mSelectedImage == null){
                    isInterrupted = false;
                    showToastMessage(getString(R.string.upload_empty, mTitle), 1);

                }
                else{
                    mDialog = DialogUtils.buildShowDialog(this, R.string.change_photoV2,
                            getString(R.string.change_photoSure, mTitle), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int id) {
                                    switch (id){
                                        case DialogInterface.BUTTON_POSITIVE:
                                            uploadFoto();
                                            break;
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            mDialog.dismiss();
                                            break;
                                    }

                                }
                            });
                    mDialog.show();
                }

                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
                mSelectedImage = data.getData();
                mImgKtpUpload.setImageURI(mSelectedImage);
        }

    }

    private void uploadFoto(){
        isInterrupted = false;
        isConnected = ConnectionUtils.isOnline(this);
        if(!isConnected){
            showToastMessage(getString(R.string.check_connection), 2);
            return;
        }
        if(hasCalled){
            hasCalled = false;
            mTask = mStorageReference.putFile(mSelectedImage);
            mTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.v(TAG, taskSnapshot.getDownloadUrl().toString());
                    hasCalled = false;
                    mDatabaseReference.setValue(taskSnapshot.getDownloadUrl().toString(), new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null){
                                showToastMessage(getString(R.string.fail_upload, mTitle),2);
                            }
                            mDialog.dismiss();
                            mTask = null;
                            hasCalled = true;
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if(!isDestroyed){
                        showToastMessage(getString(R.string.fail_upload, mTitle),2);
                    }
                    mDialog.dismiss();
                    mTask = null;
                    hasCalled = true;
                }
            });
            mDialog = DialogUtils.buildShowDialogProgress(this);
            mDialog.setCancelable(true);
            mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    isInterrupted = true;
                    if(mTask != null){
                        mTask.cancel();
                        hasCalled = true;
                    }
                }
            });
            mDialog.show();
        }
        else{
            showToastMessage(getString(R.string.fail_process), 2);
        }

    }

    private void choosePhoto(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Pilih Foto dengan"), RC_PHOTO_PICKER);
    }

    private void initFirebase(){
        Intent intent = getIntent();
        mType = intent.getAction();
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() == null){
            errorDataProfile();
        }
        else {
            mUid = mAuth.getCurrentUser().getUid();
            mEmail = mAuth.getCurrentUser().getEmail();
        }
        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReferenceFromUrl("gs://digital-biroumum-semarang.appspot.com").child("users/" + mEmail).child(mUid).child(mType);
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference().child("users").child(mUid).child(mType);
        attachFirebaseDatabaseListener();

    }

    private void errorDataProfile() {
        if (!isDestroyed) {
            String message = getString(R.string.fail_fetchData);
            mToast = Toasty.error(this, message, Toast.LENGTH_SHORT);
            mToast.show();
            finish();
        }
    }

    private void checkToast(){
        if(mToast != null){
            mToast.cancel();
        }
    }

    private void showToastMessage(String message,int tipe) {
        if (!isInterrupted) {
            isInterrupted = true;
            checkToast();
            if(tipe == 1){
                mToast = Toasty.info(this, message, Toast.LENGTH_SHORT);
            }
            else if(tipe == 2){
                mToast = Toasty.error(this, message, Toast.LENGTH_SHORT);
            }
            mToast.show();
        }


    }

    private void attachFirebaseDatabaseListener(){
        mListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mProgress.setVisibility(View.VISIBLE);
                if(dataSnapshot.exists()){
                    mNoPhoto.setVisibility(View.GONE);
                    GlideApp.with(FotoKTPActivity.this).load(dataSnapshot.getValue(String.class)).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                           .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    mProgress.setVisibility(View.GONE);
                                    return false;
                                }
                            }).fitCenter().into(mImgKtp);
                }


                else{
                    mImgKtp.setImageResource(0);
                    mNoPhoto.setVisibility(View.VISIBLE);
                    mProgress.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabaseReference.addValueEventListener(mListener);


    }
    private void detachFirebaseDatabaseListener(){
        if(mListener != null){
            mDatabaseReference.removeEventListener(mListener);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detachFirebaseDatabaseListener();
        isDestroyed = true;
        if(mTask != null){
            mTask.cancel();
            hasCalled = true;
        }
    }

}
