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
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.PhoneLookup;
import gionee.provider.GnContactsContract.RawContacts;

import gionee.provider.GnCallLog.Calls;
import static com.android.phone.AuroraMSimConstants.SUBSCRIPTION_KEY;
import android.view.ViewConfiguration;

public class AuroraPhoneUtils {
	private static final String LOG_TAG = "AuroraPhoneUtils";

	static int getOtherSlot(int slot) {
		return slot == AuroraMSimConstants.SUB1 ? AuroraMSimConstants.SUB2
				: AuroraMSimConstants.SUB1;
	}

	public static boolean isSimulate() {
		if (DeviceUtils.isIUNI() || DeviceUtils.is7503()) {
			SharedPreferences sP = null;
			sP = PhoneGlobals.getInstance().getSharedPreferences(
					"com.android.phone_preferences", Context.MODE_PRIVATE);
			return sP != null && sP.getBoolean("aurora_simulate_switch", false);
		}
		return false;
	}

	public static boolean answerCallAndSleepSoon(Call ringingCall) {
		log("answerCallAndSleepSoon(" + ringingCall + ")...");
		boolean result = PhoneUtils.answerCall(ringingCall);
		if (result) {
			PhoneGlobals.getInstance().mHandler.postDelayed(new Runnable() {
				public void run() {
					if (PhoneGlobals.getInstance().isShowingCallScreen()) {
						PhoneGlobals.getInstance().goToSleep();
					}
				}
			}, 5000);
		}
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
			sub = intent.getIntExtra(Constants.EXTRA_SLOT_ID, PhoneGlobals
					.getInstance().getVoiceSubscription());
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
	            } else if (phoneType == PhoneConstants.PHONE_TYPE_GSM) {
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
	 
}