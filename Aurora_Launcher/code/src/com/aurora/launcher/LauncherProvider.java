/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aurora.launcher;

import android.app.SearchManager;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import com.aurora.launcher.R;
import com.aurora.launcher.LauncherSettings.Favorites;
import com.aurora.util.DeviceProperties;
import com.aurora.util.DeviceProperties.DeviceCategories;
import com.aurora.util.Utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LauncherProvider extends ContentProvider {
    private static final String TAG = "Launcher.LauncherProvider";
    private static final boolean LOGD = false;

    private static final String DATABASE_NAME = "launcher.db";

    //iconcat,vulcan changed 18 to 19 in 2014-7-10
    private static final int DATABASE_VERSION = 20;
    //private static final int DATABASE_VERSION = 18;

    static final String AUTHORITY = "com.aurora.launcher.settings";

    static final String TABLE_FAVORITES = "favorites";
    static final String TABLE_FAVORITES_BACKUP= "favorites_backup";
    static final String TABLE_APP_CATEGORY = "app_info_category";
    static final String PARAMETER_NOTIFY = "notify";
    static final String DB_CREATED_BUT_DEFAULT_WORKSPACE_NOT_LOADED =
            "DB_CREATED_BUT_DEFAULT_WORKSPACE_NOT_LOADED";
    static final String DEFAULT_WORKSPACE_RESOURCE_ID =
            "DEFAULT_WORKSPACE_RESOURCE_ID";

    private static final String ACTION_APPWIDGET_DEFAULT_WORKSPACE_CONFIGURE =
            "com.aurora.launcher.action.APPWIDGET_DEFAULT_WORKSPACE_CONFIGURE";

    /**
     * {@link Uri} triggered at any registered {@link android.database.ContentObserver} when
     * {@link AppWidgetHost#deleteHost()} is called during database creation.
     * Use this to recall {@link AppWidgetHost#startListening()} if needed.
     */
    static final Uri CONTENT_APPWIDGET_RESET_URI =
            Uri.parse("content://" + AUTHORITY + "/appWidgetReset");

    private DatabaseHelper mOpenHelper;
    
    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        ((LauncherApplication) getContext()).setLauncherProvider(this);
        return true;
    }

    @Override
    public String getType(Uri uri) {
        SqlArguments args = new SqlArguments(uri, null, null);
        if (TextUtils.isEmpty(args.where)) {
            return "vnd.android.cursor.dir/" + args.table;
        } else {
            return "vnd.android.cursor.item/" + args.table;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {

        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(args.table);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor result = qb.query(db, projection, args.where, args.args, null, null, sortOrder);
        result.setNotificationUri(getContext().getContentResolver(), uri);

        return result;
    }

    private static long dbInsertAndCheck(DatabaseHelper helper,
            SQLiteDatabase db, String table, String nullColumnHack, ContentValues values) {
    	
		if (values != null) {
			String intentStr;
			intentStr = values.getAsString(LauncherSettings.Favorites.INTENT);
			if (intentStr != null) {
				if (intentStr.contains("sourceBounds=")) {
					LauncherApplication.logVulcan.print("dbInsertAndCheck: intent = " + intentStr);
					LauncherApplication.logVulcan.print(LogWriter.StackToString(new Throwable()));
				}
			}
			else {
				LauncherApplication.logVulcan.print("dbInsertAndCheck: intentStr is null");
			}
		}
		else {
			LauncherApplication.logVulcan.print("dbInsertAndCheck: values is null");
		}
    	
        if (!values.containsKey(LauncherSettings.Favorites._ID)) {
            throw new RuntimeException("Error: attempting to add item without specifying an id");
        }
        return db.insert(table, nullColumnHack, values);
    }

    private static void deleteId(SQLiteDatabase db, long id) {
        Uri uri = LauncherSettings.Favorites.getContentUri(id, false);
        SqlArguments args = new SqlArguments(uri, null, null);
        db.delete(args.table, args.where, args.args);
		LauncherApplication.logVulcan.print("delete item on deleteId, 6");
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        SqlArguments args = new SqlArguments(uri);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final long rowId = dbInsertAndCheck(mOpenHelper, db, args.table, null, initialValues);
        if (rowId <= 0) return null;

        uri = ContentUris.withAppendedId(uri, rowId);
        sendNotify(uri);

        return uri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SqlArguments args = new SqlArguments(uri);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int numValues = values.length;
            for (int i = 0; i < numValues; i++) {
                if (dbInsertAndCheck(mOpenHelper, db, args.table, null, values[i]) < 0) {
                    return 0;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        sendNotify(uri);
        return values.length;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
    	
    	Thread.dumpStack();
  
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.delete(args.table, args.where, args.args);
        if (count > 0) sendNotify(uri);

		LauncherApplication.logVulcan.print("delete item on delete, count = " + count + "uri = " + uri + ",select = " + selection + "where = " + selectionArgs);
		
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.update(args.table, values, args.where, args.args);
        if (count > 0) sendNotify(uri);

        return count;
    }

    private void sendNotify(Uri uri) {
        String notify = uri.getQueryParameter(PARAMETER_NOTIFY);
        if (notify == null || "true".equals(notify)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }

    public long generateNewId() {
        return mOpenHelper.generateNewId();
    }
    
    /**
     * vulcan created this method in 2014-8-14
     * get database helper instance
     * @return
     */
    public DatabaseHelper getDatabaseHelper() {
    	return this.mOpenHelper;
    }

    /**
     * @param workspaceResId that can be 0 to use default or non-zero for specific resource
     */
    synchronized public void loadDefaultFavoritesIfNecessary(int origWorkspaceResId) {
        String spKey = LauncherApplication.getSharedPreferencesKey();
        SharedPreferences sp = getContext().getSharedPreferences(spKey, Context.MODE_PRIVATE);
        if (sp.getBoolean(DB_CREATED_BUT_DEFAULT_WORKSPACE_NOT_LOADED, false)) {
            int workspaceResId = origWorkspaceResId;

            // Use default workspace resource if none provided
            if (workspaceResId == 0) {
//            	boolean iuniDevice = SystemProperties.get("ro.product.device", "aurora").toUpperCase().equals("IUNI");
//            	Log.i(TAG, "iuniDevice = " + iuniDevice);
//            	if(iuniDevice) {
//            		workspaceResId = sp.getInt(DEFAULT_WORKSPACE_RESOURCE_ID, R.xml.iuni_default_workspace);
//            	} else if(DeviceProperties.isNeedScale()) {
//            		workspaceResId = sp.getInt(DEFAULT_WORKSPACE_RESOURCE_ID, R.xml.note_three_default_workspace);
//            	} else {
//            		workspaceResId = sp.getInt(DEFAULT_WORKSPACE_RESOURCE_ID, R.xml.default_workspace);
//            	}
            	/**Another way to achieve this method*/
            	Log.e("linp", "============loadDefaultFavorites============"+DeviceProperties.getSysProductDeviceName());
            	switch (DeviceProperties.getSysProductDeviceName()) {
				case IUNI_DEVICE_PROPERTIES:
					workspaceResId = sp.getInt(DEFAULT_WORKSPACE_RESOURCE_ID, R.xml.iuni_default_workspace);
					break;

				case SAMSUNG_DEVICE_PROPERTIES:
					workspaceResId = sp.getInt(DEFAULT_WORKSPACE_RESOURCE_ID, R.xml.note_three_default_workspace);
					break;

				case XIAOMI_DEVICE_PROPERTIES:
					Log.e("linp", "============xiaomi");
					
					workspaceResId = sp.getInt(DEFAULT_WORKSPACE_RESOURCE_ID, R.xml.xiaomi_default_workspace);
					break;
				
				default:
					workspaceResId = sp.getInt(DEFAULT_WORKSPACE_RESOURCE_ID, R.xml.default_workspace);
					break;
				}
            }

            // Populate favorites table with initial favorites
            SharedPreferences.Editor editor = sp.edit();
            editor.remove(DB_CREATED_BUT_DEFAULT_WORKSPACE_NOT_LOADED);
            if (origWorkspaceResId != 0) {
                editor.putInt(DEFAULT_WORKSPACE_RESOURCE_ID, origWorkspaceResId);
            }
            
            mOpenHelper.loadFavorites(mOpenHelper.getWritableDatabase(), workspaceResId);
            editor.commit();
        }
    }

    static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String TAG_FAVORITES = "favorites";
        private static final String TAG_FAVORITE = "favorite";
        private static final String TAG_CLOCK = "clock";
        private static final String TAG_SEARCH = "search";
        private static final String TAG_APPWIDGET = "appwidget";
        private static final String TAG_SHORTCUT = "shortcut";
        private static final String TAG_FOLDER = "folder";
        private static final String TAG_EXTRA = "extra";
        
        private static final String TAG_DOCK = "favorite_dock";

        private final Context mContext;
        AppWidgetHost mAppWidgetHost;
        private long mMaxId = -1;

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mContext = context;
            mAppWidgetHost = new AppWidgetHost(context, Launcher.APPWIDGET_HOST_ID);
            // In the case where neither onCreate nor onUpgrade gets called, we read the maxId from
            // the DB here
            if (mMaxId == -1) {
                mMaxId = initializeMaxId(getWritableDatabase());
            }
        }

        /**
         * Send notification that we've deleted the {@link AppWidgetHost},
         * probably as part of the initial database creation. The receiver may
         * want to re-call {@link AppWidgetHost#startListening()} to ensure
         * callbacks are correctly set.
         */
        private void sendAppWidgetResetNotify() {
            final ContentResolver resolver = mContext.getContentResolver();
            resolver.notifyChange(CONTENT_APPWIDGET_RESET_URI, null);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            if (LOGD) Log.d(TAG, "creating new launcher database");

            mMaxId = 1;

            // Aurora <jialf> <2013-09-23> modify for loading data begin
            db.execSQL("CREATE TABLE favorites (" +
                    "_id INTEGER PRIMARY KEY," +
                    "title TEXT," +
                    "simplePinyin TEXT," +
                    "fullPinyin TEXT," +
                    "simle_t9 TEXT," +
                    "full_t9 TEXT," +
                    "intent TEXT," +
                    "container INTEGER," +
                    "screen INTEGER," +
                    "cellX INTEGER," +
                    "cellY INTEGER," +
                    "spanX INTEGER," +
                    "spanY INTEGER," +
                    "itemType INTEGER," +
                    "appWidgetId INTEGER NOT NULL DEFAULT -1," +
                    "isShortcut INTEGER," +
                    "iconType INTEGER," +
                    "iconPackage TEXT," +
                    "iconResource TEXT," +
                    "icon BLOB," +
                    "uri TEXT," +
                    "displayMode INTEGER," +
                    "flags INTEGER DEFAULT 0," +
                    "firstInstallTime INTEGER NOT NULL DEFAULT 0," + /*iconcat, vulcan modified it in 2014-7-10*/
                    "enableWidgets INTEGER NOT NULL DEFAULT 0" +
            		");");
            // Aurora <jialf> <2013-09-23> modify for loading data end

            createDefaultApptagsTable(db);
            
            // Database was just created, so wipe any previous widgets
            if (mAppWidgetHost != null) {
                mAppWidgetHost.deleteHost();
                sendAppWidgetResetNotify();
            }

            if (!convertDatabase(db)) {
                // Set a shared pref so that we know we need to load the default workspace later
                setFlagToLoadDefaultWorkspaceLater();
                insertDefaultAppTag(db);
            }
        }
      
        private void setFlagToLoadDefaultWorkspaceLater() {
            String spKey = LauncherApplication.getSharedPreferencesKey();
            SharedPreferences sp = mContext.getSharedPreferences(spKey, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(DB_CREATED_BUT_DEFAULT_WORKSPACE_NOT_LOADED, true);
            // Aurora <jialf> <2013-09-09> add for loading data begin
			editor.putBoolean(FIRST_QUERY_ALL_APPS, true);
			//editor.putString(DEFAULT_LANGUAGE, Utilities.getCurrentLanuage(mContext));
		    // Aurora <jialf> <2013-09-09> add for loading data end
            editor.commit();
        }

        private boolean convertDatabase(SQLiteDatabase db) {
            if (LOGD) Log.d(TAG, "converting database from an older format, but not onUpgrade");
            boolean converted = false;

            final Uri uri = Uri.parse("content://" + Settings.AUTHORITY +
                    "/old_favorites?notify=true");
            final ContentResolver resolver = mContext.getContentResolver();
            Cursor cursor = null;

            try {
                cursor = resolver.query(uri, null, null, null, null);
            } catch (Exception e) {
                // Ignore
            }

            // We already have a favorites database in the old provider
            if (cursor != null && cursor.getCount() > 0) {
                try {
                    converted = copyFromCursor(db, cursor) > 0;
                } finally {
                    cursor.close();
                }

                if (converted) {
                    resolver.delete(uri, null, null);
					LauncherApplication.logVulcan.print("delete item on convertDatabase, 8");
                }
            }

            if (converted) {
                // Convert widgets from this import into widgets
                if (LOGD) Log.d(TAG, "converted and now triggering widget upgrade");
                convertWidgets(db);
            }

            return converted;
        }

        private int copyFromCursor(SQLiteDatabase db, Cursor c) {
            final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
            final int intentIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);
            final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
            final int iconTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_TYPE);
            final int iconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON);
            final int iconPackageIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_PACKAGE);
            final int iconResourceIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_RESOURCE);
            final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
            final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
            final int screenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
            final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
            final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);
            final int uriIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.URI);
            final int displayModeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.DISPLAY_MODE);

            ContentValues[] rows = new ContentValues[c.getCount()];
            int i = 0;
            while (c.moveToNext()) {
                ContentValues values = new ContentValues(c.getColumnCount());
                values.put(LauncherSettings.Favorites._ID, c.getLong(idIndex));
                values.put(LauncherSettings.Favorites.INTENT, c.getString(intentIndex));
                values.put(LauncherSettings.Favorites.TITLE, c.getString(titleIndex));
                values.put(LauncherSettings.Favorites.ICON_TYPE, c.getInt(iconTypeIndex));
                values.put(LauncherSettings.Favorites.ICON, c.getBlob(iconIndex));
                values.put(LauncherSettings.Favorites.ICON_PACKAGE, c.getString(iconPackageIndex));
                values.put(LauncherSettings.Favorites.ICON_RESOURCE, c.getString(iconResourceIndex));
                values.put(LauncherSettings.Favorites.CONTAINER, c.getInt(containerIndex));
                values.put(LauncherSettings.Favorites.ITEM_TYPE, c.getInt(itemTypeIndex));
                values.put(LauncherSettings.Favorites.APPWIDGET_ID, -1);
                values.put(LauncherSettings.Favorites.SCREEN, c.getInt(screenIndex));
                values.put(LauncherSettings.Favorites.CELLX, c.getInt(cellXIndex));
                values.put(LauncherSettings.Favorites.CELLY, c.getInt(cellYIndex));
                values.put(LauncherSettings.Favorites.URI, c.getString(uriIndex));
                values.put(LauncherSettings.Favorites.DISPLAY_MODE, c.getInt(displayModeIndex));
                rows[i++] = values;
            }

            db.beginTransaction();
            int total = 0;
            try {
                int numValues = rows.length;
                for (i = 0; i < numValues; i++) {
                    if (dbInsertAndCheck(this, db, TABLE_FAVORITES, null, rows[i]) < 0) {
                        return 0;
                    } else {
                        total++;
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            return total;
        }
        
        
        /**
         * iconcat, in specified table check if there is the specified field.
         * supposed the table is there.
         * @param db
         * @param tableName
         * @param columnName
         * @return
         */
        private boolean columnExist(SQLiteDatabase db, String tableName, String columnName) {
            boolean result = false ;
            Cursor cursor = null ;
            try{
                //查询一行
                cursor = db.rawQuery( "SELECT * FROM " + tableName + " LIMIT 0", null );
                result = cursor != null && cursor.getColumnIndex(columnName) != -1 ;
            }catch (Exception e){
                 Log.e(TAG,"checkColumnExists1..." + e.getMessage()) ;
            }finally{
                if(null != cursor && !cursor.isClosed()){
                    cursor.close() ;
                }
            }

            return result ;
        }
        
        /**
         * iconcat, add a new Field in database
         * @param db
         * @param strColName
         * @param type
         * @param canBeNull
         * @param defaultValue
         */
        private void addFieldOfFavorite(SQLiteDatabase db, String strColName, String type,boolean canBeNull, String defaultValue) {   	
        	String sqlStatement = null;

			if (!canBeNull) {
				sqlStatement = String.format(
						"ALTER TABLE favorites ADD COLUMN %s %s NOT NULL DEFAULT %s;",
						strColName, type, defaultValue);
			}
			else {
				sqlStatement = String.format(
						"ALTER TABLE favorites ADD COLUMN %s %s;",
						strColName, type);
			}
        	
        	Log.d("vulcan-db","sqlStatement = " + sqlStatement);
            db.beginTransaction();
            try {
                // Insert new column for holding appWidgetIds
            	/*
                db.execSQL("ALTER TABLE favorites " +
                    "ADD COLUMN appWidgetId INTEGER NOT NULL DEFAULT -1;");
                    */
                db.execSQL(sqlStatement);
                db.setTransactionSuccessful();
            } catch (SQLException ex) {
                // Old version remains, which means we wipe old data
                Log.e(TAG, ex.getMessage(), ex);
                Log.d("vulcan-db","SQLException = " + ex);
            } finally {
            	Log.e("vulcan-db", "####################addFieldOfFavorite completed");
                db.endTransaction();
            }
            return;
        }
        

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (LOGD) Log.d(TAG, "onUpgrade triggered");

            int version = oldVersion;
            if (version < 3) {
                // upgrade 1,2 -> 3 added appWidgetId column
                db.beginTransaction();
                try {
                    // Insert new column for holding appWidgetIds
                    db.execSQL("ALTER TABLE favorites " +
                        "ADD COLUMN appWidgetId INTEGER NOT NULL DEFAULT -1;");
                    db.setTransactionSuccessful();
                    version = 3;
                } catch (SQLException ex) {
                    // Old version remains, which means we wipe old data
                    Log.e(TAG, ex.getMessage(), ex);
                } finally {
                    db.endTransaction();
                }

                // Convert existing widgets only if table upgrade was successful
                if (version == 3) {
                    convertWidgets(db);
                }
            }

            if (version < 4) {
                version = 4;
            }

            // Where's version 5?
            // - Donut and sholes on 2.0 shipped with version 4 of launcher1.
            // - Passion shipped on 2.1 with version 6 of launcher2
            // - Sholes shipped on 2.1r1 (aka Mr. 3) with version 5 of launcher 1
            //   but version 5 on there was the updateContactsShortcuts change
            //   which was version 6 in launcher 2 (first shipped on passion 2.1r1).
            // The updateContactsShortcuts change is idempotent, so running it twice
            // is okay so we'll do that when upgrading the devices that shipped with it.
            if (version < 6) {
                // We went from 3 to 5 screens. Move everything 1 to the right
                db.beginTransaction();
                try {
                    db.execSQL("UPDATE favorites SET screen=(screen + 1);");
                    db.setTransactionSuccessful();
                } catch (SQLException ex) {
                    // Old version remains, which means we wipe old data
                    Log.e(TAG, ex.getMessage(), ex);
                } finally {
                    db.endTransaction();
                }

               // We added the fast track.
                if (updateContactsShortcuts(db)) {
                    version = 6;
                }
            }

            if (version < 7) {
                // Version 7 gets rid of the special search widget.
                convertWidgets(db);
                version = 7;
            }

            if (version < 8) {
                // Version 8 (froyo) has the icons all normalized.  This should
                // already be the case in practice, but we now rely on it and don't
                // resample the images each time.
                normalizeIcons(db);
                version = 8;
            }

            if (version < 9) {
                // The max id is not yet set at this point (onUpgrade is triggered in the ctor
                // before it gets a change to get set, so we need to read it here when we use it)
                if (mMaxId == -1) {
                    mMaxId = initializeMaxId(db);
                }

                // Add default hotseat icons
                loadFavorites(db, R.xml.update_workspace);
                version = 9;
            }

            // We bumped the version three time during JB, once to update the launch flags, once to
            // update the override for the default launch animation and once to set the mimetype
            // to improve startup performance
            if (version < 12) {
                // Contact shortcuts need a different set of flags to be launched now
                // The updateContactsShortcuts change is idempotent, so we can keep using it like
                // back in the Donut days
                updateContactsShortcuts(db);
                version = 12;
            }
            
			//iconcat, vulcan added it in 2014-7-11
            if(version < 18) {
            	Log.d("vulcan-db","onUpgrade: change version from less than 18 to equal 18, new version is 18");
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
				version = 18;
            }

            if(version < 19) {
            	Log.d("vulcan-db","onUpgrade: change version from 18 to equal 19, new version is 19");
            	Log.d("vulcan-db","onUpgrade: firstInstallTime fields of all the application & shortcut are set to 0");
            	//SimpleDateFormat df = new SimpleDateFormat("[yy-MM-dd hh:mm:ss]: ", Locale.CHINA);
            	//String strNow = df.format(new Date());
            	String strNow = "0";
            	addFieldOfFavorite(db,Favorites.FIRST_INSTALL_TIME,"INTEGER",false,strNow);
            	db.beginTransaction();
            	try {
                	createDefaultApptagsTable(db);
                	db.execSQL("DELETE FROM "+TABLE_APP_CATEGORY); //clean up target table name
                	insertDefaultAppTag(db);
                    db.setTransactionSuccessful();
                } catch (SQLException ex) {
                } finally {
                    db.endTransaction();
                }
            	version = 19;
            }
            if(version < 20) {
            	Log.d("vulcan-db","onUpgrade: change version from 19 to equal 20, new version is 20");
            	addFieldOfFavorite(db,Favorites.ENABLE_WIDGETS,"INTEGER",false,"0");
            	version = 20;
            }

            if (version != DATABASE_VERSION) {
                Log.w(TAG, "Destroying all old data.");
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
                onCreate(db);
            }
        }

        private boolean updateContactsShortcuts(SQLiteDatabase db) {
            final String selectWhere = buildOrWhereString(Favorites.ITEM_TYPE,
                    new int[] { Favorites.ITEM_TYPE_SHORTCUT });

            Cursor c = null;
            final String actionQuickContact = "com.android.contacts.action.QUICK_CONTACT";
            db.beginTransaction();
            try {
                // Select and iterate through each matching widget
                c = db.query(TABLE_FAVORITES,
                        new String[] { Favorites._ID, Favorites.INTENT },
                        selectWhere, null, null, null, null);
                if (c == null) return false;

                if (LOGD) Log.d(TAG, "found upgrade cursor count=" + c.getCount());

                final int idIndex = c.getColumnIndex(Favorites._ID);
                final int intentIndex = c.getColumnIndex(Favorites.INTENT);

                while (c.moveToNext()) {
                    long favoriteId = c.getLong(idIndex);
                    final String intentUri = c.getString(intentIndex);
                    if (intentUri != null) {
                        try {
                            final Intent intent = Intent.parseUri(intentUri, 0);
                            android.util.Log.d("Home", intent.toString());
                            final Uri uri = intent.getData();
                            if (uri != null) {
                                final String data = uri.toString();
                                if ((Intent.ACTION_VIEW.equals(intent.getAction()) ||
                                        actionQuickContact.equals(intent.getAction())) &&
                                        (data.startsWith("content://contacts/people/") ||
                                        data.startsWith("content://com.android.contacts/" +
                                                "contacts/lookup/"))) {

                                    final Intent newIntent = new Intent(actionQuickContact);
                                    // When starting from the launcher, start in a new, cleared task
                                    // CLEAR_WHEN_TASK_RESET cannot reset the root of a task, so we
                                    // clear the whole thing preemptively here since
                                    // QuickContactActivity will finish itself when launching other
                                    // detail activities.
                                    newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    newIntent.putExtra(
                                            Launcher.INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION, true);
                                    newIntent.setData(uri);
                                    // Determine the type and also put that in the shortcut
                                    // (that can speed up launch a bit)
                                    newIntent.setDataAndType(uri, newIntent.resolveType(mContext));

                                    final ContentValues values = new ContentValues();
                                    values.put(LauncherSettings.Favorites.INTENT,
                                            newIntent.toUri(0));
                                    
                            		if (values != null) {
                            			String intentStr;
                            			intentStr = values.getAsString(LauncherSettings.Favorites.INTENT);
                            			if (intentStr != null) {
                            				if (intentStr.contains("sourceBounds=")) {
                            					LauncherApplication.logVulcan.print(LogWriter
                            							.StackToString(new Throwable()));
                            				}
                            			}
                            		}

                                    String updateWhere = Favorites._ID + "=" + favoriteId;
                                    db.update(TABLE_FAVORITES, values, updateWhere, null);
                                }
                            }
                        } catch (RuntimeException ex) {
                            Log.e(TAG, "Problem upgrading shortcut", ex);
                        } catch (URISyntaxException e) {
                            Log.e(TAG, "Problem upgrading shortcut", e);
                        }
                    }
                }

                db.setTransactionSuccessful();
            } catch (SQLException ex) {
                Log.w(TAG, "Problem while upgrading contacts", ex);
                return false;
            } finally {
                db.endTransaction();
                if (c != null) {
                    c.close();
                }
            }

            return true;
        }

        private void normalizeIcons(SQLiteDatabase db) {
            Log.d(TAG, "normalizing icons");

            db.beginTransaction();
            Cursor c = null;
            SQLiteStatement update = null;
            try {
                boolean logged = false;
                update = db.compileStatement("UPDATE favorites "
                        + "SET icon=? WHERE _id=?");

                c = db.rawQuery("SELECT _id, icon FROM favorites WHERE iconType=" +
                        Favorites.ICON_TYPE_BITMAP, null);

                final int idIndex = c.getColumnIndexOrThrow(Favorites._ID);
                final int iconIndex = c.getColumnIndexOrThrow(Favorites.ICON);

                while (c.moveToNext()) {
                    long id = c.getLong(idIndex);
                    byte[] data = c.getBlob(iconIndex);
                    try {
                        Bitmap bitmap = Utilities.resampleIconBitmap(
                                BitmapFactory.decodeByteArray(data, 0, data.length),
                                mContext);
                        if (bitmap != null) {
                            update.bindLong(1, id);
                            data = ItemInfo.flattenBitmap(bitmap);
                            if (data != null) {
                                update.bindBlob(2, data);
                                update.execute();
                            }
                            bitmap.recycle();
                        }
                    } catch (Exception e) {
                        if (!logged) {
                            Log.e(TAG, "Failed normalizing icon " + id, e);
                        } else {
                            Log.e(TAG, "Also failed normalizing icon " + id);
                        }
                        logged = true;
                    }
                }
                db.setTransactionSuccessful();
            } catch (SQLException ex) {
                Log.w(TAG, "Problem while allocating appWidgetIds for existing widgets", ex);
            } finally {
                db.endTransaction();
                if (update != null) {
                    update.close();
                }
                if (c != null) {
                    c.close();
                }
            }
        }

        // Generates a new ID to use for an object in your database. This method should be only
        // called from the main UI thread. As an exception, we do call it when we call the
        // constructor from the worker thread; however, this doesn't extend until after the
        // constructor is called, and we only pass a reference to LauncherProvider to LauncherApp
        // after that point
        public long generateNewId() {
            if (mMaxId < 0) {
                throw new RuntimeException("Error: max id was not initialized");
            }
            mMaxId += 1;
            return mMaxId;
        }

        private long initializeMaxId(SQLiteDatabase db) {
            Cursor c = db.rawQuery("SELECT MAX(_id) FROM favorites", null);

            // get the result
            final int maxIdIndex = 0;
            long id = -1;
            if (c != null && c.moveToNext()) {
                id = c.getLong(maxIdIndex);
            }
            if (c != null) {
                c.close();
            }

            if (id == -1) {
                throw new RuntimeException("Error: could not query max id");
            }

            return id;
        }

        /**
         * Upgrade existing clock and photo frame widgets into their new widget
         * equivalents.
         */
        private void convertWidgets(SQLiteDatabase db) {
            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
            final int[] bindSources = new int[] {
                    Favorites.ITEM_TYPE_WIDGET_CLOCK,
                    Favorites.ITEM_TYPE_WIDGET_PHOTO_FRAME,
                    Favorites.ITEM_TYPE_WIDGET_SEARCH,
            };

            final String selectWhere = buildOrWhereString(Favorites.ITEM_TYPE, bindSources);

            Cursor c = null;

            db.beginTransaction();
            try {
                // Select and iterate through each matching widget
                c = db.query(TABLE_FAVORITES, new String[] { Favorites._ID, Favorites.ITEM_TYPE },
                        selectWhere, null, null, null, null);

                if (LOGD) Log.d(TAG, "found upgrade cursor count=" + c.getCount());

                final ContentValues values = new ContentValues();
                while (c != null && c.moveToNext()) {
                    long favoriteId = c.getLong(0);
                    int favoriteType = c.getInt(1);

                    // Allocate and update database with new appWidgetId
                    try {
                        int appWidgetId = mAppWidgetHost.allocateAppWidgetId();

                        if (LOGD) {
                            Log.d(TAG, "allocated appWidgetId=" + appWidgetId
                                    + " for favoriteId=" + favoriteId);
                        }
                        values.clear();
                        values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPWIDGET);
                        values.put(Favorites.APPWIDGET_ID, appWidgetId);

                        // Original widgets might not have valid spans when upgrading
                        if (favoriteType == Favorites.ITEM_TYPE_WIDGET_SEARCH) {
                            values.put(LauncherSettings.Favorites.SPANX, 4);
                            values.put(LauncherSettings.Favorites.SPANY, 1);
                        } else {
                            values.put(LauncherSettings.Favorites.SPANX, 2);
                            values.put(LauncherSettings.Favorites.SPANY, 2);
                        }

                        String updateWhere = Favorites._ID + "=" + favoriteId;
                        db.update(TABLE_FAVORITES, values, updateWhere, null);

                        if (favoriteType == Favorites.ITEM_TYPE_WIDGET_CLOCK) {
                            // TODO: check return value
                            appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId,
                                    new ComponentName("com.android.alarmclock",
                                    "com.android.alarmclock.AnalogAppWidgetProvider"));
                        } else if (favoriteType == Favorites.ITEM_TYPE_WIDGET_PHOTO_FRAME) {
                            // TODO: check return value
                            appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId,
                                    new ComponentName("com.android.camera",
                                    "com.android.camera.PhotoAppWidgetProvider"));
                        } else if (favoriteType == Favorites.ITEM_TYPE_WIDGET_SEARCH) {
                            // TODO: check return value
                            appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId,
                                    getSearchWidgetProvider());
                        }
                    } catch (RuntimeException ex) {
                        Log.e(TAG, "Problem allocating appWidgetId", ex);
                    }
                }

                db.setTransactionSuccessful();
            } catch (SQLException ex) {
                Log.w(TAG, "Problem while allocating appWidgetIds for existing widgets", ex);
            } finally {
                db.endTransaction();
                if (c != null) {
                    c.close();
                }
            }
        }

        private static final void beginDocument(XmlPullParser parser, String firstElementName)
                throws XmlPullParserException, IOException {
            int type;
            while ((type = parser.next()) != XmlPullParser.START_TAG
                    && type != XmlPullParser.END_DOCUMENT) {
                ;
            }

            if (type != XmlPullParser.START_TAG) {
                throw new XmlPullParserException("No start tag found");
            }

            if (!parser.getName().equals(firstElementName)) {
                throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() +
                        ", expected " + firstElementName);
            }
        }

        /**
         * Loads the default set of favorite packages from an xml file.
         *
         * @param db The database to write the values into
         * @param filterContainerId The specific container id of items to load
         */
        private int loadFavorites(SQLiteDatabase db, int workspaceResourceId) {
        	int i = startParserXmlFile(db,mContext);
        	return i;
        }

    	/***Hazel start to parser xml file in buildin folder or in other folder */
		private int  startParserXmlFile(SQLiteDatabase db,Context cx) {
			// start initialize variable
			HashMap<String, String> map = new HashMap<String, String>();
			HashMap<String, String> mExtraMap = new HashMap<String, String>();
			HashMap<String, String> mFolderItem = new HashMap<String, String>();
			Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            int allAppsButtonRank =
                    mContext.getResources().getInteger(R.integer.hotseat_all_apps_index);
            
			Resources res = mContext.getResources();
			String[] phone = res.getString(R.string.dock_phone_name).split("#");
			String[] contacts = res.getString(R.string.dock_contacts_name).split("#");
			String[] mms = res.getString(R.string.dock_mms_name).split("#");
			String[] browser = res.getString(R.string.dock_browser_name).split("#");
			String[] camera = res.getString(R.string.dock_camera_name).split("#");
			String[] music = res.getString(R.string.dock_music_name).split("#");
            
            ContentValues values = new ContentValues();
        	int tmpCount = 0;
			 PackageManager packageManager = mContext.getPackageManager();
			try {
				//use XmlPullParser referenced from GN Launcher source code 
				XmlPullParserFactory factory = XmlPullParserFactory
						.newInstance();
				factory.setNamespaceAware(true);
				XmlPullParser xpp = factory.newPullParser();
				Utilities.setXmlPullParserInput(cx, xpp);

				mFavotite.clear();
				int type = xpp.getEventType();
				int screenCount = 0;
			
				boolean appNotFound = false;

				beginDocument(xpp, TAG_FAVORITES);

				while (type != XmlPullParser.END_DOCUMENT) {
					type = xpp.next();
					if (type != XmlPullParser.START_TAG) {
						continue;
					}
					map.clear();

					int count = xpp.getAttributeCount();

					for (int x = 0; x < count; x++) {
						map.put(xpp.getAttributeName(x),
								xpp.getAttributeValue(x));
					}
					
					boolean added = false;
             
					long container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
					//TODO we need to check child elements whether have "container" keys or not
					if(map.containsKey("container")){
						container = Long.valueOf(map.get("container"));
					}
					

					
					String strScreen = map.get("screen");
					String strX = map.get("x");
					String strY = map.get("y");

					// convert String to Integer
					int screen = Integer.parseInt(strScreen);
					int x = Integer.parseInt(strX);
					int y = Integer.parseInt(strY);
					values.clear();
					values.put(LauncherSettings.Favorites.CONTAINER, container);
					if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT
							&& appNotFound) {
						values.put(LauncherSettings.Favorites.SCREEN,
								(screen - screenCount));
						values.put(LauncherSettings.Favorites.CELLX,
								(x - screenCount));
					} else {
						values.put(LauncherSettings.Favorites.SCREEN, strScreen);
						values.put(LauncherSettings.Favorites.CELLX, strX);
					}
					values.put(LauncherSettings.Favorites.CELLY, strY);

					String name = xpp.getName();
					
					 if (TAG_FAVORITE.equals(name)) {
						 long id = addAppShortcut(db, values, map, packageManager, intent);
	                        added = id >= 0;
	                        // Aurora <jialf> <2013-09-09> add for loading data begin
	                        if (container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
	                        	if (screen == mMaxScreen) {
	                        		if (x > mMaxCellX || y > mMaxCellY) {
	                        			mMaxCellX = x;
	                                    mMaxCellY = y;
	                                }
	                        	} else if(screen > mMaxScreen){
	                        			mMaxScreen = screen;
	                        			mMaxCellX = x;
	                        			mMaxCellY = y;
	                        	}
	                        }
					 }else if (TAG_SEARCH.equals(name)) {
	                        added = addSearchWidget(db, values);
	                        // Aurora <jialf> <2013-09-09> add for loading data begin
	                        if (screen == mMaxScreen) {
	                        	if ((x + 3) > mMaxCellX || y > mMaxCellY) {
	                        		mMaxCellX = x + 3;
	                        		mMaxCellY = y;
	                        	}
	                        } else if (screen > mMaxScreen) {
	                        	mMaxScreen = screen;
	                        	mMaxCellX = x + 3;
	                        	mMaxCellY = y;
	                        }
					 } else if (TAG_CLOCK.equals(name)) {
	                        added = addClockWidget(db, values);
	                        // Aurora <jialf> <2013-09-09> add for loading data begin
	                        if (screen == mMaxScreen) {
	                        	if ((x + 1) > mMaxCellX
	                        			|| (y + 1) > mMaxCellY) {
	                        		mMaxCellX = x + 1;
	                        		mMaxCellY = y + 1;
	                        	}
	                        } else if (screen > mMaxScreen) {
	                        	mMaxScreen = screen;
	                        	mMaxCellX = x + 1;
	                        	mMaxCellY = y + 1;
	                        }
					 }else if (TAG_APPWIDGET.equals(name)) {
						 added = addAppWidget(xpp, db, type,map,mExtraMap,values, packageManager);
					 }else if (TAG_SHORTCUT.equals(name)) {
						 long id = addUriShortcut(db,values,map);
						  // Aurora <jialf> <2013-09-09> add for loading data begin
	                        if (container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
	                        	if (screen == mMaxScreen) {
	                        		if (x > mMaxCellX || y > mMaxCellY) {
	                        			mMaxCellX = x;
	                        			mMaxCellY = y;
	                        		}
	                        	} else if(screen > mMaxScreen){
	                        		mMaxScreen = screen;
	                        		mMaxCellX = x;
	                        		mMaxCellY = y;
	                        	}
	                        }
	                        // Aurora <jialf> <2013-09-09> add for loading data end
	                        added = id >= 0;
					 }else if (TAG_FOLDER.equals(name)) {
						String title;
						int titleResId = getTitleResID(map.get("title"));
						if (titleResId != -1) {
							title = mContext.getResources().getString(
									titleResId);
						} else {
							title = mContext.getResources().getString(
									R.string.folder_name);
						}
						
						values.put(LauncherSettings.Favorites.TITLE, title);
						long folderId = addFolder(db, values);
						added = folderId >= 0;

						// Aurora <jialf> <2013-09-09> add for loading data
						// begin
						if (screen == mMaxScreen) {
							if (x > mMaxCellX || y > mMaxCellY) {
								mMaxCellX = x;
								mMaxCellY = y;
							}
						} else if (screen > mMaxScreen) {
							mMaxScreen = screen;
							mMaxCellX = x;
							mMaxCellY = y;
						}
						// Aurora <jialf> <2013-09-09> add for loading data end

						ArrayList<Long> folderItems = new ArrayList<Long>();
						int folderDepth = xpp.getDepth();
						mFolderItem.clear();
						while ((type = xpp.next()) != XmlPullParser.END_TAG
								|| xpp.getDepth() > folderDepth) {
							if (type != XmlPullParser.START_TAG) {
								continue;
							}
							// iterates child again
							int FolderItemCount = xpp.getAttributeCount();

							for (int j = 0; j < FolderItemCount; j++) {
								mFolderItem.put(xpp.getAttributeName(j),
										xpp.getAttributeValue(j));
							}
							
							final String folder_item_name = xpp.getName();

							values.clear();
							values.put(LauncherSettings.Favorites.CONTAINER,
									folderId);

							if (TAG_FAVORITE.equals(folder_item_name)
									&& folderId >= 0) {
								long id = addAppShortcut(db, values,
										mFolderItem, packageManager, intent);
								if (id >= 0) {
									folderItems.add(id);
								}
							} else if (TAG_SHORTCUT.equals(folder_item_name)
									&& folderId >= 0) {
								long id = addUriShortcut(db, values,
										mFolderItem);
								if (id >= 0) {
									folderItems.add(id);
								}
							} else {
								throw new RuntimeException("Folders can "
										+ "contain only shortcuts");
							}
						}
						// We can only have folders with >= 2 items, so we need
						// to remove the
						// folder and clean up if less than 2 items were
						// included, or some
						// failed to add, and less than 2 were actually added
						if (folderItems.size() < 2 && folderId >= 0) {
							// We just delete the folder and any items that made
							// it
							deleteId(db, folderId);
							if (folderItems.size() > 0) {
								// Aurora <jialf> <2013-11-14> modify for only
								// one item in folder begin
								// deleteId(db, folderItems.get(0));
								updateContainer(db, folderItems.get(0),
										container, strScreen, strX, strY);
								// Aurora <jialf> <2013-11-14> modify for only
								// one item in folder end
							}
							added = false;
						}
					 }else if (TAG_DOCK.equals(name)) {
						 String uri = map.get("queryIntent");
						 Intent in = null;
	                        try {
	                        	in = Intent.parseUri(uri, 0);
	                        } catch(Exception e){
	                        	Log.i(TAG, "parse uri exception ...");
	                        }
	                        Log.v("nn", in.toUri(0));
	                        List<ResolveInfo> infos = packageManager.queryIntentActivities(in, 0);
	            			ResolveInfo resolveInfo = null;
							switch(screen) {
							case 0:
								resolveInfo = filterAuroraDockApps(infos, packageManager, phone);
								break;
							case 1:
								resolveInfo = filterAuroraDockApps(infos, packageManager, mms);
								break;
							case 2:
								resolveInfo = filterAuroraDockApps(infos, packageManager, camera);
								break;
							case 3:
								resolveInfo = filterAuroraDockApps(infos, packageManager, music);
							case 4:
								resolveInfo = filterAuroraDockApps(infos, packageManager, browser);
								break;
							}
							 Log.v("nn", "resolveInfo = "+resolveInfo);
	                    	// Aurora <jialf> <2014-01-21> modify for fix bug #1601 begin
							if (resolveInfo == null) {
								appNotFound = true;
								screenCount++;
							}
	                    	// Aurora <jialf> <2014-01-21> modify for fix bug #1601 end
							added = addDockAppShortcut(db, values, packageManager, intent,resolveInfo) > 0;
	                    }
						  if (added) tmpCount++;
					 }
                appNotFound = false;
                screenCount = 0;
            }
            catch (XmlPullParserException e) {
                Log.w(TAG, "Got exception parsing favorites.", e);
            } catch (IOException e) {
                Log.w(TAG, "Got exception parsing favorites.", e);
            } catch (RuntimeException e) {
                Log.w(TAG, "Got exception parsing favorites.", e);
            }
			return tmpCount;
    	}
        
        
        private long addAppShortcut(SQLiteDatabase db, ContentValues values, TypedArray a,
                PackageManager packageManager, Intent intent) {
            long id = -1;
            ActivityInfo info;
            String packageName = a.getString(R.styleable.Favorite_packageName);
            String className = a.getString(R.styleable.Favorite_className);
            try {
                ComponentName cn;
                try {
                    cn = new ComponentName(packageName, className);
                    info = packageManager.getActivityInfo(cn, 0);
                } catch (PackageManager.NameNotFoundException nnfe) {
                    String[] packages = packageManager.currentToCanonicalPackageNames(
                        new String[] { packageName });
                    cn = new ComponentName(packages[0], className);
                    info = packageManager.getActivityInfo(cn, 0);
                }
                id = generateNewId();
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

                // Aurora <jialf> <2013-09-23> dd for loading data begin
                int flags = 0;
                try {
        			int appFlags = packageManager.getApplicationInfo(packageName, 0).flags;
        			if ((appFlags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
        				flags |= ApplicationInfo.DOWNLOADED_FLAG;

        				if ((appFlags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
        					flags |= ApplicationInfo.UPDATED_SYSTEM_APP_FLAG;
        				}
        			}
        		} catch (NameNotFoundException e) {
        			Log.d(TAG, "PackageManager.getApplicationInfo failed for " + packageName);
        		}
                values.put(Favorites.FLAGS, flags);
				LauncherApplication.logVulcan.print("getShortcutInfo: intent = " + intent + ", flags = " + flags);
                // Aurora <jialf> <2013-09-23> add for loading data end
                
                values.put(Favorites.INTENT, intent.toUri(0));
                // Aurora <haojj> <2014-1-14> add for 快速检索数据保存title和拼音数据 begin
                // values.put(Favorites.TITLE, info.loadLabel(packageManager).toString());
                String title = info.loadLabel(packageManager).toString();
                String titleStr = title != null ? title.toString().replace(' ', ' ').trim() : null;
                String fullPinyin = null; 
                String simplePinyin = null;
                String[] pinyinArray = Utils.getFullPinYin(titleStr);
                if(pinyinArray != null) {
        	        fullPinyin = pinyinArray[0];
        	        simplePinyin = pinyinArray[1];
                }
                values.put(Favorites.TITLE, title);
                values.put(Favorites.FULL_PINYIN, fullPinyin);
                values.put(Favorites.SIMPLE_PINYIN, simplePinyin);
				// Aurora <haojj> <2014-1-14> end
                values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPLICATION);
                values.put(Favorites.SPANX, 1);
                values.put(Favorites.SPANY, 1);
                values.put(Favorites._ID, id);
                if (dbInsertAndCheck(this, db, TABLE_FAVORITES, null, values) < 0) {
                    return -1;
                }
                mFavotite.add(intent.toUri(0));
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "Unable to add favorite: " + packageName +
                        "/" + className, e);
            }
            return id;
        }
        
        /**
         * iconcat, find package from pm and get the time as a String.
         * if package is not found, return null
         * @param pm instance of package manager
         * @param packageName name of package
         * @return if package is not found, return null
         */
        public String getPackageFirstInstallTime(Context context,String packageName) {
        	
        	if(context == null) {
        		return null;
        	}
        	
        	PackageManager pm = null;
        	pm = context.getPackageManager();
        	
        	if(pm == null) {
        		return null;
        	}
        
        	PackageInfo pi = null;
            try {
            	pi = pm.getPackageInfo(packageName, 0);
            } catch (NameNotFoundException e) {
                Log.d("vulcan-db", "getPackageFirstInstallTime: package not found: " + packageName);
                return null;
            }
            
            long firstInstallTime = pi.firstInstallTime;
        	String datetime = Long.toString(firstInstallTime);
        	return datetime;
        }
        
        /**different param about addAppShortcut*/
        private long addAppShortcut(SQLiteDatabase db, ContentValues values, HashMap<String, String> map,
                PackageManager packageManager, Intent intent) {
            long id = -1;
            ActivityInfo info;
            String packageName = map.get("packageName");
            String className = map.get("className");
            try {
                ComponentName cn;
                try {
                    cn = new ComponentName(packageName, className);
                    info = packageManager.getActivityInfo(cn, 0);
                } catch (PackageManager.NameNotFoundException nnfe) {
                    String[] packages = packageManager.currentToCanonicalPackageNames(
                        new String[] { packageName });
                    cn = new ComponentName(packages[0], className);
                    info = packageManager.getActivityInfo(cn, 0);
                }
                id = generateNewId();
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

                // Aurora <jialf> <2013-09-23> dd for loading data begin
                int flags = 0;
                try {
        			int appFlags = packageManager.getApplicationInfo(packageName, 0).flags;
        			if ((appFlags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
        				flags |= ApplicationInfo.DOWNLOADED_FLAG;

        				if ((appFlags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
        					flags |= ApplicationInfo.UPDATED_SYSTEM_APP_FLAG;
        				}
        			}
        		} catch (NameNotFoundException e) {
        			Log.d(TAG, "PackageManager.getApplicationInfo failed for " + packageName);
        		}
                values.put(Favorites.FLAGS, flags);
                
                
                //iconcat, vulcan added it in 2014-7-10
                String firstInstallTime = getPackageFirstInstallTime(mContext,packageName);
                if(firstInstallTime != null) {
                	values.put(Favorites.FIRST_INSTALL_TIME, firstInstallTime);
                	Log.d("vulcan-db",
                		String.format("addAppShortcut: set firstInstallTime to %s, pkg is %s",
                				firstInstallTime,packageName));
                }
  
                
				LauncherApplication.logVulcan.print("getShortcutInfo: intent = " + intent + ", flags = " + flags);
                // Aurora <jialf> <2013-09-23> add for loading data end
                
                values.put(Favorites.INTENT, intent.toUri(0));
                // Aurora <haojj> <2014-1-14> add for 快速检索数据保存title和拼音数据 begin
                // values.put(Favorites.TITLE, info.loadLabel(packageManager).toString());
                String title = info.loadLabel(packageManager).toString();
                String titleStr = title != null ? title.toString().replace(' ', ' ').trim() : null;
                String fullPinyin = null; 
                String simplePinyin = null;
                String[] pinyinArray = Utils.getFullPinYin(titleStr);
                if(pinyinArray != null) {
        	        fullPinyin = pinyinArray[0];
        	        simplePinyin = pinyinArray[1];
                }
                values.put(Favorites.TITLE, title);
                values.put(Favorites.FULL_PINYIN, fullPinyin);
                values.put(Favorites.SIMPLE_PINYIN, simplePinyin);
				// Aurora <haojj> <2014-1-14> end
                values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPLICATION);
                values.put(Favorites.SPANX, 1);
                values.put(Favorites.SPANY, 1);
                values.put(Favorites._ID, id);
                if (dbInsertAndCheck(this, db, TABLE_FAVORITES, null, values) < 0) {
                    return -1;
                }
                mFavotite.add(intent.toUri(0));
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "Unable to add favorite: " + packageName +
                        "/" + className, e);
            }
            return id;
        }
        
        
        
        
        
        
		private ResolveInfo filterAuroraDockApps(List<ResolveInfo> infos,
				PackageManager pm, String[] apps) {
			if (infos == null || infos.isEmpty()){
				Log.v("nn", "infos == null || infos.isEmpty()");
				return null;
			}
		
			ResolveInfo resolveInfo = null;
			outer:for(String temp : apps) {
				for (ResolveInfo info : infos) {
					/*String title = info.activityInfo.loadLabel(pm).toString();
					Log.v("nn", "temp= "+temp+",  title= "+title);
					if(title.equals(temp)) {*/
						resolveInfo = info;
						break outer;
					//}
				}
			}
			return resolveInfo;
		}
        
        private long addDockAppShortcut(SQLiteDatabase db, ContentValues values,/* TypedArray a,*/
                PackageManager packageManager, Intent intent, ResolveInfo resolveInfo) {
            long id = -1;
            ActivityInfo info;
			if (resolveInfo != null) {
	            String packageName = resolveInfo.activityInfo.applicationInfo.packageName;
	            String className = resolveInfo.activityInfo.name;
	            try {
	                ComponentName cn;
	                try {
	                    cn = new ComponentName(packageName, className);
	                    info = packageManager.getActivityInfo(cn, 0);
	                } catch (PackageManager.NameNotFoundException nnfe) {
	                    String[] packages = packageManager.currentToCanonicalPackageNames(
	                        new String[] { packageName });
	                    cn = new ComponentName(packages[0], className);
	                    info = packageManager.getActivityInfo(cn, 0);
	                }
	                id = generateNewId();
	                intent.setComponent(cn);
	                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
	                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
	
	                int flags = 0;
	                try {
	        			int appFlags = packageManager.getApplicationInfo(packageName, 0).flags;
	        			if ((appFlags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
	        				flags |= ApplicationInfo.DOWNLOADED_FLAG;
	
	        				if ((appFlags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
	        					flags |= ApplicationInfo.UPDATED_SYSTEM_APP_FLAG;
	        				}
	        			}
	        		} catch (NameNotFoundException e) {
	        			Log.d(TAG, "PackageManager.getApplicationInfo failed for " + packageName);
	        		}
	                values.put(Favorites.FLAGS, flags);
	                
	                
	                //iconcat, vulcan added it in 2014-7-10
	                String firstInstallTime = getPackageFirstInstallTime(mContext,packageName);
	                if(firstInstallTime != null) {
	                	values.put(Favorites.FIRST_INSTALL_TIME, firstInstallTime);
	        			Log.d("vulcan-db",
	        					String.format("addDockAppShortcut: set firstInstallTime to %s,pkg = %s",
	        							firstInstallTime,packageName));
	                }
	                
	                
	                values.put(Favorites.INTENT, intent.toUri(0));
	                // Aurora <haojj> <2014-1-14> add for 快速检索数据保存title和拼音数据 begin
	                //values.put(Favorites.TITLE, info.loadLabel(packageManager).toString());
	                String title = info.loadLabel(packageManager).toString();
	                String titleStr = title != null ? title.toString().replace(' ', ' ').trim() : null;
	                String fullPinyin = null; 
	                String simplePinyin = null;
	                String[] pinyinArray = Utils.getFullPinYin(titleStr);
	                if(pinyinArray != null) {
	        	        fullPinyin = pinyinArray[0];
	        	        simplePinyin = pinyinArray[1];
	                }
	                values.put(Favorites.TITLE, title);
	                values.put(Favorites.FULL_PINYIN, fullPinyin);
	                values.put(Favorites.SIMPLE_PINYIN, simplePinyin);
					// Aurora <haojj> <2014-1-14> end
	                
	                
	                values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPLICATION);
	                values.put(Favorites.SPANX, 1);
	                values.put(Favorites.SPANY, 1);
	                values.put(Favorites._ID, id);
	                if (dbInsertAndCheck(this, db, TABLE_FAVORITES, null, values) < 0) {
	                    return -1;
	                }
	                mFavotite.add(intent.toUri(0));
	            } catch (PackageManager.NameNotFoundException e) {
	                Log.w(TAG, "Unable to add favorite: " + packageName +
	                        "/" + className, e);
	            }
			}
            return id;
        }

        private long addFolder(SQLiteDatabase db, ContentValues values) {
            values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_FOLDER);
            values.put(Favorites.SPANX, 1);
            values.put(Favorites.SPANY, 1);
            
            
            //iconcat, vulcan added it in 2014-7-11
            values.put(Favorites.FIRST_INSTALL_TIME, "0");
			Log.d("vulcan-db",
					String.format("addFolder: set firstInstallTime to 0"));
            
            long id = generateNewId();
            values.put(Favorites._ID, id);
            if (dbInsertAndCheck(this, db, TABLE_FAVORITES, null, values) <= 0) {
                return -1;
            } else {
                return id;
            }
        }

        private ComponentName getSearchWidgetProvider() {
            SearchManager searchManager =
                    (SearchManager) mContext.getSystemService(Context.SEARCH_SERVICE);
            ComponentName searchComponent = searchManager.getGlobalSearchActivity();
            if (searchComponent == null) return null;
            return getProviderInPackage(searchComponent.getPackageName());
        }

        /**
         * Gets an appwidget provider from the given package. If the package contains more than
         * one appwidget provider, an arbitrary one is returned.
         */
        private ComponentName getProviderInPackage(String packageName) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
            List<AppWidgetProviderInfo> providers = appWidgetManager.getInstalledProviders();
            if (providers == null) return null;
            final int providerCount = providers.size();
            for (int i = 0; i < providerCount; i++) {
                ComponentName provider = providers.get(i).provider;
                if (provider != null && provider.getPackageName().equals(packageName)) {
                    return provider;
                }
            }
            return null;
        }

        private boolean addSearchWidget(SQLiteDatabase db, ContentValues values) {
            ComponentName cn = getSearchWidgetProvider();
            return addAppWidget(db, values, cn, 4, 1, null);
        }

        private boolean addClockWidget(SQLiteDatabase db, ContentValues values) {
            ComponentName cn = new ComponentName("com.android.alarmclock",
                    "com.android.alarmclock.AnalogAppWidgetProvider");
            return addAppWidget(db, values, cn, 2, 2, null);
        }

        private boolean addAppWidget(XmlResourceParser parser, AttributeSet attrs, int type,
                SQLiteDatabase db, ContentValues values, TypedArray a,
                PackageManager packageManager) throws XmlPullParserException, IOException {

            String packageName = a.getString(R.styleable.Favorite_packageName);
            String className = a.getString(R.styleable.Favorite_className);

            if (packageName == null || className == null) {
                return false;
            }

            boolean hasPackage = true;
            ComponentName cn = new ComponentName(packageName, className);
            try {
                packageManager.getReceiverInfo(cn, 0);
            } catch (Exception e) {
                String[] packages = packageManager.currentToCanonicalPackageNames(
                        new String[] { packageName });
                cn = new ComponentName(packages[0], className);
                try {
                    packageManager.getReceiverInfo(cn, 0);
                } catch (Exception e1) {
                    hasPackage = false;
                }
            }

            if (hasPackage) {
                int spanX = a.getInt(R.styleable.Favorite_spanX, 0);
                int spanY = a.getInt(R.styleable.Favorite_spanY, 0);
                
                // Aurora <jialf> <2013-09-09> add for loading data begin
                int screen = Integer.parseInt(a.getString(R.styleable.Favorite_screen));
                int x = Integer.parseInt(a.getString(R.styleable.Favorite_x));
                int y = Integer.parseInt(a.getString(R.styleable.Favorite_y));
                if (screen == mMaxScreen) {
                    if ((x + spanX - 1) >= mMaxCellX
                    		|| (y + spanY - 1) > mMaxCellY) {
                    	mMaxCellX = x + spanX - 1;
                    	mMaxCellY = y + spanY - 1;
                    }
                } else if (screen > mMaxScreen) {
                    mMaxScreen = screen;
                    mMaxCellX = x + spanX - 1;
                    mMaxCellY = y + spanY - 1;
                }
                // Aurora <jialf> <2013-09-09> add for loading data end

                // Read the extras
                Bundle extras = new Bundle();
                int widgetDepth = parser.getDepth();
                while ((type = parser.next()) != XmlPullParser.END_TAG ||
                        parser.getDepth() > widgetDepth) {
                    if (type != XmlPullParser.START_TAG) {
                        continue;
                    }

                    TypedArray ar = mContext.obtainStyledAttributes(attrs, R.styleable.Extra);
                    if (TAG_EXTRA.equals(parser.getName())) {
                        String key = ar.getString(R.styleable.Extra_key);
                        String value = ar.getString(R.styleable.Extra_value);
                        if (key != null && value != null) {
                            extras.putString(key, value);
                        } else {
                            throw new RuntimeException("Widget extras must have a key and value");
                        }
                    } else {
                        throw new RuntimeException("Widgets can contain only extras");
                    }
                    ar.recycle();
                }

                return addAppWidget(db, values, cn, spanX, spanY, extras);
            }

            return false;
        }
        
        
        
		/** add app widget and refer from old version **/
		private boolean addAppWidget(XmlPullParser parser, SQLiteDatabase db,int type,
				HashMap<String, String> map, HashMap<String, String> mExtraMap,ContentValues values,
				PackageManager packageManager) throws XmlPullParserException,
				IOException {
			String packageName = map.get("packageName");
			String className = map.get("className");
			if (packageName == null || className == null) {
				return false;
			}
			boolean hasPackage = true;
			ComponentName cn = new ComponentName(packageName, className);
			try {
				packageManager.getReceiverInfo(cn, 0);
			} catch (Exception e) {
				String[] packages = packageManager
						.currentToCanonicalPackageNames(new String[] { packageName });
				cn = new ComponentName(packages[0], className);
				try {
					packageManager.getReceiverInfo(cn, 0);
				} catch (Exception e1) {
					hasPackage = false;
				}
			}
			if (hasPackage) {
                int spanX = Integer.parseInt(map.get("spanX"));
                int spanY = Integer.parseInt(map.get("spanY"));
               
                // Aurora <jialf> <2013-09-09> add for loading data begin
                int screen = Integer.parseInt(map.get("screen"));
                int x = Integer.parseInt(map.get("x"));
                int y = Integer.parseInt(map.get("y"));
                if (screen == mMaxScreen) {
                    if ((x + spanX - 1) >= mMaxCellX
                    		|| (y + spanY - 1) > mMaxCellY) {
                    	mMaxCellX = x + spanX - 1;
                    	mMaxCellY = y + spanY - 1;
                    }
                } else if (screen > mMaxScreen) {
                    mMaxScreen = screen;
                    mMaxCellX = x + spanX - 1;
                    mMaxCellY = y + spanY - 1;
                }
                // Aurora <jialf> <2013-09-09> add for loading data end
                // Read the extras
                Bundle extras = new Bundle();
                //create new HashMap
                mExtraMap.clear();
                int widgetDepth = parser.getDepth();
                while ((type = parser.next()) != XmlPullParser.END_TAG ||
                        parser.getDepth() > widgetDepth) {
                    if (type != XmlPullParser.START_TAG) {
                        continue;
                    }
                    //iterates child again
                    int count = parser.getAttributeCount();

					for (int j = 0; j < count; j++) {
						mExtraMap.put(parser.getAttributeName(j),
								parser.getAttributeValue(j));
					}
							
					if (TAG_EXTRA.equals(parser.getName())) {
						 String key = mExtraMap.get("key").toString().trim();
						 String val  = mExtraMap.get("value").toString().trim();
						 if (key != null && val != null) {
	                            extras.putString(key, val);
	                        } else {
	                            throw new RuntimeException("Widget extras must have a key and value");
	                        }
					 }else {
	                        throw new RuntimeException("Widgets can contain only extras");
	                 }
                    
                }
				   return addAppWidget(db, values, cn, spanX, spanY, extras);
			}
           return false;
		}
        
        
        

        private boolean addAppWidget(SQLiteDatabase db, ContentValues values, ComponentName cn,
                int spanX, int spanY, Bundle extras) {
            boolean allocatedAppWidgets = false;
            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);

            try {
                int appWidgetId = mAppWidgetHost.allocateAppWidgetId();

                values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPWIDGET);
                values.put(Favorites.SPANX, spanX);
                values.put(Favorites.SPANY, spanY);
                values.put(Favorites.APPWIDGET_ID, appWidgetId);
                values.put(Favorites._ID, generateNewId());
                
                //iconcat, vulcan added it in 2014-7-11
				if (cn != null) {
					String firstInstallTime = getPackageFirstInstallTime(
							mContext, cn.getPackageName());
					if (firstInstallTime != null) {
						values.put(Favorites.FIRST_INSTALL_TIME,
								firstInstallTime);
						Log.d("vulcan-db",
								String.format(
										"addAppWidget: set firstInstallTime to %s, pkg is %s",
										firstInstallTime, cn.getPackageName()));
					}
				}
                
                dbInsertAndCheck(this, db, TABLE_FAVORITES, null, values);

                allocatedAppWidgets = true;

                // TODO: need to check return value
                appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, cn);

                // Send a broadcast to configure the widget
                if (extras != null && !extras.isEmpty()) {
                    Intent intent = new Intent(ACTION_APPWIDGET_DEFAULT_WORKSPACE_CONFIGURE);
                    intent.setComponent(cn);
                    intent.putExtras(extras);
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                    mContext.sendBroadcast(intent);
                }
            } catch (RuntimeException ex) {
                Log.e(TAG, "Problem allocating appWidgetId", ex);
            }

            return allocatedAppWidgets;
        }

        private long addUriShortcut(SQLiteDatabase db, ContentValues values,
                TypedArray a) {
            Resources r = mContext.getResources();

            final int iconResId = a.getResourceId(R.styleable.Favorite_icon, 0);
            final int titleResId = a.getResourceId(R.styleable.Favorite_title, 0);
            Intent intent;
            String uri = null;
            try {
                uri = a.getString(R.styleable.Favorite_uri);
                intent = Intent.parseUri(uri, 0);
            } catch (URISyntaxException e) {
                Log.w(TAG, "Shortcut has malformed uri: " + uri);
                return -1; // Oh well
            }

            if (iconResId == 0 || titleResId == 0) {
                Log.w(TAG, "Shortcut is missing title or icon resource ID");
                return -1;
            }

            long id = generateNewId();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            values.put(Favorites.INTENT, intent.toUri(0));
            values.put(Favorites.TITLE, r.getString(titleResId));
            values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_SHORTCUT);
            values.put(Favorites.SPANX, 1);
            values.put(Favorites.SPANY, 1);
            values.put(Favorites.ICON_TYPE, Favorites.ICON_TYPE_RESOURCE);
            values.put(Favorites.ICON_PACKAGE, mContext.getPackageName());
            values.put(Favorites.ICON_RESOURCE, r.getResourceName(iconResId));
            values.put(Favorites._ID, id);
            
			
            //iconcat, vulcan added it in 2014-7-11
            String firstInstallTime = getPackageFirstInstallTime(mContext,intent.getPackage());
            if(firstInstallTime != null) {
            	values.put(Favorites.FIRST_INSTALL_TIME, firstInstallTime);
				Log.d("vulcan-db",
						String.format(
								"addUriShortcut: set firstInstallTime to %s, pkg is %s",
								firstInstallTime, intent.getPackage()));
            }

            if (dbInsertAndCheck(this, db, TABLE_FAVORITES, null, values) < 0) {
                return -1;
            }
            return id;
        }
        
        /**Hazel start to define different param about addUriShortcut function*/
		private long addUriShortcut(SQLiteDatabase db, ContentValues values,
				HashMap<String, String> map) {
			Intent intent;
			String uri = null;
			Resources r = mContext.getResources();
			int iconResId = 0;
			int titleResId = 0;
			String iconR = map.get("icon"); // something like:@drawable/xxx
			// toggle getting iconResId String
			if (iconR != null) {
				String[] tmp = iconR.split("/");
				if (tmp.length == 2) {
					String iconName = tmp[1];
					String packageName = mContext.getPackageName();
					iconResId = r.getIdentifier(iconName, "drawable",
							packageName);
				}
			}

			String title = map.get("title");
			if (title != null) {
				titleResId = getTitleResID(title);
			}

			try {
				uri = map.get("uri");
				intent = Intent.parseUri(uri, 0);
			} catch (URISyntaxException e) {
				Log.w(TAG, "Shortcut has malformed uri: " + uri);
				return -1; // Oh well
			}

			if (iconResId == 0 || titleResId <= 0) {
				Log.w(TAG, "Shortcut is missing title or icon resource ID");
				return -1;
			}

			long id = generateNewId();
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			values.put(Favorites.INTENT, intent.toUri(0));
			values.put(Favorites.TITLE, r.getString(titleResId));
			values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_SHORTCUT);
			values.put(Favorites.SPANX, 1);
			values.put(Favorites.SPANY, 1);
			values.put(Favorites.ICON_TYPE, Favorites.ICON_TYPE_RESOURCE);
			values.put(Favorites.ICON_PACKAGE, mContext.getPackageName());
			values.put(Favorites.ICON_RESOURCE, r.getResourceName(iconResId));
			values.put(Favorites._ID, id);

			if (dbInsertAndCheck(this, db, TABLE_FAVORITES, null, values) < 0) {
				return -1;
			}
			return id;
		}
        
		private int getTitleResID(String str){
			Resources r = mContext.getResources();
			String[] tmp = str.split("/");
			if (tmp.length == 2) {
				String titleName = tmp[1];
				String packageName = mContext.getPackageName();
				int titleResId = r.getIdentifier(titleName, "string",
						packageName);
				return titleResId;
			}
			return -1;
		}

		@Override
		public void onDowngrade(SQLiteDatabase db, int oldVersion,
				int newVersion) {
			//iconcat, vulcan added it in 2014-7-11
			//Since down grade is only used to debugging,
			//we simply destroy the old database.
            if (oldVersion != DATABASE_VERSION) {
				//Log.w(TAG, "Destroying all old data.");
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES_BACKUP);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_APP_CATEGORY);
				onCreate(db);
            }
			// TODO Auto-generated method stub
			//super.onDowngrade(db, oldVersion, newVersion);
		}
		/**insert app classification data*/
		public void insertDefaultAppTag(SQLiteDatabase db){
			ArrayList<String> result = null;
			if(Utilities.getCurrentLanuage(mContext).equals("zh")){
				result = Utilities.readfileFromAssert(mContext, "launcher_default_app_tags.txt");
			}else
				result = Utilities.readfileFromAssert(mContext, "launcher_default_app_tags_en.txt");
			 
			for(String sql: result){
				db.execSQL(sql);
			}
			
		}
		
		
		public void createDefaultApptagsTable(SQLiteDatabase db){
            db.execSQL("CREATE TABLE IF NOT EXISTS app_info_category ("
					+ "id integer PRIMARY KEY NOT NULL,"
					+ "title text NOT NULL," 
					+ "package_name text NOT NULL,"
					+ "app_type text NOT NULL,"
					+ "category_name text  NOT NULL,"
					+ "UNIQUE(package_name,category_name)" + ");");
            
            db.execSQL("CREATE TABLE IF NOT EXISTS favorites_backup (" +
                    "_id INTEGER PRIMARY KEY," +
                    "title TEXT," +
                    "simplePinyin TEXT," +
                    "fullPinyin TEXT," +
                    "simle_t9 TEXT," +
                    "full_t9 TEXT," +
                    "intent TEXT," +
                    "container INTEGER," +
                    "screen INTEGER," +
                    "cellX INTEGER," +
                    "cellY INTEGER," +
                    "spanX INTEGER," +
                    "spanY INTEGER," +
                    "itemType INTEGER," +
                    "appWidgetId INTEGER NOT NULL DEFAULT -1," +
                    "isShortcut INTEGER," +
                    "iconType INTEGER," +
                    "iconPackage TEXT," +
                    "iconResource TEXT," +
                    "icon BLOB," +
                    "uri TEXT," +
                    "displayMode INTEGER," +
                    "flags INTEGER DEFAULT 0," +
                    "firstInstallTime INTEGER NOT NULL DEFAULT 0," + /*iconcat, vulcan modified it in 2014-7-10*/
                    "enableWidgets INTEGER NOT NULL DEFAULT 0" +
            		");");
            
		}
    }

    /**
     * Build a query string that will match any row where the column matches
     * anything in the values list.
     */
    static String buildOrWhereString(String column, int[] values) {
        StringBuilder selectWhere = new StringBuilder();
        for (int i = values.length - 1; i >= 0; i--) {
            selectWhere.append(column).append("=").append(values[i]);
            if (i > 0) {
                selectWhere.append(" OR ");
            }
        }
        return selectWhere.toString();
    }

    static class SqlArguments {
        public final String table;
        public final String where;
        public final String[] args;

        SqlArguments(Uri url, String where, String[] args) {
            if (url.getPathSegments().size() == 1) {
                this.table = url.getPathSegments().get(0);
                this.where = where;
                this.args = args;
            } else if (url.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + url);
            } else if (!TextUtils.isEmpty(where)) {
                throw new UnsupportedOperationException("WHERE clause not supported: " + url);
            } else {
                this.table = url.getPathSegments().get(0);
                this.where = "_id=" + ContentUris.parseId(url);
                this.args = null;
            }
        }

        SqlArguments(Uri url) {
            if (url.getPathSegments().size() == 1) {
                table = url.getPathSegments().get(0);
                where = null;
                args = null;
            } else {
                throw new IllegalArgumentException("Invalid URI: " + url);
            }
        }
    }
    
    // Aurora <jialf> <2013-09-09> add for loading data begin
    static final String FIRST_QUERY_ALL_APPS = "FIRST_QUERY_ALL_APPS";
    static final String DEFAULT_LANGUAGE = "DEFAULT_LANGUAGE";
    static int mMaxScreen = -1;
    static int mMaxCellX = -1;
    static int mMaxCellY = -1;
    
    static ArrayList<String> mFavotite = new ArrayList<String>();
    // Aurora <jialf> <2013-09-09> add for loading data end
    
	// Aurora <jialf> <2013-11-14> modify for only one item in folder begin
	private static void updateContainer(SQLiteDatabase db, long id,
			long container, String screen, String x, String y) {
        Uri uri = LauncherSettings.Favorites.getContentUri(id, false);
        SqlArguments args = new SqlArguments(uri, null, null);
        Log.i(TAG,"uri = " + uri +", args.table = " + args.table);
        ContentValues values = new ContentValues();
        values.put(LauncherSettings.Favorites.CONTAINER, container);
        values.put(LauncherSettings.Favorites.SCREEN, screen);
        values.put(LauncherSettings.Favorites.CELLX, x);
        values.put(LauncherSettings.Favorites.CELLY, y);
		db.update(args.table, values, "_id=" + id, null);
    }
	// Aurora <jialf> <2013-11-14> modify for only one item in folder end
	
	/**bulk update column about title,simplepinyin,Fullpinyin*/
	public int bulkUpdate(Uri[] uri, ContentValues[] values) {
		
		int count = 0;
		if (null != uri && null != values) {
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			db.beginTransaction();
			try {
				int numValues = values.length;
				for (int i = 0; i < numValues; i++) {
				    SqlArguments args = new SqlArguments(uri[i], null, null);
					if(null!=values[i] && !values[i].toString().equals("")){
						count = db.update(args.table, values[i], args.where,
								args.args);
					}
				}
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}
		return count;
	}
	
	
	/**batch insert favorite_backup tables*/
	public void bulkBackupTable(String fromTBName,String destTBName){
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			db.execSQL("DELETE FROM "+destTBName); //clean up target table name
//			db.execSQL("INSERT INTO "
//					+ destTBName
//					+ "(_id,title,simplePinyin,fullPinyin,simle_t9,full_t9,firstInstallTime,intent,container,screen,cellX,cellY,spanX,spanY,itemType,appWidgetId,isShortcut,iconType,iconPackage,iconResource,icon,uri,displayMode,flags )select _id,title,simplePinyin,fullPinyin,simle_t9,full_t9,firstInstallTime,intent,container,screen,cellX,cellY,spanX,spanY,itemType,appWidgetId,isShortcut,iconType,iconPackage,iconResource,icon,uri,displayMode,flags from"
//					+ "\t" + fromTBName);
			db.execSQL("INSERT INTO " +destTBName+"\t"+"select * from "+fromTBName); //clean up target table name
			db.setTransactionSuccessful();
		} finally  {
			db.endTransaction();
		}
	}
	
	public void bulkUpdateCategoryTable(){
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			db.execSQL("DELETE FROM "+TABLE_APP_CATEGORY); //clean up target table name
			getDatabaseHelper().insertDefaultAppTag(db);
			db.setTransactionSuccessful();
		} finally  {
			db.endTransaction();
		}
	}

}
