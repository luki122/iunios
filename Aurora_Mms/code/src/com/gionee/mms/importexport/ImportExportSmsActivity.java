/*
 *
 * Copyright (C) 2011 gionee Inc
 *
 * Author: fangbin
 *
 * Description:
 *
 * history
 * name                              date                                      description
 *
 */

package com.gionee.mms.importexport;

import java.io.File;

import aurora.app.AuroraActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Telephony;
import android.provider.Telephony.Sms;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.widget.TextView;
import android.content.Context;
import android.content.SharedPreferences;
import com.gionee.mms.importexport.MultiExportSmsActivity;

import com.android.mms.MmsApp;
import com.android.mms.R;
import android.view.MenuItem;
//gionee zhouyj 2012-05-29 add for CR00607938 start
import java.io.FileFilter;
import android.os.storage.StorageManager;
import gionee.os.storage.GnStorageManager;
import android.os.storage.StorageVolume;
import android.os.Environment;
import com.aurora.featureoption.FeatureOption;
//gionee zhouyj 2012-05-29 add for CR00607938 end

//Gionee linggz 2012-09-03 add for CR00672099 begin
import android.os.SystemProperties;
//Gionee linggz 2012-09-03 add for CR00672099 end
public class ImportExportSmsActivity extends AuroraActivity implements OnClickListener {
    private static final String TAG = "ImportExportSmsActivity";
    private GnImageTextButton mExportButton = null;
    private GnImageTextButton mImportButton = null;
    private TextView mTextView = null;
    
