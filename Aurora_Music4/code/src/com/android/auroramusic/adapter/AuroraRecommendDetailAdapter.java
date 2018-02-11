package com.android.auroramusic.adapter;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import javax.net.ssl.HandshakeCompletedListener;

import android.R.integer;
import android.R.string;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.auroramusic.adapter.AuroraNetSearchAdapter.vh;
import com.android.auroramusic.downloadex.DownloadInfo;
import com.android.auroramusic.downloadex.DownloadManager;
import com.android.auroramusic.downloadex.DownloadStatusListener;
import com.android.auroramusic.online.AuroraNetTrackDetail;
import com.android.auroramusic.online.AuroraNetTrackDetailActivity;
import com.android.auroramusic.ui.AuroraMediaPlayHome;
import com.android.auroramusic.ui.AuroraPlayerActivity;
import com.android.auroramusic.ui.AuroraTrackBrowserActivity;
import com.android.auroramusic.util.AuroraListItem;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.FlowTips;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.LogUtil;
import com.android.auroramusic.util.ReportUtils;
import com.android.auroramusic.util.FlowTips.OndialogClickListener;
import com.android.auroramusic.util.ThreadPoolExecutorUtils;
import com.android.auroramusic.widget.AuroraRoundProgressBar;
import com.android.music.MusicUtils;
import com.android.music.R;
import com.xiami.sdk.entities.OnlineSong;

public class AuroraRecommendDetailAdapter extends BaseAdapter {
	public static final int WAIT = 0;
	public static final int RUNNING = 1;
	public static final int START = 3;
	public static final int FINISH = 4;
	public static final int EXCEPIONG = 5;
	public static final int EXIST = 200;
	public static final int MSG_NOTIFY = 10000;
	public static final int MSG_NO_PERMISSION = 10002;
	public static final int MSG_UPDOWNLOAD = 10001;
	// private int currentSelect = -1;
	private int mCurrentPosition = -1;
	private List mList;
	private Context mContext;
	private int mType = -1;
	private String mSavePath = Globals.mSavePath;
	private String bitrate = "";
	private static final String TAG = "AuroraRecommendDetailAdapter";
	private String prixBitrate = "128";
	private String IMAGE_CACHE_DIR = "NetSongImage";
	private DownloadManager mDownloadManager;
	private String albumName;
	private boolean isNoAvailable =true;
	private boolean isPartNoAvailable= false;
	
	public boolean isNoAvailable() {
		return isNoAvailable;
	}
	
	public boolean isPartNoAvailable() {
		return isPartNoAvailable;
	}

	DownloadStatusListener mDownloadStatusListener = new DownloadStatusListener() {

		@Override
		public void onDownload(String url, long id, final int status, final long downloadSize, final long fileSize) {
			mHandler.sendEmptyMessage(MSG_NOTIFY);
		};
	};

