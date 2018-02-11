package com.mediatek.contacts;

import java.util.ArrayList;
import java.util.Set;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.contacts.ContactsUtils;
import com.android.contacts.GNContactsUtils;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.ITelephony;

import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;

import com.aurora.android.contacts.AuroraITelephony;
import com.aurora.android.contacts.AuroraTelephonyManager;


import com.android.contacts.AuroraCardTypeUtils;
import com.mediatek.contacts.simcontact.SimCardUtils;

public class SubContactsUtils extends ContactsUtils{
    
    private static final String TAG = "SubContactsUtils";
    
    
    
    
     
    private static final int GEMINI_SLOT1 = ContactsFeatureConstants.GEMINI_SIM_1;
     
    private static final int GEMINI_SLOT2 = ContactsFeatureConstants.GEMINI_SIM_2;
    
    
     
    private static final Uri mIccUsim1Uri = Uri.parse("content://icc/pbr1/");
     
    private static final Uri mIccUsim2Uri = Uri.parse("content://icc/pbr2/");
    
     
    private static final Uri mIccUri = Uri.parse("content://icc/adn/");//80794   
     
    private static final Uri mIccUri1 = Uri.parse("content://icc/adn1/");
     
    private static final Uri mIccUri2 = Uri.parse("content://icc/adn2/");
    
    /************************************************************************
     * Flags used to indicate the service status while importing sim contacts
     * They are controlled in AbstractStartSIMService.java, and anywhere else
     * should not try to rewrite them.
     ************************************************************************/
    //0. SIM contacts have been imported, or importing service is not started.
    //1, SIM contacts is being imported.
    private static int mSlot1Imported = 0;
    private static int mSlot2Imported = 0;
    private static int mSlotImported = 0;
    
    
    static ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
    
