package com.android.auroramusic.adapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.android.auroramusic.ui.AuroraSongSingle;
import com.android.auroramusic.util.AuroraListItem;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.DialogUtil.OnRemoveFileListener;
import com.android.auroramusic.util.DisplayUtil;
import com.android.auroramusic.util.LogUtil;
import com.android.auroramusic.util.DialogUtil.OnDeleteFileListener;
import com.android.music.MusicUtils;
import com.android.music.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraListView;

/**
 * create date:20140512
 * @author chenhl
 */
public class AuroraSongSingleAdapter extends BaseAdapter implements OnDeleteFileListener,OnRemoveFileListener {

	private static final String TAG = "AuroraSongSingleAdapter";
	private List<AuroraListItem> mData = new ArrayList<AuroraListItem>();
	private SparseBooleanArray mCheckedMap = new SparseBooleanArray();

	private LayoutInflater mInflater;
	private static String mArtistName;
	private static String mAlbumName;
	private boolean isEditMode = false;
	private boolean mNeedin = false, mNeedout = false;
	private Handler mHandler = new Handler();
	private Handler activityHandler = null;
	private static final String IMAGE_CACHE_DIR = "AuroraAlbum";
	private Context mContext;
	private int mCurrentPosition = -2; // 点击播放位置
	public boolean hasDeleted = false;
	private int playListId;
	private boolean isHideSelect = false;
	private boolean isShowText = false;
	private DisplayImageOptions disoptions;
	private Bitmap defaultBitmap=null;

	public AuroraSongSingleAdapter(Context context, List<AuroraListItem> list) {
		this(context);
		mData = list;
	}

	public AuroraSongSingleAdapter(Context context, int playlistid, int startmode) {
		this(context);
		playListId = playlistid;
		if (startmode != 1) {
			isShowText = true;
		} else {
			isShowText = false;
		}
	}

