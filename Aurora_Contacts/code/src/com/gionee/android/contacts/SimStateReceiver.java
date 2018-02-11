package com.gionee.android.contacts;
import gionee.provider.GnContactsContract;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.GNContactsUtils;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.TelephonyIntents;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.mediatek.contacts.simcontact.SimCardUtils;

/*
 *     load sim contacts only for qc 
 */

public class SimStateReceiver extends BroadcastReceiver {
    private static boolean DBG = true;
    private static String TAG = "SimStateReceiver";

    private static final int SUB1 = 0;
    private static final int SUB2 = 1;
    private static final String SUBSCRIPTION_KEY  = "subscription";
    
    private static boolean mIsNeedDelete = true;

    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.v("broadcast", "SimStateReceiver");
        if (!GNContactsUtils.isOnlyQcContactsSupport()) {
            return; 
        }
        
        final String action = intent.getAction();
        mContext = context;
        log("received broadcast " + action);
        
        if (!ContactsUtils.mIsIUNIDeviceOnly) {
        	if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
        		sendPhoneBoot();
        	}
        	
        	return;
        }
        
        if (TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(action)) {
            final int subscription = intent.getIntExtra(SUBSCRIPTION_KEY, SUB1);
            final String stateExtra = intent.getStringExtra("ss");
            final int simState;
            log("ACTION_SIM_STATE_CHANGED intent received on sub = " + subscription
                + "  SIM STATE IS " + stateExtra);

            if ("IMSI".equals(stateExtra)) {
                simState = GNContactsUtils.SIM_STATE_NOT_READY;
                return;
            } else if ("ABSENT".equals(stateExtra)) {
                simState = GNContactsUtils.SIM_STATE_ERROR;
            } else if ("LOADED".equals(stateExtra)) {
                simState = GNContactsUtils.SIM_STATE_READY;
				Log.v("broadcast", "SimStateReceiver simState=LOADED");
            } else if ("CARD_IO_ERROR".equals(stateExtra)) {
                simState = GNContactsUtils.SIM_STATE_ERROR;
                return;
            } else if ("UNKNOWN".equals(stateExtra)){
                log("icc card status is unknown, ignore it");
                return;
            } else if ("READY".equals(stateExtra)) {
                log("icc card status is ready, ignore it");
                return;
            } else {
                simState = GNContactsUtils.SIM_STATE_ERROR;
//                return;
            }
            if (GNContactsUtils.isMultiSimEnabled()) {
            	//aurora add liguangyu 20140915 for BUG #8306 start
                GNContactsUtils.cardIsUsimInternal(0);
                GNContactsUtils.cardIsUsimInternal(1);
                SimCardUtils.getSimTypeBySlotInternal(0);
                SimCardUtils.getSimTypeBySlotInternal(1);
                //aurora add liguangyu 20140915 for BUG #8306 end
                sendSimState(subscription, simState);
            } else {
                //aurora add liguangyu 20140915 for BUG #8306 start
                GNContactsUtils.cardIsUsimInternal(0);
                SimCardUtils.getSimTypeBySlotInternal(0);             
                //aurora add liguangyu 20140915 for BUG #8306 end
                if (simState == GNContactsUtils.SIM_STATE_READY && SimContactsService.mIsImporting) {
                    return;
                }    
                sendSimState(simState);
            }
            
            mIsNeedDelete = false;
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
        	clearContacts();//clear contacts where deleted=1 and sync4 is null
        	
        	if (SimCardUtils.isSimStateReady(0)) {
        		mIsNeedDelete = false;
        	}
        	
        	if (FeatureOption.MTK_GEMINI_SUPPORT && SimCardUtils.isSimStateReady(1)) {
        		mIsNeedDelete = false;
        	}
        	
        	if (mIsNeedDelete ) {
        		sendPhoneBoot();
        	}
        } else if ("android.intent.action.ACTION_SIM_REFRESH_UPDATE".equals(action)) {
            final int subscription = intent.getIntExtra(SUBSCRIPTION_KEY, SUB1);
            log("ACTION_SIM_REFRESH_UPDATE intent received on sub = " + subscription);
            sendSimRefreshUpdate(subscription);
        } else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
            boolean isAirplaneModeOn = intent.getBooleanExtra("state", false);
            log("[processAirplaneModeChanged]isAirplaneModeOn:" + isAirplaneModeOn);
            if (isAirplaneModeOn && SimContactsService.mIsImporting) {
                return;
            }
			GNContactsUtils.mSimState[0]=GNContactsUtils.SIM_STATE_NOT_READY;//aurora change zhouxiaobing 20140708 for simcontacts
			GNContactsUtils.mSimState[1]=GNContactsUtils.SIM_STATE_NOT_READY;
            sendAirModeRefresh(isAirplaneModeOn);
        }else if("com.android.contacts.ACTION_PHB_LOAD_FINISHED".equals(action))//aurora change zhouxiaobing 20140708 for simcontacts
        {
           int slotid=intent.getIntExtra("slotId",0);
           log("ACTION_PHB_LOAD_FINISHED slotid="+slotid);
           
           GNContactsUtils.mSimState[slotid]=GNContactsUtils.SIM_STATE_READY;
		}else if(action.equals("android.intent.action.ACTION_SHUTDOWN"))
		{
          // sendPhoneBoot(); 
		}
        
        if (GNContactsUtils.isMultiSimEnabled()) {
            updateMutilSimInfor();
        }
    }

    private void sendPhoneBoot() {
    	Log.v("broadcast", "sendPhoneBoot");
        Bundle args = new Bundle();
        args.putInt(SimContactsService.OPERATION,GNContactsUtils.OP_PHONE);
        mContext.startService(new Intent(mContext, SimContactsService.class)
                .putExtras(args));
    }

    private void sendSimState(int subscription, int state) {
    	    Log.v("broadcast", "sendSimState");
			GNContactsUtils.mSimState[subscription]=GNContactsUtils.SIM_STATE_NOT_READY;//aurora change zhouxiaobing 20140708 for simcontacts
			//GNContactsUtils.mSimState[1]=GNContactsUtils.SIM_STATE_NOT_READY;
            Bundle args = new Bundle();
            args.putInt(GNContactsUtils.SUB,subscription);
            args.putInt(SimContactsService.OPERATION,GNContactsUtils.OP_SIM);
            args.putInt(SimContactsService.SIM_STATE,state);
            mContext.startService(new Intent(mContext, SimContactsService.class)
                    .putExtras(args));
    }

    private void sendSimState(int state) {
    	Log.v("broadcast", "sendSimState");
		    GNContactsUtils.mSimState[0]=GNContactsUtils.SIM_STATE_NOT_READY;//aurora change zhouxiaobing 20140708 for simcontacts
			//GNContactsUtils.mSimState[1]=GNContactsUtils.SIM_STATE_NOT_READY;
            Bundle args = new Bundle();
            args.putInt(SimContactsService.OPERATION,GNContactsUtils.OP_SIM);
            args.putInt(SimContactsService.SIM_STATE,state);
            Intent intent = new Intent(mContext, SimContactsService.class).putExtras(args);
            mContext.startService(intent);
    }

    private void sendSimRefreshUpdate(int subscription) {
    	Log.v("broadcast", "sendSimRefreshUpdate");
        Bundle args = new Bundle();
        args.putInt(SimContactsService.OPERATION,GNContactsUtils.OP_SIM_REFRESH);
        args.putInt(GNContactsUtils.SUB, subscription);
        mContext.startService(new Intent(mContext, SimContactsService.class)
                .putExtras(args));
    }
    
    private void sendAirModeRefresh(boolean isAirPlaneOn) {
    	Log.v("broadcast", "sendAirModeRefresh");
    	Bundle args = new Bundle();
        args.putBoolean("airplane_state", isAirPlaneOn);

        if (isAirPlaneOn) {
            args.putInt(GNContactsUtils.OPERATION, GNContactsUtils.OP_SIM_DELETE);
        } else {
            args.putInt(GNContactsUtils.OPERATION, GNContactsUtils.OP_SIM);
            args.putInt(SimContactsService.SIM_STATE, GNContactsUtils.SIM_STATE_READY);
        }
        
        Intent intent = new Intent(mContext, SimContactsService.class);
        
    	if (GNContactsUtils.isMultiSimEnabled() && !isAirPlaneOn) {
    		Log.d(TAG, "SimCardUtils.isSimStateReady(0) = " + SimCardUtils.isSimStateReady(0));
    		if (SimCardUtils.isSimStateReady(0)) {
    			args.putInt(GNContactsUtils.SUB, 0);
        		intent.putExtras(args);
                //aurora modify liguangyu 20140729 for #6971 start
                processSimContactsService(isAirPlaneOn, intent);
                //aurora modify liguangyu 20140729 for #6971 end
    		}
    		
    		Log.d(TAG, "SimCardUtils.isSimStateReady(1) = " + SimCardUtils.isSimStateReady(1));
    		if (SimCardUtils.isSimStateReady(1)) {
    			args.putInt(GNContactsUtils.SUB, 1);
    			Intent intent2 = new Intent(mContext, SimContactsService.class).putExtras(args);
    	        //aurora modify liguangyu 20140729 for #6971 start
    	        processSimContactsService(isAirPlaneOn, intent2);
    	        //aurora modify liguangyu 20140729 for #6971 end
    		}
    		
    		return;
    	}
    	
    	intent.putExtras(args);
        //aurora modify liguangyu 20140729 for #6971 start
        processSimContactsService(isAirPlaneOn, intent);
        //aurora modify liguangyu 20140729 for #6971 end
    }
    
    private void updateMutilSimInfor() {
        SIMInfoWrapper simInfoWrapper = SIMInfoWrapper.getSimWrapperInstanceUnCheck();
        if (simInfoWrapper != null) {
            simInfoWrapper.updateSimInfoCache();
        }
    }
 
    protected void log(String msg) {
        Log.d(TAG, msg);
    }
    
    
    //aurora add liguangyu 20140729 for #6971 start
    private static final int SLEEP_OVER = 0;
	private final Handler mHandler = new Handler() {
	        
	      @Override
	      public void handleMessage(Message msg) {
	          
	          switch(msg.what) {
		          case SLEEP_OVER:
		              try {
			        	  Intent intent = (Intent)msg.obj;
			          	  mContext.startService(intent);
		              } catch (Exception e) {
		                  e.printStackTrace();
		              }
		        	  break;
		          default:
		        	  break;
	          }
	      }
	    
	};
	
	private void processSimContactsService(boolean isAirPlaneOn, Intent intent) {
        try {
        	mContext.stopService(intent);
	        if(!isAirPlaneOn) {
	//            Thread.sleep(3000);
	        	Message msg = mHandler.obtainMessage(SLEEP_OVER, intent);
	        	mHandler.sendMessageDelayed(msg, 3000);
	        } else {
	        	mContext.startService(intent);
	        }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	//aurora add liguangyu 20140729 for #6971 end
	
	
	//aurora add qiaohu 20141022 start
	private void clearContacts(){
		new Thread(){
    		public void run() {
    			ContentResolver cr=mContext.getContentResolver();
				cr.delete(Uri.parse("content://com.android.contacts/raw_contacts").buildUpon().appendQueryParameter("batch", "true")
				.appendQueryParameter(GnContactsContract.CALLER_IS_SYNCADAPTER,String.valueOf(true)).build(), "deleted=1 and sync4 is null",null);
    		}
    	}.start();
	}
	//aurora add qiaohu 20141022 end 
}

