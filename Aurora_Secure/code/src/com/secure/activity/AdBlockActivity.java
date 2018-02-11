package com.secure.activity;

import java.util.ArrayList;
import java.util.List;
import com.aurora.secure.R;
import com.secure.adapter.AddAutoStartAppAdapter;
import com.secure.data.AppInfo;
import com.secure.data.AppsInfo;
import com.secure.data.AutoStartData;
import com.secure.data.BaseData;
import com.secure.fragment.AdScanFragment;
import com.secure.fragment.AdScanFragment2;
import com.secure.fragment.AdScanFragment3;
import com.secure.interfaces.Subject;
import com.secure.model.AdScanModel;
import com.secure.model.AutoStartModel;
import com.secure.model.ConfigModel;
import com.secure.utils.ActivityUtils;
import com.secure.utils.ApkUtils;
import com.secure.utils.mConfig;
import com.secure.utils.ActivityUtils.LoadCallback;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;

public class AdBlockActivity extends AuroraActivity{	
	private AdScanFragment3 adScanFragment;
	private static Activity activity;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        activity = this;
        setAuroraContentView(R.layout.ad_block_activity,
        		AuroraActionBar.Type.Normal);    
        getAuroraActionBar().setTitle(R.string.ad_block); 
        getAuroraActionBar().setmOnActionBarBackItemListener(
        		new OnAuroraActionBarBackItemClickListener (){
			@Override
			public void onAuroraActionBarBackItemClicked(int arg0) {
				onBackPressed();				
			}}); 
        adScanFragment = new AdScanFragment3();
        initView(adScanFragment);
    }

	@Override
	protected void onNewIntent(Intent intent) {
    	AdScanFragment3 tmpAdScanFragment = adScanFragment;
    	if(tmpAdScanFragment != null){
    		tmpAdScanFragment.startScanAdApp(getApplicationContext());
    	} 
		super.onNewIntent(intent);
	}

	private void initView(AdScanFragment3 adScanFragment){	
    	if(adScanFragment != null){
        	FragmentTransaction ft = getFragmentManager().beginTransaction();
        	ft.add(R.id.content_frame, adScanFragment).commit();
        	ft.show(adScanFragment);
    	}
    }

	@Override
	public void onBackPressed() {
		AdScanModel.getInstance(this).stopManualUpdate();
	}   
		
	@Override
	protected void onDestroy() {
		activity = null;
		releaseObject();
		super.onDestroy();
	}
	
	private void releaseObject(){
		if(adScanFragment != null){			
			adScanFragment.releaseObject();
		}	
	}
	
	public static void myFinish(){
		try{
			if(activity != null){
				activity.finish();		
			}
		}catch(Exception e){
			e.printStackTrace();
		}		
	}
}
