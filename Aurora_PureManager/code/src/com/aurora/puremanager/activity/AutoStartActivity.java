package com.aurora.puremanager.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

import com.aurora.puremanager.R;
import com.aurora.puremanager.data.AppInfo;
import com.aurora.puremanager.data.AppsInfo;
import com.aurora.puremanager.data.AutoStartData;
import com.aurora.puremanager.data.BaseData;
import com.aurora.puremanager.imageloader.ImageCallback;
import com.aurora.puremanager.imageloader.ImageLoader;
import com.aurora.puremanager.model.AutoStartModel;
import com.aurora.puremanager.model.ConfigModel;
import com.aurora.puremanager.provider.open.AutoStartAppProvider;
import com.aurora.puremanager.provider.open.FreezedAppProvider;
import com.aurora.puremanager.utils.ActivityUtils;
import com.aurora.puremanager.utils.ActivityUtils.LoadCallback;
import com.aurora.puremanager.utils.ApkUtils;
import com.aurora.puremanager.utils.LogUtils;
import com.aurora.puremanager.utils.Utils;
import com.aurora.puremanager.utils.mConfig;
import com.aurora.puremanager.view.InfoDialog;
import com.aurora.puremanager.viewcache.AutoStartCache;

public class AutoStartActivity extends AuroraActivity implements
		OnClickListener {

	private final int REQUEST_CODE_OF_ADD_APP = 1;
	private List<BaseData> autoStartAppList;
	private AutoStartManageAdapter adapter;
	private ListView ListView;

	private HashSet<String> autostartApps;
	private AppsInfo userAppsInfo;
	private AutoStartData autoStartData;
	private AppInfo appInfo;
	private AutoStartCache holder;
	private LinearLayout mHeaderView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (mConfig.isNative) {
			setContentView(R.layout.auto_start_manage_activity);
		} else {
			setAuroraContentView(R.layout.auto_start_manage_activity,
					AuroraActionBar.Type.Normal);
			getAuroraActionBar().setTitle(R.string.auto_start_manage);
			getAuroraActionBar().setBackgroundResource(
					R.color.app_manager_title_color);
		}
		initView();
		ActivityUtils.sleepForloadScreen(100, new LoadCallback() {
			@Override
			public void loaded() {
//				AutoStartModel.getInstance(AutoStartActivity.this).attach(
//						updateViewHandler);
//				updateViewHandler.sendEmptyMessage(0);
				initOrUpdatetListData();
			}
		});
	}

	private void initView() {
		// findViewById(R.id.exclamationImg).setOnClickListener(this);
		ListView = (ListView) findViewById(R.id.ListView);
		mHeaderView = (LinearLayout) getLayoutInflater().inflate(
				R.layout.auto_start_list_header, null);
		ListView.addHeaderView(mHeaderView);
		mHeaderView.findViewById(R.id.exclamationImg).setOnClickListener(this);
	}

