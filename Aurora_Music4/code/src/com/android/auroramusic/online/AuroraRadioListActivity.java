package com.android.auroramusic.online;

import java.util.ArrayList;
import java.util.List;

import com.android.auroramusic.adapter.AuroraRadioAdapter;
import com.android.auroramusic.model.XiaMiSdkUtils;
import com.android.auroramusic.ui.AuroraPlayerActivity;
import com.android.auroramusic.ui.BasicActivity;
import com.android.auroramusic.util.AuroraListItem;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.FlowTips;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.LogUtil;
import com.android.auroramusic.util.FlowTips.OndialogClickListener;
import com.android.music.Application;
import com.android.music.MediaPlaybackService;
import com.android.music.MusicUtils;
import com.xiami.music.model.RadioCategory;
import com.xiami.music.model.RadioInfo;
import com.xiami.sdk.callback.OnlineSongsCallback;
import com.xiami.sdk.entities.OnlineSong;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraListView;

import com.android.music.R;

public class AuroraRadioListActivity extends BasicActivity {

	private static final String TAG = "AuroraRadioListActivity";

	private AuroraActionBar mAuroraActionBar;
	private RadioCategory mRadioCategory;

	private static final int PLAY_BUTTON = 0;
	private Animation operatingAnim; // 播放按钮动画
	private boolean isPlaying = false; // 动画是否在运行
	private View playView; // 播放按钮

	private AuroraListView mAuroraListView;
	private AuroraRadioAdapter mAuroraRadioAdapter;
	private int mType = 0;

