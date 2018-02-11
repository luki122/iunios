package com.aurora.account.activity;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraSwitch;

import com.aurora.account.R;
import com.aurora.account.bean.AppConfigInfo;
import com.aurora.account.util.AppInfo;
import com.aurora.account.util.CommonUtil;
import com.aurora.account.util.Globals;
import com.aurora.account.util.Log;
import com.aurora.account.util.SystemUtils;

/**
 * 引导页界面
 * @author JimXia
 *
 * @date 2014年11月14日 下午3:15:44
 */
public class SyncSettingGuide2Activity extends BaseActivity implements OnClickListener {

    private static final String TAG = "SyncSettingGuide2Activity";
    
	public static final String EXTRA_KEY_LOGIN_STATUS = "loginStatus";
	private int mLoginStatus;
	
	private LinearLayout ll_app;
	private Button btn_next;
	
	private List<AppConfigInfo> mAppConfigInfoList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.sync_setting_guide2_activity,
				AuroraActionBar.Type.Empty, false);
		mLoginStatus = getIntent().getIntExtra(EXTRA_KEY_LOGIN_STATUS, SyncAccountActivity.LOGIN_STATUS_NORMAL);
		
		initViews();
		setListeners();
		initData();
		initAppList();
	}
	
	@Override
	protected String getActionBarTitle() {
		return getString(R.string.guide_setting);
	}
	
	private void initData() {
		
		
	}
	
	private void initViews() {
		ll_app = (LinearLayout) findViewById(R.id.ll_app);
		btn_next = (Button) findViewById(R.id.btn_next);
		
    }
	
	private void setListeners() {
		btn_next.setOnClickListener(this);
    }
	
	private void initAppList() {
		mAppConfigInfoList = SystemUtils.getAppConfigInfo(this);
		
		AppInfo app_info = null;
		app_info = new AppInfo(this);
		
		for (int i = 0; i < mAppConfigInfoList.size(); i++) {
			final int position = i;
			AppConfigInfo app = mAppConfigInfoList.get(i);
			View v = LayoutInflater.from(this).inflate(R.layout.item_sync_setting_guide, null);
			((TextView)v.findViewById(R.id.app_name)).setText(app_info.getAppName(app.getApp_packagename()));
			
			AuroraSwitch as_app = ((AuroraSwitch) v.findViewById(R.id.wlan_switch));
			as_app.setChecked(isSyncEnabled(mAppConfigInfoList.get(position).getApp_packagename()));
			
			as_app.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					SystemUtils.updateAppConfigInfo(SyncSettingGuide2Activity.this, 
							mAppConfigInfoList.get(position).getApp_packagename(), isChecked);
				}
			});
			ll_app.addView(v);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_next:
			
//			CommonUtil.checkAndSetPhotoBackupAlarm();
			CommonUtil.checkAndSetAppBackupAlarm();
			
			openMainActivity(mLoginStatus);
			
			break;
		}
	}
	
	private void openMainActivity(int loginStatus) {
        Log.d(TAG, "Jim, loginStatus: " + loginStatus);
        Intent intent = new Intent(this, SyncAccountActivity.class);
        intent.putExtra(SyncAccountActivity.EXTRA_KEY_LOGIN_STATUS, loginStatus);
        startActivity(intent);
        finish();
    }

    private boolean isSyncEnabled(String packageName) {
        SharedPreferences sts = getSharedPreferences(
                Globals.SHARED_MODULE_SYNC, Context.MODE_PRIVATE);
        return sts.getBoolean(packageName, true);
    }
    
}