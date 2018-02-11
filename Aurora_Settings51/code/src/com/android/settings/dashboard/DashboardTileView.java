/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.dashboard;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.Utils;

import aurora.widget.AuroraSwitch;
import android.app.Activity;

public class DashboardTileView extends FrameLayout implements View.OnClickListener {

    private static final int DEFAULT_COL_SPAN = 1;

    private ImageView mImageView;
    private TextView mTitleTextView;
    private TextView mStatusTextView;
    private View mDivider;
    private ImageView mRightView;

    private AuroraSwitch mAuroraSwitch;
    private int mColSpan = DEFAULT_COL_SPAN;

    private DashboardTile mTile;
    
    private Activity mActivity;

     
    
    public DashboardTileView(Context context) {
        this(context, null);
    }

    public DashboardTileView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mActivity = (Activity)context;

        final View view = LayoutInflater.from(context).inflate(R.layout.aurora_dashboard_tile, this);

        mImageView = (ImageView) view.findViewById(R.id.icon);
        mTitleTextView = (TextView) view.findViewById(R.id.title);
        mStatusTextView = (TextView) view.findViewById(R.id.status);
        mRightView = (ImageView) view.findViewById(R.id.right);
        setOnClickListener(this);
        setFocusable(true);
    }

    public DashboardTileView(Context context, boolean hasSwitch) {
        super(context);

        final View view = LayoutInflater.from(context).inflate(R.layout.aurora_dashboard_tile, this);

        mImageView = (ImageView) view.findViewById(R.id.icon);
        mTitleTextView = (TextView) view.findViewById(R.id.title);
        mStatusTextView = (TextView) view.findViewById(R.id.status);
        mAuroraSwitch = (AuroraSwitch) view.findViewById(R.id.switchWidget);
        mRightView = (ImageView) view.findViewById(R.id.right);
        mRightView.setVisibility(View.GONE);
        mAuroraSwitch.setVisibility(View.VISIBLE);
        setFocusable(true);
    }
    
    public void showDivider(boolean show){
    	View tile = findViewById(R.id.dashboard_tile_divider);
    	if(tile != null){
    		tile.setVisibility(show?View.VISIBLE:View.GONE);
    	}
    }
    
    @Override
    public void setEnabled(boolean enabled) {
    	// TODO Auto-generated method stub
    	super.setEnabled(enabled);
    	if(mTitleTextView != null){
    		mTitleTextView.setEnabled(enabled);
    	}
    }
    
    
    public TextView getTitleTextView() {
        return mTitleTextView;
    }

    public TextView getStatusTextView() {
        return mStatusTextView;
    }

    public ImageView getImageView() {
        return mImageView;
    }
    
    public ImageView getRightView() {
        return mRightView;
    }
    
    public void setRightView(int r){
    	if(mRightView != null){
    		mRightView.setImageResource(r);
    	}
    }

    public void setTile(DashboardTile tile) {
        mTile = tile;
    }

    public void setDividerVisibility(boolean visible) {
       // mDivider.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    void setColumnSpan(int span) {
        mColSpan = span;
    }

    int getColumnSpan() {
        return mColSpan;
    }

    public AuroraSwitch getAuroraSwitch(){
    	 return mAuroraSwitch;
    }
    
    
    
    @Override
    public void onClick(View v) {
        if (mTile.fragment != null) {
            Utils.startWithFragment(getContext(), mTile.fragment, mTile.fragmentArguments, null, 0,
                    mTile.titleRes, mTile.getTitle(getResources()));
        } else if (mTile.intent != null) {
        	mActivity.startActivity(mTile.intent);
        	mTile.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        	mActivity.overridePendingTransition(com.aurora.R.anim.aurora_activity_open_enter,com.aurora.R.anim.aurora_activity_open_exit);
        }else if(mTile.intent==null && mTile.id == R.id.personal_space) {
    		//Log.d("Settngs", " click personel center !!");
        	try{
            	Intent t = new Intent();
            	t.setAction(Intent.ACTION_VIEW);
            	t.setData(Uri.parse("openaccount://com.aurora.account.login"));
            	t.putExtra("needSetExitAnimation", true);  
            	t.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            	mActivity.startActivity(t);
            	}catch(Exception e){
            		Log.d("Settngs", " persioner center is false ******");
            	}
        	mActivity.overridePendingTransition(com.aurora.R.anim.aurora_activity_open_enter,com.aurora.R.anim.aurora_activity_open_exit);
        }/*else if(mTile.intent==null && mTile.id == R.id.wallpaper_settings){
        	try{
        		//Log.d("Settngs", " wallpaper_set center is true ******");
            	Intent t = new Intent();
            	t.setAction("aurora.intent.action.wallpaper_set");
            	t.putExtra("needSetExitAnimation", true);  
            	t.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            	mActivity.startActivity(t);
            	mActivity.overridePendingTransition(com.aurora.R.anim.aurora_activity_open_enter,com.aurora.R.anim.aurora_activity_open_exit);
            	}catch(Exception e){
            		Log.d("Settngs", " wallpaper_set center is false ******");
            	}
        	
        }*/
    }
}
