package com.example.asus.d_biumsemarang1.utils;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by ASUS on 11/19/2017.
 */

class ScrollingToolbarBehavior extends CoordinatorLayout.Behavior<BottomNavigationView> {

    public ScrollingToolbarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, BottomNavigationView child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, BottomNavigationView child, View dependency) {
        if (dependency instanceof AppBarLayout) {

            int distanceToScroll = child.getHeight();
            float ratio = dependency.getY() / (float) dependency.getHeight();
            child.setTranslationY(-distanceToScroll * ratio);
        }
        return true;
    }
}
