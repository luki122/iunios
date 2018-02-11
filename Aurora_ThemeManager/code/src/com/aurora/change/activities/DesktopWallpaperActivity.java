package com.aurora.change.activities;

import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.widget.ListView;
import aurora.app.AuroraActivity;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceScreen;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.Type;

import com.aurora.thememanager.R;

public class DesktopWallpaperActivity extends AuroraPreferenceActivity {

    /*private Context mContext;
    private ListView mListView;
    private List<ResolveInfo> mResolveInfos;
    
    private AuroraActionBar mAuroraActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setupViews();
    }

    private void setupViews() {
//        setContentView(R.layout.activity_desktop_wallpaper);
        setAuroraContentView(R.layout.activity_desktop_wallpaper, Type.Normal);
        mAuroraActionBar = getAuroraActionBar();
        mAuroraActionBar.setTitle(R.string.desktop_wallpaper);
        mListView = (ListView) findViewById(R.id.show_desktop);
        Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
        final PackageManager pm = getPackageManager();
        mResolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
    }*/
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.wallpaper_settings);
        populateWallpaperTypes();
    }

    private void populateWallpaperTypes() {
        // Search for activities that satisfy the ACTION_SET_WALLPAPER action
        Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
        final PackageManager pm = getPackageManager();
        List<ResolveInfo> rList = pm.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);

        final AuroraPreferenceScreen parent = getPreferenceScreen();
        parent.setOrderingAsAdded(false);
        // Add AuroraPreference items for each of the matching activities
        for (ResolveInfo info : rList) {
            //delete wallpaper_video 2013-11-02
            if (info.activityInfo.packageName.equals(getResources().
                getString(R.string.wallpaper_settings_video))) {
                continue;
            }

            if (info.activityInfo.packageName.equals(getResources().
                getString(R.string.wallpaper_settings_samsung))) {
                continue;
            }

            if (info.activityInfo.packageName.equals(getResources().getString(
                    R.string.wallpaper_settings_live))) {
                continue;
            }

            AuroraPreference pref = new AuroraPreference(this);
            Intent prefIntent = new Intent(intent);
            prefIntent.setComponent(new ComponentName(
                    info.activityInfo.packageName, info.activityInfo.name));
            pref.setIntent(prefIntent);
            CharSequence label = info.loadLabel(pm);
            if (label == null) label = info.activityInfo.packageName;
            pref.setTitle(label);
            parent.addPreference(pref);
        }
    }
}
