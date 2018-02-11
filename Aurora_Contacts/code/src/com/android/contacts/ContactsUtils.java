/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.contacts;

import aurora.app.AuroraProgressDialog;

import com.android.contacts.calllog.PhoneNumberHelper;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.AccountWithDataSet;
import com.android.contacts.test.NeededForTesting;
import com.android.contacts.util.Constants;
import com.android.contacts.util.IntentFactory;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.dialpad.IDialerSearchController.GnDialerSearchResultColumns;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.mediatek.contacts.util.OperatorUtils;

import android.os.IBinder;
import android.os.ServiceManager;

import com.android.internal.telephony.ITelephony;

import android.provider.Settings.System;
//Gionee:huangzy 20120628 modify for CR00627793 start
import android.provider.Telephony.Mms;
//Gionee:huangzy 20120628 modify for CR00627793 end

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.CountryDetector;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.CallLog;
import gionee.provider.GnCallLog.Calls;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.CommonDataKinds.Note;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.CommonDataKinds.GroupMembership;
import gionee.provider.GnContactsContract.CommonDataKinds.Im;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import gionee.provider.GnContactsContract.Groups;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

//Gionee:wangth 20120409 add for CR00561993 begin
import android.os.SystemProperties;
import gionee.os.storage.GnStorageManager; //import android.os.storage.StorageManager;
import gionee.provider.GnContactsContract.RawContacts;
//Gionee:wangth 20120409 add for CR00561993 end
import gionee.provider.GnTelephony.SIMInfo;
import gionee.telephony.AuroraTelephoneManager;

public class ContactsUtils {
    private static final String TAG = "ContactsUtils";
    private static final String WAIT_SYMBOL_AS_STRING = String.valueOf(PhoneNumberUtils.WAIT);
    
    //Gionee:wangth 20120409 add for CR00561993 begin
    public static boolean mIsGnContactsSupport = true;//SystemProperties.get("ro.gn.contacts.support").equals("yes");
    //Gionee:wangth 20120409 add for CR00561993 end
    
    //Gionee:lihuafang 20120504 add for CR00588600 begin
    public static boolean mIsGnShowSlotSupport = SystemProperties.get("ro.gn.operator.showslot").equals("yes");
    public static boolean mIsGnShowDigitalSlotSupport = SystemProperties.get("ro.gn.operator.showdigitalslot").equals("yes");
    //Gionee:lihuafang 20120504 add for CR00588600 end
    // Gionee baorui 2012-07-05 add for CR00637566 begin
    public static boolean mIsGnHapticVibratorSupport = SystemProperties.get("ro.gn.haptic.vibrator.prop").equals("yes");
    // Gionee baorui 2012-07-05 add for CR00637566 end
    
    // Gionee:wangth 20121113 add for CR00729290 begin
    // maybe modify in the future
    public static boolean mIsSuperSDCardVersionSupport = SystemProperties.get("ro.gn.cell.conn.platform.4.1").equals("yes");
    // Gionee:wangth 20121113 add for CR00729290 end
    
    public static boolean mIsIUNIDeviceOnly = SystemProperties.get("ro.product.brand").equals("IUNI");
    
    //aurora <wangth> <2013-9-16> add for auroro ui begin
    public static final int CONTACT_LIST_HEADER_PADDING_BOTTOM = 0;
    //aurora <wangth> <2013-9-16> add for auroro ui end
    
    public static int mListHeaderTop = 0;
    public static int mListHeaderY = 0;
    public static final int mContactHeaderHight = ContactsApplication.getInstance().getResources()
    		.getDimensionPixelOffset(R.dimen.aurora_edit_group_margin_top);

    // TODO find a proper place for the canonical version of these
    public interface ProviderNames {
        String YAHOO = "Yahoo";
        String GTALK = "GTalk";
        String MSN = "MSN";
        String ICQ = "ICQ";
        String AIM = "AIM";
        String XMPP = "XMPP";
        String JABBER = "JABBER";
        String SKYPE = "SKYPE";
        String QQ = "QQ";
    }

    /**
     * This looks up the provider name defined in
     * ProviderNames from the predefined IM protocol id.
     * This is used for interacting with the IM application.
     *
     * @param protocol the protocol ID
     * @return the provider name the IM app uses for the given protocol, or null if no
     * provider is defined for the given protocol
     * @hide
     */
    public static String lookupProviderNameFromId(int protocol) {
        switch (protocol) {
            case Im.PROTOCOL_GOOGLE_TALK:
                return ProviderNames.GTALK;
            case Im.PROTOCOL_AIM:
                return ProviderNames.AIM;
            case Im.PROTOCOL_MSN:
                return ProviderNames.MSN;
            case Im.PROTOCOL_YAHOO:
                return ProviderNames.YAHOO;
            case Im.PROTOCOL_ICQ:
                return ProviderNames.ICQ;
            case Im.PROTOCOL_JABBER:
                return ProviderNames.JABBER;
            case Im.PROTOCOL_SKYPE:
                return ProviderNames.SKYPE;
            case Im.PROTOCOL_QQ:
                return ProviderNames.QQ;
        }
        return null;
    }

    /**
     * Test if the given {@link CharSequence} contains any graphic characters,
     * first checking {@link TextUtils#isEmpty(CharSequence)} to handle null.
     */
    public static boolean isGraphic(CharSequence str) {
        return !TextUtils.isEmpty(str) && TextUtils.isGraphic(str);
    }

