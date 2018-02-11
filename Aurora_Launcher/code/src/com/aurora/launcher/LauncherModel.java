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
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.ClipData.Item;
import android.content.Intent.ShortcutIconResource;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.aurora.launcher.Launcher.EditMode;
import com.aurora.launcher.LauncherProvider.DatabaseHelper;
import com.aurora.launcher.LauncherProvider.SqlArguments;
import com.aurora.launcher.R;
import com.aurora.launcher.InstallWidgetReceiver.WidgetMimeTypeHandlerData;
import com.aurora.launcher.Workspace.ItemPos;
import com.aurora.util.ClassifyApp;
import com.aurora.util.ClassifyApp.clsfolderInfo;
import com.aurora.util.DeviceProperties;
import com.aurora.util.Utils;


import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.aurora.utils.*;


/**
 * Maintains in-memory state of the Launcher. It is expected that there should be only one
 * LauncherModel object held in a static. Also provide APIs for updating the database state
 * for the Launcher.
 */
public class LauncherModel extends BroadcastReceiver {
    static final boolean DEBUG_LOADERS = false;
    static final String TAG = "Launcher.Model";
    
    public static final String NOTIFY_APPS_TO_UPDATEIICON = "com.aurora.action.pulbicres.update";
    private static final String NOTIFY_APPS_TO_UPDATEIICON_KEY = "updates";
    private static final int NOTIFY_APPS_TO_UPDATEIICON_SIZE = 200;

    private static final int INSTALL_SHORTCUT_SUCCESSFUL = 0; //已经成功安装的应用
    
    private static final int ITEMS_CHUNK = 6; // batch size for the workspace icons
    private final boolean mAppsCanBeOnExternalStorage;
    private int mBatchSize; // 0 is all apps at once
    private int mAllAppsLoadDelay; // milliseconds between batches
    private int mAppsCount;
    private boolean showWorkspaceCling = true;
    private final LauncherApplication mApp;
    private final Object mLock = new Object();
    private DeferredHandler mHandler = new DeferredHandler();
    private LoaderTask mLoaderTask;
    private boolean mIsLoaderTaskRunning;

    // Specific runnable types that are run on the main thread deferred handler, this allows us to
    // clear all queued binding runnables when the Launcher activity is destroyed.
    private static final int MAIN_THREAD_NORMAL_RUNNABLE = 0;
    private static final int MAIN_THREAD_BINDING_RUNNABLE = 1;


    private static final HandlerThread sWorkerThread = new HandlerThread("launcher-loader");
    static {
        sWorkerThread.start();
    }
    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());

    // We start off with everything not loaded.  After that, we assume that
    // our monitoring of the package manager provides all updates and we never
    // need to do a requery.  These are only ever touched from the loader thread.
    private boolean mWorkspaceLoaded;
    private boolean mAllAppsLoaded;

    // When we are loading pages synchronously, we can't just post the binding of items on the side
    // pages as this delays the rotation process.  Instead, we wait for a callback from the first
    // draw (in Workspace) to initiate the binding of the remaining side pages.  Any time we start
    // a normal load, we also clear this set of Runnables.
    static final ArrayList<Runnable> mDeferredBindRunnables = new ArrayList<Runnable>();

    private WeakReference<Callbacks> mCallbacks;

    // < only access in worker thread >
    private AllAppsList mBgAllAppsList;

    // The lock that must be acquired before referencing any static bg data structures.  Unlike
    // other locks, this one can generally be held long-term because we never expect any of these
    // static data structures to be referenced outside of the worker thread except on the first
    // load after configuration change.
    static final Object sBgLock = new Object();

    // sBgItemsIdMap maps *all* the ItemInfos (shortcuts, folders, and widgets) created by
    // LauncherModel to their ids
    
    /**
     * @author ht 2014-11-25
     * 注意线程安全，尤其是在批量操作桌面Icon时
     * */
    //static final HashMap<Long, ItemInfo> sBgItemsIdMap = new HashMap<Long, ItemInfo>();
    static final ConcurrentHashMap<Long, ItemInfo> sBgItemsIdMap = new ConcurrentHashMap<Long, ItemInfo>();

