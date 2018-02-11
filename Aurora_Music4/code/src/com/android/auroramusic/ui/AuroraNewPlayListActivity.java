package com.android.auroramusic.ui;

import java.util.ArrayList;
import java.util.List;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraMenuBase;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import aurora.widget.AuroraMenu;

import com.android.auroramusic.util.AuroraMainMenuData;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.LogUtil;
import com.android.music.MusicUtils;
import com.android.music.R;

public class AuroraNewPlayListActivity extends AbstractBaseActivity implements OnClickListener {

	private static final String TAG = "AuroraNewPlayListActivity";
	private AuroraActionBar mActionBar;
	private AuroraMenu bottomMenu;
	private AuroraMainMenuData playlistInfo;
	private TextView bottomBarTitle;
	// private long[] mAddList;
	private ArrayList<String> mAddList = new ArrayList<String>();
	public static final int REQUEST_CODE_SONGS = 100;
	public static final int REQUEST_CODE_SONGS_FOLD = 101;
	public static final String EXTR_RESULT_DATA = "extr_result_data";
	public static final String EXTR_RESULT_MODE = "extr_result_mode";
	public static final String EXTR_ADDLIST_DATA = "addlist_data";
	private View songButton, artistButton, foldButton;
	private boolean isclicked = false;
	private List<String> ignorPathList = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		try {
			ignorPathList = AuroraMusicUtil.doParseXml(AuroraNewPlayListActivity.this, "paths.xml");
			setContent();
			initIntent();
			initViews();
			AuroraMusicActivityManiger.getInstance().addActivity(this);
		} catch (Exception e) {
			Log.i(TAG, "zll ---- AuroraNewPlayListActivity fail");
		}
	}

	protected void setContent() {
		setAuroraContentView(R.layout.aurora_newplaylist_activity, AuroraActionBar.Type.Normal, true);
	}

	// add by chenhl start
	protected void initIntent() {
		Intent intent = getIntent();
		if (intent != null) {
			playlistInfo = (AuroraMainMenuData) intent.getSerializableExtra(AuroraSongSingle.EXTR_PLAYLIST_INFO);
		}
	}

	// add by chenhl end
	protected void initViews() {
		initActionBar();
		songButton = findViewById(R.id.aurora_addsong_button);
		artistButton = findViewById(R.id.aurora_addartist_button);
		foldButton = findViewById(R.id.aurora_addfold_button);
		initButton(songButton, R.drawable.aurora_music_icon, R.string.playlist_songs, 0);
		initButton(artistButton, R.drawable.aurora_singer_icon, R.string.playlist_artist, 1);
		initButton(foldButton, R.drawable.aurora_fold_icon, R.string.playlist_files, 2);
	}

	private void initButton(View view, int resId, int strId, int type) {
		ImageView icon = (ImageView) view.findViewById(R.id.id_icon);
		TextView name = (TextView) view.findViewById(R.id.id_name);
		TextView size = (TextView) view.findViewById(R.id.id_song_size);
		icon.setImageResource(resId);
		name.setText(strId);
		view.setOnClickListener(this);
		switch (type) {
		case 0:
			size.setText(getString(R.string.aurora_playlist_total_songs, 0));
			break;
		case 1:
			size.setText(getString(R.string.aurora_playlist_total_singer, 0));
			break;
		case 2:
			size.setText(getString(R.string.aurora_playlist_total_fold, 0));
			break;
		}
		getLoaderManager().initLoader(type, null, new LoadSize(size));
	}

	private void initActionBar() {
		try {
			mActionBar = getAuroraActionBar();
			if (mActionBar == null) {
				return;
			}

			mActionBar.setTitle(R.string.playlist_addsongs);
			// add by chenhl start
			setAuroraBottomBarMenuCallBack(auroraMenuCallBack);
			mActionBar.initActionBottomBarMenu(R.menu.menu_add_playlist_song_bottombar, 1);
			bottomMenu = mActionBar.getAuroraActionBottomBarMenu();
			bottomBarTitle = bottomMenu.getTitleViewByPosition(0);
			bottomBarTitle.setText(getString(R.string.aurora_add_playlist_song_num, 0));
			bottomMenu.setBottomMenuItemEnable(0, false);
			mActionBar.setmOnActionBarBackItemListener(mOnAuroraActionBarBackItemClickListener);
			// add by chenhl start
		} catch (Exception e) {
			Log.i(TAG, "zll --- initActionBar fail");
		}
		return;
	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		mActionBar.setShowBottomBarMenu(true);
		mActionBar.showActionBottomeBarMenu();
	}

	@Override
	protected void onResume() {
		isclicked = false;
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		mActionBar.setShowBottomBarMenu(false);
		mActionBar.showActionBottomeBarMenu();
		AuroraMusicActivityManiger.getInstance().removeActivity(this);
		super.onDestroy();
	}

	// add by chenhl start
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			gobackToSongSingle();
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	private OnAuroraActionBarBackItemClickListener mOnAuroraActionBarBackItemClickListener = new OnAuroraActionBarBackItemClickListener() {

		@Override
		public void onAuroraActionBarBackItemClicked(int arg0) {

			gobackToSongSingle();
		}

	};

	/**
	 * 回到歌单页面
	 */
	private void gobackToSongSingle() {
		Intent intent = new Intent(this, AuroraSongSingle.class);
		intent.putExtra(AuroraSongSingle.EXTR_PLAYLIST_INFO, playlistInfo);
		startActivity(intent);
		finish();
//		overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		LogUtil.d(TAG, "requestCode:" + requestCode + " resultCode:" + resultCode);
		if (resultCode != RESULT_OK) {
			return;
		}

		switch (requestCode) {
		case REQUEST_CODE_SONGS:
			if (data == null) {
				return;
			}
			int mode = data.getIntExtra(EXTR_RESULT_MODE, 0);
			if (mode == 0) {
				mAddList = data.getStringArrayListExtra(EXTR_RESULT_DATA);
				if (mAddList.size() > 0) {
					bottomMenu.setBottomMenuItemEnable(0, true);
				} else {

					bottomMenu.setBottomMenuItemEnable(1, false);
				}
				bottomBarTitle.setText(getString(R.string.aurora_add_playlist_song_num, mAddList.size()));
			} else {
				// finish();
			}
			break;
		}
	}

	/**
	 * 添加按钮响应
	 */
	private AuroraMenuBase.OnAuroraMenuItemClickListener auroraMenuCallBack = new AuroraMenuBase.OnAuroraMenuItemClickListener() {
		@Override
		public void auroraMenuItemClick(int itemId) {
			switch (itemId) {
			case R.id.song_add:
				if (mAddList != null && mAddList.size() != 0) {

					MusicUtils.addToPlaylist(AuroraNewPlayListActivity.this, mAddList, playlistInfo.getPlaylistId(), playlistInfo.getName());
					gobackToSongSingle();
				}
				break;
			default:
				break;
			}
		}
	};

	// add by chenhl end

	@Override
	public void onMediaDbChange(boolean selfChange) {

	}

	@Override
	public void onClick(View arg0) {
		if (isclicked) {
			return;
		}
		isclicked = true;
		switch (arg0.getId()) {
		case R.id.aurora_addsong_button:
			Intent intent1 = new Intent(this, AuroraTrackBrowserActivity.class);
			intent1.putExtra(AuroraSongSingle.EXTR_PLAYLIST_INFO, playlistInfo);
			intent1.putExtra(AuroraTrackBrowserActivity.FROM_CODE, 1);
			intent1.putStringArrayListExtra(EXTR_ADDLIST_DATA, mAddList);
			startActivityForResult(intent1, REQUEST_CODE_SONGS);
			break;
		case R.id.aurora_addartist_button:
			Intent intent2 = new Intent(this, AuroraArtistBrowserActivity.class);
			intent2.putExtra(AuroraSongSingle.EXTR_PLAYLIST_INFO, playlistInfo);
			intent2.putExtra(AuroraTrackBrowserActivity.FROM_CODE, 1);
			intent2.putStringArrayListExtra(EXTR_ADDLIST_DATA, mAddList);
			startActivityForResult(intent2, REQUEST_CODE_SONGS);
			break;
		case R.id.aurora_addfold_button:
			Intent intent3 = new Intent(this, AuroraFoldActivity.class);
			intent3.putExtra(AuroraSongSingle.EXTR_PLAYLIST_INFO, playlistInfo);
			intent3.putExtra(AuroraFoldActivity.EXTR_FOLD_START_MODE, 1);
			intent3.putStringArrayListExtra(EXTR_ADDLIST_DATA, mAddList);
			startActivityForResult(intent3, REQUEST_CODE_SONGS);
			break;
		}
	}

	public class LoadSize implements LoaderCallbacks<Cursor> {

		private TextView textview;

		public LoadSize(TextView view) {
			this.textview = view;
		}

		@Override
		public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
			LogUtil.d(TAG, "onCreateLoader---arg0:" + arg0);
			CursorLoader loadrer = null;
			if (arg0 == 0) {
				Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				StringBuilder where = new StringBuilder();
				where.append(Globals.QUERY_SONG_FILTER);
				loadrer = new CursorLoader(AuroraNewPlayListActivity.this, uri, new String[] { MediaStore.Audio.Media.DATA }, where.toString(), null, null);
			} else if (arg0 == 1) {

				Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				StringBuilder where = new StringBuilder();
				where.append(Globals.QUERY_SONG_FILTER);
				where.append(AuroraMusicUtil.getFileString(AuroraNewPlayListActivity.this));
				where.append(") GROUP BY (" + MediaStore.Audio.Media.ARTIST_ID);
				loadrer = new CursorLoader(AuroraNewPlayListActivity.this, uri, new String[] { MediaStore.Audio.Media.DATA }, where.toString(), null, null);
			} else if (arg0 == 2) {
				Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				StringBuilder where = new StringBuilder();
				where.append(Globals.QUERY_SONG_FILTER);
				loadrer = new CursorLoader(AuroraNewPlayListActivity.this, uri, new String[] { MediaStore.Audio.Media.DATA }, where.toString(), null, null);
			}
			return loadrer;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
			LogUtil.d(TAG, "onLoadFinished---arg0:" + arg0.getId());
			List<String> list = new ArrayList<String>();
			if (cursor == null) {
				return;
			}
			int size = cursor.getCount();
			switch (arg0.getId()) {
			case 0:
				if (cursor.moveToFirst()) {
					do {
						String dir = cursor.getString(0);
						dir = dir.substring(0, dir.lastIndexOf("/"));
						if (!ignorPathList.contains(dir)) {
							list.add(dir);
						}
					} while (cursor.moveToNext());
				}
				size = list.size();
				textview.setText(getString(R.string.aurora_playlist_total_songs, size));
				break;
			case 1:
				textview.setText(getString(R.string.aurora_playlist_total_singer, size));
				break;
			case 2:

				if (cursor.moveToFirst()) {
					do {
						String path = cursor.getString(0);
						path = path.substring(0, path.lastIndexOf("/"));
						if (!list.contains(path) && !ignorPathList.contains(path)) {
							list.add(path);
						}
					} while (cursor.moveToNext());
				}
				size = list.size();
				textview.setText(getString(R.string.aurora_playlist_total_fold, size));
				break;
			}
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {

		}
	}

}
