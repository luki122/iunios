package com.mediatek.settings.ext;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import aurora.preference.*;

public class DefaultBatteryExt implements IBatteryExt {

    @Override
    public void loadPreference(Context context, AuroraPreferenceGroup listGroup) {

    }

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
        return false;
    }
}
