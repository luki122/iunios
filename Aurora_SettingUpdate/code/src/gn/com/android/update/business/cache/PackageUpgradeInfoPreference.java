package gn.com.android.update.business.cache;

import gn.com.android.update.business.Config;
import gn.com.android.update.business.PackageUpgradeInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PackageUpgradeInfoPreference {
    private static final String PREFERENCE_NAME = "new_version";
    private SharedPreferences mSharedPreferences = null;
    private Editor mEditor = null;

    public static String getPreferenceName(String packageName) {
        return packageName + Config.UNDER_LINE_STRING + PREFERENCE_NAME;
    }

    public PackageUpgradeInfoPreference(Context context, String packageName) {
        mSharedPreferences = context.getSharedPreferences(getPreferenceName(packageName), Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    }

    public PackageUpgradeInfo getPackageUpgradeInfo() {
        PackageUpgradeInfo packageUpgradeInfo = new PackageUpgradeInfo();
        return packageUpgradeInfo;
    }

    public void storePackageUpgradeInfo() {

    }

    public void initial() {
        synchronized (this) {
            mEditor.clear().commit();
        }

    }
}
