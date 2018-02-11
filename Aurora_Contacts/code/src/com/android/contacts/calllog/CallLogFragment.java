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

import com.android.common.io.MoreCloseables;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import com.mediatek.contacts.ContactsFeatureConstants;
import com.mediatek.contacts.activities.CallLogMultipleDeleteActivity;
import com.android.contacts.activities.DialtactsActivity;
import com.android.contacts.activities.DialtactsActivity.ViewPagerVisibilityListener;
import com.android.contacts.util.EmptyLoader;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.test.NeededForTesting;
import com.android.contacts.voicemail.VoicemailStatusHelper;
import com.android.contacts.voicemail.VoicemailStatusHelper.StatusMessage;
import com.android.contacts.voicemail.VoicemailStatusHelperImpl;
import com.android.internal.telephony.ITelephony;
import com.google.common.annotations.VisibleForTesting;
//import com.android.contacts.widget;

import android.app.Activity;
import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import aurora.preference.AuroraPreferenceManager; // import android.preference.PreferenceManager;
import android.provider.CallLog.Calls;
import android.telephony.PhoneNumberUtils;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import aurora.widget.AuroraButton;
import aurora.widget.AuroraListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import java.util.List;

import com.android.contacts.util.Constants;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.mediatek.contacts.widget.SimPickerDialog;
import com.mediatek.contacts.calllog.CallLogListItemView;
import com.mediatek.contacts.calllog.CallLogSimInfoHelper;
import com.android.contacts.calllog.CallLogAdapter;
import com.android.contacts.calllog.PhoneNumberHelper;
// The following lines are provided and maintained by Mediatek Inc.
import com.mediatek.contacts.list.CallLogUnavailableFragment;
import android.widget.ProgressBar;
import android.view.animation.AnimationUtils;

// The previous lines are provided and maintained by Mediatek Inc.
/**
 * Displays a list of call log entries.
 */
