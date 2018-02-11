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
 * limitations under the License
 */

package com.android.providers.contacts;

import static com.android.providers.contacts.util.DbQueryUtils.checkForSupportedColumns;
import static com.android.providers.contacts.util.DbQueryUtils.getEqualityClause;
import static com.android.providers.contacts.util.DbQueryUtils.getInequalityClause;

import com.android.providers.contacts.ContactsDatabaseHelper.GnSyncColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.PhoneLookupColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.SearchIndexColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.Tables;
import com.android.providers.contacts.ContactsDatabaseHelper.Views;
import com.android.providers.contacts.util.SelectionBuilder;
import com.android.providers.contacts.util.YuloreUtil;
import com.google.common.annotations.VisibleForTesting;
import com.mediatek.providers.contacts.ContactsFeatureConstants;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import gionee.provider.GnCallLog;
import gionee.provider.GnContactsContract;
import gionee.provider.GnCallLog.Calls;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.RawContacts;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.DialerSearch;
import gionee.provider.GnContactsContract.PhoneLookup;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import gionee.provider.GnContactsContract.CommonDataKinds.SipAddress;
import com.android.providers.contacts.util.PhoneNumberUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import com.mediatek.providers.contacts.ContactsFeatureConstants.FeatureOption;
import com.privacymanage.service.AuroraPrivacyUtils;

import gionee.provider.GnContactsContract.RawContacts;
import android.app.SearchManager;

import com.android.providers.contacts.ContactsDatabaseHelper.Views;
import android.text.TextUtils;

/**
 * Call log content provider.
 */
public class CallLogProvider extends ContentProvider {
    /** Selection clause to use to exclude voicemail records.  */
    private static final String EXCLUDE_VOICEMAIL_SELECTION = getInequalityClause(
            Calls.TYPE, Integer.toString(Calls.VOICEMAIL_TYPE));

    private static final int CALLS = 1;
    private static final int CALLS_ID = 2;
    private static final int CALLS_FILTER = 3;
    private static final int CALLS_SEARCH_FILTER = 4;
    private static final int CALLS_JION_DATA_VIEW = 5;
    private static final int CALLS_JION_DATA_VIEW_ID = 6;
    private static final int SEARCH_SUGGESTIONS = 10001;
    private static final int SEARCH_SHORTCUT = 10002;
    private static final int AURORA_DIALER_CALLLOG_SEARCH = 10003;   
    private CallLogSearchSupport mCallLogSearchSupport;
    
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(GnCallLog.AUTHORITY, "calls", CALLS);
        sURIMatcher.addURI(GnCallLog.AUTHORITY, "calls/#", CALLS_ID);
        sURIMatcher.addURI(GnCallLog.AUTHORITY, "calls/filter/*", CALLS_FILTER);
        sURIMatcher.addURI(GnCallLog.AUTHORITY, "calls/search_filter/*", CALLS_SEARCH_FILTER);
        sURIMatcher.addURI(GnCallLog.AUTHORITY, "callsjoindataview", CALLS_JION_DATA_VIEW);
        sURIMatcher.addURI(GnCallLog.AUTHORITY, "callsjoindataview/#", CALLS_JION_DATA_VIEW_ID);
        sURIMatcher.addURI(GnCallLog.AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGESTIONS);
        sURIMatcher.addURI(GnCallLog.AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGESTIONS);
        sURIMatcher.addURI(GnCallLog.AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", SEARCH_SHORTCUT);
        sURIMatcher.addURI(GnCallLog.AUTHORITY, "calls/aurora_search_filter/*", AURORA_DIALER_CALLLOG_SEARCH);
    }

    private static final HashMap<String, String> sCallsProjectionMap;
    static {

        // Calls projection map
        sCallsProjectionMap = new HashMap<String, String>();
        sCallsProjectionMap.put(Calls._ID, Tables.CALLS + "._id as " + Calls._ID);
//        sCallsProjectionMap.put(Calls._ID, Calls._ID);
        sCallsProjectionMap.put(Calls.NUMBER, Calls.NUMBER);
        sCallsProjectionMap.put(Calls.DATE, Calls.DATE);
        sCallsProjectionMap.put(Calls.DURATION, Calls.DURATION);
        sCallsProjectionMap.put(Calls.TYPE, Calls.TYPE);
        sCallsProjectionMap.put(Calls.NEW, Calls.NEW);
        sCallsProjectionMap.put(Calls.VOICEMAIL_URI, Calls.VOICEMAIL_URI);
        sCallsProjectionMap.put(Calls.IS_READ, Calls.IS_READ);
        sCallsProjectionMap.put(Calls.CACHED_NAME, Calls.CACHED_NAME);
        sCallsProjectionMap.put(Calls.CACHED_NUMBER_TYPE, Calls.CACHED_NUMBER_TYPE);
        sCallsProjectionMap.put(Calls.CACHED_NUMBER_LABEL, Calls.CACHED_NUMBER_LABEL);
        sCallsProjectionMap.put(Calls.COUNTRY_ISO, Calls.COUNTRY_ISO);
        sCallsProjectionMap.put(Calls.GEOCODED_LOCATION, Calls.GEOCODED_LOCATION);
        sCallsProjectionMap.put(Calls.CACHED_LOOKUP_URI, Calls.CACHED_LOOKUP_URI);
        sCallsProjectionMap.put(Calls.CACHED_MATCHED_NUMBER, Calls.CACHED_MATCHED_NUMBER);
        sCallsProjectionMap.put(Calls.CACHED_NORMALIZED_NUMBER, Calls.CACHED_NORMALIZED_NUMBER);
        sCallsProjectionMap.put(Calls.CACHED_PHOTO_ID, Calls.CACHED_PHOTO_ID);
        sCallsProjectionMap.put(Calls.CACHED_FORMATTED_NUMBER, Calls.CACHED_FORMATTED_NUMBER);

        // The fillowing lines are provided and maintained by Mediatek inc.
        sCallsProjectionMap.put(Calls.SIM_ID, Calls.SIM_ID);
        sCallsProjectionMap.put(Calls.VTCALL, Calls.VTCALL);
        sCallsProjectionMap.put(Calls.RAW_CONTACT_ID, Calls.RAW_CONTACT_ID);
        sCallsProjectionMap.put(Calls.DATA_ID, Calls.DATA_ID);
        // The previous lines are provided and maintained by Mediatek inc.
        
        //Gionee:huangzy 20121128 add for CR00736966 start
        sCallsProjectionMap.put(GnSyncColumns.GN_VERSION, GnSyncColumns.GN_VERSION);
        //Gionee:huangzy 20121128 add for CR00736966 end
        sCallsProjectionMap.put("area", "area");
        sCallsProjectionMap.put("reject", "reject");
        sCallsProjectionMap.put("mark", "mark");
        sCallsProjectionMap.put("black_name", "black_name");
        sCallsProjectionMap.put("user_mark", "user_mark");
        sCallsProjectionMap.put("presentation", "presentation");
        sCallsProjectionMap.put("privacy_id", "privacy_id");
//        sCallsProjectionMap.put("call_notification_type", "call_notification_type");
    }

