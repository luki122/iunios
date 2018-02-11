/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.calendar.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;


import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import android.app.Activity;
import android.app.Fragment;
import android.content.AsyncQueryHandler;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.calendar.R;
import com.android.calendar.AsyncQueryService;
import com.android.calendar.CalendarController;
import com.android.calendar.CalendarController.EventHandler;
import com.android.calendar.CalendarController.EventInfo;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarEventModel;
import com.android.calendar.CalendarEventModel.Attendee;
import com.android.calendar.CalendarEventModel.ReminderEntry;
import com.android.calendar.DeleteEventHelper;
import com.android.calendar.Utils;
import com.gionee.calendar.GNDateTextUtils;
import com.gionee.calendar.statistics.StatisticalName;
import com.gionee.calendar.statistics.Statistics;
import com.gionee.calendar.view.GNEditEventView;

public class EditEventFragment extends Fragment implements EventHandler {
    private static final String TAG = "EditEventActivity";

    private static final String BUNDLE_KEY_MODEL = "key_model";
    private static final String BUNDLE_KEY_EDIT_STATE = "key_edit_state";
    private static final String BUNDLE_KEY_EVENT = "key_event";
    private static final String BUNDLE_KEY_READ_ONLY = "key_read_only";
    private static final String BUNDLE_KEY_EDIT_ON_LAUNCH = "key_edit_on_launch";
    //Gionee <jiating><2013-05-11>  modify for CR00000000 begin
    public static final String GN_RECOGNIZE_ACTION = "gn.android.speech.action.RECOGNIZE_SPEECH";
	public static final int GN_RECOGNIZE_ACTION_CODE = 1;
	private static MenuItem  voiceAddActivity;
	private static final String VOICE_VERSION_NAME="202";
	public static  final String VOICE_PACKAGE_NAME="gn.com.voice";
	private static View doneActionView;
	private static TextView saveText;
	private static ImageView saveImage;
    //Gionee <jiating><2013-05-11>  modify for CR00000000 end
    private static final boolean DEBUG = false;

    private static final int TOKEN_EVENT = 1;
    private static final int TOKEN_ATTENDEES = 1 << 1;
    private static final int TOKEN_REMINDERS = 1 << 2;
    private static final int TOKEN_CALENDARS = 1 << 3;
    private static final int TOKEN_ALL = TOKEN_EVENT | TOKEN_ATTENDEES | TOKEN_REMINDERS
            | TOKEN_CALENDARS;
    private static final int TOKEN_UNITIALIZED = 1 << 31;

    ///M: Message used to request view focus.
    private static final int MSG_REQUEST_FOCUS = 1 << 30;
    /**
     * A bitfield of TOKEN_* to keep track which query hasn't been completed
     * yet. Once all queries have returned, the model can be applied to the
     * view.
     */
    private int mOutstandingQueries = TOKEN_UNITIALIZED;

    EditEventHelper mHelper;
    CalendarEventModel mModel;
    CalendarEventModel mOriginalModel;
    CalendarEventModel mRestoreModel;
    GNEditEventView mView;
    QueryHandler mHandler;

    private AuroraAlertDialog mModifyDialog;
    int mModification = Utils.MODIFY_UNINITIALIZED;

    private final EventInfo mEvent;
    private EventBundle mEventBundle;
    private Uri mUri;
    private long mBegin;
    private long mEnd;

    private Activity mContext;
    private final Done mOnDone = new Done();

    private boolean mSaveOnDetach = true;
    private boolean mIsReadOnly = false;
    public boolean mShowModifyDialogOnLaunch = false;

    private InputMethodManager mInputMethodManager;

    private final Intent mIntent;

    private boolean mUseCustomActionBar;

