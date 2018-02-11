package com.aurora.filemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.aurora.tools.LogUtil;
import com.aurora.tools.FileInfo;
import com.aurora.tools.Util;
import com.aurora.tools.Util.SDCardInfo;
import com.aurora.widget.AuroraFileStorageDetailInfo;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import android.os.storage.StorageVolume;

import com.aurora.dbutil.FileCategoryHelper;
import com.aurora.dbutil.FileCategoryHelper.FileCategory;
import com.aurora.filemanager.R;

/**
 * 显示存储详情
 * 
 * @author jiangxh
 * @CreateTime 2014年5月12日 下午5:48:22
 * @Description com.aurora.filemanager AuroraStorageDetailActivity.java
 */
public class AuroraStorageDetailActivity extends AuroraActivity {

	private static final String TAG = "AuroraStorageDetailActivity";

	private List<AuroraFileStorageDetailInfo> auroraFileStorageDetailInfos = new ArrayList<AuroraFileStorageDetailInfo>();
	public static FileCategoryHelper fileCategoryHelper;
	private View currentView;

	private StorageManager mStorageManager;
	private Handler handler = new Handler() {
	};
	private ConcurrentHashMap<FileCategory, List<FileInfo>> hashMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		hashMap = ((FileApplication)getApplicationContext()).getHashMap();

		if (FileExplorerTabActivity.mSDCard2Path == null) {
			setAuroraContentView(
					R.layout.aurora_file_manager_single_chart_page,
					AuroraActionBar.Type.Normal);
			currentView = (View) getContentView();

			if (FileExplorerTabActivity.mSDCardPath != null&&FileExplorerTabActivity.ROOT_PATH!=null) {

				handler.post(new Runnable() {

					@Override
					public void run() {
						SDCardInfo usbCardInfo = Util.getSDCardInfo(true,
								FileExplorerTabActivity.mSDCardPath);
						AuroraFileStorageDetailInfo auroraFileStorageDetailInfo = new AuroraFileStorageDetailInfo(
								currentView, usbCardInfo, "");
						auroraFileStorageDetailInfo
								.setmFileCategoryHelper(fileCategoryHelper);
						auroraFileStorageDetailInfo.draw(
								FileExplorerTabActivity.ROOT_PATH, hashMap);
						auroraFileStorageDetailInfos
								.add(auroraFileStorageDetailInfo);

					}
				});
			}

		} else {
			setAuroraContentView(R.layout.aurora_file_manager_chart_page,
					AuroraActionBar.Type.Normal);
			mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
			StorageVolume[] storageVolume = mStorageManager.getVolumeList();
			final View usb = getLayoutInflater().inflate(
					R.layout.aurora_file_manager_usb_chart_page, null);
			LinearLayout sd_parent = (LinearLayout) findViewById(R.id.sd_parent);
			sd_parent.addView(usb, 0);
			if (FileExplorerTabActivity.mSDCardPath != null&&FileExplorerTabActivity.ROOT_PATH!=null) {
				handler.post(new Runnable() {

					@Override
					public void run() {
						SDCardInfo usbCardInfo = Util.getSDCardInfo(true,
								FileExplorerTabActivity.mSDCardPath);
						if (usbCardInfo != null) {
							AuroraFileStorageDetailInfo auroraFileStorageDetailInfo = new AuroraFileStorageDetailInfo(
									usb, usbCardInfo, "usb");
							auroraFileStorageDetailInfo
									.setmFileCategoryHelper(fileCategoryHelper);
							auroraFileStorageDetailInfo.draw(
									FileExplorerTabActivity.mSDCardPath, hashMap);
//							auroraFileStorageDetailInfo.draw(
//									FileExplorerTabActivity.mSDCardPath,
//									hashMap);
							auroraFileStorageDetailInfos
									.add(auroraFileStorageDetailInfo);

						}
					}
				});
			}
			int s = 1;
			for (int i = 0; i < storageVolume.length; i++) {
				// for (int i = 0; i < 6; i++) {
				final String temp = storageVolume[i].getPath();
				// final String temp = FileExplorerTabActivity.ROOT_PATH;
				File file = new File(temp);
				if (Util.sdIsMounted(temp)&&temp!=null) {
					if (i != 0) {
						final View view = getLayoutInflater().inflate(
								R.layout.aurora_file_manager_sd_chart_page,
								null);
						sd_parent.addView(view, s);
						s++;
						handler.post(new Runnable() {

							@Override
							public void run() {
								SDCardInfo sdCardInfo = Util.getSDCardInfo(
										true, temp);
								if (sdCardInfo != null) {
									AuroraFileStorageDetailInfo auroraFileStorageDetailInfoSd = new AuroraFileStorageDetailInfo(
											view, sdCardInfo, "sd");
									auroraFileStorageDetailInfoSd
											.setmFileCategoryHelper(fileCategoryHelper);
									// auroraFileStorageDetailInfoSd.draw(FileExplorerTabActivity.ROOT_PATH);
									auroraFileStorageDetailInfoSd.draw(temp,
											null);
									auroraFileStorageDetailInfos
											.add(auroraFileStorageDetailInfoSd);
								}

							}
						});
					}
				}
			}

		}
		getAuroraActionBar().setTitle(R.string.aurora_file_manager_detail);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(hashMap!=null){
			hashMap.clear();
		}
		if (auroraFileStorageDetailInfos != null) {
			for (AuroraFileStorageDetailInfo info : auroraFileStorageDetailInfos) {
				info.clear();
				info = null;
			}
		}
	}

}
