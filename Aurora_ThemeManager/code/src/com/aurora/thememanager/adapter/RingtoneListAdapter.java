package com.aurora.thememanager.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraListView;

import com.aurora.change.model.PictureGroupInfo;
import com.aurora.internet.RequestQueue;
import com.aurora.internet.cache.BitmapImageCache;
import com.aurora.internet.cache.DiskCache;
import com.aurora.internet.request.ImageRequest;
import com.aurora.internet.toolbox.ImageLoader;
import com.aurora.internetimage.NetworkImageView;
import com.aurora.thememanager.R;
import com.aurora.thememanager.activity.Action;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.entities.ThemeAudio;
import com.aurora.thememanager.utils.SystemUtils;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.download.DownloadData;
import com.aurora.thememanager.utils.download.DownloadManager;
import com.aurora.thememanager.utils.download.DownloadService;
import com.aurora.thememanager.utils.download.DownloadStatusCallback;
import com.aurora.thememanager.utils.download.FileDownloader;
import com.aurora.thememanager.utils.download.RingtongDownloadService;
import com.aurora.thememanager.utils.themehelper.ThemeManager;
import com.aurora.thememanager.utils.themehelper.ThemeOperationCallBack;
import com.aurora.thememanager.utils.themeloader.ImageLoaderImpl;
import com.aurora.thememanager.view.ListViewLoadView;
import com.aurora.thememanager.view.ListViewDelegate.OnListScrollChange;
import com.aurora.thememanager.view.ListViewLoadView.OnLoadViewClick;
import com.aurora.thememanager.widget.DownloadButton;
import com.aurora.thememanager.widget.NetworkRoundedImageView;
import com.aurora.thememanager.widget.ProgressBtn;
import com.aurora.thememanager.widget.ProgressBtn.OnAnimListener;
import com.nostra13.universalimageloader.core.assist.FailReason;

public class RingtoneListAdapter extends AbsThemeAdapter {
	private Context mContext;
	private static final int RINGTONE_TYPE_COUNT = 2;
	private static final int ADD_FOOTVIEW_COUNT = 1;
	
    private static final int RINGTONG_TYPE = 1;
    private static final int RINGTONG_LIST = 2;
    private static final int RINGTONG_LOAD = 3;
    
    private int mLoadViewStatus = ListViewLoadView.STATUS_LOADMORE;
    
	private DownloadManager mDownloadManager;
	private ThemeManager mThemeManager;
	
	private List<Theme> mPhoneData = new ArrayList<Theme>();
	private List<Theme> mMessageData = new ArrayList<Theme>();
	
	private Holder holder;
	
