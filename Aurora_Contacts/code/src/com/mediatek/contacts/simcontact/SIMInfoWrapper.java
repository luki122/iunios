package com.mediatek.contacts.simcontact;

import java.util.HashMap;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.util.Log;
import android.widget.CursorAdapter;

import gionee.provider.GnTelephony.SIMInfo;
import gionee.provider.GnTelephony.SimInfo;
import gionee.provider.GnTelephony;

import com.android.contacts.ContactsApplication;
import com.android.contacts.GNContactsUtils;
import com.android.internal.telephony.TelephonyIntents;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;

import android.os.Handler;
import android.os.RegistrantList;

import com.gionee.internal.telephony.GnTelephonyManagerEx;

public class SIMInfoWrapper {
    private final static String TAG = "SIMInfoWrapper";
    private final static boolean DBG = true;
    private Context mContext;
    private List<SIMInfo> mAllSimInfoList = null;
    private List<SIMInfo> mInsertedSimInfoList = null;
    
    private HashMap<Integer,SIMInfo> mAllSimInfoMap = new HashMap<Integer, SIMInfo>();
    private HashMap<Integer,SIMInfo> mInsertedSimInfoMap = new HashMap<Integer, SIMInfo>();
    private HashMap<Integer,Integer> mSlotIdSimIdPairs = new HashMap<Integer,Integer>();
    private HashMap<Integer,Integer> mSimIdSlotIdPairs = new HashMap<Integer,Integer>();
    private boolean mInsertSim = false;
    private int mAllSimCount = 0;
    private int mInsertedSimCount = 0;
    private RegistrantList mSimInfoUpdateRegistrantList = new RegistrantList();
    private static SIMInfoWrapper sSIMInfoWrapper;
    
    public void updateSimInfoCache() {
        new Thread() {
            public void run() {
                if (mAllSimInfoList != null) {
                    mAllSimInfoList = SIMInfo.getAllSIMList(mContext);
                    if (mAllSimInfoList != null) {
                        mAllSimCount = mAllSimInfoList.size();
                        mAllSimInfoMap = new HashMap<Integer, SIMInfo>();
                        mSimIdSlotIdPairs = new HashMap<Integer, Integer>();
                        for (SIMInfo item : mAllSimInfoList) {
                            int simId = getCheckedSimId(item);
                            if (simId != -1) {
                                mAllSimInfoMap.put(simId, item);
                                mSimIdSlotIdPairs.put(simId, item.mSlot);
                            }
                        }
                        if (DBG)
                            log("[updateSimInfo] update mAllSimInfoList");
                    } else {
                        if (DBG)
                            log("[updateSimInfo] updated mAllSimInfoList is null");
                        return;
                    }
                }
                
                if (mInsertedSimInfoList != null) {
                    mInsertedSimInfoList = SIMInfo.getInsertedSIMList(mContext);
                    if (mInsertedSimInfoList != null) {
                        mInsertedSimCount = mInsertedSimInfoList.size();
                        mInsertedSimInfoMap = new HashMap<Integer, SIMInfo>();
                        mSlotIdSimIdPairs = new HashMap<Integer, Integer>();
                        for (SIMInfo item : mInsertedSimInfoList) {
                            int simId = getCheckedSimId(item);
                            if (simId != -1) {
                                mInsertedSimInfoMap.put(simId, item);
                                mSlotIdSimIdPairs.put(item.mSlot, simId);
                            }
                        }
                        if (DBG)
                            log("[updateSimInfo] update mInsertedSimInfoList");
                    } else {
                        if (DBG)
                            log("[updateSimInfo] updated mInsertedSimInfoList is null");
                        return;
                    }
                }
                mSimInfoUpdateRegistrantList.notifyRegistrants();
            }
        }.start();
    }
    
