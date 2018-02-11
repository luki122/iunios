package com.android.systemui.statusbar.util;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import gionee.provider.GnTelephony.SIMInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.PhoneConstants;
import com.android.systemui.R;

import com.mediatek.telephony.TelephonyManagerEx;
import com.android.systemui.Xlog;

import java.util.List;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collections;
import com.gionee.featureoption.FeatureOption;
import android.telephony.GnPhoneStateListener;
import com.gionee.internal.telephony.GnITelephony;

/**
 * M: [SystemUI] Support "dual SIM" and "Notification toolbar".
 */
public class SIMHelper {

    public static final String TAG = "SIMHelper";

    private static final int SIM_STATUS_COUNT = 9;
    private static final int MOBILE_ICON_COUNT = 4;

    private static List<SIMInfo> sSimInfos;

    private static int[] sSimStatusViews;
    private static int[] sMobileIconResIds;

    private static String sIsOptr = null;
    private static String sBaseband = null;

    private static ITelephony sITelephony;
    
    private static TelephonyManagerEx mTMEx = null;

    // Gionee lihuafang 2012-05-31 modify for CR00614104 begin
    public static boolean mIsGnShowDigitalSlotSupport = false;//SystemProperties.get("ro.gn.operator.showdigitalslot").equals("yes");
    public static boolean mIsGnShowSlotSupport = false;//SystemProperties.get("ro.gn.operator.showslot").equals("yes");
    // Gionee lihuafang 2012-05-31 modify for CR00614104 end


    private SIMHelper() {
    }

    /**
     * Get the default SIM id of the assigned business.
     * 
     * @param context
     * @param businessType
     * @return The default SIM id, or -1 if it is not defined.
     */
    public static long getDefaultSIM(Context context, String businessType) {
        return Settings.System.getLong(context.getContentResolver(), businessType, -1);
    }

    public static void setDefaultSIM(Context context, String businessType, long simId) {
        Settings.System.putLong(context.getContentResolver(), businessType, simId);
    }

    public static List<SIMInfo> getSIMInfoList(Context context) {
        if (sSimInfos == null || sSimInfos.size() == 0) {
            sSimInfos = getSortedSIMInfoList(context);
        }
        return sSimInfos;
    }

    /**
     * Get the SIM info of the assigned SIM id.
     * 
     * @param context
     * @param simId
     * @return The SIM info, or null if it doesn't exist.
     */
    public static SIMInfo getSIMInfo(Context context, long simId) {
        if (sSimInfos == null || sSimInfos.size() == 0) {
            getSIMInfoList(context);
        }
        for (SIMInfo info : sSimInfos) {
            if (info.mSimId == simId) {
                return info;
            }
        }
        return null;
    }

    /**
     * Get the SIM info of the assigned SLOT id.
     * 
     * @param context
     * @param slotId
     * @return The SIM info, or null if it doesn't exist.
     */
    public static SIMInfo getSIMInfoBySlot(Context context, int slotId) {
        if (sSimInfos == null || sSimInfos.size() == 0) {
            getSIMInfoList(context);
        }
        if (sSimInfos == null) {
            return null;
        }
        if (slotId == -1 && sSimInfos.size() > 0) {
            return sSimInfos.get(0);
        }
        for (SIMInfo info : sSimInfos) {
            if (info.mSlot == slotId) {
                return info;
            }
        }
        return null;
    }