//	private final Handler updateViewHandler = new Handler() {
//		@Override
//		public void handleMessage(Message msg) {
//			initOrUpdatetListData();
//		}
//	};

	/**
	 * a new version of initOrUpdatetListData According to new requirement, auto
	 * start app list should be loaded from database Vulcan created this method
	 * in 2015年1月13日 下午2:46:52 .
	 */
	public void initOrUpdatetListData2() {
		if (ListView == null) {
			return;
		}
		if (autoStartAppList == null) {
			autoStartAppList = new ArrayList<BaseData>();
		} else {
			autoStartAppList.clear();
		}

		autostartApps = AutoStartAppProvider
				.loadAutoStartAppListInDB(getApplicationContext());

		HashSet<String> freezedApps = FreezedAppProvider
				.loadFreezedAppListInDB(getApplicationContext());

		for (String app : autostartApps) {
			LogUtils.printWithLogCat("vautostart",
					"initOrUpdatetListData2: =====autostartList: " + app);
		}

		userAppsInfo = ConfigModel.getInstance(this).getAppInfoModel()
				.getThirdPartyAppsInfo();
		if (userAppsInfo == null) {
			return;
		}

		for (int i = 0; i < userAppsInfo.size(); i++) {
			appInfo = (AppInfo) userAppsInfo.get(i);
			if (appInfo == null || !appInfo.getIsInstalled()) {
				continue;
			}

			autoStartData = AutoStartModel.getInstance(this).getAutoStartData(
					appInfo.getPackageName());
			if (autoStartData == null) {
				continue;
			}

			ApkUtils.initAppNameInfo(this, appInfo);
			if (!freezedApps.contains(appInfo.getPackageName())) {
				autoStartAppList.add(appInfo);
			}
		}

		sortList(autoStartAppList);

		if (adapter == null) {
			adapter = new AutoStartManageAdapter(this, autoStartAppList);
			ListView.setAdapter(adapter);
		} else {
			adapter.notifyDataSetChanged();
		}

		if (autoStartAppList.size() == 0) {
			findViewById(R.id.NoAppLayout).setVisibility(View.VISIBLE);
			findViewById(R.id.HaveAppLayout).setVisibility(View.GONE);
		} else {
			findViewById(R.id.NoAppLayout).setVisibility(View.GONE);
			findViewById(R.id.HaveAppLayout).setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 
	 * Vulcan created this method in 2015年1月13日 下午5:06:27 .
	 */
	public void initOrUpdatetListData1() {
		if (ListView == null) {
			return;
		}
		if (autoStartAppList == null) {
			autoStartAppList = new ArrayList<BaseData>();
		} else {
			autoStartAppList.clear();
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

			AutoStartData autoStartData = AutoStartModel.getInstance(this)
					.getAutoStartData(appInfo.getPackageName());
			if (autoStartData == null) {
				continue;
			}

			if (autoStartData.getIsOpen()) {
				ApkUtils.initAppNameInfo(this, appInfo);
				autoStartAppList.add(appInfo);
			}
		}

		sortList(autoStartAppList);

		if (adapter == null) {
			adapter = new AutoStartManageAdapter(this, autoStartAppList);
			ListView.setAdapter(adapter);
		} else {
			adapter.notifyDataSetChanged();
		}

		if (autoStartAppList.size() == 0) {
			findViewById(R.id.NoAppLayout).setVisibility(View.VISIBLE);
			findViewById(R.id.HaveAppLayout).setVisibility(View.GONE);
		} else {
			findViewById(R.id.NoAppLayout).setVisibility(View.GONE);
			findViewById(R.id.HaveAppLayout).setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 更新数据
	 */
	public void initOrUpdatetListData() {
		initOrUpdatetListData2();
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
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.exclamationImg:
			InfoDialog.showDialog(this, R.string.what_is_auto_start,
					R.string.auto_start_explain, R.string.sure);
			break;
		}
	}

	@Override
	protected void onDestroy() {
//		AutoStartModel.getInstance(this).detach(updateViewHandler);
		releaseObject();
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_CODE_OF_ADD_APP:
			initOrUpdatetListData();
			break;
		}
	}

	/**
	 * 释放不需要用的对象所占用的堆内存
	 */
	private void releaseObject() {
		if (autoStartAppList != null) {
			autoStartAppList.clear();
		}
	}

	public class AutoStartManageAdapter extends ArrayAdapter<BaseData>
			implements OnCheckedChangeListener {

		public AutoStartManageAdapter(Activity activity, List<BaseData> listData) {
			super(activity, 0, listData);
		}

		@Override
		public View getView(int position, View convertView,
				final ViewGroup parent) {

			if (convertView == null) {
				LayoutInflater inflater = ((Activity) getContext())
						.getLayoutInflater();
				convertView = inflater.inflate(R.layout.auto_start_list_item,
						parent, false);
				holder = new AutoStartCache(convertView);
				convertView.setTag(holder);
			} else {
				holder = (AutoStartCache) convertView.getTag();
			}

			if (getCount() <= position) {
				return convertView;
			}

			AppInfo item = (AppInfo) getItem(position);
			holder.getAppName().setText(item.getAppName());
			holder.getAutoStartSwitch().setTag(position);
			holder.getAutoStartSwitch().setOnCheckedChangeListener(this);

			String pkgName = item.getPackageName();
			if (autostartApps.contains(pkgName)) {
				holder.getAutoStartSwitch().setChecked(true);
			} else {
				holder.getAutoStartSwitch().setChecked(false);
			}

			String iconViewTag = item.getPackageName() + "@app_icon";
			holder.getAppIcon().setTag(iconViewTag);
			Drawable cachedImage = ImageLoader.getInstance(getContext())
					.displayImage(holder.getAppIcon(), item.getPackageName(),
							iconViewTag, new ImageCallback() {
								public void imageLoaded(Drawable imageDrawable,
										Object viewTag) {
									if (parent == null || imageDrawable == null
											|| viewTag == null) {
										return;
									}
									ImageView imageViewByTag = (ImageView) parent
											.findViewWithTag(viewTag);
									if (imageViewByTag != null) {
										imageViewByTag
												.setImageDrawable(imageDrawable);
									}
								}
							});
			if (cachedImage != null) {
				holder.getAppIcon().setImageDrawable(cachedImage);
			} else {
				holder.getAppIcon().setImageResource(R.drawable.def_app_icon);
			}
			return convertView;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// TODO Auto-generated method stub
			Object tagObject = buttonView.getTag();
			if (tagObject != null) {
				int position = Integer.parseInt(tagObject.toString());
				if (getCount() <= position) {
					return;
				}

				final AppInfo item = (AppInfo) getItem(position);
				switch (buttonView.getId()) {
				case R.id.autoStartSwitch:
					if (isChecked == false) {
						if (autostartApps != null
								&& autostartApps
										.contains(item.getPackageName())) {
							InfoDialog.showAutoDialog(
									(Activity) getContext(),
									R.string.forbit_auto_start,
									android.R.attr.alertDialogIcon,
									R.string.are_you_sure_forbit_auto_start,
									R.string.sure,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											AutoStartModel
													.getInstance(getContext())
													.tryChangeAutoStartState(
															item.getPackageName(),
															false);
											if (getContext() instanceof AutoStartActivity) {
												((AutoStartActivity) getContext())
														.initOrUpdatetListData();
											}
										}
									}, R.string.cancel,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											notifyDataSetChanged();
										}
									}, new DialogInterface.OnDismissListener() {
										@Override
										public void onDismiss(DialogInterface arg0) {
											// TODO Auto-generated method stub
											notifyDataSetChanged();
										}
									});
						}
					} else {
						AutoStartModel.getInstance(getContext())
								.tryChangeAutoStartState(item.getPackageName(),
										true);
					}
					break;
				}
			}
		}
	}
}
