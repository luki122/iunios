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

package com.android.deskclock;

import android.content.Context;
import android.os.PowerManager;

/**
 * Hold a wakelock that can be acquired in the AlarmReceiver and
 * released in the AlarmAlert activity
 */
public class AlarmAlertWakeLock {

    private static PowerManager.WakeLock sCpuWakeLock;
    //android:hxc start
    private static PowerManager.WakeLock sAlarmAlertFSCpuWakeLock;
    private static final String AlarmAlert = "Alarm Alert";
    //android:hxc end

    public static PowerManager.WakeLock createPartialWakeLock(Context context) {
        PowerManager pm =
                (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Log.LOGTAG);
    }

    //android:hxc start
    static PowerManager.WakeLock createFullWakeLock(Context context) {
        PowerManager pm =
                (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, AlarmAlert);
    }
    //android:hxc end
    
    
    static void acquireCpuWakeLock(Context context) {
        if (sCpuWakeLock != null) {
            return;
        }
        android.util.Log.e("jadon", "开锁 CPU");
        sCpuWakeLock = createPartialWakeLock(context);
        sCpuWakeLock.acquire();
    }

    static void releaseCpuLock() {
        if (sCpuWakeLock != null) {
        	android.util.Log.e("jadon", "解锁 CPU");
            sCpuWakeLock.release();
            sCpuWakeLock = null;
        }
    }
    
    //android:hxc start
    // for AlarmAlertFullScreen acquire wakelock
	static void acquireAlarmAlertFSCpuWakeLock(Context context) {
		if (sAlarmAlertFSCpuWakeLock != null) {
			return;
		}
		android.util.Log.e("jadon", "开锁 FULL");
		sAlarmAlertFSCpuWakeLock = createFullWakeLock(context);
		sAlarmAlertFSCpuWakeLock.acquire();
	}
	
	static void releaseAlarmAlertFSCpuLock() {
		if (sAlarmAlertFSCpuWakeLock != null) {
			android.util.Log.e("jadon", "解锁 FULL");
			sAlarmAlertFSCpuWakeLock.release();
			sAlarmAlertFSCpuWakeLock = null;
		}
	}
    //android:hxc end
	
	
}
