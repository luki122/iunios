package com.android.phone;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.IBluetoothHeadsetPhone;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.net.sip.SipManager;
import android.os.AsyncResult;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.Toast;

import com.android.internal.telephony.*;
import com.android.internal.telephony.cdma.CdmaConnection;
import com.android.internal.telephony.sip.SipPhone;
import com.android.phone.GnPhoneRecordHelper;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import aurora.widget.*;
import aurora.app.*;
import gionee.os.storage.GnStorageManager;
import android.provider.CallLog;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.Settings.System;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.PhoneLookup;
import gionee.provider.GnContactsContract.RawContacts;
import gionee.provider.GnCallLog.Calls;
import gionee.provider.GnTelephony.SIMInfo;
import gionee.telephony.AuroraTelephoneManager;
import static com.android.phone.AuroraMSimConstants.SUBSCRIPTION_KEY;
import android.view.ViewConfiguration;
import android.location.Country;
import android.location.CountryDetector;
import android.location.CountryListener;
import android.app.KeyguardManager;
import android.app.ActivityManager;
import android.telephony.SubscriptionManager;

public class AuroraPhoneUtils {
	private static final String LOG_TAG = "AuroraPhoneUtils";

	static int getOtherSlot(int slot) {
		return slot == AuroraMSimConstants.SUB1 ? AuroraMSimConstants.SUB2
				: AuroraMSimConstants.SUB1;
	}

	public static boolean isSimulate() {
//		if (DeviceUtils.isIUNI() || DeviceUtils.is7503()) {
			SharedPreferences sP = null;
			sP = PhoneGlobals.getInstance().getSharedPreferences(
					"com.android.phone_preferences", Context.MODE_PRIVATE);
			return sP != null && sP.getBoolean("aurora_simulate_switch", false);
//		}
//		return false;
	}

	public static boolean answerCallAndSleepSoon(Call ringingCall) {
		log("answerCallAndSleepSoon(" + ringingCall + ")...");
		boolean result = PhoneUtils.answerCall(ringingCall);
//		if (result) {
//			PhoneGlobals.getInstance().mHandler.postDelayed(new Runnable() {
//				public void run() {
//					if (PhoneGlobals.getInstance().isShowingCallScreen()) {
//						PhoneGlobals.getInstance().goToSleep();
//					}
//				}
//			}, 5000);
//		}
		return result;
	}

	private static void log(String msg) {
		Log.d(LOG_TAG, msg);
	}

	public static float getDensity() {
		return PhoneGlobals.getInstance().getResources().getDisplayMetrics().density;
	}

	public static int getSlot(final Intent intent) {
		int sub = intent.getIntExtra(SUBSCRIPTION_KEY, -1);
		if (sub == -1) {
			sub = intent.getIntExtra(Constants.EXTRA_SLOT_ID, -1);
		}
		return sub;
	}

