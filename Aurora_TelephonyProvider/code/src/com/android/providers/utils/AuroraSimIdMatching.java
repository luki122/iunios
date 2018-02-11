package com.android.providers.utils;

import java.util.HashMap;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.Process;
import android.util.Log;

import gionee.provider.GnTelephony.SIMInfo;

public class AuroraSimIdMatching {

    private static AuroraSimIdMatching sInstance;
    private Context mContext;
    
    private final String system_mms = "com.android.mms";
    private final String youni_mms = "com.snda.youni.mms";
    private final String youni = "com.snda.youni";
    private final String go_mms = "com.jb.gosms";
    private final String temcent_mms = "com.tencent.qqphonebook";
    private final String qihoo_mms = "com.qihoo360.contacts";
    
    private AuroraSimIdMatching(Context context){
        init(context);
    }
    
    public static synchronized AuroraSimIdMatching getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AuroraSimIdMatching(context);
        }
        return sInstance;
    }
    
    private void init(Context context){
        mContext = context;
    }
    
    private int getUidFromPackgeName(String packgeName){
        int uid = -1;
            try {
             PackageManager pm = mContext.getPackageManager();
             ApplicationInfo ai = pm.getApplicationInfo(packgeName, PackageManager.GET_ACTIVITIES);
             uid = ai.uid;
            } catch (NameNotFoundException e) {
              e.printStackTrace();
            }
            return uid;
    }
    
    public String getAuroraSimId(String simId){
        String auroraSimId = simId;
        int uid = Binder.getCallingUid();
        if(Process.SYSTEM_UID == uid
                || uid == getUidFromPackgeName(system_mms)){
        } else if(uid == getUidFromPackgeName(youni)
                || uid == getUidFromPackgeName(youni_mms)) {
            auroraSimId = getAuroraSimIdFromYouNi(simId);
        } else if (uid == getUidFromPackgeName(go_mms)){
            auroraSimId = getAuroraSimIdFromGo(simId);
        } else {
            Log.i("AuroraSimIdMatching","simId没有匹配到, uid = " + uid);
        }
        return auroraSimId;
    }
    
    private String getAuroraSimIdFromYouNi(String simId){
        int id = Integer.valueOf(simId);
        return getAuroraSimIdFromCard(id - 1);
    }
    
    private String getAuroraSimIdFromGo(String simId){
        int id = Integer.valueOf(simId);
        return getAuroraSimIdFromCard(id);
    }
    
    private String getAuroraSimIdFromCard(int position){
        String auroraSimId = null;
        SIMInfo info = SIMInfo.getSIMInfoBySlot(mContext, position);
        if(info.mSimId > -1){
            auroraSimId = String.valueOf(info.mSimId);
        }
        return auroraSimId;
    }
}