//static final ConcurrentHashMap<Long, ItemInfo> sBgItemsNeededUpdateMap = new ConcurrentHashMap<Long, ItemInfo>();
    static ArrayList<ShortcutInfo> sBgItemList = new ArrayList<ShortcutInfo>();
    //static ArrayList<ComponentName> sWidgetItemList = new ArrayList<ComponentName>();
    static HashMap<String, String> sWidgetItemMap = new HashMap<String, String>();

    // sBgWorkspaceItems is passed to bindItems, which expects a list of all folders and shortcuts
    //       created by LauncherModel that are directly on the home screen (however, no widgets or
    //       shortcuts within folders).
    static final ArrayList<ItemInfo> sBgWorkspaceItems = new ArrayList<ItemInfo>();

    // sBgAppWidgets is all LauncherAppWidgetInfo created by LauncherModel. Passed to bindAppWidget()
    static final ArrayList<LauncherAppWidgetInfo> sBgAppWidgets =
        new ArrayList<LauncherAppWidgetInfo>();

    // sBgFolders is all FolderInfos created by LauncherModel. Passed to bindFolders()
    static final HashMap<Long, FolderInfo> sBgFolders = new HashMap<Long, FolderInfo>();

    // sBgDbIconCache is the set of ItemInfos that need to have their icons updated in the database
    static final HashMap<Object, byte[]> sBgDbIconCache = new HashMap<Object, byte[]>();
    // </ only access in worker thread >

    private IconCache mIconCache;
    private Bitmap mDefaultIcon;

    private static int mCellCountX;
    private static int mCellCountY;

    protected int mPreviousConfigMcc;
    
    
    final AutoPageMananger apm = new AutoPageMananger();
    
   
    private ClassifyApp clsApp = new ClassifyApp();
    /**array list  for classify*/
    private ArrayList<ShortcutInfo> AppList = new ArrayList<ShortcutInfo>();
    private ArrayList<ShortcutInfo> GameList = new ArrayList<ShortcutInfo>();
    private ArrayList<ShortcutInfo>	OtherList = new ArrayList<ShortcutInfo>();
    private List<Map.Entry<String, List<ShortcutInfo>>> folderList;
    
    private static  boolean reLoadWorkspaceforClassify = false;
    private Utils2Icon mUtils2Icon =null;
    
    private static HashMap<String,ArrayList<ProviderAndDimen>> sortByPkg= new HashMap<String,ArrayList<ProviderAndDimen>>();
    
    AppWidgetHost mAppWidgetHost;
	AppWidgetManager appWidgetManager;
	//ArrayList<ShortcutInfo> mWithoutWidgets = new ArrayList<ShortcutInfo>();
	
	static final ArrayList<ShortcutInfo> getAllShortcutsAndApps(){
		ArrayList<ShortcutInfo> allShortcutsAndApps = new ArrayList<ShortcutInfo>();
		for( Map.Entry entry : sBgItemsIdMap.entrySet()){
			ItemInfo item = (ItemInfo) entry.getValue();
			if(item instanceof ShortcutInfo){
				allShortcutsAndApps.add((ShortcutInfo)item);
			}
		}
		return allShortcutsAndApps;
	}
	
	public static final String FIRST_OPEN_ALL_MOVEUP_WIDGETS = "FIRST_OPEN_ALL_MOVEUP_WIDGETS";
	private boolean mOpenAllMoveupWidgets = false;
    
    public interface Callbacks {
        public boolean setLoadOnResume();
        public int getCurrentWorkspaceScreen();
        public void startBinding(HashMap<Long, ItemInfo> itemMap,HashMap<Long, FolderInfo> folderMap);
        //public void bindItems(final ArrayList<ItemInfo> shortcuts, final int start, final int end);
        public void bindItems(final ArrayList<?> shortcuts, final int start, final int end);
        public void bindFolders(HashMap<Long,FolderInfo> folders);
        public void finishBindingItems();
        public void bindAppWidget(LauncherAppWidgetInfo info);
        public void bindAllApplications(ArrayList<ApplicationInfo> apps);
        public void bindAppsAdded(ArrayList<ApplicationInfo> apps);
        public void bindAppsUpdated(ArrayList<ApplicationInfo> apps);
        public void bindAppsRemoved(HashSet<Intent> appsToRmv, HashSet<String> widgetsToRmv, boolean permanent);
        public void bindPackagesUpdated();
        public boolean isAllAppsVisible();
        public boolean isAllAppsButtonRank(int rank);
        public void bindSearchablesChanged();
        public void onPageBoundSynchronously(int page);
        
        // Aurora <jialf> <2013-09-19> add for install/unstall apps begin
        public void bindAuroraAppsAdded(ShortcutInfo info);
        public void bindAuroraAppsUpdated(ShortcutInfo info);
        public void autoManangePages();
        // Aurora <jialf> <2013-09-19> add for install/unstall apps end
        
        public void dampedOverScroll();
        
        
        public void writePagesInDBCompleted();
        
        public HashMap<String, ArrayList<ProviderAndDimen>> getPkgProviderMap();
    }
    


    LauncherModel(LauncherApplication app, IconCache iconCache) {
        mAppsCanBeOnExternalStorage = !Environment.isExternalStorageEmulated();
        mApp = app;
        mBgAllAppsList = new AllAppsList(iconCache);
        mIconCache = iconCache;

        mDefaultIcon = Utilities.createIconBitmap(
                mIconCache.getFullResDefaultActivityIcon(), app);

        final Resources res = app.getResources();
        mAllAppsLoadDelay = res.getInteger(R.integer.config_allAppsBatchLoadDelay);
        mBatchSize = res.getInteger(R.integer.config_allAppsBatchSize);
        Configuration config = res.getConfiguration();
        mPreviousConfigMcc = config.mcc;
        mUtils2Icon = Utils2Icon.getInstance(mApp.getApplicationContext());
    }
    
    /**
     * vulcan created this method in 2014-8-18
     * send a runnable to main thread to finish running activity
     */
    private void finishInMainThread() {
    	runOnMainThread(new Runnable() {
    		public void run() {
    			Callbacks cb = LauncherModel.this.getCallback();
    			if(!(cb instanceof Launcher)) {
    				return;
    			}
    			Launcher l = (Launcher)cb;
    			l.finish();
    			Log.d("vulcan-finish","finishInMainThread");
    			return;
    		}
    	});
    }

    /** Runs the specified runnable immediately if called from the main thread, otherwise it is
     * posted on the main thread handler. */
    private void runOnMainThread(Runnable r) {
        runOnMainThread(r, 0);
    }
    private void runOnMainThread(Runnable r, int type) {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            // If we are on the worker thread, post onto the main handler
            mHandler.post(r);
        } else {
            r.run();
        }
    }

    /** Runs the specified runnable immediately if called from the worker thread, otherwise it is
     * posted on the worker thread handler. */
    private static void runOnWorkerThread(Runnable r) {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            r.run();
        } else {
            // If we are not on the worker thread, then post to the worker handler
            sWorker.post(r);
        }
    }

    public Bitmap getFallbackIcon() {
        return Bitmap.createBitmap(mDefaultIcon);
    }

    public void unbindItemInfosAndClearQueuedBindRunnables() {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            throw new RuntimeException("Expected unbindLauncherItemInfos() to be called from the " +
                    "main thread");
        }

        // Clear any deferred bind runnables
        mDeferredBindRunnables.clear();
        // Remove any queued bind runnables
        mHandler.cancelAllRunnablesOfType(MAIN_THREAD_BINDING_RUNNABLE);
        // Unbind all the workspace items
        unbindWorkspaceItemsOnMainThread();
    }

    /** Unbinds all the sBgWorkspaceItems and sBgAppWidgets on the main thread */
    void unbindWorkspaceItemsOnMainThread() {
        // Ensure that we don't use the same workspace items data structure on the main thread
        // by making a copy of workspace items first.
        final ArrayList<ItemInfo> tmpWorkspaceItems = new ArrayList<ItemInfo>();
        final ArrayList<ItemInfo> tmpAppWidgets = new ArrayList<ItemInfo>();
        synchronized (sBgLock) {
            tmpWorkspaceItems.addAll(sBgWorkspaceItems);
            tmpAppWidgets.addAll(sBgAppWidgets);
        }
        Runnable r = new Runnable() {
                @Override
                public void run() {
                   for (ItemInfo item : tmpWorkspaceItems) {
                       item.unbind();
                   }
                   for (ItemInfo item : tmpAppWidgets) {
                       item.unbind();
                   }
                }
            };
        runOnMainThread(r);
    }

    /**
     * Adds an item to the DB if it was not created previously, or move it to a new
     * <container, screen, cellX, cellY>
     */
    static void addOrMoveItemInDatabase(Context context, ItemInfo item, long container,
            int screen, int cellX, int cellY) {
		
        if (item.container == ItemInfo.NO_ID) {
            // From all apps
            addItemToDatabase(context, item, container, screen, cellX, cellY, false);
        } else {
            // From somewhere else
            moveItemInDatabase(context, item, container, screen, cellX, cellY);
        }
    }

    static void checkItemInfoLocked(
            final long itemId, final ItemInfo item, StackTraceElement[] stackTrace) {
        ItemInfo modelItem = sBgItemsIdMap.get(itemId);
        if (modelItem != null && item != modelItem) {
            // check all the data is consistent
            if (modelItem instanceof ShortcutInfo && item instanceof ShortcutInfo) {
                ShortcutInfo modelShortcut = (ShortcutInfo) modelItem;
                ShortcutInfo shortcut = (ShortcutInfo) item;
                if (modelShortcut.title.toString().equals(shortcut.title.toString()) &&
                        modelShortcut.intent.filterEquals(shortcut.intent) &&
                        modelShortcut.id == shortcut.id &&
                        modelShortcut.itemType == shortcut.itemType &&
                        modelShortcut.container == shortcut.container &&
                        modelShortcut.screen == shortcut.screen &&
                        modelShortcut.cellX == shortcut.cellX &&
                        modelShortcut.cellY == shortcut.cellY &&
                        modelShortcut.spanX == shortcut.spanX &&
                        modelShortcut.spanY == shortcut.spanY &&
                        ((modelShortcut.dropPos == null && shortcut.dropPos == null) ||
                        (modelShortcut.dropPos != null &&
                                shortcut.dropPos != null &&
                                modelShortcut.dropPos[0] == shortcut.dropPos[0] &&
                        modelShortcut.dropPos[1] == shortcut.dropPos[1]))) {
                    // For all intents and purposes, this is the same object
                    return;
                }
            }

            // the modelItem needs to match up perfectly with item if our model is
            // to be consistent with the database-- for now, just require
            // modelItem == item or the equality check above
            String msg = "item: " + ((item != null) ? item.toString() : "null") +
                    "modelItem: " +
                    ((modelItem != null) ? modelItem.toString() : "null") +
                    "Error: ItemInfo passed to checkItemInfo doesn't match original";
            RuntimeException e = new RuntimeException(msg);
            if (stackTrace != null) {
                e.setStackTrace(stackTrace);
            }
            throw e;
        }
    }

    static void checkItemInfo(final ItemInfo item) {
        final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        final long itemId = item.id;
        Runnable r = new Runnable() {
            public void run() {
                synchronized (sBgLock) {
                    checkItemInfoLocked(itemId, item, stackTrace);
                }
            }
        };
        runOnWorkerThread(r);
    }

    static void updateItemInDatabaseHelper(Context context, final ContentValues values,
            final ItemInfo item, final String callingFunction) {
    	final String senderStack = LogWriter.StackToString(new Throwable());
    	
    	
        final long itemId = item.id;
        final Uri uri = LauncherSettings.Favorites.getContentUri(itemId, false);
        final ContentResolver cr = context.getContentResolver();

        final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        
        Runnable r = new Runnable() {
            public void run() {
            	
            	if(item.toString().contains("sourceBounds=")) {
            		LauncherApplication.logVulcan.print("updateItemInDatabaseHelper: find out sourceBounds\n" + senderStack);
            	}
            	
                final Cursor myResult = cr.query(LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION,
                		new String[]{"title"}, "favorites.container=? AND favorites.screen=? AND favorites.cellX=? AND favorites.cellY=? AND favorites.title<>?",
                		new String[]{String.valueOf(item.container), String.valueOf(item.screen), String.valueOf(item.cellX), String.valueOf(item.cellY), String.valueOf(item.title)},
                		null);
                if(myResult.getCount() > 0) {
                	LauncherApplication.logVulcan.print("updateItemInDatabaseHelper: myResult.getCount() = " + myResult.getCount());
           
                	if(item.title != null) {
                		myResult.moveToFirst();
                		String newString = item.title.toString().trim();
                		String dbString = myResult.getString(0).trim();
                		LauncherApplication.logVulcan.print("updateItemInDatabaseHelper: newString ="
                    			+ newString.getBytes() + ", dbString ="   + dbString.getBytes());
                		if(newString.lastIndexOf(dbString) == -1 && dbString.lastIndexOf(newString) == -1) {
                        	LauncherApplication.logVulcan.print("updateItemInDatabaseHelper: repeat data,title = "
                        			+ newString + ", container = " + item.container + " ,screen = " + item.screen + ", cellX = " + item.cellX + ", cellY = " + item.cellY);
                        	LauncherApplication.logVulcan.print("updateItemInDatabaseHelper: repeat data(db),title = "
                        			+ dbString);
                        	LauncherApplication.logVulcan.print("updateItemInDatabaseHelper: find out repeat data \n" + senderStack);
                		}

                	}
                }
                myResult.close();
            	
                cr.update(uri, values, null, null);
                LauncherApplication.logVulcan.print("140728-p1-updateItemInDatabaseHelper: already completed writing, now is " + System.currentTimeMillis()%60000);

                // Lock on mBgLock *after* the db operation
                synchronized (sBgLock) {
                    checkItemInfoLocked(itemId, item, stackTrace);

                    if (item.container != LauncherSettings.Favorites.CONTAINER_DESKTOP &&
                            item.container != LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                        // Item is in a folder, make sure this folder exists
                        if (!sBgFolders.containsKey(item.container)) {
                            // An items container is being set to a that of an item which is not in
                            // the list of Folders.
                            String msg = "item: " + item + " container being set to: " +
                                    item.container + ", not in the list of folders";
                            Log.e(TAG, msg);
                            Launcher.dumpDebugLogsToConsole();
                        }
                    }

                    // Items are added/removed from the corresponding FolderInfo elsewhere, such
                    // as in Workspace.onDrop. Here, we just add/remove them from the list of items
                    // that are on the desktop, as appropriate
                    ItemInfo modelItem = sBgItemsIdMap.get(itemId);
					if (modelItem != null && (modelItem.container == LauncherSettings.Favorites.CONTAINER_DESKTOP 
							|| modelItem.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT)) {
                        switch (modelItem.itemType) {
                            case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                            case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                            case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                                if (!sBgWorkspaceItems.contains(modelItem)) {
                                    sBgWorkspaceItems.add(modelItem);
                                }
                                break;
                            default:
                                break;
                        }
                    } else {
                        sBgWorkspaceItems.remove(modelItem);
                    }
                }
            }
        };
        runOnWorkerThread(r);
    }

    /**
     * Move an item in the DB to a new <container, screen, cellX, cellY>
     */
    static void moveItemInDatabase(Context context, final ItemInfo item, final long container,
            final int screen, final int cellX, final int cellY) {
        String transaction = "DbDebug    Modify item (" + item.title + ") in db, id: " + item.id +
                " (" + item.container + ", " + item.screen + ", " + item.cellX + ", " + item.cellY +
                ") --> " + "(" + container + ", " + screen + ", " + cellX + ", " + cellY + ")";
        Launcher.sDumpLogs.add(transaction);
        Log.d(TAG, transaction);
        item.container = container;
        item.cellX = cellX;
        item.cellY = cellY;

        // We store hotseat items in canonical form which is this orientation invariant position
        // in the hotseat
        if (context instanceof Launcher && screen < 0 &&
                container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            item.screen = ((Launcher) context).getHotseat().getOrderInHotseat(cellX, cellY);
        } else {
            item.screen = screen;
        }

        final ContentValues values = new ContentValues();
        values.put(LauncherSettings.Favorites.CONTAINER, item.container);
        values.put(LauncherSettings.Favorites.CELLX, item.cellX);
        values.put(LauncherSettings.Favorites.CELLY, item.cellY);
        values.put(LauncherSettings.Favorites.SCREEN, item.screen);

        updateItemInDatabaseHelper(context, values, item, "moveItemInDatabase");
    }

    /**
     * Move and/or resize item in the DB to a new <container, screen, cellX, cellY, spanX, spanY>
     */
    static void modifyItemInDatabase(Context context, final ItemInfo item, final long container,
            final int screen, final int cellX, final int cellY, final int spanX, final int spanY) {
        String transaction = "DbDebug    Modify item (" + item.title + ") in db, id: " + item.id +
                " (" + item.container + ", " + item.screen + ", " + item.cellX + ", " + item.cellY +
                ") --> " + "(" + container + ", " + screen + ", " + cellX + ", " + cellY + ")";
        Launcher.sDumpLogs.add(transaction);
        Log.d(TAG, transaction);
        item.cellX = cellX;
        item.cellY = cellY;
        item.spanX = spanX;
        item.spanY = spanY;

        // We store hotseat items in canonical form which is this orientation invariant position
        // in the hotseat
        if (context instanceof Launcher && screen < 0 &&
                container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            item.screen = ((Launcher) context).getHotseat().getOrderInHotseat(cellX, cellY);
        } else {
            item.screen = screen;
        }

        final ContentValues values = new ContentValues();
        values.put(LauncherSettings.Favorites.CONTAINER, item.container);
        values.put(LauncherSettings.Favorites.CELLX, item.cellX);
        values.put(LauncherSettings.Favorites.CELLY, item.cellY);
        values.put(LauncherSettings.Favorites.SPANX, item.spanX);
        values.put(LauncherSettings.Favorites.SPANY, item.spanY);
        values.put(LauncherSettings.Favorites.SCREEN, item.screen);

        updateItemInDatabaseHelper(context, values, item, "modifyItemInDatabase");
    }

    // Aurora <jialf> <2013-11-18> add for fix app update but cls has been changed begin
    /**
     * Move and/or resize item in the DB to a new <intent, title>
     */
	static void modifyAuroraItemInDatabase(Context context,
			final ItemInfo item, final String title, final String intent) {
		final ContentValues values = new ContentValues();
		values.put(LauncherSettings.Favorites.TITLE, title);
		values.put(LauncherSettings.Favorites.INTENT, intent);
		
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
		
		updateItemInDatabaseHelper(context, values, item, "modifyItemInDatabase");
	}
    // Aurora <jialf> <2013-11-18> add for fix app update but cls has been changed end

    /**
     * Update an item to the database in a specified container.
     */
    static void updateItemInDatabase(Context context, final ItemInfo item) {
        final ContentValues values = new ContentValues();
        item.onAddToDatabase(values);
        item.updateValuesWithCoordinates(values, item.cellX, item.cellY);
        updateItemInDatabaseHelper(context, values, item, "updateItemInDatabase");
    }

    // Aurora <haojj> <2014-1-11> add for field begin
    static void updatePinyinInDatabase(Context context, final ItemInfo item, ContentValues values) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = LauncherSettings.Favorites.getContentUri(item.id, false);
        // Log.e("HJJ", "===>uri:" + uri);
    	if(item.toString().contains("sourceBounds=")) {
    		LauncherApplication.logVulcan.print("updateItemInDatabaseHelper: find out sourceBounds\n" + LogWriter.StackToString(new Throwable()));
    	}
        cr.update(uri, values, null, null);
    }
    // Aurora <haojj> <2014-1-11> end
    
	static boolean shortcutExists(Context context, Intent intent) {
		final ContentResolver cr = context.getContentResolver();
		final Intent in = intent;
		if (in.getAction() == null) {
			in.setAction(Intent.ACTION_VIEW);
		}
		in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

		Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
				new String[] { "intent" }, "intent=?",
				new String[] { in.toUri(0) }, null);
		boolean result = false;
		try {
			result = c.moveToFirst();
		} finally {
			c.close();
		}
		Log.i(TAG, "in.toUri(0) = " + in.toUri(0) + ", result = " + result);
		return result;
	}
    
    /**
     * Returns true if the shortcuts already exists in the database.
     * we identify a shortcut by its title and intent.
     */
    static boolean shortcutExists(Context context, String title, Intent intent) {
        final ContentResolver cr = context.getContentResolver();
        
        // Aurora <jialf> <2014-02-25> modify for fix bug #2469 begin
        final Intent in = intent;
        if (in.getAction() == null) {
            in.setAction(Intent.ACTION_VIEW);
        }
    	in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        
        Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
            new String[] { "title", "intent" }, "title=? and intent=?",
            new String[] { title, in.toUri(0) }, null);
        boolean result = false;
        try {
            result = c.moveToFirst();
        } finally {
            c.close();
        }
		Log.i(TAG, "title = " + title + ", in.toUri(0) = " + in.toUri(0)
				+ ", result = " + result);
        // Aurora <jialf> <2014-02-25> modify for fix bug #2469 end
        return result;
    }

    /**
     * Returns an ItemInfo array containing all the items in the LauncherModel.
     * The ItemInfo.id is not set through this function.
     */
    static ArrayList<ItemInfo> getItemsInLocalCoordinates(Context context) {
        ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI, new String[] {
                LauncherSettings.Favorites.ITEM_TYPE, LauncherSettings.Favorites.CONTAINER,
                LauncherSettings.Favorites.SCREEN, LauncherSettings.Favorites.CELLX, LauncherSettings.Favorites.CELLY,
                LauncherSettings.Favorites.SPANX, LauncherSettings.Favorites.SPANY }, null, null, null);

        final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
        final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
        final int screenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
        final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
        final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);
        final int spanXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANX);
        final int spanYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANY);

        try {
            while (c.moveToNext()) {
                ItemInfo item = new ItemInfo();
                item.cellX = c.getInt(cellXIndex);
                item.cellY = c.getInt(cellYIndex);
                item.spanX = c.getInt(spanXIndex);
                item.spanY = c.getInt(spanYIndex);
                item.container = c.getInt(containerIndex);
                item.itemType = c.getInt(itemTypeIndex);
                item.screen = c.getInt(screenIndex);

                items.add(item);
            }
        } catch (Exception e) {
            items.clear();
        } finally {
            c.close();
        }

        return items;
    }

    // Aurora <haojj> <2013-12-21> add for 从数据库中查询letters数据 begin
    static ArrayList<ShortcutInfo> getItemsFilterByLetter(Context context, String letters) {
    	int len = letters.length();
        ArrayList<ShortcutInfo> items = new ArrayList<ShortcutInfo>();
        if(len < 1) return items;
        final ContentResolver cr = context.getContentResolver();
        String letterString = letters.toUpperCase();
        // 英文的chrome如果不采用title like letters的话ch将搜索不到
        
        String selection = LauncherSettings.Favorites.ITEM_TYPE + "=" + LauncherSettings.Favorites.ITEM_TYPE_APPLICATION 
        		+ " and title like '%" + letterString + "%'"
        		+ " or title like '%" + letters + "%'"
        		+ " or simplePinyin like '%" + letterString.charAt(0) + "%'";
        // Log.e("HJJ", "selection:" + selection);
        Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI, new String[] {
        		LauncherSettings.Favorites._ID, LauncherSettings.Favorites.TITLE, LauncherSettings.Favorites.FULL_PINYIN, LauncherSettings.Favorites.SIMPLE_PINYIN}, 
        		selection, null, "simplePinyin");
        
        if(c == null) return items;
        
        final int itemIdIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
        final int fullPinyinIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.FULL_PINYIN);
        final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
        final int simplePinyinIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SIMPLE_PINYIN);
        
        long itemId;
        String fullPinyin;
        String title;
        String simplePinyin;
        
        ItemInfo info;
        try {
        	c.moveToPosition(-1);
            while (c.moveToNext()) {
                itemId = c.getLong(itemIdIndex);
                title = c.getString(titleIndex);
                fullPinyin = c.getString(fullPinyinIndex);
                simplePinyin = c.getString(simplePinyinIndex);
                //Hazel start to add for support lower-case searching
                if((title != null && (title.indexOf(letterString) != -1 || title.indexOf(letters) != -1 || title.toLowerCase().indexOf(letters)!= -1) )
                		||  (simplePinyin != null && simplePinyin.indexOf(letterString) != -1)
                		|| (fullPinyin != null && fullPinyin.indexOf(letterString) != -1)){
	                info = sBgItemsIdMap.get(itemId);
	                if (info instanceof ShortcutInfo){
	                	items.add((ShortcutInfo)info);
	                }
                } 
                
                //Log.e("HJJ", "===>id:" + itemId + ",title:" + title + ",fullPinyin:" + fullPinyin + ",simplePinyin:" + simplePinyin);
                //Log.e("HJJ", "===>[letters:" + letters + "," + title.indexOf(letters) + "," + simplePinyin.indexOf(letters) + "," + fullPinyin.indexOf(letters) + "]");
            }
        } catch (Exception e) {
            items.clear();
            // Log.e("HJJ", "===>error" + e.getMessage());
        } finally {
            c.close();
        }

        return items;
    }
    // Aurora <haojj> <2013-12-21> end
     
    /**
     * Find a folder in the db, creating the FolderInfo if necessary, and adding it to folderList.
     */
    FolderInfo getFolderById(Context context, HashMap<Long,FolderInfo> folderList, long id) {
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI, null,
                "_id=? and (itemType=? or itemType=?)",
                new String[] { String.valueOf(id),
                        String.valueOf(LauncherSettings.Favorites.ITEM_TYPE_FOLDER)}, null);

        try {
            if (c.moveToFirst()) {
                final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
                final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
                final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
                final int screenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
                final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
                final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);

                FolderInfo folderInfo = null;
                switch (c.getInt(itemTypeIndex)) {
                    case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                        folderInfo = findOrMakeFolder(folderList, id);
                        break;
                }

                folderInfo.title = c.getString(titleIndex);
                folderInfo.id = id;
                folderInfo.container = c.getInt(containerIndex);
                folderInfo.screen = c.getInt(screenIndex);
                folderInfo.cellX = c.getInt(cellXIndex);
                folderInfo.cellY = c.getInt(cellYIndex);

                return folderInfo;
            }
        } finally {
            c.close();
        }

        return null;
    }
    
	ContentValues addItemToContentValues(Context context,
			final ItemInfo item, final long container, final int screen,
			final int cellX, final int cellY) {
		item.container = container;
        item.cellX = cellX;
        item.cellY = cellY;
        // We store hotseat items in canonical form which is this orientation invariant position
        // in the hotseat
        if (context instanceof Launcher && screen < 0 &&
                container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            item.screen = ((Launcher) context).getHotseat().getOrderInHotseat(cellX, cellY);
        } else {
            item.screen = screen;
        }

        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        runOnWorkerThread(new Runnable() { 
        	public  void run() {
        		item.onAddToDatabase(values);
        	}
        });

        item.updateValuesWithCoordinates(values, item.cellX, item.cellY);
        if (item instanceof ShortcutInfo) {
			ShortcutInfo info = (ShortcutInfo) item;
			values.put(LauncherSettings.Favorites.FLAGS, info.flags);
			values.put(LauncherSettings.Favorites.APPWIDGET_ID, info.appWidgetId);
		}
        
        LauncherApplication app = (LauncherApplication) context.getApplicationContext();
        item.id = app.getLauncherProvider().generateNewId();
        values.put(LauncherSettings.Favorites._ID, item.id);
        
        // Lock on mBgLock *after* the db operation
        synchronized (sBgLock) {
            checkItemInfoLocked(item.id, item, null);
            sBgItemsIdMap.put(item.id, item);
            switch (item.itemType) {
                case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                    sBgFolders.put(item.id, (FolderInfo) item);
                    // Fall through
                case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                    if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP ||
                            item.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                        sBgWorkspaceItems.add(item);
                    } else {
                        if (!sBgFolders.containsKey(item.container)) {
                            // Adding an item to a folder that doesn't exist.
                            String msg = "adding item: " + item + " to a folder that " +
                                    " doesn't exist";
                            Log.e(TAG, msg);
                            Launcher.dumpDebugLogsToConsole();
                        }
                    }
                    break;
                case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                    sBgAppWidgets.add((LauncherAppWidgetInfo) item);
                    break;
            }
        }
        
        return values;
	}

    /**
     * Add an item to the database in a specified container. Sets the container, screen, cellX and
     * cellY fields of the item. Also assigns an ID to the item.
     */
    static void addItemToDatabase(Context context, final ItemInfo item, final long container,
            final int screen, final int cellX, final int cellY, final boolean notify) {
    	
    	final String senderStack = LogWriter.StackToString(new Throwable());
    	
        item.container = container;
        item.cellX = cellX;
        item.cellY = cellY;
        // We store hotseat items in canonical form which is this orientation invariant position
        // in the hotseat
        if (context instanceof Launcher && screen < 0 &&
                container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            item.screen = ((Launcher) context).getHotseat().getOrderInHotseat(cellX, cellY);
        } else {
            item.screen = screen;
        }

        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        item.onAddToDatabase(values);

        LauncherApplication app = (LauncherApplication) context.getApplicationContext();
        item.id = app.getLauncherProvider().generateNewId();
        values.put(LauncherSettings.Favorites._ID, item.id);
        item.updateValuesWithCoordinates(values, item.cellX, item.cellY);

        // Aurora <jialf> <2013-09-23> modify for install app begin
		if (item instanceof ShortcutInfo) {
			ShortcutInfo info = (ShortcutInfo) item;
			values.put(LauncherSettings.Favorites.FLAGS, info.flags);
			values.put(LauncherSettings.Favorites.APPWIDGET_ID, info.appWidgetId);
			values.put(LauncherSettings.Favorites.ENABLE_WIDGETS, info.enableWidgets);
		}
        // Aurora <jialf> <2013-09-23> modify for install app end

        Runnable r = new Runnable() {
            public void run() {
                String transaction = "DbDebug    Add item (" + item.title + ") to db, id: "
                        + item.id + " (" + container + ", " + screen + ", " + cellX + ", "
                        + cellY + ")";
                Launcher.sDumpLogs.add(transaction);
                Log.d(TAG, transaction);
                
            	if(item.toString().contains("sourceBounds=")) {
            		LauncherApplication.logVulcan.print("updateItemInDatabaseHelper: find out sourceBounds\n" + senderStack);
            	}
                
                final Cursor myResult = cr.query(LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION,
                		new String[]{"title"}, "favorites.container=? AND favorites.screen=? AND favorites.cellX=? AND favorites.cellY=? AND favorites.title<>?",
                		new String[]{String.valueOf(item.container), String.valueOf(item.screen), String.valueOf(item.cellX), String.valueOf(item.cellY), String.valueOf(item.title)},
                		null);
                if(myResult.getCount() > 0) {
                	LauncherApplication.logVulcan.print("updateItemInDatabaseHelper: myResult.getCount() = " + myResult.getCount());
           
                	if(item.title != null) {
                		myResult.moveToFirst();
                		String newString = item.title.toString().trim();
                		String dbString = myResult.getString(0).trim();
                		LauncherApplication.logVulcan.print("updateItemInDatabaseHelper: newString ="
                    			+ newString.getBytes() + ", dbString ="   + dbString.getBytes());
                		if(newString.lastIndexOf(dbString) == -1 && dbString.lastIndexOf(newString) == -1) {
                        	LauncherApplication.logVulcan.print("updateItemInDatabaseHelper: repeat data,title = "
                        			+ newString + ", container = " + item.container + " ,screen = " + item.screen + ", cellX = " + item.cellX + ", cellY = " + item.cellY);
                        	LauncherApplication.logVulcan.print("updateItemInDatabaseHelper: repeat data(db),title = "
                        			+ dbString);
                        	LauncherApplication.logVulcan.print("updateItemInDatabaseHelper: find out repeat data \n" + senderStack);
                		}

                	}
                }
                myResult.close();
                
                cr.insert(notify ? LauncherSettings.Favorites.CONTENT_URI :
                        LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION, values);

                // Lock on mBgLock *after* the db operation
                synchronized (sBgLock) {
                    checkItemInfoLocked(item.id, item, null);
                    sBgItemsIdMap.put(item.id, item);
                    switch (item.itemType) {
                        case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                            sBgFolders.put(item.id, (FolderInfo) item);
                            // Fall through
                        case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                        case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                            if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP ||
                                    item.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                                sBgWorkspaceItems.add(item);
                                Log.d(TAG, "--------add item to sBgWorkspaceItems--->"+item.title);
                            } else {
                                if (!sBgFolders.containsKey(item.container)) {
                                    // Adding an item to a folder that doesn't exist.
                                    String msg = "adding item: " + item + " to a folder that " +
                                            " doesn't exist";
                                    Log.e(TAG, msg);
                                    Launcher.dumpDebugLogsToConsole();
                                }
                            }
                            break;
                        case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                            sBgAppWidgets.add((LauncherAppWidgetInfo) item);
                            break;
                    }
                }
            }
        };
        runOnWorkerThread(r);
    }

    /**
     * Creates a new unique child id, for a given cell span across all layouts.
     */
    static int getCellLayoutChildId(
            long container, int screen, int localCellX, int localCellY, int spanX, int spanY) {
        return (((int) container & 0xFF) << 24)
                | (screen & 0xFF) << 16 | (localCellX & 0xFF) << 8 | (localCellY & 0xFF);
    }

    static int getCellCountX() {
        return mCellCountX;
    }

    static int getCellCountY() {
        return mCellCountY;
    }

    /**
     * Updates the model orientation helper to take into account the current layout dimensions
     * when performing local/canonical coordinate transformations.
     */
    static void updateWorkspaceLayoutCells(int shortAxisCellCount, int longAxisCellCount) {
        mCellCountX = shortAxisCellCount;
        mCellCountY = longAxisCellCount;
    }

    /**
     * Removes the specified item from the database
     * @param context
     * @param item
     */
    static void deleteItemFromDatabase(Context context, final ItemInfo item) {

    	LauncherApplication.logVulcan.print("deleteItemFromDatabase: screen = " 
    				+ item.screen + ",cellX = " + item.cellX + ",cellY =" + item.cellY
    				+ ", container =" + item.container
    				+ ", title = " + item.title + ", container = " + item.NO_ID);
		
        final ContentResolver cr = context.getContentResolver();
        final Uri uriToDelete = LauncherSettings.Favorites.getContentUri(item.id, false);

        Runnable r = new Runnable() {
            public void run() {

				LauncherApplication.logVulcan.print("delete item because deleteItemFromDatabase");
				
                String transaction = "DbDebug    Delete item (" + item.title + ") from db, id: "
                        + item.id + " (" + item.container + ", " + item.screen + ", " + item.cellX +
                        ", " + item.cellY + ")";
                Launcher.sDumpLogs.add(transaction);
                Log.d(TAG, transaction);

                cr.delete(uriToDelete, null, null);

                // Lock on mBgLock *after* the db operation
                synchronized (sBgLock) {
                    switch (item.itemType) {
                        case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                            sBgFolders.remove(item.id);
                            for (ItemInfo info: sBgItemsIdMap.values()) {
                                if (info.container == item.id) {
                                    // We are deleting a folder which still contains items that
                                    // think they are contained by that folder.
                                    String msg = "deleting a folder (" + item + ") which still " +
                                            "contains items (" + info + ")";
                                    Log.e(TAG, msg);
                                    Launcher.dumpDebugLogsToConsole();
                                }
                            }
                            sBgWorkspaceItems.remove(item);
                            break;
                        case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                        case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                            sBgWorkspaceItems.remove(item);
                            break;
                        case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                            sBgAppWidgets.remove((LauncherAppWidgetInfo) item);
                            break;
                    }
                    sBgItemsIdMap.remove(item.id);
                    sBgDbIconCache.remove(item);
                    
                    LauncherApplication.logVulcan.print("deleteItemFromDatabase: sBgItemsIdMap.size = " + sBgItemsIdMap.size());
                    Log.d("vulcan-contact","deleteItemFromDatabase: sBgItemsIdMap.size = " + sBgItemsIdMap.size());
                }
            }
        };
        runOnWorkerThread(r);
    }

    /**
     * Remove the contents of the specified folder from the database
     */
    static void deleteFolderContentsFromDatabase(Context context, final FolderInfo info) {
        final ContentResolver cr = context.getContentResolver();

        Runnable r = new Runnable() {
            public void run() {
                cr.delete(LauncherSettings.Favorites.getContentUri(info.id, false), null, null);
				LauncherApplication.logVulcan.print("delete folder on deleteFolderContentsFromDatabase");
                // Lock on mBgLock *after* the db operation
                synchronized (sBgLock) {
                    sBgItemsIdMap.remove(info.id);
                    sBgFolders.remove(info.id);
                    sBgDbIconCache.remove(info);
                    sBgWorkspaceItems.remove(info);
                }

            	// Aurora <jialf> <2013-10-02> remove for dismiss folder begin
                /*cr.delete(LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION,
                        LauncherSettings.Favorites.CONTAINER + "=" + info.id, null);
                // Lock on mBgLock *after* the db operation
                synchronized (sBgLock) {
                    for (ItemInfo childInfo : info.contents) {
                        sBgItemsIdMap.remove(childInfo.id);
                        sBgDbIconCache.remove(childInfo);
                    }
                }*/
            	// Aurora <jialf> <2013-10-02> remove for dismiss folder end
            }
        };
        runOnWorkerThread(r);
    }

    /**
     * Set this as the current Launcher activity object for the loader.
     */
    public void initialize(Callbacks callbacks) {
        synchronized (mLock) {
            mCallbacks = new WeakReference<Callbacks>(callbacks);
        }
    }

    private boolean mSdcardMounted;
    private String[] mSdcardApps;
    private boolean mAppAvailable = false;
    
    public void setmSdcardMounted(boolean mSdcardMounted) {
		this.mSdcardMounted = mSdcardMounted;
	}
    
	public boolean ismSdcardMounted() {
		return mSdcardMounted;
	}

	/**
     * Call from the handler for ACTION_PACKAGE_ADDED, ACTION_PACKAGE_REMOVED and
     * ACTION_PACKAGE_CHANGED.
     */
	@Override
	public void onReceive(Context context, Intent intent) {
		if (DEBUG_LOADERS)
			Log.d(TAG, "onReceive intent=" + intent);

		final String action = intent.getAction();
		//Log.d("vulcan-startloader", "enter onReceive action = " + action);
		LauncherApplication.logVulcan.print(String.format("onReceive,action = %s",action));

		//iht桌面正在加载图标
		if (isLoadingWorkspace()) {
			//Log.e("linp","***********LoadingWorkspace is loading**************");
			enableInstallQueue();
		}
		
		//图标下载更新
		/*if(action.equals(NOTIFY_APPS_TO_UPDATEIICON)){
			ArrayList<String> pkgList =  intent.getStringArrayListExtra(NOTIFY_APPS_TO_UPDATEIICON_KEY);
			if(pkgList == null || pkgList.isEmpty()){
				return;
			}
			//获得回调句柄,当其处在编辑状态时，结束当前操作；
			Callbacks calbacks = mCallbacks != null?mCallbacks.get():null;
			if(calbacks != null && calbacks instanceof Launcher){
				Launcher launcher = (Launcher) calbacks;
				if (launcher.getEditMode() == Launcher.EditMode.DRAG) {
					launcher.cancelDrag();
				}
			}
			enqueuePackageUpdated(new UpdateIconByPackageAndClassName(context, pkgList));
		}*/
		
		//iht卸载、增加、更新应用
		if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
				|| Intent.ACTION_PACKAGE_REMOVED.equals(action)
				|| Intent.ACTION_PACKAGE_ADDED.equals(action)) {
			final String packageName = intent.getData().getSchemeSpecificPart();
			final boolean replacing = intent.getBooleanExtra(
					Intent.EXTRA_REPLACING, false);

			Log.i(TAG, "action = " + action + ", packageName = " + packageName);
			LauncherApplication.logVulcan.print("onReceive:packageName = "
					+ packageName + ", replacing = " + replacing);

			int op = PackageUpdatedTask.OP_NONE;

			if (packageName == null || packageName.length() == 0) {
				// they sent us a bad intent
				return;
			}
			if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
				op = PackageUpdatedTask.OP_UPDATE;
			} else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
				if (!replacing) {
					op = PackageUpdatedTask.OP_REMOVE;
					mAppsCount--;
				}
				// else, we are replacing the package, so a PACKAGE_ADDED will
				// be sent
				// later, we will update the package at this time
			} else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
				Log.e("linp", "Intent.ACTION_PACKAGE_ADDED");
				if (!replacing) {
					op = PackageUpdatedTask.OP_ADD;
					mAppsCount++;
					/*if(mAppsCount >= 60 && showWorkspaceCling){
						 if (mCallbacks != null) {
					            Callbacks callbacks = mCallbacks.get();
					            if (callbacks != null) {
									final Launcher launcher = (Launcher) callbacks;
									launcher.showFirstRunWorkspaceCling();
									showWorkspaceCling = false;
					            }
						 }
					}*/
				} else {
					op = PackageUpdatedTask.OP_UPDATE;
				}
			}

			// exit dragging && folder importting mode when app update start
			final Callbacks callbacks = mCallbacks != null ? mCallbacks.get()
					: null;
			if (callbacks != null && callbacks instanceof Launcher
					&& op != PackageUpdatedTask.OP_ADD) {
				Launcher launcher = (Launcher) callbacks;
				if (launcher.getEditMode() == Launcher.EditMode.DRAG) {
					launcher.cancelDrag();
				}

				if (launcher.getEditMode() == Launcher.EditMode.FOLDER_IMPORT) {
					LauncherApplication.logVulcan
							.print("onReceive: to call launcher.exitFolderImportMode,mode = "
									+ launcher.getEditMode());
					launcher.exitFolderImportMode(true);
					Toast.makeText(launcher, R.string.exit_editmode, 150)
							.show();
				}
			}
			// exit dragging && folder importting mode when app update end

			if (op != PackageUpdatedTask.OP_NONE) {

				LauncherApplication.logVulcan.print("onReceive: op = " + op
						+ ",mUseInstallORUninstallQueue = "
						+ mUseInstallORUninstallQueue);
				// Aurora-start:xiejun:Bug#56
				if (mUseInstallORUninstallQueue) {
					Log.e("linp",
							"enqueueInstallORUninstallAppQueue put need install app to queue!"
									+ " + action = " + action
									+ " packageName = " + packageName);
					enqueueInstallORUninstallAppQueue(new PackageUpdatedTask(
							op, new String[] { packageName }, replacing));
				} else {
					Log.e("linp",
							"enqueuePackageUpdated put need install app to queue!");
					mHandler.post(new Runnable() {
						@Override
						public void run() {
						//	callbacks =LauncherModel.this.tryGetCallback(callbacks);
							if(callbacks!=null){
								callbacks.dampedOverScroll();
							}
						}
					});
					enqueuePackageUpdated(new PackageUpdatedTask(op,
							new String[] { packageName }, replacing));
				}
				// Aurora-end:xiejun:Bug#56
			}
			// Aurora <jialf> <2013-10-18> modify for bug 82# begin
		} else if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)) {
			// First, schedule to add these apps back in.
			String[] packages = intent
					.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
			/*
			 * //Aurora-start:xiejun:Bug#56 if(mUseInstallORUninstallQueue){
			 * enqueueInstallORUninstallAppQueue(new
			 * PackageUpdatedTask(PackageUpdatedTask.OP_ADD, packages)); }else{
			 * enqueuePackageUpdated(new
			 * PackageUpdatedTask(PackageUpdatedTask.OP_ADD, packages)); }
			 * //Aurora-end:xiejun:Bug#56 // Then, rebind everything.
			 * startLoaderFromBackground();
			 */
			// startLoaderFromBackground();
			/*
			 * mAuroraExternalReady = true; mSdcardApps = packages;
			 * mSdcardMounted = true; String spKey =
			 * LauncherApplication.getSharedPreferencesKey(); Editor editor =
			 * mApp.getSharedPreferences(spKey, Context.MODE_PRIVATE).edit();
			 * editor.putBoolean(LauncherApplication.APPLICATIONS_AVAILABLE,
			 * mSdcardMounted); editor.commit();
			 */

			/*
			 * vulcan changed thes code in 2014-5-12 According to new
			 * requirement, we will not delete any application even if sd card
			 * is offline, so everytime we receive application available message
			 * we must reload application icon data.
			 */
			/*
			 * Log.i(TAG,
			 * "-------ACTION_EXTERNAL_APPLICATIONS_AVAILABLE-------");
			 * LauncherApplication.logVulcan.print("mSdcardMounted = " +
			 * mSdcardMounted + "\tmAppAvailable = " + mAppAvailable); if
			 * (mSdcardMounted && mAppAvailable) {
			 * LauncherApplication.logVulcan.
			 * print("return in ACTION_EXTERNAL_APPLICATIONS_AVAILABLE(pkgNum="
			 * +packages.length+")"); return; }
			 */
			mSdcardMounted = true;
			mAppAvailable = true;
			mSdcardApps = packages;
			LauncherApplication.logVulcan.print("to call forceAuroraReload when ACTION_EXTERNAL_APPLICATIONS_AVAILABLE(pkgNum="+packages.length+")");
			forceAuroraReload();
			// writeFirstBootValue();
		} else if (Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE
				.equals(action)) {
			/*
			 * String[] packages =
			 * intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
			 * //Aurora-start:xiejun:Bug#56 if(mUseInstallORUninstallQueue){
			 * enqueueInstallORUninstallAppQueue(new PackageUpdatedTask(
			 * PackageUpdatedTask.OP_UNAVAILABLE, packages)); }else{
			 * enqueuePackageUpdated(new PackageUpdatedTask(
			 * PackageUpdatedTask.OP_UNAVAILABLE, packages)); }
			 */
			// Aurora <jialf> <2013-10-18> modify for bug 82# end
			// Aurora-end:xiejun:Bug#56
			Log.i(TAG,
					"--------ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE-------");
			mAppAvailable = false;
		} else if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {

			// If we have changed locale we need to clear out the labels in all
			// apps/workspace.

			LauncherApplication.logVulcan
					.print("to call forceReload when ACTION_LOCALE_CHANGED");
			Log.d("vulcan-automan","onReceive: ACTION_LOCALE_CHANGED");
			forceReload();
			//finishInMainThread();
			
			
		} else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
			// Check if configuration change was an mcc/mnc change which would
			// affect app resources
			// and we would need to clear out the labels in all apps/workspace.
			// Same handling as
			// above for ACTION_LOCALE_CHANGED
			Configuration currentConfig = context.getResources()
					.getConfiguration();
			if (mPreviousConfigMcc != currentConfig.mcc) {
				Log.d(TAG, "Reload apps on config change. curr_mcc:"
						+ currentConfig.mcc + " prevmcc:" + mPreviousConfigMcc);
				LauncherApplication.logVulcan
						.print("to call forceReload when ACTION_CONFIGURATION_CHANGED");
				//forceReload();
			}
			// Update previousConfig
			mPreviousConfigMcc = currentConfig.mcc;
		}
		// AURORA-START:remove search:modify:xiejun
		/*
		 * else if
		 * (SearchManager.INTENT_GLOBAL_SEARCH_ACTIVITY_CHANGED.equals(action)
		 * || SearchManager.INTENT_ACTION_SEARCHABLES_CHANGED.equals(action)) {
		 * if (mCallbacks != null) { Callbacks callbacks = mCallbacks.get(); if
		 * (callbacks != null) { callbacks.bindSearchablesChanged(); } } }
		 */
		// AURORA-END:remove search:modify:xiejun
	}
    
	//AURORA_START 2015-03-16:iht 图标更新**********************************************//LauncherModel做更新监听
	private void updateAppsIcon(final Context context, ArrayList<ShortcutInfo> slist, HashMap<String, String> pkgMap){
		PackageManager mPackageManager;
		if(!slist.isEmpty()){
			//桌面、Hotseat、文件夹
			for(ShortcutInfo sinfo : slist){
				//获得了需要更新的Icon的基本信息，接下来区分其类别，找到该Icon的View并进行更新
				//桌面应用
				if(sinfo.container == LauncherSettings.Favorites.CONTAINER_DESKTOP){ //桌面
					CellLayout cellLayout = (CellLayout) mWorkspace.getPageAt(sinfo.screen);
					
					//Log.v("iht-icon", "container:"+sinfo.container+", "+"screen:"+sinfo.screen+", cellx:"+sinfo.cellX+", celly："+sinfo.cellY);
					
					final BubbleTextView iconView = (BubbleTextView) cellLayout.getChildAt(sinfo.cellX, sinfo.cellY);
					if(iconView != null){
						//final Bitmap icon =mUtils2Icon.getIcon("com.tencent.mobileqq", "com.tencent.mobileqq.activity.SplashActivity", Utils2Icon.OUTER_SHADOW);
						Bitmap bitmap =mUtils2Icon.getIcon(sinfo.getPackageName(), sinfo.getClassName(), Utils2Icon.OUTER_SHADOW);
						final Bitmap icon = bitmap;
						if(bitmap != null){
						//图标是否需要缩放
				    	if(DeviceProperties.isNeedScale()){
				    		bitmap = Utilities.zoomBitmap(bitmap, context);
				    	}
						//2、保存至IconCache,下次取图；
						mIconCache.updateIcon(sinfo.getIntent(), icon);
						sinfo.setIcon(icon);
						//3、更新IconView的图标
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								iconView.setCompoundDrawablesWithIntrinsicBounds(null, 
										new BitmapDrawable(context.getResources(), icon), null, null);//UI线程更新
							}
						});
						}
						if(bitmap != null && bitmap.isRecycled()){
							bitmap.recycle();
						}
						if(icon != null && icon.isRecycled()){
							icon.recycle();
						}
					}
				}
				
				//Hotseat应用
				if(sinfo.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT){
					Launcher launcher = (Launcher) mCallbacks.get();
					CellLayout cellLayout = (CellLayout) launcher.mHotseat.getChildAt(0);
					final BubbleTextView iconView = (BubbleTextView) cellLayout.getChildAt(sinfo.cellX, sinfo.cellY);
					//获取Icon，将iconView更新，并且保存icon至IconCache
					if(iconView != null){
						//final Bitmap icon =mUtils2Icon.getIcon("com.tencent.mobileqq", "com.tencent.mobileqq.activity.SplashActivity", Utils2Icon.OUTER_SHADOW);
						Bitmap bitmap =mUtils2Icon.getIcon(sinfo.getPackageName(), sinfo.getClassName(), Utils2Icon.OUTER_SHADOW);
						final Bitmap icon = bitmap;
						if(bitmap != null){
						if(DeviceProperties.isNeedScale()){
				    		bitmap = Utilities.zoomBitmap(bitmap, context);
				    	}
						//2、保存至IconCache,下次取图；
						mIconCache.updateIcon(sinfo.getIntent(), icon);
						sinfo.setIcon(icon);
						//3、更新IconView的图标
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								iconView.setCompoundDrawablesWithIntrinsicBounds(null, 
										new BitmapDrawable(context.getResources(), icon), null, null);//UI线程更新
							}
						});
						}
						if(bitmap != null && bitmap.isRecycled()){
							bitmap.recycle();
						}
						if(icon != null && icon.isRecycled()){
							icon.recycle();
						}
					}
				}
				
				//应用在文件夹内
				if(sinfo.container > 0){
					//获取的文件夹的Icon
					final FolderIcon mFolderIcon = mWorkspace.getFolderByContainer(sinfo.container);
					if(mFolderIcon != null){
					//获得文件夹对象
				    final Folder folder = mFolderIcon.getFolder();
					//文件夹空间
					FolderWorkspace mFolderWorkspace = folder.getFolderWorkspace(); 
					//具体页
					CellLayout cellLayout = (CellLayout) mFolderWorkspace.getPageAt(sinfo.screen);
					//具体IconView
					final BubbleTextView iconView = (BubbleTextView) cellLayout.getChildAt(sinfo.cellX, sinfo.cellY);
					//获取Icon，将iconView更新，并且保存icon至IconCache
					if(iconView != null){
						//final Bitmap icon =mUtils2Icon.getIcon("com.tencent.mobileqq", "com.tencent.mobileqq.activity.SplashActivity", Utils2Icon.OUTER_SHADOW);
						Bitmap bitmap =mUtils2Icon.getIcon(sinfo.getPackageName(), sinfo.getClassName(), Utils2Icon.OUTER_SHADOW);
						final Bitmap icon = bitmap;
						if(bitmap != null){
						if(DeviceProperties.isNeedScale()){
				    		bitmap = Utilities.zoomBitmap(bitmap, context);
				    	}
						//2、保存至IconCache,下次启动时取图；
						mIconCache.updateIcon(sinfo.getIntent(), icon);
						sinfo.setIcon(icon);
						//3、更新IconView的图标
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								iconView.setCompoundDrawablesWithIntrinsicBounds(null, 
										new BitmapDrawable(context.getResources(), icon), null, null);//UI线程更新
								mFolderIcon.invalidate();
							}
						});
						}
						if(icon != null && icon.isRecycled()){
							icon.recycle();
						}
						if(bitmap != null && bitmap.isRecycled()){
							bitmap.recycle();
						}
					}
				 }
				}			
			}//ShortCutInfo 循环结束
			
			//iht 2015-03-13
			//更新、同时将相关Icon保存之后，即可更新快速检索的部分
			Launcher launcher = (Launcher) mCallbacks.get(); 
			final QuickIndexListAdapter quickListAdapter = launcher.mQuickIndexListAdapter;
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Log.v("test", "------------QuickIndexFrameLayout---------+++++++++++++");
					quickListAdapter.notifyDataSetChanged();
				}
			});
			//快速检索结束
		}
		
		//widget信息更新
		if(!pkgMap.isEmpty()){
			
			//M:shigq Fix bug #14903 begin
			if (mCallbacks == null || mCallbacks.get() == null) return;
			//M:shigq Fix bug #14903 end
			
			Launcher launcher = (Launcher) mCallbacks.get();
			mPackageManager = context.getPackageManager();

			//判断ShortInfo是否存在weight
			AppsCustomizePagedView appsView = launcher.mWidgetViewContent; 
			int pageCount = appsView.getPageCount();
			if(pageCount == 0){
				return;
			}
			
			//String dd = "com.sankuai.meituan/com.sankuai.meituan.widget.DealWidget";
			
			for(int i=0; i<pageCount; i++){
				PagedViewGridLayout layout = (PagedViewGridLayout) appsView.getPageAt(i);
				//bug#16248 begin
				if(layout==null){
					continue;
				}
				//bug#16248 end
				int childCount = layout.getChildCount();
				if(childCount == 0){
					continue;
				}
				
				//遍历每一页（4个）的widget的Icon
				for(int k=0; k<childCount; k++){
					
					final View widget = layout.getChildAt(k);
					Object tag = widget.getTag();
					
					//页内的child
					String pkgName = "";
					String clsName = "";
					int previewImage = 0;
					Drawable d = null;
					
					if(widget instanceof PagedViewIcon){
						
						ApplicationInfo ainfo = (ApplicationInfo)tag;   //ainfo.flags;
						pkgName = ainfo.componentName.getPackageName();
						clsName = ainfo.componentName.getClassName();
						previewImage = 0;
						
						if(pkgMap.containsKey(pkgName)){
							d  = Utils2Icon.getInstance(context).getIconDrawable(pkgName,Utils2Icon.OUTER_SHADOW);
						}
						
					}else if(widget instanceof PagedViewWidget){ 
						//数据类型的确定
						PendingAddItemInfo pendItem = null;
						
						if(tag instanceof PendingAddWidgetInfo){
							pendItem = (PendingAddWidgetInfo) tag;//widget
							previewImage = ((PendingAddWidgetInfo) tag).getpreviewImage();
							
						}else if(tag instanceof PendingAddShortcutInfo){
							pendItem = (PendingAddShortcutInfo)tag;//shortCutinfo
							previewImage = 0;
						}

						if(pendItem != null){
							pkgName = pendItem.componentName.getPackageName();
							clsName = pendItem.componentName.getClassName();
						}
						
						if(sWidgetItemMap.containsKey(pkgName)){
							try {
								//context.getPackageManager()
		                		int flag = mPackageManager.getPackageInfo(pkgName,PackageManager.GET_CONFIGURATIONS).applicationInfo.flags;
		                		flag = flag & android.content.pm.ApplicationInfo.FLAG_SYSTEM;
		                		
		                		if(flag == 1 && previewImage != 0){
		                            d = context.createPackageContext(pkgName,
		                            		Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY)
		                        			.getResources().getDrawable(previewImage);
		                         d = new BitmapDrawable(context.getResources(),mUtils2Icon.getRoundedBitmap(Utilities.drawable2bitmap(d), false));
								}else{
									//TODO --需要注意--201501301921
									d  = Utils2Icon.getInstance(context).getIconDrawable(pkgName,Utils2Icon.OUTER_SHADOW);
								}
							} catch (Exception e) {
								// TODO: handle exception
							}
						}
					}
					
					if(d != null){
						final Drawable icon = d;
						mHandler.post(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								 ((PagedViewWidget) widget).applyPreviewImage(icon, 0);
								 widget.invalidate();
							}
						});
					}
				}
			}
		}//widget更新结束
	}//AURORA_END 2015-03-16:iht 图标更新
	

    //Aurora-start:xiejun:Bug#56
    private static boolean mUseInstallORUninstallQueue=false;
    private static ArrayList<PackageUpdatedTask> mInstallORUninstallAppQueue =
            new ArrayList<PackageUpdatedTask>();
    private static Object lockQueen=new Object();
    static void enableInstallQueue() {  
    	Log.i("xiejun","1:LauncherMode:enableInstallQueue");
    	mUseInstallORUninstallQueue = true;
    	
    	LauncherApplication.logVulcan.print("enableInstallQueue is called");
    	
    }
    static void disableAndFlushInstallQueue() {
    	LauncherApplication.logVulcan.print("disableAndFlushInstallQueue is called");
    	mUseInstallORUninstallQueue = false;
        flushInstallQueue();
    }
    private static void flushInstallQueue() {
      synchronized (lockQueen) {
    	  Iterator<PackageUpdatedTask> iter = mInstallORUninstallAppQueue.iterator();
          while (iter.hasNext()) {
        	  enqueuePackageUpdated(iter.next());
              iter.remove();
          }
      }
	  
	}
    private void enqueueInstallORUninstallAppQueue(PackageUpdatedTask Task){
    	synchronized (lockQueen) {
    		mInstallORUninstallAppQueue.add(Task);
    	}
    }
    //Aurora-start:xiejun:Bug#56

    private void forceReload() {
        resetLoadedState(true, true);

        // Do this here because if the launcher activity is running it will be restarted.
        // If it's not running startLoaderFromBackground will merely tell it that it needs
        // to reload.
        startLoaderFromBackground();
    }

    public void resetLoadedState(boolean resetAllAppsLoaded, boolean resetWorkspaceLoaded) {
        synchronized (mLock) {
            // Stop any existing loaders first, so they don't set mAllAppsLoaded or
            // mWorkspaceLoaded to true later
            stopLoaderLocked();
            if (resetAllAppsLoaded) mAllAppsLoaded = false;
            if (resetWorkspaceLoaded) mWorkspaceLoaded = false;
        }
    }

    /**
     * When the launcher is in the background, it's possible for it to miss paired
     * configuration changes.  So whenever we trigger the loader from the background
     * tell the launcher that it needs to re-run the loader when it comes back instead
     * of doing it now.
     */
    public void startLoaderFromBackground() {
        boolean runLoader = false;
        if (mCallbacks != null) {
            Callbacks callbacks = mCallbacks.get();
            if (callbacks != null) {
                // Only actually run the loader if they're not paused.
                if (!callbacks.setLoadOnResume()) {
                    runLoader = true;
                }
                Launcher launcher = (Launcher) callbacks;
				if (launcher.isDragMode())
					launcher.cancelDrag();
            }
        }
        if (runLoader) {
        	Log.e("linp", "--------------------------------------startLoaderFromBackground runLoader");
			LauncherApplication.logVulcan.print("to call startLoader when startLoaderFromBackground");
            startLoader(false, -1);
        }
    }
    
    public void forceAuroraReload() {
    	resetLoadedState(true, true);
    	
        boolean runLoader = false;
        if (mCallbacks != null) {
            Callbacks callbacks = mCallbacks.get();
            if (callbacks != null) {
				final Launcher launcher = (Launcher) callbacks;
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if (launcher.isDragMode())
							launcher.cancelDrag();
							LauncherApplication.logVulcan.print("to call startLoader when forceAuroraReload");
		            		startLoader(true, -1);
					}
				}, 0);
            	Log.e("linp", "------------------forceAuroraReload startLoader ");
            }
        }
    }

    // If there is already a loader task running, tell it to stop.
    // returns true if isLaunching() was true on the old task
    private boolean stopLoaderLocked() {
        boolean isLaunching = false;
        LoaderTask oldTask = mLoaderTask;
        if (oldTask != null) {
            if (oldTask.isLaunching()) {
                isLaunching = true;
            }
            oldTask.stopLocked();
        }
        return isLaunching;
    }

    public void startLoader(boolean isLaunching, int synchronousBindPage) {
    	Log.e("linp", "@@@@@@@@@@@@startLoader");
        synchronized (mLock) {
            if (DEBUG_LOADERS) {
                Log.d(TAG, "startLoader isLaunching=" + isLaunching);
            }

            // Clear any deferred bind-runnables from the synchronized load process
            // We must do this before any loading/binding is scheduled below.
            mDeferredBindRunnables.clear();

            // Don't bother to start the thread if we know it's not going to do anything
            if (mCallbacks != null && mCallbacks.get() != null) {
                // If there is already one running, tell it to stop.
                // also, don't downgrade isLaunching if we're already running
                isLaunching = isLaunching || stopLoaderLocked();
                mLoaderTask = new LoaderTask(mApp, isLaunching,Utilities.getCurrentLanuage(mApp));
                if (synchronousBindPage > -1 && mAllAppsLoaded && mWorkspaceLoaded) {
                    mLoaderTask.runBindSynchronousPage(synchronousBindPage);
                } else {
                    sWorkerThread.setPriority(Thread.NORM_PRIORITY);
                    sWorker.post(mLoaderTask);
                }
            }
        }
    }

    void bindRemainingSynchronousPages() {
        // Post the remaining side pages to be loaded
        if (!mDeferredBindRunnables.isEmpty()) {
            for (final Runnable r : mDeferredBindRunnables) {
                mHandler.post(r, MAIN_THREAD_BINDING_RUNNABLE);
            }
            mDeferredBindRunnables.clear();
        }
    }

    public void stopLoader() {
        synchronized (mLock) {
            if (mLoaderTask != null) {
                mLoaderTask.stopLocked();
            }
        }
    }

    public boolean isAllAppsLoaded() {
        return mAllAppsLoaded;
    }
    boolean isLoadingWorkspace() {
        synchronized (mLock) {
            if (mLoaderTask != null) {
                return mLoaderTask.isLoadingWorkspace();
            }
        }
        return false;
    }
    
    
    /**
     * this method is used to test
     * vulcan created it in 2014-7-22
     */
    public static void printItemsInMemory(String tag){

    	Log.d("vulcan-crash",tag + String.format("printItemInMemory: item.size = %d",	sBgItemsIdMap.size()));
		ShortcutInfo si;
		for (ItemInfo info : sBgItemsIdMap.values()) {
			if (info instanceof ShortcutInfo) {
				si = (ShortcutInfo) info;
				if (si.title.equals("有信") || si.title.equals("应用宝")) {
					Log.d("vulcan-crash",
							String.format("~~~~~~item id = %d, %s(%d,%d), hashcode = %d",
										si.id,
										si.title, si.cellX, si.cellY,
										si.hashCode()));
				}
			}
		}
    	return;
    }

    /**
     * Runnable for the thread that loads the contents of the launcher:
     *   - workspace icons
     *   - widgets
     *   - all apps icons
     */
    private class LoaderTask implements Runnable {
        private Context mContext;
        private boolean mIsLaunching;
        private boolean mIsLoadingAndBindingWorkspace;
        private boolean mStopped;
        private boolean mLoadAndBindStepFinished;

        private HashMap<Object, CharSequence> mLabelCache;

        private String currentLanguage;
        
        LoaderTask(Context context, boolean isLaunching,String language) {
            mContext = context;
            mIsLaunching = isLaunching;
            mLabelCache = new HashMap<Object, CharSequence>();
            currentLanguage = language;
        }

        boolean isLaunching() {
            return mIsLaunching;
        }

        boolean isLoadingWorkspace() {
            return mIsLoadingAndBindingWorkspace;
        }

        private void loadAndBindWorkspace() {
            mIsLoadingAndBindingWorkspace = true;

            // Load the workspace
            if (DEBUG_LOADERS) {
                Log.d(TAG, "loadAndBindWorkspace mWorkspaceLoaded=" + mWorkspaceLoaded);
            }

            
            if (!mWorkspaceLoaded) {
            	printAppNumInDB("before loadworkspace");
                loadWorkspace();
                //TODO check 
                printAppNumInDB("after loadworkspace");
                synchronized (LoaderTask.this) {
                    if (mStopped) {
                        return;
                    }
                    mWorkspaceLoaded = true;
                }
            }
			else {
				LauncherApplication.logVulcan.print("cancel load because flag mWorkspaceLoaded equals true");
			}
            
            if(reLoadWorkspaceforClassify){
						// TODO Auto-generated method stub
						loadClassifyWorkspace();
            	reLoadWorkspaceforClassify = false;
		    }
           
            // Bind the workspace
            bindWorkspace(-1);
        }

        private void waitForIdle() {
            // Wait until the either we're stopped or the other threads are done.
            // This way we don't start loading all apps until the workspace has settled
            // down.
            synchronized (LoaderTask.this) {
                final long workspaceWaitTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

                mHandler.postIdle(new Runnable() {
                        public void run() {
                            synchronized (LoaderTask.this) {
                                mLoadAndBindStepFinished = true;
                                if (DEBUG_LOADERS) {
                                    Log.d(TAG, "done with previous binding step");
                                }
                                LoaderTask.this.notify();
                            }
                        }
                    });

                while (!mStopped && !mLoadAndBindStepFinished) {
                    try {
                        this.wait();
                    } catch (InterruptedException ex) {
                        // Ignore
                    }
                }
                if (DEBUG_LOADERS) {
                    Log.d(TAG, "waited "
                            + (SystemClock.uptimeMillis()-workspaceWaitTime)
                            + "ms for previous step to finish binding");
                }
            }
        }

        void runBindSynchronousPage(int synchronousBindPage) {
            if (synchronousBindPage < 0) {
                // Ensure that we have a valid page index to load synchronously
                throw new RuntimeException("Should not call runBindSynchronousPage() without " +
                        "valid page index");
            }
            if (!mAllAppsLoaded || !mWorkspaceLoaded) {
                // Ensure that we don't try and bind a specified page when the pages have not been
                // loaded already (we should load everything asynchronously in that case)
                throw new RuntimeException("Expecting AllApps and Workspace to be loaded");
            }
            synchronized (mLock) {
                if (mIsLoaderTaskRunning) {
                    // Ensure that we are never running the background loading at this point since
                    // we also touch the background collections
                    throw new RuntimeException("Error! Background loading is already running");
                }
            }

            // XXX: Throw an exception if we are already loading (since we touch the worker thread
            //      data structures, we can't allow any other thread to touch that data, but because
            //      this call is synchronous, we can get away with not locking).

            // The LauncherModel is static in the LauncherApplication and mHandler may have queued
            // operations from the previous activity.  We need to ensure that all queued operations
            // are executed before any synchronous binding work is done.
            mHandler.flush();

            // Divide the set of loaded items into those that we are binding synchronously, and
            // everything else that is to be bound normally (asynchronously).
            bindWorkspace(synchronousBindPage);
            // XXX: For now, continue posting the binding of AllApps as there are other issues that
            //      arise from that.
            onlyBindAllApps();
        }

        public void run() {
            synchronized (mLock) {
                mIsLoaderTaskRunning = true;
            }
            
            // Aurora <jialf> <2013-09-19> modify for loading data begin
            // we just load and bind workspace , so note the code
            /*// Optimize for end-user experience: if the Launcher is up and // running with the
            // All Apps interface in the foreground, load All Apps first. Otherwise, load the
            // workspace first (default).
            final Callbacks cbk = mCallbacks.get();
            final boolean loadWorkspaceFirst = cbk != null ? (!cbk.isAllAppsVisible()) : true;*/

            keep_running: {
                // Elevate priority when Home launches for the first time to avoid
                // starving at boot time. Staring at a blank home is not cool.
                synchronized (mLock) {
                    if (DEBUG_LOADERS) Log.d(TAG, "Setting thread priority to " +
                            (mIsLaunching ? "DEFAULT" : "BACKGROUND"));
                    android.os.Process.setThreadPriority(mIsLaunching
                            ? Process.THREAD_PRIORITY_DEFAULT : Process.THREAD_PRIORITY_BACKGROUND);
                }
                if (mStopped) {
                    break keep_running;
                }
                loadAndBindWorkspace();
                /*if (loadWorkspaceFirst) {
                    if (DEBUG_LOADERS) Log.d(TAG, "step 1: loading workspace");
                    loadAndBindWorkspace();
                } else {
                    if (DEBUG_LOADERS) Log.d(TAG, "step 1: special: loading all apps");
                    loadAndBindAllApps();
                }*/

                if (mStopped) {
                    break keep_running;
                }

                // Whew! Hard work done.  Slow us down, and wait until the UI thread has
                // settled down.
                synchronized (mLock) {
                    if (mIsLaunching) {
                        if (DEBUG_LOADERS) Log.d(TAG, "Setting thread priority to BACKGROUND");
                        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    }
                }
                waitForIdle();

                /*// second step
                if (loadWorkspaceFirst) {
                    if (DEBUG_LOADERS) Log.d(TAG, "step 2: loading all apps");
                    loadAndBindAllApps();
                } else {
                    if (DEBUG_LOADERS) Log.d(TAG, "step 2: special: loading workspace");
                    loadAndBindWorkspace();
                }*/
                // Aurora <jialf> <2013-09-19> modify for loading data end

                // Restore the default thread priority after we are done loading items
                synchronized (mLock) {
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                }
            }


            // Update the saved icons if necessary
            /*if (DEBUG_LOADERS) Log.d(TAG, "Comparing loaded icons to database icons");
            synchronized (sBgLock) {
                for (Object key : sBgDbIconCache.keySet()) {
                    updateSavedIcon(mContext, (ShortcutInfo) key, sBgDbIconCache.get(key));
                }
                sBgDbIconCache.clear();
            }*/

            // Clear out this reference, otherwise we end up holding it until all of the
            // callback runnables are done.
            mContext = null;

            synchronized (mLock) {
                // If we are still the last one to be scheduled, remove ourselves.
                if (mLoaderTask == this) {
                    mLoaderTask = null;
                }
                mIsLoaderTaskRunning = false;
            }
        }

        public void stopLocked() {
            synchronized (LoaderTask.this) {
                mStopped = true;
                this.notify();
            }
        }

        /**
         * Gets the callbacks object.  If we've been stopped, or if the launcher object
         * has somehow been garbage collected, return null instead.  Pass in the Callbacks
         * object that was around when the deferred message was scheduled, and if there's
         * a new Callbacks object around then also return null.  This will save us from
         * calling onto it with data that will be ignored.
         */
        Callbacks tryGetCallbacks(Callbacks oldCallbacks) {
            synchronized (mLock) {
                if (mStopped) {
                    return null;
                }

                if (mCallbacks == null) {
                    return null;
                }

                final Callbacks callbacks = mCallbacks.get();
                if (callbacks != oldCallbacks) {
                    return null;
                }
                if (callbacks == null) {
                    Log.w(TAG, "no mCallbacks");
                    return null;
                }

                return callbacks;
            }
        }

        // check & update map of what's occupied; used to discard overlapping/invalid items
        private boolean checkItemPlacement(ItemInfo occupied[][][], ItemInfo item) {
            int containerIndex = item.screen;
            if (item.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                // Return early if we detect that an item is under the hotseat button
                // Aurora <jialf> <2013-09-10> modify for dock data begin
                if (mCallbacks == null /*|| mCallbacks.get().isAllAppsButtonRank(item.screen)*/) {
                    return false;
                }
                // Aurora <jialf> <2013-09-10> modify for dock data end

                // Aurora <jialf> <2013-09-17> modify for loading data begin
                // We use the last index to refer to the hotseat and the screen as the rank, so
                // test and update the occupied state accordingly
				if (occupied[occupied.length - 1][item.screen][0] != null) {
                    Log.e(TAG, "Error loading shortcut into hotseat " + item
                        + " into position (" + item.screen + ":" + item.cellX + "," + item.cellY
                        + ") occupied by " + occupied[Launcher.SCREEN_COUNT][item.screen][0]);
                    return false;
                } else {
					occupied[occupied.length - 1][item.screen][0] = item;
                    return true;
                }
                // Aurora <jialf> <2013-09-17> modify for loading data end
            } else if (item.container != LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                // Skip further checking if it is not the hotseat or workspace container
                return true;
            }

            // Check if any workspace icons overlap with each other
            for (int x = item.cellX; x < (item.cellX+item.spanX); x++) {
                for (int y = item.cellY; y < (item.cellY+item.spanY); y++) {
                    if (occupied[containerIndex][x][y] != null) {
                        Log.e(TAG, "Error loading shortcut " + item
                            + " into cell (" + containerIndex + "-" + item.screen + ":"
                            + x + "," + y
                            + ") occupied by "
                            + occupied[containerIndex][x][y]);
                        return false;
                    }
                }
            }
            for (int x = item.cellX; x < (item.cellX+item.spanX); x++) {
                for (int y = item.cellY; y < (item.cellY+item.spanY); y++) {
                    occupied[containerIndex][x][y] = item;
                }
            }

            return true;
        }

        boolean mCreateNewDb = false;
        
        private void loadWorkspace() {
            final long t = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
            final Context context = mContext;
            final ContentResolver contentResolver = context.getContentResolver();
            final PackageManager manager = context.getPackageManager();
            final AppWidgetManager widgets = AppWidgetManager.getInstance(context);
            final boolean isSafeMode = manager.isSafeMode();
            mOpenAllMoveupWidgets = false;
           
            Launcher callback =(Launcher) getCallback();
    		if( callback != null){
           	 mAppWidgetHost = callback.getAppWidgetHost();
           	 sortByPkg = callback.getPkgProviderMap();
    		}else{
    			Log.v("mywidget", "callback=null");
    	       	mAppWidgetHost = new AppWidgetHost(mApp.getApplicationContext(), Launcher.APPWIDGET_HOST_ID);
    	       }
    		appWidgetManager = AppWidgetManager.getInstance(mApp.getApplicationContext());
    		
            // Make sure the default workspace is loaded, if needed
            mApp.getLauncherProvider().loadDefaultFavoritesIfNecessary(0);

            // Aurora <jialf> <2013-09-09> add for loading data begin
            String spKey = LauncherApplication.getSharedPreferencesKey();
            SharedPreferences sp = mApp.getSharedPreferences(spKey, Context.MODE_PRIVATE);
            
            if(!sp.getBoolean(FIRST_OPEN_ALL_MOVEUP_WIDGETS, false)){
            	SharedPreferences.Editor editor = sp.edit();
            	editor.putBoolean(FIRST_OPEN_ALL_MOVEUP_WIDGETS, true);
            	editor.commit();
            	mOpenAllMoveupWidgets = true;
            }
            
            if (sp.getBoolean(LauncherProvider.FIRST_QUERY_ALL_APPS, false)) {
            	mCreateNewDb = true;
                int maxScreen = LauncherProvider.mMaxScreen;
                int maxCellX = LauncherProvider.mMaxCellX;
                int maxCellY = LauncherProvider.mMaxCellY;
            	Log.i(TAG, "maxScreen = " + maxScreen + ",maxCellX = "
						+ maxCellX + ",maxCellY = " + maxCellY + ", defaultPage = " + mWorkspace.getmDefaultPage());
            	
				long gnStartTime = System.currentTimeMillis();
				
				// get all application info
				List<ResolveInfo> allApps = getAllApps(mLabelCache);
				int size = 0;
				if (allApps != null && (size = allApps.size()) > 0) {
					addAllAppsIntoDB(allApps, maxScreen, maxCellX, maxCellY,
							size, mLabelCache);
				}
				long gnEndTime = System.currentTimeMillis();
				Log.i(TAG, "laod all apps need "
						+ (float) (gnEndTime - gnStartTime) / 1000 + " s.");
				
				SharedPreferences.Editor editor = sp.edit();
	            editor.remove(LauncherProvider.FIRST_QUERY_ALL_APPS);
	            editor.commit();
            }
            // Aurora <jialf> <2013-09-09> add for loading data end
            
            synchronized (sBgLock) {
                sBgWorkspaceItems.clear();
                sBgAppWidgets.clear();
                sBgFolders.clear();
                sBgItemsIdMap.clear();
                sBgDbIconCache.clear();

                final ArrayList<Long> itemsToRemove = new ArrayList<Long>();//for widgets

                LauncherApplication.logVulcan.print("140728-p3-loadWorkspace: will query db items right now, now is " + System.currentTimeMillis()%60000);
                final Cursor c = contentResolver.query(
                        LauncherSettings.Favorites.CONTENT_URI, null, null, null, null);

                // Aurora <jialf> <2013-09-17> modify for loading data begin
				int maxScreen = 0;
				int screenInd = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
				// Aurora <jialf> <2013-10-21> modify for fix bug #22 && # 140 begin
				String appIntent = null;
				List<String> dbApps = new ArrayList<String>();
				int intentInd = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);
				while(c.moveToNext()) {
					if(c.getInt(screenInd) > maxScreen)
						maxScreen = c.getInt(screenInd);
				}
				// Aurora <jialf> <2013-10-21> modify for fix bug #22 && # 140 end
				if (maxScreen >= Launcher.SCREEN_COUNT) {
					maxScreen += 1;
				} else {
					maxScreen = Launcher.SCREEN_COUNT;
				}
				c.moveToPosition(-1);
				// Aurora <jialf> <2013-10-21> modify for fix bug #22 && # 140 begin
				List<Long> removeApps = new ArrayList<Long>();//for folders
				List<Long> removeIds = new ArrayList<Long>();//for application and shortcut
				// Aurora <jialf> <2013-10-21> modify for fix bug #22 && # 140 end

                // +1 for the hotseat (it can be larger than the workspace)
                // Load workspace in reverse order to ensure that latest items are loaded first (and
                // before any earlier duplicates)
                final ItemInfo occupied[][][] =
                        new ItemInfo[maxScreen + 1][mCellCountX + 1][mCellCountY + 1];
	            // Aurora <jialf> <2013-09-17> modify for loading data end

                // Aurora <jialf> <2014-01-16> modify for fix bug #1738 begin
                int dbcount = c.getCount();
				int i = 0;
                // Aurora <jialf> <2014-01-16> modify for fix bug #1738 end
				
				mRemovedWidgetPackage.clear();
				
				//ht 2014-09-12 
				List<Long> overlapIdList = new ArrayList<Long>();
				
                try {
                    final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
                    final int intentIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.INTENT);
                    final int titleIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.TITLE);
                    final int iconTypeIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ICON_TYPE);
                    final int iconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON);
                    final int iconPackageIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ICON_PACKAGE);
                    final int iconResourceIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ICON_RESOURCE);
                    final int containerIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.CONTAINER);
                    final int itemTypeIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ITEM_TYPE);
                    final int appWidgetIdIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.APPWIDGET_ID);
                    final int screenIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.SCREEN);
                    final int cellXIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.CELLX);
                    final int cellYIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.CELLY);
                    final int spanXIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.SPANX);
                    final int spanYIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.SPANY);
                    
                    final int itemFlags = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.FLAGS);
                    
                    //iconcat,vulcan added it in 2014-7-11
                    final int firstInstallTimeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.FIRST_INSTALL_TIME);
                    
                    final int enableWidgetsIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ENABLE_WIDGETS);
                    
                    ShortcutInfo info;
                    String intentDescription;
                    LauncherAppWidgetInfo appWidgetInfo;
                    int container;
                    long id;
                    Intent intent;

                	// Aurora <jialf> <2013-10-08> add for Dock data begin
					mHotseatChildCount = 0;
					// Aurora <jialf> <2013-10-08> add for Dock data end

                    while (!mStopped && c.moveToNext()) {
                        try {
                            int itemType = c.getInt(itemTypeIndex);
                            
                            
                            String titleInDB = c.getString(titleIndex);
                            int cellXInDB = c.getInt(cellXIndex);
                            int cellYInDB = c.getInt(cellYIndex);
                            int screenInDB = c.getInt(screenIndex);
                            int containerInDB = c.getInt(containerIndex);
                            int itemTypeInDB = c.getInt(itemTypeIndex);
                            
                            LauncherApplication.logVulcan.print(String.format("loadWorkspace: item(%d) = %s,(%d,%d,%d),container = %d, itemType = %d",
                            				i,titleInDB,screenInDB,cellXInDB,cellYInDB,containerInDB,
                            				itemTypeInDB));

                            switch (itemType) {
                            case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                            case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
								
                                intentDescription = c.getString(intentIndex);
                                try {
                                    intent = Intent.parseUri(intentDescription, 0);
                                } catch (URISyntaxException e) {
                                	LauncherApplication.logVulcan.print("intent parse error:intentDescription = " + intentDescription);
                                    continue;
                                }

                                if (itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
                                    if (intentDescription != null && !intentDescription.equals("")) {
                						dbApps.add(intentDescription);
                					}

									/*vulcan changed these code in 2014-5-21
									getShortcutInfo is replaced by makeApplication!*/
									info = makeApplicationInfo(intentDescription,c,mLabelCache);// iht 完成图片Icon的创建
                                    //info = getShortcutInfo(manager, intent, context, c, iconIndex,
                                    //        titleIndex, mLabelCache);
                                } else {
                                	info = getAuroraShortcutInfo(intent, c, context, iconTypeIndex,
                                            iconPackageIndex, iconResourceIndex, iconIndex,
                                            titleIndex);

                                    // App shortcuts that used to be automatically added to Launcher
                                    // didn't always have the correct intent flags set, so do that
                                    // here
                                    if (intent.getAction() != null &&
                                        intent.getCategories() != null &&
                                        intent.getAction().equals(Intent.ACTION_MAIN) &&
                                        intent.getCategories().contains(Intent.CATEGORY_LAUNCHER)) {
                                        intent.addFlags(
                                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                                    }
                                }

                                if (info != null) {
                                    info.intent = intent;
                                    info.id = c.getLong(idIndex);
                                    container = c.getInt(containerIndex);
                                    info.container = container;
                                    info.screen = c.getInt(screenIndex);
                                    info.cellX = c.getInt(cellXIndex);
                                    info.cellY = c.getInt(cellYIndex);
                                    
                                    info.flags = c.getInt(itemFlags);
                                    
                                    //iconcat,vulcan added it in 2014-7-11
                                    info.firstInstallTime = c.getLong(firstInstallTimeIndex);
									if (info.title != null) {
										Log.d("vulcan-db",
												String.format(
														"loadWorkspace: loading firstInstallTime(%d) to mem,app is %s,type is %d,time is %s",
														info.firstInstallTime,
														info.title.toString(),
														info.itemType,
														LogWriter.LongTimeToFormatTime(info.firstInstallTime)));
									}
									info.appWidgetId = c.getInt(appWidgetIdIndex);
									info.enableWidgets = c.getInt(enableWidgetsIndex);
									
									if(mOpenAllMoveupWidgets){
										if(info.getPackageName()!=null && sortByPkg.containsKey(info.getPackageName())){
											if(info.appWidgetId == -1){
											 AppWidgetProviderInfo providerInfo = sortByPkg.get(info.getPackageName()).get(0).providerInfo;
							                 info.appWidgetId = mAppWidgetHost.allocateAppWidgetId();
							                 appWidgetManager.bindAppWidgetIdIfAllowed(info.appWidgetId, providerInfo.provider);
						            		 info.enableWidgets = 1;
						            		 addAuroraHostView(info);   
						            		 ContentValues values = new ContentValues();
						            	     values.put(LauncherSettings.Favorites.APPWIDGET_ID, info.appWidgetId);
						            	     values.put(LauncherSettings.Favorites.ENABLE_WIDGETS, info.enableWidgets);
						            	     contentResolver.update(LauncherSettings.Favorites.getContentUri(
	                                                info.id, false), values, null, null);	
						            	     Log.v("mywidget", "mOpenAllMoveupWidgets allocateAppWidgetId---: "+info.appWidgetId+" packageName"+info.getPackageName());
											}/*else if (info.hostView == null && info.enableWidgets == 1){
												addAuroraHostView(info);
												Log.v("mywidget", "mOpenAllMoveupWidgets info.hostView == null,reCreate");
											}*/
										}
							        	
							        }else{
									
									
									if( info.enableWidgets == 1 ){
										
									 if( info.appWidgetId != -1 ){
										if( info.hostView == null){
											addAuroraHostView(info);
										}
									 }
									}else{										
										/*if( info.appWidgetId == -1 && info.getPackageName()!=null && sortByPkg.containsKey(info.getPackageName())){
										
										AppWidgetProviderInfo providerInfo = sortByPkg.get(info.getPackageName()).get(0).providerInfo;
					                	info.appWidgetId = mAppWidgetHost.allocateAppWidgetId();
					            		appWidgetManager.bindAppWidgetIdIfAllowed(info.appWidgetId, providerInfo.provider);
					            		ContentValues values = new ContentValues();
					            	    values.put(LauncherSettings.Favorites.APPWIDGET_ID, info.appWidgetId);
					            	    contentResolver.update(LauncherSettings.Favorites.getContentUri(
                                                info.id, false), values, null, null);	
					            	    Log.v("mywidget", "allocateAppWidgetId---: "+info.appWidgetId+" packageName"+info.getPackageName());
									}*/
										info.hostView = null;
									}
									
							        }	
									
									
									
								    // check & update map of what's occupied
                                    if (!checkItemPlacement(occupied, info)) {
                                    	overlapIdList.add(info.id);
                                    	dbApps.remove(intentDescription);
										LauncherApplication.logVulcan.print("occupied error:intentDescription = " + intentDescription);
                                        break;
                                    }

                                    switch (container) {
                                    case LauncherSettings.Favorites.CONTAINER_DESKTOP:
                                    case LauncherSettings.Favorites.CONTAINER_HOTSEAT:
                                        sBgWorkspaceItems.add(info);
										if (info.deleteFlag) {
											removeIds.add(info.id);
										}
                                        break;
                                    default:
                                        // Item is in a user folder
                                        FolderInfo folderInfo =
                                                findOrMakeFolder(sBgFolders, container);
                                        folderInfo.add(info);
    									if (info.deleteFlag) {
											removeIds.add(info.id);
											if (!removeApps.contains(container))
												removeApps.add((long) container);
    									}
                                        break;
                                    }
                                    
                                	// Aurora <jialf> <2013-10-08> add for Dock data begin
									if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT)
										mHotseatChildCount++;
									// Aurora <jialf> <2013-10-08> add for Dock data end
									
                                    sBgItemsIdMap.put(info.id, info);
                                    if(info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) {
                                    	Log.d("vulcan-setup","loadWorkspace: shortcut was put in sBgItemsIdMap " + info.title);
                                    }

                                    // now that we've loaded everthing re-save it with the
                                    // icon in case it disappears somehow.
                                    //queueIconToBeChecked(sBgDbIconCache, info, c, iconIndex);
                                } else {

								
                                    // Failed to load the shortcut, probably because the
                                    // activity manager couldn't resolve it (maybe the app
                                    // was uninstalled), or the db row was somehow screwed up.
                                    // Delete it.

                                    id = c.getLong(idIndex);
                                    Log.e(TAG, "Error loading shortcut " + id + ", removing it");
									LauncherApplication.logVulcan.print("vulcan,to delete an app in db,id = "+id);
                                    contentResolver.delete(LauncherSettings.Favorites.getContentUri(
                                                id, false), null, null);
                                }
                                i++;
                                break;

                            case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                                id = c.getLong(idIndex);
                                FolderInfo folderInfo = findOrMakeFolder(sBgFolders, id);

                                folderInfo.title = c.getString(titleIndex);
                                folderInfo.id = id;
                                container = c.getInt(containerIndex);
                                folderInfo.container = container;
                                folderInfo.screen = c.getInt(screenIndex);
                                folderInfo.cellX = c.getInt(cellXIndex);
                                folderInfo.cellY = c.getInt(cellYIndex);
                                
                                //iconcat,vulcan added it in 2014-7-11
                                folderInfo.firstInstallTime = c.getLong(firstInstallTimeIndex);
                                if(folderInfo.title != null) {
									Log.d("vulcan-db",
											String.format(
													"loadWorkspace: loading firstInstallTime(%d) to mem,folder is %s",
													folderInfo.firstInstallTime,
													folderInfo.title.toString()));
								}

                                // check & update map of what's occupied
                                if (!checkItemPlacement(occupied, folderInfo)) {
                                	overlapIdList.add(folderInfo.id);
                                	LauncherApplication.logVulcan.print("folder occupied error:intentDescription = " + folderInfo.title + ",id = " + id);
                                    break;
                                }
                                switch (container) {
                                    case LauncherSettings.Favorites.CONTAINER_DESKTOP:
                                    case LauncherSettings.Favorites.CONTAINER_HOTSEAT:
                                        sBgWorkspaceItems.add(folderInfo);
                                        break;
                                }

                            	// Aurora <jialf> <2013-10-08> add for Dock data begin
                                if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT)
									mHotseatChildCount++;
                            	// Aurora <jialf> <2013-10-08> add for Dock data end

                                sBgItemsIdMap.put(folderInfo.id, folderInfo);
                                sBgFolders.put(folderInfo.id, folderInfo);
                                i++;
                                break;

                            case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                                // Read all Launcher-specific widget details
                                int appWidgetId = c.getInt(appWidgetIdIndex);
                                id = c.getLong(idIndex);

                                final AppWidgetProviderInfo provider =
                                        widgets.getAppWidgetInfo(appWidgetId);

                                if (!isSafeMode && (provider == null || provider.provider == null ||
                                        provider.provider.getPackageName() == null)) {
                                    String log = "Deleting widget that isn't installed anymore: id="
                                        + id + " appWidgetId=" + appWidgetId;
                                    Log.e(TAG, log);
                                    Launcher.sDumpLogs.add(log);

									LauncherApplication.logVulcan.print("vulcan,to delete a widget,id = " + id);

									if(provider == null) {
										LauncherApplication.logVulcan.print("provider == null");
									} else if(provider.provider == null) {
										LauncherApplication.logVulcan.print("provider.provider == null");
									} else if (provider.provider.getPackageName() == null) {
										LauncherApplication.logVulcan.print("provider.provider.getPackageName()");
									}

                                    itemsToRemove.add(id);
                                } else {
                                    appWidgetInfo = new LauncherAppWidgetInfo(appWidgetId,
                                            provider.provider);
                                    appWidgetInfo.id = id;
                                    appWidgetInfo.screen = c.getInt(screenIndex);
                                    appWidgetInfo.cellX = c.getInt(cellXIndex);
                                    appWidgetInfo.cellY = c.getInt(cellYIndex);
                                    appWidgetInfo.spanX = c.getInt(spanXIndex);
                                    appWidgetInfo.spanY = c.getInt(spanYIndex);
                                    
                                    //iconcat,vulcan added it in 2014-7-11
                                    appWidgetInfo.firstInstallTime = c.getLong(firstInstallTimeIndex);
									if (appWidgetInfo.title != null) {
										Log.d("vulcan-db",
												String.format(
														"loadWorkspace: loading firstInstallTime(%d) to mem,appWidgetInfo is %s",
														appWidgetInfo.firstInstallTime,
														appWidgetInfo.title.toString()));
									}
                                    
                                    int[] minSpan = Launcher.getMinSpanForWidget(context, provider);
                                    appWidgetInfo.minSpanX = minSpan[0];
                                    appWidgetInfo.minSpanY = minSpan[1];

                                    container = c.getInt(containerIndex);
                                    if (container != LauncherSettings.Favorites.CONTAINER_DESKTOP &&
                                        container != LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                                        Log.e(TAG, "Widget found where container != " +
                                            "CONTAINER_DESKTOP nor CONTAINER_HOTSEAT - ignoring!");
                                        continue;
                                    }
                                    appWidgetInfo.container = c.getInt(containerIndex);

                                    // check & update map of what's occupied
                                    if (!checkItemPlacement(occupied, appWidgetInfo)) {
                                    	overlapIdList.add(appWidgetInfo.id);
                                    	break;
                                    }
                                    sBgItemsIdMap.put(appWidgetInfo.id, appWidgetInfo);
                                    sBgAppWidgets.add(appWidgetInfo);
                                }
                                i++;
                                break;
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Desktop items loading interrupted:", e);
                        }
                    }
                } finally {
                    c.close();
                }
                String defaultLanguage = sp.getString(LauncherProvider.DEFAULT_LANGUAGE, "");
                Log.e("linp", "############sBgItemsIdMap.size()="+sBgItemsIdMap.size()+";"+"Default Language="+sp.getString(LauncherProvider.DEFAULT_LANGUAGE, "")+";"+"CurrentLanguage="+currentLanguage);
                if(null!=defaultLanguage){
                	if(!defaultLanguage.equals(currentLanguage)){
                		//TODO start update column about title,fullpinyin,simplepinyin
                		 Log.e("linp", "#####Different Language and will rewrite!");
                         ContentValues[] cv = getUpdateContentArray(sBgItemsIdMap);
                         Uri[] url = getUpdateUriArray(sBgItemsIdMap);

                         mApp.getLauncherProvider().bulkUpdate(url, cv);
                         mApp.getLauncherProvider().bulkUpdateCategoryTable();
                         writeDefaultLanguage(sp,currentLanguage);
                   }else
                	   Log.e("linp", "Choose same Language and will do nothing\n");
                }else{
                	Log.e("linp", "###getDefaultLanguage return empty noted that it dosen't write to sharepreference");
                }
                LauncherApplication.logVulcan.print("140728-p3-loadWorkspace: already completed reading, now is " + System.currentTimeMillis()%60000);
                mAppsCount = dbApps.size();
				boolean queryComplete = (i == dbcount);
				LauncherApplication.logVulcan.print("check mCreateNewDb,\t mSdcardMounted = " + mSdcardMounted + "\t queryComplete = " + queryComplete  + "\t mCreateNewDb = " + mCreateNewDb);
				if(i != dbcount) {
					LauncherApplication.logVulcan.print("i=" + i + ",dbcount=" + dbcount);
				}
				// Aurora <jialf> <2013-10-21> add for fix bug #22 && # 140 begin
				if (!mCreateNewDb) {
					
					/**
					 * if the icon overlap; the lastest one will be deleted in DB; 
					 * @author ht 2014-09-12
					 * */
					if(!overlapIdList.isEmpty()){
						for(Long id : overlapIdList){
				        	contentResolver.delete(LauncherSettings.Favorites.getContentUri(
			                        id, false), null, null);
				        	
				        	LauncherApplication.logVulcan.print("(HT:)delete the database record which id = "+id+", and the position has been"
				        			+ "overlaped ");
						}
						queryComplete = true;
					}
					
					/*vulcan changed these code in 2014-5-23
					Accordding to new requirement, even if sd card is offline we load new app from internal storage!!!*/
	                //if(mSdcardMounted && queryComplete) {
					if(queryComplete) {

						Log.d("vulcan-startloader","call addAuroraNewApps");

						LauncherApplication.logVulcan.print("to call filterAuroraNewItems,dbApps.size = " + dbApps.size());
						List<String> newApps = filterAuroraNewItems(dbApps);
						LauncherApplication.logVulcan.print("to call addAuroraNewApps,newApps.size = " + newApps.size());
		                addAuroraNewApps(newApps, manager, mLabelCache);
						if (mSdcardApps != null) {
							mSdcardApps = null;
						}
	                }
					else {
						LauncherApplication.logVulcan.print("no loading new app because mSdcardMounted&queryComplete is false");
					}
					
					//Anyway, we have to delete the garbage applications and folders
	                boolean appUpdateHotseat = updateAuroraRemoveApps(removeIds, contentResolver);
	                boolean folderUpdateHotseat = updateAuroraRemoveFolderApps(removeApps);
					boolean needUpdateHotseat = appUpdateHotseat || folderUpdateHotseat;
	                updateHotseatApps(needUpdateHotseat);
					
                }
				else {
					LauncherApplication.logVulcan.print("no loading new app because mCreateNewDb is true");
				}
				// Aurora <jialf> <2013-10-21> add for fix bug #22 && # 140 end
                
                if (itemsToRemove.size() > 0) {
                    ContentProviderClient client = contentResolver.acquireContentProviderClient(
                                    LauncherSettings.Favorites.CONTENT_URI);
                    // Remove dead items
                    for (long id : itemsToRemove) {

						LauncherApplication.logVulcan.print("vulcan,to delete a element,id = " + id);
                        if (DEBUG_LOADERS) {
                            Log.d(TAG, "Removed id = " + id);
                        }
                        // Don't notify content observers
                        try {
                            client.delete(LauncherSettings.Favorites.getContentUri(id, false),
                                    null, null);
                        } catch (RemoteException e) {
                            Log.w(TAG, "Could not remove id = " + id);
                        }
                    }
                }

                if (DEBUG_LOADERS) {
                    Log.d(TAG, "loaded workspace in " + (SystemClock.uptimeMillis()-t) + "ms");
                    Log.d(TAG, "workspace layout: ");
                    for (int y = 0; y < mCellCountY; y++) {
                        String line = "";
                        for (int s = 0; s < Launcher.SCREEN_COUNT; s++) {
                            if (s > 0) {
                                line += " | ";
                            }
                            for (int x = 0; x < mCellCountX; x++) {
                                line += ((occupied[s][x][y] != null) ? "#" : ".");
                            }
                        }
                        Log.d(TAG, "[ " + line + " ]");
                    }
                }
            }
        }
