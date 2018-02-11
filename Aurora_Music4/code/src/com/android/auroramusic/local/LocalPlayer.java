package com.android.auroramusic.local;

import java.io.IOException;

import android.content.Context;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.android.auroramusic.local.AuroraPlayer.OnCompletionListener;
import com.android.auroramusic.local.AuroraPlayer.OnErrorListener;
import com.android.auroramusic.local.AuroraPlayer.OnInfoListener;


public class LocalPlayer {
	
	private final static String TAG = "LocalPlayer";
	private Uri mUri;
	private int mDuration;

	// all possible internal states
	private static final int STATE_ERROR = -1;
	private static final int STATE_IDLE = 0;
	private static final int STATE_PREPARING = 1;
	private static final int STATE_PREPARED = 2;
	private static final int STATE_PLAYING = 3;
	private static final int STATE_PAUSED = 4;
	private static final int STATE_PLAYBACK_COMPLETED = 5;
	
	
	public static final int LOCALPLAYER_STATE_ERROR = 201;
	public static final int LOCALPLAYER_STATE_PREPARED = LOCALPLAYER_STATE_ERROR+1;
	public static final int LOCALPLAYER_STATE_NEXT = LOCALPLAYER_STATE_ERROR+2;


	private static final int FORMAT_TYPE = PixelFormat.RGB_565;

	private int mCurrentState = STATE_IDLE;
	private int mTargetState = STATE_IDLE;

	private AuroraPlayer mMediaPlayer = null;

	private int mVideoWidth;
	private int mVideoHeight;
	private int mSurfaceWidth;
	private int mSurfaceHeight;

	private OnCompletionListener mOnCompletionListener;
	private AuroraPlayer.OnPreparedListener mOnPreparedListener;
	private OnErrorListener mOnErrorListener;
	private OnInfoListener mOnInfoListener;
	private int mCurrentBufferPercentage;
	private int mSavedPosition;
	private boolean mCanPause;
	private boolean mCanSeekBack;
	private boolean mCanSeekForward;
	private int mSeekWhenPrepared; // recording the seek position while preparing
	private Context mContext;
	private boolean mInitialized = false;
	private Handler mHandler = null;
	private boolean mIsInListener = false;
	
	public LocalPlayer(Context context, Handler handler){
		mContext = context;
		mHandler = handler;
		mInitialized = false;
	}
	
	public LocalPlayer(){
		mInitialized =false;
	}
	public void setContext(Context context){
		this.mContext =context;
	}

	public void open(Uri uri) {
		mUri = uri;
		openAudio();
	}

	public void release() {
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mCurrentState = STATE_IDLE;
			mTargetState = STATE_IDLE;
		}
		
