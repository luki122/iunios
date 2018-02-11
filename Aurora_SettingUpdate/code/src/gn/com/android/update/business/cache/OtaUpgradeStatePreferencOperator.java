package gn.com.android.update.business.cache;

import android.content.Context;
import gn.com.android.update.business.OtaUpgradeState;
import gn.com.android.update.settings.SharePreferenceOperator;

public class OtaUpgradeStatePreferencOperator extends SharePreferenceOperator {
    private static final String SHARE_PREFERENCE_NAME = "ota_upgrade_state";
    private static final String KEY_UPGRADE_STATE = "upgrade_state";

    public OtaUpgradeStatePreferencOperator(Context context) {
        super(context, SHARE_PREFERENCE_NAME);
    }

    public OtaUpgradeState getOtaUpgradeState() {

        int value = getIntValue(KEY_UPGRADE_STATE, OtaUpgradeState.INITIAL.getValue());
        OtaUpgradeState state = OtaUpgradeState.INITIAL;
        for (OtaUpgradeState otaUpgradeState : OtaUpgradeState.values()) {
            if (otaUpgradeState.getValue() == value) {
                state = otaUpgradeState;
            }
        }
        return state;
    }

    public void setOtaUpgradeState(OtaUpgradeState otaUpgradeState) {
        setIntValue(KEY_UPGRADE_STATE, otaUpgradeState.getValue());
    }
}
