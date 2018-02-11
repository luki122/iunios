package com.aurora.callsetting;

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

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import gionee.provider.GnCallLog.Calls;
import static com.aurora.callsetting.AuroraMSimConstants.SUBSCRIPTION_KEY;
import android.view.ViewConfiguration;
import android.telephony.SubscriptionManager;

import android.telecom.TelecomManager;
import android.telecom.PhoneAccountHandle;

public class AuroraPhoneUtils {
	private static final String LOG_TAG = "AuroraPhoneUtils";

	static int getOtherSlot(int slot) {
		return slot == AuroraMSimConstants.SUB1 ? AuroraMSimConstants.SUB2
				: AuroraMSimConstants.SUB1;
	}

	private static void log(String msg) {
		Log.d(LOG_TAG, msg);
	}

	public static int getSlot(final Intent intent) {
		int sub = intent.getIntExtra(SUBSCRIPTION_KEY, -1);
		if (sub == -1) {
			sub = intent.getIntExtra(Constants.EXTRA_SLOT_ID,
					SubscriptionManager.getDefaultVoicePhoneId());
		}
		return sub;
	}

	private static final boolean DBG = true;

	public static PhoneAccountHandle getPhoneAccountById(Context context,
			String id) {
		if (!TextUtils.isEmpty(id)) {
			final TelecomManager telecomManager = (TelecomManager) context
					.getSystemService(Context.TELECOM_SERVICE);
			final List<PhoneAccountHandle> accounts = telecomManager
					.getCallCapablePhoneAccounts();
			for (PhoneAccountHandle account : accounts) {
				if (id.equals(account.getId())) {
					return account;
				}
			}
		}
		return null;
	}

	public static int getSubIdbySlot(Context ctx, int slot) {
		SubscriptionManager mSubscriptionManager = SubscriptionManager
				.from(ctx);
		return mSubscriptionManager.getSubIdUsingPhoneId(slot);
	}

}