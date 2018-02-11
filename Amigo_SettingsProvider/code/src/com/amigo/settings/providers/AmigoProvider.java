package com.amigo.settings.providers;

import java.io.FileNotFoundException;

import amigo.provider.AmigoSettings;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.os.SystemProperties;
import android.os.UserHandle;
//import android.provider.DrmStore;
import android.provider.MediaStore;
import android.app.backup.BackupManager;

public class AmigoProvider extends ContentProvider {

    private static final String TAG = "AmigoProvider";
    private static final boolean LOCAL_LOGV = true;
    
    private AmigoDBHelper mOpenHelper = null;
    private BackupManager mBackupManager;
    private static final String[] COLUMN_VALUE = new String[] { "value" };

    // Make sure these are in sync with RingtoneManager.java: begin
    public static final int TYPE_RINGTONE2 = 16;
    public static final int TYPE_MMS = 32;
    public static final int TYPE_MMS2 = 64;
    // Make sure these are in sync with RingtoneManager.java: end
	
    @Override
    public boolean onCreate() {
        if (mOpenHelper == null) {
            mOpenHelper = new AmigoDBHelper(getContext());
        }
        
        mBackupManager = new BackupManager(getContext());
        
        return true;
    }
    
    private SQLiteDatabase gnGetReadableDatabase() {
        if (null == mOpenHelper) {
            mOpenHelper = new AmigoDBHelper(getContext());
        }
        return mOpenHelper.getReadableDatabase();
    }
    
    private SQLiteDatabase gnGetWritableDatabase() {
        if (null == mOpenHelper) {
            mOpenHelper = new AmigoDBHelper(getContext());
        }
        return mOpenHelper.getWritableDatabase();
    }
    
    @Override
    public Bundle call(String method, String name, Bundle args) {
        if (AmigoSettings.CALL_METHOD_GET_CONFIG.equals(method)) {
            return lookupValue(name);
        } else if (AmigoSettings.CALL_METHOD_SET_CONFIG.equals(method)) {
            return putString(name, args.getString(AmigoSettings.VALUE));
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
        
        if (table.equals(AmigoSettings.TABLE_CONFIG)) {
            property = AmigoSettings.AMIGO_PROP_SETTING_VERSION;    // this one is global
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
        //Log.d(TAG, "lookupValue get value from name " + name);
        
        SQLiteDatabase db = gnGetReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(AmigoSettings.TABLE_CONFIG, COLUMN_VALUE, "name=?", new String[]{ name },
                              null, null, null, null);
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                
                Bundle bundle = new Bundle();
                bundle.putString(AmigoSettings.VALUE, cursor.getString(0));
                return bundle;
            }
        } catch (SQLiteException e) {
            Log.w(TAG, "amigosettings lookup error", e);
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }
    
    public Bundle putString(String name, String value) {
        Log.d(TAG, "AmigoSettings put string name = " + name + " , value = " + value);
        
        SQLiteDatabase db = gnGetWritableDatabase();
        
        final ContentValues values = new ContentValues();
        values.put(AmigoSettings.NAME, name);
        values.put(AmigoSettings.VALUE, value);
        
        int count = db.update(AmigoSettings.TABLE_CONFIG, values, "name=?", new String[] { name });
        Bundle bundle = new Bundle();
        bundle.putBoolean(AmigoSettings.VALUE, (count > 0) ? true : false);
        
        sendNotify(AmigoSettings.getUriFor(name), UserHandle.getCallingUserId());
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

    //Gionee <hanbj> <20140108> add for CR00972980 begin
    @Override
    public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
        /*
         * When a client attempts to openFile the default ringtone or
         * notification setting Uri, we will proxy the call to the current
         * default ringtone's Uri (if it is in the media provider).
         */
        int ringtoneType = RingtoneManager.getDefaultType(uri);
        // Above call returns -1 if the Uri doesn't match a default type
        if (ringtoneType != -1) {
            Context context = getContext();

            // Get the current value for the default sound
            Uri soundUri = RingtoneManager.getActualDefaultRingtoneUri(context, ringtoneType);

            if (soundUri != null) {
                // Proxy the openFile call to media provider
                String authority = soundUri.getAuthority();
                if (authority.equals(MediaStore.AUTHORITY)) {
                    ParcelFileDescriptor pfd = null;
                    try {
                        pfd = context.getContentResolver().openFileDescriptor(soundUri, mode);
                        return new AssetFileDescriptor(pfd, 0, -1);
                    } catch (FileNotFoundException ex) {
                        // fall through and open the fallback ringtone below
                    }
                }

                try {
                    return super.openAssetFile(soundUri, mode);
                } catch (FileNotFoundException ex) {
                    // Since a non-null Uri was specified, but couldn't be opened,
                    // fall back to the built-in ringtone.            	    
                	String filename = getDefaultRingtoneFile(ringtoneType);
                	if (LOCAL_LOGV) Log.d(TAG, "the filename is " + filename + " " + "ringtoneType is " + ringtoneType);
                	if (filename!=null){   	
					    if(Environment.getExternalStorageState().equals(Environment.MEDIA_BAD_REMOVAL) 
							|| Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED) 
                            || Environment.getExternalStorageState().equals(Environment.MEDIA_UNMOUNTABLE)){

						    if (LOCAL_LOGV) Log.d(TAG,"SDcard in unable status ,so set default ringtone");
						    setDefaultRingtone(ringtoneType, filename);
            	    	    return super.openAssetFile(uri, mode);
					    } else {
						    try{
							    String ringtoneStr = getRingtoneUri(ringtoneType,filename);
							    if(ringtoneStr != null){
								    ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(Uri.parse(getRingtoneUri(ringtoneType,filename)), mode);
		                    	    return new AssetFileDescriptor(pfd, 0, -1);
							    }else{
								    return super.openAssetFile(getDefaultUri(context, ringtoneType), mode);
							    }
                		    } catch (FileNotFoundException exx) {
                			    Log.d(TAG,"default ringtone file is not found!");
                		    }				
					    } 
                    }
                	
                    return context.getResources().openRawResourceFd(com.android.internal.R.raw.fallbackring);
                }
            }
            // no need to fall through and have openFile() try again, since we
            // already know that will fail.
            throw new FileNotFoundException(); // or return null ?
        }

        // Note that this will end up calling openFile() above.
        return super.openAssetFile(uri, mode);
    }
    
