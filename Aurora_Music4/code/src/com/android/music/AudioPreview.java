/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.music;

import android.R.string;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import aurora.app.AuroraActivity;

import com.android.auroramusic.util.Globals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Dialog that comes up in response to various music-related VIEW intents.
 */
public class AudioPreview extends AuroraActivity implements OnPreparedListener, OnErrorListener, OnCompletionListener
{
    private final static String TAG = "AudioPreview";
    private PreviewPlayer mPlayer;
    private TextView mTextLine1;
    private TextView mTextLine2;
    private TextView mLoadingText;
    private SeekBar mSeekBar;
    private Handler mProgressRefresher;
    private boolean mSeeking = false;
    private int mDuration;
    private Uri mUri;
    private long mMediaId = -1;
    private static final int OPEN_IN_MUSIC = 1;
    private AudioManager mAudioManager;
    private boolean mPausedByTransientLossOfFocus;
    
    protected final Paint mPaint = new Paint();
    
    private ImageButton m_bplay;
    private ImageButton m_bpause;
    private boolean m_bComplete = false;
    
    //Iuni <lory><2014-02-10> add begin
    private final int MSG_HEADER_OUT = 101;
    private boolean NEW_AURORAUI = true;
    private static final String AURORA_DEFAULT_NUMBER_FONT_PATH = "system/fonts/Roboto-Light.ttf";
	private Typeface m_auroraNumberTf;
	private boolean bPrepared = false;
	private boolean bSilentMode = false;
	private TelephonyManager mTelephonyManager;//add by chenhl 20140826

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        mUri = intent.getData();
        if (mUri == null) {
            finish();
            return;
        }
        String scheme = mUri.getScheme();
        
        try {
			m_auroraNumberTf = Typeface.createFromFile(AURORA_DEFAULT_NUMBER_FONT_PATH);
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.audiopreview);

