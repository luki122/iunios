package gn.com.android.update.settings;

import android.content.Context;

public class OtaPushSettingSharedPreferenceOperator extends SharePreferenceOperator {
    private static final String RECEIVER_NOTIFIER = "receiver_notifier";

    public OtaPushSettingSharedPreferenceOperator(Context context) {
        super(context, RECEIVER_NOTIFIER);
    }
}
