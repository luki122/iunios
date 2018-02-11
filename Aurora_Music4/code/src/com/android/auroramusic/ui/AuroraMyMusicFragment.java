package com.android.auroramusic.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.app.Dialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.PlaylistsColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraEditText;
import aurora.widget.AuroraSystemMenu;

import com.android.auroramusic.adapter.AuroraMainMenuAdapter;
import com.android.auroramusic.online.AuroraMusicDownloadManager;
import com.android.auroramusic.online.AuroraRankList;
import com.android.auroramusic.util.AuroraMainMenuData;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.LogUtil;
import com.android.auroramusic.util.ReportUtils;
import com.android.auroramusic.util.ThreadPoolExecutorUtils;
import com.android.music.Application;
import com.android.music.MusicUtils;
import com.android.music.R;

public class AuroraMyMusicFragment implements OnItemClickListener, OnClickListener, MusicUtils.Defs {

	private static final String TAG = "AuroraMyMusicFragment";
	private static final float HIDE_HEAED_RATE = 2.7f;// 2.7
	private static final int HIDE_HEAD_SLEEP = 7;
	private static final int TOP_REBOUND_SLEEP = 13;

	private FrameLayout.LayoutParams mLayoutParams;
	private AuroraMainMenuAdapter mAuroraMainMenuAdapter;
	private int paramsmarginsize = 0;
	private ListView firstMain;
	private View headView;
	private ImageView myMusicTopbar;
	private int bgHeight = 0;
	private int bgWidth = 0;
	private Matrix matrix = new Matrix();
	private float yDown = 0, yDown1 = 0;
	private boolean ableToPull = false;
	/** 惯性回弹动画是否可运行，true 可运行，false 不可运行。 */
	private boolean mThreadState = true;
	private boolean mRunState = true; // 用于判断是否在滑动
	private int mTime = 15;
	/** 按下的时间 */
	// long mStartTime = 0;
	/** 记录按下时滚动条位置 */
	int startPosition = 0;
	private VelocityTracker mVelocityTracker;
	private int mVelocitY; // 记录滚动速度
	private Context mContext;
	private boolean isdownOnTop = true;
	private Dialog mDialog;
	private AuroraEditText inputName;
	private boolean isFingerUp = false;
	private int myfavoriteId = -2;
	private boolean isclicked = false;
	private ImageView mAudioButtonAnim, mArtistButtonAnim, mFoldButtonAnim, mAudioButtonPress, mArtistButtonPress, mFoldButtonPress;
	private Bitmap musicBg;// add by JXH

	public void initview(View view, Context context) {
		mContext = context;
		firstMain = (ListView) view.findViewById(R.id.id_first_main);
		mAuroraMainMenuAdapter = new AuroraMainMenuAdapter(context);

		// 添加listvew head
		headView = LayoutInflater.from(context).inflate(R.layout.aurora_mymusic_top_layout, null);
		myMusicTopbar = (ImageView) headView.findViewById(R.id.id_my_music_topbar);

		firstMain.addHeaderView(headView);
		firstMain.setAdapter(mAuroraMainMenuAdapter);
		firstMain.setOnItemClickListener(this);
		// add by JXH begin
		if (musicBg == null) {
			musicBg = BitmapFactory.decodeResource(context.getResources(), R.drawable.my_music_bg);
		}
		// add by JXH end
		// 设置按钮监听
		headView.findViewById(R.id.id_songs_btn).setOnClickListener(this);
		headView.findViewById(R.id.id_singer_btn).setOnClickListener(this);
		headView.findViewById(R.id.id_fold_btn).setOnClickListener(this);
		mAudioButtonAnim = (ImageView) headView.findViewById(R.id.id_song_btn_press_anim);
		mAudioButtonPress = (ImageView) headView.findViewById(R.id.id_song_btn_press);
		mArtistButtonAnim = (ImageView) headView.findViewById(R.id.id_singer_btn_press_anim);
		mArtistButtonPress = (ImageView) headView.findViewById(R.id.id_singer_btn_press);
		mFoldButtonAnim = (ImageView) headView.findViewById(R.id.id_fold_btn_press_anim);
		mFoldButtonPress = (ImageView) headView.findViewById(R.id.id_fold_btn_press);

		paramsmarginsize = context.getResources().getDimensionPixelSize(R.dimen.aurora_my_music_page_zoom_size);
		bgHeight = context.getResources().getDimensionPixelSize(R.dimen.aurora_my_music_toplayout_height);
		bgWidth = context.getResources().getDimensionPixelSize(R.dimen.aurora_my_music_toplayout_width);
		firstMain.setOnTouchListener(mOnTouchListener);
		firstMain.setOnScrollListener(mOnScrollListener);
		mLayoutParams = (FrameLayout.LayoutParams) myMusicTopbar.getLayoutParams();
	}

