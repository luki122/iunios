package com.secure.model;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.media.AudioManager;
import android.media.SoundPool;
import com.aurora.secure.R;

public class AdScanSoundModel{
    private final static float DEFAULT_VOLUME = 0.9f;
    private final static int NORMAL_PRIORITY = 2000;
    private final static int LOOP = -1;
    private final static int NO_LOOP = 0;
    /**
     * 由于U2无法实现SoundPool循环播放，很蛋疼，
     * 所以只能在扫描音乐播完一遍后再重新播放，而单次扫描音乐的播放时长是2000ms
     */
    private final static int SCAN_MUSIC_DURING_TIME = 2000;
    
    private SoundThread mThread;
    private int soundIdOfFindAd;
    private int soundIdOfScan;
    private Context golbContext;
    private Object soundPoolLock = new Object();
    private SoundPool mSoundPool = null;
    
    public AdScanSoundModel (Context context){
    	this.golbContext = context;
    }
    
    public void onResume(){
    	mThread = new SoundThread();
        mThread.start();
    }
    
    public void onPause(){
    	if(mThread != null){
    		 mThread.quit();
    	}  
    	synchronized (soundPoolLock) {
    		if (mSoundPool != null) {
	           mSoundPool.release();
	           mSoundPool = null;
	        }
		}  	
    }
    
    private boolean playScanSound() throws java.lang.InterruptedException {
    	synchronized (soundPoolLock) {
        	if(mSoundPool == null){
        		return false;
        	}
        	mSoundPool.play(soundIdOfScan, DEFAULT_VOLUME, DEFAULT_VOLUME,
                    NORMAL_PRIORITY, NO_LOOP, 1.0f);
            return true;
    	}
    }
    
    private void stopScanSound() throws java.lang.InterruptedException {
    	synchronized (soundPoolLock) {
        	if(mSoundPool == null){
        		return ;
        	}
        	mSoundPool.stop(soundIdOfScan);
    	}
    }
    
    public boolean playFindAdSound() throws java.lang.InterruptedException {
    	synchronized (soundPoolLock) {
        	if(mSoundPool == null){
        		return false;
        	}
        	mSoundPool.play(soundIdOfFindAd, DEFAULT_VOLUME, DEFAULT_VOLUME,
                    NORMAL_PRIORITY, NO_LOOP, 1.0f);
            return true;
    	}
    }

    private final class SoundThread extends java.lang.Thread {       
        private int mLastSample;      
        private Object loadLock = new Object();
        public void run() {
            try {
                initSoundPool(golbContext,2);
                while(playScanSound()){
                	Thread.sleep(SCAN_MUSIC_DURING_TIME);
                	stopScanSound();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
 
        @TargetApi(8) 
        private void initSoundPool(Context context,int numStreams) throws java.lang.InterruptedException {
        	if(context == null){
        		return ;
        	}
        	synchronized (soundPoolLock) {
        		if (mSoundPool != null) {
       	           mSoundPool.release();
       	           mSoundPool = null;
       	        } 
                mSoundPool = new SoundPool(numStreams, AudioManager.STREAM_MUSIC, 0);
                mSoundPool.setOnLoadCompleteListener(new LoadCompleteCallback());
                soundIdOfFindAd = mSoundPool.load(context, 
            			R.raw.find_ad_music,
            			NORMAL_PRIORITY);
            	soundIdOfScan = mSoundPool.load(context, 
            			R.raw.scan_music,
            			NORMAL_PRIORITY);
        	}  
            synchronized(loadLock) {            	         	
            	mLastSample = soundIdOfScan;
            	loadLock.wait();
            }
        }

        public void quit() {
            interrupt();       
        }
        
        private final class LoadCompleteCallback implements
	        android.media.SoundPool.OnLoadCompleteListener {
	        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
	            synchronized(loadLock) {
	                if (sampleId == mLastSample) {
	                	loadLock.notify();
	                }
	            }
	        }
	    }
    }
    
    public void realseObject(){
    	this.golbContext = null;
    }
}

