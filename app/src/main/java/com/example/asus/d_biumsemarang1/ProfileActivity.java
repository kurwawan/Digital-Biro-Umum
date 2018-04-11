package com.example.asus.d_biumsemarang1;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.example.asus.d_biumsemarang1.utils.DialogUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import es.dmoral.toasty.Toasty;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener, AlertDialog.OnClickListener {

    public static final String TAG = ProfileActivity.class.getSimpleName();
    private Toolbar mToolbar;
    private TextView photoProfile;
    private TextView mEmail, mPassword;
    private TextView nama;
    private LinearLayout mNama;
    private LinearLayout mHp;
    private TextView hp;
    private LinearLayout mFoto;
    private TextView foto;
    private LinearLayout mFotoSelf;
    private TextView fotoSelf;
    private TextView judulFoto;
    private TextView judulFotoSelf;
    private TextView mLogOut;
    private TextView mEmailVerification;
    private AlertDialog mDialog;
    private NotificationManager mNotificationManager;
    private Toast mToast;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private ValueEventListener mListener;
    private FirebaseAuth mAuth;

    private String mUid;
    private String sNama;
    private String sHp;
    private boolean hasCalled = false;
    private boolean isFirstTime = true;
    private boolean isDestroyed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_activity);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        initView();
        initListener();
        initFirebase();
    }

    private void initView() {
        mToolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getString(R.string.profile));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        photoProfile = findViewById(R.id.photoProfile);
        mEmail = findViewById(R.id.mEmail);
        mPassword = findViewById(R.id.mPassword);
        mNama = findViewById(R.id.mNama);
        nama = findViewById(R.id.nama);
        mHp = findViewById(R.id.mHp);
        hp = findViewById(R.id.hp);
        mFoto = findViewById(R.id.mFoto);
        judulFoto = findViewById(R.id.judulFoto);
        judulFoto.setText(getString(R.string.ktp_only, getString(R.string.KTP)));
        foto = findViewById(R.id.foto);
        mFotoSelf = findViewById(R.id.mFotoSelf);
        judulFotoSelf = findViewById(R.id.judulFotoSelf);
        judulFotoSelf.setText(getString(R.string.ktp_only, getString(R.string.KTP_self)));
        fotoSelf = findViewById(R.id.fotoSelf);
        mLogOut = findViewById(R.id.mLogOut);
        mEmailVerification = findViewById(R.id.mEmailVerification);
    }


    private void initListener() {
        mPassword.setOnClickListener(this);
        mNama.setOnClickListener(this);
        mHp.setOnClickListener(this);
        mFoto.setOnClickListener(this);
        mFotoSelf.setOnClickListener(this);
        mLogOut.setOnClickListener(this);
        mEmailVerification.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id != R.id.mLogOut && !hasCalled) {
            return;
        }
        switch (id) {
            case R.id.mPassword:
                intentToDetailProfile(DetailProfileActivity.PASSWORD_TYPE, "");
                break;
            case R.id.mNama:
                intentToDetailProfile(DetailProfileActivity.NAME_TYPE, sNama);
                break;
            case R.id.mHp:
                intentToDetailProfile(DetailProfileActivity.PHONE_TYPE, sHp);
                break;
            case R.id.mFoto:
                intentToFotoKTP(FotoKTPActivity.KTP);
                break;
            case R.id.mFotoSelf:
                intentToFotoKTP(FotoKTPActivity.KTP_DIRI);
                break;
            case R.id.mLogOut:
                if(mDialog != null){
                    mDialog.dismiss();
                }
                mDialog = DialogUtils.buildShowDialog(this, R.string.dialog_exitTitle,
                        R.string.dialog_logOut, this);
                mDialog.show();
                break;
            case R.id.mEmailVerification:
                if(mDialog != null){
                    mDialog.dismiss();
                }
                mDialog = DialogUtils.buildShowDialog(this, R.string.email_verifTitle, R.string.email_verifContent, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        switch (id) {
                            case DialogInterface.BUTTON_POSITIVE:
                                mDialog = DialogUtils.buildShowDialogProgress(ProfileActivity.this);
                                mDialog.show();
                                sendEmailVerification();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                mDialog.dismiss();
                                break;
                            default:
                                break;
                        }
                    }
                });
                mDialog.show();
                break;
            default:
                break;

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isFirstTime) {
            attachListener();
        }
        isFirstTime = false;
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            errorDataProfile();
        } else {
            mEmail.setText(mAuth.getCurrentUser().getEmail());
            mUid = mAuth.getCurrentUser().getUid();
        }
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference("users").child(mUid);
        attachListener();
    }

    private void attachListener() {
        mListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    proceedDataProfile(dataSnapshot);
                    hasCalled = true;
                } else {
                    hasCalled = false;
                    errorDataProfile();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hasCalled = false;
                errorDataProfile();

            }
        };
        mReference.addListenerForSingleValueEvent(mListener);
    }

    private void proceedDataProfile(DataSnapshot data) {
        sNama = data.child("nama").getValue(String.class);
        if (sNama != null) {
            if (sNama.trim().length() > 0) {
                nama.setText(sNama);
                createInitialProfilePicture(sNama);
            } else {
                nama.setText(getString(R.string.hasnt_input));
                createInitialProfilePicture(mEmail.getText().toString());
            }
        } else {
            nama.setText(getString(R.string.hasnt_input));
            createInitialProfilePicture(mEmail.getText().toString());
        }
        sHp = (String) data.child("noHp").getValue(Object.class);
        if (sHp != null) {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber phoneNumber = null;
            try {
                phoneNumber = phoneUtil.parse(sHp, "ID");
                hp.setText(phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL));
            } catch (NumberParseException e) {
                hp.setText(getString(R.string.hasnt_input));
                System.err.println("NumberParseException was thrown: " + e.toString());
            }

        } else {
            hp.setText(getString(R.string.hasnt_input));
        }
        if (data.child("ktp").exists()) {

            foto.setText(getString(R.string.foto_ready));
        } else {
            foto.setText(getString(R.string.foto_notReady));
        }
        if (data.child("ktp_diri").exists()) {

            fotoSelf.setText(getString(R.string.foto_ready));
        } else {
            fotoSelf.setText(getString(R.string.foto_notReady));
        }

    }

    private void errorDataProfile() {
        if (!isDestroyed) {
            String message = getString(R.string.fail_fetchData);
            mToast = Toasty.error(this, message, Toast.LENGTH_SHORT);
            mToast.show();
            finish();
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int id) {
        switch (id) {
            case DialogInterface.BUTTON_POSITIVE:
                mAuth.signOut();
                mNotificationManager.cancelAll();
                Intent intent = new Intent(this, LoginScreenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                mDialog.dismiss();
                break;
            default:
                break;
        }
    }

    private void createInitialProfilePicture(String initial) {
        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(mEmail.getText().toString());
        String split[];
        split = initial.split(" ");
        String letter = String.valueOf(split[0].charAt(0));
        if(split.length > 1){
            letter += String.valueOf(split[split.length - 1].charAt(0));
        }
        ((GradientDrawable)photoProfile.getBackground()).setColor(color);
        photoProfile.setText(letter.toUpperCase());

    }

    private void intentToDetailProfile(String type, String data) {
        Intent intent = new Intent(this, DetailProfileActivity.class);
        intent.putExtra(DetailProfileActivity.CHANGE_TYPE, type);
        intent.putExtra(DetailProfileActivity.DATA_PROFILE, data);
        startActivity(intent);
    }

    private void sendEmailVerification() {
        mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mDialog.dismiss();
                if(task.isSuccessful()){
                    mDialog = DialogUtils.buildMessageDialog(ProfileActivity.this, R.string.success, R.string.email_verifSuccess);
                    mDialog.show();
                }
                else {
                    if(mToast != null){
                        mToast.cancel();
                    }
                    handleException(task.getException());
                }
            }
        });
    }

    private void handleException(Exception exception) {
        String message;
        try {
            throw exception;
        } catch (Exception e) {
            if (e.getMessage().contains("Network")) {
                message = this.getString(R.string.check_connection);
                mToast = Toasty.error(this, message, Toast.LENGTH_SHORT, true);
            }
            else{
                message = getString(R.string.email_verifFail);
                mToast = Toasty.error(this, message, Toast.LENGTH_SHORT, true);
            }

        }
        mToast.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
    }

    private void intentToFotoKTP(String action){
        Intent intent = new Intent(this, FotoKTPActivity.class);
        intent.setAction(action);
        startActivity(intent);
    }
}