    private static List<SIMInfo> getSortedSIMInfoList(Context context) {
        List<SIMInfo> simInfoList = SIMInfo.getInsertedSIMList(context);
        Collections.sort(simInfoList, new Comparator<SIMInfo>() {
            @Override
            public int compare(SIMInfo a, SIMInfo b) {
                if(a.mSlot < b.mSlot) {
                    return -1;
                } else if (a.mSlot > b.mSlot) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        return simInfoList;
    }

    public static void updateSIMInfos(Context context) {
        sSimInfos = null;
        sSimInfos = getSortedSIMInfoList(context);
    }

    public static void initStatusIcons() {
        if (sSimStatusViews == null) {
            sSimStatusViews = new int[SIM_STATUS_COUNT];
            sSimStatusViews[FeatureOption.SIM_INDICATOR_RADIOOFF] = 0;//com.mediatek.internal.R.drawable.sim_radio_off;
            sSimStatusViews[FeatureOption.SIM_INDICATOR_LOCKED] = 0;//com.mediatek.internal.R.drawable.sim_locked;
            sSimStatusViews[FeatureOption.SIM_INDICATOR_INVALID] = 0;//com.mediatek.internal.R.drawable.sim_invalid;
            sSimStatusViews[FeatureOption.SIM_INDICATOR_SEARCHING] = 0;//com.mediatek.internal.R.drawable.sim_searching;
            sSimStatusViews[FeatureOption.SIM_INDICATOR_ROAMING] = 0;//com.mediatek.internal.R.drawable.sim_roaming;
            sSimStatusViews[FeatureOption.SIM_INDICATOR_CONNECTED] = 0;//com.mediatek.internal.R.drawable.sim_connected;
            sSimStatusViews[FeatureOption.SIM_INDICATOR_ROAMINGCONNECTED] = 0;//com.mediatek.internal.R.drawable.sim_roaming_connected;
        }
    }

    public static void initMobileIcons() {
        if (sMobileIconResIds == null) {
            sMobileIconResIds = new int[MOBILE_ICON_COUNT];
            sMobileIconResIds[0] = R.drawable.ic_qs_mobile_blue;
            sMobileIconResIds[1] = R.drawable.ic_qs_mobile_orange;
            sMobileIconResIds[2] = R.drawable.ic_qs_mobile_green;
            sMobileIconResIds[3] = R.drawable.ic_qs_mobile_purple;
        }
    }
    
    // Gionee <fengjianyi><2013-05-08> add for CR00800567 start
    public static void gnInitMobileIcons() {
        if (sMobileIconResIds == null) {
            sMobileIconResIds = new int[MOBILE_ICON_COUNT];
            sMobileIconResIds[0] = R.drawable.zzzzz_gn_ic_qs_mobile_blue;
            sMobileIconResIds[1] = R.drawable.zzzzz_gn_ic_qs_mobile_orange;
            sMobileIconResIds[2] = R.drawable.zzzzz_gn_ic_qs_mobile_green;
            sMobileIconResIds[3] = R.drawable.zzzzz_gn_ic_qs_mobile_purple;
        }
    }
    // Gionee <fengjianyi><2013-05-08> add for CR00800567 end

    public static long getSIMIdBySlot(Context context, int slotId) {
        SIMInfo simInfo = getSIMInfoBySlot(context, slotId);
        if (simInfo == null) {
            return 0;
        }
        return simInfo.mSimId;
    }

    public static int getSIMColorIdBySlot(Context context, int slotId) {
		return 0;
		/*
        SIMInfo simInfo = getSIMInfoBySlot(context, slotId);
        if (simInfo == null) {
            return -1;
        }
        return simInfo.mColor;
        */
    }

    public static int getSIMStateIcon(SIMInfo simInfo) {
        return getSIMStateIcon(getSimIndicatorStateGemini(simInfo.mSlot));
    }

    public static int getSIMStateIcon(int simStatus) {
        if (simStatus <= -1 || simStatus >= SIM_STATUS_COUNT) {
            return -1;
        }
        if (sSimStatusViews == null) {
            initStatusIcons();
        }
        return sSimStatusViews[simStatus];
    }

    public static int getDataConnectionIconIdBySlotId(Context context, int slotId) {
        SIMInfo simInfo = getSIMInfoBySlot(context, slotId);
        if (simInfo == null) {
            return -1;
        }
        if (sMobileIconResIds == null) {
            // Gionee <fengjianyi><2013-05-08> modify for CR00800567 start
            //initMobileIcons();
        	if (ToolbarIconUtils.GN_QUICK_SETTINGS_SUPPORT) {
        		gnInitMobileIcons();
        	} else {
                initMobileIcons();
        	}
            // Gionee <fengjianyi><2013-05-08> modify for CR00800567 end
        }
        if (simInfo.mColor == -1) {
            return -1;
        } else {
            return sMobileIconResIds[simInfo.mColor];
        }
    }

    public static boolean checkSimCardDataConnBySlotId(Context context, int slotId) {
        SIMInfo simInfo = getSIMInfoBySlot(context, slotId);
        if (simInfo == null) {
            return false;
        }
        int simState = getSimIndicatorStateGemini(simInfo.mSlot);
        if (simState == FeatureOption.SIM_INDICATOR_ROAMING
                || simState == FeatureOption.SIM_INDICATOR_CONNECTED
                || simState == FeatureOption.SIM_INDICATOR_ROAMINGCONNECTED
                || simState == FeatureOption.SIM_INDICATOR_NORMAL) {
            return true;
        } else {
            return false;
        }
    }
    
    /// M: Check the data connection is connected? real one. @{
    public static boolean isTelephonyDataConnected(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null && TelephonyManager.DATA_CONNECTED == tm.getDataState()) {
            Xlog.d(TAG, "isTelephonyDataConnected called, the data state is " + tm.getDataState());
            return true;
        }
        return false;
    }
    /// M: }@
    
    /// M: check the sim card data connection is valid @{
    public static boolean checkSimCardDataConn() {
        int simState = getSimIndicatorState();
        if (simState == FeatureOption.SIM_INDICATOR_ROAMING
                || simState == FeatureOption.SIM_INDICATOR_CONNECTED
                || simState == FeatureOption.SIM_INDICATOR_ROAMINGCONNECTED
                || simState == FeatureOption.SIM_INDICATOR_NORMAL) {
            return true;
        } else {
            return false;
        }
    }
    /// M: }@

    public static boolean is3GSupported() {
        if (sBaseband == null) {
            sBaseband = SystemProperties.get("gsm.baseband.capability");
        }
        if ((sBaseband != null) && (sBaseband.length() != 0)
                && (Integer.parseInt(sBaseband) <= 3)) {
            return false;
        } else {
            return true;
        }
    }
    
    public static int getSimIndicatorState() {
        try {
             ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
             if (telephony != null) {
                 return GnITelephony.getSimIndicatorState();
             } else {
                 // This can happen when the ITelephony interface is not up yet.
                 return FeatureOption.SIM_INDICATOR_UNKNOWN;
             }
        } catch (Exception ex) {
            // the phone process is restarting.
            return FeatureOption.SIM_INDICATOR_UNKNOWN;
        }
    }
    
    public static int getSimIndicatorStateGemini(int simId) {
        try {
             ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
             if (telephony != null) {
                 final int mState = GnITelephony.getSimIndicatorStateGemini(simId);
                 if ((mState == FeatureOption.SIM_INDICATOR_CONNECTED)
                    && (TelephonyManager.DATA_CONNECTED != TelephonyManagerEx.getDefault().getDataState(simId))) {
                    /// M: Sync the data connected state with TelephonyManager, and fallback to normal.
                    Xlog.d(TAG, "getSimIndicatorStateGemini called, fallback to normal and simId is " + simId);
                    return FeatureOption.SIM_INDICATOR_NORMAL;
                 }
                 return mState;
             } else {
                 // This can happen when the ITelephony interface is not up yet.
                 return FeatureOption.SIM_INDICATOR_UNKNOWN;
             }
        } catch (Exception ex) {
            // the phone process is restarting.
            return FeatureOption.SIM_INDICATOR_UNKNOWN;
        } 
    }

    public static boolean isTelephonyDataConnectedBySimId(int simId) {
        try {
            if (TelephonyManager.DATA_CONNECTED == TelephonyManagerEx.getDefault().getDataState(simId)) {
                Xlog.d(TAG, "isTelephonyDataConnectedBySimId called, data is connected and simId is " + simId);
                return true;
            } else {
                Xlog.d(TAG, "isTelephonyDataConnectedBySimId called, data is not connected and simId is " + simId);
                return false;
            }
        } catch (NullPointerException ex) {
            return false;
        }
    }

    public static TelephonyManagerEx getDefault(Context context) {
        if (mTMEx == null) {
            mTMEx = new TelephonyManagerEx(context);
        }
        return mTMEx;
    }
    
    public static ITelephony getITelephony() {
        return sITelephony = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
    }
    
    private static ITelephonyRegistry mRegistry = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry"));
    private static ITelephonyRegistry mRegistry2 = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry2"));

    public static void listen(PhoneStateListener listener, int events, int simId) {
        try {
            Boolean notifyNow = (getITelephony() != null);
            if (FeatureOption.GEMINI_SIM_1 == simId) {
                mRegistry.listen("SystemUI SIMHelper", GnPhoneStateListener.getCallback(listener), events, notifyNow);
            } else {
                mRegistry2.listen("SystemUI SIMHelper", GnPhoneStateListener.getCallback(listener), events, notifyNow);
            }
        } catch (RemoteException ex) {
            // system process dead
        } catch (NullPointerException ex) {
            // system process dead
        }
    }
}