/*
		private void dealAppOnLoading(String intentDescription,int itemType,List<String> dbApps,
					Cursor c) {
			//String intentDescription = c.getString(intentIndex);
			ShortcutInfo info;
			Intent intent;
			final Context context = mContext;
			final PackageManager manager = mContext.getPackageManager();
			final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
			final int iconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON);
			

			try {
				intent = Intent.parseUri(intentDescription, 0);
			} catch (URISyntaxException e) {
				continue;
			}

			if (itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
				if (intentDescription != null && !intentDescription.equals("")) {
					dbApps.add(intentDescription);
				}
				info = getShortcutInfo(manager, intent, context, c, iconIndex,
						titleIndex, mLabelCache);
				
			} else {
				info = getAuroraShortcutInfo(intent, c, context, iconTypeIndex,
						iconPackageIndex, iconResourceIndex, iconIndex,
						titleIndex);
			
				// App shortcuts that used to be automatically added to Launcher
				// didn't always have the correct intent flags set, so do that
				// here
				if (intent.getAction() != null &&
					intent.getCategories() != null &&
					intent.getAction().equals(Intent.ACTION_MAIN) &&
					intent.getCategories().contains(Intent.CATEGORY_LAUNCHER)) {
					intent.addFlags(
						Intent.FLAG_ACTIVITY_NEW_TASK |
						Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				}
			}
			
			if (info != null) {
				info.intent = intent;
				info.id = c.getLong(idIndex);
				container = c.getInt(containerIndex);
				info.container = container;
				info.screen = c.getInt(screenIndex);
				info.cellX = c.getInt(cellXIndex);
				info.cellY = c.getInt(cellYIndex);
				
				info.flags = c.getInt(itemFlags);
			
				// check & update map of what's occupied
				if (!checkItemPlacement(occupied, info)) {
					break;
				}
			
				switch (container) {
				case LauncherSettings.Favorites.CONTAINER_DESKTOP:
				case LauncherSettings.Favorites.CONTAINER_HOTSEAT:
					sBgWorkspaceItems.add(info);
					if (info.deleteFlag) {
						removeIds.add(info.id);
					}
					break;
				default:
					// Item is in a user folder
					FolderInfo folderInfo =
							findOrMakeFolder(sBgFolders, container);
					folderInfo.add(info);
					if (info.deleteFlag) {
						removeIds.add(info.id);
						if (!removeApps.contains(container))
							removeApps.add((long) container);
					}
					break;
				}
				
				// Aurora <jialf> <2013-10-08> add for Dock data begin
				if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT)
					mHotseatChildCount++;
				// Aurora <jialf> <2013-10-08> add for Dock data end
				
				sBgItemsIdMap.put(info.id, info);
			
				// now that we've loaded everthing re-save it with the
				// icon in case it disappears somehow.
				queueIconToBeChecked(sBgDbIconCache, info, c, iconIndex);
			} else {
				// Failed to load the shortcut, probably because the
				// activity manager couldn't resolve it (maybe the app
				// was uninstalled), or the db row was somehow screwed up.
				// Delete it.
				id = c.getLong(idIndex);
				Log.e(TAG, "Error loading shortcut " + id + ", removing it");
				LauncherApplication.logVulcan.print("vulcan,to delete an app in db,id = "+id);
				contentResolver.delete(LauncherSettings.Favorites.getContentUri(
							id, false), null, null);
			}
			i++;
			break;
			

		}
*/
		private void printAppNumInDB(String tag) {
			final Context context = mContext;
			final ContentResolver contentResolver = context.getContentResolver();
            final Cursor c = contentResolver.query(
                    LauncherSettings.Favorites.CONTENT_URI, null, null, null, null);
			final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
			final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);


            
            final int total = c.getCount();
            int appNum = 0;
			int shortcutNum = 0;
            int widgetNum = 0;
            int folderNum = 0;
            int otherTypeNum = 0;
            int idx = 0;
            
			while(c.moveToNext()) {
				int itemType = c.getInt(itemTypeIndex);
				if(LauncherSettings.Favorites.ITEM_TYPE_APPLICATION == itemType) {
					appNum ++;
				} else if(LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT == itemType){
					shortcutNum ++;
				} else if(LauncherSettings.Favorites.ITEM_TYPE_FOLDER == itemType){
					folderNum ++;
				} else if(LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET == itemType){
					widgetNum ++;
				} else {
					otherTypeNum ++;
				}
				idx ++;
			}
			c.close();
			LauncherApplication.logVulcan.print(tag);
			LauncherApplication.logVulcan.print("a=" + appNum + ",s=" + shortcutNum + ",f=" + folderNum + ",w=" + widgetNum + ",o=" + otherTypeNum);
			if(idx != total) {
				LauncherApplication.logVulcan.print("idx != total in db");
			}            
			return;
		}

		// Aurora <jialf> <2013-10-21> add for fix bug #22 && # 140 begin
        private List<String> filterAuroraNewItems(final List<String> dbApps) {
        	final List<String> allApps = getAuroraAllApps();
        	List<String> newApps = new ArrayList<String>();
			for (String intent : allApps) {
				if (!dbApps.contains(intent) && filterAuroraNewApps(intent)) {
					newApps.add(intent);
				}
			}
			return newApps;
        }
        
		private boolean filterAuroraNewApps(String appIntent) {
			Intent intent = null;
			try{
				intent = Intent.parseUri(appIntent, 0);
				ComponentName oldComponent = intent.getComponent();
				Intent newIntent = new Intent(intent.getAction(), null);
				newIntent.addCategory(Intent.CATEGORY_LAUNCHER);
				newIntent.setPackage(oldComponent.getPackageName());
				final PackageManager manager = mContext.getPackageManager();
				List<ResolveInfo> infos = manager.queryIntentActivities(newIntent, 0);
				if (infos != null && infos.size() > 0)
					return true;
			} catch (URISyntaxException e) {
                Log.i(TAG, "Aurora parse new apps exception ...");
            }
			return false;
		}
		// Aurora <jialf> <2013-10-21> add for fix bug #22 && # 140 end

        /** Filters the set of items who are directly or indirectly (via another container) on the
         * specified screen. */
        private void filterCurrentWorkspaceItems(int currentScreen,
                ArrayList<ItemInfo> allWorkspaceItems,
                ArrayList<ItemInfo> currentScreenItems,
                ArrayList<ItemInfo> otherScreenItems) {
            // Purge any null ItemInfos
            Iterator<ItemInfo> iter = allWorkspaceItems.iterator();
            while (iter.hasNext()) {
                ItemInfo i = iter.next();
                if (i == null) {
                    iter.remove();
                }
            }

            // If we aren't filtering on a screen, then the set of items to load is the full set of
            // items given.
            if (currentScreen < 0) {
                currentScreenItems.addAll(allWorkspaceItems);
            }

            // Order the set of items by their containers first, this allows use to walk through the
            // list sequentially, build up a list of containers that are in the specified screen,
            // as well as all items in those containers.
            Set<Long> itemsOnScreen = new HashSet<Long>();
            Collections.sort(allWorkspaceItems, new Comparator<ItemInfo>() {
                @Override
                public int compare(ItemInfo lhs, ItemInfo rhs) {
                    return (int) (lhs.container - rhs.container);
                }
            });
            for (ItemInfo info : allWorkspaceItems) {
                if (info.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                    if (info.screen == currentScreen) {
                        currentScreenItems.add(info);
                        itemsOnScreen.add(info.id);
                    } else {
                        otherScreenItems.add(info);
                    }
                } else if (info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                    currentScreenItems.add(info);
                    itemsOnScreen.add(info.id);
                } else {
                    if (itemsOnScreen.contains(info.container)) {
                        currentScreenItems.add(info);
                        itemsOnScreen.add(info.id);
                    } else {
                        otherScreenItems.add(info);
                    }
                }
            }
        }

        /** Filters the set of widgets which are on the specified screen. */
        private void filterCurrentAppWidgets(int currentScreen,
                ArrayList<LauncherAppWidgetInfo> appWidgets,
                ArrayList<LauncherAppWidgetInfo> currentScreenWidgets,
                ArrayList<LauncherAppWidgetInfo> otherScreenWidgets) {
            // If we aren't filtering on a screen, then the set of items to load is the full set of
            // widgets given.
            if (currentScreen < 0) {
                currentScreenWidgets.addAll(appWidgets);
            }

            for (LauncherAppWidgetInfo widget : appWidgets) {
                if (widget == null) continue;
                if (widget.container == LauncherSettings.Favorites.CONTAINER_DESKTOP &&
                        widget.screen == currentScreen) {
                    currentScreenWidgets.add(widget);
                } else {
                    otherScreenWidgets.add(widget);
                }
            }
        }

        /** Filters the set of folders which are on the specified screen. */
        private void filterCurrentFolders(int currentScreen,
                HashMap<Long, ItemInfo> itemsIdMap,
                HashMap<Long, FolderInfo> folders,
                HashMap<Long, FolderInfo> currentScreenFolders,
                HashMap<Long, FolderInfo> otherScreenFolders) {
            // If we aren't filtering on a screen, then the set of items to load is the full set of
            // widgets given.
            if (currentScreen < 0) {
                currentScreenFolders.putAll(folders);
            }

            for (long id : folders.keySet()) {
                ItemInfo info = itemsIdMap.get(id);
                FolderInfo folder = folders.get(id);
                if (info == null || folder == null) continue;
                if (info.container == LauncherSettings.Favorites.CONTAINER_DESKTOP &&
                        info.screen == currentScreen) {
                    currentScreenFolders.put(id, folder);
                } else {
                    otherScreenFolders.put(id, folder);
                }
            }
        }

        /** Sorts the set of items by hotseat, workspace (spatially from top to bottom, left to
         * right) */
        private void sortWorkspaceItemsSpatially(ArrayList<ItemInfo> workspaceItems) {
            // XXX: review this
            Collections.sort(workspaceItems, new Comparator<ItemInfo>() {
                @Override
                public int compare(ItemInfo lhs, ItemInfo rhs) {
                    int cellCountX = LauncherModel.getCellCountX();
                    int cellCountY = LauncherModel.getCellCountY();
                    int screenOffset = cellCountX * cellCountY;
                    int containerOffset = screenOffset * (Launcher.SCREEN_COUNT + 1); // +1 hotseat
                    long lr = (lhs.container * containerOffset + lhs.screen * screenOffset +
                            lhs.cellY * cellCountX + lhs.cellX);
                    long rr = (rhs.container * containerOffset + rhs.screen * screenOffset +
                            rhs.cellY * cellCountX + rhs.cellX);
                    return (int) (lr - rr);
                }
            });
        }

        private void bindWorkspaceItems(final Callbacks oldCallbacks,
                final ArrayList<ItemInfo> workspaceItems,
                final ArrayList<LauncherAppWidgetInfo> appWidgets,
                final HashMap<Long, FolderInfo> folders,
                ArrayList<Runnable> deferredBindRunnables) {

            final boolean postOnMainThread = (deferredBindRunnables != null);

            // Bind the workspace items
            int N = workspaceItems.size();
            for (int i = 0; i < N; i += ITEMS_CHUNK) {
                final int start = i;
                final int chunkSize = (i+ITEMS_CHUNK <= N) ? ITEMS_CHUNK : (N-i);
                final Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                        if (callbacks != null) {
                            callbacks.bindItems(workspaceItems, start, start+chunkSize);
                        }
                    }
                };
                if (postOnMainThread) {
                    deferredBindRunnables.add(r);
                } else {
                    runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
                }
            }

            // Bind the folders
            if (!folders.isEmpty()) {
				LauncherApplication.logVulcan.print("test folder");
                final Runnable r = new Runnable() {
                    public void run() {
                        Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                        if (callbacks != null) {
                            callbacks.bindFolders(folders);
                        }
                    }
                };
                if (postOnMainThread) {
                    deferredBindRunnables.add(r);
                } else {
                    runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
                }
            }

            // Bind the widgets, one at a time
            N = appWidgets.size();
            for (int i = 0; i < N; i++) {
                final LauncherAppWidgetInfo widget = appWidgets.get(i);
                final Runnable r = new Runnable() {
                    public void run() {
                        Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                        if (callbacks != null) {
                            callbacks.bindAppWidget(widget);
                        }
                    }
                };
                if (postOnMainThread) {
                    deferredBindRunnables.add(r);
                } else {
                    runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
                }
            }
        }

        /**
         * Binds all loaded data to actual views on the main thread.
         */
		private void bindWorkspace(int synchronizeBindPage) {
            final long t = SystemClock.uptimeMillis();
            Runnable r;

            // Don't use these two variables in any of the callback runnables.
            // Otherwise we hold a reference to them.
            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us.  Just bail.
                Log.w(TAG, "LoaderTask running with no launcher");
                return;
            }

            final boolean isLoadingSynchronously = (synchronizeBindPage > -1);
            final int currentScreen = isLoadingSynchronously ? synchronizeBindPage :
                oldCallbacks.getCurrentWorkspaceScreen();
            
            
            LauncherApplication.logVulcan.print(String.format("bindWorkspace:  isLoadingSynchronously = %b, currentScreen = %d",
            		isLoadingSynchronously,currentScreen));

            // Load all the items that are on the current page first (and in the process, unbind
            // all the existing workspace items before we call startBinding() below.
            unbindWorkspaceItemsOnMainThread();
            ArrayList<ItemInfo> workspaceItems = new ArrayList<ItemInfo>();
            ArrayList<LauncherAppWidgetInfo> appWidgets =
                    new ArrayList<LauncherAppWidgetInfo>();
            final HashMap<Long, FolderInfo> folders = new HashMap<Long, FolderInfo>();
            final HashMap<Long, ItemInfo> itemsIdMap = new HashMap<Long, ItemInfo>();
            synchronized (sBgLock) {
                workspaceItems.addAll(sBgWorkspaceItems);
                appWidgets.addAll(sBgAppWidgets);
                folders.putAll(sBgFolders);
                itemsIdMap.putAll(sBgItemsIdMap);
            }

            ArrayList<ItemInfo> currentWorkspaceItems = new ArrayList<ItemInfo>();
            ArrayList<ItemInfo> otherWorkspaceItems = new ArrayList<ItemInfo>();
            ArrayList<LauncherAppWidgetInfo> currentAppWidgets =
                    new ArrayList<LauncherAppWidgetInfo>();
            ArrayList<LauncherAppWidgetInfo> otherAppWidgets =
                    new ArrayList<LauncherAppWidgetInfo>();
            HashMap<Long, FolderInfo> currentFolders = new HashMap<Long, FolderInfo>();
            HashMap<Long, FolderInfo> otherFolders = new HashMap<Long, FolderInfo>();

            // Separate the items that are on the current screen, and all the other remaining items
            filterCurrentWorkspaceItems(currentScreen, workspaceItems, currentWorkspaceItems,
                    otherWorkspaceItems);
            filterCurrentAppWidgets(currentScreen, appWidgets, currentAppWidgets,
                    otherAppWidgets);
            filterCurrentFolders(currentScreen, itemsIdMap, folders, currentFolders,
                    otherFolders);
            sortWorkspaceItemsSpatially(currentWorkspaceItems);
            sortWorkspaceItemsSpatially(otherWorkspaceItems);

            // Tell the workspace that we're about to start binding items
            r = new Runnable() {
                public void run() {
                    Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.startBinding(itemsIdMap,folders);
                    }
                }
            };
            runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);

            // Load items on the current page
            bindWorkspaceItems(oldCallbacks, currentWorkspaceItems, currentAppWidgets,currentFolders, null);
            if (isLoadingSynchronously) {
                r = new Runnable() {
                    public void run() {
                        Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                        if (callbacks != null) {
                            callbacks.onPageBoundSynchronously(currentScreen);
                        }
                    }
                };
                runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
            }

            // Load all the remaining pages (if we are loading synchronously, we want to defer this
            // work until after the first render)
            mDeferredBindRunnables.clear();
            bindWorkspaceItems(oldCallbacks, otherWorkspaceItems, otherAppWidgets, otherFolders,
                    (isLoadingSynchronously ? mDeferredBindRunnables : null));

            // Tell the workspace that we're done binding items
            r = new Runnable() {
                public void run() {
                    Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.finishBindingItems();
                        if(mCreateNewDb){
                        	Log.e("linp", "++++++++++++++++++++++++++++first Time to startup system...");
                        	((Launcher)callbacks).findSpareIconsAndSendNotification();
                        }else
                        	Log.e("linp", "++++++++++++++++++++++++++++second Time to startup system...");
                        mCreateNewDb = false;
						//LauncherApplication.logVulcan.print("to call callbacks.bindAppsRemoved(removedPackageNames,true) when bindWorkspace, pkgNum = " + mRemovedWidgetPackage.size());
                        //callbacks.bindAppsRemoved(mRemovedWidgetPackage, true);
                        //mRemovedWidgetPackage.clear();
                    }

                    // If we're profiling, ensure this is the last thing in the queue.
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "bound workspace in "
                            + (SystemClock.uptimeMillis()-t) + "ms");
                    }

                    mIsLoadingAndBindingWorkspace = false;
                }
            };
            if (isLoadingSynchronously) {
                mDeferredBindRunnables.add(r);
            } else {
                runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
            }
        }

        private void loadAndBindAllApps() {
            if (DEBUG_LOADERS) {
                Log.d(TAG, "loadAndBindAllApps mAllAppsLoaded=" + mAllAppsLoaded);
            }
            if (!mAllAppsLoaded) {
                loadAllAppsByBatch();
                synchronized (LoaderTask.this) {
                    if (mStopped) {
                        return;
                    }
                    mAllAppsLoaded = true;
                }
            } else {
                onlyBindAllApps();
            }
        }

        private void onlyBindAllApps() {
            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us.  Just bail.
                Log.w(TAG, "LoaderTask running with no launcher (onlyBindAllApps)");
                return;
            }

            // shallow copy
            @SuppressWarnings("unchecked")
            final ArrayList<ApplicationInfo> list
                    = (ArrayList<ApplicationInfo>) mBgAllAppsList.data.clone();
            Runnable r = new Runnable() {
                public void run() {
                    final long t = SystemClock.uptimeMillis();
                    final Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.bindAllApplications(list);
                    }
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "bound all " + list.size() + " apps from cache in "
                                + (SystemClock.uptimeMillis()-t) + "ms");
                    }
                }
            };
            boolean isRunningOnMainThread = !(sWorkerThread.getThreadId() == Process.myTid());
            if (oldCallbacks.isAllAppsVisible() && isRunningOnMainThread) {
                r.run();
            } else {
                mHandler.post(r);
            }
        }

        private void loadAllAppsByBatch() {
            final long t = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

            // Don't use these two variables in any of the callback runnables.
            // Otherwise we hold a reference to them.
            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us.  Just bail.
                Log.w(TAG, "LoaderTask running with no launcher (loadAllAppsByBatch)");
                return;
            }

            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            final PackageManager packageManager = mContext.getPackageManager();
            List<ResolveInfo> apps = null;

            int N = Integer.MAX_VALUE;

            int startIndex;
            int i=0;
            int batchSize = -1;
            while (i < N && !mStopped) {
                if (i == 0) {
                    mBgAllAppsList.clear();
                    final long qiaTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
                    apps = packageManager.queryIntentActivities(mainIntent, 0);
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "queryIntentActivities took "
                                + (SystemClock.uptimeMillis()-qiaTime) + "ms");
                    }
                    if (apps == null) {
                        return;
                    }
                    N = apps.size();
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "queryIntentActivities got " + N + " apps");
                    }
                    if (N == 0) {
                        // There are no apps?!?
                        return;
                    }
                    if (mBatchSize == 0) {
                        batchSize = N;
                    } else {
                        batchSize = mBatchSize;
                    }

                    final long sortTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
                    Collections.sort(apps,
                            new LauncherModel.ShortcutNameComparator(packageManager, mLabelCache));
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "sort took "
                                + (SystemClock.uptimeMillis()-sortTime) + "ms");
                    }
                }

                final long t2 = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

                startIndex = i;
                for (int j=0; i<N && j<batchSize; j++) {
                    // This builds the icon bitmaps.
                    mBgAllAppsList.add(new ApplicationInfo(packageManager, apps.get(i),
                            mIconCache, mLabelCache));
                    i++;
                }

                final boolean first = i <= batchSize;
                final Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                final ArrayList<ApplicationInfo> added = mBgAllAppsList.added;
                mBgAllAppsList.added = new ArrayList<ApplicationInfo>();

                mHandler.post(new Runnable() {
                    public void run() {
                        final long t = SystemClock.uptimeMillis();
                        if (callbacks != null) {
                            if (first) {
                                callbacks.bindAllApplications(added);
                            } else {
                                callbacks.bindAppsAdded(added);
                            }
                            if (DEBUG_LOADERS) {
                                Log.d(TAG, "bound " + added.size() + " apps in "
                                    + (SystemClock.uptimeMillis() - t) + "ms");
                            }
                        } else {
                            Log.i(TAG, "not binding apps: no Launcher activity");
                        }
                    }
                });

                if (DEBUG_LOADERS) {
                    Log.d(TAG, "batch of " + (i-startIndex) + " icons processed in "
                            + (SystemClock.uptimeMillis()-t2) + "ms");
                }

                if (mAllAppsLoadDelay > 0 && i < N) {
                    try {
                        if (DEBUG_LOADERS) {
                            Log.d(TAG, "sleeping for " + mAllAppsLoadDelay + "ms");
                        }
                        Thread.sleep(mAllAppsLoadDelay);
                    } catch (InterruptedException exc) { }
                }
            }

            if (DEBUG_LOADERS) {
                Log.d(TAG, "cached all " + N + " apps in "
                        + (SystemClock.uptimeMillis()-t) + "ms"
                        + (mAllAppsLoadDelay > 0 ? " (including delay)" : ""));
            }
        }

        public void dumpState() {
            synchronized (sBgLock) {
                Log.d(TAG, "mLoaderTask.mContext=" + mContext);
                Log.d(TAG, "mLoaderTask.mIsLaunching=" + mIsLaunching);
                Log.d(TAG, "mLoaderTask.mStopped=" + mStopped);
                Log.d(TAG, "mLoaderTask.mLoadAndBindStepFinished=" + mLoadAndBindStepFinished);
                Log.d(TAG, "mItems size=" + sBgWorkspaceItems.size());
            }
        }
    }
	//Aurora-start:xiejun:Bug#56
	//void enqueuePackageUpdated(PackageUpdatedTask task) {
    static void enqueuePackageUpdated(Runnable task) {
	//Aurora-start:xiejun:Bug#56
        sWorker.post(task);
    }
    
    public void loadDelay() {
    	sWorker.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mSdcardMounted) {
					Log.d("vulcan-startloader","return in loadDelay");
					return;
				}
				Log.i(TAG, "-------loadDelay()-------");
				mSdcardMounted = true;
				mAppAvailable = true;
				LauncherApplication.logVulcan.print("to call forceAuroraReload by loadDelay in 120s");
				Log.d("vulcan-crash","loadDelay: time is up to call forceAuroraReload");
				forceAuroraReload();
				// writeFirstBootValue();
			}
		}, 120000);
    }
    
	private void writeFirstBootValue() {
		String spKey = LauncherApplication.getSharedPreferencesKey();
		SharedPreferences sp = mApp.getSharedPreferences(spKey,
				Context.MODE_PRIVATE);
		boolean isFirst = sp.getBoolean(LauncherApplication.FIRST_BOOT, false);
		Log.i(TAG, "writeFirstBootValue(), first = " + isFirst);
		if (!isFirst) {
			SharedPreferences.Editor editor = sp.edit();
			editor.putBoolean(LauncherApplication.FIRST_BOOT, true);
			editor.commit();
		}
	}
	
    interface PackageAction
    {
       public boolean onPackagesChanged(Callbacks senderCallback,String[] pkgList);
    }
    
    //iht 图标更新任务
    private class UpdateIconByPackageAndClassName implements Runnable{

    	Context mcontext;
    	ArrayList<String> pkgList = new ArrayList<String>();
    	
    	public UpdateIconByPackageAndClassName(Context mcontext, ArrayList<String> list){
    		pkgList.clear();
    		pkgList.addAll(list);
    		this.mcontext = mcontext;
    	}
    	
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(!pkgList.isEmpty()){
				sBgItemList.clear();
				//sWidgetItemList.clear();
				sWidgetItemMap.clear();
				for(String s:pkgList){
					if(s != null && s.contains("$")){
						String[] tx = s.split("\\$");//特殊字符，转义
						int len = tx.length;
						if(len>1){
							String packageName = tx[0];
							String className = tx[1];
							//Log.v("iht-icon", "packageName：："+packageName+",    clsname::::"+className);
							//boolean bool = false;
							if(packageName != null && className != null){
								
								//ShortCutInfo
								Iterator<Entry<Long, ItemInfo>> ite = sBgItemsIdMap.entrySet().iterator();
								while(ite.hasNext()){
									Entry<Long, ItemInfo> e = ite.next();
									ItemInfo item = (ItemInfo) e.getValue();
									if(item instanceof ShortcutInfo){
										ShortcutInfo sin = (ShortcutInfo)item;  
										if(s != null){
											//Log.v("iht-icon", "pkgName:"+s.getPackageName()+",		"+s.getClassName());
											if(packageName.equals(sin.getPackageName()) && className.equals(sin.getClassName()) ){
												sBgItemList.add(sin);
												//bool = true;
												break;
											}
										}
									}
								}
								
								//Widget信息(所有的widget均是基于现有的桌面Icon？？？？？？？)
								/*if(!bool){
									ComponentName cmp = new ComponentName(packageName, className);
									sWidgetItemList.add(cmp);
								}*/
								sWidgetItemMap.put(packageName, packageName);
							}
						}
					}
					continue;
				}
				
				int countSize = sWidgetItemMap.size(); //sBgItemList.size() + sWidgetItemList.size();
				
				//处理更换
				if(countSize > NOTIFY_APPS_TO_UPDATEIICON_SIZE){
					//主动重新加载 
					//startLoader(true, -1);//需要优化
				}else{
					updateAppsIcon(mcontext, sBgItemList, sWidgetItemMap); //更新个别图标			
				}
			}
		}
    	
    }

    private class PackageUpdatedTask implements Runnable,PackageAction {
        int mOp;
        String[] mPackages;
        boolean mReplace;

        public static final int OP_NONE = 0;
        public static final int OP_ADD = 1;
        public static final int OP_UPDATE = 2;
        public static final int OP_REMOVE = 3; // uninstlled
        public static final int OP_UNAVAILABLE = 4; // external media unmounted
        
        public PackageUpdatedTask(int op, String[] packages, boolean replace) {
            mOp = op;
            mPackages = packages;
            mReplace = replace;
        }
        
        /**
         * query all the intents which could be seen on the desktop through package manager
         * @param pkgName
         * @return
         */
        HashSet<Intent> getShortcutIntentListByPkgName(String pkgName) {
        	HashSet<Intent> intents = new HashSet<Intent>();

        	for (ItemInfo info : sBgItemsIdMap.values()) {
        		if((info instanceof ShortcutInfo) == false) {
        			continue;
        		}
        		
        		if(info.itemType != LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION
        				&& info.itemType != LauncherSettings.BaseLauncherColumns.ITEM_TYPE_SHORTCUT) {
        			continue;
        		}
        		ShortcutInfo si = (ShortcutInfo)info;
        		if(si.intent == null) {
        			continue;
        		}

        		if(si.getPackageName() == null || si.getPackageName().equals("")){
        			
        			if(si.itemType == LauncherSettings.BaseLauncherColumns.ITEM_TYPE_SHORTCUT){
            			if(si.iconResource != null && pkgName.equals(si.iconResource.packageName)){
            				intents.add(si.intent);
            			}
            			
            			if(si.iconResource == null && si.intent.getComponent() != null){
            				String p = si.intent.getComponent().getPackageName();
            				if(p != null && !p.equals("")){
            					intents.add(si.intent);
            				}
            			}
            		}
        		}else{
            		if(si.getPackageName().equals(pkgName)) {
            			Log.d("vulcan-setup","getShortcutIntentListByPkgName: intent = " + si.intent);
            			intents.add(si.intent);
            		}
        		}
        		/*if(si.getPackageName().equals(pkgName)) {
        			Log.d("vulcan-setup","getShortcutIntentListByPkgName: intent = " + si.intent);
        			intents.add(si.intent);
        		}*/
        	}
  	
        	return intents;
        }
        
		private boolean onPackageChanged(Callbacks senderCallback,String pkg,boolean existsPM,boolean existsDesk) {
			//case1:"com.android.stk".equals(mPackages[0])
			if(filterSpecialApp(pkg)) {
				return true;
			}

			//vulcan added in 2014-6-6
			//EXCEPTION:
			//when we try to update a package the old package is not present,
			//so we add a new icon on the desktop!
			//case2: not present in local,present in pm
			if(!existsDesk && existsPM) {
				Log.d("vulcan-contact","onPackageChanged: to install new pkg,name = " + pkg);
				addAuroraApp(pkg, senderCallback);
			}
			else if(existsDesk && !existsPM) {//case3:not present in pm, present in local
				Log.d("vulcan-contact","onPackageChanged: to uninstall old pkg,name = " + pkg);
				//removedPackageNames.add(packages[i]);
				//callbacks.bindAppsRemoved(removedPackageNames, true);
				//ArrayList<Intent> pkgListToRmv = new ArrayList<Intent>();
				HashSet<Intent> appsToRmv = getShortcutIntentListByPkgName(pkg);//iht 需要卸载的Icon-Intent
				HashSet<String> widgetsToRmv = new HashSet<String>();
				widgetsToRmv.add(pkg);
				removeIntentInMainThread(senderCallback,appsToRmv,widgetsToRmv);//iht 需要卸载的Icon-Intent
			}
			else if(mReplace) {//case4:other case in which we have to update
				Log.d("vulcan-contact","onPackageChanged: to update old pkg,name = " + pkg);
				updateAuroraApp(pkg, senderCallback);
			}
			else {
				Log.d("vulcan-contact","onPackageChanged: to update old pkg,name = " + pkg);
				updateAuroraApp(pkg, senderCallback);
			}

        	return true;
        }
        
        private void checkPkgsExistPM(Context context,String[] pkgList,boolean[] pkgExistPM) {
        	final PackageManager pkgMan = context.getPackageManager();
        	List<ResolveInfo> appLst = null;

            final Intent intentK = new Intent(Intent.ACTION_MAIN, null);
            intentK.addCategory(Intent.CATEGORY_LAUNCHER);
            
            for(int ii = 0;ii < pkgList.length; ii ++) {
            	intentK.setPackage(pkgList[ii]);
            	appLst = pkgMan.queryIntentActivities(intentK, 0);
            	if(appLst == null) {
            		Log.d("vulcan-contact",String.format("checkPkgsExistPM: %s doesn't exist in PM", pkgList[ii]));
            		pkgExistPM[ii] = false;
            	}
            	else if(appLst.size() <= 0) {
            		Log.d("vulcan-contact",String.format("checkPkgsExistPM: %s doesn't exist in PM", pkgList[ii]));
            		pkgExistPM[ii] = false;
            	}
            	else {
            		Log.d("vulcan-contact",String.format("checkPkgsExistPM: %s exists in PM", pkgList[ii]));
            		pkgExistPM[ii] = true;
            	}
            }
        	return;
        }
        
        //vulcan create this method in 2014-6-26
        //look for only application(not include shortcut) on the desktop
        private void checkPkgsExistDesk(String[] pkgList,boolean[] pkgExistDesk) {
        	
        	//set default status
        	for(int ii = 0;ii < pkgExistDesk.length;ii ++) {
        		pkgExistDesk[ii] = false;
        	}
        	
    		for (ItemInfo info : sBgItemsIdMap.values()) {
    			if(!(info instanceof ShortcutInfo)) {
    				continue;
    			}
    			if(info.itemType != LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION) {
    				continue;
    			}
    			ShortcutInfo sInfo = (ShortcutInfo) info;
    			
    			if(sInfo.intent == null) {
    				continue;
    			}
    			
    			ComponentName cn = sInfo.intent.getComponent();
    			
    			String desktopPkg = cn.getPackageName();
    			
    			if(desktopPkg == null) {
    				continue;
    			}
    			
    			for(int ii = 0;ii < pkgList.length;ii ++) {
    				if(desktopPkg.equals(pkgList[ii])) {
    					pkgExistDesk[ii] = true;
    				}
    			}
    			
    			
    		}
    		
			for(int ii = 0;ii < pkgExistDesk.length;ii ++) {
				if(pkgExistDesk[ii]) {
					Log.d("vulcan-contact",String.format("checkPkgsExistDesk: %s exists on desktop", pkgList[ii]));
				}
				else {
					Log.d("vulcan-contact",String.format("checkPkgsExistDesk: %s doesn't exist on desktop", pkgList[ii]));
				}
			}
        	return;
        }

        public boolean onPackagesChanged(Callbacks senderCallback,String[] pkgList) {
        	//ensure the length is positive
        	if(pkgList.length <= 0) {
        		return true;
        	}
        	
        	//suppose operator new succeeds when length is positive
        	boolean[] pkgExistsPM = new boolean[pkgList.length];
        	boolean[] pkgExistsDesk = new boolean[pkgList.length];

            //Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
            Callbacks newestCb = getCallback();
            
            if(newestCb == null) {
            	return true;
            }
            
            if(senderCallback != newestCb) {
            	return true;
            }
            
			if (mPackages.length >= 1) {
				LauncherApplication.logVulcan
						.print("to call runPackageChangedOnDefferedTask, pkgName = "
								+ mPackages[0]);
			}

            Log.d("vulcan-contact","onPackagesChanged: to call checkPkgsExistPM");
            checkPkgsExistPM(mApp,pkgList,pkgExistsPM);

            Log.d("vulcan-contact","onPackagesChanged: to call checkPkgsExistDesk");
            checkPkgsExistDesk(pkgList,pkgExistsDesk);

            for(int ii = 0;ii < pkgList.length;ii ++) {
            	onPackageChanged(senderCallback,pkgList[ii],pkgExistsPM[ii],pkgExistsDesk[ii]);
            }
            return true;
        }
        
        private void runPackageChangedOnDefferedTask(final PackageAction pa) {
        	final Callbacks senderCallbacks = ((mCallbacks != null) ? mCallbacks.get() : null);
        	Runnable runnable = new Runnable() {
        		public void run() {
        			pa.onPackagesChanged(senderCallbacks, mPackages);
        			return;
        		}
        	};
        	mHandler.post(runnable);
        }

    	/**
    	 * vulcan re-created this method in 2014-6-26
    	 */
        public void run() {

        	final Callbacks senderCallbacks = ((mCallbacks != null) ? mCallbacks.get() : null);
        	
        	
        	
        	if(senderCallbacks == null) {
        		LauncherApplication.logVulcan.print("PackageUpdatedTask.run: return because senderCallbacks is null");
        		resetLoadedState(false, true);
        		return;
        	}
        	
//        	 mHandler.post(new Runnable() {
//	                @Override
//	                public void run() {
//	                	senderCallbacks.dampedOverScroll();
//	                }
//        	  });
        	
        	if (mOp == OP_REMOVE 
        	|| mOp == OP_UNAVAILABLE
        	|| mOp == OP_UPDATE
        	|| mOp == OP_ADD) {
        		Log.d("vulcan-contact","mOp = " + mOp);
        		LauncherApplication.logVulcan.print("to call runPackageChangedOnDefferedTask, mOp = " + mOp);
        		
        		//LauncherAppWidgetHost.onProvidersChanged do the same thing
	            /*mHandler.post(new Runnable() {
	                @Override
	                public void run() {
	                    Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
	                    if (senderCallbacks == cb && cb != null) {
	                    	senderCallbacks.bindPackagesUpdated();
	                    }
	                }
	            });*/
	            runPackageChangedOnDefferedTask(this);
        	}
			// Aurora <jialf> <2013-11-12> modify for fix bug #418 begin
			if (mCallbacks != null
					&& !((Launcher) mCallbacks.get()).isEditMode()) {
	            mHandler.post(new Runnable() {			
					@Override
					public void run() {
						Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
						if (senderCallbacks == cb && cb != null&&!(((Launcher)senderCallbacks).isWorkspaceLocked())) {
							Log.i("xiejun","PackageUpdatedTask:autoManangePages");
							LauncherApplication.logVulcan.print("PackageUpdatedTask: to call autoManangePages");
							senderCallbacks.autoManangePages();
						}
					}
				});
            }
			// Aurora <jialf> <2013-11-12> modify for fix bug #418 end
        }
    }
    
    private void addAuroraApp(final String pkg, final Callbacks oldCb) {
    	//ItemPos itemPos = null;
    	Log.d("doubleicon","doubleicon>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> = ");
    	
    	List<ResolveInfo> list = AllAppsList.findActivitiesForPackage(mApp,pkg);
		if (list == null || list.isEmpty())
			return;
		/*Callbacks newCb = LauncherModel.this.tryGetCallback(oldCb);
		if( newCb != null){
       	 sortByPkg = newCb.getPkgProviderMap();
        }*/
		//mWithoutWidgets.clear();
		for(ResolveInfo rInfo : list){
			Log.d("doubleicon","addAuroraApp: pkg = " + pkg);
			
			//int[] loc = findLastPostionView();
			//itemPos = findFirstVacancyOfWorkspace();
			
			//if(itemPos != null) {
			//	LauncherApplication.logVulcan.print(String.format("addAuroraApp: screen = %d, x=%d, y=%d",itemPos.screen,itemPos.x,itemPos.y));
			//	Log.d("vulcan-repeat",String.format("addAuroraApp: screen = %d, x=%d, y=%d",itemPos.screen,itemPos.x,itemPos.y));
			//}
			//else {
			//	LauncherApplication.logVulcan.print("addAuroraApp: location is garbage");
			//	Log.d("doubleicon","addAuroraApp: location is garbage");
			//}

			//if (itemPos != null) {
				final ShortcutInfo info = getShortcutInfo(rInfo, -1, -1, -1, null);
				boolean exist = shortcutExists(mApp, info.title.toString(), info.intent);
				Log.i(TAG, "app exists is : " + exist);
				if (exist) {
					LauncherApplication.logVulcan.print("addAuroraApp:fails to add new pkg " + pkg);
					Log.d("vulcan-contact","addAuroraApp:fails to add new pkg " + pkg);
					continue;
				}
				
				if(info.intent != null) {
					Log.d("vulcan-setup",String.format("addAuroraApp: intent = %s",info.intent.toUri(0)));
				}
				
				info.newFlag = true;
				Log.d("doubleicon", String.format("addAuroraApp: bind item: title = %s, screen = %d, x = %d, y = %d",
										info.title, info.screen, info.cellX, info.cellY));
				
				addNewAppInMainThread(oldCb,info);

			//}
		}
    }
    
    //vulcan added this function in 2014-6-6
    //this function query package name in the variable sBgItemsIdMap
    //it only query from the application type.
    private boolean pkgnameIsInMemory(String pkgname) {
		for (ItemInfo info : sBgItemsIdMap.values()) {
			if (info instanceof ShortcutInfo
					&& info.itemType == LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION) {
				ShortcutInfo sInfo = (ShortcutInfo) info;
				ComponentName cn = sInfo.intent.getComponent();
				if(cn.getPackageName().equals(pkgname)) {
					return true;
				}
			}
		}
		return false;
    }
    
    /**
     * vulcan created this method in 2014-8-8
     * remove one app or shortcut in main thread.
     * @param oldCb
     * @param si
     */
    private void removeIntentInMainThread(Callbacks oldCb,HashSet<Intent> appsToRmv,HashSet<String> widgetsToRmv) {
    	Callbacks newCb = this.tryGetCallback(oldCb);
    	if(newCb == null) {
    		LauncherApplication.logVulcan.print("removeIntentInMainThread: fails to remove app because callback is null");
    		return;
    	}
 
    	newCb.bindAppsRemoved(appsToRmv,widgetsToRmv,true);
    	return;
    }
    
    /**
     * vulcan created this method in 2014-8-5
     * update an intent in main thread.
     */
    private void updateIntentInMainThread(final Callbacks oldCb,final ShortcutInfo si) {
   	
		runOnMainThread(new Runnable() {
			public void run() {
		    	Callbacks newCb = LauncherModel.this.tryGetCallback(oldCb);
		    	if(newCb == null) {
		    		LauncherApplication.logVulcan.print("updateIntentInMainThread: fails to update app because callback is null: " + si.intent);
		    		return;
		    	}
		    	newCb.bindAuroraAppsUpdated(si);
				return;
			}
		});
    	return;
    }
    
    /**
     * xiangzx created this method in 2016-01-28
     * @param oldCb
     * @param info
     */
    private void addAuroraHostView(final ShortcutInfo info) {    	
		runOnMainThread(new Runnable() {
			public void run() {
				 info.hostView = mAppWidgetHost.createView(mApp.getApplicationContext(), info.appWidgetId, appWidgetManager.getAppWidgetInfo(info.appWidgetId));
			}
		});
    }
    
    
    /**
     * vulcan created this method in 2014-8-5
     * @param oldCb
     * @param info
     */
    private void addNewAppInMainThread(final Callbacks oldCb,final ShortcutInfo info) {    	
		runOnMainThread(new Runnable() {
			public void run() {
				Callbacks newCb = LauncherModel.this.tryGetCallback(oldCb);
				if(newCb == null) {
					LauncherApplication.logVulcan.print("addNewAppInMainThread: fails to add new app because callback is null: " + info.intent);
					return;
				}
				if(LauncherModel.this.mWorkspace == null) {
					LauncherApplication.logVulcan.print("addNewAppInMainThread: fails to add new app because mWorkspace is null: " + info.intent);
					return;
				}
				ItemPos itemPos = LauncherModel.this.mWorkspace.findFirstVacancyOfWorkspace();
				if(itemPos == null) {
					return;
				}
				info.screen = itemPos.screen;
				info.cellX = itemPos.x;
				info.cellY = itemPos.y;
				newCb.bindAuroraAppsAdded(info);
				LauncherApplication.logVulcan.print("addAuroraApp:added a new app on the desktop. app name = " + info.title);
				Log.d("vulcan-setup",String.format("addAuroraApp:added a new app: %s(%d,%d,%d)",
						info.title,info.screen,info.cellX,info.cellY));
				return;
			}
		});
    	
    	
    	return;
    }
    
    private void updateAuroraApp(final String pkg, final Callbacks callbacks) {
    	
    	if(pkg == null) {
    		return;
    	}

    	boolean bFound = false;
    	String pkgName = pkg;
    	List<ResolveInfo> list = AllAppsList.findActivitiesForPackage(mApp,pkgName);
    	if (list == null || list.isEmpty()) {
			LauncherApplication.logVulcan.print("updateAuroraApp: packagename doesnot exist :" + pkgName);
			return;
    	}
		
		Log.d("vulcan-setup",String.format("updateAuroraApp: list.size: %d,pkg = %s", list.size(), pkg));
		for(ResolveInfo ri : list) {
			if(ri == null) {
				continue;
			}
			Log.d("vulcan-setup",String.format("updateAuroraApp: updating intent: %s", ri.activityInfo.name));
		}
		
    	//first cycle: remove old intents which doesnot exist in new package
    	HashSet<Intent> appsToRmv = new HashSet<Intent>();
		for (ItemInfo info : sBgItemsIdMap.values()) {
			//select only ShortcutInfo.
			//because we are installing an application, we may not remove folders or real shortcuts
			if((info instanceof ShortcutInfo) == false) {
				continue;
			}
			
			//select only ITEM_TYPE_APPLICATION
			//we don't process the shortcut, 
			//because shortcut may be created by user and resolve information doesn't inlucde it
			if(info.itemType != LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION) {
				continue;
			}

			ShortcutInfo siOnDesk = (ShortcutInfo) info;

			if(siOnDesk.intent == null) {
				LauncherApplication.logVulcan.print(String.format("updateAuroraApp1: cmpOnDesk is null, title = %s", siOnDesk.title));
				continue;
			}
			
			//check if the item on desktop is valid
			String pkgNameOnDesk = siOnDesk.getPackageName();
			String classNameOnDesk = siOnDesk.getClassName();
			if(pkgNameOnDesk == null || classNameOnDesk == null) {
				LauncherApplication.logVulcan.print(String.format("updateAuroraApp1: item on desk is invalid, title = %s", siOnDesk.title));
				continue;
			}
			
			//try to look for the class in the same package.
			if(!pkgNameOnDesk.equals(pkgName)) {
				continue;
			}
			
			bFound = false;
			for(ResolveInfo ri : list) {
				if(ri == null) {
					LauncherApplication.logVulcan.print("updateAuroraApp1: ri is null ");
					continue;
				}
				
				if(ri.activityInfo == null) {
					LauncherApplication.logVulcan.print("updateAuroraApp1: ri.activityInfo is null ");
					continue;
				}

				if(classNameOnDesk.equals(ri.activityInfo.name)) {
					bFound = true;
					break;
				}
			}
			
			if(!bFound) {
				appsToRmv.add(siOnDesk.intent);
				LauncherApplication.logVulcan.print(String.format("updateAuroraApp1: going to remove an intent, title = %s, intent = %s",
						siOnDesk.title,siOnDesk.intent));
				Log.d("vulcan-setup",String.format("updateAuroraApp1: going to remove an intent, title = %s, intent = %s",
						siOnDesk.title,siOnDesk.intent));
			}
		}
		HashSet<String> widgetsToRmv = new HashSet<String>();
		removeIntentInMainThread(callbacks,appsToRmv,widgetsToRmv);
		
		
		//second cycle: added new intent & update new intent
    	for(ResolveInfo resolveInfo : list) {
    		
    		if(resolveInfo == null) {
    			LauncherApplication.logVulcan.print("updateAuroraApp2: resolveInfo is null ");
    			continue;
    		}
    		
    		if(resolveInfo.activityInfo == null) {
    			LauncherApplication.logVulcan.print("updateAuroraApp2: resolveInfo.activityInfo is null ");
    			continue;
    		}
    		
    		Log.d("vulcan-setup",String.format("updateAuroraApp2: to look for %s", resolveInfo.activityInfo.name));
 
            //ComponentName cmpInPM = new ComponentName(pkgName, resolveInfo.activityInfo.name);
    		//intent.setComponent(cmpInPM);
    		bFound = false;
			for (ItemInfo itemInfo : sBgItemsIdMap.values()) {
				if((itemInfo instanceof ShortcutInfo) == false) {
					continue;
				}
				
				//only update application, because we cann't find information of shortcut in package manager
				if(itemInfo.itemType != LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION) {
					continue;
				}

				ShortcutInfo siOnDesk = (ShortcutInfo) itemInfo;
				
				if(siOnDesk.intent == null) {
					LauncherApplication.logVulcan.print(String.format("updateAuroraApp2: cmpOnDesk is null, title = %s", siOnDesk.title));
					continue;
				}
				
				//check if the item on desktop is valid
				String pkgNameOnDesk = siOnDesk.getPackageName();
				String classNameOnDesk = siOnDesk.getClassName();
				if(pkgNameOnDesk == null || classNameOnDesk == null) {
					LauncherApplication.logVulcan.print(String.format("updateAuroraApp2: item on desk is invalid, title = %s", siOnDesk.title));
					continue;
				}
				
				if(pkgNameOnDesk.equals(pkgName)) {
					Log.d("vulcan-setup",String.format("updateAuroraApp2: ===========pkgOnDesk:%s,%s===========", pkgNameOnDesk, classNameOnDesk));
				}
				if(pkgNameOnDesk.equals(pkgName)
						&& classNameOnDesk.equals(resolveInfo.activityInfo.name)) {
					//update new intent
					bFound = true;
					
		        	final Intent intent = new Intent(Intent.ACTION_MAIN, null);
		    		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		    		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
		    				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					intent.setClassName(pkgName, resolveInfo.activityInfo.name);
					
		    		LauncherApplication.logVulcan.print("updateAuroraApp2: updating intent: " + intent);
		    		Log.d("vulcan-setup",String.format("updateAuroraApp2: updating intent: %s", intent));
					
					siOnDesk.intent = intent;
					
					String titleInPM = (String) resolveInfo.loadLabel(mApp.getPackageManager());
					if(!siOnDesk.title.equals(titleInPM)) {
						siOnDesk.title = titleInPM;
					}
					
					modifyAuroraItemInDatabase(mApp, siOnDesk, titleInPM, intent.toUri(0));
					updateIntentInMainThread(callbacks,siOnDesk);
					break;
				}
			}

			if(!bFound) {
				//to add new intent
				//mWithoutWidgets.clear();
				final ShortcutInfo info = getShortcutInfo(resolveInfo, -1, -1, -1, null);
				boolean exist = shortcutExists(mApp, info.title.toString(), info.intent);
				if (exist) {
					LauncherApplication.logVulcan.print("updateAuroraApp2:fails to add new intent " + info.title);
					Log.d("vulcan-setup","updateAuroraApp2:fails to add new intent " + info.intent);
					continue;
				}
				Log.d("vulcan-setup","updateAuroraApp2: to add new intent " + info.intent);
				info.newFlag = true;
				addNewAppInMainThread(callbacks,info);
			}
    	}

    	return;
    }

    /**
     * This is called from the code that adds shortcuts from the intent receiver.  This
     * doesn't have a Cursor, but
     */
    public ShortcutInfo getShortcutInfo(PackageManager manager, Intent intent, Context context) {
        return getShortcutInfo(manager, intent, context, null, -1, -1, null);
    }

    /**
     * Make an ShortcutInfo object for a shortcut that is an application.
     *
     * If c is not null, then it will be used to fill in missing data like the title and icon.
     */
    public ShortcutInfo getShortcutInfo(PackageManager manager, Intent intent, Context context,
            Cursor c, int iconIndex, int titleIndex, HashMap<Object, CharSequence> labelCache) {
    	Bitmap icon = null;
        final ShortcutInfo info = new ShortcutInfo();

        ComponentName componentName = intent.getComponent();
        if (componentName == null) {
            return null;
        }
        
		if (filterSpecialApp(componentName.getPackageName()))
			return null;

        try {
            PackageInfo pi = manager.getPackageInfo(componentName.getPackageName(), 0);
            if (!pi.applicationInfo.enabled) {
                // If we return null here, the corresponding item will be removed from the launcher
                // db and will not appear in the workspace.
                return null;
            }
        } catch (NameNotFoundException e) {
            Log.d(TAG, "getPackInfo failed for package " + componentName.getPackageName());
        }

        // TODO: See if the PackageManager knows about this case.  If it doesn't
        // then return null & delete this.

        // the resource -- This may implicitly give us back the fallback icon,
        // but don't worry about that.  All we're doing with usingFallbackIcon is
        // to avoid saving lots of copies of that in the database, and most apps
        // have icons anyway.

        // Attempt to use queryIntentActivities to get the ResolveInfo (with IntentFilter info) and
        // if that fails, or is ambiguious, fallback to the standard way of getting the resolve info
        // via resolveActivity().
        ResolveInfo resolveInfo = null;
        ComponentName oldComponent = intent.getComponent();
        Intent newIntent = new Intent(intent.getAction(), null);
        newIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        newIntent.setPackage(oldComponent.getPackageName());
        List<ResolveInfo> infos = manager.queryIntentActivities(newIntent, 0);
        for (ResolveInfo i : infos) {
            ComponentName cn = new ComponentName(i.activityInfo.packageName,
                    i.activityInfo.name);
            if (cn.equals(oldComponent)) {
                resolveInfo = i;
            }
        }
        // Aurora <jialf> <2014-01-08> modify for fix bug #1769 begin
        /*if (resolveInfo == null) {
            resolveInfo = manager.resolveActivity(intent, 0);
        }*/
        // Aurora <jialf> <2014-01-08> modify for fix bug #1769 end
		// Aurora <jialf> <2013-10-21> modify for fix bug #22 && # 140 begin
        if (resolveInfo != null) {
            icon = mIconCache.getIcon(componentName, resolveInfo, labelCache);
        } else {
        	info.deleteFlag = true;
        }
		// Aurora <jialf> <2013-10-21> modify for fix bug #22 && # 140 end
        // the db
        //TODO:Icon
        if (icon == null) {
            if (c != null) {
                icon = getIconFromCursor(c, iconIndex, context);
            }
        }
        
        if(icon == null&&componentName!=null){
			icon =mUtils2Icon.getIcon(
					componentName.getPackageName(),
					componentName.getClassName(), Utils2Icon.OUTER_SHADOW);
        }
        
        // the fallback icon
        if (icon == null) {
            icon = getFallbackIcon();
            info.usingFallbackIcon = true;
        }
        info.setIcon(icon);

        // from the resource
        if (resolveInfo != null) {
            ComponentName key = LauncherModel.getComponentNameFromResolveInfo(resolveInfo);
            if (labelCache != null && labelCache.containsKey(key)) {
                info.title = labelCache.get(key);
            } else {
                info.title = resolveInfo.activityInfo.loadLabel(manager);
                if (labelCache != null) {
                    labelCache.put(key, info.title);
                }
            }
        }
        // from the db
        if (info.title == null) {
            if (c != null) {
                info.title =  c.getString(titleIndex);
            }
        }
        // fall back to the class name of the activity
        if (info.title == null) {
            info.title = componentName.getClassName();
        }
        // Aurora <haojj> <2014-1-14> add for 快速检索更新favorites begin
        info.constructSpell();
		// Aurora <haojj> <2014-1-14> end
        info.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
        return info;
    }

	/*************************************
	vulcan added these code in 2014-5-29
	In this function, we supposed input parameter is valid
	*/
	public boolean appInfoExistInPM(String intentDesc) {
		Intent intent;
		PackageManager pm;
		List<ResolveInfo> intentInfoList;

		//1.check if intentDesc can be resolved
		//2.check if it exists in pm
		try {
			intent = Intent.parseUri(intentDesc, 0);
		} catch (URISyntaxException e){
			return false;
		}

		//LauncherApplication.logVulcan.print("check AppInfoShouldDelete: intent = " + intent);
		pm = mApp.getPackageManager();
		intentInfoList = pm.queryIntentActivities(intent, 0);

		//LauncherApplication.logVulcan.print("check AppInfoShouldDelete: intentInfoList.size() = " + intentInfoList.size());

		if(intentInfoList.size() < 1) {
			return false;
		}

		return true;
	}

	/*************************************
	vulcan added these code in 2014-5-29
	In this function, we supposed input parameter is valid
	*/
	public boolean appInfoShouldDelete(String intentDesc,Cursor c) {
		int flags;
		int idxFlags;
		boolean isSysApp;
		boolean existInPM;

		idxFlags = c.getColumnIndexOrThrow(LauncherSettings.Favorites.FLAGS);
		flags = c.getInt(idxFlags);

		//LauncherApplication.logVulcan.print("check AppInfoShouldDelete: itemFlags(bin) = " + Integer.toBinaryString(flags));
		//LauncherApplication.logVulcan.print("check AppInfoShouldDelete: itemFlags(dec) = " + flags);
		//LauncherApplication.logVulcan.print("check AppInfoShouldDelete: sysFlag = " + android.content.pm.ApplicationInfo.FLAG_SYSTEM);

		isSysApp = ((android.content.pm.ApplicationInfo.FLAG_SYSTEM & flags) == 0);
		existInPM = appInfoExistInPM(intentDesc);

		LauncherApplication.logVulcan.print("check AppInfoShouldDelete: isSysApp = " + isSysApp + ",existInPM = " + existInPM);
		if(isSysApp && !existInPM) {
			return true;
		}

		

		return false;
	}


	public CharSequence mkTitleNupdateLabelCa(String intentDesc,Cursor c,HashMap<Object, CharSequence> labelCache) {
		final int titleIndex;
		final PackageManager pm;
		Intent intent;
		List<ResolveInfo> intentInfoList;
		ResolveInfo intentInfo;
		CharSequence title = null;

		//check input parameter:intentDesc,c
		if(c == null) {
			LauncherApplication.logVulcan.print("fatal error: null para for makeIcon");
			return null;
		}
		// the caller must give this parameter
		if(labelCache == null) {
			return null;
		}
		//it indicates there is a invalid record,so stop!!!
		try {
			intent = Intent.parseUri(intentDesc, 0);
		} catch (URISyntaxException e) {
			return null;
		}
		
		//initialize pm,titleIndex,key
		pm = mApp.getPackageManager();
		titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
		ComponentName key = intent.getComponent();
		if(key == null) {
			return null;
		}

		//main process:
		//1.query the intent from pm
		//2.get information of the intent from pm
		//3.if succeeds,get title from intentInfo,which means from pm
		//4.if fails,get it from labelCache
		//5.if still fails,get it from db
		//6.if still fails,get it from component name
		//7.update labelCache
		intentInfoList = pm.queryIntentActivities(intent, 0);
		if(intentInfoList.size() > 1) {
			LauncherApplication.logVulcan.print("fatal error: multi match when find intent");
		}

		if(intentInfoList.size() == 0) {
			//LauncherApplication.logVulcan.print("fatal error: can't find intent,perhaps sd card is offline");
		}
		if(intentInfoList.size() >= 1) {
			intentInfo = intentInfoList.get(0);
		} else {
			intentInfo = null;
		}

		//get title from intentInfo
		if (intentInfo != null) {
			title = intentInfo.activityInfo.loadLabel(pm);
		}
		//get title from labelCache
		if(title == null) {
			 title = labelCache.get(key);
		}
		//get title from db
        if (title == null) {
            title =  c.getString(titleIndex);
        }
        
        // fall back to the class name of the activity
        if (title == null) {
            title = intent.getComponent().getClassName();
        }

        //update labelCache
        if(title != null) {
        	labelCache.put(key, title);
        }

		return title;
	}


	/*************************************
	vulcan added these code in 2014-5-21
	*/
	public Bitmap mkIconNupdateLabelCa(String intentDesc,Cursor c,HashMap<Object, CharSequence> labelCache, boolean[] usingFallbackIcon) {
		final PackageManager pm;
		Intent intent;
		List<ResolveInfo> intentInfoList;
		ResolveInfo intentInfo;
		Bitmap icon = null;
		int iconIndex;
		
		//check input parameter:intentDesc,c
		//it indicates there is invalid record
		try {
			intent = Intent.parseUri(intentDesc, 0);
		} catch (URISyntaxException e){
			return null;
		}
		
		if(c == null) {
			LauncherApplication.logVulcan.print("fatal error: null para for makeIcon");
			return null;
		}

		//init pm,iconIndex
		pm = mApp.getPackageManager();
		iconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON);

		
		//main process
		//1.query intent information from pm
		//2.get icon info from mIconCache
		//3.if fails, get it from db
		//4.if still fails, get it from default icon
		intentInfoList = pm.queryIntentActivities(intent, 0);
		if(intentInfoList.size() > 1) {
			LauncherApplication.logVulcan.print("fatal error: multi match when find intent");
		}
		
		if(intentInfoList.size() == 0) {
			//LauncherApplication.logVulcan.print("fatal error: can't find intent,perhaps sd card is offline");
		}
		
		ComponentName  cname = intent.getComponent();
		if(intentInfoList.size() >= 1) {
			intentInfo = intentInfoList.get(0);
			icon = mIconCache.getIcon(cname, intentInfo, labelCache);
		}

        if (icon == null) {
			icon = getIconFromCursor(c, iconIndex, mApp);
        }
        
        if(icon == null&&cname!=null){
        	icon =mUtils2Icon.getIcon(
        			cname.getPackageName(),
        			cname.getClassName(), Utils2Icon.OUTER_SHADOW);
        }
        // the fallback icon
        if (icon == null) {
            icon = getFallbackIcon();
            usingFallbackIcon[0] = true;
        } else {
        	usingFallbackIcon[0] = false;
        }
		return icon;
	}

    /**************************************
     vulcan added these code in 2014-5-21
     this function is used to replace the getShortcutInfo which has 6 parameters
     Opposite to the getShortcutInfo, this function will not delete any application*/
    public ShortcutInfo makeApplicationInfo(String intentDesc,Cursor c,HashMap<Object, CharSequence> labelCache) {
        
    	Bitmap icon = null;
		boolean[] usingFallbackIcon = new boolean[1];
        final ShortcutInfo info = new ShortcutInfo();
        Intent intent;
        ComponentName componentName;
        
        //it indicates there is an invalid record
        try {
        	intent = Intent.parseUri(intentDesc, 0);
        } catch (URISyntaxException e) {
        	return null;
        }
        
        //it indicates there is an invalid record,too
        componentName = intent.getComponent();
        if (componentName == null) {
            return null;
        }

        //it indicates there is an invalid record,too
		/*if ("com.android.stk".equals(componentName.getPackageName()))
			return null;*/

		//check if we should delete this application
		//LauncherApplication.logVulcan.print("check intentDesc: " + intentDesc);
		if (appInfoShouldDelete(intentDesc, c)) {
			final int titleIndex = c.getColumnIndex(LauncherSettings.Favorites.TITLE);
			if (titleIndex != -1) {
				LauncherApplication.logVulcan.print("check intentDesc: going to delete, title = "
								+ c.getString(titleIndex));
			}
			info.deleteFlag = true;
			return info;
		}

		
		icon = mkIconNupdateLabelCa(intentDesc,c,labelCache,usingFallbackIcon);
        info.setIcon(icon);
		info.usingFallbackIcon = usingFallbackIcon[0];

		info.title = mkTitleNupdateLabelCa(intentDesc,c,labelCache);
        info.constructSpell();
        info.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
        return info;
    }
    
    /**
     * Returns the set of workspace ShortcutInfos with the specified intent.
     */
    static ArrayList<ItemInfo> getWorkspaceShortcutItemInfosWithIntent(Intent intent) {
        ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
        synchronized (sBgLock) {
            for (ItemInfo info : sBgWorkspaceItems) {
                if (info instanceof ShortcutInfo) {
                    ShortcutInfo shortcut = (ShortcutInfo) info;
                    if (shortcut.intent.toUri(0).equals(intent.toUri(0))) {
                        items.add(shortcut);
                    }
                }
            }
        }
        return items;
    }

	private ShortcutInfo getAuroraShortcutInfo(Intent intent, Cursor c,
			Context context, int iconTypeIndex, int iconPackageIndex,
			int iconResourceIndex, int iconIndex, int titleIndex) {

		/*****************************************
	 	Vulcan deleted these codes in 2014-5-22;
	 	According to new requirement, we must not delete any data in database even if it doesn't exist in the pkg manager.
	 	We delete these code because it is deleting our shortcuts when it's target doesn't exist in the pkg manager.
	 	Note that after deleting these code, the member variable mRemovedWidgetPackage wouldn't make any sense!!!
		*/
		/*
		LauncherApplication.logVulcan.print("AllAppsList.size() = " + mBgAllAppsList.size());
		if (intent != null && !"".equals(intent)) {
			ComponentName cn = intent.getComponent();
			String pkgName = null; 
			if (cn != null) {
				pkgName = cn.getPackageName();
			} else {
				pkgName = c.getString(iconPackageIndex);
			}
			if (pkgName != null) {
				int count = AllAppsList.findActivitiesForPackage(context,
						pkgName).size();
				if (count == 0 && !mRemovedWidgetPackage.contains(pkgName)) {
					mRemovedWidgetPackage.add(pkgName);
				}
			}
		}
		*/

		return getShortcutInfo(c, context, iconTypeIndex, iconPackageIndex,
				iconResourceIndex, iconIndex, titleIndex);
	}

    /**
     * Make an ShortcutInfo object for a shortcut that isn't an application.
     */
    private ShortcutInfo getShortcutInfo(Cursor c, Context context,
            int iconTypeIndex, int iconPackageIndex, int iconResourceIndex, int iconIndex,
            int titleIndex) {

        Bitmap icon = null;
        final ShortcutInfo info = new ShortcutInfo();
        info.itemType = LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT;

        // TODO: If there's an explicit component and we can't install that, delete it.

        info.title = c.getString(titleIndex);
        
        LauncherApplication.logVulcan.print("making shortcut,title = " + info.title);

        int iconType = c.getInt(iconTypeIndex);
        switch (iconType) {
        case LauncherSettings.Favorites.ICON_TYPE_RESOURCE:
            String packageName = c.getString(iconPackageIndex);
            String resourceName = c.getString(iconResourceIndex);
            PackageManager packageManager = context.getPackageManager();
            info.customIcon = false;
            // Aurora <jialf> <2014-02-14> added for fix bug #2214 begin
			if (resourceName != null) {
				ShortcutIconResource iconResource = new ShortcutIconResource();
				iconResource.packageName = packageName;
				info.iconResource = iconResource;
			}
            // Aurora <jialf> <2014-02-14> added for fix bug #2214 end
            // the resource
            try {
                Resources resources = packageManager.getResourcesForApplication(packageName);
                if (resources != null) {
                    final int id = resources.getIdentifier(resourceName, null, null);
                    icon = Utilities.createIconBitmap(
                            mIconCache.getFullResIcon(resources, id), context);
                }
            } catch (Exception e) {
                // drop this.  we have other places to look for icons
            }
            // the db
            if (icon == null) {
            	icon = getIconFromCursor(c, iconIndex, context);
            }
            // the fallback icon
            if (icon == null) {
                icon = getFallbackIcon();
                info.usingFallbackIcon = true;
            }
            break;
        case LauncherSettings.Favorites.ICON_TYPE_BITMAP:
            icon = getIconFromCursor(c, iconIndex, context);
            if (icon == null) {
                icon = getFallbackIcon();
                info.customIcon = false;
                info.usingFallbackIcon = true;
            } else {
                info.customIcon = true;
            }
            break;
        default:
            icon = getFallbackIcon();
            info.usingFallbackIcon = true;
            info.customIcon = false;
            break;
        }
        /*Drawable sourceIcon = new BitmapDrawable(context.getResources(),(Bitmap)icon);
    	if(sourceIcon!=null){
    		sourceIcon= mUtils2Icon.getCustomerIcon(sourceIcon, Utils2Icon.OUTER_SHADOW);
    	}
    	icon = ((BitmapDrawable)sourceIcon).getBitmap();
    	*/
        //icon =  mUtils2Icon.getRoundedBitmap(icon, false);
    	//TODO:NOTE3
    	if(DeviceProperties.isNeedScale()){
    		icon = Utilities.zoomBitmap(icon, context);
    	}
        info.setIcon(icon);
        return info;
    }

    Bitmap getIconFromCursor(Cursor c, int iconIndex, Context context) {
        @SuppressWarnings("all") // suppress dead code warning
        final boolean debug = false;
        if (debug) {
            Log.d(TAG, "getIconFromCursor app="
                    + c.getString(c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE)));
        }
        byte[] data = c.getBlob(iconIndex);
        try {
            return Utilities.createIconBitmap(
                    BitmapFactory.decodeByteArray(data, 0, data.length), context);
        } catch (Exception e) {
            return null;
        }
    }

    ShortcutInfo addShortcut(Context context, Intent data, long container, int screen,
            int cellX, int cellY, boolean notify) {
        final ShortcutInfo info = infoFromShortcutIntent(context, data, null);
        if (info == null) {
            return null;
        }
        addItemToDatabase(context, info, container, screen, cellX, cellY, notify);

        return info;
    }

    /**
     * Attempts to find an AppWidgetProviderInfo that matches the given component.
     */
    AppWidgetProviderInfo findAppWidgetProviderInfoWithComponent(Context context,
            ComponentName component) {
        List<AppWidgetProviderInfo> widgets =
            AppWidgetManager.getInstance(context).getInstalledProviders();
        for (AppWidgetProviderInfo info : widgets) {
            if (info.provider.equals(component)) {
                return info;
            }
        }
        return null;
    }

    /**
     * Returns a list of all the widgets that can handle configuration with a particular mimeType.
     */
    List<WidgetMimeTypeHandlerData> resolveWidgetsForMimeType(Context context, String mimeType) {
        final PackageManager packageManager = context.getPackageManager();
        final List<WidgetMimeTypeHandlerData> supportedConfigurationActivities =
            new ArrayList<WidgetMimeTypeHandlerData>();

        final Intent supportsIntent =
            new Intent(InstallWidgetReceiver.ACTION_SUPPORTS_CLIPDATA_MIMETYPE);
        supportsIntent.setType(mimeType);

        // Create a set of widget configuration components that we can test against
        final List<AppWidgetProviderInfo> widgets =
            AppWidgetManager.getInstance(context).getInstalledProviders();
        final HashMap<ComponentName, AppWidgetProviderInfo> configurationComponentToWidget =
            new HashMap<ComponentName, AppWidgetProviderInfo>();
        for (AppWidgetProviderInfo info : widgets) {
            configurationComponentToWidget.put(info.configure, info);
        }

        // Run through each of the intents that can handle this type of clip data, and cross
        // reference them with the components that are actual configuration components
        final List<ResolveInfo> activities = packageManager.queryIntentActivities(supportsIntent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo info : activities) {
            final ActivityInfo activityInfo = info.activityInfo;
            final ComponentName infoComponent = new ComponentName(activityInfo.packageName,
                    activityInfo.name);
            if (configurationComponentToWidget.containsKey(infoComponent)) {
                supportedConfigurationActivities.add(
                        new InstallWidgetReceiver.WidgetMimeTypeHandlerData(info,
                                configurationComponentToWidget.get(infoComponent)));
            }
        }
        return supportedConfigurationActivities;
    }

    ShortcutInfo infoFromShortcutIntent(Context context, Intent data, Bitmap fallbackIcon) {
        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        Parcelable bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

        if (intent == null) {
            // If the intent is null, we can't construct a valid ShortcutInfo, so we return null
            Log.e(TAG, "Can't construct ShorcutInfo with null intent");
            return null;
        }

        Bitmap icon = null;
        boolean customIcon = false;
        ShortcutIconResource iconResource = null;

        if (bitmap != null && bitmap instanceof Bitmap) {
            /*icon = Utilities.createIconBitmap(new FastBitmapDrawable((Bitmap)bitmap), context);*/
       /*  Drawable sourceIcon = new BitmapDrawable(context.getResources(),(Bitmap)bitmap);
        	if(sourceIcon!=null){
        		sourceIcon= mUtils2Icon.getCustomerIcon(sourceIcon, Utils2Icon.OUTER_SHADOW);
        	}
        	icon = ((BitmapDrawable)sourceIcon).getBitmap();*/
        	 icon =  mUtils2Icon.getRoundedBitmap((Bitmap)bitmap, false);
        	 icon = Utilities.createIconBitmap(icon, context);
             customIcon = true;
        } else {
            Parcelable extra = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            if (extra != null && extra instanceof ShortcutIconResource) {
                try {
                    iconResource = (ShortcutIconResource) extra;
                    final PackageManager packageManager = context.getPackageManager();
                    Resources resources = packageManager.getResourcesForApplication(
                            iconResource.packageName);
                    final int id = resources.getIdentifier(iconResource.resourceName, null, null);
                    icon = Utilities.createIconBitmap(
                            mIconCache.getFullResIcon(resources, id,false), context);
                } catch (Exception e) {
                    Log.w(TAG, "Could not load shortcut icon: " + extra);
                }
            }
        }

        final ShortcutInfo info = new ShortcutInfo();

        if (icon == null) {
            if (fallbackIcon != null) {
                icon = fallbackIcon;
            } else {
                icon = getFallbackIcon();
                info.usingFallbackIcon = true;
            }
        }
        info.setIcon(icon);

        info.title = name;
        info.intent = intent;
        info.customIcon = customIcon;
        //info.iconResource = iconResource;

        //注意特殊Icon（豌豆夹--游戏Icon）
        /**
         * There is special icon that no pkg, no cmp, no iconResource,
         * only action,intent;
         * 
         * @author ht 2014-08-29
         * */
        ComponentName cmp = intent.getComponent();
        if(cmp == null && iconResource == null){
        	if(info.getPackageName() == null || info.getPackageName().equals("")){
        		ResolveInfo rInfo = context.getPackageManager().resolveActivity(intent, INSTALL_SHORTCUT_SUCCESSFUL);
        		if(rInfo != null && rInfo.activityInfo != null){
        			iconResource = new ShortcutIconResource();
        			iconResource.packageName = rInfo.activityInfo.packageName;
        			info.intent.setComponent(new ComponentName(rInfo.activityInfo.packageName, rInfo.activityInfo.name)); //存入数据库
        			info.intent.setPackage(rInfo.activityInfo.packageName);
        		}
        	}
        }
        ArrayList<ShortcutInfo> allShortcutAndApps = getAllShortcutsAndApps();
        for(ShortcutInfo shortcutInfo : allShortcutAndApps){
				if(shortcutInfo.enableWidgets ==1 && shortcutInfo.appWidgetId != -1 && shortcutInfo.getPackageName().equals(info.getPackageName())){
			        	AppWidgetProviderInfo providerInfo = sortByPkg.get(info.getPackageName()).get(0).providerInfo;
			        	info.appWidgetId = mAppWidgetHost.allocateAppWidgetId();
			    		appWidgetManager.bindAppWidgetIdIfAllowed(info.appWidgetId, providerInfo.provider);
			    		//if( shortcutInfo.enableWidgets ==1 ){
			    		if( info.appWidgetId != -1){
							 info.enableWidgets = 1;
							 info.hostView = mAppWidgetHost.createView(mApp.getApplicationContext(), info.appWidgetId, appWidgetManager.getAppWidgetInfo(info.appWidgetId));
			    		}
						//	}
			    	    ContentValues values = new ContentValues();
			    	    values.put(LauncherSettings.Favorites.APPWIDGET_ID, info.appWidgetId);
			    	    values.put(LauncherSettings.Favorites.ENABLE_WIDGETS, info.enableWidgets);
			    	    mApp.getContentResolver().update(LauncherSettings.Favorites.getContentUri(
			                    info.id, false), values, null, null);	
			    	    Log.v("mywidget", "infoFromShortcutIntent insert---id: "+info.appWidgetId+" packageName"+info.getPackageName());
					break;
				}
		}
        
        info.iconResource = iconResource;

        return info;
    }

    boolean queueIconToBeChecked(HashMap<Object, byte[]> cache, ShortcutInfo info, Cursor c,
            int iconIndex) {
        // If apps can't be on SD, don't even bother.
        if (!mAppsCanBeOnExternalStorage) {
            return false;
        }
        // If this icon doesn't have a custom icon, check to see
        // what's stored in the DB, and if it doesn't match what
        // we're going to show, store what we are going to show back
        // into the DB.  We do this so when we're loading, if the
        // package manager can't find an icon (for example because
        // the app is on SD) then we can use that instead.
        if (!info.customIcon && !info.usingFallbackIcon) {
            cache.put(info, c.getBlob(iconIndex));
            return true;
        }
        return false;
    }
    void updateSavedIcon(Context context, ShortcutInfo info, byte[] data) {
        boolean needSave = false;
        try {
            if (data != null) {
                Bitmap saved = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap loaded = info.getIcon(mIconCache);
                needSave = !saved.sameAs(loaded);
            } else {
                needSave = true;
            }
        } catch (Exception e) {
            needSave = true;
        }
        if (needSave) {
            Log.d(TAG, "going to save icon bitmap for info=" + info);
            // This is slower than is ideal, but this only happens once
            // or when the app is updated with a new icon.
            updateItemInDatabase(context, info);
        }
    }

    /**
     * Return an existing FolderInfo object if we have encountered this ID previously,
     * or make a new one.
     */
    private static FolderInfo findOrMakeFolder(HashMap<Long, FolderInfo> folders, long id) {
        // See if a placeholder was created for us already
        FolderInfo folderInfo = folders.get(id);
        if (folderInfo == null) {
            // No placeholder -- create a new instance
            folderInfo = new FolderInfo();
            folders.put(id, folderInfo);
        }
        return folderInfo;
    }

    public static final Comparator<ApplicationInfo> getAppNameComparator() {
        final Collator collator = Collator.getInstance();
        return new Comparator<ApplicationInfo>() {
            public final int compare(ApplicationInfo a, ApplicationInfo b) {
                int result = collator.compare(a.title.toString(), b.title.toString());
                if (result == 0) {
                    result = a.componentName.compareTo(b.componentName);
                }
                return result;
            }
        };
    }
    public static final Comparator<ApplicationInfo> APP_INSTALL_TIME_COMPARATOR
            = new Comparator<ApplicationInfo>() {
        public final int compare(ApplicationInfo a, ApplicationInfo b) {
            if (a.firstInstallTime < b.firstInstallTime) return 1;
            if (a.firstInstallTime > b.firstInstallTime) return -1;
            return 0;
        }
    };
    public static final Comparator<AppWidgetProviderInfo> getWidgetNameComparator() {
        final Collator collator = Collator.getInstance();
        return new Comparator<AppWidgetProviderInfo>() {
            public final int compare(AppWidgetProviderInfo a, AppWidgetProviderInfo b) {
                return collator.compare(a.label.toString(), b.label.toString());
            }
        };
    }
    static ComponentName getComponentNameFromResolveInfo(ResolveInfo info) {
        if (info.activityInfo != null) {
            return new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
        } else {
            return new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name);
        }
    }
    public static class ShortcutNameComparator implements Comparator<ResolveInfo> {
        private Collator mCollator;
        private PackageManager mPackageManager;
        private HashMap<Object, CharSequence> mLabelCache;
        ShortcutNameComparator(PackageManager pm) {
            mPackageManager = pm;
            mLabelCache = new HashMap<Object, CharSequence>();
            mCollator = Collator.getInstance();
        }
        ShortcutNameComparator(PackageManager pm, HashMap<Object, CharSequence> labelCache) {
            mPackageManager = pm;
            mLabelCache = labelCache;
            mCollator = Collator.getInstance();
        }
        public final int compare(ResolveInfo a, ResolveInfo b) {
            CharSequence labelA, labelB;
            ComponentName keyA = LauncherModel.getComponentNameFromResolveInfo(a);
            ComponentName keyB = LauncherModel.getComponentNameFromResolveInfo(b);
            if (mLabelCache.containsKey(keyA)) {
                labelA = mLabelCache.get(keyA);
            } else {
                labelA = a.loadLabel(mPackageManager).toString();

                mLabelCache.put(keyA, labelA);
            }
            if (mLabelCache.containsKey(keyB)) {
                labelB = mLabelCache.get(keyB);
            } else {
                labelB = b.loadLabel(mPackageManager).toString();

                mLabelCache.put(keyB, labelB);
            }
            return mCollator.compare(labelA, labelB);
        }
    };
    
    //iht-weight排序
    public static class WidgetAndShortcutNameComparator implements Comparator<Object> {
        private Collator mCollator;
        private PackageManager mPackageManager;
        private HashMap<Object, String> mLabelCache;
        
        WidgetAndShortcutNameComparator(PackageManager pm) {
            mPackageManager = pm;
            mLabelCache = new HashMap<Object, String>();
            mCollator = Collator.getInstance();
        }
        
        public final int compare(Object a, Object b) {
            String labelA = "";
            String labelB = "";
            if (mLabelCache.containsKey(a)) {
                labelA = mLabelCache.get(a);
            } else {
               /* labelA = (a instanceof AppWidgetProviderInfo) ?
                    ((AppWidgetProviderInfo) a).label :
                    ((ResolveInfo) a).loadLabel(mPackageManager).toString();
                mLabelCache.put(a, labelA);*/
                
            	//(0:系统应用;1:三方应用)
            	int flag = -1;
            	String pkg = "";
            	try {
                	if(a instanceof AppWidgetProviderInfo){
                		pkg = ((AppWidgetProviderInfo)a).provider.getPackageName();
                		flag = mPackageManager.getPackageInfo(pkg, PackageManager.GET_CONFIGURATIONS).applicationInfo.flags;
                	}else{
                		pkg = ((ResolveInfo)a).activityInfo.packageName;
                		flag = mPackageManager.getPackageInfo(pkg, PackageManager.GET_CONFIGURATIONS).applicationInfo.flags;
                	}
                	//labelA = String.valueOf((flag & android.content.pm.ApplicationInfo.FLAG_SYSTEM)==0?1:0);
                	//mLabelCache.put(a, labelA);
                	
                	flag = flag & android.content.pm.ApplicationInfo.FLAG_SYSTEM;
                	//1:系统应用；0：三方应用
                	labelA = "4";
                	if(flag == 1){
                		labelA = "3";
                		if(pkg.equals("com.aurora.weatherforecast")){
                			labelA = "2";
                		}
                		if(pkg.equals("com.android.deskclock")){
                			labelA = "1";
                		}
                	}
                	mLabelCache.put(a, labelA);
				} catch (Exception e) {
					// TODO: handle exception
				}
            	//AURORA_END
            }
            
            if (mLabelCache.containsKey(b)) {
                labelB = mLabelCache.get(b);
            } else {
                /*labelB = (b instanceof AppWidgetProviderInfo) ?
                    ((AppWidgetProviderInfo) b).label :
                    ((ResolveInfo) b).loadLabel(mPackageManager).toString();
                mLabelCache.put(b, labelB);*/
            	
            	int flag = -1;
            	String pkg = "";
            	try {
					if(b instanceof AppWidgetProviderInfo){
						pkg = ((AppWidgetProviderInfo)b).provider.getPackageName();
                		flag = mPackageManager.getPackageInfo(pkg, PackageManager.GET_CONFIGURATIONS).applicationInfo.flags;
					}else{
						pkg = ((ResolveInfo)b).activityInfo.packageName;
                		flag = mPackageManager.getPackageInfo(pkg, PackageManager.GET_CONFIGURATIONS).applicationInfo.flags;
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
            	//labelB = String.valueOf((flag & android.content.pm.ApplicationInfo.FLAG_SYSTEM)==1?0:1);
            	//mLabelCache.put(b, labelB);
            	
            	flag = flag & android.content.pm.ApplicationInfo.FLAG_SYSTEM;
            	labelB = "4";
            	if(flag == 1){
            		labelB = "3";
            		if(pkg.equals("com.aurora.weatherforecast")){
            			labelB = "2";
            		}
            		if(pkg.equals("com.android.deskclock")){
            			labelB = "1";
            		}
            	}
            	mLabelCache.put(b, labelB);
            	//AURORA_END
            }
            return mCollator.compare(labelA, labelB);
        }
    };

    public void dumpState() {
        Log.d(TAG, "mCallbacks=" + mCallbacks);
        ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList.data", mBgAllAppsList.data);
        ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList.added", mBgAllAppsList.added);
        ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList.removed", mBgAllAppsList.removed);
        ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList.modified", mBgAllAppsList.modified);
        if (mLoaderTask != null) {
            mLoaderTask.dumpState();
        } else {
            Log.d(TAG, "mLoaderTask=null");
        }
    }

    // Aurora <jialf> <2013-09-09> add for loading data begin
	private Workspace mWorkspace;
	public void setWorkSpace(Workspace workSpace) {
		mWorkspace = workSpace;
	}

	private List<ResolveInfo> getAllApps(HashMap<Object, CharSequence> labelCache){
    	final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final PackageManager packageManager = mApp.getPackageManager();
        final List<ResolveInfo> tmpApps = packageManager.queryIntentActivities(mainIntent, 0);
		List<ResolveInfo> allApps = new ArrayList<ResolveInfo>();
		ArrayList<String> allFavorite = LauncherProvider.mFavotite;
		for (ResolveInfo resolveInfo : tmpApps) {
			ComponentName key = getComponentNameFromResolveInfo(resolveInfo);
			if (filterSpecialApp(key.getPackageName()))
				continue;
			mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			mainIntent.setComponent(key);
			
			if (allFavorite != null
					&& allFavorite.contains(mainIntent.toUri(0))) {
				continue;
			}
			allApps.add(resolveInfo);
		}
        Collections.sort(allApps,
                new LauncherModel.ShortcutNameComparator(packageManager, labelCache));
        return allApps;
    }
	
	private void addAllAppsIntoDB(List<ResolveInfo> allApps, int maxScreen,
			int maxCellX, int maxCellY, int size,HashMap<Object, CharSequence> labelCache) {
		if (maxCellX == -1 && maxCellY == -1)
			return;
		maxCellX++;
		ShortcutInfo info = null;
		final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
		/*Launcher callback =(Launcher) getCallback();
		if( callback != null){
       	 sortByPkg = callback.getPkgProviderMap();
       }
		Log.v("mywidget","add apps not in default_workspace");*/
		
		for (int i = 0; i < size; i++) {
			// if cellX == mCellCountX , it means that needed a new row
			if(maxCellX >= mCellCountX) {
				maxCellX = 0;
				maxCellY++;
				// if cellX == mCellCountY , it means that needed a new screen
				if(maxCellY >= mCellCountY) {
					maxScreen++;
					maxCellX = 0;
					maxCellY = 0;
				}
			}
			info = getShortcutInfo(allApps.get(i), maxScreen,
					maxCellX, maxCellY, labelCache);
			// Aurora <jialf> <2013-11-03> modify for fix bug #325 begin
			if (info != null) {
				maxCellX++;
				ContentProviderOperation.Builder builder = ContentProviderOperation
						.newInsert(LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION);
				ContentValues values = addItemToContentValues(mApp, info,
						LauncherSettings.Favorites.CONTAINER_DESKTOP,
						maxScreen, info.cellX, info.cellY);
				
				/*if(mOpenAllMoveupWidgets){
					if(info.appWidgetId != -1 ){
	            		info.enableWidgets = 1;
	            	    addAuroraHostView(info);
	            	    values.put(LauncherSettings.Favorites.ENABLE_WIDGETS, info.enableWidgets);
	            	    Log.v("mywidget", "addAllAppsIntoDB allocateAppWidgetId---: "+info.appWidgetId+", info.hostView="+info.hostView+" packageName"+info.getPackageName());
					}
		        	
		        }*/
				
				builder.withValues(values);
				operationList.add(builder.build());
				/*addItemToDatabase(mApp, info,
						LauncherSettings.Favorites.CONTAINER_DESKTOP,
						maxScreen, info.cellX, info.cellY, false);*/
				
			}
			// Aurora <jialf> <2013-11-03> modify for fix bug #325 end
		}
		try {
			mApp.getContentResolver().applyBatch(LauncherProvider.AUTHORITY, operationList);
		} catch(Exception e) {
			Log.i(TAG, "applyBatch exception ...");
		}
		operationList.clear();
	}
	
	/**
	 * iconcat,query install time of specified package
	 * @param context
	 * @param pkgName
	 * @return if package is not found, return 0
	 */
	private long getPkgFirstInstallTime(Context context, String pkgName) {
    	if(context == null) {
    		return 0;
    	}
    	
    	PackageManager pm = null;
    	pm = context.getPackageManager();
    	
    	if(pm == null) {
    		return 0;
    	}
    
    	PackageInfo pi = null;
        try {
        	pi = pm.getPackageInfo(pkgName, 0);
        } catch (NameNotFoundException e) {
            Log.d("vulcan-db", "getPkgFirstInstallTime: package not found: " + pkgName);
            return 0;
        }

        long firstInstallTime = pi.firstInstallTime;
    	return firstInstallTime;
	}
	
	private ShortcutInfo getShortcutInfo(ResolveInfo resolveInfo, int screen,
			int cellX, int cellY, HashMap<Object, CharSequence> labelCache) {
		// Aurora <jialf> <2013-11-03> add for fix bug #325 begin
		if(resolveInfo == null) 
			return null;
		// Aurora <jialf> <2013-11-03> add for fix bug #325 end
		final PackageManager packageManager = mApp.getPackageManager();
    	Bitmap icon = null;
        final ShortcutInfo info = new ShortcutInfo();
        info.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
        info.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
        
        String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
        ComponentName componentName = new ComponentName(pkgName, resolveInfo.activityInfo.name);
    	final Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		intent.setComponent(componentName);
		info.intent = intent;
		int appWidgetId = -1;
		/*if(sortByPkg.containsKey(pkgName)){
        	AppWidgetProviderInfo providerInfo = sortByPkg.get(pkgName).get(0).providerInfo;
        	appWidgetId = mAppWidgetHost.allocateAppWidgetId();
        	boolean isSuccess = appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, providerInfo.provider);
        	Log.v("mywidget", info.getPackageName()+", bindAppWidgetId: "+isSuccess);
        }else{
        	mWithoutWidgets.add(info);
        	Log.v("mywidget","not contains: "+pkgName);
        }*/
		
        info.appWidgetId = appWidgetId;
       
        info.screen = screen;
        info.cellX = cellX;
        info.cellY = cellY;
        // info.id = mApp.getLauncherProvider().generateNewId();
        
		icon = Utilities.createIconBitmap(mIconCache.getFullResIcon(resolveInfo),mApp);
		// the fallback icon
		if (icon == null) {
			icon = getFallbackIcon();
			info.usingFallbackIcon = true;
		}
		info.setIcon(icon);
		
		// Aurora <jialf> <2013-09-19> add for install/unstall apps begin
		try {
			int appFlags = packageManager.getApplicationInfo(pkgName, 0).flags;
			if ((appFlags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
				info.flags |= ApplicationInfo.DOWNLOADED_FLAG;

				if ((appFlags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
					info.flags |= ApplicationInfo.UPDATED_SYSTEM_APP_FLAG;
				}
			}
		} catch (NameNotFoundException e) {
			Log.d(TAG, "PackageManager.getApplicationInfo failed for " + pkgName);
		}
        // Aurora <jialf> <2013-09-19> add for install/unstall apps end

		LauncherApplication.logVulcan.print("getShortcutInfo: intent = " + info.intent + ", flags = " + info.flags);
		
		// from the resource
        if (resolveInfo != null) {
            // ComponentName key = LauncherModel.getComponentNameFromResolveInfo(resolveInfo);
            if (labelCache != null && labelCache.containsKey(componentName)) {
                info.title = labelCache.get(componentName);
            } else {
                info.title = resolveInfo.activityInfo.loadLabel(packageManager);
                if (labelCache != null) {
                    labelCache.put(componentName, info.title);
                }
            }
        }
        // fall back to the class name of the activity
        if (info.title == null) {
            info.title = componentName.getClassName();
        }
        
        // Aurora <haojj> <2014-1-14> add for 添加对ShortcutInfo的处理 begin
        info.constructSpell();
        // Aurora <haojj> <2014-1-14> end
        
		//iconcat, vulcan added it in 2014-7-11
        if(mApp != null) {
        	info.firstInstallTime = getPkgFirstInstallTime(mApp.getApplicationContext(),pkgName);
        }
        else {
        	info.firstInstallTime = 0;
        }
		return info;
    }
    // Aurora <jialf> <2013-09-09> add for loading data end
	
	public void checkWidgets(){
		Launcher callback =(Launcher) getCallback();
		if( callback != null){
       	 sortByPkg = callback.getPkgProviderMap();
       }
		/*for(ShortcutInfo info : mWithoutWidgets){
			if(info.getPackageName()!=null && sortByPkg.containsKey(info.getPackageName())){
            	AppWidgetProviderInfo providerInfo = sortByPkg.get(info.getPackageName()).get(0).providerInfo;
            	info.appWidgetId = mAppWidgetHost.allocateAppWidgetId();
        		appWidgetManager.bindAppWidgetIdIfAllowed(info.appWidgetId, providerInfo.provider);
        	    ContentValues values = new ContentValues();
        	    values.put(LauncherSettings.Favorites.APPWIDGET_ID, info.appWidgetId);
        	    mApp.getContentResolver().update(LauncherSettings.Favorites.getContentUri(
                        info.id, false), values, null, null);	
        	    Log.v("mywidget", "checkWidgets insert---id: "+info.appWidgetId+" packageName"+info.getPackageName());
            }
		}*/
		//mWithoutWidgets.clear();
	}

    // Aurora <jialf> <2013-09-10> add for dock data begin
	static Cursor queryHotseatData(Context context, String where, String orderBy) {
		final ContentResolver cr = context.getContentResolver();
		return cr.query(LauncherSettings.Favorites.CONTENT_URI, new String[] {
				LauncherSettings.Favorites._ID,
				LauncherSettings.Favorites.CELLX,
				LauncherSettings.Favorites.SCREEN }, where, null, orderBy);
	}
	
	static int updateHotseatCellX(Context context, long id, int cellX,
			int screen,int container) {
		final ContentResolver cr = context.getContentResolver();
		ContentValues values = new ContentValues();
		switch (container) {
		case LauncherSettings.Favorites.CONTAINER_DESKTOP:
			values.put(LauncherSettings.Favorites.CELLX, ++cellX);
			values.put(LauncherSettings.Favorites.SCREEN, ++screen);
			break;
		case LauncherSettings.Favorites.CONTAINER_HOTSEAT:
			values.put(LauncherSettings.Favorites.CELLX, --cellX);
			values.put(LauncherSettings.Favorites.SCREEN, --screen);
			break;
		}
		return cr.update(LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION, values,
				LauncherSettings.Favorites._ID + "=" + id, null);
	}
    // Aurora <jialf> <2013-09-10> add for dock data end
	


    // Aurora <jialf> <2013-09-19> add for install/unstall apps begin
	public int[] findLastPostionView() {
		int[] loc = null;
		int count = mWorkspace.getChildCount();//#1.to get page number
		CellLayout cLayout = null;
		// Aurora <jialf> <2013-11-12> modify for fix bug #418 begin
		int currentPage=0;
		Launcher launcher = null;
		if(mCallbacks != null) {//#2.to get launcher instance
			launcher = (Launcher) mCallbacks.get();
		}
		boolean uninstalling = false;
		if (launcher != null)//#3.to get flag uninstalling
			uninstalling = launcher.isUninstalling();

		LauncherApplication.logVulcan.print(String.format("findLastPostionView: threadid = %s", Thread.currentThread().getName()));
		Log.d("doubleicon",String.format("findLastPostionView: threadid = %s", Thread.currentThread().getName()));
		
		if(count >= 1)	{
			CellLayout lastPage = (CellLayout) mWorkspace.getChildAt(count - 1);
			int lastPageChildCount = lastPage.getShortcutsAndWidgetsChildCount();
			Log.d("vulcan-repeat",String.format("findLastPostionView: lastPageChildCount %d", lastPageChildCount));
		}

		// Aurora <jialf> <2013-11-20> modify for fix bug #158 begin
		int[] dragItemInfo=mWorkspace.getDragItemInfo();
		if (launcher != null && launcher.isEditMode()) {
			currentPage = count - 2;
			LauncherApplication.logVulcan.print(String.format("findLastPostionView: branch1:dragmode = %b", launcher.isDragMode()));
			Log.d("vulcan-repeat",String.format("findLastPostionView: branch1:dragmode = %b, currentPage = %d", launcher.isDragMode(), currentPage));
			if(launcher.isDragMode()) {////#4.case of dragging & editting
				int tempPage = count - 1;
				CellLayout lastCellLayout = (CellLayout) mWorkspace.getChildAt(tempPage);
				boolean lastPageHasChild = lastCellLayout.getShortcutsAndWidgets().getChildCount() > 0;
				CellLayout cellLayout = (CellLayout) mWorkspace.getChildAt(currentPage);
				ShortcutAndWidgetContainer sac = cellLayout.getShortcutsAndWidgets();
				int childCount = sac.getChildCount();
				if (lastPageHasChild) {
					currentPage= tempPage;
				} else if (childCount == 1 && cellLayout.isEmptyCellLayout()) {
					Log.d("vulcan-repeat",String.format("findLastPostionView: one child branch 1, dragScreen = %d,x = %d, y = %d", dragItemInfo[0],dragItemInfo[1],dragItemInfo[2]));
					int[] temp = new int[] { -1, -1 };
					if(dragItemInfo[1] == 3) {
						if(dragItemInfo[2] + 1 <= 4){
							temp[0] = 0;
							temp[1] = dragItemInfo[2] + 1;
							loc = temp;
						}
					} else {
						temp[0] = dragItemInfo[1] + 1;
						temp[1] = dragItemInfo[2];
						loc = temp;
					}
					return loc;
				}
			}
			else {
				LauncherApplication.logVulcan.print(String.format("findLastPostionView: branch forget:count = %d", count));
				Log.d("doubleicon",String.format("findLastPostionView: branch forget:count = %d", count));
			}
		} else if(launcher != null && uninstalling){////#5.case of uninstalling
			LauncherApplication.logVulcan.print(String.format("findLastPostionView: branch2:uninstalling = %b", uninstalling));
			Log.d("doubleicon",String.format("findLastPostionView: branch2:uninstalling = %b", uninstalling));
			CellLayout cellLayout = (CellLayout) mWorkspace.getChildAt(count - 1);
			boolean oneChild = cellLayout.getShortcutsAndWidgets()
					.getChildCount() == 1 && cellLayout.isEmptyCellLayout();
			if (oneChild) {
				Log.d("vulcan-repeat",String.format("findLastPostionView: one child branch 2, dragScreen = %d,x = %d, y = %d", dragItemInfo[0],dragItemInfo[1],dragItemInfo[2]));
				int[] temp = new int[] { -1, -1 };
				if (dragItemInfo[1] == 3) {
					if (dragItemInfo[2] + 1 <= 4) {
						temp[0] = 0;
						temp[1] = dragItemInfo[2] + 1;
						loc = temp;
					}
				} else {
					temp[0] = dragItemInfo[1] + 1;
					temp[1] = dragItemInfo[2];
					loc = temp;
				}
				return loc;
			} else {
				currentPage = count - 1;
			}
		} else {//#6.case of normal
			currentPage = count - 1;
			LauncherApplication.logVulcan.print(String.format("findLastPostionView: branch3:currentPage = %d", currentPage));
			Log.d("doubleicon",String.format("findLastPostionView: branch3:currentPage = %d", currentPage));
		}
		cLayout = (CellLayout) mWorkspace.getChildAt(currentPage);
		 // Aurora <jialf> <2013-11-12> modify for fix bug #418 end
		LauncherApplication.logVulcan.print(String.format("findLastPostionView: normal:currentPage = %d", currentPage));
		Log.d("doubleicon",String.format("findLastPostionView: normal:currentPage = %d", currentPage));
		if (!cLayout.isOccupied(mCellCountX - 1, mCellCountY - 1)) {//#7.case of exception
			
			LauncherApplication.logVulcan.print(String.format("findLastPostionView: branch4:xc=%d,yc=%d", mCellCountX,mCellCountY));
			// the las CellLayout has empty cell,we need find the nearest empty
			// cell,and put the new application shortcut into it
			loc = cLayout.getLastOccupiedCells();
			Log.i("xiejun","loc[0]="+loc[0]+"   ,loc[1]="+loc[1]+"  ,dragItemInfo[0]="+dragItemInfo[0]+"  ,dragItemInfo[1]="+dragItemInfo[1]+"  ,dragItemInfo[2]="+dragItemInfo[2]);
			if (loc != null
					&& dragItemInfo != null
					&& (launcher != null && (launcher.getEditMode() == EditMode.DRAG || uninstalling))
					&& dragItemInfo[0] == currentPage
					&& dragItemInfo[1] == loc[0] && dragItemInfo[2] == loc[1]) {
				loc[0]++;
				if(loc[0]>3){
					loc[1]++;
					if(loc[1]>4) {
						return null;
					}
					loc[0]=0;
				}
			}
		}
		else {
			LauncherApplication.logVulcan.print(String.format("findLastPostionView: branch5:xc=%d,yc=%d", mCellCountX,mCellCountY));
			Log.d("doubleicon",String.format("findLastPostionView: branch5:xc=%d,yc=%d", mCellCountX,mCellCountY));
		}
		return loc;
		// Aurora <jialf> <2013-11-20> modify for fix bug #158 end
	}
    // Aurora <jialf> <2013-09-19> add for install/unstall apps end

	public int[] findLastPostionForDismissFolder() {
		int[] loc = null;
		int count = mWorkspace.getChildCount();
		CellLayout cLayout = null;
		int currentPage=0;
		Launcher launcher = null;
		if(mCallbacks != null) {
			launcher = (Launcher) mCallbacks.get();
		}
		if (launcher != null && launcher.isEditMode()) {
			cLayout = (CellLayout) mWorkspace.getChildAt(count - 1);
			if (cLayout.getShortcutsAndWidgets().getChildCount() == 0
					&& count > 1) {
				currentPage = count - 2;
			} else {
				currentPage = count - 1;
			}
		} else {
			currentPage=count-1;
		}
		Log.i(TAG, "[ " + count +", " +currentPage+" ]");
		cLayout = (CellLayout) mWorkspace.getChildAt(currentPage);
		if (!cLayout.isOccupied(mCellCountX - 1, mCellCountY - 1)) {
			// the las CellLayout has empty cell,we need find the nearest empty
			// cell,and put the new application shortcut into it
			loc = cLayout.getLastOccupiedCells();
		}
		return loc;
	}
	
	// Aurora <jialf> <2013-10-02> add for dismiss folder begin
	public Cursor queryItemInfoInFolder(long id) {
		String where = LauncherSettings.Favorites.CONTAINER + "=" + id;
		Cursor cursor = mApp.getContentResolver().query(
				LauncherSettings.Favorites.CONTENT_URI,
				new String[] { LauncherSettings.Favorites._ID }, where, null,
				null);
		return cursor;
	}
	// Aurora <jialf> <2013-10-02> add for dismiss folder end

	// Aurora <jialf> <2013-12-19> add for fix bug #1341 begin
	public long queryAuroraFolderContainer(long id) {
		String where = LauncherSettings.Favorites._ID + "=" + id;
		Cursor cursor = mApp.getContentResolver().query(
				LauncherSettings.Favorites.CONTENT_URI,
				new String[] { LauncherSettings.Favorites.CONTAINER }, where,
				null, null);
		long fId = -1;
		if (cursor != null) {
			while (cursor.moveToNext()) {
				fId = cursor.getLong(cursor.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER));
			}
		}
		cursor.close();
		return fId;
	}
	// Aurora <jialf> <2013-12-19> add for fix bug #1341 end
	
	// Aurora <jialf> <2013-10-08> add for Dock data begin
	private static int mHotseatChildCount;

	public static int getmHotseatChildCount() {
		return mHotseatChildCount;
	}
	// Aurora <jialf> <2013-10-08> add for Dock data end
	
	// Aurora <jialf> <2013-10-21> add for fix bug #22 && # 140 begin
	private List<String> getAuroraAllApps() {
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		final PackageManager packageManager = mApp.getPackageManager();
		List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
		List<String> allAppIntents = new ArrayList<String>();
		for(ResolveInfo resolveInfo : apps) {
			String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
			if (filterSpecialApp(pkgName))
				continue;
	        ComponentName componentName = new ComponentName(pkgName, resolveInfo.activityInfo.name);
	    	final Intent intent = new Intent(Intent.ACTION_MAIN, null);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			intent.setComponent(componentName);
			allAppIntents.add(intent.toUri(0));
			LauncherApplication.logVulcan.print("getAuroraAllApps: add a package: " + pkgName);
		}
		return allAppIntents;
	}
	
	private void addAuroraNewApps(final List<String> newApps,
			PackageManager manager, HashMap<Object, CharSequence> labelCache){
    	if (newApps == null || newApps.isEmpty()) return;
		int screen = -1;
		int cellX = -1;
		int cellY = -1;
		if (sBgItemsIdMap.size() > 0) {
			for (ItemInfo info : sBgItemsIdMap.values()) {
				if (info.container == -100) {
					boolean swap = false;
					if(info instanceof LauncherAppWidgetInfo) {
						LauncherAppWidgetInfo lawi = (LauncherAppWidgetInfo)info;
						int tmpCellX = lawi.cellX + lawi.spanX - 1;
						int tmpCellY = lawi.cellY + lawi.spanY - 1;
						swap = (info.screen == screen)
								&& ((tmpCellY == cellY && tmpCellX > cellX) || tmpCellY > cellY);
						// Aurora <jialf> <2013-12-19> modify for fix bug #1318 begin
						if(info.screen > screen || swap) {
						// Aurora <jialf> <2013-12-19> modify for fix bug #1318 end
							screen = info.screen;
							cellX = tmpCellX;
							cellY = tmpCellY;
						}
					} else {
						swap = (info.screen == screen)
								&& ((info.cellY == cellY && info.cellX > cellX)|| info.cellY > cellY);
						if(info.screen > screen || swap) {
							screen = info.screen;
							cellX = info.cellX;
							cellY = info.cellY;
						}
					}
				}
				LauncherApplication.logVulcan.print(String.format("addAuroraNewApps: title = %s, screen=%d, x =%d,y=%d",
						info.title,info.screen,info.cellX,info.cellY ));
			}
			Log.i(TAG, "screen = " + screen +", cellX = " + cellX +", cellY = " + cellY);
			List<ResolveInfo> list = new ArrayList<ResolveInfo>();
			for(String data : newApps) {
				try {
					Intent in = Intent.parseUri(data, 0);
					ResolveInfo rInfo = manager.resolveActivity(in , 0);
					list.add(rInfo);
                } catch (URISyntaxException e) {
                    continue;
                }
			}
			LauncherApplication.logVulcan.print(String.format("addAuroraNewApps: list.size= %d,newApp.size=%d", list.size(), newApps.size()));
			addAllAppsIntoDB(list, screen, cellX, cellY, newApps.size(), labelCache);
			newApps.clear();
		}
    }

	private boolean updateAuroraRemoveApps(List<Long> removeIds,
			ContentResolver contentResolver) {
		if (removeIds.isEmpty())
			return false;
		ShortcutInfo info;
		Intent intent;
		boolean hasFromHotseat = false;
		final PackageManager manager = mApp.getPackageManager();
		for(long id : removeIds) {
			info = (ShortcutInfo) sBgItemsIdMap.get(id);
			if(info != null) {
				intent = info.intent;
				ComponentName oldComponent = intent.getComponent();
				String pkgName = oldComponent.getPackageName();
				if (mSdcardApps != null) {
					boolean exists = false;
					inner:for(String pkg : mSdcardApps) {
						if(pkg.equals(pkgName)) {
							exists = true;
							break inner;
						}
					}
					if(exists)
						continue;
				}
		        Intent newIntent = new Intent(intent.getAction(), null);
		        newIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		        newIntent.setPackage(pkgName);
		        List<ResolveInfo> infos = manager.queryIntentActivities(newIntent, 0);
		        // Aurora <jialf> <2013-11-18> modify for fix app update but cls has been changed begin
		        ResolveInfo resolveInfo = null;
		        int size = infos.size();
				if (size > 0) {
					for (ResolveInfo i : infos) {
						ComponentName cn = new ComponentName(
								i.activityInfo.packageName, i.activityInfo.name);
						if (cn.equals(oldComponent)) {
							resolveInfo = i;
						}
					}
			        // Aurora <jialf> <2014-01-08> modify for fix bug #1769 begin
					/*if (resolveInfo == null) {
						resolveInfo = manager.resolveActivity(intent, 0);
					}*/
			        // Aurora <jialf> <2014-01-08> modify for fix bug #1769 end
				}
		        if(size == 0 || resolveInfo == null) {
					if (!hasFromHotseat
							&& info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
						hasFromHotseat = true;
					}
					if (info.container > 0) {
						FolderInfo fInfo = sBgFolders.get(info.container);
						fInfo.contents.remove(info);
					} else {
						sBgWorkspaceItems.remove(info);
					}
		        	sBgItemsIdMap.remove(id);
					LauncherApplication.logVulcan.print("vulcan,to delete an app,id = " + id);
		        	contentResolver.delete(LauncherSettings.Favorites.getContentUri(
	                        id, false), null, null);
		        }
		        // Aurora <jialf> <2013-11-18> modify for fix app update but cls has been changed end
			}
		}
		removeIds.clear();
		return hasFromHotseat;
	}
	
	private boolean updateAuroraRemoveFolderApps(List<Long> removeApps) {
		if (removeApps.isEmpty())
			return false;
		boolean needResetHotseat = false;
		for (long delContainer : removeApps) {
			FolderInfo fInfo = sBgFolders.get(delContainer);
			if (fInfo != null) {
				int size = fInfo.contents.size();
				if (size > 0 && size < 2) {
					ShortcutInfo sInfo = fInfo.contents.get(0);
					if (sInfo != null) {
						long container = fInfo.container;
						int screen = fInfo.screen;
						int cellX = fInfo.cellX;
						int cellY = fInfo.cellY;
						sInfo.container = container;
						sInfo.screen = screen;
						sInfo.cellX = cellX;
						sInfo.cellY = cellY;
						sBgItemsIdMap.remove(fInfo.id);
						sBgFolders.remove(fInfo);
						modifyItemInDatabase(mApp, sInfo, container, screen,
								cellX, cellY, 1, 1);
						deleteFolderContentsFromDatabase(mApp, fInfo);
					}
				} else if(size == 0) {
					if (!needResetHotseat
							&& fInfo.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
						needResetHotseat = true;
					}
					deleteFolderContentsFromDatabase(mApp, fInfo);
				}
			}
		}
		removeApps.clear();
		return needResetHotseat;
	}
	
	private void updateHotseatApps(boolean needUpdate) {
		if(needUpdate) {
			String where = LauncherSettings.Favorites.CONTAINER + "="
					+ LauncherSettings.Favorites.CONTAINER_HOTSEAT;
			Cursor cursor = queryHotseatData(mApp, where, LauncherSettings.Favorites.CELLX);
			if(cursor == null) return;
			int count = cursor.getCount();
			mHotseatChildCount = count;
			int idIndex = cursor.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
			ShortcutInfo sInfo = null;
			long id;
			int i = 0;
			while (cursor.moveToNext()) {
				id = cursor.getInt(idIndex);
				ItemInfo item = sBgItemsIdMap.get(id);
				if (item != null) {
					item.cellX = i;
					item.screen = i;
				}
				updateHotseatData(mApp, id, i, i);
				i++;
			}
			cursor.close();
		}
	}
	
	static int updateHotseatData(Context context, long id, int cellX, int screen) {
		final ContentResolver cr = context.getContentResolver();
		ContentValues values = new ContentValues();
		values.put(LauncherSettings.Favorites.CELLX, cellX);
		values.put(LauncherSettings.Favorites.SCREEN, screen);
		return cr.update(LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION, values,
				LauncherSettings.Favorites._ID + "=" + id, null);
	}
	// Aurora <jialf> <2013-10-21> add for fix bug #22 && # 140 end
	
	static ContentValues modifyHotseatChild(int cellX) {
		ContentValues values = new ContentValues();
		values.put(LauncherSettings.Favorites.SCREEN, cellX);
		values.put(LauncherSettings.Favorites.CELLX, cellX);
		return values;
	}

	// Aurora <jialf> <2013-12-23> add for fix bug #1199 begin
	public int getInstallShortcutScreen() {
		int screen = -1;
		Launcher launcher = null;
		if(mCallbacks != null)
			launcher = (Launcher) mCallbacks.get();
		if (launcher != null && launcher.isEditMode()) {
			int count = mWorkspace.getChildCount();
			if(launcher.isDragMode()) {
				CellLayout lastCellLayout = (CellLayout) mWorkspace
						.getChildAt(count - 1);
				boolean lastPageHasChild = lastCellLayout
						.getShortcutsAndWidgets().getChildCount() > 0;
				if (lastPageHasChild) {
					screen = count - 1;
				} else {
					screen = count - 2;
				}
			} else {
				screen = count - 2;
			}
		} else {
			screen = mWorkspace.getChildCount() - 1;
		}
		return screen;
	}
	// Aurora <jialf> <2013-12-23> add for fix bug #1199 end
	
	private boolean mAuroraExternalReady;

	public boolean ismAuroraExternalReady() {
		return mAuroraExternalReady;
	}
	
    private ArrayList<String> mRemovedWidgetPackage = new ArrayList<String>();
    
	/**
	 * vulcan created this method in 2014-6-26
	 * this method add a DynIconPlg into a list.
	 * 1.
	 * @author vulcan
	 * @return probably return null, so caller must check if it is null.
	 */
    public Callbacks getCallback() {
    	if(mCallbacks == null) {
    		return null;
    	}
    	return mCallbacks.get();
    }
    
    /**
     * vulcan created this method in 2014-8-4
     * @param oldCb
     * @return
     */
    public Callbacks tryGetCallback(Callbacks oldCb) {
    	if(mCallbacks == null) {
    		return null;
    	}
    	
    	Callbacks newCb = mCallbacks.get();
    	if(newCb == null) {
    		return null;
    	}
    	
    	if(newCb != oldCb) {
    		return null;
    	}
    	
    	return newCb;
    }
    
    /**get content array for update column*/
    //iht 2014-11-25
    //public ContentValues[] getUpdateContentArray(HashMap<Long, ItemInfo> sBgItemsIdMap){
    public ContentValues[] getUpdateContentArray(ConcurrentHashMap<Long, ItemInfo> sBgItemsIdMap){
    	if(null!=sBgWorkspaceItems){
    		int j = 0;
    		ContentValues[] cv = new ContentValues[sBgItemsIdMap.size()];
   
    		for(ItemInfo item : sBgItemsIdMap.values()){
    			cv[j] = new ContentValues(); 
    			String titleStr = item.title != null ? item.title.toString().replace(' ', ' ').trim() : null;
                String[] pinyinArray = Utils.getFullPinYin(titleStr);
                if(pinyinArray != null && null!=titleStr) {
                	String  fullPinyin = pinyinArray[0];
                	String simplePinyin = pinyinArray[1];
                	cv[j].put(LauncherSettings.BaseLauncherColumns.TITLE, titleStr);
                	cv[j].put(LauncherSettings.BaseLauncherColumns.FULL_PINYIN, fullPinyin);
                	cv[j].put(LauncherSettings.BaseLauncherColumns.SIMPLE_PINYIN, simplePinyin);
        	       // Log.e("linp", "#######updateColumn titleStr="+titleStr+";"+"fullPinyin="+fullPinyin+";"+"simplePinyin="+simplePinyin+";"+"\n");
                }
                j++;
    		}
    		return cv;
    	}
    	return null;
    }
    
    /**get uri array for update column*/
    //iht 2014-11-25
    //public Uri[] getUpdateUriArray(HashMap<Long, ItemInfo> sBgItemsIdMap){
    public Uri[] getUpdateUriArray(ConcurrentHashMap<Long, ItemInfo> sBgItemsIdMap){
    	int j = 0;
    	if(null!=sBgItemsIdMap){
    		Uri[] url = new Uri[sBgItemsIdMap.size()];
    		for(ItemInfo item : sBgItemsIdMap.values()){
    			url[j] = LauncherSettings.Favorites.getContentUri(item.id, false);
    			j++;
    		}
    		return url;
    	}
    	return null;
    }
    
    private boolean filterSpecialApp(String pkg){
    	if("com.android.stk".equals(pkg)){
    		return true;
    	}
    	if("com.android.utk".equals(pkg)){
    		return true;
    	}
    	return false;
    }
    
    /**assign a screen to filter proper application*/
	public ArrayList<ItemInfo> filterProperApp(
			ArrayList<ItemInfo> sBgItemsIdMap, int screen) {
		ArrayList<ItemInfo> list = new ArrayList<ItemInfo>();		
		for (ItemInfo info : sBgItemsIdMap) {
			if (info.screen != screen
					&& info.container != LauncherSettings.Favorites.CONTAINER_HOTSEAT
					&&( info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION || info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT)
					&& info instanceof ShortcutInfo) {
				if( info instanceof ShortcutInfo){
					int flag  = ((ShortcutInfo)info).flags;
					if ((flag& android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0 ||info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT ) {
						list.add(info);
					}
				}		
			}
		}
		return list;
	}
	
	/**classify the List's item type (game,or app)*/
	public ArrayList<ItemInfo> getFilterAppListItemType(ArrayList<ItemInfo> ListItem,ArrayList<ShortcutInfo> AppList,ArrayList<ShortcutInfo> GameList,ArrayList<ShortcutInfo>OtherList) {
		final ContentResolver cr = mApp.getContentResolver();
		final Launcher launcher = (Launcher) mCallbacks.get();

		String[] projection = new String[] { "id", "title", "package_name",
				"app_type", "category_name" };

		for (ItemInfo info : ListItem) {
			if (info instanceof ShortcutInfo) {
				if (paddingCategoryInfo((ShortcutInfo) info) || checkingCategoryLabel((ShortcutInfo) info)) {
					// TODO 2.2.2 referenced from xiejun's document
					if (!isUnderExistFolderAndAdd((ShortcutInfo) info, null))
						writeClassifyAppToEachList((ShortcutInfo) info,AppList, GameList);
				} else {
					// Log.e("linp", "could't find any data from app_category table where tile Name is "+((ShortcutInfo)info).title+";"+"pkgName = "+((ShortcutInfo)info).getPackageName());
					if (!isUnderExistFolderAndAdd((ShortcutInfo) info,launcher.getString(R.string.classify_names_other))) {
						OtherList.add((ShortcutInfo) info);
					}
				}
			}
		} // end of for loop
		return ListItem;
	}
	
	
	private boolean isUnderExistFolderAndAdd(ShortcutInfo info,String folderName){
		boolean ret = false;
		if (clsApp != null) {
			ArrayList<FolderInfo> compareList = new ArrayList<FolderInfo>();
			List<String> cateList = clsApp.getCateName(info);
			if(folderName!=null){
				 iteratesBgFoldersNameAndMatch(folderName,compareList);
				 if(compareList.size()<=0){
					 return false;
				 }
			}else {
				if (cateList.size() > 0) {
					for (String cateName : cateList) {
						iteratesBgFoldersNameAndMatch(cateName,compareList);
				}
			}
			}
			FolderInfo folderInfo = getMaxSizeFolderContent(compareList, info);
			if (folderInfo != null) {
				folderInfo.add(info);
				ret = true;
			}
		
	 }
		return ret;
	}
	private ArrayList<FolderInfo> iteratesBgFoldersNameAndMatch(String folderName,ArrayList<FolderInfo> compareList){
		for(FolderInfo folder : sBgFolders.values()){
			if(folder.title!=null){
				if(folder.title.equals(folderName) /*&& folder.contents.size()<80*/){
					compareList.add(folder);
				}
			}else{
				Log.e("linp", "+++++++++++++++++++ToXuMin folder.title=NULL");
			}

		}
		return compareList;
	}
		
	private FolderInfo getMaxSizeFolderContent(ArrayList<FolderInfo> compareList,ShortcutInfo info ){
		final Launcher launcher = (Launcher) mCallbacks.get();
/*		if(info.appType == null ||(compareList.size() <= 0 && !info.appType.equals("GAME"))){
			return iteratesBgFoldersNameAndMatch(launcher.getString(R.string.classify_names_other));
		}
		else*/ if(compareList.size() == 1){
		    return compareList.get(0);	
		}else if(compareList.size()>1){
			Log.e("linp", "######################getMaxSizeFolderContent compareList.size()>1");
			Collections.sort(compareList, new FolderContentComparator());
			 return compareList.get(0);
		}
		return null;
	}
	
	/**Comparator to compare each content size and return the most biggest content size info*/
	class FolderContentComparator implements Comparator<FolderInfo> {
		@Override
		public int compare(FolderInfo arg0, FolderInfo arg1) {
			// TODO Auto-generated method stub
			return arg1.contents.size() - arg0.contents.size();
		}
	}
	
	/**padding shortcut some info according to cursor's given values*/
	private boolean paddingCategoryInfo(ShortcutInfo info){
		boolean ret = false;
		final ContentResolver cr = mApp.getContentResolver();
		String[] params=new String[]{String.valueOf(((ShortcutInfo)info).getPackageName())};  
		String[] projection  = new String[] {"id","title","package_name","app_type","category_name" };
		Cursor c = cr.query(Uri.parse("content://" + LauncherProvider.AUTHORITY + "/"+ LauncherProvider.TABLE_APP_CATEGORY), projection, "package_name=?",params, null);
		if (null != c) {
			c.moveToFirst();
			if(c.getCount()>0){
				int id = c.getInt(c.getColumnIndex("id"));
				String type = c.getString(c.getColumnIndex("app_type"));
				String cateName = c.getString(c.getColumnIndex("category_name"));
				info.clsid = id;
				info.appType = type;
				info.cateName = cateName;
				ret = true;
				//Log.e("hazel", "Content : ["+"title="+info.title+";"+"appType="+info.appType+";"+"cateName="+info.cateName+"]"+"\r");
			}
		}
		c.close();
		return ret;
	}
	
	/**start classify app invoke by launcher*/
	public void classifyApp(){
		// <xiangzx> <2015-09-11> add for fix bug #16203 begin
		resetLoadedState(false, false);
		mWorkspaceLoaded = true;
		// <xiangzx> <2015-09-11> add for fix bug #16203 end
		mApp.getLauncherProvider().bulkBackupTable(LauncherProvider.TABLE_FAVORITES, LauncherProvider.TABLE_FAVORITES_BACKUP);
		//TODO start classify App and will add some condition later (24hours App)
		AppList.clear();
		GameList.clear();
		OtherList.clear();
		clsApp.clear();
		final Launcher launcher = (Launcher) mCallbacks.get();
		if(launcher != null){
		 ArrayList<ItemInfo> itemInfo =  getFilterAppListItemType(filterProperApp(sBgWorkspaceItems,0),AppList,GameList,OtherList);
		 Log.e("linp", "##############AppList.size="+AppList.size()+";"+"GamList.size="+GameList.size()+";"+"OtherList.size="+OtherList.size());
		 clsApp.setOtherListObject(OtherList);
		 clsApp.setNeedClassifyList(AppList);
		 clsApp.classifyAppCategory();
		 classifyGameList(GameList);
		 if(OtherList.size() == 1){
				isUnderExistFolderAndAdd(OtherList.get(0),launcher.getString(R.string.classify_names_other));
		 }
		}else{
			Log.e("linp", "classifyApp,launcher=null");
		}
		reLoadWorkspaceforClassify = true;
		startLoader(true,-1);
		
	}
	
	/**classify game list and add some logic according to the requirement and referenced from xiejun's documents */
	private void classifyGameList(ArrayList<ShortcutInfo> GameList){
		final Launcher launcher = (Launcher)tryGetCallback(mCallbacks.get());
		
		ArrayList<FolderInfo> compareList = new ArrayList<FolderInfo>();
		FolderInfo info = null;
		 iteratesBgFoldersNameAndMatch(launcher.getString(R.string.classify_names_game),compareList);
		 if(compareList.size() == 1){
			  info  = compareList.get(0);
		 }
		 int folderConentSize = 0;
		 int GameListSize = GameList.size();
		 if(info!=null){
			 folderConentSize = info.contents.size();
		 }
		 if((folderConentSize +GameListSize)<24 && GameListSize<16){
			 if(null == info){
//				 clsApp.folderList.put(launcher.getString(R.string.classify_names_game), GameList);
				 clsfolderInfo folderInfo = new clsfolderInfo();
				 folderInfo.folderName = launcher.getString(R.string.classify_names_game);
				 folderInfo.folderContent = GameList;
				 clsApp.mfolderList.add(folderInfo);
			 }else{
				 for(ShortcutInfo scInfo : GameList){
					 info.add(scInfo);
				 }
			 }
		 }else{
				clsApp.setNeedClassifyList(GameList);
				clsApp.classifyAppCategory();
		 }
	}
	
    /**
     * @desc :load new workspace after classify operation completed
     * */
    public void loadClassifyWorkspace(){
     //TODO adjust sBgItemsIdMap,sBgWorkspaceItems,sBgFolders content
    	ArrayList<ItemInfo> sortBgWorkspaceItemList = new ArrayList<ItemInfo>();
		final Launcher launcher = (Launcher) mCallbacks.get();

		for (clsfolderInfo info: clsApp.mfolderList) {
			List<ShortcutInfo> listFolderContent =info.folderContent;
			if(listFolderContent.size()>1){ //content size must larger than two  
				ShortcutInfo tmpInfo = listFolderContent.get(0); 
			launcher.addFolder(info.folderName, tmpInfo, listFolderContent);
			}
		}
			if(OtherList.size()>1){ 
				launcher.addFolder(launcher.getString(R.string.classify_names_other), OtherList.get(0), OtherList);
			}

		sortsBgAppWidgets(0);
		//TODO start re-sort item position	
		for(ItemInfo info : sBgWorkspaceItems){
			if(info.container == LauncherSettings.Favorites.CONTAINER_DESKTOP && info.screen !=0){
				sortBgWorkspaceItemList.add(info);
			}
		}
		sortBgWorkspaceItem(sortBgWorkspaceItemList);
		updateHotseatApps(true);
	}
    
	public void fallbackToOriginal() {
		resetLoadedState(true, true); 
		mApp.getLauncherProvider().bulkBackupTable(
				LauncherProvider.TABLE_FAVORITES_BACKUP,
				LauncherProvider.TABLE_FAVORITES);
		startLoader(true, -1);
	}
	
	/**@param c: get data from cursor and store some of  data to shortcut info item and each list will add it*/
	public void writeClassifyAppToEachList(ShortcutInfo info,ArrayList<ShortcutInfo> AppList,ArrayList<ShortcutInfo> GameList){
		//TODO we must distinguish between  App type or Game type.but just demo time .so noted it
		if(info.appType.equals("APP")){
			AppList.add(info);
		}else{
			//TODO game type and will handle 2.2.3 problem 
			GameList.add(info);
		}
	}
	
	
	/**sort list and reset each item's position*/
	private void sortBgWorkspaceItem(ArrayList<ItemInfo> list) {
		int CellX = 0;
		int CellY = 0;
		int screen = 1;
		for (ItemInfo info : list) {
			if (CellX > 3) {
				CellX = 0;
				CellY++;
			}
			if (CellY > 4) {
				CellX = CellY = 0;
				screen++;
			}
			info.cellX = CellX;
			info.cellY = CellY;
			info.screen = screen;
			CellX++;
		}
		updateBgWorkspaceItemInDatabase(list);
	}
	
	public void sortsBgAppWidgets(int screenCount){
		ArrayList<ItemInfo> sBgWidgetMatchItem = new ArrayList<ItemInfo>();
		HashMap<Integer,ArrayList<ItemInfo>> pages = apm.createPagesFromWorkspaceItems(sBgItemsIdMap);
	    int maxIndex = apm.getMaxIndexOfPageHolder(pages);
		Log.e("linp", "###################sBgAppWidgets.size="+sBgAppWidgets.size());
		for(ItemInfo info: sBgAppWidgets){
			maxIndex = maxIndex + 1;
			if(info.screen!=0){
				info.screen = maxIndex;
				sBgWidgetMatchItem.add(info);
			}
		}
		updateBgAppWidgetsItemIndDatabase(sBgWidgetMatchItem);
	}
	
	public void updateBgAppWidgetsItemIndDatabase(ArrayList<ItemInfo> sBgWidgetMatchItem){
		Log.e("linp", "sBgWidgetMatchItem.size="+sBgWidgetMatchItem.size());
		LauncherProvider lp = mApp.getLauncherProvider();
		if (null == lp) {
			Log.e("linp", "updateBgAppWidgetsItemIndDatabase lp is null!");
			return;
		}
		DatabaseHelper dbHelper = lp.getDatabaseHelper();
		if (null == dbHelper) {
			Log.e("linp", "updateBgAppWidgetsItemIndDatabase dbHelper is null!");
			return;
		}
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		if (null == db) {
			Log.e("linp", "updateBgAppWidgetsItemIndDatabase db is null!");
			return;
		}
		db.beginTransaction();
		try {
			// TODO update sBgWorkspaceItems
			for(final ItemInfo info : sBgWidgetMatchItem){
				Runnable r = new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						  final long itemId = info.id;
						  final Uri uri = LauncherSettings.Favorites.getContentUri(itemId, false);
					      final ContentValues values = new ContentValues();
					        values.put(LauncherSettings.Favorites.SCREEN, info.screen);
					        SqlArguments args = new SqlArguments(uri, null, null);
					        db.update(args.table, values, args.where,
									args.args);
					}
				};
				runOnWorkerThread(r);
			}		
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			Log.e("linp", "updateBgAppWidgetsItemIndDatabase ok!");
		}
	}
	
	
	private void updateBgWorkspaceItemInDatabase(
			ArrayList<ItemInfo> sBgWorkspaceItems) {
		
		LauncherProvider lp = mApp.getLauncherProvider();
		if (null == lp) {
			Log.e("linp", "updateBgWorkspaceItemInDatabase lp is null!");
			return;
		}
		DatabaseHelper dbHelper = lp.getDatabaseHelper();
		if (null == dbHelper) {
			Log.e("linp", "updateBgWorkspaceItemInDatabase dbHelper is null!");
			return;
		}
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		if (null == db) {
			Log.e("linp", "updateBgWorkspaceItemInDatabase db is null!");
			return;
		}
		db.beginTransaction();
		try {
			// TODO update sBgWorkspaceItems
			for(final ItemInfo info : sBgWorkspaceItems){
				Runnable r = new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						  final long itemId = info.id;
						  final Uri uri = LauncherSettings.Favorites.getContentUri(itemId, false);
					      final ContentValues values = new ContentValues();
					        values.put(LauncherSettings.Favorites.CELLX, info.cellX);
					        values.put(LauncherSettings.Favorites.CELLY, info.cellY);
					        values.put(LauncherSettings.Favorites.SCREEN, info.screen);
					        SqlArguments args = new SqlArguments(uri, null, null);
					        db.update(args.table, values, args.where,
									args.args);
					}
				};
				runOnWorkerThread(r);
			}		
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			Log.e("linp", "updateBgWorkspaceItemInDatabase ok!");
		}

	}
    
	public void writeDefaultLanguage(SharedPreferences sp,String currentLanguage){
    	 SharedPreferences.Editor editor = sp.edit();
    	 editor.putString(LauncherProvider.DEFAULT_LANGUAGE, currentLanguage);
         editor.commit();
    }
    
	/**get folder Name when drag source shortcut info overlaps another one .and will select it's common tags and types
	 * @param source info is the one,should not be null
	 * @param dest info is the other one,should not be null 
	 * */
	public String getFolderNameByCommonTag(ShortcutInfo sourceInfo,ShortcutInfo destInfo){
		final Launcher launcher = (Launcher)tryGetCallback(mCallbacks.get());
		String folderName = launcher.getString(R.string.folder_name);
		if (sourceInfo.flags == 0
				&& destInfo.flags == 0
				&& (sourceInfo.itemType == LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION && sourceInfo.itemType == LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION)) {
			folderName = launcher.getString(R.string.folder_name_system);
			return folderName;
		}
		boolean source = paddingCategoryInfo(sourceInfo);
		boolean dest = paddingCategoryInfo(destInfo);
		
		if(!source){
			checkingCategoryLabel(sourceInfo);
		}else if(!dest){
			checkingCategoryLabel(destInfo);
		}
    
		if(sourceInfo.cateName == null || destInfo.cateName == null){
			return folderName;
		}
		
		List<String> sourceList = Utilities.splitString(sourceInfo.cateName, ",");
		List<String> destList = Utilities.splitString(destInfo.cateName, ",");
		for(String str: sourceList){
			if(destList.contains(str)){
				return str;
			}
		}
		if(sourceInfo.appType.equals(destInfo.appType)){
			if(sourceInfo.appType.equals("APP")){
				folderName = launcher.getString(R.string.folder_name_app);
			}else{
				folderName = launcher.getString(R.string.folder_name_game);
			}
		}
		return folderName;
	}
    
	/**checking category info 's label if package name changes 
	 * @author hazel
	 * @date  2014/11/28
	 * */
	private boolean checkingCategoryLabel(ShortcutInfo info) {
		final ContentResolver cr = mApp.getContentResolver();
		boolean ret = false;
		String[] params = new String[] { String.valueOf(((ShortcutInfo) info)
				.getTitie()) };
		String[] projection = new String[] { "id","title","app_type","category_name" };
		Cursor c = cr.query(
				Uri.parse("content://" + LauncherProvider.AUTHORITY + "/"
						+ LauncherProvider.TABLE_APP_CATEGORY), projection,
				"title=?", params, null);
		if (null != c) {
			c.moveToFirst();
			if (c.getCount() > 0) {
				String title = c.getString(c.getColumnIndex("title"));
				if (info.title.equals(title.trim())) {
					int id = c.getInt(c.getColumnIndex("id"));
					String type = c.getString(c.getColumnIndex("app_type"));
					String cateName = c.getString(c.getColumnIndex("category_name"));
					info.clsid = id;
					info.appType = type;
					info.cateName = cateName;
					ret  = true;
				}
			}
		}
		c.close();
		return ret;
	}
 
    
    class AutoPageMananger {
        /**
         * vulcan created this method in 2014-8-13
         * this method create page index for the items on the desktop
         * @param items items should not be null.
         * @return return value is always not null.
         */
    	//iht 2014-11-25
        //private HashMap<Integer,ArrayList<ItemInfo>> createPagesFromWorkspaceItems(final HashMap<Long, ItemInfo> items) {
    	private HashMap<Integer,ArrayList<ItemInfo>> createPagesFromWorkspaceItems(final ConcurrentHashMap<Long, ItemInfo> items) {
        	HashMap<Integer,ArrayList<ItemInfo>> itemListHolders = new HashMap<Integer,ArrayList<ItemInfo>>();
        	ArrayList<ItemInfo> itemList = null;
        	for(ItemInfo deskItem: items.values()) {
        		
        		if(deskItem.container != LauncherSettings.Favorites.CONTAINER_DESKTOP) {
        			continue;
        		}

        		int pageIndex = deskItem.screen;
        		itemList = itemListHolders.get(pageIndex);
        		if(itemList == null) {
        			//create new page in itemListHolders
        			itemList = new ArrayList<ItemInfo>();
        			itemListHolders.put(pageIndex, itemList);
        		}
        		itemList.add(deskItem);
        	}

        	return itemListHolders;
        }
        
        /**
         * get the max page index from itemListHolders
         * @param itemListHolders. itemListHolders should not be null!
         * @return return -1 if the itemListHolders is empty
         */
        private int getMaxIndexOfPageHolder(HashMap<Integer,ArrayList<ItemInfo>> itemListHolders) {
        	int maxIndex = -1;
        	for(Integer index: itemListHolders.keySet()) {
        		if(index > maxIndex) {
        			maxIndex = index;
        		}
        	}
        	return maxIndex;
        }
        
        /**
         * vulcan created this method in 2014-8-15
         * this method adjust item info of a page in the five global variables
         */
        private void adjustPage(final ArrayList<ItemInfo> itemsOfPage,int newPage) {
			for (ItemInfo itemInfo : itemsOfPage) {
				itemInfo.screen = newPage;
			}
        }
        

        /**
         * vulcan created this method in 2014-8-15
         * this method adjust item info of some pages in the five global variables
         * @param pagesToAdjust. the pages to be adjusted
         * @return return the adjusted pages
         */
        private HashMap<Integer,ArrayList<ItemInfo>> adjustPages(final HashMap<Integer,ArrayList<ItemInfo>> pagesToAdjust) {
        	for(int page: pagesToAdjust.keySet()) {
        		adjustPage(pagesToAdjust.get(page),page);
        	}
        	return pagesToAdjust;
        }
        
        /**
         * vulcan created this method in 2014-8-14
         * adjust one page in launcher.db
         */
        private void writePageInDB(final Context context, ArrayList<ItemInfo> itemsOfPage,int newPage) {
			for (ItemInfo itemInfo : itemsOfPage) {
				LauncherModel.modifyItemInDatabase(context, itemInfo, itemInfo.container,
						newPage, itemInfo.cellX, itemInfo.cellY, itemInfo.spanX, itemInfo.spanY);
			}
        	return;
        }
        
        
        /**
         * vulcan created this method in 2014-8-14
         * this method adjusts all pages in a database transaction.
         * if any exception occurs, the transaction will be rolled back.
         * @param pagesHolder
         * @param lstRemove
         * @param lstAdjust
         */
        private void writePagesInDB(final Context context, final HashMap<Integer,ArrayList<ItemInfo>> pagesToAdjust) {
        	
            LauncherApplication app = (LauncherApplication) context.getApplicationContext();
            if(app == null) {
            	Log.e("linp", "writePagesInDB app == null");
            	return;
            }
            
            LauncherProvider lp = app.getLauncherProvider();
            
            if(lp == null) {
            	Log.e("linp", "writePagesInDB lp == null");
            	return;
            }
            
            DatabaseHelper dbHelper = lp.getDatabaseHelper();
            
            if(dbHelper == null) {
            	Log.e("linp", "writePagesInDB dbHelper == null");
            	return;
            }
        	
        	SQLiteDatabase db = dbHelper.getWritableDatabase();
        	
        	if(db == null) {
        		Log.e("linp", "writePagesInDB db == null");
        		return;
        	}
        	
        	db.beginTransaction();
        	
            try {

            	for(int page: pagesToAdjust.keySet()) {
            		writePageInDB(context,pagesToAdjust.get(page),page);
            	}

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                 mHandler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						((Launcher)context).writePagesInDBCompleted();
					}
				});
                Log.e("linp", "#################################finish writePagesInDB");
            }
 
        	return;
        }
        
        /**
         * vulcan created this method in 2014-8-13
         * @param pagesHolder input page list which will be checked
         * @return page list which need to be adjusted
         */
        private HashMap<Integer,ArrayList<ItemInfo>> getPagesToAdjust(final HashMap<Integer,ArrayList<ItemInfo>> pagesHolder) {
        	final  HashMap<Integer,ArrayList<ItemInfo>> pagesToAdjust = new  HashMap<Integer,ArrayList<ItemInfo>>();
        	final int maxIndex = getMaxIndexOfPageHolder(pagesHolder);
        	ArrayList<ItemInfo> itemList = null;
        	int newIndex = 0;
        	for(int i = 0;i <= maxIndex;i ++) {
        		itemList = pagesHolder.get(i);
        		if(itemList == null) {
        			continue;
        		}
				if (newIndex != i) {
					pagesToAdjust.put(newIndex, itemList);
				}
        		newIndex ++;
        	}        
        	
        	return pagesToAdjust;
        }
        
        /**
         * vulcan created this method in 2014-8-14
         * it is used to print the content of page
         * @param pages
         */
        private void printPages(HashMap<Integer,ArrayList<ItemInfo>> pages,String tag) {
        	Log.d("vulcan-automan",tag + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        	Log.d("vulcan-automan","printPages: pages.size = " + pages.size());
        	final int max = getMaxIndexOfPageHolder(pages);
        	//for(int pgIdx: pages.keySet()) {
        	for(int i = 0;i <= max;i ++) {
        		ArrayList<ItemInfo> onePage = pages.get(i);
        		if(onePage == null) {
        			continue;
        		}
        		int itemIdx = 0;
        		for(ItemInfo itemInfo: onePage) {
        			Log.d("vulcan-automan",String.format("printPages: item = %s(%d,%d)(%d,%d,%d),container = %d",
        							itemInfo.title,
        							i,itemIdx,
        							itemInfo.screen,itemInfo.cellX,itemInfo.cellY,
        							itemInfo.container));
        			itemIdx ++;
        		}
        	}
        	Log.d("vulcan-automan","<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        	return;
        }
        
        /**
         * vulcan created this method in 2014-8-15
         * when there are empty pages in the workspace, 
         * this method deletes them and move the following pages.
         * @return
         */
        public HashMap<Integer,ArrayList<ItemInfo>> reOrganiseWorkspaceItems() {
			//note that createPagesFromWorkspaceItems always returns non-null value

			HashMap<Integer,ArrayList<ItemInfo>> pages = createPagesFromWorkspaceItems(sBgItemsIdMap);
			
			printPages(pages,"before: ");
			HashMap<Integer,ArrayList<ItemInfo>> pagesToAdjust = getPagesToAdjust(pages);
			printPages(pagesToAdjust, "after: ");
			
			return adjustPages(pagesToAdjust);
        }
        
        /**
         * vulcan created this method in 2014-8-13.
         * this method write item info of some pages asynchronously in loader task
         * if the caller of this method is in worker thread, this calling will be done synchronously.
         * @return void
         */
        public void writePagesInWorkerThread() {
        	
        	runOnWorkerThread(new Runnable() {
        		public void run() {

        			
                	Callbacks cb = LauncherModel.this.getCallback();
                	
                	if(!(cb instanceof Context)) {
                		return;
                	}
                	
                	HashMap<Integer,ArrayList<ItemInfo>> pagesToAdjust = reOrganiseWorkspaceItems();
                	
                	Context context = (Context)cb;
        			
                	writePagesInDB(context,pagesToAdjust);
     
        			return;
        		}
        	});
        	return;
        }
    }
    //add by xiexiujie for calender plugin icon start 10.13
    static ArrayList<ShortcutInfo> getItemsByPackage(Context context) {
    	String letters="rili";
    	String flags="0";
    	int len = letters.length();
        ArrayList<ShortcutInfo> items = new ArrayList<ShortcutInfo>();
        if(len < 1) return items;
        final ContentResolver cr = context.getContentResolver();
        String letterString = letters.toUpperCase();
     
        String selection = LauncherSettings.Favorites.ITEM_TYPE + "=" + LauncherSettings.Favorites.ITEM_TYPE_APPLICATION 
        		+ " and title like '%" + letterString + "%'"
        		+ " or title like '%" + letters + "%'"
        		+ " or simplePinyin like '%" + letterString.charAt(0) + "%'"
        		+ " or flags like '%" + flags + "%'";
      
        Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI, new String[] {
        		LauncherSettings.Favorites._ID, LauncherSettings.Favorites.TITLE, LauncherSettings.Favorites.FULL_PINYIN, LauncherSettings.Favorites.SIMPLE_PINYIN, LauncherSettings.Favorites.FLAGS}, 
        		selection, null, "simplePinyin");
        
        if(c == null) return items;
        
        final int itemIdIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
        final int fullPinyinIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.FULL_PINYIN);
        final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
        final int simplePinyinIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SIMPLE_PINYIN);
        final int flag = c.getColumnIndexOrThrow(LauncherSettings.Favorites.FLAGS);
        
        long itemId;
        String fullPinyin;
        String title;
        String simplePinyin;
        long fl;
        
        ItemInfo info;
        try {
        	c.moveToPosition(-1);
            while (c.moveToNext()) {
                itemId = c.getLong(itemIdIndex);
                title = c.getString(titleIndex);
                fullPinyin = c.getString(fullPinyinIndex);
                simplePinyin = c.getString(simplePinyinIndex);
                fl=c.getLong(flag);
            
                if((title != null && (title.indexOf(letterString) != -1 || title.indexOf(letters) != -1 || title.toLowerCase().indexOf(letters)!= -1) )
                		||  (simplePinyin != null && simplePinyin.indexOf(letterString) != -1)
                		|| (fullPinyin != null && fullPinyin.indexOf(letterString) != -1)){
	                info = sBgItemsIdMap.get(itemId);
	                if (info instanceof ShortcutInfo){
	                 String pakeageName=info.getPackageName(((ShortcutInfo) info).intent);
	                  Log.i("xiexiujie", "---------launcher Model-------getItemsByPackage-------------------"+title+"__________"+fl+"___"+pakeageName);	
	                if(fl==0&&pakeageName.equals("com.android.calendar")){
	                  Log.i("xiexiujie", "---------launcher Model------getItemsByPackage----"+title+"__________"+fl);	
	                	items.add((ShortcutInfo)info);	
	                }	                
	                }
                } 
                
             
            }
        } catch (Exception e) {
            items.clear();
           
        } finally {
            c.close();
        }

        return items;
    }
    //add by xiexiujie for calender plugin icon end
    
    
}
