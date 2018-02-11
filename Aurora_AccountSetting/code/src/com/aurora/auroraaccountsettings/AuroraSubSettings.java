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

package com.aurora.auroraaccountsettings;


import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Stub class for showing sub-settings; we can't use the main Settings class
 * since for our app it is a special singleTask class.
 */
public class AuroraSubSettings extends AccountActivity {

    @Override
    public boolean onNavigateUp() {
        finish();
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (ManageAccountsSettings.isManageAccount) {
                    Log.i("qy", "KeyEvent.KEYCODE_BACK");
                    Intent intent = new Intent("com.aurora.auroraaccountsettings.action.ACCOUNT_ACTIVITY");
                    startActivity(intent);
                    overridePendingTransition(R.anim.aurora_close_enter, R.anim.aurora_close_exit);
                    finish();
                } else {
                    finish();
                    overridePendingTransition(R.anim.aurora_close_enter, R.anim.aurora_close_exit);
//    	    		overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                return true;
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }
}
