package com.android.auroramusic.online;

import java.util.List;

import com.android.auroramusic.adapter.OnlineRankListAdapter;
import com.android.auroramusic.adapter.OnlineMusicMainAdapter.OnGridViewClickListener;
import com.android.auroramusic.model.AuroraCollectPlaylist;
import com.android.auroramusic.ui.AuroraPlayerActivity;
import com.android.auroramusic.ui.BasicActivity;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.LogUtil;
import com.android.auroramusic.util.ThreadPoolExecutorUtils;
import com.android.music.MediaPlaybackService;
import com.android.music.MusicUtils;
import com.android.music.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraListView.AuroraBackOnClickListener;
import aurora.widget.AuroraListView.AuroraDeleteItemListener;

public class AuroraRankList extends BasicActivity implements OnItemClickListener {

	private static final String TAG = "AuroraRankList";
	private static final int PLAY_BUTTON = 0;
	private boolean isPlaying = false; // 动画是否在运行
	private View playView; // 播放按钮
	private Animation operatingAnim; // 播放按钮动画
	private AuroraListView mListView;
	private OnlineRankListAdapter mOnlineRankListAdapter;
	private AuroraAlertDialog mAuroraAlertDialog;
	private TextView noPlaylist;
	private List<AuroraCollectPlaylist> list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.album_list_content);

		try {
			initView();
			initActionbar();

		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.d(TAG, "INIT ERROR!!!");
		}
	}

	private void initView() {
		mListView = (AuroraListView) findViewById(android.R.id.list);
		noPlaylist = (TextView) findViewById(R.id.aurora_no_playlist);
		mListView.auroraSetNeedSlideDelete(false);
		mListView.auroraSetSelectorToContentBg(false);
		mListView.setOnItemClickListener(this);
		mListView.auroraSetNeedSlideDelete(true);
		mListView.auroraSetDeleteItemListener(mAuroraDeleteItemListener);
		mListView.auroraSetAuroraBackOnClickListener(mAuroraBackOnClickListener);
		mListView.setSelector(R.drawable.aurora_playlist_item_clicked);
		mOnlineRankListAdapter = new OnlineRankListAdapter(this);
		mListView.setAdapter(mOnlineRankListAdapter);
		mListView.setDivider(getResources().getDrawable(R.drawable.line2));
		changeViewState(0);
	}

	private void initActionbar() {
		AuroraActionBar mActionBar = getAuroraActionBar();// 获取actionbar
		mActionBar.setTitle(R.string.aurora_collect_playlist);
		// 旋转动画方式
		mActionBar.addItem(R.drawable.song_playing, PLAY_BUTTON, null);
		playView = mActionBar.getItem(PLAY_BUTTON).getItemView();
		operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_anim);

		LinearInterpolator lin = new LinearInterpolator();
		operatingAnim.setInterpolator(lin);
		mActionBar.setOnAuroraActionBarListener(mOnAuroraActionBarItemClickListener);
	}

	private AuroraDeleteItemListener mAuroraDeleteItemListener = new AuroraDeleteItemListener() {

		@Override
		public void auroraDeleteItem(View arg0, int arg1) {

			AuroraCollectPlaylist item = (AuroraCollectPlaylist) mOnlineRankListAdapter.getItem(arg1);
			MusicUtils.mSongDb.deleteCollectById(item.getPlaylistid());
			mOnlineRankListAdapter.deleteItem(arg1);
			changeViewState(list.size());
		}

	};

	private AuroraBackOnClickListener mAuroraBackOnClickListener = new AuroraBackOnClickListener() {

		@Override
		public void auroraDragedSuccess(int arg0) {

		}

		@Override
		public void auroraDragedUnSuccess(int arg0) {

		}

		@Override
		public void auroraOnClick(int arg0) {
			showDeleteDialog();
		}

		@Override
		public void auroraPrepareDraged(int arg0) {

		}

	};

	@Override
	protected void onPause() {
		mListView.auroraOnPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mListView.auroraOnResume();
		new RankDataTask().executeOnExecutor(ThreadPoolExecutorUtils.getThreadPoolExecutor().getExecutor());
	}

	@Override
	protected void onStart() {
		super.onStart();
		setPlayAnimation();
		IntentFilter f = new IntentFilter();
		f.addAction(MediaPlaybackService.PLAYSTATE_CHANGED);
		f.addAction(MediaPlaybackService.META_CHANGED);
		registerReceiver(mStatusListener, new IntentFilter(f));
	}

	@Override
	protected void onStop() {
		if (isPlaying) {
			playView.clearAnimation();
			playView.setBackgroundResource(R.drawable.aurora_left_bar_clicked);
			isPlaying = false;
		}
		unregisterReceiver(mStatusListener);
		super.onStop();
	}

	/**
	 * 设置播放动画
	 */
	private void setPlayAnimation() {
		try {
			if (MusicUtils.sService != null) {
				LogUtil.d(TAG, "isplaying:" + MusicUtils.sService.isPlaying());
				if (MusicUtils.sService.isPlaying()) {
					if (!isPlaying) {
						playView.startAnimation(operatingAnim);
						playView.setBackgroundResource(android.R.color.transparent);
						isPlaying = true;
					}
				} else if (isPlaying) {
					playView.clearAnimation();
					playView.setBackgroundResource(R.drawable.aurora_left_bar_clicked);
					isPlaying = false;
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			if (isPlaying) {
				playView.clearAnimation();
				playView.setBackgroundResource(R.drawable.aurora_left_bar_clicked);
				isPlaying = false;
			}
		}
	}

	private BroadcastReceiver mStatusListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(MediaPlaybackService.PLAYSTATE_CHANGED)) {
				LogUtil.d(TAG, "--------mStatusListener:");
				setPlayAnimation();

			}
		}
	};

	private OnAuroraActionBarItemClickListener mOnAuroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		@Override
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {
			case PLAY_BUTTON:

				Intent intent = new Intent();
				intent.setClass(AuroraRankList.this, AuroraPlayerActivity.class);
				startActivity(intent);
//				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
				break;
			}
		}

	};

	class RankDataTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			if (MusicUtils.mSongDb != null) {
				list = MusicUtils.mSongDb.queryCollectInfo();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (list == null) {
				return;
			}
			mOnlineRankListAdapter.addDatas(list);
			changeViewState(list.size());
		}

		@Override
		protected void onPreExecute() {

		}

	}

	private void changeViewState(int size) {
		if (size > 0) {
			noPlaylist.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
		} else {
			noPlaylist.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		AuroraCollectPlaylist item = (AuroraCollectPlaylist) arg0.getAdapter().getItem(arg2);
		if (item == null) {
			return;
		}
		switch (item.getListType()) {
		case 0:
			Intent intent = new Intent(this, AuroraNetTrackDetailActivity.class);
			intent.putExtra("tag", OnGridViewClickListener.NEW_ALBUM);
			intent.putExtra("title", item.getPlaylistname());
			intent.putExtra(AuroraNetTrackDetail.ID, item.getPlaylistid());
			intent.putExtra("imageUrl", item.getImgUrl());
			intent.putExtra("artist", item.getInfo());
			startActivity(intent);
			break;
		case 1:
			Intent intent2 = new Intent(this, AuroraNetTrackDetailActivity.class);
			intent2.putExtra("tag", OnGridViewClickListener.RECOMMEND_PLAYLIST);
			intent2.putExtra(AuroraNetTrackDetail.ID, item.getPlaylistid());
			intent2.putExtra("imageUrl", item.getImgUrl());
			intent2.putExtra("title", item.getPlaylistname());
			intent2.putExtra("playlist_tag", item.getInfo());
			startActivity(intent2);
			break;
		case 2:
			Intent intent3 = new Intent(this, AuroraNetTrackDetailActivity.class);
			intent3.putExtra("tag", OnGridViewClickListener.RANKING);
			intent3.putExtra(AuroraNetTrackDetail.ID, item.getPlaylistid());
			intent3.putExtra("title", item.getPlaylistname());
			startActivity(intent3);
			break;
		case 3:
			Intent intent4 = new Intent(this, AuroraNetTrackDetailActivity.class);
			intent4.putExtra("tag", OnGridViewClickListener.BANNER);
			intent4.putExtra("type", item.getType());
			intent4.putExtra(AuroraNetTrackDetail.ID, item.getPlaylistid());
			intent4.putExtra("title", item.getPlaylistname());
			intent4.putExtra("imageUrl", item.getImgUrl());
			startActivity(intent4);
			break;
		}
	}

	private void showDeleteDialog() {
		if (mAuroraAlertDialog == null) {
			AuroraAlertDialog.Builder build = new AuroraAlertDialog.Builder(this).setTitle(R.string.aurora_cancel_collection).setMessage(R.string.aurora_cancel_collection_message)
					.setNegativeButton(R.string.cancel, new OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {

							if (mListView.auroraIsRubbishOut()) {
								mListView.auroraSetRubbishBack();
								// mListView.invalidateViews();
							}
						}
					}).setPositiveButton(R.string.ok, new OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {

							mListView.auroraDeleteSelectedItemAnim();
						}
					});
			mAuroraAlertDialog = build.create();
		}
		mAuroraAlertDialog.show();

	}

	@Override
	public void onBackPressed() {

		if (mListView.auroraIsRubbishOut()) {
			mListView.auroraSetRubbishBack();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
	}
}
