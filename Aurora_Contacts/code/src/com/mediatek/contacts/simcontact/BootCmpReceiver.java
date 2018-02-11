/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.            
 *              
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.contacts.simcontact;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.provider.Settings;
import android.os.Bundle;
import android.os.ServiceManager;

import com.android.contacts.ContactsUtils;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.activities.SystemUtils;
//import com.mediatek.featureoption.FeatureOption;
import com.android.internal.telephony.*;

import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.RawContacts;
import gionee.provider.GnTelephony.SIMInfo;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.IccCard;

import com.mediatek.contacts.ContactsFeatureConstants;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.simcontact.AbstractStartSIMService;
import com.mediatek.contacts.simcontact.StartSIMService;
import com.mediatek.contacts.simcontact.StartSIMService2;

import android.os.Build;

public class BootCmpReceiver extends BroadcastReceiver {
	private static final String TAG = "liyang-BootCmpReceiver";
	private static Context mContext = null;
    
	private static final String PHONE_NAME_KEY = "phoneName";
	private static final String INTENT_KEY_ICC_STATE = "ss";
	private static final String INTENT_VALUE_ICC_ABSENT = "ABSENT";
	private static final String INTENT_VALUE_ICC_LOCKED = "LOCKED";
	
    private static String INTENT_SIM_FILE_CHANGED = "android.intent.action.sim.SIM_FILES_CHANGED"; 
    private static String INTENT_SIM_FILE_CHANGED_2 = "android.intent.action.sim.SIM_FILES_CHANGED_2";
    
