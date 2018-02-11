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

package com.mediatek.contacts.calllog;

import com.android.contacts.R;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.android.contacts.PhoneCallDetails;
import com.android.contacts.PhoneCallDetailsHelper;
import com.android.contacts.model.AccountType;
import com.android.contacts.util.ExpirableCache;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog.Calls;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.android.common.widget.GroupingListAdapter;
import com.android.contacts.calllog.CallLogQuery;
import com.android.contacts.calllog.CallLogAdapter;
import com.android.contacts.calllog.CallLogQueryHandler;
import com.android.contacts.calllog.CallLogListItemViews;
import com.android.contacts.calllog.CallLogFragment;
import com.android.contacts.calllog.CallLogGroupBuilder;
import com.android.contacts.calllog.CallTypeHelper;
import com.android.contacts.calllog.CallTypeIconsView;
import com.android.contacts.calllog.ContactInfoHelper;
import com.android.contacts.calllog.ContactInfo;
import com.android.contacts.calllog.PhoneNumberHelper;
import com.android.contacts.calllog.IntentProvider;
import com.android.contacts.calllog.CallLogNotificationsService;
import com.android.contacts.calllog.CallLogAdapter.NumberWithCountryIso;

import com.mediatek.contacts.calllog.CallLogListItemView;


public class CallLogMultipleDeleteAdapter extends CallLogAdapter {

    private static final String LOG_TAG = "CallLogMultipleDeleteAdapter";
    private Cursor mCursor;

    private final Map<Integer, Integer> mSelectedCursorItemStatusMap =
                                                    new HashMap<Integer, Integer>();

    public CallLogMultipleDeleteAdapter(Context context, CallFetcher callFetcher,
            ContactInfoHelper contactInfoHelper, String voicemailNumber) {
        super(context, callFetcher, contactInfoHelper);
    }

    public void changeCursor(Cursor cursor) {
        log("changeCursor(), cursor = " + cursor);
        if(null != cursor) {
            log("cursor count = " + cursor.getCount());
        }
        if (mCursor != cursor) {
            mCursor = cursor;
        }
        super.changeCursor(cursor);
    }

    /*@Override
    public View newStandAloneView(Context context, ViewGroup parent) {
        log("newStandAloneView()");
        LayoutInflater inflater =
                (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.call_log_multiple_delete_list_item, parent, false);
        findAndCacheViews(view);
        return view;
    }

    @Override
    public View newChildView(Context context, ViewGroup parent) {
        log("newChildView()");
        LayoutInflater inflater =
                (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.call_log_multiple_delete_list_item, parent, false);
        findAndCacheViews(view);
        return view;
    }

    @Override
    public View newGroupView(Context context, ViewGroup parent) {
        log("newGroupView()");
        LayoutInflater inflater =
                (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.call_log_multiple_delete_list_item, parent, false);
        findAndCacheViews(view);
        return view;
    }*/

    /*protected void findAndCacheViews(View view) {
        log("findAndCacheViews()");
        // Get the views to bind to.
        CallLogMultipleDeleteListItemViews views = CallLogMultipleDeleteListItemViews.fromView(view);
        view.setTag(views);
    }*/

    /**
     * Binds the views in the entry to the data in the call log.
     *
     * @param view the view corresponding to this entry
     * @param c the cursor pointing to the entry in the call log
     * @param count the number of entries in the current item, greater than 1 if it is a group
     */
    protected void bindView(View view, Cursor c, int count) {
        log("bindView(), cursor = " + c + " count = " + count );
        /*final CallLogMultipleDeleteListItemViews views = (CallLogMultipleDeleteListItemViews) view.getTag();

        final String number = c.getString(CallLogQuery.NUMBER);
        final long date = c.getLong(CallLogQuery.DATE);
        final long duration = c.getLong(CallLogQuery.DURATION);
        final int callType = c.getInt(CallLogQuery.CALL_TYPE);
        final String countryIso = c.getString(CallLogQuery.COUNTRY_ISO);
        final int simId = c.getInt(CallLogQuery.SIM_ID);
        final int vtCall = c.getInt(CallLogQuery.VTCALL);

        log("-------------DB info:-------------");
        log("cursor position = " + c.getPosition());
        log("number = " + number);
        log("date = " + date );
        log("duration = " + duration );
        log("call type = " + callType);
        log("country iso = " + countryIso );
        log("sim id = " + simId);
        log("is vt call = " + (vtCall == 1 ? true : false));
        log("----------------------------------");

        final ContactInfo cachedContactInfo = getContactInfoFromCallLog(c);

        // Lookup contacts with this number
        NumberWithCountryIso numberCountryIso = new NumberWithCountryIso(number, countryIso);
        ExpirableCache.CachedValue<ContactInfo> cachedInfo =
                mContactInfoCache.getCachedValue(numberCountryIso);
        ContactInfo info = cachedInfo == null ? null : cachedInfo.getValue();
        if (!mPhoneNumberHelper.canPlaceCallsTo(number)
                || mPhoneNumberHelper.isVoicemailNumber(number)) {
            // If this is a number that cannot be dialed, there is no point in looking up a contact
            // for it.
            info = ContactInfo.EMPTY;
        } else if (cachedInfo == null) {
            mContactInfoCache.put(numberCountryIso, ContactInfo.EMPTY);
            // Use the cached contact info from the call log.
            info = cachedContactInfo;
            // The db request should happen on a non-UI thread.
            // Request the contact details immediately since they are currently missing.
            enqueueRequest(number, countryIso, cachedContactInfo, true);
            // We will format the phone number when we make the background request.
        } else {
            if (cachedInfo.isExpired()) {
                // The contact info is no longer up to date, we should request it. However, we
                // do not need to request them immediately.
                enqueueRequest(number, countryIso, cachedContactInfo, false);
            } else  if (!callLogInfoMatches(cachedContactInfo, info)) {
                // The call log information does not match the one we have, look it up again.
                // We could simply update the call log directly, but that needs to be done in a
                // background thread, so it is easier to simply request a new lookup, which will, as
                // a side-effect, update the call log.
                enqueueRequest(number, countryIso, cachedContactInfo, false);
            }

            if (info == ContactInfo.EMPTY) {
                // Use the cached contact info from the call log.
                info = cachedContactInfo;
            }
        }

        final Uri lookupUri = info.lookupUri;
        final String name = info.name;
        final int ntype = info.type;
        final String label = info.label;
        final long photoId = info.photoId;
        CharSequence formattedNumber = info.formattedNumber;
        final String geocode = c.getString(CallLogQuery.GEOCODED_LOCATION);
        final PhoneCallDetails details;
        if (TextUtils.isEmpty(name)) {
            details = new PhoneCallDetails(number, formattedNumber, countryIso, geocode,
                    callType, date, duration, simId, vtCall, count);
        } else {
            details = new PhoneCallDetails(number, formattedNumber, countryIso, geocode,
                    callType, date, duration, name, ntype, label, lookupUri, null, simId, vtCall, count);
        }

        // Need to check below "isNew" is needed or not
        final boolean isNew = c.getInt(CallLogQuery.IS_READ) == 0;
        // New items also use the highlighted version of the text.
        final boolean isHighlighted = isNew;
        mPhoneCallDetailsHelper.setPhoneCallDetails(views.phoneCallDetailsViews, details, isHighlighted);
        // Store id to view holder
        //views.id = c.getInt(CallLogQuery.ID);*/

        super.bindView(view, c, count);
        CallLogListItemView itemView = (CallLogListItemView) view;
        // set check box state
        Integer cursorPosition = mSelectedCursorItemStatusMap.get(c.getPosition());
        itemView.setCheckBoxMultiSel(false, false);
        final Boolean checkState = (null == cursorPosition) ? false : true;
        itemView.getCheckBoxMultiSel().setChecked(checkState);
        if (null != cursorPosition) {
            mSelectedCursorItemStatusMap.put(c.getPosition(), new Integer(count));
        }
        
        //setPhoto(views.quickContactView, photoId, lookupUri);
    }

