package com.aurora.puremanager.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraListView;

import com.aurora.puremanager.R;
import com.aurora.puremanager.adapter.UninstallAppAdapter;
import com.aurora.puremanager.data.AppInfo;
import com.aurora.puremanager.data.AppsInfo;
import com.aurora.puremanager.data.BaseData;
import com.aurora.puremanager.imageloader.ImageCallback;
import com.aurora.puremanager.imageloader.ImageLoader;
import com.aurora.puremanager.interfaces.Observer;
import com.aurora.puremanager.interfaces.Subject;
import com.aurora.puremanager.model.ConfigModel;
import com.aurora.puremanager.utils.ActivityUtils;
import com.aurora.puremanager.utils.ActivityUtils.LoadCallback;
import com.aurora.puremanager.utils.ApkUtils;
import com.aurora.puremanager.utils.Utils;
import com.aurora.puremanager.utils.mConfig;
import com.aurora.puremanager.viewcache.UninstallAppCache;

public class UninstallAppActivity extends AuroraActivity implements Observer{
	private List<BaseData> AppList;
	private UninstallAppAdapter adapter;
	private ListView ListView;
	private TextView mAppNum;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (mConfig.isNative) {
			setContentView(R.layout.uninstall_activity);
		} else {
			setAuroraContentView(R.layout.uninstall_activity,
					AuroraActionBar.Type.Normal);
			getAuroraActionBar().setTitle(R.string.app_uninstall);
		}
		ConfigModel.getInstance(this).getAppInfoModel().attach(this);
		initView();
		ActivityUtils.sleepForloadScreen(100, new LoadCallback() {
			@Override
			public void loaded() {
				updateViewHandler.sendEmptyMessage(0);
			}
		});
	}

	@Override
	protected void onPause() {
		if (ListView != null) {
			((AuroraListView) ListView).auroraOnPause();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		if (ListView != null) {
			((AuroraListView) ListView).auroraOnResume();
		}
		super.onResume();
	}

	private void initView() {
		ListView = (ListView) findViewById(R.id.ListView);
		mAppNum = (TextView) findViewById(R.id.app_num);
	}

	private final Handler updateViewHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			initOrUpdateListData();
		}
	};

	/**
	 * 更新数据
	 */
	public void initOrUpdateListData() {
		if (ListView == null) {
			return;
		}
		if (AppList == null) {
			AppList = new ArrayList<BaseData>();
		} else {
			AppList.clear();
		}

		AppsInfo userAppsInfo = ConfigModel.getInstance(this).getAppInfoModel()
				.getThirdPartyAppsInfo();
		if (userAppsInfo == null) {
			return;
		}

		for (int i = 0; i < userAppsInfo.size(); i++) {
			AppInfo appInfo = (AppInfo) userAppsInfo.get(i);
			if (appInfo == null || !appInfo.getIsInstalled()) {
				continue;
			}

			ApkUtils.initAppNameInfo(this, appInfo);
			AppList.add(appInfo);
		}

		sortList(AppList);

		if (adapter == null) {
			adapter = new UninstallAppAdapter(this, AppList);
			ListView.setAdapter(adapter);
		} else {
			adapter.notifyDataSetChanged();
		}

		if (AppList.size() == 0) {
			findViewById(R.id.NoAppLayout).setVisibility(View.VISIBLE);
			findViewById(R.id.ListView).setVisibility(View.GONE);
		} else {
			findViewById(R.id.NoAppLayout).setVisibility(View.GONE);
			findViewById(R.id.ListView).setVisibility(View.VISIBLE);
			mAppNum.setText(String.valueOf(AppList.size())
					+ getResources().getString(R.string.app_num));
		}
	}

	private void sortList(List<BaseData> appsList) {
		Collections.sort(appsList, new Comparator<BaseData>() {
			public int compare(BaseData s1, BaseData s2) {
				return Utils.compare(((AppInfo) s1).getAppNamePinYin(),
						((AppInfo) s2).getAppNamePinYin());
			}
		});
	}

	@Override
	public void onBackPressed() {
		exitSelf();
	}

	@Override
	protected void onDestroy() {
		releaseObject();
		super.onDestroy();
	}

	public void exitSelf() {
		finish();
	}

	/**
	 * 释放不需要用的对象所占用的堆内存
	 */
	private void releaseObject() {
		if (AppList != null) {
			AppList.clear();
		}
	}

	@Override
	public void updateOfInit(Subject subject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOfInStall(Subject subject, String pkgName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOfCoverInStall(Subject subject, String pkgName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOfUnInstall(Subject subject, String pkgName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOfRecomPermsChange(Subject subject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOfExternalAppAvailable(Subject subject,
			List<String> pkgList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOfExternalAppUnAvailable(Subject subject,
			List<String> pkgList) {
		// TODO Auto-generated method stub
		
	}
}
