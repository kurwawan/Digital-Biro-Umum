package com.example.asus.d_biumsemarang1.listener;

import com.example.asus.d_biumsemarang1.data.GedungVenue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ASUS on 11/24/2017.
 */

public final class DetailToFragmentModel {

    private static DetailToFragmentModel mModel;
    private List<GedungVenue> mData;
    private boolean isSetupFinished = false;
    private int clickPosition;

    private DetailToFragmentModel(){

    }

    public static DetailToFragmentModel getInstance(){
        if(mModel == null){
            mModel = new DetailToFragmentModel();
        }
        return  mModel;
    }

    public void setmData(List<GedungVenue> mData){
        if(this.mData == null){
            this.mData = new ArrayList<>();
        }
        this.mData = mData;
    }
    public List<GedungVenue> getmData(){
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
