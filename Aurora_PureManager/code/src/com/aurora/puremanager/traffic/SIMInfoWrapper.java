//Gionee <jianghuan> <2013-09-29> add for CR00975553 begin
package com.aurora.puremanager.traffic;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.gionee.framework.SIMInfo;
import com.gionee.framework.SIMInfoFactory;

public class SIMInfoWrapper {
    private final TelephonyManager mTelephonyManager;
    private static List<SIMParame> sInsertedSimInfoList = new ArrayList<SIMParame>();
    private static SIMInfoWrapper sSIMInfoWrapper;
    private static Context mContext;
    private int mInsertedSimCount = 0;

    private SIMInfoWrapper(Context context) {
        mTelephonyManager = TelephonyManager.from(context);
        List<SIMInfo> simInfoList = SIMInfoFactory.getDefault().getInsertedSIMList(context);
        if (simInfoList == null) {
            return;
        }
        mInsertedSimCount = simInfoList.size();
        sInsertedSimInfoList.clear();
        SIMParame simParame = null;
        for (SIMInfo siminfo : simInfoList) {
            simParame = new SIMParame();
            simParame.mSimId = (siminfo.mSimId > 0) ? siminfo.mSimId : 0;
            simParame.mDisplayName = siminfo.mDisplayName;
            simParame.mSlot = (siminfo.mSlot > 0) ? siminfo.mSlot : 0;
            sInsertedSimInfoList.add(simParame);
        }
    }

    public static SIMInfoWrapper getDefault(Context context) {
        mContext = context;

        if (sSIMInfoWrapper == null) {
            sSIMInfoWrapper = new SIMInfoWrapper(context);
        }
        return sSIMInfoWrapper;
    }

    public static void setEmptyObject(Context context) {
        if (sSIMInfoWrapper != null) {
            sSIMInfoWrapper = null;
        }
    }

    public SIMParame getSimInfoBySlot(int slot) {
        SIMParame simInfo = null;
        for (int i = 0; i < mInsertedSimCount; i++) {
            simInfo = sInsertedSimInfoList.get(i);
            if (simInfo.mSlot == slot)
                return simInfo;
        }
        return null;// simInfo;
    }

    public int getInsertedSimCount() {
        return mInsertedSimCount;
    }

    public List<SIMParame> getInsertedSimInfo() {
        return sInsertedSimInfoList;
    }

    public int getSimIndex_CurrentNetworkActivated() {
        // Gionee <jianghuan> <2013-12-13> modify for CR00972909 begin
        int mNetworkIndex = -1;
        int simid = SIMInfoFactory.getDefault().getDefaultDataSubId();
        for (SIMParame siminfo : sInsertedSimInfoList) {
            // Gionee: mengdw <2015-08-25> modify for CR01543192 begin
            if (null != siminfo && siminfo.mSimId == simid) {
                mNetworkIndex = siminfo.mSlot;
            }
            // Gionee: mengdw <2015-08-25> modify for CR01543192 end
        }
        /*
         * if(TrafficAssistantMainActivity.sGEMINISUPPORT){ for (SIMParame
         * siminfo : sInsertedSimInfoList) { if (siminfo.mSimId == simid) {
         * mNetworkIndex = siminfo.mSlot; } } }else{ mNetworkIndex =
         * 0;//(int)simid; }
         */
        return mNetworkIndex;
    }

    public boolean gprsIsOpenMethod(String methodName) {
        /*
         * ConnectivityManager mCM = (ConnectivityManager)
         * mContext.getSystemService(Context.CONNECTIVITY_SERVICE); Class[]
         * argClasses = null; Object[] argObject = null;
         * 
         * Boolean isOpen = false; try { Method method =
         * mCM.getClass().getMethod(methodName, argClasses); isOpen = (Boolean)
         * method.invoke(mCM, argObject); } catch (Exception e) {
         * e.printStackTrace(); } return isOpen;
         */

        return mTelephonyManager.getDataEnabled();
    }

    public void setGprsEnable(String methodName, boolean isEnable) {
        /*
         * ConnectivityManager mCM = (ConnectivityManager)
         * mContext.getSystemService(Context.CONNECTIVITY_SERVICE); Class[]
         * argClasses = new Class[1]; argClasses[0] = boolean.class;
         * 
         * try { Method method = mCM.getClass().getMethod(methodName,
         * argClasses); method.invoke(mCM, isEnable); } catch (Exception e) {
         * e.printStackTrace(); }
         */
        mTelephonyManager.setDataEnabled(isEnable);
    }

    public boolean isWiFiActived() {
        ConnectivityManager connectMgr = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetInfo.isConnected()) {
            return true;
        }
        return false;
    }
}
// Gionee <jianghuan> <2013-09-29> add for CR00975553 end