	public AuroraSongSingleAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
		mArtistName = context.getString(R.string.unknown);
		mAlbumName = context.getString(R.string.unknown_album_name);
		mContext = context;
		initImageCacheParams();
	}

	public void setSonglist(ArrayList<AuroraListItem> list) {
		if (mData == null) {
			mData = new ArrayList<AuroraListItem>();
		}

		if (list == null) {
			return;
		}
		// mData.addAll(list);
		mData = list;
		// LogUtil.d(TAG, "setSonglist...mData:" + mData.size());
		notifyDataSetChanged();
	}

	public void deleteItem(int position) {
		if (position < 0 || position >= mData.size()) {
			LogUtil.d(TAG, "delete position error:" + position);
			return;
		}

		// 刚好删除正在播放的歌曲，将播放位置置空
		if (mCurrentPosition == position) {
			mCurrentPosition = -2;
		} else if (mCurrentPosition > position) {
			mCurrentPosition--;
		}
		mData.remove(position);
		notifyDataSetChanged();
	}

	public void setHandler(Handler handler) {
		activityHandler = handler;
	}

	public void setNeedIn() {
		mNeedin = true;
		isEditMode = true;
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				mNeedin = false;
			}
		}, 50);
	}

	public void setNeedIn(int position) {
		mNeedin = true;
		isEditMode = true;
		mCheckedMap.put(position, true);
		notifyDataSetChanged();
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				mNeedin = false;
			}
		}, 50);
	}

	public void setNeedOut() {
		mNeedout = true;
		isEditMode = false;
		mCheckedMap.clear();
		notifyDataSetChanged();
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				mNeedout = false;
			}
		}, 50);
	}

	/**
	 * 全选
	 */
	public void selectAll() {

		for (int i = 0; i < mData.size(); i++) {
			mCheckedMap.put(i, true);
		}
		notifyDataSetChanged();
	}

	/**
	 * 反选
	 */
	public void selectAllNot() {
		mCheckedMap.clear();
		notifyDataSetChanged();
	}

	/**
	 * 选择个数
	 * @return
	 */
	public int getCheckedCount() {
		return mCheckedMap.size();
	}

	/**
	 * 设置选项
	 * @param position
	 */
	public void setSelected(int position) {
		mCurrentPosition = position;
		if (position > -2) {
			isHideSelect = false;
		}
	}

	public void setNotSelect() {
		isHideSelect = true;
	}

	public List<String> getCheckedId() {
		List<String> list = new ArrayList<String>();
		Iterator<AuroraListItem> it = mData.iterator();
		int i = 0;
		while (it.hasNext()) {
			AuroraListItem info = it.next();
			Boolean checked = mCheckedMap.get(i);
			if (checked != null && checked) {
				list.add(String.valueOf(info.getSongId()));
			}
			i++;
		}
		return list;
	}

	private int count;
	public void deletesongs(int startMode) {
		
		Iterator<AuroraListItem> it = mData.iterator();
		int i = 0, j = 0;
		count = 0;
		long[] list = new long[mCheckedMap.size()];
		while (it.hasNext()) {
			AuroraListItem info = it.next();
			Boolean checked = mCheckedMap.get(i);
			if (checked != null && checked) {

				list[j] = info.getSongId();
				j++;
				// 刚好删除正在播放的歌曲，将播放位置置空
				if (mCurrentPosition == i) {
					mCurrentPosition = -2;
				} else if (mCurrentPosition > i) {
					count++;
				}
				it.remove();

			}
			i++;
		}
		if (startMode == 0) {
		/*	StringBuilder where = new StringBuilder();
			where.append(MediaStore.Audio.Playlists.Members.AUDIO_ID + " IN (");
			for (int i1 = 0; i1 < list.length; i1++) {
				where.append(list[i1]);
				if (i1 < list.length - 1) {
					where.append(",");
				}
			}
			where.append(")");
			Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playListId);
			mContext.getContentResolver().delete(uri, where.toString(), null);
			
			MusicUtils.removeTracksFromCurrentPlaylist(mContext, list, playListId);
			mCurrentPosition -= count;
			mCheckedMap.clear();
			notifyDataSetChanged();*/
			MusicUtils.removeMediaTracks(mContext, list, this,playListId);
			LogUtil.d(TAG, "AURORA_ID_DELETE_SONGS removeMediaTracks 1");
		} else if (startMode == 2) {
			MusicUtils.removeTracksFromCurrentPlaylist(mContext, list, playListId);
			MusicUtils.mSongDb.deleteFavoritesById(list);
			AuroraMusicUtil.showDeleteToast(mContext, mCheckedMap.size(), startMode);
			mCurrentPosition -= count;
			mCheckedMap.clear();
			notifyDataSetChanged();
			LogUtil.d(TAG, "AURORA_ID_DELETE_SONGS deletesongs 2");
		} else {
			// MusicUtils.deleteTracks(mContext, list);
			MusicUtils.deleteMediaTracks(mContext, list, this);
			LogUtil.d(TAG, "AURORA_ID_DELETE_SONGS deletesongs 3");
		}
		LogUtil.d(TAG, "AURORA_ID_DELETE_SONGS deletesongs startMode:" + startMode);
	}
	
	@Override
	public void OnRemoveFileSuccess() {
//		LogUtil.d(TAG, "-----mCheckedMap:"+mCheckedMap.size());
		mCurrentPosition -= count;
		mCheckedMap.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int arg0) {

		return mData.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {

		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {

		final HoldView holdView;
		if (arg1 == null) {
			holdView = new HoldView();
			arg1 = mInflater.inflate(com.aurora.R.layout.aurora_slid_listview, null);
			// View myView = mInflater.inflate(R.layout.aurora_songsingle_item,
			// null);
			// 获取添加内容的对象
			RelativeLayout mainUi = (RelativeLayout) arg1.findViewById(com.aurora.R.id.aurora_listview_front);
			mInflater.inflate(R.layout.aurora_songsingle_item, mainUi);
			// 将要显示的内容添加到mainUi中去
			// mainUi.addView(myView, 0, new LayoutParams(
			// LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			// 设置间距
			RelativeLayout rl_control_padding = (RelativeLayout) arg1.findViewById(com.aurora.R.id.control_padding);
			rl_control_padding.setPadding(0, 0, 0, 0);

			holdView.icon = (ImageView) mainUi.findViewById(R.id.song_playicon);
			holdView.songName = (TextView) mainUi.findViewById(R.id.song_title);
			holdView.songAlbum = (TextView) mainUi.findViewById(R.id.song_artist);
			holdView.songPlay = (ImageView) mainUi.findViewById(R.id.iv_song_selected);
			holdView.front = mainUi;
			holdView.cb = (AuroraCheckBox) arg1.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
//			RelativeLayout.LayoutParams rlp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//			rlp.addRule(RelativeLayout.CENTER_VERTICAL);
//			rlp.setMargins(DisplayUtil.dip2px(mContext, 13), 0, 0, 0);
//			((View) holdView.cb).setLayoutParams(rlp);

			// 垃圾桶层
			if (isShowText) {
				LinearLayout listBack = (LinearLayout) arg1.findViewById(com.aurora.R.id.aurora_listview_back);
				TextView rubbish = new TextView(mContext);
				rubbish.setGravity(Gravity.CENTER);
				rubbish.setTextColor(Color.parseColor("#ffffff"));
				rubbish.setText(mContext.getString(R.string.aurora_remove));
				listBack.addView(rubbish, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				View rubishView = arg1.findViewById(com.aurora.R.id.aurora_rubbish);
				if (rubishView != null) {
					rubishView.setVisibility(View.GONE);
				}
			}
			holdView.contentView = arg1.findViewById(R.id.aurora_item_content);
			arg1.setTag(holdView);
		} else {
			holdView = (HoldView) arg1.getTag();
		}

		if (!isEditMode) {
			arg1.setBackgroundResource(R.drawable.aurora_playlist_item_clicked);
		} else {
			arg1.setBackgroundResource(android.R.color.transparent);
		}

		// 操作复选框
		if (mNeedin) {
			AuroraListView.auroraStartCheckBoxAppearingAnim(holdView.front, holdView.cb);
		} else if (!mNeedin && isEditMode) {
			AuroraListView.auroraSetCheckBoxVisible(holdView.front, holdView.cb, true);
		}

		if (mNeedout) {
			AuroraListView.auroraStartCheckBoxDisappearingAnim(holdView.front, holdView.cb);
		} else if (!isEditMode && !mNeedout) {
			AuroraListView.auroraSetCheckBoxVisible(holdView.front, holdView.cb, false);
		}

		if (hasDeleted) {
			ViewGroup.LayoutParams vl = arg1.getLayoutParams();
			if (null != vl) {
				vl.height = mContext.getResources().getDimensionPixelSize(R.dimen.aurora_song_item_height);
			}
			arg1.findViewById(com.aurora.R.id.content).setAlpha(255);
			arg1.setAlpha(255);
		}

		if (isEditMode) {
			final int position = arg0;
			// arg1.setClickable(true);
			final AuroraCheckBox checkbox = (AuroraCheckBox) holdView.cb;
			// checkbox.setClickable(true);
			arg1.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					if (!checkbox.isChecked()) {
						checkbox.auroraSetChecked(true, true);
						mCheckedMap.put(position, true);
					} else {
						checkbox.auroraSetChecked(false, true);
						mCheckedMap.delete(position);

					}
					if (activityHandler != null) {
						activityHandler.obtainMessage(1).sendToTarget();
					}
				}
			});
			Boolean checked = mCheckedMap.get(position);
			if (checked != null && checked) {
				((AuroraCheckBox) holdView.cb).setChecked(true);
			} else {
				((AuroraCheckBox) holdView.cb).setChecked(false);
			}

			holdView.songName.setTextColor(mContext.getResources().getColor(R.color.black));
			holdView.songAlbum.setTextColor(mContext.getResources().getColor(R.color.aurora_item_song_size));
		} else {
			holdView.songName.setTextColor(mContext.getResources().getColorStateList(R.color.black));
			holdView.songAlbum.setTextColor(mContext.getResources().getColorStateList(R.color.aurora_item_song_size));
			arg1.setClickable(false);
		}

		final AuroraListItem info = mData.get(arg0);
		if (info != null) {
			if (info.getSongId() == MusicUtils.getCurrentAudioId() && !isEditMode) {
				if (isHideSelect) {
					holdView.songPlay.setVisibility(View.INVISIBLE);
				} else {
					holdView.songPlay.setVisibility(View.VISIBLE);
					mCurrentPosition = arg0;
				}

			} else {
				holdView.songPlay.setVisibility(View.INVISIBLE);
			}
			holdView.songName.setText(info.getTitle());

			StringBuffer tBuffer = new StringBuffer();
			String artiststr = info.getArtistName();
			if (artiststr == null || MediaStore.UNKNOWN_STRING.equals(artiststr)) {
				artiststr = mArtistName;
			}

			String albumstr = info.getAlbumName();
			albumstr = AuroraMusicUtil.doAlbumName(info.getFilePath(), albumstr);
			if (albumstr == null || MediaStore.UNKNOWN_STRING.equals(albumstr)) {
				albumstr = mAlbumName;
			}
			if (artiststr.length() > 12) {
				artiststr = artiststr.substring(0, 12) + "…";
			}
			tBuffer.append(artiststr);
			tBuffer.append("·");
			tBuffer.append(albumstr);
			holdView.songAlbum.setText(tBuffer.toString());
//			LogUtil.d(TAG, "info.getAlbumImgUri():" + info.getAlbumImgUri());
//			if (!TextUtils.isEmpty(info.getAlbumImgUri())) {//fix album icon show
				ImageLoader.getInstance().displayImage(info.getAlbumImgUri(), info.getSongId(), holdView.icon, disoptions, null, null);
//			}else {
//				holdView.icon.setImageBitmap(defaultBitmap);
//			}
		}
		return arg1;
	}

	class HoldView {
		ImageView icon;
		TextView songName;
		TextView songAlbum;
		ImageView songPlay;
		RelativeLayout front;
		AuroraCheckBox cb;
		View contentView;
	}

	private void initImageCacheParams() {
		if(defaultBitmap==null){
			defaultBitmap= BitmapFactory.decodeResource(mContext.getResources(),
					R.drawable.default_music_icon);
		}
		disoptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.default_music_icon).showImageForEmptyUri(R.drawable.default_music_icon)
				.showImageOnFail(R.drawable.default_music_icon).cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).displayer(new SimpleBitmapDisplayer()).build();
	}

	public int getCurrentPlayPosition() {
		return mCurrentPosition + 1;
	}

	@Override
	public void OnDeleteFileSuccess() {
		// mCurrentPosition -= mCount;
		mCheckedMap.clear();
		notifyDataSetChanged();
		((AuroraSongSingle) mContext).updateTitleSizeAndExitMode();
	}

	public boolean ischeckedOnline() {

		for (int i = 0; i < mData.size(); i++) {
			if (mCheckedMap == null) {
				return false;
			}
			Boolean checked = mCheckedMap.get(i);
			if (checked != null && checked) {
				AuroraListItem item = mData.get(i);
				if (item.getIsDownLoadType() == 1) {
					return true;
				}
			}
		}
		return false;
	}

}
