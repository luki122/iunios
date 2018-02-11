package com.android.auroramusic.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraListView;

import com.android.auroramusic.db.AuroraNetDBConfig;
import com.android.auroramusic.model.SearchItem;
import com.android.auroramusic.ui.AuroraArtistBrowserActivity;
import com.android.auroramusic.ui.AuroraMediaPlayHome;
import com.android.auroramusic.ui.AuroraTrackBrowserActivity;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.widget.AuroraRoundProgressBar;
import com.android.auroramusic.widget.StickyListHeadersAdapter;
import com.android.music.MusicUtils;
import com.android.music.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

public class AuroraSearchAdapter extends BaseAdapter implements
		StickyListHeadersAdapter{
	private static final String IMAGE_CACHE_DIR = "AuroraAlbum";
	private String mSavePath = Globals.mSavePath;
	private String bitrate = "";
	private String BITMAP_DOWNLOAD_DIR;
	private DisplayImageOptions mOptions;
	//private AuroraBitmapDownloader bitmapDownlaoder;
	static class ViewHold {
		TextView line1;
		TextView line2;
		ImageView iv_playicon;
		RelativeLayout front;
		AuroraCheckBox cb;
		View header;
		ImageView iv_songselected;
		AuroraRoundProgressBar image_download;
		ImageView iv_download_ok;
		View view_download_parent;
	}

	public boolean hasDeleted = false;
	private ArrayList<SearchItem> mList;
	private Context mContext;
	private boolean mEditMode = false;
	public static final int WAIT = 0;
	public static final int RUNNING = 1;
	public static final int START = 3;
	public static final int FINISH = 4;
	public static final int EXCEPIONG = 5;
	public static final int EXIST = 200;
	private String prixBitrate = "128";

	public AuroraSearchAdapter(Context context, ArrayList<SearchItem> list) {
		mList = list;
		mContext = context;
		initImageCacheParams();
		//lory del 2014.8.27 end
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (mList != null)
			return mList.size();
		return 0;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		if (mList != null)
			return mList.get(arg0);
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		final int position = arg0;
		final ViewHold vh;
		if (arg1 == null) {
			vh = new ViewHold();
			arg1 = LayoutInflater.from(mContext).inflate(
					com.aurora.R.layout.aurora_slid_listview, null);
			vh.front = (RelativeLayout) arg1
					.findViewById(com.aurora.R.id.aurora_listview_front);
			LayoutInflater.from(mContext).inflate(R.layout.song_listitem,
					vh.front);
			RelativeLayout rl_control_padding = (RelativeLayout) arg1
					.findViewById(com.aurora.R.id.control_padding);
			rl_control_padding.setPadding(0, 0, 0, 0);
			vh.iv_playicon = (ImageView) arg1.findViewById(R.id.song_playicon);
			vh.line1 = (TextView) arg1.findViewById(R.id.song_title);
			vh.line2 = (TextView) arg1.findViewById(R.id.song_artist);
			vh.iv_songselected = (ImageView) arg1
					.findViewById(R.id.iv_song_selected);
			vh.view_download_parent = arg1.findViewById(R.id.aurora_btn_recommend_download_parent);
			vh.image_download = (AuroraRoundProgressBar) arg1.findViewById(R.id.aurora_btn_recommend_download);
			vh.iv_download_ok = (ImageView)arg1.findViewById(R.id.aurora_download_ok);
			vh.header = LayoutInflater.from(mContext).inflate(
					R.layout.aurora_search_header, null);
			arg1.findViewById(R.id.aurora_songlist_parent).setPadding(36, 0, 0,
					0);
			((ViewGroup) arg1).addView(vh.header, 0);
			vh.cb = (AuroraCheckBox) arg1
					.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
			RelativeLayout.LayoutParams rlp = new LayoutParams(
					LayoutParams.MATCH_PARENT, (int) mContext.getResources()
							.getDimension(R.dimen.song_itemheight));
			rlp.addRule(RelativeLayout.CENTER_VERTICAL);
			rlp.setMargins(45, 0, 0, 0);
			((View) vh.cb).setLayoutParams(rlp);
			arg1.setTag(vh);
		} else {
			vh = (ViewHold) arg1.getTag();
		}
		final SearchItem item = mList.get(arg0);
		if (item.mMimeType.equals(MediaStore.Audio.Artists.ARTIST)||"net_artist".equals(item.mMimeType)) {
			if (item.mTag) {
				vh.header.setVisibility(View.VISIBLE);
				((ImageView) vh.header.findViewById(R.id.aurora_search_icon))
						.setImageResource(R.drawable.aurora_search_artisttag);
				((TextView) vh.header.findViewById(R.id.aurora_search_text))
						.setText(mContext.getResources().getString(
								R.string.appwidget_artisttitle));
			} else {
				vh.header.setVisibility(View.GONE);
			}
			vh.view_download_parent.setVisibility(View.GONE);
			vh.iv_download_ok.setVisibility(View.GONE);
			vh.line1.setText(mList.get(arg0).getArtistName());
			vh.line2.setText(mContext.getResources().getString(
					R.string.num_songs_and_album, mList.get(arg0).mAlbumCount,
					mList.get(arg0).mSongCount));
			vh.iv_playicon.setVisibility(View.GONE);
		} else {
			if (mList.get(arg0).mTag) {
				vh.header.setVisibility(View.VISIBLE);
				((ImageView) vh.header.findViewById(R.id.aurora_search_icon))
						.setImageResource(R.drawable.aurora_search_tracktag);
				((TextView) vh.header.findViewById(R.id.aurora_search_text))
						.setText(mContext.getResources().getString(
								R.string.appwidget_songtitle));
			} else {
				vh.header.setVisibility(View.GONE);
			}
			String title = item.getTitle();
			if(title==null||TextUtils.isEmpty(title))
				title = mContext.getResources().getString(R.string.unknown_track);
			vh.line1.setText(title);
			String artist_name = item.getArtistName();
			if(artist_name==null||TextUtils.isEmpty(artist_name))
				artist_name = mContext.getResources().getString(R.string.unknown_artist);
			if(artist_name!=null && artist_name.length()>12){
				artist_name = artist_name.substring(0, 12)+"…";
			}
			vh.line2.setText(artist_name + " · ");
			String artist_album = item.getAlbumName();
			if(artist_album==null||TextUtils.isEmpty(artist_album))
				artist_album = mContext.getResources().getString(R.string.unknown_album_name);
			vh.line2.append(artist_album);
			vh.iv_playicon.setVisibility(View.VISIBLE);
			if (!item.mMimeType.startsWith("net")) {
//				mImageResizer.loadImage(item, vh.iv_playicon, 1);
				ImageLoader.getInstance().displayImage(item.mAlbumImageUri,
						item.getSongId(), vh.iv_playicon, mOptions, null, null);
			}else{
				vh.iv_playicon.setVisibility(View.GONE);
				if(item.mMimeType.equals("net_artist")){
					vh.view_download_parent.setVisibility(View.GONE);
					vh.iv_download_ok.setVisibility(View.GONE);
				}else{
					vh.view_download_parent.setVisibility(View.VISIBLE);
				}
			}
		}
		if (mEditMode) {
			AuroraListView.auroraSetCheckBoxVisible(vh.front, vh.cb, true);
			arg1.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (!((AuroraCheckBox) vh.cb).isChecked()) {
						((AuroraCheckBox) vh.cb).auroraSetChecked(true, true);
						if (mContext instanceof AuroraTrackBrowserActivity) {
							if (mList.get(position).mMimeType.equals("artist")) {
								((AuroraTrackBrowserActivity) mContext)
										.setArtistItemChecked(
												mList.get(position).getSongId(),
												true);
							} else {
								((AuroraTrackBrowserActivity) mContext)
										.setItemChecked(mList.get(position)
												.getSongId(), true);
								((AuroraTrackBrowserActivity) mContext)
										.changeMenuState(1, mList.get(position)
												.getSongId());
							}
						} else if (mContext instanceof AuroraArtistBrowserActivity) {
							((AuroraArtistBrowserActivity) mContext)
									.setItemChecked(mList.get(position)
											.getSongId(), true);
							((AuroraArtistBrowserActivity) mContext)
									.changeMenuState();
						}
					} else {
						((AuroraCheckBox) vh.cb).auroraSetChecked(false, true);
						if (mContext instanceof AuroraTrackBrowserActivity) {
							if (mList.get(position).mMimeType.equals("artist")) {
								((AuroraTrackBrowserActivity) mContext)
										.setArtistItemChecked(
												mList.get(position).getSongId(),
												false);
							} else {
								((AuroraTrackBrowserActivity) mContext)
										.setItemChecked(mList.get(position)
												.getSongId(), false);
								((AuroraTrackBrowserActivity) mContext)
										.changeMenuState(0, mList.get(position)
												.getSongId());
							}
						} else {
							((AuroraArtistBrowserActivity) mContext)
									.setItemChecked(mList.get(position)
											.getSongId(), false);
							((AuroraArtistBrowserActivity) mContext)
									.changeMenuState();
						}
					}
				}
			});
			if (mContext instanceof AuroraTrackBrowserActivity) {
				if (mList.get(position).mMimeType.equals("artist")) {
					if (((AuroraTrackBrowserActivity) mContext)
							.getArtistItemCheckedSongId(mList.get(position)
									.getSongId(),
									mList.get(position).mSongCount))
						((AuroraCheckBox) vh.cb).auroraSetChecked(true, false);
					else
						((AuroraCheckBox) vh.cb).auroraSetChecked(false, false);
				} else {
					if (((AuroraTrackBrowserActivity) mContext)
							.getItemCheckedSongId(mList.get(position)
									.getSongId())) {
						((AuroraCheckBox) vh.cb).auroraSetChecked(true, false);
					} else {
						((AuroraCheckBox) vh.cb).auroraSetChecked(false, false);
					}
				}
			} else if (mContext instanceof AuroraArtistBrowserActivity
					&& ((AuroraArtistBrowserActivity) mContext)
							.getItemCheckedArtisId(mList.get(position)
									.getSongId())) {
				((AuroraCheckBox) vh.cb).auroraSetChecked(true, false);
			} else {
				((AuroraCheckBox) vh.cb).auroraSetChecked(false, false);
			}
			vh.line1.setTextColor(mContext.getResources().getColor(
					R.color.black));
			vh.line2.setTextColor(mContext.getResources().getColor(
					R.color.aurora_item_song_size));
			if(!mList.get(position).mMimeType.startsWith("net")){
//				mImageResizer.loadImage(item, vh.iv_playicon, 1);
				ImageLoader.getInstance().displayImage(item.mAlbumImageUri,
						item.getSongId(), vh.iv_playicon, mOptions, null, null);
			}
		} else {
			arg1.setClickable(false);
			AuroraListView.auroraSetCheckBoxVisible(vh.front, vh.cb, false);
			if(!mList.get(position).mMimeType.startsWith("net")){
//				mImageResizer.loadImage(item, vh.iv_playicon, 1);
				ImageLoader.getInstance().displayImage(item.mAlbumImageUri,
						item.getSongId(), vh.iv_playicon, mOptions, null, null);
			}
			if (!mList.get(position).mMimeType.equals("artist")) {
				long id = MusicUtils.getCurrentAudioId();
				if (item.getSongId() == id) {
					vh.iv_songselected.setVisibility(View.VISIBLE);
				} else {
					vh.iv_songselected.setVisibility(View.GONE);
				}
			} else {
				vh.iv_songselected.setVisibility(View.GONE);
			}
		}
		arg1.setBackgroundResource(R.drawable.aurora_playlist_item_clicked);
		return arg1;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getHeaderId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	private void initImageCacheParams() {
		mOptions = new DisplayImageOptions.Builder()
		.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).cacheInMemory(true)
		.cacheOnDisk(true).showImageOnLoading(R.drawable.default_music_icon2)
		.showImageForEmptyUri(R.drawable.default_music_icon2)
		.showImageOnFail(R.drawable.default_music_icon2)
		.displayer(new RoundedBitmapDisplayer((int)mContext.getResources().getDimension(R.dimen.aurora_album_play_icon_margin_bottom))).build();
	}

	public void setEidtMode(boolean flag) {
		mEditMode = flag;
	}
	
	
	public void clearCache(){
	}

}
