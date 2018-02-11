/*
**
** Copyright 2007, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/
package com.android.packageinstaller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraAlertActivity;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraCheckBox;

import java.util.List;

import com.android.packageinstaller.R;
import com.android.packageinstaller.UninstallAppProgress.PackageDeleteObserver;
//import com.android.packageinstaller.com;

/*
 * This activity presents UI to uninstall an application. Usually launched with intent
 * Intent.ACTION_UNINSTALL_PKG_COMMAND and attribute 
 * com.android.packageinstaller.PackageName set to the application package name
 */
public class UninstallerAlertActivity extends AuroraAlertActivity implements OnClickListener,
        DialogInterface.OnCancelListener {
    private static final String TAG = "UninstallerActivity";
    /// M: [ALPS00287901] [Rose][ICS][MT6577][Free Test][APPIOT]The "Open" will be displayed in gray after you install one apk twice.(5/5) @{
    private boolean localLOGV = true;
    /// @}
    PackageManager mPm;
    private ApplicationInfo mAppInfo;
    private CharSequence appLable;
    private Button mOk;
    private Button mCancel;
    private AuroraCheckBox checkbox_clear;

    // Dialog identifiers used in showDialog
    private static final int DLG_BASE = 0;
    private static final int DLG_APP_NOT_FOUND = DLG_BASE + 1;
    private static final int DLG_UNINSTALL_FAILED = DLG_BASE + 2;

    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {
        case DLG_APP_NOT_FOUND :
        	 return new AuroraAlertDialog.Builder(this)
                    .setTitle(R.string.app_not_found_dlg_title)
                    .setIcon(com.android.internal.R.drawable.ic_dialog_alert)
                    .setMessage(R.string.app_not_found_dlg_text)
                    .setNeutralButton(getString(R.string.dlg_ok), 
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    setResult(Activity.RESULT_FIRST_USER);
                                    finish();
                                }})
                    .create();
        case DLG_UNINSTALL_FAILED :
            // Guaranteed not to be null. will default to package name if not set by app
           CharSequence appTitle = mPm.getApplicationLabel(mAppInfo);
           String dlgText = getString(R.string.uninstall_failed_msg,
                    appTitle.toString());
            // Display uninstall failed dialog
           return new AuroraAlertDialog.Builder(this)
                    .setTitle(R.string.uninstall_failed)
                    .setIcon(com.android.internal.R.drawable.ic_dialog_alert)
                    .setMessage(dlgText)
                    .setNeutralButton(getString(R.string.dlg_ok), 
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    setResult(Activity.RESULT_FIRST_USER);
                                    finish();
                                }})
                    .create();
        }
        return null;
    }

    private void startUninstallProgress() {
    	if(checkbox_clear.isChecked() && appLable != null){
    		DeepClearModel.getInstance(this).scanForUnInstall(appLable.toString());
    	}
    	
    	PackageDeleteObserver observer = new PackageDeleteObserver();
        getPackageManager().deletePackage(mAppInfo.packageName, observer, 0);
        
//        finish();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // Get intent information.
        // We expect an intent with URI of the form package://<packageName>#<className>
        // className is optional; if specified, it is the activity the user chose to uninstall
        final Intent intent = getIntent();
        Uri packageURI = intent.getData();
        String packageName = packageURI.getEncodedSchemeSpecificPart();
        if(packageName == null) {
            Log.e(TAG, "Invalid package name:" + packageName);
            showDialog(DLG_APP_NOT_FOUND);
            return;
        }

        mPm = getPackageManager();
        boolean errFlag = false;
        try {
            mAppInfo = mPm.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
        } catch (NameNotFoundException e) {
            errFlag = true;
        }

        // The class name may have been specified (e.g. when deleting an app from all apps)
        String className = packageURI.getFragment();
        ActivityInfo activityInfo = null;
        if (className != null) {
            try {
                activityInfo = mPm.getActivityInfo(new ComponentName(packageName, className), 0);
            } catch (NameNotFoundException e) {
                errFlag = true;
            }
        }

        if (mAppInfo == null || errFlag) {
            Log.e(TAG, "Invalid packageName or componentName in " + packageURI.toString());
            showDialog(DLG_APP_NOT_FOUND);
        } else {
            boolean isUpdate = ((mAppInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
            
            View view = LayoutInflater.from(this).inflate(R.layout.alert_dialog_uninstall_confirm, null);
            checkbox_clear = (AuroraCheckBox)view.findViewById(R.id.checkbox_clear);
//            checkbox_clear.setText(R.string.clear_residual_files);
            
            TextView clearText = (TextView)view.findViewById(R.id.clearText);
            clearText.setText(R.string.clear_residual_files);
            clearText.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(checkbox_clear != null){
						checkbox_clear.setChecked(checkbox_clear.isChecked()?false:true);
					}
				}
			});
            
            appLable = mAppInfo.loadLabel(getPackageManager());
            
            if (activityInfo != null) {
                CharSequence activityLabel = activityInfo.loadLabel(mPm);
                Log.i(TAG, "activityLabel: " + activityLabel);
                if (!activityLabel.equals(mAppInfo.loadLabel(mPm))) {
                    CharSequence text = getString(R.string.uninstall_activity_text, activityLabel);
                    Log.i(TAG, "text: " + text);
                }
            }
            
            if (isUpdate) {
            	mAlertParams.mTitle = getString(R.string.uninstall_confirm);
            	mAlertParams.mMessage = getString(R.string.uninstall_update_text_by_name, appLable);
//            	mAlertParams.mMessage.setText(getString(R.string.uninstall_update_text_by_name, appLable));
            } else {
            	mAlertParams.mTitle = getString(R.string.uninstall_confirm);
            	mAlertParams.mMessage = getString(R.string.uninstall_application_text_by_name, appLable);
//            	message.setText(getString(R.string.uninstall_application_text_by_name, appLable));
            }
            
            mAlertParams.mView = view; //制定改activity的view
            mAlertParams.mPositiveButtonText = getString(R.string.ok);//确定按钮显示的文字
            mAlertParams.mNegativeButtonText = getString(R.string.cancel);//取消按钮显示的文字
            mAlertParams.mPositiveButtonListener = new OnClickListener() {//确定按钮的事件监听
                
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    // TODO Auto-generated method stub
                	//initiate next screen
                    startUninstallProgress();
                }
            };
            mAlertParams.mNegativeButtonListener = new OnClickListener() {//取消按钮的事件监听
                
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    // TODO Auto-generated method stub
                    finish();
                }
            };
            setupAlert();//该方法将以上设置绑定到activity中，如果不调用该方法，什么也显示不出来
        }
    }
    
    @Override
	public void finish() {
		super.finish();
		overridePendingTransition(0, R.anim.alert_activity_out);
	}
    
    public void onClick(View v) {
        if(v == mOk) {
            //initiate next screen
            startUninstallProgress();
        } else if(v == mCancel) {
            finish();
        }
    }

    public void onCancel(DialogInterface dialog) {
        finish();
    }
    
    class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
        public void packageDeleted(String packageName, int returnCode) {
            Message msg = mHandler.obtainMessage(UNINSTALL_COMPLETE);
            msg.arg1 = returnCode;
            msg.obj = packageName;
            mHandler.sendMessage(msg);
        }
    }
    
    private final int UNINSTALL_COMPLETE = 1;
    private boolean mGnAppPermCtrl = SystemProperties.get("ro.gn.app.perm.ctrl").equals("yes");
    
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UNINSTALL_COMPLETE:
                    if (getIntent().getBooleanExtra(Intent.EXTRA_RETURN_RESULT, false)) {
                        Intent result = new Intent();
                        result.putExtra(Intent.EXTRA_INSTALL_RESULT, msg.arg1);
                        //huangbin hide begin
//                        setResult(msg.arg1 == PackageManager.DELETE_SUCCEEDED
//                                ? AmigoActivity.RESULT_OK : AmigoActivity.RESULT_FIRST_USER,
//                                        result);
                        //huangbin hide end
                       
                        //huangbin add begin
                      setResult(msg.arg1 == PackageManager.DELETE_SUCCEEDED
                      ? Activity.RESULT_OK : Activity.RESULT_FIRST_USER,
                              result);
                        //huangbin add end
                        
                        finish();
                        return;
                    }

                    final String packageName = (String) msg.obj;

                    // Update the status text
                    final int statusText;
                    switch (msg.arg1) {
                        case PackageManager.DELETE_SUCCEEDED:
                            statusText = R.string.uninstall_done;
                            //Gionee qiuxd 20121018 modify for CR00711865 start
                            if(mGnAppPermCtrl){
//                                deletePermByPkgName(packageName);
                            }
                            //Gionee qiuxd 20121018 modify for CR00711865 end
                            break;
                        case PackageManager.DELETE_FAILED_DEVICE_POLICY_MANAGER:
                            Log.d(TAG, "Uninstall failed because " + packageName
                                    + " is a device admin");
//                            mDeviceManagerButton.setVisibility(View.VISIBLE);
                            statusText = R.string.uninstall_failed_device_policy_manager;
                            break;
                        default:
                            Log.d(TAG, "Uninstall failed for " + packageName + " with code "
                                    + msg.arg1);
                            statusText = R.string.uninstall_failed;
                            break;
                    }
                	Toast.makeText(UninstallerAlertActivity.this, 
                			appLable + getString(statusText), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
    
}
