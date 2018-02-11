package com.android.auroramusic.ui;

// import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.android.auroramusic.model.AuroraLoadingListener;
import com.android.auroramusic.util.LogUtil;
import com.android.music.MusicUtils;
import com.android.music.MusicUtils.DbChangeListener;
import com.android.music.R;

abstract public class AbstractBaseActivity extends BasicActivity implements DbChangeListener {

	private static final String TAG = "AbstractBaseActivity";
	private static final int AURORA_MSG_UPDATE_DB = 150;
	private static final int AURORA_MSG_NEEDUPDATE_DB = 151;

	abstract public void onMediaDbChange(boolean selfChange);

	private FrameLayout mContentContainer;
	private View mFloatView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		ViewGroup mDecorView = (ViewGroup) getWindow().getDecorView();
//		mContentContainer = (FrameLayout) ((ViewGroup) mDecorView.getChildAt(0)).getChildAt(1);
//		mFloatView = LayoutInflater.from(getBaseContext()).inflate(R.layout.aurora_music_statusbar, null);
//		mFloatView.setBackgroundColor(Color.GREEN);
		boolean flag = MusicUtils.registerDbObserver(getApplicationContext(), this);
		if (MusicUtils.mSongDb != null) {
			MusicUtils.mSongDb.setLoadingListener(mLoadingListener);
		}

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

//		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//		layoutParams.gravity = Gravity.BOTTOM;
//		mContentContainer.addView(mFloatView, layoutParams);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (MusicUtils.mSongDb != null) {
			MusicUtils.mSongDb.removesetLoadingListener(mLoadingListener);
		}
		mUiHandler.removeMessages(AURORA_MSG_UPDATE_DB);
	}
	@Override
	public void finish() {
		super.finish();
//		overridePendingTransition(0, 0);
	}

	@Override
	public void startActivity(Intent intent) {
//		intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		super.startActivity(intent);
	}

	private final AuroraLoadingListener mLoadingListener = new AuroraLoadingListener() {

		@Override
		public void onNotNeedLoading() {
		}

		@Override
		public void onNeedLoading() {
			LogUtil.d(TAG, " ----- onNeedLoading mLoadingListener");
			onMediaDbChange(false);
		}
	};

	@Override
	public void onDbChanged(boolean selfChange) {
		if (mUiHandler != null) {
			mUiHandler.removeMessages(AURORA_MSG_UPDATE_DB);
			mUiHandler.sendMessage(mUiHandler.obtainMessage(AURORA_MSG_UPDATE_DB, selfChange));
		}
	}

	private final Handler mUiHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AURORA_MSG_UPDATE_DB:
				boolean selfChange = ((Boolean) msg.obj).booleanValue();
				// Log.i(TAG,
				// "zll ----- AbstractBaseActivity handleMessage selfChange:"+selfChange);
				if (MusicUtils.mSongDb != null) {
					MusicUtils.mSongDb.onContentDirty();
				}

				break;

			case AURORA_MSG_NEEDUPDATE_DB:
				break;

			default:
				break;
			}
		}

	};

	/*
	 * private final ContentObserver mAuroraObserver = new ContentObserver(null)
	 * {
	 * 
	 * @Override public void onChange(boolean selfChange) { if (mUiHandler !=
	 * null) { mUiHandler.removeMessages(AURORA_MSG_UPDATE_DB);
	 * mUiHandler.sendMessage(mUiHandler.obtainMessage(AURORA_MSG_UPDATE_DB,
	 * selfChange) ); }
	 * 
	 * } };
	 */
}
