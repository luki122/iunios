package com.android.auroramusic.online;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import android.R.integer;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ClipData.Item;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;

import com.android.auroramusic.adapter.AuroraNetSearchAdapter;
import com.android.auroramusic.adapter.OnlineMusicMainAdapter.OnGridViewClickListener;
import com.android.auroramusic.cache.AuroraHttpAsyncTask;
import com.android.auroramusic.model.XiaMiSdkUtils;
import com.android.auroramusic.ui.AuroraPlayerActivity;
import com.android.auroramusic.ui.BasicActivity;
import com.android.auroramusic.util.AuroraListItem;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.LogUtil;
import com.android.auroramusic.util.ThreadPoolExecutorUtils;
import com.android.music.MediaPlaybackService;
import com.android.music.MusicUtils;
import com.android.music.R;
import com.google.api.client.b.r;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.xiami.sdk.entities.OnlineAlbum;
import com.xiami.sdk.entities.OnlineArtist;
import com.xiami.sdk.entities.OnlineSong;
import com.xiami.sdk.entities.QueryInfo;
import com.xiami.sdk.utils.ImageUtil;

public class AuroraNetSearchActivity extends BasicActivity {
	private static final int MSG_BTNPLAY_CLICK = 100001;
	private static final int MSG_FETCH_FINISHED = 100002;
	private static final int MSG_FETCH_ARTIST = 100003;
	private static final int MSG_FETCH_ALBUM_FINISHED = 100004;
	private static final int MSG_FETCH_ERROR = 100005;
	private AuroraActionBar mAuroraActionBar;
	private View actionBar_play;
	private boolean isPlaying = false; // 动画是否在运行
	private Animation operatingAnim; // 播放按钮动画
	private static final int PAGE_SIZE = 15;
	private static final int ALBUM_START_SIZE = 4;
	private static final int MUSIC_PAGE_SIZE = 100;
	int mPageNo = 1;
	int mMusicPageNo = 1;
	private volatile int count;
	private volatile boolean hasMusic;
	private List musicList;
	private List albumList;
	private List<Object> mList = new ArrayList<Object>();
	private AuroraNetSearchAdapter mAdapter;
	private ListView mListView;
	private View mHeaderView;
	private String imageUrl;
	private TextView tv_artist_name;
	private TextView tv_artist_songnumber;
	private int songCount;
	private int albumCount;
	private int artist_id;
	private View loadingView;
	public static ImageView mPlaySelect;
	ObjectAnimator aima;;
	private TextView mArtistInfo;
	private ProgressBar mProgressBar;
	private ArrayList<AuroraListItem> mPlayArrayList = new ArrayList<AuroraListItem>();
	private DisplayImageOptions options;
	private View mAblumTag;
	private View mSongTag;
	private View mAlbumView;
	private View mSecondAlbumView;
	private View mAlbumMoreView;
	private View mSeparateView;
	private View mSecondSeparateView;
	private View mAlbumItem;
	private AuroraActionBar.OnAuroraActionBarItemClickListener mOnActionBarListener = new OnAuroraActionBarItemClickListener() {
		@Override
		public void onAuroraActionBarItemClicked(int itemid) {
			Intent intent = new Intent(AuroraNetSearchActivity.this,
					AuroraPlayerActivity.class);
			startActivity(intent);
//			overridePendingTransition(R.anim.slide_right_in,
//					R.anim.slide_left_out);
		}
	};

	static class AuroraHandler extends Handler {
		WeakReference<AuroraNetSearchActivity> mWeakReference;

		public AuroraHandler(WeakReference<AuroraNetSearchActivity> activity) {
			mWeakReference = activity;
		}