    private Uri getDefaultUri(Context context, int type) {
    	Log.d(TAG, "getDefaultUri: unexpectedType = " + type);
        return null;
    }
    
    private String getDefaultRingtoneFile(int type) {
    	String DEFAULT_RINGTONE_PROPERTY_PREFIX = "ro.config.";
        String filename = SystemProperties.get(DEFAULT_RINGTONE_PROPERTY_PREFIX + AmigoSettings.RINGTONE2);

        switch (type) {
            //case RingtoneManager.TYPE_RINGTONE2:
            case TYPE_RINGTONE2:
                filename = SystemProperties.get(DEFAULT_RINGTONE_PROPERTY_PREFIX + AmigoSettings.RINGTONE2);
                break;
            //case RingtoneManager.TYPE_MMS:
			case TYPE_MMS:
                filename = SystemProperties.get(DEFAULT_RINGTONE_PROPERTY_PREFIX + AmigoSettings.MMS);
                break;
            //case RingtoneManager.TYPE_MMS2:
			case TYPE_MMS2:
                filename = SystemProperties.get(DEFAULT_RINGTONE_PROPERTY_PREFIX + AmigoSettings.MMS2);
                break;
            default:
                break;
        }
        Log.d(TAG, "getDefaultRingtoneFile = " + filename);
        return filename;
    }

    private void setDefaultRingtone(int type, String filename) {
        String name = AmigoSettings.RINGTONE2;
        String where = "'/system/media/audio/ringtones/";
        switch (type) {
            //case RingtoneManager.TYPE_RINGTONE2:
			case TYPE_RINGTONE2:
                name = AmigoSettings.RINGTONE2;
                where = "'/system/media/audio/ringtones/";
                break;
            //case RingtoneManager.TYPE_MMS:
			case TYPE_MMS:
                name = AmigoSettings.MMS;
                where = "'/system/media/audio/notifications/";
                break;
            //case RingtoneManager.TYPE_MMS2:
			case TYPE_MMS2:
                name = AmigoSettings.MMS2;
                where = "'/system/media/audio/notifications/";
                break;
            default:
                break;
        }
        
        Log.d(TAG, "where = " + where + filename);
        Cursor cursor = getContext().getContentResolver().query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI
                , new String[] {"_id"}, MediaStore.Audio.Media.DATA + " = " + where + filename + "'", null, null);
        
        while (cursor.moveToNext()) {
            long rowId = cursor.getLong(cursor.getColumnIndex("_id"));
            AmigoSettings.putString(getContext().getContentResolver(), name, ContentUris
                    .withAppendedId(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, rowId).toString());
        }
        cursor.close();
    }
    
    private String getRingtoneUri(int type,String filename){
		String where = "'/system/media/audio/ringtones/";
        switch (type) {
            //case RingtoneManager.TYPE_RINGTONE2:
			case TYPE_RINGTONE2:
			    where = "'/system/media/audio/ringtones/";
                break;
            //case RingtoneManager.TYPE_MMS:
			case TYPE_MMS:
			    where = "'/system/media/audio/notifications/";
                break;
            //case RingtoneManager.TYPE_MMS2:
			case TYPE_MMS2:
			    where = "'/system/media/audio/notifications/";
                break;
            default:
                break;
        }
        
        Cursor cursor = getContext().getContentResolver().query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI
                    , new String[] {"_id"}, MediaStore.Audio.Media.DATA + " = " + where + filename + "'", null, null);
        
		String uri = null;
        while (cursor.moveToNext()) {
            long rowId = cursor.getLong(cursor.getColumnIndex("_id"));
            uri = ContentUris.withAppendedId(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, rowId).toString();
        }
        cursor.close();	
		return uri;
	}
    //Gionee <hanbj> <20140108> add for CR00972980 end
}
