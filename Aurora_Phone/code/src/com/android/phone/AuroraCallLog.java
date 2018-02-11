package com.android.phone;

import com.android.internal.telephony.PhoneConstants;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.DataUsageFeedback;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.provider.CallLog;

/**
 * The CallLog provider contains information about placed and received calls.
 */
public class AuroraCallLog {
    public static String AUTHORITY = "AuroraCallLog";

    
    public static final String UNKNOWN_NUMBER = "-1";
    public static final String PRIVATE_NUMBER = "-2";
    public static final String PAYPHONE_NUMBER = "-3";
    /**
     * The content:// style URL for this provider
     */
    public static Uri CONTENT_URI =
        Uri.parse("content://" + AUTHORITY);

    /**
     * Contains the recent calls.
     */
    public static class Calls implements BaseColumns {
        /**
         * The content:// style URL for this table
         */
        public static Uri CONTENT_URI =
                Uri.parse("content://call_log/calls");

        /**
         * The content:// style URL for filtering this table on phone numbers
         */
        public static Uri CONTENT_FILTER_URI =
                Uri.parse("content://call_log/calls/filter");

        /**
         * An optional URI parameter which instructs the provider to allow the operation to be
         * applied to voicemail records as well.
         * <p>
         * TYPE: Boolean
         * <p>
         * Using this parameter with a value of {@code true} will result in a security error if the
         * calling package does not have appropriate permissions to access voicemails.
         *
         * @hide
         */
        public static String ALLOW_VOICEMAILS_PARAM_KEY = "allow_voicemails";

        /**
         * Content uri with {@link #ALLOW_VOICEMAILS_PARAM_KEY} set. This can directly be used to
         * access call log entries that includes voicemail records.
         *
         * @hide
         */
        public static Uri CONTENT_URI_WITH_VOICEMAIL = CONTENT_URI.buildUpon()
                .appendQueryParameter(ALLOW_VOICEMAILS_PARAM_KEY, "true")
                .build();

        /**
         * The default sort order for this table
         */
        public static String DEFAULT_SORT_ORDER = "date DESC";

        /**
         * The MIME type of {@link #CONTENT_URI} and {@link #CONTENT_FILTER_URI}
         * providing a directory of calls.
         */
        public static String CONTENT_TYPE = "vnd.android.cursor.dir/calls";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
         * call.
         */
        public static String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/calls";

        /**
         * The type of the call (incoming, outgoing or missed).
         * <P>Type: INTEGER (int)</P>
         */
        public static String TYPE = "type";

        /** Call log type for incoming calls. */
        public static int INCOMING_TYPE = 1;
        /** Call log type for outgoing calls. */
        public static int OUTGOING_TYPE = 2;
        /** Call log type for missed calls. */
        public static int MISSED_TYPE = 3;
        /**
         * Call log type for voicemails.
         * @hide
         */
        public static int VOICEMAIL_TYPE = 4;

        /**
         * The phone number as the user entered it.
         * <P>Type: TEXT</P>
         */
        public static String NUMBER = "number";

        /**
         * The ISO 3166-1 two letters country code of the country where the
         * user received or made the call.
         * <P>
         * Type: TEXT
         * </P>
         *
         * @hide
         */
        public static String COUNTRY_ISO = "countryiso";

        /**
         * The date the call occured, in milliseconds since the epoch
         * <P>Type: INTEGER (long)</P>
         */
        public static String DATE = "date";

        /**
         * The duration of the call in seconds
         * <P>Type: INTEGER (long)</P>
         */
        public static String DURATION = "duration";

        /**
         * Whether or not the call has been acknowledged
         * <P>Type: INTEGER (boolean)</P>
         */
        public static String NEW = "new";

        /**
         * The cached name associated with the phone number, if it exists.
         * This value is not guaranteed to be current, if the contact information
         * associated with this number has changed.
         * <P>Type: TEXT</P>
         */
        public static String CACHED_NAME = "name";

