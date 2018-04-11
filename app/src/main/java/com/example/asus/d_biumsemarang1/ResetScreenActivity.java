package com.example.asus.d_biumsemarang1;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.d_biumsemarang1.utils.DialogUtils;
import com.example.asus.d_biumsemarang1.utils.KeyboardUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import es.dmoral.toasty.Toasty;

/**
 * Created by ASUS on 10/24/2017.
 */

public class ResetScreenActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = ResetScreenActivity.class.getSimpleName();

    private TextView mTextIngat;
    private EditText mEditEmail;
    private Button mBtnReset;
    private Toast mToast;
    private AlertDialog mDialog;


    private FirebaseAuth mAuth;

    private long mLastClickTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_screen);
        mAuth = FirebaseAuth.getInstance();
        initView();
        initViewListener();
    }

    private void initView() {
        mTextIngat = findViewById(R.id.mTextIngat);
        mBtnReset = findViewById(R.id.mBtnReset);
        mEditEmail = findViewById(R.id.mEditEmail);
        mEditEmail.requestFocus();
    }

    private void initViewListener() {
        mTextIngat.setOnClickListener(this);
        mBtnReset.setOnClickListener(this);
    }

    private void prosesData() {
        String email = mEditEmail.getText().toString();
        if (email.isEmpty()) {
            mDialog.dismiss();
            checkToast();
            String kosong = this.getString(R.string.reset_empty);
            mToast = Toasty.info(this, kosong, Toast.LENGTH_SHORT);
            mToast.show();
        } else {
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                sendResetEmailSucces();

                            } else {
                                handleException(task.getException());
                                mDialog.dismiss();
                            }
                        }
                    });
        }
    }

    @Override
    public void onClick(View view) {
        KeyboardUtils.hideSoftKeyboard(this, view);
        switch (view.getId()) {
            case R.id.mTextIngat:
                Intent intent = new Intent(this, LoginScreenActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.mBtnReset:
                if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                mDialog = DialogUtils.buildShowDialogProgress(this);
                mDialog.show();
                prosesData();
                break;
            default:
                break;
        }
    }

    private void checkToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }

    private void handleException(Exception exception) {
        String message;
        try {
            checkToast();
            throw exception;
        } catch (Exception e) {
            String exceptionMessage = e.getMessage();
            if (exceptionMessage.contains("Network")) {
                message = this.getString(R.string.check_connection);
                mToast = Toasty.error(this, message, Toast.LENGTH_SHORT, true);
            } else if (exceptionMessage.contains("EMAIL") || exceptionMessage.contains("record")) {
                message = this.getString(R.string.email_invalid);
                mToast = Toasty.warning(this, message, Toast.LENGTH_SHORT, true);
            } else {
                message = this.getString(R.string.error);
                mToast = Toasty.error(this, message, Toast.LENGTH_SHORT, true);
            }
            Log.v(TAG, e.getMessage());
        }
        mToast.show();
    }

    private void sendResetEmailSucces(){
        mDialog.dismiss();
        mDialog = new AlertDialog.Builder(this, R.style.AlertDialogCustom).setTitle(R.string.success)
                .setMessage(R.string.email_resetSuccess).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        intentToLoginScreen();
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        intentToLoginScreen();
                    }
                }).create();
        mDialog.show();
    }

    private void intentToLoginScreen(){
        Intent intent = new Intent(this, LoginScreenActivity.class);
        startActivity(intent);
        finish();
    }
}
