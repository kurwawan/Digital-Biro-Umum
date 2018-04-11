package com.example.asus.d_biumsemarang1.data;

import com.google.firebase.database.Exclude;

/**
 * Created by ASUS on 11/11/2017.
 */

public class Sewa {
    long waktu_pinjam;
    long waktu_kembali;
    String id_inf;
    String alamat;
    long status;
    long biaya;
    long jam;
    String uid;
    long total;
    String status_inf;
    String email_inf;
    String status_email;
    String keterangan;
    String key;

    public Sewa(long waktu_pinjam, long waktu_kembali, String id_inf, String alamat, long status, long biaya, long jam, String uid, long total, String status_inf, String email_inf, String status_email, String keterangan) {
        this.waktu_pinjam = waktu_pinjam;
        this.waktu_kembali = waktu_kembali;
        this.id_inf = id_inf;
        this.alamat = alamat;
        this.status = status;
        this.biaya = biaya;
        this.jam = jam;
        this.uid = uid;
        this.total = total;
        this.status_inf = status_inf;
        this.email_inf = email_inf;
        this.status_email = status_email;
        this.keterangan = keterangan;
    }

    public Sewa() {

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

    public String getId_inf() {
        return id_inf;
    }

    public void setId_inf(String id_inf) {
        this.id_inf = id_inf;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public long getBiaya() {
        return biaya;
    }

    public void setBiaya(long biaya) {
        this.biaya = biaya;
    }

    public long getJam() {
        return jam;
    }

    public void setJam(long jam) {
        this.jam = jam;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public String getStatus_inf() {
        return status_inf;
    }

    public void setStatus_inf(String status_inf) {
        this.status_inf = status_inf;
    }

    public String getStatus_email() {
        return status_email;
    }

    public void setStatus_email(String status_email) {
        this.status_email = status_email;
    }

    public String getEmail_inf() {
        return email_inf;
    }

    public void setEmail_inf(String email_inf) {
        this.email_inf = email_inf;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Exclude
    public String getKey() {
        return key;
    }
}