public class CallLogFragment extends ListFragment implements ViewPagerVisibilityListener,
        CallLogQueryHandler.Listener, CallLogAdapter.CallFetcher, View.OnClickListener {

    private static final String TAG = "CallLogFragment";

    /**
     * ID of the empty loader to defer other fragments.
     */
    private static final int EMPTY_LOADER_ID = 0;
    private static final int TAB_INDEX_CALL_LOG = 1;

    private CallLogAdapter mAdapter;
    private CallLogQueryHandler mCallLogQueryHandler;
    private boolean mScrollToTop;

    private boolean mShowOptionsMenu;
    /** Whether there is at least one voicemail source installed. */
    private boolean mVoicemailSourcesAvailable = false;
    /** Whether we are currently filtering over voicemail. */
    private boolean mShowingVoicemailOnly = false;

    private VoicemailStatusHelper mVoicemailStatusHelper;
    private View mStatusMessageView;
    private TextView mStatusMessageText;
    private TextView mStatusMessageAction;
    private KeyguardManager mKeyguardManager;

    private boolean mEmptyLoaderRunning;
    private boolean mCallLogFetched;
    private boolean mVoicemailStatusFetched;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        //For performance auto test filter
        //Aurora liumx 20130828 modify for remove Mediatek Xlog begin
        Log.i("Xlog:"+TAG,"[Performance test][Contacts] loading data start time: ["
                + System.currentTimeMillis() + "]" );
        //Aurora liumx 20130828 modify for remove Mediatek Xlog end
        
        mCallLogQueryHandler = new CallLogQueryHandler(getActivity().getContentResolver(), this);
        mKeyguardManager =
                (KeyguardManager) getActivity().getSystemService(Context.KEYGUARD_SERVICE);
        setHasOptionsMenu(true);
        //The following lines are provided and maintained by Mediatek Inc.
        SIMInfoWrapper.getDefault().registerForSimInfoUpdate(mHandler, SIM_INFO_UPDATE_MESSAGE, null);
        mCallLogChangeObserver = new CallLogChangeObserver();
        getActivity().getContentResolver().registerContentObserver(Uri.parse("content://call_log/calls/"),
                true, mCallLogChangeObserver);
        //The previous lines are provided and maintained by Mediatek Inc.
    }

    /** Called by the CallLogQueryHandler when the list of calls has been fetched or updated. */
    @Override
    public void onCallsFetched(Cursor cursor) {
        log("onCallsFetched(), cursor = " + cursor);
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        mAdapter.setLoading(false);
        mAdapter.changeCursor(cursor);
        // This will update the state of the "Clear call log" menu item.
        getActivity().invalidateOptionsMenu();
        if (mScrollToTop) {
            final AuroraListView listView = (AuroraListView)getListView();
            /**
             * Change Feature by Mediatek Begin.
             *  Original Android's Code: 
             * if (listView.getFirstVisiblePosition() > 5) {
             * listView.setSelection(5); } 
             * listView.smoothScrollToPosition(0);
             * Descriptions:
             */
            log("onCallsFetched() The listview will go to first position");
            listView.setSelection(0);
            /**
             * Change Feature by Mediatek End.
             */
            
            mScrollToTop = false;
        }
        mCallLogFetched = true;
        /*
         * Bug Fix by Mediatek Begin. Original Android's code: CR ID:
         * ALPS00115673 Descriptions: add wait cursor
         */

        Log.i(TAG, "onCallsFetched is call");
        isFinished = true;
        mLoadingContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                android.R.anim.fade_out));
        mLoadingContainer.setVisibility(View.GONE);
        mLoadingContact.setVisibility(View.GONE);
        mProgress.setVisibility(View.GONE);
                
        mEmptyTitle.setText(R.string.recentCalls_empty);
        
        /*
         * Bug Fix by Mediatek End.
         */

        destroyEmptyLoaderIfAllDataFetched();
    }

    /**
     * Called by {@link CallLogQueryHandler} after a successful query to voicemail status provider.
     */
    @Override
    public void onVoicemailStatusFetched(Cursor statusCursor) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        updateVoicemailStatusMessage(statusCursor);

        int activeSources = mVoicemailStatusHelper.getNumberActivityVoicemailSources(statusCursor);
        setVoicemailSourcesAvailable(activeSources != 0);
        MoreCloseables.closeQuietly(statusCursor);
        mVoicemailStatusFetched = true;
        destroyEmptyLoaderIfAllDataFetched();
    }

    private void destroyEmptyLoaderIfAllDataFetched() {
        if (mCallLogFetched && mVoicemailStatusFetched && mEmptyLoaderRunning) {
            mEmptyLoaderRunning = false;
            getLoaderManager().destroyLoader(EMPTY_LOADER_ID);
        }
    }

    /** Sets whether there are any voicemail sources available in the platform. */
    private void setVoicemailSourcesAvailable(boolean voicemailSourcesAvailable) {
        if (mVoicemailSourcesAvailable == voicemailSourcesAvailable) return;
        mVoicemailSourcesAvailable = voicemailSourcesAvailable;

        Activity activity = getActivity();
        if (activity != null) {
            // This is so that the options menu content is updated.
            activity.invalidateOptionsMenu();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View view = inflater.inflate(R.layout.call_log_fragment, container, false);
        //mVoicemailStatusHelper = new VoicemailStatusHelperImpl();
        //mStatusMessageView = view.findViewById(R.id.voicemail_status);
        //mStatusMessageText = (TextView) view.findViewById(R.id.voicemail_status_message);
        //mStatusMessageAction = (TextView) view.findViewById(R.id.voicemail_status_action);

        //The following lines are provided and maintained by Mediatek Inc.
        typeFilter_all = (AuroraButton) view.findViewById(R.id.btn_type_filter_all);
        typeFilter_outgoing = (AuroraButton) view.findViewById(R.id.btn_type_filter_outgoing);
        typeFilter_incoming = (AuroraButton) view.findViewById(R.id.btn_type_filter_incoming);
        typeFilter_missed = (AuroraButton) view.findViewById(R.id.btn_type_filter_missed);
        typeFilter_all.setOnClickListener(this);
        typeFilter_outgoing.setOnClickListener(this);
        typeFilter_incoming.setOnClickListener(this);
        typeFilter_missed.setOnClickListener(this);
        /*
         * Bug Fix by Mediatek Begin. Original Android's code: CR ID:
         * ALPS00115673 Descriptions: add wait cursor
         */

        mLoadingContainer = view.findViewById(R.id.loading_container);
        mLoadingContainer.setVisibility(View.GONE);
        mEmptyTitle = (TextView) view.findViewById(android.R.id.empty);
        mEmptyTitle.setText(R.string.recentCalls_empty);
        mLoadingContact = (TextView) view.findViewById(R.id.loading_contact);
        mLoadingContact.setVisibility(View.GONE);
        mProgress = (ProgressBar) view.findViewById(R.id.progress_loading_contact);
        mProgress.setVisibility(View.GONE);
        /*
         * Bug Fix by Mediatek End.
         */

        SharedPreferences.Editor editor = AuroraPreferenceManager.getDefaultSharedPreferences(
                this.getActivity()).edit();
        editor.putInt(Constants.TYPE_FILTER_PREF, Constants.FILTER_TYPE_ALL);
        changeButton(typeFilter_all);
        editor.commit();
        //The previous lines are provided and maintained by Mediatek Inc.
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String currentCountryIso = ContactsUtils.getCurrentCountryIso(getActivity());
        mAdapter = new CallLogAdapter(getActivity(), this,
                new ContactInfoHelper(getActivity(), currentCountryIso));
        setListAdapter(mAdapter);
        final AuroraListView listView = (AuroraListView)getListView();
        if (null != listView) {
            listView.setItemsCanFocus(true);
            listView.setOnScrollListener(mAdapter);
        }
    }

    @Override
    public void onStart() {
        //mScrollToTop = true;

        // Start the empty loader now to defer other fragments.  We destroy it when both calllog
        // and the voicemail status are fetched.
        getLoaderManager().initLoader(EMPTY_LOADER_ID, null,
                new EmptyLoader.Callback(getActivity()));
        mEmptyLoaderRunning = true;
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        PhoneNumberHelper.getVoiceMailNumber();
        refreshData();
        SharedPreferences prefs = AuroraPreferenceManager.getDefaultSharedPreferences(this.getActivity());
        prefs.getInt(Constants.SIM_FILTER_PREF, Constants.FILTER_SIM_DEFAULT);
        prefs.getInt(Constants.TYPE_FILTER_PREF, Constants.FILTER_TYPE_DEFAULT);

        //Aurora liumx 20130828 modify for remove Mediatek Xlog begin
        Log.i("Xlog:"+TAG,"[Performance test][Contacts] loading data end time: ["
                + System.currentTimeMillis() + "]" );
        //Aurora liumx 20130828 modify for remove Mediatek Xlog end
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        if ((null == v) || (!(v instanceof CallLogListItemView))) {
            new Exception("CallLogFragment exception").printStackTrace();
            return;
        }
        Context context = this.getActivity();
        IntentProvider intentProvider = (IntentProvider) v.getTag();
        if (intentProvider != null) {
            context.startActivity(intentProvider.getIntent(context).putExtra(
                    Constants.EXTRA_FOLLOW_SIM_MANAGEMENT, true));
        }
    }

    private void updateVoicemailStatusMessage(Cursor statusCursor) {
        List<StatusMessage> messages = mVoicemailStatusHelper.getStatusMessages(statusCursor);
        if (messages.size() == 0) {
            mStatusMessageView.setVisibility(View.GONE);
        } else {
            mStatusMessageView.setVisibility(View.VISIBLE);
            // TODO: Change the code to show all messages. For now just pick the first message.
            final StatusMessage message = messages.get(0);
            if (message.showInCallLog()) {
                mStatusMessageText.setText(message.callLogMessageId);
            }
            if (message.actionMessageId != -1) {
                mStatusMessageAction.setText(message.actionMessageId);
            }
            if (message.actionUri != null) {
                mStatusMessageAction.setVisibility(View.VISIBLE);
                mStatusMessageAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().startActivity(
                                new Intent(Intent.ACTION_VIEW, message.actionUri));
                    }
                });
            } else {
                mStatusMessageAction.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Kill the requests thread
    }

    @Override
    public void onStop() {
        super.onStop();
        updateOnExit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter.changeCursor(null);
        SIMInfoWrapper.getDefault().unregisterForSimInfoUpdate(mHandler);
        getActivity().getContentResolver().unregisterContentObserver(mCallLogChangeObserver);
    }

    @Override
    public void fetchCalls() {
        if (mShowingVoicemailOnly) {
            mCallLogQueryHandler.fetchVoicemailOnly();
        } else {
            /**
            * Change Feature by Mediatek Begin.
            * Original Android's Code:
            mCallLogQueryHandler.fetchAllCalls();
            * Descriptions:
            */
        	Activity activity = this.getActivity();
        	if(activity == null){
        		Log.e(TAG, " fetchCalls(), but this.getActivity() is null, use default value");
        		mCallLogQueryHandler.fetchCallsJionDataView(Constants.FILTER_SIM_DEFAULT, Constants.FILTER_TYPE_DEFAULT);
        	}else{
        		SharedPreferences prefs = AuroraPreferenceManager.getDefaultSharedPreferences(this.getActivity());
        		int simFilter = prefs.getInt(Constants.SIM_FILTER_PREF, Constants.FILTER_SIM_DEFAULT);
        		int typeFilter = prefs.getInt(Constants.TYPE_FILTER_PREF, Constants.FILTER_TYPE_DEFAULT);
        		mCallLogQueryHandler.fetchCallsJionDataView(simFilter, typeFilter);
        	}
            /**
            * Change Feature by Mediatek End.
            */
        }
    }

    public void startCallsQuery() {
        log("startCallsQuery()");
        mAdapter.setLoading(true);
        /**
        * Change Feature by Mediatek Begin.
        * Original Android's Code:
         mCallLogQueryHandler.fetchAllCalls();
        * Descriptions:
        */
        SharedPreferences prefs = AuroraPreferenceManager.getDefaultSharedPreferences(this.getActivity());
        int simFilter = prefs.getInt(Constants.SIM_FILTER_PREF, Constants.FILTER_SIM_DEFAULT);
        int typeFilter = prefs.getInt(Constants.TYPE_FILTER_PREF, Constants.FILTER_TYPE_DEFAULT);
        mCallLogQueryHandler.fetchCallsJionDataView(simFilter, typeFilter);
        /**
        * Change Feature by Mediatek End.
        */
        if (mShowingVoicemailOnly) {
            mShowingVoicemailOnly = false;
            getActivity().invalidateOptionsMenu();
        }
        /*
         * Bug Fix by Mediatek Begin. Original Android's code: CR ID:
         * ALPS00115673 Descriptions: add wait cursor
         */
        int i = this.getListView().getCount();
       
        Log.i(TAG, "***********************i : " + i);
        isFinished = false;
        if (i == 0) {
            Log.i(TAG, "call sendmessage");
            mHandler.sendMessageDelayed(mHandler.obtainMessage(WAIT_CURSOR_START),
                    WAIT_CURSOR_DELAY_TIME);

        }
        /*
         * Bug Fix by Mediatek End.
         */

    }

    private void startVoicemailStatusQuery() {
        log("startVoicemailStatusQuery()");
        mCallLogQueryHandler.fetchVoicemailStatus();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (mShowOptionsMenu) {
            inflater.inflate(R.menu.call_log_options, menu);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (mShowOptionsMenu) {
            final MenuItem itemDeleteAll = menu.findItem(R.id.delete_all);
            // Check if all the menu items are inflated correctly. As a shortcut, we assume all
            // menu items are ready if the first item is non-null.
            if (itemDeleteAll != null) {
                itemDeleteAll.setEnabled(mAdapter != null && !mAdapter.isEmpty());
                menu.findItem(R.id.show_voicemails_only).setVisible(
                        mVoicemailSourcesAvailable && !mShowingVoicemailOnly);
                menu.findItem(R.id.show_all_calls).setVisible(
                        mVoicemailSourcesAvailable && mShowingVoicemailOnly);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        log("onOptionsItemSelected(), item id = " + item.getItemId());
        switch (item.getItemId()) {
            case R.id.delete_all:
                /**
                * Change Feature by Mediatek Begin.
                * Original Android's Code:
                  ClearCallLogDialog.show(getFragmentManager());
                * Descriptions:
                */
                final Intent intent = new Intent(getActivity(), CallLogMultipleDeleteActivity.class);
                getActivity().startActivity(intent);
                /**
                * Change Feature by Mediatek End.
                */
                return true;

            case R.id.show_voicemails_only:
                mCallLogQueryHandler.fetchVoicemailOnly();
                mShowingVoicemailOnly = true;
                return true;

            case R.id.show_all_calls:
                /**
                * Change Feature by Mediatek Begin.
                * Original Android's Code:
                  mCallLogQueryHandler.fetchAllCalls();
                * Descriptions:
                */
                SharedPreferences prefs = AuroraPreferenceManager.getDefaultSharedPreferences(this.getActivity());
                int simFilter = prefs.getInt(Constants.SIM_FILTER_PREF, Constants.FILTER_SIM_DEFAULT);
                int typeFilter = prefs.getInt(Constants.TYPE_FILTER_PREF, Constants.FILTER_TYPE_DEFAULT);
                mCallLogQueryHandler.fetchCallsJionDataView(simFilter, typeFilter);

                /**
                * Change Feature by Mediatek End.
                */
                mShowingVoicemailOnly = false;
                return true;

            default:
                return false;
        }
    }

    public void callSelectedEntry() {
        log("callSelectedEntry()");
        int position = getListView().getSelectedItemPosition();
        if (position < 0) {
            // In touch mode you may often not have something selected, so
            // just call the first entry to make sure that [send] [send] calls the
            // most recent entry.
            position = 0;
        }
        final Cursor cursor = (Cursor)mAdapter.getItem(position);
        if (cursor != null) {
            String number = cursor.getString(CallLogQuery.NUMBER);
            if (TextUtils.isEmpty(number)
                    || number.equals("-1")
                    || number.equals("-2")
                    || number.equals("-3")) {
                // This number can't be called, do nothing
                return;
            }
            Intent intent;
            // If "number" is really a SIP address, construct a sip: URI.
            if (PhoneNumberUtils.isUriNumber(number)) {
                intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                                    Uri.fromParts("sip", number, null));
                intent.setClassName(Constants.PHONE_PACKAGE, Constants.OUTGOING_CALL_BROADCASTER);
            } else {
                // We're calling a regular PSTN phone number.
                // Construct a tel: URI, but do some other possible cleanup first.
                int callType = cursor.getInt(CallLogQuery.CALL_TYPE);
                if (!number.startsWith("+") &&
                       (callType == Calls.INCOMING_TYPE
                                || callType == Calls.MISSED_TYPE)) {
                    // If the caller-id matches a contact with a better qualified number, use it
                    String countryIso = cursor.getString(CallLogQuery.COUNTRY_ISO);
                    number = mAdapter.getBetterNumberFromContacts(number, countryIso);
                }
                
                if (ContactsApplication.sIsGnContactsSupport) {
                	intent = IntentFactory.newDialNumberIntent(number);
                } else {
                	intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                            Uri.fromParts("tel", number, null));
                }
                
                intent.setClassName(Constants.PHONE_PACKAGE, Constants.OUTGOING_CALL_BROADCASTER);
            }
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
        }
    }

    @VisibleForTesting
    CallLogAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
        log("onVisibilityChanged(), visible = " + visible);
        if (mShowOptionsMenu != visible) {
            mShowOptionsMenu = visible;
            // Invalidate the options menu since we are changing the list of options shown in it.
            Activity activity = getActivity();
            if (activity != null) {
                activity.invalidateOptionsMenu();
            }
        }

        if (visible && isResumed()) {
            refreshData();
        }

        if (!visible) {
            updateOnExit();
        }
    }

    /** Requests updates to the data to be shown. */
    private void refreshData() {
        // Mark all entries in the contact info cache as out of date, so they will be looked up
        // again once being shown.
        log("refreshData()");
        startCallsQuery();
        //Deleted by Mediatek Inc to close Google default Voicemail function.
        updateOnEntry();
    }

    /** Updates call data and notification state while leaving the call log tab. */
    private void updateOnExit() {
        updateOnTransition(false);
    }

    /** Updates call data and notification state while entering the call log tab. */
    private void updateOnEntry() {
        updateOnTransition(true);
    }

    private void updateOnTransition(boolean onEntry) {
        // We don't want to update any call data when keyguard is on because the user has likely not
        // seen the new calls yet.
        if (!mKeyguardManager.inKeyguardRestrictedInputMode()) {
            // On either of the transitions we reset the new flag and update the notifications.
            // While exiting we additionally consume all missed calls (by marking them as read).
            // This will ensure that they no more appear in the "new" section when we return back.
            mCallLogQueryHandler.markNewCallsAsOld();
            if (!onEntry) {
                mCallLogQueryHandler.markMissedCallsAsRead();
            }
 //aurora changed zhouxiaobing 20130910 start          
 //           if (TAB_INDEX_CALL_LOG == ((DialtactsActivity)getActivity()).getCurrentFragmentId()) {
//                removeMissedCallNotifications();
//            }
 //aurora changed zhouxiaobing 20130910 end               
            //updateVoicemailNotifications();
        }
    }

    private void updateVoicemailNotifications() {
        Intent serviceIntent = new Intent(getActivity(), CallLogNotificationsService.class);
        serviceIntent.setAction(CallLogNotificationsService.ACTION_UPDATE_NOTIFICATIONS);
        getActivity().startService(serviceIntent);
    }

    //The following lines are provided and maintained by Mediatek Inc.
    private AuroraButton typeFilter_all;
    private AuroraButton typeFilter_outgoing;
    private AuroraButton typeFilter_incoming;
    private AuroraButton typeFilter_missed;

    private static final int SIM_INFO_UPDATE_MESSAGE = 100;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            log("handleMessage msg==== "+msg.what);

            switch (msg.what) {
                case SIM_INFO_UPDATE_MESSAGE:
                    if (null != mAdapter) {
                        mAdapter.notifyDataSetChanged();
                    }
                    break;

                case WAIT_CURSOR_START:
                    Log.i(TAG, "start WAIT_CURSOR_START !isFinished : " + !isFinished);
                    if (!isFinished) {
                        mEmptyTitle.setText("");
                        mLoadingContainer.setVisibility(View.VISIBLE);
                        mLoadingContact.setVisibility(View.VISIBLE);
                        mProgress.setVisibility(View.VISIBLE);
                    }
                    break;

                default:
                    break;
            }
        }
    };

    /** Called by the CallLogQueryHandler when the list of calls has been deleted. */
    @Override
    public void onCallsDeleted() {
        log("onCallsDeleted(), do nothing");
    }

    private void changeButton(View view) {
        log("changeButton(), view = " + view);
        if (view != typeFilter_all) {
            typeFilter_all.setBackgroundResource(R.drawable.btn_calllog_all);
        } else {
            typeFilter_all.setBackgroundResource(R.drawable.btn_calllog_all_sel);
        }

        if (view != typeFilter_outgoing) {
            typeFilter_outgoing.setBackgroundResource(R.drawable.btn_calllog_incoming);
        } else {
            typeFilter_outgoing.setBackgroundResource(R.drawable.btn_calllog_incoming_sel);
        }

        if (view != typeFilter_incoming) {
            typeFilter_incoming.setBackgroundResource(R.drawable.btn_calllog_incoming);
        } else {
            typeFilter_incoming.setBackgroundResource(R.drawable.btn_calllog_incoming_sel);
        }

        if (view != typeFilter_missed) {
            typeFilter_missed.setBackgroundResource(R.drawable.btn_calllog_missed);
        } else {
            typeFilter_missed.setBackgroundResource(R.drawable.btn_calllog_missed_sel);
        }
    }

    public void onClick(View view) {
        int id = view.getId();
        log("onClick(), view id = " + id);
        SharedPreferences.Editor editor = AuroraPreferenceManager.getDefaultSharedPreferences(
                this.getActivity()).edit();
        switch (id) {
            case R.id.btn_type_filter_all:
                editor.putInt(Constants.TYPE_FILTER_PREF, Constants.FILTER_TYPE_ALL);
                changeButton(view);
                break;
            case R.id.btn_type_filter_outgoing:
                editor.putInt(Constants.TYPE_FILTER_PREF, Constants.FILTER_TYPE_OUTGOING);
                changeButton(view);
                break;
            case R.id.btn_type_filter_incoming:
                editor.putInt(Constants.TYPE_FILTER_PREF, Constants.FILTER_TYPE_INCOMING);
                changeButton(view);
                break;
            case R.id.btn_type_filter_missed:
                editor.putInt(Constants.TYPE_FILTER_PREF, Constants.FILTER_TYPE_MISSED);
                changeButton(view);
                break;
            default:
                break;
        }
        editor.commit();
        refreshData();
    }

    public  AuroraAlertDialog mSelectResDialog = null;

    public void showChoiceResourceDialog() {
        final Resources res = getActivity().getResources();
        final String title = res.getString(R.string.choose_resources_header);
        final String allResourceStr = res.getString(R.string.all_resources);

        // final List<ItemHolder> items = ContactsUtils.createItemHolder(this,
        // allResourceStr, true,
        // null);

        final DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.dismiss();
                SharedPreferences prefs = AuroraPreferenceManager
                        .getDefaultSharedPreferences(getActivity());
                int oriSim = prefs.getInt(Constants.SIM_FILTER_PREF, -1);
                SharedPreferences.Editor editor = AuroraPreferenceManager.getDefaultSharedPreferences(
                        getActivity()).edit();

                AuroraAlertDialog alertDialog = (AuroraAlertDialog) dialog;
                Object obj = alertDialog.getListView().getAdapter().getItem(which);

                Log.i(TAG, "showChoiceResourceDialog OnClick oriSim:" + oriSim + " return:" + obj);

                int resId = 0;
                if (obj instanceof String) {
                    resId = R.string.all_resources;
                } else if (obj instanceof Integer) {
                    if ((Integer) obj == Integer
                            .valueOf((int) ContactsFeatureConstants.VOICE_CALL_SIM_SETTING_INTERNET)) {
                        resId = R.string.call_sipcall;
                    } else if ((Integer) obj == 0) { // Slot 0;
                        resId = R.string.sim1;
                    } else if ((Integer) obj == 1) {
                        resId = R.string.sim2;
                    } else {
                        Log.e(TAG, "OnClick Error! return:" + (Integer) obj);
                    }
                }

                long newsimid = (long) ContactsFeatureConstants.DEFAULT_SIM_NOT_SET;//qc--mtk
                switch (resId) {
                    case R.string.all_resources:
                        if (oriSim == Constants.FILTER_ALL_RESOURCES) {
                            Log.d(TAG, "The current sim " + Constants.FILTER_ALL_RESOURCES);
                            return;
                        }
                        editor.putInt(Constants.SIM_FILTER_PREF, Constants.FILTER_ALL_RESOURCES);
                        newsimid = Constants.FILTER_ALL_RESOURCES;
                        break;
                    // the sim in slot 0
                    case R.string.sim1:
                        int sim1ID = CallLogSimInfoHelper.getSimIdBySlotID(0);
                        if (oriSim == sim1ID) {
                            Log.d(TAG, "The current sim " + sim1ID);
                            return;
                        }
                        editor.putInt(Constants.SIM_FILTER_PREF, (int) sim1ID);
                        newsimid = sim1ID;
                        break;
                    // the sim in slot 1
                    case R.string.sim2:
                        int sim2ID = CallLogSimInfoHelper.getSimIdBySlotID(1);
                        if (oriSim == sim2ID) {
                            Log.d(TAG, "The current sim " + sim2ID);
                            return;
                        }
                        editor.putInt(Constants.SIM_FILTER_PREF, (int) sim2ID);
                        newsimid = sim2ID;
                        break;
                    case R.string.call_sipcall:
                        if (oriSim == Constants.FILTER_SIP_CALL) {
                            Log.d(TAG, "The current sim " + Constants.FILTER_SIP_CALL);
                            return;
                        }
                        editor.putInt(Constants.SIM_FILTER_PREF, Constants.FILTER_SIP_CALL);
                        newsimid = Constants.FILTER_SIP_CALL;
                        break;

                    default: {
                        Log.e(TAG, "Unexpected resource: "
                                + getResources().getResourceEntryName(resId));
                    }
                }
                Log.e(TAG, "showChoiceResourceDialog OnClick user selected:" + newsimid);
                editor.commit();
                refreshData();
            }
        };
		SharedPreferences preference = AuroraPreferenceManager
				.getDefaultSharedPreferences(this.getActivity());
		int choiceItem = preference.getInt(Constants.SIM_FILTER_PREF,
				Constants.FILTER_ALL_RESOURCES);
        log("showChoiceResourceDialog() choiceItem " +choiceItem);
		mSelectResDialog = SimPickerDialog.createSingleChoice(getActivity(),
				title, choiceItem, clickListener);
		mSelectResDialog.show();
    }

    private void log(final String log) {
        Log.i(TAG, log);
    }

    private TextView mLoadingContact;

    private ProgressBar mProgress;

    private TextView mEmptyTitle;

    private View mLoadingContainer;

    public static boolean isFinished = false;

    private static final int WAIT_CURSOR_START = 1230;

    private static final long WAIT_CURSOR_DELAY_TIME = 500;
    
    private CallLogChangeObserver mCallLogChangeObserver;

    private class CallLogChangeObserver extends ContentObserver {
        public CallLogChangeObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            Log.i(TAG, "onChange");
            // When the data is changed,the list view scroll to the first
            // position,otherwise,keep the current position
            mScrollToTop = true;
        }
    }

    // The previous lines are provided and maintained by Mediatek Inc.

}