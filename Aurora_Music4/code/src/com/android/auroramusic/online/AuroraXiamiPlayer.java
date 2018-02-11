package com.android.auroramusic.online;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpStatus;

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
import com.android.auroramusic.util.AuroraListItem;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.LogUtil;
import com.android.auroramusic.util.tagUtils.Frame;
import com.android.auroramusic.util.tagUtils.FrameUtil;
import com.xiami.sdk.entities.OnlineSong;
import com.xiami.sdk.entities.OnlineSong.Quality;

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
	private long duration, secodedu;
	private String playcache = Globals.mCachePath + File.separator + "tmp.dat";
	private static final int READY_BUFFER = 160 * 1024;

	private AuroraPlayer mLocalPlayer = null;

	public AuroraXiamiPlayer(Context context, Handler handler) {
		if (context == null) {
			LogUtil.i(TAG, "AuroraXiamiPlayer fail ---- 1");
			return;
		}
		mContext = context;
		mHandler = handler;
		if (!isVersionLollipop()) {
			mMusicPlayer = new MediaPlayer();
			mMusicPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMusicPlayer.setOnCompletionListener(mOnCompletionListener);
		}
		mOnLineInitialized = true;
	}

	public boolean isOnlineInitialized() {

		return mOnLineInitialized;
	}

	public void setListInfo(ArrayList<AuroraListItem> list) {
		// mMusicList = list;
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
//				 stop();
//				 mErrorListener.onError(mMusicPlayer, -900, -1);
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
		public boolean onError(MediaPlayer mediaplayer, int i, int j) {
			LogUtil.d(TAG, "mErrorListener...:" + i + " j:" + j);
			stop();
			if(mMusicPlayer!=null){
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
		mGetDownloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public boolean play() {
		LogUtil.d(TAG, "play......:" + mStatus);
		if (mSongIds == null || mSongIds.length == 0) {
			return false;
		}

		if (mStatus == AURORA_PAUSED) {
			resume();
			return true;
		}
		if (!AuroraMusicUtil.isNetWorkActive(mContext)) {
			mStatus = AURORA_PLAY_ERROR;
//			mHandler.obtainMessage(XIAMI_TRACK_ERROR_ENDED, -900, -1).sendToTarget();
			return false;
		}

		if (mStatus == AURORA_PLAY_ERROR || mStatus == AURORA_STOPPED) {
			mStatus = AURORA_PLAY;

			if (mGetDownloadTask != null) {
				mGetDownloadTask.cancel();
			}
			mGetDownloadTask = new GetDownloadTask();
			mGetDownloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			return true;
		}

		if (mStatus == AURORA_PLAYING) {
			if (isVersionLollipop()) {
				mLocalPlayer.start();
			} else {
				mMusicPlayer.start();
				LogUtil.d(TAG, "-----------mMusicPlayer.start()");
			}
			return true;
		}
		mStatus = AURORA_PLAY;
		return true;
	}

	public void resume() {
		if (isVersionLollipop()) {
			mLocalPlayer.start();
			mStatus = AURORA_PLAYING;
			return;
		}
		if (mMusicPlayer == null) {
			return;
		}
		mMusicPlayer.start();
		mStatus = AURORA_PLAYING;
	}

	public boolean pause() {
		if (isVersionLollipop()) {
			if (mLocalPlayer == null || mStatus != AURORA_PLAYING) {
				return false;
			}
			mLocalPlayer.pause();
			mStatus = AURORA_PAUSED;
			return true;
		}

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
		if (isVersionLollipop()) {
			if (mLocalPlayer != null) {
				LogUtil.d(TAG, "stop()...mStatus:" + mStatus);
				mLocalPlayer.setOnErrorListener(null);
				mLocalPlayer.setOnPreparedListener(null);
				mLocalPlayer.setOnCompletionListener(null);
				mLocalPlayer.stop();
				mStatus = AURORA_STOPPED;
			}
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
		if (isVersionLollipop()) {
			if (mLocalPlayer != null)
				mLocalPlayer.release();
			return;
		}
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
		if (isVersionLollipop()) {
			if (mLocalPlayer == null) {
				return -1;
			}
			return mLocalPlayer.getDuration();
		}
		return mMusicPlayer.getDuration();
	}

	public long position() {

		if (mStatus == AURORA_INIT || mStatus == AURORA_PLAY || mStatus == AURORA_PLAY_ERROR) {
			return 0;
		}
		if (isVersionLollipop()) {

			return mLocalPlayer.getCurrentPosition();
		}
		if (mMusicPlayer == null) {
			return -1;
		}
		return mMusicPlayer.getCurrentPosition();
	}

	public long secondaryPosition() {
		if (isVersionLollipop()) {
			return (long) (((float) secodedu / duration) * 100);
		}
		if (mMusicPlayer == null) {
			return -1;
		}
		// return (long)(((float)secodedu/duration)*100);
		return secodedu;
	}

	public long seek(long whereto) {

		if (isVersionLollipop()) {
			if (whereto / 1000 > secodedu - 2) {
				return position();
			}
			mLocalPlayer.seekTo((int) whereto);
			return whereto;
		}

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
		if (isVersionLollipop()) {

			return 0;
		}
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
		if (isVersionLollipop()) {
			return;
		}
		if (mMusicPlayer != null) {
			mMusicPlayer.setVolume(vol, vol);
		}
	}

	class GetDownloadTask extends AsyncTask<String, Integer, Integer> {

		private Frame frame;
		boolean isFirst = true;
		boolean isCancel = false;
		int total = 0;

		public void cancel() {
			isCancel = true;
		}

		@Override
		protected void onPostExecute(Integer result) {

		}

		@Override
		protected void onPreExecute() {
			isCancel = false;
		}

		@Override
		protected Integer doInBackground(String... arg0) {

			OnlineSong onlineSong = XiaMiSdkUtils.findSongByIdSync(mContext, mSongIds[mIndex], AuroraMusicUtil.getOnlineSongQuality());
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
//			AuroraMusicUtil.ShowOnlineSong(onlineSong);
			currentSong = onlineSong;
			mHandler.removeMessages(XIAMI_TRACK_URI_UPDATE);
			mHandler.obtainMessage(XIAMI_TRACK_URI_UPDATE, currentSong.getListenFile()).sendToTarget();
			if (!isVersionLollipop()) {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						secodedu = 0;
						playUrl();
					}
				});
				return -1;
			}
			isFirst = true;
			URL url = null;
			HttpURLConnection http = null;
			InputStream inputStream = null;
			RandomAccessFile fos = null;
			File dirFile = new File(Globals.mCachePath);
			if (!dirFile.exists()) {
				dirFile.mkdirs();
			}
			try {
				url = new URL(currentSong.getListenFile());
				http = (HttpURLConnection) url.openConnection();
				http.setConnectTimeout(10 * 1000);
				http.setReadTimeout(10 * 1000);
				http.connect();
				int code = http.getResponseCode();
				final int filelenth = http.getContentLength();
				File tmpFile = new File(playcache);
				if (tmpFile.exists()) {
					tmpFile.delete();
				}
				tmpFile.createNewFile();
				fos = new RandomAccessFile(tmpFile, "rwd");
				LogUtil.d(TAG, "filelenth:" + filelenth + " isCancel:" + isCancel);
				fos.setLength(filelenth);
				if (code == HttpStatus.SC_OK) {

					inputStream = http.getInputStream();
					byte[] buffer = new byte[4096];
					int offset = 0;
					total = 0;
					while ((offset = inputStream.read(buffer)) != -1) {
						if (isCancel) {

							break;
						}
						fos.write(buffer, 0, offset);
						total += offset;
						if (isFirst && total <= READY_BUFFER) {
							continue;
						}
						// publishProgress(filelenth,total);
						if (total >= READY_BUFFER && isFirst) {
							isFirst = false;
							LogUtil.d(TAG, "start play ....");
							try {
								frame = FrameUtil.CalcFrame(playcache, filelenth);
								duration = (long) (frame.playTime);

								mHandler.post(new Runnable() {

									@Override
									public void run() {

										if (isVersionLollipop()) {
											localPlayUrl(total);
										} else {
											playUrl();
										}
									}
								});

							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							if (isVersionLollipop() && mLocalPlayer != null) {
								mLocalPlayer.setDownloadSize(total);
							}
						}

						if (frame != null) {
							secodedu = (long) frame.getDuration(total);
						}
					}

					if (total <= READY_BUFFER && !isCancel) {
						// 整个文件不够200k 之间播放。
						mHandler.post(new Runnable() {

							@Override
							public void run() {

								try {
									frame = FrameUtil.CalcFrame(playcache, filelenth);
									duration = (long) (frame.playTime);

									if (isVersionLollipop()) {
										localPlayUrl(filelenth);
									} else {
										playUrl();
									}
								} catch (Exception e) {
									e.printStackTrace();
								}

								if (frame != null) {
									secodedu = (long) frame.getDuration(filelenth);
								}
							}
						});
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				LogUtil.d(TAG, "donwload error!", e);
				mStatus = AURORA_PLAY_ERROR;
				if (isVersionLollipop()) {
					mAuroraErrorListener.onError(mLocalPlayer, -900, -1);
				} else {
					mErrorListener.onError(mMusicPlayer, -900, -1);
				}
			} finally {
				if (http != null) {
					http.disconnect();
				}
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return 0;
		}
	}

	private void playUrl() {
		try {
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

	private boolean isVersionLollipop() {

		return false;

		// return Build.VERSION.SDK_INT>=21?false:true;
		// return
		// Build.MODEL.equals("MI 3")&&Build.VERSION.RELEASE.equals("4.2.1");
	}

	private OnBufferingUpdateListener onBufferingUpdateListener = new OnBufferingUpdateListener() {

		@Override
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			secodedu = percent;
		}
	};

	private void localPlayUrl(int size) {

		try {
			if (mLocalPlayer != null) {
				mLocalPlayer.reset();
				mLocalPlayer.release();
				mLocalPlayer = null;
			}
			LogUtil.d(TAG, "localPlayUrl start ....:" + size);
			mLocalPlayer = new AuroraPlayer();
			mLocalPlayer.setDownloadSize(size);
			mLocalPlayer.setOnPreparedListener(mAuroraPreparedListener);
			mLocalPlayer.setOnCompletionListener(mAuroraCompletionListener);
			mLocalPlayer.setOnErrorListener(mAuroraErrorListener);
			mLocalPlayer.setDataSource(playcache);
			mLocalPlayer.prepareAsync();
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.d(TAG, "localPlayUrl!");
			mAuroraErrorListener.onError(mLocalPlayer, -900, -1);
		}
	}

	private AuroraPlayer.OnPreparedListener mAuroraPreparedListener = new AuroraPlayer.OnPreparedListener() {

		@Override
		public void onPrepared(AuroraPlayer mp) {
			LogUtil.d(TAG, "AuroraPlayer onPrepared mStatus:" + mStatus);
			if (mStatus == AURORA_PLAY) {
				mLocalPlayer.start();
			}
			mStatus = AURORA_PLAYING;
			mHandler.removeMessages(XIAMI_SDK_REFRESH_DURATION);
			mHandler.sendEmptyMessage(XIAMI_SDK_REFRESH_DURATION);
		}
	};

	private AuroraPlayer.OnCompletionListener mAuroraCompletionListener = new AuroraPlayer.OnCompletionListener() {

		@Override
		public void onCompletion(AuroraPlayer mp) {

			LogUtil.d(TAG, "AuroraPlayer onCompletion...mStatus:" + mStatus);
			// 防止多次调用
			if (mStatus == AURORA_PLAY_ERROR) {
				mAuroraErrorListener.onError(mLocalPlayer, -900, -1);
				return;
			}
			if (mStatus == AURORA_PLAYING) {
				mStatus = AURORA_STOPPED;
				mHandler.sendEmptyMessage(XIAMI_TRACK_ENDED);
			}
		}
	};

	private AuroraPlayer.OnErrorListener mAuroraErrorListener = new AuroraPlayer.OnErrorListener() {

		@Override
		public boolean onError(AuroraPlayer mp, int what, int extra) {
			LogUtil.d(TAG, "mErrorListener...:" + what + " extra:" + extra);
			stop();
			mStatus = AURORA_PLAY_ERROR;
			mHandler.obtainMessage(XIAMI_TRACK_ERROR_ENDED, what, extra).sendToTarget();
			return true;
		}
	};

	public void resetOfLocalPlay() {
		if (mLocalPlayer != null) {
			mLocalPlayer.release();
			mLocalPlayer = null;
		}
	}
}
