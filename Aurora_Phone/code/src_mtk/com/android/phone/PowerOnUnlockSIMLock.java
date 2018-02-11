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
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.PowerManager;

import com.mediatek.common.featureoption.FeatureOption;
import android.telephony.TelephonyManager;//To find the SIM card Ready State
import android.text.Editable;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncResult;
//import android.provider.ContactsContract.CommonDataKinds.Phone;
//import com.android.internal.R;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneBase;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.internal.telephony.IccCard;
//import com.android.internal.widget.ITelephony;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.TelephonyIntents;

import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.provider.Settings;
import android.provider.Telephony.SIMInfo;

import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.method.DigitsKeyListener;
import android.view.ViewRootImpl;

import android.provider.Settings; // ALPS00299096
import android.app.StatusBarManager; // ALPS00305296

// ECC button should be hidden when there is no service.
import static android.provider.Telephony.Intents.SPN_STRINGS_UPDATED_ACTION;
import static android.provider.Telephony.Intents.EXTRA_PLMN;
import static android.provider.Telephony.Intents.EXTRA_SHOW_PLMN;

public class PowerOnUnlockSIMLock extends Activity
{
    public static final String LOGTAG = "PowerOnUnlockSIMLock ";
    // intent action for launching emergency dialer activity.
    static final String ACTION_EMERGENCY_DIAL = "com.android.phone.EmergencyDialer.DIAL";
    
    public static final int UNLOCK_ICC_SML_COMPLETE = 120;
    public static final int UNLOCK_ICC_SML_QUERYLEFTTIMES = 110;
    public int[] SIM1MELockStatus = {0,0,0,0,0};
    public int[] SIM2MELockStatus = {0,0,0,0,0};
    public int iSIMMELockStatus = 0;
    public static  String LOCKCATEGORY = "LockCategory";
    private static int lockCategory = -1;
    private static int intSIMNumber = -1;
    private static final int NPLOCKTYPE = 0;
    private static final int NSPLOCKTYPE = 1;
    private static final int SPLOCKTYPE = 2;
    private static final int CPLOCKTYPE = 3;
    private static final int SIMPLOCKTYPE = 4;

    // [ALPS00395362][02772]
    // Used to set left and right padding of sim card name
    private int mSIMCardNamePadding;

    private static PowerOnUnlockSIMLock mInstance; 
	private TextView mDismissButton;
	private TextView mUnlockResultNotify;
	private TextView mUnlockActionNotify;
	private TextView mUnlockRetriesNotify;
	private TextView mUnlockForSIMNotify = null;
	private TextView mUnlockSIMNameNotify = null;
	private ImageButton mbackspace;
	private Button mbtnEmergencyCall;
	public  TextView mPwdDisplay = null;
	private Button mBtnMoreInfo = null;
	private TextView mUnlockEmptyForSingleCard = null;
	public int PwdLength = -1;
	public String strPwd = null; 
	public String tempstrPwd = null;
	public int mPwdLeftChances = -1;//unlock retries left times
	public int iSIMMEUnlockNo = -1;//To unlock which card  default:-1, Slot1: 0, Slot2:1
	public ProgressDialog progressDialog;
	public int iSIM1Unlocked  = 0;//Flag to record whether SIM1 is unlocked successful
	public int iSIM2Unlocked = 0;//Flag to record whether SIM2 is unlocked successful
	private int DLGMOREINFOIMAGE = 0;//Flag to define show which SIM MoreInfo image
	private static int SIM1LockedPermanently = 0;
	private static int SIM2LockedPermanently = 0;
	
	public String[] strLockName = {" [NP]", " [NSP]"," [SP]"," [CP]"," [SIMP]"};// Lock category name string Temp use for QA/RD
	
    private static final int DIALOG_ENTERNUMBER = 120;

    private StatusBarManager mStatusBarManager; // ALPS00305296
    private PowerManager pm = null;  

        // ECC button should be hidden when there is no service.
        private MtkPhoneGlobals mApp;
        private boolean mIsNoService[] = {true, true};
        private boolean mECCBtnRunnableStarted = false;

