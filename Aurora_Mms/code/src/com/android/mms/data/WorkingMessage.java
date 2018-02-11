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

package com.android.mms.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import aurora.app.AuroraActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import gionee.provider.GnTelephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Threads;
import android.provider.Telephony.MmsSms.PendingMessages;
import android.provider.Telephony.MmsSms.WordsTable;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.android.common.contacts.DataUsageStatUpdater;
import com.android.common.userhappiness.UserHappinessSignals;
import com.android.mms.ContentRestrictionException;
import com.android.mms.ExceedMessageSizeException;
import com.android.mms.LogTag;
import com.android.mms.MmsConfig;
import com.android.mms.ResolutionException;
import com.android.mms.UnsupportContentTypeException;
import com.android.mms.model.AudioModel;
import com.android.mms.model.FileAttachmentModel;
import com.android.mms.model.ImageModel;
import com.android.mms.model.MediaModel;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.model.TextModel;
import com.android.mms.model.VCardModel;
import com.android.mms.model.VideoModel;
import com.android.mms.transaction.MessageSender;
import com.android.mms.transaction.MmsMessageSender;
import com.android.mms.transaction.SmsMessageSender;
import com.android.mms.ui.AttachmentEditor;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.SlideshowEditor;
import com.android.mms.util.DraftCache;
import com.android.mms.util.Recycler;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.ContentType;
import com.aurora.android.mms.MmsException;
import com.aurora.android.mms.pdu.EncodedStringValue;
import com.aurora.android.mms.pdu.PduBody;
import com.aurora.android.mms.pdu.PduPersister;
import com.aurora.android.mms.pdu.SendReq;
//Aurora xuyong 2013-11-15 modified for google adapt end
// a0
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDiskIOException;
import aurora.preference.AuroraPreferenceManager;
import com.android.mms.RestrictedResolutionException;
import com.android.mms.transaction.SendTransaction;
import com.android.mms.transaction.SmsReceiverService;
import com.android.mms.ui.MessagingPreferenceActivity;
import com.android.mms.ui.SlideEditorActivity;
import com.android.mms.ui.UriImage;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.pdu.PduHeaders;
//Aurora xuyong 2013-11-15 modified for google adapt end
import com.android.mms.MmsApp;
import android.provider.Telephony;

// a1
//ALPS00289861
import com.android.mms.model.SlideshowModel.MediaType;

//gionee gaoj 2012-4-10 added for CR00555790 start
import android.os.SystemProperties;
import com.android.mms.util.DraftCache;
import com.android.mms.widget.MmsWidgetProvider;
//gionee gaoj 2012-4-10 added for CR00555790 end
/**
 * Contains all state related to a message being edited by the user.
 */
public class WorkingMessage {
    private static final String TAG = "WorkingMessage";
    private static final boolean DEBUG = false;

    // Public intents
    public static final String ACTION_SENDING_SMS = "android.intent.action.SENDING_SMS";

    // Intent extras
    public static final String EXTRA_SMS_MESSAGE = "android.mms.extra.MESSAGE";
    public static final String EXTRA_SMS_RECIPIENTS = "android.mms.extra.RECIPIENTS";
    public static final String EXTRA_SMS_THREAD_ID = "android.mms.extra.THREAD_ID";

    // Database access stuff
    private final AuroraActivity mActivity;
    private final ContentResolver mContentResolver;

    // States that can require us to save or send a message as MMS.
    private static final int RECIPIENTS_REQUIRE_MMS = (1 << 0);     // 1
    private static final int HAS_SUBJECT = (1 << 1);                // 2
    private static final int HAS_ATTACHMENT = (1 << 2);             // 4
    private static final int LENGTH_REQUIRES_MMS = (1 << 3);        // 8
    private static final int FORCE_MMS = (1 << 4);                  // 16

    // A bitmap of the above indicating different properties of the message;
    // any bit set will require the message to be sent via MMS.
    private int mMmsState;

    // Errors from setAttachment()
    public static final int OK = 0;
    public static final int UNKNOWN_ERROR = -1;
    public static final int MESSAGE_SIZE_EXCEEDED = -2;
    public static final int UNSUPPORTED_TYPE = -3;
    public static final int IMAGE_TOO_LARGE = -4;

    // Attachment types
    public static final int TEXT = 0;
    public static final int IMAGE = 1;
    public static final int VIDEO = 2;
    public static final int AUDIO = 3;
    public static final int SLIDESHOW = 4;
    public static final int ATTACHMENT = 5;
    public static final int VCARD = 6; 

    // Current attachment type of the message; one of the above values.
    private int mAttachmentType;

    // Conversation this message is targeting.
    private Conversation mConversation;

    // Text of the message.
    private CharSequence mText;
    // Slideshow for this message, if applicable.  If it's a simple attachment,
    // i.e. not SLIDESHOW, it will contain only one slide.
    private SlideshowModel mSlideshow;
    // Data URI of an MMS message if we have had to save it.
    private Uri mMessageUri;
    // MMS subject line for this message
    private CharSequence mSubject;

    // Set to true if this message has been discarded.
    private boolean mDiscarded = false;

    // Track whether we have drafts
    private volatile boolean mHasMmsDraft;
    private volatile boolean mHasSmsDraft;
    
    //Gionee guoyx 20121023 by CR00705464 for MTK ALPS00270539 BEGIN
    // mms draft edit lock. at any time, only one thread can modify a mms draft.
    // here currently use a static lock is ok, because WorkingMessage is only one at any time.
    // if the condition is changed this must be changed too. this is added to fix bug 270539
    public static Object sDraftMmsLock = new Object();
    //Gionee guoyx 20121023 by CR00705464 for MTK ALPS00270539 END
    
    // gionee zhouyj 2012-04-23 add for CR00573937 start
    private boolean mInComposePage = true;
    // gionee zhouyj 2012-04-23 add for CR00573937 end

    // Cached value of mms enabled flag
    private static boolean sMmsEnabled = MmsConfig.getMmsEnabled();

    // Our callback interface
    private final MessageStatusListener mStatusListener;
    private List<String> mWorkingRecipients;

    //gionee gaoj 2012-4-10 added for CR00555790 start
    private boolean mIsDraftDirty = false;
    public String mRepeatSendReceiver;
    long mMmsDraftThreadId = -1L;
    private boolean mIsRepeatSend = false;
    //gionee gaoj 2012-4-10 added for CR00555790 end
    // gionee zhouyj 2012-07-12 add for CR00643113 start 
//    private static long mRecentThreadId = -1;
    // gionee zhouyj 2012-07-12 add for CR00643113 end 
// m0
    // Message sizes in Outbox
/*
    private static final String[] MMS_OUTBOX_PROJECTION = {
        Mms._ID,            // 0
        Mms.MESSAGE_SIZE    // 1
    };
*/
    //gionee gaoj 2012-8-14 added for CR00623375 start
    private boolean mIsRegularly = false;
    private long mRegularlyTime = -1;
    //gionee gaoj 2012-8-14 added for CR00623375 end

    private static final String[] MMS_OUTBOX_PROJECTION = {
        Mms._ID,            // 0
        Mms.MESSAGE_SIZE,   // 1
        Mms.STATUS
    };
// m1

    private static final int MMS_MESSAGE_SIZE_INDEX  = 1;

    /**
     * Callback interface for communicating important state changes back to
     * ComposeMessageActivity.
     */
    public interface MessageStatusListener {
        /**
         * Called when the protocol for sending the message changes from SMS
         * to MMS, and vice versa.
         *
         * @param mms If true, it changed to MMS.  If false, to SMS.
         */
// m0
/*
        void onProtocolChanged(boolean mms);
*/
        void onProtocolChanged(boolean mms, boolean needToast);
// m1

        /**
         * Called when an attachment on the message has changed.
         */
        void onAttachmentChanged();

        /**
         * Called just before the process of sending a mms.
         */
        void onPreMmsSent();

        /**
         * Called just before the process of sending a message.
         */
        void onPreMessageSent();

        /**
         * Called once the process of sending a message, triggered by
         * {@link send} has completed. This doesn't mean the send succeeded,
         * just that it has been dispatched to the network.
         */
        void onMessageSent();

        /**
         * Called if there are too many unsent messages in the queue and we're not allowing
         * any more Mms's to be sent.
         */
        void onMaxPendingMessagesReached();

        /**
         * Called if there's an attachment error while resizing the images just before sending.
         */
        void onAttachmentError(int error);
    }

    private WorkingMessage(ComposeMessageActivity activity) {
        mActivity = activity;
        mContentResolver = mActivity.getContentResolver();
        mStatusListener = activity;
        mAttachmentType = TEXT;
        mText = "";
// a0
        updateCreationMode(activity);
// a1
    }
    //gionee gaoj 2012-3-22 added for CR00555790 start
    private WorkingMessage(AuroraActivity activity) {
        mActivity = activity;
        mContentResolver = mActivity.getContentResolver();
        mStatusListener = (MessageStatusListener) activity;
        mAttachmentType = TEXT;
        mText = "";
//        sCreationMode = checkCreationMode(activity);
        updateCreationMode(activity);        
    }
    public static WorkingMessage createEmpty(AuroraActivity activity) {
        WorkingMessage msg = new WorkingMessage(activity);
        return msg;
    }
    //gionee gaoj 2012-3-22 added for CR00555790 end

    /**
     * Creates a new working message.
     */
    public static WorkingMessage createEmpty(ComposeMessageActivity activity) {
        // Make a new empty working message.
        WorkingMessage msg = new WorkingMessage(activity);
        return msg;
    }

    public int getMessageClassMini() {
        return sMessageClassMini;
    }
    public void setMessageClassMini() {
        sMessageClassMini = CLASSNUMBERMINI;
    }
    /**
     * Create a new WorkingMessage from the specified data URI, which typically
     * contains an MMS message.
     */
    public static WorkingMessage load(ComposeMessageActivity activity, Uri uri) {
        // If the message is not already in the draft box, move it there.
        if (!uri.toString().startsWith(Mms.Draft.CONTENT_URI.toString())) {
            PduPersister persister = PduPersister.getPduPersister(activity);
            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                LogTag.debug("load: moving %s to drafts", uri);
            }
            try {
                uri = persister.move(uri, Mms.Draft.CONTENT_URI);
            } catch (MmsException e) {
                LogTag.error("Can't move %s to drafts", uri);
                return null;
            }
        }

        WorkingMessage msg = new WorkingMessage(activity);
        msg.setConversation(activity.getConversation());
        if (msg.loadFromUri(uri)) {
            return msg;
        }

