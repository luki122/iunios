package com.aurora.thememanager.adapter;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import com.aurora.change.activities.WallpaperLocalActivity;
import com.aurora.change.data.DataOperation;
import com.aurora.change.data.DbControl;
import com.aurora.change.model.PictureGroupInfo;
import com.aurora.change.utils.Consts;
import com.aurora.change.utils.FileHelper;
import com.aurora.change.utils.WallpaperUtil;
import com.aurora.internetimage.NetworkImageView;
import com.aurora.thememanager.activity.Action;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.preference.PreferenceManager;
import com.aurora.thememanager.utils.SystemUtils;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.ToastUtils;
import com.aurora.thememanager.utils.download.DownloadData;
import com.aurora.thememanager.utils.download.DownloadManager;
import com.aurora.thememanager.utils.download.DownloadService;
import com.aurora.thememanager.utils.download.DownloadStatusCallback;
import com.aurora.thememanager.utils.download.FileDownloader;
import com.aurora.thememanager.utils.download.TimeWallpaperDownloadService;
import com.aurora.thememanager.utils.themehelper.ThemeManager;
import com.aurora.thememanager.utils.themehelper.ThemeOperationCallBack;
import com.aurora.thememanager.widget.DownloadButton;
import com.aurora.thememanager.widget.NetworkRoundedImageView;
import com.aurora.thememanager.widget.ProgressBtn;
import com.aurora.thememanager.widget.ProgressBtn.OnAnimListener;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.aurora.thememanager.R;
public class TimeWallpaperAdapter extends AbsThemeAdapter {
	private static final int MSG_UPDATE_APPLIED_THEME_ITEM = 0;
	private static final int MSG_UPDATE_APPLIED_THEME_ITEM2 = 1;
	private static final int TYPE_SINGLE = 0;
	private static final int TYPE_MULT = 1;
	private DownloadManager mDownloadManager;
	
	private ThemeManager mThemeManager;
	