	private OnTouchListener mOnTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {

			if (event.getPointerCount() > 1) {
				return true;
			}
			// 正在播放动画禁用系统touch
			if (!mThreadState) {
				return true;
			}

			if (!ableToPull) {
				yDown = event.getRawY();
			}
			// 获得动作捕捉器
			if (mVelocityTracker == null) {
				mVelocityTracker = VelocityTracker.obtain();
			}
			mVelocityTracker.addMovement(event);// 开始捕捉动作
			mVelocityTracker.computeCurrentVelocity(1);
			int velocitY = (int) mVelocityTracker.getYVelocity();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				yDown = event.getRawY();
				startPosition = (int) event.getRawY();
				if (headView.getTop() == 0) {
					isdownOnTop = true;
				} else {
					isdownOnTop = false;
				}
				mVelocitY = 0;
				break;
			case MotionEvent.ACTION_UP:
				if (ableToPull && !mRunState) {
					float yMove = event.getRawY();
					float distance = yMove - yDown1;
					if (yDown1 == 0) {
						distance = 0;
					}
					hideHead(distance);
				}
				isFingerUp = true;
				mRunState = true;

				mVelocitY = Math.abs(velocitY);

				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}

				// ableToPull=false;
				yDown = 0;
				yDown1 = 0;
				break;
			case MotionEvent.ACTION_MOVE:
				// LogUtil.d(TAG, "-------------------->velocitY:" + mVelocitY);
				if (mVelocitY < 1 || isdownOnTop) {
					mRunState = false;
				} else {
					mRunState = true;
					yDown = 0;
					break;
				}

