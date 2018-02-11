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

import android.app.Application;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.os.Environment;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.os.SystemProperties;
import android.util.Log;

import com.aurora.launcher.InstallShortcutReceiver.IAddAuroraShortcut;
import com.aurora.launcher.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

public class LauncherApplication extends Application {
    public LauncherModel mModel;
    public IconCache mIconCache;
    private static boolean sIsScreenLarge;
    private static float sScreenDensity;
    private static int sLongPressTimeout = 300;
    private static final String sSharedPreferencesKey = "com.aurora.launcher.prefs";
    WeakReference<LauncherProvider> mLauncherProvider;
    
    private StorageManager mStorageManager;
    
	// Aurora <jialf> <2014-02-20> modify for fix bug #2427 begin
	private InstallShortcutReceiver mInstallShortcutReceiver;
	// Aurora <jialf> <2014-02-20> modify for fix bug #2427 end
	
	boolean mIuniDevice = SystemProperties.get("ro.product.device", "aurora").toUpperCase().equals("IUNI");
	static final String APPLICATIONS_AVAILABLE = "available";
	static final String FIRST_BOOT = "first_boot";

	//added by vulcan in 2014-5-17
	public static LogWriter logVulcan = new LogWriter();

	@Override
	public void onCreate() {
		super.onCreate();
		// set sIsScreenXLarge and sScreenDensity *before* creating icon cache
		sIsScreenLarge = getResources().getBoolean(R.bool.is_large_screen);
		sScreenDensity = getResources().getDisplayMetrics().density;

		mIconCache = new IconCache(this);
		mModel = new LauncherModel(this, mIconCache);
		mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);

		// Register intent receivers
		IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filter.addDataScheme("package");
		registerReceiver(mModel, filter);
		filter = new IntentFilter();
		filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
		filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
		filter.addAction(Intent.ACTION_LOCALE_CHANGED);
		filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
		registerReceiver(mModel, filter);
		
		filter = new IntentFilter();
		filter.addAction(SearchManager.INTENT_GLOBAL_SEARCH_ACTIVITY_CHANGED);
		filter.addAction(mModel.NOTIFY_APPS_TO_UPDATEIICON); //iht test 
		registerReceiver(mModel, filter);
		// AURORA-START:
		/*
		 * filter = new IntentFilter();
		 * filter.addAction(SearchManager.INTENT_ACTION_SEARCHABLES_CHANGED);
		 * registerReceiver(mModel, filter);
		 */
		// AURORA-END:

    	// Aurora <jialf> <2014-02-20> modify for fix bug #2427 begin
        mInstallShortcutReceiver = new InstallShortcutReceiver();
		IntentFilter afilter = new IntentFilter(InstallShortcutReceiver.ACTION_INSTALL_SHORTCUT);
		registerReceiver(mInstallShortcutReceiver, afilter);
		// Aurora <jialf> <2014-02-20> modify for fix bug #2427 end

		// checkSdcardState();
		/*boolean first = getSharedPreferences(sSharedPreferencesKey,
				Context.MODE_PRIVATE).getBoolean(
				LauncherApplication.FIRST_BOOT, false);
		Log.i("LauncherApplication", "first = " + first);
		if (first) {
			mModel.setmSdcardMounted(true);
		} else {
			mModel.loadDelay();
		}*/
		//mModel.loadDelay();
		
