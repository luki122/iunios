/*
 * Copyright (C) 2007 The Android Open Source Project
 * Copyright (c) 2009,2012-2013 The Linux Foundation. All rights reserved.
 * Not a Contribution.
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
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.System;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import com.aurora.internal.widget.AuroraDialogTitle;
/**
 * AlretDialog used for DISPLAY TEXT commands.
 *
 */
public class SetSubscriptionDialog extends Activity implements View.OnClickListener {
    // members
    private AuroraDialogTitle mTitle;



    //keys) for saving the state of the dialog in the icicle
    private static final String TEXT = "text";

    // message id for time out
    private static final int MSG_ID_TIMEOUT = 1;

    // buttons id
    public static final int OK_BUTTON = R.id.button_ok;
    public static final int CANCEL_BUTTON = R.id.button_cancel;

    @Override
    protected void onCreate(Bundle icicle) {    	
        super.onCreate(icicle);
        
        requestWindowFeature(Window.FEATURE_LEFT_ICON);

        setContentView(R.layout.aurora_stk_msg_dialog);
        mTitle = (AuroraDialogTitle) findViewById(R.id.aurora_alertTitle);
        Button okButton = (Button) findViewById(R.id.button_ok);
        Button cancelButton = (Button) findViewById(R.id.button_cancel);

        okButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

    }

    public void onClick(View v) {
         switch (v.getId()) {
             case OK_BUTTON:
            	 Intent setSubscriptionIntent = new Intent(Intent.ACTION_MAIN);
                 setSubscriptionIntent.setClassName("com.android.phone",
                         "com.android.phone.SetSubscription");
           	     //aurora modify liguangyu 20140819 for BUG #7694 start
//                 setSubscriptionIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                 setSubscriptionIntent.putExtra("new", true);
           	     //aurora modify liguangyu 20140819 for BUG #7694 end
                 setSubscriptionIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                 setSubscriptionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 startActivity(setSubscriptionIntent);
                 break;
             case CANCEL_BUTTON:
                 finish();
                 break;
         }
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            finish();
            break;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();


        Window window = getWindow();

        TextView mMessageView = (TextView) window
                .findViewById(R.id.dialog_message);

//        setTitle(mTextMsg.title);
        mTitle.setText(R.string.config_sub_title);
        
        mMessageView.setText(R.string.new_cards_available);

//        if (mTextMsg.icon == null) {
//            window.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
//                    com.android.internal.R.drawable.stat_notify_sim_toolkit);
//        } else {
//            window.setFeatureDrawable(Window.FEATURE_LEFT_ICON,
//                    new BitmapDrawable(mTextMsg.icon));
//        }

    }

    
}
