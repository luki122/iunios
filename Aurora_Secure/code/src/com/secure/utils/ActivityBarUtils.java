package com.secure.utils;

import com.aurora.secure.R;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

public class ActivityBarUtils {
    public static final int TYPE_OF_Normal = 0;
    public static final int TYPE_OF_Appstore = 1;
    
    public static void changeActionBarStyle(AuroraActivity activity,int type){
    	if(activity == null){
    		return ;
    	}
    	AuroraActionBar bar = activity.getAuroraActionBar();
    	switch(type){
    	case TYPE_OF_Normal:break;
    	case TYPE_OF_Appstore:
    		dealAppstoreStyle(activity,bar);
    		break;
    	}
    }
    
    @SuppressLint("ParserError")
	private static void dealAppstoreStyle(AuroraActivity activity,AuroraActionBar bar){
    	if(bar == null){
    		return ;
    	}
    	bar.setBackground(activity.getResources().
    			getDrawable(R.drawable.aurora_action_bar_top_bg_for_appstore));
    	
    	TextView titleView = bar.getTitleView();
    	if(titleView != null){
    		titleView.setTextColor(Color.WHITE);
    	}
    	
    	ImageButton homeButton = (ImageButton)bar.getHomeButton();
    	if(homeButton != null){
    		homeButton.setImageResource(R.drawable.home_btn_src_of_green_bar);
    	} 
    	bar.setHomeLayoutBackgroundResource(R.drawable.home_btn_bg_of_green_bar);
    }
}
