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

package com.android.calendar;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.mediatek.calendar.InjectedServices;

public class CalendarApplication extends Application {

    private static InjectedServices sInjectedServices;
    
    //Gionee <Author: lihongyu> <2013-04-11> add for CR000000 begin
    private boolean mHaveUpgradeInfo = false;
    private Activity mRunningActivity = null;
    private static boolean UPGRADE_SUPPORT = false;
    //Gionee <Author: lihongyu> <2013-04-11> add for CR000000 end

    @Override
    public void onCreate() {
        super.onCreate();

        /*
         * Ensure the default values are set for any receiver, activity,
         * service, etc. of Calendar
         */
        GeneralPreferences.setDefaultValues(this);

        // Save the version number, for upcoming 'What's new' screen.  This will be later be
        // moved to that implementation.
        Utils.setSharedPreference(this, GeneralPreferences.KEY_VERSION,
                Utils.getVersionCode(this));
        
        //Gionee <Author: lihongyu> <2013-04-11> add for CR000000 begin
        PackageManager packageManager = getPackageManager();
        
        //Log.d("upgrade", "onCreate"+(packageManager != null));
        
        if (packageManager != null) {
            try {
                packageManager.getApplicationInfo("com.gionee.appupgrade", PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
                UPGRADE_SUPPORT = true;
                
                //Log.d("upgrade", "getApplicationInfo");
            } catch (NameNotFoundException e) {
                UPGRADE_SUPPORT = false;
            }
        }
        //Gionee <Author: lihongyu> <2013-04-11> add for CR000000 end
    }

    /**
     * M: Overrides the system services with mocks for testing. Use
     * CalendarApplication.injectServices(services) to inject this mock system
     * service, use CalendarApplication.injectServices(null) to release the mock
     * service. It will auto set to null when testing over.
     */
    public static void injectServices(InjectedServices services) {
        sInjectedServices = services;
    }

    /**
     * M: When want a system account while testing, should use injectServices()
     * to inject one mock system service, and must change source code from
     * AccountManager.get(this) or AccountManager.get(context) to
     * AccountManager.get(getApplicationContext()), because just use
     * getApplicationContext(), the AccountManager.get() will call back to
     * CalendarApplication.class file.
     *
     * many using details look {@link TestCaseUtils}
     */
    @Override
    public Object getSystemService(String name) {
        if (sInjectedServices != null) {
            Object service = sInjectedServices.getSystemService(name);
            if (service != null) {
                return service;
            }
        }
        return super.getSystemService(name);
    }
    
    //Gionee <Author: lihongyu> <2013-04-11> add for CR000000 begin
    public void setHaveUpgradeInfo(boolean flag){
        mHaveUpgradeInfo = flag;
        
        //Log.d("upgrade", "setHaveUpgradeInfo"+mHaveUpgradeInfo);
        
        if(mHaveUpgradeInfo){
            if(mRunningActivity != null){
                startVersionActivity(mRunningActivity);
                
                Log.d("upgrade", "setHaveUpgradeInfo"+"in");
                
                mHaveUpgradeInfo = false;
            }
        }
    }
    
    public void startVersionActivity(Activity activity){
        if (activity != null){
            Intent intent = new Intent
            ("com.gionee.appupgrade.action.GN_APP_UPGRAGE_SHOW_DIALOG");
            intent.putExtra("package", getPackageName());
            activity.startActivity(intent);
            
            Log.d("upgrade", "startVersionActivity");
            
        }
    }
    
    public void registerVersionCallback(Activity activity){
        mRunningActivity = activity;

        if(mHaveUpgradeInfo){
            startVersionActivity(mRunningActivity);
            mHaveUpgradeInfo = false;
            
            Log.d("upgrade", "registerVersionCallback"+"       startVersionActivity");
            
        }
    }
    
    public void unregisterVersionCallback(Activity activity){
        mRunningActivity = null;
    }
    
    public static boolean isEnableUpgrade() {
        return UPGRADE_SUPPORT;
    }
    
    //Gionee <Author: lihongyu> <2013-04-11> add for CR000000 end
}
