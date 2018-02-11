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

import com.android.common.content.SyncStateContentProviderHelper;
import com.android.common.io.MoreCloseables;
import com.android.providers.contacts.database.ContactsTableUtil;
import com.android.providers.contacts.ContactAggregator.AggregationSuggestionParameter;
import com.android.providers.contacts.ContactLookupKey.LookupKeySegment;
import com.android.providers.contacts.ContactsDatabaseHelper.AccountsColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.AggregatedPresenceColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.AggregationExceptionColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.Clauses;
import com.android.providers.contacts.ContactsDatabaseHelper.ContactsColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.ContactsStatusUpdatesColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.DataColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.DataUsageStatColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.GnSyncColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.GroupsColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.Joins;
import com.android.providers.contacts.ContactsDatabaseHelper.NameLookupColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.NameLookupType;
import com.android.providers.contacts.ContactsDatabaseHelper.PhoneColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.PhoneLookupColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.PhotoFilesColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.PresenceColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.RawContactsColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.SearchIndexColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.SettingsColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.StatusUpdatesColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.StreamItemPhotosColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.StreamItemsColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.Tables;
import com.android.providers.contacts.ContactsDatabaseHelper.Views;
import com.android.providers.contacts.database.DeletedContactsTableUtil;
import com.android.providers.contacts.DataRowHandler.DataUpdateQuery;
import com.android.providers.contacts.PhotoStore.Entry;
import com.android.providers.contacts.SearchIndexManager.FtsQueryBuilder;
import com.android.providers.contacts.util.Clock;
import com.android.providers.contacts.util.DbQueryUtils;
import com.android.vcard.VCardComposer;
import com.android.vcard.VCardConfig;
import com.google.android.collect.Lists;
import com.google.android.collect.Maps;
import com.google.android.collect.Sets;
import com.google.common.annotations.VisibleForTesting;
import com.mediatek.providers.contacts.ContactsFeatureConstants;
import com.mediatek.providers.contacts.ContactsFeatureConstants.FeatureOption;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.IContentService;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SyncAdapterType;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ProviderInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.AbstractCursor;
import android.database.CrossProcessCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.CursorWrapper;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.MatrixCursor.RowBuilder;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseInputStream;
import android.os.Process;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.AggregationExceptions;
import gionee.provider.GnContactsContract.Authorization;
import gionee.provider.GnContactsContract.CommonDataKinds;
import gionee.provider.GnContactsContract.CommonDataKinds.Email;
import gionee.provider.GnContactsContract.CommonDataKinds.GroupMembership;
import gionee.provider.GnContactsContract.CommonDataKinds.Im;
import gionee.provider.GnContactsContract.CommonDataKinds.Nickname;
import gionee.provider.GnContactsContract.CommonDataKinds.Note;
import gionee.provider.GnContactsContract.CommonDataKinds.Organization;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import gionee.provider.GnContactsContract.CommonDataKinds.Photo;
import gionee.provider.GnContactsContract.CommonDataKinds.SipAddress;
import gionee.provider.GnContactsContract.CommonDataKinds.StructuredName;
import gionee.provider.GnContactsContract.CommonDataKinds.StructuredPostal;
import gionee.provider.GnContactsContract.ContactCounts;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.Contacts.AggregationSuggestions;
import gionee.provider.GnContactsContract.DialerSearch;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.DataUsageFeedback;
import gionee.provider.GnContactsContract.Directory;
import gionee.provider.GnContactsContract.DisplayPhoto;
import gionee.provider.GnContactsContract.Groups;
import gionee.provider.GnContactsContract.Intents;
import gionee.provider.GnContactsContract.PhoneLookup;
import gionee.provider.GnContactsContract.PhotoFiles;
import gionee.provider.GnContactsContract.Profile;
import gionee.provider.GnContactsContract.ProviderStatus;
import gionee.provider.GnContactsContract.RawContacts;
import gionee.provider.GnContactsContract.RawContactsEntity;
import gionee.provider.GnContactsContract.SearchSnippetColumns;
import gionee.provider.GnContactsContract.Settings;
import gionee.provider.GnContactsContract.StatusUpdates;
import gionee.provider.GnContactsContract.StreamItemPhotos;
import gionee.provider.GnContactsContract.StreamItems;
import gionee.os.storage.GnStorageManager;
import android.provider.CallLog.Calls;
import android.provider.OpenableColumns;
import android.provider.SyncStateContract;

import com.android.providers.contacts.ContactsProvidersApplication;
import com.android.providers.contacts.util.PhoneNumberUtils;

import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.FileInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream.GetField;
import java.io.FileOutputStream;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import android.util.Base64;


/* 
 * New Feature by Mediatek Begin.
 * Descriptions: crete sim/usim contact
 */















import gionee.provider.GnTelephony.SIMInfo;

import com.mediatek.providers.contacts.SimCardUtils;
import com.mediatek.providers.contacts.SimCardUtils.SimType;
import com.privacymanage.service.AuroraPrivacyUtils;
import com.android.internal.telephony.TelephonyIntents;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import gionee.provider.GnContactsContract.CommonDataKinds.Website;

import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * New Feature by Mediatek End.
 */

/**
 * Contacts content provider. The contract between this provider and applications
 * is defined in {@link ContactsContract}.
 */
public class ContactsProvider2 extends AbstractContactsProvider
        implements OnAccountsUpdateListener {

    private static final String TAG = "ContactsProvider";
    
    private static boolean mIsGnContactsSupport = true;//SystemProperties.get("ro.gn.contacts.support").equals("yes");
    

    // Gionee lixiaohu 20120809 add for CR00672361 start
//    private static final boolean gnFLYflag = SystemProperties.get("ro.gn.oversea.custom").equals("RUSSIA_FLY");
    // Gionee liuyanbo 20120809 add for CR00672361 end

    //Gionee qinkai 2012-09-13 added for CR00692942 start
//    private static final boolean gnNGMflag = SystemProperties.get("ro.gn.oversea.custom").equals("ITALY_NGM");
    //Gionee qinkai 2012-09-13 added for CR00692942 end
	
    //Gionee guoxiaotian 2012-09-18 added for CR00696152 start
//    private static final boolean gnVFflag = SystemProperties.get("ro.gn.oversea.custom").equals("VISUALFAN");
    //Gionee guoxiaotian 2012-09-18 added for CR00696152 end
    
    private static final String AURORA_DEFAULT_PRIVACY_COLUMN = "is_privacy";
    private static final String AURORA_PRIVATE_NOTIFICATION = "call_notification_type";
    
    private static final boolean VERBOSE_LOGGING = /*Log.isLoggable(TAG, Log.VERBOSE)*/true;

    private static final int BACKGROUND_TASK_INITIALIZE = 0;
    private static final int BACKGROUND_TASK_OPEN_WRITE_ACCESS = 1;
    private static final int BACKGROUND_TASK_IMPORT_LEGACY_CONTACTS = 2;
    private static final int BACKGROUND_TASK_UPDATE_ACCOUNTS = 3;
    private static final int BACKGROUND_TASK_UPDATE_LOCALE = 4;
    private static final int BACKGROUND_TASK_UPGRADE_AGGREGATION_ALGORITHM = 5;
    private static final int BACKGROUND_TASK_UPDATE_SEARCH_INDEX = 6;
    private static final int BACKGROUND_TASK_UPDATE_PROVIDER_STATUS = 7;
    private static final int BACKGROUND_TASK_UPDATE_DIRECTORIES = 8;
    private static final int BACKGROUND_TASK_CHANGE_LOCALE = 9;
    private static final int BACKGROUND_TASK_CLEANUP_PHOTOS = 10;
    private static final int BACKGROUND_TASK_CLEAN_DELETE_LOG = 11;
    private static final int BACKGROUND_TASK_LOAD_LOCAL_ACCOUNT = 20;

    /** Default for the maximum number of returned aggregation suggestions. */
    private static final int DEFAULT_MAX_SUGGESTIONS = 5;

    /** Limit for the maximum number of social stream items to store under a raw contact. */
    private static final int MAX_STREAM_ITEMS_PER_RAW_CONTACT = 5;

    /** Rate limit (in ms) for photo cleanup.  Do it at most once per day. */
    private static final int PHOTO_CLEANUP_RATE_LIMIT = 24 * 60 * 60 * 1000;

    /**
     * Default expiration duration for pre-authorized URIs.  May be overridden from a secure
     * setting.
     */
    private static final int DEFAULT_PREAUTHORIZED_URI_EXPIRATION = 5 * 60 * 1000;

    private static final int USAGE_TYPE_ALL = -1;

    /**
     * Random URI parameter that will be appended to preauthorized URIs for uniqueness.
     */
    private static final String PREAUTHORIZED_URI_TOKEN = "perm_token";

    /**
     * Property key for the legacy contact import version. The need for a version
     * as opposed to a boolean flag is that if we discover bugs in the contact import process,
     * we can trigger re-import by incrementing the import version.
     */
    private static final String PROPERTY_CONTACTS_IMPORTED = "contacts_imported_v1";
    private static final int PROPERTY_CONTACTS_IMPORT_VERSION = 1;
    private static final String PREF_LOCALE = "locale";

    private static final String PROPERTY_AGGREGATION_ALGORITHM = "aggregation_v2";
    private static final int PROPERTY_AGGREGATION_ALGORITHM_VERSION = 2;

    private static final String AGGREGATE_CONTACTS = "sync.contacts.aggregate";

    private static final ProfileAwareUriMatcher sUriMatcher =
            new ProfileAwareUriMatcher(UriMatcher.NO_MATCH);

    /**
     * Used to insert a column into strequent results, which enables SQL to sort the list using
     * the total times contacted. See also {@link #sStrequentFrequentProjectionMap}.
     */
    private static final String TIMES_USED_SORT_COLUMN = "times_used_sort";

    private static final String FREQUENT_ORDER_BY = DataUsageStatColumns.TIMES_USED + " DESC,"
            + Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

    /* package */ static final String UPDATE_TIMES_CONTACTED_CONTACTS_TABLE =
            "UPDATE " + Tables.CONTACTS + " SET " + Contacts.TIMES_CONTACTED + "=" +
            " CASE WHEN " + Contacts.TIMES_CONTACTED + " IS NULL THEN 1 ELSE " +
            " (" + Contacts.TIMES_CONTACTED + " + 1) END WHERE " + Contacts._ID + "=?";

    /* package */ static final String UPDATE_TIMES_CONTACTED_RAWCONTACTS_TABLE =
            "UPDATE " + Tables.RAW_CONTACTS + " SET " + RawContacts.TIMES_CONTACTED + "=" +
            " CASE WHEN " + RawContacts.TIMES_CONTACTED + " IS NULL THEN 1 ELSE " +
            " (" + RawContacts.TIMES_CONTACTED + " + 1) END WHERE " + RawContacts.CONTACT_ID + "=?";

    /* package */ static final String PHONEBOOK_COLLATOR_NAME = "PHONEBOOK";

    // Regex for splitting query strings - we split on any group of non-alphanumeric characters,
    // excluding the @ symbol.
    /* package */ static final String QUERY_TOKENIZER_REGEX = "[^\\w@]+";

    private static final int CONTACTS = 1000;
    private static final int CONTACTS_ID = 1001;
    private static final int CONTACTS_LOOKUP = 1002;
    private static final int CONTACTS_LOOKUP_ID = 1003;
    private static final int CONTACTS_ID_DATA = 1004;
    private static final int CONTACTS_FILTER = 1005;
    private static final int CONTACTS_STREQUENT = 1006;
    private static final int CONTACTS_STREQUENT_FILTER = 1007;
    private static final int CONTACTS_GROUP = 1008;
    private static final int CONTACTS_ID_PHOTO = 1009;
    private static final int CONTACTS_LOOKUP_PHOTO = 1010;
    private static final int CONTACTS_LOOKUP_ID_PHOTO = 1011;
    private static final int CONTACTS_ID_DISPLAY_PHOTO = 1012;
    private static final int CONTACTS_LOOKUP_DISPLAY_PHOTO = 1013;
    private static final int CONTACTS_LOOKUP_ID_DISPLAY_PHOTO = 1014;
    private static final int CONTACTS_AS_VCARD = 1015;
    private static final int CONTACTS_AS_MULTI_VCARD = 1016;
    private static final int CONTACTS_LOOKUP_DATA = 1017;
    private static final int CONTACTS_LOOKUP_ID_DATA = 1018;
    private static final int CONTACTS_ID_ENTITIES = 1019;
    private static final int CONTACTS_LOOKUP_ENTITIES = 1020;
    private static final int CONTACTS_LOOKUP_ID_ENTITIES = 1021;
    private static final int CONTACTS_ID_STREAM_ITEMS = 1022;
    private static final int CONTACTS_LOOKUP_STREAM_ITEMS = 1023;
    private static final int CONTACTS_LOOKUP_ID_STREAM_ITEMS = 1024;
    private static final int CONTACTS_FREQUENT = 1025;
    private static final int CONTACTS_DELETE_USAGE = 1026;

    private static final int RAW_CONTACTS = 2002;
    private static final int RAW_CONTACTS_ID = 2003;
    private static final int RAW_CONTACTS_DATA = 2004;
    private static final int RAW_CONTACT_ENTITY_ID = 2005;
    private static final int RAW_CONTACTS_ID_DISPLAY_PHOTO = 2006;
    private static final int RAW_CONTACTS_ID_STREAM_ITEMS = 2007;
    private static final int RAW_CONTACTS_ID_STREAM_ITEMS_ID = 2008;

    private static final int DATA = 3000;
    private static final int DATA_ID = 3001;
    private static final int PHONES = 3002;
    private static final int PHONES_ID = 3003;
    private static final int PHONES_FILTER = 3004;
    private static final int EMAILS = 3005;
    private static final int EMAILS_ID = 3006;
    private static final int EMAILS_LOOKUP = 3007;
    private static final int EMAILS_FILTER = 3008;
    private static final int POSTALS = 3009;
    private static final int POSTALS_ID = 3010;
    private static final int CALLABLES = 3011;
    private static final int CALLABLES_ID = 3012;
    private static final int CALLABLES_FILTER = 3013;
    private static final int CONTACTABLES = 3014;
    private static final int CONTACTABLES_FILTER = 3015;
    private static final int PHONE_EMAIL = 3101; //add by MTK
    private static final int PHONE_EMAIL_FILTER = 3102; //add by MTK

    private static final int PHONE_LOOKUP = 4000;

    private static final int AGGREGATION_EXCEPTIONS = 6000;
    private static final int AGGREGATION_EXCEPTION_ID = 6001;

    private static final int STATUS_UPDATES = 7000;
    private static final int STATUS_UPDATES_ID = 7001;

    private static final int AGGREGATION_SUGGESTIONS = 8000;

    private static final int SETTINGS = 9000;

    private static final int GROUPS = 10000;
    private static final int GROUPS_ID = 10001;
    private static final int GROUPS_SUMMARY = 10003;

    private static final int SYNCSTATE = 11000;
    private static final int SYNCSTATE_ID = 11001;
    private static final int PROFILE_SYNCSTATE = 11002;
    private static final int PROFILE_SYNCSTATE_ID = 11003;

    private static final int SEARCH_SUGGESTIONS = 12001;
    private static final int SEARCH_SHORTCUT = 12002;

    private static final int LIVE_FOLDERS_PHONES_CONTACTS = 14004;  //add by MTK
    private static final int LIVE_FOLDERS_SIM_CONTACTS = 14005;     //add by MTK
    private static final int LIVE_FOLDERS_SIM1_CONTACTS = 14006;    //add by MTK
    private static final int LIVE_FOLDERS_SIM2_CONTACTS = 14007;    //add by MTK
    private static final int LIVE_FOLDERS_USIM_CONTACTS = 14008;    //add by MTK
    private static final int LIVE_FOLDERS_USIM1_CONTACTS = 14009;   //add by MTK
    private static final int LIVE_FOLDERS_USIM2_CONTACTS = 14010;   //add by MTK

    private static final int RAW_CONTACT_ENTITIES = 15001;

    private static final int PROVIDER_STATUS = 16001;

    private static final int DIRECTORIES = 17001;
    private static final int DIRECTORIES_ID = 17002;

    private static final int COMPLETE_NAME = 18000;

    private static final int PROFILE = 19000;
    private static final int PROFILE_ENTITIES = 19001;
    private static final int PROFILE_DATA = 19002;
    private static final int PROFILE_DATA_ID = 19003;
    private static final int PROFILE_AS_VCARD = 19004;
    private static final int PROFILE_RAW_CONTACTS = 19005;
    private static final int PROFILE_RAW_CONTACTS_ID = 19006;
    private static final int PROFILE_RAW_CONTACTS_ID_DATA = 19007;
    private static final int PROFILE_RAW_CONTACTS_ID_ENTITIES = 19008;
    private static final int PROFILE_STATUS_UPDATES = 19009;
    private static final int PROFILE_RAW_CONTACT_ENTITIES = 19010;
    private static final int PROFILE_PHOTO = 19011;
    private static final int PROFILE_DISPLAY_PHOTO = 19012;

    private static final int DATA_USAGE_FEEDBACK_ID = 20001;

    private static final int STREAM_ITEMS = 21000;
    private static final int STREAM_ITEMS_PHOTOS = 21001;
    private static final int STREAM_ITEMS_ID = 21002;
    private static final int STREAM_ITEMS_ID_PHOTOS = 21003;
    private static final int STREAM_ITEMS_ID_PHOTOS_ID = 21004;
    private static final int STREAM_ITEMS_LIMIT = 21005;

    private static final int DISPLAY_PHOTO = 22000;
    private static final int PHOTO_DIMENSIONS = 22001;
    
    private static final int DELETED_CONTACTS = 23000;
    private static final int DELETED_CONTACTS_ID = 23001;

    private static final int PINNED_POSITION_UPDATE = 24001;
    
    private static final int DIALER_SEARCH_INCREMENT = 90000;    //add by MTK
    private static final int DIALER_SEARCH_SIMPLE = 90001;       //add by MTK
    
    //Gionee:huangzy 20121019 modify for CR00715333 start
    /*private static final int MIMETYPES_ID = 90002;      //add by hzy
    private static final int DATA_USAG_STAT = 90003;      //add by hzy
    private static final int DATA_USAG_STAT_CLEAR = 90004;      //add by hzy*/
    private static final int CONTACTS_FREQUENT_CLEAR = 90004;
    //Gionee:huangzy 20121019 modify for CR00715333 end
    
    
    //Gionee:huangzy 20121011 add for CR00710695 start
    private static final int GN_DIALER_SEARCH = 90005;
    private static final int GN_DIALER_SEARCH_INIT = 90006;
    //Gionee:huangzy 20121011 add for CR00710695 end
    
    //Gionee:huangzy 20121128 add for CR00736966 star
    private static final int GN_SYNC_STATUS = 90007;
    //Gionee:huangzy 20121128 add for CR00736966 end
    
    // aurora <wangth> <2013-12-9> add for aurora begin
    private static final int AURORA_SAMSUNG_MATCH = 90008;
    // aurora <wangth> <2013-12-9> add for aurora end
    
    // reject begin
    private static final int BLACKS = 90009;
    private static final int BLACK = 90010;
    private static final int MARKS = 90011;
    private static final int MARK = 90012;
    // reject end
    //contacts sync begin
    private static final int SYNCS = 90013;
    private static final int SYNC = 90014;
    private static final int SYNC_ACCESSORY = 90015;
    private static final int SYNC_DOWN = 90016;
    private static final int SYNC_UP = 90017;
    private static final int SYNC_UP_RESULT = 90018;
    private static final int SYNC_UP_SIZE = 90019;
    private static final int SYNC_UP_LIMIT_COUNT = 90020;
    private static final int SYNC_UP_LIMIT = 90021;
    private static final int URI_CLEAN_ACCOUNT=90022;
    private static final int URI_INIT_ACCOUNT=90023;
    private static final int URI_INIT_ACCOUNT_MULTI=90024;
    private static final int URI_CLEAN_DATA=90025;
    private static final int URI_IS_FIRST_SYNC=90026;
    private long syncStart=0;
    private long syncEnd=0;
    private boolean syncFlag=true;
    public static final Uri CONTENT_URI = Uri.parse("content://"
			+ GnContactsContract.AUTHORITY + "/sync");
    //contacts sync end
    

    private static final int AURORA_MULTI_HANZI = 90027;

    // Inserts into URIs in this map will direct to the profile database if the parent record's
    // value (looked up from the ContentValues object with the key specified by the value in this
    // map) is in the profile ID-space (see {@link ProfileDatabaseHelper#PROFILE_ID_SPACE}).
    private static final Map<Integer, String> INSERT_URI_ID_VALUE_MAP = Maps.newHashMap();
    static {
        INSERT_URI_ID_VALUE_MAP.put(DATA, Data.RAW_CONTACT_ID);
        INSERT_URI_ID_VALUE_MAP.put(RAW_CONTACTS_DATA, Data.RAW_CONTACT_ID);
        INSERT_URI_ID_VALUE_MAP.put(STATUS_UPDATES, StatusUpdates.DATA_ID);
        INSERT_URI_ID_VALUE_MAP.put(STREAM_ITEMS, StreamItems.RAW_CONTACT_ID);
        INSERT_URI_ID_VALUE_MAP.put(RAW_CONTACTS_ID_STREAM_ITEMS, StreamItems.RAW_CONTACT_ID);
        INSERT_URI_ID_VALUE_MAP.put(STREAM_ITEMS_PHOTOS, StreamItemPhotos.STREAM_ITEM_ID);
        INSERT_URI_ID_VALUE_MAP.put(STREAM_ITEMS_ID_PHOTOS, StreamItemPhotos.STREAM_ITEM_ID);
    }

    // Any interactions that involve these URIs will also require the calling package to have either
    // android.permission.READ_SOCIAL_STREAM permission or android.permission.WRITE_SOCIAL_STREAM
    // permission, depending on the type of operation being performed.
    private static final List<Integer> SOCIAL_STREAM_URIS = Lists.newArrayList(
            CONTACTS_ID_STREAM_ITEMS,
            CONTACTS_LOOKUP_STREAM_ITEMS,
            CONTACTS_LOOKUP_ID_STREAM_ITEMS,
            RAW_CONTACTS_ID_STREAM_ITEMS,
            RAW_CONTACTS_ID_STREAM_ITEMS_ID,
            STREAM_ITEMS,
            STREAM_ITEMS_PHOTOS,
            STREAM_ITEMS_ID,
            STREAM_ITEMS_ID_PHOTOS,
            STREAM_ITEMS_ID_PHOTOS_ID
    );

//    private static final String SELECTION_FAVORITES_GROUPS_BY_RAW_CONTACT_ID =
//            RawContactsColumns.CONCRETE_ID + "=? AND "
//                    + GroupsColumns.CONCRETE_ACCOUNT_NAME
//                    + "=" + RawContactsColumns.CONCRETE_ACCOUNT_NAME + " AND "
//                    + GroupsColumns.CONCRETE_ACCOUNT_TYPE
//                    + "=" + RawContactsColumns.CONCRETE_ACCOUNT_TYPE + " AND ("
//                    + GroupsColumns.CONCRETE_DATA_SET
//                    + "=" + RawContactsColumns.CONCRETE_DATA_SET + " OR "
//                    + GroupsColumns.CONCRETE_DATA_SET + " IS NULL AND "
//                    + RawContactsColumns.CONCRETE_DATA_SET + " IS NULL)"
//                    + " AND " + Groups.FAVORITES + " != 0";
    private static final String SELECTION_FAVORITES_GROUPS_BY_RAW_CONTACT_ID =
            RawContactsColumns.CONCRETE_ID + "=? AND "
                + GroupsColumns.CONCRETE_ACCOUNT_ID + "=" + RawContactsColumns.CONCRETE_ACCOUNT_ID
                + " AND " + Groups.FAVORITES + " != 0";

//    private static final String SELECTION_AUTO_ADD_GROUPS_BY_RAW_CONTACT_ID =
//            RawContactsColumns.CONCRETE_ID + "=? AND "
//                    + GroupsColumns.CONCRETE_ACCOUNT_NAME + "="
//                    + RawContactsColumns.CONCRETE_ACCOUNT_NAME + " AND "
//                    + GroupsColumns.CONCRETE_ACCOUNT_TYPE + "="
//                    + RawContactsColumns.CONCRETE_ACCOUNT_TYPE + " AND ("
//                    + GroupsColumns.CONCRETE_DATA_SET + "="
//                    + RawContactsColumns.CONCRETE_DATA_SET + " OR "
//                    + GroupsColumns.CONCRETE_DATA_SET + " IS NULL AND "
//                    + RawContactsColumns.CONCRETE_DATA_SET + " IS NULL)"
//                    + " AND " + Groups.AUTO_ADD + " != 0";
    private static final String SELECTION_AUTO_ADD_GROUPS_BY_RAW_CONTACT_ID =
            RawContactsColumns.CONCRETE_ID + "=? AND "
                + GroupsColumns.CONCRETE_ACCOUNT_ID + "=" + RawContactsColumns.CONCRETE_ACCOUNT_ID
                + " AND " + Groups.AUTO_ADD + " != 0";

    private static final String[] PROJECTION_GROUP_ID
            = new String[]{Tables.GROUPS + "." + Groups._ID};

    private static final String SELECTION_GROUPMEMBERSHIP_DATA = DataColumns.MIMETYPE_ID + "=? "
            + "AND " + GroupMembership.GROUP_ROW_ID + "=? "
            + "AND " + GroupMembership.RAW_CONTACT_ID + "=?";

    private static final String SELECTION_STARRED_FROM_RAW_CONTACTS =
            "SELECT " + RawContacts.STARRED
                    + " FROM " + Tables.RAW_CONTACTS + " WHERE " + RawContacts._ID + "=?";

//    private interface DataContactsQuery {
//        public static final String TABLE = "data "
//                + "JOIN raw_contacts ON (data.raw_contact_id = raw_contacts._id) "
//                + "JOIN contacts ON (raw_contacts.contact_id = contacts._id)";
//
//        public static final String[] PROJECTION = new String[] {
//            RawContactsColumns.CONCRETE_ID,
//            RawContactsColumns.CONCRETE_ACCOUNT_TYPE,
//            RawContactsColumns.CONCRETE_ACCOUNT_NAME,
//            RawContactsColumns.CONCRETE_DATA_SET,
//            DataColumns.CONCRETE_ID,
//            ContactsColumns.CONCRETE_ID
//        };
//
//        public static final int RAW_CONTACT_ID = 0;
//        public static final int ACCOUNT_TYPE = 1;
//        public static final int ACCOUNT_NAME = 2;
//        public static final int DATA_SET = 3;
//        public static final int DATA_ID = 4;
//        public static final int CONTACT_ID = 5;
//    }
    private interface DataContactsQuery {
        public static final String TABLE = "data "
                + "JOIN raw_contacts ON (data.raw_contact_id = raw_contacts._id) "
                + "JOIN " + Tables.ACCOUNTS + " ON ("
                    + AccountsColumns.CONCRETE_ID + "=" + RawContactsColumns.CONCRETE_ACCOUNT_ID
                    + ")"
                + "JOIN contacts ON (raw_contacts.contact_id = contacts._id)";

        public static final String[] PROJECTION = new String[] {
            RawContactsColumns.CONCRETE_ID,
            AccountsColumns.CONCRETE_ACCOUNT_TYPE,
            AccountsColumns.CONCRETE_ACCOUNT_NAME,
            AccountsColumns.CONCRETE_DATA_SET,
            DataColumns.CONCRETE_ID,
            ContactsColumns.CONCRETE_ID
        };

        public static final int RAW_CONTACT_ID = 0;
        public static final int ACCOUNT_TYPE = 1;
        public static final int ACCOUNT_NAME = 2;
        public static final int DATA_SET = 3;
        public static final int DATA_ID = 4;
        public static final int CONTACT_ID = 5;
    }

    interface RawContactsQuery {
        String TABLE = Tables.RAW_CONTACTS_JOIN_ACCOUNTS;


        String[] COLUMNS = new String[] {
                RawContacts.DELETED,
                RawContactsColumns.ACCOUNT_ID,
                AccountsColumns.CONCRETE_ACCOUNT_TYPE,
                AccountsColumns.CONCRETE_ACCOUNT_NAME,
                AccountsColumns.CONCRETE_DATA_SET,
        };

        int DELETED = 0;
        int ACCOUNT_ID = 1;
        int ACCOUNT_TYPE = 2;
        int ACCOUNT_NAME = 3;
        int DATA_SET = 4;
    }

    public static final String DEFAULT_ACCOUNT_TYPE = "com.google";

    /** Sql where statement for filtering on groups. */
    private static final String CONTACTS_IN_GROUP_SELECT =
            Contacts._ID + " IN "
                    + "(SELECT " + RawContacts.CONTACT_ID
                    + " FROM " + Tables.RAW_CONTACTS
                    + " WHERE " + RawContactsColumns.CONCRETE_ID + " IN "
                            + "(SELECT " + DataColumns.CONCRETE_RAW_CONTACT_ID
                            + " FROM " + Tables.DATA_JOIN_MIMETYPES
                            + " WHERE " + DataColumns.MIMETYPE_ID + "=?"
                                    + " AND " + GroupMembership.GROUP_ROW_ID + "="
                                    + "(SELECT " + Tables.GROUPS + "." + Groups._ID
                                    + " FROM " + Tables.GROUPS
                                    + " WHERE " + Groups.TITLE + "=? AND " + Groups.DELETED + "=0) ) )";

    /** Sql for updating DIRTY flag on multiple raw contacts */
    private static final String UPDATE_RAW_CONTACT_SET_DIRTY_SQL =
            "UPDATE " + Tables.RAW_CONTACTS +
            " SET " + RawContacts.DIRTY + "=1" +
            " WHERE " + RawContacts._ID + " IN (";

    /** Sql for updating VERSION on multiple raw contacts */
    private static final String UPDATE_RAW_CONTACT_SET_VERSION_SQL =
            "UPDATE " + Tables.RAW_CONTACTS +
            " SET " + RawContacts.VERSION + " = " + RawContacts.VERSION + " + 1" +
            " WHERE " + RawContacts._ID + " IN (";

    /** Sql for undemoting a demoted contact **/
    private static final String UNDEMOTE_CONTACT =
            "UPDATE " + Tables.CONTACTS +
            " SET " + ContactsContract.Contacts.PINNED + " = " + ContactsContract.PinnedPositions.UNPINNED +
            " WHERE " + Contacts._ID + " = ?1 AND " + ContactsContract.Contacts.PINNED + " <= " +
            ContactsContract.PinnedPositions.DEMOTED;

    /** Sql for undemoting a demoted raw contact **/
    private static final String UNDEMOTE_RAW_CONTACT =
            "UPDATE " + Tables.RAW_CONTACTS +
            " SET " + ContactsContract.RawContacts.PINNED + " = " + ContactsContract.PinnedPositions.UNPINNED +
            " WHERE " + RawContacts.CONTACT_ID + " = ?1 AND " + ContactsContract.Contacts.PINNED + " <= " +
            ContactsContract.PinnedPositions.DEMOTED;
    
    //Gionee:huangzy 20121128 add for CR00736966 start
    private static final String GN_UPDATE_RAW_CONTACT_SET_GN_VERSION_SQL_START =
        "UPDATE " + Tables.RAW_CONTACTS +
        " SET " + GnSyncColumns.GN_VERSION +
        	" = ";
    private static final String GN_UPDATE_RAW_CONTACT_SET_GN_VERSION_SQL_END =
        " WHERE " + RawContacts._ID + " IN (";
    //Gionee:huangzy 20121128 add for CR00736966 end

    // Current contacts - those contacted within the last 3 days (in seconds)
    private static final long EMAIL_FILTER_CURRENT = 3 * 24 * 60 * 60;

    // Recent contacts - those contacted within the last 30 days (in seconds)
    private static final long EMAIL_FILTER_RECENT = 30 * 24 * 60 * 60;

    private static final String TIME_SINCE_LAST_USED =
            "(strftime('%s', 'now') - " + DataUsageStatColumns.LAST_TIME_USED + "/1000)";

    /*
     * Sorting order for email address suggestions: first starred, then the rest.
     * second in_visible_group, then the rest.
     * Within the four (starred/unstarred, in_visible_group/not-in_visible_group) groups
     * - three buckets: very recently contacted, then fairly
     * recently contacted, then the rest.  Within each of the bucket - descending count
     * of times contacted (both for data row and for contact row). If all else fails, alphabetical.
     * (Super)primary email address is returned before other addresses for the same contact.
     */
    private static final String EMAIL_FILTER_SORT_ORDER =
        Contacts.STARRED + " DESC, "
        + Contacts.IN_VISIBLE_GROUP + " DESC, "
        + "(CASE WHEN " + TIME_SINCE_LAST_USED + " < " + EMAIL_FILTER_CURRENT
        + " THEN 0 "
                + " WHEN " + TIME_SINCE_LAST_USED + " < " + EMAIL_FILTER_RECENT
        + " THEN 1 "
        + " ELSE 2 END), "
        + DataUsageStatColumns.TIMES_USED + " DESC, "
        + Contacts.DISPLAY_NAME + ", "
        + Data.CONTACT_ID + ", "
        + Data.IS_SUPER_PRIMARY + " DESC, "
        + Data.IS_PRIMARY + " DESC";

    /** Currently same as {@link #EMAIL_FILTER_SORT_ORDER} */
    private static final String PHONE_FILTER_SORT_ORDER = EMAIL_FILTER_SORT_ORDER;

    /** Name lookup types used for contact filtering */
    private static final String CONTACT_LOOKUP_NAME_TYPES =
            NameLookupType.NAME_COLLATION_KEY + "," +
            NameLookupType.EMAIL_BASED_NICKNAME + "," +
            NameLookupType.NICKNAME;

    /**
     * If any of these columns are used in a Data projection, there is no point in
     * using the DISTINCT keyword, which can negatively affect performance.
     */
    private static final String[] DISTINCT_DATA_PROHIBITING_COLUMNS = {
            Data._ID,
            Data.RAW_CONTACT_ID,
            Data.NAME_RAW_CONTACT_ID,
            RawContacts.ACCOUNT_NAME,
            RawContacts.ACCOUNT_TYPE,
            RawContacts.DATA_SET,
            RawContacts.ACCOUNT_TYPE_AND_DATA_SET,
            RawContacts.DIRTY,
            RawContacts.NAME_VERIFIED,
            RawContacts.SOURCE_ID,
            RawContacts.VERSION,
    };

//    private static final ProjectionMap sContactsColumns = ProjectionMap.builder()
//            .add(Contacts.CUSTOM_RINGTONE)
//            .add(Contacts.DISPLAY_NAME)
//            .add(Contacts.DISPLAY_NAME_ALTERNATIVE)
//            .add(Contacts.DISPLAY_NAME_SOURCE)
//            .add(Contacts.IN_VISIBLE_GROUP)
//            .add(Contacts.LAST_TIME_CONTACTED)
//            .add(Contacts.LOOKUP_KEY)
//            .add(Contacts.PHONETIC_NAME)
//            .add(Contacts.PHONETIC_NAME_STYLE)
//            .add(Contacts.PHOTO_ID)
//            .add(Contacts.PHOTO_FILE_ID)
//            .add(Contacts.PHOTO_URI)
//            .add(Contacts.PHOTO_THUMBNAIL_URI)
//            .add(Contacts.SEND_TO_VOICEMAIL)
//            .add(Contacts.SORT_KEY_ALTERNATIVE)
//            .add(Contacts.SORT_KEY_PRIMARY)
//            .add(Contacts.STARRED)
//            .add(Contacts.TIMES_CONTACTED)
//            .add(Contacts.HAS_PHONE_NUMBER)
//            .add(Contacts.INDICATE_PHONE_SIM)       //add by MTK
//            .add(Contacts.INDEX_IN_SIM)             //add by MTK
//            .add(Contacts.SEND_TO_VOICEMAIL_VT)     //add by MTK
//            .add(Contacts.SEND_TO_VOICEMAIL_SIP)    //add by MTK
//            //.add(Contacts.FILTER)                   //add by MTK
//            .add(AURORA_DEFAULT_PRIVACY_COLUMN)  // add by aurora wangth
//            .add(AURORA_PRIVATE_NOTIFICATION)
//            .build();

    private static final ProjectionMap sContactsColumns = ProjectionMap.builder()
            .add(Contacts.CUSTOM_RINGTONE)
            .add(Contacts.DISPLAY_NAME)
            .add(Contacts.DISPLAY_NAME_ALTERNATIVE)
            .add(Contacts.DISPLAY_NAME_SOURCE)
            .add(Contacts.IN_VISIBLE_GROUP)
            .add(Contacts.LAST_TIME_CONTACTED)
            .add(Contacts.LOOKUP_KEY)
            .add(Contacts.PHONETIC_NAME)
            .add(Contacts.PHONETIC_NAME_STYLE)
            .add(Contacts.PHOTO_ID)
            .add(Contacts.PHOTO_FILE_ID)
            .add(Contacts.PHOTO_URI)
            .add(Contacts.PHOTO_THUMBNAIL_URI)
            .add(Contacts.SEND_TO_VOICEMAIL)
            .add(Contacts.SORT_KEY_ALTERNATIVE)
            .add(Contacts.SORT_KEY_PRIMARY)
            .add(ContactsColumns.PHONEBOOK_LABEL_PRIMARY)
            .add(ContactsColumns.PHONEBOOK_BUCKET_PRIMARY)
            .add(ContactsColumns.PHONEBOOK_LABEL_ALTERNATIVE)
            .add(ContactsColumns.PHONEBOOK_BUCKET_ALTERNATIVE)
            .add(Contacts.STARRED)
            .add(ContactsContract.Contacts.PINNED)
            .add(Contacts.TIMES_CONTACTED)
            .add(Contacts.HAS_PHONE_NUMBER)
            .add(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP)
            .add(Contacts.INDICATE_PHONE_SIM)       //add by MTK
            .add(Contacts.INDEX_IN_SIM)             //add by MTK
            .add(Contacts.SEND_TO_VOICEMAIL_VT)     //add by MTK
            .add(Contacts.SEND_TO_VOICEMAIL_SIP)    //add by MTK
            //.add(Contacts.FILTER)                   //add by MTK
            .add(AURORA_DEFAULT_PRIVACY_COLUMN)  // add by aurora wangth
            .add(AURORA_PRIVATE_NOTIFICATION)
            .build();

    private static final ProjectionMap sContactsPresenceColumns = ProjectionMap.builder()
            .add(Contacts.CONTACT_PRESENCE,
                    Tables.AGGREGATED_PRESENCE + "." + StatusUpdates.PRESENCE)
            .add(Contacts.CONTACT_CHAT_CAPABILITY,
                    Tables.AGGREGATED_PRESENCE + "." + StatusUpdates.CHAT_CAPABILITY)
            .add(Contacts.CONTACT_STATUS,
                    ContactsStatusUpdatesColumns.CONCRETE_STATUS)
            .add(Contacts.CONTACT_STATUS_TIMESTAMP,
                    ContactsStatusUpdatesColumns.CONCRETE_STATUS_TIMESTAMP)
            .add(Contacts.CONTACT_STATUS_RES_PACKAGE,
                    ContactsStatusUpdatesColumns.CONCRETE_STATUS_RES_PACKAGE)
            .add(Contacts.CONTACT_STATUS_LABEL,
                    ContactsStatusUpdatesColumns.CONCRETE_STATUS_LABEL)
            .add(Contacts.CONTACT_STATUS_ICON,
                    ContactsStatusUpdatesColumns.CONCRETE_STATUS_ICON)
            .build();

    private static final ProjectionMap sSnippetColumns = ProjectionMap.builder()
            .add(SearchSnippetColumns.SNIPPET)
            .build();

    private static final ProjectionMap sRawContactColumns = ProjectionMap.builder()
            .add(RawContacts.ACCOUNT_NAME)
            .add(RawContacts.ACCOUNT_TYPE)
            .add(RawContacts.DATA_SET)
            .add(RawContacts.ACCOUNT_TYPE_AND_DATA_SET)
            .add(RawContacts.DIRTY)
            .add(RawContacts.NAME_VERIFIED)
            .add(RawContacts.SOURCE_ID)
            .add(RawContacts.VERSION)
            .add(RawContacts.INDICATE_PHONE_SIM)    //add by MTK
            .add(RawContacts.INDEX_IN_SIM)          //add by MTK
            .add(RawContacts.TIMESTAMP)             //add by MTK
            .build();

    private static final ProjectionMap sRawContactSyncColumns = ProjectionMap.builder()
            .add(RawContacts.SYNC1)
            .add(RawContacts.SYNC2)
            .add(RawContacts.SYNC3)
            .add(RawContacts.SYNC4)
            .build();

    private static final ProjectionMap sDataColumns = ProjectionMap.builder()
            .add(Data.DATA1)
            .add(Data.DATA2)
            .add(Data.DATA3)
            .add(Data.DATA4)
            .add(Data.DATA5)
            .add(Data.DATA6)
            .add(Data.DATA7)
            .add(Data.DATA8)
            .add(Data.DATA9)
            .add(Data.DATA10)
            .add(Data.DATA11)
            .add(Data.DATA12)
            .add(Data.DATA13)
            .add(Data.DATA14)
            .add(Data.DATA15)
            .add(Data.DATA_VERSION)
            .add(Data.IS_PRIMARY)
            .add(Data.IS_SUPER_PRIMARY)
            .add(Data.MIMETYPE)
            .add(Data.RES_PACKAGE)
            .add(Data.SYNC1)
            .add(Data.SYNC2)
            .add(Data.SYNC3)
            .add(Data.SYNC4)
            .add(GroupMembership.GROUP_SOURCE_ID)
            .add(Data.SIM_ASSOCIATION_ID)       //add by MTK
            .add(Data.IS_ADDITIONAL_NUMBER)     //add by MTK
            .add("auto_record")    // add by wangth
            .build();

    private static final ProjectionMap sContactPresenceColumns = ProjectionMap.builder()
            .add(Contacts.CONTACT_PRESENCE,
                    Tables.AGGREGATED_PRESENCE + '.' + StatusUpdates.PRESENCE)
            .add(Contacts.CONTACT_CHAT_CAPABILITY,
                    Tables.AGGREGATED_PRESENCE + '.' + StatusUpdates.CHAT_CAPABILITY)
            .add(Contacts.CONTACT_STATUS,
                    ContactsStatusUpdatesColumns.CONCRETE_STATUS)
            .add(Contacts.CONTACT_STATUS_TIMESTAMP,
                    ContactsStatusUpdatesColumns.CONCRETE_STATUS_TIMESTAMP)
            .add(Contacts.CONTACT_STATUS_RES_PACKAGE,
                    ContactsStatusUpdatesColumns.CONCRETE_STATUS_RES_PACKAGE)
            .add(Contacts.CONTACT_STATUS_LABEL,
                    ContactsStatusUpdatesColumns.CONCRETE_STATUS_LABEL)
            .add(Contacts.CONTACT_STATUS_ICON,
                    ContactsStatusUpdatesColumns.CONCRETE_STATUS_ICON)
            .build();

    private static final ProjectionMap sDataPresenceColumns = ProjectionMap.builder()
            .add(Data.PRESENCE, Tables.PRESENCE + "." + StatusUpdates.PRESENCE)
            .add(Data.CHAT_CAPABILITY, Tables.PRESENCE + "." + StatusUpdates.CHAT_CAPABILITY)
            .add(Data.STATUS, StatusUpdatesColumns.CONCRETE_STATUS)
            .add(Data.STATUS_TIMESTAMP, StatusUpdatesColumns.CONCRETE_STATUS_TIMESTAMP)
            .add(Data.STATUS_RES_PACKAGE, StatusUpdatesColumns.CONCRETE_STATUS_RES_PACKAGE)
            .add(Data.STATUS_LABEL, StatusUpdatesColumns.CONCRETE_STATUS_LABEL)
            .add(Data.STATUS_ICON, StatusUpdatesColumns.CONCRETE_STATUS_ICON)
            .build();

    private static final ProjectionMap sDataUsageColumns = ProjectionMap.builder()
            .add(ContactsContract.Data.TIMES_USED, Tables.DATA_USAGE_STAT + "." + ContactsContract.Data.TIMES_USED)
            .add(ContactsContract.Data.LAST_TIME_USED, Tables.DATA_USAGE_STAT + "." + ContactsContract.Data.LAST_TIME_USED)
            .build();

    /** Contains just BaseColumns._COUNT */
    private static final ProjectionMap sCountProjectionMap = ProjectionMap.builder()
            .add(BaseColumns._COUNT, "COUNT(*)")
            .build();

    /** Contains just the contacts columns */
    private static final ProjectionMap sContactsProjectionMap = ProjectionMap.builder()
            .add(Contacts._ID)
            .add(Contacts.HAS_PHONE_NUMBER)
            .add(Contacts.NAME_RAW_CONTACT_ID)
            .add(Contacts.IS_USER_PROFILE)
            .addAll(sContactsColumns)
            .addAll(sContactsPresenceColumns)
            .build();

    /** Contains just the contacts columns */
    private static final ProjectionMap sContactsProjectionWithSnippetMap = ProjectionMap.builder()
            .addAll(sContactsProjectionMap)
            .addAll(sSnippetColumns)
            .build();

    /** Used for pushing starred contacts to the top of a times contacted list **/
    private static final ProjectionMap sStrequentStarredProjectionMap = ProjectionMap.builder()
            .addAll(sContactsProjectionMap)
            .add(TIMES_USED_SORT_COLUMN, String.valueOf(Long.MAX_VALUE))
            .build();

    private static final ProjectionMap sStrequentFrequentProjectionMap = ProjectionMap.builder()
            .addAll(sContactsProjectionMap)
            .add(TIMES_USED_SORT_COLUMN, "SUM(" + DataUsageStatColumns.CONCRETE_TIMES_USED + ")")
            .build();

    /**
     * Used for Strequent Uri with {@link ContactsContract#STREQUENT_PHONE_ONLY}, which allows
     * users to obtain part of Data columns. Right now Starred part just returns NULL for
     * those data columns (frequent part should return real ones in data table).
     **/
    private static final ProjectionMap sStrequentPhoneOnlyStarredProjectionMap
            = ProjectionMap.builder()
            .addAll(sContactsProjectionMap)
            .add(TIMES_USED_SORT_COLUMN, String.valueOf(Long.MAX_VALUE))
            .add(Phone.NUMBER, "NULL")
            .add(Phone.TYPE, "NULL")
            .add(Phone.LABEL, "NULL")
            .build();

    /**
     * Used for Strequent Uri with {@link ContactsContract#STREQUENT_PHONE_ONLY}, which allows
     * users to obtain part of Data columns. We hard-code {@link Contacts#IS_USER_PROFILE} to NULL,
     * because sContactsProjectionMap specifies a field that doesn't exist in the view behind the
     * query that uses this projection map.
     **/
    private static final ProjectionMap sStrequentPhoneOnlyFrequentProjectionMap
            = ProjectionMap.builder()
            .addAll(sContactsProjectionMap)
            .add(TIMES_USED_SORT_COLUMN, DataUsageStatColumns.CONCRETE_TIMES_USED)
            .add(Phone.NUMBER)
            .add(Phone.TYPE)
            .add(Phone.LABEL)
            .add(Contacts.IS_USER_PROFILE, "NULL")
            .build();

    /** Contains just the contacts vCard columns */
    private static final ProjectionMap sContactsVCardProjectionMap = ProjectionMap.builder()
            .add(Contacts._ID)
            .add(OpenableColumns.DISPLAY_NAME, Contacts.DISPLAY_NAME + " || '.vcf'")
            .add(OpenableColumns.SIZE, "NULL")
            .build();

    /** Contains just the raw contacts columns */
    private static final ProjectionMap sRawContactsProjectionMap = ProjectionMap.builder()
            .add(RawContacts._ID)
            .add(RawContacts.CONTACT_ID)
            .add(RawContacts.DELETED)
            .add(RawContacts.DISPLAY_NAME_PRIMARY)
            .add(RawContacts.DISPLAY_NAME_ALTERNATIVE)
            .add(RawContacts.DISPLAY_NAME_SOURCE)
            .add(RawContacts.PHONETIC_NAME)
            .add(RawContacts.PHONETIC_NAME_STYLE)
            .add(RawContacts.SORT_KEY_PRIMARY)
            .add(RawContacts.SORT_KEY_ALTERNATIVE)
            .add(RawContacts.TIMES_CONTACTED)
            .add(RawContacts.LAST_TIME_CONTACTED)
            .add(RawContacts.CUSTOM_RINGTONE)
            .add(RawContacts.SEND_TO_VOICEMAIL)
            .add(RawContacts.STARRED)
            .add(RawContacts.AGGREGATION_MODE)
            .add(RawContacts.RAW_CONTACT_IS_USER_PROFILE)
            .add(RawContacts.TIMESTAMP)                 //add by MTK
            .add(RawContacts.SEND_TO_VOICEMAIL_VT)      //add by MTK
            .add(RawContacts.SEND_TO_VOICEMAIL_SIP)     //add by MTK
            .addAll(sRawContactColumns)
            .addAll(sRawContactSyncColumns)
            .add(GnSyncColumns.GN_VERSION)     //Gionee:huangzy 20121128 add for CR00736966
            .add(AURORA_DEFAULT_PRIVACY_COLUMN)  // add by aurora wangth
            .add(AURORA_PRIVATE_NOTIFICATION)
            .build();

    /** Contains the columns from the raw entity view*/
    private static final ProjectionMap sRawEntityProjectionMap = ProjectionMap.builder()
            .add(RawContacts._ID)
            .add(RawContacts.CONTACT_ID)
            .add(RawContacts.Entity.DATA_ID)
            .add(RawContacts.DELETED)
            .add(RawContacts.STARRED)
            .add(RawContacts.RAW_CONTACT_IS_USER_PROFILE)
            .addAll(sRawContactColumns)
            .addAll(sRawContactSyncColumns)
            .addAll(sDataColumns)
//            .add(AURORA_DEFAULT_PRIVACY_COLUMN)  // add by aurora wangth
            .build();

    /** Contains the columns from the contact entity view*/
    private static final ProjectionMap sEntityProjectionMap = ProjectionMap.builder()
            .add(Contacts.Entity._ID)
            .add(Contacts.Entity.CONTACT_ID)
            .add(Contacts.Entity.RAW_CONTACT_ID)
            .add(Contacts.Entity.DATA_ID)
            .add(Contacts.Entity.NAME_RAW_CONTACT_ID)
            .add(Contacts.Entity.DELETED)
            .add(Contacts.IS_USER_PROFILE)
            .addAll(sContactsColumns)
            .addAll(sContactPresenceColumns)
            .addAll(sRawContactColumns)
            .addAll(sRawContactSyncColumns)
            .addAll(sDataColumns)
            .addAll(sDataPresenceColumns)
            .build();

    /** Contains columns in PhoneLookup which are not contained in the data view. */
    private static final ProjectionMap sSipLookupColumns = ProjectionMap.builder()
            .add(PhoneLookup.NUMBER, SipAddress.SIP_ADDRESS)
            .add(PhoneLookup.TYPE, "0")
            .add(PhoneLookup.LABEL, "NULL")
            .add(PhoneLookup.NORMALIZED_NUMBER, "NULL")
            .build();

    /** Contains columns from the data view */
    private static final ProjectionMap sDataProjectionMap = ProjectionMap.builder()
            .add(Data._ID)
            .add(Data.RAW_CONTACT_ID)
            .add(Data.CONTACT_ID)
            .add(Data.NAME_RAW_CONTACT_ID)
            .add(RawContacts.RAW_CONTACT_IS_USER_PROFILE)
            .addAll(sDataColumns)
            .addAll(sDataPresenceColumns)
            .addAll(sRawContactColumns)
            .addAll(sContactsColumns)
            .addAll(sContactPresenceColumns)
            .addAll(sDataUsageColumns)
            .build();

    /** Contains columns from the data view used for SIP address lookup. */
    private static final ProjectionMap sDataSipLookupProjectionMap = ProjectionMap.builder()
            .addAll(sDataProjectionMap)
            .addAll(sSipLookupColumns)
            .build();

    /** Contains columns from the data view */
    private static final ProjectionMap sDistinctDataProjectionMap = ProjectionMap.builder()
            .add(Data._ID, "MIN(" + Data._ID + ")")
            .add(RawContacts.CONTACT_ID)
            .add(RawContacts.RAW_CONTACT_IS_USER_PROFILE)
            .addAll(sDataColumns)
            .addAll(sDataPresenceColumns)
            .addAll(sContactsColumns)
            .addAll(sContactPresenceColumns)
            .addAll(sDataUsageColumns)
            .build();

    /** Contains columns from the data view used for SIP address lookup. */
    private static final ProjectionMap sDistinctDataSipLookupProjectionMap = ProjectionMap.builder()
            .addAll(sDistinctDataProjectionMap)
            .addAll(sSipLookupColumns)
            .build();

    /** Contains the data and contacts columns, for joined tables */
    private static final ProjectionMap sPhoneLookupProjectionMap = ProjectionMap.builder()
            .add(PhoneLookup._ID, "contacts_view." + Contacts._ID)
            .add(PhoneLookup.LOOKUP_KEY, "contacts_view." + Contacts.LOOKUP_KEY)
            .add(PhoneLookup.DISPLAY_NAME, "contacts_view." + Contacts.DISPLAY_NAME)
            .add(PhoneLookup.LAST_TIME_CONTACTED, "contacts_view." + Contacts.LAST_TIME_CONTACTED)
            .add(PhoneLookup.TIMES_CONTACTED, "contacts_view." + Contacts.TIMES_CONTACTED)
            .add(PhoneLookup.STARRED, "contacts_view." + Contacts.STARRED)
            .add(PhoneLookup.IN_VISIBLE_GROUP, "contacts_view." + Contacts.IN_VISIBLE_GROUP)
            .add(PhoneLookup.PHOTO_ID, "contacts_view." + Contacts.PHOTO_ID)
            .add(PhoneLookup.PHOTO_URI, "contacts_view." + Contacts.PHOTO_URI)
            .add(PhoneLookup.PHOTO_THUMBNAIL_URI, "contacts_view." + Contacts.PHOTO_THUMBNAIL_URI)
            .add(PhoneLookup.CUSTOM_RINGTONE, "contacts_view." + Contacts.CUSTOM_RINGTONE)
            .add(PhoneLookup.HAS_PHONE_NUMBER, "contacts_view." + Contacts.HAS_PHONE_NUMBER)
            .add(PhoneLookup.SEND_TO_VOICEMAIL, "contacts_view." + Contacts.SEND_TO_VOICEMAIL)
            .add(PhoneLookup.NUMBER, Phone.NUMBER)
            .add(PhoneLookup.TYPE, Phone.TYPE)
            .add(PhoneLookup.LABEL, Phone.LABEL)
            .add(PhoneLookup.NORMALIZED_NUMBER, Phone.NORMALIZED_NUMBER)
            
            // The following lines are provided and maintained by Mediatek inc.
            .add(PhoneLookup.SEND_TO_VOICEMAIL_VT, "contacts_view." + Contacts.SEND_TO_VOICEMAIL_VT)
            .add(PhoneLookup.SEND_TO_VOICEMAIL_SIP, "contacts_view." + Contacts.SEND_TO_VOICEMAIL_SIP)
            .add(PhoneLookup.INDICATE_PHONE_SIM, "contacts_view." + Contacts.INDICATE_PHONE_SIM)
            .add(PhoneLookup.INDEX_IN_SIM, "contacts_view." + Contacts.INDEX_IN_SIM)
            .add(PhoneLookup.FILTER, "contacts_view." + Contacts.FILTER)
            .add(PhoneLookupColumns.DATA_ID, PhoneLookupColumns.DATA_ID)
            .add(PhoneLookupColumns.RAW_CONTACT_ID, "raw_contacts._id")
            // The previous lines are provided and maintained by Mediatek inc.
            
            .add(AURORA_DEFAULT_PRIVACY_COLUMN, "contacts_view." + AURORA_DEFAULT_PRIVACY_COLUMN)
            .add(AURORA_PRIVATE_NOTIFICATION, "contacts_view." + AURORA_PRIVATE_NOTIFICATION)
            .build();

    /** Contains the just the {@link Groups} columns */
    private static final ProjectionMap sGroupsProjectionMap = ProjectionMap.builder()
            .add(Groups._ID)
            .add(Groups.ACCOUNT_NAME)
            .add(Groups.ACCOUNT_TYPE)
            .add(Groups.DATA_SET)
            .add(Groups.ACCOUNT_TYPE_AND_DATA_SET)
            .add(Groups.SOURCE_ID)
            .add(Groups.DIRTY)
            .add(Groups.VERSION)
            .add(Groups.RES_PACKAGE)
            .add(Groups.TITLE)
            .add(Groups.TITLE_RES)
            .add("group_ringtone")
            .add(Groups.GROUP_VISIBLE)
            .add(Groups.SYSTEM_ID)
            .add(Groups.DELETED)
            .add(Groups.NOTES)
            .add(Groups.SHOULD_SYNC)
            .add(Groups.FAVORITES)
            .add(Groups.AUTO_ADD)
            .add(Groups.GROUP_IS_READ_ONLY)
            .add(Groups.SYNC1)
            .add(Groups.SYNC2)
            .add(Groups.SYNC3)
            .add(Groups.SYNC4)
            .add(GnSyncColumns.GN_VERSION)     //Gionee:huangzy 20121128 add for CR00736966
            .build();

    private static final ProjectionMap sDeletedContactsProjectionMap = ProjectionMap.builder()
            .add(ContactsContract.DeletedContacts.CONTACT_ID)
            .add(ContactsContract.DeletedContacts.CONTACT_DELETED_TIMESTAMP)
            .build();

    /**
     * Contains {@link Groups} columns along with summary details.
     *
     * Note {@link Groups#SUMMARY_COUNT} doesn't exist in groups/view_groups.
     * When we detect this column being requested, we join {@link Joins#GROUP_MEMBER_COUNT} to
     * generate it.
     */
    private static final ProjectionMap sGroupsSummaryProjectionMap = ProjectionMap.builder()
            .addAll(sGroupsProjectionMap)
            .add(Groups.SUMMARY_COUNT, "ifnull(group_member_count, 0)")
            .add(Groups.SUMMARY_WITH_PHONES,
                    "(SELECT COUNT(" + ContactsColumns.CONCRETE_ID + ") FROM "
                        + Tables.CONTACTS_JOIN_RAW_CONTACTS_DATA_FILTERED_BY_GROUPMEMBERSHIP
                        + " WHERE " + Contacts.HAS_PHONE_NUMBER + ")")
            .build();

    // This is only exposed as hidden API for the contacts app, so we can be very specific in
    // the filtering
//    private static final ProjectionMap sGroupsSummaryProjectionMapWithGroupCountPerAccount =
//            ProjectionMap.builder()
//            .addAll(sGroupsSummaryProjectionMap)
//            .add(Groups.SUMMARY_GROUP_COUNT_PER_ACCOUNT,
//                    "(SELECT COUNT(*) FROM " + Views.GROUPS + " WHERE "
//                        + "(" + Groups.ACCOUNT_NAME + "="
//                            + GroupsColumns.CONCRETE_ACCOUNT_NAME
//                            + " AND "
//                            + Groups.ACCOUNT_TYPE + "=" + GroupsColumns.CONCRETE_ACCOUNT_TYPE
//                            + " AND "
//                            + Groups.DELETED + "=0 AND "
//                            + Groups.FAVORITES + "=0 AND "
//                            + Groups.AUTO_ADD + "=0"
//                        + ")"
//                        + " GROUP BY "
//                            + Groups.ACCOUNT_NAME + ", " + Groups.ACCOUNT_TYPE
//                   + ")")
//            .build();

    /** Contains the agg_exceptions columns */
    private static final ProjectionMap sAggregationExceptionsProjectionMap = ProjectionMap.builder()
            .add(AggregationExceptionColumns._ID, Tables.AGGREGATION_EXCEPTIONS + "._id")
            .add(AggregationExceptions.TYPE)
            .add(AggregationExceptions.RAW_CONTACT_ID1)
            .add(AggregationExceptions.RAW_CONTACT_ID2)
            .build();

    /** Contains the agg_exceptions columns */
   /* private static final ProjectionMap sSettingsProjectionMap = ProjectionMap.builder()
            .add(Settings.ACCOUNT_NAME)
            .add(Settings.ACCOUNT_TYPE)
            .add(Settings.DATA_SET)
            .add(Settings.UNGROUPED_VISIBLE)
            .add(Settings.SHOULD_SYNC)
            .add(Settings.ANY_UNSYNCED,
                    "(CASE WHEN MIN(" + Settings.SHOULD_SYNC
                        + ",(SELECT "
                                + "(CASE WHEN MIN(" + Groups.SHOULD_SYNC + ") IS NULL"
                                + " THEN 1"
                                + " ELSE MIN(" + Groups.SHOULD_SYNC + ")"
                                + " END)"
                            + " FROM " + Tables.GROUPS
                            + " WHERE " + GroupsColumns.CONCRETE_ACCOUNT_NAME + "="
                                    + SettingsColumns.CONCRETE_ACCOUNT_NAME
                                + " AND " + GroupsColumns.CONCRETE_ACCOUNT_TYPE + "="
                                    + SettingsColumns.CONCRETE_ACCOUNT_TYPE
                                + " AND ((" + GroupsColumns.CONCRETE_DATA_SET + " IS NULL AND "
                                    + SettingsColumns.CONCRETE_DATA_SET + " IS NULL) OR ("
                                    + GroupsColumns.CONCRETE_DATA_SET + "="
                                    + SettingsColumns.CONCRETE_DATA_SET + "))))=0"
                    + " THEN 1"
                    + " ELSE 0"
                    + " END)")
            .add(Settings.UNGROUPED_COUNT,
                    "(SELECT COUNT(*)"
                    + " FROM (SELECT 1"
                            + " FROM " + Tables.SETTINGS_JOIN_RAW_CONTACTS_DATA_MIMETYPES_CONTACTS
                            + " GROUP BY " + Clauses.GROUP_BY_ACCOUNT_CONTACT_ID
                            + " HAVING " + Clauses.HAVING_NO_GROUPS
                    + "))")
            .add(Settings.UNGROUPED_WITH_PHONES,
                    "(SELECT COUNT(*)"
                    + " FROM (SELECT 1"
                            + " FROM " + Tables.SETTINGS_JOIN_RAW_CONTACTS_DATA_MIMETYPES_CONTACTS
                            + " WHERE " + Contacts.HAS_PHONE_NUMBER
                            + " GROUP BY " + Clauses.GROUP_BY_ACCOUNT_CONTACT_ID
                            + " HAVING " + Clauses.HAVING_NO_GROUPS
                    + "))")
            .build();*/
    private static final ProjectionMap sSettingsProjectionMap = ProjectionMap.builder()
            .add(Settings.ACCOUNT_NAME)
            .add(Settings.ACCOUNT_TYPE)
            .add(Settings.DATA_SET)
            .add(Settings.UNGROUPED_VISIBLE)
            .add(Settings.SHOULD_SYNC)
            .add(Settings.ANY_UNSYNCED,
                    "(CASE WHEN MIN(" + Settings.SHOULD_SYNC
                        + ",(SELECT "
                                + "(CASE WHEN MIN(" + Groups.SHOULD_SYNC + ") IS NULL"
                                + " THEN 1"
                                + " ELSE MIN(" + Groups.SHOULD_SYNC + ")"
                                + " END)"
                            + " FROM " + Views.GROUPS
                            + " WHERE " + ContactsDatabaseHelper.ViewGroupsColumns.CONCRETE_ACCOUNT_NAME + "="
                                    + SettingsColumns.CONCRETE_ACCOUNT_NAME
                                + " AND " + ContactsDatabaseHelper.ViewGroupsColumns.CONCRETE_ACCOUNT_TYPE + "="
                                    + SettingsColumns.CONCRETE_ACCOUNT_TYPE
                                + " AND ((" + ContactsDatabaseHelper.ViewGroupsColumns.CONCRETE_DATA_SET + " IS NULL AND "
                                    + SettingsColumns.CONCRETE_DATA_SET + " IS NULL) OR ("
                                    + ContactsDatabaseHelper.ViewGroupsColumns.CONCRETE_DATA_SET + "="
                                    + SettingsColumns.CONCRETE_DATA_SET + "))))=0"
                    + " THEN 1"
                    + " ELSE 0"
                    + " END)")
            .add(Settings.UNGROUPED_COUNT,
                    "(SELECT COUNT(*)"
                    + " FROM (SELECT 1"
                            + " FROM " + Tables.SETTINGS_JOIN_RAW_CONTACTS_DATA_MIMETYPES_CONTACTS
                            + " GROUP BY " + Clauses.GROUP_BY_ACCOUNT_CONTACT_ID
                            + " HAVING " + Clauses.HAVING_NO_GROUPS
                    + "))")
            .add(Settings.UNGROUPED_WITH_PHONES,
                    "(SELECT COUNT(*)"
                    + " FROM (SELECT 1"
                            + " FROM " + Tables.SETTINGS_JOIN_RAW_CONTACTS_DATA_MIMETYPES_CONTACTS
                            + " WHERE " + Contacts.HAS_PHONE_NUMBER
                            + " GROUP BY " + Clauses.GROUP_BY_ACCOUNT_CONTACT_ID
                            + " HAVING " + Clauses.HAVING_NO_GROUPS
                    + "))")
            .build();

    /** Contains StatusUpdates columns */
    private static final ProjectionMap sStatusUpdatesProjectionMap = ProjectionMap.builder()
            .add(PresenceColumns.RAW_CONTACT_ID)
            .add(StatusUpdates.DATA_ID, DataColumns.CONCRETE_ID)
            .add(StatusUpdates.IM_ACCOUNT)
            .add(StatusUpdates.IM_HANDLE)
            .add(StatusUpdates.PROTOCOL)
            // We cannot allow a null in the custom protocol field, because SQLite3 does not
            // properly enforce uniqueness of null values
            .add(StatusUpdates.CUSTOM_PROTOCOL,
                    "(CASE WHEN " + StatusUpdates.CUSTOM_PROTOCOL + "=''"
                    + " THEN NULL"
                    + " ELSE " + StatusUpdates.CUSTOM_PROTOCOL + " END)")
            .add(StatusUpdates.PRESENCE)
            .add(StatusUpdates.CHAT_CAPABILITY)
            .add(StatusUpdates.STATUS)
            .add(StatusUpdates.STATUS_TIMESTAMP)
            .add(StatusUpdates.STATUS_RES_PACKAGE)
            .add(StatusUpdates.STATUS_ICON)
            .add(StatusUpdates.STATUS_LABEL)
            .build();

    /** Contains StreamItems columns */
    private static final ProjectionMap sStreamItemsProjectionMap = ProjectionMap.builder()
            .add(StreamItems._ID)
            .add(StreamItems.CONTACT_ID)
            .add(StreamItems.CONTACT_LOOKUP_KEY)
            .add(StreamItems.ACCOUNT_NAME)
            .add(StreamItems.ACCOUNT_TYPE)
            .add(StreamItems.DATA_SET)
            .add(StreamItems.RAW_CONTACT_ID)
            .add(StreamItems.RAW_CONTACT_SOURCE_ID)
            .add(StreamItems.RES_PACKAGE)
            .add(StreamItems.RES_ICON)
            .add(StreamItems.RES_LABEL)
            .add(StreamItems.TEXT)
            .add(StreamItems.TIMESTAMP)
            .add(StreamItems.COMMENTS)
            .add(StreamItems.SYNC1)
            .add(StreamItems.SYNC2)
            .add(StreamItems.SYNC3)
            .add(StreamItems.SYNC4)
            .build();

    private static final ProjectionMap sStreamItemPhotosProjectionMap = ProjectionMap.builder()
            .add(StreamItemPhotos._ID, StreamItemPhotosColumns.CONCRETE_ID)
            .add(StreamItems.RAW_CONTACT_ID)
            .add(StreamItems.RAW_CONTACT_SOURCE_ID, RawContactsColumns.CONCRETE_SOURCE_ID)
            .add(StreamItemPhotos.STREAM_ITEM_ID)
            .add(StreamItemPhotos.SORT_INDEX)
            .add(StreamItemPhotos.PHOTO_FILE_ID)
            .add(StreamItemPhotos.PHOTO_URI,
                    "'" + DisplayPhoto.CONTENT_URI + "'||'/'||" + StreamItemPhotos.PHOTO_FILE_ID)
            .add(PhotoFiles.HEIGHT)
            .add(PhotoFiles.WIDTH)
            .add(PhotoFiles.FILESIZE)
            .add(StreamItemPhotos.SYNC1)
            .add(StreamItemPhotos.SYNC2)
            .add(StreamItemPhotos.SYNC3)
            .add(StreamItemPhotos.SYNC4)
            .build();

    /** Contains {@link Directory} columns */
    private static final ProjectionMap sDirectoryProjectionMap = ProjectionMap.builder()
            .add(Directory._ID)
            .add(Directory.PACKAGE_NAME)
            .add(Directory.TYPE_RESOURCE_ID)
            .add(Directory.DISPLAY_NAME)
            .add(Directory.DIRECTORY_AUTHORITY)
            .add(Directory.ACCOUNT_TYPE)
            .add(Directory.ACCOUNT_NAME)
            .add(Directory.EXPORT_SUPPORT)
            .add(Directory.SHORTCUT_SUPPORT)
            .add(Directory.PHOTO_SUPPORT)
            .build();

    // where clause to update the status_updates table
    private static final String WHERE_CLAUSE_FOR_STATUS_UPDATES_TABLE =
            StatusUpdatesColumns.DATA_ID + " IN (SELECT Distinct " + StatusUpdates.DATA_ID +
            " FROM " + Tables.STATUS_UPDATES + " LEFT OUTER JOIN " + Tables.PRESENCE +
            " ON " + StatusUpdatesColumns.DATA_ID + " = " + StatusUpdates.DATA_ID + " WHERE ";

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Notification ID for failure to import contacts.
     */
    private static final int LEGACY_IMPORT_FAILED_NOTIFICATION = 1;

    private static final String DEFAULT_SNIPPET_ARG_START_MATCH = "[";
    private static final String DEFAULT_SNIPPET_ARG_END_MATCH = "]";
    private static final String DEFAULT_SNIPPET_ARG_ELLIPSIS = "...";
    private static final int DEFAULT_SNIPPET_ARG_MAX_TOKENS = -10;

    private boolean sIsPhoneInitialized;
    private boolean sIsPhone;

    private StringBuilder mSb = new StringBuilder();
    private String[] mSelectionArgs1 = new String[1];
    private String[] mSelectionArgs2 = new String[2];
    private ArrayList<String> mSelectionArgs = Lists.newArrayList();

    private Account mAccount;

    /**
     * Stores mapping from type Strings exposed via {@link DataUsageFeedback} to
     * type integers in {@link DataUsageStatColumns}.
     */
    private static final Map<String, Integer> sDataUsageTypeMap;

    static {
        // Contacts URI matching table
        final UriMatcher matcher = sUriMatcher;
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts", CONTACTS);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/#", CONTACTS_ID);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/#/data", CONTACTS_ID_DATA);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/#/entities", CONTACTS_ID_ENTITIES);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/#/suggestions",
                AGGREGATION_SUGGESTIONS);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/#/suggestions/*",
                AGGREGATION_SUGGESTIONS);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/#/photo", CONTACTS_ID_PHOTO);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/#/display_photo",
                CONTACTS_ID_DISPLAY_PHOTO);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/#/stream_items",
                CONTACTS_ID_STREAM_ITEMS);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/filter", CONTACTS_FILTER);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/filter/*", CONTACTS_FILTER);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/lookup/*", CONTACTS_LOOKUP);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/lookup/*/data", CONTACTS_LOOKUP_DATA);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/lookup/*/photo",
                CONTACTS_LOOKUP_PHOTO);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/lookup/*/#", CONTACTS_LOOKUP_ID);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/lookup/*/#/data",
                CONTACTS_LOOKUP_ID_DATA);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/lookup/*/#/photo",
                CONTACTS_LOOKUP_ID_PHOTO);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/lookup/*/display_photo",
                CONTACTS_LOOKUP_DISPLAY_PHOTO);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/lookup/*/#/display_photo",
                CONTACTS_LOOKUP_ID_DISPLAY_PHOTO);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/lookup/*/entities",
                CONTACTS_LOOKUP_ENTITIES);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/lookup/*/#/entities",
                CONTACTS_LOOKUP_ID_ENTITIES);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/lookup/*/stream_items",
                CONTACTS_LOOKUP_STREAM_ITEMS);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/lookup/*/#/stream_items",
                CONTACTS_LOOKUP_ID_STREAM_ITEMS);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/as_vcard/*", CONTACTS_AS_VCARD);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/as_multi_vcard/*",
                CONTACTS_AS_MULTI_VCARD);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/strequent/", CONTACTS_STREQUENT);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/strequent/filter/*",
                CONTACTS_STREQUENT_FILTER);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/group/*", CONTACTS_GROUP);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/frequent", CONTACTS_FREQUENT);
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/delete_usage", CONTACTS_DELETE_USAGE);

        matcher.addURI(GnContactsContract.AUTHORITY, "raw_contacts", RAW_CONTACTS);
        matcher.addURI(GnContactsContract.AUTHORITY, "raw_contacts/#", RAW_CONTACTS_ID);
        matcher.addURI(GnContactsContract.AUTHORITY, "raw_contacts/#/data", RAW_CONTACTS_DATA);
        matcher.addURI(GnContactsContract.AUTHORITY, "raw_contacts/#/display_photo",
                RAW_CONTACTS_ID_DISPLAY_PHOTO);
        matcher.addURI(GnContactsContract.AUTHORITY, "raw_contacts/#/entity", RAW_CONTACT_ENTITY_ID);
        matcher.addURI(GnContactsContract.AUTHORITY, "raw_contacts/#/stream_items",
                RAW_CONTACTS_ID_STREAM_ITEMS);
        matcher.addURI(GnContactsContract.AUTHORITY, "raw_contacts/#/stream_items/#",
                RAW_CONTACTS_ID_STREAM_ITEMS_ID);

        matcher.addURI(GnContactsContract.AUTHORITY, "raw_contact_entities", RAW_CONTACT_ENTITIES);

        matcher.addURI(GnContactsContract.AUTHORITY, "data", DATA);
        matcher.addURI(GnContactsContract.AUTHORITY, "data/#", DATA_ID);
        matcher.addURI(GnContactsContract.AUTHORITY, "data/phones", PHONES);
        matcher.addURI(GnContactsContract.AUTHORITY, "data/phones/#", PHONES_ID);
        matcher.addURI(GnContactsContract.AUTHORITY, "data/phones/filter", PHONES_FILTER);
        matcher.addURI(GnContactsContract.AUTHORITY, "data/phones/filter/*", PHONES_FILTER);
        matcher.addURI(GnContactsContract.AUTHORITY, "data/emails", EMAILS);
        matcher.addURI(GnContactsContract.AUTHORITY, "data/emails/#", EMAILS_ID);
        matcher.addURI(GnContactsContract.AUTHORITY, "data/emails/lookup", EMAILS_LOOKUP);
        matcher.addURI(GnContactsContract.AUTHORITY, "data/emails/lookup/*", EMAILS_LOOKUP);
        matcher.addURI(GnContactsContract.AUTHORITY, "data/emails/filter", EMAILS_FILTER);
        matcher.addURI(GnContactsContract.AUTHORITY, "data/emails/filter/*", EMAILS_FILTER);
        matcher.addURI(GnContactsContract.AUTHORITY, "data/postals", POSTALS);
        matcher.addURI(GnContactsContract.AUTHORITY, "data/postals/#", POSTALS_ID);
        /** "*" is in CSV form with data ids ("123,456,789") */
        matcher.addURI(GnContactsContract.AUTHORITY, "data/usagefeedback/*", DATA_USAGE_FEEDBACK_ID);
        // Gionee lihuafang add for phone error begin
        matcher.addURI(GnContactsContract.AUTHORITY, "data/callables/", CALLABLES);
        matcher.addURI(GnContactsContract.AUTHORITY, "data/callables/#", CALLABLES_ID);
        matcher.addURI(GnContactsContract.AUTHORITY, "data/callables/filter", CALLABLES_FILTER);
        matcher.addURI(GnContactsContract.AUTHORITY, "data/callables/filter/*", CALLABLES_FILTER);
        // Gionee lihuafang add for phone error end

        matcher.addURI(GnContactsContract.AUTHORITY, "data/contactables/", CONTACTABLES);
        matcher.addURI(GnContactsContract.AUTHORITY, "data/contactables/filter", CONTACTABLES_FILTER);
        matcher.addURI(GnContactsContract.AUTHORITY, "data/contactables/filter/*",
                CONTACTABLES_FILTER);

        matcher.addURI(GnContactsContract.AUTHORITY, "groups", GROUPS);
        matcher.addURI(GnContactsContract.AUTHORITY, "groups/#", GROUPS_ID);
        matcher.addURI(GnContactsContract.AUTHORITY, "groups_summary", GROUPS_SUMMARY);

        matcher.addURI(GnContactsContract.AUTHORITY, SyncStateContentProviderHelper.PATH, SYNCSTATE);
        matcher.addURI(GnContactsContract.AUTHORITY, SyncStateContentProviderHelper.PATH + "/#",
                SYNCSTATE_ID);
        matcher.addURI(GnContactsContract.AUTHORITY, "profile/" + SyncStateContentProviderHelper.PATH,
                PROFILE_SYNCSTATE);
        matcher.addURI(GnContactsContract.AUTHORITY,
                "profile/" + SyncStateContentProviderHelper.PATH + "/#",
                PROFILE_SYNCSTATE_ID);

        matcher.addURI(GnContactsContract.AUTHORITY, "phone_lookup/*", PHONE_LOOKUP);
        matcher.addURI(GnContactsContract.AUTHORITY, "aggregation_exceptions",
                AGGREGATION_EXCEPTIONS);
        matcher.addURI(GnContactsContract.AUTHORITY, "aggregation_exceptions/*",
                AGGREGATION_EXCEPTION_ID);

        matcher.addURI(GnContactsContract.AUTHORITY, "settings", SETTINGS);

        matcher.addURI(GnContactsContract.AUTHORITY, "status_updates", STATUS_UPDATES);
        matcher.addURI(GnContactsContract.AUTHORITY, "status_updates/#", STATUS_UPDATES_ID);

        matcher.addURI(GnContactsContract.AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY,
                SEARCH_SUGGESTIONS);
        matcher.addURI(GnContactsContract.AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*",
                SEARCH_SUGGESTIONS);
        matcher.addURI(GnContactsContract.AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*",
                SEARCH_SHORTCUT);

        matcher.addURI(GnContactsContract.AUTHORITY, "provider_status", PROVIDER_STATUS);

        matcher.addURI(GnContactsContract.AUTHORITY, "directories", DIRECTORIES);
        matcher.addURI(GnContactsContract.AUTHORITY, "directories/#", DIRECTORIES_ID);

        matcher.addURI(GnContactsContract.AUTHORITY, "complete_name", COMPLETE_NAME);

        matcher.addURI(GnContactsContract.AUTHORITY, "profile", PROFILE);
        matcher.addURI(GnContactsContract.AUTHORITY, "profile/entities", PROFILE_ENTITIES);
        matcher.addURI(GnContactsContract.AUTHORITY, "profile/data", PROFILE_DATA);
        matcher.addURI(GnContactsContract.AUTHORITY, "profile/data/#", PROFILE_DATA_ID);
        matcher.addURI(GnContactsContract.AUTHORITY, "profile/photo", PROFILE_PHOTO);
        matcher.addURI(GnContactsContract.AUTHORITY, "profile/display_photo", PROFILE_DISPLAY_PHOTO);
        matcher.addURI(GnContactsContract.AUTHORITY, "profile/as_vcard", PROFILE_AS_VCARD);
        matcher.addURI(GnContactsContract.AUTHORITY, "profile/raw_contacts", PROFILE_RAW_CONTACTS);
        matcher.addURI(GnContactsContract.AUTHORITY, "profile/raw_contacts/#",
                PROFILE_RAW_CONTACTS_ID);
        matcher.addURI(GnContactsContract.AUTHORITY, "profile/raw_contacts/#/data",
                PROFILE_RAW_CONTACTS_ID_DATA);
        matcher.addURI(GnContactsContract.AUTHORITY, "profile/raw_contacts/#/entity",
                PROFILE_RAW_CONTACTS_ID_ENTITIES);
        matcher.addURI(GnContactsContract.AUTHORITY, "profile/status_updates",
                PROFILE_STATUS_UPDATES);
        matcher.addURI(GnContactsContract.AUTHORITY, "profile/raw_contact_entities",
                PROFILE_RAW_CONTACT_ENTITIES);

        matcher.addURI(GnContactsContract.AUTHORITY, "stream_items", STREAM_ITEMS);
        matcher.addURI(GnContactsContract.AUTHORITY, "stream_items/photo", STREAM_ITEMS_PHOTOS);
        matcher.addURI(GnContactsContract.AUTHORITY, "stream_items/#", STREAM_ITEMS_ID);
        matcher.addURI(GnContactsContract.AUTHORITY, "stream_items/#/photo", STREAM_ITEMS_ID_PHOTOS);
        matcher.addURI(GnContactsContract.AUTHORITY, "stream_items/#/photo/#",
                STREAM_ITEMS_ID_PHOTOS_ID);
        matcher.addURI(GnContactsContract.AUTHORITY, "stream_items_limit", STREAM_ITEMS_LIMIT);

        matcher.addURI(GnContactsContract.AUTHORITY, "display_photo/#", DISPLAY_PHOTO);
        matcher.addURI(GnContactsContract.AUTHORITY, "photo_dimensions", PHOTO_DIMENSIONS);
        
		matcher.addURI(GnContactsContract.AUTHORITY, "deleted_contacts", DELETED_CONTACTS);
		matcher.addURI(GnContactsContract.AUTHORITY, "deleted_contacts/#", DELETED_CONTACTS_ID);

        matcher.addURI(ContactsContract.AUTHORITY, "pinned_position_update",
                PINNED_POSITION_UPDATE);
        
        // The following lines are provided and maintained by Mediatek inc.
        matcher.addURI(GnContactsContract.AUTHORITY, "data/phone_email", PHONE_EMAIL);
        matcher.addURI(GnContactsContract.AUTHORITY, "data/phone_email/filter", PHONE_EMAIL_FILTER);
        matcher.addURI(GnContactsContract.AUTHORITY, "data/phone_email/filter/*", PHONE_EMAIL_FILTER);
        matcher.addURI(GnContactsContract.AUTHORITY, "live_folders/phone_contact",
                LIVE_FOLDERS_PHONES_CONTACTS);
        matcher.addURI(GnContactsContract.AUTHORITY, "live_folders/sim_contact",
                LIVE_FOLDERS_SIM_CONTACTS);
        matcher.addURI(GnContactsContract.AUTHORITY, "live_folders/sim1_contact",
                LIVE_FOLDERS_SIM1_CONTACTS);
        matcher.addURI(GnContactsContract.AUTHORITY, "live_folders/sim2_contact",
                LIVE_FOLDERS_SIM2_CONTACTS);
        matcher.addURI(GnContactsContract.AUTHORITY, "live_folders/usim_contact",
                LIVE_FOLDERS_USIM_CONTACTS);
        matcher.addURI(GnContactsContract.AUTHORITY, "live_folders/usim1_contact",
                LIVE_FOLDERS_USIM1_CONTACTS);
        matcher.addURI(GnContactsContract.AUTHORITY, "live_folders/usim2_contact",
                LIVE_FOLDERS_USIM2_CONTACTS);
        matcher.addURI(GnContactsContract.AUTHORITY, "dialer_search/filter/*", DIALER_SEARCH_INCREMENT);
        matcher.addURI(GnContactsContract.AUTHORITY, "dialer_search_number/filter/*", DIALER_SEARCH_SIMPLE);
        // The previous lines are provided and maintained by Mediatek inc.
        
        //Gionee:huangzy 20121011 add for CR00710695 start
        matcher.addURI(GnContactsContract.AUTHORITY, "gn_dialer_search/*", GN_DIALER_SEARCH);
        matcher.addURI(GnContactsContract.AUTHORITY, "gn_dialer_search_init", GN_DIALER_SEARCH_INIT);
        //Gionee:huangzy 20121011 add for CR00710695 end
        
        //Gionee:huangzy 20121128 add for CR00736966 start
        matcher.addURI(GnContactsContract.AUTHORITY, "gn_sync_status/*", GN_SYNC_STATUS);
        //Gionee:huangzy 20121128 add for CR00736966 end
        
        //Gionee:huangzy 20121019 remove for CR00715333 start
        /*matcher.addURI(GnContactsContract.AUTHORITY, "mimetypes/id", MIMETYPES_ID);        
        matcher.addURI(GnContactsContract.AUTHORITY, "data_usage_stat", DATA_USAG_STAT);
        matcher.addURI(GnContactsContract.AUTHORITY, "data_usage_stat/clear", DATA_USAG_STAT_CLEAR);*/
        matcher.addURI(GnContactsContract.AUTHORITY, "contacts/frequent/clear", CONTACTS_FREQUENT_CLEAR);
        //Gionee:huangzy 20121019 remove for CR00715333 end
        
        // aurora <wangth> <2013-12-9> add for aurora begin
        matcher.addURI(GnContactsContract.AUTHORITY, "data/phone_emails_ime/filter/*", AURORA_SAMSUNG_MATCH);
        // aurora <wangth> <2013-12-9> add for aurora end
        
		// reject begin
		// 如果match()方法匹配content://com.aurora.reject.provider/black路径,返回匹配码为BLACKS
		matcher.addURI(GnContactsContract.AUTHORITY, "black", BLACKS);
		// 如果match()方法匹配content://com.aurora.reject.provider/black/230,路径，返回匹配码为BLACK
		matcher.addURI(GnContactsContract.AUTHORITY, "black/#", BLACK);
		// 如果match()方法匹配content://com.aurora.reject.provider/mark路径,返回匹配码为MARKS
		matcher.addURI(GnContactsContract.AUTHORITY, "mark", MARKS);
		// 如果match()方法匹配content://com.aurora.reject.provider/mark/230,路径，返回匹配码为MARK
		matcher.addURI(GnContactsContract.AUTHORITY, "mark/#", MARK);
		// reject end
		
		//contacts sync begin
		matcher.addURI(GnContactsContract.AUTHORITY, "sync", SYNCS);
		matcher.addURI(GnContactsContract.AUTHORITY, "sync/#", SYNC);
		matcher.addURI(GnContactsContract.AUTHORITY, "sync/sync_up", SYNC_UP);
		matcher.addURI(GnContactsContract.AUTHORITY, "sync/sync_up/#/#", SYNC_UP_LIMIT);
		matcher.addURI(GnContactsContract.AUTHORITY, "sync/sync_up/#", SYNC_UP_LIMIT_COUNT);
		matcher.addURI(GnContactsContract.AUTHORITY, "sync/sync_up_result_multi", SYNC_UP_RESULT);
		matcher.addURI(GnContactsContract.AUTHORITY, "sync/sync_up_size", SYNC_UP_SIZE);
		matcher.addURI(GnContactsContract.AUTHORITY, "sync/sync_down_multi", SYNC_DOWN);
		matcher.addURI(GnContactsContract.AUTHORITY, "sync/accessory", SYNC_ACCESSORY);
		matcher.addURI(GnContactsContract.AUTHORITY, "sync/is_first_sync", URI_IS_FIRST_SYNC);
		
		
		
		matcher.addURI(GnContactsContract.AUTHORITY, "sync/clean_account", URI_CLEAN_ACCOUNT);
		matcher.addURI(GnContactsContract.AUTHORITY, "sync/init_account", URI_INIT_ACCOUNT);
		matcher.addURI(GnContactsContract.AUTHORITY, "sync/init_account_multi", URI_INIT_ACCOUNT_MULTI);
		matcher.addURI(GnContactsContract.AUTHORITY, "sync/clean_data", URI_CLEAN_DATA);
		//contacts sync end
		
		matcher.addURI(GnContactsContract.AUTHORITY, "aurora_multi_search/*", AURORA_MULTI_HANZI);
        HashMap<String, Integer> tmpTypeMap = new HashMap<String, Integer>();
        tmpTypeMap.put(DataUsageFeedback.USAGE_TYPE_CALL, DataUsageStatColumns.USAGE_TYPE_INT_CALL);
        tmpTypeMap.put(DataUsageFeedback.USAGE_TYPE_LONG_TEXT,
                DataUsageStatColumns.USAGE_TYPE_INT_LONG_TEXT);
        tmpTypeMap.put(DataUsageFeedback.USAGE_TYPE_SHORT_TEXT,
                DataUsageStatColumns.USAGE_TYPE_INT_SHORT_TEXT);
        sDataUsageTypeMap = Collections.unmodifiableMap(tmpTypeMap);
    }

    private static class DirectoryInfo {
        String authority;
        String accountName;
        String accountType;
    }

    /**
     * Cached information about contact directories.
     */
    private HashMap<String, DirectoryInfo> mDirectoryCache = new HashMap<String, DirectoryInfo>();
    private boolean mDirectoryCacheValid = false;

//    /**
//     * An entry in group id cache. It maps the combination of (account type, account name, data set,
//     * and source id) to group row id.
//     */
//    public static class GroupIdCacheEntry {
//        String accountType;
//        String accountName;
//        String dataSet;
//        String sourceId;
//        long groupId;
//    }

    /**
     * An entry in group id cache.
     *
     * TODO: Move this and {@link #mGroupIdCache} to {@link DataRowHandlerForGroupMembership}.
     */
    public static class GroupIdCacheEntry {
        long accountId;
        String sourceId;
        long groupId;
    }

    // We don't need a soft cache for groups - the assumption is that there will only
    // be a small number of contact groups. The cache is keyed off source id.  The value
    // is a list of groups with this group id.
    private HashMap<String, ArrayList<GroupIdCacheEntry>> mGroupIdCache = Maps.newHashMap();

    /**
     * Maximum dimension (height or width) of display photos.  Larger images will be scaled
     * to fit.
     */
    private int mMaxDisplayPhotoDim;

    /**
     * Maximum dimension (height or width) of photo thumbnails.
     */
    private int mMaxThumbnailPhotoDim;

    /**
     * Sub-provider for handling profile requests against the profile database.
     */
    private ProfileProvider mProfileProvider;

    private NameSplitter mNameSplitter;
    private NameLookupBuilder mNameLookupBuilder;

    private PostalSplitter mPostalSplitter;

    private ContactDirectoryManager mContactDirectoryManager;

    // The database tag to use for representing the contacts DB in contacts transactions.
    /* package */ static final String CONTACTS_DB_TAG = "contacts";

    // The database tag to use for representing the profile DB in contacts transactions.
    /* package */ static final String PROFILE_DB_TAG = "profile";

    /**
     * The active (thread-local) database.  This will be switched between a contacts-specific
     * database and a profile-specific database, depending on what the current operation is
     * targeted to.
     */
    private final ThreadLocal<SQLiteDatabase> mActiveDb = new ThreadLocal<SQLiteDatabase>();

    /**
     * The thread-local holder of the active transaction.  Shared between this and the profile
     * provider, to keep transactions on both databases synchronized.
     */
    private final ThreadLocal<ContactsTransaction> mTransactionHolder =
            new ThreadLocal<ContactsTransaction>();

    // This variable keeps track of whether the current operation is intended for the profile DB.
    private final ThreadLocal<Boolean> mInProfileMode = new ThreadLocal<Boolean>();

    // Separate data row handler instances for contact data and profile data.
    private HashMap<String, DataRowHandler> mDataRowHandlers;
    private HashMap<String, DataRowHandler> mProfileDataRowHandlers;

    // Depending on whether the action being performed is for the profile, we will use one of two
    // database helper instances.
    private final ThreadLocal<ContactsDatabaseHelper> mDbHelper =
            new ThreadLocal<ContactsDatabaseHelper>();
    private ContactsDatabaseHelper mContactsHelper;
    private ProfileDatabaseHelper mProfileHelper;
    // Gionee:wangth 20121225 add for CR00751311 begin
    public static ContactsDatabaseHelper mGnContactsHelper;
    // Gionee:wangth 20121225 add for CR00751311 end

    // Depending on whether the action being performed is for the profile or not, we will use one of
    // two aggregator instances.
    private final ThreadLocal<ContactAggregator> mAggregator = new ThreadLocal<ContactAggregator>();
    private ContactAggregator mContactAggregator;
    private ContactAggregator mProfileAggregator;

    // Depending on whether the action being performed is for the profile or not, we will use one of
    // two photo store instances (with their files stored in separate subdirectories).
    private final ThreadLocal<PhotoStore> mPhotoStore = new ThreadLocal<PhotoStore>();
    private PhotoStore mContactsPhotoStore;
    private PhotoStore mProfilePhotoStore;

    // The active transaction context will switch depending on the operation being performed.
    // Both transaction contexts will be cleared out when a batch transaction is started, and
    // each will be processed separately when a batch transaction completes.
    private TransactionContext mContactTransactionContext = new TransactionContext(false);
    private TransactionContext mProfileTransactionContext = new TransactionContext(true);
    private final ThreadLocal<TransactionContext> mTransactionContext =
            new ThreadLocal<TransactionContext>();

    // Duration in milliseconds that pre-authorized URIs will remain valid.
    private long mPreAuthorizedUriDuration;

    // Map of single-use pre-authorized URIs to expiration times.
    private Map<Uri, Long> mPreAuthorizedUris = Maps.newHashMap();

    // Random number generator.
    private SecureRandom mRandom = new SecureRandom();

    private LegacyApiSupport mLegacyApiSupport;
    private GlobalSearchSupport mGlobalSearchSupport;
    private CommonNicknameCache mCommonNicknameCache;
    private SearchIndexManager mSearchIndexManager;

    private ContentValues mValues = new ContentValues();
    private HashMap<String, Boolean> mAccountWritability = Maps.newHashMap();

    private int mProviderStatus = ProviderStatus.STATUS_NORMAL;
    private boolean mProviderStatusUpdateNeeded;
    private long mEstimatedStorageRequirement = 0;
    private volatile CountDownLatch mReadAccessLatch;
    private volatile CountDownLatch mWriteAccessLatch;
    private boolean mAccountUpdateListenerRegistered;
    private boolean mOkToOpenAccess = true;

    private boolean mVisibleTouched = false;

    private boolean mSyncToNetwork;

    private Locale mCurrentLocale;
    private int mContactsAccountCount;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    private long mLastPhotoCleanup = 0;

    // The following lines are provided and maintained by Mediatek Inc.
    private HandlerThread mListenerThread;
    private Handler mListenerHandler;
    private static final int MESSAGE_LOAD_DATA = 0;
    // The following lines are provided and maintained by Mediatek inc.
    // Added Local Account Type
    public static final String ACCOUNT_TYPE_SIM = "SIM Account";
    public static final String ACCOUNT_TYPE_USIM = "USIM Account";
    public static final String ACCOUNT_TYPE_LOCAL_PHONE = "Local Phone Account";

    // Added Local Account Name - For Sim/Usim Only
    public static final String ACCOUNT_NAME_SIM = "SIM" + SimCardUtils.SimSlot.SLOT_ID1;
    public static final String ACCOUNT_NAME_SIM2 = "SIM" + SimCardUtils.SimSlot.SLOT_ID2;
    public static final String ACCOUNT_NAME_USIM = "USIM" + SimCardUtils.SimSlot.SLOT_ID1;
    public static final String ACCOUNT_NAME_USIM2 = "USIM" + SimCardUtils.SimSlot.SLOT_ID2;
    // The previous lines are provided and maintained by Mediatek inc.    
    // The previous lines are provided and maintained by Mediatek Inc.
    
    
    private SharedPreferences sp=null;
    private Intent syncIntent;
    @Override
    public boolean onCreate() {
    	sp=getContext().getSharedPreferences("firstSyncFlag", 0);
    	syncIntent = new Intent("com.aurora.account.START_SYNC");  
    	syncIntent.putExtra("packageName", "com.android.contacts");  
        if (Log.isLoggable(Constants.PERFORMANCE_TAG, Log.DEBUG)) {
            Log.d(Constants.PERFORMANCE_TAG, "ContactsProvider2.onCreate start");
        }
		mUseStrictPhoneNumberComparation = false;
        super.onCreate();
        try {
            return initialize();
        } catch (RuntimeException e) {
            Log.e(TAG, "Cannot start provider", e);
            return false;
        } finally {
            if (Log.isLoggable(Constants.PERFORMANCE_TAG, Log.DEBUG)) {
                Log.d(Constants.PERFORMANCE_TAG, "ContactsProvider2.onCreate finish");
            }
        }
    }

    private boolean initialize() {
        StrictMode.setThreadPolicy(
                new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());

        Resources resources = getContext().getResources();
        //Gionee:huangzy 20120615 modify for CR00624875 start
        int maxDisplayPhotoDimResId = ContactsProvidersApplication.sIsGnZoomClipSupport ?
                R.integer.gn_config_max_display_photo_dim : R.integer.config_max_display_photo_dim;
        mMaxDisplayPhotoDim = resources.getInteger(maxDisplayPhotoDimResId);
        //Gionee:huangzy 20120615 modify for CR00624875 end
        
        if(ContactsProvidersApplication.sIsCtsSupport){
            mMaxThumbnailPhotoDim = resources.getInteger(
                    R.integer.config_max_thumbnail_photo_dim_for_cts);
        } else {
            mMaxThumbnailPhotoDim = resources.getInteger(
                        R.integer.config_max_thumbnail_photo_dim);
        }

        mContactsHelper = getDatabaseHelper(getContext());
        mDbHelper.set(mContactsHelper);
        // Gionee:wangth 20121225 add for CR00751311 begin
        mGnContactsHelper = mContactsHelper;
        // Gionee:wangth 20121225 add for CR00751311 end

        // Set up the DB helper for keeping transactions serialized.
        setDbHelperToSerializeOn(mContactsHelper, CONTACTS_DB_TAG);

        mContactDirectoryManager = new ContactDirectoryManager(this);
        mGlobalSearchSupport = new GlobalSearchSupport(this);

        // The provider is closed for business until fully initialized
        mReadAccessLatch = new CountDownLatch(1);
        mWriteAccessLatch = new CountDownLatch(1);

        mBackgroundThread = new HandlerThread("ContactsProviderWorker",
                Process.THREAD_PRIORITY_BACKGROUND);
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                performBackgroundTask(msg.what, msg.obj);
            }
        };

        // Set up the sub-provider for handling profiles.
        mProfileProvider = getProfileProvider();
        mProfileProvider.setDbHelperToSerializeOn(mContactsHelper, CONTACTS_DB_TAG);
        ProviderInfo profileInfo = new ProviderInfo();
        profileInfo.readPermission = "android.permission.READ_PROFILE";
        profileInfo.writePermission = "android.permission.WRITE_PROFILE";
        mProfileProvider.attachInfo(getContext(), profileInfo);
        mProfileHelper = mProfileProvider.getDatabaseHelper(getContext());

        // Initialize the pre-authorized URI duration.
        // aurora <wangth> <2013-10-22> modify for aurora ui begin
//        mPreAuthorizedUriDuration = android.provider.Settings.Secure.getLong(
//                getContext().getContentResolver(),
//                /*android.provider.Settings.Secure.CONTACTS_PREAUTH_URI_EXPIRATION*/
//                "contacts_preauth_uri_expiration",
//                DEFAULT_PREAUTHORIZED_URI_EXPIRATION);
        mPreAuthorizedUriDuration = DEFAULT_PREAUTHORIZED_URI_EXPIRATION;
        // aurora <wangth> <2013-10-22> modify for aurora ui end

        scheduleBackgroundTask(BACKGROUND_TASK_INITIALIZE);
        scheduleBackgroundTask(BACKGROUND_TASK_IMPORT_LEGACY_CONTACTS);
        scheduleBackgroundTask(BACKGROUND_TASK_UPDATE_ACCOUNTS);
        scheduleBackgroundTask(BACKGROUND_TASK_UPDATE_LOCALE);
        scheduleBackgroundTask(BACKGROUND_TASK_UPGRADE_AGGREGATION_ALGORITHM);
        scheduleBackgroundTask(BACKGROUND_TASK_UPDATE_SEARCH_INDEX);
        scheduleBackgroundTask(BACKGROUND_TASK_UPDATE_PROVIDER_STATUS);
        scheduleBackgroundTask(BACKGROUND_TASK_OPEN_WRITE_ACCESS);
        scheduleBackgroundTask(BACKGROUND_TASK_CLEANUP_PHOTOS);

        return true;
    }

    /**
     * (Re)allocates all locale-sensitive structures.
     */
    private void initForDefaultLocale() {
        Context context = getContext();
        mLegacyApiSupport = new LegacyApiSupport(context, mContactsHelper, this,
                mGlobalSearchSupport);
        mCurrentLocale = getLocale();
        mNameSplitter = mContactsHelper.createNameSplitter();
        mNameLookupBuilder = new StructuredNameLookupBuilder(mNameSplitter);
        mPostalSplitter = new PostalSplitter(mCurrentLocale);
        mCommonNicknameCache = new CommonNicknameCache(mContactsHelper.getReadableDatabase());
        ContactLocaleUtils.getIntance().setLocale(mCurrentLocale);
        mContactAggregator = new ContactAggregator(this, mContactsHelper,
                createPhotoPriorityResolver(context), mNameSplitter, mCommonNicknameCache);
        mContactAggregator.setEnabled(SystemProperties.getBoolean(AGGREGATE_CONTACTS, true));
        mProfileAggregator = new ProfileAggregator(this, mProfileHelper,
                createPhotoPriorityResolver(context), mNameSplitter, mCommonNicknameCache);
        mProfileAggregator.setEnabled(SystemProperties.getBoolean(AGGREGATE_CONTACTS, true));
        mSearchIndexManager = new SearchIndexManager(this);

        mContactsPhotoStore = new PhotoStore(getContext().getFilesDir(), mContactsHelper);
        mProfilePhotoStore = new PhotoStore(new File(getContext().getFilesDir(), "profile"),
                mProfileHelper);

        mDataRowHandlers = new HashMap<String, DataRowHandler>();
        initDataRowHandlers(mDataRowHandlers, mContactsHelper, mContactAggregator,
                mContactsPhotoStore);
        mProfileDataRowHandlers = new HashMap<String, DataRowHandler>();
        initDataRowHandlers(mProfileDataRowHandlers, mProfileHelper, mProfileAggregator,
                mProfilePhotoStore);

        // Set initial thread-local state variables for the Contacts DB.
        switchToContactMode();
    }

    private void initDataRowHandlers(Map<String, DataRowHandler> handlerMap,
            ContactsDatabaseHelper dbHelper, ContactAggregator contactAggregator,
            PhotoStore photoStore) {
        Context context = getContext();
        handlerMap.put(Email.CONTENT_ITEM_TYPE,
                new DataRowHandlerForEmail(context, dbHelper, contactAggregator));
        handlerMap.put(Im.CONTENT_ITEM_TYPE,
                new DataRowHandlerForIm(context, dbHelper, contactAggregator));
        handlerMap.put(Organization.CONTENT_ITEM_TYPE,
                new DataRowHandlerForOrganization(context, dbHelper, contactAggregator));
        
        /*
         * Feature Fix by Mediatek Begin
         *  
         * Original Android code:
         * handlerMap.put(Phone.CONTENT_ITEM_TYPE,
         *     new DataRowHandlerForPhoneNumber(context, dbHelper, contactAggregator));
         * handlerMap.put(StructuredName.CONTENT_ITEM_TYPE,
         *     new DataRowHandlerForStructuredName(context, dbHelper, contactAggregator,
         *			mNameSplitter, mNameLookupBuilder));
         */
        
        handlerMap.put(Phone.CONTENT_ITEM_TYPE,
        		new DataRowHandlerForPhoneNumberEx(context, dbHelper, contactAggregator));
        handlerMap.put(StructuredName.CONTENT_ITEM_TYPE,
        		new DataRowHandlerForStructuredNameEx(context, dbHelper, contactAggregator,
        				mNameSplitter, mNameLookupBuilder));
        handlerMap.put(SipAddress.CONTENT_ITEM_TYPE,
        		new DataRowHandlerForSipAddress(context, dbHelper, contactAggregator));
        if (ContactsProvider2.isCmcc()) {
            handlerMap.put(Website.CONTENT_ITEM_TYPE, 
            		new DataRowHandlerForWebsite(context, dbHelper, contactAggregator));
        }
        /*
         * Feature Fix by Mediatek End
         */
        
        handlerMap.put(Nickname.CONTENT_ITEM_TYPE,
        		new DataRowHandlerForNickname(context, dbHelper, contactAggregator));
        handlerMap.put(StructuredPostal.CONTENT_ITEM_TYPE,
                new DataRowHandlerForStructuredPostal(context, dbHelper, contactAggregator,
                        mPostalSplitter));
        handlerMap.put(GroupMembership.CONTENT_ITEM_TYPE,
                new DataRowHandlerForGroupMembership(context, dbHelper, contactAggregator,
                        mGroupIdCache));
        handlerMap.put(Photo.CONTENT_ITEM_TYPE,
                new DataRowHandlerForPhoto(context, dbHelper, contactAggregator, photoStore));
        handlerMap.put(Note.CONTENT_ITEM_TYPE,
                new DataRowHandlerForNote(context, dbHelper, contactAggregator));
    }

    /**
     * Visible for testing.
     */
    /* package */ PhotoPriorityResolver createPhotoPriorityResolver(Context context) {
        return new PhotoPriorityResolver(context);
    }

    protected void scheduleBackgroundTask(int task) {
        mBackgroundHandler.sendEmptyMessage(task);
    }

    protected void scheduleBackgroundTask(int task, Object arg) {
        mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(task, arg));
    }

    protected void performBackgroundTask(int task, Object arg) {
        Log.d(TAG, "performBackgroundTask **** task="+ task);
        switch (task) {
            case BACKGROUND_TASK_INITIALIZE: {
                initForDefaultLocale();
                // The following lines are provided and maintained by Mediatek Inc.
                loadLocalPhoneAccounts();
                //handleLocalAccountChanged();
                registerReceiverOnSimStateAndInfoChanged();
                // The previous lines are provided and maintained by Mediatek Inc.
                
                mReadAccessLatch.countDown();
                mReadAccessLatch = null;
                break;
            }

            case BACKGROUND_TASK_OPEN_WRITE_ACCESS: {
                if (mOkToOpenAccess) {
                    mWriteAccessLatch.countDown();
                    mWriteAccessLatch = null;
                }
                break;
            }

            case BACKGROUND_TASK_IMPORT_LEGACY_CONTACTS: {
                if (isLegacyContactImportNeeded()) {
                    importLegacyContactsInBackground();
                }
                break;
            }

            case BACKGROUND_TASK_UPDATE_ACCOUNTS: {
                Context context = getContext();
                if (!mAccountUpdateListenerRegistered) {
                    AccountManager.get(context).addOnAccountsUpdatedListener(this, null, false);
                    mAccountUpdateListenerRegistered = true;
                }

                // Update the accounts for both the contacts and profile DBs.
                Account[] accounts = AccountManager.get(context).getAccounts();
                switchToContactMode();
                boolean accountsChanged = updateAccountsInBackground(accounts);
                switchToProfileMode();
                accountsChanged |= updateAccountsInBackground(accounts);

                updateContactsAccountCount(accounts);
                updateDirectoriesInBackground(accountsChanged);
                break;
            }

            case BACKGROUND_TASK_UPDATE_LOCALE: {
                updateLocaleInBackground();
                break;
            }

            case BACKGROUND_TASK_CHANGE_LOCALE: {
                changeLocaleInBackground();
                break;
            }

            case BACKGROUND_TASK_UPGRADE_AGGREGATION_ALGORITHM: {
                if (isAggregationUpgradeNeeded()) {
                    upgradeAggregationAlgorithmInBackground();
                }
                break;
            }

            case BACKGROUND_TASK_UPDATE_SEARCH_INDEX: {
                updateSearchIndexInBackground();
                break;
            }

            case BACKGROUND_TASK_UPDATE_PROVIDER_STATUS: {
                updateProviderStatus();
                break;
            }

            case BACKGROUND_TASK_UPDATE_DIRECTORIES: {
                if (arg != null) {
                    mContactDirectoryManager.onPackageChanged((String) arg);
                }
                break;
            }

            case BACKGROUND_TASK_CLEANUP_PHOTOS: {
                // Check rate limit.
                long now = System.currentTimeMillis();
                if (now - mLastPhotoCleanup > PHOTO_CLEANUP_RATE_LIMIT) {
                    mLastPhotoCleanup = now;

                    // Clean up photo stores for both contacts and profiles.
                    switchToContactMode();
                    cleanupPhotoStore();
                    switchToProfileMode();
                    cleanupPhotoStore();
                    //break;
                }
                break;
            }

            case BACKGROUND_TASK_CLEAN_DELETE_LOG: {
                final SQLiteDatabase db = mDbHelper.get().getWritableDatabase();
                DeletedContactsTableUtil.deleteOldLogs(db);
            }
            case BACKGROUND_TASK_LOAD_LOCAL_ACCOUNT: {
                loadAccountsInBackground();
                break;
            }
        }
    }

    public void onLocaleChanged() {
        if (mProviderStatus != ProviderStatus.STATUS_NORMAL
                && mProviderStatus != ProviderStatus.STATUS_NO_ACCOUNTS_NO_CONTACTS) {
            return;
        }

        scheduleBackgroundTask(BACKGROUND_TASK_CHANGE_LOCALE);
    }

    /**
     * Verifies that the contacts database is properly configured for the current locale.
     * If not, changes the database locale to the current locale using an asynchronous task.
     * This needs to be done asynchronously because the process involves rebuilding
     * large data structures (name lookup, sort keys), which can take minutes on
     * a large set of contacts.
     */
    protected void updateLocaleInBackground() {

        // The process is already running - postpone the change
        if (mProviderStatus == ProviderStatus.STATUS_CHANGING_LOCALE) {
            return;
        }

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        final String providerLocale = prefs.getString(PREF_LOCALE, null);
        final Locale currentLocale = mCurrentLocale;
        if (currentLocale.toString().equals(providerLocale)) {
            return;
        }

        int providerStatus = mProviderStatus;
        setProviderStatus(ProviderStatus.STATUS_CHANGING_LOCALE);
        mContactsHelper.setLocale(this, currentLocale, false);
        mProfileHelper.setLocale(this, currentLocale, false);
        prefs.edit().putString(PREF_LOCALE, currentLocale.toString()).apply();
        setProviderStatus(providerStatus);
    }

    /**
     * Reinitializes the provider for a new locale.
     */
    private void changeLocaleInBackground() {
        // Re-initializing the provider without stopping it.
        // Locking the database will prevent inserts/updates/deletes from
        // running at the same time, but queries may still be running
        // on other threads. Those queries may return inconsistent results.
        SQLiteDatabase db = mContactsHelper.getWritableDatabase();
        SQLiteDatabase profileDb = mProfileHelper.getWritableDatabase();
        db.beginTransaction();
        profileDb.beginTransaction();
        try {
            initForDefaultLocale();
            db.setTransactionSuccessful();
            profileDb.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            profileDb.endTransaction();
        }

        updateLocaleInBackground();
    }

    protected void updateSearchIndexInBackground() {
        mSearchIndexManager.updateIndex();
    }

    protected void updateDirectoriesInBackground(boolean rescan) {
        mContactDirectoryManager.scanAllPackages(rescan);
    }

    private void updateProviderStatus() {
        if (mProviderStatus != ProviderStatus.STATUS_NORMAL
                && mProviderStatus != ProviderStatus.STATUS_NO_ACCOUNTS_NO_CONTACTS) {
            return;
        }

        // No accounts/no contacts status is true if there are no account and
        // there are no contacts or one profile contact
        if (mContactsAccountCount == 0) {
            long contactsNum = DatabaseUtils.queryNumEntries(mContactsHelper.getReadableDatabase(),
                    Tables.CONTACTS, null);
            long profileNum = DatabaseUtils.queryNumEntries(mProfileHelper.getReadableDatabase(),
                    Tables.CONTACTS, null);

            // TODO: Different status if there is a profile but no contacts?
            if (contactsNum == 0 && profileNum <= 1) {
                setProviderStatus(ProviderStatus.STATUS_NO_ACCOUNTS_NO_CONTACTS);
            } else {
                setProviderStatus(ProviderStatus.STATUS_NORMAL);
            }
        } else {
            setProviderStatus(ProviderStatus.STATUS_NORMAL);
        }
    }

    /* Visible for testing */
//    protected void cleanupPhotoStore() {
//        SQLiteDatabase db = mDbHelper.get().getWritableDatabase();
//        mActiveDb.set(db);
//
//        // Assemble the set of photo store file IDs that are in use, and send those to the photo
//        // store.  Any photos that aren't in that set will be deleted, and any photos that no
//        // longer exist in the photo store will be returned for us to clear out in the DB.
//        long photoMimeTypeId = mDbHelper.get().getMimeTypeId(Photo.CONTENT_ITEM_TYPE);
//        Cursor c = db.query(Views.DATA, new String[]{Data._ID, Photo.PHOTO_FILE_ID},
//                DataColumns.MIMETYPE_ID + "=" + photoMimeTypeId + " AND "
//                        + Photo.PHOTO_FILE_ID + " IS NOT NULL", null, null, null, null);
//        Set<Long> usedPhotoFileIds = Sets.newHashSet();
//        Map<Long, Long> photoFileIdToDataId = Maps.newHashMap();
//        try {
//            while (c.moveToNext()) {
//                long dataId = c.getLong(0);
//                long photoFileId = c.getLong(1);
//                usedPhotoFileIds.add(photoFileId);
//                photoFileIdToDataId.put(photoFileId, dataId);
//            }
//        } finally {
//            c.close();
//        }
//
//        // Also query for all social stream item photos.
//        c = db.query(Tables.STREAM_ITEM_PHOTOS + " JOIN " + Tables.STREAM_ITEMS
//                + " ON " + StreamItemPhotos.STREAM_ITEM_ID + "=" + StreamItemsColumns.CONCRETE_ID
//                + " JOIN " + Tables.RAW_CONTACTS
//                + " ON " + StreamItems.RAW_CONTACT_ID + "=" + RawContactsColumns.CONCRETE_ID,
//                new String[]{
//                        StreamItemPhotosColumns.CONCRETE_ID,
//                        StreamItemPhotosColumns.CONCRETE_STREAM_ITEM_ID,
//                        StreamItemPhotos.PHOTO_FILE_ID,
//                        RawContacts.ACCOUNT_TYPE,
//                        RawContacts.ACCOUNT_NAME
//                },
//                null, null, null, null, null);
//        Map<Long, Long> photoFileIdToStreamItemPhotoId = Maps.newHashMap();
//        Map<Long, Long> streamItemPhotoIdToStreamItemId = Maps.newHashMap();
//        Map<Long, Account> streamItemPhotoIdToAccount = Maps.newHashMap();
//        try {
//            while (c.moveToNext()) {
//                long streamItemPhotoId = c.getLong(0);
//                long streamItemId = c.getLong(1);
//                long photoFileId = c.getLong(2);
//                String accountType = c.getString(3);
//                String accountName = c.getString(4);
//                usedPhotoFileIds.add(photoFileId);
//                photoFileIdToStreamItemPhotoId.put(photoFileId, streamItemPhotoId);
//                streamItemPhotoIdToStreamItemId.put(streamItemPhotoId, streamItemId);
//                Account account = new Account(accountName, accountType);
//                streamItemPhotoIdToAccount.put(photoFileId, account);
//            }
//        } finally {
//            c.close();
//        }
//
//        // Run the photo store cleanup.
//        Set<Long> missingPhotoIds = mPhotoStore.get().cleanup(usedPhotoFileIds);
//
//        // If any of the keys we're using no longer exist, clean them up.  We need to do these
//        // using internal APIs or direct DB access to avoid permission errors.
//        if (!missingPhotoIds.isEmpty()) {
//            try {
//                db.beginTransactionWithListener(this);
//                for (long missingPhotoId : missingPhotoIds) {
//                    if (photoFileIdToDataId.containsKey(missingPhotoId)) {
//                        long dataId = photoFileIdToDataId.get(missingPhotoId);
//                        ContentValues updateValues = new ContentValues();
//                        updateValues.putNull(Photo.PHOTO_FILE_ID);
//                        updateData(ContentUris.withAppendedId(Data.CONTENT_URI, dataId),
//                                updateValues, null, null, false);
//                    }
//                    if (photoFileIdToStreamItemPhotoId.containsKey(missingPhotoId)) {
//                        // For missing photos that were in stream item photos, just delete the
//                        // stream item photo.
//                        long streamItemPhotoId = photoFileIdToStreamItemPhotoId.get(missingPhotoId);
//                        db.delete(Tables.STREAM_ITEM_PHOTOS, StreamItemPhotos._ID + "=?",
//                                new String[]{String.valueOf(streamItemPhotoId)});
//                    }
//                }
//                db.setTransactionSuccessful();
//            } catch (Exception e) {
//                // Cleanup failure is not a fatal problem.  We'll try again later.
//                Log.e(TAG, "Failed to clean up outdated photo references", e);
//            } finally {
//                db.endTransaction();
//            }
//        }
//    }
    protected void cleanupPhotoStore() {
        final SQLiteDatabase db = mDbHelper.get().getWritableDatabase();

        // Assemble the set of photo store file IDs that are in use, and send those to the photo
        // store.  Any photos that aren't in that set will be deleted, and any photos that no
        // longer exist in the photo store will be returned for us to clear out in the DB.
        long photoMimeTypeId = mDbHelper.get().getMimeTypeId(Photo.CONTENT_ITEM_TYPE);
        Cursor c = db.query(Views.DATA, new String[]{Data._ID, Photo.PHOTO_FILE_ID},
                DataColumns.MIMETYPE_ID + "=" + photoMimeTypeId + " AND "
                        + Photo.PHOTO_FILE_ID + " IS NOT NULL", null, null, null, null);
        Set<Long> usedPhotoFileIds = Sets.newHashSet();
        Map<Long, Long> photoFileIdToDataId = Maps.newHashMap();
        try {
            while (c.moveToNext()) {
                long dataId = c.getLong(0);
                long photoFileId = c.getLong(1);
                usedPhotoFileIds.add(photoFileId);
                photoFileIdToDataId.put(photoFileId, dataId);
            }
        } finally {
            c.close();
        }

        // Also query for all social stream item photos.
        c = db.query(Tables.STREAM_ITEM_PHOTOS + " JOIN " + Tables.STREAM_ITEMS
                + " ON " + StreamItemPhotos.STREAM_ITEM_ID + "=" + StreamItemsColumns.CONCRETE_ID,
                new String[]{
                        StreamItemPhotosColumns.CONCRETE_ID,
                        StreamItemPhotosColumns.CONCRETE_STREAM_ITEM_ID,
                        StreamItemPhotos.PHOTO_FILE_ID
                },
                null, null, null, null, null);
        Map<Long, Long> photoFileIdToStreamItemPhotoId = Maps.newHashMap();
        Map<Long, Long> streamItemPhotoIdToStreamItemId = Maps.newHashMap();
        try {
            while (c.moveToNext()) {
                long streamItemPhotoId = c.getLong(0);
                long streamItemId = c.getLong(1);
                long photoFileId = c.getLong(2);
                usedPhotoFileIds.add(photoFileId);
                photoFileIdToStreamItemPhotoId.put(photoFileId, streamItemPhotoId);
                streamItemPhotoIdToStreamItemId.put(streamItemPhotoId, streamItemId);
            }
        } finally {
            c.close();
        }

        // Run the photo store cleanup.
        Set<Long> missingPhotoIds = mPhotoStore.get().cleanup(usedPhotoFileIds);

        // If any of the keys we're using no longer exist, clean them up.  We need to do these
        // using internal APIs or direct DB access to avoid permission errors.
        if (!missingPhotoIds.isEmpty()) {
            try {
                // Need to set the db listener because we need to run onCommit afterwards.
                // Make sure to use the proper listener depending on the current mode.
                db.beginTransactionWithListener(inProfileMode() ? mProfileProvider : this);
                for (long missingPhotoId : missingPhotoIds) {
                    if (photoFileIdToDataId.containsKey(missingPhotoId)) {
                        long dataId = photoFileIdToDataId.get(missingPhotoId);
                        ContentValues updateValues = new ContentValues();
                        updateValues.putNull(Photo.PHOTO_FILE_ID);
                        updateData(ContentUris.withAppendedId(Data.CONTENT_URI, dataId),
                                updateValues, null, null, false);
                    }
                    if (photoFileIdToStreamItemPhotoId.containsKey(missingPhotoId)) {
                        // For missing photos that were in stream item photos, just delete the
                        // stream item photo.
                        long streamItemPhotoId = photoFileIdToStreamItemPhotoId.get(missingPhotoId);
                        db.delete(Tables.STREAM_ITEM_PHOTOS, StreamItemPhotos._ID + "=?",
                                new String[]{String.valueOf(streamItemPhotoId)});
                    }
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                // Cleanup failure is not a fatal problem.  We'll try again later.
                Log.e(TAG, "Failed to clean up outdated photo references", e);
            } finally {
                db.endTransaction();
            }
        }
    }


    @Override
    protected ContactsDatabaseHelper getDatabaseHelper(final Context context) {
        return ContactsDatabaseHelper.getInstance(context);
    }

    @Override
    protected ThreadLocal<ContactsTransaction> getTransactionHolder() {
        return mTransactionHolder;
    }

    public ProfileProvider getProfileProvider() {
        return new ProfileProvider(this);
    }

    @VisibleForTesting
    /* package */ PhotoStore getPhotoStore() {
        return mContactsPhotoStore;
    }

    @VisibleForTesting
    /* package */ PhotoStore getProfilePhotoStore() {
        return mProfilePhotoStore;
    }

    /* package */ int getMaxDisplayPhotoDim() {
        return mMaxDisplayPhotoDim;
    }

    /* package */ int getMaxThumbnailPhotoDim() {
        return mMaxThumbnailPhotoDim;
    }

    /* package */ NameSplitter getNameSplitter() {
        return mNameSplitter;
    }

    /* package */ NameLookupBuilder getNameLookupBuilder() {
        return mNameLookupBuilder;
    }

    /* Visible for testing */
    public ContactDirectoryManager getContactDirectoryManagerForTest() {
        return mContactDirectoryManager;
    }

    /* Visible for testing */
    protected Locale getLocale() {
        return Locale.getDefault();
    }

    private boolean inProfileMode() {
        Boolean profileMode = mInProfileMode.get();
        return profileMode != null && profileMode;
    }

    protected boolean isLegacyContactImportNeeded() {
        int version = Integer.parseInt(
                mContactsHelper.getProperty(PROPERTY_CONTACTS_IMPORTED, "0"));
        return version < PROPERTY_CONTACTS_IMPORT_VERSION;
    }

    protected LegacyContactImporter getLegacyContactImporter() {
        return new LegacyContactImporter(getContext(), this);
    }

    /**
     * Imports legacy contacts as a background task.
     */
    private void importLegacyContactsInBackground() {
        Log.v(TAG, "Importing legacy contacts");
        setProviderStatus(ProviderStatus.STATUS_UPGRADING);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        mContactsHelper.setLocale(this, mCurrentLocale, true);
        prefs.edit().putString(PREF_LOCALE, mCurrentLocale.toString()).commit();

        LegacyContactImporter importer = getLegacyContactImporter();
        if (importLegacyContacts(importer)) {
            onLegacyContactImportSuccess();
        } else {
            onLegacyContactImportFailure();
        }
    }

    /**
     * Unlocks the provider and declares that the import process is complete.
     */
    private void onLegacyContactImportSuccess() {
        NotificationManager nm =
            (NotificationManager)getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(LEGACY_IMPORT_FAILED_NOTIFICATION);

        // Store a property in the database indicating that the conversion process succeeded
        mContactsHelper.setProperty(PROPERTY_CONTACTS_IMPORTED,
                String.valueOf(PROPERTY_CONTACTS_IMPORT_VERSION));
        setProviderStatus(ProviderStatus.STATUS_NORMAL);
        Log.v(TAG, "Completed import of legacy contacts");
    }

    /**
     * Announces the provider status and keeps the provider locked.
     */
    private void onLegacyContactImportFailure() {
        Context context = getContext();
        NotificationManager nm =
            (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Show a notification
        Notification n = new Notification(android.R.drawable.stat_notify_error,
                context.getString(R.string.upgrade_out_of_memory_notification_ticker),
                System.currentTimeMillis());
        n.setLatestEventInfo(context,
                context.getString(R.string.upgrade_out_of_memory_notification_title),
                context.getString(R.string.upgrade_out_of_memory_notification_text),
                PendingIntent.getActivity(context, 0, new Intent(Intents.UI.LIST_DEFAULT), 0));
        n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

        nm.notify(LEGACY_IMPORT_FAILED_NOTIFICATION, n);

        setProviderStatus(ProviderStatus.STATUS_UPGRADE_OUT_OF_MEMORY);
        Log.v(TAG, "Failed to import legacy contacts");

        // Do not let any database changes until this issue is resolved.
        mOkToOpenAccess = false;
    }

    /* Visible for testing */
    /* package */ boolean importLegacyContacts(LegacyContactImporter importer) {
        boolean aggregatorEnabled = mContactAggregator.isEnabled();
        mContactAggregator.setEnabled(false);
        try {
            if (importer.importContacts()) {

                // TODO aggregate all newly added raw contacts
                mContactAggregator.setEnabled(aggregatorEnabled);
                return true;
            }
        } catch (Throwable e) {
           Log.e(TAG, "Legacy contact import failed", e);
        }
        mEstimatedStorageRequirement = importer.getEstimatedStorageRequirement();
        return false;
    }

    /**
     * Wipes all data from the contacts database.
     */
    /* package */ void wipeData() {
        mContactsHelper.wipeData();
        mProfileHelper.wipeData();
        mContactsPhotoStore.clear();
        mProfilePhotoStore.clear();
        mProviderStatus = ProviderStatus.STATUS_NO_ACCOUNTS_NO_CONTACTS;
    }

    /**
     * During intialization, this content provider will
     * block all attempts to change contacts data. In particular, it will hold
     * up all contact syncs. As soon as the import process is complete, all
     * processes waiting to write to the provider are unblocked and can proceed
     * to compete for the database transaction monitor.
     */
    private void waitForAccess(CountDownLatch latch) {
        if (latch == null) {
            return;
        }

        while (true) {
            try {
                latch.await();
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Determines whether the given URI should be directed to the profile
     * database rather than the contacts database.  This is true under either
     * of three conditions:
     * 1. The URI itself is specifically for the profile.
     * 2. The URI contains ID references that are in the profile ID-space.
     * 3. The URI contains lookup key references that match the special profile lookup key.
     * @param uri The URI to examine.
     * @return Whether to direct the DB operation to the profile database.
     */
    private boolean mapsToProfileDb(Uri uri) {
        return sUriMatcher.mapsToProfile(uri);
    }

    /**
     * Determines whether the given URI with the given values being inserted
     * should be directed to the profile database rather than the contacts
     * database.  This is true if the URI already maps to the profile DB from
     * a call to {@link #mapsToProfileDb} or if the URI matches a URI that
     * specifies parent IDs via the ContentValues, and the given ContentValues
     * contains an ID in the profile ID-space.
     * @param uri The URI to examine.
     * @param values The values being inserted.
     * @return Whether to direct the DB insert to the profile database.
     */
    private boolean mapsToProfileDbWithInsertedValues(Uri uri, ContentValues values) {
        if (mapsToProfileDb(uri)) {
            return true;
        }
        int match = sUriMatcher.match(uri);
        if (INSERT_URI_ID_VALUE_MAP.containsKey(match)) {
            String idField = INSERT_URI_ID_VALUE_MAP.get(match);
            if (values.containsKey(idField)) {
                long id = values.getAsLong(idField);
                if (GnContactsContract.isProfileId(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Switches the provider's thread-local context variables to prepare for performing
     * a profile operation.
     */
    protected void switchToProfileMode() {
        mDbHelper.set(mProfileHelper);
        mTransactionContext.set(mProfileTransactionContext);
        mAggregator.set(mProfileAggregator);
        mPhotoStore.set(mProfilePhotoStore);
        mInProfileMode.set(true);
    }

    /**
     * Switches the provider's thread-local context variables to prepare for performing
     * a contacts operation.
     */
    protected void switchToContactMode() {
        mDbHelper.set(mContactsHelper);
        mTransactionContext.set(mContactTransactionContext);
        mAggregator.set(mContactAggregator);
        mPhotoStore.set(mContactsPhotoStore);
        mInProfileMode.set(false);

        // Clear out the active database; modification operations will set this to the contacts DB.
        mActiveDb.set(null);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        waitForAccess(mWriteAccessLatch);

        // Enforce stream items access check if applicable.
        enforceSocialStreamWritePermission(uri);

        if (mapsToProfileDbWithInsertedValues(uri, values)) {
            switchToProfileMode();
            return mProfileProvider.insert(uri, values);
        } else {
            switchToContactMode();
            return super.insert(uri, values);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (mWriteAccessLatch != null) {
            // We are stuck trying to upgrade contacts db.  The only update request
            // allowed in this case is an update of provider status, which will trigger
            // an attempt to upgrade contacts again.
            int match = sUriMatcher.match(uri);
            if (match == PROVIDER_STATUS) {
                Integer newStatus = values.getAsInteger(ProviderStatus.STATUS);
                if (newStatus != null && newStatus == ProviderStatus.STATUS_UPGRADING) {
                    scheduleBackgroundTask(BACKGROUND_TASK_IMPORT_LEGACY_CONTACTS);
                    return 1;
                } else {
                    return 0;
                }
            }
        }
        waitForAccess(mWriteAccessLatch);

        // Enforce stream items access check if applicable.
        enforceSocialStreamWritePermission(uri);

        if (mapsToProfileDb(uri)) {
            switchToProfileMode();
            return mProfileProvider.update(uri, values, selection, selectionArgs);
        } else {
            switchToContactMode();
            return super.update(uri, values, selection, selectionArgs);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
    	
        //Gionee:huangzy 20121019 remove for CR00715333 start
    	/*if (ContactsProvidersApplication.sIsGnContactsSupport) {
    		int match = sUriMatcher.match(uri);
            
            switch (match) {
            	case DATA_USAG_STAT_CLEAR:
            		return deleteFrequentContactedRecord(selection, selectionArgs);
            }
    	}*/
        //Gionee:huangzy 20121019 remove for CR00715333 end
    	
        waitForAccess(mWriteAccessLatch);

        // Enforce stream items access check if applicable.
        enforceSocialStreamWritePermission(uri);

        /*
         * Bug Fix by Mediatek Begin
         * 
         * Original Android code: 
         * if (mapsToProfileDb(uri)) {
         *     switchToProfileMode(); 
         *     return mProfileProvider.delete(uri, selection, selectionArgs); 
         * } else { 
         *     switchToContactMode(); 
         *     return super.delete(uri, selection, selectionArgs); 
         * }
         * 
         * Description:
         * added for low memory handle, CR ALPS00095386
         */
        int count = 0;
        try { 
            if (mapsToProfileDb(uri)) {
                switchToProfileMode();
                count = mProfileProvider.delete(uri, selection, selectionArgs);
            } else {
                switchToContactMode();
                count = super.delete(uri, selection, selectionArgs);
            }
        } catch (android.database.sqlite.SQLiteDiskIOException ex) {
            Log.w(TAG, "[delete]catch SQLiteDiskIOException!");
            return count;
        }         
        return count;
        /*
         * Bug Fix by Mediatek End
         */
    }

    /**
     * Replaces the current (thread-local) database to use for the operation with the given one.
     * @param db The database to use.
     */
    /* package */ void substituteDb(SQLiteDatabase db) {
        mActiveDb.set(db);
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        waitForAccess(mReadAccessLatch);
        if (method.equals(Authorization.AUTHORIZATION_METHOD)) {
            Uri uri = (Uri) extras.getParcelable(Authorization.KEY_URI_TO_AUTHORIZE);

            // Check permissions on the caller.  The URI can only be pre-authorized if the caller
            // already has the necessary permissions.
            enforceSocialStreamReadPermission(uri);
            if (mapsToProfileDb(uri)) {
                mProfileProvider.enforceReadPermission(uri);
            }

            // If there hasn't been a security violation yet, we're clear to pre-authorize the URI.
            Uri authUri = preAuthorizeUri(uri);
            Bundle response = new Bundle();
            response.putParcelable(Authorization.KEY_AUTHORIZED_URI, authUri);
            return response;
        }
        return null;
    }

    /**
     * Pre-authorizes the given URI, adding an expiring permission token to it and placing that
     * in our map of pre-authorized URIs.
     * @param uri The URI to pre-authorize.
     * @return A pre-authorized URI that will not require special permissions to use.
     */
    private Uri preAuthorizeUri(Uri uri) {
        String token = String.valueOf(mRandom.nextLong());
        Uri authUri = uri.buildUpon()
                .appendQueryParameter(PREAUTHORIZED_URI_TOKEN, token)
                .build();
        long expiration = SystemClock.elapsedRealtime() + mPreAuthorizedUriDuration;
        mPreAuthorizedUris.put(authUri, expiration);

        return authUri;
    }

    /**
     * Checks whether the given URI has an unexpired permission token that would grant access to
     * query the content.  If it does, the regular permission check should be skipped.
     * @param uri The URI being accessed.
     * @return Whether the URI is a pre-authorized URI that is still valid.
     */
    public boolean isValidPreAuthorizedUri(Uri uri) {
        // Only proceed if the URI has a permission token parameter.
        if (uri.getQueryParameter(PREAUTHORIZED_URI_TOKEN) != null) {
            // First expire any pre-authorization URIs that are no longer valid.
            long now = SystemClock.elapsedRealtime();
            Set<Uri> expiredUris = Sets.newHashSet();
            for (Uri preAuthUri : mPreAuthorizedUris.keySet()) {
                if (mPreAuthorizedUris.get(preAuthUri) < now) {
                    expiredUris.add(preAuthUri);
                }
            }
            for (Uri expiredUri : expiredUris) {
                mPreAuthorizedUris.remove(expiredUri);
            }

            // Now check to see if the pre-authorized URI map contains the URI.
            if (mPreAuthorizedUris.containsKey(uri)) {
                // Unexpired token - skip the permission check.
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean yield(ContactsTransaction transaction) {
        // If there's a profile transaction in progress, and we're yielding, we need to
        // end it.  Unlike the Contacts DB yield (which re-starts a transaction at its
        // conclusion), we can just go back into a state in which we have no active
        // profile transaction, and let it be re-created as needed.  We can't hold onto
        // the transaction without risking a deadlock.
        SQLiteDatabase profileDb = transaction.removeDbForTag(PROFILE_DB_TAG);
        if (profileDb != null) {
            profileDb.setTransactionSuccessful();
            profileDb.endTransaction();
        }

        // Now proceed with the Contacts DB yield.
        SQLiteDatabase contactsDb = transaction.getDbForTag(CONTACTS_DB_TAG);
        return contactsDb != null && contactsDb.yieldIfContendedSafely(SLEEP_AFTER_YIELD_DELAY);
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        waitForAccess(mWriteAccessLatch);
        return super.applyBatch(operations);
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        waitForAccess(mWriteAccessLatch);
        return super.bulkInsert(uri, values);
    }

    @Override
    public void onBegin() {
        if (VERBOSE_LOGGING) {
            Log.v(TAG, "onBeginTransaction");
        }
        if (inProfileMode()) {
            mProfileAggregator.clearPendingAggregations();
//            mProfileTransactionContext.clear();
            mProfileTransactionContext.clearExceptSearchIndexUpdates();
        } else {
            mContactAggregator.clearPendingAggregations();
//            mContactTransactionContext.clear();
            mContactTransactionContext.clearExceptSearchIndexUpdates();
        }
    }

    @Override
    public void onCommit() {
        if (VERBOSE_LOGGING) {
            Log.v(TAG, "beforeTransactionCommit");
        }
        final SQLiteDatabase db = mDbHelper.get().getWritableDatabase();
        mActiveDb.set(db);
        flushTransactionalChanges();
        mAggregator.get().aggregateInTransaction(mTransactionContext.get(), mActiveDb.get());
        if (mVisibleTouched) {
            mVisibleTouched = false;
            mDbHelper.get().updateAllVisible();
        }

        updateSearchIndexInTransaction();

        if (mProviderStatusUpdateNeeded) {
            updateProviderStatus();
            mProviderStatusUpdateNeeded = false;
        }
    }

    @Override
    public void onRollback() {
        // Not used.
    }

    private void updateSearchIndexInTransaction() {
        Set<Long> staleContacts = mTransactionContext.get().getStaleSearchIndexContactIds();
        Set<Long> staleRawContacts = mTransactionContext.get().getStaleSearchIndexRawContactIds();
        if (!staleContacts.isEmpty() || !staleRawContacts.isEmpty()) {
            mSearchIndexManager.updateIndexForRawContacts(staleContacts, staleRawContacts);
            mTransactionContext.get().clearSearchIndexUpdates();
        }
    }

    private void flushTransactionalChanges() {
        if (VERBOSE_LOGGING) {
            Log.v(TAG, "flushTransactionChanges");
        }

        final SQLiteDatabase db = mDbHelper.get().getWritableDatabase();
        for (long rawContactId : mTransactionContext.get().getInsertedRawContactIds()) {
            mDbHelper.get().updateRawContactDisplayName(mActiveDb.get(), rawContactId, true);
            mAggregator.get().onRawContactInsert(mTransactionContext.get(), mActiveDb.get(),
                    rawContactId);
        }

        Set<Long> dirtyRawContacts = mTransactionContext.get().getDirtyRawContactIds();
        if (!dirtyRawContacts.isEmpty()) {
            mSb.setLength(0);
            mSb.append(UPDATE_RAW_CONTACT_SET_DIRTY_SQL);
            appendIds(mSb, dirtyRawContacts);
            mSb.append(")");
            mActiveDb.get().execSQL(mSb.toString());
        }

        Set<Long> updatedRawContacts = mTransactionContext.get().getUpdatedRawContactIds();
        if (!updatedRawContacts.isEmpty()) {
            mSb.setLength(0);
            mSb.append(UPDATE_RAW_CONTACT_SET_VERSION_SQL);
            appendIds(mSb, updatedRawContacts);
            mSb.append(")");
            mActiveDb.get().execSQL(mSb.toString());
        }

        final Set<Long> changedRawContacts = mTransactionContext.get().getChangedRawContactIds();
        ContactsTableUtil.updateContactLastUpdateByRawContactId(db, changedRawContacts);
        
        //Gionee:huangzy 20121128 add for CR00736966 start
        if (ContactsProvidersApplication.sIsGnSyncSupport) {
        	Set<Long> gnChangedRawContacts = new HashSet<Long>();
        	gnChangedRawContacts.addAll(mTransactionContext.get().getDirtyRawContactIds());
        	gnChangedRawContacts.addAll(mTransactionContext.get().getUpdatedRawContactIds());
        	gnChangedRawContacts.addAll(mTransactionContext.get().getDeletedRawContactIds());

            if (!gnChangedRawContacts.isEmpty()) {
                mSb.setLength(0);
                mSb.append(GN_UPDATE_RAW_CONTACT_SET_GN_VERSION_SQL_START);
                mSb.append(ContactsDatabaseHelper.toNextGnVersion(getContext()));
                mSb.append(GN_UPDATE_RAW_CONTACT_SET_GN_VERSION_SQL_END);                
                appendIds(mSb, gnChangedRawContacts);
                mSb.append(")");
                mActiveDb.get().execSQL(mSb.toString());
            }
        }
        //Gionee:huangzy 20121128 add for CR00736966 end

        // Update sync states.
        for (Map.Entry<Long, Object> entry : mTransactionContext.get().getUpdatedSyncStates()) {
            long id = entry.getKey();
            if (mDbHelper.get().getSyncState().update(mActiveDb.get(), id, entry.getValue()) <= 0) {
                throw new IllegalStateException(
                        "unable to update sync state, does it still exist?");
            }
        }

//        mTransactionContext.get().clear();
        mTransactionContext.get().clearExceptSearchIndexUpdates();
    }

    /**
     * Appends comma separated ids.
     * @param ids Should not be empty
     */
    private void appendIds(StringBuilder sb, Set<Long> ids) {
        for (long id : ids) {
            sb.append(id).append(',');
        }

        sb.setLength(sb.length() - 1); // Yank the last comma
    }

    @Override
    protected void notifyChange() {
        notifyChange(mSyncToNetwork);
        mSyncToNetwork = false;
    }

    protected void notifyChange(boolean syncToNetwork) {
        getContext().getContentResolver().notifyChange(GnContactsContract.AUTHORITY_URI, null,
                syncToNetwork);
    }

    protected void setProviderStatus(int status) {
        if (mProviderStatus != status) {
            mProviderStatus = status;
            getContext().getContentResolver().notifyChange(ProviderStatus.CONTENT_URI, null, false);
        }
    }

    public DataRowHandler getDataRowHandler(final String mimeType) {
        if (inProfileMode()) {
            return getDataRowHandlerForProfile(mimeType);
        }
        DataRowHandler handler = mDataRowHandlers.get(mimeType);
        if (handler == null) {
            handler = new DataRowHandlerForCustomMimetype(
                    getContext(), mContactsHelper, mContactAggregator, mimeType);
            mDataRowHandlers.put(mimeType, handler);
        }
        return handler;
    }

    public DataRowHandler getDataRowHandlerForProfile(final String mimeType) {
        DataRowHandler handler = mProfileDataRowHandlers.get(mimeType);
        if (handler == null) {
            handler = new DataRowHandlerForCustomMimetype(
                    getContext(), mProfileHelper, mProfileAggregator, mimeType);
            mProfileDataRowHandlers.put(mimeType, handler);
        }
        return handler;
    }

    @Override
    protected Uri insertInTransaction(Uri uri, ContentValues values) {
        if (VERBOSE_LOGGING) {
            Log.v(TAG, "insertInTransaction: " + uri + " " + values);
        }

        // Default active DB to the contacts DB if none has been set.
        if (mActiveDb.get() == null) {
            mActiveDb.set(mContactsHelper.getWritableDatabase());
        }
        
        // aurora wangth 20150109 add for begin
        if (values != null && values.containsKey(RawContacts.AGGREGATION_MODE)) {
            values.remove(RawContacts.AGGREGATION_MODE);
            values.put(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_DISABLED);
        }
        // aurora wangth 20150109 add for end

        final boolean callerIsSyncAdapter =
                readBooleanQueryParameter(uri, GnContactsContract.CALLER_IS_SYNCADAPTER, false);

        final int match = sUriMatcher.match(uri);
        Log.v(TAG, "insertInTransaction: " + match);
        long id = 0;
        String  path = "";

        switch (match) {
            case SYNCSTATE:
            case PROFILE_SYNCSTATE:
                id = mDbHelper.get().getSyncState().insert(mActiveDb.get(), values);
                break;

            case CONTACTS: {
                insertContact(values);
                break;
            }

            case PROFILE: {
                throw new UnsupportedOperationException(
                        "The profile contact is created automatically");
            }

            case RAW_CONTACTS:
            case PROFILE_RAW_CONTACTS: {
                id = insertRawContact(uri, values, callerIsSyncAdapter);
                mSyncToNetwork |= !callerIsSyncAdapter;
    			if (uri.getQueryParameter("sync")== null) {
    				sendSyncBroad();   
    			}
                break;
            }

            case RAW_CONTACTS_DATA:
            case PROFILE_RAW_CONTACTS_ID_DATA: {
                int segment = match == RAW_CONTACTS_DATA ? 1 : 2;
                values.put(Data.RAW_CONTACT_ID, uri.getPathSegments().get(segment));
                id = insertData(values, callerIsSyncAdapter);
                mSyncToNetwork |= !callerIsSyncAdapter;
                break;
            }

            case RAW_CONTACTS_ID_STREAM_ITEMS: {
                values.put(StreamItems.RAW_CONTACT_ID, uri.getPathSegments().get(1));
                id = insertStreamItem(uri, values);
                mSyncToNetwork |= !callerIsSyncAdapter;
                break;
            }

            case DATA:
            case PROFILE_DATA: {
                id = insertData(values, callerIsSyncAdapter);
                mSyncToNetwork |= !callerIsSyncAdapter;
                break;
            }

            case GROUPS: {
                id = insertGroup(uri, values, callerIsSyncAdapter);
                mSyncToNetwork |= !callerIsSyncAdapter;
                break;
            }

            case SETTINGS: {
                id = insertSettings(uri, values);
                mSyncToNetwork |= !callerIsSyncAdapter;
                break;
            }

            case STATUS_UPDATES:
            case PROFILE_STATUS_UPDATES: {
                id = insertStatusUpdate(values);
                break;
            }

            case STREAM_ITEMS: {
                id = insertStreamItem(uri, values);
                mSyncToNetwork |= !callerIsSyncAdapter;
                break;
            }

            case STREAM_ITEMS_PHOTOS: {
            	System.out.println("STREAM_ITEMS_PHOTOS:");
                id = insertStreamItemPhoto(uri, values);
                mSyncToNetwork |= !callerIsSyncAdapter;
                break;
            }

            case STREAM_ITEMS_ID_PHOTOS: {
            	System.out.println("STREAM_ITEMS_ID_PHOTOS");
                values.put(StreamItemPhotos.STREAM_ITEM_ID, uri.getPathSegments().get(1));
                id = insertStreamItemPhoto(uri, values);
                mSyncToNetwork |= !callerIsSyncAdapter;
                break;
            }
 
		// reject begin
		case BLACKS:
			id = mActiveDb.get().insert("black", "number", values); // 返回的是记录的行号，主键为int，实际上就是主键值
			
			String name = values.getAsString("black_name");
			String number = values.getAsString("number");
			int rejected = values.getAsInteger("reject");
			Log.d(TAG, "number = " + number + "  rejected = " + rejected);
			
			if (id > 0) {
				updateRejectMms(1, 1, name, number, rejected);
			}
			
			// update data
			updateDataForBlack(number, 1, true);
			// update calls
			if (rejected == 1 || rejected == 3) {
				updateCallsForBlack(number, name, true);
			}
			
			return ContentUris.withAppendedId(uri, id);
		case BLACK:
			id = mActiveDb.get().insert("black", null, values);
			path = uri.toString();
			return Uri.parse(path.substring(0, path.lastIndexOf("/")) + id); // 替换掉id
		case MARKS:
			id = mActiveDb.get().insert("mark", null, values); // 返回的是记录的行号，主键为int，实际上就是主键值
			return ContentUris.withAppendedId(uri, id);
		case MARK:
			id = mActiveDb.get().insert("mark", null, values);
			
			
			path = uri.toString();
			return Uri.parse(path.substring(0, path.lastIndexOf("/")) + id); // 替换掉id
		// reject end
	    //contacts sync begin
		case SYNC_UP:
			System.out.println("case SYNC_UP insert:");
           
			return null;
		case SYNC_UP_RESULT:
			System.out.println("case SYNC_UP_RESULT insert:");
			if (values.containsKey("multi")){

				int count = 0;
				String multi = values.getAsString("multi");
				System.out.println("SYNC_UP_RESULT multi="+multi);
				JSONObject jsonObject = null;
				try {
					jsonObject = new JSONObject(multi);
				} catch (JSONException e) {

				}
				JSONArray jsonArray =null;
				try {
					jsonArray=(JSONArray)jsonObject.get("sycndata");
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				SQLiteDatabase db=mActiveDb.get();
				
				if (jsonArray != null && jsonArray.length() > 0) {
					List<String> syncs=new ArrayList<String>();
					List<String> ids=new ArrayList<String>();
					List<JSONObject> bodys=new ArrayList<JSONObject>();
					for (int i = 0; i < jsonArray.length(); i++) {
						String sync4=null;
						String raw_contact_id=null;
						JSONObject bodyJsonObject = null;
						try {
							bodyJsonObject = jsonArray.getJSONObject(i);
						} catch (JSONException e) {

						}
						if (bodyJsonObject != null) {
							sync4=getValue(bodyJsonObject, "syncid");
							syncs.add(sync4);
							raw_contact_id=getValue(bodyJsonObject, "id");
							ids.add(raw_contact_id);
						}
						JSONObject bodyObject=null;
						String str=getValue(bodyJsonObject, "body");
						
						try {
							bodyObject=new JSONObject(str);
							bodys.add(bodyObject);
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
					}
					List<String> list=null;
					
					db.beginTransaction();
					try {
						// 批量处理操作
						for (int i = 0; i < jsonArray.length(); i++) {
							int del=0;
							try {
								del=bodys.get(i).getInt("isdelete");
							} catch (Exception e) {
								return Uri.withAppendedPath(uri, "false");
							}
							if(del==1){
								db.delete(Tables.RAW_CONTACTS,  "_id=?",new String[]{ids.get(i)});
							}else{
								list=getDataSyncIds(bodys.get(i));
								if(list!=null){
									syncUpResult(syncs.get(i), ids.get(i),db,list);
								}else{
									return Uri.withAppendedPath(uri, "false");
								}
							}
						}
						// 设置事务处理成功，不设置会自动回滚不提交。
						db.setTransactionSuccessful();
					}catch(Exception e){
						return Uri.withAppendedPath(uri, "false");
			        }finally{
			            db.endTransaction(); //处理完成
			        }
				}
//				if (jsonArray != null && jsonArray.length() > 0) {
//					for (int i = 0; i < jsonArray.length(); i++) {
//						String sync4=null;
//						String raw_contact_id=null;
//						JSONObject bodyJsonObject = null;
//						try {
//							bodyJsonObject = jsonArray.getJSONObject(i);
//						} catch (JSONException e) {
//
//						}
//						if (bodyJsonObject != null) {
//							sync4=getValue(bodyJsonObject, "syncid");
//							raw_contact_id=getValue(bodyJsonObject, "id");
//							
//						}
//						JSONObject bodyObject=null;
//						String str=getValue(bodyJsonObject, "body");
//						
//						try {
//							bodyObject=new JSONObject(str);
//						} catch (Exception e) {
//							// TODO: handle exception
//							e.printStackTrace();
//						}
//						
//						
//						
//						if (syncUpResult(sync4, raw_contact_id, bodyObject,mActiveDb.get())) {
//							count++;
//						} else {
//							break;
//						}
//
//					}
//				}
//				if(count == jsonArray.length()){
//					return Uri.withAppendedPath(uri, "true");
//				}
			
			}else{
				return Uri.withAppendedPath(uri, "false");
			}
			return Uri.withAppendedPath(uri, "true");
		case SYNC_DOWN:
			System.out.println("case SYNC_DOWN insert:");
			Long accountId = mDbHelper.get().getAccountIdOrNull(mDbHelper.get().getDefaultAccount());
			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
			ContentProviderOperation.Builder builder;
			if (values.containsKey("multi")) {
				String syncid=null;
				String multi = values.getAsString("multi");
				System.out.println("SYNC_DOWN multi="+multi);
				JSONArray jsonArray = null;
				try {
					jsonArray = new JSONArray(multi);
				} catch (JSONException e) {

				}
				if (jsonArray != null && jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						syncid=null;
						JSONObject accessoryJsonObject = null;
						String bodyJsonObject = null;
						JSONObject jsonObject = null;
						try {
							jsonObject = jsonArray.getJSONObject(i);
						} catch (JSONException e) {

						}
						if (jsonObject != null) {
							JSONObject accessoryJson = null;
							JSONArray accessoryJsonArray = null;
							try {
								syncid=getValue(jsonObject, "syncid");
								bodyJsonObject =getValue(jsonObject, "body");
								accessoryJson = (JSONObject) jsonObject
										.get("accOjb");
								accessoryJsonArray = (JSONArray) accessoryJson
										.get("accessory");
							} catch (Exception e) {
								// TODO: handle exception
								e.printStackTrace();
							}

							if (accessoryJsonArray != null
									&& accessoryJsonArray.length() > 0) {
								for (int j = 0; j < accessoryJsonArray.length(); j++) {
									try {
										accessoryJsonObject = accessoryJsonArray
												.getJSONObject(j);
									} catch (JSONException e) {

									}

								}
							}
						}
						
						
						String data1, data2, data3, data4, data5, data6, data7, data8, data9, data10, data11, data12, data13,data14=null, mimetype, data_sync4=null, updatePhoto = null;
						byte[] data15=null;
						JSONObject item = null;
						JSONObject object = null;
						try {
							object = new JSONObject(bodyJsonObject);
						} catch (Exception e) {
							e.printStackTrace();
						}
						int deleted=0;
						try {
							deleted=object.getInt("isdelete");
						} catch (Exception e) {
							e.printStackTrace();
						}
						String starred = getValue(object, "starred");
						String sync3=getValue(object, "localFlag");
						JSONArray dataJsonArray = null;
						try {
							dataJsonArray = (JSONArray) object.get("data");
						} catch (Exception e) {
							e.printStackTrace();
						}	
						
						Uri uris = Uri.parse("content://com.android.contacts/raw_contacts");
						Cursor cursor=getContext().getContentResolver().query(uris, new String[]{"_id"}, "sync4=?", new String[]{syncid}, null);
						
						
						if(cursor.moveToNext()){
							if(!syncDownOne(bodyJsonObject, accessoryJsonObject,syncid)){
								return Uri.withAppendedPath(uri, "false");
							}
						}else{
							if(deleted==0){
								int backRef = ops.size();
								builder = ContentProviderOperation
										.newInsert(Uri.parse("content://com.android.contacts/raw_contacts").buildUpon().appendQueryParameter("sync", "true").build())
										.withValue("starred",starred).withValue("sync4", syncid).withValue("sync3", sync3);
								ops.add(builder.build());
								
								for (int m = 0; m < dataJsonArray.length(); m++) {
									try {
										item = dataJsonArray.getJSONObject(m);
									} catch (Exception e) {
										e.printStackTrace();
									}
									mimetype = getValue(item, "mimetype");
									data_sync4 = getValue(item, "syncid");
									data14=null;
									data15=null;
									if (mimetype.equals("vnd.android.cursor.item/photo")) {
										if (accessoryJsonObject != null) {
											Object[] ob=new Object[2];
											String newPath =getValue(accessoryJsonObject, "new_path");
											if(getData14(newPath,ob)){
												data14=(Long)ob[1]+"";
												data15=(byte[])ob[0];
											}
										}
									}
									data1 = getValue(item, "data1");
									if (mimetype.equals("vnd.android.cursor.item/group_membership")) {
										Cursor cursors = getContext().getContentResolver().query(
												Groups.CONTENT_URI, new String[] { "_id" },
												"title=? and deleted=?", new String[] { data1, "0" },
												null);
										if (cursors.moveToNext()) {
											data1 = cursors.getString(0);
										} else {
											ContentValues val = new ContentValues();
											val.put("title", data1);
											Uri ur = getContext().getContentResolver().insert(
													Groups.CONTENT_URI, val);
											if (ContentUris.parseId(ur) < 1) {
												return Uri.withAppendedPath(uri, "false");
											}
											data1 = ur.getPathSegments().get(1);
										}
									}
									data2 = getValue(item, "data2");
									data3 = getValue(item, "data3");
									data4 = getValue(item, "data4");
									data5 = getValue(item, "data5");
									data6 = getValue(item, "data6");
									data7 = getValue(item, "data7");
									data8 = getValue(item, "data8");
									data9 = getValue(item, "data9");
									data10 = getValue(item, "data10");
									data11 = getValue(item, "data11");
									data12 = getValue(item, "data12");
									data13 = getValue(item, "data13");
									builder = ContentProviderOperation
											.newInsert(Data.CONTENT_URI)
											.withValue(Data.MIMETYPE,mimetype).withValue("data_sync4", data_sync4)
											.withValue("data1", data1).withValue("data2", data2)
											.withValue("data3", data3).withValue("data4", data4)
											.withValue("data5", data5).withValue("data6", data6)
											.withValue("data7", data7).withValue("data8", data8)
											.withValue("data9", data9).withValue("data10", data10)
											.withValue("data11", data11).withValue("data12", data12)
											.withValue("data13", data13).withValue("data14", data14).withValue("data15", data15)
											.withValueBackReference(Data.RAW_CONTACT_ID, backRef);
									ops.add(builder.build());
								}
								
								
								if (ops.size() > 100) {
									ops.add(ContentProviderOperation
											 .newUpdate(Uri.parse("content://com.android.contacts/raw_contacts"))
											 .withSelection("account_id=?", new String[]{accountId+""})
									         .withValue("dirty",0).build());
									try {
										getContext().getContentResolver().applyBatch(
												GnContactsContract.AUTHORITY, ops);
									} catch (Exception e) {
										e.printStackTrace();
										return Uri.withAppendedPath(uri, "false");
									}
									ops.clear();
								}
							}
						}

					}
				}
				
			}
			if (ops.size() > 0) {
				ops.add(ContentProviderOperation
						 .newUpdate(Uri.parse("content://com.android.contacts/raw_contacts"))
						 .withSelection("account_id=?", new String[]{accountId+""})
				         .withValue("dirty",0).build());
				try {
					getContext().getContentResolver().applyBatch(
							GnContactsContract.AUTHORITY, ops);
				} catch (Exception e) {
					e.printStackTrace();
					return Uri.withAppendedPath(uri, "false");
				}
				ops.clear();
			}
			return Uri.withAppendedPath(uri, "true");
			
		case URI_INIT_ACCOUNT_MULTI:
		{
			Builder builders = uri.buildUpon();
			if (values.containsKey("multi")) {
				int count = 0;
			    String multi = values.getAsString("multi");
			    System.out.println("URI_INIT_ACCOUNT_MULTI="+multi);
			    JSONArray jsonArray = null;
			    try{
				    jsonArray = new JSONArray(multi);
			    } catch (JSONException e){
			    	
			    }
			    if(jsonArray != null && jsonArray.length() > 0){
			    	
			    	List<String> ids=new ArrayList<String>();
					List<String> flags=new ArrayList<String>();
					List<Long> dates=new ArrayList<Long>();
					for (int i = 0; i < jsonArray.length(); i++) {
						String bodyJsonObject = null;
						JSONObject jsonObject = null;
						JSONObject body=null;
						try {
							jsonObject = jsonArray.getJSONObject(i);
						} catch (JSONException e) {

						}
						if (jsonObject != null) {
							try {
								bodyJsonObject =getValue(jsonObject, "body");
							} catch (Exception e) {
								// TODO: handle exception
								e.printStackTrace();
							}
							try {
								body=new JSONObject(bodyJsonObject);
							} catch (Exception e) {
								// TODO: handle exception
								e.printStackTrace();
							}
							
							
							if(body != null){
								String syncid =null;
								try {
									syncid=body.getInt("syncid")+"";   
									ids.add(syncid);
								} catch (Exception e) {
									// TODO: handle exception
									e.printStackTrace();
								}
								               
								String localFlag =getValue(body, "localFlag");
								flags.add(localFlag);
								long date =0;
								try {
									date=body.getLong("date");
									dates.add(date);
								} catch (Exception e) {
									// TODO: handle exception
									e.printStackTrace();
								}
								
								
							}
						}
					}
					SQLiteDatabase db=mActiveDb.get();
					db.beginTransaction();
					try {
						for (int i = 0; i < jsonArray.length(); i++) {							
							if(!TextUtils.isEmpty(ids.get(i)) && !TextUtils.isEmpty(flags.get(i))){
							    if(!syncInitOne(mActiveDb.get(), ids.get(i), flags.get(i),dates.get(i))){
							    	JSONObject result = new JSONObject();
							    	JSONObject bodyResult = new JSONObject();
							    	try {
							    		bodyResult.put("syncid", ids.get(i));
								    	result.put("body", bodyResult);
									} catch (Exception e) {
										e.printStackTrace();
									}
									builders.appendQueryParameter("results",
											result.toString());
							    }
							}else{
								return builders.build();
							}
						}
						db.setTransactionSuccessful();
					}catch(Exception e){
						return builders.build();
			        }finally{
			            db.endTransaction(); 
			        }
			    }
			}
			return builders.build();
		}	
			
			
			
		case SYNC_ACCESSORY:
			System.out.println("case SYNC_ACCESSORY insert:");
			return null;
		case SYNCS:
			System.out.println("case SYNCS insert:");
			return null;
		case SYNC:
			System.out.println("case SYNC insert:");
			return null;
			
		//contacts sync end

            default:
                mSyncToNetwork = true;
                return mLegacyApiSupport.insert(uri, values);
        }

        if (id < 0) {
            return null;
        }

        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * If account is non-null then store it in the values. If the account is
     * already specified in the values then it must be consistent with the
     * account, if it is non-null.
     *
     * @param uri Current {@link Uri} being operated on.
     * @param values {@link ContentValues} to read and possibly update.
     * @throws IllegalArgumentException when only one of
     *             {@link RawContacts#ACCOUNT_NAME} or
     *             {@link RawContacts#ACCOUNT_TYPE} is specified, leaving the
     *             other undefined.
     * @throws IllegalArgumentException when {@link RawContacts#ACCOUNT_NAME}
     *             and {@link RawContacts#ACCOUNT_TYPE} are inconsistent between
     *             the given {@link Uri} and {@link ContentValues}.
     */
    private Account resolveAccount(Uri uri, ContentValues values) throws IllegalArgumentException {
        String accountName = getQueryParameter(uri, RawContacts.ACCOUNT_NAME);
        String accountType = getQueryParameter(uri, RawContacts.ACCOUNT_TYPE);
        final boolean partialUri = TextUtils.isEmpty(accountName) ^ TextUtils.isEmpty(accountType);

        String valueAccountName = values.getAsString(RawContacts.ACCOUNT_NAME);
        String valueAccountType = values.getAsString(RawContacts.ACCOUNT_TYPE);
        final boolean partialValues = TextUtils.isEmpty(valueAccountName)
                ^ TextUtils.isEmpty(valueAccountType);

        if (partialUri || partialValues) {
            // Throw when either account is incomplete
            throw new IllegalArgumentException(mDbHelper.get().exceptionMessage(
                    "Must specify both or neither of ACCOUNT_NAME and ACCOUNT_TYPE", uri));
        }

        // Accounts are valid by only checking one parameter, since we've
        // already ruled out partial accounts.
        final boolean validUri = !TextUtils.isEmpty(accountName);
        final boolean validValues = !TextUtils.isEmpty(valueAccountName);

        if (validValues && validUri) {
            // Check that accounts match when both present
            final boolean accountMatch = TextUtils.equals(accountName, valueAccountName)
                    && TextUtils.equals(accountType, valueAccountType);
            if (!accountMatch) {
                throw new IllegalArgumentException(mDbHelper.get().exceptionMessage(
                        "When both specified, ACCOUNT_NAME and ACCOUNT_TYPE must match", uri));
            }
        } else if (validUri) {
            // Fill values from Uri when not present
            values.put(RawContacts.ACCOUNT_NAME, accountName);
            values.put(RawContacts.ACCOUNT_TYPE, accountType);
        } else if (validValues) {
            accountName = valueAccountName;
            accountType = valueAccountType;
        } else {
            return null;
        }

        // Use cached Account object when matches, otherwise create
        if (mAccount == null
                || !mAccount.name.equals(accountName)
                || !mAccount.type.equals(accountType)) {
            mAccount = new Account(accountName, accountType);
        }

        return mAccount;
    }

    /**
     * Resolves the account and builds an {@link AccountWithDataSet} based on the data set specified
     * in the URI or values (if any).
     * @param uri Current {@link Uri} being operated on.
     * @param values {@link ContentValues} to read and possibly update.
     */
    private AccountWithDataSet resolveAccountWithDataSet(Uri uri, ContentValues values) {
        final Account account = resolveAccount(uri, values);
        AccountWithDataSet accountWithDataSet = null;
        if (account != null) {
            String dataSet = getQueryParameter(uri, RawContacts.DATA_SET);
            if (dataSet == null) {
                dataSet = values.getAsString(RawContacts.DATA_SET);
            } else {
                values.put(RawContacts.DATA_SET, dataSet);
            }
            accountWithDataSet = new AccountWithDataSet(account.name, account.type, dataSet);
        }
        // aurora <wangth> <2014-1-7> add for aurora begin
        else {
            accountWithDataSet = new AccountWithDataSet("Phone", ACCOUNT_TYPE_LOCAL_PHONE, null);
        }
        // aurora <wangth> <2014-1-7> add for aurora end
        
        return accountWithDataSet;
    }

    /**
     * Same as {@link #resolveAccountWithDataSet}, but returns the account id for the
     *     {@link AccountWithDataSet}.  Used for insert.
     *
     * May update the account cache; must be used only in a transaction.
     */
    private long resolveAccountIdInTransaction(Uri uri, ContentValues values) {
        return mDbHelper.get().getOrCreateAccountIdInTransaction(
                resolveAccountWithDataSet(uri, mValues));
    }

    /**
     * Inserts an item in the contacts table
     *
     * @param values the values for the new row
     * @return the row ID of the newly created row
     */
    private long insertContact(ContentValues values) {
        throw new UnsupportedOperationException("Aggregate contacts are created automatically");
    }

    /**
     * Inserts an item in the raw contacts table
     *
     * @param uri the values for the new row
     * @param values the account this contact should be associated with. may be null.
     * @param callerIsSyncAdapter
     * @return the row ID of the newly created row
     */
    private long insertRawContact(Uri uri, ContentValues values, boolean callerIsSyncAdapter) {
        mValues.clear();
        mValues.putAll(values);
        mValues.putNull(RawContacts.CONTACT_ID);
        
        AccountWithDataSet accountWithDataSet = resolveAccountWithDataSet(uri, mValues);
        
        // aurora <wangth> <2014-1-7> add for aurora begin
        if (!mValues.containsKey(RawContacts.ACCOUNT_TYPE) && null != accountWithDataSet) {
            mValues.put(RawContacts.ACCOUNT_TYPE, accountWithDataSet.getAccountType());
            mValues.put(RawContacts.ACCOUNT_NAME, accountWithDataSet.getAccountName());
        } else if (mValues.containsKey(RawContacts.ACCOUNT_TYPE) 
                && (mValues.getAsString(RawContacts.ACCOUNT_TYPE) == null || TextUtils.isEmpty(mValues.getAsString(RawContacts.ACCOUNT_TYPE)))) {
            mValues.put(RawContacts.ACCOUNT_TYPE, accountWithDataSet.getAccountType());
            mValues.put(RawContacts.ACCOUNT_NAME, accountWithDataSet.getAccountName());
        }
        // aurora <wangth> <2014-1-7> add for aurora end
        final long accountId = resolveAccountIdInTransaction(uri, mValues);
        mValues.remove(RawContacts.ACCOUNT_NAME);
        mValues.remove(RawContacts.ACCOUNT_TYPE);
        mValues.remove(RawContacts.DATA_SET);
        mValues.put(RawContactsColumns.ACCOUNT_ID, accountId);
        if(mDbHelper.get().isDefaultAccount(accountWithDataSet)){
        	if (uri.getQueryParameter("sync")== null) {
        		mValues.put("sync3", System.currentTimeMillis());   
			}
        	
        }

        if (values.containsKey(RawContacts.DELETED)
                && values.getAsInteger(RawContacts.DELETED) != 0) {
            mValues.put(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_DISABLED);
        }

        long rawContactId = mActiveDb.get().insert(Tables.RAW_CONTACTS,
                RawContacts.CONTACT_ID, mValues);
        
        // aurora <wangth> <2015-1-9> modify for aurora begin
        int aggregationMode;
        if (ContactsProvidersApplication.sIsCtsSupport) {
            aggregationMode = getIntValue(values, RawContacts.AGGREGATION_MODE,
                RawContacts.AGGREGATION_MODE_DEFAULT);
        } else {
            aggregationMode = RawContacts.AGGREGATION_MODE_DISABLED;
        }
        // aurora <wangth> <2015-1-9> modify for aurora end
        mAggregator.get().markNewForAggregation(rawContactId, aggregationMode);

        // Trigger creation of a Contact based on this RawContact at the end of transaction
//        mTransactionContext.get().rawContactInserted(rawContactId, accountWithDataSet);
        mTransactionContext.get().rawContactInserted(rawContactId, accountId);

        if (!callerIsSyncAdapter) {
            addAutoAddMembership(rawContactId);
            final Long starred = values.getAsLong(RawContacts.STARRED);
            if (starred != null && starred != 0) {
                updateFavoritesMembership(rawContactId, starred != 0);
            }
        }

        mProviderStatusUpdateNeeded = true;
        return rawContactId;
    }

    private void addAutoAddMembership(long rawContactId) {
        final Long groupId = findGroupByRawContactId(SELECTION_AUTO_ADD_GROUPS_BY_RAW_CONTACT_ID,
                rawContactId);
        if (groupId != null) {
            insertDataGroupMembership(rawContactId, groupId);
        }
    }

    private Long findGroupByRawContactId(String selection, long rawContactId) {
        Cursor c = mActiveDb.get().query(Tables.GROUPS + "," + Tables.RAW_CONTACTS,
                PROJECTION_GROUP_ID, selection,
                new String[]{Long.toString(rawContactId)},
                null /* groupBy */, null /* having */, null /* orderBy */);
        try {
            while (c.moveToNext()) {
                return c.getLong(0);
            }
            return null;
        } finally {
            c.close();
        }
    }

    private void updateFavoritesMembership(long rawContactId, boolean isStarred) {
        final Long groupId = findGroupByRawContactId(SELECTION_FAVORITES_GROUPS_BY_RAW_CONTACT_ID,
                rawContactId);
        if (groupId != null) {
            if (isStarred) {
                insertDataGroupMembership(rawContactId, groupId);
            } else {
                deleteDataGroupMembership(rawContactId, groupId);
            }
        }
    }

    private void insertDataGroupMembership(long rawContactId, long groupId) {
        ContentValues groupMembershipValues = new ContentValues();
        groupMembershipValues.put(GroupMembership.GROUP_ROW_ID, groupId);
        groupMembershipValues.put(GroupMembership.RAW_CONTACT_ID, rawContactId);
        groupMembershipValues.put(DataColumns.MIMETYPE_ID,
                mDbHelper.get().getMimeTypeId(GroupMembership.CONTENT_ITEM_TYPE));
        mActiveDb.get().insert(Tables.DATA, null, groupMembershipValues);
    }

    private void deleteDataGroupMembership(long rawContactId, long groupId) {
        final String[] selectionArgs = {
                Long.toString(mDbHelper.get().getMimeTypeId(GroupMembership.CONTENT_ITEM_TYPE)),
                Long.toString(groupId),
                Long.toString(rawContactId)};
        mActiveDb.get().delete(Tables.DATA, SELECTION_GROUPMEMBERSHIP_DATA, selectionArgs);
    }

    /**
     * Inserts an item in the data table
     *
     * @param values the values for the new row
     * @return the row ID of the newly created row
     */
    private long insertData(ContentValues values, boolean callerIsSyncAdapter) {
        long id = 0;
        mValues.clear();
        mValues.putAll(values);

        long rawContactId = mValues.getAsLong(Data.RAW_CONTACT_ID);
        
        //Gionee:huangzy 20121011 add for CR00710695 start
        if (!ContactsProvidersApplication.sIsCtsSupport && ContactsProvidersApplication.sIsGnDialerSearchSupport) {
        	if (CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(mValues.get(Data.MIMETYPE))) {
        		String number = mValues.getAsString(Data.DATA1);
        		if (null != number) {
        		    number = number.replace(" ", "");
        		    number = number.replace("-", "");
        		    mValues.put(Data.DATA1, number);
        		}
        	}
        }
        //Gionee:huangzy 20121011 add for CR00710695 end

        // Replace package with internal mapping
        final String packageName = mValues.getAsString(Data.RES_PACKAGE);
        if (packageName != null) {
            mValues.put(DataColumns.PACKAGE_ID, mDbHelper.get().getPackageId(packageName));
        }
        mValues.remove(Data.RES_PACKAGE);

        // Replace mimetype with internal mapping
        final String mimeType = mValues.getAsString(Data.MIMETYPE);
        if (TextUtils.isEmpty(mimeType)) {
            throw new IllegalArgumentException(Data.MIMETYPE + " is required");
        }

        mValues.put(DataColumns.MIMETYPE_ID, mDbHelper.get().getMimeTypeId(mimeType));
        mValues.remove(Data.MIMETYPE);

        DataRowHandler rowHandler = getDataRowHandler(mimeType);
        id = rowHandler.insert(mActiveDb.get(), mTransactionContext.get(), rawContactId, mValues);
//        if (!callerIsSyncAdapter) {
//            mTransactionContext.get().markRawContactDirty(rawContactId);
//        }
        mTransactionContext.get().markRawContactDirtyAndChanged(rawContactId, callerIsSyncAdapter);
        mTransactionContext.get().rawContactUpdated(rawContactId);
        return id;
    }

    /**
     * Inserts an item in the stream_items table.  The account is checked against the
     * account in the raw contact for which the stream item is being inserted.  If the
     * new stream item results in more stream items under this raw contact than the limit,
     * the oldest one will be deleted (note that if the stream item inserted was the
     * oldest, it will be immediately deleted, and this will return 0).
     *
     * @param uri the insertion URI
     * @param values the values for the new row
     * @return the stream item _ID of the newly created row, or 0 if it was not created
     */
    private long insertStreamItem(Uri uri, ContentValues values) {
        long id = 0;
        mValues.clear();
        mValues.putAll(values);

        Long rawContactId = mValues.getAsLong(Data.RAW_CONTACT_ID);
        if (rawContactId == null) {
            throw new IllegalArgumentException(Data.RAW_CONTACT_ID + " is required");
        }

        // Don't attempt to insert accounts params - they don't exist in the stream items table.
        mValues.remove(RawContacts.ACCOUNT_NAME);
        mValues.remove(RawContacts.ACCOUNT_TYPE);

        // Insert the new stream item.
        final SQLiteDatabase db = mDbHelper.get().getWritableDatabase();
        id = db.insert(Tables.STREAM_ITEMS, null, mValues);
        if (id == -1) {
            // Insertion failed.
            return 0;
        }

        // Check to see if we're over the limit for stream items under this raw contact.
        // It's possible that the inserted stream item is older than the the existing
        // ones, in which case it may be deleted immediately (resetting the ID to 0).
        id = cleanUpOldStreamItems(rawContactId, id);

        return id;
    }

    /**
     * Inserts an item in the stream_item_photos table.  The account is checked against
     * the account in the raw contact that owns the stream item being modified.
     *
     * @param uri the insertion URI
     * @param values the values for the new row
     * @return the stream item photo _ID of the newly created row, or 0 if there was an issue
     *     with processing the photo or creating the row
     */
    private long insertStreamItemPhoto(Uri uri, ContentValues values) {
        long id = 0;
        mValues.clear();
        mValues.putAll(values);

        Long streamItemId = mValues.getAsLong(StreamItemPhotos.STREAM_ITEM_ID);
        if (streamItemId != null && streamItemId != 0) {
            long rawContactId = lookupRawContactIdForStreamId(streamItemId);

            // Don't attempt to insert accounts params - they don't exist in the stream item
            // photos table.
            mValues.remove(RawContacts.ACCOUNT_NAME);
            mValues.remove(RawContacts.ACCOUNT_TYPE);

            // Process the photo and store it.
            if (processStreamItemPhoto(mValues, false)) {
                // Insert the stream item photo.
                final SQLiteDatabase db = mDbHelper.get().getWritableDatabase();
                id = db.insert(Tables.STREAM_ITEM_PHOTOS, null, mValues);
            }
        }
        return id;
    }

    /**
     * Processes the photo contained in the {@link ContactsContract.StreamItemPhotos#PHOTO}
     * field of the given values, attempting to store it in the photo store.  If successful,
     * the resulting photo file ID will be added to the values for insert/update in the table.
     * <p>
     * If updating, it is valid for the picture to be empty or unspecified (the function will
     * still return true).  If inserting, a valid picture must be specified.
     * @param values The content values provided by the caller.
     * @param forUpdate Whether this photo is being processed for update (vs. insert).
     * @return Whether the insert or update should proceed.
     */
    private boolean processStreamItemPhoto(ContentValues values, boolean forUpdate) {
    	System.out.println("processStreamItemPhoto");
        if (!values.containsKey(StreamItemPhotos.PHOTO)) {
            return forUpdate;
        }
        byte[] photoBytes = values.getAsByteArray(StreamItemPhotos.PHOTO);
        if (photoBytes == null) {
            return forUpdate;
        }

        // Process the photo and store it.
        try {
            long photoFileId = mPhotoStore.get().insert(new PhotoProcessor(photoBytes,
                    mMaxDisplayPhotoDim, mMaxThumbnailPhotoDim, true), true);
            if (photoFileId != 0) {
                values.put(StreamItemPhotos.PHOTO_FILE_ID, photoFileId);
                values.remove(StreamItemPhotos.PHOTO);
                return true;
            } else {
                // Couldn't store the photo, return 0.
                Log.e(TAG, "Could not process stream item photo for insert");
                return false;
            }
        } catch (IOException ioe) {
            Log.e(TAG, "Could not process stream item photo for insert", ioe);
            return false;
        }
    }

    /**
     * Looks up the raw contact ID that owns the specified stream item.
     * @param streamItemId The ID of the stream item.
     * @return The associated raw contact ID, or -1 if no such stream item exists.
     */
    private long lookupRawContactIdForStreamId(long streamItemId) {
        long rawContactId = -1;
        Cursor c = mActiveDb.get().query(Tables.STREAM_ITEMS,
                new String[]{StreamItems.RAW_CONTACT_ID},
                StreamItems._ID + "=?", new String[]{String.valueOf(streamItemId)},
                null, null, null);
        try {
            if (c.moveToFirst()) {
                rawContactId = c.getLong(0);
            }
        } finally {
            c.close();
        }
        return rawContactId;
    }

    /**
     * If the given URI is reading stream items or stream photos, this will run a permission check
     * for the android.permission.READ_SOCIAL_STREAM permission - otherwise it will do nothing.
     * @param uri The URI to check.
     */
    private void enforceSocialStreamReadPermission(Uri uri) {
        if (SOCIAL_STREAM_URIS.contains(sUriMatcher.match(uri))
                && !isValidPreAuthorizedUri(uri)) {
            getContext().enforceCallingOrSelfPermission(
                    "android.permission.READ_SOCIAL_STREAM", null);
        }
    }

    /**
     * If the given URI is modifying stream items or stream photos, this will run a permission check
     * for the android.permission.WRITE_SOCIAL_STREAM permission - otherwise it will do nothing.
     * @param uri The URI to check.
     */
    private void enforceSocialStreamWritePermission(Uri uri) {
        if (SOCIAL_STREAM_URIS.contains(sUriMatcher.match(uri))) {
            getContext().enforceCallingOrSelfPermission(
                    "android.permission.WRITE_SOCIAL_STREAM", null);
        }
    }

    /**
     * Checks whether the given raw contact ID is owned by the given account.
     * If the resolved account is null, this will return true iff the raw contact
     * is also associated with the "null" account.
     *
     * If the resolved account does not match, this will throw a security exception.
     * @param account The resolved account (may be null).
     * @param rawContactId The raw contact ID to check for.
     */
    private void enforceModifyingAccount(Account account, long rawContactId) {
        String accountSelection = RawContactsColumns.CONCRETE_ID + "=? AND "
                + RawContactsColumns.CONCRETE_ACCOUNT_NAME + "=? AND "
                + RawContactsColumns.CONCRETE_ACCOUNT_TYPE + "=?";
        String noAccountSelection = RawContactsColumns.CONCRETE_ID + "=? AND "
                + RawContactsColumns.CONCRETE_ACCOUNT_NAME + " IS NULL AND "
                + RawContactsColumns.CONCRETE_ACCOUNT_TYPE + " IS NULL";
        Cursor c;
        if (account != null) {
            c = mActiveDb.get().query(Tables.RAW_CONTACTS,
                    new String[]{RawContactsColumns.CONCRETE_ID}, accountSelection,
                    new String[]{String.valueOf(rawContactId), mAccount.name, mAccount.type},
                    null, null, null);
        } else {
            c = mActiveDb.get().query(Tables.RAW_CONTACTS,
                    new String[]{RawContactsColumns.CONCRETE_ID}, noAccountSelection,
                    new String[]{String.valueOf(rawContactId)},
                    null, null, null);
        }
        
        //check whether the raw contact is local phone.
        if (c.getCount() == 0) {
            /*
             * Bug Fix by Mediatek Begin.
             *   CR ID: ALPS00265495
             *   Descriptions: Fix the klocwork's warning complaint
             */
            c.close();
            /*
             * Bug Fix by Mediatek End.
             */
            String localAccountSelection = RawContactsColumns.CONCRETE_ID + "=? AND "
                                    + Clauses.RAW_CONTACT_IS_LOCAL_PHONE;
            c = mActiveDb.get().query(Tables.RAW_CONTACTS,
                    new String[]{RawContactsColumns.CONCRETE_ID}, localAccountSelection,
                    new String[]{String.valueOf(rawContactId)},
                    null, null, null);
        }
        
        try {
            if(c.getCount() == 0) {
                throw new SecurityException("Caller account does not match raw contact ID "
                    + rawContactId);
            }
        } finally {
            c.close();
        }
    }

    /**
     * Checks whether the given selection of stream items matches up with the given
     * account.  If any of the raw contacts fail the account check, this will throw a
     * security exception.
     * @param account The resolved account (may be null).
     * @param selection The selection.
     * @param selectionArgs The selection arguments.
     * @return The list of stream item IDs that would be included in this selection.
     */
    private List<Long> enforceModifyingAccountForStreamItems(Account account, String selection,
            String[] selectionArgs) {
        List<Long> streamItemIds = Lists.newArrayList();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        setTablesAndProjectionMapForStreamItems(qb);
        Cursor c = qb.query(mActiveDb.get(),
                new String[]{StreamItems._ID, StreamItems.RAW_CONTACT_ID},
                selection, selectionArgs, null, null, null);
        try {
            while (c.moveToNext()) {
                streamItemIds.add(c.getLong(0));

                // Throw a security exception if the account doesn't match the raw contact's.
                enforceModifyingAccount(account, c.getLong(1));
            }
        } finally {
            c.close();
        }
        return streamItemIds;
    }

    /**
     * Checks whether the given selection of stream item photos matches up with the given
     * account.  If any of the raw contacts fail the account check, this will throw a
     * security exception.
     * @param account The resolved account (may be null).
     * @param selection The selection.
     * @param selectionArgs The selection arguments.
     * @return The list of stream item photo IDs that would be included in this selection.
     */
    private List<Long> enforceModifyingAccountForStreamItemPhotos(Account account, String selection,
            String[] selectionArgs) {
        List<Long> streamItemPhotoIds = Lists.newArrayList();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        setTablesAndProjectionMapForStreamItemPhotos(qb);
        Cursor c = qb.query(mActiveDb.get(),
                new String[]{StreamItemPhotos._ID, StreamItems.RAW_CONTACT_ID},
                selection, selectionArgs, null, null, null);
        try {
            while (c.moveToNext()) {
                streamItemPhotoIds.add(c.getLong(0));

                // Throw a security exception if the account doesn't match the raw contact's.
                enforceModifyingAccount(account, c.getLong(1));
            }
        } finally {
            c.close();
        }
        return streamItemPhotoIds;
    }

    /**
     * Queries the database for stream items under the given raw contact.  If there are
     * more entries than {@link ContactsProvider2#MAX_STREAM_ITEMS_PER_RAW_CONTACT},
     * the oldest entries (as determined by timestamp) will be deleted.
     * @param rawContactId The raw contact ID to examine for stream items.
     * @param insertedStreamItemId The ID of the stream item that was just inserted,
     *     prompting this cleanup.  Callers may pass 0 if no insertion prompted the
     *     cleanup.
     * @return The ID of the inserted stream item if it still exists after cleanup;
     *     0 otherwise.
     */
    private long cleanUpOldStreamItems(long rawContactId, long insertedStreamItemId) {
        long postCleanupInsertedStreamId = insertedStreamItemId;
        Cursor c = mActiveDb.get().query(Tables.STREAM_ITEMS, new String[]{StreamItems._ID},
                StreamItems.RAW_CONTACT_ID + "=?", new String[]{String.valueOf(rawContactId)},
                null, null, StreamItems.TIMESTAMP + " DESC, " + StreamItems._ID + " DESC");
        try {
            int streamItemCount = c.getCount();
            if (streamItemCount <= MAX_STREAM_ITEMS_PER_RAW_CONTACT) {
                // Still under the limit - nothing to clean up!
                return insertedStreamItemId;
            } else {
                c.moveToLast();
                while (c.getPosition() >= MAX_STREAM_ITEMS_PER_RAW_CONTACT) {
                    long streamItemId = c.getLong(0);
                    if (insertedStreamItemId == streamItemId) {
                        // The stream item just inserted is being deleted.
                        postCleanupInsertedStreamId = 0;
                    }
                    deleteStreamItem(c.getLong(0));
                    c.moveToPrevious();
                }
            }
        } finally {
            c.close();
        }
        return postCleanupInsertedStreamId;
    }

    /**
     * Delete data row by row so that fixing of primaries etc work correctly.
     */
    private int deleteData(String selection, String[] selectionArgs, boolean callerIsSyncAdapter) {
        int count = 0;

        // Note that the query will return data according to the access restrictions,
        // so we don't need to worry about deleting data we don't have permission to read.
        Uri dataUri = inProfileMode()
                ? Uri.withAppendedPath(Profile.CONTENT_URI, RawContacts.Data.CONTENT_DIRECTORY)
                : Data.CONTENT_URI;
                
        if (ContactsProvidersApplication.sIsAuroraPrivacySupport 
                && dataUri.toString().equals(Data.CONTENT_URI.toString())) {
            selection = parseSelection(selection, true); // aurora wangth 20140930 add for privacy
        }
        Cursor c = query(dataUri, DataRowHandler.DataDeleteQuery.COLUMNS,
                selection, selectionArgs, null);
        try {
            while(c.moveToNext()) {
                long rawContactId = c.getLong(DataRowHandler.DataDeleteQuery.RAW_CONTACT_ID);
                String mimeType = c.getString(DataRowHandler.DataDeleteQuery.MIMETYPE);
                DataRowHandler rowHandler = getDataRowHandler(mimeType);
                count += rowHandler.delete(mActiveDb.get(), mTransactionContext.get(), c);
                if (!callerIsSyncAdapter) {
//                    mTransactionContext.get().markRawContactDirty(rawContactId);
                    mTransactionContext.get().markRawContactDirtyAndChanged(rawContactId,
                            callerIsSyncAdapter);
                }
            }
        } finally {
            c.close();
        }

        return count;
    }

    /**
     * Delete a data row provided that it is one of the allowed mime types.
     */
    public int deleteData(long dataId, String[] allowedMimeTypes) {

        // Note that the query will return data according to the access restrictions,
        // so we don't need to worry about deleting data we don't have permission to read.
        mSelectionArgs1[0] = String.valueOf(dataId);
        Cursor c = query(Data.CONTENT_URI, DataRowHandler.DataDeleteQuery.COLUMNS, Data._ID + "=?",
                mSelectionArgs1, null);

        try {
            if (!c.moveToFirst()) {
                return 0;
            }

            String mimeType = c.getString(DataRowHandler.DataDeleteQuery.MIMETYPE);
            boolean valid = false;
            for (int i = 0; i < allowedMimeTypes.length; i++) {
                if (TextUtils.equals(mimeType, allowedMimeTypes[i])) {
                    valid = true;
                    break;
                }
            }

            if (!valid) {
                throw new IllegalArgumentException("Data type mismatch: expected "
                        + Lists.newArrayList(allowedMimeTypes));
            }
            DataRowHandler rowHandler = getDataRowHandler(mimeType);
            return rowHandler.delete(mActiveDb.get(), mTransactionContext.get(), c);
        } finally {
            c.close();
        }
    }

    /**
     * Inserts an item in the groups table
     */
    private long insertGroup(Uri uri, ContentValues values, boolean callerIsSyncAdapter) {
        mValues.clear();
        mValues.putAll(values);

        final AccountWithDataSet accountWithDataSet = resolveAccountWithDataSet(uri, mValues);
        
        // aurora <wangth> <2014-1-16> add for aurora begin
        String accountType = mValues.getAsString(Groups.ACCOUNT_TYPE);
        String accountName = mValues.getAsString(Groups.ACCOUNT_NAME);
        Log.d(TAG, "mValues.containsKey(Groups.GROUP_VISIBLE) = " + mValues.containsKey(Groups.GROUP_VISIBLE));
        Log.d(TAG, "accountType = " + accountType + "    accountName = " + accountName);
        if (accountName == null && accountType == null) {
            accountName = "Phone";
            accountType = ACCOUNT_TYPE_LOCAL_PHONE;
            mValues.put(Groups.ACCOUNT_TYPE, accountType);
            mValues.put(Groups.ACCOUNT_NAME, accountName);
        }
        
        if (null != accountType) {
            Cursor c = null;
            boolean visible = false;
            try {
                c = mActiveDb.get().query(Tables.SETTINGS,
                        new String[]{Settings.UNGROUPED_VISIBLE},
                        "account_name = '" + accountName + "' and account_type = '" + accountType + "'", 
                        null, null, null, null);
                
                if (c != null && c.moveToFirst()) {
                    if (c.getInt(0) == 1) {
                        visible = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null != c) {
                    c.close();
                }
            }
            
            Log.d(TAG, "visible = " + visible);
            if (visible) {
                mValues.put(Groups.GROUP_VISIBLE, 1);
            } else {
                mValues.put(Groups.GROUP_VISIBLE, 0);
            }
        }
        
        // aurora <wangth> <2014-1-16> add for aurora end
        final long accountId = mDbHelper.get().getOrCreateAccountIdInTransaction(
                resolveAccountWithDataSet(uri, mValues));
        mValues.remove(Groups.ACCOUNT_NAME);
        mValues.remove(Groups.ACCOUNT_TYPE);
        mValues.remove(Groups.DATA_SET);
        mValues.put(GroupsColumns.ACCOUNT_ID, accountId);

        // Replace package with internal mapping
        final String packageName = mValues.getAsString(Groups.RES_PACKAGE);
        if (packageName != null) {
            mValues.put(GroupsColumns.PACKAGE_ID, mDbHelper.get().getPackageId(packageName));
        }
        mValues.remove(Groups.RES_PACKAGE);

        final boolean isFavoritesGroup = mValues.getAsLong(Groups.FAVORITES) != null
                ? mValues.getAsLong(Groups.FAVORITES) != 0
                : false;

        if (!callerIsSyncAdapter) {
            mValues.put(Groups.DIRTY, 1);
        }

        //Gionee:huangzy 20121128 add for CR00736966 start
        ContactsDatabaseHelper.writeGnVersion(getContext(), mValues);
        //Gionee:huangzy 20121128 add for CR00736966 end

        /*
         * Bug Fix by Mediatek Begin
         * CR ID :ALPS000293195
         * Descriptions: 
         *   Add group sourceid for Sync issue:
         *     The contact with picture will be synchronized from server 
         *     while modified on phone device to add into groups. 
         *     And Exchange.apk add sourceid as group name.
         *     So add sourceid as title when create groups.
         */
        if (!mValues.containsKey(Groups.SOURCE_ID)) {
            String title = mValues.getAsString(Groups.TITLE);
            mValues.put(Groups.SOURCE_ID, title);
        }
        Log.i(TAG, "insertGroup mValues:" + mValues.toString());
        /*
         * Bug Fix by Mediatek End
         */
        
        // aurora <wangth> <2014-03-07> add for aurora begin
        Cursor cur = null;
        try {
            cur = mActiveDb.get().query(Tables.GROUPS,
                    new String[]{Groups.TITLE},
                    Groups.DELETED + "=0 and " +GroupsColumns.ACCOUNT_ID + "=" + accountId, 
                    null, null, null, null);
            
            boolean exit = false;
            String dbTitle;
            if (cur != null && cur.moveToFirst()) {
                do {
                    dbTitle = cur.getString(0);
                    Log.e(TAG, "dbTitle = " + dbTitle + "  mValues.getAsString(Groups.TITLE) = " + mValues.getAsString(Groups.TITLE));
                    if (dbTitle != null && dbTitle.equals(mValues.getAsString(Groups.TITLE))) {
                        exit = true;
                        break;
                    }
                } while (cur.moveToNext());
                
                if (exit) {
                    return 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cur) {
                cur.close();
            }
        }
        // aurora <wangth> <2014-03-07> add for aurora end
        
        long result = mActiveDb.get().insert(Tables.GROUPS, Groups.TITLE, mValues);

        if (!callerIsSyncAdapter && isFavoritesGroup) {
            // If the inserted group is a favorite group, add all starred raw contacts to it.
            mSelectionArgs1[0] = Long.toString(accountId);
            Cursor c = mDbHelper.get().getWritableDatabase().query(Tables.RAW_CONTACTS,
                    new String[]{RawContacts._ID, RawContacts.STARRED},
                    RawContactsColumns.CONCRETE_ACCOUNT_ID + "=?", mSelectionArgs1,
                    null, null, null);
            try {
                while (c.moveToNext()) {
                    if (c.getLong(1) != 0) {
                        final long rawContactId = c.getLong(0);
                        insertDataGroupMembership(rawContactId, result);
                        mTransactionContext.get().markRawContactDirtyAndChanged(rawContactId,
                                callerIsSyncAdapter);
                    }
                }
            } finally {
                c.close();
            }
        }

        if (mValues.containsKey(Groups.GROUP_VISIBLE)) {
            mVisibleTouched = true;
        }

        return result;
    }

    private long insertSettings(Uri uri, ContentValues values) {
        // Before inserting, ensure that no settings record already exists for the
        // values being inserted (this used to be enforced by a primary key, but that no
        // longer works with the nullable data_set field added).
        String accountName = values.getAsString(Settings.ACCOUNT_NAME);
        String accountType = values.getAsString(Settings.ACCOUNT_TYPE);
        String dataSet = values.getAsString(Settings.DATA_SET);
        Log.d(TAG, "insertSettings(), accountName= "+ accountName+ "****");
        Uri.Builder settingsUri = Settings.CONTENT_URI.buildUpon();
        if (accountName != null) {
            settingsUri.appendQueryParameter(Settings.ACCOUNT_NAME, accountName);
        }
        if (accountType != null) {
            settingsUri.appendQueryParameter(Settings.ACCOUNT_TYPE, accountType);
        }
        if (dataSet != null) {
            settingsUri.appendQueryParameter(Settings.DATA_SET, dataSet);
        }
        Cursor c = queryLocal(settingsUri.build(), null, null, null, null, 0);
        try {
            if (c.getCount() > 0) {
                // If a record was found, replace it with the new values.
                String selection = null;
                String[] selectionArgs = null;
                if (accountName != null && accountType != null) {
                    selection = Settings.ACCOUNT_NAME + "=? AND " + Settings.ACCOUNT_TYPE + "=?";
                    if (dataSet == null) {
                        selection += " AND " + Settings.DATA_SET + " IS NULL";
                        selectionArgs = new String[] {accountName, accountType};
                    } else {
                        selection += " AND " + Settings.DATA_SET + "=?";
                        selectionArgs = new String[] {accountName, accountType, dataSet};
                    }
                }
                Log.d(TAG, "insertSettings(), selection= "+ selection+ "****");
                return updateSettings(uri, values, selection, selectionArgs);
            }
        } finally {
            c.close();
        }

        // If we didn't find a duplicate, we're fine to insert.
        final long id = mActiveDb.get().insert(Tables.SETTINGS, null, values);

        if (values.containsKey(Settings.UNGROUPED_VISIBLE)) {
            mVisibleTouched = true;
        }

        return id;
    }

    /**
     * Inserts a status update.
     */
    public long insertStatusUpdate(ContentValues values) {
        final String handle = values.getAsString(StatusUpdates.IM_HANDLE);
        final Integer protocol = values.getAsInteger(StatusUpdates.PROTOCOL);
        String customProtocol = null;

        if (protocol != null && protocol == Im.PROTOCOL_CUSTOM) {
            customProtocol = values.getAsString(StatusUpdates.CUSTOM_PROTOCOL);
            if (TextUtils.isEmpty(customProtocol)) {
                throw new IllegalArgumentException(
                        "CUSTOM_PROTOCOL is required when PROTOCOL=PROTOCOL_CUSTOM");
            }
        }

        long rawContactId = -1;
        long contactId = -1;
        Long dataId = values.getAsLong(StatusUpdates.DATA_ID);
        String accountType = null;
        String accountName = null;
        mSb.setLength(0);
        mSelectionArgs.clear();
        if (dataId != null) {
            // Lookup the contact info for the given data row.

            mSb.append(Tables.DATA + "." + Data._ID + "=?");
            mSelectionArgs.add(String.valueOf(dataId));
        } else {
            // Lookup the data row to attach this presence update to

            if (TextUtils.isEmpty(handle) || protocol == null) {
                throw new IllegalArgumentException("PROTOCOL and IM_HANDLE are required");
            }

            // TODO: generalize to allow other providers to match against email
            boolean matchEmail = Im.PROTOCOL_GOOGLE_TALK == protocol;

            String mimeTypeIdIm = String.valueOf(mDbHelper.get().getMimeTypeIdForIm());
            if (matchEmail) {
                String mimeTypeIdEmail = String.valueOf(mDbHelper.get().getMimeTypeIdForEmail());

                // The following hack forces SQLite to use the (mimetype_id,data1) index, otherwise
                // the "OR" conjunction confuses it and it switches to a full scan of
                // the raw_contacts table.

                // This code relies on the fact that Im.DATA and Email.DATA are in fact the same
                // column - Data.DATA1
                mSb.append(DataColumns.MIMETYPE_ID + " IN (?,?)" +
                        " AND " + Data.DATA1 + "=?" +
                        " AND ((" + DataColumns.MIMETYPE_ID + "=? AND " + Im.PROTOCOL + "=?");
                mSelectionArgs.add(mimeTypeIdEmail);
                mSelectionArgs.add(mimeTypeIdIm);
                mSelectionArgs.add(handle);
                mSelectionArgs.add(mimeTypeIdIm);
                mSelectionArgs.add(String.valueOf(protocol));
                if (customProtocol != null) {
                    mSb.append(" AND " + Im.CUSTOM_PROTOCOL + "=?");
                    mSelectionArgs.add(customProtocol);
                }
                mSb.append(") OR (" + DataColumns.MIMETYPE_ID + "=?))");
                mSelectionArgs.add(mimeTypeIdEmail);
            } else {
                mSb.append(DataColumns.MIMETYPE_ID + "=?" +
                        " AND " + Im.PROTOCOL + "=?" +
                        " AND " + Im.DATA + "=?");
                mSelectionArgs.add(mimeTypeIdIm);
                mSelectionArgs.add(String.valueOf(protocol));
                mSelectionArgs.add(handle);
                if (customProtocol != null) {
                    mSb.append(" AND " + Im.CUSTOM_PROTOCOL + "=?");
                    mSelectionArgs.add(customProtocol);
                }
            }

            if (values.containsKey(StatusUpdates.DATA_ID)) {
                mSb.append(" AND " + DataColumns.CONCRETE_ID + "=?");
                mSelectionArgs.add(values.getAsString(StatusUpdates.DATA_ID));
            }
        }

        Cursor cursor = null;
        try {
            cursor = mActiveDb.get().query(DataContactsQuery.TABLE, DataContactsQuery.PROJECTION,
                    mSb.toString(), mSelectionArgs.toArray(EMPTY_STRING_ARRAY), null, null,
                    Clauses.CONTACT_VISIBLE + " DESC, " + Data.RAW_CONTACT_ID);
            if (cursor.moveToFirst()) {
                dataId = cursor.getLong(DataContactsQuery.DATA_ID);
                rawContactId = cursor.getLong(DataContactsQuery.RAW_CONTACT_ID);
                accountType = cursor.getString(DataContactsQuery.ACCOUNT_TYPE);
                accountName = cursor.getString(DataContactsQuery.ACCOUNT_NAME);
                contactId = cursor.getLong(DataContactsQuery.CONTACT_ID);
            } else {
                // No contact found, return a null URI
                return -1;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (values.containsKey(StatusUpdates.PRESENCE)) {
            if (customProtocol == null) {
                // We cannot allow a null in the custom protocol field, because SQLite3 does not
                // properly enforce uniqueness of null values
                customProtocol = "";
            }

            mValues.clear();
            mValues.put(StatusUpdates.DATA_ID, dataId);
            mValues.put(PresenceColumns.RAW_CONTACT_ID, rawContactId);
            mValues.put(PresenceColumns.CONTACT_ID, contactId);
            mValues.put(StatusUpdates.PROTOCOL, protocol);
            mValues.put(StatusUpdates.CUSTOM_PROTOCOL, customProtocol);
            mValues.put(StatusUpdates.IM_HANDLE, handle);
            if (values.containsKey(StatusUpdates.IM_ACCOUNT)) {
                mValues.put(StatusUpdates.IM_ACCOUNT, values.getAsString(StatusUpdates.IM_ACCOUNT));
            }
            mValues.put(StatusUpdates.PRESENCE,
                    values.getAsString(StatusUpdates.PRESENCE));
            mValues.put(StatusUpdates.CHAT_CAPABILITY,
                    values.getAsString(StatusUpdates.CHAT_CAPABILITY));

            // Insert the presence update
            mActiveDb.get().replace(Tables.PRESENCE, null, mValues);
        }


        if (values.containsKey(StatusUpdates.STATUS)) {
            String status = values.getAsString(StatusUpdates.STATUS);
            String resPackage = values.getAsString(StatusUpdates.STATUS_RES_PACKAGE);
            Resources resources = getContext().getResources();
            if (!TextUtils.isEmpty(resPackage)) {
                PackageManager pm = getContext().getPackageManager();
                try {
                    resources = pm.getResourcesForApplication(resPackage);
                } catch (NameNotFoundException e) {
                    Log.w(TAG, "Contact status update resource package not found: "
                            + resPackage);
                }
            }
            Integer labelResourceId = values.getAsInteger(StatusUpdates.STATUS_LABEL);

            if ((labelResourceId == null || labelResourceId == 0) && protocol != null) {
                labelResourceId = Im.getProtocolLabelResource(protocol);
            }
            String labelResource = getResourceName(resources, "string", labelResourceId);

            Integer iconResourceId = values.getAsInteger(StatusUpdates.STATUS_ICON);
            // TODO compute the default icon based on the protocol

            String iconResource = getResourceName(resources, "drawable", iconResourceId);

            if (TextUtils.isEmpty(status)) {
                mDbHelper.get().deleteStatusUpdate(dataId);
            } else {
                Long timestamp = values.getAsLong(StatusUpdates.STATUS_TIMESTAMP);
                if (timestamp != null) {
                    mDbHelper.get().replaceStatusUpdate(dataId, timestamp, status, resPackage,
                            iconResourceId, labelResourceId);
                } else {
                    mDbHelper.get().insertStatusUpdate(dataId, status, resPackage, iconResourceId,
                            labelResourceId);
                }

                // For forward compatibility with the new stream item API, insert this status update
                // there as well.  If we already have a stream item from this source, update that
                // one instead of inserting a new one (since the semantics of the old status update
                // API is to only have a single record).
                if (rawContactId != -1 && !TextUtils.isEmpty(status)) {
                    ContentValues streamItemValues = new ContentValues();
                    streamItemValues.put(StreamItems.RAW_CONTACT_ID, rawContactId);
                    // Status updates are text only but stream items are HTML.
                    streamItemValues.put(StreamItems.TEXT, statusUpdateToHtml(status));
                    streamItemValues.put(StreamItems.COMMENTS, "");
                    streamItemValues.put(StreamItems.RES_PACKAGE, resPackage);
                    streamItemValues.put(StreamItems.RES_ICON, iconResource);
                    streamItemValues.put(StreamItems.RES_LABEL, labelResource);
                    streamItemValues.put(StreamItems.TIMESTAMP,
                            timestamp == null ? System.currentTimeMillis() : timestamp);

                    // Note: The following is basically a workaround for the fact that status
                    // updates didn't do any sort of account enforcement, while social stream item
                    // updates do.  We can't expect callers of the old API to start passing account
                    // information along, so we just populate the account params appropriately for
                    // the raw contact.  Data set is not relevant here, as we only check account
                    // name and type.
                    if (accountName != null && accountType != null) {
                        streamItemValues.put(RawContacts.ACCOUNT_NAME, accountName);
                        streamItemValues.put(RawContacts.ACCOUNT_TYPE, accountType);
                    }

                    // Check for an existing stream item from this source, and insert or update.
                    Uri streamUri = StreamItems.CONTENT_URI;
                    Cursor c = queryLocal(streamUri, new String[]{StreamItems._ID},
                            StreamItems.RAW_CONTACT_ID + "=?",
                            new String[]{String.valueOf(rawContactId)},
                            null, -1 /* directory ID */);
                    try {
                        if (c.getCount() > 0) {
                            c.moveToFirst();
                            updateInTransaction(ContentUris.withAppendedId(streamUri, c.getLong(0)),
                                    streamItemValues, null, null);
                        } else {
                            insertInTransaction(streamUri, streamItemValues);
                        }
                    } finally {
                        c.close();
                    }
                }
            }
        }

        if (contactId != -1) {
            mAggregator.get().updateLastStatusUpdateId(contactId);
        }

        return dataId;
    }

    /** Converts a status update to HTML. */
    private String statusUpdateToHtml(String status) {
        return TextUtils.htmlEncode(status);
    }

    private String getResourceName(Resources resources, String expectedType, Integer resourceId) {
        try {
            if (resourceId == null || resourceId == 0) return null;

            // Resource has an invalid type (e.g. a string as icon)? ignore
            final String resourceEntryName = resources.getResourceEntryName(resourceId);
            final String resourceTypeName = resources.getResourceTypeName(resourceId);
            if (!expectedType.equals(resourceTypeName)) {
                Log.w(TAG, "Resource " + resourceId + " (" + resourceEntryName + ") is of type " +
                        resourceTypeName + " but " + expectedType + " is required.");
                return null;
            }

            return resourceEntryName;
        } catch (NotFoundException e) {
            return null;
        }
    }

    @Override
    protected int deleteInTransaction(Uri uri, String selection, String[] selectionArgs) {
        if (VERBOSE_LOGGING) {
            Log.v(TAG, "deleteInTransaction: " + uri + "   &&  selection = " + selection);
        }

        // Default active DB to the contacts DB if none has been set.
        if (mActiveDb.get() == null) {
            mActiveDb.set(mContactsHelper.getWritableDatabase());
        }
        
        flushTransactionalChanges();
        final boolean callerIsSyncAdapter =
                readBooleanQueryParameter(uri, GnContactsContract.CALLER_IS_SYNCADAPTER, false);
        final int match = sUriMatcher.match(uri);
        String where="";
        Log.e(TAG, "match = " + match);
        switch (match) {
            case SYNCSTATE:
            case PROFILE_SYNCSTATE:
                return mDbHelper.get().getSyncState().delete(mActiveDb.get(), selection,
                        selectionArgs);

            case SYNCSTATE_ID: {
                String selectionWithId =
                        (SyncStateContract.Columns._ID + "=" + ContentUris.parseId(uri) + " ")
                        + (selection == null ? "" : " AND (" + selection + ")");
                return mDbHelper.get().getSyncState().delete(mActiveDb.get(), selectionWithId,
                        selectionArgs);
            }

            case PROFILE_SYNCSTATE_ID: {
                String selectionWithId =
                        (SyncStateContract.Columns._ID + "=" + ContentUris.parseId(uri) + " ")
                        + (selection == null ? "" : " AND (" + selection + ")");
                return mProfileHelper.getSyncState().delete(mActiveDb.get(), selectionWithId,
                        selectionArgs);
            }

            case CONTACTS: {
                // TODO
                return 0;
            }

            case CONTACTS_ID: {
                long contactId = ContentUris.parseId(uri);
                return deleteContact(contactId, callerIsSyncAdapter);
            }

            case CONTACTS_LOOKUP: {
                final List<String> pathSegments = uri.getPathSegments();
                final int segmentCount = pathSegments.size();
                if (segmentCount < 3) {
                    throw new IllegalArgumentException(mDbHelper.get().exceptionMessage(
                            "Missing a lookup key", uri));
                }
                final String lookupKey = pathSegments.get(2);
                final long contactId = lookupContactIdByLookupKey(mActiveDb.get(), lookupKey);
                return deleteContact(contactId, callerIsSyncAdapter);
            }

            case CONTACTS_LOOKUP_ID: {
                // lookup contact by id and lookup key to see if they still match the actual record
                final List<String> pathSegments = uri.getPathSegments();
                final String lookupKey = pathSegments.get(2);
                SQLiteQueryBuilder lookupQb = new SQLiteQueryBuilder();
                setTablesAndProjectionMapForContacts(lookupQb, uri, null);
                long contactId = ContentUris.parseId(uri);
                String[] args;
                if (selectionArgs == null) {
                    args = new String[2];
                } else {
                    args = new String[selectionArgs.length + 2];
                    System.arraycopy(selectionArgs, 0, args, 2, selectionArgs.length);
                }
                args[0] = String.valueOf(contactId);
                args[1] = Uri.encode(lookupKey);
                lookupQb.appendWhere(Contacts._ID + "=? AND " + Contacts.LOOKUP_KEY + "=?");
                Cursor c = query(mActiveDb.get(), lookupQb, null, selection, args, null, null,
                        null);
                try {
                    if (c.getCount() == 1) {
                        // contact was unmodified so go ahead and delete it
                        return deleteContact(contactId, callerIsSyncAdapter);
                    } else {
                        // row was changed (e.g. the merging might have changed), we got multiple
                        // rows or the supplied selection filtered the record out
                        return 0;
                    }
                } finally {
                    c.close();
                }
            }
            
            case CONTACTS_DELETE_USAGE: {
                return deleteDataUsage();
            }

            case RAW_CONTACTS:
                // aurora privacy add
                if (ContactsProvidersApplication.sIsAuroraPrivacySupport
                        && selection != null && selection.contains(AURORA_DEFAULT_PRIVACY_COLUMN)) {
                    try {
                        long rawContactId = Long.valueOf(selectionArgs[0]);
                        mAggregator.get().deleteCallsPrivacy(rawContactId);
            	        getContext().getContentResolver().notifyChange(Calls.CONTENT_URI, null, false);
                        
                        Cursor dataC = mActiveDb.get().query(Tables.DATA,
                                new String[] {Data.DATA1}, 
                                Data.RAW_CONTACT_ID + "=" + rawContactId + " AND mimetype_id=5",
                                null, null, null, null);
                        if (dataC != null) {
                            try {
                                while (dataC.moveToNext()) {
                                    String data1 = dataC.getString(0);
                                    AuroraPrivacyUtils.sendBroadToMms(data1, -1, false);
                                    AuroraPrivacyUtils.updateCallRecordings(data1, -1);
                                }
                            } finally {
                                dataC.close();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                // The following lines are provided and maintained by Mediatek inc.
                // Only process raw_contacts for sim, do not break here.
                if ("true".equals(uri.getQueryParameter("sim"))) {
                    Log.i(TAG,"Before delete "); 
                    int count = 0;
                    if (ContactsProvidersApplication.sIsGnDialerSearchSupport) {
                    	count = updateGnDialerSearchDataForMultiDelete(selection, selectionArgs);
                    } else {
                        count = mActiveDb.get().delete(Tables.RAW_CONTACTS, selection, selectionArgs);
                    }
                    Log.i(TAG,"count is "+count);
                    return count;
                }
                // The previous lines are provided and maintained by Mediatek inc.
            case PROFILE_RAW_CONTACTS: {
                // The following lines are provided and maintained by Mediatek Inc.
                // Support delete in one batch. Need client set the query parameter
                // batch as true and callerIsSyncAdapter as true. 
                Log.i(TAG, "deleteInTransaction for raw_contacts uri is " + uri.toString()
                        + " callerIsSyncAdapter is " + callerIsSyncAdapter);
                if (callerIsSyncAdapter && ("true".equals(uri.getQueryParameter("batch")))) {
                    Log.i(TAG, "Delete in one batch begin");
                    int count = 0;
                    count = deleteRawContactInOneBatch(uri, selection, selectionArgs);
                    Log.i(TAG, "deleteRawContactInOneBatch count is " + count);
                    Log.i(TAG, "Delete in one batch end");
                    return count;
                }
                // The previous lines are provided and maintained by Mediatek Inc.
                int numDeletes = 0;
                Cursor c = mActiveDb.get().query(Tables.RAW_CONTACTS,
                        new String[]{RawContacts._ID, RawContacts.CONTACT_ID},
                        appendAccountIdToSelection(uri, selection), selectionArgs, null, null, null);
                try {
                    while (c.moveToNext()) {
                        final long rawContactId = c.getLong(0);
                        long contactId = c.getLong(1);
                        numDeletes += deleteRawContact(rawContactId, contactId,
                                callerIsSyncAdapter);
                    }
                } finally {
                    c.close();
                }
                return numDeletes;
            }

            case RAW_CONTACTS_ID:
            case PROFILE_RAW_CONTACTS_ID: {
                final long rawContactId = ContentUris.parseId(uri);
                return deleteRawContact(rawContactId, mDbHelper.get().getContactId(rawContactId),
                        callerIsSyncAdapter);
            }

            case DATA:
            case PROFILE_DATA: {
                mSyncToNetwork |= !callerIsSyncAdapter;
                return deleteData(appendAccountToSelection(uri, selection), selectionArgs,
                        callerIsSyncAdapter);
            }

            case DATA_ID:
            case PHONES_ID:
            case EMAILS_ID:
            // Gionee lihuafang add for phone error begin
            case CALLABLES_ID:
            // Gionee lihuafang add for phone error end
            case POSTALS_ID:
            case PROFILE_DATA_ID: {
                long dataId = ContentUris.parseId(uri);
                mSyncToNetwork |= !callerIsSyncAdapter;
                mSelectionArgs1[0] = String.valueOf(dataId);
                return deleteData(Data._ID + "=?", mSelectionArgs1, callerIsSyncAdapter);
            }

            case GROUPS_ID: {
                mSyncToNetwork |= !callerIsSyncAdapter;
                return deleteGroup(uri, ContentUris.parseId(uri), callerIsSyncAdapter);
            }

            case GROUPS: {
                int numDeletes = 0;
                final String accountType = getQueryParameter(uri, RawContacts.ACCOUNT_TYPE);
                Cursor c = mActiveDb.get().query(Tables.GROUPS, new String[]{Groups._ID},
                		appendAccountIdToSelection(uri, selection), selectionArgs, null, null, null);
                
                // Gionee:wangth 20121215 add for CR00741560 begin
                String usimGroupType = "USIM Account";
                boolean isUsimGroup = false;
                if (usimGroupType.equals(accountType)) {
                    isUsimGroup = true;
                }
                // Gionee:wangth 20121215 add for CR00741560 end
                
                try {
                    while (c.moveToNext()) {
                        // Gionee:wangth 20121215 add for CR00741560 begin
                        // clear usim group of Contacts2.db when boot device
                        if (ContactsProvidersApplication.sIsGnContactsSupport && isUsimGroup) {
                            numDeletes += deleteGroup(uri, c.getLong(0), true);
                        }
                        // Gionee:wangth 20121215 add for CR00741560 end
                        numDeletes += deleteGroup(uri, c.getLong(0), callerIsSyncAdapter);
                    }
                } finally {
                    c.close();
                }
                if (numDeletes > 0) {
                    mSyncToNetwork |= !callerIsSyncAdapter;
                }
                return numDeletes;
            }

            case SETTINGS: {
                mSyncToNetwork |= !callerIsSyncAdapter;
                return deleteSettings(uri, appendAccountToSelection(uri, selection), selectionArgs);
            }

            case STATUS_UPDATES:
            case PROFILE_STATUS_UPDATES: {
                return deleteStatusUpdates(selection, selectionArgs);
            }

            case STREAM_ITEMS: {
                mSyncToNetwork |= !callerIsSyncAdapter;
                return deleteStreamItems(uri, new ContentValues(), selection, selectionArgs);
            }

            case STREAM_ITEMS_ID: {
                mSyncToNetwork |= !callerIsSyncAdapter;
                return deleteStreamItems(uri, new ContentValues(),
                        StreamItems._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
            }

            case RAW_CONTACTS_ID_STREAM_ITEMS_ID: {
                mSyncToNetwork |= !callerIsSyncAdapter;
                String rawContactId = uri.getPathSegments().get(1);
                String streamItemId = uri.getLastPathSegment();
                return deleteStreamItems(uri, new ContentValues(),
                        StreamItems.RAW_CONTACT_ID + "=? AND " + StreamItems._ID + "=?",
                        new String[]{rawContactId, streamItemId});

            }

            case STREAM_ITEMS_ID_PHOTOS: {
                mSyncToNetwork |= !callerIsSyncAdapter;
                String streamItemId = uri.getPathSegments().get(1);
                String selectionWithId =
                        (StreamItemPhotos.STREAM_ITEM_ID + "=" + streamItemId + " ")
                                + (selection == null ? "" : " AND (" + selection + ")");
                return deleteStreamItemPhotos(uri, new ContentValues(),
                        selectionWithId, selectionArgs);
            }

            case STREAM_ITEMS_ID_PHOTOS_ID: {
                mSyncToNetwork |= !callerIsSyncAdapter;
                String streamItemId = uri.getPathSegments().get(1);
                String streamItemPhotoId = uri.getPathSegments().get(3);
                return deleteStreamItemPhotos(uri, new ContentValues(),
                        StreamItemPhotosColumns.CONCRETE_ID + "=? AND "
                                + StreamItemPhotos.STREAM_ITEM_ID + "=?",
                        new String[]{streamItemPhotoId, streamItemId});
            }
            
            //Gionee:huangzy 20121019 add for CR00715333 start
            case CONTACTS_FREQUENT_CLEAR: {
        		return deleteFrequentContactedRecord(selection, selectionArgs);
            }
            //Gionee:huangzy 20121019 add for CR00715333 end
            
		// reject begin
		case BLACKS:
			Log.d(TAG, "BLACKS selection = " + selection);
			if (selection.startsWith("_ID=?")) {
				if (selectionArgs != null) {
				    for (String id : selectionArgs) {
				    	String number = queryNumberForId(id);
				    	// update data
						updateDataForBlack(number, 0, false);
						// update calls
						updateCallsForBlack(number, null, false);
						
						updateRejectMms(3, 0, null, number, 0);
				    }
				}
			} else if (selection.startsWith("_ID in")) {
			    String ids = selection.replaceAll("_ID in", "");
			    ids = ids.replaceAll(" ", "");
			    ids = ids.substring(1, ids.length() - 1);
			    Log.d(TAG, "ids = " + ids);
			    
			    String[] idsArr = ids.split(",");
			    for (String id : idsArr) {
			    	String number = queryNumberForId(id);
			    	// update data
					updateDataForBlack(number, 0, false);
					// update calls
					updateCallsForBlack(number, null, false);
					
					updateRejectMms(3, 0, null, number, 0);
			    }
			} else if (selection.startsWith("_ID=")) {
				String id = selection.replaceAll("_ID=", "");
				id = id.replaceAll(" ", "");
				Log.d(TAG, "id = " + id);
				
				String number = queryNumberForId(id);
		    	// update data
				updateDataForBlack(number, 0, false);
				// update calls
				updateCallsForBlack(number, null, false);
				
				updateRejectMms(3, 0, null, number, 0);
			}
			
			return mActiveDb.get().delete("black", selection, selectionArgs);
		case BLACK:
			// 下面的方法用于从URI中解析出id，对这样的路径content://hb.android.teacherProvider/teacher/10
			// 进行解析，返回值为10
			long blackid = ContentUris.parseId(uri);
			where = "_ID=" + blackid; // 删除指定id的记录
			where += !TextUtils.isEmpty(selection) ? " and (" + selection + ")"
					: ""; // 把其它条件附加上
			return mActiveDb.get().delete("black", where, selectionArgs);
		case MARKS:
			return mActiveDb.get().delete("mark", selection, selectionArgs);
		case MARK:
			// 下面的方法用于从URI中解析出id，对这样的路径content://hb.android.teacherProvider/teacher/10
			// 进行解析，返回值为10
			long markid = ContentUris.parseId(uri);
			where = "_ID=" + markid; // 删除指定id的记录
			where += !TextUtils.isEmpty(selection) ? " and (" + selection + ")"
					: ""; // 把其它条件附加上
			return mActiveDb.get().delete("mark", where, selectionArgs);
		// reject end
		//contacts sync begin
		case SYNC_ACCESSORY:
			System.out.println("case SYNC_ACCESSORY delete:");
			return 0;
		case SYNC_UP:
			System.out.println("case SYNC_UP delete:");
			return 0;
		case SYNC_DOWN:
			System.out.println("case SYNC_DOWN delete:");
			return 0;
		case SYNCS:
			System.out.println("case SYNCS delete:");
			return 0;
		case SYNC:
			System.out.println("case SYNC delete:");
			return 0;
		case URI_CLEAN_ACCOUNT:
			System.out.println("case URI_CLEAN_ACCOUNT delete:");
			Long accountId = mDbHelper.get().getAccountIdOrNull(mDbHelper.get().getDefaultAccount());
			Cursor needClean = mActiveDb.get().query(Tables.RAW_CONTACTS, new String[]{"_id"}, "account_id="+accountId, null, null, null, null, null);
			int count = 0;
			if(needClean != null){
				count = needClean.getCount();
				ContentValues values=new ContentValues();
				while(needClean.moveToNext()){
					long needCleanId = needClean.getLong(0);
					values.clear();
					values.put("dirty", 1);
					values.putNull("sync4");
					mActiveDb.get().update(Tables.RAW_CONTACTS, values, "_id="+needCleanId, null);
//					values.clear();
//					values.putNull("data_sync4");
//					mActiveDb.get().update(Tables.DATA, values, "raw_contact_id="+needCleanId, null);
				}
				needClean.close();
			}
			int deleteCount = mActiveDb.get().delete(Tables.RAW_CONTACTS, "sync4 is null and deleted=1 and account_id="+accountId, null);
			count = count - deleteCount;
			return count;
		case URI_CLEAN_DATA:
			System.out.println("case URI_CLEAN_DATA delete:");
			accountId = mDbHelper.get().getAccountIdOrNull(mDbHelper.get().getDefaultAccount());
			count=mActiveDb.get().delete(Tables.RAW_CONTACTS, "is_privacy=0 and account_id="+accountId, null);
			return count;
		//contacts sync end

            default: {
                mSyncToNetwork = true;
                return mLegacyApiSupport.delete(uri, selection, selectionArgs);
            }
        }
    }

    public int deleteGroup(Uri uri, long groupId, boolean callerIsSyncAdapter) {
        mGroupIdCache.clear();
        final long groupMembershipMimetypeId = mDbHelper.get()
                .getMimeTypeId(GroupMembership.CONTENT_ITEM_TYPE);
        mActiveDb.get().delete(Tables.DATA, DataColumns.MIMETYPE_ID + "="
                + groupMembershipMimetypeId + " AND " + GroupMembership.GROUP_ROW_ID + "="
                + groupId, null);

        try {
            if (callerIsSyncAdapter) {
                return mActiveDb.get().delete(Tables.GROUPS, Groups._ID + "=" + groupId, null);
            } else {
                mValues.clear();
                mValues.put(Groups.DELETED, 1);
                mValues.put(Groups.DIRTY, 1);
                //Gionee:huangzy 20121128 add for CR00736966 start
                ContactsDatabaseHelper.writeGnVersion(getContext(), mValues);
                //Gionee:huangzy 20121128 add for CR00736966 end
                return mActiveDb.get().update(Tables.GROUPS, mValues, Groups._ID + "=" + groupId,
                        null);
            }
        } finally {
            mVisibleTouched = true;
        }
    }

    private int deleteSettings(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "deleteSettings() "+ "****");
        final int count = mActiveDb.get().delete(Tables.SETTINGS, selection, selectionArgs);
        mVisibleTouched = true;
        return count;
    }

    private int deleteContact(long contactId, boolean callerIsSyncAdapter) {
        mSelectionArgs1[0] = Long.toString(contactId);
        Cursor c = mActiveDb.get().query(Tables.RAW_CONTACTS, new String[]{RawContacts._ID},
                RawContacts.CONTACT_ID + "=?", mSelectionArgs1,
                null, null, null);
        try {
            while (c.moveToNext()) {
                long rawContactId = c.getLong(0);
                markRawContactAsDeleted(rawContactId, callerIsSyncAdapter);
            }
        } finally {
            c.close();
        }

        mProviderStatusUpdateNeeded = true;
        
        int result = ContactsTableUtil.deleteContact(mActiveDb.get(), contactId);
        scheduleBackgroundTask(BACKGROUND_TASK_CLEAN_DELETE_LOG);
        return result;
    }

    public int deleteRawContact(long rawContactId, long contactId, boolean callerIsSyncAdapter) {
        mAggregator.get().invalidateAggregationExceptionCache();
        mProviderStatusUpdateNeeded = true;

        // Find and delete stream items associated with the raw contact.
        Cursor c = mActiveDb.get().query(Tables.STREAM_ITEMS,
                new String[]{StreamItems._ID},
                StreamItems.RAW_CONTACT_ID + "=?", new String[]{String.valueOf(rawContactId)},
                null, null, null);
        try {
            while (c.moveToNext()) {
                deleteStreamItem(c.getLong(0));
            }
        } finally {
            c.close();
        }

        if (callerIsSyncAdapter || rawContactIsLocal(rawContactId)) {
            ContactsTableUtil.deleteContactIfSingleton(mDbHelper.get().getWritableDatabase(), rawContactId);
            mActiveDb.get().delete(Tables.PRESENCE,
                    PresenceColumns.RAW_CONTACT_ID + "=" + rawContactId, null);
            int count = mActiveDb.get().delete(Tables.RAW_CONTACTS,
                    RawContacts._ID + "=" + rawContactId, null);
            mAggregator.get().updateAggregateData(mTransactionContext.get(), contactId);
            
            //Gionee:huangzy 20130220 add for CR00769943 start
        	if (ContactsProvidersApplication.sIsGnDialerSearchSupport) {
        		updateGnDialerSearchDataForDelete(rawContactId);
        	}
        	//Gionee:huangzy 20130220 add for CR00769943 end
            return count;
        } else {
            ContactsTableUtil.deleteContactIfSingleton(mDbHelper.get().getWritableDatabase(), rawContactId);
            return markRawContactAsDeleted(rawContactId, callerIsSyncAdapter);
        }
    }

    /**
     * Returns whether the given raw contact ID is local (i.e. has no account associated with it).
     */
//    private boolean rawContactIsLocal(long rawContactId) {
//        Cursor c = mActiveDb.get().query(Tables.RAW_CONTACTS,
//                new String[] {
//                        RawContacts.ACCOUNT_NAME,
//                        RawContacts.ACCOUNT_TYPE,
//                        RawContacts.DATA_SET
//                },
//                RawContacts._ID + "=?",
//                new String[] {String.valueOf(rawContactId)}, null, null, null);
//        try {
//            return c.moveToFirst() && c.isNull(0) && c.isNull(1) && c.isNull(2);
//        } finally {
//            c.close();
//        }
//    }
    private boolean rawContactIsLocal(long rawContactId) {
        final SQLiteDatabase db = mDbHelper.get().getReadableDatabase();
        Cursor c = db.query(Tables.RAW_CONTACTS, ContactsDatabaseHelper.Projections.LITERAL_ONE,
                RawContactsColumns.CONCRETE_ID + "=? AND " +
                RawContactsColumns.ACCOUNT_ID + "=" + Clauses.LOCAL_ACCOUNT_ID,
                new String[] {String.valueOf(rawContactId)}, null, null, null);
        try {
            return c.getCount() > 0;
        } finally {
            c.close();
        }
    }

    private int deleteStatusUpdates(String selection, String[] selectionArgs) {
      // delete from both tables: presence and status_updates
      // TODO should account type/name be appended to the where clause?
      if (VERBOSE_LOGGING) {
          Log.v(TAG, "deleting data from status_updates for " + selection);
      }
      mActiveDb.get().delete(Tables.STATUS_UPDATES, getWhereClauseForStatusUpdatesTable(selection),
          selectionArgs);
      return mActiveDb.get().delete(Tables.PRESENCE, selection, selectionArgs);
    }

    private int deleteStreamItems(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // First query for the stream items to be deleted, and check that they belong
        // to the account.
        Account account = resolveAccount(uri, values);
        List<Long> streamItemIds = enforceModifyingAccountForStreamItems(
                account, selection, selectionArgs);

        // If no security exception has been thrown, we're fine to delete.
        for (long streamItemId : streamItemIds) {
            deleteStreamItem(streamItemId);
        }

        mVisibleTouched = true;
        return streamItemIds.size();
    }

    private int deleteStreamItem(long streamItemId) {
        // Note that this does not enforce the modifying account.
        deleteStreamItemPhotos(streamItemId);
        return mActiveDb.get().delete(Tables.STREAM_ITEMS, StreamItems._ID + "=?",
                new String[]{String.valueOf(streamItemId)});
    }

    private int deleteStreamItemPhotos(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // First query for the stream item photos to be deleted, and check that they
        // belong to the account.
        Account account = resolveAccount(uri, values);
        enforceModifyingAccountForStreamItemPhotos(account, selection, selectionArgs);

        // If no security exception has been thrown, we're fine to delete.
        return mActiveDb.get().delete(Tables.STREAM_ITEM_PHOTOS, selection, selectionArgs);
    }

    private int deleteStreamItemPhotos(long streamItemId) {
        // Note that this does not enforce the modifying account.
        return mActiveDb.get().delete(Tables.STREAM_ITEM_PHOTOS,
                StreamItemPhotos.STREAM_ITEM_ID + "=?",
                new String[]{String.valueOf(streamItemId)});
    }

    private int markRawContactAsDeleted(long rawContactId, boolean callerIsSyncAdapter) {
        mSyncToNetwork = true;

        mValues.clear();
        mValues.put(RawContacts.DELETED, 1);
        mValues.put(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_DISABLED);
        mValues.put(RawContactsColumns.AGGREGATION_NEEDED, 1);
        mValues.putNull(RawContacts.CONTACT_ID);
        mValues.put(RawContacts.DIRTY, 1);
        
        /*
         * Bug Fix by Mediatek Begin.
         *   Original Android's code:
         *     return updateRawContact(rawContactId, mValues, callerIsSyncAdapter);
         *   
         *   Descriptions: 
         *    It is to slow to delete/insert/update contacts.
         */
        int updateCount = updateRawContact(rawContactId, mValues, callerIsSyncAdapter);
        
        //Gionee:huangzy 20130220 add for CR00769943 start
    	if (ContactsProvidersApplication.sIsGnDialerSearchSupport) {
    		updateGnDialerSearchDataForDelete(rawContactId);
    	}
    	//Gionee:huangzy 20130220 add for CR00769943 end
        
        //Gionee:huangzy 20121128 add for CR00736966 start
        if (updateCount > 0 && ContactsProvidersApplication.sIsGnSyncSupport) {
        	mTransactionContext.get().rawContactDeleted(rawContactId);
        }
        //Gionee:huangzy 20121128 add for CR00736966 end
        
        return updateCount;
        /*
         * Bug Fix by Mediatek End.
         */
    }

    private int deleteDataUsage() {
        final SQLiteDatabase db = mDbHelper.get().getWritableDatabase();
        db.execSQL("UPDATE " + Tables.RAW_CONTACTS + " SET " +
                Contacts.TIMES_CONTACTED + "=0," +
                Contacts.LAST_TIME_CONTACTED + "=NULL"
                );
        db.execSQL("UPDATE " + Tables.CONTACTS + " SET " +
                Contacts.TIMES_CONTACTED + "=0," +
                Contacts.LAST_TIME_CONTACTED + "=NULL"
                );
        db.delete(Tables.DATA_USAGE_STAT, null, null);

        return 1;
    }

    @Override
    protected int updateInTransaction(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        if (VERBOSE_LOGGING) {
            Log.v(TAG, "updateInTransaction: " + uri);
        }

        // Default active DB to the contacts DB if none has been set.
        if (mActiveDb.get() == null) {
            mActiveDb.set(mContactsHelper.getWritableDatabase());
        }
        
        // aurora wangth 20150109 add for begin
        if (values != null && values.containsKey(RawContacts.AGGREGATION_MODE)) {
            values.remove(RawContacts.AGGREGATION_MODE);
            values.put(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_DISABLED);
        }
        // aurora wangth 20150109 add for end

        int count = 0;
        String where="";

        final int match = sUriMatcher.match(uri);
        if (match == SYNCSTATE_ID && selection == null) {
            long rowId = ContentUris.parseId(uri);
            Object data = values.get(GnContactsContract.SyncState.DATA);
            mTransactionContext.get().syncStateUpdated(rowId, data);
            return 1;
        }
        flushTransactionalChanges();
        final boolean callerIsSyncAdapter =
                readBooleanQueryParameter(uri, GnContactsContract.CALLER_IS_SYNCADAPTER, false);
        switch(match) {
            case SYNCSTATE:
            case PROFILE_SYNCSTATE:
                return mDbHelper.get().getSyncState().update(mActiveDb.get(), values,
                        appendAccountToSelection(uri, selection), selectionArgs);

            case SYNCSTATE_ID: {
                selection = appendAccountToSelection(uri, selection);
                String selectionWithId =
                        (SyncStateContract.Columns._ID + "=" + ContentUris.parseId(uri) + " ")
                        + (selection == null ? "" : " AND (" + selection + ")");
                return mDbHelper.get().getSyncState().update(mActiveDb.get(), values,
                        selectionWithId, selectionArgs);
            }

            case PROFILE_SYNCSTATE_ID: {
                selection = appendAccountToSelection(uri, selection);
                String selectionWithId =
                        (SyncStateContract.Columns._ID + "=" + ContentUris.parseId(uri) + " ")
                        + (selection == null ? "" : " AND (" + selection + ")");
                return mProfileHelper.getSyncState().update(mActiveDb.get(), values,
                        selectionWithId, selectionArgs);
            }

            case CONTACTS:
            case PROFILE: {
                count = updateContactOptions(values, selection, selectionArgs, callerIsSyncAdapter);
                break;
            }

            case CONTACTS_ID: {
                count = updateContactOptions(ContentUris.parseId(uri), values, callerIsSyncAdapter);
                break;
            }

            case CONTACTS_LOOKUP:
            case CONTACTS_LOOKUP_ID: {
                final List<String> pathSegments = uri.getPathSegments();
                final int segmentCount = pathSegments.size();
                if (segmentCount < 3) {
                    throw new IllegalArgumentException(mDbHelper.get().exceptionMessage(
                            "Missing a lookup key", uri));
                }
                
                // The following lines are provided and maintained by Mediatek inc.
                if (segmentCount > 3) {
                    final String contactId = uri.getLastPathSegment();
                    Log.i(TAG, "[updateInTransaction]contactId:" + Long.parseLong(contactId));
                    Log.i(TAG, "[updateInTransaction]callerIsSyncAdapter:" + callerIsSyncAdapter);
                    count = updateContactOptions(Long.parseLong(contactId), values,
                            callerIsSyncAdapter);
                    break;
                }
                // The previous lines are provided and maintained by Mediatek inc.
                
                final String lookupKey = pathSegments.get(2);
                final long contactId = lookupContactIdByLookupKey(mActiveDb.get(), lookupKey);
                count = updateContactOptions(contactId, values, callerIsSyncAdapter);
                break;
            }

            case RAW_CONTACTS_DATA:
            case PROFILE_RAW_CONTACTS_ID_DATA: {
                int segment = match == RAW_CONTACTS_DATA ? 1 : 2;
                final String rawContactId = uri.getPathSegments().get(segment);
                String selectionWithId = (Data.RAW_CONTACT_ID + "=" + rawContactId + " ")
                    + (selection == null ? "" : " AND " + selection);

                count = updateData(uri, values, selectionWithId, selectionArgs, callerIsSyncAdapter);

                break;
            }

            case DATA:
            case PROFILE_DATA: {
                count = updateData(uri, values, appendAccountToSelection(uri, selection),
                        selectionArgs, callerIsSyncAdapter);
                if (count > 0) {
                    mSyncToNetwork |= !callerIsSyncAdapter;
                }
                break;
            }

            case DATA_ID:
            case PHONES_ID:
            case EMAILS_ID:
            // Gionee lihuafang add for phone error begin
            case CALLABLES_ID:
            // Gionee lihuafang add for phone error end
            case POSTALS_ID: {
                count = updateData(uri, values, selection, selectionArgs, callerIsSyncAdapter);
                if (count > 0) {
                    mSyncToNetwork |= !callerIsSyncAdapter;
                }
                break;
            }

            case RAW_CONTACTS:
            case PROFILE_RAW_CONTACTS: {
                selection = appendAccountIdToSelection(uri, selection);
                count = updateRawContacts(values, selection, selectionArgs, callerIsSyncAdapter);
                break;
            }

            case RAW_CONTACTS_ID: {
                long rawContactId = ContentUris.parseId(uri);
                if (selection != null) {
                    selectionArgs = insertSelectionArg(selectionArgs, String.valueOf(rawContactId));
                    count = updateRawContacts(values, RawContacts._ID + "=?"
                                    + " AND(" + selection + ")", selectionArgs,
                            callerIsSyncAdapter);
                } else {
                    mSelectionArgs1[0] = String.valueOf(rawContactId);
                    count = updateRawContacts(values, RawContacts._ID + "=?", mSelectionArgs1,
                            callerIsSyncAdapter);
                }
                break;
            }

            case GROUPS: {
                count = updateGroups(uri, values, appendAccountIdToSelection(uri, selection),
                        selectionArgs, callerIsSyncAdapter);
                if (count > 0) {
                    mSyncToNetwork |= !callerIsSyncAdapter;
                }
                break;
            }

            case GROUPS_ID: {
                long groupId = ContentUris.parseId(uri);
                selectionArgs = insertSelectionArg(selectionArgs, String.valueOf(groupId));
                String selectionWithId = Groups._ID + "=? "
                        + (selection == null ? "" : " AND " + selection);
                count = updateGroups(uri, values, selectionWithId, selectionArgs,
                        callerIsSyncAdapter);
                if (count > 0) {
                    mSyncToNetwork |= !callerIsSyncAdapter;
                }
                break;
            }

            case AGGREGATION_EXCEPTIONS: {
                count = updateAggregationException(mActiveDb.get(), values);
                break;
            }

            case SETTINGS: {
                count = updateSettings(uri, values, appendAccountToSelection(uri, selection),
                        selectionArgs);
                mSyncToNetwork |= !callerIsSyncAdapter;
                break;
            }

            case STATUS_UPDATES:
            case PROFILE_STATUS_UPDATES: {
                count = updateStatusUpdate(uri, values, selection, selectionArgs);
                break;
            }

            case STREAM_ITEMS: {
                count = updateStreamItems(uri, values, selection, selectionArgs);
                break;
            }

            case STREAM_ITEMS_ID: {
                count = updateStreamItems(uri, values, StreamItems._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            }

            case RAW_CONTACTS_ID_STREAM_ITEMS_ID: {
                String rawContactId = uri.getPathSegments().get(1);
                String streamItemId = uri.getLastPathSegment();
                count = updateStreamItems(uri, values,
                        StreamItems.RAW_CONTACT_ID + "=? AND " + StreamItems._ID + "=?",
                        new String[]{rawContactId, streamItemId});
                break;
            }

            case STREAM_ITEMS_PHOTOS: {
                count = updateStreamItemPhotos(uri, values, selection, selectionArgs);
                break;
            }

            case STREAM_ITEMS_ID_PHOTOS: {
                String streamItemId = uri.getPathSegments().get(1);
                count = updateStreamItemPhotos(uri, values,
                        StreamItemPhotos.STREAM_ITEM_ID + "=?", new String[]{streamItemId});
                break;
            }

            case STREAM_ITEMS_ID_PHOTOS_ID: {
                String streamItemId = uri.getPathSegments().get(1);
                String streamItemPhotoId = uri.getPathSegments().get(3);
                count = updateStreamItemPhotos(uri, values,
                        StreamItemPhotosColumns.CONCRETE_ID + "=? AND " +
                                StreamItemPhotosColumns.CONCRETE_STREAM_ITEM_ID + "=?",
                        new String[]{streamItemPhotoId, streamItemId});
                break;
            }

            case DIRECTORIES: {
                mContactDirectoryManager.scanPackagesByUid(Binder.getCallingUid());
                count = 1;
                break;
            }

            case DATA_USAGE_FEEDBACK_ID: {
                if (handleDataUsageFeedback(uri)) {
                    count = 1;
                } else {
                    count = 0;
                }
                break;
            }

            case PINNED_POSITION_UPDATE: {
                final boolean forceStarWhenPinning = uri.getBooleanQueryParameter(
                		ContactsContract.PinnedPositions.STAR_WHEN_PINNING, false);
                count = handlePinningUpdate(values, forceStarWhenPinning);
                break;
            }
            
		// reject begin
		case BLACKS:
			count = mActiveDb.get().update("black", values, selection,
					selectionArgs);
			
			if (count > 0) {
				boolean needNotif = false;
				int isBlack = 0;
				if (values.containsKey("isblack")) {
					isBlack = values.getAsInteger("isblack");
					needNotif = true;
				}
				
				String number = null;
				if (values.containsKey("number")) {
					number = values.getAsString("number");
				}
				
				int reject = 0;
				String name = null;
				if (values.containsKey("black_name")) {
					name = values.getAsString("black_name");
				}
				
				if (values.containsKey("reject")) {
					reject = values.getAsInteger("reject");
				} else {
					Cursor cursor = mActiveDb.get().query("black",
							new String[] {"reject", "black_name"},
							"number='" + number + "'",
							null, null, null, null);
					if (cursor != null) {
						if (cursor.moveToFirst()) {
							try {
								reject = cursor.getInt(0);
								if (name == null) {
									name = cursor.getString(1);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						cursor.close();
					}
				}
				
				Log.d(TAG, "number = " + number + "  rejected = " + reject + "  isblack = " + isBlack + "  name = " + name);
				if (!needNotif) {
					break;
				}
				
		        updateRejectMms(2, isBlack, null, number, reject);
				
				if (isBlack > 0) {
					updateDataForBlack(number, 1, true);
					if (reject == 1 || reject == 3) {
						updateCallsForBlack(number, name, true);
					} else {
						updateCallsForBlack(number, null, false);
					}
				} else {
					updateDataForBlack(number, 0, false);
					
					String black_name = null;
					Log.d(TAG, "isBlack = " + isBlack + "  number = " + number);
					if (isBlack == 0) {
						updateCallsForBlack(number, black_name, false);
					} else {
//						updateCallsForBlack(number); // only update black_name
					}
					
					mActiveDb.get().delete("black", "number='" + number + "' and isblack<1", null);
				}
			}
			
			break;
		case BLACK:
			// 下面的方法用于从URI中解析出id，对这样的路径content://com.ljq.provider.personprovider/person/10
			// 进行解析，返回值为10
			long blackid = ContentUris.parseId(uri);
			where = "_ID=" + blackid;// 获取指定id的记录
			where += !TextUtils.isEmpty(selection) ? " and (" + selection + ")"
					: "";// 把其它条件附加上
			count = mActiveDb.get().update("black", values, where,
					selectionArgs);
			
			break;
		case MARKS:
			count = mActiveDb.get().update("mark", values, selection,
					selectionArgs);
			break;
		case MARK:
			// 下面的方法用于从URI中解析出id，对这样的路径content://com.ljq.provider.personprovider/person/10
			// 进行解析，返回值为10
			long markid = ContentUris.parseId(uri);
			where = "_ID=" + markid;// 获取指定id的记录
			where += !TextUtils.isEmpty(selection) ? " and (" + selection + ")"
					: "";// 把其它条件附加上
			count = mActiveDb.get()
					.update("mark", values, where, selectionArgs);
			break;
		// reject end
			 //contacts sync begin
		case SYNC_ACCESSORY:
			System.out.println("case SYNC_ACCESSORY update:");
			
			break;
		case SYNC_DOWN:
			    System.out.println("case SYNC_DOWN update:");
	          
			break;
		case SYNC_UP:
			System.out.println("case SYNC_UP update:");
			
			break;
		case SYNC_UP_RESULT:
			System.out.println("case SYNC_UP_RESULT update:");
			
			break;
		case SYNCS:
			System.out.println("case SYNCS update:");
			
			break;
		case SYNC:
			System.out.println("case SYNC update:");
			break;
			
		//contacts sync end

            default: {
                mSyncToNetwork = true;
                return mLegacyApiSupport.update(uri, values, selection, selectionArgs);
            }
        }

        return count;
    }

    private int updateStatusUpdate(Uri uri, ContentValues values, String selection,
        String[] selectionArgs) {
        // update status_updates table, if status is provided
        // TODO should account type/name be appended to the where clause?
        int updateCount = 0;
        ContentValues settableValues = getSettableColumnsForStatusUpdatesTable(values);
        if (settableValues.size() > 0) {
          updateCount = mActiveDb.get().update(Tables.STATUS_UPDATES,
                    settableValues,
                    getWhereClauseForStatusUpdatesTable(selection),
                    selectionArgs);
        }

        // now update the Presence table
        settableValues = getSettableColumnsForPresenceTable(values);
        if (settableValues.size() > 0) {
          updateCount = mActiveDb.get().update(Tables.PRESENCE, settableValues,
                    selection, selectionArgs);
        }
        // TODO updateCount is not entirely a valid count of updated rows because 2 tables could
        // potentially get updated in this method.
        return updateCount;
    }

    private int updateStreamItems(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // Stream items can't be moved to a new raw contact.
        values.remove(StreamItems.RAW_CONTACT_ID);

        // Don't attempt to update accounts params - they don't exist in the stream items table.
        values.remove(RawContacts.ACCOUNT_NAME);
        values.remove(RawContacts.ACCOUNT_TYPE);

        final SQLiteDatabase db = mDbHelper.get().getWritableDatabase();

        // If there's been no exception, the update should be fine.
        return db.update(Tables.STREAM_ITEMS, values, selection, selectionArgs);
    }

    private int updateStreamItemPhotos(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // Stream item photos can't be moved to a new stream item.
        values.remove(StreamItemPhotos.STREAM_ITEM_ID);

        // Check that the stream item photos being updated belong to the account.
        Account account = resolveAccount(uri, values);
        enforceModifyingAccountForStreamItemPhotos(account, selection, selectionArgs);

        // Don't attempt to update accounts params - they don't exist in the stream item
        // photos table.
        values.remove(RawContacts.ACCOUNT_NAME);
        values.remove(RawContacts.ACCOUNT_TYPE);

        // Process the photo (since we're updating, it's valid for the photo to not be present).
        if (processStreamItemPhoto(values, true)) {
            // If there's been no exception, the update should be fine.
            return mActiveDb.get().update(Tables.STREAM_ITEM_PHOTOS, values, selection,
                    selectionArgs);
        }
        return 0;
    }

    /**
     * Build a where clause to select the rows to be updated in status_updates table.
     */
    private String getWhereClauseForStatusUpdatesTable(String selection) {
        mSb.setLength(0);
        mSb.append(WHERE_CLAUSE_FOR_STATUS_UPDATES_TABLE);
        mSb.append(selection);
        mSb.append(")");
        return mSb.toString();
    }

    private ContentValues getSettableColumnsForStatusUpdatesTable(ContentValues values) {
        mValues.clear();
        ContactsDatabaseHelper.copyStringValue(mValues, StatusUpdates.STATUS, values,
            StatusUpdates.STATUS);
        ContactsDatabaseHelper.copyStringValue(mValues, StatusUpdates.STATUS_TIMESTAMP, values,
            StatusUpdates.STATUS_TIMESTAMP);
        ContactsDatabaseHelper.copyStringValue(mValues, StatusUpdates.STATUS_RES_PACKAGE, values,
            StatusUpdates.STATUS_RES_PACKAGE);
        ContactsDatabaseHelper.copyStringValue(mValues, StatusUpdates.STATUS_LABEL, values,
            StatusUpdates.STATUS_LABEL);
        ContactsDatabaseHelper.copyStringValue(mValues, StatusUpdates.STATUS_ICON, values,
            StatusUpdates.STATUS_ICON);
        return mValues;
    }

    private ContentValues getSettableColumnsForPresenceTable(ContentValues values) {
        mValues.clear();
        ContactsDatabaseHelper.copyStringValue(mValues, StatusUpdates.PRESENCE, values,
            StatusUpdates.PRESENCE);
        ContactsDatabaseHelper.copyStringValue(mValues, StatusUpdates.CHAT_CAPABILITY, values,
                StatusUpdates.CHAT_CAPABILITY);
        return mValues;
    }

    private int getIntValue(ContentValues values, String key, int defaultValue) {
        final Integer value = values.getAsInteger(key);
        return value != null ? value : defaultValue;
    }

    private boolean flagExists(ContentValues values, String key) {
        return values.getAsInteger(key) != null;
    }

    private boolean flagIsSet(ContentValues values, String key) {
        return getIntValue(values, key, 0) != 0;
    }

    private boolean flagIsClear(ContentValues values, String key) {
        return getIntValue(values, key, 1) == 0;
    }

    private interface GroupAccountQuery {
        String TABLE = Views.GROUPS;

        String[] COLUMNS = new String[] {
                Groups._ID,
                Groups.ACCOUNT_TYPE,
                Groups.ACCOUNT_NAME,
                Groups.DATA_SET,
        };
        int ID = 0;
        int ACCOUNT_TYPE = 1;
        int ACCOUNT_NAME = 2;
        int DATA_SET = 3;
    }

    private int updateGroups(Uri uri, ContentValues originalValues, String selectionWithId,
            String[] selectionArgs, boolean callerIsSyncAdapter) {
        mGroupIdCache.clear();

        final SQLiteDatabase db = mDbHelper.get().getWritableDatabase();
        final ContactsDatabaseHelper dbHelper = mDbHelper.get();

        final ContentValues updatedValues = new ContentValues();
        updatedValues.putAll(originalValues);

        if (!callerIsSyncAdapter && !updatedValues.containsKey(Groups.DIRTY)) {
            updatedValues.put(Groups.DIRTY, 1);
        }
        if (updatedValues.containsKey(Groups.GROUP_VISIBLE)) {
            mVisibleTouched = true;
        }

        // Prepare for account change
        final boolean isAccountNameChanging = updatedValues.containsKey(Groups.ACCOUNT_NAME);
        final boolean isAccountTypeChanging = updatedValues.containsKey(Groups.ACCOUNT_TYPE);
        final boolean isDataSetChanging = updatedValues.containsKey(Groups.DATA_SET);
        final boolean isAccountChanging = isAccountNameChanging || isAccountTypeChanging
                || isDataSetChanging;
        final String updatedAccountName = updatedValues.getAsString(Groups.ACCOUNT_NAME);
        final String updatedAccountType = updatedValues.getAsString(Groups.ACCOUNT_TYPE);
        final String updatedDataSet = updatedValues.getAsString(Groups.DATA_SET);

        updatedValues.remove(Groups.ACCOUNT_NAME);
        updatedValues.remove(Groups.ACCOUNT_TYPE);
        updatedValues.remove(Groups.DATA_SET);

        // We later call requestSync() on all affected accounts.
        final Set<Account> affectedAccounts = Sets.newHashSet();

        //Gionee:huangzy 20121128 add for CR00736966 start
        ContactsDatabaseHelper.writeGnVersion(getContext(), originalValues);
        //Gionee:huangzy 20121128 add for CR00736966 end

        // Look for all affected rows, and change them row by row.
        final Cursor c = db.query(GroupAccountQuery.TABLE, GroupAccountQuery.COLUMNS,
                selectionWithId, selectionArgs, null, null, null);
        int returnCount = 0;
        try {
            c.moveToPosition(-1);
            while (c.moveToNext()) {
                final long groupId = c.getLong(GroupAccountQuery.ID);

                mSelectionArgs1[0] = Long.toString(groupId);

                final String accountName = isAccountNameChanging
                        ? updatedAccountName : c.getString(GroupAccountQuery.ACCOUNT_NAME);
                final String accountType = isAccountTypeChanging
                        ? updatedAccountType : c.getString(GroupAccountQuery.ACCOUNT_TYPE);
                final String dataSet = isDataSetChanging
                        ? updatedDataSet : c.getString(GroupAccountQuery.DATA_SET);

                if (isAccountChanging) {
                    final long accountId = dbHelper.getOrCreateAccountIdInTransaction(
                            AccountWithDataSet.get(accountName, accountType, dataSet));
                    updatedValues.put(GroupsColumns.ACCOUNT_ID, accountId);
                }

                // Finally do the actual update.
                final int count = db.update(Tables.GROUPS, updatedValues,
                        GroupsColumns.CONCRETE_ID + "=?", mSelectionArgs1);

                if ((count > 0)
                        && !TextUtils.isEmpty(accountName)
                        && !TextUtils.isEmpty(accountType)) {
                    affectedAccounts.add(new Account(accountName, accountType));
                }

                returnCount += count;
            }
        } finally {
            c.close();
        }

        // TODO: This will not work for groups that have a data set specified, since the content
        // resolver will not be able to request a sync for the right source (unless it is updated
        // to key off account with data set).
        // i.e. requestSync only takes Account, not AccountWithDataSet.
        if (flagIsSet(updatedValues, Groups.SHOULD_SYNC)) {
            for (Account account : affectedAccounts) {
                ContentResolver.requestSync(account, ContactsContract.AUTHORITY, new Bundle());
            }
        }
        return returnCount;
    }

    private int updateSettings(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
            Log.d(TAG, "updateSettings(),  "+  "****");
        final int count = mActiveDb.get().update(Tables.SETTINGS, values, selection, selectionArgs);
        if (values.containsKey(Settings.UNGROUPED_VISIBLE)) {
            mVisibleTouched = true;
        }
        
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private int updateRawContacts(ContentValues values, String selection, String[] selectionArgs,
            boolean callerIsSyncAdapter) {
        if (values.containsKey(RawContacts.CONTACT_ID)) {
            throw new IllegalArgumentException(RawContacts.CONTACT_ID + " should not be included " +
                    "in content values. Contact IDs are assigned automatically");
        }

        if (!callerIsSyncAdapter) {
            selection = DatabaseUtils.concatenateWhere(selection,
                    RawContacts.RAW_CONTACT_IS_READ_ONLY + "=0");
        }

        int count = 0;
        Cursor cursor = mActiveDb.get().query(Views.RAW_CONTACTS,
                new String[] { RawContacts._ID }, selection,
                selectionArgs, null, null, null);
        try {
            while (cursor.moveToNext()) {
                long rawContactId = cursor.getLong(0);
                updateRawContact(rawContactId, values, callerIsSyncAdapter);
                count++;
            }
        } catch(Exception e){
        	Log.e(TAG,"updateRawContacts with selection  e = " + e.toString());
        } finally {
            cursor.close();
        }

        return count;
    }

    private int updateRawContact(long rawContactId, ContentValues values,
            boolean callerIsSyncAdapter) {
    	return updateRawContact(mDbHelper.get().getWritableDatabase(), rawContactId, values, callerIsSyncAdapter);
    }

    private int updateRawContact(SQLiteDatabase db, long rawContactId, ContentValues values,
            boolean callerIsSyncAdapter) {
        final String selection = RawContacts._ID + " = ?";
        mSelectionArgs1[0] = Long.toString(rawContactId);
        final boolean requestUndoDelete = (values.containsKey(RawContacts.DELETED)
                && values.getAsInteger(RawContacts.DELETED) == 0);

        final boolean isAccountNameChanging = values.containsKey(RawContacts.ACCOUNT_NAME);
        final boolean isAccountTypeChanging = values.containsKey(RawContacts.ACCOUNT_TYPE);
        final boolean isDataSetChanging = values.containsKey(RawContacts.DATA_SET);
        final boolean isAccountChanging = isAccountNameChanging || isAccountTypeChanging
                || isDataSetChanging;
        int previousDeleted = 0;
        long accountId = 0;
        String oldAccountType = null;
        String oldAccountName = null;
        String oldDataSet = null;
        if (requestUndoDelete || isAccountChanging) {
            Cursor cursor = db.query(RawContactsQuery.TABLE, RawContactsQuery.COLUMNS,
                    selection, mSelectionArgs1, null, null, null);
            try {
                if (cursor.moveToFirst()) {
                    previousDeleted = cursor.getInt(RawContactsQuery.DELETED);
                    accountId = cursor.getLong(RawContactsQuery.ACCOUNT_ID);
                    oldAccountType = cursor.getString(RawContactsQuery.ACCOUNT_TYPE);
                    oldAccountName = cursor.getString(RawContactsQuery.ACCOUNT_NAME);
                    oldDataSet = cursor.getString(RawContactsQuery.DATA_SET);
                }
            } finally {
                cursor.close();
            }
            if (isAccountChanging) {
                // We can't change the original ContentValues, as it'll be re-used over all
                // updateRawContact invocations in a transaction, so we need to create a new one.
                // (However we don't want to use mValues here, because mValues may be used in some
                // other methods that are called by this method.)
                final ContentValues originalValues = values;
                values = new ContentValues();
                values.clear();
                values.putAll(originalValues);

                final AccountWithDataSet newAccountWithDataSet = AccountWithDataSet.get(
                        isAccountNameChanging
                            ? values.getAsString(RawContacts.ACCOUNT_NAME) : oldAccountName,
                        isAccountTypeChanging
                            ? values.getAsString(RawContacts.ACCOUNT_TYPE) : oldAccountType,
                        isDataSetChanging
                            ? values.getAsString(RawContacts.DATA_SET) : oldDataSet
                        );
                accountId = mDbHelper.get().getOrCreateAccountIdInTransaction(newAccountWithDataSet);

                values.put(RawContactsColumns.ACCOUNT_ID, accountId);

                values.remove(RawContacts.ACCOUNT_NAME);
                values.remove(RawContacts.ACCOUNT_TYPE);
                values.remove(RawContacts.DATA_SET);
            }
        }
        if (requestUndoDelete) {
            values.put(GnContactsContract.RawContacts.AGGREGATION_MODE,
            		GnContactsContract.RawContacts.AGGREGATION_MODE_DEFAULT);
        }

        int count = mActiveDb.get().update(Tables.RAW_CONTACTS, values, selection, mSelectionArgs1);
        if (count != 0) {
            // aurora wangth 20140924 add begin
            if (ContactsProvidersApplication.sIsAuroraPrivacySupport
                    && values.containsKey(AURORA_DEFAULT_PRIVACY_COLUMN)) {
                int privacyId = values.getAsInteger(AURORA_DEFAULT_PRIVACY_COLUMN);
                mAggregator.get().updateContactsPrivacy(privacyId, rawContactId);
                mAggregator.get().updateCallsPrivacy(privacyId, rawContactId);
		        getContext().getContentResolver().notifyChange(Calls.CONTENT_URI, null, false);
            }
            // aurora wangth 20140924 add end
            
            if (values.containsKey(RawContacts.AGGREGATION_MODE)) {
                int aggregationMode = values.getAsInteger(RawContacts.AGGREGATION_MODE);

                // As per ContactsContract documentation, changing aggregation mode
                // to DEFAULT should not trigger aggregation
                if (aggregationMode != RawContacts.AGGREGATION_MODE_DEFAULT) {
                    mAggregator.get().markForAggregation(rawContactId, aggregationMode, false);
                }
            }
            if (values.containsKey(RawContacts.STARRED)) {
                if (!callerIsSyncAdapter) {
                    updateFavoritesMembership(rawContactId,
                            values.getAsLong(RawContacts.STARRED) != 0);
                }
                mAggregator.get().updateStarred(rawContactId);
                mAggregator.get().updatePinned(rawContactId);
            } else {
                // if this raw contact is being associated with an account, then update the
                // favorites group membership based on whether or not this contact is starred.
                // If it is starred, add a group membership, if one doesn't already exist
                // otherwise delete any matching group memberships.
                if (!callerIsSyncAdapter && values.containsKey(RawContacts.ACCOUNT_NAME)) {
                    boolean starred = 0 != DatabaseUtils.longForQuery(mActiveDb.get(),
                            SELECTION_STARRED_FROM_RAW_CONTACTS,
                            new String[]{Long.toString(rawContactId)});
                    updateFavoritesMembership(rawContactId, starred);
                }
            }

            // if this raw contact is being associated with an account, then add a
            // group membership to the group marked as AutoAdd, if any.
            if (!callerIsSyncAdapter && values.containsKey(RawContacts.ACCOUNT_NAME)) {
                addAutoAddMembership(rawContactId);
            }

            if (values.containsKey(RawContacts.SOURCE_ID)) {
                mAggregator.get().updateLookupKeyForRawContact(mActiveDb.get(), rawContactId);
            }
            if (values.containsKey(RawContacts.NAME_VERIFIED)) {

                // If setting NAME_VERIFIED for this raw contact, reset it for all
                // other raw contacts in the same aggregate
                if (values.getAsInteger(RawContacts.NAME_VERIFIED) != 0) {
                    mDbHelper.get().resetNameVerifiedForOtherRawContacts(rawContactId);
                }
                mAggregator.get().updateDisplayNameForRawContact(mActiveDb.get(), rawContactId);
            }
            if (requestUndoDelete && previousDeleted == 1) {
                mTransactionContext.get().rawContactInserted(rawContactId,
                		accountId);
            }
            mTransactionContext.get().markRawContactChangedOrDeletedOrInserted(rawContactId);
        }
        return count;
    }

    private int updateData(Uri uri, ContentValues values, String selection,
            String[] selectionArgs, boolean callerIsSyncAdapter) {
        mValues.clear();
        mValues.putAll(values);
        mValues.remove(Data._ID);
        mValues.remove(Data.RAW_CONTACT_ID);
        mValues.remove(Data.MIMETYPE);

        String packageName = values.getAsString(Data.RES_PACKAGE);
        if (packageName != null) {
            mValues.remove(Data.RES_PACKAGE);
            mValues.put(DataColumns.PACKAGE_ID, mDbHelper.get().getPackageId(packageName));
        }

        if (!callerIsSyncAdapter) {
            selection = DatabaseUtils.concatenateWhere(selection,
                    Data.IS_READ_ONLY + "=0");
        }

        int count = 0;
        
        selection = parseSelection(selection, true); // aurora wangth 20140930 add for privacy

        // Note that the query will return data according to the access restrictions,
        // so we don't need to worry about updating data we don't have permission to read.
        Cursor c = queryLocal(uri,
                DataRowHandler.DataUpdateQuery.COLUMNS,
                selection, selectionArgs, null, -1 /* directory ID */);
        try {
            while(c.moveToNext()) {
                // aurora <wangth> <2013-11-2> add for aurora begin
                if (ContactsProvidersApplication.sIsGnDialerSearchSupport) {
                    String mime = c.getString(DataRowHandler.DataUpdateQuery.MIMETYPE);
                    if (CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(mime)) {
                        String number = null;
                        if (mValues.containsKey(Data.DATA1)) {
                            number = mValues.getAsString(Data.DATA1);
                        }
                        
                        if (null != number) {
                            number = number.replace(" ", "");
                            number = number.replace("-", "");
                            mValues.put(Data.DATA1, number);
                        }
                        
                        if (ContactsProvidersApplication.sIsAuroraPrivacySupport && number == null) {
                            if (mValues.containsKey(AURORA_DEFAULT_PRIVACY_COLUMN)) {
                                number = c.getString(DataRowHandler.DataUpdateQuery.DATA1);
                                logs("updateDate number = " + number);
                                int is_privacy = mValues.getAsInteger(AURORA_DEFAULT_PRIVACY_COLUMN);
                                
                                boolean isMan = false;
                                if (selection != null && selection.contains("is_privacy>-10")) {
                                    isMan = true;
                                    
                                    int dataId = c.getInt(DataRowHandler.DataUpdateQuery._ID);
                                    int rawContactId = c.getInt(DataRowHandler.DataUpdateQuery.RAW_CONTACT_ID);
                                    updateNoNameCalls(number, dataId, rawContactId);
                                }
                                AuroraPrivacyUtils.sendBroadToMms(number, is_privacy, isMan);   
                                AuroraPrivacyUtils.updateCallRecordings(number, is_privacy);
                            }
                        }
                    }
                }
                //  aurora <wangth> <2013-11-2> add for aurora end
                
                count += updateData(mValues, c, callerIsSyncAdapter);
            }
        } finally {
            c.close();
        }

        return count;
    }

    private int updateData(ContentValues values, Cursor c, boolean callerIsSyncAdapter) {
        if (values.size() == 0) {
            return 0;
        }

        final String mimeType = c.getString(DataRowHandler.DataUpdateQuery.MIMETYPE);
        DataRowHandler rowHandler = getDataRowHandler(mimeType);
        boolean updated =
                rowHandler.update(mActiveDb.get(), mTransactionContext.get(), values, c,
                        callerIsSyncAdapter);
        if (Photo.CONTENT_ITEM_TYPE.equals(mimeType)) {
            scheduleBackgroundTask(BACKGROUND_TASK_CLEANUP_PHOTOS);
        }
        return updated ? 1 : 0;
    }

    private int updateContactOptions(ContentValues values, String selection,
            String[] selectionArgs, boolean callerIsSyncAdapter) {
        int count = 0;
        Cursor cursor = mActiveDb.get().query(Views.CONTACTS,
                new String[] { Contacts._ID }, selection, selectionArgs, null, null, null);
        try {
            while (cursor.moveToNext()) {
                long contactId = cursor.getLong(0);

                updateContactOptions(contactId, values, callerIsSyncAdapter);
                count++;
            }
        } finally {
            cursor.close();
        }

        return count;
    }

    private int updateContactOptions(long contactId, ContentValues values,
            boolean callerIsSyncAdapter) {

        // The following lines are provided and maintained by Mediatek inc.
        //MTK Handle the Filter column
        //TODO Maybe we should redesign the filter column
        if (values.containsKey(Contacts.FILTER)) {
            mValues.clear();
            ContactsDatabaseHelper.copyStringValue(mValues, Contacts.FILTER,
                    values, Contacts.FILTER);
            int count = mActiveDb.get().update(Tables.CONTACTS, 
                    mValues, Contacts._ID + "=" + contactId, null);
            Log.i(TAG, "[updateContactOptions]update contact filter column " + count);
            if (values.size() == 1) {
                return count;
            }
        }
        // The previous lines are provided and maintained by Mediatek inc.
        
        mValues.clear();
        ContactsDatabaseHelper.copyStringValue(mValues, RawContacts.CUSTOM_RINGTONE,
                values, Contacts.CUSTOM_RINGTONE);
        ContactsDatabaseHelper.copyLongValue(mValues, RawContacts.SEND_TO_VOICEMAIL,
                values, Contacts.SEND_TO_VOICEMAIL);
        ContactsDatabaseHelper.copyLongValue(mValues, RawContacts.LAST_TIME_CONTACTED,
                values, Contacts.LAST_TIME_CONTACTED);
        ContactsDatabaseHelper.copyLongValue(mValues, RawContacts.TIMES_CONTACTED,
                values, Contacts.TIMES_CONTACTED);
        ContactsDatabaseHelper.copyLongValue(mValues, RawContacts.STARRED,
                values, Contacts.STARRED);
        ContactsDatabaseHelper.copyLongValue(mValues, ContactsContract.RawContacts.PINNED,
                values, ContactsContract.Contacts.PINNED);
        
        // The following lines are provided and maintained by Mediatek inc.
        ContactsDatabaseHelper.copyLongValue(mValues, RawContacts.SEND_TO_VOICEMAIL_VT,
                values, Contacts.SEND_TO_VOICEMAIL_VT);     
        ContactsDatabaseHelper.copyLongValue(mValues, RawContacts.SEND_TO_VOICEMAIL_SIP,
                values, Contacts.SEND_TO_VOICEMAIL_SIP); 
        // The previous lines are provided and maintained by Mediatek inc.

        // Nothing to update - just return
        if (mValues.size() == 0) {
            return 0;
        }

        if (mValues.containsKey(RawContacts.STARRED)) {
            // Mark dirty when changing starred to trigger sync
            mValues.put(RawContacts.DIRTY, 1);
            
            //Gionee:huangzy 20121201 add for CR00736966 start
            if (ContactsProvidersApplication.sIsGnSyncSupport) {
            	mValues.put(GnSyncColumns.GN_VERSION, 
            			ContactsDatabaseHelper.toNextGnVersion(getContext()));
            }
            //Gionee:huangzy 20121201 add for CR00736966 start
        }

        mSelectionArgs1[0] = String.valueOf(contactId);
        mActiveDb.get().update(Tables.RAW_CONTACTS, mValues, RawContacts.CONTACT_ID + "=?"
                + " AND " + RawContacts.RAW_CONTACT_IS_READ_ONLY + "=0", mSelectionArgs1);

        if (mValues.containsKey(RawContacts.STARRED) && !callerIsSyncAdapter) {
            Cursor cursor = mActiveDb.get().query(Views.RAW_CONTACTS,
                    new String[] { RawContacts._ID }, RawContacts.CONTACT_ID + "=?",
                    mSelectionArgs1, null, null, null);
            try {
                while (cursor.moveToNext()) {
                    long rawContactId = cursor.getLong(0);
                    updateFavoritesMembership(rawContactId,
                            mValues.getAsLong(RawContacts.STARRED) != 0);
                }
            } finally {
                cursor.close();
            }
        }

        // Copy changeable values to prevent automatically managed fields from
        // being explicitly updated by clients.
        mValues.clear();
        ContactsDatabaseHelper.copyStringValue(mValues, RawContacts.CUSTOM_RINGTONE,
                values, Contacts.CUSTOM_RINGTONE);
        ContactsDatabaseHelper.copyLongValue(mValues, RawContacts.SEND_TO_VOICEMAIL,
                values, Contacts.SEND_TO_VOICEMAIL);
        ContactsDatabaseHelper.copyLongValue(mValues, RawContacts.LAST_TIME_CONTACTED,
                values, Contacts.LAST_TIME_CONTACTED);
        ContactsDatabaseHelper.copyLongValue(mValues, RawContacts.TIMES_CONTACTED,
                values, Contacts.TIMES_CONTACTED);
        ContactsDatabaseHelper.copyLongValue(mValues, RawContacts.STARRED,
                values, Contacts.STARRED);
        ContactsDatabaseHelper.copyLongValue(mValues, ContactsContract.RawContacts.PINNED,
                values, ContactsContract.Contacts.PINNED);
        mValues.put(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP,
                Clock.getInstance().currentTimeMillis());
        
        // The following lines are provided and maintained by Mediatek inc.
        ContactsDatabaseHelper.copyLongValue(mValues, RawContacts.SEND_TO_VOICEMAIL_VT,
                values, Contacts.SEND_TO_VOICEMAIL_VT);   
        ContactsDatabaseHelper.copyLongValue(mValues, RawContacts.SEND_TO_VOICEMAIL_SIP,
                values, Contacts.SEND_TO_VOICEMAIL_SIP); 
        // The previous lines are provided and maintained by Mediatek inc.

        int rslt = mActiveDb.get().update(Tables.CONTACTS, mValues, Contacts._ID + "=?",
                mSelectionArgs1);

        if (values.containsKey(Contacts.LAST_TIME_CONTACTED) &&
                !values.containsKey(Contacts.TIMES_CONTACTED)) {
            mActiveDb.get().execSQL(UPDATE_TIMES_CONTACTED_CONTACTS_TABLE, mSelectionArgs1);
            mActiveDb.get().execSQL(UPDATE_TIMES_CONTACTED_RAWCONTACTS_TABLE, mSelectionArgs1);
        }
        return rslt;
    }

    private int updateAggregationException(SQLiteDatabase db, ContentValues values) {
        int exceptionType = values.getAsInteger(AggregationExceptions.TYPE);
        long rcId1 = values.getAsInteger(AggregationExceptions.RAW_CONTACT_ID1);
        long rcId2 = values.getAsInteger(AggregationExceptions.RAW_CONTACT_ID2);

        long rawContactId1;
        long rawContactId2;
        if (rcId1 < rcId2) {
            rawContactId1 = rcId1;
            rawContactId2 = rcId2;
        } else {
            rawContactId2 = rcId1;
            rawContactId1 = rcId2;
        }

        if (exceptionType == AggregationExceptions.TYPE_AUTOMATIC) {
            mSelectionArgs2[0] = String.valueOf(rawContactId1);
            mSelectionArgs2[1] = String.valueOf(rawContactId2);
            db.delete(Tables.AGGREGATION_EXCEPTIONS,
                    AggregationExceptions.RAW_CONTACT_ID1 + "=? AND "
                    + AggregationExceptions.RAW_CONTACT_ID2 + "=?", mSelectionArgs2);
        } else {
            ContentValues exceptionValues = new ContentValues(3);
            exceptionValues.put(AggregationExceptions.TYPE, exceptionType);
            exceptionValues.put(AggregationExceptions.RAW_CONTACT_ID1, rawContactId1);
            exceptionValues.put(AggregationExceptions.RAW_CONTACT_ID2, rawContactId2);
            db.replace(Tables.AGGREGATION_EXCEPTIONS, AggregationExceptions._ID,
                    exceptionValues);
        }

        mAggregator.get().invalidateAggregationExceptionCache();
        mAggregator.get().markForAggregation(rawContactId1,
                RawContacts.AGGREGATION_MODE_DEFAULT, true);
        mAggregator.get().markForAggregation(rawContactId2,
                RawContacts.AGGREGATION_MODE_DEFAULT, true);

        mAggregator.get().aggregateContact(mTransactionContext.get(), db, rawContactId1);
        mAggregator.get().aggregateContact(mTransactionContext.get(), db, rawContactId2);

        // The return value is fake - we just confirm that we made a change, not count actual
        // rows changed.
        return 1;
    }

    public void onAccountsUpdated(Account[] accounts) {
        scheduleBackgroundTask(BACKGROUND_TASK_UPDATE_ACCOUNTS);
    }

    private static final String ACCOUNT_STRING_SEPARATOR_OUTER = "\u0001";
    private static final String ACCOUNT_STRING_SEPARATOR_INNER = "\u0002";

    /** return serialized version of {@code accounts} */
    @VisibleForTesting
    static String accountsToString(Set<Account> accounts) {
        final StringBuilder sb = new StringBuilder();
        for (Account account : accounts) {
            if (sb.length() > 0) {
                sb.append(ACCOUNT_STRING_SEPARATOR_OUTER);
            }
            sb.append(account.name);
            sb.append(ACCOUNT_STRING_SEPARATOR_INNER);
            sb.append(account.type);
        }
        return sb.toString();
    }

    /**
     * de-serialize string returned by {@link #accountsToString} and return it.
     * If {@code accountsString} is malformed it'll throw {@link IllegalArgumentException}.
     */
    @VisibleForTesting
    static Set<Account> stringToAccounts(String accountsString) {
        final Set<Account> ret = Sets.newHashSet();
        if (accountsString.length() == 0) return ret; // no accounts
        try {
            for (String accountString : accountsString.split(ACCOUNT_STRING_SEPARATOR_OUTER)) {
                String[] nameAndType = accountString.split(ACCOUNT_STRING_SEPARATOR_INNER);
                ret.add(new Account(nameAndType[0], nameAndType[1]));
            }
            return ret;
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Malformed string", ex);
        }
    }
    
    /**
     * @return {@code true} if the given {@code currentSystemAccounts} are different from the
     *    accounts we know, which are stored in the {@link DbProperties#KNOWN_ACCOUNTS} property.
     */
    @VisibleForTesting
    boolean haveAccountsChanged(Account[] currentSystemAccounts) {
        final ContactsDatabaseHelper dbHelper = mDbHelper.get();
        final Set<Account> knownAccountSet;
        try {
            knownAccountSet = stringToAccounts(
                    dbHelper.getProperty(ContactsDatabaseHelper.DbProperties.KNOWN_ACCOUNTS, ""));
        } catch (IllegalArgumentException e) {
            // Failed to get the last known accounts for an unknown reason.  Let's just
            // treat as if accounts have changed.
            return true;
        }
        final Set<Account> currentAccounts = Sets.newHashSet(currentSystemAccounts);
        return !knownAccountSet.equals(currentAccounts);
    }

    @VisibleForTesting
    void saveAccounts(Account[] systemAccounts) {
        final ContactsDatabaseHelper dbHelper = mDbHelper.get();
        dbHelper.setProperty(ContactsDatabaseHelper.DbProperties.KNOWN_ACCOUNTS,
                accountsToString(Sets.newHashSet(systemAccounts)));
    }

    protected boolean updateAccountsInBackground(Account[] accounts) {
        // TODO : Check the unit test.
        if (!haveAccountsChanged(accounts)) {
            return false;
        }
//        boolean accountsChanged = false;
        final ContactsDatabaseHelper dbHelper = mDbHelper.get();
        SQLiteDatabase db = mDbHelper.get().getWritableDatabase();
        mActiveDb.set(db);
        
        /*
         * Feature Fix by Mediatek Begin
         *  
         * Original Android code:
         * db.beginTransaction();
         * 
         * Description:
         * added for low memory handle, CR ALPS00071632
         */
        try {
            db.beginTransaction();
        } catch (android.database.sqlite.SQLiteDiskIOException ex) {
            Log.w(TAG, "[updateAccountsInBackground]catch SQLiteDiskIOException.");
            return false;
        } 
        /*
         * Feature Fix by Mediatek End
         */
        

        // WARNING: This method can be run in either contacts mode or profile mode.  It is
        // absolutely imperative that no calls be made inside the following try block that can
        // interact with the contacts DB.  Otherwise it is quite possible for a deadlock to occur.
        try {
//            Set<AccountWithDataSet> existingAccountsWithDataSets =
//                    findValidAccountsWithDataSets(Tables.ACCOUNTS);
//
//            // Add a row to the ACCOUNTS table (with no data set) for each new account.
//            for (Account account : accounts) {
//                AccountWithDataSet accountWithDataSet = new AccountWithDataSet(
//                        account.name, account.type, null);
//                if (!existingAccountsWithDataSets.contains(accountWithDataSet)) {
//                    accountsChanged = true;
//
//                    // Add an account entry with an empty data set to match the account.
//                    db.execSQL("INSERT INTO " + Tables.ACCOUNTS + " (" + RawContacts.ACCOUNT_NAME
//                            + ", " + RawContacts.ACCOUNT_TYPE + ", " + RawContacts.DATA_SET
//                            + ") VALUES (?, ?, ?)",
//                            new String[] {
//                                    accountWithDataSet.getAccountName(),
//                                    accountWithDataSet.getAccountType(),
//                                    accountWithDataSet.getDataSet()
//                            });
//                }
//            }
//
//            // Check each of the existing sub-accounts against the account list.  If the owning
//            // account no longer exists, the sub-account and all its data should be deleted.
//            List<AccountWithDataSet> accountsWithDataSetsToDelete =
//                    new ArrayList<AccountWithDataSet>();
//            List<Account> accountList = Arrays.asList(accounts);
//            for (AccountWithDataSet accountWithDataSet : existingAccountsWithDataSets) {
//                /*
//                 * Bug Fix by Mediatek Begin.
//                 *   Original Android's code:
//                 *   if (!accountList.contains(owningAccount)) { 
//                 *   CR ID: ALPS000111101 
//                 */
//                Account owningAccount = null;
//                String accountName = accountWithDataSet.getAccountName();
//                String accountType = accountWithDataSet.getAccountType();
//                if (!TextUtils.isEmpty(accountName) && !TextUtils.isEmpty(accountType) ) {
//                    owningAccount = new Account(
//                            accountWithDataSet.getAccountName(), accountWithDataSet.getAccountType());
//                }
//                if (owningAccount != null
//                        && !accountList.contains(owningAccount)
//                        && canDeleteAccount(accountWithDataSet)) {
//                /*
//                 * Bug Fix by Mediatek End.
//                 */      
//                    accountsWithDataSetsToDelete.add(accountWithDataSet);
//                }
//            }
            // First, remove stale rows from raw_contacts, groups, and related tables.

            // All accounts that are used in raw_contacts and/or groups.
            final Set<AccountWithDataSet> knownAccountsWithDataSets
                    = dbHelper.getAllAccountsWithDataSets();

            // Find the accounts that have been removed.
            final List<AccountWithDataSet> accountsWithDataSetsToDelete = Lists.newArrayList();
            for (AccountWithDataSet knownAccountWithDataSet : knownAccountsWithDataSets) {
                if (knownAccountWithDataSet.isLocalAccount()
                		|| ourSpecialAccount(knownAccountWithDataSet)
                        || knownAccountWithDataSet.inSystemAccounts(accounts)) {
                    continue;
                }
                accountsWithDataSetsToDelete.add(knownAccountWithDataSet);
            }

            if (!accountsWithDataSetsToDelete.isEmpty()) {
//                accountsChanged = true;
                for (AccountWithDataSet accountWithDataSet : accountsWithDataSetsToDelete) {
                    Log.d(TAG, "removing data for removed account " + accountWithDataSet);
                    String[] accountParams = new String[] {
                            accountWithDataSet.getAccountName(),
                            accountWithDataSet.getAccountType()
                    };
                    String[] accountWithDataSetParams = accountWithDataSet.getDataSet() == null
                            ? accountParams
                            : new String[] {
                                    accountWithDataSet.getAccountName(),
                                    accountWithDataSet.getAccountType(),
                                    accountWithDataSet.getDataSet()
                            };
                    final Long accountIdOrNull = mDbHelper.get().getAccountIdOrNull(accountWithDataSet);
                    final String accountId = Long.toString(accountIdOrNull);
                    final String[] accountIdParams =
                            new String[] {accountId};
                    String groupsDataSetClause = " AND " + Groups.DATA_SET
                            + (accountWithDataSet.getDataSet() == null ? " IS NULL" : " = ?");
                    String rawContactsDataSetClause = " AND " + RawContacts.DATA_SET
                            + (accountWithDataSet.getDataSet() == null ? " IS NULL" : " = ?");
                    String settingsDataSetClause = " AND " + Settings.DATA_SET
                            + (accountWithDataSet.getDataSet() == null ? " IS NULL" : " = ?");

                    db.execSQL(
                            "DELETE FROM " + Tables.GROUPS +
                            " WHERE " + GroupsColumns.ACCOUNT_ID + " = ?",
                            accountIdParams);
                    db.execSQL(
                            "DELETE FROM " + Tables.PRESENCE +
                            " WHERE " + PresenceColumns.RAW_CONTACT_ID + " IN (" +
                                    "SELECT " + RawContacts._ID +
                                    " FROM " + Tables.RAW_CONTACTS +
                                    " WHERE " + RawContactsColumns.ACCOUNT_ID + " = ?)",
                                    accountIdParams);
                    db.execSQL(
                            "DELETE FROM " + Tables.STREAM_ITEM_PHOTOS +
                            " WHERE " + StreamItemPhotos.STREAM_ITEM_ID + " IN (" +
                                    "SELECT " + StreamItems._ID +
                                    " FROM " + Tables.STREAM_ITEMS +
                                    " WHERE " + StreamItems.RAW_CONTACT_ID + " IN (" +
                                            "SELECT " + RawContacts._ID +
                                            " FROM " + Tables.RAW_CONTACTS +
                                            " WHERE " + RawContactsColumns.ACCOUNT_ID + "=?))",
                                            accountIdParams);
                    db.execSQL(
                            "DELETE FROM " + Tables.STREAM_ITEMS +
                            " WHERE " + StreamItems.RAW_CONTACT_ID + " IN (" +
                                    "SELECT " + RawContacts._ID +
                                    " FROM " + Tables.RAW_CONTACTS +
                                    " WHERE " + RawContactsColumns.ACCOUNT_ID + " = ?)",
                                    accountIdParams);// Delta api is only needed for regular contacts.
                    if (!inProfileMode()) {
                        // Contacts are deleted by a trigger on the raw_contacts table.
                        // But we also need to insert the contact into the delete log.
                        // This logic is being consolidated into the ContactsTableUtil.

                        // deleteContactIfSingleton() does not work in this case because raw
                        // contacts will be deleted in a single batch below.  Contacts with
                        // multiple raw contacts in the same account will be missed.

                        // Find all contacts that do not have raw contacts in other accounts.
                        // These should be deleted.
                        Cursor cursor = db.rawQuery(
                                "SELECT " + RawContactsColumns.CONCRETE_CONTACT_ID +
                                        " FROM " + Tables.RAW_CONTACTS +
                                        " WHERE " + RawContactsColumns.ACCOUNT_ID + " = ?1" +
                                        " AND " + RawContactsColumns.CONCRETE_CONTACT_ID +
                                        " NOT IN (" +
                                        "    SELECT " + RawContactsColumns.CONCRETE_CONTACT_ID +
                                        "    FROM " + Tables.RAW_CONTACTS +
                                        "    WHERE " + RawContactsColumns.ACCOUNT_ID + " != ?1"
                                        + ")", accountIdParams);
                        try {
                            while (cursor.moveToNext()) {
                                final long contactId = cursor.getLong(0);
                                ContactsTableUtil.deleteContact(db, contactId);
                            }
                        } finally {
                            MoreCloseables.closeQuietly(cursor);
                        }

                        // If the contact was not deleted, it's last updated timestamp needs to
                        // be refreshed since one of it's raw contacts got removed.
                        // Find all contacts that will not be deleted (i.e. contacts with
                        // raw contacts in other accounts)
                        cursor = db.rawQuery(
                                "SELECT DISTINCT " + RawContactsColumns.CONCRETE_CONTACT_ID +
                                        " FROM " + Tables.RAW_CONTACTS +
                                        " WHERE " + RawContactsColumns.ACCOUNT_ID + " = ?1" +
                                        " AND " + RawContactsColumns.CONCRETE_CONTACT_ID +
                                        " IN (" +
                                        "    SELECT " + RawContactsColumns.CONCRETE_CONTACT_ID +
                                        "    FROM " + Tables.RAW_CONTACTS +
                                        "    WHERE " + RawContactsColumns.ACCOUNT_ID + " != ?1"
                                        + ")", accountIdParams);
                        try {
                            while (cursor.moveToNext()) {
                                final long contactId = cursor.getLong(0);
                                ContactsTableUtil.updateContactLastUpdateByContactId(db,
                                        contactId);
                            }
                        } finally {
                            MoreCloseables.closeQuietly(cursor);
                        }
                    }

                    db.execSQL(
                            "DELETE FROM " + Tables.RAW_CONTACTS +
                            " WHERE " + RawContactsColumns.ACCOUNT_ID + " = ?",
                            accountIdParams);
                    db.execSQL(
                            "DELETE FROM " + Tables.ACCOUNTS +
                            " WHERE " + AccountsColumns._ID + "=?",
                            accountIdParams);
//                    db.execSQL(
//                            "DELETE FROM " + Tables.SETTINGS +
//                            " WHERE " + Settings.ACCOUNT_NAME + " = ?" +
//                            " AND " + Settings.ACCOUNT_TYPE + " = ?" +
//                            settingsDataSetClause, accountWithDataSetParams);
//                    db.execSQL(
//                            "DELETE FROM " + Tables.DIRECTORIES +
//                            " WHERE " + Directory.ACCOUNT_NAME + "=?" +
//                            " AND " + Directory.ACCOUNT_TYPE + "=?", accountParams);
//                    resetDirectoryCache();
                }

                // Find all aggregated contacts that used to contain the raw contacts
                // we have just deleted and see if they are still referencing the deleted
                // names or photos.  If so, fix up those contacts.
                HashSet<Long> orphanContactIds = Sets.newHashSet();
                Cursor cursor = db.rawQuery("SELECT " + Contacts._ID +
                        " FROM " + Tables.CONTACTS +
                        " WHERE (" + Contacts.NAME_RAW_CONTACT_ID + " NOT NULL AND " +
                                Contacts.NAME_RAW_CONTACT_ID + " NOT IN " +
                                        "(SELECT " + RawContacts._ID +
                                        " FROM " + Tables.RAW_CONTACTS + "))" +
                        " OR (" + Contacts.PHOTO_ID + " NOT NULL AND " +
                                Contacts.PHOTO_ID + " NOT IN " +
                                        "(SELECT " + Data._ID +
                                        " FROM " + Tables.DATA + "))", null);
                try {
                    while (cursor.moveToNext()) {
                        orphanContactIds.add(cursor.getLong(0));
                    }
                } finally {
                    cursor.close();
                }

                for (Long contactId : orphanContactIds) {
                    mAggregator.get().updateAggregateData(mTransactionContext.get(), contactId);
                }
                mDbHelper.get().updateAllVisible();

                // Don't bother updating the search index if we're in profile mode - there is no
                // search index for the profile DB, and updating it for the contacts DB in this case
                // makes no sense and risks a deadlock.
                if (!inProfileMode()) {
                    updateSearchIndexInTransaction();
                }
            }

//            // Now that we've done the account-based additions and subtractions from the Accounts
//            // table, check for raw contacts that have been added with a data set and add Accounts
//            // entries for those if necessary.
//            existingAccountsWithDataSets = findValidAccountsWithDataSets(Tables.ACCOUNTS);
//            Set<AccountWithDataSet> rawContactAccountsWithDataSets =
//                    findValidAccountsWithDataSets(Tables.RAW_CONTACTS);
//            rawContactAccountsWithDataSets.removeAll(existingAccountsWithDataSets);
//
//            // Any remaining raw contact sub-accounts need to be added to the Accounts table.
//            for (AccountWithDataSet accountWithDataSet : rawContactAccountsWithDataSets) {
//                String accountName = accountWithDataSet.getAccountName();
//                if (!TextUtils.isEmpty(accountName)) {
//                    accountsChanged = true;
//    
//                    // Add an account entry to match the raw contact.
//                    db.execSQL("INSERT INTO " + Tables.ACCOUNTS + " (" + RawContacts.ACCOUNT_NAME
//                            + ", " + RawContacts.ACCOUNT_TYPE + ", " + RawContacts.DATA_SET
//                            + ") VALUES (?, ?, ?)",
//                            new String[] {
//                                    accountWithDataSet.getAccountName(),
//                                    accountWithDataSet.getAccountType(),
//                                    accountWithDataSet.getDataSet()
//                            });
//                }
//            }
//
//            // The following lines are provided and maintained by Mediatek inc.
//            Cursor c = db.rawQuery("SELECT " + RawContacts.ACCOUNT_NAME + ","
//                    + RawContacts.ACCOUNT_TYPE + " FROM " + Tables.ACCOUNTS, null);
//            try {
//                Log.i(TAG, "onAccountsUpdated -c.count:"
//                        + ((c == null) ? 0 : c.getCount()));
//                if (c != null && c.getCount() == 0) {
//                    // Allow contacts without any account to be created for now.
//                    // Achieve that by inserting a fake account with both type 
//                    // and name as NULL.
//                    // This "account" should be eliminated as soon as the first
//                    // real writable account is added to the phone.
//                    db.execSQL("INSERT INTO accounts(account_name,account_type) VALUES(NULL, NULL)");
//                } else if  (c != null && c.getCount() > 1) {
//                    int emptyAccountCount = 0;
//                    while(c.moveToNext()) {
//                        if (TextUtils.isEmpty(c.getString(0))) {
//                            emptyAccountCount++;
//                        }
//                    }
//                    if (emptyAccountCount > 1) {
//                        db.execSQL("DELETE FROM accounts WHERE account_name IS NULL or account_name ='' ");
//                        db.execSQL("INSERT INTO accounts(account_name,account_type) VALUES(NULL, NULL)");
//                    }
//                }
//            } finally {
//                c.close();
//            }
//            // The previous lines are provided and maintained by Mediatek inc.
//            
//            if (accountsChanged) {
//                // TODO: Should sync state take data set into consideration?
//                mDbHelper.get().getSyncState().onAccountsChanged(db, accounts);
//            }
            
            // Second, remove stale rows from Tables.SETTINGS and Tables.DIRECTORIES
            removeStaleAccountRows(Tables.SETTINGS, Settings.ACCOUNT_NAME, Settings.ACCOUNT_TYPE,
            		accounts);
            removeStaleAccountRows(Tables.DIRECTORIES, Directory.ACCOUNT_NAME,
                    Directory.ACCOUNT_TYPE, accounts);

            // Third, remaining tasks that must be done in a transaction.
            // TODO: Should sync state take data set into consideration?
            mDbHelper.get().getSyncState().onAccountsChanged(db, accounts);

            saveAccounts(accounts);
            db.setTransactionSuccessful();
        } finally {
            /*
             * Feature Fix by Mediatek Begin
             * 
             * Original Android code: 
             *     db.endTransaction();
             * 
             * Description: 
             *     added for low memory handle, CR ALPS00287261
             */
            try {
                db.endTransaction();
            } catch (android.database.sqlite.SQLiteDiskIOException ex) {
                Log.w(TAG,"[updateAccountsInBackground]catch SQLiteDiskIOException.");
                return false;
            } catch(android.database.sqlite.SQLiteFullException ex) {
                Log.w(TAG, "[updateAccountsInBackground]catch SQLiteFullException.");
                return false;
            }
            /*
             * Feature Fix by Mediatek End
             */
        }
        mAccountWritability.clear();

//        if (accountsChanged) {
            updateContactsAccountCount(accounts);
            updateProviderStatus();
//        }

        return true;
    }

    private void updateContactsAccountCount(Account[] accounts) {
        int count = 0;
        for (Account account : accounts) {
            if (isContactsAccount(account)) {
                count++;
            }
        }
        mContactsAccountCount = count;
    }

    protected boolean isContactsAccount(Account account) {
        final IContentService cs = ContentResolver.getContentService();
        try {
            return cs.getIsSyncable(account, GnContactsContract.AUTHORITY) > 0;
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot obtain sync flag for account: " + account, e);
            return false;
        }
    }

    public void onPackageChanged(String packageName) {
        scheduleBackgroundTask(BACKGROUND_TASK_UPDATE_DIRECTORIES, packageName);
    }

    public void removeStaleAccountRows(String table, String accountNameColumn,
            String accountTypeColumn, Account[] systemAccounts) {
        final SQLiteDatabase db = mDbHelper.get().getWritableDatabase();
        final Cursor c = db.rawQuery(
                "SELECT DISTINCT " + accountNameColumn +
                "," + accountTypeColumn +
                " FROM " + table, null);
        try {
            c.moveToPosition(-1);
            while (c.moveToNext()) {
                final AccountWithDataSet accountWithDataSet = AccountWithDataSet.get(
                        c.getString(0), c.getString(1), null);
                if (accountWithDataSet.isLocalAccount()
                		|| ourSpecialAccount(accountWithDataSet)
                        || accountWithDataSet.inSystemAccounts(systemAccounts)) {
                    // Account still exists.
                    continue;
                }

                db.execSQL("DELETE FROM " + table +
                        " WHERE " + accountNameColumn + "=? AND " +
                        accountTypeColumn + "=?",
                        new String[] {accountWithDataSet.getAccountName(),
                                accountWithDataSet.getAccountType()});
            }
        } finally {
            c.close();
        }
    }

    /**
     * Finds all distinct account types and data sets present in the specified table.
     */
//    private Set<AccountWithDataSet> findValidAccountsWithDataSets(String table) {
//        Set<AccountWithDataSet> accountsWithDataSets = new HashSet<AccountWithDataSet>();
//        Cursor c = mActiveDb.get().rawQuery(
//                "SELECT DISTINCT " + RawContacts.ACCOUNT_NAME + "," + RawContacts.ACCOUNT_TYPE +
//                "," + RawContacts.DATA_SET +
//                " FROM " + table, null);
//        try {
//            while (c.moveToNext()) {
//                if (!c.isNull(0) && !c.isNull(1) && !isLocalAccount(c)) {
//                    accountsWithDataSets.add(
//                            new AccountWithDataSet(c.getString(0), c.getString(1), c.getString(2)));
//                }
//            }
//        } finally {
//            c.close();
//        }
//        return accountsWithDataSets;
//    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {

        waitForAccess(mReadAccessLatch);

        // Enforce stream items access check if applicable.
        enforceSocialStreamReadPermission(uri);

        // Query the profile DB if appropriate.
//        if (mapsToProfileDb(uri)) {
//            switchToProfileMode();
//            return mProfileProvider.query(uri, projection, selection, selectionArgs, sortOrder);
//        }

        // Otherwise proceed with a normal query against the contacts DB.
        switchToContactMode();
        mActiveDb.set(mContactsHelper.getReadableDatabase());
        
        String directory = getQueryParameter(uri, GnContactsContract.DIRECTORY_PARAM_KEY);
        if (directory == null) {
            return addSnippetExtrasToCursor(uri,
                    queryLocal(uri, projection, selection, selectionArgs, sortOrder, -1));
        } else if (directory.equals("0")) {
            return addSnippetExtrasToCursor(uri,
                    queryLocal(uri, projection, selection, selectionArgs, sortOrder,
                            Directory.DEFAULT));
        } else if (directory.equals("1")) {
            return addSnippetExtrasToCursor(uri,
                    queryLocal(uri, projection, selection, selectionArgs, sortOrder,
                            Directory.LOCAL_INVISIBLE));
        }

        DirectoryInfo directoryInfo = getDirectoryAuthority(directory);
        if (directoryInfo == null) {
            Log.e(TAG, "Invalid directory ID: " + uri);
            return null;
        }

        Builder builder = new Uri.Builder();
        builder.scheme(ContentResolver.SCHEME_CONTENT);
        builder.authority(directoryInfo.authority);
        builder.encodedPath(uri.getEncodedPath());
        if (directoryInfo.accountName != null) {
            builder.appendQueryParameter(RawContacts.ACCOUNT_NAME, directoryInfo.accountName);
        }
        if (directoryInfo.accountType != null) {
            builder.appendQueryParameter(RawContacts.ACCOUNT_TYPE, directoryInfo.accountType);
        }

        String limit = getLimit(uri);
        if (limit != null) {
            builder.appendQueryParameter(GnContactsContract.LIMIT_PARAM_KEY, limit);
        }

        Uri directoryUri = builder.build();

        if (projection == null) {
            projection = getDefaultProjection(uri);
        }

        Cursor cursor = getContext().getContentResolver().query(directoryUri, projection, selection,
                selectionArgs, sortOrder);

        if (cursor == null) {
            return null;
        }

        CrossProcessCursor crossProcessCursor = getCrossProcessCursor(cursor);
        if (crossProcessCursor != null) {
            return addSnippetExtrasToCursor(uri, cursor);
        } else {
            return matrixCursorFromCursor(addSnippetExtrasToCursor(uri, cursor));
        }
    }

    private Cursor addSnippetExtrasToCursor(Uri uri, Cursor cursor) {
    	if (null == cursor) {
    		return null;
    	}

        // If the cursor doesn't contain a snippet column, don't bother wrapping it.
        if (cursor.getColumnIndex(SearchSnippetColumns.SNIPPET) < 0) {
            return cursor;
        }

        // Parse out snippet arguments for use when snippets are retrieved from the cursor.
        String[] args = null;
        String snippetArgs =
                getQueryParameter(uri, SearchSnippetColumns.SNIPPET_ARGS_PARAM_KEY);
        if (snippetArgs != null) {
            args = snippetArgs.split(",");
        }

        String query = uri.getLastPathSegment();
        String startMatch = args != null && args.length > 0 ? args[0]
                : DEFAULT_SNIPPET_ARG_START_MATCH;
        String endMatch = args != null && args.length > 1 ? args[1]
                : DEFAULT_SNIPPET_ARG_END_MATCH;
        String ellipsis = args != null && args.length > 2 ? args[2]
                : DEFAULT_SNIPPET_ARG_ELLIPSIS;
        int maxTokens = args != null && args.length > 3 ? Integer.parseInt(args[3])
                : DEFAULT_SNIPPET_ARG_MAX_TOKENS;

        // Snippet data is needed for the snippeting on the client side, so store it in the cursor
        if (cursor instanceof AbstractCursor && deferredSnippetingRequested(uri)){
            Bundle oldExtras = cursor.getExtras();
            Bundle extras = new Bundle();
            if (oldExtras != null) {
                extras.putAll(oldExtras);
            }
            extras.putString(GnContactsContract.DEFERRED_SNIPPETING_QUERY, query);

            ((AbstractCursor) cursor).setExtras(extras);
        }
        return cursor;
    }

    private Cursor addDeferredSnippetingExtra(Cursor cursor) {
    	if (null == cursor) {
    		return null;
    	}
    	
        if (cursor instanceof AbstractCursor){
            Bundle oldExtras = cursor.getExtras();
            Bundle extras = new Bundle();
            if (oldExtras != null) {
                extras.putAll(oldExtras);
            }
            extras.putBoolean(GnContactsContract.DEFERRED_SNIPPETING, true);
            ((AbstractCursor) cursor).setExtras(extras);
        }
        return cursor;
    }

    private CrossProcessCursor getCrossProcessCursor(Cursor cursor) {
    	if (null == cursor) {
    		return null;
    	}
    	
        Cursor c = cursor;
        if (c instanceof CrossProcessCursor) {
            return (CrossProcessCursor) c;
        } else if (c instanceof CursorWindow) {
            return getCrossProcessCursor(((CursorWrapper) c).getWrappedCursor());
        } else {
            return null;
        }
    }

    public MatrixCursor matrixCursorFromCursor(Cursor cursor) {
    	if (null == cursor) {
    		return null;
    	}
    	
        MatrixCursor newCursor = new MatrixCursor(cursor.getColumnNames());
        int numColumns = cursor.getColumnCount();
        String data[] = new String[numColumns];
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            for (int i = 0; i < numColumns; i++) {
                data[i] = cursor.getString(i);
            }
            newCursor.addRow(data);
        }
        return newCursor;
    }

    private static final class DirectoryQuery {
        public static final String[] COLUMNS = new String[] {
                Directory._ID,
                Directory.DIRECTORY_AUTHORITY,
                Directory.ACCOUNT_NAME,
                Directory.ACCOUNT_TYPE
        };

        public static final int DIRECTORY_ID = 0;
        public static final int AUTHORITY = 1;
        public static final int ACCOUNT_NAME = 2;
        public static final int ACCOUNT_TYPE = 3;
    }

    /**
     * Reads and caches directory information for the database.
     */
    private DirectoryInfo getDirectoryAuthority(String directoryId) {
        synchronized (mDirectoryCache) {
            if (!mDirectoryCacheValid) {
                mDirectoryCache.clear();
                SQLiteDatabase db = mDbHelper.get().getReadableDatabase();
                Cursor cursor = db.query(Tables.DIRECTORIES,
                        DirectoryQuery.COLUMNS,
                        null, null, null, null, null);
                try {
                    while (cursor.moveToNext()) {
                        DirectoryInfo info = new DirectoryInfo();
                        String id = cursor.getString(DirectoryQuery.DIRECTORY_ID);
                        info.authority = cursor.getString(DirectoryQuery.AUTHORITY);
                        info.accountName = cursor.getString(DirectoryQuery.ACCOUNT_NAME);
                        info.accountType = cursor.getString(DirectoryQuery.ACCOUNT_TYPE);
                        mDirectoryCache.put(id, info);
                    }
                } finally {
                    cursor.close();
                }
                mDirectoryCacheValid = true;
            }

            return mDirectoryCache.get(directoryId);
        }
    }

    public void resetDirectoryCache() {
        synchronized(mDirectoryCache) {
            mDirectoryCacheValid = false;
        }
    }

    private boolean hasColumn(String[] projection, String column) {
        if (projection == null) {
            return true; // Null projection means "all columns".
        }

        for (int i = 0; i < projection.length; i++) {
            if (column.equalsIgnoreCase(projection[i])) return true;
        }
        return false;
    }
    
    protected Cursor queryLocal(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder, long directoryId) {
        if (VERBOSE_LOGGING) {
            Log.v(TAG, "query: " + uri + "  &&  selection: " + selection);
        }

        // Default active DB to the contacts DB if none has been set.
        if (mActiveDb.get() == null) {
            mActiveDb.set(mContactsHelper.getReadableDatabase());
        }

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String groupBy = null;
        String limit = getLimit(uri);
        boolean snippetDeferred = false;
        String where = "";

        // The expression used in bundleLetterCountExtras() to get count.
        String addressBookIndexerCountExpression = null;
        final int match = sUriMatcher.match(uri);
        Log.e(TAG, "match = " + match);
        switch (match) {
            case SYNCSTATE:
            case PROFILE_SYNCSTATE:
                return mDbHelper.get().getSyncState().query(mActiveDb.get(), projection, selection,
                        selectionArgs, sortOrder);

            case CONTACTS: {
                selection = parseSelection(selection, false); // aurora wangth 20140930 add for privacy
                setTablesAndProjectionMapForContacts(qb, uri, projection);
//                appendLocalDirectorySelectionIfNeeded(qb, directoryId);
                appendLocalDirectoryAndAccountSelectionIfNeeded(qb, directoryId, uri);
              
                break;
            }

            case CONTACTS_ID: {
//                selection = parseSelection(selection, false); // aurora wangth 20140930 add for privacy
                
                long contactId = ContentUris.parseId(uri);
                setTablesAndProjectionMapForContacts(qb, uri, projection);
                selectionArgs = insertSelectionArg(selectionArgs, String.valueOf(contactId));
                qb.appendWhere(Contacts._ID + "=?");
                break;
            }

            case CONTACTS_LOOKUP:
            case CONTACTS_LOOKUP_ID: {
//                selection = parseSelection(selection, false); // aurora wangth 20140930 add for privacy
                List<String> pathSegments = uri.getPathSegments();
                int segmentCount = pathSegments.size();
                if (segmentCount < 3) {
                    throw new IllegalArgumentException(mDbHelper.get().exceptionMessage(
                            "Missing a lookup key", uri));
                }

                String lookupKey = pathSegments.get(2);
                if (segmentCount == 4) {
                    long contactId = Long.parseLong(pathSegments.get(3));
                    SQLiteQueryBuilder lookupQb = new SQLiteQueryBuilder();
                    setTablesAndProjectionMapForContacts(lookupQb, uri, projection);

                    Cursor c = queryWithContactIdAndLookupKey(lookupQb, mActiveDb.get(), uri,
                            projection, selection, selectionArgs, sortOrder, groupBy, limit,
                            Contacts._ID, contactId, Contacts.LOOKUP_KEY, lookupKey);
                    if (c != null) {
                        return c;
                    }
                }

                setTablesAndProjectionMapForContacts(qb, uri, projection);
                selectionArgs = insertSelectionArg(selectionArgs,
                        String.valueOf(lookupContactIdByLookupKey(mActiveDb.get(), lookupKey)));
                qb.appendWhere(Contacts._ID + "=?");
                break;
            }

            case CONTACTS_LOOKUP_DATA:
            case CONTACTS_LOOKUP_ID_DATA:
            case CONTACTS_LOOKUP_PHOTO:
            case CONTACTS_LOOKUP_ID_PHOTO: {
//                selection = parseSelection(selection, false); // aurora wangth 20140930 add for privacy
                
                List<String> pathSegments = uri.getPathSegments();
                int segmentCount = pathSegments.size();
                if (segmentCount < 4) {
                    throw new IllegalArgumentException(mDbHelper.get().exceptionMessage(
                            "Missing a lookup key", uri));
                }
                String lookupKey = pathSegments.get(2);
                if (segmentCount == 5) {
                    long contactId = Long.parseLong(pathSegments.get(3));
                    SQLiteQueryBuilder lookupQb = new SQLiteQueryBuilder();
                    setTablesAndProjectionMapForData(lookupQb, uri, projection, false);
                    if (match == CONTACTS_LOOKUP_PHOTO || match == CONTACTS_LOOKUP_ID_PHOTO) {
                        qb.appendWhere(" AND " + Data._ID + "=" + Contacts.PHOTO_ID);
                    }
                    lookupQb.appendWhere(" AND ");
                    Cursor c = queryWithContactIdAndLookupKey(lookupQb, mActiveDb.get(), uri,
                            projection, selection, selectionArgs, sortOrder, groupBy, limit,
                            Data.CONTACT_ID, contactId, Data.LOOKUP_KEY, lookupKey);
                    if (c != null) {
                        return c;
                    }

                    // TODO see if the contact exists but has no data rows (rare)
                }

                setTablesAndProjectionMapForData(qb, uri, projection, false);
                long contactId = lookupContactIdByLookupKey(mActiveDb.get(), lookupKey);
                selectionArgs = insertSelectionArg(selectionArgs,
                        String.valueOf(contactId));
                if (match == CONTACTS_LOOKUP_PHOTO || match == CONTACTS_LOOKUP_ID_PHOTO) {
                    qb.appendWhere(" AND " + Data._ID + "=" + Contacts.PHOTO_ID);
                }
                qb.appendWhere(" AND " + Data.CONTACT_ID + "=?");
                break;
            }

            case CONTACTS_ID_STREAM_ITEMS: {
                long contactId = Long.parseLong(uri.getPathSegments().get(1));
                setTablesAndProjectionMapForStreamItems(qb);
                selectionArgs = insertSelectionArg(selectionArgs, String.valueOf(contactId));
                qb.appendWhere(StreamItems.CONTACT_ID + "=?");
                break;
            }

            case CONTACTS_LOOKUP_STREAM_ITEMS:
            case CONTACTS_LOOKUP_ID_STREAM_ITEMS: {
                List<String> pathSegments = uri.getPathSegments();
                int segmentCount = pathSegments.size();
                if (segmentCount < 4) {
                    throw new IllegalArgumentException(mDbHelper.get().exceptionMessage(
                            "Missing a lookup key", uri));
                }
                String lookupKey = pathSegments.get(2);
                if (segmentCount == 5) {
                    long contactId = Long.parseLong(pathSegments.get(3));
                    SQLiteQueryBuilder lookupQb = new SQLiteQueryBuilder();
                    setTablesAndProjectionMapForStreamItems(lookupQb);
                    Cursor c = queryWithContactIdAndLookupKey(lookupQb, mActiveDb.get(), uri,
                            projection, selection, selectionArgs, sortOrder, groupBy, limit,
                            StreamItems.CONTACT_ID, contactId,
                            StreamItems.CONTACT_LOOKUP_KEY, lookupKey);
                    if (c != null) {
                        return c;
                    }
                }

                setTablesAndProjectionMapForStreamItems(qb);
                long contactId = lookupContactIdByLookupKey(mActiveDb.get(), lookupKey);
                selectionArgs = insertSelectionArg(selectionArgs, String.valueOf(contactId));
                qb.appendWhere(RawContacts.CONTACT_ID + "=?");
                break;
            }

            case CONTACTS_AS_VCARD: {
                selection = parseSelection(selection, false); // aurora wangth 20140930 add for privacy
                
                final String lookupKey = Uri.encode(uri.getPathSegments().get(2));
                long contactId = lookupContactIdByLookupKey(mActiveDb.get(), lookupKey);
                qb.setTables(Views.CONTACTS);
                qb.setProjectionMap(sContactsVCardProjectionMap);
                selectionArgs = insertSelectionArg(selectionArgs,
                        String.valueOf(contactId));
                qb.appendWhere(Contacts._ID + "=?");
                break;
            }

            case CONTACTS_AS_MULTI_VCARD: {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String currentDateString = dateFormat.format(new Date()).toString();
                return mActiveDb.get().rawQuery(
                    "SELECT" +
                    " 'vcards_' || ? || '.vcf' AS " + OpenableColumns.DISPLAY_NAME + "," +
                    " NULL AS " + OpenableColumns.SIZE,
                    new String[] { currentDateString });
            }

            case CONTACTS_FILTER: {
                selection = parseSelection(selection, false); // aurora wangth 20140930 add for privacy
                
                String filterParam = "";
                boolean deferredSnipRequested = deferredSnippetingRequested(uri);
                if (uri.getPathSegments().size() > 2) {
                    filterParam = uri.getLastPathSegment();
                }
                setTablesAndProjectionMapForContactsWithSnippet(
                        qb, uri, projection, filterParam, directoryId,
                        deferredSnipRequested);
                snippetDeferred = isSingleWordQuery(filterParam) &&
                        deferredSnipRequested && snippetNeeded(projection);
                break;
            }

            case CONTACTS_STREQUENT_FILTER:
            case CONTACTS_STREQUENT: {
                // Basically the resultant SQL should look like this:
                // (SQL for listing starred items)
                // UNION ALL
                // (SQL for listing frequently contacted items)
                // ORDER BY ...

                selection = parseSelection(selection, false); // aurora wangth 20140930 add for privacy
                
                final boolean phoneOnly = readBooleanQueryParameter(
                        uri, GnContactsContract.STREQUENT_PHONE_ONLY, false);
                if (match == CONTACTS_STREQUENT_FILTER && uri.getPathSegments().size() > 3) {
                    String filterParam = uri.getLastPathSegment();
                    StringBuilder sb = new StringBuilder();
                    sb.append(Contacts._ID + " IN ");
                    appendContactFilterAsNestedQuery(sb, filterParam);
                    selection = DbQueryUtils.concatenateClauses(selection, sb.toString());
                }

                String[] subProjection = null;
                if (projection != null) {
                    subProjection = appendProjectionArg(projection, TIMES_USED_SORT_COLUMN);
                }

                // Build the first query for starred
                setTablesAndProjectionMapForContacts(qb, uri, projection, false);
                qb.setProjectionMap(phoneOnly ?
                        sStrequentPhoneOnlyStarredProjectionMap
                        : sStrequentStarredProjectionMap);
                if (phoneOnly) {
                    qb.appendWhere(DbQueryUtils.concatenateClauses(
                            selection, Contacts.HAS_PHONE_NUMBER + "=1"));
                }
                qb.setStrict(true);
                final String starredInnerQuery = qb.buildQuery(subProjection,
                        Contacts.STARRED + "=1", Contacts._ID, null,
                        Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC", null);

                // Reset the builder.
                qb = new SQLiteQueryBuilder();
                qb.setStrict(true);

                // Build the second query for frequent part. These JOINS can be very slow
                // if assembled in the wrong order. Be sure to test changes against huge databases.
                final String frequentInnerQuery;
                if (phoneOnly) {
                    final StringBuilder tableBuilder = new StringBuilder();
                    // In phone only mode, we need to look at view_data instead of
                    // contacts/raw_contacts to obtain actual phone numbers. One problem is that
                    // view_data is much larger than view_contacts, so our query might become much
                    // slower.
                    //
                    // To avoid the possible slow down, we start from data usage table and join
                    // view_data to the table, assuming data usage table is quite smaller than
                    // data rows (almost always it should be), and we don't want any phone
                    // numbers not used by the user. This way sqlite is able to drop a number of
                    // rows in view_data in the early stage of data lookup.
                    tableBuilder.append(Tables.DATA_USAGE_STAT
                            + " INNER JOIN " + Views.DATA + " " + Tables.DATA
                            + " ON (" + DataUsageStatColumns.CONCRETE_DATA_ID + "="
                                + DataColumns.CONCRETE_ID + " AND "
                            + DataUsageStatColumns.CONCRETE_USAGE_TYPE + "="
                                + DataUsageStatColumns.USAGE_TYPE_INT_CALL + ")");
                    appendContactPresenceJoin(tableBuilder, projection, RawContacts.CONTACT_ID);
                    appendContactStatusUpdateJoin(tableBuilder, projection,
                            ContactsColumns.LAST_STATUS_UPDATE_ID);

                    qb.setTables(tableBuilder.toString());
                    qb.setProjectionMap(sStrequentPhoneOnlyFrequentProjectionMap);
                    final long phoneMimeTypeId =
                            mDbHelper.get().getMimeTypeId(Phone.CONTENT_ITEM_TYPE);
                    final long sipMimeTypeId =
                            mDbHelper.get().getMimeTypeId(SipAddress.CONTENT_ITEM_TYPE);
                    qb.appendWhere(DbQueryUtils.concatenateClauses(
                            selection,
                            Contacts.STARRED + "=0 OR " + Contacts.STARRED + " IS NULL",
                            DataColumns.MIMETYPE_ID + " IN (" +
                            phoneMimeTypeId + ", " + sipMimeTypeId + ")"));
                    frequentInnerQuery =
                            qb.buildQuery(subProjection, null, null, null,
                            TIMES_USED_SORT_COLUMN + " DESC", "25");
                } else {
                    setTablesAndProjectionMapForContacts(qb, uri, projection, true);
                    qb.setProjectionMap(sStrequentFrequentProjectionMap);
                    qb.appendWhere(DbQueryUtils.concatenateClauses(
                            selection,
                            "(" + Contacts.STARRED + " =0 OR " + Contacts.STARRED + " IS NULL)"));
                    frequentInnerQuery = qb.buildQuery(subProjection,
                            null, Contacts._ID, null, null, "25");
                }

                // We need to wrap the inner queries in an extra select, because they contain
                // their own SORT and LIMIT
                final String frequentQuery = "SELECT * FROM (" + frequentInnerQuery + ")";
                final String starredQuery = "SELECT * FROM (" + starredInnerQuery + ")";

                // Put them together
                final String unionQuery =
                        qb.buildUnionQuery(new String[] {starredQuery, frequentQuery}, null, null);

                // Here, we need to use selection / selectionArgs (supplied from users) "twice",
                // as we want them both for starred items and for frequently contacted items.
                //
                // e.g. if the user specify selection = "starred =?" and selectionArgs = "0",
                // the resultant SQL should be like:
                // SELECT ... WHERE starred =? AND ...
                // UNION ALL
                // SELECT ... WHERE starred =? AND ...
                String[] doubledSelectionArgs = null;
                if (selectionArgs != null) {
                    final int length = selectionArgs.length;
                    doubledSelectionArgs = new String[length * 2];
                    System.arraycopy(selectionArgs, 0, doubledSelectionArgs, 0, length);
                    System.arraycopy(selectionArgs, 0, doubledSelectionArgs, length, length);
                }

                Cursor cursor = mActiveDb.get().rawQuery(unionQuery, doubledSelectionArgs);
                if (cursor != null) {
                    cursor.setNotificationUri(getContext().getContentResolver(),
                            GnContactsContract.AUTHORITY_URI);
                }
                return cursor;
            }

            case CONTACTS_FREQUENT: {
                selection = parseSelection(selection, false); // aurora wangth 20140930 add for privacy
                
                setTablesAndProjectionMapForContacts(qb, uri, projection, true);
                qb.setProjectionMap(sStrequentFrequentProjectionMap);
                groupBy = Contacts._ID;
                if (!TextUtils.isEmpty(sortOrder)) {
                    sortOrder = FREQUENT_ORDER_BY + ", " + sortOrder;
                } else {
                    sortOrder = FREQUENT_ORDER_BY;
                }
                break;
            }

            case CONTACTS_GROUP: {
                selection = parseSelection(selection, false); // aurora wangth 20140930 add for privacy
                
                setTablesAndProjectionMapForContacts(qb, uri, projection);
                if (uri.getPathSegments().size() > 2) {
                    qb.appendWhere(CONTACTS_IN_GROUP_SELECT);
                    String groupMimeTypeId = String.valueOf(
                            mDbHelper.get().getMimeTypeId(GroupMembership.CONTENT_ITEM_TYPE));
                    selectionArgs = insertSelectionArg(selectionArgs, uri.getLastPathSegment());
                    selectionArgs = insertSelectionArg(selectionArgs, groupMimeTypeId);
                }
                break;
            }

            case PROFILE: {
            	// aurora wangth 20140819 add for tenctent mm for default, maybe modify in the future
            	if (ContactsProvidersApplication.sIsGnContactsSupport) {
            		return null;
            	}
            	// aurora wangth 20140819 add end
                setTablesAndProjectionMapForContacts(qb, uri, projection);
                break;
            }

            case PROFILE_ENTITIES: {
                setTablesAndProjectionMapForEntities(qb, uri, projection);
                break;
            }

            case PROFILE_AS_VCARD: {
                qb.setTables(Views.CONTACTS);
                qb.setProjectionMap(sContactsVCardProjectionMap);
                break;
            }

            case CONTACTS_ID_DATA: {
                selection = parseSelection(selection, false); // aurora wangth 20140930 add for privacy
                
                long contactId = Long.parseLong(uri.getPathSegments().get(1));
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                selectionArgs = insertSelectionArg(selectionArgs, String.valueOf(contactId));
                qb.appendWhere(" AND " + RawContacts.CONTACT_ID + "=?");
                break;
            }

            case CONTACTS_ID_PHOTO: {
                selection = parseSelection(selection, false); // aurora wangth 20140930 add for privacy
                
                long contactId = Long.parseLong(uri.getPathSegments().get(1));
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                selectionArgs = insertSelectionArg(selectionArgs, String.valueOf(contactId));
                qb.appendWhere(" AND " + RawContacts.CONTACT_ID + "=?");
                qb.appendWhere(" AND " + Data._ID + "=" + Contacts.PHOTO_ID);
                break;
            }

            case CONTACTS_ID_ENTITIES: {
//                selection = parseSelection(selection, false); // aurora wangth 20140930 add for privacy
                
                long contactId = Long.parseLong(uri.getPathSegments().get(1));
                setTablesAndProjectionMapForEntities(qb, uri, projection);
                selectionArgs = insertSelectionArg(selectionArgs, String.valueOf(contactId));
                qb.appendWhere(" AND " + RawContacts.CONTACT_ID + "=?");
                break;
            }

            case CONTACTS_LOOKUP_ENTITIES:
            case CONTACTS_LOOKUP_ID_ENTITIES: {
                List<String> pathSegments = uri.getPathSegments();
                int segmentCount = pathSegments.size();
                if (segmentCount < 4) {
                    throw new IllegalArgumentException(mDbHelper.get().exceptionMessage(
                            "Missing a lookup key", uri));
                }
                String lookupKey = pathSegments.get(2);
                if (segmentCount == 5) {
                    long contactId = Long.parseLong(pathSegments.get(3));
                    SQLiteQueryBuilder lookupQb = new SQLiteQueryBuilder();
                    setTablesAndProjectionMapForEntities(lookupQb, uri, projection);
                    lookupQb.appendWhere(" AND ");

                    Cursor c = queryWithContactIdAndLookupKey(lookupQb, mActiveDb.get(), uri,
                            projection, selection, selectionArgs, sortOrder, groupBy, limit,
                            Contacts.Entity.CONTACT_ID, contactId,
                            Contacts.Entity.LOOKUP_KEY, lookupKey);
                    if (c != null) {
                        return c;
                    }
                }

                setTablesAndProjectionMapForEntities(qb, uri, projection);
                selectionArgs = insertSelectionArg(selectionArgs,
                        String.valueOf(lookupContactIdByLookupKey(mActiveDb.get(), lookupKey)));
                qb.appendWhere(" AND " + Contacts.Entity.CONTACT_ID + "=?");
                break;
            }

            case STREAM_ITEMS: {
                setTablesAndProjectionMapForStreamItems(qb);
                break;
            }

            case STREAM_ITEMS_ID: {
                setTablesAndProjectionMapForStreamItems(qb);
                selectionArgs = insertSelectionArg(selectionArgs, uri.getLastPathSegment());
                qb.appendWhere(StreamItems._ID + "=?");
                break;
            }

            case STREAM_ITEMS_LIMIT: {
                MatrixCursor cursor = new MatrixCursor(new String[]{StreamItems.MAX_ITEMS}, 1);
                cursor.addRow(new Object[]{MAX_STREAM_ITEMS_PER_RAW_CONTACT});
                return cursor;
            }

            case STREAM_ITEMS_PHOTOS: {
                setTablesAndProjectionMapForStreamItemPhotos(qb);
                break;
            }

            case STREAM_ITEMS_ID_PHOTOS: {
                setTablesAndProjectionMapForStreamItemPhotos(qb);
                String streamItemId = uri.getPathSegments().get(1);
                selectionArgs = insertSelectionArg(selectionArgs, streamItemId);
                qb.appendWhere(StreamItemPhotosColumns.CONCRETE_STREAM_ITEM_ID + "=?");
                break;
            }

            case STREAM_ITEMS_ID_PHOTOS_ID: {
                setTablesAndProjectionMapForStreamItemPhotos(qb);
                String streamItemId = uri.getPathSegments().get(1);
                String streamItemPhotoId = uri.getPathSegments().get(3);
                selectionArgs = insertSelectionArg(selectionArgs, streamItemPhotoId);
                selectionArgs = insertSelectionArg(selectionArgs, streamItemId);
                qb.appendWhere(StreamItemPhotosColumns.CONCRETE_STREAM_ITEM_ID + "=? AND " +
                        StreamItemPhotosColumns.CONCRETE_ID + "=?");
                break;
            }

            case PHOTO_DIMENSIONS: {
                MatrixCursor cursor = new MatrixCursor(
                        new String[]{DisplayPhoto.DISPLAY_MAX_DIM, DisplayPhoto.THUMBNAIL_MAX_DIM},
                        1);
                cursor.addRow(new Object[]{mMaxDisplayPhotoDim, mMaxThumbnailPhotoDim});
                return cursor;
            }

            case PHONES:
            case CALLABLES: {
                selection = parseSelection(selection, false); // aurora wangth 20140930 add for privacy
                
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                qb.appendWhere(" AND " + DataColumns.MIMETYPE_ID + "=" +
                        mDbHelper.get().getMimeTypeIdForPhone());

                final boolean removeDuplicates = readBooleanQueryParameter(
                        uri, GnContactsContract.REMOVE_DUPLICATE_ENTRIES, false);
                if (removeDuplicates) {
                    groupBy = RawContacts.CONTACT_ID + ", " + Data.DATA1;

                    // In this case, because we dedupe phone numbers, the address book indexer needs
                    // to take it into account too.  (Otherwise headers will appear in wrong
                    // positions.)
                    // So use count(distinct pair(CONTACT_ID, PHONE NUMBER)) instead of count(*).
                    // But because there's no such thing as pair() on sqlite, we use
                    // CONTACT_ID || ',' || PHONE NUMBER instead.
                    // This only slows down the query by 14% with 10,000 contacts.
                    addressBookIndexerCountExpression = "DISTINCT "
                            + RawContacts.CONTACT_ID + "||','||" + Data.DATA1;
                }
                
                break;
            }

            case PHONES_ID:
            case CALLABLES_ID: {
                selection = parseSelection(selection, false); // aurora wangth 20140930 add for privacy
                
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                selectionArgs = insertSelectionArg(selectionArgs, uri.getLastPathSegment());
                qb.appendWhere(" AND " + DataColumns.MIMETYPE_ID + " = "
                        + mDbHelper.get().getMimeTypeIdForPhone());
                qb.appendWhere(" AND " + Data._ID + "=?");
                break;
            }

            case PHONES_FILTER:
            case CALLABLES_FILTER: {
                selection = parseSelection(selection, false); // aurora wangth 20140930 add for privacy
                
                String typeParam = uri.getQueryParameter(DataUsageFeedback.USAGE_TYPE);
                Integer typeInt = sDataUsageTypeMap.get(typeParam);
                if (typeInt == null) {
                    typeInt = DataUsageStatColumns.USAGE_TYPE_INT_CALL;
                }
                setTablesAndProjectionMapForData(qb, uri, projection, true, typeInt);
                qb.appendWhere(" AND " + DataColumns.MIMETYPE_ID + " = "
                        + mDbHelper.get().getMimeTypeIdForPhone());
                if (uri.getPathSegments().size() > 2) {
                    String filterParam = uri.getLastPathSegment();
                    StringBuilder sb = new StringBuilder();
                    sb.append(" AND (");

                    boolean hasCondition = false;
                    boolean orNeeded = false;
                    final String ftsMatchQuery = SearchIndexManager.getFtsMatchQuery(
                            filterParam, FtsQueryBuilder.UNSCOPED_NORMALIZING);
                    if (ftsMatchQuery.length() > 0) {
                        sb.append(Data.RAW_CONTACT_ID + " IN " +
                                "(SELECT " + RawContactsColumns.CONCRETE_ID +
                                " FROM " + Tables.SEARCH_INDEX +
                                " JOIN " + Tables.RAW_CONTACTS +
                                " ON (" + Tables.SEARCH_INDEX + "." + SearchIndexColumns.CONTACT_ID
                                        + "=" + RawContactsColumns.CONCRETE_CONTACT_ID + ")" +
                                " WHERE " + SearchIndexColumns.NAME + " MATCH '");
                        sb.append(ftsMatchQuery);
                        sb.append("')");
                        orNeeded = true;
                        hasCondition = true;
                    }

                    // aurora <wangth> <2013-11-4> modify for aurora begin 
                    //String number = PhoneNumberUtils.normalizeNumber(filterParam);
                    String number = filterParam;
                    // aurora <wangth> <2013-11-4> modify for aurora end
                    if (!TextUtils.isEmpty(number)) {
                        if (orNeeded) {
                            sb.append(" OR ");
                        }
                        sb.append(Data._ID +
                                " IN (SELECT DISTINCT " + PhoneLookupColumns.DATA_ID
                                + " FROM " + Tables.PHONE_LOOKUP
                                + " WHERE " + PhoneLookupColumns.NORMALIZED_NUMBER + " LIKE '");
                        sb.append(number);
                        sb.append("%')");
                        hasCondition = true;
                    }

                    if (!hasCondition) {
                        // If it is neither a phone number nor a name, the query should return
                        // an empty cursor.  Let's ensure that.
                        sb.append("0");
                    }
                    sb.append(")");
                    qb.appendWhere(sb);
                }
                groupBy = "(CASE WHEN " + PhoneColumns.NORMALIZED_NUMBER
                        + " IS NOT NULL THEN " + PhoneColumns.NORMALIZED_NUMBER
                        + " ELSE " + Phone.NUMBER + " END), " + RawContacts.CONTACT_ID;
                if (sortOrder == null) {
                    final String accountPromotionSortOrder = getAccountPromotionSortOrder(uri);
                    if (!TextUtils.isEmpty(accountPromotionSortOrder)) {
                        sortOrder = accountPromotionSortOrder + ", " + PHONE_FILTER_SORT_ORDER;
                    } else {
                        sortOrder = PHONE_FILTER_SORT_ORDER;
                    }
                }
                break;
            }

            case EMAILS: {
                selection = parseSelection(selection, false); // aurora wangth 20140930 add for privacy
                
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                qb.appendWhere(" AND " + DataColumns.MIMETYPE_ID + " = "
                        + mDbHelper.get().getMimeTypeIdForEmail());

                final boolean removeDuplicates = readBooleanQueryParameter(
                        uri, GnContactsContract.REMOVE_DUPLICATE_ENTRIES, false);
                if (removeDuplicates) {
                    groupBy = RawContacts.CONTACT_ID + ", " + Data.DATA1;

                    // See PHONES for more detail.
                    addressBookIndexerCountExpression = "DISTINCT "
                            + RawContacts.CONTACT_ID + "||','||" + Data.DATA1;
                }
                break;
            }

            case EMAILS_ID: {
                selection = parseSelection(selection, false); // aurora wangth 20140930 add for privacy
                
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                selectionArgs = insertSelectionArg(selectionArgs, uri.getLastPathSegment());
                qb.appendWhere(" AND " + DataColumns.MIMETYPE_ID + " = "
                        + mDbHelper.get().getMimeTypeIdForEmail()
                        + " AND " + Data._ID + "=?");
                break;
            }

            case EMAILS_LOOKUP: {
                selection = parseSelection(selection, false); // aurora wangth 20140930 add for privacy
                
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                qb.appendWhere(" AND " + DataColumns.MIMETYPE_ID + " = "
                        + mDbHelper.get().getMimeTypeIdForEmail());
                if (uri.getPathSegments().size() > 2) {
                    String email = uri.getLastPathSegment();
                    String address = mDbHelper.get().extractAddressFromEmailAddress(email);
                    selectionArgs = insertSelectionArg(selectionArgs, address);
                    qb.appendWhere(" AND UPPER(" + Email.DATA + ")=UPPER(?)");
                }
                // unless told otherwise, we'll return visible before invisible contacts
                if (sortOrder == null) {
                    sortOrder = "(" + RawContacts.CONTACT_ID + " IN " +
                            Tables.DEFAULT_DIRECTORY + ") DESC";
                }
                break;
            }

            case EMAILS_FILTER: {
                selection = parseSelection(selection, false); // aurora wangth 20140930 add for privacy
                
                String typeParam = uri.getQueryParameter(DataUsageFeedback.USAGE_TYPE);
                Integer typeInt = sDataUsageTypeMap.get(typeParam);
                if (typeInt == null) {
                    typeInt = DataUsageStatColumns.USAGE_TYPE_INT_LONG_TEXT;
                }
                setTablesAndProjectionMapForData(qb, uri, projection, true, typeInt);
                String filterParam = null;

                if (uri.getPathSegments().size() > 3) {
                    filterParam = uri.getLastPathSegment();
                    if (TextUtils.isEmpty(filterParam)) {
                        filterParam = null;
                    }
                }

                if (filterParam == null) {
                    // If the filter is unspecified, return nothing
                    qb.appendWhere(" AND 0");
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append(" AND " + Data._ID + " IN (");
                    sb.append(
                            "SELECT " + Data._ID +
                            " FROM " + Tables.DATA +
                            " WHERE " + DataColumns.MIMETYPE_ID + "=");
                    sb.append(mDbHelper.get().getMimeTypeIdForEmail());
                    sb.append(" AND " + Data.DATA1 + " LIKE ");
                    DatabaseUtils.appendEscapedSQLString(sb, filterParam + '%');
                    if (!filterParam.contains("@")) {
                        sb.append(
                                " UNION SELECT " + Data._ID +
                                " FROM " + Tables.DATA +
                                " WHERE +" + DataColumns.MIMETYPE_ID + "=");
                        sb.append(mDbHelper.get().getMimeTypeIdForEmail());
                        sb.append(" AND " + Data.RAW_CONTACT_ID + " IN " +
                                "(SELECT " + RawContactsColumns.CONCRETE_ID +
                                " FROM " + Tables.SEARCH_INDEX +
                                " JOIN " + Tables.RAW_CONTACTS +
                                " ON (" + Tables.SEARCH_INDEX + "." + SearchIndexColumns.CONTACT_ID
                                        + "=" + RawContactsColumns.CONCRETE_CONTACT_ID + ")" +
                                " WHERE " + SearchIndexColumns.NAME + " MATCH '");
                        final String ftsMatchQuery = SearchIndexManager.getFtsMatchQuery(
                                filterParam, FtsQueryBuilder.UNSCOPED_NORMALIZING);
                        sb.append(ftsMatchQuery);
                        sb.append("')");
                    }
                    sb.append(")");
                    qb.appendWhere(sb);
                }
                groupBy = Email.DATA + "," + RawContacts.CONTACT_ID;
                if (sortOrder == null) {
                    final String accountPromotionSortOrder = getAccountPromotionSortOrder(uri);
                    if (!TextUtils.isEmpty(accountPromotionSortOrder)) {
                        sortOrder = accountPromotionSortOrder + ", " + EMAIL_FILTER_SORT_ORDER;
                    } else {
                        sortOrder = EMAIL_FILTER_SORT_ORDER;
                    }
                }
                break;
            }

            case CONTACTABLES:
            case CONTACTABLES_FILTER: {
                setTablesAndProjectionMapForData(qb, uri, projection, false);

                String filterParam = null;

                final int uriPathSize = uri.getPathSegments().size();
                if (uriPathSize > 3) {
                    filterParam = uri.getLastPathSegment();
                    if (TextUtils.isEmpty(filterParam)) {
                        filterParam = null;
                    }
                }

                // CONTACTABLES_FILTER but no query provided, return an empty cursor
                if (uriPathSize > 2 && filterParam == null) {
                    qb.appendWhere(" AND 0");
                    break;
                }

                if (uri.getBooleanQueryParameter(ContactsContract.CommonDataKinds.Contactables.VISIBLE_CONTACTS_ONLY, false)) {
                    qb.appendWhere(" AND " + Data.CONTACT_ID + " in " +
                            Tables.DEFAULT_DIRECTORY);
                    }

                final StringBuilder sb = new StringBuilder();

                // we only want data items that are either email addresses or phone numbers
                sb.append(" AND (");
                sb.append(DataColumns.MIMETYPE_ID + " IN (");
                sb.append(mDbHelper.get().getMimeTypeIdForEmail());
                sb.append(",");
                sb.append(mDbHelper.get().getMimeTypeIdForPhone());
                sb.append("))");

                // Rest of the query is only relevant if we are handling CONTACTABLES_FILTER
                if (uriPathSize < 3) {
                    qb.appendWhere(sb);
                    break;
                }

                // but we want all the email addresses and phone numbers that belong to
                // all contacts that have any data items (or name) that match the query
                sb.append(" AND ");
                sb.append("(" + Data.CONTACT_ID + " IN (");

                // All contacts where the email address data1 column matches the query
                sb.append(
                        "SELECT " + RawContacts.CONTACT_ID +
                        " FROM " + Tables.DATA + " JOIN " + Tables.RAW_CONTACTS +
                        " ON " + Tables.DATA + "." + Data.RAW_CONTACT_ID + "=" +
                        Tables.RAW_CONTACTS + "." + RawContacts._ID +
                        " WHERE (" + DataColumns.MIMETYPE_ID + "=");
                sb.append(mDbHelper.get().getMimeTypeIdForEmail());

                sb.append(" AND " + Data.DATA1 + " LIKE ");
                DatabaseUtils.appendEscapedSQLString(sb, filterParam + '%');
                sb.append(")");

                // All contacts where the phone number matches the query (determined by checking
                // Tables.PHONE_LOOKUP
                final String number = PhoneNumberUtils.normalizeNumber(filterParam);
                if (!TextUtils.isEmpty(number)) {
                    sb.append("UNION SELECT DISTINCT " + RawContacts.CONTACT_ID +
                            " FROM " + Tables.PHONE_LOOKUP + " JOIN " + Tables.RAW_CONTACTS +
                            " ON (" + Tables.PHONE_LOOKUP + "." +
                            PhoneLookupColumns.RAW_CONTACT_ID + "=" +
                            Tables.RAW_CONTACTS + "." + RawContacts._ID + ")" +
                            " WHERE " + PhoneLookupColumns.NORMALIZED_NUMBER + " LIKE '");
                    sb.append(number);
                    sb.append("%'");
                }

                // All contacts where the name matches the query (determined by checking
                // Tables.SEARCH_INDEX
                sb.append(
                        " UNION SELECT " + Data.CONTACT_ID +
                        " FROM " + Tables.DATA + " JOIN " + Tables.RAW_CONTACTS +
                        " ON " + Tables.DATA + "." + Data.RAW_CONTACT_ID + "=" +
                        Tables.RAW_CONTACTS + "." + RawContacts._ID +

                        " WHERE " + Data.RAW_CONTACT_ID + " IN " +

                        "(SELECT " + RawContactsColumns.CONCRETE_ID +
                        " FROM " + Tables.SEARCH_INDEX +
                        " JOIN " + Tables.RAW_CONTACTS +
                        " ON (" + Tables.SEARCH_INDEX + "." + SearchIndexColumns.CONTACT_ID
                        + "=" + RawContactsColumns.CONCRETE_CONTACT_ID + ")" +

                        " WHERE " + SearchIndexColumns.NAME + " MATCH '");

                final String ftsMatchQuery = SearchIndexManager.getFtsMatchQuery(
                        filterParam, FtsQueryBuilder.UNSCOPED_NORMALIZING);
                sb.append(ftsMatchQuery);
                sb.append("')");

                sb.append("))");
                qb.appendWhere(sb);

                break;
            }

            case POSTALS: {
                selection = parseSelection(selection, false); // aurora wangth 20140930 add for privacy
                
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                qb.appendWhere(" AND " + DataColumns.MIMETYPE_ID + " = "
                        + mDbHelper.get().getMimeTypeIdForStructuredPostal());

                final boolean removeDuplicates = readBooleanQueryParameter(
                        uri, GnContactsContract.REMOVE_DUPLICATE_ENTRIES, false);
                if (removeDuplicates) {
                    groupBy = RawContacts.CONTACT_ID + ", " + Data.DATA1;

                    // See PHONES for more detail.
                    addressBookIndexerCountExpression = "DISTINCT "
                            + RawContacts.CONTACT_ID + "||','||" + Data.DATA1;
                }
                break;
            }

            case POSTALS_ID: {
                selection = parseSelection(selection, false); // aurora wangth 20140930 add for privacy
                
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                selectionArgs = insertSelectionArg(selectionArgs, uri.getLastPathSegment());
                qb.appendWhere(" AND " + DataColumns.MIMETYPE_ID + " = "
                        + mDbHelper.get().getMimeTypeIdForStructuredPostal());
                qb.appendWhere(" AND " + Data._ID + "=?");
                break;
            }

            case RAW_CONTACTS:
            case PROFILE_RAW_CONTACTS: {
                selection = parseSelection(selection, false); // aurora wangth 20140930 add for privacy
                setTablesAndProjectionMapForRawContacts(qb, uri);
                break;
            }

            case RAW_CONTACTS_ID:
            case PROFILE_RAW_CONTACTS_ID: {
                long rawContactId = ContentUris.parseId(uri);
                setTablesAndProjectionMapForRawContacts(qb, uri);
                selectionArgs = insertSelectionArg(selectionArgs, String.valueOf(rawContactId));
                qb.appendWhere(" AND " + RawContacts._ID + "=?");
                break;
            }

            case RAW_CONTACTS_DATA:
            case PROFILE_RAW_CONTACTS_ID_DATA: {
                int segment = match == RAW_CONTACTS_DATA ? 1 : 2;
                long rawContactId = Long.parseLong(uri.getPathSegments().get(segment));
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                selectionArgs = insertSelectionArg(selectionArgs, String.valueOf(rawContactId));
                qb.appendWhere(" AND " + Data.RAW_CONTACT_ID + "=?");
                break;
            }

            case RAW_CONTACTS_ID_STREAM_ITEMS: {
                long rawContactId = Long.parseLong(uri.getPathSegments().get(1));
                setTablesAndProjectionMapForStreamItems(qb);
                selectionArgs = insertSelectionArg(selectionArgs, String.valueOf(rawContactId));
                qb.appendWhere(StreamItems.RAW_CONTACT_ID + "=?");
                break;
            }

            case RAW_CONTACTS_ID_STREAM_ITEMS_ID: {
                long rawContactId = Long.parseLong(uri.getPathSegments().get(1));
                long streamItemId = Long.parseLong(uri.getPathSegments().get(3));
                setTablesAndProjectionMapForStreamItems(qb);
                selectionArgs = insertSelectionArg(selectionArgs, String.valueOf(streamItemId));
                selectionArgs = insertSelectionArg(selectionArgs, String.valueOf(rawContactId));
                qb.appendWhere(StreamItems.RAW_CONTACT_ID + "=? AND " +
                        StreamItems._ID + "=?");
                break;
            }

            case PROFILE_RAW_CONTACTS_ID_ENTITIES: {
                long rawContactId = Long.parseLong(uri.getPathSegments().get(2));
                selectionArgs = insertSelectionArg(selectionArgs, String.valueOf(rawContactId));
                setTablesAndProjectionMapForRawEntities(qb, uri);
                qb.appendWhere(" AND " + RawContacts._ID + "=?");
                break;
            }

            case DATA:
            case PROFILE_DATA: {
                selection = parseSelection(selection, false); // aurora wangth 20140930 add for privacy
                if(uri.getQueryParameter("merge")!=null&&uri.getQueryParameter("merge").equals("true")){
                	groupBy="data1";
                }
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                break;
            }
            case DATA_ID:
            case PROFILE_DATA_ID: {
                selection = parseSelection(selection, false); // aurora wangth 20140930 add for privacy
                
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                selectionArgs = insertSelectionArg(selectionArgs, uri.getLastPathSegment());
                qb.appendWhere(" AND " + Data._ID + "=?");
                break;
            }

            case PROFILE_PHOTO: {
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                qb.appendWhere(" AND " + Data._ID + "=" + Contacts.PHOTO_ID);
                break;
            }

            case PHONE_LOOKUP: {
                // Phone lookup cannot be combined with a selection
                selection = null;
                selectionArgs = null;
                if (uri.getBooleanQueryParameter(PhoneLookup.QUERY_PARAMETER_SIP_ADDRESS, false)) {
                    if (TextUtils.isEmpty(sortOrder)) {
                        // Default the sort order to something reasonable so we get consistent
                        // results when callers don't request an ordering
                        sortOrder = Contacts.DISPLAY_NAME + " ASC";
                    }

                    String sipAddress = uri.getPathSegments().size() > 1
                            ? Uri.decode(uri.getLastPathSegment()) : "";
                    setTablesAndProjectionMapForData(qb, uri, null, false, true);
                    StringBuilder sb = new StringBuilder();
                    selectionArgs = mDbHelper.get().buildSipContactQuery(sb, sipAddress);
                    selection = sb.toString();
//                    selection = parseSelection(selection, false); // aurora wangth 20140930 add for privacy
                } else {
//                    selection = parsePhonelookupSelection(selection, false); // aurora wangth 20140930 add for privacy
                    if (TextUtils.isEmpty(sortOrder)) {
                        // Default the sort order to something reasonable so we get consistent
                        // results when callers don't request an ordering
                        sortOrder = " length(lookup.normalized_number) DESC";
                    }

                    String number = uri.getPathSegments().size() > 1
                            ? uri.getLastPathSegment() : "";
                    String numberE164 = PhoneNumberUtils.formatNumberToE164(number,
                            mDbHelper.get().getCurrentCountryIso());
                    String normalizedNumber =
                            PhoneNumberUtils.normalizeNumber(number);
                    mDbHelper.get().buildPhoneLookupAndContactQuery(
                            qb, normalizedNumber, numberE164);
                    qb.setProjectionMap(sPhoneLookupProjectionMap);

                    // Peek at the results of the first query (which attempts to use fully
                    // normalized and internationalized numbers for comparison).  If no results
                    // were returned, fall back to doing a match of the trailing 7 digits.
                    qb.setStrict(true);
                    boolean foundResult = false;
                    Cursor cursor = query(mActiveDb.get(), qb, projection, selection, selectionArgs,
                            sortOrder, groupBy, limit);
                    try {
                        if (cursor.getCount() > 0) {
                            foundResult = true;
                            return cursor;
                        } else {
                            qb = new SQLiteQueryBuilder();
                            mDbHelper.get().buildMinimalPhoneLookupAndContactQuery(
                                    qb, normalizedNumber);
                            qb.setProjectionMap(sPhoneLookupProjectionMap);
                        }
                    } finally {
                        if (!foundResult) {
                            // We'll be returning a different cursor, so close this one.
                            cursor.close();
                        }
                }
                }
                break;
            }

            case GROUPS: {
                qb.setTables(Views.GROUPS);
                qb.setProjectionMap(sGroupsProjectionMap);
                appendAccountFromParameter(qb, uri);
                break;
            }

            case GROUPS_ID: {
                qb.setTables(Views.GROUPS);
                qb.setProjectionMap(sGroupsProjectionMap);
                selectionArgs = insertSelectionArg(selectionArgs, uri.getLastPathSegment());
                qb.appendWhere(Groups._ID + "=?");
                break;
            }

            case GROUPS_SUMMARY: {
//                final boolean returnGroupCountPerAccount =
//                        readBooleanQueryParameter(uri, Groups.PARAM_RETURN_GROUP_COUNT_PER_ACCOUNT,
//                                false);
//                String tables = Views.GROUPS + " AS " + Tables.GROUPS;
//                if (hasColumn(projection, Groups.SUMMARY_COUNT)) {
//                    tables = tables + Joins.GROUP_MEMBER_COUNT;
//                }
//                qb.setTables(tables);
//                qb.setProjectionMap(returnGroupCountPerAccount ?
//                        sGroupsSummaryProjectionMapWithGroupCountPerAccount
//                        : sGroupsSummaryProjectionMap);
//                appendAccountFromParameter(qb, uri);
//                groupBy = GroupsColumns.CONCRETE_ID;
                String tables = Views.GROUPS + " AS " + Tables.GROUPS;
                if (ContactsDatabaseHelper.isInProjection(projection, Groups.SUMMARY_COUNT)) {
                    tables = tables + Joins.GROUP_MEMBER_COUNT;
                }
                if (ContactsDatabaseHelper.isInProjection(projection,
                        Groups.SUMMARY_GROUP_COUNT_PER_ACCOUNT)) {
                    // TODO Add join for this column too (and update the projection map)
                    // TODO Also remove Groups.PARAM_RETURN_GROUP_COUNT_PER_ACCOUNT when it works.
                    Log.w(TAG, Groups.SUMMARY_GROUP_COUNT_PER_ACCOUNT + " is not supported yet");
                }
                qb.setTables(tables);
                qb.setProjectionMap(sGroupsSummaryProjectionMap);
                appendAccountIdFromParameter(qb, uri);
                groupBy = GroupsColumns.CONCRETE_ID;
                break;
            }

            case AGGREGATION_EXCEPTIONS: {
                qb.setTables(Tables.AGGREGATION_EXCEPTIONS);
                qb.setProjectionMap(sAggregationExceptionsProjectionMap);
                break;
            }

            case AGGREGATION_SUGGESTIONS: {
                long contactId = Long.parseLong(uri.getPathSegments().get(1));
                String filter = null;
                if (uri.getPathSegments().size() > 3) {
                    filter = uri.getPathSegments().get(3);
                }
                final int maxSuggestions;
                if (limit != null) {
                    maxSuggestions = Integer.parseInt(limit);
                } else {
                    maxSuggestions = DEFAULT_MAX_SUGGESTIONS;
                }

                ArrayList<AggregationSuggestionParameter> parameters = null;
                List<String> query = uri.getQueryParameters("query");
                if (query != null && !query.isEmpty()) {
                    parameters = new ArrayList<AggregationSuggestionParameter>(query.size());
                    for (String parameter : query) {
                        int offset = parameter.indexOf(':');
                        parameters.add(offset == -1
                                ? new AggregationSuggestionParameter(
                                        AggregationSuggestions.PARAMETER_MATCH_NAME,
                                        parameter)
                                : new AggregationSuggestionParameter(
                                        parameter.substring(0, offset),
                                        parameter.substring(offset + 1)));
                    }
                }

                setTablesAndProjectionMapForContacts(qb, uri, projection);

                // The following lines are provided and maintained by Mediatek inc.
                // FIXED: Android default code could not work well, so change it.
                // Android original code:
                // return mAggregator.get().queryAggregationSuggestions(qb, projection, contactId,
                //        maxSuggestions, filter, parameters);

                if (selection == null) {
                    return mAggregator.get().queryAggregationSuggestions(qb, projection, contactId,
                            maxSuggestions, filter, parameters);
                } else {
                    return mAggregator.get().queryAggregationSuggestions(qb, projection, contactId,
                            maxSuggestions, filter, parameters, selection);
                }
                // The previous lines are provided and maintained by Mediatek inc.
                
            }

            case SETTINGS: {
                qb.setTables(Tables.SETTINGS);
                qb.setProjectionMap(sSettingsProjectionMap);
                appendAccountFromParameter(qb, uri);

                // When requesting specific columns, this query requires
                // late-binding of the GroupMembership MIME-type.
                final String groupMembershipMimetypeId = Long.toString(mDbHelper.get()
                        .getMimeTypeId(GroupMembership.CONTENT_ITEM_TYPE));
                if (projection != null && projection.length != 0 &&
                        mDbHelper.get().isInProjection(projection, Settings.UNGROUPED_COUNT)) {
                    selectionArgs = insertSelectionArg(selectionArgs, groupMembershipMimetypeId);
                }
                if (projection != null && projection.length != 0 &&
                        mDbHelper.get().isInProjection(
                                projection, Settings.UNGROUPED_WITH_PHONES)) {
                    selectionArgs = insertSelectionArg(selectionArgs, groupMembershipMimetypeId);
                }

                break;
            }

            case STATUS_UPDATES:
            case PROFILE_STATUS_UPDATES: {
                setTableAndProjectionMapForStatusUpdates(qb, projection);
                break;
            }

            case STATUS_UPDATES_ID: {
                setTableAndProjectionMapForStatusUpdates(qb, projection);
                selectionArgs = insertSelectionArg(selectionArgs, uri.getLastPathSegment());
                qb.appendWhere(DataColumns.CONCRETE_ID + "=?");
                break;
            }

            case SEARCH_SUGGESTIONS: {
                return mGlobalSearchSupport.handleSearchSuggestionsQuery(
                        mActiveDb.get(), uri, projection, limit);
            }

            case SEARCH_SHORTCUT: {
                String lookupKey = uri.getLastPathSegment();
                String filter = getQueryParameter(
                        uri, SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA);
                return mGlobalSearchSupport.handleSearchShortcutRefresh(
                        mActiveDb.get(), projection, lookupKey, filter);
            }

            case RAW_CONTACT_ENTITIES:
            case PROFILE_RAW_CONTACT_ENTITIES: {
                setTablesAndProjectionMapForRawEntities(qb, uri);
                break;
            }

            case RAW_CONTACT_ENTITY_ID: {
                long rawContactId = Long.parseLong(uri.getPathSegments().get(1));
                setTablesAndProjectionMapForRawEntities(qb, uri);
                selectionArgs = insertSelectionArg(selectionArgs, String.valueOf(rawContactId));
                qb.appendWhere(" AND " + RawContacts._ID + "=?");
                break;
            }

            case PROVIDER_STATUS: {
                return queryProviderStatus(uri, projection);
            }

            case DIRECTORIES : {
                qb.setTables(Tables.DIRECTORIES);
                qb.setProjectionMap(sDirectoryProjectionMap);
                break;
            }

            case DIRECTORIES_ID : {
                long id = ContentUris.parseId(uri);
                qb.setTables(Tables.DIRECTORIES);
                qb.setProjectionMap(sDirectoryProjectionMap);
                selectionArgs = insertSelectionArg(selectionArgs, String.valueOf(id));
                qb.appendWhere(Directory._ID + "=?");
                break;
            }

            case COMPLETE_NAME: {
                return completeName(uri, projection);
            }

            case DELETED_CONTACTS: {
                qb.setTables(Tables.DELETED_CONTACTS);
                qb.setProjectionMap(sDeletedContactsProjectionMap);
                break;
            }

            case DELETED_CONTACTS_ID: {
                String id = uri.getLastPathSegment();
                qb.setTables(Tables.DELETED_CONTACTS);
                qb.setProjectionMap(sDeletedContactsProjectionMap);
                qb.appendWhere(ContactsContract.DeletedContacts.CONTACT_ID + "=?");
                selectionArgs = insertSelectionArg(selectionArgs, id);
                break;
            }
            
        	// The fillowing lines are provided and maintained by Mediatek inc.
            
            case PHONE_EMAIL: {
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                qb.appendWhere(" AND " + "(" + Data.MIMETYPE + " = '" + Email.CONTENT_ITEM_TYPE + "'" +
                        " OR " + Data.MIMETYPE + " = '" + Phone.CONTENT_ITEM_TYPE + "'" + ")");
                break;
            }
            
            case PHONE_EMAIL_FILTER: {
                setTablesAndProjectionMapForData(qb, uri, projection, true);
                String filterParam = null;
                if (uri.getPathSegments().size() > 3) {
                    filterParam = uri.getLastPathSegment();
                    if (TextUtils.isEmpty(filterParam)) {
                        filterParam = null;
                    }
                }

                if (filterParam == null) {
                    // If the filter is unspecified, return nothing
                    qb.appendWhere(" AND 0");
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append(" AND (");

                    boolean orNeeded = false;
                    String normalizedName = NameNormalizer.normalize(filterParam);
                    if (normalizedName.length() > 0 && !filterParam.contains("@")) {
                        sb.append("(" + Data.RAW_CONTACT_ID + " IN ");
                        appendRawContactsByNormalizedNameFilter(sb, normalizedName, false);
                        sb.append(" AND " + Data.MIMETYPE + " IN ('" + Phone.CONTENT_ITEM_TYPE
                                + "' , '" + Email.CONTENT_ITEM_TYPE + "') )");
                        orNeeded = true;
                    }
                    
                    if (isPhoneNumber(filterParam)) {
                        if (orNeeded) {
                            sb.append(" OR ");
                        }
                        String number = PhoneNumberUtils.convertKeypadLettersToDigits(filterParam);
                        sb.append(Data._ID +
                                " IN (SELECT " + PhoneLookupColumns.DATA_ID
                                  + " FROM " + Tables.PHONE_LOOKUP
                                  + " WHERE " + PhoneLookupColumns.NORMALIZED_NUMBER + " LIKE '%");
                        sb.append(number);
                        sb.append("%')");
                        orNeeded = true;
                    }
                    
                    if (orNeeded) {
                        sb.append(" OR ");
                    }
                    long emailMimeType = mDbHelper.get().getMimeTypeId(Email.CONTENT_ITEM_TYPE);
                    sb.append(Data._ID + " IN (");
                    sb.append(
                            "SELECT " + Data._ID +
                            " FROM " + Tables.DATA +
                            " WHERE " + DataColumns.MIMETYPE_ID + "=" + emailMimeType +
                            " AND " + Data.DATA1 + " LIKE ");
                    DatabaseUtils.appendEscapedSQLString(sb, filterParam + '%');
                    sb.append(")");
                    sb.append(")");
                    qb.appendWhere(sb);
                }
                
                groupBy = Email.DATA + "," + RawContacts.CONTACT_ID;
                if (sortOrder == null) {
                    sortOrder = Contacts.IN_VISIBLE_GROUP + " DESC, " + RawContacts.CONTACT_ID;
                }
                break;
            }
           
            case DIALER_SEARCH_SIMPLE: {
            	return null;//queryDialerSearchSimple(mActiveDb.get(), uri);
            }
            
            case DIALER_SEARCH_INCREMENT: {
            	return null;//queryDialerSearchIncrement(mActiveDb.get(), uri);
            }
        	// The previous lines are provided and maintained by Mediatek inc.
            
            //Gionee:huangzy 20121019 modify for CR00715333 start
            /*case MIMETYPES_ID : {
            	qb.setTables(Tables.MIMETYPES);            	           
            	break;
            }
            case DATA_USAG_STAT : {
            	qb.setTables(Tables.DATA_USAGE_STAT);
            	break;
            }*/
            
            //Gionee:huangzy 20121022 add for CR00710695 start
            case GN_DIALER_SEARCH:
				return GnDialerSearchHelper.getInstance().query(mActiveDb.get(), uri, selection);	
			case GN_DIALER_SEARCH_INIT:
				GnDialerSearchHelper.getInstance().init(mActiveDb.get());
				return null;
            //Gionee:huangzy 20121022 add for CR00710695 end
			
			case AURORA_MULTI_HANZI:
			    return GnDialerSearchHelper.getInstance().queryMutil(mActiveDb.get(), uri, selection);
				
			// aurora <wangth> <2013-12-9> add for aurora begin
			case AURORA_SAMSUNG_MATCH:
			    return null;
			// aurora <wangth> <2013-12-9> add for aurora end
			    
			// reject begin
			case BLACKS:
				return mActiveDb.get().query("black", projection, selection, selectionArgs,
						null, null, sortOrder);
			case BLACK:
				// 进行解析，返回值为10
				long blackid = ContentUris.parseId(uri);
				where = "_ID=" + blackid;// 获取指定id的记录
				where += !TextUtils.isEmpty(selection) ? " and (" + selection + ")"
						: "";// 把其它条件附加上
				return mActiveDb.get().query("black", projection, where, selectionArgs, null,
						null, sortOrder);
			case MARKS:
				String groupBY = "lable";
				return mActiveDb.get().query("mark", projection, selection, selectionArgs,
						groupBY, null, "_id");
			case MARK:
				// 进行解析，返回值为10
				long markid = ContentUris.parseId(uri);
				where = "_ID=" + markid;// 获取指定id的记录
				where += !TextUtils.isEmpty(selection) ? " and (" + selection + ")"
						: "";// 把其它条件附加上
				return mActiveDb.get().query("mark", projection, where, selectionArgs, null,
						null, sortOrder);
		    // reject end
			
				
		    //contacts sync begin
				
			case SYNC_ACCESSORY:
				System.out.println("case SYNC_ACCESSORY query:");
				String newPath;
				File file;
				int temPhotoId=10000;
				do{
					temPhotoId++;
					newPath=getContext().getFilesDir()+"/photos/"+temPhotoId;
					file=new File(newPath);
				}while(file.exists());
				if(!file.exists()){
					try {
						file.createNewFile();
					} catch (Exception e) {
						// TODO: handle exception
						System.out.println("e = "+e.toString());
						e.printStackTrace();
					}
					
				}
				System.out.println("file.exists() = "+file.exists());
				String[] pathTable = new String[] {"path"};
				MatrixCursor pathCursor = new MatrixCursor(pathTable);
				pathCursor.addRow(new Object[] {newPath});
				return pathCursor;
				
			case SYNC_DOWN:
				System.out.println("case SYNC_DOWN query:");
				return null;
			case SYNC_UP_LIMIT_COUNT:
				
				System.out.println("case SYNC_UP_LIMIT_COUNT query:");
				return getSnycUpCount(uri.getLastPathSegment(), mContactsHelper.getReadableDatabase());
            case SYNC_UP_LIMIT:
				System.out.println("case SYNC_UP_LIMIT query:");
				String limits=uri.getPathSegments().get(2)+","+uri.getPathSegments().get(3);
				return getSnycUpCount(limits, mContactsHelper.getReadableDatabase());
			case SYNC_UP_SIZE:
				System.out.println("case SYNC_UP_SIZE query:");
				SQLiteDatabase db=mContactsHelper.getWritableDatabase();
				final Long accountId = mDbHelper.get().getAccountIdOrNull(mDbHelper.get().getDefaultAccount());
				Cursor cursorDelete=db.query(Tables.RAW_CONTACTS, new String[]{"_id"}, "deleted=1 and sync4 is null and account_id="+accountId, null, null, null, null);
				System.out.println("cursorDelete.getCount()="+cursorDelete.getCount());
				String deletedId;
				while(cursorDelete.moveToNext()){
					deletedId=cursorDelete.getString(0);
					db.delete(Tables.RAW_CONTACTS, "_id=?", new String[]{deletedId});
					db.delete(Tables.DATA, "raw_contact_id=?", new String[]{deletedId});
				}
				return db.query(Tables.RAW_CONTACTS,
						new String[] { "count() as 'size'" }, "account_id=? and dirty=?",
						new String[] { String.valueOf(accountId),"1" }, null, null, null);
            case SYNC_UP:
            	System.out.println("case SYNC_UP query:");
 				return getSnycUpCount(null, mContactsHelper.getReadableDatabase());
 				
 				
            case URI_IS_FIRST_SYNC:
            	System.out.println("case URI_IS_FIRST_SYNC query:");
            	MatrixCursor cursor = new MatrixCursor(new String[]{"path"},
        				1);
        		Object[] rawObject = new Object[1];
        		boolean isFirstSync = sp.getBoolean("is_first_sync", false);
        		rawObject[0] = !isFirstSync;
        		cursor.addRow(rawObject);
        		if(!isFirstSync){
        	        Editor et = sp.edit();
        	        et.putBoolean("is_first_sync", true);
        	        et.commit();
        		}
        		return cursor;

			case SYNCS:
				System.out.println("case SYNCS query:");
                return null;
			case SYNC:
				System.out.println("case SYNC query:");
				return null;
				
			//contacts sync end
				
            default:
                return mLegacyApiSupport.query(uri, projection, selection, selectionArgs,
                        sortOrder, limit);
        }

        qb.setStrict(true);

        // aurora <wangth> <2013-12-12> add for aurora begin
//        if (match == CONTACTS && null != sortOrder && sortOrder.equals("sort_key")) {
//            sortOrder = "aurora_sort_key";
//        }
        // aurora <wangth> <2013-12-12> add for aurora end

        Cursor cursor =
                query(mActiveDb.get(), qb, projection, selection, selectionArgs, sortOrder, groupBy,
                        limit);
        
        if (readBooleanQueryParameter(uri, ContactCounts.ADDRESS_BOOK_INDEX_EXTRAS, false)) {
            // aurora <wangth> <2013-12-12> modify for aurora begin
            /*
            cursor = bundleLetterCountExtras(cursor, mActiveDb.get(), qb, selection,
                    selectionArgs, sortOrder, addressBookIndexerCountExpression);
            */
            if (match == CONTACTS && cursor != null && cursor.getCount() < 400) {
                cursor = auroraBundleLetterCountExtras(uri, cursor, mActiveDb.get(), qb, selection,
                        selectionArgs, sortOrder, addressBookIndexerCountExpression);
                if (cursor != null) {
                    cursor.setNotificationUri(getContext().getContentResolver(), uri);
                }
            } else {
                cursor = bundleLetterCountExtras(cursor, mActiveDb.get(), qb, selection,
                        selectionArgs, sortOrder, addressBookIndexerCountExpression);
            }
            //  aurora <wangth> <2013-12-12> modify for aurora end
        }
        if (snippetDeferred) {
            cursor = addDeferredSnippetingExtra(cursor);
        }
        return cursor;
    }

    private Cursor query(final SQLiteDatabase db, SQLiteQueryBuilder qb, String[] projection,
            String selection, String[] selectionArgs, String sortOrder, String groupBy,
            String limit) {
        if (projection != null && projection.length == 1
                && BaseColumns._COUNT.equals(projection[0])) {
            qb.setProjectionMap(sCountProjectionMap);
        }
        // Gionee:wangth 20120808 modify for CR00671953 begin
        /*
        final Cursor c = qb.query(db, projection, selection, selectionArgs, groupBy, null,
                sortOrder, limit);
        */
        
        String having=null;
        if(groupBy!=null&&groupBy.equals("data1")){
        	having="COUNT(data1) > 1";
        }
        Cursor c = null;
        try {
            c = qb.query(db, projection, selection, selectionArgs, groupBy, having,
                    sortOrder, limit);
        } catch (Exception e) {
        	Log.e(TAG,"query exception = " + e.toString());
            e.printStackTrace();
        }
        // Gionee:wangth 20120808 modify for CR00671953 end
        if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), GnContactsContract.AUTHORITY_URI);
        }
        return c;
    }

    /**
     * Creates a single-row cursor containing the current status of the provider.
     */
    private Cursor queryProviderStatus(Uri uri, String[] projection) {
        MatrixCursor cursor = new MatrixCursor(projection);
        RowBuilder row = cursor.newRow();
        for (int i = 0; i < projection.length; i++) {
            if (ProviderStatus.STATUS.equals(projection[i])) {
                row.add(mProviderStatus);
            } else if (ProviderStatus.DATA1.equals(projection[i])) {
                row.add(mEstimatedStorageRequirement);
            }
        }
        return cursor;
    }

    /**
     * Runs the query with the supplied contact ID and lookup ID.  If the query succeeds,
     * it returns the resulting cursor, otherwise it returns null and the calling
     * method needs to resolve the lookup key and rerun the query.
     */
    private Cursor queryWithContactIdAndLookupKey(SQLiteQueryBuilder lookupQb,
            SQLiteDatabase db, Uri uri,
            String[] projection, String selection, String[] selectionArgs,
            String sortOrder, String groupBy, String limit,
            String contactIdColumn, long contactId, String lookupKeyColumn, String lookupKey) {
        String[] args;
        if (selectionArgs == null) {
            args = new String[2];
        } else {
            args = new String[selectionArgs.length + 2];
            System.arraycopy(selectionArgs, 0, args, 2, selectionArgs.length);
        }
        args[0] = String.valueOf(contactId);
        args[1] = Uri.encode(lookupKey);
        lookupQb.appendWhere(contactIdColumn + "=? AND " + lookupKeyColumn + "=?");
        Cursor c = query(db, lookupQb, projection, selection, args, sortOrder,
                groupBy, limit);
        if (c.getCount() != 0) {
            return c;
        }

        c.close();
        return null;
    }

    private static final class AddressBookIndexQuery {
        public static final String LETTER = "letter";
        public static final String TITLE = "title";
        public static final String COUNT = "count";

        public static final String[] COLUMNS = new String[] {
                LETTER, TITLE, COUNT
        };

        public static final int COLUMN_LETTER = 0;
        public static final int COLUMN_TITLE = 1;
        public static final int COLUMN_COUNT = 2;

        // The first letter of the sort key column is what is used for the index headings.
        public static final String SECTION_HEADING = "SUBSTR(%1$s,1,1)";

        public static final String ORDER_BY = LETTER + " COLLATE " + PHONEBOOK_COLLATOR_NAME;
    }

    /**
     * Computes counts by the address book index titles and adds the resulting tally
     * to the returned cursor as a bundle of extras.
     */
    private Cursor bundleLetterCountExtras(Cursor cursor, final SQLiteDatabase db,
            SQLiteQueryBuilder qb, String selection, String[] selectionArgs, String sortOrder,
            String countExpression) {
        if (!(cursor instanceof AbstractCursor)) {
            Log.w(TAG, "Unable to bundle extras.  Cursor is not AbstractCursor.");
            return cursor;
        }
        String sortKey;

        // The sort order suffix could be something like "DESC".
        // We want to preserve it in the query even though we will change
        // the sort column itself.
        String sortOrderSuffix = "";
        if (sortOrder != null) {
            int spaceIndex = sortOrder.indexOf(' ');
            if (spaceIndex != -1) {
                sortKey = sortOrder.substring(0, spaceIndex);
                sortOrderSuffix = sortOrder.substring(spaceIndex);
            } else {
                sortKey = sortOrder;
            }
        } else {
            sortKey = Contacts.SORT_KEY_PRIMARY;
        }

//        String locale = getLocale().toString();
        HashMap<String, String> projectionMap = Maps.newHashMap();
        String sectionHeading = String.format(Locale.US, AddressBookIndexQuery.SECTION_HEADING,
                sortKey);
        projectionMap.put(AddressBookIndexQuery.LETTER,
                sectionHeading + " AS " + AddressBookIndexQuery.LETTER);

        // If "what to count" is not specified, we just count all records.
        if (TextUtils.isEmpty(countExpression)) {
            countExpression = "*";
        }

        /**
         * Use the GET_PHONEBOOK_INDEX function, which is an android extension for SQLite3,
         * to map the first letter of the sort key to a character that is traditionally
         * used in phonebooks to represent that letter.  For example, in Korean it will
         * be the first consonant in the letter; for Japanese it will be Hiragana rather
         * than Katakana.
         */
        // aurora <wangth> <2013-12-19> modify for aurora begin
        /*
        projectionMap.put(AddressBookIndexQuery.TITLE,
                "GET_PHONEBOOK_INDEX(" + sectionHeading + ",'" + locale + "')"
                        + " AS " + AddressBookIndexQuery.TITLE);
        */
        projectionMap.put(AddressBookIndexQuery.TITLE,
                sectionHeading + " AS " + AddressBookIndexQuery.TITLE);
        // aurora <wangth> <2013-12-19> modify for aurora end
        projectionMap.put(AddressBookIndexQuery.COUNT,
                "COUNT(" + countExpression + ") AS " + AddressBookIndexQuery.COUNT);
        qb.setProjectionMap(projectionMap);

        Cursor indexCursor = qb.query(db, AddressBookIndexQuery.COLUMNS, selection, selectionArgs,
                AddressBookIndexQuery.ORDER_BY, null /* having */,
                AddressBookIndexQuery.ORDER_BY + sortOrderSuffix);

        try {
            int groupCount = indexCursor.getCount();
            String titles[] = new String[groupCount];
            int counts[] = new int[groupCount];
            int indexCount = 0;
            String currentTitle = null;

            // Since GET_PHONEBOOK_INDEX is a many-to-1 function, we may end up
            // with multiple entries for the same title.  The following code
            // collapses those duplicates.
            for (int i = 0; i < groupCount; i++) {
                indexCursor.moveToNext();
                String title = indexCursor.getString(AddressBookIndexQuery.COLUMN_TITLE);
                
                if (ContactsProvidersApplication.sIsGnContactsSupport) {
                	if (TextUtils.isEmpty(title)) {
                		title = "#";
                	} else if (checkIsNotAZ(title)) {
                        title = "#";
                    } else {
                	    title = title.toUpperCase();
                	}
                }
                int count = indexCursor.getInt(AddressBookIndexQuery.COLUMN_COUNT);
                if (indexCount == 0 || !TextUtils.equals(title, currentTitle)) {
                    titles[indexCount] = currentTitle = title;
                    counts[indexCount] = count;
                    indexCount++;
                } else {
                    counts[indexCount - 1] += count;
                }
            }

            if (indexCount < groupCount) {
                String[] newTitles = new String[indexCount];
                System.arraycopy(titles, 0, newTitles, 0, indexCount);
                titles = newTitles;

                int[] newCounts = new int[indexCount];
                System.arraycopy(counts, 0, newCounts, 0, indexCount);
                counts = newCounts;
            }

            final Bundle bundle = new Bundle();
            bundle.putStringArray(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_TITLES, titles);
            bundle.putIntArray(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_COUNTS, counts);

            ((AbstractCursor) cursor).setExtras(bundle);
            return cursor;
        } finally {
            indexCursor.close();
        }
    }
    
    // aurora <wangth> <2013-12-12> add for aurora begin
    private Cursor auroraBundleLetterCountExtras(Uri uri, Cursor cursor, final SQLiteDatabase db,
            SQLiteQueryBuilder qb, String selection, String[] selectionArgs, String sortOrder,
            String countExpression) {

        if (!(cursor instanceof AbstractCursor)) {
            Log.w(TAG, "Unable to bundle extras.  Cursor is not AbstractCursor.");
            return cursor;
        }
        String sortKey;

        // The sort order suffix could be something like "DESC".
        // We want to preserve it in the query even though we will change
        // the sort column itself.
        String sortOrderSuffix = "";
        if (sortOrder != null) {
            int spaceIndex = sortOrder.indexOf(' ');
            if (spaceIndex != -1) {
                sortKey = sortOrder.substring(0, spaceIndex);
                sortOrderSuffix = sortOrder.substring(spaceIndex);
            } else {
                sortKey = sortOrder;
            }
        } else {
            sortKey = Contacts.SORT_KEY_PRIMARY;
        }

//        String locale = getLocale().toString();
        HashMap<String, String> projectionMap = Maps.newHashMap();
        String sectionHeading = String.format(Locale.US, AddressBookIndexQuery.SECTION_HEADING,
                sortKey);
        projectionMap.put(AddressBookIndexQuery.LETTER,
                sectionHeading + " AS " + AddressBookIndexQuery.LETTER);

        // If "what to count" is not specified, we just count all records.
        if (TextUtils.isEmpty(countExpression)) {
            countExpression = "*";
        }

        /**
         * Use the GET_PHONEBOOK_INDEX function, which is an android extension for SQLite3,
         * to map the first letter of the sort key to a character that is traditionally
         * used in phonebooks to represent that letter.  For example, in Korean it will
         * be the first consonant in the letter; for Japanese it will be Hiragana rather
         * than Katakana.
         */
        projectionMap.put(AddressBookIndexQuery.TITLE,
                sectionHeading + " AS " + AddressBookIndexQuery.TITLE);
        projectionMap.put(AddressBookIndexQuery.COUNT,
                "COUNT(" + countExpression + ") AS " + AddressBookIndexQuery.COUNT);
        qb.setProjectionMap(projectionMap);

        Cursor indexCursor = qb.query(db, AddressBookIndexQuery.COLUMNS, selection, selectionArgs,
                AddressBookIndexQuery.ORDER_BY, null /* having */,
                AddressBookIndexQuery.ORDER_BY + sortOrderSuffix);

        try {
            int groupCount = indexCursor.getCount();
            String titles[] = new String[groupCount];
            int counts[] = new int[groupCount];
            int indexCount = 0;
            String currentTitle = null;
            
            MatrixCursor cursorOrig = new MatrixCursor(cursor.getColumnNames());
            MatrixCursor matCursor = new MatrixCursor(cursor.getColumnNames());
            ArrayList<Object[]> ObjList = new ArrayList<Object[]>();
            int columnSize = cursor.getColumnNames().length;
            int pinyinPosition = 0;
            int groupStartPosition = 0;
            int fuHaoCount = 0;

            // Since GET_PHONEBOOK_INDEX is a many-to-1 function, we may end up
            // with multiple entries for the same title.  The following code
            // collapses those duplicates.

        	int nameIndex = cursor.getColumnIndex(Contacts.DISPLAY_NAME);
        	if(nameIndex >= 0){
                for (int index = cursor.getCount() - 1; index >= 0; index--) {
                    cursor.moveToPosition(index);
                    String name = cursor.getString(nameIndex);
                    if (name != null && ContactsDatabaseHelper.firstIsFuHao(name.substring(0, 1))) {
                        Object[] obj = new Object[columnSize];
                        for (int jj = 0; jj < columnSize; jj++) {
                            obj[jj] = cursor.getString(jj);
                        }
                        ObjList.add(0, obj);
                        fuHaoCount++;
                    } else {
                        break;
                    }
                }
        	}
            
            cursor.moveToFirst();
            for (int index = 0; index < cursor.getCount() - fuHaoCount; index++) {
                Object[] obj = new Object[columnSize];
                for (int jj = 0; jj < columnSize; jj++) {
                    obj[jj] = cursor.getString(jj);
                }
                ObjList.add(obj);
                cursor.moveToNext();
            }
            
            for (Object[] obj : ObjList) {
                cursorOrig.addRow(obj);
            }
            
            ObjList.clear();
            cursor = (Cursor)cursorOrig;
            cursor.moveToFirst();
            for (int i = 0; i < groupCount; i++) {
                indexCursor.moveToNext();
                String title = indexCursor.getString(AddressBookIndexQuery.COLUMN_TITLE);
                
                if (TextUtils.isEmpty(title)) {
                    title = "#";
                } else if (checkIsNotAZ(title)) {
                    title = "#";
                } else {
                    title = title.toUpperCase();
                }
                
                int count = indexCursor.getInt(AddressBookIndexQuery.COLUMN_COUNT);
//                Log.d(TAG, "title = " + title + "  checkIsNotAZ(title) = " + checkIsNotAZ(title) + "  count = " + count);
                if (indexCount == 0 || !TextUtils.equals(title, currentTitle)) {
                    titles[indexCount] = currentTitle = title;
                    counts[indexCount] = count;
                    indexCount++;
                } else {
                    counts[indexCount - 1] += count;
                }
                
                pinyinPosition = groupStartPosition;
                groupStartPosition += count;
                
                if (title.equals("#")) {
                    for (int j = 0; j < count; j++) {
                        Object[] obj = new Object[columnSize];
                        for (int ii = 0; ii < columnSize; ii++) {
                            obj[ii] = cursor.getString(ii);
//                            Log.d(TAG, "obj[ii] = " + obj[ii]);
                        }
                        
                        ObjList.add(obj);
                        cursor.moveToNext();
                        pinyinPosition++;
                    }
                } else {
                    for (int j = 0; j < count; j++) {
                        boolean firstCharIsPinyin = false;
                        /*boolean firstCharIsHanzi = false;*/
                        Object[] obj = new Object[columnSize];
                        for (int jj = 0; jj < columnSize; jj++) {
                            obj[jj] = cursor.getString(jj);
                        }
                        
                        if (obj[1] != null) {
                            String str = obj[1].toString();
                            char firstChar = str.charAt(0);
                            if ((firstChar >= 'a' && firstChar <= 'z') || (firstChar >= 'A' && firstChar <= 'Z')) {
                                firstCharIsPinyin = true;
                            }/* else if (ContactsDatabaseHelper.hasHanZi(String.valueOf(firstChar))) {
                                firstCharIsHanzi = true;
                            }*/  
                            
                            if (firstCharIsPinyin) {
                                // move to the right position
                                ObjList.add(pinyinPosition, obj);
                            } else /*if (firstCharIsHanzi)*/ {
                                ObjList.add(obj);
                            }/* else {
                                ObjList.add(0, obj);
                            }*/
                            
                            if (firstCharIsPinyin) {
                                pinyinPosition++;
                            }
                        }
                        
                        cursor.moveToNext();
                    }
                }
            }
            
            for (Object[] obj : ObjList) {
                matCursor.addRow(obj);
            }

            if (indexCount < groupCount) {
                String[] newTitles = new String[indexCount];
                System.arraycopy(titles, 0, newTitles, 0, indexCount);
                titles = newTitles;

                int[] newCounts = new int[indexCount];
                System.arraycopy(counts, 0, newCounts, 0, indexCount);
                counts = newCounts;
            }

            final Bundle bundle = new Bundle();
            bundle.putStringArray(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_TITLES, titles);
            bundle.putIntArray(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_COUNTS, counts);

            cursor = matCursor;
            cursor.setNotificationUri(getContext().getContentResolver(), uri); // must match uri
            ((AbstractCursor) cursor).setExtras(bundle);
            if (matCursor != null) {
                matCursor.close();
            }
            if (cursorOrig != null) {
                cursorOrig.close();
            }

            return cursor;
        } finally {
            indexCursor.close();
            cursor.close();
        }
    }
    
    private boolean checkIsNotAZ(String str) {
        if (str == null) {
            return true;
        }
        boolean result = false;
        char c = str.charAt(0);
        if (!('a' <= c && c <= 'z') && !('A' <= c && c <= 'Z')) {
            result = true;
        }
        
        return result;
    }
    // aurora <wangth> <2013-12-12> add for aurora end

    /**
     * Returns the contact Id for the contact identified by the lookupKey.
     * Robust against changes in the lookup key: if the key has changed, will
     * look up the contact by the raw contact IDs or name encoded in the lookup
     * key.
     */
    public long lookupContactIdByLookupKey(SQLiteDatabase db, String lookupKey) {
        ContactLookupKey key = new ContactLookupKey();
        ArrayList<LookupKeySegment> segments = key.parse(lookupKey);

        long contactId = -1;
        if (lookupKeyContainsType(segments, ContactLookupKey.LOOKUP_TYPE_PROFILE)) {
            // We should already be in a profile database context, so just look up a single contact.
           contactId = lookupSingleContactId(db);
        }

        if (lookupKeyContainsType(segments, ContactLookupKey.LOOKUP_TYPE_SOURCE_ID)) {
            contactId = lookupContactIdBySourceIds(db, segments);
            if (contactId != -1) {
                return contactId;
            }
        }

        boolean hasRawContactIds =
                lookupKeyContainsType(segments, ContactLookupKey.LOOKUP_TYPE_RAW_CONTACT_ID);
        if (hasRawContactIds) {
            contactId = lookupContactIdByRawContactIds(db, segments);
            if (contactId != -1) {
                return contactId;
            }
        }

        if (hasRawContactIds
                || lookupKeyContainsType(segments, ContactLookupKey.LOOKUP_TYPE_DISPLAY_NAME)) {
            contactId = lookupContactIdByDisplayNames(db, segments);
        }

        return contactId;
    }

    private long lookupSingleContactId(SQLiteDatabase db) {
        Cursor c = db.query(Tables.CONTACTS, new String[] {Contacts._ID},
                null, null, null, null, null, "1");
        try {
            if (c.moveToFirst()) {
                return c.getLong(0);
            } else {
                return -1;
            }
        } finally {
            c.close();
        }
    }

    private interface LookupBySourceIdQuery {
        String TABLE = Views.RAW_CONTACTS;

        String COLUMNS[] = {
                RawContacts.CONTACT_ID,
                RawContacts.ACCOUNT_TYPE_AND_DATA_SET,
                RawContacts.ACCOUNT_NAME,
                RawContacts.SOURCE_ID
        };

        int CONTACT_ID = 0;
        int ACCOUNT_TYPE_AND_DATA_SET = 1;
        int ACCOUNT_NAME = 2;
        int SOURCE_ID = 3;
    }

    private long lookupContactIdBySourceIds(SQLiteDatabase db,
                ArrayList<LookupKeySegment> segments) {
        StringBuilder sb = new StringBuilder();
        sb.append(RawContacts.SOURCE_ID + " IN (");
        for (int i = 0; i < segments.size(); i++) {
            LookupKeySegment segment = segments.get(i);
            if (segment.lookupType == ContactLookupKey.LOOKUP_TYPE_SOURCE_ID) {
                DatabaseUtils.appendEscapedSQLString(sb, segment.key);
                sb.append(",");
            }
        }
        sb.setLength(sb.length() - 1);      // Last comma
        sb.append(") AND " + RawContacts.CONTACT_ID + " NOT NULL");

        Cursor c = db.query(LookupBySourceIdQuery.TABLE, LookupBySourceIdQuery.COLUMNS,
                 sb.toString(), null, null, null, null);
        try {
            while (c.moveToNext()) {
                String accountTypeAndDataSet =
                        c.getString(LookupBySourceIdQuery.ACCOUNT_TYPE_AND_DATA_SET);
                String accountName = c.getString(LookupBySourceIdQuery.ACCOUNT_NAME);
                int accountHashCode =
                        ContactLookupKey.getAccountHashCode(accountTypeAndDataSet, accountName);
                String sourceId = c.getString(LookupBySourceIdQuery.SOURCE_ID);
                for (int i = 0; i < segments.size(); i++) {
                    LookupKeySegment segment = segments.get(i);
                    if (segment.lookupType == ContactLookupKey.LOOKUP_TYPE_SOURCE_ID
                            && accountHashCode == segment.accountHashCode
                            && segment.key.equals(sourceId)) {
                        segment.contactId = c.getLong(LookupBySourceIdQuery.CONTACT_ID);
                        break;
                    }
                }
            }
        } finally {
            c.close();
        }

        return getMostReferencedContactId(segments);
    }

    private interface LookupByRawContactIdQuery {
        String TABLE = Views.RAW_CONTACTS;

        String COLUMNS[] = {
                RawContacts.CONTACT_ID,
                RawContacts.ACCOUNT_TYPE_AND_DATA_SET,
                RawContacts.ACCOUNT_NAME,
                RawContacts._ID,
        };

        int CONTACT_ID = 0;
        int ACCOUNT_TYPE_AND_DATA_SET = 1;
        int ACCOUNT_NAME = 2;
        int ID = 3;
    }

    private long lookupContactIdByRawContactIds(SQLiteDatabase db,
            ArrayList<LookupKeySegment> segments) {
        StringBuilder sb = new StringBuilder();
        sb.append(RawContacts._ID + " IN (");
        for (int i = 0; i < segments.size(); i++) {
            LookupKeySegment segment = segments.get(i);
            if (segment.lookupType == ContactLookupKey.LOOKUP_TYPE_RAW_CONTACT_ID) {
                sb.append(segment.rawContactId);
                sb.append(",");
            }
        }
        sb.setLength(sb.length() - 1);      // Last comma
        sb.append(") AND " + RawContacts.CONTACT_ID + " NOT NULL");

        Cursor c = db.query(LookupByRawContactIdQuery.TABLE, LookupByRawContactIdQuery.COLUMNS,
                 sb.toString(), null, null, null, null);
        try {
            while (c.moveToNext()) {
                String accountTypeAndDataSet = c.getString(
                        LookupByRawContactIdQuery.ACCOUNT_TYPE_AND_DATA_SET);
                String accountName = c.getString(LookupByRawContactIdQuery.ACCOUNT_NAME);
                int accountHashCode =
                        ContactLookupKey.getAccountHashCode(accountTypeAndDataSet, accountName);
                String rawContactId = c.getString(LookupByRawContactIdQuery.ID);
                for (int i = 0; i < segments.size(); i++) {
                    LookupKeySegment segment = segments.get(i);
                    if (segment.lookupType == ContactLookupKey.LOOKUP_TYPE_RAW_CONTACT_ID
                            && accountHashCode == segment.accountHashCode
                            && segment.rawContactId.equals(rawContactId)) {
                        segment.contactId = c.getLong(LookupByRawContactIdQuery.CONTACT_ID);
                        break;
                    }
                }
            }
        } finally {
            c.close();
        }

        return getMostReferencedContactId(segments);
    }

    private interface LookupByDisplayNameQuery {
        String TABLE = Tables.NAME_LOOKUP_JOIN_RAW_CONTACTS;

        String COLUMNS[] = {
                RawContacts.CONTACT_ID,
                RawContacts.ACCOUNT_TYPE_AND_DATA_SET,
                RawContacts.ACCOUNT_NAME,
                NameLookupColumns.NORMALIZED_NAME
        };

        int CONTACT_ID = 0;
        int ACCOUNT_TYPE_AND_DATA_SET = 1;
        int ACCOUNT_NAME = 2;
        int NORMALIZED_NAME = 3;
    }

    private long lookupContactIdByDisplayNames(SQLiteDatabase db,
                ArrayList<LookupKeySegment> segments) {
        StringBuilder sb = new StringBuilder();
        sb.append(NameLookupColumns.NORMALIZED_NAME + " IN (");
        for (int i = 0; i < segments.size(); i++) {
            LookupKeySegment segment = segments.get(i);
            if (segment.lookupType == ContactLookupKey.LOOKUP_TYPE_DISPLAY_NAME
                    || segment.lookupType == ContactLookupKey.LOOKUP_TYPE_RAW_CONTACT_ID) {
                DatabaseUtils.appendEscapedSQLString(sb, segment.key);
                sb.append(",");
            }
        }
        sb.setLength(sb.length() - 1);      // Last comma
        sb.append(") AND " + NameLookupColumns.NAME_TYPE + "=" + NameLookupType.NAME_COLLATION_KEY
                + " AND " + RawContacts.CONTACT_ID + " NOT NULL");

        Cursor c = db.query(LookupByDisplayNameQuery.TABLE, LookupByDisplayNameQuery.COLUMNS,
                 sb.toString(), null, null, null, null);
        try {
            while (c.moveToNext()) {
                String accountTypeAndDataSet =
                        c.getString(LookupByDisplayNameQuery.ACCOUNT_TYPE_AND_DATA_SET);
                String accountName = c.getString(LookupByDisplayNameQuery.ACCOUNT_NAME);
                int accountHashCode =
                        ContactLookupKey.getAccountHashCode(accountTypeAndDataSet, accountName);
                String name = c.getString(LookupByDisplayNameQuery.NORMALIZED_NAME);
                for (int i = 0; i < segments.size(); i++) {
                    LookupKeySegment segment = segments.get(i);
                    if ((segment.lookupType == ContactLookupKey.LOOKUP_TYPE_DISPLAY_NAME
                            || segment.lookupType == ContactLookupKey.LOOKUP_TYPE_RAW_CONTACT_ID)
                            && accountHashCode == segment.accountHashCode
                            && segment.key.equals(name)) {
                        segment.contactId = c.getLong(LookupByDisplayNameQuery.CONTACT_ID);
                        break;
                    }
                }
            }
        } finally {
            c.close();
        }

        return getMostReferencedContactId(segments);
    }

    private boolean lookupKeyContainsType(ArrayList<LookupKeySegment> segments, int lookupType) {
        for (int i = 0; i < segments.size(); i++) {
            LookupKeySegment segment = segments.get(i);
            if (segment.lookupType == lookupType) {
                return true;
            }
        }

        return false;
    }

    public void updateLookupKeyForRawContact(SQLiteDatabase db, long rawContactId) {
        mAggregator.get().updateLookupKeyForRawContact(db, rawContactId);
    }

    /**
     * Returns the contact ID that is mentioned the highest number of times.
     */
    private long getMostReferencedContactId(ArrayList<LookupKeySegment> segments) {
        Collections.sort(segments);

        long bestContactId = -1;
        int bestRefCount = 0;

        long contactId = -1;
        int count = 0;

        int segmentCount = segments.size();
        for (int i = 0; i < segmentCount; i++) {
            LookupKeySegment segment = segments.get(i);
            if (segment.contactId != -1) {
                if (segment.contactId == contactId) {
                    count++;
                } else {
                    if (count > bestRefCount) {
                        bestContactId = contactId;
                        bestRefCount = count;
                    }
                    contactId = segment.contactId;
                    count = 1;
                }
            }
        }
        if (count > bestRefCount) {
            return contactId;
        } else {
            return bestContactId;
        }
    }

    private void setTablesAndProjectionMapForContacts(SQLiteQueryBuilder qb, Uri uri,
            String[] projection) {
        setTablesAndProjectionMapForContacts(qb, uri, projection, false);
    }

    /**
     * @param includeDataUsageStat true when the table should include DataUsageStat table.
     * Note that this uses INNER JOIN instead of LEFT OUTER JOIN, so some of data in Contacts
     * may be dropped.
     */
    private void setTablesAndProjectionMapForContacts(SQLiteQueryBuilder qb, Uri uri,
            String[] projection, boolean includeDataUsageStat) {
        StringBuilder sb = new StringBuilder();
        if (includeDataUsageStat) {
            sb.append(Views.DATA_USAGE_STAT + " AS " + Tables.DATA_USAGE_STAT);
            sb.append(" INNER JOIN ");
        }

        sb.append(Views.CONTACTS);

        // Just for frequently contacted contacts in Strequent Uri handling.
        if (includeDataUsageStat) {
            sb.append(" ON (" +
                    DbQueryUtils.concatenateClauses(
                            DataUsageStatColumns.CONCRETE_TIMES_USED + " > 0",
                            RawContacts.CONTACT_ID + "=" + Views.CONTACTS + "." + Contacts._ID) +
                    ")");
        }

        appendContactPresenceJoin(sb, projection, Contacts._ID);
        appendContactStatusUpdateJoin(sb, projection, ContactsColumns.LAST_STATUS_UPDATE_ID);
        qb.setTables(sb.toString());
        qb.setProjectionMap(sContactsProjectionMap);
    }

    /**
     * Finds name lookup records matching the supplied filter, picks one arbitrary match per
     * contact and joins that with other contacts tables.
     */
    private void setTablesAndProjectionMapForContactsWithSnippet(SQLiteQueryBuilder qb, Uri uri,
            String[] projection, String filter, long directoryId, boolean deferredSnippeting) {

        StringBuilder sb = new StringBuilder();
        sb.append(Views.CONTACTS);

        if (filter != null) {
            filter = filter.trim();
        }

        if (TextUtils.isEmpty(filter) || (directoryId != -1 && directoryId != Directory.DEFAULT)) {
            sb.append(" JOIN (SELECT NULL AS " + SearchSnippetColumns.SNIPPET + " WHERE 0)");
        } else {
            appendSearchIndexJoin(sb, uri, projection, filter, deferredSnippeting);
        }
        appendContactPresenceJoin(sb, projection, Contacts._ID);
        appendContactStatusUpdateJoin(sb, projection, ContactsColumns.LAST_STATUS_UPDATE_ID);
        qb.setTables(sb.toString());
        qb.setProjectionMap(sContactsProjectionWithSnippetMap);
    }

    private void appendSearchIndexJoin(
            StringBuilder sb, Uri uri, String[] projection, String filter,
            boolean  deferredSnippeting) {

        if (snippetNeeded(projection)) {
            String[] args = null;
            String snippetArgs =
                    getQueryParameter(uri, SearchSnippetColumns.SNIPPET_ARGS_PARAM_KEY);
            if (snippetArgs != null) {
                args = snippetArgs.split(",");
            }

            String startMatch = args != null && args.length > 0 ? args[0]
                    : DEFAULT_SNIPPET_ARG_START_MATCH;
            String endMatch = args != null && args.length > 1 ? args[1]
                    : DEFAULT_SNIPPET_ARG_END_MATCH;
            String ellipsis = args != null && args.length > 2 ? args[2]
                    : DEFAULT_SNIPPET_ARG_ELLIPSIS;
            int maxTokens = args != null && args.length > 3 ? Integer.parseInt(args[3])
                    : DEFAULT_SNIPPET_ARG_MAX_TOKENS;

            appendSearchIndexJoin(
                    sb, filter, true, startMatch, endMatch, ellipsis, maxTokens,
                    deferredSnippeting);
        } else {
            appendSearchIndexJoin(sb, filter, false, null, null, null, 0, false);
        }
    }

    public void appendSearchIndexJoin(StringBuilder sb, String filter,
            boolean snippetNeeded, String startMatch, String endMatch, String ellipsis,
            int maxTokens, boolean deferredSnippeting) {
        boolean isEmailAddress = false;
        String emailAddress = null;
        boolean isPhoneNumber = false;
        String phoneNumber = null;
        String numberE164 = null;

        // If the query consists of a single word, we can do snippetizing after-the-fact for a
        // performance boost.
        boolean singleTokenSearch = isSingleWordQuery(filter);

        if (filter.indexOf('@') != -1) {
            emailAddress = mDbHelper.get().extractAddressFromEmailAddress(filter);
            isEmailAddress = !TextUtils.isEmpty(emailAddress);
        } else {
            isPhoneNumber = isPhoneNumber(filter);
            if (isPhoneNumber) {
                phoneNumber = PhoneNumberUtils.normalizeNumber(filter);
                numberE164 = PhoneNumberUtils.formatNumberToE164(phoneNumber,
                        mDbHelper.get().getCountryIso());
            }
        }

        final String SNIPPET_CONTACT_ID = "snippet_contact_id";
        sb.append(" JOIN (SELECT " + SearchIndexColumns.CONTACT_ID + " AS " + SNIPPET_CONTACT_ID);
        if (snippetNeeded) {
            sb.append(", ");
            if (isEmailAddress) {
                sb.append("ifnull(");
                DatabaseUtils.appendEscapedSQLString(sb, startMatch);
                sb.append("||(SELECT MIN(" + Email.ADDRESS + ")");
                sb.append(" FROM " + Tables.DATA_JOIN_RAW_CONTACTS);
                sb.append(" WHERE  " + Tables.SEARCH_INDEX + "." + SearchIndexColumns.CONTACT_ID);
                sb.append("=" + RawContacts.CONTACT_ID + " AND " + Email.ADDRESS + " LIKE ");
                DatabaseUtils.appendEscapedSQLString(sb, filter + "%");
                sb.append(")||");
                DatabaseUtils.appendEscapedSQLString(sb, endMatch);
                sb.append(",");

                // Optimization for single-token search (do only if requested).
                if (singleTokenSearch && deferredSnippeting) {
                    sb.append(SearchIndexColumns.CONTENT);
                } else {
                    appendSnippetFunction(sb, startMatch, endMatch, ellipsis, maxTokens);
                }
                sb.append(")");
            } else if (isPhoneNumber) {
                sb.append("ifnull(");
                DatabaseUtils.appendEscapedSQLString(sb, startMatch);
                sb.append("||(SELECT MIN(" + Phone.NUMBER + ")");
                sb.append(" FROM " +
                        Tables.DATA_JOIN_RAW_CONTACTS + " JOIN " + Tables.PHONE_LOOKUP);
                sb.append(" ON " + DataColumns.CONCRETE_ID);
                sb.append("=" + Tables.PHONE_LOOKUP + "." + PhoneLookupColumns.DATA_ID);
                sb.append(" WHERE  " + Tables.SEARCH_INDEX + "." + SearchIndexColumns.CONTACT_ID);
                sb.append("=" + RawContacts.CONTACT_ID);
                sb.append(" AND " + PhoneLookupColumns.NORMALIZED_NUMBER + " LIKE '%"); // aurora wangth 20140710 add %
                sb.append(phoneNumber);
                sb.append("%'");
//                if (!TextUtils.isEmpty(numberE164)) {
//                    sb.append(" OR " + PhoneLookupColumns.NORMALIZED_NUMBER + " LIKE '%"); //  aurora wangth 20141107 remove for bug:9186
//                    sb.append(numberE164);
//                    sb.append("%'");
//                }
                sb.append(")||");
                DatabaseUtils.appendEscapedSQLString(sb, endMatch);
                sb.append(",");

                // Optimization for single-token search (do only if requested).
                if (singleTokenSearch && deferredSnippeting) {
                    sb.append(SearchIndexColumns.CONTENT);
                } else {
                    appendSnippetFunction(sb, startMatch, endMatch, ellipsis, maxTokens);
                }
                sb.append(")");
            } else {
                final String normalizedFilter = NameNormalizer.normalize(filter);
                if (!TextUtils.isEmpty(normalizedFilter)) {
                    // Optimization for single-token search (do only if requested)..
                    if (singleTokenSearch && deferredSnippeting) {
                        sb.append(SearchIndexColumns.CONTENT);
                    } else {
                        sb.append("(CASE WHEN EXISTS (SELECT 1 FROM ");
                        sb.append(Tables.RAW_CONTACTS + " AS rc INNER JOIN ");
                        sb.append(Tables.NAME_LOOKUP + " AS nl ON (rc." + RawContacts._ID);
                        sb.append("=nl." + NameLookupColumns.RAW_CONTACT_ID);
                        sb.append(") WHERE nl." + NameLookupColumns.NORMALIZED_NAME);
                        sb.append(" GLOB '" + normalizedFilter + "*' AND ");
                        sb.append("nl." + NameLookupColumns.NAME_TYPE + "=");
                        sb.append(NameLookupType.NAME_COLLATION_KEY + " AND ");
                        sb.append(Tables.SEARCH_INDEX + "." + SearchIndexColumns.CONTACT_ID);
                        sb.append("=rc." + RawContacts.CONTACT_ID);
                        sb.append(") THEN NULL ELSE ");
                        appendSnippetFunction(sb, startMatch, endMatch, ellipsis, maxTokens);
                        sb.append(" END)");
                    }
                } else {
                    sb.append("NULL");
                }
            }
            sb.append(" AS " + SearchSnippetColumns.SNIPPET);
        }

        sb.append(" FROM " + Tables.SEARCH_INDEX);
        sb.append(" WHERE ");
        sb.append(Tables.SEARCH_INDEX + " MATCH '");
        if (isEmailAddress) {
            // we know that the emailAddress contains a @. This phrase search should be
            // scoped against "content:" only, but unfortunately SQLite doesn't support
            // phrases and scoped columns at once. This is fine in this case however, because:
            //  - We can't erronously match against name, as name is all-hex (so the @ can't match)
            //  - We can't match against tokens, because phone-numbers can't contain @
            final String sanitizedEmailAddress =
                    emailAddress == null ? "" : sanitizeMatch(emailAddress);
            sb.append("\"");
            sb.append(sanitizedEmailAddress);
            sb.append("*\"");
        } else if (isPhoneNumber) {
            // normalized version of the phone number (phoneNumber can only have + and digits)
            final String phoneNumberCriteria = " OR tokens:" + phoneNumber + "*";

            // international version of this number (numberE164 can only have + and digits)
            final String numberE164Criteria =
                    (numberE164 != null && !TextUtils.equals(numberE164, phoneNumber))
                    ? " OR tokens:" + numberE164 + "*"
                    : "";

            // combine all criteria
            final String commonCriteria =
                    phoneNumberCriteria + numberE164Criteria;

            // search in content
            sb.append(SearchIndexManager.getFtsMatchQuery(filter,
                    FtsQueryBuilder.getDigitsQueryBuilder(commonCriteria)));
        } else {
            // general case: not a phone number, not an email-address
            sb.append(SearchIndexManager.getFtsMatchQuery(filter,
                    FtsQueryBuilder.SCOPED_NAME_NORMALIZING));
        }
        // Omit results in "Other Contacts".
        sb.append("' AND " + SNIPPET_CONTACT_ID + " IN " + Tables.DEFAULT_DIRECTORY + ")");
        sb.append(" ON (" + Contacts._ID + "=" + SNIPPET_CONTACT_ID + ")");
    }

    private static String sanitizeMatch(String filter) {
        return filter.replace("'", "").replace("*", "").replace("-", "").replace("\"", "");
    }

    private void appendSnippetFunction(
            StringBuilder sb, String startMatch, String endMatch, String ellipsis, int maxTokens) {
        sb.append("snippet(" + Tables.SEARCH_INDEX + ",");
        DatabaseUtils.appendEscapedSQLString(sb, startMatch);
        sb.append(",");
        DatabaseUtils.appendEscapedSQLString(sb, endMatch);
        sb.append(",");
        DatabaseUtils.appendEscapedSQLString(sb, ellipsis);

        // The index of the column used for the snippet, "content"
        sb.append(",1,");
        sb.append(maxTokens);
        sb.append(")");
    }

    private void setTablesAndProjectionMapForRawContacts(SQLiteQueryBuilder qb, Uri uri) {
        StringBuilder sb = new StringBuilder();
        sb.append(Views.RAW_CONTACTS);
        qb.setTables(sb.toString());
        qb.setProjectionMap(sRawContactsProjectionMap);
        appendAccountIdFromParameter(qb, uri);
    }

    private void setTablesAndProjectionMapForRawEntities(SQLiteQueryBuilder qb, Uri uri) {
        qb.setTables(Views.RAW_ENTITIES);
        qb.setProjectionMap(sRawEntityProjectionMap);
        appendAccountFromParameter(qb, uri);
    }

    private void setTablesAndProjectionMapForData(SQLiteQueryBuilder qb, Uri uri,
            String[] projection, boolean distinct) {
        setTablesAndProjectionMapForData(qb, uri, projection, distinct, false, null);
    }

    private void setTablesAndProjectionMapForData(SQLiteQueryBuilder qb, Uri uri,
            String[] projection, boolean distinct, boolean addSipLookupColumns) {
        setTablesAndProjectionMapForData(qb, uri, projection, distinct, addSipLookupColumns, null);
    }

    /**
     * @param usageType when non-null {@link Tables#DATA_USAGE_STAT} is joined with the specified
     * type.
     */
    private void setTablesAndProjectionMapForData(SQLiteQueryBuilder qb, Uri uri,
            String[] projection, boolean distinct, Integer usageType) {
        setTablesAndProjectionMapForData(qb, uri, projection, distinct, false, usageType);
    }

    private void setTablesAndProjectionMapForData(SQLiteQueryBuilder qb, Uri uri,
            String[] projection, boolean distinct, boolean addSipLookupColumns, Integer usageType) {
        StringBuilder sb = new StringBuilder();
        sb.append(Views.DATA);
        sb.append(" data");

        appendContactPresenceJoin(sb, projection, RawContacts.CONTACT_ID);
        appendContactStatusUpdateJoin(sb, projection, ContactsColumns.LAST_STATUS_UPDATE_ID);
        appendDataPresenceJoin(sb, projection, DataColumns.CONCRETE_ID);
        appendDataStatusUpdateJoin(sb, projection, DataColumns.CONCRETE_ID);

        appendDataUsageStatJoin(sb, usageType == null ? USAGE_TYPE_ALL : usageType,
                DataColumns.CONCRETE_ID);
        qb.setTables(sb.toString());

        boolean useDistinct = distinct
                || !mDbHelper.get().isInProjection(projection, DISTINCT_DATA_PROHIBITING_COLUMNS);
        qb.setDistinct(useDistinct);

        final ProjectionMap projectionMap;
        
        if (addSipLookupColumns) {
            projectionMap = useDistinct
                    ? sDistinctDataSipLookupProjectionMap : sDataSipLookupProjectionMap;
        } else {
            projectionMap = useDistinct ? sDistinctDataProjectionMap : sDataProjectionMap;
        }

        qb.setProjectionMap(projectionMap);
        appendAccountFromParameter(qb, uri);
    }

    private void setTableAndProjectionMapForStatusUpdates(SQLiteQueryBuilder qb,
            String[] projection) {
        StringBuilder sb = new StringBuilder();
        sb.append(Views.DATA);
        sb.append(" data");
        appendDataPresenceJoin(sb, projection, DataColumns.CONCRETE_ID);
        appendDataStatusUpdateJoin(sb, projection, DataColumns.CONCRETE_ID);

        qb.setTables(sb.toString());
        qb.setProjectionMap(sStatusUpdatesProjectionMap);
    }

    private void setTablesAndProjectionMapForStreamItems(SQLiteQueryBuilder qb) {
        qb.setTables(Views.STREAM_ITEMS);
        qb.setProjectionMap(sStreamItemsProjectionMap);
    }

    private void setTablesAndProjectionMapForStreamItemPhotos(SQLiteQueryBuilder qb) {
        qb.setTables(Tables.PHOTO_FILES
                + " JOIN " + Tables.STREAM_ITEM_PHOTOS + " ON ("
                + StreamItemPhotosColumns.CONCRETE_PHOTO_FILE_ID + "="
                + PhotoFilesColumns.CONCRETE_ID
                + ") JOIN " + Tables.STREAM_ITEMS + " ON ("
                + StreamItemPhotosColumns.CONCRETE_STREAM_ITEM_ID + "="
                + StreamItemsColumns.CONCRETE_ID + ")"
                + " JOIN " + Tables.RAW_CONTACTS + " ON ("
                + StreamItemsColumns.CONCRETE_RAW_CONTACT_ID + "=" + RawContactsColumns.CONCRETE_ID
                + ")");
        qb.setProjectionMap(sStreamItemPhotosProjectionMap);
    }

    private void setTablesAndProjectionMapForEntities(SQLiteQueryBuilder qb, Uri uri,
            String[] projection) {
        StringBuilder sb = new StringBuilder();
        sb.append(Views.ENTITIES);
        sb.append(" data");

        appendContactPresenceJoin(sb, projection, Contacts.Entity.CONTACT_ID);
        appendContactStatusUpdateJoin(sb, projection, ContactsColumns.LAST_STATUS_UPDATE_ID);
        appendDataPresenceJoin(sb, projection, Contacts.Entity.DATA_ID);
        appendDataStatusUpdateJoin(sb, projection, Contacts.Entity.DATA_ID);

        qb.setTables(sb.toString());
        qb.setProjectionMap(sEntityProjectionMap);
        appendAccountFromParameter(qb, uri);
    }

    private void appendContactStatusUpdateJoin(StringBuilder sb, String[] projection,
            String lastStatusUpdateIdColumn) {
        if (mDbHelper.get().isInProjection(projection,
                Contacts.CONTACT_STATUS,
                Contacts.CONTACT_STATUS_RES_PACKAGE,
                Contacts.CONTACT_STATUS_ICON,
                Contacts.CONTACT_STATUS_LABEL,
                Contacts.CONTACT_STATUS_TIMESTAMP)) {
            sb.append(" LEFT OUTER JOIN " + Tables.STATUS_UPDATES + " "
                    + ContactsStatusUpdatesColumns.ALIAS +
                    " ON (" + lastStatusUpdateIdColumn + "="
                            + ContactsStatusUpdatesColumns.CONCRETE_DATA_ID + ")");
        }
    }

    private void appendDataStatusUpdateJoin(StringBuilder sb, String[] projection,
            String dataIdColumn) {
        if (mDbHelper.get().isInProjection(projection,
                StatusUpdates.STATUS,
                StatusUpdates.STATUS_RES_PACKAGE,
                StatusUpdates.STATUS_ICON,
                StatusUpdates.STATUS_LABEL,
                StatusUpdates.STATUS_TIMESTAMP)) {
            sb.append(" LEFT OUTER JOIN " + Tables.STATUS_UPDATES +
                    " ON (" + StatusUpdatesColumns.CONCRETE_DATA_ID + "="
                            + dataIdColumn + ")");
        }
    }

    //private void appendDataUsageStatJoin(StringBuilder sb, int usageType, String dataIdColumn) {
    //    sb.append(" LEFT OUTER JOIN " + Tables.DATA_USAGE_STAT +
    //            " ON (" + DataUsageStatColumns.CONCRETE_DATA_ID + "=" + dataIdColumn +
    //            " AND " + DataUsageStatColumns.CONCRETE_USAGE_TYPE + "=" + usageType + ")");
    //}

    private void appendDataUsageStatJoin(StringBuilder sb, int usageType, String dataIdColumn) {
        if (usageType != USAGE_TYPE_ALL) {
            sb.append(" LEFT OUTER JOIN " + Tables.DATA_USAGE_STAT +
                    " ON (" + DataUsageStatColumns.CONCRETE_DATA_ID + "=");
            sb.append(dataIdColumn);
            sb.append(" AND " + DataUsageStatColumns.CONCRETE_USAGE_TYPE + "=");
            sb.append(usageType);
            sb.append(")");
        } else {
            sb.append(
                    " LEFT OUTER JOIN " +
                        "(SELECT " +
                            DataUsageStatColumns.CONCRETE_DATA_ID + ", " +
                            "SUM(" + DataUsageStatColumns.CONCRETE_TIMES_USED +
                                ") as " + DataUsageStatColumns.TIMES_USED + ", " +
                            "MAX(" + DataUsageStatColumns.CONCRETE_LAST_TIME_USED +
                                ") as " + DataUsageStatColumns.LAST_TIME_USED +
                        " FROM " + Tables.DATA_USAGE_STAT + " GROUP BY " +
                            DataUsageStatColumns.DATA_ID + ") as " + Tables.DATA_USAGE_STAT
                    );
            sb.append(" ON (" + DataUsageStatColumns.CONCRETE_DATA_ID + "=");
            sb.append(dataIdColumn);
            sb.append(")");
        }
    }

    private void appendContactPresenceJoin(StringBuilder sb, String[] projection,
            String contactIdColumn) {
        if (mDbHelper.get().isInProjection(projection,
                Contacts.CONTACT_PRESENCE, Contacts.CONTACT_CHAT_CAPABILITY)) {
            sb.append(" LEFT OUTER JOIN " + Tables.AGGREGATED_PRESENCE +
                    " ON (" + contactIdColumn + " = "
                            + AggregatedPresenceColumns.CONCRETE_CONTACT_ID + ")");
        }
    }

    private void appendDataPresenceJoin(StringBuilder sb, String[] projection,
            String dataIdColumn) {
        if (mDbHelper.get().isInProjection(projection, Data.PRESENCE, Data.CHAT_CAPABILITY)) {
            sb.append(" LEFT OUTER JOIN " + Tables.PRESENCE +
                    " ON (" + StatusUpdates.DATA_ID + "=" + dataIdColumn + ")");
        }
    }

    private boolean appendLocalDirectorySelectionIfNeeded(SQLiteQueryBuilder qb, long directoryId) {
        if (directoryId == Directory.DEFAULT) {
            qb.appendWhere(Contacts._ID + " IN " + Tables.DEFAULT_DIRECTORY);
            return true;
        } else if (directoryId == Directory.LOCAL_INVISIBLE){
            qb.appendWhere(Contacts._ID + " NOT IN " + Tables.DEFAULT_DIRECTORY);
            return true;
        }
        return false;
    }

    private void appendLocalDirectoryAndAccountSelectionIfNeeded(SQLiteQueryBuilder qb,
            long directoryId, Uri uri) {
        final StringBuilder sb = new StringBuilder();
        if (directoryId == Directory.DEFAULT) {
            sb.append("(" + Contacts._ID + " IN " + Tables.DEFAULT_DIRECTORY + ")");
        } else if (directoryId == Directory.LOCAL_INVISIBLE){
            sb.append("(" + Contacts._ID + " NOT IN " + Tables.DEFAULT_DIRECTORY + ")");
        } else {
            sb.append("(1)");
        }

        final AccountWithDataSet accountWithDataSet = getAccountWithDataSetFromUri(uri);
        // Accounts are valid by only checking one parameter, since we've
        // already ruled out partial accounts.
        final boolean validAccount = !TextUtils.isEmpty(accountWithDataSet.getAccountName());
        if (validAccount) {
            final Long accountId = mDbHelper.get().getAccountIdOrNull(accountWithDataSet);
            if (accountId == null) {
                // No such account.
                sb.setLength(0);
                sb.append("(1=2)");
            } else {
                sb.append(
                        " AND (" + Contacts._ID + " IN (" +
                        "SELECT " + RawContacts.CONTACT_ID + " FROM " + Tables.RAW_CONTACTS +
                        " WHERE " + RawContactsColumns.ACCOUNT_ID + "=" + accountId.toString() +
                        "))");
            }
        }
        qb.appendWhere(sb.toString());
    }

    private void appendAccountIdFromParameter(SQLiteQueryBuilder qb, Uri uri) {
        final AccountWithDataSet accountWithDataSet = getAccountWithDataSetFromUri(uri);

        // Accounts are valid by only checking one parameter, since we've
        // already ruled out partial accounts.
        final boolean validAccount = !TextUtils.isEmpty(accountWithDataSet.getAccountName());
        if (validAccount) {
            final Long accountId = mDbHelper.get().getAccountIdOrNull(accountWithDataSet);
            if (accountId == null) {
                // No such account.
                qb.appendWhere("(1=2)");
            } else {
                qb.appendWhere(
                        "(" + RawContactsColumns.ACCOUNT_ID + "=" + accountId.toString() + ")");
            }
        } else {
            qb.appendWhere("1");
        }
    }

    private AccountWithDataSet getAccountWithDataSetFromUri(Uri uri) {
        final String accountName = getQueryParameter(uri, RawContacts.ACCOUNT_NAME);
        final String accountType = getQueryParameter(uri, RawContacts.ACCOUNT_TYPE);
        final String dataSet = getQueryParameter(uri, RawContacts.DATA_SET);

        final boolean partialUri = TextUtils.isEmpty(accountName) ^ TextUtils.isEmpty(accountType);
        if (partialUri) {
            // Throw when either account is incomplete
            throw new IllegalArgumentException(mDbHelper.get().exceptionMessage(
                    "Must specify both or neither of ACCOUNT_NAME and ACCOUNT_TYPE", uri));
        }
        return AccountWithDataSet.get(accountName, accountType, dataSet);
    }

    private void appendAccountFromParameter(SQLiteQueryBuilder qb, Uri uri) {
//        final String accountName = getQueryParameter(uri, RawContacts.ACCOUNT_NAME);
//        final String accountType = getQueryParameter(uri, RawContacts.ACCOUNT_TYPE);
//        final String dataSet = getQueryParameter(uri, RawContacts.DATA_SET);
//
//        final boolean partialUri = TextUtils.isEmpty(accountName) ^ TextUtils.isEmpty(accountType);
//        if (partialUri) {
//            // Throw when either account is incomplete
//            throw new IllegalArgumentException(mDbHelper.get().exceptionMessage(
//                    "Must specify both or neither of ACCOUNT_NAME and ACCOUNT_TYPE", uri));
//        }
//
//        // Accounts are valid by only checking one parameter, since we've
//        // already ruled out partial accounts.
//        final boolean validAccount = !TextUtils.isEmpty(accountName);
//        if (validAccount) {
//            String toAppend = RawContacts.ACCOUNT_NAME + "="
//                    + DatabaseUtils.sqlEscapeString(accountName) + " AND "
//                    + RawContacts.ACCOUNT_TYPE + "="
//                    + DatabaseUtils.sqlEscapeString(accountType);
//            if (dataSet == null) {
//                toAppend += " AND " + RawContacts.DATA_SET + " IS NULL";
//            } else {
//                toAppend += " AND " + RawContacts.DATA_SET + "=" +
//                        DatabaseUtils.sqlEscapeString(dataSet);
//            }
//            qb.appendWhere(toAppend);
//        } else {
//            qb.appendWhere("1");
//        }
        final AccountWithDataSet accountWithDataSet = getAccountWithDataSetFromUri(uri);

        // Accounts are valid by only checking one parameter, since we've
        // already ruled out partial accounts.
        final boolean validAccount = !TextUtils.isEmpty(accountWithDataSet.getAccountName());
        if (validAccount) {
            String toAppend = "(" + RawContacts.ACCOUNT_NAME + "="
                    + DatabaseUtils.sqlEscapeString(accountWithDataSet.getAccountName()) + " AND "
                    + RawContacts.ACCOUNT_TYPE + "="
                    + DatabaseUtils.sqlEscapeString(accountWithDataSet.getAccountType());
            if (accountWithDataSet.getDataSet() == null) {
                toAppend += " AND " + RawContacts.DATA_SET + " IS NULL";
            } else {
                toAppend += " AND " + RawContacts.DATA_SET + "=" +
                        DatabaseUtils.sqlEscapeString(accountWithDataSet.getDataSet());
            }
            toAppend += ")";
            qb.appendWhere(toAppend);
        } else {
            qb.appendWhere("1");
        }
    }

    private String appendAccountToSelection(Uri uri, String selection) {
        final String accountName = getQueryParameter(uri, RawContacts.ACCOUNT_NAME);
        final String accountType = getQueryParameter(uri, RawContacts.ACCOUNT_TYPE);
        final String dataSet = getQueryParameter(uri, RawContacts.DATA_SET);

        final boolean partialUri = TextUtils.isEmpty(accountName) ^ TextUtils.isEmpty(accountType);
        if (partialUri) {
            // Throw when either account is incomplete
            throw new IllegalArgumentException(mDbHelper.get().exceptionMessage(
                    "Must specify both or neither of ACCOUNT_NAME and ACCOUNT_TYPE", uri));
        }

        // Accounts are valid by only checking one parameter, since we've
        // already ruled out partial accounts.
        final boolean validAccount = !TextUtils.isEmpty(accountName);
        if (validAccount) {
            StringBuilder selectionSb = new StringBuilder(RawContacts.ACCOUNT_NAME + "="
                    + DatabaseUtils.sqlEscapeString(accountName) + " AND "
                    + RawContacts.ACCOUNT_TYPE + "="
                    + DatabaseUtils.sqlEscapeString(accountType));
            if (dataSet == null) {
                selectionSb.append(" AND " + RawContacts.DATA_SET + " IS NULL");
            } else {
                selectionSb.append(" AND " + RawContacts.DATA_SET + "=")
                        .append(DatabaseUtils.sqlEscapeString(dataSet));
            }
            if (!TextUtils.isEmpty(selection)) {
                selectionSb.append(" AND (");
                selectionSb.append(selection);
                selectionSb.append(')');
            }
            return selectionSb.toString();
        } else {
            return selection;
        }
    }

    private String appendAccountIdToSelection(Uri uri, String selection) {
        final AccountWithDataSet accountWithDataSet = getAccountWithDataSetFromUri(uri);

        // Accounts are valid by only checking one parameter, since we've
        // already ruled out partial accounts.
        final boolean validAccount = !TextUtils.isEmpty(accountWithDataSet.getAccountName());
        if (validAccount) {
            final StringBuilder selectionSb = new StringBuilder();

            final Long accountId = mDbHelper.get().getAccountIdOrNull(accountWithDataSet);
            if (accountId == null) {
                // No such account in the accounts table.  This means, there's no rows to be
                // selected.
                // Note even in this case, we still need to append the original selection, because
                // it may have query parameters.  If we remove these we'll get the # of parameters
                // mismatch exception.
                selectionSb.append("(1=2)");
            } else {
                selectionSb.append(RawContactsColumns.ACCOUNT_ID + "=");
                selectionSb.append(Long.toString(accountId));
            }

            if (!TextUtils.isEmpty(selection)) {
                selectionSb.append(" AND (");
                selectionSb.append(selection);
                selectionSb.append(')');
            }
            return selectionSb.toString();
        } else {
            return selection;
        }
    }

    /**
     * Gets the value of the "limit" URI query parameter.
     *
     * @return A string containing a non-negative integer, or <code>null</code> if
     *         the parameter is not set, or is set to an invalid value.
     */
    private String getLimit(Uri uri) {
        String limitParam = getQueryParameter(uri, GnContactsContract.LIMIT_PARAM_KEY);
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
    public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
		System.out.println("openAssetFile");
		switch (sUriMatcher.match(uri)) {
		case SYNC_ACCESSORY:
			return super.openAssetFile(uri, mode);
		}
    	if (mode.equals("r")) {
            waitForAccess(mReadAccessLatch);
        } else {
            waitForAccess(mWriteAccessLatch);
        }
        if (mapsToProfileDb(uri)) {
            switchToProfileMode();
            return mProfileProvider.openAssetFile(uri, mode);
        } else {
            switchToContactMode();
            return openAssetFileLocal(uri, mode);
        }
    }

    public AssetFileDescriptor openAssetFileLocal(Uri uri, String mode)
            throws FileNotFoundException {

        // Default active DB to the contacts DB if none has been set.
        if (mActiveDb.get() == null) {
            if (mode.equals("r")) {
                mActiveDb.set(mContactsHelper.getReadableDatabase());
            } else {
                mActiveDb.set(mContactsHelper.getWritableDatabase());
            }
        }

        int match = sUriMatcher.match(uri);
        switch (match) {
            case CONTACTS_ID_PHOTO: {
                long contactId = Long.parseLong(uri.getPathSegments().get(1));
                return openPhotoAssetFile(mActiveDb.get(), uri, mode,
                        Data._ID + "=" + Contacts.PHOTO_ID + " AND " +
                                RawContacts.CONTACT_ID + "=?",
                        new String[]{String.valueOf(contactId)});
            }

            case CONTACTS_ID_DISPLAY_PHOTO: {
                if (!mode.equals("r")) {
                    throw new IllegalArgumentException(
                            "Display photos retrieved by contact ID can only be read.");
                }
                long contactId = Long.parseLong(uri.getPathSegments().get(1));
                Cursor c = mActiveDb.get().query(Tables.CONTACTS,
                        new String[]{Contacts.PHOTO_FILE_ID},
                        Contacts._ID + "=?", new String[]{String.valueOf(contactId)},
                        null, null, null);
                try {
                    if (c.moveToFirst()) {
                        long photoFileId = c.getLong(0);
                        return openDisplayPhotoForRead(photoFileId);
                    } else {
                        // No contact for this ID.
                        throw new FileNotFoundException(uri.toString());
                    }
                } finally {
                    c.close();
                }
            }

            case PROFILE_DISPLAY_PHOTO: {
                if (!mode.equals("r")) {
                    throw new IllegalArgumentException(
                            "Display photos retrieved by contact ID can only be read.");
                }
                Cursor c = mActiveDb.get().query(Tables.CONTACTS,
                        new String[]{Contacts.PHOTO_FILE_ID}, null, null, null, null, null);
                try {
                    if (c.moveToFirst()) {
                        long photoFileId = c.getLong(0);
                        return openDisplayPhotoForRead(photoFileId);
                    } else {
                        // No profile record.
                        throw new FileNotFoundException(uri.toString());
                    }
                } finally {
                    c.close();
                }
            }

            case CONTACTS_LOOKUP_PHOTO:
            case CONTACTS_LOOKUP_ID_PHOTO:
            case CONTACTS_LOOKUP_DISPLAY_PHOTO:
            case CONTACTS_LOOKUP_ID_DISPLAY_PHOTO: {
                if (!mode.equals("r")) {
                    throw new IllegalArgumentException(
                            "Photos retrieved by contact lookup key can only be read.");
                }
                List<String> pathSegments = uri.getPathSegments();
                int segmentCount = pathSegments.size();
                if (segmentCount < 4) {
                    throw new IllegalArgumentException(mDbHelper.get().exceptionMessage(
                            "Missing a lookup key", uri));
                }

                boolean forDisplayPhoto = (match == CONTACTS_LOOKUP_ID_DISPLAY_PHOTO
                        || match == CONTACTS_LOOKUP_DISPLAY_PHOTO);
                String lookupKey = pathSegments.get(2);
                String[] projection = new String[]{Contacts.PHOTO_ID, Contacts.PHOTO_FILE_ID};
                if (segmentCount == 5) {
                    long contactId = Long.parseLong(pathSegments.get(3));
                    SQLiteQueryBuilder lookupQb = new SQLiteQueryBuilder();
                    setTablesAndProjectionMapForContacts(lookupQb, uri, projection);
                    Cursor c = queryWithContactIdAndLookupKey(lookupQb, mActiveDb.get(), uri,
                            projection, null, null, null, null, null,
                            Contacts._ID, contactId, Contacts.LOOKUP_KEY, lookupKey);
                    if (c != null) {
                        try {
                            c.moveToFirst();
                            if (forDisplayPhoto) {
                                long photoFileId =
                                        c.getLong(c.getColumnIndex(Contacts.PHOTO_FILE_ID));
                                return openDisplayPhotoForRead(photoFileId);
                            } else {
                                long photoId = c.getLong(c.getColumnIndex(Contacts.PHOTO_ID));
                                return openPhotoAssetFile(mActiveDb.get(), uri, mode,
                                        Data._ID + "=?", new String[]{String.valueOf(photoId)});
                            }
                        } finally {
                            c.close();
                        }
                    }
                }

                SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
                setTablesAndProjectionMapForContacts(qb, uri, projection);
                long contactId = lookupContactIdByLookupKey(mActiveDb.get(), lookupKey);
                Cursor c = qb.query(mActiveDb.get(), projection, Contacts._ID + "=?",
                        new String[]{String.valueOf(contactId)}, null, null, null);
                try {
                    c.moveToFirst();
                    if (forDisplayPhoto) {
                        long photoFileId = c.getLong(c.getColumnIndex(Contacts.PHOTO_FILE_ID));
                        return openDisplayPhotoForRead(photoFileId);
                    } else {
                        long photoId = c.getLong(c.getColumnIndex(Contacts.PHOTO_ID));
                        return openPhotoAssetFile(mActiveDb.get(), uri, mode,
                                Data._ID + "=?", new String[]{String.valueOf(photoId)});
                    }
                } finally {
                    c.close();
                }
            }

            case RAW_CONTACTS_ID_DISPLAY_PHOTO: {
                long rawContactId = Long.parseLong(uri.getPathSegments().get(1));
                boolean writeable = !mode.equals("r");

                // Find the primary photo data record for this raw contact.
                SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
                String[] projection = new String[]{Data._ID, Photo.PHOTO_FILE_ID};
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                long photoMimetypeId = mDbHelper.get().getMimeTypeId(Photo.CONTENT_ITEM_TYPE);
                Cursor c = qb.query(mActiveDb.get(), projection,
                        Data.RAW_CONTACT_ID + "=? AND " + DataColumns.MIMETYPE_ID + "=?",
                        new String[]{String.valueOf(rawContactId), String.valueOf(photoMimetypeId)},
                        null, null, Data.IS_PRIMARY + " DESC");
                long dataId = 0;
                long photoFileId = 0;
                try {
                    if (c.getCount() >= 1) {
                        c.moveToFirst();
                        dataId = c.getLong(0);
                        photoFileId = c.getLong(1);
                    }
                } finally {
                    c.close();
                }

                // If writeable, open a writeable file descriptor that we can monitor.
                // When the caller finishes writing content, we'll process the photo and
                // update the data record.
                if (writeable) {
                    return openDisplayPhotoForWrite(rawContactId, dataId, uri, mode);
                } else {
                    return openDisplayPhotoForRead(photoFileId);
                }
            }

            case DISPLAY_PHOTO: {
                long photoFileId = ContentUris.parseId(uri);
                if (!mode.equals("r")) {
                    throw new IllegalArgumentException(
                            "Display photos retrieved by key can only be read.");
                }
                return openDisplayPhotoForRead(photoFileId);
            }

            case DATA_ID: {
                long dataId = Long.parseLong(uri.getPathSegments().get(1));
                long photoMimetypeId = mDbHelper.get().getMimeTypeId(Photo.CONTENT_ITEM_TYPE);
                return openPhotoAssetFile(mActiveDb.get(), uri, mode,
                        Data._ID + "=? AND " + DataColumns.MIMETYPE_ID + "=" + photoMimetypeId,
                        new String[]{String.valueOf(dataId)});
            }

            case PROFILE_AS_VCARD: {
                // When opening a contact as file, we pass back contents as a
                // vCard-encoded stream. We build into a local buffer first,
                // then pipe into MemoryFile once the exact size is known.
                final ByteArrayOutputStream localStream = new ByteArrayOutputStream();
                outputRawContactsAsVCard(uri, localStream, null, null);
                return buildAssetFileDescriptor(localStream);
            }

            case CONTACTS_AS_VCARD: {
                // When opening a contact as file, we pass back contents as a
                // vCard-encoded stream. We build into a local buffer first,
                // then pipe into MemoryFile once the exact size is known.
                final ByteArrayOutputStream localStream = new ByteArrayOutputStream();
                outputRawContactsAsVCard(uri, localStream, null, null);
                return buildAssetFileDescriptor(localStream);
            }

            case CONTACTS_AS_MULTI_VCARD: {
                final String lookupKeys = uri.getPathSegments().get(2);
                final String[] loopupKeyList = lookupKeys.split(":");
                final StringBuilder inBuilder = new StringBuilder();
                Uri queryUri = Contacts.CONTENT_URI;
                int index = 0;

                // SQLite has limits on how many parameters can be used
                // so the IDs are concatenated to a query string here instead
                for (String lookupKey : loopupKeyList) {
                    if (index == 0) {
                        inBuilder.append("(");
                    } else {
                        inBuilder.append(",");
                    }
                    // TODO: Figure out what to do if the profile contact is in the list.
                    long contactId = lookupContactIdByLookupKey(mActiveDb.get(), lookupKey);
                    inBuilder.append(contactId);
                    index++;
                }
                inBuilder.append(')');
                final String selection = Contacts._ID + " IN " + inBuilder.toString();

                // When opening a contact as file, we pass back contents as a
                // vCard-encoded stream. We build into a local buffer first,
                // then pipe into MemoryFile once the exact size is known.
                final ByteArrayOutputStream localStream = new ByteArrayOutputStream();
                outputRawContactsAsVCard(queryUri, localStream, selection, null);
                return buildAssetFileDescriptor(localStream);
            }
            //Gionee:huangzy 20121128 add for CR00736966 start
            case GN_SYNC_STATUS: {
            	String fileName = uri.getLastPathSegment();
            	File file = new File(getContext().getFilesDir(), fileName);            	
            	try {
            		if (!file.exists()) {
            			file.createNewFile();
            		}
            		return makeAssetFileDescriptor(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE));
				} catch (FileNotFoundException e) {
					Log.e("ContactsProvider", " GN_SYNC_STATUS " + file.toString() + "  FileNotFoundException");
				} catch (IOException e) {
					Log.e("ContactsProvider", " GN_SYNC_STATUS " + file.toString() + "  createNewFile error");
				}
				
				return null;
            }
            //Gionee:huangzy 20121128 add for CR00736966 end

            default:
                throw new FileNotFoundException(mDbHelper.get().exceptionMessage(
                        "File does not exist", uri));
        }
    }

    private AssetFileDescriptor openPhotoAssetFile(SQLiteDatabase db, Uri uri, String mode,
            String selection, String[] selectionArgs)
            throws FileNotFoundException {
        if (!"r".equals(mode)) {
            throw new FileNotFoundException(mDbHelper.get().exceptionMessage("Mode " + mode
                    + " not supported.", uri));
        }

        String sql =
                "SELECT " + Photo.PHOTO + " FROM " + Views.DATA +
                " WHERE " + selection;
        try {
            return makeAssetFileDescriptor(
                    DatabaseUtils.blobFileDescriptorForQuery(db, sql, selectionArgs));
        } catch (SQLiteDoneException e) {
            // this will happen if the DB query returns no rows (i.e. contact does not exist)
            throw new FileNotFoundException(uri.toString());
        }
    }

    /**
     * Opens a display photo from the photo store for reading.
     * @param photoFileId The display photo file ID
     * @return An asset file descriptor that allows the file to be read.
     * @throws FileNotFoundException If no photo file for the given ID exists.
     */
    private AssetFileDescriptor openDisplayPhotoForRead(long photoFileId)
            throws FileNotFoundException {
        PhotoStore.Entry entry = mPhotoStore.get().get(photoFileId);
        if (entry != null) {
            try {
                return makeAssetFileDescriptor(
                        ParcelFileDescriptor.open(new File(entry.path),
                                ParcelFileDescriptor.MODE_READ_ONLY),
                        entry.size);
            } catch (FileNotFoundException fnfe) {
                scheduleBackgroundTask(BACKGROUND_TASK_CLEANUP_PHOTOS);
                throw fnfe;
            }
        } else {
            scheduleBackgroundTask(BACKGROUND_TASK_CLEANUP_PHOTOS);
            throw new FileNotFoundException("No photo file found for ID " + photoFileId);
        }
    }

    /**
     * Opens a file descriptor for a photo to be written.  When the caller completes writing
     * to the file (closing the output stream), the image will be parsed out and processed.
     * If processing succeeds, the given raw contact ID's primary photo record will be
     * populated with the inserted image (if no primary photo record exists, the data ID can
     * be left as 0, and a new data record will be inserted).
     * @param rawContactId Raw contact ID this photo entry should be associated with.
     * @param dataId Data ID for a photo mimetype that will be updated with the inserted
     *     image.  May be set to 0, in which case the inserted image will trigger creation
     *     of a new primary photo image data row for the raw contact.
     * @param uri The URI being used to access this file.
     * @param mode Read/write mode string.
     * @return An asset file descriptor the caller can use to write an image file for the
     *     raw contact.
     */
    private AssetFileDescriptor openDisplayPhotoForWrite(long rawContactId, long dataId, Uri uri,
            String mode) {
        try {
            ParcelFileDescriptor[] pipeFds = ParcelFileDescriptor.createPipe();
            PipeMonitor pipeMonitor = new PipeMonitor(rawContactId, dataId, pipeFds[0]);
            pipeMonitor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Object[]) null);
            return new AssetFileDescriptor(pipeFds[1], 0, AssetFileDescriptor.UNKNOWN_LENGTH);
        } catch (IOException ioe) {
            Log.e(TAG, "Could not create temp image file in mode " + mode);
            return null;
        }
    }

    /**
     * Async task that monitors the given file descriptor (the read end of a pipe) for
     * the writer finishing.  If the data from the pipe contains a valid image, the image
     * is either inserted into the given raw contact or updated in the given data row.
     */
    private class PipeMonitor extends AsyncTask<Object, Object, Object> {
        private final ParcelFileDescriptor mDescriptor;
        private final long mRawContactId;
        private final long mDataId;
        private PipeMonitor(long rawContactId, long dataId, ParcelFileDescriptor descriptor) {
            mRawContactId = rawContactId;
            mDataId = dataId;
            mDescriptor = descriptor;
        }

        @Override
        protected Object doInBackground(Object... params) {
        	System.out.println("doInBackground");
            AutoCloseInputStream is = new AutoCloseInputStream(mDescriptor);
            try {
                Bitmap b = BitmapFactory.decodeStream(is);
                if (b != null) {
                    waitForAccess(mWriteAccessLatch);
                    PhotoProcessor processor = new PhotoProcessor(b, mMaxDisplayPhotoDim,
                            mMaxThumbnailPhotoDim);

                    // Store the compressed photo in the photo store.
                    PhotoStore photoStore = GnContactsContract.isProfileId(mRawContactId)
                            ? mProfilePhotoStore
                            : mContactsPhotoStore;
                    long photoFileId = photoStore.insert(processor);

                    // Depending on whether we already had a data row to attach the photo
                    // to, do an update or insert.
                    if (mDataId != 0) {
                        // Update the data record with the new photo.
                        ContentValues updateValues = new ContentValues();

                        // Signal that photo processing has already been handled.
                        updateValues.put(DataRowHandlerForPhoto.SKIP_PROCESSING_KEY, true);

                        if (photoFileId != 0) {
                            updateValues.put(Photo.PHOTO_FILE_ID, photoFileId);
                        }
                        updateValues.put(Photo.PHOTO, processor.getThumbnailPhotoBytes());
                        update(ContentUris.withAppendedId(Data.CONTENT_URI, mDataId),
                                updateValues, null, null);
                    } else {
                        // Insert a new primary data record with the photo.
                        ContentValues insertValues = new ContentValues();

                        // Signal that photo processing has already been handled.
                        insertValues.put(DataRowHandlerForPhoto.SKIP_PROCESSING_KEY, true);

                        insertValues.put(Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE);
                        insertValues.put(Data.IS_PRIMARY, 1);
                        if (photoFileId != 0) {
                            insertValues.put(Photo.PHOTO_FILE_ID, photoFileId);
                        }
                        insertValues.put(Photo.PHOTO, processor.getThumbnailPhotoBytes());
                        insert(RawContacts.CONTENT_URI.buildUpon()
                                .appendPath(String.valueOf(mRawContactId))
                                .appendPath(RawContacts.Data.CONTENT_DIRECTORY).build(),
                                insertValues);
                    }

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
    }

    private static final String CONTACT_MEMORY_FILE_NAME = "contactAssetFile";

    /**
     * Returns an {@link AssetFileDescriptor} backed by the
     * contents of the given {@link ByteArrayOutputStream}.
     */
    private AssetFileDescriptor buildAssetFileDescriptor(ByteArrayOutputStream stream) {
        try {
            stream.flush();

            final byte[] byteData = stream.toByteArray();

            return makeAssetFileDescriptor(
                    ParcelFileDescriptor.fromData(byteData, CONTACT_MEMORY_FILE_NAME),
                    byteData.length);
        } catch (IOException e) {
            Log.w(TAG, "Problem writing stream into an ParcelFileDescriptor: " + e.toString());
            return null;
        }
    }

    private AssetFileDescriptor makeAssetFileDescriptor(ParcelFileDescriptor fd) {
        return makeAssetFileDescriptor(fd, AssetFileDescriptor.UNKNOWN_LENGTH);
    }

    private AssetFileDescriptor makeAssetFileDescriptor(ParcelFileDescriptor fd, long length) {
        return fd != null ? new AssetFileDescriptor(fd, 0, length) : null;
    }

    /**
     * Output {@link RawContacts} matching the requested selection in the vCard
     * format to the given {@link OutputStream}. This method returns silently if
     * any errors encountered.
     */
    private void outputRawContactsAsVCard(Uri uri, OutputStream stream,
            String selection, String[] selectionArgs) {
        final Context context = this.getContext();
        int vcardconfig = VCardConfig.VCARD_TYPE_DEFAULT;
        if(uri.getBooleanQueryParameter(
                Contacts.QUERY_PARAMETER_VCARD_NO_PHOTO, false)) {
            vcardconfig |= VCardConfig.FLAG_REFRAIN_IMAGE_EXPORT;
        }
        final VCardComposer composer =
                new VCardComposer(context, vcardconfig, false);
        Writer writer = null;
        final Uri rawContactsUri;
        if (mapsToProfileDb(uri)) {
            // Pre-authorize the URI, since the caller would have already gone through the
            // permission check to get here, but the pre-authorization at the top level wouldn't
            // carry over to the raw contact.
            rawContactsUri = preAuthorizeUri(RawContactsEntity.PROFILE_CONTENT_URI);
        } else {
            rawContactsUri = RawContactsEntity.CONTENT_URI;
        }
        try {
            writer = new BufferedWriter(new OutputStreamWriter(stream));
            if (!composer.init(uri, selection, selectionArgs, null, rawContactsUri)) {
                Log.w(TAG, "Failed to init VCardComposer");
                return;
            }

            while (!composer.isAfterLast()) {
                writer.write(composer.createOneEntry());
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e);
        } finally {
            composer.terminate();
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    Log.w(TAG, "IOException during closing output stream: " + e);
                }
            }
        }
    }

    @Override
    public String getType(Uri uri) {

        waitForAccess(mReadAccessLatch);

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CONTACTS:
                return Contacts.CONTENT_TYPE;
            case CONTACTS_LOOKUP:
            case CONTACTS_ID:
            case CONTACTS_LOOKUP_ID:
            case PROFILE:
                return Contacts.CONTENT_ITEM_TYPE;
            case CONTACTS_AS_VCARD:
            case CONTACTS_AS_MULTI_VCARD:
            case PROFILE_AS_VCARD:
                return Contacts.CONTENT_VCARD_TYPE;
            case CONTACTS_ID_PHOTO:
            case CONTACTS_LOOKUP_PHOTO:
            case CONTACTS_LOOKUP_ID_PHOTO:
            case CONTACTS_ID_DISPLAY_PHOTO:
            case CONTACTS_LOOKUP_DISPLAY_PHOTO:
            case CONTACTS_LOOKUP_ID_DISPLAY_PHOTO:
            case RAW_CONTACTS_ID_DISPLAY_PHOTO:
            case DISPLAY_PHOTO:
                return "image/jpeg";
            case RAW_CONTACTS:
            case PROFILE_RAW_CONTACTS:
                return RawContacts.CONTENT_TYPE;
            case RAW_CONTACTS_ID:
            case PROFILE_RAW_CONTACTS_ID:
                return RawContacts.CONTENT_ITEM_TYPE;
            case DATA:
            case PROFILE_DATA:
                return Data.CONTENT_TYPE;
            case DATA_ID:
                long id = ContentUris.parseId(uri);
                if (GnContactsContract.isProfileId(id)) {
                    return mProfileHelper.getDataMimeType(id);
                } else {
                    return mContactsHelper.getDataMimeType(id);
                }
            case PHONES:
                return Phone.CONTENT_TYPE;
            case PHONES_ID:
                return Phone.CONTENT_ITEM_TYPE;
            case PHONE_LOOKUP:
                return PhoneLookup.CONTENT_TYPE;
            case EMAILS:
                return Email.CONTENT_TYPE;
            case EMAILS_ID:
                return Email.CONTENT_ITEM_TYPE;
            case POSTALS:
                return StructuredPostal.CONTENT_TYPE;
            case POSTALS_ID:
                return StructuredPostal.CONTENT_ITEM_TYPE;
            case AGGREGATION_EXCEPTIONS:
                return AggregationExceptions.CONTENT_TYPE;
            case AGGREGATION_EXCEPTION_ID:
                return AggregationExceptions.CONTENT_ITEM_TYPE;
            case SETTINGS:
                return Settings.CONTENT_TYPE;
            case AGGREGATION_SUGGESTIONS:
                return Contacts.CONTENT_TYPE;
            case SEARCH_SUGGESTIONS:
                return SearchManager.SUGGEST_MIME_TYPE;
            case SEARCH_SHORTCUT:
                return SearchManager.SHORTCUT_MIME_TYPE;
            case DIRECTORIES:
                return Directory.CONTENT_TYPE;
            case DIRECTORIES_ID:
                return Directory.CONTENT_ITEM_TYPE;
            case STREAM_ITEMS:
                return StreamItems.CONTENT_TYPE;
            case STREAM_ITEMS_ID:
                return StreamItems.CONTENT_ITEM_TYPE;
            case STREAM_ITEMS_ID_PHOTOS:
                return StreamItems.StreamItemPhotos.CONTENT_TYPE;
            case STREAM_ITEMS_ID_PHOTOS_ID:
                return StreamItems.StreamItemPhotos.CONTENT_ITEM_TYPE;
            case STREAM_ITEMS_PHOTOS:
                throw new UnsupportedOperationException("Not supported for write-only URI " + uri);
                
            // reject begin
            case BLACKS:
            case MARKS:
                return "vnd.android.cursor.dir/com.aurora.reject";
            case BLACK:
            case MARK:
                return "vnd.android.cursor.item/com.aurora.reject";
            // reject end
                
            //contact sync begin
            case SYNCS:
                return "vnd.android.cursor.dir/com.aurora.sync"; 
            case SYNC:
                return "vnd.android.cursor.item/com.aurora.sync";
            //contact sync end
    		
            default:
                return mLegacyApiSupport.getType(uri);
        }
    }

    public String[] getDefaultProjection(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CONTACTS:
            case CONTACTS_LOOKUP:
            case CONTACTS_ID:
            case CONTACTS_LOOKUP_ID:
            case AGGREGATION_SUGGESTIONS:
            case PROFILE:
                return sContactsProjectionMap.getColumnNames();

            case CONTACTS_ID_ENTITIES:
            case PROFILE_ENTITIES:
                return sEntityProjectionMap.getColumnNames();

            case CONTACTS_AS_VCARD:
            case CONTACTS_AS_MULTI_VCARD:
            case PROFILE_AS_VCARD:
                return sContactsVCardProjectionMap.getColumnNames();

            case RAW_CONTACTS:
            case RAW_CONTACTS_ID:
            case PROFILE_RAW_CONTACTS:
            case PROFILE_RAW_CONTACTS_ID:
                return sRawContactsProjectionMap.getColumnNames();

            case DATA_ID:
            case PHONES:
            case PHONES_ID:
            case EMAILS:
            case EMAILS_ID:
            case POSTALS:
            case POSTALS_ID:
            case PROFILE_DATA:
                return sDataProjectionMap.getColumnNames();

            case PHONE_LOOKUP:
                return sPhoneLookupProjectionMap.getColumnNames();

            case AGGREGATION_EXCEPTIONS:
            case AGGREGATION_EXCEPTION_ID:
                return sAggregationExceptionsProjectionMap.getColumnNames();

            case SETTINGS:
                return sSettingsProjectionMap.getColumnNames();

            case DIRECTORIES:
            case DIRECTORIES_ID:
                return sDirectoryProjectionMap.getColumnNames();

            default:
                return null;
        }
    }

    private class StructuredNameLookupBuilder extends NameLookupBuilder {

        public StructuredNameLookupBuilder(NameSplitter splitter) {
            super(splitter);
        }

        @Override
        protected void insertNameLookup(long rawContactId, long dataId, int lookupType,
                String name) {
            mDbHelper.get().insertNameLookup(rawContactId, dataId, lookupType, name);
        }

        @Override
        protected String[] getCommonNicknameClusters(String normalizedName) {
            return mCommonNicknameCache.getCommonNicknameClusters(normalizedName);
        }
    }

    public void appendContactFilterAsNestedQuery(StringBuilder sb, String filterParam) {
        sb.append("(" +
                "SELECT DISTINCT " + RawContacts.CONTACT_ID +
                " FROM " + Tables.RAW_CONTACTS +
                " JOIN " + Tables.NAME_LOOKUP +
                " ON(" + RawContactsColumns.CONCRETE_ID + "="
                        + NameLookupColumns.RAW_CONTACT_ID + ")" +
                " WHERE normalized_name GLOB '");
        sb.append(NameNormalizer.normalize(filterParam));
        sb.append("*' AND " + NameLookupColumns.NAME_TYPE +
                    " IN(" + CONTACT_LOOKUP_NAME_TYPES + "))");
    }

    public boolean isPhoneNumber(String filter) {
        boolean atLeastOneDigit = false;
        int len = filter.length();
        for (int i = 0; i < len; i++) {
            char c = filter.charAt(i);
            if (c >= '0' && c <= '9') {
                atLeastOneDigit = true;
            } else if (c != '*' && c != '#' && c != '+' && c != 'N' && c != '.' && c != ';'
                    && c != '-' && c != '(' && c != ')' && c != ' ') {
                return false;
            }
        }
        return atLeastOneDigit;
    }

    /**
     * Takes components of a name from the query parameters and returns a cursor with those
     * components as well as all missing components.  There is no database activity involved
     * in this so the call can be made on the UI thread.
     */
    private Cursor completeName(Uri uri, String[] projection) {
        if (projection == null) {
            projection = sDataProjectionMap.getColumnNames();
        }

        ContentValues values = new ContentValues();
        DataRowHandlerForStructuredName handler = (DataRowHandlerForStructuredName)
                getDataRowHandler(StructuredName.CONTENT_ITEM_TYPE);

        copyQueryParamsToContentValues(values, uri,
                StructuredName.DISPLAY_NAME,
                StructuredName.PREFIX,
                StructuredName.GIVEN_NAME,
                StructuredName.MIDDLE_NAME,
                StructuredName.FAMILY_NAME,
                StructuredName.SUFFIX,
                StructuredName.PHONETIC_NAME,
                StructuredName.PHONETIC_FAMILY_NAME,
                StructuredName.PHONETIC_MIDDLE_NAME,
                StructuredName.PHONETIC_GIVEN_NAME
        );

        handler.fixStructuredNameComponents(values, values);

        MatrixCursor cursor = new MatrixCursor(projection);
        Object[] row = new Object[projection.length];
        for (int i = 0; i < projection.length; i++) {
            row[i] = values.get(projection[i]);
        }
        cursor.addRow(row);
        return cursor;
    }

    private void copyQueryParamsToContentValues(ContentValues values, Uri uri, String... columns) {
        for (String column : columns) {
            String param = uri.getQueryParameter(column);
            if (param != null) {
                values.put(column, param);
            }
        }
    }


    /**
     * Inserts an argument at the beginning of the selection arg list.
     */
    private String[] insertSelectionArg(String[] selectionArgs, String arg) {
        if (selectionArgs == null) {
            return new String[] {arg};
        } else {
            int newLength = selectionArgs.length + 1;
            String[] newSelectionArgs = new String[newLength];
            newSelectionArgs[0] = arg;
            System.arraycopy(selectionArgs, 0, newSelectionArgs, 1, selectionArgs.length);
            return newSelectionArgs;
        }
    }

    private String[] appendProjectionArg(String[] projection, String arg) {
        if (projection == null) {
            return null;
        }
        final int length = projection.length;
        String[] newProjection = new String[length + 1];
        System.arraycopy(projection, 0, newProjection, 0, length);
        newProjection[length] = arg;
        return newProjection;
    }

    protected Account getDefaultAccount() {
        AccountManager accountManager = AccountManager.get(getContext());
        try {
            Account[] accounts = accountManager.getAccountsByType(DEFAULT_ACCOUNT_TYPE);
            if (accounts != null && accounts.length > 0) {
                return accounts[0];
            }
        } catch (Throwable e) {
            Log.e(TAG, "Cannot determine the default account for contacts compatibility", e);
        }
        return null;
    }

    /**
     * Returns true if the specified account type and data set is writable.
     */
    protected boolean isWritableAccountWithDataSet(String accountTypeAndDataSet) {
        if (accountTypeAndDataSet == null) {
            return true;
        }

        Boolean writable = mAccountWritability.get(accountTypeAndDataSet);
        if (writable != null) {
            return writable;
        }

        IContentService contentService = ContentResolver.getContentService();
        try {
            // TODO(dsantoro): Need to update this logic to allow for sub-accounts.
            for (SyncAdapterType sync : contentService.getSyncAdapterTypes()) {
                if (GnContactsContract.AUTHORITY.equals(sync.authority) &&
                        accountTypeAndDataSet.equals(sync.accountType)) {
                    writable = sync.supportsUploading();
                    break;
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Could not acquire sync adapter types");
        }

        if (writable == null) {
            writable = false;
        }

        mAccountWritability.put(accountTypeAndDataSet, writable);
        return writable;
    }


    /* package */ static boolean readBooleanQueryParameter(Uri uri, String parameter,
            boolean defaultValue) {

        // Manually parse the query, which is much faster than calling uri.getQueryParameter
        String query = uri.getEncodedQuery();
        if (query == null) {
            return defaultValue;
        }

        int index = query.indexOf(parameter);
        if (index == -1) {
            return defaultValue;
        }

        index += parameter.length();

        return !matchQueryParameter(query, index, "=0", false)
                && !matchQueryParameter(query, index, "=false", true);
    }

    private static boolean matchQueryParameter(String query, int index, String value,
            boolean ignoreCase) {
        int length = value.length();
        return query.regionMatches(ignoreCase, index, value, 0, length)
                && (query.length() == index + length || query.charAt(index + length) == '&');
    }

    /**
     * A fast re-implementation of {@link Uri#getQueryParameter}
     */
    /* package */ static String getQueryParameter(Uri uri, String parameter) {
        String query = uri.getEncodedQuery();
        if (query == null) {
            return null;
        }

        int queryLength = query.length();
        int parameterLength = parameter.length();

        String value;
        int index = 0;
        while (true) {
            index = query.indexOf(parameter, index);
            if (index == -1) {
                return null;
            }

            // Should match against the whole parameter instead of its suffix.
            // e.g. The parameter "param" must not be found in "some_param=val".
            if (index > 0) {
                char prevChar = query.charAt(index - 1);
                if (prevChar != '?' && prevChar != '&') {
                    // With "some_param=val1&param=val2", we should find second "param" occurrence.
                    index += parameterLength;
                    continue;
                }
            }

            index += parameterLength;

            if (queryLength == index) {
                return null;
            }

            if (query.charAt(index) == '=') {
                index++;
                break;
            }
        }

        int ampIndex = query.indexOf('&', index);
        if (ampIndex == -1) {
            value = query.substring(index);
        } else {
            value = query.substring(index, ampIndex);
        }

        return Uri.decode(value);
    }

    protected boolean isAggregationUpgradeNeeded() {
        if (!mContactAggregator.isEnabled()) {
            return false;
        }

        int version = Integer.parseInt(mContactsHelper.getProperty(
                PROPERTY_AGGREGATION_ALGORITHM, "1"));
        return version < PROPERTY_AGGREGATION_ALGORITHM_VERSION;
    }

    protected void upgradeAggregationAlgorithmInBackground() {
        // This upgrade will affect very few contacts, so it can be performed on the
        // main thread during the initial boot after an OTA

        Log.i(TAG, "Upgrading aggregation algorithm");
        int count = 0;
        long start = SystemClock.currentThreadTimeMillis();
        SQLiteDatabase db = null;
        try {
            switchToContactMode();
            db = mContactsHelper.getWritableDatabase();
            mActiveDb.set(db);
            db.beginTransaction();
//            Cursor cursor = db.query(true,
//                    Tables.RAW_CONTACTS + " r1 JOIN " + Tables.RAW_CONTACTS + " r2",
//                    new String[]{"r1." + RawContacts._ID},
//                    "r1." + RawContacts._ID + "!=r2." + RawContacts._ID +
//                    " AND r1." + RawContacts.CONTACT_ID + "=r2." + RawContacts.CONTACT_ID +
//                    " AND r1." + RawContacts.ACCOUNT_NAME + "=r2." + RawContacts.ACCOUNT_NAME +
//                    " AND r1." + RawContacts.ACCOUNT_TYPE + "=r2." + RawContacts.ACCOUNT_TYPE +
//                    " AND r1." + RawContacts.DATA_SET + "=r2." + RawContacts.DATA_SET,
//                    null, null, null, null, null);
//            try {
//                while (cursor.moveToNext()) {
//                    long rawContactId = cursor.getLong(0);
//                    mContactAggregator.markForAggregation(rawContactId,
//                            RawContacts.AGGREGATION_MODE_DEFAULT, true);
//                    count++;
//                }
//            } finally {
//                cursor.close();
//            }
            count = mContactAggregator.markAllVisibleForAggregation(db);
            mContactAggregator.aggregateInTransaction(mTransactionContext.get(), db);
            updateSearchIndexInTransaction();
            db.setTransactionSuccessful();
            mContactsHelper.setProperty(PROPERTY_AGGREGATION_ALGORITHM,
                    String.valueOf(PROPERTY_AGGREGATION_ALGORITHM_VERSION));
        } finally {
            if (db != null) {
                db.endTransaction();
            }
            long end = SystemClock.currentThreadTimeMillis();
            Log.i(TAG, "Aggregation algorithm upgraded for " + count
                    + " contacts, in " + (end - start) + "ms");
        }
    }

    /* Visible for testing */
    boolean isPhone() {
        if (!sIsPhoneInitialized) {
            sIsPhone = new TelephonyManager(getContext()).isVoiceCapable();
            sIsPhoneInitialized = true;
        }
        return sIsPhone;
    }

    /**
     * Handles pinning update information from clients.
     *
     * @param values ContentValues containing key-value pairs where keys correspond to
     * the contactId for which to update the pinnedPosition, and the value is the actual
     * pinned position (a positive integer).
     * @return The number of contacts that had their pinned positions updated.
     */
    private int handlePinningUpdate(ContentValues values, boolean forceStarWhenPinning) {
        if (values.size() == 0) return 0;
        final SQLiteDatabase db = mDbHelper.get().getWritableDatabase();
        final String[] args;
        if (forceStarWhenPinning) {
            args = new String[3];
        } else {
            args = new String[2];
        }

        final StringBuilder sb = new StringBuilder();

        sb.append("UPDATE " + Tables.CONTACTS + " SET " + ContactsContract.Contacts.PINNED + "=?2");
        if (forceStarWhenPinning) {
            sb.append("," + Contacts.STARRED + "=?3");
        }
        sb.append(" WHERE " + Contacts._ID + " =?1;");
        final String contactSQL = sb.toString();

        sb.setLength(0);
        sb.append("UPDATE " + Tables.RAW_CONTACTS + " SET " + ContactsContract.RawContacts.PINNED + "=?2");
        if (forceStarWhenPinning) {
            sb.append("," + RawContacts.STARRED + "=?3");
        }
        sb.append(" WHERE " + RawContacts.CONTACT_ID + " =?1;");
        final String rawContactSQL = sb.toString();

        int count = 0;
        for (String id : values.keySet()) {
            count++;
            final long contactId;
            try {
                contactId = Integer.valueOf(id);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("contactId must be a positive integer. Found: "
                        + id);
            }

            // If contact is to be undemoted, go through a separate un-demotion process
            final String undemote = values.getAsString(id);
            if (ContactsContract.PinnedPositions.UNDEMOTE.equals(undemote)) {
                undemoteContact(db, contactId);
                continue;
            }

            final Integer pinnedPosition = values.getAsInteger(id);
            if (pinnedPosition == null) {
                throw new IllegalArgumentException("Pinned position must be an integer.");
            }
            args[0] = String.valueOf(contactId);
            args[1] = String.valueOf(pinnedPosition);
            if (forceStarWhenPinning) {
                args[2] = (pinnedPosition == ContactsContract.PinnedPositions.UNPINNED ||
                        pinnedPosition == ContactsContract.PinnedPositions.DEMOTED ? "0" : "1");
            }
            db.execSQL(contactSQL, args);

            db.execSQL(rawContactSQL, args);
        }
        return count;
    }

    private void undemoteContact(SQLiteDatabase db, long id) {
        final String[] arg = new String[1];
        arg[0] = String.valueOf(id);
        db.execSQL(UNDEMOTE_CONTACT, arg);
        db.execSQL(UNDEMOTE_RAW_CONTACT, arg);
    }

//    private boolean handleDataUsageFeedback(Uri uri) {
//        final long currentTimeMillis = System.currentTimeMillis();
//        final String usageType = uri.getQueryParameter(DataUsageFeedback.USAGE_TYPE);
//        final String[] ids = uri.getLastPathSegment().trim().split(",");
//        final ArrayList<Long> dataIds = new ArrayList<Long>();
//
//        for (String id : ids) {
//            dataIds.add(Long.valueOf(id));
//        }
//        final boolean successful;
//        if (TextUtils.isEmpty(usageType)) {
//            Log.w(TAG, "Method for data usage feedback isn't specified. Ignoring.");
//            successful = false;
//        } else {
//            successful = updateDataUsageStat(dataIds, usageType, currentTimeMillis) > 0;
//        }
//
//        // Handle old API. This doesn't affect the result of this entire method.
//        final String[] questionMarks = new String[ids.length];
//        Arrays.fill(questionMarks, "?");
//        final String where = Data._ID + " IN (" + TextUtils.join(",", questionMarks) + ")";
//        final Cursor cursor = mActiveDb.get().query(
//                Views.DATA,
//                new String[] { Data.CONTACT_ID },
//                where, ids, null, null, null);
//        try {
//            while (cursor.moveToNext()) {
//                mSelectionArgs1[0] = cursor.getString(0);
//                ContentValues values2 = new ContentValues();
//                values2.put(Contacts.LAST_TIME_CONTACTED, currentTimeMillis);
//                mActiveDb.get().update(Tables.CONTACTS, values2, Contacts._ID + "=?",
//                        mSelectionArgs1);
//                mActiveDb.get().execSQL(UPDATE_TIMES_CONTACTED_CONTACTS_TABLE, mSelectionArgs1);
//                mActiveDb.get().execSQL(UPDATE_TIMES_CONTACTED_RAWCONTACTS_TABLE, mSelectionArgs1);
//            }
//        } finally {
//            cursor.close();
//        }
//
//        return successful;
//    }
    private boolean handleDataUsageFeedback(Uri uri) {
        final long currentTimeMillis = Clock.getInstance().currentTimeMillis();
        final String usageType = uri.getQueryParameter(DataUsageFeedback.USAGE_TYPE);
        final String[] ids = uri.getLastPathSegment().trim().split(",");
        final ArrayList<Long> dataIds = new ArrayList<Long>(ids.length);

        for (String id : ids) {
            dataIds.add(Long.valueOf(id));
        }
        final boolean successful;
        if (TextUtils.isEmpty(usageType)) {
            Log.w(TAG, "Method for data usage feedback isn't specified. Ignoring.");
            successful = false;
        } else {
            successful = updateDataUsageStat(dataIds, usageType, currentTimeMillis) > 0;
        }

        // Handle old API. This doesn't affect the result of this entire method.
        final StringBuilder rawContactIdSelect = new StringBuilder();
        rawContactIdSelect.append("SELECT " + Data.RAW_CONTACT_ID + " FROM " + Tables.DATA +
                " WHERE " + Data._ID + " IN (");
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) rawContactIdSelect.append(",");
            rawContactIdSelect.append(ids[i]);
        }
        rawContactIdSelect.append(")");

        final SQLiteDatabase db = mDbHelper.get().getWritableDatabase();

        mSelectionArgs1[0] = String.valueOf(currentTimeMillis);

        db.execSQL("UPDATE " + Tables.RAW_CONTACTS +
                " SET " + RawContacts.LAST_TIME_CONTACTED + "=?" +
                "," + RawContacts.TIMES_CONTACTED + "=" +
                    "ifnull(" + RawContacts.TIMES_CONTACTED + ",0) + 1" +
                " WHERE " + RawContacts._ID + " IN (" + rawContactIdSelect.toString() + ")"
                , mSelectionArgs1);
        db.execSQL("UPDATE " + Tables.CONTACTS +
                " SET " + Contacts.LAST_TIME_CONTACTED + "=?1" +
                "," + Contacts.TIMES_CONTACTED + "=" +
                    "ifnull(" + Contacts.TIMES_CONTACTED + ",0) + 1" +
                "," + ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP + "=?1" +
                " WHERE " + Contacts._ID + " IN (SELECT " + RawContacts.CONTACT_ID +
                    " FROM " + Tables.RAW_CONTACTS +
                    " WHERE " + RawContacts._ID + " IN (" + rawContactIdSelect.toString() + "))"
                , mSelectionArgs1);

        return successful;
    }

    /**
     * Update {@link Tables#DATA_USAGE_STAT}.
     *
     * @return the number of rows affected.
     */
    @VisibleForTesting
    /* package */ int updateDataUsageStat(
            List<Long> dataIds, String type, long currentTimeMillis) {
        final int typeInt = sDataUsageTypeMap.get(type);
        final String where = DataUsageStatColumns.DATA_ID + " =? AND "
                + DataUsageStatColumns.USAGE_TYPE_INT + " =?";
        final String[] columns =
                new String[] { DataUsageStatColumns._ID, DataUsageStatColumns.TIMES_USED };
        final ContentValues values = new ContentValues();
        for (Long dataId : dataIds) {
            final String[] args = new String[] { dataId.toString(), String.valueOf(typeInt) };
            mActiveDb.get().beginTransaction();
            try {
                final Cursor cursor = mActiveDb.get().query(Tables.DATA_USAGE_STAT, columns, where,
                        args, null, null, null);
                try {
                    if (cursor.getCount() > 0) {
                        if (!cursor.moveToFirst()) {
                            Log.e(TAG,
                                    "moveToFirst() failed while getAccount() returned non-zero.");
                        } else {
                            values.clear();
                            values.put(DataUsageStatColumns.TIMES_USED, cursor.getInt(1) + 1);
                            values.put(DataUsageStatColumns.LAST_TIME_USED, currentTimeMillis);
                            mActiveDb.get().update(Tables.DATA_USAGE_STAT, values,
                                    DataUsageStatColumns._ID + " =?",
                                    new String[] { cursor.getString(0) });
                        }
                    } else {
                        values.clear();
                        values.put(DataUsageStatColumns.DATA_ID, dataId);
                        values.put(DataUsageStatColumns.USAGE_TYPE_INT, typeInt);
                        values.put(DataUsageStatColumns.TIMES_USED, 1);
                        values.put(DataUsageStatColumns.LAST_TIME_USED, currentTimeMillis);
                        mActiveDb.get().insert(Tables.DATA_USAGE_STAT, null, values);
                    }
                    mActiveDb.get().setTransactionSuccessful();
                } finally {
                    cursor.close();
                }
            } finally {
                mActiveDb.get().endTransaction();
            }
        }

        return dataIds.size();
    }

    /**
     * Returns a sort order String for promoting data rows (email addresses, phone numbers, etc.)
     * associated with a primary account. The primary account should be supplied from applications
     * with {@link ContactsContract#PRIMARY_ACCOUNT_NAME} and
     * {@link ContactsContract#PRIMARY_ACCOUNT_TYPE}. Null will be returned when the primary
     * account isn't available.
     */
    private String getAccountPromotionSortOrder(Uri uri) {
        final String primaryAccountName =
                uri.getQueryParameter(GnContactsContract.PRIMARY_ACCOUNT_NAME);
        final String primaryAccountType =
                uri.getQueryParameter(GnContactsContract.PRIMARY_ACCOUNT_TYPE);

        // Data rows associated with primary account should be promoted.
        if (!TextUtils.isEmpty(primaryAccountName)) {
            StringBuilder sb = new StringBuilder();
            sb.append("(CASE WHEN " + RawContacts.ACCOUNT_NAME + "=");
            DatabaseUtils.appendEscapedSQLString(sb, primaryAccountName);
            if (!TextUtils.isEmpty(primaryAccountType)) {
                sb.append(" AND " + RawContacts.ACCOUNT_TYPE + "=");
                DatabaseUtils.appendEscapedSQLString(sb, primaryAccountType);
            }
            sb.append(" THEN 0 ELSE 1 END)");
            return sb.toString();
        } else {
            return null;
        }
    }

    /**
     * Checks the URI for a deferred snippeting request
     * @return a boolean indicating if a deferred snippeting request is in the RI
     */
    private boolean deferredSnippetingRequested(Uri uri) {
        String deferredSnippeting =
            getQueryParameter(uri, SearchSnippetColumns.DEFERRED_SNIPPETING_KEY);
        return !TextUtils.isEmpty(deferredSnippeting) &&  deferredSnippeting.equals("1");
    }

    /**
     * Checks if query is a single word or not.
     * @return a boolean indicating if the query is one word or not
     */
    private boolean isSingleWordQuery(String query) {
        return query.split(QUERY_TOKENIZER_REGEX).length == 1;
    }

    /**
     * Checks the projection for a SNIPPET column indicating that a snippet is needed
     * @return a boolean indicating if a snippet is needed or not.
     */
    private boolean snippetNeeded(String [] projection) {
        return mDbHelper.get().isInProjection(projection, SearchSnippetColumns.SNIPPET);
    }
    
    /**
     * Used for PHONE_EMAIL_FILTER.
     * 
     * @param sb
     * @param normalizedName
     * @param allowEmailMatch
     */
    private void appendRawContactsByNormalizedNameFilter(StringBuilder sb, String normalizedName,
            boolean allowEmailMatch) {
        sb.append("(" +
                "SELECT " + NameLookupColumns.RAW_CONTACT_ID +
                " FROM " + Tables.NAME_LOOKUP +
                " WHERE " + NameLookupColumns.NORMALIZED_NAME +
                " GLOB '");
        sb.append(normalizedName);
        sb.append("*' AND " + NameLookupColumns.NAME_TYPE + " IN ("
                + NameLookupType.NAME_COLLATION_KEY + ","
                + NameLookupType.NICKNAME + ","
                + NameLookupType.NAME_EXACT + ","
                + NameLookupType.NAME_VARIANT);
        if (allowEmailMatch) {
            sb.append("," + NameLookupType.EMAIL_BASED_NICKNAME);
        }
        sb.append("))");
    }
    
	//Gionee:huangzy 20130220 modify for CR00769943 start
    private int updateGnDialerSearchDataForMultiDelete(String selection,
            String[] selectionArgs) {
    	
    	if (!ContactsProvidersApplication.sIsGnDialerSearchSupport) {
    		return 0;
    	}
    	
        final SQLiteDatabase db = mActiveDb.get();
        Cursor cursor = db.rawQuery("SELECT _id FROM raw_contacts WHERE " + selection, selectionArgs);
        ArrayList<Long> rawIdArray = new ArrayList<Long>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                rawIdArray.add(cursor.getLong(0));
            }
            cursor.close();
        }
        int count = mActiveDb.get().delete(Tables.RAW_CONTACTS, selection, selectionArgs);
        
        for (long rawContactId: rawIdArray) {
        	updateGnDialerSearchDataForDelete(rawContactId);
        }
        
        return count;
    }
    
    private void updateGnDialerSearchDataForDelete(long rawContactId) {
    	GnDialerSearchHelper.getInstance().deleteNameForDialerSearch(mActiveDb.get(), rawContactId);
    }
	//Gionee:huangzy 20130220 modify for CR00769943 end
    
    // The following lines are provided and maintained by Mediatek inc.

    private boolean isLocalAccount(Cursor c) {
        //String accountName = c.getString(0);
        String accountType = c.getString(1);
        Log.d(TAG, "isLocalAccount(), accountType= "+ accountType + "****");
        if (accountType.equals("SIM Account") ||
            accountType.equals("USIM Account") ||
            accountType.equals("Local Phone Account")) {
            return true;
        }
        return false;
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received Intent:" + intent);
            //mListenerHandler.sendEmptyMessage(MESSAGE_LOAD_DATA);
            /*new Thread() {
                public void run() {
            loadAccountsInBackground();
                }
            }.start();*/
            scheduleBackgroundTask(BACKGROUND_TASK_LOAD_LOCAL_ACCOUNT);
            //loadAccountsInBackground();
            //loadLocalSimAccounts("USIM0", "USIM Account");
        }

    };

    protected void handleLocalAccountChanged() {
        mListenerThread = new HandlerThread("LocalAccountChangeListener");
        mListenerThread.start();
        mListenerHandler = new Handler(mListenerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_LOAD_DATA:
                        //loadAccountsInBackground();
                        break;
                }
            }
        };

    }
    

    protected void loadAccountsInBackground() {
        Log.d(TAG, "loadAccountsInBackground()+ ");
        // TODO : Check the unit test.
        // Load Slot 1 SIM account Type
        int slotid = SimCardUtils.SimSlot.SLOT_ID1;
        String simAccountType = ACCOUNT_TYPE_SIM;
        String simAccountName = null;
        if (SimCardUtils.isSimInserted(slotid)) {
            simAccountType = getAccountTypeBySlot(slotid);
            simAccountName = getSimAccountNameBySlot(slotid);
            Log.i(TAG, "loadAccountsInBackground slotid:" + slotid + " simAccountType:"
                    + simAccountType + " simAccountName:" + simAccountName);
            loadLocalSimAccounts(simAccountName, simAccountType, slotid);
        } else {
            deleteLocalSimAccounts(ACCOUNT_NAME_SIM);
            deleteLocalSimAccounts(ACCOUNT_NAME_USIM);

        }

        // Load Slot 2 SIM account Type
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            slotid = SimCardUtils.SimSlot.SLOT_ID2;
            simAccountType = ACCOUNT_TYPE_SIM;
            simAccountName = null;
            if (SimCardUtils.isSimInserted(slotid)) {
                simAccountType = getAccountTypeBySlot(slotid);
                simAccountName = getSimAccountNameBySlot(slotid);
                Log.i(TAG, "loadAccountsInBackground slotid2:" + slotid + " simAccountType:"
                        + simAccountType + " simAccountName:" + simAccountName);
                
                loadLocalSimAccounts(simAccountName, simAccountType, slotid);
            } else {
                deleteLocalSimAccounts(ACCOUNT_NAME_SIM2);
                deleteLocalSimAccounts(ACCOUNT_NAME_USIM2);
            }
        }         

        }

    protected void loadLocalSimAccounts(String accountName, String accountType, int slotid) {
        Log.d(TAG, "loadLocalSimAccounts()+ ");
        // TODO : Check the unit test.
        SQLiteDatabase db = mDbHelper.get().getWritableDatabase();
        mActiveDb.set(db);

        /*
         * Feature Fix by Mediatek Begin Original Android code:
         * db.beginTransaction(); Description: added for low memory handle, CR
         * ALPS00071632
         */
        try {
            db.beginTransaction();
        } catch (android.database.sqlite.SQLiteDiskIOException ex) {
            Log.w(TAG, "[loadLocalSimAccounts]catch SQLiteDiskIOException.");
            return;
        }
        /*
         * Feature Fix by Mediatek End
         */

        try {

            // Add an account entry with an empty data set into settings table.

            Cursor cursor = db.rawQuery("SELECT " + Settings.ACCOUNT_NAME + "," + Settings.ACCOUNT_TYPE
                    + " FROM " + Tables.SETTINGS + " WHERE " + Settings.ACCOUNT_NAME + " = '"
                    + accountName + "'", null);
            try {
                Log.i(TAG, "loadLocalSimAccounts -cursor.count:" + ((cursor == null) ? 0 : cursor.getCount()));
                if (cursor != null && cursor.getCount() == 0) {
                    // Add an account entry with an empty data set into settings
                    // table.
                    db.execSQL("INSERT INTO " + Tables.SETTINGS + " (" + Settings.ACCOUNT_NAME
                            + ", " + Settings.ACCOUNT_TYPE + ", " + Settings.DATA_SET + ", "
                            + Settings.UNGROUPED_VISIBLE + ", " + Settings.SHOULD_SYNC
                            + ") VALUES (?, ?, ?, ?, ?)", new String[] {
                            accountName, accountType, null, "1", "1"
                    });
                }
            } finally {
                cursor.close();
            }
            if (slotid == SimCardUtils.SimSlot.SLOT_ID1) {
                if (accountName == ACCOUNT_NAME_SIM) {
                    Cursor c = db.rawQuery("SELECT " + Settings.ACCOUNT_NAME + ","
                            + Settings.ACCOUNT_TYPE + " FROM " + Tables.SETTINGS + " WHERE "
                            + Settings.ACCOUNT_NAME + " = '" + ACCOUNT_NAME_USIM + "'", null);
                    try {
                        Log.i(TAG, "Handle USIM1 Accounts -c.count:"
                                + ((c == null) ? 0 : c.getCount()));
                        if (c != null && c.getCount() != 0) {
                            db.execSQL("DELETE FROM " + Tables.SETTINGS + " WHERE "
                                    + Settings.ACCOUNT_NAME + " = '" + ACCOUNT_NAME_USIM + "'");
                        }
                    } finally {
                        c.close();
                    }
                } else if (accountName == ACCOUNT_NAME_USIM) {
                    Cursor c = db.rawQuery("SELECT " + Settings.ACCOUNT_NAME + ","
                            + Settings.ACCOUNT_TYPE + " FROM " + Tables.SETTINGS + " WHERE "
                            + Settings.ACCOUNT_NAME + " = '" + ACCOUNT_NAME_SIM + "'", null);
                    try {
                        Log.i(TAG, "Handle SIM1 Accounts -c.count:"
                                + ((c == null) ? 0 : c.getCount()));
                        if (c != null && c.getCount() != 0) {
                            db.execSQL("DELETE FROM " + Tables.SETTINGS + " WHERE "
                                    + Settings.ACCOUNT_NAME + " = '" + ACCOUNT_NAME_SIM + "'");
                        }
                    } finally {
                        c.close();
                    }
                }
            }
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                if (slotid == SimCardUtils.SimSlot.SLOT_ID2) {
                    if (accountName == ACCOUNT_NAME_SIM2) {
                        Cursor c = db.rawQuery("SELECT " + Settings.ACCOUNT_NAME + ","
                                + Settings.ACCOUNT_TYPE + " FROM " + Tables.SETTINGS + " WHERE "
                                + Settings.ACCOUNT_NAME + " = '" + ACCOUNT_NAME_USIM2 + "'", null);
                        try {
                            Log.i(TAG, "Handle USIM2 Accounts -c.count:"
                                    + ((c == null) ? 0 : c.getCount()));
                            if (c != null && c.getCount() != 0) {
                                db.execSQL("DELETE FROM " + Tables.SETTINGS + " WHERE "
                                        + Settings.ACCOUNT_NAME + " = '" + ACCOUNT_NAME_USIM2 + "'");
                            }
                        } finally {
                            c.close();
                        }
                    } else if (accountName == ACCOUNT_NAME_USIM2) {
                        Cursor c = db.rawQuery("SELECT " + Settings.ACCOUNT_NAME + ","
                                + Settings.ACCOUNT_TYPE + " FROM " + Tables.SETTINGS + " WHERE "
                                + Settings.ACCOUNT_NAME + " = '" + ACCOUNT_NAME_SIM2 + "'", null);
                        try {
                            Log.i(TAG, "Handle SIM2 Accounts -c.count:"
                                    + ((c == null) ? 0 : c.getCount()));
                            if (c != null && c.getCount() != 0) {
                                db.execSQL("DELETE FROM " + Tables.SETTINGS + " WHERE "
                                        + Settings.ACCOUNT_NAME + " = '" + ACCOUNT_NAME_SIM2 + "'");
                            }
                        } finally {
                            c.close();
                        }
                    }
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

    }


    protected void deleteLocalSimAccounts(String accountName) {
        Log.d(TAG, "deleteLocalSimAccounts()+ accountName="+ accountName);
        // TODO : Check the unit test.
        SQLiteDatabase db = mDbHelper.get().getWritableDatabase();
        mActiveDb.set(db);

        /*
         * Feature Fix by Mediatek Begin Original Android code:
         * db.beginTransaction(); Description: added for low memory handle, CR
         * ALPS00071632
         */
        try {
            db.beginTransaction();
        } catch (android.database.sqlite.SQLiteDiskIOException ex) {
            Log.w(TAG, "[deleteLocalSimAccounts]catch SQLiteDiskIOException.");
            return;
        }
        /*
         * Feature Fix by Mediatek End
         */

        try {

                Cursor c = db.rawQuery("SELECT " + Settings.ACCOUNT_NAME + ","
                        + Settings.ACCOUNT_TYPE + " FROM " + Tables.SETTINGS + " WHERE "
                        + Settings.ACCOUNT_NAME + " = '" + accountName + "'", null);
                try {
                    Log.i(TAG, "Handle Accounts -c.count:"
                            + ((c == null) ? 0 : c.getCount()));
                    if (c != null && c.getCount() != 0) {
                        db.execSQL("DELETE FROM " + Tables.SETTINGS + " WHERE "
                                + Settings.ACCOUNT_NAME + " = '" + accountName +"'");
                    }
                } finally {
                    c.close();
                }
            db.setTransactionSuccessful();
        } finally {
            try {
            db.endTransaction();
            } catch (android.database.sqlite.SQLiteDiskIOException ex) {
                Log.w(TAG, "[deleteLocalSimAccounts]catch SQLiteDiskIOException when endTransaction.");
                return;
            } catch(android.database.sqlite.SQLiteFullException ex) {
                Log.w(TAG, "[updateAccountsInBackground]catch SQLiteFullException when endTransaction.");
                return;
            }
        }

    }

    protected void loadLocalPhoneAccounts() {
        Log.d(TAG, "loadLocalPhoneAccounts()+ ");
        // TODO : Check the unit test.
        SQLiteDatabase db = mDbHelper.get().getWritableDatabase();
        mActiveDb.set(db);
        
        /*
         * Feature Fix by Mediatek Begin
         *  
         * Original Android code:
         * db.beginTransaction();
         * 
         * Description:
         * added for low memory handle, CR ALPS00071632
         */
        try {
            db.beginTransaction();
        } catch (android.database.sqlite.SQLiteDiskIOException ex) {
            Log.w(TAG, "[updateAccountsInBackground]catch SQLiteDiskIOException.");
            return;
        } 
        /*
         * Feature Fix by Mediatek End
         */

      try {
         // Add an account entry with an empty data set into settings table.

         Cursor cursor = db.rawQuery("SELECT " + Settings.ACCOUNT_NAME + "," + Settings.ACCOUNT_TYPE
                 + " FROM " + Tables.SETTINGS + " WHERE " + Settings.ACCOUNT_NAME + " = 'Phone'", null);
         try {
             Log.i(TAG, "loadLocal Phone Accounts -cursor.count:" + ((cursor == null) ? 0 : cursor.getCount()));
             if (cursor != null && cursor.getCount() == 0) {
                 // Add an account entry with an empty data set into settings
                 // table.
                 db.execSQL("INSERT INTO " + Tables.SETTINGS + " (" + Settings.ACCOUNT_NAME
                         + ", " + Settings.ACCOUNT_TYPE + ", " + Settings.DATA_SET + ", "
                         + Settings.UNGROUPED_VISIBLE + ", " + Settings.SHOULD_SYNC
                         + ") VALUES (?, ?, ?, ?, ?)", new String[] {
                         "Phone", "Local Phone Account", null, "1", "1"
                 });
             }
         } finally {
             cursor.close();
         }

         db.setTransactionSuccessful();
        } finally {
            try {
                db.endTransaction();
            } catch (android.database.sqlite.SQLiteDiskIOException ex) {
                Log.w(TAG, "[updateAccountsInBackground]catch SQLiteDiskIOException.");
                return;
            } catch(android.database.sqlite.SQLiteFullException ex) {
                Log.w(TAG, "[updateAccountsInBackground]catch SQLiteFullException.");
                return;
            }
        }
    }

    public String getAccountTypeBySlot(int slotId) {
        Log.i(TAG, "getAccountTypeBySlot()+ - slotId:" + slotId);
        if (slotId < SimCardUtils.SimSlot.SLOT_ID1 || slotId > SimCardUtils.SimSlot.SLOT_ID2) {
            Log.e(TAG, "Error! - slot id error. slotid:" + slotId);
            return null;
        }
        int simtype = SimCardUtils.SimType.SIM_TYPE_SIM;
        String simAccountType = ACCOUNT_TYPE_SIM;

        if (SimCardUtils.isSimInserted(slotId)) {
            simtype = SimCardUtils.getSimTypeBySlot(slotId);
            if (SimCardUtils.SimType.SIM_TYPE_USIM == simtype) {
                simAccountType = ACCOUNT_TYPE_USIM;
            }
        } else {
            Log.e(TAG, "Error! getAccountTypeBySlot - slotId:" + slotId + " no sim inserted!");
            simAccountType = null;
        }
        Log.i(TAG, "getAccountTypeBySlot()- - slotId:" + slotId + " AccountType:" + simAccountType);
        return simAccountType;
    }

    public String getSimAccountNameBySlot(int slotId) {
        String retSimName = null;
        int simType = SimCardUtils.SimType.SIM_TYPE_SIM;

        Log.i(TAG, "getSimAccountNameBySlot()+ slotId:" + slotId);
        if (!SimCardUtils.isSimInserted(slotId)) {
            Log.e(TAG, "getSimAccountNameBySlot Error! - SIM not inserted!");
            return retSimName;
        }

        simType = SimCardUtils.getSimTypeBySlot(slotId);
        Log.i(TAG, "getSimAccountNameBySlot() slotId:" + slotId + " simType(0-SIM/1-USIM):" + simType);

        if (SimCardUtils.SimType.SIM_TYPE_SIM == simType) {
            retSimName = ACCOUNT_NAME_SIM;
            if (SimCardUtils.SimSlot.SLOT_ID2 == slotId) {
                retSimName = ACCOUNT_NAME_SIM2;
            }
        } else if (SimCardUtils.SimType.SIM_TYPE_USIM == simType) {
            retSimName = ACCOUNT_NAME_USIM;
            if (SimCardUtils.SimSlot.SLOT_ID2 == slotId) {
                retSimName = ACCOUNT_NAME_USIM2;
            }
        } else {
            Log.e(TAG, "getSimAccountNameBySlot() Error!  get SIM Type error! simType:" + simType);
        }

        Log.i(TAG, "getSimAccountNameBySlot()- slotId:" + slotId + " SimName:" + retSimName);
        return retSimName;
    }

    private void registerReceiverOnSimStateAndInfoChanged() {
        Log.i(TAG, "registerReceiverOnSimStateAndInfoChanged");
        IntentFilter simFilter = new IntentFilter();
        // For SIM Info Changed
        //simFilter.addAction(TelephonyIntents.ACTION_SIM_INFO_UPDATE);
        simFilter.addAction("android.intent.action.SIM_INFO_UPDATE"); //qc--mtk
        // ToDo: Add SIM State Changed
        
        getContext().registerReceiver(mBroadcastReceiver, simFilter);
    }

    /*
     * Bug Fix by Mediatek Begin.
     *     
     *   CR ID: ALPS000111101
     *   Descriptions:
     */
    private boolean ourSpecialAccount(AccountWithDataSet accountWithDataSet) {
        if (accountWithDataSet.getDataSet() == null) {
           if ((accountWithDataSet.getAccountName().equals("Phone") && accountWithDataSet.getAccountType().equals("Local Phone Account")) 
               || accountWithDataSet.getAccountType().equals("SIM Account") 
               || accountWithDataSet.getAccountType().equals("USIM Account")
               ) {
               Log.d(TAG, "[canDeleteAccount] -> not delete: " + accountWithDataSet);
               return true;           
           }
        }
        return false;
    }
    private boolean mUseStrictPhoneNumberComparation;
    /*
     * Bug Fix by Mediatek End.
     */

    /**
     * An entry in raw_contacts cache.
     */
    public static class RawContactEntry {
        public RawContactEntry (long rawContactId, long contactId) {
            this.rawContactId = rawContactId;
            this.contactId = contactId;
        }

        long rawContactId;
        long contactId;
    }

    public int deleteRawContactInOneBatch(Uri uri, String selection, String[] selectionArgs) {
        mAggregator.get().invalidateAggregationExceptionCache();
        mProviderStatusUpdateNeeded = true;

        // Find and delete stream items associated with the raw contact.
        Cursor streamItemsCursor = mActiveDb.get().query(Tables.STREAM_ITEMS,
                new String[]{StreamItems._ID},
                StreamItems.RAW_CONTACT_ID + " IN (SELECT " + RawContacts._ID + " FROM "
                + Tables.RAW_CONTACTS + " WHERE (" + selection + "))", selectionArgs,
                null, null, null);
        try {
            while (streamItemsCursor.moveToNext()) {
                deleteStreamItem(streamItemsCursor.getLong(0));
            }
        } finally {
            streamItemsCursor.close();
        }

        mActiveDb.get().delete(
                Tables.PRESENCE,
                PresenceColumns.RAW_CONTACT_ID + " IN (SELECT " + RawContacts._ID + " FROM "
                        + Tables.RAW_CONTACTS + " WHERE (" + selection + "))", selectionArgs);

        ArrayList<RawContactEntry> rawContactsList = new ArrayList<RawContactEntry>();

        Cursor rawContactsCursor = mActiveDb.get().query(Tables.RAW_CONTACTS, 
                new String[] { RawContacts._ID, RawContacts.CONTACT_ID}, 
                appendAccountToSelection(uri, selection), selectionArgs, null, null, null);
        try {
            while (rawContactsCursor.moveToNext()) {
                final long rawContactId = rawContactsCursor.getLong(0);
                long contactId = rawContactsCursor.getLong(1);
                rawContactsList.add(new RawContactEntry(rawContactId, contactId));
            }
        } finally {
            rawContactsCursor.close();
        }

        int count = mActiveDb.get().delete(Tables.RAW_CONTACTS, selection, selectionArgs);

        if (rawContactsList != null && rawContactsList.size() > 0) {
            for (RawContactEntry rawContact : rawContactsList) {
                Log.d(TAG, "updateAggregateData begin");
                mAggregator.get().updateAggregateData(mTransactionContext.get(),
                        rawContact.contactId);
                Log.d(TAG, "updateAggregateData end");
                
                //Gionee:huangzy 20130220 add for CR00769943 start
            	if (ContactsProvidersApplication.sIsGnDialerSearchSupport) {
            		updateGnDialerSearchDataForDelete(rawContact.rawContactId);
            	}
            	//Gionee:huangzy 20130220 add for CR00769943 end
            }
        }

        return count;
    }

    static public boolean isCmcc() {    	          
    	return "OP01".equals(SystemProperties.get("ro.operator.optr"));
    }
    
    // The previous lines are provided and maintained by Mediatek inc.

    private int deleteFrequentContactedRecord(String whereClause, String[] whereArgs) {
    	return mActiveDb.get().delete(Tables.DATA_USAGE_STAT, whereClause, whereArgs);
    }
    
    // aurora wangth 20140612 reject begin
    private String queryNumberForId (String id) {
    	Log.e(TAG, "id = " + id);
        String result = null;
        Cursor cursor = mActiveDb.get().query("black", new String[]{"number"}, "_id=" + id, null, null, null, null);
        
        if (cursor != null) {
    		if (cursor.moveToFirst()) {
    		    result = cursor.getString(0);
    		    Log.e(TAG, "result = " + result);
    		}
    		
    		cursor.close();
    	}
        
        return result;
    }
    
    /*
     * flag: true(rejected), false(not rejected)
     */
    private void updateDataForBlack(String number, int rejected, boolean flag) {
    }
    
    /*
     * flag: true(rejected), false(not rejected)
     */
    private void updateCallsForBlack(String number, String name, boolean flag) {
    	if (number == null) {
    	    return;
    	}
    	
    	int rejected = 0;
    	if (flag) {
    	    rejected = 1;
    	}
    	
    	try {
    	    ContentValues cv = new ContentValues();
            cv.put("reject", rejected);
            cv.put("black_name", name);
            mActiveDb.get().update(Tables.CALLS, cv,
                    "PHONE_NUMBERS_EQUAL(number, " + number + ", 0) and type in (1, 3) and reject in(0, 1)", null);
            cv.clear();
            cv = null;
            
            getContext().getContentResolver().notifyChange(
                    Calls.CONTENT_URI, null, false);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    private void updateCallsForBlack(String number) {
    	if (!ContactsProvidersApplication.sIsAuroraRejectSupport || number == null) {
    	    return;
    	}
    	
    	try {
    	    ContentValues cv = new ContentValues();
            String black_name = null;
            cv.put("black_name", black_name);
            mActiveDb.get().update(Tables.CALLS, cv,
                    "PHONE_NUMBERS_EQUAL(number, " + number + ", 0) and type in (1, 3) and reject=1", null);
            cv.clear();
            cv = null;
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    private void updateRejectMms(int type, int isBlack, String name, String number, int reject) {
        ContentValues values = new ContentValues();
        values.put("insert_update_delete", type);
        if (type < 3) {
            values.put("isblack", isBlack);
            if (name != null) {
                values.put("name", name);
            }
            values.put("rejected", reject);
        }
        values.put("number", number);
        values.put("feature_name", "reject");
        
        getContext().getContentResolver().update(Uri.parse("content://mms-sms/aurora_special_feature"), values, null, null);
        values.clear();
        values = null;
    }
    // aurora wangth 20140612 reject end


	public ParcelFileDescriptor openFile(Uri uri, String mode)
			throws FileNotFoundException {
		System.out.println("openFile");
		switch (sUriMatcher.match(uri)) {
		case SYNC_ACCESSORY:
			File fileDir = new File(getContext().getFilesDir(), "photos");
			if (!fileDir.exists() && fileDir.mkdir()) {
				return null;
			}
			String path = uri.getQueryParameter("path");
			System.out.println("uri="+uri.toString());
			System.out.println("path ="+path);
			File pathFile = new File(path);
			if ("w".equals(mode)) {
				if (pathFile.exists()){
					System.out.println("pathFile is exists()");
					return ParcelFileDescriptor.open(pathFile,
							ParcelFileDescriptor.MODE_WRITE_ONLY|ParcelFileDescriptor.MODE_APPEND);
				}else{
					System.out.println("pathFile is not exists()");
					
				}
			} else if ("r".equals(mode)) {
				if (pathFile.exists()) {
					return ParcelFileDescriptor.open(pathFile,
							ParcelFileDescriptor.MODE_READ_ONLY);
				}
			}
			break;
		}
		return super.openFile(uri, mode);
	}

    
    // aurora wangth add for privacy contacts begin
    /**
     * flag : true:ours; false:others
     */
    private String parseSelection(String selection, boolean flag) {
        String sel = selection;
        String defaultStr = "=0";
        if (flag) {
            defaultStr = ">-1";
        }
        
        if (sel != null && !TextUtils.isEmpty(sel)) {
            if (!sel.contains(AURORA_DEFAULT_PRIVACY_COLUMN)) {
                sel = "(" + sel + ") AND " + AURORA_DEFAULT_PRIVACY_COLUMN + defaultStr;
            }
        } else {
            sel = AURORA_DEFAULT_PRIVACY_COLUMN + defaultStr;
        }
        
        Log.i(TAG, "sel = " + sel);
        
        return sel;
    }
    
    private String parsePhonelookupSelection(String selection, boolean flag) {
        String sel = selection;
        String defaultStr = "=0";
        if (flag) {
            defaultStr = ">-1";
        }
        
        if (sel != null && !TextUtils.isEmpty(sel)) {
            if (!sel.contains(AURORA_DEFAULT_PRIVACY_COLUMN)) {
                sel = "(" + sel + ") AND contacts_view." + AURORA_DEFAULT_PRIVACY_COLUMN + defaultStr;
            }
        } else {
            sel = " contacts_view." + AURORA_DEFAULT_PRIVACY_COLUMN + defaultStr;
        }
        
        Log.i(TAG, "sel = " + sel);
        
        return sel;
    }
    
    private void updateNoNameCalls(String number, int dataId, int rawContactId) {
        if (!ContactsProvidersApplication.sIsAuroraPrivacySupport || number == null) {
            return;
        }
        
        try {
            ContentValues cv = new ContentValues();
            long privacyId = AuroraPrivacyUtils.mCurrentAccountId;
            cv.put("privacy_id", privacyId);
            cv.put("data_id", dataId);
            cv.put("raw_contact_id", rawContactId);
            mActiveDb.get().update(Tables.CALLS, cv,
                    "PHONE_NUMBERS_EQUAL(number, " + number + ", 0) and privacy_id=0 and name is null", null);
            cv.clear();
            cv = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logs(String str) {
        Log.i(TAG, str);
    }
    
    // aurora wangth add for privacy contacts end
    
    
    
    public String getValue(JSONObject item,String name){
		try {
			return (String)item.get(name);
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			return null;
		}
	}
    
	public Cursor getSnycUpCount(String limit, SQLiteDatabase sb) {
		String accessoryid;
		String mimetype;
		String syncid;
		String photoId;
		String data1;
		String deleted;
		String date;
		String sync4;
		List<String> bodys = new ArrayList<String>();
		List<String> accessorys = new ArrayList<String>();
		List<String> infos = new ArrayList<String>();
		ContentResolver resolver = getContext().getContentResolver();
		final Long accountId = mDbHelper.get().getAccountIdOrNull(mDbHelper.get().getDefaultAccount());
		Cursor cursor = sb.query(Tables.RAW_CONTACTS, new String[] { "_id",
				"sync4", "starred", "gn_version", "deleted" ,"sync3"},
				"account_id=? and dirty=?", new String[] {
				String.valueOf(accountId), "1" }, null, null, null, limit);
		JSONObject Object;
		JSONObject info;
		while (cursor.moveToNext()) {
			accessoryid = null;
			mimetype = null;
			syncid = null;
			photoId = null;
			data1 = null;
			deleted = null;
			date = null;
			sync4 = null;
			info = new JSONObject();
			Object = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			int id = cursor.getInt(0);
			accessoryid = id + "";
			try {
				Object.put("id", accessoryid);
				sync4 = cursor.getString(1);
				info.put("syncid", sync4);
				Object.put("starred", cursor.getString(2));
				Object.put("syncid", sync4);
				date = cursor.getString(3);
				info.put("date", date);
				Object.put("date", date);
				deleted = cursor.getString(4);
				Object.put("isdelete", deleted);
				if (sync4 == null) {
					if (Integer.parseInt(deleted) == 0) {
						info.put("op", 0);
					}
				} else {
					if (Integer.parseInt(deleted) == 0) {
						info.put("op", 1);
					} else {
						info.put("op", 2);
					}
				}
				Object.put("localFlag", cursor.getString(5));
				info.put("localFlag", cursor.getString(5));
				info.put("id", accessoryid);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			Uri uri = Uri.parse("content://com.android.contacts/raw_contacts/"
					+ id + "/data");
			Cursor cursor2 = resolver.query(uri, null, null, null, null);
			while (cursor2.moveToNext()) {
				try {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("syncid", cursor2.getString(cursor2
							.getColumnIndex("data_sync4")));
					jsonObject.put("_id",
							cursor2.getString(cursor2.getColumnIndex("_id")));
					mimetype = cursor2.getString(cursor2
							.getColumnIndex("mimetype"));
					jsonObject.put("mimetype", mimetype);
					if (mimetype.equals("vnd.android.cursor.item/photo")) {
						syncid = cursor2.getString(cursor2
								.getColumnIndex("data_sync4"));
						photoId = cursor2.getString(cursor2
								.getColumnIndex("data14"));
					}
					data1 = cursor2.getString(cursor2.getColumnIndex("data1"));
					if (mimetype
							.equals("vnd.android.cursor.item/group_membership")) {
						Cursor cur = getContext().getContentResolver().query(
								Groups.CONTENT_URI, new String[] { "title" },
								"_id=?", new String[] { data1 }, null);
						if (cur.moveToNext()) {
							data1 = cur.getString(0);
						}
					}
					jsonObject.put("data1", data1);
					jsonObject.put("data2",
							cursor2.getString(cursor2.getColumnIndex("data2")));
					jsonObject.put("data3",
							cursor2.getString(cursor2.getColumnIndex("data3")));
					jsonObject.put("data4",
							cursor2.getString(cursor2.getColumnIndex("data4")));
					jsonObject.put("data5",
							cursor2.getString(cursor2.getColumnIndex("data5")));
					jsonObject.put("data6",
							cursor2.getString(cursor2.getColumnIndex("data6")));
					jsonObject.put("data7",
							cursor2.getString(cursor2.getColumnIndex("data7")));
					jsonObject.put("data8",
							cursor2.getString(cursor2.getColumnIndex("data8")));
					jsonObject.put("data9",
							cursor2.getString(cursor2.getColumnIndex("data9")));
					jsonObject.put("data10", 
							cursor2.getString(cursor2.getColumnIndex("data10")));
					jsonObject.put("data11", 
							cursor2.getString(cursor2.getColumnIndex("data11")));
					jsonObject.put("data12", 
							cursor2.getString(cursor2.getColumnIndex("data12")));
					jsonObject.put("data13", 
							cursor2.getString(cursor2.getColumnIndex("data13")));
					jsonArray.put(jsonObject);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				Object.put("data", jsonArray);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Object.toString()=" + Object.toString());
			System.out.println("info.toString()=" + info.toString());
			bodys.add(Object.toString());
			infos.add(info.toString());
			JSONArray jsonArrays = new JSONArray();
			JSONObject object = new JSONObject();
			JSONObject objectAcc = new JSONObject();
			try {
				object.put("syncid", syncid);
				object.put("type", "providerFile");
				object.put("accessoryid", accessoryid);
				object.put("date", date);
				object.put("path", getContext().getFilesDir() + "/photos/"
						+ photoId);
				jsonArrays.put(object);
				objectAcc.put("accessory", jsonArrays);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			System.out.println("objectAcc.toString()=" + objectAcc.toString());
			if (photoId == null) {
				accessorys.add(null);
			} else {
				accessorys.add(objectAcc.toString());
			}
		}
		String[] tableCursor = new String[] { "body", "accessory", "info" };
		MatrixCursor callCursor = new MatrixCursor(tableCursor);
		for (int i = 0; i < bodys.size(); i++) {
			callCursor.addRow(new Object[] { bodys.get(i), accessorys.get(i),infos.get(i) });
		}
		return callCursor;
	}
    
    
	public boolean syncDownOne(String body, JSONObject accessoryObjec,String  sync4) {
		String str;
		ArrayList<String> clientDataIds=new ArrayList<String>(); 
		ArrayList<String> serverDataIds=new ArrayList<String>(); 
		Uri uris = Uri.parse("content://com.android.contacts/raw_contacts");
		ContentResolver resolver = getContext().getContentResolver();
		JSONObject object = null;
		try {
			object = new JSONObject(body);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String data1, data2, data3, data4, data5, data6, data7, data8, data9, data10, data11, data12, data13, mimetype, data_sync4=null, updatePhoto = null;
		String starred;
		long contact_id=0;
		JSONObject item = null;
		int deleted=0;
		try {
			deleted=object.getInt("isdelete");
		} catch (Exception e) {
			e.printStackTrace();
		}
		starred = getValue(object, "starred");
		if(starred==null){
			System.out.println("starredxxxxxxxxxxxxxxxxxxxx");
			return false;
		}
		ContentValues value = new ContentValues();
		value.put("starred", starred);
		JSONArray jsonArray = null;
		try {
			jsonArray = (JSONArray) object.get("data");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("syncDownOnexxxxxxxxxxxxxxxxxxxx");
			return false;
			
		}	
		Cursor cursor=resolver.query(uris, new String[]{"_id"}, "sync4=?", new String[]{sync4}, null);
		if(cursor.moveToNext()){
			contact_id=cursor.getInt(0);
			if(deleted==0){
				uris = Uri.parse("content://com.android.contacts/raw_contacts/"+contact_id+"/data");
				Cursor cursorIds=resolver.query(uris, new String[]{"data_sync4"}, null, null, null);
				while(cursorIds.moveToNext()){
					clientDataIds.add(cursorIds.getString(0));
				}
				if(resolver.update(Uri.parse("content://com.android.contacts/raw_contacts"), value,  "sync4=?", new String[]{sync4})<1){
					return false;
				}
				for (int i = 0; i < jsonArray.length(); i++) { 
					try {
						item = jsonArray.getJSONObject(i); 
					} catch (Exception e) {
						e.printStackTrace();
					}
					data_sync4 = getValue(item, "syncid");
					serverDataIds.add(data_sync4);
				}
				
				for(int i=0;i<jsonArray.length();i++){
					value.clear();
					try {
						item = jsonArray.getJSONObject(i); 
					} catch (Exception e) {
						e.printStackTrace();
					}
					mimetype = getValue(item, "mimetype");
					if (mimetype.equals("vnd.android.cursor.item/photo")) {
						updatePhoto = getValue(item, "syncid");
					}
					data1 = getValue(item, "data1");
					if (mimetype.equals("vnd.android.cursor.item/group_membership")) {
						Cursor mcursor = getContext().getContentResolver().query(
								Groups.CONTENT_URI, new String[] { "_id" },
								"title=? and deleted=?", new String[] { data1, "0" },
								null);
						if (mcursor.moveToNext()) {
							data1 = mcursor.getString(0);
						} else {
							ContentValues val = new ContentValues();
							val.put("title", data1);
							Uri ur = getContext().getContentResolver().insert(
									Groups.CONTENT_URI, val);
							if (ContentUris.parseId(ur) < 1) {
								return false;
							}
							data1 = ur.getPathSegments().get(1);
						}
					}
					data2 = getValue(item, "data2");
					data3 = getValue(item, "data3");
					data4 = getValue(item, "data4");
					data5 = getValue(item, "data5");
					data6 = getValue(item, "data6");
					data7 = getValue(item, "data7");
					data8 = getValue(item, "data8");
					data9 = getValue(item, "data9");
					data10 = getValue(item, "data10");
					data11 = getValue(item, "data11");
					data12 = getValue(item, "data12");
					data13 = getValue(item, "data13");
					value.put("data1", data1);
					value.put("data2", data2);
					value.put("data3", data3);
					value.put("data4", data4);
					value.put("data5", data5);
					value.put("data6", data6);
					value.put("data7", data7);
					value.put("data8", data8);
					value.put("data9", data9);
					value.put("data10", data10);
					value.put("data11", data11);
					value.put("data12", data12);
					value.put("data13", data13);
					if(clientDataIds.contains(serverDataIds.get(i))){
						if(resolver.update(Uri.parse("content://com.android.contacts/data"), value, "mimetype=? and raw_contact_id=? and data_sync4=?", new String[]{mimetype,cursor.getString(0),serverDataIds.get(i)})<1){
//							return false;
						}
					}else{
						value.put("raw_contact_id", cursor.getString(0));
						value.put(Data.MIMETYPE, mimetype);
						value.put("data_sync4", data_sync4);
						Uri r = resolver.insert(Uri.parse("content://com.android.contacts/data"), value);
						Log.i("qiaohu", "insert");
						if (ContentUris.parseId(r) < 1) {
							return false;
						}
					}
				}
				for(int i=0;i<clientDataIds.size();i++){
					str=clientDataIds.get(i);
					if(str==null||"".equals(str)){
						return false;
					}
					if(!serverDataIds.contains(str)){
						Log.i("qiaohu", "delete");
						if(resolver.delete(Uri.parse("content://com.android.contacts/data"), "data_sync4=?", new String[]{str})<1){
							return false;
						}
					}
				}
			}else{
				
				int delcount=getContext().getContentResolver().delete(Uri.parse("content://com.android.contacts/raw_contacts").buildUpon().appendQueryParameter("batch", "true")
						.appendQueryParameter(GnContactsContract.CALLER_IS_SYNCADAPTER,String.valueOf(true)).build(), "_id=?",new String[]{contact_id+""});
						if(delcount>0){
							return true;
						}
			}
		}else{
			if(deleted==1){
				return true;
			}
			value.put("sync4", sync4);
			contact_id = ContentUris.parseId(resolver.insert(uris.buildUpon().appendQueryParameter("sync", "true").build(), value));
			if (contact_id < 1) {
				return false;
			}
			
			for (int i = 0; i < jsonArray.length(); i++) {
				try {
					item = jsonArray.getJSONObject(i);
				} catch (Exception e) {
					e.printStackTrace();
				}
				value.clear();
				mimetype = getValue(item, "mimetype");
				data_sync4 = getValue(item, "syncid");
				if (mimetype.equals("vnd.android.cursor.item/photo")) {
					updatePhoto = data_sync4;
				}
				data1 = getValue(item, "data1");
				if (mimetype.equals("vnd.android.cursor.item/group_membership")) {
					Cursor cursors = getContext().getContentResolver().query(
							Groups.CONTENT_URI, new String[] { "_id" },
							"title=? and deleted=?", new String[] { data1, "0" },
							null);
					if (cursors.moveToNext()) {
						data1 = cursors.getString(0);
					} else {
						ContentValues val = new ContentValues();
						val.put("title", data1);
						Uri ur = getContext().getContentResolver().insert(
								Groups.CONTENT_URI, val);
						if (ContentUris.parseId(ur) < 1) {
							return false;
						}
						data1 = ur.getPathSegments().get(1);
					}
				}
				data2 = getValue(item, "data2");
				data3 = getValue(item, "data3");
				data4 = getValue(item, "data4");
				data5 = getValue(item, "data5");
				data6 = getValue(item, "data6");
				data7 = getValue(item, "data7");
				data8 = getValue(item, "data8");
				data9 = getValue(item, "data9");
				data10 = getValue(item, "data10");
				data11 = getValue(item, "data11");
				data12 = getValue(item, "data12");
				data13 = getValue(item, "data13");
				value.put("raw_contact_id", contact_id);
				value.put(Data.MIMETYPE, mimetype);
				value.put("data_sync4", data_sync4);
				value.put("data1", data1);
				value.put("data2", data2);
				value.put("data3", data3);
				value.put("data4", data4);
				value.put("data5", data5);
				value.put("data6", data6);
				value.put("data7", data7);
				value.put("data8", data8);
				value.put("data9", data9);
				value.put("data10", data10);
				value.put("data11", data11);
				value.put("data12", data12);
				value.put("data13", data13);
				Uri r = resolver.insert(Uri.parse("content://com.android.contacts/data"), value);
				if (ContentUris.parseId(r) < 1) {
					return false;
				}

			}
		}
		if (accessoryObjec != null) {
			if(!processAcc(accessoryObjec, updatePhoto)){
				return false;
			}
		}
		value.clear();
		value.put("dirty", "0");
		int y=resolver.update(Uri.parse("content://com.android.contacts/raw_contacts"), value, "_id=?", new String[]{contact_id+""});
		if(y<1){
			return false;
		}
		return true;
	}
	
	
	
	public boolean processAcc(JSONObject accessoryObjec,String updatePhoto){

		// 附件
		String newPath =getValue(accessoryObjec, "new_path");
		File file = new File(newPath);
		if (file.exists()) {
			System.out.println("file.exists()");
		} else {
			System.out.println("file.exists() is false");
			return false;
		}
		FileInputStream ffs = null;
		try {
			ffs = new FileInputStream(file);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		Bitmap bitmap = BitmapFactory.decodeStream(ffs);
		if (bitmap == null) {
			System.out.println("bitmap==null");
		}
		int size = bitmap.getWidth() * bitmap.getHeight() * 4;
		final ByteArrayOutputStream out = new ByteArrayOutputStream(size);
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
		if (out != null) {
			try {
				out.flush();
				// out.close();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

		}
		byte[] bs = out.toByteArray();
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		ContentValues cv = new ContentValues();
		cv.put(PhotoFiles.HEIGHT, height);
		cv.put(PhotoFiles.WIDTH, width);
		cv.put(PhotoFiles.FILESIZE, file.length());
		long photoFileId = mActiveDb.get().insert(Tables.PHOTO_FILES, null,
				cv);
		if (photoFileId < 1) {
			return false;
		}
		if (photoFileId != 0) {
			File target = new File(getContext().getFilesDir() + "/photos/",
					String.valueOf(photoFileId));
			if (file.renameTo(target)) {
				Uri uriData = Uri
						.parse("content://com.android.contacts/data");// 对data表的所有数据操作
				ContentResolver cr = getContext().getContentResolver();
				ContentValues valuesData = new ContentValues();
				valuesData.put("data14", photoFileId);
				valuesData.put("data15", bs);
				int numbers = cr.update(uriData, valuesData,
						"data_sync4=?", new String[] { updatePhoto });
				if (numbers < 1) {
					return false;
				}
				System.out.println("numbers=" + numbers);
			}
		}
	    return true;
	}
	
	
	public boolean getData14(String newPath,Object[] ob){
		File file = new File(newPath);
		if (file.exists()) {
			System.out.println("file.exists()");
		} else {
			System.out.println("file.exists() is false");
			return false;
		}
		FileInputStream ffs = null;
		try {
			ffs = new FileInputStream(file);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		Bitmap bitmap = BitmapFactory.decodeStream(ffs);
		if (bitmap == null) {
			System.out.println("bitmap==null");
		}
		int size = bitmap.getWidth() * bitmap.getHeight() * 4;
		final ByteArrayOutputStream out = new ByteArrayOutputStream(size);
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
		if (out != null) {
			try {
				out.flush();
				// out.close();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

		}
		byte[] bs = out.toByteArray();
		ob[0]=bs;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		ContentValues cv = new ContentValues();
		cv.put(PhotoFiles.HEIGHT, height);
		cv.put(PhotoFiles.WIDTH, width);
		cv.put(PhotoFiles.FILESIZE, file.length());
		long photoFileId = mActiveDb.get().insert(Tables.PHOTO_FILES, null,
				cv);
		if (photoFileId < 1) {
			return false;
		}
		if (photoFileId != 0) {
			File target = new File(getContext().getFilesDir() + "/photos/",
					String.valueOf(photoFileId));
			if (file.renameTo(target)) {
				ob[1]=photoFileId;
				return true;
			}
		}
		
		return false;
	}
	
	
	
	
	
	public boolean syncUpResult(String sync4, String raw_contact_id,
			SQLiteDatabase db, List<String> list) {

		ContentValues value_result = new ContentValues();
		Cursor cursor = db.query(Tables.DATA, new String[] { "_id" },
				"raw_contact_id=?", new String[] { raw_contact_id }, null,
				null, null);
		int i = 0;
		while (cursor.moveToNext()) {
			value_result.clear();
			value_result.put("data_sync4", list.get(i));
			i++;
			db.update(Tables.DATA, value_result, "_id=?",
					new String[] { cursor.getString(0) });
		}
		value_result.clear();
		value_result.put("sync4", sync4);
		value_result.put("dirty", "0");
		db.update(Tables.RAW_CONTACTS, value_result, "_id=?",
				new String[] { raw_contact_id });
		if (cursor != null) {
			cursor.close();
		}

		return true;
	}
	
	
	public List<String> getDataSyncIds(JSONObject object_result){
		JSONArray jsonArray_result=null;
		JSONObject item_result=null;
		try {
			jsonArray_result=(JSONArray)object_result.get("data");
		} catch (Exception e) {
			// TODO: handle exception
			return null;
			
		}
		List<String> list=new ArrayList<String>();
		for (int i = 0; i < jsonArray_result.length(); i++) {
			try {
				item_result=jsonArray_result.getJSONObject(i);
			} catch (Exception e) {
				return null;
			}
			list.add(getValue(item_result, "syncid"));
		}
		
		return list;
	}
	
	
	
	
	
	

	private boolean syncInitOne(SQLiteDatabase db, String syncid, String localFlag, long date){
		Cursor cursor = db.query(Tables.RAW_CONTACTS, null, "sync3='" + localFlag + "'", null, null, null, null);
		boolean isInit = false;
		if(cursor != null){
         // if(cursor.getCount()==0){
				//cursor.close();
				//return true;
			//}
			if(cursor.moveToFirst()){
				long localDate = cursor.getLong(cursor.getColumnIndex("gn_version"));
				String id=cursor.getString(cursor.getColumnIndex("_id"));
				ContentValues values = new ContentValues();
				
				if(localDate > date){
					Log.i("qiaohu", "localDate>date");
					isInit = true;
					values.put("sync4", syncid);
					values.put("dirty", 1);
					db.update(Tables.RAW_CONTACTS, values , "_id=" + id, null);
				} else if (localDate < date) {
					Log.i("qiaohu", "localDate < date");
					isInit = false;
					values.put("sync4", syncid);
					values.put("dirty", 0);
					db.update(Tables.RAW_CONTACTS, values , "_id=" + id, null);
				} else {
					Log.i("qiaohu", "localDate=date");
					isInit = true;
					values.put("sync4", syncid);
					values.put("dirty", 0);
					db.update(Tables.RAW_CONTACTS, values , "_id=" + id, null);
				}
				
				values.clear();
				values = null;
			}
			cursor.close();
		}
		return isInit;
	}
	
	public void sendSyncBroad(){
		Log.i("qiaohu", "sendSyncBroad");
		syncStart=System.currentTimeMillis();
		Log.i("qiaohu", "start="+syncStart);
		if(!syncFlag){
			return;
		}
 		new Thread(){
     		public void run() {
     			syncFlag=false;
     			do{
     				try {
						Thread.sleep(10 * 1000l);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
     				syncEnd=System.currentTimeMillis();
     				Log.i("qiaohu", "end="+syncEnd);
     				Log.i("qiaohu", (syncEnd-syncStart)+"");
     			}while(syncEnd-syncStart<10000);
     			Cursor cursor=getContext().getContentResolver().query(Uri.parse("content://com.android.contacts/sync/sync_up_size"), null, null, null, null);
     			Intent syncIntent = new Intent("com.aurora.account.START_SYNC");  
            	syncIntent.putExtra("packageName", "com.android.contacts");
            	if(cursor!=null){
            		if(cursor.moveToNext()){
            			syncIntent.putExtra("size", cursor.getInt(0));
                		Log.i("qiaohu", cursor.getInt(0)+"");
                		cursor.close();
            		}
            	}
            	getContext().sendBroadcast(syncIntent); 
            	syncFlag=true;
     		};
     	}.start();
	} 
}