    private static final String mstableCallsJoinData = Tables.CALLS + " LEFT JOIN " 
    + " (SELECT * FROM " +  Views.DATA + " WHERE " + Data._ID + " IN "
    + "(SELECT " +  Calls.DATA_ID + " FROM " + Tables.CALLS + ")) AS " + Views.DATA
            + " ON(" + Tables.CALLS + "." + Calls.DATA_ID + " = " + Views.DATA + "." + Data._ID + ")";

    // Must match the definition in CallLogQuery - begin.
    private static final String CALL_NUMBER_TYPE = "calllognumbertype";
    private static final String CALL_NUMBER_TYPE_ID = "calllognumbertypeid";
    // Must match the definition in CallLogQuery - end.

    private static final HashMap<String, String> sCallsJoinDataViewProjectionMap;
    static {
        // Calls Join view_data projection map
        sCallsJoinDataViewProjectionMap = new HashMap<String, String>();
        sCallsJoinDataViewProjectionMap.put(Calls._ID, Tables.CALLS + "._id as " + Calls._ID);
        sCallsJoinDataViewProjectionMap.put(Calls.NUMBER, Calls.NUMBER);
        sCallsJoinDataViewProjectionMap.put(Calls.DATE, Calls.DATE);
        sCallsJoinDataViewProjectionMap.put(Calls.DURATION, Calls.DURATION);
        sCallsJoinDataViewProjectionMap.put(Calls.TYPE, Calls.TYPE);
        sCallsJoinDataViewProjectionMap.put(Calls.VOICEMAIL_URI, Calls.VOICEMAIL_URI);
        sCallsJoinDataViewProjectionMap.put(Calls.COUNTRY_ISO, Calls.COUNTRY_ISO);
        sCallsJoinDataViewProjectionMap.put(Calls.GEOCODED_LOCATION, Calls.GEOCODED_LOCATION);
        sCallsJoinDataViewProjectionMap.put(Calls.IS_READ, Calls.IS_READ);

        sCallsJoinDataViewProjectionMap.put(Calls.SIM_ID, Calls.SIM_ID);
        sCallsJoinDataViewProjectionMap.put(Calls.VTCALL, Calls.VTCALL);
        sCallsJoinDataViewProjectionMap.put(Calls.RAW_CONTACT_ID, Tables.CALLS + "." + Calls.RAW_CONTACT_ID + " AS " + Calls.RAW_CONTACT_ID);
        sCallsJoinDataViewProjectionMap.put(Calls.DATA_ID, Calls.DATA_ID);

        sCallsJoinDataViewProjectionMap.put(Contacts.DISPLAY_NAME, 
                Views.DATA + "." + Contacts.DISPLAY_NAME + " AS " + Contacts.DISPLAY_NAME);
        sCallsJoinDataViewProjectionMap.put(CALL_NUMBER_TYPE_ID,
                Views.DATA + "." + Data.DATA2 + " AS " + CALL_NUMBER_TYPE_ID);
        sCallsJoinDataViewProjectionMap.put(CALL_NUMBER_TYPE,
                Views.DATA + "." + Data.DATA3 + " AS " + CALL_NUMBER_TYPE);
        sCallsJoinDataViewProjectionMap.put(Data.PHOTO_ID, Views.DATA + "." + Data.PHOTO_ID + " AS " + Data.PHOTO_ID);
        sCallsJoinDataViewProjectionMap.put(RawContacts.INDICATE_PHONE_SIM, RawContacts.INDICATE_PHONE_SIM);
        sCallsJoinDataViewProjectionMap.put(RawContacts.CONTACT_ID, RawContacts.CONTACT_ID);
        sCallsJoinDataViewProjectionMap.put(Contacts.LOOKUP_KEY, Views.DATA + "." + Contacts.LOOKUP_KEY + " AS " + Contacts.LOOKUP_KEY);
        sCallsJoinDataViewProjectionMap.put(Data.PHOTO_URI, Views.DATA + "." + Data.PHOTO_URI + " AS " + Data.PHOTO_URI);
        sCallsJoinDataViewProjectionMap.put("area", "area");
        sCallsJoinDataViewProjectionMap.put("reject", "reject");
        sCallsJoinDataViewProjectionMap.put("mark", "mark");
        sCallsJoinDataViewProjectionMap.put("black_name", "black_name");
        sCallsJoinDataViewProjectionMap.put("user_mark", "user_mark");
        sCallsJoinDataViewProjectionMap.put("presentation", "presentation");
        sCallsJoinDataViewProjectionMap.put("privacy_id", "privacy_id");
        sCallsJoinDataViewProjectionMap.put("call_notification_type", "call_notification_type");
    }

    private ContactsDatabaseHelper mDbHelper;
    private DatabaseUtils.InsertHelper mCallsInserter;
    private boolean mUseStrictPhoneNumberComparation;
    private VoicemailPermissions mVoicemailPermissions;
    private CallLogInsertionHelper mCallLogInsertionHelper;

