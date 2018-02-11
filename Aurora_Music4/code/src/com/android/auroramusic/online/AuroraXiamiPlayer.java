package com.android.auroramusic.online;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;

import com.android.auroramusic.local.AuroraPlayer;
import com.android.auroramusic.model.XiaMiSdkUtils;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.LogUtil;
import com.android.auroramusic.util.ThreadPoolExecutorUtils;
import com.android.music.MediaPlaybackService;
import com.xiami.sdk.entities.OnlineSong;

public class AuroraXiamiPlayer {
	private static final String TAG = "AuroraXiamiPlayer";
	private Handler mHandler = null;
	private MediaPlayer mMusicPlayer = null;
	private final static int AURORA_STOPPED = 1;
	private final static int AURORA_PLAYING = 2;
	private final static int AURORA_PAUSED = 3;
	private final static int AURORA_INIT = 4;
	private final static int AURORA_PLAY_ERROR = 5;
	private final static int AURORA_PLAY = 6;
	private int mStatus = AURORA_STOPPED;
	private OnlineSong currentSong;
	private long[] mSongIds;
	private boolean mOnLineInitialized = false;
	public static final int XIAMI_SDK_REFRESH_DURATION = 300;
	public static final int XIAMI_TRACK_ENDED = XIAMI_SDK_REFRESH_DURATION + 1;
	public static final int XIAMI_TRACK_ERROR_ENDED = XIAMI_SDK_REFRESH_DURATION + 2;
	public static final int XIAMI_TRACK_URI_UPDATE = XIAMI_SDK_REFRESH_DURATION + 3;
	private Context mContext;
	private int mIndex = 0;
	private GetDownloadTask mGetDownloadTask;
	private long secodedu;
//	private MediaPlaybackService mediaPlaybackService;
	public AuroraXiamiPlayer(Context context, Handler handler) {
		if (context == null) {
			LogUtil.i(TAG, "AuroraXiamiPlayer fail ---- 1");
			return;
		}
	//	if(context instanceof MediaPlaybackService){
	//		mediaPlaybackService = (MediaPlaybackService) context;
	//	}
		mContext = context;
		mHandler = handler;
		mMusicPlayer = new MediaPlayer();
		mMusicPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mMusicPlayer.setOnCompletionListener(mOnCompletionListener);
		mOnLineInitialized = true;
	}

	public boolean isOnlineInitialized() {
		return mOnLineInitialized;
	}

	public void initListId(long[] list) {
		if (list == null) {
			return;
		}
		mSongIds = list;
		return;
	}

