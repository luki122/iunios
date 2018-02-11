package com.aurora.setupwizard;

import java.io.File;
import java.util.List;

import com.aurora.setupwizard.adapter.AppAdapter;
import com.aurora.setupwizard.domain.ApkInfo;
import com.aurora.setupwizard.service.InstallService;
import com.aurora.setupwizard.utils.ApkUtil;
import com.aurora.setupwizard.utils.Constants;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

import com.aurora.setupwizard.R;
import com.aurora.utils.Log;

public class AppRecommendActivity extends BaseActivity implements
		OnClickListener, OnItemClickListener {
	
	public static final String APK_AZ_COUNT = "count";
	
	private ListView lvApp;
	private AppAdapter mAdapter;
	private List<ApkInfo> infos;
	private TextView tv_use;
	private TextView tv_pre;
	private TextView tv_recom;
	private TextView tv_select_all;
	private RelativeLayout rl_lv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.activity_app_recommend,
				AuroraActionBar.Type.Empty);
		getAuroraActionBar().setVisibility(View.GONE);

		initView();
		initData();
		initEvent();
	}

	private void initView() {

		rl_lv = (RelativeLayout) findViewById(R.id.rl_list);
		lvApp = (ListView) findViewById(R.id.lv_recommend);
		tv_use = (TextView) findViewById(R.id.tv_next);
		tv_pre = (TextView) findViewById(R.id.tv_pre);
		tv_select_all = (TextView) findViewById(R.id.tv_select_all);
	}

	private void initData() {

		tv_use.setText(R.string.app_skip);

		infos = ApkUtil.getApkInfo(this);

		mAdapter = new AppAdapter(getApplicationContext(), infos);
		lvApp.setAdapter(mAdapter);
	}

	private void initEvent() {
		tv_pre.setOnClickListener(this);
		tv_use.setOnClickListener(this);
		tv_select_all.setOnClickListener(this);
		lvApp.setOnItemClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		change();
		com.aurora.utils.SystemUtils.switchStatusBarColorMode(
				com.aurora.utils.SystemUtils.STATUS_BAR_MODE_BLACK, this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_select_all:
			selectAll();
			break;
		case R.id.tv_next:
			go2Next();
			break;
		case R.id.tv_pre:
			onBackPressed();
			break;

		default:
			break;
		}
	}

	/**
	 * 全选
	 */
	private void selectAll() {
		for (ApkInfo info : infos) {
			info.setCheck(true);
		}
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * 跳过
	 */
	private void go2Pre() {
		Intent intent = new Intent(this, CompleteActivityU5.class);
		startActivity(intent);
	}

	/**
	 * 下一步
	 */
	private void go2Next() {
		int count = getCount();
		for (ApkInfo info : infos) {
			if (info.isCheck()) {
				Log.v("lmjssjj", info.getApkName());
				Intent intent = new Intent(this, InstallService.class);
				intent.putExtra("packageName", info.getApkPackageName());
				intent.putExtra("apkPath", info.getApkPath());
				intent.putExtra("count", count);
				startService(intent);
			}
		}
		
		if(count==0){
			File file = new File(Constants.APK_FOLDER);
			if (file != null)
				android.util.Log.v("lmjssjj", "delete file");
				ApkUtil.deleteFile(file);
		}

		Intent intent = new Intent(this, CompleteActivityU5.class);
		intent.putExtra(APK_AZ_COUNT, count);
		startActivity(intent);
	}

	public int getCount() {
		int i = 0;
		for (ApkInfo info : infos) {
			if (info.isCheck()) {
				i++;
			}
		}
		return i;
	}

	public void change() {
		boolean b = false;
		for (ApkInfo info : infos) {
			if (info.isCheck()) {
				b = true;
				break;
			}
		}
		if (b) {
			tv_use.setText(R.string.anzhuang);
		} else {
			tv_use.setText(R.string.app_skip);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Log.v("lmjssjj", position+"");
		infos.get(position).setCheck(!infos.get(position).isCheck());
		mAdapter.notifyDataSetChanged();
		change();
	}
}
