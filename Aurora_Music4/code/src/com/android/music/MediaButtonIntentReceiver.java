/*
 * Copyright (C) 2007 The Android Open Source Project
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.LogUtil;
/**
 * 
 */
public class MediaButtonIntentReceiver extends BroadcastReceiver {

    private static final int MSG_LONGPRESS_TIMEOUT = 1;
    private static final int LONG_PRESS_DELAY = 1000;

    private static long mLastClickTime = 0;
    private static boolean mDown = false;
    private static boolean mLaunched = false;
    private static int mPressTimes =0; //add by chenhl 20140614
    private static HalderPresstime mHalderPresstime;
    private static final String AURORA_START_PLAY = "android.com.auroramusic.startplay";
    private static final String TAG ="MediaButtonIntentReceiver";
    
    
    private static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LONGPRESS_TIMEOUT:
					//Log.i("zll", "zll MediaButtonIntentReceiver handleMessage mLaunched:"+mLaunched);
                    if (!mLaunched) {
//                        Context context = (Context)msg.obj;
//                        Intent i = new Intent();
//                        i.putExtra("autoshuffle", "true");
//                        i.setClass(context, MusicBrowserActivity.class);
//                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        context.startActivity(i);
                        mLaunched = true;
                    }
                    break;
            }
        }
    };
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        LogUtil.d(TAG, " onReceive action:"+intentAction);
        /*if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intentAction)) {
            Intent i = new Intent(context, MediaPlaybackService.class);
            i.setAction(MediaPlaybackService.SERVICECMD);
            i.putExtra(MediaPlaybackService.CMDNAME, MediaPlaybackService.CMDPAUSE);
            context.startService(i);
        } else*/ if (intentAction.equals(AURORA_START_PLAY)) {
        	LogUtil.d(TAG, " onReceive xxxx 2:");
        	/*if (Globals.SWITCH_FOR_SOUND_CONTROL) {
        		AuroraMusicUtil.setScreenFlag(true);
			}*/
        	
        	Intent i = new Intent(context, MediaPlaybackService.class);
			String command = MediaPlaybackService.CMDTOGGLEPAUSE;
            //i.setAction(MediaPlaybackService.AURORA_MUSICPALY_START);
            i.putExtra(MediaPlaybackService.CMDNAME, command);
            context.startService(i);
		} else if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            KeyEvent event = (KeyEvent)
                    intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            
            if (event == null) {
                return;
            }

            int keycode = event.getKeyCode();
            int action = event.getAction();
            long eventtime = event.getEventTime();

            // single quick press: pause/resume. 
            // double press: next track
            // long press: start auto-shuffle mode.
            
            String command = null;
            switch (keycode) {
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    command = MediaPlaybackService.CMDSTOP;
                    break;
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    command = MediaPlaybackService.CMDTOGGLEPAUSE;
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    command = MediaPlaybackService.CMDNEXT;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    command = MediaPlaybackService.CMDPREVIOUS;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    command = MediaPlaybackService.CMDPAUSE;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    command = MediaPlaybackService.CMDPLAY;
                    break;
            }
            LogUtil.d(TAG,  "onReceive xxxx 2---- command:"+command+",keycode:"+keycode);
            if (command != null) {
                if (action == KeyEvent.ACTION_DOWN) {
                    if (mDown) {
                        if ((MediaPlaybackService.CMDTOGGLEPAUSE.equals(command) ||
                                MediaPlaybackService.CMDPLAY.equals(command))
                                && mLastClickTime != 0 
                                && eventtime - mLastClickTime > LONG_PRESS_DELAY) {
                                LogUtil.d(TAG, " onReceive xxxx 2.1:");
                            mHandler.sendMessage(
                                    mHandler.obtainMessage(MSG_LONGPRESS_TIMEOUT, context));
                        }
                    } else if (event.getRepeatCount() == 0) {
                        // only consider the first event in a sequence, not the repeat events,
                        // so that we don't trigger in cases where the first event went to
                        // a different app (e.g. when the user ends a phone call by
                        // long pressing the headset button)

                        // The service may or may not be running, but we need to send it
                        // a command.
                        Intent i = new Intent(context, MediaPlaybackService.class);
                        i.setAction(MediaPlaybackService.SERVICECMD);
                      //modify by chenhl start 2014
                        mHalderPresstime = new HalderPresstime(context);
                        if (keycode == KeyEvent.KEYCODE_HEADSETHOOK &&
                                eventtime - mLastClickTime < 300) {
                                                	
                           /* i.putExtra(MediaPlaybackService.CMDNAME, MediaPlaybackService.CMDNEXT);
                            context.startService(i);
                            mLastClickTime = 0;*/
                        	mPressTimes++;
						   //Log.i("zll", "zll onReceive xxxx 2.2 mPressTimes:"+mPressTimes);
                        	if(mPressTimes==2){
                        		//mHandler.removeCallbacks(mHalderPresstime);
                        		i.putExtra(MediaPlaybackService.CMDNAME, MediaPlaybackService.CMDPREVIOUS);
                                context.startService(i);
                        		mPressTimes=0;
                        		mLastClickTime=0;                       		
                        	}else{
                        		                        		
                        		mHandler.postDelayed(mHalderPresstime
								, 300);
                        		mLastClickTime = eventtime;
                        	}                                                    
                        } else {
                        	//LogUtil.i("zll", "zll onReceive xxxx 2.3:"+MediaPlaybackService.isRunning);
                        	if(!MediaPlaybackService.isRunning){
                        		 AuroraMusicUtil.setHeadsetFlag(true); //add by chenhl 20140902
                        	}
                            i.putExtra(MediaPlaybackService.CMDNAME, command);
                            context.startService(i);
                            mLastClickTime = eventtime;
                            mPressTimes=0;
                            mHandler.removeCallbacks(mHalderPresstime);//add by chenhl 20140614
                           
                        }
                      //modify by chenhl end
                        mLaunched = false;
                        mDown = true;
                    }
                } else {
                	//Log.i("zll", "zll onReceive xxxx 2.4");
                    mHandler.removeMessages(MSG_LONGPRESS_TIMEOUT);
                    mDown = false;
                }
                if (isOrderedBroadcast()) {
                    abortBroadcast();
                }
            }
        }
    }
    
    //add by chenhl start 20140614
	public class HalderPresstime implements Runnable {

		private Context mContext;

		public HalderPresstime(Context context) {
			mContext = context;
		}

		@Override
		public void run() {
			mLastClickTime = 0;
			if(mPressTimes==0){
				return ;
			}
			 LogUtil.d(TAG, "HalderPresstime MediaPlaybackService");
			Intent i = new Intent(mContext, MediaPlaybackService.class);
			i.setAction(MediaPlaybackService.SERVICECMD);
			//Log.i("zll", "zll HalderPresstime mPressTimes:"+mPressTimes);
			if (mPressTimes == 1) {
				i.putExtra(MediaPlaybackService.CMDNAME,
						MediaPlaybackService.CMDNEXT);
			} else if(mPressTimes==2){
				i.putExtra(MediaPlaybackService.CMDNAME,
						MediaPlaybackService.CMDPREVIOUS);
			}
			mContext.startService(i);
			mPressTimes = 0;
			
		}

	}
    //add by chenhl end 20140614
}
