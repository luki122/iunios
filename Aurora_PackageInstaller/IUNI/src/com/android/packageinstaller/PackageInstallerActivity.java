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
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageParser;
import android.content.pm.PermissionInfo;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
//Gionee qiuxd 20121015 add for CR00711865 start
import android.content.pm.PackageInfo;
import android.os.SystemProperties;
import android.content.ContentValues;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;

import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
//Gionee qiuxd 20121015 add for CR00711865 end
import com.android.data.MyPermissionInfo;
import com.android.packageinstaller.R;
import com.android.utils.ApkUtils;
import com.android.utils.AppSecurityPermissions;
import com.android.utils.ExpandableListViewUtils;

import android.content.pm.PackageUserState;



/*
 * This activity is launched when a new application is installed via side loading
 * The package is first parsed and the user is notified of parse errors via a dialog.
 * If the package is successfully parsed, the user is notified to turn on the install unknown
 * applications setting. A memory check is made at this point and the user is notified of out
 * of memory conditions if any. If the package is already existing on the device, 
 * a confirmation dialog (to replace the existing package) is presented to the user.
 * Based on the user response the package is then installed by launching InstallAppConfirm
 * sub activity. All state transitions are handled in this activity
 */
public class PackageInstallerActivity extends AuroraActivity implements OnCancelListener, OnClickListener {
    private static final String TAG = "PackageInstaller";
    private Uri mPackageURI;    
    /// M: [ALPS00287901] [Rose][ICS][MT6577][Free Test][APPIOT]The "Open" will be displayed in gray after you install one apk twice.(5/5) @{
    private boolean localLOGV = true;
    /// @}
    PackageManager mPm;
//    PackageParser.Package mPkgInfo;//huangbin hide
    PackageInfo mPkgInfo;//huangbin add
    ApplicationInfo mSourceInfo;

    // ApplicationInfo object primarily used for already existing applications
    private ApplicationInfo mAppInfo = null;

    // View for install progress
    View mInstallConfirm;
    // Buttons to indicate user acceptance
    private Button mOk;
    private Button mCancel;

    static final String PREFS_ALLOWED_SOURCES = "allowed_sources";
    //Gionee qiuxd 20121015 add for CR00711865 start
    private boolean mGnAppPermCtrl = SystemProperties.get("ro.gn.app.perm.ctrl").equals("yes");
//    private GNAppSecurityPermissions mGnAsp;
    private boolean mIsReplacing = false;
    private static final Uri GN_PERM_URI = Uri.parse("content://com.gionee.settings.PermissionProvider/permissions");
    //Gionee qiuxd 20121015 add for CR00711865 end
    
//    private AppSecurityPermissions asp;//huangbin add

    // Dialog identifiers used in showDialog
    private static final int DLG_BASE = 0;
    private static final int DLG_REPLACE_APP = DLG_BASE + 1;
    private static final int DLG_UNKNOWN_APPS = DLG_BASE + 2;
    private static final int DLG_PACKAGE_ERROR = DLG_BASE + 3;
    private static final int DLG_OUT_OF_SPACE = DLG_BASE + 4;
    private static final int DLG_INSTALL_ERROR = DLG_BASE + 5;
    private static final int DLG_ALLOW_SOURCE = DLG_BASE + 6;

    private ExpandableListViewUtils expandableListViewUtils;
    public static boolean isFirstInstall ;
    public static AppSecurityPermissions appSecurityPermissions;

    private void startInstallConfirm() {
        if(mPkgInfo != null) {         
            if(expandableListViewUtils == null){
            	appSecurityPermissions = new AppSecurityPermissions(this, mPkgInfo);
            	expandableListViewUtils = new ExpandableListViewUtils(this,
            			appSecurityPermissions,
            			isFirstInstall,true);
            }
        }

        mInstallConfirm.setVisibility(View.VISIBLE);
        mOk = (Button)findViewById(R.id.ok_button);
        mCancel = (Button)findViewById(R.id.cancel_button);
        mOk.setOnClickListener(this);
        mCancel.setOnClickListener(this);
    }

    private void showDialogInner(int id) {
        // TODO better fix for this? Remove dialog so that it gets created again
        removeDialog(id);
        showDialog(id);
    }

