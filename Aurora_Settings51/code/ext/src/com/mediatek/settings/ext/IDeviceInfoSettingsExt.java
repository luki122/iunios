package com.mediatek.settings.ext;

import android.preference.Preference;
import aurora.preference.*;
import android.preference.PreferenceScreen;

public interface IDeviceInfoSettingsExt {
    /**
     * initialize preference summary
     * @param preference The parent preference
     */
    void initSummary(AuroraPreference preference);

    /**
     *
     * CT E push feature refactory,add Epush in common feature
     * @param root The preference screen to add E push entrance
     */
    void addEpushPreference(AuroraPreferenceScreen root);
    void updateSummary(AuroraPreference preference, String value, String dafaultValue);
}