    /**
     * SIMInfo wrapper constructor. Build simInfo according to input type
     * 
     * @param context
     * @param isInsertSimOrAll
     */
    private SIMInfoWrapper(Context context) {
        mContext = context;
        
        if (!FeatureOption.MTK_GEMINI_SUPPORT) {// aurora wangth modify for gemini only
            return;
        }
        
        mAllSimInfoList = SIMInfo.getAllSIMList(context);
        mInsertedSimInfoList = SIMInfo.getInsertedSIMList(context);

        if (mAllSimInfoList == null || mInsertedSimInfoList == null) {
            log("[SIMInfoWrapper] mSimInfoList OR mInsertedSimInfoList is nulll");
            return;
        }

        mAllSimCount = mAllSimInfoList.size();
        mInsertedSimCount = mInsertedSimInfoList.size();

        mAllSimInfoMap = new HashMap<Integer, SIMInfo>();
        mInsertedSimInfoMap = new HashMap<Integer, SIMInfo>();
        mSlotIdSimIdPairs = new HashMap<Integer,Integer>();
        mSimIdSlotIdPairs = new HashMap<Integer,Integer>();
        
        for (SIMInfo item : mAllSimInfoList) {
            int simId = getCheckedSimId(item);
            if (simId != -1) {
                mAllSimInfoMap.put(simId, item);
                mSimIdSlotIdPairs.put(simId, item.mSlot);
            }
        }

        for (SIMInfo item : mInsertedSimInfoList) {
            int simId = getCheckedSimId(item);
            if (simId != -1) {
                mInsertedSimInfoMap.put(simId, item);
                mSlotIdSimIdPairs.put(item.mSlot, simId);
            }
        }
        
        mContext.getContentResolver().registerContentObserver(SimInfo.CONTENT_URI, true, mSimInfoObaerver); 

    }

    private int getCheckedSimId(SIMInfo item) {
        if (item != null && item.mSimId > 0) {
            return (int) item.mSimId;
        } else {
            if (DBG)
                log("[getCheckedSimId]Wrong simId is "
                        + (item == null ? -1 : item.mSimId));
            return -1;
        }
    }

    /**
     * Public API to get SIMInfoWrapper instance
     * 
     * @param context
     * @param isInsertSim
     * @return SIMInfoWrapper instance
     */
    public static SIMInfoWrapper getDefault() {
        if (sSIMInfoWrapper == null)
            sSIMInfoWrapper = new SIMInfoWrapper(ContactsApplication.getInstance());
        return sSIMInfoWrapper;
    }

    public static SIMInfoWrapper getSimWrapperInstanceUnCheck() {
        return sSIMInfoWrapper;
    }

    /**
     * get cached SIM info list
     * 
     * @return
     */
    public List<SIMInfo> getSimInfoList() {
        if (mInsertSim) {
            return mInsertedSimInfoList;
        } else {
            return mAllSimInfoList;
        }
    }

    /**
     * get cached SIM info list
     * 
     * @return
     */
    public List<SIMInfo> getAllSimInfoList() {
        return mAllSimInfoList;
    }

    /**
     * get cached SIM info list
     * 
     * @return
     */
    public List<SIMInfo> getInsertedSimInfoList() {
        return mInsertedSimInfoList;
    }

    /**
     * get SimInfo cached HashMap
     * 
     * @return
     */
    public HashMap<Integer, SIMInfo> getSimInfoMap() {
        return mAllSimInfoMap;
    }

    /**
     * get SimInfo cached HashMap
     * 
     * @return
     */
    public HashMap<Integer, SIMInfo> getInsertedSimInfoMap() {
        return mInsertedSimInfoMap;
    }

    /**
     * get cached SimInfo from HashMap
     * 
     * @param id
     * @return
     */
    public SIMInfo getSimInfoById(int id) {
        return mAllSimInfoMap.get(id);
    }

    public SIMInfo getSimInfoBySlot(int slot) {
        SIMInfo simInfo = null;
        for (int i = 0; i < mInsertedSimCount; i++) {
            simInfo = mInsertedSimInfoList.get(i);
            if (simInfo.mSlot == slot)
                return simInfo;
        }
        return simInfo;
    }

    /**
     * get SIM color according to input id
     * 
     * @param id
     * @return
     */
    public int getSimColorById(int id) {
        SIMInfo simInfo = mAllSimInfoMap.get(id);
        return (simInfo == null) ? -1 : simInfo.mColor;
    }

