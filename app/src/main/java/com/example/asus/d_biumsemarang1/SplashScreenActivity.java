package com.example.asus.d_biumsemarang1;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by ASUS on 10/23/2017.
 */
/*
This class is for splash screen
 */
public class SplashScreenActivity extends AppCompatActivity {

    public static final String TAG = SplashScreenActivity.class.getSimpleName();

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        boolean isLogin = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(LoginScreenActivity.LOGIN_PREF, false);
        //check if User has login yet or not
        if (mAuth.getCurrentUser() == null || !isLogin) {
            Intent intent = new Intent(this, LoginScreenActivity.class);
            startActivity(intent);
            if (mAuth.getCurrentUser() != null) {
                Log.v(TAG,"NOT NULL");
                mAuth.signOut();
            }
            Log.v(TAG,"MAYBE NULL");

        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        Log.v(TAG,"onCreate");
        //destroy this activity
        finish();

    }


}
