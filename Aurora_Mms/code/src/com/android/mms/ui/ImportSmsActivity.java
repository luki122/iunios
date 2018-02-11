package com.android.mms.ui;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Date;

import com.android.mms.R;
import com.android.mms.util.Recycler;

import aurora.app.AuroraAlertDialog;
import android.app.Dialog;
import aurora.app.AuroraProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.Toast;
import gionee.provider.GnTelephony;
import gionee.provider.GnTelephony.Sms;
import gionee.provider.GnTelephony.SIMInfo;
import com.aurora.featureoption.FeatureOption;
//Gionee zhangxx 2012-04-02 add for CR00556294 begin
import com.android.mms.MmsApp;
import android.content.Context;
import android.os.storage.StorageManager;
// Gionee zhangxx 2012-04-02 add for CR00556294 end

public class ImportSmsActivity extends AuroraPreferenceActivity {
    public static final String PREF_IMPORT = "pref_key_import_sms";
    private static final String TAG = "MMS/ImportSmsActivity";
    private AuroraPreferenceCategory smsCategory;
    private AuroraProgressDialog progressdialog = null;
    private static final String TABLE_SMS = "sms";
    public static final String SDCARD_DIR_PATH = "//sdcard//message//";
    // Gionee zhangxx 2012-04-02 add for CR00556294 begin
    public static final String SDCARD2_DIR_PATH = "/mnt/sdcard2/message/";
    // Gionee zhangxx 2012-04-02 add for CR00556294 end
    private static final String[] SMS_COLUMNS =
    { "thread_id", "address","m_size", "person", "date", "protocol", "read", "status", "type", "reply_path_present",
      "subject", "body", "service_center", "locked", GnTelephony.GN_SIM_ID/*"sim_id"*/, "error_code", "seen"};
    public Handler mMainHandler; 
    private static final String[] ADDRESS_COLUMNS = {"address"};
    private static final int IMPORT_SMS    = 2;    
    private static final int NO_DATABASE   = 4;
    private static final int IMPORT_SUCCES = 5;
    private static final int IMPORT_FAILED = 6;
    private String importFileName = "";
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.importpreferences);
        smsCategory = (AuroraPreferenceCategory) findPreference(PREF_IMPORT);
        newMainHandler();
    }

    private void newMainHandler(){
        mMainHandler=new Handler() {
            @Override
            public void handleMessage(Message msg) { 
                int output = R.string.import_message_list_empty;  
                switch(msg.what){ 
                case NO_DATABASE:
                    output = R.string.import_message_list_empty;
                    showToast(output);
                    finish();
                    break;
                case IMPORT_SUCCES:
                    output = R.string.import_message_success;
                    break;
                case IMPORT_FAILED: 
                    output = R.string.import_message_fail;
                    break;
                default: 
                    break;
                }
                showToast(output);
                
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!isSDcardReady()){
            finish();
            return;
        }
        new Thread() {
            public void run() {
                try {
                    // Gionee zhangxx 2012-04-02 add for CR00556294 begin
                    String storageDirectory = getDefaultStorePath();
                    if (MmsApp.mGnEMMCSupport && storageDirectory != null && storageDirectory.equals("/mnt/sdcard2")) {
                        getSMSFileRecursively(MessagingPreferenceActivity.SDCARD2_DIR_PATH);
                    } else {                        
                        getSMSFileRecursively(MessagingPreferenceActivity.SDCARD_DIR_PATH);
                    }
                    // Gionee zhangxx 2012-04-02 add for CR00556294 begin
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void getSMSFileRecursively(String path) throws IOException {
        smsCategory.removeAll();
        File directory = new File(path);
        FileFilter ff = new FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                } else if (f.getName().toLowerCase().endsWith(".db")
                        && f.canRead()) {
                    return true;
                }
                return false;
            }
        };
        
        final File[] files = directory.listFiles(ff);

        // e.g. secured directory may return null toward listFiles().
        if (files == null) {
            Log.w(TAG, "listFiles() returned null (directory: " + directory + ")");
            mMainHandler.sendEmptyMessage(NO_DATABASE);
            return;
        }
        for (File file : files) {
            String fileName = file.getName();
            String date = new Date(file.lastModified()).toLocaleString();
            AuroraPreference sms = new AuroraPreference(this);
            sms.setKey(fileName);
            sms.setTitle(fileName);
            sms.setSummary(date);
            sms.setOnPreferenceClickListener(listener);
            smsCategory.addPreference(sms);
        }
    }
    
    private OnPreferenceClickListener listener = new OnPreferenceClickListener(){

        public boolean onPreferenceClick(AuroraPreference arg0) {
            // TODO Auto-generated method stub
            importFileName = arg0.getKey();
            Log.d(TAG, "Click listener you choosed file "+importFileName);
            //importMessages(key);
            showDialog(IMPORT_SMS);
            return false;
        }
        
    };
    @Override
    protected void onPrepareDialog(int id, Dialog d) { 
        if (id == IMPORT_SMS && d != null) {
            ((AuroraAlertDialog)d).setMessage(getString(R.string.whether_import_item) + " " + importFileName + "?");
        }
    }
    @Override
    protected Dialog onCreateDialog(int id) {
          switch (id) {
          case IMPORT_SMS:
              return new AuroraAlertDialog.Builder(this)
                .setMessage(getString(R.string.whether_import_item) + " " + importFileName + "?")
                .setTitle(R.string.pref_summary_import_msg)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                importMessages(importFileName);
                                return;
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                importFileName = "";
                            }
                        }).create();
          }
          return super.onCreateDialog(id);
    }
    
    private boolean importMessages(final String key){
        Log.d(TAG,"importMessages begin");
        if(!isSDcardReady() || key == null){
            return false;
        }
        progressdialog = AuroraProgressDialog.show(this, "",getString(R.string.import_message_ongoing), true);
        new Thread() {
            public void run() {
                SQLiteDatabase db;
                Cursor cursor = null; 
                try {
                    // Gionee zhangxx 2012-04-02 add for CR00556294 begin
                    String storageDirectory = getDefaultStorePath();
                    if (MmsApp.mGnEMMCSupport && storageDirectory != null && storageDirectory.equals("/mnt/sdcard2")) {
                        db = SQLiteDatabase.openDatabase(SDCARD2_DIR_PATH + key,null, SQLiteDatabase.OPEN_READONLY);
                    } else {
                        db = SQLiteDatabase.openDatabase(SDCARD_DIR_PATH + key,null, SQLiteDatabase.OPEN_READONLY);
                    }
                    // Gionee zhangxx 2012-04-02 add for CR00556294 end
                    cursor = db.query(TABLE_SMS, SMS_COLUMNS, null, null, null, null, "date ASC");
                    if (cursor == null) {
                        Log.w(TAG, "importDict sms cursor is null ");
                        mMainHandler.sendEmptyMessage(IMPORT_FAILED);
                        return;
                    }
                    int count = cursor.getCount();
                    Log.d(TAG, "importDict sms count = " + count);
                    if (count > 0) {
                        boolean loop = true;
                        while (cursor.moveToNext() && loop) {
                            if (progressdialog != null && !progressdialog.isShowing()){
                                loop = false;
                            }
                            copyToPhoneMemory(cursor);
                        }
                    } else {
                        Log.w(TAG, "importDict sms is empty");
                        return;
                    }
                    Log.d(TAG, "import message success");
                    if (progressdialog != null && progressdialog.isShowing()){
                        mMainHandler.sendEmptyMessage(IMPORT_SUCCES);
                    }
                    db.close();
                } catch (SQLiteException e) {
                    Log.e(TAG, "can't open the database file");
                    mMainHandler.sendEmptyMessage(IMPORT_FAILED);
                } finally {
                    Log.d(TAG, "finally delete old message");
                    if (cursor != null) {
                        cursor.close();
                    }
                    if (null != progressdialog) {
                        progressdialog.dismiss();
                    }
                    Recycler.getSmsRecycler().deleteOldMessages(getApplicationContext());
                }
                
            }
        }.start();
        return true;
    }
    
    private void copyToPhoneMemory(Cursor cursor) {
        ContentResolver mContentResolver = getContentResolver();
        String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
        String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        Long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
        int currentSimId = cursor.getInt(cursor.getColumnIndexOrThrow(GnTelephony.GN_SIM_ID/*"sim_id"*/));
        String serviceCenter = cursor.getString(cursor.getColumnIndexOrThrow("service_center"));
        int messageType = cursor.getInt(cursor.getColumnIndexOrThrow("type"));
        int read = cursor.getInt(cursor.getColumnIndexOrThrow("read")); 
        int seen = cursor.getInt(cursor.getColumnIndexOrThrow("seen")); 
        ContentValues insertValues = new ContentValues(6);
        insertValues.put(Sms.ADDRESS, address);
        insertValues.put(Sms.DATE, date);
        insertValues.put(Sms.BODY, body);
        insertValues.put(Sms.SIM_ID, currentSimId);
        insertValues.put(Sms.TYPE, messageType);
        insertValues.put(Sms.READ, read);
        insertValues.put(Sms.SEEN, seen);
        insertValues.put("import_sms", true);
        getContentResolver().insert(Sms.CONTENT_URI, insertValues);
    }
    
    private boolean isSDcardReady(){
        boolean isSDcard = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED); 
        if (!isSDcard) {
            showToast(R.string.no_sd_card);
            Log.d(TAG, "there is no SD card");
            return false;
        }
        return true;
    }

    private void showToast(int id) { 
        Toast t = Toast.makeText(getApplicationContext(), getString(id), Toast.LENGTH_SHORT);
        t.show();
    }
    
    // Gionee zhangxx 2012-04-02 add for CR00556294 begin
    private String getDefaultStorePath() {
        StorageManager storageManager = null;
        storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        // Aurora xuyong 2013-11-15 modified for S4 adapt start
       // Aurora xuyong 2014-04-18 modified for bug #4361 start
        return gionee.os.storage.GnStorageManager.getInstance(this).getDefaultPath_ex();
       // Aurora xuyong 2014-04-18 modified for bug #4361 end
        // Aurora xuyong 2013-11-15 modified for S4 adapt end
    }
    // Gionee zhangxx 2012-04-02 add for CR00556294 end
}
