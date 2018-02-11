package com.aurora.audioprofile;


import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.app.Dialog;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.provider.Settings.System;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.android.settings.R;

import aurora.preference.*;


public class AuroraVolumePreference extends AuroraPreference{

    private static final String TAG = "AurorVolumePreference";
//    public 	static SeekBarVolumizerEx flagSeekBarVolumizer = null; 
    public SeekBarVolumizerEx flagSeekBarVolumizer = null; 
    private int mStreamType;
    private SeekBarVolumizerEx mSeekBarVolumizer = null;
    private Context mContext;
    private Uri mMediaUri =null;    
    private SeekBar mSeekBar = null;
    private ImageView mTitleImage = null;
    private int mSaveMusicVolume = 0; 	//update the volume of MUsic
    private int mSaveRingVolume = -1;  // save the volume of ring
 
	 public AuroraVolumePreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mContext = context;
		setLayoutResource(R.layout.aurora_seekbar_volume);
		
	}

	public AuroraVolumePreference(Context context, AttributeSet attrs) {

		this(context, attrs, 0);
	}

	public AuroraVolumePreference(Context context) {
		
		this(context, null);
	}

	public void setSavedMusicVolue(int savedMusiVolume){
		mSaveMusicVolume = savedMusiVolume;
	}
	
	public void setStreamType(int streamType) {
        mStreamType = streamType;
    }
	
	public void setMediaUri(Uri uri){
		mMediaUri = uri;
	}
	
	
    protected void onBindView(View view) {
        super.onBindView(view);
        Log.i(TAG, "AuroraVolumePreference  onBindView  ");
        mSeekBar = (SeekBar) view.findViewById(R.id.volume_seekbar);
        mTitleImage  = (ImageView) view.findViewById(R.id.image);
        Log.i(TAG, "onBindView  mSeekBarVolumizer =   " + mSeekBarVolumizer);
        Log.i(TAG, "onBindView  mMediaUri =   " + mMediaUri);
        if(mSeekBarVolumizer == null){

        	if(mMediaUri != null){
        		mSeekBarVolumizer = new SeekBarVolumizerEx(mContext, mSeekBar, mStreamType, mMediaUri);
        	}else{
        		mSeekBarVolumizer = new SeekBarVolumizerEx(mContext, mSeekBar, mStreamType);
        	}
        	
        }else{
        	mSeekBarVolumizer.setSeekBar(mSeekBar);
        }
        switch(mStreamType){
        case AudioManager.STREAM_RING:
            mTitleImage.setImageResource(R.drawable.ic_audio_ring_notif);
        	break;
        case AudioManager.STREAM_ALARM:
            mTitleImage.setImageResource(R.drawable.ic_audio_alarm);
        	break;
        case AudioManager.STREAM_MUSIC:
            mTitleImage.setImageResource(R.drawable.ic_audio_vol);
        	break;
        }
        
       
    }
    //刷新最新进度
    public void refresh(AudioManager mAudioManager){
    	if(mSeekBar == null){
    		return;
    	}
    	if(mAudioManager.getRingerModeInternal() == AudioManager.RINGER_MODE_SILENT || mAudioManager.getRingerModeInternal() == AudioManager.RINGER_MODE_VIBRATE){
    		mSeekBar.setProgress(0);
    	}else{
    		int mOriginalStreamVolume = mAudioManager.getStreamVolume(mStreamType);
    		Log.i(TAG, "initSeekBar  -1/10  =  "  +  (-1/10));
    		int progress = getPersistedInt(-1);
    		if(progress != -1){
        		if(progress % 10 == 0){
        			progress = progress/10;
        		}else {
        			progress = progress/10 + 1;
        		}
    		}
    		if(progress != mOriginalStreamVolume){
    			mSeekBar.setProgress(mOriginalStreamVolume * 10);
    		}else{
    			mSeekBar.setProgress(getPersistedInt(mOriginalStreamVolume * 10));
    		}
    	}    
    }
    public void stopRingTone(){
    	if(mSeekBarVolumizer != null){
    		mSeekBarVolumizer.stopSample();
    		mSeekBarVolumizer.mIsStartCount = false;
    	}
    }
    
    public void stopRingRes(){
    	if(mSeekBarVolumizer != null){
    		mSeekBarVolumizer.stop();
    	}
    }
    
    public void startRingTone(){
    	if(mSeekBarVolumizer != null){
    		mSeekBarVolumizer.startSample();
    	}
    }
	
    public void setVolumeProgress(int progress){
    	if(mSeekBar != null){
    		Log.i(TAG, "setVolumeProgress progress  =  " + progress);
    	mSeekBar.setProgress(progress); 
    	}
    }
    
    
    public void setSeekBarEnable(boolean isEnable){
    	mSeekBar.setEnabled(isEnable);
    }
    
    public void setTitleEnable(boolean isEnable){
    	
    }
    
    public int getSavedVolume(int defaut){
    	if(mSaveRingVolume != -1){
    		return mSaveRingVolume;
    	}
    	return getPersistedInt(defaut); // 0 2015 05 08
    }

	protected void onSampleStarting(SeekBarVolumizerEx volumizer) {
        if (flagSeekBarVolumizer != null && volumizer != flagSeekBarVolumizer) {
        	flagSeekBarVolumizer.stopSample();
        }
    }
	
	// save instance state
	
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        if (mSeekBarVolumizer != null) {
            mSeekBarVolumizer.onSaveInstanceState(myState.getVolumeStore());
        }
        return myState;
    }

	    
	    protected void onRestoreInstanceState(Parcelable state) {
	        if (state == null || !state.getClass().equals(SavedState.class)) {
	            // Didn't save state for us in onSaveInstanceState
	            super.onRestoreInstanceState(state);
	            return;
	        }

	        SavedState myState = (SavedState) state;
	        super.onRestoreInstanceState(myState.getSuperState());
	        if (mSeekBarVolumizer != null) {
	            mSeekBarVolumizer.onRestoreInstanceState(myState.getVolumeStore());
	        }
	    }

	    public static class VolumeStore {
	    	// -1 modif 0
	        public int volume = 0;
	        public int originalVolume = 0;
	    }

	    private static class SavedState extends BaseSavedState {
	        VolumeStore mVolumeStore = new VolumeStore();

	        public SavedState(Parcel source) {
	            super(source);
	            mVolumeStore.volume = source.readInt();
	            mVolumeStore.originalVolume = source.readInt();
	        }

	     
	        public void writeToParcel(Parcel dest, int flags) {
	            super.writeToParcel(dest, flags);
	            dest.writeInt(mVolumeStore.volume);
	            dest.writeInt(mVolumeStore.originalVolume);
	        }

	        VolumeStore getVolumeStore() {
	            return mVolumeStore;
	        }

	        public SavedState(Parcelable superState) {
	            super(superState);
	        }

	        public static final Parcelable.Creator<SavedState> CREATOR =
	                new Parcelable.Creator<SavedState>() {
	            public SavedState createFromParcel(Parcel in) {
	                return new SavedState(in);
	            }

	            public SavedState[] newArray(int size) {
	                return new SavedState[size];
	            }
	        };
	    }

	//penggangding add begin
	public void setSaveCurrentVolume()
	{
		if(mSeekBarVolumizer!=null)
		{
			if(mSeekBarVolumizer.getSeekBar()!=null)
			{
				int progress=mSeekBarVolumizer.getSeekBar().getProgress();
				if(mSaveRingVolume == progress) return;
				mSaveRingVolume = progress; //save the current value
				mSeekBarVolumizer.setPersistInt(progress);
				Log.d("gd","  SaveCurrentVolume = "+progress);
			}
		}
	}
    // penggangding add end
	    /**
	     * Turns a {@link SeekBar} into a volume control.
	     */
	    public class SeekBarVolumizerEx implements OnSeekBarChangeListener, Runnable {

	        private Context mContext;
	        private Handler mHandler = new Handler();

	        private AudioManager mAudioManager;
	        private int mStreamType;
	        private int mOriginalStreamVolume ;
	        private Ringtone mRingtone;

	        private int mLastProgress  ;
	        private SeekBar mSeekBar;
	        private int mVolumeBeforeMute = -1;
	        private Uri mUri =null;
	        //private  int mVolumeInitState = -1;
	        //private  int mVolumeFinalState = -1;
	        private int mMaxVolume ;
	        
	        private int mCount;
	 	   	public boolean mIsStartCount = false;
	 	   	private boolean mIsRuningThread = true;
	 	   	private int mRingMode;
	 	   private int mVibrateWhenRing;
	        

	        private ContentObserver mVolumeObserver = new ContentObserver(mHandler) {
	            @Override
	            public void onChange(boolean selfChange) {
	                super.onChange(selfChange);
	                
	                Log.i(TAG, "ContentObserver  onChange ");
	                if (mSeekBar != null && mAudioManager != null) {
	                    int volume = mAudioManager.getStreamVolume(mStreamType);
	                    mSeekBar.setProgress(volume * 10);
	                }
	            }
	        };

	        public SeekBarVolumizerEx(Context context, SeekBar seekBar, int streamType) {
	            this(context, seekBar, streamType, null);
	        }

	        public SeekBarVolumizerEx(Context context, SeekBar seekBar, int streamType, Uri defaultUri) {
	            mContext = context;
	            mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	            mStreamType = streamType;
	            mSeekBar = seekBar;
	            mUri = defaultUri;
	           
	            initSeekBar(seekBar, defaultUri);
	            
	            // thread 
	            mIsRuningThread = true;
            	Thread th = new Thread(new Runnable(){

       			@Override
       			public void run() {
       				// TODO Auto-generated method stub	       			
	 	       		while(mIsRuningThread){
	 	       			
	 	       			try {
			 	   			Thread.sleep(1000);					 	   			
			 	   		} catch (InterruptedException e) {
			 	   			// TODO Auto-generated catch block
			 	   			e.printStackTrace();
			 	   		}
		 	       		if(mIsStartCount){
		 	       			mCount++;
			 	       		if(mCount > 3){
			 	       			mCount = 0;
			 	       		}
			 	       		if(mCount == 3){
			 	       			stopSample();
			 	       			mIsStartCount = false;
			 	       		}
	 	       			}
		 	       		
	 	       		}
       			
       		
   				}
       			
            	});
            	th.start();
	        }
	        
	        private void initSeekBar(SeekBar seekBar, Uri defaultUri) {
	        	mMaxVolume = mAudioManager.getStreamMaxVolume(mStreamType);
	        	Log.i(TAG, "mMaxVolume  =  "  + mMaxVolume);
	        	mMaxVolume = mMaxVolume * 10;
	            seekBar.setMax(mMaxVolume);
	            mOriginalStreamVolume = mAudioManager.getStreamVolume(mStreamType);
	            	        	
	        	if(mOriginalStreamVolume >0){
	        		mVibrateWhenRing = Settings.System.getInt(mContext.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,1 );
	        	}
	        	Log.i(TAG, "initSeekBar  mOriginalStreamVolume  =  "  + mOriginalStreamVolume);
	        	Log.i(TAG, "initSeekBar  RingMode  =  "  + mAudioManager.getRingerModeInternal());
	        	Log.i(TAG, "initSeekBar  getPersistedInt  =  "  + getPersistedInt(-1));
	        	if(mAudioManager.getRingerModeInternal() == AudioManager.RINGER_MODE_SILENT || mAudioManager.getRingerModeInternal() == AudioManager.RINGER_MODE_VIBRATE){
	        		 seekBar.setProgress(0);
	        	}else{
	        		Log.i(TAG, "initSeekBar  -1/10  =  "  +  (-1/10));
	        		int progress = getPersistedInt(-1);
	        		if(progress != -1){
		        		if(progress % 10 == 0){
		        			progress = progress/10;
		        		}else {
		        			progress = progress/10 + 1;
		        		}
	        		}
	        		if(progress != mOriginalStreamVolume){
	        			seekBar.setProgress(mOriginalStreamVolume * 10);
	        		}else{
	        			seekBar.setProgress(getPersistedInt(mOriginalStreamVolume * 10));
	        		}
	        	}
	            seekBar.setOnSeekBarChangeListener(this);

	            mContext.getContentResolver().registerContentObserver(
	                    System.getUriFor(System.VOLUME_SETTINGS[mStreamType]),
	                    false, mVolumeObserver);

	            if (defaultUri == null) {
	                if (mStreamType == AudioManager.STREAM_RING) {
	                    defaultUri = Settings.System.DEFAULT_RINGTONE_URI;
	                } else if (mStreamType == AudioManager.STREAM_NOTIFICATION) {
	                    defaultUri = Settings.System.DEFAULT_NOTIFICATION_URI;
	                } else {
	                    defaultUri = Settings.System.DEFAULT_ALARM_ALERT_URI;
	                }
	            }

	            mRingtone = RingtoneManager.getRingtone(mContext, defaultUri);

	            if (mRingtone != null && (mStreamType == AudioManager.STREAM_RING
	            		||mStreamType == AudioManager.STREAM_MUSIC
	            		||mStreamType == AudioManager.STREAM_ALARM
	            		||mStreamType == AudioManager.STREAM_NOTIFICATION
	            		||mStreamType == AudioManager.STREAM_SYSTEM
	            		||mStreamType == AudioManager.STREAM_VOICE_CALL
	            		)) {
	            	try {
	            		mRingtone.setStreamType(mStreamType);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
	                
	            }
	        }

	        public void stop() {
	            stopSample();
	            mContext.getContentResolver().unregisterContentObserver(mVolumeObserver);
	            mSeekBar.setOnSeekBarChangeListener(null);
	            mIsRuningThread =false ;
	        }

	       /* public void revertVolume() {
	            mAudioManager.setStreamVolume(mStreamType, mOriginalStreamVolume, 0);
	        }*/

	        public void onProgressChanged(SeekBar seekBar, int progress,
	                boolean fromTouch) {
	        	
	        	Log.i(TAG, "onProgressChanged   progress = "+progress + "  fromTouch  = " + fromTouch);

	            if (!fromTouch) {
	            	// reset
		        	/*if(progress ==1){
		        		Settings.System.putInt(mContext.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,mVibrateWhenRing );
		        	}*/
	                return;
	            }
	            if(progress > mMaxVolume ){
	            	progress = mMaxVolume;
	            	
	            }
	            if(progress < 0 ){
	            	progress = 0;

	            }	
	            
	            
	            postSetVolume(progress);
	            
	            if(progress < 1){
	            	
	            	stopSample();
	            }else{

	            	if (!isSamplePlaying() ) {
 	            		stopSample();
	        			startSample();
	        		}

	            }	         
	            
	        }

	        void postSetVolume(int progress) {
	            // Do the volume changing separately to give responsive UI
	        	int tempProgress = 0;
	        		if(progress % 10 == 0){
	        			tempProgress = progress/10;
	        		}else {
	        			tempProgress = progress/10 + 1;
	        		}
        		
	            mLastProgress = tempProgress;
	            mHandler.removeCallbacks(this);
	            mHandler.post(this);
	        }

	        public void onStartTrackingTouch(SeekBar seekBar) {
	        	Log.i(TAG, "onStartTrackingTouch    ");
	        	
	        	mRingMode = mAudioManager.getRingerModeInternal();	      
	        	Log.i(TAG, "onStartTrackingTouch   mRingMode  =   "  + mRingMode);
	        	mOriginalStreamVolume = mAudioManager.getStreamVolume(mStreamType);
	        	Log.i(TAG, "onStartTrackingTouch   mOriginalStreamVolume  =   "  + mOriginalStreamVolume);
	        	//save
	        	if(mOriginalStreamVolume >0){
	        		mVibrateWhenRing = Settings.System.getInt(mContext.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,1 );
	        		Log.i(TAG, "onStartTrackingTouch   mVibrateWhenRing  =   "  + mVibrateWhenRing);
	        	}
	        
	        	mLastProgress= mAudioManager.getStreamVolume(mStreamType);
	        	
	        	Log.i(TAG, "onStartTrackingTouch   mLastProgress  =   "  + mLastProgress);
	        	if(mLastProgress <1){
	        		stopSample();
	        	}else{
	        		stopSample();

	            	if (!isSamplePlaying() ) {
	 	            		stopSample();
		        			startSample();
	        		}

	        	}
	        	
	        	mIsStartCount = false;
            	mCount = 0;
	        	
	        	flagSeekBarVolumizer = this; 
	        }

	        public void onStopTrackingTouch(SeekBar seekBar) {
	        	Log.i(TAG, "onStopTrackingTouch    ");
	        	//解决在音量为零的时候，点击进度条，设置成震动的bug
	        	if(mOriginalStreamVolume == mLastProgress && mLastProgress == 0){
	        		return;
	        	}
	        		        	
			setPersistInt(mSeekBar.getProgress() );
	        	// reset
	        	/*if(mLastProgress >0){
	        		Settings.System.putInt(mContext.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,mVibrateWhenRing );
	        	}*/
	        	// volume state confirm 
	        	/*if(mOriginalStreamVolume < 1){
	        		mVolumeInitState = AudioProfileActivity.RING_MODE_VIBRATE;
	        	}else{
	        		mVolumeInitState = AudioProfileActivity.RING_MODE_RING;
	        	}
	        	if(mLastProgress < 1){
	        		mVolumeFinalState = AudioProfileActivity.RING_MODE_VIBRATE;
	        		//Settings.System.putInt(mContext.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,0 );
	        	}else{
	        		mVolumeFinalState = AudioProfileActivity.RING_MODE_RING;
	        	}
	        	
	        	
	        	// send broadcast to update the mute and vibrate state
	        	Log.i(TAG, "onStopTrackingTouch  mOriginalStreamVolume ="+mLastProgress);
	        	Log.i(TAG, "onStopTrackingTouch   mLastProgress ="+mLastProgress);
	        	if(mVolumeInitState != mVolumeFinalState){
	        			        		
	        		mContext.sendBroadcast(new Intent(AudioProfileActivity.ACTION_UPDATE_MUTE_VIBRATE).putExtra("RING_MODE",mVolumeFinalState));
	        		
	        		Log.i(TAG, "onStopTrackingTouch  mVolumeInitState != mVolumeFinalState");
	        	}
	        	if(mOriginalStreamVolume < 1 && mLastProgress < 1){
	        		mContext.sendBroadcast(new Intent(AudioProfileActivity.ACTION_UPDATE_MUTE_VIBRATE).putExtra("RING_MODE",AudioProfileActivity.RING_MODE_VIBRATE));
	        	}*/
	        	// update init state
	        	mOriginalStreamVolume = mLastProgress;	        	
	        	
	        	//postSetVolume(mLastProgress);
	        	
	        	if(mLastProgress < 1){
	        		stopSample();     		
	            //	mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
	        	}else {	        		
	        		//mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mSaveMusicVolume, 0);
	        		
	 	            	if (!isSamplePlaying()) {	
	 	            		stopSample();
			                startSample();
			            }	 	            	
	 	            	
	 	            	mIsStartCount = true;
	 	            	mCount = 0; 	          	
	 	            	
	 	       	        	
	        	}
	        	
	        }
	         
	   
	        
        // penggangding add begin 
		public void setPersistInt(int progress) {
			persistInt(progress);
		}
        // penggangding add end
	        public void run() {

	        	Log.i(TAG, "run   original mode ="+mAudioManager.getRingerModeInternal());
	        	//解决设置音量为零的时候，设置成震动的bug
	        	/*if((mAudioManager.getRingerModeInternal() == AudioManager.RINGER_MODE_SILENT || mAudioManager.getRingerModeInternal() == AudioManager.RINGER_MODE_NORMAL) && mLastProgress == 0){
	        		return;
	        	}*/
	            mAudioManager.setStreamVolume(mStreamType, mLastProgress, 0);	   
	            Log.i(TAG, "run   after set streamRingVolume  mode ="+mAudioManager.getRingerModeInternal());
	            // add notification volume
	            //mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, mLastProgress, 0);
	            
	            // add system volume 
	            //mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, mLastProgress, 0);
	        	
	        }

	        public boolean isSamplePlaying() {
	            return mRingtone != null && mRingtone.isPlaying();
	        }

	        public void startSample() {
	        	try{
	        		onSampleStarting(this);
		            if (mRingtone != null) {
		                mRingtone.play();
		            }
	        	}catch(Exception e){
	        		e.printStackTrace();
	        	}
	            
	        }

	        public void stopSample() {
	            if (mRingtone != null) {
	                mRingtone.stop();
	            }
	        }

	        public SeekBar getSeekBar() {
	            return mSeekBar;
	        }

	        

			public void setSeekBar(SeekBar seekBar) {
				this.mSeekBar = seekBar;
				initSeekBar(seekBar, mUri);
			}

			public void changeVolumeBy(int amount) {
	            mSeekBar.incrementProgressBy(amount);
	            if (!isSamplePlaying()) {
	                startSample();
	            }
	            postSetVolume(mSeekBar.getProgress());
	            mVolumeBeforeMute = -1;
	        }

	        public void muteVolume() {
	            if (mVolumeBeforeMute != -1) {
	                mSeekBar.setProgress(mVolumeBeforeMute);
	                startSample();
	                postSetVolume(mVolumeBeforeMute);
	                mVolumeBeforeMute = -1;
	            } else {
	                mVolumeBeforeMute = mSeekBar.getProgress();
	                mSeekBar.setProgress(0);
	                stopSample();
	                postSetVolume(0);
	            }
	        }

	        public void onSaveInstanceState(VolumeStore volumeStore) {
	        	if (mLastProgress >= 0) {
	            
	                volumeStore.volume = mLastProgress;
	                volumeStore.originalVolume = mOriginalStreamVolume;
	            }
	        }

	        public void onRestoreInstanceState(VolumeStore volumeStore) {
	            if (volumeStore.volume != -1) {
	                mOriginalStreamVolume = volumeStore.originalVolume;
	                mLastProgress = volumeStore.volume;
	                postSetVolume(mLastProgress);
	            }
	        }
	    }
}

