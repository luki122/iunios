/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.app;

import android.annotation.TargetApi;
import android.app.Service;
import aurora.app.AuroraAlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import com.android.gallery3d.R;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.BlobCache;
import com.android.gallery3d.util.CacheManager;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MyLog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.database.ContentObserver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import java.util.List;

import android.media.AudioManager.OnAudioFocusChangeListener;


public class MoviePlayer implements
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
        ControllerOverlay.Listener, MediaPlayer.OnPreparedListener{//paul add<2015-10-19>
    @SuppressWarnings("unused")
    private static final String TAG = "MoviePlayer";

    private static final String KEY_VIDEO_POSITION = "video-position";
    private static final String KEY_RESUMEABLE_TIME = "resumeable-timeout";

    // These are constants in KeyEvent, appearing on API level 11.
    private static final int KEYCODE_MEDIA_PLAY = 126;
    private static final int KEYCODE_MEDIA_PAUSE = 127;

    // Copied from MediaPlaybackService in the Music Player app.
    private static final String SERVICECMD = "com.android.music.musicservicecommand";
    private static final String CMDNAME = "command";
    private static final String CMDPAUSE = "pause";

    private static final long BLACK_TIMEOUT = 500;

    // If we resume the acitivty with in RESUMEABLE_TIMEOUT, we will keep playing.
    // Otherwise, we pause the player.
    private static final long RESUMEABLE_TIMEOUT = 3 * 60 * 1000; // 3 mins

    private Context mContext;
    private final View mRootView;
    private final VideoView mVideoView;
    private final Bookmarker mBookmarker;
    private final Uri mUri;
    private final Handler mHandler = new Handler();
    private final AudioBecomingNoisyReceiver mAudioBecomingNoisyReceiver;
    private final MovieControllerOverlay mController;

    private long mResumeableTime = Long.MAX_VALUE;
    private int mVideoPosition = 0;
    private boolean mHasPaused = false;
    private int mLastSystemUiVis = 0;
    
  //Aurora <SQF> <2014-03-30> for hardware key volume up/down begin 
    private boolean mIsHardwareVolumeKeyPressed = false;
  //Aurora <SQF> <2014-03-30> for hardware key volume up/down end

    // If the time bar is being dragged.
    private boolean mDragging;

    // If the time bar is visible.
    private boolean mShowing;
    
    //Aurora <shihao> <2014-10-10> for pause video when rolling-over the phone begin 
    private SensorEventListener mSensorListener = null;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private boolean mIsOpen = false;
    //Aurora <shihao> <2014-10-10> for pause video when rolling-over the phone end 
    
    private final Runnable mPlayingChecker = new Runnable() {
        @Override
        public void run() {
            if (mVideoView.isPlaying()) {
                mController.showPlaying();
            } else {
                mHandler.postDelayed(mPlayingChecker, 250);
            }
        }
    };

    private final Runnable mProgressChecker = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            mHandler.postDelayed(mProgressChecker, 1000 - (pos % 1000));
        }
    };

//paul add for BUG #17400 start
	OnAudioFocusChangeListener mListener = new  OnAudioFocusChangeListener(){
		@Override
		public void onAudioFocusChange(int flag) {
			// TODO Auto-generated method stub
			Log.d(TAG,"=====onAudioFocusChange : " + flag);
			if(AudioManager.AUDIOFOCUS_LOSS == flag && mVideoView.isPlaying()) {
				pauseVideo();
			}
		}
		
	};


