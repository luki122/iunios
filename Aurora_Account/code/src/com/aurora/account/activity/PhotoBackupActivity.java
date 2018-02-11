package com.aurora.account.activity;

import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraNumberPicker;
import aurora.widget.AuroraSwitch;

import com.aurora.account.AccountApp;
import com.aurora.account.R;
import com.aurora.account.bean.AppConfigInfo;
import com.aurora.account.util.AccountPreferencesUtil;
import com.aurora.account.util.CommonUtil;
import com.aurora.account.util.Globals;
import com.aurora.account.util.SystemUtils;
import com.aurora.account.util.TimeUtils;

public class PhotoBackupActivity extends BaseActivity {
	
	private AuroraSwitch backup_switch;
	private AuroraNumberPicker numPicker;
	
	private AppConfigInfo galleryApp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.activity_photo_backup,
				AuroraActionBar.Type.Normal, true);
		
		initViews();
		initData();
		setListeners();
	}
	
	@Override
	protected String getActionBarTitle() {
		return getString(R.string.photo_backup_title);
	}
	
	private void initViews() {
//		rl_registerByPhoneNum = (RelativeLayout) findViewById(R.id.rl_registerByPhoneNum);
//		rl_registerByEmail = (RelativeLayout) findViewById(R.id.rl_registerByEmail);
		backup_switch = (AuroraSwitch) findViewById(R.id.backup_switch);
		numPicker = (AuroraNumberPicker)findViewById(R.id.aurora_time);
		
		numPicker.setMinValue(0);
    	numPicker.setMaxValue(23);
    	numPicker.setFormatter(AuroraNumberPicker.TWO_DIGIT_FORMATTER);
    	numPicker.setLabel(getString(R.string.photo_backup_time)); 
//    	numPicker.setSelectionSrc(null);
    	
	}
	
	private void setListeners() {
//		rl_registerByPhoneNum.setOnClickListener(this);
//		rl_registerByEmail.setOnClickListener(this);
		
		backup_switch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				SystemUtils.updateAppConfigInfo(PhotoBackupActivity.this, 
						galleryApp.getApp_packagename(), isChecked);
			}
		});
		
	}
	
	private void initData() {
		List<AppConfigInfo> apps = SystemUtils.getAppConfigInfo(this);
		for (AppConfigInfo app : apps) {
			if (app.getApp_packagename().equals(Globals.GALLERY_PACKAGE_NAME)) {
				galleryApp = app;
			}
		}
		
		if (galleryApp != null) {
			backup_switch.setChecked(galleryApp.isSync());
		}
		
//		int[] times = TimeUtils.getTime(AccountPreferencesUtil.getInstance(AccountApp.getInstance()).getPhotoAutoSyncTime());
//		if (times != null && times.length > 0) {
//			numPicker.setValue(times[0]);
//		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
//		String time = getFormatTime();
//		String saveTime = AccountPreferencesUtil.getInstance(AccountApp.getInstance()).getPhotoAutoSyncTime();
//		Log.i(TAG, "time: " + time + " saveTime: " + saveTime);
//		
//		if (!time.equals(saveTime)) {
//			Log.i(TAG, "time no same! save and reset alarm!");
//			AccountPreferencesUtil.getInstance(AccountApp.getInstance()).setPhotoAutoSyncTime(time);
//			CommonUtil.checkAndSetPhotoBackupAlarm();
//		}
		
	}

	@Override
    protected boolean shouldHandleFocusChanged() {
        return false;
    }
	
	private String getFormatTime() {
		StringBuffer sb = new StringBuffer();
		int hour = numPicker.getValue();
		if (hour < 10) {
			sb.append("0");
		}
		sb.append(hour);
		sb.append(":");
		sb.append("00");
		return sb.toString();
	}

}
