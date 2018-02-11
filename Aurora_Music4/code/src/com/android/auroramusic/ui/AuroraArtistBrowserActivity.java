package com.android.auroramusic.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.transform.Templates;

import android.R.integer;
import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.TextView;
import aurora.app.AuroraActivity.OnSearchViewQuitListener;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraListView.AuroraDeleteItemListener;
import aurora.widget.AuroraMenuBase;
import aurora.widget.AuroraSearchView.OnQueryTextListener;

import com.android.auroramusic.adapter.AuroraArtistListAdapter;
import com.android.auroramusic.adapter.AuroraSearchAdapter;
import com.android.auroramusic.model.AuroraDeleteItem;
import com.android.auroramusic.model.SearchItem;
import com.android.auroramusic.util.Artist;
import com.android.auroramusic.util.AuroraListItem;
import com.android.auroramusic.util.AuroraMainMenuData;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.DialogUtil;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.IntentFactory;
import com.android.auroramusic.util.LogUtil;
import com.android.auroramusic.widget.AuroraSearchView;
import com.android.auroramusic.widget.SideBar;
import com.android.auroramusic.widget.StickyListHeadersListView;
import com.android.auroramusic.widget.SideBar.OnTouchingLetterChangedListener;
import com.android.music.MediaPlaybackService;
import com.android.music.MusicUtils;
import com.android.music.R;

