package com.aurora.setupwizard;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aurora.setupwizard.adapter.AppAdapter;
import com.aurora.setupwizard.domain.ApkInfo;
import com.aurora.setupwizard.service.InstallService;
import com.aurora.setupwizard.utils.ApkUtil;
import com.aurora.setupwizard.utils.Constants;

import java.util.List;

import aurora.widget.AuroraActionBar;

public class CompleteActivityU5 extends BaseActivity implements OnClickListener {

	private ListView lvApp;
	private AppAdapter mAdapter;
	private List<ApkInfo> infos;
	private TextView tv_anzhuang;
	private TextView tv_use;
	private TextView tv_pre;
	private TextView tv_recom;
	private TextView tv_complete_text;
	private RelativeLayout rl_lv;
	private boolean mIsRec = false;
	private int mCount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.activity_complete,
				AuroraActionBar.Type.Empty);
		getAuroraActionBar().setVisibility(View.GONE);
		// mIsRec = ApkUtil.isRecommend();
		initView();
		initData();
		initEvent();
	}

	private void initView() {

		rl_lv = (RelativeLayout) findViewById(R.id.rl_list);
		lvApp = (ListView) findViewById(R.id.lv_recommend);
		tv_anzhuang = (TextView) findViewById(R.id.tv_anzhuang);
		tv_use = (TextView) findViewById(R.id.tv_next);
		tv_pre = (TextView) findViewById(R.id.tv_pre);
		tv_recom = (TextView) findViewById(R.id.tv_recommend);
		tv_complete_text = (TextView) findViewById(R.id.tv_complete_info);

		tv_pre.setVisibility(8);
	}

	private void initData() {

		mCount = getIntent().getIntExtra(AppRecommendActivity.APK_AZ_COUNT, 0);

		if (!mIsRec) {
			rl_lv.setVisibility(View.GONE);
			tv_recom.setVisibility(View.GONE);
		} else {
			tv_complete_text.setVisibility(View.GONE);
		}

		tv_use.setText(R.string.begin_use);

		infos = ApkUtil.getApkInfo(this);

		mAdapter = new AppAdapter(getApplicationContext(), infos);
		lvApp.setAdapter(mAdapter);
	}

	private void initEvent() {
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.aurora.setupwizard.azcomplete");
		registerReceiver(receiver, filter);
		
		tv_anzhuang.setOnClickListener(this);
		tv_pre.setOnClickListener(this);
		tv_use.setOnClickListener(this);
	}

	public void play(View v) {
		Intent intent = new Intent(this, VideoActivity.class);
		intent.putExtra(AppRecommendActivity.APK_AZ_COUNT, mCount);
		startActivity(intent);
	}

	@Override
	public void onResume() {
		super.onResume();
		com.aurora.utils.SystemUtils.switchStatusBarColorMode(
				com.aurora.utils.SystemUtils.STATUS_BAR_MODE_BLACK, this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_anzhuang:
			for (ApkInfo info : infos) {
				if (info.isCheck()) {
					Log.v("lmjssjj", info.getApkName());
					Intent intent = new Intent(this, InstallService.class);
					intent.putExtra("packageName", info.getApkPackageName());
					intent.putExtra("apkPath", info.getApkPath());
					startService(intent);
				}
			}
			tv_anzhuang.setClickable(false);
			break;

		case R.id.tv_next:
			complete();
			break;
		case R.id.tv_pre:
			go2Pre();
			break;

		default:
			break;
		}
	}

	private void go2Pre() {
		onBackPressed();
	}

	private void complete() {

		Intent intent1 = new Intent(Constants.HANDLER_ENADBLE);
		sendBroadcast(intent1);

		// Add a persistent setting to allow other apps to know the device has
		// been provisioned.
		Settings.Global.putInt(getContentResolver(),
				Settings.Global.DEVICE_PROVISIONED, 1);
		Settings.Secure.putInt(getContentResolver(),
				Settings.Secure.USER_SETUP_COMPLETE, 1);

		// remove this activity from the package manager.
		PackageManager pm = getPackageManager();
		ComponentName name = new ComponentName(this, LanguageSetupWizard.class);
		pm.setComponentEnabledSetting(name,
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP);

		// App.getActivityManager(this).exit();

		if (mCount == 0) {
		//	ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		//	am.forceStopPackage(getPackageName());
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
			//android.os.Process.killProcess(android.os.Process.myPid());   //获取PID 
	
		} else {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
					| Intent.FLAG_ACTIVITY_NEW_TASK);

			startActivity(intent);
		}
		// enableStatusBar();

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
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
