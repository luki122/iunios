package com.aurora.puremanager.traffic;

import static android.net.NetworkTemplate.buildTemplateMobileAll;

import java.lang.reflect.Method;

import android.content.Context;
import android.net.NetworkTemplate;
import android.os.Build;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.gionee.framework.Common;
import com.gionee.framework.FrameworkUtility;
import com.gionee.framework.SIMInfo;
import com.gionee.framework.SIMInfoFactory;

public class MobileTemplate {
    private static final String TEST_SUBSCRIBER_PROP = "test.subscriberid";
    private static final String TAG = "MobileTemplate";

    public static NetworkTemplate getTemplate(Context context, int slotId) {
        NetworkTemplate template = buildTemplateMobileAll(getSubscriberId(context, slotId));
        template = normalize(template, context, slotId);
        return template;
    }

    public static String getSubscriberId(Context context, int slotId) {
        long simId = getSimNo(context, slotId);
        String imsiId = getImsi(simId);
        // isMobileDataAvailable(simId);
        return imsiId;
    }

    private static String getImsi(long simId) {
        Object telephonyManager = FrameworkUtility.invokeStaticMethod(TelephonyManager.class, "getDefault");
        if (Build.VERSION.SDK_INT >= 22) { // android5.1
            return (String) FrameworkUtility.invokeMethod(TelephonyManager.class, telephonyManager,
                    "getSubscriberId", new Class[] {int.class}, new Object[] {(int) simId});
        } else {
            return (String) FrameworkUtility.invokeMethod(TelephonyManager.class, telephonyManager,
                    "getSubscriberId", new Class[] {long.class}, new Object[] {simId});
        }
    }

    private static long getSimNo(Context context, int slot) {
        long simId = -1;
        SIMInfoWrapper wrapper = SIMInfoWrapper.getDefault(context);

        if (wrapper.getInsertedSimCount() == 1) {
            simId = wrapper.getInsertedSimInfo().get(0).mSimId;
        } else if (wrapper.getInsertedSimCount() == 2) {
            if (slot > 0) {
                simId = wrapper.getInsertedSimInfo().get(slot).mSimId;
            } else {
                simId = -1;
            }
        }

        return simId;
    }

    /*
     * public static void setMobileDataEnabled(Context context, boolean enabled)
     * { final TelephonyManager mTelephonyManager =
     * TelephonyManager.from(context);
     * mTelephonyManager.setDataEnabled(enabled); }
     */

    /*
     * public static boolean isMobileDataAvailable(long subId) { long
     * defaultDataSubId = SubscriptionManager.getDefaultDataSubId();
     * Log.d("imsiid", "defaultDataSubId : " + defaultDataSubId + " : "
     * +(defaultDataSubId == subId)); return defaultDataSubId == subId; }
     */

    private static boolean isMTKCdmaLteDcSupport() {
        if (SystemProperties.get("ro.mtk_svlte_support").equals("1")) {
            return true;
        } else {
            return false;
        }
    }

    private static void mtkFillTemplateForCdmaLte(Context context, NetworkTemplate template, int slotId) {
        if (isMTKCdmaLteDcSupport()) {
            Log.d(TAG, "subId:" + slotId);
            final Object teleEx = getMTKDefaultTelephonyManagerEx();
            int simId = 1;
            SIMInfo simInfo = SIMInfoFactory.getDefault().getSIMInfoBySlot(context, slotId);
            if(simInfo != null) {
                simId = (int) simInfo.mSimId;
            }
            String svlteSubscriberId = getMtkSubscriberIdForLteDcPhone(teleEx, (int)simId);
            Log.e(TAG, "getDefaultTelephonyManagerEx  svlteSubscriberId1:" + svlteSubscriberId);            
                       
            if (!(TextUtils.isEmpty(svlteSubscriberId)) && svlteSubscriberId.length() > 0) {
                Log.d(TAG, "bf:" + template);
                addMTKMatchSubscriberIds(template, svlteSubscriberId);
                Log.d(TAG, "af:" + template);
            }
        }
    }

    private static Object getMTKDefaultTelephonyManagerEx() {
        Object result = null;
        try {
            Class<?> tclass = (Class<?>) Class.forName("com.mediatek.telephony.TelephonyManagerEx");
            Method method = tclass.getDeclaredMethod("getDefault");
            result = method.invoke(null);
        } catch (Exception ex) {
            Log.e(TAG, "getDefaultTelephonyManagerEx  Exception:" + ex);
            ex.printStackTrace();
        }
        return result;
    }

    private static String getMtkSubscriberIdForLteDcPhone(Object teleEx, int subId) {
        String result = null;
        try {
            Class<?> tclass = (Class<?>) Class.forName("com.mediatek.telephony.TelephonyManagerEx");
            Method method = tclass.getDeclaredMethod("getSubscriberIdForLteDcPhone", new Class[] {int.class});
            result = (String) method.invoke(teleEx, subId);
        } catch (Exception ex) {
            Log.e(TAG, "getSubscriberIdForLteDcPhone  Exception:" + ex);
            ex.printStackTrace();
        }
        return result;
    }

    private static void addMTKMatchSubscriberIds(NetworkTemplate template, String SubscriberId) {
        try {
            Class<NetworkTemplate> nclass = NetworkTemplate.class;
            Method method = nclass.getDeclaredMethod("addMatchSubscriberIds", new Class[] {String.class});
            method.invoke(template, SubscriberId);
        } catch (Exception ex) {
            Log.e(TAG, "addMatchSubscriberIds  Exception:" + ex);
            ex.printStackTrace();
        }
    }

    private static String[] getMTKMergedSubscriberIds(Context context) {
        return (String[]) FrameworkUtility.invokeMethod(TelephonyManager.class,
                TelephonyManager.from(context), "getMergedSubscriberIds");
    }

    private static NetworkTemplate normalize(NetworkTemplate template, String[] merged) {
        if (Build.VERSION.SDK_INT >= 22) {// android 5.1
            return (NetworkTemplate) FrameworkUtility.invokeStaticMethod(NetworkTemplate.class, "normalize",
                    new Class[] {NetworkTemplate.class, String[].class}, new Object[] {template, merged});
        }
        return template;
    }

    private static NetworkTemplate normalize(NetworkTemplate template, Context context, int simIndex) {
        // android 5.1 MTK
        if (Build.VERSION.SDK_INT >= 22 && Common.getPlatform().equals(Common.MTK_PLATFORM)) {
            try {
                mtkFillTemplateForCdmaLte(context, template, simIndex);
                template = normalize(template, getMTKMergedSubscriberIds(context));
            } catch (Exception e) {
                Log.e(TAG, "mtk normalize ", e);
            }
        }
        return template;
    }

}