        return null;
    }
    
    public void setHasMmsDraft(boolean hasMmsDraft) {
        mHasMmsDraft = hasMmsDraft;
    }

    private void correctAttachmentState() {
        int slideCount = mSlideshow.size();
        // for vcard
        final int fileAttachCount = mSlideshow.sizeOfFilesAttach();

        // If we get an empty slideshow, tear down all MMS
        // state and discard the unnecessary message Uri.
        if (fileAttachCount == 0) {
            if (slideCount == 0) {
                removeAttachment(false);
            } else if (slideCount > 1) {
                mAttachmentType = SLIDESHOW;
            } else {
                SlideModel slide = mSlideshow.get(0);
                if (slide.hasImage()) {
                    mAttachmentType = IMAGE;
                } else if (slide.hasVideo()) {
                    mAttachmentType = VIDEO;
                } else if (slide.hasAudio()) {
                    mAttachmentType = AUDIO;
                }
            }
        } else {
            mAttachmentType = ATTACHMENT;
        }

        updateState(HAS_ATTACHMENT, hasAttachment(), false);
    }

    private boolean loadFromUri(Uri uri) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) LogTag.debug("loadFromUri %s", uri);
        try {
            mSlideshow = SlideshowModel.createFromMessageUri(mActivity, uri);
// a0
            mHasDrmPart = mSlideshow.checkDrmContent();
            mHasDrmRight = mSlideshow.checkDrmRight();
// a1
        } catch (MmsException e) {
            LogTag.error("Couldn't load URI %s", uri);
            return false;
        }

        mMessageUri = uri;

        // Make sure all our state is as expected.
        syncTextFromSlideshow();
        correctAttachmentState();

        return true;
    }

    /**
     * Load the draft message for the specified conversation, or a new empty message if
     * none exists.
     */
    public static WorkingMessage loadDraft(ComposeMessageActivity activity,
                                           Conversation conv) {
        WorkingMessage msg = new WorkingMessage(activity);
        if (msg.loadFromConversation(conv)) {
            return msg;
        } else {
            return createEmpty(activity);
        }
    }
    
    //Gionee <guoyx> <2013-04-20> add by 4.2 mtk code CR00797658 begin
    /**
     * Load the draft message for the specified conversation, or a new empty message if
     * none exists.
     */
    public static WorkingMessage loadDraft(ComposeMessageActivity activity,
                                           final Conversation conv,
                                           final Runnable onDraftLoaded) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) LogTag.debug("loadDraft %s", conv);

        final WorkingMessage msg = createEmpty(activity);
        /// M: Code analyze 037, For bug ALPS00291328,  save conversation to avoid JE . @{
        msg.setConversation(conv);
        /// @}
        if (conv.getThreadId() <= 0) {
            if (onDraftLoaded != null) {
                onDraftLoaded.run();
            }
            return msg;
        }

    new AsyncTask<Void, Void, Pair<String, String>>() {

            // Return a Pair where:
            //    first - non-empty String representing the text of an SMS draft
            //    second - non-null String representing the text of an MMS subject
            @Override
            protected Pair<String, String> doInBackground(Void... none) {
                // Look for an SMS draft first.
                String draftText = msg.readDraftSmsMessage(conv);
                String subject = null;

                if (TextUtils.isEmpty(draftText)) {
                    // No SMS draft so look for an MMS draft.
                    StringBuilder sb = new StringBuilder();
                    Uri uri = readDraftMmsMessage(msg.mActivity, conv, sb);
                    if (uri != null) {
                        if (msg.loadFromUri(uri)) {
                            // If there was an MMS message, readDraftMmsMessage
                            // will put the subject in our supplied StringBuilder.
                            subject = sb.toString();
                        }
                    }
                }
                Pair<String, String> result = new Pair<String, String>(draftText, subject);
                return result;
            }

            @Override
            protected void onPostExecute(Pair<String, String> result) {
                if (!TextUtils.isEmpty(result.first)) {
                    msg.mHasSmsDraft = true;
                    msg.setText(result.first);
                }
                if (result.second != null) {
                    msg.mHasMmsDraft = true;
                    if (!TextUtils.isEmpty(result.second)) {
                        msg.setSubject(result.second, false);
                    }
                }
                if (onDraftLoaded != null) {
                    onDraftLoaded.run();
                }
            }
        }.execute();

        return msg;
    }
  //Gionee <guoyx> <2013-04-20> add by 4.2 mtk code CR00797658 end

    private boolean loadFromConversation(Conversation conv) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) LogTag.debug("loadFromConversation %s", conv);

        long threadId = conv.getThreadId();
        if (threadId <= 0) {
            return false;
        }

        // Look for an SMS draft first.
        mText = readDraftSmsMessage(conv);
        if (!TextUtils.isEmpty(mText)) {
            mHasSmsDraft = true;
            return true;
        }

        // Then look for an MMS draft.
        StringBuilder sb = new StringBuilder();
        Uri uri = readDraftMmsMessage(mActivity, conv, sb);
        if (uri != null) {
            if (loadFromUri(uri)) {
                // If there was an MMS message, readDraftMmsMessage
                // will put the subject in our supplied StringBuilder.
                if (sb.length() > 0) {
                    setSubject(sb.toString(), false);
                }
                //gionee gaoj 2012-4-10 added for CR00555790 start
                if (MmsApp.mGnMessageSupport) {
                    mMmsDraftThreadId = conv.getThreadId();
                }
                //gionee gaoj 2012-4-10 added for CR00555790 end
                mHasMmsDraft = true;
                return true;
            }
        }

        return false;
    }

    /**
     * Sets the text of the message to the specified CharSequence.
     */
    public void setText(CharSequence s) {
        mText = s;
// a0
        if (mText != null && TextUtils.getTrimmedLength(mText) >= 0) {
            syncTextToSlideshow();
        }
// a1
        // gionee lwzh add for CR00774362 20130227 begin
        if (mText == null) {
            mText = "";
        }
        // gionee lwzh add for CR00774362 20130227 end
    }

    /**
     * Returns the current message text.
     */
    public CharSequence getText() {
        return mText;
    }

    /**
     * Returns true if the message has any text. A message with just whitespace is not considered
     * to have text.
     * @return
     */
    public boolean hasText() {
        // gionee zhouyj 2012-05-21 add for CR00601523 start
        if(MmsApp.mGnMessageSupport && ((ComposeMessageActivity)mActivity).isSignatureEnable()) {
            return mText != null && TextUtils.getTrimmedLength(mText) > 0 && 
                !((ComposeMessageActivity)mActivity).getSignatureContent().trim().equals(mText.toString().trim());
        }
        // gionee zhouyj 2012-05-21 add for CR00601523 end
        return mText != null && TextUtils.getTrimmedLength(mText) > 0;
    }

    public void removeAttachment(boolean notify) {
        mAttachmentType = TEXT;
        mSlideshow = null;
        if (mMessageUri != null) {
            asyncDelete(mMessageUri, null, null);
            mMessageUri = null;
        }
        // mark this message as no longer having an attachment
        updateState(HAS_ATTACHMENT, false, notify);
        if (notify) {
            // Tell ComposeMessageActivity (or other listener) that the attachment has changed.
            // In the case of ComposeMessageActivity, it will remove its attachment panel because
            // this working message no longer has an attachment.
            mStatusListener.onAttachmentChanged();
        }
        //a0
        //ginoee gaoj 2012-9-21 modified for CR00698953 start
        if (mConversation != null) {
            clearConversation(mConversation, true);
        }
        //ginoee gaoj 2012-9-21 modified for CR00698953 end
        //a1
    }
    
    //Gionee <guoyx> <2013-07-10> modify for CR00832990 begin
    // gionee zhouyj 2012-06-29 add for CR00627870 start 
    public void removeSubject() {
//        updateState(HAS_SUBJECT, false, false);
        if(requiresMms()) {
//            asyncUpdateDraftMmsMessage(mConversation,true);
            mHasMmsDraft = true;
            mConversation.setDraftState(true);
        } else {
            if (mMessageUri != null) {
                asyncDelete(mMessageUri, null, null);
                mMessageUri = null;
            }
        }
    }
    // gionee zhouyj 2012-06-29 add for CR00627870 end 
    //Gionee <guoyx> <2013-07-10> modify for CR00832990 end

    private String mMimeType = "";
    public int setAttachment(int type,Uri dataUri, boolean append, String mimeType)
    {
        mMimeType = mimeType;
        return this.setAttachment(type, dataUri, append);
    }
    /**
     * Adds an attachment to the message, replacing an old one if it existed.
     * @param type Type of this attachment, such as {@link IMAGE}
     * @param dataUri Uri containing the attachment data (or null for {@link TEXT})
     * @param append true if we should add the attachment to a new slide
     * @return An error code such as {@link UNKNOWN_ERROR} or {@link OK} if successful
     */
    public int setAttachment(int type, Uri dataUri, boolean append) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("setAttachment type=%d uri %s", type, dataUri);
        }
        int result = OK;

        // Special case for deleting a slideshow. When ComposeMessageActivity gets told to
        // remove an attachment (search for AttachmentEditor.MSG_REMOVE_ATTACHMENT), it calls
        // this function setAttachment with a type of TEXT and a null uri. Basically, it's turning
        // the working message from an MMS back to a simple SMS. The various attachment types
        // use slide[0] as a special case. The call to ensureSlideshow below makes sure there's
        // a slide zero. In the case of an already attached slideshow, ensureSlideshow will do
        // nothing and the slideshow will remain such that if a user adds a slideshow again, they'll
        // see their old slideshow they previously deleted. Here we really delete the slideshow.
        if (type == TEXT && mAttachmentType == SLIDESHOW && mSlideshow != null && dataUri == null
                && !append) {
            SlideshowEditor slideShowEditor = new SlideshowEditor(mActivity, mSlideshow);
            slideShowEditor.removeAllSlides();
        }

        if (mSlideshow == null) {// This is added for failed to share only one picture with message.
            append = true;
        }

        // Make sure mSlideshow is set up and has a slide.
        ensureSlideshow();

        // Change the attachment and translate the various underlying
        // exceptions into useful error codes.
        try {
            if (type >= ATTACHMENT) {
                if (mSlideshow == null) {
                    mSlideshow = SlideshowModel.createNew(mActivity);
                }
                setOrAppendFileAttachment(type, dataUri, append);
            } else {
                if (append) {
                    appendMedia(type, dataUri);
                } else {
                    changeMedia(type, dataUri);
                }
                //gionee gaoj 2012-4-10 added for CR00555790 start
                if (MmsApp.mGnMessageSupport) {
                    mIsDraftDirty = true;
                }
                //gionee gaoj 2012-4-10 added for CR00555790 end
            }
        } catch (MmsException e) {
            Log.e(TAG,e.getMessage());
            result = UNKNOWN_ERROR;
        } catch (UnsupportContentTypeException e) {
            result = UNSUPPORTED_TYPE;
        } catch (ExceedMessageSizeException e) {
            result = MESSAGE_SIZE_EXCEEDED;
        } catch (ResolutionException e) {
            result = IMAGE_TOO_LARGE;
// a0
        } catch (ContentRestrictionException e){
            result = sCreationMode;
        } catch (RestrictedResolutionException e){
            result = RESTRICTED_RESOLUTION;
        } catch (IllegalStateException e) {
            Log.e(TAG,e.getMessage());
            result = UNKNOWN_ERROR;
// a1
        } catch (IllegalArgumentException e) {
            Log.e(TAG,e.getMessage());
            result = UNKNOWN_ERROR;
        }

        // If we were successful, update mAttachmentType and notify
        // the listener than there was a change.
        if (result == OK) {
            mAttachmentType = type;
// a0
            if (mSlideshow == null){
                return UNKNOWN_ERROR;
            }
            if(mSlideshow.size() > 1) {
                mAttachmentType = SLIDESHOW;
            }
// a1
        } else if (append) {
            // We added a new slide and what we attempted to insert on the slide failed.
            // Delete that slide, otherwise we could end up with a bunch of blank slides.
            SlideshowEditor slideShowEditor = new SlideshowEditor(mActivity, mSlideshow);
// a0
            if (slideShowEditor == null || mSlideshow == null){
                return UNKNOWN_ERROR;
            }
// a1
            slideShowEditor.removeSlide(mSlideshow.size() - 1);
        }
        mStatusListener.onAttachmentChanged();  // have to call whether succeeded or failed,
                                                // because a replace that fails, removes the slide

        if (!MmsConfig.getMultipartSmsEnabled()) {
            if (!append && mAttachmentType == TEXT && type == TEXT) {
                int[] params = SmsMessage.calculateLength(getText(), false);
                /* SmsMessage.calculateLength returns an int[4] with:
                *   int[0] being the number of SMS's required,
                *   int[1] the number of code units used,
                *   int[2] is the number of code units remaining until the next message.
                *   int[3] is the encoding type that should be used for the message.
                */
                int msgCount = params[0];

                if (msgCount >= MmsConfig.getSmsToMmsTextThreshold()) {
                    setLengthRequiresMms(true, false);
                } else {
                    updateState(HAS_ATTACHMENT, hasAttachment(), true);
                }
            } else {
                updateState(HAS_ATTACHMENT, hasAttachment(), true);
            }
        } else {
            // Set HAS_ATTACHMENT if we need it.
            updateState(HAS_ATTACHMENT, hasAttachment(), true);
        }
        correctAttachmentState();
        return result;
    }

    /**
     * Returns true if this message contains anything worth saving.
     */
    public boolean isWorthSaving() {
        // If it actually contains anything, it's of course not empty.
        if (hasText() || hasSubject() || hasAttachment() || hasSlideshow()) {
            return true;
        }
        // When saveAsMms() has been called, we set FORCE_MMS to represent
        // sort of an "invisible attachment" so that the message isn't thrown
        // away when we are shipping it off to other activities.
        if (isFakeMmsForDraft()) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if FORCE_MMS is set.
     * When saveAsMms() has been called, we set FORCE_MMS to represent
     * sort of an "invisible attachment" so that the message isn't thrown
     * away when we are shipping it off to other activities.
     */
    public boolean isFakeMmsForDraft() {
        return (mMmsState & FORCE_MMS) > 0;
    }

    /**
     * Makes sure mSlideshow is set up.
     */
    private void ensureSlideshow() {
        if (mSlideshow != null) {
            if (mSlideshow.size() > 0) {
                return;
            } else {
                mSlideshow.add(new SlideModel(mSlideshow));
                return;
            }
        }

        SlideshowModel slideshow = SlideshowModel.createNew(mActivity);
        SlideModel slide = new SlideModel(slideshow);
        slideshow.add(slide);

        mSlideshow = slideshow;
// a0
        mHasDrmPart = mSlideshow.checkDrmContent();
        mHasDrmRight = mSlideshow.checkDrmRight();
// a1
    }

    /**
     * Change the message's attachment to the data in the specified Uri.
     * Used only for single-slide ("attachment mode") messages.
     */
    private void changeMedia(int type, Uri uri) throws MmsException {
        SlideModel slide = mSlideshow.get(0);
        MediaModel media;

        if (slide == null) {
            Log.w(LogTag.TAG, "[WorkingMessage] changeMedia: no slides!");
            return;
        }

        // If we're changing to text, just bail out.
        if (type == TEXT) {
// a0
            if(mSlideshow != null && mSlideshow.size() > 0) {
                // for vcard
                mSlideshow.removeAllAttachFiles();
                mSlideshow.clear();
                mSlideshow = null;
                ensureSlideshow();
                if(ismResizeImage()) {
                    // Delete our MMS message, if there is one.
                    if (mMessageUri != null) {
                        asyncDelete(mMessageUri, null, null);
                        setmResizeImage(false);
                    }
                }
            }
// a1
            return;
        }
// a0
        mHasDrmPart = false;
        mHasDrmRight = false;
// a1
        // Make a correct MediaModel for the type of attachment.
        if (type == IMAGE) {
            media = new ImageModel(mActivity, uri, mSlideshow.getLayout().getImageRegion(), mMimeType);
// a0
            mHasDrmPart = ((ImageModel)media).hasDrmContent();
            mHasDrmRight = ((ImageModel)media).hasDrmRight();
// a1
        } else if (type == VIDEO) {
            media = new VideoModel(mActivity, uri, mSlideshow.getLayout().getImageRegion());
// a0
            mHasDrmPart = ((VideoModel)media).hasDrmContent();
            mHasDrmRight = ((VideoModel)media).hasDrmRight();
// a1
        } else if (type == AUDIO) {
            media = new AudioModel(mActivity, uri);
// a0
            mHasDrmPart = ((AudioModel)media).hasDrmContent();
            mHasDrmRight = ((AudioModel)media).hasDrmRight();
// a1
        } else {
            throw new IllegalArgumentException("changeMedia type=" + type + ", uri=" + uri);
        }
// a0
        if (media.getMediaPackagedSize() < 0) {
            mHasDrmPart = false;
            throw new ExceedMessageSizeException("Exceed message size limitation");
        }
        if (media.getMediaPackagedSize() > mSlideshow.getCurrentSlideshowSize()){            
            mSlideshow.checkMessageSize(media.getMediaPackagedSize() + SlideshowModel.mReserveSize -mSlideshow.getCurrentSlideshowSize());
        }
// a1

        // Remove any previous attachments.
        removeSlideAttachments(slide);

        // Add it to the slide.
// m0
        //slide.add(media);

        if (slide.add(media) == false) {
            mHasDrmPart = false;
        }
// m1
        // For video and audio, set the duration of the slide to
        // that of the attachment.
        if (type == VIDEO || type == AUDIO) {
            slide.updateDuration(media.getDuration());
        }
    }

    private void removeSlideAttachments(SlideModel slide) {
        slide.removeImage();
        slide.removeVideo();
        slide.removeAudio();
        if (mSlideshow != null) {
            // for vcard
            mSlideshow.removeAllAttachFiles();
        }
    }

    /**
     * Add the message's attachment to the data in the specified Uri to a new slide.
     */
    private void appendMedia(int type, Uri uri) throws MmsException {

        // If we're changing to text, just bail out.
        if (type == TEXT) {
            return;
        }

        // The first time this method is called, mSlideshow.size() is going to be
        // one (a newly initialized slideshow has one empty slide). The first time we
        // attach the picture/video to that first empty slide. From then on when this
        // function is called, we've got to create a new slide and add the picture/video
        // to that new slide.
        boolean addNewSlide = true;
        if (mSlideshow.size() == 1 && !mSlideshow.isSimple()) {
            addNewSlide = false;
        }
        if (addNewSlide) {
            SlideshowEditor slideShowEditor = new SlideshowEditor(mActivity, mSlideshow);
            if (!slideShowEditor.addNewSlide()) {
                return;
            }
        }
        // Make a correct MediaModel for the type of attachment.
        MediaModel media;
        SlideModel slide = mSlideshow.get(mSlideshow.size() - 1);
        if (type == IMAGE) {
            media = new ImageModel(mActivity, uri, mSlideshow.getLayout().getImageRegion(),mMimeType);
// a0
            mHasDrmPart = ((ImageModel)media).hasDrmContent();
            mHasDrmRight = ((ImageModel)media).hasDrmRight();
            //ALPS00289861
            String[] fileNames = mSlideshow.getAllMediaNames(MediaType.IMAGE);
            media.setSrc(MessageUtils.getUniqueName(fileNames, media.getSrc()));
            
// a1
        } else if (type == VIDEO) {
            media = new VideoModel(mActivity, uri, mSlideshow.getLayout().getImageRegion());
// a0
            mHasDrmPart = ((VideoModel)media).hasDrmContent();
            mHasDrmRight = ((VideoModel)media).hasDrmRight();
            //ALPS00289861
            String[] fileNames = mSlideshow.getAllMediaNames(MediaType.VIDEO);
            media.setSrc(MessageUtils.getUniqueName(fileNames, media.getSrc()));
// a1
        } else if (type == AUDIO) {
            media = new AudioModel(mActivity, uri);
// a0
            mHasDrmPart = ((AudioModel)media).hasDrmContent();
            mHasDrmRight = ((AudioModel)media).hasDrmRight();
            //ALPS00289861
            String[] fileNames = mSlideshow.getAllMediaNames(MediaType.AUDIO);
            media.setSrc(MessageUtils.getUniqueName(fileNames, media.getSrc()));
// a1
        } else {
            throw new IllegalArgumentException("changeMedia type=" + type + ", uri=" + uri);
        }
// a0
        if (media.getMediaPackagedSize() < 0) {
            mHasDrmPart = false;
            throw new ExceedMessageSizeException("Exceed message size limitation");
        }
// a1
        // Add it to the slide.
// m0
        //slide.add(media);
        if (slide.add(media) == false) {
            mHasDrmPart = false;
        }
// m1
        // for vcard, since we append a media, remove vCard
        removeAllFileAttaches();

        // For video and audio, set the duration of the slide to
        // that of the attachment.
        if (type == VIDEO || type == AUDIO) {
            slide.updateDuration(media.getDuration());
        }
    }
    
    /**
     * When attaching vCard or vCalendar, we generated a *.vcs or *.vcf file locally.
     * After calling PduPersister.persist(), the files are stored into PduPart and 
     * these file generated become redundant and useless. Therefore, need to delete 
     * them after persisting.
     */
    private void deleteFileAttachmentTempFile() {
        // delete all legacy vcard files
        final String[] filenames = mActivity.fileList();
        for (String file : filenames) {
            if (file.endsWith(".vcf")) {
                if (!mActivity.deleteFile(file)) {
                    Log.d(TAG, "delete temp file, cannot delete file '" + file + "'");
                } else {
                    Log.d(TAG, "delete temp file, deleted file '" + file + "'");
                }
            }
        }
    }

    public void removeAllFileAttaches() {
        if (mSlideshow != null) {
            mSlideshow.removeAllAttachFiles();
        }
    }

    public boolean hasMediaAttachments() {
        if (mSlideshow == null) {
            return false;
        }
        if (hasSlideshow()) {
            return true;
        }
        final SlideModel slide = mSlideshow.get(0);
        return (slide != null) && (slide.hasAudio() || slide.hasVideo() || slide.hasImage());
    }

    /**
     * Add file attachments such as VCard etc into Slideshow.
     */
    private void setOrAppendFileAttachment(int type, Uri uri, boolean append) throws MmsException {
        FileAttachmentModel fileAttach = null;
        if (type == VCARD) {
            Log.i(TAG, "WorkingMessage.setOrAppendFileAttachment(): for vcard " + uri);
            fileAttach = new VCardModel(mActivity.getApplication(), uri);
        } else {
            throw new IllegalArgumentException("setOrAppendFileAttachment type=" + type + ", uri=" + uri);
        }
        if (fileAttach == null) {
            throw new IllegalStateException("setOrAppendFileAttachment failedto create FileAttachmentModel " 
                    + type + " uri = " + uri);
        }

        mSlideshow.checkAttachmentSize(fileAttach.getAttachSize(), append);

        // Remove any previous attachments.
        // NOTE: here cannot use clear(), it will remove the text too.
        SlideModel slide = mSlideshow.get(0);
        slide.removeImage();
        slide.removeVideo();
        slide.removeAudio();
        int size = mSlideshow.size();
        for (int i = size-1; i >= 1; i--) {
            mSlideshow.remove(i);
        }

        // Add file attachments
        if (append) {
            mSlideshow.addFileAttachment(fileAttach);
        } else {
            // reset file attachment, so that this is the only one
            mSlideshow.removeAllAttachFiles();
            mSlideshow.addFileAttachment(fileAttach);
        }
    }

    /**
     * Returns true if the message has an attachment (including slideshows).
     */
    public boolean hasAttachment() {
        return (mAttachmentType > TEXT);
    }

    public boolean hasAttachedFiles() {
        return mSlideshow != null && mSlideshow.sizeOfFilesAttach() > 0;
    }

    /**
     * Returns the slideshow associated with this message.
     */
    public SlideshowModel getSlideshow() {
        return mSlideshow;
    }

    /**
     * Returns true if the message has a real slideshow, as opposed to just
     * one image attachment, for example.
     */
    public boolean hasSlideshow() {
        return (mSlideshow != null && mSlideshow.size() > 1);
    }

    /**
     * Sets the MMS subject of the message.  Passing null indicates that there
     * is no subject.  Passing "" will result in an empty subject being added
     * to the message, possibly triggering a conversion to MMS.  This extra
     * bit of state is needed to support ComposeMessageActivity converting to
     * MMS when the user adds a subject.  An empty subject will be removed
     * before saving to disk or sending, however.
     */
    public void setSubject(CharSequence s, boolean notify) {
// a0
        boolean flag = ((s != null) && (s.length()>0));
        if (flag) {
// a1
            mSubject = s;
            updateState(HAS_SUBJECT, flag, notify);
// a0
        } else {
            updateState(HAS_SUBJECT, flag, notify);
        }
// a1
    }

    /**
     * Returns the MMS subject of the message.
     */
    public CharSequence getSubject() {
        return mSubject;
    }

    /**
     * Returns true if this message has an MMS subject. A subject has to be more than just
     * whitespace.
     * @return
     */
    public boolean hasSubject() {
        return mSubject != null && TextUtils.getTrimmedLength(mSubject) > 0;
    }

    /**
     * Moves the message text into the slideshow.  Should be called any time
     * the message is about to be sent or written to disk.
     */
    private void syncTextToSlideshow() {
        if (mSlideshow == null || mSlideshow.size() != 1)
            return;

        SlideModel slide = mSlideshow.get(0);
        TextModel text;
// m0
/*
        if (!slide.hasText()) {
            // Add a TextModel to slide 0 if one doesn't already exist
            text = new TextModel(mActivity, ContentType.TEXT_PLAIN, "text_0.txt",
                                           mSlideshow.getLayout().getTextRegion());
            slide.add(text);
        } else {
            // Otherwise just reuse the existing one.
            text = slide.getText();
        }
        text.setText(mText);
*/
        // Add a TextModel to slide 0 if one doesn't already exist
        text = new TextModel(mActivity, ContentType.TEXT_PLAIN, "text_0.txt",
                                           mSlideshow.getLayout().getTextRegion(),
                                           TextUtils.getTrimmedLength(mText)>=0 ? (mText.toString()).getBytes() : null);
        try {
            //klocwork issue pid:18444
            if (slide != null) {
                slide.add(text);
            }
        } catch (ExceedMessageSizeException e) {
            return;
        }
// m1
    }

    /**
     * Sets the message text out of the slideshow.  Should be called any time
     * a slideshow is loaded from disk.
     */
    private void syncTextFromSlideshow() {
        // Don't sync text for real slideshows.
        if (mSlideshow.size() != 1) {
            return;
        }

        SlideModel slide = mSlideshow.get(0);
        if (slide == null || !slide.hasText()) {
            return;
        }

        mText = slide.getText().getText();
    }

    /**
     * Removes the subject if it is empty, possibly converting back to SMS.
     */
    private void removeSubjectIfEmpty(boolean notify) {
        if (!hasSubject()) {
            setSubject(null, notify);
        }
    }

    /**
     * Gets internal message state ready for storage.  Should be called any
     * time the message is about to be sent or written to disk.
     */
    private void prepareForSave(boolean notify) {
        // Make sure our working set of recipients is resolved
        // to first-class Contact objects before we save.
        syncWorkingRecipients();

        if (requiresMms()) {
            ensureSlideshow();
            syncTextToSlideshow();
// a0
            removeSubjectIfEmpty(notify);
// a1
        }
    }

    /**
     * Resolve the temporary working set of recipients to a ContactList.
     */
    public void syncWorkingRecipients() {
        if (mWorkingRecipients != null) {
            ContactList recipients = ContactList.getByNumbers(mWorkingRecipients, false);
            mConversation.setRecipients(recipients);    // resets the threadId to zero
            mWorkingRecipients = null;
        }
    }

    public String getWorkingRecipients() {
        // this function is used for DEBUG only
        if (mWorkingRecipients == null) {
            return null;
        }
        ContactList recipients = ContactList.getByNumbers(mWorkingRecipients, false);
        return recipients.serialize();
    }

    // Call when we've returned from adding an attachment. We're no longer forcing the message
    // into a Mms message. At this point we either have the goods to make the message a Mms
    // or we don't. No longer fake it.
    public void removeFakeMmsForDraft() {
        updateState(FORCE_MMS, false, false);
    }

    /**
     * Force the message to be saved as MMS and return the Uri of the message.
     * Typically used when handing a message off to another activity.
     */
    public synchronized Uri saveAsMms(boolean notify) {
        if (DEBUG) LogTag.debug("saveAsMms mConversation=%s", mConversation);

        // If we have discarded the message, just bail out.
        if (mDiscarded) {
            LogTag.warn("saveAsMms mDiscarded: true mConversation: " + mConversation +
                    " returning NULL uri and bailing");
            return null;
        }

        // FORCE_MMS behaves as sort of an "invisible attachment", making
        // the message seem non-empty (and thus not discarded).  This bit
        // is sticky until the last other MMS bit is removed, at which
        // point the message will fall back to SMS.
        updateState(FORCE_MMS, true, notify);

        // Collect our state to be written to disk.
        prepareForSave(true /* notify */);

        try {
            //gionee gaoj 2012-4-10 added for CR00555790 start //CR00788343
            if (MmsApp.mGnMessageSupport && mConversation.getMessageCount() == 0 && MmsApp.mIsDraftOpen) {
                mIsDraftDirty = true;
            } else {
                //gionee gaoj 2012-4-10 added for CR00555790 end
            // Make sure we are saving to the correct thread ID.
            DraftCache.getInstance().setSavingDraft(true);
            //gionee gaoj 2013-4-8 modified for CR00788343 start
            if (!mConversation.getRecipients().isEmpty()) {
                mConversation.ensureThreadId();
            }
            //gionee gaoj 2013-4-8 modified for CR00788343 end
            mConversation.setDraftState(true);
            //gionee gaoj 2012-4-10 added for CR00555790 start
            }
            //gionee gaoj 2012-4-10 added for CR00555790 end

            PduPersister persister = PduPersister.getPduPersister(mActivity);
            SendReq sendReq = makeSendReq(mConversation, mSubject);

            //gionee gaoj 2012-4-10 added for CR00555790 start //CR00788343
            if (MmsApp.mGnMessageSupport && mConversation.getMessageCount() == 0 && MmsApp.mIsDraftOpen) {
                EncodedStringValue val = new EncodedStringValue("gn_draft_address_token");
                sendReq.addTo(val);
            }
            //gionee gaoj 2012-4-10 added for CR00555790 end
            // If we don't already have a Uri lying around, make a new one.  If we do
            // have one already, make sure it is synced to disk.
            if (mMessageUri == null) {
                mMessageUri = createDraftMmsMessage(persister, sendReq, mSlideshow);
            } else {
                updateDraftMmsMessage(mMessageUri, persister, mSlideshow, sendReq);
            }
            mHasMmsDraft = true;
        } finally {
            DraftCache.getInstance().setSavingDraft(false);
        }
        return mMessageUri;
    }

    /**
     * Save this message as a draft in the conversation previously specified
     * to {@link setConversation}.
     */
    public void saveDraft(final boolean isStopping) {
        // If we have discarded the message, just bail out.
        if (mDiscarded) {
            LogTag.warn("saveDraft mDiscarded: true mConversation: " + mConversation +
                " skipping saving draft and bailing");
            return;
        }

        // Make sure setConversation was called.
        if (mConversation == null) {
            throw new IllegalStateException("saveDraft() called with no conversation");
        }

        if (LogTag.VERBOSE || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("saveDraft for mConversation " + mConversation);
        }

        // Get ready to write to disk. But don't notify message status when saving draft
        prepareForSave(false /* notify */);

        if (requiresMms()) {
            asyncUpdateDraftMmsMessage(mConversation, isStopping);
            mHasMmsDraft = true;
// a0
            // Update state of the draft cache.
            mConversation.setDraftState(true);
// a1
        } else {
            // gionee lwzh add for CR00774362 20130227 begin
            if (mText == null) {
                return;
            }
            // gionee lwzh add for CR00774362 20130227 end
            
            String content = mText.toString();

            // bug 2169583: don't bother creating a thread id only to delete the thread
            // because the content is empty. When we delete the thread in updateDraftSmsMessage,
            // we didn't nullify conv.mThreadId, causing a temperary situation where conv
            // is holding onto a thread id that isn't in the database. If a new message arrives
            // and takes that thread id (because it's the next thread id to be assigned), the
            // new message will be merged with the draft message thread, causing confusion!
            if (!TextUtils.isEmpty(content)) {
                asyncUpdateDraftSmsMessage(mConversation, content);
                mHasSmsDraft = true;
            } else {
                // When there's no associated text message, we have to handle the case where there
                // might have been a previous mms draft for this message. This can happen when a
                // user turns an mms back into a sms, such as creating an mms draft with a picture,
                // then removing the picture.
                asyncDeleteDraftMmsMessage(mConversation);
                //ALPS00289861
                mMessageUri = null;
            }
        }
// m0
        // Update state of the draft cache.
//        mConversation.setDraftState(true);
// m1
        //gionee gaoj 2012-4-10 added for CR00555790 start //CR00788343
        if (MmsApp.mGnMessageSupport && mConversation.getMessageCount() == 0 && MmsApp.mIsDraftOpen) {
            mIsDraftDirty = true;
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
    }

// a0
    synchronized public void discard() {
        discard(true);
    }
// a1

// m0
//    synchronized public void discard() {
    synchronized public void discard(boolean isDiscard) {
// m1
        if (LogTag.VERBOSE || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("[WorkingMessage] discard");
        }

        if (mDiscarded == true) {
            return;
        }

        // Mark this message as discarded in order to make saveDraft() no-op.
        mDiscarded = isDiscard;

        // Delete any associated drafts if there are any.
        if (mHasMmsDraft) {
            asyncDeleteCurrentDraftMmsMessage(mConversation);
        }
        if (mHasSmsDraft) {
            asyncDeleteDraftSmsMessage(mConversation);
        }
        if (mConversation.getThreadId() > 0) {
            clearConversation(mConversation, true);
        }
    }

    public void unDiscard() {
        if (DEBUG) LogTag.debug("unDiscard");

        mDiscarded = false;
    }

    /**
     * Returns true if discard() has been called on this message.
     */
    public boolean isDiscarded() {
        return mDiscarded;
    }

    /**
     * To be called from our AuroraActivity's onSaveInstanceState() to give us a chance
     * to stow our state away for later retrieval.
     *
     * @param bundle The Bundle passed in to onSaveInstanceState
     */
    public void writeStateToBundle(Bundle bundle) {
        if (hasSubject()) {
            bundle.putString("subject", mSubject.toString());
        }

        if (mMessageUri != null) {
            bundle.putParcelable("msg_uri", mMessageUri);
        } else if (hasText()) {
            bundle.putString("sms_body", mText.toString());
        }
    }

    /**
     * To be called from our AuroraActivity's onCreate() if the activity manager
     * has given it a Bundle to reinflate
     * @param bundle The Bundle passed in to onCreate
     */
    public void readStateFromBundle(Bundle bundle) {
        if (bundle == null) {
            return;
        }

        String subject = bundle.getString("subject");
        setSubject(subject, false);

        Uri uri = (Uri)bundle.getParcelable("msg_uri");
        if (uri != null) {
            loadFromUri(uri);
            return;
        } else {
            String body = bundle.getString("sms_body");
            if( null == body ) {
                mText = "";
            }
            else {
                mText = body;
            }
        }
    }

    /**
     * Update the temporary list of recipients, used when setting up a
     * new conversation.  Will be converted to a ContactList on any
     * save event (send, save draft, etc.)
     */
    public void setWorkingRecipients(List<String> numbers) {
        mWorkingRecipients = numbers;
        String s = null;
        if (numbers != null) {
            int size = numbers.size();
            switch (size) {
            case 1:
                s = numbers.get(0);
                break;
            case 0:
                s = "empty";
                break;
            default:
                s = "{...} len=" + size;
            }
        }
        Log.i(TAG, "setWorkingRecipients: numbers=" + s);
    }

    private void dumpWorkingRecipients() {
        Log.i(TAG, "-- mWorkingRecipients:");

        if (mWorkingRecipients != null) {
            int count = mWorkingRecipients.size();
            for (int i=0; i<count; i++) {
                Log.i(TAG, "   [" + i + "] " + mWorkingRecipients.get(i));
            }
            Log.i(TAG, "");
        }
    }

    public void dump() {
        Log.i(TAG, "WorkingMessage:");
        dumpWorkingRecipients();
        if (mConversation != null) {
            Log.i(TAG, "mConversation: " + mConversation.toString());
        }
    }

    /**
     * Set the conversation associated with this message.
     */
    public void setConversation(Conversation conv) {
        if (DEBUG) LogTag.debug("setConversation %s -> %s", mConversation, conv);

        mConversation = conv;

        // Convert to MMS if there are any email addresses in the recipient list.
        setHasEmail(conv.getRecipients().containsEmail(), false);
    }

    public Conversation getConversation() {
        return mConversation;
    }

    /**
     * Hint whether or not this message will be delivered to an
     * an email address.
     */
    public void setHasEmail(boolean hasEmail, boolean notify) {
// a0
        Log.v(TAG, "WorkingMessage.setHasEmail(" + hasEmail + ", " + notify + ")");
// a1
        if (MmsConfig.getEmailGateway() != null) {
            updateState(RECIPIENTS_REQUIRE_MMS, false, notify);
        } else {
            updateState(RECIPIENTS_REQUIRE_MMS, hasEmail, notify);
        }
    }

    /**
     * Returns true if this message would require MMS to send.
     */
    public boolean requiresMms() {
        return (mMmsState > 0);
    }

    /**
     * Set whether or not we want to send this message via MMS in order to
     * avoid sending an excessive number of concatenated SMS messages.
     * @param: mmsRequired is the value for the LENGTH_REQUIRES_MMS bit.
     * @param: notify Whether or not to notify the user.
     */
    public void setLengthRequiresMms(boolean mmsRequired, boolean notify) {
        updateState(LENGTH_REQUIRES_MMS, mmsRequired, notify);
    }

    private static String stateString(int state) {
        if (state == 0)
            return "<none>";

        StringBuilder sb = new StringBuilder();
        if ((state & RECIPIENTS_REQUIRE_MMS) > 0)
            sb.append("RECIPIENTS_REQUIRE_MMS | ");
        if ((state & HAS_SUBJECT) > 0)
            sb.append("HAS_SUBJECT | ");
        if ((state & HAS_ATTACHMENT) > 0)
            sb.append("HAS_ATTACHMENT | ");
        if ((state & LENGTH_REQUIRES_MMS) > 0)
            sb.append("LENGTH_REQUIRES_MMS | ");
        if ((state & FORCE_MMS) > 0)
            sb.append("FORCE_MMS | ");

        sb.delete(sb.length() - 3, sb.length());
        return sb.toString();
    }
    // Aurora xuyong 2015-10-12 added for bug #16732 start
    private boolean mNeedProcChangeToast = true;
    
    public void setNeedProcChangeToast(boolean show) {
    	mNeedProcChangeToast = show;	
    }
    // Aurora xuyong 2015-10-12 added for bug #16732 end
    /**
     * Sets the current state of our various "MMS required" bits.
     *
     * @param state The bit to change, such as {@link HAS_ATTACHMENT}
     * @param on If true, set it; if false, clear it
     * @param notify Whether or not to notify the user
     */
    private void updateState(int state, boolean on, boolean notify) {
// a0
        Log.v(TAG, "WorkingMessage.updateState(" + state + ", " + on + ", " + notify + ")");
// a1
        if (!sMmsEnabled) {
            // If Mms isn't enabled, the rest of the Messaging UI should not be using any
            // feature that would cause us to to turn on any Mms flag and show the
            // "Converting to multimedia..." message.
            return;
        }
        int oldState = mMmsState;
        if (on) {
            mMmsState |= state;
        } else {
            mMmsState &= ~state;
        }

        // If we are clearing the last bit that is not FORCE_MMS,
        // expire the FORCE_MMS bit.
// m0
        //Gionee <guoyx> <2013-08-02> modify for CR00845227 begin
        if (mMmsState == FORCE_MMS && ((oldState & ~FORCE_MMS) > 0)) {
//        if (mMmsState == FORCE_MMS && ((oldState & ~FORCE_MMS) > 2)) {
// m1
            mMmsState = 0;
            Log.i(TAG, "WorkingMessage.updateState change state to 0 !");
        } else {
            Log.i(TAG, "WorkingMessage.updateState state is " + mMmsState);
        }
        //Gionee <guoyx> <2013-08-02> modify for CR00845227 end

        // Notify the listener if we are moving from SMS to MMS
        // or vice versa.
        if (notify) {
            if (oldState == 0 && mMmsState != 0) {
// m0
//                mStatusListener.onProtocolChanged(true);
                // Aurora xuyong 2015-10-12 modified for bug #16732 start
                mStatusListener.onProtocolChanged(true, mNeedProcChangeToast);
                // Aurora xuyong 2015-10-12 modified for bug #16732 end
// m1
            } else if (oldState != 0 && mMmsState == 0) {
// m0
//                mStatusListener.onProtocolChanged(false);
                // Aurora xuyong 2015-10-12 modified for bug #16732 start
                mStatusListener.onProtocolChanged(false, mNeedProcChangeToast);
                // Aurora xuyong 2015-10-12 modified for bug #16732 end
// m1
            }
        }

        if (oldState != mMmsState) {
            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) LogTag.debug("updateState: %s%s = %s",
                    on ? "+" : "-",
                    stateString(state), stateString(mMmsState));
        }
    }

    /**
     * Send this message over the network.  Will call back with onMessageSent() once
     * it has been dispatched to the telephony stack.  This WorkingMessage object is
     * no longer useful after this method has been called.
     *
     * @throws ContentRestrictionException if sending an MMS and uaProfUrl is not defined
     * in mms_config.xml.
     */
    public void send(final String recipientsInUI) {
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            LogTag.debug("send");
        }
        long origThreadId = mConversation.getThreadId();

        removeSubjectIfEmpty(true /* notify */);

        // Get ready to write to disk.
        prepareForSave(true /* notify */);

        // We need the recipient list for both SMS and MMS.
        final Conversation conv = mConversation;
        String msgTxt = mText.toString();

        if (requiresMms() || addressContainsEmailToMms(conv, msgTxt)) {
            // uaProfUrl setting in mms_config.xml must be present to send an MMS.
            // However, SMS service will still work in the absence of a uaProfUrl address.
            if (MmsConfig.getUaProfUrl() == null) {
                String err = "WorkingMessage.send MMS sending failure. mms_config.xml is " +
                        "missing uaProfUrl setting.  uaProfUrl is required for MMS service, " +
                        "but can be absent for SMS.";
                RuntimeException ex = new ContentRestrictionException(err);
                Log.e(TAG, err, ex);
                // now, let's just crash.
                throw ex;
            }

            // Make local copies of the bits we need for sending a message,
            // because we will be doing it off of the main thread, which will
            // immediately continue on to resetting some of this state.
            final Uri mmsUri = mMessageUri;
            final PduPersister persister = PduPersister.getPduPersister(mActivity);

            final SlideshowModel slideshow = mSlideshow;
            final CharSequence subject = mSubject;

            // Do the dirty work of sending the message off of the main UI thread.
            new Thread(new Runnable() {
                public void run() {
                    //Gionee <guoyx> <2013-05-17> add for CR00813219 begin
                    final SendReq sendReq = getNewSendReq(conv);
                    //Gionee <guoyx> <2013-05-17> add for CR00813219 end

                    // Make sure the text in slide 0 is no longer holding onto a reference to
                    // the text in the message text box.
                    slideshow.prepareForSend();
                    sendMmsWorker(conv, mmsUri, persister, slideshow, sendReq);

                    updateSendStats(conv);
                }
            }).start();
        } else {
            // Same rules apply as above.
            final String msgText = mText.toString();
            new Thread(new Runnable() {
                public void run() {
                    preSendSmsWorker(conv, msgText, recipientsInUI);

                    updateSendStats(conv);
                }
            }).start();
        }

        // update the Recipient cache with the new to address, if it's different
        RecipientIdCache.updateNumbers(conv.getThreadId(), conv.getRecipients());
// m0
        // Mark the message as discarded because it is "off the market" after being sent.
//        mDiscarded = true;
// m1
    }

    // Be sure to only call this on a background thread.
    private void updateSendStats(final Conversation conv) {
        String[] dests = conv.getRecipients().getNumbers();
        final ArrayList<String> phoneNumbers = new ArrayList<String>(Arrays.asList(dests));

        DataUsageStatUpdater updater = new DataUsageStatUpdater(mActivity);
        updater.updateWithPhoneNumber(phoneNumbers);
    }

    private boolean addressContainsEmailToMms(Conversation conv, String text) {
        if (MmsConfig.getEmailGateway() != null) {
            String[] dests = conv.getRecipients().getNumbers();
            int length = dests.length;
            for (int i = 0; i < length; i++) {
                if (Mms.isEmailAddress(dests[i]) || MessageUtils.isAlias(dests[i])) {
                    String mtext = dests[i] + " " + text;
                    int[] params = SmsMessage.calculateLength(mtext, false);
                    if (params[0] > 1) {
                        updateState(RECIPIENTS_REQUIRE_MMS, true, true);
                        ensureSlideshow();
                        syncTextToSlideshow();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Message sending stuff

    private void preSendSmsWorker(Conversation conv, String msgText, String recipientsInUI) {
        // If user tries to send the message, it's a signal the inputted text is what they wanted.
        UserHappinessSignals.userAcceptedImeText(mActivity);

        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mGnMessageSupport && mIsRepeatSend) {
            Log.e("MMSLog", "preSendSmsWorker().......reSendSms....");
        } else {
            //gionee gaoj 2012-4-10 added for CR00555790 end
        mStatusListener.onPreMessageSent();
        //gionee gaoj 2012-4-10 added for CR00555790 start
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
// a0
        // Mark the message as discarded because it is "off the market" after being sent.
        mDiscarded = true;
// a1

        long origThreadId = conv.getThreadId();

        // Make sure we are still using the correct thread ID for our recipient set.
        long threadId = conv.ensureThreadId();

        //gionee gaoj 2012-4-10 added for CR00555790 start
        String recipients = mRepeatSendReceiver;
        if (MmsApp.mGnMessageSupport) {
            if (recipients == null || recipients.isEmpty()){
                recipients = conv.getRecipients().serialize();
            }
        } else {
            recipients = conv.getRecipients().serialize();
        }
        String semiSepRecipients = recipients;
        //gionee gaoj 2012-4-10 added for CR00555790 end

        // recipientsInUI can be empty when the user types in a number and hits send
        if (LogTag.SEVERE_WARNING && ((origThreadId != 0 && origThreadId != threadId) ||
               (!semiSepRecipients.equals(recipientsInUI) && !TextUtils.isEmpty(recipientsInUI)))) {
            String msg = origThreadId != 0 && origThreadId != threadId ?
                    "WorkingMessage.preSendSmsWorker threadId changed or " +
                    "recipients changed. origThreadId: " +
                    origThreadId + " new threadId: " + threadId +
                    " also mConversation.getThreadId(): " +
                    mConversation.getThreadId()
                :
                    "Recipients in window: \"" +
                    recipientsInUI + "\" differ from recipients from conv: \"" +
                    semiSepRecipients + "\"";

            LogTag.warnPossibleRecipientMismatch(msg, mActivity);
        }

        // just do a regular send. We're already on a non-ui thread so no need to fire
        // off another thread to do this work.
        sendSmsWorker(msgText, semiSepRecipients, threadId);

        // Be paranoid and clean any draft SMS up.
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mGnMessageSupport && mIsRepeatSend) {
            Log.e("MMSLog", "preSendSmsWorker().......reSendSms....delete draft sms message....");
        } else {
            //gionee gaoj 2012-4-10 added for CR00555790 end
        deleteDraftSmsMessage(threadId);
        //gionee gaoj 2012-4-10 added for CR00555790 start
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
    }

    private void sendSmsWorker(String msgText, String semiSepRecipients, long threadId) {
        String[] dests = TextUtils.split(semiSepRecipients, ";");
        if (LogTag.VERBOSE || Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.d(LogTag.TRANSACTION, "sendSmsWorker sending message: recipients=" +
                    semiSepRecipients + ", threadId=" + threadId);
        }
        //Gionee <guoyx> <2013-05-16> modified for begin
        MessageSender sender = getSmsMessageSender(msgText, threadId, dests);
        //Gionee <guoyx> <2013-05-16> modified for end
        
        try {
            sender.sendMessage(threadId);

            // Make sure this thread isn't over the limits in message count
            Recycler.getSmsRecycler().deleteOldMessagesByThreadId(mActivity, threadId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to send SMS message, threadId=" + threadId, e);
        }

        mStatusListener.onMessageSent();
// a0
        SmsReceiverService.sSmsSent = false;
// a1
    }

    private void sendMmsWorker(Conversation conv, Uri mmsUri, PduPersister persister,
                               SlideshowModel slideshow, SendReq sendReq) {
        // If user tries to send the message, it's a signal the inputted text is what they wanted.
        UserHappinessSignals.userAcceptedImeText(mActivity);

        // First make sure we don't have too many outstanding unsent message.
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(mActivity, mContentResolver,
                    // Aurora xuyong 2014-11-05 modified for privacy feature start
                    Mms.Outbox.CONTENT_URI, MMS_OUTBOX_PROJECTION, "is_privacy >= 0", null, null);
                    // Aurora xuyong 2014-11-05 modified for privacy feature end
            if (cursor != null) {
                long maxMessageSize = MmsConfig.getMaxSizeScaleForPendingMmsAllowed() *
// m0
//                    MmsConfig.getMaxMessageSize();
                    MmsConfig.getUserSetMmsSizeLimit(true);
// m1
                long totalPendingSize = 0;
                while (cursor.moveToNext()) {
// m0
//                    totalPendingSize += cursor.getLong(MMS_MESSAGE_SIZE_INDEX);
                    if (PduHeaders.STATUS_UNREACHABLE != cursor.getLong(MMS_MESSAGE_STATUS_INDEX)) {
                        totalPendingSize += cursor.getLong(MMS_MESSAGE_SIZE_INDEX);
                    }
// m1
                }
                if (totalPendingSize >= maxMessageSize) {
                    unDiscard();    // it wasn't successfully sent. Allow it to be saved as a draft.
                    mStatusListener.onMaxPendingMessagesReached();
                    return;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mGnMessageSupport && mIsRepeatSend) {
            Log.e("MMSLog", "sendMmsWorker().......reSendMMs....");
        } else {
            //gionee gaoj 2012-4-10 added for CR00555790 end
        mStatusListener.onPreMessageSent();
        //gionee gaoj 2012-4-10 added for CR00555790 start
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
// a0
        // Mark the message as discarded because it is "off the market" after being sent.
        mDiscarded = true;
// a1
        long threadId = 0;

        try {
            DraftCache.getInstance().setSavingDraft(true);

            // Make sure we are still using the correct thread ID for our
            // recipient set.
            threadId = conv.ensureThreadId();

            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                LogTag.debug("sendMmsWorker: update draft MMS message " + mmsUri);
            }

            // One last check to verify the address of the recipient.
            String[] dests = conv.getRecipients().getNumbers(true /* scrub for MMS address */);
            if (dests.length == 1) {
                // verify the single address matches what's in the database. If we get a different
                // address back, jam the new value back into the SendReq.
                String newAddress =
                    Conversation.verifySingleRecipient(mActivity, conv.getThreadId(), dests[0]);

                if (!newAddress.equals(dests[0])) {
                    dests[0] = newAddress;
                    EncodedStringValue[] encodedNumbers = EncodedStringValue.encodeStrings(dests);
                    if (encodedNumbers != null) {
                        sendReq.setTo(encodedNumbers);
                    }
                }
            }

            if (mmsUri == null) {
                // Create a new MMS message if one hasn't been made yet.
                mmsUri = createDraftMmsMessage(persister, sendReq, slideshow);
            } else {
                // Otherwise, sync the MMS message in progress to disk.
                updateDraftMmsMessage(mmsUri, persister, slideshow, sendReq);
            }
            // Be paranoid and clean any draft SMS up.
            //gionee gaoj 2012-4-10 added for CR00555790 start
            if (MmsApp.mGnMessageSupport && mIsRepeatSend) {
                Log.e("MMSLog", "sendMmsWorker().......reSendMMs....delete draft sms message.....");
            } else {
                //gionee gaoj 2012-4-10 added for CR00555790 end
            deleteDraftSmsMessage(threadId);
            //gionee gaoj 2012-4-10 added for CR00555790 start
            }
            //gionee gaoj 2012-4-10 added for CR00555790 end
        } finally {
            DraftCache.getInstance().setSavingDraft(false);
        }

        // Resize all the resizeable attachments (e.g. pictures) to fit
        // in the remaining space in the slideshow.
// m0
        int error = 0;
// m1
        try {
// m0
/*
            slideshow.finalResize(mmsUri);
        } catch (ExceedMessageSizeException e1) {
            error = MESSAGE_SIZE_EXCEEDED;
        } catch (MmsException e1) {
            error = UNKNOWN_ERROR;
        }
        if (error != 0) {
            markMmsMessageWithError(mmsUri);
            mStatusListener.onAttachmentError(error);
            return;
        }

        MessageSender sender = new MmsMessageSender(mActivity, mmsUri,
                slideshow.getCurrentMessageSize());
        try {
*/
            //Gionee <guoyx> <2013-05-16> modify for CR00813219 begin
            // Aurora xuyong 2014-10-23 modified for privacy feature start
            MessageSender sender = null;
            if (MmsApp.sHasPrivacyFeature) {
                sender = getMmsMessageSender(mmsUri, slideshow, conv);
            } else {
                sender = getMmsMessageSender(mmsUri, slideshow);
            }
            // Aurora xuyong 2014-10-23 modified for privacy feature end
            //Gionee <guoyx> <2013-05-16> modify for CR00813219 end
            
// m1
            if (!sender.sendMessage(threadId)) {
                // The message was sent through SMS protocol, we should
                // delete the copy which was previously saved in MMS drafts.
                SqliteWrapper.delete(mActivity, mContentResolver, mmsUri, null, null);
            }

            // Make sure this thread isn't over the limits in message count
            Recycler.getMmsRecycler().deleteOldMessagesByThreadId(mActivity, threadId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to send message: " + mmsUri + ", threadId=" + threadId, e);
        }

        mStatusListener.onMessageSent();
// a0
        SendTransaction.sMMSSent = false;
// a1
    }

    private void markMmsMessageWithError(Uri mmsUri) {
        try {
            PduPersister p = PduPersister.getPduPersister(mActivity);
            // Move the message into MMS Outbox. A trigger will create an entry in
            // the "pending_msgs" table.
            p.move(mmsUri, Mms.Outbox.CONTENT_URI);

            // Now update the pending_msgs table with an error for that new item.
            ContentValues values = new ContentValues(1);
            values.put(PendingMessages.ERROR_TYPE, MmsSms.ERR_TYPE_GENERIC_PERMANENT);
            long msgId = ContentUris.parseId(mmsUri);
            SqliteWrapper.update(mActivity, mContentResolver,
                    PendingMessages.CONTENT_URI,
                    values, PendingMessages.MSG_ID + "=" + msgId, null);
        } catch (MmsException e) {
            // Not much we can do here. If the p.move throws an exception, we'll just
            // leave the message in the draft box.
            Log.e(TAG, "Failed to move message to outbox and mark as error: " + mmsUri, e);
        }
    }

    // Draft message stuff

    private static final String[] MMS_DRAFT_PROJECTION = {
        Mms._ID,                // 0
        Mms.SUBJECT,            // 1
        Mms.SUBJECT_CHARSET     // 2
    };

    private static final int MMS_ID_INDEX         = 0;
    private static final int MMS_SUBJECT_INDEX    = 1;
    private static final int MMS_SUBJECT_CS_INDEX = 2;

    private static Uri readDraftMmsMessage(Context context, Conversation conv, StringBuilder sb) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("readDraftMmsMessage conv: " + conv);
        }
        Cursor cursor;
        ContentResolver cr = context.getContentResolver();

        // Aurora xuyong 2014-11-05 modified for privacy feature start
        final String selection = Mms.THREAD_ID + " = " + conv.getThreadId() + " AND is_privacy >= 0";
        // Aurora xuyong 2014-11-05 modified for privacy feature end
        cursor = SqliteWrapper.query(context, cr,
                Mms.Draft.CONTENT_URI, MMS_DRAFT_PROJECTION,
                selection, null, null);

        Uri uri;
        try {
            //gionee gaoj 2012-9-27 modified for CR00704222 start
            if (cursor != null && cursor.moveToFirst()) {
                //gionee gaoj 2012-9-27 modified for CR00704222 end
                uri = ContentUris.withAppendedId(Mms.Draft.CONTENT_URI,
                        cursor.getLong(MMS_ID_INDEX));
                String subject = MessageUtils.extractEncStrFromCursor(cursor, MMS_SUBJECT_INDEX,
                        MMS_SUBJECT_CS_INDEX);
                if (subject != null) {
                    sb.append(subject);
                }
                if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                    LogTag.debug("readDraftMmsMessage uri: ", uri);
                }
                return uri;
            }
        } finally {
            //gionee gaoj 2012-8-5 added for CR00664088 start
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            //gionee gaoj 2012-8-5 added for CR00664088 end
        }

        return null;
    }

    /**
     * makeSendReq should always return a non-null SendReq, whether the dest addresses are
     * valid or not.
     */
    private static SendReq makeSendReq(Conversation conv, CharSequence subject) {
// m0
//        String[] dests = conv.getRecipients().getNumbers(true /* scrub for MMS address */);
        String[] dests = conv.getRecipients().getNumbers(false /*don't scrub for MMS address */);
// m1

        SendReq req = new SendReq();
        EncodedStringValue[] encodedNumbers = EncodedStringValue.encodeStrings(dests);
        if (encodedNumbers != null) {
            req.setTo(encodedNumbers);
        }

        if (!TextUtils.isEmpty(subject)) {
            req.setSubject(new EncodedStringValue(subject.toString()));
        }

        req.setDate(System.currentTimeMillis() / 1000L);

        return req;
    }

    private Uri createDraftMmsMessage(PduPersister persister, SendReq sendReq,
            SlideshowModel slideshow) {
// a0
        if (slideshow == null){
            return null;
        }
// a1
        try {
            PduBody pb = slideshow.toPduBody();
            sendReq.setBody(pb);
            // Aurora xuyong 2014-10-23 modified for privacy feature start
            Uri res = null;
            if (MmsApp.sHasPrivacyFeature) {
             // Aurora xuyong 2014-10-28 added for privacy feature start
                long privacy = 0;
                if (mConversation.getRecipients() != null && mConversation.getRecipients().size() > 0) {
                    privacy = mConversation.getRecipients().get(0).getPrivacy();
                }
             // Aurora xuyong 2014-10-28 added for privacy feature end
                res = persister.persist(sendReq, Mms.Draft.CONTENT_URI, privacy);
            } else {
                res = persister.persist(sendReq, Mms.Draft.CONTENT_URI);
            }
            // Aurora xuyong 2014-10-23 modified for privacy feature end
            // for vcard
            deleteFileAttachmentTempFile();
            slideshow.sync(pb);
            return res;
        } catch (MmsException e) {
            return null;
        }
// a0
 catch (IllegalArgumentException e){
            return null;
// a1
        }
    }

    private void asyncUpdateDraftMmsMessage(final Conversation conv, final boolean isStopping) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("asyncUpdateDraftMmsMessage conv=%s mMessageUri=%s", conv, mMessageUri);
        }

        new Thread(new Runnable() {
            public void run() {
                try {
                    DraftCache.getInstance().setSavingDraft(true);
                    final PduPersister persister = PduPersister.getPduPersister(mActivity);
                    final SendReq sendReq = makeSendReq(conv, mSubject);

                    //gionee gaoj 2012-4-10 added for CR00555790 start //CR00788343
                    if (MmsApp.mGnMessageSupport && conv.getMessageCount() == 0 && MmsApp.mIsDraftOpen) {
                        EncodedStringValue val = new EncodedStringValue("gn_draft_address_token");
                        sendReq.addTo(val);
                        synchronized (MmsApp.getApplication()) {
                            if (mMessageUri == null) {
                                mMessageUri = createDraftMmsMessage(persister, sendReq, mSlideshow);
                            } else {
                                updateDraftMmsMessage(mMessageUri, persister, mSlideshow, sendReq);
                            }
                        }

                        final String[] THREDID_PROJECTION = {
                                Mms.THREAD_ID
                            };

                        //gionee gaoj 2012-6-26 added for CR00628243 start
                        Cursor threadCursor = null;
                        if (mMessageUri != null) {
                            threadCursor = SqliteWrapper.query(mActivity, mContentResolver,
                                    // Aurora xuyong 2014-11-05 modified for privacy feature start
                                    mMessageUri, THREDID_PROJECTION, "is_privacy >= 0", null, null);
                                    // Aurora xuyong 2014-11-05 modified for privacy feature end
                        }
                        //gionee gaoj 2012-6-26 added for CR00628243 end

                        if (threadCursor != null) {
                            if (threadCursor.moveToFirst() == true) {
                                long threadId = threadCursor.getLong(0);
                                // gionee zhouyj 2012-07-12 add for CR00643113 start 
//                                if (MmsApp.mGnMessageSupport) {
//                                    mRecentThreadId = threadId;
//                                }
                                // gionee zhouyj 2012-07-12 add for CR00643113 end 
                                DraftCache.getInstance().setDraftState(threadId, true);
                            }
                            threadCursor.close();
                        }
                    } else {
                        //gionee gaoj 2012-4-10 added for CR00555790 end
                    if (mMessageUri == null) {
                        mMessageUri = createDraftMmsMessage(persister, sendReq, mSlideshow);
                    } else {
                        updateDraftMmsMessage(mMessageUri, persister, mSlideshow, sendReq);
                    }
                    /*
                    // mtk81083 this can be commented, delete useful empty thread has been fixed
                    // done in provider, thread table add a column status to indicate it's using.
                    // comment this to resolve ANR :237516 
                    if (isStopping && conv.getMessageCount() == 0) {
                        // createDraftMmsMessage can create the new thread in the threads table (the
                        // call to createDraftMmsDraftMessage calls PduPersister.persist() which
                        // can call Threads.getOrCreateThreadId()). Meanwhile, when the user goes
                        // back to ConversationList while we're saving a draft from CMA's.onStop,
                        // ConversationList will delete all threads from the thread table that
                        // don't have associated sms or pdu entries. In case our thread got deleted,
                        // well call clearThreadId() so ensureThreadId will query the db for the new
                        // thread.
                        conv.clearThreadId();   // force us to get the updated thread id
                    }
                    */
                    //gionee gaoj 2013-4-8 modified for CR00788343 start
                    if (mMessageUri != null) {
                        if (!conv.getRecipients().isEmpty()) {
                            conv.ensureThreadId();
                        }
                        conv.setDraftState(true);
                    } else {
                        conv.setDraftState(true);
                    }
                    //gionee gaoj 2013-4-8 modified for CR00788343 end
                    if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                        LogTag.debug("asyncUpdateDraftMmsMessage conv: " + conv +
                                " uri: " + mMessageUri);
                    }

                    // Be paranoid and delete any SMS drafts that might be lying around. Must do
                    // this after ensureThreadId so conv has the correct thread id.
                    asyncDeleteDraftSmsMessage(conv);
                    if (mNeedDeleteOldMmsDraft) {
                        mNeedDeleteOldMmsDraft = false;
                        asyncDeleteOldMmsDraft(conv.getThreadId());
                    }
                    //gionee gaoj 2012-4-10 added for CR00555790 start
                    }
                    //gionee gaoj 2012-4-10 added for CR00555790 end
                } finally {
                    DraftCache.getInstance().setSavingDraft(false);
                }
            }
        }).start();
    }
    // Aurora xuyong 2014-10-23 modified for privacy feature start
    private void updateDraftMmsMessage(Uri uri, PduPersister persister,
    // Aurora xuyong 2014-10-23 modified for privacy featyre end
            SlideshowModel slideshow, SendReq sendReq) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("updateDraftMmsMessage uri=%s", uri);
        }
        if (uri == null) {
            Log.e(TAG, "updateDraftMmsMessage null uri");
            return;
        }
// m0
//        persister.updateHeaders(uri, sendReq);
        try {
          // Aurora xuyong 2014-10-23 modified for privacy feature start
            if (MmsApp.sHasPrivacyFeature) {
             // Aurora xuyong 2014-10-28 modified for privacy feature start
                long privacy = 0;
                if (mConversation.getRecipients() != null && mConversation.getRecipients().size() > 0) {
                    privacy = mConversation.getRecipients().get(0).getPrivacy();
                }
                persister.updateHeaders(uri, sendReq, privacy);
             // Aurora xuyong 2014-10-28 modified for privacy feature end
            } else {
                persister.updateHeaders(uri, sendReq);
            }
          // Aurora xuyong 2014-10-23 modified for privacy feature end
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "updateDraftMmsMessage: cannot update message " + uri);
        }
