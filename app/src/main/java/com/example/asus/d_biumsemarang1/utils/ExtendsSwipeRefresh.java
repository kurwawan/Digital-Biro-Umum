package com.example.asus.d_biumsemarang1.utils;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.example.asus.d_biumsemarang1.listener.AppBarListener;

/**
 * Created by ASUS on 12/11/2017.
 */
//reload
public class ExtendsSwipeRefresh extends SwipeRefreshLayout {
    private ViewGroup mContainer;
    private AppBarListener mAppBarListener;
    private int mType = 0;
    private int mState = ViewPager.SCROLL_STATE_IDLE;

    public ExtendsSwipeRefresh(Context context) {
        super(context);
    }

    public ExtendsSwipeRefresh(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public boolean canChildScrollUp() {
        ViewGroup container = getContainer();
        if(mType != 1){
            View view = container.getChildAt(0);
            if(view.getVisibility() != View.VISIBLE){
                view = container.getChildAt(1);
            }
            return view.canScrollVertically(-1)  || isRefreshing() || !mAppBarListener.isExpanded();
        }
        else {
            return  container.canScrollVertically(-1) || isRefreshing() || !mAppBarListener.isExpanded()
                    || mState != ViewPager.SCROLL_STATE_IDLE;
        }

    }

    private ViewGroup getContainer(){
        if(mContainer != null){
            return mContainer;
        }
        for(int i=0;i<getChildCount();i++){
            if(getChildAt(i) instanceof ViewGroup){
                mContainer = (ViewGroup)getChildAt(i);
                break;
            }
        }
        return mContainer;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(getContext() instanceof AppBarListener){
            mAppBarListener = (AppBarListener) getContext();
        }


    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAppBarListener = null;
    }

    public void setType(int type){
        mType = type;
    }
    public void setState(int state){
        mState = state;
    }
}
