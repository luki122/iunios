package com.aurora.downloader;

import java.util.ArrayList;
import java.util.List;

import android.app.DownloadManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;

import com.aurora.downloader.util.AuroraDownloadManager;
import com.aurora.downloader.util.AuroraLog;
import com.aurora.downloader.util.IntentBuilder;
import com.aurora.downloader.util.NetWorkConectUtil;
import com.aurora.downloader.util.OperationAction;

import android.provider.Downloads;

/**
 * 
 * @author jiangxh
 * @CreateTime 2014年5月28日 上午10:55:28
 * @Description com.aurora.downloader DownloadActivity.java
 */
public class DownloadActivity extends AuroraActivity {

	public AuroraActionBar auroraActionBar;
	public static String mSDCardPath;
	private static final String TAG = "DownloadActivity";
	private Fragment currentFragment;
	private int currentType;
	public OperationAction operationAction;
	public static final boolean uMeng= true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setAuroraContentView(R.layout.activity_download,
				AuroraActionBar.Type.Normal, false);
		auroraActionBar = getAuroraActionBar();
		auroraActionBar.addItem(AuroraActionBarItem.Type.Edit,
				R.id.aurora_action_bar_all_edit);
		setAuroraBottomBarMenuCallBack(auroraMenuCallBack);
		
		// actionBar点击监听
		auroraActionBar
				.setOnAuroraActionBarListener(actionBarItemClickListener);
		auroraActionBar
				.setmOnActionBarBackItemListener(actionBarBackItemClickListener);
		mSDCardPath = gionee.os.storage.GnStorageManager.getInstance(
				DownloadActivity.this.getApplicationContext())
				.getInternalStoragePath();