	private OnPreparedListener mOnPreparedListener = new OnPreparedListener() {
		@Override
		public void onPrepared(MediaPlayer mediaplayer) {
			LogUtil.d(TAG, "onPrepared mStatus:" + mStatus);
			if (mStatus == AURORA_PLAY) {
				mMusicPlayer.start();
			}
			mStatus = AURORA_PLAYING;
			mHandler.removeMessages(XIAMI_SDK_REFRESH_DURATION);
			mHandler.sendEmptyMessage(XIAMI_SDK_REFRESH_DURATION);
		}
	};
	private OnCompletionListener mOnCompletionListener = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mediaplayer) {
			LogUtil.d(TAG, "onCompletion...mStatus:" + mStatus);
			// 防止多次调用
			if (mStatus == AURORA_PLAY_ERROR) {
				// stop();
				// mErrorListener.onError(mMusicPlayer, -900, -1);
				mHandler.obtainMessage(XIAMI_TRACK_ERROR_ENDED, -900, -1).sendToTarget();
				return;
			}
			if (mStatus == AURORA_PLAYING) {
				mStatus = AURORA_STOPPED;
				mHandler.sendEmptyMessage(XIAMI_TRACK_ENDED);
			}
		}
	};
	private OnErrorListener mErrorListener = new OnErrorListener() {
		@Override
		public boolean onError(MediaPlayer mediaplayer, int what, int j) {
			LogUtil.d(TAG, "mErrorListener...:" + what + " j:" + j);
		//	switch (what) {
		//	case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
		//		if(mediaPlaybackService!=null){
		//			mediaPlaybackService.sendSessionIdToAudioEffect(false);
		//		}
		//		break;
		//	case MediaPlaybackService.MEDIA_ERROR_MUSICFX_DIED:
		//		mediaPlaybackService.setAuxEffectId(0);
		//		if(mediaPlaybackService!=null){
		//			mediaPlaybackService.sendSessionIdToAudioEffect(true);
		//		}
		//		break;
		//	default:
		//		if(mediaPlaybackService!=null){
		//			mediaPlaybackService.sendSessionIdToAudioEffect(true);
		//		}
		//		break;
		//	}
			stop();
			if (mMusicPlayer != null) {
				mMusicPlayer.release();
			}
			mMusicPlayer = new MediaPlayer();
			mMusicPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMusicPlayer.setOnCompletionListener(mOnCompletionListener);
			mStatus = AURORA_PLAY_ERROR;
			mHandler.obtainMessage(XIAMI_TRACK_ERROR_ENDED, -900, -1).sendToTarget();
			return true;
		}
	};

	public void open(int position) {
		LogUtil.d(TAG, "open...:" + position + " mSongIds.length:" + mSongIds.length);
		if (mSongIds == null || mSongIds.length == 0) {
			return;
		}
		int pos = position;
		if (position < 0) {
			pos = 0;
		} else if (position >= mSongIds.length) {
			pos = mSongIds.length - 1;
		}
		mIndex = pos;
		mStatus = AURORA_INIT;
		if (mGetDownloadTask != null) {
			mGetDownloadTask.cancel();
		}
		mGetDownloadTask = new GetDownloadTask();
		mGetDownloadTask.executeOnExecutor(ThreadPoolExecutorUtils.getThreadPoolExecutor().getExecutor());
	}

	public boolean play() {
		LogUtil.d(TAG, "playmStatus......:" + mStatus);
		if (mSongIds == null || mSongIds.length == 0) {
			return false;
		}
		if (mStatus == AURORA_PAUSED) {
			resume();
			return true;
		}
		if (!AuroraMusicUtil.isNetWorkActive(mContext)) {
			mStatus = AURORA_PLAY_ERROR;
			// mHandler.obtainMessage(XIAMI_TRACK_ERROR_ENDED, -900, -1).sendToTarget();
			return false;
		}
		if (mStatus == AURORA_PLAY_ERROR || mStatus == AURORA_STOPPED) {
			mStatus = AURORA_PLAY;
			if (mGetDownloadTask != null) {
				mGetDownloadTask.cancel();
			}
			mGetDownloadTask = new GetDownloadTask();
			mGetDownloadTask.executeOnExecutor(ThreadPoolExecutorUtils.getThreadPoolExecutor().getExecutor());
			return true;
		}
		if (mStatus == AURORA_PLAYING) {
			mMusicPlayer.start();
			LogUtil.d(TAG, "-----------mMusicPlayer.start()");
			return true;
		}
		mStatus = AURORA_PLAY;
		LogUtil.d(TAG, "1111 play......:" + mStatus);
		return true;
	}
	
	public boolean isPlaying(){
		if(mMusicPlayer==null){
			return false;
		}
		return mMusicPlayer.isPlaying();
	}
	
	/**
     * M: attatch auxdio effect for Auxiliary audio effect
     *
     * @param effectId: the effect id.
     */
    public void attachAuxEffect(int effectId) {
    	if(mMusicPlayer==null){
			return;
		}
    	mMusicPlayer.attachAuxEffect(effectId);
    }

    /**
     * M: set audio effect send level for Auxiliary audio effect
     *
     * @param level: the auxiliary effect level.
     */
    public void setAuxEffectSendLevel(float level) {
    	if(mMusicPlayer==null){
			return;
		}
    	mMusicPlayer.setAuxEffectSendLevel(level);
    }

	public void resume() {
		if (mMusicPlayer == null) {
			return;
		}
		mMusicPlayer.start();
		mStatus = AURORA_PLAYING;
	}

	public boolean pause() {
		if (mMusicPlayer == null || mStatus != AURORA_PLAYING) {
			return false;
		}

		mMusicPlayer.pause();
		mStatus = AURORA_PAUSED;
		return true;
	}

	public void stop() {
		secodedu = 0;
		LogUtil.d(TAG, "--stop--mStatus:" + mStatus);
		if (AURORA_STOPPED == mStatus) {
			return;
		}
		if (mMusicPlayer != null) {
			mMusicPlayer.stop();
			mMusicPlayer.setOnErrorListener(null);
			mMusicPlayer.setOnPreparedListener(null);
			mMusicPlayer.setOnBufferingUpdateListener(null);
			mStatus = AURORA_STOPPED;
			LogUtil.d(TAG, "--mMusicPlayer.stop()");
		}
	}

	public void release() {
		if (mMusicPlayer != null) {
			mMusicPlayer.release();
		}
	}

	public long getSongId() {
		if (currentSong == null) {
			return -1;
		}
		return currentSong.getSongId();
	}

	public long getArtistId() {
		if (currentSong == null) {
			return -1;
		}
		return currentSong.getArtistId();
	}

	public long getAlbumId() {
		if (currentSong == null) {
			return -1;
		}
		return currentSong.getAlbumId();
	}

	public String getTrackName() {
		if (currentSong == null) {
			return null;
		}
		synchronized (this) {
			return currentSong.getSongName();
		}
	}

	public String getAlbumName() {
		if (currentSong == null) {
			return null;
		}
		synchronized (this) {
			return currentSong.getAlbumName();
		}
	}

	public String getArtistName() {
		if (currentSong == null) {
			return null;
		}
		synchronized (this) {
			return currentSong.getArtistName();
		}
	}

	public long duration() {
		if (mStatus == AURORA_INIT || mStatus == AURORA_PLAY || mStatus == AURORA_PLAY_ERROR) {
			return 0;
		}
		return mMusicPlayer.getDuration();
	}

	public long position() {
		if (mStatus == AURORA_INIT || mStatus == AURORA_PLAY || mStatus == AURORA_PLAY_ERROR) {
			return 0;
		}
		if (mMusicPlayer == null) {
			return -1;
		}
		return mMusicPlayer.getCurrentPosition();
	}

	public long secondaryPosition() {
		if (mMusicPlayer == null) {
			return -1;
		}
		// return (long)(((float)secodedu/duration)*100);
		return secodedu;
	}

	public long seek(long whereto) {
		if (mMusicPlayer == null) {
			return 0;
		}
		if (whereto > secodedu * mMusicPlayer.getDuration() / 100) {
			return position();
		}
		mMusicPlayer.seekTo((int) whereto);
		return whereto;
	}

	public int getAudioSessionId() {
		if (mMusicPlayer == null) {
			return 0;
		}
		return mMusicPlayer.getAudioSessionId();
	}

	public String getImgUrl(int size) {
		if (currentSong == null) {
			return null;
		}
		synchronized (this) {
			return currentSong.getImageUrl(size);
		}
	}

	public String getLyricFile() {
		if (currentSong == null) {
			return null;
		}
		synchronized (this) {
			return currentSong.getLyric();
		}
	}

	public String getSongUrl() {
		if (currentSong == null) {
			return null;
		}
		synchronized (this) {
			return currentSong.getListenFile();
		}
	}

	public void setVolume(float vol) {
		if (mMusicPlayer != null) {
			mMusicPlayer.setVolume(vol, vol);
		}
	}

	class GetDownloadTask extends AsyncTask<String, Integer, Integer> {
		boolean isFirst = true;
		boolean isCancel = false;
		int total = 0;

		public void cancel() {
			isCancel = true;
		}

		@Override
		protected void onPostExecute(Integer result) {}

		@Override
		protected void onPreExecute() {
			isCancel = false;
		}

		@Override
		protected Integer doInBackground(String... arg0) {
			OnlineSong onlineSong = XiaMiSdkUtils.findSongByIdSync(mContext, mSongIds[mIndex],
					AuroraMusicUtil.getOnlineSongQuality());
			LogUtil.d(TAG, "doInBackground 1:" + onlineSong + " isCancel:" + isCancel);
			if (isCancel) {
				return -1;
			}
			if (onlineSong == null || TextUtils.isEmpty(onlineSong.getListenFile())) {
				LogUtil.d(TAG, "doInBackground get onlinesong fail!");
				mStatus = AURORA_PLAY_ERROR;
				mHandler.obtainMessage(XIAMI_TRACK_ERROR_ENDED, -900, -1).sendToTarget();
				return -1;
			}
			// AuroraMusicUtil.ShowOnlineSong(onlineSong);
			currentSong = onlineSong;
			mHandler.removeMessages(XIAMI_TRACK_URI_UPDATE);
			mHandler.obtainMessage(XIAMI_TRACK_URI_UPDATE, currentSong.getListenFile()).sendToTarget();
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					secodedu = 0;
					playUrl();
				}
			});
			return -1;
		}
	}

	private void playUrl() {
		try {
//			AuroraMusicUtil.justTest("----playUrl " + mStatus);
			LogUtil.d(TAG, "musicplayer start...");
			mMusicPlayer.setOnErrorListener(mErrorListener);
			mMusicPlayer.setOnPreparedListener(mOnPreparedListener);
			mMusicPlayer.reset();
			mMusicPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
			mMusicPlayer.setDataSource(currentSong.getListenFile());
			mMusicPlayer.prepareAsync();

		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.e(TAG, "playUrl error!", e);
			mErrorListener.onError(mMusicPlayer, -900, -1);
		}
	}

	private OnBufferingUpdateListener onBufferingUpdateListener = new OnBufferingUpdateListener() {
		@Override
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			secodedu = percent;
		}
	};

}