    protected void onCreate(Bundle savedInstanceState) {
    	Log.d(LOGTAG, "[onCreate]+");
        super.onCreate(savedInstanceState);
        mInstance = this;
        pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        if(((MtkPhoneGlobals.arySIMLockStatus[0] == 0) && (MtkPhoneGlobals.arySIMLockStatus[1] == 0))
			||(SIM1LockedPermanently ==1&&hasIccCard(1)==false)||(SIM2LockedPermanently ==1&&hasIccCard(0)==false)
			||(SIM1LockedPermanently ==1&&SIM2LockedPermanently == 1)){
        	//Two Sim cards are dealt already
        	Log.d(LOGTAG, "[onCreate][Two cards are handled aleady]");
			   PowerOnUnlockSIMLock.this.finish();		            		  
    		   return ;
        }
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//Set this activity no title display
		if (FeatureOption.MTK_GEMINI_SUPPORT){
		   setContentView(R.layout.unlocksimlock_gemini);
		}else {
        setContentView(R.layout.unlocksimlock);
		}
		
        mApp = (MtkPhoneGlobals)MtkPhoneGlobals.getInstance();

		//settings for catch the home key
		Window win = getWindow();
		WindowManager.LayoutParams lp = win.getAttributes();
		lp.flags |= WindowManager.LayoutParams.FLAG_HOMEKEY_DISPATCHED;  //0x80000000 should be replaced by WindowManager.LayoutParams.FLAG_HOMEKEY_DISPATCHED
		win.setAttributes(lp);	

        // ALPS00305296
        mStatusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
		
        findViewById(R.id.pwdDisplay).setBackgroundDrawable(getResources().getDrawable(com.mediatek.internal.R.drawable.edit_text_configure));
        mPwdDisplay = (TextView) findViewById(R.id.pwdDisplay);
        mPwdDisplay.setTextColor(0xff000000);

        mPwdDisplay.requestFocus();
        //mPwdDisplay.setInputType(InputType.TYPE_NULL);
        //mPwdDisplay.setTransformationMethod(PasswordTransformationMethod.getInstance());
        mPwdDisplay.setInputType(InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_PASSWORD );
        mPwdDisplay.setKeyListener(DigitsKeyListener.getInstance());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        mSIMCardNamePadding = this.getResources().
            getDimensionPixelSize(com.mediatek.internal.R.dimen.sim_card_name_padding);
        
        mbackspace = (ImageButton) findViewById(R.id.backspace);
        mbtnEmergencyCall = (Button) findViewById(R.id.emergencyCall);
        mbtnEmergencyCall.setText(this.getResources().getString(R.string.simlock_emergency));
        // ECC button should be hidden when there is no service. 
        Log.d(LOGTAG, "mApp.isCurrentlyNoService() = " + mApp.isCurrentlyNoService());
        if (mApp.isCurrentlyNoService()) {
            mbtnEmergencyCall.setVisibility(View.INVISIBLE);
        }
		decideLockCategory();
        Log.d(LOGTAG, "[onCreate][lockCategory]" + lockCategory + " intSIMNumber: " +intSIMNumber);
        if (findViewById(R.id.keyPad) != null)
		{
        	new TouchInput();
		}
        mDismissButton = (TextView) findViewById(R.id.dismiss);//set the Cancel Button text to Dismiss
        //mDismissButton.setText("Dismiss");  
        mUnlockResultNotify = (TextView) findViewById(R.id.unlockResultNotify);        
        //mUnlockResultNotify.setText("Wrong code.");  
        mUnlockActionNotify = (TextView) findViewById(R.id.unlockActionNotify);
        mUnlockActionNotify.setText(this.getResources().getString(R.string.simlock_entersimmelock)); 
        mUnlockRetriesNotify = (TextView) findViewById(R.id.unlockRetriesNotify);
        //mUnlockRetriesNotify.setText("5 retries");
        
        if(true == FeatureOption.MTK_GEMINI_SUPPORT)
        {
        	mUnlockForSIMNotify = (TextView)findViewById(R.id.unlockForSIMNotify);			
			mUnlockSIMNameNotify = (TextView) findViewById(R.id.unlockSIMNameNotify);
		   mBtnMoreInfo = (Button) findViewById(R.id.btnMoreInfo);
        }
		else
		{
           mUnlockEmptyForSingleCard = (TextView)findViewById(R.id.unlockSIMEmptyForSingleCard);
		}
        
        if(false == FeatureOption.MTK_GEMINI_SUPPORT){
        	mUnlockEmptyForSingleCard.setVisibility(View.INVISIBLE);
        }else{
            
        }        

        mbackspace.setOnClickListener(new ImageButton.OnClickListener(){
        	public void onClick(View view){
            	if (mPwdDisplay != null){
                    final Editable digits = mPwdDisplay.getEditableText();
                    final int len = digits.length();
                    if (len > 0) {
                       digits.delete(len-1, len);
                    }
        		    strPwd = mPwdDisplay.getText().toString();
            		Log.d(LOGTAG, "[mbackspace][onClickListener][strPwd)]" + strPwd);
            	}           	
        	}
        });
        
        mbackspace.setOnLongClickListener(new ImageButton.OnLongClickListener(){
			public boolean onLongClick(View view) {
				// TODO Auto-generated method stub
				if (mPwdDisplay != null){
            		Log.d(LOGTAG, "[mbackspace][onLongClickListener][strPwd)]" + strPwd);
            		strPwd = null;
					mPwdDisplay.setText("");//delete all digits to display on the Pwd edittext
				}
				return false;
			}
        });
        if(true == FeatureOption.MTK_GEMINI_SUPPORT){
        
        mBtnMoreInfo.setOnClickListener(new Button.OnClickListener(){
        	public void onClick(View view){
        	    
                showDialog(DLGMOREINFOIMAGE);
        		Log.d(LOGTAG, "[mBtnMoreInfo][onClickListener][DLGMOREINFOIMAGE)]" + DLGMOREINFOIMAGE);
        	           	
    	    }
        });
        }
        
        mbtnEmergencyCall.setOnClickListener(new Button.OnClickListener(){
        	public void onClick(View view){
        		int state = TelephonyManager.getDefault().getCallState();
                if ((state == TelephonyManager.CALL_STATE_OFFHOOK)
        			|| (state == TelephonyManager.CALL_STATE_RINGING)){
                    //Need to add: if already exist a call 
                    ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
                    try {
                        if (phone != null){
                        	phone.showCallScreen();
                        }
                    } catch (RemoteException e) {
                        // What can we do?
                    }
                } 
                else {
                    Intent intent = new Intent(ACTION_EMERGENCY_DIAL);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                   startActivity(intent);
                }
            }          	
        });
                

        
        Log.d(LOGTAG, "[PowerOnUnlockSIMLock][onCreate]-");
    }//onCreat End

    

		  


