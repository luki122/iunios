package com.android.auroramusic.ui.lock;


import com.android.music.R;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import aurora.app.AuroraActivity;


public class AuroraLockActivity extends AuroraActivity{

	private static final String TAG = "AuroraLockActivity";
	
	
	private static class SingletonHolder {   
		/**  
		 * * 单例对象实例 
		 *  */    
		static final AuroraLockActivity INSTANCE = new AuroraLockActivity();  
	}
	
	public static AuroraLockActivity getInstance() {    
		return SingletonHolder.INSTANCE;    
	}
	

	private Object readResolve() {   
		return getInstance();    
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//setContentView(R.layout.aurora_lock_activity);
//		LinearLayout mLayout = new LinearLayout(this);
		
		AuroraMusicLockView mLockView = new AuroraMusicLockView(this);
		mLockView.setId(1010);
//		mLayout.addView(mLockView);
		setContentView(mLockView);
	}
	
	public View AuroraPrintf(Context context) {
		Button  mButton = new Button(context);
    	mButton.setText("OK");
    	
    	return mButton;
	}
	
	public View getMusicLockView(Context context) {
		AuroraMusicLockView mLockView = new AuroraMusicLockView(context);
		return mLockView;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

}
