package com.aurora.puremanager.activity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

import com.aurora.puremanager.R;
import com.aurora.puremanager.adapter.AppManageListAdapter;
import com.aurora.puremanager.data.BaseData;
import com.aurora.puremanager.data.MainActivityItemData;
import com.aurora.puremanager.interfaces.Observer;
import com.aurora.puremanager.interfaces.Subject;
import com.aurora.puremanager.model.ConfigModel;
import com.aurora.puremanager.totalCount.TotalCount;
import com.aurora.puremanager.utils.StorageUtil;
import com.aurora.puremanager.utils.mConfig;

public class AppManageActivity extends AuroraActivity implements Observer,
		OnItemClickListener {

	private Context mContext;
	private ArrayList<BaseData> itemList;
	private AppManageListAdapter adapter;
	private TextView mAllApp, mStorager, mInStorager, mSDStorager;
	private ProgressBar mProgress, mInprogress, mSDprogress;
	private LinearLayout mStoragerlayout, mInternallayout, mSDlayout;
	private View mView;
	private int mAppNum = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		if (mConfig.isNative) {
			setContentView(R.layout.app_manage_activity);
		} else {
			setAuroraContentView(R.layout.app_manage_activity,
					AuroraActionBar.Type.Normal);
			getAuroraActionBar().setTitle(R.string.app_manage);
			// getAuroraActionBar().setBackgroundColor(R.color.app_manager_title_color);
		}
		initView();
		initData();
	}

	private void initView() {
		mStoragerlayout = (LinearLayout) findViewById(R.id.storager_layout);
		mInternallayout = (LinearLayout) findViewById(R.id.internal_layout);
		mSDlayout = (LinearLayout) findViewById(R.id.sd_layout);

		mAllApp = (TextView) findViewById(R.id.app_num);
		mProgress = (ProgressBar) findViewById(R.id.my_progress);
		mStorager = (TextView) findViewById(R.id.storager);

		mInStorager = (TextView) findViewById(R.id.in_storager);
		mSDStorager = (TextView) findViewById(R.id.sd_storager);
		mInprogress = (ProgressBar) findViewById(R.id.in_progress);
		mSDprogress = (ProgressBar) findViewById(R.id.sd_progress);

		mView = findViewById(R.id.view);

		ConfigModel.getInstance(this).getAppInfoModel()
				.attach(AppManageActivity.this);
		if (ConfigModel.getInstance(this).getAppInfoModel()
				.isAlreadyGetAllAppInfo()) {
			updateOfInit(ConfigModel.getInstance(this).getAppInfoModel());
			int UserApp = ConfigModel.getInstance(this).getAppInfoModel()
					.getUserAppsNum();
			int sysApp = ConfigModel.getInstance(this).getAppInfoModel()
					.getSysAppsNum();
			mAppNum = UserApp + sysApp;
		}
		mAllApp.setText(String.valueOf(mAppNum));
		mAllApp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(AppManageActivity.this,AllAppListActivity.class));
			}
		});

		StorageUtil.getInstance(this).attach(updateCacheManageViewHandler);
		updateCacheManageViewHandler.sendEmptyMessage(0);
	}

	private void initData() {
		// 初始化listItem数据
		if (itemList == null) {
			itemList = new ArrayList<BaseData>();
		} else {
			itemList.clear();
		}

		// 自启管理
		MainActivityItemData itemData = new MainActivityItemData();
		itemData.setIconRes(R.drawable.app_automatic);
		itemData.setItemName(getString(R.string.auto_start_manage));
		itemData.setComponentName(new ComponentName(this,AutoStartManageActivity.class));
		itemData.setHintStrFront("0");
		itemList.add(itemData);

		// 应用卸载
		itemData = new MainActivityItemData();
		itemData.setIconRes(R.drawable.app_delete);
		itemData.setItemName(getString(R.string.app_uninstall));
		 itemData.setComponentName(new ComponentName(this,UninstallAppActivity.class));
		itemData.setHintStrFront("0");
		itemList.add(itemData);

		// 应用冻结
		itemData = new MainActivityItemData();
		itemData.setIconRes(R.drawable.app_freezeon);
		itemData.setItemName(getString(R.string.app_freezeon));
		 itemData.setComponentName(new ComponentName(this,FreezeAppActivity.class));
		itemData.setHintStrFront("0");
		itemList.add(itemData);

		// 默认应用
		itemData = new MainActivityItemData();
		itemData.setIconRes(R.drawable.app_defaults);
		itemData.setItemName(getString(R.string.def_soft_manage));
		 itemData.setComponentName(new ComponentName(this,DefSoftManageActivity.class));
		itemData.setHintStrFront("0");
		itemList.add(itemData);

		if (adapter == null) {
			adapter = new AppManageListAdapter(this, itemList);
			GridView gridView = (GridView) findViewById(R.id.app_gridView);
			gridView.setAdapter(adapter);
			gridView.setOnItemClickListener(this);
		} else {
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * 更新内部存储使用空间
	 * 
	 * @param internalAvailable
	 *            内部剩余空间
	 * @param internalTotal
	 *            内部存储总大小
	 * @param sdCard_Available
	 *            SD卡剩余空间
	 * @param cdCard_Total
	 *            SD卡总大小
	 */
	private void initInternalView(long internalAvailable, long internalTotal,
			long sdCard_Available, long sdCard_Total) {
		String Already_storage = Formatter.formatFileSize(getBaseContext(),
				internalTotal - internalAvailable);
		String All_storage = Formatter.formatFileSize(getBaseContext(),
				internalTotal);

		if (internalTotal > 0) {
			float system = Float.parseFloat(All_storage.substring(0,
					All_storage.length() - 2));
			float user = Float.parseFloat(Already_storage.substring(0,
					Already_storage.length() - 2));
			if (user > system) {
				user = user / 1024;
			}
			float availMemory = user / system;
			DecimalFormat decimalFormat = new DecimalFormat("0.00");
			String Percent = decimalFormat.format(availMemory).substring(2);

			int pro = Integer.parseInt(Percent);

			if (sdCard_Total > 0) {
				mStoragerlayout.setVisibility(View.GONE);
				mInternallayout.setVisibility(View.VISIBLE);
				mSDlayout.setVisibility(View.VISIBLE);
				mView.setVisibility(View.VISIBLE);

				String Already_SDCard = Formatter.formatFileSize(
						getBaseContext(), sdCard_Total - sdCard_Available);
				String All_SDCard = Formatter.formatFileSize(getBaseContext(),
						sdCard_Total);

				float sdCard = Float.parseFloat(All_SDCard.substring(0,
						Already_SDCard.length() - 2));
				float user_sd = Float.parseFloat(Already_SDCard.substring(0,
						Already_SDCard.length() - 2));
				if (user_sd > sdCard) {
					user_sd = user_sd / 1024;
				}
				float availSD = user_sd / sdCard;
				DecimalFormat sd_decimalFormat = new DecimalFormat("0.00");
				String sd_Percent = sd_decimalFormat.format(availSD).substring(
						2);

				int sd_pro = Integer.parseInt(sd_Percent);

				mInStorager.setText(getResources().getText(
						R.string.already_user)
						+ Already_storage + "/" + All_storage);
				mInprogress.setProgress(pro);

				mSDStorager.setText(getResources().getText(
						R.string.already_user)
						+ Already_SDCard + "/" + All_SDCard);
				mSDprogress.setProgress(sd_pro);

			} else {
				mStoragerlayout.setVisibility(View.VISIBLE);
				mInternallayout.setVisibility(View.GONE);
				mSDlayout.setVisibility(View.GONE);
				mView.setVisibility(View.GONE);

				mStorager.setText(getResources().getText(R.string.already_user)
						.toString() + Already_storage + "/" + All_storage);
				mProgress.setProgress(pro);
			}
		}
	}

	/**
	 * 更新可用空间的textView
	 */
	private final Handler updateCacheManageViewHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			long internalAvailable = StorageUtil.getInstance(mContext)
					.getAvailableInternalMemorySize();
			long internalTotal = StorageUtil.getInstance(mContext)
					.getTotalInternalSDMemorySize();

			long sdCard_Available = StorageUtil.getInstance(mContext)
					.getAvailableExternalMemorySize();
			long sdCard_Total = StorageUtil.getInstance(mContext)
					.getTotalExternalSDMemorySize();

			if (internalAvailable == StorageUtil.ERROR)
				internalAvailable = 0;
			if (internalTotal == StorageUtil.ERROR)
				internalTotal = 0;

			if (sdCard_Available == StorageUtil.ERROR)
				sdCard_Available = 0;
			if (sdCard_Total == StorageUtil.ERROR)
				sdCard_Total = 0;

			initInternalView(internalAvailable, internalTotal,
					sdCard_Available, sdCard_Total);
		}
	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		StorageUtil.getInstance(this).detach(updateCacheManageViewHandler);
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

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		if (adapter != null && arg2 < adapter.getCount()) {
			MainActivityItemData item = (MainActivityItemData) adapter
					.getItem(arg2);
			if (item == null) {
				return;
			}
			// add data count
			String itemname = item.getItemName();

			if (itemname.equals(getString(R.string.auto_start_manage))) {
				new TotalCount(AppManageActivity.this, "1", 1).CountData();
			}else if (itemname.equals(getString(R.string.app_uninstall))) {
				new TotalCount(AppManageActivity.this, "2", 1).CountData();
			}else if (itemname.equals(getString(R.string.app_freezeon))) {
				new TotalCount(AppManageActivity.this, "3", 1).CountData();
			} else if (itemname.equals(getString(R.string.def_soft_manage))) {
				new TotalCount(AppManageActivity.this, "4", 1).CountData();
			}
			Intent intent = new Intent();
			intent.setComponent(item.getComponentName());
			startActivity(intent);
		}
	}
}