	class ViewHolder {
		ImageView image_album;
		AuroraRoundProgressBar image_download;
		View download_parent;
		TextView tv_title;
		TextView tv_artist;
		TextView tv_top;
		ImageView iv_song_selected;
		ImageView iv_download_ok;
	}

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == MSG_NOTIFY) {
				notifyDataSetChanged();
			} else if (msg.what == MSG_UPDOWNLOAD) {
				try {
					OnlineSong onlineSong = (OnlineSong) msg.obj;
					DownloadInfo downloadInfo = mDownloadManager.buildDownloadInfoByOnlineSong(onlineSong);
					mDownloadManager.getDownloadManagerDb().savaOrUpdate(downloadInfo);
					HashMap<Long, DownloadInfo> hashMap = mDownloadManager.getDownloadingMapData();
					if (hashMap.get(onlineSong.getSongId()) == null) {
						hashMap.put(onlineSong.getSongId(), downloadInfo);
					}
				} catch (Exception e) {
					LogUtil.e(TAG, "--handleMessage save downloadInfo error ", e);
				}
				removeMessages(MSG_NOTIFY);
				sendEmptyMessageDelayed(MSG_NOTIFY, 1000);
			}else if (msg.what==MSG_NO_PERMISSION) {
				Toast.makeText(mContext, R.string.aurora_download_permission, Toast.LENGTH_SHORT).show();
			}
		};
	};

	public AuroraRecommendDetailAdapter(Context context, List list) {
		initAdapter(context, list);
	}

	public AuroraRecommendDetailAdapter(Context context, List list, int type, String albumName) {
		initAdapter(context, list);
		mType = type;
		this.albumName = albumName;
	}

	public AuroraRecommendDetailAdapter(Context context, List list, int type) {
		initAdapter(context, list);
		mType = type;
	}

	public void initAdapter(Context context, List list) {
		mContext = context;
		mList = list;
		if (mList!=null) {
			for (int i = 0; i < list.size(); i++) {
				OnlineSong item = (OnlineSong) list.get(i);
				boolean isShow = AuroraMusicUtil.isNoPermission(item);
				isNoAvailable = isNoAvailable&isShow;
				isPartNoAvailable=isPartNoAvailable|isShow;
			}
		}
		mDownloadManager = DownloadManager.getInstance(context);
	}

	@Override
	public int getCount() {
		if (mList == null) {
			return 0;
		}
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		if (mList != null) {
			return mList.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder viewHolder;
		final OnlineSong item;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			if (mType == 0 || mType == 1 || mType == 3) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.aurora_netablum_listitem, null);
			} else if (mType == 1) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.aurora_nettrackdetail_listitem, null);
				viewHolder.image_album = (ImageView) convertView.findViewById(R.id.song_playicon);

			} else {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.aurora_toplist_layout, null);
				viewHolder.tv_top = (TextView) convertView.findViewById(R.id.song_playicon);
			}
			viewHolder.iv_download_ok = (ImageView) convertView.findViewById(R.id.aurora_download_ok);
			viewHolder.iv_song_selected = (ImageView) convertView.findViewById(R.id.iv_song_selected);
			viewHolder.download_parent = convertView.findViewById(R.id.aurora_btn_recommend_download_parent);
			viewHolder.image_download = (AuroraRoundProgressBar) convertView.findViewById(R.id.aurora_btn_recommend_download);
			viewHolder.tv_title = (TextView) convertView.findViewById(R.id.song_title);
			viewHolder.tv_artist = (TextView) convertView.findViewById(R.id.song_artist);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		item = (OnlineSong) mList.get(position);
		String trackTitle = item.getSongName();
		String artistName = item.getArtistName();
		if (artistName != null && artistName.length() > 12) {
			artistName = artistName.substring(0, 12) + "…";
		}
		String albumName = item.getAlbumName();
		if (mType == 0 && TextUtils.isEmpty(albumName)) {
			albumName = this.albumName;
		}
		// LogUtil.d(TAG, "----------------trackTitle:" + trackTitle +
		// " artistName:" + artistName + " albumName:" + albumName +
		// " getListenFile:" + item.getListenFile() + " getLyric:" +
		// item.getLyric());
		// mType 0 热门专辑
		if (mType == 0 || mType == 1 || mType == 3) {
			if (TextUtils.isEmpty(trackTitle))
				viewHolder.tv_title.setText(mContext.getResources().getString(R.string.unknown_track));
			else
				viewHolder.tv_title.setText(trackTitle);
			if (TextUtils.isEmpty(artistName)) {
				viewHolder.tv_artist.setText(mContext.getResources().getString(R.string.unknown_artist) + " · ");
			} else {
				viewHolder.tv_artist.setText(artistName + " · ");
			}
			if (TextUtils.isEmpty(albumName)) {
				viewHolder.tv_artist.append(mContext.getResources().getString(R.string.unknown_album_name));
			} else {
				viewHolder.tv_artist.append(albumName);
			}
		} else if (mType == 2) {
			if (position == 0) {
				viewHolder.tv_top.setTextColor(Color.parseColor("#e52c1f"));
			} else if (position == 1) {
				viewHolder.tv_top.setTextColor(Color.parseColor("#fe8831"));
			} else if (position == 2) {
				viewHolder.tv_top.setTextColor(Color.parseColor("#ffd02b"));
			} else {
				viewHolder.tv_top.setTextColor(Color.parseColor("#8d8d8d"));
			}
			viewHolder.tv_top.setText(String.valueOf(position + 1));
			if (TextUtils.isEmpty(trackTitle))
				viewHolder.tv_title.setText(mContext.getResources().getString(R.string.unknown_track));
			else
				viewHolder.tv_title.setText(trackTitle);
			if (TextUtils.isEmpty(artistName)) {
				viewHolder.tv_artist.setText(mContext.getResources().getString(R.string.unknown_artist) + " · ");
			} else {
				viewHolder.tv_artist.setText(artistName + " · ");
			}
			if (TextUtils.isEmpty(albumName)) {
				viewHolder.tv_artist.append(mContext.getResources().getString(R.string.unknown_album_name));
			} else {
				viewHolder.tv_artist.append(albumName);
			}
		}
		

		final DownloadInfo downloadInfo = mDownloadManager.getDownloadingMapData().get(item.getSongId());
		if (downloadInfo != null) {
			viewHolder.image_download.setMax(downloadInfo.getFileLength());
			viewHolder.image_download.setProgress(downloadInfo.getProgress());
			changeDownloadButtonState(downloadInfo.getState().value(), viewHolder.image_download);
			if (downloadInfo.getState().value() != 7) {
				mDownloadManager.setDownloadListener(item.getSongId(), mDownloadStatusListener);
				viewHolder.iv_download_ok.setVisibility(View.GONE);
			} else {
				viewHolder.iv_download_ok.setVisibility(View.VISIBLE);
				if (downloadInfo.getFileSavePath() != null) {
					File file = new File(downloadInfo.getFileSavePath());
					if (!file.exists()) {
						viewHolder.image_download.setBackgroundResource(R.drawable.aurora_download_btn_bg);
						viewHolder.iv_download_ok.setVisibility(View.GONE);
					}
				}
			}
		} else {
			if (isExsistDownlaod(item)) {
				changeDownloadButtonState(7, viewHolder.image_download);
				viewHolder.iv_download_ok.setVisibility(View.VISIBLE);
				mHandler.obtainMessage(MSG_UPDOWNLOAD, item).sendToTarget();
			} else {
				viewHolder.image_download.setProgress(0);
				viewHolder.image_download.setBackgroundResource(R.drawable.aurora_download_btn_bg);
				viewHolder.iv_download_ok.setVisibility(View.GONE);
			}

		}
		viewHolder.download_parent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ReportUtils.getInstance(mContext.getApplicationContext()).reportMessage(104);
				if (!AuroraMusicUtil.isNetWorkActive(mContext)) {
					Toast.makeText(mContext, R.string.aurora_network_error, Toast.LENGTH_SHORT).show();
					return;
				}
				if(AuroraMusicUtil.isNoPermission(item)){
					mHandler.sendEmptyMessage(MSG_NO_PERMISSION);
					return;
				}
				if (FlowTips.showDownloadFlowTips(mContext, new OndialogClickListener() {

					@Override
					public void OndialogClick() {
						addDownload(item, downloadInfo);
					}
				})) {
					return;
				}
				addDownload(item, downloadInfo);

			}
		});
		if (mList.get(position) instanceof OnlineSong && mCurrentPosition == position) {
			if ((((AuroraNetTrackDetailActivity) mContext).getPlaySelect() != null) && (((AuroraNetTrackDetailActivity) mContext).getPlaySelect().getVisibility() == View.VISIBLE)) {
				viewHolder.iv_song_selected.setVisibility(View.GONE);
			} else
				viewHolder.iv_song_selected.setVisibility(View.VISIBLE);
		} else {
			viewHolder.iv_song_selected.setVisibility(View.GONE);
		}
		boolean isShow = AuroraMusicUtil.isNoPermission(item);
		if(isShow){
			viewHolder.tv_title.setTextColor(Color.parseColor("#b3b3b3"));
			viewHolder.tv_artist.setTextColor(Color.parseColor("#b3b3b3"));
			if(viewHolder.iv_download_ok.getVisibility()==View.GONE){
				viewHolder.image_download.setBackgroundResource(R.drawable.aurora_download_no_btn);
			}
		}else {
			viewHolder.tv_title.setTextColor(Color.parseColor("#ff000000"));
			viewHolder.tv_artist.setTextColor(mContext.getResources().getColor(R.color.aurora_item_song_size));
		}
		return convertView;
	}

	/**
	 * 检测SD指定目录是否存在需要下载到文件
	 * @param OnlineSong
	 * @return
	 */
	private boolean isExsistDownlaod(OnlineSong item) {
		try {
			String name = "";
			if (item.getArtistName().equalsIgnoreCase(item.getSingers())||TextUtils.isEmpty(item.getSingers())) {
				name = item.getSongName().replaceAll("/", "-") + "_" + item.getArtistName().replaceAll("/", "-");
			} else {
				name = item.getSongName().replaceAll("/", "-") + "_" + item.getArtistName().replaceAll("/", "-") + "_" + item.getSingers().replaceAll("/", "-");
			}
			String downloadSd = mSavePath + Globals.SYSTEM_SEPARATOR + name + ".mp3";
			return new File(downloadSd).exists();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 显示下载状态
	 * @param state
	 * @param downloadButton
	 */
	public void changeDownloadButtonState(int state, AuroraRoundProgressBar downloadButton) {
		switch (state) {
		case 0:
			downloadButton.setBackgroundResource(R.drawable.aurora_download_wait_bg);
			break;
		case 1:
		case 2:
			downloadButton.setBackgroundResource(R.drawable.aurora_download_pause_bg);
			break;
		case 7:
			downloadButton.setBackgroundResource(R.drawable.aurora_download_started);
			break;
		case 3:
			downloadButton.setBackgroundResource(R.drawable.aurora_download_started_bg);
			break;
		case 4:
		case 6:
			downloadButton.setBackgroundResource(R.drawable.aurora_download_error_bg);
			break;
		default:
			downloadButton.setBackgroundResource(R.drawable.aurora_download_btn_bg);
			break;
		}
	}

	/**
	 * 全部下载
	 */
	public void downloadAll() {
		if (!AuroraMusicUtil.isNetWorkActive(mContext)) {
			Toast.makeText(mContext, R.string.aurora_network_error, Toast.LENGTH_SHORT).show();
			return;
		}
		if(isNoAvailable()){
			mHandler.sendEmptyMessage(MSG_NO_PERMISSION);
			return;
		}
		LogUtil.d(TAG, "----------------------isPartNoAvailable:"+isPartNoAvailable);
		if(isPartNoAvailable){
			Toast.makeText(mContext, R.string.aurora_part_download_permission, Toast.LENGTH_SHORT).show();
		}
		ThreadPoolExecutor executor = ThreadPoolExecutorUtils.getThreadPoolExecutor().getExecutor();
		executor.execute(new Runnable() {

			@Override
			public void run() {
				downloadAllSong();
			}
		});

	}

	private void downloadAllSong() {
		for (int i = 0; i < mList.size(); i++) {
			try {
				final OnlineSong songinfo = (OnlineSong) mList.get(i);
				if (songinfo == null||AuroraMusicUtil.isNoPermission(songinfo)) {
					continue;
				}
				DownloadInfo downloadInfo = (DownloadInfo) mDownloadManager.getDownloadingMapData().get(songinfo.getSongId());
				if (downloadInfo != null && downloadInfo.getState().value() != 2 && downloadInfo.getState().value() != 7) {
					mDownloadManager.resumeDownloadById(songinfo.getSongId());
					mDownloadManager.setDownloadListener(songinfo.getSongId(), mDownloadStatusListener);
				} else if (downloadInfo == null || (downloadInfo != null && downloadInfo.getState().value() == 7 && !isFileExists(downloadInfo))) {
					long time = System.currentTimeMillis();
					mDownloadManager.addNewDownload(songinfo);
					mDownloadManager.setDownloadListener(songinfo.getSongId(), mDownloadStatusListener);
				}
				mHandler.sendEmptyMessage(MSG_NOTIFY);
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
		
	}

	public void setCurrentPosition(int position) {
		mCurrentPosition = position;
	}

	public int getCurrentPosition() {
		return mCurrentPosition;
	}

	public void clearCache() {
		mDownloadManager.clearListenerMap();
		mHandler.removeCallbacksAndMessages(null);
	}

	public boolean isFileExists(DownloadInfo downloadInfo) {
		if (downloadInfo.getFileSavePath() != null) {
			File file = new File(downloadInfo.getFileSavePath());
			if (!file.exists()) {
				return false;
			}
		}
		return true;
	}

	public void addDownload(OnlineSong item, final DownloadInfo downloadInfo) {
		try {
			LogUtil.d(TAG, "---addDownload-downloadInfo:" + downloadInfo);
			if (downloadInfo != null && (downloadInfo.getState().value() == 7) && isFileExists(downloadInfo)) {
				return;
			} else if (downloadInfo != null && (downloadInfo.getState().value() == 2 || downloadInfo.getState().value() == 0)) {
				mDownloadManager.pauseDownloadById(item.getSongId());
				notifyDataSetChanged();
				return;
			} else if (downloadInfo != null) {
				mDownloadManager.resumeDownloadById(item.getSongId());
				mDownloadManager.setDownloadListener(item.getSongId(), mDownloadStatusListener);
				notifyDataSetChanged();
				return;
			}

			if (item != null) {
				mDownloadManager.addNewDownload(item);
				mDownloadManager.setDownloadListener(item.getSongId(), mDownloadStatusListener);
				notifyDataSetChanged();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
