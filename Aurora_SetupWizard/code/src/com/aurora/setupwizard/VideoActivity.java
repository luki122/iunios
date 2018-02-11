package com.aurora.setupwizard;

import com.aurora.setupwizard.utils.Constants;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import aurora.app.AuroraActivity;

public class VideoActivity extends AuroraActivity {

	private int mCount;
	private VideoView mVideoView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	//	getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED, FLAG_HOMEKEY_DISPATCHED);// 屏蔽home键
		setContentView(R.layout.activity_video);
		
		mCount = getIntent().getIntExtra(AppRecommendActivity.APK_AZ_COUNT, 0);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.aurora.setupwizard.azcomplete");
		registerReceiver(receiver, filter);

		mVideoView = (VideoView) findViewById(R.id.surface_view);
		mVideoView.setMediaController(new MediaController(VideoActivity.this)); 
		mVideoView.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				complete();
				finish();
			}
		});
		mVideoView.setOnErrorListener(new OnErrorListener() {
			
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				
				return false;
			}
		});
//		Toast.makeText(this, "暂无视频", 0).show();
//		finish();
//		mVideoView.setVideoURI(Uri.parse("android.resource://"
//				+ getPackageName() + "/" + R.raw.start));
		 mVideoView.setVideoPath("/sdcard/IUNI_OS_Introduction.mp4");
		// mVideoView.setMediaController(new MediaController(this));
		mVideoView.requestFocus();
		mVideoView.start();
	}

	
    private void complete() {
    	
    	Intent intent1 = new Intent(Constants.HANDLER_ENADBLE);
        sendBroadcast(intent1);
    	
        // Add a persistent setting to allow other apps to know the device has been provisioned.
        Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);
        Settings.Secure.putInt(getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 1);

        // remove this activity from the package manager.
        PackageManager pm = getPackageManager();
        ComponentName name = new ComponentName(this, LanguageSetupWizard.class);
        pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        //App.getActivityManager(this).exit();
		if (mCount == 0) {
//			Intent intent = new Intent();
//			intent.setAction(Intent.ACTION_MAIN);
//			intent.addCategory(Intent.CATEGORY_HOME);
//			intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
//					| Intent.FLAG_ACTIVITY_NEW_TASK);
//
//			startActivity(intent);
			
			ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
			am.forceStopPackage("com.android.settings");
			am.forceStopPackage(getPackageName());
		} else {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
					| Intent.FLAG_ACTIVITY_NEW_TASK);

			startActivity(intent);
		}
    }
    
	BroadcastReceiver receiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.v("lmjssjj", intent.getAction());
			mCount=0;
		}
	};
	
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	};
	
}