    @Override
    protected void onStart() {
		// TODO Auto-generated method stub
    	Log.d(LOGTAG, "[onStart]");
        /* Solve [ALPS00396153] to prevent two SIM ME unlock screen are showed. */
    	if (TelephonyManager.getDefault().getSimStateGemini(iSIMMEUnlockNo) != TelephonyManager.SIM_STATE_NETWORK_LOCKED) {
            Log.d(LOGTAG, "[onStart]sim state = " + TelephonyManager.getDefault().getSimStateGemini(iSIMMEUnlockNo));
            sendVerifyResult(VERIFY_TYPE_SIMMELOCK,false);
            finish();
    	}
		
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
        Log.d(LOGTAG, "[onPause] enable statusbar");
        mStatusBarManager.disable(StatusBarManager.DISABLE_NONE);
        if(pm.isScreenOn() == false) {
            Log.d(LOGTAG, "screen is off");

            strPwd = null;
            if(mPwdDisplay != null) 
                mPwdDisplay.setText("");
        }
    	unregisterReceiver(mReceiver);

	//Inform SIM Framework that "New SIM Detect" screen can be showd.
	int oppIndex = (iSIMMEUnlockNo == 0) ? 1 : 0; 
        if (MtkPhoneGlobals.arySIMLockStatus[oppIndex] != 2) {
           Log.d(LOGTAG,"[onPause]New SIM Detect can be showed");
	   notifyToShowNewSimDetected();
        }
		
	super.onPause();
}
	
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		Log.d(LOGTAG, "[onNewIntent]");
		super.onNewIntent(intent);
		decideLockCategory();
        Log.d(LOGTAG, "[onNewIntent][lockCategory]" + lockCategory + " intSIMNumber: " +intSIMNumber);
	}


    private Handler mHandler= new Handler()
    {
    	public void handleMessage(Message msg) {
          AsyncResult ar = (AsyncResult)msg.obj;
            switch (msg.what) {
              case UNLOCK_ICC_SML_COMPLETE:
               {
            	   Log.d(LOGTAG, "[mHandler][UNLOCK_ICC_SML_COMPLETE]+" );
            	   if (ar.exception != null){//fail to unlock            	  
            		   if(false == FeatureOption.MTK_GEMINI_SUPPORT){
	                       //Single Card:
	                       Message callback_query = Message.obtain(mHandler,  UNLOCK_ICC_SML_QUERYLEFTTIMES);
	                       Phone phone = PhoneFactory.getDefaultPhone();
	                       phone.getIccCard().QueryIccNetworkLock(lockCategory,0,null,null,null,null,callback_query);
	                   }else{
	                       Log.d(LOGTAG, "[mHandler][UNLOCK_ICC_SML_COMPLETE][GEMINI Card]+" );
	                       GeminiPhone mGeminiPhone = (GeminiPhone)PhoneFactory.getDefaultPhone();
	                       if (iSIMMEUnlockNo == 0){
		                       Message callback_query = Message.obtain(mHandler,  UNLOCK_ICC_SML_QUERYLEFTTIMES);
		                       mGeminiPhone.getIccCardGemini(Phone.GEMINI_SIM_1).QueryIccNetworkLock(lockCategory,0,null,null,null,null,callback_query);
		            	   }else if(iSIMMEUnlockNo == 1){
	                       		Message callback_query = Message.obtain(mHandler,  UNLOCK_ICC_SML_QUERYLEFTTIMES);
	                       		mGeminiPhone.getIccCardGemini(Phone.GEMINI_SIM_2).QueryIccNetworkLock(lockCategory,0,null,null,null,null,callback_query);
		            	   }	               	   
                       }           		   
            		   mUnlockResultNotify.setText(getString(R.string.simlock_wrongcode));
            		   progressDialog.dismiss();
            	   }else{//unlock current lock category succeed,need to search whether the user have to go to another lock 
	            	   Log.d(LOGTAG, "[mHandler][GEMINI]+" );
	            	   Log.d(LOGTAG, "[mHandler][iSIMMEUnlockNo] : " + iSIMMEUnlockNo );
	            	   Log.d(LOGTAG, "[mHandler][lockCategory] : " + lockCategory );
	            	   iSIMMELockStatus = PowerOnSetupUnlockSIMLock.resetISIMMELockStatus(SIM1MELockStatus,SIM2MELockStatus,iSIMMEUnlockNo);
	            	   Log.d(LOGTAG, "[mHandler][UNLOCK_ICC_SML_COMPLETE][iSIMMELockStatus][new] : " +iSIMMELockStatus );
	            	   if (iSIMMELockStatus != 0){
	            		   //still need to unlock current SIM card's other category lock
		                   	Intent intent2 = new Intent(PowerOnUnlockSIMLock.this, PowerOnUnlockSIMLock.class);  
		            		Bundle bundle2 = new Bundle();
		            		bundle2.putInt("Phone.GEMINI_SIM_ID_KEY", iSIMMEUnlockNo);
		                    bundle2.putInt("SIMMELOCKSTATUS", iSIMMELockStatus);
	                        //intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		            		intent2.putExtras(bundle2);
		            		startActivity(intent2);
		            		PowerOnUnlockSIMLock.this.finish();
		            		progressDialog.dismiss();
		            		return ;
	            	   }else{
	            		   //need to deal with the other SIM card
	            		   if(iSIMMEUnlockNo == 0)
		            	   {
	            			   MtkPhoneGlobals.arySIMLockStatus[0] = 0;//SIM 1 already dealt
	            			   setSimLockScreenDone(0,SIMLOCK_TYPE_SIMMELOCK);
		            		   progressDialog.dismiss();
	            			   //To deal SIM2
		            		   if(MtkPhoneGlobals.arySIMLockStatus[1] == 2){//Need to unlock SIM2
	                            	Intent intent = new Intent(PowerOnUnlockSIMLock.this, PowerOnSetupUnlockSIMLock.class);  
	                        		Bundle bundle = new Bundle();                                
	                                bundle.putInt("Phone.GEMINI_SIM_ID_KEY",1);//To unlock which card  default:-1, Slot1: 0, Slot2:1
	                        		intent.putExtras(bundle);
	                        		startActivity(intent); 
	                    	           } else {
				               //Inform SIM Framework that "New SIM Detect" screen can be showd.
				               Log.d(LOGTAG,"[UNLOCK_ICC_SML_COMPLETE1]New SIM Detect can be showed");
				               notifyToShowNewSimDetected();
	                    		}
							   
		            		   PowerOnUnlockSIMLock.this.finish();
		            		   Log.d(LOGTAG, "[mHandler][UNLOCK_ICC_SML_COMPLETE][SIM1][Finished]" );
		            		   return ;
		            	   }else{
	            			   MtkPhoneGlobals.arySIMLockStatus[1] = 0;//SIM 2 already dealt
	            			   setSimLockScreenDone(1,SIMLOCK_TYPE_SIMMELOCK);
		            		   progressDialog.dismiss();
	            			   //To deal SIM1
		            		   if(MtkPhoneGlobals.arySIMLockStatus[0] == 2){//Need to unlock SIM1
	                            	Intent intent = new Intent(PowerOnUnlockSIMLock.this, PowerOnSetupUnlockSIMLock.class);  
	                        		Bundle bundle = new Bundle();                                
	                                bundle.putInt("Phone.GEMINI_SIM_ID_KEY",0);//To unlock which card  default:-1, Slot1: 0, Slot2:1
	                        		intent.putExtras(bundle);
	                        		startActivity(intent); 
	                    	           } else {
					       //Inform SIM Framework that "New SIM Detect" screen can be showd.
					       Log.d(LOGTAG,"[UNLOCK_ICC_SML_COMPLETE2]New SIM Detect can be showed");
				               notifyToShowNewSimDetected();
	                    		}
		            		   PowerOnUnlockSIMLock.this.finish();
		            		   Log.d(LOGTAG, "[mHandler][UNLOCK_ICC_SML_COMPLETE][SIM2][Finished]" );
		            		   return ;
		            	   }
	            	   }
	            	  
	            	   
            	   }  
            	   break;
               }//case UNLOCK_ICC_SML_COMPLETE End
               
              case UNLOCK_ICC_SML_QUERYLEFTTIMES:
              {
            	  Log.d(LOGTAG, "[mHandler][UNLOCK_ICC_SML_QUERYLEFTTIMES]+" );
	           	   if (ar.exception != null)
	           	   {
	                	//showAlertDialog(DIALOG_QUERYFAIL);//Query fail!
	           	   }
	           	   else{
	            	   int [] LockState = (int [])ar.result;
	            	   if (LockState[2] > 0)//still have chances to unlock
	            	   {
	            		   mPwdLeftChances = LockState[2];
	            		   mUnlockRetriesNotify.setText(" (" + getString(R.string.simlock_retriesleft) + String.valueOf(mPwdLeftChances) +" )");
	            		   Log.d(LOGTAG, "[mHandler][UNLOCK_ICC_SML_QUERYLEFTTIMES][query Left times]: "+  mPwdLeftChances);
	                   }else{//no chances to unlock current SIM Slot
	                	   Log.d(LOGTAG, "[mHandler][UNLOCK_ICC_SML_QUERYLEFTTIMES][no chances to unlock current SIM Slot]: "+  iSIMMEUnlockNo);
	                	   sendVerifyResult(VERIFY_TYPE_SIMMELOCK,false);
	                	   
	                	   if(iSIMMEUnlockNo == 0){
	                		   MtkPhoneGlobals.arySIMLockStatus[0] = 0;
				               SIM1LockedPermanently = 1;//SIM 1 permanently locked
	                		   Log.d(LOGTAG,"[changed][arySIMLockStatus]: ["+ MtkPhoneGlobals.arySIMLockStatus[0] + " , " + MtkPhoneGlobals.arySIMLockStatus[1] + " ]");
	                		   PowerOnUnlockSIMLock.this.finish();
	                		   setSimLockScreenDone(0,SIMLOCK_TYPE_SIMMELOCK);
	                      		if(MtkPhoneGlobals.arySIMLockStatus[1] == 2){//Need to unlock SIM2
	                            	Intent intent = new Intent(PowerOnUnlockSIMLock.this, PowerOnSetupUnlockSIMLock.class);  
	                        		Bundle bundle = new Bundle();                                
	                                bundle.putInt("Phone.GEMINI_SIM_ID_KEY",1);//To unlock which card  default:-1, Slot1: 0, Slot2:1
	                        		intent.putExtras(bundle);
	                        		startActivity(intent); 
	                    		}
	                    	   	                		   
	                	   }else if(iSIMMEUnlockNo == 1){
	                		   MtkPhoneGlobals.arySIMLockStatus[1] = 0;
				               SIM2LockedPermanently = 1;//SIM 2 permanently locked
	                		   Log.d(LOGTAG,"[changed][arySIMLockStatus]: ["+ MtkPhoneGlobals.arySIMLockStatus[0] + " , " + MtkPhoneGlobals.arySIMLockStatus[1] + " ]");
	                		   PowerOnUnlockSIMLock.this.finish();
	                		   setSimLockScreenDone(1,SIMLOCK_TYPE_SIMMELOCK);
	                      		if(MtkPhoneGlobals.arySIMLockStatus[0] == 2){//Need to unlock SIM2
	                            	Intent intent = new Intent(PowerOnUnlockSIMLock.this, PowerOnSetupUnlockSIMLock.class);  
	                        		Bundle bundle = new Bundle();                                
	                                bundle.putInt("Phone.GEMINI_SIM_ID_KEY",0);//To unlock which card  default:-1, Slot1: 0, Slot2:1
	                        		intent.putExtras(bundle);
	                        		startActivity(intent); 
	                    		}
	                    	   	                		   
	                	   }
	                	   PowerOnUnlockSIMLock.this.finish();
                           return ;
	            		   
	                    	}

	                	}
	           	   Log.d(LOGTAG, "[mHandler][UNLOCK_ICC_SML_QUERYLEFTTIMES]-" );
	           	   break;
               }//UNLOCK_ICC_SML_QUERYLEFTTIMES End
              
            }
           }
    };//new Handler() End

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		//register ACTION_AIRPLANE_MODE_CHANGED
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        // ECC button should be hidden when there is no service.
        intentFilter.addAction(SPN_STRINGS_UPDATED_ACTION);
        registerReceiver(mReceiver, intentFilter);
        
		Log.d(LOGTAG, "[onResume]+");
		
            // ALPS00299096
            int airplaneMode = Settings.System.getInt(this.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);
            Log.d(LOGTAG, "[onResume][getAirPlaneMode] : " + airplaneMode);
            if(airplaneMode != 0) {
                sendVerifyResult(VERIFY_TYPE_SIMMELOCK,false);
                PowerOnUnlockSIMLock.this.finish(); 
                return;
            }

		updateEmergencyCallButtonState(mbtnEmergencyCall);
		//Refresh Left Retries
		Log.d(LOGTAG, "[onResume][iSIMMELockStatus] : " + iSIMMELockStatus);
		
        if (iSIMMELockStatus == 0){
        	   //sendVerifyResult(VERIFY_TYPE_SIMMELOCK,true);
        	  
		      if(iSIMMEUnlockNo == 0){
		    	  MtkPhoneGlobals.arySIMLockStatus[0] = 0;//SIM 1 already dealt
		    	  Log.d(LOGTAG,"[changed][arySIMLockStatus]: ["+ MtkPhoneGlobals.arySIMLockStatus[0] + " , " + MtkPhoneGlobals.arySIMLockStatus[1] + " ]");
		    	  
		    	  setSimLockScreenDone(0,SIMLOCK_TYPE_SIMMELOCK);
		         		if(MtkPhoneGlobals.arySIMLockStatus[1] == 2){//Need to unlock SIM2
		               	Intent intent = new Intent(PowerOnUnlockSIMLock.this, PowerOnSetupUnlockSIMLock.class);  
		           		Bundle bundle = new Bundle();                                
		                   bundle.putInt("Phone.GEMINI_SIM_ID_KEY",1);//To unlock which card  default:-1, Slot1: 0, Slot2:1
		           		intent.putExtras(bundle);
		           		startActivity(intent); 
		       		}
		         	PowerOnUnlockSIMLock.this.finish(); 
		         	return;
		   	   }else if(iSIMMEUnlockNo == 1){
		   		 MtkPhoneGlobals.arySIMLockStatus[1] = 0;//SIM 1 already dealt
		   		Log.d(LOGTAG,"[changed][arySIMLockStatus]: ["+ MtkPhoneGlobals.arySIMLockStatus[0] + " , " + MtkPhoneGlobals.arySIMLockStatus[1] + " ]");
		   		setSimLockScreenDone(1,SIMLOCK_TYPE_SIMMELOCK);
		         		if(MtkPhoneGlobals.arySIMLockStatus[0] == 2){//Need to unlock SIM2
		               	Intent intent = new Intent(PowerOnUnlockSIMLock.this, PowerOnSetupUnlockSIMLock.class);  
		           		Bundle bundle = new Bundle();                                
		                   bundle.putInt("Phone.GEMINI_SIM_ID_KEY",0);//To unlock which card  default:-1, Slot1: 0, Slot2:1
		           		intent.putExtras(bundle);
		           		startActivity(intent); 
		       		} 
		         	PowerOnUnlockSIMLock.this.finish();
		         	return;
		       	  	                		   
		   	   }
    		   
        }else{
		   if(false == FeatureOption.MTK_GEMINI_SUPPORT)
           {
               //Single Card:
           	Message callback_query = Message.obtain(mHandler,  UNLOCK_ICC_SML_QUERYLEFTTIMES);
           	Phone phone = PhoneFactory.getDefaultPhone();
           	phone.getIccCard().QueryIccNetworkLock(lockCategory,0,null,null,null,null,callback_query);
           }
           else
           {
           	Log.d(LOGTAG, "[mHandler][UNLOCK_ICC_SML_COMPLETE][GEMINI Card]+" );
           	GeminiPhone mGeminiPhone = (GeminiPhone)PhoneFactory.getDefaultPhone();
           	if (iSIMMEUnlockNo == 0)
       	   {
       		Message callback_query = Message.obtain(mHandler,  UNLOCK_ICC_SML_QUERYLEFTTIMES);
       		mGeminiPhone.getIccCardGemini(Phone.GEMINI_SIM_1).QueryIccNetworkLock(lockCategory,0,null,null,null,null,callback_query);
		   }   
		   else
		   {
       		Message callback_query = Message.obtain(mHandler,  UNLOCK_ICC_SML_QUERYLEFTTIMES);
       		mGeminiPhone.getIccCardGemini(Phone.GEMINI_SIM_2).QueryIccNetworkLock(lockCategory,0,null,null,null,null,callback_query);
		   }
       	   
           }
	        if(false == FeatureOption.MTK_GEMINI_SUPPORT){
	        	
	        }else{
	        	getMoreInfoConfiguration(iSIMMEUnlockNo);
			   
	        }
	        
	    }
            Log.d(LOGTAG, "[onResume] disable statusbar");
            mStatusBarManager.disable(StatusBarManager.DISABLE_EXPAND);
        Log.d(LOGTAG, "[onResume]-");
		
	}


    public void sendDownUpKeyEvents(int keyEventCode) {
//        long eventTime = SystemClock.uptimeMillis();
//        Handler handler = mPwdDisplay.getHandler();
//        if (handler != null){
//            handler.sendMessage(handler.obtainMessage(ViewRootImpl.DISPATCH_KEY,
//                new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyEventCode, 0, 0, 0, 0,
//                    KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE)));
//            handler.sendMessage(handler.obtainMessage(ViewRootImpl.DISPATCH_KEY,
//                new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyEventCode, 0, 0, 0, 0,
//                        KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE)));        
//        }
    }

