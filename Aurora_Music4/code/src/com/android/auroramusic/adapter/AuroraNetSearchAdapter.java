package com.android.auroramusic.adapter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.auroramusic.db.AuroraNetDBConfig;
import com.android.auroramusic.downloadex.DownloadInfo;
import com.android.auroramusic.downloadex.DownloadManager;
import com.android.auroramusic.downloadex.DownloadStatusListener;
import com.android.auroramusic.online.AuroraNetSearchActivity;
import com.android.auroramusic.online.AuroraNetTrackActivity;
import com.android.auroramusic.ui.AuroraMediaPlayHome;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.FlowTips;
import com.android.auroramusic.util.ReportUtils;
import com.android.auroramusic.util.FlowTips.OndialogClickListener;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.LogUtil;
import com.android.auroramusic.widget.AuroraRoundProgressBar;
import com.android.music.MusicUtils;
import com.android.music.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.xiami.sdk.entities.OnlineAlbum;
import com.xiami.sdk.entities.OnlineSong;

public class AuroraNetSearchAdapter extends BaseAdapter {
	private boolean showAlbumTag = false;
	private boolean showMoremTag = true;
	private List<Object> mList;
	private Context mContext;
	private int mIconSize;
	private int albumTag = -1;
	private int songTag = -1;
	private int mCurrentPosition = -1;
	private String mSavePath = Globals.mSavePath;
	private String bitrate = "";
	public static final int WAIT = 0;
	public static final int RUNNING = 1;
	public static final int START = 3;
	public static final int FINISH = 4;
	public static final int EXCEPIONG = 5;
	public static final int EXIST = 200;
	private String prixBitrate = "128";
	private String BITMAP_DOWNLOAD_DIR;
	private DownloadManager mDownloadManager;
	public static final int MSG_NOTIFY = 10000;
	public static final int MSG_NO_PERMISSION = 10002;
	private String IMAGE_CACHE_DIR = "NetAlbum";
	private static final String TAG = "AuroraNetSearchAdapter";
	DisplayImageOptions mOptions;


	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == MSG_NOTIFY) {
				notifyDataSetChanged();
			} else if (msg.what == MSG_NO_PERMISSION) {
				Toast.makeText(mContext, R.string.aurora_download_permission, Toast.LENGTH_SHORT).show();
			}
		};
	};
	DownloadStatusListener mDownloadStatusListener = new DownloadStatusListener() {

		@Override
		public void onDownload(String url, long id, final int status, final long downloadSize, final long fileSize) {
			mHandler.sendEmptyMessage(MSG_NOTIFY);
		};
	};

	static class vh {
		View songitem_parent;
		View download_parent;
		ImageView image_album;
		AuroraRoundProgressBar image_download;
		TextView tv_title;
		TextView tv_artist;
		TextView tv_top;
		View iv_song_selected;
		ImageView iv_download_ok;
	}

	static class Trackvh {
		ImageView image_album;
		AuroraRoundProgressBar image_download;
		TextView tv_title;
		TextView tv_artist;
		TextView tv_top;
	}

	public AuroraNetSearchAdapter(Context context) {
		mContext = context;
		mIconSize = (int) mContext.getResources().getDimension(R.dimen.aurora_album_item_height);
		mDownloadManager = DownloadManager.getInstance(context);
		initImageCacheParams();
	}

	private void initImageCacheParams() {
		mOptions = new DisplayImageOptions.Builder().imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).cacheInMemory(true).cacheOnDisk(true).displayer(new SimpleBitmapDisplayer()).build();
	}

	public void setList(List<Object> list) {
		mList = list;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList == null ? 0 : mList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mList == null ? null : mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return mList == null ? 0 : position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final vh vh;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.aurora_netsearch_listitem, null);
			vh = new vh();
			vh.tv_top = (TextView) convertView.findViewById(R.id.song_playicon);
			vh.download_parent = convertView.findViewById(R.id.aurora_btn_recommend_download_parent);
			vh.songitem_parent = convertView.findViewById(R.id.aurora_songitem_parent);
			vh.image_download = (AuroraRoundProgressBar) convertView.findViewById(R.id.aurora_btn_recommend_download);
			vh.tv_title = (TextView) convertView.findViewById(R.id.song_title);
			vh.tv_artist = (TextView) convertView.findViewById(R.id.song_artist);
			vh.iv_song_selected = convertView.findViewById(R.id.iv_song_selected);
			vh.iv_download_ok = (ImageView) convertView.findViewById(R.id.aurora_download_ok);
			convertView.setTag(vh);
		} else {
			vh = (vh) convertView.getTag();
		}

		final OnlineSong item = (OnlineSong) mList.get(position);
		if (songTag == -1) {
			songTag = position;
		}
		vh.songitem_parent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (AuroraMusicUtil.isNoPermission(item)) {
					Toast.makeText(mContext, R.string.aurora_play_permission, Toast.LENGTH_SHORT).show();
					return;
				}
				((AuroraNetSearchActivity) mContext).playMusic(position);
			}
		});
		String trackTitle = item.getSongName();
		String artistName = item.getArtistName();
		if (position - songTag + 1 >= 1000) {
			vh.tv_top.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimensionPixelSize(R.dimen.aurora_toptext_size_small));
		} else if (position - songTag + 1 >= 100) {
			vh.tv_top.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimensionPixelSize(R.dimen.aurora_toptext_size));
		} else {
			vh.tv_top.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimensionPixelSize(R.dimen.aurora_toptext_sizebig));
		}
		vh.tv_top.setText(String.valueOf(position - songTag + 1));
		if (artistName != null && artistName.length() > 12) {
			artistName = artistName.substring(0, 12) + "…";
		}
		String albumName = item.getAlbumName();

		if (TextUtils.isEmpty(trackTitle))
			vh.tv_title.setText(mContext.getResources().getString(R.string.unknown_track));
		else
			vh.tv_title.setText(trackTitle);
		if (TextUtils.isEmpty(artistName)) {
			vh.tv_artist.setText(mContext.getResources().getString(R.string.unknown_artist) + " · ");
		} else {
			vh.tv_artist.setText(artistName + " · ");
		}
		if (TextUtils.isEmpty(albumName)) {
			vh.tv_artist.append(mContext.getResources().getString(R.string.unknown_album_name));
		} else {
			vh.tv_artist.append(albumName);
		}
		final DownloadInfo downloadInfo = mDownloadManager.getDownloadingMapData().get(item.getSongId());
		if (downloadInfo != null) {
			vh.image_download.setMax(downloadInfo.getFileLength());
			vh.image_download.setProgress(downloadInfo.getProgress());
			changeDownloadButtonState(downloadInfo.getState().value(), vh.image_download);
			if (downloadInfo.getState().value() != 7) {
				mDownloadManager.setDownloadListener(item.getSongId(), mDownloadStatusListener);
				vh.iv_download_ok.setVisibility(View.GONE);
			} else {
				vh.iv_download_ok.setVisibility(View.VISIBLE);
				if (downloadInfo.getFileSavePath() != null) {
					File file = new File(downloadInfo.getFileSavePath());
					if (!file.exists()) {
						vh.image_download.setBackgroundResource(R.drawable.aurora_download_btn_bg);
						vh.iv_download_ok.setVisibility(View.GONE);
					}
				}
			}
		} else {
			vh.image_download.setProgress(0);
			vh.image_download.setBackgroundResource(R.drawable.aurora_download_btn_bg);
			vh.iv_download_ok.setVisibility(View.GONE);
		}
		vh.download_parent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ReportUtils.getInstance(mContext.getApplicationContext()).reportMessage(104);
				if (!AuroraMusicUtil.isNetWorkActive(mContext)) {
					Toast.makeText(mContext, R.string.aurora_network_error, Toast.LENGTH_SHORT).show();
					return;
				}
				if (AuroraMusicUtil.isNoPermission(item)) {
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
		if (MusicUtils.getCurrentAudioId() == Long.valueOf(((OnlineSong) mList.get(position)).getSongId()) && AuroraNetSearchActivity.mPlaySelect.getVisibility() == View.GONE) {
			vh.iv_song_selected.setVisibility(View.VISIBLE);
			mCurrentPosition = position;
		} else {
			vh.iv_song_selected.setVisibility(View.GONE);
		}
		
		boolean isShow = AuroraMusicUtil.isNoPermission(item);
		if (isShow) {
			vh.tv_title.setTextColor(Color.parseColor("#b3b3b3"));
			vh.tv_artist.setTextColor(Color.parseColor("#b3b3b3"));
			if (vh.iv_download_ok.getVisibility() == View.GONE) {
				vh.image_download.setBackgroundResource(R.drawable.aurora_download_no_btn);
			}
		} else {
			vh.tv_title.setTextColor(Color.parseColor("#ff000000"));
			vh.tv_artist.setTextColor(mContext.getResources().getColor(R.color.aurora_item_song_size));
		}
		return convertView;
	}

	public void setShowAlbumTag(boolean flag) {
		showAlbumTag = flag;
	}

	public void setShowMoreTag(boolean flag) {
		showMoremTag = flag;
	}

	public void setCurrentPosition(int position) {
		mCurrentPosition = position;
	}

	public int getCurrentPosition() {
		return mCurrentPosition;
	}

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
			LogUtil.e(TAG, " addDownload error ", e);
		}
	}
}
