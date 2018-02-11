/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.contacts.calllog;

import com.android.common.widget.GroupingListAdapter;
// Gionee lihuafang 20120422 add for CR00573564 begin
import com.android.contacts.ContactsUtils;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
// Gionee lihuafang 20120422 add for CR00573564 end
import com.mediatek.contacts.simcontact.SimCardUtils;
import com.mediatek.contacts.util.OperatorUtils;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.PhoneCallDetails;
import com.android.contacts.PhoneCallDetailsHelper;
import com.android.contacts.PhoneCallDetailsViews;
import com.android.contacts.R;
import com.android.contacts.calllog.CallLogQuery;
import com.android.contacts.util.Constants;
import com.android.contacts.util.ExpirableCache;
import com.android.contacts.util.UriUtils;
import com.google.common.annotations.VisibleForTesting;

import android.content.Intent;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
//import android.provider.CallLog.Calls;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;


import libcore.util.Objects;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import com.mediatek.contacts.calllog.CallLogSubInfoHelper;
import com.mediatek.contacts.calllog.CallLogDateFormatHelper;
import com.mediatek.contacts.calllog.CallLogMultipleDeleteAdapter;
import com.mediatek.contacts.calllog.CallLogListItemView;
import com.mediatek.contacts.widget.GroupingListAdapterWithHeader;
import com.mediatek.contacts.widget.QuickContactBadgeWithPhoneNumber;


/**
 * Adapter class to fill in data for the Call Log.
 */
/**
 * Change Feature by Mediatek Begin. 
 * Original Android's Code: 
 *  (package) class CallLogAdapter extends GroupingListAdapter
 * Descriptions:
 */
public class CallLogAdapter extends GroupingListAdapterWithHeader
/**
 * Change Feature by Mediatek End.
 */