    public static Intent getExplicitIntent(Context context, Intent implicitIntent) {
        Log.i(LOG_TAG, "getExplicitIntent()...");
//        // Retrieve all services that can match the given intent
//        PackageManager pm = context.getPackageManager();
//        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
//        // Make sure only one match was found
//        if (resolveInfo == null || resolveInfo.size() != 1) {
//            return null;
//        }
//        // Get component info and create ComponentName
//        ResolveInfo serviceInfo = resolveInfo.get(0);
//        String packageName = serviceInfo.serviceInfo.packageName;
//        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName("com.android.mms", "com.android.mms.ui.NoConfirmationSendServiceProxy");
        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);
        // Set the component to be explicit
        explicitIntent.setComponent(component);
        return explicitIntent;
    }
    
	 public static void internalAnswerCall() {
	        Log.i(LOG_TAG, "internalAnswerCall()...");
	        CallManager mCM = PhoneGlobals.getInstance().mCM;
	        
	        final boolean hasRingingCall = mCM.hasActiveRingingCall();

	        if (hasRingingCall) {
	            Phone phone = mCM.getRingingPhone();
	            if (DBG) log(" Ringing Phone" + phone);
	            Call ringing = mCM.getFirstActiveRingingCall();
	            int phoneType = phone.getPhoneType();
	            if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
	                if (DBG) log("internalAnswerCall: answering (CDMA)...");
	                if (mCM.hasActiveFgCall()
	                        && mCM.getFgPhone().getPhoneType() == PhoneConstants.PHONE_TYPE_SIP) {
	                    if (DBG) log("internalAnswerCall: answer "
	                            + "CDMA incoming and end SIP ongoing");
	                    PhoneUtils.answerAndEndActive(mCM, ringing);
	                } else {
	                    PhoneUtils.answerCall(ringing);
	                }
	            } else if (phoneType == PhoneConstants.PHONE_TYPE_SIP) {
	                if (DBG) log("internalAnswerCall: answering (SIP)...");
	                if (mCM.hasActiveFgCall()
	                        && mCM.getFgPhone().getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA) {
	                    if (DBG) log("internalAnswerCall: answer "
	                            + "SIP incoming and end CDMA ongoing");
	                    PhoneUtils.answerAndEndActive(mCM, ringing);
	                } else {
	                    PhoneUtils.answerCall(ringing);
	                }
	            } else if (phoneType == PhoneConstants.PHONE_TYPE_GSM || phoneType == PhoneConstants.PHONE_TYPE_IMS) {
	                if (DBG) log("internalAnswerCall: answering (GSM)...");
	                final boolean hasActiveCall = mCM.hasActiveFgCall();
	                final boolean hasHoldingCall = mCM.hasActiveBgCall();

	                if (hasActiveCall && hasHoldingCall) {
	                    if (DBG) log("internalAnswerCall: answering (both lines in use!)...");
	                    PhoneUtils.answerAndEndHolding(mCM, ringing);                    
	                } else {
	                    if (DBG) log("internalAnswerCall: answering...");
	                    PhoneUtils.answerCall(ringing);  // Automatically holds the current active call,
	                                                    // if there is one
	                }
	            } else {
	                throw new IllegalStateException("Unexpected phone type: " + phoneType);
	            }
	            PhoneGlobals.getInstance().setLatestActiveCallOrigin(null);
	        }
	    }
	 
	    private static final boolean DBG = true;
	    
	    public static String formatNumber(String number) {
	    	String countryIso = null;
       	 try {
                CountryDetector detector =
                    (CountryDetector) PhoneGlobals.getInstance().getSystemService(Context.COUNTRY_DETECTOR);
//                detector.addCountryListener(this, null);
                final Country country = detector.detectCountry();
                if(country != null) {
                    countryIso = country.getCountryIso();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
           return PhoneNumberUtils.formatNumber(number, countryIso);
	    }
	    
	    public static boolean isShowFullScreenWhenRinging() {
	        boolean isShowFullScreen = false;
	        try {  		   		  
	 		    ActivityManager am = (ActivityManager) PhoneGlobals.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
	 		    ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
	 	        log("topActiviy = " +  cn.getClassName());
	 	        if(cn.getClassName().contains("I2InCallScreen") ||
	 	        		cn.getClassName().contains("com.aurora.launcher")
	 	        		|| cn.getClassName().contains("com.android.contacts")
	 	        		|| cn.getClassName().contains("com.android.camera")) {
	 	        	isShowFullScreen = true;
	 	        }
	 	    } catch (Exception e) {
	 		   e.printStackTrace();
	 	    }
	        
	        KeyguardManager keyguardManager = (KeyguardManager) PhoneGlobals.getInstance().getSystemService(Context.KEYGUARD_SERVICE);
	        if(keyguardManager.isKeyguardLocked()) {
		        	isShowFullScreen = true;
		     }
	        return isShowFullScreen;
	    }
	    
	  //还要加入判断ServiceState的判断才行，现在只是判断了卡插入的状态，双卡设置中关闭卡对这里无影响。需要在GnTelephonyManager加接口
	    public static boolean isShowDoubleButton() {
	    	 if(DeviceUtils.is7503() || DeviceUtils.is7505()) {    		 
		   	 	 int dualSimModeSetting = System.getInt(PhoneGlobals.getInstance().getContentResolver(), "msim_mode_setting", 3);
		   		 return SIMInfo.getInsertedSIMCount(PhoneGlobals.getInstance()) > 1 && (dualSimModeSetting == 3);
	   	     } else if(AuroraTelephoneManager.isMtkGemini()) { 
		       	  int dualSimModeSetting = System.getInt(PhoneGlobals.getInstance().getContentResolver(), "dual_sim_mode_setting", 3);
	    		 return SIMInfo.getInsertedSIMCount(PhoneGlobals.getInstance()) > 1 && (dualSimModeSetting == 3);
	    	 } else {
	    	     boolean isAllActive = false;
	    		 try {
	                 int simstate = android.provider.Settings.Global.getInt(PhoneGlobals.getInstance().getContentResolver(),
	           		   "mobile_data"+ 2);            
	                 log("updateCardState restore simstate= " + simstate);
	                 if(simstate == 3) {
	                 	isAllActive = true;
	                 } 
	             } catch (Exception e) {
	          	    e.printStackTrace();
	             }    
	    		 return SIMInfo.getInsertedSIMCount(PhoneGlobals.getInstance()) > 1 && isAllActive;
	    	 }
	    }
	    
	public static boolean simStateReady(int simId) {
		if (PhoneUtils.isMultiSimEnabled()) {
			boolean simReady = (TelephonyManager.SIM_STATE_READY == PhoneUtils.getSimState(simId));
			return simReady;
		} else {
			boolean simReady = (TelephonyManager.SIM_STATE_READY == TelephonyManager
					.getDefault().getSimState());
			return simReady;
		}
	}
	
	public static boolean hasDefaultSim() {
		int slot = SubscriptionManager.getDefaultVoicePhoneId();
		log("hasDefaultSim = " + slot);
		return slot == 0 || slot == 1;		
	}
	
	public static String getTimeString(long timeElapsed) {
    	String t= DateUtils.formatElapsedTime(timeElapsed);
    	if(t.length() == 4) {
    		t = "0" + t; 
    	}
    	return t;
    }
	
	private static String getOperator(int slot) {
		return TelephonyManager.getDefault().getNetworkOperatorForPhone(slot);
	}
	
	// 中国移动的460+ 00 、02 、07
		// 中国联通的460+01、10
		// 中国电信的460+03.
	private static  String getTitleFromOperatorNumber(Context context, String number) {
			log( "getTitleFromOperatorNumber =" + number);
			int resId = R.string.unknown;
			if (!TextUtils.isEmpty(number)) {
				if (number.equalsIgnoreCase("46000")
						|| number.equalsIgnoreCase("46002")
						|| number.equalsIgnoreCase("46007")) {
					resId = R.string.operator_china_mobile;
					log("getTitleFromOperatorNumber2 ="
							+context.getResources().getString(resId));
				} else if (number.equalsIgnoreCase("46001")
						|| number.equalsIgnoreCase("46010")) {
					resId = R.string.operator_china_unicom;
				} else if (number.equalsIgnoreCase("46003")) {
					resId = R.string.operator_china_telecom;
				}
			} else {
				return "";
			}
			return context.getResources().getString(resId);
		}
	 
		public static String  getOperatorTitle(int slot) {
			return getTitleFromOperatorNumber(PhoneGlobals.getInstance(), getOperator(slot));
		} 
		
		 public static String getCdmaRatModeKey(int subId) {
//		        if (("OP09").equals(SystemProperties.get("ro.operator.optr", "OM"))) {
		            return Settings.Global.LTE_ON_CDMA_RAT_MODE;
//		        } else {
//		            return Settings.Global.LTE_ON_CDMA_RAT_MODE + subId;
//		        }
		    }
		 
		 public static AuroraCallerInfo getAuroraCallerInfo(Call call) {			
			 int phoneType = call.getPhone().getPhoneType();
				Connection conn = null;
				if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
					conn = call.getLatestConnection();
				} else if ((phoneType == PhoneConstants.PHONE_TYPE_GSM)
						|| (phoneType == PhoneConstants.PHONE_TYPE_SIP)) {
					conn = call.getEarliestConnection();
				}
				AuroraCallerInfo info = PhoneUtils.getCallerInfo(PhoneGlobals.getInstance(), conn);
				return info;
		 }
}