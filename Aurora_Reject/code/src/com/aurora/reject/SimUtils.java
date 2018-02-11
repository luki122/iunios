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

package com.aurora.reject;

import android.os.SystemProperties;
import gionee.provider.GnTelephony.SIMInfo;
import gionee.telephony.AuroraTelephoneManager;
import android.util.Log;
import android.content.Context;
import android.provider.Settings.System;

public class SimUtils {
	
    private static final String TAG = "SimUtils";
	//u5
	private static boolean is7503() {
		String prop = SystemProperties.get("ro.gn.gnprojectid");
		return prop.contains("CBL7503");
	}
	   //还要加入判断ServiceState的判断才行，现在只是判断了卡插入的状态，双卡设置中关闭卡对这里无影响。需要在GnTelephonyManager加接口
    public static boolean isShowDoubleButton(Context context) {
    	 if(is7503()) {    		 
	   	 	 int dualSimModeSetting = System.getInt(context.getContentResolver(), "msim_mode_setting", 3);
	   		 return SIMInfo.getInsertedSIMCount(context) > 1 && (dualSimModeSetting == 3);
   	     } else if(AuroraTelephoneManager.isMtkGemini()) { 
	       	  int dualSimModeSetting = System.getInt(context.getContentResolver(), "dual_sim_mode_setting", 3);
    		 return SIMInfo.getInsertedSIMCount(context) > 1 && (dualSimModeSetting == 3);
    	 } else {
    	     boolean isAllActive = false;
    		 try {
                 int simstate = android.provider.Settings.Global.getInt(context.getContentResolver(),
           		   "mobile_data"+ 2);            
                 Log.d(TAG, "updateCardState restore simstate= " + simstate);
                 if(simstate == 3) {
                 	isAllActive = true;
                 } 
             } catch (Exception e) {
          	    e.printStackTrace();
             }    
    		 return SIMInfo.getInsertedSIMCount(context) > 1 && isAllActive;
    	 }
    }
}