        /**
         * The cached number type (Home, Work, etc) associated with the
         * phone number, if it exists.
         * This value is not guaranteed to be current, if the contact information
         * associated with this number has changed.
         * <P>Type: INTEGER</P>
         */
        public static String CACHED_NUMBER_TYPE = "numbertype";

        /**
         * The cached number label, for a custom number type, associated with the
         * phone number, if it exists.
         * This value is not guaranteed to be current, if the contact information
         * associated with this number has changed.
         * <P>Type: TEXT</P>
         */
        public static String CACHED_NUMBER_LABEL = "numberlabel";

        /**
         * URI of the voicemail entry. Populated only for {@link #VOICEMAIL_TYPE}.
         * <P>Type: TEXT</P>
         * @hide
         */
        public static String VOICEMAIL_URI = "voicemail_uri";

        /**
         * Whether this item has been read or otherwise consumed by the user.
         * <p>
         * Unlike the {@link #NEW} field, which requires the user to have acknowledged the
         * existence of the entry, this implies the user has interacted with the entry.
         * <P>Type: INTEGER (boolean)</P>
         */
        public static String IS_READ = "is_read";

        /**
         * A geocoded location for the number associated with this call.
         * <p>
         * The string represents a city, state, or country associated with the number.
         * <P>Type: TEXT</P>
         * @hide
         */
        public static String GEOCODED_LOCATION = "geocoded_location";

        /**
         * The cached URI to look up the contact associated with the phone number, if it exists.
         * This value is not guaranteed to be current, if the contact information
         * associated with this number has changed.
         * <P>Type: TEXT</P>
         * @hide
         */
        public static String CACHED_LOOKUP_URI = "lookup_uri";

        /**
         * The cached phone number of the contact which matches this entry, if it exists.
         * This value is not guaranteed to be current, if the contact information
         * associated with this number has changed.
         * <P>Type: TEXT</P>
         * @hide
         */
        public static String CACHED_MATCHED_NUMBER = "matched_number";

        /**
         * The cached normalized version of the phone number, if it exists.
         * This value is not guaranteed to be current, if the contact information
         * associated with this number has changed.
         * <P>Type: TEXT</P>
         * @hide
         */
        public static String CACHED_NORMALIZED_NUMBER = "normalized_number";

        /**
         * The cached photo id of the picture associated with the phone number, if it exists.
         * This value is not guaranteed to be current, if the contact information
         * associated with this number has changed.
         * <P>Type: INTEGER (long)</P>
         * @hide
         */
        public static String CACHED_PHOTO_ID = "photo_id";

        /**
         * The cached formatted phone number.
         * This value is not guaranteed to be present.
         * <P>Type: TEXT</P>
         * @hide
         */
        public static String CACHED_FORMATTED_NUMBER = "formatted_number";

        /**
         * The subscription id.
         * <P>Type: Integer</P>
         * @hide
         */
        public static String SUBSCRIPTION ="simid"; //CallLog.Calls.SUBSCRIPTION;

        /**
         * The network type
         * <P>Type: Integer</P>
         * @hide
         */
        public static String NETWORK_TYPE = "network_type";

        //MTK-START [mtk04070][111128][ALPS00093395]MTK added
        /**
         * {@hide}
         */
        public static String SIM_ID = SUBSCRIPTION;        
        
        
        /**
         * {@hide}
         */
        public static String VTCALL = "vtcall";
        //MTK-END [mtk04070][111128][ALPS00093395]MTK added

