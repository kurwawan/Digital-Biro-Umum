package com.example.asus.d_biumsemarang1;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.d_biumsemarang1.utils.DialogUtils;
import com.example.asus.d_biumsemarang1.utils.KeyboardUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import es.dmoral.toasty.Toasty;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = SignUpActivity.class.getSimpleName();
    private EditText mEditEmail;
    private EditText mEditPassword;
    private Button mBtnDaftar;
    private TextView mTextIngat;
    private ImageView mImgPassword;
    private AlertDialog mDialog;
    private Toast mToast;

    private FirebaseAuth mAuth;

    private boolean showPassword = true;
    private long mLastClickTime = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        initView();
        initListener();
    }

    private void initView() {
        mEditEmail = findViewById(R.id.mEditEmail);
        mEditPassword = findViewById(R.id.mEditPassword);
        mBtnDaftar = findViewById(R.id.mBtnDaftar);
        mTextIngat = findViewById(R.id.mTextIngat);
        mImgPassword = findViewById(R.id.mImgPassword);
    }

    private void initListener() {
        mBtnDaftar.setOnClickListener(this);
        mTextIngat.setOnClickListener(this);
        mImgPassword.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.mImgPassword:
                hideShowPassword();
                break;
            case R.id.mBtnDaftar:
                KeyboardUtils.hideSoftKeyboard(this, view);
                if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                mDialog = DialogUtils.buildShowDialogProgress(this);
                mDialog.show();
                createAccount();
                break;
            case R.id.mTextIngat:
                KeyboardUtils.hideSoftKeyboard(this, view);
                Intent intent = new Intent(this, LoginScreenActivity.class);
                startActivity(intent);
                finish();
                break;
            default:
                break;
        }
    }

    private void createAccount() {
        String email = mEditEmail.getText().toString();
        String password = mEditPassword.getText().toString();
        String message;
        if (email.isEmpty() || password.isEmpty()) {
            checkToast();
            message = getString(R.string.loginDaftar_empty);
            Log.v(TAG, message);
            mToast = Toasty.info(this, message, Toast.LENGTH_SHORT);
            mToast.show();
            mDialog.dismiss();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        sendEmailVerification();
                    } else {
                        checkToast();
                        handleException(task.getException());
                        mDialog.dismiss();
                    }
                }
            });
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
            throw exception;
        } catch (FirebaseNetworkException e) {
            message = this.getString(R.string.check_connection);
            mToast = Toasty.error(this, message, Toast.LENGTH_SHORT, true);

        } catch (FirebaseAuthWeakPasswordException e) {
            message = this.getString(R.string.password_weak);
            mToast = Toasty.warning(this, message, Toast.LENGTH_SHORT, true);
        } catch (FirebaseAuthInvalidCredentialsException e) {
            message = this.getString(R.string.email_invalid);
            mToast = Toasty.warning(this, message, Toast.LENGTH_SHORT, true);
        } catch (FirebaseAuthUserCollisionException e) {
            message = this.getString(R.string.already_registered);
            mToast = Toasty.warning(this, message, Toast.LENGTH_SHORT, true);
        } catch (Exception e) {
            message = this.getString(R.string.error);
            mToast = Toasty.error(this, message, Toast.LENGTH_SHORT, true);
        }
        mToast.show();
    }

    private void intentToLoginScreen() {
        Intent intent = new Intent(this, LoginScreenActivity.class);
        startActivity(intent);
        mDialog.dismiss();
        finish();
    }

    private void createAccountSuccess() {
        mDialog.dismiss();
        mDialog = new AlertDialog.Builder(this, R.style.AlertDialogCustom).setTitle(R.string.success)
                .setMessage(R.string.account).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
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

    private void sendEmailVerification() {
        mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mAuth.signOut();
                createAccountSuccess();
            }
        });
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
}