/**
 * Inner Class TouchInput
 * Helper class to handle input from touch dialer.  Only relevant when
 * the keyboard is shut.
 */
class TouchInput implements View.OnClickListener,View.OnTouchListener  {
	
	int digit = -1;
    private TextView mZero;
    private TextView mOne;
    private TextView mTwo;
    private TextView mThree;
    private TextView mFour;
    private TextView mFive;
    private TextView mSix;
    private TextView mSeven;
    private TextView mEight;
    private TextView mNine;
    private TextView mOk;
    private TextView mCancelButton;

    TouchInput() {
        mZero = (TextView) findViewById(R.id.zero);
        mOne = (TextView) findViewById(R.id.one);
        mTwo = (TextView) findViewById(R.id.two);
        mThree = (TextView) findViewById(R.id.three);
        mFour = (TextView) findViewById(R.id.four);
        mFive = (TextView) findViewById(R.id.five);
        mSix = (TextView) findViewById(R.id.six);
        mSeven = (TextView) findViewById(R.id.seven);
        mEight = (TextView) findViewById(R.id.eight);
        mNine = (TextView) findViewById(R.id.nine);
        mCancelButton = (TextView) findViewById(R.id.dismiss);
        mOk = (TextView) findViewById(R.id.ok);

        mZero.setText("0");
        mOne.setText("1");
        mTwo.setText("2");
        mThree.setText("3");
        mFour.setText("4");
        mFive.setText("5");
        mSix.setText("6");
        mSeven.setText("7");
        mEight.setText("8");
        mNine.setText("9");

        mZero.setOnClickListener(this);
        mOne.setOnClickListener(this);
        mTwo.setOnClickListener(this);
        mThree.setOnClickListener(this);
        mFour.setOnClickListener(this);
        mFive.setOnClickListener(this);
        mSix.setOnClickListener(this);
        mSeven.setOnClickListener(this);
        mEight.setOnClickListener(this);
        mNine.setOnClickListener(this);
        mOk.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        
        mZero.setOnTouchListener(this);
        mOne.setOnTouchListener(this);
        mTwo.setOnTouchListener(this);
        mThree.setOnTouchListener(this);
        mFour.setOnTouchListener(this);
        mFive.setOnTouchListener(this);
        mSix.setOnTouchListener(this);
        mSeven.setOnTouchListener(this);
        mEight.setOnTouchListener(this);
        mNine.setOnTouchListener(this);
        mOk.setOnTouchListener(this);
        mCancelButton.setOnTouchListener(this);
    }