		public void handleMessage(android.os.Message msg) {
			if(mWeakReference.get() ==null || mWeakReference.get().mAdapter == null){
				return;
			}
			switch (msg.what) {
			case MSG_FETCH_FINISHED:
				if(mWeakReference.get().musicList.size()>0){
					mWeakReference.get().mSongTag.setVisibility(View.VISIBLE);
				}else{
					mWeakReference.get().mSongTag.setVisibility(View.GONE);
				}
				mWeakReference.get().mProgressBar.setVisibility(View.GONE);
				for(int i=mWeakReference.get().albumList.size()-1;i>=2;i--){
					mWeakReference.get().albumList.remove(i);
				}
				mWeakReference.get().mAdapter
						.setList(mWeakReference.get().musicList);
				mWeakReference.get().mListView
				.setAdapter(mWeakReference.get().mAdapter);
				mWeakReference.get().getAuroraListItems(
						mWeakReference.get().musicList);
				break;
			case MSG_FETCH_ARTIST:
				OnlineArtist onlineArtist =(OnlineArtist) msg.obj;
				if(onlineArtist!=null){
					ImageLoader.getInstance().displayImage(onlineArtist.getImageUrl(),  (ImageView) mWeakReference.get().mHeaderView
						.findViewById(R.id.aurora_netartist_icon), mWeakReference.get().options);
				}
				break;
			case MSG_FETCH_ALBUM_FINISHED:
				if (mWeakReference.get().albumList.size() > 2) {
					mWeakReference.get().mAdapter.setShowMoreTag(true);
				} else {
					mWeakReference.get().mAdapter.setShowMoreTag(false);
					mWeakReference.get().mAlbumMoreView.setVisibility(View.GONE);
				}
				
				if(mWeakReference.get().albumList.size()>0){
					mWeakReference.get().mAblumTag.setVisibility(View.VISIBLE);
					mWeakReference.get().mAdapter.setShowAlbumTag(true);
				}
				if(mWeakReference.get().albumList.size()==1){
					mWeakReference.get().mSecondAlbumView.setVisibility(View.GONE);
					mWeakReference.get().mSeparateView.setVisibility(View.GONE);
				}
				if(mWeakReference.get().albumList.size()==0){
					mWeakReference.get().mSecondAlbumView.setVisibility(View.GONE);
					mWeakReference.get().mAlbumView.setVisibility(View.GONE);
					mWeakReference.get().mSeparateView.setVisibility(View.GONE);
					mWeakReference.get().mSecondSeparateView.setVisibility(View.GONE);
					mWeakReference.get().mAlbumItem.setVisibility(View.GONE);
				}
				mWeakReference.get().initAlbumDate();
				break;
				case MSG_FETCH_ERROR:
					mWeakReference.get().showError();
					break;
			default:
				break;
			}
		};
	}

