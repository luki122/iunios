package com.android.gallery3d.local.widget;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraDialog;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraTextView;

import com.android.gallery3d.R;
import com.android.gallery3d.app.Gallery;
import com.android.gallery3d.app.Log;
import com.android.gallery3d.data.LocalImage;
import com.android.gallery3d.local.BasicActivity;
import com.android.gallery3d.local.GalleryItemActivity;
import com.android.gallery3d.local.GalleryLocalActivity;
import com.android.gallery3d.local.tools.MediaFileOperationUtil;
import com.android.gallery3d.viewpager.ViewpagerActivity;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.tools.ToastUtils;
import com.android.gallery3d.xcloudalbum.tools.Utils;

public class PhotoSelectPopupWindow {
	private CheckBox checkBox;
	private AuroraListView listView;
	private AuroraTextView titleView;
	private Button popCancel;
	private View mView;
	private List<MediaFileInfo> fileInfos;
	private List<MediaFileInfo> selectFileInfos = new ArrayList<MediaFileInfo>();
	private AlbumsSelectPopupAdapter adapter;
	private static final String TAG = "AlbumsSelectPopupWindow";
	private MediaFileOperationUtil operationUtil;
	private AuroraActivity activity;
	private AuroraAlertDialog dialog;

	public PhotoSelectPopupWindow(AuroraActivity activity) {
		this.activity = activity;
		AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(activity);
		mView = LayoutInflater.from(activity).inflate(R.layout.aurora_cloud_popup, null);
		dialog = builder.setView(mView).create();
		initView();
		operationUtil = MediaFileOperationUtil.getMediaFileOperationUtil(activity);

	}

	public void showList(List<MediaFileInfo> fileInfos) {
		this.fileInfos = fileInfos;
		if (this.fileInfos == null) {
			this.fileInfos = new ArrayList<MediaFileInfo>();
		}
		LayoutParams layoutParams = listView.getLayoutParams();
		if (fileInfos.size() > 5) {
			layoutParams.height = (int) activity.getResources().getDimension(R.dimen.aurora_album_listview_max_height);
			listView.setLayoutParams(layoutParams);
		}
		adapter = new AlbumsSelectPopupAdapter(activity, fileInfos);
		listView.setAdapter(adapter);
	}

	public void setSelectFileInfos(List<MediaFileInfo> selectFileInfos) {
		this.selectFileInfos.clear();
		this.selectFileInfos.addAll(selectFileInfos);
	}

	public void setTitleText(String title) {
		if (titleView != null) {
			titleView.setText(title);
		}
	}

	public void setCheckBoxAndPopCancel() {
		mView.findViewById(R.id.pop_temp_linear).setVisibility(View.GONE);
		if (popCancel != null) {
			popCancel.setVisibility(View.GONE);
		}
	}

	private String newFilePath;

	private void initView() {
		if (mView != null) {
			checkBox = (CheckBox) mView.findViewById(R.id.pop_checkbox);
			listView = (AuroraListView) mView.findViewById(R.id.pop_listView);
			titleView = (AuroraTextView) mView.findViewById(R.id.pop_title);
			popCancel = (Button) mView.findViewById(R.id.pop_cancel);
			popCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dismiss();

				}
			});
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					LogUtil.d(TAG, "onItemClick::" + position + " activity:" + activity);
					dismiss();
					/*List<MediaFileInfo> temp = new ArrayList<MediaFileInfo>(selectFileInfos);
					for (MediaFileInfo fileInfos : temp) {
						if(!fileInfos.isImage){
							selectFileInfos.remove(fileInfos);
						}
					}
					if(selectFileInfos.size()!=temp.size()){
						LogUtil.d(TAG, "--video can not move");
						ToastUtils.showTast(activity, R.string.aurora_video_cannt_move);
						if(position==0){
							return;
						}
					}*/
					if (position == 0) {
						if (activity instanceof GalleryItemActivity || activity instanceof ViewpagerActivity || activity instanceof Gallery) {//wenyongzhe
							handler.sendEmptyMessageDelayed(100000, 100);
						}
						return;
					} else {
						MediaFileInfo fileInfo = fileInfos.get(position);
						if (fileInfo != null) {
							String path = fileInfo.IsDir ? fileInfo.filePath : Utils.getPathFromFilepath(fileInfo.filePath);
							operationUtil.moveOperation(selectFileInfos, path);
						}
					}

				}
			});
		}
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 100000) {
				operationUtil.setSelectFileInfo(selectFileInfos);
				operationUtil.setDialogParams(null, true);
				operationUtil.createNewAlbum((AuroraActivity) activity);
			}
		}

	};

	public void show() {
		if (dialog != null && !dialog.isShowing()) {
			dialog.show();
		}
	}

	public void dismiss() {
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
		}
	}

	public void setOnDismissListener(OnDismissListener dismissListener) {
		if (dialog != null) {
			dialog.setOnDismissListener(dismissListener);
		}
	}

	/*************** 蒙板开始 ****************/
	private View mCoverView = null;
	private FrameLayout windowLayout;
	private Animation coverAnimation;

	private void loadAnimation(int animId) {
		try {
			coverAnimation = AnimationUtils.loadAnimation(activity, animId);
			mCoverView.startAnimation(coverAnimation);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void addCoverView() {
		mCoverView = new TextView(activity);
		mCoverView.setBackgroundColor(Color.parseColor("#666666"));
		mCoverView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
		windowLayout = (FrameLayout) ((AuroraActivity) activity).getWindow().getDecorView();
		windowLayout.addView(mCoverView);
		loadAnimation(com.aurora.R.anim.aurora_menu_cover_enter);
	}

	public void removeCoverView() {
		if (mCoverView != null && windowLayout != null) {
			windowLayout.removeView(mCoverView);
		}
		loadAnimation(com.aurora.R.anim.aurora_menu_cover_exit);
	}

	/*************** 蒙板end ****************/

}