    //To change button color while touch the button
    public boolean onTouch(View v, MotionEvent event) {
/*    
		// TODO Auto-generated method stub
    	Resources resources = getBaseContext().getResources();
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			v.setBackgroundDrawable(resources.getDrawable(R.drawable.btnkeyboradclick));
		}else if(event.getAction() == MotionEvent.ACTION_UP){
			if(v == mOne || v == mTwo){
				v.setBackgroundDrawable(resources.getDrawable(R.drawable.btnnokeyboradunclicka));
			}else if(v == mThree){
				v.setBackgroundDrawable(resources.getDrawable(R.drawable.btnnokeyboradunclicke));
			}else if (v == mFour || v == mFive){
				v.setBackgroundDrawable(resources.getDrawable(R.drawable.btnnokeyboradunclickb));
			}else if(v == mSix){
				v.setBackgroundDrawable(resources.getDrawable(R.drawable.btnnokeyboradunclickf));
			}else if(v == mSeven || v == mEight){
				v.setBackgroundDrawable(resources.getDrawable(R.drawable.btnnokeyboradunclickc));
			}else if(v == mNine){
				v.setBackgroundDrawable(resources.getDrawable(R.drawable.btnnokeyboradunclickg));
			}else if(v == mOk){
				v.setBackgroundDrawable(resources.getDrawable(R.drawable.btnnokeyboradunclickj));
			}else if(v == mZero){
				v.setBackgroundDrawable(resources.getDrawable(R.drawable.btnnokeyboradunclickd));
			}else if(v == mCancelButton){
				v.setBackgroundDrawable(resources.getDrawable(R.drawable.btnnokeyboradunclickh));
			}else{
			
			}
		}
*/
		return false;
    }