public class AuroraArtistBrowserActivity extends AbstractBaseActivity implements AuroraDeleteItemListener, DialogUtil.OnAddPlaylistSuccessListener, OnSearchViewQuitListener,
		DialogUtil.OnDeleteFileListener {
	private long mCurrentArtistId;
	private String mCurrentArtistName;
	private String mCurrentAlbumId;
	private String mCurrentAlbumName;
	private String mCurrentArtistNameForAlbum;
	boolean mIsUnknownArtist;
	boolean mIsUnknownAlbum;
	private AuroraArtistListAdapter mAdapter;
	private AuroraActionBar mAuroraActionBar;
	private ArrayList<Artist> mArrayList;
	private StickyListHeadersListView mArtistList;
	private AsyncQueryHandler mQueryHandler;
	private String[] cols;
	private View btn_menuCanel;
	private View btn_menuAll;
	private View actionBar_play;
	private boolean isPlaying = false; // 动画是否在运行
	private Animation operatingAnim; // 播放按钮动画
	private SideBar sideBar;
	private TextView tv_sidebar;
	private TextView dialog;
	private static ArrayList<String> songList;
	private ArrayList<String> mAddList = new ArrayList<String>();
	private boolean inSearch = false;
	private AuroraSearchAdapter mSearchAdapter;
	private View mHeaderView;
	private AuroraSearchView searchView;
	private static final int MSG_BTNPLAY_CLICK = 100001;
	private static final int REQUEST_CODE_ARTISTADDSONGS = 100010;
	public static final String EXTR_RESULT_DATA = "extr_result_data";
	public static final String EXTR_RESULT_MODE = "extr_result_mode";
	public static final String FROM_CODE = "fromCode";
	private ArrayList<SearchItem> mSearchlist = new ArrayList<SearchItem>();
	ArrayList<SearchItem> artistlist = new ArrayList<SearchItem>();
	ArrayList<SearchItem> tracklist = new ArrayList<SearchItem>();
	private StickyListHeadersListView mSearchListView;
	private AuroraMainMenuData playlistInfo;
	public boolean fromAdd = false;
	private GetSearchTask mSearchTask;
	private TextView tv_menu;
	View viewselected;
	private String keyWord;
	private static final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	private static String TAG = "AuroraArtistBrowserActivity";
	private AuroraAlertDialog mAuroraAlertDialog;
	private long[] mDeletetracklist;
	private AuroraDeleteItem mDeleteItem;
	
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what==100){
				DialogUtil.showAddDialog(AuroraArtistBrowserActivity.this, songList, AuroraArtistBrowserActivity.this);
			}
		}
		
	};

	private static class QueryHandler extends AsyncQueryHandler {
		private AuroraArtistBrowserActivity mActivity;

		QueryHandler(ContentResolver res, AuroraArtistBrowserActivity activity) {
			super(res);
			mActivity = activity;
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			// Log.i("@@@", "query complete");
			if (cursor != null)
				mActivity.init(cursor);
		}
	}

	private class AuroraOnItemlongClickListener implements OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			if(inSearch&&searchView.getVisibility()==View.GONE){
				return true;
			}
			if (mAdapter.getEidtMode() == false) {
				if (isPlaying && actionBar_play != null) {
					LogUtil.d(TAG, "clearAnimation()---------------");
					actionBar_play.clearAnimation();
					actionBar_play.setBackgroundResource(R.drawable.aurora_left_bar_clicked);
					isPlaying = false;
				}
				if (!mAuroraActionBar.auroraIsEntryEditModeAnimRunning()) {
					mAuroraActionBar.setShowBottomBarMenu(true);
					mAuroraActionBar.showActionBarDashBoard();
				}
				mAdapter.setNeedin(position);
				mArtistList.auroraSetNeedSlideDelete(false);
				mAdapter.changeEidtMode(true);
				changeMenuState();
			}
			return false;
		}

	}

	private class AuroraItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (position == 0)
				return;
			if (fromAdd) {
				Intent intent = new Intent(AuroraArtistBrowserActivity.this, AuroraTrackBrowserActivity.class);
				intent.putExtra("atristid", ((Artist) mArtistList.getAdapter().getItem(position)).mArtistId);
				intent.putExtra(AuroraSongSingle.EXTR_PLAYLIST_INFO, playlistInfo);
				intent.putStringArrayListExtra(AuroraNewPlayListActivity.EXTR_ADDLIST_DATA, mAddList);
				startActivityForResult(intent, REQUEST_CODE_ARTISTADDSONGS);
//				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			} else {
				Artist selectedArtist = (Artist) mArtistList.getAdapter().getItem(position);
				int artistofalbums = selectedArtist.mAlbumNumber;
				int artistofsongs = selectedArtist.mSongNumber;
				Intent intent = IntentFactory.newAlbumListIntent(AuroraArtistBrowserActivity.this, String.valueOf(selectedArtist.mArtistId), selectedArtist.mArtistName, artistofalbums, artistofsongs);
				startActivityForResult(intent, Globals.REQUEST_CODE_BROWSER);
//				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);

			}
		}

	}

	private AuroraItemClickListener mAuroraItemClickListener;
	private AuroraOnItemlongClickListener mOnItemlongClickListener;

	// 搜索输入框数据变化接口
	class SongSearchViewQueryTextChangeListener implements OnQueryTextListener {
		@Override
		public boolean onQueryTextSubmit(String query) {
			return false;

		}

		@Override
		public boolean onQueryTextChange(String newText) {
			if (TextUtils.isEmpty(newText.trim())) {
				if (getSearchView()!=null&&getSearchView().getQueryTextView() != null)
					getSearchView().getQueryTextView().requestFocus();
			}
			mSearchTask = new GetSearchTask();
			keyWord = newText;
			mSearchTask.execute(newText);
			return false;
		}
	}

	private AuroraActionBar.OnAuroraActionBarItemClickListener mOnActionBarListener = new OnAuroraActionBarItemClickListener() {
		@Override
		public void onAuroraActionBarItemClicked(int itemid) {
			Intent intent = new Intent(AuroraArtistBrowserActivity.this, AuroraPlayerActivity.class);
			startActivity(intent);
//			overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
		}
	};
	private AuroraMenuBase.OnAuroraMenuItemClickListener auroraMenuCallBack = new AuroraMenuBase.OnAuroraMenuItemClickListener() {
		@Override
		public void auroraMenuItemClick(int itemId) {
			if (fromAdd) {
				if (mAddList != null && mAddList.size() != 0) {

					MusicUtils.addToPlaylist(AuroraArtistBrowserActivity.this, mAddList, playlistInfo.getPlaylistId());
					Intent intent = new Intent(AuroraArtistBrowserActivity.this, AuroraSongSingle.class);
					intent.putExtra(AuroraSongSingle.EXTR_PLAYLIST_INFO, playlistInfo);
					startActivity(intent);
//					overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
					// gobackToSongSingle();
//					overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);// add
					AuroraMusicActivityManiger.getInstance().exit();
				}
			} else {
				switch (itemId) {
				case R.id.song_backup:
					deleteSongs();
					break;
				case R.id.song_add:
					getSongId();
					
//					DialogUtil.showAddDialog(AuroraArtistBrowserActivity.this, songList, AuroraArtistBrowserActivity.this);
					handler.sendEmptyMessageDelayed(100, 100);
					break;
				default:
					break;
				}
			}
		}
	};

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		if (icicle != null) {
			mCurrentAlbumId = icicle.getString("selectedalbum");
			mCurrentAlbumName = icicle.getString("selectedalbumname");
			String temp = icicle.getString("selectedartist");
			if (temp != null) {
				mCurrentArtistId = Long.parseLong(temp);
			}
			mCurrentArtistName = icicle.getString("selectedartistname");
		}
		Intent intent = getIntent();
		if (intent.getIntExtra(FROM_CODE, -1) == 1) {
			fromAdd = true;
			// add by chenhl 20140523
			playlistInfo = (AuroraMainMenuData) intent.getSerializableExtra(AuroraSongSingle.EXTR_PLAYLIST_INFO);
			AuroraMusicActivityManiger.getInstance().addActivity(this);
			mAddList = intent.getStringArrayListExtra(AuroraNewPlayListActivity.EXTR_ADDLIST_DATA);

		}
		setAuroraContentView(R.layout.aurora_artistbrowser_activity, AuroraActionBar.Type.Normal, true);
		registerStateChangeReceiver();
		initView();
		initData();
	}

	private void initView() {
		setAuroraBottomBarMenuCallBack(auroraMenuCallBack);
		mAuroraActionBar = getAuroraActionBar();// 获取actionbar
		mAuroraActionBar.getAuroraActionbarSearchView().setOnQueryTextListener(new SongSearchViewQueryTextChangeListener());
		// setOnQueryTextListener(new SongSearchViewQueryTextChangeListener());
		mAuroraActionBar.setOnAuroraActionBarListener(mOnActionBarListener);
		if (mAuroraActionBar.getVisibility() != View.VISIBLE) {
			((View) mAuroraActionBar).setVisibility(View.VISIBLE);
		}
		mAuroraActionBar.setTitle(R.string.appwidget_artisttitle);
		View tView = mAuroraActionBar.getHomeButton();
		if (tView != null) {
			tView.setVisibility(View.VISIBLE);
		}
		setOnSearchViewQuitListener(this);
		mAuroraItemClickListener = new AuroraItemClickListener();
		mOnItemlongClickListener = new AuroraOnItemlongClickListener();
		mArtistList = (StickyListHeadersListView) findViewById(R.id.artist_list);
		mSearchListView = (StickyListHeadersListView) findViewById(R.id.aurora_search_list);
		mSearchListView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				mSearchListView.requestFocus();
				return false;
			}
		});
		mArtistList.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				int position = mArtistList.getFirstVisiblePosition();
				if (sideBar.getVisibility() == View.VISIBLE)
					setTagState(position);
				if (firstVisibleItem > 0) {
					mArtistList.auroraSetHeaderViewYOffset(mArtistList.getHeaderBottomPosition());
				} else {
					mArtistList.auroraSetHeaderViewYOffset(-1);
				}
			}
		});
		// AuroraListView 删除监听器接口
		mArtistList.auroraSetDeleteItemListener(this);
		mArtistList.setOnItemClickListener(mAuroraItemClickListener);
		mHeaderView = LayoutInflater.from(this).inflate(R.layout.layout_search_view, null);
		mArtistList.addHeaderView(mHeaderView);
		searchView = (AuroraSearchView) mHeaderView.findViewById(R.id.aurora_search);
		searchView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(!fromAdd&&mAdapter.getEidtMode()){
					return;
				}
				// TODO Auto-generated method stub
				inSearch = true;
				if (!fromAdd && mAuroraActionBar.getAuroraActionBottomBarMenu().isShowing()) {
					mAuroraActionBar.setShowBottomBarMenu(false);
					mAuroraActionBar.showActionBottomeBarMenu();
				}

				if (fromAdd) {
					mAuroraActionBar.setShowBottomBarMenu(false);
					mAuroraActionBar.showActionBottomeBarMenu();
				}
				showSearchviewLayout();
				isPlaying=false;
				final Button btn_search = getSearchViewRightButton();
				if (btn_search!=null) {
					if (mAdapter.getEidtMode()) {
						btn_search.setText(getResources().getString(R.string.menu_continue));
					} else {
						btn_search.setText(getResources().getString(R.string.songlist_cancel));
					}
					btn_search.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							inSearch = false;
							hideSearchviewLayout();
							searchView.setVisibility(View.VISIBLE);
							if (!fromAdd && mAuroraActionBar.getAuroraActionBottomBarMenu().isShowing()) {
								mAuroraActionBar.setShowBottomBarMenu(false);
								mAuroraActionBar.showActionBottomeBarMenu();
							}
							mAuroraActionBar.getAuroraActionbarSearchView().getQueryTextView().setText("");
						}
					});
				}
				searchView.setVisibility(View.GONE);
				AuroraMusicUtil.showInputMethod(AuroraArtistBrowserActivity.this);

			}
		});
		mAuroraActionBar.getAuroraActionbarSearchViewBackButton().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				inSearch = false;
				searchView.setVisibility(View.VISIBLE);
				hideSearchviewLayout();
				mAuroraActionBar.getAuroraActionbarSearchView().getQueryTextView().setText("");
			}
		});
		mArtistList.setCacheColorHint(0);
		mArtistList.auroraSetNeedSlideDelete(true);
		mArtistList.setOnItemLongClickListener(mOnItemlongClickListener);
		mArtistList.auroraSetAuroraBackOnClickListener(new AuroraListView.AuroraBackOnClickListener() {
			@Override
			public void auroraOnClick(int position) {
				mCurrentArtistId = mArrayList.get(position - 1).mArtistId;
				deleteItemByArtist(position);
			}

			@Override
			public void auroraPrepareDraged(int position) {
				sideBar.setVisibility(View.GONE);
				tv_sidebar.setVisibility(View.GONE);
			}

			@Override
			public void auroraDragedSuccess(int position) {
				if (!mArtistList.auroraIsRubbishOut()) {
					sideBar.setVisibility(View.VISIBLE);
					tv_sidebar.setVisibility(View.VISIBLE);
				} else {
					sideBar.setVisibility(View.GONE);
					tv_sidebar.setVisibility(View.GONE);
				}
			}

			@Override
			public void auroraDragedUnSuccess(int position) {
				if (!mArtistList.auroraIsRubbishOut()) {
					sideBar.setVisibility(View.VISIBLE);
					tv_sidebar.setVisibility(View.VISIBLE);
				} else {
					sideBar.setVisibility(View.GONE);
					tv_sidebar.setVisibility(View.GONE);
				}
			}
		});
		btn_menuCanel = mAuroraActionBar.getSelectLeftButton();
		btn_menuCanel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				exitEidtMode();
			}
		});

		btn_menuAll = mAuroraActionBar.getSelectRightButton();
		btn_menuAll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (((TextView) btn_menuAll).getText().equals(getResources().getString(R.string.selectAll))) {
					((TextView) btn_menuAll).setText(getResources().getString(R.string.selectAllNot));
					mAdapter.selectAll();
					changeMenuState();
					mAdapter.notifyDataSetChanged();
				} else {
					((TextView) btn_menuAll).setText(getResources().getString(R.string.selectAll));
					mAdapter.selectAllNot();
					changeMenuState();
					mAdapter.notifyDataSetChanged();
				}
			}
		});
		sideBar = (SideBar) findViewById(R.id.sidebar);
		tv_sidebar = (TextView) findViewById(R.id.tv_sidebar);
		dialog = (TextView) findViewById(R.id.dialog);
		sideBar.setTextView(dialog);

		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				int position = mAdapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					mArtistList.setSelection(position, 0);
				}

			}
		});
		if (fromAdd) {
			getSearchView().setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					// TODO Auto-generated method stub
					if (fromAdd && mAuroraActionBar.getAuroraActionBottomBarMenu().isShowing()) {
						mAuroraActionBar.setShowBottomBarMenu(false);
						mAuroraActionBar.showActionBottomeBarMenu();
					}
				}
			});
			mAuroraActionBar.initActionBottomBarMenu(R.menu.menu_songaddedit, 1);
			tv_menu = mAuroraActionBar.getAuroraActionBottomBarMenu().getTitleViewByPosition(0);
			mArtistList.setOnItemLongClickListener(null);
			mArtistList.auroraSetNeedSlideDelete(false);
			mAuroraActionBar.setmOnActionBarBackItemListener(mOnActionBarBackItemListener);
		} else {
			mAuroraActionBar.initActionBottomBarMenu(R.menu.menu_songedit, 2);
			mAuroraActionBar.addItem(R.drawable.song_playing, MSG_BTNPLAY_CLICK, "");
			actionBar_play = mAuroraActionBar.getItem(0).getItemView();
			operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_anim);
			LinearInterpolator lin = new LinearInterpolator();
			operatingAnim.setInterpolator(lin);
		}
		mAuroraAlertDialog = new AuroraAlertDialog.Builder(this)//.setTitle(R.string.delete)
		// .setIcon(android.R.drawable.ic_dialog_info)
				.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						MusicUtils.deleteMediaTracks(AuroraArtistBrowserActivity.this, mDeleteItem, AuroraArtistBrowserActivity.this);
					}
				}).setNegativeButton(R.string.cancel, null).create();
	}

	private void initData() {
		mArrayList = new ArrayList<Artist>();
		mAdapter = new AuroraArtistListAdapter(this, mArrayList);
		mArtistList.setAdapter(mAdapter);
		mArtistList.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mArtistList.requestFocus();
				return false;
			}
		});
		mQueryHandler = new QueryHandler(getContentResolver(), this);
		requery();
		songList = new ArrayList<String>();
		mSearchAdapter = new AuroraSearchAdapter(AuroraArtistBrowserActivity.this, mSearchlist);
		mSearchListView.setAdapter(mSearchAdapter);
	}

	private void init(Cursor cursor) {
		mArrayList.clear();
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			long id = cursor.getLong(0);
			String name = "<unknown>";
			if (cursor.getString(1) != null && !TextUtils.isEmpty(cursor.getString(1)))
				name = cursor.getString(1);
			String pinyin = MusicUtils.getSpell(name);
			int trackCont = cursor.getInt(3);
			int albumCont = cursor.getInt(2);
			if (name.equals("<unknown>")) {
				name = getResources().getString(R.string.unknown_artist);
				pinyin = String.valueOf((char) 91) + name;
			}
			if (trackCont > 0) {
				Artist artist = new Artist(id, name, trackCont, albumCont, pinyin);
				mArrayList.add(artist);
			}

		}
		if (cursor != null) {
			cursor.close();
		}

		Collections.sort(mArrayList, new Comparator<Artist>() {

			@Override
			public int compare(Artist lhs, Artist rhs) {
				try {
					if (lhs.mPinyin.length() <= 0 || rhs.mPinyin.length() <= 0 || lhs.mArtistName.length() <= 0 || rhs.mArtistName.length() <= 0) {
						return 1;
					}

					// Log.i(TAG,
					// "zll ----- compare 1 lhs.mPinyin:"+lhs.mPinyin+",rhs.mPinyin:"+rhs.mPinyin+",lhs.mArtistName:"+lhs.mArtistName+",rhs.mArtistName:"+rhs.mArtistName);
					if (lhs.mPinyin.charAt(0) == rhs.mPinyin.charAt(0) && (65 > lhs.mArtistName.toUpperCase().charAt(0) || lhs.mArtistName.toUpperCase().charAt(0) > 90)
							&& (rhs.mArtistName.toUpperCase().charAt(0) <= 90 && rhs.mArtistName.toUpperCase().charAt(0) >= 65)) {
						return 1;

					} else if (lhs.mPinyin.charAt(0) == rhs.mPinyin.charAt(0) && (65 > rhs.mArtistName.toUpperCase().charAt(0) || rhs.mArtistName.toUpperCase().charAt(0) > 90)
							&& (lhs.mArtistName.toUpperCase().charAt(0) <= 90 && lhs.mArtistName.toUpperCase().charAt(0) >= 65)) {
						return -1;
					}
					return lhs.mPinyin.compareTo(rhs.mPinyin);
				} catch (Exception e) {
					// Log.i(TAG, "zll ----- compare 2 ");
					Log.i(TAG, "zll ----- compare 2 lhs.mPinyin:" + lhs.mPinyin + ",rhs.mPinyin:" + rhs.mPinyin + ",lhs.mArtistName:" + lhs.mArtistName + ",rhs.mArtistName:" + rhs.mArtistName);
					return 1;
				}

			}
		});
		if (mArrayList.size() == 0) {
			showNavtitle(true);
		} else {
			showNavtitle(false);
		}
		if (mArtistList.auroraIsRubbishOut()) {
			mArtistList.auroraSetRubbishBack();
		}
		mAdapter.notifyDataSetChanged();
		if (mAdapter.getItemHeight() == 0 && mAdapter != null && mArtistList != null && mAdapter.getCount() > 0) {
			View tmpView = mAdapter.getView(0, null, mArtistList);
			View headView = tmpView.findViewById(R.id.tv_artist_header);
			headView.measure(0, 0);
			int headerHeight = headView.getMeasuredHeight();
			tmpView.measure(0, 0);
			mAdapter.setItemHeight(tmpView.getMeasuredHeight(), headerHeight);
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		if (mAuroraActionBar.auroraIsExitEditModeAnimRunning() || mAuroraActionBar.auroraIsEntryEditModeAnimRunning()) {
			return true;
		} else if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK && (inSearch)) {
			inSearch = false;
			searchView.setVisibility(View.VISIBLE);
			hideSearchviewLayout();
			mAuroraActionBar.getAuroraActionbarSearchView().getQueryTextView().setText("");
			LogUtil.d(TAG, "-----mAuroraActionBar.getAuroraActionBottomBarMenu().isShowing():"+mAuroraActionBar.getAuroraActionBottomBarMenu().isShowing()+"--"+mAdapter.getEidtMode()+"--fromAdd:"+fromAdd);
			if (!fromAdd && !mAuroraActionBar.getAuroraActionBottomBarMenu().isShowing()&&mAdapter.getEidtMode()) {
				mAuroraActionBar.setShowBottomBarMenu(true);
				mAuroraActionBar.showActionBottomeBarMenu();
//				showActionBarDashBoard
			}
			return true;
		} else if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK && fromAdd) {
			finishWithDataResult();
			finish();
		} else if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK && mAddList.size() > 0 && !inSearch) {
			finishWithDataResult();
			finish();
		} else if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK && (mAdapter.getEidtMode())) {
			exitEidtMode();
			return true;
		} else if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK && mArtistList.auroraIsRubbishOut()) {
			mArtistList.auroraSetRubbishBack();
			return true;
		}

		return super.dispatchKeyEvent(event);
	}

	// 删除选中的所有歌曲
	public void deleteSongs() {
		final int count = mAdapter.getCheckedCount();
		final long[] list = new long[count];
		int index = 0;
		for (int i = 0; i < mArrayList.size(); i++) {
			if (mAdapter.getmCheckedMap().get(i) != null) {
				list[index] = mArrayList.get(i).mArtistId;
				index++;
			}
		}
		final ArrayList<Long> arrayList = new ArrayList<Long>();
		final ArrayList<String> pathList = new ArrayList<String>();
		mDeleteItem = new AuroraDeleteItem();
		for (int i = 0; i < list.length; i++) {
			AuroraDeleteItem item = MusicUtils.getSongListForArtist(AuroraArtistBrowserActivity.this, list[i], null);
			for (int j = 0; j < item.getId().length; j++) {
				arrayList.add(item.getId()[j]);
				pathList.add(item.getPath()[j]);
			}
		}
		mDeletetracklist = new long[arrayList.size()];
		for (int i = 0; i < mDeletetracklist.length; i++) {
			mDeletetracklist[i] = arrayList.get(i);
		}

		mDeleteItem.setId(mDeletetracklist);
		mDeleteItem.setPath((String[]) pathList.toArray(new String[0]));
		arrayList.clear();
		pathList.clear();
		if (mAuroraAlertDialog != null && !mAuroraAlertDialog.isShowing()) {
			mAuroraAlertDialog.setTitle(getResources().getString(R.string.deleteMessage, mDeletetracklist.length));
			mAuroraAlertDialog.show();
		}
	}

	// 设置分组栏目的状态
	public void setTagState(int position) {
		if (mAdapter == null)
			return;
		if (mArrayList.size() > 0) {
			String pinyin;
			if (position - 1 >= 0)
				pinyin = mArrayList.get(position - 1).mPinyin;
			else
				pinyin = mArrayList.get(position).mPinyin;
			sideBar.setCurChooseTitle(String.valueOf(pinyin.charAt(0)));
		}
	}

	public void deleteItemByArtist(int position) {
		final long[] list;
		if (mCurrentArtistId >= 0) {
			list = MusicUtils.getSongListForArtist(this, mCurrentArtistId);
		} else {
			list = new long[] { 1 };
		}
		new AuroraAlertDialog.Builder(this).setTitle(R.string.delete)
		// .setIcon(android.R.drawable.ic_dialog_info)
				.setMessage(getString(R.string.deleteMessage, list.length)).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						showDeleteAnimation();
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mArtistList.auroraSetRubbishBack();
					}
				}).show();
	}

	@Override
	public void auroraDeleteItem(View v, int position) {
		mArrayList.remove(position - 1);
		mAdapter.notifyDataSetChanged();
		if (mCurrentArtistId >= 0) {
			AuroraDeleteItem item = MusicUtils.getSongListForArtist(this, mCurrentArtistId, null);
			final long[] list = item.getId();
			MusicUtils.deleteMediaTracks(this, item, this);
		}
	}

	// 显示删除动画
	public void showDeleteAnimation() {
		mAdapter.hasDeleted = true;
		mArtistList.auroraDeleteSelectedItemAnim();
		if (mArrayList.size() == 0)
			sideBar.setVisibility(View.GONE);
		else
			sideBar.setVisibility(View.VISIBLE);
		tv_sidebar.setVisibility(View.VISIBLE);
	}

	// 退出编辑状态，显示退出动画
	public void exitEidtMode() {
		mAdapter.changeEidtMode(false);
		mAdapter.setNeedout();
		mAdapter.notifyDataSetChanged();
		if (!mAuroraActionBar.auroraIsExitEditModeAnimRunning()) {
			mAuroraActionBar.setShowBottomBarMenu(false);
			mAuroraActionBar.showActionBarDashBoard();
		}
		((TextView) btn_menuAll).setText(getResources().getString(R.string.selectAll));
		mArtistList.auroraSetNeedSlideDelete(true);
		if (actionBar_play != null) {
			actionBar_play.postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					setPlayAnimation();
				}
			}, 500);
		}
	}

	
	@Override
	public void hideSearchviewLayout() {
		super.hideSearchviewLayout();
		setPlayAnimation();
	}
	/**
	 * 设置播放动画
	 */
	private void setPlayAnimation() {
		if (fromAdd)
			return;
		try {
			if (MusicUtils.sService != null) {
				if (MusicUtils.sService.isPlaying()) {
					if (!isPlaying && mAdapter.getEidtMode() == false) {
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

	// 动态更改actionbar 显示全选/反选
	public void changeMenuState() {
		if (mAdapter.getCheckedCount() == mAdapter.getCount()) {
			((TextView) btn_menuAll).setText(getResources().getString(R.string.selectAllNot));
		} else {
			((TextView) btn_menuAll).setText(getResources().getString(R.string.selectAll));
		}
		if (fromAdd) {
			if (mAddList.size() == 0) {
				mAuroraActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(0, false);
			} else {
				mAuroraActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(0, true);
			}
			tv_menu.setText(getString(R.string.aurora_add_playlist_song_num, mAddList.size()));
		} else {
			if (mAdapter.getCheckedCount() == 0) {
				mAuroraActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(0, false);
				mAuroraActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(1, false);
			} else {
				mAuroraActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(0, true);
				mAuroraActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(1, true);
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

	// aurora ukiliu 2014-05-20 add begin
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Globals.RESULT_CODE_MODIFY) {
			// 某个歌手在专辑界面删除全部歌曲后返回歌手列表界面要数据刷新
			// initData();
			// mAdapter.notifyDataSetChanged();
			return;
		}
		if (resultCode != RESULT_OK) {
			return;
		}

		switch (requestCode) {
		case REQUEST_CODE_ARTISTADDSONGS:
			if (data == null) {
				return;
			}
			int mode = data.getIntExtra(EXTR_RESULT_MODE, 0);
			if (mode == 0) {
				// modify by chenhl start 20140604
				mAddList = data.getStringArrayListExtra(AuroraNewPlayListActivity.EXTR_RESULT_DATA);
				if (mAddList.size() > 0) {
					mAuroraActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(0, true);

				} else {
					mAuroraActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(0, false);
				}
				tv_menu.setText(getString(R.string.aurora_add_playlist_song_num, mAddList.size()));
				// modify by chenhl end
			} else {
				// finish();
				gobackToSongSingle();
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (mAdapter != null&&!inSearch) {
			requery();
		}
//		if (inSearch) {
			// mSearchTask = new GetSearchTask();
			// mSearchTask.execute(keyWord);
//		}
		mArtistList.auroraOnResume();
	}

	@Override
	public void onAttachedToWindow() {
		// TODO Auto-generated method stub
		if (fromAdd) {
			tv_menu.setText(getString(R.string.aurora_add_playlist_song_num, mAdapter.getCheckedCount()));
			mAuroraActionBar.setShowBottomBarMenu(true);
			mAuroraActionBar.showActionBottomeBarMenu();
			changeMenuState();
		}
		super.onAttachedToWindow();
	}

	private void getSongId() {
		songList.clear();
		for (int i = 0; i < mArrayList.size(); i++) {
			if (mAdapter.getmCheckedMap().get(i) != null) {
				long[] ids = MusicUtils.getSongListForArtist(this, mArrayList.get(i).mArtistId);
				for (int j = 0; j < ids.length; j++) {
					songList.add(String.valueOf(ids[j]));
				}
			}
		}
	}

	@Override
	public void OnAddPlaylistSuccess() {
		// TODO Auto-generated method stub
		exitEidtMode();
	}

	private List<String> mPathsXml = null;

	class GetSearchTask extends AsyncTask<String, String, String> {
		private boolean flag = false;

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			if (params[0] == null || TextUtils.isEmpty(params[0].trim())) {
				flag = true;
				return null;
			}
			String filter = params[0];
			if (filter == null) {
				filter = "";
			}

			if (mPathsXml == null) {
				mPathsXml = AuroraMusicUtil.doParseXml(AuroraArtistBrowserActivity.this, "paths.xml");
			}

			tracklist.clear();
			artistlist.clear();
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
			}
			if ((!fromAdd) && (!mAdapter.getEidtMode())) {
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
			}
			if (artistlist.size() > 0)
				Collections.sort(artistlist, new Comparator<SearchItem>() {

					@Override
					public int compare(SearchItem lhs, SearchItem rhs) {
						// TODO Auto-generated method stub
						if (lhs.mSimilarity > rhs.mSimilarity) {
							return -1;
						} else if (lhs.mSimilarity < rhs.mSimilarity) {
							return 1;
						} else {
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
			if (cursor != null)
				cursor.close();
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
					// mHeaderHideView.setVisibility(View.VISIBLE);
					mSearchListView.setVisibility(View.VISIBLE);
					sideBar.setVisibility(View.GONE);
					if (mAdapter.getEidtMode()) {
						mSearchAdapter.setEidtMode(true);
					} else {
						mSearchAdapter.setEidtMode(false);
					}
					mSearchAdapter.notifyDataSetChanged();
					mSearchListView.setSelection(0);
					mSearchListView.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
							// TODO Auto-generated method stub
							mSearchListView.requestFocus();
							if (!((SearchItem) mSearchAdapter.getItem(position)).mMimeType.equals("artist") && !fromAdd) {
								ArrayList<AuroraListItem> list = new ArrayList<AuroraListItem>();
								SearchItem item = ((SearchItem) mSearchAdapter.getItem(position));
								long songId = item.getSongId();

								AuroraListItem listItem = new AuroraListItem(songId, item.getTitle(), null, item.getAlbumName(), item.getAlbumId(), item.getArtistName(), 0, null, null, null, -1);
								list.add(listItem);
								MusicUtils.playAll(AuroraArtistBrowserActivity.this, list, 0, 0);
								mSearchAdapter.notifyDataSetChanged();
							} else if (!fromAdd) {
								int artistofalbums = ((SearchItem) mSearchAdapter.getItem(position)).mAlbumCount;
								int artistofsongs = ((SearchItem) mSearchAdapter.getItem(position)).mSongCount;

								Intent intent = IntentFactory.newAlbumListIntent(AuroraArtistBrowserActivity.this, String.valueOf(((SearchItem) mSearchAdapter.getItem(position)).getSongId()),
										((SearchItem) mSearchAdapter.getItem(position)).getArtistName(), artistofalbums, artistofsongs);
								startActivityForResult(intent, Globals.REQUEST_CODE_BROWSER);
//								overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
							} else {
								Intent intent = new Intent(AuroraArtistBrowserActivity.this, AuroraTrackBrowserActivity.class);
								intent.putExtra("atristid", ((SearchItem) mSearchAdapter.getItem(position)).getSongId());
								intent.putExtra(AuroraSongSingle.EXTR_PLAYLIST_INFO, playlistInfo);
								intent.putStringArrayListExtra(AuroraNewPlayListActivity.EXTR_ADDLIST_DATA, mAddList); // add
																														// by
																														// chenhl
								startActivityForResult(intent, REQUEST_CODE_ARTISTADDSONGS);
//								overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
							}
						}
					});

				}
				if (flag) {
					mSearchListView.setVisibility(View.GONE);
					// mHeaderHideView.setVisibility(View.GONE);
					sideBar.setVisibility(View.VISIBLE);
				}
			}
			super.onPostExecute(result);
		}

	}

	@Override
	public boolean quit() {
		// TODO Auto-generated method stub
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
		return false;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// add by chenhl 20140619 start
		if (fromAdd) {
			AuroraMusicActivityManiger.getInstance().removeActivity(this);
		}
		// add by chenhl 20140619 end
		unregisterReceiverSafe(mStatusListener);
	}

	private void unregisterReceiverSafe(BroadcastReceiver receiver) {
		try {
			unregisterReceiver(receiver);
		} catch (IllegalArgumentException e) {
			// ignore
		}
	}

	public boolean getItemCheckedArtisId(long artistId) {
		for (int index = 0; index < mArrayList.size(); index++) {
			if (mArrayList.get(index).mArtistId == artistId) {
				return mAdapter.isItemChecked(index);
			}
		}
		return false;
	}

	public void setItemChecked(long i, boolean flag) {
		for (int index = 0; index < mArrayList.size(); index++) {
			if (mArrayList.get(index).mArtistId == i) {
				mAdapter.setItemChecked(index, flag);
				break;
			}
		}
	}

	@Override
	public void onMediaDbChange(boolean selfChange) {
		// TODO Auto-generated method stub
		// if (mAdapter != null) {
		// requery();
		// }
	}

	private void finishWithDataResult() {
		/*
		 * long[] list = new long[mAddList.size()]; for (int i = 0; i <
		 * list.length; i++) { list[i] = Long.valueOf(mAddList.get(i)); }
		 */
		Intent tIntent = new Intent();
		tIntent.putExtra(AuroraNewPlayListActivity.EXTR_RESULT_DATA, mAddList);
		tIntent.putExtra(AuroraNewPlayListActivity.EXTR_RESULT_MODE, 0);
		setResult(RESULT_OK, tIntent);
	}

	@Override
	public void OnDeleteFileSuccess() {
		// TODO Auto-generated method stub
		requery();
		if (mAdapter.getEidtMode())
			exitEidtMode();
		mAdapter.notifyDataSetChanged();
	}

	// add by chenhl start 20140604
	/**
	 * 回到歌单页面
	 */
	private void gobackToSongSingle() {
		Intent tIntent = new Intent();
		tIntent.putExtra(AuroraNewPlayListActivity.EXTR_RESULT_MODE, 1);
		setResult(RESULT_OK, tIntent);
		finish();
	}

	private OnAuroraActionBarBackItemClickListener mOnActionBarBackItemListener = new OnAuroraActionBarBackItemClickListener() {

		@Override
		public void onAuroraActionBarBackItemClicked(int arg0) {

			Intent tIntent = new Intent();
			tIntent.putExtra(AuroraNewPlayListActivity.EXTR_RESULT_MODE, 0);
			tIntent.putStringArrayListExtra(AuroraNewPlayListActivity.EXTR_RESULT_DATA, mAddList);
			setResult(RESULT_OK, tIntent);
			finish();
		}

	};

	// add by chenhl end 2014060

	public void showNavtitle(boolean flag) {
		if (flag) {
			mArtistList.setVisibility(View.GONE);
			sideBar.setVisibility(View.GONE);
			findViewById(R.id.aurora_no_singers).setVisibility(View.VISIBLE);
		} else {
			mArtistList.setVisibility(View.VISIBLE);
			sideBar.setVisibility(View.VISIBLE);
			findViewById(R.id.aurora_no_singers).setVisibility(View.GONE);
		}
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
		if (mArtistList.auroraIsRubbishOut()) {
			mArtistList.auroraSetRubbishBack();
		}
		if (isPlaying && actionBar_play != null) {
			actionBar_play.clearAnimation();
			actionBar_play.setBackgroundResource(R.drawable.aurora_left_bar_clicked);
			isPlaying = false;
		}
	}

	public void requery() {
		if (mAdapter != null) {
			StringBuilder where = new StringBuilder();
			where.append(Globals.QUERY_SONG_FILTER + AuroraMusicUtil.getFileString(this) + ") GROUP BY (" + MediaStore.Audio.Media.ARTIST_ID);
			mQueryHandler.startQuery(0, null, uri, new String[] { MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.ARTIST, "COUNT(DISTINCT album_id)", "COUNT(*)" }, where.toString(), null,
					null);
		}
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

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mArtistList.auroraOnPause();
	}

}
