package com.example.asus.d_biumsemarang1.data;

import com.google.firebase.database.Exclude;

import java.util.Map;

/**
 * Created by ASUS on 11/2/2017.
 */


public class GedungVenue {
    String nama;
    String alamat;
    long biaya;
    Map<String, Object> noHp;
    Map<String, Object> gambar;
    String informasi;
    String key;

    public GedungVenue() {

    }

    public GedungVenue(String nama, String alamat, long biaya,Map<String, Object> noHp, Map<String, Object> gambar, String informasi) {
        this.nama = nama;
        this.alamat = alamat;
        this.biaya = biaya;
        this.noHp = noHp;
        this.gambar = gambar;
        this.informasi = informasi;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public long getBiaya() {
        return biaya;
    }

    public void setBiaya(long biaya) {
        this.biaya = biaya;
    }

    public Map<String, Object> getNoHp() {
        return noHp;
    }

    public void setNoHp(Map<String, Object> noHp) {
        this.noHp = noHp;
    }

    public Map<String, Object> getGambar() {
        return gambar;
    }

    public void setGambar(Map<String, Object> gambar) {
        this.gambar = gambar;
    }

    public String getInformasi() {
        return informasi;
    }

    public void setInformasi(String informasi) {
        this.informasi = informasi;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Exclude
    public String getKey() {
        return key;
    }

}