    /**
     * get SIM display name according to input id
     * 
     * @param id
     * @return
     */
    public String getSimDisplayNameById(int id) {
        SIMInfo simInfo = mAllSimInfoMap.get(id);
        return (simInfo == null) ? null : simInfo.mDisplayName;
    }

    /**
     * get SIM slot according to input id
     * 
     * @param id
     * @return
     */
    public int getSimSlotById(int id) {
        SIMInfo simInfo = mAllSimInfoMap.get(id);
        return (simInfo == null) ? -1 : simInfo.mSlot;
    }

    /**
     * get cached SimInfo from HashMap
     * 
     * @param id
     * @return
     */
    public SIMInfo getInsertedSimInfoById(int id) {
        return mInsertedSimInfoMap.get(id);
    }

    /**
     * get SIM color according to input id
     * 
     * @param id
     * @return
     */
    public int getInsertedSimColorById(int id) {
        SIMInfo simInfo = mInsertedSimInfoMap.get(id);
        return (simInfo == null) ? -1 : simInfo.mColor;
    }

    /**
     * get SIM display name according to input id
     * 
     * @param id
     * @return
     */
    public String getInsertedSimDisplayNameById(int id) {
        SIMInfo simInfo = mInsertedSimInfoMap.get(id);
        return (simInfo == null) ? null : simInfo.mDisplayName;
    }

    /**
     * get SIM slot according to input id
     * 
     * @param id
     * @return
     */
    public int getInsertedSimSlotById(int id) {
        SIMInfo simInfo = mInsertedSimInfoMap.get(id);
        return (simInfo == null) ? -1 : simInfo.mSlot;
    }

    /**
     * get all SIM count according to Input
     * 
     * @return
     */
    public int getAllSimCount() {
        return mAllSimCount;
    }

    /**
     * get inserted SIM count according to Input
     * 
     * @return
     */
    public int getInsertedSimCount() {
        return mInsertedSimCount;
    }
    
    public int getSlotIdBySimId(int simId) {
        // return mSlotIdSimIdPairs.get(simId);
        
        // qc begin
        if (!FeatureOption.MTK_GEMINI_SUPPORT) {
            return 0;
        }
        // qc end
        
        Integer i = mSimIdSlotIdPairs.get(simId);
        return ((i==null) ? -1 : i);
    }

    public int getSimIdBySlotId(int slotId) {
        // return mSimIdSlotIdPairs.get(slotId);
        Integer i = mSlotIdSimIdPairs.get(slotId);
        return ((i==null) ? -1 : i);
    }

    /**
     * Get Sim Display Name according to slot id
     * 
     * @param slotId
     * @return
     */
    public String getSimDisplayNameBySlotId(int slotId) {
        // Gionee:wangth 20130307 add for CR00774316 begin
        if (GNContactsUtils.isOnlyQcContactsSupport() && mContext != null) {
            return GnTelephonyManagerEx.getDefault().getMultiSimName(mContext, slotId);
        }
        // Gionee:wangth 20130307 add for CR00774316 end
        
        String simDisplayName = null;
        int i = getSimIdBySlotId(slotId);
        simDisplayName = getSimDisplayNameById(i);
        return simDisplayName;
    }

    public void registerForSimInfoUpdate(Handler h, int what, Object obj) {
        mSimInfoUpdateRegistrantList.addUnique(h, what, obj);
    }

    public void unregisterForSimInfoUpdate(Handler h) {
        mSimInfoUpdateRegistrantList.remove(h);
    }
    
    public int getSimBackgroundResByColorId(int colorId){
    	log("getSimBackgroundResByColorId() colorId = " + colorId );
        if (colorId < 0 || colorId >3){
        	colorId = 0;
        }
        return GnTelephony.SIMBackgroundRes[colorId];
    }

    private void log(String msg) {
        Log.i(TAG, msg);
    }
 
    private ContentObserver mSimInfoObaerver = new ContentObserver(new Handler()) { 

            @Override
            public void onChange(boolean selfChange) { 
                 super.onChange(selfChange); 
                 if(sSIMInfoWrapper != null) {
                	 updateSimInfoCache();
                 }
            }   

    }; 

}
