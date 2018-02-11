
package com.aurora.ota.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.aurora.ota.database.DataBaseCreator;

import gn.com.android.update.utils.LogUtils;
import gn.com.android.update.utils.Util;

import java.util.HashMap;

public class RepoterProvider extends ContentProvider {

    private static HashMap<String, String> sReporersProjectionMap;
    private static HashMap<String, String> sModuleProjectionMap;
    private static final String[] READ_REPORTER_PROJECTION = new String[] {
            Repoters.Columns._ID,
            Repoters.Columns.KEY_APP_VERSION,
            Repoters.Columns.KEY_APP_NAME,
            Repoters.Columns.KEY_IMEI,
            Repoters.Columns.KEY_CHANEL,
            Repoters.Columns.KEY_MOBILE_MODEL,
            Repoters.Columns.KEY_MOBILE_NUMBER,
            Repoters.Columns.KEY_REGISTER_USER_ID,
            Repoters.Columns.KEY_REPORTED,
            Repoters.Columns.KEY_SHUT_DOWN_TIME,
            Repoters.Columns.KEY_CREATE_ITEM_TIME,
            Repoters.Columns.KEY_STATUS,
            Repoters.Columns.KEY_PHONE_SIZE,
            Repoters.Columns.KEY_LOCATION,
            Repoters.Columns.KEY_APP_NUM,
            Repoters.Columns.KEY_BOOT_TIME,
            Repoters.Columns.KEY_DURATION_TIME

    };

    private static final int ITEMS = 1;

    private static final int ITEM = 2;
    
    private static final int MODULE_ITEMS = 3;

    private static final int MODULE_ITEM = 4;

    private static final UriMatcher sUriMatcher;
    
    Cursor c;
    private static final String ITEM_LIST_TYPE = "vnd.android.cursor.dir/reporter";
    
    private static final String ITEM_TYPE = "vnd.android.cursor.item/reporter";
    