        // Register for changes to the favorites
        ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(LauncherSettings.Favorites.CONTENT_URI, true,
                mFavoritesObserver);
    }
    
    private void checkSdcardState() {
    	// Aurora <jialf> <2014-02-25> modify for fix bug #2467 begin
        String auroraInternalFilePath = getInternalStoragePath(); // getInternalSDCardPath();
        String auroraExternalFilePath = getExternalStoragePath(); // getExternalSDCardPath();
        
		Log.i("LauncherApplication", "auroraInternalFilePath = " + auroraInternalFilePath
				+ ", auroraExternalFilePath = "+ auroraExternalFilePath);
		boolean exMounted = false;
        if(auroraExternalFilePath == null) {
			exMounted = checkSDCardMount(auroraInternalFilePath);
			Log.i("LauncherApplication", "internal sdcard exMounted = "
					+ exMounted);
			Editor editor = getSharedPreferences(sSharedPreferencesKey, Context.MODE_PRIVATE).edit();
			editor.putBoolean(LauncherApplication.APPLICATIONS_AVAILABLE, true);
			editor.commit();
			if(mIuniDevice) {
				boolean first = getSharedPreferences(sSharedPreferencesKey,
						Context.MODE_PRIVATE).getBoolean(FIRST_BOOT, false);
				Log.i("LauncherApplication", "first = " + first);
				if(!first) {
					exMounted = exMounted && mModel.ismAuroraExternalReady();
				}
			}
		} else {
			exMounted = checkSDCardMount(auroraExternalFilePath);
			Log.i("LauncherApplication", "external sdcard exMounted = "
					+ exMounted);
			boolean temp = mModel.ismAuroraExternalReady();
			if (!temp) {
				temp = getSharedPreferences(sSharedPreferencesKey,
						Context.MODE_PRIVATE).getBoolean(APPLICATIONS_AVAILABLE, false);
			}
			Log.i("LauncherApplication", "temp = " + temp);
			exMounted = exMounted && temp;
        }
    	mModel.setmSdcardMounted(exMounted);
		LauncherApplication.logVulcan.print("in checkSdcardState setmSdcardMounted to" + false);
        // Aurora <jialf> <2014-02-25> modify for fix bug #2467 end
    }

	// Aurora <jialf> <2014-02-20> modify for fix bug #2427 begin
    public void registerAddShortcut(IAddAuroraShortcut shortcut) {
    	mInstallShortcutReceiver.setAddshortcut(shortcut);
    }
	// Aurora <jialf> <2014-02-20> modify for fix bug #2427 end
    
    protected boolean checkSDCardMount(String mountPoint) {
		if (mountPoint == null || "".equals(mountPoint)) {
			return false;
		}
		String state = null;
		state = mStorageManager.getVolumeState(mountPoint);
		return Environment.MEDIA_MOUNTED.equals(state);
	}
    
    public String getInternalSDCardPath() {
    	return gionee.os.storage.GnStorageManager.getInstance(this).getInternalStoragePath();
    }
    
	public String getExternalSDCardPath() {
		return gionee.os.storage.GnStorageManager.getInstance(this).getExternalStoragePath();
	}

    // Aurora <jialf> <2014-02-25> modify for fix bug #2467 begin
	public String getInternalStoragePath() {
		String s = android.os.SystemProperties.get("ro.internal.storage");
		Log.i("LauncherApplication", "getInternalStoragePath(): s = " + s);
		if (s == null || s.length() == 0)
			return null;
		String state = mStorageManager.getVolumeState(s);
		if (state != null) {
			Log.i("LauncherApplication", "getInternalStoragePath(): "
					+ (state != null) + ", state = " + state);
		}
		if (state != null
				&& (!state.equalsIgnoreCase(Environment.MEDIA_REMOVED))) {
			return s;
		} else {
			return null;
		}
	}
	
	public String getExternalStoragePath() {
		String s = android.os.SystemProperties.get("ro.external.storage");
		Log.i("LauncherApplication", "getExternalStoragePath(): s = " + s);
		if (s == null || s.length() == 0)
			return null;
		String state = mStorageManager.getVolumeState(s);
		if (state != null) {
			Log.i("LauncherApplication", "getExternalStoragePath(): "
					+ (state != null) + ", state = " + state);
		} else {
			Log.i("LauncherApplication",
					"getExternalStoragePath(): state == null");
		}
		if (state != null
				&& (!state.equalsIgnoreCase(Environment.MEDIA_REMOVED))) {
			return s;
		} else {
			return null;
		}
	}
    // Aurora <jialf> <2014-02-25> modify for fix bug #2467 end

    /**
     * There's no guarantee that this function is ever called.
     */
    @Override
    public void onTerminate() {
        super.onTerminate();

        unregisterReceiver(mModel);

        ContentResolver resolver = getContentResolver();
        resolver.unregisterContentObserver(mFavoritesObserver);
    }

    /**
     * Receives notifications whenever the user favorites have changed.
     */
    private final ContentObserver mFavoritesObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            // If the database has ever changed, then we really need to force a reload of the
            // workspace on the next load
            mModel.resetLoadedState(false, true);
            mModel.startLoaderFromBackground();
        }
    };

    LauncherModel setLauncher(Launcher launcher) {
        mModel.initialize(launcher);
        return mModel;
    }

    IconCache getIconCache() {
        return mIconCache;
    }

    LauncherModel getModel() {
        return mModel;
    }

    void setLauncherProvider(LauncherProvider provider) {
        mLauncherProvider = new WeakReference<LauncherProvider>(provider);
    }

    LauncherProvider getLauncherProvider() {
        return mLauncherProvider.get();
    }

    public static String getSharedPreferencesKey() {
        return sSharedPreferencesKey;
    }

    public static boolean isScreenLarge() {
        return sIsScreenLarge;
    }

    public static boolean isScreenLandscape(Context context) {
        return context.getResources().getConfiguration().orientation ==
            Configuration.ORIENTATION_LANDSCAPE;
    }

    public static float getScreenDensity() {
        return sScreenDensity;
    }

    public static int getLongPressTimeout() {
        return sLongPressTimeout;
    }
    
	public boolean isIuniDevice() {
		Log.i("LauncherApplication", "iuni device is : " + mIuniDevice);
		return mIuniDevice;
	}
	
	/**
	 * get workspace.
	 * @return return null if it doesn't exist.
	 */
	public Workspace getWorkspace() {
		if( null == mModel) {
			return null;
		}
		Launcher launcher = (Launcher)mModel.getCallback();
		if(null == launcher) {
			return null;
		}
		
		return launcher.getWorkspace();
	}
}