//paul add for BUG #17400 end

    public MoviePlayer(View rootView, final MovieActivity movieActivity,
            Uri videoUri, Bundle savedInstance, boolean canReplay) {
        mContext = movieActivity.getApplicationContext();
        initSensorManager();//Aurora <shihao> <2014-10-10> for pause video when rolling-over the phone 
        mRootView = rootView;
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);//paul fix BUG #17400
        mVideoView = (VideoView) rootView.findViewById(R.id.surface_view);
        mBookmarker = new Bookmarker(movieActivity);
        mUri = videoUri;

        mController = new MovieControllerOverlay(mContext);
        ((ViewGroup)rootView).addView(mController.getView());
        mController.setListener(this);
        mController.setCanReplay(canReplay);

		
        mVideoView.setOnErrorListener(this);
		mVideoView.setOnPreparedListener(this);//paul add<2015-10-19>
        mVideoView.setOnCompletionListener(this);
        mVideoView.setVideoURI(mUri);
        rootView.setOnTouchListener(new View.OnTouchListener() {//paul modify  mVideoView.setOnTouchListener(new View.OnTouchListener
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            	if(MotionEvent.ACTION_DOWN == event.getActionMasked()){//paul modify
                	mController.show();
            	}
                return true;
            }
        });

        // The SurfaceView is transparent before drawing the first frame.
        // This makes the UI flashing when open a video. (black -> old screen
        // -> video) However, we have no way to know the timing of the first
        // frame. So, we hide the VideoView for a while to make sure the
        // video has been drawn on it.
        mVideoView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mVideoView.setVisibility(View.VISIBLE);
            }
        }, BLACK_TIMEOUT);

        setOnSystemUiVisibilityChangeListener();
        // Hide system UI by default
        showSystemUi(false);

        mAudioBecomingNoisyReceiver = new AudioBecomingNoisyReceiver();
        mAudioBecomingNoisyReceiver.register();

        Intent i = new Intent(SERVICECMD);
        i.putExtra(CMDNAME, CMDPAUSE);
        movieActivity.sendBroadcast(i);

        if (savedInstance != null) { // this is a resumed activity
            mVideoPosition = savedInstance.getInt(KEY_VIDEO_POSITION, 0);
            mResumeableTime = savedInstance.getLong(KEY_RESUMEABLE_TIME, Long.MAX_VALUE);
            //wenyongzhe 2016.4.26 BUG #20120 按power键灭亮屏并指纹解锁后，视频自动回到视频开头
        	if (mVideoPosition==0)
			{
				mVideoPosition = mBookmarker.getBookmark(mUri);
			}
        	
            mVideoView.start();
            mVideoView.suspend();
            mHasPaused = true;
        } else {
            final Integer bookmark = mBookmarker.getBookmark(mUri);
            if (bookmark != null) {
                showResumeDialog(movieActivity, bookmark);
            } else {
                startVideo();
            }
        }

		//Aurora <paul> <2015-09-11> add start
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter != null) {
			mBluetoothAdapter.getProfileProxy(mContext.getApplicationContext(), mBluetoothProfileServiceListener,
									BluetoothProfile.HEADSET);
		}
		//Aurora <paul> <2015-09-11> add end

    }

	
	 //Aurora <paul> <2015-09-11> add start
	 private BluetoothHeadset mBluetoothHeadset;
	 protected BluetoothAdapter mBluetoothAdapter;
	 AudioManager mAudioManager;
	 
	 protected BluetoothProfile.ServiceListener mBluetoothProfileServiceListener =
             new BluetoothProfile.ServiceListener() {
         @Override
         public void onServiceConnected(int profile, BluetoothProfile proxy) {
             mBluetoothHeadset = (BluetoothHeadset) proxy;
             Log.d(TAG, "BluetoothHeadset Connected");
         }

         @Override
         public void onServiceDisconnected(int profile) {
             mBluetoothHeadset = null;
			 Log.d(TAG, "BluetoothHeadset Disconnected");
         }
    };
    
    private boolean isBluetoothAvailable() {
		Log.d(TAG,"isBluetoothAvailable");
        return (mBluetoothHeadset != null && mBluetoothHeadset.getConnectedDevices().size() > 0);
    }
	//Aurora <paul> <2015-09-11> add end

	private int mMatchTimes = 0;//paul modify for bug 14480
	private boolean mCurrentNeedPause = false;
	private boolean mLastNeedPause = false;
    //Aurora <shihao> <2014-10-10> for pause video when rolling-over the phone begin
    public void initSensorManager() { 
        //IntentFilter intentFilter = new IntentFilter("cn_somatosensory_smart_pause");
        //mContext.registerReceiver(myReceiver, intentFilter);
        mSensorManager = (SensorManager)mContext.getSystemService(Service.SENSOR_SERVICE); 
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); 
        mSensorListener = new SensorEventListener() { 
            @Override 
            public void onSensorChanged(SensorEvent event) {
            	if(event.sensor == null) return;
				if (event.values[2] < -8) {
					if (++mMatchTimes > 3) {
						mCurrentNeedPause = true;
					}
				} else {
					mCurrentNeedPause = false;
					mMatchTimes = 0;
				}
				if (mCurrentNeedPause != mLastNeedPause) {
		            if(mCurrentNeedPause && mIsOpen && mVideoView.isPlaying()){
						if(!mAudioManager.isWiredHeadsetOn() && !isBluetoothAvailable()){
							pauseVideo();
						}
		            }
					mLastNeedPause = mCurrentNeedPause;
				}
            }   
   
            @Override 
            public void onAccuracyChanged(Sensor sensor, int accuracy) { 
                // TODO Auto-generated method stub    
            } 
        }; 
    }

    
    //paul del <2015-11-09>
    /*
    BroadcastReceiver myReceiver  = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			boolean isOpenSmart = intent.getBooleanExtra("Phyflip", false);
	    	if(sensorListener != null)
	    		mSensorManager.unregisterListener(sensorListener);
			if(isOpenSmart){
	    		mSensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_GAME);
			}
		}
	};
	*/
	
    //Aurora <shihao> <2014-10-10> for pause video when rolling-over the phone end

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setOnSystemUiVisibilityChangeListener() {
        if (!ApiHelper.HAS_VIEW_SYSTEM_UI_FLAG_HIDE_NAVIGATION) return;

        //MyLog.i2("SQF_LOG", "setOnSystemUiVisibilityChangeListener ---- ");
        // When the user touches the screen or uses some hard key, the framework
        // will change system ui visibility from invisible to visible. We show
        // the media control and enable system UI (e.g. ActionBar) to be visible at this point
        mVideoView.setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
            	
                int diff = mLastSystemUiVis ^ visibility;
                mLastSystemUiVis = visibility;
                if ((diff & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0
                        && (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                	////Aurora <SQF> <2014-03-30> for hardware key volume up/down begin 
                	//ORIGINALLY: 
                	//mController.show();
                	//MODIFIED TO:
                	if ( ! mIsHardwareVolumeKeyPressed) {
                		mController.show();
                	} else {
                		mIsHardwareVolumeKeyPressed = false;
                	}
                	////Aurora <SQF> <2014-03-30> for hardware key volume up/down end
                    mRootView.setBackgroundColor(Color.BLACK);
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void showSystemUi(boolean visible) {
        if (!ApiHelper.HAS_VIEW_SYSTEM_UI_FLAG_LAYOUT_STABLE) return;
        int flag = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (!visible) {
            // We used the deprecated "STATUS_BAR_HIDDEN" for unbundling
            flag |= View.STATUS_BAR_HIDDEN | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        mVideoView.setSystemUiVisibility(flag);
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_VIDEO_POSITION, mVideoPosition);
        outState.putLong(KEY_RESUMEABLE_TIME, mResumeableTime);
    }
    
 //wenyongzhe 2016.4.26 BUG #20120 按power键灭亮屏并指纹解锁后，视频自动回到视频开头
    public void onRestoreInstanceState(Bundle savedInstanceState) {
		mVideoPosition = savedInstanceState.getInt(KEY_VIDEO_POSITION, 0);
        mResumeableTime = savedInstanceState.getLong(KEY_RESUMEABLE_TIME, Long.MAX_VALUE);
	}
    
    private void showResumeDialog(Context context, final int bookmark) {
        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(context);
        builder.setTitle(R.string.resume_playing_title);
        builder.setMessage(String.format(
                context.getString(R.string.resume_playing_message),
                GalleryUtils.formatDuration(context, bookmark / 1000)));
        builder.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                onCompletion();
            }
        });
        builder.setPositiveButton(
                R.string.resume_playing_resume, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mVideoView.seekTo(bookmark);
                startVideo();
            }
        });
        builder.setNegativeButton(
                R.string.resume_playing_restart, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startVideo();
            }
        });
        builder.show();
    }

    public void onPause() {
        mHasPaused = true;
        mHandler.removeCallbacksAndMessages(null);
        mVideoPosition = mVideoView.getCurrentPosition();
        mBookmarker.setBookmark(mUri, mVideoPosition, mVideoView.getDuration());
        mVideoView.suspend();
        mResumeableTime = System.currentTimeMillis() + RESUMEABLE_TIMEOUT;
        
    	//Aurora <shihao> <2014-10-10> for pause video when rolling-over the phone begin 
    	if(mIsOpen && (mSensorListener != null)){
			Log.d(TAG,"onPause unregisterListener");
    		mSensorManager.unregisterListener(mSensorListener);
			mIsOpen = false;
    	}
    	//Aurora <shihao> <2014-10-10> for pause video when rolling-over the phone end 
    	
	    mAudioManager.abandonAudioFocus(mListener);//paul add

    }

    public void onResume() {
    	//Aurora <shihao> <2014-10-10> for pause video when rolling-over the phone begin 
    	mIsOpen = (Settings.Secure.getInt(mContext.getContentResolver(), "somatosensory_smart_pause", 0) == 1);
    	if(mIsOpen){
    		mSensorManager.registerListener(mSensorListener, mSensor, SensorManager.SENSOR_DELAY_GAME);
    	}
    	//Aurora <shihao> <2014-10-10> for pause video when rolling-over the phone end 
        if (mHasPaused) {
            mVideoView.seekTo(mVideoPosition);
            mVideoView.resume();

            // If we have slept for too long, pause the play
            //if (System.currentTimeMillis() > mResumeableTime) { paul del, always pause
                pauseVideo();
            //}
        }
        mHandler.post(mProgressChecker);
    }

    public void onDestroy() {
        mVideoView.stopPlayback();
        mAudioBecomingNoisyReceiver.unregister();
        
    	//Aurora <shihao> <2014-10-10> for pause video when rolling-over the phone begin 
    	if(mIsOpen && (mSensorListener != null)){
			Log.d(TAG,"onDestroy unregisterListener");
    		mSensorManager.unregisterListener(mSensorListener);
    	}
    	//mContext.unregisterReceiver(myReceiver);
    	//Aurora <shihao> <2014-10-10> for pause video when rolling-over the phone end 

    	//Aurora <paul> <2015-09-11> start	
		if (mBluetoothHeadset != null) {
            mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);
            mBluetoothHeadset = null;
        }
		//Aurora <paul> <2015-09-11> end
    }

    // This updates the time bar display (if necessary). It is called every
    // second by mProgressChecker and also from places where the time bar needs
    // to be updated immediately.
    private int setProgress() {
        if (mDragging || !mShowing) {
            return 0;
        }
        int position = mVideoView.getCurrentPosition();
        int duration = mVideoView.getDuration();
		//paul add
		if(duration < 0){
			return 0;
		}
        mController.setTimes(position, duration, 0, 0);
        return position;
    }

    private void startVideo() {
        // For streams that we expect to be slow to start up, show a
        // progress spinner until playback starts.
        String scheme = mUri.getScheme();
        if ("http".equalsIgnoreCase(scheme) || "rtsp".equalsIgnoreCase(scheme)) {
            mController.showLoading();
            mHandler.removeCallbacks(mPlayingChecker);
            mHandler.postDelayed(mPlayingChecker, 250);
        } else {
            mController.showPlaying();
            mController.hide();
        }

        mVideoView.start();
        setProgress();
    }

    private void playVideo() {
    	//MyLog.i2("SQF_LOG", "MoviePlayer::playVideo");
        mVideoView.start();
        mController.showPlaying();
        setProgress();

		mAudioManager.requestAudioFocus(mListener , AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);//paul add for BUG #17400
    }

    private void pauseVideo() {
    	//MyLog.i2("SQF_LOG", "MoviePlayer::pauseVideo");
        mVideoView.pause();
        mController.showPaused();
    }

    // Below are notifications from VideoView
    @Override
    public boolean onError(MediaPlayer player, int arg1, int arg2) {
        mHandler.removeCallbacksAndMessages(null);
        // VideoView will show an error dialog if we return false, so no need
        // to show more message.
        mController.showErrorMessage("");
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mController.showEnded();
        onCompletion();
    }
	
	//paul add<2015-10-19> start
	@Override
	public void onPrepared(MediaPlayer mp) {
		/*
		if(!mp.isLooping()){
			mp.setLooping(true);
		}
		*/
		mAudioManager.requestAudioFocus(mListener , AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);//for BUG #17400
	}
	//paul add<2015-10-19> end
	
    public void onCompletion() {
    }

    // Below are notifications from ControllerOverlay
    @Override
    public void onPlayPause() {
        if (mVideoView.isPlaying()) {
            pauseVideo();
        } else {
            playVideo();
        }
    }

    @Override
    public void onSeekStart() {
        mDragging = true;
    }

    @Override
    public void onSeekMove(int time) {
        mVideoView.seekTo(time);
    }

    @Override
    public void onSeekEnd(int time, int start, int end) {
        mDragging = false;
        mVideoView.seekTo(time);
        setProgress();
    }

    @Override
    public void onShown() {
        mShowing = true;
        setProgress();
        showSystemUi(true);
    }

    @Override
    public void onHidden() {
        mShowing = false;
        showSystemUi(false);
    }

    @Override
    public void onReplay() {
        startVideo();
    }

    // Below are key events passed from MovieActivity.
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Some headsets will fire off 7-10 events on a single click
        if (event.getRepeatCount() > 0) {
            return isMediaKey(keyCode);
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_HEADSETHOOK:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (mVideoView.isPlaying()) {
                    pauseVideo();
                } else {
                    playVideo();
                }
                return true;
            case KEYCODE_MEDIA_PAUSE:
                if (mVideoView.isPlaying()) {
                    pauseVideo();
                }
                return true;
            case KEYCODE_MEDIA_PLAY:
                if (!mVideoView.isPlaying()) {
                    playVideo();
                }
                return true;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                // TODO: Handle next / previous accordingly, for now we're
                // just consuming the events.
                return true;
            //Aurora <SQF> <2014-03-30> for hardware key volume up/down begin 
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            	mIsHardwareVolumeKeyPressed = true;
            	return false;
            //Aurora <SQF> <2014-03-30> for hardware key volume up/down end
        }
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	switch(keyCode) {
    		//Aurora <SQF> <2014-03-30> for hardware key volume up/down begin 
    		case KeyEvent.KEYCODE_VOLUME_UP:
    		case KeyEvent.KEYCODE_VOLUME_DOWN:
    			mIsHardwareVolumeKeyPressed = true;
    			return true;
    		//Aurora <SQF> <2014-03-30> for hardware key volume up/down end
    			
    		//Aurora <SQF> <2014-05-09>  for NEW_UI begin
    		case KeyEvent.KEYCODE_MENU:
    			if(mController.isHidden()) {
    				mController.show();
    			} else {
    				mController.HideWhenPlaying();
    			}
    			return true;
             //Aurora <SQF> <2014-05-09>  for NEW_UI end
    	}
        return isMediaKey(keyCode);
    }

    private static boolean isMediaKey(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS
                || keyCode == KeyEvent.KEYCODE_MEDIA_NEXT
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE;
    }

    // We want to pause when the headset is unplugged.
    private class AudioBecomingNoisyReceiver extends BroadcastReceiver {

        public void register() {
            mContext.registerReceiver(this,
                    new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        }

        public void unregister() {
            mContext.unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mVideoView.isPlaying()) pauseVideo();
        }
    }
}