    /**
     * Returns true if two objects are considered equal.  Two null references are equal here.
     */
    @NeededForTesting
    public static boolean areObjectsEqual(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    /**
     * Returns true if two data with mimetypes which represent values in contact entries are
     * considered equal for collapsing in the GUI. For caller-id, use
     * {@link PhoneNumberUtils#compare(Context, String, String)} instead
     */
    public static final boolean shouldCollapse(CharSequence mimetype1, CharSequence data1,
            CharSequence mimetype2, CharSequence data2) {
        // different mimetypes? don't collapse
        if (!TextUtils.equals(mimetype1, mimetype2)) return false;

        // exact same string? good, bail out early
        if (TextUtils.equals(data1, data2)) return true;

        // so if either is null, these two must be different
        if (data1 == null || data2 == null) return false;

        // if this is not about phone numbers, we know this is not a match (of course, some
        // mimetypes could have more sophisticated matching is the future, e.g. addresses)
        if (!TextUtils.equals(Phone.CONTENT_ITEM_TYPE, mimetype1)) return false;

        // Now do the full phone number thing. split into parts, seperated by waiting symbol
        // and compare them individually
        final String[] dataParts1 = data1.toString().split(WAIT_SYMBOL_AS_STRING);
        final String[] dataParts2 = data2.toString().split(WAIT_SYMBOL_AS_STRING);
        if (dataParts1.length != dataParts2.length) return false;
        final PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        for (int i = 0; i < dataParts1.length; i++) {
            final String dataPart1 = dataParts1[i];
            final String dataPart2 = dataParts2[i];

            // substrings equal? shortcut, don't parse
            if (TextUtils.equals(dataPart1, dataPart2)) continue;

            // do a full parse of the numbers
            switch (util.isNumberMatch(dataPart1, dataPart2)) {
                case NOT_A_NUMBER:
                    // don't understand the numbers? let's play it safe
                    return false;
                case NO_MATCH:
                    return false;
                case EXACT_MATCH:
                case SHORT_NSN_MATCH:
                case NSN_MATCH:
                    break;
                default:
                    throw new IllegalStateException("Unknown result value from phone number " +
                            "library");
            }
        }
        return true;
    }

    /**
     * Returns true if two {@link Intent}s are both null, or have the same action.
     */
    public static final boolean areIntentActionEqual(Intent a, Intent b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return TextUtils.equals(a.getAction(), b.getAction());
    }

    /**
     * @return The ISO 3166-1 two letters country code of the country the user
     *         is in.
     */
    public static final String getCurrentCountryIso(Context context) {
        CountryDetector detector =
                (CountryDetector) context.getSystemService(Context.COUNTRY_DETECTOR);
        return detector.detectCountry().getCountryIso();
    }

    public static boolean areContactWritableAccountsAvailable(Context context) {
        final List<AccountWithDataSet> accounts =
                AccountTypeManager.getInstance(context).getAccounts(true /* writeable */);
        return !accounts.isEmpty();
    }

    public static boolean areGroupWritableAccountsAvailable(Context context) {
        final List<AccountWithDataSet> accounts =
                AccountTypeManager.getInstance(context).getGroupWritableAccounts();
        return !accounts.isEmpty();
    }

    /**
     * Returns the intent to launch for the given invitable account type and contact lookup URI.
     * This will return null if the account type is not invitable (i.e. there is no
     * {@link AccountType#getInviteContactActivityClassName()} or
     * {@link AccountType#resPackageName}).
     */
    public static Intent getInvitableIntent(AccountType accountType, Uri lookupUri) {
        String resPackageName = accountType.resPackageName;
        String className = accountType.getInviteContactActivityClassName();
        if (TextUtils.isEmpty(resPackageName) || TextUtils.isEmpty(className)) {
            return null;
        }
        Intent intent = new Intent();
        intent.setClassName(resPackageName, className);

        intent.setAction(GnContactsContract.Intents.INVITE_CONTACT);

        // Data is the lookup URI.
        intent.setData(lookupUri);
        return intent;
    }

    /**
     * Returns a header view based on the R.layout.list_separator, where the
     * containing {@link TextView} is set using the given textResourceId.
     */
    public static View createHeaderView(Context context, int textResourceId) {
        // gionee xuhz 20120607 modify start
        View view;
        if (ContactsApplication.sIsGnContactsSupport) {
            view = View.inflate(context, R.layout.gn_list_separator, null);
        } else {
            view = View.inflate(context, R.layout.list_separator, null);
        }
        // gionee xuhz 20120607 modify end

        TextView textView = (TextView) view.findViewById(R.id.title);
        if (textResourceId > 0) {
        	textView.setText(context.getString(textResourceId));
        }
        // gionee xuhz 20120606 add start
        if (ContactsApplication.sIsGnContactsSupport) {            
            textView.setTextColor(ResConstant.sHeaderTextColor);
        }
        // gionee xuhz 20120606 add end
        return view;
    }

    /**
     * Returns the {@link Rect} with left, top, right, and bottom coordinates
     * that are equivalent to the given {@link View}'s bounds. This is equivalent to how the
     * target {@link Rect} is calculated in {@link QuickContact#showQuickContact}.
     */
    public static Rect getTargetRectFromView(Context context, View view) {
        final float appScale = context.getResources().getCompatibilityInfo().applicationScale;
        final int[] pos = new int[2];
        view.getLocationOnScreen(pos);

        final Rect rect = new Rect();
        rect.left = (int) (pos[0] * appScale + 0.5f);
        rect.top = (int) (pos[1] * appScale + 0.5f);
        rect.right = (int) ((pos[0] + view.getWidth()) * appScale + 0.5f);
        rect.bottom = (int) ((pos[1] + view.getHeight()) * appScale + 0.5f);
        return rect;
    }

    // The following lines are provided and maintained by Mediatek Inc.
    // empty sim id
    public static final int CALL_TYPE_NONE = 0;
    // sim id of sip call in the call log database
    public static final int CALL_TYPE_SIP = -2;
    public static boolean[] isServiceRunning = {false, false};

    private static String sCurrentCountryIso;

    /**
     * this method is used only in the onCreate of DialtactsActivity
     * @return
     */
    public static final String getCurrentCountryIso() {
        if(sCurrentCountryIso == null) {
            CountryDetector detector =
                (CountryDetector) ContactsApplication.getInstance().getSystemService(Context.COUNTRY_DETECTOR);
            sCurrentCountryIso = detector.detectCountry().getCountryIso();
        }

        return sCurrentCountryIso;
    }

    // The previous lines are provided and maintained by Mediatek Inc.
    
    //Gionee:huangzy 20120628 modify for CR00632576 start
    public static String trimAllSpace(String str) {
        if(!TextUtils.isEmpty(str)) {
            str = str.replace(" ", "");
        }
        return str;
    }
    
    public static String trimAllSpace(String str, String space) {
        if(!TextUtils.isEmpty(str)) {
            str = str.replace(space, "");
        }
        return str;
    }
    //Gionee:huangzy 20120628 modify for CR00632576 end 
    
    public static int getContactsCount(ContentResolver cr) {
    	if (null == cr) {
    		return -1;
    	}
    	
    	Cursor c = cr.query(GnContactsContract.RawContacts.CONTENT_URI, new String[]{Contacts._ID}, 
    			GnContactsContract.RawContacts.DELETED + "=0", null, null);
    	if (null == c) {
    		return -1;
    	}
    	
    	int count = c.getCount();
    	c.close();
    	
    	return count;
    }
    
    //Gionee:huangzy 20120905 remove for CR00687176 start
    /*private static int sGroupMimetypeId = 0;
    public static int getGroupMimetypeId(ContentResolver cr) {
    	if (0 != sGroupMimetypeId) {
    		return sGroupMimetypeId;
    	}
    	
    	Uri uri = Uri.parse("content://com.android.contacts/mimetypes/id");
    	Cursor c = cr.query(uri, new String[]{Data._ID}, 
    			Data.MIMETYPE + "='" + GroupMembership.CONTENT_ITEM_TYPE + "'",
    			null, null);
    	
    	if (null != c) {
    		if (c.moveToFirst()) {
    			sGroupMimetypeId = c.getInt(0);    			
    		}
    		c.close();
    	}
    	
    	return sGroupMimetypeId;
    }*/
    //Gionee:huangzy 20120905 remove for CR00687176 end

    public static int clearFrequentContacted(ContentResolver cr) {
    	//Gionee:huangzy 20101019 remove for CR00715333 start
    	/*Uri uri = Uri.parse("content://com.android.contacts/data_usage_stat/clear");
    	return cr.delete(uri, null, null);*/
    	//Gionee:huangzy 20101019 remove for CR00715333 end
    	return cr.delete(Uri.withAppendedPath(GnContactsContract.Contacts.CONTENT_FREQUENT_URI, "clear"),
    			null, null);
    }
    
    //Gionee:huangzy 20101019 remove for CR00715333 start
    /*//Gionee:huangzy 20100717 add for CR00650878 start
    public static boolean gnHasFrequentContacted(ContentResolver cr) {
        Uri uri = Uri.parse("content://com.android.contacts/data_usage_stat");
        Cursor c = cr.query(uri, new String[]{"stat_id"}, null, null, "stat_id limit 1");
        if (null != c) {
            boolean has = (c.getCount() > 0);
            c.close();
            return has;
        }
        
        return true;
    }
    //Gionee:huangzy 20100717 add for CR00650878 end*/    
    //Gionee:huangzy 20101019 remove for CR00715333 end
    //Gionee:huangzy 20101019 add for CR00715333 start
    public static Bitmap getThumbnailBitmap(InputStream is, float aimWidth,
			float aimHeight)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeStream(is, null, options);

		int issW = (0 == aimWidth ? 1 : (int) (options.outWidth / aimWidth));
		int issH = (0 == aimHeight ? 1: (int) (options.outHeight / aimHeight));
		int iss = Math.max(issW, issH);
		iss = Math.max(1, iss);
		options.inSampleSize = iss;
		options.inJustDecodeBounds = false;
		/*options.inPreferredConfig = Bitmap.Config.RGB_565;*/
		Log.i("James", "options.inSampleSize   " + options.inSampleSize);

		return BitmapFactory.decodeStream(is, null, options);
	}
    