				if (isScrollTop()) {

					float yMove = event.getRawY();
					// 防止yDown为0的情况
					if (yDown == 0) {
						yDown = yMove;
					}
					float distance = yMove - yDown - 60;

					mLayoutParams.bottomMargin = paramsmarginsize + (int) (distance / HIDE_HEAED_RATE);
					mLayoutParams.topMargin = paramsmarginsize + (int) (distance / HIDE_HEAED_RATE);

					if (mLayoutParams.bottomMargin >= 0) {
						if (yDown1 == 0) {
							yDown1 = yMove;
						}
						float distance1 = yMove - yDown1;
						float scale1 = (distance1 / HIDE_HEAED_RATE + bgHeight) / bgHeight;
						mLayoutParams.bottomMargin = 0;
						mLayoutParams.topMargin = 0;
						// 防止缩小超过原始大小
						if (bgHeight * scale1 < bgHeight) {
							scale1 = 1;
						}
						// matrix.setScale(scale1, scale1, bgWidth / 2, 0);
						// myMusicTopbar.setImageMatrix(matrix);
						myMusicTopbar.setScaleX(scale1);
						myMusicTopbar.setScaleY(scale1);
						mLayoutParams.height = (int) (bgHeight * scale1);
					} else {
						yDown1 = 0;// 防止卡顿
					}

					if (isHeadMove()) {
						// LogUtil.d(TAG, "禁用系统滚动");
						myMusicTopbar.setLayoutParams(mLayoutParams);
						return true;
					} else {
						mLayoutParams.bottomMargin = paramsmarginsize;
						mLayoutParams.topMargin = paramsmarginsize;
					}

				} else {

				}
				break;
			}

			return false;
		}
	};

	private boolean isHeadMove() {
		return mLayoutParams.bottomMargin > paramsmarginsize ? true : false;
	}

	private OnScrollListener mOnScrollListener = new OnScrollListener() {

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

			// if (!ableToPull) {
			if (isScrollTop() && isFingerUp) {
				isFingerUp = false;

				if (!mThreadState) {
					return;
				}
				if (startPosition != 0 && firstMain != null && !isdownOnTop) {
					LogUtil.d(TAG, "initTopAnimator...");
					initTopAnimator();
					startPosition = 0;
					// }
				}
			}

		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

			// LogUtil.d(TAG, "---------------->scrollState:" + scrollState);
		}

	};

	private boolean isScrollTop() {

		if (headView.getTop() == 0) {
			firstMain.setOverScrollMode(View.OVER_SCROLL_NEVER);
			ableToPull = true;
			return true;
		}
		ableToPull = false;
		firstMain.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		AuroraMainMenuData info = (AuroraMainMenuData) arg0.getAdapter().getItem(arg2);

		if (isHeadMove() || isclicked) {
			return;
		}
		LogUtil.d(TAG, "---arg2:"+arg2);
		if (arg2 == 1) {
			initCreatePlayList();
		} else if (arg2 == 2) {
			Intent intent = new Intent();
			intent.setClass(mContext, AuroraSongSingle.class);
			info.setPlaylistId(myfavoriteId);
			intent.putExtra(AuroraSongSingle.EXTR_PLAYLIST_INFO, info);
			intent.putExtra(AuroraSongSingle.EXTR_PLAYLIST_START_MODE, 2);
			mContext.startActivity(intent);
		} else if (arg2 == 3) {
			Intent intent = new Intent();
			intent.setClass(mContext, AuroraSongSingle.class);
			info.setPlaylistId(-1);
			intent.putExtra(AuroraSongSingle.EXTR_PLAYLIST_INFO, info);
			intent.putExtra(AuroraSongSingle.EXTR_PLAYLIST_START_MODE, 1);
			mContext.startActivity(intent);
		} else if (arg2 == 4 && Globals.SWITCH_FOR_ONLINE_MUSIC) {
			Intent intent = new Intent(mContext, AuroraMusicDownloadManager.class);
			mContext.startActivity(intent);
		} else if (arg2 == 5 && Globals.SWITCH_FOR_ONLINE_MUSIC) {
			Intent intent = new Intent(mContext, AuroraRankList.class);
			mContext.startActivity(intent);
		} else if (Globals.NO_MEUN_KEY&&arg2 == 6) {
			AuroraMediaPlayHome activity = (AuroraMediaPlayHome) mContext;
			activity.addMusicCoverView();
			AuroraSystemMenu systemMenu = ((AuroraMediaPlayHome)mContext).getAuroraMenu();
			if (!Globals.STORAGE_PATH_SETTING) {
				systemMenu.removeMenuByItemId(R.id.aurora_storage_setting);
				return;
			}
			if (!((Application) mContext.getApplicationContext()).isHaveSdStorage()) {
				systemMenu.setMenuItemEnable(R.id.aurora_storage_setting, false);
			} else {
				systemMenu.setMenuItemEnable(R.id.aurora_storage_setting, true);
			}
			activity.showAuroraMenu(activity.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
		} else {
			Intent intent = new Intent();
			intent.setClass(mContext, AuroraSongSingle.class);
			intent.putExtra(AuroraSongSingle.EXTR_PLAYLIST_INFO, info);
			mContext.startActivity(intent);
		}

	}

	@Override
	public void onClick(View arg0) {
		if (isclicked) {
			return;
		}
		isclicked = true;
		switch (arg0.getId()) {
		case R.id.id_songs_btn:
			showButtonAnim(mAudioButtonPress, mAudioButtonAnim, AuroraTrackBrowserActivity.class);
			break;
		case R.id.id_singer_btn:
			showButtonAnim(mArtistButtonPress, mArtistButtonAnim, AuroraArtistBrowserActivity.class);
			break;
		case R.id.id_fold_btn:
			showButtonAnim(mFoldButtonPress, mFoldButtonAnim, AuroraFoldActivity.class);
			break;
		}
	}

	private void showButtonAnim(final View pressView, final View animView, final Class<?> cls) {

		AnimatorSet mAnimatorSet = new AnimatorSet();
		AnimatorSet animatorset1 = new AnimatorSet();
		ObjectAnimator objanimator1 = ObjectAnimator.ofFloat(pressView, "alpha", new float[] { 1, 0.1f });
		ObjectAnimator objanimator2 = ObjectAnimator.ofFloat(animView, "scaleX", new float[] { 0.2f, 1 });
		ObjectAnimator objanimator3 = ObjectAnimator.ofFloat(animView, "scaleY", new float[] { 0.2f, 1 });
		ObjectAnimator objanimator4 = ObjectAnimator.ofFloat(animView, "alpha", new float[] { 0, 0.4f });
		ObjectAnimator objanimator5 = ObjectAnimator.ofFloat(animView, "alpha", new float[] { 0.4f, 0f });

		objanimator1.setDuration(240);
		objanimator2.setDuration(240);
		objanimator3.setDuration(240);
		objanimator4.setDuration(80);
		objanimator5.setDuration(160);
		objanimator1.setInterpolator(new LinearInterpolator());
		objanimator2.setInterpolator(new DecelerateInterpolator());
		objanimator3.setInterpolator(new DecelerateInterpolator());
		objanimator4.setInterpolator(new LinearInterpolator());
		objanimator5.setInterpolator(new DecelerateInterpolator());
		animatorset1.play(objanimator5).after(objanimator4);
		mAnimatorSet.playTogether(objanimator1, objanimator2, objanimator3, animatorset1);

		mAnimatorSet.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator arg0) {

				pressView.setVisibility(View.VISIBLE);
				animView.setVisibility(View.VISIBLE);

			}

			@Override
			public void onAnimationRepeat(Animator arg0) {

			}

			@Override
			public void onAnimationEnd(Animator arg0) {

				Intent intent = new Intent(mContext, cls);
				mContext.startActivity(intent);
				pressView.setVisibility(View.GONE);
				animView.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationCancel(Animator arg0) {

			}
		});
		// mAnimatorSet.setInterpolator(new DecelerateInterpolator());
		mAnimatorSet.start();

	}

	private void hideHead(float distance) {
		LogUtil.d(TAG, "distance:" + distance);
		AnimatorSet scanAnimate = new AnimatorSet();
		AnimatorSet animatorSet = new AnimatorSet();

		ObjectAnimator margin = ObjectAnimator.ofInt(myMusicTopbar, "margin", new int[] { mLayoutParams.bottomMargin, paramsmarginsize });
		margin.setDuration(150);
		if (distance > 0) {
			float scale = (distance / HIDE_HEAED_RATE + bgHeight) / bgHeight;
			ObjectAnimator scanX = ObjectAnimator.ofFloat(myMusicTopbar, "scaleX", new float[] { scale, 1 });
			ObjectAnimator scanY = ObjectAnimator.ofFloat(myMusicTopbar, "scaleY", new float[] { scale, 1 });
			ObjectAnimator heightAnim = ObjectAnimator.ofInt(myMusicTopbar, "hight", new int[] { (int) (bgHeight * scale), bgHeight });
			scanAnimate.playTogether(scanX, scanY, heightAnim);
			if (distance < 200) {
				scanAnimate.setDuration(150);
			} else {
				scanAnimate.setDuration(250);
			}
			animatorSet.setInterpolator(new LinearInterpolator());
			animatorSet.play(margin).after(scanAnimate);
		} else {
			animatorSet.play(margin);
		}
		animatorSet.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animator) {
				mThreadState = false;
				LogUtil.d(TAG, "onAnimationStart");

			}

			@Override
			public void onAnimationRepeat(Animator animator) {
				LogUtil.d(TAG, "onAnimationRepeat");
			}

			@Override
			public void onAnimationEnd(Animator animator) {
				mThreadState = true;
				LogUtil.d(TAG, "onAnimationEnd");

			}

			@Override
			public void onAnimationCancel(Animator animator) {
				LogUtil.d(TAG, "onAnimationCancel");
			}
		});
		animatorSet.start();
	}

	private ScanPlaylistTask scanPlaylistTask;

	public void onResume() {
		isclicked = false;
		// mAuroraMainMenuAdapter.notifyDataSetChanged();
		if (scanPlaylistTask != null) {
			if (scanPlaylistTask.getStatus() != AsyncTask.Status.FINISHED) {
				scanPlaylistTask.cancel(true);
			}
		}
		scanPlaylistTask = new ScanPlaylistTask();
		// scanPlaylistTask.execute();
		// modify by JXH 20150811 to
		scanPlaylistTask.executeOnExecutor(ThreadPoolExecutorUtils.getThreadPoolExecutor().getFULL_TASK_EXECUTOR());
	}

	public void onPause() {
		isFingerUp = false;
		mLayoutParams.bottomMargin = paramsmarginsize;
		mLayoutParams.topMargin = paramsmarginsize;
		mLayoutParams.height = (int) (bgHeight * 1);
		myMusicTopbar.setScaleX(1);
		myMusicTopbar.setScaleY(1);
		myMusicTopbar.setLayoutParams(mLayoutParams);
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				List<AuroraMainMenuData> list = (List<AuroraMainMenuData>) msg.obj;
				// myMusicTopbar.setImageResource(R.drawable.my_music_bg);
				myMusicTopbar.setImageBitmap(musicBg);// add by JXH
				initPlaylistData(list);
				if (firstMain.getVisibility() == View.GONE) {
					firstMain.setVisibility(View.VISIBLE);
					firstMain.setAlpha(0);
					ObjectAnimator anim = ObjectAnimator.ofFloat(firstMain, "alpha", 1);
					anim.setDuration(500);
					anim.start();
				}
				break;
			case 1:
				AuroraMainMenuData info = (AuroraMainMenuData) msg.obj;
				Intent intent = new Intent(mContext, AuroraNewPlayListActivity.class);
				intent.putExtra(AuroraSongSingle.EXTR_PLAYLIST_INFO, info);
				mContext.startActivity(intent);
				break;
			}
		}
	};

	private void initPlaylistData(List<AuroraMainMenuData> list) {
		mAuroraMainMenuAdapter.clearData();
		mAuroraMainMenuAdapter.addDatas(list);
	}

	/**
	 * 新建歌单dialog
	 */
	private void initCreatePlayList() {
		View view = LayoutInflater.from(mContext).inflate(R.layout.create_playlist_view, null);
		inputName = (AuroraEditText) view.findViewById(R.id.aurora_edit_playlist_name);
		final ImageView deleteButton = (ImageView) view.findViewById(R.id.aurora_input_delete);
		deleteButton.setVisibility(View.GONE);
		deleteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				inputName.setText("");
			}
		});

		AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(mContext).setTitle(R.string.new_singer).setView(view).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();

				AuroraMusicUtil.hideInputMethod(mContext, mDialog.getCurrentFocus());
				ThreadPoolExecutor executor = ThreadPoolExecutorUtils.getThreadPoolExecutor().getExecutor();
				executor.submit(dialogButtonClick);
				ReportUtils.getInstance(mContext.getApplicationContext()).reportMessage(ReportUtils.TAG_CREATE_PL);
			}

		}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				AuroraMusicUtil.hideInputMethod(mContext, mDialog.getCurrentFocus());

			}
		});

		mDialog = builder.create();
		mDialog.setCanceledOnTouchOutside(false);
		mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		if (!mDialog.isShowing()){
			mDialog.show();
		}
		AuroraMusicUtil.showInputMethod(mContext);
		final Button positiveButton = ((AuroraAlertDialog) mDialog).getButton(AuroraAlertDialog.BUTTON_POSITIVE);
		positiveButton.setEnabled(false);
		inputName.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				String newText = inputName.getText().toString();
				if (newText.trim().length() == 0) {
					deleteButton.setVisibility(View.GONE);
					positiveButton.setEnabled(false);
				} else {
					deleteButton.setVisibility(View.VISIBLE);
					positiveButton.setEnabled(true);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

			}

			@Override
			public void afterTextChanged(Editable arg0) {

			}
		});
	}

	private Runnable dialogButtonClick = new Runnable() {

		@Override
		public void run() {

			String name = inputName.getText().toString();
			if (name != null && name.length() > 0) {
				ContentResolver resolver = mContext.getContentResolver();
				int id = AuroraMusicUtil.idForplaylist(name, mContext);

				if (id >= 0) {
					try {
						name = makePlaylistName(name);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				LogUtil.d(TAG, "name:" + name + " id:" + id);
				try {
					ContentValues values = new ContentValues();
					String name2 = Globals.AURORA_PLAYLIST_TIP + name;
					values.put(MediaStore.Audio.Playlists.NAME, name2);
					Uri uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);

					AuroraMainMenuData info = new AuroraMainMenuData();
					info.setName(name);
					info.setPlaylistId(Integer.parseInt(uri.getLastPathSegment()));
					mHandler.obtainMessage(1, info).sendToTarget();
				} catch (Exception e) {
					e.printStackTrace();
					LogUtil.d(TAG, "insert fail!");
				}
			}
		}

	};

	private String makePlaylistName(String template) throws Exception {

		String[] cols = new String[] { MediaStore.Audio.Playlists.NAME };
		ContentResolver resolver = mContext.getContentResolver();
		String whereclause = MediaStore.Audio.Playlists.NAME + " LIKE ?";
		Cursor c = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, cols, whereclause, new String[] { Globals.AURORA_PLAYLIST_TIP + "%" }, MediaStore.Audio.Playlists.NAME);
		if (c == null) {
			return template;
		}
		int num = 1;
		// modify by JXH begin BUG #14130
		String suggestedname = "";
		if (template.endsWith("%")) {
			suggestedname = template + (num++);
		} else {
			suggestedname = String.format(template + "(%d)", num++);
		}
		// modify by JXH end BUG #14130
		boolean done = false;
		while (!done) {
			done = true;
			c.moveToFirst();
			while (!c.isAfterLast()) {
				String playlistname = c.getString(0);
				if (playlistname.compareToIgnoreCase(Globals.AURORA_PLAYLIST_TIP + suggestedname) == 0) {
					// modify by JXH begin BUG #14130
					if (template.endsWith("%")) {
						suggestedname = template + (num++);
					} else {
						suggestedname = String.format(template + "(%d)", num++);
					}
					// modify by JXH end BUG #14130
					done = false;
				}
				c.moveToNext();
			}
		}
		c.close();
		LogUtil.d(TAG, "suggestedname:" + suggestedname);
		return suggestedname;
	}

	/**
	 * 惯性回弹动画
	 */
	private void initTopAnimator() {
		AnimatorSet animSetXY = new AnimatorSet();
		ObjectAnimator margin1 = ObjectAnimator.ofInt(myMusicTopbar, "margin", 0);
		ObjectAnimator margin2 = ObjectAnimator.ofInt(myMusicTopbar, "margin", paramsmarginsize);
		animSetXY.playSequentially(margin1, margin2);
		animSetXY.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator arg0) {
				mThreadState = false;

			}

			@Override
			public void onAnimationRepeat(Animator arg0) {

			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				mThreadState = true;
			}

			@Override
			public void onAnimationCancel(Animator arg0) {

			}
		});
		animSetXY.setDuration(150);
		// animSetXY.setDuration(200);
		if (!mRunState) {
			return;
		}
		animSetXY.start();
	}

	public void notifiData() {
		if (mAuroraMainMenuAdapter != null)
			mAuroraMainMenuAdapter.notifyDataSetChanged();
	}

	class ScanPlaylistTask extends AsyncTask<Void, Void, List<AuroraMainMenuData>> {
		// private Bitmap mBitmap;

		@Override
		protected List<AuroraMainMenuData> doInBackground(Void... arg0) {
			// AuroraMusicUtil.getMountedStorage(mContext);//lory del 2014.9.2
			Cursor mCursor = mContext.getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, new String[] {
			/* 0 */
			BaseColumns._ID,
			/* 1 */
			PlaylistsColumns.NAME }, MediaStore.Audio.Playlists.NAME + " LIKE ?", new String[] { Globals.AURORA_PLAYLIST_TIP + "%" }, null);

			List<AuroraMainMenuData> list = new ArrayList<AuroraMainMenuData>();
			list.add(new AuroraMainMenuData(mContext.getString(R.string.new_singer), R.drawable.aurora_create_song_list_icon, -1, false));
			list.add(new AuroraMainMenuData(mContext.getString(R.string.my_favorite_songs), R.drawable.aurora_my_favorite_song_icon, 4, myfavoriteId));
			list.add(new AuroraMainMenuData(mContext.getString(R.string.recently_added_songs), R.drawable.aurora_recently_added_songs_icon, 5, -1));
			if (Globals.SWITCH_FOR_ONLINE_MUSIC) {
				list.add(new AuroraMainMenuData(mContext.getString(R.string.aurora_download_manager), R.drawable.aurora_download_manage, 7, -4));
				list.add(new AuroraMainMenuData(mContext.getString(R.string.aurora_collect_playlist), R.drawable.aurora_collect_playlist, 6, -3));
			}
			if(Globals.NO_MEUN_KEY){
				list.add(new AuroraMainMenuData(mContext.getString(R.string.setting), R.drawable.setting, 8, -1));
			}
			if (mCursor != null && mCursor.moveToFirst()) {
				do {
					int id = mCursor.getInt(0);
					String name = mCursor.getString(1);
					name = name.substring(Globals.AURORA_PLAYLIST_TIP.length());
					AuroraMainMenuData info = new AuroraMainMenuData();
					info.setName(name);
					info.setPlaylistId(id);
					info.setSongSizeType(0);
					info.setShowArrow(true);
					info.setResouceId(R.drawable.aurora_create_song_list_default_icon);
					list.add(info);
				} while (mCursor.moveToNext());
			}

			if (mCursor != null) {
				mCursor.close();
			}
			return list;
		}

		@Override
		protected void onPostExecute(List<AuroraMainMenuData> result) {
			if (mAuroraMainMenuAdapter == null || musicBg == null) {
				return;
			}
			mHandler.obtainMessage(0, result).sendToTarget();
		}

	}

	public void destroy() {
		if (firstMain != null) {
			firstMain.setAdapter(null);
			mAuroraMainMenuAdapter = null;
		}
	}
}
