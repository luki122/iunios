
package com.android.settings;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreference.OnPreferenceClickListener;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreferenceScreen;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.PreferenceChangeListener;

public class HideAppCenter extends AuroraPreferenceActivity implements OnPreferenceClickListener {

    private final static String HIDE_CATEGORY_KEY = "hide_app_category";

    private static final Uri HIDE_APP_URI = Uri
            .parse("content://com.gionee.settings.HideAppProvider/hide");

    private AuroraPreferenceCategory mHideCategory;

    private List<String> mKeys = new ArrayList<String>();

    private Map<String, String> mMainClass = new HashMap<String, String>();
    
    private static final int INIT_PREFERENCES = 1;
    private static final int ADD_PREFERENCES_DONE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        if (GnSettingsUtils.sGnSettingSupport) {
            if (GnSettingsUtils.getThemeType(getApplicationContext()).equals(
                    GnSettingsUtils.TYPE_LIGHT_THEME)) {
                setTheme(R.style.GnSettingsLightTheme);
            } else {
                setTheme(R.style.GnSettingsDarkTheme);
            }
        }
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.gn_hide_app_center);
        
        
        mHideCategory = (AuroraPreferenceCategory) findPreference(HIDE_CATEGORY_KEY);

        //setTitle(R.string.hide_app);
        getAuroraActionBar().setTitle(R.string.hide_app);
        if (GnSettingsUtils.sGnSettingSupport) {
	//AURORA-START::delete temporarily for compile::waynelin::2013-9-14 
      	/*
            getAuroraActionBar().setDisplayShowHomeEnabled(false);
	*/
        //AURORA-END::delete temporarily for compile::waynelin::2013-9-14
            getAuroraActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private List<PackageInfo> getInstalledAppPkg() {
        List<PackageInfo> pkgList = new ArrayList<PackageInfo>();
        List<PackageInfo> tempList = getPackageManager().getInstalledPackages(
                PackageManager.GET_UNINSTALLED_PACKAGES);
        for (int i = 0; i < tempList.size(); i++) {
            PackageInfo pkg = tempList.get(i);
            if ((pkg.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                mKeys.add(pkg.packageName);
                pkgList.add(pkg);
            }
        }
        return pkgList;
    }

    private void hideAppPreferenceBuilder() {
        List<PackageInfo> installedPkgs = getInstalledAppPkg();
        if (installedPkgs.size() > 0) {
            for (int i = 0; i < installedPkgs.size(); i++) {
                PackageInfo pkg = installedPkgs.get(i);
                createPrefenceAddToScreen(pkg.applicationInfo.loadIcon(getPackageManager()),
                        pkg.applicationInfo.loadLabel(getPackageManager()).toString(),
                        pkg.packageName);
            }
        } else {
            
        }
    }

    private void createPrefenceAddToScreen(Drawable icon, String name, String key) {
        AuroraCheckBoxPreference checkPreference = new AuroraCheckBoxPreference(this);
        checkPreference.setIcon(icon);
        checkPreference.setTitle(name);
        checkPreference.setKey(key);
        checkPreference.setPersistent(false);
        checkPreference.setChecked(getHideAppStatus(key));
        checkPreference.setOnPreferenceClickListener(this);
        mHideCategory.addPreference(checkPreference);
    }

    @Override
    public boolean onPreferenceClick(AuroraPreference preference) {
        // TODO Auto-generated method stub
        String key = preference.getKey();
        AuroraCheckBoxPreference checkbox = (AuroraCheckBoxPreference) preference;
        if (checkbox.isChecked()) {
            if (isPackageInDB(key)) {
                updateHideAppInfo(key, true);
            } else {
                instertHideAppInfo(key);
            }
        } else {
            updateHideAppInfo(key, false);
        }
        return false;
    }

    private boolean isPackageInDB(String key) {
        boolean exist = false;
        Cursor cur = getContentResolver().query(HIDE_APP_URI, null, " package=?", new String[] {
            key
        }, null);
        if (cur != null) {
            if (cur.moveToFirst()) {
                exist = true;
            }
            cur.close();
        }
        return exist;
    }

    private void instertHideAppInfo(String pkgName) {
        ContentValues values = new ContentValues();
        values.put("package", pkgName);
        if (mMainClass.containsKey(pkgName)) {
            values.put("class", mMainClass.get(pkgName));
        }
        values.put("status", 1);
        getContentResolver().insert(HIDE_APP_URI, values);
    }

    private void updateHideAppInfo(String pkgName, boolean flag) {
        ContentValues cv = new ContentValues();
        cv.put("status", flag ? 1 : 0);
        getContentResolver().update(HIDE_APP_URI, cv, " package=?", new String[] {
            pkgName
        });
    }

    private boolean getHideAppStatus(String key) {
        boolean flag = false;
        Cursor cursor = getContentResolver().query(HIDE_APP_URI, new String[] {
            "status"
        }, "  package=?", new String[] {
            key
        }, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                flag = cursor.getInt(0) > 0;
            }
            cursor.close();
        }
        return flag;
    }

    private void initMainClassOfApp() {
        PackageManager manager = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));
        for (ResolveInfo r : apps) {
            ComponentInfo ci = r.activityInfo;            mMainClass.put(r.activityInfo.packageName, ci.name);
        }
    }
    
    
    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case INIT_PREFERENCES:
                    initPrefrences();
                    break;
                case ADD_PREFERENCES_DONE:
                    
                    break;
                default:
                    break;
            }
        }
        
    };
    
    private void initPrefrences(){
        ProgressBar bar = new ProgressBar(getApplicationContext());
        
        initMainClassOfApp();

        hideAppPreferenceBuilder();
    }
}
