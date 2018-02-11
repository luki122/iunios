/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
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

package com.android.settings;

import aurora.preference.*;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;

public class WechatSet extends AuroraPreferenceActivity implements AuroraPreference.OnPreferenceChangeListener{
    private static final String TAG = "WechatSet";

    private static final String DUAL_WECHAT = "dual_wechat";
    private AuroraSwitchPreference dualWechat;
    private PackageManager packageManager;
    private ComponentName wechatComponentName = new ComponentName("com.tencent.mm.clone", "com.tencent.mm.clone.MainActivity");
    boolean isEnabled = false;
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        packageManager = getPackageManager();
        int state = packageManager.getComponentEnabledSetting(wechatComponentName);
        isEnabled = ((state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED)&&(state != PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)) ;
        Log.i(TAG, state+"isEnabled="+isEnabled);
        addPreferencesFromResource(R.xml.aurora_dual_wechat);
        getAuroraActionBar().setTitle(R.string.wechat_set);
        findPreference("dual_wechat_info").setBackgroundResource(0); 
        findPreference("dual_wechat_info").setEnabled(false);
        initUI();
    }

    private void initUI() {
        dualWechat = (AuroraSwitchPreference) findPreference(DUAL_WECHAT); 
        try{
            packageManager.getPackageGids("com.tencent.mm");
            dualWechat.setSummary(R.string.use_dual_wechat_summery);
        }catch(NameNotFoundException e){
        	dualWechat.setEnabled(false);
        	dualWechat.setSummary(R.string.no_wechat);
        }
        if(dualWechat != null){
        	dualWechat.setOnPreferenceChangeListener((aurora.preference.AuroraPreference.OnPreferenceChangeListener) this);
        	dualWechat.setChecked(isEnabled);
        }
    }

	@Override
	public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {
		isEnabled = (Boolean)newValue;
		packageManager.setComponentEnabledSetting(wechatComponentName, isEnabled?PackageManager.COMPONENT_ENABLED_STATE_ENABLED:PackageManager.COMPONENT_ENABLED_STATE_DISABLED , 0);
		if(!isEnabled){
			new Thread(){
				public void run() {
					synchronized (this) {
						final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
						am.forceStopPackage("com.tencent.mm");
					}
				};
			}.start();
			
		}
		return true;
	}

}