    public void onReceive(Context context, Intent intent) {
    	Log.v("broadcast", "BootCmpReceiver");
        if (GNContactsUtils.isOnlyQcContactsSupport()) {
            return;
        }
        
        mContext = context;
        Log.i(TAG, "In onReceive ");
        final String action = intent.getAction();
        Log.i(TAG, "action is " + action);

        if (action.equals("android.intent.action.PHB_STATE_CHANGED")) {
            processPhoneBookChanged(intent);
        } else if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
            processAirplaneModeChanged(intent);
        } else if (action.equals("android.intent.action.DUAL_SIM_MODE")) {
            processDualSimModeChanged(intent);
        } else if (action.equals("android.intent.action.SIM_INFO_UPDATE")) {
            Log.i(TAG, "processSimInfoUpdate");
//            processSimInfoUpdate(intent);
        } else if(action.equals("android.intent.action.SIM_STATE_CHANGED")) {
            auroraProcessSimStateChanged(intent);        
        } else if (action.equals("android.intent.action.SIM_STATE_CHANGED_EXTEND")) {
            processSimStateChanged(intent);
        } else if (action.equals(INTENT_SIM_FILE_CHANGED)) {// SIM REFERSH
            processSimFilesChanged(0);
        } else if (action.equals(INTENT_SIM_FILE_CHANGED_2)) {// SIM REFRESH
            processSimFilesChanged(1);
        } else if (action.equals("android.intent.action.SIM_SETTING_INFO_CHANGED")) {
            processSimInfoUpdateForSettingChanged(intent);
        } else if (action.equals("android.intent.action.ACTION_SHUTDOWN_IPO")) {
    	    processIpoShutDown();
        } else if (action.equals("android.intent.action.ACTION_PHONE_RESTART")) {
            processPhoneReset(intent);
        }
    }

    public void startSimService(int slotId, int workType) {
        Intent intent = null;
        if (slotId == 0) {
            intent = new Intent(mContext, StartSIMService.class);
        } else {
            intent = new Intent(mContext, StartSIMService2.class);
        }
        
        intent.putExtra(AbstractStartSIMService.SERVICE_SLOT_KEY, slotId);
        intent.putExtra(AbstractStartSIMService.SERVICE_WORK_TYPE, workType);
        Log.i(TAG, "[startSimService]slotId:" + slotId + "|workType:" +workType);
        mContext.startService(intent);
    }
    
    void processPhoneBookChanged(Intent intent) {
        Log.i(TAG, "processPhoneBookChanged");
        boolean phbReady = intent.getBooleanExtra("ready", false);
        int slotId;
        if(Build.VERSION.SDK_INT < 21) {
            slotId = intent.getIntExtra("simId", -10);
        } else {
            // Auroraxuyong 2015-07-22 modified for bug #14263 start
            //int subId = intent.getIntExtra("subscription", -10);
            slotId = getSlotBySubId(intent.getIntExtra("subscription", -1));
            // Auroraxuyong 2015-07-22 modified for bug #14263 end
        } 
        Log.i(TAG, "[processPhoneBookChanged]phbReady:" + phbReady + "|slotId:" + slotId);
        if (slotId >= 0) {
        	 if(Build.VERSION.SDK_INT < 21) { 
        		 if(phbReady) {
        			 startSimService(slotId, AbstractStartSIMService.SERVICE_WORK_IMPORT);
        		 }
        	 } else {
             	// Auroraxuyong 2015-07-22 modified for bug #14263 start
                 startSimService(slotId, phbReady ? AbstractStartSIMService.SERVICE_WORK_IMPORT : AbstractStartSIMService.SERVICE_WORK_REMOVE);
                 // Auroraxuyong 2015-07-22 modified for bug #14263 end
        	 }
            SIMInfoWrapper simInfoWrapper = SIMInfoWrapper.getSimWrapperInstanceUnCheck();
            if (simInfoWrapper != null) {
                simInfoWrapper.updateSimInfoCache();
            }
        }
    }
    
    void processAirplaneModeChanged(Intent intent) {
        Log.i(TAG, "processAirplaneModeChanged");
        boolean isAirplaneModeOn = intent.getBooleanExtra("state", false);
        Log.i(TAG, "[processAirplaneModeChanged]isAirplaneModeOn:" + isAirplaneModeOn);
        if (isAirplaneModeOn) {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
                startSimService(1, AbstractStartSIMService.SERVICE_WORK_REMOVE);
            } else {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
            }
        } else {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_IMPORT);
                startSimService(1, AbstractStartSIMService.SERVICE_WORK_IMPORT);
            } else {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_IMPORT);
            }
        }
    }
    
    /**
     * Dual Sim mode is only for Gemini Feature.
     * 0 for none sim, 1 for sim1 only, 2 for sim2 only, 3 for dual sim
     * And the deefault mode 3
     * 
     * The change map is as following 
     *  
     *              => (Mode 1) <=
     *            ==              == 
     * (Mode 3) <=                  => (Mode 0)
     *            ==              ==
     *              => (Mode 2) <=
     * 
     * @param intent
     */
    void processDualSimModeChanged(Intent intent) {
        Log.i(TAG, "processDualSimModeChanged");
        // Intent.EXTRA_DUAL_SIM_MODE = "mode";
        int type = intent.getIntExtra("mode", -1);
        
        SharedPreferences prefs = mContext.getSharedPreferences(
                "sim_setting_preference", Context.MODE_PRIVATE);
        int prevType = prefs.getInt("dual_sim_mode", 3);
        
        Log.i(TAG, "[processDualSimModeChanged]type:" + type + "|prevType:" + prevType);
        switch(type) {
        case 0: {
            if (prevType == 1) {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
            } else if (prevType == 2) {
                startSimService(1, AbstractStartSIMService.SERVICE_WORK_REMOVE);
            } else {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
                startSimService(1, AbstractStartSIMService.SERVICE_WORK_REMOVE);
            }
            break;
        }
        case 1: {
            if (prevType == 0) {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_IMPORT);
            } else if (prevType == 3) {
                startSimService(1, AbstractStartSIMService.SERVICE_WORK_REMOVE);
            } else {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_IMPORT);
                startSimService(1, AbstractStartSIMService.SERVICE_WORK_REMOVE);
            }
            break;
        }
        case 2: {
            if (prevType == 0) {
                startSimService(1, AbstractStartSIMService.SERVICE_WORK_IMPORT);
            } else if (prevType == 3) {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
            } else {
                startSimService(1, AbstractStartSIMService.SERVICE_WORK_IMPORT);
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
            }
            break;
        }
        case 3: {
            if (prevType == 1) {
                startSimService(1, AbstractStartSIMService.SERVICE_WORK_IMPORT);
            } else if (prevType == 2) {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_IMPORT);
            } else {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_IMPORT);
                startSimService(1, AbstractStartSIMService.SERVICE_WORK_IMPORT);
            }
            break;
        }
            default:
                break;
        }
        
      SharedPreferences.Editor editor= prefs.edit();
      editor.putInt("dual_sim_mode", type);
      editor.commit();
    }
    
    void processSimStateChanged(Intent intent) {
        Log.i(TAG, "processSimStateChanged");
        String phoneName = intent.getStringExtra(PHONE_NAME_KEY);
        String iccState = intent.getStringExtra(INTENT_KEY_ICC_STATE);
        int slotId = intent.getIntExtra(ContactsFeatureConstants.GEMINI_SIM_ID_KEY, -1);

        Log.i(TAG, "mPhoneName:" + phoneName + "|mIccStae:" + iccState
                + "|mySlotId:" + slotId);
        // Check SIM state, and start service to remove old sim data if sim
        // is not ready.
        if (INTENT_VALUE_ICC_ABSENT.equals(iccState)) {
            SIMInfoWrapper simInfoWrapper = SIMInfoWrapper.getSimWrapperInstanceUnCheck();
            if (simInfoWrapper != null) {
                simInfoWrapper.updateSimInfoCache();
            }
        }
        if (INTENT_VALUE_ICC_ABSENT.equals(iccState)
                || INTENT_VALUE_ICC_LOCKED.equals(iccState)
                || "NETWORK".equals(iccState)) {
            startSimService(slotId, AbstractStartSIMService.SERVICE_WORK_REMOVE);
        }
    }
    
    void processSimFilesChanged(int slotId) {
        Log.i(TAG, "processSimStateChanged:" + slotId);
        if (SimCardUtils.isPhoneBookReady(slotId)) {
            startSimService(slotId, AbstractStartSIMService.SERVICE_WORK_IMPORT);
        }
    }
    
    void processSimInfoUpdateForSettingChanged(Intent intent) {
        Log.i(TAG, "processSimInfoUpdateForSettingChanged:" + intent.toString());
        SIMInfoWrapper simInfoWrapper = SIMInfoWrapper.getSimWrapperInstanceUnCheck();
        if (simInfoWrapper != null) {
            simInfoWrapper.updateSimInfoCache();
        } else {
            SIMInfoWrapper.getDefault();
        }
    }
    
    void processSimInfoUpdate(Intent intent) {
        Log.i(TAG, "processSimInfoUpdate:" + intent.toString());
        SIMInfoWrapper simInfoCache = SIMInfoWrapper.getDefault();
        if (simInfoCache == null)
            return;
        
        SharedPreferences prefs = mContext.getSharedPreferences(
                "sim_setting_preference", Context.MODE_PRIVATE);
        
        long oldSimIdInSlot0 = prefs.getLong("slot_0", -1);
        long oldSimIdInSlot1 = prefs.getLong("slot_1", -1);
        
        simInfoCache.updateSimInfoCache();
        
        List<SIMInfo> allSimInfoList = SIMInfo.getAllSIMList(mContext);
        
        boolean slot0Update= false;
        boolean slot1Update= false;
        
        for(SIMInfo simInfo: allSimInfoList) {
            long newSimId = simInfo.mSimId;
            int newSlotId = simInfo.mSlot;
            long oldSimId = -1;
            if (newSlotId == SimCardUtils.SimSlot.SLOT_ID1) {
                //for Single card, or slot0 in Gemini.
                if (oldSimIdInSlot0 != newSimId) {
                    oldSimId = oldSimIdInSlot0;
                    SharedPreferences.Editor editor= prefs.edit();
                    editor.putLong("slot_0", newSimId);
                    editor.commit();
                }
                slot0Update = true;
            } else if (newSlotId == SimCardUtils.SimSlot.SLOT_ID2) {
                //Only for slot1 in Gemini
                if (oldSimIdInSlot1 != newSimId) {
                    oldSimId = oldSimIdInSlot1;
                    SharedPreferences.Editor editor= prefs.edit();
                    editor.putLong("slot_1", newSimId);
                    editor.commit();
                }
                slot1Update = true;
            }
            if (oldSimId >= 0) {
                final long prevSimId = oldSimId;
                final long currSimId = newSimId;
                new Thread() {
                    public void run() {
                        
                    ContentValues values = new ContentValues(1);
                    values.put(RawContacts.INDICATE_PHONE_SIM, currSimId);
                    String where = RawContacts.INDICATE_PHONE_SIM + "=" + prevSimId;
                    
                    mContext.getContentResolver().update(RawContacts.CONTENT_URI,
                            values, where, null);
                    mContext.getContentResolver().update(Contacts.CONTENT_URI,
                            values, where, null);
                    }
                }.start();
            }
        }
        if (!slot0Update) {
            SharedPreferences.Editor editor= prefs.edit();
            editor.putLong("slot_0", -1);
            editor.commit();
        }
        if (!slot1Update) {
            SharedPreferences.Editor editor= prefs.edit();
            editor.putLong("slot_1", -1);
            editor.commit();
        }
    }

    void processIpoShutDown() {
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
            startSimService(1, AbstractStartSIMService.SERVICE_WORK_REMOVE);
        } else {
            startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
        }
    }

    void processPhoneReset(Intent intent) {
	    Log.i(TAG, "processPhoneReset");
	    SIMInfoWrapper simInfoWrapper = SIMInfoWrapper.getSimWrapperInstanceUnCheck();
        if (simInfoWrapper != null) {
            simInfoWrapper.updateSimInfoCache();
        }
    	if (FeatureOption.MTK_GEMINI_SUPPORT) {
	        int slotId = intent.getIntExtra("SimId", -1);
	        if(slotId != -1){    
		        Log.i(TAG, "processPhoneReset" + slotId);
            	startSimService(slotId, AbstractStartSIMService.SERVICE_WORK_IMPORT);
	        }
        } else {
	    Log.i(TAG, "processPhoneReset0");
            startSimService(0, AbstractStartSIMService.SERVICE_WORK_IMPORT);
        }
    }    
    
    public static int getSlotBySubId(int subId) {
     	int slot = SystemUtils.getSlotId(subId);
    	SharedPreferences sp = mContext.getSharedPreferences("simslot", Context.MODE_PRIVATE);
     	if(slot > -1) {
     		SharedPreferences.Editor editor =  sp.edit();
     		editor.putInt(subId+ "", slot);
     		editor.commit();
     	} else {
     		slot = sp.getInt(subId+"", -1);
     	}     	
 
		return slot;
    }
    
    private void auroraProcessSimStateChanged(Intent intent) {
        if(Build.VERSION.SDK_INT < 21) {
        	return;
        }
        Log.i(TAG, "AuroraProcessSimStateChanged");
        String phoneName = intent.getStringExtra(PHONE_NAME_KEY);
        String iccState = intent.getStringExtra(INTENT_KEY_ICC_STATE);
        int slotId = intent.getIntExtra(ContactsFeatureConstants.GEMINI_SIM_ID_KEY, -1);
        boolean simAbsent0 = (TelephonyManager.SIM_STATE_ABSENT == gionee.telephony.GnTelephonyManager.getSimStateGemini(0));
        boolean simAbsent1 = (TelephonyManager.SIM_STATE_ABSENT == gionee.telephony.GnTelephonyManager.getSimStateGemini(1));

        Log.i(TAG, "mPhoneName:" + phoneName + "|mIccStae:" + iccState + " simAbsent0 = " + simAbsent0 + " simAbsent1 = " + simAbsent1);
 
        if (INTENT_VALUE_ICC_ABSENT.equals(iccState)
                || INTENT_VALUE_ICC_LOCKED.equals(iccState)
                || "NETWORK".equals(iccState)) {
        	if(simAbsent0) {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);	
        	}
           	if(simAbsent1) {
                startSimService(1, AbstractStartSIMService.SERVICE_WORK_REMOVE);	
        	}

        }
    }
}
