package com.example.asus.d_biumsemarang1.data;

import java.util.Map;

/**
 * Created by ASUS on 11/16/2017.
 */

public class SukaGedungVenue {
    public long jumlah;
    public Map<String,Object> users;
    public SukaGedungVenue(){

    }

    public SukaGedungVenue(long jumlah, Map<String, Object> users) {
        this.jumlah = jumlah;
        this.users = users;
    }

    public long getJumlah() {
        return jumlah;
    }

    public void setJumlah(long jumlah) {
        this.jumlah = jumlah;
    }

    public Map<String, Object> getUsers() {
        return users;
    }

    public void setUsers(Map<String, Object> users) {
        this.users = users;
    }
}
