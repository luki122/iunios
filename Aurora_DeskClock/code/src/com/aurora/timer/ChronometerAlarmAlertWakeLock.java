/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.content.Context;
import android.os.PowerManager;

/**
 * Hold a wakelock that can be acquired in the Activity and
 * released in the activity
 */
public class ChronometerAlarmAlertWakeLock {

    private static PowerManager.WakeLock chronmenterWakeUpLock;
    private static PowerManager.WakeLock stopWatchWakeUpLock;
    private static PowerManager.WakeLock chronmenterScreenOnLock;
    
    public static final String CHRONMENTER = "CHRONMENTER";
    public static final String STOPWATCH = "STOPWATCH";
    

    public static PowerManager.WakeLock createPartialWakeLock(Context context,String className) {
    	 PowerManager pm =
             (PowerManager) context.getSystemService(Context.POWER_SERVICE);  
//     return pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, className);
    	 PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, className);
    	 wl.setReferenceCounted(false);
    	 return wl;
    }

    public static void acquireCpuWakeLock(Context context ,String className) {
    	if(CHRONMENTER.equals(className)){
    		if (chronmenterWakeUpLock != null) {
    			return;
    		}
    		
    		chronmenterWakeUpLock = createPartialWakeLock(context ,CHRONMENTER );
    		chronmenterWakeUpLock.acquire();
    	}
    	if(STOPWATCH.equals(className)){
    		if (stopWatchWakeUpLock != null) {
    			return;
    		}
    		stopWatchWakeUpLock = createPartialWakeLock(context ,STOPWATCH );
    		stopWatchWakeUpLock.acquire();
    	}
    }

    public static void releaseCpuLock(String className) {
    	if(CHRONMENTER.equals(className)){
    		if (chronmenterWakeUpLock != null) {
    			chronmenterWakeUpLock.release();
    			chronmenterWakeUpLock = null;
    		}
    	}
    	if(STOPWATCH.equals(className)){
    		if (stopWatchWakeUpLock != null) {
    			stopWatchWakeUpLock.release();
    			stopWatchWakeUpLock = null;
    		}
    	}
    }
    

    public static PowerManager.WakeLock createScreenOnLock(Context context,String className) {
    	 PowerManager pm =
             (PowerManager) context.getSystemService(Context.POWER_SERVICE);  
    	 PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP, className);
    	 wl.setReferenceCounted(false);
    	 return wl;
    }
    
    public static void acquireScreenOnLock(Context context ,String className) {
    		if (chronmenterScreenOnLock != null && chronmenterScreenOnLock.isHeld()) {
    			return;
    		} else {
    			chronmenterScreenOnLock = null;
    		}
    		
    		chronmenterScreenOnLock = createScreenOnLock(context ,CHRONMENTER );
    		chronmenterScreenOnLock.acquire(3*60*1000);
    	
    }
    
    public static void releaseScreenOnLock(String className) {
    		if (chronmenterScreenOnLock != null) {
    			if(chronmenterScreenOnLock.isHeld()) {
	    			chronmenterScreenOnLock.release();
    			} 
    			chronmenterScreenOnLock = null;	
    			
    		}
    	
    }
}
