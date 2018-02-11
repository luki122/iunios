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

package com.android.phone;

import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;
import android.app.Activity;
import android.app.Application;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.app.StatusBarManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IPowerManager;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Settings.System;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.ServiceState;
import android.util.Config;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.internal.telephony.gemini.GeminiNetworkSubUtil;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.TelephonyIntents;
import com.android.phone.OtaUtils.CdmaOtaScreenState;

import android.os.Bundle;
import com.mediatek.common.featureoption.FeatureOption;
import com.android.internal.telephony.gemini.*;
import android.provider.Settings;

import android.telephony.TelephonyManager;
import com.mediatek.telephony.TelephonyManagerEx;




public class PowerOnSetupUnlockSIMLock extends Activity
{
	int SIM1Status = MtkPhoneGlobals.arySIMLockStatus[0];//to judge which card to unlock
	int SIM2Status = MtkPhoneGlobals.arySIMLockStatus[1];
	
    public int[] SIM1MELockStatus = {0,0,0,0,0};
    public int[] SIM2MELockStatus = {0,0,0,0,0};
    public int iSIMMELockStatus = 0;
    public  int iSIMMEUnlockNo = 0; //Test for SIM1
    public static int iNextUnlock = -1;//The next lock category needed to verify
//    public int result = -1;//Test for unlock result
//    public int ifinished = -1;//whether finished action
    
    public static final String LOGTAG = "PowerOnSetupUnlockSIMLock ";
    //Msg event for SIM Lock
    private static final int SIM1QUERY = 120;
    private static final int SIM2QUERY = 122; 
    private static PowerOnSetupUnlockSIMLock mInstance;
        
    /* Solve [ALPS00350200]The SIM ME lock screen flash once when SIME ME Lock locked permanently. */
    private boolean mIsLockedPermanentlySim1 = false;
    private boolean mIsLockedPermanentlySim2 = false;
        
    protected void onCreate(Bundle savedInstanceState) {
    	Log.d(LOGTAG, "[onCreate]+");
        super.onCreate(savedInstanceState);
        mInstance = this;

        /* Solve [ALPS00350200]The SIM ME lock screen flash once when SIME ME Lock locked permanently. */
        mIsLockedPermanentlySim1 = false;
        mIsLockedPermanentlySim2 = false;
		
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//Set this activity no title display
        Bundle bundle = this.getIntent().getExtras();//get the Special SIM Query Request
        Log.d(LOGTAG, "[onCreate][Special bundle]: "+ bundle);
        if(bundle != null){
            Log.d(LOGTAG, "[onCreate][Special bundle not null]: "+ bundle);
            iSIMMEUnlockNo = bundle.getInt("Phone.GEMINI_SIM_ID_KEY",0);//default to unlock SIM1
          //result = bundle.getInt("RESULT",-1);
          //ifinished = bundle.getInt("CONDITION", -1);
        	
        }
        if(((iSIMMEUnlockNo == 0) && (isSimLockDisplay(0,SIMLOCK_TYPE_SIMMELOCK))) ||
           ((iSIMMEUnlockNo == 1) && (isSimLockDisplay(1,SIMLOCK_TYPE_SIMMELOCK)))){
            /* Solve [ALPS00412017][Rose][Free Test][SIM Hot Swap]The SIM cann't regester 
               to network after you plug out and in the SIM card by HotSwapDevTool when the SIM pin and SIM ME lock 
               are both enabled.(1/5)
            */   
            int simState = TelephonyManagerEx.getDefault().getSimState(iSIMMEUnlockNo);
            if (simState != TelephonyManager.SIM_STATE_NETWORK_LOCKED) {
                finish();
                Log.d(LOGTAG, "[onCreate][already launched]");
            }
        }
//        Log.d(LOGTAG, "[result]: "  + result);
        Log.d(LOGTAG, "[onCreate]-");
    }//onCreat End

    



