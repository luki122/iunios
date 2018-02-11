package com.android.mms.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import aurora.app.AuroraActivity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Sms;
import gionee.provider.GnTelephony.Threads;
import android.provider.Telephony.Sms.Conversations;
import android.provider.Telephony.ThreadsColumns;
// Aurora xuyong 2014-10-23 added for privacy feature start
import android.database.sqlite.SqliteWrapper;
// Aurora xuyong 2014-10-23 added for privacy feature end
import com.android.mms.util.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;

import com.android.mms.LogTag;
import com.android.mms.R;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.ui.MessageUtils;
import com.android.mms.util.DraftCache;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.util.PduCache;
//Aurora xuyong 2013-11-15 modified for google adapt end

//a0
import java.util.List;
import gionee.provider.GnTelephony.WapPush;
import gionee.provider.GnTelephony;
import android.widget.Toast;
import com.android.mms.MmsConfig;
import com.android.mms.transaction.WapPushMessagingNotification;
import com.aurora.featureoption.FeatureOption;
// Aurora xuyong 2014-10-23 added for privacy feature start
// Aurora xuyong 2015-12-04 added for aurora2.0 new feature start
import com.aurora.mms.ui.ConvFragment;
// Aurora xuyong 2015-12-04 added for aurora2.0 new feature end
import com.aurora.mms.util.Utils;
// Aurora xuyong 2014-10-23 added for privacy feature end
//a1

//gionee gaoj 2012-3-22 added for CR00555790 start
import android.os.Looper;
import android.os.SystemProperties;
import gionee.provider.GnTelephony.CbSms;
import com.android.mms.MmsApp;
import com.android.mms.transaction.SmsRejectedReceiver;
// Aurora xuyong 2014-10-23 added for privacy feature start
import com.privacymanage.service.AuroraPrivacyUtils;
// Aurora xuyong 2014-10-23 added for privacy feature end
import android.database.MatrixCursor;
//import android.drm.DrmHelper;
//gionee gaoj 2012-3-22 added for CR00555790 end

// gionee lwzh add for CR00774362 20130227 begin
import java.util.concurrent.ConcurrentHashMap;
// gionee lwzh add for CR00774362 20130227 end

/**
 * An interface for finding information about conversations and/or creating new ones.
 */
public class Conversation {
    private static final String TAG = "Mms/conv";
    private static final boolean DEBUG = false;
    private static boolean mIsInitialized = false;

    //gionee gaoj 2013-3-11 modified for CR00782858 start
    public static final Uri sAllThreadsUri =
        Threads.CONTENT_URI.buildUpon().appendQueryParameter("simple", "true").build();
    //gionee gaoj 2013-3-11 modified for CR00782858 end
    //m0
    /*
    private static final String[] ALL_THREADS_PROJECTION = {
        Threads._ID, Threads.DATE, Threads.MESSAGE_COUNT, Threads.RECIPIENT_IDS,
        Threads.SNIPPET, Threads.SNIPPET_CHARSET, Threads.READ, Threads.ERROR,
        Threads.HAS_ATTACHMENT
    };*/
    //gionee gaoj 2013-3-11 modified for CR00782858 start
    public static final String[] ALL_THREADS_PROJECTION = {
        Threads._ID, 
        Threads.DATE, 
        Threads.MESSAGE_COUNT, 
        Threads.RECIPIENT_IDS,
        Threads.SNIPPET, 
        Threads.SNIPPET_CHARSET, 
        Threads.READ, 
        Threads.ERROR,
        Threads.HAS_ATTACHMENT, 
        Threads.TYPE, 
        Threads.READCOUNT, 
        Threads.STATUS,
        Threads.SIM_ID,
      // Aurora xuyong 2014-10-23 modified for privacy feature start
        Threads.ENCRYPTION, 
        // Aurora xuyong 2015-12-04 modified for aurora2.0 new feature start
        "is_privacy",
        "notification_index"
        // Aurora xuyong 2015-12-04 modified for aurora2.0 new feature end
      // Aurora xuyong 2014-10-23 modified for privacy feature end
    };
    //gionee gaoj 2013-3-11 modified for CR00782858 end
    //m1

    private static final String[] UNREAD_PROJECTION = {
        Threads._ID,
        Threads.READ
    };

    private static final String UNREAD_SELECTION = "(read=0 OR seen=0)";
    // Aurora xuyong 2014-10-04 modified for privacy feature start
    private static final String UNREAD_SELECTION_PRIVACY = "(read=0 OR seen=0) AND is_privacy >= 0";
    // Aurora xuyong 2014-10-04 modified for privacy feature end
    private static final String[] SEEN_PROJECTION = new String[] {Sms.SEEN};

    private static final int ID             = 0;
    private static final int DATE           = 1;
    private static final int MESSAGE_COUNT  = 2;
    private static final int RECIPIENT_IDS  = 3;
    private static final int SNIPPET        = 4;
    private static final int SNIPPET_CS     = 5;
    private static final int READ           = 6;
    private static final int ERROR          = 7;
    private static final int HAS_ATTACHMENT = 8;

    //gionee gaoj 2012-3-22 added for CR00555790 start
    private static final int SIM_ID         = 12;
    private int mSimId;
    private static final int ENCRYPTION      = 13;
    // Aurora xuyong 2014-10-23 added for privacy feature start
    private static final int IS_PRIVACY     = 14;
    // Aurora xuyong 2014-10-23 added for privacy feature end
    // Aurora xuyong 2016-01-27 added for aurora 2.0 new feature start
    private static final int NOTIFICATION_INDEX = 15;
    // Aurora xuyong 2016-01-27 added for aurora 2.0 new feature end
    private boolean mHasEncryption;
    private static boolean mFirstEncryption = true;
    //gionee gaoj 2012-3-22 added for CR00555790 end
    private final Context mContext;

    // The thread ID of this conversation.  Can be zero in the case of a
    // new conversation where the recipient set is changing as the user
    // types and we have not hit the database yet to create a thread.
    private long mThreadId;

    private ContactList mRecipients;    // The current set of recipients.
    private long mDate;                 // The last update time.
    private int mMessageCount;          // Number of messages.
    private String mSnippet;            // Text of the most recent message.
    private boolean mHasUnreadMessages; // True if there are unread messages.
    private boolean mHasAttachment;     // True if any message has an attachment.
    private boolean mHasError;          // True if any message is in an error state.
    private boolean mIsChecked;         // True if user has selected the conversation for a
                                        // multi-operation such as delete.
    // Aurora xuyong 2014-10-23 added for privacy feature start
    private long mPrivacy;
    // Aurora xuyong 2014-10-23 added for privacy feature end
    // Aurora xuyong 2016-01-27 added for aurora 2.0 new feature start
    private int mNotificationIndex;
    // Aurora xuyong 2016-01-27 added for aurora 2.0 new feature end
    private static ContentValues mReadContentValues;
    private static boolean mLoadingThreads;
    private boolean mMarkAsReadBlocked;
    private Object mMarkAsBlockedSyncer = new Object();

    // gionee lwzh add for CR00774362 20130227 begin
    private static boolean sNeedCacheConv = true;
    // gionee lwzh add for CR00774362 20130227 end
    
    //gionee gaoj 2013-3-11 added for CR00782858 start
    public static final int TYPE_GROUP = 110;
    //gionee gaoj 2013-3-11 added for CR00782858 end
    
    private Conversation(Context context) {
        mContext = context;
        mRecipients = new ContactList();
        mThreadId = 0;
    }