    @Override
    public boolean onCreate() {
        if (Log.isLoggable(Constants.PERFORMANCE_TAG, Log.DEBUG)) {
            Log.d(Constants.PERFORMANCE_TAG, "CallLogProvider.onCreate start");
        }
        final Context context = getContext();
        mDbHelper = getDatabaseHelper(context);
        mUseStrictPhoneNumberComparation = false;
        mVoicemailPermissions = new VoicemailPermissions(context);
        mCallLogInsertionHelper = createCallLogInsertionHelper(context);
        if (Log.isLoggable(Constants.PERFORMANCE_TAG, Log.DEBUG)) {
            Log.d(Constants.PERFORMANCE_TAG, "CallLogProvider.onCreate finish");
        }
        
        mCallLogSearchSupport = new CallLogSearchSupport(this);
        
        return true;
    }

    @VisibleForTesting
    protected CallLogInsertionHelper createCallLogInsertionHelper(final Context context) {
        return DefaultCallLogInsertionHelper.getInstance(context);
    }

    @VisibleForTesting
    protected ContactsDatabaseHelper getDatabaseHelper(final Context context) {
        return ContactsDatabaseHelper.getInstance(context);
    }
    
    /**
     * Gets the value of the "limit" URI query parameter.
     *
     * @return A string containing a non-negative integer, or <code>null</code> if
     *         the parameter is not set, or is set to an invalid value.
     */
    private String getLimit(Uri uri) {
        String limitParam = ContactsProvider2.getQueryParameter(uri, "limit");
        if (limitParam == null) {
            return null;
        }
        // make sure that the limit is a non-negative integer
        try {
            int l = Integer.parseInt(limitParam);
            if (l < 0) {
                Log.w(TAG, "Invalid limit parameter: " + limitParam);
                return null;
            }
            return String.valueOf(l);
        } catch (NumberFormatException ex) {
            Log.w(TAG, "Invalid limit parameter: " + limitParam);
            return null;
        }
    }
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Tables.CALLS);
        qb.setProjectionMap(sCallsProjectionMap);
        qb.setStrict(true);
        
        int match = sURIMatcher.match(uri);
        Log.e(TAG, "selection : " + selection);
        String auroraSelection = selection;
        if (ContactsProvidersApplication.sIsAuroraRejectSupport && match != CALLS_JION_DATA_VIEW_ID && match != CALLS_ID) {
        	if (selection == null) {
                auroraSelection = "reject=0";
            } else {
                if (!selection.contains("reject")) {
                	auroraSelection = "(" + selection + ") AND reject=0 ";
                }
            }
        }
        
    	//aurora add liguangyu 201411029 for privacy start
        auroraSelection = parseCallLogSelection(auroraSelection, false);
 	   //aurora add liguangyu 201411029 for privacy end

        SelectionBuilder selectionBuilder = new SelectionBuilder(auroraSelection);
        checkVoicemailPermissionAndAddRestriction(uri, selectionBuilder);

        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String limit = getLimit(uri);
        String groupBy = null;
        Log.i(TAG,"uri == " + uri + "    match == " +match);
        switch (match) {        
            case CALLS:
                break;

            case CALLS_ID: {
                selectionBuilder.addClause(getEqualityClause(Calls._ID,
                        parseCallIdFromUri(uri)));
                break;
            }

            case CALLS_FILTER: {
                String phoneNumber = uri.getPathSegments().get(2);
                qb.appendWhere("PHONE_NUMBERS_EQUAL(number, ");
                qb.appendWhereEscapeString(phoneNumber);
                qb.appendWhere(mUseStrictPhoneNumberComparation ? ", 1)" : ", 0)");
                break;
            }

            case CALLS_SEARCH_FILTER: {
                String query = uri.getPathSegments().get(2);
                String nomalizeName = NameNormalizer.normalize(query);
                final String SNIPPET_CONTACT_ID = "snippet_contact_id";
            	String table = Tables.CALLS
                + " LEFT JOIN " + Views.DATA 
                    + " ON (" + Views.DATA + "." + Data._ID
                        + "=" + Tables.CALLS + "." + Calls.DATA_ID + ")" 
                + " LEFT JOIN (SELECT " + SearchIndexColumns.CONTACT_ID + " AS " + SNIPPET_CONTACT_ID
                    + " FROM " + Tables.SEARCH_INDEX
                    + " WHERE " + SearchIndexColumns.NAME + " MATCH '*" + nomalizeName + "*') "
                    + " ON (" + SNIPPET_CONTACT_ID
                        + "=" + Views.DATA + "." + Data.CONTACT_ID + ")";
            	
            	qb.setTables(table);
            	qb.setProjectionMap(sCallsJoinDataViewProjectionMap);
            	
                StringBuilder sb = new StringBuilder();
                sb.append(Tables.CALLS + "." + Calls.NUMBER + " GLOB '*") ;
                sb.append(query);
                sb.append("*' OR (" 
                        + SNIPPET_CONTACT_ID + ">0 AND " 
                        + Tables.CALLS + "." + Calls.RAW_CONTACT_ID + ">0) ");
                qb.appendWhere(sb);
                groupBy = Tables.CALLS + "." + Calls._ID;
            	
                
               Log.d(TAG, " CallLogProvider.CALLS_SEARCH_FILTER, table="+table+", query="+query+", sb="+sb.toString());
               break;
            }

            case GN_CALLS_JION_DATA_VIEW_MATCH_NUMBER:
            case CALLS_JION_DATA_VIEW: {
                qb.setTables(mstableCallsJoinData);
                qb.setProjectionMap(sCallsJoinDataViewProjectionMap);
                qb.setStrict(true);
                break;
            }
            
            case CALLS_JION_DATA_VIEW_ID: {
                qb.setTables(mstableCallsJoinData);
                qb.setProjectionMap(sCallsJoinDataViewProjectionMap);
                qb.setStrict(true);
                selectionBuilder.addClause(getEqualityClause(Tables.CALLS + "." +Calls._ID,
                        parseCallIdFromUri(uri)));
                break;
            }
            
            case GN_CALLS_JION_DATA_VIEW: {
                qb.setTables(mstableCallsJoinData);
                qb.setProjectionMap(sCallsJoinDataViewProjectionMap);
                qb.setStrict(true);
                groupBy = Tables.CALLS + "." + Calls.NUMBER;
                break;
            }

            case SEARCH_SUGGESTIONS: {
            	Log.d(TAG,"CallLogProvider.SEARCH_SUGGESTIONS");
            	return mCallLogSearchSupport.handleSearchSuggestionsQuery(db, uri, limit);
            }
            
            case SEARCH_SHORTCUT: {
                Log.d(TAG,"CallLogProvider.SEARCH_SHORTCUT. Uri:" + uri);
                String callId = uri.getLastPathSegment();
                String filter = uri.getQueryParameter(SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA);
                return mCallLogSearchSupport.handleSearchShortcutRefresh(db, projection, callId, filter);
            }

            case AURORA_DIALER_CALLLOG_SEARCH:{
                qb.setTables(mstableCallsJoinData);
                qb.setProjectionMap(sCallsJoinDataViewProjectionMap);
            	
                StringBuilder sb = new StringBuilder();
                sb.append(Tables.CALLS + "." + Calls.NUMBER + " GLOB '*") ;
                String query = uri.getPathSegments().get(2);
                sb.append(query);
//                sb.append("*' AND " + Tables.CALLS + "." + Calls.RAW_CONTACT_ID + " is null ");
                sb.append("*' ");
                qb.appendWhere(sb);
            	groupBy = Calls.NUMBER;
            	break;
            }
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
        Log.d(TAG, "   In call log providers, selection="+auroraSelection+",  selectionBuilder="+selectionBuilder.build());
        Cursor c = qb.query(db, projection, selectionBuilder.build(), selectionArgs, groupBy, null, sortOrder, null);
        
    	//Gionee:huangzy 20120906 add for CR00688166 start
        switch (match) {
		case GN_CALLS_JION_DATA_VIEW_MATCH_NUMBER:
			c = combineCalllogMatchNumber(c);
			break;

		default:
			break;
		}
    	//Gionee:huangzy 20120906 add for CR00688166 end
        
        if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), GnCallLog.CONTENT_URI);
        }
        return c;
    }

    @Override
    public String getType(Uri uri) {
        int match = sURIMatcher.match(uri);
        switch (match) {
            case CALLS:
                return Calls.CONTENT_TYPE;
            case CALLS_ID:
                return Calls.CONTENT_ITEM_TYPE;
            case CALLS_FILTER:
                return Calls.CONTENT_TYPE;
            case CALLS_SEARCH_FILTER:
            	return Calls.CONTENT_TYPE;
            case SEARCH_SUGGESTIONS:
            	return Calls.CONTENT_TYPE;
            case  AURORA_DIALER_CALLLOG_SEARCH:
              	return Calls.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri retUri = null;
        long lStart = System.currentTimeMillis();
        Log.i(TAG, "insert()+ ===========");
        checkForSupportedColumns(sCallsProjectionMap, values);
        // Inserting a voicemail record through call_log requires the voicemail
        // permission and also requires the additional voicemail param set.
        if (hasVoicemailValue(values)) {
            checkIsAllowVoicemailRequest(uri);
            mVoicemailPermissions.checkCallerHasFullAccess();
        }
        if (mCallsInserter == null) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            mCallsInserter = new DatabaseUtils.InsertHelper(db, Tables.CALLS);
        }

        ContentValues copiedValues = new ContentValues(values);

        // Add the computed fields to the copied values.
        mCallLogInsertionHelper.addComputedValues(copiedValues);

        /*
         * Feature Fix by Mediatek Begin
         *  
         * Original Android code 
         * long rowId = getDatabaseModifier(mCallsInserter).insert(copiedValues); 
         * if (rowId > 0) { 
         *     return ContentUris.withAppendedId(uri, rowId); 
         * } 
         */

        log("[insert]uri: " + uri);
        SQLiteDatabase db = null;
        try {
            db = mDbHelper.getWritableDatabase();
        } catch (SQLiteDiskIOException err) {
            err.printStackTrace();
            log("insert()- 1 =========== Time:" + (System.currentTimeMillis() - lStart));
            return null;
        }

        final String strInsNumber = values.getAsString(Calls.NUMBER);
        
        // add for reject by wangth 20140715
        boolean updateMark = false;
        if (ContactsProvidersApplication.sIsAuroraRejectSupport) {
        	if (values != null && values.containsKey("type") 
        			&& values.getAsInteger("type") != 1 && values.getAsInteger("type") != 3) {
        	    updateMark = true;
        	}
        	
            if (values != null && !values.containsKey("area")) {
                
                String area = YuloreUtil.getArea(getContext(), strInsNumber);
                if (area != null) {
                    copiedValues.put("area", area);
                }
            }
        }
        
        try {
            db.beginTransaction();
            long privacyId = 0;
            if (values != null && values.containsKey("privacy_id")) {
            	privacyId = values.getAsLong("privacy_id");
            }
            // Get all same call log id from calls table 
            Cursor allCallLogCursorOfSameNum = db.query(Tables.CALLS, new String[] {Calls._ID, Calls.DATE}, 
                    " CASE WHEN " + Calls.SIM_ID + "=" + CALL_TYPE_SIP + " THEN " + Calls.NUMBER + "='"
                        + strInsNumber + "'" + " ELSE PHONE_NUMBERS_EQUAL(" + Calls.NUMBER + ", '" + strInsNumber
                        + "') END And " + AURORA_PRIVATE_ID + " = " + privacyId, null, null, null, "_id DESC", null);
    
            long updateRowID = -1;
            long latestRowID = -1;
            StringBuilder noNamebuilder = new StringBuilder();
            if (allCallLogCursorOfSameNum != null) {
                if (allCallLogCursorOfSameNum.moveToFirst()) {
                    latestRowID = allCallLogCursorOfSameNum.getLong(0);
                    if (allCallLogCursorOfSameNum.getLong(1) > values.getAsLong(Calls.DATE)) {
                        updateRowID = latestRowID;
                    }
                    noNamebuilder.append(latestRowID);
                }
                while (allCallLogCursorOfSameNum.moveToNext()) {
                    noNamebuilder.append(",");
                    noNamebuilder.append(allCallLogCursorOfSameNum.getInt(0));
                }
                allCallLogCursorOfSameNum.close();
                allCallLogCursorOfSameNum = null;
            }

            // Get data_id and raw_contact_id information about contacts
            boolean bIsUriNumber = PhoneNumberUtils.isUriNumber(strInsNumber);
            Cursor nameCursor = null;
            boolean numberCheckFlag = false;
            long dataId = -1;
            long rawContactId = -1;
            boolean bSpecialNumber = (strInsNumber.equals("-1")
                    || strInsNumber.equals("-2")
                    || strInsNumber.equals("-3"));
            if (values != null && values.containsKey(Calls.RAW_CONTACT_ID) && !bSpecialNumber) {
                numberCheckFlag = true;
            	dataId = values.getAsLong(Calls.DATA_ID);
            	rawContactId = values.getAsLong(Calls.RAW_CONTACT_ID);
                copiedValues.put(Calls.DATA_ID, dataId);
                copiedValues.put(Calls.RAW_CONTACT_ID, rawContactId);	            
            } else {
	            if (bIsUriNumber) {
	                // Get internet call number contact information
					nameCursor = db.query(Views.DATA, new String[] { Data._ID,
							Data.RAW_CONTACT_ID }, Data.DATA1 + "='" + strInsNumber
							+ "' AND " + Data.MIMETYPE + "='" + SipAddress.CONTENT_ITEM_TYPE + "'", 
							null, null, null, null);
	            } else {
	                // Get non-internet call number contact information
	                //Do not strip the special number. Otherwise, UI would not get the right value.
	                /*
	                 * Use key "lookup" to get right data_id and raw_contact_id. 
	                 * The former one which uses "normalizedNumber" to search 
	                 * phone_lookup table would cause to get the dirty data.
	                 * 
	                 * The previous code is:
	                 *   nameCursor = getContext().getContentResolver().query(
	                 *           Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(strInsNumber)),
	                 *           new String[] {PhoneLookupColumns.DATA_ID, PhoneLookupColumns.RAW_CONTACT_ID},
	                 *           null, null, null);
	                 */
	                nameCursor = queryPhoneLookupByNumber(db, strInsNumber,
	                        new String[] { PhoneLookupColumns.DATA_ID, PhoneLookupColumns.RAW_CONTACT_ID },
	                        null, null, null, null, null , "1");
	            }
	            if ((!bSpecialNumber) && (null != nameCursor) && (nameCursor.moveToFirst())) {
	                numberCheckFlag = true;
	                dataId = nameCursor.getLong(0);
	                rawContactId = nameCursor.getLong(1);
	                // Put the data_id and raw_contact_id into copiedValues to insert
	                copiedValues.put(Calls.DATA_ID, dataId);
	                copiedValues.put(Calls.RAW_CONTACT_ID, rawContactId);
	            }
	            if (null != nameCursor) {
	                nameCursor.close();
	            }
            }
            
            //Gionee:huangzy 20121128 add for CR00736966 start
            if (ContactsProvidersApplication.sIsGnSyncSupport) {
            	ContactsDatabaseHelper.writeGnVersion(getContext(), copiedValues);            	
            }
            //Gionee:huangzy 20121128 add for CR00736966 end

            // rowId is new callLog ID, and latestRowID is old callLog ID for the same number.
            log("insert into calls table:" + copiedValues);
            final long rowId = getDatabaseModifier(mCallsInserter).insert(copiedValues);
            log("inserted into calls table. new id:" + rowId);
    
            if (FeatureOption.MTK_SEARCH_DB_SUPPORT == true) {
                if (updateRowID == -1) {
                    updateRowID = rowId;
                }
                log("[insert] insert updateRowID:" + updateRowID + " latestRowID:" + latestRowID + " rowId:" + rowId);
            }
            if (rowId > 0 && FeatureOption.MTK_SEARCH_DB_SUPPORT == true) {
                if (numberCheckFlag) {
                    /*
                     * update old NO Name CallLog records that share the same
                     * number with the new inserted one, if exist. String
                     * updateNoNameCallLogStmt = Calls.DATA_ID + " IS NULL " +
                     * " AND PHONE_NUMBERS_EQUAL(" + Calls.NUMBER + ",'" +
                     * number + "') "; update All CallLog records that share the
                     * same number with the new inserted one, if exist.
                     */
                    if (noNamebuilder != null && noNamebuilder.length() > 0) {
                        // update NO Name CallLog records of the inserted CallLog
                        log("[insert]updated calls record. number:" + strInsNumber + " data_id:"
                                + dataId + " raw_contact_id:" + rawContactId);
                        ContentValues updateNoNameCallLogValues = new ContentValues();
                        updateNoNameCallLogValues.put(Calls.RAW_CONTACT_ID, rawContactId);
                        updateNoNameCallLogValues.put(Calls.DATA_ID, dataId);	
                        int updateNoNameCallLogCount = db.update(Tables.CALLS, updateNoNameCallLogValues,
                                Calls._ID + " IN (" + noNamebuilder.toString() + ")", null);                 
                        log("[insert]updated NO Name CallLog records of the inserted CallLog. Count:" + updateNoNameCallLogCount);
                        // wangth 20140410 modify for aurora begin
                        ContactsDatabaseHelper.updateCalls(db);
                        // wangth 20140410 modify for aurora end
                    }
                }
            }
            if (rowId > 0) {
                notifyDialerSearchChange();
                retUri = ContentUris.withAppendedId(uri, rowId);
            }
            db.setTransactionSuccessful();
            
            // add for update mark
            if (updateMark && rowId > 0 && ContactsProvidersApplication.sIsAuroraRejectSupport) {
                new Thread(new Runnable() {
                    
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        updateMark(rowId, strInsNumber);
                        notifyDialerSearchChange();
                    }
                }).start();
            }
        } finally {
            db.endTransaction();
        }
        Log.i(TAG, "insert()  =========== Uri:" + uri);
        Log.i(TAG, "insert()- =========== Time:" + (System.currentTimeMillis() - lStart));
        return retUri;
        /*
		 * Feature Fix by Mediatek End
		 */
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        checkForSupportedColumns(sCallsProjectionMap, values);
        // Request that involves changing record type to voicemail requires the
        // voicemail param set in the uri.
        if (hasVoicemailValue(values)) {
            checkIsAllowVoicemailRequest(uri);
        }
        
    	//aurora add liguangyu 201411029 for privacy start
        selection = parseCallLogSelection(selection, false); 
 	   //aurora add liguangyu 201411029 for privacy end
        

        SelectionBuilder selectionBuilder = new SelectionBuilder(selection);
        checkVoicemailPermissionAndAddRestriction(uri, selectionBuilder);

        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int matchedUriId = sURIMatcher.match(uri);
        switch (matchedUriId) {
            case CALLS:
                break;

            case CALLS_ID:
                selectionBuilder.addClause(getEqualityClause(Calls._ID, parseCallIdFromUri(uri)));
                break;

            default:
                throw new UnsupportedOperationException("Cannot update URL: " + uri);
        }

        return getDatabaseModifier(db).update(Tables.CALLS, values, selectionBuilder.build(),
                selectionArgs);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
    	//aurora add liguangyu 201411029 for privacy start
        selection = parseCallLogSelection(selection, false); 
 	   //aurora add liguangyu 201411029 for privacy end
        SelectionBuilder selectionBuilder = new SelectionBuilder(selection);
        checkVoicemailPermissionAndAddRestriction(uri, selectionBuilder);
        
        SQLiteDatabase db = null;
        try {
            db = mDbHelper.getWritableDatabase();
        } catch (SQLiteDiskIOException err) {
            err.printStackTrace();
            return 0;
        }
        
        
        final int matchedUriId = sURIMatcher.match(uri);
        switch (matchedUriId) {
        case CALLS: {
            int count = getDatabaseModifier(db).delete(Tables.CALLS,
                    selectionBuilder.build(), selectionArgs);
            log("[delete] delete Calls. count: " + count);
            if (count > 0) {
            	notifyDialerSearchChange();
            }
            return count;
        }
		
        default:
            throw new UnsupportedOperationException("Cannot delete that URL: " + uri);
        }
    }

    // Work around to let the test code override the context. getContext() is final so cannot be
    // overridden.
    protected Context context() {
        return getContext();
    }

    /**
     * Returns a {@link DatabaseModifier} that takes care of sending necessary notifications
     * after the operation is performed.
     */
    private DatabaseModifier getDatabaseModifier(SQLiteDatabase db) {
        return new DbModifierWithNotification(Tables.CALLS, db, context());
    }

    /**
     * Same as {@link #getDatabaseModifier(SQLiteDatabase)} but used for insert helper operations
     * only.
     */
    private DatabaseModifier getDatabaseModifier(DatabaseUtils.InsertHelper insertHelper) {
        return new DbModifierWithNotification(Tables.CALLS, insertHelper, context());
    }

    private boolean hasVoicemailValue(ContentValues values) {
        return values.containsKey(Calls.TYPE) &&
                values.getAsInteger(Calls.TYPE).equals(Calls.VOICEMAIL_TYPE);
    }

    /**
     * Checks if the supplied uri requests to include voicemails and take appropriate
     * action.
     * <p> If voicemail is requested, then check for voicemail permissions. Otherwise
     * modify the selection to restrict to non-voicemail entries only.
     */
    private void checkVoicemailPermissionAndAddRestriction(Uri uri,
            SelectionBuilder selectionBuilder) {
        if (isAllowVoicemailRequest(uri)) {
            mVoicemailPermissions.checkCallerHasFullAccess();
        } else {
            selectionBuilder.addClause(EXCLUDE_VOICEMAIL_SELECTION);
        }
    }

    /**
     * Determines if the supplied uri has the request to allow voicemails to be
     * included.
     */
    private boolean isAllowVoicemailRequest(Uri uri) {
        return uri.getBooleanQueryParameter(Calls.ALLOW_VOICEMAILS_PARAM_KEY, false);
    }

    /**
     * Checks to ensure that the given uri has allow_voicemail set. Used by
     * insert and update operations to check that ContentValues with voicemail
     * call type must use the voicemail uri.
     * @throws IllegalArgumentException if allow_voicemail is not set.
     */
    private void checkIsAllowVoicemailRequest(Uri uri) {
        if (!isAllowVoicemailRequest(uri)) {
            throw new IllegalArgumentException(
                    String.format("Uri %s cannot be used for voicemail record." +
                            " Please set '%s=true' in the uri.", uri,
                            Calls.ALLOW_VOICEMAILS_PARAM_KEY));
        }
    }

   /**
    * Parses the call Id from the given uri, assuming that this is a uri that
    * matches CALLS_ID. For other uri types the behaviour is undefined.
    * @throws IllegalArgumentException if the id included in the Uri is not a valid long value.
    */
    private String parseCallIdFromUri(Uri uri) {
        try {
            Long id = Long.valueOf(uri.getPathSegments().get(1));
            return id.toString();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid call id in uri: " + uri, e);
        }
    }

    
	// The fillowing lines are provided and maintained by Mediatek inc.
    private static final String TAG = "CallLogProvider";
    private static final boolean DBG_DIALER_SEARCH = ContactsFeatureConstants.DBG_DIALER_SEARCH;
    private static final int DS_NAMERECORD = 1;
    private static final int DS_NUMBERRECORD = 2;

    // Reference the com.android.phone.CallNotifier.CALL_TYPE_SIP
    // the Call Type of SIP Call
    private static final int CALL_TYPE_SIP = -2;
    
    private void notifyDialerSearchChange() {
        getContext().getContentResolver().notifyChange(
        		Uri.parse("content://com.android.contacts.dialer_search/callLog/"), null, false);
//        getContext().getContentResolver().notifyChange(
//        		Uri.parse("content://call_log/callsjoindataview"), null, false);
    }
	private void log(String msg) {
		if (DBG_DIALER_SEARCH)
			Log.d(TAG, " " + msg);
	}

	private Cursor queryPhoneLookupByNumber(SQLiteDatabase db, String number, String []projection,
	        String selection, String[] selectionArgs, String groupBy,
            String having, String sortOrder, String limit) {
	    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String numberE164 = PhoneNumberUtils.formatNumberToE164(number,
                mDbHelper.getCurrentCountryIso());
        String normalizedNumber = PhoneNumberUtils.normalizeNumber(number);
        mDbHelper.buildPhoneLookupAndContactQuery(qb, normalizedNumber, numberE164);
        qb.setStrict(true);
        boolean foundResult = false;
        Cursor c = qb.query(db, projection, selection, selectionArgs, groupBy, having,
                sortOrder, limit);
        try {
            if (c.getCount() > 0) {
                foundResult = true;
                return c;
            } else {
                qb = new SQLiteQueryBuilder();
                mDbHelper.buildMinimalPhoneLookupAndContactQuery(qb, normalizedNumber);
                qb.setStrict(true);
            }
        } finally {
            if (!foundResult) {
                // We'll be returning a different cursor, so close this one.
                c.close();
            }
        }
	    return qb.query(db, projection, selection, selectionArgs, groupBy, having,
                sortOrder, limit);
	}
	// The fillowing lines are provided and maintained by Mediatek inc.
	
	private static final int GN_CALLS_JION_DATA_VIEW = 10;
	//Gionee:huangzy 20120906 add for CR00688166 start
	public static final String GN_CALLS_COUNT_BY_NUMBER = "calls_count";
	public static final String GN_CALLS_COUNT_BY_NUMBER_IDS = "calls_count_ids";
	private static final int GN_CALLS_JION_DATA_VIEW_MATCH_NUMBER = 11;
	//Gionee:huangzy 20120906 add for CR00688166 end
	static {
		sURIMatcher.addURI(GnCallLog.AUTHORITY, "gncallsjoindataview", GN_CALLS_JION_DATA_VIEW);
		//Gionee:huangzy 20120906 modify for CR00688166 start
		sURIMatcher.addURI(GnCallLog.AUTHORITY, "gncallsjoindataview_matchnumber", GN_CALLS_JION_DATA_VIEW_MATCH_NUMBER);
		/*sCallsJoinDataViewProjectionMap.put("count(number)", "count(number)");*/
		sCallsJoinDataViewProjectionMap.put(GN_CALLS_COUNT_BY_NUMBER, "count(number) AS " + GN_CALLS_COUNT_BY_NUMBER);
		//Gionee:huangzy 20120906 modify for CR00688166 end
	}
	
	public Cursor gnQuery(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {		
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Tables.CALLS);
        qb.setProjectionMap(sCallsProjectionMap);
        qb.setStrict(true);

        SelectionBuilder selectionBuilder = new SelectionBuilder(selection);
        checkVoicemailPermissionAndAddRestriction(uri, selectionBuilder);

        int match = sURIMatcher.match(uri);
        
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        //Gionee:huanzy 20130128 add for CR00767558 start
        if (!db.isOpen()) {
        	Log.i("James", "gnQuery return null if database is not opened!");
        	return null;
        }
        //Gionee:huanzy 20130128 add for CR00767558 end
        
        String limit = getLimit(uri);
        String groupBy = null;
        Log.i(TAG,"match == " +match);
        switch (match) {
            case CALLS:
                break;

            case CALLS_ID: {
                selectionBuilder.addClause(getEqualityClause(Calls._ID,
                        parseCallIdFromUri(uri)));
                break;
            }

            case CALLS_FILTER: {
                String phoneNumber = uri.getPathSegments().get(2);
                qb.appendWhere("PHONE_NUMBERS_EQUAL(number, ");
                qb.appendWhereEscapeString(phoneNumber);
                qb.appendWhere(mUseStrictPhoneNumberComparation ? ", 1)" : ", 0)");
                break;
            }
            case CALLS_SEARCH_FILTER: {
                String query = uri.getPathSegments().get(2);
                String nomalizeName = NameNormalizer.normalize(query);
                final String SNIPPET_CONTACT_ID = "snippet_contact_id";
            	String table = Tables.CALLS
                + " LEFT JOIN " + Views.DATA 
                    + " ON (" + Views.DATA + "." + Data._ID
                        + "=" + Tables.CALLS + "." + Calls.DATA_ID + ")" 
                + " LEFT JOIN (SELECT " + SearchIndexColumns.CONTACT_ID + " AS " + SNIPPET_CONTACT_ID
                    + " FROM " + Tables.SEARCH_INDEX
                    + " WHERE " + SearchIndexColumns.NAME + " MATCH '*" + nomalizeName + "*') "
                    + " ON (" + SNIPPET_CONTACT_ID
                        + "=" + Views.DATA + "." + Data.CONTACT_ID + ")";
            	
            	qb.setTables(table);
            	qb.setProjectionMap(sCallsJoinDataViewProjectionMap);
            	
                StringBuilder sb = new StringBuilder();
                sb.append(Tables.CALLS + "." + Calls.NUMBER + " GLOB '*") ;
                sb.append(query);
                sb.append("*' OR (" 
                        + SNIPPET_CONTACT_ID + ">0 AND " 
                        + Tables.CALLS + "." + Calls.RAW_CONTACT_ID + ">0) ");
                qb.appendWhere(sb);
                groupBy = Tables.CALLS + "." + Calls._ID;
            	
                
               Log.d(TAG, " CallLogProvider.CALLS_SEARCH_FILTER, table="+table+", query="+query+", sb="+sb.toString());
               break;
            }
        	//Gionee:huangzy 20120906 add for CR00688166 start
            case GN_CALLS_JION_DATA_VIEW_MATCH_NUMBER:
            //Gionee:huangzy 20120906 add for CR00688166 end
            case CALLS_JION_DATA_VIEW: {
                qb.setTables(mstableCallsJoinData);
                qb.setProjectionMap(sCallsJoinDataViewProjectionMap);
                qb.setStrict(true);
                break;
            }
            
            case GN_CALLS_JION_DATA_VIEW: {
                qb.setTables(mstableCallsJoinData);
                qb.setProjectionMap(sCallsJoinDataViewProjectionMap);
                qb.setStrict(true);
                groupBy = Tables.CALLS + "." + Calls.NUMBER;
                break;
            }
            
            case CALLS_JION_DATA_VIEW_ID: {
                qb.setTables(mstableCallsJoinData);
                qb.setProjectionMap(sCallsJoinDataViewProjectionMap);
                qb.setStrict(true);
                selectionBuilder.addClause(getEqualityClause(Tables.CALLS + "." +Calls._ID,
                        parseCallIdFromUri(uri)));
                break;
            }

            case SEARCH_SUGGESTIONS: {
            	Log.d(TAG,"CallLogProvider.SEARCH_SUGGESTIONS");
            	return mCallLogSearchSupport.handleSearchSuggestionsQuery(db, uri, limit);
            }
            
            case SEARCH_SHORTCUT: {
                Log.d(TAG,"CallLogProvider.SEARCH_SHORTCUT. Uri:" + uri);
                String callId = uri.getLastPathSegment();
                String filter = uri.getQueryParameter(SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA);
                return mCallLogSearchSupport.handleSearchShortcutRefresh(db, projection, callId, filter);
            }

            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
        Log.d(TAG, "   In call log providers, selection="+selection+",  selectionBuilder="+selectionBuilder.build());
        Cursor c = qb.query(db, projection, selectionBuilder.build(), selectionArgs, groupBy, null, sortOrder, null);
        
    	//Gionee:huangzy 20120906 add for CR00688166 start
        switch (match) {
		case GN_CALLS_JION_DATA_VIEW_MATCH_NUMBER:
			c = combineCalllogMatchNumber(c);
			break;

		default:
			break;
		}
    	//Gionee:huangzy 20120906 add for CR00688166 end
        
        if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), GnCallLog.CONTENT_URI);
        }
        return c;
    }
	
	//Gionee:huangzy 20120906 add for CR00688166 start
	private Cursor combineCalllogMatchNumber(Cursor src) {
		if (null == src || !src.moveToFirst()) {
			return src;
		}
		
		int columnCount = src.getColumnCount();
		int cursorCount = src.getCount();
		ArrayList<Object[]> raws = new ArrayList<Object[]>();
		int numberIndex = src.getColumnIndex(sCallsJoinDataViewProjectionMap.get(Calls.NUMBER));
		final int MATCH_LEN = ContactsProvidersApplication.GN_MATCH_CONTACTS_NUMBER_LENGTH;
		do {
    		String curNumber = src.getString(numberIndex);
    		
    		//ps. no check countryISO here!!!
    		int curNumberLen = curNumber.length();
    		if (MATCH_LEN < curNumberLen) {
    			curNumber = curNumber.substring(curNumberLen - MATCH_LEN, curNumberLen);
    		}
    		
    		boolean isNumberMatch = false;
    		String savedNumber = null;
    		int savedNumberLen = 0;
    		for (Object[] r : raws) {
    			savedNumber = r[numberIndex].toString();
    			savedNumberLen = savedNumber.length();
    			if (MATCH_LEN > curNumberLen) {
    				isNumberMatch = curNumber.equals(savedNumber);
    			} else {
    				isNumberMatch = savedNumber.endsWith(curNumber);
    			}
    			if (isNumberMatch) {
    				r[columnCount] = (Integer)(r[columnCount]) + 1;
    				r[columnCount+1] =r[columnCount+1]+","+src.getString(0);
    				break;
    			}
    		}
    		
    		if (!isNumberMatch) {
    			
    			Object[] objs = new Object[columnCount + 2];
    			for (int k = 0; k < columnCount; k++) {
    				switch (src.getType(k)) {
					case Cursor.FIELD_TYPE_INTEGER:
						objs[k] = src.getLong(k);	
						break;

					default:
						objs[k] = src.getString(k);
						break;
					}
    			}
    			objs[columnCount] = Integer.valueOf(1);
    			objs[columnCount+1] = src.getString(0);
    			raws.add(objs);
    		}    		
    	} while (src.moveToNext());
		
		MatrixCursor combinedCursor = null;
		{
			String[] columnNames = new String[columnCount + 2];
			for (int i = 0; i < columnCount; i++) {
				columnNames[i] = src.getColumnName(i);
			}
			columnNames[columnCount] = GN_CALLS_COUNT_BY_NUMBER;
			columnNames[columnCount+1] = GN_CALLS_COUNT_BY_NUMBER_IDS;
			combinedCursor = new MatrixCursor(columnNames);
		}
		 
		for (Object[] r : raws) {
			combinedCursor.addRow(r);
		}
		src.close();
		return combinedCursor;
	}
	//Gionee:huangzy 20120906 add for CR00688166 end
	
	private void updateMark(long id, String number) {
	    ContentValues markValues = new ContentValues();
        String mark = null;
        int user_mark = -1;
        boolean hasMark = false;
        Log.d(TAG, "number = " + number);

            mark = YuloreUtil.getUserMark(getContext(), number);
            Log.d(TAG, "user mark = " + mark);
            if (mark != null) {
                markValues.put("mark", mark);
                markValues.put("user_mark", -1);
                hasMark = true;
            }
            
            if (!hasMark) {
                mark = YuloreUtil.getMarkContent(getContext(), number);
                user_mark = YuloreUtil.getMarkNumber(getContext(), number);
                if (mark != null) {
                    markValues.put("mark", mark);
                    markValues.put("user_mark", user_mark);
                    hasMark = true;
                }
            }
        
        try {
            Log.d(TAG, "mark = " + mark + "  user_mark = " + user_mark);
            if (mark != null) {
                int count = getContext().getContentResolver().update(Calls.CONTENT_URI, markValues, "_id = " + id,
                        null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	//aurora add liguangyu 201411029 for privacy start
    private static final String AURORA_PRIVATE_ID = "privacy_id";
    private String parseCallLogSelection(String selection, boolean flag) {
    	if (!ContactsProvidersApplication.sIsAuroraPrivacySupport) {
    		return selection;
    	}
        String sel = selection;
        String defaultStr = "=0";
        if (flag) {
            defaultStr = ">-1";
        }
        
        if (!TextUtils.isEmpty(sel)) {
            if (!sel.contains(AURORA_PRIVATE_ID)) {
                sel = "(" + sel + ") AND " + AURORA_PRIVATE_ID + defaultStr;
            }
        } else {
            sel = AURORA_PRIVATE_ID + defaultStr;
        }
        
        Log.i(TAG, "sel = " + sel);
        
        return sel;
    }
    //aurora add liguangyu 201411029 for privacy end
	
}
