/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.music;

import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ThreadPoolExecutor;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RemoteControlClient;
import android.media.RemoteControlClient.MetadataEditor;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.auroramusic.db.AuroraSongDb;
import com.android.auroramusic.downloadex.BitmapUtil;
import com.android.auroramusic.downloadex.DownloadInfo;
import com.android.auroramusic.downloadex.db.Dao;
import com.android.auroramusic.dts.DtsEffects;
import com.android.auroramusic.local.LocalPlayer;
import com.android.auroramusic.online.AuroraSearchLyricActivity;
import com.android.auroramusic.online.AuroraXiamiPlayer;
import com.android.auroramusic.ui.AuroraSoundControl;
import com.android.auroramusic.util.AuroraListItem;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.DisplayUtil;
import com.android.auroramusic.util.FlowTips;
import com.android.auroramusic.util.FlowTips.OndialogClickListener;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.LogUtil;
import com.android.auroramusic.util.ThreadPoolExecutorUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * Provides "background" audio playback capabilities, allowing the user to
 * switch between activities without stopping playback.
 */
@SuppressLint("NewApi")
public class MediaPlaybackService extends Service {
	/**
	 * used to specify whether enqueue() should start playing the new list of
	 * files right away, next or once all the currently queued files have been
	 * played
	 */
	public static final int NOW = 1;
	public static final int NEXT = 2;
	public static final int LAST = 3;
	public static final int PLAYBACKSERVICE_STATUS = 2467006;

	public static final int SHUFFLE_NONE = 3;// lory modify 0;
	public static final int SHUFFLE_NORMAL = 4;// lory modify 1;
	public static final int SHUFFLE_AUTO = 5;// lory modify 2;

	public static final int REPEAT_NONE = 0;
	public static final int REPEAT_CURRENT = 1;
	public static final int REPEAT_ALL = 2;

	public static final String AURORALRC_CHANGED = "com.android.music.lrcchanged";
	public static final String PLAYSTATE_CHANGED = "com.android.auroramusic.playstatechanged";// modify
																								// by
																								// chenhl
	public static final String META_CHANGED = "com.android.music.metachanged";
	public static final String QUEUE_CHANGED = "com.android.music.queuechanged";
	public static final String FAVORITE_CHANGED = "com.android.music.favoritechanged";
	public static final String CMDFAVORITE = "cmdfavorite";

	public static final String SERVICECMD = "com.android.music.musicservicecommand";
	public static final String CMDNAME = "command";
	public static final String CMDTOGGLEPAUSE = "togglepause";
	public static final String CMDSTOP = "stop";
	public static final String CMDPAUSE = "pause";
	public static final String CMDPLAY = "play";
	public static final String CMDPREVIOUS = "previous";
	public static final String CMDNEXT = "next";

	public static final String TOGGLEPAUSE_ACTION = "com.android.music.musicservicecommand.togglepause";
	public static final String PAUSE_ACTION = "com.android.music.musicservicecommand.pause";
	public static final String PREVIOUS_ACTION = "com.android.music.musicservicecommand.previous";
	public static final String NEXT_ACTION = "com.android.music.musicservicecommand.next";
	public static final String CLOSE_ACTION = "com.android.music.musicservicecommand.close"; // add
																								// by
																								// chenhl

	private static final String AURORA_MSG_FLIP = "cn_somatosensory_smart_pause";

	private static final int TRACK_ENDED = 1;
	private static final int RELEASE_WAKELOCK = 2;
	private static final int SERVER_DIED = 3;
	private static final int FOCUSCHANGE = 4;
	private static final int FADEDOWN = 5;
	private static final int FADEUP = 6;
	private static final int TRACK_WENT_TO_NEXT = 7;
	private static final int MAX_HISTORY_SIZE = 100;
	private static final int FILE_NOT_EXISTS = 8;
	private static final int PLAYFAILED = 500001;// add by tangjie 2014/06/20
	private static final int AURORA_HIFI_SWITH = 11;
	private static final int AURORA_HEADSET_SWITH = 12;

	private MultiPlayer mPlayer;
	private String mFileToPlay;
	private int mShuffleMode = SHUFFLE_NONE;
	private int mRepeatMode = REPEAT_NONE;
	private int mMediaMountedCount = 0;
	private long[] mAutoShuffleList = null;
	private long[] mPlayList = null;
	private int mPlayListLen = 0;
	private Vector<Integer> mHistory = new Vector<Integer>(MAX_HISTORY_SIZE);
	private Cursor mCursor;
	private int mPlayPos = -1;
	private int mNextPlayPos = -1;
	private boolean mNeedtoPlay = false;// paul add for BUG #15048
	private static final String LOGTAG = "MediaPlaybackService";
	private final Shuffler mRand = new Shuffler();
	private int mOpenFailedCounter = 0;
	String[] mCursorCols = new String[] {
			"audio._id AS _id", // index must match IDCOLIDX below
			MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.ALBUM_ID,
			MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.IS_PODCAST, // index
																					// must
																					// match
																					// PODCASTCOLIDX
																					// below
			MediaStore.Audio.Media.BOOKMARK // index must match BOOKMARKCOLIDX
											// below
	};
	private final static int IDCOLIDX = 0;
	private final static int PODCASTCOLIDX = 8;
	private final static int BOOKMARKCOLIDX = 9;
	private BroadcastReceiver mUnmountReceiver = null;
	private WakeLock mWakeLock;
	private int mServiceStartId = -1;
	private boolean mServiceInUse = false;
	private boolean mIsSupposedToBePlaying = false;
	private boolean mQuietMode = false;
	private AudioManager mAudioManager;
	private boolean mQueueIsSaveable = true;
	// used to track what type of audio focus loss caused the playback to pause
	private boolean mPausedByTransientLossOfFocus = false;

	private SharedPreferences mPreferences;
	// We use this to distinguish between different cards when saving/restoring
	// playlists.
	// This will have to change if we want to support multiple simultaneous
	// cards.
	private int mCardId;

	private MediaAppWidgetProvider mAppWidgetProvider = MediaAppWidgetProvider.getInstance();

	// interval after which we stop the service when idle
	private static final int IDLE_DELAY = 3000;// lory modify 60000 to 1000;

	private RemoteControlClient mRemoteControlClient;
	private long nextAudioId = -1;// add by chenhl 20140623
	private TelephonyManager mTelephonyManager;// add by chenhl 20140624

	// Iuni <lory><2014-07-10> add begin
	// private AuroraStreamPlayer mAuroraStreamPlayer;
	private ArrayList<AuroraListItem> mMusicList = null;
	public static String mHttpUri = "default net";
	private static final int NOT_MUSIC_NET = 0;
	private static final int MUSIC_NET = 1;
	private static final int VOLUME_CHANGE = 2000;
	private String mLrcPath = null;
	private boolean mSingleSong = false;

	private LocalPlayer mLocalPlayer;
	private int mbPrev = 0; // 0为下一曲 1为上一曲 2为点击播放
	private int mNotifyWidth = 0;
	private boolean mSdMount = false;
	private boolean mbHasEar = false;
	// Iuni <lory><2014-07-10> add end
	// private MediaButtonIntentReceiver mMediaButtonIntentReceiver; // add by
	// // chenhl

	// add by chenhl start
	private AuroraXiamiPlayer mAuroraXiamiPlayer;
	public static boolean isRunning = false;
	// add by chenhl end