    private DataBaseCreator mDbCreator;
    static {

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        sUriMatcher.addURI(Repoters.AUTHORITY, "reporter", ITEMS);

        sUriMatcher.addURI(Repoters.AUTHORITY, "reporter/#", ITEM);
        
        sUriMatcher.addURI(Repoters.AUTHORITY, "module", MODULE_ITEMS);

        sUriMatcher.addURI(Repoters.AUTHORITY, "module/#", MODULE_ITEM);

        sReporersProjectionMap = new HashMap<String, String>();

        // Maps the string "_ID" to the column name "_ID"
        sReporersProjectionMap.put(Repoters.Columns._ID, Repoters.Columns._ID);

        sReporersProjectionMap.put(Repoters.Columns._ID, Repoters.Columns._ID);
        sReporersProjectionMap.put(Repoters.Columns.KEY_APP_VERSION,Repoters.Columns.KEY_APP_VERSION);
        sReporersProjectionMap.put(Repoters.Columns.KEY_APP_NAME, Repoters.Columns.KEY_APP_NAME);
        sReporersProjectionMap.put(Repoters.Columns.KEY_IMEI, Repoters.Columns.KEY_IMEI);
        sReporersProjectionMap.put(Repoters.Columns.KEY_CHANEL, Repoters.Columns.KEY_CHANEL);
        
        sReporersProjectionMap.put(Repoters.Columns.KEY_MOBILE_MODEL,Repoters.Columns.KEY_MOBILE_MODEL);
        sReporersProjectionMap.put(Repoters.Columns.KEY_MOBILE_NUMBER,Repoters.Columns.KEY_MOBILE_NUMBER);
        sReporersProjectionMap.put(Repoters.Columns.KEY_REGISTER_USER_ID,Repoters.Columns.KEY_REGISTER_USER_ID);
        sReporersProjectionMap.put(Repoters.Columns.KEY_REPORTED, Repoters.Columns.KEY_REPORTED);
        sReporersProjectionMap.put(Repoters.Columns.KEY_SHUT_DOWN_TIME,Repoters.Columns.KEY_SHUT_DOWN_TIME);
        sReporersProjectionMap.put(Repoters.Columns.KEY_CREATE_ITEM_TIME,Repoters.Columns.KEY_CREATE_ITEM_TIME);
        sReporersProjectionMap.put(Repoters.Columns.KEY_STATUS, Repoters.Columns.KEY_STATUS);
        sReporersProjectionMap.put(Repoters.Columns.KEY_PHONE_SIZE, Repoters.Columns.KEY_PHONE_SIZE);
        sReporersProjectionMap.put(Repoters.Columns.KEY_LOCATION, Repoters.Columns.KEY_LOCATION);
        sReporersProjectionMap.put(Repoters.Columns.KEY_APP_NUM, Repoters.Columns.KEY_APP_NUM);
        sReporersProjectionMap.put(Repoters.Columns.KEY_BOOT_TIME, Repoters.Columns.KEY_BOOT_TIME);
        sReporersProjectionMap.put(Repoters.Columns.KEY_DURATION_TIME, Repoters.Columns.KEY_DURATION_TIME);
        
        sModuleProjectionMap = new HashMap<String, String>();
        sModuleProjectionMap.put(Repoters.Columns.KEY_MODULE, Repoters.Columns.KEY_MODULE);
        sModuleProjectionMap.put(Repoters.Columns.KEY_MODULE_ITEM, Repoters.Columns.KEY_MODULE_ITEM);
        sModuleProjectionMap.put(Repoters.Columns.KEY_VALUE, Repoters.Columns.KEY_VALUE);
    }

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
    	Log.i("777", "RepoterProvider  onCreate ");
        mDbCreator = DataBaseCreator.getInstance(getContext());
        return true;
    }

    
    
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
     // Opens the database object in "write" mode.
        SQLiteDatabase db = mDbCreator.getWritableDatabase();
        String finalWhere;

        int count;

        // Does the delete based on the incoming URI pattern.
        switch (sUriMatcher.match(uri)) {

            // If the incoming pattern matches the general pattern for notes, does a delete
            // based on the incoming "where" columns and arguments.
            case ITEMS:
                count = db.delete(
                    DataBaseCreator.Tables.DB_TABLE,  // The database table name
                    selection,                     // The incoming where clause column names
                    selectionArgs                  // The incoming where clause values
                );
                break;
            case MODULE_ITEMS:
                count = db.delete(
                    DataBaseCreator.Tables.DB_MODULE_TABLE,  // The database table name
                    selection,                     // The incoming where clause column names
                    selectionArgs                  // The incoming where clause values
                );
                break;

                // If the incoming URI matches a single note ID, does the delete based on the
                // incoming data, but modifies the where clause to restrict it to the
                // particular note ID.
            case ITEM:
                /*
                 * Starts a final WHERE clause by restricting it to the
                 * desired note ID.
                 */
                finalWhere =
                        Repoters.Columns._ID +                              // The ID column name
                        " = " +                                          // test for equality
                        uri.getPathSegments().                           // the incoming note ID
                            get( Repoters.Columns.REPORTER_ID_PATH_POSITION)
                ;

                // If there were additional selection criteria, append them to the final
                // WHERE clause
                if (selection != null) {
                    finalWhere = finalWhere + " AND " + selection;
                }

                // Performs the delete.
                count = db.delete(
                    DataBaseCreator.Tables.DB_TABLE,  // The database table name.
                    finalWhere,                // The final WHERE clause
                    selectionArgs                  // The incoming where clause values.
                );
                break;
            case MODULE_ITEM:
                /*
                 * Starts a final WHERE clause by restricting it to the
                 * desired note ID.
                 */
                finalWhere =
                        Repoters.Columns._ID +                              // The ID column name
                        " = " +                                          // test for equality
                        uri.getPathSegments().                           // the incoming note ID
                            get( Repoters.Columns.REPORTER_ID_PATH_POSITION)
                ;

                // If there were additional selection criteria, append them to the final
                // WHERE clause
                if (selection != null) {
                    finalWhere = finalWhere + " AND " + selection;
                }

                // Performs the delete.
                count = db.delete(
                    DataBaseCreator.Tables.DB_MODULE_TABLE,  // The database table name.
                    finalWhere,                // The final WHERE clause
                    selectionArgs                  // The incoming where clause values.
                );
                break;

            // If the incoming pattern is invalid, throws an exception.
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        /*Gets a handle to the content resolver object for the current context, and notifies it
         * that the incoming URI changed. The object passes this along to the resolver framework,
         * and observers that have registered themselves for the provider are notified.
         */
        getContext().getContentResolver().notifyChange(uri, null);

        // Returns the number of rows deleted.
        return count;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM:
            case MODULE_ITEM:
                return ITEM_TYPE;
                
            case ITEMS:
            case MODULE_ITEMS:
                return ITEM_LIST_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (sUriMatcher.match(uri) != ITEMS && sUriMatcher.match(uri) != MODULE_ITEMS ) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;

        if (initialValues != null) {
            values = new ContentValues(initialValues);

        } else {
            values = new ContentValues();
        }
	if(sUriMatcher.match(uri) == MODULE_ITEMS){
		SQLiteDatabase db = mDbCreator.getWritableDatabase();
		
        // Performs the insert and returns the ID of the new note.
        long rowId = db.insert(
            DataBaseCreator.Tables.DB_MODULE_TABLE,        // The table to insert into.
            null,  // A hack, SQLite sets this column value to null
                                             // if values is empty.
            values                           // A map of column names, and the values to insert
                                             // into the columns.
        );

        // If the insert succeeded, the row ID exists.
        if (rowId > 0) {
            // Creates a URI with the note ID pattern and the new row ID appended to it.
            Uri mUri = ContentUris.withAppendedId(Repoters.Columns.CONTENT_MODULE_URI_ITEMS, rowId);

            // Notifies observers registered against this provider that the data changed.
            getContext().getContentResolver().notifyChange(mUri, null);
            return mUri;
        }
		
	} else {
	        if (values.containsKey(Repoters.Columns.KEY_APP_VERSION) == false) {
	            values.put(Repoters.Columns.KEY_APP_VERSION, "1.0");
	        }
	        if (values.containsKey(Repoters.Columns.KEY_APP_NAME) == false) {
	            values.put(Repoters.Columns.KEY_APP_NAME, "IUNI OS");
	        }
	
	        // If the values map doesn't contain a title, sets the value to the default title.
	        if (values.containsKey(Repoters.Columns.KEY_IMEI) == false) {
	            values.put(Repoters.Columns.KEY_IMEI, "");
	        }
	
	        if (values.containsKey(Repoters.Columns.KEY_CHANEL) == false) {
	            values.put(Repoters.Columns.KEY_CHANEL, "IUNI OS");
	        }
	        if (values.containsKey(Repoters.Columns.KEY_MOBILE_MODEL) == false) {
	            values.put(Repoters.Columns.KEY_MOBILE_MODEL, "ANDROID");
	        }
	        if (values.containsKey(Repoters.Columns.KEY_MOBILE_MODEL) == false) {
	            values.put(Repoters.Columns.KEY_MOBILE_MODEL, Util.getModel());
	        }
	        if (values.containsKey(Repoters.Columns.KEY_MOBILE_NUMBER) == false) {
	            values.put(Repoters.Columns.KEY_MOBILE_NUMBER, "");
	        }
	        if (values.containsKey(Repoters.Columns.KEY_REGISTER_USER_ID) == false) {
	            values.put(Repoters.Columns.KEY_REGISTER_USER_ID, "");
	        }
	        if (values.containsKey(Repoters.Columns.KEY_REPORTED) == false) {
	            values.put(Repoters.Columns.KEY_REPORTED, 0);
	        }
	        if (values.containsKey(Repoters.Columns.KEY_SHUT_DOWN_TIME) == false) {
	            values.put(Repoters.Columns.KEY_SHUT_DOWN_TIME, "");
	        }
	        if (values.containsKey(Repoters.Columns.KEY_CREATE_ITEM_TIME) == false) {
	            values.put(Repoters.Columns.KEY_CREATE_ITEM_TIME, "");
	        }
	        if (values.containsKey(Repoters.Columns.KEY_STATUS) == false) {
	            values.put(Repoters.Columns.KEY_STATUS, 0);
	        }
	        if (values.containsKey(Repoters.Columns.KEY_PHONE_SIZE) == false) {
	            values.put(Repoters.Columns.KEY_PHONE_SIZE, " ");
	        }
	        if (values.containsKey(Repoters.Columns.KEY_LOCATION) == false) {
	            values.put(Repoters.Columns.KEY_LOCATION, " ");
	        }
	        if (values.containsKey(Repoters.Columns.KEY_APP_NUM) == false) {
	            values.put(Repoters.Columns.KEY_APP_NUM, 0);
	        }
	        if (values.containsKey(Repoters.Columns.KEY_BOOT_TIME) == false) {
	            values.put(Repoters.Columns.KEY_BOOT_TIME, "");
	        }
	        if (values.containsKey(Repoters.Columns.KEY_DURATION_TIME) == false) {
	        	LogUtils.log("1122", "come here is boring");
	            values.put(Repoters.Columns.KEY_DURATION_TIME, "");
	        }
	        LogUtils.log("1122", "come here is integrant");
	        LogUtils.log("1122", "DURATION_TIME :  "  +  values.getAsString(Repoters.Columns.KEY_DURATION_TIME));
	
	        // Opens the database object in "write" mode.
	        SQLiteDatabase db = mDbCreator.getWritableDatabase();
	
	        // Performs the insert and returns the ID of the new note.
	        long rowId = db.insert(
	            DataBaseCreator.Tables.DB_TABLE,        // The table to insert into.
	            null,  // A hack, SQLite sets this column value to null
	                                             // if values is empty.
	            values                           // A map of column names, and the values to insert
	                                             // into the columns.
	        );
	
	        // If the insert succeeded, the row ID exists.
	        if (rowId > 0) {
	            // Creates a URI with the note ID pattern and the new row ID appended to it.
	            Uri mUri = ContentUris.withAppendedId(Repoters.Columns.CONTENT_URI_ITEMS, rowId);
	
	            // Notifies observers registered against this provider that the data changed.
	            getContext().getContentResolver().notifyChange(mUri, null);
	            return mUri;
	        }
	}

        // If the insert didn't succeed, then the rowID is <= 0. Throws an exception.
        throw new SQLException("Failed to insert row into " + uri);
    }
    

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
    		
        // Constructs a new query builder and sets its table name
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
            case ITEMS:
            		qb.setTables(DataBaseCreator.Tables.DB_TABLE);
                qb.setProjectionMap(sReporersProjectionMap);
                break;
            case MODULE_ITEMS:
            		qb.setTables(DataBaseCreator.Tables.DB_MODULE_TABLE);
                qb.setProjectionMap(sModuleProjectionMap);
                break;

            case ITEM:
            		qb.setTables(DataBaseCreator.Tables.DB_TABLE);
                qb.setProjectionMap(sReporersProjectionMap);
                qb.appendWhere(
                    Repoters.Columns._ID +   
                    "=" +
                    uri.getPathSegments().get(Repoters.Columns.REPORTER_ID_PATH_POSITION));
                break;
            case MODULE_ITEM:
            		qb.setTables(DataBaseCreator.Tables.DB_MODULE_TABLE);
                qb.setProjectionMap(sModuleProjectionMap);
                qb.appendWhere(
                    Repoters.Columns._ID +   
                    "=" +
                    uri.getPathSegments().get(Repoters.Columns.REPORTER_ID_PATH_POSITION));
                break;


            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }


        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = Repoters.Columns.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        SQLiteDatabase db = mDbCreator.getReadableDatabase();