    public void onClick(View v) {
        if (v == mCancelButton) {
        	Log.d(LOGTAG, "[onClick][mCandelButton]+");
        	   sendVerifyResult(VERIFY_TYPE_SIMMELOCK,false);
        	
	     	   if(iSIMMEUnlockNo == 0){
	     		   MtkPhoneGlobals.arySIMLockStatus[0] = 0;//SIM 1 already dealt
		     	   Log.d(LOGTAG,"[changed][arySIMLockStatus]: ["+ MtkPhoneGlobals.arySIMLockStatus[0] + " , " + MtkPhoneGlobals.arySIMLockStatus[1] + " ]");		     		  
		           if(MtkPhoneGlobals.arySIMLockStatus[1] == 2){//Need to unlock SIM2
			           Intent intent = new Intent(PowerOnUnlockSIMLock.this, PowerOnSetupUnlockSIMLock.class);  
			           Bundle bundle = new Bundle();                                
			           bundle.putInt("Phone.GEMINI_SIM_ID_KEY",1);//To unlock which card  default:-1, Slot1: 0, Slot2:1
			           intent.putExtras(bundle);
			           startActivity(intent); 
		           }
		           else {
			   	   //Inform SIM Framework that "New SIM Detect" screen can be showd.
				   Log.d(LOGTAG,"[Dismiss1]New SIM Detect can be showed");
				   notifyToShowNewSimDetected();
			   }
		           setSimLockScreenDone(0,SIMLOCK_TYPE_SIMMELOCK);
		           Intent t = new Intent("action_melock_dismiss");
		           t.putExtra("simslot", 0);
		           PowerOnUnlockSIMLock.this.sendBroadcast(t);
		           PowerOnUnlockSIMLock.this.finish();
		           return;		       	   	                		   
		   	   }else if(iSIMMEUnlockNo == 1){
			       MtkPhoneGlobals.arySIMLockStatus[1] = 0;//SIM 1 already dealt
			   	   Log.d(LOGTAG,"[changed][arySIMLockStatus]: ["+ MtkPhoneGlobals.arySIMLockStatus[0] + " , " + MtkPhoneGlobals.arySIMLockStatus[1] + " ]");
			       if(MtkPhoneGlobals.arySIMLockStatus[0] == 2){//Need to unlock SIM2
			           Intent intent = new Intent(PowerOnUnlockSIMLock.this, PowerOnSetupUnlockSIMLock.class);  
			           Bundle bundle = new Bundle();                                
			           bundle.putInt("Phone.GEMINI_SIM_ID_KEY",0);//To unlock which card  default:-1, Slot1: 0, Slot2:1
			           intent.putExtras(bundle);
			           startActivity(intent);		
			       	}
				else {
				   //Inform SIM Framework that "New SIM Detect" screen can be showd.
				   Log.d(LOGTAG,"[Dismiss2]New SIM Detect can be showed");
				   notifyToShowNewSimDetected();
				}
			       setSimLockScreenDone(1,SIMLOCK_TYPE_SIMMELOCK);
		           Intent t = new Intent("action_melock_dismiss");
		           t.putExtra("simslot", 1);
		           PowerOnUnlockSIMLock.this.sendBroadcast(t);
			        PowerOnUnlockSIMLock.this.finish();
			        return;		       	   	                		   
		   	   }
    		   Log.d(LOGTAG, "[onClick][mCandelButton]-");
    		   return ;
    	}//Cancel Button End
        if (v == mOk) {        	
        	if (mPwdDisplay != null){
    		    strPwd = mPwdDisplay.getText().toString();
    		    PwdLength = strPwd.length();
    		    if(bStringValid(strPwd) == false){
    		    	mUnlockResultNotify.setText(getString(R.string.simlock_input4_8digits)); 
    		    }else{
    	   		    if((PwdLength >= 4) && (PwdLength <= 8)){
        		    	//progressDialog =ProgressDialog.show(PowerOnPowerOnUnlockSIMLock.this,"","Unlocking....");
        		    	progressDialog = new ProgressDialog(PowerOnUnlockSIMLock.this);
        		    	progressDialog.setMessage(getString(R.string.simlock_unlocking));
        		    	progressDialog.setIndeterminate(true);
        		    	progressDialog.setCancelable(false);
        		    	progressDialog.getWindow().setType(
        	                    WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        		    	/*progressDialog.getWindow().setFlags(
                                WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);*/
        		    	progressDialog.show();
        	        	//Process Unlock SIM Lock
        	            if(false == FeatureOption.MTK_GEMINI_SUPPORT){
        	            	Message callback = Message.obtain(mHandler,  UNLOCK_ICC_SML_COMPLETE);
        	                //Single Card:
        	            	Phone phone = PhoneFactory.getDefaultPhone();
        	            	phone.getIccCard().supplyNetworkDepersonalization(mPwdDisplay.getText().toString(),callback);
        	            	Log.d(LOGTAG, "[onClick][mOK][Single Card][SetSIMLock]+" + mPwdDisplay.getText().toString());
        	            }else{
        	            	Log.d(LOGTAG, "[onClick][mOK][Gemini Card][SetSIMLock]+" + mPwdDisplay.getText().toString());
        	            	GeminiPhone mGeminiPhone = (GeminiPhone)PhoneFactory.getDefaultPhone();
        	            	if (iSIMMEUnlockNo == 0){
        	            		 Log.d(LOGTAG, "[onClick][mOK][Gemini Card][SetSIMLock][SIM 1]+" );
        	            		 Message callback = Message.obtain(mHandler,  UNLOCK_ICC_SML_COMPLETE);
        	               	     mGeminiPhone.getIccCardGemini(Phone.GEMINI_SIM_1).supplyNetworkDepersonalization(mPwdDisplay.getText().toString(),callback);
        	            		 Log.d(LOGTAG, "[onClick][mOK][Gemini Card][SetSIMLock][SIM 1]-" );
        	            	}else if (iSIMMEUnlockNo == 1){
        	            		Log.d(LOGTAG, "[onClick][mOK][Gemini Card][SetSIMLock][SIM 2]+" );
        	            		Message callback = Message.obtain(mHandler,  UNLOCK_ICC_SML_COMPLETE);
        	                	mGeminiPhone.getIccCardGemini(Phone.GEMINI_SIM_2).supplyNetworkDepersonalization(mPwdDisplay.getText().toString(),callback);
        	                	Log.d(LOGTAG, "[onClick][mOK][Gemini Card][SetSIMLock][SIM 2]-" );
        	            	}
        	            	Log.d(LOGTAG, "[onClick][mOK][Gemini Card][SetSIMLock]-" );
        		        } 

        		    }else{
        		    	//Notify the user to input 4---8 digits
        		    	mUnlockResultNotify.setText(getString(R.string.simlock_input4_8digits)); 
        		    }
    		    }
 
    		    strPwd = null;//delete number entered before
    		    mPwdDisplay.setText("");    		
    	    } 
        }//if (v == mOK)  End
        
        
        Log.d(LOGTAG, "[onClick][digit]");
        int keyEventCode = checkDigit(v);
        if (keyEventCode >= 0) {
        	if (mPwdDisplay != null){
        		    strPwd = mPwdDisplay.getText().toString();
        		    PwdLength = strPwd.length();
        		    if(PwdLength < 8){
        		    	strPwd += String.valueOf(digit);
        		    	sendDownUpKeyEvents(keyEventCode);
        		    }
        		    	
        		    mPwdDisplay.setText(strPwd);//add digit to display on the Pwd edittext
        		
        	    }
        	//showAlertDialog(DIALOG_ENTERNUMBER);//Show dialog of pressed number key for test
        	digit = -1;
        	
        }
    }//onClick() End
    


    private int checkDigit(View v) {
        int keyEventCode = -1;
        if (v == mZero) {
            digit = 0;
            keyEventCode = KeyEvent.KEYCODE_0;
        } else if (v == mOne) {
            digit = 1;
            keyEventCode = KeyEvent.KEYCODE_1;
        } else if (v == mTwo) {
            digit = 2;
            keyEventCode = KeyEvent.KEYCODE_2;
        } else if (v == mThree) {
            digit = 3;
            keyEventCode = KeyEvent.KEYCODE_3;
        } else if (v == mFour) {
            digit = 4;
            keyEventCode = KeyEvent.KEYCODE_4;
        } else if (v == mFive) {
            digit = 5;
            keyEventCode = KeyEvent.KEYCODE_5;
        } else if (v == mSix) {
            digit = 6;
            keyEventCode = KeyEvent.KEYCODE_6;
        } else if (v == mSeven) {
            digit = 7;
            keyEventCode = KeyEvent.KEYCODE_7;
        } else if (v == mEight) {
            digit = 8;
            keyEventCode = KeyEvent.KEYCODE_8;
        } else if (v == mNine) {
            digit = 9;
            keyEventCode = KeyEvent.KEYCODE_9;
        }
        return keyEventCode;
    }//checkDigit() end
    
    protected void  showAlertDialog(int id){
    	if (id == DIALOG_ENTERNUMBER){
    		AlertDialog.Builder builder = new AlertDialog.Builder(PowerOnUnlockSIMLock.this);
    		switch(id){
    		case DIALOG_ENTERNUMBER: //Press Numbers
    			builder.setIcon(android.R.drawable.ic_dialog_alert)
    	    	.setMessage(String.valueOf(digit))
    	    	.setNegativeButton("OK", new DialogInterface.OnClickListener() 
    	    	{			
    				public void onClick(DialogInterface dialog, int id) 
    				{
    					dialog.cancel();
    				}
    			}).show();
    	    	break;
    		}//switch() End
    	}//if() End
    	
    }//showAlertDialog() funciton End
    
  }// Inner Class End

public void onAttachedToWindow(){
	this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
	super.onAttachedToWindow();
}

public boolean onKeyDown(int keyCode, KeyEvent event) {
	// TODO Auto-generated method stub
	Log.d(LOGTAG, "[onKeyDown][Pressed invalid Key][keyCode()]:" + keyCode );
	switch(keyCode)
	{
		case KeyEvent.KEYCODE_BACK:
		case KeyEvent.KEYCODE_MENU:
		case KeyEvent.KEYCODE_HOME:
			Log.d(LOGTAG, "[onKeyDown][Pressed invalid Key]" );
			return true;
	}
	Log.d(LOGTAG, "[onKey][Pressed invalid Key]-" );
	return false;
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
 
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Log.d(LOGTAG, "[onKeyLongPress][Long Pressed invalid Key][keyCode()]:" + keyCode );
		if(event.getKeyCode() ==  KeyEvent.KEYCODE_MENU){
			Log.d(LOGTAG, "[onKeyLongPress][Pressed invalid Key]" );
			return true;
		}
		return false;
	}	
	