    @Override
    public Dialog onCreateDialog(int id, Bundle bundle) {
        switch (id) {
        case DLG_REPLACE_APP:
            int msgId = R.string.dlg_app_replacement_statement;
            // Customized text for system apps
            if ((mAppInfo != null) && (mAppInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                msgId = R.string.dlg_sys_app_replacement_statement;
            }
            //Gionee qiuxd 20121226 modify for CR00742778 add Dialog style
            return new AuroraAlertDialog.Builder(this)
            //return new AmigoAlertDialog.Builder(this,AmigoAlertDialog.THEME_AMIGO_FULLSCREEN)
                    .setTitle(R.string.dlg_app_replacement_title)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Gionee qiuxd 20121015 add for CR00711865 start
                            mIsReplacing = true;
                            //Gionee qiuxd 20121015 add for CR00711865 end
                            startInstallConfirm();
                        }})
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "Canceling installation");
                            setResult(RESULT_CANCELED);
                            finish();
                        }})
                    .setMessage(msgId)
                    .setOnCancelListener(this)
                    .create();
        case DLG_UNKNOWN_APPS:
             //Gionee qiuxd 20121226 modify for CR00742778 add Dialog style
            return new AuroraAlertDialog.Builder(this)
            //return new AmigoAlertDialog.Builder(this,AmigoAlertDialog.THEME_AMIGO_FULLSCREEN)
                    .setTitle(R.string.unknown_apps_dlg_title)
                    .setMessage(R.string.unknown_apps_dlg_text)
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "Finishing off activity so that user can navigate to settings manually");
                            finish();
                        }})
                    // Gionee lihq modify for CR00754539 start
                    //.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                    // Gionee lihq modify for CR00754539 end
                    .setPositiveButton(R.string.gn_unlock, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "Launching settings");
                            // Gionee lihq modify for CR00754539 start
                            // launchSettingsAppAndFinish();
                            Settings.Secure.putInt(getContentResolver(),
                                Settings.Secure.INSTALL_NON_MARKET_APPS, 1);
                            initiateInstall();
                            // Gionee lihq modify for CR00754539 end
                        }
                    })
                    .setOnCancelListener(this)
                    .create(); 
        case DLG_PACKAGE_ERROR :
            //Gionee qiuxd 20121226 modify for CR00742778 add Dialog style
            return new AuroraAlertDialog.Builder(this)
            //return new AmigoAlertDialog.Builder(this,AmigoAlertDialog.THEME_AMIGO_FULLSCREEN)
                    .setTitle(R.string.Parse_error_dlg_title)
                    .setMessage(R.string.Parse_error_dlg_text)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setOnCancelListener(this)
                    .create();
        case DLG_OUT_OF_SPACE:
            // Guaranteed not to be null. will default to package name if not set by app
            CharSequence appTitle = mPm.getApplicationLabel(mPkgInfo.applicationInfo);
            String dlgText = getString(R.string.out_of_space_dlg_text, 
                    appTitle.toString());
            //Gionee qiuxd 20121226 modify for CR00742778 add Dialog style
            return new AuroraAlertDialog.Builder(this)
            //return new AmigoAlertDialog.Builder(this,AmigoAlertDialog.THEME_AMIGO_FULLSCREEN)
                    .setTitle(R.string.out_of_space_dlg_title)
                    .setMessage(dlgText)
                    .setPositiveButton(R.string.manage_applications, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //launch manage applications
//                            Intent intent = new Intent("android.intent.action.MANAGE_PACKAGE_STORAGE");
                        	Intent intent = new Intent();
                        	intent.setClassName("com.aurora.secure",
                        			"com.secure.activity.CacheManageActivity");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);   
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "Canceling installation");
                            finish();
                        }
                  })
                  .setOnCancelListener(this)
                  .create();
        case DLG_INSTALL_ERROR :
            // Guaranteed not to be null. will default to package name if not set by app
            CharSequence appTitle1 = mPm.getApplicationLabel(mPkgInfo.applicationInfo);
            String dlgText1 = getString(R.string.install_failed_msg,
                    appTitle1.toString());
            //Gionee qiuxd 20121226 modify for CR00742778 add Dialog style
            return new AuroraAlertDialog.Builder(this)
            //return new AmigoAlertDialog.Builder(this,AmigoAlertDialog.THEME_AMIGO_FULLSCREEN)
                    .setTitle(R.string.install_failed)
                    .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setMessage(dlgText1)
                    .setOnCancelListener(this)
                    .create();
        case DLG_ALLOW_SOURCE:
            CharSequence appTitle2 = mPm.getApplicationLabel(mSourceInfo);
            String dlgText2 = getString(R.string.allow_source_dlg_text,
                    appTitle2.toString());
            //Gionee qiuxd 20121226 modify for CR00742778 add Dialog style
            return new AuroraAlertDialog.Builder(this)
            //return new AmigoAlertDialog.Builder(this,AmigoAlertDialog.THEME_AMIGO_FULLSCREEN)
                    .setTitle(R.string.allow_source_dlg_title)
                    .setMessage(dlgText2)
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(RESULT_CANCELED);
                            finish();
                        }})
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences prefs = getSharedPreferences(PREFS_ALLOWED_SOURCES,
                                    Context.MODE_PRIVATE);
                            prefs.edit().putBoolean(mSourceInfo.packageName, true).apply();
                            startInstallConfirm();
                        }
                    })
                    .setOnCancelListener(this)
                    .create();
       }
       return null;
   }

    private void launchSettingsAppAndFinish() {
        // Create an intent to launch SettingsTwo activity
        Intent launchSettingsIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
        launchSettingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(launchSettingsIntent);
        finish();
    }
    
    private boolean isInstallingUnknownAppsAllowed() {
        return Settings.Secure.getInt(getContentResolver(), 
            Settings.Secure.INSTALL_NON_MARKET_APPS, 0) > 0;
    }
    
    
   
    private void initiateInstall() {
        String pkgName = mPkgInfo.packageName;
        // Check if there is already a package on the device with this name
        // but it has been renamed to something else.
        String[] oldName = mPm.canonicalToCurrentPackageNames(new String[] { pkgName });
        if (oldName != null && oldName.length > 0 && oldName[0] != null) {
            pkgName = oldName[0];
            //mPkgInfo.setPackageName(pkgName);//huangbin hide
            
            //huangbin add begin
            mPkgInfo.packageName = pkgName;
            mPkgInfo.applicationInfo.packageName = pkgName;
            //huangbin add end
        }
        // Check if package is already installed. display confirmation dialog if replacing pkg
        try {
            mAppInfo = mPm.getApplicationInfo(pkgName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
        } catch (NameNotFoundException e) {
            mAppInfo = null;
        }
        
        if(mAppInfo == null){
        	isFirstInstall = true;
        }else{
        	isFirstInstall = false;
        }
        
        if (mAppInfo == null || getIntent().getBooleanExtra(Intent.EXTRA_ALLOW_REPLACE, false)) {
        	startInstallConfirm();
        } else {
            if(localLOGV) Log.i(TAG, "Replacing existing package:"+
                    mPkgInfo.applicationInfo.packageName);
            showDialogInner(DLG_REPLACE_APP);
        }
    }

    void setPmResult(int pmResult) {
        Intent result = new Intent();
        result.putExtra(Intent.EXTRA_INSTALL_RESULT, pmResult);
        setResult(pmResult == PackageManager.INSTALL_SUCCEEDED
                ? RESULT_OK : RESULT_FIRST_USER, result);
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // get intent information
        final Intent intent = getIntent();
        mPackageURI = intent.getData();
        mPm = getPackageManager();

        final String scheme = mPackageURI.getScheme();
        if (scheme != null && !"file".equals(scheme)) {
            throw new IllegalArgumentException("unexpected scheme " + scheme);
        }

        final File sourceFile = new File(mPackageURI.getPath());
        
        //huangbin add being
        if ("package".equals(mPackageURI.getScheme())) {
            try {
                mPkgInfo = mPm.getPackageInfo(mPackageURI.getSchemeSpecificPart(),
                        PackageManager.GET_PERMISSIONS | PackageManager.GET_UNINSTALLED_PACKAGES);
            } catch (NameNotFoundException e) {
            }
            if (mPkgInfo == null) {
                Log.w(TAG, "Requested package " + mPackageURI.getScheme()
                        + " not available. Discontinuing installation");
                showDialogInner(DLG_PACKAGE_ERROR);
                setPmResult(PackageManager.INSTALL_FAILED_INVALID_APK);
                return;
            }
        } else {
//            final File sourceFile = new File(mPackageURI.getPath());
            PackageParser.Package parsed = PackageUtil.getPackageInfo(sourceFile);

            // Check for parse errors
            if (parsed == null) {
                Log.w(TAG, "Parse error when parsing manifest. Discontinuing installation");
                showDialogInner(DLG_PACKAGE_ERROR);
                setPmResult(PackageManager.INSTALL_FAILED_INVALID_APK);
                return;
            }
            mPkgInfo = PackageParser.generatePackageInfo(parsed, null,
                    PackageManager.GET_PERMISSIONS, 0, 0, null,
                    new PackageUserState());
        }
        //huangbin add end

        // Check for parse errors
        if (mPkgInfo == null) {
            Log.w(TAG, "Parse error when parsing manifest. Discontinuing installation");
            showDialogInner(DLG_PACKAGE_ERROR);
            setPmResult(PackageManager.INSTALL_FAILED_INVALID_APK);
            return;
        }
        
//    	setAuroraContentView(R.layout.install_start,AuroraActionBar.Type.Normal);
//        getAuroraActionBar().setTitle(R.string.install);
//        getAuroraActionBar().getHomeButton().setVisibility(View.GONE);
        setContentView(R.layout.install_start);

        mInstallConfirm = findViewById(R.id.install_confirm_panel);
        mInstallConfirm.setVisibility(View.INVISIBLE);
        final PackageUtil.AppSnippet as = PackageUtil.getAppSnippet(
                this, mPkgInfo.applicationInfo, sourceFile);
        PackageUtil.initSnippetForNewApp(this, as, R.id.app_snippet);

        // Deal with install source.
        String callerPackage = getCallingPackage();
        if (callerPackage != null && intent.getBooleanExtra(
                Intent.EXTRA_NOT_UNKNOWN_SOURCE, false)) {
            try {
                mSourceInfo = mPm.getApplicationInfo(callerPackage, 0);
                if (mSourceInfo != null) {
                    if ((mSourceInfo.flags&ApplicationInfo.FLAG_SYSTEM) != 0) {
                        // System apps don't need to be approved.
                        initiateInstall();
                        return;
                    }
                }
            } catch (NameNotFoundException e) {
            }
        }

        // Check unknown sources.
        if (!isInstallingUnknownAppsAllowed()) {
            //ask user to enable setting first
            showDialogInner(DLG_UNKNOWN_APPS);
            return;
        }
        initiateInstall();
    }
    
    // Generic handling when pressing back key
    public void onCancel(DialogInterface dialog) {
        finish();
    }

    public void onClick(View v) {
        if(v == mOk) {
            // Start subactivity to actually install the application
            Intent newIntent = new Intent();
            newIntent.putExtra(PackageUtil.INTENT_ATTR_APPLICATION_INFO,
                    mPkgInfo.applicationInfo);
            newIntent.setData(mPackageURI);
            newIntent.setClass(this, InstallAppProgress.class);
            String installerPackageName = getIntent().getStringExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME);
            if (installerPackageName != null) {
                newIntent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, installerPackageName);
            }
            if (getIntent().getBooleanExtra(Intent.EXTRA_RETURN_RESULT, false)) {
                newIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
                newIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            }
            if(localLOGV) Log.i(TAG, "downloaded app uri="+mPackageURI);
            //Gionee qiuxd 20121015 add for CR00711865 start
            if (mGnAppPermCtrl) {
            	//huangbin hide begin
//                if(mGnAsp != null){
//                    Map<String, Boolean> permMap = mGnAsp.getPermissionsStatus();
            	//huangbin hide end
            	
            	//huangbin add begin
  //          	if(asp != null){
                  Map<String, Boolean> permMap = null;// asp.getPermissionsStatus();
                  //huangbin add end
            		
                    if (permMap != null) {
                        int size = permMap.size();
                        String[] keys = new String[size];
                        int[] values = new int[size];

                        int i = 0;
                        Iterator iterator = permMap.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Entry entry = (Entry) iterator.next();
                            String key = (String) entry.getKey();
                            boolean value = (Boolean) entry.getValue();
                            keys[i] = key;
                            values[i] = value ? 1 : 0;
                            i++;
                        }
                        
                        Bundle bundle = new Bundle();
                        
                        bundle.putStringArray("permKeys", keys);
                        bundle.putIntArray("permValues", values);
                        bundle.putBoolean("isReplacing", mIsReplacing);
                        
                        newIntent.putExtras(bundle);
                    }
//                }
            }
            //Gionee qiuxd 20121015 add for CR00711865 end
            startActivity(newIntent);
            finish();
        } else if(v == mCancel) {
            // Cancel and finish
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    /**
     * It's a ScrollView that knows how to stay awake.
     */
    static class CaffeinatedScrollView extends ScrollView {
        public CaffeinatedScrollView(Context context) {
            super(context);
        }

        /**
         * Make this visible so we can call it
         */
        @Override
        public boolean awakenScrollBars() {
            return super.awakenScrollBars();
        }
    }
}
