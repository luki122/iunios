package com.mediatek.settings.ext;

import android.preference.Preference;

import aurora.preference.*;
import android.preference.PreferenceScreen;

public class DefaultDeviceInfoSettingsExt implements IDeviceInfoSettingsExt {

    public void initSummary(AuroraPreference preference) {
    }

    /**
     * CT E push feature refactory,add Epush in common feature
     * @param root The root PreferenceScreen which add e push entrance to
     * The preference screen
     */
    public void addEpushPreference(AuroraPreferenceScreen root){
    }

    public void updateSummary(AuroraPreference preference, String value, String defaultValue) {
    }
}