// m1
        if (slideshow == null) {
            Thread.dumpStack();
            Log.e(TAG, "updateDraftMmsMessage, oops slideshow is null");
            Log.e(TAG, "updateDraftMmsMessage, sendreq " + sendReq);
            return;
        }
        //Gionee guoyx 20121023 by CR00705464 for MTK ALPS00270539 BEGIN
        synchronized (sDraftMmsLock) {
            final PduBody pb = slideshow.toPduBody();
    
            try {
                persister.updateParts(uri, pb);
            } catch (MmsException e) {
                Log.e(TAG, "updateDraftMmsMessage: cannot update message " + uri);
            }
    
            slideshow.sync(pb);
        }
        //Gionee guoyx 20121023 by CR00705464 for MTK ALPS00270539 END
    }

    // Aurora xuyong 2014-11-05 modified for privacy feature start
    private static final String SMS_DRAFT_WHERE = Sms.TYPE + "=" + Sms.MESSAGE_TYPE_DRAFT + " AND is_privacy >= 0";
    // Aurora xuyong 2014-11-05 modified for privacy feature end
    private static final String[] SMS_BODY_PROJECTION = { Sms.BODY };
    private static final int SMS_BODY_INDEX = 0;

    /**
     * Reads a draft message for the given thread ID from the database,
     * if there is one, deletes it from the database, and returns it.
     * @return The draft message or an empty string.
     */
    private String readDraftSmsMessage(Conversation conv) {
        long thread_id = conv.getThreadId();
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.d(TAG, "readDraftSmsMessage conv: " + conv);
        }
        // If it's an invalid thread or we know there's no draft, don't bother.
        if (thread_id <= 0 || !conv.hasDraft()) {
            return "";
        }

        Uri thread_uri = ContentUris.withAppendedId(Sms.Conversations.CONTENT_URI, thread_id);
        String body = "";

        Cursor c = SqliteWrapper.query(mActivity, mContentResolver,
                        thread_uri, SMS_BODY_PROJECTION, SMS_DRAFT_WHERE, null, null);
        boolean haveDraft = false;
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    body = c.getString(SMS_BODY_INDEX);
                    haveDraft = true;
                }
            } finally {
                c.close();
            }
        }

        // We found a draft, and if there are no messages in the conversation,
        // that means we deleted the thread, too. Must reset the thread id
        // so we'll eventually create a new thread.
        if (haveDraft && conv.getMessageCount() == 0) {
            asyncDeleteDraftSmsMessage(conv);

            // Clean out drafts for this thread -- if the recipient set changes,
            // we will lose track of the original draft and be unable to delete
            // it later.  The message will be re-saved if necessary upon exit of
            // the activity.
            clearConversation(conv, true);
        }
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("readDraftSmsMessage haveDraft: ", !TextUtils.isEmpty(body));
        }

        return body;
    }

    public void clearConversation(final Conversation conv, boolean resetThreadId) {
// m0
        //add for bug ALPS00047256
        int MessageCount=0;
        // Aurora xuyong 2014-11-13 added for debug start
        boolean isRejected = false;
        // Aurora xuyong 2014-11-13 added for debug end
        final Uri sAllThreadsUri =  Threads.CONTENT_URI.buildUpon().appendQueryParameter("simple", "true").build();
 
        if (conv.getMessageCount()==0){
            Cursor cursor = SqliteWrapper.query(
                mActivity,
                mContentResolver,
                sAllThreadsUri,
                // Aurora xuyong 2014-11-13 modified for debug start
                new String[] {Threads.MESSAGE_COUNT, Threads._ID, "reject"} ,
                // Aurora xuyong 2014-11-13 modified for debug end
                Threads._ID + "=" + conv.getThreadId(), null, null);
            if (cursor !=null) {
                try {
                    if (cursor.moveToFirst()) {
                        MessageCount = cursor.getInt(cursor.getColumnIndexOrThrow(Threads.MESSAGE_COUNT));
                        // Aurora xuyong 2014-11-13 added for debug start
                        isRejected = cursor.getInt(cursor.getColumnIndexOrThrow("reject")) == 1 ? true : false;
                        // Aurora xuyong 2014-11-13 added for debug end
                    }
                } finally {
                    cursor.close();
                }
            }
        }
//        if (resetThreadId && conv.getMessageCount() == 0) {
        if (resetThreadId && (conv.getMessageCount() == 0) && (MessageCount == 0)) {
// m1
            if (DEBUG) LogTag.debug("clearConversation calling clearThreadId");
            //gionee gaoj 2012-4-10 added for CR00555790 start
            if (MmsApp.mGnMessageSupport && conv.getSimId() == -1) {
                Log.w(TAG, "GN_DRAFT clearConversation have been invoke" );
                conv.setDraftState(false);
            }
            //gionee gaoj 2012-4-10 added for CR00555790 end
            // Aurora xuyong 2014-11-13 modified for debug start
            if (!isRejected) {
                conv.clearThreadId();
            }
            // Aurora xuyong 2014-11-13 modified for debug end
        }

        conv.setDraftState(false);
    }

    private void asyncUpdateDraftSmsMessage(final Conversation conv, final String contents) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    //gionee gaoj 2012-4-10 added for CR00555790 start //CR00788343
                    if (MmsApp.mGnMessageSupport && conv.getMessageCount() == 0 && MmsApp.mIsDraftOpen) {
                        // gionee lwzh add for CR00714645 20121018 begin
                        DraftCache.getInstance().setSavingDraft(true);
                        // gionee lwzh add for CR00714645 20121018 end
                        synchronized (MmsApp.getApplication()) {
                            conv.setDraftState(true);
                            updateDraftSmsMessage(conv, contents);
                            if (mMessageUri != null) {
                                asyncDelete(mMessageUri, null, null);
                            }
                        }
                    } else {
                        //gionee gaoj 2012-4-10 added for CR00555790 end
                    DraftCache.getInstance().setSavingDraft(true);
                    conv.guaranteeThreadId();
                    conv.setDraftState(true);
                    updateDraftSmsMessage(conv, contents);
                    //gionee gaoj 2012-4-10 added for CR00555790 start
                    }
                    //gionee gaoj 2012-4-10 added for CR00555790 end
                } finally {
                    DraftCache.getInstance().setSavingDraft(false);
                }
            }
        }).start();
    }

    private void updateDraftSmsMessage(final Conversation conv, String contents) {
        //gionee gaoj 2012-3-22 added for CR00555790 start
        long a = 0;
        //gionee gaoj 2013-4-2 added for CR00788343 start
        if (MmsApp.mGnMessageSupport && MmsApp.mIsDraftOpen) {
           //gionee gaoj 2013-4-2 added for CR00788343 end
            a = conv.ensureGnThreadId(true);
        } else {
            a = conv.getThreadId();
        }
        final long threadId = a;
        // gionee zhouyj 2012-07-12 add for CR00640360 start 
        if (MmsApp.mGnMessageSupport) {
            DraftCache.getInstance().setDraftState(threadId, true);
        }
        // gionee zhouyj 2012-07-12 add for CR00640360 end 
        //gionee gaoj 2012-3-22 added for CR00555790 end
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("updateDraftSmsMessage tid=%d, contents=\"%s\"", threadId, contents);
        }

        // If we don't have a valid thread, there's nothing to do.
        if (threadId <= 0) {
            return;
        }

        //gionee gaoj 2013-4-2 added for CR00788343 start
        if (conv.getEncryption() && !MmsApp.mIsDraftOpen) {
            Uri threadiduri = Uri.parse("content://mms-sms/encryption/" + threadId);
            int update = mConversation.updatethreads(mActivity, threadiduri, false);
        }
        //gionee gaoj 2013-4-2 added for CR00788343 end
        ContentValues values = new ContentValues(3);
        values.put(Sms.THREAD_ID, threadId);
        values.put(Sms.BODY, contents);
        values.put(Sms.TYPE, Sms.MESSAGE_TYPE_DRAFT);
        //gionee gaoj 2012-6-8 added for CR00622189 start
        if (MmsApp.mGnMessageSupport && conv.getRecipients() != null) {
            String addreString = conv.getRecipients().serialize().replaceAll(";", ",");
            values.put(Sms.ADDRESS, addreString);
        }
        //gionee gaoj 2012-6-8 added for CR00622189 end
        SqliteWrapper.insert(mActivity, mContentResolver, Sms.CONTENT_URI, values);
        asyncDeleteDraftMmsMessage(conv);
        //ALPS00289861
        mMessageUri = null;
    }

    //gionee gaoj 2012-4-10 added for CR00555790 start
    public boolean getDraftDirty() {
        return mIsDraftDirty;
    }

    public void setmIsRepeatSend(boolean mIsRepeatSend) {
        this.mIsRepeatSend = mIsRepeatSend;
    }
    //gionee gaoj 2012-4-10 added for CR00555790 end
    private void asyncDelete(final Uri uri, final String selection, final String[] selectionArgs) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("asyncDelete %s where %s", uri, selection);
        }
        new Thread(new Runnable() {
            public void run() {
                delete(uri, selection, selectionArgs);
            }
        }).start();
    }

    private void delete(final Uri uri, final String selection, final String[] selectionArgs) {
        // m0
        // SqliteWrapper.delete(mActivity, mContentResolver, uri,
        // selection, selectionArgs);
        // for vcard
        deleteFileAttachmentTempFile();
        if (uri != null) {
            int result = -1; // if delete is unsuccessful, the delete()
            // method will return -1
            int retryCount = 0;
            int maxRetryCount = 10;
            while (result == -1 && retryCount < maxRetryCount) {
                try {
                    result = SqliteWrapper.delete(mActivity, mContentResolver, uri, selection, selectionArgs);
                    retryCount++;
                } catch (SQLiteDiskIOException e) {
                    retryCount++;
                    Log.e(TAG, "asyncDelete(): SQLiteDiskIOException: delete thread unsuccessful! Try time="
                                    + retryCount);
                }
            }
        }
        // m1
    }

    public void asyncDeleteDraftSmsMessage(Conversation conv) {
        mHasSmsDraft = false;

        final long threadId = conv.getThreadId();
        if (threadId > 0) {
            asyncDelete(ContentUris.withAppendedId(Sms.Conversations.CONTENT_URI, threadId),
                SMS_DRAFT_WHERE, null);
        }
    }

    private void deleteDraftSmsMessage(long threadId) {
        SqliteWrapper.delete(mActivity, mContentResolver,
                ContentUris.withAppendedId(Sms.Conversations.CONTENT_URI, threadId),
                SMS_DRAFT_WHERE, null);
    }

    private void asyncDeleteDraftMmsMessage(Conversation conv) {
        mHasMmsDraft = false;
        //gionee gaoj 2012-6-8 added for CR00622189 start
        long a = 0;
        //gionee gaoj 2013-4-2 added for CR00788343 start
        if (MmsApp.mGnMessageSupport && MmsApp.mIsDraftOpen) {
            //gionee gaoj 2013-4-2 added for CR00788343 end
            a = conv.ensureGnThreadId(true);
        } else {
            a = conv.getThreadId();
        }
        final long threadId = a;
//        final long threadId = conv.getThreadId();
        //gionee gaoj 2012-6-8 added for CR00622189 end
        if (threadId > 0) {
            final String where = Mms.THREAD_ID + " = " + threadId;
            asyncDelete(Mms.Draft.CONTENT_URI, where, null);
// a0
        /* Reset MMS's message URI because the MMS is deleted */
        mMessageUri = null;
// a1
        }
    }

    private void asyncDeleteCurrentDraftMmsMessage(Conversation conv) {
        mHasMmsDraft = false;

        final long threadId = conv.getThreadId();
        if (threadId > 0) {
            final String where = Mms.THREAD_ID + " = " + threadId;
            asyncDelete(mMessageUri, where, null);

            mMessageUri = null;
        } else if (mMessageUri != null) {
            asyncDelete(mMessageUri, null, null);
            mMessageUri = null;
        }
    }