    private final View.OnClickListener mActionBarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onActionBarItemSelected(v.getId());
        }
    };

    // TODO turn this into a helper function in EditEventHelper for building the
    // model
    private class QueryHandler extends AsyncQueryHandler {
        ///M: Handle focus request. @{
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_REQUEST_FOCUS) {
                mView.requestFocus();
                return;
            }
            super.handleMessage(msg);
        }
        ///@}

        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            // If the query didn't return a cursor for some reason return
            if (cursor == null) {
                return;
            }

            // If the AuroraActivity is finishing, then close the cursor.
            // Otherwise, use the new cursor in the adapter.
            final Activity activity = EditEventFragment.this.getActivity();
            if (activity == null || activity.isFinishing()) {
                cursor.close();
                return;
            }
            long eventId;
            switch (token) {
                case TOKEN_EVENT:
                    if (cursor.getCount() == 0) {
                        // The cursor is empty. This can happen if the event
                        // was deleted.
                        cursor.close();
                        mOnDone.setDoneCode(Utils.DONE_EXIT);
                        mSaveOnDetach = false;
                        mOnDone.run();
                        return;
                    }
                    mOriginalModel = new CalendarEventModel();
                    EditEventHelper.setModelFromCursor(mOriginalModel, cursor);
                    EditEventHelper.setModelFromCursor(mModel, cursor);
                    cursor.close();

                    mOriginalModel.mUri = mUri.toString();

                    mModel.mUri = mUri.toString();
                    mModel.mOriginalStart = mBegin;
                    mModel.mOriginalEnd = mEnd;
                    mModel.mIsFirstEventInSeries = mBegin == mOriginalModel.mStart;
                    mModel.mStart = mBegin;
                    mModel.mEnd = mEnd;

                    eventId = mModel.mId;

                    // TOKEN_ATTENDEES
                    if (mModel.mHasAttendeeData && eventId != -1) {
                        Uri attUri = Attendees.CONTENT_URI;
                        String[] whereArgs = {
                            Long.toString(eventId)
                        };
                        mHandler.startQuery(TOKEN_ATTENDEES, null, attUri,
                                EditEventHelper.ATTENDEES_PROJECTION,
                                EditEventHelper.ATTENDEES_WHERE /* selection */,
                                whereArgs /* selection args */, null /* sort order */);
                    } else {
                        setModelIfDone(TOKEN_ATTENDEES);
                    }

                    // TOKEN_REMINDERS
                    if (mModel.mHasAlarm) {
                        Uri rUri = Reminders.CONTENT_URI;
                        String[] remArgs = {
                                Long.toString(eventId)
                        };
                        mHandler.startQuery(TOKEN_REMINDERS, null, rUri,
                                EditEventHelper.REMINDERS_PROJECTION,
                                EditEventHelper.REMINDERS_WHERE /* selection */,
                                remArgs /* selection args */, null /* sort order */);
                    } else {
                        setModelIfDone(TOKEN_REMINDERS);
                    }

                    // TOKEN_CALENDARS
                    String[] selArgs = {
                        Long.toString(mModel.mCalendarId)
                    };
                    mHandler.startQuery(TOKEN_CALENDARS, null, Calendars.CONTENT_URI,
                            EditEventHelper.CALENDARS_PROJECTION, EditEventHelper.CALENDARS_WHERE,
                            selArgs /* selection args */, null /* sort order */);

                    setModelIfDone(TOKEN_EVENT);
                    break;
                case TOKEN_ATTENDEES:
                    try {
                        while (cursor.moveToNext()) {
                            String name = cursor.getString(EditEventHelper.ATTENDEES_INDEX_NAME);
                            String email = cursor.getString(EditEventHelper.ATTENDEES_INDEX_EMAIL);
                            int status = cursor.getInt(EditEventHelper.ATTENDEES_INDEX_STATUS);
                            int relationship = cursor
                                    .getInt(EditEventHelper.ATTENDEES_INDEX_RELATIONSHIP);
                            if (relationship == Attendees.RELATIONSHIP_ORGANIZER) {
                                if (email != null) {
                                    mModel.mOrganizer = email;
                                    mModel.mIsOrganizer = mModel.mOwnerAccount
                                            .equalsIgnoreCase(email);
                                    mOriginalModel.mOrganizer = email;
                                    mOriginalModel.mIsOrganizer = mOriginalModel.mOwnerAccount
                                            .equalsIgnoreCase(email);
                                }

                                if (TextUtils.isEmpty(name)) {
                                    mModel.mOrganizerDisplayName = mModel.mOrganizer;
                                    mOriginalModel.mOrganizerDisplayName =
                                            mOriginalModel.mOrganizer;
                                } else {
                                    mModel.mOrganizerDisplayName = name;
                                    mOriginalModel.mOrganizerDisplayName = name;
                                }
                            }

                            if (email != null) {
                                if (mModel.mOwnerAccount != null &&
                                        mModel.mOwnerAccount.equalsIgnoreCase(email)) {
                                    int attendeeId =
                                        cursor.getInt(EditEventHelper.ATTENDEES_INDEX_ID);
                                    mModel.mOwnerAttendeeId = attendeeId;
                                    mModel.mSelfAttendeeStatus = status;
                                    mOriginalModel.mOwnerAttendeeId = attendeeId;
                                    mOriginalModel.mSelfAttendeeStatus = status;
                                    continue;
                                }
                            }
                            Attendee attendee = new Attendee(name, email);
                            attendee.mStatus = status;
                            mModel.addAttendee(attendee);
                            mOriginalModel.addAttendee(attendee);
                        }
                    } finally {
                        cursor.close();
                    }

                    setModelIfDone(TOKEN_ATTENDEES);
                    break;
                case TOKEN_REMINDERS:
                    try {
                        // Add all reminders to the models
                        while (cursor.moveToNext()) {
                            int minutes = cursor.getInt(EditEventHelper.REMINDERS_INDEX_MINUTES);
                            int method = cursor.getInt(EditEventHelper.REMINDERS_INDEX_METHOD);
                            ReminderEntry re = ReminderEntry.valueOf(minutes, method);
                            mModel.mReminders.add(re);
                            mOriginalModel.mReminders.add(re);
                        }

                        // Sort appropriately for display
                        Collections.sort(mModel.mReminders);
                        Collections.sort(mOriginalModel.mReminders);
                    } finally {
                        cursor.close();
                    }

                    setModelIfDone(TOKEN_REMINDERS);
                    break;
                case TOKEN_CALENDARS:
                    try {
                        ///M:
                        if (mUri==null || mModel.mCalendarId == -1) {
                            // Populate Calendar spinner only if no calendar is set e.g. new event
                            MatrixCursor matrixCursor = Utils.matrixCursorFromCursor(cursor);
                            if (DEBUG) {
                                Log.d(TAG, "onQueryComplete: setting cursor with "
                                        + matrixCursor.getCount() + " calendars");
                            }
                            mView.setCalendarsCursor(matrixCursor, isAdded() && isResumed());
                        } else {
                            // Populate model for an existing event
                            EditEventHelper.setModelFromCalendarCursor(mModel, cursor);
                            EditEventHelper.setModelFromCalendarCursor(mOriginalModel, cursor);
                        }
                    } finally {
                        cursor.close();
                    }

                    setModelIfDone(TOKEN_CALENDARS);
                    break;
                default:
                    cursor.close();
                    break;
            }
        }
    }

    private void setModelIfDone(int queryType) {
        synchronized (this) {
            mOutstandingQueries &= ~queryType;
            if (mOutstandingQueries == 0) {
                if (mRestoreModel != null) {
                    mModel = mRestoreModel;
                }
                if (mShowModifyDialogOnLaunch && mModification == Utils.MODIFY_UNINITIALIZED) {
                    if (!TextUtils.isEmpty(mModel.mRrule)) {
                        displayEditWhichDialog();
                    } else {
                        mModification = Utils.MODIFY_ALL;
                    }

                }
                mView.setModel(mModel);
                mView.setModification(mModification);
                ///M: Send a message to UI thread to request view focus if the view doesn't have the focus. @{
                Message msg = mHandler.obtainMessage(MSG_REQUEST_FOCUS);
                mHandler.sendMessageDelayed(msg, 100);
                ///@}
            }
        }
    }

    public EditEventFragment() {
        this(null, false, null);
    }

    public EditEventFragment(EventInfo event, boolean readOnly, Intent intent) {
        mEvent = event;
        mIsReadOnly = readOnly;
        mIntent = intent;
        setHasOptionsMenu(true);
    }

    private void startQuery() {
        mUri = null;
        mBegin = -1;
        mEnd = -1;
        if (mEvent != null) {
            if (mEvent.id != -1) {
                mModel.mId = mEvent.id;
                mUri = ContentUris.withAppendedId(Events.CONTENT_URI, mEvent.id);
            } else {
                // New event. All day?
                mModel.mAllDay = mEvent.extraLong == CalendarController.EXTRA_CREATE_ALL_DAY;
            }
            if (mEvent.startTime != null) {
                mBegin = mEvent.startTime.toMillis(true);
            }
            if (mEvent.endTime != null) {
                mEnd = mEvent.endTime.toMillis(true);
            }
        } else if (mEventBundle != null) {
            if (mEventBundle.id != -1) {
                mModel.mId = mEventBundle.id;
                mUri = ContentUris.withAppendedId(Events.CONTENT_URI, mEventBundle.id);
            }
            mBegin = mEventBundle.start;
            mEnd = mEventBundle.end;
        }
        //Gionee <jiating><2013-05-13> modify for CR00811147 begin 
       String createTime=GNDateTextUtils.buildMonthYearDayUseFormat(mBegin);
      Log.i("jiating","startQuery,,.....mEvent.startTime...createTime="+createTime);
       String []createTimes=createTime.split("\\.");
       if(Integer.parseInt(createTimes[0])<1970  && Integer.parseInt(createTimes[1])<1  && Integer.parseInt(createTimes[2])<1){
    	   mBegin = mHelper.constructDefaultStartTime(System.currentTimeMillis());
       }
//        if (mBegin <= 0 && mBegin!=(-28800000)) {
//            // use a default value instead
//            mBegin = mHelper.constructDefaultStartTime(System.currentTimeMillis());
//        }
    
        if (mEnd < mBegin || Integer.parseInt(createTimes[0])==1970  && Integer.parseInt(createTimes[1])==1  && Integer.parseInt(createTimes[2])==1) {
            // use a default value instead
            mEnd = mHelper.constructDefaultEndTime(mBegin);
        }
        //Gionee <jiating><2013-05-13> modify for CR00811147 end
        // Kick off the query for the event
        boolean newEvent = mUri == null;
        if (!newEvent) {
            mModel.mCalendarAccessLevel = Calendars.CAL_ACCESS_NONE;
            mOutstandingQueries = TOKEN_ALL;
            if (DEBUG) {
                Log.d(TAG, "startQuery: uri for event is " + mUri.toString());
            }
            mHandler.startQuery(TOKEN_EVENT, null, mUri, EditEventHelper.EVENT_PROJECTION,
                    null /* selection */, null /* selection args */, null /* sort order */);
        } else {
            mOutstandingQueries = TOKEN_CALENDARS;
            if (DEBUG) {
                Log.d(TAG, "startQuery: Editing a new event.");
            }
            mModel.mStart = mBegin;
            mModel.mEnd = mEnd;
            mModel.mSelfAttendeeStatus = Attendees.ATTENDEE_STATUS_ACCEPTED;

            // Start a query in the background to read the list of calendars
            mHandler.startQuery(TOKEN_CALENDARS, null, Calendars.CONTENT_URI,
                    EditEventHelper.CALENDARS_PROJECTION,
                    EditEventHelper.CALENDARS_WHERE_WRITEABLE_VISIBLE, null /* selection args */,
                    null /* sort order */);

            mModification = Utils.MODIFY_ALL;
            mView.setModification(mModification);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;

        mHelper = new EditEventHelper(activity, null);
        mHandler = new QueryHandler(activity.getContentResolver());
        mModel = new CalendarEventModel(activity, mIntent);
        mInputMethodManager = (InputMethodManager)
                activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        mUseCustomActionBar = !Utils.getConfigBool(mContext, R.bool.multiple_pane_config);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
//        mContext.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        View view;
        if (mIsReadOnly) {
            view = inflater.inflate(R.layout.edit_event_single_column, null);
        } else {
            view = inflater.inflate(R.layout.gn_edit_event, null);
        }
        mView = new GNEditEventView(mContext, view, mOnDone);
        startQuery();

        if (mUseCustomActionBar) {
            View actionBarButtons = inflater.inflate(R.layout.edit_event_custom_actionbar,
                    new LinearLayout(mContext), false);
            View cancelActionView = actionBarButtons.findViewById(R.id.action_cancel);
            cancelActionView.setOnClickListener(mActionBarListener);
            doneActionView = actionBarButtons.findViewById(R.id.action_done);
            doneActionView.setOnClickListener(mActionBarListener);
            saveText=(TextView)actionBarButtons.findViewById(R.id.edit_event_shave_text);
            saveImage=(ImageView)actionBarButtons.findViewById(R.id.edit_event_shave_image);
            Log.i("jiating","Aurora.....jiating......mContext.getActionBar()="+(mContext.getActionBar()==null));
            AuroraActivity auroraActivityTemp=(AuroraActivity)mContext;
//            auroraActivityTemp.getAuroraActionBar().setCustomView(actionBarButtons);
        }

        return view;
    }
    
    public void setClickVoiceButton(boolean isClickVoicebutton){
    	mView.setClickVoiceButton(isClickVoicebutton);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mUseCustomActionBar) {
        	 AuroraActivity auroraActivityTemp=(AuroraActivity)mContext;
//             auroraActivityTemp.getAuroraActionBar().setCustomView(null);
            
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(BUNDLE_KEY_MODEL)) {
                mRestoreModel = (CalendarEventModel) savedInstanceState.getSerializable(
                        BUNDLE_KEY_MODEL);
            }
            if (savedInstanceState.containsKey(BUNDLE_KEY_EDIT_STATE)) {
                mModification = savedInstanceState.getInt(BUNDLE_KEY_EDIT_STATE);
            }
            if (savedInstanceState.containsKey(BUNDLE_KEY_EDIT_ON_LAUNCH)) {
                mShowModifyDialogOnLaunch = savedInstanceState
                        .getBoolean(BUNDLE_KEY_EDIT_ON_LAUNCH);
            }
            if (savedInstanceState.containsKey(BUNDLE_KEY_EVENT)) {
                mEventBundle = (EventBundle) savedInstanceState.getSerializable(BUNDLE_KEY_EVENT);
            }
            if (savedInstanceState.containsKey(BUNDLE_KEY_READ_ONLY)) {
                mIsReadOnly = savedInstanceState.getBoolean(BUNDLE_KEY_READ_ONLY);
            }
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (!mUseCustomActionBar) {
            inflater.inflate(R.menu.edit_event_title_bar, menu);
            //Gionee <jiating><2013-05-11>  modify for CR00000000 begin
//        }else{
//        	 inflater.inflate(R.menu.edit_event_voice_add_bar, menu);
//        	 boolean voiceSupport=checkApkExist(mContext,voicePackageName);
//        	 
//        	 voiceAddActivity=menu.findItem(R.id.gn_calendar_voice_add_event);
//        	 if(voiceSupport){
//        		 voiceAddActivity.setVisible(true);
//        	 }else{
//        		 voiceAddActivity.setVisible(false);
//        	 }
//        	 
        }
        //Gionee <jiating><2013-05-11>  modify for CR00000000  end
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return onActionBarItemSelected(item.getItemId());
    }

    /**
     * Handles menu item selections, whether they come from our custom action bar buttons or from
     * the standard menu items. Depends on the menu item ids matching the custom action bar button
     * ids.
     *
     * @param itemId the button or menu item id
     * @return whether the event was handled here
     */
    private boolean onActionBarItemSelected(int itemId) {
    	
    	//Gionee <jiating> <2013-05-22> modify for CR00000000 statistical begin
        if (itemId == R.id.action_done) {
        	if(GNEditEventView.mIsClickMoreButton){
				Statistics.onEvent(mContext, Statistics.EDIT_EVENT_SHAVE_MORE);
        	}else{
				Statistics.onEvent(mContext, Statistics.EDIT_EVENT_SHAVE_NO_MORE);
        	}
        	//Gionee <jiating> <2013-05-22> modify for CR00000000 statistical begin
            if (EditEventHelper.canModifyEvent(mModel) || EditEventHelper.canRespond(mModel)) {
                if (mView != null && mView.prepareForSave()) {
                    if (mModification == Utils.MODIFY_UNINITIALIZED) {
                        mModification = Utils.MODIFY_ALL;
                    }
                    mOnDone.setDoneCode(Utils.DONE_SAVE | Utils.DONE_EXIT);
                    mOnDone.run();
                } else {
                    mOnDone.setDoneCode(Utils.DONE_REVERT);
                    mOnDone.run();
                }
            } else if (EditEventHelper.canAddReminders(mModel) && mModel.mId != -1
                    && mOriginalModel != null && mView.prepareForSave()) {
                saveReminders();
                mOnDone.setDoneCode(Utils.DONE_EXIT);
                mOnDone.run();
            } else {
                mOnDone.setDoneCode(Utils.DONE_REVERT);
                mOnDone.run();
            }
        } else if (itemId == R.id.action_cancel) {
        	//Gionee <jiating> <2013-05-22> modify for CR00000000 statistical begin
			Statistics.onEvent(mContext, Statistics.EDIT_EVENT_CANCLE);
        	//Gionee <jiating> <2013-05-22> modify for CR00000000 statistical end
            mOnDone.setDoneCode(Utils.DONE_REVERT);
            mOnDone.run();
            //Gionee <jiating><2013-05-11>  modify for CR00000000 begin
        }else if(itemId==R.id.gn_calendar_voice_add_event){
        	Intent intent = new Intent(GN_RECOGNIZE_ACTION); 
			intent.putExtra("scene", "schedule_create");
			intent.putExtra("appid", "com.android.calendar");
			mContext.startActivityForResult(intent, GN_RECOGNIZE_ACTION_CODE);
        }
        //Gionee <jiating><2013-05-11>  modify for CR00000000 end
        return true;
    }

    private void saveReminders() {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>(3);
        boolean changed = EditEventHelper.saveReminders(ops, mModel.mId, mModel.mReminders,
                mOriginalModel.mReminders, false /* no force save */);

        if (!changed) {
            return;
        }

        AsyncQueryService service = new AsyncQueryService(getActivity());
        service.startBatch(0, null, Calendars.CONTENT_URI.getAuthority(), ops, 0);
        // Update the "hasAlarm" field for the event
        Uri uri = ContentUris.withAppendedId(Events.CONTENT_URI, mModel.mId);
        int len = mModel.mReminders.size();
        boolean hasAlarm = len > 0;
        if (hasAlarm != mOriginalModel.mHasAlarm) {
            ContentValues values = new ContentValues();
            values.put(Events.HAS_ALARM, hasAlarm ? 1 : 0);
            service.startUpdate(0, null, uri, values, null, null, 0);
        }

        Toast.makeText(mContext, R.string.saving_event, Toast.LENGTH_SHORT).show();
    }

    protected void displayEditWhichDialog() {
        if (mModification == Utils.MODIFY_UNINITIALIZED) {
            final boolean notSynced = TextUtils.isEmpty(mModel.mSyncId);
            boolean isFirstEventInSeries = mModel.mIsFirstEventInSeries;
            int itemIndex = 0;
            CharSequence[] items;

            if (notSynced) {
                // If this event has not been synced, then don't allow deleting
                // or changing a single instance.
                if (isFirstEventInSeries) {
                    // Still display the option so the user knows all events are
                    // changing
                    items = new CharSequence[1];
                } else {
                    items = new CharSequence[2];
                }
            } else {
                if (isFirstEventInSeries) {
                    items = new CharSequence[2];
                } else {
                    items = new CharSequence[3];
                }
                items[itemIndex++] = mContext.getText(R.string.modify_event);
            }
            items[itemIndex++] = mContext.getText(R.string.modify_all);

            // Do one more check to make sure this remains at the end of the list
            if (!isFirstEventInSeries) {
                items[itemIndex++] = mContext.getText(R.string.modify_all_following);
            }

            // Display the modification dialog.
            if (mModifyDialog != null) {
                mModifyDialog.dismiss();
                mModifyDialog = null;
            }
            mModifyDialog = new AuroraAlertDialog.Builder(mContext).setTitle(R.string.edit_event_label)
                    .setItems(items, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                // Update this if we start allowing exceptions
                                // to unsynced events in the app
                                mModification = notSynced ? Utils.MODIFY_ALL
                                        : Utils.MODIFY_SELECTED;
                                if (mModification == Utils.MODIFY_SELECTED) {
                                    mModel.mOriginalSyncId = notSynced ? null : mModel.mSyncId;
                                    mModel.mOriginalId = mModel.mId;
                                }
                            } else if (which == 1) {
                                mModification = notSynced ? Utils.MODIFY_ALL_FOLLOWING
                                        : Utils.MODIFY_ALL;
                            } else if (which == 2) {
                                mModification = Utils.MODIFY_ALL_FOLLOWING;
                            }

                            mView.setModification(mModification);
                        }
                    }).show();

            mModifyDialog.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Activity a = EditEventFragment.this.getActivity();
                    if (a != null) {
                        a.finish();
                    }
                }
            });
        }
    }

    class Done implements EditEventHelper.EditDoneRunnable {
        private int mCode = -1;

        public void setDoneCode(int code) {
            mCode = code;
        }

        public void run() {
            // We only want this to get called once, either because the user
            // pressed back/home or one of the buttons on screen
            mSaveOnDetach = false;
            if (mModification == Utils.MODIFY_UNINITIALIZED) {
                // If this is uninitialized the user hit back, the only
                // changeable item is response to default to all events.
                mModification = Utils.MODIFY_ALL;
            }

            if ((mCode & Utils.DONE_SAVE) != 0 && mModel != null
                    && (EditEventHelper.canRespond(mModel)
                            || EditEventHelper.canModifyEvent(mModel))
                    && mView.prepareForSave()
                    && !isEmptyNewEvent()
                    && mModel.normalizeReminders()
                    && mHelper.saveEvent(mModel, mOriginalModel, mModification)) {
                int stringResource;
                if (!mModel.mAttendeesList.isEmpty()) {
                    if (mModel.mUri != null) {
                        stringResource = R.string.saving_event_with_guest;
                    } else {
                        stringResource = R.string.creating_event_with_guest;
                    }
                } else {
                    if (mModel.mUri != null) {
                        stringResource = R.string.saving_event;
                    } else {
                        stringResource = R.string.creating_event;
                    }
                }
                Toast.makeText(mContext, stringResource, Toast.LENGTH_SHORT).show();
            } else if ((mCode & Utils.DONE_SAVE) != 0 && mModel != null && isEmptyNewEvent()) {
                Toast.makeText(mContext, R.string.empty_event, Toast.LENGTH_SHORT).show();
            }

            if ((mCode & Utils.DONE_DELETE) != 0 && mOriginalModel != null
                    && EditEventHelper.canModifyCalendar(mOriginalModel)) {
                long begin = mModel.mStart;
                long end = mModel.mEnd;
                int which = -1;
                switch (mModification) {
                    case Utils.MODIFY_SELECTED:
                        which = DeleteEventHelper.DELETE_SELECTED;
                        break;
                    case Utils.MODIFY_ALL_FOLLOWING:
                        which = DeleteEventHelper.DELETE_ALL_FOLLOWING;
                        break;
                    case Utils.MODIFY_ALL:
                        which = DeleteEventHelper.DELETE_ALL;
                        break;
                }
                DeleteEventHelper deleteHelper = new DeleteEventHelper(
                        mContext, mContext, !mIsReadOnly /* exitWhenDone */);
                deleteHelper.delete(begin, end, mOriginalModel, which);
            }

            if ((mCode & Utils.DONE_EXIT) != 0) {
                // This will exit the edit event screen, should be called
                // when we want to return to the main calendar views
                if ((mCode & Utils.DONE_SAVE) != 0) {
                    if (mContext != null) {
                        long start = mModel.mStart;
                        long end = mModel.mEnd;
                        if (mModel.mAllDay) {
                            // For allday events we want to go to the day in the
                            // user's current tz
                            String tz = Utils.getTimeZone(mContext, null);
                            Time t = new Time(Time.TIMEZONE_UTC);
                            t.set(start);
                            t.timezone = tz;
                            start = t.toMillis(true);

                            t.timezone = Time.TIMEZONE_UTC;
                            t.set(end);
                            t.timezone = tz;
                            end = t.toMillis(true);
                        }
                        CalendarController.getInstance(mContext).launchViewEvent(-1, start, end,
                                Attendees.ATTENDEE_STATUS_NONE);
                    }
                }
                Activity a = EditEventFragment.this.getActivity();
                if (a != null) {
                    a.finish();
                }
            }

            // Hide a software keyboard so that user won't see it even after this Fragment's
            // disappearing.
            final View focusedView = mContext.getCurrentFocus();
            if (focusedView != null) {
                mInputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
                focusedView.clearFocus();
            }
        }
    }

    boolean isEmptyNewEvent() {
        if (mOriginalModel != null) {
            // Not new
            return false;
        }

        return isEmpty();
    }

    private boolean isEmpty() {
        if (mModel.mTitle != null) {
            String title = mModel.mTitle.trim();
            if (title.length() > 0) {
                return false;
            }
        }

        if (mModel.mLocation != null) {
            String location = mModel.mLocation.trim();
            if (location.length() > 0) {
                return false;
            }
        }

        if (mModel.mDescription != null) {
            String description = mModel.mDescription.trim();
            if (description.length() > 0) {
                return false;
            }
        }

        return true;
    }

    ///M: save the event in onDestory. @{
    @Override
    public void onDestroy() {
    	
    	//Gionee <jiating><2013-06-17> modify for CR00825993 begin
        Activity act = getActivity();
        ///M:@{
        if (mSaveOnDetach && act != null && !mIsReadOnly
                && !act.isChangingConfigurations() && mView.prepareForSave()) {
            mOnDone.setDoneCode(Utils.DONE_SAVE);
            mOnDone.run();
        }
//        /@}
    	//Gionee <jiating><2013-06-17> modify for CR00825993 end
    	Log.i("jiating","EditEvedntFragemnt...onDestroy");
        if (mView != null) {
            mView.setModel(null);
        }
        if (mModifyDialog != null) {
            mModifyDialog.dismiss();
            mModifyDialog = null;
        }
        super.onDestroy();
    }
	//Gionee <jiating><2013-06-17> modify for CR00825993 begin
