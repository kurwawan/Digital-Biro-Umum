package com.example.asus.d_biumsemarang1.fcm;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.example.asus.d_biumsemarang1.DetailActivity;
import com.example.asus.d_biumsemarang1.MainActivity;
import com.example.asus.d_biumsemarang1.R;
import com.example.asus.d_biumsemarang1.data.Sewa;
import com.example.asus.d_biumsemarang1.utils.TimeUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by ASUS on 11/14/2017.
 */

public class MyFirebaseMessageService extends FirebaseMessagingService {

    public static final String CHANNEL_SEWA = "channel_sewa";
    public static final String CHANNEL_VERIFIKASI = "channel_verifikasi";
    public static final String GROUP_NOTIFICATION = "group_notification";
    public static final String VERIFIKASI_TYPE = "verifikasi_type";
    public static final String ACTION_VERIFIKASI = "com.example.asus.d_biumsemarang1.verification";

    private FirebaseAuth mAuth;
    private String mUid;
    private String mChannel;
    private Map<String, String> mData;
    private String waktuPinjam, waktuKembali;

    private NotificationManager mNotificationManager;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        mAuth = FirebaseAuth.getInstance();
        mData = remoteMessage.getData();
        if (mAuth.getCurrentUser() != null && mData.size() > 0) {
            mUid = mAuth.getCurrentUser().getUid();
            if (mData.get("to").equals(mUid) &&
                    Long.parseLong(mData.get("waktu_pinjam")) > TimeUtils.ubahKeFormatWIB(System.currentTimeMillis())) {
                mChannel = mData.get("channel");
                buildNotification();
            }

        }


    }

    private void buildNotification() {
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            buildNotificationChannel();
        }
        int uniqueID =  (int)((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, mChannel);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(mData.get("tipe"));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //for summary
        builder.setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setGroup(GROUP_NOTIFICATION).setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.drawable.logo).setContentText(getString(R.string.notification_new))
                .setContentIntent(pendingIntent).setGroupSummary(true).setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        //for content
        NotificationCompat.Builder builder1 = new NotificationCompat.Builder(this, mChannel);
        builder1.setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setGroup(GROUP_NOTIFICATION)
                .setSmallIcon(R.drawable.logo).setPriority(NotificationCompat.PRIORITY_HIGH).setSound(defaultSoundUri)
                .setAutoCancel(true);
        String content = null, bigTitle = null, nama, alamat , title = null;
        nama = mData.get("nama").substring(2);
        alamat = mData.get("alamat");
        if(mChannel.equals(CHANNEL_SEWA)){
            PendingIntent pendingIntentContentSewa = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder1.setContentIntent(pendingIntentContentSewa);
            if(mData.get("tipe").equals(MainActivity.KODE_GEDUNG)){
                title = getString(R.string.rent_v2, getString(R.string.gedung));
            }
            else {
                title = getString(R.string.rent_v2, getString(R.string.venue));
            }
            proceedTanggal();
            if(mData.get("status").equals("sukses")){
                content = getString(R.string.notificaiton_success, nama, alamat, waktuPinjam, waktuKembali);
                bigTitle = getString(R.string.success_title, title);
            }
            else {
                content = getString(R.string.notification_fail, nama ,alamat, waktuPinjam, waktuKembali);
                bigTitle = getString(R.string.fail_title, title);
            }
            builder1.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(bigTitle).bigText(content)).setContentText(content);
        }
        else if(mChannel.equals(CHANNEL_VERIFIKASI)) {
            Intent intent1 = new Intent(ACTION_VERIFIKASI);
            intent1.setPackage(getPackageName());
            intent1.putExtra(VERIFIKASI_TYPE, mData.get("tipe"));
            intent1.putExtra(DetailActivity.DATA_DATABASE, createDataSewa());
            PendingIntent pendingIntentContentVerfikasi = PendingIntent.getBroadcast(this, 2,
                    intent1,PendingIntent.FLAG_UPDATE_CURRENT);
            builder1.setContentIntent(pendingIntentContentVerfikasi);
            if(mData.get("tipe").equals(MainActivity.KODE_GEDUNG)){
                title = getString(R.string.verification, getString(R.string.gedung));
            }
            else {
                title = getString(R.string.verification, getString(R.string.venue));
            }
            if(mData.get("status").equals("1")){
                content = getString(R.string.success_verificationContent, nama, alamat);
                bigTitle = getString(R.string.success_verification);
            }
            else {
                content = getString(R.string.fail_verificationContent, nama ,alamat);
                bigTitle = getString(R.string.fail_verification);
            }
        }
        builder1.setContentTitle(title);
        builder1.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(bigTitle).bigText(content)).setContentText(content);


        mNotificationManager.notify(0, builder.build());
        mNotificationManager.notify(uniqueID, builder1.build());

    }

    @TargetApi(26)
    private void buildNotificationChannel() {
        if(mNotificationManager.getNotificationChannel(mChannel) == null){
            String name;
            String description;
            if (mChannel.equals(CHANNEL_SEWA)) {
                name = getString(R.string.rent);
                description = getString(R.string.rent_desc);
            } else {
                name = getString(R.string.verifikasi);
                description = getString(R.string.verifikasi_desc);
            }

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(this.mChannel, name, importance);
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    private void proceedTanggal(){
        long waktupinjam = Long.parseLong(mData.get("waktu_pinjam"));
        long waktukembali = Long.parseLong(mData.get("waktu_kembali"));
        long hourpinjam = TimeUnit.MILLISECONDS.toHours(waktupinjam);
        long hourkembali = TimeUnit.MILLISECONDS.toHours(waktukembali);
        waktuPinjam = TimeUtils.getStringDate(waktupinjam) + " " + TimeUtils.getStringJam((int) hourpinjam % 24);
        waktuKembali = TimeUtils.getStringDate(waktukembali) + " " + TimeUtils.getStringJam((int) hourkembali % 24);
    }

    private String createDataSewa(){
        Sewa sewa = new Sewa();
        sewa.setKey(mData.get("key"));
        sewa.setStatus_inf(mData.get("nama"));
        sewa.setAlamat(mData.get("alamat"));
        sewa.setStatus(Integer.parseInt(mData.get("status")));
        sewa.setWaktu_pinjam(Long.parseLong(mData.get("waktu_pinjam")));
        sewa.setWaktu_kembali(Long.parseLong(mData.get("waktu_kembali")));
        sewa.setBiaya(Long.parseLong(mData.get("biaya")));
        sewa.setJam(Long.parseLong(mData.get("jam")));
        sewa.setTotal(Long.parseLong(mData.get("total")));
        sewa.setKeterangan(mData.get("keterangan"));
        Gson gson = new Gson();
        return gson.toJson(sewa);
    }

}