    //Gionee linggz 2012-09-03 add for CR00672099 begin
    private static final boolean gnVHHflag = SystemProperties.get("ro.gn.oversea.custom").equals("VIETNAM_VHH");
    //Gionee linggz 2012-09-03 add for CR00672099 end        
    // gionee zhouyj 2012-05-29 add for CR00607938 start
    private final String MESSAGE = "/message";
    private final String SDCARD2 = "/mnt/sdcard2";
    private final String TXT     = ".txt";
    private final String DB      = ".db";
    // gionee zhouyj 2012-05-29 add for CR00607938 end

    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        //gionee gaoj 2012-5-30 added for CR00555790 start
        if (MmsApp.mTransparent) {
            setTheme(R.style.TabActivityTheme);
        } else if (MmsApp.mLightTheme) {
            setTheme(R.style.GnMmsLightTheme);
        } else if (MmsApp.mDarkTheme) {
            setTheme(R.style.GnMmsDarkTheme);
        }
        //gionee gaoj 2012-5-30 added for CR00555790 end
        super.onCreate(arg0);
        Log.d(TAG, "SmsImportExportActivity----------------onCreate");
        setTitle(R.string.gn_import_export);
        setContentView(R.layout.gn_import_export);
        initViews();
        setListeners();
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        getActionBar().setDisplayHomeAsUpEnabled(true);
        // Aurora liugj 2013-09-13 modified for aurora's new feature end
        initSdCards(this);
    }
    
    // gionee zhouyj 2012-05-28 add for CR00607938 start
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch(item.getItemId()) {
        case android.R.id.home:
            finish();
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    // gionee zhouyj 2012-05-28 add for CR00607938 start

    private void initViews() {
        mExportButton = (GnImageTextButton) findViewById(R.id.exportBtn);
        mImportButton = (GnImageTextButton) findViewById(R.id.importBtn);/*
        mExportButton.setBackgroundResource(R.drawable.gn_btn_press_common);
        mImportButton.setBackgroundResource(R.drawable.gn_btn_press_common);*/
        //Gionee <zhouyj> <2013-05-23> add for CR00799376 begin for super theme
        mExportButton.setTextColor(getResources().getColor(R.color.gn_primary_text_light));
        mImportButton.setTextColor(getResources().getColor(R.color.gn_primary_text_light));
        //Gionee <zhouyj> <2013-05-23> add for CR00799376 end
        mTextView = (TextView) findViewById(R.id.date);
    }

    private void setListeners() {
        mExportButton.setOnClickListener(this);
        mImportButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        if (view.equals(mExportButton)) {
            if (hasExportSms()) {
                Intent mIntent;
                if(mStorageMountedCount >= 2) {
                    mIntent = new Intent("android.intent.action.choosesdcard");
                    mIntent.putExtra("tips", getString(R.string.import_export_tip));
                } else if(mStorageMountedCount == 1){
                    mIntent = new Intent("android.intent.action.MultiExportSmsActivity");
                } else {
                    Toast.makeText(this, getString(R.string.gn_no_sdcard), Toast.LENGTH_SHORT).show();
                    return ;
                }
                startActivityForResult(mIntent, 1);
            } else {
                Toast.makeText(this, getString(R.string.gn_no_export_sms), Toast.LENGTH_SHORT).show();
            }
        } else if (view.equals(mImportButton)) {
            if (mStorageMountedCount > 0) {
                if (hasImportFile()) {
                    Intent mIntent = new Intent("android.intent.action.MultiImportSmsActivity");
                    startActivityForResult(mIntent, 1);
                } else {
                    Toast.makeText(this, getString(R.string.gn_no_import_file), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getString(R.string.gn_no_sdcard), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean hasImportFile() {
        // gionee zhouyj 2012-05-29 modify for CR00607938 start
        /** remove
        File fileDir = new File(ConfigConstantUtils.SDCARD_ROOT_PATH + getString(R.string.gn_import_export_file_path));
        if (fileDir.exists()) {
            File[] files = fileDir.listFiles();
            if (null != files && files.length>0) {
                for (int i=0;i<files.length;i++) {
                    if (files[i].getName().endsWith(ConfigConstantUtils.IMPORT_EXPORT_SMS_FILE_EXTENSION)) {
                        return true;
                    }
                }
                return false;
            } else {
                return false;
            }
        } else {
            return false;
        }*/
        boolean hasImportFile = false;
        hasImportFile = hasImportFiles(mSDCardPath + getString(R.string.gn_import_export_file_path))
            || hasImportFiles(mSDCardPath + MESSAGE);
        if(mStorageMountedCount > 1) {
            hasImportFile = hasImportFile || hasImportFiles(mSDCard2Path + getString(R.string.gn_import_export_file_path))
                || hasImportFiles(mSDCard2Path + MESSAGE);
        }
        return hasImportFile;
        // gionee zhouyj 2012-05-29 modify for CR00607938 end
    }
    
    // gionee zhouyj 2012-05-29 add for CR00607938 start
    private boolean hasImportFiles(String path) {
        File fileDir = new File(path);
        FileFilter ff = new FileFilter() {
            public boolean accept(File f) {
                if (!f.isDirectory() && f.getName().toLowerCase().endsWith(TXT)
                        && f.canRead()) {
                    return true;
                }
                if (!f.isDirectory() && f.getName().toLowerCase().endsWith(DB)
                        && f.canRead()) {
                    return true;
                }
                return false;
            }
        };
        if(!fileDir.exists()) {
            return false;
        }
        File[] dbFiles = fileDir.listFiles(ff);
        return dbFiles.length != 0 ;
    }
    // gionee zhouyj 2012-05-29 add for CR00607938 end

    private boolean hasExportSms() {
        boolean flag = false;
        Cursor cursor = getContentResolver().query(ConfigConstantUtils.IMPORT_EXPORT_SMS_QUERY_URI, null, Sms.TYPE + " != " + Telephony.TextBasedSmsColumns.MESSAGE_TYPE_DRAFT, null, null);
        if (null != cursor && cursor.getCount() > 0) {
            flag = true;
        }
        if (null != cursor) {
            if (!cursor.isClosed()) {
                cursor.close();
            }
            cursor = null;
        }
        return flag;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        //Gionee <zhouyj> <2013-04-23> add for CR00801232 start
        if (MmsApp.mIsSafeModeSupport) {
            finish();
            return;
        }
        //Gionee <zhouyj> <2013-04-23> add for CR00801232 end
        SharedPreferences preferences = getSharedPreferences(ConfigConstantUtils.IMPORT_EXPORT_SMS_PRE, Context.MODE_PRIVATE);
        String action = preferences.getString("action", null);
        String date = preferences.getString("lastDate", null);
        if (null != action && null != date) {
            if (ConfigConstantUtils.getLocalLanguage().equals("en")) {
                date = ConfigConstantUtils.getLocalDateStr(ConfigConstantUtils.DATE_THIRD_TYPE, ConfigConstantUtils.DATE_FIFTH_TYPE, date);
            } else if (ConfigConstantUtils.getLocalLanguage().equals("zh")) {
                date = ConfigConstantUtils.getLocalDateStr(ConfigConstantUtils.DATE_THIRD_TYPE, ConfigConstantUtils.DATE_THIRD_TYPE, date);
            }
            if (action.equals("import")) {
                //Gionee linggz 2012-09-03 add for CR00672099 begin
                if(true == gnVHHflag){
                    mTextView.setText(getString(R.string.gn_last_import) + " : " + date);
                } else{
                    mTextView.setText(getString(R.string.gn_last) + getString(R.string.gn_import_text) + " : " + date);
                }
            } else if (action.equals("export")) {
                //Gionee linggz 2012-09-03 add for CR00672099 begin
                if(true == gnVHHflag){
                    mTextView.setText(getString(R.string.gn_last_export) + " : " + date);
                } else{
                    mTextView.setText(getString(R.string.gn_last) + getString(R.string.gn_export_text) + " : " + date);
                }
                //Gionee linggz 2012-09-03 add for CR00672099 end
            } else {
                mTextView.setText("");
            }
        } else {
            mTextView.setText("");
        }
    }

    @Override
    public boolean onSearchRequested() {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // gionee zhouyj 2012-07-13 add for CR00647101 start 
            if(data != null && data.getAction().equals("ChooseSdcard")) {
                int position = data.getIntExtra("position", 0);
                mDefaultSDCardPath =position == 0? mSDCardPath : mSDCard2Path;
                Intent mIntent = new Intent("android.intent.action.MultiExportSmsActivity");
                startActivityForResult(mIntent, 1);
                return ;
            }
            // gionee zhouyj 2012-07-13 add for CR00647101 end 
            this.finish();
        }
    }
    
//    private boolean isExSdcardInserted() {
//        String state = null;
//        state = mStorageManager.getVolumeState(mSDCard2Path);
//        return Environment.MEDIA_MOUNTED.equals(state);
//    }
    
    private StorageManager mStorageManager;
    private File[] mSDCardMountPointPathList = null;
    private int mStorageCount;
    private static int mStorageMountedCount;
    public static String mSDCardPath = null;
    public static String mSDCard2Path = null;
    public  static String mDefaultSDCardPath = null;
    
    private void initSdCards(Context context) {
        mStorageManager = (StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
        StorageVolume[] storageVolume = mStorageManager.getVolumeList();
        mStorageCount = storageVolume.length;
        File[] systemSDCardMountPointPathList = new File[mStorageCount];
        for (int i = 0; i < mStorageCount; i++) {
            systemSDCardMountPointPathList[i] = new File(storageVolume[i].getPath());
        }
        mSDCardMountPointPathList = updateMountedPointList(systemSDCardMountPointPathList);
        mStorageMountedCount = mSDCardMountPointPathList.length;
        Log.i(TAG,"mStorageMountedCount = " + mStorageMountedCount);
        if (mStorageMountedCount >= 2) {
            mSDCardPath = mSDCardMountPointPathList[0].getAbsolutePath();
            mSDCard2Path = mSDCardMountPointPathList[1].getAbsolutePath();
        } else if(mStorageMountedCount == 1){
            mSDCardPath = mSDCardMountPointPathList[0].getAbsolutePath();
        }
        mDefaultSDCardPath = mSDCardPath;
    }
    
    // get mounted sdcard
    private File[] updateMountedPointList(File[] systemSDCardMountPointPathList){
        int mountCount = 0;
        for (int i = 0; i < systemSDCardMountPointPathList.length; i++) {
            if (checkSDCardMount(systemSDCardMountPointPathList[i].getAbsolutePath())) {
                mountCount++;
            }
        }
        File[] sdCardMountPointPathList = new File[mountCount];
        for (int i = 0, j = 0; i < systemSDCardMountPointPathList.length; i++) {
            if (checkSDCardMount(systemSDCardMountPointPathList[i].getAbsolutePath())) {
                sdCardMountPointPathList[j++] = systemSDCardMountPointPathList[i];
            }
        }
        
        return sdCardMountPointPathList;
    }
    /**
     * This method checks whether SDcard is mounted or not
     @param  mountPoint   the mount point that should be checked
     @return               true if SDcard is mounted, false otherwise
     */ 
    protected boolean checkSDCardMount(String mountPoint) {
        if(mountPoint == null){
            return false;
        }
        String state = null;
          // Aurora liugj 2013-11-12 modified for bug-554 start
        state = GnStorageManager.getVolumeState(mountPoint);
          // Aurora liugj 2013-11-12 modified for bug-554 end
        return Environment.MEDIA_MOUNTED.equals(state);
    }
 
    //get storage name
    public static String getStorageDescription(String path, Context context) {
        // gionee zhouyj 2012-07-25 add for CR00654992 start 
        if (null == path) 
            return "";
        // gionee zhouyj 2012-07-25 add for CR00654992 end
        if(FeatureOption.MTK_2SDCARD_SWAP) {
            if (mStorageMountedCount < 2) {
                if (path.startsWith(mSDCardPath)) {
                    return context.getString(R.string.gn_chooser_internal_sdcard);
                }
            }
            if(path.startsWith(mSDCard2Path)){
                return context.getString(R.string.gn_chooser_internal_sdcard);
            }else if(path.startsWith(mSDCardPath)){
                return context.getString(R.string.gn_chooser_external_sdcard);
            }
        } else {
            if (mStorageMountedCount < 2) {
                if (path.startsWith(mSDCardPath)) {
                    return context.getString(R.string.gn_chooser_internal_sdcard);
                }
            }
            if(path.startsWith(mSDCard2Path)){
                return context.getString(R.string.gn_chooser_external_sdcard);
            }else if(path.startsWith(mSDCardPath)){
                return context.getString(R.string.gn_chooser_internal_sdcard);
            }
        }
        return null;
    }
    
    public static int getSdcardCount() {
        return mStorageMountedCount;
    }
}
