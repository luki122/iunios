/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.systemui;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.provider.Settings;
import android.util.Slog;
import android.os.SystemProperties;
import android.util.Log;

import com.android.systemui.recent.utils.Utils;
/**
 * Performs a number of miscellaneous, non-system-critical actions
 * after the system has finished booting.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "SystemUIBootReceiver";
    private static final String NAVI_KEY_HIDE = "navigation_key_hide";

    @Override
    public void onReceive(final Context context, Intent intent) {
        try {
            // Start the load average overlay, if activated
            ContentResolver res = context.getContentResolver();
            if (Settings.Global.getInt(res, Settings.Global.SHOW_PROCESSES, 0) != 0) {
                Intent loadavg = new Intent(context, com.android.systemui.LoadAverageService.class);
                context.startService(loadavg);
            }
        } catch (Exception e) {
            Slog.e(TAG, "Can't start load average service", e);
        }

        // Aurora <Felix.Duan> <2014-9-22> <BEGIN> Always show navigation bar on boot
        showNaviBarOnBoot(context);
        // Aurora <Felix.Duan> <2014-9-22> <END> Always show navigation bar on boot
    }

    // Aurora <Felix.Duan> <2014-9-22> <BEGIN> Always show navigation bar on boot
    /**
     * Show navigation bar on boot.
     * TODO should merge same code segment in DelegateViewHelper
     */
    private void showNaviBarOnBoot(Context context) {
        final Resources res = context.getResources();
        //boolean hasNavigationBar = res.getBoolean(com.android.internal.R.bool.config_showNavigationBar);
        // Aurora <Felix.Duan> <2014-12-5> <BEGIN> Add Huawei Honor 6 to navigation bar device list
        boolean hasNavigationBar = Utils.hasNavBar();
        // Aurora <Felix.Duan> <2014-12-5> <END> Add Huawei Honor 6 to navigation bar device list

        if (!hasNavigationBar) return;

        ContentValues values = new ContentValues();
        values.put("name", NAVI_KEY_HIDE);
        values.put("value", 0);
        ContentResolver cr = context.getContentResolver();
        cr.insert(Settings.System.CONTENT_URI, values);
    }
    // Aurora <Felix.Duan> <2014-9-22> <END> Always show navigation bar on boot
}
