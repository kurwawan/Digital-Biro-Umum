package com.example.asus.d_biumsemarang1.data;

/**
 * Created by ASUS on 12/3/2017.
 */

public class Bank {
    String atas_nama;
    String no_rekening;
    String gambar;
    String link;
    String nama;


    public Bank(){

    }
    public Bank(String atas_nama, String no_rekening, String gambar, String link, String nama) {
        this.atas_nama = atas_nama;
        this.no_rekening = no_rekening;
        this.gambar = gambar;
        this.link = link;
        this.nama = nama;
    }

    public String getAtas_nama() {
        return atas_nama;
    }

    public void setAtas_nama(String atas_nama) {
        this.atas_nama = atas_nama;
    }

    public String getNo_rekening() {
        return no_rekening;
    }

    public void setNo_rekening(String no_rekening) {
        this.no_rekening = no_rekening;
    }

    public String getGambar() {
        return gambar;
    }

    public void setGambar(String gambar) {
        this.gambar = gambar;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }
}
