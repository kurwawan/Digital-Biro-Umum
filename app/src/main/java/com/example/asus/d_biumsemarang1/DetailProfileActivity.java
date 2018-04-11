package com.example.asus.d_biumsemarang1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.d_biumsemarang1.utils.ConnectionUtils;
import com.example.asus.d_biumsemarang1.utils.DialogUtils;
import com.example.asus.d_biumsemarang1.utils.KeyboardUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import es.dmoral.toasty.Toasty;


public class DetailProfileActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String NAME_TYPE = "name_type";
    public static final String PASSWORD_TYPE = "password_type";
    public static final String PHONE_TYPE = "phone_type";
    public static final String CHANGE_TYPE = "change_type";
    public static final String DATA_PROFILE = "data_profile";

    private RelativeLayout mRelative;
    private TextInputLayout mWrapper;
    private LinearLayout mLinear;
    private EditText mEditText;
    private Toolbar mToolbar;
    private TextView mJudulHp;
    private Button mBtn;
    private AlertDialog mDialog;
    private BroadcastReceiver mReceiver;
    private Toast mToast;
    private EditText mEditPassword1, mEditPassword2;
    private ImageView mImgPassword1, mImgPassword2;
    private Button mBtnPassword;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String mUid;
    private boolean isDestroyed = false;

    private boolean isConnected;
    private boolean showPassword[] = new boolean[2];
    private boolean requestFocus = true;
    private String mType;
    private long mLastClickTime = 0;
    private boolean isInterrupted = true;
    private boolean hasCalled = true;
    private boolean mCounter = false;
    private String mTitle;
    private String mData;

    private PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    private Phonenumber.PhoneNumber phoneNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_profile);
        showPassword[0] = true;
        showPassword[1] = true;
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                isConnected = ConnectionUtils.isOnline(DetailProfileActivity.this);
                if (!isConnected && !isInterrupted) {
                    isInterrupted = true;
                    showToastError(R.string.check_connection);
                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);
        initView();
        initListener();
        handleIntent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (requestFocus) {
            requestFocus = false;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    KeyboardUtils.showSoftKeyboard(DetailProfileActivity.this);
                }
            }, 50);

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
        unregisterReceiver(mReceiver);
    }

    private void initView() {
        mToolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mWrapper = findViewById(R.id.mWrapper);
        mEditText = findViewById(R.id.mEditText);
        mRelative = findViewById(R.id.mRelative);
        mJudulHp = findViewById(R.id.mJudulHp);
        mBtn = findViewById(R.id.mBtn);
        mLinear = findViewById(R.id.mLinear);
        mEditPassword1 = findViewById(R.id.mEditPassword1);
        mEditPassword2 = findViewById(R.id.mEditPassword2);
        mImgPassword1 = findViewById(R.id.mImgPassword1);
        mImgPassword2 = findViewById(R.id.mImgPassword2);
        mBtnPassword = findViewById(R.id.mBtnPassword);
    }

    private void initListener() {
        mBtn.setOnClickListener(this);
        mImgPassword1.setOnClickListener(this);
        mImgPassword2.setOnClickListener(this);
        mBtnPassword.setOnClickListener(this);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        mType = intent.getStringExtra(CHANGE_TYPE);
        mData = intent.getStringExtra(DATA_PROFILE);
        changeViewAndFirebase();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500 && id != R.id.mImgPassword1 && id != R.id.mImgPassword2) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        switch (id) {
            case R.id.mBtn:
                proceedChangeDataProfile();
                break;
            case R.id.mImgPassword1:
                hideShowPassword(0);
                break;
            case R.id.mImgPassword2:
                hideShowPassword(1);
                break;
            case R.id.mBtnPassword:
                proceedChangePassword();
                break;
            default:
                break;
        }
    }

    private void changeViewAndFirebase() {
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            errorDataProfile();
        } else {
            mUser = mAuth.getCurrentUser();
            mUid = mUser.getUid();
        }
        mDatabase = FirebaseDatabase.getInstance();
        if (mType.equals(NAME_TYPE)) {
            mTitle = getString(R.string.change_name);
            mCounter = true;
            mReference = mDatabase.getReference("users").child(mUid).child("nama");
            mJudulHp.setVisibility(View.GONE);
            mEditText.setText(mData);
            mEditText.setSelection(mEditText.getText().length());
        } else if (mType.equals(PHONE_TYPE)) {
            mTitle = getString(R.string.change_phone);
            mEditText.setFilters(new InputFilter[]{});
            mEditText.setHint(R.string.number);
            if (mData != null) {
                if (mData.length() > 1)
                    mEditText.setText(mData.substring(1));
            }
            mEditText.setSelection(mEditText.getText().length());
            mEditText.setInputType(InputType.TYPE_CLASS_PHONE);
            mEditText.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
            mReference = mDatabase.getReference("users").child(mUid).child("noHp");
        } else if (mType.equals(PASSWORD_TYPE)) {
            mTitle = getString(R.string.change_password);
            mRelative.setVisibility(View.GONE);
            mLinear.setVisibility(View.VISIBLE);
        }
        getSupportActionBar().setTitle(mTitle);
        mWrapper.setCounterEnabled(mCounter);
        mBtn.setText(mTitle);

    }

    private void errorDataProfile() {
        if (!isDestroyed) {
            String message = getString(R.string.fail_fetchData);
            Toasty.error(this, message, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showToastError(int message) {
        if (!isDestroyed) {
            checkToast();
            mToast = Toasty.error(this, getString(message), Toast.LENGTH_SHORT);
            mToast.show();
        }
    }

    private void checkToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }

    private void validatePhoneNumber() {
        boolean valid;
        String newNoHp = mEditText.getText().toString();
        if (newNoHp.isEmpty()) {
            showToastMessage(getString(R.string.still_empty, getString(R.string.number)), true);
            mDialog.dismiss();
        } else {

            try {
                phoneNumber = phoneUtil.parse("0" + newNoHp, "ID");
                valid = phoneUtil.isValidNumber(phoneNumber);
                if (valid) {
                    proceedDatabase("0" + newNoHp);
                } else {
                    showToastMessage(getString(R.string.number_invalid), false);
                    mDialog.dismiss();
                }

            } catch (NumberParseException e) {
                showToastMessage(getString(R.string.number_invalid), false);
                mDialog.dismiss();
            }
        }


    }

    private void showToastMessage(String message, boolean info) {
        if (!isInterrupted) {
            isInterrupted = true;
            checkToast();
            if (info) {
                mToast = Toasty.info(this, message, Toast.LENGTH_SHORT);
            } else {
                mToast = Toasty.warning(this, message, Toast.LENGTH_SHORT);
            }

            mToast.show();
        }


    }

    private void validateName() {
        String newName = mEditText.getText().toString();
        if (newName.isEmpty() || newName.trim().length() <= 0) {
            showToastMessage(getString(R.string.still_empty, getString(R.string.name_v2)), true);
            mDialog.dismiss();
        } else {
            proceedDatabase(newName);
        }
    }

    private void proceedDatabase(String value) {
        if (!isInterrupted) {
            if (isConnected) {
                hasCalled = false;
                mReference.setValue(value, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            finish();
                        } else if (!isInterrupted) {
                            showToastError(R.string.fail_process);
                        }
                        isInterrupted = true;
                        hasCalled = true;
                        mDialog.dismiss();
                    }
                });
            } else {
                isInterrupted = true;
                showToastError(R.string.check_connection);
                mDialog.dismiss();
            }
        }

    }

    private void proceedChangePassword() {
        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        KeyboardUtils.hideSoftKeyboard(this, mEditText);
        mDialog = DialogUtils.buildShowDialogProgress(this);
        mDialog.show();
        String oldPassword = mEditPassword1.getText().toString();
        final String newPassword = mEditPassword2.getText().toString();

        if (oldPassword.isEmpty() || newPassword.isEmpty()) {
            checkToast();
            mToast = Toasty.info(this, getString(R.string.password_empty), Toast.LENGTH_SHORT, true);
            mToast.show();
            mDialog.dismiss();
            return;
        }
        AuthCredential credential = EmailAuthProvider
                .getCredential(mUser.getEmail(), oldPassword);

        // Prompt the user to re-provide their sign-in credentials
        mUser.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mUser.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mDialog.dismiss();
                                        finish();
                                    } else {
                                        handleException(task.getException());
                                    }
                                    mDialog.dismiss();
                                }
                            });
                        } else {
                            handleException(task.getException());
                            mDialog.dismiss();
                        }
                    }
                });
    }

    private void proceedChangeDataProfile() {
        KeyboardUtils.hideSoftKeyboard(this, mEditText);
        if (hasCalled) {
            mDialog = DialogUtils.buildShowDialogProgress(this);
            isInterrupted = false;
            mDialog.setCancelable(true);
            mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    isInterrupted = true;
                    mDatabase.purgeOutstandingWrites();
                }
            });
            mDialog.show();
            if (mType.equals(NAME_TYPE)) {
                validateName();
            } else if (mType.equals(PHONE_TYPE)) {
                validatePhoneNumber();
            }
        } else {
            showToastError(R.string.fail_process);
        }
    }

    private void handleException(Exception exception) {
        checkToast();
        String message;
        try {
            throw exception;
        } catch (FirebaseNetworkException e) {
            message = getString(R.string.check_connection);
            mToast = Toasty.error(this, message, Toast.LENGTH_SHORT, true);

        } catch (FirebaseAuthWeakPasswordException e) {
            message = this.getString(R.string.password_weakV2);
            mToast = Toasty.warning(this, message, Toast.LENGTH_SHORT, true);
        } catch (FirebaseAuthInvalidUserException e) {
            message = getString(R.string.mistake);
            mToast = Toasty.warning(this, message, Toast.LENGTH_SHORT, true);
        } catch (FirebaseAuthInvalidCredentialsException e) {
            message = getString(R.string.old_passwordInvalid);
            mToast = Toasty.warning(this, message, Toast.LENGTH_SHORT, true);
        } catch (Exception e) {
            message = getString(R.string.error);
            mToast = Toasty.error(this, message, Toast.LENGTH_SHORT, true);
        }
        mToast.show();
    }

    private void hideShowPassword(int pos) {
        if (showPassword[pos]) {
            showPassword[pos] = false;
            if (pos == 0) {
                mImgPassword1.setImageResource(R.drawable.visible_off);
                mEditPassword1.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                mImgPassword2.setImageResource(R.drawable.visible_off);
                mEditPassword2.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }

        } else {
            showPassword[pos] = true;
            if (pos == 0) {
                mImgPassword1.setImageResource(R.drawable.visible_on);
                mEditPassword1.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
            } else {
                mImgPassword2.setImageResource(R.drawable.visible_on);
                mEditPassword2.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
            }

        }
    }


}
