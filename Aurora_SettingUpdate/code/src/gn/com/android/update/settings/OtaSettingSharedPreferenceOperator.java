package gn.com.android.update.settings;

import android.content.Context;

public class OtaSettingSharedPreferenceOperator extends SharePreferenceOperator {
    private static final String OTA_SETTING = "gn.com.android.update_preferences";

    public OtaSettingSharedPreferenceOperator(Context context) {
        super(context, OTA_SETTING);
    }
}
