package com.example.asus.d_biumsemarang1.utils;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by ASUS on 11/13/2017.
 */

public class TimeUtils {

    public static final long DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1);
    public static final long HOUR_IN_MILLIS = TimeUnit.HOURS.toMillis(1);

    private static String [] mHari = {"Minggu","Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu"};
    private static String [] mBulan = {"Januari","Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus"
    , "September", "Oktober", "November", "Desember"};


    public static long getMinTanggal(){
        long utcDate = System.currentTimeMillis();
        long millisecondDalamWIB = ubahKeFormatWIB(utcDate);
        long millisecondDariWaktuAwalSampaiHariIni = DAY_IN_MILLIS * jumlahHariSejakWaktuAwalUTC(millisecondDalamWIB);
        long minimalTanggal = millisecondDariWaktuAwalSampaiHariIni + DAY_IN_MILLIS;
        return minimalTanggal;
    }
    public static long getMaxTanggalPinjam(){
       long maxTanggalPinjam = getMinTanggal() + (DAY_IN_MILLIS * 59);
        return maxTanggalPinjam;
    }
    public static long getMaxTanggalKembali(){
        long maxTanggalKembali = getMinTanggal() + (DAY_IN_MILLIS * 89);
        return maxTanggalKembali;
    }

    private static long jumlahHariSejakWaktuAwalUTC(long date){
        return TimeUnit.MILLISECONDS.toDays(date);
    }
    public static long ubahKeFormatWIB(long utcDate){
        long millisecondDalamWIB = utcDate + (HOUR_IN_MILLIS * 7);
        return  millisecondDalamWIB;
    }
    public static long getDateToMillisecond(int year, int month, int day){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        long millisecondDariWaktuAwalSampaiHariIni = DAY_IN_MILLIS * jumlahHariSejakWaktuAwalUTC(calendar.getTimeInMillis());
        return millisecondDariWaktuAwalSampaiHariIni;
    }
    public static String getStringDate(long normalDate){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(normalDate);
        String stringDate;
        stringDate = getStringHari(calendar.get(Calendar.DAY_OF_WEEK)) + " " + calendar.get(Calendar.DAY_OF_MONTH)
                + " " + getStringBulan(calendar.get(Calendar.MONTH)) + " " + calendar.get(Calendar.YEAR);
        return stringDate;
    }

    private static String getStringHari(int day){
        return mHari[day -1];
    }
    private static String getStringBulan(int month){
        return mBulan[month];
    }
    public static String getStringJam(int hour){
        String jam;
        if(hour < 10){
            jam = "0" + hour + ".00 WIB";
        }
        else {
            jam = "" + hour + ".00 WIB";
        }
        return jam;
    }

}
