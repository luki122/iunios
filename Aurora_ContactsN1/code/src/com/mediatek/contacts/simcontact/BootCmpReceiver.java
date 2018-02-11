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
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.provider.Settings;
import android.net.Uri;
import android.os.Bundle;
import android.os.ServiceManager;

import com.android.contacts.ContactsUtils;
import com.android.contacts.GNContactsUtils;
//import com.mediatek.featureoption.FeatureOption;
import com.android.internal.telephony.*;

import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.IccCard;
import com.mediatek.contacts.ContactsFeatureConstants;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.simcontact.AbstractStartSIMService;
import com.mediatek.contacts.simcontact.StartSIMService;
import com.mediatek.contacts.simcontact.StartSIMService2;
import android.provider.ContactsContract;

public class BootCmpReceiver extends BroadcastReceiver {
	private static final String TAG = "BootCmpReceiver";
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
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
        	clearContacts();//clear contacts where deleted=1 and sync4 is null
        	 processBootComplete(context);
        }
    }

    private void startSimService(int slotId, int workType) {
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
    
    private void processPhoneBookChanged(Intent intent) {
        Log.i(TAG, "processPhoneBookChanged");
        boolean phbReady = intent.getBooleanExtra("ready", false);
        // Auroraxuyong 2015-07-22 modified for bug #14263 start
        //int subId = intent.getIntExtra("subscription", -10);
        int slotId = getSlotBySubId(intent.getIntExtra("subscription", -1));
        // Auroraxuyong 2015-07-22 modified for bug #14263 end
        Log.i(TAG, "[processPhoneBookChanged]phbReady:" + phbReady + "|slotId:" + slotId);
        if (slotId >= 0) {
        	// Auroraxuyong 2015-07-22 modified for bug #14263 start
            startSimService(slotId, phbReady ? AbstractStartSIMService.SERVICE_WORK_IMPORT : AbstractStartSIMService.SERVICE_WORK_REMOVE);
            // Auroraxuyong 2015-07-22 modified for bug #14263 end
        }
    }
  
    
    private static int getSlotBySubId(int subId) {
     	int slot = android.telephony.SubscriptionManager.getSlotId(subId);
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
    
 
    
	//aurora add qiaohu 20141022 start
	private void clearContacts(){
		new Thread(){
    		public void run() {
    			ContentResolver cr=mContext.getContentResolver();
				cr.delete(Uri.parse("content://com.android.contacts/raw_contacts").buildUpon().appendQueryParameter("batch", "true")
				.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER,String.valueOf(true)).build(), "deleted=1 and sync4 is null",null);
    		}
    	}.start();
	}
	//aurora add qiaohu 20141022 end 
	
	 /**
     * fix for [PHB Status Refatoring] ALPS01003520
     * when boot complete,remove the contacts if the card of a slot had been removed
     */
    private void processBootComplete(Context context) {
    	 if (FeatureOption.MTK_GEMINI_SUPPORT) {
             startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
             startSimService(1, AbstractStartSIMService.SERVICE_WORK_REMOVE);
         } else {
             startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
         }
    }
}
