package com.mediatek.settings.ext;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import aurora.preference.*;

public interface IBatteryExt {

    /**
     * Load other preferences
     *
     * @param context Context
     * @param listGroup Battery main screen
     */
    void loadPreference(Context context, AuroraPreferenceGroup listGroup);

    /**
     * Handle PowerSaving preference click
     *
     * @param preferenceScreen The PreferenceScreen contains PowerSavingPreference
     * @param preference The PowerSavingPreference
     */
    boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference);
}