    public int selectAllItems() {
        log("selectAllItems()");
        for (int i = 0; i < getCount(); ++ i) {
            int count = 0;
            // Below code also need check child type,
            // but current do not use child type view
            // so need consider it in the future
            if (isGroupHeader(i)) {
                count = getGroupSize(i);
            } else {
                count = 1;
            }
            Cursor cursor = (Cursor)getItem(i);
            mSelectedCursorItemStatusMap.put(new Integer(cursor.getPosition()), new Integer(count));
        }
        return mSelectedCursorItemStatusMap.size();
    }

    public void unSelectAllItems() {
        log("unSelectAllItems()");
        mSelectedCursorItemStatusMap.clear();
    }

    public String getDeleteFilter() {
        log("getDeleteFilter()");
        StringBuilder where = new StringBuilder("_id in ");
        where.append("(");
        if (mSelectedCursorItemStatusMap.size() > 0) {
            Iterator iterator = mSelectedCursorItemStatusMap.entrySet().iterator();
            Map.Entry entry = (Map.Entry) iterator.next();
            Integer key = (Integer) entry.getKey();
            Integer value = (Integer) entry.getValue();
            if (null == mCursor || !mCursor.moveToPosition(key)) {
                return "";
            }
            where.append("\'");
            where.append(mCursor.getInt(CallLogQuery.ID));
            where.append("\'");
            while (mCursor.moveToNext() && value > 1) {
                mCursor.getInt(CallLogQuery.ID);
                where.append(",");
                where.append("\'");
                where.append(mCursor.getInt(CallLogQuery.ID));
                where.append("\'");
                value--;
            }
            while (iterator.hasNext()) {
                entry = (Map.Entry) iterator.next();
                key = (Integer) entry.getKey();
                value = (Integer) entry.getValue();
                mCursor.moveToPosition(key);
                where.append(",");
                where.append("\'");
                where.append(mCursor.getInt(CallLogQuery.ID));
                where.append("\'");

                while (mCursor.moveToNext() && value > 1) {
                    mCursor.getInt(CallLogQuery.ID);
                    where.append(",");
                    where.append("\'");
                    where.append(mCursor.getInt(CallLogQuery.ID));
                    where.append("\'");
                    value--;
                }
            }
        } else {
            where.append(-1);
        }

        where.append(")");
        log("getDeleteFilter() where ==  " + where.toString());
        return where.toString();
    }

    public int changeSelectedStatusToMap(final int listPosition) {
        log("changeSelectedStatusToMap()");
        int count = 0;
        if (isGroupHeader(listPosition)) {
            count = getGroupSize(listPosition);
        } else {
            count = 1;
        }
        Cursor cursor = (Cursor)getItem(listPosition);
        if (null != cursor) {
            if (null == mSelectedCursorItemStatusMap.get(cursor.getPosition())) {
                mSelectedCursorItemStatusMap.put(new Integer(cursor.getPosition()), new Integer(count));
            } else {
                mSelectedCursorItemStatusMap.remove(new Integer(cursor.getPosition()));
            }
        }
        return mSelectedCursorItemStatusMap.size();
    }
    
    protected void bindCallButtonView(CallLogListItemView itemView, PhoneCallDetails details) {
        // override to prevent calling it in this class
    }
    
    public int getSelectedItemCount() {
        log("getSelectedItemCount()");
        return mSelectedCursorItemStatusMap.size();
    }

    private void log(final String log) {
        Log.i(LOG_TAG, log);
    }
}