        /**
         * Adds a call to the call log for dual SIM.
         *
         * @param ci the CallerInfo object to get the target contact from.  Can be null
         * if the contact is unknown.
         * @param context the context used to get the ContentResolver
         * @param number the phone number to be added to the calls db
         * @param presentation the number presenting rules set by the network for
         *        "allowed", "payphone", "restricted" or "unknown"
         * @param callType enumerated values for "incoming", "outgoing", or "missed"
         * @param start time stamp for the call in milliseconds
         * @param duration call duration in seconds
         * @param simId valid value is 0 or 1
         * @param vtCall:
         *  normal telephone = 0;
         *  visual telephone = 1;
         *
         * {@hide}
         */
        public static Uri addCall(AuroraCallerInfo ci, Context context, String number,
                int presentation, int callType, long start, int duration, int simId, int vtCall, long rawContactId, long dataId, long privacyId) {
            ContentResolver resolver = context.getContentResolver();

            // If this is a private number then set the number to Private, otherwise check
            // if the number field is empty and set the number to Unavailable
            if (presentation == PhoneConstants.PRESENTATION_RESTRICTED) {
                number = PRIVATE_NUMBER;
                if (ci != null) ci.name = "";
            } else if (presentation == PhoneConstants.PRESENTATION_PAYPHONE) {
                number = PAYPHONE_NUMBER;
                if (ci != null) ci.name = "";
            } else if (TextUtils.isEmpty(number)
                    || presentation == PhoneConstants.PRESENTATION_UNKNOWN) {
                number = UNKNOWN_NUMBER;
                if (ci != null) ci.name = "";
            }

            ContentValues values = new ContentValues(5);

            values.put(NUMBER, number);
            values.put(TYPE, Integer.valueOf(callType));
            values.put(DATE, Long.valueOf(start));
            values.put(DURATION, Long.valueOf(duration));
            values.put(NEW, Integer.valueOf(1));
            if (callType == MISSED_TYPE) {
                values.put(IS_READ, Integer.valueOf(0));
            }
            if (ci != null) {
                values.put(CACHED_NAME, ci.name);
                values.put(CACHED_NUMBER_TYPE, ci.numberType);
                values.put(CACHED_NUMBER_LABEL, ci.numberLabel);
            }
            values.put(SIM_ID, simId);
            if (vtCall >= 0) {
                values.put(VTCALL, vtCall);
            }
            if(rawContactId > 0) {
                values.put(RAW_CONTACT_ID, Long.valueOf(rawContactId));
                values.put(DATA_ID, Long.valueOf(dataId));
            }
            if(privacyId >= 0) {
                values.put("privacy_id", Long.valueOf(privacyId));
            }
            if ((ci != null) && (ci.person_id > 0)) {
                // Update usage information for the number associated with the contact ID.
                // We need to use both the number and the ID for obtaining a data ID since other
                // contacts may have the same number.

                Cursor cursor;

                // We should prefer normalized one (probably coming from
                // Phone.NORMALIZED_NUMBER column) first. If it isn't available try others.
                if (ci.normalizedNumber != null) {
                    String normalizedPhoneNumber = ci.normalizedNumber;
                    cursor = resolver.query(Phone.CONTENT_URI,
                            new String[] { Phone._ID },
                            Phone.CONTACT_ID + " =? AND " + Phone.NORMALIZED_NUMBER + " =?",
                            new String[] { String.valueOf(ci.person_id), normalizedPhoneNumber},
                            null);
                } else {
                    String phoneNumber = ci.phoneNumber != null ? ci.phoneNumber : number;
                    cursor = resolver.query(Phone.CONTENT_URI,
                            new String[] { Phone._ID },
                            Phone.CONTACT_ID + " =? AND " + Phone.NUMBER + " =?",
                            new String[] { String.valueOf(ci.person_id), phoneNumber},
                            null);
                }

                if (cursor != null) {
                    try {
                        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                            Uri feedbackUri = DataUsageFeedback.FEEDBACK_URI.buildUpon()
                                    .appendPath(cursor.getString(0))
                                    .appendQueryParameter(DataUsageFeedback.USAGE_TYPE,
                                                DataUsageFeedback.USAGE_TYPE_CALL)
                                    .build();
                            resolver.update(feedbackUri, new ContentValues(), null, null);
                        }
                    } finally {
                        cursor.close();
                    }
                }
            }

            Uri result = resolver.insert(CONTENT_URI, values);

            removeExpiredEntries(context);

            return result;
        }