class Bookmarker {
    private static final String TAG = "Bookmarker";

    private static final String BOOKMARK_CACHE_FILE = "bookmark";
    private static final int BOOKMARK_CACHE_MAX_ENTRIES = 100;
    private static final int BOOKMARK_CACHE_MAX_BYTES = 10 * 1024;
    private static final int BOOKMARK_CACHE_VERSION = 1;

    private static final int HALF_MINUTE = 30 * 1000;
    private static final int TWO_MINUTES = 4 * HALF_MINUTE;

    private final Context mContext;

    public Bookmarker(Context context) {
        mContext = context;
    }

    public void setBookmark(Uri uri, int bookmark, int duration) {
    	MyLog.i2("SQF_LOG", "setBookmark --> uri:" + uri);
        try {
            BlobCache cache = CacheManager.getCache(mContext,
                    BOOKMARK_CACHE_FILE, BOOKMARK_CACHE_MAX_ENTRIES,
                    BOOKMARK_CACHE_MAX_BYTES, BOOKMARK_CACHE_VERSION);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeUTF(uri.toString());
            dos.writeInt(bookmark);
            dos.writeInt(duration);
            dos.flush();
            cache.insert(uri.hashCode(), bos.toByteArray());
        } catch (Throwable t) {
            Log.w(TAG, "setBookmark failed", t);
        }
    }

    public Integer getBookmark(Uri uri) {
        try {
            BlobCache cache = CacheManager.getCache(mContext,
                    BOOKMARK_CACHE_FILE, BOOKMARK_CACHE_MAX_ENTRIES,
                    BOOKMARK_CACHE_MAX_BYTES, BOOKMARK_CACHE_VERSION);

            byte[] data = cache.lookup(uri.hashCode());
            if (data == null) return null;

            DataInputStream dis = new DataInputStream(
                    new ByteArrayInputStream(data));

            String uriString = DataInputStream.readUTF(dis);
            int bookmark = dis.readInt();
            int duration = dis.readInt();

            if (!uriString.equals(uri.toString())) {
                return null;
            }

            if ((bookmark < HALF_MINUTE) || (duration < TWO_MINUTES)
                    || (bookmark > (duration - HALF_MINUTE))) {
                return null;
            }
            return Integer.valueOf(bookmark);
        } catch (Throwable t) {
            Log.w(TAG, "getBookmark failed", t);
        }
        return null;
    }
}