// a0
    //Creation mode.
    private final static String CREATION_MODE_RESTRICTED = "RESTRICTED";
    private final static String CREATION_MODE_WARNING    = "WARNING";
    private final static String CREATION_MODE_FREE       = "FREE";

    public static final int WARNING_TYPE    = -10;
    public static final int RESTRICTED_TYPE = -11;
    public static final int RESTRICTED_RESOLUTION = -12;

    public boolean mHasDrmPart = false;
    public boolean mHasDrmRight = false;
    private static final int CLASSNUMBERMINI = 130;

    //Set resizedto true if the image is 
    private boolean mResizeImage = false;

    private static final int MMS_MESSAGE_STATUS_INDEX  = 2;

    public static  int sCreationMode  = 0;

    private static int sMessageClassMini = 0;

    private boolean mNeedDeleteOldMmsDraft;

    public int getCurrentMessageSize() {
        int currentMessageSize = mSlideshow.getCurrentSlideshowSize();
        return currentMessageSize;
    }

    public static void updateCreationMode(Context context){
        SharedPreferences sp = AuroraPreferenceManager.getDefaultSharedPreferences(context);
        String creationMode = sp.getString(MessagingPreferenceActivity.CREATION_MODE, CREATION_MODE_FREE);
        if (creationMode.equals(CREATION_MODE_WARNING)) {
            sCreationMode = WARNING_TYPE;
        } else if (creationMode.equals(CREATION_MODE_RESTRICTED)) {
            sCreationMode = RESTRICTED_TYPE;
        } else {
            sCreationMode = OK;
        }
    }

    public boolean ismResizeImage() {
        return mResizeImage;
    }

    public void setmResizeImage(boolean mResizeImage) {
        this.mResizeImage = mResizeImage;
    }

    // add for gemini
    private int mSimId = 0;

    /**
     * Send this message over the network.  Will call back with onMessageSent() once
     * it has been dispatched to the telephony stack.  This WorkingMessage object is
     * no longer useful after this method has been called.
     */
    public void sendGemini(int simId) {
        Log.d(MmsApp.TXN_TAG, "Enter sendGemini(). SIM_ID = " + Integer.toString(simId, 10));
        
        // keep this sim id
        mSimId = simId;

        // Get ready to write to disk.
        prepareForSave(true /* notify */);

        // We need the recipient list for both SMS and MMS.
        final Conversation conv = mConversation;
        String msgTxt = mText.toString();

        if (requiresMms() || addressContainsEmailToMms(conv, msgTxt)) {
            //for speed.
            conv.ensureThreadId();
            mStatusListener.onPreMmsSent();        
            //
            // Make local copies of the bits we need for sending a message,
            // because we will be doing it off of the main thread, which will
            // immediately continue on to resetting some of this state.
            final Uri mmsUri = mMessageUri;
            final PduPersister persister = PduPersister.getPduPersister(mActivity);

            final SlideshowModel slideshow = mSlideshow;
            //gionee gaoj 2012-8-21 modified for CR00678315 start
            //Gionee <guoyx> <2013-05-17> modify for CR00813219 begin
            SendReq newsendReq = getNewSendReq(conv);
            //Gionee <guoyx> <2013-05-17> modify for CR00813219 end
            final SendReq sendReq = newsendReq;
            //gionee gaoj 2012-8-21 modified for CR00678315 end

            // Make sure the text in slide 0 is no longer holding onto a reference to the text
            // in the message text box.
            slideshow.prepareForSend();

            // Do the dirty work of sending the message off of the main UI thread.
            new Thread(new Runnable() {
                public void run() {
                    sendMmsWorkerGemini(conv, mmsUri, mSimId, persister, slideshow, sendReq);
                }
            }).start();
        } else {
            // Same rules apply as above.
            final String msgText = mText.toString();
            new Thread(new Runnable() {
                public void run() {
                    //sendSmsWorkerGemini(conv, msgText, mSimId);
                    preSendSmsWorkerGemini(conv, msgText, mSimId);
                }
            }).start();
        }

        // update the Recipient cache with the new to address, if it's different
        RecipientIdCache.updateNumbers(conv.getThreadId(), conv.getRecipients());
    }


    // Dual SIM Message sending stuff
    //2.2
    private void preSendSmsWorkerGemini(Conversation conv, String msgText, int simId) {
        Log.d(MmsApp.TXN_TAG, "preSendSmsWorkerGemini. SIM ID = " + simId);
        // If user tries to send the message, it's a signal the inputted text is what they wanted.
        UserHappinessSignals.userAcceptedImeText(mActivity);

        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mGnMessageSupport && mIsRepeatSend) {
            Log.e("MMSLog", "preSendSmsWorkerGemini().......reSendSms....");
        } else {
            //gionee gaoj 2012-4-10 added for CR00555790 end
        mStatusListener.onPreMessageSent();
        //gionee gaoj 2012-4-10 added for CR00555790 start
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end

        // Mark the message as discarded because it is "off the market" after being sent.
        mDiscarded = true;

        // Make sure we are still using the correct thread ID for our
        // recipient set.
        long threadId = conv.ensureThreadId();

        //gionee gaoj 2012-4-10 added for CR00555790 start
        String recipients = mRepeatSendReceiver;
        if (MmsApp.mGnMessageSupport) {
            if (recipients == null || recipients.isEmpty()){
                recipients = conv.getRecipients().serialize();
            }
        } else {
            recipients = conv.getRecipients().serialize();
        }
        final String semiSepRecipients = recipients;
        //gionee gaoj 2012-4-10 added for CR00555790 end

        // just do a regular send. We're already on a non-ui thread so no need to fire
        // off another thread to do this work.
        sendSmsWorkerGemini(msgText, semiSepRecipients, threadId, simId);

        // Be paranoid and clean any draft SMS up.
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mGnMessageSupport && mIsRepeatSend) {
            Log.e("MMSLog", "preSendSmsWorkerGemini().......reSendSms....not delete draft sms message.....");
        } else {
            //gionee gaoj 2012-4-10 added for CR00555790 end
        deleteDraftSmsMessage(threadId);
        //gionee gaoj 2012-4-10 added for CR00555790 start
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
        //gionee gaoj 2012-4-10 added for CR00555790 start
        mRepeatSendReceiver = null;
        //gionee gaoj 2012-4-10 added for CR00555790 end
    }

    private void sendSmsWorkerGemini(String msgText, String semiSepRecipients, long threadId, int simId) {
        Log.d(MmsApp.TXN_TAG, "sendSmsWorkerGemini. SIM ID = " + simId);
        String[] dests = TextUtils.split(semiSepRecipients, ";");
        //gionee gaoj 2012-8-21 modified for CR00678315 start
        //Gionee <guoyx> <2013-05-16> modify for CR00813219 begin
        MessageSender sender = getSmsMessageSenderGemini(msgText, threadId, simId, dests);
        //Gionee <guoyx> <2013-05-16> modify for CR00813219 end
        //gionee gaoj 2012-8-21 modified for CR00678315 end
        try {
            sender.sendMessage(threadId);

            // Make sure this thread isn't over the limits in message count
            Recycler.getSmsRecycler().deleteOldMessagesByThreadId(mActivity, threadId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to send SMS message, threadId=" + threadId, e);
        }

        //gionee gaoj 2013-3-11 added for CR00782858 start
          // Aurora liugj 2013-11-07 deleted for hide widget start
        //MmsWidgetProvider.notifyDatasetChanged(mActivity);
          // Aurora liugj 2013-11-07 deleted for hide widget end
        //gionee gaoj 2013-3-11 added for CR00782858 end
        
        mStatusListener.onMessageSent();
        SmsReceiverService.sSmsSent = false;
    }

    // add for gemini 2.2
    private void sendMmsWorkerGemini(Conversation conv, Uri mmsUri, int simId, 
                                                PduPersister persister, SlideshowModel slideshow, 
                                                SendReq sendReq) {
        Log.d(MmsApp.TXN_TAG, "Enter sendMmsWorkerGemini(). SIM_ID = " + Integer.toString(simId, 10) 
            + "  mmsUri=" + mmsUri);

        // If user tries to send the message, it's a signal the inputted text is what they wanted.
        UserHappinessSignals.userAcceptedImeText(mActivity);

        // First make sure we don't have too many outstanding unsent message.
        Cursor cursor = null;
        try {
            //cursor = SqliteWrapper.query(mActivity, mContentResolver,
            //        Mms.Outbox.CONTENT_URI, MMS_OUTBOX_PROJECTION, null, null, null);
            String[] selectionArgs = new String[] {Integer.toString(simId)};
            cursor = SqliteWrapper.query(mActivity, 
                                        mContentResolver, 
                                        Mms.Outbox.CONTENT_URI, 
                                        MMS_OUTBOX_PROJECTION, 
                                        // Aurora xuyong 2014-11-05 modified for privacy feature start
                                        Mms.SIM_ID + " = ?" + " AND is_privacy >= 0", 
                                        // Aurora xuyong 2014-11-05 modified for privacy feature end
                                        selectionArgs, 
                                        null);
            if (cursor != null) {
                long maxMessageSize = MmsConfig.getMaxSizeScaleForPendingMmsAllowed() *
                    MmsConfig.getUserSetMmsSizeLimit(true);
                long totalPendingSize = 0;
                while (cursor.moveToNext()) {
                    if (PduHeaders.STATUS_UNREACHABLE != cursor.getLong(MMS_MESSAGE_STATUS_INDEX)) {
                        totalPendingSize += cursor.getLong(MMS_MESSAGE_SIZE_INDEX);
                    }
                }
                if (totalPendingSize >= maxMessageSize) {
                    Log.d(MmsApp.TXN_TAG, "sendMmsWorkerGemini(). total Pending Size >= max Message Size");
                    unDiscard();    // it wasn't successfully sent. Allow it to be saved as a draft.
                    mStatusListener.onMaxPendingMessagesReached();
                    return;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mGnMessageSupport && mIsRepeatSend) {
            Log.e("MMSLog", "sendMmsWorkerGemini().......reSendMMs....");
        } else {
            //gionee gaoj 2012-4-10 added for CR00555790 end
        mStatusListener.onPreMessageSent();
        //gionee gaoj 2012-4-10 added for CR00555790 start
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end

        // Mark the message as discarded because it is "off the market" after being sent.
        mDiscarded = true;

        // Make sure we are still using the correct thread ID for our
        // recipient set.
        long threadId = conv.ensureThreadId();

        if (mmsUri == null) {
            Log.d(MmsApp.TXN_TAG, "create Draft Mms Message()");
            // Create a new MMS message if one hasn't been made yet.
            mmsUri = createDraftMmsMessage(persister, sendReq, slideshow);
        } else {
            Log.d(MmsApp.TXN_TAG, "update Draft Mms Message()");
            // Otherwise, sync the MMS message in progress to disk.
            updateDraftMmsMessage(mmsUri, persister, slideshow, sendReq);
        }
        Log.d(MmsApp.TXN_TAG, "sendMmsWorkerGemini(). mmsUri="+mmsUri);

        // Be paranoid and clean any draft SMS up.
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mGnMessageSupport && mIsRepeatSend) {
            Log.e("MMSLog", "sendMmsWorker().......reSendMMs....delete draft sms message.....");
        } else {
            //gionee gaoj 2012-4-10 added for CR00555790 end
        deleteDraftSmsMessage(threadId);
        //gionee gaoj 2012-4-10 added for CR00555790 start
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
    
        // Resize all the resizeable attachments (e.g. pictures) to fit
        // in the remaining space in the slideshow.
    
        //gionee gaoj 2012-8-21 modified for CR00678315 start
        //Gionee <guoyx> <2013-05-17> modify for CR00813219 begin
        // Aurora xuyong 2014-10-23 modified for privacy feature start
        MessageSender sender = null;
        if (MmsApp.sHasPrivacyFeature) {
            sender = getMmsMessageSender(mmsUri, slideshow, conv);
        } else {
            sender = getMmsMessageSender(mmsUri, slideshow);
        }
        // Aurora xuyong 2014-10-23 modified for privacy feature end
        //Gionee <guoyx> <2013-05-17> modify for CR00813219 end
        //gionee gaoj 2012-8-21 modified for CR00678315 end
        if (CLASSNUMBERMINI == getMessageClassMini()){
            ((MmsMessageSender)sender).setLeMeiFlag(true);
        }
        try {
            if (!sender.sendMessageGemini(threadId, simId)) {
                // The message was sent through SMS protocol, we should
                // delete the copy which was previously saved in MMS drafts.
                SqliteWrapper.delete(mActivity, mContentResolver, mmsUri, null, null);
            }

            // Make sure this thread isn't over the limits in message count
            Recycler.getMmsRecycler().deleteOldMessagesByThreadId(mActivity, threadId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to send message: " + mmsUri + ", threadId=" + threadId, e);
        }

        //gionee gaoj 2013-3-11 added for CR00782858 start
        MmsWidgetProvider.notifyDatasetChanged(mActivity);
        //gionee gaoj 2013-3-11 added for CR00782858 end
        
        mStatusListener.onMessageSent();
        SendTransaction.sMMSSent = false;
    }    
// a1
    public Uri getMessageUri() {
        return this.mMessageUri;
    }

    // Set flag that old draft should be deleted
    public void  setNeedDeleteOldMmsDraft(Boolean delete){
        mNeedDeleteOldMmsDraft = delete;
    }

    private void asyncDeleteOldMmsDraft(final long threadId) {
        MessageUtils.addRemoveOldMmsThread(new Runnable() {
            public void run() {
                if (mMessageUri != null && threadId > 0) {
                    String pduId = mMessageUri.getLastPathSegment();
                    final String where = Mms.THREAD_ID + "=" + threadId + " and " + WordsTable.ID
                            + " != " + pduId;
                    delete(Mms.Draft.CONTENT_URI, where, null);
                }
            }
        });
    }
    
    /**
     * Delete all drafts of current thread by threadId.
     * 
     * @param threadId
     */
    public void asyncDeleteAllMmsDraft(final long threadId) {
        if (threadId > 0) {
            Log.d(TAG, "asyncDeleteAllMmsDraft");
            final String where = Mms.THREAD_ID + "=" + threadId;
            asyncDelete(Mms.Draft.CONTENT_URI, where, null);
        }
    }
    
    // gionee zhouyj 2012-04-23 add for CR00573937 start
    public boolean isInComposePage(){
        return mInComposePage;
    }
    // gionee zhouyj 2012-04-23 add for CR00573937 end
    
    //gionee gaoj 2012-8-14 added for CR00623375 start
    public Uri getUri() {
        return mMessageUri;
    }

    public void setRegularly(boolean isregularly) {
        mIsRegularly = isregularly;
    }

    public void setRegularlyTime(long time) {
        mRegularlyTime = time;
    }

    private static SendReq makeSendReq(Conversation conv, CharSequence subject, long time) {
             String[] dests = conv.getRecipients().getNumbers(false);
             SendReq req = new SendReq();
             EncodedStringValue[] encodedNumbers = EncodedStringValue.encodeStrings(dests);
             if (encodedNumbers != null) {
                 req.setTo(encodedNumbers);
             }
             if (!TextUtils.isEmpty(subject)) {
                 req.setSubject(new EncodedStringValue(subject.toString()));
             }
             req.setDate(time / 1000L);
             return req;
         }
    //gionee gaoj 2012-8-14 added for CR00623375 end
    
    //Gionee <guoyx> <2013-05-17> add for CR00813219 begin
    public boolean isGnRegularMsg() {
        return MmsApp.mGnRegularlyMsgSend 
                && mIsRegularly && mRegularlyTime != -1;
    }
    
    public MessageSender getMmsMessageSender(Uri mmsUri, 
            SlideshowModel slideshow) {
        MessageSender sender;
        if (isGnRegularMsg()) {
            sender = new MmsMessageSender(mActivity, mmsUri, 
                    slideshow.getCurrentSlideshowSize(), mRegularlyTime);
        } else {
            sender = new MmsMessageSender(mActivity, mmsUri, 
                    slideshow.getCurrentSlideshowSize());
        }
        return sender;
    }
    // Aurora xuyong 2014-10-23 added for privacy feature start
    public MessageSender getMmsMessageSender(Uri mmsUri, 
            SlideshowModel slideshow, Conversation conv) {
        MessageSender sender;
        if (isGnRegularMsg()) {
            sender = new MmsMessageSender(mActivity, mmsUri, 
                    slideshow.getCurrentSlideshowSize(), mRegularlyTime, conv);
        } else {
            sender = new MmsMessageSender(mActivity, mmsUri, 
                    slideshow.getCurrentSlideshowSize(), conv);
        }
        return sender;
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end
    public MessageSender getSmsMessageSender(String msgText, 
            long threadId, String[] dests) {
        MessageSender sender;
        if (isGnRegularMsg()) {
            sender = new SmsMessageSender(mActivity, dests, msgText, 
                    threadId, mRegularlyTime);
        } else {
            sender = new SmsMessageSender(mActivity, dests, msgText, 
                    threadId);
        }
        return sender;
    }
    
    public MessageSender getSmsMessageSenderGemini(String msgText, 
            long threadId, int simId, String[] dests) {
        MessageSender sender;
        if (isGnRegularMsg()) {
            sender = new SmsMessageSender(mActivity, dests, msgText, 
                    threadId, simId, mRegularlyTime);
        } else {
            sender = new SmsMessageSender(mActivity, dests, msgText, 
                    threadId, simId);
        }
        return sender;
    }
    
    public SendReq getNewSendReq(final Conversation conv) {
        SendReq newsendReq;
        if (isGnRegularMsg()) {
            newsendReq = makeSendReq(conv, mSubject, mRegularlyTime);
        } else {
            newsendReq = makeSendReq(conv, mSubject);
        }
        return newsendReq;
    }
    //Gionee <guoyx> <2013-05-17> add for CR00813219 end
}