        /**
         * Query the call log database for the last dialed number.
         * @param context Used to get the content resolver.
         * @return The last phone number dialed (outgoing) or an empty
         * string if none exist yet.
         */
        public static String getLastOutgoingCall(Context context) {
            ContentResolver resolver = context.getContentResolver();
            Cursor c = null;
            try {
                c = resolver.query(
                    CONTENT_URI,
                    new String[] {NUMBER},
                    TYPE + " = " + OUTGOING_TYPE,
                    null,
                    DEFAULT_SORT_ORDER + " LIMIT 1");
                if (c == null || !c.moveToFirst()) {
                    return "";
                }
                return c.getString(0);
            } finally {
                if (c != null) c.close();
            }
        }

        private static void removeExpiredEntries(Context context) {
            ContentResolver resolver = context.getContentResolver();
            resolver.delete(CONTENT_URI, "_id IN " +
                    "(SELECT _id FROM calls ORDER BY " + DEFAULT_SORT_ORDER
                    + " LIMIT -1 OFFSET 500)", null);
        }

        //MTK-START [mtk04070][111128][ALPS00093395]MTK proprietary methods
        /**
         * Adds a call to the call log. VT for dual sim
         *
         * @param ci the CallerInfo object to get the target contact from.  Can be null
         * if the contact is unknown.
         * @param context the context used to get the ContentResolver
         * @param number the phone number to be added to the calls db
         * @param presentation the number presenting rules set by the network for
         *        "allowed", "payphone", "restricted" or "unknown"
         * @param callType enumerated values for "incoming", "outgoing", or "missed"
         * @param start time stamp for the call in milliseconds
         * @param duration call duration in seconds
         *
         * {@hide}
         */
        /*public static Uri addCall(CallerInfo ci, Context context, String number,
                int presentation, int callType, long start, int duration, int simId, int vtCall, boolean extra) {
            final ContentResolver resolver = context.getContentResolver();

            // If this is a private number then set the number to Private, otherwise check
            // if the number field is empty and set the number to Unavailable
            if (presentation == PhoneConstants.PRESENTATION_RESTRICTED) {
                number = CallerInfo.PRIVATE_NUMBER;
                if (ci != null) ci.name = "";
            } else if (presentation == PhoneConstants.PRESENTATION_PAYPHONE) {
                number = CallerInfo.PAYPHONE_NUMBER;
                if (ci != null) ci.name = "";
            } else if (TextUtils.isEmpty(number)
                    || presentation == PhoneConstants.PRESENTATION_UNKNOWN) {
                number = CallerInfo.UNKNOWN_NUMBER;
                if (ci != null) ci.name = "";
            }

            ContentValues values = new ContentValues(5);

            values.put(NUMBER, number);
            values.put(TYPE, Integer.valueOf(callType));
            values.put(DATE, Long.valueOf(start));
            values.put(DURATION, Long.valueOf(duration));
            values.put(NEW, Integer.valueOf(1));
            if (ci != null) {
                values.put(CACHED_NAME, ci.name);
                values.put(CACHED_NUMBER_TYPE, ci.numberType);
                values.put(CACHED_NUMBER_LABEL, ci.numberLabel);
            }
            
            values.put(SIM_ID, simId);
            if (vtCall >= 0) {
                values.put(VTCALL, vtCall);
                if (vtCall == 1) {
                    // values.put(SIM_ID, 0);  //SIM1 for VT call
                }
            }
            if ((ci != null) && (ci.person_id > 0)) {
                ContactsContract.Contacts.markAsContacted(resolver, ci.person_id);
            }

            Uri result = resolver.insert(CONTENT_URI, values);

            removeExpiredEntries(context);

            return result;
        }*/
        //MTK-END [mtk04070][111128][ALPS00093395]MTK proprietary methods


        //The fillowing lines are provided and maintained by Mediatek inc.

        /**
         * save call log corresponding phone number ID
         * {@hide}
         */
        public static String DATA_ID = "data_id";
        
        /**
         * save raw contact id of a call log corresponding to phone number 
         * {@hide}
         */
        public static String RAW_CONTACT_ID = "raw_contact_id";	

        //The previous lines are provided and maintained by Mediatek inc.
    }
}
