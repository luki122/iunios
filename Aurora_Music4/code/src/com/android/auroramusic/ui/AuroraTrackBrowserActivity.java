/*
 * /* /* /* Copyright (C) 2007 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.auroramusic.ui;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import aurora.app.AuroraActivity.OnSearchViewQuitListener;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraListView.AuroraDeleteItemListener;
import aurora.widget.AuroraMenuBase;

import com.android.auroramusic.adapter.AuroraMusicListAdapter;
import com.android.auroramusic.adapter.AuroraSearchAdapter;
import com.android.auroramusic.downloadex.DownloadManager;
import com.android.auroramusic.model.SearchItem;
import com.android.auroramusic.ui.album.AlbumDetailActivity;
import com.android.auroramusic.ui.album.AlbumListActivity;
import com.android.auroramusic.util.AuroraListItem;
import com.android.auroramusic.util.AuroraMainMenuData;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.DialogUtil;
import com.android.auroramusic.util.DialogUtil.OnAddPlaylistSuccessListener;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.LogUtil;
import com.android.auroramusic.widget.SideBar;
import com.android.auroramusic.widget.SideBar.OnTouchingLetterChangedListener;
import com.android.music.MediaPlaybackService;
import com.android.music.MusicUtils;
import com.android.music.R;
import com.nostra13.universalimageloader.core.ImageLoader;

public class AuroraTrackBrowserActivity extends AbstractBaseActivity implements AuroraDeleteItemListener, OnSearchViewQuitListener, DialogUtil.OnDeleteFileListener {
	private static final int MSG_BTNPLAY_CLICK = 100001;
	private static final int MSG_SHOW_ANIMATION = 100003;
	private static final int MSG_GET_PLAYLIST = 100008;
	private static final int MSG_CHANGE_ADAPTER = 100009;

	private static final int MSG_UPDATE_LISTVIEW = 100010;// lory add 2014.10.25

	private volatile int fromShuffl = 0;
	public static final String FROM_CODE = "fromCode";
	public boolean fromAdd = false;
	private TextView tv_menu;
	private static String[] mCursorCols;
	private SideBar sideBar;
	private TextView tv_sidebar;
	private TextView dialog;
	private boolean inSearch = false;
	private TextView btn_menuCanel;
	private TextView btn_menuAll;
	private View actionBar_play;
	private boolean isPlaying = false; // 动画是否在运行
	private Animation operatingAnim;

	private ArrayList<String> mAddList = null;// add by chenhl
	private AuroraMainMenuData playlistInfo;// add by chenhl 20140523
	private int startMode = 0; // 0为默认歌曲模式 1为文件夹进入模式 add by chenhl 20140527
	private String mFoldPath; // add by chenhl 20140527
	private long addArtistId = -1;
	private ArrayList<AuroraListItem> mArrayList;
	private AuroraListView mTrackList;
	private AuroraMusicListAdapter mAdapter;
	private ArrayList<AuroraListItem> mList;
	private boolean needUpdate;
	private boolean needShowSeleted = true;
	public int mSongNumber = 0;
	private ArrayList<String> mSearchAddList = new ArrayList<String>();
	private AuroraActionBar mAuroraActionBar = null;

	private AuroraSearchAdapter mSearchAdapter;
	private View mHeaderView;
	private View mHeaderHideView;
	private ArrayList<SearchItem> mSearchlist = new ArrayList<SearchItem>();
	private ArrayList<SearchItem> artistlist = new ArrayList<SearchItem>();
	private ArrayList<SearchItem> tracklist = new ArrayList<SearchItem>();
	private AuroraListView mSearchListView;
	private ImageView mPlaySelect;
	ObjectAnimator aima;
	private ColorStateList colorlist;
	private static final String TAG = "AuroraTrackBrowserActivity";
	private TextView iv_randPlay;
	private TextView tv_songCount;
	private View mHidHeaderView;
	private static final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	private String keyWord;
	private static WindowManager wm;
	private static WindowManager.LayoutParams params;
	private static WindowManager.LayoutParams params2;
	private ImageView btn_floatView;
	private int[] flyendPoint = new int[2];
	private int[] flystartPoint;
	private int alpha;
	private boolean isStop;
	private AnimationSet set;
	private AuroraAlertDialog mAuroraAlertDialog;
	private List<String> mPathsXml = null;// new ArrayList<String>();

	private int mWidth = 0;// 2014.10.31 lroy add for cache pic size
	private int mHight = 0;// 2014.10.31 lroy add for cache pic size

	private boolean mFinished;

	private class AuroraOnItemlongClickListener implements OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			goEditMode(position);
			mAdapter.notifyDataSetChanged();
			changeMenuState();
			return false;
		}

	}

	private AuroraOnItemlongClickListener mOnItemlongClickListener;
	private DialogUtil.OnAddPlaylistSuccessListener mAddPlaylistSuccessListener = new OnAddPlaylistSuccessListener() {

		@Override
		public void OnAddPlaylistSuccess() {
			// TODO Auto-generated method stub
			exitEidtMode();
		}
	};

	class AuroraItemClickListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
			if (fromShuffl > 0) {
				return;
			}
			if (position == 0)
				return;
			if (fromAdd)
				tv_menu.setText(getString(R.string.aurora_add_playlist_song_num, mAdapter.getCheckedCount()));
			if (!mAdapter.getEidtMode())
				startPlayAnimation(position, true);
		}

	}

	private AuroraItemClickListener mAuroraItemClickListener;

	class SortTask extends AsyncTask<Cursor, String, Boolean> {
		private long time;

		@Override
		protected Boolean doInBackground(Cursor... params) {
			// TODO Auto-generated method stub
			needUpdate = true;
			StringBuilder where = new StringBuilder();

			// add by chenhl start 20140527
			if (startMode == 1) {
				where.append(Globals.QUERY_SONG_FILTER_1);
				where.append(" and " + MediaStore.Audio.Media.DATA + " like \"" + mFoldPath + "%\"");
			} else if (addArtistId != -1) {
				where.append(Globals.QUERY_SONG_FILTER);
				where.append(" and " + MediaStore.Audio.Media.ARTIST_ID + "=" + addArtistId);
			} else {
				where.append(Globals.QUERY_SONG_FILTER);
			}
			// add by chenhl end 20140527
			Cursor cursor = AuroraTrackBrowserActivity.this.getContentResolver().query(uri, mCursorCols, where.toString(), null, null);
			initAdapter(cursor);
			return needUpdate;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			if (needUpdate && mAdapter != null) {
				mArrayList.clear();
				mArrayList.addAll(mList);
				if (mTrackList.auroraIsRubbishOut()) {
					mTrackList.auroraSetRubbishBack();
				}
				mAdapter.notifyDataSetChanged();
				if (MusicUtils.sService != null && needShowSeleted && !fromAdd) {
					try {
						long id = MusicUtils.getCurrentAudioId();
						for (int i = 0; i < mArrayList.size(); i++) {
							if (mArrayList.get(i).getSongId() == id) {
								mAdapter.setCurrentPosition(i);
								needShowSeleted = false;
								final int j = i;
								mAuroraHandler.post(new Runnable() {

									@Override
									public void run() {
										// TODO Auto-generated method stub
										if (j == 0)
											mTrackList.setSelectionFromTop(j + 1, 0);
										else
											mTrackList.setSelectionFromTop(j + 1, getResources().getDimensionPixelSize(R.dimen.aurora_playmode_height));
									}
								});

								break;
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			if (fromAdd)
				getSeletedItems();
			if (mArrayList.size() == 0) {
				showNavtitle(true);
				btn_menuAll.setEnabled(false);
				btn_menuAll.setTextColor(getResources().getColor(R.color.aurora_select_disable));
			} else {
				showNavtitle(false);
				btn_menuAll.setEnabled(true);
				if (colorlist != null)
					btn_menuAll.setTextColor(colorlist);
				sideBar.setVisibility(View.VISIBLE);
			}
			setPlayAnimation();
			super.onPostExecute(result);
		}
	}

	// 接收歌曲数据加载完毕或删除数据消息，更新UI
	static class AuroraHandler extends Handler {
		WeakReference<AuroraTrackBrowserActivity> mActivity;

		public AuroraHandler(AuroraTrackBrowserActivity activity) {
			mActivity = new WeakReference<AuroraTrackBrowserActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MSG_SHOW_ANIMATION:
				mActivity.get().setPlayAnimation();
				break;
			case MSG_CHANGE_ADAPTER:
				mActivity.get().mTrackList.setAdapter(mActivity.get().mAdapter);
			case MSG_GET_PLAYLIST:
				break;

			case MSG_UPDATE_LISTVIEW:
				mActivity.get().updateScrollListView();
				break;
			}

			super.handleMessage(msg);
		}
	}

	// lory add 2014.10.25 for scroll frame/per
	private void updateScrollListView() {
		if (mTrackList == null || mHidHeaderView == null) {
			return;
		}

		if (mTrackList.getFirstVisiblePosition() == 1)
			setTagState(mTrackList.getFirstVisiblePosition());
		else
			setTagState(mTrackList.getFirstVisiblePosition() + 1);

		if (mTrackList.getFirstVisiblePosition() >= 1) {
			mHidHeaderView.setVisibility(View.VISIBLE);
			mTrackList.auroraSetHeaderViewYOffset((int) getResources().getDimension(R.dimen.aurora_playmode_height));
		} else {
			mHidHeaderView.setVisibility(View.GONE);
			mTrackList.auroraSetHeaderViewYOffset(-1);
		}

		updataSongCount(mArrayList.size());
		setRubbishBack();

		return;
	}

	private AuroraHandler mAuroraHandler;

	private SortTask mSortTask;

	// 搜索输入框数据变化接口
	class SongSearchViewQueryTextChangeListener implements OnSearchViewQueryTextChangeListener {
		public boolean onQueryTextSubmit(String query) {
			return false;

		}

		public boolean onQueryTextChange(String newText) {
			if (TextUtils.isEmpty(newText.trim())) {
				InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				manager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
				if (getSearchView().getQueryTextView() != null)
					getSearchView().getQueryTextView().requestFocus();
			}
			keyWord = newText;
			new GetSearchTask().execute(newText);
			return false;
		}
	}

	private AuroraActionBar.OnAuroraActionBarItemClickListener mOnActionBarListener = new OnAuroraActionBarItemClickListener() {
		@Override
		public void onAuroraActionBarItemClicked(int itemid) {
			Intent intent = new Intent(AuroraTrackBrowserActivity.this, AuroraPlayerActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
		}
	};

	private AuroraMenuBase.OnAuroraMenuItemClickListener auroraMenuCallBack = new AuroraMenuBase.OnAuroraMenuItemClickListener() {
		@Override
		public void auroraMenuItemClick(int itemId) {
			if (fromAdd) {
				// modify by chenhl start 20140523
				if (playlistInfo.getPlaylistId() < 0) {
					Log.e(TAG, "error PlaylistId!!!!!");
					return;
				}
				MusicUtils.addToPlaylist(AuroraTrackBrowserActivity.this, mAddList, playlistInfo.getPlaylistId(), playlistInfo.getName());
				gobackToSongSingle();
				// modify by chenhl end 20140523
			} else {
				switch (itemId) {
				case R.id.song_backup:
					deleteSongs();
					break;
				case R.id.song_add:
					ArrayList<String> list = new ArrayList<String>();
					for (int i = 0; i < mAdapter.getCount(); i++) {
						if (mAdapter.isItemChecked(i)) {
							list.add(String.valueOf(((AuroraListItem) mAdapter.getItem(i)).getSongId()));
						}
					}
					if (mSearchAddList != null && mSearchAddList.size() > 0) {
						list.addAll(mSearchAddList);
					}
					DialogUtil.showAddDialog(AuroraTrackBrowserActivity.this, list, mAddPlaylistSuccessListener);
					break;
				default:
					break;
				}
			}
		}
	};
	// 屏蔽标题返回功能
	private OnAuroraActionBarBackItemClickListener mOnActionBarBackItemListener = new OnAuroraActionBarBackItemClickListener() {
		@Override
		public void onAuroraActionBarBackItemClicked(int itemid) {
			if (fromAdd || !mTrackList.auroraIsRubbishOut()) {
				finishWithDataResult();// add by chenhl 20140522
				// finish();
			} else if (mTrackList.auroraIsRubbishOut()) {
				mTrackList.auroraSetRubbishBack();
			}
		}
	};

	@Override
	public void auroraDeleteItem(View arg0, final int position) {
		// TODO Auto-generated method stub
		final AuroraListItem item = mArrayList.get(position - 1);
		long id = item.getSongId();
		mAdapter.deleteItem(position - 1);
		if (position - 1 < mAdapter.getCurrentPosition()) {
			mAdapter.setCurrentPosition(mAdapter.getCurrentPosition() - 1);
		}
		if (mArrayList.size() == 0) {
			actionBar_play.clearAnimation();
			actionBar_play.setBackgroundResource(R.drawable.aurora_left_bar_clicked);
			showNavtitle(true);
		} else {
			showNavtitle(false);
		}
		mSongNumber = mArrayList.size();
		final long[] list = new long[1];
		list[0] = id;
		mAuroraHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				DownloadManager.getInstance(AuroraTrackBrowserActivity.this).removeDownloadByPath(item.getFilePath());
				MusicUtils.deleteTracks(AuroraTrackBrowserActivity.this, list);
			}
		}, 100);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (intent != null) {
			if (intent.getIntExtra(FROM_CODE, -1) == 1) {
				fromAdd = true;
				// add by chenhl 20140523
				playlistInfo = (AuroraMainMenuData) intent.getSerializableExtra(AuroraSongSingle.EXTR_PLAYLIST_INFO);
				mAddList = intent.getStringArrayListExtra(AuroraNewPlayListActivity.EXTR_ADDLIST_DATA);
			} else if (intent.getLongExtra("atristid", -1) != -1) {
				addArtistId = intent.getLongExtra("atristid", -1);
				fromAdd = true;
				playlistInfo = (AuroraMainMenuData) intent.getSerializableExtra(AuroraSongSingle.EXTR_PLAYLIST_INFO);
				mAddList = intent.getStringArrayListExtra(AuroraNewPlayListActivity.EXTR_ADDLIST_DATA);
			}

			startMode = intent.getIntExtra(AuroraFoldActivity.EXTR_FOLD_START_MODE, 0);
			if (startMode == 1) {
				mFoldPath = intent.getStringExtra(AuroraFoldActivity.EXTR_FOLD_PATH);
			}

		}
		if (fromAdd) {
			setAuroraContentView(R.layout.aurora_song_activity, AuroraActionBar.Type.Dashboard, true);
		} else {
			setAuroraContentView(R.layout.aurora_song_activity, AuroraActionBar.Type.Normal, true);
		}
		
		mWidth = (int) getResources().getDimension(R.dimen.aurora_albumIcon_size);
		mHight = mWidth;

		registerStateChangeReceiver();
		initView();
		initData();
	}

	private void initData() {
		mPathsXml = AuroraMusicUtil.doParseXml(this, "paths.xml");
		mCursorCols = new String[] { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM,
				MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.DURATION };
		mSortTask = new SortTask();
		mArrayList = new ArrayList<AuroraListItem>();
		mList = new ArrayList<AuroraListItem>();
		mAdapter = new AuroraMusicListAdapter(this, mArrayList);
		mTrackList.setAdapter(mAdapter);
		mSearchAdapter = new AuroraSearchAdapter(this, mSearchlist);
		mSearchListView.setAdapter(mSearchAdapter);
		mSortTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void initAdapter(Cursor cursor) {
		if (cursor == null) {
			return;
		}
		if (cursor.getCount() == mArrayList.size()) {
			needUpdate = false;
			return;
		}
		mList.clear();
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			int mId = cursor.getInt(0);
			String mTitle = "<unknown>";
			if (cursor.getString(2) != null && !TextUtils.isEmpty(cursor.getString(2)))
				mTitle = cursor.getString(2);
			String mPath = "<unknown>";
			if (cursor.getString(3) != null && !TextUtils.isEmpty(cursor.getString(3)))
				mPath = cursor.getString(3);
			String mAlbumName = "<unknown>";
			if (cursor.getString(4) != null && !TextUtils.isEmpty(cursor.getString(4)))
				mAlbumName = cursor.getString(4);
			String mArtistName = "<unknown>";
			if (cursor.getString(5) != null && !TextUtils.isEmpty(cursor.getString(5)))
				mArtistName = cursor.getString(5);
			int mduration = cursor.getInt(7);
			String mUri = mPath;
			String imgUri = AuroraMusicUtil.getImgPath(getApplication(), AuroraMusicUtil.MD5(mTitle + mArtistName + mAlbumName));
			long mAlbumId = cursor.getLong(1);
			String mPinyin = MusicUtils.getSpell(mTitle);
			AuroraListItem listItem = new AuroraListItem((long) mId, mTitle, mUri, mAlbumName, mAlbumId, mArtistName, 0, imgUri, null, null, -1);
			listItem.setDuration(mduration);
			listItem.setPinyin(mPinyin);
			listItem.setArtistId(cursor.getLong(6));

			listItem.setItemPicSize(mWidth, mWidth);// 2014.10.31 lroy add for
													// cache pic size

			// modify by chenhl start 20140527
			mPath = mPath.substring(0, mPath.lastIndexOf("/"));
			if (startMode == 1) {
				// 确保取正确路径
				if (mPath.equals(mFoldPath)) {
					mList.add(listItem);
				}
			} else {
				if (!mPathsXml.contains(mPath)) {
					mList.add(listItem);
				}
			}
			// modify by chenhl end 20140527
		}
		if (cursor != null) {
			cursor.close();
			cursor = null;
		}
		if (mList.size() == 0)
			return;
		Collections.sort(mList, new Comparator<AuroraListItem>() {

			@Override
			public int compare(AuroraListItem lhs, AuroraListItem rhs) {
				// TODO Auto-generated method stub
				if (lhs.getPinyin().charAt(0) == rhs.getPinyin().charAt(0) && (65 > lhs.getTitle().toUpperCase().charAt(0) || lhs.getTitle().toUpperCase().charAt(0) > 90)
						&& (rhs.getTitle().toUpperCase().charAt(0) <= 90 && rhs.getTitle().toUpperCase().charAt(0) >= 65)) {
					return 1;

				} else if (lhs.getPinyin().charAt(0) == rhs.getPinyin().charAt(0) && (65 > rhs.getTitle().toUpperCase().charAt(0) || rhs.getTitle().toUpperCase().charAt(0) > 90)
						&& (lhs.getTitle().toUpperCase().charAt(0) <= 90 && lhs.getTitle().toUpperCase().charAt(0) >= 65)) {
					return -1;
				}
				return lhs.getPinyin().compareTo(rhs.getPinyin());
			}
		});
	}

	private void initView() {
		setAuroraMenuCallBack(auroraMenuCallBack);
		setOnQueryTextListener(new SongSearchViewQueryTextChangeListener());
		try {
			mAuroraActionBar = getAuroraActionBar();// 获取actionbar
			if (mAuroraActionBar == null) {
				return;
			}

			if (mAuroraActionBar.getVisibility() != View.VISIBLE) {
				((View) mAuroraActionBar).setVisibility(View.VISIBLE);
			}
			// modify by chenhl start 20140527
			if (startMode == 1) {
				String titleStr = mFoldPath.substring(mFoldPath.lastIndexOf("/") + 1);
				mAuroraActionBar.setTitle(titleStr);
			} else {
				mAuroraActionBar.setTitle(R.string.appwidget_songtitle);
			}
			// modify by chenhl end 20140527
			View tView = mAuroraActionBar.getHomeButton();
			if (tView != null) {
				tView.setVisibility(View.VISIBLE);
			}
			mAuroraItemClickListener = new AuroraItemClickListener();
			mOnItemlongClickListener = new AuroraOnItemlongClickListener();

		} catch (Exception e) {
			e.printStackTrace();
		}
		mAuroraActionBar.setmOnActionBarBackItemListener(mOnActionBarBackItemListener);
		sideBar = (SideBar) findViewById(R.id.sidebar);
		tv_sidebar = (TextView) findViewById(R.id.tv_sidebar);
		dialog = (TextView) findViewById(R.id.dialog);
		sideBar.setTextView(dialog);

		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				int position = mAdapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					if (position == 0)
						mTrackList.setSelectionFromTop(position + 1, 0);
					else
						mTrackList.setSelectionFromTop(position + 1, getResources().getDimensionPixelSize(R.dimen.aurora_playmode_height));
				}

			}
		});
		setOnSearchViewQuitListener(this);
		mTrackList = (AuroraListView) findViewById(R.id.song_list);
		mTrackList.setSelector(R.drawable.aurora_playlist_item_clicked);
		mPlaySelect = (ImageView) findViewById(R.id.aurora_song_selected);
		mSearchListView = (AuroraListView) findViewById(R.id.aurora_search_list);
		mSearchListView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				mSearchListView.requestFocus();
				return false;
			}
		});
		mTrackList.setOnItemLongClickListener(mOnItemlongClickListener);
		mTrackList.setOnItemClickListener(mAuroraItemClickListener);
		mTrackList.setCacheColorHint(0);
		// AuroraListView 删除监听器接口
		mTrackList.auroraSetDeleteItemListener(this);
		// 开启左滑删除功能
		mTrackList.auroraSetNeedSlideDelete(true);
		mTrackList.auroraSetAuroraBackOnClickListener(new AuroraListView.AuroraBackOnClickListener() {
			@Override
			public void auroraOnClick(int position) {
				deleteItem(position);
			}

			@Override
			public void auroraPrepareDraged(int position) {
				sideBar.setVisibility(View.GONE);
				tv_sidebar.setVisibility(View.GONE);
			}

			@Override
			public void auroraDragedSuccess(int position) {
				if (!mTrackList.auroraIsRubbishOut()) {
					sideBar.setVisibility(View.VISIBLE);
					tv_sidebar.setVisibility(View.VISIBLE);
				} else {
					sideBar.setVisibility(View.GONE);
					tv_sidebar.setVisibility(View.GONE);
				}
			}

			@Override
			public void auroraDragedUnSuccess(int position) {
				if (!mTrackList.auroraIsRubbishOut()) {
					sideBar.setVisibility(View.VISIBLE);
					tv_sidebar.setVisibility(View.VISIBLE);
				} else {
					sideBar.setVisibility(View.GONE);
					tv_sidebar.setVisibility(View.GONE);
				}
			}
		});
		
		mHeaderHideView = findViewById(R.id.song_addhide_search);
		mHeaderView = LayoutInflater.from(this).inflate(R.layout.layout_search, null);
		mHeaderView.findViewById(R.id.et_search).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				inSearch = true;
				if (mAuroraActionBar.getAuroraActionBottomBarMenu().isShowing()) {
					mAuroraActionBar.setShowBottomBarMenu(false);
					mAuroraActionBar.showActionBottomeBarMenu();
				}
				showSearchviewLayout();
				try {
					final Button btn_search = getSearchViewRightButton();
					if (mAdapter.getEidtMode()) {
						btn_search.setText(getResources().getString(R.string.menu_continue));
					} else {
						btn_search.setText(getResources().getString(R.string.songlist_cancel));
					}
					btn_search.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							inSearch = false;
							hideSearchviewLayout();
							changeMenuState();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		mTrackList.addHeaderView(mHeaderView);
		mAuroraHandler = new AuroraHandler(this);
		if (fromAdd) {
			btn_menuCanel = mAuroraActionBar.getCancelButton();
			btn_menuCanel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (fromAdd) {
						finishWithDataResult(); 
					} else
						exitEidtMode();
				}
			});

			btn_menuAll = mAuroraActionBar.getOkButton();
			colorlist = btn_menuAll.getTextColors();
			btn_menuAll.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (((TextView) btn_menuAll).getText().equals(getResources().getString(R.string.selectAll))) {
						((TextView) btn_menuAll).setText(getResources().getString(R.string.selectAllNot));
						selectAll();
						changeMenuState(2, -1);// modify by chenhl 20140604
						mAdapter.notifyDataSetChanged();
					} else {
						((TextView) btn_menuAll).setText(getResources().getString(R.string.selectAll));
						selectAllNot();
						changeMenuState(3, -1);// modify by chenhl 20140604
						mAdapter.notifyDataSetChanged();
					}
				}
			});
			mAuroraActionBar.initActionBottomBarMenu(R.menu.menu_songaddedit, 1);
			tv_menu = mAuroraActionBar.getAuroraActionBottomBarMenu().getTitleViewByPosition(0);
			((TextView) btn_menuCanel).setText(getResources().getString(R.string.menu_back));
			((TextView) mHeaderView.findViewById(R.id.search_hint)).setText(getResources().getString(R.string.search_hint_input3));
			if (mAddList.size() == 0) {
				mAuroraActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(1, false);
			} else {
				mAuroraActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(1, true);
			}
			tv_menu.setText(getString(R.string.aurora_add_playlist_song_num, mAddList.size()));// modify
																								// by
																								// chenhl
																								// 20140604
		} else {
			btn_menuCanel = (TextView) mAuroraActionBar.getSelectLeftButton();
			btn_menuCanel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (fromAdd) {
						finishWithDataResult(); // add by chenhl20140522
						// finish();
					} else
						exitEidtMode();
				}
			});

			btn_menuAll = (TextView) mAuroraActionBar.getSelectRightButton();
			btn_menuAll.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (((TextView) btn_menuAll).getText().equals(getResources().getString(R.string.selectAll))) {
						((TextView) btn_menuAll).setText(getResources().getString(R.string.selectAllNot));
						selectAll();
						changeMenuState(2, -1);// modify by chenhl 20140604
						mAdapter.notifyDataSetChanged();
					} else {
						((TextView) btn_menuAll).setText(getResources().getString(R.string.selectAll));
						selectAllNot();
						changeMenuState(3, -1);// modify by chenhl 20140604
						mAdapter.notifyDataSetChanged();
					}
				}
			});
			mAuroraActionBar.initActionBottomBarMenu(R.menu.menu_songedit, 2);
			tv_menu = mAuroraActionBar.getAuroraActionBottomBarMenu().getTitleViewByPosition(1);
			mAuroraActionBar.addItem(R.drawable.song_playing, MSG_BTNPLAY_CLICK, "");
			actionBar_play = mAuroraActionBar.getItem(0).getItemView();
			operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_anim);
			LinearInterpolator lin = new LinearInterpolator();
			operatingAnim.setInterpolator(lin);
			mAuroraActionBar.setOnAuroraActionBarListener(mOnActionBarListener);
		}
		mHidHeaderView = findViewById(R.id.song_hide_header);
		iv_randPlay = (TextView) findViewById(R.id.tv_playmode);
		iv_randPlay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				shufflePlay();
			}
		});
		tv_songCount = (TextView) findViewById(R.id.tv_songnumber);
		tv_songCount.setOnClickListener(null);
		mAuroraAlertDialog = new AuroraAlertDialog.Builder(this).setTitle(R.string.delete)
		// .setIcon(android.R.drawable.ic_dialog_info)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						final int count = mAdapter.getCheckedCount();
						final long[] list = new long[count];
						int index = 0;
						for (int i = 0; i < mArrayList.size(); i++) {
							if (mAdapter.isItemChecked(i)) {
								list[index] = mArrayList.get(i).getSongId();
								index++;
							}
						}
						MusicUtils.deleteMediaTracks(AuroraTrackBrowserActivity.this, list, AuroraTrackBrowserActivity.this, mArrayList);
					}
				}).setNegativeButton(R.string.cancel, null).create();
	}

	// 显示删除动画
	public void showDeleteAnimation() {
		mAdapter.hasDeleted = true;
		mTrackList.auroraDeleteSelectedItemAnim();
		if (mArrayList.size() > 0) {
			sideBar.setVisibility(View.VISIBLE);
		}
		tv_sidebar.setVisibility(View.VISIBLE);
	}

	// 设置分组栏目的状态
	public void setTagState(int position) {
		if (mAdapter == null)
			return;
		if (mArrayList.size() > 0) {
			String pinyin;
			try {
				if (position - 1 >= 0)
					pinyin = mArrayList.get(position - 1).getPinyin();
				else
					pinyin = mArrayList.get(position).getPinyin();
				sideBar.setCurChooseTitle(String.valueOf(pinyin.charAt(0)));
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	// 退出编辑状态，显示退出动画
	public void exitEidtMode() {
		if (mSearchAddList != null) {
			mSearchAddList.clear();
		}
		iv_randPlay.setEnabled(true);
		mAdapter.changeEidtMode(false);
		mAdapter.setNeedout();
		if (!mAuroraActionBar.auroraIsExitEditModeAnimRunning()) {
			mAuroraActionBar.setShowBottomBarMenu(false);
			mAuroraActionBar.showActionBarDashBoard();
		}
		((TextView) btn_menuAll).setText(getResources().getString(R.string.selectAll));
		mAuroraHandler.sendEmptyMessageDelayed(MSG_SHOW_ANIMATION, 500);
		mTrackList.auroraSetNeedSlideDelete(true);
		mAdapter.notifyDataSetChanged();
	}

	// 动态更改actionbar 显示全选/反选
	public void changeMenuState() {
		if (mAdapter.getCheckedCount() == mAdapter.getCount() && mAdapter.getCheckedCount() > 0) {
			((TextView) btn_menuAll).setText(getResources().getString(R.string.selectAllNot));
		} else {
			((TextView) btn_menuAll).setText(getResources().getString(R.string.selectAll));
		}
		if (fromAdd) {
			if (mAddList.size() == 0) {
				mAuroraActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(1, false);
			} else {
				mAuroraActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(1, true);
			}
			tv_menu.setText(getString(R.string.aurora_add_playlist_song_num, mAddList.size()));// modify
																								// by
																								// chenhl
																								// 20140604
		} else {
			if (mAdapter.getCheckedCount() == 0 && (mSearchAddList == null || mSearchAddList.size() == 0)) {
				mAuroraActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(1, false);
				mAuroraActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(2, false);
			} else {
				mAuroraActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(1, true);
				mAuroraActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(2, true);
			}
		}
	}

	// 动态更改actionbar 显示全选/反选
	public void changeMenuState(int state, long songId) {
		// add by chenhl start 20140604
		if (fromAdd) {
			if (state == 1) {
				if (!mAddList.contains(String.valueOf(songId)))
					mAddList.add(String.valueOf(songId));
			} else if (state == 0) {
				mAddList.remove(String.valueOf(songId));
			} else if (state == 2) {
				// 全选
				for (int i = 0; i < mArrayList.size(); i++) {
					if (!mAddList.contains(String.valueOf(mArrayList.get(i).getSongId())))
						mAddList.add(String.valueOf(mArrayList.get(i).getSongId()));
				}
			} else if (state == 3) {
				// 反选
				for (int i = 0; i < mArrayList.size(); i++) {
					mAddList.remove(String.valueOf(mArrayList.get(i).getSongId()));
				}
			}
		}
		// add by chenhl end
		changeMenuState();
	}

	// 删除选中的所有歌曲
	public void deleteSongs() {
		mFinished = false;
		if (mAuroraAlertDialog != null && !mAuroraAlertDialog.isShowing()) {
			mAuroraAlertDialog.setMessage(getResources().getString(R.string.deleteMessage, mAdapter.getCheckedCount()));
			mAuroraAlertDialog.show();
		}
	}

	/**
	 * 设置播放动画
	 */
	private void setPlayAnimation() {
		if (actionBar_play == null || operatingAnim == null) {
			return;
		}
		try {
			if (MusicUtils.sService != null) {
				LogUtil.d(TAG, "isplaying:" + MusicUtils.sService.isPlaying());
				if (MusicUtils.sService.isPlaying() && mAdapter.getEidtMode() == false) {
					if (!isPlaying) {
						actionBar_play.startAnimation(operatingAnim);
						actionBar_play.setBackgroundResource(android.R.color.transparent);
						isPlaying = true;
					}
				} else if (isPlaying) {
					actionBar_play.clearAnimation();
					actionBar_play.setBackgroundResource(R.drawable.aurora_left_bar_clicked);
					isPlaying = false;
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			if (isPlaying) {
				actionBar_play.clearAnimation();
				actionBar_play.setBackgroundResource(R.drawable.aurora_left_bar_clicked);
				isPlaying = false;
			}
		}
	}

	// 监听播放状态的变化
	private BroadcastReceiver mStatusListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(MediaPlaybackService.PLAYSTATE_CHANGED)) {
				setPlayAnimation();
			} else if (action.equals(MediaPlaybackService.META_CHANGED)) {
				try {
					if (MusicUtils.sService != null) {
						if (MusicUtils.sService.getAudioId() == -1) {
							if (fromShuffl > 0) {
								fromShuffl--;
							}
							return;
						}
						for (int i = 0; i < mArrayList.size(); i++) {
							if ((mArrayList.get(i).getSongId()) == MusicUtils.sService.getAudioId()) {
								final int j = i;
								if (fromShuffl > 0) {
									mAdapter.setCurrentPosition(i);
									mAdapter.notifyDataSetChanged();
									if (j == 0) {
										mTrackList.setSelectionFromTop(j + 1, 0);
									} else {
										mTrackList.setSelectionFromTop(j + 1, getResources().getDimensionPixelSize(R.dimen.aurora_playmode_height));
									}
									fromShuffl--;
								} else if (mArrayList.get(mAdapter.getCurrentPosition()).getSongId() != mArrayList.get(i).getSongId() && fromShuffl == 0 && mFinished) {
									startPlayAnimation(i + 1, false);
								} else {
									mAdapter.setCurrentPosition(i);
									mAdapter.notifyDataSetChanged();
								}
								break;
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		}
	};

	// 注册监听播放器状态更改的广播
	private void registerStateChangeReceiver() {
		IntentFilter f = new IntentFilter();
		f.addAction(MediaPlaybackService.PLAYSTATE_CHANGED);
		f.addAction(MediaPlaybackService.META_CHANGED);
		registerReceiver(mStatusListener, new IntentFilter(f));
	}

	private void goEditMode(int position) {
		iv_randPlay.setEnabled(false);
		if (isPlaying && actionBar_play != null) {
			actionBar_play.clearAnimation();
			actionBar_play.setBackgroundResource(R.drawable.aurora_left_bar_clicked);
			isPlaying = false;
		}
		if (mAdapter.getEidtMode() == false) {
			if (!mAuroraActionBar.auroraIsEntryEditModeAnimRunning()) {
				mAuroraActionBar.setShowBottomBarMenu(true);
				if (position != -1)
					mAuroraActionBar.showActionBarDashBoard();
				else
					mAuroraActionBar.showActionBottomeBarMenu();
			}
			selectAllNot();
			if (!fromAdd)
				mAdapter.setNeedin();
			if (position != -1)
				mAdapter.setItemChecked(position - 1, true);
			mTrackList.auroraSetNeedSlideDelete(false);
			mAdapter.changeEidtMode(true);
		}
		if (fromAdd) {
			changeMenuState();
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK && (inSearch)) {
			inSearch = false;
			hideSearchviewLayout();
			changeMenuState();
			return true;
		} else if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN && fromAdd) {
			finishWithDataResult();// add by chenhl 20140523
		} else if (mAuroraActionBar.auroraIsExitEditModeAnimRunning() || mAuroraActionBar.auroraIsEntryEditModeAnimRunning()) {
			return true;
		} else if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK && (mAdapter.getEidtMode()) && !fromAdd) {
			exitEidtMode();
			return true;
		} else if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK && mTrackList.auroraIsRubbishOut()) {
			mTrackList.auroraSetRubbishBack();
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mAuroraHandler.removeCallbacksAndMessages(null);
		mTrackList.setAdapter(null);
		mAdapter = null;
		mSortTask.cancel(true);
		unregisterReceiverSafe(mStatusListener);
	}

	private void unregisterReceiverSafe(BroadcastReceiver receiver) {
		try {
			unregisterReceiver(receiver);
		} catch (IllegalArgumentException e) {
			// ignore
		}
	}

	// 删除选中的歌曲
	private void deleteItem(int position) {
		new AuroraAlertDialog.Builder(this).setTitle(R.string.delete)
		// .setIcon(android.R.drawable.ic_dialog_info)
				.setMessage(getString(R.string.deleteMessage, 1)).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						showDeleteAnimation();
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mTrackList.auroraSetRubbishBack();
					}
				}).show();
	}

	// add by chenhl start 20140522
	private void finishWithDataResult() {
		Intent tIntent = new Intent();
		tIntent.putExtra(AuroraNewPlayListActivity.EXTR_RESULT_DATA, mAddList);
		tIntent.putExtra(AuroraNewPlayListActivity.EXTR_RESULT_MODE, 0);
		setResult(RESULT_OK, tIntent);
		finish();
	}

	/**
	 * 回到歌单页面
	 */
	private void gobackToSongSingle() {

		Intent intent = new Intent(this, AuroraSongSingle.class);
		intent.putExtra(AuroraSongSingle.EXTR_PLAYLIST_INFO, playlistInfo);
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
		AuroraMusicActivityManiger.getInstance().exit();
	}

	// add by chenhl end
	class GetSearchTask extends AsyncTask<String, String, String> {
		private boolean flag = false;

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			if (params[0] == null || TextUtils.isEmpty(params[0].trim())) {
				flag = true;
				return null;
			}
			tracklist.clear();
			artistlist.clear();
			String filter = params[0];
			if (filter == null) {
				filter = "";
			}

			if (mPathsXml == null) {
				mPathsXml = AuroraMusicUtil.doParseXml(AuroraTrackBrowserActivity.this, "paths.xml");
			}

			if (!fromAdd) {
				Cursor cursor = doSearchArtist(filter);
				if (cursor != null) {
					for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

						String path = cursor.getString(4);
						path = path.substring(0, path.lastIndexOf("/"));
						if (mPathsXml.contains(path)) {
							continue;
						}

						SearchItem searchItem = new SearchItem(cursor.getLong(0), null, "", "", "", "", cursor.getString(1), "artist");
						searchItem.mSimilarity = AuroraMusicUtil.getSimilarity(searchItem.getArtistName().toUpperCase(), filter.toUpperCase());
						searchItem.mAlbumCount = cursor.getInt(2);
						searchItem.mSongCount = cursor.getInt(3);
						searchItem.mPinYin = MusicUtils.getSpell(searchItem.getArtistName());
						artistlist.add(searchItem);
					}
					cursor.close();
				}
			}
			Cursor cursor2 = doSearchTrack(filter);
			if (cursor2 != null) {
				for (cursor2.moveToFirst(); !cursor2.isAfterLast(); cursor2.moveToNext()) {
					String path = cursor2.getString(4);
					path = path.substring(0, path.lastIndexOf("/"));
					if (mPathsXml.contains(path)) {
						continue;
					}
					String imgUri = AuroraMusicUtil.getImgPath(getApplication(), AuroraMusicUtil.MD5(cursor2.getString(3) + cursor2.getString(1) + cursor2.getString(2)));
					SearchItem searchItem = new SearchItem(cursor2.getLong(0), null, cursor2.getString(3), cursor2.getString(4), imgUri, cursor2.getString(2), cursor2.getString(1), "");
					searchItem.mSimilarity = AuroraMusicUtil.getSimilarity(searchItem.getTitle().toUpperCase(), filter.toUpperCase());
					searchItem.mPinYin = MusicUtils.getSpell(searchItem.getTitle());
					tracklist.add(searchItem);
				}
				cursor2.close();
			}
			if (artistlist.size() > 0)
				Collections.sort(artistlist, new Comparator<SearchItem>() {

					@Override
					public int compare(SearchItem lhs, SearchItem rhs) {
						if (lhs.mSimilarity > rhs.mSimilarity) {
							return -1;
						} else if (lhs.mSimilarity < rhs.mSimilarity) {
							return 1;
						} else {
							// TODO Auto-generated method stub
							if (lhs.mPinYin.charAt(0) == rhs.mPinYin.charAt(0) && (65 > lhs.getArtistName().toUpperCase().charAt(0) || lhs.getArtistName().toUpperCase().charAt(0) > 90)
									&& (rhs.getArtistName().toUpperCase().charAt(0) <= 90 && rhs.getArtistName().toUpperCase().charAt(0) >= 65)) {
								return 1;

							} else if (lhs.mPinYin.charAt(0) == rhs.mPinYin.charAt(0) && (65 > rhs.getArtistName().toUpperCase().charAt(0) || rhs.getArtistName().toUpperCase().charAt(0) > 90)
									&& (lhs.getArtistName().toUpperCase().charAt(0) <= 90 && lhs.getArtistName().toUpperCase().charAt(0) >= 65)) {
								return -1;
							}
						}
						return lhs.mPinYin.compareTo(rhs.mPinYin);
					}
				});
			if (tracklist.size() > 0)
				Collections.sort(tracklist, new Comparator<SearchItem>() {

					@Override
					public int compare(SearchItem lhs, SearchItem rhs) {
						// TODO Auto-generated method stub
						if (lhs.mSimilarity > rhs.mSimilarity) {
							return -1;
						} else if (lhs.mSimilarity < rhs.mSimilarity) {
							return 1;
						} else {
							if (lhs.mPinYin.charAt(0) == rhs.mPinYin.charAt(0) && (65 > lhs.getTitle().toUpperCase().charAt(0) || lhs.getTitle().toUpperCase().charAt(0) > 90)
									&& (rhs.getTitle().toUpperCase().charAt(0) <= 90 && rhs.getTitle().toUpperCase().charAt(0) >= 65)) {
								return 1;

							} else if (lhs.mPinYin.charAt(0) == rhs.mPinYin.charAt(0) && (65 > rhs.getTitle().toUpperCase().charAt(0) || rhs.getTitle().toUpperCase().charAt(0) > 90)
									&& (lhs.getTitle().toUpperCase().charAt(0) <= 90 && lhs.getTitle().toUpperCase().charAt(0) >= 65)) {
								return -1;
							}
						}
						return lhs.mPinYin.compareTo(rhs.mPinYin);
					}
				});
			if (artistlist.size() > 0)
				artistlist.get(0).mTag = true;
			if (tracklist.size() > 0)
				tracklist.get(0).mTag = true;
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			synchronized (this) {
				if (mSearchAdapter != null) {
					mSearchlist.clear();
					mSearchlist.addAll(artistlist);
					mSearchlist.addAll(tracklist);
					mHeaderHideView.setVisibility(View.VISIBLE);
					mSearchListView.setVisibility(View.VISIBLE);
					sideBar.setVisibility(View.GONE);
					if (mAdapter.getEidtMode()) {
						mSearchAdapter.setEidtMode(true);
					}
					mSearchAdapter.notifyDataSetChanged();
					mSearchListView.setSelection(0);
					mSearchListView.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
							mSearchListView.requestFocus();
							// TODO Auto-generated method stub
							if (!((SearchItem) mSearchAdapter.getItem(position)).mMimeType.equals("artist")) {
								ArrayList<AuroraListItem> list = new ArrayList<AuroraListItem>();
								SearchItem item = ((SearchItem) mSearchAdapter.getItem(position));
								long songId = item.getSongId();

								AuroraListItem listItem = new AuroraListItem(songId, item.getTitle(), null, item.getAlbumName(), item.getAlbumId(), item.getArtistName(), 0, null, null, null, -1);

								listItem.setItemPicSize(mWidth, mWidth);// 2014.10.31
																		// lroy
																		// add
																		// for
																		// cache
																		// pic
																		// size
								list.add(listItem);
								MusicUtils.playAll(AuroraTrackBrowserActivity.this, list, 0, 0);
								AuroraMusicUtil.setCurrentPlaylist(AuroraTrackBrowserActivity.this, -1);// add
																										// by
																										// chenhl
																										// 20140825
								mSearchAdapter.notifyDataSetChanged();
							} else {
								int artistofalbums = ((SearchItem) mSearchAdapter.getItem(position)).mAlbumCount;
								int artistofsongs = ((SearchItem) mSearchAdapter.getItem(position)).mSongCount;

								if (artistofalbums == 1) {
									Intent intent = new Intent(AuroraTrackBrowserActivity.this, AlbumDetailActivity.class);
									intent.putExtra(Globals.KEY_ARTIST_ID, String.valueOf(((SearchItem) mSearchAdapter.getItem(position)).getSongId()));
									intent.putExtra(Globals.KEY_ARTIST_NAME, ((SearchItem) mSearchAdapter.getItem(position)).getArtistName());
									intent.putExtra("artistofalbum", String.valueOf(artistofalbums));
									intent.putExtra("artistoftrack", String.valueOf(artistofsongs));
									startActivityForResult(intent, Globals.REQUEST_CODE_BROWSER);
								} else {
									Intent intent = new Intent(AuroraTrackBrowserActivity.this, AlbumListActivity.class);
									// 可以获得歌手id 专辑数 歌总数
									intent.putExtra(Globals.KEY_ARTIST_ID, String.valueOf(((SearchItem) mSearchAdapter.getItem(position)).getSongId()));
									intent.putExtra(Globals.KEY_ARTIST_NAME, ((SearchItem) mSearchAdapter.getItem(position)).getArtistName());
									intent.putExtra("artistofalbum", String.valueOf(artistofalbums));
									intent.putExtra("artistoftrack", String.valueOf(artistofsongs));
									startActivityForResult(intent, Globals.REQUEST_CODE_BROWSER);
								}
							}
						}
					});
				}
				if (flag) {
					mSearchListView.setVisibility(View.GONE);
					mHeaderHideView.setVisibility(View.GONE);
					sideBar.setVisibility(View.VISIBLE);
				}

			}
			super.onPostExecute(result);
		}

	}

	public synchronized void shufflePlay() {
		fromShuffl++;
		LogUtil.d("TrackBrowser", "fromShuffl==" + fromShuffl);
		MusicUtils.playAll(AuroraTrackBrowserActivity.this, mArrayList, 0, 3);
		AuroraMusicUtil.setCurrentPlaylist(AuroraTrackBrowserActivity.this, -1);
		if (mTrackList.auroraIsRubbishOut()) {
			mTrackList.auroraSetRubbishBackNoAnim();
			sideBar.setVisibility(View.VISIBLE);
		}
	}

	public void showNavtitle(boolean flag) {
		if (flag) {
			mTrackList.setVisibility(View.GONE);
			sideBar.setVisibility(View.GONE);
			mHeaderHideView.setVisibility(View.GONE);
			findViewById(R.id.aurora_no_songs).setVisibility(View.VISIBLE);
		} else {
			mTrackList.setVisibility(View.VISIBLE);
			sideBar.setVisibility(View.VISIBLE);
			findViewById(R.id.aurora_no_songs).setVisibility(View.GONE);
		}
	}

	private void selectAll() {
		for (int i = 0; i < mAdapter.getCount(); i++) {
			mAdapter.setItemChecked(i, true);
		}
	}

	private void selectAllNot() {
		for (int i = 0; i < mAdapter.getCount(); i++) {
			mAdapter.setItemChecked(i, false);
		}
	}

	public void setItemChecked(long i, boolean flag) {
		boolean inAdapter = false;
		for (int index = 0; index < mArrayList.size(); index++) {
			if (mArrayList.get(index).getSongId() == i) {
				mAdapter.setItemChecked(index, flag);
				inAdapter = true;
				break;
			}
		}
		if (inAdapter)
			return;
		if (flag) {
			mSearchAddList.add(String.valueOf(i));
		} else {
			mSearchAddList.remove(String.valueOf(i));
		}
	}

	public void setArtistItemChecked(long i, boolean flag) {
		for (int index = 0; index < mArrayList.size(); index++) {
			if (mArrayList.get(index).getArtistId() == i) {
				mAdapter.setItemChecked(index, flag);
			}
		}
		getSongByArtist(i, flag);
	}

	public boolean getItemCheckedSongId(long songId) {
		if (fromAdd) {
			for (int index = 0; index < mAddList.size(); index++) {
				if (Long.valueOf(mAddList.get(index)) == songId) {
					return true;
				}
			}
		} else {
			for (int index = 0; index < mArrayList.size(); index++) {
				if (mArrayList.get(index).getSongId() == songId) {
					return mAdapter.isItemChecked(index);
				}
			}
			for (int i = 0; i < mSearchAddList.size(); i++) {
				if (Long.valueOf(mSearchAddList.get(i)) == songId) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean getArtistItemCheckedSongId(long songId, int count) {
		int number = 0;
		for (int index = 0; index < mArrayList.size(); index++) {
			if (mArrayList.get(index).getArtistId() == songId) {
				if (mAdapter.isItemChecked(index)) {
					number++;
				}
			}
		}
		if (number == count)
			return true;
		else
			return false;
	}

	@Override
	public boolean quit() {
		// TODO Auto-generated method stub
		if (mAdapter == null) {
			return false;
		}
		mSearchListView.setVisibility(View.GONE);
		sideBar.setVisibility(View.VISIBLE);
		if (mAdapter.getEidtMode()) {
			if (!mAuroraActionBar.auroraIsExitEditModeAnimRunning()) {
				mAuroraActionBar.setShowBottomBarMenu(true);
				mAuroraActionBar.showActionBottomeBarMenu();
			}
		}
		if (fromAdd) {
			if (!mAuroraActionBar.auroraIsExitEditModeAnimRunning()) {
				mAuroraActionBar.setShowBottomBarMenu(true);
				mAuroraActionBar.showActionBottomeBarMenu();
			}
		}
		inSearch = false;
		mSearchAdapter.setEidtMode(false);
		return false;
	}

	@Override
	public void onMediaDbChange(boolean selfChange) {
		// TODO Auto-generated method stub
		// if (mAdapter != null) {
		// if (mSortTask.getStatus() == Status.FINISHED) {
		// mSortTask = new SortTask();
		// mSortTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		// }
		// }
	}

	@Override
	public void OnDeleteFileSuccess() {
		// TODO Auto-generated method stub
		if (mAdapter.getEidtMode()) {
			exitEidtMode();
		}
		mFinished = true;
		mAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (mAdapter != null) {
			if (mSortTask.getStatus() == Status.FINISHED) {
				mSortTask = new SortTask();
				mSortTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
			mAdapter.notifyDataSetChanged();
		}
		if (inSearch) {
			// new GetSearchTask().execute(keyWord);
		}
		mTrackList.auroraOnResume();
		isStop = false;
	}

	@Override
	public void onAttachedToWindow() {
		// TODO Auto-generated method stub
		mTrackList.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				if (mAdapter == null) {
					return;
				}
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
					ImageLoader.getInstance().pause();
				} else {
					ImageLoader.getInstance().resume();
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				// lory add 2014.10.25 for scroll frame/per
				mAuroraHandler.removeMessages(MSG_UPDATE_LISTVIEW);
				mAuroraHandler.obtainMessage(MSG_UPDATE_LISTVIEW).sendToTarget();
			}
		});
		if (fromAdd && needShowSeleted)
			goEditMode(-1);
		super.onAttachedToWindow();
	}

	private long time = 0;

	public void startPlayAnimation(final int arg2, final boolean flag) {
		if (aima != null && aima.isStarted()) {
			mAdapter.setCurrentPosition(arg2 - 1);
			aima.end();
		}
		int[] location = new int[2];
		int[] location1 = new int[2];
		int[] location2 = new int[2];
		int distance = 0; // 移动距离
		mTrackList.getLocationInWindow(location);
		int currentPosition = mAdapter.getCurrentPosition();
		View arg1 = mTrackList.getChildAt(arg2 - mTrackList.getFirstVisiblePosition());
		if (arg1 == null) {
			mAdapter.setCurrentPosition(arg2 - 1);
			mTrackList.invalidateViews();
			if (flag)
				playMusic(mArrayList, arg2 - 1);
			return;
		}
		arg1.getLocationInWindow(location1);
		int startx = location1[0];
		int starty = location1[1];
		flystartPoint = new int[] { startx, starty };
		if (arg2 == 1) {
			flystartPoint[1] += getResources().getDimension(R.dimen.aurora_playmode_height);
		}
		if (currentPosition < 0) {
			// 无动画
			mPlaySelect.postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (isStop) {
						playMusic(mArrayList, arg2 - 1);
						return;
					}
					startFly();
					playMusic(mArrayList, arg2 - 1);
					mAdapter.setCurrentPosition(arg2 - 1);
					mAdapter.notifyDataSetChanged();
				}
			}, 100);
			if (isStop) {
				playMusic(mArrayList, arg2 - 1);
				mAdapter.setCurrentPosition(arg2 - 1);
				mAdapter.notifyDataSetChanged();
			}
			return;
		} else if (currentPosition < mTrackList.getFirstVisiblePosition()) {
			// 从最上面飞进来
			mPlaySelect.setY(-mPlaySelect.getHeight());
			distance = location1[1] - location[1] + mPlaySelect.getHeight();
		} else if (currentPosition > mTrackList.getLastVisiblePosition()) {
			// 从最下面飞进来
			mPlaySelect.setY(mTrackList.getBottom());
			distance = mTrackList.getHeight() - location1[1] + location[1];
		} else {
			// 具体位置飞进
			View view = mTrackList.getChildAt(currentPosition - mTrackList.getFirstVisiblePosition());
			view.getLocationInWindow(location2);
			if (currentPosition == 0)
				mPlaySelect.setY(location2[1] - location[1] + getResources().getDimension(R.dimen.aurora_search_height) + getResources().getDimension(R.dimen.aurora_playmode_height));
			else if (currentPosition == 1) {
				mPlaySelect.setY(location2[1] - location[1] + getResources().getDimension(R.dimen.song_itemheight) + getResources().getDimension(R.dimen.aurora_playmode_height));
			} else
				mPlaySelect.setY(location2[1] - location[1] + getResources().getDimension(R.dimen.song_itemheight));
			distance = Math.abs(location2[1] - location1[1]);
		}
		if (arg2 == 1)
			aima = ObjectAnimator.ofFloat(mPlaySelect, "y", location1[1] - location[1] + getResources().getDimension(R.dimen.aurora_playmode_height));
		else
			aima = ObjectAnimator.ofFloat(mPlaySelect, "y", location1[1] - location[1]);
		aima.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator arg0) {
				mPlaySelect.setVisibility(View.VISIBLE);
				mAdapter.setCurrentPosition(-2);
				mTrackList.invalidateViews();
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {

			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				mAuroraHandler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (mAdapter == null) {
							return;
						}
						mAdapter.setCurrentPosition(arg2 - 1);
						mTrackList.invalidateViews();
						mPlaySelect.setVisibility(View.GONE);
						if (flag)
							playMusic(mArrayList, arg2 - 1);

					}
				});
				mPlaySelect.postDelayed(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						startFly();
					}
				}, 10);
			}

			@Override
			public void onAnimationCancel(Animator arg0) {

			}

		});

		if (distance < 300) {
			aima.setDuration(150);
		} else {
			aima.setDuration(200);
		}
		aima.start();
	}

	public void playMusic(final ArrayList<AuroraListItem> arrayList, final int position) {
		MusicUtils.playAll(AuroraTrackBrowserActivity.this, arrayList, position, 0);
		AuroraMusicUtil.setCurrentPlaylist(AuroraTrackBrowserActivity.this, -1);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		setPlayAnimation();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (mTrackList.auroraIsRubbishOut()) {
			mTrackList.auroraSetRubbishBack();
		}
		if (isPlaying && actionBar_play != null) {
			actionBar_play.clearAnimation();
			actionBar_play.setBackgroundResource(R.drawable.aurora_left_bar_clicked);
			isPlaying = false;
		}
	}

	public void getSeletedItems() {
		// add by chenhl start 20140604
		for (int i = 0; i < mArrayList.size(); i++) {
			if (mAddList.contains(String.valueOf(mArrayList.get(i).getSongId()))) {
				mAdapter.setItemChecked(i, true);
			}
		}
		changeMenuState();
		// add by chenhl end 20140604
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mTrackList.auroraOnPause();
		dialog.setVisibility(View.GONE);
		AuroraMusicUtil.clearflyWindown();
		isStop = true;
	}

	public void updataSongCount(int count) {
		tv_songCount.setText(getResources().getString(R.string.number_track, count));
	}

	public Cursor doSearchTrack(String searchString) {
		String[] searchCols = new String[] { android.provider.BaseColumns._ID, MediaStore.Audio.Artists.ARTIST, MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.DATA };
		searchString = searchString.replace("\\", "\\\\");
		searchString = searchString.replace("%", "\\%");
		searchString = searchString.replace("_", "\\_");
		String wildcardWords = "%" + searchString + "%";
		String where = MediaStore.Audio.Media.TITLE + " LIKE ? AND " + Globals.QUERY_SONG_FILTER;
		Cursor cursor = getContentResolver().query(uri, searchCols, where, new String[] { wildcardWords }, null);
		return cursor;

	}

	public Cursor doSearchArtist(String searchString) {
		searchString = searchString.replace("\\", "\\\\");
		searchString = searchString.replace("%", "\\%");
		searchString = searchString.replace("_", "\\_");
		String wildcardWords = "%" + searchString + "%";
		StringBuilder where = new StringBuilder();
		where.append(MediaStore.Audio.Media.ARTIST + " LIKE ? AND " + Globals.QUERY_SONG_FILTER + ") GROUP BY (" + MediaStore.Audio.Media.ARTIST_ID);
		Cursor cursor = getContentResolver().query(uri,
				new String[] { MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.ARTIST, "COUNT(DISTINCT album_id)", "COUNT(*)", MediaStore.Audio.Media.DATA }, where.toString(),
				new String[] { wildcardWords }, null);
		return cursor;
	}

	public void setRubbishBack() {
		if (mTrackList != null && mTrackList.auroraIsRubbishOut()) {
			mTrackList.auroraSetRubbishBackNoAnim();
		}
	}

	public void getSongByArtist(long i, boolean flag) {
		StringBuilder where = new StringBuilder();
		where.append(Globals.QUERY_SONG_FILTER);
		where.append(" and " + MediaStore.Audio.Media.ARTIST_ID + "=" + i);
		Cursor cursor = AuroraTrackBrowserActivity.this.getContentResolver().query(uri, new String[] { MediaStore.Audio.Media._ID }, where.toString(), null, null);
		if (cursor != null) {
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				boolean exist = false;
				for (int index = 0; index < mList.size(); index++) {
					if (mList.get(index).getSongId() == cursor.getLong(0)) {
						exist = true;
						break;
					}
				}
				if (exist)
					return;
				if (flag && !exist) {
					mSearchAddList.add(String.valueOf(cursor.getLong(0)));
				} else {
					mSearchAddList.remove(String.valueOf(cursor.getLong(0)));
				}
			}

		}
	}

	private void startFly() {
		if (isStop || actionBar_play == null || inSearch)
			return;
		if (flyendPoint[0] == 0 || flyendPoint[1] == 0)
			actionBar_play.getLocationInWindow(flyendPoint);
		AuroraMusicUtil.startFly(AuroraTrackBrowserActivity.this, flystartPoint[0], flyendPoint[0], flystartPoint[1], flyendPoint[1], true);
	}
}
