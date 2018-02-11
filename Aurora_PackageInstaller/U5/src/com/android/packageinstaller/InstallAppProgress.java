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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.drawable.LevelListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import com.android.packageinstaller.R;
import com.android.utils.ExpandableListViewUtils;

import com.aurora.utils.SystemUtils;

//Gionee qiuxd 20121015 add for CR00711865 start
import android.database.Cursor;
import android.content.ContentValues;
import android.os.SystemProperties;
import android.content.ContentResolver;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
//Gionee qiuxd 20121015 add for CR00711865 end

/**
 * This activity corresponds to a download progress screen that is displayed 
 * when the user tries
 * to install an application bundled as an apk file. The result of the application install
 * is indicated in the result code that gets set to the corresponding installation status
 * codes defined in PackageManager. If the package being installed already exists,
 * the existing package is replaced with the new one.
 */
public class InstallAppProgress extends AuroraActivity implements View.OnClickListener, OnCancelListener {
    private final String TAG="InstallAppProgress";
    /// M: [ALPS00287901] [Rose][ICS][MT6577][Free Test][APPIOT]The "Open" will be displayed in gray after you install one apk twice.(5/5) @{
    private boolean localLOGV = true;
    /// @}
    private ApplicationInfo mAppInfo;
    private Uri mPackageURI;
    private ProgressBar mProgressBar;
    private View mOkPanel;
    private TextView mStatusTextView;
//    private TextView mExplanationTextView;
    private Button mDoneButton;
    private Button mLaunchButton;
    private final int INSTALL_COMPLETE = 1;
    private Intent mLaunchIntent;
    private static final int DLG_OUT_OF_SPACE = 1;
    private CharSequence mLabel;
    private LinearLayout contentLayout;
    private View spaceView;
    
    //Gionee qiuxd 20121017 add for CR00711865 start
    private boolean mGnAppPermCtrl = SystemProperties.get("ro.gn.app.perm.ctrl").equals("yes");
    
    //huangbin hide begin
//    private static final Uri GN_PERM_URI = Uri.parse("content://com.gionee.settings.PermissionProvider/permissions");
    //huangbin hide end
    
    private boolean mIsReplacing = false;
    private String[] mPermKeys;
    private int[] mPermValues;
    
    private String mTheme;
    //Gionee qiuxd 20121017 add for CR00711865 end
    
    // Linxiaobin add 20140429 begin
    private BroadcastReceiver mBroadcastReceiver;
    // Linxiaobin add 20140429 end
    
    private ExpandableListViewUtils expandableListViewUtils;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INSTALL_COMPLETE:
                    if (getIntent().getBooleanExtra(Intent.EXTRA_RETURN_RESULT, false)) {
                        Intent result = new Intent();
                        result.putExtra(Intent.EXTRA_INSTALL_RESULT, msg.arg1);
                        setResult(msg.arg1 == PackageManager.INSTALL_SUCCEEDED
                                ? Activity.RESULT_OK : Activity.RESULT_FIRST_USER,
                                        result);
                        finish();
                        Log.i(TAG,"run in Intent.EXTRA_RETURN_RESULT, false");
                        return;
                    }
                    
                    //Gionee qiuxd 20121018 add for CR00711865 start
                    if(mGnAppPermCtrl){
                        if(msg.arg1 == PackageManager.INSTALL_SUCCEEDED ){
                            operatePermissionDB();
                        }
                    }
                    //Gionee qiuxd 20121018 add for CR00711865 end
                    
