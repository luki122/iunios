package com.android.contacts.util;

import com.android.contacts.AuroraCallDetailActivity;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;
import com.android.contacts.calllog.CallLogQuery;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.SubContactsUtils;

import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.CallLog.Calls;
import android.provider.Contacts.People;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Intents.Insert;
import android.provider.Telephony.Mms;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

public class IntentFactory {
	
	public static final int DIAL_NUMBER_INTENT_NORMAL = 0;
     
    private static final int DIAL_NUMBER_INTENT_IP = 1;
    private static final int DIAL_NUMBER_INTENT_VIDEO = 2;
    private static final String KEY_AUTO_IP_DIAL_IF_SUPPORT = "autoIpDial";
    public static final String GN_CATEGORY = "android.intent.category.GIONEE";
    
    private static final String EMPTY_NUMBER = "";
    private static final String EXTRA_SEND_EMPTY_FLASH = "com.android.phone.extra.SEND_EMPTY_FLASH";
    
    private static final String TENCENT_SECURE_ACTION = "com.tencent.gionee.interceptcenter";
    
    private static Intent newDialNumberIntent(String numberType, String number, int type, boolean ifAutoIpDial) {
    	// gionee xuhz 20120910 modify for CR00688935 start
    	final Intent intent;
    	if (TextUtils.isEmpty(number)) { // No number entered.
            if (ContactsUtils.phoneIsCdma() && ContactsUtils.phoneIsOffhook()) {
                // This is really CDMA specific
                intent = IntentFactory.newDialNumberIntent(EMPTY_NUMBER, 
                		IntentFactory.DIAL_NUMBER_INTENT_NORMAL, false);
            	intent.putExtra(EXTRA_SEND_EMPTY_FLASH, true);
                return intent;
            } else {
            	return null;
            }
        }
    	
    	intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                Uri.fromParts(numberType, number, null));
    	
