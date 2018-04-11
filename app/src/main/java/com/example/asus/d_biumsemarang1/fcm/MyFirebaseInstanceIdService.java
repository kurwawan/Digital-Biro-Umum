package com.example.asus.d_biumsemarang1.fcm;

import android.preference.PreferenceManager;

import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by ASUS on 11/14/2017.
 */
//notifikasi sewa berhasil
public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
    public static final String TOKEN_ID = "token_id";
    public static final String TOKEN_CHANGED = "token_changed";
    @Override
    public void onTokenRefresh() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(TOKEN_CHANGED, true).apply();

    }
}