    public static void setSimContactsImportState(int slot) {
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            if (slot == GEMINI_SLOT1)
                mSlot1Imported = 1;
            if (slot == GEMINI_SLOT2)
                mSlot2Imported = 1;
        } else {
            Log.d(TAG, "[single]set mSlotImported 1");
            mSlotImported = 1;
        }
    }
    
    public static boolean simStateReady () {
        // Aurora xuyong 2015-08-10 modified for bug #14977 start
    	if (FeatureOption.MTK_GEMINI_SUPPORT) {
	        return simStateReady(0) || simStateReady(1);
    	} else {
    		boolean simReady = (TelephonyManager.SIM_STATE_READY 
	                == TelephonyManager.getDefault().getSimState());
	        return simReady;
    	}
        // Aurora xuyong 2015-08-10 modified for bug #14977 end
    }
    
    public static boolean simStateReady (int simId) {    	
    	if (FeatureOption.MTK_GEMINI_SUPPORT)//aurora add zhouxiaobing 20140421
    	{
            boolean simReady = (TelephonyManager.SIM_STATE_READY 
                   == AuroraTelephonyManager.getSimStateGemini(simId));
            return simReady;
    	}
    	else
    	{
    		boolean simReady = (TelephonyManager.SIM_STATE_READY 
                    == TelephonyManager.getDefault().getSimState());
            return simReady; 
    	}
    }
    
    public static void resetSimContactsImportState(int slot) {
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            if (slot == GEMINI_SLOT1)
                mSlot1Imported = 0;
            if (slot == GEMINI_SLOT2)
                mSlot2Imported = 0;
        } else {
            Log.d(TAG, "[single]set mSlotImported 0");
            mSlotImported = 0;
        }
    }
    /**
     * Check whether sim contacts have been imported after booting.
     * @param slot
     * @return
     */
    public static boolean hasSimContactsImported(int slot) {
        boolean hasImported = false;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            if (slot == GEMINI_SLOT1 && mSlot1Imported == 0)
                hasImported = true;
            if (slot == GEMINI_SLOT2 && mSlot2Imported == 0)
                hasImported = true;
        } else {
            Log.d(TAG, "[single]get mSlotImported:" + mSlotImported);
            if (mSlotImported == 0)
                hasImported = true;
        }
        return hasImported;
    }
  //added by sera begin, 2011-09-02, CR71159
    public static boolean checkPhbReady(int slotID) {
    	
    	return SimCardUtils.isPhoneBookReady(slotID);
    }
    
    public static long queryForRawContactId(ContentResolver cr, long contactId) {
        Cursor rawContactIdCursor = null;
        long rawContactId = -1;
        try {
            rawContactIdCursor = cr.query(RawContacts.CONTENT_URI,
                    new String[] {RawContacts._ID},
                    RawContacts.CONTACT_ID + "=" + contactId, null, null);
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
    
    
    public static Uri getUri(int slotId) {
        // qc begin
        if (GNContactsUtils.isOnlyQcContactsSupport()) {
            return GNContactsUtils.getQcSimUri(slotId);
        }
        // qc end
        Uri uri = null;
                //we must certify the ITelephony is Alive, so now we get the ITelephony again.
        iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
        try {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                //qc--mtk
            	if(!GNContactsUtils.isMultiSimEnabled())//aurora add zhouxiaobing 20140421
            	{
            		if(slotId==0)
            			uri=Uri.parse("content://icc/adn/");
            		else
            			uri=Uri.parse("content://icc/adn_sub2/");
            	}
            	else
            	{
                //if (slotId == com.android.internal.telephony.Phone.GEMINI_SIM_1) {
                if (slotId == ContactsFeatureConstants.GEMINI_SIM_1) {
                    //qc--mtk
                    //if (iTel != null && iTel.getIccCardTypeGemini(slotId).equals("USIM")) {
                    if (iTel != null && AuroraCardTypeUtils.getIccCardTypeGemini(iTel, slotId).equals("USIM")) {
                        uri = mIccUsim1Uri;
                    } else {
                        uri = mIccUri1;
                    }
                } else {
                    //qc--mtk
                    //if (iTel != null && iTel.getIccCardTypeGemini(slotId).equals("USIM")) {
                    if (iTel != null && AuroraCardTypeUtils.getIccCardTypeGemini(iTel, slotId).equals("USIM")) {
                        uri = mIccUsim2Uri;
                    } else {
                        uri = mIccUri2;
                    }
                }
            	}
            } else {
                //qc--mtk
                //if (iTel != null && iTel.getIccCardType().equals("USIM")) {
/*                if (iTel != null && AuroraITelephony.getIccCardType(iTel).equals("USIM")) {
                    uri = mIccUsimUri;
                } else {
                    uri = mIccUri;
                }*/
            	uri = mIccUri;//aurora change zhouxiaobing 20140421
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return uri;
    }
    
    
    
    // For index in SIM change feature, we add the 'int indexInSim' argument 
    // into the argument list.
    public static Uri insertToDB(Account mAccount,String name, String number, String email,
            String additionalNumber, ContentResolver resolver, long indicate,
            String simType, long indexInSim) {
        return insertToDB(mAccount,name, number, email, additionalNumber, resolver, indicate,
                simType, indexInSim, null);
    }
    public static Uri insertToDB(Account mAccount, String name, String number, String email,
            String additionalNumber, ContentResolver resolver, long indicate,
            String simType, long indexInSim, Set<Long> grpAddIds) {
        Uri retUri = null;
        final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newInsert(RawContacts.CONTENT_URI);
        ContentValues contactvalues = new ContentValues();
        contactvalues.put(RawContacts.ACCOUNT_NAME, mAccount.name);
        contactvalues.put(RawContacts.ACCOUNT_TYPE, mAccount.type);
        contactvalues.put(RawContacts.INDICATE_PHONE_SIM, indicate);
        contactvalues.put(RawContacts.AGGREGATION_MODE,
                RawContacts.AGGREGATION_MODE_DISABLED);

        contactvalues.put(RawContacts.INDEX_IN_SIM, indexInSim); // index in SIM
        
        builder.withValues(contactvalues);

        operationList.add(builder.build());

        int phoneType = 7;
        String phoneTypeSuffix = "";
        // mtk80909 for ALPS00023212
        if (!TextUtils.isEmpty(name)) {
            final NamePhoneTypePair namePhoneTypePair = new NamePhoneTypePair(name);
        name = namePhoneTypePair.name;
                phoneType = namePhoneTypePair.phoneType;
                phoneTypeSuffix = namePhoneTypePair.phoneTypeSuffix;
        }
        Log.v(TAG, "insert name="+name);

        
        //insert number
        if (!TextUtils.isEmpty(number)) {
//          number = PhoneNumberFormatUtilEx.formatNumber(number);
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference(Phone.RAW_CONTACT_ID, 0);
            builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
            builder.withValue(Phone.NUMBER, number);
            // mtk80909 for ALPS00023212
//          builder.withValue(Phone.TYPE, phoneType);
            builder.withValue(Data.DATA2, 2);
            if (!TextUtils.isEmpty(phoneTypeSuffix)) {
                builder.withValue(Data.DATA15, phoneTypeSuffix);
            }
            operationList.add(builder.build());
            } 
        
    
        //insert name
        if (!TextUtils.isEmpty(name)) {
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference(StructuredName.RAW_CONTACT_ID, 0);
            builder.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
           // builder.withValue(StructuredName.GIVEN_NAME, name);
            builder.withValue(StructuredName.DISPLAY_NAME, name);
            operationList.add(builder.build());
        }
        

        //if USIM
        if (simType.equals("USIM")) {
            //insert email          
        if (!TextUtils.isEmpty(email)) {        
//            for (String emailAddress : emailAddressArray) {
                Log.i(TAG,"In actuallyImportOneSimContact email is " + email);
                builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                builder.withValueBackReference(Email.RAW_CONTACT_ID, 0);
                builder.withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
                builder.withValue(Email.TYPE, Email.TYPE_MOBILE);
                builder.withValue(Email.DATA, email);
                operationList.add(builder.build());
//            }
            }               
            if (!TextUtils.isEmpty(additionalNumber)) {
//          additionalNumber = PhoneNumberFormatUtilEx.formatNumber(additionalNumber);
            Log.i(TAG,"additionalNumber is " + additionalNumber);
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference(Phone.RAW_CONTACT_ID, 0);
            builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
//          builder.withValue(Phone.TYPE, phoneType);   
            builder.withValue(Data.DATA2, 7);   
            builder.withValue(Phone.NUMBER, additionalNumber);
            builder.withValue(Data.IS_ADDITIONAL_NUMBER, 1);
            operationList.add(builder.build());
            }       
            //USIM Group begin
            if (grpAddIds != null && grpAddIds.size() > 0) {
                Long [] grpIdArray = grpAddIds.toArray(new Long[0]);
                for (Long grpId: grpIdArray) {
                    builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                    builder.withValueBackReference(Phone.RAW_CONTACT_ID, 0);
                    builder.withValue(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
                    builder.withValue(GroupMembership.GROUP_ROW_ID, grpId);
                    operationList.add(builder.build());
                }
            }
            //USIM group end
        }       

        try {
            ContentProviderResult[] result= resolver.applyBatch(ContactsContract.AUTHORITY,
                    operationList);// saved in database
            Uri rawContactUri = result[0].uri;
            Log.w(TAG, "[insertToDB]rawContactUri:" + rawContactUri);
            retUri = RawContacts.getContactLookupUri(resolver, rawContactUri);
            Log.w(TAG, "[insertToDB]retUri:" + retUri);
        } catch (RemoteException e) {
            Log.e(TAG, String.format("%s: %s", e.toString(), e
                    .getMessage()));
        } catch (OperationApplicationException e) {
            Log.e(TAG, String.format("%s: %s", e.toString(), e
                    .getMessage()));
        }
        
        return retUri;
        
    }
    
    
    public static void buildInsertOperation(
            ArrayList<ContentProviderOperation> operationList,
            Account mAccount, String name, String number, String email,
            String additionalNumber, ContentResolver resolver, long indicate,
            String simType, long indexInSim, Set<Long> grpAddIds) {
        if (operationList == null)
            return;
        Uri retUri = null;
        int backRef = operationList.size();
        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newInsert(RawContacts.CONTENT_URI);
        ContentValues contactvalues = new ContentValues();
        contactvalues.put(RawContacts.ACCOUNT_NAME, mAccount.name);
        contactvalues.put(RawContacts.ACCOUNT_TYPE, mAccount.type);
        contactvalues.put(RawContacts.INDICATE_PHONE_SIM, indicate);
        contactvalues.put(RawContacts.AGGREGATION_MODE,RawContacts.AGGREGATION_MODE_DISABLED);
        contactvalues.put(RawContacts.INDEX_IN_SIM, indexInSim); // index in SIM
        builder.withValues(contactvalues);
    
        operationList.add(builder.build());
    
        int phoneType = 7;
        String phoneTypeSuffix = "";
        // ALPS00023212
        if (!TextUtils.isEmpty(name)) {
            final NamePhoneTypePair namePhoneTypePair = new NamePhoneTypePair(name);
            name = namePhoneTypePair.name;
            phoneType = namePhoneTypePair.phoneType;
            phoneTypeSuffix = namePhoneTypePair.phoneTypeSuffix;
        }
    
        // insert phone number
        if (!TextUtils.isEmpty(number)) {
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference(Phone.RAW_CONTACT_ID, backRef);
            builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
            builder.withValue(Phone.NUMBER, number);
            builder.withValue(Data.DATA2, 2);
            if (!TextUtils.isEmpty(phoneTypeSuffix)) {
                builder.withValue(Data.DATA15, phoneTypeSuffix);
            }
            operationList.add(builder.build());
        }
    
        // insert name
        if (!TextUtils.isEmpty(name)) {
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference(StructuredName.RAW_CONTACT_ID, backRef);
            builder.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
            //builder.withValue(StructuredName.GIVEN_NAME, name);
            builder.withValue(StructuredName.DISPLAY_NAME, name);
            operationList.add(builder.build());
        }
    
        // if USIM
        if (simType.equals("USIM")) {
            // insert email
            if (!TextUtils.isEmpty(email)) {
                // for (String emailAddress : emailAddressArray) {
                Log.i(TAG, "In actuallyImportOneSimContact email is " + email);
                builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                builder.withValueBackReference(Email.RAW_CONTACT_ID, backRef);
                builder.withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
                builder.withValue(Email.TYPE, Email.TYPE_MOBILE);
                builder.withValue(Email.DATA, email);
                operationList.add(builder.build());
                // }
            }
            if (!TextUtils.isEmpty(additionalNumber)) {
                // additionalNumber = PhoneNumberFormatUtilEx.formatNumber(additionalNumber);
                Log.i(TAG, "additionalNumber is " + additionalNumber);
                builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                builder.withValueBackReference(Phone.RAW_CONTACT_ID, backRef);
                builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                // builder.withValue(Phone.TYPE, phoneType);
                builder.withValue(Data.DATA2, 7);
                builder.withValue(Phone.NUMBER, additionalNumber);
                builder.withValue(Data.IS_ADDITIONAL_NUMBER, 1);
                operationList.add(builder.build());
            }
            // for USIM Group
            if (grpAddIds != null && grpAddIds.size() > 0) {
                Long[] grpIdArray = grpAddIds.toArray(new Long[0]);
                for (Long grpId : grpIdArray) {
                    builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                    builder.withValueBackReference(Phone.RAW_CONTACT_ID, backRef);
                    builder.withValue(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
                    builder.withValue(GroupMembership.GROUP_ROW_ID, grpId);
                    operationList.add(builder.build());
                }
            }
        }
    }
    
 // mtk80909 for ALPS00023212
    public static class NamePhoneTypePair {
        public String name;
        public int phoneType;
        public String phoneTypeSuffix;
        public NamePhoneTypePair(String nameWithPhoneType) {
            // Look for /W /H /M or /O at the end of the name signifying the type
            int nameLen = nameWithPhoneType.length();
            if (nameLen - 2 >= 0 && nameWithPhoneType.charAt(nameLen - 2) == '/') {
                char c = Character.toUpperCase(nameWithPhoneType.charAt(nameLen - 1));
                phoneTypeSuffix = String.valueOf(nameWithPhoneType.charAt(nameLen - 1));
                if (c == 'W') {
                    phoneType = Phone.TYPE_WORK;
                } else if (c == 'M' || c == 'O') {
                    phoneType = Phone.TYPE_MOBILE;
                } else if (c == 'H') {
                    phoneType = Phone.TYPE_HOME;
                } else {
                    phoneType = Phone.TYPE_OTHER;
                }
                name = nameWithPhoneType.substring(0, nameLen - 2);
            } else {
                phoneTypeSuffix = "";
                phoneType = Phone.TYPE_OTHER;
                name = nameWithPhoneType;
            }
        }
    }

    
}