                    // Update the status text
                    mProgressBar.setVisibility(View.INVISIBLE);
                    findViewById(R.id.line).setVisibility(View.GONE);
                    // Show the ok button
                    int centerTextLabel;
                    int centerExplanationLabel = -1;
                    boolean installSucess = false;
                    //Gionee qiuxd 2013128 modify for CR00766810 start
                    //LevelListDrawable centerTextDrawable = (LevelListDrawable) getResources()
                    //        .getDrawable(R.drawable.ic_result_status);
                    LevelListDrawable centerTextDrawable;
                    if(mTheme.equals(PackageUtil.TYPE_DARK_THEME)){
                        centerTextDrawable = (LevelListDrawable) getResources()
                                  .getDrawable(R.drawable.ic_result_status);
                    }else{
                        centerTextDrawable = (LevelListDrawable) getResources()
                                .getDrawable(R.drawable.ic_result_status_black);
                    }
                    //Gionee qiuxd 2013128 modify for CR00766810 end
                    if (msg.arg1 == PackageManager.INSTALL_SUCCEEDED) {
                    	spaceView.setVisibility(View.GONE);
                        mLaunchButton.setVisibility(View.VISIBLE);
                        centerTextDrawable.setLevel(0);
                        centerTextLabel = R.string.install_done;
                        installSucess = true;
                        // Enable or disable launch button
                        mLaunchIntent = getPackageManager().getLaunchIntentForPackage(
                                mAppInfo.packageName);
                        boolean enabled = false;
                        if(mLaunchIntent != null) {
                            List<ResolveInfo> list = getPackageManager().
                                    queryIntentActivities(mLaunchIntent, 0);
                            if (list != null && list.size() > 0) {
                                enabled = true;
                            }
                        }
                        if (enabled) {
                            mLaunchButton.setOnClickListener(InstallAppProgress.this);
                        } else {
                            mLaunchButton.setEnabled(false);
                        }
                    } else if (msg.arg1 == PackageManager.INSTALL_FAILED_INSUFFICIENT_STORAGE){
                        /// M: [ALPS00269830] @{
                        if (!isFinishing()) {
                            Log.d(TAG, "!isFinishing()");   // Check to see whether this activity is in the process of finishing
                            showDialogInner(DLG_OUT_OF_SPACE);
                            Log.i(TAG,"run in INSUFFICIENT_STORAGE nei");
                        }
                        /// @}
                        return;
                    } else {
                        // Generic error handling for all other error codes.
                        centerTextDrawable.setLevel(1);
                        centerExplanationLabel = getExplanationFromErrorCode(msg.arg1);
                        centerTextLabel = R.string.install_failed;
                        // Gionee <qiuxd> <2013-03-16> modify for CR00784976 begin
                        //mLaunchButton.setVisibility(View.INVISIBLE);
                        mLaunchButton.setVisibility(View.GONE);
                        // Gionee <qiuxd> <2013-03-16> modify for CR00784976 end
                        
                    }
                    if (centerTextDrawable != null) {
                    centerTextDrawable.setBounds(0, 0,
                            centerTextDrawable.getIntrinsicWidth(),
                            centerTextDrawable.getIntrinsicHeight());
                        mStatusTextView.setCompoundDrawables(centerTextDrawable, null, null, null);
                    }
                    mStatusTextView.setText(centerTextLabel);
//                    if (centerExplanationLabel != -1) {
//                        mExplanationTextView.setText(centerExplanationLabel);
//                        mExplanationTextView.setVisibility(View.VISIBLE);
//                    } else {
//                        mExplanationTextView.setVisibility(View.GONE);
//                    }
                    
                    //huangbin add 20140122 begin
                    mStatusTextView.setVisibility(View.GONE);
                    contentLayout.setVisibility(View.VISIBLE);
                    ImageView resultImg = (ImageView)findViewById(R.id.resultImg);
                    TextView resultText = (TextView)findViewById(R.id.resultText);
                    resultText.setText(centerTextLabel);
                    if(installSucess){                  	
                    	resultImg.setImageResource(R.drawable.install_ok_img);
                    	resultImg.setVisibility(View.GONE);
                    }else{
                    	resultImg.setImageResource(R.drawable.install_fail_img);
                    	resultImg.setVisibility(View.GONE);
                    	if (centerExplanationLabel != -1) {
                    		String errorTextStr = getString(R.string.reasons_for_failure) + getString(centerExplanationLabel);
                    		TextView errorText = (TextView) findViewById(R.id.errorText);
                    		errorText.setVisibility(View.VISIBLE);
                    		errorText.setText(errorTextStr);
                    	}
                    }
                    if (installSucess) {
                    	if(expandableListViewUtils == null){
                    		expandableListViewUtils = new ExpandableListViewUtils(
                    				InstallAppProgress.this,
                    				PackageInstallerActivity.appSecurityPermissions,
                    				PackageInstallerActivity.isFirstInstall,false);
                    	}
                    	findViewById(R.id.modifyHintText).setVisibility(View.GONE);
                    } else {
//                    	View marginView104 = findViewById(R.id.marginView104);
//                		if (marginView104 != null) {
//                			marginView104.setVisibility(View.VISIBLE);
//                		}
                		findViewById(R.id.permissionListLayout).setVisibility(View.GONE);
                		findViewById(R.id.modifyHintText).setVisibility(View.GONE);
                    }