    private Conversation(Context context, long threadId, boolean allowQuery) {
        if (DEBUG) {
            Log.v(TAG, "Conversation constructor threadId: " + threadId);
        }
        mContext = context;
        Log.d(TAG,"new Conversation.loadFromThreadId(threadId, allowQuery): threadId = " 
                + threadId + "allowQuery = " + allowQuery);
        if (!loadFromThreadId(threadId, allowQuery)) {
            mRecipients = new ContactList();
            mThreadId = 0;
        }
    }
    // Aurora xuyong 2014-10-23 added for privacy feature start
    private Conversation(Context context, long threadId, boolean allowQuery, long privacy) {
        if (DEBUG) {
            Log.v(TAG, "Conversation constructor threadId: " + threadId);
        }
        mContext = context;
        Log.d(TAG,"new Conversation.loadFromThreadId(threadId, allowQuery): threadId = " 
                + threadId + "allowQuery = " + allowQuery);
        mPrivacy = privacy;
        if (!loadFromThreadId(threadId, allowQuery, privacy)) {
            mRecipients = new ContactList();
            mThreadId = 0;
        }
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end
    private Conversation(Context context, Cursor cursor, boolean allowQuery) {
        if (DEBUG) {
            Log.v(TAG, "Conversation constructor cursor, allowQuery: " + allowQuery);
        }
        mContext = context;
        // Aurora xuyong 2014-10-23 modified for privacy feature start
        if (MmsApp.sHasPrivacyFeature) {
            fillFromCursor(context, this, cursor, allowQuery, cursor.getLong(IS_PRIVACY));
        } else {
            fillFromCursor(context, this, cursor, allowQuery);
        }
        // Aurora xuyong 2014-10-23 modified for privacy feature end
    }

    /**
     * Create a new conversation with no recipients.  {@link #setRecipients} can
     * be called as many times as you like; the conversation will not be
     * created in the database until {@link #ensureThreadId} is called.
     */
    public static Conversation createNew(Context context) {
        return new Conversation(context);
    }

    /**
     * Find the conversation matching the provided thread ID.
     */
    public static Conversation get(Context context, long threadId, boolean allowQuery) {
        if (DEBUG) {
            Log.v(TAG, "Conversation get by threadId: " + threadId);
        }
        Conversation conv = Cache.get(threadId);
        if (conv != null)
            return conv;

        conv = new Conversation(context, threadId, allowQuery);
        try {
            Cache.put(conv);
        } catch (IllegalStateException e) {
            LogTag.error("Tried to add duplicate Conversation to Cache (from threadId): " + conv);
            if (!Cache.replace(conv)) {
                LogTag.error("get by threadId cache.replace failed on " + conv);
            }
        }
        return conv;
    }
    // Aurora xuyong 2014-10-25 added for privacy feature start
    public static Conversation get(Context context, long threadId, boolean allowQuery, long privacy) {
        if (DEBUG) {
            Log.v(TAG, "Conversation get by threadId: " + threadId);
        }
        Conversation conv = Cache.get(threadId);
        if (conv != null)
            return conv;

        conv = new Conversation(context, threadId, allowQuery, privacy);
        try {
            Cache.put(conv);
        } catch (IllegalStateException e) {
            LogTag.error("Tried to add duplicate Conversation to Cache (from threadId): " + conv);
            if (!Cache.replace(conv)) {
                LogTag.error("get by threadId cache.replace failed on " + conv);
            }
        }
        return conv;
    }
    // Aurora xuyong 2014-10-25 added for privacy feature end
    /**
     * Find the conversation matching the provided recipient set.
     * When called with an empty recipient list, equivalent to {@link #createNew}.
     */
    public static Conversation get(Context context, ContactList recipients, boolean allowQuery) {
        if (DEBUG) {
            Log.v(TAG, "Conversation get by recipients: " + recipients.serialize());
        }
        // If there are no recipients in the list, make a new conversation.
        if (recipients.size() < 1) {
            return createNew(context);
        }

        Conversation conv = Cache.get(recipients);
        //Gionee <zhouyj> <2013-05-08> modify for CR00797122 begin
        if (conv != null) {
            return conv;
        }
        //Gionee <zhouyj> <2013-05-08> modify for CR00797122 end

        long threadId = getOrCreateThreadId(context, recipients);
        conv = new Conversation(context, threadId, allowQuery);
        Log.d(TAG, "Conversation.get: created new conversation " + /*conv.toString()*/ "xxxxxxx");

        if (!conv.getRecipients().equals(recipients)) {
            LogTag.error(TAG, "Conversation.get: new conv's recipients don't match input recpients "
                    + /*recipients*/ "xxxxxxx");
        }

        try {
            Cache.put(conv);
        } catch (IllegalStateException e) {
            LogTag.error("Tried to add duplicate Conversation to Cache (from recipients): " + conv);
            if (!Cache.replace(conv)) {
                LogTag.error("get by recipients cache.replace failed on " + conv);
            }
        }

        return conv;
    }
    // Aurora xuyong 2014-10-23 added for privacy feature start
    public static Conversation get(Context context, ContactList recipients, boolean allowQuery, long privacy) {
        if (DEBUG) {
            Log.v(TAG, "Conversation get by recipients: " + recipients.serialize());
        }
        // If there are no recipients in the list, make a new conversation.
        if (recipients.size() < 1) {
            return createNew(context);
        }

        Conversation conv = Cache.get(recipients);
        //Gionee <zhouyj> <2013-05-08> modify for CR00797122 begin
        if (conv != null) {
            return conv;
        }
        //Gionee <zhouyj> <2013-05-08> modify for CR00797122 end
        long threadId = getOrCreateThreadId(context, recipients, false, privacy);
        conv = new Conversation(context, threadId, allowQuery, privacy);
        Log.d(TAG, "Conversation.get: created new conversation " + /*conv.toString()*/ "xxxxxxx");

        if (!conv.getRecipients().equals(recipients)) {
            LogTag.error(TAG, "Conversation.get: new conv's recipients don't match input recpients "
                    + /*recipients*/ "xxxxxxx");
        }

        try {
            Cache.put(conv);
        } catch (IllegalStateException e) {
            LogTag.error("Tried to add duplicate Conversation to Cache (from recipients): " + conv);
            if (!Cache.replace(conv)) {
                LogTag.error("get by recipients cache.replace failed on " + conv);
            }
        }

        return conv;
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end
    /**
     * Find the conversation matching in the specified Uri.  Example
     * forms: {@value content://mms-sms/conversations/3} or
     * {@value sms:+12124797990}.
     * When called with a null Uri, equivalent to {@link #createNew}.
     */
    public static Conversation get(Context context, Uri uri, boolean allowQuery) {
        if (DEBUG) {
            Log.v(TAG, "Conversation get by uri: " + uri);
        }
        if (uri == null) {
            return createNew(context);
        }

        if (DEBUG) Log.v(TAG, "Conversation get URI: " + uri);

        // Handle a conversation URI
        if (uri.getPathSegments().size() >= 2) {
            try {
                //m0
                /*
                long threadId = Long.parseLong(uri.getPathSegments().get(1));
                */
                
                String threadIdStr = uri.getPathSegments().get(1);
                threadIdStr = threadIdStr.replaceAll("-","");
                //long threadId = Long.parseLong(uri.getPathSegments().get(1));
                long threadId = Long.parseLong(threadIdStr);
                //m1
                
                if (DEBUG) {
                    Log.v(TAG, "Conversation get threadId: " + threadId);
                }
                return get(context, threadId, allowQuery);
            } catch (NumberFormatException exception) {
                LogTag.error("Invalid URI: " + uri);
            }
        }

        String recipient = getRecipients(uri);
        
//m0
//        returen get(context, ContactList.getByNumbers(recipient,
//                allowQuery /* don't block */, true /* replace number */), allowQuery);
        return get(context, ContactList.getByNumbers(context, recipient, false /* don't block */, true /* replace number */), allowQuery);
//m1
    }
    // Aurora xuyong 2014-10-30 added for privacy feature start
    public static Conversation get(Context context, Uri uri, boolean allowQuery, long privacy) {
        if (DEBUG) {
            Log.v(TAG, "Conversation get by uri: " + uri);
        }
        if (uri == null) {
            return createNew(context);
        }

        if (DEBUG) Log.v(TAG, "Conversation get URI: " + uri);

        // Handle a conversation URI
        if (uri.getPathSegments().size() >= 2) {
            try {
                //m0
                /*
                long threadId = Long.parseLong(uri.getPathSegments().get(1));
                */
                
                String threadIdStr = uri.getPathSegments().get(1);
                threadIdStr = threadIdStr.replaceAll("-","");
                //long threadId = Long.parseLong(uri.getPathSegments().get(1));
                long threadId = Long.parseLong(threadIdStr);
                //m1
                
                if (DEBUG) {
                    Log.v(TAG, "Conversation get threadId: " + threadId);
                }
                return get(context, threadId, allowQuery, privacy);
            } catch (NumberFormatException exception) {
                LogTag.error("Invalid URI: " + uri);
            }
        }

        String recipient = getRecipients(uri);
        
//m0
//        returen get(context, ContactList.getByNumbers(recipient,
//                allowQuery /* don't block */, true /* replace number */), allowQuery);
        return get(context, ContactList.getByNumbers(context, recipient, false /* don't block */, true /* replace number */), allowQuery, privacy);
//m1
    }
    // Aurora xuyong 2014-10-30 added for privacy feature end
    /**
     * Returns true if the recipient in the uri matches the recipient list in this
     * conversation.
     */
    public boolean sameRecipient(Uri uri, Context context) {
        int size = mRecipients.size();
        if (size > 1) {
            return false;
        }
        if (uri == null) {
            return size == 0;
        }
        ContactList incomingRecipient = null;
        if (uri.getPathSegments().size() >= 2) {
            // it's a thread id for a conversation
            Conversation otherConv = get(context, uri, false);
            if (otherConv == null) {
                return false;
            }
            incomingRecipient = otherConv.mRecipients;
        } else {
            String recipient = getRecipients(uri);
            incomingRecipient = ContactList.getByNumbers(recipient,
                    false /* don't block */, false /* don't replace number */);
        }
        if (DEBUG) Log.v(TAG, "sameRecipient incomingRecipient: " + incomingRecipient +
                " mRecipients: " + mRecipients);
        return mRecipients.equals(incomingRecipient);
    }

    /**
     * Returns a temporary Conversation (not representing one on disk) wrapping
     * the contents of the provided cursor.  The cursor should be the one
     * returned to your AsyncQueryHandler passed in to {@link #startQueryForAll}.
     * The recipient list of this conversation can be empty if the results
     * were not in cache.
     */
    // Aurora yudingmin 2014-08-30 added for optimize start
    public static Conversation from(Context context, Cursor cursor) {
        return from(context, cursor, false);
    }
    // Aurora yudingmin 2014-08-30 added for optimize end
    // Aurora xuyong 2016-03-08 added for bug #18675 start
    public static Conversation from(Context context, Cursor cursor, boolean allowQuery, boolean needRebuildConv) {
        long threadId = cursor.getLong(ID);
        if (threadId > 0) {
            Conversation conv = Cache.get(threadId);
            if (conv != null && needRebuildConv) {
                fillFromCursor(context, conv, cursor, allowQuery);
                return conv;
            }
        }
        Conversation conv = new Conversation(context, cursor, allowQuery);
        try {
            if (sNeedCacheConv) {
                Cache.put(conv);
            } else if (conv.hasDraft() || conv.getMessageCount() > 0 || conv.hasAttachment()
                    || conv.hasUnreadMessages()) {
                Cache.put(conv);
            }
        } catch (IllegalStateException e) {
            LogTag.error(TAG, "Tried to add duplicate Conversation to Cache (from cursor): " +
                    conv);
            if (!Cache.replace(conv)) {
                LogTag.error("Converations.from cache.replace failed on " + conv);
            }
        }
        return conv;
    }
    // Aurora xuyong 2016-03-08 added for bug #18675 end
    public static Conversation from(Context context, Cursor cursor, boolean allowQuery) {
        // First look in the cache for the Conversation and return that one. That way, all the
        // people that are looking at the cached copy will get updated when fillFromCursor() is
        // called with this cursor.
        long threadId = cursor.getLong(ID);
        if (threadId > 0) {
            Conversation conv = Cache.get(threadId);
            if (conv != null) {
                // Aurora yudingmin 2014-08-30 modified for optimize start
//                fillFromCursor(context, conv, cursor, allowQuery);   // update the existing conv in-place
                // Aurora yudingmin 2014-08-30 modified for optimize end
                return conv;
            }
        }
        // Aurora yudingmin 2014-08-30 modified for optimize start
        Conversation conv = new Conversation(context, cursor, allowQuery);
        // Aurora yudingmin 2014-08-30 modified for optimize end
        try {
            // gionee lwzh add for CR00774362 20130227 begin
            if (sNeedCacheConv) {
                Cache.put(conv);
            } else if (conv.hasDraft() || conv.getMessageCount() > 0 || conv.hasAttachment()
                    || conv.hasUnreadMessages()) {
                Cache.put(conv);
            }
            // gionee lwzh add for CR00774362 20130227 end
        } catch (IllegalStateException e) {
            LogTag.error(TAG, "Tried to add duplicate Conversation to Cache (from cursor): " +
                    conv);
            if (!Cache.replace(conv)) {
                LogTag.error("Converations.from cache.replace failed on " + conv);
            }
        }
        return conv;
    }

    private void buildReadContentValues() {
        if (mReadContentValues == null) {
            mReadContentValues = new ContentValues(2);
            mReadContentValues.put(Sms.READ, 1);
            mReadContentValues.put(Sms.SEEN, 1);
        }
    }

    /**
     * Marks all messages in this conversation as read and updates
     * relevant notifications.  This method returns immediately;
     * work is dispatched to a background thread.
     */
    public void markAsRead() {
        // If we have no Uri to mark (as in the case of a conversation that
        // has not yet made its way to disk), there's nothing to do.
        final Uri threadUri = getUri();

        new Thread(new Runnable() {
            public void run() {
                synchronized(mMarkAsBlockedSyncer) {
                    if (mMarkAsReadBlocked) {
                        try {
                            mMarkAsBlockedSyncer.wait();
                        } catch (InterruptedException e) {
                        }
                    }

                    if (threadUri != null) {
                        buildReadContentValues();

                        // Check the read flag first. It's much faster to do a query than
                        // to do an update. Timing this function show it's about 10x faster to
                        // do the query compared to the update, even when there's nothing to
                        // update.
                        boolean needUpdate = true;

                        //gionee gaoj 2012-8-6 modified for CR00663678 end
                        Cursor c = null;
                        try {
                            // Aurora xuyong 2014-10-04 modified for privacy feature start
                            if (MmsApp.sHasPrivacyFeature) {
                                c = mContext.getContentResolver().query(threadUri,
                                    UNREAD_PROJECTION, UNREAD_SELECTION_PRIVACY, null, null);
                            } else {
                                c = mContext.getContentResolver().query(threadUri,
                                        UNREAD_PROJECTION, UNREAD_SELECTION, null, null);
                            }
                            // Aurora xuyong 2014-10-04 modified for privacy feature end
                            if (c != null) {
                                needUpdate = c.getCount() > 0;
                            }
                        } finally {
                          // Aurora xuyong 2014-07-02 added for NE start
                            if (c != null && !c.isClosed()) {
                                c.close();
                            }
                          // Aurora xuyong 2014-07-02 added for NE feature end
                        }
                        //gionee gaoj 2012-8-6 modified for CR00663678 end

                        if (needUpdate) {
                            LogTag.debug("markAsRead: update read/seen for thread uri: " +
                                    threadUri);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    // Aurora xuyong 2014-10-04 modified for privacy feature start
                                    if (MmsApp.sHasPrivacyFeature) {
                                        mContext.getContentResolver().update(threadUri, mReadContentValues,
                                                UNREAD_SELECTION_PRIVACY, null);
                                    } else {
                                        mContext.getContentResolver().update(threadUri, mReadContentValues,
                                                UNREAD_SELECTION, null);
                                    }
                                    // Aurora xuyong 2014-10-04 modified for privacy feature end
                                }
                            }, "markAsRead").start();
                        }

                        setHasUnreadMessages(false);
                    }
                }
                // Aurora xuyong 2014-12-03 added for debug start
                if (threadUri != null) {
                    final Uri updateUri = threadUri.buildUpon().appendPath("updatethreads").build();
                    mContext.getContentResolver().query(updateUri, null, null, null, null);
                }
                // Aurora xuyong 2014-12-03 added for debug end
                // Always update notifications regardless of the read state.
                MessagingNotification.blockingUpdateAllNotifications(mContext);
            }
        }).start();
    }

    public void blockMarkAsRead(boolean block) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("blockMarkAsRead: " + block);
        }

        synchronized(mMarkAsBlockedSyncer) {
            if (block != mMarkAsReadBlocked) {
                mMarkAsReadBlocked = block;
                if (!mMarkAsReadBlocked) {
                    mMarkAsBlockedSyncer.notifyAll();
                }
            }

        }
    }

    /**
     * Returns a content:// URI referring to this conversation,
     * or null if it does not exist on disk yet.
     */
    public synchronized Uri getUri() {
        if (mThreadId <= 0)
            return null;

        return ContentUris.withAppendedId(Threads.CONTENT_URI, mThreadId);
    }

    /**
     * Return the Uri for all messages in the given thread ID.
     * @deprecated
     */
    public static Uri getUri(long threadId) {
        // TODO: Callers using this should really just have a Conversation
        // and call getUri() on it, but this guarantees no blocking.
        return ContentUris.withAppendedId(Threads.CONTENT_URI, threadId);
    }

    /**
     * Returns the thread ID of this conversation.  Can be zero if
     * {@link #ensureThreadId} has not been called yet.
     */
    public synchronized long getThreadId() {
        return mThreadId;
    }
    // Aurora xuyong 2014-10-23 added for privacy feature start
    public synchronized long getPrivacy() {
        return mPrivacy;
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end
    // Aurora xuyong 2016-01-27 added for aurora 2.0 new feature start
    public synchronized int getNotificationIndex() {
        return mNotificationIndex;
    }
    public synchronized void setNotificationIndex(int notificationIndex) {
        mNotificationIndex = notificationIndex;
    }
    // Aurora xuyong 2016-01-27 added for aurora 2.0 new feature end
    /**
     * Guarantees that the conversation has been created in the database.
     * This will make a blocking database call if it hasn't.
     *
     * @return The thread ID of this conversation in the database
     */
    public synchronized long ensureThreadId() {
        //m0
        /*
        if (DEBUG) {
            LogTag.debug("ensureThreadId before: " + mThreadId);
        }
        if (mThreadId <= 0) {
            mThreadId = getOrCreateThreadId(mContext, mRecipients);
        }
        if (DEBUG) {
            LogTag.debug("ensureThreadId after: " + mThreadId);
        }

        return mThreadId;
        */
        return ensureThreadId(false);
        //m1
    }

    public synchronized void clearThreadId() {
        // remove ourself from the cache
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("clearThreadId old threadId was: " + mThreadId + " now zero");
        }
        Cache.remove(mThreadId);
        //a0
        DraftCache.getInstance().updateDraftStateInCache(mThreadId);
        //a1
        mThreadId = 0;
    }

    /**
     * Sets the list of recipients associated with this conversation.
     * If called, {@link #ensureThreadId} must be called before the next
     * operation that depends on this conversation existing in the
     * database (e.g. storing a draft message to it).
     */
    public synchronized void setRecipients(ContactList list) {
        //gionee gaoj 2012-5-7 added for CR00582506 start
        if (!MmsApp.mGnMessageSupport) {
            //gionee gaoj 2012-5-7 added for CR00582506 end
        //a0
        // remove the same contacts
        Set<Contact> contactSet = new HashSet<Contact>();
        contactSet.addAll(list);
        list.clear();
        list.addAll(contactSet);
        //a1
        //gionee gaoj 2012-5-7 added for CR00582506 start
        }
        //gionee gaoj 2012-5-7 added for CR00582506 end

        mRecipients = list;

        // Invalidate thread ID because the recipient set has changed.
        mThreadId = 0;

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.d(TAG, "setRecipients after: " + this.toString());
        }
}

    /**
     * Returns the recipient set of this conversation.
     */
    public synchronized ContactList getRecipients() {
        // Aurora xuyong 2014-06-03 added for bug #5071 start
        // Aurora xuyong 2014-06-07 deleted for bug #5377 start
          //mRecipients = updateReceipients();
        // Aurora xuyong 2014-06-07 deleted for bug #5377 end
        // Aurora xuyong 2014-06-03 added for bug #5071 end
        return mRecipients;
    }
    // Aurora xuyong 2014-06-03 added for bug #5071 start
    /**
     * Sometimes we need update the recipients
     */
    // Aurora xuyong 2014-06-07 modified for bug #5377 start
    public synchronized void updateReceipients() {
    // Aurora xuyong 2014-06-07 modified for bug #5377 end
        ContactList newRecipients = new ContactList();
        if (mRecipients != null) {
            for (Contact contact : mRecipients) {
             // Aurora xuyong 2014-10-23 modified for privacy feature start
                if (MmsApp.sHasPrivacyFeature) {
                    newRecipients.add(Contact.get(contact.getNumber(), true, contact.getPrivacy()));
                } else {
                    newRecipients.add(Contact.get(contact.getNumber(), true));
                }
             // Aurora xuyong 2014-10-23 modified for privacy feature end
            }
          // Aurora xuyong 2014-06-07 deleted for bug #5377 start
            //return newRecipients;
          // Aurora xuyong 2014-06-07 deleted for bug #5377 end
        }
       // Aurora xuyong 2014-06-07 added for bug #5377 start
        mRecipients = newRecipients;
       // Aurora xuyong 2014-06-07 added for bug #5377 start
    }
    // Aurora xuyong 2014-06-03 added for bug #5071 end
    /**
     * Returns true if a draft message exists in this conversation.
     */
    public synchronized boolean hasDraft() {
        if (mThreadId <= 0)
            return false;

        //gionee gaoj 2012-5-2 added for CR00585951 start
        if (MmsApp.mGnMessageSupport && mType == Threads.WAPPUSH_THREAD) {
            DraftCache.getInstance().setDraftState(mThreadId, false);
            return false;
        }
        //gionee gaoj 2012-5-2 added for CR00585951 end
        return DraftCache.getInstance().hasDraft(mThreadId);
    }

    /**
     * Sets whether or not this conversation has a draft message.
     */
    public synchronized void setDraftState(boolean hasDraft) {
        if (mThreadId <= 0)
            return;

        DraftCache.getInstance().setDraftState(mThreadId, hasDraft);
    }

    /**
     * Returns the time of the last update to this conversation in milliseconds,
     * on the {@link System#currentTimeMillis} timebase.
     */
    public synchronized long getDate() {
        return mDate;
    }

    /**
     * Returns the number of messages in this conversation, excluding the draft
     * (if it exists).
     */
    public synchronized int getMessageCount() {
        return mMessageCount;
    }
    /**
     * Set the number of messages in this conversation, excluding the draft
     * (if it exists).
     */
    public synchronized void setMessageCount(int cnt) {
        mMessageCount = cnt;
    }

    /**
     * Returns a snippet of text from the most recent message in the conversation.
     */
    public synchronized String getSnippet() {
        return mSnippet;
    }

    /**
     * Returns true if there are any unread messages in the conversation.
     */
    public boolean hasUnreadMessages() {
        synchronized (this) {
            return mHasUnreadMessages;
        }
    }

    private void setHasUnreadMessages(boolean flag) {
        synchronized (this) {
            mHasUnreadMessages = flag;
        }
    }

    /**
     * Returns true if any messages in the conversation have attachments.
     */
    public synchronized boolean hasAttachment() {
        return mHasAttachment;
    }

    /**
     * Returns true if any messages in the conversation are in an error state.
     */
    public synchronized boolean hasError() {
        return mHasError;
    }

    /**
     * Returns true if this conversation is selected for a multi-operation.
     */
    public synchronized boolean isChecked() {
        return mIsChecked;
    }

    public synchronized void setIsChecked(boolean isChecked) {
        mIsChecked = isChecked;
    }

    private static long getOrCreateThreadId(Context context, ContactList list) {
        //m0
        /*
        HashSet<String> recipients = new HashSet<String>();
        Contact cacheContact = null;
        for (Contact c : list) {
            cacheContact = Contact.get(c.getNumber(), false);
            if (cacheContact != null) {
                recipients.add(cacheContact.getNumber());
            } else {
                recipients.add(c.getNumber());
            }
        }
        long retVal = Threads.getOrCreateThreadId(context, recipients);
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("[Conversation] getOrCreateThreadId for (%s) returned %d",
                    recipients, retVal);
        }

        return retVal;
        */
        
        return getOrCreateThreadId(context, list, false);
        //m1
    }

    /*
     * The primary key of a conversation is its recipient set; override
     * equals() and hashCode() to just pass through to the internal
     * recipient sets.
     */
    @Override
    public synchronized boolean equals(Object obj) {
        try {
            Conversation other = (Conversation)obj;
            return (mRecipients.equals(other.mRecipients));
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public synchronized int hashCode() {
        return mRecipients.hashCode();
    }

    @Override
    public synchronized String toString() {
        return String.format("[%s] (tid %d)", mRecipients.serialize(), mThreadId);
    }

    /**
     * Remove any obsolete conversations sitting around on disk. Obsolete threads are threads
     * that aren't referenced by any message in the pdu or sms tables.
     */
    public static void asyncDeleteObsoleteThreads(AsyncQueryHandler handler, int token) {
        handler.startDelete(token, null, Threads.OBSOLETE_THREADS_URI, null, null);
    }

    /**
     * Start a query for all conversations in the database on the specified
     * AsyncQueryHandler.
     *
     * @param handler An AsyncQueryHandler that will receive onQueryComplete
     *                upon completion of the query
     * @param token   The token that will be passed to onQueryComplete
     */
    public static void startQueryForAll(AsyncQueryHandler handler, int token) {
        handler.cancelOperation(token);

        // This query looks like this in the log:
        // I/Database(  147): elapsedTime4Sql|/data/data/com.android.providers.telephony/databases/
        // mmssms.db|2.253 ms|SELECT _id, date, message_count, recipient_ids, snippet, snippet_cs,
        // read, error, has_attachment FROM threads ORDER BY  date DESC
        // Aurora xuyong 2014-10-23 modified for privacy feature start
        startQuery(handler, token, null, false);
        // Aurora xuyong 2014-10-23 modified for privacy feature end
    }

    // Aurora xuyong 2014-07-02 added for reject feature start
    public static void startQueryForAll(AsyncQueryHandler handler, int token, String selection) {
        handler.cancelOperation(token);

        // This query looks like this in the log:
        // I/Database(  147): elapsedTime4Sql|/data/data/com.android.providers.telephony/databases/
        // mmssms.db|2.253 ms|SELECT _id, date, message_count, recipient_ids, snippet, snippet_cs,
        // read, error, has_attachment FROM threads ORDER BY  date DESC
        // Aurora xuyong 2014-10-23 modified for privacy feature start
        startQuery(handler, token, selection, false);
        // Aurora xuyong 2014-10-23 modified for privacy feature end
    }
    // Aurora xuyong 2014-07-02 added for reject feature end
    // Aurora xuyong 2014-10-23 added for privacy feature start
    public static void startQueryForPrivacyAll(AsyncQueryHandler handler, int token, String selection) {
        handler.cancelOperation(token);

        // This query looks like this in the log:
        // I/Database(  147): elapsedTime4Sql|/data/data/com.android.providers.telephony/databases/
        // mmssms.db|2.253 ms|SELECT _id, date, message_count, recipient_ids, snippet, snippet_cs,
        // read, error, has_attachment FROM threads ORDER BY  date DESC

        startQuery(handler, token, selection, true);
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end

    /**
     * Start a query for in the database on the specified AsyncQueryHandler with the specified
     * "where" clause.
     *
     * @param handler An AsyncQueryHandler that will receive onQueryComplete
     *                upon completion of the query
     * @param token   The token that will be passed to onQueryComplete
     * @param selection   A where clause (can be null) to select particular conv items.
     */
    // Aurora xuyong 2014-10-23 modified for privacy feature start
    public static void startQuery(AsyncQueryHandler handler, int token, String selection, boolean privacyOnly) {
    // Aurora xuyong 2014-10-23 modified for privacy feature end
        handler.cancelOperation(token);
        // Aurora xuyong 2014-10-23 added for privacy feature start
        boolean fromReject = false;
        if (selection != null && selection.equals("fromReject")) {
            fromReject = true;
            // reject feature
            selection = "reject = 0";
        }
        // Aurora xuyong 2014-10-23 added for privacy feature end
        //a0
        final int queryToken = token;
        final AsyncQueryHandler queryHandler = handler;
        //gionee gaoj 2013-4-1 added for CR00788343 start
        if (MmsApp.mIsDraftOpen) {
            selection = "(message_count != 0)";
        }
        //gionee gaoj 2013-4-1 added for CR00788343 end
        // Aurora xuyong 2014-02-19 modified for bug #941 start
        // Aurora xuyong 2014-03-03 modified for bug #2719 start
        // Aurora xuyong 2014-03-13 modified for bug @2719 start
        // Aurora xuyong 2014-09-21 modified for bug #3935 start
       // Aurora xuyong 2014-04-22 modified for bug #4489 start
        String snippetSelection = "NOT ((snippet is null) AND (snippet_cs not null AND snippet_cs = 0) AND (has_attachment = 0) AND (message_count = 0))";
       // Aurora xuyong 2014-04-22 modified for bug #4489 end
        // Aurora xuyong 2014-09-21 modified for bug #3935 end
        // Aurora xuyong 2014-03-13 modified for bug @2719 end
        // Aurora xuyong 2014-03-03 modified for bug #2719 end
        // Aurora xuyong 2014-07-04 added for reject featrue start
        if (MmsApp.sHasRejectFeature) {
          // Aurora xuyong 2014-07-07 modified for reject feature start
          // Aurora xuyong 2014-07-19 modified for bug #6656 start
            // Aurora xuyong 2014-07-25 modified for bug #6853 start
          // Aurora xuyong 2014-07-26 modified for aurora's new feature start
            snippetSelection = "(" +snippetSelection + ") AND (NOT date IS NULL)";
          // Aurora xuyong 2014-07-26 modified for aurora's new feature end
            // Aurora xuyong 2014-07-25 modified for bug #6853 end
          // Aurora xuyong 2014-07-19 modified for bug #6656 end
          // Aurora xuyong 2014-07-07 modified for reject feature end
        }
        // Aurora xuyong 2014-10-23 added for privacy feature start
        if (MmsApp.sHasPrivacyFeature) {
            long curAccountId = AuroraPrivacyUtils.getCurrentAccountId();
            if (!privacyOnly) {
                if (curAccountId > 0 && !fromReject) {
                    snippetSelection = "(" +snippetSelection + ") AND (is_privacy IN (0, " + curAccountId +"))";
                } else {
                    snippetSelection = "(" +snippetSelection + ") AND is_privacy = 0";
                }
            } else {
                snippetSelection = "(" +snippetSelection + ") AND is_privacy = " + curAccountId;
            }
        } else {
            if (privacyOnly) {
                snippetSelection = "(" +snippetSelection + ") AND is_privacy > 0";
            }
        }
        // Aurora xuyong 2014-10-23 added for privacy feature end
        // Aurora xuyong 2014-07-04 added for reject featrue end
        String newSelection;
        if (selection == null) {
            newSelection = snippetSelection;
        } else {
            newSelection = selection + " AND (" + snippetSelection + ")";
        }
        final String Selection = newSelection;
        // Aurora xuyong 2014-02-19 modified for bug #941 end
        //a1

        // This query looks like this in the log:
        // I/Database(  147): elapsedTime4Sql|/data/data/com.android.providers.telephony/databases/
        // mmssms.db|2.253 ms|SELECT _id, date, message_count, recipient_ids, snippet, snippet_cs,
        // read, error, has_attachment FROM threads ORDER BY  date DESC

        //m0
        //handler.startQuery(token, null, sAllThreadsUri,
        //        ALL_THREADS_PROJECTION, selection, null, Conversations.DEFAULT_SORT_ORDER);
        
        //gionee gaoj 2013-4-1 modified for CR00788343 start
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {
            queryHandler.postDelayed(new Runnable() {
                public void run() {
                    queryHandler.startQuery(
                            queryToken, null, sAllThreadsUri,
                            ALL_THREADS_PROJECTION, Selection, null, Conversations.DEFAULT_SORT_ORDER);
                }
              // Aurora liugj 2014-01-06 modified for bath-delete optimize start
            }, 0);
              // Aurora liugj 2014-01-06 modified for bath-delete optimize end
            return;
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
        //gionee gaoj 2013-4-1 modified for CR00788343 end
        queryHandler.postDelayed(new Runnable() {
            public void run() {
                queryHandler.startQuery(queryToken, null, sAllThreadsUri,
                    ALL_THREADS_PROJECTION, Selection, null, Conversations.DEFAULT_SORT_ORDER);
            }
        }, 10);
        //m1
    }

    /**
     * Start a delete of the conversation with the specified thread ID.
     *
     * @param handler An AsyncQueryHandler that will receive onDeleteComplete
     *                upon completion of the conversation being deleted
     * @param token   The token that will be passed to onDeleteComplete
     * @param deleteAll Delete the whole thread including locked messages
     * @param threadId Thread ID of the conversation to be deleted
     */
    public static void startDelete(AsyncQueryHandler handler, int token, boolean deleteAll,
            long threadId) {
        //wappush: do not need modify the code here, but delete function in provider has been modified.
        Uri uri = ContentUris.withAppendedId(Threads.CONTENT_URI, threadId);
        String selection = deleteAll ? null : "locked=0";
        PduCache.getInstance().purge(uri);
        handler.startDelete(token, null, uri, selection, null);
    }
    // Aurora xuyong 2014-07-08 added for bug #6430 start
    private static boolean sIsFromReject = false;
    public static void setIsFromReject(boolean isFromReject) {
        sIsFromReject = isFromReject;
    }
    // Aurora xuyong 2014-07-08 added for bug #6430 end
    //gionee gaoj 2012-9-20 added for CR00699291 start
    public static void startDeletestar(AsyncQueryHandler handler, int token,
            boolean deleteStared, long threadId) {
        // wappush: do not need modify the code here, but delete function in
        // provider has been modified.
        Uri uri = ContentUris.withAppendedId(Threads.CONTENT_URI, threadId);
        String selection = deleteStared ? null : "star=0";
        // Aurora xuyong 2014-07-08 added for bug #6430 start
        if (MmsApp.sHasRejectFeature) {
            int value = sIsFromReject ? 1 : 0;
            if (selection == null) {
                selection = "reject = " + value;
            } else if (!selection.contains("reject")) {
                selection = "(" + selection + ") AND reject = " + value;
            }
        }
        setIsFromReject(false);
        // Aurora xuyong 2014-07-08 added for bug #6430 end
        handler.startDelete(token, null, uri, selection, null);
    }

    //gionee gaoj 2012-10-15 modified for CR00705539 start
    public static void startDelete(AsyncQueryHandler handler, int token,
            boolean deleteStared, String threadIds, String multdelString) {
        String selection = deleteStared ? null : "star=0";
        Uri uri = Uri.parse("content://mms-sms/conversations/" + threadIds);
        if (multdelString != null) {
            String[] selectionArgs = { multdelString };
            handler.startDelete(token, null, uri, selection, selectionArgs);
        } else {
            handler.startDelete(token, null, uri, selection, null);
        }
    }
    //gionee gaoj 2012-10-15 modified for CR00705539  end
    //gionee gaoj 2012-9-20 added for CR00699291 end

     // Aurora liugj 2014-01-06 modified for bath-delete optimize start
    public static void startDelete(final AsyncQueryHandler handler, final int token,
            boolean deleteStared, String threadIds, final String multdelString, boolean isUpdate) {
        final String selection = deleteStared ? null : "star=0";
        final Uri uri = Uri.parse("content://mms-sms/conversations/" + threadIds);
        // Aurora xuyong 2014-07-05 modified for reject feature start
        if (!MmsApp.sHasRejectFeature) {
            if (isUpdate) {
                ContentValues values = new ContentValues(1);
                values.put("deleted", "1");
                handler.startUpdate(token, null, uri, values, selection, null);
            }
        }
        // Aurora xuyong 2014-07-05 modified for reject feature end
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                if (multdelString != null) {
                    String[] selectionArgs = { multdelString };
                    handler.startDelete(token, null, uri, selection, selectionArgs);
                } else {
                    handler.startDelete(token, null, uri, selection, null);
                }
            }
        }).start();
    }
    // Aurora liugj 2014-01-06 modified for bath-delete optimize end
    // Aurora xuyong 2015-12-04 added for aurora2.0 new feature start
    public static void startDeleteAll(AsyncQueryHandler handler, int token, boolean deleteAll, int tabIndex) {
        String selection = null;
        long cpId = AuroraPrivacyUtils.getCurrentAccountId();
        if (cpId > 0) {
            selection = deleteAll ? " is_privacy IN ( 0, " + cpId + " )": "locked=0";
        } else {
            selection = deleteAll ? " is_privacy = 0 ": "locked=0";
        }
        if (tabIndex == ConvFragment.PERSONAL_TAB) {
            selection = "(" + selection + ")" + " AND notification_index == 0";
        } else if (tabIndex ==  ConvFragment.NOTIFICATION_TAB) {
            selection = "(" + selection + ")" + " AND notification_index == 1";
        }
        PduCache.getInstance().purge(Threads.CONTENT_URI);
        handler.startDelete(token, null, Threads.CONTENT_URI, selection, null);
        // Aurora xuyong 2014-10-28 modified for privacy feature end
    }
    // Aurora xuyong 2015-12-04 added for aurora2.0 new feature end
    /**
     * Start deleting all conversations in the database.
     * @param handler An AsyncQueryHandler that will receive onDeleteComplete
     *                upon completion of all conversations being deleted
     * @param token   The token that will be passed to onDeleteComplete
     * @param deleteAll Delete the whole thread including locked messages
     */
    public static void startDeleteAll(AsyncQueryHandler handler, int token, boolean deleteAll) {
        //wappush: do not need modify the code here, but delete function in provider has been modified.
        // Aurora xuyong 2014-10-28 modified for privacy feature start
        String selection = null;
        long cpId = AuroraPrivacyUtils.getCurrentAccountId();
        if (cpId > 0) {
            selection = deleteAll ? " is_privacy IN ( 0, " + cpId + " )": "locked=0";
        } else {
            selection = deleteAll ? " is_privacy = 0 ": "locked=0";
        }
        PduCache.getInstance().purge(Threads.CONTENT_URI);
        handler.startDelete(token, null, Threads.CONTENT_URI, selection, null);
        // Aurora xuyong 2014-10-28 modified for privacy feature end
    }
    // Aurora xuyong 2014-10-28 added for privacy feature start
    public static void startDeleteAll(AsyncQueryHandler handler, int token, boolean deleteAll, long privacy) {
        //wappush: do not need modify the code here, but delete function in provider has been modified.
        String selection = deleteAll ? " is_privacy = " + privacy : "locked=0";
        PduCache.getInstance().purge(Threads.CONTENT_URI);
        handler.startDelete(token, null, Threads.CONTENT_URI, selection, null);
    }
    // Aurora xuyong 2014-10-28 added for privacy feature end
    /**
     * Check for locked messages in all threads or a specified thread.
     * @param handler An AsyncQueryHandler that will receive onQueryComplete
     *                upon completion of looking for locked messages
     * @param threadIds   A list of threads to search. null means all threads
     * @param token   The token that will be passed to onQueryComplete
     */
    public static void startQueryHaveLockedMessages(AsyncQueryHandler handler,
            Collection<Long> threadIds,
            int token) {
        handler.cancelOperation(token);
        Uri uri = MmsSms.CONTENT_LOCKED_URI;

        String selection = null;
        if (threadIds != null) {
            StringBuilder buf = new StringBuilder();
            int i = 0;

            for (long threadId : threadIds) {
                if (i++ > 0) {
                    buf.append(" OR ");
                }
                // We have to build the selection arg into the selection because deep down in
                // provider, the function buildUnionSubQuery takes selectionArgs, but ignores it.
                buf.append(Mms.THREAD_ID).append("=").append(Long.toString(threadId));
            }
            selection = buf.toString();
        }
        handler.startQuery(token, threadIds, uri,
                ALL_THREADS_PROJECTION, selection, null, Conversations.DEFAULT_SORT_ORDER);
    }

    /**
     * Check for locked messages in all threads or a specified thread.
     * @param handler An AsyncQueryHandler that will receive onQueryComplete
     *                upon completion of looking for locked messages
     * @param threadId   The threadId of the thread to search. -1 means all threads
     * @param token   The token that will be passed to onQueryComplete
     */
    public static void startQueryHaveLockedMessages(AsyncQueryHandler handler,
            long threadId,
            int token) {
        ArrayList<Long> threadIds = null;
        if (threadId != -1) {
            threadIds = new ArrayList<Long>();
            threadIds.add(threadId);
        }
        startQueryHaveLockedMessages(handler, threadIds, token);
    }

    /**
     * Fill the specified conversation with the values from the specified
     * cursor, possibly setting recipients to empty if {@value allowQuery}
     * is false and the recipient IDs are not in cache.  The cursor should
     * be one made via {@link #startQueryForAll}.
     */
    private static void fillFromCursor(Context context, Conversation conv,
                                       Cursor c, boolean allowQuery) {
        synchronized (conv) {
            conv.mThreadId = c.getLong(ID);
            conv.mDate = c.getLong(DATE);
            conv.mMessageCount = c.getInt(MESSAGE_COUNT);

            // Replace the snippet with a default value if it's empty.
            String snippet = MessageUtils.extractEncStrFromCursor(c, SNIPPET, SNIPPET_CS);
            if (TextUtils.isEmpty(snippet)) {
                snippet = context.getString(R.string.no_subject_view);
            }
            conv.mSnippet = snippet;

            conv.setHasUnreadMessages(c.getInt(READ) == 0);
            conv.mHasError = (c.getInt(ERROR) != 0);
            conv.mHasAttachment = (c.getInt(HAS_ATTACHMENT) != 0);
            
            //a0
            //wappush: get the value for mType
            conv.mType = c.getInt(TYPE);
            conv.mMessageStatus = c.getInt(STATUS);
            //a1
            //gionee gaoj 2012-3-22 added for CR00555790 start
            if (MmsApp.mGnMessageSupport) {
                conv.mSimId = c.getInt(SIM_ID);
                conv.setHasEncryption(c.getInt(ENCRYPTION) == 1);
            }
            //gionee gaoj 2012-3-22 added for CR00555790 end
            
            //Gionee <gaoj> <2013-05-13> added for CR00811367 begin
            if (MmsApp.mGnPerfList) {
                conv.mUnreadMessageCount = conv.mMessageCount - c.getInt(READCOUNT);
                if (conv.mUnreadMessageCount < 0) {
                    conv.mUnreadMessageCount = 0;
                }
            }
            //Gionee <gaoj> <2013-05-13> added for CR00811367 end
            // Aurora xuyong 2014-10-23 added for privacy feature start
            conv.mPrivacy = c.getLong(IS_PRIVACY);
            // Aurora xuyong 2014-10-23 added for privacy feature end
            // Aurora xuyong 2016-01-27 added for aurora 2.0 new feature start
            conv.mNotificationIndex = c.getInt(NOTIFICATION_INDEX);
            // Aurora xuyong 2016-01-27 added for aurora 2.0 new feature end
        }
        // Fill in as much of the conversation as we can before doing the slow stuff of looking
        // up the contacts associated with this conversation.
        String recipientIds = c.getString(RECIPIENT_IDS);
        // Aurora xuyong 2014-10-23 modified for privacy feature start
        ContactList recipients = ContactList.getByIds(context, recipientIds, allowQuery);
        // Aurora xuyong 2014-10-23 modified for privacy feature end
        synchronized (conv) {
            conv.mRecipients = recipients;
            //Gionee <gaoj> <2013-05-13> added for CR00811367 begin
            if (!MmsApp.mGnPerfList) {
                //Gionee <gaoj> <2013-05-13> added for CR00811367 end
            //gionee gaoj 2012-4-10 added for CR00555790 start
            if (MmsApp.mGnMessageSupport) {
                if (c.getInt(READ) != 0) {
                    conv.mUnreadMessageCount = 0;
                } else {
                    if (conv.mType == Threads.WAPPUSH_THREAD) {
                        if (FeatureOption.MTK_WAPPUSH_SUPPORT) {
                            Cursor cc = context.getContentResolver().query(WapPush.CONTENT_URI,
                                    UNREAD_PROJECTION,
                                    "(thread_id=" + conv.mThreadId +") AND " + READ_SELECTION,
                                    null,
                                    null);
                            if (cc!=null) {
                                conv.mUnreadMessageCount = conv.mMessageCount - cc.getCount();
                                cc.close();
                            }
                        }
                    } else {
                        Cursor cc = context.getContentResolver().query(ContentUris.withAppendedId(Threads.CONTENT_URI, conv.mThreadId),
                                UNREAD_PROJECTION, READ_SELECTION, null, null);
                        if (cc!=null) {
                            conv.mUnreadMessageCount = conv.mMessageCount - cc.getCount();
                            cc.close();
                        }
                    }
                }
            }
            //gionee gaoj 2012-4-10 added for CR00555790 end
        //Gionee <gaoj> <2013-05-13> added for CR00811367 begin
        }
        //Gionee <gaoj> <2013-05-13> added for CR00811367 end
        }

        if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
            Log.d(TAG, "fillFromCursor: conv=" + conv + ", recipientIds=" + recipientIds);
        }
    }
    // Aurora xuyong 2014-10-23 added for privacy feature start
    private static void fillFromCursor(Context context, Conversation conv,
            Cursor c, boolean allowQuery, long privacy) {
        synchronized (conv) {
            conv.mThreadId = c.getLong(ID);
            conv.mDate = c.getLong(DATE);
            conv.mMessageCount = c.getInt(MESSAGE_COUNT);

            // Replace the snippet with a default value if it's empty.
            String snippet = MessageUtils.extractEncStrFromCursor(c, SNIPPET, SNIPPET_CS);
            if (TextUtils.isEmpty(snippet)) {
                snippet = context.getString(R.string.no_subject_view);
            }
            conv.mSnippet = snippet;
            
            conv.setHasUnreadMessages(c.getInt(READ) == 0);
            conv.mHasError = (c.getInt(ERROR) != 0);
            conv.mHasAttachment = (c.getInt(HAS_ATTACHMENT) != 0);
            
            //a0
            //wappush: get the value for mType
            conv.mType = c.getInt(TYPE);
            conv.mMessageStatus = c.getInt(STATUS);
            //a1
            //gionee gaoj 2012-3-22 added for CR00555790 start
            if (MmsApp.mGnMessageSupport) {
                conv.mSimId = c.getInt(SIM_ID);
                conv.setHasEncryption(c.getInt(ENCRYPTION) == 1);
            }
            //gionee gaoj 2012-3-22 added for CR00555790 end

            //Gionee <gaoj> <2013-05-13> added for CR00811367 begin
            if (MmsApp.mGnPerfList) {
                conv.mUnreadMessageCount = conv.mMessageCount - c.getInt(READCOUNT);
                if (conv.mUnreadMessageCount < 0) {
                    conv.mUnreadMessageCount = 0;
                }
            }
            //Gionee <gaoj> <2013-05-13> added for CR00811367 end
            conv.mPrivacy = c.getLong(IS_PRIVACY);
            // Aurora xuyong 2016-01-27 added for aurora 2.0 new feature start
            conv.mNotificationIndex = c.getInt(NOTIFICATION_INDEX);
            // Aurora xuyong 2016-01-27 added for aurora 2.0 new feature end
        }
        // Fill in as much of the conversation as we can before doing the slow stuff of looking
        // up the contacts associated with this conversation.
        String recipientIds = c.getString(RECIPIENT_IDS);
        ContactList recipients = ContactList.getByIds(context, recipientIds, allowQuery, privacy);
        if (recipients.size() == 1) {
            recipients.get(0).setPrivacy(privacy);
        }
        synchronized (conv) {
            conv.mRecipients = recipients;
            //Gionee <gaoj> <2013-05-13> added for CR00811367 begin
            if (!MmsApp.mGnPerfList) {
                //Gionee <gaoj> <2013-05-13> added for CR00811367 end
                //gionee gaoj 2012-4-10 added for CR00555790 start
                if (MmsApp.mGnMessageSupport) {
                    if (c.getInt(READ) != 0) {
                        conv.mUnreadMessageCount = 0;
                    } else {
                        if (conv.mType == Threads.WAPPUSH_THREAD) {
                            if (FeatureOption.MTK_WAPPUSH_SUPPORT) {
                                Cursor cc = context.getContentResolver().query(WapPush.CONTENT_URI,
                                        UNREAD_PROJECTION,
                                        "(thread_id=" + conv.mThreadId +") AND " + READ_SELECTION,
                                        null,
                                        null);
                                if (cc!=null) {
                                    conv.mUnreadMessageCount = conv.mMessageCount - cc.getCount();
                                    cc.close();
                                }
                            }
                        } else {
                            Cursor cc = context.getContentResolver().query(ContentUris.withAppendedId(Threads.CONTENT_URI, conv.mThreadId),
                                    UNREAD_PROJECTION, READ_SELECTION, null, null);
                            if (cc!=null) {
                                conv.mUnreadMessageCount = conv.mMessageCount - cc.getCount();
                                cc.close();
                            }
                        }
                    }
                }
                //gionee gaoj 2012-4-10 added for CR00555790 end
                //Gionee <gaoj> <2013-05-13> added for CR00811367 begin
            }
            //Gionee <gaoj> <2013-05-13> added for CR00811367 end
        }

        if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
            Log.d(TAG, "fillFromCursor: conv=" + conv + ", recipientIds=" + recipientIds);
        }
    }
    // Aurora xuyong 2014-10-23 modified for privacy feature end
    /**
     * Private cache for the use of the various forms of Conversation.get.
     */
    private static class Cache {
        private static Cache sInstance = new Cache();
        static Cache getInstance() { return sInstance; }

        // gionee lwzh add for CR00774362 20130227 begin use ConcurrentHashMap is better
        private final ConcurrentHashMap<Long, Conversation> mCache;
        private Cache() {
            mCache = new ConcurrentHashMap<Long, Conversation>();
        }

        /**
         * Return the conversation with the specified thread ID, or
         * null if it's not in cache.
         */
        static Conversation get(long threadId) {
            // gionee lwzh add for CR00774362 20130227 begin
            /** 
            synchronized (sInstance) {
                if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
                    LogTag.debug("Conversation get with threadId: " + threadId);
                }
                for (Conversation c : sInstance.mCache) {
                    if (DEBUG) {
                        LogTag.debug("Conversation get() threadId: " + threadId +
                                " c.getThreadId(): " + c.getThreadId());
                    }
                    if (c.getThreadId() == threadId) {
                        return c;
                    }
                }
            }
            return null;
            */
            Conversation c = sInstance.mCache.get(threadId);
            if (c != null && c.getThreadId() == threadId) {
                return c;
            }
            return null;
            // gionee lwzh add for CR00774362 20130227 end
        }

        /**
         * Return the conversation with the specified recipient
         * list, or null if it's not in cache.
         */
        static Conversation get(ContactList list) {
            // gionee lwzh add for CR00774362 20130227 begin
            /** 
            synchronized (sInstance) {
                if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
                    LogTag.debug("Conversation get with ContactList: " + list);
                }
                for (Conversation c : sInstance.mCache) {
                    if (c.getRecipients().equals(list)) {
                        return c;
                    }
                }
            }
            */
            Collection<Conversation> conv = sInstance.mCache.values();
            for (Conversation c : conv) {
                if (c.getRecipients().equals(list)) {
                    return c;
                }
            }
            return null;
            // gionee lwzh add for CR00774362 20130227 end
        }

        /**
         * Put the specified conversation in the cache.  The caller
         * should not place an already-existing conversation in the
         * cache, but rather update it in place.
         */
        static void put(Conversation c) {
            // gionee lwzh add for CR00774362 20130227 begin
            /** 
            synchronized (sInstance) {
                // We update cache entries in place so people with long-
                // held references get updated.
                if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
                    Log.d(TAG, "Conversation.Cache.put: conv= " + c + ", hash: " + c.hashCode());
                }

                if (sInstance.mCache.contains(c)) {
                    if (DEBUG) {
                        dumpCache();
                    }
                    throw new IllegalStateException("cache already contains " + c +
                            " threadId: " + c.mThreadId);
                }
                sInstance.mCache.add(c);
            }
            */
            if (sInstance.mCache.contains(c)) {
                if (DEBUG) {
                    dumpCache();
                }
                throw new IllegalStateException("cache already contains " + c +
                        " threadId: " + c.mThreadId);
            }
            sInstance.mCache.put(c.getThreadId(), c);
            // gionee lwzh add for CR00774362 20130227 end
        }

        /**
         * Replace the specified conversation in the cache. This is used in cases where we
         * lookup a conversation in the cache by threadId, but don't find it. The caller
         * then builds a new conversation (from the cursor) and tries to add it, but gets
         * an exception that the conversation is already in the cache, because the hash
         * is based on the recipients and it's there under a stale threadId. In this function
         * we remove the stale entry and add the new one. Returns true if the operation is
         * successful
         */
        static boolean replace(Conversation c) {
            // gionee lwzh add for CR00774362 20130227 begin
            /** 
            synchronized (sInstance) {
                if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
                    LogTag.debug("Conversation.Cache.put: conv= " + c + ", hash: " + c.hashCode());
                }

                if (!sInstance.mCache.contains(c)) {
                    if (DEBUG) {
                        dumpCache();
                    }
                    return false;
                }
                // Here it looks like we're simply removing and then re-adding the same object
                // to the hashset. Because the hashkey is the conversation's recipients, and not
                // the thread id, we'll actually remove the object with the stale threadId and
                // then add the the conversation with updated threadId, both having the same
                // recipients.
                sInstance.mCache.remove(c);
                sInstance.mCache.add(c);
                return true;
            }
            */
            if (!sInstance.mCache.contains(c)) {
                if (DEBUG) {
                    dumpCache();
                }
                return false;
            }
            sInstance.mCache.replace(c.getThreadId(), c);
            return true;
            // gionee lwzh add for CR00774362 20130227 end
        }

        static void remove(long threadId) {
            // gionee lwzh add for CR00774362 20130227 begin
            /** 
            synchronized (sInstance) {
                if (DEBUG) {
                    LogTag.debug("remove threadid: " + threadId);
                    dumpCache();
                }
                for (Conversation c : sInstance.mCache) {
                    if (c.getThreadId() == threadId) {
                        sInstance.mCache.remove(c);
                        return;
                    }
                }
            }
            */
            if (DEBUG) {
                LogTag.debug("remove threadid: " + threadId);
                dumpCache();
            }
            sInstance.mCache.remove(threadId);
            // gionee lwzh add for CR00774362 20130227 end
        }

        static void dumpCache() {
            // gionee lwzh add for CR00774362 20130227 begin
            /**
            synchronized (sInstance) {
                LogTag.debug("Conversation dumpCache: ");
                for (Conversation c : sInstance.mCache) {
                    LogTag.debug("   conv: " + c.toString() + " hash: " + c.hashCode());
                }
            }
            */
            LogTag.debug("Conversation dumpCache: ");
            Collection<Conversation> conv = sInstance.mCache.values();
            for (Conversation c : conv) {
                LogTag.debug("   conv: " + c.toString() + " hash: " + c.hashCode());
            }
            // gionee lwzh add for CR00774362 20130227 end
        }

        /**
         * Remove all conversations from the cache that are not in
         * the provided set of thread IDs.
         */
        static void keepOnly(Set<Long> threads) {
            // gionee lwzh add for CR00774362 20130227 begin
            /** 
            synchronized (sInstance) {
                Iterator<Conversation> iter = sInstance.mCache.iterator();
                //while (iter.hasNext()) {
                //    Conversation c = iter.next();
                //    if (!threads.contains(c.getThreadId())) {
                //        iter.remove();
                //    }
                //}
                Conversation c = null;
                while (iter.hasNext()) {
                    Conversation c = iter.next();
                    if (!threads.contains(c.getThreadId())) {
                        iter.remove();
                    }
                }
            }
            */
            Iterator<Long> iter = threads.iterator();
            while (iter.hasNext()) {
                sInstance.mCache.remove(iter.next());
            }
            if (DEBUG) {
                LogTag.debug("after keepOnly");
                dumpCache();
            }
            // gionee lwzh add for CR00774362 20130227 end
        }
    }

    /**
     * Set up the conversation cache.  To be called once at application
     * startup time.
     */
    public static void init(final Context context) {
        mIsInitialized = true;
        // gionee lwzh remove for CR00774362 20130227
        // new Thread(new Runnable() {
        // public void run() {
        // //gionee gaoj 2012-9-28 added for CR00705234 start
        // Looper.prepare();
        // Contact.gninit(context);
        // cacheAllThreads(context);
        // if (MmsApp.mEncryption) {
        // cachesmspsw(context);
        // }
        // Looper.loop();
        // //gionee gaoj 2012-9-28 added for CR00705234 end
        // }
        // }).start();
    }

    
    public static boolean isInitialized() {
        return mIsInitialized;
    }
    
    public static void markAllConversationsAsSeen(final Context context) {
        if (DEBUG) {
            LogTag.debug("Conversation.markAllConversationsAsSeen");
        }

        new Thread(new Runnable() {
            public void run() {
                blockingMarkAllSmsMessagesAsSeen(context);
                blockingMarkAllMmsMessagesAsSeen(context);
                
                //a0
                blockingMarkAllCellBroadcastMessagesAsSeen(context);
                //a1

                // Always update notifications regardless of the read state.
                MessagingNotification.blockingUpdateAllNotifications(context);
                
                //a0
                if(FeatureOption.MTK_WAPPUSH_SUPPORT){
                    blockingMarkAllWapPushMessagesAsSeen(context);
                    WapPushMessagingNotification.blockingUpdateNewMessageIndicator(context,false);
                }
                //a1
            }
        }).start();
    }

    private static void blockingMarkAllSmsMessagesAsSeen(final Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Sms.Inbox.CONTENT_URI,
                SEEN_PROJECTION,
                // Aurora xuyong 2014-11-10 modified for bug #9716 start
                Sms.SEEN + "=0 AND is_privacy >= 0",
                // Aurora xuyong 2014-11-10 modified for bug #9716 end
                null,
                null);

        int count = 0;

        if (cursor != null) {
            try {
                count = cursor.getCount();
            } finally {
                cursor.close();
            }
        }

        if (count == 0) {
            return;
        }

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.d(TAG, "mark " + count + " SMS msgs as seen");
        }

        ContentValues values = new ContentValues(1);
        values.put(Sms.SEEN, 1);

        resolver.update(Sms.Inbox.CONTENT_URI,
                values,
                Sms.SEEN + "=0",
                null);
    }

    private static void blockingMarkAllMmsMessagesAsSeen(final Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Mms.Inbox.CONTENT_URI,
                SEEN_PROJECTION,
                // Aurora xuyong 2014-11-10 modified for bug #9716 start
                Mms.SEEN + "=0 AND is_privacy >= 0",
                // Aurora xuyong 2014-11-10 modified for bug #9716 end
                null,
                null);

        int count = 0;

        if (cursor != null) {
            try {
                count = cursor.getCount();
            } finally {
                cursor.close();
            }
        }

        if (count == 0) {
            return;
        }

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.d(TAG, "mark " + count + " MMS msgs as seen");
        }

        ContentValues values = new ContentValues(1);
        values.put(Mms.SEEN, 1);

        resolver.update(Mms.Inbox.CONTENT_URI,
                values,
                Mms.SEEN + "=0",
                null);

    }
    
    /**
     * Are we in the process of loading and caching all the threads?.
     */
    public static boolean loadingThreads() {
        synchronized (Cache.getInstance()) {
            return mLoadingThreads;
        }
    }

    private static void cacheAllThreads(Context context) {
        if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
            LogTag.debug("[Conversation] cacheAllThreads: begin");
        }
        synchronized (Cache.getInstance()) {
            if (mLoadingThreads) {
                return;
                }
            mLoadingThreads = true;
        }

        // Keep track of what threads are now on disk so we
        // can discard anything removed from the cache.
        HashSet<Long> threadsOnDisk = new HashSet<Long>();

        // Query for all conversations.
        Cursor c = context.getContentResolver().query(sAllThreadsUri,
                ALL_THREADS_PROJECTION, null, null, null);
        try {
            if (c != null) {
                //m0
                /*
                while (c.moveToNext()) {
                    long threadId = c.getLong(ID);
                    threadsOnDisk.add(threadId);
                }
                */
                
                long threadId = 0L;
                while (c.moveToNext()) {
                    threadId = c.getLong(ID);
                    threadsOnDisk.add(threadId);
                //m1

                    // Try to find this thread ID in the cache.
                    Conversation conv;
                    synchronized (Cache.getInstance()) {
                        conv = Cache.get(threadId);
                    }

                    if (conv == null) {
                        // Make a new Conversation and put it in
                        // the cache if necessary.
                        conv = new Conversation(context, c, true);

                        try {
                            Thread.sleep(10);                
                        } catch (Exception e) {
                            
                        }
                        try {
                            synchronized (Cache.getInstance()) {
                                Cache.put(conv);
                            }
                        } catch (IllegalStateException e) {
                            Log.e(TAG, "Tried to add duplicate Conversation to Cache" +
                                    " for threadId: " + threadId + " new conv: " + conv);
                            if (!Cache.replace(conv)) {
                                Log.e(TAG, "cacheAllThreads cache.replace failed on " + conv);
                            }
                        }
                    } else {
                        // Or update in place so people with references
                        // to conversations get updated too.
                        fillFromCursor(context, conv, c, true);
                    }
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
            synchronized (Cache.getInstance()) {
                mLoadingThreads = false;
            }
        }

        // Purge the cache of threads that no longer exist on disk.
        Cache.keepOnly(threadsOnDisk);

        if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
            LogTag.debug("[Conversation] cacheAllThreads: finished");
            Cache.dumpCache();
        }
    }

    private boolean loadFromThreadId(long threadId, boolean allowQuery) {
        Cursor c = mContext.getContentResolver().query(sAllThreadsUri, ALL_THREADS_PROJECTION,
                "_id=" + Long.toString(threadId), null, null);
        try {
            if (c.moveToFirst()) {
                fillFromCursor(mContext, this, c, allowQuery);

                if (threadId != mThreadId) {
                    LogTag.error("loadFromThreadId: fillFromCursor returned differnt thread_id!" +
                            " threadId=" + threadId + ", mThreadId=" + mThreadId);
                }
            } else {
                LogTag.error("loadFromThreadId: Can't find thread ID " + threadId);
                return false;
            }
        } finally {
        	// Aurora xuyong 2015-09-22 modified for bug #16651 start
        	if (c != null && !c.isClosed()) {
        		c.close();
        	}
        	// Aurora xuyong 2015-09-22 modified for bug #16651 end
        }
        return true;
    }
    // Aurora xuyong 2014-10-23 added for privacy feature start
    private boolean loadFromThreadId(long threadId, boolean allowQuery, long privacy) {
        Cursor c = mContext.getContentResolver().query(sAllThreadsUri, ALL_THREADS_PROJECTION,
                "_id=" + Long.toString(threadId) + " AND is_privacy = " + privacy, null, null);
        try {
            if (c.moveToFirst()) {
                fillFromCursor(mContext, this, c, allowQuery, privacy);
                if (threadId != mThreadId) {
                    LogTag.error("loadFromThreadId: fillFromCursor returned differnt thread_id!" +
                            " threadId=" + threadId + ", mThreadId=" + mThreadId);
                }
            } else {
                LogTag.error("loadFromThreadId: Can't find thread ID " + threadId);
                return false;
            }
        } finally {
        	// Aurora xuyong 2015-09-22 modified for bug #16651 start
        	if (c != null && !c.isClosed()) {
        		c.close();
        	}
        	// Aurora xuyong 2015-09-22 modified for bug #16651 end
        }
        return true;
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end
    public static String getRecipients(Uri uri) {
        String base = uri.getSchemeSpecificPart();
        int pos = base.indexOf('?');
        return (pos == -1) ? base : base.substring(0, pos);
    }

    public static void dump() {
        Cache.dumpCache();
    }

    public static void dumpThreadsTable(Context context) {
        LogTag.debug("**** Dump of threads table ****");
        Cursor c = context.getContentResolver().query(sAllThreadsUri,
                ALL_THREADS_PROJECTION, null, null, "date ASC");
        try {
            c.moveToPosition(-1);
            while (c.moveToNext()) {
                String snippet = MessageUtils.extractEncStrFromCursor(c, SNIPPET, SNIPPET_CS);
                Log.d(TAG, "dumpThreadsTable threadId: " + c.getLong(ID) +
                        " " + ThreadsColumns.DATE + " : " + c.getLong(DATE) +
                        " " + ThreadsColumns.MESSAGE_COUNT + " : " + c.getInt(MESSAGE_COUNT) +
                        " " + ThreadsColumns.SNIPPET + " : " + snippet +
                        " " + ThreadsColumns.READ + " : " + c.getInt(READ) +
                        " " + ThreadsColumns.ERROR + " : " + c.getInt(ERROR) +
                        " " + ThreadsColumns.HAS_ATTACHMENT + " : " + c.getInt(HAS_ATTACHMENT) +
                        " " + ThreadsColumns.RECIPIENT_IDS + " : " + c.getString(RECIPIENT_IDS));

                ContactList recipients = ContactList.getByIds(c.getString(RECIPIENT_IDS), false);
                Log.d(TAG, "----recipients: " + recipients.serialize());
            }
        } finally {
            c.close();
        }
    }

    static final String[] SMS_PROJECTION = new String[] {
        BaseColumns._ID,
        // For SMS
        Sms.THREAD_ID,
        Sms.ADDRESS,
        Sms.BODY,
        Sms.DATE,
        Sms.READ,
        Sms.TYPE,
        Sms.STATUS,
        Sms.LOCKED,
        Sms.ERROR_CODE,
    };

    // The indexes of the default columns which must be consistent
    // with above PROJECTION.
    static final int COLUMN_ID                  = 0;
    static final int COLUMN_THREAD_ID           = 1;
    static final int COLUMN_SMS_ADDRESS         = 2;
    static final int COLUMN_SMS_BODY            = 3;
    static final int COLUMN_SMS_DATE            = 4;
    static final int COLUMN_SMS_READ            = 5;
    static final int COLUMN_SMS_TYPE            = 6;
    static final int COLUMN_SMS_STATUS          = 7;
    static final int COLUMN_SMS_LOCKED          = 8;
    static final int COLUMN_SMS_ERROR_CODE      = 9;

    public static void dumpSmsTable(Context context) {
        LogTag.debug("**** Dump of sms table ****");
        Cursor c = context.getContentResolver().query(Sms.CONTENT_URI,
                SMS_PROJECTION, null, null, "_id DESC");
        try {
            // Only dump the latest 20 messages
            c.moveToPosition(-1);
            while (c.moveToNext() && c.getPosition() < 20) {
                String body = c.getString(COLUMN_SMS_BODY);
                LogTag.debug("dumpSmsTable " + BaseColumns._ID + ": " + c.getLong(COLUMN_ID) +
                        " " + Sms.THREAD_ID + " : " + c.getLong(DATE) +
                        " " + Sms.ADDRESS + " : " + c.getString(COLUMN_SMS_ADDRESS) +
                        " " + Sms.BODY + " : " + body.substring(0, Math.min(body.length(), 8)) +
                        " " + Sms.DATE + " : " + c.getLong(COLUMN_SMS_DATE) +
                        " " + Sms.TYPE + " : " + c.getInt(COLUMN_SMS_TYPE));
            }
        } finally {
            c.close();
        }
    }

    /**
     * verifySingleRecipient takes a threadId and a string recipient [phone number or email
     * address]. It uses that threadId to lookup the row in the threads table and grab the
     * recipient ids column. The recipient ids column contains a space-separated list of
     * recipient ids. These ids are keys in the canonical_addresses table. The recipient is
     * compared against what's stored in the mmssms.db, but only if the recipient id list has
     * a single address.
     * @param context is used for getting a ContentResolver
     * @param threadId of the thread we're sending to
     * @param recipientStr is a phone number or email address
     * @return the verified number or email of the recipient
     */
    public static String verifySingleRecipient(final Context context,
            final long threadId, final String recipientStr) {
        if (threadId <= 0) {
            LogTag.error("verifySingleRecipient threadId is ZERO, recipient: " + recipientStr);
            LogTag.dumpInternalTables(context);
            return recipientStr;
        }
        //gionee gaoj 2012-8-6 modified for CR00663678 start
        Cursor c = null;
        String recipientIds = "";
        String address = "";
        try {
            c = context.getContentResolver().query(sAllThreadsUri, ALL_THREADS_PROJECTION,
                    "_id=" + Long.toString(threadId), null, null);
            if (c == null) {
                LogTag.error("verifySingleRecipient threadId: " + threadId +
                        " resulted in NULL cursor , recipient: " + recipientStr);
                LogTag.dumpInternalTables(context);
                return recipientStr;
            }
            address = recipientStr;
            if (!c.moveToFirst()) {
                LogTag.error("verifySingleRecipient threadId: " + threadId +
                        " can't moveToFirst , recipient: " + recipientStr);
                LogTag.dumpInternalTables(context);
                return recipientStr;
            }
            recipientIds = c.getString(RECIPIENT_IDS);
        } finally {
            c.close();
        }
        //gionee gaoj 2012-8-6 modified for CR00663678 end
        String[] ids = recipientIds.split(" ");

        if (ids.length != 1) {
            // We're only verifying the situation where we have a single recipient input against
            // a thread with a single recipient. If the thread has multiple recipients, just
            // assume the input number is correct and return it.
            return recipientStr;
        }

        // Get the actual number from the canonical_addresses table for this recipientId
        address = RecipientIdCache.getSingleAddressFromCanonicalAddressInDb(context, ids[0]);

        if (TextUtils.isEmpty(address)) {
            LogTag.error("verifySingleRecipient threadId: " + threadId +
                    " getSingleNumberFromCanonicalAddresses returned empty number for: " +
                    ids[0] + " recipientIds: " + recipientIds);
            LogTag.dumpInternalTables(context);
            return recipientStr;
        }
        if (PhoneNumberUtils.compareLoosely(recipientStr, address)) {
            // Bingo, we've got a match. We're returning the input number because of area
            // codes. We could have a number in the canonical_address name of "232-1012" and
            // assume the user's phone's area code is 650. If the user sends a message to
            // "(415) 232-1012", it will loosely match "232-1202". If we returned the value
            // from the table (232-1012), the message would go to the wrong person (to the
            // person in the 650 area code rather than in the 415 area code).
            return recipientStr;
        }

        if (context instanceof AuroraActivity) {
            LogTag.warnPossibleRecipientMismatch("verifySingleRecipient for threadId: " +
                    threadId + " original recipient: " + recipientStr +
                    " recipient from DB: " + address, (AuroraActivity)context);
        }
        LogTag.dumpInternalTables(context);
        if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
            LogTag.debug("verifySingleRecipient for threadId: " +
                    threadId + " original recipient: " + recipientStr +
                    " recipient from DB: " + address);
        }
        return address;
    }
    
    //a0
    private static final String[] ALL_CHANNEL_PROJECTION = { 
        CbSms._ID,
        CbSms.CbChannel.NAME,
        CbSms.CbChannel.NUMBER };
    private static final String[] ALL_ADDRESS_PROJECTION = { 
        CbSms._ID,
        CbSms.CanonicalAddressesColumns.ADDRESS };
    
    /**
     * Mark for the thread conversation has been read.
     */
    private static final String READ_SELECTION = "(read=1)";
    private static final Uri ADDRESS_ID_URI = Uri.parse("content://cb/addresses/#");
    
    //wappush: add TYPE, which already exists
    private static final int TYPE           = 9;
    private static final int READCOUNT    = 10;
    private static final int STATUS       = 11;
    
    private int mUnreadMessageCount;    // Number of unread message.
    private int mMessageStatus;         // Message Status.
    
    //add for cb
    private int mAddressId;          // Number of messages.
    private int mChannelId = -1;            // Text of the most recent message.
    private String mChannelName = null;
    
    //wappush: add mType variable
    private int mType;
    //a1
    
    //a0
    public static Conversation upDateThread(Context context, long threadId, boolean allowQuery) {
        Cache.remove(threadId);
        return get(context, threadId, allowQuery);
    }
    
    public static Conversation upDateThread(Context context, long threadId, boolean allowQuery, long privacy) {
        Cache.remove(threadId);
        return get(context, threadId, allowQuery, privacy);
    }
    
    public static List<String> getNumbers(String recipient) {
        int len = recipient.length();
        List<String> list = new ArrayList<String>();

        int start = 0;
        int i = 0;
        char c;
        while (i < len + 1) {
            if ((i == len) || ((c = recipient.charAt(i)) == ',') || (c == ';')) {
                if (i > start) {
                    list.add(recipient.substring(start, i));

                    // calculate the recipients total length. This is so if the name contains
                    // commas or semis, we'll skip over the whole name to the next
                    // recipient, rather than parsing this single name into multiple
                    // recipients.
                    int spanLen = recipient.substring(start, i).length();
                    if (spanLen > i) {
                        i = spanLen;
                    }
                }

                i++;

                while ((i < len) && (recipient.charAt(i) == ' ')) {
                    i++;
                }

                start = i;
            } else {
                i++;
            }
        }

        return list;
    }
    
    //add for cb
    public synchronized int getChannelId() {
        if(mChannelId == -1) {
            setChannelIdFromDatabase();
        }
        return mChannelId;
    }
    
    private void setChannelIdFromDatabase() {
        Uri uri = ContentUris.withAppendedId(ADDRESS_ID_URI, mAddressId);
        Cursor c = mContext.getContentResolver().query(uri,
                ALL_ADDRESS_PROJECTION, null, null, null);
        try {
            if (c == null || c.getCount() == 0) {
                mChannelId = -1;
            } else {
                c.moveToFirst();
                // Name
                mChannelId = c.getInt(1);
            }
        } finally {
            if(c != null) {
                c.close();
            }
        }
    }

    //wappush: because of different uri
    public void WPMarkAsRead() {
        final Uri threadUri = ContentUris.withAppendedId(
                WapPush.CONTENT_URI_THREAD, mThreadId);
        new Thread(new Runnable() {
            public void run() {
                synchronized (mMarkAsBlockedSyncer) {
                    if (mMarkAsReadBlocked) {
                        try {
                            mMarkAsBlockedSyncer.wait();
                        } catch (InterruptedException e) {
                        }
                    }

                    if (threadUri != null) {
                        buildReadContentValues();

                        // Check the read flag first. It's much faster to do a query than
                        // to do an update. Timing this function show it's about 10x faster to
                        // do the query compared to the update, even when there's nothing to
                        // update.
                        boolean needUpdate = true;

                        Cursor c = mContext.getContentResolver().query(threadUri,
                                UNREAD_PROJECTION, UNREAD_SELECTION, null, null);
                        if (c != null) {
                            try {
                                needUpdate = c.getCount() > 0;
                            } finally {
                                c.close();
                            }
                        }

                        if(needUpdate){
                            mContext.getContentResolver().update(threadUri,mReadContentValues, UNREAD_SELECTION, null);
                            mHasUnreadMessages = false;
                        }
                    }
                }
                WapPushMessagingNotification.updateAllNotifications(mContext);
            }
        }).start();
    }
    
    /**
     * Returns a content:// URI referring to this conversation,
     * or null if it does not exist on disk yet.
     */
    public synchronized Uri getQueryMsgUri() {
        if (mThreadId < 0)
            return null;

        return ContentUris.withAppendedId(Threads.CONTENT_URI, mThreadId);
    }

    // Guarantee Thread ID exists in Database
    public synchronized void guaranteeThreadId() {
        mThreadId = getOrCreateThreadId(mContext, mRecipients, false);
    }

    public synchronized long ensureThreadId(boolean scrubForMmsAddress) {
        if (DEBUG) {
            LogTag.debug("ensureThreadId before: " + mThreadId);
        }
        if (mThreadId <= 0) {
            mThreadId = getOrCreateThreadId(mContext, mRecipients, scrubForMmsAddress);
        }
        if (DEBUG) {
            LogTag.debug("ensureThreadId after: " + mThreadId);
        }

        return mThreadId;
    }
    
    public synchronized int getUnreadMessageCount() {
        return mUnreadMessageCount;
    }
    
    public synchronized int getMessageStatus() {
        return mMessageStatus;
    }
    
    //wappush: add getType function
    /**
     * Returns type of the thread.
     */
    public synchronized int getType() {
        return mType;
    }
    
    private static long getOrCreateThreadId(Context context, ContactList list, final boolean scrubForMmsAddress) {
        // Aurora xuyong 2014-12-24 added for bug #10703 start
        if (list != null && list.size() <= 0) {
             return 0;
        }
        // Aurora xuyong 2014-12-24 added for bug #10703 end
        HashSet<String> recipients = new HashSet<String>();
        Contact cacheContact = null;
        for (Contact c : list) {
            // Aurora xuyong 2014-10-23 modified for privacy feature start
            cacheContact = Contact.get(c.getNumber(), false, c.getPrivacy());
            // Aurora xuyong 2014-10-23 modified for privacy feature end
            String number;
            if (cacheContact != null) {
                number = cacheContact.getNumber();
            } else {
                number = c.getNumber();
            }
            if (scrubForMmsAddress) {
                number = MessageUtils.parseMmsAddress(number);
            }
            
            if (!TextUtils.isEmpty(number) && !recipients.contains(number)) {
                recipients.add(number);
            }
        }
        long retVal = 0;
        try{
          // Aurora xuyong 2014-10-23 modified for privacy feature start
            long privacy = list.get(0).getPrivacy();
            if (list != null && list.size() > 1) {
                privacy = 0;
            }
            retVal = Utils.getOrCreateThreadId(context, recipients, privacy);
          // Aurora xuyong 2014-10-23 modified for privacy feature end
        }catch(IllegalArgumentException e){
            LogTag.error("Can't get or create the thread id");
            return 0;
        }
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("[Conversation] getOrCreateThreadId for (%s) returned %d",
                    recipients, retVal);
        }

        return retVal;
    }
    // Aurora xuyong 2014-10-23 added for privacy feature start
    private static long getOrCreateThreadId(Context context, ContactList list, final boolean scrubForMmsAddress, long privacy) {
        HashSet<String> recipients = new HashSet<String>();
        Contact cacheContact = null;
        for (Contact c : list) {
            cacheContact = Contact.get(c.getNumber(), false, privacy);
            String number;
            if (cacheContact != null) {
                number = cacheContact.getNumber();
            } else {
                number = c.getNumber();
            }
            if (scrubForMmsAddress) {
                number = MessageUtils.parseMmsAddress(number);
            }
            
            if (!TextUtils.isEmpty(number) && !recipients.contains(number)) {
                recipients.add(number);
            }
        }
        long retVal = 0;
        try{
            retVal = Utils.getOrCreateThreadId(context, recipients, privacy);
        }catch(IllegalArgumentException e){
            LogTag.error("Can't get or create the thread id");
            return 0;
        }
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("[Conversation] getOrCreateThreadId for (%s) returned %d",
                    recipients, retVal);
        }

        return retVal;
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end
    //mark all wappush message as seen
    private static void blockingMarkAllWapPushMessagesAsSeen(final Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(WapPush.CONTENT_URI,
                SEEN_PROJECTION,
                Mms.SEEN + "=0",
                null,
                null);

        int count = 0;

        if (cursor != null) {
            try {
                count = cursor.getCount();
            } finally {
                cursor.close();
            }
        }

        if (count == 0) {
            return;
        }

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.d(TAG, "mark " + count + " MMS msgs as seen");
        }

        ContentValues values = new ContentValues(1);
        values.put(Mms.SEEN, 1);

        resolver.update(WapPush.CONTENT_URI,
                values,
                Mms.SEEN + "=0",
                null);

    }
    
    //mark all CellBroadcast message as seen
    private static void blockingMarkAllCellBroadcastMessagesAsSeen(final Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(
                CbSms.CONTENT_URI,
                SEEN_PROJECTION,
                // Aurora xuyong 2014-11-10 modified for bug #9716 start
                CbSms.SEEN + "=0 AND is_privacy >= 0",
                // Aurora xuyong 2014-11-10 modified for bug #9716 end
                null,
                null);

        int count = 0;

        if (cursor != null) {
            try {
                count = cursor.getCount();
            } finally {
                cursor.close();
            }
        }

        if (count == 0) {
            return;
        }

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.d(TAG, "mark " + count + " CB msgs as seen");
        }

        ContentValues values = new ContentValues(1);
        values.put(CbSms.SEEN, 1);

        resolver.update(
                CbSms.CONTENT_URI,
                values,
                CbSms.SEEN + "=0",
                null);
    }

    // Aurora yudingmin 2014-08-30 modified for optimize start
    static public void removeInvalidCache(Cursor cursor) {
        removeInvalidCache(cursor, cursor.getCount());
    }
    // Aurora yudingmin 2014-08-30 modified for optimize end

    static public void removeInvalidCache(Cursor cursor, int count) {
        if (cursor != null) {
            synchronized (Cache.getInstance()) {
                HashSet<Long> threadsOnDisk = new HashSet<Long>();

                if (cursor.moveToFirst()) {
                    do {
                        long threadId = cursor.getLong(ID);
                        threadsOnDisk.add(threadId);
                        // Aurora yudingmin 2014-08-30 modified for optimize start
                    } while (cursor.moveToNext() && count-- > 0);
                    // Aurora yudingmin 2014-08-30 modified for optimize end
                }

                Cache.keepOnly(threadsOnDisk);
            }
        }
    }
  //a1
    
    //gionee gaoj 2012-3-22 added for CR00555790 start
    /**
     * Returns messages id in the conversation.
     */
    public synchronized int getSimId() {
        return mSimId;
    }

    public synchronized boolean getEncryption() {
        return mHasEncryption;
    }

    public synchronized void setHasEncryption(boolean encryption) {
        mHasEncryption = encryption;
    }

    public synchronized long ensureGnThreadId(boolean isDraft) {
        if (DEBUG) {
            LogTag.debug("ensureThreadId before: " + mThreadId);
        }

        if (mThreadId <= 0) {
            mThreadId = getOrCreateGnThreadId(mContext, mRecipients, isDraft);
        }
        if (DEBUG) {
            LogTag.debug("ensureThreadId after: " + mThreadId);
        }

        return mThreadId;
    }

    private static long getOrCreateGnThreadId(Context context, ContactList list, boolean isDraft) {
        HashSet<String> recipients = new HashSet<String>();
        Contact cacheContact = null;
        for (Contact c : list) {
            // Aurora xuyong 2014-10-23 modified for privacy feature start
            cacheContact = Contact.get(c.getNumber(), false, c.getPrivacy());
            // Aurora xuyong 2014-10-23 modified for privacy feature end
            if (cacheContact != null) {
                recipients.add(cacheContact.getNumber());
            } else {
                recipients.add(c.getNumber());
            }
        }

        if (isDraft) {
            recipients.add("gn_draft_address_token");
        }

        long retVal = 0;
        try{
            retVal = Threads.getOrCreateThreadId(context, recipients);
        }catch(IllegalArgumentException e){
            LogTag.error("Can't get or create the thread id");
            return 0;
        }
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("[Conversation] getOrCreateThreadId for (%s) returned %d",
                    recipients, retVal);
        }

        return retVal;
    }
    public int updatethreads(Context context, Uri uri, boolean hasencryption) {
        if (context == null || uri == null) {
            return 0;
        }

        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues(1);

        if (hasencryption) {
            values.put("encryption", 0);
            setHasEncryption(false);
        } else {
            values.put("encryption", 1);
            setHasEncryption(true);
        }
        return resolver.update(uri, values, null, null);
    }

    public void updateNotification() {
        // Update the notification for new messages since they
        // may be deleted.
        MessagingNotification.blockingUpdateNewMessageIndicator(mContext.getApplicationContext(), false, false);
        // Update the notification for failed messages since they
        // may be deleted.
        MessagingNotification.updateSendFailedNotification(mContext.getApplicationContext());
        MessagingNotification.updateDownloadFailedNotification(mContext.getApplicationContext());

        WapPushMessagingNotification.blockingUpdateNewMessageIndicator(mContext.getApplicationContext(),false);
    }


    public static int queryThreadId(Context context, long threadid) {
        ContentResolver resolver = context.getContentResolver();
        String[] THREADS_QUERY_COLUMNS = { "encryption" };
        int encryption = 0;
        //gionee gaoj 2012-8-6 modified for CR00663678 start
        Cursor cursor = null;

        try {
            cursor = resolver.query(sAllThreadsUri, THREADS_QUERY_COLUMNS,
                    "_id =" + threadid, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    encryption = cursor.getInt(0);
                }
            }
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }
        //gionee gaoj 2012-8-6 modified for CR00663678 end
        return encryption;
    }

    public static void startQueryForAll(AsyncQueryHandler handler, int token, boolean nultidelete) {
        handler.cancelOperation(token);
        final int queryToken = token;
        final AsyncQueryHandler queryHandler = handler;
            queryHandler.postDelayed(new Runnable() {
                public void run() {
                    queryHandler.startQuery(
                            queryToken, null, sAllThreadsUri,
                            ALL_THREADS_PROJECTION, "(message_count!=0 and encryption = 0)", null, Conversations.DEFAULT_SORT_ORDER);
                }
            }, 100);
            return;
    }

    public static void savepsw(Context context, String pasString) {
        Uri uri = Uri.parse("content://mms-sms/smspsw/" + pasString);
        context.getContentResolver().insert(uri, new ContentValues());
    }

    public static String cachesmspsw(Context context) {
        Uri uri = Uri.parse("content://mms-sms/smspsw/" + 0);
        Cursor c = context.getContentResolver().query(uri, null, null, null, null);

        //gionee gaoj 2012-9-27 modified for CR00704222 start
        String psw = null;
        try {
            if (c != null && c.moveToFirst()) {
                psw = c.getString(0);
            }
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        Log.d("Test", "psw = "+psw);
        //Gionee <zhouyj> <2013-05-06> modify for CR00803793 begin
        setFirstEncryption(psw == null || psw.length() < 4);
        //Gionee <zhouyj> <2013-05-06> modify for CR00803793 end
        return psw;
        //gionee gaoj 2012-9-27 modified for CR00704222 end
    }

    public static void setFirstEncryption(boolean flag) {
        mFirstEncryption = flag;
    }

    public static boolean getFirstEncryption() {
        return mFirstEncryption;
    }

    public static void startQueryForAllDraft(AsyncQueryHandler handler, int token) {
        handler.cancelOperation(token);
        final int queryToken = token;
        final AsyncQueryHandler queryHandler = handler;

        queryHandler.postDelayed(new Runnable() {
             public void run() {
                queryHandler.startQuery(queryToken, null, sAllThreadsUri, ALL_THREADS_PROJECTION,
                    GnTelephony.GN_SIM_ID + "=-1 AND message_count=0", null, Conversations.DEFAULT_SORT_ORDER);
             }
        }, 200);
    }

    public int getUnReadMessageCount(long threadId, int type){
        int unReadMessageCount = 0;
        Uri uriTemp = null;
        if(type == Threads.WAPPUSH_THREAD){
            uriTemp = WapPush.CONTENT_URI_THREAD;
        } else if(type == Threads.CELL_BROADCAST_THREAD) {
            uriTemp = CbSms.CONTENT_URI;
        } else {
            uriTemp = MmsSms.CONTENT_CONVERSATIONS_URI;
        }
        Uri uri = ContentUris.withAppendedId(uriTemp, threadId);
        Cursor cursor = mContext.getContentResolver().query(uri, UNREAD_PROJECTION, UNREAD_SELECTION, null, null);
        if (cursor != null) {
            unReadMessageCount = cursor.getCount();
            cursor.close();
        }
        return unReadMessageCount;
    }
    
    public static void startQueryHaveStarMessages(AsyncQueryHandler handler, long threadId,
            int token) {
        handler.cancelOperation(token);
        Uri uri = Uri.parse("content://mms-sms/stared");
        if (threadId != -1) {
            uri = ContentUris.withAppendedId(uri, threadId);
        }
        handler.startQuery(token, new Long(threadId), uri,
                ALL_THREADS_PROJECTION, null, null, Conversations.DEFAULT_SORT_ORDER);
    }

    //gionee gaoj 2012-10-15 modified for CR00705539 start
    //gionee gaoj 2012-9-20 added for CR00699291 start
    public static void GnstartDeleteAll(AsyncQueryHandler handler, int token, boolean deleteStared) {
        String selection = deleteStared ? null : "star=0";
        handler.startDelete(token, null, Threads.CONTENT_URI, selection, null);
    }
    //gionee gaoj 2012-9-20 added for CR00699291 
    //gionee gaoj 2012-10-15 modified for CR00705539 end

    public static void startQueryForEncryption(AsyncQueryHandler handler, int token, boolean nultidelete) {

        //gionee gaoj 2013-4-2 added for CR00788343 start
        String selection = "(encryption = 1)";
        if (MmsApp.mIsDraftOpen) {
            selection = "(message_count!=0 and encryption = 1)";
        }
        final String Selection = selection;
        //gionee gaoj 2013-4-2 added for CR00788343 end
        handler.cancelOperation(token);
        final int queryToken = token;
        final AsyncQueryHandler queryHandler = handler;
            queryHandler.postDelayed(new Runnable() {
                public void run() {
                    queryHandler.startQuery(
                            queryToken, null, sAllThreadsUri,
                            ALL_THREADS_PROJECTION, Selection, null, Conversations.DEFAULT_SORT_ORDER);
                }
            }, 200);
            return;
    }

    //gionee gaoj 2012-3-22 added for CR00555790 end
    
    // gionee zhouyj 2012-06-21 add for CR00626541 start 
    public static Conversation get(long id) {
        return Cache.get(id);
    }
    // gionee zhouyj 2012-06-21 add for CR00626541 end 
    
    // gionee lwzh add for CR00774362 20130227 begin
    public static void setNeedCacheConv(boolean need) {
        sNeedCacheConv = need;
    }
    // gionee lwzh add for CR00774362 20130227 end
    
    //Gionee <gaoj> <2013-05-21> added for CR00817770 begin
    public static void startQueryForNoEncryption(AsyncQueryHandler handler, int token, boolean nultidelete) {
        String selection = "(encryption = 0)";
        final String Selection = selection;
        handler.cancelOperation(token);
        final int queryToken = token;
        final AsyncQueryHandler queryHandler = handler;
            queryHandler.postDelayed(new Runnable() {
                public void run() {
                    queryHandler.startQuery(
                            queryToken, null, sAllThreadsUri,
                            ALL_THREADS_PROJECTION, Selection, null, Conversations.DEFAULT_SORT_ORDER);
                }
            }, 0);
            return;
    }
    //Gionee <gaoj> <2013-05-21> added for CR00817770 end
}
