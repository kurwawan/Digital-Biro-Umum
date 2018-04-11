package com.example.asus.d_biumsemarang1.data;

/**
 * Created by ASUS on 12/11/2017.
 */

public class Kontak {

    String nama;
    String noHp;
    public Kontak(){

    }

    public Kontak(String nama, String noHp) {
        this.nama = nama;
        this.noHp = noHp;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getNoHp() {
        return noHp;
    }

    public void setNoHp(String noHp) {
        this.noHp = noHp;
    }
}
