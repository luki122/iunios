package com.aurora.thememanager.adapter;

import java.io.File;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraListView;

import com.aurora.internet.RequestQueue;
import com.aurora.internet.cache.BitmapImageCache;
import com.aurora.internet.cache.DiskCache;
import com.aurora.internet.request.ImageRequest;
import com.aurora.internet.toolbox.ImageLoader;
import com.aurora.internetimage.NetworkImageView;
import com.aurora.thememanager.R;
import com.aurora.thememanager.activity.Action;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.preference.PreferenceManager;
import com.aurora.thememanager.utils.SystemUtils;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.download.DownloadData;
import com.aurora.thememanager.utils.download.DownloadManager;
import com.aurora.thememanager.utils.download.DownloadService;
import com.aurora.thememanager.utils.download.DownloadStatusCallback;
import com.aurora.thememanager.utils.download.FileDownloader;
import com.aurora.thememanager.utils.themehelper.ThemeManager;
import com.aurora.thememanager.utils.themehelper.ThemeOperationCallBack;
import com.aurora.thememanager.utils.themeloader.ImageLoaderImpl;
import com.aurora.thememanager.widget.DownloadButton;
import com.aurora.thememanager.widget.NetworkRoundedImageView;
import com.aurora.thememanager.widget.ProgressBtn;
import com.aurora.thememanager.widget.ProgressBtn.OnAnimListener;
import com.nostra13.universalimageloader.core.assist.FailReason;

public class ThemeListAdapter extends AbsThemeAdapter{

	private static final int MSG_UPDATE_APPLIED_THEME_ITEM = 0;
	private static final int MSG_UPDATE_APPLIED_THEME_FAULUER= 1;
	
	private Context mContext;

	private DownloadManager mDownloadManager;
	
	private ThemeManager mThemeManager;
	
	private int mUsedFailuredId;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.what == MSG_UPDATE_APPLIED_THEME_ITEM){
				int themeId = (int) msg.obj;
				mThemeManager.restartApplications(mContext, themeId);
			}else if(msg.what == MSG_UPDATE_APPLIED_THEME_FAULUER){
				Toast.makeText(mContext, mContext.getResources().getString(R.string.apply_theme_failure), Toast.LENGTH_LONG).show();
				Integer id  = (Integer) msg.obj;
				mUsedFailuredId = id;
				notifyDataSetChanged();
			}
			
		};
	};
	
	public ThemeListAdapter(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
		mDownloadManager = new DownloadManager(mContext);
		mThemeManager = ThemeManager.getInstance(ThemeConfig.THEME_ALL);
		
	}
	
	
	
	@Override
	public View getView(final int position, View coverview, ViewGroup parent) {
		// TODO Auto-generated method stub
		final Holder holder;
		if(coverview == null){
			coverview = LayoutInflater.from(mContext).inflate(R.layout.theme_item, null);
			holder = new Holder();
			holder.preview = (NetworkImageView)coverview.findViewById(R.id.theme_item_preview);
			holder.downloadIcon = (ProgressBtn)coverview.findViewById(R.id.theme_item_download);
			holder.title = (TextView)coverview.findViewById(R.id.theme_item_title);
			holder.author = (TextView)coverview.findViewById(R.id.theme_item_author_name);
			holder.authorIcon = (NetworkRoundedImageView)coverview.findViewById(R.id.theme_item_author_icon);
			
			coverview.setTag(holder);
		}else{
			holder = (Holder) coverview.getTag();
		}
		final Theme theme = getTheme(position);
		theme.downloadId = theme.themeId;
		theme.fileName = theme.name;
		if(theme != null){
			holder.theme = theme;
			holder.title.setText(theme.name/*+"   "+theme.themeId*/);
			holder.author.setText(theme.author);
			holder.preview.setDefaultImageResId(R.drawable.item_default_bg);
			holder.preview.setImageUrl(theme.preview, getImageLoader());
			holder.authorIcon.setDefaultImageResId(R.drawable.item_default_bg);
			holder.authorIcon.setImageUrl(theme.authorIcon, getImageLoader());
			holder.theme.type = Theme.TYPE_THEME_PKG;
			holder.setOnClickListener(holder.preview);
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
										mThemeManager.setThemePackageAplied((Theme)data, mContext);
										Message msg = new Message();
										msg.what = MSG_UPDATE_APPLIED_THEME_ITEM;
										msg.obj = data.downloadId;
										mHandler.sendMessageDelayed(msg, 500);
										
									}else{
										Message msg = new Message();
										msg.what = MSG_UPDATE_APPLIED_THEME_FAULUER;
										msg.obj = data.downloadId;
										mHandler.sendMessage(msg);
									}
								}
								
								@Override
								public Context getContext() {
									// TODO Auto-generated method stub
									return mContext;
								}
							});
							mThemeManager.apply(theme);
							holder.downloadIcon.setStatus(ProgressBtn.STATUS_PROGRESSING_INSTALLING);
						}
					};
					
					theme.fileDir = data.fileDir;
					theme.fileName = data.fileName;
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
								DownloadService.pauseOrContinueDownload(
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
							DownloadService.pauseOrContinueDownload(
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
							DownloadService.startDownload(holder.downloadIcon.getContext(),data);
						}
					};
					
					// ProgressBtn
					
					holder.downloadIcon.setStatus(ProgressBtn.STATUS_NORMAL);
					holder.downloadIcon.setOnNormalClickListener(clickListener);
					final int downloadId = data.downloadId;
					
					holder.downloadIcon.setOnBeginAnimListener(new OnAnimListener() {
						@Override
						public void onEnd(ProgressBtn view) {
							FileDownloader downloader = DownloadService.getDownloaders(mContext)
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
					// TODO Auto-generated method stub
					
					
				}
				
				@Override
				public void showAppling(DownloadData data) {
					// TODO Auto-generated method stub
					
				}
			});
			int usedId = mThemeManager.getAppliedThemeId(mContext);
			if(theme.themeId == usedId){
				holder.downloadIcon.setStatus(ProgressBtn.STATUS_INSTALLED);
			}
			
			if(theme.themeId == mUsedFailuredId){
				holder.downloadIcon.setStatus(ProgressBtn.STATUS_WAIT_INSTALL);
			}
			
			mDownloadManager.updateProgress(theme, null);
		}
		return coverview;
	}

	@Override
	public void updateData() {
		// TODO Auto-generated method stub
		notifyDataSetChanged();
	}
	
	private final class Holder implements OnClickListener{
		NetworkImageView preview;
		ProgressBtn downloadIcon;
		TextView title;
		TextView author;
		Theme theme;
		NetworkRoundedImageView authorIcon;
		void setOnClickListener(View itemView){
			itemView.setOnClickListener(this);
			
		}
		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			int id = view.getId();
			switch (id) {
			case R.id.theme_item_preview:
				Intent intent = new Intent();
				intent.setAction(Action.ACTION_APPLY_THEME);
				intent.putExtra(ThemeConfig.KEY_FOR_APPLY_THEME, theme);
				mContext.startActivity(intent);
				break;

			default:
				break;
			}
		}
		
		
	}
	
	

	public void reqeustImage(){
		
		
	}
	
	
	public void stopRequest(){
		if(mQueue != null){
			mQueue.stop();
		}
	}



	public void updateView(ListView listView) {
		// TODO Auto-generated method stub
		notifyDataSetChanged();
	}



	
	
	
	
	

}
