package com.example.asus.d_biumsemarang1.data;

/**
 * Created by ASUS on 11/15/2017.
 */

public class JadwalSewa {

    long waktu_pinjam;
    long waktu_kembali;

    public JadwalSewa(){

    }

    public JadwalSewa(long waktu_pinjam, long waktu_kembali) {
        this.waktu_pinjam = waktu_pinjam;
        this.waktu_kembali = waktu_kembali;
    }

    public long getWaktu_pinjam() {
        return waktu_pinjam;
    }

    public void setWaktu_pinjam(long waktu_pinjam) {
        this.waktu_pinjam = waktu_pinjam;
    }

    public long getWaktu_kembali() {
        return waktu_kembali;
    }

    public void setWaktu_kembali(long waktu_kembali) {
        this.waktu_kembali = waktu_kembali;
    }
}
