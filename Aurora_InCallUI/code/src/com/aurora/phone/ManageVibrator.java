/*
 * Copyright (c) 2013 The Linux Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of The Linux Foundation, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.android.incallui;

import com.android.incallui.Call;
import com.android.incallui.InCallPresenter;
import com.android.incallui.InCallPresenter.InCallState;
import android.telecom.DisconnectCause;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.os.SystemVibrator;
import android.util.Log;
import android.provider.Settings;

public class ManageVibrator  {
	private static final String LOG_TAG = "ManageVibrator";

	private Context mApp;
	private InCallState mPreviousCallState = InCallState.NO_CALLS;     

	public ManageVibrator(Context app) {
		mApp = app;
        //aurora add liguangyu 20131102 for BUG #359 start
        mAudioManager = (AudioManager) app.getSystemService(Context.AUDIO_SERVICE);
        mEmgVibrator = new SystemVibrator();
        //aurora add liguangyu 20131102 for BUG #359 end
//        mCM.registerForPreciseCallStateChanged(mHandler, PHONE_STATE_CHANGED, null);
//        mCM.registerForDisconnect(mHandler, PHONE_DISCONNECT, null);
//        if(InCallApp.getPhone().getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA) {
//        	InCallApp.getPhone().registerForLineControlInfo(mHandler, CDMA_ACCEPT, null);
//        }

	}

    //aurora add liguangyu 20131102 for BUG #359 start
    private AudioManager mAudioManager;
    private Vibrator mEmgVibrator;
    private final static int vduration = 100;
    
    
    private Handler mHandler = new Handler();
    
    
    public void onPhoneStateChanged() {    
		InCallState state = InCallPresenter.getInstance().getInCallState();
        
        //aurora add liguangyu 20131108 for BUG #506 start
        if (state == InCallState.INCALL && mPreviousCallState == InCallState.OUTGOING) {
            //aurora modify liguangyu 20140730 for BUG #7013 start
//            if(call.getPhone().getPhoneType() != Phone.PHONE_TYPE_CDMA) {
        		auroraVibrate();
//            }
            //aurora modify liguangyu 20140730 for BUG #7013 end
        } 
        mPreviousCallState = InCallPresenter.getInstance().getInCallState();
        //aurora add liguangyu 20131108 for BUG #506 end

    }
    
    public void onDisconnect(Call call) {
    	DisconnectCause cause = call.getDisconnectCause();
        //aurora add liguangyu 20131108 for BUG #506 start
        if(cause.getCode() != DisconnectCause.MISSED
        		&& cause.getCode() != DisconnectCause.LOCAL       
        		&& cause.getCode() != DisconnectCause.REJECTED
        		&& (mPreviousCallState != InCallState.OUTGOING)
        		&& ((mPreviousCallState != InCallState.PENDING_OUTGOING))) {
        	auroraVibrate();
        }
        //aurora add liguangyu 20131108 for BUG #506 end
    }

    private boolean mIsSilent;
    
    public boolean isSilentMode(){
    	return mIsSilent;
    }
    
    public void auroraVibrate(){
      	log("auroraVibrate");
        if(mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
        	mIsSilent = true;
//            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
//            mHandler.postDelayed(new Runnable() {
//                public void run() {
//                	log("vibrate = ");
//                    mEmgVibrator.vibrate(vduration);
//                }
//            }, vduration);           
//            mHandler.postDelayed(new Runnable() {
//                public void run() {
//                	log("restore ringer mode");
//                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
//                    //aurora add liguangyu 20140624 for BUG #6055 start
//                    mIsSilent = false;
//                    //aurora add liguangyu 20140624 for BUG #6055 end
//                }
//            }, vduration *2);
        	
        	boolean canVibrate = Settings.System.getInt(mApp.getContentResolver(),  
                    Settings.System.VIBRATE_IN_SILENT, 1) == 1 ;
        	if(canVibrate) {
               	mIsSilent = false;
             	mEmgVibrator.vibrate(vduration);
        	} else  {
               	mIsSilent = true;
               	Settings.System.putInt(mApp.getContentResolver(),  
                        Settings.System.VIBRATE_IN_SILENT, 1);
          	  mEmgVibrator.vibrate(vduration);
              mHandler.postDelayed(new Runnable() {

	              public void run() {
	              	log("restore ringer mode");
	            	Settings.System.putInt(mApp.getContentResolver(),  
	                        Settings.System.VIBRATE_IN_SILENT, 0);
	                  mIsSilent = false;
	              }
              }, vduration);
        		
        	}
        } else {
        	mIsSilent = false;
        	mEmgVibrator.vibrate(vduration);
        }
    }
    //aurora add liguangyu 20131102 for BUG #359 end  
	

	private void log(String msg) {
		Log.d(LOG_TAG, msg);
	}

}
