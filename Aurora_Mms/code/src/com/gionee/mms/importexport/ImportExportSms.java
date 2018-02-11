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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony;
import gionee.provider.GnTelephony.Sms;
import android.provider.Telephony.TextBasedSmsColumns;
import android.util.Log;
import com.android.mms.R;
import android.net.Uri;
// gionee zhouyj 2012-05-28 add for CR00607938 start
import com.android.mms.MmsApp;
import com.android.mms.util.Recycler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
// gionee zhouyj 2012-05-28 add for CR00607938 end

public class ImportExportSms implements ImportExportInterface {
    private static final String TAG = "ImportExportSms";
    private ImportExportHandler mImportExportHandler = null;
    private ServiceCallBack mCallBack = null;
    private SmsInfoModel mModels = null;
    private Context mContext = null;
    private File mFile = null;
    private Handler mHandler = null;
    private ArrayList<String> mThreadIds = new ArrayList<String>();
    private boolean mExportFileReady = false;
    private boolean mIsInterrupt = false;
    //Gionee <guoyx> <2013-05-15> modified for CR00772651 begin
    private static final int MAX_INSERT_ONECE = 2800;
    //Gionee <guoyx> <2013-05-15> modified for CR00772651 end
    private static final int EXPORT_QUERY_COMPLETE_TOKEN = 1;
    private static final String[] PROJECTION = new String[] {
//      BaseColumns._ID,
//      Conversations.THREAD_ID,
      // For SMS
      Sms.ADDRESS,
      Sms.BODY,
      Sms.DATE,
//      Sms.READ,
      Sms.TYPE,
//      Sms.STATUS,
//      Sms.LOCKED,
//      Sms.ERROR_CODE
    };
    private static final String SMS_ANDRESS_COLUMN = TextBasedSmsColumns.ADDRESS;
    private static final String SMS_DATE_COLUMN = TextBasedSmsColumns.DATE;
    private static final String SMS_TYPE_COLUMN = TextBasedSmsColumns.TYPE;
    private static final String SMS_BODY_COLUMN = TextBasedSmsColumns.BODY;
    private static final String SMS_READ_COLUMN = TextBasedSmsColumns.READ;
    private static final String SMS_SEEN_COLUMN = TextBasedSmsColumns.SEEN;
    private String mFormat = ConfigConstantUtils.DATE_SECOND_TYPE;
    private long mAvaiableSpace = 0l;
    private boolean mCancelImport = false;
    // gionee zhouyj 2013-04-08 add for CR00794257 start
    private int mExportSmsReadyCount = 0;
    // gionee zhouyj 2013-04-08 add for CR00794257 end
    
    // gionee zhouyj 2012-05-28 add for CR00607938 start
    private static final String TABLE_SMS = "sms";
    private static final String[] SMS_COLUMNS =
    { "thread_id", "address","m_size", "person", "date", "protocol", "read", "status", "type", "reply_path_present",
      "subject", "body", "service_center", "locked", Sms.SIM_ID, "error_code", "seen"};
    // gionee zhouyj 2012-05-28 add for CR00607938 end

    public ImportExportSms(Context context) {
        // TODO Auto-generated constructor stub
        mContext = context;
        mFormat = ConfigConstantUtils.DATE_SECOND_TYPE;
        if (ConfigConstantUtils.getLocalLanguage().equals("en")) {
            mFormat = ConfigConstantUtils.DATE_FOURTH_TYPE;
        } else if (ConfigConstantUtils.getLocalLanguage().equals("zh")){
            mFormat = ConfigConstantUtils.DATE_SECOND_TYPE;
        }
    }

    public ArrayList<String> getmThreadIds() {
        return mThreadIds;
    }

    public void setmThreadIds(ArrayList<String> mThreadIds) {
        this.mThreadIds = mThreadIds;
    }

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public void setInterrupt(boolean isInterrupt) {
        this.mIsInterrupt = isInterrupt;
    }

