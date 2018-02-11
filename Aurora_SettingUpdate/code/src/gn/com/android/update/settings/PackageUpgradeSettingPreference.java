package gn.com.android.update.settings;

import gn.com.android.update.business.Config;
import android.content.Context;

public class PackageUpgradeSettingPreference extends SharePreferenceOperator {

    public static String getPackageUpgradeSettingSharePreferenceName(String packageName) {
        return packageName + Config.UNDER_LINE_STRING + Config.SETTING_STRING;
    }

    public PackageUpgradeSettingPreference(Context context, String packageName) {
        super(context, getPackageUpgradeSettingSharePreferenceName(packageName));
    }

}
