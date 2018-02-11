package com.aurora.setupwizard;

import com.aurora.setupwizard.utils.ApkUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

public class FingerPrintSettingActivity extends BaseActivity implements
		OnClickListener, OnCheckedChangeListener {

	private RadioButton rb_use;
	private RadioButton rb_dontuse;
	private RelativeLayout rl_use;
	private RelativeLayout rl_dontuse;

	private TextView tv_pre;
	private TextView tv_next;

	private boolean isUse = true;
	private boolean mIsRec;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.activity_set_finger,
				AuroraActionBar.Type.Empty);
		getAuroraActionBar().setVisibility(View.GONE);
		mIsRec = ApkUtil.isRecommend();
		initView();
		initData();
		initEvent();
	}

	private void initView() {
		tv_pre = (TextView) findViewById(R.id.tv_pre);
		tv_next = (TextView) findViewById(R.id.tv_next);
		rb_dontuse = (RadioButton) findViewById(R.id.rb_dontuse);
		rb_use = (RadioButton) findViewById(R.id.rb_use);

		rl_use = (RelativeLayout) findViewById(R.id.ll_finger_use);
		rl_dontuse = (RelativeLayout) findViewById(R.id.rl_finger_nouse);
	}

	private void initData() {
		rb_use.setChecked(isUse);

	}

	private void initEvent() {
		tv_pre.setOnClickListener(this);
		tv_next.setOnClickListener(this);
		rb_use.setOnCheckedChangeListener(this);
		rb_dontuse.setOnCheckedChangeListener(this);
		rl_dontuse.setOnClickListener(this);
		rl_use.setOnClickListener(this);
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
		case R.id.tv_pre:
			onBackPressed();
			break;
		case R.id.tv_next:
			getNext();
			break;
		case R.id.ll_finger_use:
			if(!rb_use.isChecked()){
				rb_use.setChecked(!rb_use.isChecked());
			}
			break;
		case R.id.rl_finger_nouse:
			if(!rb_dontuse.isChecked()){
				rb_dontuse.setChecked(!rb_dontuse.isChecked());
			}
			break;

		default:
			break;
		}
	}

	public void getNext() {
		if (rb_use.isChecked()) {
			go2AddFinger();
		} else {
			go2Next();
		}
	}

	private void go2AddFinger() {
		Intent intent = new Intent();
		intent.setAction("com.android.settings.action.fingerprint");
		intent.putExtra("finger_mode", 2);
		startActivityForResult(intent, 1);
	}

	private void go2Next() {
		if (mIsRec) {
			Intent intent = new Intent();
			intent.setClass(this, AppRecommendActivity.class);
			startActivity(intent);
		} else {
			Intent intent = new Intent();
			intent.setClass(this, CompleteActivityU5.class);
			startActivity(intent);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton v, boolean b) {

		switch (v.getId()) {
		case R.id.rb_use:
			rb_use.setChecked(b);
			rb_dontuse.setChecked(!b);
			break;
		case R.id.rb_dontuse:
			rb_use.setChecked(!b);
			rb_dontuse.setChecked(b);
			break;

		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 0) {

		} else
			go2Next();
	}

}