	private Handler mMediaplayerHandler = new Handler() {
		float mCurrentVolume = 1.0f;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case FADEDOWN:
				mCurrentVolume -= .05f;
				if (mCurrentVolume > .2f) {
					mMediaplayerHandler.sendEmptyMessageDelayed(FADEDOWN, 10);
				} else {
					mCurrentVolume = .2f;
				}

				if (mbOnline) {
					if (mAuroraXiamiPlayer != null) {
						mAuroraXiamiPlayer.setVolume(mCurrentVolume);
					}

				} else {
					if (mPlayer != null) {
						mPlayer.setVolume(mCurrentVolume);
					}
				}

				break;
			case FADEUP:
				mCurrentVolume += .01f;
				if (mCurrentVolume < 1.0f) {
					mMediaplayerHandler.sendEmptyMessageDelayed(FADEUP, 10);
				} else {
					mCurrentVolume = 1.0f;
				}

				if (mbOnline) {
					if (mAuroraXiamiPlayer != null) {
						mAuroraXiamiPlayer.setVolume(mCurrentVolume);
					}
				} else {
					if (mPlayer != null) {
						mPlayer.setVolume(mCurrentVolume);
					}

				}

				break;
			case SERVER_DIED:
				if (mIsSupposedToBePlaying) {
					gotoNext(true);
				} else {
					// the server died when we were idle, so just
					// reopen the same song (it will start again
					// from the beginning though when the user
					// restarts)
					openCurrentAndNext();
				}
				break;
			// lory add for local player start
			case LocalPlayer.LOCALPLAYER_STATE_NEXT:
				if (mSingleSong) {
					seek(0);
					play();
				} else {
					// add by chenhl start 20140617
					if (mShuffleMode == SHUFFLE_NORMAL) {
						if (mHistory.size() > MAX_HISTORY_SIZE) {
							mHistory.removeElementAt(0);
						}
						if (mPlayPos >= 0) {
							mHistory.add(mPlayPos);
						}
					} else {
						mHistory.clear();
					}
					// add by chenhl end
					stop(false);

					mPlayPos = mNextPlayPos;
					// openCurrent();
					openCurrentAndNext();
					notifyChange(META_CHANGED);
					play();
				}
				break;
			// lory add for local player end

			case TRACK_WENT_TO_NEXT:
				if (mSingleSong) {
					seek(0);
					play();
				} else {
					// add by chenhl start 20140617
					if (mShuffleMode == SHUFFLE_NORMAL) {
						if (mHistory.size() > MAX_HISTORY_SIZE) {
							mHistory.removeElementAt(0);
						}
						if (mPlayPos >= 0) {
							mHistory.add(mPlayPos);
						}
					} else {
						mHistory.clear();
					}
					// add by chenhl end
					mPlayPos = mNextPlayPos;
					synchronized (MediaPlaybackService.this) {
						if (mCursor != null) {
							mCursor.close();
							mCursor = null;
						}
					}
					mCursor = getCursorForId(mPlayList[mPlayPos]);
					if (mCursor != null && mCursor.getCount() > 0) {
						notifyChange(META_CHANGED);
						updateNotification(false);
						setNextTrack();
					} else {
						openCurrentAndNext();
					}
				}

				break;
			case TRACK_ENDED:
				if (mSingleSong) {
					seek(0);
					play();
				} else {
					if (mRepeatMode == REPEAT_CURRENT) {
						seek(0);
						if (isPlaying()) {// modify by JXH begin
							play();
						}// modify by JXH end
					} else {
						gotoNext(false);
					}
				}

				break;
			case RELEASE_WAKELOCK:
				mWakeLock.release();
				break;

			case FILE_NOT_EXISTS:
				long tid = Long.valueOf(msg.obj.toString());
				int numremoved = 0;
				if (tid > 0) {
					numremoved = removeTrack(tid);
				}

				if (numremoved > 0) {
					setNextTrack();
				}

				break;

			case FOCUSCHANGE:
				// This code is here so we can better synchronize it with the
				// code that
				// handles fade-in
				switch (msg.arg1) {
				case AudioManager.AUDIOFOCUS_LOSS://-1
					if (isPlaying()) {
						mPausedByTransientLossOfFocus = false;
					}
					pause();
					break;
				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK://-3
					// Log.v(LOGTAG,
					// "zll AudioFocus: received AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK 1");
					mMediaplayerHandler.removeMessages(FADEUP);
					mMediaplayerHandler.sendEmptyMessage(FADEDOWN);
					break;
				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT://-2
					// Log.v(LOGTAG,
					// "zll AudioFocus: received AUDIOFOCUS_LOSS_TRANSIENT");
					if (isPlaying()) {
						mPausedByTransientLossOfFocus = true;
					}
					pause();

					break;
				case AudioManager.AUDIOFOCUS_GAIN://1
					LogUtil.d(LOGTAG, "--------AudioFocus: received AUDIOFOCUS_GAIN isPlaying():" + isPlaying() + ",mPausedByTransientLossOfFocus:" + mPausedByTransientLossOfFocus);
					if (!isPlaying() && mPausedByTransientLossOfFocus) {
						mPausedByTransientLossOfFocus = false;
						// mCurrentVolume = 0f;
						// mPlayer.setVolume(mCurrentVolume);
						play(); // also queues a fade-in
					} else {
						mMediaplayerHandler.removeMessages(FADEDOWN);
						mMediaplayerHandler.sendEmptyMessage(FADEUP);
					}
					break;
				default:
					Log.e(LOGTAG, "zll Unknown audio focus change code");
				}
				break;
			case PLAYFAILED:
				if (isLocalEmpty()) {
					Toast.makeText(MediaPlaybackService.this, R.string.play_failed_nofiles, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(MediaPlaybackService.this, R.string.playback_failed, Toast.LENGTH_SHORT).show();
				}
				// updateNotification(true);
				updateOfFailed(PLAYSTATE_CHANGED);
				break;

			// online start
			case LocalPlayer.LOCALPLAYER_STATE_PREPARED:// for local player
														// start
			case AuroraXiamiPlayer.XIAMI_SDK_REFRESH_DURATION: // for xiami
																// music
				// Log.i(LOGTAG, "zll BAIDU_SDK_REFRESH_DURATION ---- 1");
				mOpenFailedCounter = 0;
				notifyChange(META_CHANGED);
				// updateNotification(false);
				break;

			case AuroraXiamiPlayer.XIAMI_TRACK_ENDED:
				// Log.i(LOGTAG,
				// "zll STREAM_TRACK_ENDED mRepeatMode:"+mRepeatMode);
				if (Application.isRadioType()) {
					gotoNext(false);
					break;
				}
				if (mRepeatMode == REPEAT_CURRENT) {
					seek(0);
					play();
				} else {
					gotoNext(false);
				}
				break;

			case AuroraXiamiPlayer.XIAMI_TRACK_URI_UPDATE:// add by chenhl
				mHttpUri = msg.obj.toString();
				break;

			case AURORA_HIFI_SWITH:
				/*
				 * boolean play = (msg.arg1 == 1)?true:false;
				 * openCloseHIfi(play);
				 */
				break;

			case AURORA_HEADSET_SWITH:
				boolean hasEar = ((int) msg.arg1 == 0) ? false : true;
				onEarChanged(hasEar);
				break;

			case LocalPlayer.LOCALPLAYER_STATE_ERROR:// for local player start
			case AuroraXiamiPlayer.XIAMI_TRACK_ERROR_ENDED: {
				int error = msg.arg1;
				if (error == -900 || error == 261) {
					Toast.makeText(MediaPlaybackService.this, R.string.aurora_network_error, Toast.LENGTH_SHORT).show();
					mIsSupposedToBePlaying = false;
					notifyChange(PLAYSTATE_CHANGED);
					stopForeground(true);
					return;
				}

				// modify by chenhl start 0929
				// Log.i(LOGTAG,
				// "zll ---- STREAM_TRACK_ERROR_ENDED 1 mOpenFailedCounter:"+mOpenFailedCounter+",mPlayListLen:"+mPlayListLen+",mPlayPos:"+mPlayPos+",mRepeatMode:"+mRepeatMode);
				if (mOpenFailedCounter++ < 10 && mPlayListLen > 1) {
					int pos = mPlayPos;
					if (mRepeatMode == REPEAT_CURRENT) {
						if (mPlayPos <= 0) {
							pos = mPlayListLen - 1;
						} else {
							pos--;
						}
					} else {
						if (mShuffleMode == SHUFFLE_NORMAL) {
							if (mHistory.size() > MAX_HISTORY_SIZE) {
								mHistory.removeElementAt(0);
							}
							if (mPlayPos >= 0) {
								mHistory.add(mPlayPos);
							}
						} else {
							mHistory.clear();
						}
						pos = getNextPosition(false, mbPrev == 1 ? true : false);
					}

					// Log.i(LOGTAG,
					// "zll --- STREAM_TRACK_ERROR_ENDED 2 pos:"+pos+",mbPrev:"+mbPrev+",mPlayListLen:"+mPlayListLen);
					if (pos < 0 || mbPrev == 2) {
						if (!mQuietMode) {
							mMediaplayerHandler.sendEmptyMessage(PLAYFAILED);
						}
						gotoIdleState();
						if (mIsSupposedToBePlaying) {
							mIsSupposedToBePlaying = false;
							notifyChange(PLAYSTATE_CHANGED);
						}
						// Log.i(LOGTAG,
						// "zll --- STREAM_TRACK_ERROR_ENDED 3 mOpenFailedCounter:"+mOpenFailedCounter+",mPlayListLen:"+mPlayListLen);
						return;
					}

					mPlayPos = pos;
					stop(false);
					mPlayPos = pos;

					if (mbPrev == 1) {
						openCurrentAndNextForPrev();
					} else if (mbPrev == 0) {
						openCurrentAndNext();
					}
					// openCurrent();
					notifyChange(META_CHANGED);
					// Log.i(LOGTAG,
					// "zll STREAM_TRACK_ERROR_ENDED 4 msg.arg2:"+msg.arg2);
					if (msg.arg2 != 1) {
						play();
					}
					// mCursor = getCursorForId(mPlayList[mPlayPos]);
					// modify by chenhl end 0929
				} else {
					// Log.i(LOGTAG, "zll BAIDU_SDK_REFRESH_DURATION ---- 2");
					mOpenFailedCounter = 0;
					if (!mQuietMode) {
						Toast.makeText(MediaPlaybackService.this, R.string.playback_failed, Toast.LENGTH_SHORT).show();
					}

					// Log.i(LOGTAG,
					// "zll STREAM_TRACK_ERROR_ENDED Failed to open file for playback");
					gotoIdleState();
					if (mIsSupposedToBePlaying) {
						mIsSupposedToBePlaying = false;
						notifyChange(PLAYSTATE_CHANGED);
					}
					return;
				}
			}
				break;
			// online end

			case VOLUME_CHANGE:
				changeDtsData();
				break;
			default:
				break;
			}
		}
	};

	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			String cmd = intent.getStringExtra("command");
			String app = intent.getStringExtra("application");
			LogUtil.d(LOGTAG, " ---- onReceive action:" + action + ",cmd:" + cmd + " app:" + app + " isPlaying():" + isPlaying());
			if (CMDNEXT.equals(cmd) || NEXT_ACTION.equals(action)) {
				if (!TextUtils.isEmpty(app) && app.equals("phonewindowmananger") && !isPlaying()) {// 黑屏控制音乐
					return;
				}
				gotoNext(true);
			} else if (CMDPREVIOUS.equals(cmd) || PREVIOUS_ACTION.equals(action)) {
				if (!TextUtils.isEmpty(app) && app.equals("phonewindowmananger") && !isPlaying()) {// 黑屏控制音乐
					return;
				}

				prev();
			} else if (CMDTOGGLEPAUSE.equals(cmd) || TOGGLEPAUSE_ACTION.equals(action)) {
				if (isPlaying()) {
					pause();
					mPausedByTransientLossOfFocus = false;
				} else {
					play();
				}
			} else if (CMDPAUSE.equals(cmd) || PAUSE_ACTION.equals(action) || action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
				pause();
				mPausedByTransientLossOfFocus = false;
			} else if (CMDFAVORITE.equals(cmd)) {// lory add
				if (!isFavorite()) {
					addToFavorite();
				} else {
					removeFromFavorites();
				}
			} else if (CMDPLAY.equals(cmd)) {
				play();
			} else if (CMDSTOP.equals(cmd)) {
				pause();
				mPausedByTransientLossOfFocus = false;
				seek(0);
			} else if (MediaAppWidgetProvider.CMDAPPWIDGETUPDATE.equals(cmd)) {
				// Someone asked us to refresh a set of specific widgets,
				// probably
				// because they were just added.
				int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
				mAppWidgetProvider.performUpdate(MediaPlaybackService.this, appWidgetIds);
			}
			// add by chenhl start 20140930
			else if (action.equals(AuroraMusicUtil.TIMEOFF_CLOSE_ACTION)) {
				LogUtil.i(LOGTAG, "zll close music:" + AuroraMusicUtil.getLiveAlarmTime());
				if (AuroraMusicUtil.getLiveAlarmTime() > 0) {
					AuroraMusicUtil.startAlarmToClose(context);
					updateNotification(false);
					return;
				}
				AuroraMusicUtil.resetAlarmTime(getApplicationContext());
				if (isPlaying()) {
					pause();
				}
			} else if (action.equalsIgnoreCase(AURORA_MSG_FLIP)) {
				boolean ext = intent.getBooleanExtra("Phyflip", false);
				Log.i(LOGTAG, "zll ----- ext:" + ext);
				if (ext) {
					openSensorManagerListner();
				} else {
					disopenSensorManager();
				}
			} else if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
				int state = intent.getIntExtra("state", 0);

				if (Globals.SWITCH_FOR_SOUND_CONTROL) {
					if (state == 0) {// out
						mbHasEar = false;
						mMediaplayerHandler.removeMessages(AURORA_HEADSET_SWITH);
						mMediaplayerHandler.obtainMessage(AURORA_HEADSET_SWITH, 0, -1).sendToTarget();
					} else if (state == 1) {// in
						mbHasEar = true;
						mMediaplayerHandler.removeMessages(AURORA_HEADSET_SWITH);
						mMediaplayerHandler.obtainMessage(AURORA_HEADSET_SWITH, 1, -1).sendToTarget();
					}
				} else {
					// Log.i(LOGTAG, "zll ----- ttt 1 state:"+state);
					if (state == 1 && mAudioManager != null) {
						// Log.i(LOGTAG, "zll ----- ttt 2:");

						// mAudioManager.requestAudioFocus(mAudioFocusListener,
						// AudioManager.STREAM_MUSIC,
						// AudioManager.AUDIOFOCUS_GAIN);//delete for BUG #13351

						ComponentName rec = new ComponentName(getPackageName(), MediaButtonIntentReceiver.class.getName());
						mAudioManager.registerMediaButtonEventReceiver(rec);
					}
				}
			} else if (CLOSE_ACTION.equals(action)) {
				pause();
				stopForeground(true);
				// BUG #16229 begin
				mPausedByTransientLossOfFocus = false;
				mPauseByPhoneState = false;
				// BUG #16229 end
			}
		}
	};

	private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
		public void onAudioFocusChange(int focusChange) {
			LogUtil.i(LOGTAG, "zll OnAudioFocusChangeListener----- ttt 3:" + focusChange);
			mMediaplayerHandler.obtainMessage(FOCUSCHANGE, focusChange, 0).sendToTarget();
		}
	};

	public MediaPlaybackService() {
	}

	private void initAuroraSensor() {

		synchronized (MediaPlaybackService.this) {
			// if (Globals.SWITCH_FOR_SOUND_CONTROL)
			{
				int openSensor = Settings.Secure.getInt(MediaPlaybackService.this.getContentResolver(), "somatosensory_smart_pause", 0);
				LogUtil.d(LOGTAG, " --- initAuroraSensor openSensor :" + openSensor);
				if (openSensor == 1) {
					openSensorManagerListner();
				} else {
					disopenSensorManager();
				}
			}
		}

		return;
	}

	private ThreadPoolExecutor executor = ThreadPoolExecutorUtils.getThreadPoolExecutor().getExecutor();
	private Bitmap mask, mDefautBitmap;

	@SuppressLint({ "WorldReadableFiles", "WorldWriteableFiles" })
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate() {
		super.onCreate();
		LogUtil.d(LOGTAG, "onCreate");
		// add for clear the notification when the service restart
		// stopForeground(true);
		isRunning = true;
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		ComponentName rec = new ComponentName(getPackageName(), MediaButtonIntentReceiver.class.getName());
		mAudioManager.registerMediaButtonEventReceiver(rec);

		mSdMount = false;

		// TODO update to new constructor
		Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		mediaButtonIntent.setComponent(rec);
		PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, mediaButtonIntent, 0);

		// Log.i(LOGTAG, "zll onCreate registerRemoteControlClient ----");
		mRemoteControlClient = new RemoteControlClient(mediaPendingIntent);
		mAudioManager.registerRemoteControlClient(mRemoteControlClient);

		int flags = RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS | RemoteControlClient.FLAG_KEY_MEDIA_NEXT | RemoteControlClient.FLAG_KEY_MEDIA_PLAY | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
				| RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE | RemoteControlClient.FLAG_KEY_MEDIA_STOP;
		mRemoteControlClient.setTransportControlFlags(flags);

		mRemoteControlClient.setPlaybackState(isPlaying() ? RemoteControlClient.PLAYSTATE_PLAYING : RemoteControlClient.PLAYSTATE_PAUSED);

		// insertMyProvider();
		mPreferences = getSharedPreferences("Music", MODE_WORLD_READABLE | MODE_WORLD_WRITEABLE);
		mCardId = MusicUtils.getCardId(this);

		registerExternalStorageListener();
		registerVolumeListenerReceiver();

		// Needs to be done in this thread, since otherwise
		// ApplicationContext.getPowerManager() crashes.
		mPlayer = new MultiPlayer();
		mPlayer.setHandler(mMediaplayerHandler);

		// Iuni <lory><2014-07-10> add begin moify by chenhl 20141222 for xiami
		if (MusicUtils.mSongDb == null) {
			MusicUtils.mSongDb = new AuroraSongDb(this);
		}

		mask = BitmapFactory.decodeResource(getResources(), R.drawable.mask).copy(Bitmap.Config.ARGB_8888, true);
		mDefautBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.aurora_bg_play).copy(Bitmap.Config.ARGB_8888, true);

		executor.execute(new Runnable() {
			@Override
			public void run() {
				initLocalPlayer(mMediaplayerHandler);
				initXiamiPlayer(mMediaplayerHandler);
				if (Globals.QUERY_SONG_FILTER == null) {
					AuroraMusicUtil.getMountedStorage(getApplicationContext());
				}
				initAuroraSensor();
				reloadQueue();
				notifyChange();
				if (noStopService()) {
					updateNotification(false);
				}
				mMediaplayerHandler.post(new Runnable() {

					@Override
					public void run() {
						// Iuni <lory><2014-07-10> add end
						// modify by chenhl start 20140903
						if (AuroraMusicUtil.isFromHeadset()) {
							AuroraMusicUtil.setHeadsetFlag(false);
							play();
						}

						// paul add for BUG #15048 start
						if (mNeedtoPlay) {
							LogUtil.d(LOGTAG, " mNeedtoPlay play");
							mNeedtoPlay = false;
							play();
						}
						// paul add for BUG #15048 end
					}
				});
			}
		});

		mSingleSong = false;

		IntentFilter commandFilter = new IntentFilter();
		commandFilter.addAction(SERVICECMD);
		commandFilter.addAction(TOGGLEPAUSE_ACTION);
		commandFilter.addAction(PAUSE_ACTION);
		commandFilter.addAction(NEXT_ACTION);
		commandFilter.addAction(PREVIOUS_ACTION);
		commandFilter.addAction(AuroraMusicUtil.TIMEOFF_CLOSE_ACTION);// add by
																		// chenhl
		commandFilter.addAction(AURORA_MSG_FLIP);
		commandFilter.addAction(Intent.ACTION_HEADSET_PLUG);// lory add
		commandFilter.addAction(CLOSE_ACTION);// add by chenhl
		commandFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		registerReceiver(mIntentReceiver, commandFilter);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
		mWakeLock.setReferenceCounted(false);

		// If the service was idle, but got killed before it stopped itself, the
		// system will relaunch it. Make sure it gets stopped again in that
		// case.
		Message msg = mDelayedStopHandler.obtainMessage();
		mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
		// add by chenhl start 20140624
		mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		AuroraMusicUtil.startAlarmToClose(this, 0);

		mNotifyWidth = DisplayUtil.dip2px(this, 50f);// DisplayUtil.dip2px(this,
														// 128f);
		// add by chenhl end 20140624

		if (Globals.SWITCH_FOR_SOUND_CONTROL) {
			if (mHifiTask != null) {
				executor.remove(mHifiTask);
			}
			mHifiTask = new AuroraHifiThread();
			// mHifiTask.start();
			executor.submit(mHifiTask);

		}

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter != null) {
			mBluetoothAdapter.getProfileProxy(getApplicationContext(), mBluetoothProfileServiceListener, BluetoothProfile.HEADSET);
		}
	}

	// add by JXH for Bluetooth 20150911 begin
	private BluetoothHeadset mBluetoothHeadset;
	protected BluetoothAdapter mBluetoothAdapter;
	protected BluetoothProfile.ServiceListener mBluetoothProfileServiceListener = new BluetoothProfile.ServiceListener() {
		@Override
		public void onServiceConnected(int profile, BluetoothProfile proxy) {
			mBluetoothHeadset = (BluetoothHeadset) proxy;
			LogUtil.d(LOGTAG, "-----BluetoothHeadset:" + mBluetoothHeadset);
		}

		@Override
		public void onServiceDisconnected(int profile) {
			mBluetoothHeadset = null;
		}
	};

	private boolean isBluetoothAvailable() {

		// There's no need to ask the Bluetooth system service if BT is enabled:
		//
		// BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		// if ((adapter == null) || !adapter.isEnabled()) {
		// if (DBG) log("  ==> FALSE (BT not enabled)");
		// return false;
		// }
		// if (DBG) log("  - BT enabled!  device name " + adapter.getName()
		// + ", address " + adapter.getAddress());
		//
		// ...since we already have a BluetoothHeadset instance. We can just
		// call isConnected() on that, and assume it'll be false if BT isn't
		// enabled at all.

		// Check if there's a connected headset, using the BluetoothHeadset API.
		boolean isConnected = false;
		if (mBluetoothHeadset != null) {
			List<BluetoothDevice> deviceList = mBluetoothHeadset.getConnectedDevices();
			if (deviceList != null && deviceList.size() > 0) {
				isConnected = true;
			}
		}
		LogUtil.d(LOGTAG, "-------isBluetoothAvailable:" + isConnected);
		return isConnected;
	}

	// add by JXH for Bluetooth 20150911 end

	@Override
	public void onDestroy() {
		// Check that we're not being destroyed while something is still
		// playing.
		isRunning = false;
		mbOnPause = false;

		MusicUtils.resetStaticService();
		// release all MediaPlayer resources, including the native player and
		// wakelocks
		Intent i = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
		i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
		i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
		sendBroadcast(i);
		mPlayer.release();
		mPlayer = null;

		// Log.i(LOGTAG, "zll onDestroy unregisterRemoteControlClient ----");
		// releaseStreamPlayer();
		releaseXiamiPlayer();
		releaseLocalPlayer();
		disopenSensorManager();

		mAudioManager.abandonAudioFocus(mAudioFocusListener);
		mAudioManager.unregisterRemoteControlClient(mRemoteControlClient);

		// make sure there aren't any other messages coming
		mDelayedStopHandler.removeCallbacksAndMessages(null);
		mMediaplayerHandler.removeCallbacksAndMessages(null);

		unregisterReceiver(mIntentReceiver);
		unRegisterVolumeListenerReceiver();
		if (mBluetoothHeadset != null) {
			mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);
			mBluetoothHeadset = null;
		}

		if (mUnmountReceiver != null) {
			unregisterReceiver(mUnmountReceiver);
			mUnmountReceiver = null;
		}

		synchronized (MediaPlaybackService.this) {
			if (mCursor != null) {
				mCursor.close();
				mCursor = null;
			}
		}
		mWakeLock.release();
		if (mTelephonyManager != null) {
			mTelephonyManager.listen(mPhoneStateListener, 0);// add by chenhl
																// 20140624
		}
		LogUtil.d(LOGTAG, "onDestroy");
		super.onDestroy();
	}

	private final char hexdigits[] = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	private void saveQueue(boolean full) {
		// AuroraMusicUtil.justTest();
		LogUtil.d(LOGTAG, " ----- saveQueue 1 mRepeatMode:" + mRepeatMode + ",mShuffleMode:" + mShuffleMode + ",mSingleSong:" + mSingleSong + ",full:" + full);
		if (!mQueueIsSaveable) {
			return;
		}

		if (mSingleSong) {
			return;
		}

		Editor ed = mPreferences.edit();
		// long start = System.currentTimeMillis();
		if (full) {
			StringBuilder q = new StringBuilder();

			// The current playlist is saved as a list of "reverse hexadecimal"
			// numbers, which we can generate faster than normal decimal or
			// hexadecimal numbers, which in turn allows us to save the playlist
			// more often without worrying too much about performance.
			// (saving the full state takes about 40 ms under no-load conditions
			// on the phone)
			int len = mPlayListLen;
			for (int i = 0; i < len; i++) {
				long n = mPlayList[i];
				if (n < 0) {
					continue;
				} else if (n == 0) {
					q.append("0;");
				} else {
					while (n != 0) {
						int digit = (int) (n & 0xf);
						n >>>= 4;
						q.append(hexdigits[digit]);
					}
					q.append(";");
				}
			}
			ed.putString("queue", q.toString());
			ed.putInt("cardid", mCardId);
			if (mShuffleMode != SHUFFLE_NONE) {
				// In shuffle mode we need to save the history too
				len = mHistory.size();
				q.setLength(0);
				for (int i = 0; i < len; i++) {
					int n = mHistory.get(i);
					if (n == 0) {
						q.append("0;");
					} else {
						while (n != 0) {
							int digit = (n & 0xf);
							n >>>= 4;
							q.append(hexdigits[digit]);
						}
						q.append(";");
					}
				}
				ed.putString("history", q.toString());
			}
		}
		ed.putInt("curpos", mPlayPos);
		if (mPlayer != null && mPlayer.isInitialized()) {
			ed.putLong("seekpos", mPlayer.position());
		} else if (mLocalPlayer != null && mLocalPlayer.isInitialized()) {
			ed.putLong("seekpos", mLocalPlayer.getCurrentPosition());
		}
		ed.putInt("repeatmode", mRepeatMode);
		ed.putInt("shufflemode", mShuffleMode);
		ed.putBoolean("isnet", mbOnline);
		SharedPreferencesCompat.apply(ed);
	}

	private void reloadQueue() {
		String q = null;

		int id = mCardId;
		if (mPreferences.contains("cardid")) {
			id = mPreferences.getInt("cardid", ~mCardId);
		}
		if (id == mCardId) {
			// Only restore the saved playlist if the card is still
			// the same one as when the playlist was saved
			q = mPreferences.getString("queue", "");
		}

		int qlen = q != null ? q.length() : 0;
		if (qlen > 1) {
			int plen = 0;
			int n = 0;
			int shift = 0;
			for (int i = 0; i < qlen; i++) {
				char c = q.charAt(i);
				if (c == ';') {
					ensurePlayListCapacity(plen + 1);
					mPlayList[plen] = n;
					plen++;
					n = 0;
					shift = 0;
				} else {
					if (c >= '0' && c <= '9') {
						n += ((c - '0') << shift);
					} else if (c >= 'a' && c <= 'f') {
						n += ((10 + c - 'a') << shift);
					} else {
						// bogus playlist data
						plen = 0;
						break;
					}
					shift += 4;
				}
			}
			mPlayListLen = plen;

			int pos = mPreferences.getInt("curpos", 0);
			if (pos < 0 || pos >= mPlayListLen) {
				// The saved playlist is bogus, discard it
				mPlayListLen = 0;
				return;
			}
			mPlayPos = pos;

			// When reloadQueue is called in response to a card-insertion,
			// we might not be able to query the media provider right away.
			// To deal with this, try querying for the current file, and if
			// that fails, wait a while and try again. If that too fails,
			// assume there is a problem and don't restore the state.
			boolean isnet = mPreferences.getBoolean("isnet", true);
			initData(isnet);

			// Make sure we don't auto-skip to the next song, since that
			// also starts playback. What could happen in that case is:
			// - music is paused
			// - go to UMS and delete some files, including the currently
			// playing one
			// - come back from UMS
			// (time passes)
			// - music app is killed for some reason (out of memory)
			// - music service is restarted, service restores state, doesn't
			// find
			// the "current" file, goes to the next and: playback starts on its
			// own, potentially at some random inconvenient time.
			mOpenFailedCounter = 20;

			int repmode = mPreferences.getInt("repeatmode", REPEAT_NONE);
			if (repmode != REPEAT_ALL && repmode != REPEAT_CURRENT) {
				repmode = REPEAT_NONE;
			}
			mRepeatMode = repmode;

			int shufmode = mPreferences.getInt("shufflemode", SHUFFLE_NONE);
			if (shufmode != SHUFFLE_AUTO && shufmode != SHUFFLE_NORMAL) {
				shufmode = SHUFFLE_NONE;
			}
			if (shufmode != SHUFFLE_NONE) {
				// in shuffle mode we need to restore the history too
				q = mPreferences.getString("history", "");
				qlen = q != null ? q.length() : 0;
				if (qlen > 1) {
					plen = 0;
					n = 0;
					shift = 0;
					mHistory.clear();
					for (int i = 0; i < qlen; i++) {
						char c = q.charAt(i);
						if (c == ';') {
							if (n >= mPlayListLen) {
								// bogus history data
								mHistory.clear();
								break;
							}
							mHistory.add(n);
							n = 0;
							shift = 0;
						} else {
							if (c >= '0' && c <= '9') {
								n += ((c - '0') << shift);
							} else if (c >= 'a' && c <= 'f') {
								n += ((10 + c - 'a') << shift);
							} else {
								// bogus history data
								mHistory.clear();
								break;
							}
							shift += 4;
						}
					}
				}
			}
			if (shufmode == SHUFFLE_AUTO) {
				if (!makeAutoShuffleList()) {
					shufmode = SHUFFLE_NONE;
				}
			}
			mShuffleMode = shufmode;
			mQuietMode = true;
			openCurrentAndNext();
			mQuietMode = false;
			if (!mPlayer.isInitialized() && (mLocalPlayer != null && !mLocalPlayer.isInitialized())) {
				// couldn't restore the saved state
				mPlayListLen = 0;
				// Log.i(LOGTAG,
				// "zll ----- reloadQueue 3 mRepeatMode:"+mRepeatMode+",mShuffleMode:"+mShuffleMode+",pos:"+pos+",mPlayListLen:"+mPlayListLen);
				// return;//lory del 2014.7.22
			}

			long seekpos = mPreferences.getLong("seekpos", 0);
			seek(seekpos >= 0 && seekpos < duration() && !isnet ? seekpos : 0); // modify
																				// by
																				// chenhl
																				// 20140902
			/*
			 * Log.d(LOGTAG, "restored queue, currently at position " +
			 * position() + "/" + duration() + " (requested " + seekpos + ")");
			 */
			// Log.i(LOGTAG,
			// "zll ----- reloadQueue 4 mRepeatMode:"+mRepeatMode+",mShuffleMode:"+mShuffleMode+",pos:"+pos+",mPlayListLen:"+mPlayListLen);
		} else {
			// Log.i(LOGTAG, "zll ---- reloadQueue 5 -----");
			onStartFromScreen();
		}
	}

	private void onStartFromScreen() {
		// Log.i(LOGTAG, "zll ----- onStartFromScreen 1");
		// if (AuroraMusicUtil.getScreenFlag())
		{
			initData(false);

			// Log.i(LOGTAG, "zll ----- onStartFromScreen 2");
			// AuroraMusicUtil.setScreenFlag(false);

			mOpenFailedCounter = 20;
			int repmode = mPreferences.getInt("repeatmode", REPEAT_NONE);
			if (repmode != REPEAT_ALL && repmode != REPEAT_CURRENT) {
				repmode = REPEAT_NONE;
			}
			mRepeatMode = repmode;

			String q = null;

			int shufmode = mPreferences.getInt("shufflemode", SHUFFLE_NONE);
			if (shufmode != SHUFFLE_AUTO && shufmode != SHUFFLE_NORMAL) {
				shufmode = SHUFFLE_NONE;
			}
			if (shufmode != SHUFFLE_NONE) {
				// in shuffle mode we need to restore the history too
				int plen = 0;
				int n = 0;
				int shift = 0;
				int qlen = 0;

				q = mPreferences.getString("history", "");
				qlen = q != null ? q.length() : 0;
				if (qlen > 1) {
					plen = 0;
					n = 0;
					shift = 0;
					mHistory.clear();
					for (int i = 0; i < qlen; i++) {
						char c = q.charAt(i);
						if (c == ';') {
							if (n >= mPlayListLen) {
								// bogus history data
								mHistory.clear();
								break;
							}
							mHistory.add(n);
							n = 0;
							shift = 0;
						} else {
							if (c >= '0' && c <= '9') {
								n += ((c - '0') << shift);
							} else if (c >= 'a' && c <= 'f') {
								n += ((10 + c - 'a') << shift);
							} else {
								// bogus history data
								mHistory.clear();
								break;
							}
							shift += 4;
						}
					}
				}
			}
			if (shufmode == SHUFFLE_AUTO) {
				if (!makeAutoShuffleList()) {
					shufmode = SHUFFLE_NONE;
				}
			}
			mShuffleMode = shufmode;
			mQuietMode = true;

			openCurrentAndNext();
			mQuietMode = false;
			if (!mPlayer.isInitialized() && (mLocalPlayer != null && !mLocalPlayer.isInitialized())) {
				// couldn't restore the saved state
				mPlayListLen = 0;
			}

			long seekpos = mPreferences.getLong("seekpos", 0);
			seek(seekpos >= 0 && seekpos < duration() ? seekpos : 0);
			// Log.i(LOGTAG,
			// "zll ----- onStartFromScreen 3 mRepeatMode:"+mRepeatMode+",mShuffleMode:"+mShuffleMode+",mPlayPos:"+mPlayPos+",mPlayListLen:"+mPlayListLen);
		}
		return;
	}

	private void initData(boolean is) {
		Cursor cursor = null;
		try {
			long currentid = -1;
			int pos = -1;
			StringBuilder where = new StringBuilder(Globals.QUERY_SONG_FILTER);
			if (mPlayList != null && mPlayList.length > 0 && !is) {
				currentid = mPlayList[mPlayPos];
				where.append(" AND ");
				where.append(MediaStore.Audio.Media._ID + " IN (");
				for (int i = 0; i < mPlayList.length; i++) {
					where.append(mPlayList[i]);
					if (i < mPlayList.length - 1) {
						where.append(",");
					}
				}
				where.append(")");
			}

			cursor = MusicUtils.query(this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] { "_id", "_data" }, where.toString(), null, null);
			int i = 0;

			// modify by chenhl start
			List<String> pathsXml = AuroraMusicUtil.doParseXml(this, "paths.xml");
			if (cursor != null && cursor.moveToFirst()) {
				mPlayList = new long[cursor.getCount()];
				do {
					long id1 = cursor.getLong(0);
					String path = cursor.getString(1);
					String dir = path.substring(0, path.lastIndexOf("/"));
					// LogUtil.d(LOGTAG, "initData dir:" + dir);
					if (currentid != -1 && currentid == id1) {
						pos = i;
					}
					if (!pathsXml.contains(dir)) {
						mPlayList[i] = id1;
						i++;
					}
				} while (cursor.moveToNext());
				LogUtil.d(LOGTAG, "mPlayList.length" + mPlayList.length + " i:" + i);
				mPlayListLen = i;
			} else {
				mPlayListLen = 0;
			}
			// modify by chenhl end
			if (pos != -1) {
				mPlayPos = pos;
			} else {
				mPlayPos = 0;
			}

		} catch (Exception e) {
			Log.d(LOGTAG, "chenhl initData error", e);
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return;
	}

	@Override
	public IBinder onBind(Intent intent) {
		mDelayedStopHandler.removeCallbacksAndMessages(null);
		mServiceInUse = true;
		return mBinder;
	}

	@Override
	public void onRebind(Intent intent) {
		mDelayedStopHandler.removeCallbacksAndMessages(null);
		mServiceInUse = true;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mServiceStartId = startId;
		mDelayedStopHandler.removeCallbacksAndMessages(null);

		if (intent != null) {
			String action = intent.getAction();
			String cmd = intent.getStringExtra("command");
			LogUtil.d(LOGTAG, "-- ----- onStartCommand " + action + " / " + cmd);
			if (CMDNEXT.equals(cmd) || NEXT_ACTION.equals(action)) {
				// add by JXH begin 20150711
				if (mAudioManager.isMusicActive() && !isPlaying()) {// 第三方音乐正在播放
					return START_STICKY;
				}
				// add by JXH end 20150711
				gotoNext(true);
			} else if (CMDPREVIOUS.equals(cmd) || PREVIOUS_ACTION.equals(action)) {
				// add by JXH begin 20150711
				if (mAudioManager.isMusicActive() && !isPlaying()) {// 第三方音乐正在播放
					return START_STICKY;
				}
				// add by JXH end 20150711
				// modify by chenhl start 20140614
				prev();
				/*
				 * if (position() < 2000) { prev(); } else { seek(0); play(); }
				 */
				// modify by chenhl end
			} else if (CMDTOGGLEPAUSE.equals(cmd) || TOGGLEPAUSE_ACTION.equals(action)) {
				// add by JXH begin 20150711
				if (mAudioManager.isMusicActive() && !isPlaying()) {// 第三方音乐正在播放
					return START_STICKY;
				}
				// add by JXH end 20150711
				if (isPlaying()) {
					pause();
					mPausedByTransientLossOfFocus = false;
				} else {
					// paul add for BUG #15048 JXH add mbOnline for BUG #15867
					if ((null == mPlayer || !mPlayer.isInitialized()) && !mbOnline) {
						mNeedtoPlay = true;
					} else {
						play();
					}
				}
			} else if (CMDPAUSE.equals(cmd) || PAUSE_ACTION.equals(action)) {
				pause();
				mPausedByTransientLossOfFocus = false;
			} else if (CMDFAVORITE.equals(cmd)) {// lory add
				if (!isFavorite()) {
					addToFavorite();
				} else {
					removeFromFavorites();
				}
			} else if (CMDPLAY.equals(cmd)) {
				play();
			} else if (CMDSTOP.equals(cmd)) {
				pause();
				mPausedByTransientLossOfFocus = false;
				seek(0);
			}
		}

		// make sure the service will shut down on its own if it was
		// just started but not bound to and nothing is playing
		mDelayedStopHandler.removeCallbacksAndMessages(null);
		Message msg = mDelayedStopHandler.obtainMessage();
		mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
		return START_STICKY;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		mServiceInUse = false;
		// Take a snapshot of the current playlist
		LogUtil.d(LOGTAG, "zll ---- onUnbind 1");
		saveQueue(true);
		if (isPlaying() || mPausedByTransientLossOfFocus) {
			// something is currently playing, or will be playing once
			// an in-progress action requesting audio focus ends, so don't stop
			// the service now.
			// stopSelf(mServiceStartId);//lory add
			return true;
		}

		// If there is a playlist but playback is paused, then wait a while
		// before stopping the service, so that pause/resume isn't slow.
		// Also delay stopping the service if we're transitioning between
		// tracks.
		LogUtil.d(LOGTAG, "zll ---- onUnbind 2");
		if (mPlayListLen > 0 || mMediaplayerHandler.hasMessages(TRACK_ENDED)) {
			Message msg = mDelayedStopHandler.obtainMessage();
			mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
			return true;
		}
		LogUtil.d(LOGTAG, "zll ---- onUnbind 3");
		// No active playlist, OK to stop the service right now
		stopSelf(mServiceStartId);
		return true;
	}

	private Handler mDelayedStopHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// Check again to make sure nothing is playing right now
			LogUtil.d(LOGTAG, "mDelayedStopHandler msg:" + msg);
			if (noStopService()) {
				return;
			}
			stopForeground(true);
			// save the queue again, because it might have changed
			// since the user exited the music app (because of
			// party-shuffle or because the play-position changed)

			LogUtil.d(LOGTAG, "---- handleMessage mDelayedStopHandler 1");
			saveQueue(true);
			stopSelf(mServiceStartId);
		}
	};

	private boolean noStopService() {
		if (isPlaying() || mPausedByTransientLossOfFocus || mServiceInUse || mMediaplayerHandler.hasMessages(TRACK_ENDED)) {
			return true;
		}
		if (mbOnPause) {
			return true;
		}
		return false;
	}

	/**
	 * Called when we receive a ACTION_MEDIA_EJECT notification.
	 * @param storagePath
	 *            path to mount point for the removed media
	 */
	public void closeExternalStorageFiles(String storagePath) {
		// stop playback and clean up if the SD card is going to be unmounted.
		// start add by chenhl
		// stop(true);
		pause(); // 暂停
		// end
		mSdMount = true;
		notifyChange(QUEUE_CHANGED);
		notifyChange(META_CHANGED);
		mSdMount = false;
	}

	/**
	 * Registers an intent to listen for ACTION_MEDIA_EJECT notifications. The
	 * intent will call closeExternalStorageFiles() if the external media is
	 * going to be ejected, so applications can clean up any files they have
	 * open.
	 */
	public void registerExternalStorageListener() {
		if (mUnmountReceiver == null) {
			mUnmountReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();

					if (action.equals(Intent.ACTION_MEDIA_MOUNTED) || action.equals(Intent.ACTION_MEDIA_EJECT)) {
						// modify by chenhl start 20140910
						if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
							saveQueue(true);
							mQueueIsSaveable = false;
							closeExternalStorageFiles(intent.getData().getPath());
						} else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
							mMediaMountedCount++;
							mCardId = MusicUtils.getCardId(MediaPlaybackService.this);
							// reloadQueue();
							mQueueIsSaveable = true;
							mSdMount = true;
							notifyChange(QUEUE_CHANGED);
							notifyChange(META_CHANGED);
							mSdMount = false;
							pause(); // 暂停
						}
						AuroraMusicUtil.getMountedStorage(MediaPlaybackService.this);
						// modify by chenhl end 20140910
					}
					try {
						List<String> storages = ((Application) getApplication()).getStoragePath();
						if (((Application) getApplication()).isHaveSdStorage() && MusicUtils.getIntPref(getApplicationContext(), "storage_select", 0) != 0) {
							Globals.initPath(storages.get(1));
							LogUtil.d(LOGTAG, "----------mUnmountReceiver---Globals.storagePath:" + Globals.storagePath + " ------:" + Globals.mSavePath);
						} else {
							Globals.initPath(storages.get(0));
							MusicUtils.setIntPref(getApplicationContext(), "storage_select", 0);
						}
					} catch (Exception e) {
						e.printStackTrace();
						MusicUtils.setIntPref(getApplicationContext(), "storage_select", 0);
					}

				}
			};
			IntentFilter iFilter = new IntentFilter();
			iFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);// 插入OTG
			iFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);// 拔出OTG
			iFilter.addAction(Intent.ACTION_MEDIA_CHECKING);// 插入OTG
			iFilter.addAction(Intent.ACTION_MEDIA_EJECT);// 拔出OTG
			iFilter.addDataScheme("file");
			registerReceiver(mUnmountReceiver, iFilter);
		}
	}

	private void updateOfFailed(String what) {
		Intent i = new Intent(what);
		i.putExtra("id", Long.valueOf(getAudioId()));
		i.putExtra("artist", getArtistName());
		i.putExtra("album", getAlbumName());
		i.putExtra("track", getTrackName());
		i.putExtra("playing", isPlaying());
		i.putExtra("isnet", mbOnline);
		i.putExtra("filepath", mLrcPath);
		i.putExtra("isfavorite", isFavorite());
		sendStickyBroadcast(i);

		// updateMyProvider();
		updateNotification(true);

		mAppWidgetProvider.notifyChange(this, what);
		return;
	}

	/**
	 * Notify the change-receivers that something has changed. The intent that
	 * is sent contains the following data for the currently playing track: "id"
	 * - Integer: the database row ID "artist" - String: the name of the artist
	 * "album" - String: the name of the album "track" - String: the name of the
	 * track The intent has an action that is one of
	 * "com.android.music.metachanged" "com.android.music.queuechanged",
	 * "com.android.music.playbackcomplete" "com.android.music.playstatechanged"
	 * respectively indicating that a new track has started playing, that the
	 * playback queue has changed, that playback has stopped because the last
	 * file in the list has been played, or that the play-state changed
	 * (paused/resumed).
	 */
	@SuppressWarnings("deprecation")
	private void notifyChange(String what) {
		// AuroraMusicUtil.justTest();
		Intent i = new Intent(what);
		i.putExtra("id", Long.valueOf(getAudioId()));
		i.putExtra("artist", getArtistName());
		i.putExtra("album", getAlbumName());
		i.putExtra("track", getTrackName());
		i.putExtra("playing", isPlaying());
		i.putExtra("isnet", mbOnline);
		i.putExtra("filepath", mLrcPath);// lory add
		i.putExtra("isfavorite", isFavorite());// lory add
		sendStickyBroadcast(i);
		if (what.equals(AURORALRC_CHANGED)) {
			return;
		}

		if (what.equals(PLAYSTATE_CHANGED)) {
			mRemoteControlClient.setPlaybackState(isPlaying() ? RemoteControlClient.PLAYSTATE_PLAYING : RemoteControlClient.PLAYSTATE_PAUSED);
		} else if (what.equals(META_CHANGED)) {
			// add by ukiliu 20140721 begin
			String filepath = MusicUtils.getDbImg(this, getAudioId(), getTrackName(), getArtistName(), 0);
			if (filepath != null && filepath.equals(AuroraSearchLyricActivity.DEFUALT_IMG_URL)) {
				mAlbumArtBitmap = null;
			} else {
				if (mbOnline) {
					// modify by chenhl start 20140901 for 修改在线音乐图片
					Bitmap bm = null;
					if (filepath != null && !filepath.isEmpty()) {
						bm = BitmapUtil.decodeSampledBitmapFromFileForSmall(filepath, mNotifyWidth, mNotifyWidth);
					}
					if (bm == null) {
						String imgpath = AuroraMusicUtil.getImgPath(getApplication(), AuroraMusicUtil.MD5(getTrackName() + getArtistName() + getAlbumName()));

						bm = BitmapUtil.decodeSampledBitmapFromFileForSmall(imgpath, mNotifyWidth, mNotifyWidth);
					}
					if (bm == null) {
						mAlbumArtBitmap = null;
						onUpdateOnlineArtImg();
					} else {
						mAlbumArtBitmap = bm;
					}
					// modify by chenhl end 20140901
				} else {

					Bitmap bitmap = null;
					String strpath = AuroraMusicUtil.getImgPath(getApplication(), AuroraMusicUtil.MD5(getTrackName() + getArtistName() + getAlbumName()));
					LogUtil.d(LOGTAG, "filepath:" + filepath + " strpath:" + strpath);
					if (filepath != null && filepath.equals(AuroraSearchLyricActivity.AURORA_ORIGINAL_IMG_URL)) {
						LogUtil.d(LOGTAG, "AURORA_ORIGINAL_IMG_URL-----------------");
						bitmap = MusicUtils.getAuroraArtwork(this, getAudioId(), -1, mNotifyWidth, mNotifyWidth);
					} else {
						bitmap = MusicUtils.getSmallArtwork(this, getAudioId(), getAlbumId(), false, strpath, 0, mNotifyWidth, mNotifyWidth);
					}

					if (bitmap == null) {
						mAlbumArtBitmap = null;
						if (AuroraMusicUtil.isWifiNetActvie(getApplicationContext()) && Globals.SWITCH_FOR_ONLINE_MUSIC) {
							onUpdateOnlineArtImg();
						}
					} else {
						mAlbumArtBitmap = bitmap;
					}
				}
			}
			// add by ukiliu 20140721 end
			RemoteControlClient.MetadataEditor ed = mRemoteControlClient.editMetadata(true);
			ed.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, getTrackName());
			ed.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, getAlbumName());
			ed.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, getArtistName());
			ed.putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, duration());
			Bitmap b = null;
			if (b != null) {
				ed.putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK, b);
			}
			ed.apply();
		}

		// aurora ukiliu 2014-06-03 add begin
		if (!mSdMount) {
			updateNotification(false);
		}
		// aurora ukiliu 2014-06-03 add end

		if (what.equals(QUEUE_CHANGED)) {
			saveQueue(true);
		} else {
			saveQueue(false);
		}

		// Share this notification directly with our widgets
		mAppWidgetProvider.notifyChange(this, what);
	}

	private void ensurePlayListCapacity(int size) {
		if (mPlayList == null || size > mPlayList.length) {
			// reallocate at 2x requested size so we don't
			// need to grow and copy the array for every
			// insert
			long[] newlist = new long[size * 2];
			int len = mPlayList != null ? mPlayList.length : mPlayListLen;
			// LogUtil.d(LOGTAG, "------mPlayList:" + mPlayList + "  len:" +
			// len);
			for (int i = 0; i < len; i++) {
				newlist[i] = mPlayList[i];
			}
			mPlayList = newlist;
			// LogUtil.d(LOGTAG, "--222----mPlayList:" + mPlayList + "  len:" +
			// len);
		}
		// FIXME: shrink the array when the needed size is much smaller
		// than the allocated size
	}

	// insert the list of songs at the specified position in the playlist
	private void addToPlayList(long[] list, int position) {

		int addlen = list.length;
		if (position < 0) { // overwrite
			mPlayListLen = 0;
			position = 0;
		}
		ensurePlayListCapacity(mPlayListLen + addlen);
		if (position > mPlayListLen) {
			position = mPlayListLen;
		}

		// move part of list after insertion point
		int tailsize = mPlayListLen - position;
		for (int i = tailsize; i > 0; i--) {
			mPlayList[position + i] = mPlayList[position + i - addlen];
		}

		// copy list into playlist
		for (int i = 0; i < addlen; i++) {
			mPlayList[position + i] = list[i];
		}
		mPlayListLen += addlen;
		if (mPlayListLen == 0) {
			mCursor.close();
			mCursor = null;
			notifyChange(META_CHANGED);
		}
	}

	/**
	 * Appends a list of tracks to the current playlist. If nothing is playing
	 * currently, playback will be started at the first track. If the action is
	 * NOW, playback will switch to the first of the new tracks immediately.
	 * @param list
	 *            The list of tracks to append.
	 * @param action
	 *            NOW, NEXT or LAST
	 */
	public void enqueue(long[] list, int action) {
		synchronized (this) {
			if (action == NEXT && mPlayPos + 1 < mPlayListLen) {
				addToPlayList(list, mPlayPos + 1);
				notifyChange(QUEUE_CHANGED);
			} else {
				addToPlayList(list, Integer.MAX_VALUE);
				notifyChange(QUEUE_CHANGED);
				if (action == NOW) {
					mPlayPos = mPlayListLen - list.length;
					openCurrentAndNext();
					play();
					notifyChange(META_CHANGED);
					return;
				}
			}
			if (mPlayPos < 0) {
				mPlayPos = 0;
				openCurrentAndNext();
				play();
				notifyChange(META_CHANGED);
			}
		}
	}

	/**
	 * Replaces the current playlist with a new list, and prepares for starting
	 * playback at the specified position in the list, or a random position if
	 * the specified position is 0.
	 * @param list
	 *            The new list of tracks.
	 */
	public void open(long[] list, int position) {
		mSingleSong = false;
		mbOnline = isNetMusic(list, position);

		if (mbOnline) {
			if (mShuffleMode == SHUFFLE_AUTO) {
				mShuffleMode = SHUFFLE_NORMAL;
			}

			long oldId = -1;
			int listlength = list.length;
			boolean newlist = true;
			if (mPlayListLen == listlength) {
				// possible fast path: list might be the same
				newlist = false;
				for (int i = 0; i < listlength; i++) {
					if (list[i] != mPlayList[i]) {
						newlist = true;
						break;
					}
				}
			}
			if (newlist) {
				addToPlayList(list, -1);
				notifyChange(QUEUE_CHANGED);
			}

			if (mAuroraXiamiPlayer != null) {
				mAuroraXiamiPlayer.initListId(mPlayList);
			}

			int oldpos = mPlayPos;
			if (position >= 0) {
				mPlayPos = position;
			} else {
				mPlayPos = mRand.nextInt(mPlayListLen);
			}
			mHistory.clear();
			openCurrent();
		} else {
			synchronized (this) {
				if (mShuffleMode == SHUFFLE_AUTO) {
					mShuffleMode = SHUFFLE_NORMAL;
				}
				long oldId = getAudioId();
				int listlength = list.length;
				boolean newlist = true;
				if (mPlayListLen == listlength) {
					// possible fast path: list might be the same
					newlist = false;
					for (int i = 0; i < listlength; i++) {
						if (list[i] != mPlayList[i]) {
							newlist = true;
							break;
						}
					}
				}
				if (newlist) {
					addToPlayList(list, -1);
					notifyChange(QUEUE_CHANGED);
				}
				if (mAuroraXiamiPlayer != null) {
					mAuroraXiamiPlayer.initListId(mPlayList);
				}
				int oldpos = mPlayPos;
				if (position >= 0) {
					mPlayPos = position;
				} else {
					mPlayPos = mRand.nextInt(mPlayListLen);
				}
				mHistory.clear();

				saveBookmarkIfNeeded();

				// modify by chenhl 20140620
				// openCurrentAndNext();
				openCurrent();
				// modify by chenhl end
				// modify by tangjie 2014/6/11
				// if (oldId != getAudioId()) {
				notifyChange(META_CHANGED);
				// }
			}
		}

	}

	public void shuffleOpen(long[] list, int position) {
		mSingleSong = false;
		synchronized (this) {
			if (mShuffleMode == SHUFFLE_AUTO) {
				mShuffleMode = SHUFFLE_NORMAL;
			}
			int listlength = list.length;
			boolean newlist = true;
			if (mPlayListLen == listlength) {
				// possible fast path: list might be the same
				newlist = false;
				for (int i = 0; i < listlength; i++) {
					if (list[i] != mPlayList[i]) {
						newlist = true;
						break;
					}
				}
			}
			if (newlist) {
				addToPlayList(list, -1);
				notifyChange(QUEUE_CHANGED);
			}
			position = -1;
			if (position >= 0) {
				mPlayPos = position;
			} else {
				mPlayPos = mRand.nextInt(mPlayListLen);
			}
			mHistory.clear();

			mbOnline = isNetMusic(list, mPlayPos);
			if (!mbOnline) {
				saveBookmarkIfNeeded();
			}
			openCurrentAndNext();
			notifyChange(META_CHANGED);
		}
	}

	/**
	 * Moves the item at index1 to index2.
	 * @param index1
	 * @param index2
	 */
	public void moveQueueItem(int index1, int index2) {
		synchronized (this) {
			if (index1 >= mPlayListLen) {
				index1 = mPlayListLen - 1;
			}
			if (index2 >= mPlayListLen) {
				index2 = mPlayListLen - 1;
			}
			if (index1 < index2) {
				long tmp = mPlayList[index1];
				for (int i = index1; i < index2; i++) {
					mPlayList[i] = mPlayList[i + 1];
				}
				mPlayList[index2] = tmp;
				if (mPlayPos == index1) {
					mPlayPos = index2;
				} else if (mPlayPos >= index1 && mPlayPos <= index2) {
					mPlayPos--;
				}
			} else if (index2 < index1) {
				long tmp = mPlayList[index1];
				for (int i = index1; i > index2; i--) {
					mPlayList[i] = mPlayList[i - 1];
				}
				mPlayList[index2] = tmp;
				if (mPlayPos == index1) {
					mPlayPos = index2;
				} else if (mPlayPos >= index2 && mPlayPos <= index1) {
					mPlayPos++;
				}
			}
			notifyChange(QUEUE_CHANGED);
		}
	}

	/**
	 * Returns the current play list
	 * @return An array of integers containing the IDs of the tracks in the play
	 *         list
	 */
	public long[] getQueue() {
		synchronized (this) {
			int len = mPlayListLen;
			long[] list = new long[len];
			for (int i = 0; i < len; i++) {
				list[i] = mPlayList[i];
			}
			return list;
		}
	}

	private Cursor getCursorForId(long lid) {
		String id = String.valueOf(lid);
		Cursor c = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mCursorCols, "_id=" + id, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	private boolean isLocalEmpty() {
		boolean flag = true;
		Cursor c = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Audio.Media._ID }, Globals.QUERY_SONG_FILTER, null, null);
		if (c != null) {
			if (c.getCount() > 0) {
				flag = false;
			}

			c.close();
		}

		return flag;
	}

	private void openCurrentAndNextForPrev() {
		LogUtil.d(LOGTAG, "openCurrentAndNextForPrev");
		synchronized (this) {
			if (mCursor != null) {
				mCursor.close();
				mCursor = null;
			}
			if (mPlayListLen == 0) {
				return;
			}
			stop(false);

			mCursor = getCursorForId(mPlayList[mPlayPos]);
			while (true) {
				if (mCursor != null && mCursor.getCount() != 0 && open(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + mCursor.getLong(IDCOLIDX))) {
					break;
				}
				// if we get here then opening the file failed. We can close the
				// cursor now, because
				// we're either going to create a new one next, or stop trying
				if (mCursor != null) {
					mCursor.close();
					mCursor = null;
				}

				if (mOpenFailedCounter++ < 10 && mPlayListLen > 1) {
					// add by chenhl start 20140620
					// int pos = getNextPosition(false, true);
					int pos = mPlayPos;
					if (mRepeatMode == REPEAT_CURRENT) {
						if (mPlayPos <= 0) {
							pos = mPlayListLen - 1;
						} else {
							pos--;
						}
					} else {
						pos = getNextPosition(false, true);
					}
					// add by chenhl end
					if (pos < 0) {
						gotoIdleState();
						if (mIsSupposedToBePlaying) {
							mIsSupposedToBePlaying = false;
							notifyChange(PLAYSTATE_CHANGED);
						}
						return;
					}
					mPlayPos = pos;
					stop(false);
					mPlayPos = pos;
					mCursor = getCursorForId(mPlayList[mPlayPos]);
				} else {
					mOpenFailedCounter = 0;
					if (!mQuietMode) {
						Toast.makeText(this, R.string.playback_failed, Toast.LENGTH_SHORT).show();
					}
					gotoIdleState();
					if (mIsSupposedToBePlaying) {
						mIsSupposedToBePlaying = false;
						notifyChange(PLAYSTATE_CHANGED);
					}
					return;
				}
			}

			if (isPodcast()) {
				long bookmark = getBookmark();
				seek(bookmark - 5000);
			}
			setNextTrack();
			mbPrev = 1;
		}
	}

	// add by chenhl start 20140620
	private void openCurrent() {
		AuroraMusicUtil.justTest();
		LogUtil.d(LOGTAG, "openCurrent");
		if (mbOnline) {
			if (mCursor != null) {
				mCursor.close();
				mCursor = null;
			}

			if (mPlayListLen == 0) {
				return;
			}

			stop(false);
			if (mAuroraXiamiPlayer != null) {
				if (mPlayer != null) {
					mPlayer.resetOfOnlinePlay();
				}
				if (mLocalPlayer != null) {
					mLocalPlayer.resetOfOnlinePlay();
				}
				mAuroraXiamiPlayer.open(mPlayPos);
			}
		} else {
			synchronized (this) {
				if (mCursor != null) {
					mCursor.close();
					mCursor = null;
				}
				if (mPlayListLen == 0) {
					LogUtil.d(LOGTAG, "openCurrent mPlayListLen==0 ");
					return;
				}
				stop(false);
				mCursor = getCursorForId(mPlayList[mPlayPos]);
				if (mCursor != null && mCursor.getCount() != 0 && open(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + mCursor.getLong(IDCOLIDX))) {
					if (isPodcast()) {
						long bookmark = getBookmark();
						seek(bookmark - 5000);
					}
					setNextTrack();
					mbPrev = 2;
					LogUtil.d(LOGTAG, "openCurrent mCursor ");
				} else {
					LogUtil.d(LOGTAG, "openCurrent mCursor mQuietMode:" + mQuietMode);
					if (!mQuietMode) {
						mMediaplayerHandler.sendEmptyMessage(PLAYFAILED);
					}
					mCursor = getCursorForId(mPlayList[mPlayPos]);// lory add
																	// 2014.6.20
					gotoIdleState();
					if (mIsSupposedToBePlaying) {
						mIsSupposedToBePlaying = false;
						notifyChange(PLAYSTATE_CHANGED);
					}
				}
			}
		}

	}

	private void onEarChanged(boolean on) {
		if (on && isPlaying()) {
			openCloseHIfi(true);
		}
		return;
	}

	private void updateCursor() {
		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
		}

		if (mPlayListLen == 0) {
			return;
		}
		mCursor = getCursorForId(mPlayList[mPlayPos]);
	}

	// add by chenhl end 20140830

	private void openCurrentAndNext() {
		AuroraMusicUtil.justTest();
		LogUtil.d(LOGTAG, "openCurrentAndNext");
		if (mbOnline) {
			if (mCursor != null) {
				mCursor.close();
				mCursor = null;
			}
			if (mPlayListLen == 0) {
				return;
			}
			if (!AuroraMusicUtil.isNetWorkActive(MediaPlaybackService.this)) {
				return;
			}
			stop(false);
			if (mAuroraXiamiPlayer != null) {
				if (mPlayer != null) {
					mPlayer.resetOfOnlinePlay();
				}
				mAuroraXiamiPlayer.open(mPlayPos);
			}

			setNextTrack();
			return;
		}

		synchronized (this) {
			if (mCursor != null) {
				mCursor.close();
				mCursor = null;
			}
			if (mPlayListLen == 0) {
				return;
			}
			stop(false);

			mCursor = getCursorForId(mPlayList[mPlayPos]);
			while (true) {
				if (mCursor != null && mCursor.getCount() != 0 && open(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + mCursor.getLong(IDCOLIDX))) {
					LogUtil.d(LOGTAG, "-------------------open----IDCOLIDX:" + IDCOLIDX);
					break;
				}
				// if we get here then opening the file failed. We can close the
				// cursor now, because
				// we're either going to create a new one next, or stop trying
				if (mCursor != null) {
					mCursor.close();
					mCursor = null;
				}

				if (mOpenFailedCounter++ < 10 && mPlayListLen > 1) {

					// add by chenhl start 20140620
					int pos = mPlayPos;
					if (mRepeatMode == REPEAT_CURRENT) {
						if (mPlayPos >= mPlayListLen - 1) {
							pos = 0;
						} else {
							pos++;
						}
					} else {
						pos = getNextPosition(false, false);
					}
					if (mShuffleMode == SHUFFLE_NORMAL) {
						if (mHistory.size() > MAX_HISTORY_SIZE) {
							mHistory.removeElementAt(0);
						}
						if (mPlayPos >= 0) {
							mHistory.add(mPlayPos);
						}
					} else {
						mHistory.clear();
					}
					// add by chenhl end
					if (pos < 0) {
						gotoIdleState();
						if (mIsSupposedToBePlaying) {
							mIsSupposedToBePlaying = false;
							notifyChange(PLAYSTATE_CHANGED);
						}
						return;
					}
					mPlayPos = pos;
					stop(false);
					mPlayPos = pos;
					mCursor = getCursorForId(mPlayList[mPlayPos]);
				} else {
					mOpenFailedCounter = 0;
					if (!mQuietMode) {
						mMediaplayerHandler.sendEmptyMessage(PLAYFAILED);
					}
					// add by JXH begin
					LogUtil.d(LOGTAG, "----openCurrentAndNext----clear");
					mHistory.clear();
					mPlayPos = -1;
					mNextPlayPos = -1;
					mPlayListLen = 0;
					mPlayList = null;
					mAutoShuffleList = null;
					// add by JXH end
					gotoIdleState();
					if (mIsSupposedToBePlaying) {
						mIsSupposedToBePlaying = false;
						notifyChange(PLAYSTATE_CHANGED);
					}
					return;
				}
			}

			// go to bookmark if needed
			if (isPodcast()) {
				long bookmark = getBookmark();
				// Start playing a little bit before the bookmark,
				// so it's easier to get back in to the narrative.
				seek(bookmark - 5000);
			}
			setNextTrack();

		}
	}

	private void setNextTrack() {
		if (mbPrev != 0) {
			mbPrev = 0;
		}
		mNextPlayPos = getNextPosition(false, false);

		// Iuni <lory><2014-06-09> modify begin
		if (mNextPlayPos >= 0 && mPlayList != null && mNextPlayPos <= mPlayList.length - 1) {
			long id = mPlayList[mNextPlayPos];
			if (!isNet(id)) {
				String path = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + id;
				boolean dataSource = false;
				nextAudioId = id;
				if (mPlayer != null) {
					dataSource = mPlayer.setNextDataSource(path);

				}

			}
		}

		return;
		// Iuni <lory><2014-06-09> modify end
	}

	// Iuni <lory><2014-06-09> add begin
	public boolean auroraOpen(String path) {
		mbOnline = false;
		if (!mbOnline) {
			LogUtil.d(LOGTAG, "----- auroraOpen ---2 path:" + path);
			if (!open(path)) {
				LogUtil.d(LOGTAG, "----- auroraOpen ---3 path:" + path);
				long[] mPlayList = getQueue();
				open(mPlayList, getQueuePosition());

				return true;
			} else {
				LogUtil.d(LOGTAG, "----- auroraOpen ---4 path:" + path);
				// mRepeatMode = REPEAT_CURRENT;
				mSingleSong = true;
				// setNextTrack();
			}
		}

		return true;
	}

	// Iuni <lory><2014-06-09> add end

	/**
	 * Opens the specified file and readies it for playback.
	 * @param path
	 *            The full path of the file to be opened.
	 */
	public boolean open(String path) {
		synchronized (this) {
			if (path == null) {
				return false;
			}

			// if mCursor is null, try to associate path with a database cursor
			if (mCursor == null) {

				ContentResolver resolver = getContentResolver();
				Uri uri;
				String where;
				String selectionArgs[];
				if (path.startsWith("content://media/")) {
					uri = Uri.parse(path);
					where = null;
					selectionArgs = null;
				} else {
					uri = MediaStore.Audio.Media.getContentUriForPath(path);
					where = MediaStore.Audio.Media.DATA + "=?";
					selectionArgs = new String[] { path };
				}

				try {
					mCursor = resolver.query(uri, mCursorCols, where, selectionArgs, null);
					if (mCursor != null) {
						if (mCursor.getCount() == 0) {
							mCursor.close();
							mCursor = null;
						} else {
							mCursor.moveToNext();
							ensurePlayListCapacity(1);
							mPlayListLen = 1;
							mPlayList[0] = mCursor.getLong(IDCOLIDX);
							mPlayPos = 0;
						}
					}
				} catch (UnsupportedOperationException ex) {
				}
			}
			mFileToPlay = path;
			String lpath = getFilePath();
			if (!TextUtils.isEmpty(mFileToPlay)) {
				if (mPlayer != null) {
					mPlayer.setDataSourceAsync(mFileToPlay);
					if (mPlayer.isInitialized()) {
						mOpenFailedCounter = 0;
						return true;
					}
				}

				if (mLocalPlayer != null) {
					if (mAuroraXiamiPlayer != null) {
						mAuroraXiamiPlayer.resetOfLocalPlay();
					}
					// String lpath = getFilePath();
					// add by chenhl start for 参展
					if (Globals.SWITCH_FOR_CANZHAN) {
						String lenpixx = lpath.substring(lpath.lastIndexOf(".") + 1);
						LogUtil.d(LOGTAG, "lenpixx:" + lenpixx);
						if (lenpixx.equalsIgnoreCase("mp3")) {
							stop(true);
							return false;
						}
					}
					// add by chenhl end for 参展
					if (lpath != null) {
						mLocalPlayer.open(Uri.parse(lpath));
						return true;
					}
				}
			}

			stop(true);
			return false;
		}
	}

	/**
	 * Starts playback of a previously opened file.
	 */
	public void play() {
		// add by chenhl for headset
		LogUtil.d(LOGTAG, "------------play");
		// AuroraMusicUtil.justTest();
		if (AuroraMusicUtil.isFromHeadset()) {
			return;
		}
		// modify by JXH begin
		if (AudioManager.AUDIOFOCUS_REQUEST_FAILED == mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)) {
			LogUtil.e(LOGTAG, "<< play: phone call is ongoing, can not play music!");
			return;

		}
		// modify by JXH end
		mAudioManager.registerMediaButtonEventReceiver(new ComponentName(this.getPackageName(), MediaButtonIntentReceiver.class.getName()));

		if (mbOnline) {
			if (mAuroraXiamiPlayer != null && mAuroraXiamiPlayer.isOnlineInitialized()) {
				LogUtil.d(LOGTAG, "-----------1--------------mAuroraXiamiPlayer.play() ");
				boolean play = mAuroraXiamiPlayer.play();
				if (!play) {
					LogUtil.d(LOGTAG, "----------2---------no-----mAuroraXiamiPlayer.play() ");
					mIsSupposedToBePlaying = false;
					Toast.makeText(MediaPlaybackService.this, R.string.aurora_network_error, Toast.LENGTH_SHORT).show();
					notifyChange(PLAYSTATE_CHANGED);
					return;
				}
				mbOnPause = false;

				mMediaplayerHandler.removeMessages(FADEDOWN);
				mMediaplayerHandler.sendEmptyMessage(FADEUP);

				if (!mIsSupposedToBePlaying) {
					mIsSupposedToBePlaying = true;
					notifyChange(PLAYSTATE_CHANGED);
				}
			}
			return;
		}

		if (mLocalPlayer != null && mLocalPlayer.isInitialized()) {
			mLocalPlayer.start();
			mbOnPause = false;
			LogUtil.d(LOGTAG, "-----------2--------------mLocalPlayer.play() ");
			mMediaplayerHandler.removeMessages(FADEDOWN);
			mMediaplayerHandler.sendEmptyMessage(FADEUP);
			updateNotification(false);
			if (!mIsSupposedToBePlaying) {
				mIsSupposedToBePlaying = true;
				notifyChange(PLAYSTATE_CHANGED);
			}
			return;
		}

		if (mPlayer == null) {
			mMediaplayerHandler.sendEmptyMessage(PLAYFAILED);
			return;
		}

		if (mPlayer.isInitialized()) {
			LogUtil.d(LOGTAG, "mPlayer.isInitialized:" + mPlayer.isInitialized());
			// if we are at the end of the song, go to the next song first
			long duration = mPlayer.duration();
			if (mRepeatMode != REPEAT_CURRENT && duration > 2000 && mPlayer.position() >= duration - 2000) {
				if (gotoNext(false)) {
					LogUtil.d(LOGTAG, "gotoNext(false)");
					return;
				}
			}
			LogUtil.d(LOGTAG, "-----------3--------------mPlayer.play() ");
			mPlayer.start();
			mbOnPause = false;
			// make sure we fade in, in case a previous fadein was stopped
			// because
			// of another focus loss
			mMediaplayerHandler.removeMessages(FADEDOWN);
			mMediaplayerHandler.sendEmptyMessage(FADEUP);

			updateNotification(false);
			if (!mIsSupposedToBePlaying) {
				mIsSupposedToBePlaying = true;
				notifyChange(PLAYSTATE_CHANGED);
			}

		} else {
			mMediaplayerHandler.removeMessages(PLAYFAILED);
			mMediaplayerHandler.sendEmptyMessage(PLAYFAILED);
		}
	}

	private void updateNotify() {
		String filepath = MusicUtils.getDbImg(this, getAudioId(), getTrackName(), getArtistName(), 0);
		if (filepath != null && filepath.equals(AuroraSearchLyricActivity.DEFUALT_IMG_URL)) {
			mAlbumArtBitmap = null;
		} else if (mbOnline) {

		} else {
			String strpath = AuroraMusicUtil.getImgPath(getApplication(), AuroraMusicUtil.MD5(getTrackName() + getArtistName() + getAlbumName()));
			LogUtil.d(LOGTAG, "updateNotify strpath:" + strpath);
			if (filepath != null && filepath.equals(AuroraSearchLyricActivity.AURORA_ORIGINAL_IMG_URL)) {
				LogUtil.d(LOGTAG, "AURORA_ORIGINAL_IMG_URL-----------------");
				mAlbumArtBitmap = MusicUtils.getAuroraArtwork(this, getAudioId(), -1, mNotifyWidth, mNotifyWidth);
			} else {
				mAlbumArtBitmap = MusicUtils.getSmallArtwork(this, getAudioId(), getAlbumId(), false, strpath, 0, mNotifyWidth, mNotifyWidth);
			}

		}
		updateNotification(false);
	}

	// aurora ukiliu 2014-06-03 modify begin
	private void updateNotification(boolean flag) {
		boolean tmpFlag = flag;

		RemoteViews views = new RemoteViews(getPackageName(), R.layout.aurora_music_statusbar);
		// add by JXH for new UI begin
		Bitmap drawingBitmap = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), mask.getConfig());
		Canvas canvas = new Canvas(drawingBitmap);
		Paint paint = new Paint();
		canvas.drawBitmap(mask, 0, 0, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		if (mAlbumArtBitmap == null) {
			canvas.drawBitmap(mDefautBitmap, 0f, 0f, paint);
		} else {
			Bitmap mAlbumArt = AuroraMusicUtil.zoomImage(mAlbumArtBitmap, mask.getWidth(), mask.getHeight());
			canvas.drawBitmap(mAlbumArt, 0f, 0f, paint);
		}
		views.setImageViewBitmap(R.id.icon, drawingBitmap);
		// add by JXH for new UI end
		// aurora uki.liu modify for UI end
		LogUtil.d(LOGTAG, "updateNotification getAudioId():" + getAudioId() + " tmpFlag:" + tmpFlag);
		if (getAudioId() < 0 && !tmpFlag) {
			// streaming
			if (mbOnline) {
				views.setTextViewText(R.id.trackname, null);
				views.setTextViewText(R.id.artistalbum, null);
			} else {
				views.setTextViewText(R.id.trackname, getPath());
				views.setTextViewText(R.id.artistalbum, null);
			}
			return;
		} else {
			String artist = getArtistName();
			String trackname = getTrackName();
			if (trackname == null || trackname.equalsIgnoreCase(MediaStore.UNKNOWN_STRING)) {
				trackname = getString(R.string.unknown_track);
			}
			views.setTextViewText(R.id.trackname, trackname);
			if (artist == null || artist.equals(MediaStore.UNKNOWN_STRING)) {
				artist = getString(R.string.unknown_artist_name);
			}
			String album = getAlbumName();
			album =	AuroraMusicUtil.doAlbumName(getFilePath(), album);
			if (album == null || album.equals(MediaStore.UNKNOWN_STRING)) {
				album = getString(R.string.unknown_album_name);
			}

			views.setTextViewText(R.id.artistalbum, getString(R.string.aurora_notification_artist_album, artist, album));
		}

		if (isPlaying()) {
			views.setImageViewResource(R.id.playornot, R.drawable.aurora_statusbar_btn_play_not);
		} else {
			views.setImageViewResource(R.id.playornot, R.drawable.aurora_statusbar_btn_play);
		}
		Notification.Builder builder = new Notification.Builder(this);
		builder.setPriority(Notification.PRIORITY_HIGH);
		// add by ukiliu 2014-08-04 begin
		if (isPlaying()) {
			builder.setOngoing(true);
		} else {
			builder.setOngoing(false);
		}
		// add by ukiliu 2014-08-04 end
		Notification status = builder.build();
		status.contentView = views;
		// modify by ukiliu 2014-08-04 begin
		if (isPlaying()) {
			status.flags |= Notification.FLAG_ONGOING_EVENT;
		}
		// modify by ukiliu 2014-08-04 end

		status.icon = R.drawable.stat_notify_musicplayer;
		Intent intent = new Intent("com.android.music.PLAYBACK_VIEWER");
		status.contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
		if (Build.VERSION.SDK_INT < 21) {
			status.bigContentView = views;// 5.0以下这个VIEW不设置回导致整个VIEW空白
		}
		Intent btnPlayIntent = new Intent(TOGGLEPAUSE_ACTION);
		PendingIntent pendPlayIntent = PendingIntent.getBroadcast(this, 0, btnPlayIntent, 0);
		Intent btnNextIntent = new Intent(NEXT_ACTION);
		PendingIntent pendNextIntent = PendingIntent.getBroadcast(this, 0, btnNextIntent, 0);
		// del by JXH for new UI begin
		// Intent btnPrevIntent = new Intent(PREVIOUS_ACTION);
		// PendingIntent pendPrevIntent = PendingIntent.getBroadcast(this, 0,
		// btnPrevIntent, 0);
		//
		// if (Application.isRadioType()) {
		// views.setImageViewResource(R.id.play_left,
		// R.drawable.aurora_statusbar_btn_play_left_enable);
		// views.setOnClickPendingIntent(R.id.play_left, null);
		// } else {
		// views.setImageViewResource(R.id.play_left,
		// R.drawable.aurora_statusbar_btn_play_left);
		// views.setOnClickPendingIntent(R.id.play_left, pendPrevIntent);
		// }

		// del by JXH for new UI end
		PendingIntent pendCloseIntent = PendingIntent.getBroadcast(this, 0, new Intent(CLOSE_ACTION), 0);
		views.setOnClickPendingIntent(R.id.id_close, pendCloseIntent);
		views.setOnClickPendingIntent(R.id.playornot, pendPlayIntent);
		views.setOnClickPendingIntent(R.id.play_right, pendNextIntent);
		// modify by ukiliu 2014-08-04 begin
		startForeground(PLAYBACKSERVICE_STATUS, status);
		// modify by ukiliu 2014-08-04 end
	}

	// aurora ukiliu 2014-06-03 modify end

	private void stop(boolean remove_status_icon) {
		LogUtil.d(LOGTAG, "-----stop:" + remove_status_icon);
		if (mAuroraXiamiPlayer != null && mAuroraXiamiPlayer.isOnlineInitialized()) {
			mAuroraXiamiPlayer.stop();
		}

		if (mPlayer != null && mPlayer.isInitialized()) {
			mPlayer.stop();
		}

		if (mLocalPlayer != null && mLocalPlayer.isInitialized()) {
			mLocalPlayer.stop();
		}

		mFileToPlay = null;
		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
		}

		if (remove_status_icon) {
			gotoIdleState();
		} /*
		 * else {fix bug 按通知栏下一曲通知栏被关闭 5.1MTK stopForeground(false); }
		 */
		if (remove_status_icon) {
			mIsSupposedToBePlaying = false;
			notifyChange(PLAYSTATE_CHANGED);
		}
	}

	/**
	 * Stops playback.
	 */
	public void stop() {
		stop(true);
	}

	/**
	 * Pauses playback (call play() to resume)
	 */
	public void pause() {
		LogUtil.d(LOGTAG, "------pause");
		synchronized (this) {
			mMediaplayerHandler.removeMessages(FADEUP);
			if (mbOnline) {
				// Log.i(LOGTAG, "zll ---- pause 1");
				if (isPlaying()) {
					boolean flag = mAuroraXiamiPlayer.pause();
					mbOnPause = true;
					gotoIdleState();
					mIsSupposedToBePlaying = flag ? false : true;
					notifyChange(PLAYSTATE_CHANGED);
				}
				return;
			}

			if (isPlaying()) {
				if (mLocalPlayer != null && mLocalPlayer.isInitialized()) {
					mLocalPlayer.pause();
				} else {
					mPlayer.pause();
				}
				mbOnPause = true;
				gotoIdleState();
				mIsSupposedToBePlaying = false;
				notifyChange(PLAYSTATE_CHANGED);
				saveBookmarkIfNeeded();
			}
		}
	}

	/**
	 * Returns whether something is currently playing
	 * @return true if something is playing (or will be playing shortly, in case
	 *         we're currently transitioning between tracks), false if not.
	 */
	public boolean isPlaying() {
		if (mbOnline) {
			if (mAuroraXiamiPlayer != null) {
				return mIsSupposedToBePlaying;
			}
			return false;
		}

		if (null != mLocalPlayer && mLocalPlayer.isInitialized()) {
			return mIsSupposedToBePlaying;
		}

		if (mPlayer != null) {
			return mIsSupposedToBePlaying;
		} else {
			return false;
		}
		// return mIsSupposedToBePlaying;
	}

	/*
	 * Desired behavior for prev/next/shuffle:
	 * 
	 * - NEXT will move to the next track in the list when not shuffling, and to
	 * a track randomly picked from the not-yet-played tracks when shuffling. If
	 * all tracks have already been played, pick from the full set, but avoid
	 * picking the previously played track if possible. - when shuffling, PREV
	 * will go to the previously played track. Hitting PREV again will go to the
	 * track played before that, etc. When the start of the history has been
	 * reached, PREV is a no-op. When not shuffling, PREV will go to the
	 * sequentially previous track (the difference with the shuffle-case is
	 * mainly that when not shuffling, the user can back up to tracks that are
	 * not in the history).
	 * 
	 * Example: When playing an album with 10 tracks from the start, and
	 * enabling shuffle while playing track 5, the remaining tracks (6-10) will
	 * be shuffled, e.g. the final play order might be 1-2-3-4-5-8-10-6-9-7.
	 * When hitting 'prev' 8 times while playing track 7 in this example, the
	 * user will go to tracks 9-6-10-8-5-4-3-2. If the user then hits 'next', a
	 * random track will be picked again. If at any time user disables shuffling
	 * the next/previous track will be picked in sequential order again.
	 */

	public void prev() {
		LogUtil.d(LOGTAG, "------prev");
		synchronized (this) {
			if (mSingleSong && !mbOnline) {
				stop(false);
				seek(0);
				play();
				return;
			}

			// Log.i(LOGTAG,
			// "zll --- prev mbOnline 0.1 mPlayPos:"+mPlayPos+",mShuffleMode:"+mShuffleMode);
			int tmpPos = mPlayPos;
			if (mShuffleMode == SHUFFLE_NORMAL) {
				// go to previously-played track and remove it from the history
				int histsize = mHistory.size();
				// Log.i(LOGTAG,
				// "zll --- prev mbOnline 0.2 histsize:"+histsize);
				if (histsize != 0) {
					// prev is a no-op
					// int skip = mRand.nextInt(histsize);//lory add 2014.5.16
					Integer pos = mHistory.remove(histsize - 1);
					// Integer pos = mHistory.remove(skip);
					tmpPos = pos.intValue();
					if (tmpPos < 0) {
						tmpPos = 0;
					} else if (tmpPos >= mPlayListLen) {
						tmpPos = mPlayListLen - 1;
					}
				} else {
					// mPlayPos = 0;
				}

			} else {
				if (tmpPos > 0) {
					tmpPos--;
				} else {
					tmpPos = mPlayListLen - 1;
				}

			}
			final boolean tempIsonline = isNetMusic(mPlayList, tmpPos);

			// Log.i(LOGTAG,
			// "zll --- prev mbOnline 1 mPlayPos:"+mPlayPos+",mbOnline:"+mbOnline);
			if (!tempIsonline) {
				saveBookmarkIfNeeded();
			} else {
				List<AuroraListItem> items = getListInfo();
				if (items != null) {
					while (!items.get(tmpPos).isAvailable()) {
						if (items.size() > tmpPos + 1 && tmpPos > 0) {
							tmpPos--;
						} else {
							tmpPos = mPlayListLen - 1;
						}
					}
				}
			}

			// openCurrentAndNext();
			// add by chenhl for network tips
			final int newPos = tmpPos;
			if (tempIsonline && FlowTips.showPlayFlowTips(MediaPlaybackService.this, new OndialogClickListener() {

				@Override
				public void OndialogClick() {
					mPlayPos = newPos;
					mbOnline = tempIsonline;
					stop(false);
					openCurrentAndNext();
					play();
				}
			})) {
				return;
			}
			// end
			mbOnline = tempIsonline;
			mPlayPos = tmpPos;
			stop(false);
			if (mbOnline) {
				openCurrentAndNext();
			} else {
				openCurrentAndNextForPrev();// lory modify 2014.6.11
			}

			play();
			if (!mbOnline) {
				notifyChange(META_CHANGED);
			}
		}
	}

	/**
	 * Get the next position to play. Note that this may actually modify
	 * mPlayPos if playback is in SHUFFLE_AUTO mode and the shuffle list window
	 * needed to be adjusted. Either way, the return value is the next value
	 * that should be assigned to mPlayPos;
	 */
	private int getNextPosition(boolean force, boolean bPrev) {// lory modify
																// 2014.6.11
		if (mRepeatMode == REPEAT_CURRENT) {
			if (mPlayPos < 0)
				return 0;
			return mPlayPos;
		} else if (mShuffleMode == SHUFFLE_NORMAL) {
			// Pick random next track from the not-yet-played ones
			// TODO: make it work right after adding/removing items in the
			// queue.

			// Store the current file in the history, but keep the history at a
			// reasonable size
			// delete by chenhl start 20140617
			/*
			 * if (mPlayPos >= 0) { mHistory.add(mPlayPos); } if
			 * (mHistory.size() > MAX_HISTORY_SIZE) {
			 * mHistory.removeElementAt(0); }
			 */
			// delete by chenhl end 20140617
			// add by chenhl start 20140620
			if (bPrev && mHistory.size() > 0) {
				int histsize = mHistory.size();
				int index = 0;
				if (histsize != 0) {
					// prev is a no-op
					Integer pos = mHistory.remove(histsize - 1);
					mPlayPos = pos.intValue();
					if (mPlayPos < 0) {
						index = 0;
					} else if (mPlayPos >= mPlayListLen) {
						index = mPlayListLen - 1;
					}
					return index;
				}
				return mPlayPos;
				// return mHistory.remove(mHistory.size()-1).intValue();
			}
			// add by chenhl end 20140620
			int numTracks = mPlayListLen;
			int[] tracks = new int[numTracks];
			for (int i = 0; i < numTracks; i++) {
				tracks[i] = i;
			}

			int numHistory = mHistory.size();
			int numUnplayed = numTracks;
			for (int i = 0; i < numHistory; i++) {
				int idx = mHistory.get(i).intValue();
				if (idx < numTracks && tracks[idx] >= 0) {
					numUnplayed--;
					tracks[idx] = -1;
				}
			}

			// 'numUnplayed' now indicates how many tracks have not yet
			// been played, and 'tracks' contains the indices of those
			// tracks.
			if (numUnplayed <= 0) {
				// everything's already been played
				if (mRepeatMode == REPEAT_ALL || force) {
					// pick from full set
					numUnplayed = numTracks;
					for (int i = 0; i < numTracks; i++) {
						tracks[i] = i;
					}
				} else {
					// all done
					return -1;
				}
			}

			if (numUnplayed <= 0) {
				return -1;
			}

			int skip = mRand.nextInt(numUnplayed);
			int cnt = -1;
			while (true) {
				while (tracks[++cnt] < 0)
					;
				skip--;
				if (skip < 0) {
					break;
				}
			}

			return cnt;
		} else if (mShuffleMode == SHUFFLE_AUTO) {
			doAutoShuffleUpdate();
			return mPlayPos + 1;
		} else {
			if (bPrev) {
				if (mPlayPos > 0) {
					mPlayPos--;
				} else {
					mPlayPos = mPlayListLen - 1;
				}

				return mPlayPos;
			} else {
				if (mPlayPos >= mPlayListLen - 1) {
					// we're at the end of the list
					if (mRepeatMode == REPEAT_NONE && !force) {
						// all done
						return -1;
					} else if (mRepeatMode == REPEAT_ALL || force) {
						return 0;
					}
					return -1;
				} else {
					return mPlayPos + 1;
				}
			}

		}
	}

	public boolean gotoNext(boolean force) {
		LogUtil.d(LOGTAG, "-----gotoNext:"+force);
		synchronized (this) {
			if (mSingleSong) {
				mbOnline = false;
				seek(0);
				play();
				return false;
			}

			if (mPlayListLen <= 0) {
				Log.d(LOGTAG, "zll No play queue");
				return false;
			}
			// add by chenhl start 20140617
			if (mShuffleMode == SHUFFLE_NORMAL) {
				if (mHistory.size() > MAX_HISTORY_SIZE) {
					mHistory.removeElementAt(0);
				}
				if (mPlayPos >= 0) {
					mHistory.add(mPlayPos);
				}
			} else {
				mHistory.clear();
			}
			int pos = mPlayPos;
			if (mRepeatMode == REPEAT_CURRENT) {
				if (mPlayPos >= mPlayListLen - 1) {
					pos = 0;
				} else {
					pos++;
				}
			} else if (Application.isRadioType()) {
				if (mPlayPos >= mPlayListLen - 1) {
					//
					LogUtil.d(LOGTAG, "go next radio list!");
					pause();
					((Application) getApplication()).startPlayRadio(Application.mRadioPosition, mMediaplayerHandler, this);
					return false;
				} else {
					pos++;
				}
			} else {
				pos = getNextPosition(force, false);
			}

			if (pos < 0) {
				gotoIdleState();
				if (mIsSupposedToBePlaying) {
					mIsSupposedToBePlaying = false;
					notifyChange(PLAYSTATE_CHANGED);
				}
				return false;
			}
			// mPlayPos = pos;
			final boolean tmpIsonline = isNetMusic(mPlayList, pos);

			if (!tmpIsonline) {
				saveBookmarkIfNeeded();
			} else {
				List<AuroraListItem> items = getListInfo();
				if (items != null) {
					while (!items.get(pos).isAvailable()) {
						if (items.size() > pos + 1 && pos >= 0) {
							pos++;
						} else {
							pos = 0;
						}
						LogUtil.d(LOGTAG, "-----isAvailable pos:"+pos);
					}
				}
			}

			// add by chenhl for network tips
			final int newPos = pos;
			if (tmpIsonline && FlowTips.showPlayFlowTips(MediaPlaybackService.this, new OndialogClickListener() {

				@Override
				public void OndialogClick() {
					stop(false);
					mbOnline = tmpIsonline;
					mPlayPos = newPos;
					openCurrentAndNext();
					play();
				}
			})) {
				if (!force) {
					mIsSupposedToBePlaying = false;
					notifyChange(PLAYSTATE_CHANGED);
				}
				return true;
			}
			mbOnline = tmpIsonline;
			// end
			stop(false);
			mPlayPos = pos;
			openCurrentAndNext();
			play();
			if (!mbOnline) {
				notifyChange(META_CHANGED);
			}
			return false;
		}
	}

	// Iuni <lory><2014-05-05> add begin
	public void gotoSomeSong(int position) {
		LogUtil.d(LOGTAG, "gotoSomeSong position:" + position);
		mSingleSong = false;
		synchronized (this) {
			if (mPlayListLen <= 0) {
				Log.d(LOGTAG, "zll No play queue 2");
				return;
			}
			LogUtil.d(LOGTAG, "gotoSomeSong.....");
			if (position >= mPlayListLen) {
				position = mPlayListLen - 1;
			} else if (position < 0) {
				position = 0;
			}

			int pos = position;
			mPlayPos = pos;

			mbOnline = isNetMusic(mPlayList, mPlayPos);
			if (!mbOnline) {
				saveBookmarkIfNeeded();
				stop(false);
			}

			mPlayPos = pos;
			openCurrent();
			play();
			if (!mbOnline) {
				notifyChange(META_CHANGED);
			}
		}
	}

	// Iuni <lory><2014-05-05> add end

	private void gotoIdleState() {
		mDelayedStopHandler.removeCallbacksAndMessages(null);
		Message msg = mDelayedStopHandler.obtainMessage();
		mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
		// aurora ukiliu 2014-06-03 modify begin

		// stopForeground(true);
		// stopForeground(false);// 5.1 bug fix 通知栏两次暂停通知栏消失
		// aurora ukiliu 2014-06-03 modify end
	}

	private void saveBookmarkIfNeeded() {
		try {
			// Log.d("MusicUtils", "zll saveBookmarkIfNeeded 1");
			if (isPodcast()) {
				long pos = position();
				long bookmark = getBookmark();
				long duration = duration();
				if ((pos < bookmark && (pos + 10000) > bookmark) || (pos > bookmark && (pos - 10000) < bookmark)) {
					// The existing bookmark is close to the current
					// position, so don't update it.
					return;
				}
				if (pos < 15000 || (pos + 10000) > duration) {
					// if we're near the start or end, clear the bookmark
					pos = 0;
				}

				// write 'pos' to the bookmark field
				ContentValues values = new ContentValues();
				values.put(MediaStore.Audio.Media.BOOKMARK, pos);
				Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mCursor.getLong(IDCOLIDX));
				getContentResolver().update(uri, values, null, null);
			}
		} catch (SQLiteException ex) {
			ex.printStackTrace();
		}
	}

	// Make sure there are at least 5 items after the currently playing item
	// and no more than 10 items before.
	private void doAutoShuffleUpdate() {
		boolean notify = false;

		// remove old entries
		if (mPlayPos > 10) {
			removeTracks(0, mPlayPos - 9);
			notify = true;
		}
		// add new entries if needed
		int to_add = 7 - (mPlayListLen - (mPlayPos < 0 ? -1 : mPlayPos));
		for (int i = 0; i < to_add; i++) {
			// pick something at random from the list

			int lookback = mHistory.size();
			int idx = -1;
			while (true) {
				idx = mRand.nextInt(mAutoShuffleList.length);
				if (!wasRecentlyUsed(idx, lookback)) {
					break;
				}
				lookback /= 2;
			}
			mHistory.add(idx);
			if (mHistory.size() > MAX_HISTORY_SIZE) {
				mHistory.remove(0);
			}
			ensurePlayListCapacity(mPlayListLen + 1);
			mPlayList[mPlayListLen++] = mAutoShuffleList[idx];
			notify = true;
		}
		if (notify) {
			notifyChange(QUEUE_CHANGED);
		}
	}

	// check that the specified idx is not in the history (but only look at at
	// most lookbacksize entries in the history)
	private boolean wasRecentlyUsed(int idx, int lookbacksize) {

		// early exit to prevent infinite loops in case idx == mPlayPos
		if (lookbacksize == 0) {
			return false;
		}

		int histsize = mHistory.size();
		if (histsize < lookbacksize) {
			Log.d(LOGTAG, "lookback too big");
			lookbacksize = histsize;
		}
		int maxidx = histsize - 1;
		for (int i = 0; i < lookbacksize; i++) {
			long entry = mHistory.get(maxidx - i);
			if (entry == idx) {
				return true;
			}
		}
		return false;
	}

	// A simple variation of Random that makes sure that the
	// value it returns is not equal to the value it returned
	// previously, unless the interval is 1.
	private static class Shuffler {
		private int mPrevious;
		private Random mRandom = new Random();

		public int nextInt(int interval) {
			int ret;
			do {
				ret = mRandom.nextInt(interval);
			} while (ret == mPrevious && interval > 1);
			mPrevious = ret;
			return ret;
		}
	};

	private boolean makeAutoShuffleList() {
		ContentResolver res = getContentResolver();
		Cursor c = null;
		try {
			StringBuilder where = new StringBuilder();// lory add 2014.6.11
			where.append(MediaStore.Audio.Media.IS_MUSIC + "=1 AND " + Globals.QUERY_SONG_FILTER);
			c = res.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Audio.Media._ID }, where.toString(),// MediaStore.Audio.Media.IS_MUSIC
																																		// +
																																		// "=1",
					null, null);
			if (c == null || c.getCount() == 0) {
				return false;
			}
			int len = c.getCount();
			long[] list = new long[len];
			for (int i = 0; i < len; i++) {
				c.moveToNext();
				list[i] = c.getLong(0);
			}
			mAutoShuffleList = list;
			return true;
		} catch (RuntimeException ex) {
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return false;
	}

	/**
	 * Removes the range of tracks specified from the play list. If a file
	 * within the range is the file currently being played, playback will move
	 * to the next file after the range.
	 * @param first
	 *            The first file to be removed
	 * @param last
	 *            The last file to be removed
	 * @return the number of tracks deleted
	 */
	public int removeTracks(int first, int last) {
		int numremoved = removeTracksInternal(first, last);
		if (numremoved > 0) {
			notifyChange(QUEUE_CHANGED);
		}
		return numremoved;
	}

	private int removeTracksInternal(int first, int last) {
		synchronized (this) {
			if (last < first)
				return 0;
			if (first < 0)
				first = 0;
			if (last >= mPlayListLen)
				last = mPlayListLen - 1;

			boolean gotonext = false;
			if (first <= mPlayPos && mPlayPos <= last) {
				mPlayPos = first;
				gotonext = true;
			} else if (mPlayPos > last) {
				mPlayPos -= (last - first + 1);
			}

			int num = mPlayListLen - last - 1;
			for (int i = 0; i < num; i++) {
				mPlayList[first + i] = mPlayList[last + 1 + i];
			}

			mPlayListLen -= last - first + 1;
			if (gotonext) {
				if (mPlayListLen == 0) {
					LogUtil.d(LOGTAG, "removeTracksInternal.... end!!!");
					stop(true);
					mPlayPos = -1;
					if (mCursor != null) {
						mCursor.close();
						mCursor = null;
					}

					stopForeground(true);
				} else {
					LogUtil.d(LOGTAG, "removeTracksInternal.... 222222");
					if (mPlayPos >= mPlayListLen) {
						mPlayPos = 0;
					}
					boolean wasPlaying = isPlaying();
					stop(false);
					mbOnline = isNetMusic(mPlayList, mPlayPos);// add by chenhl
																// for BUG
																// #10494
					openCurrentAndNext();
					if (wasPlaying) {
						play();
					}
					notifyChange(META_CHANGED);
				}

			}
			return last - first + 1;
		}
	}

	private void removeTrackInListInfo(long id) {
		if (id < 0 || mMusicList == null) {
			return;
		}

		int num = 0;
		int len = mMusicList.size();
		for (int i = 0; i < len; i++) {
			AuroraListItem item = mMusicList.get(i);
			if (item.getSongId() == id) {
				num = i;
				break;
			}
		}

		synchronized (this) {
			mMusicList.remove(num);
		}
		return;
	}

	/**
	 * Removes all instances of the track with the given id from the playlist.
	 * @param id
	 *            The id to be removed
	 * @return how many instances of the track were removed
	 */
	public int removeTrack(long id) {
		int numremoved = 0;
		synchronized (this) {
			for (int i = 0; i < mPlayListLen; i++) {
				if (mPlayList[i] == id) {
					numremoved += removeTracksInternal(i, i);
					removeTrackInListInfo(id);
					i--;

					// add by chenhl start 20140623
					if (id == nextAudioId && numremoved > 0) {
						setNextTrack();
					}
					// add by chenhl end
				}
			}
		}

		if (numremoved > 0) {
			notifyChange(QUEUE_CHANGED);
		}
		return numremoved;
	}

	public void setShuffleMode(int shufflemode) {
		synchronized (this) {
			if (mShuffleMode == shufflemode && mPlayListLen > 0) {
				return;
			}
			mShuffleMode = shufflemode;
			if (mShuffleMode == SHUFFLE_AUTO) {
				mShuffleMode = SHUFFLE_NORMAL;
			}
			saveQueue(false);
		}
	}

	public int getShuffleMode() {
		return mShuffleMode;
	}

	public void setRepeatMode(int repeatmode) {
		synchronized (this) {
			mRepeatMode = repeatmode;
			setNextTrack();
			saveQueue(false);
		}
	}

	public int getRepeatMode() {
		return mRepeatMode;
	}

	public int getMediaMountedCount() {
		return mMediaMountedCount;
	}

	/**
	 * Returns the path of the currently playing file, or null if no file is
	 * currently playing.
	 */
	public String getPath() {
		return mFileToPlay;
	}

	/**
	 * Returns the rowid of the currently playing file, or -1 if no file is
	 * currently playing.
	 */
	public long getAudioId() {
		if (mbOnline) {
			if (mPlayPos >= 0 && mAuroraXiamiPlayer != null && mAuroraXiamiPlayer.isOnlineInitialized()) {
				return mPlayList[mPlayPos];
			}
		} else {
			synchronized (this) {
				if (mPlayPos >= 0 && mPlayer != null && mPlayer.isInitialized()) {
					return mPlayList[mPlayPos];
				}

				if (mPlayPos >= 0 && mLocalPlayer != null && mLocalPlayer.isInitialized()) {
					return mPlayList[mPlayPos];
				}
			}
		}

		return -1;
	}

	/**
	 * Returns the position in the queue
	 * @return the position in the queue
	 */
	public int getQueuePosition() {
		synchronized (this) {
			return mPlayPos;
		}
	}

	/**
	 * Starts playing the track at the given position in the queue.
	 * @param pos
	 *            The position in the queue of the track that will be played.
	 */
	public void setQueuePosition(int pos) {
		synchronized (this) {
			stop(false);
			mPlayPos = pos;
			openCurrentAndNext();
			play();
			notifyChange(META_CHANGED);
			if (mShuffleMode == SHUFFLE_AUTO) {
				doAutoShuffleUpdate();
			}
		}
	}

	public String getMusicbitrate() {
		/*
		 * if (mbOnline && mAuroraStreamPlayer != null) { return
		 * mAuroraStreamPlayer.getMusicbitrate(); }
		 */
		return null;
	}

	private long secondaryPosition() {
		if (mbOnline && mAuroraXiamiPlayer != null) {
			return mAuroraXiamiPlayer.secondaryPosition();
		}
		return -1;
	}

	private void notifyLrcPath(String path) {
		if (path != null && !path.isEmpty()) {
			mLrcPath = path;
			notifyChange(AURORALRC_CHANGED);
		}
		return;
	}

	private boolean isonlineSong() {
		return isNetMusic(mPlayList, mPlayPos);
	}

	private String getAuroraSongLrc() {
		String CurPath = getFilePath();
		LogUtil.d(LOGTAG, "--getAuroraSongLrc---CurPath:" + CurPath);
		if (CurPath != null && !(CurPath.equalsIgnoreCase(mHttpUri))) {
			String name = CurPath.substring(CurPath.lastIndexOf("/") + 1);
			LogUtil.d(LOGTAG, "--getAuroraSongLrc---name:" + name);
			if (name != null && !name.isEmpty() || (name.equalsIgnoreCase("IUNI-song.mp3") || (name.equalsIgnoreCase("IUNI-song2.mp3")))) {
				String fileName = name.substring(0, name.lastIndexOf("."));
				if (fileName == null) {
					return null;
				}
				return AuroraMusicUtil.getLrcPath(fileName, getApplication());
			}

		}

		return null;
	}

	/**
	 * 先取DB歌词，取不到取SD卡，然后在把SD卡歌词添加到DB
	 * @return
	 */
	private String getLrcUri() {
		String pathString = MusicUtils.getLocalLrc(this, getAudioId(), getTrackName(), getArtistName());
		LogUtil.d(LOGTAG, "--db--pathString:" + pathString);
		if (!TextUtils.isEmpty(pathString)) {
			File file = new File(pathString);
			if (file.exists()) {
				mLrcPath = pathString;
				return mLrcPath;
			}
		}
		pathString = getAuroraSongLrc();
		LogUtil.d(LOGTAG, "--sd--pathString:" + pathString);
		if (!TextUtils.isEmpty(pathString)) {
			mLrcPath = pathString;
			MusicUtils.addToAudioInfoLrc(this, getAudioId(), getTrackName(), getArtistName(), pathString);
			return pathString;
		}

		return null;
	}

	public String getArtistName() {
		if (mbOnline) {
			if (mAuroraXiamiPlayer == null) {
				return null;
			}
			return mAuroraXiamiPlayer.getArtistName();
		} else {
			synchronized (this) {
				if (mCursor == null) {
					return null;
				}
				// Iuni <lory><2014-06-09> add begin
				try {
					return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
				// Iuni <lory><2014-06-09> add end
			}
		}

	}

	public boolean isFavorite() {
		if (getAudioId() >= 0) {
			return isFavorite(getAudioId(), (mbOnline == true) ? MUSIC_NET : NOT_MUSIC_NET);
		}

		return false;
	}

	public boolean isFavorite(long id) {
		return MusicUtils.isFavorite(this, id, (mbOnline == true) ? MUSIC_NET : NOT_MUSIC_NET);
	}

	private boolean isFavorite(long id, int isnet) {
		return MusicUtils.isFavorite(this, id, isnet);
	}

	public void addToFavorite() {
		if (getAudioId() >= 0) {
			addToFavorite(getAudioId());
		}
	}

	private AuroraListItem getItemByMusicId(long id) {
		if (mMusicList == null) {
			return null;
		}

		int len = mMusicList.size();
		for (int i = 0; i < len; i++) {
			AuroraListItem item = mMusicList.get(i);
			if (item.getSongId() == id) {
				// Log.i(LOGTAG,
				// "zll ---- i:"+i+",mMusicList:"+mMusicList.toString());
				return item;
			}
		}

		return null;
	}

	public void addToFavorite(long id) {
		AuroraListItem item = getItemByMusicId(id);
		if (item == null) {
			Log.i(LOGTAG, "zll ---- addToFavorite fail by no data---");
			return;
		}
		MusicUtils.addToFavorite(this, item);
		notifyChange(FAVORITE_CHANGED);
	}

	public void removeFromFavorites() {
		if (getAudioId() >= 0) {
			removeFromFavorites(getAudioId());
		}
	}

	public void removeFromFavorites(long id) {
		AuroraListItem item = getItemByMusicId(id);
		if (item == null) {
			// Log.i(LOGTAG, "zll ---- removeFromFavorites fail by no data---");
			return;
		}

		MusicUtils.removeFromFavorites(this, id, item.getIsDownLoadType());
		notifyChange(FAVORITE_CHANGED);
	}

	public void toggleMyFavorite() {

		if (!isFavorite()) {
			addToFavorite();
		} else {
			removeFromFavorites();
		}
	}

	public String getFilePath() {
		synchronized (this) {
			if (mbOnline) {
				return mHttpUri;
			}

			if (mCursor == null) {
				return null;
			}
			try {
				return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			// Iuni <lory><2014-06-09> add end
		}
	}

	public long getArtistId() {
		if (mbOnline) {
			if (mAuroraXiamiPlayer == null) {
				return -1;
			}
			return mAuroraXiamiPlayer.getArtistId();
		} else {
			synchronized (this) {
				if (mCursor == null) {
					return -1;
				}
				try {
					return mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID));
				} catch (Exception e) {
					e.printStackTrace();
					return -1;
				}
			}
		}

	}

	public String getAlbumName() {
		if (mbOnline) {
			if (mAuroraXiamiPlayer == null) {
				return null;
			}
			return mAuroraXiamiPlayer.getAlbumName();
		} else {
			synchronized (this) {
				if (mCursor == null) {
					return null;
				}
				try {
					return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		}

	}

	public long getAlbumId() {
		if (mbOnline) {
			if (mAuroraXiamiPlayer == null) {
				return -1;
			}
			return mAuroraXiamiPlayer.getAlbumId();
		} else {
			synchronized (this) {
				if (mCursor == null) {
					return -1;
				}
				try {
					return mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
				} catch (Exception e) {
					e.printStackTrace();
					return -1;
				}
			}
		}

	}

	public String getTrackName() {
		if (mbOnline) {
			if (mAuroraXiamiPlayer == null) {
				return null;
			}
			return mAuroraXiamiPlayer.getTrackName();

		} else {
			synchronized (this) {
				if (mCursor == null) {
					return null;
				}
				try {
					return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		}
	}

	private boolean isPodcast() {
		synchronized (this) {
			if (mCursor == null) {
				return false;
			}

			if (mCursor.moveToFirst()) {
				return (mCursor.getInt(PODCASTCOLIDX) > 0);
			} else {
				return false;
			}
		}
	}

	private long getBookmark() {
		synchronized (this) {
			if (mCursor == null) {
				return 0;
			}
			if (mCursor.moveToFirst()) {
				return mCursor.getLong(BOOKMARKCOLIDX);
			} else {
				return 0;
			}
		}
	}

	/**
	 * Returns the duration of the file in milliseconds. Currently this method
	 * returns -1 for the duration of MIDI files.
	 */
	public long duration() {
		if (mbOnline) {
			if (mAuroraXiamiPlayer != null && mAuroraXiamiPlayer.isOnlineInitialized()) {
				return mAuroraXiamiPlayer.duration();
			}
		} else {
			if (mPlayer != null && mPlayer.isInitialized()) {
				return mPlayer.duration();
			} else if (mLocalPlayer != null && mLocalPlayer.isInitialized()) {
				return mLocalPlayer.getDuration();
			}
		}

		return -1;
	}

	/**
	 * Returns the current playback position in milliseconds
	 */
	public long position() {
		if (mbOnline) {
			if (mAuroraXiamiPlayer != null && mAuroraXiamiPlayer.isOnlineInitialized()) {
				return mAuroraXiamiPlayer.position();
			}
		} else {
			if (mPlayer.isInitialized()) {
				return mPlayer.position();
			} else if (mLocalPlayer != null && mLocalPlayer.isInitialized()) {
				return mLocalPlayer.getCurrentPosition();
			}
		}

		return -1;
	}

	/**
	 * Seeks to the position specified.
	 * @param pos
	 *            The position to seek to, in milliseconds
	 */
	public long seek(long pos) {
		if (mbOnline) {
			if (mAuroraXiamiPlayer != null && mAuroraXiamiPlayer.isOnlineInitialized()) {
				if (pos < 0)
					pos = 0;
				if (pos > mAuroraXiamiPlayer.duration())
					pos = mAuroraXiamiPlayer.duration();
				return mAuroraXiamiPlayer.seek(pos);
			}
		} else {
			if (mPlayer.isInitialized()) {
				if (pos < 0)
					pos = 0;
				if (pos > mPlayer.duration())
					pos = mPlayer.duration();
				return mPlayer.seek(pos);
			} else if (mLocalPlayer != null && mLocalPlayer.isInitialized()) {
				if (pos < 0)
					pos = 0;
				if (pos > mLocalPlayer.getDuration())
					pos = mLocalPlayer.getDuration();
				mLocalPlayer.seekTo((int) pos);
			}
		}

		return -1;
	}

	/**
	 * Sets the audio session ID.
	 * @param sessionId
	 *            : the audio session ID.
	 */
	public void setAudioSessionId(int sessionId) {
		synchronized (this) {
			if (mbOnline) {

			} else {
				mPlayer.setAudioSessionId(sessionId);
			}

		}
	}

	/**
	 * Returns the audio session ID.
	 */
	public int getAudioSessionId() {
		synchronized (this) {
			if (mbOnline) {
				int id = 0;
				if (mAuroraXiamiPlayer != null) {
					id = mAuroraXiamiPlayer.getAudioSessionId();
				}
				LogUtil.d(LOGTAG, " ----- xxx 1 getAudioSessionId id:" + id);
				return id;
			} else {
				LogUtil.d(LOGTAG, "----- xxx 2 getAudioSessionId id:" + mPlayer.getAudioSessionId());
				return mPlayer.getAudioSessionId();
			}
		}
	}

	/**
	 * Provides a unified interface for dealing with midi files and other media
	 * files.
	 */
	private class MultiPlayer {
		private CompatMediaPlayer mCurrentMediaPlayer = new CompatMediaPlayer();
		private CompatMediaPlayer mNextMediaPlayer;
		private Handler mHandler;
		private boolean mIsInitialized = false;
		private boolean mIsInListener = false;

		public MultiPlayer() {
			mCurrentMediaPlayer.setWakeMode(MediaPlaybackService.this, PowerManager.PARTIAL_WAKE_LOCK);
		}

		public void setDataSource(String path) {
			mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, path, false);
			if (mIsInitialized) {
				setNextDataSource(null);
			}
		}

		/**
		 * M: set date source and use async prepare to aviod ANR
		 * @param path
		 *            Data source path
		 */
		public void setDataSourceAsync(String path) {
			mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, path, true);
			if (mIsInitialized) {
				setNextDataSource(null);
			}
		}

		private boolean setDataSourceImpl(MediaPlayer player, String path, boolean async) {
			try {
				LogUtil.d(LOGTAG, "------- setDataSourceImpl path:" + path + " async:" + async);
				player.reset();
				if (async) {
					player.setOnPreparedListener(preparedlistener);
				} else {
					player.setOnPreparedListener(null);
				}
				if (path.startsWith("content://")) {
					player.setDataSource(MediaPlaybackService.this, Uri.parse(path));
				} else {
					player.setDataSource(path);
				}
				player.setAudioStreamType(AudioManager.STREAM_MUSIC);
				if (async) {
					player.prepareAsync();
				} else {
					player.prepare();
				}
			}
			// Iuni <lory><2014-06-09> add begin
			catch (Exception e) {
				LogUtil.e(LOGTAG, "---- setDataSourceImpl fail -------", e);
				e.printStackTrace();
				player.setOnCompletionListener(null);
				player.setOnErrorListener(null);
				return false;
			}
			// Iuni <lory><2014-06-09> add end

			// Log.i(LOGTAG, "zll ----- setDataSourceImpl ok");
			player.setOnCompletionListener(listener);
			player.setOnErrorListener(errorListener);
			mIsInListener = true;
			Intent i = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
			i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
			i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
			sendBroadcast(i);
			return true;
		}

		public boolean setNextDataSource(String path) {
			mCurrentMediaPlayer.setNextMediaPlayer(null);
			if (mNextMediaPlayer != null) {
				mNextMediaPlayer.release();
				mNextMediaPlayer = null;
			}
			if (path == null) {
				return false;
			}

			mNextMediaPlayer = new CompatMediaPlayer();
			mNextMediaPlayer.setWakeMode(MediaPlaybackService.this, PowerManager.PARTIAL_WAKE_LOCK);
			mNextMediaPlayer.setAudioSessionId(getAudioSessionId());
			if (setDataSourceImpl(mNextMediaPlayer, path, true)) {
				// mCurrentMediaPlayer.setNextMediaPlayer(mNextMediaPlayer);
				return true;
			} else {
				// failed to open next, we'll transition the old fashioned way,
				// which will skip over the faulty file
				LogUtil.d(LOGTAG, "----setNextDataSource error");
				mNextMediaPlayer.release();
				mNextMediaPlayer = null;
				return false;
			}
		}

		public void resetOfOnlinePlay() {
			if (mCurrentMediaPlayer != null && mIsInListener) {
				mCurrentMediaPlayer.reset();
				mCurrentMediaPlayer.setOnPreparedListener(null);
				mCurrentMediaPlayer.setOnCompletionListener(null);
				mCurrentMediaPlayer.setOnErrorListener(null);

				setNextDataSource(null);
				mIsInListener = false;
			}

			return;
		}

		public boolean isInitialized() {
			return mIsInitialized;
		}

		public boolean isPlaying() {
			if (mCurrentMediaPlayer != null) {
				return mCurrentMediaPlayer.isPlaying();
			}
			return false;
		}

		public void start() {
			LogUtil.d(LOGTAG, "------------mCurrentMediaPlayer.start()");
			mCurrentMediaPlayer.start();
		}

		public void stop() {
			mCurrentMediaPlayer.reset();
			mIsInitialized = false;
			LogUtil.d(LOGTAG, " stop() mIsInitialized:" + mIsInitialized);
		}

		/**
		 * You CANNOT use this player anymore after calling release()
		 */
		public void release() {
			stop();
			mCurrentMediaPlayer.release();
			// add by JXH begin
			if (mNextMediaPlayer != null) {
				mNextMediaPlayer.release();
				mNextMediaPlayer = null;
			}
			// add by JXH end
		}

		public void pause() {
			mCurrentMediaPlayer.pause();
		}

		public void setHandler(Handler handler) {
			mHandler = handler;
		}

		MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				if (mp == mCurrentMediaPlayer && mNextMediaPlayer != null) {
					/*
					 * mCurrentMediaPlayer.release(); mCurrentMediaPlayer =
					 * mNextMediaPlayer; mNextMediaPlayer = null;
					 * mHandler.sendEmptyMessage(TRACK_WENT_TO_NEXT);
					 */
					// lory add 2014.6.13 start
					if (mPlayListLen == 1 && mRepeatMode != REPEAT_CURRENT) {
						mCurrentMediaPlayer.setNextMediaPlayer(null);
						if (mNextMediaPlayer != null) {
							mNextMediaPlayer.release();
							mNextMediaPlayer = null;
						}

						mWakeLock.acquire(30000);
						mHandler.sendEmptyMessage(TRACK_ENDED);
						mHandler.sendEmptyMessage(RELEASE_WAKELOCK);
					} else {
						mCurrentMediaPlayer.release();
						mCurrentMediaPlayer = mNextMediaPlayer;
						mNextMediaPlayer = null;
						// add by JXh begin
						// / M: fix when NextPlayer is not prepared,case we
						// don't setNextPlayer,
						// / so when the currentPlayer completed,native will
						// auto check
						// / whether the nextPlayer is not null,if not,will not
						// start,ALPS01298224 @{
						if (!isPlaying()) {
							mCurrentMediaPlayer.start();
							LogUtil.d(LOGTAG, "------------mNextMediaPlayer.start()");
						}
						// add by JXh end
						mHandler.sendEmptyMessage(TRACK_WENT_TO_NEXT);
					}
					// lory add 2014.6.13 end
				} else {
					// Log.i(LOGTAG,
					// "zll ---- onCompletion 2 mPlayListLen:"+mPlayListLen+",mRepeatMode:"+mRepeatMode);
					// Acquire a temporary wakelock, since when we return from
					// this callback the MediaPlayer will release its wakelock
					// and allow the device to go to sleep.
					// This temporary wakelock is released when the
					// RELEASE_WAKELOCK
					// message is processed, but just in case, put a timeout on
					// it.
					// Log.i(LOGTAG, "zll ---- onCompletion 2 ");
					mWakeLock.acquire(30000);
					mHandler.sendEmptyMessage(TRACK_ENDED);
					mHandler.sendEmptyMessage(RELEASE_WAKELOCK);
				}
			}
		};

		MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {
			public boolean onError(MediaPlayer mp, int what, int extra) {
				// add by JXH BUG#13209 给出提示 begin
				Toast.makeText(MediaPlaybackService.this, R.string.playback_failed, Toast.LENGTH_SHORT).show();
				// add by JXH BUG#13209 给出提示 end
				LogUtil.d(LOGTAG, "----MultiPlayer Error:" + what + "," + extra);
				mIsInitialized = false;
				mCurrentMediaPlayer.release();
				// Creating a new MediaPlayer and settings its wakemode does
				// not
				// require the media service, so it's OK to do this now,
				// while the
				// service is still being restarted
				mCurrentMediaPlayer = new CompatMediaPlayer();
				mCurrentMediaPlayer.setWakeMode(MediaPlaybackService.this, PowerManager.PARTIAL_WAKE_LOCK);
				mHandler.sendMessageDelayed(mHandler.obtainMessage(SERVER_DIED), 2000);
				return true;
			}
		};
		MediaPlayer.OnPreparedListener preparedlistener = new MediaPlayer.OnPreparedListener() {
			public void onPrepared(MediaPlayer mp) {
				synchronized (MediaPlaybackService.this) {
					// / M: Only when current player prepared need to set next
					// track.
					if (!mp.equals(mCurrentMediaPlayer) && mIsInitialized == true) {
						LogUtil.d(LOGTAG, "preparedlistener finish for next player!");
						mCurrentMediaPlayer.setNextMediaPlayer(mNextMediaPlayer);

						return;
					}
					if (duration() == 0) {
						final boolean old = mQuietMode;
						mQuietMode = true;
						LogUtil.d(LOGTAG, "12222222222222222222!");
						errorListener.onError(mp, 0, 0);
						mQuietMode = old;
						return;
					}
				}
			}
		};

		public long duration() {
			return mCurrentMediaPlayer.getDuration();
		}

		public long position() {
			return mCurrentMediaPlayer.getCurrentPosition();
		}

		public long seek(long whereto) {
			mCurrentMediaPlayer.seekTo((int) whereto);
			return whereto;
		}

		public void setVolume(float vol) {
			mCurrentMediaPlayer.setVolume(vol, vol);
		}

		public void setAudioSessionId(int sessionId) {
			mCurrentMediaPlayer.setAudioSessionId(sessionId);
		}

		public int getAudioSessionId() {
			return mCurrentMediaPlayer.getAudioSessionId();
		}
	}

	static class CompatMediaPlayer extends MediaPlayer implements OnCompletionListener {
		private static final String TAG = "MediaPlaybackService.CompatMediaPlayer";
		private boolean mCompatMode = true;
		private MediaPlayer mNextPlayer;
		private OnCompletionListener mCompletion;

		public CompatMediaPlayer() {
			try {
				// Log.i(LOGTAG, "zll ---- CompatMediaPlayer 1");
				MediaPlayer.class.getMethod("setNextMediaPlayer", MediaPlayer.class);
				mCompatMode = false;
			} catch (NoSuchMethodException e) {
				// Log.i(LOGTAG, "zll ---- CompatMediaPlayer 2");
				LogUtil.d(TAG, "CompatMediaPlayer:e " + e.getLocalizedMessage());
				mCompatMode = true;
				super.setOnCompletionListener(this);
			}
		}

		public void setNextMediaPlayer(MediaPlayer next) {

			try {
				LogUtil.d(LOGTAG, "----------------mCompatMode:" + mCompatMode);
				if (mCompatMode) {
					mNextPlayer = next;
				} else {
					super.setNextMediaPlayer(next);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public void setOnCompletionListener(OnCompletionListener listener) {
			if (mCompatMode) {
				mCompletion = listener;
			} else {
				super.setOnCompletionListener(listener);
			}
		}

		@Override
		public void onCompletion(MediaPlayer mp) {
			// Log.i(LOGTAG, "zll ---- onCompletion 3 ");
			if (mNextPlayer != null) {
				// Log.i(LOGTAG, "zll ---- onCompletion 4 ");
				// as it turns out, starting a new MediaPlayer on the completion
				// of a previous player ends up slightly overlapping the two
				// playbacks, so slightly delaying the start of the next player
				// gives a better user experience
				SystemClock.sleep(50);
				mNextPlayer.start();
			}
			mCompletion.onCompletion(this);
		}
	}

	// Iuni <lory><2014-07-10> add begin
	private boolean mbOnline = false;
	private boolean mbOnPause = false;

	private void initLocalPlayer(Handler handler) {
		mLocalPlayer = null;
		mLocalPlayer = new LocalPlayer(getApplicationContext(), handler);
		return;
	}

	private void releaseLocalPlayer() {
		if (mLocalPlayer != null) {
			mLocalPlayer.release();
			mLocalPlayer = null;
		}

		return;
	}

	public void setListInfo(List<AuroraListItem> list) {
		if (mMusicList == null) {
			mMusicList = new ArrayList<AuroraListItem>();
		}

		if (list != null && list.size() > 0) {
			// modify by chenhl start 20140905
			// mMusicList = (ArrayList)list;
			mMusicList.clear();
			mMusicList.addAll(list);
			if (mAuroraXiamiPlayer != null) {
				mAuroraXiamiPlayer.setListInfo(mMusicList);
			}
			// modify by chenhl end 20140905
		}

		return;
	}

	public List<AuroraListItem> getListInfo() {
		if (mMusicList != null) {
			return mMusicList;
		}

		return null;
	}

	private boolean isNetMusic(long[] list, int pos) {
		if (pos < 0) {
			return false;
		}

		synchronized (this) {
			if (list != null && list.length > 0 && pos < list.length) {
				long tmpId = list[pos];
				return isNet(tmpId);
			}
		}

		return false;
	}

	private boolean isNet(long id) {
		boolean bNet = false;

		if (id < 0) {
			return false;
		}

		if (mMusicList != null) {
			int len = mMusicList.size();
			for (int i = 0; i < len; i++) {
				AuroraListItem item = mMusicList.get(i);
				if (item.getSongId() == id) {
					if (item.getIsDownLoadType() == 1) {
						bNet = true;
					}
					// Log.i(LOGTAG,
					// "zll ---- i:"+i+",mPlayPos:"+mPlayPos+",bNet:"+bNet);
					break;
				}
			}
		}

		return bNet;
	}

	public void online_start(long[] list, int position) {
		return;
	}

	private Dao dao;

	public DownloadInfo queryDownloadSong(String title, String artist) {
		if (dao == null) {
			dao = Dao.getInstance(getApplicationContext());
		}
		return dao.queryDownloadSong(title, artist);
	}

	// Iuni <lory><2014-07-10> add end

	/*
	 * By making this a static class with a WeakReference to the Service, we
	 * ensure that the Service can be GCd even when the system process still has
	 * a remote reference to the stub.
	 */
	static class ServiceStub extends IMediaPlaybackService.Stub {
		WeakReference<MediaPlaybackService> mService;

		ServiceStub(MediaPlaybackService service) {
			mService = new WeakReference<MediaPlaybackService>(service);
		}

		public DownloadInfo queryDownloadSong(String title, String artist) {
			return mService.get().queryDownloadSong(title, artist);
		}

		public void openFile(String path) {
			// mService.get().open(path);//lory modify 2014.6.10
			mService.get().auroraOpen(path);
		}

		public void open(long[] list, int position) {
			mService.get().open(list, position);
		}

		public int getQueuePosition() {
			return mService.get().getQueuePosition();
		}

		public void setQueuePosition(int index) {
			mService.get().setQueuePosition(index);
		}

		public boolean isPlaying() {
			return mService.get().isPlaying();
		}

		public void stop() {
			mService.get().stop();
		}

		public void pause() {
			mService.get().pause();
		}

		public void play() {
			mService.get().play();
		}

		public void prev() {
			mService.get().prev();
		}

		public void next() {
			mService.get().gotoNext(true);
		}

		public String getTrackName() {
			return mService.get().getTrackName();
		}

		public String getAlbumName() {
			return mService.get().getAlbumName();
		}

		public long getAlbumId() {
			return mService.get().getAlbumId();
		}

		public String getArtistName() {
			return mService.get().getArtistName();
		}

		public long getArtistId() {
			return mService.get().getArtistId();
		}

		public void enqueue(long[] list, int action) {
			mService.get().enqueue(list, action);
		}

		public long[] getQueue() {
			return mService.get().getQueue();
		}

		public void moveQueueItem(int from, int to) {
			mService.get().moveQueueItem(from, to);
		}

		public String getPath() {
			return mService.get().getPath();
		}

		public long getAudioId() {
			return mService.get().getAudioId();
		}

		public long position() {
			return mService.get().position();
		}

		public long duration() {
			return mService.get().duration();
		}

		public long seek(long pos) {
			return mService.get().seek(pos);
		}

		public void setShuffleMode(int shufflemode) {
			mService.get().setShuffleMode(shufflemode);
		}

		public int getShuffleMode() {
			return mService.get().getShuffleMode();
		}

		public int removeTracks(int first, int last) {
			return mService.get().removeTracks(first, last);
		}

		public int removeTrack(long id) {
			return mService.get().removeTrack(id);
		}

		public void setRepeatMode(int repeatmode) {
			mService.get().setRepeatMode(repeatmode);
		}

		public int getRepeatMode() {
			return mService.get().getRepeatMode();
		}

		public int getMediaMountedCount() {
			return mService.get().getMediaMountedCount();
		}

		public int getAudioSessionId() {
			return mService.get().getAudioSessionId();
		}

		// lory add 2014.5.5 start
		@Override
		public void playListView(int position) throws RemoteException {
			mService.get().gotoSomeSong(position);
		}

		@Override
		public String getFilePath() throws RemoteException {
			return mService.get().getFilePath();
		}

		@Override
		public boolean isFavorite(long id) throws RemoteException {
			return mService.get().isFavorite(id);
		}

		@Override
		public void addToFavorite(long id) throws RemoteException {
			mService.get().addToFavorite(id);
		}

		@Override
		public void removeFromFavorite(long id) throws RemoteException {
			mService.get().removeFromFavorites(id);
		}

		@Override
		public void toggleMyFavorite() throws RemoteException {
			mService.get().toggleMyFavorite();
		}

		// lory add 2014.5.5 end

		@Override
		public void shuffleOpen(long[] list, int position) throws RemoteException {
			// TODO Auto-generated method stub
			mService.get().shuffleOpen(list, position);
		}

		// Iuni <lory><2014-07-10> add begin
		@Override
		public void online_startFile(String path) throws RemoteException {
		}

		@Override
		public void online_start(long[] list, int position) throws RemoteException {
			mService.get().online_start(list, position);
		}

		@Override
		public void online_playListView(int position) throws RemoteException {
		}

		@Override
		public void setListInfo(List<AuroraListItem> list) throws RemoteException {
			mService.get().setListInfo(list);
		}

		@Override
		public List<AuroraListItem> getListInfo() throws RemoteException {
			return mService.get().getListInfo();
		}

		@Override
		public String getMusicbitrate() throws RemoteException {
			return mService.get().getMusicbitrate();
		}

		@Override
		public long secondaryPosition() throws RemoteException {
			return mService.get().secondaryPosition();
		}

		@Override
		public void notifyLrcPath(String path) throws RemoteException {
			mService.get().notifyLrcPath(path);
		}

		@Override
		public boolean isOnlineSong() throws RemoteException {
			return mService.get().isonlineSong();
		}

		@Override
		public String getLrcUri() throws RemoteException {
			return mService.get().getLrcUri();
		}

		// Iuni <lory><2014-07-10> add end

		// add by chenhl 20140902 start
		@Override
		public void updateCursor() throws RemoteException {
			mService.get().updateCursor();
		}

		@Override
		public void onHifiChanged(int on) throws RemoteException {
			mService.get().onHifiChanged(on);
		}

		@Override
		public void updateNotification() throws RemoteException {
			mService.get().updateNotify();
		}

		@Override
		public String getLryFile() throws RemoteException {
			return mService.get().getLryFile();
		}

		@Override
		public boolean getRadioType() throws RemoteException {

			return Application.isRadioType();
		}
		// add by chenhl 20140902 end

	}

	@Override
	protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
		writer.println("" + mPlayListLen + " items in queue, currently at index " + mPlayPos);
		writer.println("Currently loaded:");
		writer.println(getArtistName());
		writer.println(getAlbumName());
		writer.println(getTrackName());
		writer.println(getPath());
		writer.println("playing: " + mIsSupposedToBePlaying);
		writer.println("actual: " + mPlayer.mCurrentMediaPlayer.isPlaying());
		writer.println("shuffle mode: " + mShuffleMode);
		MusicUtils.debugDump(writer);
	}

	private final IBinder mBinder = new ServiceStub(this);

	// add by chenhl start 20140624
	private boolean mPauseByPhoneState = false;
	private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			LogUtil.d(LOGTAG, "zll ----- onCallStateChanged state:" + state + ",mPausedByTransientLossOfFocus:" + mPausedByTransientLossOfFocus + " mPauseByPhoneState:" + mPauseByPhoneState);
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				mPauseByPhoneState = (isPlaying() || mPauseByPhoneState);
				pause();
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				if (mPauseByPhoneState && !isPlaying()) {
					play();
					mPauseByPhoneState = false;
				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				mPauseByPhoneState = (isPlaying() || mPauseByPhoneState);
				pause();
				break;
			}
		}

	};

	// add by chenhl end 20140624

	// add by ukiliu 20140721 begin
	private void onUpdateOnlineArtImg() {
		String imgUrl = getImgUrl(120);
		LogUtil.d(LOGTAG, "imgUrl:" + imgUrl);
		if (imgUrl == null) {
			return;
		}
		ImageLoader.getInstance().loadImage(imgUrl, new ImageSize(mNotifyWidth, mNotifyWidth), new SimpleImageLoadingListener() {

			@Override
			public void onLoadingComplete(String s, View view1, Bitmap bitmap) {
				LogUtil.d(LOGTAG, "onUpdateOnlineArtImg onLoadingComplete....1:" + bitmap);
				mAlbumArtBitmap = bitmap;
				updateNotification(false);
			}

			@Override
			public void onLoadingFailed(String s, View view1, FailReason failreason) {
				LogUtil.d(LOGTAG, "onUpdateOnlineArtImg onLoadingFailed....2");
				mAlbumArtBitmap = null;
				updateNotification(false);
			}

		});
	}

	private Bitmap mAlbumArtBitmap;

	// add by ukiliu 20140721 end

	// add by chenhl start 20140903
	private void notifyChange() {
		sendChangebroadcast(QUEUE_CHANGED);
		sendChangebroadcast(META_CHANGED);
		String filepath = MusicUtils.getDbImg(this, getAudioId(), getTrackName(), getArtistName(), 0);
		if (filepath != null && filepath.equals(AuroraSearchLyricActivity.DEFUALT_IMG_URL)) {
			mAlbumArtBitmap = null;
		} else {
			if (mbOnline) {
				Bitmap bm = null;
				if (filepath != null && !filepath.isEmpty()) {
					// bm = BitmapFactory.decodeFile(filepath);

					bm = BitmapUtil.decodeSampledBitmapFromFileForSmall(filepath, mNotifyWidth, mNotifyWidth);
					// int len = AuroraShareUtil.bmpToByteArrayEx(bm, false);
					// Log.i(LOGTAG, "zll ---- ttttttt 2 bitmap len:"+len);
				}
				if (bm == null) {
					mAlbumArtBitmap = null;
					onUpdateOnlineArtImg();
				} else {
					mAlbumArtBitmap = bm;
				}

			} else {
				// String pathString = getFilePath();
				Bitmap bitmap = null;
				String strpath = AuroraMusicUtil.getImgPath(getApplication(), AuroraMusicUtil.MD5(getTrackName() + getArtistName() + getAlbumName()));

				if (filepath != null && filepath.equals(AuroraSearchLyricActivity.AURORA_ORIGINAL_IMG_URL)) {
					LogUtil.d(LOGTAG, "AURORA_ORIGINAL_IMG_URL-----------------");
					bitmap = MusicUtils.getAuroraArtwork(this, getAudioId(), -1, mNotifyWidth, mNotifyWidth);
				} else {
					bitmap = MusicUtils.getSmallArtwork(this, getAudioId(), getAlbumId(), false, strpath, 0, mNotifyWidth, mNotifyWidth);
				}
				if (bitmap == null) {
					mAlbumArtBitmap = null;
					if (AuroraMusicUtil.isWifiNetActvie(getApplicationContext()) && Globals.SWITCH_FOR_ONLINE_MUSIC) {
						onUpdateOnlineArtImg();
					}
				} else {
					mAlbumArtBitmap = bitmap;
				}
			}
		}

		saveQueue(true);
		mAppWidgetProvider.notifyChange(this, QUEUE_CHANGED);
		mAppWidgetProvider.notifyChange(this, META_CHANGED);

	}

	private void sendChangebroadcast(String what) {
		Intent i = new Intent(what);
		i.putExtra("id", Long.valueOf(getAudioId()));
		i.putExtra("artist", getArtistName());
		i.putExtra("album", getAlbumName());
		i.putExtra("track", getTrackName());
		i.putExtra("playing", isPlaying());
		i.putExtra("isnet", mbOnline);
		i.putExtra("filepath", mLrcPath);// lory add
		i.putExtra("isfavorite", isFavorite());// lory add
		sendStickyBroadcast(i);
	}

	// add by chenhl end 20140903

	// Iuni <lory><2014-10-09> add begin
	private SensorManager mSensorManager = null;
	private Sensor mSensor;
	private boolean mbOpenSensor = false;
	private int mMatchTimes = 0;// paul modify for bug 14480

	private void openSensorManagerListner() {
		synchronized (MediaPlaybackService.this) {
			if (mbOpenSensor) {
				return;
			}

			if (mSensorManager == null) {
				mSensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
				mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				mbOpenSensor = false;
			}

			mSensorManager.registerListener(mSensorEventListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
			mbOpenSensor = true;
		}
	}

	private void disopenSensorManager() {
		if (mSensorManager != null && mbOpenSensor) {
			mSensorManager.unregisterListener(mSensorEventListener);
		}
		mbOpenSensor = false;
	}

	private final SensorEventListener mSensorEventListener = new SensorEventListener() {
		private boolean bLastSensor = false;
		private boolean bCurSensor = false;

		@Override
		public void onSensorChanged(SensorEvent sensorevent) {
			if (sensorevent.values[2] < -8) {
				if (++mMatchTimes > 3) {
					bCurSensor = true;
				}
			} else {
				bCurSensor = false;
				mMatchTimes = 0;
			}

			if (bLastSensor != bCurSensor) {
				LogUtil.d(LOGTAG, "zll ----onSensorChanged ----bCurSensor:" + bCurSensor);

				if (bCurSensor && !mbHasEar && !isBluetoothAvailable()) {
					Intent intent = new Intent();
					intent.setAction(SERVICECMD);
					intent.putExtra(CMDNAME, CMDPAUSE);
					sendBroadcast(intent);
				}

				bLastSensor = bCurSensor;
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int i) {
		}
	};

	private void openCloseHIfi(boolean open) {
		synchronized (MediaPlaybackService.this) {
			if (Globals.SWITCH_FOR_SOUND_CONTROL && mAudioManager != null) {
				boolean opened = false;
				int hifi = MusicUtils.getIntPref(this, AuroraSoundControl.AURORA_DATA_HIFI_STATUS, -1);
				if (mAudioManager.getParameters("HIFI_SWITCH").contentEquals("1")) {
					opened = true;
				}

				Log.i(LOGTAG, "zll ----- OpenCloseHIfi 0 open:" + open + ",opened:" + opened + ",hifi:" + hifi + ",hasear:" + mAudioManager.isWiredHeadsetOn());
				if (opened && !mAudioManager.isWiredHeadsetOn()) {
					Log.i(LOGTAG, "zll ----- OpenCloseHIfi 1 open:");
					mAudioManager.setParameters("HIFI_SWITCH=0");
					// MusicUtils.setIntPref(this,
					// AuroraSoundControl.AURORA_DATA_HIFI_STATUS, 0);
					return;
				}

				if (hifi > 0 && mAudioManager.isWiredHeadsetOn()) {
					if (opened != open) {
						if (open) {
							onHifiChangedEx(false, 1);

							/*
							 * mAudioManager.setParameters("HIFI_SWITCH=1"); int
							 * num1 = 0;
							 * while(!mAudioManager.getParameters("HIFI_SWITCH"
							 * ).contentEquals("1")){ if (num1 > 5) {
							 * Log.i(LOGTAG,
							 * "zll ----- openCloseHIfi 2 set fail :hifi > 1s");
							 * break; }
							 * 
							 * num1++; try { Thread.sleep(10); } catch
							 * (InterruptedException e) { e.printStackTrace(); }
							 * }
							 */
						} else {
							onHifiChangedEx(false, 0);

							/*
							 * mAudioManager.setParameters("HIFI_SWITCH=0"); int
							 * num2 = 0;
							 * while(!mAudioManager.getParameters("HIFI_SWITCH"
							 * ).contentEquals("0")){ if (num2 > 5) {
							 * Log.i(LOGTAG,
							 * "zll ----- openCloseHIfi 3 set fail :hifi > 1s");
							 * break; }
							 * 
							 * num2++; try { Thread.sleep(10); } catch
							 * (InterruptedException e) { e.printStackTrace(); }
							 * }
							 */
						}
					}
				}
			}
		}
	}

	private void onHifiChanged(int on) {
		onHifiChangedEx(true, on);
	}

	private void onHifiChangedEx(boolean fromother, int on) {
		// Log.i(LOGTAG, "zll ----- onHifiChangedEx 0 on:"+on);
		if (fromother) {
			if (isPlaying()) {
				if (mHifiTask == null) {
					mHifiTask = new AuroraHifiThread();
					// mHifiTask.start();
					executor.submit(mHifiTask);
				}

				boolean swith = (on == 1) ? true : false;
				Log.i(LOGTAG, "zll ----- onHifiChangedEx 1 swith:" + swith);
				mHifiTask.onNotify(swith);
			}
		} else {
			if (mHifiTask == null) {
				mHifiTask = new AuroraHifiThread();
				// mHifiTask.start();
				executor.submit(mHifiTask);
			}

			boolean swith = (on == 1) ? true : false;
			Log.i(LOGTAG, "zll ----- onHifiChangedEx 2 swith:" + swith);
			mHifiTask.onNotify(swith);
		}

		return;
	}

	private void onOffHifi(boolean on, boolean run) {
		AudioManager audioManager = mAudioManager;
		if (audioManager == null) {
			audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		}

		// Log.i(LOGTAG, "zll ---- onOffHifi xxxx Thread name:"+
		// Thread.currentThread().getName());

		if (on) {
			audioManager.setParameters("HIFI_SWITCH=1");
			int num1 = 0;
			while (!audioManager.getParameters("HIFI_SWITCH").contentEquals("1")) {
				if (num1 > 10 || !run) {
					Log.i(LOGTAG, "zll ----- onOffHifi 1 set fail ,hifi > 1s run:" + run);
					break;
				}

				num1++;
				try {
					// Log.i(LOGTAG, "zll ---- onOffHifi 1.1 Thread name:"+
					// Thread.currentThread().getName());
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else {
			audioManager.setParameters("HIFI_SWITCH=0");
			int num2 = 0;
			while (!audioManager.getParameters("HIFI_SWITCH").contentEquals("0")) {
				if (num2 > 10 || !run) {
					Log.i(LOGTAG, "zll ----- onOffHifi 2 set fail ,hifi > 1s run:" + run);
					break;
				}

				num2++;
				try {
					// Log.i(LOGTAG, "zll ---- onOffHifi 2.1 Thread name:"+
					// Thread.currentThread().getName());
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		return;
	}

	private AuroraHifiThread mHifiTask;

	private class AuroraHifiThread extends Thread {
		private volatile boolean mActive = true;
		private volatile boolean mDirty = false;
		private volatile boolean mFifiOn = false;

		private void updateLoading() {
		}

		@Override
		public void run() {
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

			while (mActive) {
				synchronized (this) {
					if (mActive && !mDirty) {
						try {
							this.wait();
						} catch (InterruptedException e) {
							Log.w(LOGTAG, "zll unexpected interrupt: " + this);
						}
						continue;
					}

					mDirty = false;
				}

				// boolean flag = updateFavoritesEx();
				onOffHifi(mFifiOn, mActive);
				updateLoading();
			}
		}

		public synchronized void onNotify(boolean on) {
			// Log.i(LOGTAG, "zll ---- onNotify xxxx 1");
			mDirty = true;
			mFifiOn = on;
			notifyAll();
			return;
		}

		public synchronized void terminate() {
			// Log.i(LOGTAG, "zll ---- terminate xxxx 1");
			mFifiOn = false;
			mActive = false;
			notifyAll();
			return;
		}
	}

	// Iuni <lory><2014-10-09> add end

	// add by chenhl start for xiamisdk
	private void initXiamiPlayer(Handler handler) {
		mAuroraXiamiPlayer = new AuroraXiamiPlayer(getApplicationContext(), handler);
		return;
	}

	private void releaseXiamiPlayer() {
		if (mAuroraXiamiPlayer != null) {
			mAuroraXiamiPlayer.release();
			mAuroraXiamiPlayer = null;
		}

		return;
	}

	private String getImgUrl(int size) {
		if (mAuroraXiamiPlayer == null || !mbOnline) {
			return null;
		}
		return mAuroraXiamiPlayer.getImgUrl(size);
	}

	private String getLryFile() {
		if (mAuroraXiamiPlayer == null || !mbOnline) {
			return null;
		}
		return mAuroraXiamiPlayer.getLyricFile();
	}

	// add by JXH for DTS 20150813 begin
	private void registerVolumeListenerReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.media.VOLUME_CHANGED_ACTION");
		registerReceiver(VolumeListenerReceiver, filter);
	}

	private void unRegisterVolumeListenerReceiver() {
		unregisterReceiver(VolumeListenerReceiver);
	}

	private final BroadcastReceiver VolumeListenerReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent != null) {
				int streamType = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
				if (streamType != AudioManager.STREAM_MUSIC) {
					return;
				}
			}
			boolean isDTSSupport = Globals.SWITCH_FOR_SOUND_CONTROL && DtsEffects.getInstance(getApplicationContext()).isDtsOpen();
			if (isDTSSupport) {
				Message message = mMediaplayerHandler.obtainMessage();
				message.what = VOLUME_CHANGE;
				mMediaplayerHandler.removeMessages(VOLUME_CHANGE);
				mMediaplayerHandler.sendMessageDelayed(message, 200);
			}
		}
	};
	private int mVolumeValue = -1;

	private void changeDtsData() {
		if (mbHasEar) {
			int musicVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			DtsEffects dtsEffects = DtsEffects.getInstance(getApplicationContext());
			if (mVolumeValue < musicVolume && musicVolume >= DtsEffects.DtsEffectsMaxVolume) {
				dtsEffects.setHeadphoneParam(0, 0, 0);
				// LogUtil.d(LOGTAG, "------setTrubassCompressor 0 "
				// + " musicVolume:" + musicVolume);
			}
			if (mVolumeValue > musicVolume && musicVolume < DtsEffects.DtsEffectsMaxVolume) {

				double[] ds = dtsEffects.getHeadphoneParam(getApplicationContext());
				if (ds != null) {
					dtsEffects.setHeadphoneParam(ds[5], ds[2], ds[3]);
				}
				// LogUtil.d(LOGTAG, "------setTrubassCompressor vol:"
				// + dtsEffects.getTrubassCompressor() + " musicVolume:"
				// + musicVolume);
			}
			mVolumeValue = musicVolume;
		}
	}
	// add by JXH for DTS 20150813 end
}
