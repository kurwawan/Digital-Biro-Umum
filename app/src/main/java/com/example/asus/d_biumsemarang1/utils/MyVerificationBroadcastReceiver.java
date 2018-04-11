package com.example.asus.d_biumsemarang1.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.asus.d_biumsemarang1.DetailActivity;
import com.example.asus.d_biumsemarang1.DetailSewaActivity;
import com.example.asus.d_biumsemarang1.MainActivity;
import com.example.asus.d_biumsemarang1.fcm.MyFirebaseMessageService;
import com.example.asus.d_biumsemarang1.listener.MainActivityListener;

/**
 * Created by ASUS on 11/26/2017.
 */
//Verifikasi setelah sewa
public class MyVerificationBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent mainSewa = new Intent();
        mainSewa.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mainSewa.putExtra(DetailActivity.DATA_DATABASE,
                intent.getStringExtra(DetailActivity.DATA_DATABASE));
        mainSewa.setAction(intent.getStringExtra(MyFirebaseMessageService.VERIFIKASI_TYPE));
        mainSewa.putExtra(MyFirebaseMessageService.ACTION_VERIFIKASI, true);
        if(MainActivityListener.getInstance().isCreated()){
            mainSewa.setClass(context, DetailSewaActivity.class);
        }
        else{
            mainSewa.setClass(context, MainActivity.class);
        }
        context.startActivity(mainSewa);

    }


}
