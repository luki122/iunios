package com.aurora.setupwizard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.setupwizard.navigationbar.SetupWizardNavBar;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

public class SimCardMissingActivity extends BaseActivity implements SetupWizardNavBar.NavigationBarListener, OnClickListener {

	private ImageView iv_icon;
	private TextView tv_info_title;
	
	private TextView tv_pre;
	private TextView tv_next;

	public static final String ACTION_SETUP_WIFI = "com.android.net.wifi.SETUP_WIFI_NETWORK";
	public static final String EXTRA_FIRST_RUN = "firstRun";
	public static final String EXTRA_ALLOW_SKIP = "allowSkip";
	public static final String EXTRA_AUTO_FINISH = "wifi_auto_finish_on_connect";
	public static final String EXTRA_USE_IMMERSIVE = "useImmersiveMode";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.activity_sim_u5, AuroraActionBar.Type.Empty);
		getAuroraActionBar().setVisibility(View.GONE);
		initViews();
		initData();
		initEvent();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		com.aurora.utils.SystemUtils.switchStatusBarColorMode(
				com.aurora.utils.SystemUtils.STATUS_BAR_MODE_BLACK, this);
	}

	private void initViews() {
	//	iv_icon = (ImageView) findViewById(R.id.iv_icon);
	//	tv_info_title = (TextView) findViewById(R.id.tv_info_title);
		
		tv_pre = (TextView)findViewById(R.id.tv_pre);
		tv_next = (TextView)findViewById(R.id.tv_next);
		tv_next.setText(R.string.app_skip);

	}

	private void initData() {
//		iv_icon.setImageResource(R.drawable.ic_sim);
//		tv_info_title.setText(getString(R.string.scma_insert_sim_card));

		IntentFilter filter = new IntentFilter(
				"android.intent.action.SIM_STATE_CHANGED");
		registerReceiver(receiver, filter);
	}
	
	private void initEvent(){
		tv_pre.setOnClickListener(this);
		tv_next.setOnClickListener(this);
	}

	@Override

	protected void onDestroy() {
		super.onDestroy();

		unregisterReceiver(receiver);
	}

	private void goNextPage() {
		//Intent i = new Intent(this, WifiPageActivity.class);
		//startActivity(i);
		Intent intent = new Intent(ACTION_SETUP_WIFI);
		intent.putExtra(EXTRA_FIRST_RUN, true);
		intent.putExtra(EXTRA_ALLOW_SKIP, true);
		intent.putExtra(EXTRA_USE_IMMERSIVE, true);
		intent.putExtra(EXTRA_AUTO_FINISH, false);
		startActivity(intent);
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (SetupWizardUtils.hasTelephony(SimCardMissingActivity.this)) {
				if (SetupWizardUtils.isSimInserted(SimCardMissingActivity.this)) {
					goNextPage();
					finish();
				}
			}
		}

	};

	@Override
	public void onNavigationBarCreated(SetupWizardNavBar bar) {
		bar.getNextButton().setText(R.string.skip);
	}

	@Override
	public void onNavigateBack() {
		onBackPressed();
	}

	@Override
	public void onNavigateNext() {
		goNextPage();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_pre:
			onBackPressed();
			break;
	case R.id.tv_next:
			goNextPage();
			break;

		default:
			break;
		}
	}
}
