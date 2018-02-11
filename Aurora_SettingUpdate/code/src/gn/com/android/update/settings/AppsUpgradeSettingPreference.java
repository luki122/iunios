package gn.com.android.update.settings;

import android.content.Context;

public class AppsUpgradeSettingPreference extends SharePreferenceOperator {
    private static final String PREFERENCE_NAME = "app_upgrade_settings";

    public AppsUpgradeSettingPreference(Context context) {
        super(context, PREFERENCE_NAME);
    }

}