	Handler mAuroraHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.aurora_netsearch_activity,
				AuroraActionBar.Type.Normal, true);
		init();
	}
	

	public void init() {
		mAuroraHandler = new AuroraHandler(
				new WeakReference<AuroraNetSearchActivity>(this));
		mAuroraActionBar = getAuroraActionBar();// 获取actionbar
		if (mAuroraActionBar == null) {
			return;
		}
		mHeaderView = LayoutInflater.from(this).inflate(
				R.layout.aurora_netsearch_header, null);
		mAlbumItem = mHeaderView.findViewById(R.id.album_item);
		mAblumTag = mHeaderView.findViewById(R.id.aurora_ablum_tag);
		mAlbumMoreView = mAblumTag.findViewById(R.id.aurora_more_icon);
		mAlbumMoreView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				goMoreAlbums();
			}
			
		});
		mSecondAlbumView = mHeaderView.findViewById(R.id.aurora_ablum_parent2);
		mSecondAlbumView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				if(albumList.size()>=2){
					goAuroraNetTrackDetailActivity(albumList.get(1));
				}
			}
			
		});
		mAlbumView = mHeaderView.findViewById(R.id.aurora_ablum_parent);
		mAlbumView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				if(albumList.size()>=1){
					goAuroraNetTrackDetailActivity(albumList.get(0));
				}
			}
			
		});
		mSongTag = mHeaderView.findViewById(R.id.aurora_ablum_tag2);
		mHeaderView.setOnClickListener(null);
		mSeparateView = mHeaderView.findViewById(R.id.aurora_separate);
		mSecondSeparateView = mHeaderView.findViewById(R.id.aurora_separate2);
		mArtistInfo = (TextView)mHeaderView.findViewById(R.id.aurora_artist_songnumber);
		mPlaySelect = (ImageView) findViewById(R.id.aurora_song_selected);
		loadingView = findViewById(R.id.aurora_loading_parent);
		mProgressBar = (ProgressBar) findViewById(R.id.aurora_loading);
		mListView = (ListView) findViewById(R.id.aurora_netartist_list);
		mListView.addHeaderView(mHeaderView);
		mAdapter = new AuroraNetSearchAdapter(this);
		mAuroraActionBar.addItem(R.drawable.song_playing, MSG_BTNPLAY_CLICK, "");
		Intent intent = getIntent();
		actionBar_play = mAuroraActionBar.getItem(0).getItemView();
		String title = TextUtils.isEmpty(intent.getStringExtra("title")) ? getString(R.string.aurora_search_result)
				: intent.getStringExtra("title");
		artist_id = (int) intent.getLongExtra("artist_id", -1);
		imageUrl = intent.getStringExtra("imageUrl");
		songCount = intent.getIntExtra("song_count", -1);
		albumCount = intent.getIntExtra("album_count", -1);
		tv_artist_name = (TextView) mHeaderView
				.findViewById(R.id.aurora_artist_name);
		tv_artist_name.setText(title);

		options = new DisplayImageOptions.Builder()
		.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).cacheInMemory(true)
		.cacheOnDisk(true)
		.displayer(new RoundedBitmapDisplayer( (int) getResources()
				.getDimension(R.dimen.aurora_netartist_size) / 2,0)).build();
		
		ThreadPoolExecutor executor = ThreadPoolExecutorUtils.getThreadPoolExecutor().getExecutor();
		if(TextUtils.isEmpty(imageUrl) && artist_id >0){
			executor.submit(new Runnable() {
				
				@Override
				public void run() {
					OnlineArtist onlineArtist = XiaMiSdkUtils.fetchArtistDetailSync(AuroraNetSearchActivity.this,artist_id);
					Message message = Message.obtain();
					message.obj = onlineArtist;
					message.what = MSG_FETCH_ARTIST;
					mAuroraHandler.sendMessage(message);
				}
			});
			
			initArtistInfo();
		}else{
			ImageLoader.getInstance().displayImage(imageUrl,  (ImageView) mHeaderView
					.findViewById(R.id.aurora_netartist_icon), options);
			initArtistInfo();
		}

		mAuroraActionBar.setTitle(title);
		mAuroraActionBar.setOnAuroraActionBarListener(mOnActionBarListener);
		operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_anim);
		LinearInterpolator lin = new LinearInterpolator();
		operatingAnim.setInterpolator(lin);
		registerStateChangeReceiver();
		count = 0;
		executor.submit(new Runnable() {
			
			@Override
			public void run() {

				Pair<QueryInfo,List<OnlineAlbum>> pair = XiaMiSdkUtils.fetchAlbumsByArtistIdSync(AuroraNetSearchActivity.this,artist_id, ALBUM_START_SIZE,
								1);
				if(pair == null){
					mAuroraHandler.sendEmptyMessage(MSG_FETCH_ERROR);
					return;
				}
				albumList = pair.second;
				if(albumList == null){
					mAuroraHandler.sendEmptyMessage(MSG_FETCH_ERROR);
					return;
				}
				mAuroraHandler.sendEmptyMessage(MSG_FETCH_ALBUM_FINISHED);
				musicList = XiaMiSdkUtils.fetchSongsByArtistIdSync(AuroraNetSearchActivity.this,artist_id);
				LogUtil.d("JXH", "----1 musicList:"+musicList);
				if(musicList == null){
					mAuroraHandler.sendEmptyMessage(MSG_FETCH_ERROR);
					return;
				}
				LogUtil.d("JXH", "---------2 musicList:"+musicList.size());
				mAuroraHandler.sendEmptyMessage(MSG_FETCH_FINISHED);
			
				
			}
		});