	private static final int MSG_UPDATE_APPLIED_RINGTONE_ITEM = 0;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.what == MSG_UPDATE_APPLIED_RINGTONE_ITEM){
				ProgressBtn btn = (ProgressBtn) msg.obj;
				btn.setStatus(ProgressBtn.STATUS_WAIT_INSTALL);
				notifyDataSetChanged();
				Toast.makeText(mContext, R.string.apply_success, Toast.LENGTH_SHORT).show();
			}
			
		};
	};
    
    private OnLoadViewClick mOnLoadViewClick = new OnLoadViewClick() {
		
		@Override
		public void onListViewLoadViewShowLoadMoreView() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onListViewLoadViewHideLoadMoreView() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onListViewLoadVieWLoadMore() {
			// TODO Auto-generated method stub
			if(mCallback != null) {
				mCallback.onRingToneListLoadMore();
			}
		}
	};
    
    private OnRingToneListLoadViewClick mCallback;

	public RingtoneListAdapter(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
		mDownloadManager = new DownloadManager(mContext);
		mThemeManager = ThemeManager.getInstance(ThemeConfig.THEME_RINGTONG);
		mPhoneData.clear();
		mMessageData.clear();
	}

	@Override
	public boolean isEnabled(int position) {
		// TODO Auto-generated method stub
		if (getItemType(position) == RINGTONG_TYPE) {
			return false;
		} else {
			return true;
		}
	}
	
    private int getItemType(int position) {
        if (position == 0 || position == mPhoneData.size() + 2) {
            return RINGTONG_TYPE;
        } else if ( position == mPhoneData.size() + 1 ){
            return RINGTONG_LOAD;
        } else {
        	return RINGTONG_LIST;
        }
    }
    
    public void setLoadViewStatus(int loadViewStatus) {
    	mLoadViewStatus = loadViewStatus;
    }
    
    private void updateMediaProvider(Theme theme) {
    	String themePath = theme.fileDir+"/"+theme.fileName;
		Cursor cr= null;
		Uri newUri;
		String soundRecordPathWhere = MediaStore.Audio.Media.DATA + "=?";
		cr = mContext.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Audio.Media._ID }, soundRecordPathWhere,
				new String[]{themePath}, null); 

		if(cr != null && cr.moveToFirst() && cr.getCount() > 0) {
			newUri =ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
					cr.getInt(cr.getColumnIndex(MediaStore.Audio.Media._ID)));
		} else {
			File sdfile = new File(themePath);
			ContentValues values = new ContentValues();
			values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
			values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
			values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
			values.put(MediaStore.Audio.Media.IS_RINGTONE, ((ThemeAudio)theme).ringtongType == 0 ? true : false);
			values.put(MediaStore.Audio.Media.IS_NOTIFICATION, ((ThemeAudio)theme).ringtongType == 0 ? false : true);
			values.put(MediaStore.Audio.Media.IS_ALARM, false);
			values.put(MediaStore.Audio.Media.IS_MUSIC, false);

			Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile.getAbsolutePath());
			//Uri uri = MediaStore.Audio.Media.getContentUri("external");
			newUri = mContext.getContentResolver().insert(uri, values);
		}

		if(cr != null) {
			cr.close();
		}
    }

	@Override
	public View getView(int position, View coverview, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		int itemType = getItemType(position);
//		Log.e("101010", "---position = ------" + position);
//		Log.e("101010", "---coverview = ------" + coverview);
//		Log.e("101010", "---mDatas.size() = ------" + mDatas.size());
		
        if (coverview != null) {
    		holder = (Holder) coverview.getTag();
		}

		if (itemType == RINGTONG_TYPE) {
			if (coverview == null || (holder != null && holder.mViewType != RINGTONG_TYPE)) {
				coverview = LayoutInflater.from(mContext).inflate(R.layout.ringtone_list_item_text, null);
				holder = new Holder();
				holder.mRingToneTypeText = (TextView)coverview;
				holder.mViewType = RINGTONG_TYPE;
				coverview.setTag(holder);
			}
			if (position == 0) {
				holder.mRingToneTypeText.setText(R.string.phone_ringtone);
			} else {
				holder.mRingToneTypeText.setText(R.string.message_ringtone);
			}
		} else if(itemType == RINGTONG_LIST) {
			if (coverview == null || (holder != null && holder.mViewType != RINGTONG_LIST)) {
				coverview = LayoutInflater.from(mContext).inflate(R.layout.ringtone_list_item, null);
				holder = new Holder();
				holder.divider = coverview.findViewById(R.id.divider);
				holder.downloadIcon = (ProgressBtn)coverview.findViewById(R.id.ringtone_download);
				holder.mRingtoneName = (TextView)coverview.findViewById(R.id.ringtone_name);
				holder.mRingtoneDuration = (TextView)coverview.findViewById(R.id.ringtone_duration);
				holder.loadingAndPlayAnimImage = (ImageView)coverview.findViewById(R.id.loadingandplayanim);
				holder.mViewType = RINGTONG_LIST;
				coverview.setTag(holder);
			}
			final Theme theme;
			final Holder finalHolder = holder;
			if (position <= mPhoneData.size()) {
				theme = getTheme(position - 1, ThemeAudio.RINGTONE);
				holder.divider.setVisibility(View.VISIBLE);
			} else {
				theme = getTheme(position - mPhoneData.size() - 3, ThemeAudio.NOTIFICATION);
				if(position - mPhoneData.size() - 3 == mMessageData.size() - 1) {
					holder.divider.setVisibility(View.GONE);
				} else {
					holder.divider.setVisibility(View.VISIBLE);
				}
			}
			if(theme != null){
				theme.downloadId = theme.themeId;
				theme.fileName = theme.name;
				holder.theme = theme;
				holder.mRingtoneName.setText(theme.name);
				holder.mRingtoneDuration.setText(theme.author);
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
										Log.e("101010", "--mDownloadManager.setApplied(data);------");
										Message msg = new Message();
										msg.what = MSG_UPDATE_APPLIED_RINGTONE_ITEM;
										msg.obj = finalHolder.downloadIcon;
										mHandler.sendMessage(msg);
									}
									
									@Override
									public Context getContext() {
										// TODO Auto-generated method stub
										return mContext;
									}
								});
								Log.e("101010", "----ringtonetype = ------" + ((ThemeAudio)theme).ringtongType);
								if(((ThemeAudio)theme).ringtongType > 0) {
									if(mOnItemApplyListener != null) {
										mOnItemApplyListener.setItemApply(theme, mThemeManager);
									}
								} else {
									mThemeManager.apply(theme);
								}
								//时间较短，暂时去掉该状态
								//finalHolder.downloadIcon.setStatus(ProgressBtn.STATUS_PROGRESSING_INSTALLING);
								
							}
						};
						theme.fileDir = data.fileDir;
						theme.fileName = data.fileName;
						finalHolder.downloadIcon.setStatus(ProgressBtn.STATUS_WAIT_INSTALL);
						finalHolder.downloadIcon.setOnFoucsClickListener(listener);
						
						updateMediaProvider(theme);
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
									Toast.makeText(finalHolder.downloadIcon.getContext(), finalHolder.downloadIcon.getContext()
											.getString(R.string.no_network_download_toast), Toast.LENGTH_SHORT).show();
								} else {
									RingtongDownloadService.pauseOrContinueDownload(
											finalHolder.downloadIcon.getContext(), downloader.getDownloadData());
								}
							}
						};
						// ProgressBtn
						finalHolder.downloadIcon.setStatus(ProgressBtn.STATUS_PROGRESSING_RETRY);
						finalHolder.downloadIcon.setProgress(progress);
						finalHolder.downloadIcon.setOnProgressClickListener(clickListener);
						finalHolder.downloadIcon.setProgressBackground(R.drawable.aurora_progress_refresh);
					}
					
					@Override
					public void showOperationDownloading(final FileDownloader downloader,int progress) {
						// TODO Auto-generated method stub
						int status = downloader.getStatus();
						OnClickListener clickListener = new OnClickListener() {
							@Override
							public void onClick(View v) {
								RingtongDownloadService.pauseOrContinueDownload(
										finalHolder.downloadIcon.getContext(), downloader.getDownloadData());
							}
						};
						// ProgressBtn
						if (status == FileDownloader.STATUS_WAIT) {
							if (!finalHolder.downloadIcon.isRuningStartAnim()) {
								finalHolder.downloadIcon.setStatus(ProgressBtn.STATUS_WAIT_DOWNLOAD);
							}
						} else {
							if (!finalHolder.downloadIcon.isRuningStartAnim()) {
								finalHolder.downloadIcon.setStatus(ProgressBtn.STATUS_PROGRESSING_DOWNLOAD);
							}
						
							int id =  finalHolder.downloadIcon.getTag() == null ? 0 : (Integer) finalHolder.downloadIcon.getTag();
							if (!finalHolder.downloadIcon.isRuningStartAnim()) {
								if (id == downloader.getDownloadData().downloadId) {
									finalHolder.downloadIcon.setProgressAnim(progress);
								} else {
									finalHolder.downloadIcon.setProgress(progress);
								}
							}
							finalHolder.downloadIcon.setOnProgressClickListener(clickListener);
							finalHolder.downloadIcon.setProgressBackground(R.drawable.aurora_progress_downloading);
						}
					}
					
					@Override
					public void showOperationDownload(final DownloadData data) {
						// TODO Auto-generated method stub
						OnClickListener clickListener = new OnClickListener() {
							@Override
							public void onClick(View v) {
								RingtongDownloadService.startDownload(finalHolder.downloadIcon.getContext(),data);
							}
						};
						
						// ProgressBtn
						
						finalHolder.downloadIcon.setStatus(ProgressBtn.STATUS_NORMAL);
						finalHolder.downloadIcon.setOnNormalClickListener(clickListener);
						final int downloadId = data.downloadId;
						
						finalHolder.downloadIcon.setOnBeginAnimListener(new OnAnimListener() {
							@Override
							public void onEnd(ProgressBtn view) {
								FileDownloader downloader = RingtongDownloadService.getDownloaders(mContext)
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
								mDownloadManager.doOperationContinue(downloader, finalHolder.downloadIcon.getContext());
							}
						};
						
						// ProgressBtn
						finalHolder.downloadIcon.setStatus(ProgressBtn.STATUS_PROGRESSING_DOWNLOAD);
						finalHolder.downloadIcon.setProgress(progress);
						finalHolder.downloadIcon.setOnProgressClickListener(clickListener);
						finalHolder.downloadIcon.setProgressBackground(R.drawable.aurora_progress_pause);
					}
					
					@Override
					public void showOperationApplied(DownloadData data) {
						// TODO Auto-generated method stub
						finalHolder.downloadIcon.setStatus(ProgressBtn.STATUS_INSTALLED);
						notifyDataSetChanged();
					}
					
					@Override
					public void showAppling(DownloadData data) {
						// TODO Auto-generated method stub
						
					}
				});
				mDownloadManager.updateProgress(theme, null);
			}
		} else if (itemType == RINGTONG_LOAD) {
			if (coverview == null || (holder != null && holder.mViewType != RINGTONG_LOAD)) {
				coverview = LayoutInflater.from(mContext).inflate(R.layout.list_load_more_view, null);
				holder = new Holder();
				holder.mLoadMoreView = (ListViewLoadView)coverview;
				holder.mLoadMoreView.setCallBack(mOnLoadViewClick);
				holder.mLoadMoreView.setStatus(mLoadViewStatus);
				holder.mProgress = coverview.findViewById(R.id.listview_foot_progress);
				holder.mLoadText = (TextView)coverview.findViewById(R.id.listview_foot_more);
				holder.mViewType = RINGTONG_LOAD;
				coverview.setTag(holder);
			}
		}
		return coverview;
	}

	@Override
	public void updateData() {
		// TODO Auto-generated method stub
		notifyDataSetChanged();
	}

	public Theme getTheme(int index, int type){
		if (type == ThemeAudio.RINGTONE) {
			return mPhoneData.get(index);
		} else {
			return mMessageData.get(index);
		}
	}
	
	public Theme getTheme(int position) {
		Theme theme;
		if (position <= mPhoneData.size()) {
			theme = getTheme(position - 1, ThemeAudio.RINGTONE);
		} else {
			theme = getTheme(position - mPhoneData.size() - 3, ThemeAudio.NOTIFICATION);
		}
		
		return theme;
	}
	@Override
	public Theme getItem(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mPhoneData.size() > 0 || mMessageData.size() > 0 ? mPhoneData.size() + mMessageData.size() + RINGTONE_TYPE_COUNT + ADD_FOOTVIEW_COUNT : 0;
	}
	
	synchronized public void addData(Theme data){
		
		if(data == null){
			return;
		}
		if (((ThemeAudio)data).ringtongType == ThemeAudio.RINGTONE) {
			mPhoneData.add(data);
		} else {
			mMessageData.add(data);
		}
		updateData();
		
	}

	private final class Holder {
		ProgressBtn downloadIcon;
		TextView mRingtoneName;
		TextView mRingtoneDuration;
		Theme theme;

		/**
		 * 分页加载时显示在ListView底部的footerView
		 */
		private ListViewLoadView mLoadMoreView;

		/**
		 * 加载进度条
		 */
		private View mProgress;

		/**
		 * 加载提示语
		 */
		private TextView mLoadText;

		private TextView mRingToneTypeText;
		
		int mViewType;
		
		View divider;
		
		ImageView loadingAndPlayAnimImage;
	}



	public void reqeustImage(){


	}


	public void stopRequest(){
		if(mQueue != null){
			mQueue.stop();
		}
	}

	public void updateView(AuroraListView listView) {
		// TODO Auto-generated method stub
		notifyDataSetChanged();
	}
	
	public interface OnRingToneListLoadViewClick{
		/**
		 * 显示加载更多View时需要处理的逻辑在这里处理
		 */
		public void onRingToneListShowLoadMoreView();
		/**
		 * 隐藏加载更多View时需要处理的逻辑在这里处理
		 */
		public void onRingToneListHideLoadMoreView();
		
		public void onRingToneListLoadMore();
	}
	
	public void setCallBack(OnRingToneListLoadViewClick callback){
		mCallback = callback;
	}
	
	
	public interface OnItemApplyListener{
		public void setItemApply(Theme theme, ThemeManager mThemeManager);
	}
	private OnItemApplyListener mOnItemApplyListener;
	public void setOnItemApplyListener(OnItemApplyListener onItemApplyListener) {
		mOnItemApplyListener = onItemApplyListener;
	}
}