//    @Override
//    public void onPause() {
//    	// TODO Auto-generated method stub
//    	super.onPause();
//    	 AuroraActivity act = getActivity();
//         ///M:@{
//         Log.i("jiating","EditEvedntFragemnt...onPause");
//       //Gionee <jiating><2013-06-21> modify for CR00828338 begin
//         if(!GNEditEventView.isClickVoiceButton()){
//    	  if (mSaveOnDetach && act != null && !mIsReadOnly
//                  && !act.isChangingConfigurations() && mView.prepareForSave()) {
//              mOnDone.setDoneCode(Utils.DONE_SAVE);
//              mOnDone.run();
//          }
//         }
//    	  
//    	   //Gionee <jiating><2013-06-21> modify for CR00828338 end
//    }
	//Gionee <jiating><2013-06-17> modify for CR00825993 end
    @Override
	public void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
   	 	Statistics.onPause(mContext);
    }
    
    @Override
    public void eventsChanged() {
        // TODO Requery to see if event has changed
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mView.prepareForSave();
        outState.putSerializable(BUNDLE_KEY_MODEL, mModel);
        outState.putInt(BUNDLE_KEY_EDIT_STATE, mModification);
        if (mEventBundle == null && mEvent != null) {
            mEventBundle = new EventBundle();
            mEventBundle.id = mEvent.id;
            if (mEvent.startTime != null) {
                mEventBundle.start = mEvent.startTime.toMillis(true);
            }
            if (mEvent.endTime != null) {
                mEventBundle.end = mEvent.startTime.toMillis(true);
            }
        }
        outState.putBoolean(BUNDLE_KEY_EDIT_ON_LAUNCH, mShowModifyDialogOnLaunch);
        outState.putSerializable(BUNDLE_KEY_EVENT, mEventBundle);
        outState.putBoolean(BUNDLE_KEY_READ_ONLY, mIsReadOnly);
        ///M:
        mIsSaveInstanceState = true;
    }

    @Override
    public long getSupportedEventTypes() {
        return EventType.USER_HOME;
    }

    @Override
    public void handleEvent(EventInfo event) {
        // It's currently unclear if we want to save the event or not when home
        // is pressed. When creating a new event we shouldn't save since we
        // can't get the id of the new event easily.
        if ((false && event.eventType == EventType.USER_HOME) || (event.eventType == EventType.GO_TO
                && mSaveOnDetach)) {
            if (mView != null && mView.prepareForSave()) {
                mOnDone.setDoneCode(Utils.DONE_SAVE);
                mOnDone.run();
            }
        }
    }

    private static class EventBundle implements Serializable {
        private static final long serialVersionUID = 1L;
        long id = -1;
        long start = -1;
        long end = -1;
    }

