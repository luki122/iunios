package com.android.auroramusic.online;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;

import com.android.auroramusic.adapter.AuroraNetPlaylistAdapter;
import com.android.auroramusic.adapter.AuroraNetSearchAdapter;
import com.android.auroramusic.adapter.OnlineMusicMainAdapter.OnGridViewClickListener;
import com.android.auroramusic.model.XiaMiSdkUtils;
import com.android.auroramusic.ui.AuroraPlayerActivity;
import com.android.auroramusic.ui.BasicActivity;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.LogUtil;
import com.android.auroramusic.util.ThreadPoolExecutorUtils;
import com.android.music.MediaPlaybackService;
import com.android.music.MusicUtils;
import com.android.music.R;
import com.xiami.sdk.callback.OnlineAlbumsCallback;
import com.xiami.sdk.callback.OnlineCollectsCallback;
import com.xiami.sdk.callback.OnlineSongsCallback;
import com.xiami.sdk.entities.OnlineAlbum;
import com.xiami.sdk.entities.OnlineCollect;
import com.xiami.sdk.entities.QueryInfo;

public class AuroraNetTrackActivity extends BasicActivity {
	private int mPageNo = 1;
	private static final int PAGE_SIZE = 40;
	private static final int ALBUM_PAGE_SIZE = 15;
	private int mAlbumPageNo = 1;
	private static final String TAG = "AuroraNetTrackActivity";
	private static final int MSG_BTNPLAY_CLICK = 100001;
	private static final int MSG_REFRESH_ALBUMS = 100002;
	private ListView mListView;
	private AuroraActionBar mAuroraActionBar;
	private AuroraNetPlaylistAdapter mAdapter;
	private View actionBar_play;
	private boolean isPlaying = false; // 动画是否在运行
	private Animation operatingAnim; // 播放按钮动画
	private ProgressBar mProgressBar;
	private int artist_id;
	private int albumCount;
	private View loadingView;
	private String trackTag = "全部";
	private boolean canloadmore = true;
	private String tag;
	int mType = -1;
	private int mNet = -1;
	private AuroraActionBar.OnAuroraActionBarItemClickListener mOnActionBarListener = new OnAuroraActionBarItemClickListener() {
		@Override
		public void onAuroraActionBarItemClicked(int itemid) {
			Intent intent = new Intent(AuroraNetTrackActivity.this,
					AuroraPlayerActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_right_in,
					R.anim.slide_left_out);
		}
	};
	private OnAuroraActionBarBackItemClickListener mOnActionBarBackItemListener = new OnAuroraActionBarBackItemClickListener() {

		@Override
		public void onAuroraActionBarBackItemClicked(int arg0) {
			finish();
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.aurora_nettrack_activity,
				AuroraActionBar.Type.Normal, true);
		init();
	}