	private PreferenceManager mPrefManager;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.what == MSG_UPDATE_APPLIED_THEME_ITEM){
				ProgressBtn btn = (ProgressBtn) msg.obj;
				btn.setStatus(ProgressBtn.STATUS_INSTALLED);
				notifyDataSetChanged();
			}else if(msg.what == MSG_UPDATE_APPLIED_THEME_ITEM2){
				ProgressBtn btn = (ProgressBtn) msg.obj;
				setProgressBtnInstalled(btn);
			}
			
		};
	};
	
	private synchronized void setProgressBtnInstalled(ProgressBtn btn){
		btn.setStatus(ProgressBtn.STATUS_INSTALLED);
	}
	
	public TimeWallpaperAdapter(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mDownloadManager = new DownloadManager(mContext);
		mPrefManager = PreferenceManager.getInstance(mContext);
		mThemeManager = ThemeManager.getInstance(ThemeConfig.THEME_TIMES);
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		Theme theme = mDatas.get(position);
		int previewCount = theme.previews.length;
		
		if(convertView == null){
				convertView = mInflater.inflate(R.layout.time_wallpaper_list_mult_item, null);
				holder = new ViewHolder();
				holder.previewParent = convertView.findViewById(R.id.time_wallpaper_list_preview);
				holder.preview = (NetworkImageView)convertView.findViewById(R.id.theme_item_preview);
				holder.preview1 = (NetworkImageView)convertView.findViewById(R.id.theme_item_preview1);
				holder.preview2 = (NetworkImageView)convertView.findViewById(R.id.theme_item_preview2);
				holder.preview3= (NetworkImageView)convertView.findViewById(R.id.theme_item_preview3);
				holder.downloadIcon = (ProgressBtn)convertView.findViewById(R.id.theme_item_download);
				holder.title = (TextView)convertView.findViewById(R.id.theme_item_title);
				holder.author = (TextView)convertView.findViewById(R.id.theme_item_author_name);
				holder.authorIcon = (NetworkRoundedImageView)convertView.findViewById(R.id.theme_item_author_icon);
	                convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		updateItemLayout(holder, theme);
		
		return convertView;
	}
	
	@Override
	public void addData(Theme data) {
		// TODO Auto-generated method stub
		if(data == null){
			return;
		}
		synchronized (mDatas) {
			if(!mDatas.contains(data)){
				mDatas.add(data);
				updateData();
			}
		}
	}

	/**
	 * 更新主题
	 * @param holder
	 * @param theme
	 */
	private void updateItemLayout(final ViewHolder holder,final Theme theme){
		
		if (theme != null) {
			holder.theme = theme;
			holder.title.setText(theme.name /*+ "   " + theme.themeId*/);
			holder.author.setText(theme.author);
			holder.preview.setDefaultImageResId(R.drawable.img_loading_before);
			if(theme.previews.length > 0){
				holder.preview.setImageUrl(theme.previews[0], getImageLoader());
			}

			holder.preview1.setDefaultImageResId(R.drawable.img_loading_before);
			if(theme.previews.length > 1){
			holder.preview1.setImageUrl(theme.previews[1], getImageLoader());
			}
			holder.preview2.setDefaultImageResId(R.drawable.img_loading_before);
			if(theme.previews.length > 2){
				holder.preview2.setImageUrl(theme.previews[2], getImageLoader());
			}

			holder.preview3.setDefaultImageResId(R.drawable.img_loading_before);
			if(theme.previews.length > 3){
			holder.preview3.setImageUrl(theme.previews[3], getImageLoader());
			}
			holder.authorIcon.setDefaultImageResId(R.drawable.img_loading_before);
			holder.authorIcon.setImageUrl(theme.authorIcon, getImageLoader());
			holder.previewParent.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(
							Action.ACTION_PREVIEW_TIME_WALLPAPER);
					intent.putExtra(Action.KEY_SHOW_TIME_WALL_PAPER_PREVIEW,
							theme);
					mContext.startActivity(intent);
				}
			});
			mDownloadManager.setCallBack(new DownloadStatusCallback() {
				
				@Override
				public void showWaitApply(final DownloadData data) {
					// TODO Auto-generated method stub
					
					OnClickListener listener = new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							mThemeManager.setCallBack(new ThemeOperationCallBack() {
								
								@Override
								public void onProgressUpdate(int progress) {
									// TODO Auto-generated method stub
									
								}
								
								@Override
								public void onCompleted(boolean success, int statusCode) {
									if(success){
										ThemeManager.setTimeWallpaperApplied(data, mContext);
										Message msg = new Message();
										msg.what = MSG_UPDATE_APPLIED_THEME_ITEM;
										msg.obj = holder.downloadIcon;
										mHandler.sendMessage(msg);
									}
								}
								
								@Override
								public Context getContext() {
									// TODO Auto-generated method stub
									return mContext;
								}
							});
							onClickApply(mContext, holder.theme.name+data.downloadId,data.downloadId);
							holder.downloadIcon.setStatus(ProgressBtn.STATUS_PROGRESSING_INSTALLING);
							Message msg = new Message();
							msg.what = MSG_UPDATE_APPLIED_THEME_ITEM;
							msg.obj = holder.downloadIcon;
							mHandler.sendMessageDelayed(msg, 500);
						}
					};
					holder.downloadIcon.setStatus(ProgressBtn.STATUS_WAIT_INSTALL);
					holder.downloadIcon.setOnFoucsClickListener(listener);
				}
				
				@Override
				public void showOperationUpdate(DownloadData data,
						OnClickListener onClickListener) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void showOperationRetry(final FileDownloader downloader,int progress) {
					// TODO Auto-generated method stub
					OnClickListener clickListener = new OnClickListener() {
						@Override
						public void onClick(View v) {
							if (!SystemUtils.hasNetwork()) {
								Toast.makeText(holder.downloadIcon.getContext(), holder.downloadIcon.getContext()
										.getString(R.string.no_network_download_toast), Toast.LENGTH_SHORT).show();
							} else {
								TimeWallpaperDownloadService.pauseOrContinueDownload(
										holder.downloadIcon.getContext(), downloader.getDownloadData());
							}
						}
					};
					// ProgressBtn
					holder.downloadIcon.setStatus(ProgressBtn.STATUS_PROGRESSING_RETRY);
					holder.downloadIcon.setProgress(progress);
					holder.downloadIcon.setOnProgressClickListener(clickListener);
					holder.downloadIcon.setProgressBackground(R.drawable.aurora_progress_refresh);
				}
				
				@Override
				public void showOperationDownloading(final FileDownloader downloader,int progress) {
					// TODO Auto-generated method stub
					int status = downloader.getStatus();
					OnClickListener clickListener = new OnClickListener() {
						@Override
						public void onClick(View v) {
							TimeWallpaperDownloadService.pauseOrContinueDownload(
									holder.downloadIcon.getContext(), downloader.getDownloadData());
						}
					};
					// ProgressBtn
					if (status == FileDownloader.STATUS_WAIT) {
						if (!holder.downloadIcon.isRuningStartAnim()) {
							holder.downloadIcon.setStatus(ProgressBtn.STATUS_WAIT_DOWNLOAD);
						}
					} else {
						if (!holder.downloadIcon.isRuningStartAnim()) {
							holder.downloadIcon.setStatus(ProgressBtn.STATUS_PROGRESSING_DOWNLOAD);
						}
					
						int id =  holder.downloadIcon.getTag() == null ? 0 : (Integer) holder.downloadIcon.getTag();
						if (!holder.downloadIcon.isRuningStartAnim()) {
							if (id == downloader.getDownloadData().downloadId) {
								holder.downloadIcon.setProgressAnim(progress);
							} else {
								holder.downloadIcon.setProgress(progress);
							}
						}
						holder.downloadIcon.setOnProgressClickListener(clickListener);
						holder.downloadIcon.setProgressBackground(R.drawable.aurora_progress_downloading);
					}
				}
				
				@Override
				public void showOperationDownload(final DownloadData data) {
					// TODO Auto-generated method stub
					OnClickListener clickListener = new OnClickListener() {
						@Override
						public void onClick(View v) {
							TimeWallpaperDownloadService.startDownload(holder.downloadIcon.getContext(),data);
						}
					};
					
					// ProgressBtn
					
					holder.downloadIcon.setStatus(ProgressBtn.STATUS_NORMAL);
					holder.downloadIcon.setOnNormalClickListener(clickListener);
					final int downloadId = data.downloadId;
					
					holder.downloadIcon.setOnBeginAnimListener(new OnAnimListener() {
						@Override
						public void onEnd(ProgressBtn view) {
							FileDownloader downloader = TimeWallpaperDownloadService.getDownloaders(mContext)
									.get(downloadId);
							if (downloader != null) {
								int status = downloader.getStatus();
								if (status == FileDownloader.STATUS_CONNECTING
										|| status == FileDownloader.STATUS_DOWNLOADING) {
									view.setStatus(ProgressBtn.STATUS_PROGRESSING_DOWNLOAD);
								}
							}
						}
					});
				}
				
				@Override
				public void showOperationContinue(final FileDownloader downloader,int progress) {
					// TODO Auto-generated method stub
					OnClickListener clickListener = new OnClickListener() {
						@Override
						public void onClick(View v) {
							mDownloadManager.doOperationContinue(downloader, holder.downloadIcon.getContext());
						}
					};
					
					// ProgressBtn
					holder.downloadIcon.setStatus(ProgressBtn.STATUS_PROGRESSING_DOWNLOAD);
					holder.downloadIcon.setProgress(progress);
					holder.downloadIcon.setOnProgressClickListener(clickListener);
					holder.downloadIcon.setProgressBackground(R.drawable.aurora_progress_pause);
				}
				
				@Override
				public void showOperationApplied(DownloadData data) {
//					mThemeManager.setTimeWallpaperApplied(data, mContext);
					holder.downloadIcon.setStatus(ProgressBtn.STATUS_INSTALLED);
				}
				
				@Override
				public void showAppling(DownloadData data) {
					// TODO Auto-generated method stub
					
				}
			});
			theme.downloadId = theme.themeId;
			mDownloadManager.updateProgress(theme);
			int usedId = mThemeManager.getAppliedTimeWallpaperd(mContext);
			if(theme.themeId == usedId){
				holder.downloadIcon.setStatus(ProgressBtn.STATUS_INSTALLED);
			}
			
		}
		
	}
	
	
	
	@Override
	public void updateData() {
		// TODO Auto-generated method stub
		notifyDataSetChanged();
	}
	
	
	
	class ViewHolder {
		View previewParent;
		NetworkImageView preview;
		NetworkImageView preview1;
		NetworkImageView preview2;
		NetworkImageView preview3;
		ProgressBtn downloadIcon;
		TextView title;
		TextView author;
		Theme theme;
		NetworkRoundedImageView authorIcon;
    }
	
	
	
	
	 private void onClickApply(Context context, String currentGroup,int downloadId) {
	        try {
	            boolean isCopyRight = false;
	            String currentPath = WallpaperUtil.getCurrentLockPaperPath(context, currentGroup);
	            //shigq add start
	            if (currentPath.contains(Consts.NEXTDAY_WALLPAPER_PATH + "NextDay/")) {
					File mFile = new File(Consts.NEXTDAY_WALLPAPER_PATH + "NextDay/");
					if (mFile.isDirectory()) {
						File[] files = mFile.listFiles();
						for (File myFile : files) {
							Arrays.sort(files, new Comparator<File>() {
								@Override
								public int compare(File file1, File file2) {
									// TODO Auto-generated method stub
									return file1.getName().compareToIgnoreCase(file2.getName());
								}
							});
							break;
						}
						if (files.length == 4) {
							String filePath = files[2].toString();
							
							String fileName = filePath.replace(Consts.NEXTDAY_WALLPAPER_PATH + "NextDay/", "");
							currentPath = Consts.NEXTDAY_WALLPAPER_SAVED + fileName.replace(".jpg", "_comment.jpg");
							if (!FileHelper.fileIsExist(currentPath)) {
								Log.d("Wallpaper_DEBUG", "WallpaperLocalAdapter--------onClickApply------- currentPath file is not exist!!!!"+currentPath);
								currentPath = files[2].toString();
							}
							Log.d("Wallpaper_DEBUG", "WallpaperLocalAdapter-------onClickApply-------fileName = "+fileName+" currentPath = "+currentPath);
						}
					}
				}
	            //shigq add end
	            
	            //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 start
//	          bisCopyRight = FileHelper.copyFile(currentPath, Consts.LOCKSCREEN_WALLPAPER_PATH);
	            isCopyRight = FileHelper.copyFile(currentPath, Consts.LOCKSCREEN_WALLPAPER_PATH, mContext);
	            //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 end
	            
	            /*if (isCopyRight) {
	                mHandler.sendEmptyMessage(LOCKPAPER_SET_SUCCESS);
	            }else {
	            	mHandler.sendEmptyMessage(LOCKPAPER_SET_FAILED);
				}*/
	            DbControl controller = new DbControl(mContext);
	            DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP, currentGroup);
	            PictureGroupInfo group = controller.queryGroupByName(currentGroup);
	            if(group != null){
	            	if(downloadId != ThemeConfig.TIME_WALLPAPER_UNUSED_ID){
	            		Theme theme = new Theme();
	            		theme.downloadId = downloadId;
	            		theme.themeId = downloadId;
	            		mThemeManager.setTimeWallpaperApplied(theme, context);
	            	}else{
	            		mThemeManager.setTimeWallpaperUnApplied(mContext);
	            	}
	            }
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	
	
	
	
	
	
	
	
	

}
