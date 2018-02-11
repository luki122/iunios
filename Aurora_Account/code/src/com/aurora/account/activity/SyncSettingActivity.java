package com.aurora.account.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraNumberPicker;
import aurora.widget.AuroraSwitch;

import com.aurora.account.R;
import com.aurora.account.totalCount.TotalCount;
import com.aurora.account.util.AccountPreferencesUtil;
import com.aurora.account.util.CommonUtil;
import com.aurora.account.util.TimeUtils;

/**
 * 设置界面
 */
public class SyncSettingActivity extends AuroraActivity implements OnClickListener {

    private static final String TAG = "SyncSettingActivity";
    
    private LinearLayout wlan_switch_ly;
	private AuroraSwitch wlan_switch;
    private AuroraNumberPicker numPicker;
    private LinearLayout ll_backup_time;
    private TextView tv_backup_time;
    
    private boolean isWifiSync = true;
    
    private AccountPreferencesUtil mPref;
    
    private int timeInt = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.sync_setting_activity,
				AuroraActionBar.Type.Normal, true);
		
		initActionBar();
		initViews();
		setListeners();
		initData();
	}
	
	private void initData() {
		mPref = AccountPreferencesUtil.getInstance(this);
		isWifiSync = mPref.isWifiSyncOnly();
		wlan_switch.setChecked(isWifiSync);
		
		int[] times = TimeUtils.getTime(AccountPreferencesUtil.getInstance(this).getAutoBackupTime());
		if (times != null && times.length > 0) {
			timeInt = times[0];
			updateTimeUI();
		}
	}
	
	private void initViews() {
		wlan_switch_ly = (LinearLayout) findViewById(R.id.wlan_switch_ly);
		wlan_switch = (AuroraSwitch) findViewById(R.id.wlan_switch);
		ll_backup_time = (LinearLayout) findViewById(R.id.ll_backup_time);
		tv_backup_time = (TextView) findViewById(R.id.tv_backup_time);
		
		wlan_switch.setChecked(isWifiSync);
    }
	
	private void setListeners() {
		wlan_switch_ly.setOnClickListener(this);
		ll_backup_time.setOnClickListener(this);
		
		wlan_switch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				
			    mPref.setWifiSyncOnly(isChecked);
			}
		});
    }
	
	private void initActionBar() {
		getAuroraActionBar().setTitle(R.string.setting_title);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.wlan_switch_ly:
			Log.i(TAG, " wlan_switch_ly onClick()");
			handleWlanSwitch();
			break;
		case R.id.ll_backup_time:
			AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(SyncSettingActivity.this);
			builder.setTitle(R.string.setting_auto_backup_time_title);
			builder.setTitleDividerVisible(true);
			
			View view = getLayoutInflater().inflate(R.layout.dialog_sync_setting_time_view, null);
			numPicker = (AuroraNumberPicker) view.findViewById(R.id.aurora_time);
			numPicker.setMinValue(0);
	    	numPicker.setMaxValue(23);
	    	numPicker.setValue(timeInt);
	    	numPicker.setFormatter(AuroraNumberPicker.TWO_DIGIT_FORMATTER);
	    	numPicker.setLabel(getString(R.string.photo_backup_time)); 
	    	
	    	builder.setView(view);
			
			builder.setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					
					String time = getFormatTime(numPicker);
					String saveTime = AccountPreferencesUtil.getInstance(SyncSettingActivity.this).getAutoBackupTime();
					Log.i(TAG, "time: " + time + " saveTime: " + saveTime);
					
					if (!time.equals(saveTime)) {
						Log.i(TAG, "time no same! save and reset alarm!");
						timeInt = numPicker.getValue();
						updateTimeUI();
						AccountPreferencesUtil.getInstance(SyncSettingActivity.this).setAutoBackupTime(time);
						
						CommonUtil.checkAndSetAppBackupAlarm();
					}
					
				}
			});
			builder.setNegativeButton(R.string.dialog_cancel, null);
			builder.create().show();
			break;
		}
	}
	
	private void handleWlanSwitch() {
		Log.i(TAG, "wlan_switch.isChecked(): " + wlan_switch.isChecked());
		
		if (!wlan_switch.isChecked()) {
			// wlan_switch.setChecked(true);
			wlan_switch.performClick(); // setCheck没有音效和动画效果
		} else {
			new AuroraAlertDialog.Builder(this)
					.setTitle(R.string.dialog_prompt)
					.setMessage(R.string.account_sync_turn_off_wlan_confirm)
					.setPositiveButton(R.string.dialog_confirm,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// wlan_switch.setChecked(false);
									wlan_switch.performClick();
									//add by zw 02-13 关闭仅在wifi下同步按钮并选择确认时
									new TotalCount(SyncSettingActivity.this, "280", "002", 1).CountData();
									//end by zw
								}
							}).setNegativeButton(R.string.dialog_cancel, null)
					.create().show();
		}
	}
	
	private void updateTimeUI() {
		tv_backup_time.setText(getString(R.string.setting_backup_time, timeInt));
	}

	private String getFormatTime(AuroraNumberPicker numberPicker) {
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