//        if(projection == null){
//            projection = new String[READ_REPORTER_PROJECTION.length];
//        }
//        projection = READ_REPORTER_PROJECTION.clone();
        Cursor c = qb.query(
            db,            // The database to query
            projection,    // The columns to return from the query
            selection,     // The columns for the where clause
            selectionArgs, // The values for the where clause
            null,          // don't group the rows
            null,          // don't filter by row groups
            orderBy        // The sort order
        );

        // Tells the Cursor what URI to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    	
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
     // Opens the database object in "write" mode.
        SQLiteDatabase db = mDbCreator.getWritableDatabase();
        int count = 0;
        String finalWhere;

        // Does the update based on the incoming URI pattern
        switch (sUriMatcher.match(uri)) {

            // If the incoming URI matches the general notes pattern, does the update based on
            // the incoming data.
            case ITEMS:

                // Does the update and returns the number of rows updated.
                count = db.update(
                    DataBaseCreator.Tables.DB_TABLE, // The database table name.
                    values,                   // A map of column names and new values to use.
                    selection,                    // The where clause column names.
                    selectionArgs                 // The where clause column values to select on.
                );
                break;

            // If the incoming URI matches a single note ID, does the update based on the incoming
            // data, but modifies the where clause to restrict it to the particular note ID.
            case ITEM:
                // From the incoming URI, get the note ID
                String noteId = uri.getPathSegments().get(Repoters.Columns.REPORTER_ID_PATH_POSITION);

                /*
                 * Starts creating the final WHERE clause by restricting it to the incoming
                 * note ID.
                 */
                finalWhere =
                        Repoters.Columns._ID +                              // The ID column name
                        " = " +                                          // test for equality
                        uri.getPathSegments().                           // the incoming note ID
                            get(Repoters.Columns.REPORTER_ID_PATH_POSITION)
                ;

                // If there were additional selection criteria, append them to the final WHERE
                // clause
                if (selection !=null) {
                    finalWhere = finalWhere + " AND " + selection;
                }


                // Does the update and returns the number of rows updated.
                count = db.update(
                    DataBaseCreator.Tables.DB_TABLE, // The database table name.
                    values,                   // A map of column names and new values to use.
                    finalWhere,               // The final WHERE clause to use
                                              // placeholders for whereArgs
                    selectionArgs                 // The where clause column values to select on, or
                                              // null if the values are in the where argument.
                );
                break;
            case MODULE_ITEM:
            case MODULE_ITEMS:
                if (values == null || values.size() < 3) {
                    throw new IllegalArgumentException("Error values");
                }
            	if(!values.containsKey(Repoters.Columns.KEY_VALUE) || !values.containsKey(Repoters.Columns.KEY_MODULE) || !values.containsKey(Repoters.Columns.KEY_MODULE_ITEM)){
            		throw new IllegalArgumentException("ContentValues must be  contain  required key !!!!!!" );
            	}
            	String key = values.getAsString(Repoters.Columns.KEY_MODULE);
            	String tag = values.getAsString(Repoters.Columns.KEY_MODULE_ITEM);
            	Log.i("data", "the key is  : " + key);
            	Log.i("data", "the tag is  : "  + tag);
            	Log.i("data", "the value is  : " + values.getAsString(Repoters.Columns.KEY_VALUE));
            	try{
            	 c = query(uri, null, "module_key=? and item_tag=?", new String[]{key,tag}, null);
            	 c.moveToFirst();
            	}catch(Exception e){
            		
            	}
            	if(null!= c &&0 == c.getCount()){
            		try{
            		Uri temp_Uri = insert(uri, values);
            		count = Integer.parseInt(temp_Uri.getLastPathSegment());
            		}catch(Exception e){
            			
            		}finally{
            			c.close();
            		}
            	}else{
            		values.remove(Repoters.Columns.KEY_MODULE);
        			values.remove(Repoters.Columns.KEY_MODULE_ITEM);
	            	try{
	            		int tmp = values.getAsInteger(Repoters.Columns.KEY_VALUE).intValue();
	            		if(tmp >= 1){
	            //	db.execSQL("update "+DataBaseCreator.Tables.DB_MODULE_TABLE +" set " +Repoters.Columns.KEY_VALUE + "=" + Repoters.Columns.KEY_VALUE+"+" +
	            		//	values.getAsInteger(Repoters.Columns.KEY_VALUE).intValue() + " where " + Repoters.Columns.KEY_MODULE + " = " +"'" + values.getAsString(Repoters.Columns.KEY_MODULE) + "'" +" and " + Repoters.Columns.KEY_MODULE_ITEM + " = " + "'" + values.getAsString(Repoters.Columns.KEY_MODULE_ITEM) + "'");
	            			//tmp += c.getInt(c.getColumnIndex(Repoters.Columns.KEY_VALUE));
	            			tmp += c.getInt(0);
	            			values.put(Repoters.Columns.KEY_VALUE,tmp);
	            			count = db.update(DataBaseCreator.Tables.DB_MODULE_TABLE, values, "module_key=? and item_tag=?",  new String[]{key,tag});
	            		} else {
	            			//db.execSQL("update "+DataBaseCreator.Tables.DB_MODULE_TABLE +" set " +Repoters.Columns.KEY_VALUE + "=" + values.getAsInteger(Repoters.Columns.KEY_VALUE).intValue() +
	            			//		" where " +Repoters.Columns.KEY_MODULE + " = " +"'" + values.getAsString(Repoters.Columns.KEY_MODULE)+ "'" +" and " + Repoters.Columns.KEY_MODULE_ITEM + " = " + "'" + values.getAsString(Repoters.Columns.KEY_MODULE_ITEM) + "'");
	            			count = db.update(DataBaseCreator.Tables.DB_MODULE_TABLE, values, "module_key=? and item_tag=?",  new String[]{key,tag});
	            		}
	                 
	                 } catch(Exception e ){
	                	 Log.i("RepoterProvider", "Exception =  " + e.getMessage());
	                	return -1;
	                 }finally{
	                c.close();
	                 }
            	}
            	
            	break;
            // If the incoming pattern is invalid, throws an exception.
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        Log.i("data", "the result is  : "  + count +"  (非0为成功) ");
        return count;
    }
    
    
    
    
    
 
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

}
