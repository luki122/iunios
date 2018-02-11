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

package com.aurora.timer;

//import com.android.stopwatch.StopWatchActivity;

import aurora.app.AuroraActivity;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.PowerManager.WakeLock;

import com.android.deskclock.AlarmAlertWakeLock;
import com.android.deskclock.AsyncHandler;

import android.util.Log;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;

public class TimerInitReceiver extends BroadcastReceiver {

	public static final String TAG = "TimerInitReceiver";
	
    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getAction();
        Log.v(TAG, "action = " + action);
        final PendingResult result = goAsync();        
        AsyncHandler.post(new Runnable() {
            @Override
            public void run() {
                WakeLock wl = AlarmAlertWakeLock.createPartialWakeLock(context);
                wl.acquire();
            	SharedPreferences sharedPreferences = context.getSharedPreferences("Chronometer",Activity.MODE_PRIVATE);
        		int state = sharedPreferences.getInt("state", TimerFragment.STATE_INIT);
    			if (state == TimerFragment.STATE_RUNNING) { 
    				long stopTime = sharedPreferences.getLong("stopTime", 0);
    				long now = System.currentTimeMillis();
    				if(now < stopTime) {
	    				Intent intent = new Intent(context, TimerReceiver.class);
	    				intent.setAction("coma.aurora.timer.alert");
	    				PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);	    				
	    				AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	    				am.set(AlarmManager.RTC_WAKEUP, stopTime, sender);
    				} else {
    					 Editor editor = sharedPreferences.edit();
    					 editor.putInt("state", TimerFragment.STATE_INIT);
    			 		 editor.commit();
    				}
    			}
                
                result.finish();
                Log.v(TAG, "finished");
                wl.release();
            }
        });
    }
}
