package com.mediatek.systemui.ext;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
//Gionee:zhang_xin 2012-11-15 add for CR00729316 start
import com.android.internal.telephony.Phone;
import android.os.SystemProperties;
//Gionee:zhang_xin 2012-11-15 add for CR00729316 end

/**
 * M: Default implementation of Plug-in definition of Status bar.
 */
public class DefaultStatusBarPlugin extends ContextWrapper implements IStatusBarPlugin {

    public DefaultStatusBarPlugin(Context context) {
        super(context);
    }
    
    public Resources getPluginResources() {
        //Gionee:zhang_xin 2012-11-15 modify for CR00729316 start
        //return null;
        return Resources.getSystem();
        //Gionee:zhang_xin 2012-11-15 modify for CR00729316 end
    }

    public int getSignalStrengthIcon(boolean roaming, int inetCondition, int level, boolean showSimIndicator) {
        return -1;
    }

    public String getSignalStrengthDescription(int level) {
        return null;
    }

    public int getSignalStrengthIconGemini(int simColorId, int level, boolean showSimIndicator) {
        return -1;
    }

    public int getSignalStrengthIconGemini(int simColorId, int type, int level, boolean showSimIndicator) {
        return -1;
    }

    public int getSignalStrengthNullIconGemini(int slotId) {
        return -1;
    }

    public int getSignalStrengthSearchingIconGemini(int slotId) {
        return -1;
    }

    public int getSignalIndicatorIconGemini(int slotId) {

        return -1;
    }

    public int[] getDataTypeIconListGemini(boolean roaming, DataType dataType) {
        return null;
    }

    public boolean isHspaDataDistinguishable() {
        return true;
    }
    
    public int getDataNetworkTypeIconGemini(NetworkType networkType, int simColorId) {
        return -1;
    }

    public int[] getDataActivityIconList(int simColor, boolean showSimIndicator) {
        return null;
    }

    public boolean supportDataTypeAlwaysDisplayWhileOn() {
        return false;
    }

    public boolean supportDisableWifiAtAirplaneMode() {
        return false;
    }

    public String get3gDisabledWarningString() {
        return null;
    }

}
