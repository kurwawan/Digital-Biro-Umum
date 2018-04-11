package com.example.asus.d_biumsemarang1.listener;

/**
 * Created by ASUS on 11/26/2017.
 */

public final class MainActivityListener {

    private static MainActivityListener mInstance;
    private boolean isCreated = false;

    private MainActivityListener(){

    }
    public static MainActivityListener getInstance(){
        if(mInstance == null){
            mInstance = new MainActivityListener();
        }
        return mInstance;
    }
     public boolean isCreated(){
        return isCreated;
     }
     public void setCreated(boolean isCreated){
         this.isCreated = isCreated;
     }

}