	private ObjectAnimator aima;
	private boolean isShowAnimator = false;
	private ImageView mPlaySelect;
	private int[] flyendPoint = new int[2];
	private int[] flystartPoint = new int[2];
	private boolean isStop; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.radio_layout);
		Intent intent = getIntent();
		mType = intent.getIntExtra("type", 0);
		LogUtil.d(TAG, "type:" + mType);
		if (Application.mRadioCategories != null
				&& mType < Application.mRadioCategories.size()) {
			mRadioCategory = Application.mRadioCategories.get(mType);
		} else {
			LogUtil.d(TAG, "get radio error!");
			finish();
			return;
		}
		initview();
		initactionbar();
	}

	private void initactionbar() {
		mAuroraActionBar = getAuroraActionBar();
		try {
			mAuroraActionBar.setTitle(mRadioCategory.getTypeName()
					+ getString(R.string.radio));
			mAuroraActionBar
					.addItem(R.drawable.song_playing, PLAY_BUTTON, null);
			playView = mAuroraActionBar.getItem(PLAY_BUTTON).getItemView();
			operatingAnim = AnimationUtils.loadAnimation(this,
					R.anim.rotate_anim);
			LinearInterpolator lin = new LinearInterpolator();
			operatingAnim.setInterpolator(lin);
			mAuroraActionBar
					.setOnAuroraActionBarListener(mOnAuroraActionBarItemClickListener);
		} catch (Exception e) {

		}
	}

	private void initview() {

		mPlaySelect = (ImageView) findViewById(R.id.id_song_selected);
		mAuroraListView = (AuroraListView) findViewById(R.id.aurora_id_list);
		mAuroraRadioAdapter = new AuroraRadioAdapter(this,
				mRadioCategory.getRadios(),mType);
		mAuroraListView.setAdapter(mAuroraRadioAdapter);

		mAuroraListView.setOnItemClickListener(mOnItemClickListener);
	}

	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapterview, View view, int i,
				long l) {
			final int position = i;
			
			if (!AuroraMusicUtil.isNetWorkActive(AuroraRadioListActivity.this)) {
				Toast.makeText(AuroraRadioListActivity.this, R.string.aurora_network_error,
						Toast.LENGTH_SHORT).show();
				return;
			}
			
			if (position >= 0) {

				boolean isshow = FlowTips.showPlayFlowTips(
						AuroraRadioListActivity.this,
						new OndialogClickListener() {

							@Override
							public void OndialogClick() {
								startPlayAnimation(position);
							}
						});
				if (isshow) {
					return;
				}
				startPlayAnimation(position);
			}
		}
	};

	private OnAuroraActionBarItemClickListener mOnAuroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		@Override
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {
			case PLAY_BUTTON:
				Intent intent = new Intent(AuroraRadioListActivity.this,
						AuroraPlayerActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_right_in,
						R.anim.slide_left_out);
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
				LogUtil.d(TAG, "--------PLAYSTATE_CHANGED:");

				setPlayAnimation();

			} else if (action.equals(MediaPlaybackService.META_CHANGED)) {
				LogUtil.d(TAG, "-----META_CHANGED");
			}
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		isStop =false;
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
		isStop =true;
		unregisterReceiver(mStatusListener);
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setPlayPosition();
	}

	private void setPlayPosition() {
		if (Application.mRadiotype == mType && mAuroraRadioAdapter != null) {
			mAuroraRadioAdapter.setPlayingPosition(Application.mRadioPosition);
			mAuroraRadioAdapter.notifyDataSetChanged();
		}else {
			mAuroraRadioAdapter.setPlayingPosition(-1);
			mAuroraRadioAdapter.notifyDataSetChanged();
		}
	}

	public void startPlayAnimation(final int position) {

		if (aima != null && aima.isStarted() && mAuroraRadioAdapter != null) {
			mAuroraRadioAdapter.setPlayingPosition(position);
			aima.end();
		}

		int[] location = new int[2];
		int[] location1 = new int[2];
		int[] location2 = new int[2];
		int distance = 0; // 移动距离
		mAuroraListView.getLocationInWindow(location);
		int currentPosition = mAuroraRadioAdapter.getCurrentPlayPosition();
		View arg1 = mAuroraListView.getChildAt(position
				- mAuroraListView.getFirstVisiblePosition());
		if (arg1 == null) {
			mAuroraRadioAdapter.setPlayingPosition(position);
			mAuroraListView.invalidateViews();
			startPlayRadio(position);
			return;
		}
		arg1.getLocationInWindow(location1);
		flystartPoint = location1;
		if (currentPosition < 0) {
			// 无动画
			mAuroraRadioAdapter.setPlayingPosition(position);
			mAuroraListView.invalidateViews();
			startPlayRadio(position);
			startFly();
			return;
		} else if (currentPosition < mAuroraListView.getFirstVisiblePosition()) {
			// 从最上面飞进来
			mPlaySelect.setY(-mPlaySelect.getHeight());
			distance = location1[1] - location[1] + mPlaySelect.getHeight();
		} else if (currentPosition > mAuroraListView.getLastVisiblePosition()) {
			// 从最下面飞进来
			mPlaySelect.setY(mAuroraListView.getHeight());
			distance = mAuroraListView.getHeight() - location1[1] + location[1];
		} else {
			// 具体位置飞进
			View view = mAuroraListView.getChildAt(currentPosition
					- mAuroraListView.getFirstVisiblePosition());
			view.getLocationInWindow(location2);
			mPlaySelect.setY(location2[1] - location[1]);
			distance = Math.abs(location2[1] - location1[1]);
		}

		aima = ObjectAnimator.ofFloat(mPlaySelect, "y", location1[1]
				- location[1]);
		aima.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator arg0) {
				mPlaySelect.setVisibility(View.VISIBLE);
				mAuroraRadioAdapter.setPlayingPosition(-1);
				mAuroraListView.invalidateViews();
				isShowAnimator = true;
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				isShowAnimator = false;
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						mAuroraRadioAdapter.setPlayingPosition(position);
						mAuroraListView.invalidateViews();
						mPlaySelect.setVisibility(View.GONE);
						startPlayRadio(position);
						startFly();
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

	private Handler mHandler = new Handler();

	private void startPlayRadio(int posion) {
		Application.setRadio(mType, posion);
		((Application) getApplication()).startPlayRadio(posion, mHandler, this);
	}
	
	private void startFly() {
		if(isStop)
			return;
		playView.getLocationInWindow(flyendPoint);
		AuroraMusicUtil.startFly(
				AuroraRadioListActivity.this,
				flystartPoint[0], flyendPoint[0],
				flystartPoint[1], flyendPoint[1],true);
	}
}
