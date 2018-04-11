package com.example.asus.d_biumsemarang1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.d_biumsemarang1.fcm.MyFirebaseInstanceIdService;
import com.example.asus.d_biumsemarang1.utils.ConnectionUtils;
import com.example.asus.d_biumsemarang1.utils.DialogUtils;
import com.example.asus.d_biumsemarang1.utils.KeyboardUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import es.dmoral.toasty.Toasty;

/**
 * Created by ASUS on 10/23/2017.
 */

public class LoginScreenActivity extends AppCompatActivity implements View.OnClickListener, DialogInterface.OnClickListener {

    public static final String TAG = LoginScreenActivity.class.getSimpleName();
    public static final String EXTRA_LOGIN = "extra_login";
    public static final String LOGIN_PREF = "login_pref";
    public static final String LAST_LOGIN = "last_login";
    public static final String SHOW_DIALOG = "show_dialog";
    public static final String TIME = "time";

    private ImageView mImgPassword;
    private EditText mEditPassword;
    private EditText mEditEmail;
    private Button mBtnMasuk;
    private TextView mTextLupa;
    private TextView mTextDaftar;
    private Toast mToast;
    private AlertDialog mDialog;
    private BroadcastReceiver mReceiver;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private ValueEventListener mEventListener;
    private DatabaseReference mReference;

    private boolean isSuccess = false;
    private boolean showPassword = true;
    private long mLastClickTime = 0;
    private String mUid;
    private boolean isInterrupted = false;
    private boolean isFinished = false;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginscreen);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference("users");
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isNotConnected = !ConnectionUtils.isOnline(LoginScreenActivity.this);
                if (isNotConnected && mAuth.getCurrentUser() != null && !isFinished) {
                    mAuth.signOut();
                    isInterrupted = true;
                    isFinished = true;
                    authenticationResult(false);
                    if(mDialog != null){
                        mDialog.dismiss();
                    }
                } else if (!isNotConnected) {
                    mDatabase.purgeOutstandingWrites();
                }
            }
        };
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);
        initView();
        initViewListener();
        handleIntent();
    }

    private void initView() {
        mImgPassword = findViewById(R.id.mImgPassword);
        mEditPassword = findViewById(R.id.mEditPassword);
        mBtnMasuk = findViewById(R.id.mBtnMasuk);
        mTextLupa = findViewById(R.id.mTextLupa);
        mEditEmail = findViewById(R.id.mEditEmail);
        mTextDaftar = findViewById(R.id.mTextDaftar);
        mEditEmail.requestFocus();
    }

    private void initViewListener() {
        mImgPassword.setOnClickListener(this);
        mBtnMasuk.setOnClickListener(this);
        mTextLupa.setOnClickListener(this);
        mTextDaftar.setOnClickListener(this);
    }

    private void prosesData() {
        String email = mEditEmail.getText().toString();
        String password = mEditPassword.getText().toString();
        if (email.isEmpty() || password.isEmpty()) {
            mDialog.dismiss();
            checkToast();
            String kosong = this.getString(R.string.loginDaftar_empty);
            mToast = Toasty.info(this, kosong, Toast.LENGTH_SHORT);
            mToast.show();
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                PreferenceManager.getDefaultSharedPreferences(LoginScreenActivity.this).edit()
                                        .putBoolean(LOGIN_PREF, false).apply();
                                isInterrupted = false;
                                mUid = mAuth.getCurrentUser().getUid();
                                doAuthentication();
                                DialogUtils.dialogOtentifikasi(mDialog);
                            } else {
                                // If sign in fails, display a message to the user.
                                checkToast();
                                handleException(task.getException());
                                mDialog.dismiss();
                            }
                        }
                    });
        }
    }


    @Override
    public void onClick(View view) {
        if (view.getId() != R.id.mImgPassword) {
            KeyboardUtils.hideSoftKeyboard(this, view);
        }
        switch (view.getId()) {
            case R.id.mImgPassword:
                hideShowPassword();
                break;
            case R.id.mBtnMasuk:
                if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                mDialog = DialogUtils.buildShowDialogProgress(this);
                mDialog.show();
                isFinished = false;
                prosesData();
                break;
            case R.id.mTextLupa:
                Intent intent = new Intent(this, ResetScreenActivity.class);
                startActivity(intent);
                break;
            case R.id.mTextDaftar:
                Intent intent1 = new Intent(this, SignUpActivity.class);
                startActivity(intent1);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void hideShowPassword() {
        if (showPassword) {
            showPassword = false;
            mImgPassword.setImageResource(R.drawable.visible_off);
            mEditPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            showPassword = true;
            mImgPassword.setImageResource(R.drawable.visible_on);
            mEditPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
        }
    }



    @Override
    public void onClick(DialogInterface dialog, int id) {
        switch (id) {
            case DialogInterface.BUTTON_POSITIVE:
                finish();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                dialog.dismiss();
                break;
            default:
                break;
        }
    }

    private void handleException(Exception exception) {
        String message;
        try {
            throw exception;
        } catch (FirebaseNetworkException e) {
            message = this.getString(R.string.check_connection);
            mToast = Toasty.error(this, message, Toast.LENGTH_SHORT, true);

        } catch (FirebaseAuthInvalidUserException e) {
            message = this.getString(R.string.login_invalid);
            mToast = Toasty.warning(this, message, Toast.LENGTH_SHORT, true);
        } catch (FirebaseAuthInvalidCredentialsException e) {
            message = this.getString(R.string.login_invalid);
            mToast = Toasty.warning(this, message, Toast.LENGTH_SHORT, true);
        } catch (Exception e) {
            message = this.getString(R.string.error);
            mToast = Toasty.error(this, message, Toast.LENGTH_SHORT, true);
        }
        mToast.show();
    }

    private void checkToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }


    private void doAuthentication() {
        mEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                isFinished = true;
                isSuccess = dataSnapshot.hasChild("login") && dataSnapshot.hasChild("email");
                authenticationResult(isSuccess);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (!isInterrupted) {
                    authenticationResult(false);
                }
            }
        };
        mReference.child(mUid).addListenerForSingleValueEvent(mEventListener);
    }

    private void authenticationResult(boolean success) {
        if (success) {
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putBoolean(LOGIN_PREF, true).apply();
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putBoolean(MyFirebaseInstanceIdService.TOKEN_CHANGED, true).apply();
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(EXTRA_LOGIN, true);
            startActivity(intent);
            mDialog.dismiss();
            finish();
        } else {
            mAuth.signOut();
            String message = getString(R.string.authentication_fail);
            mToast = Toasty.error(this, message, Toast.LENGTH_SHORT);
            mToast.show();
            mDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        int value = intent.getIntExtra(SHOW_DIALOG, -1);
        switch (value) {
            case 0:
                int time = intent.getIntExtra(TIME, 0);
                String message;
                if(time <= 0){
                    message = getString(R.string.quick_mt);
                }
                else if(time < 60){
                    message = getString(R.string.minutes_mt, time);
                }
                else{
                    double hour = (double)time / 60;
                    message = getString(R.string.hour_mt, Math.round(hour));
                }
                DialogUtils.buildMessageDialog(this, R.string.maintenance, getString(R.string.info_maintenance, message)).show();
                break;
            case 1:
                DialogUtils.buildMessageDialog(this, R.string.information, R.string.no_identity).show();
                break;
            case 2:
                DialogUtils.buildMessageDialog(this, R.string.information, R.string.mistake).show();
                break;
            case 3:
                DialogUtils.buildMessageDialog(this, R.string.information, R.string.login_double).show();
                break;
            default:
                break;
        }
    }
}