	public void init() {
		mHandler = new CollectHandler(this);
		mCollects = new ArrayList<OnlineCollect>();
		Intent intent = getIntent();
		tag = TextUtils.isEmpty(intent.getStringExtra("tag")) ? trackTag
				: intent.getStringExtra("tag");
		mType = intent.getIntExtra("type", -1);
		String title = TextUtils.isEmpty(intent.getStringExtra("title")) ? getString(R.string.aurora_recommend_playlist)
				: intent.getStringExtra("title");
		artist_id = intent.getIntExtra("artist_id", -1);
		LogUtil.d(TAG, "------------artist_id:"+artist_id);
		albumCount = intent.getIntExtra("album_count", -1);
		mAuroraActionBar = getAuroraActionBar();// 获取actionbar
		if (mAuroraActionBar == null) {
			return;
		}
		loadingView = findViewById(R.id.aurora_loading_parent);
		mAuroraActionBar.setTitle(title);
		mAuroraActionBar.setOnAuroraActionBarListener(mOnActionBarListener);
		mAuroraActionBar
				.setmOnActionBarBackItemListener(mOnActionBarBackItemListener);
		mListView = (ListView) findViewById(R.id.aurora_recommend_list);
		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					if (view.getLastVisiblePosition() == view.getCount() - 1) {
						LogUtil.d(TAG, "----------onScrollStateChanged--artist_id:"+artist_id);
						if (artist_id > 0) {
							if (mListView.getLastVisiblePosition() == mCollects
									.size() - 1
									&& mCollects.size() < albumCount) {
								loadingView.setVisibility(View.VISIBLE);
								mPageNo += 1;
								loadSearchAlbum();
							}
						} else {
							if (mListView.getLastVisiblePosition() == mCollects
									.size() - 1 && canloadmore) {
								loadingView.setVisibility(View.VISIBLE);
								mPageNo += 1;
								getPlayList(mType);
							}
						}
					}
					if (mAdapter != null) {
						mAdapter.setScrolling(false);
						mListView.invalidateViews();
					}
				} else {
					if (mAdapter != null) {
						mAdapter.setScrolling(true);
					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
			}
		});
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (artist_id > 0) {
					OnlineAlbum album = (OnlineAlbum) mCollects.get(position);
					Intent intent = new Intent(AuroraNetTrackActivity.this,
							AuroraNetTrackDetailActivity.class);
					intent.putExtra("tag", OnGridViewClickListener.NEW_ALBUM);
					intent.putExtra("title", album.getAlbumName());
					intent.putExtra(AuroraNetTrackDetail.ID,
							String.valueOf(album.getAlbumId()));
					intent.putExtra("imageUrl", album
							.getImageUrl((int) getResources().getDimension(
									R.dimen.aurora_recommend_toplayout_height)));
					intent.putExtra("artist", album.getArtistName());
					startActivity(intent);
				} else {
					Intent intent = new Intent(AuroraNetTrackActivity.this,
							AuroraNetTrackDetailActivity.class);
					if (mCollects.get(position) instanceof OnlineCollect) {
						intent.putExtra("tag",
								OnGridViewClickListener.RECOMMEND_PLAYLIST);
						intent.putExtra(AuroraNetTrackDetail.ID, String
								.valueOf(((OnlineCollect) mCollects
										.get(position)).getListId()));
						intent.putExtra(
								"imageUrl",
								((OnlineCollect) mCollects.get(position))
										.getImageUrl((int) getResources()
												.getDimension(
														R.dimen.aurora_recommend_toplayout_height)));
						intent.putExtra("title", ((OnlineCollect) mCollects
								.get(position)).getCollectName());
						intent.putExtra("playlist_tag",
								((OnlineCollect) mCollects.get(position))
										.getDescription());
					} else {
						intent.putExtra("tag",
								OnGridViewClickListener.NEW_ALBUM);
						intent.putExtra(AuroraNetTrackDetail.ID,
								String.valueOf(((OnlineAlbum) mCollects
										.get(position)).getAlbumId()));
						intent.putExtra(
								"imageUrl",
								((OnlineAlbum) mCollects.get(position))
										.getImageUrl((int) getResources()
												.getDimension(
														R.dimen.aurora_recommend_toplayout_height)));
						intent.putExtra("title", ((OnlineAlbum) mCollects
								.get(position)).getAlbumName());
						intent.putExtra("playlist_tag",
								((OnlineAlbum) mCollects.get(position))
										.getDescription());
					}
					startActivity(intent);
				}
			}
		});
		mProgressBar = (ProgressBar) findViewById(R.id.aurora_loading);
		mAuroraActionBar
				.addItem(R.drawable.song_playing, MSG_BTNPLAY_CLICK, "");
		actionBar_play = mAuroraActionBar.getItem(0).getItemView();
		operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_anim);
		LinearInterpolator lin = new LinearInterpolator();
		operatingAnim.setInterpolator(lin);
		registerStateChangeReceiver();
		if (artist_id > 0) {
			loadSearchAlbum();
		} else {
			getPlayList(mType);
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
				if (MusicUtils.sService.isPlaying()) {
					if (!isPlaying) {
						actionBar_play.startAnimation(operatingAnim);
						actionBar_play
								.setBackgroundResource(android.R.color.transparent);
						isPlaying = true;
					}
				} else if (isPlaying) {
					actionBar_play.clearAnimation();
					actionBar_play
							.setBackgroundResource(R.drawable.aurora_left_bar_clicked);
					isPlaying = false;
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			if (isPlaying) {
				actionBar_play.clearAnimation();
				actionBar_play
						.setBackgroundResource(R.drawable.aurora_left_bar_clicked);
				isPlaying = false;
			}
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
		if (isPlaying && actionBar_play != null) {
			actionBar_play.clearAnimation();
			actionBar_play
					.setBackgroundResource(R.drawable.aurora_left_bar_clicked);
			isPlaying = false;
		}
	}

	private void onpause() {
		// TODO Auto-generated method stub
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

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mHandler != null) {
			mHandler.removeMessages(MSG_REFRESH_COLLECT);
			mHandler.removeMessages(MSG_REFRESH_COLLECTS);
			mHandler = null;
		}
		unregisterReceiverSafe(mStatusListener);
	}

	private void unregisterReceiverSafe(BroadcastReceiver receiver) {
		try {
			unregisterReceiver(receiver);
		} catch (IllegalArgumentException e) {
			// ignore
		}
	}

	
	public void loadSearchAlbum() {
		LogUtil.d(ThreadPoolExecutorUtils.TAG, "----------------------------------loadSearchAlbum");
		ThreadPoolExecutor executor = ThreadPoolExecutorUtils.getThreadPoolExecutor().getExecutor();
		executor.submit(new Runnable() {
			
			@Override
			public void run() {
				Pair<QueryInfo, java.util.List<OnlineAlbum>> pair = XiaMiSdkUtils.fetchAlbumsByArtistIdSync(AuroraNetTrackActivity.this,artist_id, ALBUM_PAGE_SIZE,
								mPageNo);
				if (pair == null || pair.second == null) {
					if (mHandler != null) {
						mHandler.sendEmptyMessage(MSG_ERROR_RESPOSE);
					}
					return;
				}
				mTempList = pair.second;
				if (mHandler != null) {
					mHandler.sendEmptyMessage(MSG_REFRESH_ALBUMS);
				}
			
			}
		});
	}

	private void getPlayList(int type) {
		if (AuroraMusicUtil.isNetWorkActive(this)) {
			LogUtil.d(ThreadPoolExecutorUtils.TAG, "----------------------------------getPlayList");
			ThreadPoolExecutor executor = ThreadPoolExecutorUtils.getThreadPoolExecutor().getExecutor();
			executor.submit(new PlayList(type));
		} else {
			mProgressBar.setVisibility(View.GONE);
			loadingView.setVisibility(View.GONE);
			Toast.makeText(this, R.string.aurora_network_error,
					Toast.LENGTH_SHORT).show();
		}
	}

	private class PlayList implements Runnable {
		int type;

		public PlayList(int type) {
			super();
			this.type = type;
		}

		@Override
		public void run() {
			if (type == 1) {
				mNet = 2;
				Pair<QueryInfo, List<OnlineAlbum>> pair = XiaMiSdkUtils.getWeekHotAlbumsSync(getApplicationContext(),PAGE_SIZE, mPageNo);
				
//				LogUtil.d(TAG, "--------OnlineAlbum-----pair:"+pair);
				if (pair != null && pair.second != null) {
					mCollects.addAll(pair.second);
					if (mPageNo >= 4 || pair.second.size() < PAGE_SIZE) {
						canloadmore = false;
					}
					mHandler.sendEmptyMessage(MSG_REFRESH_COLLECTS);
				} else {
					mCollects = null;
					mHandler.sendEmptyMessage(MSG_ERROR_RESPOSE);
				}

			} else {
				Pair<QueryInfo, List<OnlineCollect>> pair = XiaMiSdkUtils.getCollectsRecommendSync(getApplicationContext(),PAGE_SIZE, mPageNo);
//				LogUtil.d(TAG, "--------OnlineCollect-----pair:"+pair);
				if (pair != null && pair.second != null) {
					mCollects.addAll(pair.second);
					if (mPageNo >= 4 || pair.second.size() < PAGE_SIZE) {
						canloadmore = false;
					}
					mHandler.sendEmptyMessage(MSG_REFRESH_COLLECTS);
				} else {
					mCollects = null;
					mHandler.sendEmptyMessage(MSG_ERROR_RESPOSE);
				}
			}
		}

	}

	/**
	 * 成功获取推荐精选集列表时发送的消息
	 */
	public static final int MSG_REFRESH_COLLECTS = 0x01;

	/**
	 * 成功获取选中推荐精选集时发送的消息
	 */
	public static final int MSG_REFRESH_COLLECT = 0x02;

	/**
	 * 请求失败
	 */
	public static final int MSG_ERROR_RESPOSE = 0x03;

	private CollectHandler mHandler;

	/**
	 * 处理异步操作
	 */
	private class CollectHandler extends Handler {

		SoftReference<AuroraNetTrackActivity> softReference;

		public CollectHandler(AuroraNetTrackActivity activity) {
			softReference = new SoftReference<AuroraNetTrackActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			AuroraNetTrackActivity activity = softReference.get();
			mProgressBar.setVisibility(View.GONE);
			loadingView.setVisibility(View.GONE);
			switch (msg.what) {
			// 成功获取推荐精选集列表
			case MSG_REFRESH_COLLECTS:
				if (mPageNo == 1) {
					mAdapter = new AuroraNetPlaylistAdapter(
							AuroraNetTrackActivity.this, mCollects);
					mAdapter.setType(mNet);
					mListView.setAdapter(mAdapter);
				} else {
					mAdapter.notifyDataSetChanged();
				}
				break;
			case MSG_REFRESH_ALBUMS:
				if (mPageNo == 1) {
					mCollects.addAll(mTempList);
					mAdapter = new AuroraNetPlaylistAdapter(
							AuroraNetTrackActivity.this, mCollects);
					mAdapter.setType(1);
					mListView.setAdapter(mAdapter);
				} else {
					mCollects.addAll(mTempList);
					mAdapter.notifyDataSetChanged();
				}
				break;
			case MSG_ERROR_RESPOSE:
				// Toast.makeText(activity, R.string.error_response,
				// Toast.LENGTH_SHORT).show();
				break;
			}
		}
	}


	/**
	 * 选中的精选集
	 */
	private OnlineCollect mCollect;

	/**
	 * 推荐精选集名列表
	 */
	private String[] mCollectName;

	/**
	 * 推荐精选集
	 */
	private List mCollects;
	private List mTempList;

}