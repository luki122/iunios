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
package com.aurora.wechat;

import com.android.settings.R;
import com.android.settings.WechatSet;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemProperties;
import android.util.Log;
import android.view.WindowManager;

public class InstallShortcutReceiver extends BroadcastReceiver {
	static boolean DI_ON = SystemProperties.get("ro.gn.amigo.clone.support","no").equals("yes");
	@Override
	public void onReceive(Context context, Intent data) {
		if(DI_ON){
			Log.d("vulcan-db", "onReceive: data = " + data);
			String packageName = data.getDataString();
			Log.d("vulcan-db", packageName);
			if (packageName.endsWith("com.tencent.mm")) {
				Log.d("vulcan-db", data.getAction());
				if (data.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
					Log.d("vulcan-db", "showDialog");
					showDialog(context);
				} else if (data.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
					Log.d("vulcan-db", "setComponentEnabledSetting");
					context.getPackageManager().setComponentEnabledSetting(new ComponentName("com.tencent.mm.clone", "com.tencent.mm.clone.MainActivity"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED , 0);
				}
			}
		}
	}

	private void showDialog(final Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.wechat_install_title)
				.setMessage(R.string.wechat_install_message)
				.setCancelable(false)
				.setPositiveButton(R.string.submit,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								/*Intent intent = new Intent(context, WechatSet.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								context.startActivity(intent);*/
								context.getPackageManager().setComponentEnabledSetting(new ComponentName("com.tencent.mm.clone", "com.tencent.mm.clone.MainActivity"), PackageManager.COMPONENT_ENABLED_STATE_ENABLED , 0);
							}
						})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		alert.show();
	}
}
