package com.android.mms.data;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.pdu.EncodedStringValue;
import com.aurora.android.mms.pdu.PduPersister;
//Aurora xuyong 2013-11-15 modified for google adapt end
import com.android.mms.LogTag;
import com.android.mms.ui.FolderViewList;
import com.android.mms.R;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.provider.Telephony.Threads;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Sms.Conversations;
import android.text.TextUtils;
import android.util.Log;

public class FolderView {
  //add for folderview mode
    private static final String TAG = "FolderView";
    private static final Uri sThreadUriForRecipients =
        Threads.CONTENT_URI.buildUpon().appendQueryParameter("simple", "true").build();
    
    private static final Uri DRAFTFOLDER_URI   = Uri.parse("content://mms-sms/draftbox");
    private static final Uri INBOXFOLDER_URI   = Uri.parse("content://mms-sms/inbox");
    private static final Uri OUTBOXFOLDER_URI  = Uri.parse("content://mms-sms/outbox");
    private static final Uri SENDBOXFOLDER_URI = Uri.parse("content://mms-sms/sentbox");
    
    
//    private static final String[] ALL_FOLDERVIEW_PROJECTION = {
//        "_id", "date", "read", "body","msg_type","address","attachment"
//    };
    
//    private static final String[] RECIPIENT_PROJECTION = {
//        "recipient_ids"
//    };
    
    private static final int ID             = 0;
    private static final int THREAD_ID      = 1;
    private static final int ADDRESS        = 2;
    private static final int SUBJECT        = 3;
    private static final int DATE           = 4;
    private static final int READ           = 5;
    private static final int TYPE           = 6;
    private static final int STATUS         = 7;      
    private static final int ATTACHMENT     = 8;
    private static final int M_TYPE         = 9;
    private static final int SIM_ID         =10;
    private static final int BOXTYPE        =11;
    private static final int SUB_CS         =12;
    
    private int        mId;                   // The  update id.
    private String     mSubject;              // The  update Subject.
    private long       mDate;                 // The  update time.
    private boolean    mHasAttachment;        // True if any message has an attachment.
    private boolean    mHasUnreadMessages;    // True if any message has read.
    private boolean    mHasError;
    private int        mType; 
    private int        mStatus;
    private int        mBoxType;
    private ContactList  mRecipientString;
    private static     Context mContext;
    
    private FolderView(Context context) {
        mContext = context;
        mRecipientString = new ContactList();
    }

    /**
     * Start a query for all conversations in the database on the specified
     * AsyncQueryHandler.
     *
     * @param handler An AsyncQueryHandler that will receive onQueryComplete
     *                upon completion of the query
     * @param token   The token that will be passed to onQueryComplete
     */
    public static void startQueryForDraftboxView(AsyncQueryHandler handler, int token) {
        handler.cancelOperation(token);
        final int queryToken = token;
        final AsyncQueryHandler queryHandler = handler;
        Log.d(TAG,"startQueryForDraftboxView");
        queryHandler.postDelayed(new Runnable() {
            public void run() {
                queryHandler.startQuery(
                        queryToken, null, DRAFTFOLDER_URI,
                        null, null, null, Conversations.DEFAULT_SORT_ORDER);
            }
        }, 10);
    }
    
    public static void startQueryForInboxView(AsyncQueryHandler handler, int token) {
        handler.cancelOperation(token);
        final int queryToken = token;
        final AsyncQueryHandler queryHandler = handler;
        Log.d(TAG,"startQueryForInboxView");
        queryHandler.postDelayed(new Runnable() {
            public void run() {
                queryHandler.startQuery(
                        queryToken, null, INBOXFOLDER_URI,
                        null, null, null, Conversations.DEFAULT_SORT_ORDER);
            }
        }, 10);
    }
    
    public static void startQueryForInboxView(AsyncQueryHandler handler, int token,int mPostTime) {
        handler.cancelOperation(token);
        final int queryToken = token;
        final AsyncQueryHandler queryHandler = handler;
        final int postTime = mPostTime;
        Log.d(TAG,"startQueryForInboxView");
        queryHandler.postDelayed(new Runnable() {
            public void run() {
                queryHandler.startQuery(
                        queryToken, null, INBOXFOLDER_URI,
                        null, null, null, Conversations.DEFAULT_SORT_ORDER);
            }
        }, postTime);
    }

    private static void markFailedSmsSeen(Context context) {
        ContentValues values = new ContentValues(1);
        values.put("seen", 1);
        String where = Sms.TYPE + " = " + Sms.MESSAGE_TYPE_OUTBOX + " or " +
                       Sms.TYPE + " = " + Sms.MESSAGE_TYPE_QUEUED + " or " +
                       Sms.TYPE + " = " + Sms.MESSAGE_TYPE_FAILED;
        SqliteWrapper.update(context, context.getContentResolver(), Sms.CONTENT_URI, values, where, null);
    }