                  //huangbin add 20140122 end
                    mDoneButton.setOnClickListener(InstallAppProgress.this);
                    mOkPanel.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };
    
    private int getExplanationFromErrorCode(int errCode) {
        Log.d(TAG, "Installation error code: " + errCode);
        switch (errCode) {
            case PackageManager.INSTALL_FAILED_INVALID_APK:
                return R.string.install_failed_invalid_apk;
            case PackageManager.INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES:
                return R.string.install_failed_inconsistent_certificates;
            case PackageManager.INSTALL_FAILED_OLDER_SDK:
                return R.string.install_failed_older_sdk;
            case PackageManager.INSTALL_FAILED_CPU_ABI_INCOMPATIBLE:
                return R.string.install_failed_cpu_abi_incompatible;
            case PackageManager.INSTALL_FAILED_MISSING_SHARED_LIBRARY:
            	return R.string.install_failed_missing_shared_library;
            case PackageManager.INSTALL_FAILED_VERSION_DOWNGRADE:
            	return R.string.install_failed_version_downgrade;
            default:
                return R.string.unknown_error;
        }
    }

    @Override
    public void onCreate(Bundle icicle) {
         //Gionee qiuxd 2013128 modify for  CR00766810 start
        //Gionee qiuxd 2013117 add for CR00765187 start
    	//huang bin Shield begin
        mTheme = PackageUtil.getThemeType(getApplicationContext());
//        if (mTheme.equals(PackageUtil.TYPE_LIGHT_THEME)) {
//            setTheme(R.style.GN_Perm_Ctrl_Theme_light);
//        } else if(mTheme.equals(PackageUtil.TYPE_DARK_THEME)){
//            setTheme(R.style.GN_Perm_Ctrl_Theme_dark);
//        }
      //huang bin Shield end
        //Gionee qiuxd 2013117 add for CR00765187 end
      //Gionee qiuxd 2013128 modify for  CR00766810 end
        
        super.onCreate(icicle);
        Intent intent = getIntent();
        mAppInfo = intent.getParcelableExtra(PackageUtil.INTENT_ATTR_APPLICATION_INFO);
        mPackageURI = intent.getData();

        final String scheme = mPackageURI.getScheme();
        if (scheme != null && !"file".equals(scheme)) {
            throw new IllegalArgumentException("unexpected scheme " + scheme);
        }

        initView();
        //Gionee qiuxd 20121017 add for CR00711865 start
        
        if(mGnAppPermCtrl){
            Bundle bundle = intent.getExtras();
            
            mPermKeys = bundle.getStringArray("permKeys");
            mPermValues = bundle.getIntArray("permValues");
            mIsReplacing = bundle.getBoolean("isReplacing");
            
        }
        //Gionee qiuxd 20121017 add for CR00711865 end
        
    }
    @Override
    public void onResume(){   
    	super.onResume();
    	SystemUtils.switchStatusBarColorMode(SystemUtils.STATUS_BAR_MODE_BLACK, this);
    }
    
    //Gionee qiuxd 2013128 add for  CR00766810 start
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        mTheme = PackageUtil.getThemeType(this);
    }
    //Gionee qiuxd 2013128 add for  CR00766810 end

    @Override
    public Dialog onCreateDialog(int id, Bundle bundle) {
        switch (id) {
        case DLG_OUT_OF_SPACE:
            String dlgText = getString(R.string.out_of_space_dlg_text, mLabel);
            // Gionee <qiuxd> <2013-03-27> modify for CR00790121 begin
            return new AuroraAlertDialog.Builder(this)
            //huangbin Shield begin
            //return new AlertDialog.Builder(this,AlertDialog.THEME_AMIGO_FULLSCREEN)
            //huang Shield end
            // Gionee <qiuxd> <2013-03-27> modify for CR00790121 end
                    .setTitle(R.string.out_of_space_dlg_title)
                    .setMessage(dlgText)
                    .setPositiveButton(R.string.manage_applications, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //launch manage applications
//                            Intent intent = new Intent("android.intent.action.MANAGE_PACKAGE_STORAGE");
//                            startActivity(intent);
                        	
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
        }
       return null;
   }

    private void showDialogInner(int id) {
        removeDialog(id);
        showDialog(id);
    }

    class PackageInstallObserver extends IPackageInstallObserver.Stub {
        public void packageInstalled(String packageName, int returnCode) {
            Message msg = mHandler.obtainMessage(INSTALL_COMPLETE);
            msg.arg1 = returnCode;
            mHandler.sendMessage(msg);
        }
    }

    public void initView() {
    	
        setContentView(R.layout.op_progress);//huang hide
    	
    	//huang add begin
//    	setAuroraContentView(R.layout.op_progress,AuroraActionBar.Type.Normal);
//        getAuroraActionBar().setTitle(R.string.install);
//        getAuroraActionBar().getHomeButton().setVisibility(View.GONE);
    	//huangbin add end
        
        int installFlags = 0;
//        int installFlags = PackageManager.INSTALL_INTERNAL;	Linxiaobin hide 
        PackageManager pm = getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(mAppInfo.packageName, 
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            if(pi != null) {
                installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
            }
        } catch (NameNotFoundException e) {
        }
        if((installFlags & PackageManager.INSTALL_REPLACE_EXISTING )!= 0) {
            Log.w(TAG, "Replacing package:" + mAppInfo.packageName);
        }

        final File sourceFile = new File(mPackageURI.getPath());
        PackageUtil.AppSnippet as = PackageUtil.getAppSnippet(this, mAppInfo, sourceFile);
        mLabel = as.label;
        PackageUtil.initSnippetForNewApp(this, as, R.id.app_snippet);
        spaceView = findViewById(R.id.space_view);
        mStatusTextView = (TextView)findViewById(R.id.center_text);
        mStatusTextView.setText(R.string.installing);
        contentLayout = (LinearLayout)findViewById(R.id.contentLayout);
//        mExplanationTextView = (TextView) findViewById(R.id.center_explanation);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar2);
//        mProgressBar.setIndeterminate(true);
        findViewById(R.id.line).setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        // Hide button till progress is being displayed
        mOkPanel = (View)findViewById(R.id.buttons_panel);
        mDoneButton = (Button)findViewById(R.id.done_button);
        mLaunchButton = (Button)findViewById(R.id.launch_button);
        mOkPanel.setVisibility(View.INVISIBLE);

        String installerPackageName = getIntent().getStringExtra(
                Intent.EXTRA_INSTALLER_PACKAGE_NAME);
        PackageInstallObserver observer = new PackageInstallObserver(); 
        
        pm.installPackage(mPackageURI, observer, installFlags, installerPackageName);
        Log.i(TAG, "installFlags: " + installFlags);
        
        /** M: [ALPS00264011][Rose][ICS][Free Test][packageinstaller]The "JE" about "com.android.packageinstaller" pops up after we tap "Open"(5/5) @{ */
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        /// M: [ALPS00287901][Rose][ICS][MT6577][Free Test][APPIOT]The "Launch" will be displayed in gray after you install one apk twice.(5/5) @{
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        /// @}
        intentFilter.addDataScheme("package");
        
        // Linxiaobin modify 20140429 begin
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "Receive: " + intent);
                if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
                    Uri data = intent.getData();
                    Log.i(TAG, "data: " + data);
                    if (data != null) {
                        String pkg = data.getSchemeSpecificPart();
                        Log.i(TAG, "pkg: " + pkg);
                        Log.i(TAG, "mAppInfo.packageName: " + mAppInfo.packageName);
                        if (pkg.equals(mAppInfo.packageName))
                            mLaunchButton.setEnabled(false);
                    }
                }
                /// M: [ALPS00287901][Rose][ICS][MT6577][Free Test][APPIOT]The "Launch" will be displayed in gray after you install one apk twice.(5/5)
                else if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction()) || Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
                    Uri data = intent.getData();
                    Log.i(TAG, "data: " + data);
                    if (data != null) {
                        String pkg = data.getSchemeSpecificPart();
                        Log.i(TAG, "pkg: " + pkg);
                        Log.i(TAG, "mAppInfo.packageName: " + mAppInfo.packageName);
                        if (pkg.equals(mAppInfo.packageName))
                            mLaunchButton.setEnabled(true);
                    }
                }
                /// @}
            }
        };
        registerReceiver(mBroadcastReceiver, intentFilter);
        // Linxiaobin modify 20140429 end
        /** @} */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Linxiaobin modify 20140429 begin
        if (mBroadcastReceiver != null) {
        	unregisterReceiver(mBroadcastReceiver);
        }
        // Linxiaobin modify 20140429 end
    }

    public void onClick(View v) {
        if(v == mDoneButton) {
            if (mAppInfo.packageName != null) {
                Log.i(TAG, "Finished installing "+mAppInfo.packageName);
            }
            finish();
        } else if(v == mLaunchButton) {
            startActivity(mLaunchIntent);
            finish();
        }
    }

    public void onCancel(DialogInterface dialog) {
        finish();
    }
    
    //Gionee qiuxd 20121017 add for CR00711865 start
    
    private void operatePermissionDB(){
        if((mPermKeys != null) && (mPermValues != null)){
            String  pkgName = mAppInfo.packageName;
            if(mIsReplacing){
                resavePermDB(pkgName,mPermKeys,mPermValues);
            }else{
                //not replacing install way,but user unInstall package in a special way,the permissions are not cleared.
                //so we install the package in regular again,we should check it first.
                if(getIntalledAppPermCount(pkgName) > 0){
                    deletePermsByPkgName(pkgName);
                }
                
                insertPermsToDB(pkgName,mPermKeys,mPermValues);
                
                // Gionee <qiud> <2010-03-15> modify for CR00785143 begin
                Intent intent = new Intent();
                intent.setAction("com.gionee.action.UPDATE_PERM_DB");
                sendBroadcast(intent);
                // Gionee <qiud> <2010-03-15> modify for CR00785143 end
            }
        }
    }
    
    private void resavePermDB(String pakageName,String[] keys,int[] values){
        deletePermsByPkgName(pakageName);
        insertPermsToDB(pakageName,keys,values);
    }
    
    private void updatePermDB(String packagename,String[] keys,int[] values){
        ContentValues cv = new ContentValues();
        for(int i=0;i<keys.length;i++){
            String[] temp = keys[i].split("&");
            cv.put("status", values[i]);
            updateRawToDB(cv,packagename,temp[0]);
            Log.d(TAG, "cv = "+cv.toString());
        }
    }
    
    private void deletePermsByPkgName(String packageName){
    	//huangbin hide 
 //       getContentResolver().delete(GN_PERM_URI, " packagename = ?", new String[]{packageName});
    }
    
    private void updateRawToDB(ContentValues cv,String packagename,String permission){
    	//huangbin hide
//        getContentResolver().update(GN_PERM_URI, cv, 
//                " packagename =? and permission = ?", new String[]{packagename,permission});
    }
    
    private int getIntalledAppPermCount(String packageName){
        int count = 0;
        //huangbin hide begin
//        Cursor cursor = this.getContentResolver().query(GN_PERM_URI, new String[]{"permission"},
//                " packagename = ?", new String[]{packageName}, null);
//        if(cursor != null){
//            count = cursor.getCount();
//            cursor.close();
//        }
        //huangbin hide end
        return count;
    }
    
    private void insertPermsToDB(String pkgName,String[] keys,int[] values){
        ContentResolver cr = getContentResolver();
        for(int i=0;i<keys.length;i++){
            String[] temp = keys[i].split("&");
            
            ContentValues cv = new ContentValues();
            
            cv.put("packagename", pkgName);
            cv.put("permission", temp[0]);
            cv.put("permissiongroup", temp[1]);
            cv.put("status", values[i]);  
   //huangbin hide         
 //           cr.insert(GN_PERM_URI, cv);

        }
    }
    //Gionee qiuxd 20121017 add for CR00711865 end
}
