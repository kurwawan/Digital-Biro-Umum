package com.example.asus.d_biumsemarang1.listener;

import com.example.asus.d_biumsemarang1.data.Sewa;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ASUS on 12/2/2017.
 */

public class DetailSewaVenueToFragmentModel {

    private static DetailSewaVenueToFragmentModel mModel;
    private List<Sewa> mData;
    private boolean isSetupFinished = false;
    private int clickPosition;

    private DetailSewaVenueToFragmentModel(){

    }

    public static DetailSewaVenueToFragmentModel getInstance(){
        if(mModel == null){
            mModel = new DetailSewaVenueToFragmentModel();
        }
        return mModel;
    }

    public void setmData(List<Sewa> mData){
        if(this.mData == null){
            this.mData = new ArrayList<>();
        }
        this.mData = mData;
    }
    public List<Sewa> getmData(){
        return mData;
    }
    public void clearmData(){
        mData = null;
    }

    public void setSetupFinished(boolean isSetupFinished){
        this.isSetupFinished = isSetupFinished;
    }

    public boolean getSetupFinished(){
        return isSetupFinished;
    }

    public int getClickPosition() {
        return clickPosition;
    }

    public void setClickPosition(int clickPosition) {
        this.clickPosition = clickPosition;
    }
}