    private static void markOutboxMmsSeen(Context context) {
        ContentValues values = new ContentValues(1);
        values.put("seen", 1);
        String where = Mms.MESSAGE_BOX + " = " + Mms.MESSAGE_BOX_OUTBOX;
        SqliteWrapper.update(context, context.getContentResolver(), Mms.CONTENT_URI, values, where, null);
    }

    public static void markFailedSmsMmsSeen(final Context context) {
        Log.d(TAG, "markFailedSmsMmsRead");
        new Thread(new Runnable() {
            public void run() {
                markFailedSmsSeen(context);
                markOutboxMmsSeen(context);
            }
        }).start();
    }
    
    public static void startQueryForOutBoxView(AsyncQueryHandler handler, int token) {
        handler.cancelOperation(token);
        final int queryToken = token;
        final AsyncQueryHandler queryHandler = handler;
        Log.d(TAG,"startQueryForOutBoxView");
        queryHandler.postDelayed(new Runnable() {
            public void run() {
                queryHandler.startQuery(
                        queryToken, null, OUTBOXFOLDER_URI,
                        null, null, null, Conversations.DEFAULT_SORT_ORDER);
            }
        }, 10);
    }

    public static void startQueryForSentboxView(AsyncQueryHandler handler, int token) {
        handler.cancelOperation(token);
        final int queryToken = token;
        final AsyncQueryHandler queryHandler = handler;
        Log.d(TAG,"startQueryForSentboxView");
        queryHandler.postDelayed(new Runnable() {
            public void run() {
                queryHandler.startQuery(
                        queryToken, null, SENDBOXFOLDER_URI,
                        null, null, null, Conversations.DEFAULT_SORT_ORDER);
            }
        }, 10);
    }
    

    
    /**
     * Returns a temporary Conversation (not representing one on disk) wrapping
     * the contents of the provided cursor.  The cursor should be the one
     * returned to your AsyncQueryHandler passed in to {@link #startQueryForAll}.
     * The recipient list of this conversation can be empty if the results
     * were not in cache.
     */
    public static FolderView from(Context context, Cursor cursor) {
        // First look in the cache for the Conversation and return that one. That way, all the
        // people that are looking at the cached copy will get updated when fillFromCursor() is
        // called with this cursor.
        FolderView folderview = new FolderView(context);
        fillFromCursor(context,folderview,cursor,false);
        return folderview;
    }
    
    private static void fillFromCursor(Context context, FolderView fview,
            Cursor c, boolean allowQuery) {
        synchronized (fview) {
            fview.mId      = c.getInt(ID);
            fview.mDate    = c.getLong(DATE);      
            fview.mHasUnreadMessages    = (c.getInt(READ)==0);
            fview.mSubject = c.getString(SUBJECT);
            fview.mType    = c.getInt(TYPE);
            fview.mBoxType    = c.getInt(BOXTYPE);
            fview.mStatus     = c.getInt(STATUS);
            fview.mHasError  = (fview.mBoxType == 5 ||  fview.mStatus == 10);
            fview.mHasAttachment = (c.getInt(ATTACHMENT)==1);
        }
        //fview.mRecipientString   = c.getString(ADDRESS);
        String recipientIds = c.getString(ADDRESS);
        ContactList recipients;
        //mms or sms draft
        if(fview.mType == 2 || (fview.mType == 1 && fview.mBoxType ==3) || fview.mType == 4){
            recipients = ContactList.getByIds(recipientIds, allowQuery);
        } else{
            recipients = ContactList.getByNumbers(recipientIds, false, true);
            Log.d(TAG, "recipients " + recipients.toString());     
        }
        if(fview.mType == 2){//mms
             if (!TextUtils.isEmpty(fview.mSubject)) {
                 EncodedStringValue v = new EncodedStringValue(c.getInt(SUB_CS),
                         PduPersister.getBytes(fview.mSubject));
                fview.mSubject = v.getString();
            } else {
                fview.mSubject = context.getString(R.string.no_subject_view);
            }
        }
        synchronized (fview) {
            fview.mRecipientString = recipients;
        }
        Log.d(TAG,"mRecipientString"+fview.mRecipientString);
    }


    
    public synchronized int getmId() {
        return mId;
    }


    public synchronized String getmSubject() {
        return mSubject;
    }

    public synchronized long getmDate() {
        return mDate;
    }


    public synchronized boolean getmHasAttachment() {
        return mHasAttachment;
    }


    public synchronized boolean getmRead() {
        return mHasUnreadMessages;
    }


    public synchronized int getmType() {
        return mType;
    }

    public synchronized ContactList getmRecipientString() {
        return mRecipientString;
    }

    public synchronized int getmStatus() {
        return mStatus;
    }
    
    public synchronized boolean hasError() {
        return mHasError;
    }
    
}
