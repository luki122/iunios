/*
 * Copyright (C) 2012 gionee Inc.
 *
 * Author:gaoj
 *
 * Description:class for holding the data of recent contact data from database
 *
 * history
 * name                              date                                      description
 *
 */

package com.android.mms.ui;

import android.content.Context;

import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;

import android.os.SystemProperties;
import android.util.Log;

import com.android.mms.MmsApp;
public class ConversationListItemData {
    private Conversation mConversation;
    private long mThreadId;
    private String mSubject;
    private String mDate;
    private boolean mHasAttachment;
    private boolean mIsRead;
    private boolean mHasError;
    private boolean mHasDraft;
    private int mMessageCount;
    private int mUnreadCount;
    
    private int mUnReadMessageCount;
    private int mSimId;
    
    //wappush: add mType variable
    private int mType;

    // The recipients in this conversation
    private ContactList mRecipients;
    private String mRecipientString;

    // the presence icon resource id displayed for the conversation thread.
    private int mPresenceResId;

    private boolean mHasEncryption;
    
    // gionee lwzh add for CR00706055 20121003 begin
    private boolean mIsGnDraft = false;
    // gionee lwzh add for CR00706055 20121003 end
    
    public ConversationListItemData(Context context, Conversation conv) {
        mConversation = conv;
        mThreadId = conv.getThreadId();
        mPresenceResId = 0;
        mSubject = conv.getSnippet();
        if (MmsApp.mGnMessageSupport) {
            mDate = MessageUtils.newformatGNTime(context, conv.getDate());
            mSimId = conv.getSimId();
            // mUnReadMessageCount = conv.getUnReadMessageCount(mThreadId,conv.getType());
            // just use it form ALPS.GB.FDD2.MP.V2
            mUnReadMessageCount = conv.getUnreadMessageCount();
        } else {
          // Aurora liugj 2013-09-24 modified for aurora's new feature start
            mDate = MessageUtils.formatAuroraTimeStampString(context, conv.getDate(), true);
          // Aurora liugj 2013-09-24 modified for aurora's new feature end
        }
        mIsRead = !conv.hasUnreadMessages();
        mHasError = conv.hasError();
        mHasDraft = conv.hasDraft();
        mMessageCount = conv.getMessageCount();
        mUnreadCount = conv.getUnreadMessageCount();
        mHasAttachment = conv.hasAttachment();
        
        //wappush: get the value for the mType variable
        mType = conv.getType();
        
        mHasEncryption = conv.getEncryption();
        
        updateRecipients();
    }

    public void updateRecipients() {
        mRecipients = mConversation.getRecipients();
        mRecipientString = mRecipients.formatNames(", ");
    }

    /**
     * @return Returns the ID of the thread.
     */
    public long getThreadId() {
        return mThreadId;
    }

    /**
     * @return Returns the date.
     */
    public String getDate() {
        return mDate;
    }

    /**
     * @return Returns the from.  (formatted for display)
     */
    public String getFrom() {
        return mRecipientString;
    }

    public ContactList getContacts() {
        return mRecipients;
    }

    public int getPresenceResourceId() {
        return mPresenceResId;
    }

    /**
     * @return Returns the subject.
     */
    public String getSubject() {
        return mSubject;
    }

    /**
     * @return Returns the hasAttachment.
     */
    public boolean hasAttachment() {
        return mHasAttachment;
    }
    
    //wappush: add the getType function
    /**
     * @return Returns the mType value.
     */
    public int getType() {
        return mType;
    }

    /**
     * @return Returns the isRead.
     */
    public boolean isRead() {
        return mIsRead;
    }

    /**
     * @return Whether the thread has a transmission error.
     */
    public boolean hasError() {
        return mHasError;
    }
    
    /**
     * @return simId.
     */
    public int getSimId() {
        return mSimId;
    }
    
    /**
     * @return Whether the thread has a draft.
     */
    public boolean hasDraft() {
        return mHasDraft;
    }

    /**
     * @return message count of the thread.
     */
    public int getMessageCount() {
        return mMessageCount;
    }

    
    /**
     * @return unread message count of the thread.
     */
    public int getUnReadMessageCount() {
        return mUnReadMessageCount;
    }

    public int getUnreadMessageCount() {
        return mUnreadCount;
    }
    
    public boolean hasEncryption() {
        return mHasEncryption;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "[ConversationHeader from:" + getFrom() + " subject:" + getSubject()
        + "]";
    }

    // gionee lwzh add for CR00706055 20121003 begin
    public void setGnDraft(boolean isGnDraft) {
        mIsGnDraft = isGnDraft;
    }

    public boolean isGnDraft() {
        return mIsGnDraft;
    }
    // gionee lwzh add for CR00706055 20121003 end
}
