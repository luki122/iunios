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

import com.android.packageinstaller.R;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

//Gionee qiuxd 20121015 add for CR00711865 start
import android.net.Uri;
import android.os.SystemProperties;
//Gionee qiuxd 20121015 add for CR00711865 end
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

/**
 * This activity corresponds to a download progress screen that is displayed 
 * when an application is uninstalled. The result of the application uninstall
 * is indicated in the result code that gets set to 0 or 1. The application gets launched
 * by an intent with the intent's class name explicitly set to UninstallAppProgress and expects
 * the application object of the application to uninstall.
 */
public class UninstallAppProgress extends AuroraActivity implements OnClickListener {
    private final String TAG="UninstallAppProgress";
    /// M: [ALPS00287901] [Rose][ICS][MT6577][Free Test][APPIOT]The "Open" will be displayed in gray after you install one apk twice.(5/5) @{
    private boolean localLOGV = true;
    /// @}
    private ApplicationInfo mAppInfo;
    private TextView mStatusTextView;
    private Button mOkButton;
    private Button mDeviceManagerButton;
    private ProgressBar mProgressBar;
    private View mOkPanel;
    private volatile int mResultCode = -1;
    private final int UNINSTALL_COMPLETE = 1;
    public final static int SUCCEEDED=1;
    public final static int FAILED=0;
    
    //Gionee qiuxd 20121017 add for CR00711865 start
    private boolean mGnAppPermCtrl = SystemProperties.get("ro.gn.app.perm.ctrl").equals("yes");
    private static final Uri GN_PERM_URI = Uri.parse("content://com.gionee.settings.PermissionProvider/permissions");
    //Gionee qiuxd 20121017 add for CR00711865 end
    
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

                    mResultCode = msg.arg1;
                    final String packageName = (String) msg.obj;

                    // Update the status text
                    final int statusText;
                    switch (msg.arg1) {
                        case PackageManager.DELETE_SUCCEEDED:
                            statusText = R.string.uninstall_done;
                            //Gionee qiuxd 20121018 modify for CR00711865 start
                            if(mGnAppPermCtrl){
                                deletePermByPkgName(packageName);
                            }
                            //Gionee qiuxd 20121018 modify for CR00711865 end
                            break;
                        case PackageManager.DELETE_FAILED_DEVICE_POLICY_MANAGER:
                            Log.d(TAG, "Uninstall failed because " + packageName
                                    + " is a device admin");
                            mDeviceManagerButton.setVisibility(View.VISIBLE);
                            statusText = R.string.uninstall_failed_device_policy_manager;
                            break;
                        default:
                            Log.d(TAG, "Uninstall failed for " + packageName + " with code "
                                    + msg.arg1);
                            statusText = R.string.uninstall_failed;
                            break;
                    }
                    mStatusTextView.setText(statusText);

                    // Hide the progress bar; Show the ok button
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mOkPanel.setVisibility(View.VISIBLE);
                    findViewById(R.id.line).setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle icicle) {
        //Gionee qiuxd 2013117 add for CR00765187 start
//        String theme = PackageUtil.getThemeType(getApplicationContext());
//        if (theme.equals(PackageUtil.TYPE_LIGHT_THEME)) {
//            setTheme(R.style.GN_Perm_Ctrl_Theme_light);
//        } else if(theme.equals(PackageUtil.TYPE_DARK_THEME)){
//            setTheme(R.style.GN_Perm_Ctrl_Theme_dark);
//        }
        //Gionee qiuxd 2013117 add for CR00765187 end
        super.onCreate(icicle);
        Intent intent = getIntent();
        mAppInfo = intent.getParcelableExtra(PackageUtil.INTENT_ATTR_APPLICATION_INFO);
        initView();

        //huangbin hide begin
//        AmigoActionBar actionBar = getAmigoActionBar();
//        if(null != actionBar){
//            actionBar.setDisplayShowHomeEnabled(false);
//        }
        //huangbin hide end
        
//        AuroraActionBar actionBar = getAuroraActionBar();
//        if(null != actionBar){
//        	actionBar.getHomeButton().setVisibility(View.GONE);
//        }
    }
    
    class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
        public void packageDeleted(String packageName, int returnCode) {
            Message msg = mHandler.obtainMessage(UNINSTALL_COMPLETE);
            msg.arg1 = returnCode;
            msg.obj = packageName;
            mHandler.sendMessage(msg);
        }
    }
    
    void setResultAndFinish(int retCode) {
        setResult(retCode);
        finish();
    }
    
    public void initView() {
        boolean isUpdate = ((mAppInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
        setTitle(isUpdate ? R.string.uninstall_update_title : R.string.uninstall_application_title);

       setContentView(R.layout.uninstall_progress);//huang hide
      //huang add begin
//    	setAuroraContentView(R.layout.uninstall_progress,AuroraActionBar.Type.Normal);
//        getAuroraActionBar().setTitle(R.string.uninstall_application_title);
//        getAuroraActionBar().getHomeButton().setVisibility(View.GONE);
    	//huangbin add end
        // Initialize views
        View snippetView = findViewById(R.id.app_snippet);
        PackageUtil.initSnippetForInstalledApp(this, mAppInfo, snippetView);
        mStatusTextView = (TextView) findViewById(R.id.center_text);
        mStatusTextView.setText(R.string.uninstalling);
        mDeviceManagerButton = (Button) findViewById(R.id.device_manager_button);
        mDeviceManagerButton.setVisibility(View.GONE);
        mDeviceManagerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName("com.android.settings",
                        "com.android.settings.Settings$DeviceAdminSettingsActivity");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setIndeterminate(true);
        findViewById(R.id.line).setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        // Hide button till progress is being displayed
        mOkPanel = (View) findViewById(R.id.ok_panel);
        mOkButton = (Button) findViewById(R.id.ok_button);
        mOkButton.setOnClickListener(this);
        mOkPanel.setVisibility(View.INVISIBLE);
        PackageDeleteObserver observer = new PackageDeleteObserver();
        getPackageManager().deletePackage(mAppInfo.packageName, observer, 0);
    }

    public void onClick(View v) {
        if(v == mOkButton) {
            Log.i(TAG, "Finished uninstalling pkg: " + mAppInfo.packageName);
            setResultAndFinish(mResultCode);
        }
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent ev) {
        if (ev.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (mResultCode == -1) {
                // Ignore back key when installation is in progress
                return true;
            } else {
                // If installation is done, just set the result code
                setResult(mResultCode);
            }
        }
        return super.dispatchKeyEvent(ev);
    }
    
    //Gionee qiuxd 20121018 modify for CR00711865 start
    private void deletePermByPkgName(String packageName){
    	//huangbin hide
       // getContentResolver().delete(GN_PERM_URI, " packagename = ?", new String[]{packageName});
    }
    //Gionee qiuxd 20121018 modify for CR00711865 end
}