//////////////////////////////////////////////////////////////////////////
///M: MTK code below
///TODO: extract these code out

    @Override
    public void onResume() {
        super.onResume();
        extOnResume();
        Statistics.onResume(mContext);
    }

    private void extOnResume() {
        ///M: #extension# @{
        mView.doOnResume();
        ///@}
    }
    
    public GNEditEventView getGNEditEventView(){
    	return mView;
    }
    
    ///M:@{
    public void toastText(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }
    ///@}
    
    ///M: to mark whether the fragment be saved?
    private boolean mIsSaveInstanceState = false;

    ///M: keep DatePickerDialog and TimePickerDialog when rotate device @{
    GNEditEventView getEditEventView() {
        return mView;
    }
    ///@}
    
    //Gionee <jiating><2013-05-11>  modify for CR00000000 begin 
    
    public static boolean checkApkExist(Context context, String packageName) {
        if (packageName == null || "".equals(packageName)){
        	Log.i("jiating","checkApkExist.....");
                return false;
        }
        try {
                ApplicationInfo info = context.getPackageManager()
                                .getApplicationInfo(packageName,
                                                PackageManager.GET_UNINSTALLED_PACKAGES);
               PackageInfo  packageInfo= context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
               Log.i("jiating","checkApkExist...packageInfo.versionCode="+packageInfo.versionCode+"packageInfo.versionName"+packageInfo.versionName);
               String str=packageInfo.versionName.replaceAll("[a-z]", "."); 
               String []strs=str.split("\\.");
               StringBuilder sb = new StringBuilder();
				 for(int i=0;i<strs.length;i++){
					 sb.append(strs[i]);
					 Log.i("jiating","checkApkExist......strs="+strs[i]);
					 
				 }
               if(Integer.parseInt(sb.toString())>=Integer.parseInt(VOICE_VERSION_NAME)){
            	   return true;
               }else {
            	   return false;
               }
               
              
        } catch (NameNotFoundException e) {
        	Log.i("jiating","checkApkExist...NameNotFoundException");
                return false;
        }
    }
    
    public static void setSaveButtonVisible(boolean isVisible){
    	Log.i("jiating","EditEventFragment...setSaveButtonVisible...isVisible="+isVisible);
    	 doneActionView.setClickable(isVisible);
    	 saveText.setEnabled(isVisible);
    	 saveImage.setEnabled(isVisible);

 
    	 
    }
 // Gionee <jiating><2013-06-25> modify for CR00829225 begin    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	// TODO Auto-generated method stub
    	super.onConfigurationChanged(newConfig);
    }
 // Gionee <jiating><2013-06-25> modify for CR00829225 end 
    
    //Gionee <jiating><2013-05-11>  modify for CR00000000 end
}
