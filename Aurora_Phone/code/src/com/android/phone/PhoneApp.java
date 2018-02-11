/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.phone;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;

import com.gionee.aora.numarea.export.INumAreaManager;
import com.gionee.aora.numarea.export.INumAreaObserver;
import com.gionee.aora.numarea.export.IUpdataResult;
import android.os.SystemProperties;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import android.os.Build;
/**
 * Top-level Application class for the Phone app.
 */
public class PhoneApp extends Application {
    PhoneGlobals mPhoneGlobals;
//aurora add zhouxiaobing 20130926 start   
    static PhoneApp sMe;
    private static final String ACTION_NUMAREA = "gionee.aora.numarea";
    static final Boolean DIAL_FROM_INCALLSCREEN_DIRECT = true;
    public static INumAreaManager mNumAreaManager;
    public static PhoneApp getInstance() {
        return sMe;
    }
  //aurora add zhouxiaobing 20130926 end     
    public PhoneApp() {
    }
    
    public static boolean isV2 = true;

    @Override
    public void onCreate() {
        if (UserHandle.myUserId() == 0) {
            // We are running as the primary user, so should bring up the
            // global phone state.
            if(Build.VERSION.SDK_INT >= 21) {
            	createI2PhoneGlobals();
            } else if(PhoneUtils.isMtk()) {
              	createMtkPhoneGlobals();
        	} else if (PhoneUtils.isMultiSimEnabled()) {
//                mPhoneGlobals = new MSimPhoneGlobals(this);
            	createPhoneGlobals();
            } else {
            	if(initQualcommDoubleSim(this)) {
//                    String prop = SystemProperties.get("ro.gn.gnprojectid");
//                	if(prop.contains("NBL8910A")) {
                		SystemProperties.set("persist.radio.multisim.config","dsda");
//                	}
            		createPhoneGlobals();
            	} else {
            		mPhoneGlobals = new PhoneGlobals(this);
            	}
            }
            mPhoneGlobals.onCreate();
      //aurora add zhouxiaobing 20130926 start  
        sMe=this;
        NumberAreaUtil.bindService(mNumAreaObserver);
      //aurora add zhouxiaobing 20130926 end     
        }
    }

    //aurora add zhouxiaobing 20130926 start      
    private INumAreaObserver.Stub mNumAreaObserver = new INumAreaObserver.Stub() {
        @Override
        public void updata(int aResultCode) throws RemoteException {

         }
    };
  //aurora add zhouxiaobing 20130926 end  
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (mPhoneGlobals != null) {
            mPhoneGlobals.onConfigurationChanged(newConfig);
        }
        super.onConfigurationChanged(newConfig);
    }
    
    private void createPhoneGlobals() {
    	try {               
			Class clazz = Class.forName("com.android.phone.MSimPhoneGlobals"); 
			Class[] inArgs = new Class[]{Context.class};
			Object[] inArgsParms = new Object[]{this};
		    Constructor constructor = clazz.getDeclaredConstructor(inArgs);
		    mPhoneGlobals = (PhoneGlobals) constructor.newInstance(inArgsParms);    
		} catch(Exception e){
        	e.printStackTrace();
        }    
    }
        
    private void createMtkPhoneGlobals() {
    	try {               
			Class clazz = Class.forName("com.android.phone.MtkPhoneGlobals"); 
			Class[] inArgs = new Class[]{Context.class};
			Object[] inArgsParms = new Object[]{this};
		    Constructor constructor = clazz.getDeclaredConstructor(inArgs);
		    mPhoneGlobals = (PhoneGlobals) constructor.newInstance(inArgsParms);    
		} catch(Exception e){
        	e.printStackTrace();
        }    
    }
    
    public static List<Activity> mPrivacyActivityList = new ArrayList<Activity>();
    
    private void createI2PhoneGlobals() {
    	try {               
			Class clazz = Class.forName("com.android.phone.I2PhoneGlobals"); 
			Class[] inArgs = new Class[]{Context.class};
			Object[] inArgsParms = new Object[]{this};
		    Constructor constructor = clazz.getDeclaredConstructor(inArgs);
		    mPhoneGlobals = (PhoneGlobals) constructor.newInstance(inArgsParms);    
		} catch(Exception e){
        	e.printStackTrace();
        }    
    }
    
	  private static boolean initQualcommDoubleSim(Context mContext) {
	    	boolean result = false;
			try {
				Class<?> cx = Class.forName("android.telephony.MSimTelephonyManager");
				Method md = cx.getMethod("getDeviceId", int.class);
	            Cursor cursor = mContext.getContentResolver().query(Uri.parse("content://telephony/siminfo"), 
	                    null, " slot in ('-1', '0', '1')", null, null);
	            try {
	                if (cursor != null) {
	    				if(cursor.getCount() > 0) {
	    					result = true;	
	    				}
	                }
	            } finally {
	                if (cursor != null) {
	                    cursor.close();
	                }
	            }
			} catch (Exception e) {
	        	e.printStackTrace();
				result = false;
			}
			return result;
		}
}
