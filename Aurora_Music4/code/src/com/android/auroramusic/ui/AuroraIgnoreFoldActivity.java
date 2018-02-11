package com.android.auroramusic.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;

import com.android.auroramusic.adapter.AuroraFoldListAdapter;
import com.android.auroramusic.adapter.AuroraFoldListAdapter.onRetainClickListener;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.LogUtil;
import com.android.music.MediaPlaybackService;
import com.android.music.MusicUtils;
import com.android.music.R;

public class AuroraIgnoreFoldActivity extends AuroraActivity implements LoaderCallbacks<Cursor>, OnItemClickListener {

	private static final String TAG = "AuroraIgnoreFoldActivity";
	private AuroraActionBar mAuroraActionBar;
	private static final int PLAY_BUTTON = 0;
	private Animation operatingAnim; // 播放按钮动画
	private boolean isPlaying = false; // 动画是否在运行
	private View playView; // 播放按钮
	private Button mRegainButton;
	private TextView totalFold;
	private AuroraListView mAuroraListView;
	private View mNoIgnoreFold;
	private List<String> mIgnorPathList = null;
	private int totalSongs = 0;
	private int totalFolds = 0;
	private AuroraFoldListAdapter mAuroraFoldListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.aurora_fold_main);

		try {
			initActionBar();
			initView();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initView() {

		mRegainButton = (Button) findViewById(R.id.aurora_ignore_button);
		totalFold = (TextView) findViewById(R.id.aurora_fold_total);
		mAuroraListView = (AuroraListView) findViewById(R.id.aurora_fold);
		mNoIgnoreFold = findViewById(R.id.aurora_no_ignore_fold);
		mRegainButton.setText(getString(R.string.aurora_all_regain));

		mAuroraFoldListAdapter = new AuroraFoldListAdapter(this, monRetainClickListener);
		mAuroraListView.setAdapter(mAuroraFoldListAdapter);
		mAuroraListView.setOnItemClickListener(this);

		mRegainButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				totalFolds = 0;
				totalSongs = 0;
				mIgnorPathList.clear();
				AuroraMusicUtil.writeToXml(AuroraIgnoreFoldActivity.this, AuroraMusicUtil.writeToString(mIgnorPathList), "paths.xml");
				AuroraMusicUtil.getMountedStorage(AuroraIgnoreFoldActivity.this);
				changeViewState();
			}
		});
		getLoaderManager().initLoader(0, null, this);
	}

	private void initActionBar() {
		try {
			mAuroraActionBar = getAuroraActionBar();
			mAuroraActionBar.setTitle(R.string.aurora_is_ignored);
			mAuroraActionBar.addItem(R.drawable.song_playing, PLAY_BUTTON, null);
			playView = mAuroraActionBar.getItem(PLAY_BUTTON).getItemView();
			operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_anim);
			LinearInterpolator lin = new LinearInterpolator();
			operatingAnim.setInterpolator(lin);
			mAuroraActionBar.setOnAuroraActionBarListener(mOnAuroraActionBarItemClickListener);

		} catch (Exception e) {

		}
	}

	private OnAuroraActionBarItemClickListener mOnAuroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		@Override
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {
			case PLAY_BUTTON:

				Intent intent = new Intent(AuroraIgnoreFoldActivity.this, AuroraPlayerActivity.class);
				startActivity(intent);
//				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
				break;
			}
		}
	};

	/**
	 * 设置播放动画
	 */
	private void setPlayAnimation() {
		try {
			if (playView == null) {
				return;
			}
			if (MusicUtils.sService != null) {
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
				setPlayAnimation();
			} else if (action.equals(MediaPlaybackService.META_CHANGED)) {

			}
		}
	};

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
		if (isPlaying && playView != null) {
			playView.clearAnimation();
			playView.setBackgroundResource(R.drawable.aurora_left_bar_clicked);
			isPlaying = false;
		}
		unregisterReceiver(mStatusListener);
		super.onStop();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		StringBuilder where = new StringBuilder();
		where.append(Globals.QUERY_SONG_FILTER_1);
		String[] cons = new String[] { MediaStore.Audio.Media.DATA };
		return new CursorLoader(this, uri, cons, where.toString(), null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {

		List<String> list = new ArrayList<String>();
		mIgnorPathList = AuroraMusicUtil.doParseXml(this, "paths.xml");
		if (cursor != null && mIgnorPathList != null) {
			totalSongs = cursor.getCount();

			if (cursor.moveToFirst()) {
				do {
					String path = cursor.getString(0);
					path = path.substring(0, path.lastIndexOf("/"));
					if (!list.contains(path) && mIgnorPathList.contains(path)) {
						list.add(path);
					}
					// 去掉不是忽略文件夹中的歌曲数
					if (!mIgnorPathList.contains(path)) {
						totalSongs--;
					}
				} while (cursor.moveToNext());
			}
			totalFolds = mIgnorPathList.size();
		}
		mAuroraFoldListAdapter.addFoldData(mIgnorPathList);
		changeViewState();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void changeViewState() {
		totalFold.setText(AuroraMusicUtil.getTransColorText(AuroraIgnoreFoldActivity.this, totalFolds, totalSongs));
		if (totalFolds > 0) {
			mNoIgnoreFold.setVisibility(View.GONE);
			mAuroraListView.setVisibility(View.VISIBLE);
			mRegainButton.setEnabled(true);
		} else {
			mNoIgnoreFold.setVisibility(View.VISIBLE);
			mAuroraListView.setVisibility(View.GONE);
			mRegainButton.setEnabled(false);
		}

	}

	private onRetainClickListener monRetainClickListener = new onRetainClickListener() {

		@Override
		public void onRetainClick(String path) {
			if (mIgnorPathList != null) {
				mIgnorPathList.remove(path);
				AuroraMusicUtil.writeToXml(AuroraIgnoreFoldActivity.this, AuroraMusicUtil.writeToString(mIgnorPathList), "paths.xml");

				AuroraMusicUtil.getMountedStorage(AuroraIgnoreFoldActivity.this);// lory
																					// add
																					// 2014.9.2

				getLoaderManager().restartLoader(0, null, AuroraIgnoreFoldActivity.this);
			}
		}
	};

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

		String path = (String) arg0.getAdapter().getItem(arg2);
		Intent intent = new Intent(this, AuroraTrackBrowserActivity.class);
		intent.putExtra(AuroraFoldActivity.EXTR_FOLD_START_MODE, 1);
		intent.putExtra(AuroraFoldActivity.EXTR_FOLD_PATH, path);
		startActivity(intent);
	}
}