    public static Bitmap getThumbnailBitmapV2(InputStream is, float aimWidth,
			float aimHeight)
	{
    	if (null == is) {
    		return null;
    	}
    	
    	byte[] byteArray = null;
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	byte[] buffer = new byte[16384]; /*1024*16*/

        int size;
        try {
        	while ((size = is.read(buffer)) != -1) {
                baos.write(buffer, 0, size);
            }
		} catch (IOException e) {
		}
        
        byteArray = baos.toByteArray();
        if (null == byteArray || byteArray.length == 0) {
        	return null;
        }
    	
    	BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);

		int issW = (0 == aimWidth ? 1 : (int) (options.outWidth / aimWidth));
		int issH = (0 == aimHeight ? 1: (int) (options.outHeight / aimHeight));
		int iss = Math.max(issW, issH);
		iss = Math.max(1, iss);
		options.inSampleSize = iss;
    	
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
	}
    
    //Gionee:huangzy 20101019 add for CR00715333 end
    
    public static Intent getEditNumberBeforeCallIntent(String number) {
    	return new Intent(Intent.ACTION_DIAL, PhoneNumberHelper.getCallUri(number));
    }
    
    public static void goGeminiSimManagement(Context context) {
		Intent simManIntent = new Intent("android.settings.GEMINI_MANAGEMENT");
		// Gionee:wangth 20130328 modify for CR00790712 begin
		/*
		context.startActivity(simManIntent);
		*/
		if (GNContactsUtils.isMultiSimEnabled()) {
            simManIntent = new Intent();
            simManIntent.setClassName("com.android.settings", "com.android.settings.multisimsettings.MultiSimSettings");
        }
		
		try {
		    context.startActivity(simManIntent);
		} catch (ActivityNotFoundException anfe) {
		    anfe.printStackTrace();
		}
		// Gionee:wangth 20130328 modify for CR00790712 end
    }
    
    public static void goIpCallSetting(Context context) {
		Intent ip_Call_Intent = new Intent("gn.com.android.phone.action.ip_call_setting");
		context.startActivity(ip_Call_Intent);
    }
    
    public static boolean phoneIsCdma() {
        boolean isCdma = false;
        try {
        	ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) {
                isCdma = (phone.getActivePhoneType() == TelephonyManager.PHONE_TYPE_CDMA);
            }
        } catch (RemoteException e) {
            Log.w(TAG, "phone.getActivePhoneType() failed", e);
        }
        return isCdma;
    }
    
    public static boolean phoneIsOffhook() {
        boolean phoneOffhook = false;
        try {
        	ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) phoneOffhook = phone.isOffhook();
        } catch (RemoteException e) {
            Log.w(TAG, "phone.isOffhook() failed", e);
        }
        return phoneOffhook;
    }
    
    //Gionee <huangzy> <2013-05-13> modify for CR00810406 begin
    public static boolean isIntentAvailable(Context context, String action) {  
        final PackageManager packageManager = context.getPackageManager();  
        final Intent intent = new Intent(action);  
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,  
                        PackageManager.MATCH_DEFAULT_ONLY);
        return null != list && list.size() > 0;
    }
    //Gionee <huangzy> <2013-05-13> modify for CR00810406 end
    
    public static boolean isPackageExist(String packageName) {
        return null != getPackageInfo(packageName);
    }
    
    public static PackageInfo getPackageInfo(String packageName) {
    	PackageInfo packageInfo = null;
        try { 
            packageInfo = ContactsApplication.getInstance().getPackageManager().getPackageInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "getPackageInfo()   " + packageName + " is not found!");
        }
        return packageInfo;
    }
    
    public static boolean isGnExchangeExist() {
    	return ContactsApplication.sIsGioneeCloudSpport &&
    			isPackageExist("gn.com.android.synchronizer");
    }
    
    // gionee xuhz 20120618 add for CR00624070 start
    public static int gnGetMissedCallCount(ContentResolver cr) {
    	if (null == cr) {
    		return -1;
    	}
    	
    	Cursor c = cr.query(CallLog.Calls.CONTENT_URI, null, 
    			CallLog.Calls.NEW + "='" + 1 +"'"
                + " AND " + CallLog.Calls.TYPE + "='" + CallLog.Calls.MISSED_TYPE + "'", null, null);
    	if (null == c) {
    		return -1;
    	}
    	
    	int count = c.getCount();
    	c.close();
    	
    	return count;
    }
    // gionee xuhz 20120618 add for CR00624070 end
    
    //Gionee:huangzy 20120704 add for CR00633431 start
    public static Uri gnLookupUri2ContactUri(Context context, Uri lookupUri) {
        if (null == lookupUri) {
            return null;
        }
        
        Uri contactUri = lookupUri;
        //Gionee:huangzy 20120710 modify for CR00637485 start
        /*Cursor cursor = context.getContentResolver().query(lookupUri, new String[]{"_id", "name_raw_contact_id"},
                null, null, null);*/
        Cursor cursor = context.getContentResolver().query(lookupUri, new String[]{"_id"}, null, null, null);        
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                int rawContactId = cursor.getInt(0);
                contactUri = Uri.withAppendedPath(GnContactsContract.Contacts.CONTENT_URI, rawContactId + "");
            }
            cursor.close();
        }
        //Gionee:huangzy 20120710 modify for CR00637485 end
        
        return contactUri;
    }
    //Gionee:huangzy 20120704 add for CR00633431 end

    public static Intent getCallSettingsIntent() {
        if (GNContactsUtils.isOnlyQcContactsSupport()) {
            return GNContactsUtils.getCallSettingsIntent();
        }
        
        final String PHONE_PACKAGE = "com.android.phone";
        final String CALL_SETTINGS_CLASS_NAME = "com.android.phone.CallSettings";
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName(PHONE_PACKAGE, CALL_SETTINGS_CLASS_NAME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }
    
    /**
     * @return true if the phone is "in use", meaning that at least one line
     *              is active (ie. off hook or ringing or dialing).
     */
    public static boolean phoneIsInUse() {
        boolean phoneInUse = false;
        try {
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) phoneInUse = !phone.isIdle();
        } catch (RemoteException e) {
            Log.w(TAG, "phone.isIdle() failed", e);
        }
        return phoneInUse;
    }
    
    // Gionee:wangth 20120710 add for CR00633799 begin
    public static long queryForRawContactId(ContentResolver cr, long contactId) {
        Cursor rawContactIdCursor = null;
        long rawContactId = -1;
        
        try {
            rawContactIdCursor = cr.query(RawContacts.CONTENT_URI,
                    new String[] {RawContacts._ID},
                    RawContacts.CONTACT_ID + "=" + contactId + " and is_privacy > -1", null, null);
            if (rawContactIdCursor != null && rawContactIdCursor.moveToFirst()) {
                // Just return the first one.
                rawContactId = rawContactIdCursor.getLong(0);
            }
        } finally {
            if (rawContactIdCursor != null) {
                rawContactIdCursor.close();
            }
        }
        return rawContactId;
    }
    // Gionee:wangth 20120710 add for CR00633799 end
    
    //Gionee:huangzy 20120710 add for CR00624360 start
    public static boolean gnHasStaredPeople(Context context) {
        if (null == context) {
            return true;
        }
        
        String selection = RawContacts.STARRED + "=1 And " + RawContacts.DELETED + "=0";
        Cursor c = context.getContentResolver().query(RawContacts.CONTENT_URI, new String[]{"_id"}, selection, null, "_id Limit 1");        
        if (null == c) {
            return false;
        }
        
        int count = c.getCount();
        c.close();
        
        return count > 0;
    }
    //Gionee:huangzy 20120710 add for CR00624360 end
    
    public static boolean gnHasCallLog(Context context) {
        if (null == context) {
            return true;
        }
        String selection = null;
        Cursor c = context.getContentResolver().query(Calls.CONTENT_URI, new String[]{"_id"}, selection, null, "_id Limit 1");        
        if (null == c) {
            return false;
        }
        
        int count = c.getCount();
        c.close();
        
        return count > 0;
    }
    
    //Gionee:huangzy 20120809 add for CR00671961 start
    public static String getAvailableSdcard(Context context, long request) {
//        StorageManager storageManager = (StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
        // aurora ukiliu 2013-11-12 modify for aurora ui begin
        return GnStorageManager.getInstance(ContactsApplication.getInstance()).getGnAvailableExternalStoragePath(request);
        // aurora ukiliu 2013-11-12 modify for aurora ui end
    }
    //Gionee:huangzy 20120809 add for CR00671961 end
    
    //Gionee:huangzy 20120815 modify for CR00664104 start
    public static String getTopActivityName(Context context) {
    	ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    	ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
    	return cn.getShortClassName();
    }
    //Gionee:huangzy 20120815 modify for CR00664104 end

    //Gionee <huangzy> <2013-05-13> add for CR00797633 begin
    public static boolean isTopActivity(String activityName) {
    	if (TextUtils.isEmpty(activityName)) {
    		throw new IllegalArgumentException();
    	}
    	
    	Context context = ContactsApplication.getInstance();
    	ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    	ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
    	return cn.getClassName().equals(activityName);
    }
    //Gionee <huangzy> <2013-05-13> add for CR00797633 end
    
    //Gionee:huangzy 20120823 add for CR00614805 start    
    public static void printColumnName(Cursor cursor) {
    	if (null != cursor) {
    		int columnCount = cursor.getColumnCount();
    		Log.i("James", "start -- columnName");
    		for (int i = 0; i < columnCount; ++i) {
    			Log.i("James", cursor.getColumnName(i));
    		}
    		Log.i("James", "end -- columnName");
    	}
    }
    //Gionee:huangzy 20120823 add for CR00614805 end

	// gionee xuhz 20120906 modify for CR00687581 start
	public static String trimNumberToAdd(String str) {
		String strTrim = trimWaitAndPause(str);
		strTrim = trimStarAndPoundAndPlus(strTrim);
        return strTrim;
	}
	
	public static String trimWaitAndPause(String str) {
        if(!TextUtils.isEmpty(str)) {
            str = str.replace(",", "");
            str = str.replace(";", "");
        }
        return str;
	}
	
	public static String trimStarAndPoundAndPlus(String str) {
        if(!TextUtils.isEmpty(str)) {
            str = str.replace("*", "");
            str = str.replace("#", "");
            str = str.replace("+", "");
        }
        return str;
	}
    // gionee xuhz 20120906 modify for CR00687581 end
	
    // Gionee:wangth 20121113 add for CR00729290 begin
    public static String getRealSdPath(int num) {
        if (mIsSuperSDCardVersionSupport || GNContactsUtils.isOnlyQcContactsSupport()) { // platform 4.1
            if (num == 1) {
                return "/storage/sdcard0";
            } else if (num == 2){
                return "/storage/sdcard1";
            }
        } else { // platform 4.0
            if (num == 1) {
                return "/mnt/sdcard";
            } else if (num == 2){
                return "/mnt/sdcard2";
            }
        }
        
        return null;
    }
    // Gionee:wangth 20121113 add for CR00729290 end
    
    public static Bitmap getRoundImageDrawable(Context context, Bitmap bitmap) {
    	return getRoundImageDrawable(context, bitmap, 5);
    }
    
    public static Bitmap getRoundImageDrawable(Context context, Bitmap bitmap, int roundPixels) {
    	if (null == bitmap) {
    		return bitmap;
    	}
    	
        float scale = context.getResources().getDisplayMetrics().density;
        Rect rect = new Rect(0, 0, (int)(bitmap.getWidth() * scale), (int)(bitmap.getHeight() * scale));
        RectF rectF = new RectF(rect);
        
        Bitmap roundConcerImage = Bitmap.createBitmap(
                (int)(rect.width() * scale), (int)(rect.height() * scale), Config.ARGB_8888);
        Canvas canvas = new Canvas(roundConcerImage);
        
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        
        canvas.drawRoundRect(rectF, roundPixels, roundPixels, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, null, rect, paint);
        
        return roundConcerImage;
    }

    //Gionee <huangzy> <2013-04-26> add for CR00795330 begin
    public static void broadcastUserProfileUpdated() {
    	Intent profileUpdated = new Intent("com.android.contacts.profileUpdate");
		ContactsApplication.getInstance().sendOrderedBroadcast(profileUpdated, null);
		Log.i("James", "sendOrderedBroadcast  com.android.contacts.profileUpdate");
    }
    //Gionee <huangzy> <2013-04-26> add for CR00795330 end
    
    //Gionee <huangzy> <2013-04-10> add for CR00813031 begin
    public static void hide(IBinder windowToken) {
    	InputMethodManager imm = 
    			(InputMethodManager)ContactsApplication.getInstance().
    			getSystemService(Context.INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(windowToken, 0);
    }
    //Gionee <huangzy> <2013-04-10> add for CR00813031 end
    
    // aurora <wangth> <2013-9-2> add for auroro ui begin 
    public static int getStarredCount(Context context) {
        Cursor cursor = context.getContentResolver().query(Contacts.CONTENT_URI, null,
                Contacts.STARRED + "!=0 AND " + Contacts.IN_VISIBLE_GROUP + "=1", null, null);
        
        if (cursor == null) {
            return 0;
        } else {
            int result = cursor.getCount();
            cursor.close();
            return result;
        }
    }
    
    public static int getGroupsCount(Context context) {
        Cursor cursor = context.getContentResolver().query(Groups.CONTENT_URI, null,
                null, null, null);
        
        if (cursor == null) {
            return 0;
        } else {
            int result = cursor.getCount();
            cursor.close();
            return result;
        }
    }
    
    public static int getNoGroupsCount(Context context) {
        String re = "";
        String noteMimetypeId;
        String phoneMimetypeId;
        int groupPeopleCount = 0;
        int allPeopleCount = 0;
        
        Cursor groupsPeopleCountCursor = context.getContentResolver().query(
                Data.CONTENT_URI, 
                new String[]{Data.RAW_CONTACT_ID},
                Data.MIMETYPE + " = '" + GroupMembership.CONTENT_ITEM_TYPE + "'", 
                null, 
                Data.RAW_CONTACT_ID);
        
        Cursor allPeopleCountCursor = context.getContentResolver().query(
                RawContacts.CONTENT_URI, 
                new String[]{RawContacts._ID},
                "index_in_sim = -1 and deleted = 0", 
                null, 
                null);
        
        if (groupsPeopleCountCursor != null) {
            if (groupsPeopleCountCursor.moveToFirst()) {
                String pre = groupsPeopleCountCursor.getString(0);
                groupPeopleCount++;
                String cur = null;
                
                for (int i = 0, j = groupsPeopleCountCursor.getCount(); i < j; i++)
                if (groupsPeopleCountCursor.moveToNext()) {
                    cur = groupsPeopleCountCursor.getString(0);
                    if (!cur.equals(pre)) {
                        groupPeopleCount++;
                        pre = cur;
                    }
                }
                
            }
            groupsPeopleCountCursor.close();
        }
        
        if (allPeopleCountCursor != null) {
            allPeopleCount = allPeopleCountCursor.getCount();
            allPeopleCountCursor.close();
        }
        
        return (allPeopleCount - groupPeopleCount);
    }
    
    public static String getNote(Context context, String number) {
        String re = "";
        String noteMimetypeId;
        String phoneMimetypeId;
        
        Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, new String[]{Data.RAW_CONTACT_ID},
                Data.MIMETYPE + " = '" + Phone.CONTENT_ITEM_TYPE + "' AND " + Data.DATA1 + " = " + number, null, null);
        
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String id = cursor.getString(0);
                
                Cursor c = context.getContentResolver().query(Data.CONTENT_URI, new String[]{Data.DATA1},
                        Data.MIMETYPE + " = '" + Note.CONTENT_ITEM_TYPE + "' AND " + Data.RAW_CONTACT_ID + " = " + id, null, null);
                
                if (c != null) {
                    if (c.moveToFirst()) {
                        re = c.getString(0);
                    }
                    
                    c.close();
                }
            }
            cursor.close();
        }
        
        return re;
    }

    // aurora <ukiliu> <2013-10-01> add for aurora ui begin
    public static String getNote(Context context, long rawContactId) {
        String re = "";
        Cursor c = context.getContentResolver().query(Data.CONTENT_URI, new String[]{Data.DATA1},
                Data.MIMETYPE + " = '" + Note.CONTENT_ITEM_TYPE + "' AND " + Data.RAW_CONTACT_ID + " = " + rawContactId, null, null);

        if (c != null) {
            if (c.moveToFirst()) {
                re = c.getString(0);
            }
            c.close();
        }
        return re;
    }
    // aurora <ukiliu> <2013-10-01> add for aurora ui end

    public static boolean checkName(final Context context, String name) {
//        if (name.contains("/") || name.contains("%")) {
//            return false;
//        }

        String tempName = null;
        boolean nameExists = false;
        // check group name in DB
        if (!nameExists) {
            Cursor cursor = context.getContentResolver().query(
                    Groups.CONTENT_SUMMARY_URI,
                    new String[] { Groups._ID },
                    Groups.TITLE + "=? AND " + Groups.ACCOUNT_NAME + " =? AND "
                            + Groups.ACCOUNT_TYPE + "=? AND " + Groups.DELETED
                            + "=0",
                    new String[] { name, AccountType.ACCOUNT_NAME_LOCAL_PHONE,
                            AccountType.ACCOUNT_TYPE_LOCAL_PHONE }, null);
            if (cursor == null || cursor.getCount() == 0) {
                if (cursor != null) {
                    cursor.close();
                }
            } else {
                cursor.close();
                nameExists = true;
            }
        }
        
        if (nameExists) {
            return false;
        } else {
            return true;
        }
    }
    
    public static int getRawContactId(Context context, int contactId) {
        int rawContactId = -1;
        
        Cursor c = context.getContentResolver().query(
                RawContacts.CONTENT_URI, 
                new String[]{RawContacts._ID}, 
                GnContactsContract.RawContacts.DELETED + "=0 and " + RawContacts.CONTACT_ID + " = " +  contactId, 
                null, 
                null);
        
        if (c != null) {
            if (c.moveToFirst()) {
                rawContactId = c.getInt(0);
            }
            c.close();
        }
        
        return rawContactId;
    }
    
    public static int getRawContactIdFromDataId(Context context, long dataId) {
        int rawContactId = -1;
        
        Cursor c = context.getContentResolver().query(
                Data.CONTENT_URI, 
                new String[]{Data.RAW_CONTACT_ID}, 
                Data._ID + " = " +  dataId, 
                null, 
                null);
        
        if (c != null) {
            if (c.moveToFirst()) {
                rawContactId = c.getInt(0);
            }
            c.close();
        }
        
        return rawContactId;
    }
    
    public static int getAuroraContactsCount(ContentResolver cr) {
        if (null == cr) {
            return -1;
        }
        
        String selection = RawContacts.INDICATE_PHONE_SIM + "<0 and " 
                + GnContactsContract.RawContacts.DELETED + "=0";
        Cursor c = cr.query(
                GnContactsContract.RawContacts.CONTENT_URI, 
                new String[]{Contacts._ID},
                selection, 
                null, null);
        
        if (null == c) {
            return -1;
        }
        
        int count = c.getCount();
        c.close();
        
        return count;
    }
    
    public static class AuroraContactsProgressDialog extends AuroraProgressDialog {
        public AuroraContactsProgressDialog(Context context, int style) {
            super(context);
        }
        
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: {
                if (ContactsUtils.mIsGnContactsSupport) {
                    return true;
                }
                break;
            }
            }
            
            return super.onKeyDown(keyCode, event);
        }
    };
    
    public static int queryForRawContactId(ContentResolver cr, int contactId) {
        Cursor rawContactIdCursor = null;
        int rawContactId = -1;
        
        try {
            rawContactIdCursor = cr.query(RawContacts.CONTENT_URI,
                    new String[] {RawContacts._ID},
                    RawContacts.CONTACT_ID + "=" + contactId, null, null);
            if (rawContactIdCursor != null && rawContactIdCursor.moveToFirst()) {
                // Just return the first one.
                rawContactId = rawContactIdCursor.getInt(0);
            }
        } finally {
            if (rawContactIdCursor != null) {
                rawContactIdCursor.close();
            }
        }
        return rawContactId;
    }
    
    public static void aurorahighlightName(TextView tv, Cursor cursor) {
        int spanColorBg = ContactsApplication.getInstance().getColor(R.color.aurora_hightlight_contact_text_color);
        String name = cursor.getString(GnDialerSearchResultColumns.NAME_INDEX);
        String pinyin = cursor.getString(GnDialerSearchResultColumns.PINYIN_INDEX);
        
        if (TextUtils.isEmpty(name)) {
            String number = cursor.getString(GnDialerSearchResultColumns.PHONE_NUMBER_INDEX);
            if (!TextUtils.isEmpty(number)) {
                tv.setText(number);
                return;
            }
            
            tv.setText(null);
            return;
        }
        
        if (!TextUtils.isEmpty(pinyin)) {
            String pinyinHighlight = cursor.getString(GnDialerSearchResultColumns.PINYIN_HIGHLIGHT_INDEX);
            if (pinyinHighlight != null) {
                SpannableStringBuilder namestyle = new SpannableStringBuilder(name);
                SpannableStringBuilder pinyinstyle = new SpannableStringBuilder(pinyin);
//                Log.d(TAG, "name = " + name + "   pinyin = " + pinyin);
                if (!name.equals(pinyin)) {
                    ArrayList<Integer> nameToken = new ArrayList<Integer>();
                    int nameIndex = 0;
                    int pinyinIndex = 0;
                    int pinyinLength = pinyin.length();
                    for (int i = 0; i < pinyinLength; i++) {
                        char c = pinyin.charAt(i);
                        if (0 == i && c != name.charAt(nameIndex)) {
                            pinyinIndex++;
                            continue;
                        } else if (i > 0 && (nameIndex + 1) < name.length() &&
                                c != name.charAt(nameIndex + 1) && ('a' <= c && c <= 'z')) {
                            pinyinIndex++;
                            continue;
                        } else if (i > 0 && (nameIndex + 1) == name.length()) {
                            if (pinyinIndex < pinyinLength && ('A' <= c && c <= 'Z')) {
                                nameToken.add(pinyinIndex);
                                pinyinIndex++;
                                continue;
                            }
                            
                            pinyinIndex++;
                            if (pinyinIndex == pinyinLength) {
                                nameToken.add(pinyinIndex);
                                break;
                            }
                        } else {
                            if (pinyinIndex > 0) {
                                nameToken.add(pinyinIndex);
                            }
                            nameIndex++;
                            pinyinIndex++;
                        }
                    }
                    
                    if (nameToken.size() <= 0) {
                        tv.setText(name);
                        return;
                    }
                    
                    if (pinyin.charAt(pinyin.length() - 1) == name.charAt(name.length() - 1)) {
                        nameToken.add(nameToken.get(nameToken.size() - 1) + 1);
                    }
                    
//                    for (int i : nameToken) {
//                        Log.d(TAG, " = " + i);
//                    }
                    
                    for (int i = 0, len = pinyinHighlight.length(); i < len; i = i + 2) {
                        int start = (int) pinyinHighlight.charAt(i);
                        int end = (int) pinyinHighlight.charAt(i + 1);
                        int startIndex = 0;
                        int endIndex = 0;
//                        Log.d(TAG, "start = " + start + "  end = " + end);
                        if (start ==0 && end == 0) {
                            break;
                        }
                        
                        for (int temp = 0; temp < nameToken.size(); temp++) {
                            if (start == 0) {
                                startIndex = 0;
                            } else if (start == nameToken.get(temp)) {
                                startIndex = temp + 1;
                            }
                            
                            if (end > nameToken.get(nameToken.size() - 1)) {
                                endIndex = nameToken.size();
                            } else if (end == nameToken.get(temp)) {
                                endIndex = temp + 1;
                            } else if (end > nameToken.get(temp) && end < nameToken.get(temp + 1)) {
                                endIndex = temp + 2;
                            } else if (end <= nameToken.get(0)) {
                                endIndex = 1;
                            }
                        }
                        
//                        Log.d(TAG, "startIndex = " + startIndex + "  endIndex = " + endIndex + "   len = " + len);
                        
                        try {
                            namestyle.setSpan(new ForegroundColorSpan(spanColorBg), startIndex, endIndex,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    
                    tv.setText(namestyle);
                    return;
                }
                
                for (int i = 0, len = pinyinHighlight.length(); i < len; i = i + 2) {
                    int start = (int) pinyinHighlight.charAt(i);
                    int end = (int) pinyinHighlight.charAt(i + 1);
                    pinyinstyle.setSpan(new ForegroundColorSpan(spanColorBg), start, end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                
                tv.setText(pinyinstyle);
                return;
            }
        }
        
        tv.setText(name);
    }
    
    public static Toast mToast;
    public static void toastManager(Context context, int resId) {
        if (mToast == null) {
            mToast = Toast.makeText(context,
                    resId, Toast.LENGTH_SHORT);
        }/* else {
            mToast.cancel();
        }*/
        
        mToast.setText(resId);
        mToast.show();
    }
    // aurora <wangth> <2013-9-2> add for auroro ui end
    // aurora <ukiliu> <2013-11-12> add for auroro ui begin
    public static String getSdcardPath(Context mContext, int num) {
    	return GnStorageManager.getInstance(ContactsApplication.getInstance()).getSdCardPath(num);
	}
    // aurora <ukiliu> <2013-11-12> add for auroro ui end
    
    public static int getSimIcon(Context mContext, int simId) {
    	int result = -1;
    	int slot = 0;
//        SIMInfo simInfo = SIMInfo.getSIMInfoById(mContext, simId);
    	SIMInfo simInfo = SIMInfoWrapper.getDefault().getSimInfoById(simId);
        if (simInfo != null) {          
//        	result = color2resId(simInfo.mColor, false);
        	slot = simInfo.mSlot;
        }else{
        	result=R.drawable.record_default;
        }
       	if(result == -1) {    
        	if(slot == 1) {
        		result = R.drawable.sim2_icon;
	    	}else if(slot == 0){
	    		result = R.drawable.sim1_icon;
	    	}else {
	    		result = result=R.drawable.record_default;
	    	}	
    	}
        
    	return result;
    }
    
    public static int getSimBigIcon(Context mContext, int simId) {
        int result = -1;
        int slot = 0;
//        SIMInfo simInfo = SIMInfo.getSIMInfoById(mContext, simId);
        SIMInfo simInfo = SIMInfoWrapper.getDefault().getSimInfoById(simId);
        if (simInfo != null) {          
//            result = color2resId(simInfo.mColor, true);
            slot = simInfo.mSlot;
        }
        
        if(result == -1) {    
            if(slot == 1) {
                result = R.drawable.aurora_photo_sim2;
            } else {
                result = R.drawable.aurora_photo_sim1;
            }   
        }
        
        return result;
    }
    
    public static int color2resId(int color, boolean big) {
    	int result = -1 ;
    	if (big) {
    	    switch (color) {
            case 0: {
                result = R.drawable.aurora_photo_sim1;
                break;
            }
            case 1: {
                result = R.drawable.aurora_photo_sim2;
                break;
            }         
            case 2: {
                result = R.drawable.aurora_photo_net;
                break;
            }
            case 3: {
                result = R.drawable.aurora_photo_home;
                break;
            }
            case 4: {
                result = R.drawable.aurora_photo_work;
                break;
            }         
            case 5: {
                result = R.drawable.aurora_photo_phone;
                break;
            }
            }
    	} else {
    	    switch (color) {
            case 0: {
                result = R.drawable.sim1_icon;
                break;
            }
            case 1: {
                result = R.drawable.sim2_icon;
                break;
            }         
            case 2: {
                result = R.drawable.net_icon;
                break;
            }
            case 3: {
                result = R.drawable.home_icon;
                break;
            }
            case 4: {
                result = R.drawable.office_icon;
                break;
            }         
            case 5: {
                result = R.drawable.phone_icon;
                break;
            }
            }
    	}
    	
    	return result;
    }
    
    public static int getSimConvertIcon(Context mContext, int simId) {
    	int result = -1;
    	int slot = 0;
//        SIMInfo simInfo = SIMInfo.getSIMInfoById(mContext, simId);
    	SIMInfo simInfo = SIMInfoWrapper.getDefault().getSimInfoById(simId);
        if (simInfo != null) {          
//        	result = color2ConvertResId(simInfo.mColor);
        	slot = simInfo.mSlot;
        }else{
        	result=R.drawable.dial_default;
        }
       	if(result == -1) {    
        	if(slot == 1) {
        		result = R.drawable.sim22_icon;
	    	} else if(slot == 0) {
	    		result = R.drawable.sim11_icon;
	    	}else{
	    		result=R.drawable.dial_default;
	    	}
    	}
        
    	return result;
    }
    
    
    public static int color2ConvertResId(int color) {
    	int result = -1 ;
   
	    switch (color) {
	        case 0: {
	            result = R.drawable.sim1_icon_convert;
	            break;
	        }
	        case 1: {
	            result = R.drawable.sim2_icon_convert;
	            break;
	        }         
	        case 2: {
	            result = R.drawable.net_icon_convert;
	            break;
	        }
	        case 3: {
	            result = R.drawable.home_icon_convert;
	            break;
	        }
	        case 4: {
	            result = R.drawable.office_icon_convert;
	            break;
	        }         
	        case 5: {
	            result = R.drawable.phone_icon_convert;
	            break;
	        }
        
	    }
    	
    	return result;
    }
    
    
    public static String getSlotExtraKey() {
    	return "subscription";
    } 
    
    //还要加入判断ServiceState的判断才行，现在只是判断了卡插入的状态，双卡设置中关闭卡对这里无影响。需要在GnTelephonyManager加接口
    public static boolean isShowDoubleButton() {
    	 if(is7503()) {    		 
	   	 	 int dualSimModeSetting = System.getInt(ContactsApplication.getInstance().getContentResolver(), "msim_mode_setting", 3);
	   		 return SIMInfo.getInsertedSIMCount(ContactsApplication.getInstance()) > 1 && (dualSimModeSetting == 3);
   	     } else if(AuroraTelephoneManager.isMtkGemini()) { 
	       	  int dualSimModeSetting = System.getInt(ContactsApplication.getInstance().getContentResolver(), "dual_sim_mode_setting", 3);
    		 return SIMInfo.getInsertedSIMCount(ContactsApplication.getInstance()) > 1 && (dualSimModeSetting == 3);
    	 } else {
    	     boolean isAllActive = false;
    		 try {
                 int simstate = android.provider.Settings.Global.getInt(ContactsApplication.getInstance().getContentResolver(),
           		   "mobile_data"+ 2);            
                 Log.d(TAG, "updateCardState restore simstate= " + simstate);
                 if(simstate == 3) {
                 	isAllActive = true;
                 } 
             } catch (Exception e) {
          	    e.printStackTrace();
             }    
    		 return SIMInfo.getInsertedSIMCount(ContactsApplication.getInstance()) > 1 && isAllActive;
    	 }
    }
    
    // get last call SlotId
    public static int getLastCallSlotId(Context context, String number) {
        if (!isShowDoubleButton() || !FeatureOption.MTK_GEMINI_SUPPORT) {
            return -1;
        }

        int lastSimId = -1;
        String[] projection = { Calls.SIM_ID };
        Cursor cursor = null;
        if (null != context) {
            cursor = context.getContentResolver().query(Calls.CONTENT_URI,
                    projection, Calls.NUMBER + " = '" + number + "'", null,
                    "_id desc");
        }

        if (null != cursor) {
            if (true == cursor.moveToFirst()) {
                lastSimId = Integer.valueOf(cursor.getInt(0));
            }

            cursor.close();
        }

        int simId1 = SIMInfoWrapper.getDefault().getSimIdBySlotId(0);
        int simId2 = SIMInfoWrapper.getDefault().getSimIdBySlotId(1);

        Log.d(TAG, "number = " + number + "   last call simId = " + lastSimId + "  simId1 = " + simId1
                + "   simId2 = " + simId2);
        
        if (lastSimId == simId1) {
            return 0;
        } else if (lastSimId == simId2) {
            return 1;
        } else {
            return -1;
        }
    }
    
    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            dest = str.replace("\n", "");
        }
        return dest;
    }
	
	public static boolean auroraChangeFile(String file) throws Exception {
        int len = 8;
        java.io.RandomAccessFile raf = new java.io.RandomAccessFile(file, "rw");
        java.nio.channels.FileChannel channel = raf.getChannel();
        java.nio.MappedByteBuffer buffer = channel.map(
                java.nio.channels.FileChannel.MapMode.READ_WRITE, 0, len);
        
        for (int i = 0; i < len; i++) {
            byte src = buffer.get(i);
            buffer.put(i, (byte) (src ^ 2));
        }
        buffer.force();
        buffer.clear();
        channel.close();
        raf.close();
        return true;
    }
	
	//u5
	private static boolean is7503() {
		String prop = SystemProperties.get("ro.gn.gnprojectid");
		return prop.contains("CBL7503");
	}
}