    @Override
    public void importComponent(String path) {
        // TODO Auto-generated method stub
        Log.d(TAG, "ImportExportSms------------------------ImportMms");
        if (null == mImportExportHandler) {
            mImportExportHandler = new ImportExportHandler(mContext.getContentResolver());
        }
        if ((ImportExportSmsActivity.getSdcardCount() > 0) && !mIsInterrupt) {
            if (null != path) {
                // gionee zhouyj 2012-05-28 modify for CR00607938 start
                if(path.toLowerCase().endsWith(".db")) {
                    importDb2SmsDb(path);
                } else {
                    //import2SmsDb(ConfigConstantUtils.SDCARD_ROOT_PATH + mContext.getString(R.string.gn_import_export_file_path) + path);
                    import2SmsDb(path);
                }
                // gionee zhouyj 2012-05-28 modify for CR00607938 end
            } else {
                if (!mIsInterrupt) {
                    handleMessage(ConfigConstantUtils.SDCARD_ROOT_PATH + mContext.getString(R.string.gn_import_export_file_path) + path, ConfigConstantUtils.IMPORT_FILE_PATH_ERROR);
                    return;
                }
            }
        } else {
            if (!mIsInterrupt) {
                Log.i(TAG, "ImportExportSms   importComponent");
                callBack(ConfigConstantUtils.NO_SDCARD, null);
                return;
            }
        }
    }

    @Override
    public void exportComponent() {
        // TODO Auto-generated method stub
        Log.d(TAG, "ImportExportSms------------------------ExportMms");
        if (null == mImportExportHandler) {
            mImportExportHandler = new ImportExportHandler(mContext.getContentResolver());
        }
        if ((ImportExportSmsActivity.getSdcardCount() > 0) && !mIsInterrupt) {
            exportFromSmsDb();
        } else {
            if (!mIsInterrupt) {
                Log.i(TAG, "ImportExportSms   exportComponent");
                callBack(ConfigConstantUtils.NO_SDCARD, null);
                return;
            }
        }
    }

    @Override
    public void setCallBack(ServiceCallBack callBack) {
        // TODO Auto-generated method stub
        mCallBack = callBack;
    }

    @Override
    public void setImportExportComponent(ImportExportInterface component) {
        // TODO Auto-generated method stub
    }

    private void callBack(int responseCode, Object obj) {
        if (null != mCallBack && !mIsInterrupt) {
            mCallBack.callBack(responseCode, obj);
        }
    }

    private void handleMessage(String path, int code) {
        if (null != mHandler) {
            Message msg = Message.obtain();
            msg.obj = path;
            msg.what = code;
            if (!mIsInterrupt) {
                mHandler.sendMessage(msg);
            }
        }
    }