implements CallLogGroupBuilder.GroupCreator, OnScrollListener{
    /** Interface used to initiate a refresh of the content. */
    public interface CallFetcher {
        public void fetchCalls();
    }

    /**
     * Stores a phone number of a call with the country code where it originally occurred.
     * <p>
     * Note the country does not necessarily specifies the country of the phone number itself, but
     * it is the country in which the user was in when the call was placed or received.
     */
    public static final class NumberWithCountryIso {
         
         
        private final String number;
        private final String countryIso;

        private NumberWithCountryIso(String number, String countryIso) {
            this.number = number;
            this.countryIso = countryIso;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (!(o instanceof NumberWithCountryIso)) return false;
            NumberWithCountryIso other = (NumberWithCountryIso) o;
            return TextUtils.equals(number, other.number)
                    && TextUtils.equals(countryIso, other.countryIso);
        }

        @Override
        public int hashCode() {
            return (number == null ? 0 : number.hashCode())
                    ^ (countryIso == null ? 0 : countryIso.hashCode());
        }
    }

    /** The size of the cache of contact info. */
    private static final int CONTACT_INFO_CACHE_SIZE = 100;

    private final Context mContext;
    private final ContactInfoHelper mContactInfoHelper;
    private final CallFetcher mCallFetcher;

    /**
     * A cache of the contact details for the phone numbers in the call log.
     * <p>
     * The content of the cache is expired (but not purged) whenever the application comes to
     * the foreground.
     * <p>
     * The key is number with the country in which the call was placed or received.
     */

    private volatile boolean mDone;
    private boolean mLoading = true;

    /** Instance of helper class for managing views. */
    private final CallLogListItemHelper mCallLogViewsHelper;

    /** Helper to set up contact photos. */
    private final ContactPhotoManager mContactPhotoManager;
     
    /** Helper to parse and process phone numbers. */
    private PhoneNumberHelper mPhoneNumberHelper;
    /** Helper to group call log entries. */
    private final CallLogGroupBuilder mCallLogGroupBuilder;

    /** Can be set to true by tests to disable processing of requests. */
    private volatile boolean mRequestProcessingDisabled = false;
    
    private Cursor mCursor;

    /** Listener for the secondary action in the list, either call or play. */
    private final View.OnClickListener mSecondaryActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            IntentProvider intentProvider = (IntentProvider) view.getTag();
            if (intentProvider != null) {
                /**
                 * Change Feature by Mediatek Begin.
                 *  Original Android's Code:
                 * mContext.startActivity(intentProvider.getIntent(mContext));
                 * Descriptions:
                 */
                mContext.startActivity(intentProvider.getIntent(mContext).putExtra(
                        Constants.EXTRA_FOLLOW_SIM_MANAGEMENT, true));
                /**
                 * Change Feature by Mediatek End.
                 */
   
            }
        }
    };

    public CallLogAdapter(Context context, CallFetcher callFetcher,
            ContactInfoHelper contactInfoHelper) {
        super(context);

        mContext = context;
        mCallFetcher = callFetcher;
        mContactInfoHelper = contactInfoHelper;

        Resources resources = mContext.getResources();
        CallTypeHelper callTypeHelper = new CallTypeHelper(resources);

        mContactPhotoManager = ContactPhotoManager.getInstance(mContext);
        mPhoneNumberHelper = new PhoneNumberHelper(resources);
        /**
         * Change Feature by Mediatek Begin. 
         * Original Android's Code:
         * PhoneCallDetailsHelper phoneCallDetailsHelper = new
         * PhoneCallDetailsHelper( resources, callTypeHelper,
         * mPhoneNumberHelper); 
         * mCallLogViewsHelper = new CallLogListItemHelper(
         * phoneCallDetailsHelper, mPhoneNumberHelper, resources); 
         * Descriptions:
         */
        mCallLogSubInfoHelper = new CallLogSubInfoHelper(resources);
        mPhoneCallDetailsHelper = new PhoneCallDetailsHelper(resources, callTypeHelper,
                mPhoneNumberHelper, mCallLogSubInfoHelper, mContext);
        mCallLogViewsHelper = new CallLogListItemHelper(mPhoneCallDetailsHelper,
                mPhoneNumberHelper, resources);
        /**
         * Change Feature by Mediatek End.
         */
       
        mCallLogGroupBuilder = new CallLogGroupBuilder(this);
        if (null == mContactInfoMap) {
            mContactInfoMap = new HashMap<String, ContactInfo>();
        } else {
            mContactInfoMap.clear();
        }
    }

    /**
     * Requery on background thread when {@link Cursor} changes.
     */
    @Override
    protected void onContentChanged() {
        mCallFetcher.fetchCalls();
    }

    public void setLoading(boolean loading) {
        mLoading = loading;
    }

    @Override
    public boolean isEmpty() {
        if (mLoading) {
            // We don't want the empty state to show when loading.
            return false;
        } else {
            return super.isEmpty();
        }
    }

    @Override
    protected void addGroups(Cursor cursor) {
        log("addGroups(), cursor = " + cursor);
        mCallLogGroupBuilder.addGroups(cursor);
    }

    @Override
    protected View newStandAloneView(Context context, ViewGroup parent) {
        return newCallLogItemView(context, parent);
    }

    @Override
    protected void bindStandAloneView(View view, Context context, Cursor cursor) {
        bindView(view, cursor, 1);
    }

    @Override
    protected View newChildView(Context context, ViewGroup parent) {
    	return newCallLogItemView(context, parent);
    }

    @Override
    protected void bindChildView(View view, Context context, Cursor cursor) {
        bindView(view, cursor, 1);
    }

    @Override
    protected View newGroupView(Context context, ViewGroup parent) {
    	return newCallLogItemView(context, parent);
    }

    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, int groupSize,
            boolean expanded) {
        log("bindGroupView()");
        bindView(view, cursor, groupSize);
    }

    /**
     * Binds the views in the entry to the data in the call log.
     *
     * @param view the view corresponding to this entry
     * @param c the cursor pointing to the entry in the call log
     * @param count the number of entries in the current item, greater than 1 if it is a group
     */
    protected void bindView(View view, Cursor c, int count) {
    	log(" ContactsPerf CallLogAdapter bindView()");
        log("bindView(), cursor = " + c + " count = " + count);

        if (!(view instanceof CallLogListItemView)) {
            log("Error!!! - bindView(): view is not CallLogListItemView!");
            return;
        }
        CallLogListItemView itemView = (CallLogListItemView) view;

       
        ContactInfo contactInfo = getContactInfo(c);
        
        itemView.setTag(
                IntentProvider.getCallDetailIntentProvider(
                        this, c.getPosition(), c.getLong(CallLogQuery.ID), count));

        String sDate = null;
        if (isDateGroupHeader(c.getPosition())) {
            sDate = CallLogDateFormatHelper.getFormatedDateText(mContext, contactInfo.date);
            itemView.setSectionDate(sDate);
        } else {
            itemView.setSectionDate(null);
        }
        final PhoneCallDetails details;
        if (TextUtils.isEmpty(contactInfo.name)) {
            /**
             * Change Feature by Mediatek Begin. 
             * Original Android's Code:
             * details = new PhoneCallDetails(number, formattedNumber,
             * countryIso, geocode, callTypes, date, duration); 
             * Descriptions:
             */      
            details = new PhoneCallDetails(contactInfo.number, contactInfo.formattedNumber, 
                                           contactInfo.countryIso, contactInfo.geocode, 
                                           contactInfo.type, contactInfo.date,
                                           contactInfo.duration, contactInfo.simId,
                                           count);
            /**
             * Change Feature by Mediatek End.
             */
        } else {
            // We do not pass a photo id since we do not need the high-res
            // picture.
            /**
             * Change Feature by Mediatek Begin. 
             * Original Android's Code:
             * details = new PhoneCallDetails(number, formattedNumber,
             * countryIso, geocode, callTypes, date, duration, name, ntype,
             * label, lookupUri, null); 
             * Descriptions:
             */

            details = new PhoneCallDetails(contactInfo.number, contactInfo.formattedNumber, 
                                           contactInfo.countryIso, contactInfo.geocode,
                                           contactInfo.type, contactInfo.date,
                                           contactInfo.duration, contactInfo.name,
                                           contactInfo.nNumberTypeId, contactInfo.label,
                                           contactInfo.lookupUri, null, contactInfo.simId,
                                           count);
            /**
             * Change Feature by Mediatek End.
             */

        }
        //final boolean isNew = c.getInt(CallLogQuery.CALLS_JOIN_DATA_VIEW_IS_READ) == 0;
        // New items also use the highlighted version of the text.
        final boolean isHighlighted = contactInfo.isRead == 0;
        final boolean isEmergencyNumber = mPhoneNumberHelper.isEmergencyNumber(details.number);
        final boolean isVoiceMailNumber = mPhoneNumberHelper.isVoiceMailNumberForMtk(details.number, details.simId);
        final boolean isSipCallNumber = mPhoneNumberHelper.isSipNumber(details.number);
        mPhoneCallDetailsHelper.setPhoneCallDetails(itemView, details, isHighlighted,
                                                    isEmergencyNumber, isVoiceMailNumber);
        //mCallLogViewsHelper.setPhoneCallDetails(itemView, details, isHighlighted);
        bindCallButtonView(itemView, details);
        /**
         * Change Feature by Mediatek Begin. 
         * Original Android's Code:
         * setPhoto(views, photoId, lookupUri); 
         * Descriptions:
         */
        if (isEmergencyNumber || isVoiceMailNumber) {
            contactInfo.photoId = 0;
            contactInfo.lookupUri = null;
        } else if (contactInfo.contactSimId > 0) {
            int slotId = ContactsUtils.getSlotBySubId(contactInfo.contactSimId);
            if (OperatorUtils.getOptrProperties().equals("OP02")) {
                contactInfo.photoId = (slotId == 0) ? -3 : -4;
            } else {
                contactInfo.photoId = SimCardUtils.isSimUsimType(contactInfo.contactSimId) ? -2 : -1;
            }
        }
        
		if (contactInfo.lookupUri != null) {
			setPhoto(itemView.getQuickContact(), contactInfo.photoId,
					contactInfo.lookupUri);
		} else {
			setPhoto(itemView.getQuickContact(), contactInfo.photoId,
					contactInfo.number, isSipCallNumber);
		}
		/**
		 * Change Feature by Mediatek End.
		 */
	}

    /** Returns true if this is the last item of a section. */
    /*
     * private boolean isLastOfSection(Cursor c) { if (c.isLast()) return true;
     * final int section = c.getInt(CallLogQuery.SECTION); if (!c.moveToNext())
     * return true; final int nextSection = c.getInt(CallLogQuery.SECTION);
     * c.moveToPrevious(); 
     * return section != nextSection; 
     * }
     */

    /**
     * Checks whether the contact info from the call log matches the one from
     * the contacts db.
     */
    protected boolean callLogInfoMatches(ContactInfo callLogInfo, ContactInfo info) {
        // The call log only contains a subset of the fields in the contacts db.
        // Only check those.
        return TextUtils.equals(callLogInfo.name, info.name)
                && callLogInfo.type == info.type
                && TextUtils.equals(callLogInfo.label, info.label);
    }

    protected ContactInfo getContactInfo(Cursor c) {
        ContactInfo info = null;

		String hashKey = c.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_NUMBER)
				+ c.getInt(CallLogQuery.CALLS_JOIN_DATA_VIEW_DATE);
        info = mContactInfoMap.get(hashKey);
        if (null == info) {
            info = getContactInfoFromCallLog(c);
            mContactInfoMap.put(hashKey, info);
        }

        return info;
    }
    

    /** Returns the contact information as stored in the call log. */
    protected ContactInfo getContactInfoFromCallLog(Cursor c) {
        ContactInfo info = ContactInfo.fromCursor(c);
//        info.lookupUri = UriUtils.parseUriOrNull(c.getString(CallLogQuery.CACHED_LOOKUP_URI));
//        info.name = c.getString(CallLogQuery.CACHED_NAME);
//        info.type = c.getInt(CallLogQuery.CACHED_NUMBER_TYPE);
//        info.label = c.getString(CallLogQuery.CACHED_NUMBER_LABEL);
//        String matchedNumber = c.getString(CallLogQuery.CACHED_MATCHED_NUMBER);
//        info.number = matchedNumber == null ? c.getString(CallLogQuery.NUMBER) : matchedNumber;
//        info.normalizedNumber = c.getString(CallLogQuery.CACHED_NORMALIZED_NUMBER);
//        info.photoId = c.getLong(CallLogQuery.CACHED_PHOTO_ID);
//        info.photoUri = null;  // We do not cache the photo URI.
//        info.formattedNumber = c.getString(CallLogQuery.CACHED_FORMATTED_NUMBER);
        return info;
    }

    /**
     * Returns the call types for the given number of items in the cursor.
     * <p>
     * It uses the next {@code count} rows in the cursor to extract the types.
     * <p>
     * It position in the cursor is unchanged by this function.
     */
    /*
     * protected int[] getCallTypes(Cursor cursor, int count) { int position =
     * cursor.getPosition(); int[] callTypes = new int[count]; for (int index =
     * 0; index < count; ++index) { callTypes[index] =
     * cursor.getInt(CallLogQuery.CALL_TYPE); cursor.moveToNext(); }
     * cursor.moveToPosition(position);
     * return callTypes; 
     * }
     */

    private void setPhoto(CallLogListItemViews views, long photoId, Uri contactUri) {
        views.quickContactPhoto.assignContactUri(contactUri);
        mContactPhotoManager.loadPhoto(views.quickContactPhoto, photoId, false, true);
    }

    /**
     * Sets whether processing of requests for contact details should be enabled.
     * <p>
     * This method should be called in tests to disable such processing of requests when not
     * needed.
     */
    @VisibleForTesting
    void disableRequestProcessingForTest() {
        mRequestProcessingDisabled = true;
    }

    @VisibleForTesting
    void injectContactInfoForTest(String number, String countryIso, ContactInfo contactInfo) {
        NumberWithCountryIso numberCountryIso = new NumberWithCountryIso(number, countryIso);
    }

    @Override
    public void addGroup(int cursorPosition, int size, boolean expanded) {
        super.addGroup(cursorPosition, size, expanded);
    }

    @Override
    public void setGroupHeaderPosition(int cursorPosition) {
        super.setGroupHeaderPosition(cursorPosition);
    }

    /*
     * Get the number from the Contacts, if available, since sometimes
     * the number provided by caller id may not be formatted properly
     * depending on the carrier (roaming) in use at the time of the
     * incoming call.
     * Logic : If the caller-id number starts with a "+", use it
     *         Else if the number in the contacts starts with a "+", use that one
     *         Else if the number in the contacts is longer, use that one
     */
    public String getBetterNumberFromContacts(String number, String countryIso) {
        String matchingNumber = null;
        // Look in the cache first. If it's not found then query the Phones db
        NumberWithCountryIso numberCountryIso = new NumberWithCountryIso(number, countryIso);
        ContactInfo ci = null;
        if (ci != null && ci != ContactInfo.EMPTY) {
            matchingNumber = ci.number;
        } else {
            try {
                Cursor phonesCursor = mContext.getContentResolver().query(
                        Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, number),
                        PhoneQuery._PROJECTION, null, null, null);
                if (phonesCursor != null) {
                    if (phonesCursor.moveToFirst()) {
                        matchingNumber = phonesCursor.getString(PhoneQuery.MATCHED_NUMBER);
                    }
                    phonesCursor.close();
                }
            } catch (Exception e) {
                // Use the number from the call log
            }
        }
        if (!TextUtils.isEmpty(matchingNumber) &&
                (matchingNumber.startsWith("+")
                        || matchingNumber.length() > number.length())) {
            number = matchingNumber;
        }
        return number;
    }

    // The following lines are provided and maintained by Mediatek Inc.
    
    private  int simId;

     
    private final PhoneCallDetailsHelper mPhoneCallDetailsHelper;
    private final CallLogSubInfoHelper mCallLogSubInfoHelper;
    private HashMap<String, ContactInfo> mContactInfoMap = null; 

    protected void setPhoto(QuickContactBadgeWithPhoneNumber view, long photoId, Uri contactUri) {
    	view.assignPhoneNumber(null,false);
        view.assignContactUri(contactUri);
        mContactPhotoManager.loadPhoto(view, photoId, false, true);
    }
    
    protected void setPhoto(QuickContactBadgeWithPhoneNumber view, long photoId, String number, boolean isSipCallNumber) {
        view.assignContactUri(null);
    	view.assignPhoneNumber(number, isSipCallNumber);
        mContactPhotoManager.loadPhoto(view, photoId, false, true);
    }

    private static final String TAG = "CallLogAdapter";

    private void log(final String log) {
        Log.i(TAG, log);
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
        // TODO Auto-generated method stub
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // TODO Auto-generated method stub
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            mContactPhotoManager.resume();
        } else {
            mContactPhotoManager.pause();
        }
    }

    public void clearCachedContactInfo() {
        mContactInfoMap.clear();
    }

    protected View newCallLogItemView(Context context, ViewGroup parent) {
        CallLogListItemView view = new CallLogListItemView(context, null);
        view.setOnCallButtonClickListener(mSecondaryActionListener);
        return view;
    }
    
    protected void bindCallButtonView(CallLogListItemView itemView, PhoneCallDetails details) {
        // Store away the voicemail information so we can play it directly.
        /*if (callType == Calls.VOICEMAIL_TYPE) {
            String voicemailUri = c.getString(CallLogQuery.VOICEMAIL_URI);
            final long rowId = c.getLong(CallLogQuery.ID);
            itemView.getCallButton().setTag(
                    IntentProvider.getPlayVoicemailIntentProvider(rowId, voicemailUri, (long)simId));
        } else*/ if (!TextUtils.isEmpty(details.number)) {
            // Store away the number so we can call it directly if you click on the call icon.
            itemView.getCallButton().setTag(
                    IntentProvider.getReturnCallIntentProvider((String)details.number, (long)details.simId));
        } else {
            // No action enabled.
            itemView.getCallButton().setTag(null);
        }
        boolean canCall = PhoneNumberHelper.canPlaceCallsTo(details.number);

        /**
        * Change Feature by Mediatek Begin.
        * Original Android's Code:
         boolean canPlay = details.callTypes[0] == Calls.VOICEMAIL_TYPE;
        * Descriptions:No Voice mail funcition
        */
        //boolean canPlay = false;
        /**
        * Change Feature by Mediatek End.
        */
        /*if (canPlay) {
            // Playback action takes preference.
            configurePlaySecondaryAction(views, isHighlighted);
        } else*/ if (canCall) {
            // Call is the secondary action.
            configureCallSecondaryAction(itemView, details);
            itemView.getCallButton().setVisibility(View.VISIBLE);
        } else {
            // No action available.
            // Here should consider again because multple Delete Adapter
            // does not have call button
            itemView.getCallButton().setVisibility(View.GONE);
        }
    }

    /** Sets the secondary action to correspond to the call button. */
    private void configureCallSecondaryAction(CallLogListItemView views,
            PhoneCallDetails details) {
        views.getCallButton().setContentDescription(getCallActionDescription(details));
    }
    
    /** Returns the description used by the call action for this phone call. */
    private CharSequence getCallActionDescription(PhoneCallDetails details) {
        final CharSequence recipient;
        if (!TextUtils.isEmpty(details.name)) {
            recipient = details.name;
        } else {
            recipient = mPhoneNumberHelper.getDisplayNumber(
                    details.number, details.formattedNumber);
        }
        return mContext.getResources().getString(R.string.description_call, recipient);
    }
    
    public void changeCursor(Cursor cursor) {
        log("changeCursor(), cursor = " + cursor);
        if (mCursor != cursor) {
            mCursor = cursor;
            mContactInfoMap.clear();
        }
        super.changeCursor(cursor);
    }
    // The previous lines are provided and maintained by Mediatek Inc.
}
