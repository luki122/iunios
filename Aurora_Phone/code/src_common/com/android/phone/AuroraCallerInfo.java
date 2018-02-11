package com.android.phone;

import android.util.Log;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Country;
import android.location.CountryDetector;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.RawContacts;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;
import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;

import java.util.Locale;


/**
 * Looks up caller information for the given phone number.
 *
 * {@hide}
 */
public class AuroraCallerInfo {
    private static final String TAG = "AuroraCallerInfo";
    private static final boolean VDBG = false;

    public String name;
    public String phoneNumber;
    public String normalizedNumber;
    public String geoDescription;

    public String cnapName;
    public int numberPresentation;
    public int namePresentation;
    public boolean contactExists;

    public String phoneLabel;

    public int    numberType;
    public String numberLabel;

    public int photoResource;
    public long person_id;
    public boolean needUpdate;
    public Uri contactRefUri;


    public Uri contactRingtoneUri;
    public boolean shouldSendToVoicemail;


    public Drawable cachedPhoto;

    public Bitmap cachedPhotoIcon;
 
    public boolean isCachedPhotoCurrent;

    private boolean mIsEmergency;
    private boolean mIsVoiceMail;
    
    public long mRawContactId = 0;
    
    public long mPrivateId = 0;
    
    public int mPrivateNotiType = 0; 
    
    public String mRealName = "";
    
    public String mRealNumber = ""; 
    
    public String mArea;
    public String mNote;
    public String mMark;
    public String mMarkNumber;
    public boolean isCompelete = false;

    public AuroraCallerInfo() {

        mIsEmergency = false;
        mIsVoiceMail = false;
    }


    public static AuroraCallerInfo getCallerInfo(Context context, Uri contactRef, Cursor cursor) {
        AuroraCallerInfo info = new AuroraCallerInfo();
        info.photoResource = 0;
        info.phoneLabel = null;
        info.numberType = 0;
        info.numberLabel = null;
        info.cachedPhoto = null;
        info.isCachedPhotoCurrent = false;
        info.contactExists = false;

        if (VDBG) Log.v(TAG, "getCallerInfo() based on cursor...");

        if (cursor != null) {
            if (cursor.moveToFirst()) {
       

                int columnIndex;

                // Look for the name
                columnIndex = cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME);
                if (columnIndex != -1) {
                    info.name = cursor.getString(columnIndex);
                }

                // Look for the number
                columnIndex = cursor.getColumnIndex(PhoneLookup.NUMBER);
                if (columnIndex != -1) {
                    info.phoneNumber = cursor.getString(columnIndex);
                }

                // Look for the normalized number
                columnIndex = cursor.getColumnIndex(PhoneLookup.NORMALIZED_NUMBER);
                if (columnIndex != -1) {
                    info.normalizedNumber = cursor.getString(columnIndex);
                }

                // Look for the label/type combo
                columnIndex = cursor.getColumnIndex(PhoneLookup.LABEL);
                if (columnIndex != -1) {
                    int typeColumnIndex = cursor.getColumnIndex(PhoneLookup.TYPE);
                    if (typeColumnIndex != -1) {
                        info.numberType = cursor.getInt(typeColumnIndex);
                        info.numberLabel = cursor.getString(columnIndex);
                        info.phoneLabel = Phone.getDisplayLabel(context,
                                info.numberType, info.numberLabel)
                                .toString();
                    }
                }

                // Look for the person_id.
                columnIndex = getColumnIndexForPersonId(contactRef, cursor);
                if (columnIndex != -1) {
                    info.person_id = cursor.getLong(columnIndex);
                    if (VDBG) Log.v(TAG, "==> got info.person_id: " + info.person_id);
                } else {

                    Log.w(TAG, "Couldn't find person_id column for " + contactRef);

                }


                columnIndex = cursor.getColumnIndex(PhoneLookup.CUSTOM_RINGTONE);
                if ((columnIndex != -1) && (cursor.getString(columnIndex) != null)) {
                    info.contactRingtoneUri = Uri.parse(cursor.getString(columnIndex));
                } else {
                    info.contactRingtoneUri = null;
                }

                columnIndex = cursor.getColumnIndex(PhoneLookup.SEND_TO_VOICEMAIL);
                info.shouldSendToVoicemail = (columnIndex != -1) &&
                        ((cursor.getInt(columnIndex)) == 1);
                info.contactExists = true;
                
                info.mRealName = normalize(info.name);
                if(AuroraPrivacyUtils.isSupportPrivate()) {
	                int privateData[] = AuroraPrivacyUtils.getPrivateData(contactRef);
	                long currentPrivateId = AuroraPrivacyUtils.getCurrentAccountId();
	                info.mRawContactId = privateData != null ? privateData[0] : 0;
	                info.mPrivateId = privateData != null ? privateData[1] : 0;	       
	        		if(info.mPrivateId > 0) {
	        			info.mPrivateNotiType = privateData[2];
	        			if (info.mPrivateId != currentPrivateId) {
	        	        	Log.i(TAG, "displayName need to show as unknown");   
	        	        	info.name = PhoneGlobals.getInstance().getString(R.string.private_unknown_contact);
	        			} 
	        		} 
                } else {
                    info.mRawContactId = ProviderUtils.getRawContactId(contactRef);
                }
                
                info.mNote = NoteUtils.getNote(context,  info.mRawContactId);
             	
            }
            cursor.close();
        }