    private void import2SmsDb(String filePath) {
        File mFile = new File(filePath);
        if (mFile.exists()) {
            if (mFile.length() <= 0) {
                handleMessage(filePath, ConfigConstantUtils.IMPORT_FILE_EMPTY);
                return;
            } else if (null != mHandler) {
                Message msg = Message.obtain();
                msg.obj = filePath;
                msg.what = ConfigConstantUtils.IMPORT_FILE_READY;
                if (!mIsInterrupt) {
                    mHandler.sendMessage(msg);
                }
            }
            //Gionee <guoyx> <2013-05-15> modified for CR00772651 begin
            StringBuffer buffer = new StringBuffer();
            try {
                FileInputStream inputStream = new FileInputStream(mFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));

                String lineStr = "";
                while (null != (lineStr = reader.readLine())) {
                    buffer.append(lineStr);
                }
                // Gionee <guoyx> <2013-05-06> added for CR00772651 begin
                if (inputStream != null) {
                    inputStream.close();
                }
                // Gionee <guoyx> <2013-05-06> added for CR00772651 end
            } catch (Exception e) {
                handleMessage(filePath, ConfigConstantUtils.IMPORT_FILE_ERROR);
                Log.e(TAG, "ImportExportSms--------------readFromFile---------read object exception!");
                e.printStackTrace();
            }
            //Gionee <guoyx> <2013-05-15> modified for CR00772651 end
            String[] mModelsString = buffer.toString().split(ConfigConstantUtils.IMPORT_EXPORT_SMS_SEPARATOR_TAB_TWO);
            String[] smsInfoString = null;
            ArrayList<ContentValues> mValuesList = new ArrayList<ContentValues>();
            ContentValues mValues = new ContentValues();
            Log.i("import2SmsDb", "import sms :Total " + mModelsString.length);

            for (int i = 0; i < mModelsString.length && !mCancelImport; i++) {
                smsInfoString = mModelsString[i].split(ConfigConstantUtils.IMPORT_EXPORT_SMS_SEPARATOR_TAB_ONE);
                if (smsInfoString.length == 4) {
                    // gionee zhouyj 2012-06-30 add for CR00632426 start
                    if (i == 0) {
                        smsInfoString[0] = ConfigConstantUtils.removeUTF8Header(smsInfoString[0]);
                    }
                    // gionee zhouyj 2012-06-30 add for CR00632426 end
                    mValues = new ContentValues();
                    mValues.put(SMS_ANDRESS_COLUMN, smsInfoString[0]);
                    if (i == 0) {
                        if (smsInfoString[1].contains("/")) {
                            mFormat = ConfigConstantUtils.DATE_FOURTH_TYPE;
                        } else {
                            mFormat = ConfigConstantUtils.DATE_SECOND_TYPE;
                        }
                    }
                    mValues.put(SMS_DATE_COLUMN, ConfigConstantUtils.formatDate2Long(mFormat, smsInfoString[1]));
                    mValues.put(SMS_TYPE_COLUMN, Integer.parseInt(smsInfoString[3]));
                    mValues.put(SMS_BODY_COLUMN, smsInfoString[2]);
                    mValues.put(SMS_READ_COLUMN, 1);
                    mValues.put(SMS_SEEN_COLUMN, 1);
                    mValuesList.add(mValues);
                } else if (smsInfoString.length == 2) {
                    String[] smsInfoString2 = mModelsString[i + 1].split(ConfigConstantUtils.IMPORT_EXPORT_SMS_SEPARATOR_TAB_ONE);
                    if (smsInfoString2.length != 1) {
                        handleMessage(filePath, ConfigConstantUtils.IMPORT_FILE_ERROR);
                        Log.e("import2SmsDb", "import num[" + i + "] msg length=2 IMPORT_FILE_ERROR!");
                        return;
                    }
                    mValues = new ContentValues();
                    mValues.put(SMS_ANDRESS_COLUMN, smsInfoString[0]);
                    if (i == 0) {
                        if (smsInfoString[1].contains("/")) {
                            mFormat = ConfigConstantUtils.DATE_FOURTH_TYPE;
                        } else {
                            mFormat = ConfigConstantUtils.DATE_SECOND_TYPE;
                        }
                    }
                    mValues.put(SMS_DATE_COLUMN, ConfigConstantUtils.formatDate2Long(mFormat, smsInfoString[1]));
                    mValues.put(SMS_TYPE_COLUMN, Integer.parseInt(smsInfoString2[0]));
                    mValues.put(SMS_BODY_COLUMN, "");
                    mValues.put(SMS_READ_COLUMN, 1);
                    mValues.put(SMS_SEEN_COLUMN, 1);
                    mValuesList.add(mValues);
                    i++;
                    Log.d(TAG, "ImportExportSms--------------readFromFile---------read one empty message!");
                } else {
                    handleMessage(filePath,  ConfigConstantUtils.IMPORT_FILE_ERROR);
                    Log.e("import2SmsDb", "import num[" + i + "] msg length=" + smsInfoString.length + " IMPORT_FILE_ERROR!!");
                    return;
                }
            }
           ContentValues[] mValuesArray = null;
                
           //Gionee <guoyx> <2013-05-15> modified for CR00772651 begin
            try {
                if (!mCancelImport) {
                    if (mValuesList.size() > MAX_INSERT_ONECE) {
                        ArrayList<List<ContentValues>> importValuesArrayList = getInsertValues(mValuesList, new ArrayList<List<ContentValues>>());
                        for (int i = 0; i < importValuesArrayList.size() && !mCancelImport; i++) {
                            mValuesArray = importValuesArrayList .get(i).toArray(new ContentValues[importValuesArrayList.get(i).size()]);
                            Log.i("import2SmsDb", "import sms bulkInsert ValuesArray[" + i + "] lenth:" + mValuesArray.length);
                            Uri uri = Uri .parse(ConfigConstantUtils.IMPORT_SMS_URI.toString() + "/" + ConfigConstantUtils.getMessageLimit(mContext));
                            mContext.getContentResolver().bulkInsert(uri, mValuesArray);
                        }
                        importValuesArrayList.clear();
                        importValuesArrayList = null;
                    } else {
                        mValuesArray = mValuesList.toArray(new ContentValues[mValuesList.size()]);
                        Uri uri = Uri.parse(ConfigConstantUtils.IMPORT_SMS_URI.toString() + "/" + ConfigConstantUtils.getMessageLimit(mContext));
                        mContext.getContentResolver().bulkInsert(uri, mValuesArray);
                    }
                }
                mValuesArray = null;
                if (!mCancelImport) {
                    callBack(ConfigConstantUtils.IMPORT_COMPLETE_CODE, filePath);
                }
            } catch (Exception e) {
                handleMessage(filePath, ConfigConstantUtils.IMPORT_FILE_ERROR);
                Log.e(TAG, "ImportExportSms--------------readFromFile---------read object exception!");
                e.printStackTrace();
            }
            //Gionee <guoyx> <2013-05-15> modified for CR00772651 end
        } else {
            handleMessage(filePath, ConfigConstantUtils.IMPORT_FILE_NOT_EXISTS);
            Log.d(TAG, "ImportExportSms--------------readFromFile---------File not exists!");
        }
    }

    private ArrayList<List<ContentValues>> getInsertValues(List<ContentValues> valuesList, ArrayList<List<ContentValues>> insertValues) {
        if (valuesList.size() / MAX_INSERT_ONECE > 0) {
            insertValues.add(valuesList.subList(0, MAX_INSERT_ONECE));
            valuesList = valuesList.subList(MAX_INSERT_ONECE, valuesList.size());
            getInsertValues(valuesList, insertValues);
        } else {
            insertValues.add(valuesList);
        }
        return insertValues;
    }

    private void exportFromSmsDb() {
        if (null != mThreadIds && mThreadIds.size() > 0 && !mIsInterrupt) {
            if (!mExportFileReady && !mIsInterrupt) {
                mFile = new File(ImportExportSmsActivity.mDefaultSDCardPath + mContext.getString(R.string.gn_import_export_file_path));
                if (!mFile.exists() && !mIsInterrupt) {
                    mFile.mkdirs();
                }
                mFile = new File(ImportExportSmsActivity.mDefaultSDCardPath + mContext.getString(R.string.gn_import_export_file_path) + ConfigConstantUtils.formatDate2String(ConfigConstantUtils.DATE_FIRST_TYPE, System.currentTimeMillis()) + ConfigConstantUtils.IMPORT_EXPORT_SMS_FILE_EXTENSION);
            }
            if (!mFile.exists() && !mIsInterrupt) {
                try {
                    mFile.createNewFile();
                    ConfigConstantUtils.addUTF8Header(mFile);
                    if (!mExportFileReady && !mIsInterrupt) {
                        if (!mIsInterrupt) {
                            callBack(ConfigConstantUtils.EXPORT_FILE_READY, mFile.getAbsolutePath());
                        }
                        mExportFileReady = true;
                        return;
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                    // gionee zhouyj 2012-08-07 modify for CR00667794 start 
                    if (!ConfigConstantUtils.isAvaiableSpace(mAvaiableSpace, ImportExportSmsActivity.mDefaultSDCardPath, mContext) && !mIsInterrupt) {
                        Log.d(TAG, "ImportExportSms--------------exportFromSmsDb---------no space!");
                        callBack(ConfigConstantUtils.SDCARD_NO_SPACE, null);
                        mIsInterrupt = true;
                        return;
                    }
                    // gionee zhouyj 2012-08-07 modify for CR00667794 end 
                }
            }
            if (null != mHandler && !mIsInterrupt) {
                mHandler.sendEmptyMessage(1);
            }

            // gionee zhouyj 2013-04-08 add for CR00794257 start
            mExportSmsReadyCount = 0;
            // gionee zhouyj 2013-04-08 add for CR00794257 end
            for (int i=0; null != mThreadIds && i<mThreadIds.size() && !mIsInterrupt;i++) {
                if (mIsInterrupt && mFile.exists()) {
                    mFile.delete();
                    return;
                }
                mImportExportHandler.startQuery(EXPORT_QUERY_COMPLETE_TOKEN, i, ConfigConstantUtils.IMPORT_EXPORT_SMS_QUERY_URI, PROJECTION, Sms.THREAD_ID + " = '" + mThreadIds.get(i) + "' and " + Sms.TYPE + " != " + Telephony.TextBasedSmsColumns.MESSAGE_TYPE_DRAFT, null, Sms.DATE + " ASC");
                //gionee gaoj 2012-8-22 modified for CR00678531 end
            }
        }
    }

    private void write2File(Cursor cursor) {
        buildExportMmsInfoModels(cursor);
        try {
            if (!mIsInterrupt) {
                // Aurora xuyong 2013-11-15 added for bug #788 start
                if (mFile == null) {
                    return;
                }
                // Aurora xuyong 2013-11-15 added for bug #788 end
                FileOutputStream outputStream = new FileOutputStream(mFile, true);
                OutputStreamWriter outWriter = new OutputStreamWriter(outputStream, "utf-8");
                BufferedWriter writer = new BufferedWriter(outWriter);
                if (null != mModels && mModels.getCount() > 0 && !mIsInterrupt) {
                    ArrayList<SmsInfoModel> infoes = mModels.getSmsInfoModels();
                    String itemInfo = "";
                    for (int i=0;i<infoes.size() && !mIsInterrupt;i++) {
                        itemInfo = infoes.get(i).getSmsAddress() + ConfigConstantUtils.IMPORT_EXPORT_SMS_SEPARATOR_TAB_ONE + infoes.get(i).getSmsDate() + ConfigConstantUtils.IMPORT_EXPORT_SMS_SEPARATOR_TAB_ONE
                        + infoes.get(i).getSmsBody() + ConfigConstantUtils.IMPORT_EXPORT_SMS_SEPARATOR_TAB_ONE + infoes.get(i).getSmsType() + ConfigConstantUtils.IMPORT_EXPORT_SMS_SEPARATOR_TAB_TWO;
                        mAvaiableSpace += itemInfo.getBytes().length;
                        writer.write(itemInfo);
                    }
                }
                writer.flush();
                writer.close();
                outWriter.close();
                outputStream.close();
                // gionee zhouyj 2012-08-07 modify for CR00667794 start 
                if (!ConfigConstantUtils.isAvaiableSpace(mAvaiableSpace, ImportExportSmsActivity.mDefaultSDCardPath, mContext)  && !mIsInterrupt) {
                    callBack(ConfigConstantUtils.SDCARD_NO_SPACE, null);
                    mIsInterrupt = true;
                    return;
                }
                // gionee zhouyj 2012-08-07 modify for CR00667794 end 
            }
        } catch (IOException e) {
            e.printStackTrace();
            // gionee zhouyj 2012-08-07 modify for CR00667794 start 
            if (!ConfigConstantUtils.isAvaiableSpace(mAvaiableSpace, ImportExportSmsActivity.mDefaultSDCardPath, mContext) && !mIsInterrupt) {
                Log.d(TAG, "ImportExportSms--------------write2File---------no space!");
                callBack(ConfigConstantUtils.SDCARD_NO_SPACE, null);
                mIsInterrupt = true;
                return;
            }
            // gionee zhouyj 2012-08-07 modify for CR00667794 end 
        }
    }

    private SmsInfoModel buildExportMmsInfoModels(Cursor cursor) {
        if (null == mModels) {
            mModels = new SmsInfoModel();
        } else {
            mModels.resetSmsInfoModels();
        }
        if (null != cursor && cursor.getCount() > 0) {
            cursor.moveToPosition(-1);
            while(cursor.moveToNext()) {
                SmsInfoModel mModel = new SmsInfoModel();
                mModel.setSmsAddress(cursor.getString(cursor.getColumnIndexOrThrow(SMS_ANDRESS_COLUMN)));
                mModel.setSmsDate(ConfigConstantUtils.formatDate2String(mFormat, cursor.getLong(cursor.getColumnIndexOrThrow(SMS_DATE_COLUMN))));
                mModel.setSmsType(cursor.getInt(cursor.getColumnIndexOrThrow(SMS_TYPE_COLUMN)));
                if (null == cursor.getString(cursor.getColumnIndexOrThrow(SMS_BODY_COLUMN))) {
                    mModel.setSmsBody("");
                } else {
                    mModel.setSmsBody(cursor.getString(cursor.getColumnIndexOrThrow(SMS_BODY_COLUMN)).replaceAll(ConfigConstantUtils.IMPORT_EXPORT_SMS_SEPARATOR_TAB_ONE, "    "));
                }
                mModels.addSmsInfoModel(mModel);
            }
            cursor.close();
            cursor = null;
        }
        return mModels;
    }

    class ImportExportHandler extends AsyncQueryHandler {

        public ImportExportHandler(ContentResolver resolver) {
            super(resolver);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            // TODO Auto-generated method stub
            switch (token) {
                case EXPORT_QUERY_COMPLETE_TOKEN:
                    write2File(cursor);
                    // gionee zhouyj 2013-04-08 modify for CR00794257 start
                    if (++mExportSmsReadyCount >= mThreadIds.size() && !mIsInterrupt) {
                    // gionee zhouyj 2013-04-08 modify for CR00794257 end
                        mFile = null;
                        mThreadIds = null;
                        mExportFileReady = false;
                        // gionee zhouyj 2013-04-08 add for CR00794257 start
                        mExportSmsReadyCount = 0;
                        // gionee zhouyj 2013-04-08 add for CR00794257 end
                        if (!mIsInterrupt) {
                            // gionee zhouyj 2012-08-07 modify for CR00667794 start 
                            if (!ConfigConstantUtils.isAvaiableSpace(mAvaiableSpace, ImportExportSmsActivity.mDefaultSDCardPath, mContext)) {
                                Log.d(TAG, "ImportExportSms--------------onQueryComplete---------no space!");
                                callBack(ConfigConstantUtils.SDCARD_NO_SPACE, null);
                                mIsInterrupt = true;
                                return;
                            }
                            // gionee zhouyj 2012-08-07 modify for CR00667794 end 
                            callBack(ConfigConstantUtils.EXPORT_COMPLETE_CODE, null);
                            return;
                        }
                    } else if (!mIsInterrupt && null != mHandler) {
                        mHandler.sendEmptyMessage(Integer.valueOf(cookie.toString()) + 2);
                    }
                    break;
                default:
                    break;
            }
            super.onQueryComplete(token, cookie, cursor);
        }
    }

    public void cancelImport() {
        mCancelImport = true;
        new Thread() {
            public void run() {
                if (null != mContext) {
                    mContext.getContentResolver().bulkInsert(ConfigConstantUtils.IMPORT_SMS_CANCEL_URI, null);
                }
            }
        }.start();
    }
    
    // gionee zhouyj 2012-05-28 add for CR00607938 start
    private void importDb2SmsDb(String filePath) {
        File file = new File(filePath);
        final String path = filePath;
        if(file.exists()) {
            if (path.length() <= 0) {
                handleMessage(filePath, ConfigConstantUtils.IMPORT_FILE_EMPTY);
                return;
            } else if (null != mHandler) {
                Message msg = Message.obtain();
                msg.obj = filePath;
                msg.what = ConfigConstantUtils.IMPORT_FILE_READY;
                if (!mIsInterrupt) {
                    mHandler.sendMessage(msg);
                }
            }
            new Thread() {
                public void run() {
                    SQLiteDatabase db;
                    Cursor cursor = null; 
                    try {
                        db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
                        cursor = db.query(TABLE_SMS, SMS_COLUMNS, null, null, null, null, "date ASC");
                        if (cursor == null) {
                            handleMessage(path, ConfigConstantUtils.IMPORT_FILE_ERROR);
                            return;
                        }
                        int count = cursor.getCount();
                        if (count > 0) {
                            boolean loop = true;
                            while (cursor.moveToNext() && loop) {
                                copyToPhoneMemory(cursor);
                            }
                        }
                        db.close();
                    } catch (SQLiteException e) {
                        handleMessage(path, ConfigConstantUtils.IMPORT_FILE_ERROR);
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                        Recycler.getSmsRecycler().deleteOldMessages(ImportExportSms.this.mContext);
                    }
                }
            }.start();
            callBack(ConfigConstantUtils.IMPORT_COMPLETE_CODE, filePath);
        } else {
            handleMessage(filePath, ConfigConstantUtils.IMPORT_FILE_NOT_EXISTS);
        }
    }
    
    private void copyToPhoneMemory(Cursor cursor) {
        ContentResolver mContentResolver = mContext.getContentResolver();
        String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
        String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        Long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
        int currentSimId = cursor.getInt(cursor.getColumnIndexOrThrow(Sms.SIM_ID));
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
        mContext.getContentResolver().insert(Sms.CONTENT_URI, insertValues);
    }
    // gionee zhouyj 2012-05-28 add for CR00607938 end
    
    // gionee zhouyj 2012-12-17 add for CR00746518 start 
    public boolean isInterrupt(){
        return mIsInterrupt;
    }
    // gionee zhouyj 2012-12-17 add for CR00746518 end 
}