    private final BroadcastReceiver mReceiver = new  BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                finish();
                sendVerifyResult(VERIFY_TYPE_SIMMELOCK,false);
            } else if (SPN_STRINGS_UPDATED_ACTION.equals(action)) {
                Log.d(LOGTAG, "Receive SPN_STRINGS_UPDATED_ACTION ");
                if (intent.getBooleanExtra(EXTRA_SHOW_PLMN, false)) {
                   String plmn = intent.getStringExtra(EXTRA_PLMN);
                   int simId = intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, Phone.GEMINI_SIM_1);
                   int index = simId - Phone.GEMINI_SIM_1;
                   Log.d(LOGTAG, "[SPN_STRINGS_UPDATED_ACTION]index = " + index);
                   Log.d(LOGTAG, "[SPN_STRINGS_UPDATED_ACTION]plmn = " + plmn);
                   if (index < 2) {
                      mIsNoService[index] = (plmn == null);
                   }
                   if (mIsNoService[0] && mIsNoService[1]) {
                       mHandler.removeCallbacks(mHideECCBtnRunnable);
                       mHandler.postDelayed(mHideECCBtnRunnable, 500);
                       mECCBtnRunnableStarted = true;
                   }
                   else
                   {
                       if (mECCBtnRunnableStarted) {
                           mHandler.removeCallbacks(mHideECCBtnRunnable);
                           mECCBtnRunnableStarted = false;
                       }
                       mbtnEmergencyCall.setVisibility(View.VISIBLE);
                   }
                }
            }
        }
    };

    private Runnable mHideECCBtnRunnable = new Runnable() {
        public void run() {
            mbtnEmergencyCall.setVisibility(View.INVISIBLE);
        }
    };

  //To get OptrName by Slot No
    public String getOptrNameBySlotId(long Slot){
    	Log.d(LOGTAG, "[getOptrNameBySlotId][Slot]: "+  (int)Slot);
    	if(Slot >= 0){
    		SIMInfo info = SIMInfo.getSIMInfoBySlot(this,(int)Slot);
    		Log.d(LOGTAG, "[getOptrNameBySlotId][info]: "+  info);    		
    		if (info.mDisplayName != null){
    			Log.d(LOGTAG, "[getOptrNameBySlotId][OptrName]: "+  info.mDisplayName);
    			return info.mDisplayName;
    		}else{
    			return this.getResources().getString(R.string.simlock_newsim);
    		}		
    	}else{
    		return this.getResources().getString(R.string.simlock_nocard);
    	}
    }

    public Drawable getOptrDrawableBySlotId(long Slot){
        if(Slot >= 0){
            SIMInfo info = SIMInfo.getSIMInfoBySlot(this,(int)Slot);
            Log.d(LOGTAG, "[getOptrDrawableBySlotId][info]: "+  info);
            if(info != null){
                return this.getResources().getDrawable(info.mSimBackgroundDarkRes);    			
            } else
                return null;
        }else{
            return null;
        }
    }
    
    public static boolean bCharNumber(char ch){
    	if((ch >= '0') && (ch <= '9')){
    		return true;
    	}else{
    		return false;
    	}
    }
    
    
    public static boolean bStringValid(String str){
    	for(int i = 0;i < str.length();i++){
    		if(bCharNumber(str.charAt(i)) == false){
    			return false;
    		}
    	}
    		return true;    	
    }
    
    public void updateEmergencyCallButtonState(Button button) {
        int newState = TelephonyManager.getDefault().getCallState();
        int textId;
        if (newState == TelephonyManager.CALL_STATE_OFFHOOK) {
            // show "return to call" text and show phone icon
            button.setText(getString(R.string.simlock_returntocall));
        } else {
            button.setText(getString(R.string.simlock_emergency));
        }
    }
    
    
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
    
    //To get MoreInfo configuration by SIMInfo
    public void getMoreInfoConfiguration(long Slot){
        Log.d(LOGTAG, "[getMoreInfoConfiguration][Slot]: "+  (int)Slot);
        if(Slot >= 0){
            SIMInfo info = SIMInfo.getSIMInfoBySlot(this,(int)Slot);
            Log.d(LOGTAG, "[getMoreInfoConfiguration][info]: "+  info);         
            if(true == FeatureOption.MTK_GEMINI_SUPPORT){
                if ((info == null) || (info.mDisplayName == null)){
                    //set MoreInfo button
                    if (iSIMMEUnlockNo == 0){
                        mUnlockForSIMNotify.setText(getString(R.string.simlock_firstsim));
                    }else{
                        mUnlockForSIMNotify.setText(getString(R.string.simlock_secondsim));
                    }
                    mUnlockSIMNameNotify.setVisibility(View.GONE);
                    mBtnMoreInfo.setVisibility(View.VISIBLE);
                    mBtnMoreInfo.setText(getString(R.string.simlock_moreinfo));
                    return ;
                }else{
                    //Set SIM Info name
                    mUnlockSIMNameNotify.setText(getOptrNameBySlotId(iSIMMEUnlockNo));
                    mUnlockSIMNameNotify.setBackgroundDrawable(getOptrDrawableBySlotId(iSIMMEUnlockNo));
                    mUnlockSIMNameNotify.setPadding(mSIMCardNamePadding, 0, mSIMCardNamePadding, 0);
                }       
            }
        }else{
            return ;
        }
    }


	public static boolean hasIccCard(int slot) {

		boolean bRet = false;

		if (true == FeatureOption.MTK_GEMINI_SUPPORT) {
			try {
				final ITelephony iTelephony = ITelephony.Stub
						.asInterface(ServiceManager
								.getService(Context.TELEPHONY_SERVICE));
				if (null != iTelephony) {
					bRet = iTelephony.isSimInsert(slot);
				}
			} catch (RemoteException ex) {
				ex.printStackTrace();
			}
		} else {
			try {
				final ITelephony iTelephony = ITelephony.Stub
						.asInterface(ServiceManager
								.getService(Context.TELEPHONY_SERVICE));
				if (null != iTelephony) {
					bRet = iTelephony.isSimInsert(0);
				}
			} catch (RemoteException ex) {
				ex.printStackTrace();
			}
		}

		return bRet;
	}

	public static PowerOnUnlockSIMLock getInstance()
	{
		return mInstance;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		
		ImageView img = new ImageView(this);
		int imgId = -1;
		if (iSIMMEUnlockNo == 0){
			//Deal with SIM1
			if(hasIccCard(1) == true){
				//SIM2 inserted 
				imgId = R.drawable.pinlock01;
			}else{
				//SIM2 not inserted
				imgId = R.drawable.pinlock04;
			}
		}else {
			//Deal with SIM2
			if(hasIccCard(0) == true){
				//SIM1 inserted
				imgId = R.drawable.pinlock02;
			}else{
				//SIM1 not inserted
				imgId = R.drawable.pinlock03;
			}
		}
		img.setImageResource(imgId);
		new AlertDialog.Builder(this).setMessage(getString(R.string.simlock_unlockmardedcard)).setView(img)
			.setPositiveButton(getString(R.string.simlock_ok), null).show();
		return super.onCreateDialog(id);
	}
    
    
	private void decideLockCategory(){
        Bundle bundle = this.getIntent().getExtras();//get the current Lock Status: 
        if(bundle != null){
        	iSIMMEUnlockNo = bundle.getInt("Phone.GEMINI_SIM_ID_KEY",-1);
        	iSIMMELockStatus = bundle.getInt("SIMMELOCKSTATUS", -1);
        	Log.d(LOGTAG, "[onCreate][iSIMMELockStatus][original]: "+iSIMMELockStatus);
        	Log.d(LOGTAG, "[onCreate][iSIMMEUnlockNo]: "+iSIMMEUnlockNo);
        	SIM1MELockStatus[0] = (iSIMMELockStatus & 0x200) >> 9;
        	SIM1MELockStatus[1] = (iSIMMELockStatus & 0x100) >> 8;
        	SIM1MELockStatus[2] = (iSIMMELockStatus & 0x080) >> 7;
        	SIM1MELockStatus[3] = (iSIMMELockStatus & 0x040) >> 6;
        	SIM1MELockStatus[4] = (iSIMMELockStatus & 0x020) >> 5;
        	SIM2MELockStatus[0] = (iSIMMELockStatus & 0x010) >> 4;
        	SIM2MELockStatus[1] = (iSIMMELockStatus & 0x008) >> 3;
        	SIM2MELockStatus[2] = (iSIMMELockStatus & 0x004) >> 2;
        	SIM2MELockStatus[3] = (iSIMMELockStatus & 0x002) >> 1;
        	SIM2MELockStatus[4] = (iSIMMELockStatus & 0x001) >> 0;
        }
        
       
        if (SIM1MELockStatus[0] == 1){//SIM1 NP locked
        	LOCKCATEGORY = String.valueOf(NPLOCKTYPE);
        	lockCategory = 0;        	
        }else if(SIM1MELockStatus[1] == 1)
        {
        	LOCKCATEGORY = String.valueOf(NSPLOCKTYPE);
        	lockCategory = 1;
        }else if(SIM1MELockStatus[2] == 1)
        {
        	LOCKCATEGORY = String.valueOf(SPLOCKTYPE);
        	lockCategory = 2;
        }else if(SIM1MELockStatus[3] == 1)
        {
        	LOCKCATEGORY = String.valueOf(CPLOCKTYPE);
        	lockCategory = 3;
        }else if(SIM1MELockStatus[4] == 1)
        {
        	LOCKCATEGORY = String.valueOf(SIMPLOCKTYPE);
        	lockCategory = 4;
        }else if (SIM2MELockStatus[0] == 1)//SIM2 NP locked
        {
        	LOCKCATEGORY = String.valueOf(NPLOCKTYPE);
        	lockCategory = 0;
        }else if(SIM2MELockStatus[1] == 1)
        {
        	LOCKCATEGORY = String.valueOf(NSPLOCKTYPE);
        	lockCategory = 1;
        }else if(SIM2MELockStatus[2] == 1)
        {
        	LOCKCATEGORY = String.valueOf(SPLOCKTYPE);
        	lockCategory = 2;
        }else if(SIM2MELockStatus[3] == 1)
        {
        	LOCKCATEGORY = String.valueOf(CPLOCKTYPE);
        	lockCategory = 3;
        }else if(SIM2MELockStatus[4] == 1)
        {
        	LOCKCATEGORY = String.valueOf(SIMPLOCKTYPE);
        	lockCategory = 4;
        }
   }

    /**
     * Send broadcast intent to inform GeminiPhone to show "New SIM detected". 
     * "New SIM detected" dialog should be showed after SIM ME unlock screen is dismissed.
     */
    private void notifyToShowNewSimDetected() {
	Intent intent = new Intent(TelephonyIntents.ACTION_SHOW_NEW_SIM_DETECTED);
        intent.putExtra("simslot", iSIMMEUnlockNo);
	sendStickyBroadcast(intent);
    }
    
    
}//Main Class End