		mInitialized = false;
	}
	
	public void stop() {
		if (mMediaPlayer != null) {
			mMediaPlayer.reset();
			mCurrentState = STATE_IDLE;
			mTargetState = STATE_IDLE;
		}
		mInitialized = false;
	}

	AuroraPlayer.OnVideoSizeChangedListener mSizeChangedListener = new AuroraPlayer.OnVideoSizeChangedListener() {
		public void onVideoSizeChanged(AuroraPlayer mp, int width, int height) {
			if (width == -1 && height == -1) {
				Log.i(TAG, "reset color format to " + FORMAT_TYPE);
			}
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();
			if (mVideoWidth != 0 && mVideoHeight != 0) {
				Log.d(TAG, "to setFixedSize(" + mVideoWidth + ","
						+ mVideoHeight + ")");
			}
		}
	};

	AuroraPlayer.OnPreparedListener mPreparedListener = new AuroraPlayer.OnPreparedListener() {
		public void onPrepared(AuroraPlayer mp) {
			mCurrentState = STATE_PREPARED;
			Log.i(TAG, "zll ---- AuroraPlayer onPrepared ---");
			mCanPause = mCanSeekBack = mCanSeekForward = true;

			if (mOnPreparedListener != null) {
				mOnPreparedListener.onPrepared(mMediaPlayer);
			}

			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();

			// TODO,FIXME:vseaplyer need start before seek
			// int seekToPosition = mSeekWhenPrepared; // mSeekWhenPrepared may
			// be changed after seekTo() call
			// if (seekToPosition != 0) {
			// seekTo(seekToPosition);
			// }
			
			int seekToPosition = 0;

			if (mVideoWidth != 0 && mVideoHeight != 0) {
				Log.i("@@@@", "video size: " + mVideoWidth + "/" + mVideoHeight);
				if (mSurfaceWidth == mVideoWidth
						&& mSurfaceHeight == mVideoHeight) {
					// We didn't actually change the size (it was already at the
					// size
					// we need), so we won't get a "surface changed" callback,
					// so
					// start the video here instead of in the callback.
					Log.v(TAG, "current target state: " + mTargetState);
					if (mTargetState == STATE_PLAYING) {
						start();
					} else if (!isPlaying()
							&& (seekToPosition != 0 || getCurrentPosition() > 0)) {
					}
				}
			} else {
				// We don't know the video size yet, but should start anyway.
				// The video size might be reported to us later.
				Log.i(TAG, "zll ---- AuroraPlayer onPrepared --- mTargetState:"+mTargetState);
				if (mTargetState == STATE_PLAYING) {
					start();
				}
			}
			
			if (mHandler != null) {
				mHandler.removeMessages(LOCALPLAYER_STATE_PREPARED);
				mHandler.sendEmptyMessage(LOCALPLAYER_STATE_PREPARED);
			}
			
		}
	};

	private AuroraPlayer.OnCompletionListener mCompletionListener = new AuroraPlayer.OnCompletionListener() {
		public void onCompletion(AuroraPlayer mp) {
			mCurrentState = STATE_PLAYBACK_COMPLETED;
			mTargetState = STATE_PLAYBACK_COMPLETED;
			Log.i(TAG, "zll ---- onCompletion ---");
			if (mOnCompletionListener != null) {
				mOnCompletionListener.onCompletion(mMediaPlayer);
			}
			
			if (mHandler != null) {
				mHandler.removeMessages(LOCALPLAYER_STATE_NEXT);
				mHandler.sendEmptyMessage(LOCALPLAYER_STATE_NEXT);
			}
		}
	};

	private AuroraPlayer.OnErrorListener mErrorListener = new AuroraPlayer.OnErrorListener() {
		public boolean onError(AuroraPlayer mp, int framework_err, int impl_err) {
			Log.d(TAG, "zll onError Error: " + framework_err + "," + impl_err);
			
			//modify by chenhl start 20141009
			int errFlag = 0; //1表示没有start ，0表示有start
			if(mTargetState!=STATE_PLAYING){
				errFlag=1;
			}else{
				errFlag=0;
			}
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mInitialized = false;
			
			/* If an error handler has been supplied, use it and finish. */
			if (mOnErrorListener != null) {
				if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
					return true;
				}
			}
			
			if (mHandler != null) {
				mHandler.removeMessages(LOCALPLAYER_STATE_ERROR);
				//mHandler.sendEmptyMessage(LOCALPLAYER_STATE_ERROR);
				mHandler.obtainMessage(LOCALPLAYER_STATE_ERROR, 0, errFlag).sendToTarget();
			}
			//modify by chenhl end 20141009
			return true;
		}
	};

	private AuroraPlayer.OnInfoListener mInfoListener = new AuroraPlayer.OnInfoListener() {
		@Override
		public boolean onInfo(AuroraPlayer mp, int what, int extra) {
			if (mOnInfoListener != null) {
				return mOnInfoListener.onInfo(mp, what, extra);
			}

			switch (what) {
			case AuroraPlayer.INFO_NETWORK_RESUMED:
				break;
			}

			return true;
		}

	};

	private AuroraPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new AuroraPlayer.OnBufferingUpdateListener() {
		public void onBufferingUpdate(AuroraPlayer mp, int percent) {
			mCurrentBufferPercentage = percent;
		}
	};

	private AuroraPlayer.OnLiveEndTimeUpdateListener mLiveEndTimeUpdateListener = new AuroraPlayer.OnLiveEndTimeUpdateListener() {
		public void onLiveEndTimeUpdate(AuroraPlayer mp, int endTime) {
			if (endTime >= 0)
				mDuration = endTime;
		}
	};

	/**
	 * Register a callback to be invoked when the media file is loaded and ready
	 * to go.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnPreparedListener(AuroraPlayer.OnPreparedListener l) {
		mOnPreparedListener = l;
	}

	/**
	 * Register a callback to be invoked when the end of a media file has been
	 * reached during playback.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnCompletionListener(OnCompletionListener l) {
		mOnCompletionListener = l;
	}

	/**
	 * Register a callback to be invoked when an error occurs during playback or
	 * setup. If no listener is specified, or if the listener returned false,
	 * VideoView will inform the user of any errors.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnErrorListener(OnErrorListener l) {
		mOnErrorListener = l;
	}

	public void setOnInfoListener(OnInfoListener l) {
		mOnInfoListener = l;
	}

	/*
	 * release the media player in any state
	 */
	private void releaseEx(boolean cleartargetstate) {
		if (mMediaPlayer != null) {
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mCurrentState = STATE_IDLE;
			if (cleartargetstate) {
				mTargetState = STATE_IDLE;
			}
			
			mInitialized = false;
		}
	}

	private void openAudio() {
		if (mUri == null) {
			// not ready for playback just yet, will try again later
			return;
		}
		// Tell the music playback service to pause
		// TODO: these constants need to be published somewhere in the
		// framework.
		/*Intent i = new Intent("com.android.music.musicservicecommand");
		i.putExtra("command", "pause");*/

		// we shouldn't clear the target state, because somebody might have
		// called start() previously
		releaseEx(false);
		try {
			mMediaPlayer = new AuroraPlayer();
			mMediaPlayer.setOnPreparedListener(mPreparedListener);
			mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
			mDuration = -1;
			mMediaPlayer.setOnCompletionListener(mCompletionListener);
			mMediaPlayer.setOnErrorListener(mErrorListener);
			mMediaPlayer.setOnInfoListener(mInfoListener);
			mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
			mCurrentBufferPercentage = 0;
			mMediaPlayer.setDataSource(mContext.getApplicationContext(), mUri);
			mMediaPlayer.setScreenOnWhilePlaying(true);
			mMediaPlayer.prepareAsync(mSavedPosition);
			// we don't set the target state here either, but preserve the
			// target state that was there before.
			mCurrentState = STATE_PREPARING;
			
			mInitialized = true;
			mIsInListener =true;
		} catch (IOException ex) {
			Log.w(TAG, "zll Unable to open content: " + mUri, ex);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer,
					AuroraPlayer.MEDIA_ERROR_UNKNOWN, 0);
			mInitialized = false;
			return;
		} catch (IllegalArgumentException ex) {
			Log.w(TAG, "zll Unable to open content: " + mUri, ex);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer,
					AuroraPlayer.MEDIA_ERROR_UNKNOWN, 0);
			mInitialized = false;
			return;
		}
	}

	public boolean isInitialized() {
		return mInitialized;
	}

	public void start() {
		//Log.i(TAG, "zll ----- start ---- cccc 0.1 mMediaPlayer:"+mMediaPlayer+",mCurrentState:"+mCurrentState);
		if (isInPlaybackState()) {
			//Log.i(TAG, "zll ----- start ---- cccc 0.2");
			mMediaPlayer.start();
			mCurrentState = STATE_PLAYING;
		}
		mTargetState = STATE_PLAYING;
	}

	public void pause() {
		if (isInPlaybackState()) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
				mCurrentState = STATE_PAUSED;
			}
		}
		mTargetState = STATE_PAUSED;
	}

	public void suspend() {
		releaseEx(false);
	}

	//resume -> reOpen
	public void reOpen() {
		openAudio();
	}

	// cache duration as mDuration for faster access
	public int getDuration() {
		if (isInPlaybackState()) {
			// if (mDuration > 0) {
			// return mDuration;
			// }
			mDuration = mMediaPlayer.getDuration();
			//Log.i(TAG, "zll ---- xxx 1 getDuration :"+mDuration);
			return mDuration;
		}
		mDuration = -1;
		//Log.i(TAG, "zll ---- xxx 2 getDuration :"+mDuration+",mMediaPlayer:"+mMediaPlayer+",mCurrentState:"+mCurrentState);
		return mDuration;
	}

	public int getCurrentPosition() {
		if (isInPlaybackState()) {
//			int time = mMediaPlayer.getCurrentPosition();
			//Log.i(TAG, "zll ---- xxx getCurrentPosition :"+time);
			return mMediaPlayer.getCurrentPosition();
		}
		return 0;
	}

	public void seekTo(int msec) {
		if (isInPlaybackState()) {
			mMediaPlayer.seekTo(msec);
			mSeekWhenPrepared = 0;
		} else {
			mSeekWhenPrepared = msec;
		}
	}

	public boolean isPlaying() {
		return isInPlaybackState() && mMediaPlayer.isPlaying();
	}

	public int getBufferPercentage() {
		if (mMediaPlayer != null) {
			return mCurrentBufferPercentage;
		}
		return 0;
	}

	private boolean isInPlaybackState() {
		return (mMediaPlayer != null && mCurrentState != STATE_ERROR
				&& mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
	}

	public boolean canPause() {
		return mCanPause;
	}

	public boolean canSeekBackward() {
		return mCanSeekBack;
	}

	public boolean canSeekForward() {
		return mCanSeekForward;
	}
	
	public void resetOfOnlinePlay(){
		if (mMediaPlayer != null && mIsInListener) {
			mMediaPlayer.release();
			mMediaPlayer.setOnPreparedListener(null);
			mMediaPlayer.setOnCompletionListener(null);
			mMediaPlayer.setOnErrorListener(null);
			mMediaPlayer=null;
    		mIsInListener = false;
		}
	}
}
