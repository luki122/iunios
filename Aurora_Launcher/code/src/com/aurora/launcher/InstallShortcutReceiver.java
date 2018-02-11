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

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.aurora.launcher.Workspace.ItemPos;
import com.aurora.launcher.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class InstallShortcutReceiver extends BroadcastReceiver {
    public static final String ACTION_INSTALL_SHORTCUT =
            "com.android.launcher.action.INSTALL_SHORTCUT";
    public static final String NEW_APPS_PAGE_KEY = "apps.new.page";
    public static final String NEW_APPS_LIST_KEY = "apps.new.list";

    public static final int NEW_SHORTCUT_BOUNCE_DURATION = 450;
    public static final int NEW_SHORTCUT_STAGGER_DELAY = 75;

    private static final int INSTALL_SHORTCUT_SUCCESSFUL = 0;
    private static final int INSTALL_SHORTCUT_IS_DUPLICATE = -1;
    private static final int INSTALL_SHORTCUT_NO_SPACE = -2;

    // A mime-type representing shortcut data
    public static final String SHORTCUT_MIMETYPE =
            "com.aurora.launcher/shortcut";

    // The set of shortcuts that are pending install
    private static ArrayList<PendingInstallShortcutInfo> mInstallQueue =
            new ArrayList<PendingInstallShortcutInfo>();

    // Determines whether to defer installing shortcuts immediately until
    // processAllPendingInstalls() is called.
    private static boolean mUseInstallQueue = false;

	// Aurora <jialf> <2014-02-20> modify for fix bug #2427 begin
	// Aurora <jialf> <2013-12-23> add for fix bug #1199 begin
    private IAddAuroraShortcut mLauncher;
    
    InstallShortcutReceiver() {
	}
    
    public void setAddshortcut(IAddAuroraShortcut shortcut) {
    	mLauncher = shortcut;
    }
	// Aurora <jialf> <2013-12-23> add for fix bug #1199 end
	// Aurora <jialf> <2014-02-20> modify for fix bug #2427 end

    private static class PendingInstallShortcutInfo {
        Intent data;
        Intent launchIntent;
        String name;

        public PendingInstallShortcutInfo(Intent rawData, String shortcutName,
                Intent shortcutIntent) {
            data = rawData;
            name = shortcutName;
            launchIntent = shortcutIntent;
        }
    }

    public void onReceive(Context context, Intent data) {
    	
    	Log.d("vulcan-db","onReceive: data = " + data);
    	
        if (!ACTION_INSTALL_SHORTCUT.equals(data.getAction())) {
            return;
        }

        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        if (intent == null) {
            return;
        }
        // This name is only used for comparisons and notifications, so fall back to activity name
        // if not supplied
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        if (name == null) {
            try {
                PackageManager pm = context.getPackageManager();
                ActivityInfo info = pm.getActivityInfo(intent.getComponent(), 0);
                name = info.loadLabel(pm).toString();
            } catch (PackageManager.NameNotFoundException nnfe) {
                return;
            }
        }
        // Queue the item up for adding if launcher has not loaded properly yet
        boolean launcherNotLoaded = LauncherModel.getCellCountX() <= 0 ||
                LauncherModel.getCellCountY() <= 0;

        PendingInstallShortcutInfo info = new PendingInstallShortcutInfo(data, name, intent);
        if (mUseInstallQueue || launcherNotLoaded) {
            mInstallQueue.add(info);
        } else {
        	// Aurora <jialf> <2013-12-23> modify for fix bug #1199 begin
            // processInstallShortcut(context, info);
        	processAuroraInstallShortcut(context, info);
        	// Aurora <jialf> <2013-12-23> modify for fix bug #1199 end
        }
    }

    static void enableInstallQueue() {
        mUseInstallQueue = true;
    }
    static void disableAndFlushInstallQueue(Context context) {
        mUseInstallQueue = false;
        flushInstallQueue(context);
    }
    static void flushInstallQueue(Context context) {
        Iterator<PendingInstallShortcutInfo> iter = mInstallQueue.iterator();
        while (iter.hasNext()) {
            processInstallShortcut(context, iter.next());
            iter.remove();
        }
    }

    private static void processInstallShortcut(Context context,
            PendingInstallShortcutInfo pendingInfo) {
        String spKey = LauncherApplication.getSharedPreferencesKey();
        SharedPreferences sp = context.getSharedPreferences(spKey, Context.MODE_PRIVATE);

        final Intent data = pendingInfo.data;
        final Intent intent = pendingInfo.launchIntent;
        final String name = pendingInfo.name;

        // Lock on the app so that we don't try and get the items while apps are being added
        LauncherApplication app = (LauncherApplication) context.getApplicationContext();
        final int[] result = {INSTALL_SHORTCUT_SUCCESSFUL};
        boolean found = false;
        synchronized (app) {
            final ArrayList<ItemInfo> items = LauncherModel.getItemsInLocalCoordinates(context);
            final boolean exists = LauncherModel.shortcutExists(context, name, intent);

            // Try adding to the workspace screens incrementally, starting at the default or center
            // screen and alternating between +1, -1, +2, -2, etc. (using ~ ceil(i/2f)*(-1)^(i-1))
            final int screen = Launcher.DEFAULT_SCREEN;
            for (int i = 0; i < (2 * Launcher.SCREEN_COUNT) + 1 && !found; ++i) {
                int si = screen + (int) ((i / 2f) + 0.5f) * ((i % 2 == 1) ? 1 : -1);
                if (0 <= si && si < Launcher.SCREEN_COUNT) {
                    found = installShortcut(context, data, items, name, intent, si, exists, sp,
                            result);
                }
            }
        }

        // We only report error messages (duplicate shortcut or out of space) as the add-animation
        // will provide feedback otherwise
        if (!found) {
            if (result[0] == INSTALL_SHORTCUT_NO_SPACE) {
                Toast.makeText(context, context.getString(R.string.completely_out_of_space),
                        Toast.LENGTH_SHORT).show();
            } else if (result[0] == INSTALL_SHORTCUT_IS_DUPLICATE) {
                Toast.makeText(context, context.getString(R.string.shortcut_duplicate, name),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static boolean installShortcut(Context context, Intent data, ArrayList<ItemInfo> items,
            String name, Intent intent, final int screen, boolean shortcutExists,
            final SharedPreferences sharedPrefs, int[] result) {
        int[] tmpCoordinates = new int[2];
        if (findEmptyCell(context, items, tmpCoordinates, screen)) {
            if (intent != null) {
                if (intent.getAction() == null) {
                    intent.setAction(Intent.ACTION_VIEW);
                } else if (intent.getAction().equals(Intent.ACTION_MAIN) &&
                        intent.getCategories() != null &&
                        intent.getCategories().contains(Intent.CATEGORY_LAUNCHER)) {
                    intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                }

                // By default, we allow for duplicate entries (located in
                // different places)
                boolean duplicate = data.getBooleanExtra(Launcher.EXTRA_SHORTCUT_DUPLICATE, true);
                if (duplicate || !shortcutExists) {
                    // If the new app is going to fall into the same page as before, then just
                    // continue adding to the current page
                    int newAppsScreen = sharedPrefs.getInt(NEW_APPS_PAGE_KEY, screen);
                    Set<String> newApps = new HashSet<String>();
                    if (newAppsScreen == screen) {
                        newApps = sharedPrefs.getStringSet(NEW_APPS_LIST_KEY, newApps);
                    }
                    synchronized (newApps) {
                        newApps.add(intent.toUri(0).toString());
                    }
                    final Set<String> savedNewApps = newApps;
                    new Thread("setNewAppsThread") {
                        public void run() {
                            synchronized (savedNewApps) {
                                sharedPrefs.edit()
                                           .putInt(NEW_APPS_PAGE_KEY, screen)
                                           .putStringSet(NEW_APPS_LIST_KEY, savedNewApps)
                                           .commit();
                            }
                        }
                    }.start();

                    // Update the Launcher db
                    LauncherApplication app = (LauncherApplication) context.getApplicationContext();
                    ShortcutInfo info = app.getModel().addShortcut(context, data,
                            LauncherSettings.Favorites.CONTAINER_DESKTOP, screen,
                            tmpCoordinates[0], tmpCoordinates[1], true);
                    if (info == null) {
                        return false;
                    }
                } else {
                    result[0] = INSTALL_SHORTCUT_IS_DUPLICATE;
                }

                return true;
            }
        } else {
            result[0] = INSTALL_SHORTCUT_NO_SPACE;
        }

        return false;
    }

    private static boolean findEmptyCell(Context context, ArrayList<ItemInfo> items, int[] xy,
            int screen) {
        final int xCount = LauncherModel.getCellCountX();
        final int yCount = LauncherModel.getCellCountY();
        boolean[][] occupied = new boolean[xCount][yCount];

        ItemInfo item = null;
        int cellX, cellY, spanX, spanY;
        for (int i = 0; i < items.size(); ++i) {
            item = items.get(i);
            if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                if (item.screen == screen) {
                    cellX = item.cellX;
                    cellY = item.cellY;
                    spanX = item.spanX;
                    spanY = item.spanY;
                    for (int x = cellX; 0 <= x && x < cellX + spanX && x < xCount; x++) {
                        for (int y = cellY; 0 <= y && y < cellY + spanY && y < yCount; y++) {
                            occupied[x][y] = true;
                        }
                    }
                }
            }
        }

        return CellLayout.findVacantCell(xy, 1, 1, xCount, yCount, occupied);
    }

	// Aurora <jialf> <2013-12-23> add for fix bug #1199 begin
    private void processAuroraInstallShortcut(Context context,
            PendingInstallShortcutInfo pendingInfo) {
        final Intent data = pendingInfo.data;
        final Intent intent = pendingInfo.launchIntent;
        final String name = pendingInfo.name;

        // Lock on the app so that we don't try and get the items while apps are being added
        LauncherApplication app = (LauncherApplication) context.getApplicationContext();
        final int[] result = {INSTALL_SHORTCUT_SUCCESSFUL};
        boolean found = false;
        synchronized (app) {
        	if(mLauncher.isFromAuroraShortcut()) {
        		found = installAuroraShortcut(context, app, data, intent, false, result);
        	} else {
				final boolean exists = checkPackageAndClassName(context, intent);
				if(exists) return;
        		found = installAuroraShortcut(context, app, data, intent, exists, result);
        	}
			
        	/*// Aurora <jialf> <2014-02-25> modify for fix bug #2469 begin
            final boolean exists = LauncherModel.shortcutExists(context, name, intent, mLauncher.isFromAuroraShortcut());
            		// LauncherModel.shortcutExists(context, name, intent);
			if (exists) return;
	        // Aurora <jialf> <2014-02-25> modify for fix bug #2469 end
            found = installAuroraShortcut(context, app, data, intent, exists, result);*/
            Log.i("InstallShortcutReceiver", "install aurora shortcut ok is : " + found);
        }
    }
    
	private boolean checkPackageAndClassName(Context context, final Intent data) {
		boolean exists = false;
    	ComponentName cn = data.getComponent();
		if (cn == null) return exists;
		String packageName = cn.getPackageName();
		String className = cn.getClassName();
		if(packageName == null || className == null) return exists;
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		cn = new ComponentName(packageName,className);
		intent.setComponent(cn);
		// title = title.replaceAll("\\s{1,}", "");
		exists = LauncherModel.shortcutExists(context, intent);
		Log.i("InstallShortcutReceiver",
				"ACTION_MAIN or CATEGORY_LAUNCHER exists : " + exists);
		return exists;
    }
    
    private boolean installAuroraShortcut(Context context,
    		LauncherApplication app, Intent data,
            Intent intent, boolean shortcutExists, int[] result) {
        //LauncherModel model = app.getModel();
		//int[] tmpLoc = model.findLastPostionView();
		//int screen = model.getInstallShortcutScreen();
		//int[] loc = null;
		//if (tmpLoc != null && tmpLoc[0] != -1 && tmpLoc[1] != -1) {
		//	loc = tmpLoc;
		//} else {
		//	loc = new int[] { 0, 0 };
		//	screen++;
		//}
		//Log.i("InstallShortcutReceiver", "[" + loc[0] + ", " + loc[1] + "]" +", " + screen);
    	
    	
    	
    	if(app == null) {
    		return false;
    	}
    	
    	
    	Workspace workspace = app.getWorkspace();
    	if(workspace == null) {
    		Log.d("vulcan-repeat","installAuroraShortcut: launcherModel is null!!!");
    		return false;
    	}
    	
    	ItemPos itemPos;
    	itemPos = workspace.findFirstVacancyOfWorkspace();
    	if(itemPos == null) {
    		Log.d("vulcan-repeat","installAuroraShortcut: launcherModel is null!!!");
    		return false;
    	}
    	
    	
        if (intent != null) {
            if (intent.getAction() == null) {
                intent.setAction(Intent.ACTION_VIEW);
            } else if (intent.getAction().equals(Intent.ACTION_MAIN) &&
                    intent.getCategories() != null &&
                    intent.getCategories().contains(Intent.CATEGORY_LAUNCHER)) {
                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            }

            // By default, we allow for duplicate entries (located in
            // different places)
            boolean duplicate = data.getBooleanExtra(Launcher.EXTRA_SHORTCUT_DUPLICATE, true);
            if (duplicate || !shortcutExists) {
                // Update the Launcher db
                ShortcutInfo info = app.getModel().addShortcut(context,
                		data, LauncherSettings.Favorites.CONTAINER_DESKTOP,
                		itemPos.screen, itemPos.x, itemPos.y, false);
                        //LauncherSettings.Favorites.CONTAINER_DESKTOP, screen, loc[0], loc[1], false);
                if (info == null) {
                    return false;
                } else {
                	int loc[] = new int[2];
					info.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
					loc[0] = itemPos.x;
					loc[1] = itemPos.y;
					mLauncher.addAuroraShortcut(info, itemPos.screen, loc);
                }
            } else {
                result[0] = INSTALL_SHORTCUT_IS_DUPLICATE;
            }
            return true;
        }
        return false;
    }
	// Aurora <jialf> <2013-12-23> add for fix bug #1199 end

	// Aurora <jialf> <2014-02-20> modify for fix bug #2427 begin
    interface IAddAuroraShortcut {
    	void addAuroraShortcut(ShortcutInfo item, int screen, int[] loc);
    	boolean isFromAuroraShortcut();
    }
	// Aurora <jialf> <2014-02-20> modify for fix bug #2427 end
}