        //Iuni <lory><2014-01-22> add begin
        m_bplay = (ImageButton) findViewById(R.id.bt_play);
        m_bpause = (ImageButton) findViewById(R.id.bt_pause);
        
        
        IntentFilter filter = new IntentFilter();
//        filter.addAction(Intent.ACTION_HEADSET_PLUG); //delete by chenhl 20140821
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);//add by chenhl 20140821
        registerReceiver(HeaderSetListener, filter);
        bPrepared = false;
        bSilentMode = false;
        //Iuni <lory><2014-01-22> add end        
        mTextLine1 = (TextView) findViewById(R.id.line1);
        mTextLine2 = (TextView) findViewById(R.id.line2);
        m_bComplete = false;
                
        mLoadingText = (TextView) findViewById(R.id.loading);
        if (scheme.equals("http")) {
            String msg = getString(R.string.streamloadingtext, mUri.getHost());
            mLoadingText.setText(msg);
            mLoadingText.setVisibility(View.VISIBLE);//Iuni <lory><2014-02-27> add begin
        } else {
            mLoadingText.setVisibility(View.GONE);
        }
        mSeekBar = (SeekBar) findViewById(R.id.progress);
        mProgressRefresher = new Handler();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        PreviewPlayer player = (PreviewPlayer) getLastNonConfigurationInstance();
        if (player == null) {
        	if (mPlayer != null) {
        		mPlayer.release();
        		mPlayer = null;
			}
        	
            mPlayer = new PreviewPlayer();
            mPlayer.setActivity(this);
            try {
                mPlayer.setDataSourceAndPrepare(mUri);
            } catch (Exception ex) {
                // catch generic Exception, since we may be called with a media
                // content URI, another content provider's URI, a file URI,
                // an http URI, and there are different exceptions associated
                // with failure to open each of those.
                Log.i(TAG, "zll Failed to open file: " + ex);
                Toast.makeText(this, R.string.playback_failed, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            mPlayer = player;
            mPlayer.setActivity(this);
            if (mPlayer.isPrepared()) {
                showPostPrepareUI();
            }
        }

        AsyncQueryHandler mAsyncQueryHandler = new AsyncQueryHandler(getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                if (cursor != null && cursor.moveToFirst()) {

                    int titleIdx = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                    int artistIdx = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                    int idIdx = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                    int displaynameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                    if (idIdx >=0) {
                        mMediaId = cursor.getLong(idIdx);
                    }
                    
                    if (titleIdx >= 0) {
                        String title = cursor.getString(titleIdx);
                        if (title != null) {
                        	//Iuni <lory><2014-02-10> add begin
                        	String tstr3 = getLimitLengthString(title);
                            if (tstr3.matches("^[a-z0-9A-Z.]*")) {
                            	mTextLine1.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
                                mTextLine1.getPaint().setStrokeWidth((float)2.2);
                                
                                try {
                                	mTextLine1.setTypeface(m_auroraNumberTf);
                    			} catch (Exception e) {
                    				e.printStackTrace();
                    			}
                                
    						} else {
    							mTextLine1.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
    		                    mTextLine1.getPaint().setStrokeWidth((float)1.0);
    						}
                            //Iuni <lory><2014-02-10> add end
                            mTextLine1.setText(tstr3);
						}
                        
                        if (artistIdx >= 0) {
                            String artist = cursor.getString(artistIdx);
                            if (artist != null) {
                            	//Iuni <lory><2014-02-10> add begin
                            	String tstr2 = artist;//artist.replaceAll("\\s*", "");
                                if (tstr2.matches("^[<a-z0-9A-Z.>]*")) {
                                    try {
                                    	mTextLine2.setTypeface(m_auroraNumberTf);
                        			} catch (Exception e) {
                        				e.printStackTrace();
                        			}
                                    
                                    /*float text_width = mTextLine1.getPaint().measureText(tstr2);
                                    float width = text_width/tstr2.length();
                                    Log.i(TAG, "zll ---- 2 text_width:"+text_width+",width:"+width);*/
        						} 
                                //Iuni <lory><2014-02-10> add end
                                
                                mTextLine2.setText(artist);
							}
                            
                        }
                    } else if (displaynameIdx >= 0) {
                        String name = cursor.getString(displaynameIdx);
                        if (name != null) {
                        	String tstr1 = getLimitLengthString(name);
                            //Iuni <lory><2014-02-10> add begin
                            if (tstr1.matches("^[a-z0-9A-Z.]*")) {
                            	mTextLine1.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
                                mTextLine1.getPaint().setStrokeWidth((float)2.2);
                                
                                try {
                                	mTextLine1.setTypeface(m_auroraNumberTf);
                    			} catch (Exception e) {
                    				e.printStackTrace();
                    			}
                                
    						} else {
    							mTextLine1.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
    		                    mTextLine1.getPaint().setStrokeWidth((float)1.0);
    						}
                            //Iuni <lory><2014-02-10> add end
                            mTextLine1.setText(tstr1);
						}
                        
                    } else {
                        // Couldn't find anything to display, what to do now?
                        Log.w(TAG, "Cursor had no names for us");
                    }
                } else {
                    Log.w(TAG, "empty cursor");
                }

                if (cursor != null) {
                    cursor.close();
                }
                setNames();
            }
        };

        if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            if (mUri.getAuthority() == MediaStore.AUTHORITY) {
                // try to get title and artist from the media content provider
                mAsyncQueryHandler.startQuery(0, null, mUri, new String [] {
                        MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST},
                        null, null, null);
            } else {
                // Try to get the display name from another content provider.
                // Don't specifically ask for the display name though, since the
                // provider might not actually support that column.
                mAsyncQueryHandler.startQuery(0, null, mUri, null, null, null, null);
            }
        } else if (scheme.equals("file")) {
            // check if this file is in the media database (clicking on a download
            // in the download manager might follow this path
            String path = mUri.getPath();
            mAsyncQueryHandler.startQuery(0, null,  MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String [] {MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST},
                    MediaStore.Audio.Media.DATA + "=?", new String [] {path}, null);
        } else {
            // We can't get metadata from the file/stream itself yet, because
            // that API is hidden, so instead we display the URI being played
            if (mPlayer.isPrepared()) {
                setNames();
            }
        }
        //add by chenhl start 20140826 
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		
        //add by chenhl end 20140826
    }
    
    //Iuni <lory><2014-03-12> add begin
    Handler mReceiverHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_HEADER_OUT:
				ReceiveplayPause();
				break;
			}
		}
    	
    };
    
    //Iuni <lory><2014-03-12> add begin
    private final BroadcastReceiver HeaderSetListener = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
				if (intent.getIntExtra("state", 0) == 0 && bPrepared) {//out
					//if (!mAudioManager.isWiredHeadsetOn()) 
					{
						mReceiverHandler.removeMessages(MSG_HEADER_OUT);
						mReceiverHandler.sendMessage(mReceiverHandler.obtainMessage(MSG_HEADER_OUT));
					}
					
				}
			}
			//add by chenhl start 20140821
			else if(action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)){
				
					mReceiverHandler.removeMessages(MSG_HEADER_OUT);
					mReceiverHandler.sendMessage(mReceiverHandler.obtainMessage(MSG_HEADER_OUT));
				
			}
			//add by chenhl end 20140821
			
		}
	};
	
	private void ReceiveplayPause (){
		if (mPlayer == null) {
            return;
        }
		
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            updatePlayPause();
        } 
        
		return;
	}
	
	private String getLimitLengthString(String str) {
		int len = 30;
		byte b[];
		String result = "";
		try {
			b = str.getBytes("GBK");
			if (b.length < len)
				return str;
			else {
				if (str.length() == b.length) {
					result = auroraSubString(str, 12, 12);
				} else {
					result = auroraSubString(str, 12, 8);
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private String auroraSubString(String text, int left, int right)
			throws UnsupportedEncodingException {
		if (text == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		int currentLength = 0;

		char[] chars = text.toCharArray();

		for (int i = 0; i < left; i++) {
			char c = chars[i];
			currentLength += String.valueOf(c).getBytes("GBK").length;
			if (currentLength >= left)
				break;
			sb.append(c);
		}
		sb.append("...");
		currentLength = 0;
		for (int i = chars.length - right; i < chars.length; i++) {
			char c = chars[i];
			// currentLength += String.valueOf(c).getBytes("GBK").length;
			// if(currentLength>=12)break;
			sb.append(c);
		}
		return sb.toString();

	}
	//Iuni <lory><2014-03-12> add end

    @Override
    public Object onRetainNonConfigurationInstance() {
        PreviewPlayer player = mPlayer;
        mPlayer = null;
        return player;
    }
    
	@Override
	protected void onPause() {
		super.onPause();
		//delete by chenhl 20140821
/*		if (isInSilentMode()) {
			bSilentMode = true;
			PauseClicked(null);
		}*/
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//delete by chenhl 20140821
/*		if (bSilentMode) {
			bSilentMode = false;
			PauseClicked(null);
		}*/
	}

	private boolean isInSilentMode() {
		return mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL;
	}  

	@Override
    public void onDestroy() {
		mReceiverHandler.removeMessages(MSG_HEADER_OUT);
    	unregisterReceiver(HeaderSetListener);
    	if(mTelephonyManager!=null)
            mTelephonyManager.listen(mPhoneStateListener, 0);//add by chenhl 20140826
        stopPlayback();
        super.onDestroy();
    }

    private void stopPlayback() {
    	
        if (mProgressRefresher != null) {
            mProgressRefresher.removeCallbacksAndMessages(null);
        }
        
        if (mPlayer != null) {
        	mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
            mAudioManager.abandonAudioFocus(mAudioFocusListener);
        }
    }

    @Override
    public void onUserLeaveHint() {
        stopPlayback();
        finish();
        super.onUserLeaveHint();
    }

    public void onPrepared(MediaPlayer mp) {
        if (isFinishing()) return;
        mPlayer = (PreviewPlayer) mp;
        setNames();
        mPlayer.start();
        
        bPrepared = true;//Iuni <lory><2014-03-25> add begin
        showPostPrepareUI();
    }

    private void showPostPrepareUI() {
    	//Iuni <lory><2014-02-27> add begin
        //ProgressBar pb = (ProgressBar) findViewById(R.id.spinner);
        //pb.setVisibility(View.GONE);
        //Iuni <lory><2014-02-27> add end
        mDuration = mPlayer.getDuration();
        if (mDuration != 0) {
            mSeekBar.setMax(mDuration);
            mSeekBar.setVisibility(View.VISIBLE);
        } else if (mDuration == 0) {
        	mSeekBar.setMax(0);
            mSeekBar.setVisibility(View.VISIBLE);
		}
        mSeekBar.setOnSeekBarChangeListener(mSeekListener);
        mLoadingText.setVisibility(View.GONE);
        View v = findViewById(R.id.titleandbuttons);
        v.setVisibility(View.VISIBLE);
        mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        mProgressRefresher.postDelayed(new ProgressRefresher(), 200);
        updatePlayPause();
    }
    
    private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (mPlayer == null) {
                // this activity has handed its MediaPlayer off to the next activity
                // (e.g. portrait/landscape switch) and should abandon its focus
                mAudioManager.abandonAudioFocus(this);
                return;
            }
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    mPausedByTransientLossOfFocus = false;
                    if(mPlayer.isPlaying()){
                    	mPlayer.pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if (mPlayer.isPlaying()) {
                        mPausedByTransientLossOfFocus = true;
                        mPlayer.pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (mPausedByTransientLossOfFocus) {
                        mPausedByTransientLossOfFocus = false;
                        start();
                    }
                    break;
            }
            updatePlayPause();
        }
    };
    
    private void start() {
        mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        if (m_bComplete && mPlayer != null) {

        	if(mSeekBar.getProgress()==mSeekBar.getMax()){
        		mPlayer.seekTo(0);
        	}else {
        		mPlayer.seekTo(mSeekBar.getProgress());
			}
        	m_bComplete = false;        	
		}
        mPlayer.start();
        bPrepared = true;
        mProgressRefresher.postDelayed(new ProgressRefresher(), 200);
    }
    
    public void setNames() {
        if (TextUtils.isEmpty(mTextLine1.getText())) {
        	String tmpname = mUri.getLastPathSegment();
        	if (tmpname != null) {
        		String tstr = getLimitLengthString(tmpname);
            	//Iuni <lory><2014-02-10> add begin
                if (tstr.matches("^[a-z0-9A-Z.]*")) {
                	mTextLine1.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
                    mTextLine1.getPaint().setStrokeWidth((float)2.2);
                    
                    try {
                    	mTextLine1.setTypeface(m_auroraNumberTf);
        			} catch (Exception e) {
        				e.printStackTrace();
        			}
    			} else {
    				mTextLine1.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
                    mTextLine1.getPaint().setStrokeWidth((float)1.0);
    			}
                //Iuni <lory><2014-02-10> add end
                
                mTextLine1.setText(tstr);
			}
        }
        if (TextUtils.isEmpty(mTextLine2.getText())) {
            mTextLine2.setVisibility(View.GONE);
        } else {
            mTextLine2.setVisibility(View.VISIBLE);
        }
    }

    class ProgressRefresher implements Runnable {

        public void run() {
            if (mPlayer != null && !mSeeking && mDuration != 0) {
                int progress = mPlayer.getCurrentPosition() / mDuration;
                mSeekBar.setProgress(mPlayer.getCurrentPosition());
            }
            
            mProgressRefresher.removeCallbacksAndMessages(null);
            mProgressRefresher.postDelayed(new ProgressRefresher(), 200);
        }
    }
    
    private void updatePlayPause() {
    	if (mPlayer == null) {
			return;
		}

		if ((m_bplay != null) && 
    		(m_bpause != null)) {
			if (mPlayer.isPlaying()) {
				m_bplay.setVisibility(View.GONE);
				m_bpause.setVisibility(View.VISIBLE);
			} else {
				m_bpause.setVisibility(View.GONE);
				m_bplay.setVisibility(View.VISIBLE);
			}
		}

    }

    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            mSeeking = true;
        }
        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                return;
            }
            // Protection for case of simultaneously tapping on seek bar and exit
            if (mPlayer == null) {
                return;
            }
            mPlayer.seekTo(progress);
        }
        public void onStopTrackingTouch(SeekBar bar) {
            mSeeking = false;
        }
    };

    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(this, R.string.playback_failed, Toast.LENGTH_SHORT).show();
        finish();
        return true;
    }

    public void onCompletion(MediaPlayer mp) {
    	mProgressRefresher.removeCallbacksAndMessages(null);

        mSeekBar.setProgress(mDuration);
        //updatePlayPause();//lory del 2014.4.1
    	
		if ((m_bplay != null) && 
    		(m_bpause != null)) {
			m_bpause.setVisibility(View.GONE);
			m_bplay.setVisibility(View.VISIBLE);
		}
		m_bComplete = true;
    }

    public void playPauseClicked(View v) {
        // Protection for case of simultaneously tapping on play/pause and exit
        if (mPlayer == null) {
            return;
        }
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } 
        else {
            start();
        }
        
        updatePlayPause();
    }
    
    public void PauseClicked(View v) {
        // Protection for case of simultaneously tapping on play/pause and exit
        if (mPlayer == null) {
            return;
        }
        if (mPlayer.isPlaying()) {
        	mPlayer.pause();
        } else {
            start();
        }
        
        updatePlayPause();
    }
    
    //Iuni <lory><2014-03-12> del begin
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.ateOptionsMenu(menu);
        // TODO: if mMediaId != -1, then the playing file has an entry in the media
        // database, and we could open it in the full music app instead.
        // Ideally, we would hand off the currently running mediaplayer
        // to the music UI, which can probably be done via a public static
        //menu.add(0, OPEN_IN_MUSIC, 0, "open in music");
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(OPEN_IN_MUSIC);
        if (mMediaId >= 0) {
            item.setVisible(true);
            return true;
        }
        item.setVisible(false);
        return false;
    }*/
    //Iuni <lory><2014-03-12> del end
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_HEADSETHOOK:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                } else {
                    start();
                }
                
                updatePlayPause();
                return true;
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                start();
                updatePlayPause();
                return true;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                }
                updatePlayPause();
                return true;
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
            case KeyEvent.KEYCODE_MEDIA_NEXT:
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                return true;
            case KeyEvent.KEYCODE_MEDIA_STOP:
            case KeyEvent.KEYCODE_BACK:
                stopPlayback();
                finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /*
     * Wrapper class to help with handing off the MediaPlayer to the next instance
     * of the activity in case of orientation change, without losing any state.
     */
    private static class PreviewPlayer extends MediaPlayer implements OnPreparedListener {
        AudioPreview mActivity;
        boolean mIsPrepared = false;

        public void setActivity(AudioPreview activity) {
            mActivity = activity;
            setOnPreparedListener(this);
            setOnErrorListener(mActivity);
            setOnCompletionListener(mActivity);
        }

        public void setDataSourceAndPrepare(Uri uri) throws IllegalArgumentException,
                        SecurityException, IllegalStateException, IOException {
            setDataSource(mActivity,uri);
            prepareAsync();
        }

        /* (non-Javadoc)
         * @see android.media.MediaPlayer.OnPreparedListener#onPrepared(android.media.MediaPlayer)
         */
        @Override
        public void onPrepared(MediaPlayer mp) {
            mIsPrepared = true;
            mActivity.onPrepared(mp);
        }

        boolean isPrepared() {
            return mIsPrepared;
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			return true;
		case MotionEvent.ACTION_MOVE:
			return true;
		case MotionEvent.ACTION_UP:
			finish();
			break;
		default:
			break;
		}
    	return super.onTouchEvent(event);
    }
    
  //add by chenhl start 20140624
  	private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {

  		@Override
  		public void onCallStateChanged(int state, String incomingNumber) {
  			if(mPlayer==null){
  				return;
  			}
  			switch (state) {
  			case TelephonyManager.CALL_STATE_RINGING:

  				mPausedByTransientLossOfFocus = (mPlayer.isPlaying() || mPausedByTransientLossOfFocus);
  				if(mPlayer.isPlaying()){
  					mPlayer.pause();
  				}

  				break;
  			case TelephonyManager.CALL_STATE_IDLE:
  				if (mPausedByTransientLossOfFocus&&!mPlayer.isPlaying()) {
  					start();
  					mPausedByTransientLossOfFocus=false;
  				}
  				break;
  			case TelephonyManager.CALL_STATE_OFFHOOK:
  				mPausedByTransientLossOfFocus = (mPlayer.isPlaying() || mPausedByTransientLossOfFocus);
  				if(mPlayer.isPlaying()){
  					mPlayer.pause();
  				}
  				break;
  			}
  		}

  	};
      //add by chenhl end 20140624
}