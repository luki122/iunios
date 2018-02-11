package com.aurora.settings.providers;

import aurora.provider.AuroraSettings;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.util.Log;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.app.backup.BackupManager;

public class AuroraProvider extends ContentProvider {

    private static final String TAG = "AuroraProvider";
    private static final boolean LOCAL_LOGV = true;
    
    private AuroraDBHelper mOpenHelper = null;
    private BackupManager mBackupManager;
    private static final String[] COLUMN_VALUE = new String[] { "value" };
    
    @Override
    public boolean onCreate() {
        if (mOpenHelper == null) {
            mOpenHelper = new AuroraDBHelper(getContext());
        }
        
        mBackupManager = new BackupManager(getContext());
        
        return true;
    }
    
    private SQLiteDatabase gnGetReadableDatabase() {
        if (null == mOpenHelper) {
            mOpenHelper = new AuroraDBHelper(getContext());
        }
        return mOpenHelper.getReadableDatabase();
    }
    
    private SQLiteDatabase gnGetWritableDatabase() {
        if (null == mOpenHelper) {
            mOpenHelper = new AuroraDBHelper(getContext());
        }
        return mOpenHelper.getWritableDatabase();
    }
    
    @Override
    public Bundle call(String method, String name, Bundle args) {
        if (AuroraSettings.CALL_METHOD_GET_CONFIG.equals(method)) {
            return lookupValue(name);
        } else if (AuroraSettings.CALL_METHOD_SET_CONFIG.equals(method)) {
            return putString(name, args.getString(AuroraSettings.VALUE));
        } else {
            return null;
        }
    }
    
    private void sendNotify(Uri uri, int userHandle) {
        // Update the system property *first*, so if someone is listening for
        // a notification and then using the contract class to get their data,
        // the system property will be updated and they'll get the new data.

        boolean backedUpDataChanged = false;
        String property = null, table = uri.getPathSegments().get(0);

        if (LOCAL_LOGV) Log.v(TAG, "table: " + table);
        
        if (table.equals(AuroraSettings.TABLE_CONFIG)) {
            property = AuroraSettings.AMIGO_PROP_SETTING_VERSION;    // this one is global
            backedUpDataChanged = true;
            if (LOCAL_LOGV) Log.v(TAG, "property: " + property);
        }

        if (property != null) {
            long version = SystemProperties.getLong(property, 0) + 1;
            if (LOCAL_LOGV) Log.v(TAG, "property: " + property + "=" + version);
            SystemProperties.set(property, Long.toString(version));
        }

        // Inform the backup manager about a data change
        if (backedUpDataChanged) {
            mBackupManager.dataChanged();
        }
        // Now send the notification through the content framework.

        String notify = uri.getQueryParameter("notify");
        if (notify == null || "true".equals(notify)) {
            final int notifyTarget = userHandle;
            final long oldId = Binder.clearCallingIdentity();
            try {
                getContext().getContentResolver().notifyChange(uri, null, true, notifyTarget);
            } finally {
                Binder.restoreCallingIdentity(oldId);
            }
            if (LOCAL_LOGV) Log.v(TAG, "notifying for " + notifyTarget + ": " + uri);
        } else {
            if (LOCAL_LOGV) Log.v(TAG, "notification suppressed: " + uri);
        }
    }

    private Bundle lookupValue(String name) {
        Log.d(TAG, "lookupValue get value from name " + name);
        
        SQLiteDatabase db = gnGetReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(AuroraSettings.TABLE_CONFIG, COLUMN_VALUE, "name=?", new String[]{ name },
                              null, null, null, null);
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                
                Bundle bundle = new Bundle();
                bundle.putString(AuroraSettings.VALUE, cursor.getString(0));
                return bundle;
            }
        } catch (SQLiteException e) {
            Log.w(TAG, "aurorasettings lookup error", e);
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }
    
    public Bundle putString(String name, String value) {
        Log.d(TAG, "AuroraSettings put string name = " + name + " , value = " + value);
        
        SQLiteDatabase db = gnGetWritableDatabase();
        
        final ContentValues values = new ContentValues();
        values.put(AuroraSettings.NAME, name);
        values.put(AuroraSettings.VALUE, value);
        
        int count = db.update(AuroraSettings.TABLE_CONFIG, values, "name=?", new String[] { name });
        Bundle bundle = new Bundle();
        bundle.putBoolean(AuroraSettings.VALUE, (count > 0) ? true : false);
        
        sendNotify(AuroraSettings.getUriFor(name), UserHandle.getCallingUserId());
        return bundle;
    }
    
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) { 
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        return 0;
    }

}