    	if (!Constants.SCHEME_TEL.equalsIgnoreCase(numberType)) {
    		ifAutoIpDial = false;
    	}
    	// gionee xuhz 20120910 modify for CR00688935 end

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_AUTO_IP_DIAL_IF_SUPPORT, ifAutoIpDial);
        
        intent.putExtra(Constants.EXTRA_IS_IP_DIAL, false);
        intent.putExtra(Constants.EXTRA_IS_VIDEO_CALL, false);

        if((type & DIAL_NUMBER_INTENT_IP) != 0)
            intent.putExtra(Constants.EXTRA_IS_IP_DIAL, true);

        if((type & DIAL_NUMBER_INTENT_VIDEO) != 0)
            intent.putExtra(Constants.EXTRA_IS_VIDEO_CALL, true);

        return intent;
    }
    
    public static Intent newDialNumberIntent(String number, int type, boolean ifAutoIpDial) {
    	return newDialNumberIntent("tel", number, type, ifAutoIpDial);
    }
    
    public static Intent newDialNumberIntent(String number, int type) {
    	return newDialNumberIntent(number, type, true);
    }
    
    
    
    public static Intent newDialNumberIntent(String number) {
    	return newDialNumberIntent(number, DIAL_NUMBER_INTENT_NORMAL, true);
    }
    
    

    private static Intent newDialNumberIntent(Uri callUri, boolean ifAutoIpDial) {
    	Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED, callUri);
    	
    	if (null != callUri && !Constants.SCHEME_TEL.equalsIgnoreCase(callUri.getScheme())) {
    		ifAutoIpDial = false;
    	}
    	
    	intent.putExtra(KEY_AUTO_IP_DIAL_IF_SUPPORT, ifAutoIpDial);
    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	
    	return intent;
    }
    
    public static Intent newDialNumberIntent(Uri callUri) {
    	return newDialNumberIntent(callUri, true);
    }
    
    public static Intent newGoPeopleIntent() {
        Intent intent = new Intent();
        intent.setAction("com.android.contacts.action.LIST_CONTACTS");
        //Gionee:huangzy 20130401 modify for CR00792013 start
        intent.addCategory(IntentFactory.GN_CATEGORY);
        //Gionee:huangzy 20130401 modify for CR00792013 end
        return intent;
    }
    
    

    public static Intent newViewContactIntent(Uri contactUri) {
    	Intent intent = new Intent(Intent.ACTION_VIEW, contactUri);
        //Gionee:huangzy 20130401 modify for CR00792013 start
    	intent.addCategory(IntentFactory.GN_CATEGORY);
        //Gionee:huangzy 20130401 modify for CR00792013 end
        return intent;
    }
    
    //Gionee:huangzy 20120823 add for CR00614805 start
    public static Intent newInsertContactIntent(boolean createNew, String phone, String name, String email) {    	
    	Intent intent = null;
    	if (createNew) {
    		intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
    		
    		if (!TextUtils.isEmpty(name)) {
        		intent.putExtra(Insert.NAME, name);
        	}
    	} else {
    		intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
    		intent.setType(People.CONTENT_ITEM_TYPE);
    	}
    	
    	if (!TextUtils.isEmpty(phone)) {
    		phone = ContactsUtils.trimAllSpace(phone);
    		// gionee xuhz 20120906 modify for CR00687581 start
            if (PhoneNumberUtils.isGlobalPhoneNumber(ContactsUtils.trimNumberToAdd(phone))) {
            // gionee xuhz 20120906 modify for CR00687581 end
                intent.putExtra(Insert.PHONE, phone);
            }
        } 
    	
    	if (!TextUtils.isEmpty(email)) {
        	email = ContactsUtils.trimAllSpace(email);
            if (Mms.isEmailAddress(email)) {
                intent.putExtra(Insert.EMAIL, email);
            }
        }
    	
    	/*if (!TextUtils.isEmpty(name)) {
    		intent.putExtra(Insert.NAME, name);
    	}*/
        //Gionee:huangzy 20130401 modify for CR00792013 start
    	intent.addCategory(IntentFactory.GN_CATEGORY);
        //Gionee:huangzy 20130401 modify for CR00792013 end
    	
    	return intent;
    }
    
    private static Intent newInsertContactIntent(boolean createNew, String content, String name) {
    	String phone = null;
    	String email = null;
    	if (!TextUtils.isEmpty(content)) {
    		content = ContactsUtils.trimAllSpace(content);
    		// gionee xuhz 20120906 modify for CR00687581 start
            if (PhoneNumberUtils.isGlobalPhoneNumber(ContactsUtils.trimNumberToAdd(content))) {
            // gionee xuhz 20120906 modify for CR00687581 end
            	phone = content;
            } else if (Mms.isEmailAddress(content)) {
                email = content;
            }
        }
    	return newInsertContactIntent(createNew, phone, name, email);
    }
    
    public static Intent newInsert2ExistContactIntent(String content) {
    	return newInsertContactIntent(false, content, null);
    }
    
    public static Intent newCreateContactIntent(String content) {
    	return newInsertContactIntent(true, content, null);
    }
    
    public static Intent newCreateSmsIntent(String number) {
    	Uri msgUri = Uri.fromParts(Constants.SCHEME_SMSTO, number, null);
        Intent intent = new Intent(Intent.ACTION_SENDTO, msgUri);
        //Gionee:huangzy 20130401 modify for CR00792013 start
        intent.setComponent(new ComponentName("com.android.mms", 
        		"com.android.mms.ui.ComposeMessageActivity"));
        //Gionee:huangzy 20130401 modify for CR00792013 end
        
        return intent;
    }
    

    
    
    
    //Gionee:huangzy 20120823 add for CR00614805 end
    
    
    //Gionee:huangzy 20130314 add for CR00784577 end
 //aurora changes zhouxiaobing 20130925
    public static Intent newShowCallDetailIntent(Context context, String number,int callsType, int callId, int callsCount, Uri voicemailUri,int ids[]) {
    	Intent intent = new Intent(context, AuroraCallDetailActivity.class);
    	if (voicemailUri != null) {
            intent.putExtra(AuroraCallDetailActivity.EXTRA_VOICEMAIL_URI, voicemailUri);
        }
    	
    	intent.putExtra(AuroraCallDetailActivity.EXTRA_VOICEMAIL_START_PLAYBACK, false);
        
        // If there is a single item, use the direct URI for it.
        intent.putExtra(Calls.NUMBER, number);
        intent.putExtra(Calls._COUNT, callsCount);
        if (1 == callsCount) {
        	intent.putExtra(Calls._ID, callId);
        }
        
        intent.putExtra(Constants.EXTRA_FOLLOW_SIM_MANAGEMENT, true);
        intent.putExtra(Calls.TYPE, callsType);
        intent.putExtra("ids", ids);
        //Gionee:huangzy 20130401 modify for CR00792013 start
        intent.addCategory(IntentFactory.GN_CATEGORY);
        //Gionee:huangzy 20130401 modify for CR00792013 end
        
        return intent;
    }    
 //aurora changes zhouxiaobing 20130925   
    public static Intent newPickRingtoneIntent(Context context, String customRingtone) {
    	// aurora ukiliu 2013-11-15 modify for aurora ui begin
//    	Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
    	Intent intent = new Intent("gn.com.android.audioprofile.action.RINGTONE_PICKER");
        // Allow user to pick 'Default'
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        // Show only ringtones
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
        // Don't show 'Silent'
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        CharSequence title = context.getText(R.string.menu_set_ring_tone);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, title);
        // aurora ukiliu 2013-11-15 modify for aurora ui end
        Uri ringtoneUri;
        if (customRingtone != null) {
            ringtoneUri = Uri.parse(customRingtone);
        } else {
            // Otherwise pick default ringtone Uri so that something is selected.
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        }

        // Put checkmark next to the current ringtone for this contact
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ringtoneUri);
        
        return intent;
    }
    


    
    

}