    @Override
	protected void onStart() {
		// TODO Auto-generated method stub
    	Log.d(LOGTAG, "[onStart]");
		super.onStart();
	}



@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(LOGTAG, "[onDestroy]");
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
    	Log.d(LOGTAG, "[onPause]");
		super.onPause();
	}
	
	

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		Log.d(LOGTAG, "[onNewIntent]");
		super.onNewIntent(intent);
	}






	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.d(LOGTAG, "[onResume]+");
	        if(false == FeatureOption.MTK_GEMINI_SUPPORT)
	        {
	        	Log.d(LOGTAG, "[onResume][Single Card]");
	        	Message msgQuerySIM1LockStatusNP = Message.obtain(mHandlerSIMLock, SIM1QUERY);
	        	Message msgQuerySIM1LockStatusNSP = Message.obtain(mHandlerSIMLock, SIM1QUERY);
	        	Message msgQuerySIM1LockStatusSP = Message.obtain(mHandlerSIMLock, SIM1QUERY);
	        	Message msgQuerySIM1LockStatusCP = Message.obtain(mHandlerSIMLock, SIM1QUERY);
	        	Message msgQuerySIM1LockStatusSIMP = Message.obtain(mHandlerSIMLock, SIM1QUERY);
	    		Phone phone = PhoneFactory.getDefaultPhone();
	    		phone.getIccCard().QueryIccNetworkLock(0,4,null,null,null,null,msgQuerySIM1LockStatusNP);
	      		phone.getIccCard().QueryIccNetworkLock(1,4,null,null,null,null,msgQuerySIM1LockStatusNSP);
	      		phone.getIccCard().QueryIccNetworkLock(2,4,null,null,null,null,msgQuerySIM1LockStatusSP);
	      		phone.getIccCard().QueryIccNetworkLock(3,4,null,null,null,null,msgQuerySIM1LockStatusCP);
	      		phone.getIccCard().QueryIccNetworkLock(4,4,null,null,null,null,msgQuerySIM1LockStatusSIMP);
	       	
	        }else{  
	        	    Log.d(LOGTAG, "[onResume][Gemini Card]");
	    			//Step1 :Query SIM1/SIM2 SIM states
	    	    	GeminiPhone mGeminiPhone = (GeminiPhone)PhoneFactory.getDefaultPhone();
	    	    	if (iSIMMEUnlockNo == 0){//deal with SIM1
	        	    	Message msgQuerySIM1LockStatusNP = Message.obtain(mHandlerSIMLock, SIM1QUERY);
	        	    	Message msgQuerySIM1LockStatusNSP = Message.obtain(mHandlerSIMLock, SIM1QUERY);
	        	    	Message msgQuerySIM1LockStatusSP = Message.obtain(mHandlerSIMLock, SIM1QUERY);
	        	    	Message msgQuerySIM1LockStatusCP = Message.obtain(mHandlerSIMLock, SIM1QUERY);
	        	    	Message msgQuerySIM1LockStatusSIMP = Message.obtain(mHandlerSIMLock, SIM1QUERY);    	    		           
		    	    	mGeminiPhone.getIccCardGemini(Phone.GEMINI_SIM_1).QueryIccNetworkLock(0,0,null,null,null,null,msgQuerySIM1LockStatusNP);
		    	    	mGeminiPhone.getIccCardGemini(Phone.GEMINI_SIM_1).QueryIccNetworkLock(1,0,null,null,null,null,msgQuerySIM1LockStatusNSP);
		    	    	mGeminiPhone.getIccCardGemini(Phone.GEMINI_SIM_1).QueryIccNetworkLock(2,0,null,null,null,null,msgQuerySIM1LockStatusSP);
		    	    	mGeminiPhone.getIccCardGemini(Phone.GEMINI_SIM_1).QueryIccNetworkLock(3,0,null,null,null,null,msgQuerySIM1LockStatusCP);
		    	    	mGeminiPhone.getIccCardGemini(Phone.GEMINI_SIM_1).QueryIccNetworkLock(4,0,null,null,null,null,msgQuerySIM1LockStatusSIMP);
	    	    	}else if (iSIMMEUnlockNo == 1){
	        	    	Message msgQuerySIM2LockStatusNP = Message.obtain(mHandlerSIMLock, SIM2QUERY);
	        	    	Message msgQuerySIM2LockStatusNSP = Message.obtain(mHandlerSIMLock, SIM2QUERY);
	        	    	Message msgQuerySIM2LockStatusSP = Message.obtain(mHandlerSIMLock, SIM2QUERY);
	        	    	Message msgQuerySIM2LockStatusCP = Message.obtain(mHandlerSIMLock, SIM2QUERY);
	        	    	Message msgQuerySIM2LockStatusSIMP = Message.obtain(mHandlerSIMLock, SIM2QUERY);
	        	    	mGeminiPhone.getIccCardGemini(Phone.GEMINI_SIM_2).QueryIccNetworkLock(0,0,null,null,null,null,msgQuerySIM2LockStatusNP);
	        	    	mGeminiPhone.getIccCardGemini(Phone.GEMINI_SIM_2).QueryIccNetworkLock(1,0,null,null,null,null,msgQuerySIM2LockStatusNSP);
	        	    	mGeminiPhone.getIccCardGemini(Phone.GEMINI_SIM_2).QueryIccNetworkLock(2,0,null,null,null,null,msgQuerySIM2LockStatusSP);
	        	    	mGeminiPhone.getIccCardGemini(Phone.GEMINI_SIM_2).QueryIccNetworkLock(3,0,null,null,null,null,msgQuerySIM2LockStatusCP);
	        	    	mGeminiPhone.getIccCardGemini(Phone.GEMINI_SIM_2).QueryIccNetworkLock(4,0,null,null,null,null,msgQuerySIM2LockStatusSIMP); 
	    	    	}           
	        }			
         
    	super.onResume();
    	Log.d(LOGTAG, "[onResume]-");
		}
	

   Handler mHandlerSIMLock = new Handler(){
    	public void handleMessage(Message msg){
    		AsyncResult ar = (AsyncResult)msg.obj;
    		int [] LockState = (int [])ar.result;
    	switch(msg.what){
	    	case SIM1QUERY:
	    	    if (ar.exception != null)//Query exception occurs
         	    {
	    	    	
         	    }else{
         	    	Log.d (LOGTAG, "[mHandlerSIMLock][SIM1QUERY][SIM1 Status][0][1]+: " + SIM1MELockStatus[0] + " , " + SIM1MELockStatus[1]);
         	    	SIM1MELockStatus[LockState[0]] = LockState[1]==1?1:0;
         	    	Log.d (LOGTAG, "[mHandlerSIMLock][SIM1QUERY][SIM1 Status][0][1]-: " + SIM1MELockStatus[0] + " , " + SIM1MELockStatus[1]);

         	    	/* 
         	    	  Solve [ALPS00350200]The SIM ME lock screen flash once when SIME ME Lock locked permanently.
         	    	  If retry count is equal 0 for any type of ME locks, set the flag to true to avoid launching 
         	    	  PowerOnUnlockSIMLock activity.
         	    	*/
         	    	if (LockState[2] == 0) {
         	    	    mIsLockedPermanentlySim1 = true;
         	    	    Log.d (LOGTAG, "mIsLockedPermanentlySim1 = " + mIsLockedPermanentlySim1);
         	    	}
					
                	if(LockState[0] != 4){//have not query all categories
                		
                	}else{//already query all categories
             	    	iSIMMELockStatus = 0;
             	    	for(int i = 0;i<5;i++){
             	    		SIM2MELockStatus[i] = 0;
             	    	}
                    	iSIMMELockStatus = resetISIMMELockStatus(SIM1MELockStatus,SIM2MELockStatus, iSIMMEUnlockNo);
                    	Log.d(LOGTAG, "[EVENT_SIM1_NETWORK_LOCKED][packageArray][iSIMMELockStatus] : " + iSIMMELockStatus);
                    	Log.d(LOGTAG, "mIsLockedPermanentlySim1 = " + mIsLockedPermanentlySim1);
                    	/* Solve [ALPS00350200]The SIM ME lock screen flash once when SIME ME Lock locked permanently. */
                    	if ((iSIMMELockStatus == 0) || mIsLockedPermanentlySim1) {//no need to unlock SIM1,just quit
                    		//sendVerifyResult(VERIFY_TYPE_SIMMELOCK,true);
                    		MtkPhoneGlobals.arySIMLockStatus[0] = 0;//SIM 1 already dealt
                    		Log.d(LOGTAG,"[changed][arySIMLockStatus]: ["+ MtkPhoneGlobals.arySIMLockStatus[0] + " , " + MtkPhoneGlobals.arySIMLockStatus[1] + " ]");
                    		
                    		if(MtkPhoneGlobals.arySIMLockStatus[1] == 2){//Need to unlock SIM2
                            	Intent intent = new Intent(PowerOnSetupUnlockSIMLock.this, PowerOnSetupUnlockSIMLock.class);  
                        		Bundle bundle = new Bundle();                                
                                bundle.putInt("Phone.GEMINI_SIM_ID_KEY",1);//To unlock which card  default:-1, Slot1: 0, Slot2:1
                        		intent.putExtras(bundle);
                        		startActivity(intent); 
                    		}
                    		setSimLockScreenDone(0,SIMLOCK_TYPE_SIMMELOCK);
                    		PowerOnSetupUnlockSIMLock.this.finish();
                    		return;
                    	}else{
                    		MtkPhoneGlobals.arySIMLockStatus[0] = 4;//SIM1 is now dealing with
                        	Intent intent = new Intent(PowerOnSetupUnlockSIMLock.this, PowerOnUnlockSIMLock.class);  
                    		Bundle bundle = new Bundle();
                            bundle.putInt("SIMMELOCKSTATUS", iSIMMELockStatus);
                            bundle.putInt("Phone.GEMINI_SIM_ID_KEY",iSIMMEUnlockNo);//To unlock which card  default:-1, Slot1: 0, Slot2:1
                            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    		intent.putExtras(bundle);
                    		startActivity(intent);
                        	Log.d(LOGTAG, "[mHandlerSIMLock][startActivity]");                        	
                    		PowerOnSetupUnlockSIMLock.this.finish();
                    		return;
                    	}
                		
                	}
         	    }
	    		break;
	    	case SIM2QUERY: 
	    	    if (ar.exception != null)//Query exception occurs
         	    {
	    	    	
         	    }else{
         	    	Log.d (LOGTAG, "[mHandlerSIMLock][SIM2QUERY][SIM2 Status Ori][0][1]+: " + SIM2MELockStatus[0] + " , " + SIM2MELockStatus[1]);
         	    	SIM2MELockStatus[LockState[0]] = LockState[1]==1?1:0;
         	    	Log.d (LOGTAG, "[mHandlerSIMLock][SIM2QUERY][SIM2 Status][0][1]-: " + SIM2MELockStatus[0] + " , " + SIM2MELockStatus[1]);

         	    	/* 
         	    	  Solve [ALPS00350200]The SIM ME lock screen flash once when SIME ME Lock locked permanently.
         	    	  If retry count is equal 0 for any type of ME locks, set the flag to true to avoid launching 
         	    	  PowerOnUnlockSIMLock activity.
         	    	*/
         	    	if (LockState[2] == 0) {
         	    	    mIsLockedPermanentlySim2 = true;
         	    	    Log.d (LOGTAG, "mIsLockedPermanentlySim2 = " + mIsLockedPermanentlySim2);
         	    	}
					
                	if(LockState[0] != 4){//have not query all categories
                		
                	}else{//already query all categories
             	    	iSIMMELockStatus = 0;
             	    	for(int i = 0;i<5;i++){
             	    		SIM1MELockStatus[i] = 0;
             	    	}
                    	iSIMMELockStatus = resetISIMMELockStatus(SIM1MELockStatus,SIM2MELockStatus, iSIMMEUnlockNo);
                    	Log.d(LOGTAG, "[EVENT_SIM2_NETWORK_LOCKED][packageArray][iSIMMELockStatus] : " + iSIMMELockStatus);
         	    	/* Solve [ALPS00350200]The SIM ME lock screen flash once when SIME ME Lock locked permanently. */
                    	if ((iSIMMELockStatus == 0) || mIsLockedPermanentlySim2) {//no need to unlock SIM2,just quit
                    		//sendVerifyResult(VERIFY_TYPE_SIMMELOCK,true);
                    		MtkPhoneGlobals.arySIMLockStatus[1] = 0;//SIM 2 already dealt
                    		Log.d(LOGTAG,"[changed][arySIMLockStatus]: ["+ MtkPhoneGlobals.arySIMLockStatus[0] + " , " + MtkPhoneGlobals.arySIMLockStatus[1] + " ]");
                    		
                    		if(MtkPhoneGlobals.arySIMLockStatus[0] == 2){//Need to unlock SIM2
                            	Intent intent = new Intent(PowerOnSetupUnlockSIMLock.this, PowerOnSetupUnlockSIMLock.class);  
                        		Bundle bundle = new Bundle();                                
                                bundle.putInt("Phone.GEMINI_SIM_ID_KEY",0);//To unlock which card  default:-1, Slot1: 0, Slot2:1
                        		intent.putExtras(bundle);
                        		startActivity(intent); 
                    		}
                    		setSimLockScreenDone(1,SIMLOCK_TYPE_SIMMELOCK);
                    		PowerOnSetupUnlockSIMLock.this.finish();
                    		return;
                    	}else{
                    		MtkPhoneGlobals.arySIMLockStatus[1] = 4;//SIM1 is now dealing with
                        	Intent intent = new Intent(PowerOnSetupUnlockSIMLock.this, PowerOnUnlockSIMLock.class);  
                    		Bundle bundle = new Bundle();
                            bundle.putInt("SIMMELOCKSTATUS", iSIMMELockStatus);
                            bundle.putInt("Phone.GEMINI_SIM_ID_KEY",iSIMMEUnlockNo);//To unlock which card  default:-1, Slot1: 0, Slot2:1
                            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    		intent.putExtras(bundle);
                    		startActivity(intent);                     		
                    		PowerOnSetupUnlockSIMLock.this.finish();
                    		return;
                    	}             		
                	}
         	    }	    		
	    		break;
    	}

    	}
    };
 
    public static int resetISIMMELockStatus(int[] SIM1MELockStatus, int[] SIM2MELockStatus, int CurrentSIMNO){
    	int tempISIMMELockStatus = 0;
    	if(false == FeatureOption.MTK_GEMINI_SUPPORT)
        {
    		Phone phone = PhoneFactory.getDefaultPhone();
    		iNextUnlock = phone.getIccCard().getNetworkPersoType();
    		if (iNextUnlock == -1){//No need very any log category
    			tempISIMMELockStatus = 0;
    		}else{
    			for(int i = 0 ;i < iNextUnlock ; i++){
    				SIM1MELockStatus[i] = 0;
    				}
    			for(int i = 0;i < 5; i++){
    				SIM2MELockStatus[i] = 0;
    			}
    			tempISIMMELockStatus = packageArray(SIM1MELockStatus,SIM2MELockStatus);
    		}
    		return  tempISIMMELockStatus;
    		
        }else{
        	GeminiPhone mGeminiPhone = (GeminiPhone)PhoneFactory.getDefaultPhone();
        	if (CurrentSIMNO == 0){//deal with SIM1
        		iNextUnlock = mGeminiPhone.getIccCardGemini(Phone.GEMINI_SIM_1).getNetworkPersoType();
        		if (iNextUnlock == -1){//No need very any log category
        			tempISIMMELockStatus = 0;
        		}else{
        			for(int i = 0 ;i < iNextUnlock ; i++){
        				SIM1MELockStatus[i] = 0;
        				}
        			for(int i = 0;i < 5; i++){
        				SIM2MELockStatus[i] = 0;
        			}
        			tempISIMMELockStatus = packageArray(SIM1MELockStatus,SIM2MELockStatus);
        		}
        		return  tempISIMMELockStatus;
        	}else{
        		iNextUnlock = mGeminiPhone.getIccCardGemini(Phone.GEMINI_SIM_2).getNetworkPersoType();
        		if (iNextUnlock == -1){//No need very any log category
        			tempISIMMELockStatus = 0;
        		}else{
        			for(int i = 0 ;i < iNextUnlock ; i++){
        				SIM2MELockStatus[i] = 0;
        				}
        			for(int i = 0;i < 5; i++){
        				SIM1MELockStatus[i] = 0;
        			}
        			tempISIMMELockStatus = packageArray(SIM1MELockStatus,SIM2MELockStatus);
        		}
        		return  tempISIMMELockStatus;
        	}
        	
        }
    }

    //package two src Arrays tvalue to a int value
    public static int packageArray(int[] ArySrc1, int[] ArySrc2 ){
    	int iAry1Length = ArySrc1.length;
    	int iAry2Length = ArySrc2.length;
    	int iArySumLength = iAry1Length + iAry2Length;
    	int intTempSrc = 0;
    	for (int i = 0;i < iAry1Length;i++){
    		intTempSrc += ArySrc1[i]*Math.pow(2, iArySumLength-1-i);
    	}
    	for (int i = 0;i < iAry2Length;i++){
    		intTempSrc += ArySrc2[i]*Math.pow(2, iAry2Length-1-i);
    	}
    	return intTempSrc;
    }
    


	static final int VERIFY_TYPE_PIN = 501;
	static final int VERIFY_TYPE_PUK = 502;
	static final int VERIFY_TYPE_SIMMELOCK = 503;
	static final int VERIFY_TYPE_PIN2 = 504;
	static final int VERIFY_TYPE_PUK2 = 505;

	static final String VERIFY_TYPE = "verfiy_type";
	static final String VERIFY_RESULT = "verfiy_result";
	public static final String START_TYPE = "start_type";
	public static final String START_TYPE_REQ = "request";
	public static final String START_TYPE_RSP = "response";

	public void sendVerifyResult(int verifyType, boolean bRet) {
		Log.d(LOGTAG, "sendVerifyResult verifyType = " + verifyType
				+ " bRet = " + bRet);
		Intent retIntent = new Intent(
				"android.intent.action.CELLCONNSERVICE").putExtra(
				START_TYPE, START_TYPE_RSP);

		if (null == retIntent) {
			Log.e(LOGTAG, "sendVerifyResult new retIntent failed");
			return;
		}
		retIntent.putExtra(VERIFY_TYPE, verifyType);

		retIntent.putExtra(VERIFY_RESULT, bRet);

		startService(retIntent);
	}    

    private final BroadcastReceiver mReceiver = new  BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                finish();
            }
        }
    };
    
    private static int SIMLOCK_TYPE_PIN = 1;
    private static final int SIMLOCK_TYPE_SIMMELOCK = 2;
    
    private boolean isSimLockDisplay(int slot, int type) {
    	if (slot < 0) {
    		return false;
    	}
    	
    	Long simLockState = Settings.System.getLong(this.getContentResolver(), Settings.System.SIM_LOCK_STATE_SETTING, 0);
    	Long bitSet = simLockState;
    	
    	bitSet = bitSet>>>2*slot;
    	if (SIMLOCK_TYPE_PIN == type) {
    		if (0x1L == (bitSet & 0x1L)) {
    			return true;
    		} else {
    			return false;
    		}
    	} else if (SIMLOCK_TYPE_SIMMELOCK == type) {
    		bitSet = bitSet>>>1;
    		if (0x1L == (bitSet & 0x1L)) {
    			return true;
    		} else {
    			return false;
    		}
    	}
    	
    	return true;
    }
    
    private void setSimLockScreenDone(int slot, int type) {
    	if (slot < 0) {
    		return ;
    	}
    	
    	if (isSimLockDisplay(slot, type)) {
    		Log.d(LOGTAG, "setSimLockScreenDone the SimLock display is done");
    		return;
    	}
    	
    	Long simLockState = Settings.System.getLong(this.getContentResolver(), Settings.System.SIM_LOCK_STATE_SETTING, 0);
    	
    	Long bitSet = 0x1L;
    	
    	bitSet = bitSet<<2*slot;
    	Log.d(LOGTAG, "setSimLockScreenDone1 bitset = " + bitSet);
		if (SIMLOCK_TYPE_SIMMELOCK == type) {
			bitSet = bitSet << 1;
		}
		Log.d(LOGTAG, "setSimLockScreenDone2 bitset = " + bitSet);
		
		simLockState += bitSet;
		Settings.System.putLong(this.getContentResolver(), Settings.System.SIM_LOCK_STATE_SETTING, simLockState);
    }
    public static PowerOnSetupUnlockSIMLock getInstance()
	{
		return mInstance;
	}

}//Main Class End
