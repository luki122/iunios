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

import aurora.app.AuroraAlertActivity;
import android.content.DialogInterface;

/**
 * AlretDialog used for DISPLAY TEXT commands.
 *
 */
public class SetSubscriptionDialog extends AuroraAlertActivity {
	// members
	private AuroraDialogTitle mTitle;

	// keys) for saving the state of the dialog in the icicle
	private static final String TEXT = "text";

	// message id for time out
	private static final int MSG_ID_TIMEOUT = 1;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		mAlertParams.mTitle = getString(R.string.config_sub_title);// Dialog 的标题
		mAlertParams.mMessage = getString(R.string.new_cards_available);// Dialog 的 MSG
		mAlertParams.mPositiveButtonText=getString(R.string.ok);//确定按钮的文本
        mAlertParams.mNegativeButtonText=getString(R.string.cancel);//取消按钮的文本
		mAlertParams.mPositiveButtonListener = new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				Intent setSubscriptionIntent = new Intent(Intent.ACTION_MAIN);
				setSubscriptionIntent.setClassName("com.android.phone",
						"com.android.phone.MSimMobileNetworkSettingsV2");
				// aurora modify liguangyu 20140819 for BUG #7694 start
				// setSubscriptionIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				setSubscriptionIntent.putExtra("new", true);
				// aurora modify liguangyu 20140819 for BUG #7694 end
				setSubscriptionIntent
						.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
				setSubscriptionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(setSubscriptionIntent);
				finish();
			}
		};
		// 取消按钮的监听器
		mAlertParams.mNegativeButtonListener = new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				finish();
			}
		};
		// 最后调用这个方法添加 View 到 Activity 中
		setupAlert();

	}


}