		if (mSDCardPath != null) {
			showDownloadListView();
		} else {
			showNoSdView();
		}
		registerBroadcastReceiver();
	}
	
	

	@Override
	protected void onPause() {
		super.onPause();
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		
	}



	private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {

		@Override
		public void auroraMenuItemClick(int item) {
			switch (item) {
			case R.id.share_download:
				if (currentFragment instanceof DownloadFragment) {
					List<FileInfo> selectPath = ((DownloadFragment) currentFragment)
							.getSelectPath();
					if (selectPath.size() == 0) {
						return;
					}
					if (selectPath.size() > Integer.valueOf(getResources()
							.getString(R.string.share_max))) {
						Toast.makeText(
								DownloadActivity.this,
								getResources().getString(
										R.string.share_too_much),
								Toast.LENGTH_SHORT).show();
						return;
					}
					Intent intent = IntentBuilder.buildSendFile(selectPath);
					startActivity(Intent.createChooser(intent,
							getString(R.string.share_download)));
				}
				break;
			case R.id.delete_download:
				operationAction.deleteFiles();
				break;

			default:
				break;
			}
		}
	};

	private OnAuroraActionBarItemClickListener actionBarItemClickListener = new OnAuroraActionBarItemClickListener() {

		@Override
		public void onAuroraActionBarItemClicked(int item) {
			switch (item) {
			case R.id.aurora_action_bar_all_edit:
				if (currentFragment instanceof DownloadFragment) {
					if(((DownloadFragment) currentFragment).auroraSetRubbishBackBool()){
						return;
					}
					((DownloadFragment) currentFragment)
							.showEditViewByActivity();
				}
				break;

			default:
				break;
			}
		}
	};
	/*
	 * actionBar返回监听
	 */
	private OnAuroraActionBarBackItemClickListener actionBarBackItemClickListener = new OnAuroraActionBarBackItemClickListener() {

		@Override
		public void onAuroraActionBarBackItemClicked(int item) {
			AuroraLog.log(TAG, "OnAuroraActionBarBackItemClickListener item=="
					+ item);
			if (item == -1) {
				onBackPressed();
			}
		}
	};

	public static final int DOWNLOADVIEW = 1;
	public static final int NOSDVIEW = 2;

	/**
	 * 显示下载列表
	 */
	private void showDownloadListView() {
		DownloadFragment fragment = new DownloadFragment();
		FragmentManager fragmentManager = getFragmentManager();
		Fragment old = fragmentManager.findFragmentById(R.id.container);
		if (old != null) {
			old.onDetach();
		}
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.replace(R.id.container, fragment);
		ft.commitAllowingStateLoss();
		setCurrentFragment(fragment, DOWNLOADVIEW);
		operationAction = new OperationAction(DownloadActivity.this, fragment);
	}

	/**
	 * 显示SD卡不可用界面
	 */
	private void showNoSdView() {
		SdNotAvailableFragment fragment = new SdNotAvailableFragment();
		FragmentManager fragmentManager = getFragmentManager();
		Fragment old = fragmentManager.findFragmentById(R.id.container);
		if (old != null) {
			old.onDetach();
		}
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.replace(R.id.container, fragment);
		ft.commitAllowingStateLoss();
		setCurrentFragment(fragment, NOSDVIEW);
	}

	/**
	 * 设置fragment标记
	 * 
	 * @param fragment
	 * @param type
	 */
	public void setCurrentFragment(Fragment fragment, int type) {
		try {
			AuroraLog.log(TAG, "fragment=" + fragment + " type=" + type);
		} catch (Exception e) {
			e.printStackTrace();
		}
		currentFragment = fragment;
		currentType = type;
	}

	public interface IBackPressedListener {
		boolean onBack();
	}

	@Override
	public void onBackPressed() {
		switch (currentType) {
		case DOWNLOADVIEW:
			currentFragment = (DownloadFragment) currentFragment;
			break;
		case NOSDVIEW:
			currentFragment = (SdNotAvailableFragment) currentFragment;
			break;

		default:
			break;
		}
		AuroraLog.log(TAG, "currentType==" + currentType);
		IBackPressedListener backPressedListener = (IBackPressedListener) currentFragment;
		if (backPressedListener != null && !backPressedListener.onBack()) {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AuroraLog.elog(TAG, "KeyEvent.KEYCODE_BACK");
			onBackPressed();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unRegisterBroadcastReceiver();
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(0, 0);
//		overridePendingTransition(0, R.anim.no_anim);
	}
	
	/**
	 * 判断编辑动画是否播放完成
	 * 
	 * @return
	 */
	public boolean actionBarIsAnimRunning() {
		if (auroraActionBar == null) {// 快速点击图片和手机返回键
			return true;
		}
		if (auroraActionBar.auroraIsEntryEditModeAnimRunning()
				|| auroraActionBar.auroraIsExitEditModeAnimRunning()) {
			return true;
		}
		return false;
	}

	/**
	 * 下载完成广播start
	 */
	private DownlaodBroadcastReceiver downlaodReceiver;

	private void registerBroadcastReceiver() {
		downlaodReceiver = new DownlaodBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		intentFilter
				.addAction(AuroraDownloadManager.GN_ACTION_DOWNLOAD_COMPLETE);
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(downlaodReceiver, intentFilter);
	}

	private void unRegisterBroadcastReceiver() {
		if (downlaodReceiver != null) {
			unregisterReceiver(downlaodReceiver);
		}
	}

	private class DownlaodBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(!DownloadFragment.isPause){
				String action = intent.getAction();
				AuroraLog.elog(TAG, "action==" + action);
				if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)&&!NetWorkConectUtil.hasNetWorkConection(context)){
					Toast.makeText(
							context,
							context.getResources().getString(
									R.string.no_network_2), 0).show();
					return;
				}
				if (currentFragment instanceof DownloadFragment) {
					((DownloadFragment) currentFragment).notifyDataSetChanged();
				}
			}
		}

	}

	/**
	 * 下载完成广播end
	 */

	/**
	 * 显示可编辑到actionBar
	 */
	public void showEditBarItem() {
		showActionBarItem(0, View.VISIBLE);
	}

	public void showAllOperationBar(Cursor cursor) {
		if (cursor.getCount() == 0) {
			showActionBarItem(0, View.GONE);
		} else {
			showActionBarItem(0, View.VISIBLE);
		}
	}

	/**
	 * 隐藏actionBar编辑按钮
	 */
	public void showEmptyBarItem() {
		if (auroraActionBar == null) {
			AuroraLog.elog(TAG, "auroraActionBar ==null ");
			return;
		}
		showActionBarItem(0, View.GONE);
		// 取消动画影响edit bar item
		Animation menuAnimation = AnimationUtils.loadAnimation(this,
				R.anim.aurora_action_dismiss_back_button);
		auroraActionBar.getItem(0).getItemView().startAnimation(menuAnimation);
	}

	/**
	 * 显示ActionBar item
	 * 
	 * @param position
	 * @param show
	 */
	public void showActionBarItem(int position, int show) {
		if (auroraActionBar == null) {
			AuroraLog.elog(TAG, "auroraActionBar ==null");
			return;
		}
		auroraActionBar.getItem(position).getItemView()
				.setEnabled(show == View.GONE ? false : true);
		auroraActionBar.getItem(position).getItemView().setVisibility(show);
	}

	/**
	 * 设置auroraActionBar title
	 * 
	 * @param rid
	 */
	public void setAuroraActionBarTitle(int rid) {
		if (auroraActionBar != null) {
			auroraActionBar.setTitle(getResourceString(rid));
			auroraActionBar.getHomeButton().setVisibility(View.VISIBLE);
		}
	}

	private String getResourceString(int resId) {
		return getResources().getString(resId);
	}

	/**
	 * 设置auroraActionBar 是否可用返回
	 * 
	 * @param enable
	 *            true 可以返回 false 不能
	 */
	public void setAuroraActionBarBtn(boolean enable) {
		if (auroraActionBar != null) {
			auroraActionBar.setDisplayHomeAsUpEnabled(enable);
		}
	}

	public void auroraSetRubbishBack() {
		if (currentFragment instanceof DownloadFragment) {
			((DownloadFragment) currentFragment).auroraSetRubbishBack();
		}
	}

	public int restartDownloadedFiles(long... ids) {
		if (ids == null || ids.length == 0) {
			// called with nothing to remove!
			throw new IllegalArgumentException(
					"input param 'ids' can't be null");
		}
		Cursor cursor = null;
		ArrayList<Integer> list = new ArrayList<Integer>();
		try {
			ContentResolver resolver = getContentResolver();
			cursor = resolver.query(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI,
					new String[] { Downloads.Impl._ID,
							Downloads.Impl.COLUMN_STATUS,
							Downloads.Impl.COLUMN_CONTROL },
					getWhereClauseForIds(ids), getWhereArgsForIds(ids), null);
			int count = cursor.getColumnCount();
			int idColumn = cursor.getColumnIndex(Downloads.Impl._ID);
			int statusColumn = cursor
					.getColumnIndex(Downloads.Impl.COLUMN_STATUS);
			int controlColumn = cursor
					.getColumnIndex(Downloads.Impl.COLUMN_CONTROL);
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {

				int id = cursor.getInt(idColumn);
				int status = cursor.getInt(statusColumn);
				int control = cursor.getInt(controlColumn);
				if (status != Downloads.Impl.STATUS_SUCCESS) {
					list.add(id);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
				cursor = null;
			}
		}

		if (list.size() == 0) {
			return 0;
		} else {
			ids = getIdsFromList(list);
		}

		ContentValues values = new ContentValues();
		values.put(Downloads.Impl.COLUMN_CONTROL, Downloads.Impl.CONTROL_RUN);
		values.put(Downloads.Impl.COLUMN_STATUS, Downloads.Impl.STATUS_PENDING);

		// if only one id is passed in, then include it in the uri itself.
		// this will eliminate a full database scan in the download service.
		if (ids.length == 1) {
			int updateCount = this.getContentResolver().update(
					ContentUris.withAppendedId(
							Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, ids[0]),
					values, null, null);
			return updateCount;
		}
		return this.getContentResolver().update(
				Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, values,
				getWhereClauseForIds(ids), getWhereArgsForIds(ids));

	}

	public int pauseDownloadedFiles(long... ids) {

		if (ids == null || ids.length == 0) {
			// called with nothing to remove!
			throw new IllegalArgumentException(
					"input param 'ids' can't be null");
		}

		ContentResolver resolver = getContentResolver();
		Cursor cursor = null;
		ArrayList<Integer> list = new ArrayList<Integer>();
		try {
			cursor = resolver.query(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI,
					new String[] { Downloads.Impl._ID,
							Downloads.Impl.COLUMN_STATUS,
							Downloads.Impl.COLUMN_CONTROL },
					getWhereClauseForIds(ids), getWhereArgsForIds(ids), null);
			int count = cursor.getColumnCount();
			int idColumn = cursor.getColumnIndex(Downloads.Impl._ID);
			int statusColumn = cursor
					.getColumnIndex(Downloads.Impl.COLUMN_STATUS);
			int controlColumn = cursor
					.getColumnIndex(Downloads.Impl.COLUMN_CONTROL);

			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {

				int id = cursor.getInt(idColumn);
				int status = cursor.getInt(statusColumn);
				int control = cursor.getInt(controlColumn);
//				StringBuilder sb = new StringBuilder("Cursor["
//						+ cursor.getPosition() + "]: ");
//				sb.append("id = ").append(id);
//				sb.append(" status = ").append(status);
//				sb.append(" control = ").append(control);
				if (status != Downloads.Impl.STATUS_SUCCESS) {
					list.add(id);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
				cursor = null;
			}

		}
		if (list.size() == 0) {
			return 0;
		} else {
			ids = getIdsFromList(list);
		}
		/*StringBuilder sb = new StringBuilder("update ids: ");
		for (int i = 0; i < ids.length; i++) {
			sb.append(ids[i]).append(" ");
		}*/

		ContentValues values = new ContentValues();
		values.put(Downloads.Impl.COLUMN_CONTROL, Downloads.Impl.CONTROL_PAUSED);
		// if only one id is passed in, then include it in the uri itself.
		// this will eliminate a full database scan in the download service.
		if (ids.length == 1) {
			int updateCount = this.getContentResolver().update(
					ContentUris.withAppendedId(
							Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, ids[0]),
					values, null, null);
			AuroraLog.elog(TAG, "update download");
			return updateCount;
		}
		return this.getContentResolver().update(
				Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, values,
				getWhereClauseForIds(ids), getWhereArgsForIds(ids));
	}

	private long[] getIdsFromList(ArrayList<Integer> list) {
		long[] ids = new long[list.size()];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = list.get(i);
		}
		return ids;
	}

	/**
	 * Get a parameterized SQL WHERE clause to select a bunch of IDs.
	 */
	private String getWhereClauseForIds(long[] ids) {
		StringBuilder whereClause = new StringBuilder();
		whereClause.append("(");
		for (int i = 0; i < ids.length; i++) {
			if (i > 0) {
				whereClause.append("OR ");
			}
			whereClause.append(Downloads.Impl._ID);
			whereClause.append(" = ? ");
		}
		whereClause.append(")");
		return whereClause.toString();
	}

	/**
	 * Get the selection args for a clause returned by
	 * {@link #getWhereClauseForIds(long[])}.
	 */
	private String[] getWhereArgsForIds(long[] ids) {
		String[] whereArgs = new String[ids.length];
		for (int i = 0; i < ids.length; i++) {
			whereArgs[i] = Long.toString(ids[i]);
		}
		return whereArgs;
	}

	public void deleteDownload(long id) {
		if (currentFragment instanceof DownloadFragment) {
			((DownloadFragment) currentFragment).deleteDownload(id);
		}
	}

	public void showEmptyOrEditView() {
		if (currentFragment instanceof DownloadFragment) {
			((DownloadFragment) currentFragment).showEmptyOrEditView();
		}
	}
}