        info.needUpdate = false;
//        info.name = normalize(info.name);
        info.contactRefUri = contactRef;

        return info;
    }

    public void auroraUpdate(Context context) {    	
        mRealNumber = phoneNumber;        
    	AuroraMarkUtils.resetMarks();
        RecordUtils.handleAutoRecord(context, phoneNumber, mRawContactId);
        mArea = AuroraAreaUtils.getNumArea(phoneNumber);
        AuroraMarkUtils.getNumberInfoInternal(phoneNumber);
        final String[] marks = AuroraMarkUtils.getNote(phoneNumber);
        mMark = marks[0];
        mMarkNumber = marks[1];
        if(AuroraPhoneNumberUtils.isLocalEmergencyNumber(phoneNumber, context)) {
            photoResource = R.drawable.picture_emergency;
            mIsEmergency = true;
        }
        if(PhoneGlobals.getInstance().mYuloreUtils.isInit() && !mIsEmergency) {
    		Bitmap photo = YuLoreUtils.getPhoto();
    		if (photo != null) {
    			cachedPhoto = new BitmapDrawable(photo);
    			cachedPhotoIcon = RoundBitmapUtils.getAuroraPhotoIconWhenAppropriate(context, photo);
    			isCachedPhotoCurrent = true;
    		}
        }
        isCompelete = true;
    }

    public static AuroraCallerInfo getCallerInfo(Context context, Uri contactRef) {
        String PRIVATE_SQL_ORDER = "is_privacy DESC LIMIT 0,1";
        return getCallerInfo(context, contactRef,
                context.getContentResolver().query(contactRef, null, null, null, PRIVATE_SQL_ORDER));
    }

    
    public static AuroraCallerInfo getCallerInfo(Context context, String number) {
        if (VDBG) Log.v(TAG, "getCallerInfo() based on number...");

        if (TextUtils.isEmpty(number)) {
            return null;
        }


        if (AuroraPhoneNumberUtils.isLocalEmergencyNumber(number, context)) {
            return new AuroraCallerInfo().markAsEmergency(context);
        } else if (PhoneNumberUtils.isVoiceMailNumber(number)) {
            return new AuroraCallerInfo().markAsVoiceMail();
        }

        Uri contactUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        AuroraCallerInfo info = getCallerInfo(context, contactUri);
        info = doSecondaryLookupIfNecessary(context, number, info);

 
        if (TextUtils.isEmpty(info.phoneNumber)) {
            info.phoneNumber = number;
        }

        return info;
    }


    static AuroraCallerInfo doSecondaryLookupIfNecessary(Context context,
            String number, AuroraCallerInfo previousResult) {
        if (!previousResult.contactExists
                && PhoneNumberUtils.isUriNumber(number)) {
            String username = PhoneNumberUtils.getUsernameFromUriNumber(number);
            if (PhoneNumberUtils.isGlobalPhoneNumber(username)) {
                previousResult = getCallerInfo(context,
                        Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                                Uri.encode(username)));
            }
        }
        return previousResult;
    }


    public static String getCallerId(Context context, String number) {
        AuroraCallerInfo info = getCallerInfo(context, number);
        String callerID = null;

        if (info != null) {
            String name = info.name;

            if (!TextUtils.isEmpty(name)) {
                callerID = name;
            } else {
                callerID = number;
            }
        }

        return callerID;
    }


    public boolean isEmergencyNumber() {
        return mIsEmergency;
    }

  
    public boolean isVoiceMailNumber() {
        return mIsVoiceMail;
    }

    AuroraCallerInfo markAsEmergency(Context context) {
        phoneNumber = context.getString(
            com.android.internal.R.string.emergency_call_dialog_number_for_display);
        photoResource = com.android.internal.R.drawable.picture_emergency;
        mIsEmergency = true;
        return this;
    }



     AuroraCallerInfo markAsVoiceMail() {
        mIsVoiceMail = true;

        try {
            String voiceMailLabel = TelephonyManager.getDefault().getVoiceMailAlphaTag();

            phoneNumber = voiceMailLabel;
        } catch (SecurityException se) {

            Log.e(TAG, "Cannot access VoiceMail.", se);
        }

        return this;
    }

    private static String normalize(String s) {
        if (s == null || s.length() > 0) {
            return s;
        } else {
            return null;
        }
    }


    private static int getColumnIndexForPersonId(Uri contactRef, Cursor cursor) {


        if (VDBG) Log.v(TAG, "- getColumnIndexForPersonId: contactRef URI = '"
                        + contactRef + "'...");

        String url = contactRef.toString();
        String columnName = null;
        if (url.startsWith("content://com.android.contacts/data/phones")) {
            // Direct lookup in the Phone table.
            // MIME type: Phone.CONTENT_ITEM_TYPE (= "vnd.android.cursor.item/phone_v2")
            if (VDBG) Log.v(TAG, "'data/phones' URI; using RawContacts.CONTACT_ID");
            columnName = RawContacts.CONTACT_ID;
        } else if (url.startsWith("content://com.android.contacts/data")) {
            // Direct lookup in the Data table.
            // MIME type: Data.CONTENT_TYPE (= "vnd.android.cursor.dir/data")
            if (VDBG) Log.v(TAG, "'data' URI; using Data.CONTACT_ID");
            // (Note Data.CONTACT_ID and RawContacts.CONTACT_ID are equivalent.)
            columnName = Data.CONTACT_ID;
        } else if (url.startsWith("content://com.android.contacts/phone_lookup")) {
            // Lookup in the PhoneLookup table, which provides "fuzzy matching"
            // for phone numbers.
            // MIME type: PhoneLookup.CONTENT_TYPE (= "vnd.android.cursor.dir/phone_lookup")
            if (VDBG) Log.v(TAG, "'phone_lookup' URI; using PhoneLookup._ID");
            columnName = PhoneLookup._ID;
        } else {
            Log.w(TAG, "Unexpected prefix for contactRef '" + url + "'");
        }
        int columnIndex = (columnName != null) ? cursor.getColumnIndex(columnName) : -1;
        if (VDBG) Log.v(TAG, "==> Using column '" + columnName
                        + "' (columnIndex = " + columnIndex + ") for person_id lookup...");
        return columnIndex;
    }


    public void updateGeoDescription(Context context, String fallbackNumber) {
        String number = TextUtils.isEmpty(phoneNumber) ? fallbackNumber : phoneNumber;
        geoDescription = getGeoDescription(context, number);
    }


    private static String getGeoDescription(Context context, String number) {
        if (VDBG) Log.v(TAG, "getGeoDescription('" + number + "')...");

        if (TextUtils.isEmpty(number)) {
            return null;
        }

        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        PhoneNumberOfflineGeocoder geocoder = PhoneNumberOfflineGeocoder.getInstance();

        Locale locale = context.getResources().getConfiguration().locale;
        String countryIso = getCurrentCountryIso(context, locale);
        PhoneNumber pn = null;
        try {
            if (VDBG) Log.v(TAG, "parsing '" + number
                            + "' for countryIso '" + countryIso + "'...");
            pn = util.parse(number, countryIso);
            if (VDBG) Log.v(TAG, "- parsed number: " + pn);
        } catch (NumberParseException e) {
            Log.w(TAG, "getGeoDescription: NumberParseException for incoming number '" + number + "'");
        }

        if (pn != null) {
            String description = geocoder.getDescriptionForNumber(pn, locale);
            if (VDBG) Log.v(TAG, "- got description: '" + description + "'");
            return description;
        } else {
            return null;
        }
    }

    private static String getCurrentCountryIso(Context context, Locale locale) {
        String countryIso = null;
        CountryDetector detector = (CountryDetector) context.getSystemService(
                Context.COUNTRY_DETECTOR);
        if (detector != null) {
            Country country = detector.detectCountry();
            if (country != null) {
                countryIso = country.getCountryIso();
            } else {
                Log.e(TAG, "CountryDetector.detectCountry() returned null.");
            }
        }
        if (countryIso == null) {
            countryIso = locale.getCountry();
            Log.w(TAG, "No CountryDetector; falling back to countryIso based on locale: "
                    + countryIso);
        }
        return countryIso;
    }

    protected static String getCurrentCountryIso(Context context) {
        return getCurrentCountryIso(context, Locale.getDefault());
    }

    /**
     * @return a string debug representation of this instance.
     */
    public String toString() {

        final boolean VERBOSE_DEBUG = false;

        if (VERBOSE_DEBUG) {
            return new StringBuilder(384)
                    .append(super.toString() + " { ")
                    .append("\nname: " + name)
                    .append("\nphoneNumber: " + phoneNumber)
                    .append("\nnormalizedNumber: " + normalizedNumber)
                    .append("\ngeoDescription: " + geoDescription)
                    .append("\ncnapName: " + cnapName)
                    .append("\nnumberPresentation: " + numberPresentation)
                    .append("\nnamePresentation: " + namePresentation)
                    .append("\ncontactExits: " + contactExists)
                    .append("\nphoneLabel: " + phoneLabel)
                    .append("\nnumberType: " + numberType)
                    .append("\nnumberLabel: " + numberLabel)
                    .append("\nphotoResource: " + photoResource)
                    .append("\nperson_id: " + person_id)
                    .append("\nneedUpdate: " + needUpdate)
                    .append("\ncontactRefUri: " + contactRefUri)
                    .append("\ncontactRingtoneUri: " + contactRefUri)
                    .append("\nshouldSendToVoicemail: " + shouldSendToVoicemail)
                    .append("\ncachedPhoto: " + cachedPhoto)
                    .append("\nisCachedPhotoCurrent: " + isCachedPhotoCurrent)
                    .append("\nemergency: " + mIsEmergency)
                    .append("\nvoicemail " + mIsVoiceMail)
                    .append("\ncontactExists " + contactExists)
                    .append(" }")
                    .toString();
        } else {
            return new StringBuilder(128)
                    .append(super.toString() + " { ")
                    .append("name " + ((name == null) ? "null" : "non-null"))
                    .append(", phoneNumber " + ((phoneNumber == null) ? "null" : "non-null"))
                    .append(" }")
                    .toString();
        }
    }    
    
    public boolean isPrivate() {
		if(mPrivateId > 0) {
	        long currentPrivateId = AuroraPrivacyUtils.getCurrentAccountId();
			if (mPrivateId != currentPrivateId) {
				return true;
			} 
		} 
		return false;
    }


}