/*		new Thread() {
			public void run() {
				Pair<QueryInfo,java.util.List<OnlineAlbum>> pair = XiaMiSdkUtils.getXiamiSDK(
						AuroraNetSearchActivity.this)
						.fetchAlbumsByArtistIdSync(artist_id, ALBUM_START_SIZE,
								1);
				if(pair == null){
					mAuroraHandler.sendEmptyMessage(MSG_FETCH_ERROR);
					return;
				}
				albumList = pair.second;
				if(albumList == null){
					mAuroraHandler.sendEmptyMessage(MSG_FETCH_ERROR);
					return;
				}
				mAuroraHandler.sendEmptyMessage(MSG_FETCH_ALBUM_FINISHED);
				musicList = XiaMiSdkUtils.getXiamiSDK(
						AuroraNetSearchActivity.this).fetchSongsByArtistIdSync(
						artist_id);
				if(musicList == null){
					mAuroraHandler.sendEmptyMessage(MSG_FETCH_ERROR);
					return;
				}
				mAuroraHandler.sendEmptyMessage(MSG_FETCH_FINISHED);
			};
		}.start();*/
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

	// 监听播放状态的变化
	private BroadcastReceiver mStatusListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(MediaPlaybackService.PLAYSTATE_CHANGED)) {
				setPlayAnimation();
			} else {
				mAdapter.notifyDataSetChanged();
			}
		}
	};

	// 注册监听播放器状态更改的广播
	private void registerStateChangeReceiver() {
		IntentFilter f = new IntentFilter();
		f.addAction(MediaPlaybackService.PLAYSTATE_CHANGED);
		f.addAction(MediaPlaybackService.META_CHANGED);
		registerReceiver(mStatusListener, f);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiverSafe(mStatusListener);
		if (mAdapter != null) {
			mAdapter = null;
		}
	}

	private void unregisterReceiverSafe(BroadcastReceiver receiver) {
		try {
			unregisterReceiver(receiver);
		} catch (IllegalArgumentException e) {
			// ignore
		}
	}

	public void goMoreAlbums() {
		Intent intent = new Intent(this, AuroraNetTrackActivity.class);
		intent.putExtra("artist_id", artist_id);
		intent.putExtra("title", getString(R.string.aurora_albums));
		intent.putExtra("album_count", albumCount);
		startActivity(intent);
	}

	private void getAuroraListItems(List list) {
		for (int i = 0; i < list.size(); i++) {
			OnlineSong music = (OnlineSong) list.get(i);
			if (music != null && music.getSongId() > 0) {
				String pic = music.getImageUrl();
				AuroraListItem songItem = new AuroraListItem(music.getSongId(),
						music.getSongName(), null, music.getAlbumName(),
						Long.valueOf(music.getAlbumId()),
						music.getArtistName(), 1, pic, music.getLyric(), null, -1,!AuroraMusicUtil.isNoPermission(music));
				mPlayArrayList.add(songItem);
			}
		}
	}

	public void goAuroraNetTrackDetailActivity(Object object) {
		Intent intent = new Intent(AuroraNetSearchActivity.this,
				AuroraNetTrackDetailActivity.class);
		intent.putExtra("tag", OnGridViewClickListener.NEW_ALBUM);
		intent.putExtra("title", ((OnlineAlbum) object).getAlbumName());
		intent.putExtra(AuroraNetTrackDetail.ID,
				String.valueOf(((OnlineAlbum) object).getAlbumId()));
		intent.putExtra("imageUrl", ImageUtil.transferImgUrl(((OnlineAlbum) object)
				.getImageUrl(), 330));
		intent.putExtra("artist", ((OnlineAlbum) object).getArtistName());
		startActivity(intent);
	}

	public void playMusic(int position) {
		startPlayAnimation(position);
	}

	public void startPlayAnimation(final int clickingPosition) {
		if (aima != null && aima.isStarted()) {
			mAdapter.setCurrentPosition(clickingPosition);
			aima.end();
		}

		int targetY, sourceY;// 都是相对于列表父组件上边缘的距离。
		int distance = 0; // 移动距离
		int mIndicatorHeight = getResources().getDimensionPixelSize(
				R.dimen.song_itemheight);
		int currentPosition = mAdapter.getCurrentPosition();
		View arg1 = mListView.getChildAt(clickingPosition + 1 - mListView.getFirstVisiblePosition());
		if (arg1 == null) {
			mAdapter.setCurrentPosition(clickingPosition);
			mListView.invalidateViews();
			MusicUtils.playAll(this, mPlayArrayList, clickingPosition, 0,true);
			return;
		}
		targetY = arg1.getBottom();

		if (currentPosition < 0) {
			// 无动画
			mAdapter.setCurrentPosition(clickingPosition);
			mListView.invalidateViews();
			MusicUtils.playAll(this, mPlayArrayList, clickingPosition, 0,true);
			return;
		} else if (currentPosition < mListView.getFirstVisiblePosition()) {
			// 从最上面飞进来
			mPlaySelect.setY(-mIndicatorHeight);
			distance = targetY - mIndicatorHeight;
		} else if (currentPosition > mListView.getLastVisiblePosition()) {
			// 从最下面飞进来
			mPlaySelect.setY(mListView.getHeight());
			distance = mListView.getHeight() - targetY + mIndicatorHeight;
		} else {
			// 起始与终止位置皆可见
			View view = mListView.getChildAt(currentPosition
					- mListView.getFirstVisiblePosition());
			sourceY = view.getBottom();
			if (currentPosition == 0) {
				sourceY += getResources().getDimension(
						R.dimen.aurora_netsearch_margin);
			}
			if (sourceY > targetY)
				sourceY -= mIndicatorHeight;

			mPlaySelect.setY(sourceY);
			distance = Math.abs(sourceY - targetY);
		}
		aima = ObjectAnimator.ofFloat(mPlaySelect, "y", targetY
				- mIndicatorHeight);
		aima.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator arg0) {
				mPlaySelect.setVisibility(View.VISIBLE);
				mAdapter.setCurrentPosition(-1);
				mListView.invalidateViews();
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {

			}

			@Override
			public void onAnimationEnd(Animator arg0) {

				mPlaySelect.post(new Runnable() {

					@Override
					public void run() {
						mAdapter.setCurrentPosition(clickingPosition);
						mListView.invalidateViews();
						mPlaySelect.setVisibility(View.GONE);
						MusicUtils.playAll(AuroraNetSearchActivity.this,
								mPlayArrayList, clickingPosition, 0,true);
					}
				});

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

	private List removeUnuse(List items) {
		OnlineSong item;
		for (int i = items.size() - 1; i >= 0; i--) {
			item = (OnlineSong) items.get(i);
			if (item == null || item.getSongId() <= 0) {
				items.remove(i);
			}
		}
		return items;
	}
	private void initAlbumDate(){
		DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
		.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).cacheInMemory(true)
		.cacheOnDisk(true)
		.displayer(new SimpleBitmapDisplayer()).build(); 
			if(albumList.size()>0&&albumList.get(0)!=null){
				ImageLoader.getInstance().displayImage(((OnlineAlbum)albumList.get(0)).getImageUrl(220), (ImageView)mAlbumView.findViewById(R.id.album_art), displayImageOptions);
				((TextView)mAlbumView.findViewById(R.id.album_name)).setText(((OnlineAlbum)albumList.get(0)).getAlbumName());
				((TextView)mAlbumView.findViewById(R.id.album_numtrack)).setText(getString(R.string.number_track,
						((OnlineAlbum)albumList.get(0)).getSongCount()));
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date date = new Date(((OnlineAlbum)albumList.get(0)).getPublishTime() * 1000);
				((TextView)mAlbumView.findViewById(R.id.album_release_date)).setText(getString(R.string.aurora_update_time,
						sdf.format(date)));
			}
			if(albumList.size()>1&&albumList.get(1)!=null){
				ImageLoader.getInstance().displayImage(((OnlineAlbum)albumList.get(1)).getImageUrl(220), (ImageView)mSecondAlbumView.findViewById(R.id.album_art), displayImageOptions);
				((TextView)mSecondAlbumView.findViewById(R.id.album_name)).setText(((OnlineAlbum)albumList.get(1)).getAlbumName());
				((TextView)mSecondAlbumView.findViewById(R.id.album_numtrack)).setText(getString(R.string.number_track,
						((OnlineAlbum)albumList.get(1)).getSongCount()));
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date date = new Date(((OnlineAlbum)albumList.get(1)).getPublishTime() * 1000);
				((TextView)mSecondAlbumView.findViewById(R.id.album_release_date)).setText(getString(R.string.aurora_update_time,
						sdf.format(date)));
		}
	}
	private void initArtistInfo(){
		AuroraHttpAsyncTask<Integer, Integer, HashMap<String, Integer>> task = new AuroraHttpAsyncTask<Integer, Integer, HashMap<String, Integer>>(){

			@Override
			protected HashMap<String, Integer> doInBackground(
					Integer... params) {
				// TODO Auto-generated method stub
				HashMap<String, Integer> tmpHashMap = XiaMiSdkUtils.fetchArtistCountInfoSync(AuroraNetSearchActivity.this,
								artist_id);
				return tmpHashMap;
			}
			@Override
			protected void onPostExecute(HashMap<String, Integer> result) {
				// TODO Auto-generated method stub
				if(result==null){
					mArtistInfo.setText(getResources().getString(
							R.string.num_songs_num_albums, 0,
							0));
					return;
				}
				int song_count = result.get("song_count");
				int album_count = result.get("album_count");
				if(song_count<0){
					song_count = 0;
				}
				if(album_count<0){
					album_count = 0;
				}
				albumCount = album_count;
				mArtistInfo.setText(getResources().getString(
						R.string.num_songs_num_albums, song_count,
						album_count));
			}
			
		};
		task.executeOnExecutor(AuroraHttpAsyncTask.THREAD_POOL_EXECUTOR);
	}
	private void showError(){
		Toast.makeText(AuroraNetSearchActivity.this, R.string.aurora_network_error,
				Toast.LENGTH_SHORT).show();
	}

}
