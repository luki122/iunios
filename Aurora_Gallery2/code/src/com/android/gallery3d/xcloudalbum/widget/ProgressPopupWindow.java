package com.android.gallery3d.xcloudalbum.widget;

import java.lang.ref.WeakReference;

import com.android.gallery3d.ui.Log;
import com.android.gallery3d.xcloudalbum.CloudActivity;
import com.android.gallery3d.xcloudalbum.inter.IBaiduTaskListener;
import com.android.gallery3d.xcloudalbum.tools.BaiduAlbumUtils;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.tools.cache.image.ImageLoader;
import com.android.gallery3d.xcloudalbum.tools.cache.image.ImageLoader.ImageProcessingCallback;
import com.android.gallery3d.xcloudalbum.uploaddownload.UploadDownloadListActivity;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;
import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;
import com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.gallery3d.R;

public class ProgressPopupWindow extends PopupWindow {
	private Context context;
	private ProgressBar progressBar;
	private View mView;
	private TextView loadNum, loadStatus,taskTitle;
	private ImageView loadIcon;
	private ImageLoader imageLoader;
	private static final String TAG="ProgressPopupWindow";
	

	public ProgressPopupWindow(final Context context, int aniTabMenu) {
		super();
		this.context = context;
		mView = LayoutInflater.from(context).inflate(
				R.layout.aurora_cloud_popup_progress, null);
		imageLoader = ImageLoader.getInstance(context);

		progressBar = (ProgressBar) mView.findViewById(R.id.load_progressbar);
		loadIcon = (ImageView) mView.findViewById(R.id.load_icon);
		//wenyongzhe 2015.9.18需求隐藏
		loadIcon.setVisibility(View.INVISIBLE);
		
		loadNum = (TextView) mView.findViewById(R.id.load_num);
		loadStatus = (TextView) mView.findViewById(R.id.load_status);
		taskTitle= (TextView) mView.findViewById(R.id.aurora_task_title);
		defaultMenu(mView, aniTabMenu);
		mView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, UploadDownloadListActivity.class);
				intent.putExtra(UploadDownloadListActivity.JUMP_TO_DOWNLOAD_TAB_KEY, isDownLoadTab);
				context.startActivity(intent);
			}
		});

	}

	private boolean isDownLoadTab=false;
	public void setJumpDownLoadTab(boolean download){
		isDownLoadTab=download;
	}
	
	public void setLoadStatusText(String msg) {
		loadStatus.setText(msg);
	}

	public void setLoadNumText(String msg) {
		loadNum.setText(msg);
	}

	public void setLoadProgressBar(int progress) {
		progressBar.setProgress(progress);
	}
	
	public void setTaskTitle(int resId) {
		taskTitle.setText(resId);
	}
	
	public void setTaskTitle(String name){
		taskTitle.setText(name);
	}

	/**
	 * 
	 * @param fileInfo
	 */
	public void displayIconDownLoadImage(FileUpDownloadInfo fileInfo) {
		loadIcon.setTag(fileInfo.getTarget());
		LogUtil.d(TAG, "fileInfo.getTarget()::"+fileInfo.getTarget());
		imageLoader.displayImage(fileInfo.getTarget(), fileInfo.getTarget(),
				new ImageProcessingCallback() {

					@Override
					public void onImageProcessing(WeakReference<Bitmap> weak,
							String tag) {
						if (((String) loadIcon.getTag()).equals(tag)) {//
							Bitmap bitmap = weak.get();
							if (bitmap == null || bitmap.isRecycled()) {
								return;
							}
							loadIcon.setImageBitmap(bitmap);
						}
					}
				});

	}
	
	public void displayIconDownLoadImage(String path) {
		loadIcon.setTag(path);
		LogUtil.d(TAG, "path::"+path);
		imageLoader.displayImage(path, path,
				new ImageProcessingCallback() {

					@Override
					public void onImageProcessing(WeakReference<Bitmap> weak,
							String tag) {
						if (((String) loadIcon.getTag()).equals(tag)) {//
							Bitmap bitmap = weak.get();
							if (bitmap == null || bitmap.isRecycled()) {
								return;
							}
							loadIcon.setImageBitmap(bitmap);
						}
					}
				});

	}
	
	private static final int MSG_SET_IMAGE = 1001;
	private Handler mSetImageHander = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if(msg.what == MSG_SET_IMAGE) {
				Bitmap bmp = (Bitmap)msg.obj;
				loadIcon.setImageBitmap(bmp);
			}
		}
		
	};
	
	public void displayIconImage(FileUpDownloadInfo info) {
		String md5 = info.getSource();
		loadIcon.setTag(md5);
		imageLoader.displayThumbnail(context, info.getSource(), md5, new ImageProcessingCallback() {

			@Override
			public void onImageProcessing(WeakReference<Bitmap> weak, String tag) {
				if (((String) loadIcon.getTag()).equals(tag)) {//
					Bitmap bitmap = weak.get();
					if (bitmap == null || bitmap.isRecycled()) {
						return;
					}
					Message msg = mSetImageHander.obtainMessage(MSG_SET_IMAGE, weak.get());
					mSetImageHander.sendMessage(msg);
					//loadIcon.setImageBitmap(weak.get());
				}
			}
		});

	}

	protected void defaultMenu(View view, int aniTabMenu) {
		// 设置默认项
		this.setContentView(view);
		this.setWidth(LayoutParams.WRAP_CONTENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setBackgroundDrawable(new BitmapDrawable());// 设置Menu菜单背景
		this.setAnimationStyle(aniTabMenu);
		this.setFocusable(false);
		this.setOutsideTouchable(false);
		this.setHeight(56*3);
	}

}
